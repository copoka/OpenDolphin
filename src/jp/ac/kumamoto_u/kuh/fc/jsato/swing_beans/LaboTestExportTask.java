/*
 * LaboTestExportTask.java
 *
 * Created on 2001/12/10, 9:03
 */

package jp.ac.kumamoto_u.kuh.fc.jsato.swing_beans;

import swingworker.*;
import netscape.ldap.*;
import java.util.*;

import java.io.*;

import open.dolphin.project.*;

/**
 *
 * @author  Junzo SATO
 * @version 
 */
public class LaboTestExportTask {
    private int lengthOfTask;
    private int current = 0;
    private String statMessage;

    private FileWriter fw = null;
    
    private String patientId = null;
    private boolean isLocalId = false;
    private String fromDate = null;
    private String toDate = null;

    /** Creates new LaboTestExportTask */
    public LaboTestExportTask(
        FileWriter fw,
        String patientId, boolean isLocalId,
        String fromDate, String toDate ) {
        
        this.fw = fw;
        
        this.patientId = patientId;
        this.isLocalId = isLocalId;
        this.fromDate = fromDate;
        this.toDate = toDate;
        
        //Compute length of task...
        lengthOfTask = 1000;
    }

    /**
     * Called to start the task.
     */
    void go() {
        current = 0;
        final SwingWorker worker = new SwingWorker() {
            public Object construct() {
                return new ActualTask();
            }
        };
        worker.start();
    }

    /**
     * Called to find out how much work needs to be done.
     */
    int getLengthOfTask() {
        return lengthOfTask;
    }

    /**
     * Called to find out how much has been done.
     */
    int getCurrent() {
        return current;
    }

    void stop() {
        current = lengthOfTask;
    }

    /**
     * Called to find out if the task has completed.
     */
    boolean done() {
        if (current >= lengthOfTask)
            return true;
        else
            return false;
    }

    String getMessage() {
        return statMessage;
    }

    /**
     * The actual long running task.  This runs in a SwingWorker thread.
     */
    class ActualTask {
        String host = "";
        int port = 389;
        String bindDN = "";
        String passwd = "";
        
        String baseDN = "o=Dolphin";
        String laboTestDN = "cn=LaboTest," + baseDN;
        String patientDN = null;
        
        Vector row = null;
        
        ActualTask() {
            //ServerConnection sc = Karte.getServerConnection();
            //if (sc != null) {
                host = Project.getHostAddress();
                port = Project.getHostPort();
                bindDN = Project.getAuthenticationDN();
                passwd = Project.getPasswd();
            //} else {  
            if (host == null || bindDN == null || passwd == null) {
                statMessage = "�T�[�o�[�ݒ�̎擾�Ɏ��s���܂����B";
                try {Thread.sleep(100);} catch (Exception e){}
                stop();
                return;
            }
            
            statMessage = "�T�[�o�[�֐ڑ���...";
            try {Thread.sleep(100);} catch (Exception e){}

            LDAPConnection ld = getConnection();
            if (ld == null) {
                statMessage = "�T�[�o�[�ڑ��Ɏ��s���܂����B";
                try {Thread.sleep(100);} catch (Exception e){}
                stop();
                return;
            }

            if (AsyncAuthenticate(ld) == false) {
                return;
            }
                        
            patientDN = searchPatient();
            if (patientDN == null) {
                statMessage = "�Y������f�[�^�͂���܂���B";
                try {Thread.sleep(100);} catch (Exception e){}
                stop();
                return;
            }

            //==========================================================================
            // search laboModules which have laboSampleTime between fromDate and toDate.
            try {
                outputLaboModules(fw);
            } catch (IOException ie) {
                statMessage = "�t�@�C�������o���Ɏ��s���܂����B";
                try {Thread.sleep(100);} catch (Exception ee){}
                stop();
                return;
            }
            
            //==========================================================================
            // notify the successful message
            // NOTE: it is very important that this message is received by the 
            // searchTimer in LaboTestBean to check the result of this ActualTask
            statMessage = "�t�@�C�������o�����I�����܂����B";
            try {Thread.sleep(100);} catch (Exception ee){}
            stop();
        }

        // ldap connection 
        private LDAPConnection conn = null;// never use conn directly
        
