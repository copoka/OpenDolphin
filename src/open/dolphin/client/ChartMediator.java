/*
 * ChartMediator.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2004-2005 Digital Globe, Inc. All rights reserved.
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
import javax.swing.event.MenuEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import open.dolphin.infomodel.IInfoModel;
import open.dolphin.plugin.helper.ChainAction;
import open.dolphin.plugin.helper.MenuSupport;
import open.dolphin.project.Project;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.*;
import open.dolphin.infomodel.PVTHealthInsuranceModel;

/**
 * Mediator class to control Karte Window Menu.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class ChartMediator extends MenuSupport implements UndoableEditListener, ActionListener {
    
    protected enum CompState{NONE, SOA, SOA_TEXT, SCHEMA, P, P_TEXT, STAMP};
    
    private Action undoAction;
    private Action redoAction;
    
    // �I�����N�����Ă��� KartePane
    private KartePane curPane;
    private JComponent curComp;
    
    // ChartPlugin
    private IChart chart;
    
    // Undo Manager
    private UndoManager undoManager = new UndoManager();
    
    public ChartMediator(Object owner) {
        super(owner);
        chart = (IChart) owner;
    }
    
    public void registerActions(Hashtable<String, Action> map) {
        
        super.registerActions(map);
        
        undoAction = map.get(GUIConst.ACTION_UNDO);
        redoAction = map.get(GUIConst.ACTION_REDO);
        
        // �����~���� Preference ����擾���ݒ肵�Ă���
        boolean asc = Project.getPreferences().getBoolean(Project.DOC_HISTORY_ASCENDING, false);
        if (asc) {
            Action a = map.get(GUIConst.ACTION_ASCENDING);
            JRadioButtonMenuItem rdi = (JRadioButtonMenuItem) a.getValue("menuItem");
            rdi.setSelected(true);
        } else {
            Action desc = map.get(GUIConst.ACTION_DESCENDING);
            JRadioButtonMenuItem rdi = (JRadioButtonMenuItem) desc.getValue("menuItem");
            rdi.setSelected(true);
        }
    }
    
    public void dispose() {
        Hashtable<String , Action> actions = getActions();
        Iterator<String> iter = actions.keySet().iterator();
        while (iter.hasNext()) {
            Action a = actions.get(iter.next());
            if (a instanceof ChainAction) {
                ((ChainAction)a).setTarget(null);
            } else if (a instanceof ReflectAction) {
                ((ReflectAction)a).setTarget(null);
            }
        }
        actions.clear();
    }
    
    public void actionPerformed(ActionEvent e) {
    }
    
    public JComponent getCurrentComponent() {
        return curComp;
    }
    
    /**
     * Focus���ꂽ JTextPane���Z�b�g����B
     */
    public void setCurrentComponent(JComponent newCompo) {
        curComp = newCompo;
    }
    
    /**
     * ���j���[���X�i�̎����B
     * �}���y�уe�L�X�g���j���[���I�����ꂽ���̏������s���B
     */
    public void menuSelected(MenuEvent e) {
        
        // �}���ƃe�L�X�g���j���[�Ƀ��X�i���o�^����Ă���
        JMenu selectedMenu = (JMenu) e.getSource();
        String cmd = selectedMenu.getActionCommand();
        
        //
        // �}�����j���[�̎�
        // StampBox �̃c���[�����j���[�ɂ���
        //
        if (cmd.equals(GUIConst.MENU_INSERT)) {
            
            selectedMenu.removeAll();
            
            // StampBox �̑S�c���[���擾����
            List<StampTree> trees = getStampBox().getAllTrees();
            
            // �c���[���C�e���[�g����
            for (StampTree tree : trees) {
                
                // �c���[�̃G���e�B�e�B���擾����
                String entity = tree.getEntity();
                
                if (entity.equals(IInfoModel.ENTITY_DIAGNOSIS)) {
                    // ���a���̎��A���a�����j���[���\�z���ǉ�����
                    selectedMenu.add(createDiagnosisMenu(tree));
                    selectedMenu.addSeparator();
                    
                } else if (entity.equals(IInfoModel.ENTITY_TEXT)) {
                    // �e�L�X�g�̎��A�e�L�X�g���j���[���\�z���ǉ�����
                    selectedMenu.add(createTextMenu(tree));
                    selectedMenu.addSeparator();
                    
                } else {
                    // �ʏ��P�I�[�_�̎�
                    selectedMenu.add(createStampMenu(tree));
                }
            }
        } 
        
        else if (cmd.equals(GUIConst.MENU_TEXT)) {
            //
            // �e�L�X�g���j���[�̏ꍇ�A�X�^�C���𐧌䂷��
            //
            adjustStyleMenu();
        }
    }
    
    public void menuDeselected(MenuEvent e) {
    }
    
    public void menuCanceled(MenuEvent e) {
    }
    
    /**
     * �t�H�[�}�b�g�֘A���j���[�𒲐�����B
     * @param kartePane
     */
    private void adjustStyleMenu() {
        
        boolean enabled = false;
        KartePane kartePane = null;
        if (getChain() instanceof KarteEditor) {
            KarteEditor editor = (KarteEditor) getChain();
            kartePane = editor.getSOAPane();
            enabled = (kartePane.getTextPane().isEditable()) ? true : false;
        }
        
        // �T�u���j���[�𐧌䂷��
        getAction(GUIConst.ACTION_SIZE).setEnabled(enabled);
        getAction(GUIConst.ACTION_STYLE).setEnabled(enabled);
        getAction(GUIConst.ACTION_ALIGNMENT).setEnabled(enabled);
        getAction(GUIConst.ACTION_COLOR).setEnabled(enabled);
        
        // ���j���[�A�C�e���𐧌䂷��
        getAction(GUIConst.ACTION_RESET_STYLE).setEnabled(enabled);
        
        getAction(GUIConst.ACTION_RED).setEnabled(enabled);
        getAction(GUIConst.ACTION_ORANGE).setEnabled(enabled);
        getAction(GUIConst.ACTION_YELLOW).setEnabled(enabled);
        getAction(GUIConst.ACTION_GREEN).setEnabled(enabled);
        getAction(GUIConst.ACTION_BLUE).setEnabled(enabled);
        getAction(GUIConst.ACTION_PURPLE).setEnabled(enabled);
        getAction(GUIConst.ACTION_GRAY).setEnabled(enabled);
        
        getAction(GUIConst.ACTION_S9).setEnabled(enabled);
        getAction(GUIConst.ACTION_S10).setEnabled(enabled);
        getAction(GUIConst.ACTION_S12).setEnabled(enabled);
        getAction(GUIConst.ACTION_S14).setEnabled(enabled);
        getAction(GUIConst.ACTION_S18).setEnabled(enabled);
        getAction(GUIConst.ACTION_S24).setEnabled(enabled);
        getAction(GUIConst.ACTION_S36).setEnabled(enabled);
        
        getAction(GUIConst.ACTION_BOLD).setEnabled(enabled);
        getAction(GUIConst.ACTION_ITALIC).setEnabled(enabled);
        getAction(GUIConst.ACTION_UNDERLINE).setEnabled(enabled);
        
        getAction(GUIConst.ACTION_LEFT_ALIGN).setEnabled(enabled);
        getAction(GUIConst.ACTION_CENTER_ALIGN).setEnabled(enabled);
        getAction(GUIConst.ACTION_RIGHT_ALIGN).setEnabled(enabled);
    }
    
    /**
     * �X�^���vTree���珝�a�����j���[���\�z����B
     * @param insertMenu �e�L�X�g���j���[
     */
    private JMenu createDiagnosisMenu(StampTree stampTree) {
        
        //
        // chain�̐擪��DiagnosisDocument�̎��̂ݎg�p�\�Ƃ���
        //
        JMenu myMenu = null;
        DiagnosisDocument diagnosis = null;
        boolean enabled = false;
        Object obj = getChain();
        if (obj instanceof DiagnosisDocument) {
            diagnosis = (DiagnosisDocument) obj;
            enabled = true;
        }
        
        if (!enabled) {
            // cjain�̐擪��Diagnosis�łȂ��ꍇ�͂߂ɂ�[��disable�ɂ���
            myMenu = new JMenu(stampTree.getTreeName());
            myMenu.setEnabled(false);
            
        } else {
            // ���a��Tree�A�e�[�u���A�n���h�����烁�j���[���\�z����
            JComponent comp = diagnosis.getDiagnosisTable();
            TransferHandler handler = comp.getTransferHandler();
            StmapTreeMenuBuilder builder = new StmapTreeMenuBuilder();
            myMenu = builder.build(stampTree, comp, handler);
        }
        return myMenu;
    }
    
    /**
     * �X�^���vTree����e�L�X�g���j���[���\�z����B
     * @param insertMenu �e�L�X�g���j���[
     */
    private JMenu createTextMenu(StampTree stampTree) {
        
        // chain �̐擪�� KarteEditor �ł��� SOAane ���ҏW�̏ꍇ�̂݃��j���[���g����
        JMenu myMenu = null;
        boolean enabled = false;
        KartePane kartePane = null;
        Object obj = getChain();
        if (obj instanceof KarteEditor) {
            KarteEditor editor = (KarteEditor) obj;
            kartePane = editor.getSOAPane();
            enabled = (kartePane.getTextPane().isEditable()) ? true : false;
        }
        
        if (!enabled) {
            myMenu = new JMenu(stampTree.getTreeName());
            myMenu.setEnabled(false);
            
        } else {
            //
            // TextTree�AJTextPane�Ahandler ���烁�j���[���\�z����
            // PPane �ɂ����Ƃ��Ȃ���΂Ȃ�Ȃ� TODO
            //JComponent comp = kartePane.getTextPane();
            // TransferHandler handler = comp.getTransferHandler();
            
            // 2007-03-31
            // ���߂Ńt�H�[�J�X�𓾂Ă���R���|�[�l���g(JTextPan�j�֑}������
            //
            JComponent comp = getCurrentComponent();
            if (comp == null) {
                comp = kartePane.getTextPane();
            }
            TransferHandler handler = comp.getTransferHandler();
            StmapTreeMenuBuilder builder = new StmapTreeMenuBuilder();
            myMenu = builder.build(stampTree, comp, handler);
        }
        
        return myMenu;
    }
    
    /**
     * �X�^���v���j���[���\�z����B
     * @param insertMenu �X�^���v���j���[
     */
    private JMenu createStampMenu(StampTree stampTree) {
        
        // chain �̐擪�� KarteEditor �ł��� Pane ���ҏW�̏ꍇ�̂݃��j���[���g����
        JMenu myMenu = null;
        boolean enabled = false;
        KartePane kartePane = null;
        Object obj = getChain();
        if (obj instanceof KarteEditor) {
            KarteEditor editor = (KarteEditor) obj;
            kartePane = editor.getPPane();
            enabled = (kartePane.getTextPane().isEditable()) ? true : false;
        }
        
        if (!enabled) {
            myMenu = new JMenu(stampTree.getTreeName());
            myMenu.setEnabled(false);
            
        } else {
            // StampTree�AJTextPane�AHandler ���烁�j���[���\�z����
            JComponent comp = kartePane.getTextPane();
            TransferHandler handler = comp.getTransferHandler();
            StmapTreeMenuBuilder builder = new StmapTreeMenuBuilder();
            myMenu = builder.build(stampTree, comp, handler);
        }
        return myMenu;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * �����̃|�b�v�A�b�v���j���[�֏��a�����j���[��ǉ�����B
     * @param popup ���a�����j���[��ǉ�����|�b�v�A�b�v���j���[
     */
    public void addDiseaseMenu(JPopupMenu popup) {
        
        // Chain�̐擪��DiagnosisDocument�̎��̂ݒǉ�����
        boolean enabled = false;
        DiagnosisDocument diagnosis = null;
        Object obj = getChain();
        if (obj instanceof DiagnosisDocument) {
            diagnosis = (DiagnosisDocument) obj;
            enabled = true;
        }
        
        StampTree stampTree = getStampBox().getStampTree(IInfoModel.ENTITY_DIAGNOSIS);
        
        if (stampTree != null) {
        
            if (!enabled) {
                JMenu myMenu = new JMenu(stampTree.getTreeName());
                myMenu.setEnabled(false);
                popup.add(myMenu);
                return;
            } else {
                JComponent comp = diagnosis.getDiagnosisTable();
                TransferHandler handler = comp.getTransferHandler();
                StmapTreePopupBuilder builder = new StmapTreePopupBuilder();
                builder.build(stampTree, popup, comp, handler);
            }
        }
    }
    
    /**
     * �����̃|�b�v�A�b�v���j���[�փe�L�X�g���j���[��ǉ�����B
     * @param popup �e�L�X�g���j���[��ǉ�����|�b�v�A�b�v���j���[
     */
    public void addTextMenu(JPopupMenu popup) {
        
        boolean enabled = false;
        KartePane kartePane = null;
        Object obj = getChain();
        if (obj instanceof KarteEditor) {
            KarteEditor editor = (KarteEditor) obj;
            kartePane = editor.getSOAPane();
            enabled = (kartePane.getTextPane().isEditable()) ? true : false;
        }
        
        StampTree stampTree = getStampBox().getStampTree(IInfoModel.ENTITY_TEXT);
        
        // ASP �X�^���v�{�b�N�X�� entity �ɑΉ����� Tree ���Ȃ��ꍇ������
        if (stampTree != null) {
        
            if (!enabled) {
                JMenu myMenu = new JMenu(stampTree.getTreeName());
                myMenu.setEnabled(false);
                popup.add(myMenu);
                return;
                
            } else {
                JComponent comp = getCurrentComponent();
                if (comp == null) {
                    comp = kartePane.getTextPane();
                }
                TransferHandler handler = comp.getTransferHandler();
                StmapTreePopupBuilder builder = new StmapTreePopupBuilder();
                builder.build(stampTree, popup, comp, handler);
            }
        }
    }
    
    /**
     * PPane �̃R���e�L�X�g���j���[�܂��̓c�[���o�[�� stampIcon �փX�^���v���j���[��ǉ�����B
     * @param menu Ppane �̃R���e�L�X�g���j���[
     * @param kartePane PPnae
     */
    public void addStampMenu(JPopupMenu menu, final KartePane kartePane) {
        
        // ������Pane��P���ҏW�̎��̂ݒǉ�����
        // �R���e�L�X�g���j���[�Ȃ̂ł����OK
        if (kartePane.getMyRole().equals(IInfoModel.ROLE_P) && kartePane.getTextPane().isEditable()) {
            
            StampBoxPlugin stampBox = getStampBox();
            List<StampTree> trees = stampBox.getAllTrees();
            
            StmapTreeMenuBuilder builder = new StmapTreeMenuBuilder();
            JComponent cmp = kartePane.getTextPane();
            TransferHandler handler = cmp.getTransferHandler();
            
            // StampBox���̑STree���C�e���[�g����
            for (StampTree stampTree : trees) {
                
                // ���a���ƃe�L�X�g�͕ʂɍ쐬����̂ŃX�L�b�v����
                String entity = stampTree.getEntity();
                if (entity.equals(IInfoModel.ENTITY_DIAGNOSIS) || entity.equals(IInfoModel.ENTITY_TEXT)) {
                    continue;
                }
                
                JMenu subMenu = builder.build(stampTree, cmp, handler);
                menu.add(subMenu);
            }   
        }
    }
    
    /**
     * �����̃|�b�v�A�b�v���j���[�փX�^���v���j���[��ǉ�����B
     * ���̃��\�b�h�̓c�[���o�[�� stamp icon �� actionPerformed ����R�[�������B
     * @param popup
     */
    public void addStampMenu(JPopupMenu popup) {
        
        boolean enabled = false;
        KartePane kartePane = null;
        Object obj = getChain();
        if (obj instanceof KarteEditor) {
            KarteEditor editor = (KarteEditor) obj;
            kartePane = editor.getPPane();
            enabled = (kartePane.getTextPane().isEditable()) ? true : false;
        }
        
        if (enabled) {
            addStampMenu(popup, kartePane);
        }
    }
    
    public StampTree getStampTree(String entity) {
        
        StampTree stampTree = getStampBox().getStampTree(entity);
        return stampTree;
    }
    
    public StampBoxPlugin getStampBox() {
        return (StampBoxPlugin)chart.getContext().getPlugin("mainWindow/stampBox");
    }
    
    public boolean hasTree(String entity) {
        StampBoxPlugin stBox = (StampBoxPlugin)chart.getContext().getPlugin("mainWindow/stampBox");
        StampTree tree = stBox.getStampTree(entity);
        return tree != null ? true : false;
    }
    
    public void applyInsurance(PVTHealthInsuranceModel hm) {
        
        Object target = getChain();
        if (target != null) {
            try {
                Method m = target.getClass().getMethod("applyInsurance", new Class[]{hm.getClass()});
                m.invoke(target, new Object[]{hm});
                
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }
 
    
    ///////////////////////////////////////////////////////////////////////////
    
    public void cut() {
        JComponent focusOwner = getCurrentComponent();
        if (focusOwner != null) {
            Action a = focusOwner.getActionMap().get(TransferHandler.getCutAction().getValue(Action.NAME));
            if (a != null) {
                a.actionPerformed(new ActionEvent(focusOwner,
                        ActionEvent.ACTION_PERFORMED,
                        null));
            }
        }
    }
    
    public void copy() {
        JComponent focusOwner = getCurrentComponent();
        if (focusOwner != null) {
            Action a = focusOwner.getActionMap().get(TransferHandler.getCopyAction().getValue(Action.NAME));
            if (a != null) {
                a.actionPerformed(new ActionEvent(focusOwner,
                        ActionEvent.ACTION_PERFORMED,
                        null));
            }
        }
    }
    
    public void paste() {
        JComponent focusOwner = getCurrentComponent();
        if (focusOwner != null) {
            Action a = focusOwner.getActionMap().get(TransferHandler.getPasteAction().getValue(Action.NAME));
            if (a != null) {
                a.actionPerformed(new ActionEvent(focusOwner,
                        ActionEvent.ACTION_PERFORMED,
                        null));
            }
        }
    }
    
    public void delete() {
    }
    
    public void resetStyle() {
        JComponent focusOwner = getCurrentComponent();
        if (focusOwner != null && focusOwner instanceof JTextPane) {
            JTextPane pane = (JTextPane) focusOwner;
            pane.setCharacterAttributes(SimpleAttributeSet.EMPTY, true);
        }
    }
    
    public void undoableEditHappened(UndoableEditEvent e) {
        undoManager.addEdit(e.getEdit());
        updateUndoAction();
        updateRedoAction();
    }
    
    public void undo() {
        try {
            undoManager.undo();
            
        } catch (CannotUndoException ex) {
            ex.printStackTrace();
        }
        updateUndoAction();
        updateRedoAction();
    }
    
    public void redo() {
        try {
            undoManager.redo();
        } catch (CannotRedoException ex) {
            ex.printStackTrace();
        }
        updateRedoAction();
        updateUndoAction();
    }
    
    private void updateUndoAction() {
        
        if(undoManager.canUndo()) {
            undoAction.setEnabled(true);
        } else {
            undoAction.setEnabled(false);
        }
    }
    
    private void updateRedoAction() {
        
        if(undoManager.canRedo()) {
            redoAction.setEnabled(true);
        } else {
            redoAction.setEnabled(false);
        }
    }
}