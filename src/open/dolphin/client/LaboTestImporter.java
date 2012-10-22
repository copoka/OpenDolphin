/*
 * PatientSearch.java
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

import javax.swing.*;

import open.dolphin.infomodel.LaboImportSummary;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.plugin.IPluginContext;
import open.dolphin.project.Project;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * LaboTestImport
 *
 * @author Kazushi Minagawa
 */
public class LaboTestImporter extends DefaultMainWindowPlugin implements PropertyChangeListener {
    
    // �I������Ă��銳�ҏ��
    private LaboImportSummary selectedLabo;
    private int number = 100000;
    private final String CHART_JNDI = "mainWindow/chart";
    
    // GUI �R���|�[�l���g
    private ObjectListTable laboListTable;
    private UltraSonicProgressLabel usp;
    private JLabel countLabel;
    private JLabel dateLabel;
    
    /** Creates new PatientSearch */
    public LaboTestImporter() {
    }
    
    public void start() {
        initComponents();
        connect();
        enter();
        super.start();
    }
    
    // ���C���E�C���h�E�̃^�u�y�C���Ńv���O�C�����؂�ւ�������A
    // �C���X�y�N�^�ɑI������Ă��銳�҂�ʒm����
    public void enter() {
        controlMenu();
    }
    
    public LaboImportSummary getSelectedLabo() {
        return selectedLabo;
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
                
                int row = laboListTable.getTable().rowAtPoint(e.getPoint());
                Object obj = laboListTable.getTableModel().getObject(row);
                int selected = laboListTable.getTable().getSelectedRow();
                
                if (row == selected && obj != null) {
                    String pop1 = ClientContext.getString("watingList.popup.openKarte");
                    contextMenu.add(new JMenuItem(new ReflectAction(pop1, LaboTestImporter.this, "openKarte")));
                }
                contextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
    
    public void setSelectedLabo(LaboImportSummary selectedLabo) {
        this.selectedLabo = selectedLabo;
        controlMenu();
    }
    
    public void openKarte() {
        
        final Preferences pref = Preferences.userNodeForPackage(this.getClass());
        boolean showReceiptMessage = pref.getBoolean("showReceiptMessage", true);
        showReceiptMessage = true;
        if (showReceiptMessage) {
            JLabel msg1 = new JLabel("��t���X�g����I�[�v�����Ȃ��Ɛf�Ãf�[�^�����Z�R����");
            JLabel msg2 = new JLabel("���M���邱�Ƃ��ł��܂���B�����܂���?");
            final JCheckBox cb = new JCheckBox("���ケ�̃��b�Z�[�W��\�����Ȃ�");
            cb.setFont(new Font("Dialog", Font.PLAIN, 10));
            cb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    pref.putBoolean("showReceiptMessage", !cb.isSelected());
                }
            });
            JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
            p1.add(msg1);
            JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
            p2.add(msg2);
            JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
            p3.add(cb);
            JPanel box = new JPanel();
            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
            box.add(p1);
            box.add(p2);
            box.add(p3);
            box.setBorder(BorderFactory.createEmptyBorder(0, 0, 11, 11));
            
            int option = JOptionPane.showConfirmDialog(this.getContext().getFrame(),
                    new Object[]{box},
                    ClientContext.getFrameTitle(getTitle()),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    ClientContext.getImageIcon("about_32.gif"));
            
