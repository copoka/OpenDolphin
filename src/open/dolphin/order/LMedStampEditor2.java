package open.dolphin.order;

import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.border.*;

import open.dolphin.client.*;

/**
 * �����X�^���v�G�f�B�^�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class LMedStampEditor2 extends StampModelEditor  {
    
    private static final long serialVersionUID = 3721140728191931803L;
    
    private static final String MEDICINE_TABLETITLE_BORDER    = "�����Z�b�g";
    private static final String EDITOR_NAME = "����";
    
    private MedicineTablePanel medicineTable;
    private MasterSetPanel masterPanel;
    
    /** Creates new MedStampEditor2 */
    public LMedStampEditor2(IStampEditorDialog context, MasterSetPanel masterPanel) {
        setContext(context);
        this.masterPanel = masterPanel;
        initComponent();
    }
    
    @Override
    public void start() {
        masterPanel.startMedicine(medicineTable);
    }
    
    private void initComponent() {
        
        setTitle(EDITOR_NAME);
        
        // Medicine table
        medicineTable = new MedicineTablePanel(this);
        Border b = BorderFactory.createEtchedBorder();
        medicineTable.setBorder(BorderFactory.createTitledBorder(b, MEDICINE_TABLETITLE_BORDER));
        
        // Connects
        medicineTable.setParent(this);
        
        this.setLayout(new BorderLayout());
        this.add(medicineTable, BorderLayout.CENTER);
        
        //setPreferredSize(GUIConst.DEFAULT_STAMP_EDITOR_SIZE);
    }
    
    public Object getValue() {
        return medicineTable.getValue();
    }
    
    public void setValue(Object val) {
        medicineTable.setValue(val);
    }
    
    @Override
    public void dispose() {
        masterPanel.stopMedicine(medicineTable);
    }
}