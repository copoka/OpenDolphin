package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.PVTHealthInsuranceModel;
import open.dolphin.project.Project;

/**
 * DocumentViewer
 *
 * @author Minagawa,Kazushi
 *
 */
public class DocumentViewer extends DefaultChartDocument {
    
    /** Busy �v���p�e�B�� */
    public static final String BUSY_PROP = "busyProp";
    
    /** �X�V��\������ */
    private static final String TITLE_UPDATE = "�X�V";
    
    // ���̃A�v���P�[�V�����͕��������𕡐��I�����邱�Ƃ��ł���
    // ���̃��X�g�͂���ɑΉ����� KarteViewer(2���J���e)��ێ����Ă���
    // ���̃��X�g�̓��e�iKarteViewer)���ꖇ�̃p�l���ɕ��ׂĕ\�������
    private List<KarteViewer> karteList;
    
    // ��L�p�l�����Ń}�E�X�őI������Ă���J���e(karteViewer)
    // �O�񏈕���K�p�����V�K�J���e�͂��̑I�����ꂽ�J���e�����ɂȂ�
    private KarteViewer selectedKarte; // �I������Ă��� karteViewer
    
    /** busy �v���p�e�B */
    private boolean busy;
    
    /** �����T�|�[�g */
    private PropertyChangeSupport boundSupport;
    
    /** �������������ŕ\������ꍇ�� true */
    private boolean ascending;
    
    /** �����̏C��������\������ꍇ�� true */
    private boolean showModified;
    
    /** Scroller  */
    private JScrollPane scroller;
    
    /** ���̃N���X�̏�ԃ}�l�[�W�� */
    private StateMgr stateMgr;
    
    /** Timer */
    private Timer taskTimer;
    
    private ArrayList<KarteViewer> removed;
    
   
    /**
     * DocumentViewer�I�u�W�F�N�g�𐶐�����B
     */
    public DocumentViewer() {
    }
    
    /**
     * �������X�i��ǉ�����B
     * @param prop �����v���p�e�B��
     * @param l ���X�i
     */
    public void addPropertyChangeListener(String prop, PropertyChangeListener l) {
        if (boundSupport == null) {
            boundSupport = new PropertyChangeSupport(this);
        }
        boundSupport.addPropertyChangeListener(prop, l);
    }
    
    /**
     * �������X�i���폜����B
     * @param prop �����v���p�e�B��
     * @param l ���X�i
     */
    public void removePropertyChangeListener(String prop,PropertyChangeListener l) {
        if (boundSupport == null) {
            boundSupport = new PropertyChangeSupport(this);
        }
        boundSupport.removePropertyChangeListener(prop, l);
    }
    
    /**
     * busy ���ǂ�����Ԃ��B
     * @return busy �̎� true
     */
    public boolean isBusy() {
        return busy;
    }
    
    /**
     * busy ��Ԃ�ݒ肷��B
     * @param busy busy �̎� true
     */
    public void setBusy(boolean busy) {
        boolean old = this.busy;
        this.busy = busy;
        boundSupport.firePropertyChange(BUSY_PROP, old, this.busy);
    }
    
    /**
     * GUI�R���|�[�l���g������������B
     */
    public void initialize() {
        // StateMgr�𐶐����ŏ��� NO_KARTE State�ɂ���
        stateMgr = new StateMgr();
        initComponent();
        connect();
    }
    
    /**
     * �v���O�������J�n����B
     */
    public void start() {
        enter();
    }
    
    public void stop() {
        if (karteList != null) {
            for (KarteViewer karte : karteList) {
                karte.stop();
            }
            karteList.clear();
        }
    }
    
    /**
     * �^�u�̐؂�ւ��ɃR�[������A���݂�StateMgr�Ń��j���[�𐧌䂷��B
     */
    public void enter() {
        super.enter();
        stateMgr.controlMenu();
    }
    
    /**
     * �I������Ă���KarteViwer��Ԃ��B
     * @return �I������Ă���KarteViwer
     */
    public KarteViewer getSelectedKarte() {
        return selectedKarte;
    }
    
