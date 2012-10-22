package open.dolphin.client;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.helper.DBTask;
import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.project.Project;

/**
 * DocumentViewer
 *
 * @author Minagawa,Kazushi
 *
 */
public class KarteDocumentViewer extends AbstractChartDocument implements DocumentViewer {

    // Busy �v���p�e�B��
    public static final String BUSY_PROP = "busyProp";
    // �X�V��\������ 
    private static final String TITLE_UPDATE = "�X�V";
    private static final String TITLE = "�Q ��";
    // ���̃A�v���P�[�V�����͕��������𕡐��I�����邱�Ƃ��ł���
    // ���̃��X�g�͂���ɑΉ����� KarteViewer(2���J���e)��ێ����Ă���
    // ���̃��X�g�̓��e�iKarteViewer)���ꖇ�̃p�l���ɕ��ׂĕ\�������
    private List<KarteViewer> karteList;
    // ��L�p�l�����Ń}�E�X�őI������Ă���J���e(karteViewer)
    // �O�񏈕���K�p�����V�K�J���e�͂��̑I�����ꂽ�J���e�����ɂȂ�
    private KarteViewer selectedKarte; // �I������Ă��� karteViewer
    // busy �v���p�e�B
    private boolean busy;
    // �������������ŕ\������ꍇ�� true
    private boolean ascending;
    // �����̏C��������\������ꍇ�� true 
    private boolean showModified;
    // ���̃N���X�̏�ԃ}�l�[�W�� 
    private StateMgr stateMgr;
    // �I�����������ꂽ�J���e�̃��X�g
    private ArrayList<KarteViewer> removed;
    private JPanel scrollerPanel;

    /**
     * DocumentViewer�I�u�W�F�N�g�𐶐�����B
     */
    public KarteDocumentViewer() {
        super();
        setTitle(TITLE);
    }

    /**
     * busy ���ǂ�����Ԃ��B
     * @return busy �̎� true
     */
    public boolean isBusy() {
        return busy;
    }

    @Override
    public void start() {
        karteList = new ArrayList<KarteViewer>(1);
        connect();
        stateMgr = new StateMgr();
        enter();
    }

    @Override
    public void stop() {
        if (karteList != null) {
            for (KarteViewer karte : karteList) {
                karte.stop();
            }
            karteList.clear();
        }
    }

