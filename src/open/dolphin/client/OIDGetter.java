package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import open.dolphin.ejb.RemoteSystemService;

import org.jboss.security.auth.callback.UsernamePasswordHandler;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;

/**
 * OIDRequester
 *
 * @author Minagawa,Kazushi
 *
 */
public class OIDGetter extends JPanel {
    
    private static final long serialVersionUID = 1666003906485274645L;
    
    public static final String NEXT_OID_PROP = "nextOidProp";
    private static final int MAX_ESTIMATION = 30*1000;
    private static final int DELAY = 200;
    private static final String PROGRESS_NOTE = "�ʐM�e�X�g�����Ă��܂�...";
    private static final String SUCCESS_NOTE = "�ʐM�ɐ������܂����B�����{�^�����N���b�N�����ɐi�ނ��Ƃ��ł��܂��B";
    private static final String TASK_TITLE = "�ʐM�e�X�g";
    
    private String helloReply;
    private PropertyChangeSupport boundSupport = new PropertyChangeSupport(this);
    
    private JButton comTest = new JButton(TASK_TITLE);
    
    public OIDGetter() {
        initialize();
        connect();
    }
    
    public String getHelloReply() {
        return helloReply;
    }
    
    public void setHelloReply(String oid) {
        helloReply = oid;
        boundSupport.firePropertyChange(NEXT_OID_PROP, "", helloReply);
    }
    
    public void addOidPropertyListener(PropertyChangeListener l) {
        boundSupport.addPropertyChangeListener(NEXT_OID_PROP, l);
    }
    
    public void removeOidPropertyListener(PropertyChangeListener l) {
        boundSupport.removePropertyChangeListener(NEXT_OID_PROP, l);
    }
    
    private void initialize() {
        
        try {
            InputStream in = ClientContext.getResourceAsStream("account-make-info.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "SHIFT_JIS"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ( (line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            JTextArea infoArea = new JTextArea();
            infoArea.setEditable(false);
            infoArea.setLineWrap(true);
            infoArea.setMargin(new Insets(10,10,10,10));
            infoArea.setText(sb.toString());
            JScrollPane scroller = new JScrollPane(infoArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            btnPanel.add(new JLabel("���̃{�^�����N���b�N���A�ʐM�ł��邩�ǂ����m�F���Ă��������B"));
            btnPanel.add(comTest);
            
            this.setLayout(new BorderLayout());
            this.add(scroller, BorderLayout.CENTER);
            this.add(btnPanel, BorderLayout.SOUTH);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void connect() {
        comTest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doTest();
            }
        });
    }
    
    private void doTest() {
               
        ApplicationContext appCtx = ClientContext.getApplicationContext();
        Application app = appCtx.getApplication();
        
        OidTask task = new OidTask(app);
        
        TaskMonitor taskMonitor = appCtx.getTaskMonitor();
        String message = "�ʐM�e�X�g";
        Component c = SwingUtilities.getWindowAncestor(this);
        TaskTimerMonitor w = new TaskTimerMonitor(task, taskMonitor, c, message, PROGRESS_NOTE, DELAY, MAX_ESTIMATION);
        taskMonitor.addPropertyChangeListener(w);
        
        appCtx.getTaskService().execute(task);
    }
    
    class OidTask extends Task<String, Void> {
                
        public OidTask(Application app) {
            super(app);
        }
        
        @Override
        protected String doInBackground() throws Exception {
                      
            // SECURITY
            String qid = "minagawa";
            String password = "hanagui+";
            String securityDomain = "openDolphinSysAd";
            String providerURL = "jnp://210.153.124.60:1099";

            UsernamePasswordHandler h = new UsernamePasswordHandler(qid, password.toCharArray());
            LoginContext lc = new LoginContext(securityDomain, h);
            lc.login();

            Properties props = new Properties();
            props.setProperty("java.naming.factory.initial","org.jnp.interfaces.NamingContextFactory");
            props.setProperty("java.naming.provider.url",providerURL);
            props.setProperty("java.naming.factory.url.pkgs","org.jboss.namingrg.jnp.interfaces");
            InitialContext ctx = new InitialContext(props);

            RemoteSystemService service = (RemoteSystemService)ctx.lookup("openDolphin/RemoteSystemService");
            String result = service.helloDolphin();
            return result;
                
        }
        
        @Override
        protected void succeeded(String result) {
            //logger.debug("Task succeeded");
            Window myParent = SwingUtilities.getWindowAncestor(OIDGetter.this);
            String title = ClientContext.getFrameTitle(TASK_TITLE);
            JOptionPane.showMessageDialog(myParent, SUCCESS_NOTE, title, JOptionPane.INFORMATION_MESSAGE);
            setHelloReply(result);
        }
        
        @Override
        protected void failed(Throwable cause) {
            
            String errMsg = null;
                
            if (cause instanceof javax.ejb.EJBAccessException) {
                StringBuilder sb = new StringBuilder();
                sb.append("�V�X�e���ݒ�G���[");
                sb.append("\n");
                sb.append(appendExceptionInfo(cause));
                errMsg = sb.toString();
                
            } else if (cause instanceof javax.naming.CommunicationException) {
                StringBuilder sb = new StringBuilder();
                sb.append("ASP�T�[�o�ɐڑ��ł��܂���B");
                sb.append("\n");
                sb.append("�t�@�C���[�E�H�[�������T�[�r�X�𗘗p�ł��Ȃ��ݒ�ɂȂ��Ă���\��������܂��B");
                sb.append("\n");
                sb.append(appendExceptionInfo(cause));
                errMsg = sb.toString();
                
            } else if (cause instanceof javax.naming.NamingException) {
                StringBuilder sb = new StringBuilder();
                sb.append("�A�v���P�[�V�����G���[");
                sb.append("\n");
                sb.append(appendExceptionInfo(cause));
                errMsg = sb.toString();
                
            } else if (cause instanceof LoginException) {
                StringBuilder sb = new StringBuilder();
                sb.append("�Z�L�����e�B�G���[�������܂����B");
                sb.append("\n");
                sb.append("�N���C�A���g�̊������s��������Ȃ��ݒ�ɂȂ��Ă���\��������܂��B");
                sb.append("\n");
                sb.append(appendExceptionInfo(cause));
                errMsg = sb.toString();
                
            } else if (cause instanceof Exception) {
                StringBuilder sb = new StringBuilder();
                sb.append("�\�����Ȃ��G���[");
                sb.append("\n");
                sb.append(appendExceptionInfo(cause));
                errMsg = sb.toString();
            }
            
            Window myParent = SwingUtilities.getWindowAncestor(OIDGetter.this);
            String title = ClientContext.getFrameTitle(TASK_TITLE);
            JOptionPane.showMessageDialog(myParent, errMsg, title, JOptionPane.WARNING_MESSAGE);
            setHelloReply(null);
            
        }
        
        private String appendExceptionInfo(Throwable cause) {
            StringBuilder sb = new StringBuilder();
            sb.append("��O�N���X: ");
            sb.append(cause.getClass().getName());
            sb.append("\n");
            if (cause.getCause() != null) {
                sb.append("����: ");
                sb.append(cause.getCause().getMessage());
                sb.append("\n");
            }
            if (cause.getMessage() != null) {
                sb.append("���e: ");
                sb.append(cause.getMessage());
                sb.append("\n");
            }
            return sb.toString();
        }  
    }
}
