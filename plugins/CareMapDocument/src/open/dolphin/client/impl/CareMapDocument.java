
package open.dolphin.client.impl;

import javax.swing.*;
import open.dolphin.client.*;

import open.dolphin.delegater.AppointmentDelegater;
import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.dto.ImageSearchSpec;
import open.dolphin.dto.ModuleSearchSpec;
import open.dolphin.infomodel.AppointmentModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModelUtils;

import java.beans.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import open.dolphin.helper.DBTask;
import open.dolphin.project.Project;

/**
 * CareMap Document.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public final class CareMapDocument extends AbstractChartDocument {
    
    public static final String MARK_EVENT_PROP = "MARK_EVENT_PROP";
    public static final String PERIOD_PROP = "PERIOD_PROP";
    public static final String CALENDAR_PROP = "CALENDAR_PROP";
    public static final String SELECTED_DATE_PROP = "SELECTED_DATE_PROP";
    public static final String SELECTED_APPOINT_DATE_PROP = "SELECTED_DATE_PROP";
    public static final String APPOINT_PROP = "APPOINT_PROP";
    
    private static final String[] orderNames = { "����", "���u", "�w��", "���{�e�X�g", "���̌���", "���ː�"};
    
    private static final String[] orderCodes = { "medOrder", "treatmentOrder", "instractionChargeOrder", "testOrder", "physiologyOrder", "radiologyOrder"};
    
        /*private static final Color[] orderColors = {
                        ClientContext.getColor("color.RP"),
                        ClientContext.getColor("color.TREATMENT"),
                        ClientContext.getColor("color.TEST"),
                        ClientContext.getColor("color.IMAGE") };*/
    
    private static final String[] appointNames = { "�Đf", "���̌���", "�摜�f�f", "���̑�" };
    
    private static final Color[] appointColors = {
        ClientContext.getColor("color.EXAM_APPO"),
        ClientContext.getColor("color.TEST"),
        ClientContext.getColor("color.IMAGE"),
        new Color(251, 239, 128) };
    
        /*private static final Icon[] orderIcons = {
                        new ColorFillIcon(orderColors[0], 10, 10, 1),
                        new ColorFillIcon(orderColors[1], 10, 10, 1),
                        new ColorFillIcon(orderColors[2], 10, 10, 1),
                        new ColorFillIcon(orderColors[3], 10, 10, 1) };*/
    
    private static final int IMAGE_WIDTH = 128;
    private static final int IMAGE_HEIGHT = 128;
    private static final String TITLE = "���×���";
    
    private JComboBox orderCombo;
    private OrderHistoryPanel history;
    private AppointTablePanel appointTable;
    private ImageHistoryPanel imagePanel;
    private JPanel historyContainer;
    private String imageEvent = "image"; //orderCodes[2]; //
    // Calendars
    private SimpleCalendarPanel c0;
    private SimpleCalendarPanel c1;
    private SimpleCalendarPanel c2;
    private Period selectedPeriod;
    private int origin;
    private PropertyChangeSupport boundSupport;
    private Hashtable<Integer, SimpleCalendarPanel> cPool;
    private String selectedEvent;
    //private boolean updated;
    private JButton updateAppoBtn; // �\��̍X�V�͂��̃{�^���ōs��
    
    // ���W���[�������֘A
    private List allModules;
    private List allAppointments;
    private List allImages;
    
    private javax.swing.Timer taskTimer;
    
    /**
     * Creates new CareMap
     */
    public CareMapDocument() {
        setTitle(TITLE);
    }
    
    /**
     * ����������B
     */
    private void initialize() {
        
        cPool = new Hashtable<Integer, SimpleCalendarPanel>(12, 0.75f);
        Chart chartCtx = getContext();
        
        JPanel myPanel = getUI();
        myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
        
        // �挎�A�����A�����̃J�����_�[�𐶐�����
        SimpleCalendarPanel.SimpleCalendarPool pool = SimpleCalendarPanel.SimpleCalendarPool
                .getInstance();
        c0 = pool.acquireSimpleCalendar(origin - 1);
        c1 = pool.acquireSimpleCalendar(origin);
        c2 = pool.acquireSimpleCalendar(origin + 1);
        c0.setChartContext(chartCtx);
        c1.setChartContext(chartCtx);
        c2.setChartContext(chartCtx);
        c0.setParent(this);
        c1.setParent(this);
        c2.setParent(this);
        cPool.put(new Integer(origin - 1), c0);
        cPool.put(new Integer(origin), c1);
        cPool.put(new Integer(origin + 1), c2);
        
        // 3�P�����̃J�����_�[��z�u����
        final JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(Box.createHorizontalStrut(11));
        p.add(c0);
        p.add(Box.createHorizontalStrut(11));
        p.add(c1);
        p.add(Box.createHorizontalStrut(11));
        p.add(c2);
        p.add(Box.createHorizontalStrut(11));
        
        // �J�����_�[�͈̔͂��P�P���Ȃɖ߂��{�^��
        JButton prevBtn = new JButton(ClientContext.getImageIcon("back_16.gif"));
        
        prevBtn.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                // �N���b�N���ꂽ�� (c0 | c1 | c2) -> (c0=test | c1=c0 | c2=c1)
                SimpleCalendarPanel.SimpleCalendarPool pool = SimpleCalendarPanel.SimpleCalendarPool
                        .getInstance();
                origin--;
                SimpleCalendarPanel save = c0;
                SimpleCalendarPanel test = (SimpleCalendarPanel) cPool
                        .get(new Integer(origin - 1));
                
                if (test != null) {
                    // Pool ����Ă����ꍇ
                    c0 = test;
                    
                } else {
                    // �V�K�ɍ쐬
                    c0 = pool.acquireSimpleCalendar(origin - 1);
                    c0.setChartContext(getContext());
                    c0.setParent(CareMapDocument.this);
                    
                    // �J�����_�̓����N���b�N�������ɑ��������ʒm���󂯂郊�X�i
                    c0.addPropertyChangeListener(SELECTED_DATE_PROP, history);
                    c0
                            .addPropertyChangeListener(SELECTED_DATE_PROP,
                            imagePanel);
                    c0.addPropertyChangeListener(SELECTED_APPOINT_DATE_PROP,
                            appointTable);
                    c0.addPropertyChangeListener(APPOINT_PROP, appointTable);
                    
                    cPool.put(new Integer(origin - 1), c0);
                }
                
                c2 = c1;
                c1 = save;
                p.removeAll();
                p.add(c0);
                p.add(c1);
                p.add(c2);
                p.revalidate();
                
                // �I�[�_�����̒��o���ԑS�̂��ω������̂Œʒm����
                Period p = new Period(this);
                p.setStartDate(c0.getFirstDate());
                p.setEndDate(c2.getLastDate());
                setSelectedPeriod(p);
            }
        });
        
        // �J�����_�[�͈̔͂��P�P������{�^��
        JButton nextBtn = new JButton(ClientContext
                .getImageIcon("forwd_16.gif"));
        
        nextBtn.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                // �N���b�N���ꂽ�� (c0 | c1 | c2) -> (c0=c1 | c1=c2 | c2=test)
                SimpleCalendarPanel.SimpleCalendarPool pool = SimpleCalendarPanel.SimpleCalendarPool.getInstance();
                origin++;
                SimpleCalendarPanel save = c2;
                SimpleCalendarPanel test = (SimpleCalendarPanel) cPool.get(new Integer(origin + 1));
                
                if (test != null) {
                    // Pool ����Ă����ꍇ
                    c2 = test;
                    
                } else {
                    // �V�K�ɍ쐬����
                    c2 = pool.acquireSimpleCalendar(origin + 1);
                    c2.setChartContext(getContext());
                    c2.setParent(CareMapDocument.this);
                    
                    // �J�����_�̓����N���b�N�������ɑ��������ʒm���󂯂郊�X�i
                    c2.addPropertyChangeListener(SELECTED_DATE_PROP, history);
                    c2.addPropertyChangeListener(SELECTED_DATE_PROP, imagePanel);
                    c2.addPropertyChangeListener(SELECTED_APPOINT_DATE_PROP, appointTable);
                    c2.addPropertyChangeListener(APPOINT_PROP, appointTable);
                    
                    cPool.put(new Integer(origin + 1), c2);
                }
                
                c0 = c1;
                c1 = save;
                p.removeAll();
                p.add(c0);
                p.add(c1);
                p.add(c2);
                p.revalidate();
                
                // �I�[�_�����̒��o���ԑS�̂��ω������̂Œʒm����
                Period p = new Period(this);
                p.setStartDate(c0.getFirstDate());
                p.setEndDate(c2.getLastDate());
                setSelectedPeriod(p);
            }
        });
        
        // �\��\�e�[�u���𐶐�����
        updateAppoBtn = new JButton(ClientContext.getImageIcon("save_24.gif"));
        updateAppoBtn.setEnabled(false);
        updateAppoBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        appointTable = new AppointTablePanel(updateAppoBtn);
        appointTable.setParent(this);
        appointTable.setBorder(BorderFactory.createTitledBorder("�\��\"));
        appointTable.setPreferredSize(new Dimension(500, 260));
        
        // �I�[�_����\���p�e�[�u���𐶐�����
        history = new OrderHistoryPanel();
        //history.setParent(this);
        history.setPid(chartCtx.getPatient().getPatientId());
        
        // �摜����p�̃p�l���𐶐�����
        imagePanel = new ImageHistoryPanel();
        imagePanel.setMyParent(this);
        imagePanel.setPid(chartCtx.getPatient().getPatientId());
        
        // �\������I�[�_��I������ Combo, �J�����_�[�̑���A�߂�{�^����z�u����p�l��
        JPanel cp = new JPanel();
        cp.setLayout(new BoxLayout(cp, BoxLayout.X_AXIS));
        
        // �I�[�_�I��p�̃R���{�{�b�N�X
        orderCombo = new JComboBox(orderNames);
        Dimension dim = new Dimension(100, 26);
        orderCombo.setPreferredSize(dim);
        orderCombo.setMaximumSize(dim);
        ComboBoxRenderer r = new ComboBoxRenderer();
        orderCombo.setRenderer(r);
        orderCombo.addItemListener(new ItemListener() {
            
            public void itemStateChanged(ItemEvent e) {
                
                // �I�[�_�I�����ύX���ꂽ��
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    
                    String event = getMarkCode();
                    
                    if (event.equals(imageEvent)) {
                        // �摜�������I�����ꂽ�ꍇ Image Panel �ɕύX����
                        historyContainer.removeAll();
                        historyContainer.add(imagePanel, BorderLayout.CENTER);
                        historyContainer.revalidate();
                        // CareMapDocument.this.repaint();
                        getUI().repaint();
                        
                    } else if (selectedEvent.equals(imageEvent)) {
                        // ���݂̃C�x���g�� Image �̏ꍇ�� �I�[�_����p�Ɠ���ւ���
                        historyContainer.removeAll();
                        historyContainer.add(history, BorderLayout.CENTER);
                        historyContainer.revalidate();
                        // CareMapDocument.this.repaint();
                        getUI().repaint();
                    }
                    
                    // �I�����ꂽ�I�[�_���C�x���g�����ɐݒ肷��
                    setSelectedEvent(event);
                }
            }
        });
        cp.add(Box.createHorizontalGlue());
        cp.add(prevBtn);
        cp.add(Box.createHorizontalStrut(5));
        cp.add(orderCombo);
        cp.add(Box.createHorizontalStrut(5));
        cp.add(nextBtn);
        // cp.add(Box.createHorizontalStrut(30));
        cp.add(Box.createHorizontalGlue());
        JPanel han = new JPanel();
        han.setLayout(new BoxLayout(han, BoxLayout.X_AXIS));
        han.add(new JLabel("�\��( "));
        for (int i = 0; i < appointNames.length; i++) {
            if (i != 0) {
                han.add(Box.createHorizontalStrut(7));
            }
            AppointLabel dl = new AppointLabel(appointNames[i],
                    new ColorFillIcon(appointColors[i], 10, 10, 1),
                    SwingConstants.CENTER);
            han.add(dl);
        }
        han.add(new JLabel(" )"));
        han.add(Box.createHorizontalStrut(7));
        Color birthC = ClientContext.getColor("color.BIRTHDAY_BACK");
        han.add(new JLabel("�a����", new ColorFillIcon(birthC, 10, 10, 1),
                SwingConstants.CENTER));
        han.add(Box.createHorizontalStrut(11));
        cp.add(han);
        
        myPanel.add(p);
        myPanel.add(Box.createVerticalStrut(7));
        myPanel.add(cp);
        myPanel.add(Box.createVerticalStrut(7));
        
        // ���������Ɖ摜���̐؂�ւ��R���e�i
        historyContainer = new JPanel(new BorderLayout());
        historyContainer.add(history, BorderLayout.CENTER);
        historyContainer.setBorder(BorderFactory.createTitledBorder("�� ��"));
        myPanel.add(historyContainer);
        
        myPanel.add(Box.createVerticalStrut(7));
        myPanel.add(appointTable);
        
        myPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        
        // �C�x���g�ƃ��X�i�̊֌W��ݒ肷��
        
        // �J�����_�[�Z�b�g�̕ύX�ʒm
        addPropertyChangeListener(CALENDAR_PROP, appointTable);
        
        c0.addPropertyChangeListener(APPOINT_PROP, appointTable);
        c1.addPropertyChangeListener(APPOINT_PROP, appointTable);
        c2.addPropertyChangeListener(APPOINT_PROP, appointTable);
        
        // �J�����_�[�̓���I���������ɒʒm��������
        c0.addPropertyChangeListener(SELECTED_DATE_PROP, history);
        c1.addPropertyChangeListener(SELECTED_DATE_PROP, history);
        c2.addPropertyChangeListener(SELECTED_DATE_PROP, history);
        c0.addPropertyChangeListener(SELECTED_DATE_PROP, imagePanel);
        c1.addPropertyChangeListener(SELECTED_DATE_PROP, imagePanel);
        c2.addPropertyChangeListener(SELECTED_DATE_PROP, imagePanel);
        
        // �J�����_��̗\�����I�����ꂽ���ɒʒm��������
        c0.addPropertyChangeListener(SELECTED_APPOINT_DATE_PROP, appointTable);
        c1.addPropertyChangeListener(SELECTED_APPOINT_DATE_PROP, appointTable);
        c2.addPropertyChangeListener(SELECTED_APPOINT_DATE_PROP, appointTable);
    }
    
    @Override
    public void start() {
        initialize();
        enter();
        // �ŏ��ɑI������Ă���I�[�_�̗�����\������
        setSelectedEvent(getMarkCode());
        Period period = new Period(this);
        period.setStartDate(c0.getFirstDate());
        period.setEndDate(c2.getLastDate());
        setSelectedPeriod(period);
    }
    
    @Override
    public void stop() {  
    }
    
    /**
     * �I�[�_��\������J���[��Ԃ��B
     *
     * @param order
     *            �I�[�_��
     * @return �J���[
     */
    public Color getOrderColor(String order) {
        Color ret = Color.PINK;
                /*for (int i = 0; i < orderCodes.length; i++) {
                        if (order.equals(orderCodes[i])) {
                                ret = orderColors[i];
                        }
                }*/
        return ret;
    }
    
    /**
     * �\��̃J���[��Ԃ��B
     *
     * @param appoint
     *            �\��
     * @return �J���[
     */
    public Color getAppointColor(String appoint) {
        
        if (appoint == null) {
            return Color.white;
        }
        
        Color ret = null;
        for (int i = 0; i < appointNames.length; i++) {
            if (appoint.equals(appointNames[i])) {
                ret = appointColors[i];
            }
        }
        return ret;
    }
    
    /**
     * �v���p�e�B�`�F���W���X�i��ǉ�����B
     *
     * @param prop �v���p�e�B��
     * @param l ���X�i
     */
    public void addPropertyChangeListener(String prop, PropertyChangeListener l) {
        if (boundSupport == null) {
            boundSupport = new PropertyChangeSupport(this);
        }
        boundSupport.addPropertyChangeListener(prop, l);
    }
    
    /**
     * �v���p�e�B�`�F���W���X�i���폜����B
     *
     * @param prop �v���p�e�B��
     * @param l ���X�i
     */
    public void removePropertyChangeListener(String prop,
            PropertyChangeListener l) {
        if (boundSupport == null) {
            boundSupport = new PropertyChangeSupport(this);
        }
        boundSupport.removePropertyChangeListener(prop, l);
    }
    
    /**
     * �\�����Ă�����ԓ��ɂ��郂�W���[���̓����}�[�N����B
     * @param newModules  �\�����Ă�����ԓ��ɂ��郂�W���[���̃��X�g
     */
    public void setAllModules(List newModules) {
        
        if (newModules == null || newModules.size() == 0) {
            return;
        }
        
        allModules = newModules;
        
        c0.setModuleList(selectedEvent, (ArrayList) allModules.get(0));
        c1.setModuleList(selectedEvent, (ArrayList) allModules.get(1));
        c2.setModuleList(selectedEvent, (ArrayList) allModules.get(2));
        
        history.setModuleList(allModules);
    }
    
    /**
     * �\�����Ă�����ԓ��ɂ���\������}�[�N����B
     * @param allAppo �\�����Ă�����ԓ��ɂ���\����̃��X�g
     */
    public void setAllAppointments(List allAppo) {
        
        if (allAppo == null || allAppo.size() == 0) {
            return;
        }
        
        allAppointments = allAppo;
        
        c0.setAppointmentList((ArrayList) allAppointments.get(0));
        c1.setAppointmentList((ArrayList) allAppointments.get(1));
        c2.setAppointmentList((ArrayList) allAppointments.get(2));
        
        notifyCalendar();
    }
    
    /**
     * �\�����Ă�����ԓ��ɂ���摜���}�[�N����B
     * @param allAppo �\�����Ă�����ԓ��ɂ���摜�̃��X�g
     */
    public void setAllImages(List images) {
        
        if (images == null || images.size() == 0) {
            return;
        }
        
        allImages = images;
        
        c0.setImageList(selectedEvent, (ArrayList) allImages.get(0));
        c1.setImageList(selectedEvent, (ArrayList) allImages.get(1));
        c2.setImageList(selectedEvent, (ArrayList) allImages.get(2));
        
        imagePanel.setImageList(allImages);
    }
    
    /**
     * ���o���Ԃ��ύX���ꂽ�ꍇ�A���ݑI������Ă���C�x���g�ɉ����A ���W���[���܂��͉摜�������擾����B
     */
    public void setSelectedPeriod(Period p) {
        //Period old = selectedPeriod;
        selectedPeriod = p;
        
        if (getSelectedEvent().equals(imageEvent)) {
            getImageList();
            
        } else {
            getModuleList(true);
        }
    }
    
    /**
     * �J�����_�[�Z�b�g�̕ύX�ʒm������B
     */
    private void notifyCalendar() {
        SimpleCalendarPanel[] sc = new SimpleCalendarPanel[3];
        sc[0] = c0;
        sc[1] = c1;
        sc[2] = c2;
        boundSupport.firePropertyChange("CALENDAR_PROP", null, sc);
    }
    
    public String getSelectedEvent() {
        return selectedEvent;
    }
    
    /**
     * �\������I�[�_���ύX���ꂽ�ꍇ�A�I�����ꂽ�C�x���g�ɉ����A ���W���[���܂��͉摜�������擾����B
     */
    public void setSelectedEvent(String code) {
        //String old = selectedEvent;
        selectedEvent = code;
        
        if (getSelectedEvent().equals(imageEvent)) {
            getImageList();
            
        } else {
            getModuleList(false);
        }
    }
    
    /**
     * �ݒ肳��Ă��� curEvent �ƒ��o���Ԃ��烂�W���[���̃��X�g���擾����B
     */
    private void getModuleList(final boolean appo) {
        
        if (selectedEvent == null || selectedPeriod == null) {
            return;
        }
        
        final ModuleSearchSpec spec = new ModuleSearchSpec();
        spec.setCode(ModuleSearchSpec.ENTITY_SEARCH);
        spec.setKarteId(getContext().getKarte().getId());
        spec.setEntity(selectedEvent);
        spec.setStatus("F");
        
        // �J�����_�ʂɌ�������
        Date[] fromDate = new Date[3];
        fromDate[0] = ModelUtils.getDateTimeAsObject(c0.getFirstDate() + "T00:00:00");
        fromDate[1] = ModelUtils.getDateTimeAsObject(c1.getFirstDate() + "T00:00:00");
        fromDate[2] = ModelUtils.getDateTimeAsObject(c2.getFirstDate() + "T00:00:00");
        spec.setFromDate(fromDate);
        
        Date[] toDate = new Date[3];
        toDate[0] = ModelUtils.getDateTimeAsObject(c0.getLastDate() + "T23:59:59");
        toDate[1] = ModelUtils.getDateTimeAsObject(c1.getLastDate() + "T23:59:59");
        toDate[2] = ModelUtils.getDateTimeAsObject(c2.getLastDate() + "T23:59:59");
        spec.setToDate(toDate);
        
                /*String[] fromDate = new String[3];
                fromDate[0] = c0.getFirstDate() + "T00:00:00";
                fromDate[1] = c1.getFirstDate() + "T00:00:00";
                fromDate[2] = c2.getFirstDate() + "T00:00:00";
                spec.setFromDate(fromDate);
                 
                String[] toDate = new String[3];
                toDate[0] = c0.getLastDate() + "T23:59:59";
                toDate[1] = c1.getLastDate() + "T23:59:59";
                toDate[2] = c2.getLastDate() + "T23:59:59";
                spec.setToDate(toDate);*/
        
        final DocumentDelegater ddl = new DocumentDelegater();
        
        DBTask task = new DBTask<List[]>(getContext()) {
            
            @Override
            public List[] doInBackground() throws Exception {
                List[] ret = new List[2];
                List modules = ddl.getModuleList(spec);
                ret[0] = modules;
		if (appo) {
                    List appointments = ddl.getAppoinmentList(spec);
                    ret[1] = appointments;
		}
                return ret;
            }
            
            @Override
            public void succeeded(List[] result) {
                setAllModules(result[0]);
                if (appo) {
                    setAllAppointments(result[1]);
                }
            }
        };
        
        task.execute();
    }
    
    /**
     * �ݒ肳��Ă��钊�o���Ԃ���摜�������擾����B
     */
    private void getImageList() {
        
        if (selectedPeriod == null) {
            return;
        }
        
        final ImageSearchSpec spec = new ImageSearchSpec();
        spec.setCode(ImageSearchSpec.PATIENT_SEARCH);
        spec.setKarteId(getContext().getKarte().getId());
        spec.setStatus("F");
        
        // �J�����_�ʂɌ�������
        Date[] fromDate = new Date[3];
        fromDate[0] = ModelUtils.getDateTimeAsObject(c0.getFirstDate() + "T00:00:00");
        fromDate[1] = ModelUtils.getDateTimeAsObject(c1.getFirstDate() + "T00:00:00");
        fromDate[2] = ModelUtils.getDateTimeAsObject(c2.getFirstDate() + "T00:00:00");
        spec.setFromDate(fromDate);
        
        Date[] toDate = new Date[3];
        toDate[0] = ModelUtils.getDateTimeAsObject(c0.getLastDate() + "T23:59:59");
        toDate[1] = ModelUtils.getDateTimeAsObject(c1.getLastDate() + "T23:59:59");
        toDate[2] = ModelUtils.getDateTimeAsObject(c2.getLastDate() + "T23:59:59");
        spec.setToDate(toDate);
        
        // �J�����_�ʂɌ�������
                /*String[] fromDate = new String[3];
                fromDate[0] = c0.getFirstDate() + "T00:00:00";
                fromDate[1] = c1.getFirstDate() + "T00:00:00";
                fromDate[2] = c2.getFirstDate() + "T00:00:00";
                spec.setFromDate(fromDate);
                 
                String[] toDate = new String[3];
                toDate[0] = c0.getLastDate() + "T23:59:59";
                toDate[1] = c1.getLastDate() + "T23:59:59";
                toDate[2] = c2.getLastDate() + "T23:59:59";
                spec.setToDate(toDate);*/
        
        spec.setIconSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
        
        final DocumentDelegater ddl = new DocumentDelegater();
        
        DBTask task = new DBTask<List>(getContext()) {
            
            @Override
            public List doInBackground() throws Exception {
                return ddl.getImageList(spec);
            }
            
            @Override
            public void succeeded(List result) {
                setAllImages(result);
            }
        };
        
        task.execute();
    }
    
