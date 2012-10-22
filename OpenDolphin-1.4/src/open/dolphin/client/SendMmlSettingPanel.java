package open.dolphin.client;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import open.dolphin.helper.GridBagBuilder;

import open.dolphin.project.ProjectStub;

/**
 * @author Kazushi Minagawa Digital Globe, Inc.
 *
 */
public class SendMmlSettingPanel extends AbstractSettingPanel {
    
    private static final String ID = "mmlSetting";
    private static final String TITLE = "MML�o��";
    private static final String ICON = "cd_16.gif";
    
    // MML���M�֌W�R���|�[�l���g
    private JRadioButton sendMML;
    private JRadioButton sendNoMML;
    private JRadioButton mml3;
    private JRadioButton mml23;
    //private JTextField uploaderServer;
    private JTextField shareDirectory;
    private JButton dirSettingBtn;
    //private JComboBox protocolCombo;
    
    private MmlModel model;
    
    private StateMgr stateMgr;
    
    public SendMmlSettingPanel() {
        this.setId(ID);
        this.setTitle(TITLE);
        this.setIcon(ICON);
    }
    
    /**
     * MML�o�͂��J�n����B
     */
    @Override
    public void start() {
        
        //
        // ���f���𐶐�����
        //
        model = new MmlModel();
        
        //
        // GUI
        //
        initComponents();
        
        //
        // populate
        //
        model.populate(getProjectStub());
        
    }
    
    /**
     * �ۑ�����B
     */
    @Override
    public void save() {
        model.restore(getProjectStub());
    }
    
    /**
     * GUI���\�z����B
     */
    private void initComponents() {
        
        // ����
        ButtonGroup bg = new ButtonGroup();
        sendMML = GUIFactory.createRadioButton("�s��", null, bg);
        sendNoMML = GUIFactory.createRadioButton("�s��Ȃ�", null, bg);
        bg = new ButtonGroup();
        mml3 = GUIFactory.createRadioButton("3.0", null, bg);
        mml23 = GUIFactory.createRadioButton("2.3", null, bg);
        mml23.setSelected(true);
        //uploaderServer = GUIFactory.createTextField(10, null, null, null);
        shareDirectory = GUIFactory.createTextField(12, null, null, null);
        dirSettingBtn = new JButton("�o�͐�ݒ�...");
        //protocolCombo = new JComboBox(new String[]{"Samba"});
        //
        // No Support
        //
        mml3.setEnabled(false);
        
        // ���C�A�E�g
        
        GridBagBuilder gbl = new GridBagBuilder("MML(XML)�o��");
        gbl.add(GUIFactory.createRadioPanel(new JRadioButton[]{sendMML,sendNoMML}), 0, 0, 2, 1, GridBagConstraints.CENTER);
        gbl.add(new JLabel("MML �o�[�W����:", SwingConstants.RIGHT),	                 0, 1, 1, 1, GridBagConstraints.EAST);
        gbl.add(GUIFactory.createRadioPanel(new JRadioButton[]{mml23,mml3}),        1, 1, 1, 1, GridBagConstraints.WEST);
        gbl.add(dirSettingBtn,                                                      0, 2, 1, 1, GridBagConstraints.EAST);
        gbl.add(shareDirectory,                                                     1, 2, 1, 1, GridBagConstraints.WEST);
        
        JPanel content = gbl.getProduct();
        
        // �S�̂����C�A�E�g����
        gbl = new GridBagBuilder();
        gbl.add(content,        0, 0, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gbl.add(new JLabel(""), 0, 1, GridBagConstraints.BOTH,       1.0, 1.0);
        
        setUI(gbl.getProduct());
        
    }
    
    public void connect() {
        
        stateMgr = new StateMgr();
        
        // MML���M�{�^�����N���b�N���ꂽ�� State check ���s��
        ActionListener al = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                stateMgr.controlSendMml();
            }
        };
        sendMML.addActionListener(al);
        sendNoMML.addActionListener(al);
        
        // �e�L�X�g�t�B�[���h�̃C�x���g����������@State check ���s��
        //DocumentListener dl = ProxyDocumentListener.create(stateMgr, "checkState");
        DocumentListener dl = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                stateMgr.checkState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                stateMgr.checkState();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                stateMgr.checkState();
            }
        };
        //uploaderServer.getDocument().addDocumentListener(dl);
        shareDirectory.getDocument().addDocumentListener(dl);
        
        //
        // IME OFF FocusAdapter
        //
        //uploaderServer.addFocusListener(AutoRomanListener.getInstance());
        shareDirectory.addFocusListener(AutoRomanListener.getInstance());

        dirSettingBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doDirectorySetting();
            }

        });
        
        stateMgr.controlSendMml();
        
    }

    // MML �̏o�͐�f�B���N�g�����w�肷��
    private void doDirectorySetting() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String dir = chooser.getSelectedFile().getPath();
            shareDirectory.setText(dir);
        }
    }

    
    class MmlModel {
        
        public void populate(ProjectStub stub) {
            
            boolean sending = stub.getSendMML();
            sendNoMML.setSelected(! sending);
            sendMML.setSelected(sending);
            //mml3.setEnabled(sending);
            //mml23.setEnabled(sending);
            //protocolCombo.setEnabled(sending);
            //uploaderServer.setEnabled(sending);
            //shareDirectory.setEnabled(sending);
            
            // V3 MML Version and Sending
//            String val = stub.getMMLVersion();
//            if (val != null && val.startsWith("2")) {
//                mml23.setSelected(true);
//            } else {
//                mml3.setSelected(true);
//            }
            mml23.setSelected(true);
            
            // ���M��
            //val = stub.getUploaderIPAddress();
            //if (val != null && ! val.equals("")) {
                //uploaderServer.setText(val);
            //}
            
            // ���M�f�B���N�g��
            String val = stub.getUploadShareDirectory();
            if (val != null && ! val.equals("")) {
                shareDirectory.setText(val);
            }
            
            connect();
        }
        
        public void restore(ProjectStub stub) {
            // �Z���^�[���M
            boolean b = sendMML.isSelected();
            stub.setSendMML(b);
            
            // MML �o�[�W����
            //String val = mml3.isSelected() ? "300" : "230";
            stub.setMMLVersion("230");
            
            // �A�b�v���[�_�A�h���X
//            val = uploaderServer.getText().trim();
//            if (! val.equals("")) {
//                stub.setUploaderIPAddress(val);
//            }
            
            // ���L�f�B���N�g��
            String val = shareDirectory.getText().trim();
            if (! val.equals("")) {
                if (val.endsWith(File.separator)) {
                    val = val.substring(0, val.length()-1);
                }
                stub.setUploadShareDirectory(val);
//                if (ClientContext.isWin()) {
//                    val = val.replace(File.separatorChar, '/');
//                }
//                stub.setUploadShareDirectory(val);
            }
        }
    }
    
    class StateMgr {
        
        public void checkState() {
            
            AbstractSettingPanel.State newState = isValid()
            ? AbstractSettingPanel.State.VALID_STATE
                    : AbstractSettingPanel.State.INVALID_STATE;
            if (newState != state) {
                setState(newState);
            }
        }
        
        public void controlSendMml() {
            boolean b = sendMML.isSelected();
            mml23.setEnabled(b);
            shareDirectory.setEnabled(b);
            this.checkState();
        }
        
        protected boolean isValid() {
            if (sendMML.isSelected()) {
                boolean shareOk = (! shareDirectory.getText().trim().equals("")) ? true : false;
                return shareOk;
            } else {
                return true;
            }
        }
    }
}
