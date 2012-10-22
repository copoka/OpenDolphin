/*
 * Created on 2005/09/23
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package open.dolphin.client;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.awt.datatransfer.*;

import javax.swing.*;
import javax.swing.text.*;

import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.ModuleModel;

/**
 * KartePaneTransferHandler
 * @author Minagawa,Kazushi
 */
public class PTransferHandler extends TransferHandler {
    
    private static final long serialVersionUID = -7891004155072724783L;
    
    private DataFlavor stringFlavor = DataFlavor.stringFlavor;
    
    // KartePane
    private KartePane pPane;
    
    private JTextPane source;
    
    private boolean shouldRemove;
    
    // Start and end position in the source text.
    // We need this information when performing a MOVE
    // in order to remove the dragged text from the source.
    Position p0 = null, p1 = null;
    
    public PTransferHandler(KartePane pPane) {
        this.pPane = pPane;
    }
    
    /**
     * Drop���ꂽFlavor���C���|�[�g����B
     */
    public boolean importData(JComponent c, Transferable tr) {
        
        JTextPane tc = (JTextPane) c;
        
        if (!canImport(c, tr.getTransferDataFlavors())) {
            return false;
        }
        
        if (tc.equals(source) &&
                (tc.getCaretPosition() >= p0.getOffset()) &&
                (tc.getCaretPosition() <= p1.getOffset())) {
            shouldRemove = false;
            return true;
        }
        
        try {
            if (tr.isDataFlavorSupported(LocalStampTreeNodeTransferable.localStampTreeNodeFlavor)) {
                // �X�^���v�{�b�N�X����̃X�^���v���C���|�[�g����
                shouldRemove = false;
                return doStampInfoDrop(tr);
                
            } else if (tr.isDataFlavorSupported(OrderListTransferable.orderListFlavor)) {
                // KartePane����̃I�[�_�X�^���v���C���|�[�g����
                return doStampDrop(tr);
                
            } else if (tr.isDataFlavorSupported(stringFlavor)) {
                String str = (String) tr.getTransferData(stringFlavor);
                tc.replaceSelection(str);
                shouldRemove = tc == source ? true : false;
                return true;
            }
        } catch (UnsupportedFlavorException ufe) {
        } catch (IOException ioe) {
        }
        
        return false;
    }
    