            if (option != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        PatientModel patient = selectedLabo.getPatient();
        PatientVisitModel pvt = new PatientVisitModel();
        pvt.setNumber(number++);
        pvt.setPatient(patient);
        
        // �󂯕t����ʂ��Ă��Ȃ��̂Őf�ÉȂ̓��[�U�o�^���Ă�����̂��g�p����
        StringBuilder sb = new StringBuilder();
        sb.append(Project.getUserModel().getDepartmentModel().getDepartmentDesc());
        sb.append(",");
        sb.append(Project.getUserModel().getDepartmentModel().getDepartment());
        // Doctor name, id, JMARI code
        pvt.setDepartment(sb.toString());
        
        // �J���e�R���e�i�𐶐�����
        try {
            IPluginContext plCtx = ClientContext.getPluginContext();
            ChartPlugin chart = (ChartPlugin) plCtx.lookup(CHART_JNDI);
            chart.setContext(getContext());
            chart.setPatientVisit(pvt);
            chart.setReadOnly(Project.isReadOnly());
            chart.start();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public ObjectListTable getLaboListTable() {
        return laboListTable;
    }
    
    public UltraSonicProgressLabel getUltraSonicProgressBar() {
        return usp;
    }
    
    /**
     * �������ʌ�����ݒ肵�X�e�[�^�X�p�l���֕\������B
     * @param cnt ����
     */
    public void updateCount() {
        int count = laboListTable.getTableModel().getObjectCount();
        String text = ClientContext.getString("laboTestImport.count.text");
        text += String.valueOf(count);
        countLabel.setText(text);
    }
    
    /**
     * ���j���[�𐧌䂷��
     */
    private void controlMenu() {
        
        PatientModel pvt = getSelectedLabo() != null 
                         ? getSelectedLabo().getPatient() 
                         : null;
        
        Action action = getContext().getAction("openKarte");
        boolean enabled = canOpen(pvt);
        if (action != null) {
            action.setEnabled(enabled);
        }
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
            java.util.List<ChartPlugin> allCharts = ChartPlugin.getAllChart();
            for (ChartPlugin chart : allCharts) {
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
     * GUI �R���|�[�l���g������������B
     */
    private void initComponents() {
        
        JLabel iconLabel = new JLabel(ClientContext.getImageIcon("impt_24.gif"));
        JLabel instLabel = new JLabel("�������ʃt�@�C��(MML�`��)�����̃e�[�u���� Drag & Drop ���Ă��������B");
        instLabel.setFont(new Font("Dialog", Font.PLAIN, ClientContext.getInt("watingList.state.font.size")));
        JPanel importPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        importPanel.add(iconLabel);
        importPanel.add(instLabel);
        
        // ���{�e�X�g�e�[�u���𐶐�����
        String[] columnNames = ClientContext.getStringArray("labotestImport.columnNames");
        int startNumRows = ClientContext.getInt("labotestImport.startNumRows");
        String[] methodNames = ClientContext.getStringArray("labotestImport.methodNames");
        Class[] classes = ClientContext.getClassArray("labotestImport.classNames");
        int[] columnWidth = ClientContext.getIntArray("labotestImport.columnWidth");
        int rowHeight = ClientContext.getInt("labotestImport.rowHeight");
        laboListTable = new ObjectListTable(columnNames, startNumRows, methodNames, classes);
        laboListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        laboListTable.setColumnWidth(columnWidth);
        laboListTable.getTable().setRowHeight(rowHeight);
        JScrollPane scroller = laboListTable.getScroller();
        
        // TransferHandler��ݒ肷��
        JTable table = laboListTable.getTable();
        table.setTransferHandler(new LaboTestFileTransferHandler(this));
        
        // Status �p�l���𐶐�����
        usp = new UltraSonicProgressLabel();
        Font font = new Font("Dialog", Font.PLAIN, ClientContext.getInt("watingList.state.font.size"));
        countLabel = new JLabel("");
        dateLabel = new JLabel("");
        countLabel.setFont(font);
        dateLabel.setFont(font);
        countLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel statusP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        statusP.add(usp);
        statusP.add(countLabel);
        statusP.add(new SeparatorPanel());
        statusP.add(dateLabel);
        
        // �J�E���g�l�O��ݒ肷��
        updateCount();
        
        // ���t��ݒ肷��
        String formatStr = ClientContext.getString("watingList.state.dateFormat");
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr); // 2006-11-20(��)
        dateLabel.setText(sdf.format(new Date()));
        
        // �S�̂����C�A�E�g����
        JPanel myPanel = getUI();
        myPanel.setLayout(new BorderLayout(0, 11));
        myPanel.add(importPanel, BorderLayout.NORTH);
        myPanel.add(scroller, BorderLayout.CENTER);
        myPanel.add(statusP, BorderLayout.SOUTH);
        myPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
    }
    
    /**
     * �R���|�[�����g�Ƀ��X�i��o�^���ڑ�����B
     */
    private void connect() {
        // �_�u���N���b�N�ŃJ���e�I�[�v������
        laboListTable.addPropertyChangeListener(ObjectListTable.DOUBLE_CLICKED_OBJECT, this);
        // �R���e�L�X�g���j���[���X�i��ݒ肷��
        laboListTable.getTable().addMouseListener(new ContextListener());
        
        // �s�I��
        PropertyChangeListener pls = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(ObjectListTable.SELECTED_OBJECT)) {
                    Object[] obj = (Object[]) e.getNewValue();
                    // �������t���b�V�����邽�� null ���ǂ����Ɋ֌W�Ȃ��Z�b�g���ʒm����K�v������
                    LaboImportSummary value = (obj != null && obj.length > 0) ? (LaboImportSummary) obj[0] : null;
                    setSelectedLabo(value);
                }
            }
        };
        laboListTable.addPropertyChangeListener(ObjectListTable.SELECTED_OBJECT, pls);
    }
    
    /**
     * �_�u���N���b�N���ꂽ���҂̃J���e���J���B
     */
    public void propertyChange(PropertyChangeEvent e) {
        
        String prop = e.getPropertyName();
        
        if (prop.equals(ObjectListTable.DOUBLE_CLICKED_OBJECT)) {
            
            LaboImportSummary value = (LaboImportSummary) e.getNewValue();
            if (value != null) {
                setSelectedLabo(value);
                if (canOpen(value.getPatient())) {
                    openKarte();
                }
            }
        }
    }
}