package open.dolphin.order;

import javax.swing.*;
import javax.swing.border.*;

import open.dolphin.client.*;

import java.awt.*;
import open.dolphin.client.GUIConst;

/**
 * TestStampEditor.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class LTestStampEditor extends StampModelEditor  {
	
	private static final long serialVersionUID = -6025524427260907687L;
	
	private ItemTablePanel testTable;
    private MasterSetPanel masterPanel;
        
    /** 
     * Creates new InjectionStampEditor 
     */
    public LTestStampEditor(IStampEditorDialog context, MasterSetPanel masterPanel) {
    	setContext(context);
    	this.masterPanel = masterPanel;
    	initComponent();
    }
    
    @Override
    public void start() {
    	ClaimConst.ClaimSpec spec = ClaimConst.ClaimSpec.LABO_TEST;
    	masterPanel.setSearchClass(spec.getSearchCode());
        masterPanel.startTest(testTable);
    }
    
    private void initComponent() {
                
    	// Labo Test�� CLAIM �d�l�𓾂�
        ClaimConst.ClaimSpec spec = ClaimConst.ClaimSpec.LABO_TEST;
        
        // �Z�b�g�e�[�u���𐶐��� CLAIM �p�����[�^��ݒ肷��
        testTable = new ItemTablePanel(this);
        testTable.setOrderName(spec.getName());
        testTable.setClassCode(spec.getClassCode());
        testTable.setClassCodeId(ClaimConst.CLASS_CODE_ID);
        testTable.setSubClassCodeId(ClaimConst.SUBCLASS_CODE_ID);
        
        // �^�C�g����ݒ肵���C�A�E�g����
        setTitle(spec.getName());
        Border b = BorderFactory.createEtchedBorder();
        testTable.setBorder(BorderFactory.createTitledBorder(b, spec.getName()));
        
        setLayout(new BorderLayout(0, GUIConst.DEFAULT_CMP_V_SPACE));
        add(testTable, BorderLayout.CENTER);
        //setPreferredSize(GUIConst.DEFAULT_STAMP_EDITOR_SIZE);
    }
    
    public Object getValue() {
        return testTable.getValue();
    }
    
    public void setValue(Object val) {
        testTable.setValue(val);
    }
    
    @Override
    public void dispose() {
        masterPanel.stopTest(testTable);
    }
}