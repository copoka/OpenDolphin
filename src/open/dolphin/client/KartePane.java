/*
 * KartePane.java
 * Copyright(C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2003-2006 Digital Globe, Inc. All rights reserved.
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

import java.awt.im.InputSubset;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import open.dolphin.client.ChartMediator.CompState;
import open.dolphin.dao.SqlOrcaSetDao;
import open.dolphin.delegater.StampDelegater;
import open.dolphin.infomodel.ClaimBundle;
import open.dolphin.infomodel.ExtRefModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.SchemaModel;
import open.dolphin.infomodel.StampModel;
import open.dolphin.util.BeanUtils;

import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.beans.*;

import jp.ac.kumamoto_u.kuh.fc.jsato.swing_beans.SchemaEditorDialog;

/**
 * Karte Pane
 *
 * @author Kazushi Minagawa, Digital Globe, inc.
 */
public class KartePane implements PropertyChangeListener {
    
    /** SOA �y�� P Pane �̕� */
    public static final int PANE_WIDTH = 345;
    
    /** SOA �y�� P Pane �̍ő卂�� */
    public static final int PANE_HEIGHT = 3*700;
    
    /** 2���J���e�̕������� */
    public static final int PANE_DIVIDER_WIDTH = 2;
    
    private static final int MARGIN_LEFT = 10;
    
    /** Pane �̏�}�[�W�� */
    private static final int MARGIN_TOP = 10;
    
    /** Pane �̉E�}�[�W�� */
    private static final int MARGIN_RIGHT = 10;
    
    /** Pane �̉��}�[�W�� */
    private static final int MARGIN_BOTTOM = 10;
    
    
    private static final int TITLE_LENGTH = 15;
    
    private static final Color UNEDITABLE_COLOR = new Color(227, 250, 207);
    
    // JTextPane (���̃N���X��JTextPane�ւ̃f�R���[�^�I������S��)
    private JTextPane textPane;
    
    // SOA / P �̃��[��
    private String myRole;
    
    // �����KartePane
    private KartePane myPartner;
    
    // ����KartePane�̃I�[�i
    private KarteEditor parent;
    
    // StampHolder��TransferHandler
    private StampHolderTransferHandler stampHolderTransferHandler;
    
    // SchemaHolder��TransferHandler
    private SchemaHolderTransferHandler schemaHolderTransferHandler;
    
    private int stampId;
    
    // Dirty Flag
    private boolean dirty;
    
    // ���������ꂽ����Document�̒���
    private int initialLength;
    
    // ChartMediator(MenuSupport)
    private ChartMediator mediator;
    
    // ���̃I�u�W�F�N�g�Ő������镶��DocumentModel�̕���ID
    private String docId;
    
    // �ۑ���y�уu���E�Y���̕ҏW�s��\���J���[
    private Color uneditableColor = UNEDITABLE_COLOR;
    
    // ���̃y�C������Dragg�y��Dropp���ꂽ�X�^���v�̏��
    private IComponentHolder[] drragedStamp;
    private int draggedCount;
    private int droppedCount;
    
    //
    // Listeners
    //
    private ContextListener contextListener;
    private FocusCaretListener focusCaret;
    private DocumentListener dirtyListner;
    
    
    /** 
     * Creates new KartePane2 
     */
    public KartePane() {
        //
        // StyledDocument�𐶐���JTextPane�𐶐�����
        //
        KarteStyledDocument doc = new KarteStyledDocument();
        setTextPane(new JTextPane(doc));
        
        //
        // ��{������ݒ肷��
        //
        getTextPane().setMinimumSize(new Dimension(PANE_WIDTH, PANE_WIDTH));
        getTextPane().setMaximumSize(new Dimension(PANE_WIDTH, PANE_HEIGHT));
        getTextPane().setMargin(new Insets(MARGIN_TOP, MARGIN_LEFT, MARGIN_BOTTOM, MARGIN_RIGHT));
        getTextPane().setAlignmentY(Component.TOP_ALIGNMENT);
        doc.setParent(this);
        stampHolderTransferHandler = new StampHolderTransferHandler();
        schemaHolderTransferHandler = new SchemaHolderTransferHandler();
    }
    
    /**
     * ����Pane�̃I�[�i��ݒ肷��B
     * @param parent KarteEditor�I�[�i
     */
    protected void setParent(KarteEditor parent) {
        this.parent = parent;
    }
    
    /**
     * ����Pane�̃I�[�i��Ԃ��B
     * @return KarteEditor�I�[�i
     */
    protected KarteEditor getParent() {
        return parent;
    }
    
    /**
     * �ҏW�s��\���J���[��ݒ肷��B
     * @param uneditableColor �ҏW�s��\���J���[
     */
    public void setUneditableColor(Color uneditableColor) {
        this.uneditableColor = uneditableColor;
    }
    
