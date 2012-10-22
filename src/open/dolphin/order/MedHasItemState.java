package open.dolphin.order;

import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * ValidState
 *
 * @author Minagawa,Kazushi
 */
public class MedHasItemState extends AbstractMedTableState {

    public MedHasItemState(JTable medTable, JButton deleteBtn, JButton clearBtn,
            JTextField stampNameField, JLabel stateLabel) {
        super(medTable, deleteBtn, clearBtn, stampNameField, stateLabel);
    }

    public void enter() {

        clearBtn.setEnabled(true);
        int index = medTable.getSelectedRow();
        Object obj = getTableModel().getObject(index);
        if (obj != null && (!deleteBtn.isEnabled())) {
            deleteBtn.setEnabled(true);
        } else if (obj == null && deleteBtn.isEnabled()) {
            deleteBtn.setEnabled(false);
        }

        if (!hasMedicine()) {
            stateLabel.setText("���i����͂��Ă��������B");
            return;
        }

        if (!hasAdmin()) {
            stateLabel.setText("�p�@����͂��Ă��������B");
            return;
        }

        if (!isNumberOk()) {
            stateLabel.setText("���ʂ�����������܂���B");
            return;
        }

        stateLabel.setText("�J���e�ɓW�J�ł��܂��B");
    }

    public boolean isValidModel() {
        return (hasMedicine() && hasAdmin() && isNumberOk()) ? true : false;
    }

    private boolean hasMedicine() {

        boolean medicineOk = false;
        List list = getTableModel().getObjectList();

        for (Iterator iter = list.iterator(); iter.hasNext();) {
            MasterItem mItem = (MasterItem) iter.next();
            if (mItem.getClassCode() == ClaimConst.YAKUZAI) {
                medicineOk = true;
                break;
            }
        }

        return medicineOk;
    }

    private boolean hasAdmin() {

        boolean adminOk = false;
        List list = getTableModel().getObjectList();

        for (Iterator iter = list.iterator(); iter.hasNext();) {
            MasterItem mItem = (MasterItem) iter.next();
            if (mItem.getClassCode() == ClaimConst.ADMIN) {
                adminOk = true;
                break;
            }
        }

        return adminOk;
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

            // ��ނ܂��͈��i�̏ꍇ�A���ʂ𒲂ׂ�
            if (mItem.getClassCode() == ClaimConst.YAKUZAI || mItem.getClassCode() == ClaimConst.ZAIRYO) {

                if (!isNumber(mItem.getNumber().trim())) {
                    numberOk = false;
                    break;
                }

            } else if (mItem.getClassCode() == ClaimConst.ADMIN) {
                // �o���h�����𒲂ׂ�
                if (!isNumber(mItem.getBundleNumber().trim())) {
                    numberOk = false;
                    break;
                }
                
            } else if (mItem.getClassCode() == ClaimConst.SYUGI) {
                
                // ��Z�̏ꍇ null "" ��
                if (mItem.getNumber() == null || mItem.getNumber().equals("")) {
                    continue;
                }
                
                if (!isNumber(mItem.getNumber().trim())) {
                    numberOk = false;
                    break;
                }
            }
        }

        return numberOk;
    }
}
