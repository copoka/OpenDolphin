package open.dolphin.order;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import open.dolphin.client.AutoKanjiListener;
import open.dolphin.client.AutoRomanListener;
import open.dolphin.client.ClientContext;
import open.dolphin.client.IStampModelEditor;
import open.dolphin.table.OddEvenRowRenderer;
import open.dolphin.infomodel.RegisteredDiagnosisModel;
import open.dolphin.project.Project;
import open.dolphin.table.ObjectTableModel;

/**
 * ���a���ҏW�e�[�u���N���X�B
 *
 * @author Kazushi Minagawa
 */
public class DiagnosisTablePanel extends JPanel implements PropertyChangeListener {
    
    // ���a���̏C����R�[�h
    private static final String MODIFIER_CODE = "ZZZ";
    
    // ���a������͎��ɂ���R�[�h
    private static final String HAND_CODE = "0000999";
    
    // Diagnosis table �̃p�����[�^
    private static final int CODE_COL            = 0;
    private static final int NAME_COL            = 1;
    private static final int ALIAS_COL           = 2;
    private static final int[] DIAGNOSIS_TABLE_COLUMN_WIDTHS = {
        150, 200, 200
    };
    private static final int START_NUM_ROWS = 10;
    private static final String REMOVE_BUTTON_IMAGE = "del_16.gif";
    private static final String CLEAR_BUTTON_IMAGE  = "remov_16.gif";
    private static final String INFO_BUTTON_IMAGE   = "about_16.gif";
    private static final int TABLE_WIDTH = 890;
    private static final int TABLE_HEIGHT = 90;
    
    private static final String TOOLTIP_REMOVE = "�I���������a�����폜���܂�";
    private static final String TOOLTIP_CLEAR  = "�e�[�u�����N���A���܂�";
    private static final String TOOLTIP_TABLE  = "�R�[�h�̃J������ Drag & Drop �ŏ��Ԃ����ւ��邱�Ƃ��ł��܂�";
    private static final String TOOLTIP_COMBINE  = "�e�[�u���̍s��A�����ďC����t���̏��a���ɂ��܂�";
    
    // �C����t�����a�� �\�����x��
    private static final String LABEL_COMBINED_DIAGNOSIS = "�A���������a��:";
    
    // �}�X�^�����̑I���A�C�e���v���p�e�B
    private static final String SELECTED_ITEM_PROP = "selectedItemProp";
    
    // �����a���\���t�B�[���h�̒���
    private static final int COMBINED_FIELD_LENGTH = 20;
    
    // Table model
    private ObjectTableModel tableModel;
    
    // ���a���ҏW�e�[�u��
    private JTable table;
    
    // �폜�{�^��
    private JButton removeButton;
    
    //�N���A�{�^��
    private JButton clearButton;
    
    // �����a����\������t�B�[���h
    private JTextField combinedDiagnosis;
    
    // State ��\�����郉�x�� 
    private JLabel stateLabel;
    
    // Stamp Editor
    private IStampModelEditor context;
    
