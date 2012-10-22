
package open.dolphin.client.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.table.*;
import open.dolphin.client.*;
import open.dolphin.delegater.PatientDelegater;

import open.dolphin.infomodel.PVTHealthInsuranceModel;
import open.dolphin.infomodel.PVTPublicInsuranceItemModel;
import open.dolphin.infomodel.PatientModel;

import java.awt.*;
import java.util.*;
import open.dolphin.helper.DBTask;
import open.dolphin.table.OddEvenRowRenderer;

/**
 * Documet to show Patient and Health Insurance info.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class PatientInfoDocument extends AbstractChartDocument {
    
    // Title
    private static final String TITLE = "���ҏ��";
    
    // ���ґ�����
    private static final String[] PATIENT_ATTRS = { 
        "���� ID", "��  ��", "�J�i", "���[�}�� *","��  ��", "���N����", "��  �� *", "������ *", "�X�֔ԍ�", "�Z  ��", "�d  �b", "�g�ѓd�b *", "�d�q���[�� *"
    };
    
    // Info �A�C�R��
    private static final String INFO_BUTTON_IMAGE   = "about_16.gif";
    
    private static final String INFO = "* �̍��ڂ͕ҏW���\�ł�";
    
    // �J������
    private static final String[] COLUMN_NAMES = { "��   ��", "�l" };
    
    // �ҏW�\�ȍs
    private static final int[] EDITABLE_ROWS = {3, 6, 7, 11, 12};
    
    // �ۑ��A�C�R��
    private static final String SAVE_ICON = "save_16.gif";
    
    // �ۑ��{�^��
    private JButton saveBtn;
    
    // �e�[�u�����f��
    private PatientInfoTableModel pModel;
    
    // �����\���e�[�u��
    private JTable pTable;
    
    // State Context
    private StateContext stateMgr;
    
    /** 
     * Creates new PatientInfoDocument 
     */
    public PatientInfoDocument() {
        setTitle(TITLE);
        
    }
    
    private void initialize() {
        
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
        
        stateMgr = new StateContext();
        enter();
    }
    
    @Override
    public void start() {
        initialize();
    }
    
    @Override
    public void stop() {
    }
    
    @Override
    public void enter() {
        super.enter();
        if (stateMgr != null) {
            stateMgr.enter();
        }
    }
    
    @Override
    public boolean isDirty() {
        if (stateMgr != null) {
            return stateMgr.isDirtyState();
        } else {
            return super.isDirty();
        }
    }
    
    /**
     * ���ҏ����X�V����B
     */
    @Override
    public void save() {
        
        final PatientModel update = getContext().getPatient();
        final PatientDelegater pdl = new PatientDelegater();
        
        DBTask task = new DBTask<Void>(getContext()) {
            
            @Override
            public Void doInBackground() throws Exception {
                pdl.updatePatient(update);
                return null;
            }
            
            @Override
            public void succeeded(Void result) {
                stateMgr.processSavedEvent();
            }
        };
        
        task.execute();
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
        de.setClickCountToStart(2);
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
        
        // ���҃��f��
        private PatientModel patient;
        
        // �������̔z��
        private String[] attributes;
        
        // �J�������̔z��
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
        
        @Override
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
        @Override
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
                    stateMgr.processDirtyEvent();
                    break;
                    
                case 6:
                    //
                    // ����
                    //
                    patient.setNationality(strValue);
                    stateMgr.processDirtyEvent();
                    break;
                    
                case 7:
                    //
                    // ������
                    //
                    patient.setMaritalStatus(strValue);
                    stateMgr.processDirtyEvent();
                    break;
                    
               case 11:
                    //
                    // �g�ѓd�b
                    //
                    patient.setMobilePhone(strValue);
                    stateMgr.processDirtyEvent();
                    break;     
                    
                case 12:
                    //
                    // �d�q���[��
                    //
                    patient.setEmail(strValue);
                    stateMgr.processDirtyEvent();
                    break;
            }
        }
    }
    
    /**
     * �ی�����\������ TableModel �N���X�B
     */
    protected class HealthInsuranceTableModel extends AbstractTableModel {
        
        private String[] columnNames;
        
        private ArrayList<String[]> data;
        
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
    
    abstract class State {
        
        public abstract void enter();
        
    }
    
    class CleanState extends State {
        
        public void enter() {
            saveBtn.setEnabled(false);
            setDirty(false);
        }
    }
    
    class DirtyState extends State {
        
        public void enter() {
            saveBtn.setEnabled(true);
        }
    }
    
    class StateContext {
        
        private CleanState cleanState = new CleanState();
        private DirtyState dirtyState = new DirtyState();
        private State curState;
        
        public StateContext() {
            curState = cleanState;
        }
        
        public void enter() {
            curState.enter();
        }
        
        public void processSavedEvent() {
            curState = cleanState;
            this.enter();
        }
        
        public void processDirtyEvent() {
            if (!isDirtyState()) {
                curState = dirtyState;
                this.enter();
            }
        }
        
        public boolean isDirtyState() {
            return curState == dirtyState ? true : false;
        }
    }
}