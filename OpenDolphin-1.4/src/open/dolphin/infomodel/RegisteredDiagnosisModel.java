/*
 * RegisteredDiagnosisModule.java
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
package open.dolphin.infomodel;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * �f�f�����N���X�B
 *
 * @author Kazushi Minagawa, Digital Globe,Inc.
 */
@Entity
@Table(name = "d_diagnosis")
public class RegisteredDiagnosisModel extends KarteEntryBean {
    
    private static final long serialVersionUID = 8449675831667704574L;
    
    // ������
    @Column(nullable=false)
    private String diagnosis;
    
    // �����R�[�h
    private String diagnosisCode;
    
    // �����R�[�h�̌n��
    private String diagnosisCodeSystem;
    
    // �a�����ރ��f��
    @Embedded
    private DiagnosisCategoryModel diagnosisCategoryModel;
    
    // �]�A���f��
    @Embedded
    private DiagnosisOutcomeModel diagnosisOutcomeModel;
    
    // �����̏��f��
    private String firstEncounterDate;
    
    // �֘A���N�ی����
    private String relatedHealthInsurance;
    
//    // �����J�n��
//    private String startDate;
//    
//    // �����I����
//    private String endDate;
    
//    private String firstConfirmDate;
//    
//    private String confirmDate;
    
    @Transient
    private PatientLiteModel patientLiteModel;
    
    @Transient
    private UserLiteModel userLiteModel;
    
    
    /**
     * Creates new RegisteredDiagnosisModule
     */
    public RegisteredDiagnosisModel() {
    }
    
    /**
     * �L���ȃ��f�����ǂ�����Ԃ��B
     * @return �L���ȃ��f���̎� true
     */
    public boolean isValidMML() {
        return getDiagnosis() != null ? true : false;
    }
    
    /**
     * ��������Ԃ��B
     * @return  ������
     */
    public String getDiagnosis() {
        return diagnosis;
    }
    
