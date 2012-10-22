package open.dolphin.dao;

import open.dolphin.client.ClientContext;
import org.apache.log4j.Logger;

/**
 * DaoBean
 *
 * @author  Kazushi Minagawa
 */
public class DaoBean {
    
    public static final int TT_NONE              = 10;
    public static final int TT_NO_ERROR          =  0;
    public static final int TT_CONNECTION_ERROR  = -1;
    public static final int TT_DATABASE_ERROR    = -2;
    public static final int TT_UNKNOWN_ERROR     = -3;
    
    protected String host;
    protected int port;
    protected String user;
    protected String passwd;
    
    protected int errorCode;
    protected String errorMessage;
    
    protected Logger logger;
    
    /**
     * Creates a new instance of DaoBean
     */
    public DaoBean() {
        logger = ClientContext.getBootLogger();
    }
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getUser() {
        return user;
    }
    
    public void setUser(String user) {
        this.user = user;
    }
    
    public String getPasswd() {
        return passwd;
    }
    
    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }
    
    public boolean isNoError() {
        return errorCode == TT_NO_ERROR ? true : false;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    /**
     * ��O����͂��G���[�R�[�h�ƃG���[���b�Z�[�W��ݒ肷��B
     *
     * @param e Exception
     */
    protected void processError(Exception e) {
        
        StringBuilder sb  = new StringBuilder();
        
        if (e instanceof org.postgresql.util.PSQLException) {
            setErrorCode(TT_CONNECTION_ERROR);
            sb.append("�T�[�o�ɐڑ��ł��܂���B�l�b�g���[�N�������m���߂��������B");
            sb.append("\n");
            sb.append(appenExceptionInfo(e));
            setErrorMessage(sb.toString());
            
        } else if (e instanceof java.sql.SQLException) {
            setErrorCode(TT_DATABASE_ERROR);
            sb.append("�f�[�^�x�[�X�A�N�Z�X�G���[");
            sb.append("\n");
            sb.append(appenExceptionInfo(e));
            setErrorMessage(sb.toString());
        } else {
            setErrorCode(TT_UNKNOWN_ERROR);
            sb.append("�A�v���P�[�V�����G���[");
            sb.append("\n");
            sb.append(appenExceptionInfo(e));
            setErrorMessage(sb.toString());
        }
    }
    
    /**
     * ��O�̎�����������B
     * @param e ��O
     */
    protected String appenExceptionInfo(Exception e) {
        
        StringBuilder sb  = new StringBuilder();
        sb.append("��O�N���X: ");
        sb.append(e.getClass().getName());
        sb.append("\n");
        if (e.getCause() != null && e.getCause().getMessage() != null) {
            sb.append("����: ");
            sb.append(e.getCause().getMessage());
            sb.append("\n");
        }
        if (e.getMessage() != null) {
            sb.append("���e: ");
            sb.append(e.getMessage());
        }
        
        return sb.toString();
    }
}