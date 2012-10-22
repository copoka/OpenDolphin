package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
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
import open.dolphin.message.DiseaseHelper;
import open.dolphin.message.MessageBuilder;
import open.dolphin.project.*;
import open.dolphin.table.*;
import open.dolphin.util.*;

import open.dolphin.dao.SqlOrcaView;
import open.dolphin.helper.DBTask;
import open.dolphin.message.DiagnosisModuleItem;
import open.dolphin.order.StampEditor;

/**
 * DiagnosisDocument
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public final class DiagnosisDocument extends AbstractChartDocument implements PropertyChangeListener {

    private static final String TITLE = "���a��";

    // ���a���e�[�u���̃J�����ԍ���`
    private static final int DIAGNOSIS_COL  = 0;
    private static final int CATEGORY_COL   = 1;
    private static final int OUTCOME_COL    = 2;
    private static final int START_DATE_COL = 3;
    private static final int END_DATE_COL   = 4;

    // ���o���ԃR���{�{�b�N�X�f�[�^
    //private NameValuePair[] extractionObjects = ClientContext.getNameValuePair("diagnosis.combo.period");
    // GUI �R���|�[�l���g��`
    private static final String RESOURCE_BASE = "/open/dolphin/resources/images/";
    private static final String DELETE_BUTTON_IMAGE = "del_16.gif";
    private static final String ADD_BUTTON_IMAGE = "add_16.gif";
    private static final String UPDATE_BUTTON_IMAGE = "save_16.gif";
    private static final String ORCA_VIEW_IMAGE = "impt_16.gif";
    private static final String TABLE_BORDER_TITLE = "���a��";
    private static final String ORCA_VIEW = "ORCA View";
    private static final String ORCA_RECORD = "ORCA";
    private static final String[] COLUMN_TOOLTIPS = new String[]{null,
        "�N���b�N����ƃR���{�{�b�N�X�������オ��܂��B", "�N���b�N����ƃR���{�{�b�N�X�������オ��܂��B",
        "�E�N���b�N�ŃJ�����_���|�b�v�A�b�v���܂��B", "�E�N���b�N�ŃJ�����_���|�b�v�A�b�v���܂��B"};

    // GUI Component
    /** JTable�����_���p�̊�J���[ */
    private static final Color ODD_COLOR = ClientContext.getColor("color.odd");

    /** JTable�����_���p�̋����J���[ */
    private static final Color EVEN_COLOR = ClientContext.getColor("color.even");
    private static final Color ORCA_BACK = ClientContext.getColor("color.CALENDAR_BACK");

    private JButton addButton;                  // �V�K�a���G�f�B�^�{�^��
    private JButton updateButton;               // �������a���̓]�A���̍X�V�{�^��
    private JButton deleteButton;               // �������a���̍폜�{�^��
    private JButton orcaButton;                 // ORCA View �{�^��
    private JTable diagTable;                   // �a���e�[�u��
    private ListTableModel<RegisteredDiagnosisModel> tableModel; // TableModel
    private JComboBox extractionCombo;          // ���o���ԃR���{
    private JTextField countField;              // �����t�B�[���h

    // ���o���ԓ��� Dolphin �ɍŏ��ɕa���������
    // ORCA �̕a���͒��o���ԁ`dolphinFirstDate
    private String dolphinFirstDate;

    // �����~���t���O
    private boolean ascend;

    // �V�K�ɒǉ����ꂽ���a�����X�g
    List<RegisteredDiagnosisModel> addedDiagnosis;

    // �X�V���ꂽ���a�����X�g
    List<RegisteredDiagnosisModel> updatedDiagnosis;

    // ���a������
    private int diagnosisCount;

    /**
     *  Creates new DiagnosisDocument
     */
    public DiagnosisDocument() {
        setTitle(TITLE);
    }

    /**
     * GUI �R���|�[�l���g�𐶐�����������B
     */
    private void initialize() {

        // �R�}���h�{�^���p�l���𐶐�����
        JPanel cmdPanel = createButtonPanel2();

        // Dolphin ���a���p�l���𐶐�����
        JPanel dolphinPanel = createDignosisPanel();

        // ���o���ԃp�l���𐶐�����
        JPanel filterPanel = createFilterPanel();

        JPanel content = new JPanel(new BorderLayout(0, 7));
        content.add(cmdPanel, BorderLayout.NORTH);
        content.add(dolphinPanel, BorderLayout.CENTER);
        content.add(filterPanel, BorderLayout.SOUTH);
        //content.setBorder(BorderFactory.createTitledBorder(TABLE_BORDER_TITLE));

        // �S�̂����C�A�E�g����
        JPanel myPanel = getUI();
        myPanel.setLayout(new BorderLayout(0, 7));
        myPanel.add(content);
        myPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));

        // Preference ���珸���~����ݒ肷��
        ascend = Project.getPreferences().getBoolean(Project.DIAGNOSIS_ASCENDING, false);
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

            @Override
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
        tableModel = new ListTableModel<RegisteredDiagnosisModel>(columnNames, startNumRows, methodNames, columnClasses) {

            // Diagnosis�͕ҏW�s��
            @Override
            public boolean isCellEditable(int row, int col) {

                // licenseCode�Ő���
                if (isReadOnly()) {
                    return false;
                }

                // �a�����R�[�h�����݂��Ȃ��ꍇ�� false
                RegisteredDiagnosisModel entry = getObject(row);
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
            @Override
            public void setValueAt(Object value, int row, int col) {

                RegisteredDiagnosisModel entry = getObject(row);

                if (value == null || entry == null) {
                    return;
                }

                switch (col) {

                    case DIAGNOSIS_COL:
                        break;

                    case CATEGORY_COL:
                        // JComboBox ����I��
                        String saveCategory = entry.getCategory();
                        DiagnosisCategoryModel dcm = (DiagnosisCategoryModel) value;
                        String test = dcm.getDiagnosisCategory();
                        test = test != null && (!test.equals("")) ? test : null;
                        if (saveCategory != null) {
                            if (test != null) {
                                if (!test.equals(saveCategory)) {
                                    entry.setCategory(dcm.getDiagnosisCategory());
                                    entry.setCategoryDesc(dcm.getDiagnosisCategoryDesc());
                                    entry.setCategoryCodeSys(dcm.getDiagnosisCategoryCodeSys());
                                    fireTableRowsUpdated(row, row);
                                    addUpdatedList(entry);
                                }
                            } else {
                                entry.setDiagnosisCategoryModel(null);
                                fireTableRowsUpdated(row, row);
                                addUpdatedList(entry);
                            }
                            
                        } else {
                            if (test != null) {
                                entry.setCategory(dcm.getDiagnosisCategory());
                                entry.setCategoryDesc(dcm.getDiagnosisCategoryDesc());
                                entry.setCategoryCodeSys(dcm.getDiagnosisCategoryCodeSys());
                                fireTableRowsUpdated(row, row);
                                addUpdatedList(entry);
                            }
                        }
                        break;

                    case OUTCOME_COL:
                        // JComboBox ����I��
                        String saveOutcome = entry.getOutcome();
                        DiagnosisOutcomeModel dom = (DiagnosisOutcomeModel) value;
                        test = dom.getOutcome();
                        test = test != null && (!test.equals("")) ? test : null;
                        if (saveOutcome != null) {
                            if (test != null) {
                                if (!saveOutcome.equals(test)) {
                                    entry.setOutcome(dom.getOutcome());
                                    entry.setOutcomeDesc(dom.getOutcomeDesc());
                                    entry.setOutcomeCodeSys(dom.getOutcomeCodeSys());
                                    // �����I����������
                                    if (Project.getPreferences().getBoolean("autoOutcomeInput", false)) {
                                        String val = entry.getEndDate();
                                        if (val == null || val.equals("")) {
                                            GregorianCalendar gc = new GregorianCalendar();
                                            int offset = Project.getPreferences().getInt(Project.OFFSET_OUTCOME_DATE, -7);
                                            gc.add(Calendar.DAY_OF_MONTH, offset);
                                            String today = MMLDate.getDate(gc);
                                            entry.setEndDate(today);
                                        }
                                    }
                                    fireTableRowsUpdated(row, row);
                                    addUpdatedList(entry);
                                }
                            } else {
                                entry.setDiagnosisOutcomeModel(null);
                                fireTableRowsUpdated(row, row);
                                addUpdatedList(entry);
                            }
                        } else {
                            if (test != null) {
                                entry.setOutcome(dom.getOutcome());
                                entry.setOutcomeDesc(dom.getOutcomeDesc());
                                entry.setOutcomeCodeSys(dom.getOutcomeCodeSys());
                                // �����I����������
                                if (Project.getPreferences().getBoolean("autoOutcomeInput", false)) {
                                    String val = entry.getEndDate();
                                    if (val == null || val.equals("")) {
                                        GregorianCalendar gc = new GregorianCalendar();
                                        int offset = Project.getPreferences().getInt(Project.OFFSET_OUTCOME_DATE, -7);
                                        gc.add(Calendar.DAY_OF_MONTH, offset);
                                        String today = MMLDate.getDate(gc);
                                        entry.setEndDate(today);
                                    }
                                }
                                fireTableRowsUpdated(row, row);
                                addUpdatedList(entry);
                            }
                        }
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
                        String strVal = (String) value;
                        if (!strVal.trim().equals("")) {
                            entry.setStartDate(strVal);
                            fireTableRowsUpdated(row, row);
                            addUpdatedList(entry);
                        }
                        break;

                    case END_DATE_COL:
                        strVal = (String) value;
                        if (!strVal.trim().equals("")) {
                            entry.setEndDate((String) value);
                            fireTableRowsUpdated(row, row);
                            addUpdatedList(entry);
                        }
                        break;
                }
            }
        };

        // ���a���e�[�u���𐶐�����
        diagTable = new JTable(tableModel) {

            @Override
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    @Override
                    public String getToolTipText(MouseEvent e) {
                        String tip = null;
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        return COLUMN_TOOLTIPS[realIndex];
                    }
                };
            }
        };

        // �s��
        diagTable.setRowHeight(ClientContext.getMoreHigherRowHeight());

        // ��A�����s�̐F����������
        diagTable.setDefaultRenderer(Object.class, new DolphinOrcaRenderer());

        // ??
        diagTable.setSurrendersFocusOnKeystroke(true);

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
        TableColumn column = diagTable.getColumnModel().getColumn(CATEGORY_COL);
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
        tf.addFocusListener(AutoRomanListener.getInstance());
        new PopupListener(tf);
        tf.setDocument(new RegexConstrainedDocument(datePattern));
        DefaultCellEditor de = new DefaultCellEditor(tf);
        column.setCellEditor(de);
        int clickCountToStart = Project.getPreferences().getInt("diagnosis.table.clickCountToStart", 1);
        de.setClickCountToStart(clickCountToStart);

        column = diagTable.getColumnModel().getColumn(END_DATE_COL);
        tf = new JTextField();
        tf.addFocusListener(AutoRomanListener.getInstance());
        tf.setDocument(new RegexConstrainedDocument(datePattern));
        new PopupListener(tf);
        de = new DefaultCellEditor(tf);
        column.setCellEditor(de);
        de.setClickCountToStart(clickCountToStart);

        // �w�b�_�[�� ToolTip ��ݒ肷��
        //diagTable.getTableHeader().getCol

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
        NameValuePair[] extractionObjects = ClientContext.getNameValuePair("diagnosis.combo.period");
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
            RegisteredDiagnosisModel rd = tableModel.getObject(row);

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

    @Override
    public void start() {

        initialize();

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

    @Override
    public void stop() {
        if (tableModel != null) {
            tableModel.clear();
        }
    }

    @Override
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
    public void importStampList(final List<ModuleInfoBean> stampList, final int insertRow) {

        final StampDelegater sdl = new StampDelegater();

        DBTask task = new DBTask<List<StampModel>, Void>(getContext()) {

            @Override
            protected List<StampModel> doInBackground() throws Exception {
                List<StampModel> result = sdl.getStamp(stampList);
                return result;
            }

            @Override
            protected void succeeded(List<StampModel> list) {
                logger.debug("importStampList succeeded");
                if (sdl.isNoError() && list != null) {
                    for (int i = list.size() - 1; i > -1; i--) {
                        insertStamp((StampModel) list.get(i), insertRow);
                    }
                }
            }
        };

        task.execute();
    }

    /**
     * ���a���X�^���v���f�[�^�x�[�X����擾���e�[�u���֑}������B
     * Worker Thread �Ŏ��s�����B
     * @param stampInfo
     */
    private void insertStamp(StampModel sm, int row) {

        if (sm != null) {
            RegisteredDiagnosisModel module = (RegisteredDiagnosisModel) BeanUtils.xmlDecode(sm.getStampBytes());

            // �����̓��t�������J�n���Ƃ��Đݒ肷��
            GregorianCalendar gc = new GregorianCalendar();
            String today = MMLDate.getDate(gc);
            module.setStartDate(today);

            row = tableModel.getObjectCount() == 0 ? 0 : row;
            int cnt = tableModel.getObjectCount();
            if (row == 0 && cnt == 0) {
                tableModel.addObject(module);
            } else if (row < cnt) {
                tableModel.addObject(row, module);
            } else {
                tableModel.addObject(module);
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

        Window lock = SwingUtilities.getWindowAncestor(this.getUI());
        new StampEditor("diagnosis", this, lock);
    }

    /**
     * ���a���G�f�B�^����f�[�^���󂯎��e�[�u���֒ǉ�����B
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {

        ArrayList list = (ArrayList) e.getNewValue();
        if (list == null || list.size() == 0) {
            return;
        }

        int len = list.size();
        // �����̓��t�������J�n���Ƃ��Đݒ肷��
        GregorianCalendar gc = new GregorianCalendar();
        String today = MMLDate.getDate(gc);

        if (ascend) {
            // �����Ȃ̂Ńe�[�u���̍Ō�֒ǉ�����
            for (int i = 0; i < len; i++) {
                RegisteredDiagnosisModel module = (RegisteredDiagnosisModel) list.get(i);
                module.setStartDate(today);
                tableModel.addObject(module);
                addAddedList(module);
            }

        } else {
            // �~���Ȃ̂Ńe�[�u���̐擪�֒ǉ�����
            for (int i = len - 1; i > -1; i--) {
                RegisteredDiagnosisModel module = (RegisteredDiagnosisModel) list.get(i);
                module.setStartDate(today);
                tableModel.addObject(0, module);
                addAddedList(module);
            }
        }
    }
    
    private boolean isValidOutcome(RegisteredDiagnosisModel rd) {
        
        if (rd.getOutcome() == null) {
            return true;
        }
        
        String start = rd.getStartDate();
        String end = rd.getEndDate();
        
        if (start == null) {
            JOptionPane.showMessageDialog(
                    getContext().getFrame(),
                    "�����̊J�n��������܂���B",
                    ClientContext.getFrameTitle("�a���`�F�b�N"),
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (end == null) {
            JOptionPane.showMessageDialog(
                    getContext().getFrame(),
                    "�����̏I����������܂���B",
                    ClientContext.getFrameTitle("�a���`�F�b�N"),
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        Date startDate = null;
        Date endDate = null;
        boolean formatOk = true;
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            start = start.replaceAll("/", "-");
            end = end.replaceAll("/", "-");
            startDate = sdf.parse(start);
            endDate = sdf.parse(end);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("���t�̃t�H�[�}�b�g������������܂���B");
            sb.append("\n");
            sb.append("�uyyyy-MM-dd�v�̌`���œ��͂��Ă��������B");
            sb.append("\n");
            sb.append("�E�N���b�N�ŃJ�����_���g�p�ł��܂��B");
            JOptionPane.showMessageDialog(
                    getContext().getFrame(),
                    sb.toString(),
                    ClientContext.getFrameTitle("�a���`�F�b�N"),
                    JOptionPane.WARNING_MESSAGE);
            formatOk = false;
        }
        
        if (!formatOk) {
            return false;
        }
        
        if (endDate.before(startDate)) {
            StringBuilder sb = new StringBuilder();
            sb.append("�����̏I�������J�n���ȑO�ɂȂ��Ă��܂��B");
            JOptionPane.showMessageDialog(
                    getContext().getFrame(),
                    sb.toString(),
                    ClientContext.getFrameTitle("�a���`�F�b�N"),
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }

    /**
     * �V�K�y�ѕύX���ꂽ���a����ۑ�����B
     */
    @Override
    public void save() {

        if ((addedDiagnosis == null || addedDiagnosis.size() == 0) &&
                (updatedDiagnosis == null || updatedDiagnosis.size() == 0)) {
            return;
        }

        final boolean sendDiagnosis = Project.getSendDiagnosis() && ((ChartImpl) getContext()).getCLAIMListener() != null ? true : false;
        logger.debug("sendDiagnosis = " + sendDiagnosis);

        // continue to save
        Date confirmed = new Date();
        logger.debug("confirmed = " + confirmed);
        
        boolean go = true;

        if (addedDiagnosis != null && addedDiagnosis.size() > 0) {

            for (RegisteredDiagnosisModel rd : addedDiagnosis) {
                
                logger.debug("added rd = " + rd.getDiagnosis());
                logger.debug("id = " + rd.getId());

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
                
                // �]�A���`�F�b�N����
                if (!isValidOutcome(rd)) {
                    go = false;
                    break;
                }
            }
        }
        
        if (!go) {
            return;
        }

        if (updatedDiagnosis != null && updatedDiagnosis.size() > 0) {

            for (RegisteredDiagnosisModel rd : updatedDiagnosis) {
                
                logger.debug("updated rd = " + rd.getDiagnosis());
                logger.debug("id = " + rd.getId());

                // ���o�[�W�����͏㏑�����Ă���
                rd.setCreator(Project.getUserModel());
                rd.setConfirmed(confirmed);
                rd.setRecorded(confirmed);
                rd.setStatus(IInfoModel.STATUS_FINAL);

                // TODO �g���t�B�b�N
                rd.setPatientLiteModel(getContext().getPatient().patientAsLiteModel());
                rd.setUserLiteModel(Project.getUserModel().getLiteModel());
                
                // �]�A���`�F�b�N����
                if (!isValidOutcome(rd)) {
                    go = false;
                    break;
                }
            }
        }
        
        if (!go) {
            return;
        }

        DocumentDelegater ddl = new DocumentDelegater();
        DiagnosisPutTask task = new DiagnosisPutTask(getContext(), addedDiagnosis, updatedDiagnosis, sendDiagnosis, ddl);
        task.execute();
    }

    /**
     * �w����Ԉȍ~�̏��a�����������ăe�[�u���֕\������B
     * �o�b�O�O�����h�X���b�h�Ŏ��s�����B
     */
    public void getDiagnosisHistory(Date past) {

        final DiagnosisSearchSpec spec = new DiagnosisSearchSpec();
        spec.setCode(DiagnosisSearchSpec.PATIENT_SEARCH);
        spec.setKarteId(getContext().getKarte().getId());
        if (past != null) {
            spec.setFromDate(past);
        }

        final DocumentDelegater ddl = new DocumentDelegater();

        DBTask task = new DBTask<List<RegisteredDiagnosisModel>, Void>(getContext()) {

            @Override
            protected List<RegisteredDiagnosisModel> doInBackground() throws Exception {
                logger.debug("getDiagnosisHistory doInBackground");
                List<RegisteredDiagnosisModel> result = ddl.getDiagnosisList(spec);
                return result;
            }

            @Override
            protected void succeeded(List<RegisteredDiagnosisModel> list) {
                logger.debug("getDiagnosisHistory succeeded");
                if (ddl.isNoError() && list != null && list.size() > 0) {
                    if (ascend) {
                        Collections.sort(list);
                        RegisteredDiagnosisModel rd = (RegisteredDiagnosisModel) list.get(0);
                        dolphinFirstDate = rd.getStartDate();
                    } else {
                        Collections.sort(list, Collections.reverseOrder());
                        int index = list.size() - 1;
                        RegisteredDiagnosisModel rd = (RegisteredDiagnosisModel) list.get(index);
                        dolphinFirstDate = rd.getStartDate();
                    }
                    tableModel.setDataProvider(list);
                    setDiagnosisCount(list.size());
                }
            }
        };

        task.execute();
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
                tableModel.deleteAt(row);
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

        final DocumentDelegater ddl = new DocumentDelegater();

        DBTask task = new DBTask<Void, Void>(getContext()) {

            @Override
            protected Void doInBackground() throws Exception {
                logger.debug("delete doInBackground");
                ddl.removeDiagnosis(list);
                return null;
            }

            @Override
            protected void succeeded(Void result) {
                logger.debug("delete succeeded");
                if (ddl.isNoError()) {
                    tableModel.deleteAt(row);
                    setDiagnosisCount(tableModel.getObjectCount());
                    // �X�V���X�g�ɂ���ꍇ
                    // �X�V���X�g�����菜��
                    if (updatedDiagnosis != null) {
                        updatedDiagnosis.remove(model);
                        controlUpdateButton();
                    }
                }
            }
        };

        task.execute();
    }

    /**
     * ORCA�ɓo�^����Ă���a������荞�ށB�i�e�[�u���֒ǉ�����j 
     * ������A�{�^���� disabled �ɂ���B
     */
    public void viewOrca() {

        // ����ID���擾����
        final String patientId = getContext().getPatient().getPatientId();

        // ���o���Ԃ��猟���͈͂̍ŏ��̓����擾����
        NameValuePair pair = (NameValuePair) extractionCombo.getSelectedItem();
        int past = Integer.parseInt(pair.getValue());

        // �����J�n��
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
            date = new Date();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        final String from = sdf.format(date);

        // �����I����=����
        final String to = sdf.format(new Date());
        logger.debug("from = " + from);
        logger.debug("to = " + to);

        DBTask task = new DBTask<List<RegisteredDiagnosisModel>, Void>(getContext()) {

            @Override
            protected List<RegisteredDiagnosisModel> doInBackground() throws Exception {
                SqlOrcaView dao = new SqlOrcaView();
                List<RegisteredDiagnosisModel> result = dao.getOrcaDisease(patientId, from, to, new Boolean(ascend));
                if (dao.isNoError()) {
                    return result;
                } else {
                    throw new Exception(dao.getErrorMessage());
                }
            }

            @Override
            protected void succeeded(List<RegisteredDiagnosisModel> result) {
                if (result != null && result.size() > 0) {
                    if (ascend) {
                        Collections.sort(result);
                    } else {
                        Collections.sort(result, Collections.reverseOrder());
                    }
                    tableModel.addAll(result);
                }
                orcaButton.setEnabled(true);
            }
        };

        task.execute();
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

        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {

            if (e.isPopupTrigger()) {
                popup = new JPopupMenu();
                CalendarCardPanel cc = new CalendarCardPanel(ClientContext.getEventColorTable());
                cc.addPropertyChangeListener(CalendarCardPanel.PICKED_DATE, this);
                cc.setCalendarRange(new int[]{-12, 0});
                popup.insert(cc, 0);
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        @Override
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
     * DiagnosisPutTask
     */
    class DiagnosisPutTask extends DBTask<List<Long>, Void> {

        //private Chart chart;
        private List<RegisteredDiagnosisModel> added;
        private List<RegisteredDiagnosisModel> updated;
        private boolean sendClaim;
        private DocumentDelegater ddl;

        public DiagnosisPutTask(
                Chart chart,
                List<RegisteredDiagnosisModel> added,
                List<RegisteredDiagnosisModel> updated,
                boolean sendClaim,
                DocumentDelegater ddl) {

            super(chart);
            this.added = added;
            this.updated = updated;
            this.sendClaim = sendClaim;
            this.ddl = ddl;
        }

        @Override
        protected List<Long> doInBackground() throws Exception {
            
            logger.debug("doInBackground");

            // �X�V����
            if (updated != null && updated.size() > 0) {
                logger.debug("ddl.updateDiagnosis");
                ddl.updateDiagnosis(updated);
            }

            List<Long> result = null;

            // �ۑ�����
            if (added != null && added.size() > 0) {
                logger.debug("ddl.putDiagnosis");
                result = ddl.putDiagnosis(added);
                if (ddl.isNoError()) {
                    logger.debug("ddl.putDiagnosis() is NoErr");
                    for (int i = 0; i < added.size(); i++) {
                        long pk = result.get(i).longValue();
                        logger.debug("persist id = " + pk);
                        RegisteredDiagnosisModel rd = (RegisteredDiagnosisModel) added.get(i);
                        rd.setId(pk);
                    }
                }
            }

            //
            // �ǉ��a���� CLAIM ���M����
            //
            if (sendClaim && added != null && added.size() > 0) {
                
                logger.debug("sendClaim Diagnosis");

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
                //UserLiteModel creator = added.get(0).getUserLiteModel();
                PatientLiteModel patient = added.get(0).getPatientLiteModel();

                // �w���p�[�N���X�𐶐�����
                logger.debug("create DiseaseHelper");
                DiseaseHelper dhl = new DiseaseHelper();
                
                dhl.setPatientId(patient.getPatientId());
                logger.debug("setPatientId = " + dhl.getPatientId());
                
                dhl.setConfirmDate(confirmDate);
                logger.debug("setConfirmDate = " + dhl.getConfirmDate());
                
                dhl.setDiagnosisModuleItems(moduleItems);
                
                dhl.setGroupId(GUIDGenerator.generate(dhl));
                logger.debug("setGroupId = " + dhl.getGroupId());
                
                dhl.setDepartment(context.getPatientVisit().getDepartmentCode());
                logger.debug("dhl.setDepartment = " + dhl.getDepartment());
                
                dhl.setDepartmentDesc(context.getPatientVisit().getDepartment());
                logger.debug("dhl.setDepartmentDesc = " + dhl.getDepartmentDesc());
                
                dhl.setCreatorName(context.getPatientVisit().getAssignedDoctorName());
                logger.debug("dhl.setCreatorName = " + dhl.getCreatorName());
                
                dhl.setCreatorId(context.getPatientVisit().getAssignedDoctorId());
                logger.debug("dhl.setCreatorId = " + dhl.getCreatorId());
                
                dhl.setCreatorLicense(Project.getUserModel().getLicenseModel().getLicense());
                logger.debug("dhl.setCreatorLicense = " + dhl.getCreatorLicense());
                
                dhl.setFacilityName(Project.getUserModel().getFacilityModel().getFacilityName());
                logger.debug("dhl.setFacilityName = " + dhl.getFacilityName());
                
                dhl.setJmariCode(context.getPatientVisit().getJmariCode());
                logger.debug("dhl.setJmariCode = " + dhl.getJmariCode());

                MessageBuilder mb = new MessageBuilder();
                logger.debug("MessageBuilder created");
                String claimMessage = mb.build(dhl);
                logger.debug("claimMessage builded" + claimMessage);
                
                if (ClientContext.getClaimLogger() != null) {
                    ClientContext.getClaimLogger().debug(claimMessage);
                }
                ClaimMessageEvent event = new ClaimMessageEvent(this);
                event.setPatientId(patient.getPatientId());
                event.setPatientName(patient.getName());
                event.setPatientSex(patient.getGender());
                event.setTitle(IInfoModel.DEFAULT_DIAGNOSIS_TITLE);
                event.setClaimInstance(claimMessage);
                event.setConfirmDate(confirmDate);
                ClaimMessageListener claimListener = ((ChartImpl) context).getCLAIMListener();
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
                //UserLiteModel creator = updated.get(0).getUserLiteModel();
                PatientLiteModel patient = updated.get(0).getPatientLiteModel();

                // �w���p�[�N���X�𐶐�����
                DiseaseHelper dhl = new DiseaseHelper();
                dhl.setPatientId(patient.getPatientId());
                dhl.setConfirmDate(confirmDate);
                dhl.setDiagnosisModuleItems(moduleItems);
                dhl.setGroupId(GUIDGenerator.generate(dhl));
                dhl.setDepartment(context.getPatientVisit().getDepartmentCode());
                dhl.setDepartmentDesc(context.getPatientVisit().getDepartment());
                dhl.setCreatorName(context.getPatientVisit().getAssignedDoctorName());
                dhl.setCreatorId(context.getPatientVisit().getAssignedDoctorId());
                dhl.setCreatorLicense(Project.getUserModel().getLicenseModel().getLicense());
                dhl.setFacilityName(Project.getUserModel().getFacilityModel().getFacilityName());
                dhl.setJmariCode(context.getPatientVisit().getJmariCode());

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
                ClaimMessageListener claimListener = ((ChartImpl) context).getCLAIMListener();
                if (claimListener != null) {
                    claimListener.claimMessageEvent(event);
                }
            }

            return result;
        }

        @Override
        protected void succeeded(List<Long> list) {
            logger.debug("DiagnosisPutTask succeeded");
            clearDiagnosisList();
        }
    }

    /**
     *
     */
    class DolphinOrcaRenderer extends DefaultTableCellRenderer {

        /** Creates new IconRenderer */
        public DolphinOrcaRenderer() {
            super();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean isFocused,
                int row, int col) {

            RegisteredDiagnosisModel rd = (RegisteredDiagnosisModel) tableModel.getObject(row);

            // ORCA ���R�[�h���ǂ����𔻒肷��
            boolean orca = (rd != null && rd.getStatus() != null && rd.getStatus().equals(ORCA_RECORD)) ? true : false;

            if (isSelected) {
                // �I������Ă��鎞�̓f�t�H���g�̕\�����s��
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                // �I������Ă��Ȃ���
                // Foreground ���f�t�H���g�ɂ���
                // ORCA �f�[�^�̎��͔w�i��ς���
                // ����ȊO�͊�Ƌ����ŐF��������
                setForeground(table.getForeground());
                if (orca) {
                    setBackground(ORCA_BACK);

                } else {

                    if (row % 2 == 0) {
                        setBackground(EVEN_COLOR);
                    } else {
                        setBackground(ODD_COLOR);
                    }
                }
            }

            if (value != null) {
                if (value instanceof String) {
                    this.setText((String) value);
                } else {
                    this.setText(value.toString());
                }
            } else {
                this.setText("");
            }

            return this;
        }
    }
}
