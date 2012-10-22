/*
 * Created on 2005/07/11
 *
 */
package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.KarteBean;
import open.dolphin.infomodel.PVTHealthInsuranceModel;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.plugin.helper.ComponentMemory;
import open.dolphin.plugin.helper.WindowSupport;
import open.dolphin.project.Project;

/**
 * EditorFrame
 *
 * @author Kazushi Minagawa
 */
public class EditorFrame extends DefaultMainWindowPlugin implements IChart {
    
    // ���̃N���X�̂Q�̃��[�h�i��ԁj�Ń��j���[�̐���Ɏg�p����
    public enum EditorMode {BROWSER, EDITOR};
    
    // �S�C���X�^���X��ێ����郊�X�g
    private static List<IChart> allEditorFrames = new ArrayList<IChart>(3);
    
    // �t���[���T�C�Y�֘A
    private static final int FRAME_X = 25;
    private static final int FRAME_Y = 20;
    private static final int FRAME_WIDTH          = 724;
    private static final int FRAME_HEIGHT         = 740;
    private static final String TITLE_ASSIST = " - �J���e";
    
    /** ���̃t���[���̎��̃R���e�L�X�g�`���[�g */
    private IChart realChart;
    
    /** ���̃t���[���ɕ\������ KarteView �I�u�W�F�N�g */
    private KarteViewer view;
    
    /** ���̃t���[���ɕ\������ KarteEditor �I�u�W�F�N�g */
    private KarteEditor editor;
    
    /** ToolBar �p�l�� */
    private JPanel myToolPanel;
    
    /** �X�N���[���R���|�[�l���g */
    private JScrollPane scroller;
    
    /** Status �p�l�� */
    private IStatusPanel statusPanel;
    
    /** ���̃t���[���̓��샂�[�h */
    private EditorMode mode;
    
    /** WindowSupport �I�u�W�F�N�g */
    private WindowSupport windowSupport;
    
    /** Mediator �I�u�W�F�N�g */
    private ChartMediator mediator;
    
    /** Block GlassPane */
    private BlockGlass blockGlass;
    
    /** �e�`���[�g�̈ʒu */
    private Point parentLoc;
    
    private JPopupMenu insurancePop;
    
    /**
     * �S�C���X�^���X��ێ����郊�X�g��Ԃ��B
     * @return �S�C���X�^���X��ێ����郊�X�g
     */
    public static List<IChart> getAllEditorFrames() {
        return allEditorFrames;
    }
    
    private static PageFormat pageFormat = null;
    static {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        if (printJob != null && pageFormat == null) {
            // set default format
            pageFormat = printJob.defaultPage();
        }
    }
    
    /**
     * EditorFrame �I�u�W�F�N�g�𐶐�����B
     */
    public EditorFrame() {
        allEditorFrames.add(this);
    }
    
    /**
     * IChart �R���e�L�X�g��ݒ肷��B
     * @param chartCtx IChart �R���e�L�X�g
     */
    public void setChart(IChart chartCtx) {
        this.realChart = chartCtx;
        parentLoc = realChart.getFrame().getLocation();
        super.setContext(chartCtx.getContext());
    }
    
    public IChart getChart() {
        return realChart;
    }
    
    /**
     * �\������ KarteViewer �I�u�W�F�N�g��ݒ肷��B
     * @param view �\������ KarteView
     */
    public void setKarteViewer(KarteViewer view) {
        this.view = view;
    }
    
    /**
     * �ҏW���� KarteEditor �I�u�W�F�N�g��ݒ肷��B
     * @param editor �ҏW���� KarteEditor
     */
    public void setKarteEditor(KarteEditor editor) {
        this.editor = editor;
    }
    
    /**
     * ���҃��f����Ԃ��B
     * @return ���҃��f��
     */
    public PatientModel getPatient() {
        return realChart.getPatient();
    }
    
    /**
     * �ΏۂƂ��Ă��� KarteBean �I�u�W�F�N�g��Ԃ��B
     * @return KarteBean �I�u�W�F�N�g
     */
    public KarteBean getKarte() {
        return realChart.getKarte();
    }
    
