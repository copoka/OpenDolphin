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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

/**
 * Stamp �y�� Module �̑�����ێ�����N���X�B
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
@Embeddable
public class ModuleInfoBean extends InfoModel implements StampInfo, Comparable {
    
    private static final long serialVersionUID = -3011774071100921454L;
    
    /** Module ��: StampTree�A �I�[�_���𓖂ɕ\�����閼�O */
    @Column(nullable=false)
    private String name;
    
    /** SOA �܂��� P �̖��� */
    @Column(nullable=false)
    private String role;
    
    /** �h�L�������g�ɏo�����鏇�� */
    @Column(nullable=false)
    private int stampNumber;
    
    /** ���̎��̖� */
    @Column(nullable=false)
    private String entity;
    
    /** �ҏW�\���ǂ��� */
    @Transient
    private boolean editable = true;
    
    /** ASP �񋟂� */
    @Transient
    private boolean asp;
    
    /** DB �ۑ�����Ă���ꍇ�A���̃L�[ */
    @Transient
    private String stampId;
    
    /** Memo �̓��e���� */
    @Transient
    private String memo;
    
    /** �܂�Ԃ��\�����邩�ǂ��� */
    @Transient
    private boolean turnIn;
    
    /**
     * ModuleInfo�I�u�W�F�N�g�𐶐�����B
     */
    public ModuleInfoBean() {
    }
    
    /**
     * �X�^���v����Ԃ��B
     * @return �X�^���v��
     */
    public String getStampName() {
        return name;
    }
    
    /**
     * �X�^���v����ݒ肷��B
     * @param name �X�^���v��
     */
    public void setStampName(String name) {
        this.name = name;
    }
    
    /**
     * �X�^���v�̃��[����Ԃ��B
     * @return �X�^���v�̃��[��
     */
    public String getStampRole() {
        return role;
    }
    
    /**
     * �X�^���v�̃��[����ݒ肷��B
     * @param role �X�^���v�̃��[��
     */
    public void setStampRole(String role) {
        this.role = role;
    }
    
    /**
     * �X�^���v�̃G���e�B�e�B����Ԃ��B
     * @return �G���e�B�e�B��
     */
    public String getEntity() {
        return entity;
    }
    
    /**
     * �X�^���v�̃G���e�B�e�B����ݒ肷��B
     * @param entity �G���e�B�e�B��
     */
    public void setEntity(String entity) {
        this.entity = entity;
    }
    
    /**
     * �V���A���C�Y����Ă��邩�ǂ�����Ԃ��B
     * @return �V���A���C�Y����Ă��鎞 true
     */
    public boolean isSerialized() {
        return stampId != null ? true : false;
    }
    
    /**
     * ASP�񋟂��ǂ�����Ԃ��B
     * @return ASP�񋟂̎� true
     */
    public boolean isASP() {
        return asp;
    }
    
    /**
     * ASP�񋟂�ݒ肷��B
     * @param asp ASP�񋟂̐^�U�l
     */
    public void setASP(boolean asp) {
        this.asp = asp;
    }
    
    /**
     * Databse�ɕۑ�����Ă��鎞�� PK ��ς����B
     * @return Primary Key
     */
    public String getStampId() {
        return stampId;
    }
    
    /**
     * Databse�ɕۑ�����鎞�� PK ��ݒ肷��B
     * @param id Primary Key
     */
    public void setStampId(String id) {
        stampId = id;
    }
    
    /**
     * �X�^���v�̃�����Ԃ��B
     * @return �X�^���v�̃���
     */
    public String getStampMemo() {
        return memo;
    }
    
    /**
     * �X�^���v�̃�����ݒ肷��B
     * @param memo �X�^���v�̃���
     */
    public void setStampMemo(String memo) {
        this.memo = memo;
    }
    
    /**
     * ���̃X�^���v���ҏW�\���ǂ�����ݒ肷��B
     * @param editable �ҏW�\���ǂ����̐^�U�l
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }
    
    /**
     * ���̃X�^���v���ҏW�\���ǂ�����Ԃ��B
     * @return �ҏW�\�̎� true
     */
    public boolean isEditable() {
        return editable;
    }
    
    public void setTurnIn(boolean turnIn) {
        this.turnIn = turnIn;
    }
    
    public boolean isTurnIn() {
        return turnIn;
    }
    
    /**
     * ������\����Ԃ��B
     * @return �X�^���v��
     */
    public String toString() {
        return name;
    }
    
    /**
     * �h�L�������g���̏o���ԍ���ݒ肷��B
     * @param stampNumber�@�o������ԍ�
     */
    public void setStampNumber(int stampNumber) {
        this.stampNumber = stampNumber;
    }
    
    /**
     * �h�L�������g���̏o���ԍ���Ԃ��B
     * @return �h�L�������g���̏o���ԍ�
     */
    public int getStampNumber() {
        return stampNumber;
    }
    
    /**
     * �X�^���v�ԍ��Ŕ�r����B
     * @return ��r�l
     */
    public int compareTo(Object other) {
        if (other != null && getClass() == other.getClass()) {
            int result = getStampNumber() - ((ModuleInfoBean)other).getStampNumber();
            return result;
        }
        return -1;
    }
}
