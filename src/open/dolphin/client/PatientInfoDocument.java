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

import javax.swing.*;
import javax.swing.table.*;

import open.dolphin.dao.*;
import open.dolphin.infomodel.Patient;

import java.awt.*;
import java.util.*;
import mirrorI.dolphin.server.*;


/**
 * Documet to show Patient and Health Insurance info.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class PatientInfoDocument extends DefaultChartDocument {

    /** Creates new PatientInfoDocument */
    public PatientInfoDocument() {
    }
    
    public void start() {
        
        JComponent compo = createComponent();
        compo.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        this.setLayout(new BorderLayout());
        this.add(compo);
        enter();
    }
    
    public void enter() {
        super.enter();
        super.controlMenu();
    }
    
    private JComponent createComponent() {
        
        SqlPatientDao dao = (SqlPatientDao)SqlDaoFactory.create(this, "dao.patient");
        Patient patient = dao.getById(context.getPatient().getId());
        //PVTHealthInsurance insurance = dao.getHealthInsurance(context.getPatientId());
        ArrayList insList = dao.getHealthInsurance(context.getPatient().getId());
        
        // Patient Info table
        String[] attrs = new String[]{"���� ID", "�n�� ID", "��  ��", "�J�i","���[�}��",
                                      "��  ��", "���N����", "��  ��", "������",
                                      "�X�֔ԍ�", "�Z  ��","�d  �b","�d�q���[��"};
        String[] columnNames = new String[]{"��   ��", "�l"};
        PatientInfoTableModel pModel = new PatientInfoTableModel(patient,
                                                               attrs,
                                                               columnNames);
        JTable pTable = new JTable(pModel);
        
        // �z�u
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(pTable);
        //panel.add(Box.createVerticalStrut(7));
        
        // Health Insurance table
        if (insList != null) {
        
            int size = insList.size();
            for (int i = 0; i < size; i++) {
                String[] insColumNames = new String[]{"��   ��", "�l"};
                PVTHealthInsurance insurance = (PVTHealthInsurance)insList.get(i);
                HealthInsuranceTableModel hModel = new HealthInsuranceTableModel(insurance,
                                                                       insColumNames);
                JTable hTable = new JTable(hModel);

                // �z�u
                panel.add(Box.createVerticalStrut(7));
                panel.add(hTable);
            }
        }
        
        JScrollPane scroller = new JScrollPane(panel);
        
        return scroller;
    }
    
    protected class PatientInfoTableModel extends AbstractTableModel {
        
        private Patient patient;
        private String[] attributes;
        private String[] columnNames;
        
        public PatientInfoTableModel(Patient patient, String[] attrs, String[] columnNames) {
            this.patient = patient;
            this.attributes = attrs;
            this.columnNames = columnNames;
        }
        
        public int getColumnCount() {
            return columnNames.length;
        }
        
        public int getRowCount() {
            return 13;
        }
        
        public Object getValueAt(int row, int col) {
            
            String ret = null;
            
            if (col == 0) {
                
                ret = attributes[row];
                
            } else if (col == 1 && patient != null) {
                
                switch (row) {
                    
                    case 0:
                        ret = patient.getId();
                        break;
                        
                    case 1:
                        ret = patient.getLocalId();
                        break;
                        
                    case 2:
                        ret = patient.getName();
                        break;
                        
                    case 3:
                        ret = patient.getKanaName();
                        break;
                        
                    case 4:
                        ret = patient.getRomanName();
                        break;
                        
                    case 5:
                        ret = patient.getGender();
                        break;
                        
                    case 6:
                        ret = patient.getBirthday();
                        break;
                        
                    case 7:
                        ret = patient.getMaritalStatus();
                        break;
                        
                    case 8:
                        ret = patient.getNationality();
                        break;
                        
                    case 9:
                        ret = patient.getHomePostalCode();
                        break;
                        
                    case 10:
                        ret = patient.getHomeAddress();
                        break;
                        
                    case 11:
                        ret = patient.getHomePhone();
                        break;
                        
                    case 12:
                        ret = patient.getEmailAddress()[0];
                        break;
                            
                }                
            }
            return ret;
        }
    } 
    
    protected class HealthInsuranceTableModel extends AbstractTableModel {
        
        private String[] columnNames;
        private ArrayList data;
        
        public HealthInsuranceTableModel(PVTHealthInsurance insurance, String[] columnNames) {
            this.columnNames = columnNames;
            data = getData(insurance);
        }
        
        private ArrayList getData(PVTHealthInsurance insurance) {
            
            if (insurance == null) {
                return null;
            }

            ArrayList list = new ArrayList();
            
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
            rowData[1] = insurance.getInsuranceClientGroup();
            list.add(rowData);
            
            rowData = new String[2];
            rowData[0] = "��ی��Ҕԍ�";
            rowData[1] = insurance.getInsuranceClientNumber();
            list.add(rowData);
            
            rowData = new String[2];
            rowData[0] = "�{�l�Ƒ��敪";
            rowData[1] = insurance.getInsuranceFamilyClass();
            list.add(rowData);
            
            rowData = new String[2];
            rowData[0] = "�J�n��";
            rowData[1] = insurance.getInsuranceStartDate();
            list.add(rowData);
            
            rowData = new String[2];
            rowData[0] = "�L������";
            rowData[1] = insurance.getInsuranceExpiredDate();
            list.add(rowData);
            
            String[] vals = insurance.getInsuranceDisease();
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
            rowData[1] = insurance.getInsurancePayInRatio();
            list.add(rowData);
            
            rowData = new String[2];
            rowData[0] = "�O�����̕��S��";
            rowData[1] = insurance.getInsurancePayOutRatio();
            list.add(rowData);
                        
            PvtPublicInsuranceItem[] pbi = insurance.getPvtPublicInsuranceItem();
            if (pbi == null) {
                return list;
            }
            int count = pbi.length;
            for (int i = 0; i < count; i++) {
                PvtPublicInsuranceItem item = pbi[i];
                
                rowData = new String[2];
                rowData[0] = "����̗D�揇��";
                rowData[1] = item.getPublicInsurancePriority();
                list.add(rowData);
                
                rowData = new String[2];
                rowData[0] = "����S����";
                rowData[1] = item.getPublicInsuranceProviderName();
                list.add(rowData);
                
                rowData = new String[2];
                rowData[0] = "���S�Ҕԍ�";
                rowData[1] = item.getPublicInsuranceProvider();
                list.add(rowData);
                
                rowData = new String[2];
                rowData[0] = "�󋋎Ҕԍ�";
                rowData[1] = item.getPublicInsuranceRecipient();
                list.add(rowData);
                
                rowData = new String[2];
                rowData[0] = "�J�n��";
                rowData[1] = item.getPublicInsuranceStartDate();
                list.add(rowData);
                
                rowData = new String[2];
                rowData[0] = "�L������";
                rowData[1] = item.getPublicInsuranceExpiredDate();
                list.add(rowData);
                
                rowData = new String[2];
                rowData[0] = "���S��";
                rowData[1] = item.getPublicInsurancePaymentRatio();
                list.add(rowData);
                
                rowData = new String[2];
                rowData[0] = "���S���܂��͕��S��";
                rowData[1] = item.getPublicInsurancePaymentRatioType();
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
            
            String[] rowData = (String[])data.get(row);
                       
            return (Object)rowData[col];
        }
    }    
}