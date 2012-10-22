package open.dolphin.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import open.dolphin.infomodel.HealthInsuranceModel;
import open.dolphin.infomodel.NLaboItem;
import open.dolphin.infomodel.NLaboModule;
import open.dolphin.infomodel.PatientLiteModel;
import open.dolphin.infomodel.PatientModel;

/**
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
@Stateless
public class NLabServiceBean implements NLabServiceBeanLocal {

    private static final String QUERY_MODULE_BY_MODULE_KEY = "from NLaboModule m where m.moduleKey=:moduleKey";
    private static final String QUERY_MODULE_BY_PID_SAMPLEDATE_LABCODE = "from NLaboModule m where m.patientId=:fidPid and m.sampleDate=:sampleDate and m.laboCenterCode=:laboCode";
    private static final String QUERY_MODULE_BY_FIDPID = "from NLaboModule l where l.patientId=:fidPid order by l.sampleDate desc";
    private static final String QUERY_ITEM_BY_MID = "from NLaboItem l where l.laboModule.id=:mid order by groupCode,parentCode,itemCode";
    private static final String QUERY_ITEM_BY_MID_ORDERBY_SORTKEY = "from NLaboItem l where l.laboModule.id=:mid order by l.sortKey";
    private static final String QUERY_ITEM_BY_FIDPID_ITEMCODE = "from NLaboItem l where l.patientId=:fidPid and l.itemCode=:itemCode order by l.sampleDate desc";
    private static final String QUERY_INSURANCE_BY_PATIENT_PK = "from HealthInsuranceModel h where h.patient.id=:pk";

    private static final String PK = "pk";
    private static final String FIDPID = "fidPid";
    private static final String SAMPLEDATE = "sampleDate";
    private static final String LABOCODE = "laboCode";
    private static final String MODULEKEY = "moduleKey";
    private static final String MID = "mid";
    private static final String ITEM_CODE = "itemCode";
    private static final String WOLF = "WOLF";

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<PatientLiteModel> getConstrainedPatients(String fid, List<String>idList) {

        List<PatientLiteModel> ret = new ArrayList<PatientLiteModel>(idList.size());

        for (String pid : idList) {

            try {
                PatientModel patient = (PatientModel) em
                    .createQuery("from PatientModel p where p.facilityId=:fid and p.patientId=:pid")
                    .setParameter("fid", fid)
                    .setParameter("pid", pid)
                    .getSingleResult();
                
                ret.add(patient.patientAsLiteModel());
                
            } catch (NoResultException e) {
                PatientLiteModel dummy = new PatientLiteModel();
                dummy.setFullName("���o�^");
                dummy.setKanaName("���o�^");
                dummy.setGender("U");
                ret.add(dummy);
            }
        }

        return ret;
    }

    @Override
    public PatientModel create(String fid, NLaboModule module) {

        String pid = module.getPatientId();

        // �{��ID�� LaboModule �̊���ID�� ���҂��擾����
        PatientModel patient = (PatientModel) em
                .createQuery("from PatientModel p where p.facilityId=:fid and p.patientId=:pid")
                .setParameter("fid", fid)
                .setParameter("pid", pid)
                .getSingleResult();


        //--------------------------------------------------------
        if (patient!=null) {

            // ���҂̌��N�ی����擾����
            List<HealthInsuranceModel> insurances
                    = (List<HealthInsuranceModel>)em.createQuery(QUERY_INSURANCE_BY_PATIENT_PK)
                    .setParameter(PK, patient.getId()).getResultList();
            patient.setHealthInsurances(insurances);
        }
        //--------------------------------------------------------

        String fidPid = fid+":"+pid;
        module.setPatientId(fidPid);

        // item �� patientId ��ύX����
        Collection<NLaboItem> items = module.getItems();
        for (NLaboItem item : items) {
            item.setPatientId(fidPid);
        }

        //--------------------------------------------------------
        // patientId & ���̍̎�� & ���{�R�[�h �� key
        // ���ꂪ��v���Ă��郂�W���[���͍ĕ񍐂Ƃ��č폜���Ă���o�^����B
        //--------------------------------------------------------
        String sampleDate = module.getSampleDate();
        String laboCode = module.getLaboCenterCode();
        String moduleKey = module.getModuleKey();

        NLaboModule exist = null;

        try {
            if (moduleKey!=null) {
                exist = (NLaboModule)em.createQuery(QUERY_MODULE_BY_MODULE_KEY)
                                       .setParameter(MODULEKEY, moduleKey)
                                       .getSingleResult();

            } else {
                exist = (NLaboModule)em.createQuery(QUERY_MODULE_BY_PID_SAMPLEDATE_LABCODE)
                                       .setParameter(FIDPID, fidPid)
                                       .setParameter(SAMPLEDATE, sampleDate)
                                       .setParameter(LABOCODE, laboCode)
                                       .getSingleResult();
            }

        } catch (Exception e) {
            exist = null;
        }

        // Cascade.TYPE=ALL
        if (exist != null) {
            em.remove(exist);
        }

        // �i��������
        em.persist(module);

        return patient;
    }


    /**
     * ���{���W���[������������B
     * @param patientId     �Ώۊ��҂�ID
     * @param firstResult   �擾���ʃ��X�g�̍ŏ��̔ԍ�
     * @param maxResult     �擾���錏���̍ő�l
     * @return              ���{���W���[���̃��X�g
     */
    @Override
    public List<NLaboModule> getLaboTest(String fidPid, int firstResult, int maxResult) {

        //String fidPid = SessionHelper.getQualifiedPid(ctx, patientId);

        //
        // ���̍̎���̍~���ŕԂ�
        //
        List<NLaboModule> ret = (List<NLaboModule>)
                        em.createQuery(QUERY_MODULE_BY_FIDPID)
                          .setParameter(FIDPID, fidPid)
                          .setFirstResult(firstResult)
                          .setMaxResults(maxResult)
                          .getResultList();

        for (NLaboModule m : ret) {

            if (m.getReportFormat()!=null && m.getReportFormat().equals(WOLF)) {
                List<NLaboItem> items = (List<NLaboItem>)
                                em.createQuery(QUERY_ITEM_BY_MID_ORDERBY_SORTKEY)
                                  .setParameter(MID, m.getId())
                                  .getResultList();
                m.setItems(items);

            } else {
                List<NLaboItem> items = (List<NLaboItem>)
                                em.createQuery(QUERY_ITEM_BY_MID)
                                  .setParameter(MID, m.getId())
                                  .getResultList();
                m.setItems(items);
            }
        }
        return ret;
    }


    /**
     * �w�肳�ꂽ�������ڂ���������B
     * @param patientId     ����ID
     * @param firstResult   �ŏ��̌���
     * @param maxResult     �߂������̍ő�l
     * @param itemCode      �������錟�����ڃR�[�h
     * @return              �������ڃR�[�h���~���Ɋi�[���ꂽ���X�g
     */
    @Override
    public List<NLaboItem> getLaboTestItem(String fidPid, int firstResult, int maxResult, String itemCode) {

        //String fidPid = SessionHelper.getQualifiedPid(ctx, patientId);

        List<NLaboItem> ret = (List<NLaboItem>)
                        em.createQuery(QUERY_ITEM_BY_FIDPID_ITEMCODE)
                          .setParameter(FIDPID, fidPid)
                          .setParameter(ITEM_CODE, itemCode)
                          .setFirstResult(firstResult)
                          .setMaxResults(maxResult)
                          .getResultList();

        return ret;
    }
}
