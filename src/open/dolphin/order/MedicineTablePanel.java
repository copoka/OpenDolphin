/*
 * MedicineTable.java
 * Copyright (C) 2007 Digital Globe, Inc. All rights reserved.
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
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import open.dolphin.client.*;
import open.dolphin.client.GUIConst;
import open.dolphin.infomodel.BundleMed;
import open.dolphin.infomodel.ClaimItem;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.project.Project;
import open.dolphin.table.ObjectReflectTableModel;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.NumberFormat;
import java.awt.im.InputSubset;


/**
 * Medicine Set Table.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class MedicineTablePanel extends JPanel implements PropertyChangeListener {
    
    private static final long serialVersionUID = 8361225970230987080L;
    
    protected static final String DEFAULT_STAMP_NAME = "�V�K�X�^���v";
    private static final String FROM_EDITOR_STAMP_NAME = "�G�f�B�^����";
    
    private static final String[] COLUMN_NAMES = {"�R�[�h", "�� ��", "���(��)��","�P��"};
    private static final String[] METHOD_NAMES = {"getCode", "getName", "getNumber", "getUnit"};
    private static final int[] COLUMN_WIDTH = {50, 200, 40, 40};
    private static final String TOOLTIP_DELETE = "�I�������A�C�e�����폜���܂�";
    private static final String TOOLTIP_CLEAR = "�Z�b�g���e���N���A���܂�";
    private static final String TOOLTIP_DND = "�h���b�O & �h���b�v�ŏ��Ԃ����ւ��邱�Ƃ��ł��܂�";
    private static final String TOOLTIP_ADMIN = "�p�@���}�X�^����I��ł�������";
    private static final String TOOLTIP_ADMIN_COMMENT = "�p�@�R�����g���}�X�^����I��ł�������";
    private static final String RESOURCE_BASE       = "/open/dolphin/resources/images/";
    private static final String REMOVE_BUTTON_IMAGE = "del_16.gif";
    private static final String CLEAR_BUTTON_IMAGE  = "remov_16.gif";
    private static final String LABEL_TEXT_IN_MED = "�@��";
    private static final String LABEL_TEXT_OUT_MED = "�@�O";
    private static final String IN_MEDICINE     = "�@������";
    private static final String EXT_MEDICINE    = "�@�O����";
    private static final String LABEL_TEXT_ADMIN = "�p�@";
    private static final String LABEL_TEXT_MEMO = "����";
    private static final String LABEL_TEXT_QUONTITY = "��(��)��";
    private static final String LABEL_TEXT_STAMP_NAME = "�X�^���v��";
    
    /** Table �̍s�� */
    private static final int ROWS               = 9;
    
    /** ���ʃJ�����ԍ� */
    private static final int ONEDAY_COLUMN      = 2;
    
    //
    // GUI �R���|�[�l���g
    //
    /** �Z�b�g�e�[�u���� TableModel */
    private ObjectReflectTableModel medTableModel;
    
    /** �Z�b�g�e�[�u���� JTable */
    private JTable medTable;
    
    /** �p�@�t�B�[���h */
    private JTextField adminField;
    
    /** �X�^���v���t�B�[���h */
    private JTextField stampNameField;
    
    /** �p�@�����t�B�[���h */
    //private JTextField adminMemo;
    
    /** �@������ */
    private JRadioButton inMed;
    
    /** �@�O���� */
    private JRadioButton extMed;
    
    /** ���ʃR���{�{�b�N�X */
    private JComboBox numberCombo;
    
    /** �폜�{�^�� */
    private JButton removeButton;
    
    /** �N���A�{�^�� */
    private JButton clearButton;
    
    /** StampModelEditor */
    private StampModelEditor parent;
    
    /** �L�����f���t���O */
    private boolean validModel;
    
    /** State Manager */
    private MedTableStateMgr stateMgr;
    
    /** ���̃G�f�B�^�� Entity */
    private String entity;
    
    /** �p�@ */
    private AdminInfo adminInfo;
    
    /** �ĕҏW�̏ꍇ�ɕۑ����Ă������Z�d�Z�R�[�h */
    private String saveReceiptCode;
    
    /** �ĕҏW�̏ꍇ�ɕۑ����Ă������ʃR�[�h */
    private String saveNumberCode;
    
    
    /**
     * Creates new MedicineTable
     */
    public MedicineTablePanel(StampModelEditor parent) {
        
        super(new BorderLayout());
        
        setParent(parent);
        
        // ��܃Z�b�g�e�[�u���𐶐�����
        medTableModel = new ObjectReflectTableModel(COLUMN_NAMES, ROWS, METHOD_NAMES, null) {
            
            private static final long serialVersionUID = 2532058728387931882L;
            
            // ����i���j���ʂ̂ݕҏW�\
            public boolean isCellEditable(int row, int col) {
                return col == ONEDAY_COLUMN ? true : false;
            }
            
            public void setValueAt(Object o, int row, int col) {
                if (o == null || ((String)o).trim().equals("")) {
                    return;
                }
                
                MasterItem mItem = (MasterItem) getObject(row);
                
                if (col == ONEDAY_COLUMN && mItem != null) {
                    mItem.setNumber((String) o);
                    stateMgr.checkState();
                }
            }
        };
        
        // Table�𐶐�����
        medTable = new JTable(medTableModel);
        medTable.setToolTipText(TOOLTIP_DND);
        
        //
        // Table �� MasterItemTrasferHandler ��ݒ肷��
        //
        medTable.setTransferHandler(new MasterItemTransferHandler());
        medTable.addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                int ctrlMask = InputEvent.CTRL_DOWN_MASK;
                int action = ((e.getModifiersEx() & ctrlMask) == ctrlMask) ?
                    TransferHandler.COPY : TransferHandler.MOVE;
                JComponent c = (JComponent)e.getSource();
                TransferHandler handler = c.getTransferHandler();
                handler.exportAsDrag(c, e, action);
            }
            public void mouseMoved(MouseEvent e) {}
        });
        medTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        medTable.setRowSelectionAllowed(true);
        medTable.setSurrendersFocusOnKeystroke(true);         //JDK 1.4
        medTable.setDefaultRenderer(Object.class, new OddEvenRowRenderer());
        
        //
        // �s���I�����ꂽ���A�폜�{�^���𐧌䂷��
        //
        ListSelectionModel m = medTable.getSelectionModel();
        m.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    notifySelectedRow();
                }
            }
        });
        
        //
        // ���ʃJ�����ɃG�f�B�^��ݒ肷��
        // IME �� OFF �ɂ���
        //
        NumberFormat numFormat = NumberFormat.getNumberInstance();
        numFormat.setMinimumFractionDigits(2);
        JFormattedTextField tf = new JFormattedTextField(numFormat);
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent event) {
                JFormattedTextField tf = (JFormattedTextField) event.getSource();
                tf.getInputContext().setCharacterSubsets(null);
            }
        });
        
        TableColumn column = medTable.getColumnModel().getColumn(ONEDAY_COLUMN);
        column.setCellEditor(new DefaultCellEditor(tf));
        
        // �񕝂�ݒ肷��
        int len = COLUMN_NAMES.length;
        for (int i = 0; i < len; i++) {
            column = medTable.getColumnModel().getColumn(i);
            column.setPreferredWidth(COLUMN_WIDTH[i]);
        }
        
        // �p�@�t�B�[���h�𐶐�����
        adminField = new JTextField(10);
        adminField.setEditable(false);
        adminField.setToolTipText(TOOLTIP_ADMIN);
        
        // �����t�B�[���h�𐶐�����
        //adminMemo = new JTextField(8);
        //adminMemo.setToolTipText(TOOLTIP_ADMIN_COMMENT);
        
        // �o���h���i���o�[ Combo�𐶐�����
        String[] numberList = ClientContext.getStringArray("rp.number.list");
        numberCombo = new JComboBox(numberList);
        numberCombo.setMaximumSize(numberCombo.getPreferredSize());
        //
        // ���ʂ�����͂ł���悤�ɂ���
        //
        numberCombo.setEditable(true);
        
        // �@���E�@�O�������W�I�{�^���𐶐�����
        inMed = new JRadioButton(LABEL_TEXT_IN_MED);
        extMed = new JRadioButton(LABEL_TEXT_OUT_MED);
        ButtonGroup g = new ButtonGroup();
        g.add(inMed);
        g.add(extMed);
        boolean bOut = Project.getPreferences().getBoolean(Project.RP_OUT, true);
        if (bOut) {
            extMed.setSelected(true);
            
        } else {
            inMed.setSelected(true);
        }
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean b = extMed.isSelected();
                Project.getPreferences().putBoolean(Project.RP_OUT, b);
            }
        };
        inMed.addActionListener(al);
        extMed.addActionListener(al);
        
        // StampName�t�B�[���h�𐶐�����
        stampNameField = new JTextField(8);
        stampNameField.setOpaque(true);
        stampNameField.setBackground(new Color(251,239,128));
        stampNameField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent event) {
                stampNameField.getInputContext().setCharacterSubsets(new Character.Subset[] {InputSubset.KANJI});
            }
        });
        
        // Remove button�𐶐�����
        removeButton = new JButton(createImageIcon(REMOVE_BUTTON_IMAGE));
        removeButton.setEnabled(false);
        removeButton.setToolTipText(TOOLTIP_DELETE);
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedItem();
            }
        });
        
        // Clear button�𐶐�����
        clearButton = new JButton(createImageIcon(CLEAR_BUTTON_IMAGE));
        clearButton.setEnabled(false);
        clearButton.setToolTipText(TOOLTIP_CLEAR);
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clear();
            }
        });
        
        
        // �X�^���v���A���ʁA�p�@�A�����A�@���@�O�p�l���𐶐�����
        JPanel infoP = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoP.add(new JLabel(LABEL_TEXT_STAMP_NAME));
        infoP.add(stampNameField);
        infoP.add(new JLabel(LABEL_TEXT_QUONTITY));
        infoP.add(numberCombo);
        infoP.add(new JLabel(LABEL_TEXT_ADMIN));
        infoP.add(adminField);
        //infoP.add(Box.createHorizontalGlue());
        //infoP.add(new JLabel(LABEL_TEXT_MEMO));
        //infoP.add(adminMemo);
        infoP.add(inMed);
        infoP.add(extMed);
        
        // �{�^���p�l���𐶐�����
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bp.add(removeButton);
        bp.add(clearButton);
        if (parent.getContext().getOkButton() != null) {
            bp.add(parent.getContext().getOkButton());
        }
        
        // ��p�l���𐶐�����
        JPanel southP = new JPanel();
        southP.setLayout(new BoxLayout(southP, BoxLayout.X_AXIS));
        southP.add(infoP);
        //southP.add(Box.createHorizontalGlue());
        southP.add(bp);
        
        // �Z�b�g�e�[�u���p�̃p�l���𐶐�����
        //JPanel setP = new JPanel(new BorderLayout());
        //setP.add(medTable.getTableHeader(), BorderLayout.NORTH);
        //setP.add(medTable, BorderLayout.CENTER);
        
        // �S�̂����C�A�E�g����
        JScrollPane scroller = new JScrollPane(medTable);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.add(scroller, BorderLayout.CENTER);
        this.add(southP, BorderLayout.SOUTH);
        this.setPreferredSize(new Dimension(GUIConst.DEFAULT_EDITOR_WIDTH, GUIConst.DEFAULT_EDITOR_HEIGHT+50));
        
        // ���ʃf�t�H���g
        setDefaultBundleNumber();
        
        // StateMgr�𐶐�����
        stateMgr = new MedTableStateMgr(this, medTable, removeButton, clearButton, stampNameField, adminField);
    }
    
    private void setDefaultBundleNumber() {
        numberCombo.setSelectedIndex(2);
    }
    
    public StampModelEditor getMyParent() {
        return this.parent;
    }
    
    public void setParent(StampModelEditor parent) {
        this.parent = parent;
    }
    
    public String getEntity() {
        return entity;
    }
    
    public void setEntity(String val) {
        entity = val;
    }
    
    public boolean isValidModel() {
        return validModel;
    }
    
    public void setValidModel(boolean valid) {
        validModel = valid;
        getMyParent().setValidModel(validModel);
    }
    
    /**
     * ���i�y�їp�@�̒ʒm���󂯁A�f�[�^���Z�b�g����B
     * @param e PropertyChangeEvent
     */
    public void propertyChange(PropertyChangeEvent e) {
        
        String prop = e.getPropertyName();
            
        Object newValue = e.getNewValue();

        if (newValue != null && (newValue instanceof MasterItem)) {

            MasterItem item = (MasterItem) newValue;

            //if (medTableModel.getObjectCount() < ROWS) {

                medTableModel.addRow(item);

                String name = stampNameField.getText().trim();
                if (name.equals("") || name.equals(DEFAULT_STAMP_NAME)) {
                    stampNameField.setText(item.getName());
                }
                stateMgr.checkState();
            //}

        } else if (newValue != null && (newValue instanceof AdminInfo)) {

            AdminInfo info = (AdminInfo) newValue;

            switch(info.eventType) {

                case AdminInfo.TT_ADMIN:
                    adminInfo = info;
                    adminField.setText(info.getAdmin());
                    stateMgr.checkState();
                    break;

                case AdminInfo.TT_MEMO:
                    //adminMemo.setText(info.getAdminMemo());
                    break;
            }   
        }   
    }
    
    private void notifySelectedRow() {
        int index = medTable.getSelectedRow();
        boolean b = medTableModel.getObject(index) != null ? true : false;
        removeButton.setEnabled(b);
    }
    
    private void clear() {
        medTableModel.clear();
        stateMgr.checkState();
    }
    
    private void removeSelectedItem() {
        
        int index = medTable.getSelectedRow();
        if (index < 0) {
            notifySelectedRow();
            return;
        }
        Object o = medTableModel.getObject(index);
        if ( o == null ) {
            notifySelectedRow();
            return;
        }
        
        medTableModel.deleteRow(index);
        stateMgr.checkState();
    }
    
    /**
     * �G�f�B�^�ŕҏW�����X�^���v��Ԃ��B
     * @return �G�f�B�^�ŕҏW�����X�^���v
     */
    public Object getValue() {
        
        //
        // ��ɐV�K�̃I�u�W�F�N�g�Ƃ��ĕԂ�
        //
        ModuleModel retModel = new ModuleModel();
        BundleMed med = new BundleMed();
        retModel.setModel(med);
        
        //
        // StampInfo��ݒ肷��
        //
        ModuleInfoBean moduleInfo = retModel.getModuleInfo();
        moduleInfo.setEntity(getEntity());
        moduleInfo.setStampRole(IInfoModel.ROLE_P);
        
        //
        //�@�X�^���v����ݒ肷��
        //
        String stampName = stampNameField.getText().trim();
        if (!stampName.equals("")) {
            moduleInfo.setStampName(stampName);
        } else {
            moduleInfo.setStampName(DEFAULT_STAMP_NAME);
        }
        
        //
        // Data List ���C�e���[�g����
        //
        java.util.List list = medTableModel.getObjectList();
        String receiptCode = null;
        
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            
            MasterItem mItem = (MasterItem) iter.next();
            
            if (mItem != null) {
                
                //
                // ClaimItem �𐶐�����
                //
                ClaimItem item = new ClaimItem();
                item.setClassCode(String.valueOf(mItem.getClassCode()));
                item.setClassCodeSystem(ClaimConst.SUBCLASS_CODE_ID);
                item.setCode(mItem.getCode());
                item.setName(mItem.getName());
                
                //
                // ��܂̏ꍇ
                //
                if (mItem.getClassCode() == ClaimConst.YAKUZAI) {
                
                    //
                    // ���Z�d�Z�R�[�h��ݒ肷��
                    // ��܋敪�� 1=���p(210) 6=�O�p(230)
                    // �ŏ��̖�܂݂̂��g�p����
                    //
                    if (receiptCode == null) {
                        
                        if (mItem.getYkzKbn() != null) {
                            
                            receiptCode = mItem.getYkzKbn().equals(ClaimConst.YKZ_KBN_NAIYO)
                                        ? ClaimConst.RECEIPT_CODE_NAIYO 
                                        : ClaimConst.RECEIPT_CODE_GAIYO;
                            
                        } else if (saveReceiptCode != null) {
                            
                            receiptCode = saveReceiptCode;
                            
                        } else {
                            
                            receiptCode = ClaimConst.RECEIPT_CODE_NAIYO;
                        }
                        
                        med.setClassCode(receiptCode);
                        med.setClassCodeSystem(ClaimConst.CLASS_CODE_ID);
                        med.setClassName(MMLTable.getClaimClassCodeName(receiptCode));
                    }
                    
                    //
                    // ���ʂƐ��ʃR�[�h��ݒ肷��
                    //
                    String number = mItem.getNumber();
                    if (number != null && (! number.trim().equals("")) ) {
                        item.setNumber(number.trim());
                        item.setUnit(mItem.getUnit());
                        //
                        // ���ʃR�[�h 10/11/12 2007-05 ���݂�ORCA�̎����ł͍̗p���Ă��Ȃ�
                        //
                        item.setNumberCode(ClaimConst.YAKUZAI_TOYORYO);
                        item.setNumberCodeSystem(ClaimConst.NUMBER_CODE_ID);
                    }
                    
                    med.addClaimItem(item);
                
                } else if (mItem.getClassCode() == ClaimConst.ZAIRYO) {
                    //
                    // �ޗ��̏ꍇ
                    //
                    String number = mItem.getNumber();
                    if (number != null && (! number.trim().equals("")) ) {
                        item.setNumber(number.trim());
                        item.setUnit(mItem.getUnit());
                        item.setNumberCode(ClaimConst.ZAIRYO_KOSU);
                        item.setNumberCodeSystem(ClaimConst.NUMBER_CODE_ID);
                    }
                    
                    med.addClaimItem(item);
                    
                } else if (mItem.getClassCode() == ClaimConst.SYUGI) {
                    //
                    // ��Z�̏ꍇ
                    //
                    med.addClaimItem(item);
                }
            }
        }
        
        //
        // �p�@��ݒ肷��
        //
        if ( adminInfo != null && (!adminField.getText().trim().equals("")) ) {
            med.setAdmin(adminInfo.getAdmin());
            med.setAdminCode(adminInfo.getAdminCode());
        }
        