    /**
     * �ҏW�s��\���J���[��Ԃ��B
     * @return �ҏW�s��\���J���[
     */
    public Color getUneditableColor() {
        return uneditableColor;
    }
    
    /**
     * ����Pane�Ő�������DocumentModel�̕���ID��ݒ肷��B
     * @param docId ����ID
     */
    protected void setDocId(String docId) {
        this.docId = docId;
    }
    
    /**
     * ����Pane�Ő�������DocumentModel�̕���ID��Ԃ��B
     * @return ����ID
     */
    protected String getDocId() {
        return docId;
    }
    
    /**
     * ChartMediator��ݒ肷��B
     * @param mediator ChartMediator
     */
    protected void setMediator(ChartMediator mediator) {
        this.mediator = mediator;
    }
    
    /**
     * ChartMediator��Ԃ��B
     * @return ChartMediator
     */
    protected ChartMediator getMediator() {
        return mediator;
    }
    
    /**
     * �p�[�g�iPane��ݒ肷��B
     * @param myPartner �p�[�g�iPane
     */
    protected void setMyPartner(KartePane myPartner) {
        this.myPartner = myPartner;
    }
    
    /**
     * �p�[�g�iPane��Ԃ��B
     * @return �p�[�g�iPane
     */
    protected KartePane getMyPartner() {
        return myPartner;
    }
    
    /**
     * ����Pane�̃��[����ݒ肷��B
     * @param myRole SOA�܂���P�̃��[��
     */
    public void setMyRole(String myRole) {
        this.myRole = myRole;
    }
    
    /**
     *  ����Pane�̃��[����Ԃ��B
     * @return SOA�܂���P�̃��[��
     */
    public String getMyRole() {
        return myRole;
    }
    
    /**
     * JTextPane��ݒ肷��B
     * @param textPane JTextPane
     */
    protected void setTextPane(JTextPane textPane) {
        this.textPane = textPane;
    }
    
    /**
     * JTextPane��Ԃ��B
     * @return JTextPane
     */
    protected JTextPane getTextPane() {
        return textPane;
    }
    
    /**
     * JTextPane��StyledDocument��Ԃ��B
     * @return JTextPane��StyledDocument
     */
    protected KarteStyledDocument getDocument() {
        return (KarteStyledDocument) getTextPane().getDocument();
    }
    
    /**
     * ��������ݒ肷��B
     * @param Document�̏�����
     */
    protected void setInitialLength(int initialLength) {
        this.initialLength = initialLength;
    }
    
    /**
     * ��������Ԃ��B
     * @return Document�̏�����
     */
    protected int getInitialLength() {
        return initialLength;
    }
    
    /**
     * ����Pane����Drag���ꂽ�X�^���v����Ԃ��B
     * @return ����Pane����Drag���ꂽ�X�^���v��
     */
    protected int getDraggedCount() {
        return draggedCount;
    }
    
    /**
     * ����Pane����Drag���ꂽ�X�^���v����ݒ肷��B
     * @param draggedCount ����Pane����Drag���ꂽ�X�^���v��
     */
    protected void setDraggedCount(int draggedCount) {
        this.draggedCount = draggedCount;
    }
    
    /**
     * ����Pane��Drop���ꂽ�X�^���v����Ԃ��B
     * @return ����Pane��Drop���ꂽ�X�^���v��
     */
    protected int getDroppedCount() {
        return droppedCount;
    }
    
    /**
     * ����Pane��Drop���ꂽ�X�^���v����ݒ肷��B
     * @param droppedCount ����Pane��Drop���ꂽ�X�^���v��
     */
    protected void setDroppedCount(int droppedCount) {
        this.droppedCount = droppedCount;
    }
    
    /**
     * ����Pane����Drag���ꂽ�X�^���v��Ԃ��B
     * @return ����Pane����Drag���ꂽ�X�^���v�z��
     */
    protected IComponentHolder[] getDrragedStamp() {
        return drragedStamp;
    }
    
    /**
     * ����Pane����Drag���ꂽ�X�^���v��ݒ�i�L�^�j����B
     * @param drragedStamp ����Pane����Drag���ꂽ�X�^���v�z��
     */
    protected void setDrragedStamp(IComponentHolder[] drragedStamp) {
        this.drragedStamp = drragedStamp;
    }
    
