package open.dolphin.delegater;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import open.dolphin.project.Project;

/**
 * Bsiness Delegater �̃��[�g�N���X�B
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class BusinessDelegater {
    
    public static final int NO_ERROR            = 0;
    public static final int COMMUNICATION_ERROR = -1;
    public static final int EJB_ACCESS_ERROR    = -2;
    public static final int SECURITY_ERROR      = -3;
    public static final int UNDECLARED_ERROR    = -4;
    public static final int UNKNOWM_ERROR       = -5;
    
    private static final String SECURITY_DOMAIN = "openDolphin";
    
    private String securityDomain;
    private int errorCode;
    private String errorMessage;
    
    
    public BusinessDelegater() {
        setSecurityDomain(SECURITY_DOMAIN);
    }
    
    /**
     * @return Returns the enviroment.
     */
    private Hashtable getEnviroment() {
        Hashtable<String, String> enviroment = new Hashtable<String, String>(5,0.9f);
        enviroment.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
        enviroment.put("java.naming.provider.url", Project.getProviderURL());
        enviroment.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
        return enviroment;
    }
    
    /**
     * �T�[�r�X��Ԃ��B
     * @param name �T�[�r�X��
     * @return �����[�g�T�[�r�X
     * @throws NamingException
     */
    public Object getService(String name) throws NamingException {
        InitialContext ctx = new InitialContext(getEnviroment());
        StringBuilder sb = new StringBuilder();
        sb.append(SECURITY_DOMAIN);
        sb.append("/");
        sb.append(name);
        return ctx.lookup(sb.toString());
    }
    
    /**
     * @return Returns the securityDomain.
     */
    public String getSecurityDomain() {
        return securityDomain;
    }
    
    public void setSecurityDomain(String value) {
        securityDomain = value;
    }
    
    /**
     * @param errCode The errCode to set.
     */
    public void setErrorCode(int errCode) {
        this.errorCode = errCode;
    }
    
    /**
     * @return Returns the errCode.
     */
    public int getErrorCode() {
        return errorCode;
    }
    
    /**
     * �G���[�������Ă��邩�ǂ�����Ԃ��B
     * @return �G���[�������Ă��Ȃ��� true
     */
    public boolean isNoError() {
        return errorCode == NO_ERROR ? true : false;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMesseage) {
        this.errorMessage = errorMesseage;
    }
    
    public void processError(Exception e) {
        
        StringBuilder sb = new StringBuilder();
        
        if (e instanceof javax.ejb.EJBAccessException) {
            setErrorCode(EJB_ACCESS_ERROR);
            sb.append("�F�؂ł��܂���B���[�UID�܂��̓p�X���[�h�ɊԈႢ������܂��B");
            sb.append("\n");
            sb.append(appendExceptionInfo(e));
            setErrorMessage(sb.toString());
            
        } else if (e instanceof javax.naming.CommunicationException) {
            setErrorCode(COMMUNICATION_ERROR);
            sb.append("�T�[�o�ɐڑ��ł��܂���B�l�b�g���[�N�������m���߂��������B");
            sb.append("\n");
            sb.append(appendExceptionInfo(e));
            setErrorMessage(sb.toString());
            
        } else if (e instanceof LoginException) {
            setErrorCode(SECURITY_ERROR);
            sb.append("�Z�L�����e�B�G���[�������܂����B");
            sb.append("\n");
            sb.append("�N���C�A���g�̊������s��������Ȃ��ݒ�ɂȂ��Ă���\��������܂��B");
            sb.append("\n");
            sb.append(appendExceptionInfo(e));
            setErrorMessage(sb.toString());
            
        } else if (e instanceof UndeclaredThrowableException) {
            setErrorCode(UNDECLARED_ERROR);
            sb.append("���������s�ł��܂���B");
            sb.append("\n");
            sb.append("�N���C�A���g�̃o�[�W�������Â��\��������܂��B");
            sb.append(appendExceptionInfo(e));
            setErrorMessage(sb.toString());
            
        } else {
            setErrorCode(UNKNOWM_ERROR);
            sb.append("�A�v���P�[�V�����G���[");
            sb.append("\n");
            sb.append(appendExceptionInfo(e));
            setErrorMessage(sb.toString());
        }
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
