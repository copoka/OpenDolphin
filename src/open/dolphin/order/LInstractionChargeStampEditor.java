package open.dolphin.order;

import javax.swing.*;
import javax.swing.border.*;

import open.dolphin.client.*;

import java.awt.*;
import open.dolphin.client.GUIConst;

/**
 * InstractionCharge editor.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class LInstractionChargeStampEditor extends StampModelEditor  {
           
    private static final long serialVersionUID = -6666962160916099254L;
	
    private ItemTablePanel testTable;
    private MasterSetPanel masterPanel;
        
    /** 
     * Creates new InjectionStampEditor 
     */
    public LInstractionChargeStampEditor(IStampEditorDialog context, MasterSetPanel masterPanel) {
    	setContext(context);
    	this.masterPanel = masterPanel;
    	initComponent();
    }
    
    @Override
    public void start() {
    	ClaimConst.ClaimSpec spec = ClaimConst.ClaimSpec.INSTRACTION_CHARGE;
    	masterPanel.setSearchClass(spec.getSearchCode());   
        masterPanel.startTest(testTable);  // 2003-04-17
    }
    
    private void initComponent() {
        
    	// �w���ݑ��CLAIM �d�l�𓾂�
        ClaimConst.ClaimSpec spec = ClaimConst.ClaimSpec.INSTRACTION_CHARGE;
        
        // �Z�b�g�e�[�u���𐶐��� CLAIM �p�����[�^��ݒ肷��
        testTable = new ItemTablePanel(this);
        testTable.setOrderName(spec.getName());
        testTable.setFindClaimClassCode(true);         // �f�Ís�׋敪�̓}�X�^�A�C�e������
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
        masterPanel.stopTest(testTable);  // 2003-04-17
    }
}