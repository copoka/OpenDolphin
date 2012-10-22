/*
 * StampTree.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2001,2003,2004,2005 Digital Globe, Inc. All rights reserved.
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
import javax.swing.tree.*;
import javax.swing.event.*;

import open.dolphin.delegater.StampDelegater;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.RegisteredDiagnosisModel;
import open.dolphin.infomodel.StampModel;
import open.dolphin.infomodel.TextStampModel;
import open.dolphin.project.*;
import open.dolphin.util.GUIDGenerator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.*;

/**
 * StampTree
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class StampTree extends JTree implements TreeModelListener {
    
    public static final String SELECTED_NODE_PROP = "selectedNodeProp";
    
    private static final long serialVersionUID = -4651151848166376384L;
    private static final int TOOLTIP_LENGTH = 35;
    private static final ImageIcon ASP_ICON = ClientContext.getImageIcon("move2_16.gif");
    private static final ImageIcon LOCAL_ICON = ClientContext.getImageIcon("move2_16.gif");
    private static final String NEW_FOLDER_NAME = "�V�K�t�H���_";
    private static final String STAMP_SAVE_TASK_NAME = "�X�^���v�ۑ�";
    
    /** ASP Tree ���ǂ����̃t���O */
    private boolean asp;
    
    /** �l�pTree���ǂ����̃t���O */
    private boolean userTree;
    
    /** StampBox */
    private StampBoxPlugin stampBox;
    
    /** DB����擾���鎞��TaskTimer */
    private javax.swing.Timer taskTimer;
    
    /**
     * StampTree�I�u�W�F�N�g�𐶐�����B
     *
     * @param model TreeModel
     */
    public StampTree(TreeModel model) {
        
        super(model);
        
        this.putClientProperty("JTree.lineStyle", "Angled"); // �����y�ѐ��������g�p����
        this.setEditable(false); // �m�[�h����ҏW�s�ɂ���
        this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION); // Single Selection// �ɂ���
        
        this.setRootVisible(false);
        this.addMouseMotionListener(new MouseDragDetecter());
        
        //
        // �f�t�H���g�̃Z�������_���[��u��������
        //
        final TreeCellRenderer oldRenderer = this.getCellRenderer();
        TreeCellRenderer r = new TreeCellRenderer() {
            
            public Component getTreeCellRendererComponent(JTree tree,
                    Object value, boolean selected, boolean expanded,
                    boolean leaf, int row, boolean hasFocus) {
                
                Component c = oldRenderer.getTreeCellRendererComponent(tree,
                        value, selected, expanded, leaf, row, hasFocus);
                if (leaf && c instanceof JLabel) {
                    JLabel l = (JLabel) c;
                    Object o = ((StampTreeNode) value).getUserObject();
                    if (o instanceof ModuleInfoBean) {
                        
                        // �ŗL�̃A�C�R����ݒ肷��
                        if (isAsp()) {
                            l.setIcon(ASP_ICON);
                        } else {
                            l.setIcon(LOCAL_ICON);
                        }
                        // ToolTips ��ݒ肷��
                        l.setToolTipText(((ModuleInfoBean) o).getStampMemo());
                    }
                }
                return c;
            }
        };
        this.setCellRenderer(r);
        
        // Listens TreeModelEvent
        model.addTreeModelListener(this);
        
        // Enable ToolTips
        enableToolTips(true);
        
    }
    
    /**
     * ����StampTree��TreeInfo��Ԃ��B
     * @return Tree���
     */
    public TreeInfo getTreeInfo() {
        StampTreeNode node = (StampTreeNode) this.getModel().getRoot();
        TreeInfo info = (TreeInfo)node.getUserObject();
        return info;
    }
    
    /**
     * ����StampTree�̃G���e�B�e�B��Ԃ��B
     * @return �G���e�B�e�B
     */
    public String getEntity() {
        return getTreeInfo().getEntity();
    }
    
    /**
     * ����StampTree�̖��O��Ԃ��B
     * @return ���O
     */
    public String getTreeName() {
        return getTreeInfo().getName();
    }
    
    /**
     * UserTree���ǂ�����Ԃ��B
     * @return UserTree�̎�true
     */
    public boolean isUserTree() {
        return userTree;
    }
    
    /**
     * UserTree���ǂ�����ݒ肷��B
     * @param userTree UserTree�̎�true
     */
    public void setUserTree(boolean userTree) {
        this.userTree = userTree;
    }
    
    /**
     * ASP��Tree���ǂ�����Ԃ��B
     * @return ASP�񋟂̎� true
     */
    public boolean isAsp() {
        return asp;
    }
    
    /**
     * ASP��Tree���ǂ�����ݒ肷��B
     * @param asp ASP�񋟂̎� true
     */
    public void setAsp(boolean asp) {
        this.asp = asp;
    }
    
    /**
     * Enable or disable tooltip
     */
    public void enableToolTips(boolean state) {
        
        ToolTipManager mgr = ToolTipManager.sharedInstance();
        if (state) {
            // Enable tooltips
            mgr.registerComponent(this);
            
        } else {
            mgr.unregisterComponent(this);
        }
    }
    
    /**
     * Set StampBox reference
     */
    public void setStampBox(StampBoxPlugin stampBox) {
        this.stampBox = stampBox;
    }
    
    /**
     * �I������Ă���m�[�h��Ԃ��B
     */
    public StampTreeNode getSelectedNode() {
        return (StampTreeNode) this.getLastSelectedPathComponent();
    }
    
    /**
     * �����̃|�C���g�ʒu�̃m�[�h��Ԃ��B
     */
    public StampTreeNode getNode(Point p) {
        TreePath path = this.getPathForLocation(p.x, p.y);
        return (path != null)
        ? (StampTreeNode) path.getLastPathComponent()
        : null;
    }
    
    /**
     * ����StampTree��enter()����B
     */
    public void enter() {
    }
    
    /**
     * KartePane���� drop ���ꂽ�X�^���v���c���[�ɉ�����B
     */
    public boolean addStamp(final ModuleModel droppedStamp, final StampTreeNode selected) {
        
        boolean ret = false;
        if (droppedStamp == null) {
            return ret;
        }
        
        //
        // Drop ���ꂽ Stamp �� ModuleInfo�𓾂�
        //
        ModuleInfoBean droppedInfo = droppedStamp.getModuleInfo();
        
        //
        // �f�[�^�x�[�X�� droppedStamp �̃f�[�^���f����ۑ�����
        //
        // Entity�𐶐�����
        //
        StampModel stampModel = new StampModel();
        String stampId = GUIDGenerator.generate(stampModel);    // stampId
        stampModel.setId(stampId);
        stampModel.setUserId(Project.getUserModel().getId());   // userId
        stampModel.setEntity(droppedInfo.getEntity());          // entity
        stampModel.setStampBytes(getXMLBytes(droppedStamp.getModel())); // XML
        
        // Delegator �𐶐�����
        final StampDelegater sdl = new StampDelegater();
        
        //
        // Tree �ɉ�����V���� StampInfo �𐶐�����
        //
        final ModuleInfoBean info = new ModuleInfoBean();
        info.setStampName(droppedInfo.getStampName());      // �I���W�i����
        info.setEntity(droppedInfo.getEntity());            // Entity
        info.setStampRole(droppedInfo.getStampRole());      // Role
        info.setStampMemo(constractToolTip(droppedStamp));  // Tooltip
        info.setStampId(stampId);                           // StampID
        
        // �ۑ��^�X�N�𐶐�����
        int maxEstimation = ClientContext.getInt("task.default.maxEstimation");
        int delay = ClientContext.getInt("task.default.delay");
        int taskLength = maxEstimation/delay;
        int decideToPopup = ClientContext.getInt("task.default.decideToPopup");
        int milisToPopup = ClientContext.getInt("task.default.milisToPopup");
        String saveMsg = ClientContext.getString("task.default.saveMessage");
        final StampTask worker = new StampTask(stampModel, sdl, taskLength);
        
        // ProgressMonitor �𐶐�����
        final ProgressMonitor monitor = new ProgressMonitor(null, null, saveMsg, 0, taskLength);
        monitor.setProgress(0);
        monitor.setMillisToDecideToPopup(decideToPopup);
        monitor.setMillisToPopup(milisToPopup);
        
        // �^�X�N�^�C�}�[���N������
        taskTimer = new javax.swing.Timer(delay, new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                monitor.setProgress(worker.getCurrent());
                
                if (worker.isDone()) {
                    
                    //
                    // �I���������s��
                    //
                    taskTimer.stop();
                    monitor.close();
                    
                    // �ۑ��������������Ă��邩�ǂ������`�F�b�N����
                    if (sdl.isNoError()) {
                        //
                        // ���������� Tree �ɉ�����
                        //
                        addInfoToTree(info, selected);
                        
                    } else {
                        //
                        // �G���[���b�Z�[�W��\������
                        //
                        warning(sdl.getErrorMessage());
                    }
                    
                } else if (worker.isTimeOver()) {
                    taskTimer.stop();
                    monitor.close();
                    String title = ClientContext.getString("stamptree.title");
                    new TimeoutWarning(null, title, null);
                }
            }
        });
        worker.start();
        taskTimer.start();
        return true;
    }
    
    /**
     * StampTree �ɐV�����m�[�h��������B
     * @param info �ǉ�����m�[�h�̏��
     * @param selected �J�[�\���̉��ɂ���m�[�h(Drop �ʒu�̃m�[�h�j
     */
    public void addInfoToTree(ModuleInfoBean info, StampTreeNode selected) {
        
        //
        // StampInfo ����V���� StampTreeNode �𐶐�����
        //
        StampTreeNode node = new StampTreeNode(info);
        
        // 
        // Drop �ʒu�̃m�[�h�ɂ���Ēǉ�����ʒu�����߂�
        //
        if (selected != null && selected.isLeaf()) {
            //
            // Drop�ʒu�̃m�[�h���t�̏ꍇ�A���̑O�ɑ}������
            //
            StampTreeNode newParent = (StampTreeNode) selected.getParent();
            int index = newParent.getIndex(selected);
            DefaultTreeModel model = (DefaultTreeModel) this.getModel();
            model.insertNodeInto(node, newParent, index);
            //
            // �ǉ������m�[�h��I������
            //
            TreeNode[] path = model.getPathToRoot(node);
            ((JTree)this).setSelectionPath(new TreePath(path));
            
        } else if (selected != null && (!selected.isLeaf())) {
            //
            // Drop�ʒu�̃m�[�h���q�������A�Ō�̎q�Ƃ��đ}������
            //
            DefaultTreeModel model = (DefaultTreeModel) this.getModel();
            model.insertNodeInto(node, selected, selected.getChildCount());
            //
            // �ǉ������m�[�h��I������
            //
            TreeNode[] path = model.getPathToRoot(node);
            ((JTree)this).setSelectionPath(new TreePath(path));
            
        } else {
            //  
            // Drop �ʒu�̃m�[�h�� null �ŃR�[�������P�[�X������
            // 1. ����tree�̃X�^���v�ł͂Ȃ��ꍇ�A�Y������Tree�̃��[�g�ɉ�����
            // 2. �p�X Tree �ȂǁA�܂��m�[�h�������Ȃ�������Ԃ̎�
            //
            // Stamp �{�b�N�X���� entity �ɑΉ����� tree �𓾂�
            StampTree another = stampBox.getStampTree(info.getEntity());
            boolean myTree = (another == this) ? true : false;
            final String treeName = another.getTreeName();
            DefaultTreeModel model = (DefaultTreeModel) another.getModel();
            StampTreeNode root = (StampTreeNode) model.getRoot();
            root.add(node);
            model.reload(root);
            //
            // �ǉ������m�[�h��I������
            //
            TreeNode[] path = model.getPathToRoot(node);
            ((JTree)this).setSelectionPath(new TreePath(path));
            
            // ���b�Z�[�W��\������
            if (!myTree) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        StringBuilder buf = new StringBuilder();
                        buf.append("�X�^���v�͌l�p�� ");
                        buf.append(treeName);
                        buf.append(" �ɕۑ����܂����B");
                        JOptionPane.showMessageDialog(
                                StampTree.this,
                                buf.toString(),
                                ClientContext.getFrameTitle(STAMP_SAVE_TASK_NAME),
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }
        }
    }
    
    /**
     * Diagnosis Table ���� Drag & Drop ���ꂽRegisteredDiagnosis���X�^���v������B
     */
    public boolean addDiagnosis(RegisteredDiagnosisModel rd, final StampTreeNode selected) {
        
        if (rd == null) {
            return false;
        }
        
        // �N���A
        rd.setId(0L);
        rd.setKarte(null);
        rd.setCreator(null);
        rd.setDiagnosisCategoryModel(null);
        rd.setDiagnosisOutcomeModel(null);
        rd.setFirstEncounterDate(null);
        rd.setStartDate(null);
        rd.setEndDate(null);
        rd.setRelatedHealthInsurance(null);
        rd.setFirstConfirmDate(null);
        rd.setConfirmDate(null);
        rd.setStatus(null);
        rd.setPatientLiteModel(null);
        rd.setUserLiteModel(null);
        
        RegisteredDiagnosisModel add = new RegisteredDiagnosisModel();
        add.setDiagnosis(rd.getDiagnosis());
        add.setDiagnosisCode(rd.getDiagnosisCode());
        add.setDiagnosisCodeSystem(rd.getDiagnosisCodeSystem());
        
        ModuleModel stamp = new ModuleModel();
        stamp.setModel(add);
        
        // �f�[�^�x�[�X�� Stamp �̃f�[�^���f�����i��������
        StampModel addStamp = new StampModel();
        String stampId = GUIDGenerator.generate(addStamp);
        addStamp.setId(stampId);
        addStamp.setUserId(Project.getUserModel().getId());
        addStamp.setEntity(IInfoModel.ENTITY_DIAGNOSIS);
        addStamp.setStampBytes(getXMLBytes(stamp.getModel()));
        final StampDelegater sdl = new StampDelegater();
        
        // Tree �ɉ����� �V���� StampInfo �𐶐�����
        final ModuleInfoBean info = new ModuleInfoBean();
        info.setStampId(stampId);                       // Stamp ID
        info.setStampName(add.getDiagnosis());          // ���a��
        info.setEntity(IInfoModel.ENTITY_DIAGNOSIS);    // �J�e�S��
        info.setStampRole(IInfoModel.ENTITY_DIAGNOSIS); // Role
        
        StringBuilder buf = new StringBuilder();
        buf.append(add.getDiagnosis());
        String cd = add.getDiagnosisCode();
        if (cd != null) {
            buf.append("(");
            buf.append(cd);
            buf.append(")"); // Tooltip
        }
        info.setStampMemo(buf.toString());
        
        // �ۑ��^�X�N�𐶐������s����
        int maxEstimation = ClientContext.getInt("task.default.maxEstimation");
        int delay = ClientContext.getInt("task.default.delay");
        int taskLength = maxEstimation/delay;
        int decideToPopup = ClientContext.getInt("task.default.decideToPopup");
        int milisToPopup = ClientContext.getInt("task.default.milisToPopup");
        String saveMsg = ClientContext.getString("task.default.saveMessage");
        final StampTask worker = new StampTask(addStamp, sdl, taskLength);
        
        final ProgressMonitor monitor = new ProgressMonitor(null, null, saveMsg, 0, taskLength);
        monitor.setProgress(0);
        monitor.setMillisToDecideToPopup(decideToPopup);
        monitor.setMillisToPopup(milisToPopup);
        
        taskTimer = new javax.swing.Timer(delay, new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                monitor.setProgress(worker.getCurrent());
                
                if (worker.isDone()) {
                    taskTimer.stop();
                    monitor.close();
                    
                    if (sdl.isNoError()) {
                        // ���������� Tree �ɉ�����
                        addInfoToTree(info, selected);
                    } else {
                        // �G���[���b�Z�[�W��\������
                        warning(sdl.getErrorMessage());
                    }
                    
                } else if (worker.isTimeOver()) {
                    taskTimer.stop();
                    monitor.close();
                    String title = ClientContext.getString("stamptree.title");
                    new TimeoutWarning(null, title, null);
                }
            }
        });
        worker.start();
        taskTimer.start();
        
        return true;
    }
    
    /**
     * �G�f�B�^�Ő��������a�����X�g��o�^����B
     */
    public void addDiagnosis(ArrayList<RegisteredDiagnosisModel> list) {
        
        if (list == null || list.size() == 0) {
            return;
        }
        
        final ArrayList<StampModel> stampList = new ArrayList<StampModel>();
        final ArrayList<ModuleInfoBean> infoList = new ArrayList<ModuleInfoBean>();
        
        for (RegisteredDiagnosisModel rd : list) {
            // �N���A
            rd.setId(0L);
            rd.setKarte(null);
            rd.setCreator(null);
            rd.setDiagnosisCategoryModel(null);
            rd.setDiagnosisOutcomeModel(null);
            rd.setFirstEncounterDate(null);
            rd.setStartDate(null);
            rd.setEndDate(null);
            rd.setRelatedHealthInsurance(null);
            rd.setFirstConfirmDate(null);
            rd.setConfirmDate(null);
            rd.setStatus(null);
            rd.setPatientLiteModel(null);
            rd.setUserLiteModel(null);
            RegisteredDiagnosisModel add = new RegisteredDiagnosisModel();
            add.setDiagnosis(rd.getDiagnosis());
            add.setDiagnosisCode(rd.getDiagnosisCode());
            add.setDiagnosisCodeSystem(rd.getDiagnosisCodeSystem());
            
            ModuleModel stamp = new ModuleModel();
            stamp.setModel(add);
            
            // �f�[�^�x�[�X�� Stamp �̃f�[�^���f�����i��������
            StampModel addStamp = new StampModel();
            String stampId = GUIDGenerator.generate(addStamp);
            addStamp.setId(stampId);
            addStamp.setUserId(Project.getUserModel().getId());
            addStamp.setEntity(IInfoModel.ENTITY_DIAGNOSIS);
            addStamp.setStampBytes(getXMLBytes(stamp.getModel()));
            stampList.add(addStamp);
            
            // Tree �ɉ����� �V���� StampInfo �𐶐�����
            ModuleInfoBean info = new ModuleInfoBean();
            info.setStampId(stampId);                       // Stamp ID
            info.setStampName(add.getDiagnosis());          // ���a��
            info.setEntity(IInfoModel.ENTITY_DIAGNOSIS);    // �J�e�S��
            info.setStampRole(IInfoModel.ENTITY_DIAGNOSIS); // Role
            
            StringBuilder buf = new StringBuilder();
            buf.append(add.getDiagnosis());
            String cd = add.getDiagnosisCode();
            if (cd != null) {
                buf.append("(");
                buf.append(cd);
                buf.append(")"); // Tooltip
            }
            info.setStampMemo(buf.toString());
            infoList.add(info);
        }
        
        final StampDelegater sdl = new StampDelegater();
        
        // �ۑ��^�X�N�𐶐������s����
        int maxEstimation = ClientContext.getInt("task.default.maxEstimation");
        int delay = ClientContext.getInt("task.default.delay");
        int taskLength = maxEstimation/delay;
        int decideToPopup = ClientContext.getInt("task.default.decideToPopup");
        int milisToPopup = ClientContext.getInt("task.default.milisToPopup");
        String saveMsg = ClientContext.getString("task.default.saveMessage");
        final StampTask worker = new StampTask(stampList, sdl, taskLength);
        
        final ProgressMonitor monitor = new ProgressMonitor(null, null, saveMsg, 0, taskLength);
        monitor.setProgress(0);
        monitor.setMillisToDecideToPopup(decideToPopup);
        monitor.setMillisToPopup(milisToPopup);
        
        taskTimer = new javax.swing.Timer(delay, new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                monitor.setProgress(worker.getCurrent());
                
                if (worker.isDone()) {
                    taskTimer.stop();
                    monitor.close();
                    
                    if (sdl.isNoError()) {
                        // ���������� Tree �ɉ�����
                        for(ModuleInfoBean info : infoList) {
                            addInfoToTree(info, null);
                        }
                    } else {
                        // �G���[���b�Z�[�W��\������
                        warning(sdl.getErrorMessage());
                    }
                    
                } else if (worker.isTimeOver()) {
                    taskTimer.stop();
                    monitor.close();
                    String title = ClientContext.getString("stamptree.title");
                    new TimeoutWarning(null, title, null);
                }
            }
        });
        worker.start();
        taskTimer.start();
    }
    
    /**
     * �e�L�X�g�X�^���v��ǉ�����B
     */
    public boolean addTextStamp(String text, final StampTreeNode selected) {
        
        if ( (text == null) || (text.length() == 0) || text.equals("") )  {
            return false;
        }
        
        TextStampModel stamp = new TextStampModel();
        stamp.setText(text);
        
        //
        // �f�[�^�x�[�X�� Stamp �̃f�[�^���f�����i��������
        //
        StampModel addStamp = new StampModel();
        String stampId = GUIDGenerator.generate(addStamp);
        addStamp.setId(stampId);
        addStamp.setUserId(Project.getUserModel().getId());
        addStamp.setEntity(IInfoModel.ENTITY_TEXT);
        addStamp.setStampBytes(getXMLBytes((IInfoModel) stamp));
        
        final StampDelegater sdl = new StampDelegater();
        
        //
        // Tree �։����� �V���� StampInfo �𐶐�����
        //
        final ModuleInfoBean info = new ModuleInfoBean();
        int len = text.length() > 16 ? 16 : text.length();
        String name = text.substring(0, len);
        len = name.indexOf("\n");
        if (len > 0) {
            name = name.substring(0, len);
        }
        info.setStampName(name);                    //
        info.setEntity(IInfoModel.ENTITY_TEXT);     // �J�e�S��
        info.setStampRole(IInfoModel.ENTITY_TEXT);  // Role
        info.setStampMemo(text);                    // Tooltip
        info.setStampId(stampId);                   // Stamp ID
        
        // �ۑ��^�X�N�𐶐������s����
        int maxEstimation = ClientContext.getInt("task.default.maxEstimation");
        int delay = ClientContext.getInt("task.default.delay");
        int taskLength = maxEstimation/delay;
        int decideToPopup = ClientContext.getInt("task.default.decideToPopup");
        int milisToPopup = ClientContext.getInt("task.default.milisToPopup");
        String saveMsg = ClientContext.getString("task.default.saveMessage");
        
        final StampTask worker = new StampTask(addStamp, sdl, taskLength);
        
        final ProgressMonitor monitor = new ProgressMonitor(null, null, saveMsg, 0, taskLength);
        monitor.setProgress(0);
        monitor.setMillisToDecideToPopup(decideToPopup);
        monitor.setMillisToPopup(milisToPopup);
        
        taskTimer = new javax.swing.Timer(delay, new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                monitor.setProgress(worker.getCurrent());
                
                if (worker.isDone()) {
                    
                    taskTimer.stop();
                    monitor.close();
                    
                    if (sdl.isNoError()) {
                        // 
                        // ���������� Tree �ɉ�����
                        //
                        addInfoToTree(info, selected);
                        
                    } else {
                        //
                        // �G���[���b�Z�[�W��\������
                        //
                        warning(sdl.getErrorMessage());
                    }
                    
                } else if (worker.isTimeOver()) {
                    taskTimer.stop();
                    monitor.close();
                    String title = ClientContext.getString("stamptree.title");
                    new TimeoutWarning(null, title, null);
                }
            }
        });
        worker.start();
        taskTimer.start();
        
        return true;
    }
    
    /**
     * �X�^���v�̏���\�����邽�߂̕�����𐶐�����B
     * @param stamp ���𐶐�����X�^���v
     * @return �X�^���v�̏�񕶎���
     */
    protected String constractToolTip(ModuleModel stamp) {
        
        String ret = null;
        
        try {
            StringBuilder buf = new StringBuilder();
            BufferedReader reader = new BufferedReader(new StringReader(stamp.getModel().toString()));
            
            String line = null;
            while ( (line = reader.readLine()) != null ) {
                
                buf.append(line);
                
                if (buf.length() < TOOLTIP_LENGTH) {
                    buf.append(",");
                } else {
                    break;
                }
            }
            reader.close();
            if (buf.length() > TOOLTIP_LENGTH) {
                buf.setLength(TOOLTIP_LENGTH);
            }
            buf.append("...");
            ret = buf.toString();
            
        } catch (IOException e) {
            e.toString();
        }
        
        return ret;
    }
    
    /**
     * �X�^���v�^�X�N���ʂ� warning �_�C�A���O��\������B
     * @param title  �_�C�A���O�E�C���h�E�ɕ\������^�C�g��
     * @param message�@�G���[���b�Z�[�W
     */
    private void warning(String message) {
        String title = ClientContext.getString("stamptree.title");
        JOptionPane.showMessageDialog(
                StampTree.this,
                message,
                ClientContext.getFrameTitle(title),
                JOptionPane.WARNING_MESSAGE);
    }
    
    // //////////// PopupMenu �T�|�[�g //////////////
    
    /**
     * �m�[�h�̖��O��ύX����B
     */
    public void renameNode() {
        
        if (!isUserTree()) {
            return;
        }
        
        // Root �ւ̃p�X���擾����
        StampTreeNode node = getSelectedNode();
        if (node == null) {
            return;
        }
        TreeNode[] nodes = node.getPath();
        TreePath path = new TreePath(nodes);
        
        // �ҏW���J�n����
        this.setEditable(true);
        this.startEditingAtPath(path);
        // this.setEditable (false); �� TreeModelListener �ōs��
    }
    
