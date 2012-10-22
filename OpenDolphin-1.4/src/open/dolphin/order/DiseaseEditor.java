package open.dolphin.order;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import open.dolphin.client.AutoKanjiListener;
import open.dolphin.client.AutoRomanListener;
import open.dolphin.client.ClientContext;
import open.dolphin.dao.SqlDaoFactory;
import open.dolphin.dao.SqlMasterDao;
import open.dolphin.infomodel.DiseaseEntry;
import open.dolphin.table.OddEvenRowRenderer;
import open.dolphin.infomodel.RegisteredDiagnosisModel;
import open.dolphin.infomodel.TensuMaster;
import open.dolphin.project.Project;
import open.dolphin.table.ListTableModel;

/**
 * ���a���ҏW�e�[�u���N���X�B
 *
 * @author Kazushi Minagawa
 */
public class DiseaseEditor extends AbstractStampEditor {
    
    // ���a���̏C����R�[�h
    private static final String MODIFIER_CODE = "ZZZ";
    
    // ���a������͎��ɂ���R�[�h
    private static final String HAND_CODE = "0000999";
    
    // Diagnosis table �̃p�����[�^
    private static final int NAME_COL       = 1;
    private static final int ALIAS_COL      = 2;
    private static final int DISEASE_NUM_ROWS = 10;
    
    //private static final String TOOLTIP_TABLE  = "�R�[�h�̃J������ Drag & Drop �ŏ��Ԃ����ւ��邱�Ƃ��ł��܂�";
    private static final String TOOLTIP_COMBINE  = "�e�[�u���̍s��A�����ďC����t���̏��a���ɂ��܂�";
    
    // Table model
    private DiseaseView view;

    private ListTableModel<RegisteredDiagnosisModel> tableModel;

    private ListTableModel<DiseaseEntry> searchResultModel;


    @Override
    public JPanel getView() {
        return view;
    }

    @Override
    public void dispose() {

        if (tableModel != null) {
            tableModel.clear();
        }

        if (searchResultModel != null) {
            searchResultModel.clear();
        }

        super.dispose();
    }


    @Override
    public Object getValue() {

        if (hasModifier()) {
            return getValue1();
        } else {
            return getValue2();
        }
    }

    /**
     * ���a���e�[�u�����X�L�������C������̏��a�ɂ��ĕԂ��B
     */
    private Object getValue1() {

        RegisteredDiagnosisModel diagnosis = null;

        StringBuilder name = new StringBuilder();
        StringBuilder code = new StringBuilder();

        // �e�[�u�����X�L��������
        int count = tableModel.getObjectCount();
        for (int i = 0; i < count; i++) {

            RegisteredDiagnosisModel diag = tableModel.getObject(i);
            String diagCode = diag.getDiagnosisCode();

            if (!diagCode.startsWith(MODIFIER_CODE)) {
                //
                // �C����łȂ��ꍇ�͊�{�a���ƌ��Ȃ��A�p�����[�^��ݒ肷��
                //
                diagnosis = new RegisteredDiagnosisModel();
                diagnosis.setDiagnosisCodeSystem(diag.getDiagnosisCodeSystem());

            } else {
                // ZZZ ���g�������� ORCA ����
                diagCode = diagCode.substring(MODIFIER_CODE.length());
            }

            // �R�[�h�� . �ŘA������
            if (code.length() > 0) {
                code.append(".");
            }
            code.append(diagCode);

            // ���O��A������
            name.append(diag.getDiagnosis());

        }

        if (diagnosis != null && name.length() > 0 && code.length() > 0) {

            // ���O�ƃR�[�h��ݒ肷��
            diagnosis.setDiagnosis(name.toString());
            diagnosis.setDiagnosisCode(code.toString());
            ArrayList<RegisteredDiagnosisModel> ret = new ArrayList<RegisteredDiagnosisModel>(1);
            ret.add(diagnosis);

            return ret;

        } else {
            return null;
        }
    }

    /**
     * ���a���e�[�u�����X�L�������C������̏��a�ɂ��ĕԂ��B
     */
    private Object getValue2() {

        return tableModel.getDataProvider();
    }

    @Override
    public void setValue(Object o) {
        clear();
    }
    

