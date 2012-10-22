package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;

import org.apache.log4j.Logger;

import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.infomodel.RoleModel;
import open.dolphin.helper.ComponentMemory;
import open.dolphin.helper.WindowSupport;
import open.dolphin.helper.MenuSupport;
import open.dolphin.helper.TaskProgressMonitor;
import open.dolphin.project.*;
import open.dolphin.server.PVTServer;
import open.dolphin.plugin.PluginLoader;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceManager;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;

/**
 * �A�v���P�[�V�����̃��C���E�C���h�E�N���X�B
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class Dolphin extends Application implements MainWindow {

    // Window �� Menu �T�|�[�g
    private WindowSupport windowSupport;
    // Mediator
    private Mediator mediator;
    // ��Ԑ���
    private StateManager stateMgr;
    // �v���O�C���̃v���o�C�_
    private HashMap<String, MainService> providers;
    // plugin���i�[���� tabbedPane
    private JTabbedPane tabbedPane;
    // timerTask �֘A
    private javax.swing.Timer taskTimer;
    // ���K�[
    private Logger bootLogger;
    // �v�����^�[�Z�b�g�A�b�v��MainWindow�݂̂ōs���A�ݒ肳�ꂽ PageFormat�e�v���O�C�����g�p����
    private PageFormat pageFormat;
    // ���ݒ�p�� Properties
    private Properties saveEnv;
    // BlockGlass
    private BlockGlass blockGlass;
    // StampBox
    private StampBoxPlugin stampBox;
    // ��t��M�T�[�o
    private PVTServer pvtServer;
    // CLAIM ���X�i
    private ClaimMessageListener sendClaim;
    // MML ���X�i
    private MmlMessageListener sendMml;
    // URL �N���X���[�_
    private URLClassLoader pluginClassLoader;
    // ResourceMap
    private ResourceMap resource;
    // �I���\�t���O
    private boolean canExit;

    /**
     * Creates new MainWindow
     */
    public Dolphin() {
    }

    @Override
    protected void initialize(String[] args) {

        // ClientContext �𐶐�����
        ClientContextStub stub = new ClientContextStub();
        ClientContext.setClientContextStub(stub);

        // �v���W�F�N�g�X�^�u�𐶐�����
        Project.setProjectStub(new ProjectStub());

        // Resources
        ApplicationContext ctxt = getContext();
        ResourceManager mgr = ctxt.getResourceManager();
        resource = mgr.getResourceMap(Dolphin.class);
        ClientContext.setApplicationContext(ctxt);

        // PluginClassLoader
        pluginClassLoader = ClientContext.getPluginClassLoader();

        // ���K�[���擾����
        bootLogger = ClientContext.getBootLogger();

        // Mac Application Menu
        com.apple.eawt.Application fApplication = com.apple.eawt.Application.getApplication();
        fApplication.setEnabledPreferencesMenu(true);
        fApplication.addApplicationListener(
                new com.apple.eawt.ApplicationAdapter() {

                    @Override
                    public void handleAbout(com.apple.eawt.ApplicationEvent e) {
                        showAbout();
                        e.setHandled(true);
                    }

                    @Override
                    public void handleOpenApplication(
                            com.apple.eawt.ApplicationEvent e) {
                    }

                    @Override
                    public void handleOpenFile(com.apple.eawt.ApplicationEvent e) {
                    }

                    @Override
                    public void handlePreferences(
                            com.apple.eawt.ApplicationEvent e) {
                        doPreference();
                    }

                    @Override
                    public void handlePrintFile(
                            com.apple.eawt.ApplicationEvent e) {
                    }

                    @Override
                    public void handleQuit(com.apple.eawt.ApplicationEvent e) {
                        processExit();
                    }
                });
    }

    @Override
    protected void startup() {

        // ExitListner ��o�^����
        this.addExitListener(new ExitAdapter());

        // ���O�C���_�C�A���O��\�����F�؂��s��
        PropertyChangeListener pl = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent e) {

                LoginDialog.LoginStatus result = (LoginDialog.LoginStatus) e.getNewValue();

                switch (result) {
                    case AUTHENTICATED:
                        startServices();
                        initComponents();
                        break;
                    case NOT_AUTHENTICATED:
                        setCanExit(true);
                        exit();
                        break;
                    case CANCELD:
                        setCanExit(true);
                        exit();
                        break;
                }
            }
        };
        LoginDialog login = new LoginDialog();
        login.addPropertyChangeListener("LOGIN_PROP", pl);
        login.start();
    }

    private boolean isCanExit() {
        return canExit;
    }

    private void setCanExit(boolean canExit) {
        this.canExit = canExit;
    }

    class ExitAdapter implements ExitListener {

        public boolean canExit(EventObject e) {
            return isCanExit();
        }

        public void willExit(EventObject event) {
        }
    }

    /**
     * �N�����̃o�b�N�O���E���h�Ŏ��s�����ׂ��^�X�N���s���B
     */
    private void startServices() {

        // �v���O�C���̃v���o�C�_�}�b�v�𐶐�����
        setProviders(new HashMap<String, MainService>());

        // ���ݒ�_�C�A���O�ŕύX�����ꍇ������̂ŕۑ�����
        saveEnv = new Properties();

        // PVT Sever ���N������
        if (Project.getUseAsPVTServer()) {
            startPvtServer();

        } else {
            saveEnv.put(GUIConst.KEY_PVT_SERVER, GUIConst.SERVICE_NOT_RUNNING);
        }

        // CLAIM���M�𐶐�����
        if (Project.getSendClaim()) {
            startSendClaim();

        } else {
            saveEnv.put(GUIConst.KEY_SEND_CLAIM, GUIConst.SERVICE_NOT_RUNNING);
        }
        if (Project.getClaimAddress() != null) {
            saveEnv.put(GUIConst.ADDRESS_CLAIM, Project.getClaimAddress());
        }

        // MML���M�𐶐�����
        if (Project.getSendMML()) {
            startSendMml();

        } else {
            saveEnv.put(GUIConst.KEY_SEND_MML, GUIConst.SERVICE_NOT_RUNNING);
        }
        if (Project.getCSGWPath() != null) {
            saveEnv.put(GUIConst.CSGW_PATH, Project.getCSGWPath());
        }
    }

    private void initComponents() {

        // �ݒ�ɕK�v�Ȓ萔���R���e�L�X�g����擾����
        String windowTitle = resource.getString("title");
        Rectangle setBounds = new Rectangle(0, 0, 1000, 690);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int defaultX = (screenSize.width - setBounds.width) / 2;
        int defaultY = (screenSize.height - setBounds.height) / 2;
        int defaultWidth = 666;
        int defaultHeight = 678;

        // WindowSupport �𐶐����� ���̎��_�� Frame,WindowMenu ������MenuBar ����������Ă���
        String title = ClientContext.getFrameTitle(windowTitle);
        System.out.println(title);
        windowSupport = WindowSupport.create(title);
        JFrame myFrame = windowSupport.getFrame();		// MainWindow �� JFrame
        JMenuBar myMenuBar = windowSupport.getMenuBar();	// MainWindow �� JMenuBar

        // Window�ɂ��̃N���X�ŗL�̐ݒ������
        Point loc = new Point(defaultX, defaultY);
        Dimension size = new Dimension(defaultWidth, defaultHeight);
        myFrame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                processExit();
            }
        });
        ComponentMemory cm = new ComponentMemory(myFrame, loc, size, this);
        cm.setToPreferenceBounds();

        // BlockGlass ��ݒ肷��
        blockGlass = new BlockGlass();
        myFrame.setGlassPane(blockGlass);

        // mainWindow�̃��j���[�𐶐������j���[�o�[�ɒǉ�����
        mediator = new Mediator(this);
        AbstractMenuFactory appMenu = AbstractMenuFactory.getFactory();
        appMenu.setMenuSupports(mediator, null);
        appMenu.build(myMenuBar);
        mediator.registerActions(appMenu.getActionMap());

        // mainWindow�̃R���e���gGUI�𐶐���Frame�ɒǉ�����
        tabbedPane = new JTabbedPane();
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(true);
        content.add(tabbedPane, BorderLayout.CENTER);
        content.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        myFrame.setContentPane(content);

        //
        // �^�u�y�C���Ɋi�[���� Plugin �����[�h����
        //
        PluginLoader<MainComponent> loader = PluginLoader.load(MainComponent.class, pluginClassLoader);
        Iterator<MainComponent> iter = loader.iterator();

        MainComponent[] top = new MainComponent[2];
        List<MainComponent> list = new ArrayList<MainComponent>(3);

        while (iter.hasNext()) {
            MainComponent plugin = iter.next();
            if (plugin.getName().equals("��t���X�g")) {
                top[0] = plugin;
            } else if (plugin.getName().equals("���Ҍ���")) {
                top[1] = plugin;
            } else {
                list.add(plugin);
            }
        }
        list.add(0, top[0]);
        list.add(1, top[1]);

        int index = 0;
        for (MainComponent plugin : list) {

            if (index == 0) {
                plugin.setContext(this);
                plugin.start();
                tabbedPane.addTab(plugin.getName(), plugin.getUI());
                providers.put(String.valueOf(index), plugin);
                mediator.addChain(plugin);

            } else {
                tabbedPane.addTab(plugin.getName(), plugin.getUI());
                providers.put(String.valueOf(index), plugin);
            }

            index += 1;
        }
        list.clear();

        //
        // �^�u�̐؂�ւ��� plugin.enter() ���R�[������
        //
        tabbedPane.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                int index = tabbedPane.getSelectedIndex();
                MainComponent plugin = (MainComponent) providers.get(String.valueOf(index));
                if (plugin.getContext() == null) {
                    plugin.setContext(Dolphin.this);
                    plugin.start();
                    tabbedPane.setComponentAt(index, plugin.getUI());
                } else {
                    plugin.enter();
                }
                mediator.addChain(plugin);
            }
        });

        // StaeMagr���g�p���ă��C���E�C���h�E�̏�Ԃ𐧌䂷��
        stateMgr = new StateManager();
        stateMgr.processLogin(true);

        stampBox = new StampBoxPlugin();
        stampBox.setContext(Dolphin.this);

        ApplicationContext appCtx = ClientContext.getApplicationContext();
        Application app = appCtx.getApplication();

        final Callable<Boolean> task = stampBox.getStartingTask();

        Task stampTask = new Task<Boolean, Void>(app) {

            @Override
            protected Boolean doInBackground() throws Exception {
                bootLogger.debug("stampTask doInBackground");
                try {

                    Boolean result = task.call();
                    return result;

                } catch (Exception e) {
                    bootLogger.warn(e);
                }

                return new Boolean(false);
            }

            @Override
            protected void succeeded(Boolean result) {
                bootLogger.debug("stampTask succeeded");
                if (result.booleanValue()) {
                    stampBox.start();
                    stampBox.getFrame().setVisible(true);
                    providers.put("stampBox", stampBox);
                    windowSupport.getFrame().setVisible(true);
                } else {
                    System.exit(1);
                }
            }

            @Override
            protected void failed(Throwable cause) {
                bootLogger.debug("stampTask failed");
                bootLogger.debug(cause.getCause());
                bootLogger.debug(cause.getMessage());
                System.exit(1);
            }

            @Override
            protected void cancelled() {
                bootLogger.debug("stampTask cancelled");
                System.exit(1);
            }
        };

        TaskMonitor taskMonitor = appCtx.getTaskMonitor();
        String message = "�X�^���v��";
        String note = "�X�^���v�c���[��ǂݍ���ł��܂�...";
        Component c = windowSupport.getFrame();
        TaskTimerMonitor w = new TaskTimerMonitor(stampTask, taskMonitor, c, message, note, 200, 60 * 1000);
        taskMonitor.addPropertyChangeListener(w);

        appCtx.getTaskService().execute(stampTask);
    }

    public BlockGlass getGlassPane() {
        return blockGlass;
    }

    public MainService getPlugin(String id) {
        return providers.get(id);
    }

    public HashMap<String, MainService> getProviders() {
        return providers;
    }

    public void setProviders(HashMap<String, MainService> providers) {
        this.providers = providers;
    }

    /**
     * �J���e���I�[�v������B
     * @param pvt ���җ��@���
     */
    public void openKarte(PatientVisitModel pvt) {
        PluginLoader<Chart> loader = PluginLoader.load(Chart.class, pluginClassLoader);
        Iterator<Chart> iter = loader.iterator();
        if (iter.hasNext()) {
            Chart chart = iter.next();
            chart.setContext(this);
            chart.setPatientVisit(pvt);                 //
            chart.setReadOnly(Project.isReadOnly());    // RedaOnlyProp
            chart.start();
        }
    }

    /**
     * �V�K�f�Ø^���쐬����B
     */
    public void addNewPatient() {

        PluginLoader<NewKarte> loader = PluginLoader.load(NewKarte.class, pluginClassLoader);
        Iterator<NewKarte> iter = loader.iterator();
        if (iter.hasNext()) {
            NewKarte newKarte = iter.next();
            newKarte.setContext(this);
            newKarte.start();
        }
    }

    public MenuSupport getMenuSupport() {
        return mediator;
    }

    /**
     * MainWindow �̃A�N�V������Ԃ��B
     * @param name Action��
     * @return Action
     */
    public Action getAction(String name) {
        return mediator.getAction(name);
    }

    public JMenuBar getMenuBar() {
        return windowSupport.getMenuBar();
    }

    public void registerActions(ActionMap actions) {
        mediator.registerActions(actions);
    }

    public void enabledAction(String name, boolean b) {
        mediator.enabledAction(name, b);
    }

    public JFrame getFrame() {
        return windowSupport.getFrame();
    }

    public PageFormat getPageFormat() {
        if (pageFormat == null) {
            PrinterJob printJob = PrinterJob.getPrinterJob();
            if (printJob != null) {
                pageFormat = printJob.defaultPage();
            }
        }
        return pageFormat;
    }

    /**
     * �u���b�N����B
     */
    public void block() {
        blockGlass.block();
    }

    /**
     * �u���b�N����������B
     */
    public void unblock() {
        blockGlass.unblock();
    }

    /**
     * PVTServer ���J�n����B
     */
    private void startPvtServer() {
        PluginLoader<PVTServer> loader = PluginLoader.load(PVTServer.class, pluginClassLoader);
        Iterator<PVTServer> iter = loader.iterator();
        if (iter.hasNext()) {
            pvtServer = iter.next();
            pvtServer.setContext(this);
            pvtServer.start();
            providers.put("pvtServer", pvtServer);
            saveEnv.put(GUIConst.KEY_PVT_SERVER, GUIConst.SERVICE_RUNNING);
        }
    }

    /**
     * CLAIM ���M���J�n����B
     */
    private void startSendClaim() {
        PluginLoader<ClaimMessageListener> loader = PluginLoader.load(ClaimMessageListener.class, pluginClassLoader);
        Iterator<ClaimMessageListener> iter = loader.iterator();
        if (iter.hasNext()) {
            sendClaim = iter.next();
            sendClaim.setContext(this);
            sendClaim.start();
            providers.put("sendClaim", sendClaim);
            saveEnv.put(GUIConst.KEY_SEND_CLAIM, GUIConst.SERVICE_RUNNING);
        }
    }

    /**
     * MML���M���J�n����B
     */
    private void startSendMml() {
        PluginLoader<MmlMessageListener> loader = PluginLoader.load(MmlMessageListener.class, pluginClassLoader);
        Iterator<MmlMessageListener> iter = loader.iterator();
        if (iter.hasNext()) {
            sendMml = iter.next();
            sendMml.setContext(this);
            sendMml.start();
            providers.put("sendMml", sendMml);
            saveEnv.put(GUIConst.KEY_SEND_MML, GUIConst.SERVICE_RUNNING);
        }
    }

    /**
     * �v�����^�[���Z�b�g�A�b�v����B
     */
    public void printerSetup() {

        Runnable r = new Runnable() {

            public void run() {
                PrinterJob printJob = PrinterJob.getPrinterJob();
                if (pageFormat != null) {
                    pageFormat = printJob.pageDialog(pageFormat);
                } else {
                    pageFormat = printJob.defaultPage();
                    pageFormat = printJob.pageDialog(pageFormat);
                }
            }
        };
        Thread t = new Thread(r);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

    /**
     * �J���e�̊��ݒ���s���B
     */
    public void setKarteEnviroment() {
        ProjectSettingDialog sd = new ProjectSettingDialog();
        sd.addPropertyChangeListener("SETTING_PROP", new PreferenceListener());
        sd.setLoginState(stateMgr.isLogin());
        sd.setProject("karteSetting");
        sd.start();
    }

    /**
     * ���ݒ���s���B
     */
    public void doPreference() {
        ProjectSettingDialog sd = new ProjectSettingDialog();
        sd.addPropertyChangeListener("SETTING_PROP", new PreferenceListener());
        sd.setLoginState(stateMgr.isLogin());
        sd.setProject(null);
        sd.start();
    }

    /**
     * ���ݒ�̃��X�i�N���X�B���ݒ肪�I������Ƃ����֒ʒm�����B
     */
    class PreferenceListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent e) {

            if (e.getPropertyName().equals("SETTING_PROP")) {

                boolean valid = ((Boolean) e.getNewValue()).booleanValue();

                if (valid) {

                    // �ݒ�̕ω��𒲂ׁA�T�[�r�X�̐�����s��
                    ArrayList<String> messages = new ArrayList<String>(2);

                    // PvtServer
                    boolean oldRunning = saveEnv.getProperty(GUIConst.KEY_PVT_SERVER).equals(GUIConst.SERVICE_RUNNING) ? true : false;
                    boolean newRun = Project.getUseAsPVTServer();
                    boolean start = ((!oldRunning) && newRun) ? true : false;
                    boolean stop = ((oldRunning) && (!newRun)) ? true : false;

                    if (start) {
                        startPvtServer();
                        messages.add("��t��M���J�n���܂����B");
                    } else if (stop && pvtServer != null) {
                        pvtServer.stop();
                        pvtServer = null;
                        saveEnv.put(GUIConst.KEY_PVT_SERVER, GUIConst.SERVICE_NOT_RUNNING);
                        messages.add("��t��M���~���܂����B");
                    }

                    // SendClaim
                    oldRunning = saveEnv.getProperty(GUIConst.KEY_SEND_CLAIM).equals(GUIConst.SERVICE_RUNNING) ? true : false;
                    newRun = Project.getSendClaim();
                    start = ((!oldRunning) && newRun) ? true : false;
                    stop = ((oldRunning) && (!newRun)) ? true : false;

                    boolean restart = false;
                    String oldAddress = saveEnv.getProperty(GUIConst.ADDRESS_CLAIM);
                    String newAddress = Project.getClaimAddress();
                    if (oldAddress != null && newAddress != null && (!oldAddress.equals(newAddress)) && newRun) {
                        restart = true;
                    }

                    if (start) {
                        startSendClaim();
                        saveEnv.put(GUIConst.ADDRESS_CLAIM, newAddress);
                        messages.add("CLAIM���M���J�n���܂����B(���M�A�h���X=" + newAddress + ")");

                    } else if (stop && sendClaim != null) {
                        sendClaim.stop();
                        sendClaim = null;
                        saveEnv.put(GUIConst.KEY_SEND_CLAIM, GUIConst.SERVICE_NOT_RUNNING);
                        saveEnv.put(GUIConst.ADDRESS_CLAIM, newAddress);
                        messages.add("CLAIM���M���~���܂����B");

                    } else if (restart) {
                        sendClaim.stop();
                        sendClaim = null;
                        startSendClaim();
                        saveEnv.put(GUIConst.ADDRESS_CLAIM, newAddress);
                        messages.add("CLAIM���M�����X�^�[�g���܂����B(���M�A�h���X=" + newAddress + ")");
                    }

                    // SendMML
                    oldRunning = saveEnv.getProperty(GUIConst.KEY_SEND_MML).equals(GUIConst.SERVICE_RUNNING) ? true : false;
                    newRun = Project.getSendMML();
                    start = ((!oldRunning) && newRun) ? true : false;
                    stop = ((oldRunning) && (!newRun)) ? true : false;

                    restart = false;
                    oldAddress = saveEnv.getProperty(GUIConst.CSGW_PATH);
                    newAddress = Project.getCSGWPath();
                    if (oldAddress != null && newAddress != null && (!oldAddress.equals(newAddress)) && newRun) {
                        restart = true;
                    }

                    if (start) {
                        startSendMml();
                        saveEnv.put(GUIConst.CSGW_PATH, newAddress);
                        messages.add("MML���M���J�n���܂����B(���M�A�h���X=" + newAddress + ")");

                    } else if (stop && sendMml != null) {
                        sendMml.stop();
                        sendMml = null;
                        saveEnv.put(GUIConst.KEY_SEND_MML, GUIConst.SERVICE_NOT_RUNNING);
                        saveEnv.put(GUIConst.CSGW_PATH, newAddress);
                        messages.add("MML���M���~���܂����B");

                    } else if (restart) {
                        sendMml.stop();
                        sendMml = null;
                        startSendMml();
                        saveEnv.put(GUIConst.CSGW_PATH, newAddress);
                        messages.add("MML���M�����X�^�[�g���܂����B(���M�A�h���X=" + newAddress + ")");
                    }

                    if (messages.size() > 0) {
                        String[] msgArray = messages.toArray(new String[messages.size()]);
                        Object msg = msgArray;
                        Component cmp = null;
                        String title = ClientContext.getString("settingDialog.title");

                        JOptionPane.showMessageDialog(
                                cmp,
                                msg,
                                ClientContext.getFrameTitle(title),
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        }
    }

    private boolean isDirty() {

        // ���ۑ��̃J���e������ꍇ�͌x�������^�[������
        // �J���e��ۑ��܂��͔j�����Ă���ēx���s����
        boolean dirty = false;

        // Chart �𒲂ׂ�
        ArrayList<ChartImpl> allChart = ChartImpl.getAllChart();
        if (allChart != null && allChart.size() > 0) {
            for (ChartImpl chart : allChart) {
                if (chart.isDirty()) {
                    dirty = true;
                    break;
                }
            }
        }

        // �ۑ����ĂȂ����̂�����΃��^�[������
        if (dirty) {
            return false;
        }

        // EditorFrame�̃`�F�b�N���s��
        java.util.List<Chart> allEditorFrames = EditorFrame.getAllEditorFrames();
        if (allEditorFrames != null && allEditorFrames.size() > 0) {
            for (Chart chart : allEditorFrames) {
                if (chart.isDirty()) {
                    dirty = true;
                    break;
                }
            }
        }

        return dirty;
    }

    private List<Callable<Boolean>> getStoppingTask() {

        // StoppingTask ���W�߂�
        List<Callable<Boolean>> tasks = new ArrayList<Callable<Boolean>>(1);

        try {
            HashMap<String, MainService> cloneMap = null;
            synchronized (providers) {
                cloneMap = (HashMap) providers.clone();
            }
            Iterator iter = cloneMap.values().iterator();
            while (iter != null && iter.hasNext()) {
                MainService pl = (MainService) iter.next();
                if (pl instanceof MainTool) {
                    Callable<Boolean> task = ((MainTool) pl).getStoppingTask();
                    if (task != null) {
                        tasks.add(task);
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            bootLogger.warn(ex.toString());
        }

        return tasks;
    }

    public void processExit() {

        if (isDirty()) {
            alertDirty();
            return;
        }

        final List<Callable<Boolean>> tasks = getStoppingTask();

        if (tasks.size() == 0) {
            setCanExit(true);
            exit();
        }

        ApplicationContext appCtx = ClientContext.getApplicationContext();

        Task stampTask = new Task<Boolean, Void>(Dolphin.this) {

            @Override
            protected Boolean doInBackground() throws Exception {
                bootLogger.debug("stampTask doInBackground");
                boolean success = true;
                for (Callable<Boolean> c : tasks) {
                    Boolean result = c.call();
                    if (!result.booleanValue()) {
                        success = false;
                        break;
                    }
                }
                return new Boolean(success);
            }

            @Override
            protected void succeeded(Boolean result) {
                bootLogger.debug("stampTask succeeded");
                if (result.booleanValue()) {
                    setCanExit(true);
                    exit();
                } else {
                    doStoppingAlert();
                }
            }

            @Override
            protected void failed(Throwable cause) {
                bootLogger.warn("stampTask failed");
                bootLogger.warn(cause);
            }
        };

        TaskMonitor taskMonitor = appCtx.getTaskMonitor();
        String message = resource.getString("exitDolphin.taskTitle");
        String note = resource.getString("exitDolphin.savingNote");
        Component c = getFrame();
        TaskTimerMonitor w = new TaskTimerMonitor(stampTask, taskMonitor, c, message, note, 200, 120 * 1000);
        taskMonitor.addPropertyChangeListener(w);

        appCtx.getTaskService().execute(stampTask);
    }

    /**
     * ���ۑ��̃h�L�������g������ꍇ�̌x����\������B
     */
    private void alertDirty() {
        String msg0 = resource.getString("exitDolphin.msg0"); //"���ۑ��̃h�L�������g������܂��B";
        String msg1 = resource.getString("exitDolphin.msg1"); //"�ۑ��܂��͔j��������ɍēx���s���Ă��������B";
        String taskTitle = resource.getString("exitDolphin.taskTitle");
        JOptionPane.showMessageDialog(
                (Component) null,
                new Object[]{msg0, msg1},
                ClientContext.getFrameTitle(taskTitle),
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * �I���������ɃG���[���������ꍇ�̌x�����_�C�A���O��\������B
     * @param errorTask �G���[���������^�X�N
     * @return ���[�U�̑I��l
     */
    private void doStoppingAlert() {

        String msg1 = resource.getString("exitDolphin.err.msg1");
        String msg2 = resource.getString("exitDolphin.err.msg2");
        String msg3 = resource.getString("exitDolphin.err.msg3");
        String msg4 = resource.getString("exitDolphin.err.msg4");
        Object message = new Object[]{msg1, msg2, msg3, msg4};

        // �I������
        String exitOption = resource.getString("exitDolphin.exitOption");

        // �L�����Z������
        String cancelOption = resource.getString("exitDolphin.cancelOption");

        // ���ۑ�
        String taskTitle = resource.getString("exitDolphin.taskTitle");

        String title = ClientContext.getFrameTitle(taskTitle);

        String[] options = new String[]{cancelOption, exitOption};

        int option = JOptionPane.showOptionDialog(
                null, message, title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);

        if (option == 1) {
            setCanExit(true);
            exit();
        }
    }

    @Override
    protected void shutdown() {

        if (providers != null) {

            try {
                HashMap cloneMap = null;
                synchronized (providers) {
                    cloneMap = (HashMap) providers.clone();
                }
                Iterator iter = cloneMap.values().iterator();
                while (iter != null && iter.hasNext()) {
                    MainService pl = (MainService) iter.next();
                    pl.stop();
                }

            } catch (Exception e) {
                e.printStackTrace();
                bootLogger.warn(e.toString());
            }
        }

        if (windowSupport != null) {
            JFrame myFrame = windowSupport.getFrame();
            myFrame.setVisible(false);
            myFrame.dispose();
        }
        bootLogger.info("�A�v���P�[�V�������I�����܂�");
        System.exit(0);
    }

    /**
     * ���[�U�̃p�X���[�h��ύX����B
     */
    public void changePassword() {

        PluginLoader<ChangeProfile> loader = PluginLoader.load(ChangeProfile.class, pluginClassLoader);
        Iterator<ChangeProfile> iter = loader.iterator();
        if (iter.hasNext()) {
            ChangeProfile cp = iter.next();
            cp.setContext(this);
            cp.start();
        }
    }

    /**
     * ���[�U�o�^���s���B�Ǘ��҃��j���[�B
     */
    public void addUser() {

        PluginLoader<AddUser> loader = PluginLoader.load(AddUser.class, pluginClassLoader);
        Iterator<AddUser> iter = loader.iterator();
        if (iter.hasNext()) {
            AddUser au = iter.next();
            au.setContext(this);
            au.start();
        }
    }

    public void invokeToolPlugin(String pluginClass) {

        try {
            MainTool tool = (MainTool) Class.forName(pluginClass, true, pluginClassLoader).newInstance();
            tool.setContext(this);
            tool.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listJars(List<UpdateObject> list, File dir) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                listJars(list, file);
            } else if (file.isFile()) {
                String path = file.getPath();
                if (path.toLowerCase().endsWith(".jar")) {
                    path = path.replace(File.separatorChar, '/');
                    String[] pathEle = path.split("/plugins/");
                    bootLogger.debug("update taget: " + pathEle[1]);
                    list.add(new UpdateObject(pathEle[1]));
                }
            }
        }
    }

    private List<UpdateObject> listUpdateTargets() {

        bootLogger.debug("listUpdateTargets()");

        List<UpdateObject> ret = new ArrayList<UpdateObject>();
        String dolphin = resource.getString("dolphin.jar");
        UpdateObject uo = new UpdateObject(dolphin);
        ret.add(uo);
        File pluginDir = new File(ClientContext.getPluginsDirectory());
        listJars(ret, pluginDir);

        Properties prop = null;
        try {
            prop = (Properties) ClientContext.getLocalStorage().load("lastModified.xml");
        } catch (Exception e) {
            bootLogger.warn(e);
        }

        if (prop != null) {
            bootLogger.debug("lastModified.xml loaded");
            for (UpdateObject o : ret) {
                String localStr = prop.getProperty(o.getName());
                if (localStr != null) {
                    bootLogger.debug(o.getName() + " = " + localStr);
                    o.setLocalLast(Long.parseLong(localStr));
                }
            }
        }

        return ret;
    }

    private URL getRemoteURL(String resName) throws MalformedURLException {

        StringBuilder sb = new StringBuilder();

        if (ClientContext.isMac()) {
            sb.append(resource.getString("update.url.mac"));
        } else if (ClientContext.isWin()) {
            sb.append(resource.getString("update.url.win"));
        } else if (ClientContext.isLinux()) {
            sb.append(resource.getString("update.url.linux"));
        } else {
            sb.append(resource.getString("update.url.linux"));
        }

        sb.append(resName);
        String urlStr = sb.toString();
        bootLogger.debug("remote url = " + urlStr);

        return new URL(urlStr);
    }

    private File getUpdateTarget(String resName) {

        bootLogger.debug("getUpdateTarget()");
        bootLogger.debug("resName = " + resName);

        StringBuilder sb = new StringBuilder();
        if (resName.equals(resource.getString("dolphin.jar"))) {
            bootLogger.debug("resName.equals(resource.getString(dolphin.jar");
            String str = ClientContext.getLocation("dolphin.jar");
            sb.append(str);
            bootLogger.debug("1 added = " + str);
        } else {
            String str = ClientContext.getPluginsDirectory();
            sb.append(str);
            bootLogger.debug("2 added = " + str);
        }
        if (ClientContext.isWin()) {
            String dest = resName.replace('/', File.separatorChar);
            sb.append(File.separator);
            sb.append(dest);
        } else {
            sb.append(File.separator);
            sb.append(resName);
        }
        String targetStr = sb.toString();
        bootLogger.debug("write file = " + targetStr);
        return new File(targetStr);
    }

    private void writeUpdates(final List<UpdateObject> list) {

        ApplicationContext appCtx = ClientContext.getApplicationContext();
        Application app = appCtx.getApplication();

        Task task = new Task<Boolean, Void>(app) {

            @Override
            protected Boolean doInBackground() throws Exception {
                bootLogger.debug("writeUpdates task doInBackground");
                boolean result = false;
                Properties prop = new Properties();
                for (UpdateObject uo : list) {
                    if (!uo.isNew()) {
                        continue;
                    }
                    File dest = getUpdateTarget(uo.getName());
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
                    out.write(uo.getBytes());
                    out.flush();
                    out.close();
                    prop.setProperty(uo.getName(), String.valueOf(uo.getRemoteLast()));
                    bootLogger.debug(dest.getPath() + " ���X�V���܂���");
                }
                ClientContext.getLocalStorage().save(prop, "lastModified.xml");
                result = true;
                return new Boolean(result);
            }

            @Override
            protected void succeeded(Boolean result) {
                bootLogger.debug("writeUpdates task succeeded");
                if (result.booleanValue()) {

                    String title = resource.getString("update.title.text");
                    StringBuilder sb = new StringBuilder();
                    sb.append(resource.getString("update.success.msg1"));
                    sb.append("\n");
                    sb.append(resource.getString("update.success.msg2"));
                    String msg = sb.toString();

                    // Show succeeded message
                    JOptionPane.showMessageDialog(
                            null,
                            msg,
                            ClientContext.getFrameTitle(title),
                            JOptionPane.INFORMATION_MESSAGE);

                    shutdown();
                }
            }

            @Override
            protected void failed(Throwable cause) {
                bootLogger.warn("writeUpdates task failed");
                bootLogger.warn(cause);
                String errorMsg = resource.getString("update.failed.msg");
                String title = resource.getString("update.title.text");
                JOptionPane.showMessageDialog(
                        null,
                        errorMsg,
                        ClientContext.getFrameTitle(title),
                        JOptionPane.ERROR_MESSAGE);
            }
        };

        TaskMonitor taskMonitor = appCtx.getTaskMonitor();
        String message = resource.getString("update.title.text");
        String note = resource.getString("update.writing.note");
        Component c = null;
        TaskTimerMonitor w = new TaskTimerMonitor(task, taskMonitor, c, message, note, 200, 120 * 1000);
        taskMonitor.addPropertyChangeListener(w);
        appCtx.getTaskService().execute(task);
    }

    public void update() {

        // �X�V���� JAR �t�@�C��
        // OpenDolphin-1.3.jar
        // Plugin�f�B���N�g������ jar

        // �X�V���� JAR �t�@�C���̃��X�g�ƃV���A���ԍ��𓾂�
        final List<UpdateObject> list = listUpdateTargets();

        // �����[�g�T�[�o�́@�Ή����� JAR �̃V���A���ԍ��𓾂�
        ApplicationContext appCtx = ClientContext.getApplicationContext();
        Application app = appCtx.getApplication();
        Task task = new Task<Boolean, Void>(app) {

            @Override
            protected Boolean doInBackground() throws Exception {
                bootLogger.debug("update check doInBackground");
                for (UpdateObject uo : list) {
                    URL url = getRemoteURL(uo.getName());
                    URLConnection con = url.openConnection();
                    long remote = con.getLastModified();
                    uo.setRemoteLast(remote);
                    int length = con.getContentLength();
                    uo.setContentLength(length);

                    StringBuilder sb = new StringBuilder();
                    sb.append("Remote Info: ");
                    sb.append(uo.getName());
                    sb.append(",");
                    sb.append(remote);
                    sb.append(",");
                    sb.append(length);
                    bootLogger.debug(sb.toString());
                }
                return new Boolean(true);
            }

            @Override
            protected void succeeded(Boolean update) {
                bootLogger.debug("update check succeeded");
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        showUpdateStatus(list);
                    }
                });
            }

            @Override
            protected void failed(Throwable cause) {
                bootLogger.warn("update check failed");
                bootLogger.warn(cause);
            }
        };

        TaskMonitor taskMonitor = appCtx.getTaskMonitor();
        String message = resource.getString("update.title.text");
        String note = resource.getString("update.checking.note");
        Component c = null; //getFrame();
        TaskTimerMonitor w = new TaskTimerMonitor(task, taskMonitor, c, message, note, 200, 60 * 1000);
        taskMonitor.addPropertyChangeListener(w);
        appCtx.getTaskService().execute(task);
    }

    private void showUpdateStatus(List<UpdateObject> list) {

        boolean update = false;
        for (UpdateObject uo : list) {
            if (uo.isNew()) {
                update = true;
                break;
            }
        }
        if (update) {
            //
            // �X�V�\�Ȃ��̂�����ꍇ
            //
            String updateYes = resource.getString("update.yes.text");
            String updateNo = resource.getString("update.no.text");
            String title = resource.getString("update.title.text");
            Object[] cstOptions = new Object[]{updateYes, updateNo};

            String msg = resource.getString("update.available.msg");
            bootLogger.info(msg);
            int select = JOptionPane.showOptionDialog(
                    null,
                    msg,
                    ClientContext.getFrameTitle(title),
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    ClientContext.getImageIcon(resource.getString("update.dialog.icon")),
                    cstOptions,
                    updateYes);
            if (select == 0) {
                updateSaveEnv(list);
            }

        } else {
            // �X�V�\�Ȃ��̂��Ȃ��ꍇ
            String msg = resource.getString("update.noUpdate.msg");
            String title = resource.getString("update.title.tex");
            bootLogger.info(msg);
            JOptionPane.showMessageDialog(null, msg, title,
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * OpenDolphin Clinet ���X�V����B
     */
    private void updateSaveEnv(final List<UpdateObject> list) {

        if (isDirty()) {
            alertDirty();
            return;
        }

        final List<Callable<Boolean>> tasks = getStoppingTask();

        ApplicationContext appCtx = ClientContext.getApplicationContext();
        Application app = appCtx.getApplication();

        Task stampTask = new Task<Boolean, Void>(app) {

            @Override
            protected Boolean doInBackground() throws Exception {
                bootLogger.debug("updateSaveEnv doInBackground");
                boolean success = true;
                for (Callable<Boolean> c : tasks) {
                    Boolean result = c.call();
                    if (!result.booleanValue()) {
                        success = false;
                        break;
                    }
                }
                return new Boolean(success);
            }

            @Override
            protected void succeeded(final Boolean result) {
                bootLogger.debug("updateSaveEnv succeeded");
                Runnable r = new Runnable() {

                    public void run() {
                        if (result.booleanValue()) {
                            downloadUpdates(list);
                        } else {
                            doStoppingAlert();
                        }
                    }
                };
                SwingUtilities.invokeLater(r);
            }

            @Override
            protected void failed(Throwable cause) {
                bootLogger.warn("updateSaveEnv failed");
                bootLogger.warn(cause);
            }
        };

        TaskMonitor taskMonitor = appCtx.getTaskMonitor();
        String message = resource.getString("exitDolphin.taskTitle");
        String note = resource.getString("exitDolphin.savingNote");
        Component c = null; //getFrame();
        TaskTimerMonitor w = new TaskTimerMonitor(stampTask, taskMonitor, c, message, note, 200, 120 * 1000);
        taskMonitor.addPropertyChangeListener(w);
        appCtx.getTaskService().execute(stampTask);
    }

    private void downloadUpdates(final List<UpdateObject> list) {

        ApplicationContext appCtx = ClientContext.getApplicationContext();
        Application app = appCtx.getApplication();

        Task task = new Task<Void, Integer>(app) {

            @Override
            protected Void doInBackground() throws Exception {

                bootLogger.debug("downloadUpdates doInBackground");

                int total = 0;

                for (UpdateObject uo : list) {
                    if (!uo.isNew()) {
                        continue;
                    }
                    int contentLength = uo.getContentLength();
                    if (contentLength > 0) {
                        total += contentLength;
                    }
                }

                int current = 0;
                bootLogger.debug("total length = " + total);

                for (UpdateObject uo : list) {

                    if (!uo.isNew()) {
                        continue;
                    }

                    URL url = getRemoteURL(uo.getName());
                    URLConnection con = url.openConnection();
                    int contentLength = con.getContentLength();

                    // Create streams
                    DataInputStream din = new DataInputStream(new BufferedInputStream(con.getInputStream()));
                    ByteArrayOutputStream bo = new ByteArrayOutputStream();
                    BufferedOutputStream bout = new BufferedOutputStream(bo);
                    byte aByte;
                    int cur = 0;
                    setMessage(uo.getName() + "���_�E�����[�h���Ă��܂�...   ");

                    // Read untill EOF
                    while (cur < contentLength) {
                        // Read byte
                        aByte = din.readByte();
                        bout.write(aByte);
                        cur++;
                        current++;
                        int percent = (int) (((float) current / (float) total) * 100F);
                        setProgress(new Integer(percent));
                    }

                    bout.flush();
                    uo.setBytes(bo.toByteArray());

                    din.close();
                    bo.close();
                    bout.close();
                }

                return null;
            }

            @Override
            protected void succeeded(Void result) {
                bootLogger.debug("downloadUpdates succeeded");
                Runnable r = new Runnable() {

                    public void run() {
                        writeUpdates(list);
                    }
                };
                SwingUtilities.invokeLater(r);
            }

            @Override
            protected void failed(Throwable cause) {
                bootLogger.warn("downloadUpdates failed");
                bootLogger.warn(cause);
            }

            @Override
            protected void cancelled() {
                bootLogger.debug("downloadUpdates cancelled");
            }
        };
        TaskMonitor taskMonitor = appCtx.getTaskMonitor();
        String message = resource.getString("update.title.text");
        String note = resource.getString("update.downloading.note");
        Component c = null; //getFrame();
        int min = 0;
        int max = 100;

        new TaskProgressMonitor(task, taskMonitor, c, message, note, min, max);
        appCtx.getTaskService().execute(task);
    }

    /**
     * �h���t�B���T�|�[�g���I�[�v������B
     */
    public void browseDolphinSupport() {
        browseURL(resource.getString("menu.dolphinSupportUrl"));
    }

    /**
     * �h���t�B���v���W�F�N�g���I�[�v������B
     */
    public void browseDolphinProject() {
        browseURL(resource.getString("menu.dolphinUrl"));
    }

    /**
     * MedXML���I�[�v������B
     */
    public void browseMedXml() {
        browseURL(resource.getString("menu.medXmlUrl"));
    }

    /**
     * SG���I�[�v������B
     */
    public void browseSeaGaia() {
        browseURL(resource.getString("menu.seaGaiaUrl"));
    }

    /**
     * URL���I�[�v������B
     * @param url URL
     */
    private void browseURL(String url) {

        try {
            if (ClientContext.isMac()) {
                ProcessBuilder builder = new ProcessBuilder("open", url);
                builder.start();

            } else if (ClientContext.isWin()) {
                ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "start", url);
                builder.start();

            } else {
                String[] browsers = {
                    "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"
                };
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++) {
                    if (Runtime.getRuntime().exec(
                            new String[]{"which", browsers[count]}).waitFor() == 0) {
                        browser = browsers[count];
                    }
                    if (browser == null) {
                        throw new Exception("Could not find web browser");
                    } else {
                        Runtime.getRuntime().exec(new String[]{browser, url});
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * About ��\������B
     */
    public void showAbout() {
        AbstractProjectFactory f = Project.getProjectFactory();
        f.createAboutDialog();
    }

    /**
     * �V�F�[�}�{�b�N�X��\������B
     */
    public void showSchemaBox() {
        ImageBox imageBox = new ImageBox();
        imageBox.setContext(this);
        imageBox.start();
    }

    /**
     * �X�^���v�{�b�N�X��\������B
     */
    public void showStampBox() {
        if (stampBox != null) {
            stampBox.enter();
        }
    }

    /**
     * Mediator
     */
    protected final class Mediator extends MenuSupport {

        public Mediator(Object owner) {
            super(owner);
        }

        // global property �̐���
        @Override
        public void menuSelected(MenuEvent e) {
        }

        @Override
        public void registerActions(ActionMap actions) {
            super.registerActions(actions);
        // ���C���E�C���h�E�Ȃ̂ŕ��邾���͖����ɂ���
        //getAction(GUIConst.ACTION_WINDOW_CLOSING).setEnabled(false);
        }
    }

    /**
     * MainWindowState
     */
    abstract class MainWindowState {

        public MainWindowState() {
        }

        public abstract void enter();

        public abstract boolean isLogin();
    }

    /**
     * LoginState
     */
    class LoginState extends MainWindowState {

        public LoginState() {
        }

        public boolean isLogin() {
            return true;
        }

        public void enter() {

            // Menu�𐧌䂷��
            mediator.disableAllMenus();

            String[] enables = new String[]{
                GUIConst.ACTION_PRINTER_SETUP,
                GUIConst.ACTION_PROCESS_EXIT,
                GUIConst.ACTION_SET_KARTE_ENVIROMENT,
                GUIConst.ACTION_SHOW_STAMPBOX,
                GUIConst.ACTION_NEW_PATIENT,
                GUIConst.ACTION_SHOW_SCHEMABOX,
                GUIConst.ACTION_CHANGE_PASSWORD,
                GUIConst.ACTION_CONFIRM_RUN,
                GUIConst.ACTION_SOFTWARE_UPDATE,
                GUIConst.ACTION_BROWS_DOLPHIN,
                GUIConst.ACTION_BROWS_DOLPHIN_PROJECT,
                GUIConst.ACTION_BROWS_MEDXML,
                GUIConst.ACTION_SHOW_ABOUT
            };
            mediator.enableMenus(enables);

            Action addUserAction = mediator.getAction(GUIConst.ACTION_ADD_USER);
            boolean admin = false;
            Collection<RoleModel> roles = Project.getUserModel().getRoles();
            for (RoleModel model : roles) {
                if (model.getRole().equals(GUIConst.ROLE_ADMIN)) {
                    admin = true;
                    break;
                }
            }
            addUserAction.setEnabled(admin);
        }
    }

    /**
     * LogoffState
     */
    class LogoffState extends MainWindowState {

        public LogoffState() {
        }

        public boolean isLogin() {
            return false;
        }

        public void enter() {
            mediator.disableAllMenus();
        }
    }

    /**
     * StateManager
     */
    class StateManager {

        private MainWindowState loginState = new LoginState();
        private MainWindowState logoffState = new LogoffState();
        private MainWindowState currentState = logoffState;

        public StateManager() {
        }

        public boolean isLogin() {
            return currentState.isLogin();
        }

        public void processLogin(boolean b) {
            currentState = b ? loginState : logoffState;
            currentState.enter();
        }
    }

    public static void main(String[] args) {
        Application.launch(Dolphin.class, args);
    }
}