//    public void cut() {
//        System.out.println("called cut");
//        if (!isUserTree()) {
//            return;
//        }
//        
//        StampTreeNode theNode = getSelectedNode();
//        if (theNode != null && theNode.isLeaf()) {
//            Action a = this.getTransferHandler().getCutAction();
//            if (a != null) {
//                a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
//            }
//        }
//    }
//    
//    public void copy() {
//        System.out.println("called copy");
//        if (!isUserTree()) {
//            return;
//        }
//        
//        StampTreeNode theNode = getSelectedNode();
//        if (theNode != null) {
//            Action a = this.getTransferHandler().getCopyAction();
//            if (a != null) {
//                a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
//            }
//        }
//    }
//    
//    public void paste() {
//        System.out.println("called paste");
//        if (!isUserTree()) {
//            return;
//        }
//        Action a = this.getTransferHandler().getPasteAction();
//        if (a != null) {
//            a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
//        }
//    }
    
    /**
     * �m�[�h���폜����B
     */
    public void deleteNode() {
        
        //System.out.println("Called delete");
        //Toolkit.getDefaultToolkit().beep();
        
        if (!isUserTree()) {
            return;
        }
        
        //
        // �폜����m�[�h���擾����
        // �E�N���b�N�őI������Ă���
        //
        final StampTreeNode theNode = getSelectedNode();
        if (theNode == null) {
            return;
        }
        
        //
        // ���̃m�[�h�����[�g�ɂ���T�u�c���[��O����������񋓂𐶐����ĕԂ��܂��B
        // �񋓂� nextElement() ���\�b�h�ɂ���ĕԂ����ŏ��̃m�[�h�́A���̍폜����m�[�h�ł��B
        //
        Enumeration e = theNode.preorderEnumeration();
        
        //
        // ���̃��X�g�̂Ȃ��ɍ폜����m�[�h�Ƃ��̎q���܂߂�
        //
        ArrayList<String> deleteList = new ArrayList<String>();
        
        // �G�f�B�^���甭�s�����邩�ǂ����̃t���O
        boolean hasEditor = false;
        
        // �񋓂���
        while (e.hasMoreElements()) {
            //System.out.println("e.hasMore");
            StampTreeNode node = (StampTreeNode) e.nextElement();
            if (node.isLeaf()) {
                ModuleInfoBean info = (ModuleInfoBean) node.getUserObject();
                String stampId = info.getStampId();
                //
                // �G�f�B�^���甭�s������ꍇ�͒��~����
                //
                if (info.getStampName().equals("�G�f�B�^���甭�s...") && (! info.isSerialized()) ) {
                    hasEditor = true;
                    break;
                }
                
                //
                // ID���t���Ă�����݂̂̂�������
                //
                if (stampId != null) {
                    deleteList.add(stampId);
                }
            }
        }
        
        //
        // �G�f�B�^���甭�s���L�����ꍇ�̓_�C�A���O��\����
        // ���^�[������
        //
        if (hasEditor) {
            String msg0 = "�G�f�B�^���甭�s�͏����ł��܂���B�t�H���_�Ɋ܂܂�Ă���";
            String msg1 = "�ꍇ�� Drag & Drop �ňړ���A�ēx���s���Ă��������B";
            String taskTitle = ClientContext.getString("stamptree.title");
            JOptionPane.showMessageDialog(
                        (Component) null,
                        new Object[]{msg0, msg1},
                        ClientContext.getFrameTitle(taskTitle),
                        JOptionPane.INFORMATION_MESSAGE
                        );
            return;
        }
        
        //
        // �폜����t�H���_����̏ꍇ�͍폜���ă��^�[������
        // ���X�g�̃T�C�Y���[������ theNode ���t�łȂ���
        // 
        if (deleteList.size() == 0 && (!theNode.isLeaf()) ) {
            //System.out.println("Empty Folder");
            DefaultTreeModel model = (DefaultTreeModel)(StampTree.this).getModel();
            model.removeNodeFromParent(theNode);
            return;
        }
        
        // �f�[�^�x�[�X�̃X�^���v���폜����f���Q�[�^�𐶐�����
        final StampDelegater sdl = new StampDelegater();
        
        // �폜�^�X�N�𐶐�����
        int maxEstimation = ClientContext.getInt("task.default.maxEstimation");
        int delay = ClientContext.getInt("task.default.delay");
        int taskLength = maxEstimation/delay;
        int decideToPopup = ClientContext.getInt("task.default.decideToPopup");
        int milisToPopup = ClientContext.getInt("task.default.milisToPopup");
        String deleteMessage = ClientContext.getString("task.default.deleteMessage");
        final StampTask worker = new StampTask(deleteList, sdl, taskLength);
        
        // Progress Monitor �𐶐�����
        final ProgressMonitor monitor = new ProgressMonitor(null, null, deleteMessage, 0, taskLength);
        monitor.setProgress(0);
        monitor.setMillisToDecideToPopup(decideToPopup);
        monitor.setMillisToPopup(milisToPopup);
        
        // �^�X�N�^�C�}�[�𐶐�����
        taskTimer = new javax.swing.Timer(delay, new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                // ���j�^��i�߂�
                monitor.setProgress(worker.getCurrent());
                
                // �^�X�N���I�����Ă��邩�ǂ������`�F�b�N����
                if (worker.isDone()) {
                    
                    //
                    // �I���������s��
                    //
                    taskTimer.stop();
                    monitor.close();
                    
                    // 
                    // �f�[�^�x�[�X�̍폜���������Ă��邩�ǂ������`�F�b�N����
                    //
                    if (sdl.isNoError()) {
                        //
                        // �������Ă���ꍇ�� Tree ����m�[�h���폜����
                        // TODO �G�f�B�^���甭�s���폜�����
                        //
                        DefaultTreeModel model = (DefaultTreeModel)(StampTree.this).getModel();
                        model.removeNodeFromParent(theNode);
                        
                    } else {
                        //
                        // �G���[���b�Z�[�W��\������
                        // warning(sdl.getErrorMessage());
                        //
                        // �����폜������o�[�W�����܂Ŏ��s����
                        // TODO �G�f�B�^���甭�s���폜�����
                        //
                        DefaultTreeModel model = (DefaultTreeModel)(StampTree.this).getModel();
                        model.removeNodeFromParent(theNode); 
                    }
                    
                } else if (worker.isTimeOver()) {
                    taskTimer.stop();
                    monitor.close();
                    String title = ClientContext.getString("stamptree.title");
                    new TimeoutWarning(null, title, null);
                }
            }
        });
        worker.start();
        taskTimer.start();
    }
    
    /**
     * �V�K�̃t�H���_��ǉ�����
     */
    public void createNewFolder() {
        
        if (!isUserTree()) {
            return;
        }
        
        // �t�H���_�m�[�h�𐶐�����
        StampTreeNode folder = new StampTreeNode(NEW_FOLDER_NAME);
        
        //
        // �����ʒu�ƂȂ�I�����ꂽ�m�[�h�𓾂�
        //
        StampTreeNode selected = getSelectedNode();
        
        if (selected != null && selected.isLeaf()) {
            //
            // �I���ʒu�̃m�[�h���t�̏ꍇ�A���̑O�ɑ}������
            //
            StampTreeNode newParent = (StampTreeNode) selected.getParent();
            int index = newParent.getIndex(selected);
            DefaultTreeModel model = (DefaultTreeModel) this.getModel();
            model.insertNodeInto(folder, newParent, index);
            
        } else if (selected != null && (!selected.isLeaf())) {
            //
            // �I���ʒu�̃m�[�h���q�������A�Ō�̎q�Ƃ��đ}������
            //
            DefaultTreeModel model = (DefaultTreeModel) this.getModel();
            model.insertNodeInto(folder, selected, selected.getChildCount());
        }
        
        //TreePath parentPath = new TreePath(parent.getPath());
        //this.expandPath(parentPath);
    }
    
    // /////////// TreeModelListener ////////////////
    
    public void treeNodesChanged(TreeModelEvent e) {
        this.setEditable(false);
    }
    
    public void treeNodesInserted(TreeModelEvent e) {
    }
    
    public void treeNodesRemoved(TreeModelEvent e) {
    }
    
    public void treeStructureChanged(TreeModelEvent e) {
    }
    
    private byte[] getXMLBytes(Object bean) {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        XMLEncoder e = new XMLEncoder(new BufferedOutputStream(bo));
        e.writeObject(bean);
        e.close();
        return bo.toByteArray();
    }
}