        public LDAPConnection getConnection() {
            if (conn != null && conn.isConnected()) {
                return conn;
            }

            try {
                conn = new LDAPConnection();
                conn.connect( host, port, bindDN, passwd );
                return conn;
            } catch (LDAPException e) {
                System.out.println(e.toString());
                return null;
            }
        }

        public void disconnectLDAP() {
            if (conn != null && conn.isConnected()) {
                try {
                    conn.disconnect();
                    conn = null;
                } catch (LDAPException e) {
                    System.out.println(e.toString());
                }
            }
        }        
        
        // authentication
        public boolean AsyncAuthenticate(LDAPConnection ld) {          
            try {
                LDAPResponseListener r = ld.bind(
                        bindDN,//"uid=directorymanager,ou=Managers,o=Dolphin",
                        passwd,//"secret",
                        (LDAPResponseListener)null);
                // Wait until it completes
                while ( r.isResponseReceived() == false ) {
                    statMessage = "�T�[�o�[����̉�����҂��Ă��܂�...";
                    try {Thread.sleep(100);} catch (Exception e){}
                }
                LDAPResponse response = r.getResponse();
                // Did the authentication succeed?
                int resultCode = response.getResultCode();
                if (resultCode != LDAPException.SUCCESS) {
                    // Do what the synchronous interface does - throw
                    // an exception
                    String err = LDAPException.errorCodeToString( resultCode );
                    statMessage = err;
                    throw new LDAPException( err,
                                               resultCode,
                                               response.getErrorMessage(),
                                               response.getMatchedDN()
                    );
                } else {
                    statMessage = "�T�[�o�[�F�؂ɐ������܂����B";
                    try {Thread.sleep(100);} catch (Exception eee){}
                }
                return true;
            } catch (LDAPException le) {
                le.printStackTrace();
                statMessage = "�T�[�o�[�F�؂Ɏ��s���܂����B";
                try {Thread.sleep(100);} catch (Exception ee){}
                stop();
                return false; 
            }
        }
        
        // search
        public String searchPatient() {
            String dn = laboTestDN;
            String facilityOrLocal = "";
            if ( isLocalId ) {
                facilityOrLocal = "local";
            } else {
                facilityOrLocal = "facility";
            }

            String pid = facilityOrLocal + "__" + patientId.replaceAll(",","");
            String filter = "(mmlPid=" + pid + ")";
            filter = filter.replaceAll("-","");
            filter = filter.replaceAll(":","");

            return searchDN(dn, filter);
        }

