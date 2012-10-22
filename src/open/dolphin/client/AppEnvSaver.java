/*
 * AppEnvSaver.java
 *
 * Created on 2007/04/27, 20:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package open.dolphin.client;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import open.dolphin.plugin.ILongTask;
import open.dolphin.plugin.helper.AbstractLongTask;
import open.dolphin.plugin.helper.TaskManager;
import org.apache.log4j.Logger;

/**
 * �A�v���P�[�V�����̊��ۑ��N���X�B
 *
 * @author Kazushi Minagawa
 */
public class AppEnvSaver {
    
    /** ���ۑ��X�e�[�g�v���p�e�B */
    public static final String SAVE_ENV_PROP = "saveEnvProp";
    
    /** ��������ɕۑ����ꂽ */
    public static final int SAVE_DONE   = 1;
    
    /** ���̕ۑ��Ɏ��s���� */
    public static final int SAVE_ERROR  = 2;
    
    /** ���ۑ��̏����������Ă��Ȃ� */
    public static final int NO_SAVE_CONDITION = 3;
    
    /** ���ۑ��X�e�[�g���� */
    private int state;
    
    /** �G���[���N�����Ă���^�X�N */
    private ILongTask errorTask;
    
    /** ���ۑ��X�e�[�g�̑����T�|�[�g*/
    private PropertyChangeSupport boundSupport;
    
    // TimerTask
    private javax.swing.Timer taskTimer;
    
    /** Creates a new instance of AppEnvSaver */
    public AppEnvSaver() {
        boundSupport = new PropertyChangeSupport(this);
    }
    
    /**
     * �ۑ��X�e�[�g��ݒ肵���X�i�֒ʒm����B
     */
    public void setSaveEnvState(int state) {
        int old = this.state;
        this.state = state;
        boundSupport.firePropertyChange(SAVE_ENV_PROP, old, this.state);
    }
    
    public ILongTask getErrorTask() {
        return errorTask;
    }
    
    public void setErrorTask(ILongTask task) {
        errorTask = task;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        boundSupport.addPropertyChangeListener(SAVE_ENV_PROP, l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        boundSupport.removePropertyChangeListener(SAVE_ENV_PROP, l);
    }
    
    /**
     * �I���������s���B
     */
    public void save(Hashtable activeChildren) {
        
        final Logger bootLogger = ClientContext.getLogger("boot");
        
        // ���ۑ��̃J���e������ꍇ�͌x�������^�[������
        // �J���e��ۑ��܂��͔j�����Ă���ēx���s����
        boolean dirty = false;
        
        // Chart �𒲂ׂ�
        ArrayList<ChartPlugin> allChart = ChartPlugin.getAllChart();
        if (allChart != null && allChart.size() > 0) {
            for (ChartPlugin chart : allChart) {
                if (chart.isDirty()) {
                    dirty = true;
                    break;
                }
            }
        }
        
        // �ۑ����ĂȂ����̂�����΃��^�[������
        if (dirty) {
            alertDirty();
            setSaveEnvState(NO_SAVE_CONDITION);
            return;
        }
        
        // EditorFrame�̃`�F�b�N���s��
        java.util.List<IChart> allEditorFrames = EditorFrame.getAllEditorFrames();
        if (allEditorFrames != null && allEditorFrames.size() > 0) {
            for(IChart chart : allEditorFrames) {
                if (chart.isDirty()) {
                    dirty = true;
                    break;
                }
            }
        }
        
        if (dirty) {
            alertDirty();
            setSaveEnvState(NO_SAVE_CONDITION);
            return;
        }
        
        
        //
        // StoppingTask ���W�߂�
        //
        Vector<ILongTask> stoppingTasks = new Vector<ILongTask>();
        ILongTask task = null;
        
        try {
            Hashtable cloneMap = null;
            synchronized (activeChildren) {
                cloneMap = (Hashtable) activeChildren.clone();
            }
            Iterator iter = cloneMap.values().iterator();
            while (iter != null && iter.hasNext()) {
                IMainWindowPlugin pl = (IMainWindowPlugin) iter.next();
                task = pl.getStoppingTask();
                if (task != null) {
                    stoppingTasks.add(task);
                }
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            bootLogger.warn(ex.toString());
        }
        
        //
        // StoppingTask ����̃^�C�}�y�� Progress Monitor �Ŏ��s����
        // �S�Ẵ^�X�N���I��������A�v���P�[�V�����̏I�������Ɉڂ�
        //
        int cnt = stoppingTasks.size();
        
        if (cnt == 0) {
            setSaveEnvState(SAVE_DONE);
            return; // Never come back
            
        } else {
            bootLogger.info(cnt + " �� StoppingTask ������܂�");
        }
        
        // �ꊇ���Ď��s���邽�߂�TaskManager�𐶐�����
        ILongTask[] longs = new AbstractLongTask[cnt];
        for (int i = 0; i < cnt; i++) {
            longs[i] = stoppingTasks.get(i);
        }
        final TaskManager taskMgr = new TaskManager(longs);
        
        // Progress Monitor �𐶐�����
        String exittingNote = ClientContext.getString("mainWindow.progressNote.exitting");
        final ProgressMonitor monitor = new ProgressMonitor(null, null, exittingNote, 0, taskMgr.getLength());
        
        // ���s Timer �𐶐�����
        taskTimer = new javax.swing.Timer(taskMgr.getDelay(), new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                if (taskMgr.isDone()) {
                    
                    // �I���������s��
                    taskTimer.stop();
                    monitor.close();
                    
                    //
                    // ���s���ʂ𓾂�
                    //
                    if (!taskMgr.getResult()) {
                        //
                        // �ۑ��������G���[�̎�
                        //
                        setErrorTask(taskMgr.getCurTask());
                        bootLogger.warn("StoppingTask �ɃG���[������܂�");
                        setSaveEnvState(SAVE_ERROR);
                        
                    } else {
                        //
                        // �G���[�Ȃ�
                        //
                        bootLogger.info("StoppingTask ���I�����܂���");
                        setSaveEnvState(SAVE_DONE);
                    }
                    
                } else {
                    // ���ݒl���X�V����
                    monitor.setProgress(taskMgr.getCurrent());
                }
            }
        });
        taskMgr.start();
        taskTimer.start();
    }
    
    /**
     * ���ۑ��̃h�L�������g������ꍇ�̌x����\������B
     */
    private void alertDirty() {
        String msg0 = "���ۑ��̃h�L�������g������܂��B";
        String msg1 = "�ۑ��܂��͔j��������ɍēx���s���Ă��������B";
        String taskTitle = ClientContext.getString("mainWindow.exit.taskTitle");
        JOptionPane.showMessageDialog(
                        (Component) null,
                        new Object[]{msg0, msg1},
                        ClientContext.getFrameTitle(taskTitle),
                        JOptionPane.INFORMATION_MESSAGE
                        );
    }
}
