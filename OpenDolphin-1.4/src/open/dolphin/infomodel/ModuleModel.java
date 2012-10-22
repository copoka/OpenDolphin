/*
 * ModuleModel.java
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

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;


/**
 * ModuleModel
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
@Entity
@Table(name = "d_module")
public class ModuleModel extends KarteEntryBean implements Stamp {
    
    private static final long serialVersionUID = -8781968977231876023L;;
    
    @Embedded
    private ModuleInfoBean moduleInfo;
    
    @Transient
    private IInfoModel model;
    
    @Lob
    @Column(nullable=false)
    private byte[] beanBytes;
    
    @ManyToOne
    @JoinColumn(name="doc_id", nullable=false)
    private DocumentModel document;
    
    /**
     * ModuleModel�I�u�W�F�N�g�𐶐�����B
     */
    public ModuleModel() {
        setModuleInfo(new ModuleInfoBean());
    }
    
    public DocumentModel getDocument() {
        return document;
    }
    
    public void setDocument(DocumentModel document) {
        this.document = document;
    }
    
    /**
     * ���W���[������ݒ肷��B
     * @param moduleInfo ���W���[�����
     */
    public void setModuleInfo(ModuleInfoBean moduleInfo) {
        this.moduleInfo = moduleInfo;
    }
    
    /**
     * ���W���[������Ԃ��B
     * @return ���W���[�����
     */
    public ModuleInfoBean getModuleInfo() {
        return moduleInfo;
    }
    
    /**
     * ���W���[���̏�񃂃f���i���̂�POJO)��ݒ肷��B
     * @param model ���f��
     */
    public void setModel(IInfoModel model) {
        this.model = model;
    }
    
    /**
     * ���W���[���̏�񃂃f���i���̂�POJO)��Ԃ��B
     * @return ���f��
     */
    public IInfoModel getModel() {
        return model;
    }
    
    /**
     * ���W���[���̉i�����o�C�g�z���Ԃ��B
     * @return ���W���[���̉i�����o�C�g�z��
     */
    public byte[] getBeanBytes() {
        return beanBytes;
    }
    
    /**
     * ���W���[���̉i�����o�C�g�z���ݒ肷��B
     * @param beanBytes ���W���[���̉i�����o�C�g�z��
     */
    public void setBeanBytes(byte[] beanBytes) {
        this.beanBytes = beanBytes;
    }
    
    /**
     * �h�L�������g�Ɍ���鏇�ԂŔ�r����B
     * @return ��r�l
     */
    public int compareTo(Object other) {
        if (other != null && getClass() == other.getClass()) {
            ModuleInfoBean moduleInfo1 = getModuleInfo();
            ModuleInfoBean moduleInfo2 = ((ModuleModel)other).getModuleInfo();
            return moduleInfo1.compareTo(moduleInfo2);
        }
        return -1;
    }
}
