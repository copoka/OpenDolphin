/*
 * AreaDataTask.java
 *
 * Created on 2002/01/10, 21:01
 */

package jp.ac.kumamoto_u.kuh.fc.jsato.swing_beans;

import swingworker.*;
import java.util.*;
import java.awt.event.*;

/**
 *
 * @author  Junzo SATO
 * @version This calss is based on LongTask.java from java.sun.com example.
 */
public class AreaDataTask {
    private int lengthOfTask;
    private int current = 0;
    private String statMessage;

    AreaDataBean dataBean;
    /** Creates new AreaDataTask */
    public AreaDataTask(AreaDataBean dataBean) {
        //Compute length of task...
        lengthOfTask = 1000;
        
        this.dataBean = dataBean;
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
        public ActualTask() {
            statMessage = "�T�[�o�[�֐ڑ���...";
            try {Thread.sleep(100);} catch (Exception e){}

            if (dataBean.doLogin() == true) {
                statMessage = "���O�C���ɐ������܂����B";
                try {Thread.sleep(100);} catch (Exception e){}
                
                //--------------------------------------------------------------
                statMessage = "���X�g�擾��...";
                try {Thread.sleep(1000);} catch (Exception e){}
                // yield much time before going futher task:-)

                if (dataBean.doList() == true) {
                    statMessage = "���X�g�擾�ɐ������܂����B";
                    try {Thread.sleep(100);} catch (Exception e){}

                    //----------------------------------------------------------
                    statMessage = "�h�L�������g�擾��...";
                    try {Thread.sleep(1000);} catch (Exception e){}
                    
                    if (dataBean.doBody() == true) {
                        statMessage = "�h�L�������g�擾�ɐ������܂����B";
                        try {Thread.sleep(100);} catch (Exception e){}
                    } else {
                        statMessage = "�h�L�������g�擾�Ɏ��s���܂����B";
                        try {Thread.sleep(100);} catch (Exception e){}

                        stop();
                        return;
                    }
                    
                } else {
                    statMessage = "���X�g�擾�Ɏ��s���܂����B";
                    try {Thread.sleep(100);} catch (Exception e){}

                    stop();
                    return;
                }
                
            } else {
                statMessage = "���O�C���Ɏ��s���܂����B";
                try {Thread.sleep(100);} catch (Exception e){}

                stop();
                return;
            }
            
            // finished:-)
            stop();
            return;
        }
    }// EOF class ActualTask
}
