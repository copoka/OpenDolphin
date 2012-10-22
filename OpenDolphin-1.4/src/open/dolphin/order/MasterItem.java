package open.dolphin.order;

/**
 * Class to hold selected master item information.
 *
 * @author  Kazuhi Minagawa, Digital Globe, Inc.
 */
public class MasterItem implements java.io.Serializable {
    
    private static final long serialVersionUID = -6359300744722498857L;
    
    /** Claim subclass code �}�X�^���ڂ̎�� */
    // 0: ��Z  1: �ޗ�  2: ��� 3: �p�@ 4: ����
    private int classCode = -1;
    
    /** ���ږ� */
    private String name;
    
    /** ���ڃR�[�h */
    private String code;
    
    /** �R�[�h�̌n�� */
    private String masterTableId;
    
    /** ���� */
    private String number;
    
    /** �P�� */
    private String unit;
    
    /** �㎖�p�a���R�[�h */
    private String claimDiseaseCode;
    
    /** �f�Ís�׋敪(007)�E�_���W�v�� */
    private String claimClassCode;
    
    /** ��܂̏ꍇ�̋敪 ���p1�A�O�p6�A���˖�4 */
    private String ykzKbn;
    
    private String dummy;
    
    private String bundleNumber;
    
    
    /**
     * Creates new MasterItem
     */
    public MasterItem() {
    }
    
    public MasterItem(int classCode, String name, String code) {
        this();
        setClassCode(classCode);
        setName(name);
        setCode(code);
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
    /**
     * @param classCode The classCode to set.
     */
    public void setClassCode(int classCode) {
        this.classCode = classCode;
    }
    
    /**
     * @return Returns the classCode.
     */
    public int getClassCode() {
        return classCode;
    }
    
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param code The code to set.
     */
    public void setCode(String code) {
        this.code = code;
    }
    
    /**
     * @return Returns the code.
     */
    public String getCode() {
        return code;
    }
    
    /**
     * @param masterTableId The masterTableId to set.
     */
    public void setMasterTableId(String masterTableId) {
        this.masterTableId = masterTableId;
    }
    
    /**
     * @return Returns the masterTableId.
     */
    public String getMasterTableId() {
        return masterTableId;
    }
    
    /**
     * @param number The number to set.
     */
    public void setNumber(String number) {
        this.number = number;
    }
    
    /**
     * @return Returns the number.
     */
    public String getNumber() {
        return number;
    }
    
    /**
     * @param unit The unit to set.
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    /**
     * @return Returns the unit.
     */
    public String getUnit() {
        return unit;
    }
    
    /**
     * @param claimDiseaseCode The claimDiseaseCode to set.
     */
    public void setClaimDiseaseCode(String claimDiseaseCode) {
        this.claimDiseaseCode = claimDiseaseCode;
    }
    
    /**
     * @return Returns the claimDiseaseCode.
     */
    public String getClaimDiseaseCode() {
        return claimDiseaseCode;
    }
    
    /**
     * @param claimClassCode The claimClassCode to set.
     */
    public void setClaimClassCode(String claimClassCode) {
        this.claimClassCode = claimClassCode;
    }
    
    /**
     * @return Returns the claimClassCode.
     */
    public String getClaimClassCode() {
        return claimClassCode;
    }

    public String getYkzKbn() {
        return ykzKbn;
    }

    public void setYkzKbn(String ykzKbn) {
        this.ykzKbn = ykzKbn;
    }

    public String getDummy() {
        return dummy;
    }

    public void setDummy(String dummy) {
        this.dummy = dummy;
    }

    public String getBundleNumber() {
        return bundleNumber;
    }

    public void setBundleNumber(String bundleNumber) {
        this.bundleNumber = bundleNumber;
    }
}