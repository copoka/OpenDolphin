/*
 * MasterItem.java
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
package open.dolphin.order;

/**
 * Class to hold selected master item information.
 *
 * @author  Kazuhi Minagawa, Digital Globe, Inc.
 */
public class MasterItem implements java.io.Serializable {
    
    private static final long serialVersionUID = -6359300744722498857L;
    
    /** Claim subclass code �}�X�^���ڂ̎�� */
    // 0: ��Z  1: �ޗ�  2: ���
    private int classCode;
    
    /** ���ږ� */
    private String name;
    
    /** ���ڃR�[�h */
    private String code;
    
    /** �R�[�h�̌n�� */
    private String masterTableId;
    
    /** ���� */
    private String number;
    
    /** �P�� */
    private String unit;
    
    /** �㎖�p�a���R�[�h */
    private String claimDiseaseCode;
    
    /** �f�Ís�׋敪(007)�E�_���W�v�� */
    private String claimClassCode;
    
    /** ��܂̏ꍇ�̋敪 ���p1�A�O�p6�A���˖�4 */
    private String ykzKbn;
    
    /**
     * Creates new MasterItem
     */
    public MasterItem() {
    }
    
    public MasterItem(int classCode, String name, String code) {
        this();
        setClassCode(classCode);
        setName(name);
        setCode(code);
    }
    
    public String toString() {
        return getName();
    }
    
    /**
     * @param classCode The classCode to set.
     */
    public void setClassCode(int classCode) {
        this.classCode = classCode;
    }
    
    /**
     * @return Returns the classCode.
     */
    public int getClassCode() {
        return classCode;
    }
    
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param code The code to set.
     */
    public void setCode(String code) {
        this.code = code;
    }
    
    /**
     * @return Returns the code.
     */
    public String getCode() {
        return code;
    }
    
    /**
     * @param masterTableId The masterTableId to set.
     */
    public void setMasterTableId(String masterTableId) {
        this.masterTableId = masterTableId;
    }
    
    /**
     * @return Returns the masterTableId.
     */
    public String getMasterTableId() {
        return masterTableId;
    }
    
    /**
     * @param number The number to set.
     */
    public void setNumber(String number) {
        this.number = number;
    }
    
    /**
     * @return Returns the number.
     */
    public String getNumber() {
        return number;
    }
    
    /**
     * @param unit The unit to set.
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    /**
     * @return Returns the unit.
     */
    public String getUnit() {
        return unit;
    }
    
    /**
     * @param claimDiseaseCode The claimDiseaseCode to set.
     */
    public void setClaimDiseaseCode(String claimDiseaseCode) {
        this.claimDiseaseCode = claimDiseaseCode;
    }
    
    /**
     * @return Returns the claimDiseaseCode.
     */
    public String getClaimDiseaseCode() {
        return claimDiseaseCode;
    }
    
    /**
     * @param claimClassCode The claimClassCode to set.
     */
    public void setClaimClassCode(String claimClassCode) {
        this.claimClassCode = claimClassCode;
    }
    
    /**
     * @return Returns the claimClassCode.
     */
    public String getClaimClassCode() {
        return claimClassCode;
    }

    public String getYkzKbn() {
        return ykzKbn;
    }

    public void setYkzKbn(String ykzKbn) {
        this.ykzKbn = ykzKbn;
    }
}