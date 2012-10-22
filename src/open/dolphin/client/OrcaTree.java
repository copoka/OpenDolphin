/*
 * OrcaTree.java
 * Copyright (C) 2007 Digital Globe, Inc. All rights reserved.
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

import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import open.dolphin.dao.SqlOrcaSetDao;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.OrcaInputCd;
import open.dolphin.project.Project;
import open.dolphin.util.ReflectMonitor;

/**
 * ORCA StampTree �N���X�B
 *
 * @author Kazushi Minagawa
 */
public class OrcaTree extends StampTree {
    
    private static final String MONITOR_TITLE = "ORCA�Z�b�g����";
    
    /** ORCA ���̓Z�b�g���t�F�b�`�������ǂ����̃t���O */
    private boolean fetched;
    
    /** 
     * Creates a new instance of OrcaTree 
     */
    public OrcaTree(TreeModel model) {
        super(model);
    }
    
    /**
     * ORCA ���̓Z�b�g���t�F�b�`�������ǂ�����Ԃ��B
     * @return �擾�ς݂̂Ƃ� true
     */
    public boolean isFetched() {
        return fetched;
    }
    
    /**
     * ORCA ���̓Z�b�g���t�F�b�`�������ǂ�����ݒ肷��B
     * @param fetched �擾�ς݂̂Ƃ� true
     */
    public void setFetched(boolean fetched) {
        this.fetched = fetched;
    }
    
    /**
     * StampBox �̃^�u�ł���Tree���I�����ꂽ���R�[�������B
     */
    public void enter() {
        
        if (!fetched) {

            // CLAIM(Master) Address ���ݒ肳��Ă��Ȃ��ꍇ�Ɍx������
            String address = Project.getClaimAddress();
            if (address == null || address.equals("")) {
//                if (SwingUtilities.isEventDispatchThread()) {
//                    String msg0 = "���Z�R����IP�A�h���X���ݒ肳��Ă��Ȃ����߁A�}�X�^�[�������ł��܂���B";
//                    String msg1 = "���ݒ胁�j���[���烌�Z�R����IP�A�h���X��ݒ肵�Ă��������B";
//                    Object message = new String[]{msg0, msg1};
//                    Window parent = SwingUtilities.getWindowAncestor(OrcaTree.this);
//                    String title = ClientContext.getFrameTitle(MONITOR_TITLE);
//                    JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
//                }
                return;
            }

            if (SwingUtilities.isEventDispatchThread()) {
                fetchOrcaSet();
            } else {
                fetchOrcaSet2();
            }
        }
    }
    
    /**
     * ORCA �̓��̓Z�b�g���擾��Tree�ɉ�����B
     */
    private void fetchOrcaSet2() {
        
        try {
            SqlOrcaSetDao dao = new SqlOrcaSetDao();
            
            ArrayList<OrcaInputCd> inputSet = dao.getOrcaInputSet();
            StampTreeNode root = (StampTreeNode) this.getModel().getRoot();
            
            for (OrcaInputCd set : inputSet) {
                ModuleInfoBean stampInfo = set.getStampInfo();
                StampTreeNode node = new StampTreeNode(stampInfo);
                root.add(node);
            }
            
            DefaultTreeModel model = (DefaultTreeModel) this.getModel();
            model.reload(root);
            
            setFetched(true);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
      
    /**
     * ORCA �̓��̓Z�b�g���擾��Tree�ɉ�����B
     */
    private void fetchOrcaSet() {

        
        // DAO�𐶐�����
        final SqlOrcaSetDao dao = new SqlOrcaSetDao();
        
        // ReflectMonitor �𐶐�����
        final ReflectMonitor rm = new ReflectMonitor();
        rm.setReflection(dao, 
                         "getOrcaInputSet", 
                         (Class[]) null, 
                         (Object[]) null);
        rm.setMonitor(SwingUtilities.getWindowAncestor(this), MONITOR_TITLE, "���̓Z�b�g���������Ă��܂�...  ", 200, 60*1000);
        
        //
        // ReflectMonitor �̌���State property �̑������X�i�𐶐�����
        //
        PropertyChangeListener pl = new PropertyChangeListener() {
           
            public void propertyChange(PropertyChangeEvent e) {
                
                int state = ((Integer) e.getNewValue()).intValue();
                
                switch (state) {
                    
                    case ReflectMonitor.DONE:
                        processResult(dao.isNoError(), rm.getResult(), dao.getErrorMessage());
                        break;
                        
                    case ReflectMonitor.TIME_OVER:
                        Window parent = SwingUtilities.getWindowAncestor(OrcaTree.this);
                        String title = ClientContext.getString(MONITOR_TITLE);
                        new TimeoutWarning(parent, title, null).start();
                        break;
                        
                    case ReflectMonitor.CANCELED:
                        break;
                }
                
                //
                // Block ����������
                //
                //setBusy(false);
            }
        };
        rm.addPropertyChangeListener(pl);
        
        //
        // Block ���A���\�b�h�̎��s���J�n����
        //
        //setBusy(true);
        rm.start();
    }
    
    /**
     * ORCA�Z�b�g��StampTree���\�z����B
     */
    private void processResult(boolean noErr, Object result, String message) {
        
        if (noErr) {
            
            ArrayList<OrcaInputCd> inputSet = (ArrayList<OrcaInputCd>) result;
            StampTreeNode root = (StampTreeNode) this.getModel().getRoot();
            
            for (OrcaInputCd set : inputSet) {
                ModuleInfoBean stampInfo = set.getStampInfo();
                StampTreeNode node = new StampTreeNode(stampInfo);
                root.add(node);
            }
            
            DefaultTreeModel model = (DefaultTreeModel) this.getModel();
            model.reload(root);
            
            setFetched(true);
            
        } else {
            
            String title = ClientContext.getFrameTitle(MONITOR_TITLE);
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
        }
    }
}
