
package open.dolphin.client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
    // CSGW = Client Side Gateway
    private String csgwPath;
    
    // MML Encoding
    private String encoding;
    
    // Work Queue
    private final LinkedList queue = new LinkedList();
    
    private Kicker kicker;
    
    private Thread sendThread;
    
    private MainWindow context;
    
    private String name;
    
    /** Creates new SendMmlService */
    public SendMmlImpl() {
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
    
    @Override
    public String getCSGWPath() {
        return csgwPath;
    }
    
    private Logger getLogger() {
        return ClientContext.getMmlLogger();
    }
    
    @Override
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
    
    @Override
    public void stop() {
        if (sendThread!=null) {
            Thread moribund = sendThread;
            sendThread = null;
            moribund.interrupt();
        }
        logDump();
    }
    
    @Override
    public void start() {
        
        // CSGW �������݃p�X��ݒ肷��
        setCSGWPath(Project.getCSGWPath());
        encoding = Project.getMMLEncoding();
        
        // ���M�X���b�h���J�n����
        kicker = new Kicker();
        sendThread = new Thread(kicker);
        sendThread.start();
        getLogger().info("Send MML statered with CSGW = " + getCSGWPath());
    }
    
    @Override
    public void mmlMessageEvent(MmlMessageEvent e) {
        synchronized (queue) {
            queue.add(e);
            queue.notify();
        }
    }
    
    private Object getMML() throws InterruptedException {
        synchronized(queue) {
            while (queue.isEmpty()) {
                queue.wait();
            }
        }
       return queue.remove(0);
    }
    
    public void logDump() {

        synchronized(queue) {

            Iterator iter = queue.iterator();

            while (iter.hasNext()) {
                MmlMessageEvent evt = (MmlMessageEvent) iter.next();
                getLogger().warn(evt.getMmlInstance());
            }

            queue.clear();
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
        
        @Override
        public void run() {
            
            
            Thread thisThread = Thread.currentThread();
            BufferedOutputStream writer = null;
            
            while (thisThread == sendThread) {
                
                try {
                    // MML �p�b�P�[�W���擾
                    MmlMessageEvent mevt = (MmlMessageEvent) getMML();
                    //getLogger().debug("MML�t�@�C�����R���V���[�����܂���");
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
                    getLogger().warn(e.getMessage());
                    
                } catch (InterruptedException ie) {
                    getLogger().warn("Interrupted sending MML");

                } catch (Exception ee) {
                    ee.printStackTrace();
                    getLogger().warn(ee.getMessage());
                }
            }
        }
    }
}