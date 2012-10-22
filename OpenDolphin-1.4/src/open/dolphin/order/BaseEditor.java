package open.dolphin.order;

import open.dolphin.infomodel.ClaimConst;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
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
import open.dolphin.infomodel.BundleDolphin;
import open.dolphin.infomodel.ClaimItem;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.InfoModel;
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
public class BaseEditor extends AbstractStampEditor {
    
    private static final String[] COLUMN_NAMES = {"�R�[�h", "�f�Ó��e", "�� ��", "�P ��"};
    private static final String[] METHOD_NAMES = {"getCode", "getName", "getNumber", "getUnit"};
    private static final int[] COLUMN_WIDTH = {50, 200, 10, 10};
    private static final int NUMBER_COLUMN = 2;

    private static final String[] SR_COLUMN_NAMES = {"���", "�R�[�h", "�� ��", "�P��", "�_��", "�f��", "�a�f", "���O", "�ИV"};
    private static final String[] SR_METHOD_NAMES = {
        "getSlot", "getSrycd", "getName", "getTaniname", "getTen","getSrysyukbn", "getHospsrykbn", "getNyugaitekkbn", "getRoutekkbn"};
    private static final int[] SR_COLUMN_WIDTH = {10, 50, 200, 10, 10, 10, 5, 5, 5};
    private static final int SR_NUM_ROWS = 20;

    private BaseView view;

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

    @Override
    public Object getValue() {

        // ��ɐV�K�̃��f���Ƃ��ĕԂ�
        ModuleModel retModel = new ModuleModel();
        ModuleInfoBean moduleInfo = retModel.getModuleInfo();
        moduleInfo.setEntity(getEntity());
        moduleInfo.setStampRole(IInfoModel.ROLE_P);

        // �X�^���v����ݒ肷��
        String text = view.getStampNameField().getText().trim();
        if (!text.equals("")) {
            moduleInfo.setStampName(text);
        } else {
            moduleInfo.setStampName(DEFAULT_STAMP_NAME);
        }

        // BundleDolphin �𐶐�����
        BundleDolphin bundle = new BundleDolphin();

        // Dolphin Appli �Ŏg�p����I�[�_���̂�ݒ肷��
        // StampHolder �Ŏg�p�����i�^�u���ɑ����j
        bundle.setOrderName(getOrderName());

        // �Z�b�g�e�[�u���̃}�X�^�[�A�C�e�����擾����
        List<MasterItem> itemList = tableModel.getDataProvider();

        // �f�Ís�ׂ����邩�ǂ����̃t���O
        boolean found = false;

        for (MasterItem masterItem : itemList) {

            ClaimItem item = masterToClaimItem(masterItem);

            // �f���ݒ肷��
            // �ŏ��Ɍ���������Z�̐f������Ƃ� ClaimBundle �ɐݒ肷��
            if ((masterItem.getClassCode() == ClaimConst.SYUGI) && (!found)) {

                // �W�v����}�X�^�A�C�e�����̂֎������Ă���
                String c007 = getClaim007Code(masterItem.getClaimClassCode());

                if (c007 != null) {
                    setClassCode(c007);
                    found = true;
                }
            }

            bundle.addClaimItem(item);
        }

        // �f�Ís�׋敪
        String c007 = getClassCode()!=null ? getClassCode() : getImplied007();

        if (c007 != null) {

            bundle.setClassCode(c007);

            // Claim007 �Œ�̒l
            bundle.setClassCodeSystem(getClassCodeId());

            // ��L�e�[�u���Œ�`����Ă���f�Ís�ׂ̖���
            bundle.setClassName(MMLTable.getClaimClassCodeName(c007));
        }

        // �o���h����
        bundle.setBundleNumber((String) view.getNumberCombo().getSelectedItem());

        retModel.setModel((InfoModel) bundle);

        return (Object)retModel;
    }

