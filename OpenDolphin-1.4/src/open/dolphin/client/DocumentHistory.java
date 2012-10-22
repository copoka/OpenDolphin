package open.dolphin.client;

import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.swing.DefaultCellEditor;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.dto.DocumentSearchSpec;
import open.dolphin.helper.DBTask;
import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.project.Project;
import open.dolphin.table.ListTableModel;
import open.dolphin.table.OddEvenRowRenderer;

/**
 * �����������擾���A�\������N���X�B
 *
 * @author Minagawa,Kazushi
 */
public class DocumentHistory {

    // PropertyChange ��
    public static final String DOCUMENT_TYPE = "documentTypeProp";
    public static final String SELECTED_HISTORIES = "selectedHistories";
    public static final String SELECTED_KARTES = "selectedKartes";
    public static final String HITORY_UPDATED = "historyUpdated";
    
    // ���������e�[�u��
    private ListTableModel<DocInfoModel> tableModel;
    
    private DocumentHistoryView view;
    
    // ���o���ԃR���{�{�b�N�X
    private JComboBox extractionCombo;
    
    // ������ʃR���{�{�b�N�X
    private JComboBox contentCombo;
    
    // �����t�B�[���h 
    private JLabel countField;
    
    // �����T�|�[�g
    private PropertyChangeSupport boundSupport;
    
    // context 
    private ChartImpl context;
    
    // �I�����ꂽ�������(DocInfo)�̔z��
    private DocInfoModel[] selectedHistories;
    
    // ���o�R���e���g(�������)
    private String extractionContent;
    
    // ���o�J�n�� 
    private Date extractionPeriod;
    
    // �����I�Ɏ擾���镶����
    private int autoFetchCount;
    
    // �����~���̃t���O 
    private boolean ascending;
    
    // �C���ł��\�����邩�ǂ����̃t���O
    private boolean showModified;
    
    // �t���O
    private boolean start;
    private NameValuePair[] contentObject;
    private NameValuePair[] extractionObjects;
    
    // Key���͂��u���b�N���郊�X�i
    private BlockKeyListener blockKeyListener;

    /**
     * ���������I�u�W�F�N�g�𐶐�����B
     * @param owner �R���e�L�V�g
     */
    public DocumentHistory(ChartImpl context) {
        this.context = context;
        initComponent();
        connect();
        start = true;
    }

    /**
     * �����e�[�u���̃R���N�V������ clear ����B
     */
    public void clear() {
        if (tableModel != null && tableModel.getDataProvider() != null)
        {
            tableModel.getDataProvider().clear();
        }
    }

    public void requestFocus() {
        view.getTable().requestFocusInWindow();
    }

    /**
     * �����v���p�e�B���X�i��o�^����B
     * @param propName �v���p�e�B��
     * @param listener ���X�i
     */
    public void addPropertyChangeListener(String propName, PropertyChangeListener listener) {
        if (boundSupport == null) {
            boundSupport = new PropertyChangeSupport(this);
        }
        boundSupport.addPropertyChangeListener(propName, listener);
    }

    /**
     * �����v���p�e�B���폜����B
     * @param propName �v���p�e�B��
     * @param listener ���X�i
     */
    public void removePropertyChangeListener(String propName, PropertyChangeListener listener) {
        if (boundSupport == null) {
            boundSupport = new PropertyChangeSupport(this);
        }
        boundSupport.removePropertyChangeListener(propName, listener);
    }

    /**
     * �I�����ꂽ��������(����)��Ԃ��B
     * @return �I�����ꂽ��������(����)
     */
    public DocInfoModel[] getSelectedHistories() {
        return selectedHistories;
    }

    /**
     * �����v���p�e�B�̑I�����ꂽ��������(����)��ݒ肷��B�ʒm���s���B
     * @param newSelected �I�����ꂽ��������(����)
     */
    public void setSelectedHistories(DocInfoModel[] newSelected) {

        DocInfoModel[] old = selectedHistories;
        selectedHistories = newSelected;
        //
        // ���X�i�֒ʒm���s��
        //
        if (selectedHistories != null) {
            boundSupport.firePropertyChange(SELECTED_HISTORIES, old, selectedHistories);
        }
    }

    /**
     * �����̌������Ƀe�[�u���̃L�[���͂��u���b�N����B
     * @param busy true �̎�������
     */
    public void blockHistoryTable(boolean busy) {
        if (busy) {
            view.getTable().addKeyListener(blockKeyListener);
        } else {
            view.getTable().removeKeyListener(blockKeyListener);
        }
    }

    /**
     * ���������� Karte ����擾���\������B
     */
    public void showHistory() {
        List<DocInfoModel> list = (List<DocInfoModel>)context.getKarte().getEntryCollection("docInfo");
        updateHistory(list);
    }

