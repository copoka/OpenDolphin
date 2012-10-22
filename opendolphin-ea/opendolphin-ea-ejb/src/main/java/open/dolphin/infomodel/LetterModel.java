package open.dolphin.infomodel;

import javax.persistence.*;

/**
 * �Љ�󃂃f���B
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="docType",
    discriminatorType=DiscriminatorType.STRING
)
//@DiscriminatorValue("Letter")
@Table(name = "d_letter")
public class LetterModel extends KarteEntryBean implements java.io.Serializable {
    
    //@Lob // OpenDolphin-1.4 �ł͂��̃A�m�e�[�V�����Ȃ�
    @Column(nullable=false)
    private byte[] beanBytes;
    
    /** Creates a new instance of LetterModel */
    public LetterModel() {
    }
    
    public byte[] getBeanBytes() {
        return beanBytes;
    }

    public void setBeanBytes(byte[] beanBytes) {
        this.beanBytes = beanBytes;
    }
}
