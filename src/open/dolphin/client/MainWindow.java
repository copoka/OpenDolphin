/*
 * MainWindow.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2003-2007 Digital Globe, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package open.dolphin.client;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;

import org.apache.log4j.Logger;

import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.infomodel.RoleModel;
import open.dolphin.plugin.*;
import open.dolphin.plugin.helper.ComponentMemory;
import open.dolphin.plugin.helper.MenuBarBuilder;
import open.dolphin.plugin.helper.WindowSupport;
import open.dolphin.plugin.helper.MenuSupport;
import open.dolphin.project.*;
import open.dolphin.server.PVTClientServer;
import open.dolphin.util.ReflectMonitor;

/**
 * �A�v���P�[�V�����̃��C���E�C���h�E�N���X�B
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class MainWindow {
    
    // Global Property�֌W
    private HashMap<String, Color> eventColorTable;
    
    // Window �� Menu �T�|�[�g
    private WindowSupport windowSupport;
    
    // Mediator
    private Mediator mediator;
    
    // ��Ԑ���
    private StateManager stateMgr;
    
    // Plugin �R���e�i�Ƃ��Ďg�p�������
    // �v���O�|�C���g��
    private final String MY_PLUG_POINT = "mainWindow/comp";
    
    // �A�N�e�B�u�ɂȂ��Ă���v���O�C�����Ǘ�����e�[�u��
    private Hashtable<String, IMainWindowPlugin> activeChildren;
    
    // TabbedPane �Ɋi�[���� plugin
    private ArrayList<PluginReference> children;
    
    // TabbedPane�ŃA�N�e�B�u�ɂȂ��Ă���v���O�C��
    private Hashtable<String, IMainWindowPlugin> activeTabbedChildren;
    
    // plugin���i�[���� tabbedPane
    private JTabbedPane tabbedPane;
    
    // timerTask �֘A
    private javax.swing.Timer taskTimer;
    
    // �v���O�C���� lookup ���邽�߂̃R���e�L�X�g
    private IPluginContext pluginCtx;
    
    // ���K�[
    private Logger bootLogger;
    
    // �v�����^�[�Z�b�g�A�b�v��MainWindow�݂̂ōs���A�ݒ肳�ꂽ PageFormat�e�v���O�C�����g�p����
    private PageFormat pageFormat;
    
    // ���ݒ�p�� Properties
    private Properties saveEnv;
    
    // ��t��M�T�[�o
    private PVTClientServer pvtServer;
    
    /** BlockGlass */
    private BlockGlass blockGlass;
    
    /** StampBox */
    private StampBoxPlugin stampBox;
    
    
    /**
     * Creates new MainWindow
     */
    public MainWindow() {
        
        // �v���O�C���R���e�L�X�g���擾����
        pluginCtx = ClientContext.getPluginContext();
        
        // ���K�[���擾����
        bootLogger = ClientContext.getLogger("boot");
        
        // �v���W�F�N�g�X�^�u�𐶐�����
        Project.setProjectStub(new ProjectStub());
        
        // Mac Application Menu
        com.apple.eawt.Application fApplication = com.apple.eawt.Application.getApplication();
        fApplication.setEnabledPreferencesMenu(true);
        fApplication.addApplicationListener(
                new com.apple.eawt.ApplicationAdapter() {
            public void handleAbout(com.apple.eawt.ApplicationEvent e) {
                showAbout();
                e.setHandled(true);
            }
            public void handleOpenApplication(
                com.apple.eawt.ApplicationEvent e) {
            }
            public void handleOpenFile(com.apple.eawt.ApplicationEvent e) {
            }
            public void handlePreferences(
                    com.apple.eawt.ApplicationEvent e) {
                doPreference();
            }
            public void handlePrintFile(
                com.apple.eawt.ApplicationEvent e) {
            }
            public void handleQuit(com.apple.eawt.ApplicationEvent e) {
                processWindowClosing();
            }
        });
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
    
    public HashMap<String, Color> getEventColorTable() {
        return eventColorTable;
    }
    
    public void setEventColorTable(HashMap<String, Color> table) {
        eventColorTable = table;
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
     * �v���O�������J�n����B
     */
    public void start() {
        // ���O�C���_�C�A���O��\�����F�؂��s��
        LoginDialog login = new LoginDialog();
        login.addPropertyChangeListener("LOGIN_PROP", ProxyPropertyChangeListener.create(this, "loginResult"));
        login.start();
    }
    
    /**
     * �F�،��ʂ̒ʒm���󂯁A�����̑��s�����߂�B
     */
    public void loginResult(Object newValue) {
            
        LoginDialog.LoginStatus result = (LoginDialog.LoginStatus) newValue;
        
        switch (result) {
            case AUTHENTICATED:
                startApp();
                break;
            case NOT_AUTHENTICATED:
                System.exit(1);
                break;
            case CANCELD:
                System.exit(1);
                break;
        }
    }
    
    /**
     * �A�v���P�[�V�������J�n����B
     * �o�b�N�O���E���h�^�X�N��EDT�^�X�N�̐ڑ����s���B
     */
    private void startApp() {
        
        ReflectMonitor rm = new ReflectMonitor();
        rm.setReflection(this, 
                         "startServices", 
                         (Class[]) null, 
                         (Object[]) null);
        rm.setMonitor(null, "OpenDolphin", "���������Ă��܂�...  ", 200, 60*1000);
        
        // ReflectMonitor �̌���State property �̑������X�i�𐶐�����
        PropertyChangeListener pl = new PropertyChangeListener() {
           
            public void propertyChange(PropertyChangeEvent e) {
                
                int state = ((Integer) e.getNewValue()).intValue();
                
                switch (state) {
                    
                    case ReflectMonitor.DONE:
                        // EDT ����R�[�������
                        start2(true);
                        break;
                        
                    case ReflectMonitor.TIME_OVER:
                        System.exit(1);
                        break;
                        
                    case ReflectMonitor.CANCELED:
                        System.exit(1);
                        break;
                }
            }
        };
        rm.addPropertyChangeListener(pl);
        
        rm.start();
    }
    
    /**
     * �N�����̃o�b�N�O���E���h�Ŏ��s�����ׂ��^�X�N���s���B
     */
    public void startServices() {
        
        try {
            // MainWindow �� TabbedPane �Ɋi�[���� plugin ��lookup����
            Collection<PluginReference> c = pluginCtx.listPluginReferences(MY_PLUG_POINT);
            children = new ArrayList<PluginReference>(c);
            
            // �v���O�C���Ǘ��p�̃}�b�v�𐶐�����
            activeTabbedChildren = new Hashtable<String, IMainWindowPlugin>();
            activeChildren = new Hashtable<String, IMainWindowPlugin>();
            
            // StampBox ���N����StampTree��ǂݍ��ނ܂ōs��
            // GUI ���\�z����start()��EDT����R�[������B
            stampBox = (StampBoxPlugin) pluginCtx.lookup(GUIConst.JNDI_STAMP_BOX);
            stampBox.setContext(this);
            stampBox.loadStampTrees();
            
            // ���ݒ�_�C�A���O�ŕύX�����ꍇ������̂ŕۑ�����
            saveEnv = new Properties();
            
            // PVT Sever ���N������
            if (Project.getUseAsPVTServer()) {
                pvtServer = new PVTClientServer();
                pvtServer.startService();
                saveEnv.put(GUIConst.KEY_PVT_SERVER, GUIConst.SERVICE_RUNNING);

            } else {
                saveEnv.put(GUIConst.KEY_PVT_SERVER, GUIConst.SERVICE_NOT_RUNNING);
            }

            // CLAIM���M�𐶐�����
            if (Project.getSendClaim()) {
                createPlugin(GUIConst.JNDI_SEND_CLAIM);
                saveEnv.put(GUIConst.KEY_SEND_CLAIM, GUIConst.SERVICE_RUNNING);

            } else {
                saveEnv.put(GUIConst.KEY_SEND_CLAIM, GUIConst.SERVICE_NOT_RUNNING);
            }
            if (Project.getClaimAddress() != null) {
                saveEnv.put(GUIConst.ADDRESS_CLAIM, Project.getClaimAddress());
            }

            // MML���M�𐶐�����
            if (Project.getSendMML()) {
                createPlugin(GUIConst.JNDI_SEND_MML);
                saveEnv.put(GUIConst.KEY_SEND_MML, GUIConst.SERVICE_RUNNING);

            } else {
                saveEnv.put(GUIConst.KEY_SEND_MML, GUIConst.SERVICE_NOT_RUNNING);
            }
            if (Project.getCSGWPath() != null) {
                saveEnv.put(GUIConst.CSGW_PATH, Project.getCSGWPath());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            bootLogger.fatal(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * �F�ؐ�����ɃX�^�[�g�̎��̃t�F�[�Y�����s����B
     * @param loginState
     */
    private void start2(Boolean login) {
        
        // �C�x���g�J���[���`����
        HashMap<String, Color> cTable = new HashMap<String, Color>(10, 0.75f);
        cTable.put("TODAY", ClientContext.getColor("color.TODAY_BACK"));
        cTable.put("BIRTHDAY", ClientContext.getColor("color.BIRTHDAY_BACK"));
        cTable.put("PVT", ClientContext.getColor("color.PVT"));
        cTable.put("DOC_HISTORY", ClientContext.getColor("color.PVT"));
        setEventColorTable(cTable);
        
        // StateMgr �𐶐�����
        stateMgr = new StateManager();
        
        // �ݒ�ɕK�v�Ȓ萔�����R���e�L�X�g����擾����
        String windowTitle = ClientContext.getString("mainWindow.title");
        int defaultX = ClientContext.getInt("mainWindow.defaultX");
        int defaultY = ClientContext.getInt("mainWindow.defaultY");
        int defaultWidth = ClientContext.getInt("mainWindow.defaultWidth");
        int defaultHeight = ClientContext.getInt("mainWindow.defaultHeight");
        
        // WindowSupport �𐶐����� ���̎��_�� Frame,WindowMenu ������MenuBar ����������Ă���
        String title = ClientContext.getFrameTitle(windowTitle);
        windowSupport = WindowSupport.create(title);
        JFrame myFrame = windowSupport.getFrame();		// MainWindow �� JFrame
        JMenuBar myMenuBar = windowSupport.getMenuBar();	// MainWindow �� JMenuBar
        
        // Window�ɂ��̃N���X�ŗL�̐ݒ������
        Point loc = new Point(defaultX, defaultY);
        Dimension size = new Dimension(defaultWidth, defaultHeight);
        myFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                processWindowClosing();
            }
        });
        ComponentMemory cm = new ComponentMemory(myFrame, loc, size, this);
        cm.setToPreferenceBounds();
        // BlockGlass ��ݒ肷��
        blockGlass = new BlockGlass();
        myFrame.setGlassPane(blockGlass);
        
        // mainWindow�̃��j���[�𐶐������j���[�o�[�ɒǉ�����
        mediator = new Mediator(this);
        createMenuBar(myMenuBar, mediator);
        
        // mainWindow�̃R���e���gGUI�𐶐���Frame�ɒǉ�����
        tabbedPane = new JTabbedPane();
        JPanel content = new JPanel(new BorderLayout());
        content.add(tabbedPane, BorderLayout.CENTER);
        content.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        myFrame.getContentPane().add(content);
        
        // Plugin�𐶐�����
        try {
            // PluginSpec �� tab �Ɋi�[����
            for (PluginReference plRef : children) {
                tabbedPane.addTab((String) plRef.getAddrContent(PluginReference.TITLE), null);
            }
            
            // Tab �̐؂�ւ��� plugin �� Factory �Ő��������悤�ɂ���
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
            tabbedPane.addChangeListener(ProxyChangeListener.create(this, "tabSelectionChanged"));
            tabbedPane.setSelectedIndex(0);
            
            // StaeMagr���g�p���ă��C���E�C���h�E�̏�Ԃ𐧌䂷��
            stateMgr.processLogin(login);
            
            // ��������
            stampBox.start();
            stampBox.getFrame().setVisible(true);
            windowSupport.getFrame().setVisible(true);
            
            // ��t���X�g���J�n����
            WatingListPlugin watingList = (WatingListPlugin) getPlugin(GUIConst.JNDI_WATING_LIST);
            watingList.restartCheckTimer();
            
        } catch (Exception e) {
            bootLogger.fatal(e.toString());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * �v���O�C���̒x���������^�u�̐؂�ւ��ŏ�������B
     */
    public void tabSelectionChanged() {
            
        try {
            int index = tabbedPane.getSelectedIndex();
            String key = String.valueOf(index);
            IMainWindowPlugin plugin = activeTabbedChildren.get(key);

            // �܂� plugin ����������Ă��Ȃ��ꍇ
            if (plugin == null) {
                PluginReference plRef = children.get(index);
                plugin = (IMainWindowPlugin) pluginCtx.lookup(plRef.getJndiName());
                plugin.setContext(MainWindow.this);
                plugin.start();
                tabbedPane.setComponentAt(index, plugin.getUI());

                // ���� plugin �� key �𓾁Achildren �Ƀo�C���h����
                activeTabbedChildren.put(key, plugin);
                bootLogger.info("Plugin �𐶐����܂���: " + key);
            }
            // ���ɐ�������Ă���ꍇ
            else {
                plugin.enter();
            }

            // chain �ɉ�����
            mediator.addChain(plugin);

        } catch (Exception ex) {
            ex.printStackTrace();
            bootLogger.warn(ex.getMessage());
        }
    }

    
    // ////////////// �v���O�C���Ǘ����\�b�h ///////////////////////////
    
    public Collection listChildren() {
        try {
            return pluginCtx.listPluginReferences(MY_PLUG_POINT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Collection listChildrenNames() {
        try {
            return pluginCtx.listPluginNames(MY_PLUG_POINT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * �v���O�C���𐶐����N������B
     * @param jndiName ��������v���O�C���� JNDI �l�[��
     */
    public void createPlugin(String jndiName) {
        try {
            IMainWindowPlugin plugin = (IMainWindowPlugin) pluginCtx.lookup(jndiName);
            plugin.setContext(this);
            plugin.start();
        } catch (Exception e) {
            e.printStackTrace();
            bootLogger.warn(e.getMessage());
        }
    }
    
    public IMainWindowPlugin getPlugin(String jndiName) {
        return activeChildren.get(jndiName);
    }
    
    public void pluginStarted(IMainWindowPlugin plugin) {
        if (plugin != null) {
            String name = plugin.getJNDIname();
            bootLogger.info(name + " started");
            activeChildren.put(name, plugin);
        }
    }
    
    public void pluginStopped(IMainWindowPlugin plugin) {
        if (plugin != null) {
            String name = plugin.getJNDIname();
            bootLogger.info(name + " stopped");
            activeChildren.remove(plugin.getJNDIname());
            plugin = null;
        }
    }
    
    //////////////////////////////////////////////////////////
    
    /**
     * �J���e���I�[�v������B
     * @param pvt ���җ��@���
     */
    public void openKarte(PatientVisitModel pvt) {
        
        try {
            IChart chart = (IChart) pluginCtx.lookup(GUIConst.JNDI_CHART);
            chart.setContext(MainWindow.this);
            chart.setPatientVisit(pvt);                 //
            chart.setReadOnly(Project.isReadOnly());    // RedaOnlyProp
            chart.start();
            
        } catch (Exception e) {
            e.printStackTrace();
            bootLogger.warn(e.getMessage());
        }
    }
    
    /**
     * MainWindow �̃A�N�V������Ԃ��B
     * @param name Action��
     * @return Action
     */
    public Action getAction(String name) {
        return mediator.getAction(name);
    }
    
    // //////////////// ���j���[�o�[�T�|�[�g�̎��� /////////////////
    
    /**
     * �A�v���P�[�V�����̃��j���[�o�[�y�� ToolPanel �𐶐�����B �A�v���P�[�V�������j���[�o�[ = MainWindowMB +
     * Charet/EditoFrame MB �iWindows ���猩��� ��� Window�� ���j���[����ɍ�������Ă���j
     * �v�X�̃��j���[����(Action)�̃^�[�Q�b�g�͈قȂ�B
     */
    public Object[] createMenuBar(JMenuBar menuBar, MenuSupport requester) {
        
        try {
            boolean mac = ClientContext.isMac();
            
            Hashtable<String, Action> mainActions = null;
            Hashtable<String, Action> editorActions = null;
            
            
            // Action �͋��ʁBMenuBar�̓E�C���h�E���ɐ�������B
            // i.e. ���C���E�C���h�E�A�`���[�g�E�C���h�E�ɂ����̃A�N�V�����������j���[�o�[�𐶐�����B
            if (mainActions == null) {
                
                mainActions = new Hashtable<String, Action>();
                
                ReflectAction action = new ReflectAction("�y�[�W�ݒ�...", this, GUIConst.PRINTER_SETUP);
                mainActions.put(GUIConst.PRINTER_SETUP, action);
                
                // Mac �̏ꍇ�A�I���ƃA�o�E�g�̓A�v���P�[�V�������j���[
                if (!mac) {
                    action = new ReflectAction("�A�o�E�g...", this, GUIConst.SHOW_ABOUT);
                    mainActions.put(GUIConst.SHOW_ABOUT, action);
                    
                    action = new ReflectAction("�I��", this, GUIConst.EXIT);
                    mainActions.put(GUIConst.EXIT, action);
                }
                
                action = new ReflectAction("�p�X���[�h�ύX...", this, GUIConst.CHANGE_PASSWORD);
                mainActions.put(GUIConst.CHANGE_PASSWORD, action);
                
                action = new ReflectAction("���[�U�o�^...", this, GUIConst.ADD_USER);
                mainActions.put(GUIConst.ADD_USER, action);
                
                action = new ReflectAction("�A�b�v�f�[�g�m�F...", this, GUIConst.UPDATE_SOFTWARE);
                mainActions.put(GUIConst.UPDATE_SOFTWARE, action);
                
                action = new ReflectAction("�h���t�B���T�|�[�g", this, GUIConst.BROWSE_DOLPHIN_SUPPORT);
                mainActions.put(GUIConst.BROWSE_DOLPHIN_SUPPORT, action);
                
                action = new ReflectAction("�h���t�B���v���W�F�N�g", this, GUIConst.BROWSE_DOLPHIN_PROJECT);
                mainActions.put(GUIConst.BROWSE_DOLPHIN_PROJECT, action);
                
                action = new ReflectAction("MedXML�R���\�[�V�A��", this, GUIConst.BROWSE_MEDXML);
                mainActions.put(GUIConst.BROWSE_MEDXML, action);
                
                action = new ReflectAction("���ݒ�", this, GUIConst.SET_KARTE_ENV);
                mainActions.put(GUIConst.SET_KARTE_ENV, action);
                
                action = new ReflectAction("�X�^���v�{�b�N�X", this, GUIConst.SHOW_STAMP_BOX);
                mainActions.put(GUIConst.SHOW_STAMP_BOX, action);
                
                action = new ReflectAction("�V�F�[�}�{�b�N�X", this, GUIConst.SHOW_SCHEMA_BOX);
                mainActions.put(GUIConst.SHOW_SCHEMA_BOX, action);
            }
            
            if (editorActions == null) {
                
                editorActions = new Hashtable<String, Action>();
                
                // �T�C�Y�T�u���j���[�A�N�V�����𐶐�����
                SubMenuAction subActtion = new SubMenuAction("�T�C�Y");
                editorActions.put(GUIConst.ACTION_SIZE, subActtion);
                
                // �X�^�C���T�u���j���[�A�N�V�����𐶐�����
                subActtion = new SubMenuAction("�X�^�C��");
                editorActions.put(GUIConst.ACTION_STYLE, subActtion);
                
                // �s�����T�u���j���[�A�N�V�����𐶐�����
                subActtion = new SubMenuAction("�s����");
                editorActions.put(GUIConst.ACTION_ALIGNMENT, subActtion);
                
                // �J���[�T�u���j���[�A�N�V�����𐶐�����
                subActtion = new SubMenuAction("�J���[");
                editorActions.put(GUIConst.ACTION_COLOR, subActtion);
                
                if (requester instanceof ChartMediator) {
                
                    // Red
                    Action action = new StyledEditorKit.ForegroundAction("���b�h", ClientContext.getColor("color.set.default.red"));
                    editorActions.put(GUIConst.ACTION_RED, action);

                    // OR
                    action = new StyledEditorKit.ForegroundAction("�I�����W", ClientContext.getColor("color.set.default.orange"));
                    editorActions.put(GUIConst.ACTION_ORANGE, action);

                    // Y
                    action = new StyledEditorKit.ForegroundAction("�C�F���[", ClientContext.getColor("color.set.default.yellow"));
                    editorActions.put(GUIConst.ACTION_YELLOW, action);

                    // Green
                    action = new StyledEditorKit.ForegroundAction("�O���[��", ClientContext.getColor("color.set.default.green"));
                    editorActions.put(GUIConst.ACTION_GREEN, action);

                    // Blue
                    action = new StyledEditorKit.ForegroundAction("�u���[", ClientContext.getColor("color.set.default.blue"));
                    editorActions.put(GUIConst.ACTION_BLUE, action);

                    // Purpule
                    action = new StyledEditorKit.ForegroundAction("�p�[�v��", ClientContext.getColor("color.set.default.purpule"));
                    editorActions.put(GUIConst.ACTION_PURPLE, action);

                    // Gray
                    action = new StyledEditorKit.ForegroundAction("�O���[", ClientContext.getColor("color.set.default.gray"));
                    editorActions.put(GUIConst.ACTION_GRAY, action);

                    // 9
                    action = new StyledEditorKit.FontSizeAction("9", 9);
                    editorActions.put(GUIConst.ACTION_S9, action);

                    // 10
                    action = new StyledEditorKit.FontSizeAction("10", 10);
                    editorActions.put(GUIConst.ACTION_S10, action);

                    // 12
                    action = new StyledEditorKit.FontSizeAction("12", 12);
                    editorActions.put(GUIConst.ACTION_S12, action);

                    // 14
                    action = new StyledEditorKit.FontSizeAction("14", 14);
                    editorActions.put(GUIConst.ACTION_S14, action);

                    // 18
                    action = new StyledEditorKit.FontSizeAction("18", 18);
                    editorActions.put(GUIConst.ACTION_S18, action);

                    // 24
                    action = new StyledEditorKit.FontSizeAction("24", 24);
                    editorActions.put(GUIConst.ACTION_S24, action);

                    // 36
                    action = new StyledEditorKit.FontSizeAction("36", 36);
                    editorActions.put(GUIConst.ACTION_S36, action);

                    // Bold
                    action = new StyledEditorKit.BoldAction();
                    editorActions.put(GUIConst.ACTION_BOLD, action);

                    // Italic
                    action = new StyledEditorKit.ItalicAction();
                    editorActions.put(GUIConst.ACTION_ITALIC, action);

                    // Underline
                    action = new StyledEditorKit.UnderlineAction();
                    editorActions.put(GUIConst.ACTION_UNDERLINE, action);

                    // Left
                    action = new StyledEditorKit.AlignmentAction("������", StyleConstants.ALIGN_LEFT);
                    editorActions.put(GUIConst.ACTION_LEFT_ALIGN, action);

                    // Center
                    action = new StyledEditorKit.AlignmentAction("��������", StyleConstants.ALIGN_CENTER);
                    editorActions.put(GUIConst.ACTION_CENTER_ALIGN, action);

                    // Right
                    action = new StyledEditorKit.AlignmentAction("�E����", StyleConstants.ALIGN_RIGHT);
                    editorActions.put(GUIConst.ACTION_RIGHT_ALIGN, action);
                }
            }
            
            // Local Action �p�̃A�N�V�����e�[�u���𐶐�����
            Hashtable<String, Action> actions = new Hashtable<String, Action>();
            
            // Global Action��ǉ�����
            Enumeration<String> enums = mainActions.keys();
            while (enums.hasMoreElements()) {
                String key = enums.nextElement();
                actions.put(key, mainActions.get(key));
            }
            
            // �G�f�B�^�A�N�V������ǉ�����
            enums = editorActions.keys();
            while (enums.hasMoreElements()) {
                String key = enums.nextElement();
                actions.put(key, editorActions.get(key));
            }
            
            // MenuTarget
            Hashtable<String, Object> menuTargets = new Hashtable<String, Object>(3, 1.0f);
            
            // Chain target ��o�^����
            menuTargets.put("chain", requester);
            
            // ���j���[���X�i�[��ݒ肷��
            // ex. display_listener �� display Menu �ɂ� MenuListener �����邱�Ƃ��w������
            // ChartMediator �̏ꍇ �}�����j���[�ƃe�L�X�g���j���[�� menuListener ��ݒ肷��
            if (requester instanceof ChartMediator) {
                menuTargets.put("insert_listener", requester);
                menuTargets.put("text_listener", requester);
            }
            
            // MenuBarBuilder �𐶐������j���[���\�z����
            MenuBarBuilder builder = (MenuBarBuilder) pluginCtx.lookup(GUIConst.JNDI_MENUBAR_BUILDER);
            builder.setMenuBar(menuBar);
            builder.setActions(actions);
            builder.setTargets(menuTargets);
            builder.build(ClientContext.getMenuBarResource());
            
            // MenuBar������v������Window��mediator ��Action�̓o�^��������
            requester.registerActions(actions);
            
            Object[] ret = new Object[3];
            ret[0] = builder.getJMenuBar();
            ret[1] = builder.getToolPanel();
            builder.close();
            return ret;
            
        } catch (Exception e) {
            e.printStackTrace();
            bootLogger.warn(e.getMessage());
            System.exit(1);
        }
        return null;
    }
    
    // ///////////// MainWindow �����s���郁�j���[�̎��� ///////////////////////
    
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
     * �F�،��ʂ̒ʒm���󂯁A�A�v���P�[�V�����̏�Ԃ𐧌䂷��
     */
    class ConnectListener implements PropertyChangeListener {
        
        public void propertyChange(PropertyChangeEvent e) {
            
            if (e.getPropertyName().equals("LOGIN_PROP")) {
                
                LoginDialog.LoginStatus result = ((LoginDialog.LoginStatus) e
                        .getNewValue());
                switch (result) {
                    case AUTHENTICATED:
                        stateMgr.processLogin(true);
                        break;
                    case NOT_AUTHENTICATED:
                        System.exit(1);
                        break;
                    case CANCELD:
                        break;
                }
            }
        }
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
        //Thread t = new Thread(sd);
        //t.setPriority(Thread.NORM_PRIORITY);
        //t.start();
    }
    
    /**
     * CloseBox�������s���B
     */
    public void processWindowClosing() {
        exit();
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
        //Thread t = new Thread(sd);
        //t.setPriority(Thread.NORM_PRIORITY);
        //t.start();
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
                    boolean start = ( (!oldRunning) && newRun ) ? true : false;
                    boolean stop = ( (oldRunning) && (!newRun) ) ? true : false;
                    
                    if (start) {
                        pvtServer = new PVTClientServer();
                        //pvtServer.setUser(Project.getUserModel());
                        pvtServer.startService();
                        saveEnv.put(GUIConst.KEY_PVT_SERVER, GUIConst.SERVICE_RUNNING);
                        messages.add("��t��M���J�n���܂����B");
                    }
                    else if (stop) {
                        pvtServer.stopService();
                        saveEnv.put(GUIConst.KEY_PVT_SERVER, GUIConst.SERVICE_NOT_RUNNING);
                        messages.add("��t��M���~���܂����B");
                    }
                    
                    // SendClaim
                    oldRunning = saveEnv.getProperty(GUIConst.KEY_SEND_CLAIM).equals(GUIConst.SERVICE_RUNNING) ? true : false;
                    newRun = Project.getSendClaim();
                    start = ( (!oldRunning) && newRun ) ? true : false;
                    stop = ( (oldRunning) && (!newRun) ) ? true : false;
                    
                    boolean restart = false;
                    String oldAddress = saveEnv.getProperty(GUIConst.ADDRESS_CLAIM);
                    String newAddress = Project.getClaimAddress();
                    if (oldAddress != null && newAddress != null && (!oldAddress.equals(newAddress)) && newRun) {
                        restart = true;
                    }
                    
                    if (start) {
                        createPlugin(GUIConst.JNDI_SEND_CLAIM);
                        saveEnv.put(GUIConst.KEY_SEND_CLAIM, GUIConst.SERVICE_RUNNING);
                        saveEnv.put(GUIConst.ADDRESS_CLAIM, newAddress);
                        messages.add("CLAIM���M���J�n���܂����B(���M�A�h���X=" + newAddress + ")");
                        
                    } else if (stop) {
                        SendClaimPlugin sendClaim = (SendClaimPlugin) getPlugin(GUIConst.JNDI_SEND_CLAIM);
                        sendClaim.stop();
                        saveEnv.put(GUIConst.KEY_SEND_CLAIM, GUIConst.SERVICE_NOT_RUNNING);
                        saveEnv.put(GUIConst.ADDRESS_CLAIM, newAddress);
                        messages.add("CLAIM���M���~���܂����B");
                        
                    } else if (restart) {
                        SendClaimPlugin sendClaim = (SendClaimPlugin) getPlugin(GUIConst.JNDI_SEND_CLAIM);
                        sendClaim.stop();
                        createPlugin(GUIConst.JNDI_SEND_CLAIM);
                        saveEnv.put(GUIConst.KEY_SEND_CLAIM, GUIConst.SERVICE_RUNNING);
                        saveEnv.put(GUIConst.ADDRESS_CLAIM, newAddress);
                        messages.add("CLAIM���M�����X�^�[�g���܂����B(���M�A�h���X=" + newAddress + ")");
                    }
                    
                    // SendMML
                    oldRunning = saveEnv.getProperty(GUIConst.KEY_SEND_MML).equals(GUIConst.SERVICE_RUNNING) ? true : false;
                    newRun = Project.getSendMML();
                    start = ( (!oldRunning) && newRun ) ? true : false;
                    stop = ( (oldRunning) && (!newRun) ) ? true : false;
                    
                    restart = false;
                    oldAddress = saveEnv.getProperty(GUIConst.CSGW_PATH);
                    newAddress = Project.getCSGWPath();
                    if (oldAddress != null && newAddress != null && (!oldAddress.equals(newAddress)) && newRun) {
                        restart = true;
                    }
                    
                    if (start) {
                        createPlugin(GUIConst.JNDI_SEND_MML);
                        saveEnv.put(GUIConst.KEY_SEND_MML, GUIConst.SERVICE_RUNNING);
                        saveEnv.put(GUIConst.CSGW_PATH, newAddress);
                        messages.add("MML���M���J�n���܂����B(���M�A�h���X=" + newAddress + ")");
                        
                    } else if (stop) {
                        SendMmlPlugin sendMml = (SendMmlPlugin) getPlugin(GUIConst.JNDI_SEND_MML);
                        sendMml.stop();
                        saveEnv.put(GUIConst.KEY_SEND_MML, GUIConst.SERVICE_NOT_RUNNING);
                        saveEnv.put(GUIConst.CSGW_PATH, newAddress);
                        messages.add("MML���M���~���܂����B");
                        
                    } else if (restart) {
                        SendMmlPlugin sendMml = (SendMmlPlugin) getPlugin(GUIConst.JNDI_SEND_MML);
                        sendMml.stop();
                        createPlugin(GUIConst.JNDI_SEND_MML);
                        saveEnv.put(GUIConst.KEY_SEND_MML, GUIConst.SERVICE_RUNNING);
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
                                JOptionPane.INFORMATION_MESSAGE
                                );
                    }
                }
            }
        }
    }
    
//    /**
//     * �I���������s���B
//     */
//    public void exit() {
//        
//        // ���ۑ��̃J���e������ꍇ�͌x�������^�[������
//        // �J���e��ۑ��܂��͔j�����Ă���ēx���s����
//        boolean dirty = false;
//        
//        // Chart �𒲂ׂ�
//        ArrayList<ChartPlugin> allChart = ChartPlugin.getAllChart();
//        if (allChart != null && allChart.size() > 0) {
//            for (ChartPlugin chart : allChart) {
//                if (chart.isDirty()) {
//                    dirty = true;
//                    break;
//                }
//            }
//        }
//        
//        // �ۑ����ĂȂ����̂�����΃��^�[������
//        if (dirty) {
//            alertDirty();
//            return;
//        }
//        
//        // EditorFrame�̃`�F�b�N���s��
//        java.util.List<IChart> allEditorFrames = EditorFrame.getAllEditorFrames();
//        if (allEditorFrames != null && allEditorFrames.size() > 0) {
//            for(IChart chart : allEditorFrames) {
//                if (chart.isDirty()) {
//                    dirty = true;
//                    break;
//                }
//            }
//        }
//        
//        if (dirty) {
//            alertDirty();
//            return;
//        }
//        
//        
//        //
//        // StoppingTask ���W�߂�
//        //
//        Vector<ILongTask> stoppingTasks = new Vector<ILongTask>();
//        ILongTask task = null;
//        
//        try {
//            Hashtable cloneMap = null;
//            synchronized (activeChildren) {
//                cloneMap = (Hashtable) activeChildren.clone();
//            }
//            Iterator iter = cloneMap.values().iterator();
//            while (iter != null && iter.hasNext()) {
//                IMainWindowPlugin pl = (IMainWindowPlugin) iter.next();
//                task = pl.getStoppingTask();
//                if (task != null) {
//                    stoppingTasks.add(task);
//                }
//            }
//            
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            bootLogger.warn(ex.toString());
//        }
//        
//        // StoppingTask ����̃^�C�}�y�� Progress Monitor �Ŏ��s����
//        // �S�Ẵ^�X�N���I��������A�v���P�[�V�����̏I�������Ɉڂ�
//        int cnt = stoppingTasks.size();
//        if (cnt == 0) {
//            doExit();
//            return; // Never come back
//        } else {
//            bootLogger.info(cnt + " �� StoppingTask ������܂�");
//        }
//        
//        // �ꊇ���Ď��s���邽�߂�TaskManager�𐶐�����
//        ILongTask[] longs = new AbstractLongTask[cnt];
//        for (int i = 0; i < cnt; i++) {
//            longs[i] = stoppingTasks.get(i);
//        }
//        final TaskManager taskMgr = new TaskManager(longs);
//        
//        // Progress Monitor �𐶐�����
//        String exittingNote = ClientContext.getString("mainWindow.progressNote.exitting");
//        final ProgressMonitor monitor = new ProgressMonitor(null, null, exittingNote, 0, taskMgr.getLength());
//        
//        // ���s Timer �𐶐�����
//        taskTimer = new javax.swing.Timer(taskMgr.getDelay(),
//                new ActionListener() {
//            
//            public void actionPerformed(ActionEvent e) {
//                
//                if (taskMgr.isDone()) {
//                    
//                    // �I���������s��
//                    taskTimer.stop();
//                    monitor.close();
//                    
//                    // ���s���ʂ𓾂�
//                    if (!taskMgr.getResult()) {
//                        
//                        bootLogger.warn("StoppingTask �ɃG���[������܂�");
//                        
//                        // �G���[������ꍇ�̓_�C�A���O��\�����A�I�v�V������I��������
//                        int option = doStoppingAlert(taskMgr.getCurTask());
//                        bootLogger.info("�I�����ꂽ�I�v�V���� = " + option);
//                        if (option == 1) {
//                            // �I�������I�񂾏ꍇ
//                            bootLogger.info("�I���I�v�V�������I�΂�܂���");
//                            doExit();
//                        } else {
//                            bootLogger.info("�L�����Z���I�v�V�������I�΂�܂���");
//                        }
//                        
//                    } else {
//                        // �G���[�Ȃ�
//                        bootLogger.info("StoppingTask ���I�����܂���");
//                        doExit();
//                    }
//                    
//                } else {
//                    // ���ݒl���X�V����
//                    monitor.setProgress(taskMgr.getCurrent());
//                }
//            }
//        });
//        taskMgr.start();
//        taskTimer.start();
//    }
//    
//    /**
//     * ���ۑ��̃h�L�������g������ꍇ�̌x����\������B
//     */
//    private void alertDirty() {
//        String msg0 = "���ۑ��̃h�L�������g������܂��B";
//        String msg1 = "�ۑ��܂��͔j��������ɍēx���s���Ă��������B";
//        String taskTitle = ClientContext.getString("mainWindow.exit.taskTitle");
//        JOptionPane.showMessageDialog(
//                        (Component) null,
//                        new Object[]{msg0, msg1},
//                        ClientContext.getFrameTitle(taskTitle),
//                        JOptionPane.INFORMATION_MESSAGE
//                        );
//    }
    
    
    public void exit() {
        
        final AppEnvSaver saver = new AppEnvSaver();
        
        saver.addPropertyChangeListener(new PropertyChangeListener() {
            
            public void propertyChange(PropertyChangeEvent e) {
                
                int state = ((Integer) e.getNewValue()).intValue();
                
                switch (state) {
                    case AppEnvSaver.NO_SAVE_CONDITION:
                        break;
                       
                    case AppEnvSaver.SAVE_ERROR:
                        int option = doStoppingAlert(saver.getErrorTask());
                        if (option == 1) {
                            // �I�������I�񂾏ꍇ
                            bootLogger.info("�I���I�v�V�������I�΂�܂���");
                            doExit();
                        } else {
                            bootLogger.info("�L�����Z���I�v�V�������I�΂�܂���");
                        }
                        break;
                        
                    case AppEnvSaver.SAVE_DONE:
                        doExit();
                        break;
                }
            }
        });
        
        saver.save(activeChildren);
    }
    
    /**
     * �I���������ɃG���[���������ꍇ�̌x�����_�C�A���O��\������B
     * @param errorTask �G���[���������^�X�N
     * @return ���[�U�̑I��l
     */
    private int doStoppingAlert(ILongTask errorTask) {
        
        // �A�v���P�[�V�����̊��ۑ����ɃG���[�������܂����B
        String msg1 = ClientContext.getString("mainWindow.exit.errorMsg1");
        // ���̂܂܏I�����܂���?
        String msg2 = ClientContext.getString("mainWindow.exit.errorMsg2");
        // �I������
        String exitOption = ClientContext.getString("mainWindow.exit.exitOption");
        // �L�����Z������
        String cancelOption = ClientContext.getString("mainWindow.exit.cancelOption");
        // ���ۑ�
        String taskTitle = ClientContext.getString("mainWindow.exit.taskTitle");
        
        StringBuilder buf = new StringBuilder();
        buf.append(msg1);
        buf.append("\n");
        buf.append(errorTask.getMessage());
        buf.append("\n");
        buf.append(msg2);
        String msg = buf.toString();
        
        String title = ClientContext.getFrameTitle(taskTitle);
        
        String[] options = new String[] {cancelOption, exitOption};
        
        int option = JOptionPane.showOptionDialog(
                null, msg, title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);
        return option;
    }
    
    /**
     * �ŏI�I�ɏI������B
     */
    private void doExit() {
        
        try {
            Hashtable cloneMap = null;
            synchronized (activeChildren) {
                cloneMap = (Hashtable) activeChildren.clone();
            }
            Iterator iter = cloneMap.values().iterator();
            while (iter != null && iter.hasNext()) {
                IMainWindowPlugin pl = (IMainWindowPlugin) iter.next();
                pl.stop();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            bootLogger.warn(e.toString());
        }
        JFrame myFrame = windowSupport.getFrame();
        myFrame.setVisible(false);
        myFrame.dispose();
        bootLogger.info("�A�v���P�[�V�������I�����܂�");
        System.exit(1);
    }
    
    /**
     * ���[�U�̃p�X���[�h��ύX����B
     *
     */
    public void changePassword() {
        IMainWindowPlugin plugin = (IMainWindowPlugin) getPlugin(GUIConst.JNDI_CHANGE_PASSWORD);
        if (plugin == null) {
            createPlugin(GUIConst.JNDI_CHANGE_PASSWORD);
        } else {
            plugin.toFront();
        }
    }
    
    /**
     * ���[�U�o�^���s���B�Ǘ��҃��j���[�B
     *
     */
    public void addUser() {
        IMainWindowPlugin plugin = (IMainWindowPlugin) getPlugin(GUIConst.JNDI_ADD_USER);
        if (plugin == null) {
            createPlugin(GUIConst.JNDI_ADD_USER);
        } else {
            plugin.toFront();
        }
    }
    
    /**
     * OpenDolphin Clinet ���X�V����B
     */
    public void update1() {
        
        Logger logger = ClientContext.getLogger("boot");
        logger.info("�\�t�g�E�F�A�X�V���I������܂���");
        
        final DolphinUpdater updater = new DolphinUpdater();
        
        // Proxy ��ݒ肷��
        String proxyHost = Project.getProxyHost();
        String proxyPort = String.valueOf(Project.getProxyPort());
        if ((proxyHost != null) && (proxyPort != null)) {
            updater.setProxyHost(proxyHost);
            updater.setProxyPort(proxyPort);
        }
        
        // Remote URL�̍ŏI�X�V�����`�F�b�N����
        ArrayList<String> urls = new ArrayList<String>();
        String tmp = ClientContext.getUpdateURL();
        logger.info("Remote resource = " + tmp);
        urls.add(tmp);
        
        ArrayList<String> localLastModified = new ArrayList<String>();
        tmp = String.valueOf(Project.getLastModify());
        localLastModified.add(tmp);
        
        ArrayList<String> remoteLastModified = updater.getLastModified(urls);
        
        // ���ʂ�\������
        String msg = null;
        String title = ClientContext.getString("updater.dialog.title");
        
        // �X�V�����擾�ł��Ȃ������ꍇ
        if (!updater.getResult()) {
            logger.info(msg);
            msg = ClientContext.getString("updater.msg.noConnection");
            JOptionPane.showMessageDialog(null, msg, title, JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // �X�V�����擾�ł����ꍇ
        final ArrayList<String> updateUrl = new ArrayList<String>();
        final ArrayList<String> updateLastModied = new ArrayList<String>();
        String remote = null;
        String local = null;
        for (int i = 0; i < localLastModified.size(); i++) {
            
            remote = remoteLastModified.get(i);
            local = localLastModified.get(i);
            logger.info((String) urls.get(i) + ": cachedLM=" + local
                    + " remoteLM=" + remote);
            
            // remote = 0L Jar file �Ȃ� = �X�V�Ȃ�
            if ((!remote.equals("0")) && (!remote.equals(local))) {
                updateUrl.add(urls.get(i));
                updateLastModied.add(remote);
            }
        }
        
        // �X�V�\�Ȃ��̂��Ȃ��ꍇ
        if (updateUrl == null || updateUrl.size() == 0) {
            msg = ClientContext.getString("updater.msg.noUpdate");
            logger.info(msg);
            JOptionPane.showMessageDialog(null, msg, title,
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        //
        // �X�V�\�Ȃ��̂�����ꍇ
        //
        // mnimonic ��\�������Ȃ�
        Object[] cstOptions = new Object[]{"�͂�", "������"};
        
        msg = ClientContext.getString("updater.msg.available");
        logger.info(msg);
        int select = JOptionPane.showOptionDialog(
                null,
                msg,
                ClientContext.getFrameTitle(title),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                ClientContext.getImageIcon("favs_32.gif"),
                cstOptions,
                "�͂�");
        
        // YES ��I�������ꍇ
        if (select == 0) {
            
            //
            // �_�E�����[�h����O�Ɋ��̕ۑ����s��
            //
            final AppEnvSaver saver = new AppEnvSaver();
        
            saver.addPropertyChangeListener(new PropertyChangeListener() {
            
                public void propertyChange(PropertyChangeEvent e) {

                    int state = ((Integer) e.getNewValue()).intValue();

                    switch (state) {
                        case AppEnvSaver.NO_SAVE_CONDITION:
                            break;

                        case AppEnvSaver.SAVE_ERROR:
                            int option = doStoppingAlert(saver.getErrorTask());
                            if (option == 1) {
                                // �I�������I�񂾏ꍇ
                                bootLogger.info("�I���I�v�V�������I�΂�܂���");
                                update3(updater, updateUrl, updateLastModied);
                            } else {
                                bootLogger.info("�L�����Z���I�v�V�������I�΂�܂���");
                            }
                            break;

                        case AppEnvSaver.SAVE_DONE:
                            update3(updater, updateUrl, updateLastModied);
                            break;
                    }
                }
            });
        
            saver.save(activeChildren);
        }
    }
    
    /**
     * �X�V�������s���B
     * @param updater DolphinUpdater
     * @param updateUrl
     * @param updateLastModied
     */
    private void update3(final DolphinUpdater updater, final ArrayList<String> updateUrl, final ArrayList<String> updateLastModied) {
        
        // �X�V�t�@�C���̃o�C�g���𓾂�
        final Logger logger = ClientContext.getLogger("boot");
        updater.getContentLength(updateUrl);
        final int totalLength = updater.getTotalLength();
        logger.info("�_�E�����[�h�̍��v�o�C�g�� = " + totalLength);
        
        // ���j�^�y�у^�C�}�[�̐ݒ�萔�𓾂�
        int delay = ClientContext.getInt("task.default.delay");
        int decideToPopup = ClientContext.getInt("task.default.decideToPopup");
        int milisToPopup = ClientContext.getInt("task.default.milisToPopup");
        String updateMsg = ClientContext.getString("updater.msg.downloading");
        
        final ProgressMonitor monitor = new ProgressMonitor(null, null, updateMsg, 0, totalLength);
        
        taskTimer = new javax.swing.Timer(delay, new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                if (monitor.isCanceled()) {
                    updater.cancel();
                    taskTimer.stop();
                    monitor.close();
                    logger.info("�_�E�����[�h���L�����Z������܂���");
                    return;
                }
                
                if (!updater.done()) {
                    int current = updater.getCurrent();
                    String msg = updater.getMessage();
                    int done = (int) (((float) current / (float) totalLength) * 100);
                    StringBuilder buf = new StringBuilder();
                    buf.append(msg);
                    buf.append(" [");
                    buf.append(totalLength);
                    buf.append("�o�C�g�� ");
                    buf.append(done);
                    buf.append("% ����]");
                    monitor.setNote(buf.toString());
                    monitor.setProgress(updater.getCurrent());
                    return;
                }
                
                taskTimer.stop();
                monitor.close();
                
                if (updater.getResult()) {
                    logger.info("�_�E�����[�h�ɐ������܂���");
                    update4(updateUrl, updater.getReadBytes(), updateLastModied);
                } else {
                    // warning
                    logger.warn("�_�E�����[�h�Ɏ��s���܂���");
                }
            }
        });
        monitor.setProgress(0);
        monitor.setMillisToDecideToPopup(decideToPopup);
        monitor.setMillisToPopup(milisToPopup);
        updater.downLoad(updateUrl);
        taskTimer.start();
    }
    
    /**
     * �X�V����B
     * @param url
     * @param readBytes
     * @param lastModified
     */
    private void update4(ArrayList<String> url, ArrayList<byte[]> readBytes, ArrayList<String> lastModified) {
        
        final Logger logger = ClientContext.getLogger("boot");
        int cnt = url.size();
        String resource = null;
        String urlString = null;
        boolean result = false;
        
        try {
            for (int i = 0; i < cnt; i++) {
                urlString = url.get(i);
                int index = urlString.lastIndexOf("/");
                resource = urlString.substring(index + 1);
                // resource = ClientContext.getLocation("lib") + File.separator + resource;
                // Version 1.2 �̎����ł� OpenDolphin-1.2.jar �݂̂��X�V����
                resource = ClientContext.getLocation("dolphin.jar") + File.separator + resource;
                logger.info(resource + " ���X�V���܂�");
                File dest = new File(resource);
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
                out.write(readBytes.get(i));
                out.flush();
                out.close();
                
                Project.setLastModify(Long.parseLong(lastModified.get(i)));
            }
            result = true;
            
        } catch (Exception e) {
            // Show error message
            String errorMsg = ClientContext.getString("updater.msg.updateError");
            String title = ClientContext.getString("updater.task.titl");
            JOptionPane.showMessageDialog(
                    null,
                    errorMsg,
                    ClientContext.getFrameTitle(title),
                    JOptionPane.ERROR_MESSAGE);
        }
        
        if (result) {
            
            //ClientContextStub stub = (ClientContextStub) ClientContext.getClientContextStub();
            
            String title = ClientContext.getString("updater.task.title");
            StringBuilder sb = new StringBuilder();
            sb.append(ClientContext.getString("updater.msg.updateSuccess1"));
            sb.append("\n");
            sb.append(ClientContext.getString("updater.msg.updateSuccess2"));
            String msg = sb.toString();
            
            // Show succeeded message
            JOptionPane.showMessageDialog(
                    null,
                    msg,
                    ClientContext.getFrameTitle(title),
                    JOptionPane.INFORMATION_MESSAGE);
            
            System.exit(1);
        }
    }
    
    /**
     * �h���t�B���T�|�[�g���I�[�v������B
     */
    public void browseDolphinSupport() {
        browseURL(ClientContext.getString("mainWindow.menu.dolphinSupportUrl"));
    }
    
    /**
     * �h���t�B���v���W�F�N�g���I�[�v������B
     */
    public void browseDolphinProject() {
        browseURL(ClientContext.getString("mainWindow.menu.dolphinUrl"));
    }
    
    /**
     * MedXML���I�[�v������B
     */
    public void browseMedXml() {
        browseURL(ClientContext.getString("mainWindow.menu.medXmlUrl"));
    }
    
    /**
     * SG���I�[�v������B
     */
    public void browseSeaGaia() {
        browseURL(ClientContext.getString("mainWindow.menu.seaGaiaUrl"));
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
                // Unsupported OS
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
        IMainWindowPlugin plugin = (IMainWindowPlugin) getPlugin(GUIConst.JNDI_SCHEMA_BOX);
        if (plugin == null) {
            createPlugin(GUIConst.JNDI_SCHEMA_BOX);
        } else {
            plugin.toFront();
        }
    }
    
    /**
     * �X�^���v�{�b�N�X��\������B
     */
    public void showStampBox() {
        IMainWindowPlugin plugin = (IMainWindowPlugin) getPlugin(GUIConst.JNDI_STAMP_BOX);
        if (plugin == null) {
            createPlugin(GUIConst.JNDI_STAMP_BOX);
        } else {
            plugin.toFront();
        }
    }
    
    // //////////////////////////////////////////////////////////////
    
    /**
     * Mediator
     */
    protected final class Mediator extends MenuSupport {
        
        public Mediator(Object owner) {
            super(owner);
        }
        
        // global property �̐���
        public void menuSelected(MenuEvent e) {
        }
        
        public void registerActions(Hashtable<String, Action> actions) {
            super.registerActions(actions);
            // ���C���E�C���h�E�Ȃ̂ŕ��邾���͖����ɂ���
            getAction(GUIConst.ACTION_WINDOW_CLOSING).setEnabled(false);
        }
    }
    
    //////////////////// StateMgr ///////////////////////////////
    
    /**
     * MainWindowState
     */
    abstract class MainWindowState {
        
        public MainWindowState() {
            super();
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
            Action addUserAction = mediator.getAction(GUIConst.ADD_USER);
            boolean admin = false;
            Collection<RoleModel> roles = Project.getUserModel().getRoles();
            for (RoleModel model : roles) {
                if (model.getRole().equals(GUIConst.ROLE_ADMIN)) {
                    admin = true;
                    break;
                }
            }
            addUserAction.setEnabled(admin);
            
            mediator.getAction(GUIConst.CHANGE_PASSWORD).setEnabled(true);
            mediator.getAction(GUIConst.UPDATE_SOFTWARE).setEnabled(true);
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
            mediator.getAction(GUIConst.CHANGE_PASSWORD).setEnabled(false);
            mediator.getAction(GUIConst.ADD_USER).setEnabled(false);
            mediator.getAction(GUIConst.UPDATE_SOFTWARE).setEnabled(false);
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
}