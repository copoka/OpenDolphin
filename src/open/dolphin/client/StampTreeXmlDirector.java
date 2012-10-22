package open.dolphin.client;

import java.util.*;
import java.io.*;
import javax.swing.tree.*;


/**
 * Director to build StampTree XML data.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class StampTreeXmlDirector {
    
    private DefaultStampTreeXmlBuilder builder;
    
    /** 
     * Creates new StampTreeXmlDirector 
     */
    public StampTreeXmlDirector(DefaultStampTreeXmlBuilder builder) {
        
        super();
        this.builder = builder;
    }
    
    /**
     * �X�^���v�c���[�S�̂�XML�ɃG���R�[�h����B
     * @param allTrees StampTree�̃��X�g
     * @return XML
     */
    public String build(ArrayList<StampTree> allTrees) {
        
        try {
            builder.buildStart();
            for (StampTree tree : allTrees) {
                lbuild(tree);
            }
            
            builder.buildEnd();
            return builder.getProduct();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * ��̃c���[��XML�ɃG���R�[�h����
     * @param tree StampTree
     * @throws IOException
     */
    private void lbuild(StampTree tree) throws IOException {
        
        // ���[�g�m�[�h���擾���`���C���h��Enumeration�𓾂�
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
        Enumeration e = rootNode.preorderEnumeration();
        StampTreeNode node = (StampTreeNode) e.nextElement();
        
        // ���[�g�m�[�h�������o��
        builder.buildRoot(node);
        
        // �q�������o��
        while (e.hasMoreElements()) {
            builder.buildNode((StampTreeNode) e.nextElement());
        }
        
        builder.buildRootEnd();
    }
}