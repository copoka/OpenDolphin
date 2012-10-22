/*
 * DirectoryWatcher.java
 *
 * Created on 2001/08/25, 23:50
 */

package jp.ac.kumamoto_u.kuh.fc.jsato.swing_beans;

import javax.swing.*;
import java.io.*;
import java.io.File;
import java.lang.System;
import java.lang.Integer;
import java.util.Vector;

import netscape.ldap.*;
import netscape.ldap.beans.*;
/**
 *
 * @author  Junzo SATO
 * @copyright   Copyright (c) 2001, Junzo SATO. All rights reserved.
 */

public class LaboTestReceiver extends javax.swing.JFrame {

    /** Creates new form DirectoryWatcher */
    public LaboTestReceiver() {
        initComponents();
        pack();
        setLocation(10,10);
        foundFiles = new Vector();
        fileProcessor = new LaboTestProcessor(statusBean1);
    }

    public LaboTestReceiver(String args[], int firstArg) {
        boot(args, firstArg);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        pnlMain = new javax.swing.JPanel();
        pnlDirectory = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        tfDirToWatch = new javax.swing.JTextField();
        btnChoose = new javax.swing.JButton();
        pnlExtRefs = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        tfExtRefs = new javax.swing.JTextField();
        btnExtRefs = new javax.swing.JButton();
        pnlThird = new javax.swing.JPanel();
        pnlLDAP = new javax.swing.JPanel();
        lDAPLoginBean1 = new jp.ac.kumamoto_u.kuh.fc.jsato.swing_beans.LDAPLoginBean();
        btnAuthenticate = new javax.swing.JButton();
        pnlStart = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        tfTime = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        btnStartStop = new javax.swing.JButton();
        statusBean1 = new jp.ac.kumamoto_u.kuh.fc.jsato.swing_beans.StatusBean();
        
        setTitle("LaboTestReceiver");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
        
        pnlMain.setLayout(new javax.swing.BoxLayout(pnlMain, javax.swing.BoxLayout.Y_AXIS));
        
        pnlMain.setPreferredSize(new java.awt.Dimension(1010, 676));
        pnlDirectory.setMaximumSize(new java.awt.Dimension(32767, 38));
        pnlDirectory.setMinimumSize(new java.awt.Dimension(131, 38));
        pnlDirectory.setPreferredSize(new java.awt.Dimension(1010, 38));
        jLabel1.setText("Directory To Watch:");
        pnlDirectory.add(jLabel1);
        
        tfDirToWatch.setText("/dolphin/mmlLb/incoming/");
        tfDirToWatch.setPreferredSize(new java.awt.Dimension(600, 20));
        tfDirToWatch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfDirToWatchActionPerformed(evt);
            }
        });
        
        pnlDirectory.add(tfDirToWatch);
        
        btnChoose.setText("Choose...");
        btnChoose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChooseActionPerformed(evt);
            }
        });
        
        pnlDirectory.add(btnChoose);
        
        pnlMain.add(pnlDirectory);
        
        pnlExtRefs.setMaximumSize(new java.awt.Dimension(32767, 38));
        pnlExtRefs.setMinimumSize(new java.awt.Dimension(131, 38));
        pnlExtRefs.setPreferredSize(new java.awt.Dimension(1010, 38));
        jLabel4.setText("Directory For External References: ");
        pnlExtRefs.add(jLabel4);
        
        tfExtRefs.setText("/dolphin/mmlLb/extRefs/");
        tfExtRefs.setPreferredSize(new java.awt.Dimension(600, 20));
        tfExtRefs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfExtRefsActionPerformed(evt);
            }
        });
        
        pnlExtRefs.add(tfExtRefs);
        
        btnExtRefs.setText("Choose...");
        btnExtRefs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExtRefsActionPerformed(evt);
            }
        });
        
        pnlExtRefs.add(btnExtRefs);
        
        pnlMain.add(pnlExtRefs);
        
        pnlThird.setLayout(new javax.swing.BoxLayout(pnlThird, javax.swing.BoxLayout.X_AXIS));
        
        pnlLDAP.setPreferredSize(new java.awt.Dimension(410, 600));
        pnlLDAP.add(lDAPLoginBean1);
        
        btnAuthenticate.setText("Authenticate");
        btnAuthenticate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAuthenticateActionPerformed(evt);
            }
        });
        
        pnlLDAP.add(btnAuthenticate);
        
        pnlStart.setBorder(new javax.swing.border.TitledBorder("Frequency"));
        pnlStart.setMaximumSize(new java.awt.Dimension(32767, 38));
        pnlStart.setMinimumSize(new java.awt.Dimension(410, 60));
        pnlStart.setPreferredSize(new java.awt.Dimension(410, 60));
        jLabel2.setText("Watch every");
        pnlStart.add(jLabel2);
        
        tfTime.setText("90000");
        tfTime.setPreferredSize(new java.awt.Dimension(100, 20));
        pnlStart.add(tfTime);
        
        jLabel3.setText("milliseconds.");
        pnlStart.add(jLabel3);
        
        pnlLDAP.add(pnlStart);
        
        btnStartStop.setText("Start");
        btnStartStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartStopActionPerformed(evt);
            }
        });
        
        pnlLDAP.add(btnStartStop);
        
        pnlThird.add(pnlLDAP);
        
        pnlThird.add(statusBean1);
        
        pnlMain.add(pnlThird);
        
        getContentPane().add(pnlMain, java.awt.BorderLayout.NORTH);
        
    }//GEN-END:initComponents

    private void tfExtRefsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfExtRefsActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_tfExtRefsActionPerformed

    private void btnExtRefsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExtRefsActionPerformed
        // Add your handling code here:
        JFileChooser chooser = new JFileChooser("./");
        if (chooser == null) return;
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        String path;
        int selected = chooser.showOpenDialog(this.getContentPane());
        if (selected == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            path = file.getPath();
            tfExtRefs.setText(path);
            statusBean1.printlnStatus("Selected: " + path);
            return;
        } else if (selected == JFileChooser.CANCEL_OPTION) {
            return;
        }
    }//GEN-LAST:event_btnExtRefsActionPerformed

    private void btnAuthenticateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAuthenticateActionPerformed
        // Add your handling code here:
        // update properties in the connection panel
        lDAPLoginBean1.updateProperties();

        // suthenticate using properties in the panel
        LDAPSimpleAuth auth = new LDAPSimpleAuth();
        auth.setHost(lDAPLoginBean1.getHost());
        auth.setPort(lDAPLoginBean1.getPort());
        auth.setAuthDN(lDAPLoginBean1.getBindDN());
        auth.setAuthPassword(lDAPLoginBean1.getPassword());
        String res = auth.authenticate();
        if (res == "N") {
            statusBean1.printlnStatus("Authenticaton failed:( Check your configurations!");
        } else {
            statusBean1.printlnStatus("Authentication succeeded:)");
        }
        validate();
    }//GEN-LAST:event_btnAuthenticateActionPerformed

    private void btnChooseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChooseActionPerformed
        // Add your handling code here:
        JFileChooser chooser = new JFileChooser("./");
        if (chooser == null) return;
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        String path;
        int selected = chooser.showOpenDialog(this.getContentPane());
        if (selected == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            path = file.getPath();
            tfDirToWatch.setText(path);
            statusBean1.printlnStatus("Selected: " + path);
            return;
        } else if (selected == JFileChooser.CANCEL_OPTION) {
            return;
        }
    }//GEN-LAST:event_btnChooseActionPerformed

  private void tfDirToWatchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfDirToWatchActionPerformed
