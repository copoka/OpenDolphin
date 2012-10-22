package open.dolphin.infomodel;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.*;

/**
 * KarteEntry
 *
 * @author Minagawa,Kazushi
 *
 */
@MappedSuperclass
public class KarteEntryBean extends InfoModel implements Comparable {
    
    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
    
    @Column(nullable=false)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date confirmed;
    
    @Column(nullable=false)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date started;
    
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date ended;
    
    @Column(nullable=false)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date recorded;
    
    private long linkId;
    
    private String linkRelation;
    
    @Column(length=1, nullable=false)
    private String status;
    
    //@ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne
    @JoinColumn(name="creator_id", nullable=false)
    private UserModel creator;
    
    //@ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne
    @JoinColumn(name="karte_id", nullable=false)
    private KarteBean karte;
    
    
    /**
     * ���̃G���g����Id��Ԃ��B
     * @return Id
     */
    public long getId() {
        return id;
    }
    
    /**
     * ���̃G���g����Id��ݒ肷��B
     * @param id �G���g��Id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * �m�������Ԃ��B
     * @return �m�����
     */
    public Date getConfirmed() {
        return confirmed;
    }

    /**
     * �m�������ݒ肷��B
     * @param confirmed �m�����
     */
    public void setConfirmed(Date confirmed) {
        this.confirmed = confirmed;
    }
    
    /**
     * �K���J�n����Ԃ��B
     * @return �L�^�̓K���J�n��(TimeStamp)
     */    
    public Date getStarted() {
        return started;
    }

    /**
     * �K���J�n����ݒ肷��B
     * @param started �L�^�̓K���J�n��(TimeStamp)
     */
    public void setStarted(Date started) {
        this.started = started;
    }

    /**
     * �K���I������Ԃ��B
     * @return ���̋L�^�̓K���I������
     */
    public Date getEnded() {
        return ended;
    }

    /**
     * �K���I������ݒ肷��B
     * @param ended ���̋L�^�̓K���I������
     */
    public void setEnded(Date ended) {
        this.ended = ended;
    }

    /**
     * �L�^����Ԃ��B
     * @return ���̃G���g���̋L�^����
     */
    public Date getRecorded() {
        return recorded;
    }

    /**
     * �L�^����ݒ肷��B
     * @param recorded ���̃G���g���̋L�^����
     */
    public void setRecorded(Date recorded) {
        this.recorded = recorded;
    }
    
    /**
     * �G���g���̃����N��ID��Ԃ��B
     * @return �G���g���̃����N��ID
     */
    public long getLinkId() {
        return linkId;
    }

    /**
     * �G���g���̃����N��ID��ݒ肷��B
     * @param linkId �G���g���̃����N��ID
     */
    public void setLinkId(long linkId) {
        this.linkId = linkId;
    }
    
    /**
     * �����N��Ƃ̊֌W��Ԃ��B
     * @return �����N��Ƃ̊֌W
     */
    public String getLinkRelation() {
        return linkRelation;
    }

    /**
     * �����N��Ƃ̊֌W��ݒ肷��B
     * @param linkRelation �����N��Ƃ̊֌W
     */
    public void setLinkRelation(String linkRelation) {
        this.linkRelation = linkRelation;
    }
        
    /**
     * ���̃G���g���̃X�e�[�^�X��Ԃ��B
     * @return �X�e�[�^�X
     */
    public String getStatus() {
        return status;
    }

    /**
     * ���̃G���g���̃X�e�[�^�X��ݒ肷��B
     * @param status �X�e�[�^�X
     */
    public void setStatus(String status) {
        this.status = status;
    }

    
    /**
     * Creator��Ԃ��B
     * @return Creator (�V�X�e���̃��[�U)
     */
    public UserModel getUserModel() {
        return creator;
    }
    
    /**
     * Creator ��ݒ肷��B
     * @param creator ���̃G���g���̋L�q��
     */
    public void setUserModel(UserModel creator) {
        this.creator = creator;
    }
    
    /**
     * �J���e��Ԃ��B
     * @return Karte
     */
    public KarteBean getKarteBean() {
        return karte;
    }
    
    /**
     * �J���e��ݒ肷��B
     * @param karte Karte
     */
    public void setKarteBean(KarteBean karte) {
        this.karte = karte;
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
        final KarteEntryBean other = (KarteEntryBean) obj;
        if (id != other.id)
            return false;
        return true;
    }
    
    /**
     * �K���J�n���Ɗm����Ŕ�r����B
     * @return Comparable �̔�r�l
     */    
    @Override
    public int compareTo(Object other) {
        if (other != null && getClass() == other.getClass()) {
            Date date1 = getStarted();
            Date date2 = ((KarteEntryBean) other).getStarted();
            int result = compareDate(date1, date2);
            if (result == 0) {
                date1 = getConfirmed();
                date2 = ((KarteEntryBean) other).getConfirmed();
                result = compareDate(date1, date2);
            }
            return result;
        }
        return -1;
    }
    
    private int compareDate(Date date1, Date date2) {
        if (date1 != null && date2 == null) {
            return -1;
        } else if (date1 == null && date2 != null) {
            return 1;
        } else if (date1 == null && date2 == null) {
            return 0;
        } else {
            return date1.compareTo(date2);
        }
    }
    
    //
    // �݊����p�̃v���L�V�R�[�h
    //
    public Date getFirstConfirmed() {
        return getStarted();
    }
    
    public void setFirstConfirmed(Date firstConfirmed) {
        setStarted(firstConfirmed);
    }
    
    public String getFirstConfirmDate() {
        return ModelUtils.getDateTimeAsString(getFirstConfirmed());
    }
    
    public void setFirstConfirmDate(String timeStamp) {
        setFirstConfirmed(ModelUtils.getDateTimeAsObject(timeStamp));
    }
    
    public String getConfirmDate() {
        return ModelUtils.getDateTimeAsString(getConfirmed());
    }
    
    public void setConfirmDate(String timeStamp) {
        setConfirmed(ModelUtils.getDateTimeAsObject(timeStamp));
    }
    
      
    //
    // ����R�[�h  Date
    //
    public String firstConfirmDateAsString() {
        return dateAsString(getFirstConfirmed());
    }
    
    public String confirmDateAsString() {
        return dateAsString(getConfirmed());
    }
    
    public String startedDateAsString() {
        return dateAsString(getStarted());
    }
    
    public String endedDateAsString() {
        return dateAsString(getEnded());
    }
    
    public String recordedDateAsString() {
        return dateAsString(getRecorded());
    }
    
    private String dateAsString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_WITHOUT_TIME);
        return sdf.format(date);
    }
    
    //
    // ����R�[�h  TimeStamp
    //
    public String confirmedTimeStampAsString() {
        return timeStampAsString(getConfirmed());
    }
    
    public String startedTimeStampAsString() {
        return timeStampAsString(getStarted());
    }
    
    public String endedTimeStampAsString() {
        return timeStampAsString(getEnded());
    }
    
    public String recordedTimeStampAsString() {
        return timeStampAsString(getRecorded());
    }
    
    private String timeStampAsString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_DATE_FORMAT);
        return sdf.format(date);
    }

    //------------------------------------------------------------
    public UserModel getCreator() {
        return getUserModel();
    }

    public void setCreator(UserModel creator) {
        setUserModel(creator);
    }

    public KarteBean getKarte() {
        return getKarteBean();
    }

    public void setKarte(KarteBean karte) {
        setKarteBean(karte);
    }
    //------------------------------------------------------------
}


