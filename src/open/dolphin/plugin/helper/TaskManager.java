/*
 * Created on 2005/06/08
 *
 */
package open.dolphin.plugin.helper;

import open.dolphin.plugin.ILongTask;
import swingworker.SwingWorker;

/**
 * TaskManager
 * �A�v���P�[�V�����̋N�����y�яI�����ɕ����̃^�X�N���܂Ƃ߂Ď��s����B
 *
 * @author Kazushi Minagawa
 */
public class TaskManager {
    
    //private static final int DEFAULT_LENGTH = 10000;  // 10 sec
    private static final int DEFAULT_LENGTH = 180000;  // 180 sec
    private static final int DEFAULT_DELAY  =   200;  // 200 msec
    
    private ILongTask[] tasks;
    private ILongTask curTask;
    private int length = DEFAULT_LENGTH;
    private int delay  = DEFAULT_DELAY;
    private int current;
    private boolean done;
    private boolean result = true;
    
    public TaskManager() {
    }
    
    public TaskManager(ILongTask[] tasks) {
        this();
        setTask(tasks);
    }
    
    public void start() {
        final SwingWorker worker = new SwingWorker() {
            public Object construct() {
                return new ActualTask();
            }
        };
        worker.start();
    }
    
    public ILongTask[] getTask() {
        return tasks;
    }
    
    public void setTask(ILongTask[] tasks) {
        this.tasks = tasks;
    }
    
    public void setCurTask(ILongTask curTask) {
        this.curTask = curTask;
    }
    
    public ILongTask getCurTask() {
        return curTask;
    }
    
    public int getLength() {
        return length;
    }
    
    public void setLength(int length) {
        this.length = length;
    }
    
    public int getDelay() {
        return delay;
    }
    
    public void setDelay(int delay) {
        this.delay = delay;
    }
    
    /**
     * �R�[�������x�� delay �Â����錻�ݒl��Ԃ��B
     * @return ���ݒl
     */
    public int getCurrent() {
        return current += delay;
    }
    
    public String getMessage() {
        return getCurTask().getMessage();
    }
    
    public boolean isDone() {
        return done;
    }
    
    public void setResult(boolean result) {
        this.result = result;
    }
    
    public boolean getResult() {
        return result;
    }
    
    private class ActualTask {
        
        ActualTask() {
            
            if (tasks == null || tasks.length == 0) {
                return;
            }
            
            // ���ԂɃ^�X�N�����s����
            for (ILongTask task : tasks) {
                
                setCurTask(task);
                getCurTask().run();
                setResult(getCurTask().getResult());
                
                if (getResult()) {
                    continue;
                    
                } else {
                    // Task �̈�����s������I������
                    // �G���[�͌Ăяo�����ŏ��������
                    break;
                }
            }
            
            // �S�Ẵ^�X�N���I�������� true �ɂ���
            done = true;
        }
    }
}
