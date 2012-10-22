package open.dolphin.order;

import java.util.concurrent.ExecutionException;
import open.dolphin.infomodel.ClaimConst;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
import open.dolphin.infomodel.BundleMed;
import open.dolphin.infomodel.ClaimItem;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.TensuMaster;
import open.dolphin.project.Project;
import open.dolphin.table.ListTableModel;
import open.dolphin.table.OddEvenRowRenderer;
import open.dolphin.util.ZenkakuUtils;

/**
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class RpEditor extends AbstractStampEditor {

    private static final String[] COLUMN_NAMES = {"�R�[�h", "�f�Ó��e", "����", "�P��", " ", "����/��"};
    private static final String[] METHOD_NAMES = {"getCode", "getName", "getNumber", "getUnit", "getDummy", "getBundleNumber"};
    private static final int[] COLUMN_WIDTH = {50, 200, 10, 10, 10};
    private static final int ONEDAY_COLUMN = 2;
    private static final int BUNDLE_COLUMN = 5;

    private static final String[] SR_COLUMN_NAMES = {"���", "�R�[�h", "�� ��", "�P��", "�_��", "�򉿊"};
    private static final String[] SR_METHOD_NAMES = {"getSlot", "getSrycd", "getName", "getTaniname", "getTen","getYakkakjncd"};
    private static final int[] SR_COLUMN_WIDTH = {10, 50, 200, 10, 10, 10};
    private static final int SR_NUM_ROWS = 20;

    private static final String[] ADMIN_CODE_REGEXP = {"","^0010001","^0010002","^0010003","^0010004","^(0010005|0010007)","^0010006","^0010008","^0010009","^001"};

    private static final String IN_MEDICINE     = "�@������";
    private static final String EXT_MEDICINE    = "�@�O����";

    // �ĕҏW�̏ꍇ�ɕۑ����Ă������Z�d�Z�R�[�h
    private String saveReceiptCode;
    
    private RpView view;

    private ListTableModel<MasterItem> tableModel;

    private ListTableModel<TensuMaster> searchResultModel;


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

    private ModuleModel createModuleModel() {

        ModuleModel retModel = new ModuleModel();
        BundleMed med = new BundleMed();
        retModel.setModel(med);

        // StampInfo��ݒ肷��
        ModuleInfoBean moduleInfo = retModel.getModuleInfo();
        moduleInfo.setEntity(getEntity());
        moduleInfo.setStampRole(IInfoModel.ROLE_P);

        //�@�X�^���v����ݒ肷��
        String stampName = view.getStampNameField().getText().trim();
        if (!stampName.equals("")) {
            moduleInfo.setStampName(stampName);
        } else {
            moduleInfo.setStampName(DEFAULT_STAMP_NAME);
        }

        return retModel;
    }

    @Override
    public Object getValue() {

        ModuleModel ret = createModuleModel();
        BundleMed bundle = (BundleMed) ret.getModel();

        List<MasterItem> items = tableModel.getDataProvider();

        for (MasterItem mItem : items) {

            switch(mItem.getClassCode()) {

                case ClaimConst.YAKUZAI:

                    bundle.addClaimItem(masterToClaimItem(mItem));

                    // �܌^�敪�Ɖ@���^�@�O�ɂ��f�Î�ʋ敪���s��
                    if (bundle.getClassCode() == null) {

                        String rCode = null;
                        if (mItem.getYkzKbn() != null) {

                            // �܌^�敪
                            String test = mItem.getYkzKbn();
                            
                            // �@���^�@�O
                            boolean inMed = view.getInRadio().isSelected();

                            if (test.equals(ClaimConst.YKZ_KBN_NAIYO)) {
                                // ����
                                rCode = inMed ? ClaimConst.RECEIPT_CODE_NAIYO_IN : ClaimConst.RECEIPT_CODE_NAIYO_EXT;
                                bundle.setClassCode(rCode);

                            } else if (test.equals(ClaimConst.YKZ_KBN_GAIYO)) {
                                // �O�p
                                rCode = inMed ? ClaimConst.RECEIPT_CODE_GAIYO_IN : ClaimConst.RECEIPT_CODE_GAIYO_EXT;
                                bundle.setClassCode(rCode);
                            }
                        }
                    }
                    break;

                case ClaimConst.ADMIN:
                    String ommit = mItem.getName().replaceAll(REG_ADMIN_MARK, "");
                    bundle.setAdmin(ommit);
                    bundle.setAdminCode(mItem.getCode());
                    String bNum = trimToNullIfEmpty(mItem.getBundleNumber());
                    if (bNum != null) {
                        bNum = ZenkakuUtils.toHalfNumber(bNum);
                        bundle.setBundleNumber(bNum);
                    }
                    String memo = view.getInRadio().isSelected() ? IN_MEDICINE : EXT_MEDICINE;
                    bundle.setMemo(memo);

                    break;

                default:
                    bundle.addClaimItem(masterToClaimItem(mItem));
                    break;
            }
        }
        
        if (bundle.getClassCode() == null) {

            // �����̃X�^���v
            if (saveReceiptCode != null) {
                
                StringBuilder sb = new StringBuilder();

                // 210 -> 21
                sb.append(saveReceiptCode.substring(0, 2));
                
                if (view.getInRadio().isSelected()) {
                    // �@������
                    sb.append("1");     // 211
                } else {
                    // �@�O����
                    sb.append("2");     // 212
                }
                
                bundle.setClassCode(sb.toString());
                
            } else {
                // �ی��K�p�O�̈��i�Ɨp�@��OK�Ƃ��邽��
                bundle.setClassCode(ClaimConst.RECEIPT_CODE_NAIYO);
            }
        }

        //ClientContext.getBootLogger().debug("�f�Î�敪 GET =" + bundle.getClassCode());
        
        bundle.setClassCodeSystem(ClaimConst.CLASS_CODE_ID);
        bundle.setClassName(MMLTable.getClaimClassCodeName(bundle.getClassCode()));

        return ret;
    }
   
    @Override
    public void setValue(Object value) {

        // �A�����ĕҏW�����ꍇ������̂Ńe�[�u�����e�����N���A����
        clear();

        ModuleModel target = (ModuleModel) value;

        if (target == null) {
            return;
        }

        // Entity��ۑ�����
        setEntity(target.getModuleInfo().getEntity());

        // �X�^���v����\������
        String stampName = target.getModuleInfo().getStampName();
        boolean serialized = target.getModuleInfo().isSerialized();

        if (!serialized && stampName.startsWith(FROM_EDITOR_STAMP_NAME)) {
            stampName = DEFAULT_STAMP_NAME;
        } else if (stampName.equals("")) {
            stampName = DEFAULT_STAMP_NAME;
        }
        view.getStampNameField().setText(stampName);

        BundleMed med = (BundleMed) target.getModel();
        if (med == null) {
            return;
        }

        //
        // ���Z�d�Z�R�[�h��ۑ�����
        //
        if (med.getClassCode() != null) {
            saveReceiptCode = med.getClassCode();
            //ClientContext.getBootLogger().debug("�f�Î�敪�ۑ�=" + saveReceiptCode);
        }

        // ClaimItem��MasterItem�֕ϊ����ăe�[�u���֒ǉ�����
        ClaimItem[] items = med.getClaimItem();
        for (ClaimItem item : items) {
            tableModel.addObject(claimToMasterItem(item));
        }

        // Save Administration
        if (med.getAdmin() != null) {
            MasterItem item = new MasterItem();
            item.setClassCode(ClaimConst.ADMIN);
            item.setCode(med.getAdminCode());
            item.setName(ADMIN_MARK + med.getAdmin());
            item.setDummy("X");
            String bNumber = med.getBundleNumber();
            bNumber = ZenkakuUtils.toHalfNumber(bNumber);
            item.setBundleNumber(bNumber);
            tableModel.addObject(item);
        }

        // Memo
        String memo = med.getMemo();
        if (memo != null && memo.equals(IN_MEDICINE)) {
            view.getInRadio().setSelected(true);
        } else {
            view.getOutRadio().setSelected(true);
        }

        checkValidation();
    }

    @Override
    protected void search(final String text) {

        boolean pass = true;
        pass = pass && ipOk();
        pass = pass && (text.length() > 1);

        final boolean textIsCode = isCode(text);
        boolean textIsComment = (text.startsWith("8") || text.startsWith("�W")) ? true : false;

        if (textIsCode) {
            pass = pass && (textIsComment || text.length() > 5);
        }

        if (!pass) {
            return;
        }

        // �������[���ɂ��Ă���
        view.getCountField().setText("0");

        SwingWorker worker = new SwingWorker<List<TensuMaster>, Void>() {

            @Override
            protected List<TensuMaster> doInBackground() throws Exception {
                SqlMasterDao dao = (SqlMasterDao) SqlDaoFactory.create("dao.master");
                String d = effectiveFormat.format(new Date());
                List<TensuMaster> result = null;
                if (textIsCode) {
                    result = dao.getTensuMasterByCode(ZenkakuUtils.toHalfNumber(text), d);
                } else {
                    result = dao.getTensuMasterByName(text, d);
                }

                if (!dao.isNoError()) {
                    throw new Exception(dao.getErrorMessage());
                }
                return result;
            }

            @Override
            protected void done() {
                try {
                    List<TensuMaster> result = get();
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

    private void getUsage(final String regExp) {

        if (!ipOk()) {
            return;
        }

        // �������[���ɂ��Ă���
        view.getCountField().setText("0");

        SwingWorker worker = new SwingWorker<List<TensuMaster>, Void>() {

            @Override
            protected List<TensuMaster> doInBackground() throws Exception {
                SqlMasterDao dao = (SqlMasterDao) SqlDaoFactory.create("dao.master");
                String d = effectiveFormat.format(new Date());
                
                List<TensuMaster> result = dao.getTensuMasterByCode(regExp, d);

                if (!dao.isNoError()) {
                    throw new Exception(dao.getErrorMessage());
                }
                return result;
            }

            @Override
            protected void done() {
                try {
                    List<TensuMaster> result = get();
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
    protected void checkValidation() {

        setIsEmpty = tableModel.getObjectCount() == 0 ? true : false;

        if (setIsEmpty) {
            view.getStampNameField().setText(DEFAULT_STAMP_NAME);
        }

        setIsValid = true;

        // ��܂܂��͂��̑��Ɨp�@�����邱��
        int medCnt = 0;
        int useCnt = 0;
        int other = 0;

        List<MasterItem> itemList = tableModel.getDataProvider();

        for (MasterItem item : itemList) {

            if (item.getClassCode() == ClaimConst.YAKUZAI) {
                medCnt++;

            } else if (item.getClassCode() == ClaimConst.ADMIN) {
                useCnt++;

            } else {
                // 2010-03-09
                // �ی��K�p�O���i����������
                // �����������͕s��
                other++;
            }
        }

        setIsValid = setIsValid && (medCnt > 0 || other > 0);
        setIsValid = setIsValid && (useCnt == 1);

        // ButtonControl
        view.getClearBtn().setEnabled(!setIsEmpty);
        view.getOkCntBtn().setEnabled(setIsValid && getFromStampEditor());
        view.getOkBtn().setEnabled(setIsValid && getFromStampEditor());

        view.getMedicineCheck().setSelected((medCnt > 0));
        view.getUsageCheck().setSelected((useCnt == 1));

        // �ʒm����
        super.checkValidation();
    }

    @Override
    protected void addSelectedTensu(TensuMaster tm) {

        // ���ڂ̎󂯓��ꎎ��
        String test = tm.getSlot();

        if (!Pattern.compile(passRegExp).matcher(test).find()) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        // MasterItem �ɕϊ�����
        MasterItem item = tensuToMasterItem(tm);

        // ���i�����X�^���v���̌��ɂ���
        String name = view.getStampNameField().getText().trim();
        if (name.equals("") || name.equals(DEFAULT_STAMP_NAME)) {
            view.getStampNameField().setText(item.getName());
        }

        // �e�[�u���֒ǉ�����
        tableModel.addObject(item);

        // �o���f�[�V���������s����
        checkValidation();
    }

    @Override
    protected void clear() {
        tableModel.clear();
        view.getStampNameField().setText(DEFAULT_STAMP_NAME);
        checkValidation();
    }

    @Override
    protected void initComponents() {

        // View
        view = new RpView();

        // Info Label
        view.getInfoLabel().setText(this.getInfo());

        //
        // �Z�b�g�e�[�u���𐶐�����
        //
        tableModel = new ListTableModel<MasterItem>(COLUMN_NAMES, START_NUM_ROWS, METHOD_NAMES, null) {

            // ���ʂƉ񐔂̂ݕҏW�\
            @Override
            public boolean isCellEditable(int row, int col) {

                // �����畆��
                if (col == 1) {
                    String code = (String) this.getValueAt(row, 0);
                    return (code!=null && (code.equals("810000001") || code.startsWith("83"))) ? true : false;
                }

                // �p�@
                if (col == BUNDLE_COLUMN) {
                    String code = (String) this.getValueAt(row, 0);
                    return (code!=null && code.startsWith(ClaimConst.ADMIN_CODE_START)) ? true : false;
                }

                // ����
                if (col == ONEDAY_COLUMN) {
                    String code = (String) this.getValueAt(row, 0);
                    boolean codeIsComment = (code!=null && (code.startsWith("81") || code.startsWith("83")));
                    boolean codeIsAdmin = (code!=null && code.startsWith(ClaimConst.ADMIN_CODE_START));
                    return (code==null || codeIsComment || codeIsAdmin) ? false : true;
                }

                return false;

                //return (col == ONEDAY_COLUMN || col == BUNDLE_COLUMN) ? true : false;
            }

            @Override
            public void setValueAt(Object o, int row, int col) {

                MasterItem mItem = getObject(row);

                if (mItem == null) {
                    return;
                }

                String value = (String) o;
                if (value != null) {
                    value = value.trim();
                }

                // �R�����g�ҏW �����畆��
                if (col == 1) {
                    mItem.setName(value);
                    return;
                }

                // null
                if (value == null || value.equals("")) {
                    boolean test = (col == ONEDAY_COLUMN && (mItem.getClassCode()==ClaimConst.SYUGI || mItem.getClassCode()==ClaimConst.OTHER))
                                 ? true
                                 : false;
                    if (test) {
                        mItem.setNumber(null);
                        mItem.setUnit(null);
                    }
                    checkValidation();
                    return;
                }

                if (col == ONEDAY_COLUMN && mItem.getClassCode()!=ClaimConst.ADMIN) {
                    mItem.setNumber(value);
                    checkValidation();
                    return;
                }

                if (col == BUNDLE_COLUMN && mItem.getClassCode()==ClaimConst.ADMIN) {
                    mItem.setBundleNumber(value);
                    checkValidation();
                }
            }
        };
        
        JTable setTable = view.getSetTable();
        setTable.setModel(tableModel);

        // Set Table �̍s�̍���
        setTable.setRowHeight(ClientContext.getMoreHigherRowHeight());

        // DnD
        setTable.setTransferHandler(new MasterItemTransferHandler()); // TransferHandler
        setTable.addMouseMotionListener(new MouseMotionListener() {
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

        // Selection Mode
        setTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // �I�����[�h
        setTable.setRowSelectionAllowed(true);
        ListSelectionModel m = setTable.getSelectionModel();
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

        // �񕝂�ݒ肷��
        TableColumn column = null;
        int len = COLUMN_WIDTH.length;
        for (int i = 0; i < len; i++) {
            column = setTable.getColumnModel().getColumn(i);
            column.setPreferredWidth(COLUMN_WIDTH[i]);
        }
        setTable.setDefaultRenderer(Object.class, new OddEvenRowRenderer());

        // ���ʃJ�����ɃZ���G�f�B�^��ݒ肷��
        JTextField tf = new JTextField();
        tf.addFocusListener(AutoRomanListener.getInstance());
        column = setTable.getColumnModel().getColumn(ONEDAY_COLUMN);
        DefaultCellEditor de = new DefaultCellEditor(tf);
        int ccts = Project.getPreferences().getInt("order.table.clickCountToStart", 1);
        de.setClickCountToStart(ccts);
        column.setCellEditor(de);

        // �f�Ó��e�J����(column number = 1)�ɃZ���G�f�B�^��ݒ肷�� �����畆��
        JTextField tf2 = new JTextField();
        tf2.addFocusListener(AutoKanjiListener.getInstance());
        column = setTable.getColumnModel().getColumn(1);
        DefaultCellEditor de2 = new DefaultCellEditor(tf2);
        de2.setClickCountToStart(ccts);
        column.setCellEditor(de2);

        // �����񐔃J����
        JTextField tf3 = new JTextField();
        tf3.addFocusListener(AutoRomanListener.getInstance());
        column = setTable.getColumnModel().getColumn(BUNDLE_COLUMN);
        DefaultCellEditor de3 = new DefaultCellEditor(tf3);
        de3.setClickCountToStart(ccts);
        column.setCellEditor(de3);
        
        //
        // �������ʃe�[�u���𐶐�����
        //
        JTable searchResultTable = view.getSearchResultTable();
        searchResultModel = new ListTableModel<TensuMaster>(SR_COLUMN_NAMES, SR_NUM_ROWS, SR_METHOD_NAMES, null);
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
                    TensuMaster o = searchResultModel.getObject(row);
                    if (o != null) {
                        addSelectedTensu(o);
                    }
                }
            }
        });
        
        column = null;
        len = SR_COLUMN_WIDTH.length;
        for (int i = 0; i < len; i++) {
            column = searchResultTable.getColumnModel().getColumn(i);
            column.setPreferredWidth(SR_COLUMN_WIDTH[i]);
        }
        searchResultTable.setDefaultRenderer(Object.class, new OddEvenRowRenderer());
        

        // �����t�B�[���h
        DocumentListener dl = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (view.getRtCheck().isSelected()) {
                    search(view.getSearchTextField().getText().trim());
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (view.getRtCheck().isSelected()) {
                    search(view.getSearchTextField().getText().trim());
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (view.getRtCheck().isSelected()) {
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
        view.getRtCheck().setSelected(rt);
        view.getRtCheck().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                Project.getPreferences().putBoolean("masterSearch.realTime", view.getRtCheck().isSelected());
            }
        });

        // �p�@����
        JComboBox usage = view.getUsageCombo();
        usage.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int index = view.getUsageCombo().getSelectedIndex();
                    String regExp = ADMIN_CODE_REGEXP[index];
                    if (!regExp.equals("")) {
                        getUsage(regExp);
                    }
                }
            }
        });

        // �����t�B�[���h
        countField = view.getCountField();

        // �@���A�@�O�{�^��
        JRadioButton inBtn = view.getInRadio();
        JRadioButton outBtn = view.getOutRadio();
        ButtonGroup g = new ButtonGroup();
        g.add(inBtn);
        g.add(outBtn);
        boolean bOut = Project.getPreferences().getBoolean(Project.RP_OUT, true);
        outBtn.setSelected(bOut);
        inBtn.setSelected(!bOut);
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean b = view.getOutRadio().isSelected();
                Project.getPreferences().putBoolean(Project.RP_OUT, b);
            }
        };
        inBtn.addActionListener(al);
        outBtn.addActionListener(al);

        // �X�^���v���t�B�[���h
        view.getStampNameField().addFocusListener(AutoKanjiListener.getInstance());

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

    public RpEditor() {
        super();
    }

    public RpEditor(String entity) {
        super(entity, true);
    }

    public RpEditor(String entity, boolean mode) {
        super(entity, mode);
    }
}
