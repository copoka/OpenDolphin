/*
 * MasterTabPanel.java
 * Copyright (C) 2007 Dolphin Project. All rights reserved.
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
package open.dolphin.order;

import javax.swing.*;
import javax.swing.event.*;

import open.dolphin.client.BlockGlass;
import open.dolphin.client.GUIFactory;
import open.dolphin.client.SeparatorPanel;
import open.dolphin.client.UltraSonicProgressLabel;

import java.awt.*;
import java.beans.*;
import java.util.EnumSet;

/**
 * TabbedPane contains master serach panels.
 *
 * @author  Kazushi Minagawa, Digital Globe, INc.
 */
public class MasterSetPanel extends JPanel{
    
    private static final long serialVersionUID = 4282518548618120301L;
    
    /** ���a���}�X�^�̃C���f�b�N�X */
    private static final int DIGNOSIS_INDEX         = 0;
    
    /** �f�Ís�׃}�X�^�̃C���f�b�N�X */
    private static final int MEDICAL_TRAET_INDEX    = 1;
    
    /** ���i�}�X�^�̃C���f�b�N�X */
    private static final int MEDICAL_SUPPLY_INDEX   = 2;
    
    /** �p�@�}�X�̃C���f�b�N�X */
    private static final int ADMIN_INDEX            = 3;
    
    /** ���˖�}�X�^�̃C���f�b�N�X */
    private static final int INJECTION_INDEX        = 4;
    
    /** �����ރ}�X�^�̃C���f�b�N�X */
    private static final int TOOL_MATERIAL_INDEX    = 5;
    
    /** �}�X�^�����p�l�����i�[����^�u�y�C�� */
    private JTabbedPane tabbedPane;
    
    /** ���a���}�X�^ */
    private DiagnosisMaster diagnosis;
    
    /** �f�Ís�׃}�X�^ */
    private TreatmentMaster treatment;
    
    /** ���i�}�X�^ */
    private MedicalSuppliesMaster medicalSupplies;
    
    /** �p�@�}�X�^ */
    private AdminMaster administration;
    
    /** ���˖�}�X�^ */
    private InjectionMedicineMaster injection;
    
    /** �����ރ}�X�^ */
    private ToolMaterialMaster toolMaterial;
    
    /** �g�p����}�X�^�� Set */
    private EnumSet<ClaimConst.MasterSet> masterSet;
    
    private BusyListener busyListener;
    private ItemCountListener itemCountListener;
    
    // Status �֘A
    /** Progressbar */
    //private UltraSonicProgressLabel pulse;
    
    /** �����\�����x�� */
    private JLabel countLabel;
    
    /** �f�Ís�׃��x�� */
    private JLabel classCodeLabel;
    
    /** 
     * Creates new MasterTabPanel 
     */
    public MasterSetPanel() {
        super(new BorderLayout(0, 11));
        EnumSet<ClaimConst.MasterSet> enumSet = EnumSet.of(
                ClaimConst.MasterSet.DIAGNOSIS,
                ClaimConst.MasterSet.TREATMENT,
                ClaimConst.MasterSet.MEDICAL_SUPPLY,
                ClaimConst.MasterSet.ADMINISTRATION,
                ClaimConst.MasterSet.INJECTION_MEDICINE,
                ClaimConst.MasterSet.TOOL_MATERIAL);
        setMasterSet(enumSet);
        intialize();
    }
    
    /** 
     * Creates new MasterTabPanel 
     */
    public MasterSetPanel(EnumSet<ClaimConst.MasterSet> enumSet) {
        super(new BorderLayout());
        setMasterSet(enumSet);
        intialize();
    }
    
    /**
     * ���\�[�X���������B
     */
    public void dispose() {
        if (tabbedPane != null) {
            int cnt = tabbedPane.getTabCount();
            for (int i = 0; i < cnt; i++) {
                MasterPanel mp = (MasterPanel) tabbedPane.getComponentAt(i);
                if (mp != null) {
                    mp.dispose();
                }
            }
        }
    }
    
