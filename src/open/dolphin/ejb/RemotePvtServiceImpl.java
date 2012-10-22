package open.dolphin.ejb;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;

import open.dolphin.dto.PatientVisitSpec;
import open.dolphin.infomodel.AppointmentModel;
import open.dolphin.infomodel.HealthInsuranceModel;
import open.dolphin.infomodel.KarteBean;
import open.dolphin.infomodel.ModelUtils;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.PatientVisitModel;

/**
 * RemotePvtServiceImpl
 *
 * @author Minagawa,Kazushi
 *
 */
@Stateless
@SecurityDomain("openDolphin")
@RolesAllowed("user")
@Remote({RemotePvtService.class})
@RemoteBinding(jndiBinding="openDolphin/RemotePvtService")
public class  RemotePvtServiceImpl extends DolphinService implements RemotePvtService {
    
    private static final long serialVersionUID = -3889943133781444449L;
    
    @Resource
    private SessionContext ctx;
    
    @PersistenceContext
    private EntityManager em;
    
    /**
     * ���җ��@����o�^����B
     * @param spec ���@����ێ����� DTO �I�u�W�F�N�g
     * @return �o�^��
     */
    public int addPvt(PatientVisitModel pvt) {
        
        PatientModel patient = pvt.getPatient();
        
        // CLAIM ���M�̏ꍇ facilityID ���f�[�^�x�[�X�ɓo�^����Ă�����̂ƈقȂ�ꍇ������
        // �{��ID��F�؂Ƀp�X�������[�U�̎{��ID�ɐݒ肷��B
        String facilityId = getCallersFacilityId(ctx);
        pvt.setFacilityId(facilityId);
        patient.setFacilityId(facilityId);
        
        // �����̊��҂��ǂ������ׂ�
        try {
            PatientModel exist = (PatientModel) em
                    .createQuery("from PatientModel p where p.facilityId = :fid and p.patientId = :pid")
                    .setParameter("fid", facilityId)
                    .setParameter("pid", patient.getPatientId())
                    .getSingleResult();
            
            //
            // ���N�ی������X�V����
            //
            Collection<HealthInsuranceModel> ins = patient.getHealthInsurances();
            if (ins != null && ins.size() > 0) {
            
                // ���N�ی����X�V����
                Collection old = em.createQuery("from HealthInsuranceModel h where h.patient.id = :pk")
                .setParameter("pk", exist.getId())
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
            exist.setAddress(patient.getAddress());
            exist.setTelephone(patient.getTelephone());
            //exist.setMobilePhone(patient.getMobilePhone());
            
            // PatientVisit �Ƃ̊֌W��ݒ肷��
            pvt.setPatient(exist);
            
        } catch (NoResultException e) {
            // �V�K���҂ł���Γo�^����
            // ���ґ����� cascade=PERSIST �Ŏ����I�ɕۑ������
            em.persist(patient);
            
            // ���̊��҂̃J���e�𐶐�����
            KarteBean karte = new KarteBean();
            karte.setPatient(patient);
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
    @SuppressWarnings("unchecked")
    public Collection<PatientVisitModel> getPvt(PatientVisitSpec spec) {
        
        String date = spec.getDate();
        if (!date.endsWith("%")) {
            date = date + "%";
        }
        int index = date.indexOf('%');
        Date theDate = ModelUtils.getDateAsObject(date.substring(0, index));
        int firstResult = spec.getSkipCount();
        String fid = getCallersFacilityId(ctx);
        
        String appoDateFrom = spec.getAppodateFrom();
        String appoDateTo = spec.getAppodateTo();
        boolean searchAppo = (appoDateFrom != null && appoDateTo != null) ? true : false;
        
        // PatientVisitModel���{��ID�Ō�������
        Collection result = em.createQuery("from PatientVisitModel p where p.facilityId = :fid and p.pvtDate >= :date order by p.pvtDate")
                              .setFirstResult(firstResult)
                              .setParameter("fid", fid)
                              .setParameter("date", date)
                              .getResultList();
        
        // ���҂̊�{�f�[�^���擾����
        // ���@���Ɗ��҂� ManyToOne �̊֌W�ł���
        for (Iterator iter = result.iterator(); iter.hasNext(); ) {
            
            PatientVisitModel pvt = (PatientVisitModel) iter.next();
            PatientModel patient = pvt.getPatient();
            
            // ���҂̌��N�ی����擾����
            Collection insurances = em.createQuery("from HealthInsuranceModel h where h.patient.id = :pk")
            .setParameter("pk", patient.getId()).getResultList();
            patient.setHealthInsurances(insurances);
            
            // ���̑���ID���擾����
//            Collection otherIds = em.createQuery("from OtherIdModel o where o.patient.id = :pk")
//            .setParameter("pk", patient.getId()).getResultList();
//            patient.setOtherIds(otherIds);
            
            // �\�����������
            if (searchAppo) {
                KarteBean karte = (KarteBean)em.createQuery("from KarteBean k where k.patient.id = :pk")
                .setParameter("pk", patient.getId())
                .getSingleResult();
                // �J���e�� PK �𓾂�
                long karteId = karte.getId();
                
                List c = em.createQuery("from AppointmentModel a where a.karte.id = :karteId and a.date = :date")
                .setParameter("karteId", karteId)
                .setParameter("date", theDate)
                .getResultList();
                if (c != null && c.size() > 0) {
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
    public int removePvt(long id) {
        
        try {
            PatientVisitModel exist = (PatientVisitModel) em.find(PatientVisitModel.class, new Long(id));
            em.remove(exist);
            return 1;
        } catch (Exception e) {
        }
        return 0;
    }
    
    /**
     * �f�@�I�������������ށB
     * @param pk ���R�[�hID
     * @param state �f�@�I���̎� 1
     */
    public int updatePvtState(long pk, int state) {
        PatientVisitModel exist = (PatientVisitModel) em.find(PatientVisitModel.class, new Long(pk));
        exist.setState(state);
        return 1;
    }
}
