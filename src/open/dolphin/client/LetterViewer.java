package open.dolphin.client;

import java.awt.EventQueue;
import java.awt.print.PageFormat;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.helper.DBTask;
import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.infomodel.LetterModel;
import open.dolphin.infomodel.ModelUtils;
import open.dolphin.infomodel.TouTouLetter;
import open.dolphin.project.Project;

/**
 * ���������őI�����ꂽ�Љ���\������N���X�B
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class LetterViewer extends AbstractChartDocument implements DocumentViewer {

    private StateMgr stateMgr;
    private LetterView2 view;
    private TouTouLetter model;

    @Override
    public void start() {
        stateMgr = new StateMgr();
        this.enter();
    }

    @Override
    public void stop() {
    }

    @Override
    public void enter() {
        super.enter();
        stateMgr.enter();
    }

    @Override
    public void print() {

        if (this.model == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("PDF�t�@�C�����쐬���܂���?");

        int option = JOptionPane.showOptionDialog(
                getContext().getFrame(),
                sb.toString(),
                ClientContext.getFrameTitle("�Љ����"),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"PDF�쐬", "��ʈ��", "�����"},
                "PDF�쐬");

        if (option == 0) {
            makePDF();
        } else if (option == 1) {
            PageFormat pageFormat = getContext().getContext().getPageFormat();
            String name = getContext().getPatient().getFullName();
            Panel2 panel = (Panel2) this.view;
            panel.printPanel(pageFormat, 1, false, name, 0);
        }
    }

    public void makePDF() {

        if (this.model == null) {
            return;
        }

        Runnable r = new Runnable() {

            public void run() {
                
                PDFLetterMaker pdf = new PDFLetterMaker();
                String pdfDir = Project.getPreferences().get("pdfStore", System.getProperty("user.dir"));
                pdf.setDocumentDir(pdfDir);
                pdf.setModel(model);
                final boolean result = pdf.create();
                final String fileName = pdf.getFileName();
                final String dir = pdf.getDocumentDir();

                Runnable awt = new Runnable() {

                    public void run() {
                        if (result) {
                            StringBuilder sb = new StringBuilder();
                            //String fileName = pdf.getFileName();
                            //String dir = pdf.getDocumentDir();
                            sb.append(fileName);
                            sb.append("��");
                            sb.append("\n");
                            sb.append(dir);
                            sb.append("�ɕۑ����܂����B");
                            sb.append("\n");
                            sb.append("PDF �r�����[���N����������Ă��������B");
                            JOptionPane.showMessageDialog(
                                    getContext().getFrame(),
                                    sb.toString(),
                                    ClientContext.getFrameTitle("�Љ��쐬"),
                                    JOptionPane.INFORMATION_MESSAGE);

                        } else {
                            JOptionPane.showMessageDialog(
                                    getContext().getFrame(),
                                    "�Љ��PDF�t�@�C���𐶐����邱�Ƃ��ł��܂���B",
                                    ClientContext.getFrameTitle("�Љ��쐬"),
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    }
                };
                EventQueue.invokeLater(awt);
            }
        };
        Thread t = new Thread(r);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

    public void historyPeriodChanged() {
        stateMgr.processEmptyEvent();
    }

    public void showDocuments(DocInfoModel[] docs, JScrollPane scroller) {

        if (docs == null || docs.length == 0) {
            stateMgr.processEmptyEvent();
            return;
        }

        DocInfoModel docInfo = docs[0];
        long pk = docInfo.getDocPk();

        if (pk == 0L) {
            return;
        }

        LetterGetTask task = new LetterGetTask(getContext(), pk, scroller);

        task.execute();
    }

    class LetterGetTask extends DBTask<LetterModel> {

        private long letterPk;
        private JScrollPane scroller;

        public LetterGetTask(Chart app, long letterPk, JScrollPane scroller) {
            super(app);
            this.letterPk = letterPk;
            this.scroller = scroller;
        }

        @Override
        protected LetterModel doInBackground() throws Exception {

            DocumentDelegater ddl = new DocumentDelegater();
            LetterModel letter = ddl.getLetter(letterPk);

            if (ddl.isNoError()) {
                return letter;
            } else {
                return null;
            }
        }

        @Override
        protected void succeeded(LetterModel letter) {
            logger.debug("LetterGetTask succeeded");
            if (letter != null) {

                model = (TouTouLetter) letter;

                view = new LetterView2();
                view.getConfirmed().setText(ModelUtils.getDateAsString(model.getConfirmed()));
                view.getCHospital().setText(model.getConsultantHospital());
                view.getCDept().setText(model.getConsultantDept());
                view.getCDoctor().setText(model.getConsultantDoctor());
                view.getPatientName().setText(model.getPatientName());

//                view.getMyHospital().setText(model.getClientHospital());
//                view.getMyName().setText(model.getClientName());
//                view.getAddress().setText(model.getClientAddress());
//                view.getTelephone().setText(model.getClientTelephone());

                view.getPatientName().setText(model.getPatientName());
                view.getPatientGender().setText(model.getPatientGender());
                view.getPatientBirthday().setText(model.getPatientBirthday());
                view.getPatientAge().setText(model.getPatientAge());

                view.getDisease().setText(model.getDisease());
                view.getPurpose().setText(model.getPurpose());
                view.getClinicalCourse().setText(model.getClinicalCourse());
                view.getPastFamily().setText(model.getPastFamily());
                view.getMedication().setText(model.getMedication());
                view.getRemarks().setText(model.getRemarks());

                boolean b = false;
                view.getConfirmed().setEditable(b);
                view.getCHospital().setEditable(b);
                view.getCDept().setEditable(b);
                view.getCDoctor().setEditable(b);
                view.getDisease().setEditable(b);
                view.getPurpose().setEditable(b);
                view.getPastFamily().setEditable(b);
                view.getClinicalCourse().setEditable(b);
                view.getMedication().setEditable(b);
                view.getRemarks().setEditable(b);

                scroller.setViewportView(view);

                stateMgr.processCleanEvent();
            }
        }
    }

    /**
     * ���ۏ�ԃN���X�B
     */
    protected abstract class LetterState {

        public LetterState() {
        }

        public abstract void enter();
    }

    /**
     * �\������J���e���Ȃ���Ԃ�\���B
     */
    protected final class EmptyState extends LetterState {

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
    protected final class ClaenState extends LetterState {

        public ClaenState() {
        }

        public void enter() {

            //
            // �V�K�J���e���\�ȃP�[�X ���ۑ��łȂ����Ƃ�ǉ�
            //
            boolean canEdit = isReadOnly() ? false : true;
            getContext().enabledAction(GUIConst.ACTION_NEW_KARTE, canEdit);      // �V�K�J���e
            getContext().enabledAction(GUIConst.ACTION_NEW_DOCUMENT, canEdit);   // �V�K����
            getContext().enabledAction(GUIConst.ACTION_MODIFY_KARTE, false);     // �C��
            getContext().enabledAction(GUIConst.ACTION_DELETE, false);           // �폜
            getContext().enabledAction(GUIConst.ACTION_PRINT, true);             // ���
            getContext().enabledAction(GUIConst.ACTION_ASCENDING, false);        // ����
            getContext().enabledAction(GUIConst.ACTION_DESCENDING, false);       // �~��
            getContext().enabledAction(GUIConst.ACTION_SHOW_MODIFIED, false);    // �C������\��
        }
    }

    /**
     * StateContext �N���X�B
     */
    protected final class StateMgr {

        private LetterState emptyState = new EmptyState();
        private LetterState cleanState = new ClaenState();
        private LetterState currentState;

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