    @Override
    public void setValue(Object value) {

        // �A�����ĕҏW�����ꍇ������̂Ńe�[�u�����e�����N���A����
        clear();

        ModuleModel target = (ModuleModel) value;

        // null �ł���΃��^�[������
        if (target == null) {
            return;
        }

        // Entity��ۑ�����
        setEntity(target.getModuleInfo().getEntity());

        // Stamp ���ƕ\���`����ݒ肷��
        String stampName = target.getModuleInfo().getStampName();
        boolean serialized = target.getModuleInfo().isSerialized();

        // �X�^���v�����G�f�B�^���甭�s�̏ꍇ�̓f�t�H���g�̖��̂ɂ���
        // ���j�I�Ȃ���
        if (!serialized && stampName.startsWith(FROM_EDITOR_STAMP_NAME)) {
            stampName = DEFAULT_STAMP_NAME;
        } else if (stampName.equals("")) {
            stampName = DEFAULT_STAMP_NAME;
        }
        view.getStampNameField().setText(stampName);

        // Model ��\������
        BundleDolphin bundle = (BundleDolphin) target.getModel();
        if (bundle == null) {
            return;
        }

        //
        // Bundle �� �f�Ís�׋敪��ۑ�
        //
        setClassCode(bundle.getClassCode());

        // ClaimItem��MasterItem�֕ϊ����ăe�[�u���֒ǉ�����
        ClaimItem[] items = bundle.getClaimItem();
        for (ClaimItem item : items) {
            tableModel.addObject(claimToMasterItem(item));
        }

        // �o���h�����𐔗ʃR���{�֐ݒ肷��
        String number = bundle.getBundleNumber();
        if (number != null && (!number.trim().equals(""))) {
            number = ZenkakuUtils.toHalfNumber(number.trim());
            view.getNumberCombo().setSelectedItem(number);
        }

        // State��ύX����
        checkValidation();
    }

    @Override
    protected void checkValidation() {

        setIsEmpty = tableModel.getObjectCount() == 0 ? true : false;

        if (setIsEmpty) {
            view.getStampNameField().setText(DEFAULT_STAMP_NAME);
        }

        setIsValid = true;

        int techCnt = 0;
        int other = 0;

        List<MasterItem> itemList = tableModel.getDataProvider();

        for (MasterItem item : itemList) {

            if (item.getClassCode() == ClaimConst.SYUGI) {
                techCnt++;

            } else {
                other++;
            }
        }

        // ���������OK
        setIsValid = setIsValid && (techCnt > 0 || other > 0);

        // ButtonControl
        view.getClearBtn().setEnabled(!setIsEmpty);
        view.getOkCntBtn().setEnabled(setIsValid && getFromStampEditor());
        view.getOkBtn().setEnabled(setIsValid && getFromStampEditor());

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

        // �f�Ë敪�̎󂯓��ꎎ��
        if (test.equals(ClaimConst.SLOT_SYUGI)) {
            String shinku = tm.getSrysyukbn();
            if (!Pattern.compile(shinkuRegExp).matcher(shinku).find()) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
        }

        // MasterItem �ɕϊ�����
        MasterItem item = tensuToMasterItem(tm);

        // ��Z�̏ꍇ�ɃX�^���v����ݒ肷��
        if (item.getClassCode() == ClaimConst.SYUGI) {
            String stName = view.getStampNameField().getText().trim();
            if (stName.equals("") || stName.equals(DEFAULT_STAMP_NAME)) {
                view.getStampNameField().setText(item.getName());
            }
        }

        // �e�[�u���֒ǉ�����
        tableModel.addObject(item);

        // �o���f�[�V���������s����
        checkValidation();
    }

