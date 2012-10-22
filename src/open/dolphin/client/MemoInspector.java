package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.util.Date;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.DocumentListener;
import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.helper.DBTask;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.PatientMemoModel;
import open.dolphin.project.Project;
import org.apache.log4j.Logger;

/**
 * ���҂̃�����\�����ҏW����N���X�B
 * 
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class MemoInspector {

    private boolean dirty;

    private JPanel memoPanel;
    
    private CompositeArea memoArea;
    
    private JButton updateMemoBtn;

    private PatientMemoModel patientMemoModel;
    
    private ChartImpl context;
    
    private Logger logger;

    /**
     * MemoInspector�I�u�W�F�N�g�𐶐�����B
     */
    public MemoInspector(ChartImpl context) {
        
        this.context = context;
        logger = ClientContext.getBootLogger();
        
        initComponents();
        update();
        memoArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateCheck();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateCheck();
            }

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
        
        //int[] memoSize = ClientContext.getIntArray("patientInspector.memoInspector.textArea.size"); // 5,10
        ImageIcon updateIcon = ClientContext.getImageIcon("ref_16.gif");

        memoArea = new CompositeArea(5, 10);
        memoArea.setLineWrap(true);
        memoArea.setMargin(new java.awt.Insets(3, 3, 2, 2));
        memoArea.addFocusListener(AutoKanjiListener.getInstance());

        updateMemoBtn = new JButton(updateIcon);
        updateMemoBtn.setMargin(new Insets(2, 2, 2, 2));
        updateMemoBtn.setEnabled(false);
        updateMemoBtn.addActionListener(ProxyActionListener.create(this, "updateMemo"));
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(updateMemoBtn);

        memoPanel = new JPanel(new BorderLayout());
        memoPanel.add(memoArea, BorderLayout.CENTER);
        memoPanel.add(btnPanel, BorderLayout.EAST);
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
     * ���҃������X�V����B
     */
    public void updateMemo() {

        //updateMemoBtn.setEnabled(false);

        if (patientMemoModel == null) {
            patientMemoModel =  new PatientMemoModel();
        }
        // �㏑���X�V
        Date confirmed = new Date();
        patientMemoModel.setKarte(context.getKarte());
        patientMemoModel.setCreator(Project.getUserModel());
        patientMemoModel.setConfirmed(confirmed);
        patientMemoModel.setRecorded(confirmed);
        patientMemoModel.setStarted(confirmed);
        patientMemoModel.setStatus(IInfoModel.STATUS_FINAL);
        patientMemoModel.setMemo(memoArea.getText().trim());
        
        DBTask task = new DBTask<Void>(context) {

            @Override
            protected Void doInBackground() throws Exception {
                logger.debug("updateMemo doInBackground");
                DocumentDelegater ddl = new DocumentDelegater();
                ddl.updatePatientMemo(patientMemoModel);
                return null;
            }
            
            @Override
            protected void succeeded(Void result) {
                logger.debug("updateMemo succeeded");
                dirty = false;
            }
        };
        
        task.execute();
    }

    /**
     * �������e���ω��������A�{�^��������������B
     */
    private void updateCheck() {
        boolean old = dirty;
        dirty = true;
        if (old != dirty) {
            updateMemoBtn.setEnabled(true);
        }
    }
}