    /**
     * �}�X�^�[�Z�b�g��Ԃ��B
     * @return �}�X�^�[�Z�b�g
     */
    public EnumSet<ClaimConst.MasterSet> getMasterSet() {
        return masterSet;
    }
    
    /**
     * �}�X�^�[�Z�b�g��ݒ肷��B
     * @param masterSet �}�X�^�[�Z�b�g
     */
    public void setMasterSet(EnumSet<ClaimConst.MasterSet> masterSet) {
        this.masterSet = masterSet;
        if (tabbedPane != null) {
            enabled();
        }
    }
    
    /**
     * Glass pane ��ݒ肷��B
     * @param glass �C�x���g�u���b�N���� Glass Pane 
     */
    public void setGlass(BlockGlass glass) {
    }
    
    /**
     * ����������B
     */
    private void intialize() {
        
        // �����g�i���o�[�𐶐�����
        //pulse = new UltraSonicProgressLabel();
        
        // ���a���}�X�^�𐶐�����
        if (masterSet.contains(ClaimConst.MasterSet.DIAGNOSIS)) {
            diagnosis = new DiagnosisMaster(ClaimConst.MasterSet.DIAGNOSIS.getName());
        }
        
        // �f�Ís�׃}�X�^�𐶐�����
        if (masterSet.contains(ClaimConst.MasterSet.TREATMENT)) {
            treatment = new TreatmentMaster(ClaimConst.MasterSet.TREATMENT.getName());
        }
        
        // ���i�}�X�^�𐶐�����
        if (masterSet.contains(ClaimConst.MasterSet.MEDICAL_SUPPLY)) {
            medicalSupplies = new MedicalSuppliesMaster(ClaimConst.MasterSet.MEDICAL_SUPPLY.getName());
        }
        
        // �p�@�}�X�^�𐶐�����
        if (masterSet.contains(ClaimConst.MasterSet.ADMINISTRATION)) {
            administration = new AdminMaster(ClaimConst.MasterSet.ADMINISTRATION.getName());
        }
        
        // ���˖�}�X�^�𐶐�����
        if (masterSet.contains(ClaimConst.MasterSet.INJECTION_MEDICINE)) {
            injection = new InjectionMedicineMaster(ClaimConst.MasterSet.INJECTION_MEDICINE.getName());
        }
        
        // �����ރ}�X�^�𐶐�����
        if (masterSet.contains(ClaimConst.MasterSet.TOOL_MATERIAL)) {
            toolMaterial = new ToolMaterialMaster(ClaimConst.MasterSet.TOOL_MATERIAL.getName());
        }
        
        // BUSY ���X�i�𐶐�����
        busyListener = new BusyListener();
        
        // �������X�i�𐶐�����
        itemCountListener = new ItemCountListener();
        
        // �^�u�y�C���𐶐����}�X�^���i�[����
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(ClaimConst.MasterSet.DIAGNOSIS.getDispName(), diagnosis);
        tabbedPane.addTab(ClaimConst.MasterSet.TREATMENT.getDispName(), treatment);
        tabbedPane.addTab(ClaimConst.MasterSet.MEDICAL_SUPPLY.getDispName(), medicalSupplies);
        tabbedPane.addTab(ClaimConst.MasterSet.ADMINISTRATION.getDispName(), administration);
        tabbedPane.addTab(ClaimConst.MasterSet.INJECTION_MEDICINE.getDispName(), injection);
        tabbedPane.addTab(ClaimConst.MasterSet.TOOL_MATERIAL.getDispName(), toolMaterial);
        
        // �^�u�� ChangeListener ��o�^����
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                //
                // �I�����ꂽ�C���f�b�N�X�ɑΉ�����}�X�^�� enter() ��ʒm����
                //
                int index = tabbedPane.getSelectedIndex();
                MasterPanel masterPanel = (MasterPanel)tabbedPane.getComponentAt(index);
                masterPanel.enter();
            }
        });
        
        // ���� Label
        countLabel = new JLabel(paddCount(0));
        countLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // �f�Ís�הԍ���\�����郉�x���𐶐�����
        classCodeLabel = new JLabel("");
        classCodeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Font��ݒ肷��
        Font font = GUIFactory.createSmallFont();
        countLabel.setFont(font);
        classCodeLabel.setFont(font);
        
        // Status�p�l���𐶐�����
        JPanel statusP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        //statusP.add(pulse);
        statusP.add(countLabel);
        statusP.add(new SeparatorPanel());
        statusP.add(classCodeLabel);
        statusP.add(Box.createHorizontalStrut(6));
        
        this.add(tabbedPane, BorderLayout.CENTER);
        this.add(statusP, BorderLayout.SOUTH);
    }
    
    /**
     * �I�[�_�N���X(�X�^���v�{�b�N�X�̃^�u�Ɋ֘A�Â�����Ă���ԍ�)��ݒ肷��B
     * ���̃R�[�h�����f�Ís�ׂ��}�X�^���猟������B
     * @param code �I�[�_�N���X
     */
    public void setSearchClass(String serchClass) {
        treatment.setSearchClass(serchClass);
        if (serchClass != null) {
            classCodeLabel.setText("�f�Ís��:" + serchClass);
        } else {
            classCodeLabel.setText("�f�Ís��:100-999");
        }
    }
    
    /**
     * �B�e���ʌ����� enable/disable �𐧌䂷��B
     * @param enabled enable�ɂ��鎞 true
     */
    public void setRadLocationEnabled(boolean b) {
        treatment.setRadLocationEnabled(b);
    }
    
    /**
     * �}�X�^�����p�l���ɍ��ڂ��I�����ꂽ���̃��X�i��o�^����B
     * �}�X�^�����p�l���̌��ʃe�[�u���ł̍��ڑI���͑����v���p�e�B�ɂȂ��Ă���B
     * @param l �I�����ڃv���p�e�B�ւ̃��X�i
     */
    private void addListeners(PropertyChangeListener l) {
        
        if (masterSet.contains(ClaimConst.MasterSet.DIAGNOSIS)) {
            diagnosis.addPropertyChangeListener(MasterPanel.SELECTED_ITEM_PROP, l);
            diagnosis.addPropertyChangeListener(MasterPanel.BUSY_PROP, busyListener);
            diagnosis.addPropertyChangeListener(MasterPanel.ITEM_COUNT_PROP, itemCountListener);
        }
        
        if (masterSet.contains(ClaimConst.MasterSet.TREATMENT)) {
            treatment.addPropertyChangeListener(MasterPanel.SELECTED_ITEM_PROP, l);
            treatment.addPropertyChangeListener(MasterPanel.BUSY_PROP, busyListener);
            treatment.addPropertyChangeListener(MasterPanel.ITEM_COUNT_PROP, itemCountListener);
        }
        
        if (masterSet.contains(ClaimConst.MasterSet.MEDICAL_SUPPLY)) {
            medicalSupplies.addPropertyChangeListener(MasterPanel.SELECTED_ITEM_PROP, l);
            medicalSupplies.addPropertyChangeListener(MasterPanel.BUSY_PROP, busyListener);
            medicalSupplies.addPropertyChangeListener(MasterPanel.ITEM_COUNT_PROP, itemCountListener);
        }
        
        // �p�@
        if (masterSet.contains(ClaimConst.MasterSet.ADMINISTRATION)) {
            administration.addPropertyChangeListener(MasterPanel.SELECTED_ITEM_PROP, l);
            administration.addPropertyChangeListener(MasterPanel.BUSY_PROP, busyListener);
            administration.addPropertyChangeListener(MasterPanel.ITEM_COUNT_PROP, itemCountListener);
        }
        
        if (masterSet.contains(ClaimConst.MasterSet.INJECTION_MEDICINE)) {
            injection.addPropertyChangeListener(MasterPanel.SELECTED_ITEM_PROP, l);
            injection.addPropertyChangeListener(MasterPanel.BUSY_PROP, busyListener);
            injection.addPropertyChangeListener(MasterPanel.ITEM_COUNT_PROP, itemCountListener);
        }
        
        if (masterSet.contains(ClaimConst.MasterSet.TOOL_MATERIAL)) {
            toolMaterial.addPropertyChangeListener(MasterPanel.SELECTED_ITEM_PROP, l);
            toolMaterial.addPropertyChangeListener(MasterPanel.BUSY_PROP, busyListener);
            toolMaterial.addPropertyChangeListener(MasterPanel.ITEM_COUNT_PROP, itemCountListener);
        }
    }
    
    /**
     * �v���O�����̏I�����ɍ��ڑI���ւ̑������X�i���폜����B
     * @param l �}�X�^���ڑI���ւ̑������X�i
     */
    private void removeListeners(PropertyChangeListener l) {
        
        if (masterSet.contains(ClaimConst.MasterSet.DIAGNOSIS)) {
            diagnosis.removePropertyChangeListener(MasterPanel.SELECTED_ITEM_PROP, l);
            diagnosis.removePropertyChangeListener(MasterPanel.BUSY_PROP, busyListener);
            diagnosis.removePropertyChangeListener(MasterPanel.ITEM_COUNT_PROP, itemCountListener);
        }
        
        if (masterSet.contains(ClaimConst.MasterSet.TREATMENT)) {
            treatment.removePropertyChangeListener(MasterPanel.SELECTED_ITEM_PROP, l);
            treatment.removePropertyChangeListener(MasterPanel.BUSY_PROP, busyListener);
            treatment.removePropertyChangeListener(MasterPanel.ITEM_COUNT_PROP, itemCountListener);
        }
        
        if (masterSet.contains(ClaimConst.MasterSet.MEDICAL_SUPPLY)) {
            medicalSupplies.removePropertyChangeListener(MasterPanel.SELECTED_ITEM_PROP, l);
            medicalSupplies.removePropertyChangeListener(MasterPanel.BUSY_PROP, busyListener);
            medicalSupplies.removePropertyChangeListener(MasterPanel.ITEM_COUNT_PROP, itemCountListener);
        }
        
         // �p�@
        if (masterSet.contains(ClaimConst.MasterSet.ADMINISTRATION)) {
            administration.removePropertyChangeListener(MasterPanel.SELECTED_ITEM_PROP, l);
            administration.removePropertyChangeListener(MasterPanel.BUSY_PROP, busyListener);
            administration.removePropertyChangeListener(MasterPanel.ITEM_COUNT_PROP, itemCountListener);
        }
        
        if (masterSet.contains(ClaimConst.MasterSet.INJECTION_MEDICINE)) {
            injection.removePropertyChangeListener(MasterPanel.SELECTED_ITEM_PROP, l);
            injection.removePropertyChangeListener(MasterPanel.BUSY_PROP, busyListener);
            injection.removePropertyChangeListener(MasterPanel.ITEM_COUNT_PROP, itemCountListener);
        }
        
        if (masterSet.contains(ClaimConst.MasterSet.TOOL_MATERIAL)) {
            toolMaterial.removePropertyChangeListener(MasterPanel.SELECTED_ITEM_PROP, l);
            toolMaterial.removePropertyChangeListener(MasterPanel.BUSY_PROP, busyListener);
            toolMaterial.removePropertyChangeListener(MasterPanel.ITEM_COUNT_PROP, itemCountListener);
        }
    }
    
    /**
     * �g�p�����}�X�^�̃^�u�� enabled �ɂ���B
     * �g�p����Ȃ��}�X�^�̃^�u�� disabled �ɂ���B
     */
    private void enabled() {
        
        tabbedPane.setEnabledAt(DIGNOSIS_INDEX, false);
        tabbedPane.setEnabledAt(MEDICAL_TRAET_INDEX, false);
        tabbedPane.setEnabledAt(MEDICAL_SUPPLY_INDEX, false);
        tabbedPane.setEnabledAt(ADMIN_INDEX, false);
        tabbedPane.setEnabledAt(INJECTION_INDEX, false);
        tabbedPane.setEnabledAt(TOOL_MATERIAL_INDEX, false);
        
        if (masterSet.contains(ClaimConst.MasterSet.DIAGNOSIS)) {
            tabbedPane.setEnabledAt(DIGNOSIS_INDEX, true);
        }
        
        if (masterSet.contains(ClaimConst.MasterSet.TREATMENT)) {
            tabbedPane.setEnabledAt(MEDICAL_TRAET_INDEX, true);
        }
        
        if (masterSet.contains(ClaimConst.MasterSet.MEDICAL_SUPPLY)) {
            tabbedPane.setEnabledAt(MEDICAL_SUPPLY_INDEX, true);
        }
        
        if (masterSet.contains(ClaimConst.MasterSet.ADMINISTRATION)) {
            tabbedPane.setEnabledAt(ADMIN_INDEX, true);
        }
        
        if (masterSet.contains(ClaimConst.MasterSet.INJECTION_MEDICINE)) {
            tabbedPane.setEnabledAt(INJECTION_INDEX, true);
        }
        
        if (masterSet.contains(ClaimConst.MasterSet.TOOL_MATERIAL)) {
            tabbedPane.setEnabledAt(TOOL_MATERIAL_INDEX, true);
        }
    }
    
    /**
     * ���a���ҏW���J�n����B
     * @param listener ���a���G�f�B�^
     */
    public void startDiagnosis(PropertyChangeListener listener) {
        classCodeLabel.setText("MEDIS ICD10");
        EnumSet<ClaimConst.MasterSet> enumSet = EnumSet.of(
                ClaimConst.MasterSet.DIAGNOSIS);
        setMasterSet(enumSet);
        addListeners(listener);
        tabbedPane.setSelectedIndex(DIGNOSIS_INDEX);
        diagnosis.enter();
    }
    
    /**
     * ���a���ҏW���I������B
     * @param listener ���a���G�f�B�^
     */
    public void stopDiagnosis(PropertyChangeListener listener) {
        removeListeners(listener);
    }
    
    /**
     * �����G�f�B�^���J�n����B
     * @param editor �����G�f�B�^
     */
    public void startMedicine(PropertyChangeListener editor) {
        // ��ށE���i�E���˖�
        classCodeLabel.setText("�f�Ís��:210-230");
        EnumSet<ClaimConst.MasterSet> enumSet = EnumSet.of(
                ClaimConst.MasterSet.MEDICAL_SUPPLY,
                ClaimConst.MasterSet.ADMINISTRATION,
                ClaimConst.MasterSet.INJECTION_MEDICINE,
                ClaimConst.MasterSet.TOOL_MATERIAL);
        setMasterSet(enumSet);
        addListeners(editor);
        tabbedPane.setSelectedIndex(MEDICAL_SUPPLY_INDEX);
    }
    
    /**
     * �����G�f�B�^���I������B
     * @param editor �����G�f�B�^
     */
    public void stopMedicine(PropertyChangeListener editor) {
        removeListeners(editor);
    }
    
    /**
     * ���˃G�f�B�^���J�n����B
     * @param editor ���˃G�f�B�^
     */
    public void startInjection(PropertyChangeListener editor) {
        //�f�Ís�ׁE���˖�E���
        EnumSet<ClaimConst.MasterSet> enumSet = EnumSet.of(
                ClaimConst.MasterSet.TREATMENT,
                ClaimConst.MasterSet.INJECTION_MEDICINE,
                ClaimConst.MasterSet.TOOL_MATERIAL);
        setMasterSet(enumSet);
        addListeners(editor);
        tabbedPane.setSelectedIndex(MEDICAL_TRAET_INDEX);
    }
    
    /**
     * ���˃G�f�B�^���I������B
     * @param editor
     */
    public void stopInjection(PropertyChangeListener editor) {
        removeListeners(editor);
    }
    
    /**
     * �f�f��/�w���ݑ�G�f�B�^���J�n����B
     * @param editor �f�f��/�w���ݑ�
     */
    public void startCharge(PropertyChangeListener editor) {
        // �f�Ís�אݒ�
        EnumSet<ClaimConst.MasterSet> enumSet = EnumSet.of(
                ClaimConst.MasterSet.TREATMENT);
        setMasterSet(enumSet);
        addListeners(editor);
        tabbedPane.setSelectedIndex(MEDICAL_TRAET_INDEX);
    }
    
    /**
     * �f�f��/�w���ݑ�G�f�B�^���I������B
     * @param editor �f�f��/�w���ݑ�
     */
    public void stopCharge(PropertyChangeListener editor) {
        removeListeners(editor);
    }
    
    /**
     * ���u/���ː�/���̌���/��p/���̌���/���̑��G�f�B�^���J�n����B
     * @param editor �G�f�B�^
     */
    public void startTest(PropertyChangeListener editor) {
        // �f�Ís�ׁE��ށE���i�E���˖�
        EnumSet<ClaimConst.MasterSet> enumSet = EnumSet.of(
                ClaimConst.MasterSet.TREATMENT,
                ClaimConst.MasterSet.MEDICAL_SUPPLY,
                ClaimConst.MasterSet.INJECTION_MEDICINE,
                ClaimConst.MasterSet.TOOL_MATERIAL);
        setMasterSet(enumSet);
        addListeners(editor);
        tabbedPane.setSelectedIndex(MEDICAL_TRAET_INDEX);
    }
    
    /**
     * ���u/���ː�/���̌���/��p/���̌���/���̑��G�f�B�^���I������B
     * @param editor �G�f�B�^
     */
    public void stopTest(PropertyChangeListener editor) {
        removeListeners(editor);
    }
    
    /**
     * �ėp�G�f�B�^���J�n����B
     * @param l
     */
    public void startGeneral(PropertyChangeListener editor) {
        // �ėp����
        treatment.setSearchClass(null);
        // �f�Ís�ׁE��ށE���i�E���˖�
        EnumSet<ClaimConst.MasterSet> enumSet = EnumSet.of(
                ClaimConst.MasterSet.TREATMENT,
                ClaimConst.MasterSet.MEDICAL_SUPPLY,
                ClaimConst.MasterSet.INJECTION_MEDICINE,
                ClaimConst.MasterSet.TOOL_MATERIAL);
        setMasterSet(enumSet);
        addListeners(editor);
        tabbedPane.setSelectedIndex(MEDICAL_TRAET_INDEX);
    }
    
    public void stopGeneral(PropertyChangeListener l) {
        removeListeners(l);
    }
        
    /**
     * �e�t���[���� GlassPane ��Ԃ��B
     * @return �e�t���[���� BlockGlass
     */
    protected BlockGlass getBlockGlass() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w != null && w instanceof JFrame) {
            JFrame frame = (JFrame) w;
            Component c = frame.getGlassPane();
            return c != null && c instanceof BlockGlass ? (BlockGlass) c : null;
        }
        return null;
    }
    
    /**
     * Block ����B
     */
    protected void block() {
        BlockGlass glass = getBlockGlass();
        if (glass != null) {
            glass.block();
        }
    }
    
    /**
     * Unblock ����B
     */
    protected void unblock() {
        BlockGlass glass = getBlockGlass();
        if (glass != null) {
            glass.unblock();
        }
    }
    
    protected class BusyListener implements PropertyChangeListener {
        
        public void propertyChange(PropertyChangeEvent e) {
            
            if (e.getPropertyName().equals(MasterPanel.BUSY_PROP)) {
                
                boolean busy = ((Boolean)e.getNewValue()).booleanValue();
                if (busy) {
                    //glass.start();
                    countLabel.setText("����:  ? ");
                    block();
                } else {
                    //glass.stop();
                    unblock();
                }
            }
        }
    }
    
    protected class ItemCountListener implements PropertyChangeListener {
        
        public void propertyChange(PropertyChangeEvent e) {
            
            if (e.getPropertyName().equals(MasterPanel.ITEM_COUNT_PROP)) {
                int count = ((Integer)e.getNewValue()).intValue();
                countLabel.setText(paddCount(count));
            }
        }
    }
    
    private String paddCount(int num) {
        StringBuilder sb = new StringBuilder();
        sb.append("����:");
        String numStr = String.valueOf(num);
        int len = numStr.length();
        int cnt = 4 - len;
        for (int i = 0; i < cnt; i++) {
            sb.append(" ");
        }
        sb.append(numStr);
        return sb.toString();
    }
}











