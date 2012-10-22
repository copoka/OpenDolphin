package open.dolphin.delegater;

import java.util.Collection;
import java.util.Iterator;

import javax.naming.NamingException;

import open.dolphin.dto.PatientSearchSpec;
import open.dolphin.ejb.RemotePatientService;
import open.dolphin.infomodel.HealthInsuranceModel;
import open.dolphin.infomodel.PVTHealthInsuranceModel;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.util.BeanUtils;

/**
 * ���Ҋ֘A�� Business Delegater�@�N���X�B
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class  PatientDelegater extends BusinessDelegater {
    
    /**
     * ���ҏ���ۑ�����B
     * @param patientValue PatientValue
     * @return Result Code
     */
    public long putPatient(PatientModel patientValue) {
        
        try {
            return getService().addPatient(patientValue);
            
        } catch (Exception e) {
            e.printStackTrace();
            processError(e);
        }
        
        return 0L;
    }
    
    /**
     * ���ҏ����������ĕԂ��B
     * @param id ����ID
     * @return PatientValue
     */
    public PatientModel getPatient(String qId) {
        
        PatientModel ret = null;
        
        try {
            ret = getService().getPatient(qId);
            decodeHealthInsurance(ret);
            
        } catch (Exception e) {
            e.printStackTrace();
            processError(e);
        }
        
        return ret;
    }
    
    /**
     * ���ҏ����������ĕԂ��B
     * @param spec PatientSearchSpec �����d�l
     * @return PatientValue �� Collection
     */
    public Collection getPatients(PatientSearchSpec spec) {
        
        Collection ret= null;
        
        try {
            ret = getService().getPatients(spec);
            for (Iterator iter=ret.iterator(); iter.hasNext(); ) {
                PatientModel patient = (PatientModel)iter.next();
                decodeHealthInsurance(patient);
            }
        } catch (Exception e) {
            e.printStackTrace();
            processError(e);
        }
        
        return ret;
    }
    
    /**
     * �o�C�i���̌��N�ی��f�[�^���I�u�W�F�N�g�Ƀf�R�[�h����B
     * @param patient ���҃��f��
     */
    private void decodeHealthInsurance(PatientModel patient) {
        
        // Health Insurance ��ϊ������� beanXML2PVT
        Collection<HealthInsuranceModel> c = patient.getHealthInsurances();
        
        if (c != null) {
            
            for (HealthInsuranceModel model : c) {
                try {
                    // byte[] �� XMLDecord
                    PVTHealthInsuranceModel hModel = (PVTHealthInsuranceModel)BeanUtils.xmlDecode(model.getBeanBytes());
                    patient.addPvtHealthInsurance(hModel);
                } catch (Exception e) {
                    e.printStackTrace();
                    processError(e);
                }
            }
            
            c.clear();
            patient.setHealthInsurances(null);
            
        }
    }
    
    /**
     * ���҃������X�V����B
     * @param patient ����
     * @return
     */
    public int updatePatient(PatientModel patient) {
        
        int retCode = -1;
        
        try {
            retCode = getService().update(patient);
        } catch (Exception e) {
            e.printStackTrace();
            processError(e);
        }
        
        return retCode;
    }
    
    private RemotePatientService getService() throws NamingException {
        return (RemotePatientService) getService("RemotePatientService");
    }
}
