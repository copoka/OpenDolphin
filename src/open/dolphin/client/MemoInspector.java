/*
 * MemoInspector.java
 *
 * Created on 2007/01/18, 17:10
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.im.InputSubset;
import java.util.Date;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentListener;
import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.PatientMemoModel;
import open.dolphin.project.Project;

/**
 *
 * @author kazm
 */
public class MemoInspector {

    private boolean dirty;

    private JPanel memoPanel;
    
    private JTextArea memoArea;
    
    private JButton updateMemoBtn;

    private PatientMemoModel patientMemoModel;
    
    private ChartPlugin context;

    /**
     * MemoInspector�I�u�W�F�N�g�𐶐�����B
     */
    public MemoInspector(ChartPlugin context) {
        this.context = context;
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
        
        int[] memoSize = ClientContext.getIntArray("patientInspector.memoInspector.textArea.size"); // 5,10
        ImageIcon updateIcon = ClientContext.getImageIcon("ref_16.gif");

        memoArea = new JTextArea(5, 10);
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

        updateMemoBtn.setEnabled(false);

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

        // worker thread
        Runnable r = new Runnable() {
            public void run() {
                fireStart();
                DocumentDelegater ddl = new DocumentDelegater();
                ddl.updatePatientMemo(patientMemoModel);
                dirty = false;
                fireStop();
            }
        };
        Thread t = new Thread(r);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

    /**
     * �X�V�{�^���𖳌��ɂ� progress bar ���J�n����B
     */
    private void fireStart() {
        Runnable awt = new Runnable() {
            public void run() {
                updateMemoBtn.setEnabled(false);
                context.getStatusPanel().start();
            }
        };
        SwingUtilities.invokeLater(awt);
    }

    /**
     * progress bar ���X�g�b�v����B
     */
    private void fireStop() {
        Runnable awt = new Runnable() {
            public void run() {
                context.getStatusPanel().stop();
            }
        };
        SwingUtilities.invokeLater(awt);
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
