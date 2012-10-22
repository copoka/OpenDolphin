/*
 * DataBackupTask.java
 *
 * Created on 2003/02/16
 *
 * Last updated on 2003/03/06
 *
 */

package mirrorI.dolphin.client;

import swingworker.*;
import java.util.*;
import java.io.*;
import java.net.*;

/**
 *
 * This class communicate with local server's 'ClientRequestHandler' program<br>
 * for executing data backup program which is exist in local server<br>
 *
 * This object is created when '�J�n' button is clicked from '�f�[�^�o�b�N�A�b�v' GUI<br>
 *
 * @author  Prasahnth Kumar, Mirror-I Corp.
 *
 */
public class DataBackupTask{

    //For data backup program returns
    private static final int OPERATION_SUCCESS      = 0;
    private static final int OPERATION_FAIL         = 1;
    private static final int PATH_ERROR             = 2;
    private static final int TAPE_ERROR             = 3;

    private static final int DATA_BACKUP            = 0x12;

    //private static Logger logger ;
    private Properties dataBackupParameter;
    private boolean backupOver=false;
    private int selectedMediaToBackup;
    private int selectedHdd;
    private String backupFilePath;

    private String statMessage=null;

    /** Creates new DataBackupTask */
    public DataBackupTask (int selectedMedia, int selectedHDD, String path, Properties dataBackupParameter) {
        this.selectedMediaToBackup=selectedMedia;
        this.selectedHdd=selectedHDD;
        this.backupFilePath=path;
        this.dataBackupParameter = dataBackupParameter;
        //this.logger = logger;
    }

	/**
	 *
	 * go(), creates new object of  'ActualTask' class to send request for data backup<br>
	 * <br>
	 * This method is called from DataBackupService.exeBackup()<br>
	 *
 	 */
    void go() {
	statMessage="���[�J���T�[�o�[�֐ڑ���. . . . . . .";
        final SwingWorker worker = new SwingWorker() {
            public Object construct() {
                return new ActualTask();
            }
        };
        worker.start();
    }

	/**
	 *
	 * done(), returns true when backup task is completed and else returns false<br>
	 * <br>
	 * This method is called from DataBackupService.exeUpdate()<br>
	 *
 	 */
     boolean done() {
    	return backupOver;
     }

 	/**
	 *
	 * getMessage(), returns the present message to DataBackupService to display<br>
	 * <br>
	 * This method is called from DataBackupService.exeUpdate()<br>
	 *
 	 */
    String getMessage() {
        return statMessage;
    }

 	/**
	 *
 	 * This class communicate with local server's 'ClientRequestHandler' program<br>
 	 * for executing Data Backup program which is exist in local server<br>
 	 *
 	 * Returns updated result to DataBackupService for displaying message<br>
 	 *
	 * This class is initiated from go()<br>
	 *
 	 */
    private class ActualTask {

         /** Creates new ActualTask */
        ActualTask() {
                //send request to execute
                sendRequest();
        }

