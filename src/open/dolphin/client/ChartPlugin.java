/*
 * ChartPlugin.java
 * Copyright 2001,2002 Dolphin project. All Rights Reserved.
 * Copyright 2004-2005 Digital Globe, Inc. All Rights Reserved.
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

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.beans.*;
import java.text.SimpleDateFormat;
import java.util.*;

import open.dolphin.delegater.DocumentDelegater;
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
import open.dolphin.order.MMLTable;
import open.dolphin.plugin.*;
import open.dolphin.plugin.helper.*;
import open.dolphin.project.*;
import open.dolphin.util.GUIDGenerator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 2���J���e�A���a���A�������ʗ��𓙁A���҂̑����I�f�[�^��񋟂���N���X�B
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class ChartPlugin extends DefaultMainWindowPlugin implements IChart,IInfoModel {
    
    private static final long serialVersionUID = 3074544825882680694L;
    
    /** �J���e��Ԃ̑����v���p�e�B�� */
    public static final String CHART_STATE = "chartStateProp";
    
    /** �f�@���I���ŕ��Ă����� */
    public static final int CLOSE_NONE      = 0;
    
    /** �f�@���I�������Ă�����  */
    public static final int CLOSE_SAVE      = 1;
    
    /** �f�@���I���ŃI�[�v�����Ă����� */
    public static final int OPEN_NONE       = 2;
    
    /** �f�@���I�����I�[�v�����Ă����� */
    public static final int OPEN_SAVE       = 3;
    
    /** ��t�L�����Z�� */
    public static final int CANCEL_PVT      = -1;
    
    /**  Chart �C���X�^���X���Ǘ�����static �ϐ� */
    private static ArrayList<ChartPlugin> allCharts = new ArrayList<ChartPlugin>(3);
    
    /** Chart ��Ԃ̒ʒm���s�����߂� static �����T�|�[�g */
    private static PropertyChangeSupport boundSupport = new PropertyChangeSupport(new Object());
    
    // �t���[���T�C�Y�ƈʒu
    private static final int FRAME_X = 25;
    
    private static final int FRAME_Y = 20;
    
    private static final int DOCUMENT_WIDTH     = 710; // 345+345+2+scrollW = 692+17 = 710? -> 724
    
    private static final int DOC_HISTORY_WIDTH  = 280;
    
    private static final int FRAME_WIDTH = DOCUMENT_WIDTH + DOC_HISTORY_WIDTH;
    
    private static final int FRAME_HEIGHT   = 740;
    
    private static final int HISTORY_PALCE  = 0; // 0 = LEFT, 1 = RIGHT
    
    private static final int DIVIDER_SIZE   = 1;
    
    private static final String TITLE_ASSIST = " - �C���X�y�N�^";
    
    private static final String SAMA = "�l";
    
    // ���̃N���X����N������v���O�C���� JNDI ��
    private static final String KARTE_EDITOR_JNDI = "chart/karteEditor";
    
    // ���̃N���X�̃v���O�C��
    private static final String DOCUMENT_PLUG_POINT = "chart/comp"; // PlugPoint
    
    //
    // �C���X�^���X�ϐ�
    //
    
    /** Document Plugin ���i�[���� TabbedPane */
    private JTabbedPane tabbedPane;
    
    /** Document Plugin �̃��X�g */
    private ArrayList<PluginReference> documents; 
    
    /** Active �ɂȂ��Ă���Document Plugin */
    private Hashtable<String, IChartDocument> activeChildren;
    
    /** ���҃C���X�y�N�^ */
    private PatientInspector inspector; 
    
    /** �h�L�������g�ƃq�X�g���𕪊����� SpltPane */
    private int historyPlace = HISTORY_PALCE; // �q�X�g���[�������Ɉʒu������
    
    /** �f�B�o�C�_�̃T�C�Y */
    private int dividerSize = DIVIDER_SIZE;
    
    /** �f�B�o�C�_�̈ʒu */
    private int dividerLocation
            = HISTORY_PALCE == 0 ? DOC_HISTORY_WIDTH : DOCUMENT_WIDTH;
    
    /** Window Menu ���T�|�[�g����Ϗ��N���X */
    private WindowSupport windowSupport;
    
    /** Toolbar */
    private JPanel myToolPanel;
    
    /** �����󋵓���\�����鋤�ʂ̃p�l�� */
    private IStatusPanel statusPanel;
    
    /** ���җ��@��� */
    private PatientVisitModel pvt;
    
    /** Read Only �̎� true */
    private boolean readOnly;
    
