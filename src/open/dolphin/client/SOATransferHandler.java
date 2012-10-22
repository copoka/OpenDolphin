package open.dolphin.client;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.awt.datatransfer.*;

import javax.swing.*;
import javax.swing.text.*;

import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.SchemaModel;

/**
 * KartePaneTransferHandler
 *
 * @author Minagawa,Kazushi
 */
public class SOATransferHandler extends TransferHandler {
    
    private static final long serialVersionUID = -7891004155072724783L;
    
    private KartePane soaPane;
    
    private DataFlavor stringFlavor = DataFlavor.stringFlavor;
    
    private JTextPane source;
    
    private boolean shouldRemove;
    
    // Start and end position in the source text.
    // We need this information when performing a MOVE
    // in order to remove the dragged text from the source.
    Position p0 = null, p1 = null;
    
    public SOATransferHandler(KartePane soaPane) {
        this.soaPane = soaPane;
    }
    
    /**
     * Drop���ꂽFlavor���C���|�[�g����B
     */
    @Override
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
                // StampTreeNode���󂯓����
                shouldRemove = false;
                return doStampInfoDrop(tr);
                
            } else if (tr.isDataFlavorSupported(ImageEntryTransferable.imageEntryFlavor)) {
                // �V�F�[�}�{�b�N�X�����DnD���󂯓����
                return doImageEntryDrop(tr);
                
            } else if (tr.isDataFlavorSupported(SchemaListTransferable.schemaListFlavor)) {
                // Pane����̃V�F�[�}���󂯓����
                return doSchemaDrop(tr);
                
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
    @Override
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
    
    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }
    
    // Remove the old text if the action is a MOVE.
    // However, we do not allow dropping on top of the selected text,
    // so in that case do nothing.
    @Override
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
    @Override
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
            // String ok
            if (stringFlavor.equals(flavor)) {
                return true;
            }
            // StampTreeNode OK
            if (LocalStampTreeNodeTransferable.localStampTreeNodeFlavor.equals(flavor)) {
                return true;
            }
            // Schema OK
            if (SchemaListTransferable.schemaListFlavor.equals(flavor)) {
                return true;
            }
            // Image OK
            if (ImageEntryTransferable.imageEntryFlavor.equals(flavor)) {
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
                if (role.equals(IInfoModel.ROLE_TEXT)) {
                    soaPane.stampInfoDropped(stampInfo);
                } else if (role.equals(IInfoModel.ROLE_SOA)) {
                    soaPane.stampInfoDropped(stampInfo);
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
                    if (stampInfo.isSerialized() && (!stampInfo.getEntity().equals(IInfoModel.ENTITY_DIAGNOSIS)) ) {
                        if (role == null) {
                            role = stampInfo.getStampRole();
                        }
                        addList.add(stampInfo);
                    }
                }
            }
            
            // �܂Ƃ߂ăf�[�^�x�[�X����t�F�b�`���C���|�[�g����
            if (role.equals(IInfoModel.ROLE_TEXT)) {
                soaPane.textStampInfoDropped(addList);
            } else if (role.equals(IInfoModel.ROLE_SOA)) {
                soaPane.stampInfoDropped(addList);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Drop���ꂽ�V�F�[�}���C���|�[�I����B
     * @param tr
     * @return
     */
    private boolean doSchemaDrop(Transferable tr) {
        
        try {
            // Schema���X�g���擾����
            SchemaList list = (SchemaList) tr.getTransferData(SchemaListTransferable.schemaListFlavor);
            SchemaModel[] schemas = list.schemaList;
            for (int i = 0; i < schemas.length; i++) {
                soaPane.stampSchema(schemas[i]);
            }
            if (soaPane.getDraggedCount() > 0 && soaPane.getDrragedStamp() != null) {
                soaPane.setDroppedCount(schemas.length);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Drop���ꂽ�C���[�W���C���|�[�g����B
     */
    private boolean doImageEntryDrop(final Transferable tr) {
        
        try {
            // Image���擾����
            ImageEntry entry = (ImageEntry) tr.getTransferData(ImageEntryTransferable.imageEntryFlavor);
            soaPane.imageEntryDropped(entry);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * �N���b�v�{�[�h�փf�[�^��]������B
     */
    @Override
    public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
        super.exportToClipboard(comp, clip, action);
        // cut �̎� ...?
        if (action == MOVE) {
            JTextPane pane = (JTextPane) comp;
            if (pane.isEditable()) {
                pane.replaceSelection("");
            }
        }
    }
}
