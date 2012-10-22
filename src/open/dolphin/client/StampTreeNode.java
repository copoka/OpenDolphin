/*
 * StampTreeNode.java
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
 * StampTree �̃m�[�h�N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class StampTreeNode extends DefaultMutableTreeNode {
	
	private static final long serialVersionUID = -4253332364508651955L;

    /**
     * �R���X�g���N�^
     */
    public StampTreeNode(Object userObject) {
        
        super(userObject);
        
        // StampInfo �ŏ��������ꂽ�ꍇ�͗t�m�[�h�ɂ���
        if (userObject instanceof open.dolphin.infomodel.ModuleInfoBean) {
            this.allowsChildren = false;
        }
    }
    
    /**
     * �t���ǂ�����Ԃ�
     */
    public boolean isLeaf() {
        return (! this.allowsChildren);
    }
    
    /**
     * StampInfo ��Ԃ�
     */
    public ModuleInfoBean getStampInfo() {
        return (ModuleInfoBean) userObject;
    }
}