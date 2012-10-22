package open.dolphin.infomodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.*;

/**
 * PatientModel
 *
 * @author Minagawa,kazushi
 * -
 */
@Entity
@Table(name = "d_patient")
public class PatientModel extends InfoModel implements java.io.Serializable {
    
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    /** �{��ID */
    @Column(nullable=false)
    private String facilityId;
    
    /** �{�ݓ��̊���ID */
    @Column(nullable=false)
    private String patientId;
    
    private String familyName;
    
    private String givenName;
    
    @Column(nullable=false)
    private String fullName;
    
    private String kanaFamilyName;
    
    private String kanaGivenName;
    
    private String kanaName;
    
    private String romanFamilyName;
    
    private String romanGivenName;
    
    private String romanName;
    
    @Column(nullable=false)
    private String gender;
    
    private String genderDesc;
    
    @Transient
    private String genderCodeSys;
    
    private String birthday;
    
    private String nationality;
    
    @Transient
    private String nationalityDesc;
    
    @Transient
    private String nationalityCodeSys;
    
    private String maritalStatus;
    
    @Transient
    private String maritalStatusDesc;
    
    @Transient
    private String maritalStatusCodeSys;
    
    @Lob
    private byte[] jpegPhoto;
    
    private String memo;
    
    @Embedded
    private SimpleAddressModel address;
    
    private String telephone;
    
    private String mobilePhone;
    
    private String email;
    
    @OneToMany(mappedBy="patient", cascade={CascadeType.PERSIST, CascadeType.REMOVE})
    private List<HealthInsuranceModel> healthInsurances;
    
    @Transient
    private List<PVTHealthInsuranceModel> pvtHealthInsurances;
    
    @Transient
    private Collection<AddressModel> addresses;
    
    @Transient
    private Collection<TelephoneModel> telephones;
    
    /**
     * ���҃I�u�W�F�N�g�𐶐�����B
     */
    public PatientModel() {
    }
    
    /**
     * Database Pk ��Ԃ��B
     *
     * @return Database Pk
     */
    public long getId() {
        return id;
    }
    
    /**
     * Database Pk ��ݒ肷��B
     *
     * @param id
     *            Database Pk
     */
    public void setId(long id) {
        this.id = id;
    }
    
    /**
     * �{��ID��Ԃ��B
     * @return �{��ID
     */
    public String getFacilityId() {
        return facilityId;
    }
    