//    // CLAIM�f�[�^�𑗐M�����ꍇ�� true
//    private boolean claimSent;
    
    /** Chart �̃X�e�[�g */
    private int chartState;
    
    /** Chart���̃h�L�������g�ɋ��ʂ� MEDIATOR */
    private ChartMediator mediator;
    
    /** State Mgr */
    private StateMgr stateMgr;
    
    /** MML���M listener */
    private MmlMessageListener mmlListener;
    
    /** CLAIM ���M listener */
    private ClaimMessageListener claimListener;
       
    /** ���̃`���[�g�� KarteBean */
    private KarteBean karte;
    
    //
    // �^�X�N�֘A�̒萔
    //
    /** Task Timer */
    private javax.swing.Timer taskTimer;
    
    /** ���荞�ݎ��� msec */
    private static final int TIMER_DELAY = 200;
    
    /** �S�̂̌��ς莞�� */
    private static final int MAX_ESTIMATION = 3000;
    
    /** 300 msec ��Ƀ|�b�v�A�b�v�̔��f������ */
    private static final int DECIDE_TO_POPUP = 300;
    
    /** ���̎� Task�� 500msec�ȏォ����悤�ł���΃|�b�v�A�b�v���� */
    private static final int MILIS_TO_POPUP = 500;
    
    /** Progress monitor  */
    private static final String PROGRESS_NOTE = "�J���e���J���Ă��܂�...";
    
    /** GlassPane */
    private BlockGlass blockGlass;
    
    /**
     * Creates new ChartService
     */
    public ChartPlugin() {
    }
    
    /**
     * ���̃`���[�g�̃J���e��Ԃ��B
     * @return �J���e
     */
    public KarteBean getKarte() {
        return karte;
    }
    
    /**
     * ���̃`���[�g�̃J���e��ݒ肷��B
     * @param karte ���̃`���[�g�̃J���e
     */
    public void setKarte(KarteBean karte) {
        this.karte = karte;
    }
    
    /**
     * Chart �� JFrame ��Ԃ��B
     * @return �`���[�g�E�C���h�Eno JFrame
     */
    public JFrame getFrame() {
        return windowSupport.getFrame();
    }
    
    /**
     * Chart���h�L�������g�����ʂɎg�p���� Status �p�l����Ԃ��B
     * @return IStatusPanel
     */
    public IStatusPanel getStatusPanel() {
        return statusPanel;
    }
    
    /**
     * Chart���h�L�������g�����ʂɎg�p���� Status �p�l����ݒ肷��B
     * @param statusPanel IStatusPanel
     */
    public void setStatusPanel(IStatusPanel statusPanel) {
        this.statusPanel = statusPanel;
    }
    
    /**
     * ���@����ݒ肷��B
     * @param pvt ���@���
     */
    public void setPatientVisit(PatientVisitModel pvt) {
        this.pvt = pvt;
    }
    
    /**
     * ���@����Ԃ��B
     * @return ���@���
     */
    public PatientVisitModel getPatientVisit() {
        return pvt;
    }
    
    /**
     * ReadOnly ���ǂ�����Ԃ��B
     * @return ReadOnly�̎� true
     */
    public boolean isReadOnly() {
        return readOnly;
    }
    
    /**
     * ReadOnly ������ݒ肷��B
     * @param readOnly ReadOnly user �̎� true
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    
    /**
     * ���̃`���[�g���ΏۂƂ��Ă��銳�҃��f����Ԃ��B
     * @return �`���[�g���ΏۂƂ��Ă��銳�҃��f��
     */
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
    
