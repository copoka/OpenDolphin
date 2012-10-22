package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.KarteBean;
import open.dolphin.infomodel.PVTHealthInsuranceModel;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.helper.WindowSupport;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.project.Project;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;

/**
 * EditorFrame
 *
 * @author Kazushi Minagawa
 */
public class EditorFrame extends AbstractMainTool implements Chart {
    
    // ���̃N���X�̂Q�̃��[�h�i��ԁj�Ń��j���[�̐���Ɏg�p����
    public enum EditorMode {BROWSER, EDITOR};
    
    // �S�C���X�^���X��ێ����郊�X�g
    private static List<Chart> allEditorFrames = new ArrayList<Chart>(3);
    
    // ���̃t���[���̎��̃R���e�L�X�g�`���[�g
    private Chart realChart;
    
    // ���̃t���[���ɕ\������ KarteView �I�u�W�F�N�g
    private KarteViewer view;
    
    // ���̃t���[���ɕ\������ KarteEditor �I�u�W�F�N�g
    private KarteEditor editor;
    
    // ToolBar �p�l��
    private JPanel myToolPanel;
    
    // �X�N���[���R���|�[�l���g
    private JScrollPane scroller;
    
    // Status �p�l��
    private IStatusPanel statusPanel;
    
    // ���̃t���[���̓��샂�[�h
    private EditorMode mode;
    
    // WindowSupport �I�u�W�F�N�g
    private WindowSupport windowSupport;
    
    // Mediator �I�u�W�F�N�g
    private ChartMediator mediator;
    
    // Block GlassPane 
    private BlockGlass blockGlass;
    
    // �e�`���[�g�̈ʒu 
    private Point parentLoc;
    
    //private JPopupMenu insurancePop;
    
    private ResourceMap resMap;
    
