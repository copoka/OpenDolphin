package open.dolphin.order;

import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * ValidState
 *
 * @author Minagawa,Kazushi
 *
 */
public class MedHasItemState extends AbstractMedTableState {
    
    public MedHasItemState(JTable medTable, JButton deleteBtn, JButton clearBtn, JTextField stampNameField, JTextField adminField) {
        super(medTable, deleteBtn, clearBtn, stampNameField, adminField);
    }
    
    public void enter() {
        clearBtn.setEnabled(true);
        int index = medTable.getSelectedRow();
        Object obj = getTableModel().getObject(index);
        if ( obj != null && (!deleteBtn.isEnabled()) ) {
            deleteBtn.setEnabled(true);
        } else if (obj == null && deleteBtn.isEnabled()) {
            deleteBtn.setEnabled(false);
        }
    }
    
    public boolean isValidModel() {
        return (isAdminOk() && isNumberOk()) ? true : false;
    }
    
    private boolean isAdminOk() {
        return adminField.getText().trim().equals("") ? false : true;
        
        // �p�@�ɓK���Ȃ��̂��Ȃ��ꍇ�A�I�����Ȃ��ő��M������
        // test 2007-4-12
        //
        //return true;
    }
    
    private boolean isNumberOk() {
        
        boolean numberOk = true;
        List list = getTableModel().getObjectList();
        
        // �e�[�u�����C�e���[�g����
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            
            // �}�X�^�[�A�C�e�������o��
            MasterItem mItem = (MasterItem) iter.next();
            
            // ��ނ܂��͈��i�̏ꍇ�A���ʂ𒲂ׂ�
            if (mItem.getClassCode() != ClaimConst.SYUGI) {
                
                if ( (mItem.getNumber() != null) && (!mItem.getNumber().trim().equals("")) ) {
                    
                    String number = mItem.getNumber().trim();
                    for (int k = 0; k < number.length(); k++) {
                        
                        int ctype = Character.getType(number.charAt(k));
                        
                        // ���� && . �łȂ�
                        if (ctype != Character.DECIMAL_DIGIT_NUMBER && number.charAt(k) != '.') {
                            numberOk = false;
                            break;
                        }
                    }
                    
                } else {
                    numberOk = false;
                }
            }
        }
        
        return numberOk;
    }
}
