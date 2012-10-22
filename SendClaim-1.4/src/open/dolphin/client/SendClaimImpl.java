package open.dolphin.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;


import open.dolphin.project.*;
import org.apache.log4j.Logger;


/**
 * SendClaimPlugin
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class SendClaimImpl implements ClaimMessageListener {
    
    // Socket constants
    private final int EOT                   	= 0x04;
    private final int ACK                   	= 0x06;
    private final int NAK                   	= 0x15;
    private final int DEFAULT_TRY_COUNT     	= 3;		// Socket �ڑ������݂��
    private final long DEFAULT_SLEEP_TIME   	= 20*1000L; 	// Socket �ڑ��������Ȃ������ꍇ�Ɏ��̃g���C�܂ő҂��� msec
    private final int MAX_QUEU_SIZE 		= 10;
    
    // Alert constants
    private final int TT_QUEUE_SIZE         = 0;
    private final int TT_NAK_SIGNAL         = 1;
    private final int TT_SENDING_TROUBLE    = 2;
    private final int TT_CONNECTION_REJECT  = 3;
    
    // Strings
    private final String proceedString      = "�p��";
    private final String dumpString         = "���O�֋L�^";
    
    // Properties
    private String host;
    private int port;
    private String enc;
    private int tryCount 	= DEFAULT_TRY_COUNT;
    private long sleepTime 	= DEFAULT_SLEEP_TIME;
    private int alertQueueSize 	= MAX_QUEU_SIZE;
    
    private Thread sendThread;
    private final List queue = new LinkedList();
    private OrcaSocket orcaSocket;
    
    
    private MainWindow context;
    private String name;
    
    /**
     * Creates new ClaimQue 
     */
    public SendClaimImpl() {
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public MainWindow getContext() {
        return context;
    }
    
    @Override
    public void setContext(MainWindow context) {
        this.context = context;
    }

    private Logger getLogger() {
        return ClientContext.getClaimLogger();
    }
    
    private void setup() {
        setHost(Project.getClaimAddress());
        setPort(Project.getClaimPort());
        setEncoding(Project.getClaimEncoding());
        
        if (orcaSocket == null) {
            orcaSocket = new OrcaSocket(getHost(), getPort(), sleepTime, tryCount);
        }
    }
    
    /**
     * �v���O�������J�n����B
     */
    @Override
    public void start() {
        
        setup();
        
        sendThread = new Thread(new Consumer(orcaSocket));
        sendThread.start();
        
        getLogger().info("SendClaim started with = host = " + getHost() + " port = " + getPort());
    }
    
    /**
     * �v���O�������I������B
     */
    @Override
    public void stop() {
        
        if (sendThread != null) {
            Thread moribund = sendThread;
            sendThread = null;
            moribund.interrupt();
        }

        logDump();
    }
    
    @Override
    public String getHost() {
        return host;
    }
    
    @Override
    public void setHost(String host) {
        this.host = host;
    }
    
    @Override
    public int getPort() {
        return port;
    }
    
    @Override
    public void setPort(int port) {
        this.port = port;
    }
    
    @Override
    public String getEncoding() {
        return enc;
    }
    
    @Override
    public void setEncoding(String enc) {
        this.enc = enc;
    }
    
    public int getTryCount() {
        return tryCount;
    }
    
    public void setTryCount(int val) {
        tryCount = val;
    }
    
    public int getAlertQueueSize() {
        return alertQueueSize;
    }
    
    public void getAlertQueueSize(int val) {
        alertQueueSize = val;
    }
    
    /**
     * �J���e�� CLAIM �f�[�^�����������Ƃ��̒ʒm���󂯂�B
     */
    @Override
    public void claimMessageEvent(ClaimMessageEvent e) {
        synchronized (queue) {
            queue.add(e);
            queue.notify();
        }     
    }
    
    /**
     * Queue ������o���B
     */
    private Object getCLAIM() throws InterruptedException {
        synchronized(queue) {
            while (queue.isEmpty()) {
                queue.wait();
            }
        }
        return queue.remove(0);
    }
    
    public synchronized int getQueueSize() {
        return queue.size();
    }
    
    /**
     * Queue���� CLAIM message �����O�֏o�͂���B
     */
    public void logDump() {
        
        synchronized(queue) {
            
            Iterator iter = queue.iterator();

            while (iter.hasNext()) {
                ClaimMessageEvent evt = (ClaimMessageEvent) iter.next();
                getLogger().warn(evt.getClaimInsutance());
            }

            queue.clear();
        }
    }
    
    private int alertDialog(int code) {
        
        int option = -1;
        String title = "OpenDolphin: CLAIM ���M";
        StringBuffer buf = null;
        
        switch(code) {
            
            case TT_QUEUE_SIZE:
                buf = new StringBuffer();
                buf.append("�����M��CLAIM(���Z�v�g)�f�[�^��");
                buf.append(getQueueSize());
                buf.append(" ����܂��BCLAIM �T�[�o�Ƃ̐ڑ����m�F���Ă��������B\n");
                buf.append("1. ���̂܂܏������p�����邱�Ƃ��ł��܂��B\n");
                buf.append("2. �����M�f�[�^�����O�ɋL�^���邱�Ƃ��ł��܂��B\n");
                buf.append("   ���̏ꍇ�A�f�[�^�͑��M���ꂸ�A�f�Õ�V�͎���͂ƂȂ�܂��B");
                
                option = JOptionPane.showOptionDialog(
                        null,
                        buf.toString(),
                        title,
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE,null,
                        new String[]{proceedString, dumpString},proceedString);
                break;
                
            case TT_NAK_SIGNAL:
                buf = new StringBuffer();
                buf.append("CLAIM(���Z�v�g)�f�[�^���T�[�o�ɂ�苑�ۂ���܂����B\n");
                buf.append("���M���̃f�[�^�̓��O�ɋL�^���܂��B�f�Õ�V�̎������͂͂ł��܂���B");
                JOptionPane.showMessageDialog(
                        null,
                        buf.toString(),
                        title,
                        JOptionPane.ERROR_MESSAGE);
                break;
                
            case TT_SENDING_TROUBLE:
                buf = new StringBuffer();
                buf.append("CLAIM(���Z�v�g)�f�[�^�̑��M���ɃG���[�������܂����B\n");
                buf.append("���M���̃f�[�^�̓��O�ɋL�^���܂��B�f�Õ�V�̎������͂͂ł��܂���B");
                JOptionPane.showMessageDialog(
                        null,
                        buf.toString(),
                        title,
                        JOptionPane.ERROR_MESSAGE);
                break;
                
            case TT_CONNECTION_REJECT:
                buf = new StringBuffer();
                buf.append("CLAIM(���Z�v�g)�T�[�o ");
                buf.append("Host=");
                buf.append(host);
                buf.append(" Port=");
                buf.append(port);
                buf.append(" ���������܂���B\n");
                //buf.append(tryCount*sleepTime);
                buf.append("�T�[�o�̓d���y�ѐڑ����m�F���Ă��������B\n");
                buf.append("1. ���̂܂ܐڑ���҂��Ƃ��ł��܂��B\n");
                buf.append("2. �f�[�^�����O�ɋL�^���邱�Ƃ��ł��܂��B\n");
                buf.append("   ���̏ꍇ�A�f�[�^�͑��M���ꂸ�A�f�Õ�V�͎���͂ƂȂ�܂��B");
                
                option = JOptionPane.showOptionDialog(
                        null,
                        buf.toString(),
                        title,
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE,null,
                        new String[]{proceedString, dumpString},proceedString);
                break;
        }
        
        return option;
    }
    
    private void warnLog(String result, ClaimMessageEvent evt) {
        getLogger().warn(getBasicInfo(result, evt));
        getLogger().warn(evt.getClaimInsutance());
    }
    
    private void log(String result, ClaimMessageEvent evt) {
        getLogger().info(getBasicInfo(result, evt));
    }
    
    private String getBasicInfo(String result, ClaimMessageEvent evt) {
        
        String id = evt.getPatientId();
        String nm = evt.getPatientName();
        String sex = evt.getPatientSex();
        String title = evt.getTitle();
        String timeStamp = evt.getConfirmDate();
        
        StringBuilder buf = new StringBuilder();
        buf.append(result);
        buf.append("[");
        buf.append(id);
        buf.append(" ");
        buf.append(nm);
        buf.append(" ");
        buf.append(sex);
        buf.append(" ");
        buf.append(title);
        buf.append(" ");
        buf.append(timeStamp);
        buf.append("]");
        
        return buf.toString();
    }
    
    /**
     * CLAIM ���M�X���b�h�B
     */
    protected class Consumer implements Runnable {
        
        private OrcaSocket orcaSocket;
        
        public Consumer(OrcaSocket orcaSocket) {
            this.orcaSocket = orcaSocket;
        }
        
        @Override
        public void run() {
            
            Thread thisThread = Thread.currentThread();

            while (thisThread == sendThread) {

                try {
                    // CLAIM Event ���擾
                    ClaimMessageEvent claimEvent = (ClaimMessageEvent) getCLAIM();
                    String instance = claimEvent.getClaimInsutance();

                    // Gets connection
                    Socket socket = orcaSocket.getSocket();
                    if ( socket == null ) {
                        int option = alertDialog(TT_CONNECTION_REJECT);
                        if (option == 1) {
                            warnLog("CLAIM  Socket Error", claimEvent);
                            continue;
                        } else {
                            // push back to the queue
                            claimMessageEvent(claimEvent);
                            continue;
                        }
                    }

                    // Gets io stream
                    OutputStream out = socket.getOutputStream();
                    DataOutputStream dout = new DataOutputStream(out);
                    BufferedOutputStream writer = new BufferedOutputStream(dout);
                    
                    InputStream in = socket.getInputStream();
                    DataInputStream din = new DataInputStream(in);
                    BufferedInputStream reader = new BufferedInputStream(din);

                    // Writes UTF8 data
                    writer.write(instance.getBytes(enc));
                    writer.write(EOT);
                    writer.flush();

                    // Reads result
                    int c = reader.read();
                    if (c == ACK) {
                        log("CLAIM ACK", claimEvent);
                    } else if (c == NAK) {
                        warnLog("received NAK", claimEvent);
                    }

                    writer.close();
                    reader.close();
                    socket.close();
                    socket = null;

                } catch (IOException e) {
                    e.printStackTrace();
                    getLogger().warn(e.getMessage());
                    alertDialog(TT_SENDING_TROUBLE);

                } catch (InterruptedException e) {
                    getLogger().warn("Interrupted sending CLAIM");

                } catch (Exception e) {
                    e.printStackTrace();
                    getLogger().warn(e.getMessage());
                }
            }
        }
    }
}