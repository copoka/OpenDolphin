package open.dolphin.util;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import javax.swing.ProgressMonitor;
import javax.swing.Timer;

/**
 * Reflection ���g�p���ă��\�b�h�����s���AProgressMonitor ��\������N���X�B
 *
 * @author Kazushi Minagawa
 */
public class ReflectMonitor implements ActionListener {
    
    public static final String STATE_PROP = "resultProp";
    
    public static final int DONE        = 0;
    public static final int TIME_OVER   = 1;
    public static final int CANCELED    = 2;
    
    public static final int DEFAULT_DELAY   = 200;
    public static final int DEFAULT_MAX     = 30 * 1000;
    public static final String DEFAULT_NOTE = "�������Ă��܂�...  ";
    public static final int DEFAULT_DECIDE  = 200;
    public static final int DEFAULT_MILLIS  = 600;
    
    /** �^�[�Q�b�g�I�u�W�F�N�g */
    private Object target;
    
    /** ���s���郁�\�b�h�� */
    private String method;
    
    /** ���\�b�h������ Class �z�� */
    private Class[] argClasses;
    
    /** ���\�b�h�̈��� */
    private Object[] args;
    
    /** ���\�b�h�̖߂�l */
    private Object result;
    
    /** ProgressMonitor Component */
    private Component cmp;
    
    /** ProgressMonitor Message */
    private Object message;
    
    /** ProgressMonitor Note */
    private String note = DEFAULT_NOTE;
    
    /** Popup ���莞�� msec*/
    private int decideToPopup = DEFAULT_DECIDE;
    
    /** Popup ����c��̎��� msec*/
    private int millisToPopup = DEFAULT_MILLIS;
    
    /** �^�C�}�[�̒x������ msec*/
    private int delay = DEFAULT_DELAY;
    
    /** ���\�b�h���s�ɗv���錩�ς��莞�� */
    private int maxEstimation = DEFAULT_MAX;
    
    /** ���s�̏I����Ԃ�ʒm���鑩���T�|�[�g */
    private PropertyChangeSupport boundSupport;
    
    /** ���s�̏I����ԃv���p�e�B */
    private int state = -1;
    
    /** ���s���r���ŃL�����Z���ł��邩�ǂ����̃t���O */
    private boolean cancelOk = true;
    
    /** �^�C���A�E�g���邩�ǂ����̃t���O */
    private boolean timeoutOk = true;
    
    /** ���\�b�h�����s����X���b�h */
    private Thread exec;
    
    /** ���\�b�h���I�����Ă��邩�ǂ����̃t���O */
    private boolean done;
    
    // ProgressMonitor �֘A
    private int min;
    private int max;
    private int current;
    private ProgressMonitor progress;
    
    /** ���荞�݃^�C�}�[ */
    private javax.swing.Timer timer;
    
    
    /** 
     * Creates a new instance of ReflectMonitor. 
     */
    public ReflectMonitor() {
        boundSupport = new PropertyChangeSupport(this);
    }
    
    /** 
     * Creates a new instance of ReflectMonitor.
     * @param target Reflection �̑ΏۃI�u�W�F�N�g
     * @param method ���s���郁�\�b�h
     * @param argClasses ���\�b�h������ Class �z��
     * @param args ���\�b�h�̈���
     */
    public ReflectMonitor(Object target, String method, Class[] argClasses, Object[] args) {
        this();
        setReflection(target, method, argClasses, args);
    }
    
    /**
     * ���ʃv���p�e�B�̑������X�i��ǉ�����B
     * @param l �ǉ����鑩�����X�i
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        boundSupport.addPropertyChangeListener(STATE_PROP, l);
    }
    
    /**
     * ���ʃv���p�e�B�̑������X�i���폜����B
     * @param l �폜���鑩�����X�i
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        boundSupport.removePropertyChangeListener(STATE_PROP, l);
    }
    
    /**
     * ���\�b�h�̎��s���ʂ�Ԃ��B
     * @return ���\�b�h�̎��s���ʃI�u�W�F�N�g
     */
    public Object getResult() {
        return result;
    }
    
    /**
     * ���� Status ��Ԃ��B
     * @return ����Status
     */
    public int getState() {
        return state;
    }
    
    /**
     * ���� Status ��ݒ肷��B
     * @param result ����Status
     */
    public void setState(int result) {
        int old = this.state;
        this.state = result;
        boundSupport.firePropertyChange(STATE_PROP, old, this.state);
    }
    
    /**
     * Reflection �̃p�����[�^��ݒ肷��B
     * @param target Reflection �̑ΏۃI�u�W�F�N�g
     * @param method ���s���郁�\�b�h
     * @param argClasses ���\�b�h������ Class �z��
     * @param args ���\�b�h�̈���
     */
    public void setReflection(Object target, String method, Class[] argClasses, Object[] args) {
        this.target = target;
        this.method = method;
        this.argClasses = argClasses;
        this.args = args;
    }
    
