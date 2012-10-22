package open.dolphin.infomodel;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * FacilityModel
 *
 * @author Minagawa,Kazushi
 *
 */
@Entity
@Table(name = "d_facility")
public class FacilityModel extends InfoModel {
    
    private static final long serialVersionUID = 3142760011378628588L;
    
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    /** Business Key */
    @Column(nullable=false, unique=true)
    private String facilityId;
    
    @Column(nullable=false)
    private String facilityName;
    
    @Column(nullable=false)
    private String zipCode;
    
    @Column(nullable=false)
    private String address;
    
    @Column(nullable=false)
    private String telephone;
    
    private String url;
    
    @Column(nullable=false)
    @Temporal(value = TemporalType.DATE)
    private Date registeredDate;
    
    @Column(nullable= false)
    private String memberType;
    
    
    /**
     * Id��Ԃ��B
     * @return Id
     */
    public long getId() {
        return id;
    }
    
    /**
     * Id��ݒ肷��B
     * @param id Database pk
     */
    public void setId(long id) {
        this.id = id;
    }
    
    /**
     * FacilityModel�I�u�W�F�N�g��������������B
     */
    public FacilityModel() {
    }
    
    /**
     * �{��ID��ݒ肷��B
     *
     * @param facilityId
     *            �{��ID
     */
    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }
    
    /**
     * �{��ID��Ԃ��B
     *
     * @return �{��ID
     */
    public String getFacilityId() {
        return facilityId;
    }
    
    /**
     * �{�ݖ���ݒ肷��B
     *
     * @param name
     *            �{�ݖ�
     */
    public void setFacilityName(String name) {
        this.facilityName = name;
    }
    
    /**
     * �{�ݖ���Ԃ��B
     *
     * @return �{�ݖ�
     */
    public String getFacilityName() {
        return facilityName;
    }
    
    /**
     * �X�֔ԍ���ݒ肷��B
     *
     * @param zipCode
     *            �X�֔ԍ�
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    
    /**
     * �X�֔ԍ���Ԃ��B
     *
     * @return �X�֔ԍ�
     */
    public String getZipCode() {
        return zipCode;
    }
    
    /**
     * �Z����ݒ肷��B
     *
     * @param address
     *            �Z��
     */
    public void setAddress(String address) {
        this.address = address;
    }
    
    /**
     * �Z����Ԃ��B
     *
     * @return �Z��
     */
    public String getAddress() {
        return address;
    }
    
    /**
     * �d�b�ԍ���ݒ肷��B
     *
     * @param telephone
     *            �d�b�ԍ�
     */
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    
    /**
     * �d�b�ԍ���Ԃ��B
     *
     * @return �d�b�ԍ�
     */
    public String getTelephone() {
        return telephone;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    /**
     * �o�^����ݒ肷��B
     *
     * @param registeredDate
     *            �o�^��
     */
    public void setRegisteredDate(Date registeredDate) {
        this.registeredDate = registeredDate;
    }
    
    /**
     * �o�^����Ԃ��B
     *
     * @return �o�^��
     */
    public Date getRegisteredDate() {
        return registeredDate;
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
        final FacilityModel other = (FacilityModel) obj;
        if (id != other.id)
            return false;
        return true;
    }
}