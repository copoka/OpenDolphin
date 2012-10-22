package open.dolphin.ejb;

import java.util.Collection;

import open.dolphin.dto.PatientSearchSpec;
import open.dolphin.infomodel.PatientModel;

/**
 * RemotePatientService
 *
 * @author Minagawa,Kazushi
 *
 */
public interface RemotePatientService {
    
    /**
     * ���҃I�u�W�F�N�g���擾����B
     * @param spec PatientSearchSpec �����d�l
     * @return ���҃I�u�W�F�N�g�� Collection
     */
    public Collection getPatients(PatientSearchSpec spec);
    
    /**
     * ����ID(BUSINESS KEY)���w�肵�Ċ��҃I�u�W�F�N�g��Ԃ��B
     *
     * @param patientId �{�ݓ�����ID
     * @return �Y������PatientModel
     */
    public PatientModel getPatient(String patientId);
    
    /**
     * ���҂�o�^����B
     * @param patient PatientModel
     * @return �f�[�^�x�[�X Primary Key
     */
    public long addPatient(PatientModel patient);
    
    /**
     * ���ҏ����X�V����B
     * @param patient �X�V����u���҃I�u�W�F�N�g
     * @return �X�V��
     */
    public int update(PatientModel patient);
}