    /**
     * ����������B
     * @param editable �ҏW�\���ǂ����̃t���O
     * @param mediator �`���[�g���f�B�G�[�^�i���ۂɂ̓��j���[�T�|�[�g�j
     */
    public void init(boolean editable, ChartMediator mediator) {
        
        // Mediator��ۑ�����
        setMediator(mediator);
        
        // JTextPane�փA�N�V������o�^����
        // Undo & Redo
        ActionMap map = getTextPane().getActionMap();
        KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        map.put(keystroke, mediator.getAction(GUIConst.ACTION_UNDO));
        keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        map.put(keystroke, mediator.getAction(GUIConst.ACTION_REDO));
        
        // Drag �� editable �Ɋ֌W�Ȃ��\
        getTextPane().setDragEnabled(true);
        
        // Dirty ����p�� DocumentListener ��ݒ肷��
        final KarteStyledDocument doc = getDocument();
        
        if (getTextPane().isEditable()) {
            
            // �X�^���v�}���オ�������ɂȂ�
            setInitialLength(0);
            
            dirtyListner = new DocumentListener() {
                
                // JTextPane�ւ̑}����dirty���ǂ����𔻒肷��
                public void insertUpdate(DocumentEvent e) {
                    boolean newDirty = doc.getLength() > getInitialLength() ? true : false;
                    if (newDirty != dirty) {
                        dirty = newDirty;
                        // KarteEditor �֒ʒm����
                        getParent().setDirty(dirty);
                    }
                }
                
                // �폜���N��������dirty���ǂ����𔻒肷��
                public void removeUpdate(DocumentEvent e) {
                    boolean newDirty = doc.getLength() > getInitialLength() ? true : false;
                    if (newDirty != dirty) {
                        dirty = newDirty;
                        // KarteEditor �֒ʒm
                        getParent().setDirty(dirty);
                    }
                }
                
                public void changedUpdate(DocumentEvent e) {
                }
            };
            
            doc.addDocumentListener(dirtyListner);
        }
        
        // �R���e�L�X�g���j���[�p�̃��X�i�ݒ肷��
        contextListener = new ContextListener(this, mediator);
        getTextPane().addMouseListener(contextListener);
        
        // Focus�ƃL�����b�g�̃R���g���[���𐶐�����
        focusCaret = new FocusCaretListener(getTextPane(), mediator);
        
        // TextPane��FocusListener��ǉ�����
        getTextPane().addFocusListener(focusCaret);
        
        // TextPane��CaretListener��ǉ�����
        getTextPane().addCaretListener(focusCaret);
        
        // Editable Property ��ݒ肷��
        setEditableProp(editable);
    }
    
    /**
     * ���\�[�X�����肠����B
     */
    public void clear() {
        
        if (dirtyListner != null) {
            getTextPane().getDocument().removeDocumentListener(dirtyListner);
            dirtyListner = null;
        }
        
        if (contextListener != null) {
            getTextPane().removeMouseListener(contextListener);
            contextListener = null;
        }
        
        if (focusCaret != null) {
            getTextPane().removeFocusListener(focusCaret);
            getTextPane().removeCaretListener(focusCaret);
            focusCaret = null;
        }
        
        try {
            KarteStyledDocument doc = getDocument();
            doc.remove(0, doc.getLength());
            doc = null;
        } catch (Exception e) {
            
        }
        
        setTextPane(null);
    }
    
    /**
     * FocusCaretListener
     */
    class FocusCaretListener implements FocusListener, CaretListener {
        
        private JTextPane myPane;
        private ChartMediator myMediator;
        private boolean hasSelection;
        private CompState curState;
        
        public FocusCaretListener(JTextPane myPane, ChartMediator mediator) {
            this.myPane = myPane;
            myMediator = mediator;
        }
        
        /**
         * Focus���ꂽ���̃��j���[�𐧌䂷��B
         */
        public void focusGained(FocusEvent e) {
            //System.out.println(getMyRole() + " gained");
            //
            // ���� IME on
            //
            myPane.getInputContext().setCharacterSubsets(new Character.Subset[] {InputSubset.KANJI});
            // 
            // Mediator�� curCompo �ɐݒ肷��
            // Mediator�̓t�H�[�J�X���������pane��ۑ����Ă���
            //
            myMediator.setCurrentComponent(myPane);
            
            // Menu�𐧌䂷��
            curState = getMyRole().equals(IInfoModel.ROLE_SOA) ? CompState.SOA : CompState.P;
            controlMenus();
        }
        
        /**
         * FocusLost�̏������s���B
         */
        public void focusLost(FocusEvent e) {
            //System.out.println(getMyRole() + " lost");
            hasSelection = false;
            //curState = CompState.NONE;
            //controlMenus();
        }
        
