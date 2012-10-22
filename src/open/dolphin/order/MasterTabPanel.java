/*
 * MasterTabPanel.java
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
import javax.swing.table.*;
import javax.swing.event.*;

import open.dolphin.client.*;
import open.dolphin.dao.*;
import open.dolphin.infomodel.DiseaseEntry;
import open.dolphin.infomodel.MedicineEntry;
import open.dolphin.infomodel.ToolMaterialEntry;
import open.dolphin.infomodel.TreatmentEntry;
import open.dolphin.table.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.util.prefs.*;
import java.awt.im.InputSubset;

/**
 * TabbedPane contains master serach panels.
 *
 * @author  Kazushi Minagawa, Digital Globe, INc. 
 */
public class MasterTabPanel extends JPanel{
        
    private static final int DIGNOSIS_INDEX         = 0;
    private static final int MEDICAL_SUPPLY_INDEX   = 1;
    private static final int INJECTION_INDEX        = 2;
    private static final int TOOL_MATERIAL_INDEX    = 3;
    private static final int MEDICAL_TRAET_INDEX    = 4;
    private static final String SELECTED_ITEM_PROP  = "selectedItemProp";
        
    private final Color[] masterColors = ClientContext.getColorArray("masterSearch.masterColors");
    private final String[] masterNames = ClientContext.getStringArray("masterSearch.masterNames");
    private final String[] masterTabNames = ClientContext.getStringArray("masterSearch.masterTabNames");
    
    private final int START_NUM_ROWS    = 20;
    
    private final String keywordBorderTitle = ClientContext.getString("masterSearch.text.keywordBorderTitle");
    private final String countLabelText = ClientContext.getString("masterSearch.text.countLabel");
    private final String underSearchMsg = ClientContext.getString("search.status.underSearchMsg");
    private final String endSearchMsg = ClientContext.getString("search.status.endSearchMsg");
    
    private JTabbedPane tabbedPane;
    
    private Diagnosis diagnosis;                // �a��
    
    private MedicalSupplies medicalSupplies;    // ���i�i�������˖�j
    
    private ToolMaterial toolMaterial;          // ���
    
    private Treatment treatment;                // �f�Ís��
    
    private InjectionMedicine injection;        // ���˖�
    
    private Preferences prefs = ClientContext.getPreferences();
        

    /** Creates new MasterTabPanel */
    public MasterTabPanel() {
        
        super(new BorderLayout());
                        
        diagnosis = new Diagnosis(masterNames[0]);
        medicalSupplies = new MedicalSupplies(masterNames[1]);
        injection = new InjectionMedicine(masterNames[2]);
        toolMaterial = new ToolMaterial(masterNames[3]);
        treatment = new Treatment(masterNames[4]);
        
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(masterTabNames[0], diagnosis);
        tabbedPane.addTab(masterTabNames[1], medicalSupplies);
        tabbedPane.addTab(masterTabNames[2], injection);
        tabbedPane.addTab(masterTabNames[3], toolMaterial);
        tabbedPane.addTab(masterTabNames[4], treatment);
        
        tabbedPane.addChangeListener(new ChangeListener() {
            
            public void stateChanged(ChangeEvent e) {
                int index = tabbedPane.getSelectedIndex();
                MasterPanel mp = (MasterPanel)tabbedPane.getComponentAt(index);
                mp.enter();
            }
        });
        
        this.add(tabbedPane);
        
    }
        
    /**
     * �I�[�_�N���X(�X�^���v�{�b�N�X�̃^�u�Ɋ֘A�Â�����Ă���ԍ�)��ݒ肷��B
     * ���̃R�[�h�����f�Ís�ׂ��}�X�^���猟������B
     * @param code �I�[�_�N���X
     */
    public void setSearchClass(String serchClass) {
        treatment.setSearchClass(serchClass);
    }
    
    public void setRadLocationEnabled(boolean b) {
        treatment.setRadLocationEnabled(b);
    }
    
    public void startDiagnosis(PropertyChangeListener l) {
        diagnosis.enter();
        diagnosis.addPropertyChangeListener(SELECTED_ITEM_PROP, l);
        
        tabbedPane.setEnabledAt(MEDICAL_SUPPLY_INDEX, false);
        tabbedPane.setEnabledAt(INJECTION_INDEX, false);
        tabbedPane.setEnabledAt(TOOL_MATERIAL_INDEX, false);
        tabbedPane.setEnabledAt(MEDICAL_TRAET_INDEX, false);
        
        tabbedPane.setEnabledAt(DIGNOSIS_INDEX, true);
        tabbedPane.setSelectedIndex(DIGNOSIS_INDEX);
    }
    
    public void stopDiagnosis(PropertyChangeListener l) {
        diagnosis.removePropertyChangeListener(SELECTED_ITEM_PROP, l);
    }
    
    public void startMedicine(PropertyChangeListener l) {
        
        // ��ށE���i�E���˖�
        medicalSupplies.addPropertyChangeListener(SELECTED_ITEM_PROP, l);
        toolMaterial.addPropertyChangeListener(SELECTED_ITEM_PROP, l);
        injection.addPropertyChangeListener(SELECTED_ITEM_PROP, l);
        
        tabbedPane.setEnabledAt(DIGNOSIS_INDEX, false);
        tabbedPane.setEnabledAt(MEDICAL_SUPPLY_INDEX, true);
        tabbedPane.setEnabledAt(INJECTION_INDEX, true);
        tabbedPane.setEnabledAt(TOOL_MATERIAL_INDEX, true);
        tabbedPane.setEnabledAt(MEDICAL_TRAET_INDEX, false);
                
        tabbedPane.setSelectedIndex(MEDICAL_SUPPLY_INDEX);
    }
    
    public void stopMedicine(PropertyChangeListener l) {
        medicalSupplies.removePropertyChangeListener(SELECTED_ITEM_PROP, l);
        toolMaterial.removePropertyChangeListener(SELECTED_ITEM_PROP, l);
        injection.removePropertyChangeListener(SELECTED_ITEM_PROP, l);
    }
    
    public void startInjection(PropertyChangeListener l) {
        
        // ��ށE�f�Ís�ׁE���˖�
        toolMaterial.addPropertyChangeListener(SELECTED_ITEM_PROP, l);
        treatment.addPropertyChangeListener(SELECTED_ITEM_PROP, l);
        injection.addPropertyChangeListener(SELECTED_ITEM_PROP, l);
        
        tabbedPane.setEnabledAt(DIGNOSIS_INDEX, false);
        tabbedPane.setEnabledAt(MEDICAL_SUPPLY_INDEX, false);
        tabbedPane.setEnabledAt(INJECTION_INDEX, true);
        tabbedPane.setEnabledAt(TOOL_MATERIAL_INDEX, true);
        tabbedPane.setEnabledAt(MEDICAL_TRAET_INDEX, true);
                
        tabbedPane.setSelectedIndex(MEDICAL_TRAET_INDEX);
    }
    
    public void stopInjection(PropertyChangeListener l) {
        toolMaterial.removePropertyChangeListener(SELECTED_ITEM_PROP, l);
        treatment.removePropertyChangeListener(SELECTED_ITEM_PROP, l);
        injection.removePropertyChangeListener(SELECTED_ITEM_PROP, l);
    }    
    
