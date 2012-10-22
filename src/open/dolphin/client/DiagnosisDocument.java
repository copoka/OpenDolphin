/*
 * DiagnosisDocument.java
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

import java.awt.*;
import java.awt.event.*;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.delegater.StampDelegater;
import open.dolphin.dto.DiagnosisSearchSpec;
import open.dolphin.infomodel.DiagnosisCategoryModel;
import open.dolphin.infomodel.DiagnosisOutcomeModel;
import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModelUtils;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.PatientLiteModel;
import open.dolphin.infomodel.RegisteredDiagnosisModel;
import open.dolphin.infomodel.SimpleDate;
import open.dolphin.infomodel.StampModel;
import open.dolphin.infomodel.UserLiteModel;
import open.dolphin.message.DiseaseHelper;
import open.dolphin.message.MessageBuilder;
import open.dolphin.project.*;
import open.dolphin.table.*;
import open.dolphin.util.*;

import java.beans.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import open.dolphin.dao.SqlOrcaView;
import open.dolphin.message.DiagnosisModuleItem;

/**
 * DiagnosisDocument
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public final class DiagnosisDocument extends DefaultChartDocument implements PropertyChangeListener {
    
    // ���a���e�[�u���̃J�����ԍ���`
    private static final int DIAGNOSIS_COL     = 0;
    private static final int CATEGORY_COL      = 1;
    private static final int OUTCOME_COL       = 2;
    private static final int START_DATE_COL    = 3;
    private static final int END_DATE_COL      = 4;
    
    // ���o���ԃR���{�{�b�N�X�f�[�^
    private NameValuePair[] extractionObjects
            = ClientContext.getNameValuePair("diagnosis.combo.period");
    
    // GUI �R���|�[�l���g��`
    private static final String RESOURCE_BASE          = "/open/dolphin/resources/images/";
    private static final String DELETE_BUTTON_IMAGE    = "del_16.gif";
    private static final String ADD_BUTTON_IMAGE       = "add_16.gif";
    private static final String UPDATE_BUTTON_IMAGE    = "save_16.gif";
    private static final String ORCA_VIEW_IMAGE        = "impt_16.gif";
    private static final String TABLE_BORDER_TITLE     = "���a��";
    private static final String ORCA_VIEW              = "ORCA View";
    private static final String ORCA_RECORD            = "ORCA";
    
    // GUI Component
    private JButton addButton;                  // �V�K�a���G�f�B�^�{�^��
    private JButton updateButton;               // �������a���̓]�A���̍X�V�{�^��
    private JButton deleteButton;               // �������a���̍폜�{�^��
    private JButton orcaButton;                 // ORCA View �{�^��
    private JTable diagTable;                   // �a���e�[�u��
    private ObjectReflectTableModel tableModel; // TableModel
    private JComboBox extractionCombo;          // ���o���ԃR���{
    private JTextField countField;              // �����t�B�[���h
    
//    // ORCA �a���֌W
//    private JTable orcaTable;                   // �a���e�[�u��
//    private ObjectReflectTableModel orcaModel;  // TableModel
    
    // ���o���ԓ��� Dolphin �ɍŏ��ɕa���������
    // ORCA �̕a���͒��o���ԁ`dolphinFirstDate
    private String dolphinFirstDate;
    
    // �����~���t���O
    private boolean ascend;
    
    // �V�K�ɒǉ����ꂽ���a�����X�g
    List<RegisteredDiagnosisModel> addedDiagnosis;
    
    // �X�V���ꂽ���a�����X�g
    List<RegisteredDiagnosisModel> updatedDiagnosis;
    
    // �a���X�V�̃t���O
    //private boolean updated;
    
    // ���a������
    private int diagnosisCount;
    
    // TaskTimer
    private javax.swing.Timer taskTimer;
    
    /**
     *  Creates new DiagnosisDocument
     */
    public DiagnosisDocument() {
    }
    
    /**
     * GUI �R���|�[�l���g�𐶐�����������B
     */
    public void initialize() {
        
        // �R�}���h�{�^���p�l���𐶐�����
        JPanel cmdPanel = createButtonPanel2();
        
        // Dolphin ���a���p�l���𐶐�����
        JPanel dolphinPanel = createDignosisPanel();
        
//        // ORCA ���a���p�l���𐶐�����
//        JPanel orcaPanel = createOrcaViewPanel();
        
        // ���o���ԃp�l���𐶐�����
        JPanel filterPanel = createFilterPanel();
        
//        // Dolphin & ORCA �p�l���� SplitPane �ɉ�����
//        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dolphinPanel, orcaPanel);
//        sp.setOneTouchExpandable(true);
//        sp.setDividerLocation(300);
        
        JPanel content = new JPanel(new BorderLayout(0, 7));
        content.add(cmdPanel, BorderLayout.NORTH);
        content.add(dolphinPanel, BorderLayout.CENTER);
        content.add(filterPanel, BorderLayout.SOUTH);
        content.setBorder(BorderFactory.createTitledBorder(TABLE_BORDER_TITLE));
        
        // �S�̂����C�A�E�g����
        JPanel myPanel = getUI();
        myPanel.setLayout(new BorderLayout(0, 7));
        myPanel.add(content);
        myPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        
        // Preference ���珸���~����ݒ肷��
        ascend = Project.getPreferences().getBoolean(Project.DOC_HISTORY_ASCENDING, false);
    }
    
    /**
     * �R�}���h�{�^���p�l��������B
     */
    private JPanel createButtonPanel2() {
        
        // �X�V�{�^�� (ActionListener) EventHandler.create(ActionListener.class, this, "save")
        updateButton = new JButton(createImageIcon(UPDATE_BUTTON_IMAGE));
        updateButton.addActionListener((ActionListener) EventHandler.create(ActionListener.class, this, "save"));
        updateButton.setEnabled(false);
        updateButton.setToolTipText("�ǉ��ύX�������a�����f�[�^�x�[�X�ɔ��f���܂��B");
        
        // �폜�{�^��
        deleteButton = new JButton(createImageIcon(DELETE_BUTTON_IMAGE));
        deleteButton.addActionListener((ActionListener) EventHandler.create(ActionListener.class, this, "delete"));
        deleteButton.setEnabled(false);
        deleteButton.setToolTipText("�I���������a�����폜���܂��B");
        
        // �V�K�o�^�{�^��
        addButton = new JButton(createImageIcon(ADD_BUTTON_IMAGE));
        addButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    // ASP StampBox ���I������Ă��ď��a��Tree���Ȃ��ꍇ������
                    if (getContext().getChartMediator().hasTree(IInfoModel.ENTITY_DIAGNOSIS)) {
                        JPopupMenu popup = new JPopupMenu();
                        getContext().getChartMediator().addDiseaseMenu(popup);
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                        String msg1 = "���ݎg�p���̃X�^���v�{�b�N�X�ɂ͏��a��������܂���B";
                        String msg2 = "�l�p�̃X�^���v�{�b�N�X���ɐ؂�ւ��Ă��������B";
                        Object obj = new String[]{msg1, msg2};
                        String title = ClientContext.getFrameTitle("���a���ǉ�");
                        Component comp = getUI();
                        JOptionPane.showMessageDialog(comp, obj, title, JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });
        
        // Depends on readOnly prop
        addButton.setEnabled(!isReadOnly());
        addButton.setToolTipText("���a����ǉ����܂��B");
        
        // ORCA View
        orcaButton = new JButton(createImageIcon(ORCA_VIEW_IMAGE));
        //orcaButton.setMargin(new Insets(0,0,0,0));
        orcaButton.addActionListener((ActionListener) EventHandler.create(ActionListener.class, this, "viewOrca"));
        orcaButton.setToolTipText("ORCA�ɓo�^���Ă���a������荞�݂܂��B");
        
        // �{�^���p�l��
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        p.add(orcaButton);
        p.add(deleteButton);
        p.add(addButton);
        p.add(updateButton);
        return p;
    }
    
    /**
     * �����a���e�[�u���𐶐�����B
     */
    private JPanel createDignosisPanel() {
        
        String[] columnNames = ClientContext.getStringArray("diagnosis.columnNames");
        String[] methodNames = ClientContext.getStringArray("diagnosis.methodNames");
        Class[] columnClasses = new Class[]{String.class, String.class, String.class, String.class, String.class};
        int startNumRows = ClientContext.getInt("diagnosis.startNumRows");
        
        // Diagnosis �e�[�u�����f���𐶐�����
        tableModel = new ObjectReflectTableModel(columnNames, startNumRows, methodNames, columnClasses) {
            
            private static final long serialVersionUID = 3528305657868387682L;
            
            // Diagnosis�͕ҏW�s��
            public boolean isCellEditable(int row, int col) {
                
                // licenseCode�Ő���
                if (isReadOnly()) {
                    return false;
                }
                
                // �a�����R�[�h�����݂��Ȃ��ꍇ�� false
                RegisteredDiagnosisModel entry = (RegisteredDiagnosisModel) getObject(row);
                if (entry == null) {
                    return false;
                }
                
                // ORCA �ɓo�^����Ă���a���̏ꍇ
                if (entry.getStatus() != null && entry.getStatus().equals(ORCA_RECORD)) {
                    return false;
                }
                
                // ����ȊO�̓J�����Ɉˑ�����
                return ((col == CATEGORY_COL || col == OUTCOME_COL || col == START_DATE_COL || col == END_DATE_COL))
                ? true
                : false;
            }
            
            // �I�u�W�F�N�g�̒l��ݒ肷��
            public void setValueAt(Object value, int row, int col) {
                
                RegisteredDiagnosisModel entry = (RegisteredDiagnosisModel) getObject(row);
                
                if (value == null || entry == null) {
                    return;
                }
                
                switch (col) {
                    
                    case DIAGNOSIS_COL:
                        break;
                        
                    case CATEGORY_COL:
                        // JComboBox ����I��
                        DiagnosisCategoryModel dcm = (DiagnosisCategoryModel) value;
                        String test = dcm.getDiagnosisCategory();
                        if (test != null && test.equals("") == false) {
                            entry.setCategory(dcm.getDiagnosisCategory());
                            entry.setCategoryDesc(dcm.getDiagnosisCategoryDesc());
                            entry.setCategoryCodeSys(dcm.getDiagnosisCategoryCodeSys());
                        } else {
                            entry.setDiagnosisCategoryModel(null);
                        }
                        fireTableRowsUpdated(row, row);
                        addUpdatedList(entry);
                        break;
                        
                    case OUTCOME_COL:
                        // JComboBox ����I��
                        DiagnosisOutcomeModel dom = (DiagnosisOutcomeModel) value;
                        test = dom.getOutcome();
                        if (test != null && test.equals("") == false) {
                            entry.setOutcome(dom.getOutcome());
                            entry.setOutcomeDesc(dom.getOutcomeDesc());
                            entry.setOutcomeCodeSys(dom.getOutcomeCodeSys());
                            
                            // �����I����������
                            String val = entry.getEndDate();
                            if (val == null || val.equals("")) {
                                GregorianCalendar gc = new GregorianCalendar();
                                int offset = Project.getPreferences().getInt(Project.OFFSET_OUTCOME_DATE, -7);
                                gc.add(Calendar.DAY_OF_MONTH, offset);
                                String today = MMLDate.getDate(gc);
                                entry.setEndDate(today);
                            }
                        } else {
                            entry.setDiagnosisOutcomeModel(null);
                        }
                        fireTableRowsUpdated(row, row);
                        addUpdatedList(entry);
                        break;
                        
                        // case FIRST_ENCOUNTER_COL:
                        // if (value != null && ! ((String)value).trim().equals("") ) {
                        // entry.setFirstEncounterDate((String)value);
                        // entry.setStatus('M');
                        // fireTableRowsUpdated(row, row);
                        // setDirty(true);
                        // }
                        // break;
                        
                    case START_DATE_COL:
                        String strVal = (String)value;
                        if ( !strVal.trim().equals("") ) {
                            entry.setStartDate(strVal);
                            fireTableRowsUpdated(row, row);
                            addUpdatedList(entry);
                        }
                        break;
                        // entry.setStartDate((String)value);
                        // break;
                        
                    case END_DATE_COL:
                        strVal = (String)value;
                        if (!strVal.trim().equals("") ) {
                            entry.setEndDate((String) value);
                            fireTableRowsUpdated(row, row);
                            addUpdatedList(entry);
                        }
                        break;
                }
            }
        };
        
        // ���a���e�[�u���𐶐�����
        diagTable = new JTable(tableModel);
        
        // ��A�����s�̐F����������
        diagTable.setDefaultRenderer(Object.class, new DolphinOrcaRenderer());
        
        diagTable.setSurrendersFocusOnKeystroke(true);
        
        // �����I�����J�����ɃG�f�B�^��ݒ肷��
        TableColumn column = diagTable.getColumnModel().getColumn(END_DATE_COL);
        column.setCellEditor(new IMECellEditor(new JTextField(), 1, false));
        
        // �s�I�����N�������̃��X�i��ݒ肷��
        diagTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        diagTable.setRowSelectionAllowed(true);
        ListSelectionModel m = diagTable.getSelectionModel();
        m.addListSelectionListener((ListSelectionListener) EventHandler.create(ListSelectionListener.class, this, "rowSelectionChanged", ""));
        
        // Category comboBox ���͂�ݒ肷��
        String[] values = ClientContext.getStringArray("diagnosis.category");
        String[] descs = ClientContext.getStringArray("diagnosis.categoryDesc");
        String[] codeSys = ClientContext.getStringArray("diagnosis.categoryCodeSys");
        DiagnosisCategoryModel[] categoryList = new DiagnosisCategoryModel[values.length + 1];
        DiagnosisCategoryModel dcm = new DiagnosisCategoryModel();
        dcm.setDiagnosisCategory("");
        dcm.setDiagnosisCategoryDesc("");
        dcm.setDiagnosisCategoryCodeSys("");
        categoryList[0] = dcm;
        for (int i = 0; i < values.length; i++) {
            dcm = new DiagnosisCategoryModel();
            dcm.setDiagnosisCategory(values[i]);
            dcm.setDiagnosisCategoryDesc(descs[i]);
            dcm.setDiagnosisCategoryCodeSys(codeSys[i]);
            categoryList[i + 1] = dcm;
        }
        JComboBox categoryCombo = new JComboBox(categoryList);
        column = diagTable.getColumnModel().getColumn(CATEGORY_COL);
        column.setCellEditor(new DefaultCellEditor(categoryCombo));
        
        // Outcome comboBox ���͂�ݒ肷��
        String[] ovalues = ClientContext.getStringArray("diagnosis.outcome");
        String[] odescs = ClientContext.getStringArray("diagnosis.outcomeDesc");
        String ocodeSys = ClientContext.getString("diagnosis.outcomeCodeSys");
        DiagnosisOutcomeModel[] outcomeList = new DiagnosisOutcomeModel[ovalues.length + 1];
        DiagnosisOutcomeModel dom = new DiagnosisOutcomeModel();
        dom.setOutcome("");
        dom.setOutcomeDesc("");
        dom.setOutcomeCodeSys("");
        //outcomeList[0] = dom;
        //
        // ��a���͎g�p���Ȃ��炢����
        //
        outcomeList[0] = null;
        for (int i = 0; i < ovalues.length; i++) {
            dom = new DiagnosisOutcomeModel();
            dom.setOutcome(ovalues[i]);
            dom.setOutcomeDesc(odescs[i]);
            dom.setOutcomeCodeSys(ocodeSys);
            outcomeList[i + 1] = dom;
        }
        JComboBox outcomeCombo = new JComboBox(outcomeList);
        column = diagTable.getColumnModel().getColumn(OUTCOME_COL);
        column.setCellEditor(new DefaultCellEditor(outcomeCombo));
        
        //
        // Start Date && EndDate Col �Ƀ|�b�v�A�b�v�J�����_�[��ݒ肷��
        // IME �� OFF �ɂ���
        //
        String datePattern = ClientContext.getString("common.pattern.mmlDate");
        column = diagTable.getColumnModel().getColumn(START_DATE_COL);
        JTextField tf = new JTextField();
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent event) {
                JTextField tf = (JTextField) event.getSource();
                tf.getInputContext().setCharacterSubsets(null);
            }
        });
        new PopupListener(tf);
        tf.setDocument(new RegexConstrainedDocument(datePattern));
        DefaultCellEditor de = new DefaultCellEditor(tf);
        column.setCellEditor(de);
        
        column = diagTable.getColumnModel().getColumn(END_DATE_COL);
        tf = new JTextField();
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent event) {
                JTextField tf = (JTextField) event.getSource();
                tf.getInputContext().setCharacterSubsets(null);
            }
        });
        tf.setDocument(new RegexConstrainedDocument(datePattern));
        new PopupListener(tf);
        de = new DefaultCellEditor(tf);
        column.setCellEditor(de);
        
        //
        // TransferHandler ��ݒ肷��
        //
        diagTable.setTransferHandler(new DiagnosisTransferHandler(this));
        diagTable.setDragEnabled(true);
        
        // Layout
        JScrollPane scroller = new JScrollPane(diagTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        JPanel p = new JPanel(new BorderLayout());
        p.add(scroller, BorderLayout.CENTER);
        return p;
    }
    
