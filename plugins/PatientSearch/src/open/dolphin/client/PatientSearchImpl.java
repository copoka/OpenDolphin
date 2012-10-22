
package open.dolphin.client;


import javax.swing.*;

import javax.swing.event.ListSelectionEvent;
import open.dolphin.delegater.PatientDelegater;
import open.dolphin.dto.PatientSearchSpec;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.infomodel.SimpleDate;
import open.dolphin.project.Project;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.event.ListSelectionListener;
import open.dolphin.delegater.PVTDelegater;
import open.dolphin.helper.KeyBlocker;
import open.dolphin.infomodel.ModelUtils;
import open.dolphin.table.ObjectReflectTableModel;
import open.dolphin.table.OddEvenRowRenderer;
import open.dolphin.util.StringTool;
import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.Task.InputBlocker;
import org.jdesktop.application.TaskMonitor;

/**
 * ���Ҍ���PatientSearchPlugin
 *
 * @author Kazushi Minagawa
 */
public class PatientSearchImpl extends AbstractMainComponent {

    private int number = 10000;
    private final String NAME = "���Ҍ���";
    private final String[] COLUMN_NAMES = {"ID","����","�J�i","����","���N����"};
    private final int START_NUM_ROWS = 30;
    private final String[] METHOD_NAMES = {"getPatientId","getFullName",
        "getKanaName","getGenderDesc","getAgeBirthday"};
    private final int[] COLUMN_WIDTH = {80,120,120,30,80};
    //private final int ROW_HEIGHT = 18;
    private final String UNSUITABLE_CHAR = "�����ɓK���Ȃ��������܂܂�Ă��܂��B";
    
    // �I������Ă��銳�ҏ��
    private PatientModel selectedPatient;
    
    // �N��\��
    private boolean ageDisplay;
    
    // �N��\���J����
    private final int AGE_COLUMN = 4;
    
    // �N��N�������\�b�h
    private final String[] AGE_METHOD = new String[]{"getAgeBirthday", "getBirthday"};
    
    // View
    private PatientSearchView view;
    
    private KeyBlocker keyBlocker;
    
    private Logger logger;
    private Application app;
    private ApplicationContext appCtx;
    private TaskMonitor taskMonitor;

    /** Creates new PatientSearch */
    public PatientSearchImpl() {
        setName(NAME);
        logger = ClientContext.getBootLogger();
        appCtx = ClientContext.getApplicationContext();
        app = appCtx.getApplication();
        taskMonitor = appCtx.getTaskMonitor();
    }

    @Override
    public void start() {
        initComponents();
        connect();
        enter();
    }

    @Override
    public void enter() {
        controlMenu();
    }

    @Override
    public void stop() {
    }

    public PatientModel getSelectedPatinet() {
        return selectedPatient;
    }

    public void setSelectedPatinet(PatientModel model) {
        selectedPatient = model;
        controlMenu();
    }

    /**
     * �N��\�����I���I�t����B
     */
    public void switchAgeDisplay() {
        ageDisplay = !ageDisplay;
        Preferences preferences = Preferences.userNodeForPackage(this.getClass());
        preferences.putBoolean("ageDisplay", ageDisplay);
        
        if (view.getTable() != null) {
            ObjectReflectTableModel tModel = (ObjectReflectTableModel) view.getTable().getModel();
            String method = ageDisplay ? AGE_METHOD[0] : AGE_METHOD[1];
            tModel.setMethodName(method, AGE_COLUMN);
        }
    }

    /**
     * ���j���[�𐧌䂷��
     */
    private void controlMenu() {

        PatientModel pvt = getSelectedPatinet();
        boolean enabled = canOpen(pvt);
        getContext().enabledAction(GUIConst.ACTION_OPEN_KARTE, enabled);
    }

    /**
     * �J���e���J�����Ƃ��\���ǂ�����Ԃ��B
     * @return �J�����Ƃ��\�Ȏ� true
     */
    private boolean canOpen(PatientModel patient) {
        if (patient == null) {
            return false;
        }

        if (isKarteOpened(patient)) {
            return false;
        }

        return true;
    }