    private JPanel content;
    
    
    /**
     * �S�C���X�^���X��ێ����郊�X�g��Ԃ��B
     * @return �S�C���X�^���X��ێ����郊�X�g
     */
    public static List<Chart> getAllEditorFrames() {
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
    public void setChart(Chart chartCtx) {
        this.realChart = chartCtx;
        parentLoc = realChart.getFrame().getLocation();
        super.setContext(chartCtx.getContext());
    }
    
    public Chart getChart() {
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
    @Override
    public PatientModel getPatient() {
        return realChart.getPatient();
    }
    
    /**
     * �ΏۂƂ��Ă��� KarteBean �I�u�W�F�N�g��Ԃ��B
     * @return KarteBean �I�u�W�F�N�g
     */
    @Override
    public KarteBean getKarte() {
        return realChart.getKarte();
    }
    
    /**
     * �ΏۂƂȂ� KarteBean �I�u�W�F�N�g��ݒ肷��B
     * @param karte KarteBean �I�u�W�F�N�g
     */
    @Override
    public void setKarte(KarteBean karte) {
        realChart.setKarte(karte);
    }
    
    /**
     * ���@����Ԃ��B
     * @return ���@���
     */
    @Override
    public PatientVisitModel getPatientVisit() {
        return realChart.getPatientVisit();
    }
    
    /**
     * ���@����ݒ肷��B
     * @param model ���@��񃂃f��
     */
    @Override
    public void setPatientVisit(PatientVisitModel model) {
        realChart.setPatientVisit(model);
    }
    
    /**
     * Chart state ��Ԃ��B
     * @return Chart �� state ����
     */
    @Override
    public int getChartState() {
        return realChart.getChartState();
    }
    
    /**
     * Chart state ��ݒ肷��B
     * @param state Chart �� state
     */
    @Override
    public void setChartState(int state) {
        realChart.setChartState(state);
    }
    
    /**
     * ReadOnly ���ǂ�����Ԃ��B
     * @return readOnly �̎� true
     */
    @Override
    public boolean isReadOnly() {
        return realChart.isReadOnly();
    }
    
    /**
     * ReadOnly ������ݒ肷��B
     * @param readOnly �̎� true
     */
    @Override
    public void setReadOnly(boolean b) {
        realChart.setReadOnly(b);
    }
    
    /**
     * ���̃I�u�W�F�N�g�� JFrame ��Ԃ��B
     * @return JFrame �I�u�W�F�N�g
     */
    @Override
    public JFrame getFrame() {
        return windowSupport.getFrame();
    }
    
    /**
     * StatusPanel ��Ԃ��B
     * @return StatusPanel
     */
    @Override
    public IStatusPanel getStatusPanel() {
        return this.statusPanel;
    }
    
    /**
     * StatusPanel ��ݒ肷��B
     * @param statusPanel StatusPanel �I�u�W�F�N�g
     */
    @Override
    public void setStatusPanel(IStatusPanel statusPanel) {
        this.statusPanel = statusPanel;
    }
    
    /**
     * ChartMediator ��Ԃ��B
     * @return ChartMediator
     */
    @Override
    public ChartMediator getChartMediator() {
        return mediator;
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
     * DocumentHistory ��Ԃ��B
     * @return DocumentHistory
     */
    @Override
    public DocumentHistory getDocumentHistory() {
        return realChart.getDocumentHistory();
    }
    
    /**
     * �����̃^�u�ԍ��ɂ���h�L�������g��\������B
     * @param index �\������h�L�������g�̃^�u�ԍ�
     */
    @Override
    public void showDocument(int index) {
        realChart.showDocument(index);
    }
    
    /**
     * dirty ���ǂ�����Ԃ��B
     * @return dirty �̎� true
     */
    @Override
    public boolean isDirty() {
        return (mode == EditorMode.EDITOR) ? editor.isDirty() : false;
    }
    
    @Override
    public PVTHealthInsuranceModel[] getHealthInsurances() {
        return realChart.getHealthInsurances();
    }
    
    /**
     * �v���O�������J�n����B
     */
    @Override
    public void start() {
        initialize();
    }
    
    /**
     * ����������B
     */
    private void initialize() {
        
        // ResourceMap ��ۑ�����
        resMap = ClientContext.getResourceMap(EditorFrame.class);
        
        //
        // Frame �𐶐�����
        // Frame �̃^�C�g����
        // ���Ҏ���(�J�i):����:����ID �ɐݒ肷��
        String karteStr = resMap.getString("karteStr");
        StringBuilder sb = new StringBuilder();
        sb.append(getPatient().getFullName());
        sb.append("(");
        String kana = getPatient().getKanaName();
        kana = kana.replace("�@", " ");
        sb.append(kana);
        sb.append(")");
        sb.append(" : ");
        sb.append(getPatient().getPatientId());
        sb.append(karteStr);
        
        windowSupport = WindowSupport.create(sb.toString());
        
        JMenuBar myMenuBar = windowSupport.getMenuBar();
        
        JFrame frame = windowSupport.getFrame();
        frame.setName("editorFrame");
        content = new JPanel(new BorderLayout());
        
        //
        // Mediator ���ύX�ɂȂ�
        //
        mediator = new ChartMediator(this);
        
        //
        //  MenuBar �𐶐�����
        //
        AbstractMenuFactory appMenu = AbstractMenuFactory.getFactory();
        appMenu.setMenuSupports(realChart.getContext().getMenuSupport(), mediator);
        appMenu.build(myMenuBar);
        mediator.registerActions(appMenu.getActionMap());
        myToolPanel = appMenu.getToolPanelProduct();
        content.add(myToolPanel, BorderLayout.NORTH);
        
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

        // Status ���
        setStatusPanel(new StatusPanel(false));
        getStatusPanel().setRightInfo(getPatient().getPatientId());
        getStatusPanel().setLeftInfo(getPatient().getFullName());
        
        if (view != null) {
            mode = EditorMode.BROWSER;
            view.setContext(EditorFrame.this);
            view.start();
            scroller = new JScrollPane(view.getUI());
            mediator.enabledAction(GUIConst.ACTION_NEW_DOCUMENT, false);

        } else if (editor != null) {
            mode = EditorMode.EDITOR;
            editor.setContext(EditorFrame.this);
            editor.initialize();
            editor.start();
            scroller = new JScrollPane(editor.getUI());
            mediator.enabledAction(GUIConst.ACTION_NEW_KARTE, false);
            mediator.enabledAction(GUIConst.ACTION_NEW_DOCUMENT, false);
        }

        content.add(scroller, BorderLayout.CENTER);
        frame.getContentPane().setLayout(new BorderLayout(0, 7));
        frame.getContentPane().add(content, BorderLayout.CENTER);
        frame.getContentPane().add((JPanel) statusPanel, BorderLayout.SOUTH);
        resMap.injectComponents(frame);
        
        
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                processWindowClosing();
            }
        });
        
        blockGlass = new BlockGlass();
        frame.setGlassPane(blockGlass);
        
        // Frame �̑傫�����X�g���[�W���烍�[�h����
        Rectangle bounds = null;
        ApplicationContext appCtx = ClientContext.getApplicationContext();
        try {
            bounds = (Rectangle) appCtx.getLocalStorage().load("editorFrameBounds.xml");

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (bounds == null) {
            int x = resMap.getInteger("frameX").intValue();
            int y = resMap.getInteger("frameY").intValue();
            int width = resMap.getInteger("frameWidth").intValue();
            int height = resMap.getInteger("frameHeight").intValue();
            bounds = new Rectangle(x, y, width, height);
        }
        frame.setBounds(bounds);
        windowSupport.getFrame().setVisible(true);
        
        Runnable awt = new Runnable() {      
            @Override
            public void run() {
                if (view != null) {
                    view.getUI().scrollRectToVisible(new Rectangle(0,0,view.getUI().getWidth(), 50));
                } else if (editor != null) {
                    editor.getUI().scrollRectToVisible(new Rectangle(0,0,editor.getUI().getWidth(), 50));
                }
            }
        };
        EventQueue.invokeLater(awt);
    }
    
    /**
     * �v���O�������I������B
     */
    @Override
    public void stop() {
        mediator.dispose();
        allEditorFrames.remove(this);
        try {
            ClientContext.getLocalStorage().save(getFrame().getBounds(), "editorFrameBounds.xml");
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
            // Editor Frame �̎��A
            // �V�K�J���e�ƃh�L�������g�͕s�Ƃ���
            mediator.enabledAction(GUIConst.ACTION_NEW_KARTE, false);
            mediator.enabledAction(GUIConst.ACTION_NEW_DOCUMENT, false);
            mode = EditorMode.EDITOR;
            content.remove(scroller);
            scroller = new JScrollPane(editor.getUI());
            content.add(scroller, BorderLayout.CENTER);
            getFrame().validate();
        }
    }
    
    /**
     * �V�K�J���e���쐬����B
     */    
    public void newKarte() {
        
        // �V�K�J���e�쐬�_�C�A���O��\�����p�����[�^�𓾂�
        String docType = view.getModel().getDocInfo().getDocType();
        
        final ChartImpl chart = (ChartImpl) realChart;
        String dept = chart.getPatientVisit().getDepartment();
        String deptCode = chart.getPatientVisit().getDepartmentCode();
        String insuranceUid = chart.getPatientVisit().getInsuranceUid();
        
        NewKarteParams params = null;
        Preferences prefs = Project.getPreferences();
        
        if (prefs.getBoolean(Project.KARTE_SHOW_CONFIRM_AT_NEW, true)) {
            
            params = chart.getNewKarteParams(docType,Chart.NewKarteOption.EDITOR_COPY_NEW, getFrame(), dept, deptCode, insuranceUid);
            
        } else {
            //
            // �蓮�Ńp�����[�^��ݒ肷��
            //
            params = new NewKarteParams(Chart.NewKarteOption.EDITOR_COPY_NEW);
            params.setDocType(docType);
            params.setDepartment(dept);
            params.setDepartmentCode(deptCode);
            
            PVTHealthInsuranceModel[] ins = chart.getHealthInsurances();
            params.setPVTHealthInsurance(ins[0]);
            
            int cMode = prefs.getInt(Project.KARTE_CREATE_MODE, 0);
            if (cMode == 0) {
                params.setCreateMode(Chart.NewKarteMode.EMPTY_NEW);
            } else if (cMode == 1) {
                params.setCreateMode(Chart.NewKarteMode.APPLY_RP);
            } else if (cMode == 2) {
                params.setCreateMode(Chart.NewKarteMode.ALL_COPY);
            }
        }
        
        if (params == null) {
            return;
        }
        
        // �ҏW�p�̃��f���𓾂�
        DocumentModel editModel = null;
        
        if (params.getCreateMode() == Chart.NewKarteMode.EMPTY_NEW) {
            editModel = chart.getKarteModelToEdit(params);
        } else {
            editModel = chart.getKarteModelToEdit(view.getModel(), params);
        }
        
        final DocumentModel theModel = editModel;
        
        Runnable r = new Runnable() {
            
            @Override
            public void run() {
                
                editor = chart.createEditor();
                editor.setModel(theModel);
                editor.setEditable(true);
                editor.setContext(EditorFrame.this);
                editor.setMode(KarteEditor.DOUBLE_MODE);
                
                Runnable awt = new Runnable() {
                    @Override
                    public void run() {
                        editor.initialize();
                        editor.start();
                        replaceView();
                    }
                };
                
                EventQueue.invokeLater(awt);
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
            
            @Override
            public void run() {
                
                ChartImpl chart = (ChartImpl)realChart;
                DocumentModel editModel = chart.getKarteModelToEdit(view.getModel());
                editor = chart.createEditor();
                editor.setModel(editModel);
                editor.setEditable(true);
                editor.setContext(EditorFrame.this);
                editor.setModify(true);
                String docType = editModel.getDocInfo().getDocType();
                int mode = docType.equals(IInfoModel.DOCTYPE_KARTE) ? KarteEditor.DOUBLE_MODE : KarteEditor.SINGLE_MODE;
                editor.setMode(mode);
                
                Runnable awt = new Runnable() {
                    @Override
                    public void run() {
                        editor.initialize();
                        editor.start();
                        replaceView();
                    }
                };
                
                EventQueue.invokeLater(awt);
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
    @Override
    public void close() {
        
        if (mode == EditorMode.EDITOR) {
            
            if (editor.isDirty()) {
                
                String save = resMap.getString("unsavedtask.saveText"); //"�ۑ�";
                String discard = resMap.getString("unsavedtask.discardText"); //"�j��";
                String question = resMap.getString("unsavedtask.question"); // ���ۑ��̃h�L�������g������܂��B�ۑ����܂��� ?
                String title = resMap.getString("unsavedtask.title"); // ���ۑ�����
                String cancelText =  (String) UIManager.get("OptionPane.cancelButtonText");
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
