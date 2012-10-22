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

import open.dolphin.plugin.*;
import open.dolphin.util.*;


/**
 * Medical Event Calendar including orders, patient-visit and appointments.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public final class SimpleCalendarPanel extends JPanel implements DragGestureListener, DropTargetListener, DragSourceListener {
    
    private final int COLUMN_WIDTH = 27;
    private final int ROW_HEIGHT   = 18;
    private final Font OUTOF_MONTH_FONT  = new Font("Dialog", Font.PLAIN,9);
    private final Font IN_MONTH_FONT     = new Font("Dialog", Font.PLAIN,12);
    
    /*private final String[] COLUMN_NAMES = {
        "<html><body><tt><center><font color=FF0000>��</font></center></body></html>",
        "<html><body><tt><center>��</center></body></html>",
        "<html><body><tt><center>��</center></body></html>",
        "<html><body><tt><center>��</center></body></html>",
        "<html><body><tt><center>��</center></body></html>",
        "<html><body><tt><center>��</center></body></html>",
        "<html><body><tt><center>�y</center></body></html>"
    };*/
    private final String[] COLUMN_NAMES = {
        "��", "��", "��", "��", "��", "��", "�y"
    };
    
    private int year;
    private int month;
    private int numRows;    // �J�����_�e�[�u���̍s��
    private int firstCol;
    private int lastCol;
    private GregorianCalendar firstDay;
    private GregorianCalendar lastDay;
    private GregorianCalendar today;
    private String birthday;
    
    private JTable table;
    private MedicalEvent[][] days;
    private int rowHeight = ROW_HEIGHT;
    private int columnWidth = COLUMN_WIDTH;
    private int horizontalAlignment = SwingConstants.LEFT;
    
    // Color
    private Color sundayColor = ClientContext.getColor("calendar.background.sunday");      //new Color(255, 0, 130); //Color.red;
    private Color saturdayColor = ClientContext.getColor("calendar.background.saturday");  //= Color.blue;
    private Color todayBackground = ClientContext.getColor("calendar.background.today");   //new Color(191, 239, 131); //Color.yellow;
    private Color calendarBackground = ClientContext.getColor("calendar.background.default"); //Color.white;
    private Color weekdayColor = ClientContext.getColor("calendar.foreground.weekday"); //new Color(20, 20, 70); //Color.black;
    private Color outOfMothColor = ClientContext.getColor("calendar.foreground.outOfMonth"); //Color.lightGray;
    private Color birthdayColor = ClientContext.getColor("calendar.background.birthday");
    
    private Font outOfMonthFont = OUTOF_MONTH_FONT;
    private Font inMonthFont = IN_MONTH_FONT;
    
    private DragSource dragSource;
    private int dragRow;
    private int dragCol;
    
    private int relativeMonth;
    
    private IChartContext context;
    private CareMapDocument parent;
    private boolean dirty;
    
    private JPopupMenu appointMenu;
    private int popedRow;
    private int popedCol;
    
    private String markEvent = "-1";
    //private CalendarDao dao;
    
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
        Dimension dim = new Dimension(COLUMN_WIDTH*7 + 10, ROW_HEIGHT*8 + 5);
        this.setPreferredSize(dim);
        this.setMinimumSize(dim);
        this.setMaximumSize(dim);
       
        // Embed popup menu
        appointMenu = PopupMenuFactory.create("appoint.popupMenu.", this);
        
        // DAO
        //dao = (CalendarDao)DaoFactory.create(this, "calendar");
        
        // Table �� DragTarget, ���g�����X�i�ɐݒ肷��
        dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(table, DnDConstants.ACTION_COPY_OR_MOVE, this);
        
        // Table �� DropTarget, ���g�����X�i�ɐݒ肷��
        new DropTarget(table, this);
        
        // Print date
        //System.out.println("Today = " + MMLDate.getDate(today));
        //System.out.println("First date = " + MMLDate.getDate(firstDay));
        //System.out.println("Last date = " + MMLDate.getDate(lastDay));
    }
    
    public void setChartContext(IChartContext context) {
        this.context = context;
        birthday = context.getPatient().getBirthday().substring(5);
    }
    
    public void setParent(CareMapDocument doc) {
        parent = doc;
    }
    
    public String getCalendarTitle() {
        StringBuffer buf = new StringBuffer();
        buf.append(year);
        buf.append("�N");
        buf.append(month + 1);
        buf.append("��");
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
        //return MMLDate.getDateTime(firstDay);
    }
    
    public String getLastDate() {
        return MMLDate.getDate(lastDay);
        //return MMLDate.getDateTime(lastDay);
    }
    
    public ArrayList getAppointDays() {
        
        ArrayList results = new ArrayList();
        MedicalEvent event;
        AppointEntry appoint;
        
        for (int col = firstCol; col < 7; col++) {
            event = days[0][col];
            appoint = event.getAppointEntry();
            if (appoint != null && appoint.getAppointName() != null) {
                results.add(appoint);
            }
        }
        
        for (int row = 1; row < numRows - 1; row++) {
            for (int col = 0; col < 7; col++) {
                event = days[row][col];
                appoint = event.getAppointEntry();
                if (appoint != null && appoint.getAppointName() != null) {
                    results.add(appoint);
                }
            }
        }
        
        for (int col = 0; col < lastCol + 1; col++) {
            event = days[numRows - 1][col];
            appoint = event.getAppointEntry();
            if (appoint != null && appoint.getAppointName() != null) {
                results.add(appoint);
            }
        } 
        
        return results;
    }
    
    public ArrayList getUpdatedAppoints() {
        
        ArrayList results = new ArrayList();
        MedicalEvent event;
        AppointEntry appoint;
        
        for (int col = firstCol; col < 7; col++) {
            event = days[0][col];
            appoint = event.getAppointEntry();
            if (appoint != null && appoint.getState() != AppointEntry.TT_NONE) {
                results.add(appoint);
            }
        }
        
        for (int row = 1; row < numRows - 1; row++) {
            for (int col = 0; col < 7; col++) {
                event = days[row][col];
                appoint = event.getAppointEntry();
                if (appoint != null && appoint.getState() != AppointEntry.TT_NONE) {
                    results.add(appoint);
                }
            }
        }
        
        for (int col = 0; col < lastCol + 1; col++) {
            event = days[numRows - 1][col];
            appoint = event.getAppointEntry();
            if (appoint != null && appoint.getState() != AppointEntry.TT_NONE) {
                results.add(appoint);
            }
        } 
        
        /*for (int i = 0; i < results.size(); i++) {
            appoint = (AppointEntry)results.get(i);
            System.out.println(appoint.getDate() + " " + appoint.getState() + " " + appoint.getAppointName());
        }*/
        
        return results;
    }
    
    /**
     * �J�����_�ɃI�[�_�����{���ꂽ����\������
     */
    public void setMarkEvent(String newEvent) {
    
        if (newEvent.equals(markEvent)) {
            return;
        }
        
        markEvent = newEvent;
        clearMark();
        
        //if (markEvent.equals("0")) {
            // ���@����\������
            //markPVT();
            
        //} else 
        
        if (markEvent.equals("700")) {
            
            // �摜������J���e�̓����}�[�N����
            markImage();
            
        } else {
            // �I�[�_�����{���ꂽ����\�[�N����
            markOrder(markEvent);
        }
    }
    
     public void markAppoint() {
        
        // �����ȍ~�̃J�����_�̂݌�������
        if (relativeMonth < 0 ) {
            return;
        }
        
        // �ŏI���܂Ō���
        String endDate = MMLDate.getDate(lastDay);
        
        // �����ł���Ζ{���̂R���O���猟���A�����łȂ��ꍇ�̓J�����_�̍ŏ��̓����猟������
        String startDate = isThisMonth() ? MMLDate.getDayFromToday(-3) : MMLDate.getDate(firstDay);
        
        // �����ł���Ζ{���̂R���O���猟���A�����łȂ��ꍇ�̓J�����_�̍ŏ��̓����猟������
        /*if (isThisMonth()) {
            startDate = MMLDate.getDayFromToday(-3);
        
        } else {
            startDate = MMLDate.getDate(firstDay);
        }*/
        
        // ��������
        //AppointDao appoDao = (AppointDao)DaoFactory.create(this, "appoint");
        //SqlKarteDao dao = parent.getKarteDao();
		IChartContext ctx = parent.getChartContext();
        ArrayList list = ctx.getAppointments(context.getPatient().getId(), startDate, endDate);
        //fetchedAppo = true;
        
        // ��Ȃ烊�^�[��
        if ( list == null || list.size() == 0 ) {
            return;
        }
        
        // �\������
        int size = list.size();
        for (int i = 0; i < size; i++) {
            AppointEntry ae = (AppointEntry)list.get(i);
            ae.setState(AppointEntry.TT_HAS);
            String date = ae.getDate();
            int index = date.indexOf('T');
            if (index > 0) {
                date = date.substring(0, index);
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
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void markDate(ArrayList list) {
        //System.out.println ("entering markDate");
        int size = list.size();
        String mkDate;
        MedicalEvent me;
        int index;
        int[] ymd;
        int row,col;
        
        for (int i = 0; i < size; i++) {
         
            mkDate = (String)list.get(i);
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
        
     private void markOrder(String order) {
        
        // �J�����_�̍ŏ��̓����{���ڍs�ł���Ύ��{�I�[�_�͂Ȃ� 
        if (firstDay.after(today)) {
            return;
        }
       
        String startDate = MMLDate.getDate(firstDay);
        String endDate = isThisMonth() ? MMLDate.getDate(today) : MMLDate.getDate(lastDay);
        
        /*if (isThisMonth()) {
            // �����ł���Ζ{���܂ł������͈�
            endDate = MMLDate.getDate(today);
            if (startDate.equals(endDate)) {
                return;
                
            } else {
                // Search before today
                endDate = MMLDate.getDayFromToday(-1);
            }
            
        } else {
            // �����łȂ��ꍇ�̓J�����_�̍Ō�̓��܂Ō�������
            endDate = MMLDate.getDate(lastDay);
        }*/
        
        //SqlKarteDao dao = parent.getKarteDao();
		IChartContext ctx = parent.getChartContext();
        
        ArrayList list = ctx.getOrderDateHistory(context.getPatient().getId(), order, startDate, endDate);
        
        if ( (list != null) && (list.size() > 0) ) {
            markDate(list);
        }
    }
     
     private void markImage() {
        
        // �J�����_�̍ŏ��̓����{���ȍ~�ł���Ύ��{�I�[�_�͂Ȃ� 
        if (firstDay.after(today)) {
            return;
        }
        
        String startDate = MMLDate.getDate(firstDay);
        String endDate = isThisMonth() ? MMLDate.getDate(today) : MMLDate.getDate(lastDay);
        
        //if (isThisMonth()) {
            // �����ł���Ζ{���܂ł������͈�
            //endDate = MMLDate.getDate(today);
            //if (startDate.equals(endDate)) {
                //return;
                
            //} //else {
                // Search before today
               // endDate = MMLDate.getDayFromToday(-1);
            //}
            
        //} else {
            // �����łȂ��ꍇ�̓J�����_�̍Ō�̓��܂Ō�������
            //endDate = MMLDate.getDate(lastDay);
        //}
        
        //SqlKarteDao dao = parent.getKarteDao();
		IChartContext ctx = parent.getChartContext();
        ArrayList list = ctx.getImageDateHistory(context.getPatient().getId(), startDate, endDate);
        
        if ( (list != null) && (list.size() > 0) ) {
            markDate(list);
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
        AppointEntry appo = me.getAppointEntry();
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
        
        /*int action = event.getDropAction();
        String actionSt = action == DnDConstants.ACTION_MOVE ? "MoveAction" : "CopyAction";
        String resultSt = event.getDropSuccess() ? "DnD succeeded" : "DnD failed";
        
        System.out.println("This is the drag source: " + resultSt + " " + actionSt);
        MedicalEvent me = days[dragRow][dragCol];
        System.out.println("Source is " + me.getDisplayDate());*/
    }

    public void dragEnter(DragSourceDragEvent event) {
    }

    public void dragOver(DragSourceDragEvent event) {
    }
    
    public void dragExit(DragSourceEvent event) {
    }    

    public void dropActionChanged ( DragSourceDragEvent event) {
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
        AppointEntry source = null;
        try {
            source = (AppointEntry)tr.getTransferData(AppointEntryTransferable.appointFlavor);
            
        } catch (Exception ue) {
            System.out.println(ue);
            source = null;
        }
        if (source == null) {
            e.getDropTargetContext().dropComplete(false);
            return;
        }
        
        processAppoint(row, col, source.getAppointName(), source.getAppointMemo());
        
        e.getDropTargetContext().dropComplete(true);
        
        //MedicalEvent me = days[row][col];
        /*System.out.println("Source: " + source.toString() + " " + source.getAppointName());
        System.out.println("Destination:" + me.getDisplayDate());
        if (me.before(today)) {
            System.out.println("Before today");
        } else {
            System.out.println("After today");
        }
        if (action == DnDConstants.ACTION_MOVE) {
            System.out.println("Move Drop");
        } else {
            System.out.println("Copy Drop");
        }*/
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
        Color c = b ? DesignFactory.getDropOkColor() : this.getBackground();
        table.setBorder(BorderFactory.createLineBorder(c, 2));
    }     
    
    
    /**
     * �J�����_�[�e�[�u���𐶐�����
     */
    private JTable createCalendarTable(GregorianCalendar gc) {
        
        days = createDays(gc);
        
        AbstractTableModel model = new AbstractTableModel() {
                        
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
                return COLUMN_NAMES[col];
            }
            
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
        tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
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
    
    public void appointCancel(ActionEvent e) {
        processCancel(popedRow, popedCol);
    }
        
    private void processAppoint(int row, int col, String appointName, String memo) {
        
        MedicalEvent entry = days[row][col];
        AppointEntry appoint = entry.getAppointEntry();
        
        if (appoint == null) {
            appoint = new AppointEntry();
            appoint.setDate(entry.getDisplayDate());
            entry.setAppointEntry(appoint);
        }
            
        int oldState = appoint.getState();
        int next = 0;
        switch (oldState) {

            case AppointEntry.TT_NONE:
                next = AppointEntry.TT_NEW;
                break;

            case AppointEntry.TT_NEW:
                next = AppointEntry.TT_NEW;
                break;

            case AppointEntry.TT_HAS:
                next = AppointEntry.TT_REPLACE;
                break;

            case AppointEntry.TT_REPLACE:
                next = AppointEntry.TT_REPLACE;
                break;
        }
        appoint.setState(next);
        
        appoint.setAppointName(appointName);
        appoint.setAppointMemo(memo);
        
        ((AbstractTableModel)table.getModel()).fireTableCellUpdated(popedRow, popedCol);

        boundSupport.firePropertyChange(CareMapDocument.APPOINT_PROP, null, appoint);
        
        if (! dirty) {
            dirty = true;
            parent.setDirty(dirty);
        }
    }    
    
    private void processCancel(int row, int col) {
        
        MedicalEvent entry = days[row][col];
        AppointEntry appoint = entry.getAppointEntry();
        if (appoint == null) {
            return;
        }

        int oldState = appoint.getState();
        int nextState = 0;
        
        switch (oldState) {
            case AppointEntry.TT_NONE:
                break;
                
            case AppointEntry.TT_NEW:
                nextState = AppointEntry.TT_NONE;
                break;
                
            case AppointEntry.TT_HAS:
                nextState = AppointEntry.TT_REPLACE;
                break;
                
            case AppointEntry.TT_REPLACE:
                nextState = AppointEntry.TT_REPLACE;
                break;
        }
        
        appoint.setState(nextState);
        appoint.setAppointName(null); 
        
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
        
        private Hashtable poolDictionary = new Hashtable(12,0.75f);
        
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
        
        public synchronized void releaseSimpleCalendar(SimpleCalendarPanel c) {
            int n = c.getRelativeMonth();
            String key = String.valueOf(n);
            ArrayList pool = (ArrayList)poolDictionary.get(key);
            if (pool == null) {
                pool = new ArrayList(5);
                poolDictionary.put(key, pool);
            }
            pool.add(c);
        }
    }
}