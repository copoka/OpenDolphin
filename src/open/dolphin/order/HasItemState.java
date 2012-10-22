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
 */
public class HasItemState extends AbstractSetTableState {

    public HasItemState(JTable setTable, JButton deleteBtn, JButton clearBtn, JTextField stampNameField) {
        super(setTable, deleteBtn, clearBtn, stampNameField);
    }

    public void enter() {
        clearBtn.setEnabled(true);
        int index = setTable.getSelectedRow();
        Object obj = getTableModel().getObject(index);
        if (obj != null && (!deleteBtn.isEnabled())) {
            deleteBtn.setEnabled(true);
        } else if (obj == null && deleteBtn.isEnabled()) {
            deleteBtn.setEnabled(false);
        }
    }
    
    public boolean isValidModel() {
        return (hasSyugi() && isNumberOk()) ? true : false;
    }
    
    private boolean hasSyugi() {
        
        boolean hasSyugi = false;
        List list = getTableModel().getObjectList();

        // �e�[�u�����C�e���[�g����
        for (Iterator iter = list.iterator(); iter.hasNext();) {

            // �}�X�^�[�A�C�e�������o��
            MasterItem mItem = (MasterItem) iter.next();

            // ��Z���ǂ����𒲂ׂ�
            if (mItem.getClassCode() == ClaimConst.SYUGI) {
                hasSyugi = true;
                break;
            }
        }
        
        return hasSyugi;
    }
    
    private boolean isNumber(String test) {
        
        boolean result = true;
        
        try {
            Float num = Float.parseFloat(test);
            if (num < 0F || num == 0F) {
                result = false;
            }
            
        } catch (Exception e) {
            result = false;
        }
        
        return result;
    }
    
    private boolean isNumberOk() {

        boolean numberOk = true;
        List list = getTableModel().getObjectList();

        // �e�[�u�����C�e���[�g����
        for (Iterator iter = list.iterator(); iter.hasNext();) {

            // �}�X�^�[�A�C�e�������o��
            MasterItem mItem = (MasterItem) iter.next();

            // ��Z�̏ꍇ
            if (mItem.getClassCode() == ClaimConst.SYUGI) {
                // null "" ok
                if (mItem.getNumber() == null || mItem.getNumber().equals("")) {
                    continue;
                }
                else if (!isNumber(mItem.getNumber())) {
                    numberOk = false;
                    break;
                }
                
            } else {
                // ���i�y�ъ�ނ̏ꍇ�͐��ʂ��`�F�b�N����
                if (!isNumber(mItem.getNumber())) {
                    numberOk = false;
                    break;
                }
            }
        }
        
        return numberOk;
    }
}
