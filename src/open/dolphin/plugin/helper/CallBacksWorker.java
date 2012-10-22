package open.dolphin.plugin.helper;

import java.lang.reflect.Method;

import swingworker.SwingWorker;

/**
 * CallBacksWorker
 *
 * @author Minagawa, Kazushi
 *
 */
public class CallBacksWorker {
    
    // �R�[���o�b�N�I�u�W�F�N�g
    private Object target;
    
    // �R�[���o�b�N���\�b�h��
    private String methodName;
    
    // �N���X����
    private Class[] argClasses;
    
    // �I�u�W�F�N�g����
    private Object[] args;
    
    // ���b�Z�[�W
    private String message;
    
    // ���ݒl
    private int current;
    
    // �^�X�N���I���t���O
    private boolean done;
    
    /**
     * CallBacksWorker�𐶐�����B
     * @param target      �R�[���o�b�N�I�u�W�F�N�g
     * @param methodName  �R�[���o�b�N���\�b�h��
     * @param argClasses  �N���X����
     * @param args        �I�u�W�F�N�g����
     */
    public CallBacksWorker(Object target, String methodName,
            Class[] argClasses, Object[] args) {
        this.target = target;
        this.methodName = methodName;
        this.argClasses = argClasses;
        this.args = args;
    }
    
    public void start() {
        
        SwingWorker worker = new SwingWorker() {
            
            public Object construct() {
                return new CallBack();
            }
        };
        worker.start();
    }
    
    public String getMessage() {
        return message;
    }
    
    public int getCurrent() {
        return ++current;
    }
    
    public boolean isDone() {
        return done;
    }
    
    class CallBack {
        
        CallBack() {
            
            try {
                Method method = target.getClass().getMethod(methodName, argClasses);
                method.invoke(target, args);
                done = true;
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
