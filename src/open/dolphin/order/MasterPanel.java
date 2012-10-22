package open.dolphin.order;

import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import open.dolphin.client.AutoKanjiListener;
import open.dolphin.client.ClientContext;
import open.dolphin.client.TaskTimerMonitor;
import open.dolphin.dao.SqlDaoFactory;
import open.dolphin.dao.SqlMasterDao;
import open.dolphin.project.Project;
import open.dolphin.table.ObjectTableModel;
import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;

/**
 * �}�X�^�����p�l���̃��[�g���ۃN���X�B
 *
 * @author Kazushi Minagawa
 */
public abstract class MasterPanel extends JPanel {

    /** �}�X�^���ڑI���v���p�e�B�� */
    public static final String SELECTED_ITEM_PROP = "selectedItemProp";
    /** �������v���p�e�B�� */
    public static final String BUSY_PROP = "busyProp";
    /** �����v���p�e�B�� */
    public static final String ITEM_COUNT_PROP = "itemCount";
    /** �L�[���[�h�t�B�[���h�p�� tooltip text */
    protected static final String TOOLTIP_KEYWORD = "�������g�p�ł��܂�";
    /** �L�[���[�h�t�B�[���h�̒��� */
    protected static final int KEYWORD_FIELD_LENGTH = 12;
    /** �����A�C�R�� */
    protected static final String FIND_ICON = "/open/dolphin/resources/images/srch_16.gif";
    /** �L�[���[�h�����̃{�[�_�^�C�g�� */
    protected static final String keywordBorderTitle = ClientContext.getString("masterSearch.text.keywordBorderTitle");
    protected static final Color[] masterColors = ClientContext.getColorArray("masterSearch.masterColors");
    protected static final String[] masterNames = ClientContext.getStringArray("masterSearch.masterNames");
    protected static final String[] masterTabNames = ClientContext.getStringArray("masterSearch.masterTabNames");
    /** �������ʃe�[�u���̊J�n�s�� */
    protected final int START_NUM_ROWS = 20;
    /** �L�[���[�h�t�B�[���h */
    protected JTextField keywordField;
    /** �����A�C�R�� */
    protected ImageIcon findIcon = new ImageIcon(this.getClass().getResource(FIND_ICON));
    /** �����A�C�R����\�����郉�x�� */
    protected JLabel findLabel = new JLabel(findIcon);
    /** �\�[�g�{�^���z�� */
    protected JRadioButton[] sortButtons;
    /** �������ʃe�[�u�� */
    protected JTable table;
    /** �������ʃe�[�u���� table model */
    protected ObjectTableModel tableModel;
    /** ��������}�X�^�� */
    protected String master;
    /** ��������N���X */
    protected String searchClass;
    /** �\�[�g�� */
    protected String sortBy;
    /** order by �� */
    protected String order;
    /** �I�����ꂽ�}�X�^���� */
    protected MasterItem selectedItem;
    /** �������̃t���O */
    protected boolean busy;
    /** �������ʌ��� */
    protected int itemCount;
    /** �^�X�N�p�̃^�C�} */
    protected javax.swing.Timer taskTimer;
    /** ���荞�ݎ��� */
    protected static final int TIMER_DELAY = 200;
    /** �����T�|�[�g */
    protected PropertyChangeSupport boundSupport;
    /** �v���t�@�����X */
    protected Preferences prefs = Preferences.userNodeForPackage(this.getClass());

    protected Logger logger;
    
    /**
     * MasterPanel�I�u�W�F�N�g�𐶐�����B
     */
    public MasterPanel() {
        logger = ClientContext.getBootLogger();
    }

