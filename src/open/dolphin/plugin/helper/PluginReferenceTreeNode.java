/*
 * Created on 2005/06/01
 *
 */
package open.dolphin.plugin.helper;

import javax.swing.tree.DefaultMutableTreeNode;

import open.dolphin.plugin.PluginReference;

/**
 * PluginReferenceTreeNode
 * 
 * @author Kazushi Minagawa Digital Globe, Inc.
 *
 */
public class PluginReferenceTreeNode extends DefaultMutableTreeNode {
	
    private static final long serialVersionUID = -8868941741690163273L;

	public PluginReferenceTreeNode(Object userObject) {
        
        super(userObject);
        
        // class ����ێ����Ă���ꍇ�͗t�m�[�h
        // �N���X����ێ����Ă��Ȃ��ꍇ�̓J�e�S������\��
        PluginReference ref = (PluginReference)userObject;
        if (ref.getClassName() != null) {
            this.allowsChildren = false;
        }
    }
    
    /**
     * �t���ǂ�����Ԃ�
     */
    public boolean isLeaf () {
        return (! this.allowsChildren);
    }
}
