/*
 * ModuleInfo.java
 * Copyright (C) 2003,2004 Digital Globe, Inc. All rights reserved. 
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
 * Stamp �y�� Module �̑�����ێ�����N���X�B 
 * 
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class ModuleInfo extends InfoModel implements StampInfo {
	        
	/** Module ��: StampTree�A �I�[�_���𓖂ɕ\�����閼�O */
	private String name;

	/** SOA �܂��� P �̖��� */
	private String role;

	/** ���̎��̖� */
	private String entity;
	
	/** �ҏW�\���ǂ��� */
	private boolean editable = true;
	
	/** GCP Visit */
	private String gcpVisit;

	/** ASP �񋟂� */
	private boolean asp;

	/** DB �ۑ�����Ă���ꍇ�A���̃L�[ */
	private String stampId;
   
	/** Memo �̓��e���� */
	private String memo;
	
	/** ModuleId */
	private String moduleId;
	
	/** Module ParentId */
	private String ParentId;
	
	/** ParentId relation */
	private String parentIdRelation;
	
	/** FirstConfirmDate */
	private String firstConfirmDate;
	
	/** ConfirmDate */
	private String confirmDate;

	public ModuleInfo() {
	}
        
	public String getName() {
	   return name;
	}
   
	public void setName(String value) {
	   name = value;
	}

	public String getRole() {
		return role;
	}
    
	public void setRole(String value) {
		role = value;
	}
    
	public String getEntity() {
	   return entity;
	}
   
	public void setEntity(String value) {
	   entity = value;
	}

	public boolean isSerialized() {
		return stampId != null ? true : false;
	}
    
	public boolean isASP() {
		return asp;
	}
    
	public void setASP(boolean b) {
		asp = b;
	}    
    
	public String getStampId() {
	   return stampId;
	}
   
	public void setStampId(String value) {
	   stampId = value;
	}
    
	public String getMemo() {
	   return memo;
	}
   
	public void setMemo(String value) {
	   memo = value;
	}
   
	public String toString() {
	   return name;
	}
    
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setParentId(String parentId) {
		ParentId = parentId;
	}

	public String getParentId() {
		return ParentId;
	}

	public void setParentIdRelation(String parentIdRelation) {
		this.parentIdRelation = parentIdRelation;
	}

	public String getParentIdRelation() {
		return parentIdRelation;
	}

	public void setFirstConfirmDate(String firstConfirmDate) {
		this.firstConfirmDate = firstConfirmDate;
	}

	public String getFirstConfirmDate() {
		return firstConfirmDate;
	}

	public void setConfirmDate(String confirmDate) {
		this.confirmDate = confirmDate;
	}

	public String getConfirmDate() {
		return confirmDate;
	}

	public void setGcpVisit(String gcpVisit) {
		this.gcpVisit = gcpVisit;
	}

	public String getGcpVisit() {
		return gcpVisit;
	}
	
	public boolean isGcp() {
		return gcpVisit != null ? true : false;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public boolean isEditable() {
		return editable;
	}	
}
