/*
 * AdminMaster.java
 * Copyright (C) 2007 Dolphin Project. All rights reserved.
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
package open.dolphin.order;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.StringTokenizer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import open.dolphin.client.ClientContext;
import open.dolphin.client.MasterRenderer;
import open.dolphin.client.TimeoutWarning;
import open.dolphin.dao.SqlDaoFactory;
import open.dolphin.dao.SqlMasterDao;
import open.dolphin.infomodel.AdminEntry;
import open.dolphin.table.ObjectTableModel;
import open.dolphin.util.ReflectMonitor;

/**
 * �p�@�}�X�^�����N���X�B
 *
 * @author Kazushi Minagawa
 */
public class AdminMaster extends MasterPanel {
    
    public static final String ADMIN_PROP = "adminProp";
    private static final String[] COLUMN_NAMES = {"�R�[�h", "�� ��"};
    private static final String CUSTOM_CODE = "001";
    private static final String[] TONYO_RANGE = {"001000800", "001000899"};
    
    private static final String[] ADMIN_CATEGORY =   {"�I�����Ă�������","�����P��(100)", "�����Q��(200)", "�����R��(300)", "�����S��(400)", "�_�ᓙ(500,700)", "�h�z��(600)", "�ڗp��(800)", "�z����(900)", "�S��"};
    private static final String[] ADMIN_CODE_RANGE = {"","0010001",  "0010002", "0010003", "0010004", "0010005 0010007", "0010006", "0010008", "0010009", "001"};
    private static final int COMMENT_INDEX = 9;
    
    /** �p�@�J�e�S�� ComboBox */
    private JComboBox adminCombo;
    
    /** ����R�[�h���̓t�B�|���h */
    private JTextField customCode;
    
    
    /** 
     * Creates a new instance of AdminMaster 
     * @param master �}�X�^��
     * @param pulse �����g�i���o�[
     */
    public AdminMaster(String master) {
        super(master);
    }
    
    /**
     * ����������B
     */
    protected void initialize() {        
              
        //
        // �p�@�J�e�S�� ComboBox �𐶐�����   
        // �I���̕ω����������ꍇ�̓J�e�S���ɊY������p�@����������
        //
        int index = 0;
        adminCombo = new JComboBox(ADMIN_CATEGORY);
        adminCombo.setToolTipText("���ʓ��̓R�[�h�̔ԍ����\���܂��B");
        adminCombo.setSelectedIndex(index);
        adminCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int index = adminCombo.getSelectedIndex();
                    String code = ADMIN_CODE_RANGE[index];
                    if (!code.equals("")) {
                        fetchAdministration(code); 
                    }
                }
            }
        });  
        
        //
        // TableModel�𐶐�����
        // 
        tableModel = new ObjectTableModel(COLUMN_NAMES, START_NUM_ROWS) {
            
            private static final long serialVersionUID = 8084360322119845887L;
            
            @SuppressWarnings("unchecked")
            public Class getColumnClass(int col) {
                return AdminEntry.class;
            }
        };
        
        //
        // Table �𐶐�����
        //
        table = new JTable(tableModel);
        
        // �V���O���I�����[�h�ɐݒ肷��
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        //
        // �s�I�����N�������� AdminInfo �𐶐������X�i�֒ʒm����
        //
        table.setRowSelectionAllowed(true);
        ListSelectionModel m = table.getSelectionModel();
        m.addListSelectionListener(new ListSelectionListener() {
            
            public void valueChanged(ListSelectionEvent e) {
                
                if (e.getValueIsAdjusting() == false) {
                    
                    int row = table.getSelectedRow();
                    AdminEntry o = (AdminEntry) tableModel.getObject(row);
                    
                    if (o != null) {
                        AdminInfo info = new AdminInfo();
                        String code = o.getCode();
                        String name = o.getName();
                        info.setAdminCode(code);
                        info.setAdmin(name);
                        info.eventType = AdminInfo.TT_ADMIN;
                        boundSupport.firePropertyChange(SELECTED_ITEM_PROP, null, info);
                    }
                }
            }
        });
        
        // �񕝂�ݒ肷��
        TableColumn column = null;
        int[] width = new int[]{50, 250};
        int len = width.length;
        for (int i = 0; i < len; i++) {
            column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(width[i]);
        }
        
        // �����_���[��ݒ肷��
        AdminMasterRenderer mr = new AdminMasterRenderer();
        table.setDefaultRenderer(AdminEntry.class, mr);
        
        //
        // ���C�A�E�g����
        //
        // Keyword �p�l��
        JPanel key = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 5));
        key.add(findLabel);
        key.add(new JLabel("�p�@:"));
        key.add(keywordField);
        key.setBorder(BorderFactory.createTitledBorder(keywordBorderTitle));
        
        // �J�e�S��
        JPanel ctp = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        ctp.add(new JLabel("�J�e�S��:"));
        ctp.add(adminCombo);
        ctp.setBorder(BorderFactory.createTitledBorder(keywordBorderTitle));
        
        // ���@�R�[�h
        customCode = new JTextField(7);
        customCode.setToolTipText("���@�}�X�^�̃R�[�h�i�ԑ�j����͂��Ă��������B");
        customCode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                String text = customCode.getText().trim();
                if (!text.equals("")) {
                    StringTokenizer st = new StringTokenizer(text, " ");
                    StringBuilder sb = new StringBuilder();
                    int cnt = 0;
                    while (st.hasMoreTokens()) {
                        if (cnt != 0) {
                            sb.append(" ");
                        }
                        sb.append(CUSTOM_CODE);
                        sb.append(st.nextToken());
                        cnt++;
                    }
                    text = sb.toString();
                    fetchAdministration(text);
                }
            }
        });
        customCode.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent event) {
                JTextField tf = (JTextField) event.getSource();
                tf.getInputContext().setCharacterSubsets(null);
            }
        });
        JPanel customP = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        customP.add(new JLabel("���@�R�[�h: 001"));
        customP.add(customCode);
        customP.setBorder(BorderFactory.createTitledBorder(keywordBorderTitle));
        
        // �g�b�v�p�l���֔z�u����
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        top.add(key);
        top.add(Box.createHorizontalStrut(11));
        top.add(ctp);
        top.add(Box.createHorizontalGlue());
        top.add(customP);
        
        JScrollPane scroller = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        this.setLayout(new BorderLayout(0, 11));
        this.add(top, BorderLayout.NORTH);
        this.add(scroller, BorderLayout.CENTER);
    }
    
