/*
 * SimpleCalendarPanel.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package open.dolphin.client;

import java.util.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import javax.swing.table.*;

import open.dolphin.infomodel.AppointmentModel;
import open.dolphin.infomodel.ModelUtils;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.util.*;


/**
 * SimpleCalendarPanel
 *
 * @author Kazushi Minagawa
 */
public final class SimpleCalendarPanel extends JPanel implements DragGestureListener, DropTargetListener, DragSourceListener {
    
    private static final long serialVersionUID = 3030024622746649784L;
    
    private String[] columnNames = ClientContext.getStringArray("calendar.day.week");
    
    private int year;
    private int month;
    private int numRows;
    private int firstCol;
    private int lastCol;
    private GregorianCalendar firstDay;
    private GregorianCalendar lastDay;
    private GregorianCalendar today;
    private String birthday;
    
    private JTable table;
    private MedicalEvent[][] days;
    private int rowHeight = ClientContext.getInt("calendar.cell.height");
    private int columnWidth = ClientContext.getInt("calendar.cell.width");
    private int horizontalAlignment = SwingConstants.RIGHT;
    private int autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS;
    
    // Color
    private Color sundayColor = ClientContext.getColor("color.SUNDAY_FORE");
    private Color saturdayColor = ClientContext.getColor("color.SATURDAY_FORE");
    private Color todayBackground = ClientContext.getColor("color.TODAY_BACK");
    private Color calendarBackground = ClientContext.getColor("color.CALENDAR_BACK");
    private Color weekdayColor = ClientContext.getColor("color.WEEKDAY_FORE");
    private Color birthdayColor = ClientContext.getColor("color.BIRTHDAY_BACK");
    
    // Font
    private Font outOfMonthFont = new Font("Dialog", Font.PLAIN, ClientContext.getInt("calendar.font.size.outOfMonth"));
    private Font inMonthFont = new Font("Dialog", Font.PLAIN, ClientContext.getInt("calendar.font.size"));
    
    // DnD
    private DragSource dragSource;
    private int dragRow;
    private int dragCol;
    
    private int relativeMonth;
    
    private IChart context;
    private CareMapDocument parent;
    private boolean dirty;
    
    private JPopupMenu appointMenu;
    private int popedRow;
    private int popedCol;
    
    private String markEvent = "-1";
    
    private PropertyChangeSupport boundSupport;
    
    
    /** Creates new SimpleCalendarPanel*/
    private SimpleCalendarPanel() {
        super(new BorderLayout());
    }
    
    /** Creates new SimpleCalendarPanel*/
    private SimpleCalendarPanel(int n) {
        
        this();
        
        // ��������_�Ƃ������Ό���
        relativeMonth = n;
        
        // Get right now
        today = new GregorianCalendar();
        today.clear(Calendar.MILLISECOND);
        today.clear(Calendar.SECOND);
        today.clear(Calendar.MINUTE);
        today.clear(Calendar.HOUR_OF_DAY);
        GregorianCalendar gc = (GregorianCalendar)today.clone();
        
        // Create requested month calendar
        // Add relative number to create
        gc.add(Calendar.MONTH, n);
        this.year = gc.get(Calendar.YEAR);
        this.month = gc.get(Calendar.MONTH);
        table = createCalendarTable(gc);
        table.addMouseListener(new MouseAdapter() {
            
            public void mouseClicked(MouseEvent e) {
                
                if (e.getClickCount() != 1) {
                    return;
                }
                
                Point p = e.getPoint();
                int row = table.rowAtPoint(p);
                int col = table.columnAtPoint(p);
                if (row != -1 && col != -1) {
                    MedicalEvent evt = days[row][col];
                    if (evt.getMedicalCode() != null) {
                        boundSupport.firePropertyChange(CareMapDocument.SELECTED_DATE_PROP, null, evt.getDisplayDate());
                        
                    } else if (evt.getAppointmentName() != null) {
                        boundSupport.firePropertyChange(CareMapDocument.SELECTED_APPOINT_DATE_PROP, null, evt.getDisplayDate());
                    }
                }
            }
        });
        
        String title = getCalendarTitle();
        this.add(table.getTableHeader(), BorderLayout.NORTH);
        this.add(table, BorderLayout.CENTER);
        this.setBorder(BorderFactory.createTitledBorder(title));
        
        // Adjust cut & try
        Dimension dim = new Dimension(columnWidth*7 + 10, rowHeight*8 + 5);
        this.setPreferredSize(dim);
        this.setMinimumSize(dim);
        this.setMaximumSize(dim);
        
        // Embed popup menu
        appointMenu = PopupMenuFactory.create("appoint.popupMenu", this);
        
        // Table �� DragTarget, ���g�����X�i�ɐݒ肷��
        dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(table, DnDConstants.ACTION_COPY_OR_MOVE, this);
        
        // Table �� DropTarget, ���g�����X�i�ɐݒ肷��
        new DropTarget(table, this);
    }
    
