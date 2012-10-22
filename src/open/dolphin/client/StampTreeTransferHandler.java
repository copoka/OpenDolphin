package open.dolphin.client;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.InfoModelTransferable;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.RegisteredDiagnosisModel;


/**
 * StampTreeTransferHandler
 *
 * @author Minagawa,Kazushi
 *
 */
public class StampTreeTransferHandler extends TransferHandler {
    
    private static final long serialVersionUID = 1205897976539749194L;
    
    // Drag����StampTree
    private StampTree sourceTree;
    
    // Drag����Ă���m�[�h
    private StampTreeNode dragNode;
    
    // StampTreeNode Flavor
    private DataFlavor stampTreeNodeFlavor = LocalStampTreeNodeTransferable.localStampTreeNodeFlavor;
    
    // KartePane����Drop�����I�[�_��Flavor
    private DataFlavor orderFlavor = OrderListTransferable.orderListFlavor;
    
    // KartePane����Drop�����e�L�X�gFlavor
    private DataFlavor stringFlavor = DataFlavor.stringFlavor;;
    
    // �a���G�f�B�^����Drop�����RegisteredDiagnosis Flavor
    private DataFlavor infoModelFlavor = InfoModelTransferable.infoModelFlavor;
    
    /**
     * �I�����ꂽ�m�[�h��Drag���J�n����B
     */
    protected Transferable createTransferable(JComponent c) {
        sourceTree = (StampTree) c;
        dragNode = (StampTreeNode) sourceTree.getLastSelectedPathComponent();
        return new LocalStampTreeNodeTransferable(dragNode);
    }
    
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }
    
    /**
     * Drop���ꂽFlavor��StampTree�ɃC���|�[�g����B
     */
    public boolean importData(JComponent c, Transferable tr) {
        
        if (canImport(c, tr.getTransferDataFlavors())) {
            
            try {
                // Drop���󂯂�StampTree���擾����
                StampTree target = (StampTree) c;
                String targetEntity = target.getEntity();
                
                //
                // Drop�ʒu�̃m�[�h���擾����
                // DnD �ɂ���đI����ԂɂȂ��Ă���
                //
                StampTreeNode selected = (StampTreeNode) target.getLastSelectedPathComponent();
                StampTreeNode newParent = null;
                
                if (selected != null) {
                    //
                    // Drop�ʒu�̐e���擾����
                    //
                    newParent = (StampTreeNode) selected.getParent();
                    
                } else {
                    // �܂�����X�^���v�������Ȃ�������Ԃ�
                    // TextStamp ���Aroot(�\������Ȃ�)���������Ȃ��ꍇ��
                    // �����ւ���
                    selected = null;
                    newParent = null;
                }
                
                //
                // Flavor��StampTreeNode�̎�
                // StampTree ���� DnD
                //
                if (tr.isDataFlavorSupported(stampTreeNodeFlavor) && (selected != null)) {
                    
                    // Drop�����m�[�h���擾����
                    StampTreeNode dropNode = (StampTreeNode) tr.getTransferData(stampTreeNodeFlavor);
                    
                    //
                    // root �܂ł̐e�̃p�X�̂Ȃ��Ɏ��������邩�ǂ����𔻒肷��
                    // Drop�悪 DragNode �̎q�ł��鎞�� DnD �ł��Ȃ� i.e �e�������̎q�ɂȂ邱�Ƃ͂ł��Ȃ�
                    //
                    DefaultTreeModel model = (DefaultTreeModel) target.getModel();
                    TreeNode[] parents = model.getPathToRoot(selected);
                    boolean exist = false;
                    for (TreeNode parent : parents) {
                        if (parent == (TreeNode) dropNode) {
                            exist = true;
                            Toolkit.getDefaultToolkit().beep();
                            System.out.println("new Child is ancestor");
                            break;
                        }
                    }
                    
                    if (exist) {
                        return true;
                    }
                    
                    // newChild is ancestor �̃P�[�X
                    if (newParent != dropNode) {
                    
                        // Drag����StampTree��Drop�����Tree���������ǂ����𔻒肷��
                        // shouldRemove = (sourceTree == target) ? true : false;
                        // Tree����DnD��LocalTransferable(�Q��)�̌́A�}�����_�Ō��̃X�^���v��
                        // ��ɍ폜����BDnD��̍폜�͍s��Ȃ��B
                        // shouldRemove = false;

                        if (selected.isLeaf()) {
                            //
                            // Drop�ʒu�̃m�[�h���t�̏ꍇ�A���̑O�ɑ}������
                            //
                            int index = newParent.getIndex(selected);
                            //DefaultTreeModel model = (DefaultTreeModel) target.getModel();

                            try {
                                model.removeNodeFromParent(dropNode);
                                model.insertNodeInto(dropNode, newParent, index);
                                TreeNode[] path = model.getPathToRoot(dropNode);
                                ((JTree)target).setSelectionPath(new TreePath(path));

                            } catch (Exception e1) {
                                Toolkit.getDefaultToolkit().beep();
                                e1.printStackTrace();
                            }

                        } else if (dropNode != selected) {
                            //
                            // Drop�ʒu�̃m�[�h���q�������A�Ō�̎q�Ƃ��đ}������
                            // 
                            try {
                                model.removeNodeFromParent(dropNode);
                                model.insertNodeInto(dropNode, selected, selected.getChildCount());
                                TreeNode[] path = model.getPathToRoot(dropNode);
                                ((JTree)target).setSelectionPath(new TreePath(path));

                            } catch (Exception ee) {
                                ee.printStackTrace();
                                Toolkit.getDefaultToolkit().beep();
                            }
                        }
                    }
                    
                    return true;
                    
                } else if (tr.isDataFlavorSupported(orderFlavor)) {
                    //
                    // KartePane����Drop���ꂽ�I�[�_���C���|�[�g����
                    // 
                    OrderList list = (OrderList) tr.getTransferData(OrderListTransferable.orderListFlavor);
                    ModuleModel droppedStamp = list.orderList[0];
                    
                    //
                    // ����G���e�B�e�B�̏ꍇ�A�I���͕K���N���Ă���
                    //
                    if (droppedStamp.getModuleInfo().getEntity().equals(targetEntity)) {
                        
                        return target.addStamp(droppedStamp, selected);
                        
                    } else if (targetEntity.equals(IInfoModel.ENTITY_PATH)) {
                        //
                        // �p�X Tree �̏ꍇ
                        //
                        if (selected == null) {
                            selected = (StampTreeNode) target.getModel().getRoot();
                        }
                        return target.addStamp(droppedStamp, selected);
                        
                    } else {
                        // Root�̍Ō�ɒǉ�����
                        return target.addStamp(droppedStamp, null);
                    }
                    
                } else if (tr.isDataFlavorSupported(stringFlavor)) {
                    //
                    // KartePane����Drop���ꂽ�e�L�X�g���C���|�[�g����
                    // 
                    String text = (String) tr.getTransferData(DataFlavor.stringFlavor);
                    if (targetEntity.equals(IInfoModel.ENTITY_TEXT)) {
                        return target.addTextStamp(text, selected);
                    } else {
                        return target.addTextStamp(text, null);
                    }
                    
                } else if (tr.isDataFlavorSupported(infoModelFlavor)) {
                    //
                    // DiagnosisEditor����Drop���ꂽ�a�����C���|�[�g����
                    // 
                    RegisteredDiagnosisModel rd = (RegisteredDiagnosisModel) tr.getTransferData(InfoModelTransferable.infoModelFlavor);
                    if (targetEntity.equals(IInfoModel.ENTITY_DIAGNOSIS)) {
                        return target.addDiagnosis(rd, selected);
                    } else {
                        return target.addDiagnosis(rd, null);
                    }
                    
                } else {
                    return false;
                }
                
            } catch (Exception ioe) {
                ioe.printStackTrace();
            }
        }
        
        return false;
    }
    
    /**
     * DnD��ADrag�����m�[�h������Stamptree����폜����B
     */
    protected void exportDone(JComponent c, Transferable data, int action) {
    }
    
    /**
     * �C���|�[�g�\���ǂ�����Ԃ��B
     */
    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        
        for (DataFlavor flavor : flavors) {
            if (stampTreeNodeFlavor.equals(flavor)) {
                return true;
            }
            if (orderFlavor.equals(flavor)) {
                return true;
            }
            if (stringFlavor.equals(flavor)) {
                return true;
            }
            if (infoModelFlavor.equals(flavor)) {
                return true;
            }
        }
        return false;
    }
}
