package open.dolphin.order;

import javax.swing.*;
import javax.swing.border.*;

import open.dolphin.client.*;

import java.awt.*;
import open.dolphin.client.GUIConst;

/**
 * BaseCharge editor.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class LBaseChargeStampEditor extends StampModelEditor  {
           
    private static final long serialVersionUID = 8284352054746435316L;
	
    private ItemTablePanel testTable;
    private MasterSetPanel masterPanel;
    
    /** 
     * Creates new InjectionStampEditor 
     */
    public LBaseChargeStampEditor(IStampEditorDialog context, MasterSetPanel masterPanel) {
    	setContext(context);
    	this.masterPanel = masterPanel;
    	initComponent();
    }
    
    @Override
    public void start() {
    	ClaimConst.ClaimSpec spec = ClaimConst.ClaimSpec.BASE_CHARGE;
    	masterPanel.setSearchClass(spec.getSearchCode());
        masterPanel.startCharge(testTable);
    }
    
    private void initComponent() {
        
    	// �f�f����CLAIM �d�l�𓾂�
        ClaimConst.ClaimSpec spec = ClaimConst.ClaimSpec.BASE_CHARGE;
        
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
    }
    
    public Object getValue() {
        return testTable.getValue();
    }
    
    public void setValue(Object val) {
        testTable.setValue(val);
    }
    
    @Override
    public void dispose() {
        masterPanel.stopCharge(testTable);
    }
}