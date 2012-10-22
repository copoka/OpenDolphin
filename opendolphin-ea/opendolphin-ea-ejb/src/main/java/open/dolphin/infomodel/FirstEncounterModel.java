package open.dolphin.infomodel;

import javax.persistence.*;

/**
 * ���f�����N���X�B
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="docType",
    discriminatorType=DiscriminatorType.STRING
)
@Table(name = "d_first_encounter")
public class FirstEncounterModel extends KarteEntryBean implements java.io.Serializable {
        
    //@Lob ASP �T�[�o�֔z�����鎞�A�R�����g�A�E�g���Ă͂����Ȃ�
    @Column(nullable=false)
    private byte[] beanBytes;
    
    /** Creates a new instance of FirstEncounterModel */
    public FirstEncounterModel() {
    }

    public byte[] getBeanBytes() {
        return beanBytes;
    }

    public void setBeanBytes(byte[] beanBytes) {
        this.beanBytes = beanBytes;
    }
}
