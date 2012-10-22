package open.dolphin.infomodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * HealthInsuranceModel
 *
 * @author Minagawa,kazushi
 *
 */
@Entity
@Table(name = "d_health_insurance")
public class HealthInsuranceModel extends InfoModel implements java.io.Serializable {
    
    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
    
    @Lob
    @Column(nullable=false)
    private byte[] beanBytes;
    
    @ManyToOne
    @JoinColumn(name="patient_id", nullable=false)
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
     * BeanXml ��w�Ⴗ��B
     * @param BeanXml�o�C�g�z��
     */
    public void setBeanBytes(byte[] beanBytes) {
        this.beanBytes = beanBytes;
    }
    
    /**
     * BeanXml ��Ԃ��B
     * @return BeanXml�o�C�g�z��
     */
    public byte[] getBeanBytes() {
        return beanBytes;
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
        final HealthInsuranceModel other = (HealthInsuranceModel) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
