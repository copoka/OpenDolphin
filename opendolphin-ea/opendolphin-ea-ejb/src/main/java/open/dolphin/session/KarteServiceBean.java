package open.dolphin.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import open.dolphin.infomodel.AllergyModel;
import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.KarteBean;
import open.dolphin.infomodel.LetterModel;
import open.dolphin.infomodel.ModelUtils;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.ObservationModel;
import open.dolphin.infomodel.PatientMemoModel;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.infomodel.PhysicalModel;
import open.dolphin.infomodel.RegisteredDiagnosisModel;
import open.dolphin.infomodel.SchemaModel;

/**
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
@Stateless
public class KarteServiceBean implements KarteServiceBeanLocal {

    //private static final String QUERY_PATIENT_BY_FIDPID = "from PatientModel p where p.facilityId=:fid and p.patientId=:pid";

    private static final String PATIENT_PK = "patientPk";
    private static final String KARTE_ID = "karteId";
    private static final String FROM_DATE = "fromDate";
    private static final String TO_DATE = "toDate";
    private static final String ID = "id";
    private static final String ENTITY = "entity";
    private static final String FID = "fid";
    private static final String PID = "pid";

    private static final String QUERY_KARTE = "from KarteBean k where k.patient.id=:patientPk";
    private static final String QUERY_ALLERGY = "from ObservationModel o where o.karte.id=:karteId and o.observation='Allergy'";
    private static final String QUERY_BODY_HEIGHT = "from ObservationModel o where o.karte.id=:karteId and o.observation='PhysicalExam' and o.phenomenon='bodyHeight'";
    private static final String QUERY_BODY_WEIGHT = "from ObservationModel o where o.karte.id=:karteId and o.observation='PhysicalExam' and o.phenomenon='bodyWeight'";
    private static final String QUERY_PATIENT_VISIT = "from PatientVisitModel p where p.patient.id=:patientPk and p.pvtDate >= :fromDate";
    private static final String QUERY_DOC_INFO = "from DocumentModel d where d.karte.id=:karteId and d.started >= :fromDate and (d.status='F' or d.status='T')";
    private static final String QUERY_PATIENT_MEMO = "from PatientMemoModel p where p.karte.id=:karteId";

    private static final String QUERY_DOCUMENT_INCLUDE_MODIFIED = "from DocumentModel d where d.karte.id=:karteId and d.started >= :fromDate and d.status !='D'";
    private static final String QUERY_DOCUMENT = "from DocumentModel d where d.karte.id=:karteId and d.started >= :fromDate and (d.status='F' or d.status='T')";
    private static final String QUERY_DOCUMENT_BY_LINK_ID = "from DocumentModel d where d.linkId=:id";

    private static final String QUERY_MODULE_BY_DOC_ID = "from ModuleModel m where m.document.id=:id";
    private static final String QUERY_SCHEMA_BY_DOC_ID = "from SchemaModel i where i.document.id=:id";
    private static final String QUERY_MODULE_BY_ENTITY = "from ModuleModel m where m.karte.id=:karteId and m.moduleInfo.entity=:entity and m.started between :fromDate and :toDate and m.status='F'";
    private static final String QUERY_SCHEMA_BY_KARTE_ID = "from SchemaModel i where i.karte.id =:karteId and i.started between :fromDate and :toDate and i.status='F'";

    private static final String QUERY_SCHEMA_BY_FACILITY_ID = "from SchemaModel i where i.karte.patient.facilityId like :fid and i.extRef.sop is not null and i.status='F'";

    private static final String QUERY_DIAGNOSIS_BY_KARTE_DATE = "from RegisteredDiagnosisModel r where r.karte.id=:karteId and r.started >= :fromDate";
    private static final String QUERY_DIAGNOSIS_BY_KARTE_DATE_ACTIVEONLY = "from RegisteredDiagnosisModel r where r.karte.id=:karteId and r.started >= :fromDate and r.ended is NULL";
    private static final String QUERY_DIAGNOSIS_BY_KARTE = "from RegisteredDiagnosisModel r where r.karte.id=:karteId";
    private static final String QUERY_DIAGNOSIS_BY_KARTE_ACTIVEONLY = "from RegisteredDiagnosisModel r where r.karte.id=:karteId and r.ended is NULL";

    private static final String TOUTOU = "TOUTOU";
    private static final String TOUTOU_REPLY = "TOUTOU_REPLY";
    private static final String QUERY_LETTER_BY_KARTE_ID = "from TouTouLetter f where f.karte.id=:karteId";
    private static final String QUERY_REPLY_BY_KARTE_ID = "from TouTouReply f where f.karte.id=:karteId";
    private static final String QUERY_LETTER_BY_ID = "from TouTouLetter t where t.id=:id";
    private static final String QUERY_REPLY_BY_ID = "from TouTouReply t where t.id=:id";

    private static final String QUERY_APPO_BY_KARTE_ID_PERIOD = "from AppointmentModel a where a.karte.id = :karteId and a.date between :fromDate and :toDate";

    //private static final String QUERY_PVT_BY_ID = "from PatientVisitModel p where p.id=id";
    
    private static final String QUERY_PATIENT_BY_FID_PID = "from PatientModel p where p.facilityId=:fid and p.patientId=:pid";

    @PersistenceContext
    private EntityManager em;
    
    @Override
    public KarteBean getKarte(String fid, String pid, Date fromDate) {
        
        try {
            
            // ���҃��R�[�h�� FacilityId �� patientId �ŕ����L�[�ɂȂ��Ă���
            PatientModel patient
                = (PatientModel)em.createQuery(QUERY_PATIENT_BY_FID_PID)
                .setParameter(FID, fid)
                .setParameter(PID, pid)
                .getSingleResult();

            long patientPK = patient.getId();
            
            // �ŏ��Ɋ��҂̃J���e���擾����
            List<KarteBean> kartes = em.createQuery(QUERY_KARTE)
                                  .setParameter(PATIENT_PK, patientPK)
                                  .getResultList();
            KarteBean karte = kartes.get(0);

            // �J���e�� PK �𓾂�
            long karteId = karte.getId();

            // �A�����M�[�f�[�^���擾����
            List<ObservationModel> list1 =
                    (List<ObservationModel>)em.createQuery(QUERY_ALLERGY)
                                              .setParameter(KARTE_ID, karteId)
                                              .getResultList();
            if (!list1.isEmpty()) {
                List<AllergyModel> allergies = new ArrayList<AllergyModel>(list1.size());
                for (ObservationModel observation : list1) {
                    AllergyModel allergy = new AllergyModel();
                    allergy.setObservationId(observation.getId());
                    allergy.setFactor(observation.getPhenomenon());
                    allergy.setSeverity(observation.getCategoryValue());
                    allergy.setIdentifiedDate(observation.confirmDateAsString());
                    allergy.setMemo(observation.getMemo());
                    allergies.add(allergy);
                }
                karte.setAllergies(allergies);
            }

            // �g���f�[�^���擾����
            List<ObservationModel> list2 =
                    (List<ObservationModel>)em.createQuery(QUERY_BODY_HEIGHT)
                                              .setParameter(KARTE_ID, karteId)
                                              .getResultList();
            if (!list2.isEmpty()) {
                List<PhysicalModel> physicals = new ArrayList<PhysicalModel>(list2.size());
                for (ObservationModel observation : list2) {
                    PhysicalModel physical = new PhysicalModel();
                    physical.setHeightId(observation.getId());
                    physical.setHeight(observation.getValue());
                    physical.setIdentifiedDate(observation.confirmDateAsString());
                    physical.setMemo(ModelUtils.getDateAsString(observation.getRecorded()));
                    physicals.add(physical);
                }
                karte.setHeights(physicals);
            }

            // �̏d�f�[�^���擾����
            List<ObservationModel> list3 =
                    (List<ObservationModel>)em.createQuery(QUERY_BODY_WEIGHT)
                                              .setParameter(KARTE_ID, karteId)
                                              .getResultList();
            if (!list3.isEmpty()) {
                List<PhysicalModel> physicals = new ArrayList<PhysicalModel>(list3.size());
                for (ObservationModel observation : list3) {
                    PhysicalModel physical = new PhysicalModel();
                    physical.setWeightId(observation.getId());
                    physical.setWeight(observation.getValue());
                    physical.setIdentifiedDate(observation.confirmDateAsString());
                    physical.setMemo(ModelUtils.getDateAsString(observation.getRecorded()));
                    physicals.add(physical);
                }
                karte.setWeights(physicals);
            }

            // ���߂̗��@���G���g���[���擾���J���e�ɐݒ肷��
            List<PatientVisitModel> latestVisits =
                    (List<PatientVisitModel>)em.createQuery(QUERY_PATIENT_VISIT)
                                               .setParameter(PATIENT_PK, patientPK)
                                               .setParameter(FROM_DATE, ModelUtils.getDateAsString(fromDate))
                                               .getResultList();

            if (!latestVisits.isEmpty()) {
                List<String> visits = new ArrayList<String>(latestVisits.size());
                for (PatientVisitModel bean : latestVisits) {
                    // ���@���݂̂��g�p����
                    visits.add(bean.getPvtDate());
                }
                karte.setPatientVisits(visits);
            }

            // ���������G���g���[���擾���J���e�ɐݒ肷��
            List<DocumentModel> documents =
                    (List<DocumentModel>)em.createQuery(QUERY_DOC_INFO)
                                           .setParameter(KARTE_ID, karteId)
                                           .setParameter(FROM_DATE, fromDate)
                                           .getResultList();

            if (!documents.isEmpty()) {
                List<DocInfoModel> c = new ArrayList<DocInfoModel>(documents.size());
                for (DocumentModel docBean : documents) {
                    docBean.toDetuch();
                    c.add(docBean.getDocInfoModel());
                }
                karte.setDocInfoList(c);
            }

            // ����Memo���擾����
            List<PatientMemoModel> memo =
                    (List<PatientMemoModel>)em.createQuery(QUERY_PATIENT_MEMO)
                                              .setParameter(KARTE_ID, karteId)
                                              .getResultList();
            if (!memo.isEmpty()) {
                karte.setMemoList(memo);
            }

            return karte;
        
            
        } catch (Exception e) {
            
        }
        
        return null;
    }

    /**
     * �J���e�̊�b�I�ȏ����܂Ƃ߂ĕԂ��B
     * @param patientPk ���҂� Database Primary Key
     * @param fromDate �e��G���g���̌����J�n��
     * @return ��b�I�ȏ����t�F�b�`���� KarteBean
     */
    @Override
    public KarteBean getKarte(long patientPK, Date fromDate) {

        try {
            // �ŏ��Ɋ��҂̃J���e���擾����
            List<KarteBean> kartes = em.createQuery(QUERY_KARTE)
                                  .setParameter(PATIENT_PK, patientPK)
                                  .getResultList();
            KarteBean karte = kartes.get(0);

            // �J���e�� PK �𓾂�
            long karteId = karte.getId();

            // �A�����M�[�f�[�^���擾����
            List<ObservationModel> list1 =
                    (List<ObservationModel>)em.createQuery(QUERY_ALLERGY)
                                              .setParameter(KARTE_ID, karteId)
                                              .getResultList();
            if (!list1.isEmpty()) {
                List<AllergyModel> allergies = new ArrayList<AllergyModel>(list1.size());
                for (ObservationModel observation : list1) {
                    AllergyModel allergy = new AllergyModel();
                    allergy.setObservationId(observation.getId());
                    allergy.setFactor(observation.getPhenomenon());
                    allergy.setSeverity(observation.getCategoryValue());
                    allergy.setIdentifiedDate(observation.confirmDateAsString());
                    allergy.setMemo(observation.getMemo());
                    allergies.add(allergy);
                }
                karte.setAllergies(allergies);
            }

            // �g���f�[�^���擾����
            List<ObservationModel> list2 =
                    (List<ObservationModel>)em.createQuery(QUERY_BODY_HEIGHT)
                                              .setParameter(KARTE_ID, karteId)
                                              .getResultList();
            if (!list2.isEmpty()) {
                List<PhysicalModel> physicals = new ArrayList<PhysicalModel>(list2.size());
                for (ObservationModel observation : list2) {
                    PhysicalModel physical = new PhysicalModel();
                    physical.setHeightId(observation.getId());
                    physical.setHeight(observation.getValue());
                    physical.setIdentifiedDate(observation.confirmDateAsString());
                    physical.setMemo(ModelUtils.getDateAsString(observation.getRecorded()));
                    physicals.add(physical);
                }
                karte.setHeights(physicals);
            }

            // �̏d�f�[�^���擾����
            List<ObservationModel> list3 =
                    (List<ObservationModel>)em.createQuery(QUERY_BODY_WEIGHT)
                                              .setParameter(KARTE_ID, karteId)
                                              .getResultList();
            if (!list3.isEmpty()) {
                List<PhysicalModel> physicals = new ArrayList<PhysicalModel>(list3.size());
                for (ObservationModel observation : list3) {
                    PhysicalModel physical = new PhysicalModel();
                    physical.setWeightId(observation.getId());
                    physical.setWeight(observation.getValue());
                    physical.setIdentifiedDate(observation.confirmDateAsString());
                    physical.setMemo(ModelUtils.getDateAsString(observation.getRecorded()));
                    physicals.add(physical);
                }
                karte.setWeights(physicals);
            }

            // ���߂̗��@���G���g���[���擾���J���e�ɐݒ肷��
            List<PatientVisitModel> latestVisits =
                    (List<PatientVisitModel>)em.createQuery(QUERY_PATIENT_VISIT)
                                               .setParameter(PATIENT_PK, patientPK)
                                               .setParameter(FROM_DATE, ModelUtils.getDateAsString(fromDate))
                                               .getResultList();

            if (!latestVisits.isEmpty()) {
                List<String> visits = new ArrayList<String>(latestVisits.size());
                for (PatientVisitModel bean : latestVisits) {
                    // ���@���݂̂��g�p����
                    visits.add(bean.getPvtDate());
                }
                karte.setPatientVisits(visits);
            }

            // ���������G���g���[���擾���J���e�ɐݒ肷��
            List<DocumentModel> documents =
                    (List<DocumentModel>)em.createQuery(QUERY_DOC_INFO)
                                           .setParameter(KARTE_ID, karteId)
                                           .setParameter(FROM_DATE, fromDate)
                                           .getResultList();

            if (!documents.isEmpty()) {
                List<DocInfoModel> c = new ArrayList<DocInfoModel>(documents.size());
                for (DocumentModel docBean : documents) {
                    docBean.toDetuch();
                    c.add(docBean.getDocInfoModel());
                }
                karte.setDocInfoList(c);
            }

            // ����Memo���擾����
            List<PatientMemoModel> memo =
                    (List<PatientMemoModel>)em.createQuery(QUERY_PATIENT_MEMO)
                                              .setParameter(KARTE_ID, karteId)
                                              .getResultList();
            if (!memo.isEmpty()) {
                karte.setMemoList(memo);
            }

            return karte;

        } catch (NoResultException e) {
            // ���ғo�^�̍ۂɃJ���e���������Ă���
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
    @Override
    public List<DocInfoModel> getDocumentList(long karteId, Date fromDate, boolean includeModifid) {

        List<DocumentModel> documents = null;

        if (includeModifid) {
            documents = (List<DocumentModel>)em.createQuery(QUERY_DOCUMENT_INCLUDE_MODIFIED)
            .setParameter(KARTE_ID, karteId)
            .setParameter(FROM_DATE, fromDate)
            .getResultList();
        } else {
            documents = (List<DocumentModel>)em.createQuery(QUERY_DOCUMENT)
            .setParameter(KARTE_ID, karteId)
            .setParameter(FROM_DATE, fromDate)
            .getResultList();
        }

        List<DocInfoModel> result = new ArrayList<DocInfoModel>();
        for (DocumentModel doc : documents) {
            // ���f������DocInfo �֕K�v�ȃf�[�^���ڂ�
            // �N���C�A���g�� DocInfo �����𗘗p����P�[�X�����邽��
            doc.toDetuch();
            result.add(doc.getDocInfoModel());
        }
        return result;
    }

    /**
     * ����(DocumentModel Object)���擾����B
     * @param ids DocumentModel �� pk�R���N�V����
     * @return DocumentModel�̃R���N�V����
     */
    @Override
    public List<DocumentModel> getDocuments(List<Long> ids) {

        List<DocumentModel> ret = new ArrayList<DocumentModel>(3);

        // ���[�v����
        for (Long id : ids) {

            // DocuentBean ���擾����
            DocumentModel document = (DocumentModel) em.find(DocumentModel.class, id);

            // ModuleBean ���擾����
            List modules = em.createQuery(QUERY_MODULE_BY_DOC_ID)
            .setParameter(ID, id)
            .getResultList();
            document.setModules(modules);

            // SchemaModel ���擾����
            List images = em.createQuery(QUERY_SCHEMA_BY_DOC_ID)
            .setParameter(ID, id)
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
    @Override
    public long addDocument(DocumentModel document) {

        // �i��������
        em.persist(document);

        // ID
        long id = document.getId();

        // �C���ł̏������s��
        long parentPk = document.getDocInfoModel().getParentPk();

        if (parentPk != 0L) {

            // �K���I������V�����ł̊m����ɂ���
            Date ended = document.getConfirmed();

            // �I���W�i�����擾�� �I������ status = M ��ݒ肷��
            DocumentModel old = (DocumentModel) em.find(DocumentModel.class, parentPk);
            old.setEnded(ended);
            old.setStatus(IInfoModel.STATUS_MODIFIED);

            // �֘A���郂�W���[���ƃC���[�W�ɓ������������s����
            Collection oldModules = em.createQuery(QUERY_MODULE_BY_DOC_ID)
            .setParameter(ID, parentPk).getResultList();
            for (Iterator iter = oldModules.iterator(); iter.hasNext(); ) {
                ModuleModel model = (ModuleModel)iter.next();
                model.setEnded(ended);
                model.setStatus(IInfoModel.STATUS_MODIFIED);
            }

            Collection oldImages = em.createQuery(QUERY_SCHEMA_BY_DOC_ID)
            .setParameter(ID, parentPk).getResultList();
            for (Iterator iter = oldImages.iterator(); iter.hasNext(); ) {
                SchemaModel model = (SchemaModel)iter.next();
                model.setEnded(ended);
                model.setStatus(IInfoModel.STATUS_MODIFIED);
            }
        }

        return id;
    }


    @Override
    public long addDocumentAndUpdatePVTState(DocumentModel document, long pvtPK, int state) {

        // �i��������
        em.persist(document);

        // ID
        long id = document.getId();

        // �C���ł̏������s��
        long parentPk = document.getDocInfoModel().getParentPk();

        if (parentPk != 0L) {

            // �K���I������V�����ł̊m����ɂ���
            Date ended = document.getConfirmed();

            // �I���W�i�����擾�� �I������ status = M ��ݒ肷��
            DocumentModel old = (DocumentModel) em.find(DocumentModel.class, parentPk);
            old.setEnded(ended);
            old.setStatus(IInfoModel.STATUS_MODIFIED);

            // �֘A���郂�W���[���ƃC���[�W�ɓ������������s����
            Collection oldModules = em.createQuery(QUERY_MODULE_BY_DOC_ID)
            .setParameter(ID, parentPk).getResultList();
            for (Iterator iter = oldModules.iterator(); iter.hasNext(); ) {
                ModuleModel model = (ModuleModel)iter.next();
                model.setEnded(ended);
                model.setStatus(IInfoModel.STATUS_MODIFIED);
            }

            Collection oldImages = em.createQuery(QUERY_SCHEMA_BY_DOC_ID)
            .setParameter(ID, parentPk).getResultList();
            for (Iterator iter = oldImages.iterator(); iter.hasNext(); ) {
                SchemaModel model = (SchemaModel)iter.next();
                model.setEnded(ended);
                model.setStatus(IInfoModel.STATUS_MODIFIED);
            }
        }
        
        try {
            // PVT �X�V  state==2 || state == 4
            PatientVisitModel exist = (PatientVisitModel) em.find(PatientVisitModel.class, new Long(pvtPK));
            exist.setState(state);
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        }

        return id;
    }

    /**
     * �h�L�������g��_���폜����B
     * @param pk �_���폜����h�L�������g�� primary key
     * @return �폜��������
     */
    @Override
    public int deleteDocument(long id) {

        //
        // �Ώ� Document ���擾����
        //
        Date ended = new Date();
        DocumentModel delete = (DocumentModel) em.find(DocumentModel.class, id);

        //
        // �Q�Ƃ��Ă���ꍇ�͗�O�𓊂���
        //
        if (delete.getLinkId() != 0L) {
            throw new CanNotDeleteException("���̃h�L�������g���Q�Ƃ��Ă��邽�ߍ폜�ł��܂���B");
        }

        //
        // �Q�Ƃ���Ă���ꍇ�͗�O�𓊂���
        //
        Collection refs = em.createQuery(QUERY_DOCUMENT_BY_LINK_ID)
        .setParameter(ID, id).getResultList();
        if (refs != null && refs.size() >0) {
            CanNotDeleteException ce = new CanNotDeleteException("���̃h�L�������g����Q�Ƃ���Ă��邽�ߍ폜�ł��܂���B");
            throw ce;
        }

        //
        // �P�ƃ��R�[�h�Ȃ̂ō폜�t���O�����Ă�
        //
        delete.setStatus(IInfoModel.STATUS_DELETE);
        delete.setEnded(ended);

        //
        // �֘A���郂�W���[���ɓ����������s��
        //
        Collection deleteModules = em.createQuery(QUERY_MODULE_BY_DOC_ID)
        .setParameter(ID, id).getResultList();
        for (Iterator iter = deleteModules.iterator(); iter.hasNext(); ) {
            ModuleModel model = (ModuleModel) iter.next();
            model.setStatus(IInfoModel.STATUS_DELETE);
            model.setEnded(ended);
        }

        //
        // �֘A����摜�ɓ����������s��
        //
        Collection deleteImages = em.createQuery(QUERY_SCHEMA_BY_DOC_ID)
        .setParameter(ID, id).getResultList();
        for (Iterator iter = deleteImages.iterator(); iter.hasNext(); ) {
            SchemaModel model = (SchemaModel) iter.next();
            model.setStatus(IInfoModel.STATUS_DELETE);
            model.setEnded(ended);
        }

        return 1;
    }

    /**
     * �h�L�������g�̃^�C�g����ύX����B
     * @param pk �ύX����h�L�������g�� primary key
     * @return �ύX��������
     */
    @Override
    public int updateTitle(long pk, String title) {
        DocumentModel update = (DocumentModel) em.find(DocumentModel.class, pk);
        update.getDocInfoModel().setTitle(title);
        return 1;
    }

    /**
     * ModuleModel�G���g�����擾����B
     * @param spec ���W���[�������d�l
     * @return ModuleModel���X�g�̃��X�g
     */
    @Override
    public List<List> getModules(long karteId, String entity, List fromDate, List toDate) {

        // ���o���Ԃ͕ʂ����Ă���
        int len = fromDate.size();
        List<List> ret = new ArrayList<List>(len);

        // ���o���ԃZ�b�g�̐������J��Ԃ�
        for (int i = 0; i < len; i++) {

            List modules
                    = em.createQuery(QUERY_MODULE_BY_ENTITY)
                    .setParameter(KARTE_ID, karteId)
                    .setParameter(ENTITY, entity)
                    .setParameter(FROM_DATE, fromDate.get(i))
                    .setParameter(TO_DATE, toDate.get(i))
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
    @Override
    public List<List> getImages(long karteId, List fromDate, List toDate) {

        // ���o���Ԃ͕ʂ����Ă���
        int len = fromDate.size();
        List<List> ret = new ArrayList<List>(len);

        // ���o���ԃZ�b�g�̐������J��Ԃ�
        for (int i = 0; i < len; i++) {

            List modules
                    = em.createQuery(QUERY_SCHEMA_BY_KARTE_ID)
                    .setParameter(KARTE_ID, karteId)
                    .setParameter(FROM_DATE, fromDate.get(i))
                    .setParameter(TO_DATE, toDate.get(i))
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
    @Override
    public SchemaModel getImage(long id) {
        SchemaModel image = (SchemaModel)em.find(SchemaModel.class, id);
        return image;
    }

    @Override
    public List<SchemaModel> getS3Images(String fid, int firstResult, int maxResult) {

        List<SchemaModel> ret = (List<SchemaModel>)
                                em.createQuery(QUERY_SCHEMA_BY_FACILITY_ID)
                                .setParameter(FID, fid+"%")
                                .setFirstResult(firstResult)
                                .setMaxResults(maxResult)
                                .getResultList();
        return ret;
    }

    @Override
    public void deleteS3Image(long pk) {
        SchemaModel target = em.find(SchemaModel.class, pk);
        target.getExtRefModel().setBucket(null);
        target.getExtRefModel().setSop(null);
        target.getExtRefModel().setUrl(null);
    }

    /**
     * ���a�����X�g���擾����B
     * @param spec �����d�l
     * @return ���a���̃��X�g
     */
    @Override
    public List<RegisteredDiagnosisModel> getDiagnosis(long karteId, Date fromDate, boolean activeOnly) {

        List<RegisteredDiagnosisModel> ret = null;

        // �����J�n�����w�肵�Ă���
        if (fromDate != null) {
            String query = activeOnly ? QUERY_DIAGNOSIS_BY_KARTE_DATE_ACTIVEONLY : QUERY_DIAGNOSIS_BY_KARTE_DATE;
            ret = (List<RegisteredDiagnosisModel>) em.createQuery(query)
                    .setParameter(KARTE_ID, karteId)
                    .setParameter(FROM_DATE, fromDate)
                    .getResultList();
        } else {
            // �S���Ԃ̏��a���𓾂�
            String query = activeOnly ? QUERY_DIAGNOSIS_BY_KARTE_ACTIVEONLY : QUERY_DIAGNOSIS_BY_KARTE;
            ret = (List<RegisteredDiagnosisModel>)em.createQuery(query)
                    .setParameter(KARTE_ID, karteId)
                    .getResultList();
        }

        return ret;
    }

    /**
     * ���a����ǉ�����B
     * @param addList �ǉ����鏝�a���̃��X�g
     * @return id�̃��X�g
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
    public List<ObservationModel> getObservations(long karteId, String observation, String phenomenon, Date firstConfirmed) {

        List ret = null;

        if (observation != null) {
            if (firstConfirmed != null) {
                ret = em.createQuery("from ObservationModel o where o.karte.id=:karteId and o.observation=:observation and o.started >= :firstConfirmed")
                .setParameter(KARTE_ID, karteId)
                .setParameter("observation", observation)
                .setParameter("firstConfirmed", firstConfirmed)
                .getResultList();

            } else {
                ret = em.createQuery("from ObservationModel o where o.karte.id=:karteId and o.observation=:observation")
                .setParameter(KARTE_ID, karteId)
                .setParameter("observation", observation)
                .getResultList();
            }
        } else if (phenomenon != null) {
            if (firstConfirmed != null) {
                ret = em.createQuery("from ObservationModel o where o.karte.id=:karteId and o.phenomenon=:phenomenon and o.started >= :firstConfirmed")
                .setParameter(KARTE_ID, karteId)
                .setParameter("phenomenon", phenomenon)
                .setParameter("firstConfirmed", firstConfirmed)
                .getResultList();
            } else {
                ret = em.createQuery("from ObservationModel o where o.karte.id=:karteId and o.phenomenon=:phenomenon")
                .setParameter(KARTE_ID, karteId)
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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

    //--------------------------------------------------------------------------

    /**
     * �Љ���ۑ��܂��͍X�V����B
     */
    @Override
    public long saveOrUpdateLetter(LetterModel model) {
        LetterModel saveOrUpdate = em.merge(model);
        return saveOrUpdate.getId();
    }

    /**
     * �Љ��̃��X�g���擾����B
     */
    @Override
    public List<LetterModel> getLetterList(long karteId, String docType) {

        if (docType.equals(TOUTOU)) {
            // �Љ��
            List<LetterModel> ret = (List<LetterModel>)
                        em.createQuery(QUERY_LETTER_BY_KARTE_ID)
                        .setParameter(KARTE_ID, karteId)
                        .getResultList();
            return ret;

        } else if (docType.equals(TOUTOU_REPLY)) {
            // �ԏ�
            List<LetterModel> ret = (List<LetterModel>)
                        em.createQuery(QUERY_REPLY_BY_KARTE_ID)
                        .setParameter(KARTE_ID, karteId)
                        .getResultList();
            return ret;
        }

        return null;
    }

    /**
     * �Љ����擾����B
     */
    @Override
    public LetterModel getLetter(long letterPk) {

        LetterModel ret = (LetterModel)
                        em.createQuery(QUERY_LETTER_BY_ID)
                        .setParameter(ID, letterPk)
                        .getSingleResult();
        return ret;
    }

    @Override
    public LetterModel getLetterReply(long letterPk) {

        LetterModel ret = (LetterModel)
                        em.createQuery(QUERY_REPLY_BY_ID)
                        .setParameter(ID, letterPk)
                        .getSingleResult();
        return ret;
    }

    //--------------------------------------------------------------------------

    @Override
    public List<List> getAppointmentList(long karteId, List fromDate, List toDate) {

        // ���o���Ԃ͕ʂ����Ă���
        int len = fromDate.size();
        List<List> ret = new ArrayList<List>(len);

        // ���o���ԃZ�b�g�̐������J��Ԃ�
        for (int i = 0; i < len; i++) {

            List modules
                    = em.createQuery(QUERY_APPO_BY_KARTE_ID_PERIOD)
                    .setParameter(KARTE_ID, karteId)
                    .setParameter(FROM_DATE, fromDate.get(i))
                    .setParameter(TO_DATE, toDate.get(i))
                    .getResultList();

            ret.add(modules);
        }

        return ret;
    }
}
