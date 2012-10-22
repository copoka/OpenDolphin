/*
 * WatingListService.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2003-2005 Digital Globe, Inc. All rights reserved.
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

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import javax.swing.table.*;

import open.dolphin.delegater.PVTDelegater;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.PatientVisitModel;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import org.apache.log4j.Logger;

/**
 * ��t���X�g�B
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class WatingListPlugin extends DefaultMainWindowPlugin {
    
    /** �f�@�I���A�C�R�� */
    private static final ImageIcon FLAG_ICON = ClientContext.getImageIcon("flag_16.gif");
    
    /** �J���e�I�[�v���A�C�R�� */
    private static final ImageIcon OPEN_ICON = ClientContext.getImageIcon("open_16.gif");
    
    /** JTable�����_���p�̒j���J���[ */
    private static final Color MALE_COLOR = ClientContext.getColor("watingList.color.male");
    
    /** JTable�����_���p�̏����J���[ */
    private static final Color FEMALE_COLOR = ClientContext.getColor("watingList.color.female");
    
    /** JTable�����_���p�̊�J���[ */
    private static final Color ODD_COLOR = ClientContext.getColor("color.odd");
    
    /** JTable�����_���p�̋����J���[ */
    private static final Color EVEN_COLOR = ClientContext.getColor("color.even");
    
    /** ��t�L�����Z���J���[ */
    private static final Color CANCEL_PVT_COLOR = ClientContext.getColor("watingList.color.pvtCancel");
    
    /** ���@���̃`�F�b�N�Ԋu�I�u�W�F�N�g */
    private NameValuePair[] intervalObjects = ClientContext.getNameValuePair("watingList.interval");
    
    /** �f�t�H���g�̃`�F�b�N�Ԋu */
    private int CHECK_INTERVAL = 30; // �f�t�H���g�l
    
    // ���@���e�[�u���̃X�e�[�^�X�J����
    private int STATE_COLUMN = 7;
    
    /** �N��\���J���� */
    private final int AGE_COLUMN = 4;
    
    /** �N��N�������\�b�h */
    private final String[] AGE_METHOD = new String[]{"getPatientAgeBirthday", "getPatientBirthday"};
    
    //
    // GUI �R���|�[�l���g
    //
    /** PVT Table */
    private ObjectListTable pvtTable;
    
    /** �C�A�C�R���{�^�� */
    private JButton shoes;
    
    /** ���@�����\�����x�� */
    private JLabel countLabel;
    
    /** �`�F�b�N�����\�����x�� */
    private JLabel checkTimeLabel;
    
    /** �`�F�b�N�Ԋu�\�����x�� */
    private JLabel checkIntervalLabel;
    
    /** �^�]���\�����x�� */
    private JLabel dayLabel;
    
    //
    // �����֘A
    //
    private String PVT_DELEGATER_JNDI = "delegater/pvt";
    
    /** Preference */
    private Preferences preferences = Preferences.userNodeForPackage(this.getClass());
    
    /** ���ʃ����_���t���O */
    private boolean sexRenderer;
    
    /** �N��\�� */
    private boolean ageDisplay;
    
    /** �^�]�� */
    private Date operationDate;
    
    /** ��t DB ���`�F�b�N���� Date  */
    private Date checkedTime;
    
    /** ���@���Ґ� */
    private int pvtCount;
    
    /** �`�F�b�N�Ԋu */
    private int checkInterval;
    
    /** �I������Ă��銳�ҏ�� */
    private PatientVisitModel selectedPvt;
    
    private int saveSelectedIndex;
    
    private ScheduledFuture timerHandler;
    
    private PvtChecker pvtChecker;
    
    private Logger logger;
    
    /** 
     * Creates new WatingList 
     */
    public WatingListPlugin() {
        logger = ClientContext.getLogger("boot");
        sexRenderer = preferences.getBoolean("sexRenderer", false);
        ageDisplay = preferences.getBoolean("ageDisplay", true);
        checkInterval = preferences.getInt("checkInterval", CHECK_INTERVAL);
        
    }
    
    /**
     * �v���O�������J�n����B
     */
    public void start() {
        // �R���|�[�l���g�����������ڑ�����
        initComponents();
        connect();
        super.start();
    }
    
    /**
     * ���C���E�C���h�E�̃^�u�Ŏ�t���X�g�ɐ؂�ւ������
     * �R�[�������B
     */
    public void enter() {
        controlMenu();
    }
    
    /**
     * �I������Ă��闈�@���̊��҃I�u�W�F�N�g��Ԃ��B
     * @return ���҃I�u�W�F�N�g
     */
    public PatientModel getPatinet() {
        return selectedPvt != null ? selectedPvt.getPatient() : null;
    }
    
    /**
     * ���ʃ����_�����ǂ�����Ԃ��B
     * @return ���ʃ����_���̎� true
     */
    public boolean isSexRenderer() {
        return sexRenderer;
    }
    
    /**
     * �����_�����g�O���Ő؂�ւ���B
     */
    public void switchRenderere() {
        sexRenderer = !sexRenderer;
        preferences.putBoolean("sexRenderer", sexRenderer);
        if (pvtTable != null) {
            pvtTable.getTableModel().fireTableDataChanged();
        }
    }
    
    /**
     * �N��\�����I���I�t����B
     */
    public void switchAgeDisplay() {
        ageDisplay = !ageDisplay;
        preferences.putBoolean("ageDisplay", ageDisplay);
        if (pvtTable != null) {
            //String coumnName = ageDisplay ? "�N��(���N����)" : "���N����";
            String method = ageDisplay ? AGE_METHOD[0] : AGE_METHOD[1];
            //pvtTable.getTableModel().setColumnName(coumnName, AGE_COLUMN);
            pvtTable.getTableModel().setMethodName(method, AGE_COLUMN);
        }
    }
    
    /**
     * ���@�����擾�������ݒ肷��B
     * @param date �擾�����
     */
    public void setOperationDate(Date date) {
        operationDate = date;
        String formatStr = ClientContext.getString("watingList.state.dateFormat");
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr); // 2006-11-20(��)
        dayLabel.setText(sdf.format(operationDate));
    }
    
    /**
     * ���@�����`�F�b�N����������ݒ肷��B
     * @param date �`�F�b�N��������
     */
    public void setCheckedTime(Date date) {
        checkedTime = date;
        String formatStr = ClientContext.getString("watingList.state.timeFormat");
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
        checkTimeLabel.setText(sdf.format(checkedTime));
    }
    
    /**
     * ���@���̃`�F�b�N�Ԋu(Timer delay)��ݒ肷��B
     * @param interval �`�F�b�N�Ԋu sec
     */
    public void setCheckInterval(int interval) {
        
        checkInterval = interval;
        String intervalSt = String.valueOf(checkInterval);
        for (NameValuePair pair : intervalObjects) {
            if (intervalSt.equals(pair.getValue())) {
                String text = ClientContext.getString("watingList.state.checkText");
                text += pair.getName(); // �`�F�b�N�Ԋu:30�b
                checkIntervalLabel.setText(text);
                break;
            }
        }
    }
    
    /**
     * ���@����ݒ肷��B
     * @param cnt ���@��
     */
    public void setPvtCount(int cnt) {
        pvtCount = cnt;
        String text = ClientContext.getString("watingList.state.pvtCountText");
        text += String.valueOf(pvtCount); // ���@��:20
        countLabel.setText(text);
    }
    
    /**
     * �e�[�u���y�ьC�A�C�R���� enable/diable ������s���B
     * @param busy pvt �������� true
     */
    public void setBusy(boolean busy) {
        
        shoes.setEnabled(!busy);
        
        if (busy) {
            getContext().block();
            saveSelectedIndex = pvtTable.getTable().getSelectedRow();
        } else {
            getContext().unblock();
            pvtTable.getTable().getSelectionModel().addSelectionInterval(saveSelectedIndex, saveSelectedIndex);
        }
    }
    
    /**
     * �I������Ă��闈�@����ݒ�Ԃ��B
     * @return �I������Ă��闈�@���
     */
    public PatientVisitModel getSelectedPvt() {
        return selectedPvt;
    }
    
    /**
     * �I�����ꂽ���@����ݒ肷��B
     * @param �I�����ꂽ���@���
     */
    public void setSelectedPvt(PatientVisitModel selectedPvt) {
        PatientVisitModel old = this.selectedPvt;
        this.selectedPvt = selectedPvt;
        controlMenu();
    }
    
    public void maybeSelectedPvt(Object newValue) {
        Object[] obj = (Object[]) newValue;
        // �������t���b�V�����邽�� null ���ǂ����Ɋ֌W�Ȃ��Z�b�g���ʒm����K�v������
        selectedPvt = (obj != null && obj.length > 0) 
                        ? (PatientVisitModel) obj[0]
                        : null;
        setSelectedPvt(selectedPvt);
    }
      
    /**
     * �`�F�b�N�^�C�}�[�����X�^�[�g����B
     */
    public void restartCheckTimer() {
        
        if (timerHandler != null) {
            timerHandler.cancel(false);
        }
        
        if (pvtChecker == null) {
            pvtChecker = new PvtChecker();
        }
        
        ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();
        timerHandler = schedule.scheduleWithFixedDelay(pvtChecker, 0, checkInterval, TimeUnit.SECONDS);
    }
    
    /**
     * �J���e�I�[�v�����j���[�𐧌䂷��B
     */
    private void controlMenu() {
        PatientVisitModel pvt = getSelectedPvt();
        Action action = getContext().getAction("openKarte");
        boolean enabled = canOpen(pvt);
        if (action != null) {
            action.setEnabled(enabled);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * GUI �R���|�[�l���g�������������A�C�A�E�g����B
     */
    private void initComponents() {
        
        //
        // ���@�e�[�u���p�̃p�����[�^���擾����
        //
        String[] columnNames = ClientContext.getStringArray("watingList.columnNames");
        String[] methodNames = ClientContext.getStringArray("watingList.methodNames");
        Class[] classes = ClientContext.getClassArray("watingList.columnClasses");
        int[] columnWidth = ClientContext.getIntArray("watingList.columnWidth");
        int startNumRows = ClientContext.getInt("watingList.startNumRows");
        int rowHeight = ClientContext.getInt("watingList.rowHeight");
        Dimension cellSpacing = ClientContext.getDimension("watingList.cellSpacing");
        
        //
        // �N��\�������Ȃ��ꍇ�̓��\�b�h��ύX����
        //
        if (!ageDisplay) {
            //columnNames[AGE_COLUMN] = "���N����";
            methodNames[AGE_COLUMN] = AGE_METHOD[1];
        }
        
        //
        // ��������
        //
        pvtTable = new ObjectListTable(columnNames,startNumRows, methodNames, classes, false);
        pvtTable.getTable().setRowHeight(rowHeight);
        //pvtTable.getTable().setIntercellSpacing(cellSpacing);
        
        // �R���e�L�X�g���j���[��o�^����
        pvtTable.getTable().addMouseListener(new ContextListener());     
        
        
        // ���@���e�[�u���̑�����ݒ肷��
        pvtTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pvtTable.setColumnWidth(columnWidth);
        
        // ���ʃ����_���𐶐�����
        MaleFemaleRenderer sRenderer = new MaleFemaleRenderer();
        pvtTable.getTable().getColumnModel().getColumn(0).setCellRenderer(sRenderer);
        pvtTable.getTable().getColumnModel().getColumn(2).setCellRenderer(sRenderer);
        pvtTable.getTable().getColumnModel().getColumn(4).setCellRenderer(sRenderer);
        pvtTable.getTable().getColumnModel().getColumn(5).setCellRenderer(sRenderer);
        pvtTable.getTable().getColumnModel().getColumn(6).setCellRenderer(sRenderer);
        
        // Center Renderer
        CenterRenderer centerRenderer = new CenterRenderer();
        pvtTable.getTable().getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        pvtTable.getTable().getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        
        // �J���e��ԃ����_��
        KarteStateRenderer renderer = new KarteStateRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        pvtTable.getTable().getColumnModel().getColumn(STATE_COLUMN).setCellRenderer(renderer);
        JScrollPane pvtScroller = pvtTable.getScroller();
        
        // �X�e�[�^�X��񃉃x���𐶐�����
        Font font = new Font("Dialog", Font.PLAIN, ClientContext.getInt("watingList.state.font.size"));
        shoes = new JButton(ClientContext.getImageIcon("kutu01.gif"));
        countLabel = new JLabel("");
        checkTimeLabel = new JLabel("");
        checkIntervalLabel = new JLabel("");
        dayLabel = new JLabel("");
        countLabel.setFont(font);
        checkTimeLabel.setFont(font);
        checkIntervalLabel.setFont(font);
        dayLabel.setFont(font);
        countLabel.setHorizontalAlignment(SwingConstants.CENTER);
        checkTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        checkIntervalLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dayLabel.setHorizontalAlignment(SwingConstants.CENTER);
        shoes.setToolTipText(ClientContext.getString("watingList.tooltip.shoesBtn"));
        checkTimeLabel.setToolTipText(ClientContext.getString("watingList.tooltip.checkTimeLabel"));
        countLabel.setToolTipText(ClientContext.getString("watingList.tooltip.countLabel"));
        checkIntervalLabel.setToolTipText(ClientContext.getString("watingList.tooltip.checkIntervalLabel"));
        
        // ���t���x���ɒl��ݒ肷��
        setOperationDate(new Date());
        
        // �`�F�b�N�Ԋu����ݒ肷��
        setCheckInterval(checkInterval);
        
        // ���@����ݒ肷��
        setPvtCount(0);
        
        // �}��p�l���𐶐�����
        String openText = ClientContext.getString("watingList.state.openText");
        JPanel exp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        exp.add(new JLabel(openText, OPEN_ICON, SwingConstants.CENTER));
        exp.add(new JLabel("�f�@�I��", FLAG_ICON, SwingConstants.CENTER));
        
        // Status �p�l���̍����𐶐�����
        JPanel kutuP = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 10));
        kutuP.add(checkTimeLabel);
        kutuP.add(new SeparatorPanel());
        kutuP.add(countLabel);
        // Status �p�l���̉E���𐶐�����
        JPanel rightS = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 10));
        rightS.add(checkIntervalLabel);
        rightS.add(new SeparatorPanel());
        rightS.add(dayLabel);
        // Status �p�l���𐶐�����
        JPanel statusP = new JPanel();
        statusP.setLayout(new BoxLayout(statusP, BoxLayout.X_AXIS));
        statusP.add(shoes);
        statusP.add(Box.createHorizontalStrut(5));
        statusP.add(kutuP);
        statusP.add(Box.createHorizontalGlue());
        statusP.add(rightS);
        
        // �S�̂����C�A�E�g����
        JPanel myPanel = getUI();
        myPanel.setLayout(new BorderLayout(0, 7));
        myPanel.add(exp, BorderLayout.NORTH);
        myPanel.add(pvtScroller, BorderLayout.CENTER);
        myPanel.add(statusP, BorderLayout.SOUTH);
        
        getUI().setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        
    }
    
    /**
     * �R���|�[�l���g�ɃC�x���g�n���h���[��o�^�����݂ɐڑ�����B
     */
    private void connect() {
        
        //
        // Chart �̃��X�i�ɂȂ�
        // ���҃J���e�� Open/Save/SaveTemp �̒ʒm���󂯂Ď�t���X�g�̕\���𐧌䂷��
        //
        ChartPlugin.addPropertyChangeListener(ChartPlugin.CHART_STATE, 
            (PropertyChangeListener) EventHandler.create(PropertyChangeListener.class, this, "updateState", "newValue"));
        
        //
        // �I���������@�����C���X�y�N�^�֒ʒm����
        //
        pvtTable.addPropertyChangeListener(ObjectListTable.SELECTED_OBJECT, 
            (PropertyChangeListener) EventHandler.create(PropertyChangeListener.class, this, "maybeSelectedPvt", "newValue"));
        
        //
        // �_�u���N���b�N���ꂽ���@��񂩂�J���e���J��
        //
        pvtTable.addPropertyChangeListener(ObjectListTable.DOUBLE_CLICKED_OBJECT,
            (PropertyChangeListener) EventHandler.create(PropertyChangeListener.class, this, "openKarte", "newValue"));
        
        //
        // �C�̃A�C�R�����N���b�N���������@������������
        //
        shoes.addActionListener((ActionListener) EventHandler.create(ActionListener.class, this, "checkFullPvt"));
    }
    
    public void openKarte(PatientVisitModel pvtModel) {
        
        if (pvtModel != null && canOpen(pvtModel)) {
            getContext().openKarte(pvtModel);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }
        
    /**
     * �`���[�g�X�e�[�g�̏�Ԃ��f�[�^�x�[�X�ɏ������ށB
     */
    public void updateState(final PatientVisitModel updated) {
        
        //
        // ��t���X�g�̏�ԃJ�������X�V����
        //
        List pvtList = pvtTable.getTableModel().getObjectList();
        int cnt = pvtList.size();
        boolean found = false;
        for (int i = 0; i < cnt; i++) {
            //
            // �J���e���I�[�v�����L�^���Ă���r����
            // ��t���X�g���X�V����A�v�f���ʃI�u�W�F�N�g��
            // �Ȃ��Ă���ꍇ�����邽�߁A���R�[�hID�Ŕ�r����
            //
            PatientVisitModel test = (PatientVisitModel) pvtList.get(i);
            if (updated.getId() == test.getId()) {
                test.setState(updated.getState());
                pvtTable.getTableModel().fireTableRowsUpdated(i, i);
                found = true;
                break;
            }
        }
        
        //
        // �f�[�^�x�[�X���X�V����
        //
        if (found && updated.getState() == ChartPlugin.CLOSE_SAVE) {
            
            Runnable r = new Runnable() {
                public void run() {
                    PVTDelegater pdl = new PVTDelegater();
                    pdl.updatePvtState(updated.getId(), updated.getState());
                }
            };

            Thread t = new Thread(r);
            t.setPriority(Thread.NORM_PRIORITY);
            t.start();
        }
    }
    
    public void checkFullPvt() {
        
        if (timerHandler != null) {
            timerHandler.cancel(false);
        }
        
        try {
            
            FutureTask<Integer> task = new FutureTask<Integer>(new PvtChecker2());
            new Thread(task).start();

            Integer result = task.get(120, TimeUnit.SECONDS); // 2��
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (pvtChecker == null) {
            pvtChecker = new PvtChecker();
        }
        
        ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();
        timerHandler = schedule.scheduleWithFixedDelay(pvtChecker, checkInterval, checkInterval, TimeUnit.SECONDS);
    }
    
    /**
     * �J���e���J�����Ƃ��\���ǂ�����Ԃ��B
     * @return �J�����Ƃ��\�Ȏ� true
     */
    private boolean canOpen(PatientVisitModel pvt) {
        if (pvt == null) {
            return false;
        }
        if (isKarteOpened(pvt)) {
            return false;
        }
        if (isKarteCanceled(pvt)) {
            return false;
        }
        return true;
    }
    
    /**
     * �J���e���I�[�v������Ă��邩�ǂ�����Ԃ��B
     * @return �I�[�v������Ă��鎞 true
     */
    private boolean isKarteOpened(PatientVisitModel pvtModel) {
        if (pvtModel != null) {
            boolean opened = false;
            List<ChartPlugin> allCharts = ChartPlugin.getAllChart();
            for (ChartPlugin chart : allCharts) {
                if (chart.getPatientVisit().getId() == pvtModel.getId()) {
                    opened = true;
                    break;
                }
            }
            return opened;
        }
        return false;
    }
    
    /**
     * ��t���L�����Z������Ă��邩�ǂ�����Ԃ��B
     * @return �L�����Z������Ă��鎞 true
     */
    private boolean isKarteCanceled(PatientVisitModel pvtModel) {
        if (pvtModel != null) {
            if (pvtModel.getState() == ChartPlugin.CANCEL_PVT) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * ��t���X�g�̃R���e�L�X�g���j���[�N���X�B
     */
    class ContextListener extends MouseAdapter {
        
        public void mousePressed(MouseEvent e) {
            mabeShowPopup(e);
        }
        
        public void mouseReleased(MouseEvent e) {
            mabeShowPopup(e);
        }
        
        public void mabeShowPopup(MouseEvent e) {
            
            if (e.isPopupTrigger()) {
                
                final JPopupMenu contextMenu = new JPopupMenu();
                String pop3 = ClientContext.getString("watingList.popup.oddEvenRenderer");
                String pop4 = ClientContext.getString("watingList.popup.sexRenderer");
                String pop5 = "�N��\��";
                
                int row = pvtTable.getTable().rowAtPoint(e.getPoint());
                Object obj = pvtTable.getTableModel().getObject(row);
                int selected = pvtTable.getTable().getSelectedRow();
                
                if (row == selected && obj != null) {
                    String pop1 = ClientContext.getString("watingList.popup.openKarte");
                    String pop2 = ClientContext.getString("watingList.popup.cancelVisit");
                    contextMenu.add(new JMenuItem(new ReflectAction(pop1, WatingListPlugin.this, "openKarte")));
                    contextMenu.add(new JMenuItem(new ReflectAction(pop2, WatingListPlugin.this, "cancelVisit")));
                    contextMenu.addSeparator();
                }
                
                JRadioButtonMenuItem oddEven = new JRadioButtonMenuItem(new ReflectAction(pop3, WatingListPlugin.this, "switchRenderere"));
                JRadioButtonMenuItem sex = new JRadioButtonMenuItem(new ReflectAction(pop4, WatingListPlugin.this, "switchRenderere"));
                ButtonGroup bg = new ButtonGroup();
                bg.add(oddEven);
                bg.add(sex);
                contextMenu.add(oddEven);
                contextMenu.add(sex);
                if (sexRenderer) {
                    sex.setSelected(true);
                } else {
                    oddEven.setSelected(true);
                }
                
                JCheckBoxMenuItem item = new JCheckBoxMenuItem(pop5);
                contextMenu.add(item);
                item.setSelected(ageDisplay);
                item.addActionListener((ActionListener)EventHandler.create(ActionListener.class, WatingListPlugin.this, "switchAgeDisplay"));
                
                contextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
    
    /**
     * Popup���j���[����I������Ă��銳�҂̃J���e���J���B
     */
    public void openKarte() {
        PatientVisitModel pvtModel = getSelectedPvt();
        if (canOpen(pvtModel)) {
            openKarte(pvtModel);
        }
    }
    
    /**
     * �I���������҂̎�t���L�����Z������B
     */
    public void cancelVisit() {
        
        final int selected = pvtTable.getTable().getSelectedRow();
        Object obj = pvtTable.getTableModel().getObject(selected);
        final PatientVisitModel pvtModel = (PatientVisitModel) obj;
        
        //
        // �_�C�A���O��\�����m�F����
        //
        
        Object[] cstOptions = new Object[]{"�͂�", "������"};
        
        StringBuilder sb = new StringBuilder(pvtModel.getPatientName());
        sb.append("�l�̎�t���������܂���?");
        
        int select = JOptionPane.showOptionDialog(
                SwingUtilities.getWindowAncestor(pvtTable.getTable()),
                sb.toString(),
                ClientContext.getFrameTitle(getTitle()),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                ClientContext.getImageIcon("cancl_32.gif"),
                cstOptions,
                "�͂�");
        
        //
        // ��t��������
        //
        if (select == 0) {
//            if (checkTimer != null) {
//                checkTimer.cancel();
//                checkTimer = null;
//            };
            Runnable r = new Runnable() {
                public void run() {
                    pvtModel.setState(ChartPlugin.CANCEL_PVT);
                    PVTDelegater pdl = new PVTDelegater();
                    pdl.updatePvtState(pvtModel.getId(), pvtModel.getState());
                    pvtTable.getTableModel().fireTableRowsUpdated(selected, selected);
//                    Runnable awt = new Runnable() {
//                        public void run() {
//                            restartCheckTimer();
//                        }
//                    };
//                    SwingUtilities.invokeLater(awt);
                }
            };
            Thread t = new Thread(r);
            t.setPriority(Thread.NORM_PRIORITY);
            t.run();
        }
    }   
    
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * ���җ��@�������I�Ƀ`�F�b�N����^�C�}�[�^�X�N�N���X�B
     */
    protected class PvtChecker implements Runnable {
        
        /**
         * Creates new Task
         */
        public PvtChecker() {
        }
        
        private PVTDelegater getDelegater() {
            return new PVTDelegater();
        }
        
        /**
         * �c�a�̌����^�X�N
         */
        public void run() {
            
            Runnable awt1 = new Runnable() {
                public void run() {
                    setBusy(true);
                }
            };
            SwingUtilities.invokeLater(awt1);
            
            final Date date = new Date();
            final String[] dateToSerach = getSearchDateAsString(date);
            
            //
            // Hibernate �� firstResult �����݂̌�����ۑ�����
            //
            List dataList = pvtTable.getTableModel().getObjectList();
            int firstResult = dataList != null ? dataList.size() : 0;
            
            logger.info("check PVT at " + date);
            logger.info("first result = " + firstResult);
            
            //
            // ��������
            //
            final ArrayList result = (ArrayList) getDelegater().getPvt(dateToSerach, firstResult);
            int newVisitCount = result != null ? result.size() : 0;
            logger.info("new visits = " + newVisitCount);
            
            //
            // ���ʂ�ǉ�����
            //
            if (newVisitCount > 0) {
                
                for (int i = 0; i < newVisitCount; i++) {
                    dataList.add(result.get(i));
                }
                
                pvtTable.getTableModel().fireTableRowsInserted(firstResult, dataList.size() - 1);
                
            }
            
            Runnable awt2 = new Runnable() {
                @SuppressWarnings("unchecked")
                public void run() {
                    setCheckedTime(date);
                    setPvtCount(pvtTable.getTableModel().getObjectCount());
                    setBusy(false);
                }
            };
            SwingUtilities.invokeLater(awt2);
        }
    }
    
        
    /**
     * ���җ��@�������I�Ƀ`�F�b�N����^�C�}�[�^�X�N�N���X�B
     */
    protected class PvtChecker2 implements Callable<Integer> {
        
        /**
         * Creates new Task
         */
        public PvtChecker2() {
        }
        
        private PVTDelegater getDelegater() {
            return new PVTDelegater();
        }
        
        /**
         * �c�a�̌����^�X�N
         */
        public Integer call() {
            
            Runnable awt1 = new Runnable() {
                public void run() {
                    setBusy(true);
                }
            };
            SwingUtilities.invokeLater(awt1);
            
            final Date date = new Date();
            final String[] dateToSerach = getSearchDateAsString(date);
            
            //
            // �ŏ��Ɍ����f�@���I�����R�[�h�� Hibernate �� firstResult �ɂ���
            // ���݂̌�����ۑ�����
            //
            List dataList = pvtTable.getTableModel().getObjectList();
            int firstResult = 0;
            int curCount = dataList != null ? dataList.size() : 0;
            
            if (dataList != null && curCount > 0) {
                boolean found = false;
                int cnt = curCount;
                for (int i = 0; i < cnt; i++) {
                    PatientVisitModel pvt = (PatientVisitModel) dataList.get(i);
                    if (pvt.getState() == ChartPlugin.CLOSE_NONE) {
                        //
                        // �f�@���I�����R�[�h���������ꍇ
                        // firstResult = i;
                        //
                        firstResult = i;
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    //
                    // firstResult = ���������ꍇ�̓��R�[�h����
                    //
                    firstResult = cnt;
                }
            }
            
            logger.info("check full PVT at " + date);
            logger.info("first result = " + firstResult);
            
            //
            // ��������
            //
            final ArrayList result = (ArrayList) getDelegater().getPvt(dateToSerach, firstResult);
            
            int checkCount = result != null ? result.size() : 0;
            logger.info("check visits = " + checkCount);
            
            //
            // ���ʂ���������
            //
            if (checkCount > 0) {
                //
                // firstResult ���� cnt �܂ł͌��݂̃��R�[�h���g�p���� 
                //
                int index = 0;
                for (int i = firstResult; i < curCount; i++) {
                    PatientVisitModel pvtC = (PatientVisitModel) dataList.get(i);
                    PatientVisitModel pvtU = (PatientVisitModel) result.get(index++);
                    //
                    // �I�����Ă�����ݒ肷��
                    //
                    if (pvtU.getState() == ChartPlugin.CLOSE_SAVE && (!isKarteOpened(pvtU))) {
                        pvtC.setState(pvtU.getState());
                    }
                }
                
                //
                // cnt �ȍ~�͐V�������R�[�h�Ȃ̂ł��̂܂ܒǉ�����
                //
                for (int i = index; i < result.size(); i++) {
                    dataList.add(result.get(index++));
                }
                
                pvtTable.getTableModel().fireTableDataChanged();
                
            }
            
            Runnable awt2 = new Runnable() {
                @SuppressWarnings("unchecked")
                public void run() {
                    setCheckedTime(date);
                    setPvtCount(pvtTable.getTableModel().getObjectCount());
                    setBusy(false);
                }
            };
            SwingUtilities.invokeLater(awt2);
            
            return new Integer(checkCount);
        }
    }
    
    private String[] getSearchDateAsString(Date date) {
            
        String[] ret = new String[3];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        ret[0] = sdf.format(date);

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);

        gc.add(Calendar.DAY_OF_MONTH, -2);
        date = gc.getTime();
        ret[1] = sdf.format(date);

        gc.add(Calendar.DAY_OF_MONTH, 2);
        date = gc.getTime();
        ret[2] = sdf.format(date);

        return ret;
    }
    
    /**
     * KarteStateRenderer
     * �J���e�i�`���[�g�j�̏�Ԃ������_�����O����N���X�B
     */
    protected class KarteStateRenderer extends DefaultTableCellRenderer {
        
        private static final long serialVersionUID = 7134379493874260895L;
        
        /** Creates new IconRenderer */
        public KarteStateRenderer() {
            super();
            setOpaque(true);
        }
        
        public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean isFocused,
                int row, int col) {
            Component c = super.getTableCellRendererComponent(table,
                    value,
                    isSelected,
                    isFocused, row, col);
            
            PatientVisitModel pvt = (PatientVisitModel) pvtTable.getTableModel().getObject(row);
            
            if (isSexRenderer()) {
                
                if (pvt !=null && pvt.getPatient().getGender().equals(IInfoModel.MALE)) {
                    setBackground(MALE_COLOR);
                } else if (pvt !=null && pvt.getPatient().getGender().equals(IInfoModel.FEMALE)) {
                    setBackground(FEMALE_COLOR);
                } else {
                    setBackground(Color.WHITE);
                }
                
            } else {
                if (row % 2 == 0) {
                    setBackground(EVEN_COLOR);
                } else {
                    setBackground(ODD_COLOR);
                }
            }
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            }
            
            Color fore = pvt != null && pvt.getState() == ChartPlugin.CANCEL_PVT ? CANCEL_PVT_COLOR : Color.BLACK;
            this.setForeground(fore);
            
            if (value != null && value instanceof Integer) {
                
                int state = ((Integer) value).intValue();
                
                switch (state) {
                    
                    case ChartPlugin.CLOSE_NONE:
                        //
                        // �A�C�R���Ȃ�
                        //
                        setIcon(null);
                        break;
                        
                    case ChartPlugin.CLOSE_SAVE:
                        //
                        // �f�@���I�����Ă���ꍇ�͊�
                        //
                        setIcon(FLAG_ICON);
                        break;
                        
                    case ChartPlugin.OPEN_NONE:
                    case ChartPlugin.OPEN_SAVE:
                        //
                        // �I�[�v�����Ă���ꍇ�̓I�[�v��
                        //
                        setIcon(OPEN_ICON);
                        break;    
                        
                    default:
                        setIcon(null);
                        break;
                }
                ((JLabel) c).setText("");
                
            } else {
                setIcon(null);
                ((JLabel) c).setText(value == null ? "" : value.toString());
            }
            return c;
        }
    }
    
    /**
     * KarteStateRenderer
     * �J���e�i�`���[�g�j�̏�Ԃ������_�����O����N���X�B
     */
    protected class MaleFemaleRenderer extends DefaultTableCellRenderer {
        
        private static final long serialVersionUID = 7134379493874260895L;
        
        /** Creates new IconRenderer */
        public MaleFemaleRenderer() {
            super();
        }
        
        public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean isFocused,
                int row, int col) {
            Component c = super.getTableCellRendererComponent(table,
                    value,
                    isSelected,
                    isFocused, row, col);
            
            PatientVisitModel pvt = (PatientVisitModel) pvtTable.getTableModel().getObject(row);
            
            if (isSexRenderer()) {
                
                if (pvt !=null && pvt.getPatient().getGender().equals(IInfoModel.MALE)) {
                    setBackground(MALE_COLOR);
                } else if (pvt !=null && pvt.getPatient().getGender().equals(IInfoModel.FEMALE)) {
                    setBackground(FEMALE_COLOR);
                } else {
                    setBackground(Color.WHITE);
                }
                
            } else {
                
                if (row % 2 == 0) {
                    setBackground(EVEN_COLOR);
                } else {
                    setBackground(ODD_COLOR);
                }
            }
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            }
            
            Color fore = pvt != null && pvt.getState() == ChartPlugin.CANCEL_PVT ? CANCEL_PVT_COLOR : Color.BLACK;
            this.setForeground(fore);
            
            if (value != null && value instanceof String) {
                ((JLabel) c).setText((String) value);
            } else {
                setIcon(null);
                ((JLabel) c).setText(value == null ? "" : value.toString());
            }
            return c;
        }
    }
    
    protected class CenterRenderer extends MaleFemaleRenderer {
        
        private static final long serialVersionUID = -4050639296626793056L;
        
        /** Creates new IconRenderer */
        public CenterRenderer() {
            super();
            this.setHorizontalAlignment(JLabel.CENTER);
        }
    }
}