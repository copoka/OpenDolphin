package open.dolphin.ejb;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import open.dolphin.infomodel.CompositeImageModel;
import open.dolphin.infomodel.FirstEncounter0Model;
import open.dolphin.infomodel.FirstEncounter2Model;
import open.dolphin.infomodel.FirstEncounterModel;
import open.dolphin.infomodel.KarteBean;
import open.dolphin.infomodel.LetterModel;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.PatientVisitModel;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;

/**
 * ���c�N���j�b�N�T�[�r�X�B
 */
@Stateless
@SecurityDomain("openDolphin")
@RolesAllowed("user")
@Remote({RemoteSetaService.class})
@RemoteBinding(jndiBinding="openDolphin/RemoteSetaService")
public class RemoteSetaServiceImpl extends DolphinService implements RemoteSetaService {
    
    @Resource
    private SessionContext ctx;
    
    @PersistenceContext
    private EntityManager em;
    
    /** Creates a new instance of RemoteSetaServiceImpl */
    public RemoteSetaServiceImpl() {
    }
    
    /**
     * ���c�N���j�b�N�V���o�^&��t�B
     * @param patient PatientModel
     * @param model ���c�N���j�b�N�ŗL�̏��
     */
    public long[] saveOrUpdateAsPvt(PatientVisitModel pvt, FirstEncounter0Model model) {
        
        long[] ret = new long[2];
        String facilityId = getCallersFacilityId(ctx);
        
        PatientModel patient = pvt.getPatient();
        patient.setFacilityId(facilityId);
        pvt.setFacilityId(facilityId);
        
        try {
            PatientModel exist = (PatientModel) em
                    .createQuery("from PatientModel p where p.facilityId = :fid and p.patientId = :pid")
                    .setParameter("fid", facilityId)
                    .setParameter("pid", patient.getPatientId())
                    .getSingleResult();
            
            // �X�V����
            PatientModel update = em.merge(patient);
            
            KarteBean karte = (KarteBean) em
                       .createQuery("from KarteBean k where k.patient.id = :pk")
                       .setParameter("pk", update.getId())
                       .getSingleResult();
            
            model.setKarte(karte);
            FirstEncounter0Model updateF = em.merge(model);
            ret[0] = update.getId();
            ret[1] = updateF.getId();
            
        } catch (NoResultException ex) {
            
            // �V�K���҂Ȃ̂ŉi��������
            em.persist(patient);
            
            // ���̊��҂̃J���e�𐶐�����
            KarteBean karte = new KarteBean();
            karte.setPatient(patient);
            karte.setCreated(new Date());
            em.persist(karte);
            
            // ���f������ۑ�����
            // Creator �͐������ɂ��Ă���
            model.setKarte(karte);
            em.persist(model);
            
            ret[0] = patient.getId();
            ret[1] = model.getId();
        }
        
        // PVT ���i��������
        em.persist(pvt);
        
        return ret;
    }
    
    /**
     * ���c�N���j�b�N�V���o�^�B
     * @param patient PatientModel
     * @param model ���c�N���j�b�N�ŗL�̏��
     */
    public long[] saveOrUpdatePatient(PatientModel patient, FirstEncounter0Model model) {
        
        long[] ret = new long[2];
        String facilityId = getCallersFacilityId(ctx);
        patient.setFacilityId(facilityId);
        
        try {
            PatientModel exist = (PatientModel) em
                    .createQuery("from PatientModel p where p.facilityId = :fid and p.patientId = :pid")
                    .setParameter("fid", facilityId)
                    .setParameter("pid", patient.getPatientId())
                    .getSingleResult();
            
            // �X�V����
            PatientModel update = em.merge(patient);
            
            KarteBean karte = (KarteBean) em
                       .createQuery("from KarteBean k where k.patient.id = :pk")
                       .setParameter("pk", update.getId())
                       .getSingleResult();
            
            model.setKarte(karte);
            FirstEncounter0Model updateF = em.merge(model);
            ret[0] = update.getId();
            ret[1] = updateF.getId();
            
        } catch (NoResultException ex) {
            
            // �V�K���҂Ȃ̂ŉi��������
            em.persist(patient);
            
            // ���̊��҂̃J���e�𐶐�����
            KarteBean karte = new KarteBean();
            karte.setPatient(patient);
            karte.setCreated(new Date());
            em.persist(karte);
            
            // ���f������ۑ�����
            // Creator �͐������ɂ��Ă���
            model.setKarte(karte);
            em.persist(model);
            
            ret[0] = patient.getId();
            ret[1] = model.getId();
        }
        
        return ret;
    }
    
    
    /**
     * ���f������ۑ��܂��͍X�V����B 
     */
    public long saveOrUpdateFirstEncounter(FirstEncounterModel model) {
        
        // �ۑ����͍X�V����
        FirstEncounterModel saveOrUpdate = em.merge(model);
        
        // ���f�����2�̎�
        if (model instanceof FirstEncounter2Model) {
        
            // �摜���X�V�����\��������̂ŁA�Â����͍̂폜����
            List<CompositeImageModel> oldImages = (List<CompositeImageModel>) 
                    em.createQuery("from CompositeImageModel c where compositor = :cid")
                      .setParameter("cid", saveOrUpdate.getId())
                      .getResultList();
            if (oldImages != null && oldImages.size() > 0) {
                for (CompositeImageModel cid : oldImages) {
                    em.remove(cid);
                }
            }

            // �摜�����Ă���Εۑ�����
            Collection<CompositeImageModel> newImages = ((FirstEncounter2Model)model).getCompositeImages();
            if (newImages != null && newImages.size() > 0) {
                for (CompositeImageModel cim : newImages) {
                    cim.setCompositor(saveOrUpdate.getId());
                    em.merge(cim);
                }
            }
        }
        
        return saveOrUpdate.getId();
    }
    
    
    /**
     * ���f�������擾����B
     * @param karteId karte PK
     * @param docType ���
     */
    public List<FirstEncounterModel> getFirstEncounter(long karteId, String docType) {
        
        if (docType.equals("SETA_0")) {
            List<FirstEncounterModel> ret = (List<FirstEncounterModel>)
                        em.createQuery("from FirstEncounter0Model f where f.karte.id = :karteId")
                        .setParameter("karteId", karteId)
                        .getResultList();
            return ret;
            
        }
        else if (docType.equals("SETA_1")) {
            List<FirstEncounterModel> ret = (List<FirstEncounterModel>)
                        em.createQuery("from FirstEncounter1Model f where f.karte.id = :karteId")
                        .setParameter("karteId", karteId)
                        .getResultList();
            return ret;
            
        } 
        else if (docType.equals("SETA_2")) {
            List<FirstEncounterModel> ret = (List<FirstEncounterModel>)
                        em.createQuery("from FirstEncounter2Model f where f.karte.id = :karteId")
                        .setParameter("karteId", karteId)
                        .getResultList();
            if (ret != null && ret.size() > 0) {
                FirstEncounter2Model fe2 = (FirstEncounter2Model) ret.get(0);
                List<CompositeImageModel> images = (List<CompositeImageModel>)
                            em.createQuery("from CompositeImageModel c where compositor = :cid")
                            .setParameter("cid", fe2.getId())
                            .getResultList();
                fe2.setCompositeImages(images);
            }
            return ret;
        }
        
        return null;
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
    public List<LetterModel> getLetters(long karteId, String docType) {
        if (docType.equals("TOUTOU")) {
            List<LetterModel> ret = (List<LetterModel>)
                        em.createQuery("from TouTouLetter f where f.karte.id = :karteId")
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
}
