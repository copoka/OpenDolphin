/*
 * AddFacility.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2004 Digital Globe, Inc. All rights reserved.
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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import javax.swing.UIManager;

import org.jboss.security.Util;
import org.jboss.security.auth.callback.UsernamePasswordHandler;

import open.dolphin.ejb.RemoteSystemService;
import open.dolphin.infomodel.UserModel;

/**
 * ��Ë@�ւƊǗ��ӔC�҂�o�^����N���X�B
 *
 * @Author Kazushi Minagawa, Digital Globe, Inc.
 *
 */
public class AddFacilityDialog extends JDialog implements ComponentListener, Runnable {
    
    private static final long serialVersionUID = 1841335066247317770L;
    
    public static final String ACCOUNT_INFO = "accountInfo";
    
    private enum AccountState {COM_TEST, AGREEMENT, ACCOUNT_INFO};
    
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JButton okBtn;
    private JButton cancelBtn;
    private JButton nextBtn;
    private JButton backBtn;
    
    private PropertyChangeSupport boundSupport;
    private ServerInfo serverInfo;
    
    private OIDGetter oidGetter;
    private AgreementPanel agreement;
    private AccountInfoPanel accountInfo;
    
    private AccountState state = AccountState.COM_TEST;
    private boolean comTestOk;
    private boolean agreementOk;
    private boolean accountInfoOk;
    
    private javax.swing.Timer taskTimer;
    
    
    public AddFacilityDialog() {
        super((Frame)null, null, true);
        boundSupport = new PropertyChangeSupport(this);
    }
    
    public void addPropertyChangeListener(String prop, PropertyChangeListener l) {
        boundSupport.addPropertyChangeListener(prop, l);
    }
    
    public void removePropertyChangeListener(String prop, PropertyChangeListener l) {
        boundSupport.addPropertyChangeListener(prop, l);
    }
    
    public void run() {
        initialize();
        connect();
        cardLayout.show(cardPanel, "comTest");
        this.setVisible(true);
    }
    
    public void setServerInfo(ServerInfo info) {
        serverInfo = info;
        boundSupport.firePropertyChange(ACCOUNT_INFO, null, serverInfo);
    }
    
