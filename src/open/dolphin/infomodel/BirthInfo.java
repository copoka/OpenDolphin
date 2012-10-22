/*
 * BirthInfo.java
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
 * BirthInfo �v�f�N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class BirthInfo extends InfoModel {
   
    // �o���{�ݖ��@�ȗ���
    String facilityName;
   
    // ���؏T
    String deliveryWeeks;
   
    // ���ؖ@
    String deliveryMethod;
   
    // �̏d
    String bodyWeight;
   
    // �̏d�P��
    String bodyWeightUnit = "kg";
   
    // �̒�
    String bodyHeight;
   
    // �̒��P��
    String bodyHeightUnit = "cm";
   
    // ����
    String chestCircumference;
   
    // ���͒P��
    String chestCircumferenceUnit = "cm";
   
    // ����
    String headCircumference;
   
    // ���͒P��
    String headCircumferenceUnit = "cm";
   
    // ����
    String memo;
   
    /**
     * �f�t�H���g�R���X�g���N�^
     */
    public BirthInfo() {
    }
  
    // facilityName
    public String getFacilityName() {
        return facilityName;
    }
   
    public void setFacilityName(String value) {
        facilityName = value;
    } 
   
    // deliveryWeeks
    public String getDeliveryWeeks() {
        return deliveryWeeks;
    }
    
    public void setDeliveryWeeks(String value) {
        deliveryWeeks = value;
    }   
   
    // deliveryMethod
    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String value) {
        deliveryMethod = value;
    }
   
    // bodyWeight
    public String getBodyWeight() {
        return bodyWeight;
    }
   
    public void setBodyWeight(String value) {
        bodyWeight = value;
    }
   
    // bodyHeight
    public String getBodyHeight() {
        return bodyHeight;
    }
    
    public void setBodyHeight(String value) {
        bodyHeight = value;
    }
   
    // chestCircumference
    public String getChestCircumference() {
        return chestCircumference;
    }
   
    public void setChestCircumference(String value) {
        chestCircumference = value;
    }
   
    // headCircumference
    public String getHeadCircumference() {
        return headCircumference;
    }
   
    public void setHeadCircumference(String value) {
        headCircumference = value;
    }
   
    // memo
    public String getMemo() {
        return memo;
    }
   
    public void setMemo(String value) {
        memo = value;
    }
   
    public boolean isValidModel() {
        
        // At least one item is not null
        if ( (facilityName != null) && (! facilityName.equals("")) ) {
            return true;
        }
        
        if ( (deliveryWeeks != null) && (! deliveryWeeks.equals("")) ) {
            return true;
        }
        
        if ( (deliveryMethod != null) && (! deliveryMethod.equals("")) ) {
            return true;
        }
        
        if ( (bodyWeight != null) && (! bodyWeight.equals("")) ) {
            return true;
        }
        
        if ( (bodyHeight != null) && (! bodyHeight.equals("")) ) {
            return true;
        }
        
        if ( (chestCircumference != null) && (! chestCircumference.equals("")) ) {
            return true;
        }
        
        if ( (headCircumference != null) && (! headCircumference.equals("")) ) {
            return true;
        }
        
        if ( (memo != null) && (! memo.equals("")) ) {
            return true;
        }
        
        return false;
    }
}