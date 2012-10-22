/*
 * DiagnosisEditor.java
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
import open.dolphin.infomodel.RegisteredDiagnosisModule;
import open.dolphin.table.*;
import open.dolphin.util.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.beans.*;

/**
 * Diagnosis editor.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class DiagnosisEditor extends StampModelEditor  {
    
    // Staff
    private DiagnosisTable diagnosisTable;
    private MasterTabPanel masterPanel;
    
    // Diagnosis table
    private static final String DIAGNOSIS_TITLE_BORDER = "���a��";
    /*private static final String [] DIAGNOSIS_COLUMN_NAMES = {
        "������", "���ޖ�", "�] �A", "�������f��","�����J�n��","�����I����"
    };*/
    private static final String [] DIAGNOSIS_COLUMN_NAMES = {
        "������", "�� ��", "�] �A", "�����J�n��", "�����I����"
    };
    private static final int NAME_COL            = 0;
    private static final int CATEGORY_COL        = 1;
    private static final int OUTCOME_COL         = 2;
    //private static final int FIRST_ENCOUNTER_COL = 3;
    private static final int START_DATE_COL      = 3;
    private static final int END_DATE_COL        = 4;
    /*private static final String[] CATEGORY_LIST = {      
        "","��a��", "����(����)��", "�f�f�Q��(DRG)",
        "�w�p�f�f��", "�㎖�a��", "�Տ��f�f��", "�a���f�f��", "�����f�f��", "��p�f�f��", 
        "�m��f�f","�^���a��"
    };*/
    private static final String[] CATEGORY_LIST = {      
        "","��a��","�^���a��"
    };
    private static final String[] OUTCOME_LIST = {      
        "","��", "�S��", "������(�̔���)", "�I��", "���~", "�p��", "���S", "����", "�s��",
        "�]��", "�]��(�}���a�@��)","�]��(�����a�@��)","����֑މ@", "�s��"
    };
    /*private static final int[] DIAGNOSIS_TABLE_COLUMN_WIDTHS = {
        300, 90, 90, 90, 90, 90
    };*/
    private static final int[] DIAGNOSIS_TABLE_COLUMN_WIDTHS = {
        300, 90, 90, 90, 90
    };
    private static final String RESOURCE_BASE       = "/open/dolphin/resources/images/";
    private static final String REMOVE_BUTTON_IMAGE = "Delete24.gif";
    private static final String CLEAR_BUTTON_IMAGE  = "New24.gif";
    private JButton removeButton;
    private JButton clearButton;
    private boolean okState;
    
    private String defaultCategory = "��a��";
    
    /** Creates new DiagnosisEditor */
    public DiagnosisEditor() {
        
        //this.title = "Dolphin: ���a���G�f�B�^";
        
        // Creates staffs       
        diagnosisTable = new DiagnosisTable();
        diagnosisTable.setBorder(BorderFactory.createTitledBorder(DIAGNOSIS_TITLE_BORDER));
        
        masterPanel = new MasterTabPanel();
        masterPanel.startDiagnosis(diagnosisTable);
                
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(diagnosisTable);
        add(masterPanel);
        setPreferredSize(new Dimension(920, 610));
    }
    
    public Object getValue() {
        return diagnosisTable.getValue();
    }
    
    public void setValue(Object val) {
        diagnosisTable.setValue((Object[])val);
    }
    
    public void dispose() {
        masterPanel.stopDiagnosis(diagnosisTable);
    }
    
    private ImageIcon createImageIcon(String name) {
        String res = RESOURCE_BASE + name;
        return new ImageIcon(this.getClass().getResource(res));
    }
    
    protected final class DiagnosisTable extends JPanel implements PropertyChangeListener {

        // Table model
        private ArrayListTableModel tableModel;

        // Table
        private JTable table;
        
        private JComboBox categoryCombo;
        
        private JComboBox outcomeCombo;
        
        public DiagnosisTable() {
            
            super(new BorderLayout());

            tableModel = new ArrayListTableModel(DIAGNOSIS_COLUMN_NAMES, 10) {
                
                public boolean isCellEditable (int row, int col) {
                    //return (col != 0) ? true : false;
                    return true;
                }
                
                public void setValueAt(Object o, int row, int col) {
                    
                    // �J�e�S���̎�
                    if (col == CATEGORY_COL) {
                        defaultCategory = (String)o;
                        Object[] rowData = getRowData(row);
                        if (rowData != null) {
                            rowData[col] = defaultCategory;
                            fireTableCellUpdated(row, col);
                        }
                        return;
                    }
                    
                    // �J�e�S���ȊO�� null �̏ꍇ
                    if (o == null || o.equals("")) {
                        return;
                    }
                    
                    
                    // �a���ȊO
                    if (col != NAME_COL) {
                        Object[] rowData = getRowData(row);
                        if (rowData != null) {
                            rowData[col] = o;
                            fireTableCellUpdated(row, col);
                            return;
                        }
                    }
                    
                    // �a���̎�
                    Object[] rowData = getRowData(row);
                    
                    if (rowData != null) {
                        
                        // �C���ꂪ���͂���Ă���Ɖ���
                        MasterItem item = (MasterItem)rowData[col];
                        item.name = ((String)o);
                        //item.code = null; ���a���R�[�h�͕ۑ�����
                        fireTableCellUpdated(row, col);
                        
                    } else {
                        
                        // �a������́A�R�[�h�Ȃ�
                        MasterItem item = new MasterItem();
                        item.name = ((String)o);
                        
                        // ���ޖ� ���t��������
                        GregorianCalendar gc = new GregorianCalendar();
                        String today = MMLDate.getDate(gc);
                        //Object[] data = new Object[]{item, defaultCategory, null, today, today, null};
                        // ������, �J�e�S��, �]�A, ���f��, �I����
                        Object[] data = new Object[]{item, defaultCategory, null, today, null};
                        addRow(data);
                        notifyDataCount();
                    }
                }
            };
            table = new JTable(tableModel);
            table.setSurrendersFocusOnKeystroke(true);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setRowSelectionAllowed(true);
            ListSelectionModel m = table.getSelectionModel();
            m.addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting() == false) {
                        notifySelectedRow();
                    }
                }
            });
            
            // CellEditor ��ݒ肷��
            // NAME_COL clickCountToStart=1, IME=ON
            TableColumn column = table.getColumnModel().getColumn(NAME_COL);
            column.setCellEditor (new IMECellEditor (new JTextField(), 1, true));
            
            // Category comboBox ���͂�ݒ肷��
            categoryCombo = new JComboBox (CATEGORY_LIST);
            column = table.getColumnModel().getColumn(CATEGORY_COL);
            column.setCellEditor (new DefaultCellEditor (categoryCombo));
          
            // Outcome comboBox ���͂�ݒ肷��
            outcomeCombo = new JComboBox (OUTCOME_LIST);
            column = table.getColumnModel().getColumn(OUTCOME_COL);
            column.setCellEditor (new DefaultCellEditor (outcomeCombo));
          
            // �񕝐ݒ�
            int len = DIAGNOSIS_TABLE_COLUMN_WIDTHS.length;
            for (int i = 0; i < len; i++) {
                column = table.getColumnModel().getColumn(i);
                column.setPreferredWidth(DIAGNOSIS_TABLE_COLUMN_WIDTHS[i]);
            }
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
          
            // Command button
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(Box.createHorizontalGlue());
            
            removeButton = new JButton(createImageIcon(REMOVE_BUTTON_IMAGE));
            removeButton.setEnabled(false);
            removeButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    int row = table.getSelectedRow();
                    // TableModel �Ń����W�`�F�b�N���Ă���̂ň��S
                    tableModel.removeRow(row);
                    notifySelectedRow();
                    notifyDataCount();
                }
            });
            p.add(removeButton);

            p.add(Box.createRigidArea(new Dimension(5, 0)));

            clearButton = new JButton(createImageIcon(CLEAR_BUTTON_IMAGE));
            clearButton.setEnabled(false);
            clearButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    tableModel.clear();
                    notifySelectedRow();
                    notifyDataCount();
                }
            });
            p.add(clearButton);     
            p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            // �J�����w�b�_�[��\�����ă��C�A�E�g����
            add(table.getTableHeader(), BorderLayout.NORTH);
            add(table, BorderLayout.CENTER);
            add(p, BorderLayout.SOUTH);
        }

        public void propertyChange(PropertyChangeEvent e) {
            
            String prop = e.getPropertyName();
            
            if (prop.equals("selectedItemProp")) {
                
                MasterItem item = (MasterItem)e.getNewValue();
                if (item != null) {                    
                    // ���ޖ� ���t��������
                    GregorianCalendar gc = new GregorianCalendar();
                    String today = MMLDate.getDate(gc);
                    //Object[] data = new Object[]{item, defaultCategory, null, today, today, null};
                    Object[] data = new Object[]{item, defaultCategory, null, today, null};
                    tableModel.addRow(data);
                    notifyDataCount();
                }
            }
        }
        
        private void notifySelectedRow() {
            int row = table.getSelectedRow();
            boolean b = tableModel.isValidRow(row);
            removeButton.setEnabled(b);
        }
        
        private void notifyDataCount() {
            boolean b = (tableModel.getDataCount() > 0) ? true : false;
            clearButton.setEnabled(b);
            if (b != okState) {
                okState = b;
                setValidModel(b);
            }
        }
        
        public Object getValue() {
            
            RegisteredDiagnosisModule diagnosis = null;
            ArrayList list = null;

            // �e�[�u�����X�L��������
            int count = tableModel.getDataCount();

            for (int i = 0; i < count; i++) {

                // �a��
                MasterItem item = (MasterItem)table.getValueAt(i, NAME_COL);

                if (item != null) {

                    // Dignosis & code
                    diagnosis = new RegisteredDiagnosisModule();
                    diagnosis.setDiagnosis(item.name);
                    diagnosis.setDiagnosisCode(item.code);
                    diagnosis.setDiagnosisCodeSystem(item.masterTableId);
                    //diagnosis.setClaimDiseaseCode(item.claimDiseaseCode);
                    
                    // Category
                    String data = (String)table.getValueAt(i, CATEGORY_COL);                // ���{��\��
                    if ( data != null && (! data.equals("")) ) {
                        String value = (String)MMLTable.getDiagnosisCategoryValue(data);    // �K�i�l
                        String tableId = (String)MMLTable.getDiagnosisCategoryTable(value); // TableId
                        //diagnosis.addCategory(new Category(value, tableId));
                        diagnosis.setCategory(value);
                        diagnosis.setCategoryTable(tableId);                    
                    }

                    // Outcome
                    data = (String)table.getValueAt(i, OUTCOME_COL);                    
                    if ( data != null && (! data.equals("")) ) {
                        String value = (String)MMLTable.getDiagnosisOutcomeValue(data);      // �K�i�l
                        diagnosis.setOutcome(value);
                    }

                    // FirstEncounter date
                    /*data = (String)table.getValueAt(i, FIRST_ENCOUNTER_COL);
                    if ( data != null && (! data.equals("")) ) {
                        diagnosis.setFirstEncounterDate(data);
                    }*/
                    
                    // Start date
                    data = (String)table.getValueAt(i, START_DATE_COL);
                    if ( data != null && (! data.equals("")) ) {
                       diagnosis.setStartDate(data);
                    }
                    
                    // End date
                    data = (String)table.getValueAt(i, END_DATE_COL);
                    if ( data != null && (! data.equals("")) ) {
                        diagnosis.setEndDate(data);
                    }

                    if (list == null) {
                        list = new ArrayList(10);
                    }
                    list.add(diagnosis);
                }
            }
            return (list != null) ? list : null;
        }

        public void setValue(Object[] o) {

            //setValidModel(true);
            if (o == null) return;
            int len = o.length;

            for (int i = 0; i < len; i++) {
                
                Object[] obj = new Object[DIAGNOSIS_COLUMN_NAMES.length];

                RegisteredDiagnosisModule diagnosis = (RegisteredDiagnosisModule)o[i];

                // Dignosis
                MasterItem item = new MasterItem();
                item.name = diagnosis.getDiagnosis();
                item.code = diagnosis.getDiagnosisCode();
                item.masterTableId = diagnosis.getDiagnosisCodeSystem();
                //item.claimDiseaseCode = diagnosis.getClaimDiseaseCode();
                obj[NAME_COL] = item;

                // Category
                //Category category = diagnosis.getCategory(0);
                String category = diagnosis.getCategory();
                if (category != null) {
                    //String data = category.getCategory();
                    if ( (category != null) && (! category.equals(""))) {
                        obj[CATEGORY_COL] = (String)MMLTable.getDiagnosisCategoryDesc(category); // ���{��\��
                    }
                }
                
                // Outcome
                String data = diagnosis.getOutcome();
                if ( (data != null) && (! data.equals(""))) {
                    obj[OUTCOME_COL] = (String)MMLTable.getDiagnosisOutcomeDesc(data);  // ���{��\��
                }
                
                // First encounter date
                /*data = diagnosis.getFirstEncounterDate();
                if ( (data != null) && (! data.equals(""))) {
                    obj[FIRST_ENCOUNTER_COL] = data;
                }*/
                
                // Start date
                data = diagnosis.getStartDate();
                if ( (data != null) && (! data.equals(""))) {
                    obj[START_DATE_COL] = data;
                }
                
                // End date
                data = diagnosis.getEndDate();
                if ( (data != null) && (! data.equals(""))) {
                    obj[END_DATE_COL] = data;
                }  
                
                // Add row data
                tableModel.addRow(obj);
            }
            notifyDataCount();
        }
    }
}