package open.dolphin.ejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import open.dolphin.infomodel.FacilityModel;
import open.dolphin.infomodel.PublishedTreeModel;
import open.dolphin.infomodel.StampModel;
import open.dolphin.infomodel.StampTreeModel;
import open.dolphin.infomodel.SubscribedTreeModel;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;

import open.dolphin.infomodel.UserModel;

@Stateless
@SecurityDomain("openDolphin")
@Remote({RemoteUserService.class})
@RemoteBinding(jndiBinding="openDolphin/RemoteUserService")
public class RemoteUserServiceImpl extends DolphinService implements RemoteUserService {
    
    private static final String ASP_MEMBER = "ASP_MEMBER";
    private static final String QUEUE_JNDI = "queue/tutorial/example";
    
    @Resource
    private SessionContext ctx;
    
    @PersistenceContext
    private EntityManager em;
    
    /**
     * �{�݊Ǘ��҂��@��User��o�^����B
     * @param add �o�^����User
     */
    @RolesAllowed("admin")
    public int addUser(UserModel add) {
        
        try {
            // �������[�U�̏ꍇ�͗�O���X���[����
            getUser(add.getUserId());
            throw new EntityExistsException();
        } catch (NoResultException e) {
        }
        em.persist(add);
        return 1;
    }
    
    /**
     * User����������B
     * @param userId �������郆�[�U�̕����L�[
     * @return �Y������User
     */
    @RolesAllowed("user")
    public UserModel getUser(String userId) {
        checkIdAsComposite(ctx, userId);
        UserModel user = (UserModel) em.createQuery("from UserModel u where u.userId = :uid")
        .setParameter("uid", userId)
        .getSingleResult();
        
        if (user.getMemberType() != null && user.getMemberType().equals("EXPIRED")) {
            throw new SecurityException("Expired User");
        }
        return user;
    }
    
    /**
     * �{�ݓ��̑SUser���擾����B
     *
     * @return �{�ݓ����[�U���X�g
     */
    @SuppressWarnings("unchecked")
    @RolesAllowed("admin")
    public Collection<UserModel> getAllUser() {
        Collection results = em.createQuery("from UserModel u where u.userId like :fid")
        .setParameter("fid", getCallersFacilityId(ctx)+":%")
        .getResultList();
        
        Collection<UserModel> ret = new ArrayList<UserModel>();
        for (Iterator iter = results.iterator(); iter.hasNext(); ) {
            UserModel user = (UserModel) iter.next();
            if (user.getMemberType() != null && (!user.getMemberType().equals("EXPIRED"))) {
                ret.add(user);
            }
        }
        return ret;
    }
    
    /**
     * User���(�p�X���[�h��)���X�V����B
     * @param update �X�V����User detuched
     */
    @RolesAllowed("user")
    public int updateUser(UserModel update) {
        //checkFacility(ctx, update.getUserId());
        UserModel current = (UserModel) em.find(UserModel.class, update.getId());
        update.setMemberType(current.getMemberType());
        update.setRegisteredDate(current.getRegisteredDate());
        em.merge(update);   //em.refresh(update);
        return 1;
    }
    
    /**
     * User���폜����B
     * @param removeId �폜���郆�[�U��Id
     */
    @RolesAllowed("admin")
    public int removeUser(String removeId) {
        
        //
        // �폜���郆�[�U�𓾂�
        //
        UserModel remove = getUser(removeId);
        
        // Stamp ���폜����
        Collection<StampModel> stamps = (Collection<StampModel>) em.createQuery("from StampModel s where s.userId = :pk")
                                                                   .setParameter("pk", remove.getId())
                                                                   .getResultList();
        for (StampModel stamp : stamps) {
            em.remove(stamp);
        }

        // Subscribed Tree ���폜����
        Collection<SubscribedTreeModel> subscribedTrees = (Collection<SubscribedTreeModel>) 
                                                          em.createQuery("from SubscribedTreeModel s where s.user.id = :pk")
                                                            .setParameter("pk", remove.getId())
                                                            .getResultList();
        for (SubscribedTreeModel tree : subscribedTrees) {
            em.remove(tree);
        }

        // PublishedTree ���폜����
        Collection<PublishedTreeModel> publishedTrees = (Collection<PublishedTreeModel>)
                                                         em.createQuery("from PublishedTreeModel p where p.user.id = :pk")
                                                           .setParameter("pk", remove.getId())
                                                           .getResultList();
        for (PublishedTreeModel tree : publishedTrees) {
            em.remove(tree);
        }

        // PersonalTree���폜����
        Collection<StampTreeModel> stampTree = (Collection<StampTreeModel>) em.createQuery("from StampTreeModel s where s.user.id = :pk")
                                                      .setParameter("pk", remove.getId())
                                                      .getResultList();
        for (StampTreeModel tree : stampTree) {
            em.remove(tree);
        }
        
        //
        // ���[�U���폜����
        //
        if (remove.getLicenseModel().getLicense().equals("doctor")) {
            StringBuilder sb = new StringBuilder();
            remove.setMemo(sb.toString());
            remove.setMemberType("EXPIRED");
            remove.setPassword("c9dbeb1de83e60eb1eb3675fa7d69a02");
        } else {
            em.remove(remove);
        }
        
        return 1;
    }
    
