package open.dolphin.table;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * ObjectReflectTableModel
 *
 * @author Minagawa,Kazushi
 */
public class ObjectReflectTableModel extends AbstractTableModel {
    
    private static final long serialVersionUID = -8280948755982277457L;
    
    // �J�������z��
    private String[] columnNames;
    
    // �����l���擾���邽�߂̃��\�b�h��
    private String[] methodNames;
    
    // �J�����N���X�z��
    private Class[] columnClasses;
    
    // �J�n���s��
    private int startNumRows;
    
    // �J������
    private int columnCount;
    
    // �f�[�^�I�u�W�F�N�g���X�g
    private List<Object> objectList;
    
    /**
     * ObjectReflectTableModel�𐶐�����B
     * @param columnNames �J�������z��
     * @param startNumRows	�J�n���s��
     * @param methodNames ���\�b�h���z��
     * @param columnClasses �J�����N���X�z��
     */
    public ObjectReflectTableModel(String[] columnNames, int startNumRows,
            String[] methodNames, Class[] columnClasses) {
        this.columnNames = columnNames;
        this.startNumRows = startNumRows;
        this.methodNames = methodNames;
        this.columnClasses = columnClasses;
        if (this.columnNames != null) {
            columnCount = columnNames.length;
        }
        objectList = new ArrayList<Object>();
    }
    
    /**
     * �J�������Ȃ���TableModel�𐶐�����B
     * @param columnCount �J������
     * @param startNumRows �J�n���s��
     * @param methodNames ���\�b�h���z��
     * @param columnClasses �J�����N���X�z��
     */
    public ObjectReflectTableModel(int columnCount, int startNumRows,
            String[] methodNames, Class[] columnClasses) {
        this.columnCount = columnCount;
        this.startNumRows = startNumRows;
        this.methodNames = methodNames;
        this.columnClasses = columnClasses;
        objectList = new ArrayList<Object>();
    }
    
    /**
     * �J��������Ԃ��B
     * @param index �J�����C���f�b�N�X
     */
    public String getColumnName(int index) {
        return (columnNames != null && index < columnNames.length)
        ? columnNames[index]
                : null;
    }
    
    /**
     * �J��������Ԃ��B
     * @return �J������
     */
    public int getColumnCount() {
        return columnCount;
    }
    
    /**
     * �s����Ԃ��B
     * @return �s��
     */
    public int getRowCount() {
        return (objectList != null && objectList.size() > startNumRows) ? objectList
                .size()
                : startNumRows;
    }
    
    /**
     * �J�����̃N���X�^��Ԃ��B
     * @param �J�����C���f�b�N�X
     */
    @SuppressWarnings("unchecked")
    public Class getColumnClass(int index) {
        return (columnClasses != null && index < columnClasses.length) ? columnClasses[index]
                : String.class;
    }
    
    /**
     * �I�u�W�F�N�g�̒l��Ԃ��B
     * @param row �s�C���f�b�N�X
     * @param col�@���ރC���f�b�N�X
     * @return
     */
    public Object getValueAt(int row, int col) {
        
        Object object = getObject(row);
        
        if (object != null && methodNames != null && col < methodNames.length) {
            try {
                Method targetMethod = object.getClass().getMethod(
                        methodNames[col], (Class[])null);
                return targetMethod.invoke(object, (Object[])null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    /**
     * �f�[�^���X�g��ݒ肷��B
     * @param objectList �f�[�^���X�g
     */
    public void setObjectList(List<Object> objectList) {
        if (this.objectList != null) {
            this.objectList.clear();
            this.objectList = null;
        }
        this.objectList = objectList; // �Q�Ƃ��Ă���̂�
        this.fireTableDataChanged();
    }
    
    /**
     * �R���X�g���N�g��ɃJ��������ύX����B
     */
    public void setColumnName(String columnName, int col) {
        if (col >=0 && col < columnNames.length) {
            columnNames[col] = columnName;
            this.fireTableStructureChanged();
        }
    }
    
    /**
     * �R���X�g���N�g��Ƀ��\�b�h��ύX����B
     */
    public void setMethodName(String methodName, int col) {
        if (col >=0 && col < methodNames.length) {
            methodNames[col] = methodName;
            if(objectList != null) {
                this.fireTableDataChanged();
            }
        }
    }
    
    /**
     * �f�[�^���X�g��Ԃ��B
     * @return �f�[�^���X�g
     */
    public List getObjectList() {
        return objectList;
    }
    
    /**
     * �f�[�^���X�g���N���A����B
     */
    public void clear() {
        if (objectList != null) {
            objectList.clear();
            this.fireTableDataChanged();
        }
    }
    
    /**
     * �w�肳�ꂽ�s�̃I�u�W�F�N�g��Ԃ��B
     * @param index �s�C���f�b�N�X
     * @return �I�u�W�F�N�g
     */
    public Object getObject(int index) {
        return (objectList != null && index >= 0 && index < objectList.size()) ? objectList
                .get(index)
                : null;
    }
    
    /**
     * �I�u�W�F�N�g��(=�f�[�^��)��Ԃ�
     * @return �I�u�W�F�N�g��
     */
    public int getObjectCount() {
        return objectList != null ? objectList.size() : 0;
    }
    
    // //////// �f�[�^�ǉ��폜�̊ȈՃT�|�[�g /////////
    
    public void addRow(Object add) {
        if (add != null) {
            if (objectList == null) {
                objectList = new ArrayList<Object>();
            }
            int index = objectList.size();
            objectList.add(add);
            this.fireTableRowsInserted(index, index);
        }
    }
    
    public void addRow(int index, Object add) {
        if (add != null && index > -1 && objectList != null) {
            if ( (objectList.size() == 0 && index == 0) || (index < objectList.size()) ){
                objectList.add(index, add);
                this.fireTableRowsInserted(index, index);
            }
        }
    }
    
    public void insertRow(int index, Object o) {
        addRow(index, o);
    }
    
    public void moveRow(int from, int to) {
        if (! isValidRow(from) || ! isValidRow(to)) {
            return;
        }
        if (from == to) {
            return;
        }
        Object o = objectList.remove(from);
        objectList.add(to, o);
        fireTableRowsUpdated(0, getObjectCount());
    }
    
    public void addRows(Collection c) {
        if (c != null) {
            if (objectList == null) {
                objectList = new ArrayList<Object>();
            }
            int first = objectList.size();
            for (Iterator iter = c.iterator(); iter.hasNext(); ) {
                objectList.add(iter.next());
            }
            int last = objectList.size() - 1;
            this.fireTableRowsInserted(first, last);
        }
    }
    
    public void deleteRow(int index) {
        if (index > -1 && index < objectList.size()) {
            objectList.remove(index);
            this.fireTableRowsDeleted(index, index);
        }
    }
    
    public void deleteRow(Object delete) {
        if (objectList != null) {
            if (objectList.remove(delete)) {
                this.fireTableDataChanged();
            }
        }
    }
    
    public void deleteRows(Collection c) {
        if (objectList != null) {
            if (c != null) {
                objectList.removeAll(c);
                this.fireTableDataChanged();
            }
        }
    }
    
    public int getIndex(Object o) {
        int index = 0;
        boolean found = false;
        if (objectList != null && o != null) {
            for (Object obj : objectList) {
                if (obj == o) {
                    found = true;
                    break;
                } else {
                    index++;
                }
            }
        }
        return found ? index : -1;
    }
    
    public boolean isValidRow(int row) {
        return ( (objectList != null) && (row > -1) && (row < objectList.size()) ) ? true : false;
    }
}
