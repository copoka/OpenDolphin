/*
 * DiagnosisDocument.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2004 Digital Globe, Inc. All rights reserved.
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import open.dolphin.dao.*;
import open.dolphin.exception.*;
import open.dolphin.infomodel.DocInfo;
import open.dolphin.infomodel.ID;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleInfo;
import open.dolphin.infomodel.RegisteredDiagnosisModule;
import open.dolphin.message.*;
import open.dolphin.message.MessageBuilder;
import open.dolphin.order.*;
import open.dolphin.plugin.event.ClaimMessageEvent;
import open.dolphin.plugin.event.ClaimMessageListener;
import open.dolphin.project.*;
import open.dolphin.table.*;
import open.dolphin.util.*;

import java.beans.*;
import java.util.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;

/**
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc. 
 */
public final class DiagnosisDocument extends DefaultChartDocument 
implements DragGestureListener, DropTargetListener, DragSourceListener,PropertyChangeListener {
    
    // ���a���e�[�u���̃J������
    private static final String[] diagnosisColumnNames;
    static {
        diagnosisColumnNames = ClientContext.getStringArray("diagnosis.columnNames");
    }
    
    // ���a���e�[�u���̃J�����ԍ�
    private final int DIAGNOSIS_COL         = 0;
    private final int CATEGORY_COL          = 1;
    private final int OUTCOME_COL           = 2;
    //private final int FIRST_ENCOUNTER_COL   = 3;
    private final int START_DATE_COL        = 3;
    private final int END_DATE_COL          = 4;

    // �]�A���X�g
    private static final String[] outcomeList;
    static {
        outcomeList = ClientContext.getStringArray("diagnosis.outcomeList");
        outcomeList[0] = null;
    }
      
    // ���o���Ԗ����X�g
    private static final String[] periodList = ClientContext.getStringArray("filter.combo.periodName");
    
    // ���o���Ԓl���X�g
    private static final String[] periodValueList = ClientContext.getStringArray("filter.combo.periodValue");
    
    // GUI �R���|�[�l���g
    private static final String RESOURCE_BASE       = "/open/dolphin/resources/images/";
    private static final String DELETE_BUTTON_IMAGE = "Delete24.gif";
    private static final String NEW_BUTTON_IMAGE  = "New24.gif";
    
    private JTextField countField;              // �����t�B�[���h
    
    private JButton deleteNewButton;            // �폜�{�^��
    
    private JButton openEditorButton;           // �V�K�{�^��
    
    private JButton deleteButton;               // �폜�{�^��
    
    private JTable newDiagTable;                // �V�K�a���e�[�u��
    
    private ObjectTableModel newDiagTableModel;
    
    private JTable diagTable;                   // �a���e�[�u��
    
    private ObjectTableModel tableModel;
    
    private JComboBox extractionCombo;          // ���o���ԃR���{
    
    private DragSource dragSource;
    
    // Properties
    private boolean dirty;
    
    private int newDiagnosisCount;
    
    private int diagnosisCount;
    
    private boolean editable = true;
    
    // �C���f�[�^�R���g���[��
    private int modifyRow = -1;
        
    /** Creates new DiagnosisDocument */
    public DiagnosisDocument() {
    }

    public void start() {
        
        JPanel p1 = createButtonPanel();
        JPanel p2 = createNewDiagPanel();
        JPanel p3 = new JPanel(new BorderLayout(0, 7));
        p3.add(p1, BorderLayout.NORTH);
        p3.add(p2, BorderLayout.CENTER);
        p3.setBorder(BorderFactory.createTitledBorder("�V�K���a��"));
        
        JPanel p4 = createButtonPanel2();
        JPanel p5 = createDignosisPanel();
        JPanel p6 = createFilterPanel();
        JPanel p7 = new JPanel(new BorderLayout(0, 7));
        p7.add(p4, BorderLayout.NORTH);
        p7.add(p5, BorderLayout.CENTER);
        p7.add(p6, BorderLayout.SOUTH);
        p7.setBorder(BorderFactory.createTitledBorder("���a��"));
        
        // Layouts        
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(p3);
        add(Box.createVerticalStrut(11));
        add(p7);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
               
        // ���a��\��
        getDiagHistory(getFilterDate(0));
        
        // State
        enter();
    }
    
    public boolean isDirty() {
        return dirty;
    }
    
    private void setDirty(boolean newDirty) {
        boolean oldDirty = dirty;
        dirty = newDirty;
        
        if (dirty != oldDirty) {
            controlMenu();
        }
    }
    
    public void enter() {
        super.enter();
        controlMenu();
    }    
    
    protected void controlMenu() {
        super.controlMenu();
        ChartMediator mediator = ((ChartPlugin)context).getChartMediator();
        mediator.saveKarteAction.setEnabled(dirty);
    }    
    
    public int getNewDiagnosisCount() {
        return newDiagnosisCount;
    }
    
    public void setNewDiagnosisCount(int n) {
        newDiagnosisCount = n;
        boolean b = n > 0 ? true : false;
        setDirty(b);
    }
    
    public int getDiagnosisCount() {
        return diagnosisCount;
    }
    
    public void setDiagnosisCount(int n) {
        diagnosisCount = n;
        try {
            String val = String.valueOf(diagnosisCount);
            countField.setText(val);
        } catch (RuntimeException e) {
            countField.setText("");
        }
    }    
    
    public boolean isEditable() {
        return editable;
    }
    
    public void setEditable(boolean b) {
        editable = b;
        
        if (! editable) {
            deleteNewButton.setEnabled(false);
            deleteButton.setEnabled(false);
            openEditorButton.setEnabled(false);
            extractionCombo.setEnabled(false);
        }
    }
    
    /**
     * �V�K���a���e�[�u���Ɏg�p����R���g���[���{�^���p�l����Ԃ�
     */
    private JPanel createButtonPanel() {
        
        // �폜�{�^��
        deleteNewButton = new JButton(createImageIcon(DELETE_BUTTON_IMAGE));
        deleteNewButton.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                deleteNew();
            }
        });
        deleteNewButton.setEnabled(false);
        
        // �V�K�o�^�{�^��        
        openEditorButton = new JButton(createImageIcon(NEW_BUTTON_IMAGE));
        openEditorButton.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                openEditor2();
           }
        });
        
        // Depends on readOnly prop
        openEditorButton.setEnabled(! isReadOnly());
        
        // �{�^���p�l��
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(Box.createHorizontalGlue());
        p.add(deleteNewButton);
        p.add(Box.createHorizontalStrut(5));
        p.add(openEditorButton);
        p.add(Box.createHorizontalStrut(7));
        return p;
    }
    
    /**
     * ImageIcon ��Ԃ�
     */    
    private ImageIcon createImageIcon(String name) {
        String res = RESOURCE_BASE + name;
        return new ImageIcon(this.getClass().getResource(res));
    }  
    
    /**
     * �V�K���a���e�[�u���p�̃p�l����Ԃ�
     */
    private JPanel createNewDiagPanel() {
               
        // �V�K�o�^�e�[�u��
        newDiagTableModel = new ObjectTableModel(diagnosisColumnNames, 7) {
            
            // �ҏW�s��
            public boolean isCellEditable(int row, int col) {
                return false;
            }
            
            // �I�u�W�F�N�g���e�[�u���ɕ\������
            public Object getValueAt(int row, int col) {
                
                Object o = getObject(row);
                if (o == null) {
                    return null;
                }
                
                RegisteredDiagnosisModule module = (RegisteredDiagnosisModule)o;
                String ret = null;
                
                switch (col) {
                    
                    case DIAGNOSIS_COL:
                        ret = module.getDiagnosis();
                        break;
                        
                    case CATEGORY_COL:
                        String categories = module.getCategory();
                        if (categories != null) {
                            ret = categories;
                        }
                        ret = ret != null ? (String)MMLTable.getDiagnosisCategoryDesc((String)ret) : null;
                        break;
                        
                    case OUTCOME_COL:
                        ret = module.getOutcome();
                        ret = ret != null ? (String)MMLTable.getDiagnosisOutcomeDesc((String)ret) : null;
                        break;
                        
                    /*case FIRST_ENCOUNTER_COL:
                        ret = module.getFirstEncounterDate();
                        break;*/
                        
                    case START_DATE_COL:
                        ret = module.getStartDate();
                        break;
                        
                    case END_DATE_COL:
                        ret = module.getEndDate();
                        break;
                }
                
                return ret;
            }
        };
        
        // Sort �@�\��������
        TableSorter s = new TableSorter(newDiagTableModel);
        newDiagTable = new JTable(s);
        s.addMouseListenerToHeaderInTable(newDiagTable);
        
        // Selection ��ݒ肷��
        newDiagTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        newDiagTable.setRowSelectionAllowed(true);
                
        // Mouse Click ������o�^����
        newDiagTable.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                
                if(! editable) {
                    return;
                }
                
                switch (e.getClickCount()) {
                 
                    case 1:
                        // �폜�{�^�����R���g���[��
                        controlDeleteNewButton();
                        break;
                        
                    case 2:
                        // �I�����ꂽ�s���G�f�B�^�ŕҏW
                        modify();
                        break;
                    
                }
            }
        });

        // 2003-10-30 licenseCode �ɂ�鐧��
        // RedaOnly �łȂ���� DnD ����t
        if (! isReadOnly()) {
        
            // Table �� DragTarget, this�����X�i�ɐݒ肷��
            dragSource = new DragSource();
            dragSource.createDefaultDragGestureRecognizer(newDiagTable, DnDConstants.ACTION_COPY_OR_MOVE, this);

            // Table �� DropTarget, this�����X�i�ɐݒ肷��
            new DropTarget(newDiagTable, this);
        }
        
        // Layout
        JScrollPane scroller = new JScrollPane(newDiagTable, 
                                   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                   JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        JPanel p = new JPanel(new BorderLayout());
        p.add(scroller, BorderLayout.CENTER);    
        p.setPreferredSize(new Dimension(400, 150));
        return p;      
    }
    
    
        //////////////   Drag Support //////////////////
    
    public void dragGestureRecognized(DragGestureEvent event) {
        
        if (! editable) {
            return;
        }
        
        // �I������Ă���s�𓾂�
        int row = newDiagTable.getSelectedRow();
        
        // �e�[�u�����f������I�u�W�F�N�g�𓾂�
        RegisteredDiagnosisModule o = (RegisteredDiagnosisModule)newDiagTableModel.getObject(row);
        if (o == null) {
            return;
        }
        
        // InfoModelTransferable �𐶐�����
        Transferable t = new InfoModelTransferable(o);
        Cursor cursor = DragSource.DefaultCopyDrop;
        int action = event.getDragAction();
        if (action == DnDConstants.ACTION_MOVE) {
            cursor = DragSource.DefaultMoveDrop;
        }

        // �h���b�O���J�n����
        dragSource.startDrag(event, cursor, t, this);
    }

    public void dragDropEnd(DragSourceDropEvent event) { 
        
        if (! event.getDropSuccess() || event.getDropAction() == DnDConstants.ACTION_COPY) {
            return;
        }
                
        /*int action = event.getDropAction();
        String actionSt = action == DnDConstants.ACTION_MOVE ? "MoveAction" : "CopyAction";
        String resultSt = event.getDropSuccess() ? "DnD succeeded" : "DnD failed";
        
        System.out.println("This is the drag source: " + resultSt + " " + actionSt);
        */
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
        
    public void drop(DropTargetDropEvent event) {
   
        
        if ( ! editable || (! isDropAcceptable(event))) {
            event.rejectDrop();
            setDropTargetBorder(false);
            event.getDropTargetContext().dropComplete(true);
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        
        // Transferable�𓾂�
        final Transferable tr = event.getTransferable();
        
        // Drop �ʒu�𓾂�
        final Point loc = event.getLocation();
        
        // ���߂� DnD ���������������� GUI �̒���t�����Ȃ�
        event.acceptDrop(DnDConstants.ACTION_COPY);
        event.getDropTargetContext().dropComplete(true);
        setDropTargetBorder(false);
        
        // �X���b�h�Ŏ��ۂ̃h���b�v����������
        Runnable r = new Runnable() {
            public void run() {
                boolean ok = doDrop(tr, loc);
            }
        };
        Thread t = new Thread(r);
        t.start();
    }
     
    /**
     * Drop �������s�Ȃ�
     */
    private boolean doDrop(Transferable tr, Point loc) {
        
        boolean ret = false;
        
        // StampTreeNode �łȂ���΃��^�[��
        if (! tr.isDataFlavorSupported(StampTreeTransferable.stampTreeNodeFlavor)) {
            Toolkit.getDefaultToolkit().beep();
            return ret;
        }
        
        // ���a���X�^���v�łȂ���΃��^�[��
        ModuleInfo stampInfo = null;
        try {
            StampTreeNode node = (StampTreeNode)tr.getTransferData(StampTreeTransferable.stampTreeNodeFlavor);
            stampInfo = (ModuleInfo)node.getStampInfo();
            if (! stampInfo.getEntity().equals("diagnosis")) {
                throw new DolphinException("Not diagnosis");
            }
            
        } catch (Exception ue) {
            Toolkit.getDefaultToolkit().beep();
            return ret;
        }

        // Drop �ʒu���e�[�u���͈͂ɂȂ���΃��^�[��
        int row = newDiagTable.rowAtPoint(loc);
        if (row == -1 ) {
            Toolkit.getDefaultToolkit().beep();
            return ret;
        }
        
        // �G�f�B�^���X�^���v�ɉ����ĕ��򂷂�
        if (stampInfo.isSerialized()) {
            applySerializedStamp(stampInfo);
        
        } else {
            openEditor();
        }
        
        ret = true;
        return ret;
    }
    
    /**
     * ���a���X�^���v����������
     */
    private void applySerializedStamp(ModuleInfo stampInfo) {
        
        // �a���X�^���v�� DB ����擾���ăe�[�u���֕\������
        startAnimation();

        String rdn = stampInfo.getStampId();
        String category = stampInfo.getEntity();
        String userId = Project.getUserId();
        
        RegisteredDiagnosisModule module = null;
        SqlStampDao dao = (SqlStampDao)SqlDaoFactory.create(DiagnosisDocument.this, "dao.stamp");
        module = (RegisteredDiagnosisModule)dao.getStamp(userId, category, rdn);

        if (module != null) {
            GregorianCalendar gc = new GregorianCalendar();
            String today = MMLDate.getDate(gc);
            module.setFirstEncounterDate(today);
            newDiagTableModel.addRow(module);
            setNewDiagnosisCount(newDiagTableModel.getObjectCount());
        }

        stopAnimation();
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
    
    /**
     * Drop �^�[�Q�b�g�̋��E��\������
     */
    private void setDropTargetBorder(final boolean b) {
        Color c = b ? DesignFactory.getDropOkColor() : this.getBackground();
        newDiagTable.setBorder(BorderFactory.createLineBorder(c, 2));
    }  
    
    private void startAnimation() {
        
        SwingUtilities.invokeLater(new Runnable() {
                    
            public void run() {
                ChartPlugin ct = (ChartPlugin)context;
                StatusPanel sp = ct.getStatusPanel();
                sp.start("�X�^���v���擾���Ă��܂�...");
            }
        });
    }
    
    private void stopAnimation() {
        
        SwingUtilities.invokeLater(new Runnable() {
                    
            public void run() {
                ChartPlugin ct = (ChartPlugin)context;
                StatusPanel sp = ct.getStatusPanel();
                sp.stop("");                   
            }
        });
    }   
    
    /**
     * ���a���G�f�B�^�� EventDispatch ����J��
     */
    private void openEditor2() {
        
        StampEditorDialog stampEditor = getEditor();
        
        if (stampEditor == null) {
            return;
        }
        
        stampEditor.setOkButtonText("�o �^");
        stampEditor.addPropertyChangeListener("value", this);
        stampEditor.setValue(null);
        Thread t = new Thread((Runnable)stampEditor);
        t.start();
    }
     
    /**
     * ���a���G�f�B�^���o�b�N�O�����h�X���b�h����J��
     */
    private void openEditor() {
                
        // StampEditor ���N������
        final StampEditorDialog stampEditor = getEditor();
        
        if (stampEditor == null) {
            return;
        }
        
        stampEditor.setOkButtonText("�o �^");
        stampEditor.addPropertyChangeListener("value", this);
        stampEditor.setValue(null);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                stampEditor.run();
            }
        });
    }
    
    /**
     * �V�K�ɒǉ����ꂽ���a����ύX����
     */
    private void modify() {
        
        // �_�u���N���b�N���ꂽ�s�̃I�u�W�F�N�g���擾����
        modifyRow = newDiagTable.getSelectedRow();
        
        RegisteredDiagnosisModule o = (RegisteredDiagnosisModule)newDiagTableModel.getObject(modifyRow);
        
        if (o == null) {
            modifyRow = -1;
            return;
        }
        
        StampEditorDialog stampEditor = getEditor();
        
        if (stampEditor == null) {
            modifyRow = -1;
            return;
        }
        
        Object[] obj = new Object[1];
        obj[0] = o;
        
        stampEditor.setOkButtonText("�o �^");
        stampEditor.addPropertyChangeListener("value", this);
        stampEditor.setValue(obj);
        Thread t = new Thread((Runnable)stampEditor);
        t.start();
    }
    
    /**
     * �V�K�ǉ����a�����폜����
     */
    private void deleteNew() {
        
        int row = newDiagTable.getSelectedRow();
        newDiagTableModel.deleteRow(row);
        setNewDiagnosisCount(newDiagTableModel.getObjectCount());
    }
        
    /**
     * �폜�{�^���R���g���[��
     */
    private void controlDeleteNewButton() {
        
        int row = newDiagTable.getSelectedRow();
        
        boolean b1 = newDiagTableModel.isValidRow(row);
        boolean b2 = deleteNewButton.isEnabled();
        
        if (b1 && !b2) {
            deleteNewButton.setEnabled(true);
            
        } else if (! b1 && b2) {
            deleteNewButton.setEnabled(false);
        }
    }
    
    /**
     * ���a���G�f�B�^��Ԃ�
     */
    private StampEditorDialog getEditor() {
        
        StampEditorDialog stampEditor = null;
        try {
            stampEditor = new StampEditorDialog("diagnosis");
        
        } catch (DolphinException e) {
            System.out.println("DolphinException at stampInfoDropped: " + e.toString());
            stampEditor = null;
        }
        
        return stampEditor;
    }
    
    /**
     * ���a���G�f�B�^����f�[�^���󂯎��e�[�u���֒ǉ�����
     */
    public void propertyChange(PropertyChangeEvent e) {

        ArrayList list = (ArrayList)e.getNewValue();
        if (list == null) {
            return;
        }

        int len = list.size();
        
        if (modifyRow > -1) {
            RegisteredDiagnosisModule module = (RegisteredDiagnosisModule)list.get(0);
            newDiagTableModel.insertRow(modifyRow, module);
            newDiagTableModel.deleteRow(modifyRow+1);
            modifyRow = -1;
            
        } else {
        
            // �G�f�B�^����󂯎�����f�[�^��ǉ�
            for (int i = len - 1; i > -1; i--) {
                RegisteredDiagnosisModule module = (RegisteredDiagnosisModule)list.get(i);
                newDiagTableModel.insertRow(0, module);
            }
        }
        
        // �V�K����ݒ肷��
        setNewDiagnosisCount(newDiagTableModel.getObjectCount());
    }

    /**
     * �V�K���a����ۑ�����
     */
    public void save() {  
        
        if (! dirty) {
            return;
        }
                
        ID masterID = Project.getMasterId(context.getPatient().getId());
        if (masterID == null) {
            // 2003-09-2
            //return;
        }
        
        final boolean sendMML = (Project.getSendMML() && masterID != null) ? true : false;
        final boolean sendDiagnosis = Project.getSendDiagnosis() && ((ChartPlugin)context).getCLAIMListener() != null ? true : false;
        
        // Show and get saving params
        SaveParams params = new SaveParams(sendMML);
        params.setTitle("�a���o�^");
        params.setPrintCount(-1);  // disable print
        
        // ��t���痈�Ă���f�ÉȁiPatientVisit ���ێ��j���Z�b�g����
        String dept = ((ChartPlugin)context).getPatientVisit().getDepartment();
        params.setDepartment(dept);
        
        SaveDialog sd = (SaveDialog)Project.createSaveDialog(getParentFrame(),params);
        sd.show();
        params = sd.getValue();
        if (params == null) {
            sd.dispose();
            return;
        }
        sd.dispose();
        
        // continue to save
        // Create confirm date(YYYY-MM-DDTHH-MM-SS)
        GregorianCalendar gc = new GregorianCalendar();
        String confirmDate = MMLDate.getDateTime(gc);
        saveNewData(sendMML, sendDiagnosis, masterID, params, confirmDate);
        saveModifiedData(confirmDate);
        
        // Uneditable
        setEditable(false);
        
        // set the flag
        dirty = false;
        controlMenu();   
    }
           
    /**
     * �V�K�ɓ��͂��ꂽ���a����ۑ�����
     */
    private void saveNewData(final boolean sendMML, final boolean sendClaim, ID masterID, SaveParams params, final String confirmDate)  {
        
        if (newDiagTableModel.getObjectCount() == 0) {
            return;
        }
        
        Object[] modules = newDiagTableModel.getObjectList().toArray();
        int moduleCount = modules.length;
		final RegisteredDiagnosisModule[] rd = new RegisteredDiagnosisModule[moduleCount];
        for (int i = 0; i < moduleCount; i++) {
        	rd[i] = (RegisteredDiagnosisModule)modules[i];
        }
        
        // Allocate docinfo
        final DocInfo[] infos = new DocInfo[moduleCount];
        DocInfo docInfo = null;
                
        for (int i = 0; i < moduleCount; i++) {
            
			docInfo = new DocInfo();
			docInfo.setDocId(Project.createUUID());
			docInfo.setTitle("�a���o�^");
			docInfo.setFirstConfirmDate(confirmDate);
			docInfo.setConfirmDate(confirmDate);
			docInfo.setCreator(Project.getCreatorInfo());
            infos[i] = docInfo;
        }
       
        final SqlRDSaverDao dao = (SqlRDSaverDao)SqlDaoFactory.create(this, "dao.rdSaver");
        dao.setPid(context.getPatient().getId());
        dao.setDocInfo(infos);
        dao.setRegisteredDiagnosis(modules);
        
        final StatusPanel statusPanel = ((ChartPlugin)context).getStatusPanel();
        
        Runnable r = new Runnable() {
        	
        	public void run() {
        		
				SwingUtilities.invokeLater(new Runnable() {

					public void run() {  
						statusPanel.start("�ۑ����Ă��܂�...");
					}
				});

				boolean ret = dao.save();
				
				if (sendClaim) {
					DiseaseHelper dhl = new DiseaseHelper();
					dhl.setPatientId(context.getPatient().getId());
					dhl.setConfirmDate(confirmDate);
					dhl.setCreator(Project.getCreatorInfo());
					dhl.setRegisteredDiagnosisModule(rd);
					dhl.setDocInfo(infos);
					dhl.setGroupId(Project.createUUID());
					DmlMessageBuilder builder = new DmlMessageBuilder();
					String dml = builder.build((IInfoModel)dhl);
					debug(dml);
					
					MessageBuilder mb = new MessageBuilder();
					mb.setTemplateFile("disease.vm");
					String claimMessage = mb.build(dml);
					debug(claimMessage);
					
					ClaimMessageEvent event = new ClaimMessageEvent(this);
					event.setPatientId(context.getPatient().getId());
					event.setPatientName(context.getPatient().getId());
					event.setPatientSex(context.getPatient().getId());
					event.setTitle("�a���o�^");
					event.setClaimInstance(claimMessage);
					event.setConfirmDate(confirmDate);
					ClaimMessageListener claimListener = ((ChartPlugin)context).getCLAIMListener();
					claimListener.claimMessageEvent(event);
				}
        		
				SwingUtilities.invokeLater(new Runnable() {

					public void run() {
						statusPanel.stop("");
					}
				});
        	}
        };
        
        Thread t = new Thread(r);
        t.start();
    }
    
    /**
     * �]�A�ƏI���������͂��ꂽ���a�����X�V����
     */
    private void saveModifiedData(final String confirmDate) {
        
        final ArrayList results = getModifiedData();
        if (results == null) {
            return;
        }
        
        final SqlRDSaverDao dao = (SqlRDSaverDao)SqlDaoFactory.create(this, "dao.rdSaver");
        
		final StatusPanel statusPanel = ((ChartPlugin)context).getStatusPanel();
        
		Runnable r = new Runnable() {
        	
			public void run() {
        		
				SwingUtilities.invokeLater(new Runnable() {

					public void run() {  
						statusPanel.start("�ۑ����Ă��܂�...");
					}
				});

				boolean ret = dao.update(results, confirmDate);
        		
				SwingUtilities.invokeLater(new Runnable() {

					public void run() {
						statusPanel.stop("");
					}
				});
			}
		};
        
		Thread t = new Thread(r);
		t.start();          
    }
            
    /**
     * �w����Ԉȍ~�̏��a�����������ăe�[�u���֕\������
     */
    private void getDiagHistory(final String past) {
    	
		final StatusPanel statusPanel = ((ChartPlugin)context).getStatusPanel();
        
		Runnable r = new Runnable() {
        	
			public void run() {
        		
				SwingUtilities.invokeLater(new Runnable() {

					public void run() {  
						statusPanel.start("�������Ă��܂�...");
					}
				});

				final ArrayList results = context.getDiagnosisHistory(context.getPatient().getId(), past);
        		
				SwingUtilities.invokeLater(new Runnable() {

					public void run() {
						
						statusPanel.stop("");
						
						if (results != null) {
							tableModel.setObjectList(results);
						}
        
						setDiagnosisCount(tableModel.getObjectCount());
					}
				});
			}
		};
        
		Thread t = new Thread(r);
		t.start();
    }
    
    /**
     * ���a���e�[�u��
     */
    private JPanel createDignosisPanel() {
        
        tableModel = new ObjectTableModel(diagnosisColumnNames, 20) {
            
            // �]�A�ƏI�����̂ݕҏW�\�Ƃ���
            public boolean isCellEditable(int row, int col) {
                
                // licenseCode && �ۑ��� (editable) �Ő���
                if ( isReadOnly() || (! editable) ) {
                    return false;
                }
                
                return ( isValidRow(row) && (col == OUTCOME_COL || col == END_DATE_COL) ) ? true : false;
            }
            
            // �I�u�W�F�N�g��\������
            public Object getValueAt(int row, int col) {
                
                String ret = null;
                
                DiagnosisEntry entry = (DiagnosisEntry)getObject(row);
                
                if (entry == null) {
                    return ret;
                }
                
                switch (col) {
                    
                    case DIAGNOSIS_COL:
                        ret = entry.getDiagnosis();
                        break;
                        
                    case CATEGORY_COL:
                        ret = entry.getCategory();
                        ret = ret != null ? (String)MMLTable.getDiagnosisCategoryDesc((String)ret) : null;
                        break;
                        
                    case OUTCOME_COL:
                        ret = entry.getOutcome();
                        ret = ret != null ? (String)MMLTable.getDiagnosisOutcomeDesc((String)ret) : null;
                        break;
                        
                    /*case FIRST_ENCOUNTER_COL:
                        ret = entry.getFirstEncounterDate();
                        break;*/
                        
                    case START_DATE_COL:
                        // 2003-11
                        ret = entry.getStartDate();
                        if (ret == null) {
                            ret = entry.getFirstEncounterDate();
                        }
                        break;
                        
                    case END_DATE_COL:
                        ret = entry.getEndDate();
                        break;    
                }
                return ret;
            }
            
            public void setValueAt(Object value, int row, int col) {
                
                DiagnosisEntry entry = (DiagnosisEntry)getObject(row);
                
                if (entry == null) {
                    return;
                }
                
                switch (col) {
                    
                    case DIAGNOSIS_COL:
                        break;
                        
                    case CATEGORY_COL:
                        break;
                        
                    case OUTCOME_COL:
                        if (value == null) {
                            
                            entry.setOutcome(null);
                            entry.setEndDate(null);
                            
                        } else {
                            
                            String val = (String)MMLTable.getDiagnosisOutcomeValue((String)value);
                            entry.setOutcome(val);
                            
                            val = entry.getEndDate();
                            if (val == null) {
                                GregorianCalendar gc = new GregorianCalendar();
                                String today = MMLDate.getDate(gc);
                                entry.setEndDate(today);
                            }  
                         }
                        break;
                        
                    //case 4:
                        //entry.setStartDate((String)value);
                        //break;
                        
                    case END_DATE_COL:
                        if (value != null && ((String)value).trim().equals("") ) {
                            entry.setEndDate((String)value);
                        }
                        break; 
                }
                
                // Set the modifyed flag true
                entry.setModified(true);
                
                fireTableRowsUpdated(row, row);
                
                setDirty(true);
                
            }
        };
        
        // �\�[�g�@�\������
        TableSorter s = new TableSorter(tableModel);
        diagTable = new JTable(s);
        s.addMouseListenerToHeaderInTable(diagTable);
        
        diagTable.setSurrendersFocusOnKeystroke(true);
        
        // �����I�����J����
        TableColumn column = diagTable.getColumnModel().getColumn(END_DATE_COL);
        column.setCellEditor (new IMECellEditor (new JTextField(), 1, false));
        
        // �s�I��
        diagTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        diagTable.setRowSelectionAllowed(true);
        ListSelectionModel m = diagTable.getSelectionModel();
        m.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    // �폜�{�^�����R���g���[������
                    // licenseCode �����ǉ�
                    if ( isReadOnly() || (! editable) ) {
                        return;
                    }
                    
                    int row = diagTable.getSelectedRow();
                    boolean b1 = tableModel.isValidRow(row);
                    boolean b2 =  deleteButton.isEnabled();
                    if (b1 && ! b2) {
                        deleteButton.setEnabled(true);
                    
                    } else if (! b1 && b2) {
                        deleteButton.setEnabled(false);
                    }
                }
            }
        });

        // Outcome comboBox ���͂�ݒ肷��
        JComboBox outcomeCombo = new JComboBox(outcomeList);
        column = diagTable.getColumnModel().getColumn(OUTCOME_COL);
        column.setCellEditor(new DefaultCellEditor(outcomeCombo));
        
        // Layout
        JScrollPane scroller = new JScrollPane(diagTable, 
                                   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                   JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        JPanel p = new JPanel(new BorderLayout());
        p.add(scroller, BorderLayout.CENTER);
        return p;
    }
    
    /**
     * �C�����ꂽ�f�[�^�����X�g�ɂ��ĕԂ�
     */
    private ArrayList getModifiedData() {
        
        int count = tableModel.getObjectCount();
        if (count == 0) {
            return null;
        }
        
        ArrayList list = new ArrayList();
        for (int i = 0; i < count; i++) {
            DiagnosisEntry entry = (DiagnosisEntry)tableModel.getObject(i);
            if (entry.isModified()) {
                list.add(entry);
            }
        }
        
        return list.size() > 0 ? list : null;        
    }
    
    /**
     * �I�����ꂽ�s�̃f�[�^���폜����
     */    
    private void delete() {
     
        int row = diagTable.getSelectedRow();
        
        DiagnosisEntry entry = (DiagnosisEntry)tableModel.getObject(row);
        if (entry != null) {
            SqlRDSaverDao dao = (SqlRDSaverDao)SqlDaoFactory.create(this, "dao.rdSaver");
            boolean ret = dao.delete(entry);
            if (ret) {
                tableModel.deleteRow(row);
                setDiagnosisCount(tableModel.getObjectCount());
            }
        }
    }
        
    /**
     * �폜�{�^���p�l����Ԃ�
     */    
    private JPanel createButtonPanel2() {
        
        // �폜�{�^��
        deleteButton = new JButton(createImageIcon(DELETE_BUTTON_IMAGE));
        //deleteButton.setMnemonic('D');
        deleteButton.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                delete();
            }
        });
        deleteButton.setEnabled(false);
        
        // �{�^���p�l��
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(Box.createHorizontalGlue());
        p.add(deleteButton);
        p.add(Box.createHorizontalStrut(7));
        return p;
    }
    
    /**
     * ���o���ԃp�l����Ԃ�
     */
    private JPanel createFilterPanel() {

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(Box.createHorizontalStrut(7));

        // ���o���ԃR���{�{�b�N�X
        p.add(new JLabel("���o���� �ߋ��F "));        
        extractionCombo = new JComboBox(periodList);
        Dimension dim = new Dimension(80, 20);
        extractionCombo.setPreferredSize(dim);
        extractionCombo.setMaximumSize(dim);
        extractionCombo.setMinimumSize(dim);
        extractionCombo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int index = extractionCombo.getSelectedIndex();
                    String s = getFilterDate(index);
                    
                    getDiagHistory(s);
                }
            }
        });
        p.add(extractionCombo);

        p.add(Box.createHorizontalGlue());

        // �����t�B�[���h
        p.add (new JLabel("�����F "));        
        countField = new JTextField();
        dim = new Dimension(40, 20);
        countField.setPreferredSize(dim);
        countField.setMaximumSize(dim);
        countField.setMinimumSize(dim);
        countField.setEditable(false);
        p.add(countField);

        p.add(Box.createHorizontalStrut(7));
        
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
        
        return p;
    }
    
    private String getFilterDate(int index) {

        index *= 2;
        String flag = periodValueList[index++];
        String val = periodValueList[index];
        int n = Integer.parseInt(val);
        
        GregorianCalendar today = new GregorianCalendar();
        
        if (flag.equals("YEAR")) {
            today.add(GregorianCalendar.YEAR, n);
        
        } else if (flag.equals("MONTH")) {
            today.add(GregorianCalendar.MONTH, n);
        
        } else if (flag.equals("DATE")) {
            today.add(GregorianCalendar.DATE, n);
        
        } else {
            //assert false : "Invalid Calendar Field: " + flag;
            System.out.println("Invalid Calendar Field: " + flag);
        }
        
        return MMLDate.getDate(today);
    }  
}