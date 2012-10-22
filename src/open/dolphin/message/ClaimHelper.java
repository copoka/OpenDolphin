package open.dolphin.message;

import open.dolphin.infomodel.ClaimBundle;

/**
 * ClaimHelper
 *
 * @author Minagawa,Kazushi
 *
 */
public class ClaimHelper {
    
    /** �m��� */
    private String confirmDate;
    
    /** Creator ID */
    private String creatorId;
    
    /** Creator �� */
    private String creatorName;
    
    /** �f�ÉȃR�[�h */
    private String creatorDept;
    
    /** �f�ÉȖ� */
    private String creatorDeptDesc;
    
    /** ��Î��i */
    private String creatorLicense;
    
    /** ����ID */
    private String patientId;
    
    /** �����ړI */
    private String generationPurpose;
    
    /** ����ID */
    private String docId;
    
    /** ���N�ی� GUID */
    private String healthInsuranceGUID;
    
    /** ���N�ی��R�[�h�l */
    private String healthInsuranceClassCode;
    
    /** ���N�ی����� */
    private String healthInsuranceDesc;
    
    /** ClaimBundle �z�� */
    private ClaimBundle[] claimBundle;
    
    public void setConfirmDate(String confirmDate) {
        this.confirmDate = confirmDate;
    }
    
    public String getConfirmDate() {
        return confirmDate;
    }
    
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }
    
    public String getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }
    
    public String getCreatorName() {
        return creatorName;
    }
    
    public void setCreatorLicense(String creatorLicense) {
        this.creatorLicense = creatorLicense;
    }
    
    public String getCreatorLicense() {
        return creatorLicense;
    }
    
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
    
    public String getPatientId() {
        return patientId;
    }
    
    public void setGenerationPurpose(String generationPurpose) {
        this.generationPurpose = generationPurpose;
    }
    
    public String getGenerationPurpose() {
        return generationPurpose;
    }
    
    public void setDocId(String docId) {
        this.docId = docId;
    }
    
    public String getDocId() {
        return docId;
    }
    
    public void setHealthInsuranceGUID(String healthInsuranceGUID) {
        this.healthInsuranceGUID = healthInsuranceGUID;
    }
    
    public String getHealthInsuranceGUID() {
        return healthInsuranceGUID;
    }
    
    public void setHealthInsuranceClassCode(String healthInsuranceClassCode) {
        this.healthInsuranceClassCode = healthInsuranceClassCode;
    }
    
    public String getHealthInsuranceClassCode() {
        return healthInsuranceClassCode;
    }
    
    public void setHealthInsuranceDesc(String healthInsuranceDesc) {
        this.healthInsuranceDesc = healthInsuranceDesc;
    }
    
    public String getHealthInsuranceDesc() {
        return healthInsuranceDesc;
    }
    
    public void setClaimBundle(ClaimBundle[] claimBundle) {
        this.claimBundle = claimBundle;
    }
    
    public ClaimBundle[] getClaimBundle() {
        return claimBundle;
    }
    
    public void addClaimBundle(ClaimBundle val) {
        if (claimBundle == null) {
            claimBundle = new ClaimBundle[1];
            claimBundle[0] = val;
            return;
        }
        int len = claimBundle.length;
        ClaimBundle[] dest = new ClaimBundle[len + 1];
        System.arraycopy(claimBundle, 0, dest, 0, len);
        claimBundle = dest;
        claimBundle[len] = val;
    }

    public String getCreatorDept() {
        return creatorDept;
    }

    public void setCreatorDept(String creatorDept) {
        this.creatorDept = creatorDept;
    }

    public String getCreatorDeptDesc() {
        return creatorDeptDesc;
    }

    public void setCreatorDeptDesc(String creatorDeptDesc) {
        this.creatorDeptDesc = creatorDeptDesc;
    }
}
