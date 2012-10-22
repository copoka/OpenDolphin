package open.dolphin.client;

import open.dolphin.infomodel.PVTHealthInsuranceModel;


/**
 * NewKarteParams
 *
 * @author  Kazushi Minagawa
 */
public final class NewKarteParams {
    
    // �x�[�X�̃J���e�����邩�ǂ����A�^�u�y��EditorFrame�̕ʁA�C�����ǂ���
    private Chart.NewKarteOption option;
    
    // �󔒁A�S�R�s�[�A�O�񏈕��K�p�̃t���O
    private Chart.NewKarteMode createMode;
    
    // �f�É�
    private String department;
    
    // �f�ÉȃR�[�h
    private String departmentCode;
    
    // ���N�ی�
    private Object[] insurances;
    
    // ���������ɑI������ی�
    private int initialSelectedInsurance;
    
    // �_�C�A���O�Ń��[�U���I�������ی�
    private PVTHealthInsuranceModel insurance;
    
    // EditorFrame �ŕҏW���邩�ǂ����̃t���O 
    private boolean openFrame;
    
    // ��������h�L�������g�̎��
    // 2���J���e�A�V���O���A�Љ��
    private String docType;
    
    // �s��
    private String groupId;
    
    
    /** Creates a new instance of NewKarteParams */
    public NewKarteParams(Chart.NewKarteOption option) {
        this.option = option;
    }
    
    public Chart.NewKarteOption getOption() {
        return option;
    }
    
    public String getDocType() {
        return docType;
    }
    
    public String setDocType(String docType) {
        return this.docType = docType;
    }
    
    public String getGroupId() {
        return groupId;
    }
    
    public void setGroupId(String val) {
        groupId = val;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String val) {
        department = val;
    }
    
    public String getDepartmentCode() {
        return departmentCode;
    }
    
    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }
    
    public Object[] getInsurances() {
        return insurances;
    }
    
    public void setInsurances(Object[] ins) {
        insurances = ins;
    }
    
    public PVTHealthInsuranceModel getPVTHealthInsurance() {
        return insurance;
    }
    
    public void setPVTHealthInsurance(PVTHealthInsuranceModel val) {
        insurance = val;
    }
    
    public void setOpenFrame(boolean openFrame) {
        this.openFrame = openFrame;
    }
    
    public boolean isOpenFrame() {
        return openFrame;
    }
    
    public Chart.NewKarteMode getCreateMode() {
        return createMode;
    }
    
    public void setCreateMode(Chart.NewKarteMode createMode) {
        this.createMode = createMode;
    }
    
    public int getInitialSelectedInsurance() {
        return initialSelectedInsurance;
    }
    
    public void setInitialSelectedInsurance(int index) {
        initialSelectedInsurance = index;
    }
}