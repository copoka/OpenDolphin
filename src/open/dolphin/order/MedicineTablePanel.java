/*
 * MedicineTable.java
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
import javax.swing.event.*;
import javax.swing.table.*;

import open.dolphin.client.*;
import open.dolphin.infomodel.*;
import open.dolphin.project.*;
import open.dolphin.util.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.im.InputSubset;
import flextable.*;


/**
 * Medicine Set Table.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class MedicineTablePanel extends JPanel 
implements PropertyChangeListener ,DropTargetListener, DragSourceListener, DragGestureListener {
    
    private static final int TT_NAIHUKU        = 0;
    private static final int TT_TONYO          = 1;
    private static final int TT_GAIYO          = 2;
    private static final int TT_KYUNYU         = 3;
    
    private static final String DOLPHIN_ADMIN_CODE_SYSTEM = "dolphinAdmin_2001-10-03";
    private static final String FUJITSU_RECICON     = "�x�m��";
    private static final String FUJITSU_CODE_SYSTEM = "Fujitsu SX-P V1";
    
    private static final String[] COLUMN_NAMES = {
        
       "�� ��", "���(��)��","�P��", "�p �@","����"
    };
    private static final int[] COLUMN_WIDTH = {
        220, 50, 40, 110, 40
    };
    private static String[] NUMBER_LIST = null;
    static {
        NUMBER_LIST = new String[31];
        for (int i = 0; i < 31; i++) {
            NUMBER_LIST[i] = String.valueOf(i+ 1);
        }
    }
    public static final String ITEM_COUNT_PROP  = "itemCount";
    public static final String SELECTED_ROW     = "selectedRow";
    
    private static final int ROWS               = 9;
    private static final int NAME_COLUMN        = 0;
    private static final int ONEDAY_COLUMN      = 1;
    private static final int UNIT_COLUMN        = 2;
    private static final int ADMIN_COLUMN       = 3;
    private static final int NUMBER_COLUMN      = 4;
    private static final int ADMIN_ROW          = 0;
    private static final int NUMBER_ROW         = 0;
        
    private FlexibleTable medTable;
    private JTextField stampNameField;
    private JTextField   adminMemo;
    private JRadioButton inMed;
    private JRadioButton extMed;
    private int itemCount;
    private final JComboBox numberCombo;
    
    private static final String RESOURCE_BASE       = "/open/dolphin/resources/images/";
    private static final String REMOVE_BUTTON_IMAGE = "Delete24.gif";
    private static final String CLEAR_BUTTON_IMAGE  = "New24.gif";
    private JButton removeButton;
    private JButton clearButton;
    private StampModelEditor parent;
    
    private Module savedStamp;
    
    // Claim �f�Ís�׋敪
    private String classCode;
    private String classCodeId = "Claim007";    // MML Table number
    private String classCodeName;

    // �f�Î�ʋ敪
    //private static final String subclassCode    = "2";   // item �ɂ��
    private static final String subclassCodeId  = "Claim003";
    
    // ���ʃR�[�h
    //private String numberCode;
    
    // �@���E�@�O����
    private static final String IN_MEDICINE     = "�@������";
    private static final String EXT_MEDICINE    = "�@�O����";

    // �����^�C�v
    //private int rpType;
    //private String numberDiv;   // ��
    private AdminInfo adminInfo;
    
    // DnD Support
    private DragSource dragSource;
    private DropTarget dropTarget;    
    
    /** 
     * Creates new MedicineTable
     */
    public MedicineTablePanel() {
        
        super(new BorderLayout());
        
        // ��܃Z�b�g�e�[�u���𐶐�����
        medTable = new FlexibleTable(new FlexibleTableModel(COLUMN_NAMES, ROWS)) {
            
            public boolean isCellEditable(int row, int col) {
                return ( (col == ONEDAY_COLUMN) || 
                         (col == ADMIN_COLUMN)  ||
                         (col == NUMBER_COLUMN) )  ? true : false;
            }
            
            public void setValueAt(Object o, int row, int col) {
                //if (o == null || ((String)o).trim().equals("")) {
                    //return;
                //}
                super.setValueAt(o, row, col);
                
                if (col == ONEDAY_COLUMN) {
                    checkValidModel();
                }
            }
        };
        medTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        medTable.setRowSelectionAllowed(true);
        
        //JDK 1.4
        medTable.setSurrendersFocusOnKeystroke(true);
        
        // �s���I�����ꂽ���̏�����o�^����
        ListSelectionModel m = medTable.getSelectionModel();
        m.addListSelectionListener(new ListSelectionListener() {
            
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    notifySelectedRow();
                }
            }
        });
        
        // �e�[�u���� DnD �@�\��ǉ�����
        dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(medTable, DnDConstants.ACTION_COPY_OR_MOVE, this);
        dropTarget = new DropTarget(medTable, this);
        
        // �񕝂�ݒ肷��
        TableColumn column = null;
        int len = COLUMN_NAMES.length;
        for (int i = 0; i < len; i++) {
            column = medTable.getColumnModel().getColumn(i);
            column.setPreferredWidth(COLUMN_WIDTH[i]);
        }
        
        // ���ʃJ������ DocumentListener ��ݒ肷��
        //DocumentListener dl = new DocumentListener() {
          
            //public void changedUpdate(DocumentEvent e) {
            //}
            
            //public void insertUpdate(DocumentEvent e) {
                //checkValidModel();
            //}
            
            //public void removeUpdate(DocumentEvent e) {
                //checkValidModel();
            //}
        //};
        //JFormattedTextField tf = new JFormattedTextField(new DecimalFormat("####"));
        JTextField tf = new JTextField();
        TableColumn oneDayColumn = medTable.getColumnModel().getColumn(ONEDAY_COLUMN);
        oneDayColumn.setCellEditor (new NumberCellEditor(tf));
        //tf.getDocument().addDocumentListener(dl);
        
        // �o���h���i���o�[�Z�b�g
        numberCombo = new JComboBox(NUMBER_LIST);
        TableColumn numberColumn = medTable.getColumnModel().getColumn(NUMBER_COLUMN);
        numberColumn.setCellEditor (new DefaultCellEditor(numberCombo));
        
        // �s����
        combineRows(ADMIN_ROW, ADMIN_COLUMN, ROWS);
        combineRows(NUMBER_ROW, NUMBER_COLUMN, ROWS);
        
        // StampName, AdminComment, CommandButtons
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        JLabel stampName = new JLabel("�X�^���v��");
        p.add(stampName);
        p.add(Box.createRigidArea(new Dimension(5,0)));  // DG = 12
        stampNameField = new JTextField();
        stampNameField.setOpaque(true);
        stampNameField.setBackground(new Color(251,239,128));
        
        stampNameField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent event) {
               stampNameField.getInputContext().setCharacterSubsets(new Character.Subset[] {InputSubset.KANJI});
            }
            public void focusLosted(FocusEvent event) {
               stampNameField.getInputContext().setCharacterSubsets(null);
            }
        });
        Dimension dim = new Dimension(120,21);
        stampNameField.setPreferredSize(dim);
        stampNameField.setMaximumSize(dim);
        p.add(stampNameField);
        
        p.add(Box.createRigidArea(new Dimension(5,0)));
        
        p.add(new JLabel("�p�@����"));
        p.add(Box.createRigidArea(new Dimension(5,0)));
        adminMemo = new JTextField();
        adminMemo.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent event) {
               adminMemo.getInputContext().setCharacterSubsets(new Character.Subset[] {InputSubset.KANJI});
            }
            public void focusLosted(FocusEvent event) {
               adminMemo.getInputContext().setCharacterSubsets(null);
            }
        });
        dim = new Dimension(80,21);
        adminMemo.setPreferredSize(dim);
        adminMemo.setMaximumSize(dim);
        p.add(adminMemo);
        
        p.add(Box.createRigidArea(new Dimension(5,0)));
        
        // �@���E�@�O����
        inMed = new JRadioButton("�@��");
        extMed = new JRadioButton("�@�O");
        ButtonGroup g = new ButtonGroup();
        g.add(inMed);
        g.add(extMed);
        
        boolean bOut = ClientContext.getPreferences().getBoolean("medicineOrder.outMedicine", true);
        if (bOut) {
            extMed.setSelected(true);
            
        } else {
            inMed.setSelected(true);
        }
        
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean b = extMed.isSelected();
                ClientContext.getPreferences().putBoolean("medicineOrder.outMedicine", b);
            }
        };
        
        inMed.addActionListener(al);
        extMed.addActionListener(al);
        
        p.add(inMed);
        p.add(extMed);
        //p.add(Box.createRigidArea(new Dimension(5,0)));
        
        p.add(Box.createHorizontalGlue());
        
        // Remove button
        removeButton = new JButton(createImageIcon(REMOVE_BUTTON_IMAGE));
        removeButton.setEnabled(false);
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                removeSelectedItem();
            }
        });
        p.add(removeButton);
        
        p.add(Box.createRigidArea(new Dimension(5, 0)));

        // Clear button
        clearButton = new JButton(createImageIcon(CLEAR_BUTTON_IMAGE));
        clearButton.setEnabled(false);
        clearButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                clear();
            }
        });
        p.add(clearButton);     
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
               
        this.add(medTable.getTableHeader(), BorderLayout.NORTH);
        this.add(medTable, BorderLayout.CENTER);
        this.add(p, BorderLayout.SOUTH);
        
        // ���ʃf�t�H���g
        setDefaultBundleNumber();
    }
    
    private void setDefaultBundleNumber() {
        FlexibleTableModel model = (FlexibleTableModel)medTable.getModel();
        model.setValueAt("3", NUMBER_ROW, NUMBER_COLUMN); 
    }
    
    public void setParent(StampModelEditor parent) {
        this.parent = parent;
    }
    
    public void propertyChange(PropertyChangeEvent e) {
    
        String prop = e.getPropertyName();
        
        if (prop.equals("selectedItemProp")) {
            
            if (itemCount < ROWS) {
                
                MasterItem mItem = (MasterItem)e.getNewValue();
                //
                medTable.setValueAt(mItem, itemCount, NAME_COLUMN);
                if (mItem.unit != null) {
                    medTable.setValueAt(mItem.unit, itemCount, UNIT_COLUMN);
                }
                
                FlexibleTableModel model = (FlexibleTableModel)medTable.getModel();
                model.fireTableRowsUpdated(itemCount, itemCount);
                itemCount++;
                notifyCount();
                notifySelectedRow();
            }
        }
        else if (prop.equals(AdminPanel.ADMIN_PROP)) {
         
            AdminInfo info = (AdminInfo)e.getNewValue();
            switch(info.eventType) {
                                   
                case AdminInfo.TT_ADMIN:
                    //System.out.println(info.admin1);
                    //System.out.println(info.admin2);
                    //System.out.println(info.admin);
                    //System.out.println(info.classCode);
                    //System.out.println(info.numberCode);
                    //info.showState = 0;
                    // Save admin
                    adminInfo = info;
                    medTable.setValueAt(info.admin, ADMIN_ROW, ADMIN_COLUMN);
                    FlexibleTableModel model = (FlexibleTableModel)medTable.getModel();
                    model.fireTableRowsUpdated(0, ROWS);
                    checkValidModel();
                    break;
                    
                case AdminInfo.TT_MEMO:
                    adminMemo.setText(info.adminMemo);
                    break;
            }  
        }
    }
    
    private void notifyCount() {
        boolean b = (itemCount > 0) ? true : false;
        clearButton.setEnabled(b);
        checkValidModel();
    }
    
    private void notifySelectedRow() {
        int index = medTable.getSelectedRow();
        boolean b = isValidRow(index);
        removeButton.setEnabled(b);
    }    
    
    private void clear() {
     
        for (int i = 0; i < ROWS; i++) {
            for ( int j = 0; j < ADMIN_COLUMN; j++) {
                medTable.setValueAt(null, i, j);
            }
        }
        medTable.setValueAt(null, ADMIN_ROW, ADMIN_COLUMN);        
        FlexibleTableModel model = (FlexibleTableModel)medTable.getModel();
        model.fireTableRowsUpdated(0, ROWS);
        itemCount= 0;
        notifyCount();
        notifySelectedRow();
    }
    
    private void removeSelectedItem() {
     
        int index = medTable.getSelectedRow();
        if (index < 0) {
            notifySelectedRow();
            return;
        }
        Object o = medTable.getValueAt(index, 0);
        if ( o == null ) {
            notifySelectedRow();
            return;
        }
        
        for (int i = index; i < itemCount -1; i++) {
            for ( int j = 0; j < ADMIN_COLUMN; j++) {
                o = medTable.getValueAt(i + 1, j);
                medTable.setValueAt(o, i, j);    
            }
        }
        for ( int j = 0; j < ADMIN_COLUMN; j++) {
            medTable.setValueAt(null, itemCount - 1, j);    
        }
        itemCount--;
        notifyCount();
        notifySelectedRow();
    }
        
    private void combineRows(int row, int col, int nRows) {
        
        FlexibleTableModel model = (FlexibleTableModel)medTable.getModel();
        CellAttribute cellAtt = (CellAttribute)model.getCellAttribute();
        int[] r = new int[nRows];
        for (int i = 0; i < nRows; i++) {
            r[i] = row + i;
        }
        int[] c = {col};
        cellAtt.combine(r,c);       
    }
    
    public Object getValue() {
        
        String stampName = stampNameField.getText().trim();
        if (stampName != null && ! stampName.equals("")) {
            savedStamp.getModuleInfo().setName(stampName);
        }
        
        BundleMed med = new BundleMed();

        FlexibleTableModel tableModel = (FlexibleTableModel)medTable.getModel();
        
        // Admin & its code 
        //AdminInfo info = (AdminInfo)tableModel.getValueAt(ADMIN_ROW, ADMIN_COLUMN);  // ComboBox !
        AdminInfo info = adminInfo;
        med.setAdmin(info.admin);
        med.setAdminCode(info.adminCode);
        
        // 2002-03-22 Default CODE SYSTEM �Ɉڍs
        //med.setAdminCodeId(getCodeSystem());
        
                // ClaimClassCode �̐ݒ�
        med.setClassCode(info.classCode);
        med.setClassCodeSystem(classCodeId);
        med.setClassName(MMLTable.getClaimClassCodeName(info.classCode));
                     
        int rows = tableModel.getRowCount();
        String name;
        String number;
        String unit;
        
        //ClaimNumber cNumber;
        ClaimItem item;
        MasterItem mItem;
        String numberCode = null;
        
        for (int i = 0; i < rows; i++) {
        
            mItem = (MasterItem)tableModel.getValueAt(i, NAME_COLUMN);
            
            if (mItem != null) {
             
                item = new ClaimItem();
                item.setName(mItem.name);
                // Convert
                item.setClassCode(String.valueOf(mItem.classCode));   
                item.setClassCodeSystem(subclassCodeId);
                item.setCode(mItem.code);
                //item.setTableId(mItem.masterTableId);
        
                // ��ޖ��͈��i�̏ꍇ
                if (mItem.classCode != 0) {
                    number = (String)tableModel.getValueAt(i, ONEDAY_COLUMN);
                    number = number.trim();
                    //number = "1";
                    if (number != null && (! number.equals("")) ) {

                        if (mItem.classCode == 1) {
                            // �A�C�e�����ޗ��̏ꍇ
                            numberCode = "21"; //�ޗ���
                        }
                        else {
                            // �A�C�e�������i�̏ꍇ�͗p�@�Ō��܂�
                            numberCode = info.numberCode;
                        }
                        item.setNumber(number);
                        item.setUnit(mItem.unit);
                        item.setNumberCode(numberCode);
                        item.setNumberCodeSystem("Claim004");
                        //cNumber = new ClaimNumber(number, numberCode, mItem.unit);
                        //item.addNumber(cNumber);
                    }
                }
                med.addClaimItem(item);
            }
        }
        
        // Admin Memo
        String memo = adminMemo.getText();
        if (! memo.equals("")) {
            med.setAdminMemo(memo);
        }
        
        // FIXME Memo
        if (inMed.isSelected()) {
            memo = IN_MEDICINE;
        }
        else {
            memo = EXT_MEDICINE;
        }
        med.setMemo(memo);
        
        // BundleNumber
        med.setBundleNumber((String)tableModel.getValueAt(NUMBER_ROW, NUMBER_COLUMN));
        
        //return (Object)med;
        savedStamp.setModel((InfoModel)med);
        return (Object)savedStamp;
    }
    
    public void setValue(Object theModel) {
        
        if (theModel == null) {
            return;
        }
        
        savedStamp = (Module)theModel;
        String stampName = savedStamp.getModuleInfo().getName();
        boolean serialized = savedStamp.getModuleInfo().isSerialized();
        if (!serialized && stampName.startsWith("�G�f�B�^����")) {
            stampName = "�V�K�X�^���v";
        }
        stampNameField.setText(stampName);
        
        BundleMed med = (BundleMed)savedStamp.getModel();
        if (med == null) {
            return;
        }
        
        ClaimItem[] items = med.getClaimItem();
        int count = items.length;
        ClaimItem item;
        String number = null;
        MasterItem mItem;
        FlexibleTableModel tableModel = (FlexibleTableModel)medTable.getModel();
        String saveNumberCode = null;
        
        for (int i = 0; i < count; i++) {
            item = items[i];
            mItem = new MasterItem();
            mItem.classCode = Integer.parseInt(item.getClassCode());
            
            // Code Name TableId
            mItem.name = item.getName();
            mItem.code = item.getCode();
            //mItem.masterTableId = item.getTableId();
            tableModel.setValueAt(mItem, i, NAME_COLUMN);
           
            // ��ނ܂��͈��i
            if (mItem.classCode != 0) {
                number = item.getNumber();
                mItem.unit = item.getUnit();
                
                if (number != null) {
                    if (mItem.classCode == 2) {
                        // ���i�̏ꍇ�͐��ʃR�[�h��ۑ����Ă���
                        saveNumberCode = item.getNumberCode();
                    }
                    tableModel.setValueAt(number, i, ONEDAY_COLUMN);  
                }
                if (mItem.unit != null) {
                    tableModel.setValueAt(mItem.unit, i, UNIT_COLUMN);       // ComboBox !
                }
            }
        }
        
        // Save Administration
        adminInfo = new AdminInfo();
        adminInfo.admin = med.getAdmin();
        //aInfo.showState = 0;
        adminInfo.adminCode = med.getAdminCode();
        adminInfo.numberCode = saveNumberCode;
        adminInfo.classCode = med.getClassCode();
        
        tableModel.setValueAt(adminInfo.admin, ADMIN_ROW, ADMIN_COLUMN);    // ComboBox !
        
        // AdminMemo
        String memo = med.getAdminMemo();
        if (memo != null) {
            adminMemo.setText(memo);
        }
        
        // Memo
        memo = med.getMemo();
        if (memo.equals(IN_MEDICINE)) {
            inMed.setSelected(true);
        }
        else {
            extMed.setSelected(true);
        }
        
        // Bundle number
        tableModel.setValueAt(med.getBundleNumber(), NUMBER_ROW, NUMBER_COLUMN);      // ComboBox !
        
        // RP type;
        //rpType = med.getRPType();
        //numberDiv = med.getNumberDiv();
        
        // Notify
        itemCount = count;
        notifyCount();
    } 
    
    private void checkValidModel() {
        boolean mmlOk = (isCountOk() && isNumberOk() && isAdminOk()) ? true : false;
        System.out.println("Check is valid MML: " + String.valueOf(mmlOk));
        parent.setValidModel(mmlOk);
    }
    
    private boolean isCountOk() {
        return itemCount > 0 ? true : false;
    }

    private boolean isNumberOk() {
        String o = null;
        boolean numberOk = false;
        MasterItem mItem;

        for ( int i = 0; i < itemCount; i++) {
            
            mItem = (MasterItem)medTable.getValueAt(i, NAME_COLUMN);

            // ��ނ܂��͈��i�̏ꍇ�A���ʂ𒲂ׂ�
            if (mItem.classCode != 0) {
                
                o = (String)medTable.getValueAt(i, ONEDAY_COLUMN);
                /*if ( (o != null) && (! o.equals("")) ) {
                    try {
                        int test = Integer.parseInt(o);
                    }
                    catch (Exception e) {
                        Toolkit.getDefaultToolkit().beep();
                        numberOk = false;
                    }                   
                }
                else {
                    numberOk = false;
                }*/
                if ( (o != null) && ! o.trim().equals("") ) {
                    numberOk = true;
                    break;
                }
            }
        } 

        return numberOk;
    }
    
    private boolean isAdminOk() {
        //AdminInfo info = (AdminInfo)medTable.getValueAt(ADMIN_ROW, ADMIN_COLUMN); 
        return  adminInfo != null ? true : false;
    }
    
    private ImageIcon createImageIcon(String name) {  
        String res = RESOURCE_BASE + name;
        return new ImageIcon(this.getClass().getResource(res));
    }
    
    private String getWord2(String adm) {
        
        String ret = null;
        
        int index = adm.indexOf(' ');
        if (index > 0 ) {
            ret = adm.substring(index + 1);
            index = ret.indexOf(' ');
            if (index > 0) {
                ret = ret.substring(index + 1);
            }
        }
        return ret;
    }
    
    private String getCodeSystem() {
        
        String receiCon = Project.getClaimHostName();
        
        if (receiCon.startsWith(FUJITSU_RECICON)) {
            return FUJITSU_CODE_SYSTEM;
            
        } else {
            return DOLPHIN_ADMIN_CODE_SYSTEM;
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public void dragGestureRecognized(DragGestureEvent event) {
        
        try {
            int row = medTable.getSelectedRow();
            if ( isValidRow(row) ) {                
                Transferable t = new IntegerTransferable(new Integer(row));
                dragSource.startDrag(event, DragSource.DefaultCopyDrop, t, this);
            }
        }
        catch (Exception e) {
            System.out.println("Exception at dragGestureRecognized: " + e.toString());
        }
    }

    public void dragDropEnd(DragSourceDropEvent event) { 
    }

    public void dragEnter(DragSourceDragEvent event) {
    }

    public void dragExit(DragSourceEvent event) {
    }

    public void dragOver(DragSourceDragEvent event) {
    }

    public void dropActionChanged ( DragSourceDragEvent event) {
    }
   
    ///////////////////////////////////////////////////////////////////////////
    
    private void setDropTargetBorder(final boolean b) {
        Color c = b ? DesignFactory.getDropOkColor() : this.getBackground();
        medTable.setBorder(BorderFactory.createLineBorder(c));
    }
    
    public boolean isDragAcceptable(DropTargetDragEvent evt) {
        return (evt.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) != 0;
    }
    
    public boolean isDropAcceptable(DropTargetDropEvent evt) {
        return (evt.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) != 0;
    }    
    
    public void dragEnter(DropTargetDragEvent evt) {
        //evt.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        if (! isDragAcceptable(evt)) {
            evt.rejectDrag();
        }
    }
   
    public void dragOver(DropTargetDragEvent evt) {
        if (isDragAcceptable(evt)) {
            setDropTargetBorder(true);
        }
    }
   
    public void dragExit(DropTargetEvent evt) {
        setDropTargetBorder(true);
    }
   
    public void dropActionChanged(DropTargetDragEvent evt) {
        if (! isDragAcceptable(evt)) {
            evt.rejectDrag();
        }
    }
   
    public void drop(DropTargetDropEvent event) {
        
        if (! isDropAcceptable(event)) {
            event.rejectDrop();
            setDropTargetBorder(false);
            return;
        }

        event.acceptDrop(DnDConstants.ACTION_MOVE);
        
        Transferable tr = event.getTransferable();

        // Drop �ʒu�𓾂�
        Point loc = event.getLocation();

        // CopyAction ���ǂ���
        //int action = event.getDropAction();
        //boolean copyAction = (action == DnDConstants.ACTION_COPY);

        // Table �� Drop ����
        boolean ok = doDrop(tr, loc);

        /*if (ok) {
            // ���������ꍇ
            if (copyAction) 
                event.acceptDrop(DnDConstants.ACTION_COPY);
            else 
                event.acceptDrop(DnDConstants.ACTION_MOVE);
        }
        else {
            // ���s�����ꍇ
            event.rejectDrop();
        }*/
        // Drop ����
        event.getDropTargetContext().dropComplete(true);
        setDropTargetBorder(false);
    }   
    
    /**
     * Handle drop.
     */ 
    private boolean doDrop(Transferable tr, Point loc) {
             
        // �T�|�[�g���Ă���@Data Flavor ��
        if (! tr.isDataFlavorSupported(IntegerTransferable.intFlavor)) {
            return false;
        }
        
        // PointToCell
        int toRow = medTable.rowAtPoint(loc); //int toCol = setTable.columnAtPoint(loc);
        toRow = getValidRow(toRow);
        boolean ret = false;
        
        try {
            Integer integer = (Integer)tr.getTransferData(IntegerTransferable.intFlavor);
            DefaultTableModel model = (DefaultTableModel)medTable.getModel();
            int fromIndex = integer.intValue();
            model.moveRow(fromIndex, fromIndex, toRow);
            medTable.getSelectionModel().addSelectionInterval(toRow, toRow);
            ret = true;
        }        
        catch (IOException io) { 
            System.out.println (io);
        } 
        catch (UnsupportedFlavorException ufe) {
            System.out.println (ufe);
        }
        return ret;
    }   
    
    private boolean isValidRow(int row) {
        return (row > -1 && row < itemCount) ? true : false;
    }
    
    private int getValidRow(int row) {
        int start = 0;
        int end = itemCount - 1;
        if (row < start) {
            row = start;
        }
        else if ( row > end ) {
            row = end;
        }
        return row;
    }    
}