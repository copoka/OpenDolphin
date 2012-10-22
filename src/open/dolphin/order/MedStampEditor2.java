package open.dolphin.order;

import java.awt.BorderLayout;
import java.util.EnumSet;

import javax.swing.*;
import javax.swing.border.*;

import open.dolphin.client.*;
import open.dolphin.client.GUIConst;

/**
 * �����X�^���v�G�f�B�^�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class MedStampEditor2 extends StampModelEditor  {
    
    private static final String MEDICINE_TABLETITLE_BORDER    = "�����Z�b�g";
    private static final String EDITOR_NAME = "����";
    
    /** �����Z�b�g�쐬�p�l�� */
    private MedicineTablePanel medicineTable;
    
    /** �}�X�^�Z�b�g�p�l�� */
    private MasterTabPanel masterPanel;
    
    /** Creates new MedStampEditor2 */
    public MedStampEditor2() {
    }
    
    /**
     * �v���O�������J�n����B
     */
    @Override
    public void start() {
        
        setTitle(EDITOR_NAME);
        
        // Medicine table
        medicineTable = new MedicineTablePanel(this);
        Border b = BorderFactory.createEtchedBorder();
        medicineTable.setBorder(BorderFactory.createTitledBorder(b, MEDICINE_TABLETITLE_BORDER));
        
        //
        // �����Ŏg�p����}�X�^���w�肵�A�}�X�^�Z�b�g�p�l���𐶐�����
        //
        EnumSet<ClaimConst.MasterSet> set = EnumSet.of(
                ClaimConst.MasterSet.MEDICAL_SUPPLY,
                ClaimConst.MasterSet.ADMINISTRATION,
                ClaimConst.MasterSet.INJECTION_MEDICINE,
                ClaimConst.MasterSet.TOOL_MATERIAL);
        masterPanel = new MasterTabPanel(set);
        
        //
        // �����쐬�ł��邱�Ƃ�ʒm����
        //
        masterPanel.startMedicine(medicineTable);
        
        //
        // Connects
        //
        medicineTable.setParent(this);
        
        //
        // ��ɃX�^���v�̃Z�b�g�p�l���A���Ƀ}�X�^�̃Z�b�g�p�l����z�u����
        // �S�ẴX�^���v�G�f�B�^�ɋ���
        //
        JPanel top = new JPanel(new BorderLayout());
        top.add(medicineTable, BorderLayout.CENTER);
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(top);
        add(masterPanel);
        setPreferredSize(GUIConst.DEFAULT_STAMP_EDITOR_SIZE);
    }
    
    /**
     * �쐬�����X�^���v��Ԃ��B
     * @return �쐬�����X�^���v
     */
    public Object getValue() {
        return medicineTable.getValue();
    }
    
    /**
     * �ҏW����X�^���v��ݒ肷��B
     * @param val �ҏW����X�^���v
     */
    public void setValue(Object val) {
        System.err.println("setValue");
        medicineTable.setValue(val);
        System.err.println("setValue1");
    }
    
    /**
     * �v���O�������I������B
     */
    @Override
    public void dispose() {
        masterPanel.stopMedicine(medicineTable);
    }
}