    // ��ԃ}�V��
    private DiagnosisStateMgr curState;
    
    
    /**
     * DiagnosisTablePanel�𐶐�����B
     */
    public DiagnosisTablePanel(IStampModelEditor context) {
        
        super(new BorderLayout());
        
        setContext(context);
        
        // �e�[�u���̃J���������擾����
        String[] diganosisColumns = new String[]{
            "�R�[�h", "������/�C����", "�G�C���A�X"
        };
        
        // �e�[�u�����f���𐶐�����
        tableModel = new ObjectTableModel(diganosisColumns, START_NUM_ROWS) {
            
            // �a���J�������C����̕ҏW���\
            @Override
            public boolean isCellEditable(int row, int col) {
                
                boolean ret = false;
                
                RegisteredDiagnosisModel model = (RegisteredDiagnosisModel) getObject(row);
                
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
            public Object getValueAt(int row, int col) {
                
                RegisteredDiagnosisModel model = (RegisteredDiagnosisModel) getObject(row);
                
                if (model == null) {
                    return null;
                }
                
                String ret = null;
                
                switch (col) {
                    
                    case CODE_COL:
                        ret = model.getDiagnosisCode();
                        break;
                    
                    case NAME_COL:
                        ret = model.getDiagnosisName();
                        break;
                        
                    case ALIAS_COL:
                        ret = model.getDiagnosisAlias();
                        break;
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
                
                
                RegisteredDiagnosisModel model = (RegisteredDiagnosisModel) getObject(row);
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
                                addRow(model);
                                curState.processEvent(DiagnosisStateMgr.Event.ADDED);
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
        
        // Table �𐶐��� transferHandler �𐶐�����
        table = new JTable(tableModel);
        table.setTransferHandler(new RegisteredDiagnosisTransferHandler(DiagnosisTablePanel.this)); // TransferHandler
        table.addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                int ctrlMask = InputEvent.CTRL_DOWN_MASK;
                int action = ((e.getModifiersEx() & ctrlMask) == ctrlMask)
                            ? TransferHandler.COPY
                            : TransferHandler.MOVE;
                JComponent c = (JComponent) e.getSource();
                TransferHandler handler = c.getTransferHandler();
                handler.exportAsDrag(c, e, action);
            }
            
            public void mouseMoved(MouseEvent e) {
            }
        });
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        ListSelectionModel m = table.getSelectionModel();
        m.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    curState.processEvent(DiagnosisStateMgr.Event.SELECTED);
                }
            }
        });
        table.setToolTipText(TOOLTIP_TABLE);
        table.setDefaultRenderer(Object.class, new OddEvenRowRenderer());
        table.setPreferredSize(new Dimension(TABLE_WIDTH,TABLE_HEIGHT));
        
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
        int len = DIAGNOSIS_TABLE_COLUMN_WIDTHS.length;
        for (int i = 0; i < len; i++) {
            column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(DIAGNOSIS_TABLE_COLUMN_WIDTHS[i]);
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // �����a���� Command button
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));
        
        // �����a���t�B�[���h�𐶐�����
        btnPanel.add(new JLabel(LABEL_COMBINED_DIAGNOSIS));
        btnPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        combinedDiagnosis = new JTextField(COMBINED_FIELD_LENGTH);
        combinedDiagnosis.setEditable(false);
        combinedDiagnosis.setToolTipText(TOOLTIP_COMBINE);
        
        // State ��\�����郉�x��
        stateLabel = new JLabel("");
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(combinedDiagnosis);
        p.add(new JLabel(ClientContext.getImageIcon(INFO_BUTTON_IMAGE)));
        p.add(stateLabel);
        btnPanel.add(p);
        
        btnPanel.add(Box.createHorizontalGlue());
        
        // �폜�{�^���𐶐�����
        removeButton = new JButton(ClientContext.getImageIcon(REMOVE_BUTTON_IMAGE));
        removeButton.setToolTipText(TOOLTIP_REMOVE);
        removeButton.setEnabled(false);
        removeButton.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                // TableModel �Ń����W�`�F�b�N���Ă���̂ň��S
                tableModel.removeRow(row);
                reconstractDiagnosis();
                curState.processEvent(DiagnosisStateMgr.Event.DELETED);
            }
        });
        btnPanel.add(removeButton);
        
        btnPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        
        // �N���A�{�^���𐶐�����
        clearButton = new JButton(ClientContext.getImageIcon(CLEAR_BUTTON_IMAGE));
        clearButton.setToolTipText(TOOLTIP_CLEAR);
        clearButton.setEnabled(false);
        clearButton.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                tableModel.clear();
                combinedDiagnosis.setText("");
                curState.processEvent(DiagnosisStateMgr.Event.CLEARED);
            }
        });
        btnPanel.add(clearButton);
        
        btnPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        
        // �e�_�C�A���O��OK�{�^����ǉ�����
        if (getContext().getContext().getOkButton() != null) {
            btnPanel.add(getContext().getContext().getOkButton());
        }
        
        // ��ԃ}�V�����J�n����
        curState = new DiagnosisStateMgr(removeButton, clearButton, stateLabel,
                                        tableModel, table, getContext());
        curState.enter();
        
        add(table.getTableHeader(), BorderLayout.NORTH);
        add(table, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }
    
    /**
     * StampEditor ��Ԃ��B
     * @return ���̕ҏW�e�[�u���� StampEditor
     */
    public IStampModelEditor getContext() {
        return context;
    }
    
    /**
     * StampEditor ��ݒ肷��B
     * @param context ���̕ҏW�e�[�u���� StampEditor
     */
    public void setContext(IStampModelEditor context) {
        this.context = context;
    }
    
    /**
     * �}�X�^�����e�[�u���őI�����ꂽ�A�C�e����ҏW�e�[�u���֎�荞�ށB
     * @param e PropertyChangeEvent
     */
    public void propertyChange(PropertyChangeEvent e) {
        
        String prop = e.getPropertyName();
        
        if (prop.equals(SELECTED_ITEM_PROP)) {
            
            // �ʒm���ꂽ MasterItem ���� RegisteredDiagnosisModel 
            // �𐶐����A�ҏW�e�[�u���։�����B
            MasterItem item = (MasterItem) e.getNewValue();
            
            if (item != null) {
                
                RegisteredDiagnosisModel model = new RegisteredDiagnosisModel();
                model.setDiagnosis(item.getName());
                model.setDiagnosisCode(item.getCode());
                model.setDiagnosisCodeSystem(item.getMasterTableId());
                
                tableModel.addRow(model);
                
                reconstractDiagnosis();

                // ��ԃ}�V���փC�x���g�𑗐M����
                curState.processEvent(DiagnosisStateMgr.Event.ADDED);
            }
        }
    }
    
    /**
     * �e�[�u�����X�L�������A���a���R���|�W�b�g����B
     */
    public void reconstractDiagnosis() {
        
        if (hasModifier()) {
            StringBuilder sb = new StringBuilder();
            int count = tableModel.getDataSize();
            for (int i = 0; i < count; i++) {
                RegisteredDiagnosisModel diag = (RegisteredDiagnosisModel) tableModel.getObject(i);
                sb.append(diag.getDiagnosis());
            }
            combinedDiagnosis.setText(sb.toString());
        } else {
            combinedDiagnosis.setText("");
        }
    }
    
    /**
     * �C������ӂ���ł��邩�ǂ�����Ԃ��B
     */
    private boolean hasModifier() {
        boolean hasModifier = false;
        int count = tableModel.getDataSize();
        for (int i = 0; i < count; i++) {
            RegisteredDiagnosisModel diag = (RegisteredDiagnosisModel) tableModel.getObject(i);
            if (diag.getDiagnosisCode().startsWith(MODIFIER_CODE)) {
                hasModifier = true;
                break;
            }
        }
        return hasModifier;
    }
    
    /**
     * ���a���e�[�u�����X�L�������C������̏��a�ɂ��ĕԂ��B
     */
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
        int count = tableModel.getDataSize();
        for (int i = 0; i < count; i++) {
            
            RegisteredDiagnosisModel diag = (RegisteredDiagnosisModel) tableModel.getObject(i);
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
        
        return tableModel.getObjectList();
    }
    
    public void setValue(Object[] o) {
    }
    
}

