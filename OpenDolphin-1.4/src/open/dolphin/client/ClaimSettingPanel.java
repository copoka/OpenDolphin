package open.dolphin.client;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import open.dolphin.helper.GridBagBuilder;

import open.dolphin.project.ProjectStub;

/**
 * ClaimSettingPanel
 *
 * @author Kazushi Minagawa Digital Globe, Inc.
 *
 */
public class ClaimSettingPanel extends AbstractSettingPanel {
    
    private static final String ID = "claimSetting";
    private static final String TITLE = "���Z�R��";
    private static final String ICON = "calc_16.gif";
    
    // GUI staff
    private JRadioButton sendClaimYes;
    private JRadioButton sendClaimNo;
    private JComboBox claimHostCombo;
    private JCheckBox claim01;
    private JTextField jmariField;
    private JTextField claimAddressField;
    private JTextField claimPortField;
    private JCheckBox useAsPVTServer;
    private JTextField bindAddress;
    
    /** ��ʃ��f�� */
    private ClaimModel model;
    
    private StateMgr stateMgr;
    
    
    public ClaimSettingPanel() {
        this.setId(ID);
        this.setTitle(TITLE);
        this.setIcon(ICON);
    }
    
    /**
     * GUI �y�� State �𐶐�����B
     */
    @Override
    public void start() {
        
        //
        // ���f���𐶐�������������
        //
        model = new ClaimModel();
        model.populate(getProjectStub());
        
        //
        // GUI���\�z����
        //
        initComponents();
        
        //
        // bind ����
        //
        bindModelToView();
    }
    
    /**
     * �ݒ�l��ۑ�����B
     */
    @Override
    public void save() {
        bindViewToModel();
        model.restore(getProjectStub());
    }
    