    /**
     * �}�E�X�N���b�N(�I��)���ꂽKarteViwer��selectedKarte�ɐݒ肷��B
     * ���̃J���e���I������Ă���ꍇ�͂������������B
     * StateMgr�� Haskarte State �ɂ���B
     * @param view �I�����ꂽKarteViwer
     */
    public void setSelectedKarte(KarteViewer view) {
        KarteViewer old = selectedKarte;
        selectedKarte = view;
        //
        // ���̃J���e���I������Ă���ꍇ�͂������������
        //
        if (selectedKarte != old) {
            if (selectedKarte != null) {
                for (KarteViewer karte : karteList) {
                    karte.setSelected(false);
                }
                selectedKarte.setSelected(true);
                stateMgr.setHasKarte();
                
            } else {
                // null 
                stateMgr.setNoKarte();
            }
        }
    }
    
    /**
     * �V�K�J���e�쐬�̌��ɂȂ�J���e��Ԃ��B
     * @return �쐬�̌��ɂȂ�J���e
     */
    private KarteViewer getBaseKarte() {
        KarteViewer ret = getSelectedKarte();
        if (ret == null) {
            if (karteList != null && karteList.size() > 0) {
                ret = ascending ? karteList.get(karteList.size() - 1) : karteList.get(0);
            }
        }
        return ret;
    }
    
    /**
     * GUI�R���|�[�l���g������������B
     */
    private void initComponent() {
        scroller = new JScrollPane();
        JPanel myPanel = getUI();
        myPanel.setLayout(new BorderLayout());
        myPanel.add(scroller, BorderLayout.CENTER);
        karteList = new ArrayList<KarteViewer>(1);
    }
    
    public void historyPeriodChanged() {
        if (karteList != null) {
            karteList.clear();
        }
        scroller.setViewportView(null);
        setSelectedKarte(null);
        enter();
        getContext().showDocument(0);
    }
    
    public void documentSelectionChanged(PropertyChangeEvent e) {
        DocInfoModel[] selectedHistoroes = (DocInfoModel[]) e.getNewValue();
        if (selectedHistoroes != null && selectedHistoroes.length > 0) {
            getContext().showDocument(0);
            createAndShowKarteViewers(selectedHistoroes);
        }
    }
    
    /**
     * GUI�R���|�[�l���g�Ƀ��X�i��ݒ肷��B
     *
     */
    private void connect() {
        
        //
        // ���������̒��o���Ԃ��X�V���ꂽ�ꍇ�ɒʒm���󂯂�
        // karteList ��clear�A�I������Ă���karteViewer�������AsateMgr��NoKarte��Ԃɐݒ肷��
        //
        getContext().getDocumentHistory().addPropertyChangeListener(DocumentHistory.HITORY_UPDATED, 
                (PropertyChangeListener) EventHandler.create(PropertyChangeListener.class, this, "historyPeriodChanged"));
        
        //
        // ���������e�[�u���őI���̕ω����������ꍇ�ɒʒm���󂯂�
        //
        getContext().getDocumentHistory().addPropertyChangeListener(DocumentHistory.SELECTED_HISTORIES, 
                (PropertyChangeListener) EventHandler.create(PropertyChangeListener.class, this, "documentSelectionChanged", ""));
        
        // DocHistory �� busy prop ���X�i�ɂ���
        // ���̐ݒ�͕����擾���ɗ���I���������Ȃ����߂ɍs��
        addPropertyChangeListener(BUSY_PROP, getContext().getDocumentHistory());
        
        // ���������ɏ����^�~���A�C������\���̐ݒ������
        // ���̒l�̏����l�̓f�t�H���l�ł���A�X�̃h�L�������g�i��ʁj�P�ʂɃ��j���[�ŕύX�ł���B�i�K�p�����̂͌X�̃h�L�������g�̂݁j
        // �f�t�H���g�l�̐ݒ�͊��ݒ�ōs���B
        ascending = getContext().getDocumentHistory().isAscending();
        showModified = getContext().getDocumentHistory().isShowModified();
    }
    