    /**
     * GUI�R���|�[�l���g������������B
     */
    private void initialize() {
        
        // ���\�[�X����l���擾����
        String windowTitleitle = ClientContext.getString("account.window.tile");
        int windowWidth = ClientContext.getInt("account.window.width");
        int windowHeight = ClientContext.getInt("account.window.height");
        String agreementRes = ClientContext.getString("account.agreement.resource");
        String agreementEnc = ClientContext.getString("account.agreement.encoding");
        
        String backBtnText = ClientContext.getString("account.backBtn.text");
        String nextBtnText = ClientContext.getString("account.nextBtn.text");
        String cancelBtnText = (String)UIManager.get("OptionPane.cancelButtonText");
        String addBtnText = ClientContext.getString("account.addBtn.text");
        
        // �ʐM�e�X�g�p�l���𐶐�����
        oidGetter = new OIDGetter();
        
        // �g�p�����p�l���𐶐�����
        AgreementModel agreeModel = new AgreementModel();
        try {
            InputStreamReader ir = new InputStreamReader(ClientContext.getResourceAsStream(agreementRes), agreementEnc);
            BufferedReader reader = new BufferedReader(ir);
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            reader.close();
            agreeModel.setAgreeText(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        agreement = new AgreementPanel(agreeModel);
        
        // �A�J�E���g���p�l���𐶐�����
        accountInfo = new AccountInfoPanel();
        
        // �J�[�h���A�C�E�g�֔z�u����
        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);
        cardLayout.addLayoutComponent(oidGetter, "comTest");
        cardLayout.addLayoutComponent(agreement, "agreement");
        cardLayout.addLayoutComponent(accountInfo, "accountInfo");
        cardPanel.add(oidGetter, "comTest");
        cardPanel.add(agreement, "agreement");
        cardPanel.add(accountInfo, "accountInfo");
        
        // �߂�{�^���𐶐�����
        backBtn = new JButton(backBtnText);
        backBtn.setEnabled(false);
        
        // �����{�^���𐶐�����
        nextBtn = new JButton(nextBtnText);
        nextBtn.setEnabled(false);
        
        // �o�^�{�^���𐶐�����
        okBtn = new JButton(addBtnText);
        okBtn.setEnabled(false);
        
        // �L�����Z���{�^���𐶐�����
        cancelBtn = new JButton(cancelBtnText);
        
        // �{�^���p�l���𐶐�����
        JPanel btnPanel = null;
        if (ClientContext.isMac()) {
            btnPanel = GUIFactory.createCommandButtonPanel(
                    new JButton[]{backBtn, nextBtn, cancelBtn, okBtn});
            
        } else {
            btnPanel = GUIFactory.createCommandButtonPanel(
                    new JButton[]{backBtn, nextBtn, okBtn, cancelBtn});
        }
        
        // �S�̂�z�u����
        JPanel content = new JPanel(new BorderLayout(0, 17));
        content.add(cardPanel, BorderLayout.CENTER);
        content.add(btnPanel,BorderLayout.SOUTH);
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        
        // �R���e���g�ɂ���
        this.getContentPane().add(content, BorderLayout.CENTER);
        
        // Window �̐ݒ���s��
        this.setTitle(ClientContext.getFrameTitle(windowTitleitle));
        this.setSize(new Dimension(windowWidth, windowHeight));
        Dimension size = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int top = (size.height - windowHeight) / 3;
        int left = (size.width - windowWidth) / 2;
        this.setLocation(left, top);
    }
    
    /**
     * �C�x���g�ڑ����s���B
     */
    private void connect() {
        
        oidGetter.addOidPropertyListener(new OidListener());
        
        agreement.addAgreePropertyListener(new AgreementListener());
        
        accountInfo.addValidInfoPropertyListener(new AccountInfoListener());
        
        backBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (state == AccountState.AGREEMENT) {
                    setState(AccountState.COM_TEST);
                    cardLayout.show(cardPanel, "comTest");
                } else if (state == AccountState.ACCOUNT_INFO) {
                    setState(AccountState.AGREEMENT);
                    cardLayout.show(cardPanel, "agreement");
                }
            }
        });
        
        nextBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (state == AccountState.COM_TEST) {
                    setState(AccountState.AGREEMENT);
                    cardLayout.show(cardPanel, "agreement");
                } else if (state == AccountState.AGREEMENT) {
                    setState(AccountState.ACCOUNT_INFO);
                    cardLayout.show(cardPanel, "accountInfo");
                }
            }
        });
        
        okBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addFacilityAdmin();
            }
        });
        
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        
        this.addComponentListener(this);
    }
    
    class OidListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            String oid = (String)e.getNewValue();
            comTestOk = (oid != null && (!oid.equals("")) ) ? true : false;
            controlButton();
        }
    }
    
    class AgreementListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            boolean agree = ((Boolean)e.getNewValue()).booleanValue();
            agreementOk = agree;
            controlButton();
        }
    }
    
    class AccountInfoListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            boolean account = ((Boolean)e.getNewValue()).booleanValue();
            accountInfoOk = account;
            controlButton();
        }
    }
    
    private void setState(AccountState s) {
        state = s;
        controlButton();
    }
    
    private void controlButton() {
        
        backBtn.setEnabled(false);
        nextBtn.setEnabled(false);
        okBtn.setEnabled(false);
        
        switch (state) {
            
            case COM_TEST:
                nextBtn.setEnabled(comTestOk);
                break;
                
            case AGREEMENT:
                backBtn.setEnabled(true);
                nextBtn.setEnabled(agreementOk);
                break;
                
            case ACCOUNT_INFO:
                backBtn.setEnabled(true);
                okBtn.setEnabled(accountInfoOk);
                break;
        }
    }
    
    private void close() {
        setVisible(false);
        dispose();
    }
    
    /**
     * �{�݋y�ъǗ��҃A�J�E���g��o�^����B
     */
    private void addFacilityAdmin() {
        
        // �o�^���郆�[�U���f���𓾂�
        final UserModel model = accountInfo.getModel();
        
        // Password �� hash�����s��
        String userId = model.getUserId();
        String Algorithm = ClientContext.getString("addUser.password.hash.algorithm");
        String encoding = ClientContext.getString("addUser.password.hash.encoding");
        String charset = ClientContext.getString("addUser.password.hash.charset");
        String hashPass = Util.createPasswordHash(Algorithm,encoding,charset,userId,model.getPassword());
        model.setPassword(hashPass);
        
        // �^�C�}�[�֘A�萔���擾����
        int maxEstimation = ClientContext.getInt("account.task.maxEstimation");
        int delay = ClientContext.getInt("account.task.delay");
        int decideToPopup = ClientContext.getInt("account.task.decideToPopup");
        int milisToPopup = ClientContext.getInt("account.task.milisToPopup");
        String progressNote = ClientContext.getString("account.task.addingMsg");
        
        final ProgressMonitor monitor = new ProgressMonitor(AddFacilityDialog.this, null, progressNote, 0, maxEstimation / delay);
        
        final AddFacilityTask task = new AddFacilityTask(model, maxEstimation / delay);
        
        taskTimer = new javax.swing.Timer(delay, new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                if (monitor.isCanceled()) {
                    taskTimer.stop();
                    monitor.close();
                    okBtn.setEnabled(true);
                    return;
                }
                
                if (! task.isDone()) {
                    monitor.setProgress(task.getCurrent());
                    return;
                }
                
                taskTimer.stop();
                monitor.close();
                
                if (task.isNoError()) {
                    
                    okBtn.setEnabled(false);
                    
                    // �������b�Z�[�W��\������
                    StringBuilder sb = new StringBuilder();
                    sb.append(ClientContext.getString("account.task.successMsg1"));
                    sb.append("\n");
                    sb.append(ClientContext.getString("account.task.successMsg2"));
                    sb.append("\n");
                    sb.append(ClientContext.getString("account.task.successMsg3"));
                    sb.append("\n");
                    sb.append(ClientContext.getString("account.task.successMsg4"));
                    JOptionPane.showMessageDialog(
                            AddFacilityDialog.this,
                            sb.toString(),
                            AddFacilityDialog.this.getTitle(),
                            JOptionPane.INFORMATION_MESSAGE);
                    
                    // �T�[�o�A�J�E���g����ʒm����
                    ServerInfo info = new ServerInfo();
                    //info.setFacilityId(model.getFacilityModel().getFacilityId());
                    info.setAdminId(model.getUserId());
                    setServerInfo(info);
                    
                    AddFacilityDialog.this.setVisible(false);
                    AddFacilityDialog.this.dispose();
                    
                } else {
                    // �G���[���b�Z�[�W��\������
                    String msg = task.getErrorMessage();
                    String title = AddFacilityDialog.this.getTitle();
                    JOptionPane.showMessageDialog(AddFacilityDialog.this, msg, title, JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        okBtn.setEnabled(false);
        monitor.setProgress(0);
        monitor.setMillisToDecideToPopup(decideToPopup);
        monitor.setMillisToPopup(milisToPopup);
        task.start();
        taskTimer.start();
    }
    
    /**
     * AddFacilityTask
     */
    protected class AddFacilityTask extends AbstractInfiniteTask {
        
        private UserModel user;
        private int errorCode;
        private String errorMessage;
        
        public AddFacilityTask(UserModel user, int taskLength) {
            this.user = user;
            setTaskLength(taskLength);
        }
        
        public boolean isNoError() {
            return errorCode == 0 ? true : false;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        
        protected void doTask() {
            
            try {
                // SECURITY
                String qid = "minagawa";
                String password = "hanagui+";
                String securityDomain = "openDolphinSysAd";
                //String providerURL = "jnp://172.168.158.1:1099";
                String providerURL = "jnp://210.153.124.60:1099";
                //String providerURL = "jnp://localhost:1099";
                
                UsernamePasswordHandler h = new UsernamePasswordHandler(qid, password.toCharArray());
                LoginContext lc = new LoginContext(securityDomain, h);
                lc.login();
                
                Properties props = new Properties();
                props.setProperty("java.naming.factory.initial","org.jnp.interfaces.NamingContextFactory");
                props.setProperty("java.naming.provider.url",providerURL);
                props.setProperty("java.naming.factory.url.pkgs","org.jboss.namingrg.jnp.interfaces");
                InitialContext ctx = new InitialContext(props);
                
                RemoteSystemService service = (RemoteSystemService)ctx.lookup("openDolphin/RemoteSystemService");
                service.addFacilityAdmin(user);
                
            } catch (javax.ejb.EJBAccessException ee) {
                ee.printStackTrace();
                errorCode = -10;
                StringBuilder sb = new StringBuilder();
                sb.append("�V�X�e���ݒ�G���[");
                sb.append("\n");
                sb.append(appendExceptionInfo(ee));
                setErrorMessage(sb.toString());
                
            } catch (javax.naming.CommunicationException ce) {
                ce.printStackTrace();
                errorCode = -20;
                StringBuilder sb = new StringBuilder();
                sb.append("ASP�T�[�o�ɐڑ��ł��܂���B");
                sb.append("\n");
                sb.append("�t�@�C���[�E�H�[�������T�[�r�X�𗘗p�ł��Ȃ��ݒ�ɂȂ��Ă���\��������܂��B");
                sb.append("\n");
                sb.append(appendExceptionInfo(ce));
                setErrorMessage(sb.toString());
                
            } catch (javax.naming.NamingException ne) {
                ne.printStackTrace();
                errorCode = -30;
                StringBuilder sb = new StringBuilder();
                sb.append("�A�v���P�[�V�����G���[");
                sb.append("\n");
                sb.append(appendExceptionInfo(ne));
                setErrorMessage(sb.toString());
                
            } catch (LoginException le) {
                le.printStackTrace();
                errorCode = -40;
                StringBuilder sb = new StringBuilder();
                sb.append("�Z�L�����e�B�G���[�������܂����B");
                sb.append("\n");
                sb.append("�N���C�A���g�̊������s��������Ȃ��ݒ�ɂȂ��Ă���\��������܂��B");
                sb.append("\n");
                sb.append(appendExceptionInfo(le));
                setErrorMessage(sb.toString());
                
            } catch (Exception oe) {
                oe.printStackTrace();
                errorCode = -50;
                StringBuilder sb = new StringBuilder();
                sb.append("�\�����Ȃ��G���[");
                sb.append("\n");
                sb.append(appendExceptionInfo(oe));
                setErrorMessage(sb.toString());
            }
            
            setDone(true);
        }
        
        private String appendExceptionInfo(Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("��O�N���X: ");
            sb.append(e.getClass().getName());
            sb.append("\n");
            if (e.getCause() != null) {
                sb.append("����: ");
                sb.append(e.getCause().getMessage());
                sb.append("\n");
            }
            if (e.getMessage() != null) {
                sb.append("���e: ");
                sb.append(e.getMessage());
                sb.append("\n");
            }
            return sb.toString();
        }
    }
    
    public void componentMoved(java.awt.event.ComponentEvent componentEvent) {
        Point loc = getLocation();
        System.out.println(getTitle() + " : x=" + loc.x+ " y=" + loc.y);
    }
    
    public void componentResized(java.awt.event.ComponentEvent componentEvent) {
        int width = getWidth();
        int height = getHeight();
        System.out.println(getTitle() + " : width=" + width + " height=" + height);
    }
    
    public void componentShown(java.awt.event.ComponentEvent componentEvent) {
    }
    
    public void componentHidden(java.awt.event.ComponentEvent componentEvent) {
    }
}