// Add your handling code here:
  }//GEN-LAST:event_tfDirToWatchActionPerformed

  private void btnStartStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartStopActionPerformed
// Add your handling code here:
      if (watchingThread != null) {
          btnStartStop.setText("Start");
          statusBean1.printlnStatus("Stopping thread...");
          watchingThread.interrupt();
          watchingThread = null;
      } else {
          btnStartStop.setText("Stop");
          
          statusBean1.clearStatus();
          
          statusBean1.printlnStatus("Starting thread...");
          watchingThread = new WatchingThread();
          watchingThread.start();
      }
  }//GEN-LAST:event_btnStartStopActionPerformed

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit (0);
    }//GEN-LAST:event_exitForm

    public void boot(String args[], int firstArg) { 
        int n = args.length - firstArg;
        if (n != 8) {
            System.err.println("Usage: java Exec [-g] incoming extrefs host port basedn binddn passwd period");
            return;
        }

        //for (int i = firstArg; i < args.length; i++) {
        //    System.out.println(args[i]);
        //}
        
        int i = firstArg;
        String incoming = args[i]; ++i;
        String extrefs = args[i]; ++i;
        String host = args[i]; ++i;
        String port = args[i]; ++i;
        String basedn = args[i]; ++i;
        String binddn = args[i]; ++i;
        String passwd = args[i]; ++i;
        String period = args[i];
        //
        foundFiles = new Vector();
        fileProcessor = new LaboTestProcessor(null);
        //
        // suthenticate using properties in the panel
        LDAPSimpleAuth auth = new LDAPSimpleAuth();
        auth.setHost(host);
        auth.setPort(Integer.parseInt(port));
        auth.setAuthDN(binddn);
        auth.setAuthPassword(passwd);
        String res = auth.authenticate();
        if (res == "N") {
            System.out.println("Authenticaton failed:-( Check your configurations!");
            return;
        } else {
            System.out.println("Authentication succeeded:-)");
        }
        
        /////////////////////////////
        // Start Watching here....
        /////////////////////////////
    }
    
    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        if (true) {
            new LaboTestReceiver().setVisible(true);
            return;
        }
        
        if (args.length < 1) {
            System.err.println("Usage: java Exec [-g] incoming extrefs host port basedn binddn passwd period");
            return;
        }

        int firstArg = 0;
        int mode = 0;
        
        if (args[0].startsWith("-")) {
            firstArg = 1;
            if (args[0].equals("-g")) mode = 1;
        }

        if (mode == 1) {
            new LaboTestReceiver().setVisible(true);
        } else {
            new LaboTestReceiver(args, firstArg);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlDirectory;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField tfDirToWatch;
    private javax.swing.JButton btnChoose;
    private javax.swing.JPanel pnlExtRefs;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField tfExtRefs;
    private javax.swing.JButton btnExtRefs;
    private javax.swing.JPanel pnlThird;
    private javax.swing.JPanel pnlLDAP;
    private jp.ac.kumamoto_u.kuh.fc.jsato.swing_beans.LDAPLoginBean lDAPLoginBean1;
    private javax.swing.JButton btnAuthenticate;
    private javax.swing.JPanel pnlStart;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField tfTime;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton btnStartStop;
    private jp.ac.kumamoto_u.kuh.fc.jsato.swing_beans.StatusBean statusBean1;
    // End of variables declaration//GEN-END:variables
    
    String srcDir = null;
    String extRefsDir = null;

    private void watchDirectory() {
        // Firstly, check the existence of the target directory to watch.
        srcDir = tfDirToWatch.getText();
        File targetDir = new File(srcDir);
        if (targetDir.exists() == false) {
            statusBean1.printlnStatus("Target directory doesn't exist.");
            // Create new directory with specified name.
            if (targetDir.mkdirs() == true) {
                statusBean1.printlnStatus("Target directory was created.");
            }
            // Check the readability of the directory
            if (targetDir.isDirectory() == false || targetDir.canRead() == false) {
                statusBean1.printlnStatus("Couldn't read target directory.");
             }
            // Bail out so far, because this directory has no file in it.
            return;
        }
        
        // check extRefs directory too.
        extRefsDir = tfExtRefs.getText();
        File extDir = new File(extRefsDir);
        if (extDir.exists() == false) {
            statusBean1.printlnStatus("extRefs directory doesn't exist.");
            // Create new directory with specified name.
            if (extDir.mkdirs() == true) {
                statusBean1.printlnStatus("extRefs directory was created.");
            }
            // Check the readability of the directory
            if (extDir.isDirectory() == false || extDir.canRead() == false) {
                statusBean1.printlnStatus("Couldn't create extRefs directory.");
             }
            return;            
        }
        
        statusBean1.clearStatus();//////////////

        // Traverse target directory to find files in it.
        String flist[] = targetDir.list();
        for (int i=0; i < flist.length; ++i) {
            // case insensitive
            if ( flist[i].toLowerCase().endsWith("xml") == false ) {
                continue;
            }
            
            File file = new File(targetDir.getPath(),flist[i]);
            if (file.isFile() == false) {
                // target object is not a file
                // skip it
                continue;
            }
            
            statusBean1.printlnStatus("Found XML file: " + file.getName());
            foundFiles.addElement(file);
        }
    }
    
    private void processFiles() {
        if (foundFiles.size() <= 0) {
            statusBean1.printlnStatus("Nothing was found so far... Maybe next time.");
            isBusy = false;
            return;
        }
        
        isBusy = true;
        
        // Process Files
        while (foundFiles.size() > 0) {            
            File f = (File)foundFiles.elementAt(0);
            
            statusBean1.printlnStatus("---------------------------------------------------------------------");
            //statusBean1.printlnStatus(f.toString());
            //
            // process this file
            fileProcessor.processFile(f, lDAPLoginBean1, srcDir, extRefsDir);
            
            // delete this file from the directory
            // I'm not interested in processed file anymore even if some error occurred.
            try {
                f.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // remove this file from the list
            foundFiles.removeElementAt(0);
            statusBean1.printlnStatus("Processing current file done...\n");
        }
        
        isBusy = false;
    }
    
    private void doWork() {
        //statusBean1.printlnStatus("\n\nWorking...");
        watchDirectory();
        processFiles();
    }
    
    private class WatchingThread extends Thread {
        private int millis;
        
        public WatchingThread() {
            super();
        }
        
        public void run() {
            millis = Integer.parseInt(tfTime.getText());
            doWork();
            while (true) {
                long last = System.currentTimeMillis();
                while (true) {
                    if ((System.currentTimeMillis() - last) > millis) {
                        if (isBusy == false) {
                            doWork();
                        } else {
                            statusBean1.printlnStatus("\n\nI'm busy now...");
                        }
                        yield();
                        break;
                    }
                }
	    }
        }
    }

    WatchingThread watchingThread = null;
    Vector foundFiles = null;
    LaboTestProcessor fileProcessor = null;
    boolean isBusy = false;
}
