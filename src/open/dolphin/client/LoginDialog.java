package open.dolphin.client;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import open.dolphin.infomodel.ModelUtils;

import org.apache.log4j.Logger;

import open.dolphin.delegater.UserDelegater;
import open.dolphin.infomodel.UserModel;
import open.dolphin.project.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;

/**
 * ���O�C���_�C�A���O�@�N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class LoginDialog {
    
    /** Login Status */
    public enum LoginStatus {AUTHENTICATED, NOT_AUTHENTICATED, CANCELD};
    
    private LoginView view;
    private BlockGlass blockGlass;
    
    // �F�ؐ���p
    private UserDelegater userDlg;
    private Logger part11Logger;
    private int tryCount;
    private int maxTryCount;
    
    // �F�،��ʂ̃v���p�e�B
    private LoginStatus result;
    private PropertyChangeSupport boundSupport;
    
    // ���f��
    private DolphinPrincipal principal;
    
    // StateMgr
    private StateMgr stateMgr;
    
    
    /**
     * Creates new LoginService
     */
    public LoginDialog() {
    }
    
    /**
     * �F�،��ʃv���p�e�B���X�i��o�^����B
     * @param listener �o�^����F�،��ʃ��X�i
     */
    public void addPropertyChangeListener(String prop, PropertyChangeListener listener) {
        if (boundSupport == null) {
            boundSupport = new PropertyChangeSupport(this);
        }
        boundSupport.addPropertyChangeListener(prop, listener);
    }
    
    /**
     * �F�،��ʃv���p�e�B���X�i��o�^����B
     * @param listener �폜����F�،��ʃ��X�i
     */
    public void removePropertyChangeListener(String prop, PropertyChangeListener listener) {
        if (boundSupport == null) {
            boundSupport = new PropertyChangeSupport(this);
        }
        boundSupport.addPropertyChangeListener(prop, listener);
    }
    
    /**
     * ���O�C����ʂ��J�n����B
     */
    public void start() {
        
        //
        // �_�C�A���O���f���𐶐����l������������
        //
        principal = new DolphinPrincipal();
        if (Project.isValid()) {
            principal.setFacilityId(Project.getFacilityId());
            principal.setUserId(Project.getUserId());
        }
        
        //
        // GUI ���\�z�����f����\������
        //
        initComponents();
        bindModelToView();
        
        //
        // EDT ����R�[������Ă���
        //
        int width = view.getWidth();
        int height = view.getHeight();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int n = ClientContext.isMac() ? 3 : 2;
        int left = (screen.width - width) / 2;
        int top = (screen.height - height) / n;
        view.setLocation(left, top);
        view.setVisible(true);
    }
    
    /**
     * �F�؂������������ǂ�����Ԃ��B
     * @return true �F�؂����������ꍇ
     */
    public LoginStatus getResult() {
        return result;
    }
    
    /**
     * PropertyChange �Ō��ʂ��󂯎��A�v���ɒʒm����B
     * @param result true �F�؂����������ꍇ
     */
    private void notifyResult(final LoginStatus ret) {
         SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               boundSupport.firePropertyChange("LOGIN_PROP", -100, ret);
            }
        });
    }
    
    /**
     * �F�؂����݂�B
     * JBoss �� DatabaseLoginModule ���g�p���Ă��邽�߁AUserValue���擾�ł����ꍇ�ɔF�؂����������Ƃ݂Ȃ��B
     * �ڍׂ�Business Delegater �ֈϏ��B
     */
    public void tryLogin() {
        
        // User �����擾���邽�߂̃f���Q�[�^�𓾂�
        if (userDlg == null) {
            userDlg = new UserDelegater();
        }
        
        // Part11 ���K�[���擾����
        if (part11Logger == null) {
            part11Logger = ClientContext.getLogger("part11");
        }
        
        // �g���C�o����ő�񐔂𓾂�
        if (maxTryCount == 0) {
            maxTryCount = ClientContext.getInt("loginDialog.maxTryCount");
        }
        
        part11Logger.info("�F�؂��J�n���܂�");
        
        // ���s�� += 1
        tryCount++;
        
        // userId��password���擾����
        bindViewToModel();
        final String password = new String(view.getPasswordField().getPassword());
        
        // LoginTask �𐶐�����
        int maxEstimation = ClientContext.getInt("task.default.maxEstimation");
        int delay = ClientContext.getInt("task.default.delay");
        int lengthOfTask = maxEstimation / delay;	// �^�X�N�̒��� = �ő�\�z���� / ���荞�݊Ԋu
        
        ApplicationContext appCtx = ClientContext.getApplicationContext();
        Application app = appCtx.getApplication();
        
        Task task = new Task<UserModel, Void>(app) {

            @Override
            protected UserModel doInBackground() throws Exception {
                UserModel userModel = userDlg.login(principal, password);
                return userModel;
            }
            
            @Override
            protected void succeeded(UserModel userModel) {
                part11Logger.debug("Task succeeded");
                if (userModel != null) {
                    //
                    // Member �̗L�����Ԃ��`�F�b�N����
                    //
                    Project.UserType userType = Project.UserType.valueOf(userModel.getMemberType());
                    part11Logger.info("User Type = " + userType.toString());

                    if (userType.equals(Project.UserType.ASP_TESTER)) {

                        // �o�^�����擾����
                        Date registered = userModel.getRegisteredDate();

                        // �e�X�g���Ԃ��擾���� �P�ʂ͌���
                        int testPeriod = ClientContext.getInt("loginDialog.asp.testPeriod");

                        // �o�^���Ƀe�X�g���Ԃ�������
                        GregorianCalendar gc = new GregorianCalendar();
                        gc.setTime(registered);
                        gc.add(Calendar.MONTH, testPeriod);

                        // ���O
                        part11Logger.info("�o�^��: " + ModelUtils.getDateAsString(registered));
                        part11Logger.info("�L������: " + ModelUtils.getDateAsString(gc.getTime()));

                        // �����̂��擾����
                        GregorianCalendar today = new GregorianCalendar();

                        // gc�������ȑO�̎��͗L�������؂�
                        if (gc.before(today)) {
                            String evalOut = ClientContext.getString("loginDialog.asp.evalout.msg");
                            part11Logger.warn(evalOut);
                            showMessageDialog(evalOut);
                            result = LoginStatus.NOT_AUTHENTICATED;
                            notifyClose(result);
                            return;

                        } else {
                            // �c��̓������v�Z����
                            // 7���ȓ��̎����b�Z�[�W��\������
                            int days = 0;
                            int warningDays = ClientContext.getInt("loginDialog.asp.warning.days");
                            while (today.before(gc)) {
                                days++;
                                if (days > warningDays) {
                                    break;
                                }
                                today.add(Calendar.DAY_OF_MONTH, 1);
                            }

                            //if (days <= warningDays)  {
                            if (days <= warningDays) {
                                //if (days <= warningDays && ((days % 2) != 0) )  {
                                part11Logger.info("�c�� " + days + " ��");
                                String title = view.getTitle();
                                String msg1 = "�]�����Ԃ͎c�� " + days + " ���ł��B";
                                String msg2 = "�p�����Ă��g�p�̏ꍇ�̓T�|�[�g���C�Z���X�̂��w�������肢���܂��B";
                                Object obj = new String[]{msg1, msg2};
                                JOptionPane.showMessageDialog(null, obj, title, JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }

                    // �F�ؐ���
                    String time = ModelUtils.getDateTimeAsString(new Date());
                    part11Logger.info(time + ": " + userModel.getUserId() + " �����O�C�����܂���");

                    // ���[�UID�A�{��ID�A���[�U���f���� rojectStub �֕ۑ�����
                    Project.getProjectStub().setUserId(principal.getUserId());
                    Project.getProjectStub().setUserModel(userModel);
                    Project.getProjectStub().setDolphinPrincipal(principal);

                    result = LoginStatus.AUTHENTICATED;
                    notifyClose(result);
                     
                } else {
                    part11Logger.warn("User == null, this never ocuured");
                }
            }
            
            @Override
            protected void cancelled() {
                part11Logger.debug("Task cancelled");
            }
            
            @Override
            protected void failed(java.lang.Throwable cause) {
                part11Logger.warn("Task failed");
                part11Logger.warn(cause.getCause());
                part11Logger.warn(cause.getMessage());
                if (tryCount <= maxTryCount && cause instanceof Exception) {
                    userDlg.processError((Exception) cause);
                    String errMsg = userDlg.getErrorMessage();
                    showMessageDialog(errMsg);
                } else {
                    StringBuilder sb = new StringBuilder();
                    userDlg.processError((Exception) cause);
                    sb.append(userDlg.getErrorMessage());
                    sb.append("\n");
                    sb.append(ClientContext.getString("loginDialog.forceClose"));
                    String msg = sb.toString();
                    showMessageDialog(msg);
                    result = LoginStatus.NOT_AUTHENTICATED;
                    notifyClose(result);
                }
            }
            
            @Override
            protected void interrupted(java.lang.InterruptedException e) {
                part11Logger.warn("Task interrupted");
                part11Logger.warn(e.getMessage());
            }
        };
        
        final TaskMonitor taskMonitor = appCtx.getTaskMonitor();
        taskMonitor.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent e) {
                
                String propertyName = e.getPropertyName();
                part11Logger.info("propertyName = " + propertyName);

                if ("started".equals(propertyName)) {
                    //part11Logger.info("login task started");
                    setBusy(true);

                } else if ("done".equals(propertyName)) {
                    //part11Logger.info("login task done");
                    setBusy(false);
                    taskMonitor.removePropertyChangeListener(this);
                }
            }  
        });
        
        appCtx.getTaskService().execute(task);
        
    }
    
    /**
     * �f�[�^�x�[�X�A�N�Z�X���̏������s���B
     */
    private void setBusy(boolean busy) {
        
        if (busy) {
            blockGlass.block();
            view.getUserIdField().setEnabled(false);
            view.getPasswordField().setEnabled(false);
            view.getSettingBtn().setEnabled(false);
            view.getLoginBtn().setEnabled(false);
            view.getCancelBtn().setEnabled(false);
            view.getProgressBar().setIndeterminate(true);
            
        } else {
            view.getProgressBar().setIndeterminate(true);
            view.getProgressBar().setValue(0);
            view.getUserIdField().setEnabled(true);
            view.getPasswordField().setEnabled(true);
            view.getSettingBtn().setEnabled(true);
            view.getLoginBtn().setEnabled(true);
            view.getCancelBtn().setEnabled(true);
            blockGlass.unblock();
        }
    }
    
    /**
     * �x�����b�Z�[�W��\������B
     * @param msg �\�����郁�b�Z�[�W
     */
    private void showMessageDialog(String msg) {
        String title = view.getTitle();
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * ���O�C���_�C�A���O���I������B
     * @param result
     */
    private void notifyClose(LoginStatus result) {
        view.setVisible(false);
        view.dispose();
        notifyResult(result);
    }
    
    /**
     * GUI ���\�z����B
     */
    private void initComponents() {
        
        String title = ClientContext.getString("loginDialog.title");
        String windowTitle = ClientContext.getFrameTitle(title);
        
        view = new LoginView((Frame) null, false);
        view.setTitle(windowTitle);
        view.getRootPane().setDefaultButton(view.getLoginBtn());
        blockGlass = new BlockGlass();
        view.setGlassPane(blockGlass);
        //
        // �C�x���g�ڑ����s��
        //
        connect();
    }
    
    /**
     * �C�x���g�ڑ����s���B
     */
    private void connect() {
        
        //
        // Mediator ���C�N�� StateMgr
        //
        stateMgr = new StateMgr();
        
        //
        // �t�B�[���h�Ƀ��X�i��o�^����
        //
        JTextField userIdField = view.getUserIdField();
        userIdField.getDocument().addDocumentListener(ProxyDocumentListener.create(stateMgr, "checkButtons"));
        userIdField.addFocusListener(AutoRomanListener.getInstance());
        userIdField.addActionListener(ProxyActionListener.create(stateMgr, "onUserIdAction"));
        
        JPasswordField passwdField = view.getPasswordField();
        passwdField.getDocument().addDocumentListener(ProxyDocumentListener.create(stateMgr, "checkButtons"));
        passwdField.addFocusListener(AutoRomanListener.getInstance());
        passwdField.addActionListener(ProxyActionListener.create(stateMgr, "onPasswordAction"));
        
        //
        // �{�^���� ActionListener ��o�^����
        //
        view.getSettingBtn().addActionListener(ProxyActionListener.create(this, "doSettingDialog"));
        view.getCancelBtn().addActionListener(ProxyActionListener.create(this, "doCancel"));
        view.getLoginBtn().addActionListener(ProxyActionListener.create(this, "tryLogin"));
        view.getLoginBtn().setEnabled(false);
        
        //
        // �_�C�A���O�� WindowAdapter ��ݒ肷��
        //
        view.addWindowListener(stateMgr);
    }
    
    /**
     * ���f����\������B
     */
    private void bindModelToView() {
        
        if (principal.getUserId() != null && (!principal.getUserId().equals(""))) {
            view.getUserIdField().setText(principal.getUserId());
        }
    }
    
    /**
     * ���f���l���擾����B
     */
    private void bindViewToModel() {
        
        String id = view.getUserIdField().getText().trim();
        
        if (!id.equals("")) {
            principal.setUserId(id);
        }
    }
    
    
    /**
     * �ݒ�{�^���������ꂽ���A�ݒ��ʂ��J�n����B
     */
    public void doSettingDialog() {
        
        blockGlass.block();
        
        ProjectSettingDialog sd = new ProjectSettingDialog();
        PropertyChangeListener pl = ProxyPropertyChangeListener.create(this, "setNewParams", new Class[]{Boolean.class});
        sd.addPropertyChangeListener("SETTING_PROP", pl);
        sd.setLoginState(false);
        sd.start();
    }
    
    /**
     * �ݒ�_�C�A���O����ʒm���󂯂�B
     * �L���ȃv���W�F�N�g�ł�΃��[�UID���t�B�[���h�ɐݒ肵�p�X���[�h�t�B�[���h�Ƀt�H�[�J�X����B
     **/
    public void setNewParams(Boolean newValue) {
        
        blockGlass.unblock();
        
        boolean valid = newValue.booleanValue();
        if (valid) {
            principal.setUserId(Project.getUserId());
            principal.setFacilityId(Project.getFacilityId());
            bindModelToView();
            view.getPasswordField().requestFocus();
        }
    }
    
    /**
     * ���O�C�����L�����Z������B
     */
    public void doCancel() {
        view.setVisible(false);
        view.dispose();
        result = LoginStatus.CANCELD;
        notifyResult(result);
    }
    
    /**
     * ���O�C���{�^���𐧌䂷��Ȉ� StateMgr �N���X�B
     */
    class StateMgr extends WindowAdapter {
        
        private boolean okState;
        
        public StateMgr() {
        }
        
        /**
         * ���O�C���{�^���� enable/disable �𐧌䂷��B
         */
        public void checkButtons() {
            
            boolean userEmpty = view.getUserIdField().getText().equals("") ? true : false;
            boolean passwdEmpty = view.getPasswordField().getPassword().length == 0 ? true : false;
            
            boolean newOKState = ( (userEmpty == false) && (passwdEmpty == false) ) ? true : false;
            
            if (newOKState != okState) {
                view.getLoginBtn().setEnabled(newOKState);
                okState = newOKState;
            }
        }
        
        /**
         * UserId �t�B�[���h�Ń��^�[�����[�������ꂽ���̏������s���B
         */
        public void onUserIdAction() {
            view.getPasswordField().requestFocus();
        }
        
        /**
         * Password �t�B�[���h�Ń��^�[�����[�������ꂽ���̏������s���B
         */
        public void onPasswordAction() {
            
            if (view.getUserIdField().getText().equals("")) {
                
                view.getUserIdField().requestFocus();
                
            } else if (view.getPasswordField().getPassword().length != 0 && okState) {
                //
                // ���O�C���{�^�����N���b�N����
                //
                view.getLoginBtn().doClick();
            }
        }
        
        @Override
        public void windowClosing(WindowEvent e) {
            doCancel();
        }
        
        @Override
        public void windowOpened(WindowEvent e) {
            
            if (!view.getUserIdField().getText().trim().equals("")) {
                //
                // UserId �ɗL���Ȓl���ݒ肳��Ă����
                // �p�X���[�h�t�B�[���h�Ƀt�H�[�J�X����
                //
                view.getPasswordField().requestFocus();
            }
        }
    }
}













