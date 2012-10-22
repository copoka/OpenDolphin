package open.dolphin.order;

import javax.swing.*;
import javax.swing.border.*;

import open.dolphin.client.*;

import java.awt.*;
import java.util.EnumSet;
import open.dolphin.client.GUIConst;

/**
 * TreatmentStampEditor.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class OtherStampEditor extends StampModelEditor  {
            
    private static final long serialVersionUID = -1157611315239742056L;
	
    private ItemTablePanel testTable;
    private MasterTabPanel masterPanel;
    
    /** 
     * Creates new InjectionStampEditor 
     */
    public OtherStampEditor() {
    }
    
    @Override
    public void start() {
                
    	// ���̑���CLAIM �d�l�𓾂�
        ClaimConst.ClaimSpec spec = ClaimConst.ClaimSpec.OTHER;
        
        // �Z�b�g�e�[�u���𐶐��� CLAIM �p�����[�^��ݒ肷��
        testTable = new ItemTablePanel(this);
        testTable.setOrderName(spec.getName());
        testTable.setClassCode(spec.getClassCode());
        testTable.setClassCodeId(ClaimConst.CLASS_CODE_ID);
        testTable.setSubClassCodeId(ClaimConst.SUBCLASS_CODE_ID);
        
        // ���̑��̃}�X�^�Z�b�g�𐶐�����
        EnumSet<ClaimConst.MasterSet> set = EnumSet.of(
        		ClaimConst.MasterSet.TREATMENT,
        		ClaimConst.MasterSet.MEDICAL_SUPPLY,
        		ClaimConst.MasterSet.INJECTION_MEDICINE,
        		ClaimConst.MasterSet.TOOL_MATERIAL);
        // �}�X�^�p�l���𐶐����A�f�Ís�ׂ̌����ΏۃR�[�h�͈͂�ݒ肷��
        masterPanel = new MasterTabPanel(set);
        masterPanel.setSearchClass(spec.getSearchCode());
        masterPanel.startTest(testTable);

        // �^�C�g����ݒ肵���C�A�E�g����
        setTitle(spec.getName());
        Border b = BorderFactory.createEtchedBorder();
        testTable.setBorder(BorderFactory.createTitledBorder(b, spec.getName()));
        
        setLayout(new BorderLayout(0, GUIConst.DEFAULT_CMP_V_SPACE));
        add(testTable, BorderLayout.NORTH);
        add(masterPanel, BorderLayout.CENTER);
        setPreferredSize(GUIConst.DEFAULT_STAMP_EDITOR_SIZE);
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