        /**
         *
         * This method communicate with local server's 'ClientRequestHandler' program<br>
         * for executing Data Backup program which is exist in local server<br>
         *
         * Returns updated result to DataBackupService for displaying message<br>
         *
         * This method is called from ActualTask's Constructor<br>
         *
         */	
        private boolean sendRequest() {
            
            Socket localSocket = null;
            ObjectOutputStream writer = null;
            ObjectInputStream reader = null;
            boolean sendRequestRet = false;
            int resultRet = 0;
            backupOver=false;
            String clientReqIP = null;
            int clientReqPort = 0;
            
            try {
                //Get ClientRequestHandler's IP Address and port to communicate
                if (dataBackupParameter != null && dataBackupParameter.size() > 0 &&
                    dataBackupParameter.containsKey("ClientReqHandlerIP") &&
                    dataBackupParameter.getProperty("ClientReqHandlerIP") != null ) {
                        clientReqIP=dataBackupParameter.getProperty("ClientReqHandlerIP");
                
                }
                
                if (dataBackupParameter != null && dataBackupParameter.size() > 0 &&
                    dataBackupParameter.containsKey("ClientReqHandlerPort") &&
                    new Integer(dataBackupParameter.getProperty("ClientReqHandlerPort")).intValue() >0) {
                        clientReqPort = (new Integer(dataBackupParameter.getProperty("ClientReqHandlerPort")).intValue());
                }

                            
                if( (clientReqIP != null) && (clientReqPort>0) ){

                    localSocket = new Socket(clientReqIP, clientReqPort );
 
                    if(localSocket.isConnected()) {
                        
                        //logger.finer("connected to remote server");
                        //Initialize output stream writer
                        writer = new ObjectOutputStream(new BufferedOutputStream (localSocket.getOutputStream()));
                        //Send local server Identification
                        writer.write(DATA_BACKUP);
                        writer.flush();
                        //write backup parameters to client request handler
                        writer.writeInt(selectedMediaToBackup);
                        writer.flush();
                        writer.writeInt(selectedHdd);
                        writer.flush();
                        writer.writeObject(backupFilePath);
                        writer.flush();
                        //Initialize input stream reader
                        reader = new ObjectInputStream(new BufferedInputStream(localSocket.getInputStream()));
                        //Read requested program name
                        resultRet =reader.read();

                        if(resultRet ==DATA_BACKUP){
                                statMessage ="���[�J���T�[�o�[�֐ڑ���. . . . . . .";
                                //logger.finer("Client system requested to execute 'Data Backup' program");
                                //read and Display return result
                                resultRet =reader.readInt();
                                switch(resultRet) {

                                        case (OPERATION_SUCCESS):
                                                statMessage ="����I���F�f�[�^�o�b�N�A�b�v����܂����B";
                                                //logger.info("����I���F�f�[�^�o�b�N�A�b�v����܂����B");
                                                break;

                                        case (OPERATION_FAIL):
                                                statMessage ="�ُ�I���F�f�[�^�o�b�N�A�b�v�G���[�B";
                                                //logger.info("�ُ�I���F�f�[�^�o�b�N�A�b�v�G���[�B");
                                                break;

                                        case (PATH_ERROR):
                                                statMessage="�ُ�I���F�A�N�Z�X�G���[�i�o�b�N�A�b�v��p�X�j�B";
                                                //logger.info("�ُ�I���F�A�N�Z�X�G���[�i�o�b�N�A�b�v��p�X)�B");
                                                break;

                                        case (TAPE_ERROR):
                                                statMessage="�ُ�I���F�e�[�v�������݃G���[�B";
                                                //logger.info("�ُ�I���F�e�[�v�������݃G���[�B");
                                                break;

                                        default:
                                                statMessage="�ُ�I���F�s���ȃG���[�B";
                                                //logger.info("�ُ�I���F�s���ȃG���[�B");
                                                break;
                                }
                        }
                                            
                        sendRequestRet=true;
                    
                    } else{
                                            
                        statMessage="�ُ�I���F���[�J���T�[�o�[�ڑ��Ɏ��s���܂����B";
                                            //logger.warning("Not able to connect to Client Request handler ");
                    }
                    
                } else{
                    statMessage="�ُ�I���F���[�J���T�[�o�[�ڑ��Ɏ��s���܂����B";
                                    //logger.warning("Could not get IP address or/and port of Client Request handler from INI file");
                }
            
            } catch (IOException ioe) {
                //logger.warning("IOException while communicating with Client Request handler");
                //logger.warning("Exception details:"  + ioe );
                statMessage="�ُ�I���F���[�J���T�[�o�[�ڑ��Ɏ��s���܂����B";
                sendRequestRet=false;
            } catch (Exception e) {
                //logger.warning("Exception while communicating with Client Request handler");
                //logger.warning("Exception details:"  + e );
                statMessage="�ُ�I���F���[�J���T�[�o�[�ڑ��Ɏ��s���܂����B";
                sendRequestRet=false;
            
            } finally {
                            
                try {
                    if (reader != null) {
                        reader.close();
                    }
                    if (writer != null) {
                        writer.close();
                    }
                    if (localSocket != null) {
                        localSocket.close();
                    }
                    
                } catch (IOException e2) {
                    //logger.warning("Exception while closing socket or writer or reader");
                    //logger.warning("Exception details:"  + e2 );
                    sendRequestRet=false;
                }
            }
            backupOver = true;
            return sendRequestRet;
        }
    }
}