/*
 * SendClaimService.java
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

import javax.swing.*;
import javax.swing.table.*;

import open.dolphin.plugin.*;
import open.dolphin.plugin.event.*;
import open.dolphin.project.*;
import open.dolphin.table.*;
import open.dolphin.util.*;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.beans.*;

/**
 * CLAIM ���M�T�[�r�X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class SendClaimService extends AbstractFramePlugin implements ClaimMessageListener {
    
    // Socket constants
    private final int EOT                   = 0x04;
    private final int ACK                   = 0x06;
    private final int NAK                   = 0x15;
    private final String UTF8               = "UTF8";
    
    // Alert constants
    private final int TT_QUEUE_SIZE         = 0;
    private final int TT_NAK_SIGNAL         = 1;
    private final int TT_SENDING_TROUBLE    = 2;
    private final int TT_CONNECTION_REJECT  = 3;
    
    // Strings
    private final String watingString       = "���M�����܂ő҂�";
    private final String proceedString      = "�p��";
    private final String dumpString         = "�t�@�C���֋L�^";
    
    // Properties
    private LinkedList queue;    
    private String host;  
    private int port;                       //5001;    
    private String enc;    
    private int watingTime = 10; //3*60;    // 3 minuets. 
    private int sleepTime = 30;             // 30 sec.
    private int alertQueueSize = 10;
    private String journalDir = "CLAIM-JOURNALS";
    private ITrace trace;
    private boolean DEBUG;
    
    private Kicker kicker;
    
    // ���M���X�g�e�[�u���̃J������
    private String[] columnNames = {
        "��������", "����ID", "���Ҏ���", "����", "�����^�C�g��", "���M��","����","�_���v"
    };
    private final int SENT_COLUMN   = 5;
    private final int ACKNAK_COLUMN = 6;
    private final int DUMP_COLUMN   = 7;
    
    // ���M���X�g�e�[�u���̃J������
    private int[] columnWidth = {150, 100, 100, 30, 200, 50, 50, 50};
    
    // ���M�e�[�u�����f��
    private ArrayListTableModel tableModel;    

    /** Creates new ClaimQue */
    public SendClaimService() {
        String name = Project.getName();
        if (name.equals("debug")) {
            DEBUG = true;
        }
    }
    
    public void initComponent() {
		JPanel ui = createUI();
		Dimension size = new Dimension(710, 340);
		centerFrame(size, ui);    	
    }
    
    public void start() {
        
        host = Project.getClaimAddress();  
        port = Project.getClaimPort(); //5001;    
        enc = Project.getClaimEncoding(); 
    
        queue = new LinkedList();
        kicker = new Kicker();
        kicker.start();
    }
    
    public void stop() {
        if (kicker != null) {
            kicker.interrupt();            
        }
        super.stop();    
    } 
    
    public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
        
        String prop = e.getPropertyName();
        if (prop.equals("loginProp")) {
            boolean b = ((Boolean)e.getNewValue()).booleanValue();
            this.setVisible(b);
            
        } else if (prop.equals("exitProp")) {
            
            boolean b = ((Boolean)e.getNewValue()).booleanValue();
            
            if (! b) {
                return;
            }
                
            // Queue �Ɏc���Ă��邩
            /*int count = getQueueSize();
            if (count == 0) {
                //frame.setVisible(false);
                //frame.dispose();
            }
            else {
                int option = alertExitDialog(count);
                if (option == 0) {
                    throw new PropertyVetoException("�����M��CLAIM�f�[�^������܂�",e);
                }
                else {
                    synchronized(this) {
                        dump();
                    }
                }
            }*/
        }
    }
    
    private int alertExitDialog(int count) {

        int option = -1;
        String title = "Dolphin: CLAIM ���M";
        StringBuffer buf = new StringBuffer();

        buf.append("�����M��CLAIM(���Z�v�g)�f�[�^��");
        buf.append(count);
        buf.append(" ����܂��B\n");
        buf.append("1. ���΂炭�҂��Ă��������x�I�����������Ă��������B\n");
        buf.append("2. �����M�f�[�^���t�@�C���ɋL�^���A�����I�����邱�Ƃ��ł��܂��B\n");
        buf.append("   ���̏ꍇ�A�f�[�^�͑��M���ꂸ�A�f�Õ�V�͎���͂ƂȂ�܂��B");

        option = JOptionPane.showOptionDialog(
                                              null, 
                                              buf.toString(),
                                              title,
                                              JOptionPane.DEFAULT_OPTION,
                                              JOptionPane.WARNING_MESSAGE,null,
                                               new String[]{watingString, dumpString},proceedString);
                   
        return option;
    }
    
    private JPanel createUI() {
        
        tableModel = new ArrayListTableModel(columnNames, 10) {
            
            public boolean isCellEditable(int row, int col) {
                return false;
            }
            
            public Class getColumnClass (int col) {
                return (col == SENT_COLUMN || col == DUMP_COLUMN) ? java.lang.Boolean.class : java.lang.String.class;
            }
        };
        JTable table = new JTable(tableModel);
        JScrollPane scroller = new JScrollPane(table, 
                                   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                   JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        TableColumn column = null;
        int len = columnNames.length;
        for (int i = 0; i < len; i++) {
            column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(columnWidth[i]);
        }
        //table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scroller, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        return panel;
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
    
    public String getEncoding() {
        return enc;
    }
    
    public void setEncoding(String enc) {
        this.enc = enc;
    }
    
    public void setTrace(ITrace t) {
        this.trace = t;
    }
    
    public int getWatingTime() {
        return watingTime;
    }
    
    public void setWatingTime(int val) {
        watingTime = val;
    }
    
    public String getJournalDirectory() {
        return journalDir;
    }
    
    public void setJournalDirectory(String val) {
        journalDir = val;
    }
    
    public int getAlertQueueSize() {
        return alertQueueSize;
    }
    
    public void getAlertQueueSize(int val) {
        alertQueueSize = val;
    }
    
    public synchronized void claimMessageEvent(ClaimMessageEvent e) {
        
        queue.addLast(e);
        String id = e.getPatientId();
        String name = e.getPatientName();
        String sex = e.getPatientSex();
        String title = e.getTitle();
        String instance = e.getClaimInsutance();
        String timeStamp = e.getConfirmDate();
        Boolean b = new Boolean("false");
        tableModel.addRow(new Object[]{timeStamp, id, name, sex, title, b, null, b});
        e.setNumber(tableModel.getDataCount() - 1);
        
        // Queue �ɗ��܂肷���Ă���ꍇ�̌x��
        if ( (queue.size() % alertQueueSize) == 0) {
            
            int option = alertDialog(TT_QUEUE_SIZE);
            if (option == 1) {
                dump();
            }
            else {
                notify();
            }
        }
        else {
            notify();
        }
    }
    
    public synchronized Object getCLAIM() throws InterruptedException {        
       while (queue.size() == 0) {
          wait();
       }
       return queue.removeFirst();
    }
    
    public synchronized int getQueueSize() {
        return queue.size();
    }
    
    public void dump() {        
        int size = queue.size();
        if (size == 0) {
            return;
        }
        
        String fileName = getDateTime();
        
        for (int i = 0; i < size; i++) {
            try {
                ClaimMessageEvent evt = (ClaimMessageEvent)queue.removeFirst();
                int num = evt.getNumber();
                
                StringBuffer buf = new StringBuffer();
                buf.append(fileName);
                buf.append("-");
                buf.append(i);
                writeJournal(buf.toString(), evt.getClaimInsutance());
                setSentColumn(num, DUMP_COLUMN);
            }
            catch (Exception e) {
                System.out.println("Exception while dumping the CLAIM data: " + e.toString());
            }
        }
    }
    
    private void log(String msg) {
        if (trace != null) {
            trace.debug(msg);
        }
    }

    private void error(String msg) {
        if (trace != null) {
            trace.error(msg);
        }
    }

    private int alertDialog(int code) {

        int option = -1;
        String title = "Dolphin: CLAIM ���M";
        StringBuffer buf = null;
        
        switch(code) {
            
            case TT_QUEUE_SIZE:
                buf = new StringBuffer();
                buf.append("�����M��CLAIM(���Z�v�g)�f�[�^��");
                buf.append(getQueueSize());
                buf.append(" ����܂��BCLAIM �T�[�o�̓d���y�ѐڑ����m�F���Ă��������B\n");
                buf.append("1. ���̂܂܏������p�����邱�Ƃ��ł��܂��B\n");
                buf.append("2. �����M�f�[�^���t�@�C���ɋL�^���邱�Ƃ��ł��܂��B\n");
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
                buf.append("���M���̃f�[�^�̓t�@�C���ɋL�^���܂��B�f�Õ�V�̎������͂͂ł��܂���B");
                JOptionPane.showMessageDialog(
                        null, 
                        buf.toString(),
                        title,
                        JOptionPane.ERROR_MESSAGE);
                break;

            case TT_SENDING_TROUBLE:
                buf = new StringBuffer();
                buf.append("CLAIM(���Z�v�g)�f�[�^�̑��M���ɃG���[�������܂����B\n");
                buf.append("���M���̃f�[�^�̓t�@�C���ɋL�^���܂��B�f�Õ�V�̎������͂͂ł��܂���B");
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
                buf.append(" �� ");
                buf.append(watingTime);
                buf.append(" �b�ȏ㉞�����܂���B�T�[�o�̓d���y�ѐڑ����m�F���Ă��������B\n");
                buf.append("1. ���̂܂ܐڑ���҂��Ƃ��ł��܂��B\n");
                buf.append("2. �f�[�^���t�@�C���ɋL�^���邱�Ƃ��ł��܂��B\n");
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

    // Records error & data to the error-file
    private void writeJournal(String fileName, String data) {

        try {
            //String path = DolphinContext.getInstalledDirectory();
            String path = ClientContext.getUserDirectory();
            path = path + "/" + journalDir;
            File f = new File(path);
            if (! f.exists()) {
                f.mkdirs();
            }
            path = path + "/" + fileName + ".xml";
            f = new File(path);
            BufferedOutputStream  writer = new BufferedOutputStream(new FileOutputStream(f));
            writer.write(data.getBytes(enc));
            writer.flush();
            writer.close();
        }
        catch (Exception e) {
            error("Exception while writing " + journalDir + ": " + e.toString());
        }
    }

    private String getDateTime() {       
        return MMLDate.getDateTime("yyyy-MM-dd-hh-mm-ss");
    }
    
    protected class Kicker extends Thread {
        
        public void run() {
            
            try {
                Socket socket = null;
                BufferedOutputStream writer;
                BufferedInputStream reader;

                String instance = null;
                int num;

                while (! interrupted()) {

                    try {                     
                        // CLAIM Event ���擾                  
                        ClaimMessageEvent e = (ClaimMessageEvent)getCLAIM();
                        instance = e.getClaimInsutance();
                        num = e.getNumber();

                        // Gets connection                
                        socket = null;
                        /*if ( DEBUG || (socket = getSocket()) == null ) {
                            String fileName = getDateTime() + "-1";
                            writeJournal(fileName, instance);
                            setSentColumn(num, DUMP_COLUMN);
                            continue;
                        }*/
                        if ( (socket = getSocket()) == null ) {
                            String fileName = getDateTime() + "-1";
                            writeJournal(fileName, instance);
                            setSentColumn(num, DUMP_COLUMN);
                            continue;
                        }
                        //log("got claim socket");

                        // Gets io stream
                        writer = new BufferedOutputStream(new DataOutputStream(socket.getOutputStream()));
                        reader = new BufferedInputStream(new DataInputStream(socket.getInputStream()));

                        // Writes UTF8 data
                        writer.write(instance.getBytes(enc));
                        writer.write(EOT);
                        writer.flush();
                        setSentColumn(num, SENT_COLUMN);
                        //log("sent claim data");

                        // Reads result
                        int c = reader.read();
                        if (c == ACK) {
                            setResultColumn(num,true);
                            //log("recieved ACK, transaction succeded");
                        }
                        else if (c == NAK) {
                            //log("recieved NAK, transaction failed. recordes the data to the erro-log");
                            //alertDialog(TT_NAK_SIGNAL);
                            setResultColumn(num,false);
                            String fileName = "error-" + getDateTime() + "-1";
                            writeJournal(fileName, instance);
                        }
                        socket.close();
                    }
                    catch (IOException e) {
                        alertDialog(TT_SENDING_TROUBLE);
                        if (instance != null) {
                            String fileName = getDateTime() + "-1";
                            writeJournal(fileName, instance);
                        }
                    }
                }
            }
            catch (InterruptedException ie) {

            }
        }
        
        private Socket getSocket() throws InterruptedException {
            
            Socket s = null;
            long enter = System.currentTimeMillis();
            
            while (true) {
                
                try {
                    s = new Socket(host, port);
                    break;
                    
                } catch (IOException e) {
                    
                    long failed = System.currentTimeMillis();
                    
                    if ( ((failed - enter)/1000) > watingTime ) {
                        
                        int option = alertDialog(TT_CONNECTION_REJECT);
                        if (option == 1) {
                            s = null;
                            break;
                            
                        } else {
                            enter = System.currentTimeMillis();
                        }
                    }
                
                    // Sleep a while
                    sleep(sleepTime*1000);
                    
                }
            }
            return s;
        }
    }
    
    private void setSentColumn(int row, int col) {
        Object[] o = tableModel.getRowData(row);
        if (o != null) {
            o[col] = new Boolean("true");
            tableModel.fireTableCellUpdated(row, col);
        }
    }
    
    private void setResultColumn(int row, boolean b) {
        Object[] o = tableModel.getRowData(row);
        if (o != null) {
            String result = b ? "ACK" : "NAK";
            o[ACKNAK_COLUMN] = result;
            tableModel.fireTableCellUpdated(row, ACKNAK_COLUMN);
        }
    }
}