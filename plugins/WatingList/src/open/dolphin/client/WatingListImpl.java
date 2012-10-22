package open.dolphin.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import open.dolphin.delegater.PVTDelegater;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.PatientVisitModel;

import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import open.dolphin.table.ObjectReflectTableModel;
import org.apache.log4j.Logger;

/**
 * ��t���X�g�B
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class WatingListImpl extends AbstractMainComponent {
    
    private static final String NAME = "��t���X�g";
    
    // �f�@�I���A�C�R��
    private static final ImageIcon FLAG_ICON = ClientContext.getImageIcon("flag_16.gif");
    
    // �J���e�I�[�v���A�C�R�� 
    private static final ImageIcon OPEN_ICON = ClientContext.getImageIcon("open_16.gif");
    
    // JTable�����_���p�̒j���J���[ 
    private static final Color MALE_COLOR = ClientContext.getColor("watingList.color.male");
    
    // JTable�����_���p�̏����J���[
    private static final Color FEMALE_COLOR = ClientContext.getColor("watingList.color.female");
    
    // JTable�����_���p�̊�J���[
    private static final Color ODD_COLOR = ClientContext.getColor("color.odd");
    
    // JTable�����_���p�̋����J���[ 
    private static final Color EVEN_COLOR = ClientContext.getColor("color.even");
    
    // ��t�L�����Z���J���[ 
    private static final Color CANCEL_PVT_COLOR = ClientContext.getColor("watingList.color.pvtCancel");
    
    // ���@���̃`�F�b�N�Ԋu�I�u�W�F�N�g
    private NameValuePair[] intervalObjects = ClientContext.getNameValuePair("watingList.interval");
    
    // �f�t�H���g�̃`�F�b�N�Ԋu 
    private int CHECK_INTERVAL = 30; // �f�t�H���g�l
    
    // ���@���e�[�u���̃X�e�[�^�X�J����
    private int STATE_COLUMN = 7;
    
    // �N��\���J���� 
    private final int AGE_COLUMN = 4;
    
    // �N��N�������\�b�h 
    private final String[] AGE_METHOD = new String[]{"getPatientAgeBirthday", "getPatientBirthday"};
    
    // PVT Table 
    private JTable pvtTable;
    private ObjectReflectTableModel pvtTableModel;
    
    // Preference 
    private Preferences preferences;
    
    // ���ʃ����_���t���O 
    private boolean sexRenderer;
    
    // �N��\�� 
    private boolean ageDisplay;
    
    // �^�]�� 
    private Date operationDate;
    
    // ��t DB ���`�F�b�N���� Date 
    private Date checkedTime;
    
    // ���@���Ґ� 
    private int pvtCount;
    
    // �`�F�b�N�Ԋu
    private int checkInterval;
    
    // �I������Ă��銳�ҏ�� 
    private PatientVisitModel selectedPvt;
    
    private int saveSelectedIndex;
    
    private ScheduledFuture timerHandler;
    
    private PvtChecker pvtChecker;
    
    private Logger logger;
    
    private WatingListView view;
    
    /** 
     * Creates new WatingList 
     */
    public WatingListImpl() {
        setName(NAME);
    }
    
    /**
     * ���K�[�����擾����B
     */
    private void setup() {
        logger = ClientContext.getBootLogger();
        preferences = Preferences.userNodeForPackage(this.getClass());
        sexRenderer = preferences.getBoolean("sexRenderer", false);
        ageDisplay = preferences.getBoolean("ageDisplay", true);
        checkInterval = preferences.getInt("checkInterval", CHECK_INTERVAL);  
    }
    
    /**
     * �v���O�������J�n����B
     */
    public void start() {
        setup();
        initComponents();
        connect();
        restartCheckTimer();
    }
    
    /**
     * ���C���E�C���h�E�̃^�u�Ŏ�t���X�g�ɐ؂�ւ������
     * �R�[�������B
     */
    @Override
    public void enter() {
        controlMenu();
    }
    
    /**
     * �v���O�������I������B
     */
    public void stop() {
    }
    
    /**
     * �I������Ă��闈�@���̊��҃I�u�W�F�N�g��Ԃ��B
     * @return ���҃I�u�W�F�N�g
     */
    public PatientModel getPatinet() {
        return selectedPvt != null ? selectedPvt.getPatient() : null;
    }
    
    /**
     * ���ʃ����_�����ǂ�����Ԃ��B
     * @return ���ʃ����_���̎� true
     */
    public boolean isSexRenderer() {
        return sexRenderer;
    }
    
    /**
     * �����_�����g�O���Ő؂�ւ���B
     */
    public void switchRenderere() {
        sexRenderer = !sexRenderer;
        preferences.putBoolean("sexRenderer", sexRenderer);
        if (pvtTable != null) {
            pvtTableModel.fireTableDataChanged();
        }
    }
    
    /**
     * �N��\�����I���I�t����B
     */
    public void switchAgeDisplay() {
        ageDisplay = !ageDisplay;
        preferences.putBoolean("ageDisplay", ageDisplay);
        if (pvtTable != null) {
            String method = ageDisplay ? AGE_METHOD[0] : AGE_METHOD[1];
            pvtTableModel.setMethodName(method, AGE_COLUMN);
        }
    }
    
    /**
     * ���@�����擾�������ݒ肷��B
     * @param date �擾�����
     */
    public void setOperationDate(Date date) {
        operationDate = date;
        String formatStr = ClientContext.getString("watingList.state.dateFormat");
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr); // 2006-11-20(��)
        view.getDateLbl().setText(sdf.format(operationDate));
    }
    
    /**
     * ���@�����`�F�b�N����������ݒ肷��B
     * @param date �`�F�b�N��������
     */
    public void setCheckedTime(Date date) {
        checkedTime = date;
        String formatStr = ClientContext.getString("watingList.state.timeFormat");
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
        view.getCheckedTimeLbl().setText(sdf.format(checkedTime));
    }
    
    /**
     * ���@���̃`�F�b�N�Ԋu(Timer delay)��ݒ肷��B
     * @param interval �`�F�b�N�Ԋu sec
     */
    public void setCheckInterval(int interval) {
        
        checkInterval = interval;
        String intervalSt = String.valueOf(checkInterval);
        for (NameValuePair pair : intervalObjects) {
            if (intervalSt.equals(pair.getValue())) {
                String text = ClientContext.getString("watingList.state.checkText");
                text += pair.getName(); // �`�F�b�N�Ԋu:30�b
                view.getIntervalLbl().setText(text);
                break;
            }
        }
    }
    
    /**
     * ���@����ݒ肷��B
     * @param cnt ���@��
     */
    public void setPvtCount(int cnt) {
        pvtCount = cnt;
        String text = ClientContext.getString("watingList.state.pvtCountText");
        text += String.valueOf(pvtCount); // ���@��:20
        view.getCountLbl().setText(text);
    }
    
    /**
     * �e�[�u���y�ьC�A�C�R���� enable/diable ������s���B
     * @param busy pvt �������� true
     */
    public void setBusy(boolean busy) {
        
        view.getKutuBtn().setEnabled(!busy);
        
        if (busy) {
            getContext().block();
            saveSelectedIndex = pvtTable.getSelectedRow();
        } else {
            getContext().unblock();
            pvtTable.getSelectionModel().addSelectionInterval(saveSelectedIndex, saveSelectedIndex);
        }
    }
    
    /**
     * �I������Ă��闈�@����ݒ�Ԃ��B
     * @return �I������Ă��闈�@���
     */
    public PatientVisitModel getSelectedPvt() {
        return selectedPvt;
    }
    
    /**
     * �I�����ꂽ���@����ݒ肷��B
     * @param �I�����ꂽ���@���
     */
    public void setSelectedPvt(PatientVisitModel selectedPvt) {
        PatientVisitModel old = this.selectedPvt;
        this.selectedPvt = selectedPvt;
        controlMenu();
    }
      
    /**
     * �`�F�b�N�^�C�}�[�����X�^�[�g����B
     */
    public void restartCheckTimer() {
        
        if (timerHandler != null) {
            timerHandler.cancel(false);
        }
        
        if (pvtChecker == null) {
            pvtChecker = new PvtChecker();
        }
        
        ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();
        timerHandler = schedule.scheduleWithFixedDelay(pvtChecker, 0, checkInterval, TimeUnit.SECONDS);
    }
    
    /**
     * �J���e�I�[�v�����j���[�𐧌䂷��B
     */
    private void controlMenu() {
        PatientVisitModel pvt = getSelectedPvt();
        boolean enabled = canOpen(pvt);
        getContext().enabledAction(GUIConst.ACTION_OPEN_KARTE, enabled);
    }
    
    /**
     * GUI �R���|�[�l���g�������������A�C�A�E�g����B
     */
    private void initComponents() {
        
        view = new WatingListView();
        setUI(view);
        
        // ���@�e�[�u���p�̃p�����[�^���擾����
        String[] columnNames = ClientContext.getStringArray("watingList.columnNames");
        String[] methodNames = ClientContext.getStringArray("watingList.methodNames");
        Class[] classes = ClientContext.getClassArray("watingList.columnClasses");
        int[] columnWidth = ClientContext.getIntArray("watingList.columnWidth");
        int startNumRows = ClientContext.getInt("watingList.startNumRows");
        int rowHeight = ClientContext.getInt("watingList.rowHeight");
        
        // �N��\�������Ȃ��ꍇ�̓��\�b�h��ύX����
        if (!ageDisplay) {
            methodNames[AGE_COLUMN] = AGE_METHOD[1];
        }
        
        // ��������
        pvtTable = view.getTable();
        pvtTableModel = new ObjectReflectTableModel(columnNames,startNumRows, methodNames, classes);
        pvtTable.setModel(pvtTableModel);
        
        // �R���e�L�X�g���j���[��o�^����
        pvtTable.addMouseListener(new ContextListener());     
        
        // ���@���e�[�u���̑�����ݒ肷��
        pvtTable.setRowHeight(rowHeight);
        pvtTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        for (int i = 0; i <columnWidth.length; i++) {
            pvtTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidth[i]);
        }
        
        // ���ʃ����_���𐶐�����
        MaleFemaleRenderer sRenderer = new MaleFemaleRenderer();
        pvtTable.getColumnModel().getColumn(0).setCellRenderer(sRenderer);
        pvtTable.getColumnModel().getColumn(2).setCellRenderer(sRenderer);
        pvtTable.getColumnModel().getColumn(4).setCellRenderer(sRenderer);
        pvtTable.getColumnModel().getColumn(5).setCellRenderer(sRenderer);
        pvtTable.getColumnModel().getColumn(6).setCellRenderer(sRenderer);
        
        // Center Renderer
        CenterRenderer centerRenderer = new CenterRenderer();
        pvtTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        pvtTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        
        // �J���e��ԃ����_��
        KarteStateRenderer renderer = new KarteStateRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        pvtTable.getColumnModel().getColumn(STATE_COLUMN).setCellRenderer(renderer);
               
        // ���t���x���ɒl��ݒ肷��
        setOperationDate(new Date());
        
        // �`�F�b�N�Ԋu����ݒ肷��
        setCheckInterval(checkInterval);
        
        // ���@����ݒ肷��
        setPvtCount(0);
    }
    
    /**
     * �R���|�[�l���g�ɃC�x���g�n���h���[��o�^�����݂ɐڑ�����B
     */
    private void connect() {
        
        //
        // Chart �̃��X�i�ɂȂ�
        // ���҃J���e�� Open/Save/SaveTemp �̒ʒm���󂯂Ď�t���X�g�̕\���𐧌䂷��
        //
        ChartImpl.addPropertyChangeListener(ChartImpl.CHART_STATE, 
            (PropertyChangeListener) EventHandler.create(PropertyChangeListener.class, this, "updateState", "newValue"));
                
        new EventAdapter(view.getTable());
        
        //
        // �C�̃A�C�R�����N���b�N���������@������������
        //
        view.getKutuBtn().addActionListener((ActionListener) EventHandler.create(ActionListener.class, this, "checkFullPvt"));
    }
    
    class EventAdapter implements ListSelectionListener, MouseListener {
        
        public EventAdapter(JTable tbl) {
            
            tbl.getSelectionModel().addListSelectionListener(this);
            tbl.addMouseListener(this);
        }
        
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting() == false) {
                JTable table = view.getTable();
                ObjectReflectTableModel tableModel = (ObjectReflectTableModel) table.getModel();
                int row = table.getSelectedRow();
                PatientVisitModel patient = (PatientVisitModel) tableModel.getObject(row);
                setSelectedPvt(patient);
            }
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                JTable table = (JTable) e.getSource();
                ObjectReflectTableModel tableModel = (ObjectReflectTableModel) table.getModel();
                PatientVisitModel value = (PatientVisitModel) tableModel.getObject(table.getSelectedRow());
                if (value != null) {
                    openKarte(value);
                }
            }
        }

        public void mousePressed(MouseEvent arg0) {}

        public void mouseReleased(MouseEvent arg0) {}

        public void mouseEntered(MouseEvent arg0) {}

        public void mouseExited(MouseEvent arg0) {}
    }
    
    public void openKarte(PatientVisitModel pvtModel) {
        
        if (pvtModel != null && canOpen(pvtModel)) {
            getContext().openKarte(pvtModel);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }
        
    /**
     * �`���[�g�X�e�[�g�̏�Ԃ��f�[�^�x�[�X�ɏ������ށB
     */
    public void updateState(final PatientVisitModel updated) {
        
        // ��t���X�g�̏�ԃJ�������X�V����
        List pvtList = pvtTableModel.getObjectList();
        int cnt = pvtList.size();
        boolean found = false;
        for (int i = 0; i < cnt; i++) {
            //
            // �J���e���I�[�v�����L�^���Ă���r����
            // ��t���X�g���X�V����A�v�f���ʃI�u�W�F�N�g��
            // �Ȃ��Ă���ꍇ�����邽�߁A���R�[�hID�Ŕ�r����
            //
            PatientVisitModel test = (PatientVisitModel) pvtList.get(i);
            if (updated.getId() == test.getId()) {
                test.setState(updated.getState());
                pvtTableModel.fireTableRowsUpdated(i, i);
                found = true;
                break;
            }
        }
        
        //
        // �f�[�^�x�[�X���X�V����
        //
        if (found && updated.getState() == ChartImpl.CLOSE_SAVE) {
            Runnable r = new Runnable() {
                public void run() {
                    PVTDelegater pdl = new PVTDelegater();
                    pdl.updatePvtState(updated.getId(), updated.getState());
                }
            };
            Thread t = new Thread(r);
            t.setPriority(Thread.NORM_PRIORITY);
            t.start();
        }
    }
    
    public void checkFullPvt() {
        
        if (timerHandler != null) {
            timerHandler.cancel(false);
        }
        
        try {
            
            FutureTask<Integer> task = new FutureTask<Integer>(new PvtChecker2());
            new Thread(task).start();

            Integer result = task.get(120, TimeUnit.SECONDS); // 2��
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (pvtChecker == null) {
            pvtChecker = new PvtChecker();
        }
        
        ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();
        timerHandler = schedule.scheduleWithFixedDelay(pvtChecker, checkInterval, checkInterval, TimeUnit.SECONDS);
    }
    
    /**
     * �J���e���J�����Ƃ��\���ǂ�����Ԃ��B
     * @return �J�����Ƃ��\�Ȏ� true
     */
    private boolean canOpen(PatientVisitModel pvt) {
        if (pvt == null) {
            return false;
        }
        if (isKarteOpened(pvt)) {
            return false;
        }
        if (isKarteCanceled(pvt)) {
            return false;
        }
        return true;
    }
    
    /**
     * �J���e���I�[�v������Ă��邩�ǂ�����Ԃ��B
     * @return �I�[�v������Ă��鎞 true
     */
    private boolean isKarteOpened(PatientVisitModel pvtModel) {
        if (pvtModel != null) {
            boolean opened = false;
            List<ChartImpl> allCharts = ChartImpl.getAllChart();
            for (ChartImpl chart : allCharts) {
                if (chart.getPatientVisit().getId() == pvtModel.getId()) {
                    opened = true;
                    break;
                }
            }
            return opened;
        }
        return false;
    }
    
    /**
     * ��t���L�����Z������Ă��邩�ǂ�����Ԃ��B
     * @return �L�����Z������Ă��鎞 true
     */
    private boolean isKarteCanceled(PatientVisitModel pvtModel) {
        if (pvtModel != null) {
            if (pvtModel.getState() == ChartImpl.CANCEL_PVT) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * ��t���X�g�̃R���e�L�X�g���j���[�N���X�B
     */
    class ContextListener extends MouseAdapter {
        
        @Override
        public void mousePressed(MouseEvent e) {
            mabeShowPopup(e);
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            mabeShowPopup(e);
        }
        
        public void mabeShowPopup(MouseEvent e) {
            
            if (e.isPopupTrigger()) {
                
                final JPopupMenu contextMenu = new JPopupMenu();
                String pop3 = ClientContext.getString("watingList.popup.oddEvenRenderer");
                String pop4 = ClientContext.getString("watingList.popup.sexRenderer");
                String pop5 = "�N��\��";
                
                int row = pvtTable.rowAtPoint(e.getPoint());
                Object obj = pvtTableModel.getObject(row);
                int selected = pvtTable.getSelectedRow();
                
                if (row == selected && obj != null) {
                    String pop1 = ClientContext.getString("watingList.popup.openKarte");
                    String pop2 = ClientContext.getString("watingList.popup.cancelVisit");
                    contextMenu.add(new JMenuItem(new ReflectAction(pop1, WatingListImpl.this, "openKarte")));
                    contextMenu.add(new JMenuItem(new ReflectAction(pop2, WatingListImpl.this, "cancelVisit")));
                    contextMenu.addSeparator();
                }
                
                JRadioButtonMenuItem oddEven = new JRadioButtonMenuItem(new ReflectAction(pop3, WatingListImpl.this, "switchRenderere"));
                JRadioButtonMenuItem sex = new JRadioButtonMenuItem(new ReflectAction(pop4, WatingListImpl.this, "switchRenderere"));
                ButtonGroup bg = new ButtonGroup();
                bg.add(oddEven);
                bg.add(sex);
                contextMenu.add(oddEven);
                contextMenu.add(sex);
                if (sexRenderer) {
                    sex.setSelected(true);
                } else {
                    oddEven.setSelected(true);
                }
                
                JCheckBoxMenuItem item = new JCheckBoxMenuItem(pop5);
                contextMenu.add(item);
                item.setSelected(ageDisplay);
                item.addActionListener((ActionListener)EventHandler.create(ActionListener.class, WatingListImpl.this, "switchAgeDisplay"));
                
                contextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
    
    /**
     * Popup���j���[����I������Ă��銳�҂̃J���e���J���B
     */
    public void openKarte() {
        PatientVisitModel pvtModel = getSelectedPvt();
        if (canOpen(pvtModel)) {
            openKarte(pvtModel);
        }
    }
    
    /**
     * �I���������҂̎�t���L�����Z������B
     */
    public void cancelVisit() {
        
        final int selected = pvtTable.getSelectedRow();
        Object obj = pvtTableModel.getObject(selected);
        final PatientVisitModel pvtModel = (PatientVisitModel) obj;
        
        //
        // �_�C�A���O��\�����m�F����
        //
        Object[] cstOptions = new Object[]{"�͂�", "������"};
        
        StringBuilder sb = new StringBuilder(pvtModel.getPatientName());
        sb.append("�l�̎�t���������܂���?");
        
        int select = JOptionPane.showOptionDialog(
                SwingUtilities.getWindowAncestor(pvtTable),
                sb.toString(),
                ClientContext.getFrameTitle(getName()),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                ClientContext.getImageIcon("cancl_32.gif"),
                cstOptions,"�͂�");
        
        // ��t��������
        if (select == 0) {
            Runnable r = new Runnable() {
                public void run() {
                    pvtModel.setState(ChartImpl.CANCEL_PVT);
                    PVTDelegater pdl = new PVTDelegater();
                    pdl.updatePvtState(pvtModel.getId(), pvtModel.getState());
                    pvtTableModel.fireTableRowsUpdated(selected, selected);
                }
            };
            Thread t = new Thread(r);
            t.setPriority(Thread.NORM_PRIORITY);
            t.run();
        }
    }   
    
    /**
     * ���җ��@�������I�Ƀ`�F�b�N����^�C�}�[�^�X�N�N���X�B
     */
    protected class PvtChecker implements Runnable {
        
        /**
         * Creates new Task
         */
        public PvtChecker() {
        }
        
        private PVTDelegater getDelegater() {
            return new PVTDelegater();
        }
        
        /**
         * �c�a�̌����^�X�N
         */
        public void run() {
            
            Runnable awt1 = new Runnable() {
                public void run() {
                    setBusy(true);
                }
            };
            EventQueue.invokeLater(awt1);
            
            final Date date = new Date();
            final String[] dateToSerach = getSearchDateAsString(date);
            
            // Hibernate �� firstResult �����݂̌�����ۑ�����
            List dataList = pvtTableModel.getObjectList();
            int firstResult = dataList != null ? dataList.size() : 0;
            
            logger.info("check PVT at " + date);
            logger.info("first result = " + firstResult);
            
            // ��������
            final ArrayList result = (ArrayList) getDelegater().getPvt(dateToSerach, firstResult);
            int newVisitCount = result != null ? result.size() : 0;
            logger.info("new visits = " + newVisitCount);
            
            // ���ʂ�ǉ�����
            if (newVisitCount > 0) {
                for (int i = 0; i < newVisitCount; i++) {
                    dataList.add(result.get(i));
                }
                pvtTableModel.fireTableRowsInserted(firstResult, dataList.size() - 1);
            }
            
            Runnable awt2 = new Runnable() {
                public void run() {
                    setCheckedTime(date);
                    setPvtCount(pvtTableModel.getObjectCount());
                    setBusy(false);
                }
            };
            EventQueue.invokeLater(awt2);
        }
    }
    
        
    /**
     * ���җ��@�������I�Ƀ`�F�b�N����^�C�}�[�^�X�N�N���X�B
     */
    protected class PvtChecker2 implements Callable<Integer> {
        
        /**
         * Creates new Task
         */
        public PvtChecker2() {
        }
        
        private PVTDelegater getDelegater() {
            return new PVTDelegater();
        }
        
        /**
         * �c�a�̌����^�X�N
         */
        public Integer call() {
            
            Runnable awt1 = new Runnable() {
                public void run() {
                    setBusy(true);
                }
            };
            EventQueue.invokeLater(awt1);
            
            final Date date = new Date();
            final String[] dateToSerach = getSearchDateAsString(date);
            
            //
            // �ŏ��Ɍ����f�@���I�����R�[�h�� Hibernate �� firstResult �ɂ���
            // ���݂̌�����ۑ�����
            //
            List dataList = pvtTableModel.getObjectList();
            int firstResult = 0;
            int curCount = dataList != null ? dataList.size() : 0;
            
            if (dataList != null && curCount > 0) {
                boolean found = false;
                int cnt = curCount;
                for (int i = 0; i < cnt; i++) {
                    PatientVisitModel pvt = (PatientVisitModel) dataList.get(i);
                    if (pvt.getState() == ChartImpl.CLOSE_NONE) {
                        //
                        // �f�@���I�����R�[�h���������ꍇ
                        // firstResult = i;
                        //
                        firstResult = i;
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    //
                    // firstResult = ���������ꍇ�̓��R�[�h����
                    //
                    firstResult = cnt;
                }
            }
            
            logger.info("check full PVT at " + date);
            logger.info("first result = " + firstResult);
            
            //
            // ��������
            //
            final ArrayList result = (ArrayList) getDelegater().getPvt(dateToSerach, firstResult);
            
            int checkCount = result != null ? result.size() : 0;
            logger.info("check visits = " + checkCount);
            
            //
            // ���ʂ���������
            //
            if (checkCount > 0) {
                //
                // firstResult ���� cnt �܂ł͌��݂̃��R�[�h���g�p���� 
                //
                int index = 0;
                for (int i = firstResult; i < curCount; i++) {
                    PatientVisitModel pvtC = (PatientVisitModel) dataList.get(i);
                    PatientVisitModel pvtU = (PatientVisitModel) result.get(index++);
                    //
                    // �I�����Ă�����ݒ肷��
                    //
                    if (pvtU.getState() == ChartImpl.CLOSE_SAVE && (!isKarteOpened(pvtU))) {
                        pvtC.setState(pvtU.getState());
                    }
                }
                
                //
                // cnt �ȍ~�͐V�������R�[�h�Ȃ̂ł��̂܂ܒǉ�����
                //
                for (int i = index; i < result.size(); i++) {
                    dataList.add(result.get(index++));
                }
                
                pvtTableModel.fireTableDataChanged();
                
            }
            
            Runnable awt2 = new Runnable() {
                public void run() {
                    setCheckedTime(date);
                    setPvtCount(pvtTableModel.getObjectCount());
                    setBusy(false);
                }
            };
            EventQueue.invokeLater(awt2);
            
            return new Integer(checkCount);
        }
    }
    
    private String[] getSearchDateAsString(Date date) {
            
        String[] ret = new String[3];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        ret[0] = sdf.format(date);

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);

        gc.add(Calendar.DAY_OF_MONTH, -2);
        date = gc.getTime();
        ret[1] = sdf.format(date);

        gc.add(Calendar.DAY_OF_MONTH, 2);
        date = gc.getTime();
        ret[2] = sdf.format(date);

        return ret;
    }
    
    /**
     * KarteStateRenderer
     * �J���e�i�`���[�g�j�̏�Ԃ������_�����O����N���X�B
     */
    protected class KarteStateRenderer extends DefaultTableCellRenderer {
        
        /** Creates new IconRenderer */
        public KarteStateRenderer() {
            super();
            setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean isFocused,
                int row, int col) {
            
            PatientVisitModel pvt = (PatientVisitModel) pvtTableModel.getObject(row);
            
            if (isSelected) {
                this.setBackground(table.getSelectionBackground());
                this.setForeground(table.getSelectionForeground());
                
            } else {
                
                if (isSexRenderer()) {

                    if (pvt !=null && pvt.getPatient().getGender().equals(IInfoModel.MALE)) {
                        this.setBackground(MALE_COLOR);
                    } else if (pvt !=null && pvt.getPatient().getGender().equals(IInfoModel.FEMALE)) {
                        this.setBackground(FEMALE_COLOR);
                    } else {
                        this.setBackground(Color.WHITE);
                    }

                } else {
                    if (row % 2 == 0) {
                        this.setBackground(EVEN_COLOR);
                    } else {
                        this.setBackground(ODD_COLOR);
                    }
                }
                
                Color fore = pvt != null && pvt.getState() == ChartImpl.CANCEL_PVT ? CANCEL_PVT_COLOR : table.getForeground();
                this.setForeground(fore);
            }
            
            if (value != null && value instanceof Integer) {
                
                int state = ((Integer) value).intValue();
                
                switch (state) {
                    
                    case ChartImpl.CLOSE_NONE:
                        //
                        // �A�C�R���Ȃ�
                        //
                        this.setIcon(null);
                        break;
                        
                    case ChartImpl.CLOSE_SAVE:
                        //
                        // �f�@���I�����Ă���ꍇ�͊�
                        //
                        this.setIcon(FLAG_ICON);
                        break;
                        
                    case ChartImpl.OPEN_NONE:
                    case ChartImpl.OPEN_SAVE:
                        //
                        // �I�[�v�����Ă���ꍇ�̓I�[�v��
                        //
                        this.setIcon(OPEN_ICON);
                        break;    
                        
                    default:
                        this.setIcon(null);
                        break;
                }
                this.setText("");
                
            } else {
                setIcon(null);
                this.setText(value == null ? "" : value.toString());
            }
            return this;
        }
    }
    
    /**
     * KarteStateRenderer
     * �J���e�i�`���[�g�j�̏�Ԃ������_�����O����N���X�B
     */
    protected class MaleFemaleRenderer extends DefaultTableCellRenderer {
        
        /** Creates new IconRenderer */
        public MaleFemaleRenderer() {
            super();
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean isFocused,
                int row, int col) {
            
            PatientVisitModel pvt = (PatientVisitModel) pvtTableModel.getObject(row);
            
            if (isSelected) {
                this.setBackground(table.getSelectionBackground());
                this.setForeground(table.getSelectionForeground());
                
            } else {
                if (isSexRenderer()) {

                    if (pvt !=null && pvt.getPatient().getGender().equals(IInfoModel.MALE)) {
                        this.setBackground(MALE_COLOR);
                    } else if (pvt !=null && pvt.getPatient().getGender().equals(IInfoModel.FEMALE)) {
                        this.setBackground(FEMALE_COLOR);
                    } else {
                        this.setBackground(Color.WHITE);
                    }

                } else {

                    if (row % 2 == 0) {
                        this.setBackground(EVEN_COLOR);
                    } else {
                        this.setBackground(ODD_COLOR);
                    }
                }
                
                Color fore = pvt != null && pvt.getState() == ChartImpl.CANCEL_PVT ? CANCEL_PVT_COLOR : table.getForeground();
                this.setForeground(fore);
            }
            
            if (value != null && value instanceof String) {
                this.setText((String) value);
            } else {
                setIcon(null);
                this.setText(value == null ? "" : value.toString());
            }
            return this;
        }
    }
    
    protected class CenterRenderer extends MaleFemaleRenderer {
        
        /** Creates new IconRenderer */
        public CenterRenderer() {
            super();
            this.setHorizontalAlignment(JLabel.CENTER);
        }
    }
}