    public void startCharge(PropertyChangeListener l) {
        
        // �f�Ís�אݒ�
        treatment.addPropertyChangeListener(SELECTED_ITEM_PROP, l);
      
        tabbedPane.setEnabledAt(DIGNOSIS_INDEX, false);
        tabbedPane.setEnabledAt(MEDICAL_SUPPLY_INDEX, false);
        tabbedPane.setEnabledAt(INJECTION_INDEX, false);
        tabbedPane.setEnabledAt(TOOL_MATERIAL_INDEX, false);
        tabbedPane.setEnabledAt(MEDICAL_TRAET_INDEX, true);
        
        tabbedPane.setSelectedIndex(MEDICAL_TRAET_INDEX);
    }
    
    public void stopCharge(PropertyChangeListener l) {
        treatment.removePropertyChangeListener(SELECTED_ITEM_PROP, l);
    }
    
    public void startTest(PropertyChangeListener l) {
        
        // �f�Ís�ׁE��ށE���i�E���˖�
        medicalSupplies.addPropertyChangeListener(SELECTED_ITEM_PROP, l);
        injection.addPropertyChangeListener(SELECTED_ITEM_PROP, l);
        toolMaterial.addPropertyChangeListener(SELECTED_ITEM_PROP, l);
        treatment.addPropertyChangeListener(SELECTED_ITEM_PROP, l);
        
        tabbedPane.setEnabledAt(DIGNOSIS_INDEX, false);
        tabbedPane.setEnabledAt(TOOL_MATERIAL_INDEX, true);
        tabbedPane.setEnabledAt(MEDICAL_TRAET_INDEX, true);
        tabbedPane.setEnabledAt(MEDICAL_SUPPLY_INDEX, true);
        tabbedPane.setEnabledAt(INJECTION_INDEX, true);
        
        tabbedPane.setSelectedIndex(MEDICAL_TRAET_INDEX);
    }

    public void stopTest(PropertyChangeListener l) {
        medicalSupplies.removePropertyChangeListener(SELECTED_ITEM_PROP, l);
        toolMaterial.removePropertyChangeListener(SELECTED_ITEM_PROP, l);
        treatment.removePropertyChangeListener(SELECTED_ITEM_PROP, l);
        injection.removePropertyChangeListener(SELECTED_ITEM_PROP, l);
    }
    
    public void startGeneral(PropertyChangeListener l) {
        
        // �ėp����
        //treatment.setGeneralSearch();
        treatment.setSearchClass(null);
        
        // �f�Ís�ׁE��ށE���i�E���˖�
        medicalSupplies.addPropertyChangeListener(SELECTED_ITEM_PROP, l);
        injection.addPropertyChangeListener(SELECTED_ITEM_PROP, l);
        toolMaterial.addPropertyChangeListener(SELECTED_ITEM_PROP, l);
        treatment.addPropertyChangeListener(SELECTED_ITEM_PROP, l);
        
        tabbedPane.setEnabledAt(DIGNOSIS_INDEX, false);
        tabbedPane.setEnabledAt(TOOL_MATERIAL_INDEX, true);
        tabbedPane.setEnabledAt(MEDICAL_TRAET_INDEX, true);
        tabbedPane.setEnabledAt(MEDICAL_SUPPLY_INDEX, true);
        tabbedPane.setEnabledAt(INJECTION_INDEX, true);
        
        tabbedPane.setSelectedIndex(MEDICAL_TRAET_INDEX);
    }

    public void stopGeneral(PropertyChangeListener l) {
        medicalSupplies.removePropertyChangeListener(SELECTED_ITEM_PROP, l);
        toolMaterial.removePropertyChangeListener(SELECTED_ITEM_PROP, l);
        treatment.removePropertyChangeListener(SELECTED_ITEM_PROP, l);
        injection.removePropertyChangeListener(SELECTED_ITEM_PROP, l);
    }  
        
    /**
     * �a���}�X�^�̌����N���X�B
     */
    protected class Diagnosis extends MasterPanel {
        
        private final String[] diseaseColumns = ClientContext.getStringArray("masterSearch.disease.columnNames");
        private final String codeSystem = ClientContext.getString("mml.codeSystem.diseaseMaster");
        private final String[] sortButtonNames = ClientContext.getStringArray("masterSearch.disease.sortButtonNames");
        private final String[] sortColumnNames = ClientContext.getStringArray("masterSearch.disease.sortColumnNames");
                