        public void outputLaboModules(FileWriter fw) throws IOException {
            // get connection
            LDAPConnection ld = getConnection();
            if (ld == null) {
                statMessage = "�T�[�o�[�ڑ��Ɏ��s���܂����B";
                try {Thread.sleep(100);} catch (Exception e){}
                stop();
                return;
            }

            String dn = patientDN;        
            String filter = "(&(objectclass=laboModule)(&(laboSampleTime>=" + 
                fromDate + "T00:00:00)(laboSampleTime<=" + toDate + "T23:59:59)))";

            String[] attrs = {
                "laboSampleTime","mmlConfirmDate","laboTestReporter","laboCreatorLicense","laboTestCenterId","laboTestCenterName",
                "laboTestDepartmentId","laboTestDepartmentName","laboAddress","mail","laboPhone",
                "laboRegistId","laboRegistTime","laboReportTime","laboReportStatus","laboReportMemo","laboReportFreeMemo",
                "laboSetCode","laboSetCodeId","laboSet"
            };

            String[] names = {
                "���̍̎����","�m���","�񍐎�","�񍐎Ҏ��i","�������{�{�݂h�c","�������{�{��",
                "�������{�{�ݕ����h�c","�������{�{�ݕ���","�Z��","�d�q���[��","�d�b�ԍ�",
                "�o�^�h�c","�o�^����","�񍐓���","�񍐏��","�񍐃���","�񍐎��R����",
                "�Z�b�g�R�[�h","�Z�b�g�e�[�u��","�Z�b�g��"
            };

            String[] sortattrs = {
                "laboSampleTime","laboRegistId"
            };
            boolean[] ascend = {
                true, true
            };

            LDAPSearchResults results = null;
            try {
                statMessage = "�f�[�^������...";
                try {Thread.sleep(100);} catch (Exception ee){}

                results = ld.search(dn, netscape.ldap.LDAPConnection.SCOPE_ONE, filter, attrs, false);
                if (results != null) {
                    results.sort(new LDAPCompareAttrNames(sortattrs, ascend));
                }
            } catch (LDAPException e) {
                statMessage = "�Y������f�[�^�͂���܂���B";
                try {Thread.sleep(100);} catch (Exception ee){}
                stop();
                return;
            }

            // handle laboModules
            while ( results.hasMoreElements() ) {
                LDAPEntry entry = null;
                try {
                    entry = (LDAPEntry)results.next();
                } catch (LDAPException e) {
                    continue;
                }

                // get each entry of the laboModule for this patient
                String moduleDN = entry.getDN();
                //-----------------------------------------------------
                for (int i = 0; i < attrs.length; ++i) {
                    statMessage = names[i];
                    try {Thread.sleep(100);} catch (Exception ee){}

                    LDAPAttribute attr = entry.getAttribute(attrs[i]);
                    if (attr == null) {
                        fw.write(names[i] + "," + "\"\"" + "\n");
                        continue;
                    }

                    //===================================================
                    if (attrs[i].equals("laboSampleTime")) {
                    // HEADER //
                        String st = (String)attr.getStringValues().nextElement();
                        fw.write(names[i] + "," + st + "\n");
                    } else {
                    // COLUMN //
                        // get values of attribute attrs[i] if the attribute has multiple values.
                        Enumeration enumVals = attr.getStringValues();
                        fw.write(names[i]);
                        while ( (enumVals != null) && enumVals.hasMoreElements() ) {
                            String es = (String)enumVals.nextElement();
                            if (es != null) {
                                fw.write("," + es);
                            } else {
                                fw.write("," + "\"\"");
                            }
                        }
                        fw.write("\n");
                    }
                    //===================================================
                }
                fw.flush();

                //-----------------------------------------------------
                // go deeper...never forget to add result to the row! :-)
                outputLaboTests(fw, moduleDN);
            }
        }

        public void outputLaboTests(FileWriter fw, String moduleDN) throws IOException {
            // get connection
            LDAPConnection ld = getConnection();
            if (ld == null) {
                //printlnMessage("*** Couldn't search DN. Connection is null.");
                return;
            }

            String dn = moduleDN;        
            String filter = "(objectclass=laboTest)";

            String[] attrs = {
                "laboSpecimenName","laboSpecimenCode","laboSpecimenCodeId","laboSpecimenMemo","laboSpecimenFreeMemo"
            };
             String[] names = {
                "����","���̃R�[�h","���̃e�[�u��","���̃���","���̎��R����"
            };

            String[] sortattrs = {
                "laboSpecimenCodeId","laboSpecimenCode"
            };
            boolean[] ascend = {
                true, true
            };

            LDAPSearchResults results = null;
            try {
                statMessage = "�f�[�^������...";
                try {Thread.sleep(100);} catch (Exception ee){}

                results = ld.search(dn, netscape.ldap.LDAPConnection.SCOPE_ONE, filter, attrs, false);
                if (results != null) {
                    results.sort(new LDAPCompareAttrNames(sortattrs, ascend));
                }
            } catch (LDAPException e) {
                statMessage = "�Y������f�[�^�͂���܂���B";
                try {Thread.sleep(100);} catch (Exception ee){}
                stop();
                return;
            }

            while ( results.hasMoreElements() ) {
                LDAPEntry entry = null;
                try {
                    entry = (LDAPEntry)results.next();
                } catch (LDAPException e) {
                    continue;
                }

                // get each entry of the laboModule for this patient
                String testDN = entry.getDN();

                int len = attrs.length;
                for (int i = 0; i < len; ++i) {
                    statMessage = names[i];
                    try {Thread.sleep(100);} catch (Exception ee){}

                    LDAPAttribute attr = entry.getAttribute(attrs[i]);
                    if (attr == null) {
                        fw.write(names[i] + "," + "\"\"" + "\n");
                        continue;
                    }

                    // get values of attribute attrs[i]
                    Enumeration enumVals = attr.getStringValues();
                    fw.write(names[i]);
                    while ( (enumVals != null) && enumVals.hasMoreElements() ) {
                        String es = (String)enumVals.nextElement();
                        if (es != null) {
                            fw.write("," + es);
                        } else {
                            fw.write("," + "\"\"");
                        }
                    }
                    fw.write("\n");
                }

                fw.flush();

                //-----------------------------------------------------
                // go deeper... never forget to add result to the row! :-):-)
                outputLaboItems(fw, testDN);
            }
        }

