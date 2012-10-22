package open.dolphin.infomodel;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

/**
 * DocInfoModel
 *
 * @author Minagawa,Kazushi. Digital Globe, Inc.
 *
 */
@Embeddable
public class DocInfoModel extends InfoModel
        implements Comparable,java.io.Serializable {
    
    @Transient
    private long docPk;
    
    @Transient
    private long parentPk;
    
    @Column(nullable=false, length=32)
    private String docId;
    
    @Column(nullable=false)
    private String docType;
    
    @Column(nullable=false)
    private String title;
    
    @Column(nullable=false)
    private String purpose;
    
    @Transient
    private String purposeDesc;
    
    @Transient
    private String purposeCodeSys;
    
    @Transient
    private Date firstConfirmDate;
    
    @Transient
    private Date confirmDate;
    
    private String department;
    
    private String departmentDesc;
    
    @Transient
    private String departmentCodeSys;
    
    private String healthInsurance;
    
    private String healthInsuranceDesc;
    
    @Transient
    private String healthInsuranceCodeSys;
    
    private String healthInsuranceGUID;
    
    private boolean hasMark;
    
    private boolean hasImage;
    
    private boolean hasRp;
    
    private boolean hasTreatment;
    
    private boolean hasLaboTest;
    
    private String versionNumber;

    //private long pvtId;
    
    @Transient
    private String versionNotes;
    
    private String parentId;
    
    private String parentIdRelation;
    
    @Transient
    private String parentIdDesc;
    
    @Transient
    private String parentIdCodeSys;
    
    @Transient
    private Collection<AccessRightModel> accessRights;
    
    @Transient
    private String status;

    @Transient
    private String handleClass;

    //----------------------------------
    // Params foe senders
    //----------------------------------
    private String labtestOrderNumber;

    @Transient
    private boolean sendClaim;

    @Transient
    private boolean sendLabtest;

    @Transient
    private boolean sendMml;
    //----------------------------------
    
    /**
     * Document �� Database Primary Key ��Ԃ��B
     * @return Primary Key
     */
    public long getDocPk() {
        return docPk;
    }
    
    
    /**
     * Document �� Database Primary Key ��ݒ肷��B
     * @param docPk Database Primary Key
     */
    public void setDocPk(long docPk) {
        this.docPk = docPk;
    }
    
    /**
     * �e��������PrimaryKey��Ԃ��B
     * @return �e��������PrimaryKey
     */
    public long getParentPk() {
        return parentPk;
    }
    
    /**
     * �e��������PrimaryKey��ݒ肷��B
     * @param parentPk �e��������PrimaryKey
     */
    public void setParentPk(long parentPk) {
        this.parentPk = parentPk;
    }
    
    /**
     * ����ID��ݒ肷��B
     *
     * @param docId
     *            ����ID
     */
    public void setDocId(String docId) {
        this.docId = docId;
    }
    
    /**
     * ����ID��Ԃ��B
     *
     * @return ����ID
     */
    public String getDocId() {
        return docId;
    }
    
    /**
     * �����^�C�v��ݒ肷��B
     *
     * @param docType
     *            �����^�C�v
     */
    public void setDocType(String docType) {
        this.docType = docType;
    }
    
    /**
     * �����^�C�v��Ԃ��B
     *
     * @return �����^�C�v.
     */
    public String getDocType() {
        return docType;
    }
    
    /**
     * �^�C�g����ݒ肷��B
     *
     * @param title
     *            �^�C�g��
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * �^�C�g����Ԃ��B
     *
     * @return �^�C�g��
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * �����ړI��ݒ肷��B
     *
     * @param purpose
     *            �����ړI
     */
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
    
    /**
     * �����ړI��Ԃ��B
     *
     * @return �����ړI
     */
    public String getPurpose() {
        return purpose;
    }
    
    /**
     * �����ړI������ݒ肷��B
     *
     * @param purposeDesc
     *            �����ړI����
     */
    public void setPurposeDesc(String purposeDesc) {
        this.purposeDesc = purposeDesc;
    }
    
    /**
     * �����ړI������Ԃ��B
     *
     * @return �����ړI����
     */
    public String getPurposeDesc() {
        return purposeDesc;
    }
    
    /**
     * �����ړI�R�[�h�̌n��ݒ肷��B
     *
     * @param purposeCodeSys
     *            �����ړI�R�[�h�̌n
     */
    public void setPurposeCodeSys(String purposeCodeSys) {
        this.purposeCodeSys = purposeCodeSys;
    }
    
    /**
     * �����ړI�R�[�h�̌n��Ԃ��B
     *
     * @return �����ړI�R�[�h�̌n
     */
    public String getPurposeCodeSys() {
        return purposeCodeSys;
    }
    
    /**
     * �ŏ��̊m�����ݒ肷��B
     *
     * @param firstConfirmDate
     *            �ŏ��̊m���
     */
    public void setFirstConfirmDate(Date firstConfirmDate) {
        this.firstConfirmDate = firstConfirmDate;
    }
    
    /**
     * �ŏ��̊m�����Ԃ��B
     *
     * @return �ŏ��̊m���
     */
    public Date getFirstConfirmDate() {
        return firstConfirmDate;
    }
    
    /**
     * �ŏ��̊m����̓��t������Ԃ��B
     *
     * @return �ŏ��̊m����̓��t����
     */
    public String getFirstConfirmDateTrimTime() {
        return ModelUtils.getDateAsString(getFirstConfirmDate());
    }
    
    /**
     * �m�����ݒ肷��B
     *
     * @param confirmDate
     *            �m���
     */
    public void setConfirmDate(Date confirmDate) {
        this.confirmDate = confirmDate;
    }
    
    /**
     * �m�����Ԃ��B
     *
     * @return �m���
     */
    public Date getConfirmDate() {
        return confirmDate;
    }
    
    /**
     * �m����̓��t������Ԃ��B
     *
     * @return �m����̓��t����
     */
    public String getConfirmDateTrimTime() {
        return ModelUtils.getDateAsString(getConfirmDate());
    }
    
    /**
     * �f�ÉȂ�ݒ肷��B
     *
     * @param department �f�É�
     */
    public void setDepartment(String department) {
        this.department = department;
    }
    
    /**
     * �f�ÉȂ�Ԃ��B
     *
     * @return �f�É�
     */
    public String getDepartment() {
        return department;
    }
    
    /**
     * �f�ÉȐ�����ݒ肷��B
     *
     * @param departmentDesc �f�ÉȐ���
     */
    public void setDepartmentDesc(String departmentDesc) {
        this.departmentDesc = departmentDesc;
    }
    
    /**
     * �f�ÉȐ�����Ԃ��B
     *
     * @return �f�ÉȐ���
     */
    public String getDepartmentDesc() {
        return departmentDesc;
    }
    
    /********************************************/
    public String getDepartmentName() {
        String[] tokens = tokenizeDept(departmentDesc);
        return tokens[0];
    }
    
    public String getDepartmentCode() {
        String[] tokens = tokenizeDept(departmentDesc);
        if (tokens[1] != null) {
            return tokens[1];
        }
        return department;
    }
    
    public String getAssignedDoctorName() {
        String[] tokens = tokenizeDept(departmentDesc);
        return tokens[2];
    }
    
    public String getAssignedDoctorId() {
        String[] tokens = tokenizeDept(departmentDesc);
        return tokens[3];
    }
    
    public String getJMARICode() {
        String[] tokens = tokenizeDept(departmentDesc);
        return tokens[4];
    }
    
    private String[] tokenizeDept(String dept) {
        
        // �f�ÉȖ��A�R�[�h�A�S���㖼�A�S����R�[�h�AJMARI �R�[�h
        // ���i�[����z��𐶐�����
        String[] ret = new String[5];
        Arrays.fill(ret, null);
        
        if (dept != null) {
            try {
                String[] params = dept.split("\\s*,\\s*");
                System.arraycopy(params, 0, ret, 0, params.length);

            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
        
        return ret;
    }
    /********************************************/
    
    /**
     * �f�Éȑ̌n��ݒ肷��B
     *
     * @param departmentCodeSys
     *            �f�Éȑ̌n
     */
    public void setDepartmentCodeSys(String departmentCodeSys) {
        this.departmentCodeSys = departmentCodeSys;
    }
    
    /**
     * �f�Éȑ̌n��Ԃ��B
     *
     * @return �f�Éȑ̌n
     */
    public String getDepartmentCodeSys() {
        return departmentCodeSys;
    }
    
    /**
     * ���N�ی���ݒ肷��B
     *
     * @param healthInsuranceCode
     *            ���N�ی�
     */
    public void setHealthInsurance(String healthInsurance) {
        this.healthInsurance = healthInsurance;
    }
    
    /**
     * ���N�ی���Ԃ��B
     *
     * @return ���N�ی�
     */
    public String getHealthInsurance() {
        return healthInsurance;
    }
    
    /**
     * ���N�ی�������ݒ肷��B
     *
     * @param healthInsuranceDesc
     *            ���N�ی�����
     */
    public void setHealthInsuranceDesc(String healthInsuranceDesc) {
        this.healthInsuranceDesc = healthInsuranceDesc;
    }
    
    /**
     * ���N�ی�������Ԃ��B
     *
     * @return ���N�ی�����
     */
    public String getHealthInsuranceDesc() {
        return healthInsuranceDesc;
    }
    
    /**
     * ���N�ی��̌n��ݒ肷��B
     *
     * @param healthInsuranceCodeSys
     *            ���N�ی��̌n
     */
    public void setHealthInsuranceCodeSys(String healthInsuranceCodeSys) {
        this.healthInsuranceCodeSys = healthInsuranceCodeSys;
    }
    
    /**
     * ���N�ی��̌n��Ԃ��B
     *
     * @return ���N�ی��̌n
     */
    public String getHealthInsuranceCodeSys() {
        return healthInsuranceCodeSys;
    }
    
    /**
     * ���N�ی�GUID��ݒ肷��B
     *
     * @param healthInsuranceGUID
     *            ���N�ی�UUID
     */
    public void setHealthInsuranceGUID(String healthInsuranceGUID) {
        this.healthInsuranceGUID = healthInsuranceGUID;
    }
    
    /**
     * ���N�ی�GUID��Ԃ��B
     *
     * @return ���N�ی�UUID
     */
    public String getHealthInsuranceGUID() {
        return healthInsuranceGUID;
    }
    
    /**
     * ���L�����邩�ǂ�����ݒ肷��B
     *
     * @param hasMark
     *            ���L�����鎞 true
     */
    public void setHasMark(boolean hasMark) {
        this.hasMark = hasMark;
    }
    
    /**
     * ���L�����邩�ǂ�����Ԃ��B
     *
     * @return ���L�����鎞 true
     */
    public boolean isHasMark() {
        return hasMark;
    }
    
    /**
     * �摜�����邩�ǂ�����ݒ肷��B
     *
     * @param hasImage
     *            �摜�����鎞 true
     */
    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }
    
    /**
     * �摜�����邩�ǂ�����Ԃ��B
     *
     * @return �摜�����鎞 true
     */
    public boolean isHasImage() {
        return hasImage;
    }
    
    public Boolean isHasImageBoolean() {
        return hasImage;
    }
    
    /**
     * ���������邩�ǂ�����ݒ肷��B
     *
     * @param hasRp
     *            ���������鎞 true
     */
    public void setHasRp(boolean hasRp) {
        this.hasRp = hasRp;
    }
    
    /**
     * ���������邩�ǂ�����Ԃ��B
     *
     * @return ���������鎞 true
     */
    public boolean isHasRp() {
        return hasRp;
    }
    
    public Boolean isHasRpBoolean() {
        return hasRp;
    }
    
    /**
     * ���u�����邩�ǂ�����ݒ肷��B
     *
     * @param hasTreatment
     *            ���u�����鎞 true
     */
    public void setHasTreatment(boolean hasTreatment) {
        this.hasTreatment = hasTreatment;
    }
    
    /**
     * ���u�����邩�ǂ�����Ԃ��B
     *
     * @return ���u�����鎞 true
     */
    public boolean isHasTreatment() {
        return hasTreatment;
    }
    
    public Boolean isHasTreatmentBoolean() {
        return hasTreatment;
    }
    
    /**
     * ���{�e�X�g�����邩�ǂ�����ݒ肷��B
     *
     * @param hasLaboTest
     *            ���{�e�X�g�����鎞 true
     */
    public void setHasLaboTest(boolean hasLaboTest) {
        this.hasLaboTest = hasLaboTest;
    }
    
    public boolean isHasLaboTest() {
        return hasLaboTest;
    }
    
    /**
     * ���{�e�X�g�����邩�ǂ�����Ԃ��B
     *
     * @return ���{�e�X�g�����鎞 true
     */
    public Boolean isHasLaboTestBoolean() {
        return hasLaboTest;
    }
    
    /**
     * �o�[�W�����ԍ���ݒ肷��B
     *
     * @param version
     *            �o�[�W�����ԍ�
     */
    public void setVersionNumber(String version) {
        this.versionNumber = version;
    }
    
    /**
     * �o�[�W�����ԍ���Ԃ��B
     *
     * @return �o�[�W�����ԍ�
     */
    public String getVersionNumber() {
        return versionNumber;
    }
    
    /**
     * �o�[�W�����m�[�g��ݒ肷��B
     *
     * @param versionNotes
     *            �o�[�W�����m�[�g
     */
    public void setVersionNotes(String versionNotes) {
        this.versionNotes = versionNotes;
    }
    
    /**
     * �o�[�W�����m�[�g��Ԃ��B
     *
     * @return �o�[�W�����m�[�g
     */
    public String getVersionNotes() {
        return versionNotes;
    }

//    public Long getPvtId() {
//        return pvtId;
//    }
//
//    public void setPvtId(Long pvtId) {
//        if (pvtId!=null) {
//            this.pvtId = pvtId.longValue();
//        }
//    }
    
    /**
     * �e����ID��ݒ肷��B
     *
     * @param parentId
     *            �e����ID
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    
    /**
     * �e����ID��Ԃ��B
     *
     * @return �e����ID
     */
    public String getParentId() {
        return parentId;
    }
    
    /**
     * �e�����Ƃ̊֌W��ݒ肷��B
     *
     * @param parentIdRelation
     *            �e�����Ƃ̊֌W
     */
    public void setParentIdRelation(String parentIdRelation) {
        this.parentIdRelation = parentIdRelation;
    }
    
    /**
     * �e�����Ƃ̊֌W��Ԃ��B
     *
     * @return �e�����Ƃ̊֌W
     */
    public String getParentIdRelation() {
        return parentIdRelation;
    }
    
    /**
     * �e�����Ƃ̊֌W������ݒ肷��B
     *
     * @param relationDesc
     *            �e�����Ƃ̊֌W����
     */
    public void setParentIdDesc(String relationDesc) {
        this.parentIdDesc = relationDesc;
    }
    
    /**
     * �e�����Ƃ̊֌W������Ԃ��B
     *
     * @return �e�����Ƃ̊֌W����
     */
    public String getParentIdDesc() {
        return parentIdDesc;
    }
    
    /**
     * �e�����Ƃ̊֌W�̌n��ݒ肷��B
     *
     * @param relationCodeSys
     *            �e�����Ƃ̊֌W�̌n��ݒ肷��B
     */
    public void setParentIdCodeSys(String relationCodeSys) {
        this.parentIdCodeSys = relationCodeSys;
    }
    
    /**
     * �e�����Ƃ̊֌W�̌n��Ԃ��B
     *
     * @return �e�����Ƃ̊֌W�̌n
     */
    public String getParentIdCodeSys() {
        return parentIdCodeSys;
    }
    
    /**
     * �A�N�Z�X����Ԃ��B
     *
     * @return AccessRightModel�̃R���N�V����
     */
    public Collection<AccessRightModel> getAccessRights() {
        return accessRights;
    }
    
    /**
     * �A�N�Z�X����ݒ肷��B
     *
     * @param sccessRights �A�N�Z�X���̃R���N�V����
     */
    public void setAccessRights(Collection<AccessRightModel> accessRights) {
        this.accessRights = accessRights;
    }
    
    /**
     * �A�N�Z�X����ǉ�����B
     *
     * @param accessRight �ǉ�����A�N�Z�X��
     */
    public void addAccessRight(AccessRightModel accessRight) {
        if (accessRights == null) {
            accessRights = new ArrayList<AccessRightModel>(3);
        }
        accessRights.add(accessRight);
    }
    
    /**
     * ���̕����̃X�e�[�^�X��ݒ肷��B
     *
     * @param status
     *            ���̕����̃X�e�[�^�X
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * ���̕����̃X�e�[�^�X��Ԃ��B
     *
     * @return ���̕����̃X�e�[�^�X
     */
    public String getStatus() {
        return status;
    }
    
    public String getHandleClass() {
        return handleClass;
    }

    public void setHandleClass(String handleClass) {
        this.handleClass = handleClass;
    }
    
    /**
     * �n�b�V���l��Ԃ��B
     */
    @Override
    public int hashCode() {
        return docId.hashCode() + 11;
    }
    
    /**
     * ����ID�� eqaul ���ǂ�����Ԃ��B
     *
     * @return equal �̎� true
     */
    @Override
    public boolean equals(Object other) {
        if (other != null && getClass() == other.getClass()) {
            return getDocId().equals(((DocInfoModel) other).getDocId());
        }
        return false;
    }
    
    /**
     * �ŏ��̊m����y�ъm����Ŕ�r����B
     *
     * @return ��r�l
     */
    @Override
    public int compareTo(Object other) {
        if (other != null && getClass() == other.getClass()) {
            Date val1 = getFirstConfirmDate();
            Date val2 = ((DocInfoModel) other).getFirstConfirmDate();
            int result = val1.compareTo(val2);
            if (result == 0) {
                val1 = getConfirmDate();
                val2 = ((DocInfoModel) other).getConfirmDate();
                result = val1.compareTo(val2);
            }
            return result;
        }
        return -1;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        DocInfoModel ret = new DocInfoModel();
//        ret.setAccessRights(this.getAccessRights());
        ret.setConfirmDate(this.getConfirmDate());
        ret.setDepartment(this.getDepartment());
        ret.setDepartmentCodeSys(this.getDepartmentCodeSys());
        ret.setDepartmentDesc(this.getDepartmentDesc());
//        ret.setDocPk(this.getDocPk());
//        ret.setDocId(this.getDocId());  //
        ret.setDocType(this.getDocType());
        ret.setFirstConfirmDate(this.getFirstConfirmDate());
        ret.setHandleClass(this.getHandleClass());
        ret.setHasImage(this.isHasImage());
        ret.setHasLaboTest(this.isHasLaboTest());
        ret.setHasMark(this.isHasMark());
        ret.setHasRp(this.isHasRp());
        ret.setHasTreatment(this.isHasTreatment());
        ret.setHealthInsurance(this.getHealthInsurance());
        ret.setHealthInsuranceCodeSys(this.getHealthInsuranceCodeSys());
        ret.setHealthInsuranceDesc(this.getHealthInsuranceDesc());
        ret.setHealthInsuranceGUID(this.getHealthInsuranceGUID());
//        ret.setParentId(this.getParentId());
//        ret.setParentIdCodeSys(this.getParentIdCodeSys());
//        ret.setParentIdDesc(this.getParentIdDesc());
//        ret.setParentIdRelation(this.getParentIdRelation());
//        ret.setParentPk(this.getParentPk()); //
        ret.setPurpose(this.getPurpose());
        ret.setPurposeCodeSys(this.getPurposeCodeSys());
        ret.setPurposeDesc(this.getPurposeDesc());
        ret.setStatus(this.getStatus());
        ret.setTitle(this.getTitle());
        ret.setVersionNotes(this.getVersionNotes());
        ret.setVersionNumber(this.getVersionNumber());
        return ret;

        // ret.setDocPk(this.getDocPk());
        // ret.setDocId(this.getDocId());
        // ret.setParentPk(this.getParentPk());
    }

    public String getLabtestOrderNumber() {
        return labtestOrderNumber;
    }

    public void setLabtestOrderNumber(String labtestOrderNumber) {
        this.labtestOrderNumber = labtestOrderNumber;
    }

    public boolean isSendClaim() {
        return sendClaim;
    }

    public void setSendClaim(boolean sendClaim) {
        this.sendClaim = sendClaim;
    }

    public boolean isSendLabtest() {
        return sendLabtest;
    }

    public void setSendLabtest(boolean sendLabtest) {
        this.sendLabtest = sendLabtest;
    }

    public boolean isSendMml() {
        return sendMml;
    }

    public void setSendMml(boolean sendMml) {
        this.sendMml = sendMml;
    }
}
