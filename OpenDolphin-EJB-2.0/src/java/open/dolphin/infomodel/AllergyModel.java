package open.dolphin.infomodel;

/**
 * AllergyModel
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class AllergyModel extends InfoModel implements Comparable {
    
    private long observationId;
    
    // �v��
    private String factor;
    
    // �������x
    private String severity;
    
    // �R�[�h�̌n
    private String severityTableId;
    
    // �����
    private String identifiedDate;
    
    // ����
    private String memo;
    
    public String getFactor() {
        return factor;
    }
    
    public void setFactor(String factor) {
        this.factor = factor;
    }
    
    public String getIdentifiedDate() {
        return identifiedDate;
    }
    
    public void setIdentifiedDate(String identifiedDate) {
        this.identifiedDate = identifiedDate;
    }
    
    public String getMemo() {
        return memo;
    }
    
    public void setMemo(String memo) {
        this.memo = memo;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    public String getSeverityTableId() {
        return severityTableId;
    }
    
    public void setSeverityTableId(String severityTableId) {
        this.severityTableId = severityTableId;
    }
    
    /**
     * ������Ŕ�r����B
     * @param other ��r�ΏۃI�u�W�F�N�g
     * @return ��r�l
     */
    @Override
    public int compareTo(Object other) {
        if (other != null && getClass() == other.getClass()) {
            String val1 = getIdentifiedDate();
            String val2 = ((AllergyModel)other).getIdentifiedDate();
            return val1.compareTo(val2);
        }
        return 1;
    }
    
    public long getObservationId() {
        return observationId;
    }
    
    public void setObservationId(long observationId) {
        this.observationId = observationId;
    }
}