    // Create a Transferable implementation that contains the
    // selected text.
    protected Transferable createTransferable(JComponent c) {
        source = (JTextPane) c;
        int start = source.getSelectionStart();
        int end = source.getSelectionEnd();
        Document doc = source.getDocument();
        if (start == end) {
            return null;
        }
        try {
            p0 = doc.createPosition(start);
            p1 = doc.createPosition(end);
        } catch (BadLocationException e) {
        }
        String data = source.getSelectedText();
        return new StringSelection(data);
    }
    
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }
    
    // Remove the old text if the action is a MOVE.
    // However, we do not allow dropping on top of the selected text,
    // so in that case do nothing.
    protected void exportDone(JComponent c, Transferable data, int action) {
        JTextComponent tc = (JTextComponent) c;
        if (tc.isEditable() && (shouldRemove == true) && (action == MOVE)) {
            if ((p0 != null) && (p1 != null)
            && (p0.getOffset() != p1.getOffset())) {
                try {
                    tc.getDocument().remove(p0.getOffset(),
                            p1.getOffset() - p0.getOffset());
                } catch (BadLocationException e) {
                }
            }
        }
        shouldRemove = false;
        source = null;
    }
    
    /**
     * �C���|�[�g�\���ǂ�����Ԃ��B
     */
    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        JTextPane tc = (JTextPane) c;
        if (tc.isEditable() && hasFlavor(flavors)) {
            return true;
        }
        return false;
    }
    
    /**
     * Flavor���X�g�̂Ȃ��Ɏ󂯓�������̂����邩�ǂ�����Ԃ��B
     */
    protected boolean hasFlavor(DataFlavor[] flavors) {
        
        for (DataFlavor flavor : flavors) {
            // String OK
            if (stringFlavor.equals(flavor)) {
                return true;
            }
            // StampTreeNode(FromStampTree) OK
            if (LocalStampTreeNodeTransferable.localStampTreeNodeFlavor.equals(flavor)) {
                return true;
            }
            // OrderStamp List OK
            if (OrderListTransferable.orderListFlavor.equals(flavor)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Drop���ꂽModuleInfo(StampInfo)���C���|�[�g����B
     * @param tr Transferable
     * @return ���������� true
     */
    private boolean doStampInfoDrop(Transferable tr) {
        
        try {
            // Drop���ꂽTreeNode���擾����
            StampTreeNode droppedNode = (StampTreeNode) tr.getTransferData(LocalStampTreeNodeTransferable.localStampTreeNodeFlavor);
            
            // �t�̏ꍇ
            if (droppedNode.isLeaf()) {
                ModuleInfoBean stampInfo = (ModuleInfoBean) droppedNode.getStampInfo();
                String role = stampInfo.getStampRole();
                if (role.equals(IInfoModel.ROLE_P)) {
                    pPane.stampInfoDropped(stampInfo);
                } else if (role.equals(IInfoModel.ROLE_TEXT)) {
                    pPane.stampInfoDropped(stampInfo);
                } else if (role.equals(IInfoModel.ROLE_ORCA_SET)) {
                    pPane.stampInfoDropped(stampInfo);
                }
                return true;
            }
            
            // Drop���ꂽ�m�[�h�̗t��񋓂���
            Enumeration e = droppedNode.preorderEnumeration();
            ArrayList<ModuleInfoBean> addList = new ArrayList<ModuleInfoBean>(5);
            String role = null;
            while (e.hasMoreElements()) {
                StampTreeNode node = (StampTreeNode) e.nextElement();
                if (node.isLeaf()) {
                    ModuleInfoBean stampInfo = (ModuleInfoBean) node.getStampInfo();
                    role = stampInfo.getStampRole();
                    if (stampInfo.isSerialized() && (role.equals(IInfoModel.ROLE_P) || (role.equals(IInfoModel.ROLE_TEXT))) ) {
                        addList.add(stampInfo);
                    }
                }
            }
            
            if (role == null) {
                return true;
            }
            
            // �܂Ƃ߂ăf�[�^�x�[�X����t�F�b�`���C���|�[�g����
            if (role.equals(IInfoModel.ROLE_TEXT)) {
                pPane.textStampInfoDropped(addList);
            } else if (role.equals(IInfoModel.ROLE_P)) {
                pPane.stampInfoDropped(addList);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Drop���ꂽStamp(ModuleModel)���C���|�[�g����B
     * @param tr Transferable
     * @return �C���|�[�g�ɐ��������� true
     */
    private boolean doStampDrop(Transferable tr) {
        
        try {
            // �X�^���v�̃��X�g���擾����
            OrderList list = (OrderList) tr.getTransferData(OrderListTransferable.orderListFlavor);
            ModuleModel[] stamps = list.orderList;
            // pPane�ɃX�^���v��}������
            for (int i = 0; i < stamps.length; i++) {
                pPane.stamp(stamps[i]);
            }
            // draggg���ꂽ�X�^���v������Ƃ�dropp��������ݒ肷��
            // ����œ���pane���ł�DnD�𔻒肵�Ă���
            if (pPane.getDraggedCount() > 0 && pPane.getDrragedStamp() != null) {
                pPane.setDroppedCount(stamps.length);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * �N���b�v�{�[�h�փf�[�^��]������B
     */
    public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
        super.exportToClipboard(comp, clip, action);
        // cut �̏ꍇ����������
        if (action == MOVE) {
            JTextPane pane = (JTextPane) comp;
            if (pane.isEditable()) {
                pane.replaceSelection("");
            }
        }
    }
}
