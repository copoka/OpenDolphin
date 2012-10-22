package open.dolphin.ejb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;

import open.dolphin.infomodel.IStampTreeModel;
import open.dolphin.infomodel.PublishedTreeModel;
import open.dolphin.infomodel.StampModel;
import open.dolphin.infomodel.StampTreeModel;
import open.dolphin.infomodel.SubscribedTreeModel;

@Stateless
@SecurityDomain("openDolphin")
@RolesAllowed("user")
@Remote({RemoteStampService.class})
@RemoteBinding(jndiBinding="openDolphin/RemoteStampService")
public class RemoteStampServiceImpl extends DolphinService implements RemoteStampService {
    
    @Resource
    private SessionContext ctx;
    
    @PersistenceContext
    private EntityManager em;
    
    /**
     * user�l��StampTree��ۑ�/�X�V����B
     * @param model �ۑ����� StampTree
     * @return id
     */
    @Override
    public long putTree(StampTreeModel model) {
        
        StampTreeModel saveOrUpdate = em.merge(model);
        return saveOrUpdate.getId();
    }
    
    /**
     * User�l�y�уT�u�X�N���C�u���Ă���Tree���擾����B
     * @param userPk userId(DB key)
     * @return User�l�y�уT�u�X�N���C�u���Ă���Tree�̃��X�g
     */
    @Override
    public List<IStampTreeModel> getTrees(long userPk) {
        
        List<IStampTreeModel> ret = new ArrayList<IStampTreeModel>();
        
        //
        // �p�[�\�i���c���[���擾����
        //
        List<StampTreeModel> list = (List<StampTreeModel>) em.createQuery("from StampTreeModel s where s.user.id=:userPk")
                                                             .setParameter("userPk", userPk)
                                                             .getResultList();

        // �V�K���[�U�̏ꍇ
        if (list.size() == 0) {
            return ret;
        }

        // �ŏ��� Tree ��ǉ�
        StampTreeModel st = (StampTreeModel) list.remove(0);
        ret.add(st);

        // �܂�����ꍇ BUG
        if (list.size() > 0) {
            // ��� delete ����
            for (int i=0; i < list.size(); i++) {
                st = (StampTreeModel) list.remove(0);
                em.remove(st);
            }
        }

        //
        // ���[�U���T�u�X�N���C�u���Ă���StampTree�̃��X�g���擾����
        //
        List<SubscribedTreeModel> subscribed = (List<SubscribedTreeModel>)em.createQuery("from SubscribedTreeModel s where s.user.id=:userPk")
                            .setParameter("userPk", userPk)
                            .getResultList();

        HashMap tmp = new HashMap(5, 0.8f);

        for (SubscribedTreeModel sm : subscribed) {

            // BUG �d�����`�F�b�N����
            if (tmp.get(sm.getTreeId()) == null) {

                // �܂����݂��Ȃ��ꍇ
                tmp.put(sm.getTreeId(), "A");

                try {
                    PublishedTreeModel published = (PublishedTreeModel) em.find(PublishedTreeModel.class, sm.getTreeId());

                    if (published != null) {
                        ret.add(published);

                    } else {
                        em.remove(sm);
                    }

                } catch (NoResultException e) {
                    em.remove(sm);
                }

            } else {
                // �d�����ăC���|�[�g���Ă���ꍇ�ɍ폜����
                em.remove(sm);
            }
        }
        
        return ret;
    }
    
