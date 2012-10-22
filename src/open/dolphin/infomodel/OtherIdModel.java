package open.dolphin.infomodel;

/**
 * OtherIdModel
 *
 * @author Minagawa,Kazushi
 *
 */
public class OtherIdModel extends InfoModel {
    
    private static final long serialVersionUID = -8472213236692719666L;
    
    private long id;
    
    private String otherId;
    
    private String idType;
    
    private String idTypeDesc;
    
    private String idTypeCodeSys;
    
    private PatientModel patient;
    
    /**
     * Id��Ԃ��B
     * @return Id
     */
    public long getId() {
        return id;
    }
    
    /**
     * Id��ݒ肷��B
     * @param id Id
     */
    public void setId(long id) {
        this.id = id;
    }
    
    /**
     * @param id
     *            The id to set.
     */
    public void setOtherId(String id) {
        this.otherId = id;
    }
    
    /**
     * @return Returns the id.
     */
    public String getOtherId() {
        return otherId;
    }
    
    /**
     * @param otherIdType
     *            The otherIdType to set.
     */
    public void setIdType(String otherIdType) {
        this.idType = otherIdType;
    }
    
    /**
     * @return Returns the otherIdType.
     */
    public String getIdType() {
        return idType;
    }
    
    /**
     * @param otherIdTypeDesc
     *            The otherIdTypeDesc to set.
     */
    public void setIdTypeDesc(String otherIdTypeDesc) {
        this.idTypeDesc = otherIdTypeDesc;
    }
    
    /**
     * @return Returns the otherIdTypeDesc.
     */
    public String getIdTypeDesc() {
        return idTypeDesc;
    }
    
    /**
     * @param otherIdCodeSys
     *            The otherIdCodeSys to set.
     */
    public void setIdTypeCodeSys(String otherIdCodeSys) {
        this.idTypeCodeSys = otherIdCodeSys;
    }
    
    /**
     * @return Returns the otherIdCodeSys.
     */
    public String getIdTypeCodeSys() {
        return idTypeCodeSys;
    }
    
    /**
     * ���҂�Ԃ��B
     * @return ����
     */
    public PatientModel getPatient() {
        return patient;
    }
    
    /**
     * ���҂�ݒ肷��B
     * @param patient ����
     */
    public void setPatient(PatientModel patient) {
        this.patient = patient;
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
        final OtherIdModel other = (OtherIdModel) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
