package open.dolphin.infomodel;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * MemoModel
 *
 * @author Minagawa, Kazushi
 *
 */
@Entity
@Table(name = "d_patient_memo")
public class PatientMemoModel extends KarteEntryBean implements java.io.Serializable {

    // DolphinPro @Lob�A�m�e�[�V����������
    // OpenDolphin �A�m�e�[�V�����Ȃ�
    @Lob
    private String memo;
    
    public String getMemo() {
        return memo;
    }
    
    public void setMemo(String memo) {
        this.memo = memo;
    }
}