//    /**
//     * ORCA �a���p�l���𐶐�����B
//     */
//    private JPanel createOrcaViewPanel() {
//        
//        String[] columnNames = ClientContext.getStringArray("diagnosis.columnNames");
//        String[] methodNames = ClientContext.getStringArray("diagnosis.methodNames");
//        Class[] columnClasses = new Class[]{String.class, String.class, String.class, String.class, String.class};
//        int startNumRows = ClientContext.getInt("diagnosis.startNumRows");
//        
//        // Diagnosis �e�[�u�����f���𐶐�����
//        orcaModel = new ObjectReflectTableModel(columnNames, startNumRows, methodNames, columnClasses);
//        
//        // ORCA ���a���e�[�u���𐶐�����
//        orcaTable = new JTable(orcaModel);
//        
//        // ��A�����s�̐F����������
//        orcaTable.setDefaultRenderer(Object.class, new OddEvenRowRenderer());
//        
//        // Layout
//        JScrollPane scroller = new JScrollPane(orcaTable,
//                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
//                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//        
//        JPanel p = new JPanel(new BorderLayout());
//        p.add(scroller, BorderLayout.CENTER);
//        return p;
//    }
    
    /**
     * ���o���ԃp�l���𐶐�����B
     */
    private JPanel createFilterPanel() {
        
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(Box.createHorizontalStrut(7));
        
        // ���o���ԃR���{�{�b�N�X
        p.add(new JLabel("���o����(�ߋ�)"));
        p.add(Box.createRigidArea(new Dimension(5, 0)));
        extractionCombo = new JComboBox(extractionObjects);
        Preferences prefs = Project.getPreferences();
        int currentDiagnosisPeriod = prefs.getInt(Project.DIAGNOSIS_PERIOD, 0);
        int selectIndex = NameValuePair.getIndex(String.valueOf(currentDiagnosisPeriod), extractionObjects);
        extractionCombo.setSelectedIndex(selectIndex);
        extractionCombo.addItemListener((ItemListener) EventHandler.create(ItemListener.class, this, "extPeriodChanged", ""));
        
        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        comboPanel.add(extractionCombo);
        p.add(comboPanel);
        
        p.add(Box.createHorizontalGlue());
        
        // �����t�B�[���h
        countField = new JTextField(2);
        countField.setEditable(false);
        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        countPanel.add(new JLabel("����"));
        countPanel.add(countField);
        
        p.add(countPanel);
        p.add(Box.createHorizontalStrut(7));
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
        
        return p;
    }
    
    /**
     * �s�I�����N�������̃{�^��������s���B
     */
    public void rowSelectionChanged(ListSelectionEvent e) {
        
        if (e.getValueIsAdjusting() == false) {
            // �폜�{�^�����R���g���[������
            // licenseCode �����ǉ�
            if (isReadOnly()) {
                return;
            }

            // �I�����ꂽ�s�̃I�u�W�F�N�g�𓾂�
            int row = diagTable.getSelectedRow();
            RegisteredDiagnosisModel rd = (RegisteredDiagnosisModel) tableModel.getObject(row);
            
            // �k���̏ꍇ
            if (rd == null) {
                if (deleteButton.isEnabled()) {
                    deleteButton.setEnabled(false);
                }
                return;
            }
            
            // ORCA �̏ꍇ
            if (rd.getStatus() != null && rd.getStatus().equals(ORCA_RECORD)) {
                if (deleteButton.isEnabled()) {
                    deleteButton.setEnabled(false);
                }
                return;
            }
            
            // Dolphin �̏ꍇ
            if (!deleteButton.isEnabled()) {
                deleteButton.setEnabled(true);
            }
        }
    }
    
    /**
     * ���o���Ԃ�ύX�����ꍇ�ɍČ������s���B
     * ORCA �a���{�^���� disable �ł���Ό������ enable �ɂ���B
     */            
    public void extPeriodChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            NameValuePair pair = (NameValuePair) extractionCombo.getSelectedItem();
            int past = Integer.parseInt(pair.getValue());
            if (past != 0) {
                GregorianCalendar today = new GregorianCalendar();
                today.add(GregorianCalendar.MONTH, past);
                today.clear(Calendar.HOUR_OF_DAY);
                today.clear(Calendar.MINUTE);
                today.clear(Calendar.SECOND);
                today.clear(Calendar.MILLISECOND);
                getDiagnosisHistory(today.getTime());
            } else {
                getDiagnosisHistory(new Date(0L));
            }
        }
    }
    
    public JTable getDiagnosisTable() {
        return diagTable;
    }
    
    /**
     * �v���O�������J�n����B
     */
    public void start() {
        
        NameValuePair pair = (NameValuePair) extractionCombo.getSelectedItem();
        int past = Integer.parseInt(pair.getValue());
        
        Date date = null;
        if (past != 0) {
            GregorianCalendar today = new GregorianCalendar();
            today.add(GregorianCalendar.MONTH, past);
            today.clear(Calendar.HOUR_OF_DAY);
            today.clear(Calendar.MINUTE);
            today.clear(Calendar.SECOND);
            today.clear(Calendar.MILLISECOND);
            date = today.getTime();
        } else {
            date = new Date(0l);
        }
        
        getDiagnosisHistory(date);
        enter();
    }
    
    public void stop() {
        if (tableModel != null) {
            tableModel.clear();
        }
    }
    
    public void enter() {
        super.enter();
    }
    
    /**
     * �V�K���a�����X�g�ɒǉ�����B
     * @param added �ǉ����ꂽRegisteredDiagnosisModel
     */
    private void addAddedList(RegisteredDiagnosisModel added) {
        if (addedDiagnosis == null) {
            addedDiagnosis = new ArrayList<RegisteredDiagnosisModel>(5);
        }
        addedDiagnosis.add(added);
        controlUpdateButton();
    }
    
    /**
     * �X�V���X�g�ɒǉ�����B
     * @param updated �X�V���ꂽRegisteredDiagnosisModel
     */
    private void addUpdatedList(RegisteredDiagnosisModel updated) {
        
        // �f�B�^�b�`�I�u�W�F�N�g�̎�
        if (updated.getId() != 0L) {
            // �X�V���X�g�ɒǉ�����
            if (updatedDiagnosis == null) {
                updatedDiagnosis = new ArrayList<RegisteredDiagnosisModel>(5);
            }
            // �������̂��ēx�X�V����Ă���P�[�X������
            if (!updatedDiagnosis.contains(updated)) {
                updatedDiagnosis.add(updated);
            }
            controlUpdateButton();
        }
    }
    
    /**
     * �ǉ��y�эX�V���X�g���N���A����B
     */
    private void clearDiagnosisList() {
        
        if (addedDiagnosis != null && addedDiagnosis.size() > 0) {
            int index = 0;
            while (addedDiagnosis.size() > 0) {
                addedDiagnosis.remove(index);
            }
        }
        
        if (updatedDiagnosis != null && updatedDiagnosis.size() > 0) {
            int index = 0;
            while (updatedDiagnosis.size() > 0) {
                updatedDiagnosis.remove(index);
            }
        }
        
        controlUpdateButton();
    }
    
    /**
     * �X�V�{�^���𐧌䂷��B
     */
    private void controlUpdateButton() {
        boolean hasAdded = (addedDiagnosis != null && addedDiagnosis.size() > 0) ? true : false;
        boolean hasUpdated = (updatedDiagnosis != null && updatedDiagnosis.size() > 0) ? true : false;
        boolean newDirty = (hasAdded || hasUpdated) ? true : false;
        boolean old = isDirty();
        if (old != newDirty) {
            setDirty(newDirty);
            updateButton.setEnabled(isDirty());
        }
    }
    
    /**
     * ���a��������Ԃ��B
     * @return ���a������
     */
    public int getDiagnosisCount() {
        return diagnosisCount;
    }
    
    /**
     * ���a��������ݒ肷��B
     * @param cnt ���a������
     */
    public void setDiagnosisCount(int cnt) {
        diagnosisCount = cnt;
        try {
            String val = String.valueOf(diagnosisCount);
            countField.setText(val);
        } catch (RuntimeException e) {
            countField.setText("");
        }
    }
    
    /**
     * ImageIcon ��Ԃ�
     */
    private ImageIcon createImageIcon(String name) {
        String res = RESOURCE_BASE + name;
        return new ImageIcon(this.getClass().getResource(res));
    }
    
    /**
     * ���a���X�^���v���擾���� worker ���N������B
     */
    public void importStampList(List<ModuleInfoBean> stampList, final int insertRow) {
        
        // ProgressBar ���擾����
        final IStatusPanel statusPanel = getContext().getStatusPanel();
        
        // Worker �𐶐�����
        int maxEstimation = ClientContext.getInt("task.default.maxEstimation");
        int delay = ClientContext.getInt("task.default.delay");
        int taskLength = maxEstimation/delay;
        final StampDelegater sdl = new StampDelegater();
        final StampGetTask worker = new StampGetTask(stampList, sdl, taskLength);
        
        // �^�X�N�^�C�}�[�𐶐�����
        taskTimer = new javax.swing.Timer(delay, new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                worker.getCurrent();
                
                if (worker.isDone()) {
                    statusPanel.stop();
                    taskTimer.stop();
                    
                    if (sdl.isNoError()) {
                        List<StampModel> modelList = worker.getModelList();
                        if (modelList != null) {
                            for (int i = modelList.size() -1; i > -1; i--) {
                                insertStamp((StampModel)modelList.get(i), insertRow);
                            }
                        }
                        
                    } else {
                        warning(ClientContext.getString("diagnosis.title"), sdl.getErrorMessage());
                    }
                    
                } else if (worker.isTimeOver()) {
                    statusPanel.stop();
                    taskTimer.stop();
                    JFrame parent = getContext().getFrame();
                    String title = ClientContext.getString("diagnosis.title");
                    new TimeoutWarning(parent, title, null).start();
                }
            }
        });
        worker.start();
        statusPanel.start("");
        taskTimer.start();
    }
    
    /**
     * ���a���X�^���v���f�[�^�x�[�X����擾���e�[�u���֑}������B
     * Worker Thread �Ŏ��s�����B
     * @param stampInfo
     */
    private void insertStamp(StampModel sm, int row) {
        
        if (sm != null) {
            RegisteredDiagnosisModel module
                    = (RegisteredDiagnosisModel) BeanUtils.xmlDecode(sm.getStampBytes());
            
            // �����̓��t�������J�n���Ƃ��Đݒ肷��
            GregorianCalendar gc = new GregorianCalendar();
            String today = MMLDate.getDate(gc);
            module.setStartDate(today);
            
//            // �f�t�H���g��Category �l���Z�b�g����
//            DiagnosisCategoryModel dc = new DiagnosisCategoryModel();
//            dc.setDiagnosisCategory(IInfoModel.DEFAULT_DIAGNOSIS_CATEGORY);
//            dc.setDiagnosisCategoryDesc(IInfoModel.DEFAULT_DIAGNOSIS_CATEGORY_DESC);
//            dc.setDiagnosisCategoryCodeSys(IInfoModel.DEFAULT_DIAGNOSIS_CATEGORY_CODESYS);
//            module.setDiagnosisCategoryModel(dc);
            
            row = tableModel.getObjectCount() == 0 ? 0 : row;
            int cnt = tableModel.getObjectCount();
            if (row == 0 && cnt == 0) {
                tableModel.addRow(module);
            } else if (row < cnt) {
                tableModel.insertRow(row, module);
            } else {
                tableModel.addRow(module);
            }
            
            //
            // row ��I������
            //
            diagTable.getSelectionModel().setSelectionInterval(row, row);
            
            addAddedList(module);
        }
    }
    
    /**
     * ���a���G�f�B�^���J���B
     */
    public void openEditor2() {
        StampEditorDialog stampEditor = new StampEditorDialog("diagnosis", null);
        
        // �ҏW�I���A�l�̎󂯎��ɂ��̃I�u�W�F�N�g��ݒ肷��
        stampEditor.addPropertyChangeListener(StampEditorDialog.VALUE_PROP, this);
        stampEditor.start();
    }
    
    /**
     * ���a���G�f�B�^����f�[�^���󂯎��e�[�u���֒ǉ�����B
     */
    public void propertyChange(PropertyChangeEvent e) {
        
        ArrayList list = (ArrayList) e.getNewValue();
        if (list == null) {
            return;
        }
        
        int len = list.size();
        
        if (ascend) {
            // �����Ȃ̂Ńe�[�u���̍Ō�֒ǉ�����
            for (int i = 0; i < len; i++) {
                RegisteredDiagnosisModel module
                        = (RegisteredDiagnosisModel) list.get(i);
                tableModel.addRow(module);
                addAddedList(module);
            }
            
        } else {
            // �~���Ȃ̂Ńe�[�u���̐擪�֒ǉ�����
            for (int i = len - 1; i > -1; i--) {
                RegisteredDiagnosisModel module
                        = (RegisteredDiagnosisModel) list.get(i);
                tableModel.insertRow(0, module);
                addAddedList(module);
            }
        }
    }
    
    /**
     * �V�K�y�ѕύX���ꂽ���a����ۑ�����B
     */
    public void save() {
        
        if ( (addedDiagnosis == null || addedDiagnosis.size() == 0) &&
                (updatedDiagnosis == null || updatedDiagnosis.size() == 0) ) {
            return;
        }
        
        final boolean sendDiagnosis = Project.getSendDiagnosis()
        && ((ChartPlugin) getContext()).getCLAIMListener() != null ? true : false;
        
        
        // continue to save
        Date confirmed = new Date();
        
        if (addedDiagnosis != null && addedDiagnosis.size() > 0) {
            
            for (RegisteredDiagnosisModel rd : addedDiagnosis) {
                
                // �J�n���A�I�����̓e�[�u������擾���Ă���
                // TODO confirmed, recorded
                rd.setKarte(getContext().getKarte());           // Karte
                rd.setCreator(Project.getUserModel());          // Creator
                rd.setConfirmed(confirmed);                     // �m���
                rd.setRecorded(confirmed);                      // �L�^��
                rd.setStatus(IInfoModel.STATUS_FINAL);
                
                // �J�n��=�K���J�n�� not-null
                if (rd.getStarted() == null) {
                    rd.setStarted(confirmed);
                }
                
                // TODO �g���t�B�b�N
                rd.setPatientLiteModel(getContext().getPatient().patientAsLiteModel());
                rd.setUserLiteModel(Project.getUserModel().getLiteModel());
            }
        }
        
        if (updatedDiagnosis != null && updatedDiagnosis.size() > 0) {
            
            for (RegisteredDiagnosisModel rd : updatedDiagnosis) {
                
                // ���o�[�W�����͏㏑�����Ă���
                rd.setCreator(Project.getUserModel());
                rd.setConfirmed(confirmed);
                rd.setRecorded(confirmed);
                rd.setStatus(IInfoModel.STATUS_FINAL);
                
                // TODO �g���t�B�b�N
                rd.setPatientLiteModel(getContext().getPatient().patientAsLiteModel());
                rd.setUserLiteModel(Project.getUserModel().getLiteModel());
            }
        }
        
        final IStatusPanel statusPanel = getContext().getStatusPanel();
        
        // Worker �𐶐�����
        int maxEstimation = ClientContext.getInt("task.default.maxEstimation");
        int delay = ClientContext.getInt("task.default.delay");
        int taskLength = maxEstimation/delay;
        final DocumentDelegater ddl = new DocumentDelegater();
        final DiagnosisPutTask worker = new DiagnosisPutTask(addedDiagnosis, updatedDiagnosis, sendDiagnosis, ddl, taskLength);
        
        // �^�C�}�[���N������
        taskTimer = new javax.swing.Timer(delay, new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                worker.getCurrent();
                
                if (worker.isDone()) {
                    statusPanel.stop();
                    taskTimer.stop();
                    
                    // �ۑ��y�эX�V���ꂽ��
                    if (ddl.isNoError()) {
                        List<Long> addedIds = worker.getAddedIds();
                        if (addedDiagnosis != null && addedIds != null && addedDiagnosis.size() == addedIds.size()) {
                            for (int i = 0; i < addedDiagnosis.size(); i++) {
                                RegisteredDiagnosisModel rd = (RegisteredDiagnosisModel) addedDiagnosis.get(i);
                                rd.setId(addedIds.get(i));
                            }
                        }
                        clearDiagnosisList();
                    } else {
                        //�G���[���e��\������
                        warning(ClientContext.getString("diagnosis.title"), ddl.getErrorMessage());
                    }
                    
                } else if (worker.isTimeOver()) {
                    statusPanel.stop();
                    taskTimer.stop();
                    JFrame parent = getContext().getFrame();
                    String title = ClientContext.getString("diagnosis.title");
                    new TimeoutWarning(parent, title, null).start();
                }
            }
        });
        worker.start();
        statusPanel.start("");
        taskTimer.start();
    }
    
    /**
     * �w����Ԉȍ~�̏��a�����������ăe�[�u���֕\������B
     * �o�b�O�O�����h�X���b�h�Ŏ��s�����B
     */
    @SuppressWarnings("unchecked")
    public void getDiagnosisHistory(Date past) {
        
        DiagnosisSearchSpec spec = new DiagnosisSearchSpec();
        spec.setCode(DiagnosisSearchSpec.PATIENT_SEARCH);
        spec.setKarteId(getContext().getKarte().getId());
        if (past != null) {
            spec.setFromDate(past);
        }
        
        final DocumentDelegater ddl = new DocumentDelegater();
        
        // ProgressBar ���擾����
        final IStatusPanel statusPanel = getContext().getStatusPanel();
        
        // Worker �𐶐�����
        int maxEstimation = ClientContext.getInt("task.default.maxEstimation");
        int delay = ClientContext.getInt("task.default.delay");
        int taskLength = maxEstimation/delay;
        final DiagnosisGetTask worker = new DiagnosisGetTask(spec, ddl, taskLength);
        
        // �^�X�N�^�C�}�[�𐶐�����
        taskTimer = new javax.swing.Timer(delay, new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                worker.getCurrent();
                statusPanel.setMessage(worker.getMessage());
                
                if (worker.isDone()) {
                    statusPanel.stop();
                    taskTimer.stop();
                    orcaButton.setEnabled(true);    // reset
                    
                    // �G���[���`�F�b�N����
                    if (ddl.isNoError()) {
                        // noError
                        List list = worker.getResult();
                        if (list != null && list.size() > 0) {
                            if (ascend) {
                                Collections.sort(list);
                                RegisteredDiagnosisModel rd = (RegisteredDiagnosisModel) list.get(0);
                                dolphinFirstDate = rd.getStartDate();
                            } else {
                                Collections.sort(list, Collections.reverseOrder());
                                int index = list.size() -1;
                                RegisteredDiagnosisModel rd = (RegisteredDiagnosisModel) list.get(index);
                                dolphinFirstDate = rd.getStartDate();
                            }
                            tableModel.setObjectList(list);
                            setDiagnosisCount(list.size());
                        }
                        
                    } else {
                        // �G���[���e��\������
                        warning(ClientContext.getString("diagnosis.title"), ddl.getErrorMessage());
                    }
                    
                } else if (worker.isTimeOver()) {
                    // �^�C���A�E�g�\��������
                    statusPanel.stop();
                    taskTimer.stop();
                    orcaButton.setEnabled(true);
                    JFrame parent = getContext().getFrame();
                    String title = ClientContext.getString("diagnosis.title");
                    new TimeoutWarning(parent, title, null).start();
                }
            }
        });
        worker.start();
        statusPanel.start("");
        taskTimer.start();
    }
    
    /**
     * �I�����ꂽ�s�̃f�[�^���폜����B
     */
    public void delete() {
        
        // �I�����ꂽ�s�̃I�u�W�F�N�g���擾����
        final int row = diagTable.getSelectedRow();
        final RegisteredDiagnosisModel model = (RegisteredDiagnosisModel) tableModel.getObject(row);
        if (model == null) {
            return;
        }
        
        // �܂��f�[�^�x�[�X�ɓo�^����Ă��Ȃ��f�[�^�̏ꍇ
        // �e�[�u������폜���ă��^�[������
        if (model.getId() == 0L) {
            if (addedDiagnosis != null && addedDiagnosis.contains(model)) {
                tableModel.deleteRow(row);
                setDiagnosisCount(tableModel.getObjectCount());
                addedDiagnosis.remove(model);
                controlUpdateButton();
                return;
            }
        }
        
        // �f�B�^�b�`�I�u�W�F�N�g�̏ꍇ�̓f�[�^�x�[�X����폜����
        // �폜�̏ꍇ�͂��̏�Ńf�[�^�x�[�X�̍X�V���s�� 2006-03-25
        final List<Long> list = new ArrayList<Long>(1);
        list.add(new Long(model.getId()));
        
        // Worker �𐶐�����
        final IStatusPanel statusPanel = getContext().getStatusPanel();
        int maxEstimation = ClientContext.getInt("task.default.maxEstimation");
        int delay = ClientContext.getInt("task.default.delay");
        int taskLength = maxEstimation/delay;
        final DocumentDelegater ddl = new DocumentDelegater();
        final DiagnosisDeleteTask worker = new DiagnosisDeleteTask(list, ddl, taskLength);
        
        taskTimer = new javax.swing.Timer(delay, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                worker.getCurrent();
                if (worker.isDone()) {
                    taskTimer.stop();
                    statusPanel.stop("");
                    if (ddl.isNoError()) {
                        tableModel.deleteRow(row);
                        setDiagnosisCount(tableModel.getObjectCount());
                        // �X�V���X�g�ɂ���ꍇ
                        // �X�V���X�g�����菜��
                        if (updatedDiagnosis != null) {
                            updatedDiagnosis.remove(model);
                            controlUpdateButton();
                        }
                    } else {
                        warning(ClientContext.getString("diagnosis.title"), ddl.getErrorMessage());
                    }
                    
                } else if (worker.isTimeOver()) {
                    taskTimer.stop();
                    statusPanel.stop("");
                    JFrame parent = getContext().getFrame();
                    String title = ClientContext.getString("diagnosis.title");
                    new TimeoutWarning(parent, title, null).start();
                }
            }
        });
        worker.start();
        statusPanel.start("");
        taskTimer.start();
    }
    
    /**
     * ORCA�ɓo�^����Ă���a������荞�ށB�i�e�[�u���֒ǉ�����j 
     * ������A�{�^���� disabled �ɂ���B
     */
    public void viewOrca() {
        
        // ����ID���擾����
        String patientId = getContext().getPatient().getPatientId();
        
        // ���o���Ԃ��猟���͈͂̍ŏ��̓����擾����
        NameValuePair pair = (NameValuePair) extractionCombo.getSelectedItem();
        int past = Integer.parseInt(pair.getValue());
        
        Date date = null;
        if (past != 0) {
            GregorianCalendar today = new GregorianCalendar();
            today.add(GregorianCalendar.MONTH, past);
            today.clear(Calendar.HOUR_OF_DAY);
            today.clear(Calendar.MINUTE);
            today.clear(Calendar.SECOND);
            today.clear(Calendar.MILLISECOND);
            date = today.getTime();
        } else {
            date = new Date(0l);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String from = sdf.format(date);
        
        // �����͈͂̍Ō�̓��� Dolphin ���R�[�h�̍ŏ��̓�
        // �k���̏ꍇ�͍����܂ł���������
//        String to = null;
//        if (dolphinFirstDate == null) {
//            to = sdf.format(new Date());
//        } else {
//            to = dolphinFirstDate.replaceAll("-", "");
//        }
        String to = sdf.format(new Date());
        System.out.println("from = " + from);
        System.out.println("to = " + to);
        
        final Component comp = (Component) this.getUI();
        
        // DAO�𐶐�����
        final SqlOrcaView dao = new SqlOrcaView();
        
        // ReflectMonitor �𐶐�����
        final ReflectMonitor rm = new ReflectMonitor();
        rm.setReflection(dao, 
                         "getOrcaDisease", 
                         new Class[]{String.class, String.class, String.class, Boolean.class}, 
                         new Object[]{patientId, from, to, new Boolean(ascend)});
        rm.setMonitor(SwingUtilities.getWindowAncestor(comp), ORCA_VIEW, "�a�����������Ă��܂�...  ", 200, 60*1000);
        
        //
        // ReflectMonitor �̌���State property �̑������X�i�𐶐�����
        //
        PropertyChangeListener pl = new PropertyChangeListener() {
           
            public void propertyChange(PropertyChangeEvent e) {
                
                int state = ((Integer) e.getNewValue()).intValue();
                
                switch (state) {
                    
                    case ReflectMonitor.DONE:
                        if (dao.isNoError()) {
                            List list = (List) rm.getResult();
                            if (ascend) {
                                Collections.sort(list);
                            } else {
                                Collections.sort(list, Collections.reverseOrder());
                            }
                            tableModel.addRows(list);
                        } else {
                            String errMsg = dao.getErrorMessage();
                            String title = ClientContext.getFrameTitle(ORCA_VIEW);
                            JOptionPane.showMessageDialog(comp, errMsg, title, JOptionPane.WARNING_MESSAGE);
                        }
                        
                        break;
                        
                    case ReflectMonitor.TIME_OVER:
                        orcaButton.setEnabled(true);
                        break;
                        
                    case ReflectMonitor.CANCELED:
                        orcaButton.setEnabled(true);
                        break;
                }
                
                //
                // Block ����������
                //
                //setBusy(false);
            }
        };
        rm.addPropertyChangeListener(pl);
        
        //
        // Block ���A���\�b�h�̎��s���J�n����
        //
        //setBusy(true);
        orcaButton.setEnabled(false);
        rm.start();
    }
    
    /**
     * PopupListener
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
                CalendarCardPanel cc = new CalendarCardPanel(getContext().getContext().getEventColorTable());
                cc.addPropertyChangeListener(CalendarCardPanel.PICKED_DATE, this);
                cc.setCalendarRange(new int[] { -12, 0 });
                popup.insert(cc, 0);
                popup.show(e.getComponent(), e.getX(), e.getY());
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
    
    /**
     * StampTask
     */
    protected class StampTask extends AbstractInfiniteTask {
        
        private StampModel stampModel;
        private String stampId;
        private StampDelegater sdl;
        
        public StampTask(String stampId, StampDelegater sdl, int taskLength) {
            this.stampId = stampId;
            this.sdl = sdl;
            setTaskLength(taskLength);
        }
        
        protected StampModel getStamp() {
            return stampModel;
        }
        
        protected void doTask() {
            stampModel = sdl.getStamp(stampId);
            setDone(true);
        }
    }
    
    /**
     * DiagnosisGetTask
     */
    protected class DiagnosisGetTask extends AbstractInfiniteTask {
        
        private List list;
        private DocumentDelegater ddl;
        private DiagnosisSearchSpec spec;
        
        public DiagnosisGetTask(DiagnosisSearchSpec spec, DocumentDelegater ddl, int taskLength) {
            this.spec = spec;
            this.ddl = ddl;
            setTaskLength(taskLength);
        }
        
        protected List getResult() {
            return list;
        }
        
        protected void doTask() {
            list = ddl.getDiagnosisList(spec);
            setDone(true);
        }
    }
    
    /**
     * DiagnosisDeleteTask
     */
    protected class DiagnosisDeleteTask extends AbstractInfiniteTask {
        
        private List<Long> list;
        private DocumentDelegater ddl;
        private int putCode;
        
        public DiagnosisDeleteTask(List<Long> list, DocumentDelegater ddl, int taskLength) {
            this.list = list;
            this.ddl = ddl;
            setTaskLength(taskLength);
        }
        
        protected int getResult() {
            return putCode;
        }
        
        protected void doTask() {
            putCode = ddl.removeDiagnosis(list);
            setDone(true);
        }
    }
    
    /**
     * DiagnosisPutTask
     */
    protected class DiagnosisPutTask extends AbstractInfiniteTask {
        
        private List<RegisteredDiagnosisModel> added;
        private List<RegisteredDiagnosisModel> updated;
        private boolean sendClaim;
        private DocumentDelegater ddl;
        private List<Long> ids;
        
        public DiagnosisPutTask(List<RegisteredDiagnosisModel> added,
                List<RegisteredDiagnosisModel> updated,
                boolean sendClaim,
                DocumentDelegater ddl,
                int taskLength) {
            
            this.added = added;
            this.updated = updated;
            this.sendClaim = sendClaim;
            this.ddl = ddl;
            setTaskLength(taskLength);
        }
        
        protected List<Long> getAddedIds() {
            return ids;
        }
        
        protected void doTask() {
            
            // �X�V����
            if (updated != null && updated.size() > 0) {
                ddl.updateDiagnosis(updated);
            }
            
            // �ۑ�����
            if (added != null && added.size() > 0) {
                ids = ddl.putDiagnosis(added);
            }
            
            //
            // �ǉ��a���� CLAIM ���M����
            //
            if (sendClaim && added != null && added.size() > 0) {
                
                // DocInfo & RD ���J�v�Z���������A�C�e���𐶐�����
                ArrayList<DiagnosisModuleItem> moduleItems = new ArrayList<DiagnosisModuleItem>();
                
                for (RegisteredDiagnosisModel rd : added) {
                    DocInfoModel docInfo = new DocInfoModel();
                    docInfo.setDocId(GUIDGenerator.generate(docInfo));
                    docInfo.setTitle(IInfoModel.DEFAULT_DIAGNOSIS_TITLE);
                    docInfo.setPurpose(IInfoModel.PURPOSE_RECORD);
                    docInfo.setFirstConfirmDate(ModelUtils.getDateTimeAsObject(rd.getConfirmDate()));
                    docInfo.setConfirmDate(ModelUtils.getDateTimeAsObject(rd.getFirstConfirmDate()));
                    
                    DiagnosisModuleItem mItem = new DiagnosisModuleItem();
                    mItem.setDocInfo(docInfo);
                    mItem.setRegisteredDiagnosisModule(rd);
                    moduleItems.add(mItem);
                }
                
                // �w���p�[�p�̒l�𐶐�����
                String confirmDate = added.get(0).getConfirmDate();
                UserLiteModel creator = added.get(0).getUserLiteModel();
                PatientLiteModel patient = added.get(0).getPatientLiteModel();
                
                // �w���p�[�N���X�𐶐�����
                DiseaseHelper dhl = new DiseaseHelper();
                dhl.setPatientId(patient.getPatientId());
                dhl.setConfirmDate(confirmDate);
                dhl.setCreator(creator);
                dhl.setDiagnosisModuleItems(moduleItems);
                dhl.setGroupId(GUIDGenerator.generate(dhl));
                dhl.setDepartment(getContext().getPatientVisit().getDepartmentCode());
                dhl.setDepartmentDesc(getContext().getPatientVisit().getDepartment());
                //dhl.setDepartment(Project.getUserModel().getDepartmentModel().getDepartment());
                //dhl.setDepartmentDesc(Project.getUserModel().getDepartmentModel().getDepartmentDesc());
                
                MessageBuilder mb = new MessageBuilder();
                String claimMessage = mb.build(dhl);
                // debug
                if (ClientContext.getLogger("claim") != null) {
                    ClientContext.getLogger("claim").debug(claimMessage);
                }
                ClaimMessageEvent event = new ClaimMessageEvent(this);
                event.setPatientId(patient.getPatientId());
                event.setPatientName(patient.getName());
                event.setPatientSex(patient.getGender());
                event.setTitle(IInfoModel.DEFAULT_DIAGNOSIS_TITLE);
                event.setClaimInstance(claimMessage);
                event.setConfirmDate(confirmDate);
                ClaimMessageListener claimListener = ((ChartPlugin) getContext()).getCLAIMListener();
                if (claimListener != null) {
                    claimListener.claimMessageEvent(event);
                }
            }
            
            //          
            // �X�V���ꂽ�a���� CLAIM ���M����
            //
            if (sendClaim && updated != null && updated.size() > 0) {
                
                // RegisteredDiagnosisModel������ DocInfo �𐶐�����
                ArrayList<DiagnosisModuleItem> moduleItems = new ArrayList<DiagnosisModuleItem>();
                
                for (RegisteredDiagnosisModel rd : updated) {
                    DocInfoModel docInfo = new DocInfoModel();
                    docInfo.setDocId(GUIDGenerator.generate(docInfo));
                    docInfo.setTitle(IInfoModel.DEFAULT_DIAGNOSIS_TITLE);
                    docInfo.setPurpose(IInfoModel.PURPOSE_RECORD);
                    docInfo.setFirstConfirmDate(ModelUtils.getDateTimeAsObject(rd.getConfirmDate()));
                    docInfo.setConfirmDate(ModelUtils.getDateTimeAsObject(rd.getFirstConfirmDate()));
                    
                    DiagnosisModuleItem mItem = new DiagnosisModuleItem();
                    mItem.setDocInfo(docInfo);
                    mItem.setRegisteredDiagnosisModule(rd);
                    moduleItems.add(mItem);
                }
                
                // �w���p�[�p�̒l�𐶐�����
                String confirmDate = updated.get(0).getConfirmDate();
                UserLiteModel creator = updated.get(0).getUserLiteModel();
                PatientLiteModel patient = updated.get(0).getPatientLiteModel();
                
                // �w���p�[�N���X�𐶐�����
                DiseaseHelper dhl = new DiseaseHelper();
                dhl.setPatientId(patient.getPatientId());
                dhl.setConfirmDate(confirmDate);
                dhl.setCreator(creator);
                dhl.setDiagnosisModuleItems(moduleItems);
                dhl.setGroupId(GUIDGenerator.generate(dhl));
                dhl.setDepartment(Project.getUserModel().getDepartmentModel().getDepartment());
                dhl.setDepartmentDesc(Project.getUserModel().getDepartmentModel().getDepartmentDesc());
                
                MessageBuilder mb = new MessageBuilder();
                String claimMessage = mb.build(dhl);
                // debug
                if (ClientContext.getLogger("claim") != null) {
                    ClientContext.getLogger("claim").debug(claimMessage);
                }
                ClaimMessageEvent event = new ClaimMessageEvent(this);
                event.setPatientId(patient.getPatientId());
                event.setPatientName(patient.getName());
                event.setPatientSex(patient.getGender());
                event.setTitle(IInfoModel.DEFAULT_DIAGNOSIS_TITLE);
                event.setClaimInstance(claimMessage);
                event.setConfirmDate(confirmDate);
                ClaimMessageListener claimListener = ((ChartPlugin) getContext()).getCLAIMListener();
                if (claimListener != null) {
                    claimListener.claimMessageEvent(event);
                }
            }
            
            setDone(true);
        }
    }
    
    /**
     *
     */    
    class DolphinOrcaRenderer extends DefaultTableCellRenderer {
        
        /** JTable�����_���p�̊�J���[ */
        private Color ODD_COLOR = ClientContext.getColor("color.odd");
    
        /** JTable�����_���p�̋����J���[ */
        private Color EVEN_COLOR = ClientContext.getColor("color.even");
        
        private Color ORCA_BACK = ClientContext.getColor("color.CALENDAR_BACK");
        
        /** Creates new IconRenderer */
        public DolphinOrcaRenderer() {
            super();
        }
        
        public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean isFocused,
                int row, int col) {
            Component c = super.getTableCellRendererComponent(table,
                    value,
                    isSelected,
                    isFocused, row, col);
            
            RegisteredDiagnosisModel rd = (RegisteredDiagnosisModel) tableModel.getObject(row);
            
            // ORCA ���R�[�h���ǂ����𔻒肷��
            boolean orca = (rd != null && rd.getStatus() != null && rd.getStatus().equals(ORCA_RECORD)) ? true : false;
            
            if (orca) {
                setBackground(ORCA_BACK);
                
            } else {
                
                if (row % 2 == 0) {
                    setBackground(EVEN_COLOR);
                } else {
                    setBackground(ODD_COLOR);
                }
            }
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            }
            
            if (value != null && value instanceof String) {
                ((JLabel) c).setText((String) value);
            } else {
                ((JLabel) c).setText(value == null ? "" : value.toString());
            }
            return c;
        }
    }
}