//    /**
//     * �p�@���烌�Z�d�Z�R�[�h�Ɛ��ʃR�[�h��Ԃ��B
//     * @param code �p�@�R�[�h
//     * @param name �p�@��
//     * @return ���Z�d�Z�R�[�h�Ɛ��ʃR�[�h�z��
//     */
//    private String[] getClassNumberCode(String code, String name) {
//        
//        if (name.startsWith("�P���P��") || name.startsWith("�P���Q��") || name.startsWith("�P���R��")) {
//            //
//            return new String[]{ClaimConst.RECEIPT_CODE_NAIYO, ClaimConst.YAKUZAI_TOYORYO_1NICHI};
//            
//        } else if (code.compareTo(TONYO_RANGE[0]) >=0 && code.compareTo(TONYO_RANGE[1]) <=0) {
//            //
//            return new String[]{ClaimConst.RECEIPT_CODE_GAIYO, ClaimConst.YAKUZAI_TOYORYO_1KAI};
//        }
//        
//        return new String[]{ClaimConst.RECEIPT_CODE_GAIYO, ClaimConst.YAKUZAI_TOYORYO};
//    }
    
    
    /**
     * �I�����ꂽ�J�e�S���ɑΉ�����p�@����������B
     */
    private void fetchAdministration(String category) {
        
        if (category == null) {
            return;
        }

        final SqlMasterDao dao = (SqlMasterDao) SqlDaoFactory.create(this, "dao.master");
        
        // ReflectMonitor �𐶐�����
        final ReflectMonitor rm = new ReflectMonitor();
        rm.setReflection(dao, 
                         "getAdminByCategory", 
                         new Class[]{String.class},
                         new Object[]{category});
        rm.setMonitor(SwingUtilities.getWindowAncestor(this), "�p�@����", category + " ���������Ă��܂�...  ", 200, 30*1000);
        
        // ���ʏ�Ԃ̃��X�i�𐶐�����
        PropertyChangeListener pl = new PropertyChangeListener() {
           
            public void propertyChange(PropertyChangeEvent e) {
                
                int state = ((Integer) e.getNewValue()).intValue();
                
                switch (state) {
                    
                    case ReflectMonitor.DONE:
                        processResult(dao.isNoError(), rm.getResult(), dao.getErrorMessage());
                        break;
                        
                    case ReflectMonitor.TIME_OVER:
                        Window parent = SwingUtilities.getWindowAncestor(AdminMaster.this);
                        String title = ClientContext.getString(getMaster());
                        new TimeoutWarning(parent, title, null).start();
                        break;
                        
                    case ReflectMonitor.CANCELED:
                        break;
                }
                
                //
                // Block ����������
                //
                setBusy(false);
            }
        };
        rm.addPropertyChangeListener(pl);
        
        //
        // Block ���A���\�b�h�̎��s���J�n����
        //
        setBusy(true);
        rm.start();
        
    }
    