    /**
     * �J���e���I�[�v������Ă��邩�ǂ�����Ԃ��B
     * @return �I�[�v������Ă��鎞 true
     */
    private boolean isKarteOpened(PatientModel patient) {
        if (patient != null) {
            boolean opened = false;
            List<ChartImpl> allCharts = ChartImpl.getAllChart();
            for (ChartImpl chart : allCharts) {
                if (chart.getPatient().getId() == patient.getId()) {
                    opened = true;
                    break;
                }
            }
            return opened;
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

                int row = view.getTable().rowAtPoint(e.getPoint());
                Object obj = ((ObjectReflectTableModel) view.getTable().getModel()).getObject(row);
                int selected = view.getTable().getSelectedRow();

                if (row == selected && obj != null) {
                    String pop1 = ClientContext.getString("watingList.popup.openKarte");
                    contextMenu.add(new JMenuItem(new ReflectAction(pop1, PatientSearchImpl.this, "openKarte")));
                    contextMenu.add(new JMenuItem(new ReflectAction("��t�o�^", PatientSearchImpl.this, "addAsPvt")));
                }

                JCheckBoxMenuItem item = new JCheckBoxMenuItem("�N��\��");
                contextMenu.add(item);
                item.setSelected(ageDisplay);
                item.addActionListener((ActionListener) EventHandler.create(ActionListener.class, PatientSearchImpl.this, "switchAgeDisplay"));

                contextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    /**
     * GUI �R���|�[�l���g������������B
     *
     */
    private void initComponents() {
        
        view = new PatientSearchView();
        setUI(view);
        
        //
        // �N��\�������Ȃ��Ȃ�ĐM�����Ȃ��v�]!
        //
        Preferences preferences = Preferences.userNodeForPackage(this.getClass());
        ageDisplay = preferences.getBoolean("ageDisplay", true);
        if (!ageDisplay) {
            METHOD_NAMES[4] = AGE_METHOD[1];
        }
           
        ObjectReflectTableModel tableModel = new ObjectReflectTableModel(
                 COLUMN_NAMES, START_NUM_ROWS, METHOD_NAMES, null);
        view.getTable().setModel(tableModel);
        
        // �J��������ύX����
        for (int i = 0; i < COLUMN_WIDTH.length; i++) {
            view.getTable().getColumnModel().getColumn(i).setPreferredWidth(
                    COLUMN_WIDTH[i]);
        }
        
        // �����_����ݒ肷��
        view.getTable().setDefaultRenderer(Object.class, new OddEvenRowRenderer());
        
        // ���t��ݒ肷��
        String formatStr = "yyyy-M-d(EEE)";
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
        view.getDateLbl().setText(sdf.format(new Date()));
    }

    /**
     * �R���|�[�����g�Ƀ��X�i��o�^���ڑ�����B
     */
    private void connect() {
        
        EventAdapter adp = new EventAdapter(view.getKeywordFld(), view.getTable());

        // �J�����_�ɂ����t������ݒ肷��
        PopupListener pl = new PopupListener(view.getKeywordFld());

        // �R���e�L�X�g���j���[��ݒ肷��
        view.getTable().addMouseListener(new ContextListener());
        
        keyBlocker = new KeyBlocker(view.getKeywordFld());

    }
    
    class EventAdapter implements ActionListener, ListSelectionListener, MouseListener {
        
        public EventAdapter(JTextField tf, JTable tbl) {
            
            tf.addActionListener(this);
            tf.addFocusListener(AutoKanjiListener.getInstance());
            tbl.getSelectionModel().addListSelectionListener(this);
            tbl.addMouseListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            JTextField tf = (JTextField) e.getSource();
            String test = tf.getText().trim();
            if (!test.equals("")) {
                find(test);
            }
        }

        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting() == false) {
                JTable table = view.getTable();
                ObjectReflectTableModel tableModel = (ObjectReflectTableModel) table.getModel();
                int row = table.getSelectedRow();
                PatientModel patient = (PatientModel) tableModel.getObject(row);
                setSelectedPatinet(patient);
            }
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                JTable table = (JTable) e.getSource();
                ObjectReflectTableModel tableModel = (ObjectReflectTableModel) table.getModel();
                PatientModel value = (PatientModel) tableModel.getObject(table.getSelectedRow());
                if (value != null) {
                    openKarte();
                }
            }
        }

        public void mousePressed(MouseEvent arg0) {}

        public void mouseReleased(MouseEvent arg0) {}

        public void mouseEntered(MouseEvent arg0) {}