    /**
     * GUI���\�z����
     */
    private void initComponents() {
        
        // �f�Ís�ב��M�{�^��
        ButtonGroup bg1 = new ButtonGroup();
        sendClaimYes = GUIFactory.createRadioButton("���M����", null, bg1);
        sendClaimNo = GUIFactory.createRadioButton("���M���Ȃ�", null, bg1);
        
        // 01 �����ȓ�
        claim01 = new JCheckBox("�f�t�H���g01���g�p");
        
        // JMARI�A�z�X�g���A�A�h���X�A�|�[�g�ԍ�
        String[] hostNames = ClientContext.getStringArray("settingDialog.claim.hostNames");
        claimHostCombo = new JComboBox(hostNames);
        jmariField = GUIFactory.createTextField(10, null, null, null);
        jmariField.setToolTipText("��Ë@�փR�[�h�̐��������̂�12������͂��Ă��������B");
        claimAddressField = GUIFactory.createTextField(10, null, null, null);
        claimPortField = GUIFactory.createTextField(5, null, null, null);
        
        // ��t��M�{�^��
        useAsPVTServer = GUIFactory.createCheckBox("���̃}�V����ORCA����̎�t������M����", null);
        useAsPVTServer.setToolTipText("���̃}�V����ORCA����̎�t������M����ꍇ�̓`�F�b�N���Ă�������");
        bindAddress = GUIFactory.createTextField(10, null, null, null);
        bindAddress.setToolTipText("�����l�b�g���[�N�J�[�h������ꍇ�A��t��M�T�[�o�̃o�C���h�A�h���X����͂��Ă�������");
        
        // CLAIM�i�����j���M���
        GridBagBuilder gbl = new GridBagBuilder("CLAIM�i�����f�[�^�j���M");
        int row = 0;
        JLabel label = new JLabel("�f�Ís�ב��M:");
        JPanel panel = GUIFactory.createRadioPanel(new JRadioButton[]{sendClaimYes,sendClaimNo});
        gbl.add(label, 0, row, GridBagConstraints.EAST);
        gbl.add(panel, 1, row, GridBagConstraints.CENTER);
        JPanel sendClaim = gbl.getProduct();
        
        // ���Z�R�����
        gbl = new GridBagBuilder("���Z�R�����");
        row = 0;
        label = new JLabel("�@��:");
        gbl.add(label,          0, row, GridBagConstraints.EAST);
        gbl.add(claimHostCombo, 1, row, GridBagConstraints.WEST);
        
        row++;
        label = new JLabel("CLAIM�f�ÉȃR�[�h:");
        gbl.add(label,  0, row, GridBagConstraints.EAST);
        gbl.add(claim01,1, row, GridBagConstraints.WEST);
        
        row++;
        label = new JLabel("��Ë@��ID:  JPN");
        gbl.add(label,      0, row, GridBagConstraints.EAST);
        gbl.add(jmariField, 1, row, GridBagConstraints.WEST);
        
        row++;
        label = new JLabel("IP�A�h���X:");
        gbl.add(label,             0, row, GridBagConstraints.EAST);
        gbl.add(claimAddressField, 1, row, GridBagConstraints.WEST);
        
        row++;
        label = new JLabel("�|�[�g�ԍ�:");
        gbl.add(label,          0, row, GridBagConstraints.EAST);
        gbl.add(claimPortField, 1, row, GridBagConstraints.WEST);
        JPanel port = gbl.getProduct();
        
        // ���Z�R������̎�t��M
        gbl = new GridBagBuilder("��t���̎�M");
        label = new JLabel("�o�C���h�A�h���X(�I�v�V����):");
        gbl.add(useAsPVTServer, 0, 0, 2, 1, GridBagConstraints.EAST);
        gbl.add(label,          0, 1, GridBagConstraints.EAST);
        gbl.add(bindAddress,    1, 1, GridBagConstraints.WEST);
        JPanel pvt = gbl.getProduct();
        
        // �S�̃��C�A�E�g
        gbl = new GridBagBuilder();
        gbl.add(sendClaim, 0, 0, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gbl.add(port,      0, 1, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gbl.add(pvt,       0, 2, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gbl.add(new JLabel(""), 0, 3, GridBagConstraints.BOTH,  1.0, 1.0);
        setUI(gbl.getProduct());

        connect();       
    }
    
    /**
     * ���X�i��ڑ�����B
     */
    private void connect() {
        
        stateMgr = new StateMgr();
        
        // DocumentListener
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

        String jmariPattern = "[0-9]*";
        RegexConstrainedDocument jmariDoc = new RegexConstrainedDocument(jmariPattern);
        jmariField.setDocument(jmariDoc);
        jmariField.getDocument().addDocumentListener(dl);
        jmariField.addFocusListener(AutoRomanListener.getInstance());
        
        String portPattern = "[0-9]*";
        RegexConstrainedDocument portDoc = new RegexConstrainedDocument(portPattern);
        claimPortField.setDocument(portDoc);
        claimPortField.getDocument().addDocumentListener(dl);
        claimPortField.addFocusListener(AutoRomanListener.getInstance());
        
        String ipPattern = "[A-Za-z0-9.]*";
        RegexConstrainedDocument ipDoc = new RegexConstrainedDocument(ipPattern);
        claimAddressField.setDocument(ipDoc);
        claimAddressField.getDocument().addDocumentListener(dl);
        claimAddressField.addFocusListener(AutoRomanListener.getInstance());
        
        String ipPattern2 = "[A-Za-z0-9.]*";
        RegexConstrainedDocument ipDoc2 = new RegexConstrainedDocument(ipPattern2);
        bindAddress.setDocument(ipDoc2);
        bindAddress.getDocument().addDocumentListener(dl);
        bindAddress.addFocusListener(AutoRomanListener.getInstance());
        
        // �A�N�V�������X�i
        ActionListener al = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                stateMgr.controlClaim();
            }
        };
        sendClaimYes.addActionListener(al);
        sendClaimNo.addActionListener(al);
        
        // �o�C���h�A�h���X
        ActionListener al3 = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                stateMgr.controlBindAddress();
            }
        };
        useAsPVTServer.addActionListener(al3);
    }
    
    /**
     * ModelToView
     */
    private void bindModelToView() {
        //
        // �f�Ís�ב��M��I������
        //
        boolean sending = model.isSendClaim();
        sendClaimYes.setSelected(sending);
        sendClaimNo.setSelected(!sending);
        claimPortField.setEnabled(sending);
        
        // JMARICode
        String jmari = model.getJmariCode();
        jmari = jmari != null ? jmari : "";
        if (!jmari.equals("") && jmari.startsWith("JPN")) {
            jmari = jmari.substring(3);
            jmariField.setText(jmari);
        }
        
        // CLAIM �z�X�g��IP�A�h���X��ݒ肷��
        String val = model.getClaimAddress();
        val = val != null ? val : "";
        claimAddressField.setText(val);
        
        // CLAIM �z�X�g�̃|�[�g�ԍ���ݒ肷��
        val = String.valueOf(model.getClaimPort());
        val = val != null ? val : "";
        claimPortField.setText(val);
        
        // �z�X�g��
        val = model.getClaimHostName();
        val = val != null ? val : "";
        claimHostCombo.setSelectedItem(val);
        
        // ��t��M
        boolean tmp = model.isUseAsPVTServer();
        useAsPVTServer.setSelected(tmp);
        
        // �o�C���h�A�h���X
        String bindAddr = model.getBindAddress();
        bindAddr = bindAddr != null ? bindAddr : "";
        bindAddress.setText(bindAddr);
        bindAddress.setEnabled(tmp);
        
        // 01 ������
        claim01.setSelected(model.isClaim01());
        
    }
    
    /**
     * ViewToModel
     */
    private void bindViewToModel() {
        //
        // �f�Ís�ב��M�A���ۑ����A�C�����A�a�����M
        // �̐ݒ��ۑ�����
        //
        model.setSendClaim(sendClaimYes.isSelected());
        
        // JMARI
        String jmari = jmariField.getText().trim();
        if (!jmari.equals("")) {
            model.setJmariCode("JPN"+jmari);
        } else {
            model.setJmariCode(null);
        }
        
        // �z�X�g����ۑ�����
        String val = (String)claimHostCombo.getSelectedItem();
        model.setClaimHostName(val);
        
        // IP�A�h���X��ۑ�����
        val = claimAddressField.getText().trim();
        model.setClaimAddress(val);
        
        // �|�[�g�ԍ���ۑ�����
        val = claimPortField.getText().trim();
        try {
            int port = Integer.parseInt(val);
            model.setClaimPort(port);
            
        } catch (NumberFormatException e) {
            model.setClaimPort(5001);
        }
        
        // ��t��M��ۑ�����
        model.setUseAsPVTServer(useAsPVTServer.isSelected());
        
        // �o�C���h�A�h���X��ۑ�����
        val = bindAddress.getText().trim();
        model.setBindAddress(val);
        
        // 01 ������
        model.setClaim01(claim01.isSelected());
    }
    
    /**
     * ��ʂ��o��N���X�B
     */
    class ClaimModel {
        
        private boolean sendClaim;
        private String claimHostName;
        private String version;
        private String jmariCode;
        private String claimAddress;
        private int claimPort;
        private boolean useAsPvtServer;
        private String bindAddress;
        private boolean claim01;
        
        public void populate(ProjectStub stub) {
            
            // �f�Ís�ב��M
            setSendClaim(stub.getSendClaim());
            
            // JMARI code
            setJmariCode(stub.getJMARICode());
            
            // CLAIM �z�X�g��IP�A�h���X
            setClaimAddress(stub.getClaimAddress());
            
            // CLAIM �z�X�g�̃|�[�g�ԍ�
            setClaimPort(stub.getClaimPort());
            
            // �z�X�g��
            setClaimHostName(stub.getClaimHostName());
            
            // ��t��M
            setUseAsPVTServer(stub.getUseAsPVTServer());
            
            // �o�C���h�A�h���X
            setBindAddress(stub.getBindAddress());
            
            // 01 �����ȓ�
            setClaim01(stub.isClaim01());
        }
        
        public void restore(ProjectStub stub) {
            
            // �f�Ís�ב��M
            stub.setSendClaim(isSendClaim());
            
            // JMARI
            stub.setJMARICode(getJmariCode());
            //System.out.println(stub.getJMARICode());
            
            // CLAIM �z�X�g��IP�A�h���X
            stub.setClaimAddress(getClaimAddress());
            
            // CLAIM �z�X�g�̃|�[�g�ԍ�
            stub.setClaimPort(getClaimPort());
            
            // �z�X�g��
            stub.setClaimHostName(getClaimHostName());
            
            // ��t��M
            stub.setUseAsPVTServer(isUseAsPVTServer());
            
            // �o�C���h�A�h���X
            stub.setBindAddress(getBindAddress());
            
            // 01 ������
            stub.setClaim01(isClaim01());
        }
        
        public boolean isSendClaim() {
            return sendClaim;
        }
        
        public void setSendClaim(boolean sendClaim) {
            this.sendClaim = sendClaim;
        }
        
        public boolean isUseAsPVTServer() {
            return useAsPvtServer;
        }
        
        public void setUseAsPVTServer(boolean useAsPvtServer) {
            this.useAsPvtServer = useAsPvtServer;
        }
        
        public String getClaimHostName() {
            return claimHostName;
        }
        
        public void setClaimHostName(String claimHostName) {
            this.claimHostName = claimHostName;
        }
        
        public String getClaimAddress() {
            return claimAddress;
        }
        
        public void setClaimAddress(String claimAddress) {
            this.claimAddress = claimAddress;
        }
        
        public String getBindAddress() {
            return bindAddress;
        }
        
        public void setBindAddress(String bindAddress) {
            this.bindAddress = bindAddress;
        }
        
        public int getClaimPort() {
            return claimPort;
        }
        
        public void setClaimPort(int claimPort) {
            this.claimPort = claimPort;
        }

        public String getJmariCode() {
            return jmariCode;
        }

        public void setJmariCode(String jmariCode) {
            this.jmariCode = jmariCode;
        }
        
        public boolean isClaim01() {
            return claim01;
        }
        
        public void setClaim01(boolean b) {
            this.claim01 = b;
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
        
        public void controlClaim() {
            //
            // �f�Ís�ׂ̑��M���s���ꍇ�̂�
            // ���ۑ��A�C���A�a�����M�A�z�X�g�I���A�|�[�g���A�N�e�B�u�ɂȂ�
            //
            boolean b = sendClaimYes.isSelected();
            
            claimPortField.setEnabled(b);
            
            this.checkState();
        }
        
        public void controlVersion() {
            boolean b = true;   //v40.isSelected();
            jmariField.setEnabled(b);
            this.checkState();
        }
        
        public void controlBindAddress() {
            boolean b = useAsPVTServer.isSelected();
            bindAddress.setEnabled(b);
            this.checkState();
        }
        
        private boolean isValid() {
            
            boolean jmariOk = false;
            boolean claimAddrOk = false;
            boolean claimPortOk = false;
            boolean bindAdrOk = false;
            
            String code = jmariField.getText().trim();
            if (!code.equals("") && code.length() == 12) {
                jmariOk = true;
            }
            
            if (sendClaimYes.isSelected()) {
                claimAddrOk = isIPAddress(claimAddressField.getText().trim());
                claimPortOk = isPort(claimPortField.getText().trim());
            } else {
                claimAddrOk = true;
                claimPortOk = true;
            }
            
            if (useAsPVTServer.isSelected()) {
                String test = bindAddress.getText().trim();
                if (test != null && (!test.equals(""))) {
                    bindAdrOk = isIPAddress(test);
                } else {
                    bindAdrOk = true;
                }
            } else {
                bindAdrOk = true;
            }
            
            return (jmariOk && claimAddrOk && claimPortOk && bindAdrOk) ? true : false;
        }
        
        private boolean isIPAddress(String test) {
            
            boolean ret = false;
            
            if (test != null) {
                test = test.replace('.', ':');
                String[] ips = test.split(":");
                if (ips.length == 4) {
                    try {
                        boolean num = true;
                        for (int i = 0; i < ips.length; i++) {
                            int a = Integer.parseInt(ips[i]);
                            if (a < 0 || a > 255) {
                                num = false;
                                break;
                            }
                        }
                        ret = num;
                        
                    } catch (Exception e) {
                    }
                }
            }
            
            return ret;
        }
        
        private boolean isPort(String test) {
            
            boolean ret = false;
            
            if (test != null) {
                try {
                    int port = Integer.parseInt(test);
                    ret = port < 0 || port > 65535 ? false : true;
                }catch (Exception e) {
                }
            }
            
            return ret;
        }
    }
}