//    public void setClaimSent(boolean b) {
//        claimSent = b;
//    }
//    
//    public boolean isClaimSent() {
//        return claimSent;
//    }
    
    /**
     * �`���[�g�̃X�e�[�g������Ԃ��B
     * @return �`���[�g�̃X�e�[�g����
     */
    public int getChartState() {
        return chartState;
    }
    
    /**
     * �`���[�g�̃X�e�[�g��ݒ肷��B
     * @param chartState �`���[�g�X�e�[�g
     */
    public void setChartState(int chartState) {
        this.chartState = chartState;
        //
        // �C���X�^���X���Ǘ����� static �I�u�W�F�N�g
        // ���g�p���������X�i�֒ʒm����
        //
        ChartPlugin.fireChanged(this);
    }
    
    /**
     * �`���[�g���ŋ��ʂɎg�p���� Mediator ��Ԃ��B
     * @return ChartMediator
     */
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
     * �����q�X�g���I�u�W�F�N�g��Ԃ��B
     * @return �����q�X�g���I�u�W�F�N�g DocumentHistory
     */
    public DocumentHistory getDocumentHistory() {
        return inspector.getDocumentHistory();
    }
    
    /**
     * �����Ŏw�肳�ꂽ�^�u�ԍ��̃h�L�������g��\������B
     * @param �\������h�L�������g�̃^�u�ԍ�
     */
    public void showDocument(int index) {
        int cnt = tabbedPane.getTabCount();
        if (index >= 0 && index <= cnt -1 && index != tabbedPane.getSelectedIndex()) {
            tabbedPane.setSelectedIndex(index);
        }
    }
    
    /**
     * �`���[�g���ɖ��ۑ��h�L�������g�����邩�ǂ�����Ԃ��B
     * @return ���ۑ��h�L�������g�����鎞 true
     */
    public boolean isDirty() {
        
        boolean dirty = false;
        
        if (activeChildren != null && activeChildren.size() > 0) {
            
            Collection<IChartDocument> docs = activeChildren.values();
            for (IChartDocument doc : docs) {
                if (doc.isDirty()) {
                    dirty = true;
                    break;
                }
            }
        }
        return dirty;
    }
    
    /**
     * �v���O�������J�n����B
     */
    public void start() {
        
        // ������ Worker �𐶐�����
        final CallBacksWorker worker = new CallBacksWorker(this, "initComponent", null, null);
        
        // ������ Worker �p�� ProgressMonitor �𐶐�����
        Object[] messages = new Object[1];
        String patientName = getPatientVisit().getPatient().getFullName() + SAMA;
        messages[0] = new JLabel(patientName, ClientContext.getImageIcon("open_32.gif"), SwingConstants.CENTER);
        final ProgressMonitor monitor = new ProgressMonitor(null, messages, PROGRESS_NOTE, 0, MAX_ESTIMATION / TIMER_DELAY);
        
        // Worker �`�F�b�N�̊��荞�݃^�C�}�[�𐶐�����
        taskTimer = new javax.swing.Timer(TIMER_DELAY, new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                if (worker.isDone()) {
                    taskTimer.stop();
                    monitor.close();
                    //
                    // ���̃R�[���ŕ���������\������
                    //
                    getDocumentHistory().showHistory();
                } else {
                    monitor.setProgress(worker.getCurrent());
                }
            }
        });
        // ���������J�n����
        monitor.setProgress(0);
        monitor.setMillisToDecideToPopup(DECIDE_TO_POPUP);
        monitor.setMillisToPopup(MILIS_TO_POPUP);
        worker.start();
        taskTimer.start();
    }
    
    /**
     * ���҂̃J���e�������擾���AGUI ���\�z����B
     * ���̃��\�b�h�̓o�b�N�O�����h�X���b�h�Ŏ��s�����B
     */
    @SuppressWarnings("serial")
    public void initComponent() {
        
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
        karteBean.setPatient(null);
        karteBean.setPatient(this.getPatientVisit().getPatient());
        setKarte(karteBean);
        
        //
        // ���̃`���[�g �� Frame �𐶐�������������B
        // Frame �̃^�C�g����
        // ���Ҏ���(�J�i):����ID �ɐݒ肷��
        //
        StringBuilder sb = new StringBuilder();
        sb.append(getPatient().getFullName());
        sb.append("(");
        String kana = getPatient().getKanaName();
        kana = kana.replace("�@", " ");
        sb.append(kana);
        sb.append(")");
        //sb.append(" : ");
        //sb.append(getPatient().getGenderDesc());
        sb.append(" : ");
        sb.append(getPatient().getPatientId());
        sb.append(TITLE_ASSIST);
        windowSupport = WindowSupport.create(sb.toString());
        
        // �`���[�g�p�̃��j���[�o�[�𓾂�
        JMenuBar myMenuBar = windowSupport.getMenuBar();
        
        // �`���[�g�� JFrame �I�u�W�F�N�g�𓾂�
        JFrame frame = windowSupport.getFrame();
        
        // BlockGlass ��ݒ肷��
        blockGlass = new BlockGlass();
        frame.setGlassPane(blockGlass);
        
        // ���̃`���[�g�� Window �Ƀ��X�i��ݒ肷��
        frame.addWindowListener(new WindowAdapter() {
            
            public void windowClosing(WindowEvent e) {
                // CloseBox �̏������s��
                processWindowClosing();
            }
            
            public void windowOpened(WindowEvent e) {
                // Window ���I�[�v�����ꂽ���̏������s��
                ChartPlugin.windowOpened(ChartPlugin.this);
            }
            
            public void windowClosed(WindowEvent e) {
                // Window ���N���[�Y���ꂽ���̏������s��
                ChartPlugin.windowClosed(ChartPlugin.this);
            }
            
            public void windowActivated(WindowEvent e) {
                //
                // ���������փt�H�[�J�X����
                //
                getDocumentHistory().requestFocus();
            }
        });
        
        //
        // �t���[���̕\���ʒu�����߂� J2SE 5.0
        //
        boolean locByPlatform = Project.getPreferences().getBoolean(Project.LOCATION_BY_PLATFORM, true);
        
        if (locByPlatform) {
            
            frame.setSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
            frame.setLocationByPlatform(true);
            
        } else {
            frame.setLocationByPlatform(false);
            Point loc = new Point(FRAME_X, FRAME_Y);
            Dimension size = new Dimension(FRAME_WIDTH, FRAME_HEIGHT);
            ComponentMemory cm = new ComponentMemory(frame, loc, size, this);
            cm.setToPreferenceBounds();
        }
        
        // ���҃C���X�y�N�^�𐶐�����
        inspector = new PatientInspector(this);
        inspector.getPanel().setBorder(BorderFactory.createEmptyBorder(7, 7, 5, 2)); // �J�b�g&�g���C
        
        // Status �p�l���𐶐�����
        statusPanel = new StatusPanel();
        
        // Status �p�l���ɕ\��������𐶐�����
        // �J���e�o�^�� Status �p�l���̉E���ɔz�u����
        Date date = getKarte().getCreated();
        String dateF = ClientContext.getString("statusPanel.karte.rdFormat");
        String rdTitle = ClientContext.getString("statusPanel.karte.title");
        SimpleDateFormat sdf = new SimpleDateFormat(dateF);
        String created = sdf.format(date);
        statusPanel.setRightInfo(rdTitle + created); // �J���e�o�^��:yyyy/mm/dd
        // ����ID Status �p�l���̍��ɔz�u����
        String pidTitle = ClientContext.getString("statusPanel.patient.idTitle");
        statusPanel.setLeftInfo(pidTitle + getKarte().getPatient().getPatientId()); // ����ID:xxxxxx
        
        // ChartMediator �𐶐�����
        mediator = new ChartMediator(this);
        
        // MenuBar �𐶐�����
        Object[] menuStaff = getContext().createMenuBar(myMenuBar, mediator);
        myToolPanel = (JPanel) menuStaff[1];
        myToolPanel.add(inspector.getBasicInfoInspector().getPanel(), 0);
        
        //
        // ���̃N���X�ŗL��ToolBar�𐶐�����
        //
        JToolBar toolBar = new JToolBar();
        myToolPanel.add(toolBar);
        
        // �e�L�X�g�c�[���𐶐�����
        AbstractAction action = new AbstractAction(GUIConst.MENU_TEXT) {
            public void actionPerformed(ActionEvent e) {
            }
        };
        mediator.getActions().put(GUIConst.ACTION_INSERT_TEXT, action);
        JButton stampBtn = toolBar.add(action);
        stampBtn.setText("");
        stampBtn.setIcon(ClientContext.getImageIcon("notep_24.gif"));
        stampBtn.setToolTipText(GUIConst.TOOLTIPS_INSERT_TEXT);
        stampBtn.setFocusable(false);
        stampBtn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JPopupMenu popup = new JPopupMenu();
                mediator.addTextMenu(popup);
                if (!e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        
        // �V�F�[�}�c�[���𐶐�����
        action = new AbstractAction(GUIConst.MENU_SCHEMA) {
            public void actionPerformed(ActionEvent e) {
            }
        };
        mediator.getActions().put(GUIConst.ACTION_INSERT_SCHEMA, action);
        stampBtn = toolBar.add(action);
        stampBtn.setText("");
        stampBtn.setIcon(ClientContext.getImageIcon("picts_24.gif"));
        stampBtn.setToolTipText(GUIConst.TOOLTIPS_INSERT_SCHEMA);
        stampBtn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                getContext().showSchemaBox();
            }
        });
        
        // �X�^���v�c�[���𐶐�����
        action = new AbstractAction(GUIConst.MENU_STAMP) {
            public void actionPerformed(ActionEvent e) {
            }
        };
        mediator.getActions().put(GUIConst.ACTION_INSERT_STAMP, action);
        stampBtn = toolBar.add(action);
        stampBtn.setText("");
        stampBtn.setIcon(ClientContext.getImageIcon("lgicn_24.gif"));
        stampBtn.setToolTipText(GUIConst.TOOLTIPS_INSERT_STAMP);
        stampBtn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JPopupMenu popup = new JPopupMenu();
                mediator.addStampMenu(popup);
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
        
        // �ی��I���c�[���𐶐�����
        action = new AbstractAction(GUIConst.MENU_INSURANCE) {
            public void actionPerformed(ActionEvent e) {
            }
        };
        mediator.getActions().put(GUIConst.ACTION_SELECT_INSURANCE, action);
        stampBtn = toolBar.add(action);
        stampBtn.setText("");
        stampBtn.setIcon(ClientContext.getImageIcon("addbk_24.gif"));
        stampBtn.setToolTipText(GUIConst.TOOLTIPS_SELECT_INSURANCE);
        stampBtn.addMouseListener(new MouseAdapter() {
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
        
        // StateMgr �𐶐�����
        stateMgr = new StateMgr();
        
        // Document �v���O�C���̃^�u�𐶐�����
        tabbedPane = loadDocuments();
        
        // �S�̂����C�A�E�g����
        JSplitPane splitPane = null;
        switch (historyPlace) {
            case 0:
                splitPane = new JSplitPane(
                        JSplitPane.HORIZONTAL_SPLIT, inspector.getPanel(), tabbedPane);
                splitPane.setDividerLocation(dividerLocation);
                splitPane.setDividerSize(dividerSize);
                break;
                
            case 1:
                splitPane = new JSplitPane(
                        JSplitPane.HORIZONTAL_SPLIT, tabbedPane,inspector.getPanel());
                splitPane.setDividerLocation(dividerLocation);
                splitPane.setDividerSize(dividerSize);
                break;
        }
        
        JPanel myPanel = getUI();
        myPanel.setLayout(new BorderLayout());
        myPanel.add(myToolPanel, BorderLayout.NORTH);
        myPanel.add(splitPane, BorderLayout.CENTER);
        myPanel.add((JPanel) statusPanel, BorderLayout.SOUTH);
        frame.getContentPane().add(myPanel);
        
        // MML ���M Queue
        if (Project.getSendMML()) {
            mmlListener = (MmlMessageListener) getContext().getPlugin(GUIConst.JNDI_SEND_MML);
        }
        
        // CLAIM ���M Queue
        if (Project.getSendClaim()) {
            claimListener = (ClaimMessageListener) getContext().getPlugin(GUIConst.JNDI_SEND_CLAIM);
        }
        
        // �Ō�Ɏ��̉��̐錾������
        // ����ȍ~�A���̃X���b�h�ŃR���|�[�l���g�ɃA�N�Z�X�ł��Ȃ��B
        getFrame().setVisible(true);
    }
    
    /**
     * ���̃`���[�g�̃v���O�C���R���N�V������Ԃ��B
     * @return �v���O�C���̃R���N�V����
     */
    public Collection listChildren() {
        return null;
    }
    
    /**
     * ���̃`���[�g�̃v���O�C�����R���N�V������Ԃ��B
     * @return �v���O�C�����̃R���N�V����
     */
    public Collection listChildrenNames() {
        return null;
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
    
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * �h�L�������g�^�u�𐶐�����B
     */
    private JTabbedPane loadDocuments() {
        
        // �v���O�C���R���e�L�X�g�𓾂�
        IPluginContext plctx = ClientContext.getPluginContext();
        
        try {
            // �v���O�|�C���g�̃v���O�C���R���N�V�����𓾂�
            Collection<PluginReference> c = plctx.listPluginReferences(DOCUMENT_PLUG_POINT);
            documents = new ArrayList<PluginReference>(c);
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        //
        // ���ۂɐ����i�A�N�e�B�u�j����Ă��� Document plugin ��ێ�����e�[�u��
        //
        activeChildren = new Hashtable<String, IChartDocument>();
        
        //
        // Document ���i�[����^�u�y�C��
        //
        JTabbedPane tab = new JTabbedPane();
        
        //
        // index = 0 �̃v���O�C���𐶐����^�u�ɉ�����
        // 
        try {
            PluginReference plf = documents.get(0);
            IChartDocument plugin = (IChartDocument) plctx.lookup(plf.getJndiName());
            String title = (String) plf.getAddrContent(PluginReference.TITLE);
            plugin.setContext(this);
            plugin.setTitle(title);
            plugin.initialize();
            plugin.start();
            tab.addTab(title, plugin.getUI());
            activeChildren.put(String.valueOf(0), plugin);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // �Q�Ԗڈȍ~�̃v���O�C���͒x���������邽�߃^�C�g���݂̂�ݒ肷��
        int index = 0;
        for (PluginReference plref : documents) {
            if (index++ > 0) {
                tab.addTab((String) plref.getAddrContent(PluginReference.TITLE), null);
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
        final JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
        final int index = tabbedPane.getSelectedIndex();
        String key = String.valueOf(index);
        IChartDocument docPlugin = (IChartDocument) activeChildren.get(key);

        if (docPlugin == null) {
            //
            // �܂���������Ă��Ȃ��v���O�C���𐶐�����
            //
            try {
                PluginReference plf = documents.get(index);
                IPluginContext plctx = ClientContext.getPluginContext();
                final IChartDocument plugin = (IChartDocument) plctx.lookup(plf.getJndiName());
                plugin.setContext(ChartPlugin.this);
                plugin.setTitle((String) plf.getAddrContent(PluginReference.TITLE));

                // 2005-09-21 plugin �� initialize �� start �𕪗�����
                // initialize �ŃR���|�[�l���g�������������ꂪ�\�����ꂽ���start�Ńf�[�^���擾����
                // ���̃p�^�[�����Ƃ���� �a���A���{�e�X�g�A�P�A�}�b�v
                Runnable r = new Runnable() {
                    public void run() {
                        plugin.initialize();
                        Runnable awt = new Runnable() {
                            public void run() {
                                tabbedPane.setComponentAt(index, plugin.getUI());
                                plugin.start();
                                activeChildren.put(String.valueOf(index), plugin);
                            }
                        };
                        EventQueue.invokeLater(awt);
                    }
                };
                Thread t = new Thread(r);
                t.setPriority(Thread.NORM_PRIORITY);
                t.start();

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else {
            //
            // ���ɐ����ς݃v���O�C���̏ꍇ�� enter() ���R�[������
            //
            docPlugin.enter();
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * �V�K�J���e�̃��f���𐶐�����B
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
        
        //
        // �f�ÉȂ�ݒ肷��
        // ��t��񂩂瓾�Ă���
        //
        String dept = params.getDepartment();
        docInfo.setDepartmentDesc(dept); // department
        
        // �f�ÉȃR�[�h
        // ��t����Ƃ��Ă��Ȃ��ꍇ....
        String deptCode = params.getDepartmentCode();
        if (deptCode == null) {
            docInfo.setDepartment(MMLTable.getDepartmentCode(dept)); // dept.code
        }
        
        // ���N�ی���ݒ肷��
        PVTHealthInsuranceModel insurance = params.getPVTHealthInsurance();
        docInfo.setHealthInsurance(insurance.getInsuranceClassCode());
        docInfo.setHealthInsuranceDesc(insurance.toString());
        //docInfo.setHealthInsuranceDesc(insurance.getInsuranceClass());
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
        boolean applyRp = params.getCreateMode() == IChart.NewKarteMode.APPLY_RP ? true : false;
        copyModel(oldModel, newModel, applyRp);
        
        //
        // �V�K�J���e�� DocInfo ��ݒ肷��
        //
        DocInfoModel docInfo = newModel.getDocInfo();
        
        // ����ID
        docInfo.setDocId(GUIDGenerator.generate(docInfo));
        
        // �����ړI
        docInfo.setPurpose(PURPOSE_RECORD);
        
        //
        // �f�ÉȂ�ݒ肷�� ��t��񂩂�ݒ肷��
        //
        String dept = params.getDepartment();
        docInfo.setDepartmentDesc(dept);
        
        // �f�ÉȃR�[�h
        // ��t����Ƃ��Ă��Ȃ��ꍇ....
        String deptCode = params.getDepartmentCode();
        if (deptCode == null) {
            docInfo.setDepartment(MMLTable.getDepartmentCode(dept)); // dept.code
        }
        
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
        try {
            IPluginContext pluginCtx = ClientContext.getPluginContext();
            KarteEditor editor = (KarteEditor) pluginCtx.lookup(KARTE_EDITOR_JNDI);
            editor.addMMLListner(mmlListener); // Listeners to send XML
            editor.addCLAIMListner(claimListener);
            return editor;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
                        if (((ClaimBundle) model).getClassCode().equals(CLAIM_210)) {
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
    public NewKarteParams getNewKarteParams(IChart.NewKarteOption option, JFrame frame, String dept, String deptCode, String insuranceUid) {
        
        NewKarteParams params = new NewKarteParams(option);
        params.setDepartment(dept);
        params.setDepartmentCode(deptCode);
        
        // ����ی���ǉ� 2006-05-01 �p�~
        // PVTHealthInsuranceModel self = new PVTHealthInsuranceModel();
        // self.setInsuranceClass("����");
        // self.setInsuranceClassCode("Z1");
        // self.setInsuranceClassCodeSys("MML031");
        
        //
        // ���҂̌��N�ی��R���N�V����
        // 
        Collection<PVTHealthInsuranceModel> insurances
                = pvt.getPatient().getPvtHealthInsurances();
        
        //
        // �R���N�V������ null �̏ꍇ�͎���ی���ǉ�����
        //
        if (insurances == null) {
            insurances = new ArrayList<PVTHealthInsuranceModel>(1);
            PVTHealthInsuranceModel model = new PVTHealthInsuranceModel();
            model.setInsuranceClass(INSURANCE_SELF);
            model.setInsuranceClassCode(INSURANCE_SELF_CODE);
            model.setInsuranceClassCodeSys(INSURANCE_SYS);
        }
        
        //
        // �ی��R���N�V������z��ɕϊ����A�p�����[�^�ɃZ�b�g����
        // ���[�U�����̒��̕ی���I������
        //
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
        
        
        String text = option == IChart.NewKarteOption.BROWSER_MODIFY 
                    ? "�J���e�C��" 
                    : "�V�K�J���e";
        
        text = ClientContext.getFrameTitle(text);
        
        JFrame parent = frame != null ? frame : getFrame();
        
        // ���[�_���_�C�A���O��\������
        NewKarteDialog od = new NewKarteDialog(parent, text);
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
    public PVTHealthInsuranceModel[] getHealthInsurances() {
        // ����ی���ǉ� 2006-05-01 �p�~
        // PVTHealthInsuranceModel self = new PVTHealthInsuranceModel();
        // self.setInsuranceClass("����");
        // self.setInsuranceClassCode("Z1");
        // self.setInsuranceClassCodeSys("MML031");
        
        // ���҂̌��N�ی�
        Collection<PVTHealthInsuranceModel> insurances
                = pvt.getPatient().getPvtHealthInsurances();
        
        if (insurances == null) {
            insurances = new ArrayList<PVTHealthInsuranceModel>(1);
            PVTHealthInsuranceModel model = new PVTHealthInsuranceModel();
            model.setInsuranceClass(INSURANCE_SELF);
            model.setInsuranceClassCode(INSURANCE_SELF_CODE);
            model.setInsuranceClassCodeSys(INSURANCE_SYS);
        }
        
        return (PVTHealthInsuranceModel[])insurances.toArray(new PVTHealthInsuranceModel[insurances.size()]);   
    }
    
    /**
     * �^�u�Ƀh�L�������g��ǉ�����B
     * @param doc �ǉ�����h�L�������g
     * @param params �ǉ�����h�L�������g�̏���ێ����� NewKarteParams
     */
    public void addChartDocument(IChartDocument doc, NewKarteParams params) {
        String title = getTabTitle(params.getDepartment(), params.getPVTHealthInsurance().getInsuranceClass());
        tabbedPane.addTab(title, doc.getUI());
        int index = tabbedPane.getTabCount() - 1;
        activeChildren.put(String.valueOf(index), doc);
        tabbedPane.setSelectedIndex(index);
    }
    
    /**
     * �^�u�Ƀh�L�������g��ǉ�����B
     * @param title �^�u�^�C�g��
     */
    public void addChartDocument(IChartDocument doc, String title) {
        tabbedPane.addTab(title, doc.getUI());
        int index = tabbedPane.getTabCount() - 1;
        activeChildren.put(String.valueOf(index), doc);
        tabbedPane.setSelectedIndex(index);
    }

   /**
     * �V�K�J���e�p�̃^�u�^�C�g�����쐬����
     * @param insurance �ی���
     * @return �^�u�^�C�g��
     */
    public String getTabTitle(String dept, String insurance) {
        StringBuilder buf = new StringBuilder();
        buf.append("�L��(");
        buf.append(dept);
        buf.append("�E");
        buf.append(insurance);
        buf.append(")");
        return buf.toString();
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
                    IChartDocument doc = (IChartDocument) activeChildren.get(String.valueOf(undoc.getIndex()));
                    if (doc != null && doc.isDirty()) {
                        tabbedPane.setSelectedIndex(undoc.getIndex());
                        doc.save();
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
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
            IChartDocument doc = (IChartDocument) activeChildren.get(String.valueOf(i));
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
    public void close() {
        
        //
        // ���ۑ��h�L�������g������ꍇ�̓_�C�A���O��\����
        // �ۑ����邩�ǂ������m�F����
        //
        java.util.List<UnsavedDocument> dirtyList = dirtyList();
        
        if (dirtyList != null && dirtyList.size() > 0) {
            
            String saveAll = ClientContext.getString("chart.unsavedtask.saveText");     //"�ۑ�";
            String discard = ClientContext.getString("chart.unsavedtask.discardText");  //"�j��";
            String question = ClientContext.getString("chart.unsavedtask.question");    // ���ۑ��̃h�L�������g������܂��B�ۑ����܂��� ?
            String title = ClientContext.getString("chart.unsavedtask.title");          // ���ۑ�����
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
                    new String[] { saveAll, discard, cancelText },
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
    
    /**
     * ���̃`���[�g���I������BFrame �̌�n��������B
     */
    public void stop() {
        if (activeChildren != null) {
            for (Iterator<String> iter = activeChildren.keySet().iterator(); iter.hasNext(); ) {
                IChartDocument doc = activeChildren.get(iter.next());
                if (doc != null) {
                    doc.stop();
                }
            }
            activeChildren.clear();
        }
        documents.clear();
        mediator.dispose();
        inspector.dispose();
        getFrame().setVisible(false);
        getFrame().setJMenuBar(null);
        getFrame().dispose();
    }
    
    //////////////////// State Mgr /////////////////////////////////////////////
    
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
    public static ArrayList<ChartPlugin> getAllChart() {
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
    public static void windowOpened(ChartPlugin opened) {
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
        boundSupport.firePropertyChange(ChartPlugin.CHART_STATE, null, model);
    }
    
    /**
     * �`���[�g�E�C���h�E�̃N���[�Y��ʒm����B
     * @param closed �N���[�Y���� ChartPlugin
     */
    public static void windowClosed(ChartPlugin closed) {
        
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
            
            //
            // �ʒm����
            //
            model.setState(newState);
            boundSupport.firePropertyChange(ChartPlugin.CHART_STATE, null, model);
            closed = null;
        }
    }
    
    /**
     * �`���[�g��Ԃ̕ω���ʒm����B
     * @param �ω��̂����� ChartPlugin
     */
    public static void fireChanged(ChartPlugin changed) {
        PatientVisitModel model = changed.getPatientVisit();
        model.setState(changed.getChartState());
    }
}