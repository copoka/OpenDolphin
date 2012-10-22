/*
 * Created on 2005/06/01
 *
 */
package open.dolphin.client;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import open.dolphin.helper.GridBagBuilder;
import open.dolphin.project.DolphinPrincipal;
import open.dolphin.project.Project;
import open.dolphin.project.ProjectStub;

/**
 * HostSettingPanel
 *
 * @author Kazushi Minagawa
 */
public class HostSettingPanel extends AbstractSettingPanel {
    
    private String ipAddressPattern = "[A-Za-z0-9.\\-_]*";
    private static final String ID = "hostSetting";
    private static final String TITLE = "�T�[�o";
    private static final String ICON = "ntwrk_16.gif";
    
    // �ݒ�p�� GUI components
    private JRadioButton aspMember;
    private JRadioButton facilityUser;
    private JTextField userIdField;
    private JTextField hostAddressField;
    private JTextField facilityIdField;
    private JButton registTesterBtn;
    
    // JBoss Server PORT
    private int hostPort = 1099;
    
    /** ��ʗp�̃��f�� */
    private ServerModel model;
    
    private StateMgr stateMgr;
    
    public HostSettingPanel() {
        this.setId(ID);
        this.setTitle(TITLE);
        this.setIcon(ICON);
    }
    
    /**
     * �T�[�o�ݒ��ʂ��J�n����B
     */
    @Override
    public void start() {
        
        //
        // ��ʃ��f���𐶐�������������
        //
        model = new ServerModel();
        model.populate(getProjectStub());
        
        //
        // GUI �𐶐�����
        //
        initComponents();
        
        //
        // �R���e�i�ŕ\�������
        //
        bindModelToView();
    }
    
