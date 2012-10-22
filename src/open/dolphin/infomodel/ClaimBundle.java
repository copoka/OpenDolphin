/*
 * ClaimBundle.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
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
 * ClaimBundle �v�f�N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class ClaimBundle extends InfoModel {
          
    String className;           // �f�Ís�ז�
    String classCode;           // �f�Ís�׃R�[�h
    String classCodeSystem;     // �R�[�h�̌n
    String admin;               // �p�@
    String adminCode;           // �p�@�R�[�h
    String adminCodeSystem;     // �p�@�R�[�h�̌n
    String adminMemo;           // �p�@����
    String bundleNumber;        // �o���h����
    ClaimItem[] claimItem;      // �o���h���\���i��
    String memo;                // ����
    
    //TableValue claimClass;     // 2003-12-15
    //MasterValue administration; // 2003-12-15
    
    //String admMemo;         // IMP
    //String bundleNumber;    // ? integer
    //ClaimItem[] items;      // +
    //String memo;
    
    /** Creates new ClaimBundle*/
    public ClaimBundle() {        
    }
        
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String val) {
        className = val;
    } 
    
    public String getClassCode() {
        return classCode;
    }
    
    public void setClassCode(String val) {
        classCode = val;
    }
    
    public String getClassCodeSystem() {
        return classCodeSystem;
    }
    
    public void setClassCodeSystem(String val) {
        classCodeSystem = val;
    }    
    
    public String getAdmin() {
        return admin;
    }
    
    public void setAdmin(String val) {
        admin = val;
    }
    
    public String getAdminCode() {
        return adminCode;
    }
    
    public void setAdminCode(String val) {
        adminCode = val;
    } 
    
    public String getAdminCodeSystem() {
        return adminCodeSystem;
    }
    
    public void setAdminCodeSystem(String val) {
        adminCodeSystem = val;
    } 
    
    public String getAdminMemo() {
        return adminMemo;
    }
    
    public void setAdminMemo(String val) {
        adminMemo = val;
    }  
    
    public String getBundleNumber() {
        return bundleNumber;
    }
    
    public void setBundleNumber(String val) {
        bundleNumber = val;
    } 
    
    public ClaimItem[] getClaimItem() {
        return claimItem;
    }
    
    public void setClaimItem(ClaimItem[] val) {
        claimItem = val;
    }
    
    public void addClaimItem(ClaimItem val) {
        if (claimItem == null) {
            claimItem = new ClaimItem[1];
            claimItem[0] = val;
            return;
        }
        int len = claimItem.length;
        ClaimItem[] dest = new ClaimItem[len + 1];
        System.arraycopy(claimItem, 0, dest, 0, len);
        claimItem = dest;
        claimItem[len] = val;
    }  
    
    public String getMemo() {
        return memo;
    }
    
    public void setMemo(String val) {
        memo = val;
    }         
    
   
    
    /*public TableValue getClaimClass() {
        return claimClass;
    }
    
    public void setClaimClass(TableValue val) {
        claimClass = val;
    }    
    
    public MasterValue getAdministration() {
        return administration;
    }
    
    public void setAdministration(MasterValue val) {
        administration = val;
    }
    
    public String getAdmMemo() {
        return admMemo;
    }
    
    public void setAdmMemo(String val) {
        admMemo = val;
    }  
    
    public String getBundleNumber() {
        return bundleNumber;
    }
    
    public void setBundleNumber(String val) {
        bundleNumber = val;
    } 
    
    public ClaimItem[] getClaimItem() {
        return items;
    }
    
    public void setClaimItem(ClaimItem[] val) {
        items = val;
    }
    
    public void addClaimItem(ClaimItem val) {
        if (items == null) {
            items = new ClaimItem[1];
            items[0] = val;
            return;
        }
        int len = items.length;
        ClaimItem[] dest = new ClaimItem[len + 1];
        System.arraycopy(items,0,dest,0,len);
        items = dest;
        items[len] = val;
    }  
    
    public String getMemo() {
        return memo;
    }
    
    public void setMemo(String val) {
        memo = val;
    }     
        
    public boolean isValidMML() {
        return ((items != null) && (items.length > 0)) ? true : false;
    }*/
}