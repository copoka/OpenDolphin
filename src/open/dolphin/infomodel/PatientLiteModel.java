package open.dolphin.infomodel;

/**
 * PatientLiteModel
 *
 * @author Minagawa, kazushi
 */
public class PatientLiteModel extends InfoModel {
    
    private static final long serialVersionUID = 2257606235838636648L;
    
    private String patientId;
    private String name;
    private String gender;
    private String genderDesc;
    private String genderCodeSys;
    private String birthday;
    
    /**
     * �ȈՊ��ҏ��I�u�W�F�N�g�𐶐�����B
     */
    public PatientLiteModel() {
    }
    
    /**
     * ����ID��ݒ肷��B
     * @param patientId ����ID
     */
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
    
    /**
     * ����ID��Ԃ��B
     * @return ����ID
     */
    public String getPatientId() {
        return patientId;
    }
    
    /**
     * �t���l�[����ݒ肷��B
     * @param name �t���l�[��
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * �t���l�[����Ԃ��B
     * @return �t���l�[��
     */
    public String getName() {
        return name;
    }
    
    /**
     * ���ʂ�ݒ肷��B
     * @param gender ����
     */
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    /**
     * ���ʂ�Ԃ��B
     * @return ����
     */
    public String getGender() {
        return gender;
    }
    
    /**
     * ���ʐ�����ݒ肷��B
     * @param genderDesc ���ʐ���
     */
    public void setGenderDesc(String genderDesc) {
        this.genderDesc = genderDesc;
    }
    
    /**
     * ���ʐ�����Ԃ��B
     * @return ���ʐ���
     */
    public String getGenderDesc() {
        return genderDesc;
    }
    
    /**
     * ���ʐ����̌n��ݒ肷��B
     * @param genderCodeSys ���ʐ����̌n
     */
    public void setGenderCodeSys(String genderCodeSys) {
        this.genderCodeSys = genderCodeSys;
    }
    
    /**
     * ���ʐ����̌n��Ԃ��B
     * @return ���ʐ����̌n
     */
    public String getGenderCodeSys() {
        return genderCodeSys;
    }
    
    /**
     * ���N������ݒ肷��B
     * @param birthday ���N���� yyyy-MM-dd
     */
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
    
    /**
     * ���N������Ԃ��B
     * @return ���N���� yyyy-MM-dd
     */
    public String getBirthday() {
        return birthday;
    }
}
