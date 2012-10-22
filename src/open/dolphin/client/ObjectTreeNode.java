/*
 * Created on 2005/06/01
 *
 */
package open.dolphin.client;

import javax.swing.tree.DefaultMutableTreeNode;

import open.dolphin.project.ObjectBox;

/**
 * ObjectTreeNode
 * 
 * @author Kazushi Minagawa Digital Globe, Inc.
 *
 */
public class ObjectTreeNode extends DefaultMutableTreeNode {
	
    private static final long serialVersionUID = -2595126726183270328L;

	public ObjectTreeNode(Object userObject) {
        
        super(userObject);
        
        // class ����ێ����Ă���ꍇ�͗t�m�[�h
        // �N���X����ێ����Ă��Ȃ��ꍇ�̓J�e�S������\��
        ObjectBox box = (ObjectBox)userObject;
        if (box.getClassName() != null) {
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