    /**
     * �܂��ۑ�����Ă��Ȃ��l�p��Tree��ۑ������J����B
     */
    @Override
    public long saveAndPublishTree(StampTreeModel model, byte[] publishBytes) {
        
        //
        // �ŏ��ɕۑ�����
        //
        em.persist(model);
        
        //
        // ���J�pTree���f���𐶐����l���R�s�[����
        // ���JTree��id=�l�pTree��Id
        //
        PublishedTreeModel publishedModel = new PublishedTreeModel();
        publishedModel.setId(model.getId());
        publishedModel.setUser(model.getUser());
        publishedModel.setName(model.getName());
        publishedModel.setPublishType(model.getPublishType());
        publishedModel.setCategory(model.getCategory());
        publishedModel.setPartyName(model.getPartyName());
        publishedModel.setUrl(model.getUrl());
        publishedModel.setDescription(model.getDescription());
        publishedModel.setPublishedDate(model.getPublishedDate());
        publishedModel.setLastUpdated(model.getLastUpdated());
        publishedModel.setTreeBytes(publishBytes);
        
        //
        // ���JTree��ۑ�����
        //
        em.persist(publishedModel);
        
        // id ��Ԃ�
        return model.getId();
        
    }
    
    
    /**
     * �ۑ�����Ă���l�p��Tree��V�K�Ɍ��J����B
     * @param model ���J����StampTree
     */
    @Override
    public int publishTree(StampTreeModel model, byte[] publishBytes) {
        
        //
        // �ŏ��ɍX�V����
        //
        em.merge(model);
        
        //
        // ���J�pStampTreeModel�𐶐����l���R�s�[����
        // ���JTree��id=�l�pTree��Id
        //
        PublishedTreeModel publishedModel = new PublishedTreeModel();
        publishedModel.setId(model.getId());                            // pk
        publishedModel.setUser(model.getUser());                        // UserModel
        publishedModel.setName(model.getName());                        // ����
        publishedModel.setPublishType(model.getPublishType());          // ���J�^�C�v
        publishedModel.setCategory(model.getCategory());                // �J�e�S��
        publishedModel.setPartyName(model.getPartyName());              // �p�[�e�B�[��
        publishedModel.setUrl(model.getUrl());                          // URL
        publishedModel.setDescription(model.getDescription());          // ����
        publishedModel.setPublishedDate(model.getPublishedDate());      // ���J��
        publishedModel.setLastUpdated(model.getLastUpdated());          // �X�V��
        publishedModel.setTreeBytes(publishBytes);                      // XML bytes
        
        //
        // ���JTree��ۑ�����
        //
        em.persist(publishedModel);
        
        return 1;
    }
    
    /**
     * ���J���Ă���Tree���X�V����B
     * @param model ���J���Ă���Tree
     * @return �X�V������
     */
    @Override
    public int updatePublishedTree(StampTreeModel model, byte[] publishBytes) {
        
        //
        // �ŏ��ɍX�V����
        //
        em.merge(model);
        
        //
        // ���J�pTree�փR�s�[����
        //
        PublishedTreeModel publishedModel = new PublishedTreeModel();
        publishedModel.setId(model.getId());
        publishedModel.setUser(model.getUser());
        publishedModel.setName(model.getName());
        publishedModel.setPublishType(model.getPublishType());
        publishedModel.setCategory(model.getCategory());
        publishedModel.setPartyName(model.getPartyName());
        publishedModel.setUrl(model.getUrl());
        publishedModel.setDescription(model.getDescription());
        publishedModel.setPublishedDate(model.getPublishedDate());
        publishedModel.setLastUpdated(model.getLastUpdated());
        publishedModel.setTreeBytes(publishBytes);
        
        //
        // ���JTree���X�V����
        // �������l��ݒ肷��ق��������̂ł͂Ȃ���?
        //
        em.merge(publishedModel);
        
        return 1;
        
    }
    
    /**
     * ���J����Tree���폜����B
     * @param id �폜����Tree��Id
     * @return �폜������
     */
    @Override
    public int cancelPublishedTree(StampTreeModel model) {
        //
        // ���J�������X�V����
        //
        em.merge(model);
        
        //
        // ���JTree���폜����
        //
        PublishedTreeModel exist = (PublishedTreeModel) em.find(PublishedTreeModel.class, model.getId());
        em.remove(exist);
        
        return 1;
    }
    
    /**
     * ���J����Ă���StampTree�̃��X�g���擾����B
     * @return ���[�J���y�уp�u���b�NTree�̃��X�g
     */
    @Override
    public List<PublishedTreeModel> getPublishedTrees() {
        
        // ���O�C�����[�U�̎{��ID���擾����
        String fid = this.getCallersFacilityId(ctx);
        
        List<PublishedTreeModel> ret = new ArrayList<PublishedTreeModel>();
        
        // local �Ɍ��J����Ă���Tree���擾����
        // publishType=�{��ID
        List locals = em.createQuery("from PublishedTreeModel p where p.publishType=:fid")
        .setParameter("fid", fid)
        .getResultList();
        ret.addAll((List<PublishedTreeModel>) locals);
        
        // �p�u���b�NTee���擾����
        List publics = em.createQuery("from PublishedTreeModel p where p.publishType='global'")
        .getResultList();
        ret.addAll((List<PublishedTreeModel>) publics);
        
        return ret;
    }
    
