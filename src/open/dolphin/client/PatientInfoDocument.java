/*
 * PatientInfoDocument.java
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
package open.dolphin.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.table.*;
import open.dolphin.delegater.PatientDelegater;

import open.dolphin.infomodel.PVTHealthInsuranceModel;
import open.dolphin.infomodel.PVTPublicInsuranceItemModel;
import open.dolphin.infomodel.PatientModel;

import java.awt.*;
import java.util.*;

/**
 * Documet to show Patient and Health Insurance info.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class PatientInfoDocument extends DefaultChartDocument {
    
    /** ���ґ����� */
    private static final String[] PATIENT_ATTRS = { 
        "���� ID", "��  ��", "�J�i", "���[�}�� *","��  ��", "���N����", "��  �� *", "������ *", "�X�֔ԍ�", "�Z  ��", "�d  �b", "�g�ѓd�b *", "�d�q���[�� *"
    };
    
    private static final String INFO_BUTTON_IMAGE   = "about_16.gif";
    
    private static final String INFO = "* �̍��ڂ͕ҏW���\�ł�";
                     
//    private static final String[] MARITAL_NAMES = {"�Ɛg", "����", "���S�l", "����", "�ʋ�"};
//    
//    private static final String[] MARITAL_VALUES = {"single" ,"married", "widowed", "divorced", "separated"};
    
    /** �J������ */
    private static final String[] COLUMN_NAMES = { "��   ��", "�l" };
    
    /** �ҏW�\�ȍs */
    private static final int[] EDITABLE_ROWS = {3, 6, 7, 11, 12};
    
    /** �ۑ��A�C�R�� */
    private static final String SAVE_ICON = "save_16.gif";
    
    /** �ۑ��{�^�� */
    private JButton saveBtn;
    
    /** �����󋵃R���{�{�b�N�X */
    private JComboBox maritalStatusCmb;
    
    private PatientInfoTableModel pModel;
    
    private JTable pTable;
    
    /** 
     * Creates new PatientInfoDocument 
     */
    public PatientInfoDocument() {
    }
    
    public void initialize() {
        
        JPanel myPanel = getUI();
        JComponent compo = createComponent();
        compo.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        myPanel.setLayout(new BorderLayout());
        
        //
        // �ۑ��{�^���𐶐�����
        //
        JPanel cmdPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cmdPanel.add(new JLabel(ClientContext.getImageIcon(INFO_BUTTON_IMAGE)));
        cmdPanel.add(new JLabel(INFO));
        saveBtn = new JButton(ClientContext.getImageIcon(SAVE_ICON));
        saveBtn.setEnabled(false);
        saveBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        cmdPanel.add(saveBtn);
        
        myPanel.add(cmdPanel, BorderLayout.NORTH);
        myPanel.add(compo, BorderLayout.CENTER);
        enter();
    }
    
    public void start() {
        enter();
    }
    
    public void enter() {
        super.enter();
    }
    
    public void setDirty(boolean dirty) {
        saveBtn.setEnabled(dirty);
        super.setDirty(dirty);
    }
    
    private void startAnim() {
        SwingUtilities.invokeLater(new Runnable() {
           public void run() {
               getContext().getStatusPanel().start();
           }
        });
    }
    
    private void stoppAnim() {
        SwingUtilities.invokeLater(new Runnable() {
           public void run() {
               getContext().getStatusPanel().stop();
               setDirty(false);
           }
        });
    }

    
    /**
     * ���ҏ����X�V����B
     */
    public void save() {
        
        final PatientModel update = getContext().getPatient();
        
        Runnable r = new Runnable() {
            public void run() {
                startAnim();
                PatientDelegater pdl = new PatientDelegater();
                pdl.updatePatient(update);
                stoppAnim();
            }
        };
        
        Thread t = new Thread(r);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }
    
    private JComponent createComponent() {
        
        //
        // ���҃��f�����擾����
        //
        PatientModel patient = getContext().getPatient();
        Collection<PVTHealthInsuranceModel> insList = patient.getPvtHealthInsurances();
        
        //
        // ���ҏ��e�[�u���𐶐�����
        //
        pModel = new PatientInfoTableModel(patient, PATIENT_ATTRS, COLUMN_NAMES);
        pTable = new JTable(pModel);
        pTable.setDefaultRenderer(Object.class, new OddEvenRowRenderer());
        TableColumn column = pTable.getColumnModel().getColumn(1);
        DefaultCellEditor de = new DefaultCellEditor(new JTextField());
        de.setClickCountToStart(1);
        column.setCellEditor(de);
        
        //
        // �z�u����
        //
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(pTable);
        
        //
        // ���N�ی����e�[�u���𐶐�����
        //
        if (insList != null) {
            
            for (PVTHealthInsuranceModel insurance : insList) {
                HealthInsuranceTableModel hModel = new HealthInsuranceTableModel(
                        insurance, COLUMN_NAMES);
                JTable hTable = new JTable(hModel);
                hTable.setDefaultRenderer(Object.class,
                        new OddEvenRowRenderer());
                
                // �z�u����
                panel.add(Box.createVerticalStrut(7));
                panel.add(hTable);
            }
        }
        
        JScrollPane scroller = new JScrollPane(panel);
        
        return scroller;
    }
    
    /**
     * ���ҏ���\������ TableModel �N���X�B
     */
    protected class PatientInfoTableModel extends AbstractTableModel {
        
        private static final long serialVersionUID = 8069060595824496345L;
        
        /** ���҃��f�� */
        private PatientModel patient;
        
        /** �������̔z�� */
        private String[] attributes;
        
        /** �J�������̔z�� */
        private String[] columnNames;
        
        public PatientInfoTableModel(PatientModel patient, String[] attrs, String[] columnNames) {
            this.patient = patient;
            this.attributes = attrs;
            this.columnNames = columnNames;
        }
        
        public int getColumnCount() {
            return columnNames.length;
        }
        
        public int getRowCount() {
            return PATIENT_ATTRS.length;
        }
        
        public boolean isCellEditable(int row, int col) {
            //
            // �ҏW�\�ȍs�ł���ꍇ�� true 
            //
            boolean ret = false;
            if (col == 1) {
                for (int i = 0; i < EDITABLE_ROWS.length; i++) {
                    if (row == EDITABLE_ROWS[i]) {
                        ret = true;
                        break;
                    }
                }
            }
            return ret;
        }
        
        public Object getValueAt(int row, int col) {
            
            String ret = null;
            
            if (col == 0) {
                //
                // ��������Ԃ�
                //
                ret = attributes[row];
                
            } else if (col == 1 && patient != null) {
                
                //
                // ���ґ�����Ԃ�
                //
                
                switch (row) {
                    
                    case 0:
                        ret = patient.getPatientId();
                        break;
                        
//                    case 1:
//                        Collection<OtherIdModel> otherIds = patient.getOtherIds();
//                        if (otherIds != null && otherIds.size() >0) {
//                            StringBuilder sb = new StringBuilder();
//                            for (OtherIdModel bean : otherIds) {
//                                sb.append(bean.getIdType());
//                                sb.append("=");
//                                sb.append(bean.getOtherId());
//                                sb.append(" ");
//                            }
//                            ret = sb.toString();
//                        }
//                        break;
                        
                    case 1:
                        ret = patient.getFullName();
                        break;
                        
                    case 2:
                        ret = patient.getKanaName();
                        break;
                        
                    case 3:
                        ret = patient.getRomanName();
                        break;
                        
                    case 4:
                        ret = patient.getGender();
                        break;
                        
                    case 5:
//                        StringBuilder sb = new StringBuilder();
//                        sb.append(patient.getBirthday());
//                        sb.append(patient.getAgeBirthday());
//                        ret = patient.getBirthday();
                        ret = patient.getAgeBirthday();
                        break;
                        
                    case 6:
                        ret = patient.getNationality();
                        break;
                        
                    case 7:
                        ret = patient.getMaritalStatus();
                        break;
                        
                    case 8:
                        ret = patient.contactZipCode();
                        break;
                        
                    case 9:
                        ret = patient.contactAddress();
                        break;
                        
                    case 10:
                        ret = patient.getTelephone();
                        break;
                        
                    case 11:
                        ret = patient.getMobilePhone();
                        break;
                        
                    case 12:
                        ret = patient.getEmail();
                        break;
                        
                }
            }
            return ret;
        }
        
         
        /**
         * �����l��ύX����B
         * @param value �����l
         * @param row �s
         * @param col ��
         */
        public void setValueAt(Object value, int row, int col) {
            
            if (value == null || value.equals("") || col == 0) {
                return;
            }
            
            String strValue = (String) value;
            
            switch (row) {
                
                case 3:
                    //
                    // ���[�}��
                    //
                    patient.setRomanName(strValue);
                    setDirty(true);
                    break;
                    
                case 6:
                    //
                    // ����
                    //
                    patient.setNationality(strValue);
                    setDirty(true);
                    break;
                    
                case 7:
                    //
                    // ������
                    //
                    patient.setMaritalStatus(strValue);
                    setDirty(true);
                    break;
                    
               case 11:
                    //
                    // �g�ѓd�b
                    //
                    patient.setMobilePhone(strValue);
                    setDirty(true);
                    break;     
                    
                case 12:
                    //
                    // �d�q���[��
                    //
                    patient.setEmail(strValue);
                    setDirty(true);
                    break;
            }
        }
    }
    
    protected class HealthInsuranceTableModel extends AbstractTableModel {
        
        private static final long serialVersionUID = 5845546647767875931L;
        
        private String[] columnNames;
        
        private ArrayList data;
        
        public HealthInsuranceTableModel(PVTHealthInsuranceModel insurance,
                String[] columnNames) {
            this.columnNames = columnNames;
            data = getData(insurance);
        }
        
        private ArrayList getData(PVTHealthInsuranceModel insurance) {
            
            if (insurance == null) {
                return null;
            }
            
            ArrayList<String[]> list = new ArrayList<String[]>();
            
            String[] rowData = new String[2];
            rowData[0] = "�ی����";
            rowData[1] = insurance.getInsuranceClass();
            list.add(rowData);
            
            rowData = new String[2];
            rowData[0] = "�ی���ʃR�[�h";
            rowData[1] = insurance.getInsuranceClassCode();
            list.add(rowData);
            
            rowData = new String[2];
            rowData[0] = "�ی��Ҕԍ�";
            rowData[1] = insurance.getInsuranceNumber();
            list.add(rowData);
            
            rowData = new String[2];
            rowData[0] = "��ی��ҋL��";
            rowData[1] = insurance.getClientGroup();
            list.add(rowData);
            
            rowData = new String[2];
            rowData[0] = "��ی��Ҕԍ�";
            rowData[1] = insurance.getClientNumber();
            list.add(rowData);
            
            rowData = new String[2];
            rowData[0] = "�{�l�Ƒ��敪";
            rowData[1] = insurance.getFamilyClass();
            list.add(rowData);
            
            rowData = new String[2];
            rowData[0] = "�J�n��";
            rowData[1] = insurance.getStartDate();
            list.add(rowData);
            
            rowData = new String[2];
            rowData[0] = "�L������";
            rowData[1] = insurance.getExpiredDate();
            list.add(rowData);
            
            String[] vals = insurance.getContinuedDisease();
            if (vals != null) {
                int count = vals.length;
                for (int i = 0; i < count; i++) {
                    rowData = new String[2];
                    rowData[0] = "�p���K��������";
                    rowData[1] = vals[i];
                    list.add(rowData);
                }
            }
            
            rowData = new String[2];
            rowData[0] = "���@���̕��S��";
            rowData[1] = insurance.getPayInRatio();
            list.add(rowData);
            
            rowData = new String[2];
            rowData[0] = "�O�����̕��S��";
            rowData[1] = insurance.getPayOutRatio();
            list.add(rowData);
            
            PVTPublicInsuranceItemModel[] pbi = insurance
                    .getPVTPublicInsuranceItem();
            if (pbi == null) {
                return list;
            }
            int count = pbi.length;
            for (int i = 0; i < count; i++) {
                PVTPublicInsuranceItemModel item = pbi[i];
                
                rowData = new String[2];
                rowData[0] = "����̗D�揇��";
                rowData[1] = item.getPriority();
                list.add(rowData);
                
                rowData = new String[2];
                rowData[0] = "����S����";
                rowData[1] = item.getProviderName();
                list.add(rowData);
                
                rowData = new String[2];
                rowData[0] = "���S�Ҕԍ�";
                rowData[1] = item.getProvider();
                list.add(rowData);
                
                rowData = new String[2];
                rowData[0] = "�󋋎Ҕԍ�";
                rowData[1] = item.getRecipient();
                list.add(rowData);
                
                rowData = new String[2];
                rowData[0] = "�J�n��";
                rowData[1] = item.getStartDate();
                list.add(rowData);
                
                rowData = new String[2];
                rowData[0] = "�L������";
                rowData[1] = item.getExpiredDate();
                list.add(rowData);
                
                rowData = new String[2];
                rowData[0] = "���S��";
                rowData[1] = item.getPaymentRatio();
                list.add(rowData);
                
                rowData = new String[2];
                rowData[0] = "���S���܂��͕��S��";
                rowData[1] = item.getPaymentRatioType();
                list.add(rowData);
            }
            
            return list;
        }
        
        public int getColumnCount() {
            return columnNames.length;
        }
        
        public int getRowCount() {
            return data != null ? data.size() : 5;
        }
        
        public Object getValueAt(int row, int col) {
            
            if (data == null) {
                return null;
            }
            
            if (row >= data.size()) {
                return null;
            }
            
            String[] rowData = (String[]) data.get(row);
            
            return (Object) rowData[col];
        }
    }
}