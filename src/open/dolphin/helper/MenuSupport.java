package open.dolphin.helper;

import java.lang.reflect.Method;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * MenuSupport
 *
 * @author Minagawa,Kazushi
 *
 */
public class MenuSupport implements MenuListener {
    
    private ActionMap actions;
    private Object[] chains;
    
    public MenuSupport(Object owner) {
        Object[] chs = new Object[3];
        chs[1] = this;
        chs[2] = owner;
        setChains(chs);
    }
    
    public void menuSelected(MenuEvent e) {
    }
    
    public void menuDeselected(MenuEvent e) {
    }
    
    public void menuCanceled(MenuEvent e) {
    }
    
    public void registerActions(ActionMap actions) {
        this.actions = actions;
    }
    
    public Action getAction(String name) {
        if (actions != null) {
            return actions.get(name);
        }
        return null;
    }
    
    public ActionMap getActions() {
        return actions;
    }
    
    public void disableAllMenus() {
        if (actions != null) {
            Object[] keys = actions.keys();
            for (Object o : keys) {
                actions.get(o).setEnabled(false);
            }
        }
    }
    
    public void disableMenus(String[] menus) {
        if (actions != null && menus != null) {
            for (String name : menus) {
                Action action = actions.get(name);
                if (action != null) {
                    action.setEnabled(false);
                }
            }
        }
    }
    
    public void enableMenus(String[] menus) {
        if (actions != null && menus != null) {
            for (String name : menus) {
                Action action = actions.get(name);
                if (action != null) {
                    action.setEnabled(true);
                }
            }
        }
    }
    
    public void enabledAction(String name, boolean enabled) {
        if (actions != null) {
            Action action = actions.get(name);
            if (action != null) {
                action.setEnabled(enabled);
            }
        }
    }
    
    public void setChains(Object[] chains) {
        this.chains = chains;
    }
    
    public Object[] getChains() {
        return chains;
    }
    
    public void addChain(Object obj) {
        // �ŏ��̃^�[�Q�b�g�ɐݒ肷��
        chains[0] = obj;
    }
    
    public Object getChain() {
        // �ŏ��̃^�[�Q�b�g��Ԃ�
        return chains[0];
    }
    
    /**
     * chain �ɂ����ă��t���N�V�����Ń��\�b�h�����s����B
     * ���\�b�h�����s����I�u�W�F�N�g������΂����ŏI������B
     * ���\�b�h�����s����I�u�W�F�N�g�����݂��Ȃ��ꍇ�������ŏI������B
     * �R�}���h�`�F�C���p�^�[���̃��t���N�V�����ŁB
     * @param obj
     * @return ���\�b�h�����s���ꂽ�� true
     */
    public boolean sendToChain(String method) {
        
        boolean handled = false;
        
        if (chains != null) {
            
            for (Object target : chains) {
                
                if (target != null) {
                    try {
                        Method mth = target.getClass().getMethod(method, (Class[])null);
                        mth.invoke(target, (Object[])null);
                        handled = true;
                        break;
                    } catch (Exception e) {
                        // ���� target �ł͎��s�ł��Ȃ�
                    }
                }
            }
        }
        
        return handled;
    }
    
    public boolean sendToChain(String method, String arg) {
        
        boolean handled = false;
        
        if (chains != null) {
            
            for (Object target : chains) {
                
                if (target != null) {
                    try {
                        Method mth = target.getClass().getMethod(method, new Class[]{String.class});
                        mth.invoke(target, new Object[]{arg});
                        handled = true;
                        break;
                    } catch (Exception e) {
                        // ���� target �ł͎��s�ł��Ȃ�
                    }
                }
            }
        }
        
        return handled;
    }
    
    
    public void cut() {}
        
    public void copy() {}             
       
    public void paste() {}
}