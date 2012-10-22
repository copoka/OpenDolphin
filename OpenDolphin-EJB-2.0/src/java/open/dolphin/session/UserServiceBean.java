package open.dolphin.session;

import java.util.Collection;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import open.dolphin.infomodel.FacilityModel;
import open.dolphin.infomodel.PublishedTreeModel;
import open.dolphin.infomodel.StampModel;
import open.dolphin.infomodel.StampTreeModel;
import open.dolphin.infomodel.SubscribedTreeModel;
import open.dolphin.infomodel.UserModel;

/**
 *
 * @author kazushi Minagawa, Digital Globe, Inc.
 */
@Stateless
public class UserServiceBean implements UserServiceBeanLocal {

    private static final String QUERY_USER_BY_UID = "from UserModel u where u.userId=:uid";
    private static final String QUERY_USER_BY_FID_MEMBERTYPE = "from UserModel u where u.userId like :fid and u.memberType!=:memberType";

    private static final String UID = "uid";
    private static final String FID = "fid";
    private static final String MEMBER_TYPE = "memberType";
    private static final String MEMBER_TYPE_EXPIRED = "EXPIRED";

    @Resource
    private SessionContext ctx;

    @PersistenceContext
    private EntityManager em;

    @Override
    public boolean authenticate(String userName, String password) {

        boolean ret = false;

        try {
            UserModel user = (UserModel)
                em.createQuery(QUERY_USER_BY_UID)
                  .setParameter(UID, userName)
                  .getSingleResult();
            if (user.getPassword().equals(password)) {
                ret = true;
            }

        } catch (Exception e) {
        }

        return ret;
    }

    /**
     * �{�݊Ǘ��҂��@��User��o�^����B
     * @param add �o�^����User
     */
    @Override
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
    @Override
    public UserModel getUser(String uid) {
        UserModel user = (UserModel)
                em.createQuery(QUERY_USER_BY_UID)
                  .setParameter(UID, uid)
                  .getSingleResult();

        if (user.getMemberType() != null && user.getMemberType().equals(MEMBER_TYPE_EXPIRED)) {
            throw new SecurityException("Expired User");
        }
        return user;
    }

    /**
     * �{�ݓ��̑SUser���擾����B
     *
     * @return �{�ݓ����[�U���X�g
     */
    @Override
    public List<UserModel> getAllUser(String fid) {

        List<UserModel> results =
                (List<UserModel>)em.createQuery(QUERY_USER_BY_FID_MEMBERTYPE)
                                         .setParameter(FID, fid+":%")
                                         .setParameter(MEMBER_TYPE, MEMBER_TYPE_EXPIRED)
                                         .getResultList();
        return results;

//        Collection<UserModel> ret = new ArrayList<UserModel>();
//        for (Iterator iter = results.iterator(); iter.hasNext(); ) {
//            UserModel user = (UserModel) iter.next();
//            if (user.getMemberType() != null && (!user.getMemberType().equals("EXPIRED"))) {
//                ret.add(user);
//            }
//        }
//        return ret;
    }

    /**
     * User���(�p�X���[�h��)���X�V����B
     * @param update �X�V����User detuched
     */
    @Override
    public int updateUser(UserModel update) {
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
    @Override
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
            remove.setMemberType(MEMBER_TYPE_EXPIRED);
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
    @Override
    public int updateFacility(UserModel update) {
        FacilityModel updateFacility = update.getFacilityModel();
        FacilityModel current = (FacilityModel) em.find(FacilityModel.class, updateFacility.getId());
        updateFacility.setMemberType(current.getMemberType());
        updateFacility.setRegisteredDate(current.getRegisteredDate());
        em.merge(updateFacility );
        return 1;
    }
}
