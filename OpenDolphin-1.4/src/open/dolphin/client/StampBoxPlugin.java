package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.IStampTreeModel;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.PublishedTreeModel;
import open.dolphin.infomodel.RegisteredDiagnosisModel;
import open.dolphin.infomodel.StampTreeModel;
import open.dolphin.order.EditorSetPanel;
import open.dolphin.helper.ComponentMemory;
import open.dolphin.project.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

/**
 * StampBox �N���X�B
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class StampBoxPlugin extends AbstractMainTool {
    
    private static final String NAME = "�X�^���v��";
    
    // frame�̃f�t�H���g�̑傫���y�у^�C�g��
    private final int DEFAULT_WIDTH     = 320;
    private final int DEFAULT_HEIGHT    = 690;
    private final int IMPORT_TREE_OFFSET = 1;
    
    // StampBox �� JFrame
    private JFrame frame;
    
    // StampBox
    private JTabbedPane parentBox;
    
    //���[�U�l�p�� StampBox
    private AbstractStampBox userBox;
    
    // ���ݑI������Ă��� StampBox
    private AbstractStampBox curBox;
    
    // �C���|�[�g���Ă��� StampTree �̃��X�g
    private List<Long> importedTreeList;
    
    // ���ݑI������Ă��� StampBox �̏���\�����郉�x��
    private JLabel curBoxInfo;
    
    // StampBox JFrame ��contentPane
    private JPanel content;
    
    // Stampmaker �{�^��
    private JToggleButton toolBtn;
    
    // ���J�{�^��
    private JButton publishBtn;
    
    // �C���|�[�g�{�^��
    private JButton importBtn;
    
    // StampMaker �̃G�f�B�^�Z�b�g
    private EditorSetPanel editors;
    
    // Editor�̕ҏW�l���X�i
    private EditorValueListener editorValueListener;
    
    // StampMaker ���[�h�̃t���O
    private boolean editing;
    
    // StampBox �ʒu
    private Point stampBoxLoc;
    
    // StampBox ��
    private int stampBoxWidth;
    
    // StampBox ����
    private int stampBoxHeight;
    
    // Block Glass Pane
    private BlockGlass glass;
    
    // Container Panel
    private JPanel stampBoxPanel;
    
    // ���̃X�^���v�{�b�N�X�� StmpTreeModel
    private List<IStampTreeModel> stampTreeModels;
    
    // Logger
    private Logger logger;
    
    /**
     * Creates new StampBoxPlugin
     */
    public StampBoxPlugin() {
        setName(NAME);
        logger = ClientContext.getBootLogger();
    }
    
    /**
     * StampTreeModel ��Ԃ��B
     * @return StampTreeModel�̃��X�g
     */
    public List<IStampTreeModel> getStampTreeModels() {
        return stampTreeModels;
    }
    
    /**
     * StampTreeModel ��ݒ肷��B
     * @param stampTreeModels StampTreeModel�̃��X�g
     */
    public void setStampTreeModels(List<IStampTreeModel> stampTreeModels) {
        this.stampTreeModels = stampTreeModels;
    }
    
    /**
     * ���݂�StampBox��Ԃ��B
     * @return ���ݑI������Ă���StampBox
     */
    public AbstractStampBox getCurrentBox() {
        return curBox;
    }
    
    /**
     * ���݂�StampBox��ݒ肷��B
     * @param curBox �I�����ꂽStampBox
     */
    public void setCurrentBox(AbstractStampBox curBox) {
        this.curBox = curBox;
    }
    
    /**
     * User(�l�p)��StampBox��Ԃ��B
     * @return User(�l�p)��StampBox
     */
    public AbstractStampBox getUserStampBox() {
        return userBox;
    }
    
    /**
     * User(�l�p)��StampBox��ݒ肷��B
     * @param userBox User(�l�p)��StampBox
     */
    public void setUserStampBox(AbstractStampBox userBox) {
        this.userBox = userBox;
    }
    
    /**
     * StampBox �� JFrame ��Ԃ��B
     * @return StampBox �� JFrame
     */
    public JFrame getFrame() {
        return frame;
    }
    
    /**
     * �C���|�[�g���Ă���StampTree�̃��X�g��Ԃ��B
     * @return �C���|�[�g���Ă���StampTree�̃��X�g
     */
    public List<Long> getImportedTreeList() {
        return importedTreeList;
    }
    
    /**
     * Block�pGlassPane��Ԃ��B
     * @return Block�pGlassPane
     */
    public BlockGlass getBlockGlass() {
        return glass;
    }
    
    /**
     * �v���O�������J�n����B
     */
    @Override
    public void start() {
        
        if (stampTreeModels == null) {
            logger.fatal("StampTreeModel is null");
            throw new RuntimeException("Fatal error: StampTreeModel is null at start.");
        }
        
        //
        // StampBox��JFrame�𐶐�����
        //
        String title = ClientContext.getFrameTitle(getName());
        Rectangle setBounds = new Rectangle(0, 0, 1000, 690);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int defaultX = (screenSize.width - setBounds.width) / 2;
        int defaultY = (screenSize.height - setBounds.height) / 2;
        int defaultWidth = setBounds.width;
        int defaultHeight = setBounds.height;
        setBounds = new Rectangle(defaultX, defaultY, defaultWidth, defaultHeight);
        int x = (defaultX + defaultWidth) - DEFAULT_WIDTH;
        int y = defaultY;
        int width = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;
        frame = new JFrame(title);
        glass = new BlockGlass();
        frame.setGlassPane(glass);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (editing) {
                    toolBtn.doClick();
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });
        ComponentMemory cm = new ComponentMemory(frame, new Point(x, y), new Dimension(width, height), this);
        cm.setToPreferenceBounds();
        
        //
        // �S�̂̃{�b�N�X�𐶐�����
        //
        parentBox = new JTabbedPane();
        parentBox.setTabPlacement(JTabbedPane.BOTTOM);
        
        //
        // �ǂݍ���StampTree��TabbedPane�Ɋi�[���A����ɂ����parentBox�ɒǉ�����
        //
        for (IStampTreeModel model : stampTreeModels) {
            
            if (model != null) {
                
                logger.debug("id = " + model.getId());
                logger.debug("name = " + model.getName());
                logger.debug("publishType = " + model.getPublishType());
                logger.debug("category = " + model.getCategory());
                logger.debug("partyName = " + model.getPartyName());
                logger.debug("url = " + model.getUrl());
                logger.debug("description = " + model.getDescription());
                logger.debug("publishedDate = " + model.getPublishedDate());
                logger.debug("lastUpdated = " + model.getLastUpdated());
                logger.debug("userId = " + model.getUser());
                
                //
                // ���[�U�l�pStampTree�̏ꍇ
                //
                if (model.getUser().getId() == Project.getUserModel().getId() && model instanceof StampTreeModel) {
                    
                    //
                    // �l�p�̃X�^���v�{�b�N�X(JTabbedPane)�𐶐�����
                    //
                    userBox = new UserStampBox();
                    userBox.setContext(this);
                    userBox.setStampTreeModel(model);
                    userBox.buildStampBox();
                    
                    //
                    // ParentBox �ɒǉ�����
                    //
                    parentBox.addTab(ClientContext.getString("stampTree.personal.box.name"), userBox);
                    
                } else if (model instanceof PublishedTreeModel) {
                    //
                    // �C���|�[�g���Ă���Tree�̏ꍇ
                    //
                    importPublishedTree(model);
                }
                model.setTreeXml(null);
            }
        }
        
        //
        // StampTreeModel �� clear ����
        //
        stampTreeModels.clear();
        
        // ParentBox ��Tab �� tooltips ��ݒ肷��
        for (int i = 0; i < parentBox.getTabCount(); i++) {
            AbstractStampBox box = (AbstractStampBox) parentBox.getComponentAt(i);
            parentBox.setToolTipTextAt(i, box.getInfo());
        }
        
        //
        // ParentBox��ChangeListener��o�^���X�^���v���[�J�̐�����s��
        //
        parentBox.addChangeListener(new BoxChangeListener());
        setCurrentBox(userBox);
        
        //
        // ���[�UBox�p��ChangeListener��ݒ肷��
        //
        userBox.addChangeListener(new TabChangeListener());
        
        //
        // �X�^���v���[�J���N�����邽�߂̃{�^���𐶐�����
        //
        toolBtn = new JToggleButton(ClientContext.getImageIcon("tools_24.gif"));
        toolBtn.setToolTipText("�X�^���v���[�J���N�����܂�");
        toolBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!editing) {
                    startStampMake();
                    editing = true;
                } else {
                    stopStampMake();
                    editing = false;
                }
            }
        });
        
        //
        // �X�^���v���J�{�^���𐶐�����
        //
        publishBtn = new JButton(ClientContext.getImageIcon("exp_24.gif"));
        publishBtn.setToolTipText("�X�^���v�̌��J���Ǘ������܂�");
        publishBtn.addActionListener(new ReflectActionListener(this, "publishStamp"));
        
        //
        // �C���|�[�g�{�^���𐶐�����
        //
        importBtn = new JButton(ClientContext.getImageIcon("impt_24.gif"));
        importBtn.setToolTipText("�X�^���v�̃C���|�[�g���Ǘ������܂�");
        importBtn.addActionListener(new ReflectActionListener(this, "importStamp"));
        
        //
        // curBoxInfo���x���𐶐�����
        //
        curBoxInfo = new JLabel("");
        curBoxInfo.setFont(GUIFactory.createSmallFont());
        
        //
        // ���C�A�E�g����
        //
        stampBoxPanel = new JPanel(new BorderLayout());
        stampBoxPanel.add(parentBox, BorderLayout.CENTER);
        JPanel cmdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cmdPanel.add(toolBtn);
        cmdPanel.add(publishBtn);
        cmdPanel.add(importBtn);
        cmdPanel.add(curBoxInfo);
        stampBoxPanel.add(cmdPanel, BorderLayout.NORTH);
        
        //
        // �R���e���g�p�l���𐶐�����
        //
        content = new JPanel(new BorderLayout());
        content.add(stampBoxPanel, BorderLayout.CENTER);
        content.setOpaque(true);
        
        //
        // Frame �ɉ�����
        //
        frame.setContentPane(content);
        
        //
        // �O��I�����̃^�u��I������
        //
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        String name = this.getClass().getName();
        int index = prefs.getInt(name + "_parentBox", 0);
        index = ( index >= 0 && index <= (parentBox.getTabCount() -1) ) ? index : 0;
        parentBox.setSelectedIndex(index);
        index = prefs.getInt(name + "_stampBox", 0);
        index = ( index >= 0 && index <= (userBox.getTabCount() -1) ) ? index : 0;
        
        //
        // ORCA �^�u���I������Ă��� ORCA �ɐڑ����Ȃ��ꍇ�������
        //
        index = index == IInfoModel.TAB_INDEX_ORCA ? 0 : index;
        userBox.setSelectedIndex(index);
        
        //
        // �{�^�����R���g���[������
        //
        boxChanged();
    }
    
    /**
     * �I������Ă���Index�Ń{�^���𐧌䂷��B
     */
    private void boxChanged() {
        
        int index = parentBox.getSelectedIndex();
        setCurrentBox((AbstractStampBox) parentBox.getComponentAt(index));
        String info = getCurrentBox().getInfo();
        curBoxInfo.setText(info);
        
        if (getCurrentBox() == userBox) {
            publishBtn.setEnabled(true);
            int index2 = userBox.getSelectedIndex();
            boolean enabled = userBox.isHasEditor(index2);
            toolBtn.setEnabled(enabled);
            
        } else {
            toolBtn.setEnabled(false);
            publishBtn.setEnabled(false);
        }
    }
    
    /**
     * Import����StampBox�̑I���\�𐧌䂷��B
     * @param enabled �I���\�Ȏ� true
     */
    private void enabledImportBox(boolean enabled) {
        int cnt = parentBox.getTabCount();
        for (int i = 0 ; i < cnt; i++) {
            if ((JTabbedPane) parentBox.getComponentAt(i) != userBox) {
                parentBox.setEnabledAt(i, enabled);
            }
        }
    }
    
    /**
     * TabChangeListener
     * User�pStampBox��Tab�؂�ւ����X�i�N���X�B
     */
    class TabChangeListener implements ChangeListener {
        
        @Override
        public void stateChanged(ChangeEvent e) {
            
            if (!editing) {
                // �X�^���v���[�J�N�����łȂ���
                // �e�L�X�g�X�^���v�^�u���I�����ꂽ��X�^���v���[�J�{�^���� disabled�ɂ���
                // ORCA �Z�b�g�^�u�̏ꍇ����������
                int index = userBox.getSelectedIndex();
                StampTree tree = userBox.getStampTree(index);
                tree.enter();
                boolean enabled = userBox.isHasEditor(index);
                toolBtn.setEnabled(enabled);
                
            } else {
                // �X�^���v���[�J�N�����̎�
                // �I�����ꂽ�^�u�ɑΉ�����G�f�B�^��\������
                int index = userBox.getSelectedIndex();
                StampTree tree = userBox.getStampTree(index);
                if (editors != null && (!tree.getEntity().equals(IInfoModel.ENTITY_TEXT)) ) {
                    editors.show(tree.getEntity());
                }
            }
        }
    }
    
    /**
     * ParentBox �� TabChangeListener�N���X�B
     */
    class BoxChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            boxChanged();
        }
    }

    /**
     * �X�^���v���[�J���N������B
     */
    public void startStampMake() {

        if (editing) {
            return;
        }

        Runnable awt = new Runnable() {

            @Override
            public void run() {
                createAndShowEditorSet();
            }
        };

        SwingUtilities.invokeLater(awt);
    }
    
    /**
     * �X�^���v���[�J���N������B
     */
    private void createAndShowEditorSet() {

        // �C���|�[�g�{�b�N�X��I��s�ɂ���
        enabledImportBox(false);
        
        // ���݂̈ʒu�Ƒ傫����ۑ�����
        stampBoxLoc = frame.getLocation();
        stampBoxWidth = frame.getWidth();
        stampBoxHeight = frame.getHeight();
        
        // ���݂̃^�u����tree��Entity�𓾂�
        int index = userBox.getSelectedIndex();
        StampTree tree = userBox.getStampTree(index);
        String entity = tree.getEntity();
        
        // �G�f�B�^�Z�b�g�𐶐�����
        editors = new EditorSetPanel();

        // text �^�u��I��s�ɂ���
        userBox.setHasNoEditorEnabled(false);

        //
        // �S Tree �� edirorSet �� treeSelectionListener �Ƃ��ēo�^����
        //
        List<StampTree> allTrees = userBox.getAllTrees();
        for (StampTree st : allTrees) {
            st.addTreeSelectionListener(editors);
        }

        // Editor�֕ҏW�l���󂯂Ƃ邽�߂̃��X�i��o�^����
        editorValueListener = new EditorValueListener();
        editors.addPropertyChangeListener(EditorSetPanel.EDITOR_VALUE_PROP, editorValueListener);

        // EditorSet �֌��݂�entity(Tree)�ɑΉ�����G�f�B�^��\��������
        editors.show(entity);

        // StampBox �� Frame ���č\�z����
        frame.setVisible(false);
        content.removeAll();
        content.add(editors, BorderLayout.CENTER);
        content.add(stampBoxPanel, BorderLayout.EAST);
        stampBoxPanel.setPreferredSize(new Dimension(300, 650));
        content.revalidate();
        frame.pack();
        
        // �O��I�����̈ʒu�ƃT�C�Y���擾����
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        String name = this.getClass().getName();
        int locX = prefs.getInt(name + ".stampmMaker.x", 0);
        int locY = prefs.getInt(name + ".stampmMaker.y", 0);
        int width = prefs.getInt(name + ".stampmMaker.width", 0);
        int height = prefs.getInt(name + ".stampmMaker.height", 0);

        width=0;
        height=0;

        if (width == 0 || height == 0) {
            // �Z���^�����O����
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (screen.width - frame.getSize().width)/2;
            int y = (screen.height - frame.getSize().height)/2;
            frame.setLocation(x, y);
        } else {
            frame.setLocation(locX, locY);
            frame.setSize(width, height);
        }

        frame.setVisible(true);
        editing = true;
        toolBtn.setToolTipText("�X�^���v���[�J���I�����܂�");
        publishBtn.setEnabled(false);
        importBtn.setEnabled(false);
    }


    public void stopStampMake() {

        if (!editing) {
            return;
        }

        Runnable awt = new Runnable() {

            @Override
            public void run() {
                disposeEditorSet();
            }
        };

        SwingUtilities.invokeLater(awt);
    }

    
    /**
     * �X�^���v���[�J���I������B
     */
    private void disposeEditorSet() {
        
        // ���݂̑傫���ƈʒu��Preference�ɕۑ�����
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        String name = this.getClass().getName();
        prefs.putInt(name + ".stampmMaker.x", frame.getLocation().x);
        prefs.putInt(name + ".stampmMaker.y", frame.getLocation().y);
        prefs.putInt(name + ".stampmMaker.width", frame.getWidth());
        prefs.putInt(name + ".stampmMaker.height", frame.getHeight());
        
        editors.close();
        editors.removePropertyChangeListener(EditorSetPanel.EDITOR_VALUE_PROP, editorValueListener);
        List<StampTree> allTrees = userBox.getAllTrees();
        for (StampTree st : allTrees) {
            //st.removePropertyChangeListener(StampTree.SELECTED_NODE_PROP, editors);
            st.removeTreeSelectionListener(editors);
        }
        
        content.removeAll();
        content.add(stampBoxPanel, BorderLayout.CENTER);
        
        editors = null;
        editorValueListener = null;
        userBox.setHasNoEditorEnabled(true);
        content.revalidate();
        frame.setLocation(stampBoxLoc);
        frame.setSize(new Dimension(stampBoxWidth, stampBoxHeight));
        editing = false;
        toolBtn.setToolTipText("�X�^���v���[�J���N�����܂�");
        publishBtn.setEnabled(true);
        importBtn.setEnabled(true);
        
        //
        // ASP �{�b�N�X��I���ɂ���
        //
        enabledImportBox(true);
    }
    
    /**
     * EditorValueListener
     * �G�f�B�^�ō쐬�����X�^���v��StampTree�ɉ�����B
     */
    class EditorValueListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            Object obj = e.getNewValue();
            if (obj != null && obj instanceof ModuleModel) {
                ModuleModel stamp = (ModuleModel) obj;
                String entity = stamp.getModuleInfo().getEntity();
                StampTree tree = userBox.getStampTree(entity);
                tree.addStamp(stamp, null);
                
            } else if (obj != null && obj instanceof ArrayList) {
                // ���a��
                StampTree tree = getStampTree(IInfoModel.ENTITY_DIAGNOSIS);
                tree.addDiagnosis((ArrayList<RegisteredDiagnosisModel>) obj);
            }
        }
    }
    
    /**
     * �X�^���v�p�u���b�V���[���N������B
     */
    public void publishStamp() {
        StampPublisher publisher = new StampPublisher(this);
        publisher.start();
    }
    
    /**
     * �X�^���v�C���|�[�^�[���N������B
     */
    public void importStamp() {
        StampImporter importer = new StampImporter(this);
        importer.start();
    }
    
    /**
     * ���J����Ă���X�^���vTree���C���|�[�g����B
     * @param importTree �C���|�[�g������JTree
     */
    public void importPublishedTree(IStampTreeModel importTree) {
        
        //
        // Asp StampBox �𐶐��� parentBox �ɉ�����
        //
        AbstractStampBox aspBox = new AspStampBox();
        aspBox.setContext(this);
        aspBox.setStampTreeModel(importTree);
        aspBox.buildStampBox();
        parentBox.addTab(importTree.getName(), aspBox);
        
        //
        // �C���|�[�g���X�g�ɒǉ�����
        //
        if (importedTreeList == null) {
            importedTreeList = new ArrayList<Long>(5);
        }
        importedTreeList.add(new Long(importTree.getId()));
    }
    
    /**
     * �C���|�[�g���Ă�����JTree���폜����B
     * @param removeId �폜������JTree��Id
     */
    public void removeImportedTree(long removeId) {
        
        if (importedTreeList != null) {
            for (int i = 0; i < importedTreeList.size(); i++) {
                Long id = importedTreeList.get(i);
                if (id.longValue() == removeId) {
                    parentBox.removeTabAt(i+IMPORT_TREE_OFFSET);
                    importedTreeList.remove(i);
                    break;
                }
            }
        }
    }
    
    /**
     * �v���O�������I������B
     */
    @Override
    public void stop() {
        frame.setVisible(false);
        frame.dispose();
    }
    
    /**
     * �t���[����O�ʂɏo���B
     */
    @Override
    public void enter() {
        if (frame != null) {
            frame.toFront();
        }
    }

    /**
     * �A�v���P�[�V�����̏I�����ɃX�^���v�c���[��Ԃ��ۑ�����B
     * @return StamPtreeMode; 
     */
    public IStampTreeModel getUsersTreeTosave() {

        preSave();

        //
        // User Tree �݂̂�ۑ�����
        //
        ArrayList<StampTree> list = (ArrayList<StampTree>) userBox.getAllTrees();
        if (list == null || list.size() == 0) {
            // never
            return null;
        }

        //
        // ORCA �Z�b�g�͏���
        //
        for (StampTree tree : list) {
            if (tree.getTreeInfo().getEntity().equals(IInfoModel.ENTITY_ORCA)) {
                list.remove(tree);
                logger.debug("ORCA�Z�b�g�������܂���");
                break;
            }
        }

        // StampTree ��\�� XML �f�[�^�𐶐�����
        DefaultStampTreeXmlBuilder builder = new DefaultStampTreeXmlBuilder();
        StampTreeXmlDirector director = new StampTreeXmlDirector(builder);
        String treeXml = director.build(list);

        // �l�p��StampTreeModel��XML���Z�b�g����
        IStampTreeModel treeTosave = userBox.getStampTreeModel();
        treeTosave.setTreeXml(treeXml);

        return treeTosave;
    }

    /**
     * �ʒu�傫����ۑ�����B
     * @throws Exception
     */
    private void preSave() {

        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        String name = (StampBoxPlugin.this).getClass().getName();

        // StampMeker mode�ŏI�������ꍇ�A
        // ����N�����ɒʏ탂�[�h�̈ʒu�Ƒ傫���ŕ\�����邽��
        if (editing) {
            prefs.putInt(name + "_x", stampBoxLoc.x);
            prefs.putInt(name + "_y", stampBoxLoc.y);
            prefs.putInt(name + "_width", stampBoxWidth);
            prefs.putInt(name + "_height", stampBoxHeight);
        }

        // �I�����̃^�u�I���C���f�b�N�X��ۑ�����
        prefs.putInt(name + "_parentBox", parentBox.getSelectedIndex());
        prefs.putInt(name + "_stampBox", userBox.getSelectedIndex());
    }

    
    /**
     * �����̃J�e�S���ɑΉ�����Tree��Ԃ��B
     * @param category Tree�̃J�e�S��
     * @return �J�e�S���Ƀ}�b�`����StampTree
     */
    public StampTree getStampTree(String entity) {
        return getCurrentBox().getStampTree(entity);
    }
    
    public StampTree getStampTreeFromUserBox(String entity) {
        return getUserStampBox().getStampTree(entity);
    }
    
    /**
     * �X�^���v�{�b�N�X�Ɋ܂܂��Stree��TreeInfo���X�g��Ԃ��B
     * @return TreeInfo�̃��X�g
     */
    public List<TreeInfo> getAllTress() {
        return getCurrentBox().getAllTreeInfos();
    }
    
    /**
     * �X�^���v�{�b�N�X�Ɋ܂܂��Stree��Ԃ��B
     * @return StampTree�̃��X�g
     */
    public List<StampTree> getAllTrees() {
        return getCurrentBox().getAllTrees();
    }
    
    /**
     * �X�^���v�{�b�N�X�Ɋ܂܂��Stree��Ԃ��B
     * @return StampTree�̃��X�g
     */
    public List<StampTree> getAllAllPTrees() {
        
        int cnt = parentBox.getTabCount();
        ArrayList<StampTree> ret = new ArrayList<StampTree>();
        
        for (int i = 0; i < cnt; i++) {
            AbstractStampBox stb = (AbstractStampBox) parentBox.getComponentAt(i);
            ret.addAll(stb.getAllPTrees());
        }
        
        return ret;
    }
    
    /**
     * Current�{�b�N�X�� P �֘AStaptree��Ԃ��B
     * @return StampTree�̃��X�g
     */
    public List<StampTree> getAllPTrees() {
        
        AbstractStampBox stb = (AbstractStampBox) getCurrentBox();
        return stb.getAllPTrees();
    }
    
    /**
     * �����̃G���e�B�e�B�z���ɂ���S�ẴX�^���v��Ԃ��B
     * ����̓��j���[���Ŏg�p����B
     * @param entity Tree�̃G���e�B�e�B
     * @return �S�ẴX�^���v�̃��X�g
     */
    public List<ModuleInfoBean> getAllStamps(String entity) {
        return getCurrentBox().getAllStamps(entity);
    }
}