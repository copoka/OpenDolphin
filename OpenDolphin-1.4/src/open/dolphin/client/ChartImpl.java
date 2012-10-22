package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.EventHandler;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import open.dolphin.plugin.PluginLoader;
import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.helper.StripeRenderer;
import open.dolphin.helper.WindowSupport;
import open.dolphin.infomodel.ClaimBundle;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.KarteBean;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.PVTHealthInsuranceModel;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.infomodel.VersionModel;
import open.dolphin.plugin.PluginLister;
import open.dolphin.project.*;
import open.dolphin.util.GUIDGenerator;

import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;

/**
 * 2���J���e�A���a���A�������ʗ��𓙁A���҂̑����I�f�[�^��񋟂���N���X�B
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class ChartImpl extends AbstractMainTool implements Chart, IInfoModel {
    
    private static final long DELAY = 10L;

    /** �J���e��Ԃ̑����v���p�e�B�� */
    public static final String CHART_STATE = "chartStateProp";
    
    /** �f�@���I���ŕ��Ă����� */
    public static final int CLOSE_NONE = 0;
    
    /** �f�@���I�������Ă�����  */
    public static final int CLOSE_SAVE = 1;
    
    /** �f�@���I���ŃI�[�v�����Ă����� */
    public static final int OPEN_NONE = 2;
    
    /** �f�@���I�����I�[�v�����Ă����� */
    public static final int OPEN_SAVE = 3;
    
    /** ��t�L�����Z�� */
    public static final int CANCEL_PVT = -1;
    
    //  Chart �C���X�^���X���Ǘ�����static �ϐ�
    private static ArrayList<ChartImpl> allCharts = new ArrayList<ChartImpl>(3);
    
    // Chart ��Ԃ̒ʒm���s�����߂� static �����T�|�[�g
    private static PropertyChangeSupport boundSupport = new PropertyChangeSupport(new Object());
    
    // Document Plugin ���i�[���� TabbedPane
    private JTabbedPane tabbedPane;
    
    // Active �ɂȂ��Ă���Document Plugin
    private Hashtable<String, ChartDocument> providers;
    
    // ���҃C���X�y�N�^ 
    private PatientInspector inspector;
    
    // Window Menu ���T�|�[�g����Ϗ��N���X
    private WindowSupport windowSupport;
    
    // Toolbar
    private JPanel myToolPanel;
    
    // �����󋵓���\�����鋤�ʂ̃p�l��
    private IStatusPanel statusPanel;
    
    // ���җ��@��� 
    private PatientVisitModel pvt;
    
    // Read Only �̎� true
    private boolean readOnly;
    
    // Chart �̃X�e�[�g 
    private int chartState;
    
    // Chart���̃h�L�������g�ɋ��ʂ� MEDIATOR 
    private ChartMediator mediator;
    
    // State Mgr
    private StateMgr stateMgr;
    
    // MML���M listener
    private MmlMessageListener mmlListener;
    
    // CLAIM ���M listener 
    private ClaimMessageListener claimListener;
    
    // ���̃`���[�g�� KarteBean
    private KarteBean karte;
    
    // GlassPane 
    private BlockGlass blockGlass;
    
    // Resource Map
    private ResourceMap resMap;
    
    // Logger
    private Logger logger;
    
    // �^�C�}�[
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> beeperHandle;
    private long statred;
    private long delay = DELAY;

    /**
     * Creates new ChartService
     */
    public ChartImpl() {
        logger = ClientContext.getBootLogger();
    }

    /**
     * ���̃`���[�g�̃J���e��Ԃ��B
     * @return �J���e
     */
    @Override
    public KarteBean getKarte() {
        return karte;
    }

    /**
     * ���̃`���[�g�̃J���e��ݒ肷��B
     * @param karte ���̃`���[�g�̃J���e
     */
    @Override
    public void setKarte(KarteBean karte) {
        this.karte = karte;
    }

    /**
     * Chart �� JFrame ��Ԃ��B
     * @return �`���[�g�E�C���h�Eno JFrame
     */
    @Override
    public JFrame getFrame() {
        return windowSupport.getFrame();
    }

    /**
     * Chart���h�L�������g�����ʂɎg�p���� Status �p�l����Ԃ��B
     * @return IStatusPanel
     */
    @Override
    public IStatusPanel getStatusPanel() {
        return statusPanel;
    }

    /**
     * Chart���h�L�������g�����ʂɎg�p���� Status �p�l����ݒ肷��B
     * @param statusPanel IStatusPanel
     */
    @Override
    public void setStatusPanel(IStatusPanel statusPanel) {
        this.statusPanel = statusPanel;
    }

    /**
     * ���@����ݒ肷��B
     * @param pvt ���@���
     */
    @Override
    public void setPatientVisit(PatientVisitModel pvt) {
        this.pvt = pvt;
    }

    /**
     * ���@����Ԃ��B
     * @return ���@���
     */
    @Override
    public PatientVisitModel getPatientVisit() {
        return pvt;
    }

    /**
     * ReadOnly ���ǂ�����Ԃ��B
     * @return ReadOnly�̎� true
     */
    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * ReadOnly ������ݒ肷��B
     * @param readOnly ReadOnly user �̎� true
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * ���̃`���[�g���ΏۂƂ��Ă��銳�҃��f����Ԃ��B
     * @return �`���[�g���ΏۂƂ��Ă��銳�҃��f��
     */
    @Override
    public PatientModel getPatient() {
        return getKarte().getPatient();
    }

    /**
     * ���̃`���[�g���ΏۂƂ��Ă��銳�҃��f����ݒ肷��B
     * @param patientModel �`���[�g���ΏۂƂ��銳�҃��f��
     */
    public void setPatientModel(PatientModel patientModel) {
        this.getKarte().setPatient(patientModel);
    }

    /**
     * �`���[�g�̃X�e�[�g������Ԃ��B
     * @return �`���[�g�̃X�e�[�g����
     */
    @Override
    public int getChartState() {
        return chartState;
    }

    /**
     * �`���[�g�̃X�e�[�g��ݒ肷��B
     * @param chartState �`���[�g�X�e�[�g
     */
    @Override
    public void setChartState(int chartState) {
        this.chartState = chartState;
        //
        // �C���X�^���X���Ǘ����� static �I�u�W�F�N�g
        // ���g�p���������X�i�֒ʒm����
        //
        ChartImpl.fireChanged(this);
    }

    /**
     * �`���[�g���ŋ��ʂɎg�p���� Mediator ��Ԃ��B
     * @return ChartMediator
     */
    @Override
    public ChartMediator getChartMediator() {
        return mediator;
    }

    /**
     * �`���[�g���ŋ��ʂɎg�p���� Mediator ��ݒ肷��B
     * @param mediator ChartMediator
     */
    public void setChartMediator(ChartMediator mediator) {
        this.mediator = mediator;
    }

    /**
     * Menu �A�N�V�����𐧌䂷��B
     */
    @Override
    public void enabledAction(String name, boolean enabled) {
        Action action = mediator.getAction(name);
        if (action != null) {
            action.setEnabled(enabled);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     * �����q�X�g���I�u�W�F�N�g��Ԃ��B
     * @return �����q�X�g���I�u�W�F�N�g DocumentHistory
     */
    @Override
    public DocumentHistory getDocumentHistory() {
        return inspector.getDocumentHistory();
    }

    /**
     * �����Ŏw�肳�ꂽ�^�u�ԍ��̃h�L�������g��\������B
     * @param �\������h�L�������g�̃^�u�ԍ�
     */
    @Override
    public void showDocument(int index) {
        int cnt = tabbedPane.getTabCount();
        if (index >= 0 && index <= cnt - 1 && index != tabbedPane.getSelectedIndex()) {
            tabbedPane.setSelectedIndex(index);
        }
    }

    /**
     * �`���[�g���ɖ��ۑ��h�L�������g�����邩�ǂ�����Ԃ��B
     * @return ���ۑ��h�L�������g�����鎞 true
     */
    @Override
    public boolean isDirty() {

        boolean dirty = false;

        if (providers != null && providers.size() > 0) {
            Collection<ChartDocument> docs = providers.values();
            for (ChartDocument doc : docs) {
                if (doc.isDirty()) {
                    dirty = true;
                    break;
                }
            }
        }
        return dirty;
    }

    @Override
    public void start() {

        // ResourceMap ��ۑ�����
        resMap = ClientContext.getResourceMap(ChartImpl.class);

        // �p�����[�^�����\�[�X����ݒ肷��
        String sama = resMap.getString("sama");
        int maxEstimation = resMap.getInteger("maxEstimation").intValue();
        int dl = resMap.getInteger("timerDelay").intValue();

        ApplicationContext appCtx = ClientContext.getApplicationContext();
        Application app = appCtx.getApplication();

        Task task = new Task<KarteBean, Void>(app) {

            @Override
            protected KarteBean doInBackground() throws Exception {
                logger.debug("CahrtImpl start task doInBackground");
                //
                // Database ���犳�҂̃J���e���擾����
                //
                int past = Project.getPreferences().getInt(Project.DOC_HISTORY_PERIOD, -12);
                GregorianCalendar today = new GregorianCalendar();
                today.add(GregorianCalendar.MONTH, past);
                today.clear(Calendar.HOUR_OF_DAY);
                today.clear(Calendar.MINUTE);
                today.clear(Calendar.SECOND);
                today.clear(Calendar.MILLISECOND);
                DocumentDelegater ddl = new DocumentDelegater();
                KarteBean karteBean = ddl.getKarte(getPatientVisit().getPatient().getId(), today.getTime());
                return karteBean;
            }

            @Override
            protected void succeeded(KarteBean karteBean) {
                logger.debug("CahrtImpl start task succeeded");
                karteBean.setPatient(null);
                karteBean.setPatient(getPatientVisit().getPatient());
                setKarte(karteBean);
                initComponents();
                logger.debug("initComponents end");
                SwingUtilities.invokeLater(new Runnable() {

                    @Override

                    public void run() {
                        getDocumentHistory().showHistory();
                    }
                });
            }

            @Override
            protected void cancelled() {
                logger.debug("Task cancelled");
            }

            @Override
            protected void failed(java.lang.Throwable cause) {
                logger.warn("Task failed");
                logger.warn(cause.getMessage());
            }

            @Override
            protected void interrupted(java.lang.InterruptedException e) {
                logger.warn("Task interrupted");
                logger.warn(e.getMessage());
            }
        };

        TaskMonitor taskMonitor = appCtx.getTaskMonitor();
        String message = "�J���e�I�[�v��";
        String patientName = getPatientVisit().getPatient().getFullName() + sama;
        String note = patientName + "���J���Ă��܂�...";
        Component c = null;
        TaskTimerMonitor w = new TaskTimerMonitor(task, taskMonitor, c, message, note, dl, maxEstimation);
        //taskMonitor.addPropertyChangeListener(w);

        appCtx.getTaskService().execute(task);
    }

    /**
     * ���҂̃J���e�������擾���AGUI ���\�z����B
     * ���̃��\�b�h�̓o�b�N�O�����h�X���b�h�Ŏ��s�����B
     */
    public void initComponents() {

        //
        // ���̃`���[�g �� Frame �𐶐�������������B
        // Frame �̃^�C�g����
        // ���Ҏ���(�J�i):����ID �ɐݒ肷��
        //
        String Inspector = resMap.getString("inspector");
        StringBuilder sb = new StringBuilder();
        sb.append(getPatient().getFullName());
        sb.append("(");
        String kana = getPatient().getKanaName();
        kana = kana.replace("�@", " ");
        sb.append(kana);
        sb.append(")");
        sb.append(" : ");
        sb.append(getPatient().getPatientId());
        sb.append(Inspector);

        // Frame �� MenuBar �𐶐�����
        windowSupport = WindowSupport.create(sb.toString());

        // �`���[�g�p�̃��j���[�o�[�𓾂�
        JMenuBar myMenuBar = windowSupport.getMenuBar();

        // �`���[�g�� JFrame �I�u�W�F�N�g�𓾂�
        JFrame frame = windowSupport.getFrame();
        frame.setName("chartFrame");

        // ���҃C���X�y�N�^�𐶐�����
        inspector = new PatientInspector(this);
        inspector.getPanel().setBorder(BorderFactory.createEmptyBorder(7, 7, 5, 2)); // �J�b�g&�g���C

        // Status �p�l���𐶐�����
        statusPanel = new StatusPanel();

        // Status �p�l���ɕ\��������𐶐�����
        // �J���e�o�^�� Status �p�l���̉E���ɔz�u����
        String rdFormat = resMap.getString("rdFormat");         // yyyy-MM-dd
        String rdPrifix = resMap.getString("rdDatePrefix");     // �J���e�o�^��:
        String patienIdPrefix = resMap.getString("patientIdPrefix"); // ����ID:
        Date date = getKarte().getCreated();
        SimpleDateFormat sdf = new SimpleDateFormat(rdFormat);
        String created = sdf.format(date);
        statusPanel.setRightInfo(rdPrifix + created);           // �J���e�o�^��:yyyy/mm/dd

        // ����ID Status �p�l���̍��ɔz�u����
        statusPanel.setLeftInfo(patienIdPrefix + getKarte().getPatient().getPatientId()); // ����ID:xxxxxx

        // ChartMediator �𐶐�����
        mediator = new ChartMediator(this);

        // Menu �𐶐�����
        AbstractMenuFactory appMenu = AbstractMenuFactory.getFactory();
        appMenu.setMenuSupports(getContext().getMenuSupport(), mediator);
        appMenu.build(myMenuBar);
        mediator.registerActions(appMenu.getActionMap());
        myToolPanel = appMenu.getToolPanelProduct();
        myToolPanel.add(inspector.getBasicInfoInspector().getPanel(), 0);

        //
        // ���̃N���X�ŗL��ToolBar�𐶐�����
        //
        JToolBar toolBar = new JToolBar();
        myToolPanel.add(toolBar);

        // �e�L�X�g�c�[���𐶐�����
        Action action = mediator.getActions().get(GUIConst.ACTION_INSERT_TEXT);
        JButton textBtn = new JButton();
        textBtn.setName("textBtn");
        textBtn.setAction(action);
        textBtn.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                JPopupMenu popup = new JPopupMenu();
                mediator.addTextMenu(popup);
                if (!e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        toolBar.add(textBtn);

        // �V�F�[�}�c�[���𐶐�����
        action = mediator.getActions().get(GUIConst.ACTION_INSERT_SCHEMA);
        JButton schemaBtn = new JButton();
        schemaBtn.setName("schemaBtn");
        schemaBtn.setAction(action);
        schemaBtn.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                getContext().showSchemaBox();
            }
        });
        toolBar.add(schemaBtn);

        // �X�^���v�c�[���𐶐�����
        action = mediator.getActions().get(GUIConst.ACTION_INSERT_STAMP);
        JButton stampBtn = new JButton();
        stampBtn.setName("stampBtn");
        stampBtn.setAction(action);
        stampBtn.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                JPopupMenu popup = new JPopupMenu();
                mediator.addStampMenu(popup);
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
        toolBar.add(stampBtn);

        // �ی��I���c�[���𐶐�����
        action = mediator.getActions().get(GUIConst.ACTION_SELECT_INSURANCE);
        JButton insBtn = new JButton();
        insBtn.setName("insBtn");
        insBtn.setAction(action);
        insBtn.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                JPopupMenu popup = new JPopupMenu();
                PVTHealthInsuranceModel[] insurances = getHealthInsurances();
                for (PVTHealthInsuranceModel hm : insurances) {
                    ReflectActionListener ra = new ReflectActionListener(mediator,
                            "applyInsurance",
                            new Class[]{hm.getClass()},
                            new Object[]{hm});
                    JMenuItem mi = new JMenuItem(hm.toString());
                    mi.addActionListener(ra);
                    popup.add(mi);
                }

                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
        toolBar.add(insBtn);

        // Document �v���O�C���̃^�u�𐶐�����
        tabbedPane = loadDocuments();

        // �S�̂����C�A�E�g����
        //JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inspector.getPanel(), tabbedPane);
        //splitPane.setName("splitPane");

        inspector.getPanel().setPreferredSize(new Dimension(280, 620));
        JPanel tmp = new JPanel(new BorderLayout());
        tmp.add(myToolPanel, BorderLayout.NORTH);
        tmp.add(inspector.getPanel(), BorderLayout.WEST);
        tmp.add(tabbedPane, BorderLayout.CENTER);

        JPanel myPanel = new JPanel();
        myPanel.setOpaque(true);
        myPanel.setLayout(new BorderLayout(5, 7));

        myPanel.add(tmp, BorderLayout.CENTER);
        myPanel.add((JPanel) statusPanel, BorderLayout.SOUTH);
        frame.setContentPane(myPanel);

        // Injection
        resMap.injectComponents(myPanel);

        // StateMgr �𐶐�����
        stateMgr = new StateMgr();

        // BlockGlass ��ݒ肷��
        blockGlass = new BlockGlass();
        frame.setGlassPane(blockGlass);

        // ���̃`���[�g�� Window �Ƀ��X�i��ݒ肷��
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                // CloseBox �̏������s��
                processWindowClosing();
            }

            @Override
            public void windowOpened(WindowEvent e) {
                // Window ���I�[�v�����ꂽ���̏������s��
                ChartImpl.windowOpened(ChartImpl.this);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                // Window ���N���[�Y���ꂽ���̏������s��
                ChartImpl.windowClosed(ChartImpl.this);
            }

            @Override
            public void windowActivated(WindowEvent e) {
                //
                // ���������փt�H�[�J�X����
                //
                getDocumentHistory().requestFocus();
            }
        });

        // Frame �̑傫�����X�g���[�W���烍�[�h����
        Rectangle bounds = null;
        ApplicationContext appCtx = ClientContext.getApplicationContext();
        try {
            bounds = (Rectangle) appCtx.getLocalStorage().load("chartBounds.xml");

        } catch (IOException ex) {
            logger.warn(ex);
        }

        if (bounds == null) {
            int x = resMap.getInteger("frameX").intValue();
            int y = resMap.getInteger("frameY").intValue();
            int width = resMap.getInteger("frameWidth").intValue();
            int height = resMap.getInteger("frameHeight").intValue();
            bounds = new Rectangle(x, y, width, height);
        }
        
        // �t���[���̕\���ʒu�����߂� J2SE 5.0
        boolean locByPlatform = Project.getPreferences().getBoolean(Project.LOCATION_BY_PLATFORM, false);

        if (locByPlatform) {
            frame.setLocationByPlatform(true);
            frame.setSize(bounds.width, bounds.height);

        } else {
            frame.setLocationByPlatform(false);
            frame.setBounds(bounds);
        }
        
        // MML ���M Queue
        if (Project.getSendMML()) {
            mmlListener = (MmlMessageListener) getContext().getPlugin("sendMml");
        }

        // CLAIM ���M Queue
        if (Project.getSendClaim()) {
            claimListener = (ClaimMessageListener) getContext().getPlugin("sendClaim");
        }

        getFrame().setVisible(true);
        
        // timer �J�n
        statred = System.currentTimeMillis();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        final Runnable beeper = new Runnable() {

            @Override
            public void run() {
                long time = System.currentTimeMillis() - statred;
                time = time / 1000L;
                statusPanel.setTimeInfo(time);
            }
        };
        beeperHandle = scheduler.scheduleAtFixedRate(beeper, delay, delay, TimeUnit.SECONDS);
    }

    /**
     * MML���M���X�i��Ԃ��B
     * @return MML���M���X�i
     */
    public MmlMessageListener getMMLListener() {
        return mmlListener;
    }

    /**
     * CLAIM���M���X�i��Ԃ��B
     * @return CLAIM���M���X�i
     */
    public ClaimMessageListener getCLAIMListener() {
        return claimListener;
    }

    /**
     * ���j���[�𐧌䂷��B
     */
    public void controlMenu() {
        stateMgr.controlMenu();
    }

    /**
     * �h�L�������g�^�u�𐶐�����B
     */
    private JTabbedPane loadDocuments() {

        // �h�L�������g�v���O�C�������[�h����
        PluginLoader<ChartDocument> loader = PluginLoader.load(ChartDocument.class, ClientContext.getPluginClassLoader());
        Iterator<ChartDocument> iterator = loader.iterator();

        int index = 0;
        providers = new Hashtable<String, ChartDocument>();
        JTabbedPane tab = new JTabbedPane();

        while (iterator.hasNext()) {

            try {
                ChartDocument plugin = iterator.next();
                
                if (index == 0) {
                    plugin.setContext(this);
                    plugin.start();
                }

                tab.addTab(plugin.getTitle(), plugin.getIconInfo(this), plugin.getUI());
                providers.put(String.valueOf(index), plugin);

                index += 1;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // �[���Ԗڂ�I�����Ă��� changeListener ���@�\������
        tab.setSelectedIndex(0);

        //
        // tab �� �v���O�C����x���������邽�߂̂� ChangeListener ��ǉ�����
        //
        tab.addChangeListener((ChangeListener) EventHandler.create(ChangeListener.class, this, "tabChanged", ""));

        return tab;
    }

    /**
     * �h�L�������g�^�u�Ƀv���O�C����x���������ǉ�����B
     */
    public void tabChanged(ChangeEvent e) {

        //
        // �I�����ꂽ�^�u�ԍ��ɑΉ�����v���O�C�����e�[�u�����猟������
        //
        int index = tabbedPane.getSelectedIndex();
        String key = String.valueOf(index);
        ChartDocument plugin = (ChartDocument) providers.get(key);

        if (plugin.getContext() == null) {
            //
            // �܂���������Ă��Ȃ��v���O�C���𐶐�����
            //
            plugin.setContext(ChartImpl.this);
            plugin.start();
            tabbedPane.setComponentAt(index, plugin.getUI());

        } else {
            //
            // ���ɐ����ς݃v���O�C���̏ꍇ�� enter() ���R�[������
            //
            plugin.enter();
        }
    }

    
//    public void newKarteOrg() {
//
//        logger.debug("entering newKarte");
//        String dept = getPatientVisit().getDeptNoTokenize();
//        String deptCode = getPatientVisit().getDepartmentCode();
//        String insuranceUid = getPatientVisit().getInsuranceUid();
//
//        // �V�K�h�L�������g�̃^�C�v�Ɖ\�ȃI�v�V������ݒ肷��
//        String docType = null;
//        Chart.NewKarteOption option = null;
//        KarteViewer base = null;
//
//        ChartDocument bridgeOrViewer = (ChartDocument) providers.get("0");
//
//        if (bridgeOrViewer instanceof DocumentBridgeImpl) {
//            logger.debug("bridgeOrViewer instanceof DocumentBridgeImpl");
//            DocumentBridgeImpl bridge = (DocumentBridgeImpl) bridgeOrViewer;
//            base = bridge.getBaseKarte();
//
//        } else if (bridgeOrViewer instanceof KarteDocumentViewer) {
//            logger.debug("bridgeOrViewer instanceof KarteDocumentViewer");
//            KarteDocumentViewer viwer = (KarteDocumentViewer) bridgeOrViewer;
//            base = viwer.getBaseKarte();
//        } else {
//            return;
//        }
//
//        if (base != null) {
//            // �쐬����h�L�������g�̃^�C�v�̓x�[�X�ɍ��킹��
//            logger.debug("base != null");
//            if (base.getDocType().equals(IInfoModel.DOCTYPE_KARTE)) {
//                logger.debug("base.getDocType().equals(IInfoModel.DOCTYPE_KARTE");
//                option = Chart.NewKarteOption.BROWSER_COPY_NEW;
//                docType = IInfoModel.DOCTYPE_KARTE;
//            } else {
//                logger.debug("base.getDocType().equals(IInfoModel.DOCTYPE_S_KARTE");
//                option = Chart.NewKarteOption.BROWSER_COPY_NEW;
//                docType = IInfoModel.DOCTYPE_S_KARTE;
//            }
//
//        } else {
//            // �x�[�X�̃J���e���Ȃ��ꍇ�A�Q���J���e���f�t�H���g�őI������
//            logger.debug("base == null");
//
//            Preferences prefs = Project.getPreferences();
//            boolean b2 = prefs.getBoolean(Project.DOUBLE_KARTE, true);
//            if (b2) {
//                option = Chart.NewKarteOption.BROWSER_NEW;
//                docType = IInfoModel.DOCTYPE_KARTE;
//            } else {
//                option = Chart.NewKarteOption.BROWSER_NEW;
//                docType = IInfoModel.DOCTYPE_S_KARTE;
//            }
//        }
//
//        //
//        // �V�K�J���e�쐬���Ɋm�F�_�C�A���O��\�����邩�ǂ���
//        //
//        NewKarteParams params = null;
//        Preferences prefs = Project.getPreferences();
//
//        if (prefs.getBoolean(Project.KARTE_SHOW_CONFIRM_AT_NEW, true)) {
//
//            // �V�K�J���e�_�C�A���O�փp�����[�^��n���A�R�s�[�V�K�̃I�v�V�����𐧌䂷��
//            logger.debug("show newKarteDialog");
//            params = getNewKarteParams(docType, option, null, dept, deptCode, insuranceUid);
//
//        } else {
//            // �ی��A�쐬���[�h�A�z�u���@���蓮�Őݒ肷��
//            params = new NewKarteParams(option);
//            params.setDocType(docType);
//            params.setDepartment(dept);
//            params.setDepartmentCode(deptCode);
//
//            //
//            // �ی�
//            //
//            PVTHealthInsuranceModel[] ins = getHealthInsurances();
//            params.setPVTHealthInsurance(ins[0]);
//            if (insuranceUid != null) {
//                for (int i = 0; i < ins.length; i++) {
//                    if (ins[i].getGUID() != null) {
//                        if (insuranceUid.equals(ins[i].getGUID())) {
//                            params.setPVTHealthInsurance(ins[i]);
//                            break;
//                        }
//                    }
//                }
//            }
//
//            //
//            // �쐬���[�h
//            //
//            switch (option) {
//
//                case BROWSER_NEW:
//                    params.setCreateMode(Chart.NewKarteMode.EMPTY_NEW);
//                    break;
//
//                case BROWSER_COPY_NEW:
//                    int cMode = prefs.getInt(Project.KARTE_CREATE_MODE, 0);
//                    if (cMode == 0) {
//                        params.setCreateMode(Chart.NewKarteMode.EMPTY_NEW);
//                    } else if (cMode == 1) {
//                        params.setCreateMode(Chart.NewKarteMode.APPLY_RP);
//                    } else if (cMode == 2) {
//                        params.setCreateMode(Chart.NewKarteMode.ALL_COPY);
//                    }
//                    break;
//            }
//
//            //
//            // �z�u���@
//            //
//            params.setOpenFrame(prefs.getBoolean(Project.KARTE_PLACE_MODE, true));
//
//        }
//
//        // �L�����Z�������ꍇ�̓��^�[������
//        if (params == null) {
//            return;
//        }
//
//        logger.debug("returned newKarteDialog");
//        DocumentModel editModel = null;
//        KarteEditor editor = null;
//
//        // �V���O���h�L�������g�𐶐�����ꍇ
//        if (params.getDocType().equals(IInfoModel.DOCTYPE_S_KARTE)) {
//            logger.debug("�V���O���h�L�������g�𐶐�����");
//            // Base�ɂȂ�J���e�����邩�ǂ����Ń��f���̐������قȂ�
//            if (params.getCreateMode() == Chart.NewKarteMode.EMPTY_NEW) {
//                logger.debug("empty new is selected");
//                editModel = getKarteModelToEdit(params);
//            } else {
//                logger.debug("copy new is selected");
//                editModel = getKarteModelToEdit(base.getModel(), params);
//            }
//
//            editor = createEditor();
//            editor.setModel(editModel);
//            editor.setEditable(true);
//            editor.setMode(KarteEditor.SINGLE_MODE);
//
//        } else {
//            logger.debug("2���J���e�𐶐�����");
//            // Base�ɂȂ�J���e�����邩�ǂ����Ń��f���̐������قȂ�
//            if (params.getCreateMode() == Chart.NewKarteMode.EMPTY_NEW) {
//                logger.debug("empty new is selected");
//                editModel = getKarteModelToEdit(params);
//            } else {
//                logger.debug("copy new is selected");
//                editModel = getKarteModelToEdit(base.getModel(), params);
//            }
//            editor = createEditor();
//            editor.setModel(editModel);
//            editor.setEditable(true);
//            editor.setMode(KarteEditor.DOUBLE_MODE);
//        }
//
//        if (params.isOpenFrame()) {
//            EditorFrame editorFrame = new EditorFrame();
//            editorFrame.setChart(this);
//            editorFrame.setKarteEditor(editor);
//            editorFrame.start();
//        } else {
//            editor.setContext(this);
//            editor.initialize();
//            editor.start();
//            this.addChartDocument(editor, params);
//        }
//    }
    
    /**
     * �V�K�J���e���쐬����B
     */    
    public void newKarte() {

        logger.debug("newKarte()");
        String dept = getPatientVisit().getDeptNoTokenize();
        String deptCode = getPatientVisit().getDepartmentCode();
        String insuranceUid = getPatientVisit().getInsuranceUid();

        // �V�K�h�L�������g�̃^�C�v=2���J���e�Ɖ\�ȃI�v�V������ݒ肷��
        String docType = IInfoModel.DOCTYPE_KARTE;
        Chart.NewKarteOption option = null;
        KarteViewer base = null;

        ChartDocument bridgeOrViewer = (ChartDocument) providers.get("0");

        if (bridgeOrViewer instanceof DocumentBridgeImpl) {
            // Chart��ʂ̃^�u�p�l��
            logger.debug("bridgeOrViewer instanceof DocumentBridgeImpl");
            DocumentBridgeImpl bridge = (DocumentBridgeImpl) bridgeOrViewer;
            base = bridge.getBaseKarte();

        } else if (bridgeOrViewer instanceof KarteDocumentViewer) {
            logger.debug("bridgeOrViewer instanceof KarteDocumentViewer");
            KarteDocumentViewer viwer = (KarteDocumentViewer) bridgeOrViewer;
            base = viwer.getBaseKarte();
        } else {
            return;
        }

        if (base != null) {
            logger.debug("base != null");
            if (base.getDocType().equals(IInfoModel.DOCTYPE_KARTE)) {
                logger.debug("base.getDocType().equals(IInfoModel.DOCTYPE_KARTE");
                option = Chart.NewKarteOption.BROWSER_COPY_NEW;
            } else {
                // �x�[�X�����Ă��Q���J���e�łȂ��ꍇ
                logger.debug("base.getDocType().equals(IInfoModel.DOCTYPE_S_KARTE");
                option = Chart.NewKarteOption.BROWSER_NEW;
            }

        } else {
            // �x�[�X�̃J���e���Ȃ��ꍇ
            logger.debug("base == null");
            option = Chart.NewKarteOption.BROWSER_NEW;
        }

        //
        // �V�K�J���e�쐬���Ɋm�F�_�C�A���O��\�����邩�ǂ���
        //
        NewKarteParams params = null;
        Preferences prefs = Project.getPreferences();

        if (prefs.getBoolean(Project.KARTE_SHOW_CONFIRM_AT_NEW, true)) {

            // �V�K�J���e�_�C�A���O�փp�����[�^��n���A�R�s�[�V�K�̃I�v�V�����𐧌䂷��
            logger.debug("show newKarteDialog");
            params = getNewKarteParams(docType, option, null, dept, deptCode, insuranceUid);

        } else {
            // �ی��A�쐬���[�h�A�z�u���@���蓮�Őݒ肷��
            params = new NewKarteParams(option);
            params.setDocType(docType);
            params.setDepartment(dept);
            params.setDepartmentCode(deptCode);

            // �ی�
            PVTHealthInsuranceModel[] ins = getHealthInsurances();
            params.setPVTHealthInsurance(ins[0]);
            if (insuranceUid != null) {
                for (int i = 0; i < ins.length; i++) {
                    if (ins[i].getGUID() != null) {
                        if (insuranceUid.equals(ins[i].getGUID())) {
                            params.setPVTHealthInsurance(ins[i]);
                            break;
                        }
                    }
                }
            }

            // �쐬���[�h
            switch (option) {

                case BROWSER_NEW:
                    params.setCreateMode(Chart.NewKarteMode.EMPTY_NEW);
                    break;

                case BROWSER_COPY_NEW:
                    int cMode = prefs.getInt(Project.KARTE_CREATE_MODE, 0);
                    if (cMode == 0) {
                        params.setCreateMode(Chart.NewKarteMode.EMPTY_NEW);
                    } else if (cMode == 1) {
                        params.setCreateMode(Chart.NewKarteMode.APPLY_RP);
                    } else if (cMode == 2) {
                        params.setCreateMode(Chart.NewKarteMode.ALL_COPY);
                    }
                    break;
            }

            // �z�u���@
            params.setOpenFrame(prefs.getBoolean(Project.KARTE_PLACE_MODE, true));

        }

        // �L�����Z�������ꍇ�̓��^�[������
        if (params == null) {
            return;
        }

        logger.debug("returned newKarteDialog");
        DocumentModel editModel = null;
        KarteEditor editor = null;
        
        // Base�ɂȂ�J���e�����邩�ǂ����Ń��f���̐������قȂ�
        if (params.getCreateMode() == Chart.NewKarteMode.EMPTY_NEW) {
            logger.debug("empty new is selected");
            editModel = getKarteModelToEdit(params);
        } else {
            logger.debug("copy new is selected");
            editModel = getKarteModelToEdit(base.getModel(), params);
        }
        editor = createEditor();
        editor.setModel(editModel);
        editor.setEditable(true);
        editor.setMode(KarteEditor.DOUBLE_MODE);
       
        if (params.isOpenFrame()) {
            EditorFrame editorFrame = new EditorFrame();
            editorFrame.setChart(this);
            editorFrame.setKarteEditor(editor);
            editorFrame.start();
        } else {
            editor.setContext(this);
            editor.initialize();
            editor.start();
            this.addChartDocument(editor, params);
        }
    }

    /**
     * EmptyNew �V�K�J���e�̃��f���𐶐�����B
     * @param params �쐬�p�����[�^�Z�b�g
     * @return �V�K�J���e�̃��f��
     */
    public DocumentModel getKarteModelToEdit(NewKarteParams params) {

        // �J���e���f���𐶐�����
        DocumentModel model = new DocumentModel();

        //
        // DocInfo��ݒ肷��
        //
        DocInfoModel docInfo = model.getDocInfo();

        // docId ����ID
        docInfo.setDocId(GUIDGenerator.generate(docInfo));

        // �����ړI
        docInfo.setPurpose(PURPOSE_RECORD);

        // DocumentType
        docInfo.setDocType(params.getDocType());

//        //
//        // �f�ÉȂ�ݒ肷��
//        // ��t��񂩂瓾�Ă���
//        //
//        String dept = params.getDepartment();
//        docInfo.setDepartmentDesc(dept); // department
//        
//        // �f�ÉȃR�[�h
//        // ��t����Ƃ��Ă��Ȃ��ꍇ....
//        String deptCode = params.getDepartmentCode();
//        if (deptCode == null) {
//            docInfo.setDepartment(MMLTable.getDepartmentCode(dept)); // dept.code
//        }
        docInfo.setDepartmentDesc(getPatientVisit().getDeptNoTokenize());
        docInfo.setDepartment(getPatientVisit().getDepartmentCode());


        // ���N�ی���ݒ肷��
        PVTHealthInsuranceModel insurance = params.getPVTHealthInsurance();
        docInfo.setHealthInsurance(insurance.getInsuranceClassCode());
        docInfo.setHealthInsuranceDesc(insurance.toString());
        docInfo.setHealthInsuranceGUID(insurance.getGUID());

        // Version��ݒ肷��
        VersionModel version = new VersionModel();
        version.initialize();
        docInfo.setVersionNumber(version.getVersionNumber());

        //
        // Document �� Status ��ݒ肷��
        // �V�K�J���e�̏ꍇ�� none
        //
        docInfo.setStatus(STATUS_NONE);

        return model;
    }

    /**
     * �R�s�[���ĐV�K�J���e�𐶐�����ꍇ�̃J���e���f���𐶐�����B
     * @param oldModel �R�s�[���̃J���e���f��
     * @param params �����p�����[�^�Z�b�g
     * @return �V�K�J���e�̃��f��
     */
    public DocumentModel getKarteModelToEdit(DocumentModel oldModel, NewKarteParams params) {

        //
        // �V�K���f�����쐬���A�\������Ă��郂�f���̓��e���R�s�[����
        //
        DocumentModel newModel = new DocumentModel();
        boolean applyRp = params.getCreateMode() == Chart.NewKarteMode.APPLY_RP ? true : false;
        copyModel(oldModel, newModel, applyRp);

        //
        // �V�K�J���e�� DocInfo ��ݒ肷��
        //
        DocInfoModel docInfo = newModel.getDocInfo();

        // ����ID
        docInfo.setDocId(GUIDGenerator.generate(docInfo));

        // �����ړI
        docInfo.setPurpose(PURPOSE_RECORD);

        // DocumentType
        docInfo.setDocType(params.getDocType());

        //
        // �f�ÉȂ�ݒ肷�� ��t��񂩂�ݒ肷��
        //
        String dept = params.getDepartment();
        docInfo.setDepartmentDesc(dept);

//        // �f�ÉȃR�[�h
//        // ��t����Ƃ��Ă��Ȃ��ꍇ....
//        String deptCode = params.getDepartmentCode();
//        if (deptCode == null) {
//            docInfo.setDepartment(MMLTable.getDepartmentCode(dept)); // dept.code
//        }        
        docInfo.setDepartmentDesc(getPatientVisit().getDeptNoTokenize());
        docInfo.setDepartment(getPatientVisit().getDepartmentCode());

        // ���N�ی���ݒ肷��
        PVTHealthInsuranceModel insurance = params.getPVTHealthInsurance();
        docInfo.setHealthInsurance(insurance.getInsuranceClassCode());
        //docInfo.setHealthInsuranceDesc(insurance.getInsuranceClass());
        docInfo.setHealthInsuranceDesc(insurance.toString());
        docInfo.setHealthInsuranceGUID(insurance.getGUID());

        // Version��ݒ肷��
        VersionModel version = new VersionModel();
        version.initialize();
        docInfo.setVersionNumber(version.getVersionNumber());

        //
        // Document �� Status ��ݒ肷��
        // �V�K�J���e�̏ꍇ�� none
        //
        docInfo.setStatus(STATUS_NONE);

        return newModel;
    }

    /**
     * �C���̏ꍇ�̃J���e���f���𐶐�����B
     * @param oldModel �C���Ώۂ̃J���e���f��
     * @return �V�����ł̃J���e���f��
     */
    public DocumentModel getKarteModelToEdit(DocumentModel oldModel) {

        // �C���Ώۂ� DocInfo ���擾����
        DocInfoModel oldDocInfo = oldModel.getDocInfo();

        // �V�����ł̃��f���Ƀ��W���[���Ɖ摜���R�s�[����
        DocumentModel newModel = new DocumentModel();
        copyModel(oldModel, newModel, false);

        //
        // �V�����ł� DocInfo ��ݒ肷��
        //
        DocInfoModel newInfo = newModel.getDocInfo();

        // ����ID
        newInfo.setDocId(GUIDGenerator.generate(newInfo));

        // �V�����ł� firstConfirmDate = ���ɂȂ�ł� firstConfirmDate
        newInfo.setFirstConfirmDate(oldDocInfo.getFirstConfirmDate());

        // docType = old one
        newInfo.setDocType(oldDocInfo.getDocType());

        // purpose = old one
        newInfo.setPurpose(oldDocInfo.getPurpose());

        //
        // �^�C�g���������p��
        //
        newInfo.setTitle(oldDocInfo.getTitle());

        //
        // �f�ÉȂ�ݒ肷�� 
        // ���ɂȂ�ł̏��𗘗p����
        //
        newInfo.setDepartmentDesc(oldDocInfo.getDepartmentDesc());
        newInfo.setDepartment(oldDocInfo.getDepartment());

        //
        // ���N�ی���ݒ肷��
        // ���ɂȂ�ł̏��𗘗p����
        // 
        newInfo.setHealthInsuranceDesc(oldDocInfo.getHealthInsuranceDesc());
        newInfo.setHealthInsurance(oldDocInfo.getHealthInsurance());
        newInfo.setHealthInsuranceGUID(oldDocInfo.getHealthInsuranceGUID());

        //
        // �e����ID��ݒ肷��
        //
        newInfo.setParentId(oldDocInfo.getDocId());
        newInfo.setParentIdRelation(PARENT_OLD_EDITION);

        //
        // old PK ��ݒ肷��
        //
        newInfo.setParentPk(oldModel.getId());

        //
        // Version��ݒ肷��
        // new = old + 1.0
        VersionModel newVersion = new VersionModel();
        newVersion.setVersionNumber(oldDocInfo.getVersionNumber());
        newVersion.incrementNumber(); // version number ++
        newInfo.setVersionNumber(newVersion.getVersionNumber());

        //
        // Document Status ��ݒ肷��
        // ���ɂȂ�ł� status (Final | Temporal | Modified)
        //
        newInfo.setStatus(oldDocInfo.getStatus());

        return newModel;
    }

    /**
     * �J���e�G�f�B�^�𐶐�����B
     * @return �J���e�G�f�B�^
     */
    public KarteEditor createEditor() {
        KarteEditor editor = null;
        try {
            editor = new KarteEditor();
            editor.addMMLListner(mmlListener);
            editor.addCLAIMListner(claimListener);
        } catch (Exception e) {
            logger.warn(e);
            editor = null;
        }
        return editor;
    }

    //
    // ���f�����R�s�[����
    // ToDO �Q�Ƃł͂����Ȃ�
    // DocInfo �̐ݒ�͂Ȃ�
    // 
    private void copyModel(DocumentModel oldModel, DocumentModel newModel, boolean applyRp) {

        //
        // �O�񏈕���K�p����ꍇ
        //
        if (applyRp) {
            Collection<ModuleModel> modules = oldModel.getModules();
            if (modules != null) {
                Collection<ModuleModel> apply = new ArrayList<ModuleModel>(5);
                for (ModuleModel bean : modules) {
                    IInfoModel model = bean.getModel();
                    if (model instanceof ClaimBundle) {
                        //
                        // �������ǂ����𔻒肷��
                        //
                        if (((ClaimBundle) model).getClassCode().startsWith("2")) {
                            apply.add(bean);
                        }
                    }
                }

                if (apply.size() != 0) {
                    newModel.setModules(apply);
                }
            }

        } else {
            // �S�ăR�s�[
            newModel.setModules(oldModel.getModules());
            newModel.setSchema(oldModel.getSchema());
        }
    }

    /**
     * �J���e�쐬���Ƀ_�A�C���O���I�[�v�����A�ی���I��������B
     *
     * @return NewKarteParams
     */
    public NewKarteParams getNewKarteParams(String docType, Chart.NewKarteOption option, JFrame f, String dept, String deptCode, String insuranceUid) {

        NewKarteParams params = new NewKarteParams(option);
        params.setDocType(docType);
        params.setDepartment(dept);
        params.setDepartmentCode(deptCode);

        // ���҂̌��N�ی��R���N�V����
        Collection<PVTHealthInsuranceModel> insurances = pvt.getPatient().getPvtHealthInsurances();

        // �R���N�V������ null �̏ꍇ�͎���ی���ǉ�����
        if (insurances == null || insurances.size() == 0) {
            insurances = new ArrayList<PVTHealthInsuranceModel>(1);
            PVTHealthInsuranceModel model = new PVTHealthInsuranceModel();
            model.setInsuranceClass(INSURANCE_SELF);
            model.setInsuranceClassCode(INSURANCE_SELF_CODE);
            model.setInsuranceClassCodeSys(INSURANCE_SYS);
            insurances.add(model);
        }

        // �ی��R���N�V������z��ɕϊ����A�p�����[�^�ɃZ�b�g����
        // ���[�U�����̒��̕ی���I������
        PVTHealthInsuranceModel[] insModels = (PVTHealthInsuranceModel[]) insurances.toArray(new PVTHealthInsuranceModel[insurances.size()]);
        params.setInsurances(insModels);
        int index = 0;
        if (insuranceUid != null) {
            for (int i = 0; i < insModels.length; i++) {
                if (insModels[i].getGUID() != null) {
                    if (insModels[i].getGUID().equals(insuranceUid)) {
                        index = i;
                        break;
                    }
                }
            }
        }
        params.setInitialSelectedInsurance(index);

        String text = option == Chart.NewKarteOption.BROWSER_MODIFY
                ? resMap.getString("modifyKarteTitle")
                : resMap.getString("newKarteTitle");

        text = ClientContext.getFrameTitle(text);

        // ���[�_���_�C�A���O��\������
        JFrame frame = f != null ? f : getFrame();
        NewKarteDialog od = new NewKarteDialog(frame, text);
        od.setValue(params);
        od.start();

        // �߂�l�����^�[������
        params = (NewKarteParams) od.getValue();

        return params;
    }

    /**
     * ���҂̌��N�ی���Ԃ��B
     * @return ���҂̌��N�ی��z��
     */
    @Override
    public PVTHealthInsuranceModel[] getHealthInsurances() {

        // ���҂̌��N�ی�
        Collection<PVTHealthInsuranceModel> insurances = pvt.getPatient().getPvtHealthInsurances();

        if (insurances == null || insurances.size() == 0) {
            insurances = new ArrayList<PVTHealthInsuranceModel>(1);
            PVTHealthInsuranceModel model = new PVTHealthInsuranceModel();
            model.setInsuranceClass(INSURANCE_SELF);
            model.setInsuranceClassCode(INSURANCE_SELF_CODE);
            model.setInsuranceClassCodeSys(INSURANCE_SYS);
            insurances.add(model);
        }

        return (PVTHealthInsuranceModel[]) insurances.toArray(new PVTHealthInsuranceModel[insurances.size()]);
    }

    /**
     * �^�u�Ƀh�L�������g��ǉ�����B
     * @param doc �ǉ�����h�L�������g
     * @param params �ǉ�����h�L�������g�̏���ێ����� NewKarteParams
     */
    public void addChartDocument(ChartDocument doc, NewKarteParams params) {
        String title = null;
        if (params.getPVTHealthInsurance() != null) {
            title = getTabTitle(params.getDepartment(), params.getPVTHealthInsurance().getInsuranceClass());
        } else {
            title = getTabTitle(params.getDepartment(), null);
        }
        tabbedPane.addTab(title, doc.getUI());
        int index = tabbedPane.getTabCount() - 1;
        providers.put(String.valueOf(index), doc);
        tabbedPane.setSelectedIndex(index);
    }

    /**
     * �^�u�Ƀh�L�������g��ǉ�����B
     * @param title �^�u�^�C�g��
     */
    public void addChartDocument(ChartDocument doc, String title) {
        tabbedPane.addTab(title, doc.getUI());
        int index = tabbedPane.getTabCount() - 1;
        providers.put(String.valueOf(index), doc);
        tabbedPane.setSelectedIndex(index);
    }

    /**
     * �V�K�J���e�p�̃^�u�^�C�g�����쐬����
     * @param insurance �ی���
     * @return �^�u�^�C�g��
     */
    public String getTabTitle(String dept, String insurance) {
        String[] depts = dept.split("\\s*,\\s*");
        StringBuilder buf = new StringBuilder();
        buf.append(resMap.getString("newKarteTabTitle"));
        if (insurance != null) {
            buf.append("(");
            buf.append(depts[0]);
            buf.append("�E");
            buf.append(insurance);
            buf.append(")");
        }
        return buf.toString();
    }

    /**
     * �V�K�����쐬�őI�����ꂽ�v���O�C�����N������B
     * 
     * @param pluginClass �N������v���O�C���̃N���X��
     */ 
    private void invokePlugin(String pluginClass) {

        try {
            NChartDocument doc = (NChartDocument) Class.forName(
                    pluginClass,
                    true,
                    ClientContext.getPluginClassLoader()).newInstance();
            
            if (doc instanceof KarteEditor) {
                String dept = getPatientVisit().getDeptNoTokenize();
                String deptCode = getPatientVisit().getDepartmentCode();
                String insuranceUid = getPatientVisit().getInsuranceUid();
                Chart.NewKarteOption option = Chart.NewKarteOption.BROWSER_NEW;
                String docType = IInfoModel.DOCTYPE_S_KARTE;
                NewKarteParams params = new NewKarteParams(option);
                params.setDocType(docType);
                params.setDepartment(dept);
                params.setDepartmentCode(deptCode);

                //
                // �ی�
                //
                PVTHealthInsuranceModel[] ins = getHealthInsurances();
                params.setPVTHealthInsurance(ins[0]);
                if (insuranceUid != null) {
                    for (int i = 0; i < ins.length; i++) {
                        if (ins[i].getGUID() != null) {
                            if (insuranceUid.equals(ins[i].getGUID())) {
                                params.setPVTHealthInsurance(ins[i]);
                                break;
                            }
                        }
                    }
                }

                DocumentModel editModel = getKarteModelToEdit(params);
                KarteEditor editor = (KarteEditor) doc;
                editor.setModel(editModel);
                editor.setEditable(true);
                editor.setContext(this);
                editor.setMode(KarteEditor.SINGLE_MODE);
                editor.initialize();
                editor.start();
                this.addChartDocument(editor, params);
                
            } else {
                doc.setContext(this);
                doc.start();
                addChartDocument(doc, doc.getTitle());
            }

        } catch (Exception e) {
            logger.warn(e);
        }
    }

    /**
     * �J���e�ȊO�̕������쐬����B
     */
    public void newDocument() {

        // �g���|�C���g�V�K�����̃v���O�C�������X�g�A�b�v���A
        // ���X�g�őI��������
        ArrayList<NameValuePair> documents = new ArrayList<NameValuePair>(3);
        PluginLister<NChartDocument> lister = PluginLister.list(NChartDocument.class, ClientContext.getPluginClassLoader());
        LinkedHashMap<String, String> nproviders = lister.getProviders();
        if (nproviders != null) {
            Iterator<String> iter = nproviders.keySet().iterator();
            while (iter.hasNext()) {
                String cmd = iter.next();
                String clsName = nproviders.get(cmd);
                NameValuePair pair = new NameValuePair(cmd, clsName);
                documents.add(pair);
                logger.debug(cmd + " = " + clsName);
            }
        }

        if (documents.size() == 0) {
            logger.debug("No plugins");
            return;
        }
        
        // docs �A�C�R�������x����Injection����
        JLabel newDocsLabel = new JLabel();
        newDocsLabel.setName("newDocsLabel");
        resMap.injectComponent(newDocsLabel);

        final JList docList = new JList(documents.toArray());
        docList.setCellRenderer(new StripeRenderer());
        
        JPanel panel = new JPanel(new BorderLayout(7, 0));
        panel.add(newDocsLabel,BorderLayout.WEST);
        panel.add(docList, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(6,6,5,5));
        JPanel content = new JPanel(new BorderLayout());
        content.add(panel, BorderLayout.CENTER);
        content.setBorder(BorderFactory.createTitledBorder("�쐬���镶��"));

        final JButton okButton = new JButton("����");
        final JButton cancelButton = new JButton("�����");
        Object[] options = new Object[]{okButton, cancelButton};

        JOptionPane jop = new JOptionPane(
                content,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                options,
                okButton);

        final JDialog dialog = jop.createDialog(getFrame(), ClientContext.getFrameTitle("�V�K�����쐬"));
        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(WindowEvent e) {
                docList.requestFocusInWindow();
            }
        });

        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
                dialog.dispose();
                NameValuePair pair = (NameValuePair) docList.getSelectedValue();
                String clsName = pair.getValue();
                invokePlugin(clsName);
            }
        });
        okButton.setEnabled(false);

        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });

        docList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    int index = docList.getSelectedIndex();
                    if (index >= 0) {
                        okButton.setEnabled(true);
                    }
                }
            }
        });

        dialog.setVisible(true);
    }

    /**
     * �S�Ẵh�L�������g��ۑ�����B
     * @param dirtyList ���ۑ��h�L�������g�̃��X�g
     */
    private void saveAll(java.util.List<UnsavedDocument> dirtyList) {

        if (dirtyList == null || dirtyList.size() == 0) {
            return;
        }

        try {
            for (UnsavedDocument undoc : dirtyList) {
                if (undoc.isNeedSave()) {
                    ChartDocument doc = (ChartDocument) providers.get(String.valueOf(undoc.getIndex()));
                    if (doc != null && doc.isDirty()) {
                        tabbedPane.setSelectedIndex(undoc.getIndex());
                        doc.save();
                    }
                }
            }

        } catch (Exception e) {
            logger.warn(e);
        }
    }

    /**
     * �h�L�������g�̂Ȃ���dirty�̂��̂����邩�ǂ�����Ԃ��B
     * @return dirty�̎�true
     */
    private java.util.List<UnsavedDocument> dirtyList() {
        java.util.List<UnsavedDocument> ret = null;
        int count = tabbedPane.getTabCount();
        for (int i = 0; i < count; i++) {
            ChartDocument doc = (ChartDocument) providers.get(String.valueOf(i));
            if (doc != null && doc.isDirty()) {
                if (ret == null) {
                    ret = new ArrayList<UnsavedDocument>(3);
                }
                ret.add(new UnsavedDocument(i, doc));
            }
        }
        return ret;
    }

    /**
     * CloseBox ���N���b�N���ꂽ���̏������s���B
     */
    public void processWindowClosing() {
        close();
    }

    /**
     * �`���[�g�E�C���h�E�����B
     */
    @Override
    public void close() {

        //
        // ���ۑ��h�L�������g������ꍇ�̓_�C�A���O��\����
        // �ۑ����邩�ǂ������m�F����
        //
        java.util.List<UnsavedDocument> dirtyList = dirtyList();

        if (dirtyList != null && dirtyList.size() > 0) {

            String saveAll = resMap.getString("unsavedtask.saveText");     // �ۑ�;
            String discard = resMap.getString("unsavedtask.discardText");  // �j��;
            String question = resMap.getString("unsavedtask.question");    // ���ۑ��̃h�L�������g������܂��B�ۑ����܂��� ?
            String title = resMap.getString("unsavedtask.title");          // ���ۑ�����
            String cancelText = (String) UIManager.get("OptionPane.cancelButtonText");

            Object[] message = new Object[dirtyList.size() + 1];
            message[0] = (Object) question;
            int index = 1;
            for (UnsavedDocument doc : dirtyList) {
                message[index++] = doc.getCheckBox();
            }

            int option = JOptionPane.showOptionDialog(
                    getFrame(),
                    message,
                    ClientContext.getFrameTitle(title),
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{saveAll, discard, cancelText},
                    saveAll);

            switch (option) {
                case 0:
                    // save
                    saveAll(dirtyList);
                    stop();
                    break;

                case 1:
                    // discard
                    stop();
                    break;

                case 2:
                    // cancel
                    break;
            }
        } else {
            stop();
        }
    }

    @Override
    public void stop() {
        logger.debug("stop");
        if (beeperHandle != null) {
            boolean b = beeperHandle.cancel(true);
            logger.debug("beeperHandle.cancel = " + b);
        }
        if (scheduler != null) {
            scheduler.shutdown();
            logger.debug("scheduler.shutdown");
        }
        if (providers != null) {
            for (Iterator<String> iter = providers.keySet().iterator(); iter.hasNext();) {
                ChartDocument doc = providers.get(iter.next());
                if (doc != null) {
                    doc.stop();
                }
            }
            providers.clear();
        }
        mediator.dispose();
        inspector.dispose();
        try {
            ClientContext.getLocalStorage().save(getFrame().getBounds(), "chartBounds.xml");

        } catch (IOException ex) {
            logger.warn(ex);
        }
        getFrame().setVisible(false);
        getFrame().setJMenuBar(null);
        getFrame().dispose();
    }

    protected abstract class ChartState {

        public ChartState() {
        }

        public abstract void controlMenu();
    }

    /**
     * ReadOnly ���[�U�� State �N���X�B
     */
    protected final class ReadOnlyState extends ChartState {

        public ReadOnlyState() {
        }

        /**
         * �V�K�J���e�쐬�y�яC�����j���[�� disable �ɂ���B
         */
        @Override
        public void controlMenu() {
            mediator.getAction(GUIConst.ACTION_NEW_KARTE).setEnabled(false);
            mediator.getAction(GUIConst.ACTION_MODIFY_KARTE).setEnabled(false);
        }
    }

    /**
     * �ی��؂��Ȃ��ꍇ�� State �N���X�B
     */
    protected final class NoInsuranceState extends ChartState {

        public NoInsuranceState() {
        }

        @Override
        public void controlMenu() {
            mediator.getAction(GUIConst.ACTION_NEW_KARTE).setEnabled(false);
        }
    }

    /**
     * �ʏ�� State �N���X�B
     */
    protected final class OrdinalyState extends ChartState {

        public OrdinalyState() {
        }

        @Override
        public void controlMenu() {
            mediator.getAction(GUIConst.ACTION_NEW_KARTE).setEnabled(true);
        }
    }

    /**
     * State Manager �N���X�B
     */
    protected final class StateMgr {

        private ChartState readOnlyState = new ReadOnlyState();
        private ChartState noInsuranceState = new NoInsuranceState();
        private ChartState ordinalyState = new OrdinalyState();
        private ChartState currentState;

        public StateMgr() {
            if (isReadOnly()) {
                enterReadOnlyState();
            } else {
                enterOrdinalyState();
            }
        }

        public void enterReadOnlyState() {
            currentState = readOnlyState;
            currentState.controlMenu();
        }

        public void enterNoInsuranceState() {
            currentState = noInsuranceState;
            currentState.controlMenu();
        }

        public void enterOrdinalyState() {
            currentState = ordinalyState;
            currentState.controlMenu();
        }

        public void controlMenu() {
            currentState.controlMenu();
        }
    }

    /**** Chart Instance ���Ǘ����邽�߂� static �N���X **/
    /**
     * �I�[�v�����Ă���S�C���X�^���X��ێ����郊�X�g��Ԃ��B
     * @return �I�[�v�����Ă��� ChartPlugin �̃��X�g
     */
    public static ArrayList<ChartImpl> getAllChart() {
        return allCharts;
    }

    /**
     * �`���[�g�X�e�[�g�̑������X�i��o�^����B
     * @param prop �����v���p�e�B��
     * @param l �������X�i
     */
    public static void addPropertyChangeListener(String prop,
            PropertyChangeListener l) {
        boundSupport.addPropertyChangeListener(prop, l);
    }

    /**
     * �`���[�g�X�e�[�g�̑������X�i���폜����B
     * @param prop �����v���p�e�B��
     * @param l �������X�i
     */
    public static void removePropertyChangeListener(String prop,
            PropertyChangeListener l) {
        boundSupport.removePropertyChangeListener(prop, l);
    }

    /**
     * �`���[�g�E�C���h�E�̃I�[�v����ʒm����B
     * @param opened �I�[�v������ ChartPlugin
     */
    public static void windowOpened(ChartImpl opened) {
        //
        // �C���X�^���X��ێ����郊�X�g�֒ǉ�����
        //
        allCharts.add(opened);

        //
        // PVT (Chart) �̏�Ԃ�ݒ肷��
        //
        PatientVisitModel model = opened.getPatientVisit();
        int oldState = model.getState();
        int newState = 0;

        switch (oldState) {

            case CLOSE_NONE:
                newState = OPEN_NONE;
                break;

            case CLOSE_SAVE:
                newState = OPEN_SAVE;
                break;

            default:
                throw new RuntimeException("Invalid Chart State");
        }

        //opened.getDocumentHistory().requestFocus();

        //
        // �ʒm����
        //
        model.setState(newState);
        boundSupport.firePropertyChange(ChartImpl.CHART_STATE, null, model);
    }

    /**
     * �`���[�g�E�C���h�E�̃N���[�Y��ʒm����B
     * @param closed �N���[�Y���� ChartPlugin
     */
    public static void windowClosed(ChartImpl closed) {

        //
        // �C���X�^���X���X�g�����菜��
        //
        if (allCharts.remove(closed)) {

            //
            // �`���[�g�̏�Ԃ� PVT �ɐݒ肷��
            //
            PatientVisitModel model = closed.getPatientVisit();
            int oldState = model.getState();
            int newState = 0;

            switch (oldState) {

                case OPEN_NONE:
                    newState = CLOSE_NONE;
                    break;

                case OPEN_SAVE:
                    newState = CLOSE_SAVE;
                    break;

                default:
                    throw new RuntimeException("Invalid Chart State");

            }

            //System.out.println("oldState=" + oldState);
            //System.out.println("newState=" + newState);

            //
            // �ʒm����
            //
            model.setState(newState);
            boundSupport.firePropertyChange(ChartImpl.CHART_STATE, null, model);
            closed = null;
        }
    }

    /**
     * �`���[�g��Ԃ̕ω���ʒm����B
     * @param �ω��̂����� ChartPlugin
     */
    public static void fireChanged(ChartImpl changed) {
        PatientVisitModel model = changed.getPatientVisit();
        model.setState(changed.getChartState());
    }
}