    /**
     * �{�ݏ����X�V����B
     * @param update �X�V����User detuched
     */
    @RolesAllowed("admin")
    public int updateFacility(UserModel update) {
        checkFacility(ctx, update.getUserId());
        FacilityModel updateFacility = update.getFacilityModel();
        FacilityModel current = (FacilityModel) em.find(FacilityModel.class, updateFacility.getId());
        updateFacility.setMemberType(current.getMemberType());
        updateFacility.setRegisteredDate(current.getRegisteredDate());
        em.merge(updateFacility );
        return 1;
    }
    
    /**
     * �T�|�[�g���C�Z���X���w������B
     * @param purchase detuched User
     */
    public void purchase(UserModel purchase) {
        
        // �w�����ƃ����o�[�^�C�v���{�݂ɐݒ肷��
        Date purchaseDate = new Date();
        FacilityModel facility = purchase.getFacilityModel();
        facility.setMemberType(ASP_MEMBER);
        facility.setRegisteredDate(purchaseDate);
        
        // Merge �����I�u�W�F�N�g�𓾂�
        FacilityModel member = (FacilityModel) em.merge(facility);
        
        // �{�ݓ����[�U�S�����X�V����
        Collection<UserModel> userList = (Collection<UserModel>) em.createQuery("from UserModel u where u.facility.id = :fid")
                                                                   .setParameter("fid", member.getId()).getResultList();
        
        for (UserModel user : userList) {
            user.setMemberType(ASP_MEMBER);
            user.setRegisteredDate(purchaseDate);
            em.merge(user);
        }
        
        // TODO �w����ɃV���b�g�_�E���������[�U�����X�V�����ꍇ
        // �{�݂ƃ��[�U�̃����o�[�y�ѓo�^���f�[�^�͕ύX���Ȃ�
        
        // Mail�Œʒm���邽��MessageDrivenBean�ɓn��
        try {
            QueueConnection cnn = null;
            QueueSender sender = null;
            QueueSession session = null;
            InitialContext ict = new InitialContext();
            Queue queue = (Queue) ict.lookup(QUEUE_JNDI);
            QueueConnectionFactory factory = (QueueConnectionFactory) ict.lookup("ConnectionFactory");
            cnn = factory.createQueueConnection();
            session = cnn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
            
            AccountSummary account = new AccountSummary();
            account.setMemberType(ASP_MEMBER);
            account.setRegisteredDate(purchaseDate);
            account.setFacilityAddress(purchase.getFacilityModel().getAddress());
            account.setFacilityId(purchase.getFacilityModel().getFacilityId());
            account.setFacilityName(purchase.getFacilityModel().getFacilityName());
            account.setFacilityTelephone(purchase.getFacilityModel().getTelephone());
            account.setFacilityZipCode(purchase.getFacilityModel().getZipCode());
            account.setUserEmail(purchase.getEmail());
            account.setUserName(purchase.getCommonName());
            account.setUserId(purchase.idAsLocal());
            
            ObjectMessage msg = session.createObjectMessage(account);
            sender = session.createSender(queue);
            sender.send(msg);
            session.close();
            sender.close();
            
        } catch (NamingException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
            
        } catch (JMSException je) {
            je.printStackTrace();
            throw new RuntimeException(je.getMessage());
        }
    }
}




















