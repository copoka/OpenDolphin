package open.dolphin.ejb;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import javax.persistence.PersistenceException;
import open.dolphin.infomodel.DgOid;
import open.dolphin.infomodel.FacilityModel;
import open.dolphin.infomodel.RoleModel;
import open.dolphin.infomodel.UserModel;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;

@Stateless
@SecurityDomain("openDolphinSysAd")
@RolesAllowed("sysAd")
@Remote({RemoteSystemService.class})
@RemoteBinding(jndiBinding="openDolphin/RemoteSystemService")
public class RemoteSystemServiceImpl extends DolphinService implements RemoteSystemService {
    
    @PersistenceContext
    private EntityManager em;
    
    /**
     * �ʐM���m�F����B
     * @return Hello, OpenDolphin������
     */
    @Override
    public String helloDolphin() {
        return "Hello, OpenDolphin";
    }
    
    /**
     * �{�݂ƊǗ��ҏ���o�^����B
     *
     * @param user �{�݊Ǘ���
     */
    @Override
    public void addFacilityAdmin(UserModel user) {

        // mail address
        String email = user.getEmail();
        if (email == null) {
            throw new PersistenceException("�d�q���[���A�h���X����̂��ߓo�^�ł��܂���B");
        }
        
        // �{��ID�Ɏg�p���� OID ���擾����
        DgOid oid = (DgOid)em.find(DgOid.class, new Long(1L));
        String baseOid = oid.getBaseOid();
        int nextNumber = oid.getNextNumber();
        oid.setNextNumber(nextNumber+1);
        StringBuilder sb = new StringBuilder();
        sb.append(baseOid);
        sb.append(".");
        sb.append(String.valueOf(nextNumber));
        String facilityId = sb.toString();
        
        // OID���Z�b�g���{�݃��R�[�h�𐶐�����
        FacilityModel facility = user.getFacilityModel();
        facility.setFacilityId(facilityId);
        try {
            em.createQuery("from FacilityModel f where f.facilityId = :fid")
            .setParameter("fid", facilityId)
            .getSingleResult();
            
            // ���łɑ��݂��Ă���ꍇ�͗�O���X���[����
            throw new EntityExistsException();
            
        } catch (NoResultException e) {
            // ������O
        }
        
        // �i��������
        // ���̃��\�b�h�� facility ���Ǘ����ꂽ��ԂɂȂ�
        em.persist(facility);
        
        // ���̃��[�U�̕����L�[�𐶐�����
        // i.e. userId = facilityId:userId(local)
        sb = new StringBuilder();
        sb.append(facilityId);
        sb.append(COMPOSITE_KEY_MAKER);
        sb.append(user.getUserId());
        user.setUserId(sb.toString());
        
        // ��L Facility �� Admin User ��o�^����
        // admin �� user Role ��ݒ肷��
        boolean hasAdminRole = false;
        boolean hasUserRole = false;
        Collection<RoleModel> roles = user.getRoles();
        if (roles != null) {
            for (RoleModel val : roles) {
                if (val.getRole().equals(ADMIN_ROLE)) {
                    hasAdminRole = true;
                    continue;
                }
                if (val.getRole().equals(USER_ROLE)) {
                    hasUserRole = true;
                    continue;
                }
            }
        }
        
        if (!hasAdminRole) {
            RoleModel role = new RoleModel();
            role.setRole(ADMIN_ROLE);
            role.setUser(user);
            role.setUserId(user.getUserId());
            user.addRole(role);
        }
        
        if (!hasUserRole) {
            RoleModel role = new RoleModel();
            role.setRole(USER_ROLE);
            role.setUser(user);
            role.setUserId(user.getUserId());
            user.addRole(role);
        }
        
        // �i��������
        // Role �ɂ� User ���� CascadeType.ALL ���ݒ肳��Ă���
        em.persist(user);
    }
}
