/*
 * StampTreeXmlDirector.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
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

import java.util.*;
import java.io.*;
import javax.swing.tree.*;


/**
 * Director to build StampTree XML data.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class StampTreeXmlDirector {
    
    private DefaultStampTreeXmlBuilder builder;
    
    /** 
     * Creates new StampTreeXmlDirector 
     */
    public StampTreeXmlDirector(DefaultStampTreeXmlBuilder builder) {
        
        super();
        this.builder = builder;
    }
    
    /**
     * �X�^���v�c���[�S�̂�XML�ɃG���R�[�h����B
     * @param allTrees StampTree�̃��X�g
     * @return XML
     */
    public String build(ArrayList<StampTree> allTrees) {
        
        try {
            builder.buildStart();
            for (StampTree tree : allTrees) {
                lbuild(tree);
            }
            
            builder.buildEnd();
            return builder.getProduct();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * ��̃c���[��XML�ɃG���R�[�h����
     * @param tree StampTree
     * @throws IOException
     */
    private void lbuild(StampTree tree) throws IOException {
        
        // ���[�g�m�[�h���擾���`���C���h��Enumeration�𓾂�
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
        Enumeration e = rootNode.preorderEnumeration();
        StampTreeNode node = (StampTreeNode) e.nextElement();
        
        // ���[�g�m�[�h�������o��
        builder.buildRoot(node);
        
        // �q�������o��
        while (e.hasMoreElements()) {
            builder.buildNode((StampTreeNode) e.nextElement());
        }
        
        builder.buildRootEnd();
    }
}