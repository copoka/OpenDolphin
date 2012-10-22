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
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import open.dolphin.infomodel.AppointmentModel;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.FacilityModel;
import open.dolphin.infomodel.LaboModuleValue;
import open.dolphin.infomodel.ObservationModel;
import open.dolphin.infomodel.PatientMemoModel;
import open.dolphin.infomodel.PublishedTreeModel;
import open.dolphin.infomodel.RegisteredDiagnosisModel;
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
        .setParameter("fid", getCallersFacilityId(ctx)+"%")
        .getResultList();
        
        Collection<UserModel> ret = new ArrayList<UserModel>();
        for (Iterator iter = results.iterator(); iter.hasNext(); ) {
            UserModel user = (UserModel) iter.next();
            if (user != null && user.getMemberType() != null && (!user.getMemberType().equals("EXPIRED"))) {
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
        em.merge(update);
        return 1;
    }
    
    /**
     * User���폜����B
     * @param removeId �폜���郆�[�U��Id
     */
    @RolesAllowed("admin")
    public int removeUser(String removeId) {
        
        UserModel remove = getUser(removeId);
        long removePk = remove.getId();
        
        // Stamp ���폜����
        Collection<StampModel> stamps = (Collection<StampModel>) em.createQuery("from StampModel s where s.userId = :pk")
        .setParameter("pk", removePk)
        .getResultList();
        for (StampModel stamp : stamps) {
            em.remove(stamp);
        }
        
        // Subscribed Tree ���폜����
        Collection<SubscribedTreeModel> subscribedTrees = (Collection<SubscribedTreeModel>)
        em.createQuery("from SubscribedTreeModel s where s.user.id = :pk")
        .setParameter("pk", removePk)
        .getResultList();
        for (SubscribedTreeModel tree : subscribedTrees) {
            em.remove(tree);
        }
        
        // PublishedTree ���폜����
        Collection<PublishedTreeModel> publishedTrees = (Collection<PublishedTreeModel>)
        em.createQuery("from PublishedTreeModel p where p.user.id = :pk")
        .setParameter("pk", removePk)
        .getResultList();
        for (PublishedTreeModel tree : publishedTrees) {
            em.remove(tree);
        }
        
        // PersonalTree���폜����
        try {
            StampTreeModel stampTree = (StampTreeModel) em.createQuery("from StampTreeModel s where s.user.id = :pk")
            .setParameter("pk", removePk)
            .getSingleResult();
            em.remove(stampTree);
        } catch (Exception e) {
            
        }
        
        //
        // ���[�U���폜����
        //
        if (remove.getLicenseModel().getLicense().equals("doctor")) {
            StringBuilder sb = new StringBuilder();
            sb.append(new Date());
            String note = sb.toString();
            remove.setMemo(note);
            remove.setPassword("c9dbeb1de83e60eb1eb3675fa7d69a02");
            remove.setMemberType("EXPIRED");
        } else {
            em.remove(remove);
        }
            
        boolean deleteDoc = false;
        if (deleteDoc) {            
            
            //
            // Document, Module, Image (Cascade)
            //
            Collection<DocumentModel> documents = (Collection<DocumentModel>) 
                                    em.createQuery("from DocumentModel d where d.creator.id = :removeId")
                                      .setParameter("removeId", removePk).getResultList();

            System.out.println(documents.size() + " ���̃h�L�������g���폜���܂��B");
            //
            // Document ���폜����� Module��Image�̓J�X�P�[�h�폜�����
            //
            for (DocumentModel document : documents) {
                em.remove(document);
            }


            //
            // Diagnosis
            //
            Collection<RegisteredDiagnosisModel> rds = (Collection<RegisteredDiagnosisModel>) 
                                                        em.createQuery("from RegisteredDiagnosisModel d where d.creator.id = :removeId")
                                                          .setParameter("removeId", removePk)
                                                          .getResultList();
            System.out.println(rds.size() + " ���̏��a�����폜���܂��B");
            for (RegisteredDiagnosisModel rd : rds) {
                em.remove(rd);
            } 


            //
            // Observation
            //
            Collection<ObservationModel> observations = (Collection<ObservationModel>) 
                                                        em.createQuery("from ObservationModel o where o.creator.id = :removeId")
                                                          .setParameter("removeId", removePk)
                                                          .getResultList();
            System.out.println(observations.size() + " ���̊ϑ����폜���܂��B");
            for (ObservationModel observation : observations) {
                em.remove(observation);
            }
            
            //
            // ���҃���
            //
            Collection<PatientMemoModel> memos = (Collection<PatientMemoModel>) 
                                                        em.createQuery("from PatientMemoModel o where o.creator.id = :removeId")
                                                          .setParameter("removeId", removePk)
                                                          .getResultList();
            System.out.println(memos.size() + " ���̊��҃������폜���܂��B");
            for (PatientMemoModel memo : memos) {
                em.remove(memo);
            }       
            
            
            //
            // �\��
            //
            Collection<AppointmentModel> appos = (Collection<AppointmentModel>) 
                                                        em.createQuery("from AppointmentModel o where o.creator.id = :removeId")
                                                          .setParameter("removeId", removePk)
                                                          .getResultList();
            System.out.println(appos.size() + " ���̗\����폜���܂��B");
            for (AppointmentModel appo : appos) {
                em.remove(appo);
            }  
            
            
            //
            // ���{
            //
            Collection<LaboModuleValue> labos = (Collection<LaboModuleValue>) 
                                                        em.createQuery("from LaboModuleValue o where o.creator.id = :removeId")
                                                          .setParameter("removeId", removePk)
                                                          .getResultList();
            System.out.println(labos.size() + " ���̃��{���폜���܂��B");
            for (LaboModuleValue lb : labos) {
                em.remove(lb);
            }              

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
        //checkFacility(ctx, update.getUserId());
        FacilityModel updateFacility = update.getFacilityModel();
        FacilityModel current = (FacilityModel) em.find(FacilityModel.class, updateFacility.getId());
        updateFacility.setMemberType(current.getMemberType());
        updateFacility.setRegisteredDate(current.getRegisteredDate());
        em.merge(updateFacility );
        return 1;
    }
}