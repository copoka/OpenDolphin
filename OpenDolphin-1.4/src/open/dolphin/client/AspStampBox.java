package open.dolphin.client;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

/**
 * AspStampBox
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class AspStampBox extends AbstractStampBox {
    
    /** Creates new StampBoxPlugin */
    public AspStampBox() {
    }
    
    protected void buildStampBox() {
        
        try {
            // Build stampTree
            BufferedReader reader = new BufferedReader(new StringReader(stampTreeModel.getTreeXml()));
            ASpStampTreeBuilder builder = new ASpStampTreeBuilder();
            StampTreeDirector director = new StampTreeDirector(builder);
            List<StampTree> aspTrees = director.build(reader);
            reader.close();
            stampTreeModel.setTreeXml(null);
            
            // StampTree�ɐݒ肷��|�b�v�A�b�v���j���[�ƃg�����X�t�@�[�n���h���[�𐶐�����
            AspStampTreeTransferHandler transferHandler = new AspStampTreeTransferHandler();
            
            // StampBox(TabbedPane) �փ��X�g���Ɋi�[����
            for (StampTree stampTree : aspTrees) {
                stampTree.setTransferHandler(transferHandler);
                stampTree.setAsp(true);
                stampTree.setStampBox(getContext());
                StampTreePanel treePanel = new StampTreePanel(stampTree);
                this.addTab(stampTree.getTreeName(), treePanel);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}