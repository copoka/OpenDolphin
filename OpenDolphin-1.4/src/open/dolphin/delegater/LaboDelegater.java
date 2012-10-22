package open.dolphin.delegater;

import java.util.Collection;

import java.util.List;
import javax.naming.NamingException;

import open.dolphin.dto.LaboSearchSpec;
import open.dolphin.ejb.RemoteLaboService;
import open.dolphin.ejb.RemoteNLaboService;
import open.dolphin.infomodel.LaboModuleValue;
import open.dolphin.infomodel.NLaboItem;
import open.dolphin.infomodel.NLaboModule;
import open.dolphin.infomodel.PatientModel;

/**
 * Labo �֘A�� Delegater �N���X�B
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class LaboDelegater extends BusinessDelegater {
    
    /**
     * LaboModule ��ۑ�����B
     * @param laboModuleValue
     * @return LaboImportReply
     */
    public PatientModel putLaboModule(LaboModuleValue value) {
        
        try {
            return getService().putLaboModule(value);
            
        } catch (Exception e) {
            e.printStackTrace();
            processError(e);
        }
        
        return null;
    }
    
    public Collection getLaboModules(LaboSearchSpec spec) {
        
        Collection c = null;
        
        try {
            c = getService().getLaboModuless(spec);
            
        } catch (Exception e) {
            e.printStackTrace();
            processError(e);
        }
        
        return c;
    }

    private RemoteLaboService getService() throws NamingException {
        return (RemoteLaboService) getService("RemoteLaboService");
    }

    //=========================================================
    // �V LabMozule
    //=========================================================
    
    /**
     * �������ʂ�ǉ�����B
     * @param value �ǉ����錟�����W���[��
     * @return      ���҃I�u�W�F�N�g
     */
    public PatientModel putNLaboModule(NLaboModule value) {

        try {
            return getNService().create(value);

        } catch (Exception e) {
            e.printStackTrace();
            processError(e);
        }

        return null;
    }

    /**
     * ���{���W���[������������B
     * @param patientId     �Ώۊ��҂�ID
     * @param firstResult   �擾���ʃ��X�g�̍ŏ��̔ԍ�
     * @param maxResult     �擾���錏���̍ő�l
     * @return              ���{���W���[�����̎���ō~���Ɋi�[�������X�g
     */
    public List<NLaboModule> getLaboTest(String patientId, int firstResult, int maxResult) {
        
        try {
            return getNService().getLaboTest(patientId, firstResult, maxResult);

        } catch (Exception e) {
            e.printStackTrace();
            processError(e);
        }

        return null;
    }

    /**
     * �w�肳�ꂽ�����R�[�h�̌��ʂ��擾����B
     * @param patientId     �Ώۊ���
     * @param firstResult   �S�����̂Ȃ��ōŏ��ɕԂ��ԍ�
     * @param maxResult     �߂��ő匏��
     * @param itemCode      �������錟���R�[�h
     * @return              �������ʍ��ڂ��̎���ō~���Ɋi�[�������X�g
     */
    public List<NLaboItem> getLaboTestItem(String patientId, int firstResult, int maxResult, String itemCode) {
        
        try {
            return getNService().getLaboTestItem(patientId, firstResult, maxResult, itemCode);

        } catch (Exception e) {
            e.printStackTrace();
            processError(e);
        }

        return null;
    }

    //
    // RemoteNLaboService��Ԃ��B
    //
    private RemoteNLaboService getNService() throws NamingException {
        return (RemoteNLaboService) getService("RemoteNLaboService");
    }
}
