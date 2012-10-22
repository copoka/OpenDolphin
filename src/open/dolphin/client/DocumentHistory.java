package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.im.InputSubset;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.swing.DefaultCellEditor;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.table.TableColumn;

import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.dto.DocumentSearchSpec;
import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.project.Project;

/**
 * DocumentHistory
 *
 * @author Minagawa,Kazushi
 */
public class DocumentHistory implements PropertyChangeListener {
    
    // PropertyChange ��
    public static final String SELECTED_HISTORIES = "selectedHistories";
    
    public static final String SELECTED_KARTES = "selectedKartes";
    
    public static final String HITORY_UPDATED = "historyUpdated";
    
    /** ���������e�[�u�� */
    private ObjectListTable docHistoryTable;
    
    /** ���o���� */
    private JComboBox extractionCombo;
    
    /** �����t�B�[���h */
    private JTextField countField;
    
    /** ���C�A�E�g�p�l�� */
    private JPanel historyPanel;
    
    /** �����T�|�[�g */
    private PropertyChangeSupport boundSupport;
    
    /** context */
    private ChartPlugin context;
    
    /** �I�����ꂽ�������(DocInfo)�̔z�� */
    private DocInfoModel[] selectedHistories;
    
    /** ���o�R���e���g */
    private String extractionContent;
    
    /** ���o�J�n�� */
    private Date extractionPeriod;
    
    /** �����I�Ɏ擾���镶���� */
    private int autoFetchCount;
    
    /** �����~���̃t���O */
    private boolean ascending;
    
    /** �C���ł��\�����邩�ǂ����̃t���O */
    private boolean showModified;
    
    /** */
    private boolean start;
    
    /** �^�C�}�[�^�X�N�֘A */
    private Timer taskTimer;
    
    /** Key���͂��u���b�N���郊�X�i */
    private BlockKeyListener blockKeyListener;
    
    
    /**
     * ���������I�u�W�F�N�g�𐶐�����B
     * @param owner �R���e�L�V�g
     */
    public DocumentHistory(ChartPlugin context) {
        this.context = context;
        initComponent();
        connect();
        start = true;
    }
    
    /**
     * �����e�[�u���̃R���N�V������ clear ����B
     */
    public void clear() {
        if (docHistoryTable != null) {
            docHistoryTable.clear();
        }
    }
    
