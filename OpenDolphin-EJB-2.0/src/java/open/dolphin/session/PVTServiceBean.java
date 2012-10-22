package open.dolphin.session;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import open.dolphin.infomodel.AppointmentModel;
import open.dolphin.infomodel.HealthInsuranceModel;
import open.dolphin.infomodel.KarteBean;
import open.dolphin.infomodel.ModelUtils;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.PatientVisitModel;
import org.apache.log4j.Logger;

/**
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
@Stateless
public class PVTServiceBean implements PVTServiceBeanLocal {

    private static final String QUERY_PATIENT_BY_FID_PID        = "from PatientModel p where p.facilityId=:fid and p.patientId=:pid";
    private static final String QUERY_PVT_BY_FID_PID_DATE       = "from PatientVisitModel p where p.facilityId=:fid and p.pvtDate like :date and p.patient.patientId=:pid";
    private static final String QUERY_PVT_BY_FID_DATE           = "from PatientVisitModel p where p.facilityId=:fid and p.pvtDate like :date order by p.pvtDate";
    private static final String QUERY_PVT_BY_FID_DID_DATE       = "from PatientVisitModel p where p.facilityId=:fid and p.pvtDate like :date and (doctorId=:did or doctorId=:unassigned) order by p.pvtDate";
    private static final String QUERY_INSURANCE_BY_PATIENT_ID   = "from HealthInsuranceModel h where h.patient.id=:id";
    private static final String QUERY_KARTE_BY_PATIENT_ID       = "from KarteBean k where k.patient.id=:id";
    private static final String QUERY_APPO_BY_KARTE_ID_DATE     = "from AppointmentModel a where a.karte.id=:id and a.date=:date";

    private static final String FID = "fid";
    private static final String PID = "pid";
    private static final String DID = "did";
    private static final String UNASSIGNED = "unassigned";
    private static final String ID = "id";
    private static final String DATE = "date";
    private static final String PERCENT = "%";
    private static final int BIT_SAVE_CLAIM     = 1;
    private static final int BIT_MODIFY_CLAIM   = 2;
    private static final int BIT_CANCEL         = 6;

    @PersistenceContext
    private EntityManager em;

    /**
     * ���җ��@����o�^����B
     * @param spec ���@����ێ����� DTO �I�u�W�F�N�g
     * @return �o�^��
     */
    @Override
    public int addPvt(PatientVisitModel pvt) {

        PatientModel patient = pvt.getPatientModel();
        String fid = pvt.getFacilityId();

        //--------------------------------------------
        // ��d�o�^���`�F�b�N����
        //--------------------------------------------
        try {
            List<PatientVisitModel> list = (List<PatientVisitModel>)em
                    .createQuery(QUERY_PVT_BY_FID_PID_DATE)
                    .setParameter(FID, fid)
                    .setParameter(DATE, pvt.getPvtDate()+PERCENT)
                    .setParameter(PID, patient.getPatientId())
                    .getResultList();
            if (!list.isEmpty()) {
                for (PatientVisitModel doubleEntry : list) {
                    em.remove(doubleEntry);
                }
            }

        } catch (Exception te) {
            Logger.getLogger("org.jboss.logging.util.OnlyOnceErrorHandler").warn(te.getMessage());
            return 0;
        }

        // �����̊��҂��ǂ������ׂ�
        try {
            PatientModel exist = (PatientModel) em
                    .createQuery(QUERY_PATIENT_BY_FID_PID)
                    .setParameter(FID, fid)
                    .setParameter(PID, patient.getPatientId())
                    .getSingleResult();

            //-----------------------------
            // ���N�ی������X�V����
            //-----------------------------
            Collection<HealthInsuranceModel> ins = patient.getHealthInsurances();
            if (ins != null && ins.size() > 0) {

                // ���N�ی����X�V����
                Collection old = em.createQuery(QUERY_INSURANCE_BY_PATIENT_ID)
                .setParameter(ID, exist.getId())
                .getResultList();

                // ���݂̕ی������폜����
                for (Iterator iter = old.iterator(); iter.hasNext(); ) {
                    HealthInsuranceModel model = (HealthInsuranceModel) iter.next();
                    em.remove(model);
                }

                // �V�������N�ی�����o�^����
                Collection<HealthInsuranceModel> newOne = patient.getHealthInsurances();
                for (HealthInsuranceModel model : newOne) {
                    model.setPatient(exist);
                    em.persist(model);
                }
            }

            // ���O���X�V���� 2007-04-12
            exist.setFamilyName(patient.getFamilyName());
            exist.setGivenName(patient.getGivenName());
            exist.setFullName(patient.getFullName());
            exist.setKanaFamilyName(patient.getKanaFamilyName());
            exist.setKanaGivenName(patient.getKanaGivenName());
            exist.setKanaName(patient.getKanaName());
            exist.setRomanFamilyName(patient.getRomanFamilyName());
            exist.setRomanGivenName(patient.getRomanGivenName());
            exist.setRomanName(patient.getRomanName());

            // ����
            exist.setGender(patient.getGender());
            exist.setGenderDesc(patient.getGenderDesc());
            exist.setGenderCodeSys(patient.getGenderCodeSys());

            // Birthday
            exist.setBirthday(patient.getBirthday());

            // �Z���A�d�b���X�V����
            exist.setSimpleAddressModel(patient.getSimpleAddressModel());
            exist.setTelephone(patient.getTelephone());
            //exist.setMobilePhone(patient.getMobilePhone());

            // PatientVisit �Ƃ̊֌W��ݒ肷��
            pvt.setPatientModel(exist);

        } catch (NoResultException e) {
            // �V�K���҂ł���Γo�^����
            // ���ґ����� cascade=PERSIST �Ŏ����I�ɕۑ������
            em.persist(patient);

            // ���̊��҂̃J���e�𐶐�����
            KarteBean karte = new KarteBean();
            karte.setPatientModel(patient);
            karte.setCreated(new Date());
            em.persist(karte);
        }

        // ���@����o�^����
        // CLAIM �̎d�l�ɂ�芳�ҏ��݂̂�o�^���A���@���͂Ȃ��ꍇ������
        // ����� pvtDate �̑����Ŕ��f���Ă���
        if (pvt.getPvtDate() != null) {
            em.persist(pvt);
        }

        return 1;
    }

    /**
     * �{�݂̊��җ��@�����擾����B
     * @param spec �����d�lDTO�I�u�W�F�N�g
     * @return ���@����Collection
     */
    @Override
    public List<PatientVisitModel> getPvt(String fid, String date, int firstResult, String appoDateFrom, String appoDateTo) {

        if (!date.endsWith(PERCENT)) {
            date += PERCENT;
        }
        
        // PatientVisitModel���{��ID�Ō�������
        List<PatientVisitModel> result =
                (List<PatientVisitModel>) em.createQuery(QUERY_PVT_BY_FID_DATE)
                              .setParameter(FID, fid)
                              .setParameter(DATE, date+PERCENT)
                              .setFirstResult(firstResult)
                              .getResultList();

        int len = result.size();

        if (len == 0) {
            return result;
        }

        int index = date.indexOf(PERCENT);
        Date theDate = ModelUtils.getDateAsObject(date.substring(0, index));

        boolean searchAppo = (appoDateFrom != null && appoDateTo != null) ? true : false;

        // ���@���Ɗ��҂� ManyToOne �̊֌W�ł���
        for (int i = 0; i < len; i++) {
            //for (int i = firstResult; i < len; i++) {
            
            PatientVisitModel pvt = result.get(i);
            PatientModel patient = pvt.getPatientModel();

            // ���҂̌��N�ی����擾����
            List<HealthInsuranceModel> insurances = (List<HealthInsuranceModel>)em.createQuery(QUERY_INSURANCE_BY_PATIENT_ID)
            .setParameter(ID, patient.getId()).getResultList();
            patient.setHealthInsurances(insurances);

            // �\�����������
            if (searchAppo) {
                KarteBean karte = (KarteBean)em.createQuery(QUERY_KARTE_BY_PATIENT_ID)
                .setParameter(ID, patient.getId())
                .getSingleResult();
                // �J���e�� PK �𓾂�
                long karteId = karte.getId();

                List c = em.createQuery(QUERY_APPO_BY_KARTE_ID_DATE)
                .setParameter(ID, karteId)
                .setParameter(DATE, theDate)
                .getResultList();
                //System.err.println("appo size = " + c.size());
                if (c != null && c.size() > 0) {
                    // �����̗\��ōŏ��̂���
                    AppointmentModel appo = (AppointmentModel) c.get(0);
                    pvt.setAppointment(appo.getName());
                }
            }
        }

        return result;
    }

    @Override
    public List<PatientVisitModel> getPvt(String fid, String did, String unassigned, String date, int firstResult, String appoDateFrom, String appoDateTo) {

        if (!date.endsWith(PERCENT)) {
            date += PERCENT;
        }

        // PatientVisitModel���{��ID�Ō�������
        List<PatientVisitModel> result =
                (List<PatientVisitModel>) em.createQuery(QUERY_PVT_BY_FID_DID_DATE)
                              .setParameter(FID, fid)
                              .setParameter(DID, did)
                              .setParameter(UNASSIGNED, unassigned)
                              .setParameter(DATE, date+PERCENT)
                              .setFirstResult(firstResult)
                              .getResultList();

        int len = result.size();

        if (len == 0) {
            return result;
        }

        int index = date.indexOf(PERCENT);
        Date theDate = ModelUtils.getDateAsObject(date.substring(0, index));

        boolean searchAppo = (appoDateFrom != null && appoDateTo != null) ? true : false;

        // ���@���Ɗ��҂� ManyToOne �̊֌W�ł���
        for (int i = 0; i < len; i++) {
            //for (int i = firstResult; i < len; i++) {

            PatientVisitModel pvt = result.get(i);
            PatientModel patient = pvt.getPatientModel();

            // ���҂̌��N�ی����擾����
            List<HealthInsuranceModel> insurances = (List<HealthInsuranceModel>)em.createQuery(QUERY_INSURANCE_BY_PATIENT_ID)
            .setParameter(ID, patient.getId()).getResultList();
            patient.setHealthInsurances(insurances);

            // �\�����������
            if (searchAppo) {
                KarteBean karte = (KarteBean)em.createQuery(QUERY_KARTE_BY_PATIENT_ID)
                .setParameter(ID, patient.getId())
                .getSingleResult();
                // �J���e�� PK �𓾂�
                long karteId = karte.getId();

                List c = em.createQuery(QUERY_APPO_BY_KARTE_ID_DATE)
                .setParameter(ID, karteId)
                .setParameter(DATE, theDate)
                .getResultList();
                //System.err.println("appo size = " + c.size());
                if (c != null && c.size() > 0) {
                    // �����̗\��ōŏ��̂���
                    AppointmentModel appo = (AppointmentModel) c.get(0);
                    pvt.setAppointment(appo.getName());
                }
            }
        }

        return result;
    }

    /**
     * ��t�����폜����B
     * @param id ��t���R�[�h
     * @return �폜����
     */
    @Override
    public int removePvt(long id) {
        PatientVisitModel exist = (PatientVisitModel) em.find(PatientVisitModel.class, new Long(id));
        em.remove(exist);
        return 1;
    }

    /**
     * �f�@�I�������������ށB
     * @param pk ���R�[�hID
     * @param state �f�@�I���̎� 1
     */
    @Override
    public int updatePvtState(long pk, int state) {

        PatientVisitModel exist = (PatientVisitModel) em.find(PatientVisitModel.class, new Long(pk));

        // �ۑ��iCLAIM���M�j==2 (bit=1)
        // �C�����M == 4 (bit=2)
        if (state == 2 || state == 4) {
            exist.setState(state);
            return 1;
        }

        int curState = exist.getState();
        boolean red = ((curState & (1<<BIT_SAVE_CLAIM))!=0);
        boolean yellow = ((curState & (1<<BIT_MODIFY_CLAIM))!=0);
        boolean cancel = ((curState & (1<<BIT_CANCEL))!=0);

        // �ۑ� | �C�� | �L�����Z�� --> �ύX�s��
        if (red || yellow || cancel) {
            return 0;
        }

        exist.setState(state);
        return 1;
    }

    /**
     * �������X�V����B
     * @param pk ���R�[�hID
     * @param memo ����
     * @return 1
     */
    @Override
    public int updateMemo(long pk, String memo) {
        PatientVisitModel exist = (PatientVisitModel) em.find(PatientVisitModel.class, new Long(pk));
        exist.setMemo(memo);
        return 1;
    }
}
