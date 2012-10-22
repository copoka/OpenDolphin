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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import open.dolphin.client.AutoRomanListener;
import open.dolphin.client.CalendarCardPanel;
import open.dolphin.client.ClientContext;
import open.dolphin.client.IStampModelEditor;
import open.dolphin.client.OddEvenRowRenderer;
import open.dolphin.infomodel.DiagnosisCategoryModel;
import open.dolphin.infomodel.DiagnosisOutcomeModel;
import open.dolphin.infomodel.RegisteredDiagnosisModel;
import open.dolphin.infomodel.SimpleDate;
import open.dolphin.table.IMECellEditor;
import open.dolphin.table.ObjectTableModel;
import open.dolphin.util.MMLDate;

/**
 * ���a���ҏW�e�[�u���N���X�B
 *
 * @author Kazushi Minagawa
 */
public class DiagnosisTablePanel extends JPanel implements PropertyChangeListener {
    
    /** ���a���̏C����R�[�h */
    private static final String MODIFIER_CODE = "ZZZ";
    
    /** ���a������͎��ɂ���R�[�h */
    private static final String HAND_CODE = "0000999";
    
    //
    // Diagnosis table �̃p�����[�^
    //
    private static final int NAME_COL            = 0;
    private static final int CATEGORY_COL        = 1;
    private static final int OUTCOME_COL         = 2;
    private static final int START_DATE_COL      = 3;
    private static final int END_DATE_COL        = 4;
    private static final int[] DIAGNOSIS_TABLE_COLUMN_WIDTHS = {
        300, 90, 90, 90, 90
    };
    private static final int START_NUM_ROWS = 10;
    private static final int ROW_HEIGHT = 18;
    private static final String REMOVE_BUTTON_IMAGE = "del_16.gif";
    private static final String CLEAR_BUTTON_IMAGE  = "remov_16.gif";
    private static final String INFO_BUTTON_IMAGE   = "about_16.gif";
    private static final int TABLE_WIDTH = 890;
    private static final int TABLE_HEIGHT = 90;
    
    private static final String TOOLTIP_REMOVE = "�I���������a�����폜���܂�";
    private static final String TOOLTIP_CLEAR  = "�e�[�u�����N���A���܂�";
    private static final String TOOLTIP_TABLE  = "Drag & Drop �ŏ��Ԃ����ւ��邱�Ƃ��ł��܂�";
    private static final String TOOLTIP_COMBINE  = "�e�[�u���̍s��A�����ďC����t���̏��a���ɂ��܂�";
    
    /** �C����t�����a�� �\�����x�� */
    private static final String LABEL_COMBINED_DIAGNOSIS = "�A���������a��:";
    
    /** �}�X�^�����̑I���A�C�e���v���p�e�B */
    private static final String SELECTED_ITEM_PROP = "selectedItemProp";
    
    /** �����a���\���t�B�[���h�̒��� */
    private static final int COMBINED_FIELD_LENGTH = 20;
    
    /** Table model */
    private ObjectTableModel tableModel;
    
    /** ���a���ҏW�e�[�u�� */
    private JTable table;
    
    /** �J�e�S�� ComboBox*/
    private JComboBox categoryCombo;
    
    /** �]�AComboBox */
    private JComboBox outcomeCombo;
    
    /** �f�t�H���g�̃J�e�S�� */
    private DiagnosisCategoryModel defaultCategory;
    
    /** �폜�{�^�� */
    private JButton removeButton;
    
    /** �N���A�{�^�� */
    private JButton clearButton;
    
    /** �����a����\������t�B�[���h */
    private JTextField combinedDiagnosis;
    
    /** State ��\�����郉�x�� */
    private JLabel stateLabel;
    
    /** Stamp Editor */
    private IStampModelEditor context;
    
    /** �J�����_�[�J���[�e�[�u�� */
    private HashMap<String, Color> cTable;
    
