/*
 * PCodeHelper.java
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

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleInfoBean;

/**
 * �J���e�y�C���̃R�[�h�w���p�[�N���X�B
 *
 * @author Kazyshi Minagawa
 */
public class PCodeHelper extends AbstractCodeHelper {
    
    /**
     * Creates a new instance of CodeHelper
     */
    public PCodeHelper(KartePane pPane, ChartMediator mediator) {
        super(pPane, mediator);
    }
    
    
    protected void buildPopup(String text) {
        
        String test = text.toLowerCase();
        String entity = null;
        
        //
        // StampTree �̃L�[���[�h�Ɉ�v���Ă��邩�ǂ����𔻒肷��
        //
        Preferences prefs = Preferences.userNodeForPackage(AbstractCodeHelper.class);
        
        if (prefs.get(IInfoModel.ENTITY_TEXT, "tx").startsWith(test)) {
            entity = IInfoModel.ENTITY_TEXT;
        
        } else if (prefs.get(IInfoModel.ENTITY_PATH, "pat").startsWith(test)) {
            entity = IInfoModel.ENTITY_PATH;
            
        } else if (prefs.get(IInfoModel.ENTITY_GENERAL_ORDER, "gen").startsWith(test)) {
            entity = IInfoModel.ENTITY_GENERAL_ORDER;
            
        } else if (prefs.get(IInfoModel.ENTITY_OTHER_ORDER, "oth").startsWith(test)) {
            entity = IInfoModel.ENTITY_OTHER_ORDER;
            
        } else if (prefs.get(IInfoModel.ENTITY_TREATMENT, "tr").startsWith(test)) {
            entity = IInfoModel.ENTITY_TREATMENT;
            
        } else if (prefs.get(IInfoModel.ENTITY_SURGERY_ORDER, "sur").startsWith(test)) {
            entity = IInfoModel.ENTITY_SURGERY_ORDER;
            
        } else if (prefs.get(IInfoModel.ENTITY_RADIOLOGY_ORDER, "rad").startsWith(test)) {
            entity = IInfoModel.ENTITY_RADIOLOGY_ORDER;
            
        } else if (prefs.get(IInfoModel.ENTITY_LABO_TEST, "lab").startsWith(test)) {
            entity = IInfoModel.ENTITY_LABO_TEST;
            
        } else if (prefs.get(IInfoModel.ENTITY_PHYSIOLOGY_ORDER, "phy").startsWith(test)) {
            entity = IInfoModel.ENTITY_PHYSIOLOGY_ORDER;
            
        } else if (prefs.get(IInfoModel.ENTITY_BACTERIA_ORDER, "bac").startsWith(test)) {
            entity = IInfoModel.ENTITY_BACTERIA_ORDER;
            
        } else if (prefs.get(IInfoModel.ENTITY_INJECTION_ORDER, "inj").startsWith(test)) {
            entity = IInfoModel.ENTITY_INJECTION_ORDER;
            
        } else if (prefs.get(IInfoModel.ENTITY_MED_ORDER, "rp").startsWith(test)) {
            entity = IInfoModel.ENTITY_MED_ORDER;
            
        } else if (prefs.get(IInfoModel.ENTITY_BASE_CHARGE_ORDER, "base").startsWith(test)) {
            entity = IInfoModel.ENTITY_BASE_CHARGE_ORDER;
            
        } else if (prefs.get(IInfoModel.ENTITY_INSTRACTION_CHARGE_ORDER, "ins").startsWith(test)) {
            entity = IInfoModel.ENTITY_INSTRACTION_CHARGE_ORDER;
            
        } else if (prefs.get(IInfoModel.ENTITY_ORCA, "orca").startsWith(test)) {
            entity = IInfoModel.ENTITY_ORCA;
            
        } 
        
        if (entity != null) {
            buildEntityPopup(entity);
        
        } else {
            //
            // �S�ẴX�^���v�c���[���Ȃ߂�
            //
            buildMatchPopup(text);
        }
    }
    
    
    protected void buildMatchPopup(String text) {
        
        //
        // current StampBox��P�֘A StampTree ���擾����
        //
        StampBoxPlugin stampBox = mediator.getStampBox();
        List<StampTree> allTree = stampBox.getAllPTrees();
        if (allTree == null || allTree.size() == 0) {
            return;
        }
        
        popup = new JPopupMenu();
        
        //
        // ���j���[�̃X�^�b�N�𐶐�����
        //
        LinkedList menus = new LinkedList();
        menus.addFirst(popup);
        
        //
        // �e�m�[�h�̃X�^�b�N�𐶐�����
        //
        LinkedList parents = new LinkedList();
        
        //
        // Stamp �̖��O���L�[���[�h�Ŏn�܂�A���ꂪ�P�ȏ゠����̂�⊮���j���[�ɉ�����
        //
        pattern = Pattern.compile("^" + text + ".*");
        
        for (StampTree tree : allTree) {
            
            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
            
            Enumeration e = rootNode.preorderEnumeration();
            
            if (e != null) {
                
                e.nextElement(); // consume root
                
                while (e.hasMoreElements()) {
                    
                    //
                    // �����Ώۂ̃m�[�h�𓾂�
                    //
                    StampTreeNode node = (StampTreeNode) e.nextElement();
                    
                    // 
                    // ���̐e�𓾂�
                    //
                    StampTreeNode parent = (StampTreeNode) node.getParent();
                    
                    //
                    // �e�����X�g�Ɋ܂܂�Ă��邩�ǂ���
                    //
                    int index = parents.indexOf(parent);
                    if (index > -1) {
                        //
                        // �����̐e���C���f�b�N�X=0�ɂȂ�܂Ń|�b�v����
                        //
                        for (int i = 0; i < index; i++) {
                            parents.removeFirst();
                            menus.removeFirst();
                        }
                        
                        if (!node.isLeaf()) {
                            //
                            // �t�H���_�̏ꍇ
                            //
                            String folderName = node.getUserObject().toString();
                            JMenu subMenu = new JMenu(folderName);
                            if (menus.getFirst() instanceof JPopupMenu) {
                                ((JPopupMenu) menus.getFirst()).add(subMenu);
                            } else {
                                ((JMenu) menus.getFirst()).add(subMenu);
                            }
                            menus.addFirst(subMenu);
                            parents.addFirst(node);
                            JMenuItem item = new JMenuItem(folderName);
                            item.setIcon(icon);
                            subMenu.add(item);
                            addActionListner(item, node);
                            
                        } else {
                            ModuleInfoBean info = (ModuleInfoBean) node.getUserObject();
                            String completion = info.getStampName();
                            JMenuItem item = new JMenuItem(completion);
                            addActionListner(item, node);
                            if (menus.getFirst() instanceof JPopupMenu) {
                                ((JPopupMenu) menus.getFirst()).add(item);
                            } else {
                                ((JMenu) menus.getFirst()).add(item);
                            }
                        }
                    
                    } else {
                        //
                        // �܂܂�Ă��Ȃ��̂Ń}�b�`�������K�v
                        //
                        if (!node.isLeaf()) {
                            //
                            // �t�H���_�̏ꍇ
                            //
                            String completion = node.getUserObject().toString();
                            Matcher matcher = pattern.matcher(completion);
                            if (matcher.matches()) {
                                //
                                // �}�b�`�����ꍇ�̓J�����g���j���[�։�����
                                // �������J�����g���j���[�ɂȂ�
                                // �e���X�g�Ɏ�����������
                                String folderName = node.getUserObject().toString();
                                JMenu subMenu = new JMenu(folderName);
                                if (menus.getFirst() instanceof JPopupMenu) {
                                    ((JPopupMenu) menus.getFirst()).add(subMenu);
                                } else {
                                    ((JMenu) menus.getFirst()).add(subMenu);
                                }
                                menus.addFirst(subMenu);
                                parents.addFirst(node);
                                
                                //
                                // �t�H���_�I���̃A�C�e���𐶐����T�u���j���[�̗v�f�ɂ���
                                //
                                JMenuItem item = new JMenuItem(folderName);
                                item.setIcon(icon);
                                subMenu.add(item);
                                addActionListner(item, node);
                            }
                            
                        } else {
                            //
                            // �t�̏ꍇ
                            //
                            ModuleInfoBean info = (ModuleInfoBean) node.getUserObject();
                            String completion = info.getStampName();
                            Matcher matcher = pattern.matcher(completion);
                        
                            if (matcher.matches()) {
                                //
                                // ��v�����ꍇ
                                //
                                JMenuItem item = new JMenuItem(completion);
                                addActionListner(item, node);
                                if (menus.getFirst() instanceof JPopupMenu) {
                                    ((JPopupMenu) menus.getFirst()).add(item);
                                } else {
                                    ((JMenu) menus.getFirst()).add(item);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
