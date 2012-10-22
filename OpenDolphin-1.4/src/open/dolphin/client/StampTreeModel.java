package open.dolphin.client;

import javax.swing.tree.*;

import open.dolphin.infomodel.ModuleInfoBean;

/**
 * �X�^���v�c���[�̃��f���N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class StampTreeModel extends DefaultTreeModel {

    private static final long serialVersionUID = -2227174337081687786L;

    /**
     * �f�t�H���g�R���X�g���N�^
     */
    public StampTreeModel(TreeNode node) {
        super(node);
    }

    /**
     * �m�[�h���̕ύX���C���^�[�Z�v�g���ď�������
     */
    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

        // �ύX�m�[�h���擾����
        StampTreeNode node = (StampTreeNode) path.getLastPathComponent();

        // Debug
        //String oldString = node.toString ();
        String newString = (String) newValue;
        //System.out.println (oldString + " -> " + newString);

        /**
         * �t�m�[�h�̏ꍇ�� StampInfo �� name ��ύX����
         * �����łȂ��ꍇ�͐V����������� userObject �ɐݒ肷��
         */
        if (node.isLeaf()) {
            ModuleInfoBean info = (ModuleInfoBean) node.getUserObject();
            info.setStampName(newString);

        } else {
            node.setUserObject(newString);
        }

        // ���X�i�֒ʒm����
        nodeChanged(node);
    }
}