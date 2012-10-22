
package open.dolphin.client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import open.dolphin.infomodel.SchemaModel;
import open.dolphin.project.Project;

import org.apache.log4j.Logger;

/**
 * MML ���M�T�[�r�X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class SendMmlImpl implements MmlMessageListener {
    
    // CSGW �ւ̏������݃p�X
    private String csgwPath;
    
    // MML Encoding
    private String encoding;
    
    // Work Queue
    private LinkedBlockingQueue queue;
    
    private Kicker kicker;
    
    private Thread sendThread;
    
    private Logger logger;
    
    private MainWindow context;
    
    private String name;
    
    /** Creates new SendMmlService */
    public SendMmlImpl() {
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public MainWindow getContext() {
        return context;
    }
    
    public void setContext(MainWindow context) {
        this.context = context;
    }
    
    public String getCSGWPath() {
        return csgwPath;
    }
    
    private Logger getLogger() {
        if (logger == null) {
            logger = ClientContext.getPart11Logger();
        }
        return logger;
    }
    
    public void setCSGWPath(String val) {
        csgwPath = val;
        File directory = new File(csgwPath);
        if (! directory.exists()) {
            if (directory.mkdirs()) {
                getLogger().debug("MML�t�@�C���o�͐�̃f�B���N�g�����쐬���܂���");
            } else {
                getLogger().warn("MML�t�@�C���o�͐�̃f�B���N�g�����쐬�ł��܂���");
            }
        }
    }
    
    public void stop() {
        try {
            Thread moribund = sendThread;
            sendThread = null;
            moribund.interrupt();
            logDump();
            getLogger().info("Send MML stopped");
            
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().warn("Exception while stopping the send MML");
            getLogger().warn(e.getMessage());
        }
    }
    
    public void start() {
        
        // CSGW �������݃p�X��ݒ肷��
        setCSGWPath(Project.getCSGWPath());
        encoding = Project.getMMLEncoding();
        
        // ���M�L���[�𐶐�����
        queue = new LinkedBlockingQueue();
        kicker = new Kicker();
        sendThread = new Thread(kicker);
        sendThread.start();
        getLogger().info("Send MML statered with CSGW = " + getCSGWPath());
    }
    
    public void mmlMessageEvent(MmlMessageEvent e) {
        queue.offer(e);
    }
    
    public Object getMML() throws InterruptedException {
        return queue.take();
    }
    
    public void logDump() {
        
        synchronized (queue) {
            
            int size = queue.size();
            
            if (size != 0) {
                for (int i = 0; i < size; i++) {
                    try {
                        MmlMessageEvent evt = (MmlMessageEvent) queue.take();
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    protected String getCSGWPathname(String fileName, String ext) {
        StringBuffer buf = new StringBuffer();
        buf.append(csgwPath);
        buf.append(File.separator);
        buf.append(fileName);
        buf.append(".");
        buf.append(ext);
        return buf.toString();
    }
    
    protected class Kicker implements Runnable {
        
        public void run() {
            
            
            Thread thisThread = Thread.currentThread();
            BufferedOutputStream writer = null;
            
            while (thisThread == sendThread) {
                
                try {
                    // MML �p�b�P�[�W���擾
                    MmlMessageEvent mevt = (MmlMessageEvent) getMML();
                    getLogger().debug("MML�t�@�C�����R���V���[�����܂���");
                    String groupId = mevt.getGroupId();
                    String instance = mevt.getMmlInstance();
                    List<SchemaModel> schemas = mevt.getSchema();
                    
                    // �t�@�C�����𐶐�����
                    String dest = getCSGWPathname(groupId, "xml");
                    String temp = getCSGWPathname(groupId, "xml.tmp");
                    File f = new File(temp);
                    
                    // �C���X�^���X��UTF8�ŏ�������
                    writer = new BufferedOutputStream(new FileOutputStream(f));
                    byte[] bytes = instance.getBytes(encoding);
                    writer.write(bytes);
                    writer.flush();
                    writer.close();
                    
                    // �������ݏI����Ƀ��l�[������ (.tmp -> .xml)
                    f.renameTo(new File(dest));
                    getLogger().debug("MML�t�@�C�����������݂܂���");
                    
                    // �摜�𑗐M����
                    if (schemas != null) {
                        for (SchemaModel schema : schemas) {
                            dest = csgwPath + File.separator + schema.getExtRef().getHref();
                            temp = dest + ".tmp";
                            f = new File(temp);
                            writer = new BufferedOutputStream(new FileOutputStream(f));
                            writer.write(schema.getJpegByte());
                            writer.flush();
                            writer.close();
                            
                            // Rename����
                            f.renameTo(new File(dest));
                            getLogger().debug("�摜�t�@�C�����������݂܂���");
                        }
                    }
                    
                } catch (IOException e) {
                    e.printStackTrace();
                    getLogger().warn("IOException while send MML");
                    getLogger().warn(e.getMessage());
                    
                } catch (InterruptedException ie) {
                    getLogger().warn("InterruptedException while send MML");
                    break;
                }
            }
            
        }
    }
}