    /**
     * GUI �R���|�[�l���g������������B
     */
    private void initComponents() {
        
        String serverInfoText  = "�T�[�o���";
        String serverStyleText = "���p�`��:";
        String aspMemberText   = "ASP";
        String useLocaltext    = "�@���T�[�o";
        String ipAddressText   = "IP�A�h���X:";
        
        String userInfoText    = "���[�U���";
        String userIdText      = "���[�UID:";
        String facilityIdText  = "��Ë@��ID:";
        
        String initServerText  = "ASP�]���̐\������";
        String addSuperUserText = "�A�J�E���g�쐬";
        
        // �e�L�X�g�t�B�[���h�𐶐�����
        hostAddressField = GUIFactory.createTextField(10, null, null, null);
        facilityIdField = GUIFactory.createTextField(15, null, null, null);
        userIdField = GUIFactory.createTextField(10, null, null, null);
        
        // �p�^�[�������������
        RegexConstrainedDocument hostDoc = new RegexConstrainedDocument(ipAddressPattern);
        hostAddressField.setDocument(hostDoc);
        
        // �{�^���O���[�v�𐶐�����
        ButtonGroup bg = new ButtonGroup();
        aspMember = GUIFactory.createRadioButton(aspMemberText, null, bg);
        facilityUser = GUIFactory.createRadioButton(useLocaltext, null, bg);
        
        // �Ǘ��ғo�^�{�^��
        registTesterBtn = new JButton(addSuperUserText);
        
        // �T�[�o���p�l��
        GridBagBuilder gb = new GridBagBuilder(serverInfoText);
        int row = 0;
        JLabel label = new JLabel(serverStyleText, SwingConstants.RIGHT);
        JPanel panel = GUIFactory.createRadioPanel(new JRadioButton[]{aspMember,facilityUser});
        gb.add(label, 0, row, GridBagConstraints.EAST);
        gb.add(panel, 1, row, GridBagConstraints.WEST);
        
        row++;
        label = new JLabel(ipAddressText, SwingConstants.RIGHT);
        gb.add(label,            0, row, GridBagConstraints.EAST);
        gb.add(hostAddressField, 1, row, GridBagConstraints.WEST);
        JPanel sip = gb.getProduct();
        
        // ���[�U���p�l��
        gb = new GridBagBuilder(userInfoText);
        row = 0;
        label = new JLabel(userIdText, SwingConstants.RIGHT);
        gb.add(label,       0, row, GridBagConstraints.EAST);
        gb.add(userIdField, 1, row, GridBagConstraints.WEST);
        
        row++;
        label = new JLabel(facilityIdText, SwingConstants.RIGHT);
        gb.add(label,           0, row, GridBagConstraints.EAST);
        gb.add(facilityIdField, 1, row, GridBagConstraints.WEST);
        JPanel uip = gb.getProduct();
        
        // �A�J�E���g�쐬
        gb = new GridBagBuilder(initServerText);
        row = 0;
        label = new JLabel("");
        gb.add(label,           0, row, GridBagConstraints.EAST);
        gb.add(registTesterBtn, 1, row, GridBagConstraints.CENTER);
        JPanel iip = gb.getProduct();
        
        // �S�̃��C�A�E�g
        gb = new GridBagBuilder();
        gb.add(sip, 0, 0, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gb.add(uip, 0, 1, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gb.add(iip, 0, 2, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gb.add(new JLabel(""), 0, 3, GridBagConstraints.BOTH, 1.0, 1.0);
        setUI(gb.getProduct());
        
        //
        // �R���|�[�l���g�̃��X�i�ڑ����s��
        //
        connect();
    }
    
    /**
     * �R���|�[�l���g�̃��X�i�ڑ����s���B
     */
    private void connect() {
        
        stateMgr = new StateMgr();
        
        // TextField �֓��͂܂��͍폜���������ꍇ�AcutState �� checkState() �𑗂�
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

        hostAddressField.getDocument().addDocumentListener(dl);
        facilityIdField.getDocument().addDocumentListener(dl);
        userIdField.getDocument().addDocumentListener(dl);
        
        //
        // IME OFF FocusAdapter
        //
        hostAddressField.addFocusListener(AutoRomanListener.getInstance());
        facilityIdField.addFocusListener(AutoRomanListener.getInstance());
        userIdField.addFocusListener(AutoRomanListener.getInstance());
        
        // �T�[�o�̗��p�`�� ���W�I�{�^�����N���b�N���ꂽ��@cutState �� checkState �𑗂�
        ActionListener al = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                stateMgr.controlAddressField();
            }

        };
        aspMember.addActionListener(al);
        facilityUser.addActionListener(al);
        
        // �Ǘ��ғo�^�{�^�����N���b�N���ꂽ�玩�g��PropertyChangeListener �ɂ�
        // �Ǘ��ғo�^�_�C�A���O��ʃX���b�h�ŃX�^�[�g������
        registTesterBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                make5TestAccount();
            }
        });
        
        facilityIdField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hostAddressField.requestFocus();
            }
        });
        
        hostAddressField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userIdField.requestFocus();
            }
        });
        
        userIdField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hostAddressField.requestFocus();
            }
        });
        
        // ���O�C�����Ă����Ԃ̏ꍇ�A���̐ݒ�͂ł��Ȃ��悤�ɂ���
        if (isLoginState()) {
            facilityUser.setEnabled(false);
            aspMember.setEnabled(false);
            userIdField.setEnabled(false);
            hostAddressField.setEnabled(false);
            facilityIdField.setEnabled(false);
            registTesterBtn.setEnabled(false);
        }
    }
    
    /**
     * Model �l��\������B
     */
    private void bindModelToView() {
                
        // userId�ݒ肷��
        String val = model.getUserId();
        val = val != null ? val : "";
        userIdField.setText(val);

        // �{��ID��ݒ肷��
        val = model.getFacilityId();
        val = val != null ? val : "";
        facilityIdField.setText(val);

        // UserType �ŕ��򂷂�
        Project.UserType userType = model.getUserType();

        switch (userType) {
            case ASP_MEMBER:
                aspMember.doClick();
                break;

            case ASP_TESTER:
                aspMember.doClick();
                break;

            case FACILITY_USER:
                val = model.getIpAddress();
                val = val != null ? val : "";
                hostAddressField.setText(val);
                if (model.getPort() != 0) {
                    hostPort = model.getPort();
                }
                facilityUser.doClick();
                break;
        }
    }
    
    /**
     * View�̒l�����f���֐ݒ肷��B
     */
    private void bindViewToModel() {
        
        // �{��ID�ƃ��[�UID��ۑ�����
        String facilityId = facilityIdField.getText().trim();
        String userId = userIdField.getText().trim();
        model.setFacilityId(facilityId);
        model.setUserId(userId);
        
        // �����o�[�^�C�v��ۑ�����
        if (aspMember.isSelected()) {
            model.setUserType(Project.UserType.ASP_MEMBER);
        } else if (facilityUser.isSelected()) {
            String val = hostAddressField.getText().trim();
            if (!val.equals("")) {
                model.setUserType(Project.UserType.FACILITY_USER);
                model.setIpAddress(val);
            }
        }
        
        model.setPort(hostPort);
    }
    
    /**
     * 5���ԕ]���p�̃A�J�E���g���쐬����B
     */
    public void make5TestAccount() {
        AddFacilityDialog af = new AddFacilityDialog();
        //PropertyChangeListener pl = ProxyPropertyChangeListener.create(this, "newAccount", new Class[]{ServerInfo.class});
        PropertyChangeListener pl = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                newAccount((ServerInfo) evt.getNewValue());
            }
        };
        af.addPropertyChangeListener(AddFacilityDialog.ACCOUNT_INFO, pl);
        Thread t = new Thread(af);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }
    
    /**
     * �Ǘ��ғo�^�_�C�A���O�̌��ʂ��󂯎�����\������B
     */
    public void newAccount(ServerInfo info) {
        
        if (info != null) {
            facilityIdField.setText(info.getFacilityId());
            userIdField.setText(info.getAdminId());
            aspMember.doClick();
        }
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
     * �T�[�o��ʐݒ�p�̃��f���N���X�B
     */
    class ServerModel {
        
        private Project.UserType userType;
        private String ipAddress;
        private int port;
        private String facilityId;
        private String userId;
        
        public ServerModel() {
        }
        
        /**
         * ProjectStub ����|�s�����C�g����B
         */
        public void populate(ProjectStub stub) {
                        
            // userId�ݒ肷��
            setUserId(stub.getUserId());
            
            // �{��ID��ݒ肷��
            setFacilityId(stub.getFacilityId());
            
            // UserType��ݒ肷��
            setUserType(stub.getUserType());
            
            // IPAddress��ݒ肷��
            setIpAddress(stub.getHostAddress());
            
            // Port��ݒ肷��
            setPort(stub.getHostPort());
        }
        
        /**
         * ProjectStub�փ��X�g�A����B
         */
        public void restore(ProjectStub stub) {
            
            // �{��ID�ƃ��[�UID��ۑ�����
            stub.setFacilityId(getFacilityId());
            stub.setUserId(getUserId());
            
            // Principle��ۑ�����
            DolphinPrincipal principal = new DolphinPrincipal();
            principal.setFacilityId(getFacilityId());
            principal.setUserId(getUserId());
            stub.setDolphinPrincipal(principal);
            
            // �����o�[�^�C�v��ۑ�����
            stub.setUserType(getUserType());
            
            // IPAddress��ۑ�����
            stub.setHostAddress(getIpAddress());
            
            // Port��ݒ��ۑ�����
            stub.setHostPort(getPort());
        }

        public Project.UserType getUserType() {
            return userType;
        }

        public void setUserType(Project.UserType userType) {
            this.userType = userType;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getFacilityId() {
            return facilityId;
        }

        public void setFacilityId(String facilityId) {
            this.facilityId = facilityId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
    
    /**
     * Mediator �I StateMgr �N���X�B
     */
    class StateMgr {
        
        public void checkState() {
            
            AbstractSettingPanel.State newState = isValid() 
                                                ? AbstractSettingPanel.State.VALID_STATE 
                                                : AbstractSettingPanel.State.INVALID_STATE;
            if (newState != state) {
                setState(newState);
            }
        }
        
        public void controlAddressField() {
            
            if (aspMember.isSelected()) {
                hostAddressField.setText("");
                hostAddressField.setEnabled(false);
                
            } else if (facilityUser.isSelected()) {
                hostAddressField.setEnabled(true);
            }
            
            this.checkState();
        }
        
        private boolean isValid() {
            
            boolean hostAddrOk = isIPAddress(hostAddressField.getText().trim());
            boolean facilityIdOk = (facilityIdField.getText().trim().equals("") == false) ? true : false;
            boolean userIdOk = (userIdField.getText().trim().equals("") == false) ? true : false;
            
            if (facilityUser.isSelected()) {
                return (facilityIdOk && hostAddrOk && userIdOk) ? true : false;
            } else {
                return (facilityIdOk && userIdOk) ? true : false;
            }
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
    }
}
