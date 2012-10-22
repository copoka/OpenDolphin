package open.dolphin.infomodel;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * KarteBean
 *
 * @author Minagawa,Kazushi
 *
 */
@Entity
@Table(name = "d_karte")
public class KarteBean extends InfoModel {
    
    private static final long serialVersionUID = 4658519288418950016L;
    
    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
    
    @ManyToOne
    @JoinColumn(name="patient_id", nullable=false)
    private PatientModel patient;
    
    @Transient
    private Map<String, List> entries;
    
    @Column(nullable=false)
    @Temporal(value = TemporalType.DATE)
    private Date created;
    
    /**
     * Id��Ԃ��B
     * @return Id
     */
    public long getId() {
        return id;
    }
    
    /**
     * Id��ݒ肷��B
     * @param id �J���e��Id
     */
    public void setId(long id) {
        this.id = id;
    }
    
    /**
     * ���҂�Ԃ��B
     * @return ���̃J���e�̃I�[�i�[
     */
    public PatientModel getPatient() {
        return patient;
    }
    
    /**
     * ���҂�ݒ肷��B
     * @param patient ���̃J���e�̃I�[�i�[
     */
    public void setPatient(PatientModel patient) {
        this.patient = patient;
    }
    
    /**
     * ���̃J���e�̍쐬����Ԃ��B
     * @return �J���e�̍쐬��
     */
    public Date getCreated() {
        return created;
    }
    
    /**
     * ���̃J���e�̍쐬����ݒ肷��B
     * @param created �J���e�̍쐬��
     */
    public void setCreated(Date created) {
        this.created = created;
    }
    
    /**
     * �J���e�̃G���g����Ԃ��B
     * @return �J�e�S����Key�A�G���g���̃R���N�V������Value�ɂ���HashMap
     */
    public Map<String, List> getEntries() {
        return entries;
    }
    
    /**
     * �J���e�̃G���g����ݒ肷��B
     * param entries �J�e�S����Key�A�G���g���̃R���N�V������Value�ɂ���HashMap
     */
    public void setEntries(Map<String, List> entries) {
        this.entries = entries;
    }
    
    /**
     * �w�肵���J�e�S���̃G���g���R���N�V������Ԃ��B
     * @param category �J�e�S��
     * @return�@�G���g���̃R���N�V����
     */
    public List getEntryCollection(String category) {
        return entries != null ? entries.get(category) : null;
    }
    
    /**
     * �J�e�S���Ƃ��̃G���g���̃R���N�V������ǉ�����B
     * @param category �J�e�S��
     * @param entries �J�e�S���̃G���g���[�̃R���N�V����
     */
    public void addEntryCollection(String category, List entrs) {
        
        if (entries == null) {
            entries = new HashMap<String, List>();
        }
        entries.put(category, entrs);
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
        final KarteBean other = (KarteBean) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
