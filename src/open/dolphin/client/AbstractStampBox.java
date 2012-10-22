/*
 * AbstractStampBox.java
 * Copyright (C) 2006 Digital Globe, Inc. All rights reserved.
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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JTabbedPane;
import javax.swing.tree.DefaultMutableTreeNode;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.IStampTreeModel;
import open.dolphin.infomodel.ModuleInfoBean;

/**
 * AbstractStampBox
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public abstract class AbstractStampBox extends JTabbedPane implements IStampBox {
    
    protected IStampTreeModel stampTreeModel;
    protected StampBoxPlugin context;
    
    /** Creates new StampBoxPlugin */
    public AbstractStampBox() {
    }
    
    public StampBoxPlugin getContext() {
        return context;
    }
    
    public void setContext(StampBoxPlugin plugin) {
        context = plugin;
    }
    
    public IStampTreeModel getStampTreeModel() {
        return stampTreeModel;
    }
    
    public void setStampTreeModel(IStampTreeModel stampTreeModel) {
        this.stampTreeModel = stampTreeModel;
    }
    
    protected abstract void buildStampBox();
    
    /**
     * �����̃J�e�S���ɑΉ�����Tree��Ԃ��B
     * @param category Tree�̃J�e�S��
     * @return �J�e�S���Ƀ}�b�`����StampTree
     */
    public StampTree getStampTree(String entity) {
        int count = this.getTabCount();
        boolean found = false;
        StampTree tree = null;
        for (int i = 0; i < count; i++) {
            StampTreePanel panel = (StampTreePanel) this.getComponentAt(i);
            tree = panel.getTree();
            if (entity.equals(tree.getEntity())) {
                found = true;
                break;
            }
        }
        
        return found ? tree : null;
    }
    
    public StampTree getStampTree(int index) {
        StampTreePanel panel = (StampTreePanel) this.getComponentAt(index);
        return panel.getTree();
    }
    
    public boolean isHasEditor(int index) {
        return false;
    }
    
    public void setHasNoEditorEnabled(boolean b) {
    }
    
    
    /**
     * �X�^���v�{�b�N�X�Ɋ܂܂��Stree��TreeInfo���X�g��Ԃ��B
     * @return TreeInfo�̃��X�g
     */
    public List<TreeInfo> getAllTreeInfos() {
        List<TreeInfo> ret = new ArrayList<TreeInfo>();
        int cnt = this.getTabCount();
        for (int i = 0; i < cnt; i++) {
            StampTreePanel tp = (StampTreePanel) this.getComponent(i);
            StampTree tree = tp.getTree();
            TreeInfo info = tree.getTreeInfo();
            ret.add(info);
        }
        return ret;
    }
    
    /**
     * �X�^���v�{�b�N�X�Ɋ܂܂��Stree��Ԃ��B
     * @return StampTree�̃��X�g
     */
    public List<StampTree> getAllTrees() {
        List<StampTree> ret = new ArrayList<StampTree>();
        int cnt = this.getTabCount();
        for (int i = 0; i < cnt; i++) {
            StampTreePanel tp = (StampTreePanel) this.getComponent(i);
            StampTree tree = tp.getTree();
            ret.add(tree);
        }
        return ret;
    }
    
    /**
     * �X�^���v�{�b�N�X�Ɋ܂܂��a���ȊO��StampTree��Ԃ��B
     * @return StampTree�̃��X�g
     */
    public List<StampTree> getAllPTrees() {
        
        List<StampTree> ret = new ArrayList<StampTree>();
        int cnt = this.getTabCount();
        
        for (int i = 0; i < cnt; i++) {
            StampTreePanel tp = (StampTreePanel) this.getComponent(i);
            StampTree tree = tp.getTree();
            //
            // �a��StampTree �̓X�L�b�v����
            //
            if (tree.getEntity().equals(IInfoModel.ENTITY_DIAGNOSIS)){
                continue;
            } else {
                ret.add(tree);
            }
        }
        
        return ret;
    }
    
    /**
     * �����̃G���e�B�e�B�z���ɂ���S�ẴX�^���v��Ԃ��B
     * ����̓��j���[���Ŏg�p����B
     * @param entity Tree�̃G���e�B�e�B
     * @return �S�ẴX�^���v�̃��X�g
     */
    public List<ModuleInfoBean> getAllStamps(String entity) {
        
        StampTree tree = getStampTree(entity);
        if (tree != null) {
            List<ModuleInfoBean> ret = new ArrayList<ModuleInfoBean>();
            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
            Enumeration e = rootNode.preorderEnumeration();
            while (e.hasMoreElements()) {
                StampTreeNode node = (StampTreeNode) e.nextElement();
                if (node.isLeaf()) {
                    ModuleInfoBean info = (ModuleInfoBean) node.getUserObject();
                    ret.add(info);
                }
            }
            return ret;
        }
        
        return null;
    }
    
    public List<String> getEntities() {
        List<String> ret = new ArrayList<String>();
        List<TreeInfo> infos = getAllTreeInfos();
        for (TreeInfo ti : infos) {
            ret.add(ti.getEntity());
        }
        return ret;
    }
    
    public String getInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(stampTreeModel.getName());
        sb.append(" ");
        sb.append(stampTreeModel.getPartyName());
//        sb.append(" ");
//        sb.append(ModelUtils.getDateAsString(stampTreeModel.getLastUpdated()));
        //String info = sb.toString();
        if (sb.length() > 16) {
            sb.setLength(12);
            sb.append("...");
        }
        return sb.toString();
    }
}