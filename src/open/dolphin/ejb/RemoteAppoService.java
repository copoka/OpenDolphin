package open.dolphin.ejb;

import java.util.Collection;

import open.dolphin.dto.AppointSpec;
import open.dolphin.dto.ModuleSearchSpec;

/**
 * RemoteAppoService
 *
 * @author Minagawa,Kazushi
 *
 */
public interface RemoteAppoService {
    
    /**
     * �\���ۑ��A�X�V�A�폜����B
     * @param spec �\����� DTO
     */
    public void putAppointments(AppointSpec spec);
    
    /**
     * �\�����������B
     * @param spec �����d�l
     * @return �\��� Collection
     */
    public Collection getAppointmentList(ModuleSearchSpec spec);
    
}
