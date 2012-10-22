/*
 * BaseClinicStampEditor.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2004 Digital Globe, Inc. All rights reserved.
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
import javax.swing.table.*;

import open.dolphin.infomodel.Allergy;
import open.dolphin.infomodel.BaseClinicModule;
import open.dolphin.infomodel.BloodType;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.Infection;
import open.dolphin.infomodel.Module;
import open.dolphin.table.*;
import open.dolphin.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.im.InputSubset;
import java.util.ArrayList;

/**
 * ��b�I�f�Ï�񃂃W���[���G�f�B�^�N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class BaseClinicStampEditor extends StampModelEditor {
           
    // �A�����M�[�e�[�u���̃J������
    final String [] allergyColumnNames  = ClientContext.getStringArray("allergy.columnNames");
    private final int FACTOR_COLUMN     = 0;
    private final int SEVERITY_COLUMN   = 1;
    private final int IDENTIFIED_COLUMN = 2;
    private final int MEMO_COLUMN       = 3;
    
    // �A�����M�������x���X�g
    final String[] severityList = ClientContext.getStringArray("allergy.severityList");
    
    // �����ǃe�[�u���̃J������
    final String [] infectionColumnNames = ClientContext.getStringArray("infection.columnNames");
    private final int EXAM_VALUE_COLUMN = 1;

    // �A�����M�[
    private AllergyPanel allergyPanel;

    // ���t�^   
    private BloodtypePanel bloodTypePanel;

    // ������
    private InfectionPanel infectionPanel;
    
    private boolean okState;
    
    private Module savedStamp;

    /** Creates new BaseClinicStampEditor */
    public BaseClinicStampEditor() {
        this.title = "��b�I�f�Ï��";
        createComponent();
    }

    //-----------------------------------------------------------------
    // �T�u�N���X���������ׂ����\�b�h
    //-----------------------------------------------------------------

    /**
     * ��b�I�f�Ï���ҏW���邽�߂̃p�l���iGUI�j��Ԃ� 
     */
    protected void createComponent() {

        setLayout (new BoxLayout (this, BoxLayout.Y_AXIS));

        allergyPanel = new AllergyPanel();
        allergyPanel.setBorder(BorderFactory.createTitledBorder("�A�����M�[���"));
        add(allergyPanel);
        
        add(Box.createVerticalStrut(7));

        bloodTypePanel = new BloodtypePanel();
        bloodTypePanel.setBorder(BorderFactory.createTitledBorder("���t�^"));
        add(bloodTypePanel);
        
        add(Box.createVerticalStrut(7));

        infectionPanel = new InfectionPanel();
        infectionPanel.setBorder(BorderFactory.createTitledBorder("�����Ǐ��"));
        add(infectionPanel);
        
        setPreferredSize(new Dimension(600, 400));
    }

    /**
     * UI�G�f�B�^�̃��f����ݒ肵�l��\������
     */
    public void setValue(Object stamp) {
        
        savedStamp = (Module)stamp;
        IInfoModel model = savedStamp.getModel();
        
        if (model == null) {
            return;
        }
        allergyPanel.setValue(((BaseClinicModule)model).getAllergy());
        bloodTypePanel.setValue(((BaseClinicModule)model).getBloodType());
        infectionPanel.setValue(((BaseClinicModule)model).getInfection());
    }
   
   /**
    * GUI ���烂�f���l���擾����
    */
    public Object getValue() {
                  
        BaseClinicModule module = new BaseClinicModule();
        
        // �A�����M
        module.setAllergy(allergyPanel.getValue());

        // ���t�^
        module.setBloodType(bloodTypePanel.getValue());

        // ������
        module.setInfection(infectionPanel.getValue());
        
        savedStamp.setModel(module);
        
        return (Object)savedStamp;

        //return module.isValidMML() ? (Object)module : null;
    }
    
    class AllergyPanel extends JPanel {

        // �A�����M�[�e�[�u���̃��f��
        private AbstractTableModel allergyModel;

        // �A�����M�[�e�[�u��
        private JTable allergyTable;

        // �������x�i�J�����P�j�ɐݒ肷��R���{�{�b�N�X
        private JComboBox severityCombo;

        public AllergyPanel() {
            
            super(new BorderLayout(1,1));

            allergyTable = createAllergyTable();

            // �J�����w�b�_�[��\������
            add(allergyTable.getTableHeader(), BorderLayout.NORTH);
            add(allergyTable, BorderLayout.CENTER);
        }

        protected Allergy[] getValue() {
            
            Allergy[] allergies = null;
            ArrayList list = null;
            Allergy allergy = null;

            // �A�����M�e�[�u�����X�L��������
            int rowCount = allergyTable.getRowCount();
            String data = null;

            for (int i = 0; i < rowCount ; i++) {

                data = (String) allergyTable.getValueAt(i, 0);

                if (! data.equals ("")) {
                	
					allergy = new Allergy();

                    // Factor
					allergy.setFactor(data);

                    // severity
                    data = (String) allergyTable.getValueAt(i, 1);
                    if (! data.equals("")) {
						allergy.setSeverity(data);
                    }

                    // identifiedDate
                    data = (String) allergyTable.getValueAt(i, 2);
                    if (! data.equals("")) {
						allergy.setIdentifiedDate(data);
                    }

                    // memo
                    data = (String) allergyTable.getValueAt(i, 3);
                    if (! data.equals("")) {
						allergy.setMemo(data);
                    }

                    if (list == null) {

                        list = new ArrayList();
                    }
                    list.add(allergy);
                }
            }
            
            if (list != null && list.size() > 0) {
            	int cnt = list.size();
				allergies = new Allergy[cnt];
				for (int i = 0; i < cnt; i++) {
					allergies[i] = (Allergy)list.get(i);
				}
            }
            
            return allergies;
        }

        protected void setValue(Allergy[] allergies) {

            if (allergies == null) {
                return;
            }

            int count = allergies.length;
            Allergy item;
            String data;
            int col;

            for (int i = 0; i < count; i++) {

                item = allergies[i];

                col = 0;
                allergyModel.setValueAt(item.getFactor(), i, col);

                col++;
                allergyModel.setValueAt(item.getSeverity(), i, col);

                col++;
                allergyModel.setValueAt(item.getIdentifiedDate(), i, col);

                col++;
                allergyModel.setValueAt(item.getMemo(), i, col);
            }
        }

       /**
        * �A�����M�[�e�[�u���𐶐����ĕԂ�
        */
        private JTable createAllergyTable() {

          allergyModel = new AbstractTableModel() {

             Object[][] data = {
                { "","","",""},
                { "","","",""},
                { "","","",""},
                { "","","",""},
                { "","","",""},
                { "","","",""},
                { "","","",""},
                { "","","",""}
             };

             public int getRowCount () {
                return data.length;
             }

             public int getColumnCount () {
                return data[0].length;
             }

             public Object getValueAt (int row, int col) {
                return data[row][col];
             }

             public String getColumnName(int col) {
                return allergyColumnNames[col];
             }

             public Class getColumnClass(int col) {
                return java.lang.String.class;
             }

             public boolean isCellEditable(int row, int col) {
                return true;
             }

             public void setValueAt(Object value, int row, int col) {
                 if (value == null || ((String)value).trim().equals("")) {
                     return;
                 }
                 data[row][col] = value;
                 fireTableCellUpdated(row, col);
                 
                 if (col == FACTOR_COLUMN) {
                     String date = MMLDate.getDate();
                     data[row][IDENTIFIED_COLUMN] = date;
                     fireTableCellUpdated(row, IDENTIFIED_COLUMN);
                     boolean newOk =  (! data[0][0].equals("")) ? true : false;
                     if (newOk != okState) {
                         okState = newOk;
                         setValidModel(okState);
                     }
                 }
             }
          };

          // ���f���������Ƀe�[�u���𐶐�����
          JTable table = new JTable(allergyModel);
          table.setSurrendersFocusOnKeystroke(true);
          
          // Editor for IME-ON/OFFfields
          IMECellEditor imeOn = new IMECellEditor (new JTextField(), 1, true);
          IMECellEditor imeOff = new IMECellEditor (new JTextField(), 1, false);
          
          // Factor column
          TableColumn column = table.getColumnModel().getColumn(FACTOR_COLUMN);
          column.setCellEditor(imeOn);

          // �������x�� �� comboBox ���͂�ݒ肷��
          severityCombo = new JComboBox(severityList);
          column = table.getColumnModel().getColumn(SEVERITY_COLUMN);
          column.setCellEditor (new DefaultCellEditor(severityCombo));
          
          // Identified date column
          column = table.getColumnModel().getColumn(IDENTIFIED_COLUMN);
          column.setCellEditor (imeOff);
          
          // Memo column
          column = table.getColumnModel().getColumn(MEMO_COLUMN);
          column.setCellEditor (imeOn);

          return table;
       }    
    }
    
    class BloodtypePanel extends JPanel {

       private JComboBox aboBloodtype;
       
       private JComboBox rhBloodtype;

       private JTextField ��loodtypeMemo;
       
       /**
        * �f�t�H���g�R���X�g���N�^
        */
       public BloodtypePanel () {

          setLayout(new BoxLayout (this, BoxLayout.X_AXIS));

          Dimension dim = new Dimension(200, 21);
          
          // ABO
          add(new JLabel("ABO: "));
          aboBloodtype = new JComboBox(new String[]{"","A", "B", "O", "AB"});
          aboBloodtype.addItemListener(new ItemListener() {
              
              public void itemStateChanged(ItemEvent e) {
                  if (e.getStateChange() == ItemEvent.SELECTED) {
                      int index = aboBloodtype.getSelectedIndex();
                      boolean newOk = index > 0 ? true : false;
                      if (newOk != okState) {
                          okState = newOk;
                          setValidModel(okState);
                      }
                  }
              }
          });
          add(aboBloodtype);

          // space
          add(Box.createHorizontalStrut(11));

          // RH
          add(new JLabel("RH: "));
          rhBloodtype = new JComboBox(new String[]{"","rhD+", "rhD-"});
          add(rhBloodtype);
          
          add(Box.createHorizontalStrut(11));
          
          add(new JLabel("���t�^����: "));
          ��loodtypeMemo = new JTextField();
          ��loodtypeMemo.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent event) {
               ��loodtypeMemo.getInputContext().setCharacterSubsets(new Character.Subset[] {InputSubset.KANJI});
            }
            public void focusLosted(FocusEvent event) {
               ��loodtypeMemo.getInputContext().setCharacterSubsets(null);
            }
          });
          ��loodtypeMemo.setPreferredSize(dim);
          ��loodtypeMemo.setMinimumSize(dim);
          ��loodtypeMemo.setMaximumSize(dim);
          add(��loodtypeMemo);
       }

       /**
        * ���t�f�[�^���擾���ĕԂ�
        */
       protected BloodType getValue() {

          BloodType bloodType = null;

          String data = (String)aboBloodtype.getSelectedItem();

          if (! data.equals("")) {

             bloodType = new BloodType();

             bloodType.setAbo(data);

             data = (String)rhBloodtype.getSelectedItem();

             if (! data.equals(""))
                bloodType.setRhod(data);
             
             String memo = ��loodtypeMemo.getText().trim();
             if (! memo.equals("")) {
                bloodType.setMemo(memo);
             }
          }
          return bloodType;
       }

       /**
        * ���t�^�f�[�^���R���{�{�b�N�X�ɐݒ肷��
        */
       protected void setValue(BloodType bloodType) {

          if (bloodType == null) {
             return;
          }

          String data = bloodType.getAbo();
          aboBloodtype.setSelectedItem(data);

          data = bloodType.getRhod();
          rhBloodtype.setSelectedItem(data);
       }
    }    
    
    class InfectionPanel extends JPanel {

        // �����ǃe�[�u���̃��f��
        private AbstractTableModel infectionModel;

        // �����ǃe�[�u��
        private JTable infectionTable;  

        /**
         * �f�t�H���g�R���X�g���N�^
         */
        public InfectionPanel() {

          super(new BorderLayout(1, 1));

          infectionTable = createInfectionTable();

          // �J�����w�b�_�[��\������
          add(infectionTable.getTableHeader(), BorderLayout.NORTH);
          add(infectionTable, BorderLayout.CENTER);
        }

        /**
         * �����ǃf�[�^���e�[�u������擾���ĕԂ�
         */
        protected Infection[] getValue() {

          Infection[] infections = null;
          Infection item = null;
          ArrayList list = null;

          // �����ǃe�[�u�����X�L��������
          int rowCount = infectionTable.getRowCount();
          String data = null;

          for (int i = 0; i < rowCount ; i++) {

             // Factor
             data = (String)infectionTable.getValueAt(i, 0); 

             if (data != null && ! data.equals("")) {

                item = new Infection();
                item.setFactor(data);

                // examValue
                data =  (String) infectionTable.getValueAt(i, 1);
                if (data != null && ! data.equals("")) {
                   item.setExamValue(data);
                }

                // identifiedDate
                data =  (String)infectionTable.getValueAt(i, 2);
                if (data != null && ! data.equals("")) {
                   item.setIdentifiedDate(data);
                }

                // memo
                data =  (String)infectionTable.getValueAt(i, 3);
                if (data != null && ! data.equals("")) {
                   item.setMemo(data);
                }

                if (list == null) {
                   list = new ArrayList();
                }

                list.add(item);
             }
          }
          
		  if (list != null && list.size() > 0) {
				int cnt = list.size();
				infections = new Infection[cnt];
				for (int i = 0; i < cnt; i++) {
					infections[i] = (Infection)list.get(i);
				}
			}
            
			return infections;
        }
        

        /**
         * �����ǃf�[�^���e�[�u���ɕ\������
         */
        protected void setValue(Infection[] infections) {

          if (infections == null) {
             return;
          }

          int count = infections.length;
          Infection item = null;
          String data = null;
          int col = 0;

          for (int i = 0; i < count; i++) {

             item = (Infection)infections[i];

             col = 0;
             data = item.getFactor();
             if (data != null) {
                infectionModel.setValueAt(data, i, col);
             }

             col++;
             data = item.getExamValue();
             if ( data != null) {
                infectionModel.setValueAt(data, i, col);
             }

             col++;
             data = item.getIdentifiedDate();
             if (data != null) {
                infectionModel.setValueAt(data, i, col);
             }

             col++;
             data = item.getMemo();
             infectionModel.setValueAt(data, i, col);
          }
        }

        /**
         * �����ǃe�[�u���𐶐����ĕԂ�
         */
        private JTable createInfectionTable() {

          infectionModel = new AbstractTableModel() {

             Object[][] data = {
                { "","","",""},
                { "","","",""},
                { "","","",""},
                { "","","",""},
                { "","","",""},
                { "","","",""},
                { "","","",""},
                { "","","",""}
             };

             public int getRowCount() {
                return data.length;
             }

             public int getColumnCount() {
                return data[0].length;
             }

             public Object getValueAt(int row, int col) {
                return data[row][col];
             }

             public String getColumnName(int col) {
                return infectionColumnNames[col];
             }

             public Class getColumnClass(int col) {
                return java.lang.String.class;
             }

             public boolean isCellEditable(int row, int col) {
                return true;
             }

             public void setValueAt(Object value, int row, int col) {
                 if (value == null || ((String)value).trim().equals("")) {
                     return;
                 }
                data[row][col] = value;
                fireTableCellUpdated(row, col);
                if (col == FACTOR_COLUMN) {
                     String date = MMLDate.getDate();
                     data[row][IDENTIFIED_COLUMN] = date;
                     fireTableCellUpdated(row, IDENTIFIED_COLUMN);
                     boolean newOk =  (! data[0][0].equals("")) ? true : false;
                     if (newOk != okState) {
                         okState = newOk;
                         setValidModel(okState);
                     }
                 }
             }
          };
          
          JTable table = new JTable(infectionModel);
          table.setSurrendersFocusOnKeystroke(true);
          
          // Editor for IME-ON/OFFfields
          IMECellEditor imeOn = new IMECellEditor (new JTextField(), 1, true);
          IMECellEditor imeOff = new IMECellEditor (new JTextField(), 1, false);
          
          // Factor column
          TableColumn column = table.getColumnModel().getColumn(FACTOR_COLUMN);
          column.setCellEditor(imeOn);
          
          // Exam value column
          column = table.getColumnModel().getColumn(EXAM_VALUE_COLUMN);
          column.setCellEditor(imeOff);
          
          // Identified date column
          column = table.getColumnModel().getColumn(IDENTIFIED_COLUMN);
          column.setCellEditor(imeOff);
          
          // Memo column
          column = table.getColumnModel().getColumn(MEMO_COLUMN);
          column.setCellEditor(imeOn);
          
          return table;
        }
    }
}