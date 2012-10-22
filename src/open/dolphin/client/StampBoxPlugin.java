/*
 * StampBoxService.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2001-2007 Digital Globe, Inc. All rights reserved.
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import open.dolphin.delegater.StampDelegater;
import open.dolphin.infomodel.FacilityModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.IStampTreeModel;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.PublishedTreeModel;
import open.dolphin.infomodel.RegisteredDiagnosisModel;
import open.dolphin.infomodel.StampTreeModel;
import open.dolphin.order.EditorSetPanel;
import open.dolphin.plugin.ILongTask;
import open.dolphin.plugin.helper.ComponentMemory;
import open.dolphin.project.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import java.io.*;
import org.apache.log4j.Logger;

/**
 * StampBoxPlugin
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class StampBoxPlugin extends DefaultMainWindowPlugin {
    
    // frame�̃f�t�H���g�̑傫���y�у^�C�g��
    private final int DEFAULT_WIDTH     = 320;
    private final int DEFAULT_HEIGHT    = 690;
    private final int IMPORT_TREE_OFFSET = 1;
    
    /** StampBox �� JFrame */
    private JFrame frame;
    
    /** StampBox */
    private JTabbedPane parentBox;
    
    /** ���[�U�l�p�� StampBox*/
    private AbstractStampBox userBox;
    
    /** ���ݑI������Ă��� StampBox */
    private AbstractStampBox curBox;
    
    /** �C���|�[�g���Ă��� StampTree �̃��X�g */
    private List<Long> importedTreeList;
    
    /** ���ݑI������Ă��� StampBox �̏���\�����郉�x�� */
    private JLabel curBoxInfo;
    
    /** Container Panel*/
    private JPanel content;
    
    /** Stampmaker �{�^�� */
    private JToggleButton toolBtn;
    
    /** ���J�{�^�� */
    private JButton publishBtn;
    
    /** �C���|�[�g�{�^�� */
    private JButton importBtn;
    
    /** StampMaker �̃G�f�B�^�Z�b�g */
    private EditorSetPanel editors;
    
    /** Editor�̕ҏW�l���X�i */
    private EditorValueListener editorValueListener;
    
    /** StampMaker ���[�h�̃t���O */
    private boolean editing;
    
    /** StampBox �ʒu */
    private Point stampBoxLoc;
    
    /** StampBox �� */
    private int stampBoxWidth;
    
    /** StampBox ���� */
    private int stampBoxHeight;
    
    /** Block Glass Pane */
    private BlockGlass glass;
    
    /** Container Panel */
    private JPanel stampBoxPanel;
    
    /** ���̃X�^���v�{�b�N�X�� StmpTreeModel */
    private List<IStampTreeModel> stampTreeModels;
    
    
    /** 
     * Creates new StampBoxPlugin 
     */
    public StampBoxPlugin() {
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
     * User�p�A���L�p�A�T�[�h�p�[�e�B����StampTree���f�[�^�x�[�X����ǂݍ��ށB
     * �o�b�N�O�����h�X���b�h�Ŏ��s����邱�Ƃ�z�肵�Ă���B
     * @return �ǂݍ���StampTreeModel �̃��X�g
     */
    public void loadStampTrees() {
        
        Logger logger = ClientContext.getLogger("boot");
        
        try {
            // UserPk���擾����
            long userPk = Project.getUserModel().getId();
            
            // �f�[�^�x�[�X�������s��
            StampDelegater stampDel = new StampDelegater();
            List<IStampTreeModel> treeList = stampDel.getTrees(userPk);
            if (!stampDel.isNoError()) {
                logger.fatal("Fata error reading stampTrees from the server.");
                throw new RuntimeException();
            }
            
            // User�p��StampTree�����݂��Ȃ��V�K���[�U�̏ꍇ�A����Tree�𐶐�����
            boolean hasTree = false;
            if (treeList != null || treeList.size() > 0) {
                for (IStampTreeModel tree : treeList) {
                    if (tree != null) {
                        long id = tree.getUser().getId();
                        if (id == userPk && tree instanceof StampTreeModel) {
                            hasTree = true;
                            break;
                        }
                    }
                }
            }
            
            // �V�K���[�U�Ńf�[�^�x�[�X�Ɍl�p��StampTree�����݂��Ȃ������ꍇ
            if (!hasTree) {
                // ���\�[�XTree��ǂݍ���
                InputStream in = ClientContext.getResourceAsStream("stamptree-seed.xml");
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "SHIFT_JIS"));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while( (line = reader.readLine()) != null ) {
                    sb.append(line);
                }
                // Tree����ݒ肵�ۑ�����
                IStampTreeModel tm = new StampTreeModel();
                tm.setUser(Project.getUserModel());
                tm.setName(ClientContext.getString("stampTree.personal.box.name"));
                tm.setDescription(ClientContext.getString("stampTree.personal.box.tooltip"));
                FacilityModel facility = Project.getUserModel().getFacilityModel();
                tm.setPartyName(facility.getFacilityName());
                String url = facility.getUrl();
                if (url != null) {
                    tm.setUrl(url);
                }
                tm.setTreeXml(sb.toString());
                in.close();
                reader.close();
                // ���X�g�̐擪�֒ǉ�����
                treeList.add(0, tm);
            }
            
            setStampTreeModels(treeList);
            
        } catch (Exception e) {
            e.printStackTrace();
            logger.fatal("Fata error at loadStampTrees:" + e.getMessage());
            throw new RuntimeException();
        }
    }
    
    /**
     * �v���O�������J�n����B
     */
    public void start() {
        
        Logger logger = ClientContext.getLogger("boot");
        
        if (stampTreeModels == null) {
            logger.fatal("StampTreeModel is null");
            throw new RuntimeException("Fatal error: StampTreeModel is null at start.");
        }
        
        //
        // StampBox��JFrame�𐶐�����
        //
        String title = ClientContext.getFrameTitle(getTitle());
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screen.width - DEFAULT_WIDTH;
        int y = (screen.height - DEFAULT_HEIGHT) / 2;
        int width = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;
        frame = new JFrame(title);
        glass = new BlockGlass();
        frame.setGlassPane(glass);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
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
        
        super.start();
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
        // ���݂̈ʒu�Ƒ傫����ۑ�����
        stampBoxLoc = frame.getLocation();
        stampBoxWidth = frame.getWidth();
        stampBoxHeight = frame.getHeight();
        
        //
        // ASP �{�b�N�X��I��s�ɂ���
        //
        enabledImportBox(false);
        
        // ���݂̃^�u����tree��Entity�𓾂�
        int index = userBox.getSelectedIndex();
        StampTree tree = userBox.getStampTree(index);
        String entity = tree.getEntity();
        
        // �G�f�B�^�𐶐�����
        // text �^�u��I��s�ɂ���
        userBox.setHasNoEditorEnabled(false);
        List<StampTree> allTrees = userBox.getAllTrees();
        editors = new EditorSetPanel();
        for (StampTree st : allTrees) {
            st.addTreeSelectionListener(editors);
        }
        editorValueListener = new EditorValueListener();
        editors.addPropertyChangeListener(IStampEditorDialog.EDITOR_VALUE_PROP, editorValueListener);
        editors.show(entity);
        
        content.removeAll();
        content.add(editors, BorderLayout.CENTER);
        content.add(stampBoxPanel, BorderLayout.EAST);
        //content.setLayout(new FlowLayout());
        //content.add(editors);
        //content.add(stampBoxPanel);
        stampBoxPanel.setPreferredSize(new Dimension(300, 690));
        editors.setPreferredSize(new Dimension(724, 690));
        content.setPreferredSize(new Dimension(1000, 690));
        content.revalidate();
        frame.setVisible(false);
        
        // �O��I�����̈ʒu�ƃT�C�Y���擾����
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        String name = this.getClass().getName();
        int locX = prefs.getInt(name + ".stampmMaker.x", 0);
        int locY = prefs.getInt(name + ".stampmMaker.y", 0);
        int width = prefs.getInt(name + ".stampmMaker.width", 0);
        int height = prefs.getInt(name + ".stampmMaker.height", 0);
        //width = 0;
        //height = 0;
        
        if (width == 0 || height == 0) {
            // �Z���^�����O����
            frame.pack();
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (screen.width - frame.getWidth())/2;
            int y = (screen.height - frame.getHeight())/2;
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
    
    /**
     * �X�^���v���[�J���I������B
     */
    public void stopStampMake() {
        
        if (! editing) {
            return;
        }
        
        // ���݂̑傫���ƈʒu��Preference�ɕۑ�����
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        String name = this.getClass().getName();
        prefs.putInt(name + ".stampmMaker.x", frame.getLocation().x);
        prefs.putInt(name + ".stampmMaker.y", frame.getLocation().y);
        prefs.putInt(name + ".stampmMaker.width", frame.getWidth());
        prefs.putInt(name + ".stampmMaker.height", frame.getHeight());
        
        editors.close();
        editors.removePropertyChangeListener(IStampEditorDialog.EDITOR_VALUE_PROP, editorValueListener);
        List<StampTree> allTrees = userBox.getAllTrees();
        for (StampTree st : allTrees) {
            //st.removePropertyChangeListener(StampTree.SELECTED_NODE_PROP, editors);
            st.removeTreeSelectionListener(editors);
        }
        
        content.removeAll();
        //content.setLayout(new BorderLayout());
        content.add(stampBoxPanel, BorderLayout.CENTER);
        //content.remove(editors);
        
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
        
        @SuppressWarnings("unchecked")
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
    public void stop() {
        frame.setVisible(false);
        frame.dispose();
        super.stop();
    }
     
    /**
     * �t���[����O�ʂɏo���B
     */
    public void toFront() {
        if (frame != null) {
            frame.toFront();
        }
    }
    
    /**
     * stop() ���ɕԂ� ILongTask �� Stamptree ��ۑ����邽�߂̃^�X�N�B
     * �R���e�i�����̃v���O�C���̃^�X�N���܂Ƃ߁A��̃^�C�}�[�Ŏ��s����B
     */
    public ILongTask getStoppingTask() {
        
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        String name = this.getClass().getName();
        
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
        
        //
        // User Tree �݂̂�ۑ�����
        //
        ArrayList<StampTree> list = (ArrayList<StampTree>) userBox.getAllTrees();
        if (list == null || list.size() == 0) {
            return null;
        }
        
        //
        // ORCA �Z�b�g�͏���
        //
        for (StampTree tree : list) {
            if (tree.getTreeInfo().getEntity().equals(IInfoModel.ENTITY_ORCA)) {
                list.remove(tree);
                System.out.println("ORCA�Z�b�g�������܂���");
                break;
            }
        }
        
        // StampTree ��\�� XML �f�[�^�𐶐�����
        DefaultStampTreeXmlBuilder builder = new DefaultStampTreeXmlBuilder();
        StampTreeXmlDirector director = new StampTreeXmlDirector(builder);
        String treeXml = director.build(list);
        
        // �l�p��StampTreeModel��XML���Z�b�g����
        IStampTreeModel treeM = userBox.getStampTreeModel();
        treeM.setTreeXml(treeXml);
        
        // �ۑ��^�X�N�p�� delegater �𐶐�����
        StampDelegater stampDel = new StampDelegater();
        
        // StampTree �ۑ��p�̃^�X�N�𐶐����ĕԂ�
        return new SaveStampTreeTask(treeM, stampDel);
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