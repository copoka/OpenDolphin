/*
 * StampTree.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2001,2003,2004 Digital Globe, Inc. All rights reserved.
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

import open.dolphin.dao.*;
import open.dolphin.exception.DolphinException;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.Module;
import open.dolphin.infomodel.ModuleInfo;
import open.dolphin.infomodel.RegisteredDiagnosisModule;
import open.dolphin.infomodel.TextStamp;
import open.dolphin.project.*;
import open.dolphin.util.*;

import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.rmi.server.*;

/**
 * StampTree 
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class StampTree extends JTree 
implements TreeModelListener, DragGestureListener, DropTargetListener, DragSourceListener {
                     
    private static final int TOOLTIP_LENGTH = 35;
    private static final ImageIcon ASP_ICON = new ImageIcon(open.dolphin.client.StampTree.class.getResource("/open/dolphin/resources/images/WebComponent16.gif"));
    private static final ImageIcon LOCAL_ICON = new ImageIcon(open.dolphin.client.StampTree.class.getResource("/open/dolphin/resources/images/Bean16.gif"));
    
    /** ���� Tree ���ҏW�\���ǂ��� */
    private boolean editable;
            
    private DragSource dragSource;
    
    /** Reference to the StampBox */
    private StampBoxService stampBox;
    
        
    /**
     * Tree model ���炱�̃N���X�𐶐�����
     */
    public StampTree(TreeModel model) {
        
        super(model);

        this.putClientProperty("JTree.lineStyle", "Angled");           // �����y�ѐ��������g�p����
        this.setEditable(false);                                       // �m�[�h����ҏW�s�ɂ���
        this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);  // Single Selection �ɂ���
        this.setRootVisible(false);
        
        // Replace the default CellRenderer
        final TreeCellRenderer oldRenderer = this.getCellRenderer();
        TreeCellRenderer r = new TreeCellRenderer() {
            
            public Component getTreeCellRendererComponent(JTree tree,
                Object value,
                boolean selected,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {
                    
                    Component c = oldRenderer.getTreeCellRendererComponent(tree,
                        value,selected,expanded,leaf,row,hasFocus);
                    if (leaf && c instanceof JLabel) {
                        JLabel l = (JLabel)c;
                        Object o = ((StampTreeNode)value).getUserObject();
                        if (o instanceof ModuleInfo) {
                            
                            // �ŗL�̃A�C�R����ݒ肷��              
                            if ( ((ModuleInfo)o).isASP() ) {
                                l.setIcon(ASP_ICON);
                            }
                            else {
                                l.setIcon(LOCAL_ICON);
                            }
                            
                            // ToolTips ��ݒ肷��
                            l.setToolTipText(((ModuleInfo)o).getMemo());
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
        
        // DragEnabled
        dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
        
        // Make tree dropTarget
        new DropTarget(this, this);
    }
    
    public boolean isEdiatble() {
    	return editable;
    }
    
    public void setEdiatble(boolean b) {
    	editable = b;
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
    public void setStampBox(StampBoxService ref) {
        stampBox = ref;
    }
    
    /**
     * RootNode �̖��O��Ԃ�
     */
    public String getRootName() {
        StampTreeNode node = (StampTreeNode)this.getModel().getRoot();
        return node.toString();
    }
    
    /**
     * �I������Ă���m�[�h��Ԃ�
     */
    protected StampTreeNode getSelectedNode() {
        return (StampTreeNode) this.getLastSelectedPathComponent();   
    }
    
    /**
     * Drop �ʒu�̃m�[�h��Ԃ�
     */
    protected StampTreeNode getNode(Point p) {
        TreePath path = this.getPathForLocation(p.x, p.y);
        return (path != null) ? (StampTreeNode)path.getLastPathComponent() : null;
    }
    
    //////////////   Drag Support //////////////////
    
    public void dragGestureRecognized(DragGestureEvent event) {
        
        StampTreeNode dragNode = getSelectedNode();
        
        if (dragNode == null) {
            return;
        }
        
		dragNode.setTreeId(getRootName());
        
        Transferable t = new StampTreeTransferable(dragNode);
        Cursor cursor = DragSource.DefaultCopyDrop;

        //begin the drag
        dragSource.startDrag(event, cursor, t, this);
    }

    public void dragDropEnd(DragSourceDropEvent event) { 
    }

    public void dragEnter(DragSourceDragEvent event) {
    }

    public void dragOver(DragSourceDragEvent event) {
    }
    
    public void dragExit(DragSourceEvent event) {
    }    

    public void dropActionChanged ( DragSourceDragEvent event) {
    }   
    
    //////////// Drop Support ////////////////
        
    public void drop(DropTargetDropEvent e) {
        
        if (! isDropAcceptable(e)) {
            e.rejectDrop();
            setDropTargetBorder(false);
            return;
        }
        
        // Transferable ���擾����
        final Transferable tr = e.getTransferable();

        // Drop �ʒu�𓾂�
        final Point loc = e.getLocation();
        
        // Force copy
        e.acceptDrop(DnDConstants.ACTION_COPY);
        e.getDropTargetContext().dropComplete(true);
        setDropTargetBorder(false);
            
        boolean ok = doDrop(tr, loc);
    }
    
    public boolean isDragAcceptable(DropTargetDragEvent evt) {
    	if (!editable) {
    		return false;
    	}
        return (evt.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) != 0;
    }
    
    public boolean isDropAcceptable(DropTargetDropEvent evt) {
		if (!editable) {
			return false;
		}
        return (evt.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) != 0;
    }        

    /** DropTaregetListener interface method */
    public void dragEnter(DropTargetDragEvent e) {
        if (! isDragAcceptable(e)) {
            e.rejectDrag();
        }
    }

    /** DropTaregetListener interface method */
    public void dragExit(DropTargetEvent e) {
        setDropTargetBorder(false);
    }

    /** DropTaregetListener interface method */
    public void dragOver(DropTargetDragEvent e) { 
        if (isDragAcceptable(e)) {
            setDropTargetBorder(true);
        }
    }

    /** DropTaregetListener interface method */
    public void dropActionChanged(DropTargetDragEvent e) {
        if (! isDragAcceptable(e)) {
            e.rejectDrag();
        }
    }
    
    private void setDropTargetBorder(final boolean b) {
        Color c = b ? DesignFactory.getDropOkColor() : this.getBackground();
        this.setBorder(BorderFactory.createLineBorder(c, 2));
    }
    
    /**
     * �m�[�h�� Drop ����
     */
    protected boolean doDrop(Transferable tr, Point loc) {
                     
        int state = -1;
        
        // StampTreeNode
        if (tr.isDataFlavorSupported(StampTreeTransferable.stampTreeNodeFlavor)) {
            state = 0;
            
        // OrderList    
        } else if (tr.isDataFlavorSupported(OrderListTransferable.orderListFlavor)) {
            state = 1;
            
        // Text    
        } else if (tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            state = 2;
        
        // InforModel    
        } else if (tr.isDataFlavorSupported(InfoModelTransferable.infoModelFlavor)) {
            state = 3;
            
        } else {
            return false;
        }
        
        boolean ret = false;
        
        switch (state) {
            
            case 0:
                // TreeNode dropped
                try {
                	StampTreeNode dropNode = (StampTreeNode)tr.getTransferData(StampTreeTransferable.stampTreeNodeFlavor);
					if (dropNode.getTreeId().equals(getRootName())) {
						ret = moveNode(dropNode, loc);
					} else {
						ModuleInfo info =(ModuleInfo)dropNode.getUserObject();
						info.setEditable(true);
						ret = addNode(dropNode, loc);
					}
                } catch (Exception ufe) {
                	debug(ufe.toString());
                }  
                break;
                
            case 1:
                // Stamp dropped
                if (editable) {
                    ret = addStamp(tr, loc);
                
                }
                break;
                
            case 2:
                // Text dropped
                if (editable) {
                    ret = addTextStamp(tr, loc);
               
                }
                break;
                
            case 3:
                // RegisteredDiagnosis
                if (editable) {
                	ret = addDiagnosis(tr, loc);
                }
                break;
        }
        
        return ret;
    }
    
    /**
     * TreeNode �̈ړ����s���B
     */
    protected boolean moveNode(StampTreeNode dropNode, Point loc) {  

        // Drop �ʒu�̃m�[�h���擾
        StampTreeNode destinationNode = getNode(loc);
        
        if (destinationNode == null) {
            //���ꂪ�k���̏ꍇ�̓��^�[��
            return false;
            
        } else if (destinationNode == getSelectedNode()) {
            // Drag Node �ɓ����̏ꍇ�̓��^�[��
            return false;
        }
        
        boolean ret = false;
        
        // Drop(Drg)Node �� ���i���j�̐e
        StampTreeNode orgNode = getSelectedNode();
        StampTreeNode oldParent = (StampTreeNode)orgNode.getParent();

        // Move Action �̏ꍇ�͋��̐e����m�[�h��؂藣��
        orgNode.removeFromParent();

        /**
         * Drop �ʒu���t�H���_�ł���� dropNode �����̃t�H���_�� add
         * Drop �ʒu���t�m�[�h�ł���� dropNode �����̐e�� insert
         */
        StampTreeNode newParent = null;

        if (! destinationNode.isLeaf()) {
            newParent = destinationNode;
            newParent.add(dropNode);
            
        } else {
            // �e�𓾂�
            newParent = (StampTreeNode)destinationNode.getParent();

            // Drop �ʒu�̃C���f�b�N�X�𓾂�
            int index = newParent.getIndex(destinationNode);

            // �����֑}������
            newParent.insert(dropNode, index);
        }

        //expand nodes appropriately - this probably isnt the best way...
        DefaultTreeModel model = (DefaultTreeModel)this.getModel();
        model.reload(oldParent);
        model.reload(newParent);
        TreePath parentPath = new TreePath(newParent.getPath());
        this.expandPath(parentPath);
        parentPath = new TreePath(oldParent.getPath());
        this.expandPath(parentPath);

        // �����܂ŗ����琬��
        ret = true;
        
        return ret;
    }
    
    /**
     * ���� StampTree ���� Drop ���ꂽ�@TreeNode �������ɉ�����B
     */
	private boolean addNode(StampTreeNode dropNode, Point loc) {  

		// Drop �ʒu�̃m�[�h���擾
		StampTreeNode destinationNode = getNode(loc);
        
		if (destinationNode == null) {
			//���ꂪ�k���̏ꍇ�� root �����ɒǉ�����
			destinationNode = (StampTreeNode)this.getModel().getRoot();
		}
        
		boolean ret = false;
        
		/**
		 * Drop �ʒu���t�H���_�ł���� dropNode �����̃t�H���_�� add
		 * Drop �ʒu���t�m�[�h�ł���� dropNode �����̐e�� insert
		 */
		StampTreeNode newParent = null;

		if (! destinationNode.isLeaf()) {
			newParent = destinationNode;
			newParent.add(dropNode);
            
		} else {
			// �e�𓾂�
			newParent = (StampTreeNode)destinationNode.getParent();

			// Drop �ʒu�̃C���f�b�N�X�𓾂�
			int index = newParent.getIndex(destinationNode);

			// �����֑}������
			newParent.insert(dropNode, index);
		}

		//expand nodes appropriately - this probably isnt the best way...
		DefaultTreeModel model = (DefaultTreeModel)this.getModel();
		model.reload(newParent);
		TreePath parentPath = new TreePath(newParent.getPath());
		this.expandPath(parentPath);

		// �����܂ŗ����琬��
		ret = true;
        
		return ret;
	}    
    
    /**
     * KartePane ����@Drag & Drop ���ꂽ�X�^���v���i��������
     */
    protected boolean addStamp(Transferable tr, Point loc) {
        
        boolean ret = false;
        
        try {
            // �X�^���v�� PTrainTrasnferable
            OrderList list = (OrderList)tr.getTransferData(OrderListTransferable.orderListFlavor);
            final Module stamp = list.orderList[0];
            ModuleInfo org = stamp.getModuleInfo();
            
            // �f�[�^�x�[�X�� Stamp �̃f�[�^���f�����i��������
            String stampId = Project.createUUID();
            String userId = Project.getUserId();
            String category = org.getEntity();
            
            final SqlStampDao dao = (SqlStampDao)SqlDaoFactory.create(this, "dao.stamp");
            
            if (! dao.addStamp(userId, category, stampId, (IInfoModel)stamp.getModel())) {
                        
                throw new DolphinException("Unable to save the stamp");
            }
            
            // �V���� StampInfo �𐶐�����
            ModuleInfo info = new ModuleInfo();
            //info.setName(org.getName() + "-copy");        // �I���W�i����-copy
            info.setName(org.getName());                    // �I���W�i����-copy
            info.setEntity(org.getEntity());  				// Entity
            info.setRole(org.getRole());                    // Role
            info.setMemo(constractToolTip(stamp));          // Tooltip                        
            info.setStampId(stampId);                       // Stamp ID
                       
            // StampInfo ����V���� StampTreeNode �𐶐�����
            StampTreeNode node = new StampTreeNode(info);
            
            // ������^�[�Q�b�g�� StampTree�ɉ�����
            // �i�[���� StampTree ���擾����
            final StampTree theTree = stampBox.getStampTree(category);
            boolean notMe = (theTree != this) ? true : false;
            DefaultTreeModel model = (DefaultTreeModel)theTree.getModel();
            StampTreeNode root = (StampTreeNode)model.getRoot();
            root.add(node);
            model.reload(root);            
            
            // ���b�Z�[�W��\������
            if (notMe) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        StringBuffer buf = new StringBuffer();
                        buf.append("�X�^���v�� ");
                        buf.append(theTree.getRootName());
                        buf.append(" �Ɋi�[���܂����B");
                        JOptionPane.showMessageDialog(null,
                                             buf.toString(),
                                             "Dolphin: �X�^���v�o�^",
                                             JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }
            
            ret = true;
            
        } catch (IOException ie) {
            //ClientContext.getLogger().warning("IOException at addStamp: " + ie.toString());
			debug(ie.toString());
        
        } catch (UnsupportedFlavorException ue) {
            //ClientContext.getLogger().warning("UnsupportedFlavorException at addStamp: " + ue.toString());
			debug(ue.toString());
        
        } catch (DolphinException de) {
            //ClientContext.getLogger().warning("DolphinException at addStamp: " + de.toString());
			debug(de.toString());
        }
        
        return ret;
    }
    
    /**
     * Diagnosis Table ����@Drag & Drop ���ꂽRegisteredDiagnosis���X�^���v������
     */
    protected boolean addDiagnosis(Transferable tr, Point loc) {
        
        boolean ret = false;
        
        try {
            // �X�^���v�� InfoModelTrasnferable
            RegisteredDiagnosisModule rd = (RegisteredDiagnosisModule)tr.getTransferData(InfoModelTransferable.infoModelFlavor);
            
            // ���t���N���A
            rd.setFirstEncounterDate(null);
            rd.setEndDate(null);
            
            final Module stamp = new Module();
            stamp.setModel(rd);
            
            // �f�[�^�x�[�X�� Stamp �̃f�[�^���f�����i��������
            String stampId = Project.createUUID();
            String userId = Project.getUserId();
            String category = "diagnosis";
            
            final SqlStampDao dao = (SqlStampDao)SqlDaoFactory.create(this, "dao.stamp");
            
            if (! dao.addStamp(userId, category, stampId, (IInfoModel)stamp.getModel())) {
                        
                throw new DolphinException("Unable to save the stamp");
            }
            
            // �V���� StampInfo �𐶐�����
            ModuleInfo info = new ModuleInfo();
            info.setName(rd.getDiagnosis());                // ���a��
            info.setEntity(category);                		// �J�e�S��
            info.setRole("diagnosis");                      // Role
            
            StringBuffer buf = new StringBuffer();
            buf.append(rd.getDiagnosis());
            String cd = rd.getDiagnosisCode();
            if (cd != null) {
                buf.append("(");
                buf.append(cd);
                buf.append(")");   // Tooltip  
            } 
            info.setMemo(buf.toString());
            info.setStampId(stampId);                       // Stamp ID
                       
            // StampInfo ����V���� StampTreeNode �𐶐�����
            StampTreeNode node = new StampTreeNode(info);
            
            // ������^�[�Q�b�g�� StampTree�ɉ�����
            // �i�[���� StampTree ���擾����
            final StampTree theTree = stampBox.getStampTree(category);
            boolean notMe = (theTree != this) ? true : false;
            DefaultTreeModel model = (DefaultTreeModel)theTree.getModel();
            StampTreeNode root = (StampTreeNode)model.getRoot();
            root.add(node);
            model.reload(root);            
            
            // ���b�Z�[�W��\������
            if (notMe) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        StringBuffer buf = new StringBuffer();
                        buf.append("�X�^���v�� ");
                        buf.append(theTree.getRootName());
                        buf.append(" �Ɋi�[���܂����B");
                        JOptionPane.showMessageDialog(null,
                                             buf.toString(),
                                             "Dolphin: �X�^���v�o�^",
                                             JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }
            
            ret = true;
            
        } catch (IOException ie) {
            //ClientContext.getLogger().warning("IOException at addStamp: " + ie.toString());
			debug(ie.toString());
        
        } catch (UnsupportedFlavorException ue) {
            //ClientContext.getLogger().warning("UnsupportedFlavorException at addStamp: " + ue.toString());
			debug(ue.toString());
        
        } catch (DolphinException de) {
            //ClientContext.getLogger().warning("DolphinException at addStamp: " + de.toString());
			debug(de.toString());
        }
        
        return ret;
    }    
    
    /**
     * �e�L�X�g�X�^���v��o�^
     */
    protected boolean addTextStamp(Transferable tr, Point loc) {
        
        boolean ret = false;
        
        try {
            // �X�^���v�� PTrainTrasnferable
            String text = (String)tr.getTransferData(DataFlavor.stringFlavor);
            
            final TextStamp stamp = new TextStamp();
            stamp.setText(text);
            //DolphinContext.getLogger().warning(stamp.toString());
            
            // �f�[�^�x�[�X�� Stamp �̃f�[�^���f�����i��������
            String stampId = Project.createUUID();
            String userId = Project.getUserId();
            
            final SqlStampDao dao = (SqlStampDao)SqlDaoFactory.create(this, "dao.stamp");
            
            if (! dao.addStamp(userId, "text", stampId, (IInfoModel)stamp)) {
                throw new DolphinException("Unable to save the stamp");
            }
            
            // �V���� StampInfo �𐶐�����
            ModuleInfo info = new ModuleInfo();
            int len = text.length() > 16 ? 16 : text.length();
            String name = text.substring(0, len);
            len = name.indexOf("\n");
            if (len > 0 ) {
                name = name.substring(0, len);
            }
            info.setName(name);                             // 
            info.setEntity("text");                  		// �J�e�S��
            info.setRole("text");                           // Role
            info.setMemo(text);                             // Tooltip                        
            info.setStampId(stampId);                       // Stamp ID
                       
            // StampInfo ����V���� StampTreeNode �𐶐�����
            StampTreeNode node = new StampTreeNode(info);
            
            // ������^�[�Q�b�g�� StampTree�ɉ�����
            // �i�[���� StampTree ���擾����
            String category = info.getEntity();
            final StampTree theTree = stampBox.getStampTree(category);
            boolean notMe = (theTree != this) ? true : false;
            DefaultTreeModel model = (DefaultTreeModel)theTree.getModel();
            StampTreeNode root = (StampTreeNode)model.getRoot();
            root.add(node);
            model.reload(root);            
            
            // ���b�Z�[�W��\������
            if (notMe) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        StringBuffer buf = new StringBuffer();
                        buf.append("�X�^���v�� ");
                        buf.append(theTree.getRootName());
                        buf.append(" �Ɋi�[���܂����B");
                        JOptionPane.showMessageDialog(null,
                                             buf.toString(),
                                             "Dolphin: �X�^���v�o�^",
                                             JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }
            
            ret = true;
            
        } catch (IOException ie) {
            //ClientContext.getLogger().warning("IOException at addStamp: " + ie.toString());
			debug(ie.toString());
        
        } catch (UnsupportedFlavorException ue) {
            //ClientContext.getLogger().warning("UnsupportedFlavorException at addStamp: " + ue.toString());
			debug(ue.toString());
        
        } catch (DolphinException de) {
            //ClientContext.getLogger().warning("DolphinException at addStamp: " + de.toString());
			debug(de.toString());
        }
        return ret;
    }    
    
    /**
     * ASP Stamp �Ƃ��ĕۑ�����BASP_TOOL���[�h�̎��̂ݓ��삷��B
     */
    protected boolean addAspStamp(Transferable tr, Point loc) {
        
        boolean ret = false;
        
        try {
            OrderList list = (OrderList)tr.getTransferData(OrderListTransferable.orderListFlavor);
            final Module stamp = list.orderList[0];
            ModuleInfo org = stamp.getModuleInfo();
            
            // �f�[�^�x�[�X�� Stamp �̃f�[�^���f�����i��������
            UID docId = new UID();
            final String uid = docId.toString();            
            final AspStampModelDao dao = (AspStampModelDao)StampDaoFactory.createAspDao(this, "dao.aspStampModel");
            
            Runnable r = new Runnable() {
                public void run() {
                    if (! dao.save(uid, (IInfoModel)stamp.getModel())) {
                        //ClientContext.getLogger().warning("Failed to save the stamp");
						debug("Failed to save the stamp");
                    }
                }
            };
            Thread t = new Thread(r);
            t.start();
            
            // �V���� StampInfo �𐶐�����
            ModuleInfo info = new ModuleInfo();
            info.setName(org.getName() + "-asp");           // �I���W�i����-copy
            info.setEntity(org.getEntity());  // �J�e�S��
            info.setRole(org.getRole());                    // Role
            info.setMemo(constractToolTip(stamp));          // Tooltip                        
            info.setStampId(uid);                           // Stamp ID
            info.setASP(true);                              // ASP stamp
                       
            // StampInfo ����V���� StampTreeNode �𐶐�����
            StampTreeNode node = new StampTreeNode(info);
            
            DefaultTreeModel model = (DefaultTreeModel)this.getModel();
            StampTreeNode root = (StampTreeNode)model.getRoot();
            root.add(node);
            model.reload(root);
            
            ret = true;
            
        } catch (IOException ie) {
            //ClientContext.getLogger().warning("IOException at addStamp: " + ie.toString());
			debug(ie.toString());
        
        } catch (UnsupportedFlavorException ue) {
            //ClientContext.getLogger().warning("UnsupportedFlavorException at addStamp: " + ue.toString());
			debug(ue.toString());
        }
        
        return ret;
    }    
    
    protected String constractToolTip(Module stamp) {
        
        String ret = null;
        
        try {
            StringBuffer buf = new StringBuffer();
            BufferedReader reader = new BufferedReader(new StringReader(stamp.getModel().toString()));
            String line;
            while(true) {
                line = reader.readLine();
                
                if (line == null) {
                    break;
                }
                
                buf.append(line);
                
                if (buf.length() < TOOLTIP_LENGTH) {
                    buf.append(",");
                }
                else {
                    break;
                }
            }
            reader.close();
            if (buf.length() > TOOLTIP_LENGTH ) {
                buf.setLength(TOOLTIP_LENGTH);
            }
            buf.append("...");
            ret = buf.toString();
        
        } catch(IOException e) {
            //ClientContext.getLogger().warning(e.toString());
			debug(e.toString());
        }
        
        return ret;
    }

    //////////////  PopupMenu �T�|�[�g //////////////
    
    /**
     * �m�[�h�̖��O��ύX����
     */
    public void renameNode () {
    	
    	if (!editable) {
    		return;
    	}

        // Root �ւ̃p�X���擾����
        StampTreeNode node = getSelectedNode();
        TreeNode[] nodes = node.getPath();
        TreePath path = new TreePath(nodes);
        
        // �ҏW���J�n����
        this.setEditable(true); 
        this.startEditingAtPath(path);
        //this.setEditable (false); �� TreeModelListener �ōs��
    }
    
    /**
     * �m�[�h���폜����
     */
    public void deleteNode () {
    	
		if (!editable) {
			return;
		}
       
        // Gets the target node
        StampTreeNode theNode = getSelectedNode();
        
        // Removes template editors contained by the target node
        Enumeration e = theNode.preorderEnumeration();
        
        while(e.hasMoreElements()) {
            StampTreeNode node = (StampTreeNode)e.nextElement();
            if (node.isLeaf()) {
            	
                ModuleInfo info = (ModuleInfo)node.getUserObject();
                
                //String treeId = node.getTreeId();
                //boolean myNode = (treeId != null && treeId.equals(this.getId())) ? true : false;
                String stampId = info.getStampId();
                String category = info.getEntity();
                String userId = Project.getUserId();
                
                // �i��������Ă��郂�f�����폜����
                if (editable) {
                    
                    SqlStampDao dao = (SqlStampDao)SqlDaoFactory.create(this, "dao.stamp");
                    
                    boolean result = dao.removeStamp(userId, category, stampId);
                }
                //else if (mode == ASP_TOOL) {
                    //AspStampModelDao dao = (AspStampModelDao)StampDaoFactory.createAspDao(this, "dao.aspStampModel");
                    //dao.remove(stampId);
                //}
            }
        }
        // Removes from parent
        StampTreeNode parent = (StampTreeNode) theNode.getParent();
        parent.remove(theNode);
            
        // Tells the model
        DefaultTreeModel model = (DefaultTreeModel)getModel();
        model.reload(parent);
    }
    
    /**
     * �V�K�̃t�H���_�m�[�h��ǉ�����
     */
    public void createNewFolder () {
    	
		if (!editable) {
			return;
		}
       
        // �I�����ꂽ�m�[�h�𓾂�
        StampTreeNode node = getSelectedNode();
        StampTreeNode parent = null;

        // �m�[�h���t�Ȃ�e�֒ǉ����A�t�H���_�Ȃ玩���֒ǉ�����
        parent = (node.isLeaf () == true) ? (StampTreeNode) node.getParent() : node;

        // �V�K�m�[�h�𐶐����e�֒ǉ�����
        StampTreeNode newChild = new StampTreeNode("�V�K�t�H���_");
        parent.add (newChild);
        
        // ���f���֒ʒm����
        DefaultTreeModel model = (DefaultTreeModel) getModel();
        model.reload(parent);
        
        TreePath parentPath = new TreePath(parent.getPath());
        expandPath(parentPath);
    }
    
    ///////////// TreeModelListener ////////////////
    
    public void treeNodesChanged(TreeModelEvent e) {        
        this.setEditable(false);
    }
    
    public void treeNodesInserted(TreeModelEvent e) {        
    }
 
    public void treeNodesRemoved(TreeModelEvent e) {       
    }
    
    public void treeStructureChanged(TreeModelEvent e) {        
    }
    
	private void debug(String msg) {
		if (ClientContext.isDebug()) {
			System.out.println(msg);
		}
	}
}