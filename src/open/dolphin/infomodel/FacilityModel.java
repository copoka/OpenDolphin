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
     * Idを返す。
     * @return Id
     */
    public long getId() {
        return id;
    }
    
    /**
     * Idを設定する。
     * @param id Database pk
     */
    public void setId(long id) {
        this.id = id;
    }
    
    /**
     * FacilityModelオブジェクトをせいせいする。
     */
    public FacilityModel() {
    }
    
    /**
     * 施設IDを設定する。
     *
     * @param facilityId
     *            施設ID
     */
    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }
    
    /**
     * 施設IDを返す。
     *
     * @return 施設ID
     */
    public String getFacilityId() {
        return facilityId;
    }
    
    /**
     * 施設名を設定する。
     *
     * @param name
     *            施設名
     */
    public void setFacilityName(String name) {
        this.facilityName = name;
    }
    
    /**
     * 施設名を返す。
     *
     * @return 施設名
     */
    public String getFacilityName() {
        return facilityName;
    }
    
    /**
     * 郵便番号を設定する。
     *
     * @param zipCode
     *            郵便番号
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    
    /**
     * 郵便番号を返す。
     *
     * @return 郵便番号
     */
    public String getZipCode() {
        return zipCode;
    }
    
    /**
     * 住所を設定する。
     *
     * @param address
     *            住所
     */
    public void setAddress(String address) {
        this.address = address;
    }
    
    /**
     * 住所を返す。
     *
     * @return 住所
     */
    public String getAddress() {
        return address;
    }
    
    /**
     * 電話番号を設定する。
     *
     * @param telephone
     *            電話番号
     */
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    
    /**
     * 電話番号を返す。
     *
     * @return 電話番号
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
     * 登録日を設定する。
     *
     * @param registeredDate
     *            登録日
     */
    public void setRegisteredDate(Date registeredDate) {
        this.registeredDate = registeredDate;
    }
    
    /**
     * 登録日を返す。
     *
     * @return 登録日
     */
    public Date getRegisteredDate() {
        return registeredDate;
    }
        
    /**
     * メンバータイプを設定する。
     * @param memberType メンバータイプ
     */
    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }
    
    /**
     * メンバータイプを返す。
     * @return メンバータイプ
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