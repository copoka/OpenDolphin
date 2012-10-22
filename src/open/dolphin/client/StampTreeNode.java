package open.dolphin.client;

import javax.swing.tree.*;

import open.dolphin.infomodel.ModuleInfoBean;

/**
 * StampTree �̃m�[�h�N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class StampTreeNode extends DefaultMutableTreeNode {

    /**
     * �R���X�g���N�^
     */
    public StampTreeNode(Object userObject) {
        
        super(userObject);
        
        // StampInfo �ŏ��������ꂽ�ꍇ�͗t�m�[�h�ɂ���
        if (userObject instanceof open.dolphin.infomodel.ModuleInfoBean) {
            this.allowsChildren = false;
        }
    }
    
    /**
     * �t���ǂ�����Ԃ�
     */
    @Override
    public boolean isLeaf() {
        return (! this.allowsChildren);
    }
    
    /**
     * StampInfo ��Ԃ�
     */
    public ModuleInfoBean getStampInfo() {
        return (ModuleInfoBean) userObject;
    }
}