//    public void setUpdated(boolean b) {
//        if (updated != b) {
//            updated = b;
//            updateAppoBtn.setEnabled(updated);
//        }
//    }
    
    @Override
    public void setDirty(boolean dirty) {
        if (isDirty() != dirty) {
            super.setDirty(dirty);
            updateAppoBtn.setEnabled(isDirty());
        }
    }
    
    /**
     * �V�K�y�ѕύX���ꂽ�\���ۑ�����B
     */
    @Override
    public void save() {
        
        final ArrayList<AppointmentModel> results = new ArrayList<AppointmentModel>();
        Enumeration e = cPool.elements();
        
        while (e.hasMoreElements()) {
            
            // �J�����_�[�P�ʂɒ��o����
            SimpleCalendarPanel c = (SimpleCalendarPanel) e.nextElement();
            if (c.getRelativeMonth() >= 0) {
                
                ArrayList<AppointmentModel> list = c.getUpdatedAppoints();
                int size = list.size();
                for (int i = 0; i < size; i++) {
                    AppointmentModel appo = list.get(i);
                    
                    // �V�K�\��̂�EJB3.0�̊֌W��ݒ肷��
                    if (appo.getKarte() == null) {
                        appo.setKarte(getContext().getKarte());
                    }
                    appo.setCreator(Project.getUserModel());
                    
                    // �m����A�L�^���A�J�n��
                    // ����̎����͂����܂�
                    Date confirmed = new Date();
                    appo.setConfirmed(confirmed);
                    appo.setRecorded(confirmed);
                    if (appo.getStarted() == null) {
                        appo.setStarted(confirmed);
                    }
                    // ���FINAL
                    appo.setStatus(IInfoModel.STATUS_FINAL);
                    
                    results.add(list.get(i));
                }
            }
        }
        
        if (results.size() == 0) {
            return;
        }
        
        final AppointmentDelegater adl = new AppointmentDelegater();
        
        DBTask task = new DBTask<Void>(getContext()) {

            @Override
            protected Void doInBackground() throws Exception {
                adl.putAppointments(results);
                return null;
            }
            
            @Override
            public void succeeded(Void result) {
                setDirty(false);
            }
        };
        
        task.execute();
    }
    
    private String getMarkCode() {
        // ���𖼂������R�[�h(EntityName)�ɕϊ�
        int index = orderCombo.getSelectedIndex();
        return orderCodes[index];
    }
    
    /**
     * ComboBoxRenderer
     *
     */
    protected class ComboBoxRenderer extends JLabel implements ListCellRenderer {
        
        private static final long serialVersionUID = 4661822065789099499L;
        
        public ComboBoxRenderer() {
            setOpaque(true);
            // setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
        }
        
                /*
                 * This method finds the image and text corresponding to the selected
                 * value and returns the label, set up to display the text and image.
                 */
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            // Get the selected index. (The index param isn't
            // always valid, so just use the value.)
            
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            // Set the icon and text. If icon was null, say so.
            Icon icon = getOrderIcon((String) value);
            
            if (icon != null) {
                setIcon(icon);
                setText((String) value);
            } else {
                setText((String) value);
            }
            
            return (Component) this;
        }
        
        private Icon getOrderIcon(String name) {
            Icon ret = null;
                        /*for (int i = 0; i < orderNames.length; i++) {
                                if (name.equals(orderNames[i])) {
                                        ret = orderIcons[i];
                                        break;
                                }
                        }*/
            
            return ret;
        }
    }
}