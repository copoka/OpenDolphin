/*
 */
package open.dolphin.plugin.helper;

import javax.swing.*;

import java.awt.event.*;

/**
 * ChainAction
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class ChainAction extends AbstractAction {
    
    private static final long serialVersionUID = -8729508189547074832L;
    
    private MenuSupport target;
    private String method;
    
    
    public void setTarget(MenuSupport target) {
        this.target = target;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    /**
     * Creates a new instance of ChainAction.
     * @param text ���j���[�C�e����
     * @param target	���j���[�T�|�[�g chain �̔��Ό�
     * @pram method ���\�b�h��
     */
    public ChainAction(String text, MenuSupport target, String method) {
        super(text);
        setTarget(target);
        setMethod(method);
    }
    
    /**
     * Creates a new instance of ChainAction.
     * @param text ���j���[�C�e����
     * @param icon �A�C�e���̃A�C�R��
     * @param target ���j���[�T�|�[�g
     * @param method ���\�b�h��
     */
    public ChainAction(String text, Icon icon, MenuSupport target, String method) {
        super(text, icon);
        setTarget(target);
        setMethod(method);
    }
    
    /**
     * ���j���[�T�|�[�g�փ��\�b�h�𑗐M����B
     * ���j���[�T�|�[�g���I�u�W�F�N�g�� chain ���Ǘ����Ă���
     * ���ԂɃ��t���N�V�������g�p���ă��\�b�h�����s����B
     * ���j���[�T�|�[�g�̓��C���E�C���h�E�A�`���[�g�E�C���h�E����
     * ���j���o�[�����I�u�W�F�N�g���ۗL���Ă���B
     */
    public void actionPerformed(ActionEvent e) {
        target.sendToChain(method);
    }
}