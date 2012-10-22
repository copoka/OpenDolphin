package open.dolphin.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.concurrent.Executors;
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

import open.dolphin.helper.SimpleWorker;
import open.dolphin.helper.WorkerService;
import open.dolphin.table.ListTableModel;

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
    //private NameValuePair[] intervalObjects = ClientContext.getNameValuePair("watingList.interval");

    private static final String TEXT_PVT_COUNT = "���@��: ";

    private static final String TEXT_SECONDS = "�b";

    private static final String TEXT_CHECK_INTERVAL = "�`�F�b�N�Ԋu: ";

    // ���@�e�[�u���p�̃p�����[�^
    private final String[] COLUMN_NAMES = new String[]{"����ID","���@����","��   ��","����","���N����","�f�É�","�\��","���"};

    private static final String[] PROPERTY_NAMES = new String[]{
        "getPatientId","getPvtDateTrimDate","getPatientName","getPatientGenderDesc",
        "getPatientAgeBirthday","getDepartment","getAppointment","getStateInteger"};

    private static final Class[] COLUMN_CLASSES = new Class[]{String.class,String.class,String.class,String.class,String.class,String.class,String.class,Integer.class};

    private static final int[] COLUMN_WIDTH = new int[]{80,60,140,40,150,50,40,30};

    private static final int START_NUM_ROWS = 30;

    //private static final int ROW_HEIGHT = 18;
    
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
    private ListTableModel<PatientVisitModel> pvtTableModel;
    
    // Preference 
    private Preferences preferences;
    
    // ���ʃ����_���t���O 
    private boolean sexRenderer;
    
    // �N��\�� 
    private boolean ageDisplay;
    
    // �`�F�b�N�Ԋu
    private int checkInterval;
    
    // �I������Ă��銳�ҏ�� 
    private PatientVisitModel selectedPvt;
    
    private int saveSelectedIndex;

    private ScheduledExecutorService schedule;
    
    private ScheduledFuture timerHandler;
    
    private PvtChecker pvtChecker;

    // View class
    private WatingListView view;

    private SimpleDateFormat timeFormatter;

    // Status�@���
    private String statusInfo;
    
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
        preferences = Preferences.userNodeForPackage(this.getClass());
        sexRenderer = preferences.getBoolean("sexRenderer", false);
        ageDisplay = preferences.getBoolean("ageDisplay", true);
        checkInterval = preferences.getInt("checkInterval", CHECK_INTERVAL);
        timeFormatter = new SimpleDateFormat("HH:mm");
    }
    
    /**
     * �v���O�������J�n����B
     */
    @Override
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
        getContext().getStatusLabel().setText(getStatusInfo());
    }
    
    /**
     * �v���O�������I������B
     */
    @Override
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
            pvtTableModel.setProperty(method, AGE_COLUMN);
        }
    }

    public String getStatusInfo() {
        return statusInfo;
    }

    public void setStatusInfo(String info) {
        this.statusInfo = info;
    }
    
    /**
     * ���@���̃`�F�b�N�Ԋu(Timer delay)��ݒ肷��B
     * @param interval �`�F�b�N�Ԋu sec
     */
    public void setCheckInterval(int interval) {
        checkInterval = interval;
        StringBuilder sb = new StringBuilder();
        sb.append(TEXT_CHECK_INTERVAL);
        sb.append(interval);
        sb.append(TEXT_SECONDS);
        setStatusInfo(sb.toString());
    }

    public void updatePvtInfo(Date date, int count) {
        StringBuilder sb = new StringBuilder();
        sb.append(timeFormatter.format(date));
        sb.append(" | ");
        sb.append(TEXT_PVT_COUNT);
        sb.append(count);
        view.getPvtInfoLbl().setText(sb.toString());
    }

    /**
     * �e�[�u���y�ьC�A�C�R���� enable/diable ������s���B
     * @param busy pvt �������� true
     */
    public void setBusy(boolean busy) {
        
        if (busy) {
            view.getKutuBtn().setEnabled(false);
            if (getContext().getCurrentComponent() == getUI()) {
                getContext().block();
                getContext().getProgressBar().setIndeterminate(true);
            }
            saveSelectedIndex = pvtTable.getSelectedRow();
        } else {
            view.getKutuBtn().setEnabled(true);
            if (getContext().getCurrentComponent() == getUI()) {
                getContext().unblock();
                getContext().getProgressBar().setIndeterminate(false);
                getContext().getProgressBar().setValue(0);
            }
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
        
        if (timerHandler != null && (!timerHandler.isCancelled())) {
            timerHandler.cancel(true);
            boolean cancelled = timerHandler.isCancelled();
            ClientContext.getBootLogger().debug("timerHandler isCancelled = " + cancelled);
            if (!cancelled) {
                return;
            }
        }
        
        if (pvtChecker == null) {
            pvtChecker = new PvtChecker();
        }

        if (schedule == null) {
            schedule = Executors.newSingleThreadScheduledExecutor();
        }
        
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

        // View �N���X�𐶐������̃v���O�C���� UI �Ƃ���
        view = new WatingListView();
        setUI(view);

        view.getPvtInfoLbl().setText("");
        
        //
        // View �̃e�[�u�����f����u��������
        //
        pvtTable = view.getTable();
        pvtTableModel = new ListTableModel<PatientVisitModel>(COLUMN_NAMES, START_NUM_ROWS, PROPERTY_NAMES, COLUMN_CLASSES);
        // �N��\�������Ȃ��ꍇ�̓��\�b�h��ύX����
        if (!ageDisplay) {
            pvtTableModel.setProperty(AGE_METHOD[1], AGE_COLUMN);
        }
        pvtTable.setModel(pvtTableModel);
        
        // �R���e�L�X�g���j���[��o�^����
        pvtTable.addMouseListener(new ContextListener());     
        
        // ���@���e�[�u���̑�����ݒ肷��
        pvtTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pvtTable.setRowHeight(ClientContext.getHigherRowHeight());
        for (int i = 0; i < COLUMN_WIDTH.length; i++) {
            pvtTable.getColumnModel().getColumn(i).setPreferredWidth(COLUMN_WIDTH[i]);
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
        
        // �`�F�b�N�Ԋu����ݒ肷��
        setCheckInterval(checkInterval);
        getContext().getStatusLabel().setText(getStatusInfo());
    }
    
    /**
     * �R���|�[�l���g�ɃC�x���g�n���h���[��o�^�����݂ɐڑ�����B
     */
    private void connect() {
        
        // Chart �̃��X�i�ɂȂ�
        // ���҃J���e�� Open/Save/SaveTemp �̒ʒm���󂯂Ď�t���X�g�̕\���𐧌䂷��
        ChartImpl.addPropertyChangeListener(ChartImpl.CHART_STATE, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(ChartImpl.CHART_STATE)) {
                    updateState((PatientVisitModel) evt.getNewValue());
                }
            }
        });

        // ���@���X�g�e�[�u�� �I��
        pvtTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    PatientVisitModel patient = pvtTableModel.getObject(pvtTable.getSelectedRow());
                    setSelectedPvt(patient);
                }
            }
        });

        // ���@���X�g�e�[�u�� �_�u���N���b�N
        view.getTable().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    PatientVisitModel patient = pvtTableModel.getObject(pvtTable.getSelectedRow());
                    openKarte(patient);
                }
            }
        });
        
        // �C�̃A�C�R�����N���b�N���������@������������
        view.getKutuBtn().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                checkFullPvt();
            }
        });
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
        List<PatientVisitModel> pvtList = pvtTableModel.getDataProvider();
        int cnt = pvtList != null ? pvtList.size() : 0;
        boolean found = false;
        for (int i = 0; i < cnt; i++) {
            //
            // �J���e���I�[�v�����L�^���Ă���r����
            // ��t���X�g���X�V����A�v�f���ʃI�u�W�F�N�g��
            // �Ȃ��Ă���ꍇ�����邽�߁A���R�[�hID�Ŕ�r����
            //
            PatientVisitModel test = pvtList.get(i);
            if (updated.getId() == test.getId()) {
                test.setState(updated.getState());
                //pvtTableModel.fireTableRowsUpdated(i, i);
                pvtTableModel.fireTableDataChanged();
                found = true;
                break;
            }
        }
        
        //
        // �f�[�^�x�[�X���X�V����
        //
        if (found && updated.getState() == ChartImpl.CLOSE_SAVE) {

            SimpleWorker worker = new SimpleWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    PVTDelegater pdl = new PVTDelegater();
                    pdl.updatePvtState(updated.getId(), updated.getState());
                    return null;
                }

                @Override
                protected void succeeded(Void result) {
                    ClientContext.getBootLogger().debug("ChartState �̍X�V����");
                }

                @Override
                protected void failed(Throwable cause) {
                    ClientContext.getBootLogger().warn("ChartState �̍X�V���s");
                }

            };

            worker.execute();
        }
    }
    
    public void checkFullPvt() {
        
        if (timerHandler != null && (!timerHandler.isCancelled())) {
            timerHandler.cancel(true);
            boolean cancelled = timerHandler.isCancelled();
            ClientContext.getBootLogger().debug("timerHandler isCancelled = " + cancelled);
            if (!cancelled) {
                return;
            }
        }

        SimpleWorker worker = new SimpleWorker<List<PatientVisitModel>, Void>() {

            private int saveCount;
            
            private int firstResult;

            private PVTDelegater getDelegater() {
                return new PVTDelegater();
            }

            @Override
            protected List<PatientVisitModel> doInBackground() throws Exception {
            
                ClientContext.getBootLogger().debug("checkFullPvt.doInBackground()");

                Date date = new Date();
                String[] dateToSerach = getSearchDateAsString(date);

                // �ŏ��Ɍ����f�@���I�����R�[�h�� Hibernate �� firstResult �ɂ���
                // ���݂̌�����ۑ�����
                List<PatientVisitModel> dataList = pvtTableModel.getDataProvider();
                saveCount = pvtTableModel.getObjectCount();

                boolean found = false;

                for (int i = 0; i < saveCount; i++) {
                    PatientVisitModel pvt = dataList.get(i);
                    if (pvt.getState() == ChartImpl.CLOSE_NONE) {
                        // �f�@���I�����R�[�h���������ꍇ
                        // firstResult = i;
                        firstResult = i;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    // firstResult = ���������ꍇ�̓��R�[�h����
                    firstResult = saveCount;
                }

                // ��������
                PVTDelegater pdl = getDelegater();
                List<PatientVisitModel> result = (List<PatientVisitModel>) pdl.getPvt(dateToSerach, firstResult);

                if (pdl.isNoError()) {
                    return result;
                } else {
                    throw new Exception(pdl.getErrorMessage());
                }
            }

            @Override
            protected void succeeded(List<PatientVisitModel> result) {

                ClientContext.getBootLogger().debug("checkFullPvt.succeeded()");
                for (PatientVisitModel test : result) {
                    System.out.println(test.getState());
                }

                if (result!= null && result.size() > 0) {
                    //
                    // firstResult ���� saveCount �܂ł͌��݂̃��R�[�h���g�p����
                    //
                    int index = 0;
                    for (int i = firstResult; i < saveCount; i++) {
                        PatientVisitModel pvtC = result.get(i);
                        PatientVisitModel pvtU = result.get(index++);

                        // �I�����Ă�����ݒ肷��
                        if (pvtU.getState() == ChartImpl.CLOSE_SAVE && (!isKarteOpened(pvtU))) {
                            pvtC.setState(pvtU.getState());
                        }
                    }

                    // saveCount �ȍ~�͐V�������R�[�h�Ȃ̂ł��̂܂ܒǉ�����
                    for (int i = index; i < result.size(); i++) {
                        pvtTableModel.addObject(result.get(index++));
                    }
                }
            }

            @Override
            protected void failed(Throwable e) {
                ClientContext.getBootLogger().warn(e.getMessage());
            }
        };

        WorkerService service = new WorkerService() {

            @Override
            protected void startProgress() {
                setBusy(true);
            }

            @Override
            protected void stopProgress() {
                setBusy(false);

                // ��t��M�����Ȃ��ݒ�ɂȂ��Ă���\��������
                if (schedule != null) {
                    restartCheckTimer();
                }
            }
        };

        service.execute(worker);
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

        if (selected != 0) {
            return;
        }

        SimpleWorker worker = new SimpleWorker<Boolean, Void>() {

            @Override
            protected Boolean doInBackground() throws Exception {
                pvtModel.setState(ChartImpl.CANCEL_PVT);
                PVTDelegater pdl = new PVTDelegater();
                pdl.updatePvtState(pvtModel.getId(), pvtModel.getState());
                if (pdl.isNoError()) {
                    return new Boolean(true);
                } else {
                    throw new Exception(pdl.getErrorMessage());
                }
            }

            @Override
            protected void succeeded(Boolean result) {
                pvtTableModel.fireTableRowsUpdated(selected, selected);
            }
        };
        
        WorkerService service = new WorkerService();
        service.execute(worker);
    }   
    
    /**
     * ���җ��@�������I�Ƀ`�F�b�N����^�C�}�[�^�X�N�N���X�B
     */
    protected class PvtChecker implements Runnable {
        
        /** Creates new Task */
        public PvtChecker() {
        }
        
        private PVTDelegater getDelegater() {
            return new PVTDelegater();
        }
        
        /**
         * �c�a�̌����^�X�N
         */
        @Override
        public void run() {
            
            Runnable awt1 = new Runnable() {
                @Override
                public void run() {
                    setBusy(true);
                }
            };
            EventQueue.invokeLater(awt1);
            
            final Date date = new Date();
            final String[] dateToSerach = getSearchDateAsString(date);
            
            // Hibernate �� firstResult �����݂̌�����ۑ�����
            List<PatientVisitModel> dataList = pvtTableModel.getDataProvider();
            int firstResult = dataList != null ? dataList.size() : 0;
            
            // ��������
            final ArrayList<PatientVisitModel> result = (ArrayList<PatientVisitModel>) getDelegater().getPvt(dateToSerach, firstResult);
            int newVisitCount = result != null ? result.size() : 0;
            
            // ���ʂ�ǉ�����
            if (newVisitCount > 0) {
                for (int i = 0; i < newVisitCount; i++) {
                    dataList.add(result.get(i));
                }
                pvtTableModel.fireTableRowsInserted(firstResult, dataList.size() - 1);
            }
            
            Runnable awt2 = new Runnable() {
                @Override
                public void run() {
                    updatePvtInfo(date, pvtTableModel.getObjectCount());
                    setBusy(false);
                }
            };
            EventQueue.invokeLater(awt2);
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