    /**
     * �����������擾����B
     * �擾����p�����[�^(����ID�A�����^�C�v�A���o����)�͂��̃N���X�̑����Ƃ���
     * ��`����Ă���B�����̃p�����[�^�� comboBox���őI�������B�l���ω�����x��
     * ���̃��\�b�h���R�[�������B
     */
    public void getDocumentHistory() {

        if (start && extractionPeriod != null && extractionContent != null) {

            // �����p�����[�^�Z�b�g��DTO�𐶐�����
            DocumentSearchSpec spec = new DocumentSearchSpec();
            spec.setKarteId(context.getKarte().getId());	// �J���eID
            spec.setDocType(extractionContent);			// �����^�C�v
            spec.setFromDate(extractionPeriod);			// ���o���ԊJ�n
            spec.setIncludeModifid(showModified);		// �C������
            spec.setCode(DocumentSearchSpec.DOCTYPE_SEARCH);	// �����^�C�v
            spec.setAscending(ascending);

            DocInfoTask task = new DocInfoTask(context, spec, new DocumentDelegater());
            task.execute();
        }
    }

    /**
     * ���o���ԓ����ω����A�������Ď擾�����ꍇ���̏����ŁA�����e�[�u���̍X�V�A �ŏ��̍s�̎����I���A�����v���p�e�B�̕ω��ʒm���s���B
     */
    private void updateHistory(List<DocInfoModel> mewHistory) {

        // �\�[�e�B���O����
        if (mewHistory != null && mewHistory.size() > 0) {
            if (ascending) {
                Collections.sort(mewHistory);
            } else {
                Collections.sort(mewHistory, Collections.reverseOrder());
            }
        }

        // ���������e�[�u���Ƀf�[�^�� Arraylist ��ݒ肷��
        tableModel.setDataProvider(mewHistory);

        // �����v���p�e�B�̒ʒm���s��
        boundSupport.firePropertyChange(HITORY_UPDATED, false, true);

        if (mewHistory != null && mewHistory.size() > 0) {

            int cnt = mewHistory.size();
            countField.setText(String.valueOf(cnt) + " ��");
            int fetchCount = cnt > autoFetchCount ? autoFetchCount : cnt;

            // �e�[�u���̍ŏ��̍s�̎����I�����s��
            JTable table = view.getTable();
            int first = 0;
            int last = 0;

            if (ascending) {
                last = cnt - 1;
                first = cnt - fetchCount;
            } else {
                first = 0;
                last = fetchCount - 1;
            }

            // �����I��
            table.getSelectionModel().addSelectionInterval(first, last);

            // �I�������s���\�������悤�ɃX�N���[������
            Rectangle r = table.getCellRect(first, last, true);
            table.scrollRectToVisible(r);

        } else {
            countField.setText("0 ��");
        }
    }

    /**
     * ���������̃^�C�g����ύX����B
     */
    public void titleChanged(DocInfoModel docInfo) {

        if (docInfo != null && docInfo.getTitle() != null) {
            ChangeTitleTask task = new ChangeTitleTask(context, docInfo, new DocumentDelegater());
            task.execute();
        }
    }

    /**
     * ���o���Ԃ�ύX���Č�������B
     */
    public void periodChanged(int state) {
        if (state == ItemEvent.SELECTED) {
            int index = extractionCombo.getSelectedIndex();
            NameValuePair pair = extractionObjects[index];
            String value = pair.getValue();
            int addValue = Integer.parseInt(value);
            GregorianCalendar today = new GregorianCalendar();
            today.add(GregorianCalendar.MONTH, addValue);
            today.clear(Calendar.HOUR_OF_DAY);
            today.clear(Calendar.MINUTE);
            today.clear(Calendar.SECOND);
            today.clear(Calendar.MILLISECOND);
            setExtractionPeriod(today.getTime());
        }
    }

    /**
     * ������ʂ�ύX���Č�������B
     */
    public void contentChanged(int state) {
        if (state == ItemEvent.SELECTED) {
            int index = contentCombo.getSelectedIndex();
            NameValuePair pair = contentObject[index];
            setExtractionContent(pair.getValue());
        }
    }