    @Override
    protected void checkValidation() {

        setIsEmpty = tableModel.getObjectCount() == 0 ? true : false;

        setIsValid = true;

        int diseaseCnt = 0;
        List<RegisteredDiagnosisModel> itemList = tableModel.getDataProvider();

        for (RegisteredDiagnosisModel diag : itemList) {

            if (diag.getDiagnosisCode().startsWith(MODIFIER_CODE)) {
                continue;

            } else {
                diseaseCnt++;
            }
        }

        setIsValid = setIsValid && (diseaseCnt > 0);

        // ButtonControl
        view.getClearBtn().setEnabled(!setIsEmpty);
        view.getOkCntBtn().setEnabled(setIsValid && getFromStampEditor());
        view.getOkBtn().setEnabled(setIsValid && getFromStampEditor());

        // ���a���`�F�b�N�{�b�N�X
        view.getDiseaseCheck().setSelected((diseaseCnt > 0));

        // �ʒm����
        super.checkValidation();
    }

    @Override
    protected void addSelectedTensu(TensuMaster tm) {
        // No use
    }

    @Override
    protected void search(final String text) {

        boolean pass = true;
        pass = pass && ipOk();
        pass = pass && (text.length() > 1);

        if (!pass) {
            return;
        }

        // �������[���ɂ��Ă���
        view.getCountField().setText("0");

        // ���������s����
        SwingWorker worker = new SwingWorker<List<DiseaseEntry>, Void>() {

            @Override
            protected List<DiseaseEntry> doInBackground() throws Exception {
                SqlMasterDao dao = (SqlMasterDao) SqlDaoFactory.create("dao.master");
                String d = effectiveFormat.format(new Date());
                List<DiseaseEntry> result = dao.getDiseaseByName(text, d);
                if (!dao.isNoError()) {
                    throw new Exception(dao.getErrorMessage());
                }
                return result;
            }

            @Override
            protected void done() {
                try {
                    List<DiseaseEntry> result = get();
                    searchResultModel.setDataProvider(result);
                    int cnt = searchResultModel.getObjectCount();
                    view.getCountField().setText(String.valueOf(cnt));
                } catch (InterruptedException ex) {

                } catch (ExecutionException ex) {
                    alertSearchError(ex.getMessage());
                }
            }
        };

        worker.execute();
    }

    @Override
    protected void clear() {
        tableModel.clear();
        view.getStampNameField().setText("");
        checkValidation();
    }

