package open.dolphin.infomodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * UserModel
 *
 * @author Minagawa,Kazushi
 *
 */
@Entity
@Table(name="d_users")
public class UserModel extends InfoModel  {
    
    private static final long serialVersionUID = 1646664434908470285L;
    
    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
    
    /** composite businnes key */
    @Column(nullable=false, unique=true)
    private String userId;
    
    @Column(nullable=false)
    private String password;
    
    private String sirName;
    
    private String givenName;
    
    @Column(nullable=false)
    private String commonName;
    
    @Embedded
    private LicenseModel licenseModel;
    
    @Embedded
    private DepartmentModel departmentValue;
    
    @Column(nullable=false)
    private String memberType;
    
    private String memo;
    
    @Column(nullable=false)
    @Temporal(value = TemporalType.DATE)
    private Date registeredDate;
    
    @Column(nullable=false)
    private String email;
    
    @ManyToOne
    @JoinColumn(name="facility_id", nullable=false)
    private FacilityModel facility;
    
    @OneToMany(mappedBy="user", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    private java.util.Collection<RoleModel> roles;
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    /**
     * UserModel�I�u�W�F�N�g�𐶐�����B
     */
    public UserModel(){
    }
    
    /**
     * ���[�UID��ݒ肷��B
     * @param userId ���[�UID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    /**
     * ���[�UID��Ԃ��B
     * @return ���[�UID
     */
    public String getUserId() {
        return userId;
    }
    
    /**
     * �{��ID��������ID��Ԃ��B
     * @return �{��ID��������ID
     */
    public String idAsLocal() {
        int index = userId.indexOf(COMPOSITE_KEY_MAKER);
        return userId.substring(index+1);
    }
    
    /**
     * �p�X���[�h��ݒ肷��B
     * @param password �p�X���[�h
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * �p�X���[�h��Ԃ��B
     * @return �p�X���[�h
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * SirName ��ݒ肷��B
     * @param sirName SirName
     */
    public void setSirName(String sirName) {
        this.sirName = sirName;
    }
    
    /**
     * SirName ��Ԃ��B
     * @return SirName
     */
    public String getSirName() {
        return sirName;
    }
    
    /**
     * GivenName ��ݒ肷��B
     * @param givenName GivenName
     */
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }
    
    /**
     * GivenName ��Ԃ��B
     * @return GivenName
     */
    public String getGivenName() {
        return givenName;
    }
    
    /**
     * �t���l�[����ݒ肷��B
     * @param commonName �t���l�[��
     */
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }
    
    /**
     * �t���l�[����Ԃ��B
     * @return �t���l�[��
     */
    public String getCommonName() {
        return commonName;
    }
    
    /**
     * ��Î��i���f����ݒ肷��B
     * @param licenseValue ��Î��i���f��
     */
    public void setLicenseModel(LicenseModel licenseValue) {
        this.licenseModel = licenseValue;
    }
    
    /**
     * ��Î��i���f����Ԃ��B
     * @return ��Î��i���f��
     */
    public LicenseModel getLicenseModel() {
        return licenseModel;
    }
    
    /**
     * �{�݃��f����ݒ肷��B
     * @param facilityValue �{�݃��f��
     */
    public void setFacilityModel(FacilityModel facility) {
        this.facility = facility;
    }
    
    /**
     * �{�݃��f����Ԃ��B
     * @return �{�݃��f��
     */
    public FacilityModel getFacilityModel() {
        return facility;
    }
    
    /**
     * �f�Éȃ��f����ݒ肷��B
     * @param departmentValue �f�Éȃ��f��
     */
    public void setDepartmentModel(DepartmentModel departmentValue) {
        this.departmentValue = departmentValue;
    }
    
    /**
     * �f�Éȃ��f����Ԃ��B
     * @return �f�Éȃ��f��
     */
    public DepartmentModel getDepartmentModel() {
        return departmentValue;
    }
    
    /**
     * ���[�U���[����ݒ肷��B
     * @param roles ���[�U���[��
     */
    public void setRoles(Collection<RoleModel> roles) {
        this.roles = roles;
    }
    
    /**
     * ���[�U���[����Ԃ��B
     * @return ���[�U���[��
     */
    public Collection<RoleModel> getRoles() {
        return roles;
    }
    
    /**
     * ���[�U���[����ǉ�����B
     * @param value ���[�U���[��
     */
    public void addRole(RoleModel value) {
        
        if (roles == null) {
            roles = new ArrayList<RoleModel>(1);
        }
        roles.add(value);
    }
    
    /**
     * �ȈՃ��[�U����Ԃ��B
     * @return �ȈՃ��[�U���
     */
    public UserLiteModel getLiteModel() {
        
        UserLiteModel model = new UserLiteModel();
        model.setUserId(getUserId());
        model.setCommonName(getCommonName());
        LicenseModel lm = new LicenseModel();
        lm.setLicense(getLicenseModel().getLicense());
        lm.setLicenseDesc(getLicenseModel().getLicenseDesc());
        lm.setLicenseCodeSys(getLicenseModel().getLicenseCodeSys());
        model.setLicenseModel(lm);
        return model;
    }
    
    /**
     * �����o�[�^�C�v��ݒ肷��B
     * @param memberType �����o�[�^�C�v
     */
    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }
    
    /**
     * �����o�[�^�C�v��Ԃ��B
     * @return �����o�[�^�C�v
     */
    public String getMemberType() {
        return memberType;
    }
    
    /**
     * ���̃��[�U�̃�����ݒ肷��B
     * @param memo ����
     */
    public void setMemo(String memo) {
        this.memo = memo;
    }
    
    /**
     * ���̃��[�U�̃�����Ԃ��B
     * @return ����
     */
    public String getMemo() {
        return memo;
    }
    
    /**
     * �o�^����ݒ肷��B
     * @param registeredDate �o�^��
     */
    public void setRegisteredDate(Date registeredDate) {
        this.registeredDate = registeredDate;
    }
    
    /**
     * �o�^����Ԃ��B
     * @return �o�^��
     */
    public Date getRegisteredDate() {
        return registeredDate;
    }
    
    /**
     * �d�q���[����ݒ肷��B
     * @param email �d�q���[��
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * �d�q���[����Ԃ��B
     * @return  �d�q���[��
     */
    public String getEmail() {
        return email;
    }
    
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (int) (id ^ (id >>> 32));
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final UserModel other = (UserModel) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