    /**
     * �{��ID��ݒ肷��B
     * @param facilityId �{��ID
     */
    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }
    
    /**
     * ����ID��ݒ肷��B
     *
     * @param patientId
     *            ����ID
     */
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
    
    /**
     * ����ID��Ԃ��B
     *
     * @return ����ID
     */
    public String getPatientId() {
        return patientId;
    }
    
    /**
     * ������ݒ肷��B
     *
     * @param sirName
     *            ����
     */
    public void setFamilyName(String sirName) {
        this.familyName = sirName;
    }
    
    /**
     * ������Ԃ��B
     *
     * @return ����
     */
    public String getFamilyName() {
        return familyName;
    }
    
    /**
     * ���O��ݒ肷��B
     *
     * @param givenName
     *            ���O
     */
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }
    
    /**
     * ���O��Ԃ��B
     *
     * @return ���O
     */
    public String getGivenName() {
        return givenName;
    }
    
    /**
     * �t���l�[����ݒ肷��B
     *
     * @param name
     *            �t���l�[��
     */
    public void setFullName(String name) {
        this.fullName = name;
    }
    
    /**
     * �t���l�[����Ԃ��B
     *
     * @return �t���l�[��
     */
    public String getFullName() {
        return fullName;
    }
    
    /**
     * �J�i�Ƒ�����ݒ肷��B
     *
     * @param kanaSirName
     *            T�J�i�Ƒ���
     */
    public void setKanaFamilyName(String kanaSirName) {
        this.kanaFamilyName = kanaSirName;
    }
    
    /**
     * �J�i�Ƒ�����Ԃ��B
     *
     * @return �J�i�Ƒ���
     */
    public String getKanaFamilyName() {
        return kanaFamilyName;
    }
    
    /**
     * �J�iGivenName ��ݒ肷��B
     *
     * @param kanaGivenName
     *            �J�iGivenName
     */
    public void setKanaGivenName(String kanaGivenName) {
        this.kanaGivenName = kanaGivenName;
    }
    
    /**
     * �J�iGivenName ��Ԃ��B
     *
     * @return �J�iGivenName
     */
    public String getKanaGivenName() {
        return kanaGivenName;
    }
    
    /**
     * �J�i�t���l�[����ݒ肷��B
     *
     * @param kanaName
     *            �J�i�t���l�[��
     */
    public void setKanaName(String kanaName) {
        this.kanaName = kanaName;
    }
    
    /**
     * �J�i�t���l�[����Ԃ��B
     *
     * @return �J�i�t���l�[��
     */
    public String getKanaName() {
        return kanaName;
    }
    
    /**
     * ���[�}��������ݒ肷��B
     *
     * @param romanSirName
     *            ���[�}������
     */
    public void setRomanFamilyName(String romanSirName) {
        this.romanFamilyName = romanSirName;
    }
    
    /**
     * ���[�}��������Ԃ��B
     *
     * @return ���[�}����
     */
    public String getRomanFamilyName() {
        return romanFamilyName;
    }
    
    /**
     * ���[�}������ݒ肷��B
     *
     * @param romanGivenName
     *            ���[�}����
     */
    public void setRomanGivenName(String romanGivenName) {
        this.romanGivenName = romanGivenName;
    }
    
    /**
     * ���[�}������Ԃ��B
     *
     * @return ���[�}����
     */
    public String getRomanGivenName() {
        return romanGivenName;
    }
    
    /**
     * ���[�}���t���l�[����ݒ肷��B
     *
     * @param romanName
     *            ���[�}���t���l�[��
     */
    public void setRomanName(String romanName) {
        this.romanName = romanName;
    }
    
    /**
     * ���[�}���t���l�[����Ԃ��B
     *
     * @return ���[�}���t���l�[��
     */
    public String getRomanName() {
        return romanName;
    }
    
    /**
     * ���ʂ�ݒ肷��B
     *
     * @param gender
     *            ����
     */
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    /**
     * ���ʂ�Ԃ��B
     *
     * @return ����
     */
    public String getGender() {
        return gender;
    }
    
    /**
     * ���ʐ�����ݒ肷��B
     *
     * @param genderDesc
     *            ���ʐ���
     */
    public void setGenderDesc(String genderDesc) {
        this.genderDesc = genderDesc;
    }
    
    /**
     * ���ʐ�����Ԃ��B
     *
     * @return ���ʐ���
     */
    public String getGenderDesc() {
        return genderDesc != null
                ? genderDesc
                : ModelUtils.getGenderDesc(gender);
    }
    
    /**
     * ���ʐ����̌n��ݒ肷��B
     *
     * @param genderCodeSys
     *            ���ʐ����̌n
     */
    public void setGenderCodeSys(String genderCodeSys) {
        this.genderCodeSys = genderCodeSys;
    }
    
    /**
     * ���ʐ����̌n��Ԃ��B
     *
     * @return ���ʐ����̌n
     */
    public String getGenderCodeSys() {
        return genderCodeSys;
    }
    
    /**
     * ���N������ݒ肷��B
     *
     * @param birthday
     *            ���N���� yyyy-MM-dd
     */
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
    
    /**
     * ���N������Ԃ��B
     *
     * @return ���N���� yyyy-MM-dd
     */
    public String getBirthday() {
        return birthday;
    }
    
    /**
     * �N��Ɛ��N������Ԃ��B
     *
     * @return age(yyyy-MM-dd)
     */
    public String getAgeBirthday() {
        return ModelUtils.getAgeBirthday(birthday);
    }
    
    /**
     * ���Ђ�ݒ肷��B
     *
     * @param nationality
     *            ����
     */
    public void setNationality(String nationality) {
        this.nationality = nationality;
    }
    
    /**
     * ���Ђ�Ԃ��B
     *
     * @return ����
     */
    public String getNationality() {
        return nationality;
    }
    
    /**
     * ���А�����ݒ肷��B
     *
     * @param nationalityDesc
     *            ���А���
     */
    public void setNationalityDesc(String nationalityDesc) {
        this.nationalityDesc = nationalityDesc;
    }
    
    /**
     * ���А�����Ԃ��B
     *
     * @return ���А���
     */
    public String getNationalityDesc() {
        return nationalityDesc;
    }
    
    /**
     * ���Б̌n��ݒ肷��B
     *
     * @param nationalityCodeSys
     *            ���Б̌n
     */
    public void setNationalityCodeSys(String nationalityCodeSys) {
        this.nationalityCodeSys = nationalityCodeSys;
    }
    
    /**
     * ���Б̌n��Ԃ��B
     *
     * @return ���Б̌n
     */
    public String getNationalityCodeSys() {
        return nationalityCodeSys;
    }
    
    /**
     * �����󋵂�ݒ肷��B
     *
     * @param maritalStatus
     *            ������
     */
    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }
    
    /**
     * �����󋵂�Ԃ��B
     *
     * @return ������
     */
    public String getMaritalStatus() {
        return maritalStatus;
    }
    
    /**
     * �����󋵐�����ݒ肷��B
     *
     * @param maritalStatusDesc
     *            �����󋵐�����ݒ肷��B
     */
    public void setMaritalStatusDesc(String maritalStatusDesc) {
        this.maritalStatusDesc = maritalStatusDesc;
    }
    
    /**
     * �����󋵐�����Ԃ��B
     *
     * @return �����󋵐���
     */
    public String getMaritalStatusDesc() {
        return maritalStatusDesc;
    }
    
    /**
     * �����󋵑̌n��ݒ肷��B
     *
     * @param maritalStatusCodeSys
     *            �����󋵑̌n
     */
    public void setMaritalStatusCodeSys(String maritalStatusCodeSys) {
        this.maritalStatusCodeSys = maritalStatusCodeSys;
    }
    
    /**
     * �����󋵑̌n��Ԃ��B
     *
     * @return �����󋵑̌n
     */
    public String getMaritalStatusCodeSys() {
        return maritalStatusCodeSys;
    }
    
    /**
     * �ʐ^��ݒ肷��B
     *
     * @param jpegPhoto
     *            JPEG �摜�̃o�C�g�z��
     */
    public void setJpegPhoto(byte[] jpegPhoto) {
        this.jpegPhoto = jpegPhoto;
    }
    
    /**
     * �ʐ^��Ԃ��B
     *
     * @return JPEG �摜�̃o�C�g�z��
     */
    public byte[] getJpegPhoto() {
        return jpegPhoto;
    }
    
    /**
     * ���҃�����ݒ肷��B
     *
     * @param memo
     *            ���҃���
     */
    public void setMemo(String memo) {
        this.memo = memo;
    }
    
    /**
     * ���҃�����Ԃ��B
     *
     * @return ���҃���
     */
    public String getMemo() {
        return memo;
    }
    
    /**
     * �Z�����f����Ԃ��B
     * @return �Z�����G��
     */
    public SimpleAddressModel getSimpleAddressModel() {
        return address;
    }
    
    /**
     * �Z�����f����ݒ肷��B
     * @param address �Z�����f��
     */
    public void setSimpleAddressModel(SimpleAddressModel address) {
        this.address = address;
    }
    
    /**
     * �X�֔ԍ���Ԃ��B
     * @return �X�֔ԍ�
     */
    public String contactZipCode() {
        return (address!=null) ? address.getZipCode() : null;
    }
    
    /**
     * �Z����Ԃ��B
     * @return �Z��
     */
    public String contactAddress() {
        return (address!=null) ? address.getAddress() : null;
    }

    public String getHomeAddress() {
        return (address!=null) ? address.getAddress() : null;
    }
    
    /**
     * �d�b�ԍ���Ԃ��B
     * @return �d�b�ԍ�
     */
    public String getTelephone() {
        return telephone;
    }
    
    /**
     * �d�b�ԍ���ݒ肷��B
     * @param telephone �d�b�ԍ�
     */
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    
    /**
     *�g�ѓd�b�̔ԍ���Ԃ��B
     * @return �g�ѓd�b�̔ԍ�
     */
    public String getMobilePhone() {
        return mobilePhone;
    }
    
    /**
     *�g�ѓd�b�̔ԍ���ݒ肷��B
     *@param mobilePhone �g�ѓd�b�̔ԍ�
     */
    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }
    
    /**
     * �d�q���[���A�h���X��Ԃ��B
     * @return �d�q���[���A�h���X
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * �d�q���[���A�h���X��ݒ肷��B
     * @param email �d�q���[���A�h���X
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * ���N�ی���ݒ肷��B
     *
     * @param healthInsurances
     *            ���N�ی�
     */
    public void setHealthInsurances(
            List<HealthInsuranceModel> healthInsurances) {
        this.healthInsurances = healthInsurances;
    }
    
    /**
     * ���N�ی���Ԃ��B
     *
     * @return ���N�ی�
     */
    public List<HealthInsuranceModel> getHealthInsurances() {
        return healthInsurances;
    }
    
    /**
     * ���N�ی���ǉ�����B
     *
     * @param value
     *            ���N�ی�
     */
    public void addHealthInsurance(HealthInsuranceModel value) {
        if (healthInsurances == null) {
            healthInsurances = new ArrayList<HealthInsuranceModel>(2);
        }
        healthInsurances.add(value);
    }
    
    public List<PVTHealthInsuranceModel> getPvtHealthInsurances() {
        return pvtHealthInsurances;
    }
    
    public void setPvtHealthInsurances(
            List<PVTHealthInsuranceModel> pvtHealthInsurances) {
        this.pvtHealthInsurances = pvtHealthInsurances;
    }
    
    public void addPvtHealthInsurance(PVTHealthInsuranceModel model) {
        if (pvtHealthInsurances == null) {
            pvtHealthInsurances =  new ArrayList<PVTHealthInsuranceModel>(2);
        }
        pvtHealthInsurances.add(model);
    }
    
    public Collection<AddressModel> getAddresses() {
        return addresses;
    }
    
    public void setAddresses(Collection<AddressModel> addresses) {
        this.addresses = addresses;
    }
    
    public void addAddress(AddressModel address) {
        if (addresses == null) {
            addresses = new ArrayList<AddressModel>(1);
        }
        addresses.add(address);
    }
    
    public Collection<TelephoneModel> getTelephones() {
        return telephones;
    }
    
    public void setTelephones(Collection<TelephoneModel> telephones) {
        this.telephones = telephones;
    }
    
    public void addTelephone(TelephoneModel telephone) {
        if (telephones == null) {
            telephones = new ArrayList<TelephoneModel>(1);
        }
        telephones.add(telephone);
    }
    
    /**
     * ���ҊȈՏ���Ԃ��B
     *
     * @return ���ҊȈՏ��
     */
    public PatientLiteModel patientAsLiteModel() {
        PatientLiteModel model = new PatientLiteModel();
        model.setPatientId(getPatientId());
        model.setFullName(getFullName());
        model.setKanaName(getKanaName());
        model.setGender(getGender());
        model.setGenderDesc(getGenderDesc());
        model.setBirthday(getBirthday());
        return model;
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
        final PatientModel other = (PatientModel) obj;
        if (id != other.id)
            return false;
        return true;
    }

    //-------------------------------------------------------------
    public SimpleAddressModel getAddress() {
        return getSimpleAddressModel();
    }

    public void setAddress(SimpleAddressModel address) {
        setSimpleAddressModel(address);
    }
    //-------------------------------------------------------------
}
