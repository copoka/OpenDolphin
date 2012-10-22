/*
 * Created on 2005/02/22
 *
 */
package open.dolphin.client;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.swing.table.AbstractTableModel;

import open.dolphin.infomodel.SimpleDate;


/**
 * CalendarTableModel
 *
 * @author Kazushi Minagawa Digital Globe, Inc.
 *
 */
public class CalendarTableModel extends AbstractTableModel {
    
    private static final String[] COLUMN_NAMES = {
        "��", "��", "��", "��", "��", "��", "�y"
    };
    private String[] columnNames = COLUMN_NAMES;
    private Object[][] data;
    private Collection markDates;
    private int year;
    private int month;
    private int startDay;
    private int firstCell;
    private int lastCell;
    private int numCols = columnNames.length;
    private int numRows;
    private int numDaysOfMonth;
    
    //private GregorianCalendar firstDate;
    //private GregorianCalendar lastDate;
    private GregorianCalendar startDate;
    
    
    /**
     * CalendarTableModel �𐶐�����B
     * @param year   �J�����_�̔N
     * @param month�@ �J�����_�̌�
     */
    public CalendarTableModel(int year, int month) {
        
        this.year = year;
        this.month = month;
        
        // �쐬���錎�̍ŏ��̓�  yyyyMM1
        GregorianCalendar gc = new GregorianCalendar(year, month, 1);
        //firstDate = (GregorianCalendar) gc.clone();
        
        // �ŏ��̓��͏T�̉����ڂ�
        // 1=SUN 6=SAT
        firstCell = gc.get(Calendar.DAY_OF_WEEK);
        firstCell--;  // table �̃Z���ԍ��֕ϊ�����
        
        // ���̌��̓����𓾂�
        numDaysOfMonth = gc.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // ���̌��̍Ō�̓������߂� 1�� + �i����-1�j
        gc.add(Calendar.DAY_OF_MONTH, numDaysOfMonth - 1);
        //lastDate = (GregorianCalendar) gc.clone();
        
        // �Ō�̓��͂��̌��̉��T�ڂ�
        numRows = gc.get(Calendar.WEEK_OF_MONTH);
        
        // ����͏T�̉����ڂ�
        lastCell = gc.get(Calendar.DAY_OF_WEEK);
        lastCell--;
        
        // �P�����̃Z���ԍ��֕ϊ�����
        lastCell += (numRows-1)*numCols; // table �̃Z���ԍ��֕ϊ�����
        
        // ���̃J�����_�̕\���J�n�������߂�
        // ��x����ɖ߂��A���ꂩ�炳��ɃJ�����ԍ����̓���������
        gc.add(Calendar.DAY_OF_MONTH, 1 - numDaysOfMonth);
        gc.add(Calendar.DAY_OF_MONTH, -firstCell);
        startDate = (GregorianCalendar) gc.clone();
        
        startDay = gc.get(Calendar.DAY_OF_MONTH);
        
        // ��̃f�[�^�z��
        data = new Object[numRows][numCols];
    }
    
    public String[] getColumnNames() {
        return columnNames;
    }
    
    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }
    
    public String getColumnName(int col) {
        return columnNames[col];
    }
    
    public int getRowCount() {
        return numRows;
    }
    
    public int getColumnCount() {
        return numCols;
    }
    
    public Object getValueAt(int row, int col) {
        
        // Cell �ԍ��𓾂�
        int cellNumber = row*numCols + col;
        Object ret = null;
        
        // �挎��
        if (cellNumber < firstCell) {
            ret = String.valueOf(startDay + cellNumber);
            
            // ������
        } else if (cellNumber > lastCell) {
            ret = String.valueOf(cellNumber - lastCell);
            
            // ����	�̏ꍇ
        } else {
            // data �z�񂩂���o��
            ret = data[row][col];
            
            // null �łȂ���΂����Ԃ�
            // null �Ȃ����Ԃ�
            if (ret == null) {
                
                return String.valueOf(1 + cellNumber - firstCell);
            }
        }
        
        return ret;
    }
    
    public void setValueAt(Object value, int row, int col) {
        
        int cellNumber = row*numCols + col;
        
        // �挎�܂��͗����̎��͉������Ȃ�
        if ( (cellNumber < firstCell) || (cellNumber > lastCell) ) {
            return;
        }
        
        // �����̏ꍇ�͂����P���ɐݒ肷��
        data[row][col] = value;
    }
    
    public void setMarkDates(Collection c) {
        
        this.markDates = c;
        clear();
        if (markDates != null) {
            Iterator iter = markDates.iterator();
            SimpleDate date = null;
            
            while (iter.hasNext()) {
                date = (SimpleDate)iter.next();
                if ( (year != date.getYear()) || (month != date.getMonth()) ) {
                    continue;
                }
                int day = date.getDay();
                int cellNumber = firstCell + (day-1);
                int row = cellNumber / numCols;
                int col = cellNumber % numCols;
                setValueAt(date, row, col);
            }
        }
        this.fireTableDataChanged();
    }
    
    public Collection getMarkDates() {
        return markDates;
    }
    
    public void clear() {
        data = new Object[numRows][numCols];
    }
    
    public boolean isOutOfMonth(int row, int col) {
        int cellNumber = row*numCols + col;
        return ( (cellNumber < firstCell) || (cellNumber > lastCell) ) ? true : false;
    }
    
    public SimpleDate getFirstDate() {
        return new SimpleDate(year, month, 1);
    }
    
    public SimpleDate getLastDate() {
        return new SimpleDate(year, month, numDaysOfMonth);
    }

    public SimpleDate getDate(int row, int col) {
        int cellNumber = row*numCols + col;
        GregorianCalendar gc = (GregorianCalendar) startDate.clone();
        gc.add(Calendar.DAY_OF_MONTH, cellNumber);
        int y = gc.get(Calendar.YEAR);
        int m = gc.get(Calendar.MONTH);
        int d = gc.get(Calendar.DAY_OF_MONTH);
        return new SimpleDate(y, m, d);
    }
}