//    /**
//     * �I�����ꂽ�J�e�S���ɑΉ�����p�@����������B
//     */
//    private void fetchAdministration(String category) {
//        
//        if (category == null) {
//            return;
//        }
//
//        final SqlMasterDao dao = (SqlMasterDao) SqlDaoFactory.create(this, "dao.master");
//        
//        // Worker �𐶐�����
//        int maxEstimation = ClientContext.getInt("task.masterSearch.maxEstimation");
//        int delay = ClientContext.getInt("task.masterSearch.delay");
//        final AdminTask worker = new AdminTask(category, dao, maxEstimation/delay);
//        
//        // �^�X�N�^�C�}�[�𐶐�����
//        taskTimer = new javax.swing.Timer(TIMER_DELAY, new ActionListener() {
//            
//            public void actionPerformed(ActionEvent e) {
//                
//                worker.getCurrent();
//                
//                if (worker.isDone()) {
//                    
//                    taskTimer.stop();
//                    setBusy(false);
//                    
//                    if (dao.isNoError()) {
//                        List result = worker.getResult();
//                        tableModel.setObjectList(result);
//                        setItemCount(tableModel.getObjectCount());
//                        
//                    } else {
//                        Window parent = SwingUtilities.getWindowAncestor(AdminMaster.this);
//                        String message = dao.getErrorMessage();
//                        String title = ClientContext.getFrameTitle(getMaster());
//                        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
//                    }
//                    
//                } else if (worker.isTimeOver()) {
//                    taskTimer.stop();
//                    setBusy(false);
//                    Window parent = SwingUtilities.getWindowAncestor(AdminMaster.this);
//                    String title = ClientContext.getString(getMaster());
//                    new TimeoutWarning(parent, title, null).start();
//                }
//            }
//        });
//        setBusy(true);
//        worker.start();
//        taskTimer.start();
//    }
    
//    /**
//     * �����^�X�N�N���X�B
//     */
//    protected class AdminTask extends AbstractInfiniteTask {
//        
//        private String category;
//        private SqlMasterDao dao;
//        private List result;
//        
//        public AdminTask(String category, SqlMasterDao dao, int taskLength) {
//            this.category = category;
//            this.dao = dao;
//            setTaskLength(taskLength);
//        }
//        
//        protected List getResult() {
//            return result;
//        }
//        
//        protected void doTask() {
//            result = dao.getAdminByCategory(category);
//            setDone(true);
//        }
//    }
        
    /**
     * �p�@�}�X�^ Table �̃����_���[�N���X�B
     */
    protected final class AdminMasterRenderer extends MasterRenderer {
        
        private static final long serialVersionUID = 8567079934909643686L;
        
        private final int CODE_COLUMN       = 0;
        private final int NAME_COLUMN       = 1;
        
        public AdminMasterRenderer() {
        }
        
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean isFocused,
                int row, int col) {
            Component c = super.getTableCellRendererComponent(
                    table,
                    value,
                    isSelected,
                    isFocused,
                    row, col);
            if (row % 2 == 0) {
                setBackground(getEvenColor());
            } else {
                setBackground(getOddColor());
            }
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            }
            JLabel label = (JLabel)c;
            
            if (value != null && value instanceof AdminEntry) {
                
                AdminEntry entry = (AdminEntry) value;

                switch(col) {
                    
                    case CODE_COLUMN:
                        label.setText(entry.getCode());
                        break;
                        
                    case NAME_COLUMN:
                        label.setText(entry.getName());
                        break;
                }
                
            } else {
                label.setBackground(Color.white);
                label.setText(value == null ? "" : value.toString());
            }
            return c;
        }
    }
}
