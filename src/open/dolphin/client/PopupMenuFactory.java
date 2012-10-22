package open.dolphin.client;

import javax.swing.*;

/**
 * ���\�[�X�f�[�^���� PopupMenu �𐶐�����N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class PopupMenuFactory {
    
    private PopupMenuFactory () {
    }
    
    /**
     * ���\�[�X�ƃ^�[�Q�b�g�I�u�W�F�N�g���� PopupMenu �𐶐����ĕԂ��B
     * @param resource ���\�[�X��
     * @target ���\�b�h�����s����I�u�W�F�N�g
     */
    public static JPopupMenu create(String resource, Object target) {
        
        JPopupMenu popMenu = new JPopupMenu ();
        
        String[] itemLine = ClientContext.getStringArray(resource + ".items");
        String[] methodLine = ClientContext.getStringArray(resource + ".methods");
        
        for (int i = 0; i < itemLine.length; i++) {
            
            String name = itemLine[i];
            String method = methodLine[i];
            
            if (name.equals("-")) {
                popMenu.addSeparator();
            }
            else {
                ReflectAction action = new ReflectAction(name, target, method);
                JMenuItem item = new JMenuItem(action);
                popMenu.add(item);
            }
        }
        return popMenu;
    }
}


