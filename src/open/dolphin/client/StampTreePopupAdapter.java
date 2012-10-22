package open.dolphin.client;

import javax.swing.*;
import javax.swing.tree.*;

import open.dolphin.infomodel.ModuleInfoBean;

import java.awt.*;
import java.awt.event.*;

/**
 * StampTreePopupAdapter
 *
 * @author  Kazushi Minagawa
 */
public class StampTreePopupAdapter extends MouseAdapter {
    
    public StampTreePopupAdapter() {
    }
    
    @Override
    public void mousePressed(MouseEvent evt) {
        maybePopup(evt);
    }
    
    @Override
    public void mouseReleased(MouseEvent evt) {
        maybePopup(evt);
    }
    
    private void maybePopup(MouseEvent evt) {
        
        if (evt.isPopupTrigger()) {
            
            // �C�x���g�\�[�X�� StampTree ���擾����
            StampTree tree = (StampTree) evt.getSource();
            int x = evt.getX();
            int y = evt.getY();
            
            // �N���b�N�ʒu�ւ̃p�X�𓾂�
            TreePath destPath = tree.getPathForLocation(x, y);
            if (destPath == null) {
                return;
            }
            
            // �N���b�N�ʒu�� Node �𓾂�
            StampTreeNode node = (StampTreeNode) destPath.getLastPathComponent();
            
            if (node.isLeaf()) {
                // Leaf �Ȃ̂� StampInfo �@�𓾂�
                ModuleInfoBean info = (ModuleInfoBean) node.getUserObject();
                
                // Editable
                if ( ! info.isEditable() ) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
            }
            
            // Popup����
            JPopupMenu popup = PopupMenuFactory.create("stampTree.pop", tree);
            popup.show(evt.getComponent(),x, y);
        }
    }
}