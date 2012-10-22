package open.dolphin.order;

import open.dolphin.table.OddEvenRowRenderer;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import open.dolphin.client.*;
import open.dolphin.client.GUIConst;
import open.dolphin.infomodel.BundleDolphin;
import open.dolphin.infomodel.ClaimItem;
import open.dolphin.infomodel.InfoModel;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.table.ObjectReflectTableModel;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.Iterator;
import open.dolphin.project.Project;
import open.dolphin.util.ZenkakuUtils;

/**
 * ItemTablePanel
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class RadItemTablePanel extends JPanel implements PropertyChangeListener {
    
    private static final long serialVersionUID = 4365016271224659707L;
    
    protected static final String DEFAULT_STAMP_NAME = "�V�K�X�^���v";
    private static final String FROM_EDITOR_STAMP_NAME = "�G�f�B�^����";
    private static final String DEFAULT_NUMBER = "1";
    private static final String[] COLUMN_NAMES = { "�R�[�h", "�f�Ó��e", "�� ��", "�P ��" };
    private static final String[] METHOD_NAMES = { "getCode", "getName", "getNumber", "getUnit" };
    private static final int[] COLUMN_WIDTH = { 50, 200, 10, 10 };
    private static final int NUM_ROWS = 14;
    private static final String REMOVE_BUTTON_IMAGE = "del_16.gif";
    private static final String CLEAR_BUTTON_IMAGE = "remov_16.gif";
    private static final String NUMBER_LABEL_TEXT = "�� ��";
    private static final String SET_NAME_LABEL_TEXT = "�Z�b�g��";
    private static final String MEMO_LABEL_TEXT = "�� ��";
    private static final String TOOLTIP_DELETE = "�I�������A�C�e�����폜���܂��B";
    private static final String TOOLTIP_CLEAR = "�Z�b�g���e���N���A���܂��B";
    private static final String TOOLTIP_DND = "�h���b�O & �h���b�v�ŏ��Ԃ����ւ��邱�Ƃ��ł��܂��B";
    
    // ���ʃR���{�p�̃f�[�^
    private static String[] NUMBER_LIST = null;
    static {
        NUMBER_LIST = new String[31];
        for (int i = 0; i < 31; i++) {
            NUMBER_LIST[i] = String.valueOf(i + 1);
        }
    }
    
    // �J�����̃C���f�b�N�X
    private static final int NUMBER_COLUMN = 2;
    
    // CLAIM �֌W
    //private ModuleModel savedStamp;       // �ҏW����X�^���v
    private boolean findClaimClassCode;     // �f�Ís�׋敪��f�Ís�׃A�C�e������擾����Ƃ� true
    private String orderName;               // �h���t�B���̃I�[�_����p�̖��O
    private String classCode;               // �f�Ís�׋敪
    private String classCodeId;             // �f�Ís�׋敪��`�̃e�[�u��ID == Claim007
    private String subclassCodeId;          // == Claim003
    private String entity;
    
    // GUI �R���|�[�l���g
    private JTable setTable;
    private ObjectReflectTableModel tableModel;
    private JTextField stampNameField;
    private JTextField commentField;
    private JComboBox numberCombo;
    private JButton removeButton;
    private JButton clearButton;
    
    private IStampModelEditor parent;
    private boolean validModel;
    private RadSetTableStateMgr stateMgr;
    
    /**
     * Creates new ItemTable
     */
    public RadItemTablePanel(IStampModelEditor parent) {
        
        super(new BorderLayout(11, 5));
        
        setMyParent(parent);
        
        // �Z�b�g�e�[�u���̃��f���𐶐�����
        tableModel = new ObjectReflectTableModel(COLUMN_NAMES, NUM_ROWS, METHOD_NAMES, null) {
            
            private static final long serialVersionUID = 5162264518307934378L;
            
            // NUMBER_COLUMN ��ҏW�\�ɂ���
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == NUMBER_COLUMN ? true : false;
            }
            
            // NUMBER_COLUMN �ɒl��ݒ肷��
            @Override
            public void setValueAt(Object o, int row, int col) {
                
                if (o == null || ((String) o).trim().equals("")) {
                    return;
                }
                
                // MasterItem �ɐ��ʂ�ݒ肷��
                MasterItem mItem = (MasterItem) getObject(row);
                
                if (col == NUMBER_COLUMN && mItem != null) {
                    mItem.setNumber((String) o);
                    stateMgr.checkState();
                }
            }
        };
        
        // �Z�b�g�e�[�u���𐶐�����
        setTable = new JTable(tableModel);
        setTable.setTransferHandler(new MasterItemTransferHandler()); // TransferHandler
        setTable.addMouseMotionListener(new MouseMotionListener() {
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
        setTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // �I�����[�h
        setTable.setRowSelectionAllowed(true); 
        ListSelectionModel m = setTable.getSelectionModel();
        m.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    notifySelectedRow();
                }
            }
        });
        setTable.setToolTipText(TOOLTIP_DND);
        setTable.setDefaultRenderer(Object.class, new OddEvenRowRenderer());
        
        // �J��������ݒ肷��
        TableColumn column = null;
        if (COLUMN_WIDTH != null) {
            int len = COLUMN_WIDTH.length;
            for (int i = 0; i < len; i++) {
                column = setTable.getColumnModel().getColumn(i);
                column.setPreferredWidth(COLUMN_WIDTH[i]);
            }
        }
        
        // ���ʃJ�����ɃZ���G�f�B�^��ݒ肷��
        JTextField tf = new JTextField();
        tf.addFocusListener(AutoRomanListener.getInstance());
        column = setTable.getColumnModel().getColumn(NUMBER_COLUMN);
        DefaultCellEditor dce = new DefaultCellEditor(tf);
        int ccts = Project.getPreferences().getInt("order.table.clickCountToStart", 1);
        dce.setClickCountToStart(ccts);
        column.setCellEditor(dce);
        
        // ���ʃR���{��ݒ肷��
        numberCombo = new JComboBox(NUMBER_LIST);
        
        // �R�����g�G���A�𐶐�����
        commentField = new JTextField(15);
        commentField.addFocusListener(AutoKanjiListener.getInstance());
        
        // �X�^���v���t�B�[���h�𐶐�����
        stampNameField = new JTextField(15);
        stampNameField.setOpaque(true);
        stampNameField.setBackground(new Color(251, 239, 128));
        stampNameField.addFocusListener(AutoKanjiListener.getInstance());
        
        // �폜�{�^���𐶐�����
        removeButton = new JButton(createImageIcon(REMOVE_BUTTON_IMAGE));
        removeButton.setEnabled(false);
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedItem();
            }
        });
        removeButton.setToolTipText(TOOLTIP_DELETE);
        
        // �N���A�{�^���𐶐�����
        clearButton = new JButton(createImageIcon(CLEAR_BUTTON_IMAGE));
        clearButton.setEnabled(false);
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clear();
            }
        });
        clearButton.setToolTipText(TOOLTIP_CLEAR);
        
        // ���ː����\�b�h�̃��X�g�{�b�N�X�ƃZ�b�g�e�[�u�������X�i�֌W�ɂ���
        RadiologyMethod method = new RadiologyMethod();
        method.addPropertyChangeListener(RadiologyMethod.RADIOLOGY_MEYTHOD_PROP, this);
        
        
        // �Z�b�g���A���ʁA�R�����g
        JPanel infoP = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        infoP.add(new JLabel(SET_NAME_LABEL_TEXT));
        infoP.add(stampNameField);
        
        infoP.add(new JLabel(NUMBER_LABEL_TEXT));
        infoP.add(numberCombo);
        
        infoP.add(new JLabel(MEMO_LABEL_TEXT));
        infoP.add(commentField);
        
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bp.add(removeButton);
        bp.add(clearButton);
        if (parent.getContext().getOkButton() != null) {
            bp.add(parent.getContext().getOkButton());
        }
        
        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.X_AXIS));
        south.add(infoP);
        south.add(Box.createHorizontalGlue());
        south.add(bp);
        
        
        // �X�N���[��
        JScrollPane scroller = new JScrollPane(setTable);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // �S�̂�z�u����
        JPanel center = new JPanel(new BorderLayout());
        center.add(method, BorderLayout.WEST);
        center.add(scroller, BorderLayout.CENTER);
        this.add(center, BorderLayout.CENTER);
        this.add(south, BorderLayout.SOUTH);
        this.setPreferredSize(new Dimension(GUIConst.DEFAULT_EDITOR_WIDTH, GUIConst.DEFAULT_EDITOR_HEIGHT));
        
        // StateMgr�𐶐�����
        stateMgr = new RadSetTableStateMgr(this, setTable, removeButton, clearButton, stampNameField);
    }
    
    public boolean isValidModel() {
        return validModel;
    }
    
    public void setValidModel(boolean valid) {
        validModel = valid;
        getMyParent().setValidModel(validModel);
    }
    
    public String getOrderName() {
        return orderName;
    }
    
    public void setOrderName(String val) {
        orderName = val;
    }
    
    public String getEntity() {
        return entity;
    }
    
    public void setEntity(String val) {
        entity = val;
    }
    
    public String getClassCode() {
        return classCode;
    }
    
    public void setClassCode(String val) {
        classCode = val;
    }
    
    public String getClassCodeId() {
        return classCodeId;
    }
    
    public void setClassCodeId(String val) {
        classCodeId = val;
    }
    
    public String getSubClassCodeId() {
        return subclassCodeId;
    }
    
    public void setSubClassCodeId(String val) {
        subclassCodeId = val;
    }
    
    public IStampModelEditor getMyParent() {
        return parent;
    }
    
    public void setMyParent(IStampModelEditor parent) {
        this.parent = parent;
    }
    
    public String getBundleNumber() {
        return (String)numberCombo.getSelectedItem();
    }
    
    public void setBundleNumber(String val) {
        numberCombo.setSelectedItem(val);
    }
    
    public boolean isFindClaimClassCode() {
        return findClaimClassCode;
    }
    
    public void setFindClaimClassCode(boolean b) {
        findClaimClassCode = true;
    }
    
    /**
     * �G�f�B�^�ŕҏW�����X�^���v�̒l��Ԃ��B
     * @return �X�^���v(ModuleMode = ModuleInfo + InfoModel)
     */
    public Object getValue() {
        
        // ��ɐV�K�̃��f���Ƃ��ĕԂ�
        ModuleModel retModel = new ModuleModel();
        ModuleInfoBean moduleInfo = retModel.getModuleInfo();
        moduleInfo.setEntity(getEntity());
        moduleInfo.setStampRole("p");
        
        // �X�^���v����ݒ肷��
        String text = stampNameField.getText().trim();
        if (!text.equals("")) {
            moduleInfo.setStampName(text);
        } else {
            moduleInfo.setStampName(DEFAULT_STAMP_NAME);
        }
        
        // BundleDolphin �𐶐�����
        BundleDolphin bundle = new BundleDolphin();
        
        // Dolphin Appli �Ŏg�p����I�[�_���̂�ݒ肷��
        bundle.setOrderName(getOrderName()); // StampHolder �Ŏg�p�����
        
        // �Z�b�g�e�[�u���̃}�X�^�[�A�C�e�����擾����
        java.util.List itemList = tableModel.getObjectList();
        
        if (itemList != null) {
            
            // �f�Ís�ׂ����邩�ǂ����̃t���O
            boolean found = false;
            
            for (Iterator iter = itemList.iterator(); iter.hasNext(); ) {
                
                MasterItem mItem = (MasterItem) iter.next();
                ClaimItem item = new ClaimItem();
                
                // ���́A�R�[�h��ݒ肷��
                item.setName(mItem.getName()); // ����
                item.setCode(mItem.getCode()); // �R�[�h
                // item.setCodeSystem(mItem.masterTableId); // �e�[�u��ID
                
                // �f�Î�ʋ敪(��Z/�ޗ��E��܂̕�) mItem ���ێ���ݒ肷��
                String subclassCode = String.valueOf(mItem.getClassCode());
                item.setClassCode(subclassCode);
                item.setClassCodeSystem(subclassCodeId); // == Claom003
                
                // �f�Ís�׃R�[�h���擾����
                // �ŏ��Ɍ���������Z�̐f�Ís�׃R�[�h��CLAIM�ɐݒ肷��
                // Dolphin Project �̌��莖��
                if (isFindClaimClassCode() && (mItem.getClassCode() == 0) && (!found)) {
                    if (mItem.getClaimClassCode() != null) {
                        
                        // ���˂̏ꍇ�A�_���W�v��R�[�h����V���ɐf�Ís�׃R�[�h�𐶐�����
                        // Kirishima ver. ���
                        if (mItem.getClaimClassCode().equals(ClaimConst.INJECTION_311)) {
                            classCode = ClaimConst.INJECTION_310;
                        } else if (mItem.getClaimClassCode().equals(ClaimConst.INJECTION_321)) {
                            classCode = ClaimConst.INJECTION_320;
                        } else if (mItem.getClaimClassCode().equals(ClaimConst.INJECTION_331)) {
                            classCode = ClaimConst.INJECTION_330;
                        } else {
                            // ���ˈȊO�̃P�[�X
                            classCode = mItem.getClaimClassCode();
                        }
                        found = true;
                    }
                }
                
                // �ޗ��������͖�܂̎��A���ʂƒP�ʂ��擾����
                //if (mItem.getClassCode() != ClaimConst.SYUGI) {
                    String number = mItem.getNumber();
                    if (number != null) {
                        number = number.trim();
                        if (!number.equals("")) {
                            number = ZenkakuUtils.toHankuNumber(number);
                            item.setNumber(number);
                            item.setUnit(mItem.getUnit());
                            item.setNumberCode(getNumberCode(mItem
                                    .getClassCode()));
                            item.setNumberCodeSystem(ClaimConst.NUMBER_CODE_ID);
                        }
                    }
                //} 
                bundle.addClaimItem(item);
            }
        }
        
        // �o���h������
        String memo = commentField.getText();
        if (!memo.equals("")) {
            bundle.setMemo(memo);
        }
        
        // �o���h����
        bundle.setBundleNumber((String) numberCombo.getSelectedItem());
        
        // ClaimClassCode found or saved value
        bundle.setClassCode(classCode); // �f�Ís�׃R�[�h
        bundle.setClassCodeSystem(classCodeId); // Claim007 �Œ�̒l
        bundle.setClassName(MMLTable.getClaimClassCodeName(classCode)); // ��L�e�[�u���Œ�`����Ă���f�Ís�ׂ̖���
        
        // return (Object)bundle;
        retModel.setModel((InfoModel) bundle);
        
        return (Object) retModel;
    }
    
    /**
     * �ҏW����X�^���v�̓��e��\������B
     * @param theStamp �ҏW����X�^���v�A�߂�l�͏�ɐV�K�X�^���v�ł���B
     */
    public void setValue(Object theStamp) {
        
        // �A�����ĕҏW�����ꍇ������̂Ńe�[�u�����e�����N���A����
        clear();
        
        // null �ł���΃��^�[������
        if (theStamp == null) {
            // State��ύX����
            stateMgr.checkState();
            return;
        }
        
        // �����œn���ꂽ Stamp ���L���X�g����
        ModuleModel target  = (ModuleModel) theStamp;
        
        // Entity��ۑ�����
        setEntity(target.getModuleInfo().getEntity());
        
        // Stamp ���ƕ\���`����ݒ肷��
        String stampName = target.getModuleInfo().getStampName();
        boolean serialized = target.getModuleInfo().isSerialized();
        
        // �X�^���v�����G�f�B�^���甭�s�̏ꍇ�̓f�t�H���g�̖��̂ɂ���
        if (!serialized && stampName.startsWith(FROM_EDITOR_STAMP_NAME)) {
            stampName = DEFAULT_STAMP_NAME;
        } else if (stampName.equals("")) {
            stampName = DEFAULT_STAMP_NAME;
        }
        stampNameField.setText(stampName);
        
        // Model ��\������
        BundleDolphin bundle = (BundleDolphin) target.getModel();
        if (bundle == null) {
            return;
        }
        
        // �f�Ís�׋敪��ۑ�
        classCode = bundle.getClassCode();
        
        ClaimItem[] items = bundle.getClaimItem();
        int count = items.length;
        
        for (int i = 0; i < count; i++) {
            
            ClaimItem item = items[i];
            MasterItem mItem = new MasterItem();
            
            // ��Z�E�ޗ��E��i�̃t���O
            String val = item.getClassCode();
            mItem.setClassCode(Integer.parseInt(val));
            
            // Name Code TableId
            mItem.setName(item.getName());
            mItem.setCode(item.getCode());
            
            val = item.getNumber();
            if (val != null) {
                val = val.trim();
                if (!val.equals("")) {
                    val = ZenkakuUtils.toHankuNumber(val);
                    mItem.setNumber(val);
                }
                val = item.getUnit();
                if (val != null) {
                    mItem.setUnit(val);
                }
            }
            
            // Show item
            tableModel.addRow(mItem);
        }
        
        // Bundle Memo
        String memo = bundle.getMemo();
        if (memo != null) {
            commentField.setText(memo);
        }
        
        String number = bundle.getBundleNumber();
        if (number != null && (!number.equals(""))) {
            number = ZenkakuUtils.toHankuNumber(number);
            numberCombo.setSelectedItem(number);
        }
        
        // State��ύX����
        stateMgr.checkState();
    }
    
    /**
     * �}�X�^�[�e�[�u���őI�����ꂽ�A�C�e���̒ʒm���󂯁A
     * �Z�b�g�e�[�u���֒ǉ�����B
     */
    public void propertyChange(PropertyChangeEvent e) {
        
        String prop = e.getPropertyName();
        
        if (prop.equals("selectedItemProp")) {
            
            MasterItem item = (MasterItem) e.getNewValue();
            String textVal = stampNameField.getText().trim();
            
            // �}�X�^�[�A�C�e���𔻕ʂ��Ď����ݒ���s��
            if (item.getClassCode() == ClaimConst.SYUGI) {
                // �ޗ��y�і�܂̏ꍇ�͐���1��ݒ肷��
                //item.setNumber(DEFAULT_NUMBER);
                if (textVal.equals("") || textVal.equals(DEFAULT_STAMP_NAME)) {
                    // ��Z�̏ꍇ�̓X�^���v���t�B�[���h�ɖ��O��ݒ肷��
                    stampNameField.setText(item.getName());
                }
                
            } else if (item.getClassCode() == ClaimConst.YAKUZAI) {
                String inputNum = "1";
                if (item.getUnit()!= null) {
                    String unit = item.getUnit();
                    if (unit.equals("��")) {
                        inputNum = Project.getPreferences().get("defaultZyozaiNum", "3");
                    } else if (unit.equals("��")) {
                        inputNum = Project.getPreferences().get("defaultSanyakuNum", "1.0");
                    } else if (unit.equals("���k")) {
                        inputNum = Project.getPreferences().get("defaultMizuyakuNum", "1");
                    }
                } 
                item.setNumber(inputNum);
                
            } else if (item.getClassCode() == ClaimConst.ZAIRYO) {
                item.setNumber(DEFAULT_NUMBER);
            }
            tableModel.addRow(item);
            stateMgr.checkState();
            
        } else if (prop.equals(RadiologyMethod.RADIOLOGY_MEYTHOD_PROP)) {
            String text = (String) e.getNewValue();
            commentField.setText(text);
        }
    }
    
    private void notifySelectedRow() {
        int index = setTable.getSelectedRow();
        boolean b = tableModel.getObject(index) != null ? true : false;
        removeButton.setEnabled(b);
    }
    
    /**
     * Clear all items.
     */
    public void clear() {
        tableModel.clear();
        stateMgr.checkState();
    }
    
    /**
     * Clear selected item row.
     */
    private void removeSelectedItem() {
        int row = setTable.getSelectedRow();
        if (tableModel.getObject(row) != null) {
            tableModel.deleteRow(row);
            stateMgr.checkState();
        }
    }
    
    /**
     * Returns Claim004 Number Code 21 �ޗ��� when subclassCode = 1 11
     * ��ܓ��^�ʁi�P��jwhen subclassCode = 2
     */
    private String getNumberCode(int subclassCode) {
        return (subclassCode == 1) ? ClaimConst.ZAIRYO_KOSU : ClaimConst.YAKUZAI_TOYORYO_1KAI; // �ޗ��� : ��ܓ��^�ʂP��
    }
    
    private ImageIcon createImageIcon(String name) {
        return ClientContext.getImageIcon(name);
    }
}