    /**
     * ���JTree�ɃT�u�X�N���C�u����B
     * @param addList �T�u�X�N���C�u����
     * @return
     */
    @Override
    public List<Long> subscribeTrees(List<SubscribedTreeModel> addList) {
        
        List<Long> ret = new ArrayList<Long>();
        for (SubscribedTreeModel model : addList) {
            em.persist(model);
            ret.add(new Long(model.getId()));
        }
        return ret;
    }
    
    /**
     * ���JTree�ɃA���T�u�X�N���C�u����B
     * @param ids �A���T�u�X�N���C�u����Tree��Id���X�g
     * @return
     */
    @Override
    public int unsubscribeTrees(List<SubscribedTreeModel> removeList) {
        
        int cnt = 0;
        
        for (SubscribedTreeModel model : removeList) {
            List<SubscribedTreeModel> removes = (List<SubscribedTreeModel>) em.createQuery("from SubscribedTreeModel s where s.user.id=:userPk and s.treeId=:treeId")
            .setParameter("userPk", model.getUser().getId())
            .setParameter("treeId", model.getTreeId())
            .getResultList();
            
            for (SubscribedTreeModel sm : removes) {
                em.remove(sm);
            }
            cnt++;
        }
        
        return cnt;
    }
    
    /**
     * ASP StampTree���擾����B
     * @param managerId ASP Tree�}�l�[�W��ID
     * @return ASP�񋟂�StampTree
     */
    @Override
    public StampTreeModel getAspTree(String managerId) {
        
        StampTreeModel ret = null;
        
        try {
            ret =  (StampTreeModel)em.createQuery("from StampTreeModel s where s.userId = :mid")
            .setParameter("mid", managerId)
            .getSingleResult();
        } catch (NoResultException e) {
        }
        return ret;
    }
    
    /**
     * Stamp��ۑ�����B
     * @param model StampModel
     * @return �ۑ�����
     */
    @Override
    public List<String> putStamp(List<StampModel> list) {
        List<String> ret = new ArrayList<String>();
        for (StampModel model : list) {
            em.persist(model);
            ret.add(model.getId());
        }
        return ret;
    }
    
    /**
     * Stamp��ۑ�����B
     * @param model StampModel
     * @return �ۑ�����
     */
    @Override
    public String putStamp(StampModel model) {
        em.persist(model);
        return model.getId();
    }
    
    /**
     * Stamp���擾����B
     * @param stampId �擾���� StampModel �� id
     * @return StampModel
     */
    @Override
    public StampModel getStamp(String stampId) {
        
        try {
            return (StampModel) em.find(StampModel.class, stampId);
        } catch (NoResultException e) {
        }
        
        return null;
    }
    
    /**
     * Stamp���擾����B
     * @param stampId �擾���� StampModel �� id
     * @return StampModel
     */
    @Override
    public List<StampModel> getStamp(List<String> ids) {
        
        List<StampModel> ret = new ArrayList<StampModel>();
        
        try {
            for (String stampId : ids) {
                StampModel test = (StampModel) em.find(StampModel.class, stampId);
                ret.add(test);
            }
        } catch (Exception e) {
        }
        
        return ret;
    }
    
    /**
     * Stamp���폜����B
     * @param stampId �폜���� StampModel �� id
     * @return �폜����
     */
    @Override
    public int removeStamp(String stampId) {
        StampModel exist = (StampModel) em.find(StampModel.class, stampId);
        em.remove(exist);
        return 1;
    }
    
    /**
     * Stamp���폜����B
     * @param stampId �폜���� StampModel �� id List
     * @return �폜����
     */
    @Override
    public int removeStamp(List<String> ids) {   
        int cnt =0;
        for (String stampId : ids) {
            StampModel exist = (StampModel) em.find(StampModel.class, stampId);
            em.remove(exist);
            cnt++;
        }
        return cnt;
    }
}
