package open.dolphin.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import open.dolphin.infomodel.*;

/**
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
@Named
@Stateless
public class NLabServiceBean {

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
                dummy.setFullName("未登録");
                dummy.setKanaName("未登録");
                dummy.setGender("U");
                ret.add(dummy);
            }
        }

        return ret;
    }

    
    public PatientModel create(String fid, NLaboModule module) {

        String pid = module.getPatientId();

        // 施設IDと LaboModule の患者IDで 患者を取得する
        PatientModel patient = (PatientModel) em
                .createQuery("from PatientModel p where p.facilityId=:fid and p.patientId=:pid")
                .setParameter("fid", fid)
                .setParameter("pid", pid)
                .getSingleResult();


        //--------------------------------------------------------
        if (patient!=null) {

            // 患者の健康保険を取得する
            List<HealthInsuranceModel> insurances
                    = (List<HealthInsuranceModel>)em.createQuery(QUERY_INSURANCE_BY_PATIENT_PK)
                    .setParameter(PK, patient.getId()).getResultList();
            patient.setHealthInsurances(insurances);
        }
        //--------------------------------------------------------

        String fidPid = fid+":"+pid;
        module.setPatientId(fidPid);

        // item の patientId を変更する
        Collection<NLaboItem> items = module.getItems();
        for (NLaboItem item : items) {
            item.setPatientId(fidPid);
        }

        //--------------------------------------------------------
        // patientId & 検体採取日 & ラボコード で key
        // これが一致しているモジュールは再報告として削除してから登録する。
        //--------------------------------------------------------
        String sampleDate = module.getSampleDate();
        String laboCode = module.getLaboCenterCode();
        String moduleKey = module.getModuleKey();
        if (moduleKey!=null) {
            StringBuilder sb = new StringBuilder();
            sb.append(pid).append(".").append(sampleDate).append(".").append(laboCode);
            String test = sb.toString();
            if (test.equals(moduleKey)) {
                sb = new StringBuilder();
                sb.append(fid);
                sb.append(":");
                sb.append(moduleKey);
                moduleKey = sb.toString();
                module.setModuleKey(moduleKey);
                //System.err.println("corrected moduke key=" + module.getModuleKey());
            } 
        }

        NLaboModule exist;

        try {
            if (moduleKey!=null) {
                exist = (NLaboModule)em.createQuery(QUERY_MODULE_BY_MODULE_KEY)
                                       .setParameter(MODULEKEY, moduleKey)
                                       .getSingleResult();
                //System.err.println("module did exist");

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
            //System.err.println("module did remove");
        }

        // 永続化する
        em.persist(module);

        return patient;
    }


    /**
     * ラボモジュールを検索する。
     * @param patientId     対象患者のID
     * @param firstResult   取得結果リストの最初の番号
     * @param maxResult     取得する件数の最大値
     * @return              ラボモジュールのリスト
     */
    
    public List<NLaboModule> getLaboTest(String fidPid, int firstResult, int maxResult) {

        //String fidPid = SessionHelper.getQualifiedPid(ctx, patientId);

        //
        // 検体採取日の降順で返す
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
     * 指定された検査項目を検索する。
     * @param patientId     患者ID
     * @param firstResult   最初の結果
     * @param maxResult     戻す件数の最大値
     * @param itemCode      検索する検査項目コード
     * @return              検査項目コードが降順に格納されたリスト
     */
    
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