    /**
     * KarteViewer�𐶐����\������B
     *
     * @param selectedHistories �I�����ꂽ������� DocInfo �z��
     */
    private void createAndShowKarteViewers(DocInfoModel[] selectedHistories) {
        
        if (selectedHistories == null || selectedHistories.length == 0) {
            return;
        }
        
        // ���݂̃��X�g�Ɣ�r���A�V���ɒǉ����ꂽ���́A�폜���ꂽ���̂ɕ�����
        final ArrayList<DocInfoModel> added = new ArrayList<DocInfoModel>(1); // �ǉ����ꂽ����
        if (removed == null) {
            removed = new ArrayList<KarteViewer>(1); // �I������������Ă������
        } else {
            removed.clear();
        }
        
        // �ǉ����ꂽ���̂ƑI�����������ꂽ���̂ɕ�����
        
        // 1. �I�����X�g�ɂ����� ���݂� karteList �ɂȂ����̂͒ǉ�����
        for (DocInfoModel selectedDocInfo : selectedHistories) {
            boolean found = false;
            for (KarteViewer viewer : karteList) {
                if (viewer.getModel().getDocInfo().equals(selectedDocInfo)) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                added.add(selectedDocInfo);
            }
        }
        
        // 2 karteList �ɂ����đI�����X�g�ɂȂ����̂�karteList����폜����
        for (KarteViewer viewer : karteList) {
            boolean found = false;
            for (DocInfoModel selectedDocInfo : selectedHistories) {
                if (viewer.getModel().getDocInfo().equals(selectedDocInfo)) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                removed.add(viewer);
            }
        }
        
        // �������ꂽ���̂�����΂�������X�g�����菜��
        if (removed != null && removed.size() > 0) {
            for (KarteViewer karte : removed) {
                karteList.remove(karte);
                //karte.stop();
                //karte = null;
            }
        }
        
        // �ǉ����ꂽ���̂��f�[�^�x�[�X���猟������
        if (added == null || added.size() == 0) {
            
            Preferences prefs = Project.getPreferences();
            boolean vsc = prefs.getBoolean(Project.KARTE_SCROLL_DIRECTION, true);
            JPanel panel = new JPanel();
            if (vsc) {
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            } else {
                panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            }
            //
            for (KarteViewer view : karteList) {
                if (!vsc) {
                    view.getUI().setPreferredSize(new Dimension(692, 2100));
                }
                panel.add(view.getUI());     
            }
            
            showKartePanel(panel);
            
            return;
        }
        
        //final DocInfoModel[] docInfos = new DocInfoModel[added.size()];
        //added.toArray(docInfos);
        
        // �擾���镶��ID(PK)�𐶐���
        List<Long> docId = new ArrayList<Long>(added.size());
        for (DocInfoModel bean : added) {
            docId.add(new Long(bean.getDocPk()));
        }
        
        // �f�[�^�x�[�X���������AkarteList�։�����
        final IStatusPanel statusPanel = getContext().getStatusPanel();
        int maxEstimation = ClientContext.getInt("task.default.maxEstimation");
        int delay = ClientContext.getInt("task.default.delay");
        final DocumentDelegater ddl = new DocumentDelegater();
        
        final KarteTask worker = new KarteTask(docId, ddl, maxEstimation/delay);
        
        taskTimer = new javax.swing.Timer(delay, new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                worker.getCurrent();
                statusPanel.setMessage(worker.getMessage());
                
                if (worker.isDone()) {
                    
                    statusPanel.stop();
                    taskTimer.stop();
                    
                    if (ddl.isNoError()) {
                        // �����ł� KarteModel ���擾����
                        List<DocumentModel> models = worker.getKarteModel();
                        // ���̃��\�b�h�ŕ\������
                        addKarteViewer(models, added);
                        
                    } else {
                        warning(ClientContext.getString("docHistory.title"), ddl.getErrorMessage());
                    }
                    setBusy(false);
                    
                } else if (worker.isTimeOver()) {
                    statusPanel.stop();
                    taskTimer.stop();
                    JFrame parent = getContext().getFrame();
                    String title = ClientContext.getString("docHistory.title");
                    new TimeoutWarning(parent, title, null).start();
                    setBusy(false);
                }
            }
        });
        setBusy(true);
        worker.start();
        statusPanel.start("");
        taskTimer.start();
    }
    
    /**
     * �f�[�^�x�[�X�Ō������� KarteModel�� Viewer �ŕ\������B
     *
     * @param models KarteModel
     * @param docInfos DocInfo
     */
    @SuppressWarnings("unchecked")
    private void addKarteViewer(List<DocumentModel> models, List<DocInfoModel> docInfos) {
        
        if (models != null) {
            
            int index = 0;
            for (DocumentModel karteModel : models) {
                
                //System.out.println("Karte PK = " + karteModel.getId());
                karteModel.setDocInfo(docInfos.get(index++)); // ?
                
                // KarteViewer(2���J���e)�𐶐�����
                final KarteViewer karteViewer = new KarteViewer();
                karteViewer.setContext(getContext());
                karteViewer.setModel(karteModel);
                karteViewer.setAvoidEnter(true); // ?
                
                // ���̃R�[���Ń��f���̃����_�����O���J�n�����
                karteViewer.start();
                //System.out.println("Karte viwer statred");
                
                // MouseListener �𐶐����� KarteViewer �� Pane �ɃA�^�b�`����
                // ����Ń_�u���N���b�N���ꂽ�J���e��ʉ�ʂŕ\������
                final MouseListener ml = new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        int cnt = e.getClickCount();
                        if (cnt == 2) {
                            // �I������ Karte �� EditoFrame �ŊJ��
                            setSelectedKarte(karteViewer);
                            openKarte();
                        } else if (cnt == 1) {
                            setSelectedKarte(karteViewer);
                        }
                    }
                };
                // MouseListener �� JTextPane �֓o�^����
                karteViewer.getSOAPane().getTextPane().addMouseListener(ml);
                karteViewer.getPPane().getTextPane().addMouseListener(ml);
                
                karteList.add(karteViewer);
            }
            // ���Ԏ��Ń\�[�g�Aview�֒ʒm�A�I������������
            if (ascending) {
                Collections.sort(karteList);
            } else {
                Collections.sort(karteList, Collections.reverseOrder());
            }
            
            //
            // �I������
            //
            if (karteList.size() > 0) {
                if (ascending) {
                    setSelectedKarte(karteList.get(karteList.size() - 1));
                } else {
                    setSelectedKarte(karteList.get(0));
                }
            }
        }

        Preferences prefs = Project.getPreferences();
        boolean vsc = prefs.getBoolean(Project.KARTE_SCROLL_DIRECTION, true);
        JPanel panel = new JPanel();
        
        if (vsc) {
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        } else {
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        }
        //
        for (KarteViewer view : karteList) {
            if (!vsc) {
                view.getUI().setPreferredSize(new Dimension(692, 2100));
            }
            panel.add(view.getUI());     
        }
        
        showKartePanel(panel);
    }
    
    
    private void showKartePanel(final JPanel panel) {
        
        Runnable awt = new Runnable() {
                    
            public void run() {
                //
                // �R���|�[�l���g�����̉�����
                //
                scroller.setViewportView(panel);
                
                if (removed != null) {
                    for (KarteViewer karte : removed) {
                        karte.stop();
                    }
                    removed.clear();
                }
            }
        };
        SwingUtilities.invokeLater(awt);
    }
    
    /*public boolean copyStamp() {
            return selectedKarte != null ? selectedKarte.copyStamp() : false;
    }*/
    
    //////////////// ���j���[���\�b�h /////////////
    
    /**
     * �V�K�J���e���쐬����B
     */
    public void newKarte() {
        
        ChartPlugin chart = (ChartPlugin) getContext();
        String dept = getContext().getPatientVisit().getDepartment();
        String deptCode = getContext().getPatientVisit().getDepartmentCode();
        String insuranceUid = getContext().getPatientVisit().getInsuranceUid();
        
        
        // �V�K�J���e�쐬���̃x�[�X�ɂȂ�J���e�����邩
        KarteViewer base = getBaseKarte();
        IChart.NewKarteOption option = base != null
                ? IChart.NewKarteOption.BROWSER_COPY_NEW
                : IChart.NewKarteOption.BROWSER_NEW;
        
        //
        // �V�K�J���e�쐬���Ɋm�F�_�C�A���O��\�����邩�ǂ���
        //
        NewKarteParams params = null;
        Preferences prefs = Project.getPreferences();
        
        if (prefs.getBoolean(Project.KARTE_SHOW_CONFIRM_AT_NEW, true)) {
        
            // �V�K�J���e�_�C�A���O�փp�����[�^��n���A�R�s�[�V�K�̃I�v�V�����𐧌䂷��
            params = chart.getNewKarteParams(option, getContext().getFrame(), dept, deptCode, insuranceUid);
            
        } else {
            // �ی��A�쐬���[�h�A�z�u���@���蓮�Őݒ肷��
            params = new NewKarteParams(option);
            params.setDepartment(dept);
            params.setDepartmentCode(deptCode);
            
            //
            // �ی�
            //
            PVTHealthInsuranceModel[] ins = chart.getHealthInsurances();
            params.setPVTHealthInsurance(ins[0]);
            if (insuranceUid != null) {
                int index = 0;
                for (int i = 0; i < ins.length; i++) {
                    if (ins[i].getGUID() != null) {
                        if (insuranceUid.equals(ins[i].getGUID())) {
                            params.setPVTHealthInsurance(ins[i]);
                            break;
                        }
                    }
                }
            }
            
            //
            // �쐬���[�h
            //
            switch (option) {
                
                case BROWSER_NEW:
                    params.setCreateMode(IChart.NewKarteMode.EMPTY_NEW);
                    break;
                    
                case BROWSER_COPY_NEW:
                    int cMode = prefs.getInt(Project.KARTE_CREATE_MODE, 0);
                    if (cMode == 0) {
                        params.setCreateMode(IChart.NewKarteMode.EMPTY_NEW);
                    } else if (cMode == 1) {
                        params.setCreateMode(IChart.NewKarteMode.APPLY_RP);
                    } else if (cMode == 2) {
                        params.setCreateMode(IChart.NewKarteMode.ALL_COPY);
                    }
                    break;
            }
            
            //
            // �z�u���@
            //
            params.setOpenFrame(prefs.getBoolean(Project.KARTE_PLACE_MODE, true));
            
        }
        
        // �L�����Z�������ꍇ�̓��^�[������
        if (params == null) {
            return;
        }
        
        // Base�ɂȂ�J���e�����邩�ǂ����Ń��f���̐������قȂ�
        DocumentModel editModel = null;
        if (params.getCreateMode() == IChart.NewKarteMode.EMPTY_NEW) {
            editModel = chart.getKarteModelToEdit(params);
        } else {
            editModel = chart.getKarteModelToEdit(base.getModel(), params);
        }
        final KarteEditor editor = chart.createEditor();
        editor.setModel(editModel);
        editor.setEditable(true);
        
        if (params.isOpenFrame()) {
            startEditorFrame(editor);
        } else {
            editor.setContext(chart);
            editor.initialize();
            editor.start();
            chart.addChartDocument(editor, params);
        }
    }
    
    /**
     * �J���e���C������B
     */
    public void modifyKarte() {
        
        if (getBaseKarte() == null) {
            return;
        }
        final ChartPlugin chart = (ChartPlugin) getContext();
        String dept = getContext().getPatientVisit().getDepartment();
        String deptCode = getContext().getPatientVisit().getDepartmentCode();
        String insuranceUid = getContext().getPatientVisit().getInsuranceUid();
        
        NewKarteParams params = null;
        Preferences prefs = Project.getPreferences();
        
        if (prefs.getBoolean(Project.KARTE_SHOW_CONFIRM_AT_NEW, true)) {
            params = chart.getNewKarteParams(IChart.NewKarteOption.BROWSER_MODIFY, getContext().getFrame(), dept, deptCode, insuranceUid);
            
        } else {
            params = new NewKarteParams(IChart.NewKarteOption.BROWSER_MODIFY);
            params.setDepartment(dept); 
            params.setDepartmentCode(deptCode); 
            //
            // �z�u���@
            //
            params.setOpenFrame(prefs.getBoolean(Project.KARTE_PLACE_MODE, true));
        }
        
        if (params == null) {
            return;
        }
        
        DocumentModel editModel = chart.getKarteModelToEdit(getBaseKarte().getModel());
        KarteEditor editor = chart.createEditor();
        editor.setModel(editModel);
        editor.setEditable(true);
        editor.setModify(true);
        
        if (params.isOpenFrame()) {
            startEditorFrame(editor);
        } else {
            editor.setContext(chart);
            editor.initialize();
            editor.start();
            chart.addChartDocument(editor, TITLE_UPDATE);
        }
    }
    
    /**
     * �J���e���������B
     */
    public void print() {
        KarteViewer view = getSelectedKarte();
        if (view != null) {
            view.print();
        }
    }
    
    private void startEditorFrame(KarteEditor editor) {
        EditorFrame editorFrame = new EditorFrame();
        editorFrame.setChart(getContext());
        editorFrame.setKarteEditor(editor);
        editorFrame.start();
    }
    
    /**
     * �����\���ɂ���B
     */
    public void ascending() {
        ascending = true;
        getContext().getDocumentHistory().setAscending(ascending);
    }
    
    /**
     * �~���\���ɂ���B
     */
    public void descending() {
        ascending = false;
        getContext().getDocumentHistory().setAscending(ascending);
    }
    
    /**
     * �C�������̕\�����[�h�ɂ���B
     */
    public void showModified() {
        showModified = !showModified;
        getContext().getDocumentHistory().setShowModified(showModified);
    }
    
    /**
     * karteList ���Ń_�u���N���b�N���ꂽ�J���e�i�����j�� EditorFrame �ŊJ���B
     */
    public void openKarte() {
        if (getSelectedKarte() != null) {
            EditorFrame editorFrame = new EditorFrame();
            editorFrame.setChart(getContext());
            KarteViewer view = new KarteViewer();
            view.setModel(getSelectedKarte().getModel());
            editorFrame.setKarteViewer(view);
            editorFrame.start();
        }
    }
    
    /**
     * �\���I������Ă���J���e��_���폜����B
     * ���҂��ԈႦ���ꍇ���ɗ����ɕ\������Ȃ��悤�ɂ��邽�߁B
     */
    public void delete() {
        
        // �Ώۂ̃J���e�𓾂�
        KarteViewer delete = getBaseKarte();
        if (delete == null) {
            return;
        }
        
        // Dialog ��\�������R�����߂�
        String message = "���̃h�L�������g���폜���܂��� ?   ";
        final JCheckBox box1 = new JCheckBox("�쐬�~�X");
        final JCheckBox box2 = new JCheckBox("�f�@�L�����Z��");
        final JCheckBox box3 = new JCheckBox("���̑�");
        box1.setSelected(true);
        
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (box1.isSelected() || box2.isSelected()) {
                    return;
                } else if (!box3.isSelected()){
                    box3.setSelected(true);
                }
            }
        };
        
        box1.addActionListener(al);
        box2.addActionListener(al);
        box3.addActionListener(al);
        
        Object[] msg = new Object[5];
        msg[0] = message;
        msg[1] = box1;
        msg[2] = box2;
        msg[3] = box3;
        msg[4] = new JLabel(" ");
        String deleteText = "�폜����";
        String cancelText = (String) UIManager.get("OptionPane.cancelButtonText");
        
        int option = JOptionPane.showOptionDialog(
                    this.getUI(),
                    msg,
                    ClientContext.getFrameTitle("�h�L�������g�폜"),
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    new String[] { deleteText, cancelText },
                    cancelText);
        
        System.out.println(option);
        
        // �L�����Z���̏ꍇ�̓��^�[������
        if (option != 0) {
            return;
        }
        
        //
        // �폜���� status = 'D'
        //
        final long deletePk = delete.getModel().getId();
        final DocumentDelegater ddl = new DocumentDelegater();
        
        final IStatusPanel statusPanel = getContext().getStatusPanel();
        int maxEstimation = ClientContext.getInt("task.default.maxEstimation");
        int delay = ClientContext.getInt("task.default.delay");
        
        final DeleteTask worker = new DeleteTask(deletePk, ddl, maxEstimation/delay);
        
        taskTimer = new javax.swing.Timer(delay, new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                worker.getCurrent();
                statusPanel.setMessage(worker.getMessage());
                
                if (worker.isDone()) {
                    
                    statusPanel.stop();
                    taskTimer.stop();
                    
                    if (ddl.isNoError()) {
                        //
                        // ���������̍X�V��ʒm����
                        //
                        getContext().getDocumentHistory().getDocumentHistory();
                        
                    } else {
                        //
                        // �G���[�������Ă���ꍇ�͌x������
                        //
                        warning(ClientContext.getString("�h�L�������g�폜"), ddl.getErrorMessage());
                    }
                    setBusy(false);
                    
                } else if (worker.isTimeOver()) {
                    statusPanel.stop();
                    taskTimer.stop();
                    JFrame parent = getContext().getFrame();
                    String title = ClientContext.getString("�h�L�������g�폜");
                    new TimeoutWarning(parent, title, null).start();
                    setBusy(false);
                }
            }
        });
        setBusy(true);
        worker.start();
        statusPanel.start("");
        taskTimer.start();
 
        // ������ where �߂�ύX����K�v������
        // where and (status='F' or status='T')
        // where and status!='D'
    }
    
    /////////////////////////////////////////////////////////
    
    class KarteTask extends AbstractInfiniteTask {
        
        private List<DocumentModel> model;
        private DocumentDelegater ddl;
        private List<Long> docId;
        
        public KarteTask(List<Long> docId, DocumentDelegater ddl, int taskLength) {
            this.docId = docId;
            this.ddl = ddl;
            setTaskLength(taskLength);
        }
        
        protected List<DocumentModel> getKarteModel() {
            return model;
        }
        
        protected void doTask() {
            model = ddl.getDocuments(docId);
            setDone(true);
        }
    }
    
        
    class DeleteTask extends AbstractInfiniteTask {
        
        private DocumentDelegater ddl;
        private long docPk;
        
        public DeleteTask(long docPk, DocumentDelegater ddl, int taskLength) {
            this.docPk = docPk;
            this.ddl = ddl;
            setTaskLength(taskLength);
        }
        
        protected void doTask() {
            ddl.deleteDocument(docPk);
            setDone(true);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    protected abstract class BrowserState {
        
        public BrowserState() {
        }
        
        public abstract void controlMenu();
    }
    
    /**
     * �\������J���e���Ȃ���Ԃ�\���B
     */
    protected final class NoKarteState extends BrowserState {
        
        public NoKarteState() {
        }
        
        public void controlMenu() {
            // �X�[�p�[�N���X�Ƃ̍����݂̂��݂̂𐧌䂷��
            ChartMediator mediator = getContext().getChartMediator();
            boolean canEdit = isReadOnly() ? false : true;
            mediator.getAction(GUIConst.ACTION_NEW_KARTE).setEnabled(canEdit); // �V�K�J���e
            mediator.getAction(GUIConst.ACTION_ASCENDING).setEnabled(true);
            mediator.getAction(GUIConst.ACTION_DESCENDING).setEnabled(true);
            mediator.getAction(GUIConst.ACTION_SHOW_MODIFIED).setEnabled(true);
        }
    }
    
    /**
     * �J���e���\������Ă����Ԃ�\���B
     */
    protected final class HasKarteState extends BrowserState {
        
        public HasKarteState() {
        }
        
        public void controlMenu() {
            // �X�[�p�[�N���X�Ƃ̍����݂̂��݂̂𐧌䂷��
            ChartMediator mediator = getContext().getChartMediator();
            
            //
            // �V�K�J���e���\�ȃP�[�X ���ۑ��łȂ����Ƃ�ǉ�
            //
            boolean canEdit = isReadOnly() ? false : true;
            boolean tmpKarte = false;
            KarteViewer base = getBaseKarte();
            if (base != null) {
                String state = base.getModel().getDocInfo().getStatus();
                if (state.equals(IInfoModel.STATUS_TMP)) {
                    tmpKarte = true;
                }
            }
            boolean newOk = canEdit && (!tmpKarte) ? true : false;
            mediator.getAction(GUIConst.ACTION_NEW_KARTE).setEnabled(newOk);  // �V�K�J���e
            mediator.getAction(GUIConst.ACTION_MODIFY_KARTE).setEnabled(canEdit); // �V�K�J���e
            mediator.getAction(GUIConst.ACTION_DELETE_KARTE).setEnabled(canEdit); // �폜
            mediator.getAction(GUIConst.ACTION_PRINT).setEnabled(true); // ���
            mediator.getAction(GUIConst.ACTION_ASCENDING).setEnabled(true);
            mediator.getAction(GUIConst.ACTION_DESCENDING).setEnabled(true);
            mediator.getAction(GUIConst.ACTION_SHOW_MODIFIED).setEnabled(true);
        }
    }
    
    protected final class StateMgr {
        
        private BrowserState noKarteState = new NoKarteState();
        private BrowserState hasKarteState = new HasKarteState();
        private BrowserState currentState;
        
        public StateMgr() {
            currentState = noKarteState;
        }
        
        public void setNoKarte() {
            currentState = noKarteState;
            currentState.controlMenu();
        }
        
        public void setHasKarte() {
            if (currentState != hasKarteState) {
                currentState = hasKarteState;
            }
            currentState.controlMenu();
        }
        
        public void controlMenu() {
            currentState.controlMenu();
        }
    }
}
