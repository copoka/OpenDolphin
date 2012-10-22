package open.dolphin.ejb;

import java.util.Collection;

import open.dolphin.dto.LaboSearchSpec;
import open.dolphin.infomodel.LaboModuleValue;
import open.dolphin.infomodel.PatientModel;

/**
 * RemoteLaboService
 *
 * @author Minagawa,Kazushi
 */
public interface RemoteLaboService {
    
    /**
     * LaboModule��ۑ�����B
     * @param laboModuleValue LaboModuleValue
     */
    public PatientModel putLaboModule(LaboModuleValue laboModuleValue);
    
    /**
     * ���҂̌��̌������W���[�����擾����B
     * @param spec LaboSearchSpec �����d�l
     * @return laboModule �� Collection
     */
    public Collection getLaboModuless(LaboSearchSpec spec);
    
}