    /**
     * ProgressMonitor �̃p�����[�^��ݒ肷��B
     * @param cmp ProgressMonitor �� Component
     * @param message ProgressMonitor �� Message
     * @param note ProgressMonitor �� note
     * @param delay �x������ msec
     * @param maxEstimation ���ς��莞�� msec
     */
    public void setMonitor(Component cmp, Object message, String note, int delay, int maxEstimation) {
        this.cmp = cmp;
        this.message = message;
        this.note = note;
        setDelay(delay);
        setMaxEstimation(maxEstimation);
    }
    
    /**
     * �x�����Ԃ�Ԃ��B
     * @return �x������
     */
    public int getDelay() {
        return delay;
    }
    
    /**
     * �x�����Ԃ�ݒ肷��B
     * @param delay �x������
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }
    
    /**
     * ���ς��莞�Ԃ�Ԃ��B
     * @return ���ς��莞��
     */
    public int getMaxEstimation() {
        return maxEstimation;
    }
    
    /**
     * ���ς��莞�Ԃ�ݒ肷��B
     * @param maxEstimation ���ς��莞��
     */
    public void setMaxEstimation(int maxEstimation) {
        this.maxEstimation = maxEstimation;
    }
    
    public int getDecideToPopup() {
        return decideToPopup;
    }
    
    public void setDecideToPopup(int decideToPopup) {
        this.decideToPopup = decideToPopup;
    }
        
    public int getMillisToPopup() {
        return millisToPopup;
    }
    
    public void setMillisToPopup(int millisToPopup) {
        this.millisToPopup = millisToPopup;
    }
    
    /**
     * �L�����Z���\���ǂ�����Ԃ��B
     * @return �L�����Z���\�Ȏ� true
     */
    public boolean isCancelOk() {
        return cancelOk;
    }
    
    /**
     * �L�����Z���\���ǂ�����ݒ肷��B
     * @param ok �L�����Z���\�Ȏ� true
     */
    public void setCancelOk(boolean ok) {
        cancelOk = ok;
    }
    
    /**
     * �^�C���A�E�g���邩�ǂ�����Ԃ��B
     * @return �^�C���A�E�g���鎞 true
     */
    public boolean isTimeoutOk() {
        return timeoutOk;
    }
    
    /**
     * �^�C���A�E�g���邩�ǂ�����ݒ肷��B
     * @param �^�C���A�E�g���鎞 true
     */
    public void setTimeoutOk(boolean timeout) {
        this.timeoutOk = timeout;
    }
    
    /**
     * Reflection �̃��\�b�h���I���������ǂ�����Ԃ��B
     * @return �I�����Ă��鎞 true
     */
    public boolean isDone() {
        return done;
    }
    
    /**
     * Reflection �̃��\�b�h���I����ݒ肷��B
     * @param �I�������� true
     */
    public void setDone(boolean done) {
        this.done = done;
    }
    
    /**
     * ���\�b�h�̎��s���J�n����B
     */
    public void start() {
        
        if (target == null || method == null) {
            throw new RuntimeException("Reflection �I�u�W�F�N�g���̓��\�b�h���ݒ肳��Ă��܂���B");
        }
        
        // �ŏ��l�ƍő�l��ݒ肷��
        min = 0;
        max = getMaxEstimation() / getDelay();
        
        // ProgressMonitor �𐶐�����
        progress = new ProgressMonitor(cmp, message, note, min, max);
        progress.setMillisToDecideToPopup(getDecideToPopup());
        progress.setMillisToPopup(getMillisToPopup());
        
        // �^�C�}�[�𐶐�����
        timer = new Timer(getDelay(), this);
        
        //
        // Thread �𐶐������\�b�h�����s����
        //
        Runnable r = new Runnable() {
            public void run() {
                try {
                    Method mth = target.getClass().getMethod(method, argClasses);
                    result = mth.invoke(target, args);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                
                setDone(true);
            }
        };
        exec = new Thread(r);
        exec.setPriority(Thread.NORM_PRIORITY);
        exec.start();
        timer.start();
    }
    
    /**
     * �I���������s���B
     * �^�C�}�[�y�у��j�^���N���[�Y����B
     */
    private void stop() {
        
        if (progress != null) {
            progress.close();
        }
        
        if (timer != null) {
            timer.stop();
        }
    }
    
    /**
     * ���s�X���b�h�Ɋ��荞�ށB
     */
    private void interrupt() {
        if (exec != null) {
            exec.interrupt();
        }
    }
    
    /**
     * �i���󋵂��Ǘ�����B
     * @param e ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
        
        current++;
        
        if (progress.isCanceled()) {
            if (isCancelOk()) {
                interrupt();
                stop();
                setState(CANCELED);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
            
        } else if (isDone()) {
            stop();
            setState(DONE);
            
        } else if (current > max && isTimeoutOk()) {
            interrupt();
            stop();
            setState(TIME_OVER);
            
        } else {
            progress.setProgress(current);
        }
    }
}
