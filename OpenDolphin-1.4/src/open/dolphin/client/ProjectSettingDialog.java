package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import open.dolphin.plugin.PluginLoader;
import open.dolphin.project.Project;
import org.apache.log4j.Logger;

/**
 * ���ݒ�_�C�A���O�B
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public final class ProjectSettingDialog implements PropertyChangeListener {

    // GUI
    private JDialog dialog;
    private JPanel itemPanel;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JButton okButton;
    private JButton cancelButton;
    //
    // �S�̂̃��f��
    //
    private HashMap<String, AbstractSettingPanel> settingMap;
    private ArrayList<AbstractSettingPanel> allSettings;
    private ArrayList<JToggleButton> allBtns;
    private String startSettingName;
    private boolean loginState;
    private PropertyChangeSupport boundSupport;
    private static final String SETTING_PROP = "SETTING_PROP";
    private boolean okState;

    private Logger logger;

    /**
     * Creates new ProjectSettingDialog
     */
    public ProjectSettingDialog() {
        logger = ClientContext.getBootLogger();
    }

    public void addPropertyChangeListener(String prop, PropertyChangeListener l) {
        if (boundSupport == null) {
            boundSupport = new PropertyChangeSupport(this);
        }
        boundSupport.addPropertyChangeListener(prop, l);
    }

    public void removePropertyChangeListener(String prop, PropertyChangeListener l) {
        if (boundSupport == null) {
            boundSupport = new PropertyChangeSupport(this);
        }
        boundSupport.addPropertyChangeListener(prop, l);
    }

    public boolean getLoginState() {
        return loginState;
    }

    public void setLoginState(boolean b) {
        loginState = b;
    }

    public boolean getValue() {
        return Project.getProjectStub().isValid();
    }

    public void notifyResult() {
        boolean valid = Project.getProjectStub().isValid() ? true : false;
        boundSupport.firePropertyChange(SETTING_PROP, !valid, valid);
    }

    /**
     * �I�[�v�����ɕ\������ݒ��ʂ��Z�b�g����B
     */
    public void setProject(String startSettingName) {
        this.startSettingName = startSettingName;
    }

    /**
     * �ݒ��ʂ��J�n����B
     */
    public void start() {

        Runnable r = new Runnable() {

            @Override
            public void run() {

                //
                // ���f���𓾂�
                // �S�Ă̐ݒ�v���O�C��(Reference)�𓾁A���X�g�Ɋi�[����
                //
                try {
                    allSettings = new ArrayList<AbstractSettingPanel>();
                    PluginLoader<AbstractSettingPanel> loader = PluginLoader.load(AbstractSettingPanel.class);
                    Iterator<AbstractSettingPanel> iter = loader.iterator();
                    while (iter.hasNext()) {
                        AbstractSettingPanel setting = iter.next();
                        logger.debug(setting.getClass().getName());
                        allSettings.add(setting);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                // �ݒ�p�l��(AbstractSettingPanel)���i�[���� Hashtable�𐶐�����
                // key=�ݒ�v���O�C���̖��O value=�ݒ�v���O�C��
                settingMap = new HashMap<String, AbstractSettingPanel>();

                //
                // GUI ���\�z�����f�����o�C���h����
                //
                initComponents();

                //
                // �I�[�v�����ɕ\������ݒ��ʂ����肷��
                //
                int index = 0;

                if (startSettingName != null) {
                    logger.debug("startSettingName = " + startSettingName);
                    for (AbstractSettingPanel setting : allSettings) {
                        if (startSettingName.equals(setting.getId())) {
                            logger.debug("found index " + index);
                            break;
                        }
                        index++;
                    }
                }
                
                index = (index >= 0 && index < allSettings.size()) ? index : 0;

                //
                // �{�^���������ĕ\������
                //
                allBtns.get(index).doClick();
            }
        };

        SwingUtilities.invokeLater(r);
    }

    /**
     * GUI ���\�z����B
     */
    private void initComponents() {

        itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        //
        // �ݒ�v���O�C�����N�����邽�߂̃g�O���{�^���𐶐���
        // �p�l���։�����
        //
        allBtns = new ArrayList<JToggleButton>();
        ButtonGroup bg = new ButtonGroup();
        for (AbstractSettingPanel setting : allSettings) {
            String id = setting.getId();
            String text = setting.getTitle();
            String iconStr = setting.getIcon();
            logger.debug("id = " + id);
            logger.debug("text = " + text);
            logger.debug("icon = " + iconStr);
            ImageIcon icon = ClientContext.getImageIcon(iconStr);
            JToggleButton tb = new JToggleButton(text, icon);
            if (ClientContext.isWin()) {
                tb.setMargin(new Insets(0, 0, 0, 0));
            }
            tb.setHorizontalTextPosition(SwingConstants.CENTER);
            tb.setVerticalTextPosition(SwingConstants.BOTTOM);
            itemPanel.add(tb);
            bg.add(tb);
            tb.setActionCommand(id);    // button �� actionCommand=id
            allBtns.add(tb);
        }

        //
        // �ݒ�p�l���̃R���e�i�ƂȂ�J�[�h�p�l��
        //
        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        // �R�}���h�{�^��
        String text = ClientContext.getString("settingDialog.saveButtonText");
        okButton = GUIFactory.createButton(text, null, null);
        okButton.setEnabled(false);

        // Cancel
        text = (String) UIManager.get("OptionPane.cancelButtonText");
        cancelButton = GUIFactory.createButton(text, "C", null);

        // �S�̃_�C�A���O�̃R���e���g�p�l��
        JPanel panel = new JPanel(new BorderLayout(11, 0));
        panel.add(itemPanel, BorderLayout.NORTH);
        panel.add(cardPanel, BorderLayout.CENTER);

        //
        // �_�C�A���O�𐶐�����
        //
        String title = ClientContext.getString("settingDialog.title");
        Object[] options = new Object[]{okButton, cancelButton};

        JOptionPane jop = new JOptionPane(
                panel,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                options,
                okButton);

        dialog = jop.createDialog((Frame) null, ClientContext.getFrameTitle(title));
        dialog.setResizable(true);
        logger.debug("dialog created");

        //
        // �C�x���g�ڑ����s��
        //
        connect();

    }

    /**
     * GUI �R���|�[�l���g�̃C�x���g�ڑ����s���B
     */
    private void connect() {

        //
        // �ݒ荀�ڃ{�^���ɒǉ�����A�N�V�������X�i�𐶐�����
        //
        ActionListener al = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {

                AbstractSettingPanel theSetting = null;

                // Action Command �ɐݒ�p�l����ID���ݒ肵�Ă���
                String name = event.getActionCommand();
                
                for (AbstractSettingPanel setting : allSettings) {
                    String id = setting.getId();
                    if (id.equals(name)) {
                        theSetting = setting;
                        break;
                    }
                }

                // �{�^���ɑΉ�����ݒ�p�l���ɃX�^�[�g��������
                if (theSetting != null) {
                    startSetting(theSetting);
                }
            }
        };

        //
        // �S�Ẵ{�^���Ƀ��X�i��ǉ�����
        //
        for (JToggleButton btn : allBtns) {
            btn.addActionListener(al);
        }

        // Save
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doOk();
            }
        });
        okButton.setEnabled(false);

        // Cancel
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        });

        // Dialog
        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                doCancel();
            }
        });
    }

    /**
     * �I�����ꂽ����(SettingPanel)�̕ҏW���J�n����.
     */
    private void startSetting(final AbstractSettingPanel sp) {

        //
        // ���ɐ�������Ă���ꍇ�͂����\������
        //
        if (sp.getContext() != null) {
            cardLayout.show(cardPanel, sp.getTitle());
            return;
        }

        Runnable r = new Runnable() {

            @Override
            public void run() {

                //
                // �܂���������Ă��Ȃ��ꍇ��
                // �I�����ꂽ�ݒ�p�l���𐶐����J�[�h�ɒǉ�����
                try {
                    settingMap.put(sp.getId(), sp);
                    sp.setContext(ProjectSettingDialog.this);
                    sp.setProjectStub(Project.getProjectStub());
                    sp.start();

                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            cardPanel.add(sp.getUI(), sp.getTitle());
                            cardLayout.show(cardPanel, sp.getTitle());
                            dialog.validate();
                            dialog.pack();

                            if (!dialog.isVisible()) {
                                Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
                                int x = (size.width - dialog.getPreferredSize().width) / 2;
                                int y = (size.height - dialog.getPreferredSize().height) / 3;
                                dialog.setLocation(x, y);
                                dialog.setVisible(true);
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Thread t = new Thread(r);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

    /**
     * SettingPanel �� state ���ω������ꍇ�ɒʒm���󂯁A
     * �S�ẴJ�[�h���X�L�������� OK �{�^�����R���g���[������B
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {

        String prop = e.getPropertyName();
        if (!prop.equals(AbstractSettingPanel.STATE_PROP)) {
            return;
        }

        //
        // �S�ẴJ�[�h���X�L�������� OK �{�^�����R���g���[������
        //
        boolean newOk = true;
        Iterator<AbstractSettingPanel> iter = settingMap.values().iterator();
        int cnt = 0;
        while (iter.hasNext()) {
            cnt++;
            AbstractSettingPanel p = iter.next();
            if (p.getState().equals(AbstractSettingPanel.State.INVALID_STATE)) {
                newOk = false;
                break;
            }
        }

        if (okState != newOk) {
            okState = newOk;
            okButton.setEnabled(okState);
        }
    }

    public void doOk() {

        Iterator<AbstractSettingPanel> iter = settingMap.values().iterator();
        while (iter.hasNext()) {
            AbstractSettingPanel p = iter.next();
            logger.debug(p.getTitle());
            p.save();
        }

        dialog.setVisible(false);
        dialog.dispose();
        notifyResult();
    }

    public void doCancel() {
        dialog.setVisible(false);
        dialog.dispose();
        notifyResult();
    }
}
