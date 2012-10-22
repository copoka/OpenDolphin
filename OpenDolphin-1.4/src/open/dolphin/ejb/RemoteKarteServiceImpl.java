
package open.dolphin.ejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import open.dolphin.exception.CanNotDeleteException;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;

import open.dolphin.dto.AppointSpec;
import open.dolphin.dto.DiagnosisSearchSpec;
import open.dolphin.dto.DocumentSearchSpec;
import open.dolphin.dto.ImageSearchSpec;
import open.dolphin.dto.ModuleSearchSpec;
import open.dolphin.dto.ObservationSearchSpec;
import open.dolphin.infomodel.AllergyModel;
import open.dolphin.infomodel.AppointmentModel;
import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.KarteBean;
import open.dolphin.infomodel.LetterModel;
import open.dolphin.infomodel.ModelUtils;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.ObservationModel;
import open.dolphin.infomodel.PatientMemoModel;
import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.infomodel.PhysicalModel;
import open.dolphin.infomodel.RegisteredDiagnosisModel;
import open.dolphin.infomodel.SchemaModel;

@Stateless
@SecurityDomain("openDolphin")
@RolesAllowed("user")
@Remote({RemoteKarteService.class})
@RemoteBinding(jndiBinding="openDolphin/RemoteKarteService")
public class RemoteKarteServiceImpl extends DolphinService implements RemoteKarteService {
    
    //@Resource
    //private SessionContext sctx;
    
    @PersistenceContext
    private EntityManager em;
    
