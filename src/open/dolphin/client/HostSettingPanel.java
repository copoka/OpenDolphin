package open.dolphin.client;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
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
    
    private static final String DEFAULT_FACILITY_OID = "1.3.6.1.4.1.9414.10.1";
    
    private String ipAddressPattern = "[A-Za-z0-9.\\-_]*";
    private static final String ID = "hostSetting";
    private static final String TITLE = "�T�[�o";
    private static final String ICON = "ntwrk_24.gif";
   
    private JTextField userIdField;
    private JTextField hostAddressField;
    
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
        String ipAddressText   = "IP�A�h���X:";
        
        String userInfoText    = "���[�U���";
        String userIdText      = "���[�UID:";
        
        // �e�L�X�g�t�B�[���h�𐶐�����
        hostAddressField = GUIFactory.createTextField(10, null, null, null);
        userIdField = GUIFactory.createTextField(10, null, null, null);
        
        // �p�^�[�������������
        RegexConstrainedDocument hostDoc = new RegexConstrainedDocument(ipAddressPattern);
        hostAddressField.setDocument(hostDoc);
        
        // �T�[�o���p�l��
        GridBagBuilder gb = new GridBagBuilder(serverInfoText);
        int row = 0;
        JLabel label = new JLabel(ipAddressText, SwingConstants.RIGHT);
        gb.add(label,            0, row, GridBagConstraints.EAST);
        gb.add(hostAddressField, 1, row, GridBagConstraints.WEST);
        JPanel sip = gb.getProduct();
        
        // ���[�U���p�l��
        gb = new GridBagBuilder(userInfoText);
        row = 0;
        label = new JLabel(userIdText, SwingConstants.RIGHT);
        gb.add(label,       0, row, GridBagConstraints.EAST);
        gb.add(userIdField, 1, row, GridBagConstraints.WEST);
        JPanel uip = gb.getProduct();
        
        // �S�̃��C�A�E�g
        gb = new GridBagBuilder();
        gb.add(sip, 0, 0, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gb.add(uip, 0, 1, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gb.add(new JLabel(""), 0, 2, GridBagConstraints.BOTH, 1.0, 1.0);
        setUI(gb.getProduct());
        
        connect();
    }
    
    /**
     * �R���|�[�l���g�̃��X�i�ڑ����s���B
     */
    private void connect() {
        
        stateMgr = new StateMgr();
        
        // TextField �֓��͂܂��͍폜���������ꍇ�AcutState �� checkState() �𑗂�
        DocumentListener dl = ProxyDocumentListener.create(stateMgr, "checkState");
        hostAddressField.getDocument().addDocumentListener(dl);
        userIdField.getDocument().addDocumentListener(dl);
        
        //
        // IME OFF FocusAdapter
        //
        hostAddressField.addFocusListener(AutoRomanListener.getInstance());
        userIdField.addFocusListener(AutoRomanListener.getInstance());        
        
        hostAddressField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                userIdField.requestFocus();
            }
        });
        
        userIdField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hostAddressField.requestFocus();
            }
        });
        
        // ���O�C�����Ă����Ԃ̏ꍇ�A���̐ݒ�͂ł��Ȃ��悤�ɂ���
        if (isLoginState()) {
            userIdField.setEnabled(false);
            hostAddressField.setEnabled(false);
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

        // UserType �ŕ��򂷂�
        Project.UserType userType = model.getUserType();

        switch (userType) {

            case FACILITY_USER:
                val = model.getIpAddress();
                val = val != null ? val : "";
                hostAddressField.setText(val);
                if (model.getPort() != 0) {
                    hostPort = model.getPort();
                }
                break;
        }
    }
    
    /**
     * View�̒l�����f���֐ݒ肷��B
     */
    private void bindViewToModel() {
        
        // �{��ID�ƃ��[�UID��ۑ�����
        // �{��ID�ƃ��[�UID��ۑ�����
        String facilityId = DEFAULT_FACILITY_OID;
        String userId = userIdField.getText().trim();
        String ipAddress = hostAddressField.getText().trim();
        model.setFacilityId(facilityId);
        model.setUserId(userId);
        model.setIpAddress(ipAddress);
        model.setUserType(Project.UserType.FACILITY_USER);
        model.setPort(hostPort);
    }
    
    /**
     * �ݒ�l��ۑ�����B
     */
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
        
        private boolean isValid() {
            
            boolean hostAddrOk = (hostAddressField.getText().trim().equals("") == false) ? true : false;
            boolean userIdOk = (userIdField.getText().trim().equals("") == false) ? true : false;
            
            return (hostAddrOk && userIdOk) ? true : false;
            
        }
    }
}
