package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Date;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.DocumentListener;
import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.PatientMemoModel;
import open.dolphin.project.Project;

/**
 * ���҂̃�����\�����ҏW����N���X�B
 * 
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class MemoInspector {

    private boolean dirty;

    private JPanel memoPanel;
    
    private CompositeArea memoArea;

    private PatientMemoModel patientMemoModel;
    
    private ChartImpl context;

    /**
     * MemoInspector�I�u�W�F�N�g�𐶐�����B
     */
    public MemoInspector(ChartImpl context) {
        
        this.context = context;

        initComponents();
        update();

        memoArea.getDocument().addDocumentListener(new DocumentListener() {
            
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                dirtySet();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                dirtySet();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
            }
        });
    }

    /**
     * ���C�A�E�g�p�̃p�l����Ԃ��B
     * @return ���C�A�E�g�p�l��
     */
    public JPanel getPanel() {
        return memoPanel;
    }

    /**
     * GUI �R���|�[�l���g������������B
     */
    private void initComponents() {
        memoArea = new CompositeArea(5, 10);
        memoArea.setLineWrap(true);
        memoArea.setMargin(new java.awt.Insets(3, 3, 2, 2));
        memoArea.addFocusListener(AutoKanjiListener.getInstance());
        memoArea.setToolTipText("�����Ɏg�p���܂��B���e�͎����I�ɕۑ�����܂��B");

        memoPanel = new JPanel(new BorderLayout());
        memoPanel.add(memoArea, BorderLayout.CENTER);

        Dimension size = memoPanel.getPreferredSize();
        int h = size.height;
        int w = 268;
        size = new Dimension(w, h);
        memoPanel.setMinimumSize(size);
        memoPanel.setMaximumSize(size);

        //memoPanel.setMinimumSize(memoPanel.getPreferredSize());
        //memoPanel.setMaximumSize(new Dimension(268, 100));
    }

    /**
     * ���҃�����\������B
     */
    private void update() {
        List list = context.getKarte().getEntryCollection("patientMemo");
        if (list != null && list.size()>0) {
            patientMemoModel = (PatientMemoModel) list.get(0);
            memoArea.setText(patientMemoModel.getMemo());
        }
    }

    /**
     * �J���e�̃N���[�Y���ɃR�[������A���҃������X�V����B
     */
    public void save() {

        if (!dirty) {
            return;
        }

        if (patientMemoModel == null) {
            patientMemoModel =  new PatientMemoModel();
        }
        patientMemoModel.setKarte(context.getKarte());
        patientMemoModel.setCreator(Project.getUserModel());
        Date confirmed = new Date();
        patientMemoModel.setConfirmed(confirmed);
        patientMemoModel.setRecorded(confirmed);
        patientMemoModel.setStarted(confirmed);
        patientMemoModel.setStatus(IInfoModel.STATUS_FINAL);
        patientMemoModel.setMemo(memoArea.getText().trim());

        final DocumentDelegater ddl = new DocumentDelegater();

        Runnable r = new Runnable() {

            @Override
            public void run() {
                ddl.updatePatientMemo(patientMemoModel);
                patientMemoModel = null;
                context = null;
            }
        };

        Thread t = new Thread(r);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

    /**
     * �������e���ω��������A�{�^��������������B
     */
    private void dirtySet() {
        dirty = true;
    }
}