    /**
     * �J���e�̊�b�I�ȏ����܂Ƃ߂ĕԂ��B
     * @param patientPk ���҂� Database Primary Key
     * @param fromDate �e��G���g���̌����J�n��
     * @return ��b�I�ȏ����t�F�b�`���� KarteBean
     */
    public KarteBean getKarte(long patientPk, Date fromDate) {
        
        try {
            // �ŏ��Ɋ��҂̃J���e���擾����
            KarteBean karte = (KarteBean) em.createQuery("from KarteBean k where k.patient.id = :patientPk")
            .setParameter("patientPk", patientPk)
            .getSingleResult();
            
            // �J���e�� PK �𓾂�
            long karteId = karte.getId();
            
            // �A�����M�[�f�[�^���擾����
            List observations = em.createQuery("from ObservationModel o where o.karte.id=:karteId and o.observation='Allergy'")
            .setParameter("karteId", karteId)
            .getResultList();
            if (observations != null && observations.size() > 0) {
                List<AllergyModel> allergies = new ArrayList<AllergyModel>(observations.size());
                for (Iterator iter = observations.iterator(); iter.hasNext(); ) {
                    ObservationModel observation = (ObservationModel) iter.next();
                    AllergyModel allergy = new AllergyModel();
                    allergy.setObservationId(observation.getId());
                    allergy.setFactor(observation.getPhenomenon());
                    allergy.setSeverity(observation.getCategoryValue());
                    allergy.setIdentifiedDate(observation.confirmDateAsString());
                    allergies.add(allergy);
                }
                karte.addEntryCollection("allergy", allergies);
            }
            
            // �g���f�[�^���擾����
            observations = em.createQuery("from ObservationModel o where o.karte.id=:karteId and o.observation='PhysicalExam' and o.phenomenon='bodyHeight'")
            .setParameter("karteId", karteId)
            .getResultList();
            if (observations != null && observations.size() > 0) {
                List<PhysicalModel> physicals = new ArrayList<PhysicalModel>(observations.size());
                for (Iterator iter = observations.iterator(); iter.hasNext(); ) {
                    ObservationModel observation = (ObservationModel) iter.next();
                    PhysicalModel physical = new PhysicalModel();
                    physical.setHeightId(observation.getId());
                    physical.setHeight(observation.getValue());
                    physical.setIdentifiedDate(observation.confirmDateAsString());
                    physical.setMemo(ModelUtils.getDateAsString(observation.getRecorded()));
                    physicals.add(physical);
                }
                karte.addEntryCollection("height", physicals);
            }
            
            // �̏d�f�[�^���擾����
            observations = em.createQuery("from ObservationModel o where o.karte.id=:karteId and o.observation='PhysicalExam' and o.phenomenon='bodyWeight'")
            .setParameter("karteId", karteId)
            .getResultList();
            if (observations != null && observations.size() > 0) {
                List<PhysicalModel> physicals = new ArrayList<PhysicalModel>(observations.size());
                for (Iterator iter = observations.iterator(); iter.hasNext(); ) {
                    ObservationModel observation = (ObservationModel) iter.next();
                    PhysicalModel physical = new PhysicalModel();
                    physical.setWeightId(observation.getId());
                    physical.setWeight(observation.getValue());
                    physical.setIdentifiedDate(observation.confirmDateAsString());
                    physical.setMemo(ModelUtils.getDateAsString(observation.getRecorded()));
                    physicals.add(physical);
                }
                karte.addEntryCollection("weight", physicals);
            }
            
            // ���߂̗��@���G���g���[���擾���J���e�ɐݒ肷��
            List latestVisits = em.createQuery("from PatientVisitModel p where p.patient.id = :patientPk and p.pvtDate >= :fromDate")
            .setParameter("patientPk", patientPk)
            .setParameter("fromDate", ModelUtils.getDateAsString(fromDate))
            .getResultList();
            
            if (latestVisits != null && latestVisits.size() > 0) {
                List<String> visits = new ArrayList<String>();
                for (Iterator iter=latestVisits.iterator(); iter.hasNext() ;) {
                    PatientVisitModel bean = (PatientVisitModel) iter.next();
                    visits.add(bean.getPvtDate());
                }
                karte.addEntryCollection("visit", visits);
            }
            
            // ���������G���g���[���擾���J���e�ɐݒ肷��
            List documents = em.createQuery("from DocumentModel d where d.karte.id = :karteId and d.started >= :fromDate and (d.status='F' or d.status='T')")
            .setParameter("karteId", karteId)
            .setParameter("fromDate", fromDate)
            .getResultList();
            
            if (documents != null && documents.size() > 0) {
                List<DocInfoModel> c = new ArrayList<DocInfoModel>();
                for (Iterator iter = documents.iterator(); iter.hasNext() ;) {
                    DocumentModel docBean = (DocumentModel) iter.next();
                    docBean.toDetuch();
                    c.add(docBean.getDocInfo());
                }
                karte.addEntryCollection("docInfo", c);
            }
            
            // ����Memo���擾����
            List memo = em.createQuery("from PatientMemoModel p where p.karte.id = :karteId")
            .setParameter("karteId", karteId)
            .getResultList();
            if (memo != null && memo.size() >0) {
                karte.addEntryCollection("patientMemo", memo);
            }
            
            return karte;
            
        } catch (NoResultException e) {
            // ���ғo�^�̍ۂɃJ���e���������Ă���
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * ���������G���g�����擾����B
     * @param karteId �J���eId
     * @param fromDate �擾�J�n��
     * @param status �X�e�[�^�X
     * @return DocInfo �̃R���N�V����
     */
    public List getDocumentList(DocumentSearchSpec spec) {
        
        List documents = null;
        
        if (spec.isIncludeModifid()) {
            documents = em.createQuery("from DocumentModel d where d.karte.id = :karteId and d.started >= :fromDate and d.status !='D'")
            .setParameter("karteId", spec.getKarteId())
            .setParameter("fromDate", spec.getFromDate())
            .getResultList();
        } else {
            documents = em.createQuery("from DocumentModel d where d.karte.id = :karteId and d.started >= :fromDate and (d.status='F' or d.status='T')")
            .setParameter("karteId", spec.getKarteId())
            .setParameter("fromDate", spec.getFromDate())
            .getResultList();
        }
        
        List<DocInfoModel> result = new ArrayList<DocInfoModel>();
        for (Iterator iter = documents.iterator(); iter.hasNext() ;) {
            DocumentModel docBean = (DocumentModel) iter.next();
            // ���f������DocInfo �֕K�v�ȃf�[�^���ڂ�
            // �N���C�A���g�� DocInfo �����𗘗p����P�[�X�����邽��
            docBean.toDetuch();
            result.add(docBean.getDocInfo());
        }
        return result;
    }
    
    /**
     * ����(DocumentModel Object)���擾����B
     * @param ids DocumentModel �� pk�R���N�V����
     * @return DocumentModel�̃R���N�V����
     */
    @SuppressWarnings({ "unchecked", "unchecked" })
    public List<DocumentModel> getDocuments(List<Long> ids) {
        
        List<DocumentModel> ret = new ArrayList<DocumentModel>(3);
        
        // ���[�v����
        for (Long id : ids) {
            
            // DocuentBean ���擾����
            DocumentModel document = (DocumentModel) em.find(DocumentModel.class, id);
            
            // ModuleBean ���擾����
            List modules = em.createQuery("from ModuleModel m where m.document.id = :id")
            .setParameter("id", id)
            .getResultList();
            document.setModules(modules);
            
            // SchemaModel ���擾����
            List images = em.createQuery("from SchemaModel i where i.document.id = :id")
            .setParameter("id", id)
            .getResultList();
            document.setSchema(images);
            
            ret.add(document);
        }
        
        return ret;
    }
    
    /**
     * �h�L�������g DocumentModel �I�u�W�F�N�g��ۑ�����B
     * @param karteId �J���eId
     * @param document �ǉ�����DocumentModel �I�u�W�F�N�g
     * @return �ǉ�������
     */
    public long addDocument(DocumentModel document) {
        
        // �i��������
        em.persist(document);
        
        // ID
        long id = document.getId();
        
        // �C���ł̏������s��
        long parentPk = document.getDocInfo().getParentPk();
        
        if (parentPk != 0L) {
            
            // �K���I������V�����ł̊m����ɂ���
            Date ended = document.getConfirmed();
            
            // �I���W�i�����擾�� �I������ status = M ��ݒ肷��
            DocumentModel old = (DocumentModel) em.find(DocumentModel.class, parentPk);
            old.setEnded(ended);
            old.setStatus(STATUS_MODIFIED);
            
            // �֘A���郂�W���[���ƃC���[�W�ɓ������������s����
            Collection oldModules = em.createQuery("from ModuleModel m where m.document.id = :id")
            .setParameter("id", parentPk).getResultList();
            for (Iterator iter = oldModules.iterator(); iter.hasNext(); ) {
                ModuleModel model = (ModuleModel)iter.next();
                model.setEnded(ended);
                model.setStatus(STATUS_MODIFIED);
            }
            
            Collection oldImages = em.createQuery("from SchemaModel s where s.document.id = :id")
            .setParameter("id", parentPk).getResultList();
            for (Iterator iter = oldImages.iterator(); iter.hasNext(); ) {
                SchemaModel model = (SchemaModel)iter.next();
                model.setEnded(ended);
                model.setStatus(STATUS_MODIFIED);
            }
        }
        
        return id;
    }
    
    /**
     * �h�L�������g��_���폜����B
     * @param pk �_���폜����h�L�������g�� primary key
     * @return �폜��������
     */
    public int deleteDocument(long pk) {
        
        //
        // �Ώ� Document ���擾����
        //
        Date ended = new Date();
        DocumentModel delete = (DocumentModel) em.find(DocumentModel.class, pk);
        
        //
        // �Q�Ƃ��Ă���ꍇ�͗�O�𓊂���
        //
        if (delete.getLinkId() != 0L) {
            throw new CanNotDeleteException("���̃h�L�������g���Q�Ƃ��Ă��邽�ߍ폜�ł��܂���B");
        }
        
        //
        // �Q�Ƃ���Ă���ꍇ�͗�O�𓊂���
        //
        Collection refs = em.createQuery("from DocumentModel d where d.linkId=:pk")
        .setParameter("pk", pk).getResultList();
        if (refs != null && refs.size() >0) {
            CanNotDeleteException ce = new CanNotDeleteException("���̃h�L�������g����Q�Ƃ���Ă��邽�ߍ폜�ł��܂���B");
            throw ce;
        }
        
        //
        // �P�ƃ��R�[�h�Ȃ̂ō폜�t���O�����Ă�
        //
        delete.setStatus(STATUS_DELETE);
        delete.setEnded(ended);
        
        //
        // �֘A���郂�W���[���ɓ����������s��
        //
        Collection deleteModules = em.createQuery("from ModuleModel m where m.document.id = :pk")
        .setParameter("pk", pk).getResultList();
        for (Iterator iter = deleteModules.iterator(); iter.hasNext(); ) {
            ModuleModel model = (ModuleModel) iter.next();
            model.setStatus(STATUS_DELETE);
            model.setEnded(ended);
        }
        
        //
        // �֘A����摜�ɓ����������s��
        //
        Collection deleteImages = em.createQuery("from SchemaModel s where s.document.id = :pk")
        .setParameter("pk", pk).getResultList();
        for (Iterator iter = deleteImages.iterator(); iter.hasNext(); ) {
            SchemaModel model = (SchemaModel) iter.next();
            model.setStatus(STATUS_DELETE);
            model.setEnded(ended);
        }
        
        return 1;
    }
    
    /**
     * �h�L�������g�̃^�C�g����ύX����B
     * @param pk �ύX����h�L�������g�� primary key
     * @return �ύX��������
     */
    public int updateTitle(long pk, String title) {
        
        DocumentModel update = (DocumentModel) em.find(DocumentModel.class, pk);
        update.getDocInfo().setTitle(title);
        return 1;
    }
    
    /**
     * ModuleModel�G���g�����擾����B
     * @param spec ���W���[�������d�l
     * @return ModuleModel���X�g�̃��X�g
     */
    public List<List> getModules(ModuleSearchSpec spec) {
        
        // ���o���Ԃ͕ʂ����Ă���
        Date[] fromDate = spec.getFromDate();
        Date[] toDate = spec.getToDate();
        int len = fromDate.length;
        List<List> ret = new ArrayList<List>(len);
        
        // ���o���ԃZ�b�g�̐������J��Ԃ�
        for (int i = 0; i < len; i++) {
            
            List modules
                    = em.createQuery("from ModuleModel m where m.karte.id = :karteId and m.moduleInfo.entity = :entity and m.started between :fromDate and :toDate and m.status='F'")
                    .setParameter("karteId", spec.getKarteId())
                    .setParameter("entity", spec.getEntity())
                    .setParameter("fromDate", fromDate[i])
                    .setParameter("toDate", toDate[i])
                    .getResultList();
            
            ret.add(modules);
        }
        
        return ret;
    }
    
    /**
     * SchemaModel�G���g�����擾����B
     * @param karteId �J���eID
     * @param fromDate
     * @param toDate
     * @return SchemaModel�G���g���̔z��
     */
    @SuppressWarnings("unchecked")
    public List<List> getImages(ImageSearchSpec spec) {
        
        // ���o���Ԃ͕ʂ����Ă���
        Date[] fromDate = spec.getFromDate();
        Date[] toDate = spec.getToDate();
        int len = fromDate.length;
        List<List> ret = new ArrayList<List>(len);
        
        // ���o���ԃZ�b�g�̐������J��Ԃ�
        for (int i = 0; i < len; i++) {
            
            List modules
                    = em.createQuery("from SchemaModel i where i.karte.id = :karteId and i.started between :fromDate and :toDate and i.status='F'")
                    .setParameter("karteId", spec.getKarteId())
                    .setParameter("fromDate", fromDate[i])
                    .setParameter("toDate", toDate[i])
                    .getResultList();
            
            ret.add(modules);
        }
        
        return ret;
    }
    
    /**
     * �摜���擾����B
     * @param id SchemaModel Id
     * @return SchemaModel
     */
    public SchemaModel getImage(long id) {
        
        SchemaModel image = (SchemaModel)em.find(SchemaModel.class, id);
        return image;
    }
    
    /**
     * ���a�����X�g���擾����B
     * @param spec �����d�l
     * @return ���a���̃��X�g
     */
    @SuppressWarnings("unchecked")
    public List<RegisteredDiagnosisModel> getDiagnosis(DiagnosisSearchSpec spec) {
        
        List ret = null;
        
        // �����J�n�����w�肵�Ă���
        if (spec.getFromDate() != null) {
            ret = em.createQuery("from RegisteredDiagnosisModel r where r.karte.id = :karteId and r.started >= :fromDate")
                    .setParameter("karteId", spec.getKarteId())
                    .setParameter("fromDate", spec.getFromDate())
                    .getResultList();
        } else {
            // �S���Ԃ̏��a���𓾂�
            ret = em.createQuery("from RegisteredDiagnosisModel r where r.karte.id = :karteId")
                    .setParameter("karteId", spec.getKarteId())
                    .getResultList();
        } 
        
        return ret;
    }
    
    /**
     * ���a����ǉ�����B
     * @param addList �ǉ����鏝�a���̃��X�g
     * @return id�̃��X�g
     */
    public List<Long> addDiagnosis(List<RegisteredDiagnosisModel> addList) {
        
        List<Long> ret = new ArrayList<Long>(addList.size());
        
        for (RegisteredDiagnosisModel bean : addList) {
            em.persist(bean);
            ret.add(new Long(bean.getId()));
        }
        
        return ret;
    }
    
    /**
     * ���a�����X�V����B
     * @param updateList
     * @return �X�V��
     */
    public int updateDiagnosis(List<RegisteredDiagnosisModel> updateList) {
        
        int cnt = 0;
        
        for (RegisteredDiagnosisModel bean : updateList) {
            em.merge(bean);
            cnt++;
        }
        
        return cnt;
    }
    
    /**
     * ���a�����폜����B
     * @param removeList �폜���鏝�a����id���X�g
     * @return �폜��
     */
    public int removeDiagnosis(List<Long> removeList) {
        
        int cnt = 0;
        
        for (Long id : removeList) {
            RegisteredDiagnosisModel bean = (RegisteredDiagnosisModel) em.find(RegisteredDiagnosisModel.class, id);
            em.remove(bean);
            cnt++;
        }
        
        return cnt;
    }
    
    /**
     * Observation���擾����B
     * @param spec �����d�l
     * @return Observation�̃��X�g
     */
    @SuppressWarnings("unchecked")
    public List<ObservationModel> getObservations(ObservationSearchSpec spec) {
        
        List ret = null;
        String observation = spec.getObservation();
        String phenomenon = spec.getPhenomenon();
        Date firstConfirmed = spec.getFirstConfirmed();
        
        if (observation != null) {
            if (firstConfirmed != null) {
                ret = em.createQuery("from ObservationModel o where o.karte.id=:karteId and o.observation=:observation and o.started >= :firstConfirmed")
                .setParameter("karteId", spec.getKarteId())
                .setParameter("observation", observation)
                .setParameter("firstConfirmed", firstConfirmed)
                .getResultList();
                
            } else {
                ret = em.createQuery("from ObservationModel o where o.karte.id=:karteId and o.observation=:observation")
                .setParameter("karteId", spec.getKarteId())
                .setParameter("observation", observation)
                .getResultList();
            }
        } else if (phenomenon != null) {
            if (firstConfirmed != null) {
                ret = em.createQuery("from ObservationModel o where o.karte.id=:karteId and o.phenomenon=:phenomenon and o.started >= :firstConfirmed")
                .setParameter("karteId", spec.getKarteId())
                .setParameter("phenomenon", phenomenon)
                .setParameter("firstConfirmed", firstConfirmed)
                .getResultList();
            } else {
                ret = em.createQuery("from ObservationModel o where o.karte.id=:karteId and o.phenomenon=:phenomenon")
                .setParameter("karteId", spec.getKarteId())
                .setParameter("phenomenon", phenomenon)
                .getResultList();
            }
        }
        return ret;
    }
    
    /**
     * Observation��ǉ�����B
     * @param observations �ǉ�����Observation�̃��X�g
     * @return �ǉ�����Observation��Id���X�g
     */
    public List<Long> addObservations(List<ObservationModel> observations) {
        
        if (observations != null && observations.size() > 0) {
            
            List<Long> ret = new ArrayList<Long>(observations.size());
            
            for (ObservationModel model : observations) {
                em.persist(model);
                ret.add(new Long(model.getId()));
            }
            
            return ret;
        }
        return null;
    }
    
    /**
     * Observation���X�V����B
     * @param observations �X�V����Observation�̃��X�g
     * @return �X�V������
     */
    public int updateObservations(List<ObservationModel> observations) {
        
        if (observations != null && observations.size() > 0) {
            int cnt = 0;
            for (ObservationModel model : observations) {
                em.merge(model);
                cnt++;
            }
            return cnt;
        }
        return 0;
    }
    
    /**
     * Observation���폜����B
     * @param observations �폜����Observation�̃��X�g
     * @return �폜������
     */
    public int removeObservations(List<Long> observations) {
        if (observations != null && observations.size() > 0) {
            int cnt = 0;
            for (Long id : observations) {
                ObservationModel model = (ObservationModel) em.find(ObservationModel.class, id);
                em.remove(model);
                cnt++;
            }
            return cnt;
        }
        return 0;
    }
    
    /**
     * ���҃������X�V����B
     * @param memo �X�V���郁��
     */
    public int updatePatientMemo(PatientMemoModel memo) {
        
        int cnt = 0;
        
        if (memo.getId() == 0L) {
            em.persist(memo);
        } else {
            em.merge(memo);
        }
        cnt++;
        return cnt;
    }
    
    /**
     * �\���ۑ��A�X�V�A�폜����B
     * @param spec �\����� DTO
     */
    public int putAppointments(AppointSpec spec) {
        
        int cnt = 0;
        
        Collection added = spec.getAdded();
        Collection updated = spec.getUpdared();
        Collection removed = spec.getRemoved();
        AppointmentModel bean = null;
        
        // �o�^����
        if (added != null && added.size() > 0 ) {
            Iterator iter = added.iterator();
            while(iter.hasNext()) {
                bean = (AppointmentModel)iter.next();
                em.persist(bean);
                cnt++;
            }
        }
        
        // �X�V����
        if (updated != null && updated.size() > 0 ) {
            Iterator iter = updated.iterator();
            while(iter.hasNext()) {
                bean = (AppointmentModel)iter.next();
                // av �͕����I�u�W�F�N�g�ł���
                em.merge(bean);
                cnt++;
            }
        }
        
        // �폜
        if (removed != null && removed.size() > 0 ) {
            Iterator iter = removed.iterator();
            while(iter.hasNext()) {
                bean = (AppointmentModel)iter.next();
                // �����I�u�W�F�N�g�� remove �ɓn���Ȃ��̂őΏۂ���������
                AppointmentModel target = (AppointmentModel)em.find(AppointmentModel.class, bean.getId());
                em.remove(target);
                cnt++;
            }
        }
        
        return cnt;
    }
    
    /**
     * �\�����������B
     * @param spec �����d�l
     * @return �\��� Collection
     */
    public List<List> getAppointmentList(ModuleSearchSpec spec) {
        
        // ���o���Ԃ͕ʂ����Ă���
        Date[] fromDate = spec.getFromDate();
        Date[] toDate = spec.getToDate();
        int len = fromDate.length;
        List<List> ret = new ArrayList<List>(len);
        
        // ���o���Ԃ��ƂɌ������R���N�V�����ɉ�����
        for (int i = 0; i < len; i++) {
            
            List c = em.createQuery("from AppointmentModel a where a.karte.id = :karteId and a.date between :fromDate and :toDate")
            .setParameter("karteId", spec.getKarteId())
            .setParameter("fromDate", fromDate[i])
            .setParameter("toDate", toDate[i])
            .getResultList();
            ret.add(c);
        }
        
        return ret;
    }
    
    /**
     * �Љ���ۑ��܂��͍X�V����B
     */
    public long saveOrUpdateLetter(LetterModel model) {
        LetterModel saveOrUpdate = em.merge(model);
        return saveOrUpdate.getId();
    }
    
    /**
     * �Љ��̃��X�g���擾����B
     */
    public List<LetterModel> getLetterList(long karteId, String docType) {
        
        if (docType.equals("TOUTOU")) {
            List<LetterModel> ret = (List<LetterModel>)
                        em.createQuery("from TouTouLetter f where f.karte.id = :karteId")
                        .setParameter("karteId", karteId)
                        .getResultList();
            return ret;
            
        } else if (docType.equals("TOUTOU_REPLY")) {
            List<LetterModel> ret = (List<LetterModel>)
                        em.createQuery("from TouTouReply f where f.karte.id = :karteId")
                        .setParameter("karteId", karteId)
                        .getResultList();
            return ret;   
        }
        
        return null;
    }
    
    /**
     * �Љ����擾����B
     */
    public LetterModel getLetter(long letterPk) {
        
        LetterModel ret = (LetterModel)
                        em.createQuery("from TouTouLetter t where t.id = :id")
                        .setParameter("id", letterPk)
                        .getSingleResult();
        return ret;
    }
    
    public LetterModel getLetterReply(long letterPk) {
        
        LetterModel ret = (LetterModel)
                        em.createQuery("from TouTouReply t where t.id = :id")
                        .setParameter("id", letterPk)
                        .getSingleResult();
        return ret;
    }
}






