    /**
     * �ΏۂƂȂ� KarteBean �I�u�W�F�N�g��ݒ肷��B
     * @param karte KarteBean �I�u�W�F�N�g
     */
    public void setKarte(KarteBean karte) {
        realChart.setKarte(karte);
    }
    
    /**
     * ���@����Ԃ��B
     * @return ���@���
     */
    public PatientVisitModel getPatientVisit() {
        return realChart.getPatientVisit();
    }
    
    /**
     * ���@����ݒ肷��B
     * @param model ���@��񃂃f��
     */
    public void setPatientVisit(PatientVisitModel model) {
        realChart.setPatientVisit(model);
    }
    
//    public void setClaimSent(boolean b) {
//        realChart.setClaimSent(b);
//    }
//
//    public boolean isClaimSent() {
//        return realChart.isClaimSent();
//    }
    
    /**
     * Chart state ��Ԃ��B
     * @return Chart �� state ����
     */
    public int getChartState() {
        return realChart.getChartState();
    }
    
    /**
     * Chart state ��ݒ肷��B
     * @param state Chart �� state
     */
    public void setChartState(int state) {
        realChart.setChartState(state);
    }
    
    /**
     * ReadOnly ���ǂ�����Ԃ��B
     * @return readOnly �̎� true
     */
    public boolean isReadOnly() {
        return realChart.isReadOnly();
    }
    
    /**
     * ReadOnly ������ݒ肷��B
     * @param readOnly �̎� true
     */
    public void setReadOnly(boolean b) {
        realChart.setReadOnly(b);
    }
    
    /**
     * ���̃I�u�W�F�N�g�� JFrame ��Ԃ��B
     * @return JFrame �I�u�W�F�N�g
     */
    public JFrame getFrame() {
        return windowSupport.getFrame();
    }
    
    /**
     * StatusPanel ��Ԃ��B
     * @return StatusPanel
     */
    public IStatusPanel getStatusPanel() {
        return this.statusPanel;
    }
    
    /**
     * StatusPanel ��ݒ肷��B
     * @param statusPanel StatusPanel �I�u�W�F�N�g
     */
    public void setStatusPanel(IStatusPanel statusPanel) {
        this.statusPanel = statusPanel;
    }
    
    /**
     * ChartMediator ��Ԃ��B
     * @return ChartMediator
     */
    public ChartMediator getChartMediator() {
        return mediator;
    }
    
    /**
     * DocumentHistory ��Ԃ��B
     * @return DocumentHistory
     */
    public DocumentHistory getDocumentHistory() {
        return realChart.getDocumentHistory();
    }
    
    /**
     * �����̃^�u�ԍ��ɂ���h�L�������g��\������B
     * @param index �\������h�L�������g�̃^�u�ԍ�
     */
    public void showDocument(int index) {
        realChart.showDocument(index);
    }
    
    /**
     * dirty ���ǂ�����Ԃ��B
     * @return dirty �̎� true
     */
    public boolean isDirty() {
        return (mode == EditorMode.EDITOR) ? editor.isDirty() : false;
    }
    
    /**
     * �v���O�������J�n����B
     */
    public void start() {
        
        //
        // �R���|�[�l���g�̏�������ʃX���b�h�ōs��������Ăяo�����ɕԂ�
        //
        Runnable r = new Runnable() {
            public void run() {
                initialize();
            }
        };
        Thread t = new Thread(r);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }
    
    /**
     * ����������B
     */
    @SuppressWarnings("serial")
    private void initialize() {
        
        //
        // Frame �𐶐�����
        // Frame �̃^�C�g����
        // ���Ҏ���(�J�i):����:����ID �ɐݒ肷��
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
        JMenuBar myMenuBar = windowSupport.getMenuBar();
        final JFrame frame = windowSupport.getFrame();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                processWindowClosing();
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
            int x = parentLoc.x + FRAME_X;
            int y = parentLoc.y + FRAME_Y;
            Point loc = new Point(x, y);
            Dimension size = new Dimension(FRAME_WIDTH, FRAME_HEIGHT);
            ComponentMemory cm = new ComponentMemory(frame, loc, size, this);
            cm.setToPreferenceBounds();
        }
        
