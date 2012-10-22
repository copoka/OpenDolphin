package open.dolphin.client;


/**
 * CLAIM �C�x���g���X�i�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public interface ClaimMessageListener extends MainService {
    
    public String getHost();
    
    public void setHost(String host);
    
    public int getPort();
    
    public void setPort(int port);
    
    public String getEncoding();
    
    public void setEncoding(String enc);

    public void claimMessageEvent(ClaimMessageEvent e);

}