    /** ��ԃ}�V�� */
    private DiagnosisStateMgr curState;
    
    
    /**
     * DiagnosisTablePanel�𐶐�����B
     */
    public DiagnosisTablePanel(IStampModelEditor context) {
        
        super(new BorderLayout());
        
        setContext(context);
        
        // Popup �J�����_�[�p�̃J���[�e�[�u���𐶐�����
        cTable = new HashMap<String, Color>(10, 0.75f);
        cTable.put("TODAY", Color.PINK);
        cTable.put("BIRTHDAY", Color.CYAN);
        cTable.put("PVT", Color.YELLOW);
        cTable.put("DOC_HISTORY", Color.YELLOW);
        
        // �e�[�u���̃J���������擾����
        String[] diganosisColumns = ClientContext.getStringArray("diagnosis.columnNames");
        
        // �e�[�u�����f���𐶐�����
        tableModel = new ObjectTableModel(diganosisColumns, START_NUM_ROWS) {
            
            // �a���J�������C����̕ҏW���\
            public boolean isCellEditable(int row, int col) {
                return true;
            }
            
            public Object getValueAt(int row, int col) {
                
                RegisteredDiagnosisModel model = (RegisteredDiagnosisModel) getObject(row);
                
                if (model == null) {
                    return null;
                }
                
                String ret = null;
                
                switch (col) {
                    
                    case NAME_COL:
                        ret = model.getDiagnosis();
                        break;
                        
                    case CATEGORY_COL:
                        if (model.getDiagnosisCategoryModel() != null) {
                            ret = model.getDiagnosisCategoryModel().getDiagnosisCategoryDesc();
                        }
                        break;
                        
                    case OUTCOME_COL:
                        if (model.getDiagnosisOutcomeModel() != null) {
                            ret = model.getDiagnosisOutcomeModel().getOutcomeDesc();
                        }
                        break;
                        
                    case START_DATE_COL:
                        ret = model.getStartDate();
                        break;
                        
                    case END_DATE_COL:
                        ret = model.getEndDate();
                        break;
                }
                
                return ret;
            }
            
            public void setValueAt(Object o, int row, int col) {
                
                if (o == null) {
                    return;
                }
                
                String value = (String) o;
                value = value.trim();
                
                if (value.equals("")){
                    return;
                }
                
                RegisteredDiagnosisModel model = (RegisteredDiagnosisModel) getObject(row);
                
                switch (col) {
                    
                    case NAME_COL:
                        //
                        // �a��������͂��ꂽ�ꍇ�́A�R�[�h�� 0000999 ��ݒ肷��
                        //
                        if (model != null) {
                            model.setDiagnosis(value);
                            model.setDiagnosisCode(HAND_CODE);
                            fireTableCellUpdated(row, col);
                            
                        } else {
                            model = new RegisteredDiagnosisModel();
                            model.setDiagnosis(value);
                            model.setDiagnosisCode(HAND_CODE);
                            // ���ޖ� ���t��������
                            //
                            // ��a���̃Z�b�g�͂��Ȃ�
                            //model.setDiagnosisCategoryModel(defaultCategory);
                            GregorianCalendar gc = new GregorianCalendar();
                            String today = MMLDate.getDate(gc);
                            model.setStartDate(today);
                            addRow(model);
                            curState.processEvent(DiagnosisStateMgr.Event.ADDED);
                        }
                        break;
                        
                    case CATEGORY_COL:
                        if (model != null) {
                            defaultCategory = (DiagnosisCategoryModel) o;
                            model.setDiagnosisCategoryModel(defaultCategory);
                            fireTableCellUpdated(row, col);
                        } else {
                            o = null;
                        }
                        break;
                        
                    case OUTCOME_COL:
                        if (model != null) {
                            model.setDiagnosisOutcomeModel((DiagnosisOutcomeModel) o);
                            fireTableCellUpdated(row, col);
                        } else {
                            o = null;
                        }
                        break;
                        
                    case START_DATE_COL:
                        if (model != null) {
                            model.setStartDate((String)o);
                            fireTableCellUpdated(row, col);
                        } else {
                            o = null;
                        }
                        break;
                        
                    case END_DATE_COL:
                        if (model != null) {
                            model.setEndDate((String)o);
                            fireTableCellUpdated(row, col);
                        } else {
                            o = null;
                        }
                        break;
                }
            }
        };
        
        //
        // Table �𐶐��� transferHandler �𐶐�����
        //
        table = new JTable(tableModel);;
        table.setToolTipText(TOOLTIP_TABLE);
        table.setTransferHandler(new RegisteredDiagnosisTransferHandler(DiagnosisTablePanel.this)); // TransferHandler
        table.addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                int ctrlMask = InputEvent.CTRL_DOWN_MASK;
                int action = ((e.getModifiersEx() & ctrlMask) == ctrlMask)
                            ? TransferHandler.COPY
                            : TransferHandler.MOVE;
                JComponent c = (JComponent) e.getSource();
                TransferHandler handler = c.getTransferHandler();
                handler.exportAsDrag(c, e, action);
            }
            
