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

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;

import open.dolphin.dto.LaboSearchSpec;
import open.dolphin.infomodel.KarteBean;
import open.dolphin.infomodel.LaboItemValue;
import open.dolphin.infomodel.LaboModuleValue;
import open.dolphin.infomodel.LaboSpecimenValue;
import open.dolphin.infomodel.PatientModel;

@Stateless
@SecurityDomain("openDolphin")
@RolesAllowed("user")
@Remote({RemoteLaboService.class})
@RemoteBinding(jndiBinding="openDolphin/RemoteLaboService")
public class  RemoteLaboServiceImpl extends DolphinService implements RemoteLaboService {
    
    @Resource
    private SessionContext ctx;
    
    @PersistenceContext
    private EntityManager em;
    
    /**
     * LaboModule��ۑ�����B
     * @param laboModuleValue LaboModuleValue
     */
    public PatientModel putLaboModule(LaboModuleValue laboModuleValue) {
        
        //try {
            // MML�t�@�C�����p�[�X�������ʂ��o�^�����
            // �{��ID�̓R���e�L�X�g����擾����
            String facilityId = this.getCallersFacilityId(ctx);
            
            // �{��ID�� LaboModule �̊���ID�� ���҂��擾����
            PatientModel exist = (PatientModel) em
                    .createQuery("from PatientModel p where p.facilityId = :fid and p.patientId = :pid")
                    .setParameter("fid", facilityId)
                    .setParameter("pid", laboModuleValue.getPatientId())
                    .getSingleResult();
            
            // ���҂̃J���e���擾����
            KarteBean karte = (KarteBean) em.createQuery("from KarteBean k where k.patient.id = :pk")
            .setParameter("pk", exist.getId())
            .getSingleResult();
            
            // laboModule�ƃJ���e�̊֌W��ݒ肷��
            laboModuleValue.setKarte(karte);
            
            // �i��������
            em.persist(laboModuleValue);
            
            // ID�����^�[������
            return exist;
            
        //} catch (Exception e) {
        //}
        
        //return null;
    }
    
    /**
     * ���҂̌��̌������W���[�����擾����B
     * @param spec LaboSearchSpec �����d�l
     * @return laboModule �� Collection
     */
    @SuppressWarnings("unchecked")
    public Collection getLaboModuless(LaboSearchSpec spec ) {
        
        long karteId = spec.getKarteId();
        
        // �����t�F�b�`�ł͂Ȃ�
        List<LaboModuleValue> modules = em.createQuery("from LaboModuleValue l where l.karte.id = :karteId and l.sampleTime between :sampleFrom and :sampleTo")
        .setParameter("karteId", karteId)
        .setParameter("sampleFrom", spec.getFromDate())
        .setParameter("sampleTo", spec.getToDate())
        .getResultList();
        
        for (LaboModuleValue module : modules) {
            List<LaboSpecimenValue> specimens = em.createQuery("from LaboSpecimenValue l where l.laboModule.id = :moduleId")
            .setParameter("moduleId", module.getId())
            .getResultList();
            module.setLaboSpecimens(specimens);
            
            for (LaboSpecimenValue specimen : specimens) {
                List<LaboItemValue> items = em.createQuery("from LaboItemValue l where l.laboSpecimen.id = :specimenId")
                .setParameter("specimenId", specimen.getId())
                .getResultList();
                specimen.setLaboItems(items);
            }
        }
        
        return modules;
    }
    
}






