    public void setChartContext(IChart context) {
        this.context = context;
        birthday = context.getPatient().getBirthday().substring(5);
    }
    
    public void setParent(CareMapDocument doc) {
        parent = doc;
    }
    
    public String getCalendarTitle() {
        StringBuffer buf = new StringBuffer();
        buf.append(year);
        buf.append(ClientContext.getString("calendar.title.year"));
        buf.append(month + 1);
        buf.append(ClientContext.getString("calendar.title.month"));
        return buf.toString();
    }
    
    public int getRelativeMonth() {
        return relativeMonth;
    }
    
    public boolean isThisMonth() {
        return relativeMonth == 0 ? true : false;
    }
    
    public void addPropertyChangeListener(String prop, PropertyChangeListener l) {
        if (boundSupport == null) {
            boundSupport = new PropertyChangeSupport(this);
        }
        boundSupport.addPropertyChangeListener(prop, l);
    }
    
    public void removePropertyChangeListener(String propName, PropertyChangeListener l) {
        if (boundSupport == null) {
            boundSupport = new PropertyChangeSupport(this);
        }
        boundSupport.removePropertyChangeListener(propName, l);
    }
    
    public String getFirstDate() {
        return MMLDate.getDate(firstDay);
    }
    
    public String getLastDate() {
        return MMLDate.getDate(lastDay);
    }
    
    /**
     * �\��̂���������X�g�ŕԂ��B
     * @return �\������X�g
     */
    public ArrayList<AppointmentModel> getAppointDays() {
        
        ArrayList<AppointmentModel> results = new ArrayList<AppointmentModel>();
        MedicalEvent event = null;
        AppointmentModel appoint = null;
        
        // 1 �T�ڂ𒲂ׂ�
        for (int col = firstCol; col < 7; col++) {
            event = days[0][col];
            appoint = event.getAppointEntry();
            if (appoint != null && appoint.getName() != null) {
                results.add(appoint);
            }
        }
        
        // 2 �T�ڈȍ~�𒲂ׂ�
        for (int row = 1; row < numRows - 1; row++) {
            for (int col = 0; col < 7; col++) {
                event = days[row][col];
                appoint = event.getAppointEntry();
                if (appoint != null && appoint.getName() != null) {
                    results.add(appoint);
                }
            }
        }
        
        // �Ō�̏T�𒲂ׂ�
        for (int col = 0; col < lastCol + 1; col++) {
            event = days[numRows - 1][col];
            appoint = event.getAppointEntry();
            if (appoint != null && appoint.getName() != null) {
                results.add(appoint);
            }
        }
        
        return results;
    }
    
    /**
     * �X�V���ꂽ�\��̃��X�g��Ԃ��B
     * @return �X�V���ꂽ�\��̃��X�g
     */
    public ArrayList<AppointmentModel> getUpdatedAppoints() {
        
        ArrayList<AppointmentModel> results = new ArrayList<AppointmentModel>();
        MedicalEvent event = null;
        AppointmentModel appoint = null;
        
        // 1 �T�ڂ𒲂ׂ�
        for (int col = firstCol; col < 7; col++) {
            event = days[0][col];
            appoint = event.getAppointEntry();
            if (appoint != null && appoint.getState() != AppointmentModel.TT_NONE) {
                results.add(appoint);
            }
        }
        
        // 2�T�ڈȍ~�𒲂ׂ�
        for (int row = 1; row < numRows - 1; row++) {
            for (int col = 0; col < 7; col++) {
                event = days[row][col];
                appoint = event.getAppointEntry();
                if (appoint != null && appoint.getState() != AppointmentModel.TT_NONE) {
                    results.add(appoint);
                }
            }
        }
        
        // �Ō�̏T�𒲂ׂ�
        for (int col = 0; col < lastCol + 1; col++) {
            event = days[numRows - 1][col];
            appoint = event.getAppointEntry();
            if (appoint != null && appoint.getState() != AppointmentModel.TT_NONE) {
                results.add(appoint);
            }
        }
        
        return results;
    }
    
