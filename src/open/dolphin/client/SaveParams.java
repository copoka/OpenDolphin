package open.dolphin.client;

/**
 * Parametrs to save document.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class SaveParams {
    
    // MML���M���邩�ǂ����̃t���O ���M���鎞 true
    private boolean sendMML;
    
    // �����^�C�g��
    private String title;
    
    // �f�Éȏ��
    private String department;
    
    // �������
    private int printCount = -1;
    
    // ���҂ւ̎Q�Ƃ������邩�ǂ����̃t���O ������Ƃ� true
    private boolean allowPatientRef;
    
    // �f�×��̂���{�݂ւ̎Q�Ƌ��t���O �����鎞 true
    private boolean allowClinicRef;
    
    // ���ۑ��̎� true
    private boolean tmpSave;
    
    // CLAIM ���M�t���O
    private boolean sendClaim;
    
    // CLAIM ���M�� disable �ɂ���
    private boolean disableSendClaim;

    
    /** 
     * Creates new SaveParams 
     */
    public SaveParams() {
        super();
    }
    
    public SaveParams(boolean sendMML) {
        this();
        this.sendMML = sendMML;
    }
    
    public boolean getSendMML() {
        return sendMML;
    }
    
    public void setSendMML(boolean b) {
        sendMML = b;
    }

    public String getTitle() {
        return title;
    }
    
    public void setTitle(String val) {
        title = val;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String val) {
        department = val;
    }
    
    public int getPrintCount() {
        return printCount;
    }
    
    public void setPrintCount(int val) {
        printCount = val;
    }
    
    public boolean isAllowPatientRef() {
        return allowPatientRef;
    }
    
    public void setAllowPatientRef(boolean b) {
        allowPatientRef = b;
    }
    
    public boolean isAllowClinicRef() {
        return allowClinicRef;
    }
    
    public void setAllowClinicRef(boolean b) {
        allowClinicRef = b;
    }

    public boolean isTmpSave() {
        return tmpSave;
    }

    public void setTmpSave(boolean tmpSave) {
        this.tmpSave = tmpSave;
    }
    
    public boolean isSendClaim() {
        return sendClaim;
    }

    public void setSendClaim(boolean sendClaim) {
        this.sendClaim = sendClaim;
    }

    public boolean isDisableSendClaim() {
        return disableSendClaim;
    }

    public void setDisableSendClaim(boolean disableSendClaim) {
        this.disableSendClaim = disableSendClaim;
    }
}