        /**
         * Caret�X�V���̏������s���B
         */
        public void caretUpdate(CaretEvent e) {
            boolean newSelection =  (e.getDot() != e.getMark()) ? true : false;
            if (newSelection != hasSelection) {
                hasSelection = newSelection;
                // �e�L�X�g�I���̏�Ԃ֑J�ڂ���
                if (hasSelection) {
                    curState = getMyRole().equals(IInfoModel.ROLE_SOA) ? CompState.SOA_TEXT : CompState.P_TEXT;
                } else {
                    curState = getMyRole().equals(IInfoModel.ROLE_SOA) ? CompState.SOA : CompState.P;
                }
                controlMenus();
            }
        }
        
        /**
         * ���j���[�𐧌䂷��B
         *
         */
        private void controlMenus() {
            
            // �S���j���[��disable�ɂ���
            myMediator.getAction(GUIConst.ACTION_CUT).setEnabled(false);
            myMediator.getAction(GUIConst.ACTION_COPY).setEnabled(false);
            myMediator.getAction(GUIConst.ACTION_PASTE).setEnabled(false);
            myMediator.getAction(GUIConst.ACTION_UNDO).setEnabled(false);
            myMediator.getAction(GUIConst.ACTION_REDO).setEnabled(false);
            myMediator.getAction(GUIConst.ACTION_INSERT_TEXT).setEnabled(false);
            myMediator.getAction(GUIConst.ACTION_INSERT_SCHEMA).setEnabled(false);
            myMediator.getAction(GUIConst.ACTION_INSERT_STAMP).setEnabled(false);
            
            // �eState��enable�ɂȂ�����������Ǘ�����
            switch (curState) {
                
                case NONE:
                    break;
                    
                case SOA:
                    // SOAPane��Focus������e�L�X�g�I�����Ȃ����
                    if (myPane.isEditable()) {
                        myMediator.getAction(GUIConst.ACTION_PASTE).setEnabled(canPaste());
                        myMediator.getAction(GUIConst.ACTION_INSERT_TEXT).setEnabled(true);
                        myMediator.getAction(GUIConst.ACTION_INSERT_SCHEMA).setEnabled(true);
                    }
                    break;
                    
                case SOA_TEXT:
                    // SOAPane��Focus������e�L�X�g�I����������
                    myMediator.getAction(GUIConst.ACTION_CUT).setEnabled(myPane.isEditable());
                    myMediator.getAction(GUIConst.ACTION_COPY).setEnabled(true);
                    boolean pasteOk = (myPane.isEditable() && canPaste()) ? true : false;
                    myMediator.getAction(GUIConst.ACTION_PASTE).setEnabled(pasteOk);
                    break;
                    
                case P:
                    // PPane��Focus������e�L�X�g�I�����Ȃ����
                    if (myPane.isEditable()) {
                        myMediator.getAction(GUIConst.ACTION_PASTE).setEnabled(canPaste());
                        myMediator.getAction(GUIConst.ACTION_INSERT_TEXT).setEnabled(true);
                        myMediator.getAction(GUIConst.ACTION_INSERT_STAMP).setEnabled(true);
                    }
                    break;
                    
                case P_TEXT:
                    // PPane��Focus������e�L�X�g�I����������
                    myMediator.getAction(GUIConst.ACTION_CUT).setEnabled(myPane.isEditable());
                    myMediator.getAction(GUIConst.ACTION_COPY).setEnabled(true);
                    pasteOk = (myPane.isEditable() && canPaste()) ? true : false;
                    myMediator.getAction(GUIConst.ACTION_PASTE).setEnabled(pasteOk);
                    break;
            }
        }
    }
    
    /**
     * KartePane�̃R���e�L�X�g���j���[�N���X�B
     */
    class ContextListener extends MouseAdapter {
        
        private KartePane context;
        private ChartMediator myMediator;
        
        public ContextListener(KartePane kartePane, ChartMediator mediator) {
            context = kartePane;
            myMediator = mediator;
        }
        
        private JPopupMenu createMenus() {
            final JPopupMenu contextMenu = new JPopupMenu();
            // cut, copy, paste ���j���[��ǉ�����
            contextMenu.add(myMediator.getAction(GUIConst.ACTION_CUT));
            contextMenu.add(myMediator.getAction(GUIConst.ACTION_COPY));
            contextMenu.add(myMediator.getAction(GUIConst.ACTION_PASTE));
            // �e�L�X�g�J���[���j���[��ǉ�����
            if (context.getTextPane().isEditable()) {
                ColorChooserComp ccl = new ColorChooserComp();
                ccl.addPropertyChangeListener(ColorChooserComp.SELECTED_COLOR, new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent e) {
                        Color selected = (Color) e.getNewValue();
                        Action action = new StyledEditorKit.ForegroundAction("selected", selected);
                        action.actionPerformed(new ActionEvent(context.getTextPane(), ActionEvent.ACTION_PERFORMED, "foreground"));
                        contextMenu.setVisible(false);
                    }
                });
                JLabel l = new JLabel("  �J���[:");
                JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
                p.add(l);
                p.add(ccl);
                contextMenu.add(p);
            } else {
                contextMenu.addSeparator();
            }
            
