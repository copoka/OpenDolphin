
/*
 * Created on 2004/02/03
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2003 Digital Globe, Inc. All rights reserved.
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
package open.dolphin.infomodel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * PatientVisitModel
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
@Entity
@Table(name = "d_patient_visit")
public class PatientVisitModel extends InfoModel  {
    
    private static final long serialVersionUID = 7049490761810599245L;
    
    public static final DataFlavor PVT_FLAVOR =
            new DataFlavor(open.dolphin.infomodel.PatientVisitModel.class, "Patient Visit");
    
    public static DataFlavor flavors[] = {PVT_FLAVOR};
    
    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
    
    /** ���� */
    @ManyToOne
    @JoinColumn(name="patient_id", nullable=false)
    private PatientModel patient;
    
    /** �{��ID  */
    @Column(nullable=false)
    private String facilityId;
    
    /** ��t���X�g��̔ԍ� */
    @Transient
    private int number;
    
    /** ���@���� */
    @Column(nullable=false)
    private String pvtDate;
    
    /** �\�� */
    @Transient
    private String appointment;
    
    /** �f�É� */
    private String department;
    
    /** �I���t���O */
    private int status;
    
    /** ���N�ی�GUID 2006-05-01 */
    private String insuranceUid;
    
    /**
     * PatientVisitModel�I�u�W�F�N�g�𐶐�����B
     */
    public PatientVisitModel() {
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    /**
     * ���҃��f����Ԃ��B
     * @return ���҃��f��
     */
    public PatientModel getPatient() {
        return patient;
    }
    
    /**
     * ���҃��f����ݒ肷��B
     * @param patientModel
     */
    public void setPatient(PatientModel patientModel) {
        this.patient = patientModel;
    }
    
    /**
     * �{��ID��Ԃ��B
     * @return �{��ID
     */
    public String getFacilityId() {
        return facilityId;
    }
    
    /**
     * �{��ID��ݒ肷��B
     * @param facilityId �{��ID
     */
    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }
    
    
    /**
     * ���X�g�ԍ���ݒ肷��B
     * @param number ���X�g�ԍ�
     */
    public void setNumber(int number) {
        this.number = number;
    }
    
    /**
     * ���X�g�ԍ���Ԃ��B
     * @return ���X�g�ԍ�
     */
    public int getNumber() {
        return number;
    }
    
    /**
     * ���@������ݒ肷��B
     * @param time ���@���� yyyy-MM-ddTHH:mmss
     */
    public void setPvtDate(String time) {
        this.pvtDate = time;
    }
    
    /**
     * ���@������Ԃ��B
     * @return ���@���� yyyy-MM-ddTHH:mmss
     */
    public String getPvtDate() {
        return pvtDate;
    }
    
    /**
     * ���@�����̓��t������Ԃ��B
     * @return ���@�����̓��t����
     */
    public String getPvtDateTrimTime() {
        return ModelUtils.trimTime(pvtDate);
    }
    
    /**
     * ���@�����̎��ԕ�����Ԃ��B
     * @return ���@�����̎��ԕ���
     */
    public String getPvtDateTrimDate() {
        return ModelUtils.trimDate(pvtDate);
    }
    
    public String getAppointment() {
        return appointment;
    }
    
    /**
     * �\�񂪂��邩�ǂ�����ݒ肷��B
     * @param appointment �\�񂪂��鎞 true
     */
    public void setAppointment(String appointment) {
        this.appointment = appointment;
    }
    
    /**
     * ��t�f�ÉȂ�ݒ肷��B
     * @param department ��t�f�É�
     */
    public void setDepartment(String department) {
        this.department = department;
    }
    
    /**
     * ��t�f�ÉȂ�Ԃ��B
     * @return ��t�f�ÉȖ�
     */
    public String getDepartment() {
        // 1.3 �܂ł̎b��
        String[] tokens = tokenizeDept(department);
        return tokens[0];
    }
    
    /**
     * ��t�f�ÉȃR�[�h��Ԃ��B
     * @return ��t�f�É�
     */
    public String getDepartmentCode() {
        // 1.3 �܂ł̎b��
        String[] tokens = tokenizeDept(department);
        return tokens[1];
    }
    
    /**
     * �S�����Ԃ��B
     * @return �S���㖼
     */
    public String getAssignedDoctorName() {
        // 1.3 �܂ł̎b��
        String[] tokens = tokenizeDept(department);
        return tokens[2];
    }
    
    /**
     * �S����ID��Ԃ��B
     * @return �S����ID
     */
    public String getAssignedDoctorId() {
        // 1.3 �܂ł̎b��
        String[] tokens = tokenizeDept(department);
        return tokens[3];
    }
    
    /**
     * JMARI �R�[�h��Ԃ��B
     * @return JMARI �R�[�h
     */
    public String getJmariCode() {
        // 1.3 �܂ł̎b��
        String[] tokens = tokenizeDept(department);
        return tokens[4];
    }
    
    public String getDeptNoTokenize() {
        return department;
    }
    
    /**
     * department �� , �ŕ�������
     */
    private String[] tokenizeDept(String dept) {
        
        // �f�ÉȖ��A�R�[�h�A�S���㖼�A�S����R�[�h�AJMARI �R�[�h
        // ���i�[����z��𐶐�����
        String[] ret = new String[5];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = null;
        }
        
        if (dept != null) {
            int index = 0; 
            try {
                StringTokenizer st = new StringTokenizer(dept, ",");
                while (st.hasMoreTokens()) {
                    ret[index++] = st.nextToken();
                }
            } catch (Exception e) { 
                e.printStackTrace();
            }
        }
        
        return ret;
    }
    
    /**
     * �J���e�̏�Ԃ�ݒ肷��B
     * @param state �J���e�̏��
     */
    public void setState(int state) {
        this.status = state;
    }
    
    /**
     * �J���e�̏�Ԃ�Ԃ��B
     * @return �J���e�̏��
     */
    public int getState() {
        return status;
    }
    
    public Integer getStateInteger() {
        return new Integer(status);
    }
    
    /**
     * ����ID��Ԃ��B
     * @return ����ID
     */
    public String getPatientId() {
        return getPatient().getPatientId();
    }
    
    /**
     * ���Ҏ�����Ԃ��B
     * @return ���Ҏ���
     */
    public String getPatientName() {
        return getPatient().getFullName();
    }
    
    /**
     * ���Ґ��ʐ�����Ԃ��B
     * @return ���ʐ���
     */
    public String getPatientGenderDesc() {
        return ModelUtils.getGenderDesc(getPatient().getGender());
    }
    
    /**
     * ���҂̔N��Ɛ��N�����̕\����Ԃ��B
     * @return ���҂̔N��Ɛ��N����
     */
    public String getPatientAgeBirthday() {
        return ModelUtils.getAgeBirthday(getPatient().getBirthday());
    }
    
    /**
     * ���҂̐��N�����̕\����Ԃ��B
     * @return ���҂̔N��Ɛ��N����
     */
    public String getPatientBirthday() {
        return getPatient().getBirthday();
    }
    
    public String getPatientAge() {
        return ModelUtils.getAge(getPatient().getBirthday());
    }
    
    /**
     * ���N�ی���񃂃W���[����UUID��ݒ肷��B
     * @param insuranceUid ���N�ی���񃂃W���[����UUID
     */
    public void setInsuranceUid(String insuranceUid) {
        this.insuranceUid = insuranceUid;
    }
    
    /**
     * ���N�ی���񃂃W���[����UUID��Ԃ��B
     * @return ���N�ی���񃂃W���[����UUID
     */
    public String getInsuranceUid() {
        return insuranceUid;
    }
    
    /////////////////// Transferable ���� //////////////////////////
    
    public boolean isDataFlavorSupported(DataFlavor df) {
        return df.equals(PVT_FLAVOR);
    }
    
    public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
        if (df.equals(PVT_FLAVOR)) {
            return this;
        } else throw new UnsupportedFlavorException(df);
    }
    
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
