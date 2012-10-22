package open.dolphin.client;

import java.awt.Dimension;
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
    private ChartImpl context;
    
    private boolean bMemo;
    private boolean bAllergy;
    private boolean bPhysical;
    private boolean bCalendar;  
    
    /**
     * ���҃C���X�y�N�^�N���X�𐶐�����B
     *
     * @param context �C���X�y�N�^�̐e�R���e�L�X�g
     */
    public PatientInspector(ChartImpl context) {
        
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
        memoInspector.save();
    }
    
    /**
     * �R���e�L�X�g��Ԃ��B
     */
    public ChartImpl getContext() {
        return context;
    }
    
    /**
     * �R���e�L�X�g��ݒ肷��B
     */
    public void setContext(ChartImpl context) {
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
    
    
    private void initComponents() {
        
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
        
        String topInspector = Project.getPreferences().get("topInspector", "����");
        String secondInspector = Project.getPreferences().get("secondInspector", "�J�����_");
        String thirdInspector = Project.getPreferences().get("thirdInspector", "��������");
        String forthInspector = Project.getPreferences().get("forthInspector", "�A�����M");
        
        // �e�C���X�y�N�^�𐶐�����
        basicInfoInspector = new BasicInfoInspector(context);
        patientVisitInspector = new PatientVisitInspector(context);
        memoInspector = new MemoInspector(context);
        docHistory = new DocumentHistory(getContext());
        allergyInspector = new AllergyInspector(context);
        physicalInspector = new PhysicalInspector(context);
        
        // �^�u�p�l���֊i�[����(���������A���N�ی��A�A�����M�A�g���̏d�̓^�u�p�l���Ő؂�ւ��\������)
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(docHistoryTitle, docHistory.getPanel());
        
        int prefW = 260;
        int prefW2 = 260;
        if (ClientContext.isMac()) {
            prefW2 += 20;
        }
        basicInfoInspector.getPanel().setPreferredSize(new Dimension(prefW2, 40));
        basicInfoInspector.getPanel().setMaximumSize(new Dimension(prefW2, 40));
        basicInfoInspector.getPanel().setMinimumSize(new Dimension(prefW2, 40));

        memoInspector.getPanel().setPreferredSize(new Dimension(prefW, 70));
        allergyInspector.getPanel().setPreferredSize(new Dimension(prefW, 100));
        docHistory.getPanel().setPreferredSize(new Dimension(prefW, 280));
        physicalInspector.getPanel().setPreferredSize(new Dimension(prefW, 110));
        
        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        
        // �����̃��C�A�E�g���s��
        layoutRow(container, topInspector);
        layoutRow(container, secondInspector);
        layoutRow(container, thirdInspector);
        layoutRow(container, forthInspector);
        
        // �����Ƀ��C�A�E�g����Ȃ��������̂��^�u�Ɋi�[����
        if (!bMemo) {
            tabbedPane.addTab(memoTitle, memoInspector.getPanel());
        }
        
        if (!bCalendar) {
            tabbedPane.addTab(pvtTitle, patientVisitInspector.getPanel());
        }
        
        if (!bAllergy) {
            tabbedPane.addTab(allergyTitle, allergyInspector.getPanel());
        }
        
        if (!bPhysical) {
            tabbedPane.addTab(physicalTitle, physicalInspector.getPanel());
        }
    }
    
    private void layoutRow(JPanel content, String itype) {
        
        if (itype.equals("����")) {
           //memoInspector.getPanel().setBorder(BorderFactory.createTitledBorder("����"));
           content.add(memoInspector.getPanel());
           bMemo = true;
        
        } else if (itype.equals("�J�����_")) {
            //patientVisitInspector.getPanel().setBorder(BorderFactory.createTitledBorder("���@��"));
            content.add(patientVisitInspector.getPanel());
            bCalendar = true;
        
        } else if (itype.equals("��������")) {
            content.add(tabbedPane);
        
        } else if (itype.equals("�A�����M")) {
            //allergyInspector.getPanel().setBorder(BorderFactory.createTitledBorder("�A�����M"));
            content.add(allergyInspector.getPanel());
            bAllergy = true;
        
        } else if (itype.equals("�g���̏d")) {
            //physicalInspector.getPanel().setBorder(BorderFactory.createTitledBorder("�g���̏d"));
            content.add(physicalInspector.getPanel());
            bPhysical = true;
        }
    }
}




