            public void mouseMoved(MouseEvent e) {
            }
        });
        
        table.setRowHeight(ROW_HEIGHT);
        table.setDefaultRenderer(Object.class, new OddEvenRowRenderer());
        table.setPreferredSize(new Dimension(TABLE_WIDTH,TABLE_HEIGHT));
        //table.setRowMargin(5);
        //table.setIntercellSpacing(new Dimension(-5,-5));
        table.setSurrendersFocusOnKeystroke(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        ListSelectionModel m = table.getSelectionModel();
        m.addListSelectionListener(new ListSelectionListener() {
            
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    //notifySelectedRow();
                    curState.processEvent(DiagnosisStateMgr.Event.SELECTED);
                }
            }
        });
        
        // CellEditor ��ݒ肷��
        // NAME_COL clickCountToStart=2, IME=ON
        TableColumn column = table.getColumnModel().getColumn(NAME_COL);
        column.setCellEditor(new IMECellEditor(new JTextField(), 2, true));
        
        // Category comboBox ���͂�ݒ肷��
        String[] values = ClientContext.getStringArray("diagnosis.category");
        String[] descs = ClientContext.getStringArray("diagnosis.categoryDesc");
        String[] codeSys = ClientContext.getStringArray("diagnosis.categoryCodeSys");
        DiagnosisCategoryModel[] categoryList = new DiagnosisCategoryModel[values.length + 1];
        DiagnosisCategoryModel dcm = new DiagnosisCategoryModel();
        dcm.setDiagnosisCategory("");
        dcm.setDiagnosisCategoryDesc("");
        categoryList[0] = null;
        for (int i = 0; i < values.length; i++) {
            dcm = new DiagnosisCategoryModel();
            dcm.setDiagnosisCategory(values[i]);
            dcm.setDiagnosisCategoryDesc(descs[i]);
            dcm.setDiagnosisCategoryCodeSys(codeSys[i]);
            categoryList[i+1] = dcm;
        }
        categoryCombo = new JComboBox(categoryList);
        column = table.getColumnModel().getColumn(CATEGORY_COL);
        column.setCellEditor(new DefaultCellEditor(categoryCombo));
        defaultCategory = categoryList[1];
        
        // Outcome comboBox ���͂�ݒ肷��
        String[] ovalues = ClientContext.getStringArray("diagnosis.outcome");
        String[] odescs = ClientContext.getStringArray("diagnosis.outcomeDesc");
        String ocodeSys = ClientContext.getString("diagnosis.outcomeCodeSys");
        DiagnosisOutcomeModel[] outcomeList = new DiagnosisOutcomeModel[ovalues.length + 1];
        DiagnosisOutcomeModel dom = new DiagnosisOutcomeModel();
        dom.setOutcome("");
        dom.setOutcomeDesc("");
        //outcomeList[0] = dom;
        //
        // ��a���͎g�p���Ȃ��炵��
        //
        outcomeList[0] = null;
        for (int i = 0; i < ovalues.length; i++) {
            dom = new DiagnosisOutcomeModel();
            dom.setOutcome(ovalues[i]);
            dom.setOutcomeDesc(odescs[i]);
            dom.setOutcomeCodeSys(ocodeSys);
            outcomeList[i+1] = dom;
        }
        outcomeCombo = new JComboBox(outcomeList);
        column = table.getColumnModel().getColumn(OUTCOME_COL);
        column.setCellEditor(new DefaultCellEditor(outcomeCombo));
        
        // Start Date && EndDate Col
        column = table.getColumnModel().getColumn(START_DATE_COL);
        JTextField tf = new JTextField();
        tf.addFocusListener(AutoRomanListener.getInstance());
        new PopupListener(tf);
        DefaultCellEditor de = new DefaultCellEditor(tf);
        column.setCellEditor(de);
        
        column = table.getColumnModel().getColumn(END_DATE_COL);
        tf = new JTextField();
        tf.addFocusListener(AutoRomanListener.getInstance());
        new PopupListener(tf);
        de = new DefaultCellEditor(tf);
        column.setCellEditor(de);
        
        // �񕝐ݒ�
        int len = DIAGNOSIS_TABLE_COLUMN_WIDTHS.length;
        for (int i = 0; i < len; i++) {
            column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(DIAGNOSIS_TABLE_COLUMN_WIDTHS[i]);
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        //
        // �����a���� Command button
        //
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));
        
        // �����a���t�B�[���h�𐶐�����
        btnPanel.add(new JLabel(LABEL_COMBINED_DIAGNOSIS));
        btnPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        combinedDiagnosis = new JTextField(COMBINED_FIELD_LENGTH);
        combinedDiagnosis.setEditable(false);
        combinedDiagnosis.setToolTipText(TOOLTIP_COMBINE);
        // State ��\�����郉�x��
        stateLabel = new JLabel("");
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(combinedDiagnosis);
        p.add(new JLabel(ClientContext.getImageIcon(INFO_BUTTON_IMAGE)));
        p.add(stateLabel);
        btnPanel.add(p);
        
        btnPanel.add(Box.createHorizontalGlue());
        
        // �폜�{�^���𐶐�����
        removeButton = new JButton(ClientContext.getImageIcon(REMOVE_BUTTON_IMAGE));
        removeButton.setToolTipText(TOOLTIP_REMOVE);
        removeButton.setEnabled(false);
        removeButton.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                // TableModel �Ń����W�`�F�b�N���Ă���̂ň��S
                tableModel.removeRow(row);
                reconstractDiagnosis();
                curState.processEvent(DiagnosisStateMgr.Event.DELETED);
            }
        });
        btnPanel.add(removeButton);
        
        btnPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        
        // �N���A�{�^���𐶐�����
        clearButton = new JButton(ClientContext.getImageIcon(CLEAR_BUTTON_IMAGE));
        clearButton.setToolTipText(TOOLTIP_CLEAR);
        clearButton.setEnabled(false);
        clearButton.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                tableModel.clear();
                combinedDiagnosis.setText("");
                curState.processEvent(DiagnosisStateMgr.Event.CLEARED);
            }
        });
        btnPanel.add(clearButton);
        
        btnPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        
        // �e�_�C�A���O��OK�{�^����ǉ�����
        if (getContext().getContext().getOkButton() != null) {
            btnPanel.add(getContext().getContext().getOkButton());
        }
        //btnPanel.add(getContext().getOkButton());
        
        //
        // ��ԃ}�V�����J�n����
        //
        curState = new DiagnosisStateMgr(removeButton, clearButton, stateLabel,
                                        tableModel, table, getContext());
        curState.enter();
        
        // �J�����w�b�_�[��\�����ă��C�A�E�g����
        //add(table.getTableHeader(), BorderLayout.NORTH);
        //JScrollPane scroller = new JScrollPane(table);
        //scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        //scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //add(scroller, BorderLayout.CENTER);
        add(table.getTableHeader(), BorderLayout.NORTH);
        add(table, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }
    
    /**
     * StampEditor ��Ԃ��B
     * @return ���̕ҏW�e�[�u���� StampEditor
     */
    public IStampModelEditor getContext() {
        return context;
    }
    
    /**
     * StampEditor ��ݒ肷��B
     * @param context ���̕ҏW�e�[�u���� StampEditor
     */
    public void setContext(IStampModelEditor context) {
        this.context = context;
    }
    
    /**
     * �}�X�^�����e�[�u���őI�����ꂽ�A�C�e����ҏW�e�[�u���֎�荞�ށB
     * @param e PropertyChangeEvent
     */
    public void propertyChange(PropertyChangeEvent e) {
        
        String prop = e.getPropertyName();
        
        if (prop.equals(SELECTED_ITEM_PROP)) {
            
            //
            // �ʒm���ꂽ MasterItem ���� RegisteredDiagnosisModel 
            // �𐶐����A�ҏW�e�[�u���։�����B
            //
            MasterItem item = (MasterItem) e.getNewValue();
            
            if (item != null) {
                
                RegisteredDiagnosisModel model = new RegisteredDiagnosisModel();
                model.setDiagnosis(item.getName());
                model.setDiagnosisCode(item.getCode());
                model.setDiagnosisCodeSystem(item.getMasterTableId());
                
                if (item.getCode().startsWith(MODIFIER_CODE)) {
                    
                    //
                    // �ړ���y�ѐڔ���̃P�[�X
                    //
                    
                } else {
                    
                    //
                    // ���ޖ� ���t�������͂���
                    // ��a���͎g�p���Ȃ�
                    //
                    //model.setDiagnosisCategoryModel(defaultCategory);
                    GregorianCalendar gc = new GregorianCalendar();
                    String today = MMLDate.getDate(gc);
                    model.setStartDate(today);
                }
                
                tableModel.addRow(model);
                
                reconstractDiagnosis();
                
//                //
//                // �R���|�W�b�g�a����\������
//                //
//                StringBuilder sb = new StringBuilder();
//                String value = combinedDiagnosis.getText().trim();
//                if (!value.equals("")) {
//                    sb.append(value);
//                }
//                sb.append(item.getName());
//                combinedDiagnosis.setText(sb.toString());
                
                //
                // ��ԃ}�V���փC�x���g�𑗐M����
                //
                curState.processEvent(DiagnosisStateMgr.Event.ADDED);
            }
        }
    }
    
    /**
     * �e�[�u�����X�L�������A���a���R���|�W�b�g����B
     */
    public void reconstractDiagnosis() {
        
        if (hasModifier()) {
            StringBuilder sb = new StringBuilder();
            int count = tableModel.getDataSize();
            for (int i = 0; i < count; i++) {
                RegisteredDiagnosisModel diag = (RegisteredDiagnosisModel) tableModel.getObject(i);
                sb.append(diag.getDiagnosis());
            }
            combinedDiagnosis.setText(sb.toString());
        } else {
            combinedDiagnosis.setText("");
        }
    }
    
    /**
     * �C������ӂ���ł��邩�ǂ�����Ԃ��B
     */
    private boolean hasModifier() {
        boolean hasModifier = false;
        int count = tableModel.getDataSize();
        for (int i = 0; i < count; i++) {
            RegisteredDiagnosisModel diag = (RegisteredDiagnosisModel) tableModel.getObject(i);
            if (diag.getDiagnosisCode().startsWith(MODIFIER_CODE)) {
                hasModifier = true;
                break;
            }
        }
        return hasModifier;
    }
    
    /**
     * ���a���e�[�u�����X�L�������C������̏��a�ɂ��ĕԂ��B
     */
    public Object getValue() {
        
        if (hasModifier()) {
            return getValue1();
        } else {
            return getValue2();
        }
    }
    
    
    /**
     * ���a���e�[�u�����X�L�������C������̏��a�ɂ��ĕԂ��B
     */
    private Object getValue1() {
        
        RegisteredDiagnosisModel diagnosis = null;
        
        StringBuilder name = new StringBuilder();
        StringBuilder code = new StringBuilder();
        
        // �e�[�u�����X�L��������
        int count = tableModel.getDataSize();
        for (int i = 0; i < count; i++) {
            
            RegisteredDiagnosisModel diag = (RegisteredDiagnosisModel) tableModel.getObject(i);
            String diagCode = diag.getDiagnosisCode();
            
            if (!diagCode.startsWith(MODIFIER_CODE)) {
                //
                // �C����łȂ��ꍇ�͊�{�a���ƌ��Ȃ��A�p�����[�^��ݒ肷��
                //
                diagnosis = new RegisteredDiagnosisModel();
                diagnosis.setDiagnosisCodeSystem(diag.getDiagnosisCodeSystem());
                diagnosis.setDiagnosisCategoryModel(diag.getDiagnosisCategoryModel());
                diagnosis.setDiagnosisOutcomeModel(diag.getDiagnosisOutcomeModel());
                diagnosis.setStartDate(diag.getStartDate());
                diagnosis.setEndDate(diag.getEndDate());
            
            } else {
                //
                // ZZZ ���g�������� ORCA ����
                //
                diagCode = diagCode.substring(MODIFIER_CODE.length());
            }
            
            //
            // �R�[�h�� . �ŘA������
            //
            if (code.length() > 0) {
                code.append(".");
            }
            code.append(diagCode);
            
            //
            // ���O��A������
            //
            name.append(diag.getDiagnosis());
            
        }
        
        if (diagnosis != null && name.length() > 0 && code.length() > 0) {
            
            //
            // ���O�ƃR�[�h��ݒ肷��
            //
            diagnosis.setDiagnosis(name.toString());
            diagnosis.setDiagnosisCode(code.toString());
            ArrayList ret = new ArrayList(1);
            ret.add(diagnosis);
            
            return ret;
            
        } else {
            return null;
        }
    }
    
        
    /**
     * ���a���e�[�u�����X�L�������C������̏��a�ɂ��ĕԂ��B
     */
    private Object getValue2() {
        
        return tableModel.getObjectList();
    }
    
    public void setValue(Object[] o) {
    }
        

    /**
     * Popup Calendar �N���X�B
     */
    class PopupListener extends MouseAdapter implements PropertyChangeListener {

        private JPopupMenu popup;
        private JTextField tf;

        public PopupListener(JTextField tf) {
            this.tf = tf;
            tf.addMouseListener(this);
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {

            if (e.isPopupTrigger()) {
                popup = new JPopupMenu();
                CalendarCardPanel cc = new CalendarCardPanel(cTable);
                cc.addPropertyChangeListener(CalendarCardPanel.PICKED_DATE, this);
                cc.setCalendarRange(new int[]{-12, 0});
                popup.insert(cc,0);
                popup.show(e.getComponent(),e.getX(), e.getY());
            }
        }

        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals(CalendarCardPanel.PICKED_DATE)) {
                SimpleDate sd = (SimpleDate) e.getNewValue();
                tf.setText(SimpleDate.simpleDateToMmldate(sd));
                popup.setVisible(false);
                popup = null;
            }
        }
    }
}

