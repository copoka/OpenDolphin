package open.dolphin.ejb;

import java.util.Collection;
import java.util.Iterator;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import open.dolphin.infomodel.AdminComentValue;
import open.dolphin.infomodel.AdminValue;
import open.dolphin.infomodel.FacilityModel;
import open.dolphin.infomodel.RadiologyMethodValue;
import open.dolphin.infomodel.RoleModel;
import open.dolphin.infomodel.UserModel;

import org.jboss.annotation.ejb.RemoteBinding;

@Stateless
@Remote({RemoteSystemService.class})
@RemoteBinding(jndiBinding="openDolphin/RemoteSystemService")
public class RemoteSystemServiceImpl extends DolphinService implements RemoteSystemService {
    
    private static final String DEFAULT_FACILITY_OID = "1.3.6.1.4.1.9414.10.1";
    
    @PersistenceContext
    private EntityManager em;
    
    /**
     * �ʐM���m�F����B
     * @return Hello, OpenDolphin������
     */
    public String helloDolphin() {
        return "Hello, OpenDolphin";
    }
    
    /**
     * �{�݂ƊǗ��ҏ���o�^����B
     *
     * @param user �{�݊Ǘ���
     */
    public void addFacilityAdmin(UserModel user) {
        
        // OID���Z�b�g���{�݃��R�[�h�𐶐�����
        FacilityModel facility = user.getFacilityModel();
        String facilityId = facility.getFacilityId();
        if (facilityId == null || facilityId.equals("")) {
            facilityId = DEFAULT_FACILITY_OID;
            facility.setFacilityId(facilityId);
        }
        
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
        StringBuilder sb = new StringBuilder();
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
    
    /**
     * �p�@�}�X�^��o�^����B
     */
    public void putAdminMaster(Collection c) {
        
        if (c == null) {
            return;
        }
        
        Iterator iter = c.iterator();
        while(iter.hasNext()) {
            AdminValue value = (AdminValue)iter.next();
            em.persist(value);
        }
    }
    
    /**
     * �p�@�R�����g�}�X�^��o�^����B
     */
    public void putAdminComentMaster(Collection c) {
        
        if (c == null) {
            return;
        }
        
        Iterator iter = c.iterator();
        while(iter.hasNext()) {
            AdminComentValue value = (AdminComentValue)iter.next();
            em.persist(value);
        }
    }
    
    /**
     * ���ː����\�b�h�}�X�^��o�^����B
     */
    public void putRadMethodMaster(Collection c) {
        
        if (c == null) {
            return;
        }
        
        Iterator iter = c.iterator();
        while(iter.hasNext()) {
            RadiologyMethodValue value = (RadiologyMethodValue)iter.next();
            em.persist(value);
        }
    }
}