    @Override
    public void enter() {
        super.enter();
        stateMgr.enter();
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
                stateMgr.processCleanEvent();

            } else {
                // null 
                stateMgr.processEmptyEvent();
            }
        }
    }

    /**
     * �V�K�J���e�쐬�̌��ɂȂ�J���e��Ԃ��B
     * @return �쐬�̌��ɂȂ�J���e
     */
    public KarteViewer getBaseKarte() {
        KarteViewer ret = getSelectedKarte();
        if (ret == null) {
            if (karteList != null && karteList.size() > 0) {
                ret = ascending ? karteList.get(karteList.size() - 1) : karteList.get(0);
            }
        }
        return ret;
    }

    /**
     * ���������̒��o���Ԃ��ύX���ꂽ�ꍇ�A
     * karteList ��clear�A�I������Ă���karteViewer�������AsateMgr��NoKarte��Ԃɐݒ肷��B
     */
    public void historyPeriodChanged() {
        if (karteList != null) {
            karteList.clear();
        }
        setSelectedKarte(null);
        getContext().showDocument(0);
    }

    /**
     * GUI�R���|�[�l���g�Ƀ��X�i��ݒ肷��B
     *
     */
    private void connect() {

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
    public void showDocuments(DocInfoModel[] selectedHistories, final JScrollPane scroller) {

        if (selectedHistories == null || selectedHistories.length == 0) {
            return;
        }

        // ���݂̃��X�g�Ɣ�r���A�V���ɒǉ����ꂽ���́A�폜���ꂽ���̂ɕ�����
        ArrayList<DocInfoModel> added = new ArrayList<DocInfoModel>(1); // �ǉ����ꂽ����
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
            }
        }

        // �ǉ����ꂽ���̂��Ȃ��ꍇ
        if (added == null || added.size() == 0) {

            Preferences prefs = Project.getPreferences();
            boolean vsc = prefs.getBoolean(Project.KARTE_SCROLL_DIRECTION, true);

            if (scrollerPanel != null) {
                scrollerPanel.removeAll();
            }

            scrollerPanel = new JPanel();

            if (vsc) {
                scrollerPanel.setLayout(new BoxLayout(scrollerPanel, BoxLayout.Y_AXIS));
            } else {
                scrollerPanel.setLayout(new BoxLayout(scrollerPanel, BoxLayout.X_AXIS));
            }

            for (KarteViewer view : karteList) {
                scrollerPanel.add(view.getUI());
            }

            scroller.setViewportView(scrollerPanel);

            if (vsc) {
                showKarteListV();
            } else {
                showKarteListH();
            }

            return;
        }

        // �擾���镶��ID(PK)�𐶐���
        List<Long> docId = new ArrayList<Long>(added.size());
        for (DocInfoModel bean : added) {
            docId.add(new Long(bean.getDocPk()));
        }

        DocumentDelegater ddl = new DocumentDelegater();
        KarteTask task = new KarteTask(getContext(), docId, added, ddl, scroller);
        task.execute();
    }

    private KarteViewer createKarteViewer(DocInfoModel docInfo) {
        if (docInfo != null && docInfo.getDocType().equals(IInfoModel.DOCTYPE_S_KARTE)) {
            return new KarteViewer();
        }
        return new KarteViewer2();
    }

    /**
     * �f�[�^�x�[�X�Ō������� KarteModel�� Viewer �ŕ\������B
     *
     * @param models KarteModel
     * @param docInfos DocInfo
     */
    private void addKarteViewer(List<DocumentModel> models, List<DocInfoModel> docInfos, final JScrollPane scroller) {

        if (models != null) {

            int index = 0;
            for (DocumentModel karteModel : models) {

                karteModel.setDocInfo(docInfos.get(index++)); // ?

                // �V���O���y�тQ���p���̔�����s���AKarteViewer �𐶐�����
                final KarteViewer karteViewer = createKarteViewer(karteModel.getDocInfo());
                karteViewer.setContext(getContext());
                karteViewer.setModel(karteModel);
                karteViewer.setAvoidEnter(true);

                // ���̃R�[���Ń��f���̃����_�����O���J�n�����
                karteViewer.start();

                // 2���J���e�̏ꍇ�_�u���N���b�N���ꂽ�J���e��ʉ�ʂŕ\������
                // MouseListener �𐶐����� KarteViewer �� Pane �ɃA�^�b�`����
                if (karteModel.getDocInfo().getDocType().equals(IInfoModel.DOCTYPE_KARTE)) {
                    final MouseListener ml = new MouseAdapter() {

                        @Override
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
                    karteViewer.addMouseListener(ml);
                }

                karteList.add(karteViewer);

            }
            // ���Ԏ��Ń\�[�g�Aview�֒ʒm�A�I������������
            if (ascending) {
                Collections.sort(karteList);
            } else {
                Collections.sort(karteList, Collections.reverseOrder());
            }

            // �I������
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

        if (scrollerPanel != null) {
            scrollerPanel.removeAll();
        }

        scrollerPanel = new JPanel();
        //scrollerPanel.setVisible(false);
        
        if (vsc) {
            scrollerPanel.setLayout(new BoxLayout(scrollerPanel, BoxLayout.Y_AXIS));
        } else {
            scrollerPanel.setLayout(new BoxLayout(scrollerPanel, BoxLayout.X_AXIS));
        }

        for (KarteViewer view : karteList) {
            scrollerPanel.add(view.getUI());
        }
        
        scroller.setViewportView(scrollerPanel);

        if (vsc) {
            showKarteListV();
        } else {
            showKarteListH();
        }
    }
    
//    private void showKarteListV(final JScrollPane scroller) {
//
//        Runnable awt = new Runnable() {
//
//            public void run() {
//
//                scroller.setViewportView(scrollerPanel);
//                getContext().showDocument(0);
//                if (removed != null) {
//                    for (KarteViewer karte : removed) {
//                        karte.stop();
//                    }
//                    removed.clear();
//                }
//            }
//        };
//        EventQueue.invokeLater(awt);
//    }

    private void showKarteListV() {

        Runnable awt = new Runnable() {

            public void run() {

                if (karteList.size() > 1) {
                    int totalHeight = 0;
                    for (KarteViewer view : karteList) {
                        int w = view.panel2.getPreferredSize().width;
                        int h = view.getActualHeight() + 30;
                        totalHeight += h;
                        view.panel2.setPreferredSize(new Dimension(w, h));
                    }
                    int spWidth = scrollerPanel.getPreferredSize().width;
                    scrollerPanel.setPreferredSize(new Dimension(spWidth, totalHeight));
                }

                scrollerPanel.scrollRectToVisible(new Rectangle(0, 0, scrollerPanel.getWidth(), 100));
                //scrollerPanel.setVisible(true);
                getContext().showDocument(0);
                if (removed != null) {
                    for (KarteViewer karte : removed) {
                        karte.stop();
                    }
                    removed.clear();
                }
            }
        };
        EventQueue.invokeLater(awt);
    }

    private void showKarteListH() {

        Runnable awt = new Runnable() {

            public void run() {

                if (karteList.size() > 1) {
                    int maxHeight = 0;
                    for (KarteViewer view : karteList) {
                        int w = view.panel2.getPreferredSize().width;
                        int h = view.getActualHeight() + 20;
                        maxHeight = maxHeight >= h ? maxHeight : h;
                        view.panel2.setPreferredSize(new Dimension(w, h));
                    }
                    int spWidth = scrollerPanel.getPreferredSize().width;
                    scrollerPanel.setPreferredSize(new Dimension(spWidth, maxHeight));
                }

                scrollerPanel.scrollRectToVisible(new Rectangle(0, 0, scrollerPanel.getWidth(), 100));
                getContext().showDocument(0);
                if (removed != null) {
                    for (KarteViewer karte : removed) {
                        karte.stop();
                    }
                    removed.clear();
                }
            }
        };
        EventQueue.invokeLater(awt);
    }

    /**
     * �J���e���C������B
     */
//    public void modifyKarte() {
//        
//        if (getBaseKarte() == null) {
//            return;
//        }
//        final ChartImpl chart = (ChartImpl) getContext();
//        String dept = getContext().getPatientVisit().getDepartment();
//        String deptCode = getContext().getPatientVisit().getDepartmentCode();
//        String insuranceUid = getContext().getPatientVisit().getInsuranceUid();
//        
//        NewKarteParams params = null;
//        Preferences prefs = Project.getPreferences();
//        
//        if (prefs.getBoolean(Project.KARTE_SHOW_CONFIRM_AT_NEW, true)) {
//            params = chart.getNewKarteParams(Chart.NewKarteOption.BROWSER_MODIFY, getContext().getFrame(), dept, deptCode, insuranceUid);
//            
//        } else {
//            params = new NewKarteParams(Chart.NewKarteOption.BROWSER_MODIFY);
//            params.setDepartment(dept); 
//            params.setDepartmentCode(deptCode); 
//            //
//            // �z�u���@
//            //
//            params.setOpenFrame(prefs.getBoolean(Project.KARTE_PLACE_MODE, true));
//        }
//        
//        if (params == null) {
//            return;
//        }
//        
//        DocumentModel editModel = chart.getKarteModelToEdit(getBaseKarte().getModel());
//        KarteEditor editor = chart.createEditor();
//        editor.setModel(editModel);
//        editor.setEditable(true);
//        editor.setModify(true);
//        
//        if (params.isOpenFrame()) {
//            startEditorFrame(editor);
//        } else {
//            editor.setContext(chart);
//            editor.initialize();
//            editor.start();
//            chart.addChartDocument(editor, TITLE_UPDATE);
//        }
//    }
    /**
     * �J���e���C������B
     */
    public void modifyKarte() {

        if (getBaseKarte() == null) {
            return;
        }

        String docType = getBaseKarte().getModel().getDocInfo().getDocType();

        ChartImpl chart = (ChartImpl) getContext();
        String dept = getContext().getPatientVisit().getDepartment();
        String deptCode = getContext().getPatientVisit().getDepartmentCode();

        Preferences prefs = Project.getPreferences();

        NewKarteParams params = new NewKarteParams(Chart.NewKarteOption.BROWSER_MODIFY);
        params.setDocType(docType);
        params.setDepartment(dept);
        params.setDepartmentCode(deptCode);
        // ���̃t���O�̓J���e��ʃE�C���h�E�ŕҏW���邩�ǂ���
        params.setOpenFrame(prefs.getBoolean(Project.KARTE_PLACE_MODE, true));

        DocumentModel editModel = chart.getKarteModelToEdit(getBaseKarte().getModel());
        KarteEditor editor = chart.createEditor();
        editor.setModel(editModel);
        editor.setEditable(true);
        editor.setModify(true);
        int mode = docType.equals(IInfoModel.DOCTYPE_KARTE) ? KarteEditor.DOUBLE_MODE : KarteEditor.SINGLE_MODE;
        editor.setMode(mode);

        // Single Karte �̏ꍇ EF �����Ȃ�
        if (mode == 1) {
            params.setOpenFrame(false);
        }

        if (params.isOpenFrame()) {
            EditorFrame editorFrame = new EditorFrame();
            editorFrame.setChart(getContext());
            editorFrame.setKarteEditor(editor);
            editorFrame.start();
        } else {
            editor.setContext(chart);
            editor.initialize();
            editor.start();
            chart.addChartDocument(editor, TITLE_UPDATE);
        }
    }

    @Override
    public void print() {
        KarteViewer view = getSelectedKarte();
        if (view != null) {
            view.print();
        }
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

            // �\�����Ă��镶���^�C�v�ɉ����� Viewer ���쐬����
            DocumentModel model = getSelectedKarte().getModel();
            String docType = model.getDocInfo().getDocType();

            if (docType.equals(IInfoModel.DOCTYPE_S_KARTE)) {
                KarteViewer view = new KarteViewer();
                view.setModel(model);
                editorFrame.setKarteViewer(view);
                editorFrame.start();
            } else if (docType.equals(IInfoModel.DOCTYPE_KARTE)) {
                KarteViewer2 view = new KarteViewer2();
                view.setModel(model);
                editorFrame.setKarteViewer(view);
                editorFrame.start();
            }
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
                } else if (!box3.isSelected()) {
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
                new String[]{deleteText, cancelText},
                cancelText);

        System.out.println(option);

        // �L�����Z���̏ꍇ�̓��^�[������
        if (option != 0) {
            return;
        }

        //
        // �폜���� status = 'D'
        //
        long deletePk = delete.getModel().getId();
        DocumentDelegater ddl = new DocumentDelegater();
        DeleteTask task = new DeleteTask(getContext(), deletePk, ddl);
        task.execute();
    }

    /**
     * �������f�[�^�x�[�X����擾����^�X�N�N���X�B
     */
    class KarteTask extends DBTask<List<DocumentModel>> {

        private DocumentDelegater ddl;
        private List<Long> docId;
        private List<DocInfoModel> docInfos;
        private JScrollPane scroller;

        public KarteTask(Chart ctx, List<Long> docId, List<DocInfoModel> docInfos, DocumentDelegater ddl, JScrollPane scroller) {
            super(ctx);
            this.docId = docId;
            this.ddl = ddl;
            this.docInfos = docInfos;
            this.scroller = scroller;
        }

        @Override
        protected List<DocumentModel> doInBackground() {
            logger.debug("KarteTask doInBackground");
            List<DocumentModel> result = ddl.getDocuments(docId);
            if (ddl.isNoError()) {
                logger.debug("doInBackground noErr, return result");
                return result;
            } else {
                logger.warn("doInBackground err: " + ddl.getErrorMessage());
                return null;
            }
        }

        @Override
        protected void succeeded(List<DocumentModel> list) {
            logger.debug("KarteTask succeeded");
            if (list != null) {
                addKarteViewer(list, docInfos, scroller);
            }
        }
    }

    /**
     * �J���e�̍폜�^�X�N�N���X�B
     */
    class DeleteTask extends DBTask<Boolean> {

        private DocumentDelegater ddl;
        private long docPk;

        public DeleteTask(Chart ctx, long docPk, DocumentDelegater ddl) {
            super(ctx);
            this.docPk = docPk;
            this.ddl = ddl;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            logger.debug("DeleteTask started");
            ddl.deleteDocument(docPk);
            return new Boolean(ddl.isNoError());
        }

        @Override
        protected void succeeded(Boolean result) {
            logger.debug("DeleteTask succeeded");
            if (result.booleanValue()) {
                Chart chart = (KarteDocumentViewer.this).getContext();
                chart.getDocumentHistory().getDocumentHistory();
            } else {
                warning(ClientContext.getString("�h�L�������g�폜"), ddl.getErrorMessage());
            }
        }
    }

    /**
     * ���ۏ�ԃN���X�B
     */
    protected abstract class BrowserState {

        public BrowserState() {
        }

        public abstract void enter();
    }

    /**
     * �\������J���e���Ȃ���Ԃ�\���B
     */
    protected final class EmptyState extends BrowserState {

        public EmptyState() {
        }

        public void enter() {
            boolean canEdit = isReadOnly() ? false : true;
            getContext().enabledAction(GUIConst.ACTION_NEW_KARTE, canEdit);     // �V�K�J���e
            getContext().enabledAction(GUIConst.ACTION_NEW_DOCUMENT, canEdit);  // �V�K����
            getContext().enabledAction(GUIConst.ACTION_MODIFY_KARTE, false);    // �C��
            getContext().enabledAction(GUIConst.ACTION_DELETE, false);          // �폜
            getContext().enabledAction(GUIConst.ACTION_PRINT, false);           // ���   
            getContext().enabledAction(GUIConst.ACTION_ASCENDING, false);       // ����
            getContext().enabledAction(GUIConst.ACTION_DESCENDING, false);      // �~��
            getContext().enabledAction(GUIConst.ACTION_SHOW_MODIFIED, false);   // �C������\��
        }
    }

    /**
     * �J���e���\������Ă����Ԃ�\���B
     */
    protected final class ClaenState extends BrowserState {

        public ClaenState() {
        }

        public void enter() {

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
            getContext().enabledAction(GUIConst.ACTION_NEW_KARTE, newOk);        // �V�K�J���e
            getContext().enabledAction(GUIConst.ACTION_NEW_DOCUMENT, canEdit);   // �V�K����
            getContext().enabledAction(GUIConst.ACTION_MODIFY_KARTE, canEdit);   // �C��
            getContext().enabledAction(GUIConst.ACTION_DELETE, canEdit);         // �폜
            getContext().enabledAction(GUIConst.ACTION_PRINT, true);             // ���
            getContext().enabledAction(GUIConst.ACTION_ASCENDING, true);         // ����
            getContext().enabledAction(GUIConst.ACTION_DESCENDING, true);        // �~��
            getContext().enabledAction(GUIConst.ACTION_SHOW_MODIFIED, true);     // �C������\��
        }
    }

    /**
     * StateContext �N���X�B
     */
    protected final class StateMgr {

        private BrowserState emptyState = new EmptyState();
        private BrowserState cleanState = new ClaenState();
        private BrowserState currentState;

        public StateMgr() {
            currentState = emptyState;
        }

        public void processEmptyEvent() {
            currentState = emptyState;
            this.enter();
        }

        public void processCleanEvent() {
            currentState = cleanState;
            this.enter();
        }

        public void enter() {
            currentState.enter();
        }
    }
}
