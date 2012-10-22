/*
 * Created on 2005/06/08
 *
 */
package open.dolphin.plugin;


/**
 * �񌈒�I�Ȓ����ԃ^�X�N�̃C���^�[�t�F�C�X�B
 * ���̃N���X�� run() �� TaskManager ����R�[���o�b�N�����B
 * 
 * @author Kazushi Minagawa Digital Globe, Inc.
 *
 */
public interface ILongTask {

	public void setMessage(String message);

	public String getMessage();
	
	public boolean getResult();
	
	public void run();
}
