package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;

import open.dolphin.delegater.StampDelegater;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.PublishedTreeModel;
import open.dolphin.infomodel.SubscribedTreeModel;
import open.dolphin.plugin.helper.ComponentMemory;
import open.dolphin.project.Project;

/**
 * StampImporter
 *
 * @author Minagawa,Kazushi
 */
public class StampImporter {
    
    private static final String[] COLUMN_NAMES = {
        "��  ��", "�J�e�S��", "���J��", "��  ��", "���J��", "�C���|�[�g"
    };
    private static final String[] METHOD_NAMES = {
        "getName", "getCategory", "getPartyName", "getDescription", "getPublishType", "isImported"
    };
    private static final Class[] CLASSES = {
        String.class, String.class, String.class, String.class, String.class, Boolean.class
    };
    private static final int[] COLUMN_WIDTH = {
        120, 90, 170, 270, 40, 40
    };
    private static final Color ODD_COLOR = ClientContext.getColor("color.odd");
    private static final Color EVEN_COLOR = ClientContext.getColor("color.even");
    private static final ImageIcon WEB_ICON = ClientContext.getImageIcon("web_16.gif");
    private static final ImageIcon HOME_ICON = ClientContext.getImageIcon("home_16.gif");
    private static final ImageIcon FLAG_ICON = ClientContext.getImageIcon("flag_16.gif");
    
    private static final int WIDTH = 780;
    private static final int HEIGHT = 380;
    
    private String title = "�X�^���v�C���|�[�g";
    private JFrame frame;
    private ObjectListTable browseTable;
    private JButton importBtn;
    private JButton deleteBtn;
    private JButton cancelBtn;
    private JLabel publicLabel;
    private JLabel localLabel;
    private JLabel importedLabel;
    
    private StampBoxPlugin stampBox;
    private List<Long> importedTreeList;
    private StampDelegater sdl;
    private Timer timer;
    private BrowseTask browseWorker;
    private  SubscribeTask subscribeWorker;
    private UnsubscribeTask unsubscribeTask;
    private ProgressMonitor progressMonitor;
    
    public StampImporter(StampBoxPlugin stampBox) {
        this.stampBox = stampBox;
        importedTreeList = stampBox.getImportedTreeList();
    }
    