    /**
     * GUI �R���|�[�l���g�𐶐�����B
     */
    private void initComponent() {

        view = new DocumentHistoryView();

        // �����e�[�u���̃p�����[�^���擾����
        String[] columnNames = ClientContext.getStringArray("docHistory.columnNames"); // {"�m���", "���e"};
        String[] methodNames = ClientContext.getStringArray("docHistory.methodNames"); // {"getFirstConfirmDateTrimTime",// "getTitle"};
        Class[] columnClasses = {String.class, String.class};
        //int[] columnWidth = ClientContext.getIntArray("docHistory.columnWidth"); // {80,200};
        //int startNumRows = ClientContext.getInt("docHistory.startNumRows");
        
        // ToDO
        extractionObjects = new NameValuePair[7];
        extractionObjects[0] = new NameValuePair("1�N", "-12");
        extractionObjects[1] = new NameValuePair("2�N", "-24");
        extractionObjects[2] = new NameValuePair("3�N", "-36");
        extractionObjects[3] = new NameValuePair("4�N", "-48");
        extractionObjects[4] = new NameValuePair("5�N", "-60");
        extractionObjects[5] = new NameValuePair("�S��", "-120");

        // ���������e�[�u���𐶐�����
        tableModel = new ListTableModel<DocInfoModel>(columnNames, 20, methodNames, columnClasses) {

            @Override
            public boolean isCellEditable(int row, int col) {

                if (col == 1 && getObject(row) != null) {
                    return true;
                }
                return false;
            }

            @Override
            public void setValueAt(Object value, int row, int col) {

                if (col != 1 || value == null || value.equals("")) {
                    return;
                }

                DocInfoModel docInfo = getObject(row);
                if (docInfo == null) {
                    return;
                }

                // �����^�C�g����ύX���ʒm����
                docInfo.setTitle((String) value);
                titleChanged(docInfo);
            }
        };
        view.getTable().setModel(tableModel);

        view.getTable().setRowHeight(ClientContext.getMoreHigherRowHeight());

        // �J�������𒲐�����
        view.getTable().getColumnModel().getColumn(0).setPreferredWidth(90);
        view.getTable().getColumnModel().getColumn(1).setPreferredWidth(190);
        
        // �^�C�g���J������ IME ON ��ݒ肷��
        JTextField tf = new JTextField();
        tf.addFocusListener(AutoKanjiListener.getInstance());
        TableColumn column = view.getTable().getColumnModel().getColumn(1);
        column.setCellEditor(new DefaultCellEditor(tf));
        
        // ����������_����ݒ肷��
        view.getTable().setDefaultRenderer(Object.class, new OddEvenRowRenderer());

        // �������(�R���e���g�^�C�v) ComboBox �𐶐�����
        contentObject = new NameValuePair[4];
        contentObject[0] = new NameValuePair("�J���e", IInfoModel.DOCTYPE_KARTE);
        contentObject[1] = new NameValuePair("�Љ��", IInfoModel.DOCTYPE_LETTER);
        contentObject[2] = new NameValuePair("�Љ��ԏ�", IInfoModel.DOCTYPE_LETTER_REPLY);
        contentObject[3] = new NameValuePair("����", IInfoModel.DOCTYPE_LETTER_REPLY2);
        
        contentCombo = view.getDocTypeCombo();

        // ���o�@�� ComboBox �𐶐�����
        extractionCombo = view.getExtractCombo();

        // �����t�B�[���h�𐶐�����
        countField = view.getCntLbl();
    }

    /**
     * ���C�A�E�g�p�l����Ԃ��B
     * @return
     */
    public JPanel getPanel() {
        return (JPanel) view;
    }