    public void setModuleList(String event, ArrayList list) {
        
        markEvent = event;
        clearMark();
        
        if (list == null || list.size() == 0) {
            return;
        }
        
        int size = list.size();
        String mkDate = null;
        MedicalEvent me = null;
        int index = 0;
        int[] ymd = null;
        int row = 0;
        int col = 0;
        
        ModuleModel module = null;
        
        for (int i = 0; i < size; i++) {
            
            module = (ModuleModel)list.get(i);
            //mkDate = ModelUtils.getDateAsString(module.getModuleInfo().getConfirmDate());
            mkDate = ModelUtils.getDateAsString(module.getConfirmed());
            index = mkDate.indexOf('T');
            if (index > 0) {
                mkDate = mkDate.substring(0, index);
            }
            ymd = MMLDate.getCalendarYMD(mkDate);
            
            int shiftDay = ymd[2] + (firstCol -1);
            row = shiftDay / 7;
            col = shiftDay % 7;
            
            me = (MedicalEvent)days[row][col];
            me.setMedicalCode(markEvent);
            
            ((AbstractTableModel)table.getModel()).fireTableCellUpdated(row, col);
        }
    }
    
    public void setImageList(String event, ArrayList list) {
        
        markEvent = event;
        clearMark();
        
        if (list == null || list.size() == 0) {
            return;
        }
        
        int size = list.size();
        String mkDate = null;
        MedicalEvent me = null;
        int index = 0;
        int[] ymd = null;
        int row = 0;
        int col = 0;
        
        ImageEntry image = null;
        
        for (int i = 0; i < size; i++) {
            
            image = (ImageEntry)list.get(i);
            mkDate = image.getConfirmDate();
            index = mkDate.indexOf('T');
            if (index > 0) {
                mkDate = mkDate.substring(0, index);
            }
            //System.out.println("PVT date: " + pvtDate);
            ymd = MMLDate.getCalendarYMD(mkDate);
            
            int shiftDay = ymd[2] + (firstCol -1);
            row = shiftDay / 7;
            col = shiftDay % 7;
            
            me = (MedicalEvent)days[row][col];
            me.setMedicalCode(markEvent);
            
            ((AbstractTableModel)table.getModel()).fireTableCellUpdated(row, col);
        }
    }
    
    public void setAppointmentList(ArrayList list) {
        
        // �����ȍ~�̃J�����_�̂݌�������
        if (relativeMonth < 0 ) {
            return;
        }
        
        // ��Ȃ烊�^�[��
        if ( list == null || list.size() == 0 ) {
            return;
        }
        
        // �����ł���Ζ{���̂R���O���猟���A�����łȂ��ꍇ�̓J�����_�̍ŏ��̓����猟������
        String startDate = isThisMonth() ? MMLDate.getDayFromToday(-3) : MMLDate.getDate(firstDay);
        
        // �\������
        int size = list.size();
        for (int i = 0; i < size; i++) {
            AppointmentModel ae = (AppointmentModel)list.get(i);
            ae.setState(AppointmentModel.TT_HAS);
            String date = ModelUtils.getDateAsString(ae.getDate());
            int index = date.indexOf('T');
            if (index > 0) {
                date = date.substring(0, index);
            }
            
            // startDate �ȑO�̏ꍇ�͕\�����Ȃ�
            if (date.compareTo(startDate) < 0 ) {
                continue;
            }
            
            int[] ymd = MMLDate.getCalendarYMD(date);
            
            int shiftDay = ymd[2] + (firstCol -1);
            int row = shiftDay / 7;
            int col = shiftDay % 7;
            
            MedicalEvent me = days[row][col];
            me.setAppointEntry(ae);
            
            ((AbstractTableModel)table.getModel()).fireTableCellUpdated(row, col);
        }
    }
    
