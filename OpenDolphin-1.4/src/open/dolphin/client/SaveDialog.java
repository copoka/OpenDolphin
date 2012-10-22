package open.dolphin.client;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * SaveDialog
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class SaveDialog {
    
    private static final String[] PRINT_COUNT = {
        "0", "1",  "2",  "3",  "4", "5"
    };
    
    private static final String[] TITLE_LIST = {"�o�ߋL�^", "����", "���u", "����", "�摜", "�w��"};
    
    private static final String TITLE = "�h�L�������g�ۑ�";
    private static final String SAVE = "�ۑ�";
    private static final String TMP_SAVE = "���ۑ�";
    
    private JCheckBox patientCheck;
    private JCheckBox clinicCheck;
    
    // �ۑ��{�^��
    private JButton okButton;
    
    // �L�����Z���{�^��
    private JButton cancelButton;
    
    // ���ۑ��{�^��
    private JButton tmpButton;
    
    private JTextField titleField;
    private JComboBox titleCombo;
    //private JLabel sendMmlLabel;
    private JComboBox printCombo;
    private JLabel departmentLabel;
    //private Frame parent;
    
    // CLAIM ���M
    private JCheckBox sendClaim;
    
    // �߂�l��SaveParams/
    private SaveParams value;
    
    // �_�C�A���O
    private JDialog dialog;
    
    /** 
     * Creates new OpenKarteDialog  
     */
    public SaveDialog(Window parent) {
        
        JPanel contentPanel = createComponent();
        
        Object[] options = new Object[]{okButton, tmpButton, cancelButton};
        
        JOptionPane jop = new JOptionPane(
                contentPanel,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                options,
                okButton);
        
        dialog = jop.createDialog(parent, ClientContext.getFrameTitle(TITLE));
    }
    
    public void start() {
        dialog.setVisible(true);
    }
    
    public SaveParams getValue() {
        return value;
    }
    
    /**
     * �R���|�[�l���g��SaveParams�̒l��ݒ肷��B
     */
    public void setValue(SaveParams params) {
        
        // Title��\������
        String val = params.getTitle();
        if (val != null && (!val.equals("") &&(!val.equals("�o�ߋL�^")))) {
            titleCombo.insertItemAt(val, 0);
        }
        titleCombo.setSelectedIndex(0);
        
        //
        // �f�ÉȂ�\������
        // ��t��񂩂�̐f�ÉȂ�ݒ肷��
        val = params.getDepartment();
        if (val != null) {
            String[] depts = val.split("\\s*,\\s*");
            if (depts[0] != null) {
                departmentLabel.setText(depts[0]);
            } else {
                departmentLabel.setText(val);
            }
        }
        
        // ��������I��
        int count = params.getPrintCount();
        if (count != -1) {
            printCombo.setSelectedItem(String.valueOf(count));
            
        } else {
            printCombo.setEnabled(false);
        }
        
        //
        // CLAIM ���M���`�F�b�N����
        //
        if (params.isDisableSendClaim()) {
            // �V���O���J���e�� CLAIM ���M���̂��s��Ȃ��ꍇ
            sendClaim.setEnabled(false);
        } else {
            sendClaim.setSelected(params.isSendClaim());
        }
        
        
        // �A�N�Z�X����ݒ肷��
        if (params.getSendMML()) {
            // ���҂ւ̎Q�ƂƐf�×��̂���{�݂̎Q�Ƌ���ݒ肷��
            boolean permit = params.isAllowPatientRef();
            patientCheck.setSelected(permit);
            permit = params.isAllowClinicRef();
            clinicCheck.setSelected(permit);
            
        } else {
            // MML ���M�����Ȃ��Ƃ�diasble�ɂ���
            patientCheck.setEnabled(false);
            clinicCheck.setEnabled(false);
        }
        
        checkTitle();
    }

    
    /**
     * GUI�R���|�[�l���g������������B
     */
    private JPanel createComponent() {
                
        // content
        JPanel content = new JPanel();
        content.setLayout(new GridLayout(0, 1));
        
        // ����Title
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titleCombo = new JComboBox(TITLE_LIST);
        titleCombo.setPreferredSize(new Dimension(220, titleCombo.getPreferredSize().height));
        titleCombo.setMaximumSize(titleCombo.getPreferredSize());
        titleCombo.setEditable(true);
        p.add(new JLabel("�^�C�g��:"));
        p.add(titleCombo);
        content.add(p);
        
        //
        // ComboBox �̃G�f�B�^�R���|�[�l���g�փ��X�i��ݒ肷��
        //
        titleField = (JTextField) titleCombo.getEditor().getEditorComponent();
        titleField.addFocusListener(AutoKanjiListener.getInstance());
        //titleField.getDocument().addDocumentListener(ProxyDocumentListener.create(this, "checkTitle"));
        titleField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkTitle();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkTitle();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkTitle();
            }
        });
        
        // �f�ÉȁA���������\�����郉�x���ƃp�l���𐶐�����
        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        departmentLabel = new JLabel();
        p1.add(new JLabel("�f�É�:"));
        p1.add(departmentLabel);
        
        p1.add(Box.createRigidArea(new Dimension(11, 0)));
        
        // Print
        printCombo = new JComboBox(PRINT_COUNT);
        printCombo.setSelectedIndex(1);
        p1.add(new JLabel("�������:"));
        p1.add(printCombo);
        
        content.add(p1);
        
        
        // AccessRight��ݒ肷��{�^���ƃp�l���𐶐�����
        patientCheck = new JCheckBox("���҂ɎQ�Ƃ�������");
        clinicCheck = new JCheckBox("�f�×��̂���a�@�ɎQ�Ƃ�������");
        
        //
        // CLAIM ���M����Ȃ�
        //
        sendClaim = new JCheckBox("�f�Ís�ׂ𑗐M���� (���ۑ��̏ꍇ�͑��M���Ȃ�)");
        JPanel p5 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p5.add(sendClaim);
        content.add(p5);
        
        // OK button
        okButton = new JButton(SAVE);
        okButton.setToolTipText("�f�Ís�ׂ̑��M�̓`�F�b�N�{�b�N�X�ɏ]���܂��B");
        okButton.addActionListener((ActionListener) EventHandler.create(ActionListener.class, this, "doOk"));
        okButton.setEnabled(false);
        
        // Cancel Button
        String buttonText =  (String)UIManager.get("OptionPane.cancelButtonText");
        cancelButton = new JButton(buttonText);
        cancelButton.addActionListener((ActionListener) EventHandler.create(ActionListener.class, this, "doCancel"));
        
        // ���ۑ� button
        tmpButton = new JButton(TMP_SAVE);
        tmpButton.setToolTipText("�f�Ís�ׂ͑��M���܂���B");
        tmpButton.addActionListener((ActionListener) EventHandler.create(ActionListener.class, this, "doTemp"));
        tmpButton.setEnabled(false);
        
        return content;
    }
    
    /**
     * �^�C�g���t�B�[���h�̗L�������`�F�b�N����B
     */
    public void checkTitle() {    
        boolean enabled = titleField.getText().trim().equals("") ? false : true;
        okButton.setEnabled(enabled);
        tmpButton.setEnabled(enabled);
    }
    
    
    /**
     * GUI�R���|�[�l���g����l���擾���Asaveparams�ɐݒ肷��B
     */
    public void doOk() {
        
        // �߂�l��Saveparams�𐶐�����
        value = new SaveParams();
        
        // �����^�C�g�����擾����
        String val = (String) titleCombo.getSelectedItem();
        if (! val.equals("")) {
            value.setTitle(val);
        } else {
            value.setTitle("�o�ߋL�^");
        }
        
        // Department
        val = departmentLabel.getText();
        value.setDepartment(val);
        
        // ����������擾����
        int count = Integer.parseInt((String)printCombo.getSelectedItem());
        value.setPrintCount(count);
        
        //
        // CLAIM ���M
        //
        value.setSendClaim(sendClaim.isSelected());
        
        // ���҂ւ̎Q�Ƌ����擾����
        boolean b = patientCheck.isSelected();
        value.setAllowPatientRef(b);
        
        // �f�×��̂���{�݂ւ̎Q�Ƌ���ݒ肷��
        b = clinicCheck.isSelected();
        value.setAllowClinicRef(b);
        
        close();
    }
    
      
    /**
     * ���ۑ��̏ꍇ�̃p�����[�^��ݒ肷��B
     */
    public void doTemp() {
        
        // �߂�l��Saveparams�𐶐�����
        value = new SaveParams();
        
        //
        // ���ۑ��ł��邱�Ƃ�ݒ肷��
        //
        value.setTmpSave(true);
        
        // �����^�C�g�����擾����
        String val = (String) titleCombo.getSelectedItem();
        if (! val.equals("")) {
            value.setTitle(val);
        }
        
        // Department
        val = departmentLabel.getText();
        value.setDepartment(val);
        
        //
        // ����������擾����
        // ���ۑ��ł�������邩���m��Ȃ�
        //
        int count = Integer.parseInt((String)printCombo.getSelectedItem());
        value.setPrintCount(count);
        
        //
        // CLAIM ���M
        //
        value.setSendClaim(false);
        
        // ���҂ւ̎Q�Ƌ����擾����
        boolean b = false;
        value.setAllowPatientRef(b);
        
        // �f�×��̂���{�݂ւ̎Q�Ƌ���ݒ肷��
        b = false;
        value.setAllowClinicRef(b);
        
        close();
    }
    
    /**
     * �L�����Z���������Ƃ�ݒ肷��B
     */
    public void doCancel() {
        value = null;
        close();
    }
    
    private void close() {
        dialog.setVisible(false);
        dialog.dispose();
    }
}