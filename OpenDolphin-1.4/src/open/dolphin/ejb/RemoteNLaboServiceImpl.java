package open.dolphin.ejb;

import java.util.Collection;
import java.util.List;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import open.dolphin.infomodel.NLaboItem;
import open.dolphin.infomodel.NLaboModule;
import open.dolphin.infomodel.PatientModel;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;

/**
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
@Stateless
@SecurityDomain("openDolphin")
@RolesAllowed("user")
@Remote({RemoteNLaboService.class})
@RemoteBinding(jndiBinding="openDolphin/RemoteNLaboService")
public class RemoteNLaboServiceImpl implements RemoteNLaboService {
    
    @Resource
    private SessionContext ctx;

    @PersistenceContext
    private EntityManager em;


    //
    // ���O�C�����[�U�[�̎{��ID������Ԃ��B
    //
    private String getCallersFacilityId(SessionContext ctx) {
        String callerId = ctx.getCallerPrincipal().getName();
        int index = callerId.indexOf(":");
        return index > 0 ? callerId.substring(0, index) : callerId;
    }

    //
    // ����ID���{��ID:����ID�̌`�ɂ���B
    //
    private String getFidPid(String pid) {
        StringBuilder sb = new StringBuilder();
        sb.append(getCallersFacilityId(ctx));
        sb.append(":");
        sb.append(pid);
        return sb.toString();
    }


    @Override
    public PatientModel create(NLaboModule module) {

        String facilityId = this.getCallersFacilityId(ctx);
        String patientId = module.getPatientId();

        // �{��ID�� LaboModule �̊���ID�� ���҂��擾����
        PatientModel patient = (PatientModel) em
                .createQuery("from PatientModel p where p.facilityId=:fid and p.patientId=:pid")
                .setParameter("fid", facilityId)
                .setParameter("pid", patientId)
                .getSingleResult();

        // FacilityId:PatientId �̌`�ɂ���
        String fidPid = getFidPid(patientId);
        module.setPatientId(fidPid);

        // item �� patientId ��ύX����
        Collection<NLaboItem> items = module.getItems();
        for (NLaboItem item : items) {
            item.setPatientId(fidPid);
        }

        //
        // patientId & ���̍̎�� & ���{�R�[�h �� key
        // ���ꂪ��v���Ă��郂�W���[���͍ĕ񍐂Ƃ��č폜���Ă���o�^����B
        //
        String sampleDate = module.getSampleDate();
        String laboCode = module.getLaboCenterCode();

        NLaboModule exist = null;

        try {

            exist = (NLaboModule)
                    em.createQuery("from NLaboModule m where m.patientId=:fidPid and m.sampleDate=:sampleDate and m.laboCenterCode=:laboCode")
                      .setParameter("fidPid", fidPid)
                      .setParameter("sampleDate", sampleDate)
                      .setParameter("laboCode", laboCode)
                      .getSingleResult();

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
    public List<NLaboModule> getLaboTest(String patientId, int firstResult, int maxResult) {

        String fidPid = getFidPid(patientId);

        //
        // ���̍̎���̍~���ŕԂ�
        //
        List<NLaboModule> ret = (List<NLaboModule>)
                        em.createQuery("from NLaboModule l where l.patientId=:fidPid order by l.sampleDate desc")
                          .setParameter("fidPid", fidPid)
                          .setFirstResult(firstResult)
                          .setMaxResults(maxResult)
                          .getResultList();

        for (NLaboModule m : ret) {

            List<NLaboItem> items = (List<NLaboItem>)
                            em.createQuery("from NLaboItem l where l.laboModule.id=:mid order by groupCode,parentCode,itemCode")
                              .setParameter("mid", m.getId())
                              .getResultList();
            m.setItems(items);
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
    public List<NLaboItem> getLaboTestItem(String patientId, int firstResult, int maxResult, String itemCode) {

        String fidPid = getFidPid(patientId);

        List<NLaboItem> ret = (List<NLaboItem>)
                        em.createQuery("from NLaboItem l where l.patientId=:fidPid and l.itemCode=:itemCode order by l.sampleDate desc")
                          .setParameter("fidPid", fidPid)
                          .setParameter("itemCode", itemCode)
                          .setFirstResult(firstResult)
                          .setMaxResults(maxResult)
                          .getResultList();

        return ret;
    }
}
