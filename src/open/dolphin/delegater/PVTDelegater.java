/*
 * PVTDelegater.java
 * Copyright (C) 2004 Digital Globe, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package open.dolphin.delegater;

import java.util.ArrayList;
import java.util.Collection;

import javax.naming.NamingException;

import org.apache.log4j.Logger;

import open.dolphin.dto.PatientVisitSpec;
import open.dolphin.ejb.RemotePvtService;
import open.dolphin.infomodel.HealthInsuranceModel;
import open.dolphin.infomodel.PVTHealthInsuranceModel;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.util.BeanUtils;

/**
 * User �֘A�� Business Delegater�@�N���X�B
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class PVTDelegater extends BusinessDelegater {
    
    private Logger logger;

    
    public void setLogger(Logger l) {
        logger = l;
    }
    
    /**
     * ��t��� PatientVisitModel ���f�[�^�x�[�X�ɓo�^����B
     * @param pvtModel   ��t��� PatientVisitModel
     * @param principal  UserId �� FacilityId
     * @return �ۑ��ɐ���������
     */
    public int addPvt(PatientVisitModel pvtModel) {
        
        int retCode = 0;
        
        try {
            retCode = getService().addPvt(pvtModel);
            if (logger != null && logger.isDebugEnabled()) {
                logger.info("��t����ۑ����܂����B");
            }
            
        } catch (Exception e) {
            if (logger != null && logger.isDebugEnabled()) {
                logger.warn("��t���̕ۑ��Ɏ��s���܂���");
                logger.warn(e.toString());
            }
            e.printStackTrace();
            processError(e);
        }
        
        return retCode;
    }
    
    /**
     * ���@�����f�[�^�x�[�X����擾����B
     * @param date     �������闈�@��
     * @param firstRecord ���Ԗڂ̃��R�[�h����擾���邩
     * @return PatientVisitModel �̃R���N�V����
     */
    @SuppressWarnings("unchecked")
    public Collection<PatientVisitModel> getPvt(String[] date, int firstRecord) {
        
        PatientVisitSpec spec = new PatientVisitSpec();
        spec.setDate(date[0]);
        spec.setAppodateFrom(date[1]);
        spec.setAppodateTo(date[2]);
        spec.setSkipCount(firstRecord);
        
        try {
            Collection<PatientVisitModel> ret = getService().getPvt(spec);
            
            for (PatientVisitModel model : ret) {
                PatientModel patient = model.getPatient();
                decodeHealthInsurance(patient);
            }
            
            return ret;
            
        } catch (Exception e) {
            e.printStackTrace();
            processError(e);
        }
        
        return null;
    }
    
    
    /**
     * �o�C�i���̌��N�ی��f�[�^���I�u�W�F�N�g�Ƀf�R�[�h����B
     * @param patient ���҃��f��
     */
    private void decodeHealthInsurance(PatientModel patient) {
        
        // Health Insurance ��ϊ������� beanXML2PVT
        Collection<HealthInsuranceModel> c = patient.getHealthInsurances();
        
        if (c != null) {
            
            ArrayList<PVTHealthInsuranceModel> list = new ArrayList<PVTHealthInsuranceModel>(c.size());
            
            for (HealthInsuranceModel model : c) {
                try {
                    // byte[] �� XMLDecord
                    PVTHealthInsuranceModel hModel = (PVTHealthInsuranceModel)BeanUtils.xmlDecode(model.getBeanBytes());
                    list.add(hModel);
                } catch (Exception e) {
                    e.printStackTrace();
                    processError(e);
                }
            }
            
            patient.setPvtHealthInsurances(list);
            patient.getHealthInsurances().clear();
            patient.setHealthInsurances(null);
        }
    }
    
    public int removePvt(long id) {
        try {
            return getService().removePvt(id);
        } catch (Exception e) {
            e.printStackTrace();
            processError(e);
        }
        return 0;
    }
    
    public int updatePvtState(long pk, int state) {
        try {
            return getService().updatePvtState(pk, state);
        } catch (Exception e) {
            e.printStackTrace();
            processError(e);
        }
        return 0;
    }
    
    private RemotePvtService getService() throws NamingException {
        return (RemotePvtService)getService("RemotePvtService");
    }
}
