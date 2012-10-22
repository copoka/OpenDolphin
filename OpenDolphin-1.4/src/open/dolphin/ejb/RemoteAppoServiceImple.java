package open.dolphin.ejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import open.dolphin.dto.AppointSpec;
import open.dolphin.dto.ModuleSearchSpec;
import open.dolphin.infomodel.AppointmentModel;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;

@Stateless
@SecurityDomain("openDolphin")
@RolesAllowed("user")
@Remote({RemoteAppoService.class})
@RemoteBinding(jndiBinding="openDolphin/RemoteAppoService")
public class RemoteAppoServiceImple extends DolphinService implements RemoteAppoService {
    
    @Resource
    private SessionContext ctx;
    
    @PersistenceContext
    private EntityManager em;
    
    /**
     * �\���ۑ��A�X�V�A�폜����B
     * @param spec �\����� DTO
     */
    public void putAppointments(AppointSpec spec) {
        
        Collection added = spec.getAdded();
        Collection updated = spec.getUpdared();
        Collection removed = spec.getRemoved();
        AppointmentModel av = null;
        
        // �o�^����
        if (added != null && added.size() > 0 ) {
            Iterator iter = added.iterator();
            while(iter.hasNext()) {
                av = (AppointmentModel)iter.next();
                checkIdAsComposite(ctx, av.getPatientId());
                em.persist(av);
            }
        }
        
        // �X�V����
        if (updated != null && updated.size() > 0 ) {
            Iterator iter = updated.iterator();
            while(iter.hasNext()) {
                av = (AppointmentModel)iter.next();
                checkIdAsComposite(ctx, av.getPatientId());
                // av �͕����I�u�W�F�N�g�ł���
                em.merge(av);
            }
        }
        
        // �폜
        if (removed != null && removed.size() > 0 ) {
            Iterator iter = removed.iterator();
            while(iter.hasNext()) {
                av = (AppointmentModel)iter.next();
                checkIdAsComposite(ctx, av.getPatientId());
                // �����I�u�W�F�N�g�� remove �ɓn���Ȃ��̂őΏۂ���������
                AppointmentModel target = (AppointmentModel)em.find(AppointmentModel.class, av.getId());
                em.remove(target);
            }
        }
    }
    
    /**
     * �\�����������B
     * @param spec �����d�l
     * @return �\��� Collection
     */
    public Collection getAppointmentList(ModuleSearchSpec spec) {
        
        // �������銳�҂� Composite Key
        String pcid = checkIdAsComposite(ctx, spec.getPatientId());
        
        // ���o���Ԃ͕ʂ����Ă���
        Date[] fromDate = spec.getFromDate();
        Date[] toDate = spec.getToDate();
        int len = fromDate.length;
        ArrayList<Collection> ret = new ArrayList<Collection>(len);
        
        // ���o���Ԃ��ƂɌ������R���N�V�����ɉ�����
        for (int i = 0; i < len; i++) {
            
            Collection c = em.createQuery("appoByPatient")
            .setParameter("pid", pcid)
            .setParameter("from", fromDate[i])
            .setParameter("to", toDate[i])
            .getResultList();
            ret.add(c);
        }
        
        return ret;
    }
}
