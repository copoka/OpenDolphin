package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import open.dolphin.delegater.StampDelegater;
import open.dolphin.helper.GridBagBuilder;
import open.dolphin.infomodel.FacilityModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.IStampTreeModel;
import open.dolphin.infomodel.ModelUtils;
import open.dolphin.infomodel.StampTreeModel;
import open.dolphin.helper.ComponentMemory;
import open.dolphin.project.Project;
import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;

/**
 * StampTreePublisher
 *
 * @author Kazushi, Minagawa
 *
 */
public class StampPublisher {
    
    public enum PublishedState {NONE, SAVED_NONE, LOCAL, GLOBAL};
    
    private static final int TT_NONE = -1;
    private static final int TT_LOCAL = 0;
    private static final int TT_PUBLIC = 1;
    private static final int WIDTH = 845;
    private static final int HEIGHT = 477;
    
    private StampBoxPlugin stampBox;
    private String title = "�X�^���v���J";
    
    //private JDialog dialog;
    private JFrame dialog;
    private JLabel infoLable;
    private JLabel instLabel;
    private JLabel publishedDate;
    private JTextField stampBoxName;
    private JTextField partyName;
    private JTextField contact;
    private JTextField description;
    private JRadioButton local;
    private JRadioButton publc;
    private JButton publish;
    private JButton cancel;
    private JButton cancelPublish;
    
    private JCheckBox[] entities;
    
    private JComboBox category;
    
    private int publishType = TT_NONE;
    private boolean okState;
            
    private StampDelegater sdl;
    
    private PublishedState publishState;
    
    private ApplicationContext appCtx;
    private Application app;
    private Logger logger;
    
    
    public StampPublisher(StampBoxPlugin stampBox) {
        this.stampBox = stampBox;
        appCtx = ClientContext.getApplicationContext();
        app = appCtx.getApplication();
        logger = ClientContext.getBootLogger();
    }
    
