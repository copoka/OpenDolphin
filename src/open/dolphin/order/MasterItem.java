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
    
    // Claim subclass code
    public int classCode;           // 0: ��Z  1: �ޗ�  2: ���
        
    public String name;             // ���O
    
    public String code;             // �R�[�h
    
    public String masterTableId;    // �R�[�h�̌n��   
    
    public String number;           // ����
    
    public String unit;             // �P��
    
    public String claimDiseaseCode;  // �㎖�p�a���R�[�h
    
    public String claimClassCode;    // �f�Ís�׋敪(007)�E�_���W�v��
    
    /** 
     * Creates new MasterItem 
     */
    public MasterItem() {
    }
    
    public String toString() {
        return name;
    }
}