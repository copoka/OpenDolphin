package open.dolphin.ejb;

import java.util.Date;
import java.util.List;

import open.dolphin.dto.AppointSpec;
import open.dolphin.dto.DiagnosisSearchSpec;
import open.dolphin.dto.DocumentSearchSpec;
import open.dolphin.dto.ImageSearchSpec;
import open.dolphin.dto.ModuleSearchSpec;
import open.dolphin.dto.ObservationSearchSpec;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.KarteBean;
import open.dolphin.infomodel.ObservationModel;
import open.dolphin.infomodel.PatientMemoModel;
import open.dolphin.infomodel.RegisteredDiagnosisModel;
import open.dolphin.infomodel.SchemaModel;

public interface RemoteKarteService {  

    /**
     * �J���e�̊�b�I�ȏ����܂Ƃ߂ĕԂ��B
     * ����̓N���C�C���g���J���e���I�[�v�����鎞�A�Ȃ�ׂ��ʐM�g���t�B�b�N�����Ȃ����邽�߂̎�i�ł���B
     * @param patientPk ���҂� Database Primary Key
     * @param fromDate �e��G���g���̌����J�n��
     * @return ��b�I�ȏ����t�F�b�`���� KarteBean
     */
    public KarteBean getKarte(long patientPk, Date fromDate);
    
    /**
     * ���������G���g�����擾����B
     * @param karteId �J���eId
     * @param fromDate �擾�J�n��
     * @param status �X�e�[�^�X
     * @return DocInfo �̃R���N�V����
     */
    public List getDocumentList(DocumentSearchSpec spec);
    
    /**
     * ����(DocumentModel Object)���擾����B
     * @param ids DocumentModel �� pk�R���N�V����
     * @return DocumentModel�̃R���N�V����
     */
    public List<DocumentModel> getDocuments(List<Long> ids);
    
    /**
     * �h�L�������g DocumentModel �I�u�W�F�N�g��ۑ�����B
     * @param karteId �J���eId
     * @param document �ǉ�����DocumentModel �I�u�W�F�N�g
     * @return �ǉ�������
     */
    public long addDocument(DocumentModel document);
    
    /**
     * �h�L�������g��_���폜����B
     * @param pk �_���폜����h�L�������g�� primary key
     * @return �폜��������
     */
    public int deleteDocument(long pk);
    
    /**
     * �h�L�������g�̃^�C�g����ύX����B
     * @param pk �ύX����h�L�������g�� primary key
     * @return �ύX��������
     */
    public int updateTitle(long pk, String title);
    
    /**
     * ModuleModel�G���g�����擾����B
     * @param spec ���W���[�������d�l
     * @return ModuleModel���X�g�̃��X�g
     */
    public List<List> getModules(ModuleSearchSpec spec);
    
    /**
     * SchemaModel�G���g�����擾����B
     * @param karteId �J���eID
     * @param fromDate
     * @param toDate
     * @return SchemaModel�G���g���̔z��
     */
    public List<List> getImages(ImageSearchSpec spec);
    
    /**
     * �摜���擾����B
     * @param id SchemaModel Id
     * @return SchemaModel
     */
    public SchemaModel getImage(long id);
    
    /**
     * ���a�����X�g���擾����B
     * @param spec �����d�l
     * @return ���a���̃��X�g
     */
    public List<RegisteredDiagnosisModel> getDiagnosis(DiagnosisSearchSpec spec);
    
    /**
     * ���a����ǉ�����B
     * @param addList �ǉ����鏝�a���̃��X�g
     * @return id�̃��X�g
     */
    public List<Long> addDiagnosis(List<RegisteredDiagnosisModel> addList);
    
    /**
     * ���a�����X�V����B
     * @param updateList
     * @return �X�V�� 
     */
    public int updateDiagnosis(List<RegisteredDiagnosisModel> updateList);
    
    /**
     * ���a�����폜����B
     * @param removeList �폜���鏝�a����id���X�g
     * @return �폜��
     */
    public int removeDiagnosis(List<Long> removeList);
    
    /**
     * Observation���擾����B
     * @param spec �����d�l
     * @return Observation�̃��X�g
     */
    public List<ObservationModel> getObservations(ObservationSearchSpec spec);
    
    /**
     * Observation��ǉ�����B
     * @param observations �ǉ�����Observation�̃��X�g
     * @return �ǉ�����Observation��Id���X�g
     */
    public List<Long> addObservations(List<ObservationModel> observations);
    
    /**
     * Observation���X�V����B
     * @param observations �X�V����Observation�̃��X�g
     * @return �X�V������
     */
    public int updateObservations(List<ObservationModel> observations);
    
    /**
     * Observation���폜����B
     * @param observations �폜����Observation�̃��X�g
     * @return �폜������
     */
    public int removeObservations(List<Long> observations);
    
    /**
     * ���҃������X�V����B
     * @param memo �X�V���郁��
     */
    public int updatePatientMemo(PatientMemoModel memo);
    
    /**
     * �\���ۑ��A�X�V�A�폜����B
     * @param spec �\����� DTO
     */
    public int putAppointments(AppointSpec spec);
    
    /**
     * �\�����������B
     * @param spec �����d�l
     * @return �\��� Collection
     */
    public List<List> getAppointmentList(ModuleSearchSpec spec);
    
}
