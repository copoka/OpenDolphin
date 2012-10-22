package open.dolphin.client;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import open.dolphin.infomodel.IInfoModel;

/**
 * UserStampBox
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class UserStampBox extends AbstractStampBox {
    
    private static final String BOX_INFO = "�l�p�X�^���v�{�b�N�X";
    
    /** �e�L�X�g�X�^���v�̃^�u�ԍ� */
    private int textIndex;
    
    /** �p�X�X�^���v�̃^�u�ԍ� */
    private int pathIndex;
    
    /** ORCA �Z�b�g�̃^�u�ԍ� */
    private int orcaIndex;

    /**
     * StampBox ���\�z����B
     */
    protected void buildStampBox() {
        
        try {
            //
            // Build stampTree
            //
            BufferedReader reader = new BufferedReader(new StringReader(stampTreeModel.getTreeXml()));
            DefaultStampTreeBuilder builder = new DefaultStampTreeBuilder();
            StampTreeDirector director = new StampTreeDirector(builder);
            List<StampTree> userTrees = director.build(reader);
            reader.close();
            stampTreeModel.setTreeXml(null);
            stampTreeModel.setTreeBytes(null);
            
            // StampTree�֐ݒ肷��PopupMenu��TransferHandler�𐶐�����
            StampTreePopupAdapter popAdapter = new StampTreePopupAdapter();
            StampTreeTransferHandler transferHandler = new StampTreeTransferHandler();
            
            // StampBox(TabbedPane) �փ��X�g���Ɋi�[����
            // ���tab�ֈ��tree���Ή�
            int index = 0;
            for (StampTree stampTree : userTrees) {
                stampTree.setUserTree(true);
                stampTree.setTransferHandler(transferHandler);
                stampTree.setStampBox(getContext());
                StampTreePanel treePanel = new StampTreePanel(stampTree);
                this.addTab(stampTree.getTreeName(), treePanel);
                
                //
                // Text�APath�AORCA �̃^�u�ԍ���ۑ�����
                //
                if (stampTree.getEntity().equals(IInfoModel.ENTITY_TEXT)) {
                    textIndex = index;
                    stampTree.addMouseListener(popAdapter);
                } else if (stampTree.getEntity().equals(IInfoModel.ENTITY_PATH)) {
                    pathIndex = index;
                    stampTree.addMouseListener(popAdapter);
                } else if (stampTree.getEntity().equals(IInfoModel.ENTITY_ORCA)) {
                    orcaIndex = index;
                } else {
                    stampTree.addMouseListener(popAdapter);
                }
                
                index++;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
    /**
     * �����̃^�u�ԍ��ɑΉ�����StampTree�ɃG�f�B�^���甭�s�����邩�ǂ�����Ԃ��B
     * @param index �^�u�ԍ�
     * @return �G�f�B�^���甭�s������ꍇ�� true 
     */
    @Override
    public boolean isHasEditor(int index) {
        return (index == textIndex || index == pathIndex || index == orcaIndex) ? false : true;
    }

    @Override
    public void setHasNoEditorEnabled(boolean b) {
        this.setEnabledAt(textIndex, b);
        this.setEnabledAt(pathIndex, b);
        this.setEnabledAt(orcaIndex, b);
    }
    
    @Override
    public String getInfo() {
        return BOX_INFO;
    }
}