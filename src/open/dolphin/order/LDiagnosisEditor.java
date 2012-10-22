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

/**
 * Diagnosis editor.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class LDiagnosisEditor extends StampModelEditor  {
    
    private static final String EDITOR_NAME = "���a��";
    
    /** ���a���ҏW�e�[�u�� */
    private DiagnosisTablePanel diagnosisTable;
    
    /** �}�X�^�[�Z�b�g�p�l�� */
    private MasterSetPanel masterPanel;
    
    
    /** 
     * Creates new DiagnosisEditor 
     */
    public LDiagnosisEditor(IStampEditorDialog context, MasterSetPanel masterPanel) {
        setContext(context);
        this.masterPanel = masterPanel;
        initComponent();
    }
    
    /**
     * �G�f�B�^���J�n����B
     */
    public void start() {
        masterPanel.startDiagnosis(diagnosisTable);
    }
    
    /**
     * Component������������B
     */
    private void initComponent() {
        
        setTitle(EDITOR_NAME);
        
        //
        // ���a���ҏW�e�[�u��
        // �}�X�^�[�Z�b�g�p�l��
        // �𐶐������C�A�E�g����
        //
        diagnosisTable = new DiagnosisTablePanel(this);
        Border b = BorderFactory.createEtchedBorder();
        diagnosisTable.setBorder(BorderFactory.createTitledBorder(b, EDITOR_NAME));
        
        setLayout(new BorderLayout(0, GUIConst.DEFAULT_CMP_V_SPACE));
        add(diagnosisTable, BorderLayout.CENTER);
    }
    
    /**
     * �ҏW�������a����Ԃ��B
     * @return RegisteredDiagnosisModel
     */
    public Object getValue() {
        return diagnosisTable.getValue();
    }
    
    /**
     * �ҏW���鏝�a����ݒ肷��B
     * @param val RegisteredDiagnosisModel
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