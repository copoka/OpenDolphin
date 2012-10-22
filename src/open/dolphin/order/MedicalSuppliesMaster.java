package open.dolphin.order;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import open.dolphin.client.ClientContext;
import open.dolphin.client.MasterRenderer;
import open.dolphin.infomodel.MedicineEntry;
import open.dolphin.table.ObjectTableModel;

/**
 * ���i�}�X�^�����p�l���B
 */
public class MedicalSuppliesMaster extends MasterPanel {
    
    private static final String[] medicineColumns = ClientContext.getStringArray("masterSearch.medicine.columnNames");
    private static final String[] costFlags = ClientContext.getStringArray("masterSearch.medicine.costFlags");
    private static final String[] sortButtonNames = ClientContext.getStringArray("masterSearch.medicine.sortButtonNames");
    private static final String[] sortColumnNames = ClientContext.getStringArray("masterSearch.medicine.sortColumnNames");
    
    /**
     * MedicalSuppliesMaster�I�u�W�F�N�g�𐶐�����B
     * @param master �}�X�^��
     * @param pulse �����g�i���o�[
     */
    public MedicalSuppliesMaster(String master) {
        super(master);
        this.setSearchClass(ClaimConst.MASTER_FLAG_MEDICICE);
    }
    
    /**
     * ����������B
     */
    protected void initialize() {
        
        //
        // �\�[�g�{�^�� group �𐶐�����
        //
        ButtonGroup bg = new ButtonGroup();
        sortButtons = new JRadioButton[sortButtonNames.length];
        for (int i = 0; i < sortButtonNames.length; i++) {
            JRadioButton radio = new JRadioButton(sortButtonNames[i]);
            sortButtons[i] = radio;
            bg.add(radio);
            radio.addActionListener(new SortActionListener(this, sortColumnNames[i], i));
        }
        
        // �O��I������Ă������̂�I������
        int index = prefs.getInt("masterSearch.medicine.sort", 0);
        sortButtons[index].setSelected(true);
        setSortBy(sortColumnNames[index]);
        
        //
        // Table Model �𐶐�����
        //
        tableModel = new ObjectTableModel(medicineColumns, START_NUM_ROWS) {
            
            @Override
            public Class getColumnClass(int col) {
                return MedicineEntry.class;
            }
        };
        
        //
        // Table �𐶐�����
        //
        table = new JTable(tableModel);
        
        // �V���O���I�����[�h�ɂ���
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        //
        // �s���N���b�N(���ڂ��I���j���ꂽ�ꍇ�AMasterItem �I�u�W�F�N�g�𐶐���
        // ���X�i�֒ʒm����
        //
        table.setRowSelectionAllowed(true);
        ListSelectionModel m = table.getSelectionModel();
        m.addListSelectionListener(new ListSelectionListener() {
            
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    int row = table.getSelectedRow();
                    MedicineEntry o = (MedicineEntry) tableModel.getObject(row);
                    if (o != null) {
                        MasterItem mItem = new MasterItem();
                        mItem.setClassCode(ClaimConst.YAKUZAI);
                        mItem.setCode(o.getCode());
                        mItem.setName(o.getName());
                        mItem.setUnit(o.getUnit());
                        mItem.setYkzKbn(o.getYkzKbn());
                        setSelectedItem(mItem);
                    }
                }
            }
        });
        
        // �񕝂�ݒ肷��
        TableColumn column = null;
        int[] width = new int[]{50, 200, 200, 40, 30, 50, 50, 50};
        int len = width.length;
        for (int i = 0; i < len; i++) {
            column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(width[i]);
        }
        
        // �����_���[��ݒ肷��
        MedicineMasterRenderer mr = new MedicineMasterRenderer();
        mr.setBeforStartColor(masterColors[0]);
        mr.setInUseColor(masterColors[1]);
        mr.setAfterEndColor(masterColors[2]);
        mr.setCostFlag(costFlags);
        table.setDefaultRenderer(MedicineEntry.class, mr);
        
        // ���C�A�E�g����
        // Keyword
        JPanel key = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 5));
        key.add(findLabel);
        key.add(new JLabel(masterTabNames[1] + ":"));
        key.add(keywordField);
        key.setBorder(BorderFactory.createTitledBorder(keywordBorderTitle));
        
        JPanel sort = new JPanel();
        sort.setLayout(new BoxLayout(sort, BoxLayout.X_AXIS));
        for (int i = 0; i < sortButtons.length; i++) {
            if ( i != 0) {
                sort.add(Box.createHorizontalStrut(5));
            }
            sort.add(sortButtons[i]);
        }
        sort.setBorder(BorderFactory.createTitledBorder("�\�[�g"));
        
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        top.add(key);
        top.add(Box.createHorizontalGlue());
        top.add(sort);
        
        JScrollPane scroller = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        this.setLayout(new BorderLayout(0, 11));
        this.add(top, BorderLayout.NORTH);
        this.add(scroller, BorderLayout.CENTER);
    }
    
    /**
     * ���i�}�X�^ Table �̃����_���[�N���X�B
     */
    protected final class MedicineMasterRenderer extends MasterRenderer {
        
        private static final long serialVersionUID = 8567079934909643686L;
        
        private final int CODE_COLUMN       = 0;
        private final int NAME_COLUMN       = 1;
        private final int KANA_COLUMN       = 2;
        private final int UNIT_COLUMN       = 3;
        private final int COST_FLAG_COLUMN  = 4;
        private final int COST_COLUMN       = 5;
        private final int JNCD_COLUMN       = 6;
        private final int START_COLUMN      = 7;
        private final int END_COLUMN        = 8;
        
        private String[] costFlags;
        
        public MedicineMasterRenderer() {
        }
        
        public String[] getCostFlag() {
            return costFlags;
        }
        
        public void setCostFlag(String[] val) {
            costFlags = val;
        }
        
        @Override
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
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {

                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            JLabel label = (JLabel)c;
            
            if (value != null && value instanceof MedicineEntry) {
                
                MedicineEntry entry = (MedicineEntry)value;
                
                String startDate = entry.getStartDate();
                String endDate = entry.getEndDate();
                
                setColor(label, startDate, endDate);
                
                switch(col) {
                    
                    case CODE_COLUMN:
                        label.setText(entry.getCode());
                        break;
                        
                    case NAME_COLUMN:
                        label.setText(entry.getName());
                        break;
                        
                    case KANA_COLUMN:
                        label.setText(entry.getKana());
                        break;
                        
                    case UNIT_COLUMN:
                        label.setText(entry.getUnit());
                        break;
                        
                    case COST_FLAG_COLUMN:
                        try {
                            int index = Integer.parseInt(entry.getCostFlag());
                            label.setText(costFlags[index]);
                        } catch (Exception e) {
                            label.setText("");
                        }
                        break;
                        
                    case COST_COLUMN:
                        label.setText(entry.getCost());
                        break;
                        
                    case JNCD_COLUMN:
                        label.setText(entry.getJNCD());
                        break;
                        
                    case START_COLUMN:
                        if (startDate.startsWith("0")) {
                            label.setText("");
                        } else {
                            label.setText(startDate);
                        }
                        break;
                        
                    case END_COLUMN:
                        if (endDate.startsWith("9")) {
                            label.setText("");
                        } else {
                            label.setText(endDate);
                        }
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
