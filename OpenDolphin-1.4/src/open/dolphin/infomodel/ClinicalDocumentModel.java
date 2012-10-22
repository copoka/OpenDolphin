/*
 * ClinicalDocumentModel.java
 * Copyright (C) 2004 Digital Globe, Inc. All rights reserved.
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

import java.util.ArrayList;

/**
 * ClinicalDocumentModel
 * 
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class ClinicalDocumentModel extends InfoModel {

	private static final long serialVersionUID = 570360867001362278L;

	private PatientLiteModel patientLiteModel;

	private UserLiteModel creatorLiteModel;

	private DocInfoModel docInfo;

	private ModuleModel[] moduleModel;

	/** 
	 * Creates a new instance of ClinicalDocumentModel 
	 */
	public ClinicalDocumentModel() {
		docInfo = new DocInfoModel();
	}

	/**
	 * ��������Ԃ��B
	 * @return �������
	 */
	public DocInfoModel getDocInfoModel() {
		return docInfo;
	}

	/**
	 * ��������ݒ肷��B
	 * @param docInfo �������
	 */
	public void setDocInfoModel(DocInfoModel docInfo) {
		this.docInfo = docInfo;
	}

	/**
	 * ���W���[�����f����ݒ肷��B
	 * @param module ���W���[�����f���̔z��
	 */
	public void setModuleModel(ModuleModel[] module) {
		this.moduleModel = module;
	}

	/**
	 * ���W���[�����f����Ԃ��B
	 * @return ���W���[�����f���̔z��
	 */
	public ModuleModel[] getModuleModel() {
		return moduleModel;
	}

	/**
	 * ���W���[�����f���̔z���ǉ�����B
	 * @param moules ���W���[�����f���̔z��
	 */
	public void addModule(ModuleModel[] moules) {
		if (moduleModel == null) {
			moduleModel = new ModuleModel[moules.length];
			System.arraycopy(moules, 0, moduleModel, 0, moules.length);
			return;
		}
		int len = moduleModel.length;
		ModuleModel[] dest = new ModuleModel[len + moules.length];
		System.arraycopy(moduleModel, 0, dest, 0, len);
		System.arraycopy(moules, 0, dest, len, moules.length);
		moduleModel = dest;
	}

	/**
	 * ���W���[�����f����ǉ�����B
	 * @param value ���W���[�����f��
	 */
	public void addModule(ModuleModel value) {
		if (moduleModel == null) {
			moduleModel = new ModuleModel[1];
			moduleModel[0] = value;
			return;
		}
		int len = moduleModel.length;
		ModuleModel[] dest = new ModuleModel[len + 1];
		System.arraycopy(moduleModel, 0, dest, 0, len);
		moduleModel = dest;
		moduleModel[len] = value;
	}

	/**
	 * �����̃G���e�B�e�B�������W���[�����f����Ԃ��B
	 * @param entityName �G���e�B�e�B�̖��O
	 * @return �Y�����郂�W���[�����f��
	 */
	public ModuleModel getModule(String entityName) {

		if (moduleModel != null) {

			ModuleModel ret = null;
	
			for (ModuleModel model : moduleModel) {
				if (model.getModuleInfo().getEntity().equals(entityName)) {
					ret = model;
					break;
				}
			}
			return ret;
		}
		
		return null;
	}

	/**
	 * �����̃G���e�B�e�B���������W���[������Ԃ��B
	 * @param entityName �G���e�B�e�B�̖��O
	 * @return ���W���[�����
	 */
	public ModuleInfoBean[] getModuleInfo(String entityName) {

		if (moduleModel != null) {
			
			ArrayList<ModuleInfoBean> list = new ArrayList<ModuleInfoBean>(2);
			
			for (ModuleModel model : moduleModel) {
	
				if (model.getModuleInfo().getEntity().equals(entityName)) {
					list.add(model.getModuleInfo());
				}
			}
			
			if (list.size() > 0) {
				return  (ModuleInfoBean[])list.toArray(new ModuleInfoBean[list.size()]);
			}
		}
		
		return null;
	}

	/**
	 * ���҃��f����ݒ肷��B
	 * @param patientLiteModel ���҃��f��
	 */
	public void setPatientLiteModel(PatientLiteModel patientLiteModel) {
		this.patientLiteModel = patientLiteModel;
	}

	/**
	 * ���҃��f����Ԃ��B
	 * @return ���҃��f��
	 */
	public PatientLiteModel getPatientLiteModel() {
		return patientLiteModel;
	}

	/**
	 * �L�ڎ҃��f����ݒ肷��B
	 * @param creatorLiteModel �L�ڎ҃��f��
	 */
	public void setCreatorLiteModel(UserLiteModel creatorLiteModel) {
		this.creatorLiteModel = creatorLiteModel;
	}

	/**
	 * �L�ڎ҃��f����Ԃ��B
	 * @return �L�ڎ҃��f��
	 */
	public UserLiteModel getCreatorLiteModel() {
		return creatorLiteModel;
	}
}