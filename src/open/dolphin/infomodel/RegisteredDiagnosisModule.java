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


/**
 * �f�f�����N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe,Inc.
 */
public class RegisteredDiagnosisModule extends InfoModel {
   
   private // ������
   String diagnosis;
   
   private // �����R�[�h
   String diagnosisCode;
   
   private // �����R�[�h�̌n��
   String diagnosisCodeSystem;
   
   private // �f�f���̕���
   String category;
   
   private // ���ރe�[�u��
   String categoryTable;
   
   private // �J�n��
   String startDate;
   
   private // �I����
   String endDate;
   
   private // �]�A
   String outcome;
   
   private // �]�A�e�[�u����
   String outcomeTable;
   
   private // �����̏��f��
   String firstEncounterDate;
   
   private // �֘A���N�ی����
   String relatedHealthInsurance;
   
   /** Creates new RegisteredDiagnosisModule */
   public RegisteredDiagnosisModule() {
   }
      
   // isValid?
   public boolean isValidMML() {
      return getDiagnosis() != null ? true : false;
   }

	public void setDiagnosis(String diagnosis) {
		this.diagnosis = diagnosis;
	}
	
	public String getDiagnosis() {
		return diagnosis;
	}
	
	public void setDiagnosisCode(String diagnosisCode) {
		this.diagnosisCode = diagnosisCode;
	}
	
	public String getDiagnosisCode() {
		return diagnosisCode;
	}
	
	public void setDiagnosisCodeSystem(String diagnosisCodeSystem) {
		this.diagnosisCodeSystem = diagnosisCodeSystem;
	}
	
	public String getDiagnosisCodeSystem() {
		return diagnosisCodeSystem;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getCategory() {
		return category;
	}
	
	public void setCategoryTable(String categoryTable) {
		this.categoryTable = categoryTable;
	}
	
	public String getCategoryTable() {
		return categoryTable;
	}
	
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	
	public String getStartDate() {
		return startDate;
	}
	
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
	public String getEndDate() {
		return endDate;
	}
	
	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}
	
	public String getOutcome() {
		return outcome;
	}
	
	public void setOutcomeTable(String outcomeTable) {
		this.outcomeTable = outcomeTable;
	}
	
	public String getOutcomeTable() {
		return outcomeTable;
	}
	
	public void setFirstEncounterDate(String firstEncounterDate) {
		this.firstEncounterDate = firstEncounterDate;
	}
	
	public String getFirstEncounterDate() {
		return firstEncounterDate;
	}
	
	public void setRelatedHealthInsurance(String relatedHealthInsurance) {
		this.relatedHealthInsurance = relatedHealthInsurance;
	}
	
	public String getRelatedHealthInsurance() {
		return relatedHealthInsurance;
	}
}