        public void mouseExited(MouseEvent arg0) {}
    }
    
    private String constarctDept() {
        StringBuilder sb = new StringBuilder();
        sb.append(Project.getUserModel().getDepartmentModel().getDepartmentDesc());
        sb.append(",");
        sb.append(Project.getUserModel().getDepartmentModel().getDepartment());
        sb.append(",");
        sb.append(Project.getUserModel().getCommonName());
        sb.append(",");
        sb.append(Project.getUserModel().getUserId());
        sb.append(",");
        sb.append(Project.getJMARICode());
        String ret = sb.toString();
        logger.debug("constracted dept = " + ret);
        return ret;
    }

    /**
     * �J���e���J���B
     * @param value �Ώۊ���
     */
    public void openKarte() {

        //        final Preferences pref = Preferences.userNodeForPackage(this.getClass());
//        boolean showReceiptMessage = pref.getBoolean("showReceiptMessage", true);
//        //showReceiptMessage = true;
//        if (showReceiptMessage) {
//            JLabel msg1 = new JLabel("��t���X�g����I�[�v�����Ȃ��Ɛf�Ãf�[�^�����Z�R����");
//            JLabel msg2 = new JLabel("���M���邱�Ƃ��ł��܂���B�����܂���?");
//            final JCheckBox cb = new JCheckBox("���ケ�̃��b�Z�[�W��\�����Ȃ�");
//            cb.setFont(new Font("Dialog", Font.PLAIN, 10));
//            cb.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent e) {
//                    pref.putBoolean("showReceiptMessage", !cb.isSelected());
//                }
//            });
//            JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
//            p1.add(msg1);
//            JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
//            p2.add(msg2);
//            JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
//            p3.add(cb);
//            JPanel box = new JPanel();
//            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
//            box.add(p1);
//            box.add(p2);
//            box.add(p3);
//            box.setBorder(BorderFactory.createEmptyBorder(0, 0, 11, 11));
//            
//            int option = JOptionPane.showConfirmDialog(this.getContext().getFrame(),
//                    new Object[]{box},
//                    ClientContext.getFrameTitle(getTitle()),
//                    JOptionPane.YES_NO_OPTION,
//                    JOptionPane.QUESTION_MESSAGE,
//                    ClientContext.getImageIcon("about_32.gif"));
//            
//            if (option != JOptionPane.YES_OPTION) {
//                return;
//            }
//        }
        if (canOpen(getSelectedPatinet())) {

            // ���@���𐶐�����
            PatientVisitModel pvt = new PatientVisitModel();
            pvt.setId(0L);
            pvt.setNumber(number++);
            pvt.setPatient(getSelectedPatinet());

            // �󂯕t����ʂ��Ă��Ȃ��̂Ń��O�C�����y�ѐݒ�t�@�C�����g�p����
            // �f�ÉȖ��A�f�ÉȃR�[�h�A��t���A��t�R�[�h�AJMARI
            pvt.setDepartment(constarctDept());

            // �J���e�R���e�i�𐶐�����
            getContext().openKarte(pvt);
        }
    }

    /**
     * ���X�g�őI�����ꂽ���҂���t�ɓo�^����B
     */
    public void addAsPvt() {

        // ���@���𐶐�����
        PatientVisitModel pvt = new PatientVisitModel();
        pvt.setId(0L);
        pvt.setNumber(number++);
        pvt.setPatient(getSelectedPatinet());

        // �󂯕t����ʂ��Ă��Ȃ��̂Őf�ÉȂ̓��[�U�o�^���Ă�����̂��g�p����
        pvt.setDepartment(constarctDept());

        // ���@��
        pvt.setPvtDate(ModelUtils.getDateTimeAsString(new Date()));

        app = ClientContext.getApplicationContext().getApplication();
        taskMonitor = ClientContext.getApplicationContext().getTaskMonitor();

        AddAsPvtTask task = new AddAsPvtTask(app, pvt);
        MyInputBlocker blocker = new MyInputBlocker(task, getContext().getGlassPane(), keyBlocker);
        task.setInputBlocker(blocker);

        StatusMonitor bar = new StatusMonitor(task, taskMonitor, view.getProgressBar());
        ClientContext.getApplicationContext().getTaskService().execute(task);
    }

    class AddAsPvtTask extends Task<Void, Void> {

        private PatientVisitModel pvt;

        public AddAsPvtTask(Application app, PatientVisitModel pvt) {
            super(app);
            this.pvt = pvt;
        }

        protected Void doInBackground() {

            PVTDelegater pdl = new PVTDelegater();
            pdl.setLogger(ClientContext.getPvtLogger());
            pdl.addPvt(pvt);

            return null;
        }
    }

    class StatusMonitor implements PropertyChangeListener {

        private Task task;
        private TaskMonitor taskMonitor;
        private JProgressBar bar;

        public StatusMonitor(Task task,
                TaskMonitor taskMonitor, JProgressBar bar) {
            this.task = task;
            this.taskMonitor = taskMonitor;
            this.bar = bar;
            taskMonitor.addPropertyChangeListener(this);
        }

        public void propertyChange(PropertyChangeEvent e) {

            String propertyName = e.getPropertyName();

            if ("started".equals(propertyName)) {
                bar.setIndeterminate(true);

            } else if ("done".equals(propertyName)) {
                bar.setIndeterminate(false);
                bar.setValue(0);
                taskMonitor.removePropertyChangeListener(this); // �d�v
            }
        }
    }

    class FindTask extends Task<Collection, Void> {

        private PatientSearchSpec spec;

        public FindTask(Application app, PatientSearchSpec spec) {
            super(app);
            this.spec = spec;
        }

        protected Collection doInBackground() throws Exception {
            logger.debug("FindTask doInBackground");
            PatientDelegater pdl = new PatientDelegater();
            Collection result = pdl.getPatients(spec);

            return result;
        }

        protected void succeeded(Collection result) {
            logger.debug("FindTask succeeded");
            ObjectReflectTableModel tableModel = (ObjectReflectTableModel) view.getTable().getModel();
            tableModel.setObjectList((ArrayList) result);
            int cnt = result != null ? result.size() : 0;
            String cntStr = String.valueOf(cnt);
            view.getCntLbl().setText(cntStr + " ��");
        }
        
        protected void failed(Throwable cause) {
            logger.debug("FindTask failed");
            logger.debug(cause.getCause());
            logger.debug(cause.getMessage());
        }
        
        protected void cancelled() {
            logger.debug("FindTask cancelled");
        }
    }

    class MyInputBlocker extends InputBlocker {

        private BlockGlass glassPane;
        private KeyBlocker keyBlocker;
        
        MyInputBlocker(Task task, BlockGlass glassPane, KeyBlocker keyBlocker) {
            super(task, Task.BlockingScope.WINDOW, glassPane);
            this.glassPane = glassPane;
            this.keyBlocker = keyBlocker;
        }

        protected void block() {
            glassPane.block();
            keyBlocker.block();
        }

        protected void unblock() {
            glassPane.unblock();
            keyBlocker.unblock();
        }
    }

    /**
     * ���������s����B
     * @param text �L�[���[�h
     */
    private void find(String text) {

        PatientSearchSpec spec = new PatientSearchSpec();

        if (text.startsWith("N ") || text.startsWith("n ")) {
            spec.setCode(PatientSearchSpec.NAME_SEARCH);
            text = text.substring(2);
            spec.setName(text);

        } else if (text.startsWith("K ") || text.startsWith("k ")) {
            spec.setCode(PatientSearchSpec.KANA_SEARCH);
            text = text.substring(2);
            spec.setName(text);

        } else if (text.startsWith("A ") || text.startsWith("a ")) {
            spec.setCode(PatientSearchSpec.ADDRESS_SEARCH);
            text = text.substring(2);
            spec.setAddress(text);

        } else if (text.startsWith("Z ") || text.startsWith("z ")) {
            spec.setCode(PatientSearchSpec.ZIPCODE_SEARCH);
            text = text.substring(2);
            spec.setZipCode(text);

        } else if (text.startsWith("T ") || text.startsWith("t ")) {
            spec.setCode(PatientSearchSpec.TELEPHONE_SEARCH);
            text = text.substring(2);
            spec.setTelephone(text);

        } else if (text.startsWith("I ") || text.startsWith("i ")) {
            spec.setCode(PatientSearchSpec.ID_SEARCH);
            text = text.substring(2);
            spec.setPatientId(text);

        } else if (text.startsWith("E ") || text.startsWith("e ")) {
            spec.setCode(PatientSearchSpec.EMAIL_SEARCH);
            text = text.substring(2);
            spec.setEmail(text);

        } else if (text.startsWith("O ") || text.startsWith("o ")) {
            spec.setCode(PatientSearchSpec.OTHERID_SEARCH);
            text = text.substring(2);
            spec.setOtherId(text);

        } else if (isDate(text)) {
            //System.out.println("Date search");
            spec.setCode(PatientSearchSpec.DATE_SEARCH);
            spec.setDigit(text);

        } else if (isKana(text)) {
            spec.setCode(PatientSearchSpec.KANA_SEARCH);
            spec.setName(text);


        } else if (isNameAddress(text)) {
            spec.setCode(PatientSearchSpec.NAME_SEARCH);
            spec.setName(text);

        } else if (isTelephoneZip(text)) {
            spec.setCode(PatientSearchSpec.DIGIT_SEARCH);
            spec.setDigit(text);

        } else if (isId(text)) {
            spec.setCode(PatientSearchSpec.ID_SEARCH);
            spec.setPatientId(text);

        } else {
            String msg = UNSUITABLE_CHAR;
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(getUI()), msg, getName(), JOptionPane.WARNING_MESSAGE);
            return;
        }

        app = ClientContext.getApplicationContext().getApplication();
        taskMonitor = ClientContext.getApplicationContext().getTaskMonitor();

        FindTask task = new FindTask(app, spec);
        MyInputBlocker blocker = new MyInputBlocker(task, getContext().getGlassPane(), keyBlocker);
        task.setInputBlocker(blocker);

        StatusMonitor bar = new StatusMonitor(task, taskMonitor, view.getProgressBar());
        appCtx.getTaskService().execute(task);
    }

    private boolean isDate(String text) {
        boolean maybe = false;
        if (text != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.parse(text);
                maybe = true;

            } catch (Exception e) {
            }
        }

        return maybe;
    }

    private boolean isKana(String text) {
        boolean maybe = true;
        if (text != null) {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (!StringTool.isKatakana(c)) {
                    maybe = false;
                    break;
                }
            }
            return maybe;
        }

        return false;
    }

    private boolean isNameAddress(String text) {
        boolean maybe = false;
        if (text != null) {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (Character.getType(c) == Character.OTHER_LETTER) {
                    maybe = true;
                    break;
                }
            }
        }
        return maybe;
    }

    private boolean isId(String text) {
        boolean maybe = true;
        if (text != null) {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                int type = Character.getType(c);
                if (type == Character.UPPERCASE_LETTER ||
                        type == Character.LOWERCASE_LETTER ||
                        type == Character.DECIMAL_DIGIT_NUMBER) {
                    continue;
                } else {
                    maybe = false;
                    break;
                }
            }
            return maybe;
        }
        return false;
    }

    private boolean isTelephoneZip(String text) {
        boolean maybe = true;
        if (text != null) {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                int type = Character.getType(c);
                if (type == Character.DECIMAL_DIGIT_NUMBER ||
                        c == '-' ||
                        c == '(' ||
                        c == ')') {
                    continue;
                } else {
                    maybe = false;
                    break;
                }
            }
            return maybe;
        }
        return false;
    }

    /**
     * �e�L�X�g�t�B�[���h�֓��t����͂��邽�߂̃J�����_�[�|�b�v�A�b�v���j���[�N���X�B
     */
    class PopupListener extends MouseAdapter implements PropertyChangeListener {

        /** �|�b�v�A�b�v���j���[ */
        private JPopupMenu popup;
        /** �^�[�Q�b�g�̃e�L�X�g�t�B�[���h */
        private JTextField tf;

        public PopupListener(JTextField tf) {
            this.tf = tf;
            tf.addMouseListener(this);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {

            if (e.isPopupTrigger()) {
                popup = new JPopupMenu();
                CalendarCardPanel cc = new CalendarCardPanel(ClientContext.getEventColorTable());
                cc.addPropertyChangeListener(CalendarCardPanel.PICKED_DATE, this);
                cc.setCalendarRange(new int[]{-12, 0});
                popup.insert(cc, 0);
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals(CalendarCardPanel.PICKED_DATE)) {
                SimpleDate sd = (SimpleDate) e.getNewValue();
                tf.setText(SimpleDate.simpleDateToMmldate(sd));
                popup.setVisible(false);
                popup = null;
                String test = tf.getText().trim();
                if (!test.equals("")) {
                    find(test);
                }
            }
        }
    }
}

