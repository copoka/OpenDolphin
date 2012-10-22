/*
 * SendMmlService.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2003 Digital Globe, Inc. All rights reserved.
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

import open.dolphin.infomodel.SchemaModel;
import open.dolphin.project.*;

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
 * MML ���M�T�[�r�X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class SendMmlPlugin extends DefaultMainWindowPlugin implements MmlMessageListener {
    
    // CSGW �ւ̏������݃p�X
    private String csgwPath;
    
    // MML Encoding
    private String encoding;
    
    // Work Queue
    private LinkedList queue;
    
    private Kicker kicker;
    
    private Thread sendThread;
    
    private Logger logger;
    
    /** Creates new SendMmlService */
    public SendMmlPlugin() {
        logger = ClientContext.getLogger("part11");
    }
    
    public String getCSGWPath() {
        return csgwPath;
    }
    
    public void setCSGWPath(String val) {
        csgwPath = val;
        File directory = new File(csgwPath);
        if (! directory.exists()) {
            if (directory.mkdirs()) {
                logger.debug("MML�t�@�C���o�͐�̃f�B���N�g�����쐬���܂���");
            } else {
                logger.warn("MML�t�@�C���o�͐�̃f�B���N�g�����쐬�ł��܂���");
            }
        }
    }
    
    public void stop() {
        try {
            Thread moribund = sendThread;
            sendThread = null;
            moribund.interrupt();
            logDump();
            super.stop();
            logger.info("Send MML stopped");
            
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("Exception while stopping the send MML");
            logger.warn(e.getMessage());
        }
    }
    
    public void start() {
        
        // CSGW �������݃p�X��ݒ肷��
        setCSGWPath(Project.getCSGWPath());
        encoding = Project.getMMLEncoding();
        
        // ���M�L���[�𐶐�����
        queue = new LinkedList();
        kicker = new Kicker();
        sendThread = new Thread(kicker);
        sendThread.start();
        super.start();
        logger.info("Send MML statered with CSGW = " + getCSGWPath());
    }
    
    @SuppressWarnings("unchecked")
    public synchronized void mmlMessageEvent(MmlMessageEvent e) {
        queue.addLast(e);
        notify();
    }
    
    public synchronized Object getMML() throws InterruptedException {
        while (queue.size() == 0) {
            wait();
        }
        return queue.removeFirst();
    }
    
    public void logDump() {
        
        synchronized (queue) {
            
            int size = queue.size();
            
            if (size != 0) {
                for (int i = 0; i < size; i++) {
                    try {
                        MmlMessageEvent evt = (MmlMessageEvent) queue.removeFirst();
                        
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
            
            //try {
                Thread thisThread = Thread.currentThread();
                BufferedOutputStream writer = null;
                
                while (thisThread == sendThread) {
                    
                    try {
                        // MML �p�b�P�[�W���擾
                        MmlMessageEvent mevt = (MmlMessageEvent) getMML();
                        logger.debug("MML�t�@�C�����R���V���[�����܂���");
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
                        logger.debug("MML�t�@�C�����������݂܂���");
                        
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
                                logger.debug("�摜�t�@�C�����������݂܂���");
                            }
                        }
                        
                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.warn("IOException while send MML");
                        logger.warn(e.getMessage());
                        
                    } catch (InterruptedException ie) {
                        logger.warn("InterruptedException while send MML");
                        break;
                    }
                }
            //} catch (InterruptedException ie) {
            //}
        }
    }
}