        blockGlass = new BlockGlass();
        frame.setGlassPane(blockGlass);
        
        //
        // Mediator ���ύX�ɂȂ�
        //
        mediator = new ChartMediator(this);
        
        //
        //  MenuBar �𐶐�����
        //
        Object[] menuStaff = realChart.getContext().createMenuBar(myMenuBar,mediator);
        myToolPanel = (JPanel) menuStaff[1];
        frame.getContentPane().add(myToolPanel, BorderLayout.NORTH);
        
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
        insurancePop = new JPopupMenu();
        PVTHealthInsuranceModel[] insurances = ((ChartPlugin)realChart).getHealthInsurances();
        for (PVTHealthInsuranceModel hm : insurances) {
            ReflectActionListener ra = new ReflectActionListener(mediator,
                    "applyInsurance",
                    new Class[]{hm.getClass()},
                    new Object[]{hm});
            JMenuItem mi = new JMenuItem(hm.toString());
            mi.addActionListener(ra);
            insurancePop.add(mi);
        }
        
        stampBtn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                insurancePop.show(e.getComponent(), e.getX(), e.getY());
            }
        });
        
        statusPanel = new StatusPanel();
        
        //
        // ���̂� Event Dispatch �X���b�h�� GUI �̑g�ݗ��Ă����Ă���
        //
        Runnable awt = new Runnable() {
            
            public void run() {
                
                if (view != null) {
                    mode = EditorMode.BROWSER;
                    view.setContext(EditorFrame.this);
                    view.initialize();
                    view.start();
                    scroller = new JScrollPane(view.getUI());
                    
                } else if (editor != null) {
                    mode = EditorMode.EDITOR;
                    editor.setContext(EditorFrame.this);
                    editor.initialize();
                    editor.start();
                    scroller = new JScrollPane(editor.getUI());
                }
                
                frame.getContentPane().add(scroller, BorderLayout.CENTER);
                frame.getContentPane().add((JPanel) statusPanel, BorderLayout.SOUTH);
                frame.setVisible(true);
            }
        };
        
        SwingUtilities.invokeLater(awt);
        
    }
    
    /**
     * �v���O�������I������B
     */
    public void stop() {
        mediator.dispose();
        allEditorFrames.remove(this);
        getFrame().setVisible(false);
        getFrame().dispose();
    }
    
    /**
     * �E�C���h�E�� close box �������ꂽ���̏��������s����B
     */
    public void processWindowClosing() {
        close();
    }
    
    /**
     * �E�C���h�E�I�[�v�����̏������s���B
     */
    public void processWindowOpened() {
    }
    
    /**
     * Focus �Q�C���𓾂����̏������s���B
     */
    public void processGainedFocus() {
        
        switch (mode) {
            case BROWSER:
                if (view != null) {
                    view.enter();
                }
                break;
                
            case EDITOR:
                if (editor != null) {
                    editor.enter();
                }
                break;
        }
    }
    
    /**
     * �R���e���c�� KarteView ���� KarteEditor �ɐ؂�ւ���B
     */
    private void replaceView() {
        if (editor != null) {
            mode = EditorMode.EDITOR;
            getFrame().getContentPane().remove(scroller);
            scroller = new JScrollPane(editor.getUI());
            getFrame().getContentPane().add(scroller, BorderLayout.CENTER);
            getFrame().validate();
        }
    }
    
    /**
     * �V�K�J���e���쐬����B
     */
    public void newKarte() {
        
        //
        // �V�K�J���e�쐬�_�C�A���O��\�����p�����[�^�𓾂�
        //
        final ChartPlugin chart = (ChartPlugin) realChart;
        String dept = chart.getPatientVisit().getDepartment();
        String deptCode = chart.getPatientVisit().getDepartmentCode();
        String insuranceUid = chart.getPatientVisit().getInsuranceUid();
        
        NewKarteParams params = null;
        Preferences prefs = Project.getPreferences();
        
        if (prefs.getBoolean(Project.KARTE_SHOW_CONFIRM_AT_NEW, true)) {
            
            params = chart.getNewKarteParams(IChart.NewKarteOption.EDITOR_COPY_NEW, getFrame(), dept, deptCode, insuranceUid);
            
        } else {
            //
            // �蓮�Ńp�����[�^��ݒ肷��
            //
            params = new NewKarteParams(IChart.NewKarteOption.EDITOR_COPY_NEW);
            params.setDepartment(dept);
            params.setDepartmentCode(deptCode);
            
            PVTHealthInsuranceModel[] ins = chart.getHealthInsurances();
            params.setPVTHealthInsurance(ins[0]);
            
            int cMode = prefs.getInt(Project.KARTE_CREATE_MODE, 0);
            if (cMode == 0) {
                params.setCreateMode(IChart.NewKarteMode.EMPTY_NEW);
            } else if (cMode == 1) {
                params.setCreateMode(IChart.NewKarteMode.APPLY_RP);
            } else if (cMode == 2) {
                params.setCreateMode(IChart.NewKarteMode.ALL_COPY);
            }
        }
        
        if (params == null) {
            return;
        }
        
        // �ҏW�p�̃��f���𓾂�
        DocumentModel editModel = null;
        if (params.getCreateMode() == IChart.NewKarteMode.EMPTY_NEW) {
            editModel = chart.getKarteModelToEdit(params);
        } else {
            editModel = chart.getKarteModelToEdit(view.getModel(), params);
        }
        final DocumentModel theModel = editModel;
        
        Runnable r = new Runnable() {
            
            public void run() {
                
                editor = chart.createEditor();
                editor.setModel(theModel);
                editor.setEditable(true);
                editor.setContext(EditorFrame.this);
                
                Runnable awt = new Runnable() {
                    public void run() {
                        editor.initialize();
                        editor.start();
                        replaceView();
                    }
                };
                
                SwingUtilities.invokeLater(awt);
            }
        };
        
        Thread t = new Thread(r);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }
    
    /**
     * �J���e���C������B
     */
    public void modifyKarte() {
        
        Runnable r = new Runnable() {
            
            public void run() {
                
                ChartPlugin chart = (ChartPlugin)realChart;
                DocumentModel editModel = chart.getKarteModelToEdit(view.getModel());
                editor = chart.createEditor();
                editor.setModel(editModel);
                editor.setEditable(true);
                editor.setContext(EditorFrame.this);
                editor.setModify(true);
                
                Runnable awt = new Runnable() {
                    public void run() {
                        editor.initialize();
                        editor.start();
                        replaceView();
                    }
                };
                
                SwingUtilities.invokeLater(awt);
            }
        };
        
        Thread t = new Thread(r);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }
    
    private PageFormat getPageFormat() {
        return realChart.getContext().getPageFormat();
    }
    
    /**
     * �������B
     */
    public void print() {
        
        switch (mode) {
            
            case BROWSER:
                if (view != null) {
                    view.printPanel2(getPageFormat());
                }
                break;
                
            case EDITOR:
                if (editor != null) {
                    editor.printPanel2(getPageFormat());
                }
                break;
        }
    }
    
    /**
     * �N���[�Y����B
     */
    public void close() {
        
        if (mode == EditorMode.EDITOR) {
            
            if (editor.isDirty()) {
                
                String save = ClientContext.getString("chart.unsavedtask.saveText"); //"�ۑ�";
                String discard = ClientContext.getString("chart.unsavedtask.discardText"); //"�j��";
                String question = ClientContext.getString("editoFrame.unsavedtask.question"); // ���ۑ��̃h�L�������g������܂��B�ۑ����܂��� ?
                String title = ClientContext.getString("chart.unsavedtask.title"); // ���ۑ�����
                String cancelText =  (String)UIManager.get("OptionPane.cancelButtonText");
                int option = JOptionPane.showOptionDialog(
                        getFrame(),
                        question,
                        ClientContext.getFrameTitle(title),
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[]{save, discard, cancelText},
                        save
                        );
                
                switch (option) {
                    
                    case 0:
                        editor.save();
                        break;
                        
                    case 1:
                        stop();
                        break;
                        
                    case 2:
                        break;
                        
                }
                
            } else {
                stop();
            }
            
        } else {
            stop();
        }
    }
}