        public Diagnosis(String master) {
            
            super(master);
            
            ButtonGroup bg = new ButtonGroup();
            sortButtons = new JRadioButton[sortButtonNames.length];
            for (int i = 0; i < sortButtonNames.length; i++) {
                JRadioButton radio = new JRadioButton(sortButtonNames[i]);
                sortButtons[i] = radio;
                bg.add(radio);
                radio.addActionListener(new SortActionListener(this, sortColumnNames[i], i));
            }
            
            int index = prefs.getInt("masterSearch.disease.sort", 0);
            sortButtons[index].setSelected(true);
            setSortBy(sortColumnNames[index]);
                                    
            model = new ObjectTableModel(diseaseColumns, START_NUM_ROWS) {
                        
                public Class getColumnClass(int col) {
                    return DiseaseEntry.class;
                }
            };
            
            // Table �𐶐�����
            table = new JTable(model);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            // �s�N���b�N������o�^����
            ListSelectionModel m = table.getSelectionModel();
            m.addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting() == false) {
                        int row = table.getSelectedRow();
                        DiseaseEntry o = (DiseaseEntry)model.getObject(row);
                        if (o != null) {
                            // Event adapter
                            MasterItem mItem = new MasterItem();
                            mItem.classCode = 0;
                            mItem.code = o.getCode();
                            mItem.name = o.getName();
                            mItem.claimDiseaseCode = mItem.code;
                            mItem.masterTableId = codeSystem;
                            fireSelectedRow(mItem);
                        }
                    }
                }
            });
            
            // �s�I�����\�ɂ���
            table.setRowSelectionAllowed(true);
            
            // �J��������ݒ肷��
            TableColumn column = null;
            int[] width = new int[]{50, 200, 200, 40, 50};
            int len = width.length;
            for (int i = 0; i < len; i++) {
                column = table.getColumnModel().getColumn(i);
                column.setPreferredWidth(width[i]);
            }
     
            // �����_����o�^����
            DiseaseMasterRenderer dr = new DiseaseMasterRenderer();
            dr.setBeforStartColor(masterColors[0]);
            dr.setInUseColor(masterColors[1]);
            dr.setAfterEndColor(masterColors[2]);
            table.setDefaultRenderer(DiseaseEntry.class, dr);

            // Layout
            // Keyword
            JPanel key = new JPanel();
            key.setLayout(new BoxLayout(key, BoxLayout.X_AXIS));
            key.add(new JLabel(masterTabNames[0] + ":"));
            key.add(Box.createHorizontalStrut(7));
            key.add(keywordField);
            key.add(Box.createHorizontalStrut(11));
            key.add(startsWith);
            key.add(Box.createHorizontalStrut(5));
            key.add(contains);
            key.add(Box.createHorizontalStrut(7));
            key.add(searchButton);
            key.setBorder(BorderFactory.createTitledBorder(keywordBorderTitle));
            
            JPanel sort = new JPanel();
            sort.setLayout(new BoxLayout(sort, BoxLayout.X_AXIS));
            for (int i = 0; i < sortButtons.length; i++) {
                if ( i != 0) {
                    sort.add(Box.createHorizontalStrut(5));
                }
                sort.add(sortButtons[i]);
            }
            sort.setBorder(BorderFactory.createTitledBorder("�\�[�g"));
            
            JPanel top = new JPanel();
            top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
            top.add(key);
            top.add(Box.createHorizontalGlue());
            top.add(sort);
            
            // Table
            JScrollPane scroller = new JScrollPane(table, 
                                       JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                       JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            // Command
            JPanel bottom = new JPanel();
            bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
            bottom.add(new JLabel(countLabelText));
            bottom.add(Box.createHorizontalStrut(7));
            bottom.add(countLabel);
            bottom.add(Box.createHorizontalStrut(17));
            bottom.add(statusLabel);
            bottom.add(Box.createHorizontalStrut(11));
            bottom.add(progressBar);
            bottom.add(Box.createHorizontalGlue());
            
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(top);
            this.add(Box.createVerticalStrut(5));
            this.add(scroller);
            this.add(Box.createVerticalStrut(11));
            this.add(bottom);
        }
    } 
    
    /**
     * �a���}�X�^ Table �̃����_���[
     */
    protected final class DiseaseMasterRenderer extends MasterRenderer {
        
        private final int CODE_COLUMN       = 0;
        private final int NAME_COLUMN       = 1;
        private final int KANA_COLUMN       = 2;
        private final int ICD10_COLUMN      = 3;
        private final int DISUSES_COLUMN    = 4;
                
        public DiseaseMasterRenderer() {
        }
        
        public Component getTableCellRendererComponent(
                                JTable table,
                                Object value,
                                boolean isSelected,
                                boolean isFocused,
                                int row, int col) {
            Component c = super.getTableCellRendererComponent(
                                             table, 
                                             value,
                                             isSelected,
                                             isFocused, 
                                             row, col);
            JLabel label = (JLabel)c;
            
            if (value != null && value instanceof DiseaseEntry) {
                
                DiseaseEntry entry = (DiseaseEntry)value;
               
                String disUseDate = entry.getDisUseDate();
                
                setColor(label, disUseDate);
                
                 switch(col) {

                    case CODE_COLUMN:
                        label.setText(entry.getCode());
                        break;

                    case NAME_COLUMN:
                        label.setText(entry.getName());
                        break;

                    case KANA_COLUMN:
                        label.setText(entry.getKana());
                        break;                            

                    case ICD10_COLUMN:
                        label.setText(entry.getIcdTen());
                        break;

                    case DISUSES_COLUMN:
                        if (disUseDate.startsWith("9")) {
                            label.setText("");
                        } else {
                            label.setText(disUseDate);
                        }
                        break;    
                }
                                    
            } else {
                label.setBackground(Color.white);
                label.setText(value == null ? "" : value.toString());
            }
            return c;
        }
    }        
    
    /**
     * �����}�X�^�̌����N���X�B
     */
    protected class MedicalSupplies extends MasterPanel {
        
        private final String[] medicineColumns = ClientContext.getStringArray("masterSearch.medicine.columnNames");
        private final String medicineFlag = ClientContext.getString("masterSearch.medicine.medicineFlag");
        private final String[] costFlags = ClientContext.getStringArray("masterSearch.medicine.costFlags");
        private final String[] sortButtonNames = ClientContext.getStringArray("masterSearch.medicine.sortButtonNames");
        private final String[] sortColumnNames = ClientContext.getStringArray("masterSearch.medicine.sortColumnNames");
                                
        public MedicalSupplies(String master) {
            
            super(master);
            searchClass = medicineFlag;
            
            ButtonGroup bg = new ButtonGroup();
            sortButtons = new JRadioButton[sortButtonNames.length];
            for (int i = 0; i < sortButtonNames.length; i++) {
                JRadioButton radio = new JRadioButton(sortButtonNames[i]);
                sortButtons[i] = radio;
                bg.add(radio);
                radio.addActionListener(new SortActionListener(this, sortColumnNames[i], i));
            }
            
            int index = prefs.getInt("masterSearch.medicine.sort", 0);
            sortButtons[index].setSelected(true);
            setSortBy(sortColumnNames[index]);
                        
            /*adminButton = new JButton("�p�@");
            adminButton.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                    getAdmin();
                }
            });
            
            adminComment = new JButton("�p�@�R�����g");
            adminComment.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                    getAdminComment();
                }
            });*/
                    
            // Table Model �𐶐�����
            model = new ObjectTableModel(medicineColumns, START_NUM_ROWS) {
                
                public Class getColumnClass(int col) {
                    return MedicineEntry.class;
                }
            };
            
            // Table �𐶐�����
            table = new JTable(model);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            // �s�N���b�N������o�^����
            table.setRowSelectionAllowed(true);
            ListSelectionModel m = table.getSelectionModel();
            m.addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting() == false) {
                        int row = table.getSelectedRow();
                        MedicineEntry o = (MedicineEntry)model.getObject(row);
                        if (o != null) {
                            // Event adpter
                            MasterItem mItem = new MasterItem();
                            mItem.classCode = 2;
                            mItem.code = o.getCode();
                            mItem.name = o.getName();
                            mItem.unit = o.getUnit();
                            fireSelectedRow(mItem);
                        }
                    }
                }
            });
            
            // �񕝂�ݒ肷��
            TableColumn column = null;
            int[] width = new int[]{50, 200, 200, 40, 30, 50, 50, 50};
            int len = width.length;
            for (int i = 0; i < len; i++) {
                column = table.getColumnModel().getColumn(i);
                column.setPreferredWidth(width[i]);
            }
     
            // �����_���[��ݒ肷��
            MedicineMasterRenderer mr = new MedicineMasterRenderer();
            mr.setBeforStartColor(masterColors[0]);
            mr.setInUseColor(masterColors[1]);
            mr.setAfterEndColor(masterColors[2]);
            mr.setCostFlag(costFlags);
            table.setDefaultRenderer(MedicineEntry.class, mr);

            // Layout
            // Keyword
            JPanel key = new JPanel();
            key.setLayout(new BoxLayout(key, BoxLayout.X_AXIS));
            key.add(new JLabel(masterTabNames[1] + ":"));
            key.add(Box.createHorizontalStrut(7));
            key.add(keywordField);
            key.add(Box.createHorizontalStrut(11));
            key.add(startsWith);
            key.add(Box.createHorizontalStrut(5));
            key.add(contains);
            key.add(Box.createHorizontalStrut(7));
            key.add(searchButton);
            key.setBorder(BorderFactory.createTitledBorder(keywordBorderTitle));
            
            JPanel sort = new JPanel();
            sort.setLayout(new BoxLayout(sort, BoxLayout.X_AXIS));
            for (int i = 0; i < sortButtons.length; i++) {
                if ( i != 0) {
                    sort.add(Box.createHorizontalStrut(5));
                }
                sort.add(sortButtons[i]);
            }
            sort.setBorder(BorderFactory.createTitledBorder("�\�[�g"));
            
            JPanel top = new JPanel();
            top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
            top.add(key);
            top.add(Box.createHorizontalGlue());
            top.add(sort);
            
            //top.add(adminButton);
            //top.add(Box.createHorizontalStrut(5));
            //top.add(adminComment);
            // Table
            JScrollPane scroller = new JScrollPane(table, 
                                       JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                       JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            // Command
            JPanel bottom = new JPanel();
            bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
            bottom.add(new JLabel(countLabelText));
            bottom.add(Box.createHorizontalStrut(7));
            bottom.add(countLabel);
            bottom.add(Box.createHorizontalStrut(17));
            bottom.add(statusLabel);
            bottom.add(Box.createHorizontalStrut(11));
            bottom.add(progressBar);
            bottom.add(Box.createHorizontalGlue());
            
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(top);
            this.add(Box.createVerticalStrut(5));
            this.add(scroller);
            this.add(Box.createVerticalStrut(11));
            this.add(bottom);
        }     
    }
        
    /**
     * ���i�}�X�^ Table �̃����_���[
     */
    protected final class MedicineMasterRenderer extends MasterRenderer {
        
        private final int CODE_COLUMN       = 0;
        private final int NAME_COLUMN       = 1;
        private final int KANA_COLUMN       = 2;
        private final int UNIT_COLUMN       = 3;
        private final int COST_FLAG_COLUMN  = 4;
        private final int COST_COLUMN       = 5;
        private final int JNCD_COLUMN       = 6;
        private final int START_COLUMN      = 7;
        private final int END_COLUMN        = 8;
        
        private String[] costFlags;
        
        public MedicineMasterRenderer() {
        }
        
        public String[] getCostFlag() {
            return costFlags;
        }
        
        public void setCostFlag(String[] val) {
            costFlags = val;
        }
        
        public Component getTableCellRendererComponent(
                                JTable table,
                                Object value,
                                boolean isSelected,
                                boolean isFocused,
                                int row, int col) {
            Component c = super.getTableCellRendererComponent(
                                             table, 
                                             value,
                                             isSelected,
                                             isFocused, 
                                             row, col);
            JLabel label = (JLabel)c;
            
            if (value != null && value instanceof MedicineEntry) {
                
                MedicineEntry entry = (MedicineEntry)value;
                
                String startDate = entry.getStartDate();
                String endDate = entry.getEndDate();
                
                setColor(label,startDate, endDate);
                
                switch(col) {
                        
                    case CODE_COLUMN:
                        label.setText(entry.getCode());
                        break;

                    case NAME_COLUMN:
                        label.setText(entry.getName());
                        break;

                    case KANA_COLUMN:
                        label.setText(entry.getKana());
                        break;                              

                    case UNIT_COLUMN:
                        label.setText(entry.getUnit());
                        break;

                    case COST_FLAG_COLUMN:
                        try {
                            int index = Integer.parseInt(entry.getCostFlag());
                            label.setText(costFlags[index]); 
                        }
                        catch (Exception e) {
                            label.setText("");
                        }
                        break;

                    case COST_COLUMN:
                        label.setText(entry.getCost());
                        break;

                    case JNCD_COLUMN:
                        label.setText(entry.getJNCD());
                        break;     

                    case START_COLUMN:
                        if (startDate.startsWith("0")) {
                            label.setText("");
                        } else {
                            label.setText(startDate);
                        }
                        break;
                        
                    case END_COLUMN:
                        if (endDate.startsWith("9")) {
                            label.setText("");
                        } else {
                            label.setText(endDate);
                        }
                        break;    
                }
                                    
            } else {
                label.setBackground(Color.white);
                label.setText(value == null ? "" : value.toString());
            }
            return c;
        }
    }
    
    /**
     * ���˖򌟍��N���X�B
     */
    protected class InjectionMedicine extends MasterPanel {
        
        private final String[] medicineColumns = ClientContext.getStringArray("masterSearch.medicine.columnNames");
        private final String injectionFlag = ClientContext.getString("masterSearch.medicine.injectionFlag");
        private final String[] costFlags = ClientContext.getStringArray("masterSearch.medicine.costFlags");
        private final String[] sortButtonNames = ClientContext.getStringArray("masterSearch.medicine.sortButtonNames");
        private final String[] sortColumnNames = ClientContext.getStringArray("masterSearch.medicine.sortColumnNames");
                
        private JRadioButton jncdButton;
        
        public InjectionMedicine(String master) {
            
            super(master);
            searchClass = injectionFlag;
            
            ButtonGroup bg = new ButtonGroup();
            sortButtons = new JRadioButton[sortButtonNames.length];
            for (int i = 0; i < sortButtonNames.length; i++) {
                JRadioButton radio = new JRadioButton(sortButtonNames[i]);
                sortButtons[i] = radio;
                bg.add(radio);
                radio.addActionListener(new SortActionListener(this, sortColumnNames[i], i));
            }
            
            int index = prefs.getInt("masterSearch.injection.sort", 0);
            sortButtons[index].setSelected(true);
            setSortBy(sortColumnNames[index]);
                    
            // Table Model �𐶐�����
            model = new ObjectTableModel(medicineColumns, START_NUM_ROWS) {
                  
                public Class getColumnClass(int col) {
                    return MedicineEntry.class;
                }
            };
            
            // Table �𐶐�����
            table = new JTable(model);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            // �s�N���b�N������o�^����
            table.setRowSelectionAllowed(true);
            ListSelectionModel m = table.getSelectionModel();
            m.addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting() == false) {
                        int row = table.getSelectedRow();
                        MedicineEntry o = (MedicineEntry)model.getObject(row);
                        if (o != null) {
                            // Event adpter
                            MasterItem mItem = new MasterItem();
                            mItem.classCode = 2;
                            mItem.code = o.getCode();
                            mItem.name = o.getName();
                            mItem.unit = o.getUnit();
                            fireSelectedRow(mItem);
                        }
                    }
                }
            });
            
            // �񕝂�ݒ肷��
            TableColumn column = null;
            int[] width = new int[]{50, 200, 200, 40, 30, 50, 50};
            int len = width.length;
            for (int i = 0; i < len; i++) {
                column = table.getColumnModel().getColumn(i);
                column.setPreferredWidth(width[i]);
            }
     
            // �����_���[��ݒ肷��
            MedicineMasterRenderer mr = new MedicineMasterRenderer();
            mr.setBeforStartColor(masterColors[0]);
            mr.setInUseColor(masterColors[1]);
            mr.setAfterEndColor(masterColors[2]);
            mr.setCostFlag(costFlags);
            table.setDefaultRenderer(MedicineEntry.class, mr);

            // Layout            
            // Keyword
            JPanel key = new JPanel();
            key.setLayout(new BoxLayout(key, BoxLayout.X_AXIS));
            key.add(new JLabel(masterTabNames[1] + ":"));
            key.add(Box.createHorizontalStrut(7));
            key.add(keywordField);
            key.add(Box.createHorizontalStrut(11));
            key.add(startsWith);
            key.add(Box.createHorizontalStrut(5));
            key.add(contains);
            key.add(Box.createHorizontalStrut(7));
            key.add(searchButton);
            key.setBorder(BorderFactory.createTitledBorder(keywordBorderTitle));
            
            JPanel sort = new JPanel();
            sort.setLayout(new BoxLayout(sort, BoxLayout.X_AXIS));
            for (int i = 0; i < sortButtons.length; i++) {
                if ( i != 0) {
                    sort.add(Box.createHorizontalStrut(5));
                }
                sort.add(sortButtons[i]);
            }
            sort.setBorder(BorderFactory.createTitledBorder("�\�[�g"));
            
            JPanel top = new JPanel();
            top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
            top.add(key);
            top.add(Box.createHorizontalGlue());
            top.add(sort);
                        
            // Table
            JScrollPane scroller = new JScrollPane(table, 
                                       JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                       JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            // Command
            JPanel bottom = new JPanel();
            bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
            bottom.add(new JLabel(countLabelText));
            bottom.add(Box.createHorizontalStrut(7));
            bottom.add(countLabel);
            bottom.add(Box.createHorizontalStrut(17));
            bottom.add(statusLabel);
            bottom.add(Box.createHorizontalStrut(11));
            bottom.add(progressBar);
            bottom.add(Box.createHorizontalGlue());
            
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(top);
            this.add(Box.createVerticalStrut(5));
            this.add(scroller);
            this.add(Box.createVerticalStrut(11));
            this.add(bottom);            
        }
    }        
    
    /**
     * �����ރ}�X�^�̌����N���X�B
     */
    protected class ToolMaterial extends MasterPanel {
        
        private final String[] toolMaterialColumns = ClientContext.getStringArray("masterSearch.toolMaterial.columnNames");
        private final String[] toolMaterialCostFlags = ClientContext.getStringArray("masterSearch.toolMaterial.costFlags");
        private final String[] sortButtonNames = ClientContext.getStringArray("masterSearch.toolMaterial.sortButtonNames");
        private final String[] sortColumnNames = ClientContext.getStringArray("masterSearch.toolMaterial.sortColumnNames");
                
        public ToolMaterial(String master) {
            
            super(master);
            
            ButtonGroup bg = new ButtonGroup();
            sortButtons = new JRadioButton[sortButtonNames.length];
            for (int i = 0; i < sortButtonNames.length; i++) {
                JRadioButton radio = new JRadioButton(sortButtonNames[i]);
                sortButtons[i] = radio;
                bg.add(radio);
                radio.addActionListener(new SortActionListener(this, sortColumnNames[i], i));
            }
            
            int index = prefs.getInt("masterSearch.toolMaterial.sort", 0);
            sortButtons[index].setSelected(true);
            setSortBy(sortColumnNames[index]);
                               
            // Table Model �𐶐�����
            model = new ObjectTableModel(toolMaterialColumns, START_NUM_ROWS) {
                        
                public Class getColumnClass(int col) {
                    return ToolMaterialEntry.class;
                }
            };

            // Table �𐶐�����
            table = new JTable(model);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            // �s�I��������o�^����
            table.setRowSelectionAllowed(true);
            ListSelectionModel m = table.getSelectionModel();
            m.addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting() == false) {
                        int row = table.getSelectedRow();
                        ToolMaterialEntry o = (ToolMaterialEntry)model.getObject(row);
                        if (o != null) {
                            // Event adpter
                            MasterItem mItem = new MasterItem();
                            mItem.classCode = 1;
                            mItem.code = o.getCode();
                            mItem.name = o.getName();
                            mItem.unit = o.getUnit();
                            fireSelectedRow(mItem);
                        }
                    }
                }
            });
            
            // �񕝂�ݒ肷��
            TableColumn column = null;
            int[] width = new int[]{50, 200, 200, 40, 30, 50, 50};
            int len = width.length;
            for (int i = 0; i < len; i++) {
                column = table.getColumnModel().getColumn(i);
                column.setPreferredWidth(width[i]);
            } 
            
            // �J�X�^�������_���[�o�^          
            ToolMaterialMasterRenderer tmr = new ToolMaterialMasterRenderer();
            tmr.setBeforStartColor(masterColors[0]);
            tmr.setInUseColor(masterColors[1]);
            tmr.setAfterEndColor(masterColors[2]);
            tmr.setCostFlag(toolMaterialCostFlags);
            table.setDefaultRenderer(ToolMaterialEntry.class, tmr);

            // Layout
            // Keyword
            JPanel key = new JPanel();
            key.setLayout(new BoxLayout(key, BoxLayout.X_AXIS));
            key.add(new JLabel(masterTabNames[3] + ":"));
            key.add(Box.createHorizontalStrut(7));
            key.add(keywordField);
            key.add(Box.createHorizontalStrut(11));
            key.add(startsWith);
            key.add(Box.createHorizontalStrut(5));
            key.add(contains);
            key.add(Box.createHorizontalStrut(7));
            key.add(searchButton);
            key.setBorder(BorderFactory.createTitledBorder(keywordBorderTitle));
            
            JPanel sort = new JPanel();
            sort.setLayout(new BoxLayout(sort, BoxLayout.X_AXIS));
            for (int i = 0; i < sortButtons.length; i++) {
                if ( i != 0) {
                    sort.add(Box.createHorizontalStrut(5));
                }
                sort.add(sortButtons[i]);
            }
            sort.setBorder(BorderFactory.createTitledBorder("�\�[�g"));
            
            JPanel top = new JPanel();
            top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
            top.add(key);
            top.add(Box.createHorizontalGlue());
            top.add(sort);
            
            // Table
            JScrollPane scroller = new JScrollPane(table, 
                                       JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                       JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            // Command
            JPanel bottom = new JPanel();
            bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
            bottom.add(new JLabel(countLabelText));
            bottom.add(Box.createHorizontalStrut(7));
            bottom.add(countLabel);
            bottom.add(Box.createHorizontalStrut(17));
            bottom.add(statusLabel);
            bottom.add(Box.createHorizontalStrut(11));
            bottom.add(progressBar);
            bottom.add(Box.createHorizontalGlue());
            
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(top);
            this.add(Box.createVerticalStrut(5));
            this.add(scroller);
            this.add(Box.createVerticalStrut(11));
            this.add(bottom);
        }
    }
    
    /**
     * ��ރ}�X�^ Table �̃����_���[
     */
    protected final class ToolMaterialMasterRenderer extends MasterRenderer {
                
        private final int CODE_COLUMN       = 0;
        private final int NAME_COLUMN       = 1;
        private final int KANA_COLUMN       = 2;
        private final int UNIT_COLUMN       = 3;
        private final int COST_FLAG_COLUMN  = 4;
        private final int COST_COLUMN       = 5;
        private final int START_COLUMN      = 6;
        private final int END_COLUMN        = 7;

        private String[] costFlags;
        
        public ToolMaterialMasterRenderer() {
        }
        
        public String[] getCostFlag() {
            return costFlags;
        }
        
        public void setCostFlag(String[] val) {
            costFlags = val;
        }
                
        public Component getTableCellRendererComponent(
                                JTable table,
                                Object value,
                                boolean isSelected,
                                boolean isFocused,
                                int row, int col) {
            Component c = super.getTableCellRendererComponent(
                                             table, 
                                             value,
                                             isSelected,
                                             isFocused, 
                                             row, col);
            JLabel label = (JLabel)c;
            
            if (value != null && value instanceof ToolMaterialEntry) {
                
                ToolMaterialEntry entry = (ToolMaterialEntry)value;
                
                String startDate = entry.getStartDate();
                String endDate = entry.getEndDate();
                
                setColor(label,startDate, endDate);
                
                String tmp = null;
                
                switch(col) {

                    case CODE_COLUMN:
                        label.setText(entry.getCode());
                        break;

                    case NAME_COLUMN:
                        label.setText(entry.getName());
                        break;

                    case KANA_COLUMN:
                        label.setText(entry.getKana());
                        break;    
                        
                    case UNIT_COLUMN:
                        label.setText(entry.getUnit());
                        break;

                    case COST_FLAG_COLUMN:
                        tmp = entry.getCostFlag();
                        if (tmp != null) {
                            try {
                                int index = Integer.parseInt(tmp);
                                label.setText(costFlags[index]); 
                            }
                            catch (Exception e) {
                                label.setText("");
                            }
                        } else {
                            label.setText(""); 
                        }
                        break;

                    case COST_COLUMN:
                        label.setText(entry.getCost());
                        break;    

                    case START_COLUMN:
                        if (startDate.startsWith("0")) {
                            label.setText("");
                        } else {
                            label.setText(startDate);
                        }
                        break;
                        
                    case END_COLUMN:
                        if (endDate.startsWith("9")) {
                            label.setText("");
                        } else {
                            label.setText(endDate);
                        }
                        break;  
                }
                                    
            } else {
                label.setBackground(Color.white);
                label.setText(value == null ? "" : value.toString());
            }
            return c;
        }
    }            
    
    /**
     * �f�Ís�׃}�X�^�̌����N���X�B
     */
    protected class Treatment extends MasterPanel {
        
        private final String[] treatmentColumns = ClientContext.getStringArray("masterSearch.treatment.columnNames");
        private final String[] treatmentCostFlags = ClientContext.getStringArray("masterSearch.treatment.costFlags");
        private final String[] inOutFlags = ClientContext.getStringArray("masterSearch.treatment.inOutFlags");
        private final String[] hospClinicFlags= ClientContext.getStringArray("masterSearch.treatment.hospitalClinicFlags");
        private final String[] oldFlags = ClientContext.getStringArray("masterSearch.treatment.oldFlags");
        private final String[] treatmentNames = ClientContext.getStringArray("masterSearch.treatment.names");
        private final String[] treatmentCodes = ClientContext.getStringArray("masterSearch.treatment.codes");
        private final String[] sortButtonNames = ClientContext.getStringArray("masterSearch.treatment.sortButtonNames");
        private final String[] sortColumnNames = ClientContext.getStringArray("masterSearch.treatment.sortColumnNames");
                
        private JButton categoryButton;
        private JButton radLOcationButton;

        public Treatment(String master) {
            
            super(master);
            
            ButtonGroup bg = new ButtonGroup();
            sortButtons = new JRadioButton[sortButtonNames.length];
            for (int i = 0; i < sortButtonNames.length; i++) {
                JRadioButton radio = new JRadioButton(sortButtonNames[i]);
                sortButtons[i] = radio;
                bg.add(radio);
                radio.addActionListener(new SortActionListener(this, sortColumnNames[i], i));
            }
            
            int index = prefs.getInt("masterSearch.treatment.sort", 0);
            sortButtons[index].setSelected(true);
            setSortBy(sortColumnNames[index]);
            
            // �J�e�S�����X�g�A�b�v�{�^��
            categoryButton = new JButton("�J�e�S���S����");
            categoryButton.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                    getByClaimClass();
                }
            });
            
            radLOcationButton = new JButton("���ː�����");
            radLOcationButton.setEnabled(false);
            radLOcationButton.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                    getRadLocation();
                }
            });
                                            
            model = new ObjectTableModel(treatmentColumns, START_NUM_ROWS) {
                
                public Class getColumnClass(int col) {
                    return TreatmentEntry.class;
                }                        
            };
            
            // Table �𐶐�����
            table = new JTable(model);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            // �s�I��������o�^����
            table.setRowSelectionAllowed(true);
            ListSelectionModel m = table.getSelectionModel();
            m.addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting() == false) {
                        int row = table.getSelectedRow();
                        TreatmentEntry o = (TreatmentEntry)model.getObject(row);
                        if (o != null) {
                            // Event adpter
                            MasterItem mItem = new MasterItem();
                            mItem.classCode = 0;
                            mItem.code = o.getCode();
                            mItem.name = o.getName();
                            mItem.claimClassCode = o.getClaimClassCode();
                            fireSelectedRow(mItem);
                        }
                    }
                }
            });
            
            TableColumn column = null;
            int[] width = new int[]{50, 150, 150, 30, 50, 30, 30, 30, 50};
            int len = width.length;
            for (int i = 0; i < len; i++) {
                column = table.getColumnModel().getColumn(i);
                column.setPreferredWidth(width[i]);
            }
            
            TreatmentMasterRenderer tr = new TreatmentMasterRenderer();
            tr.setBeforStartColor(masterColors[0]);
            tr.setInUseColor(masterColors[1]);
            tr.setAfterEndColor(masterColors[2]);
            tr.setCostFlag(treatmentCostFlags);
            tr.setInOutFlag(inOutFlags);
            tr.setOldFlag(oldFlags);
            tr.setHospitalClinicFlag(hospClinicFlags);
            table.setDefaultRenderer(TreatmentEntry.class, tr);

            // Layout
            // Keyword
            JPanel key = new JPanel();
            key.setLayout(new BoxLayout(key, BoxLayout.X_AXIS));
            key.add(new JLabel(masterTabNames[4] + ":"));
            key.add(Box.createHorizontalStrut(7));
            key.add(keywordField);
            key.add(Box.createHorizontalStrut(11));
            key.add(startsWith);
            key.add(Box.createHorizontalStrut(5));
            key.add(contains);
            key.add(Box.createHorizontalStrut(7));
            key.add(searchButton);
            key.setBorder(BorderFactory.createTitledBorder(keywordBorderTitle));
            
            JPanel sort = new JPanel();
            sort.setLayout(new BoxLayout(sort, BoxLayout.X_AXIS));
            for (int i = 0; i < sortButtons.length; i++) {
                if ( i != 0) {
                    sort.add(Box.createHorizontalStrut(5));
                }
                sort.add(sortButtons[i]);
            }
            sort.setBorder(BorderFactory.createTitledBorder("�\�[�g"));
            
            JPanel category = new JPanel();
            category.setLayout(new BoxLayout(category, BoxLayout.X_AXIS));
            category.add(categoryButton);
            category.add(Box.createHorizontalStrut(5));
            category.add(radLOcationButton);
            category.setBorder(BorderFactory.createTitledBorder("�J�e�S��"));
            
            JPanel top = new JPanel();
            top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
            top.add(key);
            top.add(Box.createHorizontalStrut(11));
            top.add(category);
            top.add(Box.createHorizontalGlue());
            top.add(sort);
            
            // Table
            JScrollPane scroller = new JScrollPane(table, 
                                       JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                       JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            // Command
            JPanel bottom = new JPanel();
            bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
            bottom.add(new JLabel(countLabelText));
            bottom.add(Box.createHorizontalStrut(7));
            bottom.add(countLabel);
            bottom.add(Box.createHorizontalStrut(17));
            bottom.add(statusLabel);
            bottom.add(Box.createHorizontalStrut(11));
            bottom.add(progressBar);
            bottom.add(Box.createHorizontalGlue());
             
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(top);
            this.add(Box.createVerticalStrut(5));
            this.add(scroller);
            this.add(Box.createVerticalStrut(11));
            this.add(bottom);
        }
        
        public void setSearchClass(String searchClass) {
            
            this.searchClass = searchClass;
            
            if (this.searchClass == null) {
                categoryButton.setEnabled(false);
            }
        }
        
        public void setRadLocationEnabled(boolean b) {
            radLOcationButton.setEnabled(b);
        }
        
        private void getByClaimClass() {
                        
            final SqlMasterDao dao = (SqlMasterDao)SqlDaoFactory.create(this, "dao.master");

            Runnable r = new Runnable() {
                public void run() {
                    fireBusy(true);
                    ArrayList results = dao.getByClaimClass(master, searchClass, sortBy, order);
                    model.setObjectList(results);
                    int count = model.getObjectCount();
                    fireCount(count);
                    fireBusy(false);
                }
            };
            Thread t = new Thread(r);
            t.start();
        }
        
        private void getRadLocation() {
                        
            final SqlMasterDao dao = (SqlMasterDao)SqlDaoFactory.create(this, "dao.master");

            Runnable r = new Runnable() {
                public void run() {
                    fireBusy(true);
                    ArrayList results = dao.getRadLocation(master, sortBy, order);
                    model.setObjectList(results);
                    int count = model.getObjectCount();
                    fireCount(count);
                    fireBusy(false);
                }
            };
            Thread t = new Thread(r);
            t.start();
        }        
    }
    
    /**
     * �f�Ís�׃}�X�^ Table �̃����_���[
     */
    protected final class TreatmentMasterRenderer extends MasterRenderer {
        
        private final int CODE_COLUMN       = 0;
        private final int NAME_COLUMN       = 1;
        private final int KANA_COLUMN       = 2;
        private final int COST_FLAG_COLUMN  = 3;
        private final int COST_COLUMN       = 4;
        private final int INOUT_COLUMN      = 5;
        private final int OLD_COLUMN        = 6;
        private final int HOSP_CLINIC_COLUMN = 7;
        private final int START_COLUMN      = 8;
        private final int END_COLUMN      = 9;

        private String[] costFlags;
        private String[] inOutFlags;
        private String[] oldFlags;
        private String[] hospitalClinicFlags;
        
        public TreatmentMasterRenderer() {
        }
        
        public String[] getCostFlag() {
            return costFlags;
        }
        
        public void setCostFlag(String[] val) {
            costFlags = val;
        }
        
        public String[] getInOutFlag() {
            return inOutFlags;
        }
        
        public void setInOutFlag(String[] val) {
            inOutFlags = val;
        }
        
        public String[] getOldFlag() {
            return oldFlags;
        }
        
        public void setOldFlag(String[] val) {
            oldFlags = val;
        }
        
        public String[] getHospitalClinicFlag() {
            return hospitalClinicFlags;
        }
        
        public void setHospitalClinicFlag(String[] val) {
            hospitalClinicFlags = val;
        }        
        
        public Component getTableCellRendererComponent(
                                JTable table,
                                Object value,
                                boolean isSelected,
                                boolean isFocused,
                                int row, int col) {
            Component c = super.getTableCellRendererComponent(
                                             table, 
                                             value,
                                             isSelected,
                                             isFocused, 
                                             row, col);
            JLabel label = (JLabel)c;
            
            if (value != null && value instanceof TreatmentEntry) {
                
                TreatmentEntry entry = (TreatmentEntry)value;
                
                String startDate = entry.getStartDate();
                String endDate = entry.getEndDate();
                
                setColor(label,startDate, endDate);
                
                String tmp = null;
                
                switch(col) {

                    case CODE_COLUMN:
                        label.setText(entry.getCode());
                        break;

                    case NAME_COLUMN:
                        label.setText(entry.getName());
                        break;

                    case KANA_COLUMN:
                        label.setText(entry.getKana());
                        break;    

                    case COST_FLAG_COLUMN:
                        tmp = entry.getCostFlag();
                        if (tmp != null) {
                            try {
                                int index = Integer.parseInt(tmp);
                                label.setText(costFlags[index]); 
                            }
                            catch (Exception e) {
                                label.setText("");
                            }
                        } else {
                            label.setText(""); 
                        }
                        break;

                    case COST_COLUMN:
                        label.setText(entry.getCost());
                        break;    

                    case INOUT_COLUMN:
                        tmp = entry.getInOutFlag();
                        if (tmp != null) {
                            try {
                                int index = Integer.parseInt(tmp);
                                label.setText(inOutFlags[index]); 
                            }
                            catch (Exception e) {
                                label.setText("");
                            }
                        } else {
                            label.setText(""); 
                        }
                        break;

                    case OLD_COLUMN:
                        tmp = entry.getOldFlag();
                        if (tmp != null) {
                            try {
                                int index = Integer.parseInt(tmp);
                                label.setText(oldFlags[index]); 
                            }
                            catch (Exception e) {
                                label.setText("");
                            }
                        } else {
                            label.setText(""); 
                        }
                        break;

                    case HOSP_CLINIC_COLUMN:
                        tmp = entry.getHospitalClinicFlag();
                        if (tmp != null) {
                            try {
                                int index = Integer.parseInt(tmp);
                                label.setText(hospitalClinicFlags[index]); 
                            }
                            catch (Exception e) {
                                label.setText("");
                            }
                        } else {
                            label.setText(""); 
                        }
                        break;

                    case START_COLUMN:
                        if (startDate.startsWith("0")) {
                            label.setText("");
                        } else {
                            label.setText(startDate);
                        }
                        break;
                        
                    case END_COLUMN:
                        if (endDate.startsWith("9")) {
                            label.setText("");
                        } else {
                            label.setText(endDate);
                        }
                        break;  
                }
                                    
            } else {
                label.setBackground(Color.white);
                label.setText(value == null ? "" : value.toString());
            }
            return c;
        }
    }    
    
    /**
     * �}�X�^�����p�l���� root �N���X
     */
    protected class MasterPanel extends JPanel {
        
        protected final int KEYWORD_WIDTH  = 250;
        protected final int KEYWORD_HEIGHT = 21;
        protected JTextField keywordField;
        protected JButton searchButton;
        protected JRadioButton startsWith;
        protected JRadioButton contains;
        protected JRadioButton[] sortButtons;
        protected JTable table;
        protected JLabel countLabel;
        protected JLabel statusLabel;
        protected JProgressBar progressBar;
        protected ObjectTableModel model;
        
        protected String master;
        protected String searchClass;
        protected String sortBy;
        protected String order;
        
        protected PropertyChangeSupport boundSupport;
        
        public MasterPanel(final String master) {    
            
            this.master = master;
            
            this.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
            
            // �L�[���[�h�t�B�[���h
            keywordField = new JTextField();
            Dimension d = new Dimension(KEYWORD_WIDTH, KEYWORD_HEIGHT);
            keywordField.setPreferredSize(d);
            keywordField.setMaximumSize(d);
            keywordField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    searchButton.doClick();
                }
            });
            
            keywordField.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent event) {
                   keywordField.getInputContext().setCharacterSubsets(new Character.Subset[] {InputSubset.KANJI});
                }
                public void focusLosted(FocusEvent event) {
                   keywordField.getInputContext().setCharacterSubsets(null);
                }
            });

            keywordField.getDocument().addDocumentListener(new DocumentListener() {

                public void insertUpdate(DocumentEvent e) {
                    boolean b = (! keywordField.getText().trim().equals("")) ? true : false;
                    searchButton.setEnabled(b);
                }

                public void removeUpdate(DocumentEvent e) {
                    boolean b = (! keywordField.getText().trim().equals("")) ? true : false;
                    searchButton.setEnabled(b);     
                }  

                public void changedUpdate(DocumentEvent e) {
                } 
            });

            // �����p�^�[��
            String[] matchButtonText = ClientContext.getStringArray("masterSearch.text.matchBbttons");
            startsWith = new JRadioButton(matchButtonText[0]);
            contains = new JRadioButton(matchButtonText[1]);
            ButtonGroup bg = new ButtonGroup();
            bg.add(startsWith);
            bg.add(contains);
            boolean bStartsWith = prefs.getBoolean(master + "keywordSearch.startsWith", false);
            startsWith.setSelected(bStartsWith);
            contains.setSelected(! bStartsWith);
            
            ActionListener al = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    boolean b = startsWith.isSelected();
                    prefs.putBoolean(master + "keywordSearch.startsWith", b);
                }
            };
            startsWith.addActionListener(al);
            contains.addActionListener(al);

            // �����{�^��
            String searchButtonText = ClientContext.getString("search.button.text");
            String searchButtonImage = ClientContext.getString("search.button.image");
            searchButton = new JButton(searchButtonText, ClientContext.getImageIcon(searchButtonImage));
            searchButton.setEnabled(false);
            setMnemonic(searchButton);
            searchButton.addActionListener(new ActionListener() {

                // �{�^���N���b�N�Ń}�X�^�������s��
                public void actionPerformed(ActionEvent e) {

                    // �L���[�h�������擾����
                    String text = keywordField.getText().trim();
                    if (! text.equals("")) {
                        getByName(text);
                    }
                }
            });
            
            // ���� Label
            countLabel = new JLabel();
            
            // Status label
            statusLabel = new JLabel();
            
            // Progress Bar
            progressBar = new JProgressBar(0, 100);
            progressBar.setBorderPainted(false);
        }
        
        public void addPropertyChangeListener(String prop,PropertyChangeListener l) {
            if ( boundSupport == null) {
                boundSupport = new PropertyChangeSupport(this);
            }
            boundSupport.addPropertyChangeListener(prop, l);
        }

        public void removePropertyChangeListener(String prop,PropertyChangeListener l) {
            if ( boundSupport == null) {
                boundSupport = new PropertyChangeSupport(this);
            }
            boundSupport.removePropertyChangeListener(prop, l);
        }
        
        public void fireSelectedRow(MasterItem mItem) {
            boundSupport.firePropertyChange("selectedItemProp", null, mItem);
        }
        
        public void setSearchClass(String searchClass) {
            this.searchClass = searchClass;
        }
        
        public void setSortBy(String sortBy) {
            this.sortBy = sortBy;
        }
        
        public String getMaster() {
            return master;
        }
        
        public void setOrder(String order) {
            this.order = order;
        }
        
        public void enter() {
        }
        
        protected void getByName(final String text) {
            
            final SqlMasterDao dao = (SqlMasterDao)SqlDaoFactory.create(this, "dao.master");

            //final String keyword = StringTool.toKatakana(text, false);
            final boolean starts = startsWith.isSelected();
            
            Runnable r = new Runnable() {
                public void run() {
                    fireBusy(true);
                    ArrayList results = dao.getByName(master, text, starts, searchClass, sortBy, order);
                    model.setObjectList(results);
                    int count = model.getObjectCount();
                    fireCount(count);
                    fireBusy(false);
                }
            };
            Thread t = new Thread(r);
            t.start();
        }
        
        protected void fireBusy(final boolean b) {
            
            SwingUtilities.invokeLater(new Runnable() {
                
                public void run() {
                    
                    if (b) {
                        countLabel.setText("");
                        statusLabel.setText(underSearchMsg);
                        progressBar.setIndeterminate(true);
                        
                    } else {
                        statusLabel.setText(endSearchMsg);
                        progressBar.setIndeterminate(false);
                        progressBar.setValue(0);
                    }
                }
            });
        }
        
        protected void fireCount(final int count) {
            
            SwingUtilities.invokeLater(new Runnable() {
                
                public void run() {
                
                    String str = String.valueOf(count);
                    countLabel.setText(str);
                }
            });
        }
    }    
        
    protected final class FlagRenderer extends DefaultTableCellRenderer {
        
        String[] flags;

        /** Creates new FlagRenderer */
        public FlagRenderer() {
        }
        
        public String[] getFlag() {
            return flags;
        }
        
        public void setFlag(String[] val) {
            flags = val;
        }

        public Component getTableCellRendererComponent(
                                JTable table,
                                Object value,
                                boolean isSelected,
                                boolean isFocused,
                                int row, int col) {
            Component c = super.getTableCellRendererComponent(
                                             table, 
                                             value,
                                             isSelected,
                                             isFocused, 
                                             row, col);
            if (value != null && value instanceof String) {
                
                try {
                    int index = Integer.parseInt((String)value);
                    ((JLabel)c).setText(flags[index]); 
                }
                catch (Exception e) {
                }
            }                        
            else {
                ((JLabel)c).setText(value == null ? "" : value.toString());
            }
            return c;
        }
    }    
    
    protected final class DateRenderer extends DefaultTableCellRenderer {
        
        private int yearLen = 2;
        private String separator = "/";
        
        /** Creates new FlagRenderer */
        public DateRenderer() {
        }
        
        public int getYearLength() {
            return yearLen;
        }
        
        public void setYearLength(int val) {
            yearLen = val;
        }
        
        public String getSeparator() {
            return separator;
        }
        
        public void setSeparator(String val) {
            separator = val;
        }
        
        private String getDateRep(String val) {
            StringBuffer buf = new StringBuffer();
            int begin = 4 - yearLen;
            buf.append(val.substring(begin, 4));
            buf.append(separator);
            buf.append(val.substring(4, 6));
            buf.append(separator);
            buf.append(val.substring(6));
            return buf.toString();
        }

        public Component getTableCellRendererComponent(
                                JTable table,
                                Object value,
                                boolean isSelected,
                                boolean isFocused,
                                int row, int col) {
            Component c = super.getTableCellRendererComponent(
                                             table, 
                                             value,
                                             isSelected,
                                             isFocused, 
                                             row, col);
            if (value != null && value instanceof String) {
                
                if (! ((String)value).startsWith("9") ) {
                    ((JLabel)c).setText(getDateRep((String)value));
                }
                else {
                    ((JLabel)c).setText("");
                }
            }                        
            else {
                ((JLabel)c).setText(value == null ? "" : value.toString());
            }
            return c;
        }
    }
    
    protected final class CostRenderer extends DefaultTableCellRenderer {
        
        /** Creates new FlagRenderer */
        public CostRenderer() {
        }
        
        public Component getTableCellRendererComponent(
                                JTable table,
                                Object value,
                                boolean isSelected,
                                boolean isFocused,
                                int row, int col) {
            Component c = super.getTableCellRendererComponent(
                                             table, 
                                             value,
                                             isSelected,
                                             isFocused, 
                                             row, col);
            if (value != null && value instanceof String) {
                
                try {
                    
                    ((JLabel)c).setText((String)value); 
                }
                catch (Exception e) {
                }
            }                        
            else {
                ((JLabel)c).setText(value == null ? "" : value.toString());
            }
            return c;
        }
    }
    
    protected class SortActionListener implements ActionListener {
        
        private MasterPanel target;
        private String sortBy;
        private int btnIndex;
        
        public SortActionListener(MasterPanel target, String sortBy, int btnIndex) {
            this.target = target;
            this.sortBy = sortBy;
            this.btnIndex = btnIndex;
        }
        
        public void actionPerformed(ActionEvent e) {
            prefs.putInt("masterSearch." + target.getMaster() + ".sort", btnIndex);
            target.setSortBy(sortBy);
        }
    }
    
    private void setMnemonic(JButton button) {
        
        String text = button.getText();
        int index = text.indexOf("(");
        if ( index > -1 ) {
            char c = text.charAt(index + 1);
            button.setMnemonic(c);
        }
    }
}