    /**
     * Event �ڑ����s��
     */
    private void connect() {

        // �����e�[�u���őI�����ꂽ�s�̕�����\������
        ListSelectionModel slm = view.getTable().getSelectionModel();
        slm.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    JTable table = view.getTable();
                    int[] selectedRows = table.getSelectedRows();
                    if (selectedRows.length > 0) {
                        List<DocInfoModel> list = new ArrayList<DocInfoModel>(1);
                        for (int i = 0; i < selectedRows.length; i++) {
                            DocInfoModel obj = tableModel.getObject(selectedRows[i]);
                            if (obj != null) {
                                list.add(obj);
                            }
                        }
                        DocInfoModel[] selected = (DocInfoModel[]) list.toArray(new DocInfoModel[list.size()]);
                        if (selected != null && selected.length > 0) {
                            setSelectedHistories(selected);
                        } else {
                            setSelectedHistories((DocInfoModel[]) null);
                        }
                    }
                }
            }
        });

        // ������ʕύX
        contentCombo.addItemListener((ItemListener) EventHandler.create(ItemListener.class, this, "contentChanged", "stateChange"));

        // ���o���ԃR���{�{�b�N�X�̑I������������
        extractionCombo.addItemListener((ItemListener) EventHandler.create(ItemListener.class, this, "periodChanged", "stateChange"));

        // Preference ���當����ʂ�ݒ肷��
        extractionContent = IInfoModel.DOCTYPE_KARTE;

        // Preference ���璊�o���Ԃ�ݒ肷��
        int past = Project.getPreferences().getInt(Project.DOC_HISTORY_PERIOD, -12);
        int index = NameValuePair.getIndex(String.valueOf(past), extractionObjects);
        extractionCombo.setSelectedIndex(index);
        GregorianCalendar today = new GregorianCalendar();
        today.add(GregorianCalendar.MONTH, past);
        today.clear(Calendar.HOUR_OF_DAY);
        today.clear(Calendar.MINUTE);
        today.clear(Calendar.SECOND);
        today.clear(Calendar.MILLISECOND);
        setExtractionPeriod(today.getTime());

        // Preference ���玩�������擾����ݒ肷��
        autoFetchCount = Project.getPreferences().getInt(Project.DOC_HISTORY_FETCHCOUNT, 1);

        // Preference ���珸���~����ݒ肷��
        ascending = Project.getPreferences().getBoolean(Project.DOC_HISTORY_ASCENDING, false);

        // Preference ����C������\����ݒ肷��
        showModified = Project.getPreferences().getBoolean(Project.DOC_HISTORY_SHOWMODIFIED, false);

        // ���������e�[�u���̃L�[�{�[�h���͂��u���b�N���郊�X�i
        blockKeyListener = new BlockKeyListener();
    }

    /**
     * �L�[�{�[�h���͂��u���b�N���郊�X�i�N���X�B
     */
    class BlockKeyListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
            e.consume();
        }

        /** Handle the key-pressed event from the text field. */
        @Override
        public void keyPressed(KeyEvent e) {
            e.consume();
        }

        /** Handle the key-released event from the text field. */
        @Override
        public void keyReleased(KeyEvent e) {
            e.consume();
        }
    }

    /**
     * �����p�����[�^�̕����^�C�v��ݒ肷��B�B
     * @param extractionContent �����^�C�v
     */
    public void setExtractionContent(String extractionContent) {
        String old = this.extractionContent;
        this.extractionContent = extractionContent;
        boundSupport.firePropertyChange(DOCUMENT_TYPE, old, this.extractionContent);
        getDocumentHistory();
    }

    /**
     * �����p�����[�^�̕����^�C�v��Ԃ��B
     * @return �����^�C�v
     */
    public String getExtractionContent() {
        return extractionContent;
    }

    /**
     * �����p�����[�^�̒��o���Ԃ�ݒ肷��B
     * @param extractionPeriod ���o����
     */
    public void setExtractionPeriod(Date extractionPeriod) {
        this.extractionPeriod = extractionPeriod;
        getDocumentHistory();
    }

    /**
     * �����p�����[�^�̒��o���Ԃ�Ԃ��B
     * @return ���o����
     */
    public Date getExtractionPeriod() {
        return extractionPeriod;
    }

    /**
     * ��������\���̏���/�~����ݒ肷��B
     * @param ascending �����̎� true
     */
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
        getDocumentHistory();
    }

    /**
     * ��������\���̏���/�~����Ԃ��B
     * @return �����̎� true
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * �C���ł�\�����邩�ǂ�����ݒ肷��B
     * @param showModifyed �\�����鎞 true
     */
    public void setShowModified(boolean showModifyed) {
        this.showModified = showModifyed;
        getDocumentHistory();
    }

    /**
     * �C���ł�\�����邩�ǂ�����Ԃ��B
     * @return �\�����鎞 true
     */
    public boolean isShowModified() {
        return showModified;
    }

    /**
     * �����^�X�N�B
     */
    class DocInfoTask extends DBTask<List<DocInfoModel>, Void> {

        // Delegator
        private DocumentDelegater ddl;
        // �����p�����[�^��ێ�����I�u�W�F�N�g
        private DocumentSearchSpec spec;

        public DocInfoTask(Chart ctx, DocumentSearchSpec spec, DocumentDelegater ddl) {
            super(ctx);
            this.spec = spec;
            this.ddl = ddl;
        }

        @Override
        protected List<DocInfoModel> doInBackground() throws Exception {
            List<DocInfoModel> result = (List<DocInfoModel>) ddl.getDocumentList(spec);
            if (ddl.isNoError()) {
                return result;
            } else {
                throw new Exception(ddl.getErrorMessage());
            }
        }

        @Override
        protected void succeeded(List<DocInfoModel> result) {
            if (result != null) {
                updateHistory(result);
            }
        }
    }

    /**
     * �^�C�g���ύX�^�X�N�N���X�B
     */
    class ChangeTitleTask extends DBTask<Boolean, Void> {

        // DocInfo
        private DocInfoModel docInfo;
        // Delegator
        private DocumentDelegater ddl;

        public ChangeTitleTask(Chart ctx, DocInfoModel docInfo, DocumentDelegater ddl) {
            super(ctx);
            this.docInfo = docInfo;
            this.ddl = ddl;
        }

        @Override
        protected Boolean doInBackground() {
            ddl.updateTitle(docInfo);
            return new Boolean(ddl.isNoError());
        }

        @Override
        protected void succeeded(Boolean result) {
        }
    }
}
