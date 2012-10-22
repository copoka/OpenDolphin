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

import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import open.dolphin.client.IStampModelEditor;
import open.dolphin.infomodel.RegisteredDiagnosisModel;
import open.dolphin.table.ObjectTableModel;

/**
 * ���a���ҏW�e�[�u���� State �N���X�B
 *
 * @author Kazushi Minagawa
 */
public class DiagnosisStateMgr {
    
    /** Event */
    public enum Event {ADDED, DELETED, CLEARED, SELECTED};
    
    /** ��̏�Ԃ�\�� State */
    private final EmptyState empty = new EmptyState();
    
    /** �L���ȏ�Ԃ�\�� State */
    private final ValidState valid = new ValidState();
    
    /** �����ȏ�Ԃ�\�� State */
    private final InValidState invalid = new InValidState();
    
    /** �폜�{�^�� */
    private JButton delete;
    
    /** �N���A�{�^�� */
    private JButton clear;
    
    /** State label */
    private JLabel stateLabel;
    
    /** ���a���ҏW�e�[�u���� TableModel  */
    private ObjectTableModel tableModel;
    
    /** ���a���ҏW�e�[�u�� */
    private JTable table;
    
    /** ���݂� State */
    private DiagnosisState curState;
    
    /** �R���e�L�X�g */
    private IStampModelEditor context;
    
    /** ��{�a���̌� */
    private int baseCnt;
    
    /** �C����������ǂ��� */
    private boolean hasModifier;
    
    /**
     * Creates a new instance of DiagnosisStateMgr
     */    
    public DiagnosisStateMgr(JButton delete, 
                             JButton clear, 
                             JLabel stateLabel,
                             ObjectTableModel tableModel,
                             JTable table,
                             IStampModelEditor context) {
        this.delete = delete;
        this.clear = clear;
        this.stateLabel = stateLabel;
        this.tableModel = tableModel;
        this.table = table;
        this.context = context;
        curState = empty;
    }
    
    /**
     * Event ����������B
     */
    public void processEvent(Event evt) {
        curState = curState.processEvent(evt);
    }
    
    /**
     * State �֑J�ڂ���B
     */
    public void enter() {
        curState.enter();
    }
    
    /**
     * ���� State �𔻒肷��B
     * @return ���� State
     */
    protected DiagnosisState judgeState() {
        
        List dataList = tableModel.getObjectList();
        
        if (dataList == null || dataList.size() == 0) {
            return empty;
        }
        
        hasModifier = false;
        baseCnt = 0;
        
        for (Iterator iter = dataList.iterator(); iter.hasNext(); ) {
            RegisteredDiagnosisModel rm = (RegisteredDiagnosisModel) iter.next();
            if (rm.getDiagnosisCode().startsWith("ZZZ")) {
                hasModifier = true;
            } else {
                baseCnt += 1;
            }
        }
        
        if (hasModifier) {
            //
            // �C���ꂪ����ꍇ�A��{�a���͈�̂�
            //
            return baseCnt == 1 ? valid : invalid;
        } else {
            //
            // 
            //
            return valid;
        }
    }
    
    /**
     * ���ەҏW�e�[�u�� State �N���X�B
     */
    protected abstract class DiagnosisState {
        
        public abstract DiagnosisState processEvent(Event evt);
        
        public abstract void enter();
    }
    
    
    /**
     * EmptyState class.
     */
    private class EmptyState extends DiagnosisState {
         
        public DiagnosisState processEvent(Event evt) {
            
            DiagnosisState next = null;
            
            switch (evt) {
                case ADDED:
                    next = judgeState();
                    next.enter();
                    break;
                    
                case SELECTED:
                    next = this;
                    next.enter();
                    break;
            }
            
            return next;
        } 
        
        public void enter() {
            //System.out.println("enter empty");
            delete.setEnabled(false);
            clear.setEnabled(false);
            stateLabel.setText("���a��������܂���B");
            context.setValidModel(false);
        }
    }
    
    private class ValidState extends DiagnosisState {
        
        public DiagnosisState processEvent(Event evt) {
            
            DiagnosisState next = null;
            
            switch (evt) {
                
                case ADDED:
                    next = judgeState();
                    next.enter();
                    break;
                    
                case DELETED:
                    next = judgeState();
                    next.enter();
                    break; 
                    
                case CLEARED:
                    next = empty;
                    next.enter();
                    break;
                    
                case SELECTED:
                    next = this;
                    next.enter();
                    break;                    
            }
            
            return next;
        }        
        
        public void enter() {
            //System.out.println("enter valid");
            int row = table.getSelectedRow();
            boolean b = tableModel.isValidRow(row);
            delete.setEnabled(b);
            clear.setEnabled(true);
            stateLabel.setText("�L���ȃf�[�^�ɂȂ��Ă��܂��B");
            context.setValidModel(true);
        }
        
    }
    
    private class InValidState extends DiagnosisState {
        
         public DiagnosisState processEvent(Event evt) {
            
            DiagnosisState next = null;
            
            switch (evt) {
                
                case ADDED:
                    next = judgeState();
                    next.enter();
                    break;
                    
                case DELETED:
                    next = judgeState();
                    next.enter();
                    break; 
                    
                case CLEARED:
                    next = empty;
                    next.enter();
                    break;
                    
                case SELECTED:
                    next = this;
                    next.enter();
                    break;                    
            }
            
            return next;
        }               
        
        public void enter() {
            //System.out.println("enter invalid");
            int row = table.getSelectedRow();
            boolean b = tableModel.isValidRow(row);
            delete.setEnabled(b);
            clear.setEnabled(true);
            
            if (baseCnt == 0) {
                stateLabel.setText("��{���a��������܂���B");
            } else {
                stateLabel.setText("�C���ꂪ����ꍇ�́A��{���a���͈����������܂���B");
            }
            context.setValidModel(false);
        }
    }
}
