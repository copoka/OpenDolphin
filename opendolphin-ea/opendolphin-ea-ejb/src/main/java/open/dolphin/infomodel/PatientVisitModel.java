package open.dolphin.infomodel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.persistence.*;

/**
 * PatientVisitModel
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
@Entity
@Table(name = "d_patient_visit")
public class PatientVisitModel extends InfoModel implements java.io.Serializable {
    
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

    //----------------------------------------------
    // 2.0 �Œǉ�
    private String deptCode;        // �f�ÉȃR�[�h
    private String deptName;        // �f�ÉȖ�
    private String doctorId;        // ORCA�ł̒S����R�[�h
    private String doctorName;      // �S���㖼
    private String jmariNumber;     // JMARI code
    private String firstInsurance;  // �󂯕t�������N�ی�
    private String memo;

    @Transient
    private String watingTime;      // �҂�����
    //----------------------------------------------
    
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
    public PatientModel getPatientModel() {
        return patient;
    }
    
    /**
     * ���҃��f����ݒ肷��B
     * @param patientModel
     */
    public void setPatientModel(PatientModel patientModel) {
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
//        // 1.3 �܂ł̎b��
//        String[] tokens = tokenizeDept(department);
//        return tokens[0];

        // 2.0 ����
        return department;
    }
    
    /**
     * department �� , �ŕ�������
     */
    private String[] tokenizeDept(String dept) {
        
        // �f�ÉȖ��A�R�[�h�A�S���㖼�A�S����R�[�h�AJMARI �R�[�h
        // ���i�[����z��𐶐�����
        String[] ret = new String[5];
        Arrays.fill(ret, null);
        
        if (dept != null) {
            try {
                String[] params = dept.split(",");
                System.arraycopy(params, 0, ret, 0, params.length);
            } catch (Exception e) { 
                e.printStackTrace(System.err);
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
        return getPatientModel().getPatientId();
    }
    
    /**
     * ���Ҏ�����Ԃ��B
     * @return ���Ҏ���
     */
    public String getPatientName() {
        return getPatientModel().getFullName();
    }
    
    /**
     * ���Ґ��ʐ�����Ԃ��B
     * @return ���ʐ���
     */
    public String getPatientGenderDesc() {
        return ModelUtils.getGenderDesc(getPatientModel().getGender());
    }
    
    /**
     * ���҂̔N��Ɛ��N�����̕\����Ԃ��B
     * @return ���҂̔N��Ɛ��N����
     */
    public String getPatientAgeBirthday() {
        return ModelUtils.getAgeBirthday(getPatientModel().getBirthday());
    }
    
    /**
     * ���҂̐��N�����̕\����Ԃ��B
     * @return ���҂̔N��Ɛ��N����
     */
    public String getPatientBirthday() {
        return getPatientModel().getBirthday();
    }

    public void setInsuranceUid(String insuranceUid) {
        this.insuranceUid = insuranceUid;
    }
    
    public String getInsuranceUid() {
        return insuranceUid;
    }

    //-----------------------------------------------------
    public String getDeptName() {
        if (deptName!=null) {
            return deptName;
        }
        // 2.0 �ȑO�̃��R�[�h
        String[] tokens = tokenizeDept(department);
        return tokens[0];
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getDeptCode() {
        if (deptCode!=null) {
            return deptCode;
        }
        // 2.0 �ȑO�̃��R�[�h
        String[] tokens = tokenizeDept(department);
        return tokens[1];
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getDoctorName() {
        if (doctorName!=null) {
            return doctorName;
        }
        // 2.0 �ȑO�̃��R�[�h
        String[] tokens = tokenizeDept(department);
        return tokens[2];
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorId() {
        if (doctorId!=null) {
            return doctorId;
        }
        // 2.0 �ȑO�̃��R�[�h
        String[] tokens = tokenizeDept(department);
        return tokens[3];
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }
    
    public String getJmariNumber() {
        if (jmariNumber!=null) {
            return jmariNumber;
        }
        // 2.0 �ȑO�̃��R�[�h
        String[] tokens = tokenizeDept(department);
        return tokens[4];
    }

    public void setJmariNumber(String jmariNumber) {
        this.jmariNumber = jmariNumber;
    }

    public String getFirstInsurance() {
        return firstInsurance;
    }

    public void setFirstInsurance(String firstInsurance) {
        this.firstInsurance = firstInsurance;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    //---------------------------------------------------
    // �󂯕t�����ی���Ԃ�
    //---------------------------------------------------
    public String getHealthInsuranceInfo() {

        String uuid = getInsuranceUid();
        if (uuid == null) {
            return null;
        }

        List<PVTHealthInsuranceModel> list = getPatientModel().getPvtHealthInsurances();
        if (list == null || list.isEmpty()) {
            return null;
        }

        StringBuilder info = new StringBuilder();
        for (PVTHealthInsuranceModel pm : list) {
            if (pm.getGUID()!=null && uuid.equals(pm.getGUID())) {

                info.append(pm.getInsuranceClass());
                PVTPublicInsuranceItemModel[] pbs = pm.getPVTPublicInsuranceItem();
                if (pbs!=null) {
                    for (PVTPublicInsuranceItemModel pb : pbs) {
                        info.append(":");
                        info.append(pb.getProviderName());
                    }
                }

                break;
            }
        }

        return info.length()>0 ? info.toString() : null;
    }


    //---------------------------------------------------
    //              �҂����ԕ\��
    //---------------------------------------------------
    public String getWatingTime() {
        return watingTime;
    }

    public void setWatingTime(String watingTime) {
        this.watingTime = watingTime;
    }

    //---------------------------------------------------
    // �f�ÉȁA�S����AJMARI �R�[�h�̏���Ԃ��B 2.0
    //---------------------------------------------------
    public String getDeptDoctorJmariInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDeptName()).append(",");
        sb.append(getDeptCode()).append(",");
        sb.append(getDoctorName()).append(",");
        sb.append(getDoctorId()).append(",");
        sb.append(getJmariNumber());
        return sb.toString();
    }

    //---------------------------------------------------
    //              Transferable ����
    //---------------------------------------------------
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
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final PatientVisitModel other = (PatientVisitModel) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