//        // Admin Memo
//        String memo = adminMemo.getText();
//        if (! memo.equals("")) {
//            med.setAdminMemo(memo);
//        }
        
        // FIXME Memo
        String memo = null;
        if (inMed.isSelected()) {
           memo = IN_MEDICINE;
        } else {
            memo = EXT_MEDICINE;
        }
        med.setMemo(memo);
        
        // BundleNumber
        med.setBundleNumber((String) numberCombo.getSelectedItem());
        
        return (Object) retModel;
    }
    
    /**
     * �ҏW����X�^���v��\������B
     * @param theModel �ҏW����X�^���v
     */
    public void setValue(Object theStamp) {
        
        
        // �A�����ĕҏW�����ꍇ������̂Ńe�[�u�����e�����N���A����
        clear();
        
        
        if (theStamp == null) {
            // State��ύX����
            stateMgr.checkState();
            return;
        }
        // �����œn���ꂽ Stamp ���L���X�g����
        ModuleModel target  = (ModuleModel) theStamp;
        
        // Entity��ۑ�����
        setEntity(target.getModuleInfo().getEntity());
        
        // �X�^���v����\������
        String stampName = target.getModuleInfo().getStampName();
        boolean serialized = target.getModuleInfo().isSerialized();
        
        if (!serialized && stampName.startsWith(FROM_EDITOR_STAMP_NAME)) {
            stampName = DEFAULT_STAMP_NAME;
        } else if (stampName.equals("")) {
            stampName = DEFAULT_STAMP_NAME;
        }
        stampNameField.setText(stampName);
        
        BundleMed med = (BundleMed) target.getModel();
        if (med == null) {
            return;
        }
        
        //
        // ���Z�d�Z�R�[�h��ۑ�����
        //
        if (med.getClassCode() != null) {
            saveReceiptCode = med.getClassCode();
        }
        
        ClaimItem[] items = med.getClaimItem();
        String saveNumberCode = null;
        
        for (ClaimItem item : items) {
            
            MasterItem mItem = new MasterItem();
            mItem.setClassCode(Integer.parseInt(item.getClassCode()));
            
            // Code Name TableId
            mItem.setName(item.getName());
            mItem.setCode(item.getCode());
            
            // ��ނ܂��͈��i
            if (mItem.getClassCode() != ClaimConst.SYUGI) {
                mItem.setNumber(item.getNumber());
                mItem.setUnit(item.getUnit());
                
                if (mItem.getNumber() != null) {
                    if (mItem.getClassCode() == ClaimConst.YAKUZAI) {
                        // ���i�̏ꍇ�͐��ʃR�[�h��ۑ����Ă���
                        saveNumberCode = item.getNumberCode();
                    }
                }
            }
            medTableModel.addRow(mItem);
        }
        
        // Save Administration
        if (med.getAdmin() != null) {
            adminInfo = new AdminInfo();
            adminInfo.setAdmin(med.getAdmin());
            adminInfo.setAdminCode(med.getAdminCode());
            adminField.setText(adminInfo.getAdmin());
        }
        
        // Memo
        String memo = med.getMemo();
        if (memo != null && memo.equals(IN_MEDICINE)) {
            inMed.setSelected(true);
        } else {
            extMed.setSelected(true);
        }
        
        // Bundle number
        numberCombo.setSelectedItem(med.getBundleNumber());
        
        // Notify
        stateMgr.checkState();
    }
    
    private ImageIcon createImageIcon(String name) {
        String res = RESOURCE_BASE + name;
        return new ImageIcon(this.getClass().getResource(res));
    }
}