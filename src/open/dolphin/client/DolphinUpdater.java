/*
 * DolphinUpdater.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
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

import open.dolphin.plugin.*;
import open.dolphin.project.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.beans.*;

/**
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class DolphinUpdater extends DefaultPlugin {
    
    static int TT_NO_INFO       = -1;
    static int TT_LATEST        = 0;
    static int TT_AVAILABLE     = 1;
    
    String remoteURL;
    long remoteLastModify;

    /** Creates new DolphinUpdater */
    public DolphinUpdater() {
    }
        
    public String getRemoteURL() {
        return remoteURL;
    }
    
    public void setRemoteURL(String val) {
        remoteURL = val;
    }
    
    public int checkUpdateAvailable() {
        
        int ret = TT_NO_INFO;
        long lastModify = 0L;
        
        String proxyHost = Project.getProxyHost();
        String proxyPort  = String.valueOf(Project.getProxyPort());
                
        if ((proxyHost != null) && (proxyPort != null)) {
            Properties prop = System.getProperties();
            prop.put("http.proxyHost", proxyHost);
            prop.put("http.proxyPort", proxyPort);
        }
        
        // �ݒ�t�@�C������ lastModify ���擾����
        long longSt = Project.getLastModify();
        if (longSt != 0L) {
            // �k���łȂ���Βl���Z�b�g����
            System.out.println("last modify: " + longSt);
            lastModify = longSt;
        }
        
        // URL �ɐڑ����� lastModify ���r����
        try {
            URL url = new URL(remoteURL);
            URLConnection con = url.openConnection();
            remoteLastModify = con.getLastModified();
            if (remoteLastModify != 0L) {
                System.out.println("remote last modify: " + String.valueOf(remoteLastModify));
                // �ݒ�t�@�C���ƈقȂ��Ă���΍X�V
                ret = (lastModify != remoteLastModify) ? 1 : 0;
            }
            else {
                System.out.println("Could'n get the lastModify information");
            }
        }
        catch (IOException ie) {
            System.out.println("Exception while opening the URLConection: " + ie.toString());
        }
       
        return ret;
    }
    
    public void update() {
                      
        URLBinaryDownloader bdl = new URLBinaryDownloader();
        bdl.setURLString(remoteURL);
        bdl.setMessage("�V�����\�t�g�E�F�A���_�E�����[�h���Ă��܂�...");
        bdl.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pe) {
                byte[] readBytes = (byte[])pe.getNewValue();
                if (readBytes != null) {
                    processUpdate(readBytes);
                }
                else {
                    // Cancel
                    ClientContext.remove(DolphinUpdater.this);
                }
            }
        });
        Thread t = new Thread(bdl);
        t.start();
    }
     
    /**
     * Binary �f�[�^���󂯎���Ď��g������������
     */
    private void processUpdate(final byte[] newJar) {

       try {
           // Replace jar
            String myPath = ClientContext.getUserDirectory() + File.separator + "Dolphin.jar";
            File dest = new File(myPath);
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
            out.write(newJar);
            out.flush();
            out.close();
                      
            Project.setLastModify(remoteLastModify);
            ClientContextStub stub = (ClientContextStub)ClientContext.getClientContextStub();
            stub.storeProject(Project.getProjectStub());
            
            // Show succeeded message
            JOptionPane.showMessageDialog(null,
                                         "�\�t�g�E�F�A�̍X�V�ɐ������܂����B\n�h���t�B�����ċN�����Ă��������B",
                                         "Dolphin: �����X�V",
                                         JOptionPane.INFORMATION_MESSAGE);
            
            System.exit(1);
        }
        catch (Exception e) {
            // Show error message
            JOptionPane.showMessageDialog(null,
                                         "�X�V�ɃG���[�������܂����B\n�X�V�ł��܂���B",
                                         "Dolphin: �����X�V",
                                         JOptionPane.ERROR_MESSAGE);
            ClientContext.remove(DolphinUpdater.this);
            
        }
    }    
}