        public void outputLaboItems(FileWriter fw, String testDN) throws IOException {
            // get connection
            LDAPConnection ld = getConnection();
            if (ld == null) {
                return;
            }

            String dn = testDN;        
            String filter = "(objectclass=laboItem)";

            String[] attrs = {
                "laboItemName","laboItemCode","laboItemCodeId",
                "laboItemAcode","laboItemIcode","laboItemScode","laboItemMcode","laboItemRcode",
                "laboValue","laboUnit","laboUnitCode","laboUnitCodeId","laboUp","laboLow","laboNormal","laboOut","laboItemMemo","laboItemFreeMemo"
            };

            String[] names = {
                "���ږ�","���ڃR�[�h","���ڃe�[�u��",
                "JLAC10-A","JLAC10-I","JLAC10-S","JLAC10-M","JLAC10-R",
                "�l","�P��","�P�ʃR�[�h","�P�ʃe�[�u��","����l","�����l","��l","�ُ�l","���ڃ���","���ڎ��R����"
            };

            String[] sortattrs = {
                "laboItemCode", "laboItemCodeId"
            };
            boolean[] ascend = {
                true, true
            };

            LDAPSearchResults results = null;
            try {
                statMessage = "�f�[�^������...";
                try {Thread.sleep(100);} catch (Exception ee){}

                results = ld.search(dn, netscape.ldap.LDAPConnection.SCOPE_ONE, filter, attrs, false);
                if (results != null) {
                    results.sort(new LDAPCompareAttrNames(sortattrs, ascend));
                }
            } catch (LDAPException e) {
                statMessage = "�Y������f�[�^�͂���܂���B";
                try {Thread.sleep(100);} catch (Exception ee){}
                stop();
                return;
            }

            while ( results.hasMoreElements() ) {
                LDAPEntry entry = null;
                try {
                    entry = (LDAPEntry)results.next();
                } catch (LDAPException e) {
                    continue;
                }

                // get each entry of the laboModule for this patient
                String itemDN = entry.getDN();
                statMessage = itemDN;
                try {Thread.sleep(100);} catch (Exception ee){}

                int len = attrs.length;
                for (int i = 0; i < len; ++i) {
                    statMessage = names[i];
                    try {Thread.sleep(100);} catch (Exception ee){}

                    LDAPAttribute attr = entry.getAttribute(attrs[i]);
                    if (attr == null) {
                        fw.write(names[i] + "," + "\"\"" + "\n");
                        continue;
                    }

                    // get values of attribute attrs[i]
                    Enumeration enumVals = attr.getStringValues();
                    fw.write(names[i]);
                    while ( (enumVals != null) && enumVals.hasMoreElements() ) {
                        String es = (String)enumVals.nextElement();
                        if (es != null) {
                            fw.write("," + es);
                        } else {
                            fw.write("," + "\"\"");
                        }
                    }
                    fw.write("\n");
                }
                fw.flush();
            }
        }

        public String searchDN(String dn, String filter) {
            // get connection
            LDAPConnection ld = getConnection();
            if (ld == null) {
                return null;
            }

            String[] attrs = null;
            LDAPSearchResults results = null;
            try {
                results = ld.search(dn, netscape.ldap.LDAPConnection.SCOPE_ONE, filter, attrs, false);
            } catch (LDAPException e) {
                return null;
            }

            if ( results.hasMoreElements() == false ) {
                return null;
            }

            // get first entry we found
            LDAPEntry entry = null;
            try {
                entry = (LDAPEntry)results.next();
            } catch (LDAPException e) {
                return null;
            }
            return entry.getDN();
        }
    }// EOF class ActualTask
}