    public void requestFocus() {
        docHistoryTable.getTable().requestFocusInWindow();
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
     * ����I����s�ɂ��邽�߂̒ʒm���󂯁A�����e�[�u���� dsiabled �ɂ���B
     * ����͕����{�̂̎擾�����ɁA�����̑I�����ł��Ȃ��悤�ɂ��邽�߂̃��\�b�h�ł���B
     */
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals(DocumentViewer.BUSY_PROP)) {
            boolean busy = ((Boolean) e.getNewValue()).booleanValue();
            if (busy) {
                docHistoryTable.getTable().addKeyListener(blockKeyListener);
            } else {
                docHistoryTable.getTable().removeKeyListener(blockKeyListener);
            }
        }
    }
    
    /**
     * ���������� Karte ����擾���\������B
     */
    public void showHistory() {
        List list = context.getKarte().getEntryCollection("docInfo");
        updateHistory(list);
    }
    
    /**
     * �����������擾����B
     * �擾����p�����[�^(����ID�A�����^�C�v�A���o����)�͂��̃N���X�̑����Ƃ���
     * ��`����Ă���B�����̃p�����[�^�� comboBox���őI�������B�l���ω�����x��
     * ���̃��\�b�h���R�[�������B
     */
    public void getDocumentHistory() {
        
        if (start && extractionPeriod != null
                && extractionContent != null) {
            
            // �����p�����[�^�Z�b�g��DTO�𐶐�����
            DocumentSearchSpec spec = new DocumentSearchSpec();
            spec.setKarteId(context.getKarte().getId());		// �J���eID
            spec.setDocType(extractionContent);					// �����^�C�v
            spec.setFromDate(extractionPeriod);					// ���o���ԊJ�n
            spec.setIncludeModifid(showModified);				// �C������
            spec.setCode(DocumentSearchSpec.DOCTYPE_SEARCH);	// �����^�C�v
            spec.setAscending(ascending);
            
            // ProgressBar
            final IStatusPanel statusPanel = context.getStatusPanel();
            
            // �����^�X�N�𐶐�����
            int maxEstimation = ClientContext.getInt("task.default.maxEstimation");
            int delay = ClientContext.getInt("task.default.delay");
            final DocumentDelegater ddl = new DocumentDelegater();
            final DocInfoTask worker = new DocInfoTask(spec, ddl, maxEstimation/delay);
            
            // �^�C�}�[�𐶐�����
            taskTimer = new javax.swing.Timer(delay,
                    new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                    
                    worker.getCurrent();
                    statusPanel.setMessage(worker.getMessage());
                    
                    if (worker.isDone()) {
                        taskTimer.stop();
                        statusPanel.stop();
                        
                        // �G���[���`�F�b�N����
                        if (ddl.isNoError()) {
                            updateHistory(worker.getDocumentList());
                            
                        } else {
                            JFrame parent = context.getFrame();
                            String title = ClientContext.getString("docHistory.title");
                            JOptionPane.showMessageDialog(
                                    parent,
                                    ddl.getErrorMessage(),
                                    ClientContext.getFrameTitle(title),
                                    JOptionPane.WARNING_MESSAGE);
                        }
                        
                    } else if (worker.isTimeOver()) {
                        // �^�C���A�E�g�\�����s��
                        taskTimer.stop();
                        statusPanel.stop();
                        JFrame parent = context.getFrame();
                        String title = ClientContext.getString("docHistory.title");
                        new TimeoutWarning(parent, title, null).start();
                    }
                }
            });
            // �������J�n����
            statusPanel.start("");
            countField.setText("");
            worker.start();
            taskTimer.start();
        }
    }
    
    /**
     * ���o���ԓ����ω����A�������Ď擾�����ꍇ���̏����ŁA�����e�[�u���̍X�V�A �ŏ��̍s�̎����I���A�����v���p�e�B�̕ω��ʒm���s���B
     */
    @SuppressWarnings("unchecked")
    private void updateHistory(List mewHistory) {
        
        // �\�[�e�B���O����
        if (mewHistory != null && mewHistory.size() > 0) {
            if (ascending) {
                Collections.sort(mewHistory);
            } else {
                Collections.sort(mewHistory, Collections.reverseOrder());
            }
        }
        
        // ���������e�[�u���Ƀf�[�^�� Arraylist ��ݒ肷��
        docHistoryTable.setObjectList(mewHistory);
        
        // �����v���p�e�B�̒ʒm���s��
        boundSupport.firePropertyChange(HITORY_UPDATED, false, true);
        
        if (mewHistory != null && mewHistory.size() > 0) {
            
            int cnt = mewHistory.size();
            countField.setText(String.valueOf(cnt));
            int fetchCount = cnt > autoFetchCount ? autoFetchCount : cnt;
            
            // �e�[�u���̍ŏ��̍s�̎����I�����s��
            JTable table = docHistoryTable.getTable();
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
            countField.setText("0");
        }
    }
    
    public void documentSelectionChanged(PropertyChangeEvent e) {
        
        Object[] obj = (Object[]) e.getNewValue();
        
        if (obj != null && obj.length > 0) {
            DocInfoModel[] selectedHistories = new DocInfoModel[obj.length];
            for (int i = 0; i < obj.length; i++) {
                selectedHistories[i] = (DocInfoModel) obj[i];
            }
            setSelectedHistories(selectedHistories);

        } else {
            setSelectedHistories((DocInfoModel[]) null);
        }
    }
       
    /**
     * ���������̃^�C�g����ύX����B
     */
    public void titleChanged(PropertyChangeEvent e) {
        
        DocInfoModel docInfo = (DocInfoModel) e.getNewValue();
        
        if (docInfo != null && docInfo.getTitle() != null) {
            
            // ProgressBar
            final IStatusPanel statusPanel = context.getStatusPanel();
            
            // �����^�X�N�𐶐�����
            int maxEstimation = ClientContext.getInt("task.default.maxEstimation");
            int delay = ClientContext.getInt("task.default.delay");
            final DocumentDelegater ddl = new DocumentDelegater();
            final ChangeTitleTask worker = new ChangeTitleTask(docInfo, ddl, maxEstimation/delay);
            
            // �^�C�}�[�𐶐�����
            taskTimer = new javax.swing.Timer(delay,
                    new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                    
                    worker.getCurrent();
                    statusPanel.setMessage(worker.getMessage());
                    
                    if (worker.isDone()) {
                        taskTimer.stop();
                        statusPanel.stop();
                        
                        // �G���[���`�F�b�N����
                        if (ddl.isNoError()) {
                            
                        } else {
                            JFrame parent = context.getFrame();
                            String title = ClientContext.getString("docHistory.title");
                            JOptionPane.showMessageDialog(
                                    parent,
                                    ddl.getErrorMessage(),
                                    ClientContext.getFrameTitle(title),
                                    JOptionPane.WARNING_MESSAGE);
                        }
                        
                    } else if (worker.isTimeOver()) {
                        // �^�C���A�E�g�\�����s��
                        taskTimer.stop();
                        statusPanel.stop();
                        JFrame parent = context.getFrame();
                        String title = ClientContext.getString("docHistory.title");
                        new TimeoutWarning(parent, title, null).start();
                    }
                }
            });
            // �������J�n����
            statusPanel.start("");
            countField.setText("");
            worker.start();
            taskTimer.start();
        }
    }
    
    public void periodChanged(int state) {
        if (state == ItemEvent.SELECTED) {
            NameValuePair pair = (NameValuePair) extractionCombo.getSelectedItem();
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
     * GUI �R���|�[�l���g�𐶐�����B
     */
    private void initComponent() {
        
        //
        // �����e�[�u���̃p�����[�^���擾����
        //
        String[] columnNames = ClientContext.getStringArray("docHistory.columnNames"); // {"�m���", "���e"};
        String[] methodNames = ClientContext.getStringArray("docHistory.methodNames"); // {"getFirstConfirmDateTrimTime",// "getTitle"};
        Class[] columnClasses = { String.class, String.class };
        int[] columnWidth = ClientContext.getIntArray("docHistory.columnWidth"); // {80,200};
        int startNumRows = ClientContext.getInt("docHistory.startNumRows");
        int[] cellSpacing = ClientContext.getIntArray("docHistory.cellSpacing"); // 7,2
        String extractionText = ClientContext.getString("docHistory.combo.text");
        String countText = ClientContext.getString("docHistory.countField.text");
        NameValuePair[] extractionObjects = ClientContext.getNameValuePair("docHistory.combo.period");
        
        //
        // ���������e�[�u���𐶐�����
        //
        docHistoryTable = new ObjectListTable(columnNames, startNumRows, methodNames, columnClasses, new int[]{1});
        docHistoryTable.setColumnWidth(columnWidth);
        docHistoryTable.getTable().setIntercellSpacing(new Dimension(cellSpacing[0], cellSpacing[1]));
        JTextField tf = new JTextField();
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent event) {
                JTextField tf = (JTextField) event.getSource();
                tf.getInputContext().setCharacterSubsets(new Character.Subset[] {InputSubset.KANJI});
            }
        });
        TableColumn column = docHistoryTable.getTable().getColumnModel().getColumn(1);
        column.setCellEditor(new DefaultCellEditor(tf));
        
        // ���o�@�� ComboBox �𐶐�����
        extractionCombo = new JComboBox(extractionObjects);
        
        // �����t�B�[���h�𐶐�����
        countField = new JTextField(2);
        countField.setEditable(false);
        
        // �t�B���^�[�p�l��
        JLabel extractionLabel = new JLabel(extractionText);
        JLabel countLabel = new JLabel(countText);
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        filter.add(extractionLabel);
        filter.add(extractionCombo);
        filter.add(countLabel);
        filter.add(countField);
        
        historyPanel = new JPanel(new BorderLayout(0, 7));
        historyPanel.add(docHistoryTable.getScroller());
        historyPanel.add(filter, BorderLayout.SOUTH);
    }
    
    /**
     * ���C�A�E�g�p�l����Ԃ��B
     * @return
     */
    public JPanel getPanel() {
        return historyPanel;
    }
    
    /**
     * Event �ڑ����s��
     */
    private void connect() {
        
        // ���������̑I�������_�C���N�g����
        docHistoryTable.addPropertyChangeListener(ObjectListTable.SELECTED_OBJECT,
                (PropertyChangeListener) EventHandler.create(PropertyChangeListener.class, this, "documentSelectionChanged", ""));
        
        //
        // �����^�C�g����ύX����
        //
        docHistoryTable.addPropertyChangeListener(ObjectListTable.OBJECT_VALUE,
                (PropertyChangeListener) EventHandler.create(PropertyChangeListener.class, this, "titleChanged", ""));
        
        // ���o���ԃR���{�{�b�N�X�̑I������������
        extractionCombo.addItemListener((ItemListener)
            EventHandler.create(ItemListener.class, this, "periodChanged", "stateChange"));
        
        // Preference ���當����ʂ�ݒ肷��
        setExtractionContent("karte");
        
        // Preference ���璊�o���Ԃ�ݒ肷��
        NameValuePair[] extractionObjects = ClientContext.getNameValuePair("docHistory.combo.period");
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
        ascending = Project.getPreferences().getBoolean(Project.DIAGNOSIS_ASCENDING, false);
        
        // Preference ����C������\����ݒ肷��
        showModified = Project.getPreferences().getBoolean(Project.DOC_HISTORY_SHOWMODIFIED, false);
        
        //
        // ���������e�[�u���̃L�[�{�[�h���͂��u���b�N���郊�X�i
        //
        blockKeyListener = new BlockKeyListener();
    }
    
    /**
     * �L�[�{�[�h���͂��u���b�N���郊�X�i�N���X�B
     */
    class BlockKeyListener implements KeyListener {
        
        public void keyTyped(KeyEvent e) {
            e.consume();
        }

        /** Handle the key-pressed event from the text field. */
        public void keyPressed(KeyEvent e) {
            e.consume();
        }

        /** Handle the key-released event from the text field. */
        public void keyReleased(KeyEvent e) {
            e.consume();
        }
    }
    
    /**
     * �����p�����[�^�̕����^�C�v��ݒ肷��B�B
     * @param extractionContent �����^�C�v
     */
    public void setExtractionContent(String extractionContent) {
        this.extractionContent = extractionContent;
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
    class DocInfoTask extends AbstractInfiniteTask {
        
        // ���ʂ��i�[���郊�X�g
        private List result;
        
        // Delegator
        private DocumentDelegater ddl;
        
        // �����p�����[�^��ێ�����I�u�W�F�N�g
        private DocumentSearchSpec spec;
        
        /**
         * �^�X�N�𐶐�����B
         * @param spec �����p�����[�^��ێ�����I�u�W�F�N�g
         * @param ddl Delegator
         */
        public DocInfoTask(DocumentSearchSpec spec, DocumentDelegater ddl, int taskLength) {
            this.spec = spec;
            this.ddl = ddl;
            setTaskLength(taskLength);
        }
        
        /**
         * �������ʂ̕����������X�g��Ԃ��B
         * @return ���������X�g
         */
        protected List getDocumentList() {
            return result;
        }
        
        /**
         * ���^�X�N���s�N���X�B
         */
        protected void doTask() {
            result = ddl.getDocumentList(spec);
            setDone(true);
        }
    }
    
        
    /**
     * �^�C�g���ύX�^�X�N�N���X�B
     */
    class ChangeTitleTask extends AbstractInfiniteTask {
        
        // DocInfo
        private DocInfoModel docInfo;
        
        // Delegator
        private DocumentDelegater ddl;
        
        
        /**
         * �^�X�N�𐶐�����B
         * @param spec �����p�����[�^��ێ�����I�u�W�F�N�g
         * @param ddl Delegator
         */
        public ChangeTitleTask(DocInfoModel docInfo, DocumentDelegater ddl, int taskLength) {
            this.docInfo = docInfo;
            this.ddl = ddl;
            setTaskLength(taskLength);
        }
        
        /**
         * ���^�X�N���s�N���X�B
         */
        protected void doTask() {
            ddl.updateTitle(docInfo);
            setDone(true);
        }
    }
}
