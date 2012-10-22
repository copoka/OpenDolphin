package open.dolphin.ejb;

import java.util.Collection;

import open.dolphin.infomodel.UserModel;

/**
 * RemoteUserService
 *
 * @author Minagawa, Kazushi
 */
public interface RemoteUserService {
    
    /**
     * �{�݊Ǘ��҂��@��User��o�^����B
     * @param add �o�^����User
     * @return �ǉ�����
     */
    public int addUser(UserModel add);
    
    /**
     * User����������B
     * @param userId �������郆�[�U�̕����L�[
     * @return �Y������User
     */
    public UserModel getUser(String userId);
    
    /**
     * �{�ݓ��̑SUser���擾����B
     * @return �{�ݓ����[�U���X�g
     */
    public Collection<UserModel> getAllUser();
    
    /**
     * User���(�p�X���[�h��)���X�V����B
     * @param update �X�V����User detuched
     * @return �X�V����
     */
    public int updateUser(UserModel update);
    
    /**
     * User���폜����B
     * @param removeId �폜���郆�[�U��Id
     * @return �폜����
     */
    public int removeUser(String removeId);
    
    /**
     * �{�ݏ����X�V����B
     * @param update �X�V����User detuched
     */
    public int updateFacility(UserModel update);
    
    /**
     * �T�|�[�g���C�Z���X���w������B
     * @param purchase detuched User
     */
    public void purchase(UserModel purchase);
    
}