    public void start() {
        
        //dialog = new JDialog((JFrame) null, ClientContext.getFrameTitle(title), true);
        dialog = new JFrame(ClientContext.getFrameTitle(title));
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stop();
            }
        });
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int n = ClientContext.isMac() ? 3 : 2;
        int x = (screen.width - WIDTH) / 2;
        int y = (screen.height - HEIGHT) / n;
        ComponentMemory cm = new ComponentMemory(dialog, new Point(x, y), new Dimension(new Dimension(WIDTH, HEIGHT)), this);
        cm.setToPreferenceBounds();
        
        JPanel contentPane = createContentPane();
        contentPane.setOpaque(true);
        dialog.setContentPane(contentPane);
        
        stampBox.getBlockGlass().block();
        dialog.setVisible(true);
    }
    
    public void stop() {
        dialog.setVisible(false);
        dialog.dispose();
        stampBox.getBlockGlass().unblock();
    }
    
    private JPanel createContentPane() {
        
        JPanel contentPane = new JPanel();
        
        // GUI�R���|�[�l���g�𐶐�����
        infoLable = new JLabel(ClientContext.getImageIcon("about_16.gif"));
        instLabel = new JLabel("");
        instLabel.setFont(new Font("Dialog", Font.PLAIN, ClientContext.getInt("watingList.state.font.size")));
        publishedDate = new JLabel("");
        
        stampBoxName = GUIFactory.createTextField(15, null, null, null);
        partyName = GUIFactory.createTextField(20, null, null, null);
        contact = GUIFactory.createTextField(30, null, null, null);
        description = GUIFactory.createTextField(30, null, null, null);
        local = new JRadioButton(IInfoModel.PUBLISH_TREE_LOCAL);
        publc = new JRadioButton(IInfoModel.PUBLISH_TREE_PUBLIC);
        publish = new JButton("");
        publish.setEnabled(false);
        cancelPublish = new JButton("���J���~�߂�");
        cancelPublish.setEnabled(false);
        cancel = new JButton("�_�C�A���O�����");
        entities = new JCheckBox[IInfoModel.STAMP_NAMES.length];
        for (int i = 0; i < IInfoModel.STAMP_NAMES.length; i++) {
            entities[i] = new JCheckBox(IInfoModel.STAMP_NAMES[i]);
            if (IInfoModel.STAMP_NAMES[i].equals(IInfoModel.TABNAME_ORCA)) {
                entities[i].setEnabled(false);
            }
        }
        JPanel chkPanel1 = GUIFactory.createCheckBoxPanel(new JCheckBox[]{entities[0], entities[1], entities[2], entities[3], entities[4], entities[5], entities[6], entities[7]});
        JPanel chkPanel2 = GUIFactory.createCheckBoxPanel(new JCheckBox[]{entities[8], entities[9], entities[10], entities[11], entities[12], entities[13], entities[14], entities[15]});
        
        String[] categories = ClientContext.getStringArray("stamp.publish.categories");
        category = new JComboBox(categories);
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        categoryPanel.add(category);
        
        // ���J��RadioButton�p�l��
        JPanel radioPanel = GUIFactory.createRadioPanel(new JRadioButton[]{local, publc});
        
        // �����ݒ�p�l��
        GridBagBuilder gbl = new GridBagBuilder("�X�^���v���J�ݒ�");
        
        int y = 0;
        gbl.add(infoLable, 0, y, GridBagConstraints.EAST);
        gbl.add(instLabel, 1, y, GridBagConstraints.WEST);
        
        y++;
        gbl.add(new JLabel("���J�X�^���v�Z�b�g��"), 0, y, GridBagConstraints.EAST);
        gbl.add(stampBoxName, 1, y, GridBagConstraints.WEST);
        
        y++;
        gbl.add(new JLabel("���J��"), 0, y, GridBagConstraints.EAST);
        gbl.add(radioPanel, 1, y, GridBagConstraints.WEST);
        
        y++;
        gbl.add(new JLabel("�J�e�S��"), 0, y, GridBagConstraints.EAST);
        gbl.add(categoryPanel, 1, y, GridBagConstraints.WEST);
        
        y++;
        gbl.add(new JLabel("���J����X�^���v"), 0, y, GridBagConstraints.EAST);
        gbl.add(chkPanel1, 1, y, GridBagConstraints.WEST);
        
        y++;
        gbl.add(new JLabel(" "), 0, y, GridBagConstraints.EAST);
        gbl.add(chkPanel2, 1, y, GridBagConstraints.WEST);
        
        y++;
        gbl.add(new JLabel("���J�Җ�"), 0, y, GridBagConstraints.EAST);
        gbl.add(partyName, 1, y, GridBagConstraints.WEST);
        
        y++;
        gbl.add(new JLabel("URL��"), 0, y, GridBagConstraints.EAST);
        gbl.add(contact, 1, y, GridBagConstraints.WEST);
        
        y++;
        gbl.add(new JLabel("���p�҂ւ̐���"), 0, y, GridBagConstraints.EAST);
        gbl.add(description, 1, y, GridBagConstraints.WEST);
        
        y++;
        gbl.add(new JLabel("���J��"), 0, y, GridBagConstraints.EAST);
        gbl.add(publishedDate, 1, y, GridBagConstraints.WEST);
        
        // �R�}���h�p�l��
        JPanel cmdPanel = null;
        if (ClientContext.isMac()) {
            cmdPanel = GUIFactory.createCommandButtonPanel(new JButton[]{cancel, cancelPublish, publish});
        } else {
            cmdPanel = GUIFactory.createCommandButtonPanel(new JButton[]{publish, cancelPublish, cancel});
        }
        
        // �z�u����
        contentPane.setLayout(new BorderLayout(0, 17));
        contentPane.add(gbl.getProduct(), BorderLayout.CENTER);
        contentPane.add(cmdPanel, BorderLayout.SOUTH);
        contentPane.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        
        // PublishState �ɉ����ĐU�蕪����
        IStampTreeModel stmpTree = stampBox.getUserStampBox().getStampTreeModel();
        FacilityModel facility = Project.getUserModel().getFacilityModel();
        String facilityId = facility.getFacilityId();
        long treeId = stmpTree.getId();
        String publishTypeStr = stmpTree.getPublishType();
        
        if (treeId == 0L && publishTypeStr == null) {
            //
            // Stamptree��ۑ��i�ŏ��̃��O�C�����j
            //
            publishState = PublishedState.NONE;
        } else if (treeId != 0L && publishTypeStr == null) {
            //
            // �ۑ�����Ă���Stamptree�Ŕ���J�̃P�[�X
            //
            publishState = PublishedState.SAVED_NONE;
        } else if (treeId != 0L && publishTypeStr != null && publishTypeStr.equals(facilityId)) {
            //
            // publishType=facilityId ���[�J���Ɍ��J����Ă���
            //
            publishState = PublishedState.LOCAL;
        } else if (treeId != 0L && publishTypeStr != null && publishTypeStr.equals(IInfoModel.PUBLISHED_TYPE_GLOBAL)) {
            //
            // publishType=global �O���[�o���Ɍ��J����Ă���
            //
            publishState = PublishedState.GLOBAL;
        }
        
        // GUI�R���|�[�l���g�ɏ����l����͂���
        switch (publishState) {
            
            case NONE:
                instLabel.setText("���̃X�^���v�͌��J����Ă��܂���B");
                partyName.setText(facility.getFacilityName());
                String url = facility.getUrl();
                if (url != null) {
                    contact.setText(url);
                }
                String dateStr = ModelUtils.getDateAsString(new Date());
                publishedDate.setText(dateStr);
                publish.setText("���J����");
                break;
                
            case SAVED_NONE:
                instLabel.setText("���̃X�^���v�͌��J����Ă��܂���B");
                partyName.setText(stmpTree.getPartyName());
                url = facility.getUrl();
                if (url != null) {
                    contact.setText(url);
                }
                dateStr = ModelUtils.getDateAsString(new Date());
                publishedDate.setText(dateStr);
                publish.setText("���J����");
                break;
                
            case LOCAL:
                instLabel.setText("���̃X�^���v�͉@���Ɍ��J����Ă��܂��B");
                stampBoxName.setText(stmpTree.getName());
                local.setSelected(true);
                publc.setSelected(false);
                publishType = TT_LOCAL;
                
                //
                // Publish ���Ă��� Entity ���`�F�b�N����
                //
                String published = ((StampTreeModel) stmpTree).getPublished();
                if (published != null) {
                    StringTokenizer st = new StringTokenizer(published, ",");
                    while (st.hasMoreTokens()) {
                        String entity = st.nextToken();
                        for (int i = 0; i < IInfoModel.STAMP_ENTITIES.length; i++) {
                            if (entity.equals(IInfoModel.STAMP_ENTITIES[i])) {
                                entities[i].setSelected(true);
                                break;
                            }
                        }
                    }
                }
                
                category.setSelectedItem(stmpTree.getCategory());
                partyName.setText(stmpTree.getPartyName());
                contact.setText(stmpTree.getUrl());
                description.setText(stmpTree.getDescription());
                StringBuilder sb = new StringBuilder();
                sb.append(ModelUtils.getDateAsString(stmpTree.getPublishedDate()));
                sb.append("  �ŏI�X�V��( ");
                sb.append(ModelUtils.getDateAsString(stmpTree.getLastUpdated()));
                sb.append(" )");
                publishedDate.setText(sb.toString());
                publish.setText("�X�V����");
                publish.setEnabled(true);
                cancelPublish.setEnabled(true);
                break;
                
            case GLOBAL:
                instLabel.setText("���̃X�^���v�̓O���[�o���Ɍ��J����Ă��܂��B");
                stampBoxName.setText(stmpTree.getName());
                local.setSelected(false);
                publc.setSelected(true);
                category.setSelectedItem(stmpTree.getCategory());
                partyName.setText(stmpTree.getPartyName());
                contact.setText(stmpTree.getUrl());
                description.setText(stmpTree.getDescription());
                publishType = TT_PUBLIC;
                
                published = ((StampTreeModel) stmpTree).getPublished();
                if (published != null) {
                    StringTokenizer st = new StringTokenizer(published, ",");
                    while (st.hasMoreTokens()) {
                        String entity = st.nextToken();
                        for (int i = 0; i < IInfoModel.STAMP_ENTITIES.length; i++) {
                            if (entity.equals(IInfoModel.STAMP_ENTITIES[i])) {
                                entities[i].setSelected(true);
                                break;
                            }
                        }
                    }
                }
                
                sb = new StringBuilder();
                sb.append(ModelUtils.getDateAsString(stmpTree.getPublishedDate()));
                sb.append("  �ŏI�X�V��( ");
                sb.append(ModelUtils.getDateAsString(stmpTree.getLastUpdated()));
                sb.append(" )");
                publishedDate.setText(sb.toString());
                publish.setText("�X�V����");
                publish.setEnabled(true);
                cancelPublish.setEnabled(true);
                break;
        }
        
        // �R���|�[�l���g�̃C�x���g�ڑ����s��
        // Text���͂��`�F�b�N����
        ReflectDocumentListener dl = new ReflectDocumentListener(this, "checkButton");
        stampBoxName.getDocument().addDocumentListener(dl);
        partyName.getDocument().addDocumentListener(dl);
        contact.getDocument().addDocumentListener(dl);
        description.getDocument().addDocumentListener(dl);
        
        // RadioButton
        ButtonGroup bg = new ButtonGroup();
        bg.add(local);
        bg.add(publc);
        PublishTypeListener pl = new PublishTypeListener();
        local.addActionListener(pl);
        publc.addActionListener(pl);
        
        // CheckBox listener
        ReflectActionListener cbListener = new ReflectActionListener(this, "checkButton");
        for (JCheckBox cb : entities) {
            cb.addActionListener(cbListener);
        }
        
        // publish & cancel
        publish.addActionListener(new ReflectActionListener(this, "publish"));
        cancelPublish.addActionListener(new ReflectActionListener(this, "cancelPublish"));
        cancel.addActionListener(new ReflectActionListener(this, "stop"));
        
        return contentPane;
    }
    
    class PublishTypeListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            if (local.isSelected()) {
                publishType = TT_LOCAL;
                category.setSelectedIndex(ClientContext.getInt("stamp.publish.categories.localItem"));
            } else if (publc.isSelected()) {
                publishType = TT_PUBLIC;
            }
            checkButton();
        }
    }
    
    /**
     * �X�^���v�����J����B
     */
    public void publish() {
        
        //
        // ���J����StampTree���擾����
        //
        ArrayList<StampTree> publishList = new ArrayList<StampTree>(IInfoModel.STAMP_ENTITIES.length);
        
        // Entity �̃J���}�A���p StringBuilder 
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < IInfoModel.STAMP_ENTITIES.length; i++) {
            
            if (entities[i].isSelected()) {
                //
                // Entity �`�F�b�N�{�b�N�X���`�F�b�N����Ă��鎞
                // �Ή�����Entity�����擾����
                //
                String entity = IInfoModel.STAMP_ENTITIES[i];
                
                //
                // StampBox ����Emtity�ɑΉ�����StampTree�𓾂�
                //
                StampTree st = stampBox.getStampTreeFromUserBox(entity);
                
                //
                // ���J���X�g�ɉ�����
                //
                publishList.add(st);
                
                // Entity �����J���}�ŘA������
                sb.append(",");
                sb.append(entity);
            }
        }
        String published = sb.toString();
        published = published.substring(1);
        
        //
        // ���J���� StampTree �� XML �f�[�^�𐶐�����
        //
        DefaultStampTreeXmlBuilder builder = new DefaultStampTreeXmlBuilder();
        StampTreeXmlDirector director = new StampTreeXmlDirector(builder);
        String publishXml = director.build(publishList);
        byte[] bytes = null;
        try {
            bytes = publishXml.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        final byte[] publishBytes = bytes;
        
        //
        // ���J���̎����i�l�p�j�� StampTree �Ɠ������Ƃ�
        // ���J���̎����i�l�p�jStamptree��ۑ�/�X�V����
        //
        List<StampTree> personalTree = stampBox.getUserStampBox().getAllTrees();
        builder = new DefaultStampTreeXmlBuilder();
        director = new StampTreeXmlDirector(builder);
        String treeXml = director.build((ArrayList<StampTree>) personalTree);
        
        //
        // �l�p��StampTreeModel�Ɍ��J����XML���Z�b�g����
        //
        final open.dolphin.infomodel.StampTreeModel stmpTree = (open.dolphin.infomodel.StampTreeModel) stampBox.getUserStampBox().getStampTreeModel();
        stmpTree.setTreeXml(treeXml);
        
        //
        // ���J����ݒ肷��
        //
        stmpTree.setName(stampBoxName.getText().trim());
        String pubType = publc.isSelected() ? IInfoModel.PUBLISHED_TYPE_GLOBAL : Project.getUserModel().getFacilityModel().getFacilityId();
        stmpTree.setPublishType(pubType);
        stmpTree.setCategory((String) category.getSelectedItem());
        stmpTree.setPartyName(partyName.getText().trim());
        stmpTree.setUrl(contact.getText().trim());
        stmpTree.setDescription(description.getText().trim());
        stmpTree.setPublished(published);
        
        // ���J�y�эX�V����ݒ肷��
        switch (publishState) {
            case NONE:
            case SAVED_NONE:
                Date date = new Date();
                stmpTree.setPublishedDate(date);
                stmpTree.setLastUpdated(date);
                break;
            case LOCAL:
            case GLOBAL:
                stmpTree.setLastUpdated(new Date());
                break;
        }
        
        // Delegator �𐶐�����
        sdl = new StampDelegater();
        
        int delay = 200;
        int maxEstimation = 30*1000;
    
        
        Task task = new Task<Boolean, Void>(app) {
        
        
            @Override
            protected Boolean doInBackground() throws Exception {

                switch (publishState) {

                    case NONE:
                        //
                        // �ŏ��̃��O�C�����A�܂�������Stamptree���ۑ�����Ă��Ȃ���Ԃ̎�
                        // �����i�l�p�jStampTreeModel��ۑ������J����
                        //
                        long id = sdl.saveAndPublishTree(stmpTree, publishBytes);
                        stmpTree.setId(id);
                        break;

                    case SAVED_NONE:
                        //
                        // �����p��StampTree�������ĐV�K�Ɍ��J����ꍇ
                        //
                        sdl.publishTree(stmpTree, publishBytes);
                        break;

                    case LOCAL:
                        //
                        // Local�Ɍ��J����Ă��čX�V����ꍇ
                        //
                        sdl.updatePublishedTree(stmpTree, publishBytes);
                        break;

                    case GLOBAL:
                        //
                        // Global �Ɍ��J����Ă��čX�V����ꍇ
                        //
                        sdl.updatePublishedTree(stmpTree, publishBytes);
                        break;
                }
                return new Boolean(sdl.isNoError());
            }
            
            @Override
            protected void succeeded(Boolean result) {
                logger.debug("Task succeeded");
                if (result.booleanValue()) {
                    JOptionPane.showMessageDialog(dialog,
                            "�X�^���v�����J���܂����B",
                            ClientContext.getFrameTitle(title),
                            JOptionPane.INFORMATION_MESSAGE);
                    stop();

                } else {
                    JOptionPane.showMessageDialog(dialog,
                            sdl.getErrorMessage(),
                            ClientContext.getFrameTitle(title),
                            JOptionPane.WARNING_MESSAGE);
                }
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
        String message = "�X�^���v���J";
        String note = "���J���Ă��܂�...";
        Component c = dialog;
        TaskTimerMonitor w = new TaskTimerMonitor(task, taskMonitor, c, message, note, delay, maxEstimation);
        taskMonitor.addPropertyChangeListener(w);
        
        appCtx.getTaskService().execute(task);
    }
    
    /**
     * ���J���Ă���Tree���������B
     */
    public void cancelPublish() {
        
        // �m�F���s��
        JLabel msg1 = new JLabel("���J���������ƃT�u�X�N���C�u���Ă��郆�[�U�����Ȃ���");
        JLabel msg2 = new JLabel("�X�^���v���g�p�ł��Ȃ��Ȃ�܂��B���J���������܂���?");
        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
        p1.add(msg1);
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
        p2.add(msg2);
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.add(p1);
        box.add(p2);
        box.setBorder(BorderFactory.createEmptyBorder(0, 0, 11, 11));
        
        int option = JOptionPane.showConfirmDialog(dialog,
                new Object[]{box},
                ClientContext.getFrameTitle(title),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                ClientContext.getImageIcon("sinfo_32.gif"));
        
        if (option != JOptionPane.YES_OPTION) {
            return;
        }
        
        //
        // StampTree ��\�� XML �f�[�^�𐶐�����
        //
        List<StampTree> list = stampBox.getUserStampBox().getAllTrees();
        DefaultStampTreeXmlBuilder builder = new DefaultStampTreeXmlBuilder();
        StampTreeXmlDirector director = new StampTreeXmlDirector(builder);
        String treeXml = director.build((ArrayList<StampTree>) list);
        
        //
        // �l�p��StampTreeModel��XML���Z�b�g����
        //
        final open.dolphin.infomodel.StampTreeModel stmpTree = (open.dolphin.infomodel.StampTreeModel) stampBox.getUserStampBox().getStampTreeModel();
        
        //
        // ���J�f�[�^���N���A����
        //
        stmpTree.setTreeXml(treeXml);
        stmpTree.setPublishType(null);
        stmpTree.setPublishedDate(null);
        stmpTree.setLastUpdated(null);
        stmpTree.setCategory(null);
        stmpTree.setName(ClientContext.getString("stampTree.personal.box.name"));
        stmpTree.setDescription(ClientContext.getString("stampTree.personal.box.tooltip"));
        
        sdl = new StampDelegater();
        
        int delay = 200;
        int maxEstimation = 60*1000;       
        
        Task task = new Task<Boolean, Void>(app) {
               
            @Override
            protected Boolean doInBackground() throws Exception {
                sdl.cancelPublishedTree(stmpTree);
                return new Boolean(sdl.isNoError());
            }
            
            @Override
            protected void succeeded(Boolean result) {
                logger.debug("Task succeeded");
                if (result.booleanValue()) {
                    JOptionPane.showMessageDialog(dialog,
                            "���J���������܂����B",
                            ClientContext.getFrameTitle(title),
                            JOptionPane.INFORMATION_MESSAGE);
                    stop();

                } else {
                    JOptionPane.showMessageDialog(dialog,
                            sdl.getErrorMessage(),
                            ClientContext.getFrameTitle(title),
                            JOptionPane.WARNING_MESSAGE);
                }
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
        String message = "�X�^���v���J";
        String note = "���J���������Ă��܂�...";
        Component c = dialog;
        TaskTimerMonitor w = new TaskTimerMonitor(task, taskMonitor, c, message, note, delay, maxEstimation);
        taskMonitor.addPropertyChangeListener(w);
        
        appCtx.getTaskService().execute(task);
    }
    
    public void checkButton() {
        
        switch (publishType) {
            case TT_NONE:
                break;
                
            case TT_LOCAL:
                boolean stampNameOk = stampBoxName.getText().trim().equals("") ? false : true;
                boolean partyNameOk = partyName.getText().trim().equals("") ? false : true;
                boolean descriptionOk = description.getText().trim().equals("") ? false : true;
                boolean checkOk = false;
                for (JCheckBox cb : entities) {
                    if (cb.isSelected()) {
                        checkOk = true;
                        break;
                    }
                }
                boolean newOk = (stampNameOk && partyNameOk && descriptionOk && checkOk) ? true : false;
                if (newOk != okState) {
                    okState = newOk;
                    publish.setEnabled(okState);
                }
                break;
                
            case TT_PUBLIC:
                stampNameOk = stampBoxName.getText().trim().equals("") ? false : true;
                partyNameOk = partyName.getText().trim().equals("") ? false : true;
                boolean urlOk = contact.getText().trim().equals("") ? false : true;
                descriptionOk = description.getText().trim().equals("") ? false : true;
                checkOk = false;
                for (JCheckBox cb : entities) {
                    if (cb.isSelected()) {
                        checkOk = true;
                        break;
                    }
                }
                newOk = (stampNameOk && partyNameOk && urlOk && descriptionOk && checkOk) ? true : false;
                if (newOk != okState) {
                    okState = newOk;
                    publish.setEnabled(okState);
                }
                break;
        }
    }
}



