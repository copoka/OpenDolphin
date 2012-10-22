/*
 * LoginDialog.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2003-2005 Digital Globe, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package open.dolphin.client;

import javax.swing.*;
import open.dolphin.infomodel.ModelUtils;

import org.apache.log4j.Logger;

import open.dolphin.delegater.UserDelegater;
import open.dolphin.infomodel.UserModel;
import open.dolphin.project.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * ���O�C���_�C�A���O�@�N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class LoginDialog {
    
    /** Login Status */
    public enum LoginStatus {AUTHENTICATED, NOT_AUTHENTICATED, CANCELD};
    
    //
    // GUI Components
    //
    private JDialog dialog;
    private JTextField userIdField;
    private JPasswordField passwdField;
    private JButton settingButton;
    private JButton loginButton;
    private JButton cancelButton;
    private UltraSonicProgressLabel glassPane;
    
    //
    // �F�ؐ���p
    //
    private BlockGlass glass;
    private UserDelegater userDlg;
    private Logger part11Logger;
    private int tryCount;
    private int maxTryCount;
    private javax.swing.Timer taskTimer;
    private LoginTask task;
    
    //
    // �F�،��ʂ̃v���p�e�B
    //
    private LoginStatus result;
    private PropertyChangeSupport boundSupport;
    
    //
    // �_�C�A���O���f��
    //
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
        // EDT ����\������
        //
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                dialog.pack();
                int width = dialog.getWidth();
                int height = dialog.getHeight();
                Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                int n = ClientContext.isMac() ? 3 : 2;
                int left = (screen.width - width) / 2;
                int top = (screen.height - height) / n;
                dialog.setLocation(left, top);
                dialog.setVisible(true);
            }
        });
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
    private void notifyResult(LoginStatus ret) {
        boundSupport.firePropertyChange("LOGIN_PROP", -100, ret);
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
        String passwd = new String(passwdField.getPassword());
        
        // LoginTask �𐶐�����
        int maxEstimation = ClientContext.getInt("task.default.maxEstimation");
        int delay = ClientContext.getInt("task.default.delay");
        int lengthOfTask = maxEstimation / delay;	// �^�X�N�̒��� = �ő�\�z���� / ���荞�݊Ԋu
        
        task = new LoginTask(principal, passwd, userDlg, lengthOfTask);
        
        // TaskTimer �𐶐�����
        taskTimer = new javax.swing.Timer(delay, ProxyActionListener.create(this, "onTimerAction"));
        
        // �X�^�|�g��������
        setBusy(true);
        task.start();
    }
    
    /**
     * �^�X�N�^�C�}�[�̊��荞�݂ŔF�،��ʂ���������B
     */
    public void onTimerAction() {
        
        if (task.isDone()) {
            
            setBusy(false);
            
            //
            // �F�،��ʂł��� userModel ���擾����
            //
            UserModel userModel = task.getUserModel();
            
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
                            String title = dialog.getTitle();
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
                //
                // �F�؂����s�����ꍇ
                //
                if (tryCount <= maxTryCount) {
                    String msg = userDlg.getErrorMessage();
                    part11Logger.warn(msg);
                    showMessageDialog(msg);
                    
                } else {
                    //
                    // �K��񐔈ȏ㎸�s
                    //
                    StringBuilder sb = new StringBuilder();
                    sb.append(userDlg.getErrorMessage());
                    sb.append("\n");
                    sb.append(ClientContext.getString("loginDialog.forceClose"));
                    String msg = sb.toString();
                    part11Logger.warn(msg);
                    showMessageDialog(msg);
                    result = LoginStatus.NOT_AUTHENTICATED;
                    notifyClose(result);
                }
            }
            
        } else if (task.isTimeOver()) {
            //
            // �^�C���I�[�o�[�������s��
            //
            setBusy(false);
            new TimeoutWarning(dialog, dialog.getTitle(), null).start();
        }
    }
    
    /**
     * �f�[�^�x�[�X�A�N�Z�X���̏������s���B
     */
    private void setBusy(boolean busy) {
        
        if (busy) {
            userIdField.setEnabled(false);
            passwdField.setEnabled(false);
            settingButton.setEnabled(false);
            loginButton.setEnabled(false);
            cancelButton.setEnabled(false);
            glassPane.start();
            taskTimer.start();
            
        } else {
            glassPane.stop();
            taskTimer.stop();
            userIdField.setEnabled(true);
            passwdField.setEnabled(true);
            settingButton.setEnabled(true);
            loginButton.setEnabled(true);
            cancelButton.setEnabled(true);
        }
    }
    
    /**
     * �x�����b�Z�[�W��\������B
     * @param msg �\�����郁�b�Z�[�W
     */
    private void showMessageDialog(String msg) {
        String title = dialog.getTitle();
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * ���O�C���_�C�A���O���I������B
     * @param result
     */
    private void notifyClose(LoginStatus result) {
        dialog.setVisible(false);
        dialog.dispose();
        notifyResult(result);
    }
    
    /**
     * GUI ���\�z����B
     */
    private void initComponents() {
        
        // Image ���x���𐶐�����
        JLabel imageLabel = new JLabel(ClientContext.getImageIcon("splash.jpg"));
        
        // ���[�UID�t�B�[���h�𐶐�����
        userIdField = GUIFactory.createTextField(10, null, null, null);
        
        // �p�X���[�h�t�B�[���h�𐶐�����
        passwdField = GUIFactory.createPassField(10, null, null, null);
        
        // �ݒ�{�^���𐶐�����
        String text = ClientContext.getString("loginDialog.settingButtonText");
        settingButton = new JButton(text);
        
        // Cancel�{�^���𐶐�����
        text =  (String)UIManager.get("OptionPane.cancelButtonText");
        cancelButton = new JButton(text);
        
        // Login�{�^���𐶐�����
        text = ClientContext.getString("loginDialog.loginButtonText");
        loginButton = new JButton(text);
        
        //
        // ���C�A�E�g������
        //
        String loginInfoText = ClientContext.getString("loginDialog.loginBorderTitle");
        String userIdText = ClientContext.getString("loginDialog.userIdLabel");
        String passwrdText = ClientContext.getString("loginDialog.passwdLabel");
        GridBagBuilder gbl = new GridBagBuilder(loginInfoText);
        
        int row = 0;
        JLabel label = new JLabel(userIdText, SwingConstants.RIGHT);
        gbl.add(label,          0, row, GridBagConstraints.EAST);
        gbl.add(userIdField,    1, row, GridBagConstraints.WEST);
        
        row++;
        label = new JLabel(passwrdText, SwingConstants.RIGHT);
        gbl.add(label, 		0, row, GridBagConstraints.EAST);
        gbl.add(passwdField,    1, row, GridBagConstraints.WEST);
        
        row++;
        glassPane = new UltraSonicProgressLabel();
        int width = userIdField.getPreferredSize().width;
        int height = glassPane.getPreferredSize().height;
        glassPane.setPreferredSize(new Dimension(width, height));
        gbl.add(new JLabel(""), 0, row, 1, 1, GridBagConstraints.CENTER);
        gbl.add(glassPane, 	1, row, 1, 1, GridBagConstraints.CENTER);
        
        // ID Panel�𓾂�
        JPanel idPanel = gbl.getProduct();
        
        // �{�^���p�l���𐶐�����
        JPanel buttonPanel = ClientContext.isMac()
        ? GUIFactory.createCommandButtonPanel(new JButton[]{settingButton,cancelButton,loginButton})
        : GUIFactory.createCommandButtonPanel(new JButton[]{settingButton,loginButton,cancelButton});
        
        // �E���p�l���𐶐�����
        JPanel rightPanel = new JPanel(new BorderLayout(0, 17));
        rightPanel.add(idPanel, BorderLayout.CENTER);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        //
        // �R���e���g�p�l���𐶐�����
        //
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
        content.add(imageLabel);
        content.add(Box.createHorizontalStrut(11));
        content.add(rightPanel);
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        
        //
        // �_�C�A���O�𐶐�����
        //
        // ���\�[�X����Window�^�C�g�����擾����
        String title = ClientContext.getString("loginDialog.title");
        String windowTitle = ClientContext.getFrameTitle(title);
        
        // Login Dialog �𐶐��� GlassPane���Z�b�g����
        dialog = new JDialog((Frame) null, windowTitle, false);
        glass = new BlockGlass();
        dialog.setGlassPane(glass);
        
        //
        // �R���e���c��������
        //
        content.setOpaque(true);
        dialog.setContentPane(content);
        dialog.getRootPane().setDefaultButton(loginButton);
        
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
        userIdField.getDocument().addDocumentListener(ProxyDocumentListener.create(stateMgr, "checkButtons"));
        userIdField.addFocusListener(AutoRomanListener.getInstance());
        userIdField.addActionListener(ProxyActionListener.create(stateMgr, "onUserIdAction"));
        
        passwdField.getDocument().addDocumentListener(ProxyDocumentListener.create(stateMgr, "checkButtons"));
        passwdField.addFocusListener(AutoRomanListener.getInstance());
        passwdField.addActionListener(ProxyActionListener.create(stateMgr, "onPasswordAction"));
        
        //
        // �{�^���� ActionListener ��o�^����
        //
        settingButton.addActionListener(ProxyActionListener.create(this, "doSettingDialog"));
        cancelButton.addActionListener(ProxyActionListener.create(this, "doCancel"));
        loginButton.addActionListener(ProxyActionListener.create(this, "tryLogin"));
        loginButton.setEnabled(false);
        
        //
        // �_�C�A���O�� WindowAdapter ��ݒ肷��
        //
        dialog.addWindowListener(stateMgr);
    }
    
    /**
     * ���f����\������B
     */
    private void bindModelToView() {
        
        if (principal.getUserId() != null && (!principal.getUserId().equals(""))) {
            userIdField.setText(principal.getUserId());
        }
    }
    
    /**
     * ���f���l���擾����B
     */
    private void bindViewToModel() {
        
        if (!userIdField.getText().trim().equals("")) {
            principal.setUserId(userIdField.getText().trim());
        }
    }
    
    
    /**
     * �ݒ�{�^���������ꂽ���A�ݒ��ʂ��J�n����B
     */
    public void doSettingDialog() {
        
        glass.block();
        
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
        
        glass.unblock();
        
        boolean valid = newValue.booleanValue();
        if (valid) {
            principal.setUserId(Project.getUserId());
            principal.setFacilityId(Project.getFacilityId());
            bindModelToView();
            passwdField.requestFocus();
        }
    }
    
    /**
     * ���O�C�����L�����Z������B
     */
    public void doCancel() {
        dialog.setVisible(false);
        dialog.dispose();
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
            
            boolean userEmpty = userIdField.getText().equals("") ? true : false;
            boolean passwdEmpty = passwdField.getPassword().length == 0 ? true : false;
            
            boolean newOKState = ( (userEmpty == false) && (passwdEmpty == false) ) ? true : false;
            
            if (newOKState != okState) {
                loginButton.setEnabled(newOKState);
                okState = newOKState;
            }
        }
        
        /**
         * UserId �t�B�[���h�Ń��^�[�����[�������ꂽ���̏������s���B
         */
        public void onUserIdAction() {
            passwdField.requestFocus();
        }
        
        /**
         * Password �t�B�[���h�Ń��^�[�����[�������ꂽ���̏������s���B
         */
        public void onPasswordAction() {
            
            if (userIdField.getText().equals("")) {
                
                userIdField.requestFocus();
                
            } else if (passwdField.getPassword().length != 0 && okState) {
                //
                // ���O�C���{�^�����N���b�N����
                //
                loginButton.doClick();
            }
        }
        
        @Override
        public void windowClosing(WindowEvent e) {
            doCancel();
        }
        
        @Override
        public void windowOpened(WindowEvent e) {
            
            if (!userIdField.getText().trim().equals("")) {
                //
                // UserId �ɗL���Ȓl���ݒ肳��Ă����
                // �p�X���[�h�t�B�[���h�Ƀt�H�[�J�X����
                //
                passwdField.requestFocus();
            }
        }
    }
}