    @Override
    protected void search(final String text) {

        boolean pass = true;
        pass = pass && ipOk();

        // * �̏ꍇ
        final boolean ast = (text.equals("*") || text.equals("��")) ? true : false;
        if (ast) {
            pass = pass && (getShinkuRegExp()!=null);
        } else {
            // �Q�����ȏ�
            pass = pass && (text.length() > 1);
        }

        // 81,83,84
        final boolean textIsCode = isCode(text);
        final boolean textIsComment = (text.startsWith("8") || text.startsWith("�W")) ? true : false;
        if (textIsCode) {
            pass = pass && (textIsComment || text.length() > 5);
        }
        
        if (!pass) {
            return;
        }
        
        // �������[���ɂ��Ă���
        countField.setText("0");

        SwingWorker worker = new SwingWorker<List<TensuMaster>, Void>() {

            @Override
            protected List<TensuMaster> doInBackground() throws Exception {
                SqlMasterDao dao = (SqlMasterDao) SqlDaoFactory.create("dao.master");
                String d = effectiveFormat.format(new Date());
                List<TensuMaster> result = null;

                if (ast) {
                    result = dao.getTensuMasterByShinku(getShinkuRegExp(), d);

                } else if (textIsCode) {
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

    @Override
    protected void clear() {
        tableModel.clear();
        view.getStampNameField().setText(DEFAULT_STAMP_NAME);
        checkValidation();
    }

    @Override
    protected void initComponents() {

        // View
        view = new BaseView();

        // Info Label
        view.getInfoLabel().setText(this.getInfo());

        //
        // �Z�b�g�e�[�u���𐶐�����
        //
        tableModel = new ListTableModel<MasterItem>(COLUMN_NAMES, START_NUM_ROWS, METHOD_NAMES, null) {

            // NUMBER_COLUMN ��ҏW�\�ɂ���
            @Override
            public boolean isCellEditable(int row, int col) {
                // �����畆��
                if (col == 1) {
                    String code = (String) this.getValueAt(row, 0);
                    return (code!=null && (code.equals("810000001") || code.startsWith("83"))) ? true : false;
                }
                // ����
                if (col == NUMBER_COLUMN) {
                    String code = (String) this.getValueAt(row, 0);
                    boolean codeIsComment = (code!=null && (code.startsWith("81") || code.startsWith("83")));
                    return (code==null || codeIsComment) ? false : true;
                }
                return col == NUMBER_COLUMN ? true : false;
            }

            // NUMBER_COLUMN �ɒl��ݒ肷��
            @Override
            public void setValueAt(Object o, int row, int col) {

                MasterItem mItem = getObject(row);

                if (mItem == null) {
                    return;
                }

                String value = (String) o;
                if (o != null) {
                    value = value.trim();
                }

                // �R�����g�ҏW �����畆��
                if (col == 1) {
                    mItem.setName(value);
                    return;
                }

                // ����
                int code = mItem.getClassCode();

                if (value == null || value.equals("")) {

                    boolean test = (code==ClaimConst.SYUGI ||
                                    code==ClaimConst.OTHER ||
                                    code==ClaimConst.BUI) ? true : false;
                    if (test) {
                        mItem.setNumber(null);
                        mItem.setUnit(null);
                    }
                    checkValidation();
                    return;
                }

                mItem.setNumber(value);
                checkValidation();
            }
        };
        
        JTable setTable = view.getSetTable();
        setTable.setModel(tableModel);

        // Set Table �̍s�̍���
        setTable.setRowHeight(ClientContext.getMoreHigherRowHeight());

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
        column = setTable.getColumnModel().getColumn(NUMBER_COLUMN);
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
        
        //
        // �������ʃe�[�u���𐶐�����
        //
        JTable searchResultTable = view.getSearchResultTable();
        searchResultModel = new ListTableModel<TensuMaster>(SR_COLUMN_NAMES, SR_NUM_ROWS, SR_METHOD_NAMES, null) {

            @Override
            public Object getValueAt(int row, int col) {

                Object ret = super.getValueAt(row, col);

                switch (col) {

                    case 6:
                        // �a�f
                        //System.out.println((String) ret);
                        if (ret!=null) {
                            int index = Integer.parseInt((String) ret);
                            ret = HOSPITAL_CLINIC_FLAGS[index];
                        }
                        break;

                    case 7:
                        // ���O
                        if (ret!=null) {
                            int index = Integer.parseInt((String) ret);
                            ret = IN_OUT_FLAGS[index];
                        }
                        break;

                    case 8:
                        // �ИV
                        if (ret!=null) {
                            int index = Integer.parseInt((String) ret);
                            ret = OLD_FLAGS[index];
                        }
                        break;
                }

                return ret;

            }
        };
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

        // �X�^���v���t�B�[���h
        view.getStampNameField().addFocusListener(AutoKanjiListener.getInstance());

//        // �R�����g�t�B�[���h
//        view.getCommentField().addFocusListener(AutoKanjiListener.getInstance());

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

    public BaseEditor() {
    }

    public BaseEditor(String entity) {
        super(entity, true);
    }

    public BaseEditor(String entity, boolean mode) {
        super(entity, mode);
    }
}
