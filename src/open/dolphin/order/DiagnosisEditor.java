package open.dolphin.order;

import javax.swing.*;
import javax.swing.border.Border;

import open.dolphin.client.*;
import open.dolphin.client.GUIConst;

import java.awt.*;
import java.util.*;

/**
 * ���a���G�f�B�^�N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class DiagnosisEditor extends StampModelEditor {

    /** �G�f�B�^�� */
    private static final String EDITOR_NAME = "���a��";
    /** ���a���ҏW�e�[�u�� */
    private DiagnosisTablePanel diagnosisTable;
    /** �}�X�^�[�Z�b�g�p�l�� */
    private MasterTabPanel masterPanel;

    /** 
     * Creates new DiagnosisEditor 
     */
    public DiagnosisEditor() {
    }

    /**
     * �v���O�������J�n����B
     */
    @Override
    public void start() {

        setTitle(EDITOR_NAME);

        //
        // ���a���ҏW�e�[�u���𐶐�����
        //
        diagnosisTable = new DiagnosisTablePanel(this);
        Border b = BorderFactory.createEtchedBorder();
        diagnosisTable.setBorder(BorderFactory.createTitledBorder(b, EDITOR_NAME));

        //
        // ���a���Ŏg�p����}�X�^�̃Z�b�g�𐶐�����
        //
        EnumSet<ClaimConst.MasterSet> set = EnumSet.of(
                ClaimConst.MasterSet.DIAGNOSIS);

        //
        // �}�X�^�[�Z�b�g�𐶐�����
        //
        masterPanel = new MasterTabPanel(set);
        masterPanel.startDiagnosis(diagnosisTable);

        //
        // �S�̂����C�A�E�g����
        //
        setLayout(new BorderLayout(0, GUIConst.DEFAULT_CMP_V_SPACE));
        add(diagnosisTable, BorderLayout.NORTH);
        add(masterPanel, BorderLayout.CENTER);
        setPreferredSize(GUIConst.DEFAULT_STAMP_EDITOR_SIZE);
    }

    /**
     * �ҏW�������a����Ԃ��B
     * @return �ҏW���� RegisteredDiagnosisModel
     */
    public Object getValue() {
        return diagnosisTable.getValue();
    }

    /**
     * �ҏW���鏝�a����ݒ肷��B
     * @param val �ҏW���� RegisteredDiagnosisModel
     */
    public void setValue(Object val) {
        diagnosisTable.setValue((Object[]) val);
    }

    /**
     * ���\�[�X���������B
     */
    @Override
    public void dispose() {
        masterPanel.stopDiagnosis(diagnosisTable);
    }
}
