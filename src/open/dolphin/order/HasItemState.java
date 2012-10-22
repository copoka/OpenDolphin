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
public class HasItemState extends AbstractSetTableState {
	
	public HasItemState(JTable setTable, JButton deleteBtn, JButton clearBtn, JTextField stampNameField) {
		super(setTable, deleteBtn, clearBtn, stampNameField);
	}
	
	public void enter() {
		clearBtn.setEnabled(true);
		int index = setTable.getSelectedRow();
		Object obj = getTableModel().getObject(index);
		if ( obj != null && (!deleteBtn.isEnabled()) ) {
			deleteBtn.setEnabled(true);
		} else if (obj == null && deleteBtn.isEnabled()) {
			deleteBtn.setEnabled(false);
		}
	}
	
	public boolean isValidModel() {
		
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
							//setTable.getSelectionModel().addSelectionInterval(i, i);
							break;
						}
					}
					
				} else {
					numberOk = false;
					//setTable.getSelectionModel().addSelectionInterval(i, i);
				}
			}
		}

		return numberOk;
	}
}
