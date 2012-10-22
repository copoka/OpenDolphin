package open.dolphin.client;


import java.awt.Dimension;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import open.dolphin.infomodel.KarteBean;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.project.Project;


public class PatientInspector {
    
    // �X�̃C���X�y�N�^
    // ���Ҋ�{���
    private BasicInfoInspector basicInfoInspector;
    
    // ���@��
    private PatientVisitInspector patientVisitInspector;
    
    // ���҃���
    private MemoInspector memoInspector;
    
    // ��������
    private DocumentHistory docHistory;
    
    // �A�����M
    private AllergyInspector allergyInspector;
    
    // �g���̏d
    private PhysicalInspector physicalInspector;
    
    // �C���X�y�N�^���i�[����^�u�y�C�� View
    private JTabbedPane tabbedPane;
    
    // ���̃N���X�̃R���e�i�p�l�� View
    private JPanel container;
    
    // Context ���̃C���X�y�N�^�̐e�R���e�L�X�g
    private ChartPlugin context;
    
    /**
     * ���҃C���X�y�N�^�N���X�𐶐�����B
     *
     * @param context �C���X�y�N�^�̐e�R���e�L�X�g
     */
    public PatientInspector(ChartPlugin context) {
        
        // ���̃C���X�y�N�^���i�[����� Chart Object
        setContext(context);
        
        // GUI ������������
        initComponents();
    }
    
    public void dispose() {
        // List ���N���A����
        docHistory.clear();
        allergyInspector.clear();
        physicalInspector.clear();
    }
    
    /**
     * �R���e�L�X�g��Ԃ��B
     */
    public ChartPlugin getContext() {
        return context;
    }
    
    /**
     * �R���e�L�X�g��ݒ肷��B
     */
    public void setContext(ChartPlugin context) {
        this.context = context;
    }
    
    /**
     * ���҃J���e��Ԃ��B
     * @return  ���҃J���e
     */
    public KarteBean getKarte() {
        return context.getKarte();
    }
    
    /**
     * ���҂�Ԃ��B
     * @return ����
     */
    public PatientModel getPatient() {
        return context.getKarte().getPatient();
    }
    
    /**
     * ��{���C���X�y�N�^��Ԃ��B
     * @return ��{���C���X�y�N�^
     */
    public BasicInfoInspector getBasicInfoInspector() {
        return basicInfoInspector;
    }
    
    /**
     * ���@���C���X�y�N�^��Ԃ��B
     * @return ���@���C���X�y�N�^
     */
    public PatientVisitInspector getPatientVisitInspector() {
        return patientVisitInspector;
    }
    
    /**
     * ���҃����C���X�y�N�^��Ԃ��B
     * @return ���҃����C���X�y�N�^
     */
    public MemoInspector getMemoInspector() {
        return memoInspector;
    }
    
    /**
     * ���������C���X�y�N�^��Ԃ��B
     * @return ���������C���X�y�N�^
     */
    public DocumentHistory getDocumentHistory() {
        return docHistory;
    }
    
    /**
     * ���C�A�E�g�̂��߂ɃC���X�y�N�^�̃R���e�i�p�l����Ԃ��B
     * @return �C���X�y�N�^�̃R���e�i�p�l��
     */
    public JPanel getPanel() {
        return container;
    }
    
    /**
     * GUI �R���|�[�l���g������������B
     *
     */
    private void initComponents() {
        
        // �^�u�y�у{�[�_�^�C�g�������擾����
        // ���@��
        String pvtTitle = ClientContext.getString("patientInspector.pvt.title");
        
        // ��������
        String docHistoryTitle = ClientContext.getString("patientInspector.docHistory.title");
        
        // �A�����M
        String allergyTitle = ClientContext.getString("patientInspector.allergy.title");
        
        // �g���̏d
        String physicalTitle = ClientContext.getString("patientInspector.physical.title");
        
        // ����
        String memoTitle = ClientContext.getString("patientInspector.memo.title");
        
        // �e�C���X�y�N�^�𐶐�����
        basicInfoInspector = new BasicInfoInspector(context);
        patientVisitInspector = new PatientVisitInspector(context);
        memoInspector = new MemoInspector(context);
        docHistory = new DocumentHistory(getContext());
        allergyInspector = new AllergyInspector(context);
        physicalInspector = new PhysicalInspector(context);
        
        // ���@���ƃ����͏�Ɍ�����悤�ɔz�u����
        JPanel patientVisitPanel = patientVisitInspector.getPanel();
        patientVisitPanel.setBorder(BorderFactory.createTitledBorder(pvtTitle));
        JPanel memoPanel = memoInspector.getPanel();
        memoPanel.setBorder(BorderFactory.createTitledBorder(memoTitle));
        
        // �^�u�p�l���֊i�[����(���������A���N�ی��A�A�����M�A�g���̏d�̓^�u�p�l���Ő؂�ւ��\������)
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(docHistoryTitle, docHistory.getPanel());
        tabbedPane.addTab(allergyTitle, allergyInspector.getPanel());
        tabbedPane.addTab(physicalTitle, physicalInspector.getPanel());
        
        // �S�̂�z�u����
        Preferences pref = Project.getPreferences();
        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        int memoLoc = pref.getInt(Project.INSPECTOR_MEMO_LOCATION, 0);
        
        switch (memoLoc) {
            
            case 0:
                // �J�����_�A���������A���� �i�f�t�H���g�j
                container.add(patientVisitPanel);
                container.add(Box.createRigidArea(new Dimension(0,7)));
                container.add(tabbedPane);
                container.add(Box.createRigidArea(new Dimension(0,7)));
                container.add(memoPanel);
                break;
                
            case 1:
                // �����A�J�����_�A��������
                container.add(memoPanel);
                container.add(Box.createRigidArea(new Dimension(0,7)));
                container.add(patientVisitPanel);
                container.add(Box.createRigidArea(new Dimension(0,7)));
                container.add(tabbedPane);
                break;
                
            case 2:
                // �����A���������J�����_
                container.add(memoPanel);
                container.add(Box.createRigidArea(new Dimension(0,7)));
                container.add(tabbedPane);
                container.add(Box.createRigidArea(new Dimension(0,7)));
                container.add(patientVisitPanel);
                break;
        }
    }
}