    /**
     * MasterPanel�I�u�W�F�N�g�𐶐�����B
     * @param master �}�X�^��
     * @param pulse �i���o�[  // ����?
     */
    public MasterPanel(final String master) {
        //public MasterPanel(final String master, UltraSonicProgressLabel pulse) {

        this();
        setMaster(master);
        //this.pulse = pulse;

        this.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));

        //
        // �L�[���[�h�t�B�[���h�𐶐�����
        //
        keywordField = new JTextField(KEYWORD_FIELD_LENGTH);
        keywordField.setToolTipText(TOOLTIP_KEYWORD);
        keywordField.setMaximumSize(keywordField.getPreferredSize());
        keywordField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String key = keywordField.getText().trim();
                if (!key.equals("")) {
                    search(key);
                }
            }
        });

        // �t�H�[�J�X������������ IME ���I���ɂ���
        keywordField.addFocusListener(AutoKanjiListener.getInstance());

        // ����������
        initialize();
    }

    /**
     * �T�u�N���X���������鏉�������\�b�h�B
     */
    protected abstract void initialize();

    /**
     * �������X�i��o�^����B
     * @param prop �v���p�e�B��
     * @param l ���X�i
     */
    @Override
    public void addPropertyChangeListener(String prop, PropertyChangeListener l) {
        if (boundSupport == null) {
            boundSupport = new PropertyChangeSupport(this);
        }
        boundSupport.addPropertyChangeListener(prop, l);
    }

    /**
     * �������X�i���폜����B
     * @param prop �v���p�e�B��
     * @param l ���X�i
     */
    @Override
    public void removePropertyChangeListener(String prop, PropertyChangeListener l) {
        if (boundSupport == null) {
            boundSupport = new PropertyChangeSupport(this);
        }
        boundSupport.removePropertyChangeListener(prop, l);
    }

    /**
     * �I�����ꂽ�}�X�^���ڂ�Ԃ��B
     * @return �I�����ꂽ�}�X�^����
     */
    public MasterItem getSelectedItem() {
        return selectedItem;
    }

    /**
     * �I�����ꂽ�}�X�^���ڃv���p�e�B���Z�b�g�����X�i�֒ʒm����B
     * @param �I�����ꂽ�}�X�^����
     */
    public void setSelectedItem(MasterItem item) {
        MasterItem oldItem = selectedItem;
        selectedItem = item;
        boundSupport.firePropertyChange(SELECTED_ITEM_PROP, oldItem, selectedItem);
    }

    /**
     * �������v���p�e�B��Ԃ��B
     * @return �������̎� true
     */
    public boolean isBusy() {
        return busy;
    }

    /**
     * �������v���p�e�B��ݒ肵���X�i�֒ʒm����B
     * @param newBusy �������̎� true
     */
    public void setBusy(boolean newBusy) {
        boolean oldBusy = busy;
        busy = newBusy;
        boundSupport.firePropertyChange(BUSY_PROP, oldBusy, busy);
    }

    /**
     * ������Ԃ��B
     * @return �}�X�^�����̌��ʌ���
     */
    public int getCount() {
        return itemCount;
    }

    /**
     * �������ʌ������Z�b�g�����X�i�֒ʒm����B
     * @param count �}�X�^�����̌��ʌ���
     */
    public void setItemCount(int count) {
        itemCount = count;
        boundSupport.firePropertyChange(ITEM_COUNT_PROP, -1, itemCount);
    }

    /**
     * �����N���X��ݒ肷��B
     * @param searchClass �����N���X
     */
    public void setSearchClass(String searchClass) {
        this.searchClass = searchClass;
    }

    /**
     * �\�[�g���ڂ�ݒ肷��B
     * @param sortBy �\�[�g����
     */
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    /**
     * �}�X�^����Ԃ��B
     * @return �}�X�^��
     */
    public String getMaster() {
        return master;
    }

    /**
     * �}�X�^����ݒ肷��B
     * @param master �}�X�^��
     */
    public void setMaster(String master) {
        this.master = master;
    }

    /**
     * order by ���ڂ�ݒ肷��B
     * @param order order by ����
     */
    public void setOrder(String order) {
        this.order = order;
    }

    /**
     * ���̃}�X�^�p�l���̃^�u���I�����ꂽ���R�[�������B
     * �������ʌ���������������B
     */
    public void enter() {
        setItemCount(tableModel.getObjectCount());
    }

    /**
     * �v���O�����̏I���������s���B
     */
    public void dispose() {
        if (tableModel != null) {
            tableModel.clear();
        }
    }

    /**
     * �����̃L�[���[�h����}�X�^����������B
     * @param text �L�[���[�h
     */
    protected void search(final String text) {
        
        logger.debug("master = " + master);
        logger.debug("text = " + text);
        logger.debug("searchClass = " + searchClass);
        logger.debug("sortBy = " + sortBy);
        logger.debug("order = " + order);

        // CLAIM(Master) Address ���ݒ肳��Ă��Ȃ��ꍇ�Ɍx������
        String address = Project.getClaimAddress();
        if (address == null || address.equals("")) {
            String msg0 = "���Z�R����IP�A�h���X���ݒ肳��Ă��Ȃ����߁A�}�X�^�[�������ł��܂���B";
            String msg1 = "���ݒ胁�j���[���烌�Z�R����IP�A�h���X��ݒ肵�Ă��������B";
            Object message = new String[]{msg0, msg1};
            Window parent = SwingUtilities.getWindowAncestor(MasterPanel.this);
            String title = ClientContext.getFrameTitle(getMaster());
            JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
            return;
        }

        // DAO�𐶐�����
        final SqlMasterDao dao = (SqlMasterDao) SqlDaoFactory.create(this, "dao.master");
        
        ApplicationContext appCtx = ClientContext.getApplicationContext();
        Application app = appCtx.getApplication();
        
        Task task = new Task<Object, Void>(app) {

            @Override
            protected Object doInBackground() throws Exception {
                Object result = dao.getByName(master, text, false, searchClass, sortBy, order);
                return result;
            }
            
            @Override
            protected void succeeded(Object result) {
                logger.debug("Task succeeded");
                processResult(dao.isNoError(), result, dao.getErrorMessage());
            }
            
            @Override
            protected void cancelled() {
                logger.debug("Task cancelled");
            }
            
            @Override
            protected void failed(java.lang.Throwable cause) {
                logger.warn(cause.getMessage());
            }
            
            @Override
            protected void interrupted(java.lang.InterruptedException e) {
                logger.warn(e.getMessage());
            }
        };
        
        TaskMonitor taskMonitor = appCtx.getTaskMonitor();
        String message = "�}�X�^����";
        String note = text + "���������Ă��܂�...";
        Component c = SwingUtilities.getWindowAncestor(this);
        TaskTimerMonitor w = new TaskTimerMonitor(task, taskMonitor, c, message, note, 200, 60*1000);
        taskMonitor.addPropertyChangeListener(w);
        
        appCtx.getTaskService().execute(task);
    }

    /**
     * �������ʂ��e�[�u���֕\������B
     */
    protected void processResult(boolean noErr, Object result, String message) {

        if (noErr) {

            tableModel.setObjectList((List) result);
            setItemCount(tableModel.getObjectCount());

        } else {

            String title = ClientContext.getFrameTitle(getMaster());
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * �\�[�g�{�^���֓o�^����A�N�V�������X�i�N���X�B
     */
    protected class SortActionListener implements ActionListener {

        private MasterPanel target;
        private String sortBy;
        private int btnIndex;

        public SortActionListener(MasterPanel target, String sortBy, int btnIndex) {
            this.target = target;
            this.sortBy = sortBy;
            this.btnIndex = btnIndex;
        }

        public void actionPerformed(ActionEvent e) {
            prefs.putInt("masterSearch." + target.getMaster() + ".sort", btnIndex);
            target.setSortBy(sortBy);
        }
    }
}
