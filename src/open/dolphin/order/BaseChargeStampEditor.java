/*
 * BaseChargeStampEditor.java
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
package open.dolphin.order;

import javax.swing.*;
import javax.swing.border.*;

import open.dolphin.client.*;

import java.awt.*;
import java.util.EnumSet;
import open.dolphin.client.GUIConst;

/**
 * BaseCharge editor.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class BaseChargeStampEditor extends StampModelEditor  {
    
    //private static final String orderClass          = "110-125";   //"10";
    //private static final String orderName           = "���f�E�Đf";
    //private static final String classCodeId         = "Claim007";
    //private static final String subclassCodeId      = "Claim003";
           
    private static final long serialVersionUID = 8284352054746435316L;
	
    private ItemTablePanel testTable;
    private MasterTabPanel masterPanel;
    
    /** 
     * Creates new InjectionStampEditor 
     */
    public BaseChargeStampEditor() {
    }
    
    public void start() {
        
    	// �f�f����CLAIM �d�l�𓾂�
        ClaimConst.ClaimSpec spec = ClaimConst.ClaimSpec.BASE_CHARGE;
        
        // �Z�b�g�e�[�u���𐶐��� CLAIM �p�����[�^��ݒ肷��
        testTable = new ItemTablePanel(this);
        testTable.setOrderName(spec.getName());
        testTable.setFindClaimClassCode(true);         // �f�Ís�׋敪�̓}�X�^�A�C�e������
        testTable.setClassCodeId(ClaimConst.CLASS_CODE_ID);
        testTable.setSubClassCodeId(ClaimConst.SUBCLASS_CODE_ID);
        
        // �f�f���̃}�X�^�Z�b�g�𐶐�����
        EnumSet<ClaimConst.MasterSet> enumSet = EnumSet.of(
        		ClaimConst.MasterSet.TREATMENT);
        // �}�X�^�p�l���𐶐����A�f�Ís�ׂ̌����ΏۃR�[�h�͈͂�ݒ肷��
        masterPanel = new MasterTabPanel(enumSet);
        masterPanel.setSearchClass(spec.getSearchCode());
        masterPanel.startCharge(testTable);
        
        // �^�C�g����ݒ肵���C�A�E�g����
        setTitle(spec.getName());
        Border b = BorderFactory.createEtchedBorder();
        testTable.setBorder(BorderFactory.createTitledBorder(b, spec.getName()));
        
        setLayout(new BorderLayout(0, GUIConst.DEFAULT_CMP_V_SPACE));
        add(testTable, BorderLayout.NORTH);
        add(masterPanel, BorderLayout.CENTER);
        setPreferredSize(GUIConst.DEFAULT_STAMP_EDITOR_SIZE);
    }
    
    public Object getValue() {
        return testTable.getValue();
    }
    
    public void setValue(Object val) {
        testTable.setValue(val);
    }
    
    public void dispose() {
        masterPanel.stopCharge(testTable);
    }
}