    /**
     * ���݂̕\�����N���A����
     */
    private void clearMark() {
        MedicalEvent me = null;
        boolean exit = false;
        //String val = null;
        
        for (int row = 0; row < numRows; row++) {
            
            for (int col = 0; col < 7; col++) {
                
                me = days[row][col];
                
                if (me.isToday()) {
                    exit = true;
                    break;
                    
                } else if (me.getMedicalCode() != null) {
                    
                    me.setMedicalCode(null);
                    ((AbstractTableModel)table.getModel()).fireTableCellUpdated(row, col);
                }
            }
            if (exit) {
                break;
            }
        }
    }
    
    //////////////   Drag Support //////////////////
    
    public void dragGestureRecognized(DragGestureEvent event) {
        
        int row = table.getSelectedRow();
        int col = table.getSelectedColumn();
        if (row == -1 || col == -1) {
            return;
        }
        
        dragRow = row;
        dragCol = col;
        MedicalEvent me = days[row][col];
        AppointmentModel appo = me.getAppointEntry();
        if (appo == null) {
            //System.out.println("No Appoint");
            return;
        }
        Transferable t = new AppointEntryTransferable(appo);
        Cursor cursor = DragSource.DefaultCopyDrop;
        int action = event.getDragAction();
        if (action == DnDConstants.ACTION_MOVE) {
            cursor = DragSource.DefaultMoveDrop;
        }
        
        // Starts the drag
        dragSource.startDrag(event, cursor, t, this);
    }
    
    public void dragDropEnd(DragSourceDropEvent event) {
        
        if (! event.getDropSuccess() || event.getDropAction() == DnDConstants.ACTION_COPY) {
            return;
        }
        
        processCancel(dragRow, dragCol);
    }
    
    public void dragEnter(DragSourceDragEvent event) {
    }
    
    public void dragOver(DragSourceDragEvent event) {
    }
    
    public void dragExit(DragSourceEvent event) {
    }
    
    public void dropActionChanged( DragSourceDragEvent event) {
    }
    
    //////////// Drop Support ////////////////
    
    public void drop(DropTargetDropEvent e) {
        
        if (! isDropAcceptable(e)) {
            e.rejectDrop();
            setDropTargetBorder(false);
            return;
        }
        
        // Transferable ���擾����
        final Transferable tr = e.getTransferable();
        
        // Drop �ʒu�𓾂�
        final Point loc = e.getLocation();
        
        // accept?
        int action = e.getDropAction();
        e.acceptDrop(action);
        //e.getDropTargetContext().dropComplete(true);
        setDropTargetBorder(false);
        
        int row = table.rowAtPoint(loc);
        int col = table.columnAtPoint(loc);
        //System.out.println("row = " + droppedRow + " col = " + droppedCol);
        if (row == -1 || col == -1) {
            e.getDropTargetContext().dropComplete(false);
            return;
        }
        
        // outOfMonth ?
        MedicalEvent evt = days[row][col];
        if (evt.isOutOfMonth()) {
            e.getDropTargetContext().dropComplete(false);
            return;
        }
        
        // �{���ȑO
        if (evt.before(today)) {
            e.getDropTargetContext().dropComplete(false);
            return;
        }
        
        // Drop ����
        AppointmentModel source = null;
        try {
            source = (AppointmentModel)tr.getTransferData(AppointEntryTransferable.appointFlavor);
            
        } catch (Exception ue) {
            System.out.println(ue);
            source = null;
        }
        if (source == null) {
            e.getDropTargetContext().dropComplete(false);
            return;
        }
        
        processAppoint(row, col, source.getName(), source.getMemo());
        
        e.getDropTargetContext().dropComplete(true);
    }
    
    public boolean isDragAcceptable(DropTargetDragEvent evt) {
        return (evt.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) != 0;
    }
    
    public boolean isDropAcceptable(DropTargetDropEvent evt) {
        return (evt.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) != 0;
    }
    
    /** DropTaregetListener interface method */
    public void dragEnter(DropTargetDragEvent e) {
        if (! isDragAcceptable(e)) {
            e.rejectDrag();
        }
    }
    
