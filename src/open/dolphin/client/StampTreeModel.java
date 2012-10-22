/*
 * StampTreeModel.java
 * Copyright (C) 2001,2003,2004 Digital Globe, Inc. All rights reserved.
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

import javax.swing.tree.*;

import open.dolphin.infomodel.ModuleInfoBean;

/**
 * �X�^���v�c���[�̃��f���N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class StampTreeModel extends DefaultTreeModel {
    
    private static final long serialVersionUID = -2227174337081687786L;

	/**
     * �f�t�H���g�R���X�g���N�^
     */
    public StampTreeModel(TreeNode node) {
        super(node);
    }
    
    /**
     * �m�[�h���̕ύX���C���^�[�Z�v�g���ď�������
     */
    public void valueForPathChanged (TreePath path, Object newValue) {
        
        // �ύX�m�[�h���擾����
        StampTreeNode node = (StampTreeNode) path.getLastPathComponent();
        
        // Debug
        //String oldString = node.toString ();
        String newString = (String) newValue;
        //System.out.println (oldString + " -> " + newString);
        
        /**
         * �t�m�[�h�̏ꍇ�� StampInfo �� name ��ύX����
         * �����łȂ��ꍇ�͐V����������� userObject �ɐݒ肷��
         */
        if (node.isLeaf()) {
            ModuleInfoBean info = (ModuleInfoBean) node.getUserObject();
            info.setStampName(newString);
        
        } else {
            node.setUserObject(newString);
        }
        
        // ���X�i�֒ʒm����
        nodeChanged (node);       
    }
}