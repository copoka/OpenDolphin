/*
 * DiagnosisEditor.java
 * Copyright (C) 2007 Dolphin Project. All rights reserved.
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
import javax.swing.border.Border;

import open.dolphin.client.*;
import open.dolphin.client.GUIConst;

import java.awt.*;
import java.util.*;

/**
 * ���a���G�f�B�^�N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class DiagnosisEditor extends StampModelEditor  {
    
    /** �G�f�B�^�� */
    private static final String EDITOR_NAME = "���a��";
    
    /** ���a���ҏW�e�[�u�� */
    private DiagnosisTablePanel diagnosisTable;
    
    /** �}�X�^�[�Z�b�g�p�l�� */
    private MasterTabPanel masterPanel;
    
    /** 
     * Creates new DiagnosisEditor 
     */
    public DiagnosisEditor() {
    }
    
    /**
     * �v���O�������J�n����B
     */
    public void start() {
        
        setTitle(EDITOR_NAME);
        
        //
        // ���a���ҏW�e�[�u���𐶐�����
        //
        diagnosisTable = new DiagnosisTablePanel(this);
        Border b = BorderFactory.createEtchedBorder();
        diagnosisTable.setBorder(BorderFactory.createTitledBorder(b, EDITOR_NAME));
        
        //
        // ���a���Ŏg�p����}�X�^�̃Z�b�g�𐶐�����
        //
        EnumSet<ClaimConst.MasterSet> set = EnumSet.of(
                ClaimConst.MasterSet.DIAGNOSIS);
        
        //
        // �}�X�^�[�Z�b�g�𐶐�����
        //
        masterPanel = new MasterTabPanel(set);
        masterPanel.startDiagnosis(diagnosisTable);
        
        //
        // �S�̂����C�A�E�g����
        //
        setLayout(new BorderLayout(0, GUIConst.DEFAULT_CMP_V_SPACE));
        add(diagnosisTable, BorderLayout.NORTH);
        add(masterPanel, BorderLayout.CENTER);
        setPreferredSize(GUIConst.DEFAULT_STAMP_EDITOR_SIZE);
    }
    
    /**
     * �ҏW�������a����Ԃ��B
     * @return �ҏW���� RegisteredDiagnosisModel
     */
    public Object getValue() {
        return diagnosisTable.getValue();
    }
    
    /**
     * �ҏW���鏝�a����ݒ肷��B
     * @param val �ҏW���� RegisteredDiagnosisModel
     */
    public void setValue(Object val) {
        diagnosisTable.setValue((Object[])val);
    }
    
    /**
     * ���\�[�X���������B
     */
    public void dispose() {
        masterPanel.stopDiagnosis(diagnosisTable);
    }
}