    /** DropTaregetListener interface method */
    public void dragExit(DropTargetEvent e) {
        setDropTargetBorder(false);
    }
    
    /** DropTaregetListener interface method */
    public void dragOver(DropTargetDragEvent e) {
        if (isDragAcceptable(e)) {
            setDropTargetBorder(true);
        }
    }
    
    /** DropTaregetListener interface method */
    public void dropActionChanged(DropTargetDragEvent e) {
        if (! isDragAcceptable(e)) {
            e.rejectDrag();
        }
    }
    
    private void setDropTargetBorder(final boolean b) {
        Color c = b ? GUIFactory.getDropOkColor() : this.getBackground();
        table.setBorder(BorderFactory.createLineBorder(c, 2));
    }
    
    
    /**
     * �J�����_�[�e�[�u���𐶐�����
     */
    private JTable createCalendarTable(GregorianCalendar gc) {
        
        days = createDays(gc);
        
        AbstractTableModel model = new AbstractTableModel() {
            
            private static final long serialVersionUID = -6437119956252935580L;
            
            public int getRowCount() {
                return days.length;
            }
            
            public int getColumnCount() {
                return days[0].length;
            }
            
            public Object getValueAt(int row, int col) {
                return days[row][col];
            }
            
            public String getColumnName(int col) {
                return columnNames[col];
            }
            
            @SuppressWarnings("unchecked")
            public Class getColumnClass(int col) {
                return java.lang.String.class;
            }
            
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        
        // Basic settings
        JTable tbl = new JTable(model);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.setCellSelectionEnabled(true);
        tbl.setAutoResizeMode(autoResizeMode);
        tbl.setBackground(calendarBackground);
        
        // Replace DefaultRender
        DateRenderer dateRenderer = new DateRenderer();
        dateRenderer.setHorizontalAlignment(horizontalAlignment);
        tbl.setDefaultRenderer(java.lang.Object.class, dateRenderer);
        
        // Set ColumnWidth
        TableColumn column = null;
        for (int i = 0; i < 7; i++) {
            column = tbl.getColumnModel().getColumn(i);
            column.setMinWidth(columnWidth);
            column.setPreferredWidth(columnWidth);
            column.setMaxWidth(columnWidth);
        }
        tbl.setRowHeight(rowHeight);
        
        // Embed popupMenu
        tbl.addMouseListener(new MouseAdapter() {
            
            public void mousePressed(MouseEvent e) {
                if (appointMenu.isPopupTrigger(e)) {
                    doPopup(e);
                }
            }
            
            public void mouseReleased(MouseEvent e) {
                if (appointMenu.isPopupTrigger(e)) {
                    doPopup(e);
                }
            }
        });
        
        return tbl;
    }
    
    private void doPopup(MouseEvent e) {
        
        // ReadOnly ���̗\��͕s��
        if (context.isReadOnly()) {
            return;
        }
        
        popedRow = table.rowAtPoint(e.getPoint());
        popedCol = table.columnAtPoint(e.getPoint());
        if (popedRow == -1 || popedCol == -1) {
            return;
        }
        
        // �N���b�N���ꂽ�ʒu�� MedicalEvent
        MedicalEvent me = days[popedRow][popedCol];
        
        // �\��̂Ȃ���
        // popup menu ���L�����Z���݂̂Ȃ̂�
        if (me.getAppointmentName() == null) {
            return;
        }
        
        // ���O�̓��̗\��͕s��
        //if (me.isOutOfMonth()) {
        //return;
        //}
        
        // �{���ȑO�̗\��͕s��
        if (me.before(today)) {
            return;
        }
        
        appointMenu.show(e.getComponent(),e.getX(), e.getY());
    }
    
    public void appointInspect(ActionEvent e) {
        //processAppoint(popedRow, popedCol, "�Đf", null);
    }
    
    public void appointTest(ActionEvent e) {
        processAppoint(popedRow, popedCol, "���̌���", null);
    }
    
    public void appointImage(ActionEvent e) {
        processAppoint(popedRow, popedCol, "�摜�f�f", null);
    }
    
    public void appointOther(ActionEvent e) {
        processAppoint(popedRow, popedCol, "���̑�", null);
    }
    
    //public void appointCancel(ActionEvent e) {
    // processCancel(popedRow, popedCol);
    //}
    public void appointCancel() {
        processCancel(popedRow, popedCol);
    }
    
    private void processAppoint(int row, int col, String appointName, String memo) {
        
        MedicalEvent entry = days[row][col];
        AppointmentModel appoint = entry.getAppointEntry();
        
        if (appoint == null) {
            appoint = new AppointmentModel();
            appoint.setDate(ModelUtils.getDateAsObject(entry.getDisplayDate()));
            entry.setAppointEntry(appoint);
        }
        
        int oldState = appoint.getState();
        int next = 0;
        switch (oldState) {
            
            case AppointmentModel.TT_NONE:
                next = AppointmentModel.TT_NEW;
                break;
                
            case AppointmentModel.TT_NEW:
                next = AppointmentModel.TT_NEW;
                break;
                
            case AppointmentModel.TT_HAS:
                next = AppointmentModel.TT_REPLACE;
                break;
                
            case AppointmentModel.TT_REPLACE:
                next = AppointmentModel.TT_REPLACE;
                break;
        }
        appoint.setState(next);
        
        appoint.setName(appointName);
        appoint.setMemo(memo);
        
        ((AbstractTableModel)table.getModel()).fireTableCellUpdated(popedRow, popedCol);
        
        boundSupport.firePropertyChange(CareMapDocument.APPOINT_PROP, null, appoint);
        
        if (! dirty) {
            dirty = true;
            parent.setDirty(dirty);
        }
    }
    
    private void processCancel(int row, int col) {
        
        MedicalEvent entry = days[row][col];
        AppointmentModel appoint = entry.getAppointEntry();
        if (appoint == null) {
            return;
        }
        
        int oldState = appoint.getState();
        int nextState = 0;
        
        switch (oldState) {
            case AppointmentModel.TT_NONE:
                break;
                
            case AppointmentModel.TT_NEW:
                nextState = AppointmentModel.TT_NONE;
                break;
                
            case AppointmentModel.TT_HAS:
                nextState = AppointmentModel.TT_REPLACE;
                break;
                
            case AppointmentModel.TT_REPLACE:
                nextState = AppointmentModel.TT_REPLACE;
                break;
        }
        
        appoint.setState(nextState);
        appoint.setName(null);
        
        ((AbstractTableModel)table.getModel()).fireTableCellUpdated(popedRow, popedCol);
        
        boundSupport.firePropertyChange(CareMapDocument.APPOINT_PROP, null, appoint);
        
        if (! dirty) {
            dirty = true;
            parent.setDirty(dirty);
        }
    }
    
    /**
     * �J�����_�e�[�u���̃f�[�^�𐶐�����
     */
    private MedicalEvent[][] createDays(GregorianCalendar gc) {
        
        MedicalEvent[][] data = null;
        
        // �m�P���O�^��̍����Ɠ�����
        int dayOfMonth = gc.get(Calendar.DAY_OF_MONTH);
        
        // �쐬����J�����_���̓���
        int numDaysOfMonth = gc.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // �Ō�̓������̉��T�ڂ�
        gc.add(Calendar.DAY_OF_MONTH, numDaysOfMonth - dayOfMonth);  // Last day
        lastDay = (GregorianCalendar)gc.clone();                     // Save last day
        numRows = gc.get(Calendar.WEEK_OF_MONTH);                    // Week of month
        
        // ����͉��J�����ڂ�
        lastCol = gc.get(Calendar.DAY_OF_WEEK);
        lastCol--;
        
        // ���̍ŏ��̓�
        numDaysOfMonth--;
        gc.add(Calendar.DAY_OF_MONTH, -numDaysOfMonth);
        firstDay = (GregorianCalendar)gc.clone();
        
        // �T�̉����ڂ�
        firstCol = gc.get(Calendar.DAY_OF_WEEK);
        firstCol--;
        
        // ���̌��̃J�����_�[�ɕ\������ŏ��̓�
        gc.add(Calendar.DAY_OF_MONTH, -firstCol);
        
        // �f�[�^�z��𐶐�
        data = new MedicalEvent[numRows][7];
        
        // ����Â��������Ȃ��疄�ߍ���
        MedicalEvent me;
        boolean b;
        for (int i = 0; i < numRows; i++) {
            
            for (int j = 0; j < 7; j++) {
                
                me = new MedicalEvent(
                        gc.get(Calendar.YEAR),
                        gc.get(Calendar.MONTH),
                        gc.get(Calendar.DAY_OF_MONTH),
                        gc.get(Calendar.DAY_OF_WEEK));
                
                // ���O�̓���
                b = month == gc.get(Calendar.MONTH) ? true : false;
                me.setOutOfMonth(!b);
                
                // ������
                b = today.equals(gc) ? true : false;
                me.setToday(b);
                
                data[i][j] = me;
                
                // ���̓�
                gc.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        return data;
    }
    
    /**
     * Custom table cell renderer for the carendar panel.
     */
    class DateRenderer extends DefaultTableCellRenderer {
        
        private static final long serialVersionUID = -5061911803358533448L;
        
        public DateRenderer() {
            super();
            setOpaque(true);
        }
        
        public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean isFocused,
                int row, int col) {
            Component compo = super.getTableCellRendererComponent(table,
                    value,
                    isSelected,
                    isFocused,
                    row, col);
            if (value != null) {
                
                MedicalEvent me = (MedicalEvent)value;
                String eventCode = me.getMedicalCode();
                
                int dayOfWeek = me.getDayOfWeek();
                
                if (dayOfWeek == 1) {
                    this.setForeground(sundayColor);
                    
                } else if (dayOfWeek == 7) {
                    this.setForeground(saturdayColor);
                    
                } else {
                    this.setForeground(weekdayColor);
                }
                
                if (me.isOutOfMonth()) {
                    this.setFont(outOfMonthFont);
                    
                } else {
                    this.setFont(inMonthFont);
                }
                
                // �a����
                if (me.getDisplayDate().endsWith(birthday)) {
                    this.setBackground(birthdayColor);
                    
                    // �{��
                } else if (me.isToday() && (!me.isOutOfMonth())) {
                    this.setBackground(todayBackground);
                    
                    // ���{�I�[�_�̂����
                } else if (eventCode != null) {
                    Color c = parent.getOrderColor(eventCode);
                    this.setBackground(c);
                    
                    // �\��̂����
                } else if (me.getAppointEntry() != null) {
                    
                    String appoName = me.getAppointmentName();
                    
                    if (appoName == null) {
                        // Cancel
                        this.setBackground(calendarBackground);
                        
                    } else {
                        
                        Color c = parent.getAppointColor(appoName);
                        
                        // �{���ȑO
                        if (me.before(today)) {
                            this.setBackground(calendarBackground);
                            this.setBorder(BorderFactory.createLineBorder(c));
                            
                        } else {
                            // �{���ȍ~
                            this.setBackground(c);
                        }
                    }
                    
                    // �����Ȃ���
                } else {
                    
                    this.setBackground(calendarBackground);
                }
                
                ((JLabel)compo).setText(me.toString());
                
            }
            return compo;
        }
    }
    
    /**
     * CalendarPool Class
     */
    public static class SimpleCalendarPool {
        
        private static SimpleCalendarPool instance = new SimpleCalendarPool();
        
        private Hashtable<String, ArrayList> poolDictionary = new Hashtable<String, ArrayList>(12,0.75f);
        
        private SimpleCalendarPool() {
        }
        
        public static SimpleCalendarPool getInstance() {
            return instance;
        }
        
        public synchronized SimpleCalendarPanel acquireSimpleCalendar(int n) {
            ArrayList pool = (ArrayList)poolDictionary.get(String.valueOf(n));
            if (pool != null) {
                int size = pool.size();
                size--;
                return (SimpleCalendarPanel)pool.remove(size);
            }
            return new SimpleCalendarPanel(n);
        }
        
        @SuppressWarnings("unchecked")
        public synchronized void releaseSimpleCalendar(SimpleCalendarPanel c) {
            int n = c.getRelativeMonth();
            String key = String.valueOf(n);
            ArrayList pool = poolDictionary.get(key);
            if (pool == null) {
                pool = new ArrayList<SimpleCalendarPanel>(5);
                poolDictionary.put(key, pool);
            }
            pool.add(c);
        }
    }
}