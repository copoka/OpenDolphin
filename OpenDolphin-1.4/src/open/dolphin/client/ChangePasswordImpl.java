package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;

import org.jboss.security.Util;

import open.dolphin.infomodel.RoleModel;
import open.dolphin.delegater.UserDelegater;
import open.dolphin.infomodel.DepartmentModel;
import open.dolphin.infomodel.LicenseModel;
import open.dolphin.infomodel.UserModel;
import open.dolphin.project.*;

import java.awt.im.InputSubset;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;

/**
 * ChangePasswordPlugin
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class ChangePasswordImpl extends AbstractMainTool implements ChangeProfile {
    
    private static final String TITLE = "�v���t�B�[���ύX";
    //private static int DEFAULT_WIDTH = 568;
    //private static int DEFAULT_HEIGHT = 300;
    private static final String PROGRESS_NOTE = "���[�U����ύX���Ă��܂�...";
    private static final String UPDATE_BTN_TEXT = "�ύX";
    private static final String CLOSE_BTN_TEXT = "����";
    private static final String USER_ID_TEXT = "���[�UID:";
    private static final String PASSWORD_TEXT = "�p�X���[�h:";
    private static final String CONFIRM_TEXT = "�m�F:";
    private static final String SIR_NAME_TEXT = "��:";
    private static final String GIVEN_NAME_TEXT = "��:";
    private static final String EMAIL_TEXT = "�d�q���[��:";
    private static final String LISENCE_TEXT = "��Î��i:";
    private static final String DEPT_TEXT = "�f�É�:";
    private static final String PASSWORD_ASSIST_1 = "�p�X���[�h(���p�p����";
    private static final String PASSWORD_ASSIST_2 = "�����ȏ�";
    private static final String PASSWORD_ASSIST_3 = "�����ȓ�) �ύX���Ȃ��ꍇ�͋󔒂ɂ��Ă����܂��B";
    private static final String SUCCESS_MESSAGE = "���[�U����ύX���܂����B";
    private static final String DUMMY_PASSWORD = "";
    
    private JFrame frame;
    protected JButton okButton;
    
    private ApplicationContext appCtx;
    private Application app;
    private Logger logger;
    
    /**
     * Creates a new instance of AddUserService
     */
    public ChangePasswordImpl() {
        setName(TITLE);
        appCtx = ClientContext.getApplicationContext();
        app = appCtx.getApplication();
        logger = ClientContext.getBootLogger();
    }
    
    public void setFrame(JFrame frame) {
        this.frame = frame;
    }
    
    public JFrame getFrame() {
        return frame;
    }
    
    @Override
    public void start() {
        
        // Super Class �� Frame ������������

        Runnable awt = new Runnable() {

            public void run() {
                String title = ClientContext.getFrameTitle(getName());
                setFrame(new JFrame(title));
                getFrame().setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                getFrame().addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        stop();
                    }
                });
        //        ComponentMemory cm = new ComponentMemory(getFrame(), new Point(0, 0),
        //                new Dimension(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT)),
        //                this);
        //        cm.putCenter();

                ChangePasswordPanel cp = new ChangePasswordPanel();
                cp.get();
                getFrame().getContentPane().add(cp, BorderLayout.CENTER);
                getFrame().getRootPane().setDefaultButton(okButton);
                getFrame().pack();
                Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
                int x = (size.width - getFrame().getPreferredSize().width) / 2;
                int y = (size.height - getFrame().getPreferredSize().height) / 3;
                getFrame().setLocation(x, y);
                getFrame().setVisible(true);
            }
        };

        SwingUtilities.invokeLater(awt);
    }
    
    @Override
    public void stop() {
        getFrame().setVisible(false);
        getFrame().dispose();
    }
    
    public void toFront() {
        if (getFrame() != null) {
            getFrame().toFront();
        }
    }
    
    /**
     * �p�X���[�h�ύX�N���X�B
     */
    protected class ChangePasswordPanel extends JPanel {
        
        private JTextField uid; // ���p��ID
        private JPasswordField userPassword1; // �p�X���[�h1
        private JPasswordField userPassword2; // �p�X���[�h2
        private JTextField sn; // ��
        private JTextField givenName; // ��
        private JTextField email;
        private LicenseModel[] licenses; // �E��(MML0026)
        private JComboBox licenseCombo;
        private DepartmentModel[] depts; // �f�É�(MML0028)
        private JComboBox deptCombo;
        
        private JButton okButton;
        private JButton cancelButton;
        private boolean ok;
        
        private int[] userIdLength;
        private int[] passwordLength; // min,max
        
        
        public ChangePasswordPanel() {
            
            userIdLength = ClientContext.getIntArray("addUser.userId.length");
            passwordLength = ClientContext.getIntArray("addUser.password.length");
            
            FocusAdapter imeOn = new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent event) {
                    JTextField tf = (JTextField) event.getSource();
                    tf.getInputContext().setCharacterSubsets(
                            new Character.Subset[] { InputSubset.KANJI });
                }
            };
            
            FocusAdapter imeOff = new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent event) {
                    JTextField tf = (JTextField) event.getSource();
                    tf.getInputContext().setCharacterSubsets(null);
                }
            };
            
            // DocumentListener
            DocumentListener dl = new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                }
                public void insertUpdate(DocumentEvent e) {
                    checkButton();
                }
                public void removeUpdate(DocumentEvent e) {
                    checkButton();
                }
            };
            
            //
            // ���[�UID�t�B�[���h�𐶐�����
            //
            uid = createTextField(10, null, imeOff, null);
            String pattern = ClientContext.getString("addUser.pattern.idPass");
            RegexConstrainedDocument userIdDoc = new RegexConstrainedDocument(pattern);
            uid.setDocument(userIdDoc);
            uid.getDocument().addDocumentListener(dl);
            uid.setToolTipText(pattern);
            
            //
            // �p�X���[�h�t�B�[���h��ݒ肷��
            //
            userPassword1 = createPassField(10, null, imeOff, null);
            userPassword1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    userPassword2.requestFocus();
                }
            });
            
            userPassword2 = createPassField(10, null, imeOff, null);
            userPassword2.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    sn.requestFocus();
                }
            });
            RegexConstrainedDocument passwordDoc1 = new RegexConstrainedDocument(pattern);
            userPassword1.setDocument(passwordDoc1);
            userPassword1.setToolTipText(pattern);
            userPassword1.getDocument().addDocumentListener(dl);
            RegexConstrainedDocument passwordDoc2 = new RegexConstrainedDocument(pattern);
            userPassword2.setDocument(passwordDoc2);
            userPassword2.getDocument().addDocumentListener(dl);
            userPassword2.setToolTipText(pattern);
            
            //
            // ��
            //
            sn = createTextField(10, null, imeOn, dl);
            sn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    givenName.requestFocus();
                }
            });
            
            //
            // ��
            //
            givenName = createTextField(10, null, imeOn, dl);
            givenName.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    userPassword1.requestFocus();
                }
            });
            
            //
            // �d�q���[��
            //
            email = createTextField(20, null, imeOff, null);
            pattern = ClientContext.getString("addUser.pattern.email");
            RegexConstrainedDocument emailDoc = new RegexConstrainedDocument(pattern);
            email.setDocument(emailDoc);
            email.getDocument().addDocumentListener(dl);
            
            //
            // ��Î��i
            //
            licenses = ClientContext.getLicenseModel();
            licenseCombo = new JComboBox(licenses);
            boolean readOnly = Project.isReadOnly();
            licenseCombo.setEnabled(!readOnly);
            
            //
            // �f�É�
            //
            depts = ClientContext.getDepartmentModel();
            deptCombo = new JComboBox(depts);
            deptCombo.setEnabled(true);
            
            //
            // OK Btn
            //
            ActionListener al = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    changePassword();
                }
            };
            
            okButton = new JButton(UPDATE_BTN_TEXT);
            okButton.addActionListener(al);
            //okButton.setMnemonic(KeyEvent.VK_U);
            okButton.setEnabled(false);
            
            //
            // Cancel Btn
            //
            cancelButton = new JButton(CLOSE_BTN_TEXT);
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    stop();
                }
            });
            //cancelButton.setMnemonic(KeyEvent.VK_C);
            
            // ���C�A�E�g
            JPanel content = new JPanel(new GridBagLayout());
            
            int x = 0;
            int y = 0;
            JLabel label = new JLabel(USER_ID_TEXT, SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, uid, x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            
            x = 0;
            y += 1;
            label = new JLabel(PASSWORD_TEXT, SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, userPassword1, x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            label = new JLabel(CONFIRM_TEXT, SwingConstants.RIGHT);
            constrain(content, label, x + 2, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, userPassword2, x + 3, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            
            x = 0;
            y += 1;
            label = new JLabel(SIR_NAME_TEXT, SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, sn, x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            label = new JLabel(GIVEN_NAME_TEXT, SwingConstants.RIGHT);
            constrain(content, label, x + 2, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, givenName, x + 3, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            
            x = 0;
            y += 1;
            label = new JLabel(EMAIL_TEXT, SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, email, x + 1, y, 2, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            
            
            x = 0;
            y += 1;
            label = new JLabel(LISENCE_TEXT, SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, licenseCombo, x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            label = new JLabel(DEPT_TEXT, SwingConstants.RIGHT);
            constrain(content, label, x + 2, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, deptCombo, x + 3, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            
            x = 0;
            y += 1;
            label = new JLabel(" ", SwingConstants.RIGHT);
            constrain(content, label, x, y, 4, 1, GridBagConstraints.BOTH, GridBagConstraints.EAST);
            
            x = 0;
            y += 1;
            label = new JLabel(PASSWORD_ASSIST_1 + passwordLength[0] + PASSWORD_ASSIST_2
                    + passwordLength[1] + PASSWORD_ASSIST_3);
            constrain(content, label, x, y, 4, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);
            
            JPanel btnPanel = null;
            if (isMac()) {
                btnPanel = GUIFactory.createCommandButtonPanel(new JButton[]{cancelButton, okButton});
            } else {
                btnPanel = GUIFactory.createCommandButtonPanel(new JButton[]{okButton, cancelButton});
            }
            
            this.setLayout(new BorderLayout(0, 17));
            this.add(content, BorderLayout.CENTER);
            this.add(btnPanel, BorderLayout.SOUTH);
            
            this.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        }
        
        /**
         * GUI �֒l��ݒ肷��B
         */
        public void get() {
            
            //
            // UserModel �� Project ����ݒ肷��
            //
            UserModel user = Project.getUserModel();
            uid.setText(user.idAsLocal());
            sn.setText(user.getSirName());
            givenName.setText(user.getGivenName());
            userPassword1.setText(DUMMY_PASSWORD);
            userPassword2.setText(DUMMY_PASSWORD);
            email.setText(user.getEmail());
            String license = user.getLicenseModel().getLicense();
            for (int i = 0; i < licenses.length; i++) {
                if (license.equals(licenses[i].getLicense())) {
                    licenseCombo.setSelectedIndex(i);
                    break;
                }
            }
            String deptStr = user.getDepartmentModel().getDepartment();
            for (int i = 0; i < depts.length; i++) {
                if (deptStr.equals(depts[i].getDepartment())) {
                    deptCombo.setSelectedIndex(i);
                    break;
                }
            }
            
            checkButton();
        }
        
        /**
         * �p�X���[�h��ύX����B
         */
        private void changePassword() {
            
            // �L���ȃp�X���[�h�łȂ���΃��^�[������
            if (!passwordOk()) {
                return;
            }
            
            //
            // Project ���烆�[�U���f�����擾����
            //
            UserModel user = Project.getUserModel();
            
            //
            // �X�V����������܂ł͕ύX���Ȃ�
            //
            final UserModel updateModel = new UserModel();
            updateModel.setId(user.getId());
            updateModel.setFacilityModel(user.getFacilityModel());
            updateModel.setMemberType(user.getFacilityModel().getMemberType());
            //updateModel.setMemberType(user.getMemberType());
            
            //
            // ���O�C��ID��ݒ肷��
            //
            String userId = user.getFacilityModel().getFacilityId() + ":" + uid.getText().trim();
            updateModel.setUserId(userId);
            
            //
            // �p�X���[�h��ݒ肷��
            //
            final String password = new String(userPassword1.getPassword());
            
            if (!password.equals(DUMMY_PASSWORD)) {
            
                // Password �� hash �����s��
                String Algorithm = ClientContext.getString("addUser.password.hash.algorithm");
                String encoding = ClientContext.getString("addUser.password.hash.encoding");
                //String charset = ClientContext.getString("addUser.password.hash.charset");
                String charset = null;
                String hashPass = Util.createPasswordHash(Algorithm, encoding, charset, userId, password);
                updateModel.setPassword(hashPass);
                
            } else {
                //
                // �p�X���[�h�͕ύX����Ă��Ȃ�
                //
                updateModel.setPassword(user.getPassword());
            }
            
            //
            // ������ݒ肷��
            //
            String snSt = sn.getText().trim();
            updateModel.setSirName(snSt);
            String givenNameSt = givenName.getText().trim();
            updateModel.setGivenName(givenNameSt);
            updateModel.setCommonName(snSt + " " + givenNameSt);
            
            //
            // �d�q���[����ݒ肷��
            //
            updateModel.setEmail(email.getText().trim());
            
            //
            // ��Î��i��ݒ肷��
            //
            int selected = licenseCombo.getSelectedIndex();
            updateModel.setLicenseModel(licenses[selected]);
            
            //
            // �f�ÉȂ�ݒ肷��
            //
            selected = deptCombo.getSelectedIndex();
            updateModel.setDepartmentModel(depts[selected]);
            
            //
            // Role��t��������
            //
            Collection<RoleModel> roles = user.getRoles();
            for (RoleModel role : roles) {
                role.setUserId(user.getUserId());
                RoleModel updateRole = new RoleModel();
                updateRole.setId(role.getId());
                updateRole.setRole(role.getRole());
                updateRole.setUser(updateModel);
                updateRole.setUserId(updateModel.getUserId());
                updateModel.addRole(updateRole);
            }
            
            // �^�X�N�����s����
            final UserDelegater udl = new UserDelegater();
            int maxEstimation = ClientContext.getInt("task.default.maxEstimation");
            int delay = ClientContext.getInt("task.default.delay");
            
            Task task = new Task<Boolean, Void>(app) {
        
                @Override
                protected Boolean doInBackground() throws Exception {
                    logger.debug("ChangePassword doInBackground");
                    int cnt = udl.updateUser(updateModel);
                    return cnt > 0 ? new Boolean(true) : new Boolean(false);
                }
                
                @Override
                protected void succeeded(Boolean result) {
                    logger.debug("ChangePassword succeeded");
                    if (udl.isNoError()) {
                        //
                        // Project ���X�V����
                        //
                        Project.getProjectStub().setUserModel(updateModel);
                        DolphinPrincipal principal = new DolphinPrincipal();
                        principal.setUserId(updateModel.idAsLocal());
                        principal.setFacilityId(updateModel.getFacilityModel().getFacilityId());
                        Project.getProjectStub().setUserId(updateModel.idAsLocal());
                        Project.getProjectStub().setDolphinPrincipal(principal);
                        
                        JOptionPane.showMessageDialog(getFrame(),
                                SUCCESS_MESSAGE,
                                ClientContext.getFrameTitle(getName()),
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(getFrame(),
                                udl.getErrorMessage(),
                                ClientContext.getFrameTitle(getName()),
                                JOptionPane.WARNING_MESSAGE);
                    }
                }

                @Override
                protected void cancelled() {
                    logger.debug("ChangePassword cancelled");
                }

                @Override
                protected void failed(java.lang.Throwable cause) {
                    logger.warn("ChangePassword failed");
                    logger.warn(cause.getCause());
                    logger.warn(cause.getMessage());
                }

                @Override
                protected void interrupted(java.lang.InterruptedException e) {
                    logger.warn("ChangePassword interrupted");
                    logger.warn(e.getMessage());
                }
            };
            
            TaskMonitor taskMonitor = appCtx.getTaskMonitor();
            String message = null;
            Component c = getFrame();
            TaskTimerMonitor w = new TaskTimerMonitor(task, taskMonitor, c, message, PROGRESS_NOTE, delay, maxEstimation);
            //taskMonitor.addPropertyChangeListener(w);

            appCtx.getTaskService().execute(task);
            
        }
        
        private void setBusy(boolean busy) {
            if (busy) {
                okButton.setEnabled(false);
            } else {
                okButton.setEnabled(true);
            }
        }
        
        private boolean userIdOk() {
            
            String userId = uid.getText().trim();
            if (userId.equals("")) {
                return false;
            }
            
            if (userId.length() < userIdLength[0] || userId.length() > userIdLength[1]) {
                return false;
            }
            
            return true;
        }
        
        /**
         * �p�X���[�h�̗L�������`�F�b�N����B
         */
        private boolean passwordOk() {
            
            String passwd1 = new String(userPassword1.getPassword());
            String passwd2 = new String(userPassword2.getPassword());
            
            if (passwd1.equals(DUMMY_PASSWORD) && passwd2.equals(DUMMY_PASSWORD)) {
                return true;
            }
            
            if ((passwd1.length() < passwordLength[0])
            || (passwd1.length() > passwordLength[1])) {
                return false;
            }
            
            if ((passwd2.length() < passwordLength[0])
            || (passwd2.length() > passwordLength[1])) {
                return false;
            }
            
            return passwd1.equals(passwd2) ? true : false;
        }
        
        /**
         * �{�^���� enable/disable ���R���g���[������B
         */
        private void checkButton() {
            
            boolean uidOk = userIdOk();
            boolean passwordOk = passwordOk();
            boolean snOk = sn.getText().trim().equals("") ? false : true;
            boolean givenOk = givenName.getText().trim().equals("") ? false : true;
            boolean emailOk = email.getText().trim().equals("") ? false: true;
            
            boolean newOk = (uidOk && passwordOk && snOk && givenOk && emailOk) ? true : false;
            
            if (ok != newOk) {
                ok = newOk;
                okButton.setEnabled(ok);
            }
        }
    }
    
    /**
     * TextField �𐶐�����B
     */
    private JTextField createTextField(int val, Insets margin, FocusAdapter fa, DocumentListener dl) {
        
        if (val == 0) {
            val = 30;
        }
        JTextField tf = new JTextField(val);
        
        if (margin == null) {
            margin = new Insets(1, 2, 1, 2);
        }
        tf.setMargin(margin);
        
        if (dl != null) {
            tf.getDocument().addDocumentListener(dl);
        }
        
        if (fa != null) {
            tf.addFocusListener(fa);
        }
        
        return tf;
    }
    
    /**
     * �p�X���[�h�t�B�[���h�𐶐�����B
     */
    private JPasswordField createPassField(int val, Insets margin, FocusAdapter fa, DocumentListener dl) {
        
        if (val == 0) {
            val = 30;
        }
        JPasswordField tf = new JPasswordField(val);
        
        if (margin == null) {
            margin = new Insets(1, 2, 1, 2);
        }
        tf.setMargin(margin);
        
        if (dl != null) {
            tf.getDocument().addDocumentListener(dl);
        }
        
        if (fa != null) {
            tf.addFocusListener(fa);
        }
        
        return tf;
    }
    
    /**
     * GridBagLayout ���g�p���ăR���|�[�l���g��z�u����B
     */
    private void constrain(JPanel container, Component cmp, int x, int y,
            int width, int height, int fill, int anchor) {
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = width;
        c.gridheight = height;
        c.fill = fill;
        c.anchor = anchor;
        c.insets = new Insets(0, 0, 5, 7);
        ((GridBagLayout) container.getLayout()).setConstraints(cmp, c);
        container.add(cmp);
    }
    
    /**
     * OS��mac���ǂ�����Ԃ��B
     * @return mac �̎� true
     */
    private boolean isMac() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.startsWith("mac") ? true : false;
    }
}
