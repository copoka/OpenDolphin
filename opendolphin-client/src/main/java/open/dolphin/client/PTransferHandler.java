package open.dolphin.client;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.TransferHandler;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.ModuleModel;

/**
 * KartePaneTransferHandler
 * @author Minagawa,Kazushi
 */
public class PTransferHandler extends TransferHandler implements IKarteTransferHandler {
    
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
    
//    /**
//     * DropされたFlavorをインポートする。
//     */
//    @Override
//    public boolean importData(TransferHandler.TransferSupport support) {
//
//        if (!canImport(support)) {
//            return false;
//        }
//
//        JTextPane tc = (JTextPane) support.getComponent();
//        if (tc.equals(source) &&
//                (tc.getCaretPosition() >= p0.getOffset()) &&
//                (tc.getCaretPosition() <= p1.getOffset())) {
//            shouldRemove = false;
//            return true;
//        }
//
//        Transferable tr = support.getTransferable();
//
//        try {
//            if (support.isDataFlavorSupported(LocalStampTreeNodeTransferable.localStampTreeNodeFlavor)) {
//                // スタンプボックスからのスタンプをインポートする
//                shouldRemove = false;
//                StampTreeNode droppedNode = (StampTreeNode) tr.getTransferData(LocalStampTreeNodeTransferable.localStampTreeNodeFlavor);
//                return doStampInfoDrop(droppedNode);
//
//            } else if (support.isDataFlavorSupported(OrderListTransferable.orderListFlavor)) {
//                // KartePaneからのオーダスタンプをインポートする
//                OrderList list = (OrderList) tr.getTransferData(OrderListTransferable.orderListFlavor);
//                return doStampDrop(list);
//
//            } else if (support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
//                String str = (String) tr.getTransferData(DataFlavor.stringFlavor);
//                tc.replaceSelection(str);
//                shouldRemove = tc == source ? true : false;
//                return true;
//            }
//        } catch (UnsupportedFlavorException ufe) {
//        } catch (IOException ioe) {
//        }
//
//        return false;
//    }

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
                // スタンプボックスからのスタンプをインポートする
                shouldRemove = false;
                StampTreeNode droppedNode = (StampTreeNode) tr.getTransferData(LocalStampTreeNodeTransferable.localStampTreeNodeFlavor);
                return doStampInfoDrop(droppedNode);

            } else if (tr.isDataFlavorSupported(OrderListTransferable.orderListFlavor)) {
                // KartePaneからのオーダスタンプをインポートする
                OrderList list = (OrderList) tr.getTransferData(OrderListTransferable.orderListFlavor);
                return doStampDrop(list);

            } else if (tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String str = (String) tr.getTransferData(DataFlavor.stringFlavor);
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
    
//    /**
//     * インポート可能かどうかを返す。
//     */
//    @Override
//    public boolean canImport(TransferHandler.TransferSupport support) {
//        System.err.println("canImport");
//        JTextPane tc = (JTextPane) support.getComponent();
//        if (!tc.isEditable()) {
//            return false;
//        }
//        if (support.isDataFlavorSupported(DataFlavor.stringFlavor) ||
//            support.isDataFlavorSupported(LocalStampTreeNodeTransferable.localStampTreeNodeFlavor) ||
//            support.isDataFlavorSupported(OrderListTransferable.orderListFlavor)) {
//            return true;
//        }
//        return false;
//    }

    /**
     * インポート可能かどうかを返す。
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
     * Flavorリストのなかに受け入れられものがあるかどうかを返す。
     */
    protected boolean hasFlavor(DataFlavor[] flavors) {

        for (DataFlavor flavor : flavors) {
            // String OK
            if (DataFlavor.stringFlavor.equals(flavor)) {
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
     * DropされたModuleInfo(StampInfo)をインポートする。
     * @param tr Transferable
     * @return 成功した時 true
     */
    private boolean doStampInfoDrop(StampTreeNode droppedNode) {
        
        try {
            // 葉の場合
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
            
            // Dropされたノードの葉を列挙する
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
            
            // まとめてデータベースからフェッチしインポートする
            if (role.equals(IInfoModel.ROLE_TEXT)) {
                pPane.textStampInfoDropped(addList);
            } else if (role.equals(IInfoModel.ROLE_P)) {
                pPane.stampInfoDropped(addList);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return false;
    }
    
    /**
     * DropされたStamp(ModuleModel)をインポートする。
     * @param tr Transferable
     * @return インポートに成功した時 true
     */
    private boolean doStampDrop(OrderList list) {
        
        try {
            // スタンプのリストを取得する
            ModuleModel[] stamps = list.orderList;
            // pPaneにスタンプを挿入する
            for (int i = 0; i < stamps.length; i++) {
                pPane.stamp(stamps[i]);
            }
            // dragggされたスタンプがあるときdroppした数を設定する
            // これで同じpane内でのDnDを判定している
            if (pPane.getDraggedCount() > 0 && pPane.getDrragedStamp() != null) {
                pPane.setDroppedCount(stamps.length);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return false;
    }
    
    /**
     * クリップボードへデータを転送する。
     */
    @Override
    public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
        super.exportToClipboard(comp, clip, action);
        // cut の場合を処理する
        if (action == MOVE) {
            JTextPane pane = (JTextPane) comp;
            if (pane.isEditable()) {
                pane.replaceSelection("");
            }
        }
    }

    @Override
    public JComponent getComponent() {
        return pPane.getTextPane();
    }

    private boolean canPaste() {
        if (!pPane.getTextPane().isEditable()) {
            return false;
        }
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (t == null) {
            return false;
        }
        if (t.isDataFlavorSupported(DataFlavor.stringFlavor) ||
            t.isDataFlavorSupported(OrderListTransferable.orderListFlavor) ||
            t.isDataFlavorSupported(LocalStampTreeNodeTransferable.localStampTreeNodeFlavor)) {
            return true;
        }
        return false;
    }

    @Override
    public void enter(ActionMap map) {
        if (pPane.getTextPane().isEditable()) {
            map.get(GUIConst.ACTION_PASTE).setEnabled(canPaste());
            map.get(GUIConst.ACTION_INSERT_TEXT).setEnabled(true);
            map.get(GUIConst.ACTION_INSERT_STAMP).setEnabled(true);
        }
    }

    @Override
    public void exit(ActionMap map) {
    }
}
