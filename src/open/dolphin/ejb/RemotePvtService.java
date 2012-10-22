package open.dolphin.ejb;

import java.util.Collection;

import open.dolphin.dto.PatientVisitSpec;
import open.dolphin.infomodel.PatientVisitModel;

/**
 * RemotePvtService
 *
 * @author Minagawa,Kazushi
 *
 */
public interface RemotePvtService {
    
    /**
     * ���җ��@����ۑ�����B
     * @param pvtValue PatientVisitValue
     */
    public int addPvt(PatientVisitModel model);
    
    /**
     * �{�݂̊��җ��@�����擾����B
     * @param spec �����d�l�I�u�W�F�N�g
     * @return Collection
     */
    public Collection<PatientVisitModel> getPvt(PatientVisitSpec spec);
    
        /**
     * ���@�����폜����B
     * @param id ���R�[�h ID
     * @return �폜����
     */
    public int removePvt(long id);
    
    /**
     * �f�@�I���̏�Ԃ��������ށB
     * @param pk ���R�[�hID
     * @state �f�@�I���t���O 1 �̎��I��
     */
    public int updatePvtState(long pk, int state);
}