    /**
     * ���J����Ă���Tree�̃��X�g���擾���e�[�u���֕\������B
     */
    public void start() {
        
        sdl = new StampDelegater();
        
        int delay = 200;
        int maxEstimation = 60*1000;
        String mmsg = "���J�X�^���v���擾���Ă��܂�...";
        int decideToPopup = ClientContext.getInt("task.default.decideToPopup");
        int milisToPopup = ClientContext.getInt("task.default.milisToPopup");
        progressMonitor = new ProgressMonitor(frame, null, mmsg, 0, maxEstimation/delay);
        
        browseWorker = new BrowseTask(sdl, maxEstimation/delay);
        
        timer = new javax.swing.Timer(delay, new ActionListener() {
            
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                
                progressMonitor.setProgress(browseWorker.getCurrent());
                
                if (browseWorker.isDone()) {
                    
                    timer.stop();
                    progressMonitor.close();
                    
                    if (sdl.isNoError()) {
                        // DB����擾������������GUI�R���|�[�l���g�𐶐�����
                        initComponent();
                        List list = browseWorker.getResult();
                        if (importedTreeList != null && importedTreeList.size() > 0) {
                            for (Iterator iter = list.iterator(); iter.hasNext(); ) {
                                PublishedTreeModel model = (PublishedTreeModel) iter.next();
                                for (Long id : importedTreeList) {
                                    if (id.longValue() == model.getId()) {
                                        model.setImported(true);
                                        break;
                                    }
                                }
                            }
                        }
                        browseTable.setObjectList(list);
                    } else {
                        JOptionPane.showMessageDialog(frame,
                                sdl.getErrorMessage(),
                                ClientContext.getFrameTitle(title),
                                JOptionPane.WARNING_MESSAGE);
                    }
                    
                } else if (browseWorker.isTimeOver()) {
                    timer.stop();
                    progressMonitor.close();
                    new TimeoutWarning(frame, title, null).start();
                }
            }
        });
        progressMonitor.setProgress(0);
        progressMonitor.setMillisToDecideToPopup(milisToPopup);
        progressMonitor.setMillisToPopup(decideToPopup);
        browseWorker.start();
        timer.start();
    }
    
    /**
     * GUI�R���|�[�l���g������������B
     */
    public void initComponent() {
        frame = new JFrame(ClientContext.getFrameTitle(title));
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                stop();
            }
        });
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int n = ClientContext.isMac() ? 3 : 2;
        int x = (screen.width - WIDTH) / 2;
        int y = (screen.height - HEIGHT) / n;
        ComponentMemory cm = new ComponentMemory(frame, new Point(x, y), new Dimension(new Dimension(WIDTH, HEIGHT)), this);
        cm.setToPreferenceBounds();
        
        JPanel contentPane = createBrowsePane();
        contentPane.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        
        contentPane.setOpaque(true);
        frame.setContentPane(contentPane);
        frame.setVisible(true);
    }
    
    /**
     * �I������B
     */
    public void stop() {
        frame.setVisible(false);
        frame.dispose();
    }
    
    /**
     * ���J�X�^���v�u���E�Y�y�C���𐶐�����B
     */
    private JPanel createBrowsePane() {
        
        JPanel browsePane = new JPanel();
        
        browseTable = new ObjectListTable(COLUMN_NAMES, 10, METHOD_NAMES, CLASSES);
        browseTable.setColumnWidth(COLUMN_WIDTH);
        importBtn = new JButton("�C���|�[�g");
        importBtn.setEnabled(false);
        cancelBtn = new JButton("����");
        deleteBtn = new JButton("�폜");
        deleteBtn.setEnabled(false);
        publicLabel = new JLabel("�O���[�o��", WEB_ICON, SwingConstants.CENTER);
        localLabel = new JLabel("�@��", HOME_ICON, SwingConstants.CENTER);
        importedLabel = new JLabel("�C���|�[�g��", FLAG_ICON, SwingConstants.CENTER);
        
        // ���C�A�E�g����
        browsePane.setLayout(new BorderLayout(0, 17));
        JPanel flagPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 5));
        flagPanel.add(localLabel);
        flagPanel.add(publicLabel);
        flagPanel.add(importedLabel);
        JPanel cmdPanel = GUIFactory.createCommandButtonPanel(new JButton[]{cancelBtn, deleteBtn, importBtn});
        browsePane.add(flagPanel, BorderLayout.NORTH);
        browsePane.add(browseTable.getScroller(), BorderLayout.CENTER);
        browsePane.add(cmdPanel, BorderLayout.SOUTH);
        
        // �����_����ݒ肷��
        PublishTypeRenderer pubTypeRenderer = new PublishTypeRenderer();
        browseTable.getTable().getColumnModel().getColumn(4).setCellRenderer(pubTypeRenderer);
        ImportedRenderer importedRenderer = new ImportedRenderer();
        browseTable.getTable().getColumnModel().getColumn(5).setCellRenderer(importedRenderer);
        
        // BrowseTable���V���O���Z���N�V�����ɂ���
        browseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // �R���|�[�l���g�Ԃ̃C�x���g�ڑ����s��
        PropertyChangeListener pl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                Object[] selected = (Object[]) e.getNewValue();
                if (selected != null && selected.length > 0) {
                    PublishedTreeModel model = (PublishedTreeModel) selected[0];
                    if (model.isImported()) {
                        importBtn.setEnabled(false);
                        deleteBtn.setEnabled(true);
                    } else {
                        importBtn.setEnabled(true);
                        deleteBtn.setEnabled(false);
                    }
                    
                } else {
                    importBtn.setEnabled(false);
                    deleteBtn.setEnabled(false);
                }
            }
        };
        browseTable.addPropertyChangeListener(ObjectListTable.SELECTED_OBJECT, pl);
        
        // import
        importBtn.addActionListener(new ReflectActionListener(this, "importPublishedTree"));
        // remove
        deleteBtn.addActionListener(new ReflectActionListener(this, "removeImportedTree"));
        // �L�����Z��
        cancelBtn.addActionListener(new ReflectActionListener(this, "stop"));
        
        return browsePane;
    }
    
    /**
     * �u���E�U�e�[�u���őI���������JTree���C���|�[�g����B
     */
    public  void importPublishedTree() {
        
        Object[] objects = (Object[]) browseTable.getSelectedObject();
        if (objects == null || objects.length == 0) {
            return;
        }
        // �e�[�u���̓V���O���Z���N�V�����ł���
        // TODO �u���E�Y����byte[]���擾���Ă���...
        final PublishedTreeModel importTree = (PublishedTreeModel) objects[0];
        try {
            importTree.setTreeXml(new String(importTree.getTreeBytes(), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //importTree.setTreeBytes(null);
        // �T�u�X�N���C�u���X�g�ɒǉ�����
        SubscribedTreeModel sm = new SubscribedTreeModel();
        sm.setUser(Project.getUserModel());
        sm.setTreeId(importTree.getId());
        List<SubscribedTreeModel> subscribeList = new ArrayList<SubscribedTreeModel>(1);
        subscribeList.add(sm);
        
        // �f���Q�[�^�𐶐�����
        sdl = new StampDelegater();
        
        // Worker, Timer �����s����
        int delay = 200;
        int maxEstimation = 60*1000;
        String mmsg = "���J�X�^���v���C���|�[�g���Ă��܂�...";
        int decideToPopup = ClientContext.getInt("task.default.decideToPopup");
        int milisToPopup = ClientContext.getInt("task.default.milisToPopup");
        progressMonitor = new ProgressMonitor(frame, null, mmsg, 0, maxEstimation/delay);
        
        subscribeWorker = new SubscribeTask(subscribeList, sdl, maxEstimation/delay);
        
        timer = new javax.swing.Timer(delay, new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                progressMonitor.setProgress(subscribeWorker.getCurrent());
                
                if (subscribeWorker.isDone()) {
                    
                    timer.stop();
                    progressMonitor.close();
                    
                    if (sdl.isNoError()) {
                        // �X�^���v�{�b�N�X�փC���|�[�g����
                        stampBox.importPublishedTree(importTree);
                        // Browser�\�����C���|�[�g�ς݂ɂ���
                        importTree.setImported(true);
                        browseTable.getTableModel().fireTableDataChanged();
                        
                    } else {
                        JOptionPane.showMessageDialog(frame,
                                sdl.getErrorMessage(),
                                ClientContext.getFrameTitle(title),
                                JOptionPane.WARNING_MESSAGE);
                    }
                    
                } else if (subscribeWorker.isTimeOver()) {
                    timer.stop();
                    progressMonitor.close();
                    new TimeoutWarning(frame, title, null).start();
                }
            }
        });
        progressMonitor.setProgress(0);
        progressMonitor.setMillisToDecideToPopup(milisToPopup);
        progressMonitor.setMillisToPopup(decideToPopup);
        subscribeWorker.start();
        timer.start();
    }
    
    /**
     * �C���|�[�g���Ă���X�^���v���폜����B
     */
    public void removeImportedTree() {
        
        Object[] objects = (Object[]) browseTable.getSelectedObject();
        if (objects == null || objects.length == 0) {
            return;
        }
        
        // �폜����Tree���擾����
        final PublishedTreeModel removeTree = (PublishedTreeModel) objects[0];
        SubscribedTreeModel sm = new SubscribedTreeModel();
        sm.setTreeId(removeTree.getId());
        sm.setUser(Project.getUserModel());
        List<SubscribedTreeModel> list = new ArrayList<SubscribedTreeModel>(1);
        list.add(sm);
        
        // DeleteTask�����s����
        sdl = new StampDelegater();
        
        // Unsubscribe�^�X�N�����s����
        int delay = 200;
        int maxEstimation = 60*1000;
        String mmsg = "�C���|�[�g�ς݃X�^���v���폜���Ă��܂�...";
        int decideToPopup = ClientContext.getInt("task.default.decideToPopup");
        int milisToPopup = ClientContext.getInt("task.default.milisToPopup");
        progressMonitor = new ProgressMonitor(frame, null, mmsg, 0, maxEstimation/delay);
        
        unsubscribeTask = new UnsubscribeTask(list, sdl, maxEstimation/delay);
        
        timer = new javax.swing.Timer(delay, new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                progressMonitor.setProgress(unsubscribeTask.getCurrent());
                
                if (unsubscribeTask.isDone()) {
                    
                    timer.stop();
                    progressMonitor.close();
                    
                    if (sdl.isNoError()) {
                        // �X�^���v�{�b�N�X����폜����
                        stampBox.removeImportedTree(removeTree.getId());
                        // �u���E�U�\����ύX����
                        removeTree.setImported(false);
                        browseTable.getTableModel().fireTableDataChanged();
                        
                    } else {
                        JOptionPane.showMessageDialog(frame,
                                sdl.getErrorMessage(),
                                ClientContext.getFrameTitle(title),
                                JOptionPane.WARNING_MESSAGE);
                    }
                    
                } else if (subscribeWorker.isTimeOver()) {
                    timer.stop();
                    progressMonitor.close();
                    new TimeoutWarning(frame, title, null).start();
                }
            }
        });
        progressMonitor.setProgress(0);
        progressMonitor.setMillisToDecideToPopup(milisToPopup);
        progressMonitor.setMillisToPopup(decideToPopup);
        unsubscribeTask.start();
        timer.start();
    }
    
    /**
     * ���J����Ă���Tree�̃��X�g���擾����^�X�N�B
     */
    class BrowseTask extends AbstractInfiniteTask {
        
        private StampDelegater mySdl;
        private List<PublishedTreeModel> result;
        
        public BrowseTask(StampDelegater mySdl, int taskLength) {
            this.mySdl = mySdl;
            setTaskLength(taskLength);
        }
        
        public List<PublishedTreeModel> getResult() {
            return result;
        }
        
        protected void doTask() {
            result = mySdl.getPublishedTrees();
            setDone(true);
        }
    }
    
    /**
     * �T�u�X�N���C�u�^�X�N�B
     */
    class SubscribeTask extends AbstractInfiniteTask {
        
        private StampDelegater mySdl;
        private List<SubscribedTreeModel> subscribeList;
        
        public SubscribeTask(List<SubscribedTreeModel> subscribeList, StampDelegater mySdl, int taskLength) {
            this.subscribeList = subscribeList;
            this.mySdl = mySdl;
            setTaskLength(taskLength);
        }
        
        protected void doTask() {
            mySdl.subscribeTrees(subscribeList);
            setDone(true);
        }
    }
    
    /**
     * �A���T�u�X�N���C�u�^�X�N�B
     */
    class UnsubscribeTask extends AbstractInfiniteTask {
        
        private StampDelegater mySdl;
        private List<SubscribedTreeModel> subscribeList;
        
        public UnsubscribeTask(List<SubscribedTreeModel> subscribeList, StampDelegater mySdl, int taskLength) {
            this.subscribeList = subscribeList;
            this.mySdl = mySdl;
            setTaskLength(taskLength);
        }
        
        protected void doTask() {
            mySdl.unsubscribeTrees(subscribeList);
            setDone(true);
        }
    }
    
    protected class PublishTypeRenderer extends DefaultTableCellRenderer {
        
        private static final long serialVersionUID = 7134379493874260895L;
        
        /** Creates new IconRenderer */
        public PublishTypeRenderer() {
            super();
            setOpaque(true);
            setHorizontalAlignment(JLabel.CENTER);
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
            
            if (row % 2 == 0) {
                setBackground(EVEN_COLOR);
            } else {
                setBackground(ODD_COLOR);
            }
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            }
            
            if (value != null && value instanceof String) {
                
                String pubType = (String) value;
                
                if (pubType.equals(IInfoModel.PUBLISHED_TYPE_GLOBAL)) {
                    setIcon(WEB_ICON);
                } else {
                    setIcon(HOME_ICON);
                } 
                ((JLabel) c).setText("");
            } else {
                setIcon(null);
                ((JLabel) c).setText(value == null ? "" : value.toString());
            }
            return c;
        }
    }
    
    protected class ImportedRenderer extends DefaultTableCellRenderer {
        
        private static final long serialVersionUID = 7134379493874260895L;
        
        /** Creates new IconRenderer */
        public ImportedRenderer() {
            super();
            setOpaque(true);
            setHorizontalAlignment(JLabel.CENTER);
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
            
            if (row % 2 == 0) {
                setBackground(EVEN_COLOR);
            } else {
                setBackground(ODD_COLOR);
            }
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            }
            
            if (value != null && value instanceof Boolean) {
                
                Boolean imported = (Boolean) value;
                
                if (imported.booleanValue()) {
                    setIcon(FLAG_ICON);
                } else {
                    setIcon(null);
                }
                ((JLabel) c).setText("");
            } else {
                setIcon(null);
                ((JLabel) c).setText(value == null ? "" : value.toString());
            }
            return c;
        }
    }
}