            // PPane �̏ꍇ��StampMenu��ǉ�����
            if (getMyRole().equals(IInfoModel.ROLE_P)) {
                //contextMenu.addSeparator();
                myMediator.addStampMenu(contextMenu, context);
            } else {
                // TextMenu��ǉ�����
                myMediator.addTextMenu(contextMenu);
            }
            
            return contextMenu;
        }
        
        public void mousePressed(MouseEvent e) {
            mabeShowPopup(e);
        }
        
        public void mouseReleased(MouseEvent e) {
            mabeShowPopup(e);
        }
        
        public void mabeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                JPopupMenu contextMenu = createMenus();
                contextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
    
    /**hanagui+
     *
     * �ҏW�\���ǂ����̑�����TextPane�ɐݒ肷��B
     * @param editable �ҏW�\��Pane�̎�true
     */
    protected void setEditableProp(boolean editable) {
        if (editable) {
            getDocument().addUndoableEditListener(getMediator());
            if (myRole.equals(IInfoModel.ROLE_SOA)) {
                SOACodeHelper helper = new SOACodeHelper(this, getMediator());
            } else {
                PCodeHelper helper = new PCodeHelper(this, getMediator());
            }
        } else {
            setBackgroundUneditable();
        }
        getTextPane().setEditable(editable);
    }
    
    /**
     * �w�i��ҏW�s�J���[�ɐݒ肷��B
     */
    protected void setBackgroundUneditable() {
        getTextPane().setBackground(getUneditableColor());
        getTextPane().setOpaque(true);
    }
    
    /**
     * ���[���ƃp�[�g�i��ݒ肷��B
     * @param role ���̃y�C���̃��[��
     * @param partner �p�[�g�i
     */
    protected void setRole(String role, KartePane partner) {
        setMyRole(role);
        setMyPartner(partner);
    }
    
    /**
     * Dirty���ǂ�����Ԃ��B
     * @return dirty �̎� true
     */
    protected boolean isDirty() {
        return getTextPane().isEditable() ? dirty : false;
    }
    
    /**
     * �ۑ����ɂ���h�L�������g�̃^�C�g����Document Object���璊�o����B
     * @return �擪����w�肳�ꂽ������؂�o����������
     */
    protected String getTitle() {
        try {
            KarteStyledDocument doc = getDocument();
            int len = doc.getLength();
            int freeTop = 0; // doc.getFreeTop();
            int freeLen = len - freeTop;
            freeLen = freeLen < TITLE_LENGTH ? freeLen : TITLE_LENGTH;
            return getTextPane().getText(freeTop, freeLen).trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Document�̒i���X�^�C����ݒ肷��B
     * @param str �X�^�C��
     */
    public void setLogicalStyle(String str) {
        getDocument().setLogicalStyle(str);
    }
    
    /**
     * Document�̒i���_���X�^�C�����N���A����B
     */
    public void clearLogicalStyle() {
        getDocument().clearLogicalStyle();
    }
    
    /**
     * �i�����\������B
     */
    public void makeParagraph() {
        getDocument().makeParagraph();
    }
    
    /**
     * Document�ɕ������}������B
     * @param str �}�����镶����
     * @param attr ����
     */
    public void insertFreeString(String s, AttributeSet a) {
        getDocument().insertFreeString(s,a);
    }
    
    /**
     * ���̃y�C���� Stamp ��}������B
     */
    public void stamp(final ModuleModel stamp) {
        if (stamp != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    StampHolder h = new StampHolder(KartePane.this, stamp);
                    h.setTransferHandler(stampHolderTransferHandler);
                    KarteStyledDocument doc = getDocument();
                    doc.stamp(h);
                }
            });
        }
    }
    
    /**
     * ���̃y�C���� Stamp ��}������B
     */
    public void flowStamp(ModuleModel stamp) {
        if (stamp != null) {
            StampHolder h = new StampHolder(this, stamp);
            h.setTransferHandler(stampHolderTransferHandler);
            KarteStyledDocument doc = getDocument();
            doc.flowStamp(h);
        }
    }
    
    /**
     * ���̃y�C���ɃV�F�[�}��}������B
     * @param schema �V�F�[�}
     */
    public void stampSchema(final SchemaModel schema) {
        if (schema != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    
                    SchemaHolder h = new SchemaHolder(KartePane.this, schema);
                    h.setTransferHandler(schemaHolderTransferHandler);
                    KarteStyledDocument doc = getDocument();
                    doc.stampSchema(h);
                }
            });
        }
    }
    
    /**
     * ���̃y�C���ɃV�F�[�}��}������B
     * @param schema  �V�F�[�}
     */
    public void flowSchema(SchemaModel schema) {
        if (schema != null) {
            SchemaHolder h = new SchemaHolder(this, schema);
            h.setTransferHandler(schemaHolderTransferHandler);
            KarteStyledDocument doc = (KarteStyledDocument) getTextPane().getDocument();
            doc.flowSchema(h);
        }
    }
    
    /**
     * ���̃y�C���� TextStamp ��}������B
     */
    public void insertTextStamp(final String s) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                KarteStyledDocument doc = getDocument();
                doc.insertTextStamp(s);
            }
        });
    }
    
    /**
     * StampInfo��Drop���ꂽ���A���̃f�[�^���y�C���ɑ}������B
     * @param stampInfo �h���b�v���ꂽ�X�^���v���
     */
    public void stampInfoDropped(ModuleInfoBean stampInfo) {
        
        //
        // Drop ���ꂽ StampInfo �̑����ɉ����ď�����U������
        //
        String entity = stampInfo.getEntity();
        //System.out.println(entity);
        
        String role = stampInfo.getStampRole();
        //System.out.println(role);
        
        
        //
        // �a���̏ꍇ�͂Q���J���e�y�C���ɂ͓W�J���Ȃ�
        //
        if (entity.equals(IInfoModel.ENTITY_DIAGNOSIS)) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        
        //
        // Text �X�^���v��}������
        //
        if (entity.equals(IInfoModel.ENTITY_TEXT)) {
            applyTextStamp(stampInfo);
            return;
        }
        
        //
        // ORCA ���̓Z�b�g�̏ꍇ
        //
        if (role.equals(IInfoModel.ROLE_ORCA_SET)) {
            //System.out.println("orca set dropped");
            applyOrcaSet(stampInfo);
            return;
        }
        
        //
        // �f�[�^�x�[�X�ɕۑ�����Ă���X�^���v��}������
        //
        if (stampInfo.isSerialized()) {
            //System.out.println("apply serialized stamp");
            applySerializedStamp(stampInfo);
            return;
        }
        
        //
        // Stamp �G�f�B�^���N������
        //
        ModuleModel stamp = new ModuleModel();
        stamp.setModuleInfo(stampInfo);
        
        StampEditorDialog stampEditor = new StampEditorDialog(entity, stamp);
        stampEditor.addPropertyChangeListener(StampEditorDialog.VALUE_PROP, this);
        stampEditor.start();
    }
    
    /**
     * StampInfo��Drop���ꂽ���A���̃f�[�^���y�C���ɑ}������B
     * @param addList �X�^���v���̃��X�g
     */
    public void stampInfoDropped(final ArrayList<ModuleInfoBean> addList) {
        
        
        Runnable serializedRunner = new Runnable() {
            
            public void run() {
                
                startAnimation();
                
                StampDelegater sdl = new StampDelegater();
                final List<StampModel> list = sdl.getStamp(addList);
                
                stopAnimation();
                
                if (list != null) {
                    //for (int i = list.size() -1; i > -1; i--) {
                    for (int i = 0; i < list.size(); i++) {
                        ModuleInfoBean stampInfo = addList.get(i);
                        StampModel theModel = list.get(i);
                        IInfoModel model = (IInfoModel) BeanUtils.xmlDecode(theModel.getStampBytes());
                        if (model != null) {
                            ModuleModel stamp = new ModuleModel();
                            stamp.setModel(model);
                            stamp.setModuleInfo(stampInfo);
                            stamp(stamp);
                        }
                    }
                }
            }
        };
        Thread t = new Thread(serializedRunner);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }
    
    /**
     * TextStampInfo �� Drop ���ꂽ���̏������s�Ȃ��B
     */
    public void textStampInfoDropped(final ArrayList<ModuleInfoBean> addList) {
        
        Runnable serializedRunner = new Runnable() {
            
            public void run() {
                
                startAnimation();
                
                StampDelegater sdl = new StampDelegater();
                final List<StampModel> list = sdl.getStamp(addList);
                
                stopAnimation();
                
                if (list != null) {
                    //for (int i = list.size() -1; i > -1; i--) {
                    for (int i = 0; i < list.size(); i++) {
                        StampModel theModel = list.get(i);
                        IInfoModel model = (IInfoModel) BeanUtils.xmlDecode(theModel.getStampBytes());
                        if (model != null) {
                            insertTextStamp(model.toString() + "\n");
                        }
                    }
                }
            }
        };
        Thread t = new Thread(serializedRunner);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }
    
    /**
     * TextStamp �����̃y�C���ɑ}������B
     */
    private void applyTextStamp(final ModuleInfoBean stampInfo) {
        
        Runnable textRunner = new Runnable() {
            
            public void run() {
                
                startAnimation();
                
                String rdn = stampInfo.getStampId();
                IInfoModel model = null;
                
                StampDelegater sdl = new StampDelegater();
                StampModel getStamp = sdl.getStamp(rdn);
                
                if (getStamp != null) {
                    
                    try {
                        // String beanXml = getStamp.getStampXml();
                        // byte[] bytes = beanXml.getBytes("UTF-8");
                        byte[] bytes = getStamp.getStampBytes();
                        // XMLDecode
                        XMLDecoder d = new XMLDecoder(new BufferedInputStream(
                                new ByteArrayInputStream(bytes)));
                        
                        model = (IInfoModel) d.readObject();
                        
                    } catch (Exception e) {
                    }
                }
                
                stopAnimation();
                
                if (model != null) {
                    insertTextStamp(model.toString());
                }
            }
        };
        Thread t = new Thread(textRunner);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }
    
    /**
     * �i��������Ă���X�^���v���擾���Ă��̃y�C���ɓW�J����B
     */
    private void applySerializedStamp(final ModuleInfoBean stampInfo) {
        
        Runnable serializedRunner = new Runnable() {
            
            public void run() {
                
                startAnimation();
                
                String rdn = stampInfo.getStampId();
                IInfoModel model = null;
                
                StampDelegater sdl = new StampDelegater();
                StampModel getStamp = sdl.getStamp(rdn);
                
                if (getStamp != null) {
                    model = (IInfoModel) BeanUtils.xmlDecode(getStamp.getStampBytes());
                }
                
                stopAnimation();
                
                final ModuleModel stamp = new ModuleModel();
                stamp.setModel(model);
                stamp.setModuleInfo(stampInfo);
                stamp(stamp);
            }
        };
        
        Thread t = new Thread(serializedRunner);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }
    
    /**
     * ORCA �̓��̓Z�b�g���擾���Ă��̃y�C���ɓW�J����B
     */
    private void applyOrcaSet(final ModuleInfoBean stampInfo) {
        
        Runnable serializedRunner = new Runnable() {
            
            public void run() {
                
                startAnimation();
                
                String id = stampInfo.getStampId();
                
                SqlOrcaSetDao sdl = new SqlOrcaSetDao();
                List<ModuleModel> models = sdl.getStamp(stampInfo);
                
                stopAnimation();
                
                if (models != null) {
                    for (ModuleModel stamp : models) {
                        stamp(stamp);
                    }
                }
            }
        };
        
        Thread t = new Thread(serializedRunner);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start(); 
    }
    
    
    /**
     * ProgressBar�A�j���[�V�������J�n����B
     */
    private void startAnimation() {
        Runnable awt = new Runnable() {
            public void run() {
                IChart context = (IChart) getParent().getContext();
                IStatusPanel sp = context.getStatusPanel();
                sp.start("�X�^���v���擾���Ă��܂�...");
            }
        };
        SwingUtilities.invokeLater(awt);
    }
    
    /**
     * ProgressBar���X�g�b�v����B
     */
    private void stopAnimation() {
        Runnable awt = new Runnable() {
            public void run() {
                IChart context = (IChart) getParent().getContext();
                IStatusPanel sp = context.getStatusPanel();
                sp.stop("");
            }
        };
        SwingUtilities.invokeLater(awt);
    }
    
    /**
     * Schema �� DnD ���ꂽ�ꍇ�A�V�F�[�}�G�f�B�^���J���ĕҏW����B
     */
    public void myInsertImage(Image trImg) {
        
        try {
            ImageIcon org = new ImageIcon(trImg);
            
            // Size �𔻒肷��
            int maxImageWidth = ClientContext.getInt("image.max.width");
            int maxImageHeight = ClientContext.getInt("image.max.height");
            Dimension maxSImageSize = new Dimension(maxImageWidth, maxImageHeight);
            final Preferences pref = Preferences.userNodeForPackage(this.getClass());
            if (org.getIconWidth() > maxImageWidth || org.getIconHeight() > maxImageHeight) {
                if (pref.getBoolean("showImageSizeMessage", true)) {
                    String title = ClientContext.getFrameTitle("�摜�T�C�Y�ɂ���");
                    JLabel msg1 = new JLabel("�J���e�ɑ}������摜�́A�ő�� " + maxImageWidth + " x " + maxImageHeight + " pixcel �ɐ������Ă��܂��B");
                    JLabel msg2 = new JLabel("���̂��ߕۑ����ɂ͉摜���k�����܂��B");
                    final JCheckBox cb = new JCheckBox("���ケ�̃��b�Z�[�W��\�����Ȃ�");
                    cb.setFont(new Font("Dialog", Font.PLAIN, 10));
                    cb.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            pref.putBoolean("showImageSizeMessage", !cb.isSelected());
                        }
                    });
                    JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
                    p1.add(msg1);
                    JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
                    p2.add(msg2);
                    JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
                    p3.add(cb);
                    JPanel box = new JPanel();
                    box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
                    box.add(p1);
                    box.add(p2);
                    box.add(p3);
                    box.setBorder(BorderFactory.createEmptyBorder(0, 0, 11, 11));
                    Window parent = SwingUtilities.getWindowAncestor(getTextPane());
                    
                    JOptionPane.showMessageDialog(parent,
                            new Object[]{box},
                            ClientContext.getFrameTitle(getTitle()),
                            JOptionPane.INFORMATION_MESSAGE,
                            ClientContext.getImageIcon("about_32.gif"));
                    
                    
                }
            }
            
            SchemaModel schema = new SchemaModel();
            schema.setIcon(org);
            
            // IInfoModel �Ƃ��� ExtRef ��ێ����Ă���
            ExtRefModel ref = new ExtRefModel();
            ref.setContentType("image/jpeg");
            ref.setTitle("Schema Image");
            schema.setExtRef(ref);
            
            stampId++;
            String fileName = getDocId() + "-" + stampId + ".jpg";
            schema.setFileName(fileName);
            ref.setHref(fileName);
            
            final SchemaEditorDialog dlg = new SchemaEditorDialog((Frame) null, true, schema, true);
            dlg.addPropertyChangeListener(KartePane.this);
            Runnable awt = new Runnable() {
                public void run() {
                    dlg.run();
                }
            };
            EventQueue.invokeLater(awt);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * StampEditor �̕ҏW���I������Ƃ����֒ʒm�����B
     * �ʒm���ꂽ�X�^���v���y�C���ɑ}������B
     */
    public void propertyChange(PropertyChangeEvent e) {
        
        String prop = e.getPropertyName();
        
        if (prop.equals("imageProp")) {
            
            SchemaModel schema = (SchemaModel) e.getNewValue();
            
            if (schema != null) {
                // �ҏW���ꂽ�V�F�[�}�����̃y�C���ɑ}������
                stampSchema(schema);
            }
            
        } else if (prop.equals(StampEditorDialog.VALUE_PROP)) {
            
            Object o = e.getNewValue();
            
            if (o != null) {
                // �ҏW���ꂽ Stamp �����̃y�C���ɑ}������
                ModuleModel stamp = (ModuleModel) o;
                stamp(stamp);
            }
        }
    }
    
    /**
     * ���j���[����̂��߁A�y�[�X�g�\���ǂ�����Ԃ��B
     * @return �y�[�X�g�\�Ȏ� true
     */
    protected boolean canPaste() {
        
        boolean ret = false;
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (t == null) {
            return false;
        }
        
        if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return true;
        }
        
        if (getMyRole().equals(IInfoModel.ROLE_P)) {
            if (t.isDataFlavorSupported(OrderListTransferable.orderListFlavor)) {
                ret = true;
            }
        } else {
            if (t.isDataFlavorSupported(StampListTransferable.stampListFlavor)
            || t.isDataFlavorSupported(SchemaListTransferable.schemaListFlavor)) {
                ret = true;
            }
        }
        return ret;
    }
    
    /**
     * ���̃y�C������X�^���v���폜����B
     * @param sh �폜����X�^���v�̃z���_
     */
    public void removeStamp(StampHolder sh) {
        getDocument().removeStamp(sh.getStartPos(), 2);
    }
    
    /**
     * ���̃y�C������X�^���v���폜����B
     * @param sh �폜����X�^���v�̃z���_���X�g
     */
    public void removeStamp(StampHolder[] sh) {
        if (sh != null && sh.length > 0) {
            for (int i = 0; i < sh.length; i++) {
                removeStamp(sh[i]);
            }
        }
    }
    
    /**
     * ���̃y�C������V�F�[�}���폜����B
     * @param sh �폜����V�F�[�}�̃z���_
     */
    public void removeSchema(SchemaHolder sh) {
        getDocument().removeStamp(sh.getStartPos(), 2);
    }
    
    /**
     * ���̃y�C������V�F�[�}���폜����B
     * @param sh �폜����V�F�[�}�̃z���_���X�g
     */
    public void removeSchema(SchemaHolder[] sh) {
        if (sh != null && sh.length > 0) {
            for (int i = 0; i < sh.length; i++) {
                removeSchema(sh[i]);
            }
        }
    }
}