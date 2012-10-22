/*
 * StampTreePopupAdapter.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *	
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *	
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package open.dolphin.client;

import javax.swing.*;
import javax.swing.tree.*;

import open.dolphin.infomodel.ModuleInfo;

import java.awt.*;
import java.awt.event.*;

/**
 * StampTree �ŋ��L���� PopupMenu �A�_�v�^�N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class StampTreePopupAdapter extends MouseAdapter { 
        
    /** Popup ���j���[ */
    private JPopupMenu popUpMenu;
    
    /** ����Ώۂ� Tree �C�x���g����ʒm����� */
    private StampTree tree;
    
    /**
     * �f�t�H���g�R���X�g���N�^
     */
    public StampTreePopupAdapter() {
        // popUp ���j���[�𐶐�����
        popUpMenu = open.dolphin.client.PopupMenuFactory.create("stampTree.pop.", this);
    }

    public void mousePressed(MouseEvent evt) {        
        // JDK1.4
       if (popUpMenu.isPopupTrigger(evt)) {
           startService (evt);
       }         
    }
    
    public void mouseReleased(MouseEvent evt) {        
        // JDK1.4
       if (popUpMenu.isPopupTrigger(evt)) {
           startService (evt);
       }         
    }
    
    private void startService(MouseEvent evt) {
        
       tree = (StampTree)evt.getSource();
       int x = evt.getX();
       int y = evt.getY();
       
       TreePath destPath = tree.getPathForLocation(x, y);
       if (destPath == null) {
           return;
       }
       
       // �V�X�e���Œ񋟂���X�^���v�G�f�B�^���ŁA�ҏW�s�̃X�^���v��
       // �I�����ꂽ�ꍇ�̓|�b�v�A�b�v���Ȃ�
       
       // ASP-Tree �͕ҏW�s�� 
       //int mode = tree.getMode();
       //if (mode == StampTree.ASP_USER) {
           //return;
       //}
       if (!tree.isEdiatble()) {
       		return;
       }
       
       // �N���b�N�ʒu�� Node �𓾂�
       StampTreeNode node =(StampTreeNode)destPath.getLastPathComponent();
       
       if (node.isLeaf()) {
  
           // Leaf �Ȃ̂� StampInfo �@�𓾂� 
           ModuleInfo info = (ModuleInfo)node.getUserObject();

           // Editable
           if ( ! info.isEditable() ) {
                Toolkit.getDefaultToolkit().beep();
                return;
           }
       }
       
       tree.setSelectionPath(destPath);
       popUpMenu.show(evt.getComponent(),x, y);       
    }
    
    /**
     * �m�[�h�̖��O��ύX����
     */
    public void doRename (ActionEvent e) {
        tree.renameNode();
    }
    
    /**
     * �m�[�h���폜����
     */
    public void doDelete (ActionEvent e) {       
        tree.deleteNode ();
    }
     
    /**
     * �V�K�̃t�H���_�m�[�h��ǉ�����
     */
    public void doNewFolder(ActionEvent e) {        
        tree.createNewFolder();
    }
}