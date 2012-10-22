package open.dolphin.session;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import open.dolphin.infomodel.HealthInsuranceModel;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.PatientVisitModel;

/**
 *
 * @author Kazushi Minagawa, Digital Globe, Inc
 */
@Stateless
public class PatientServiceBean implements PatientServiceBeanLocal {

    private static final String QUERY_PATIENT_BY_PVTDATE = "from PatientVisitModel p where p.facilityId = :fid and p.pvtDate like :date";
    private static final String QUERY_PATIENT_BY_NAME = "from PatientModel p where p.facilityId=:fid and p.fullName like :name";
    private static final String QUERY_PATIENT_BY_KANA = "from PatientModel p where p.facilityId=:fid and p.kanaName like :name";
    private static final String QUERY_PATIENT_BY_FID_PID = "from PatientModel p where p.facilityId=:fid and p.patientId like :pid";
    private static final String QUERY_PATIENT_BY_TELEPHONE = "from PatientModel p where p.facilityId = :fid and (p.telephone like :number or p.mobilePhone like :number)";
    private static final String QUERY_PATIENT_BY_ZIPCODE = "from PatientModel p where p.facilityId = :fid and p.address.zipCode like :zipCode";
    private static final String QUERY_INSURANCE_BY_PATIENT_PK = "from HealthInsuranceModel h where h.patient.id=:pk";

    private static final String PK = "pk";
    private static final String FID = "fid";
    private static final String PID = "pid";
    private static final String NAME = "name";
    private static final String NUMBER = "number";
    private static final String ZIPCODE = "zipCode";
    private static final String DATE = "date";
    private static final String PERCENT = "%";

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<PatientModel> getPatientsByName(String fid, String name) {

        List<PatientModel> ret = em.createQuery(QUERY_PATIENT_BY_NAME)
                .setParameter(FID, fid)
                .setParameter(NAME, name + PERCENT)
                .getResultList();

        // �����v�������s��
        if (ret.isEmpty()) {
            ret = em.createQuery(QUERY_PATIENT_BY_NAME)
                .setParameter(FID, fid)
                .setParameter(NAME, PERCENT + name)
                .getResultList();
        }
        
        //-----------------------------------
        if (!ret.isEmpty()) {

            for (PatientModel patient : ret) {

                // ���҂̌��N�ی����擾����
                List<HealthInsuranceModel> insurances
                        = (List<HealthInsuranceModel>)em.createQuery(QUERY_INSURANCE_BY_PATIENT_PK)
                        .setParameter(PK, patient.getId()).getResultList();
                patient.setHealthInsurances(insurances);
            }
        }
        //-----------------------------------

        return ret;
    }

    @Override
    public List<PatientModel> getPatientsByKana(String fid, String name) {

        List<PatientModel> ret = em.createQuery(QUERY_PATIENT_BY_KANA)
            .setParameter(FID, fid)
            .setParameter(NAME, name + PERCENT)
            .getResultList();

        if (ret.isEmpty()) {
            ret = em.createQuery(QUERY_PATIENT_BY_KANA)
                .setParameter(FID, fid)
                .setParameter(NAME, PERCENT + name)
                .getResultList();
        }

        //-----------------------------------
        if (!ret.isEmpty()) {

            for (PatientModel patient : ret) {

                // ���҂̌��N�ی����擾����
                List<HealthInsuranceModel> insurances
                        = (List<HealthInsuranceModel>)em.createQuery(QUERY_INSURANCE_BY_PATIENT_PK)
                        .setParameter(PK, patient.getId()).getResultList();
                patient.setHealthInsurances(insurances);
            }
        }
        //-----------------------------------
        
        return ret;
    }

    @Override
    public List<PatientModel> getPatientsByDigit(String fid, String digit) {

        List<PatientModel> ret = em.createQuery(QUERY_PATIENT_BY_FID_PID)
            .setParameter(FID, fid)
            .setParameter(PID, digit+PERCENT)
            .getResultList();

        if (ret.isEmpty()) {
            ret = em.createQuery(QUERY_PATIENT_BY_TELEPHONE)
                .setParameter(FID, fid)
                .setParameter(NUMBER, digit+PERCENT)
                .getResultList();
        }

        if (ret.isEmpty()) {
            ret = em.createQuery(QUERY_PATIENT_BY_ZIPCODE)
                .setParameter(FID, fid)
                .setParameter(ZIPCODE, digit+PERCENT)
                .getResultList();
        }

        //-----------------------------------
        if (!ret.isEmpty()) {

            for (PatientModel patient : ret) {

                // ���҂̌��N�ی����擾����
                List<HealthInsuranceModel> insurances
                        = (List<HealthInsuranceModel>)em.createQuery(QUERY_INSURANCE_BY_PATIENT_PK)
                        .setParameter(PK, patient.getId()).getResultList();
                patient.setHealthInsurances(insurances);
            }
        }
        //-----------------------------------

        return ret;
    }

    @Override
    public List<PatientModel> getPatientsByPvtDate(String fid, String pvtDate) {

        List<PatientVisitModel> list =
                em.createQuery(QUERY_PATIENT_BY_PVTDATE)
                  .setParameter(FID, fid)
                  .setParameter(DATE, pvtDate+PERCENT)
                  .getResultList();

        List<PatientModel> ret = new ArrayList<PatientModel>();

        for (PatientVisitModel pvt : list) {
            PatientModel patient = pvt.getPatientModel();
            List<HealthInsuranceModel> insurances
                        = (List<HealthInsuranceModel>)em.createQuery(QUERY_INSURANCE_BY_PATIENT_PK)
                        .setParameter(PK, patient.getId()).getResultList();
                patient.setHealthInsurances(insurances);
            ret.add(patient);
        }

        return ret;
    }

    /**
     * ����ID(BUSINESS KEY)���w�肵�Ċ��҃I�u�W�F�N�g��Ԃ��B
     *
     * @param patientId �{�ݓ�����ID
     * @return �Y������PatientModel
     */
    @Override
    public PatientModel getPatientById(String fid,String pid) {

        // ���҃��R�[�h�� FacilityId �� patientId �ŕ����L�[�ɂȂ��Ă���
        PatientModel bean
                = (PatientModel)em.createQuery(QUERY_PATIENT_BY_FID_PID)
                .setParameter(FID, fid)
                .setParameter(PID, pid)
                .getSingleResult();

        long pk = bean.getId();

        // Lazy Fetch �� ��{��������������
        // ���҂̌��N�ی����擾����
        List<HealthInsuranceModel> insurances
                = (List<HealthInsuranceModel>)em.createQuery(QUERY_INSURANCE_BY_PATIENT_PK)
                .setParameter(PK, pk).getResultList();
        bean.setHealthInsurances(insurances);

        return bean;
    }

    /**
     * ���҂�o�^����B
     * @param patient PatientModel
     * @return �f�[�^�x�[�X Primary Key
     */
    @Override
    public long addPatient(PatientModel patient) {
        em.persist(patient);
        long pk = patient.getId();
        return pk;
    }

    /**
     * ���ҏ����X�V����B
     * @param patient �X�V���銳��
     * @return �X�V��
     */
    @Override
    public int update(PatientModel patient) {
        em.merge(patient);
        return 1;
    }
}