    /**
     * ��������ݒ肷��B
     * @param diagnosis  ������
     */
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }
    
    /**
     * �����R�[�h��Ԃ��B
     * @return �����R�[�h
     */
    public String getDiagnosisCode() {
        return diagnosisCode;
    }
    
    /**
     * �����R�[�h��ݒ肷��B
     * @param diagnosisCode �����R�[�h
     */
    public void setDiagnosisCode(String diagnosisCode) {
        this.diagnosisCode = diagnosisCode;
    }
    
    /**
     * �����R�[�h�̌n����Ԃ��B
     * @return �����R�[�h�̌n��
     */
    public String getDiagnosisCodeSystem() {
        return diagnosisCodeSystem;
    }
    
    /**
     * �����R�[�h�̌n����ݒ肷��B
     * @param diagnosisCodeSystem �����R�[�h�̌n��
     */
    public void setDiagnosisCodeSystem(String diagnosisCodeSystem) {
        this.diagnosisCodeSystem = diagnosisCodeSystem;
    }
    
    /**
     * ���ޖ���Ԃ��B
     * @return ���ޖ�
     */
    public String getCategory() {
        return diagnosisCategoryModel != null ? diagnosisCategoryModel.getDiagnosisCategory() : null;
    }
    
    /**
     * ���ޖ���ݒ肷��B
     * @param category ���ޖ�
     */
    public void setCategory(String category) {
        if (diagnosisCategoryModel == null) {
            diagnosisCategoryModel = new DiagnosisCategoryModel();
        }
        this.diagnosisCategoryModel.setDiagnosisCategory(category);
    }
    
    /**
     * ���ސ�����Ԃ��B
     * @return ���ސ���
     */
    public String getCategoryDesc() {
        return diagnosisCategoryModel != null ? diagnosisCategoryModel.getDiagnosisCategoryDesc() : null;
    }
    
    /**
     * ���ސ�����ݒ肷��B
     * @param categoryDesc ���ސ���
     */
    public void setCategoryDesc(String categoryDesc) {
        if (diagnosisCategoryModel == null) {
            diagnosisCategoryModel = new DiagnosisCategoryModel();
        }
        this.diagnosisCategoryModel.setDiagnosisCategoryDesc(categoryDesc);
    }
    
    /**
     * ���ޑ̌n����Ԃ��B
     * @return ���ޑ̌n��
     */
    public String getCategoryCodeSys() {
        return diagnosisCategoryModel != null ? diagnosisCategoryModel.getDiagnosisCategoryCodeSys() : null;
    }
    
    /**
     * ���ޑ̌n����ݒ肷��B
     * @param categoryTable ���ޑ̌n��
     */
    public void setCategoryCodeSys(String categoryTable) {
        if (diagnosisCategoryModel == null) {
            diagnosisCategoryModel = new DiagnosisCategoryModel();
        }
        this.diagnosisCategoryModel.setDiagnosisCategoryCodeSys(categoryTable);
    }
    
    /**
     * �������f����Ԃ��B
     * @return �������f��
     */
    public String getFirstEncounterDate() {
        return firstEncounterDate;
    }
    
    /**
     * �������f����ݒ肷��B
     * @param firstEncounterDate �������f��
     */
    public void setFirstEncounterDate(String firstEncounterDate) {
        this.firstEncounterDate = firstEncounterDate;
    }
    
    /**
     * �����J�n����Ԃ��B
     * @return �����J�n��
     */
    public String getStartDate() {
        if (getStarted() != null) {
            return ModelUtils.getDateAsString(getStarted());
        }
        return null;
    }
    
    /**
     * �����J�n����ݒ肷��B
     * @param startDate �����J�n��
     */
    public void setStartDate(String startDate) {
        if (startDate != null) {
            int index = startDate.indexOf('T');
            if (index < 0) {
                startDate += "T00:00:00";
            }
            //System.out.println(startDate);
            setStarted(ModelUtils.getDateTimeAsObject(startDate));
        }
    }
    
    /**
     * �����I������Ԃ��B
     * @return �����I����
     */
    public String getEndDate() {
        if (getEnded() != null) {
            return ModelUtils.getDateAsString(getEnded());
        }
        return null;
    }
    
    /**
     * �����I������ݒ肷��B
     * @param endDate �����I����
     */
    public void setEndDate(String endDate) {
        if (endDate != null) {
            int index = endDate.indexOf('T');
            if (index < 0) {
                endDate += "T00:00:00";
            }
            setEnded(ModelUtils.getDateTimeAsObject(endDate));
        }
    }
    
    /**
     * �]�A��Ԃ��B
     * @return �]�A
     */
    public String getOutcome() {
        return diagnosisOutcomeModel != null ? diagnosisOutcomeModel.getOutcome() : null;
    }
    
    /**
     * �]�A��ݒ肷��B
     * @param outcome �]�A
     */
    public void setOutcome(String outcome) {
        if (diagnosisOutcomeModel == null) {
            diagnosisOutcomeModel = new DiagnosisOutcomeModel();
        }
        this.diagnosisOutcomeModel.setOutcome(outcome);
    }
    
    /**
     * �]�A������Ԃ��B
     * @return �]�A����
     */
    public String getOutcomeDesc() {
        return diagnosisOutcomeModel != null ? diagnosisOutcomeModel.getOutcomeDesc() : null;
    }
    
    /**
     * �]�A������ݒ肷��B
     * @param outcomeDesc �]�A������ݒ�
     */
    public void setOutcomeDesc(String outcomeDesc) {
        if (diagnosisOutcomeModel == null) {
            diagnosisOutcomeModel = new DiagnosisOutcomeModel();
        }
        this.diagnosisOutcomeModel.setOutcomeDesc(outcomeDesc);
    }
    
    /**
     * �]�A�̌n��Ԃ��B
     * @return �]�A�̌n
     */
    public String getOutcomeCodeSys() {
        return diagnosisOutcomeModel != null ? diagnosisOutcomeModel.getOutcomeCodeSys() : null;
    }
    
    /**
     * �]�A�̌n��ݒ肷��B
     * @param outcomeTable
     */
    public void setOutcomeCodeSys(String outcomeTable) {
        if (diagnosisOutcomeModel == null) {
            diagnosisOutcomeModel = new DiagnosisOutcomeModel();
        }
        this.diagnosisOutcomeModel.setOutcomeCodeSys(outcomeTable);
    }
    
    /**
     * �֘A���N�ی�����Ԃ��B
     * @return �֘A���N�ی����
     */
    public String getRelatedHealthInsurance() {
        return relatedHealthInsurance;
    }
    
    /**
     * �֘A���N�ی�����ݒ肷��B
     * @param relatedHealthInsurance �֘A���N�ی����
     */
    public void setRelatedHealthInsurance(String relatedHealthInsurance) {
        this.relatedHealthInsurance = relatedHealthInsurance;
    }
    
    /**
     * �J�e�S�����f����Ԃ��B
     * @return �J�e�S�����f��
     */
    public DiagnosisCategoryModel getDiagnosisCategoryModel() {
        return diagnosisCategoryModel;
    }
    
    /**
     * �J�e�S�����f����ݒ肷��B
     * @param diagnosisOutcomeModel �J�e�S�����f��
     */
    public void setDiagnosisCategoryModel(
            DiagnosisCategoryModel diagnosisCategoryModel) {
        this.diagnosisCategoryModel = diagnosisCategoryModel;
    }
    
    /**
     * �]�A���f����ݒ肷��B
     * @return �]�A���f��
     */
    public DiagnosisOutcomeModel getDiagnosisOutcomeModel() {
        return diagnosisOutcomeModel;
    }
    
    /**
     * �]�A���f����Ԃ��B
     * @param diagnosisOutcomeModel �]�A���f��
     */
    public void setDiagnosisOutcomeModel(
            DiagnosisOutcomeModel diagnosisOutcomeModel) {
        this.diagnosisOutcomeModel = diagnosisOutcomeModel;
    }
    
    public PatientLiteModel getPatientLiteModel() {
        return patientLiteModel;
    }
    
    public void setPatientLiteModel(PatientLiteModel patientLiteModel) {
        this.patientLiteModel = patientLiteModel;
    }
    
    public UserLiteModel getUserLiteModel() {
        return userLiteModel;
    }
    
    public void setUserLiteModel(UserLiteModel userLiteModel) {
        this.userLiteModel = userLiteModel;
    }
    
    private static String[] splitDiagnosis(String diagnosis) {
        if (diagnosis == null) {
            return null;
        }
        String[] ret = null;
        try {
            ret = diagnosis.split("\\s*,\\s*");
        } catch (Exception e) {
        }
        return ret;
    }
    
    public String getDiagnosisName() {
        String[] splits = splitDiagnosis(this.diagnosis);
        return (splits != null && splits.length == 2 && splits[0] != null) ? splits[0] : this.diagnosis;
    }
    
    public String getDiagnosisAlias() {
        String[] splits = splitDiagnosis(this.diagnosis);
        return (splits != null && splits.length == 2 && splits[1] != null) ? splits[1] : null;
    }
    
    public String getAliasOrName() {
        String[] aliasOrName = splitDiagnosis(this.diagnosis);
        if (aliasOrName != null && aliasOrName.length == 2 && aliasOrName[1] != null) {
            return aliasOrName[1];
        }
        return this.diagnosis;
    }
}