    @Override
    protected void initComponents() {

        view = new DiseaseView();
        
        // �e�[�u���̃J���������擾����
        String[] diganosisColumns = new String[]{
            "�R�[�h", "������/�C����", "�G�C���A�X"
        };

        // �e�[�u���̃J���������擾����
        String[] methodNames = new String[]{
            "getDiagnosisCode", "getDiagnosisName", "getDiagnosisAlias"
        };
        
        // �a���e�[�u���𐶐�����
        tableModel = new ListTableModel<RegisteredDiagnosisModel>(diganosisColumns, DISEASE_NUM_ROWS, methodNames, null) {
            
            // �a���J�������C����̕ҏW���\
            @Override
            public boolean isCellEditable(int row, int col) {
                
                boolean ret = false;
                
                RegisteredDiagnosisModel model = getObject(row);
                
                if (col == NAME_COL) {
                    if (model == null) {
                        ret = true;
                    } else if (!model.getDiagnosisCode().startsWith(MODIFIER_CODE)) {
                        ret = true;
                    }
                    
                } else if (col == ALIAS_COL) {
                    if (model != null && (!model.getDiagnosisCode().startsWith(MODIFIER_CODE))) {
                        ret = true;
                    }
                }
                
                return ret;
            }
            
            @Override
            public void setValueAt(Object o, int row, int col) {
                
                if (o == null) {
                    return;
                }
                
                int index = ((String)o).indexOf(',');
                if (index > 0) {
                    return;
                }
                
                RegisteredDiagnosisModel model = getObject(row);
                String value = (String) o;
                
                switch (col) {
                    
                    case NAME_COL:
                        //
                        // �a��������͂��ꂽ�ꍇ�́A�R�[�h�� 0000999 ��ݒ肷��
                        //
                        if (!value.equals("")) {
                            if (model != null) {
                                model.setDiagnosis(value);
                                model.setDiagnosisCode(HAND_CODE);
                                fireTableCellUpdated(row, col);

                            } else {
                                model = new RegisteredDiagnosisModel();
                                model.setDiagnosis(value);
                                model.setDiagnosisCode(HAND_CODE);
                                addObject(model);
                                checkValidation();
                            }
                        }
                        break;
                        
                    case ALIAS_COL:
                        //
                        // �G�C���A�X�̓��͂��������ꍇ
                        //
                        if (model != null) {
                            String test = model.getDiagnosis();
                            int idx = test.indexOf(',');
                            if (idx >0 ) {
                                test = test.substring(0, idx);
                                test = test.trim();
                            }
                            if (value.equals("")) {
                                model.setDiagnosis(test);
                            } else {
                                StringBuilder sb = new StringBuilder();
                                sb.append(test);
                                sb.append(",");
                                sb.append(value);
                                model.setDiagnosis(sb.toString());
                            }
                        }
                        break;
                }
            }
        };
        
        // SetTable �𐶐��� transferHandler �𐶐�����
        JTable table = view.getSetTable();
        table.setModel(tableModel);

        // Set Table �̍s�̍���
        table.setRowHeight(ClientContext.getMoreHigherRowHeight());

        table.setTransferHandler(new RegisteredDiagnosisTransferHandler(DiseaseEditor.this)); // TransferHandler
        table.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int ctrlMask = InputEvent.CTRL_DOWN_MASK;
                int action = ((e.getModifiersEx() & ctrlMask) == ctrlMask)
                            ? TransferHandler.COPY
                            : TransferHandler.MOVE;
                JComponent c = (JComponent) e.getSource();
                TransferHandler handler = c.getTransferHandler();
                handler.exportAsDrag(c, e, action);
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        ListSelectionModel m = table.getSelectionModel();
        m.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    int row = view.getSetTable().getSelectedRow();
                    if (tableModel.getObject(row)!= null) {
                        view.getDeleteBtn().setEnabled(true);
                    } else {
                        view.getDeleteBtn().setEnabled(false);
                    }
                }
            }
        });
        table.setDefaultRenderer(Object.class, new OddEvenRowRenderer());

        // CellEditor ��ݒ肷��
        // ������
        TableColumn column = table.getColumnModel().getColumn(NAME_COL);
        JTextField nametf = new JTextField();
        nametf.addFocusListener(AutoKanjiListener.getInstance());
        DefaultCellEditor nameEditor = new DefaultCellEditor(nametf);
        int clickCountToStart = Project.getPreferences().getInt("diagnosis.table.clickCountToStart", 1);
        nameEditor.setClickCountToStart(clickCountToStart);
        column.setCellEditor(nameEditor);

        // �a���G�C���A�X
        column = table.getColumnModel().getColumn(ALIAS_COL);
        JTextField aliastf = new JTextField();
        aliastf.addFocusListener(AutoRomanListener.getInstance()); // alias 
        DefaultCellEditor aliasEditor = new DefaultCellEditor(aliastf);
        aliasEditor.setClickCountToStart(clickCountToStart);
        column.setCellEditor(aliasEditor);
        
        // �񕝐ݒ�
        int[] columnWidth = new int[]{20, 135, 135};
        int len = columnWidth.length;
        for (int i = 0; i < len; i++) {
            column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(columnWidth[i]);
        }

        //
        // �a���}�X�^�������ʃe�[�u��
        //
        String[] srColumnNames = new String[]{"�R�[�h", "�� ��", "�J�i", "ICD10"};
        String[] srMthodNames = new String[]{"getCode", "getName", "getKana", "getIcdTen"};
        int[] srColumnWidth = new int[]{10, 135, 135, 10};

        searchResultModel = new ListTableModel<DiseaseEntry>(srColumnNames, 20, srMthodNames, null);

        JTable searchResultTable = view.getSearchResultTable();
        searchResultTable.setModel(searchResultModel);
        searchResultTable.setRowHeight(ClientContext.getHigherRowHeight());
        searchResultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchResultTable.setRowSelectionAllowed(true);
        ListSelectionModel lm = searchResultTable.getSelectionModel();
        lm.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {

                if (e.getValueIsAdjusting() == false) {

                    int row = view.getSearchResultTable().getSelectedRow();

                    DiseaseEntry o = searchResultModel.getObject(row);

                    if (o != null) {

                        String codeSystem = ClientContext.getString("mml.codeSystem.diseaseMaster");
                        RegisteredDiagnosisModel model = new RegisteredDiagnosisModel();
                        model.setDiagnosis(o.getName());
                        model.setDiagnosisCode(o.getCode());
                        model.setDiagnosisCodeSystem(codeSystem);
                        tableModel.addObject(model);
                        reconstractDiagnosis();
                        checkValidation();
                    }
                }
            }
        });

        column = null;
        len = srColumnWidth.length;
        for (int i = 0; i < len; i++) {
            column = searchResultTable.getColumnModel().getColumn(i);
            column.setPreferredWidth(srColumnWidth[i]);
        }
        searchResultTable.setDefaultRenderer(Object.class, new OddEvenRowRenderer());
        
        // �����a���t�B�[���h
        JTextField combinedDiagnosis = view.getStampNameField();
        combinedDiagnosis.setEditable(false);
        combinedDiagnosis.setToolTipText(TOOLTIP_COMBINE);
        
        // �����t�B�[���h
        DocumentListener dl = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (view.getRtBtn().isSelected()) {
                    search(view.getSearchTextField().getText().trim());
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (view.getRtBtn().isSelected()) {
                    search(view.getSearchTextField().getText().trim());
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (view.getRtBtn().isSelected()) {
                    search(view.getSearchTextField().getText().trim());
                }
            }
        };
        searchTextField = view.getSearchTextField();
        searchTextField.getDocument().addDocumentListener(dl);
        searchTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                search(view.getSearchTextField().getText().trim());
            }
        });
        searchTextField.addFocusListener(AutoKanjiListener.getInstance());

        // Real Time Search
        boolean rt = Project.getPreferences().getBoolean("masterSearch.realTime", true);
        view.getRtBtn().setSelected(rt);
        view.getRtBtn().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                Project.getPreferences().putBoolean("masterSearch.realTime", view.getRtBtn().isSelected());
            }
        });

        // �����t�B�[���h
        countField = view.getCountField();

        // OK & �A���{�^��
        view.getOkCntBtn().setEnabled(false);
        view.getOkCntBtn().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boundSupport.firePropertyChange(VALUE_PROP, null, getValue());
                clear();
            }
        });


        // OK �{�^��
        view.getOkBtn().setEnabled(false);
        view.getOkBtn().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boundSupport.firePropertyChange(VALUE_PROP, null, getValue());
                dispose();
                boundSupport.firePropertyChange(EDIT_END_PROP, false, true);
            }
        });

        // �폜�{�^��
        view.getDeleteBtn().setEnabled(false);
        view.getDeleteBtn().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = view.getSetTable().getSelectedRow();
                if (tableModel.getObject(row) != null) {
                    tableModel.deleteAt(row);
                    checkValidation();
                }
            }
        });

        // �N���A�{�^��
        view.getClearBtn().setEnabled(false);
        view.getClearBtn().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clear();
            }
        });
    }

    /**
     * �e�[�u�����X�L�������A���a���R���|�W�b�g����B
     */
    public void reconstractDiagnosis() {
        
        if (hasModifier()) {
            StringBuilder sb = new StringBuilder();
            int count = tableModel.getObjectCount();
            for (int i = 0; i < count; i++) {
                RegisteredDiagnosisModel diag = (RegisteredDiagnosisModel) tableModel.getObject(i);
                sb.append(diag.getDiagnosis());
            }
            view.getStampNameField().setText(sb.toString());
        } else {
            view.getStampNameField().setText("");
        }
    }
    
    /**
     * �C������ӂ���ł��邩�ǂ�����Ԃ��B
     */
    private boolean hasModifier() {
        boolean hasModifier = false;
        int count = tableModel.getObjectCount();
        for (int i = 0; i < count; i++) {
            RegisteredDiagnosisModel diag = (RegisteredDiagnosisModel) tableModel.getObject(i);
            if (diag.getDiagnosisCode().startsWith(MODIFIER_CODE)) {
                hasModifier = true;
                break;
            }
        }
        return hasModifier;
    }

    public DiseaseEditor() {
        this(true);
    }

    public DiseaseEditor(boolean mode) {
        super();
        this.setFromStampEditor(mode);
        this.setOrderName("���a��");
    }
}

