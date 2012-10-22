/*
 * SaveParams.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2003 Digital Globe, Inc. All rights reserved.
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
package open.dolphin.client;

/**
 * Parametrs to save document.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class SaveParams {
    
    /** MML���M���邩�ǂ����̃t���O ���M���鎞 true */
    private boolean sendMML;
    
    /** �����ւ���^�C�g�� */
    private String title;
    
    /** �f�Éȏ�� */
    private String department;
    
    /** ������� */
    private int printCount = -1;
    
    /** ���҂ւ̎Q�Ƃ������邩�ǂ����̃t���O ������Ƃ� true*/
    private boolean allowPatientRef;
    
    /** �f�×��̂���{�݂ւ̎Q�Ƌ��t���O �����鎞 true */
    private boolean allowClinicRef;
    //private ArrayList facilityList;
    
    /** ���ۑ��̎� true */
    private boolean tmpSave;
    
    /** CLAIM ���M�t���O */
    private boolean sendClaim;

    
    /** 
     * Creates new SaveParams 
     */
    public SaveParams() {
        super();
    }
    
    public SaveParams(boolean sendMML) {
        this();
        this.sendMML = sendMML;
    }
    
    public boolean getSendMML() {
        return sendMML;
    }
    
    public void setSendMML(boolean b) {
        sendMML = b;
    }

    public String getTitle() {
        return title;
    }
    
    public void setTitle(String val) {
        title = val;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String val) {
        department = val;
    }
    
    public int getPrintCount() {
        return printCount;
    }
    
    public void setPrintCount(int val) {
        printCount = val;
    }
    
    public boolean isAllowPatientRef() {
        return allowPatientRef;
    }
    
    public void setAllowPatientRef(boolean b) {
        allowPatientRef = b;
    }
    
    public boolean isAllowClinicRef() {
        return allowClinicRef;
    }
    
    public void setAllowClinicRef(boolean b) {
        allowClinicRef = b;
    }
    
//    public ArrayList getFacilityAccessList() {
//        return facilityList;
//    }
//    
//    public void setFacilityAccessList(ArrayList list) {
//        facilityList = list;
//    }

    public boolean isTmpSave() {
        return tmpSave;
    }

    public void setTmpSave(boolean tmpSave) {
        this.tmpSave = tmpSave;
    }
    
    public boolean isSendClaim() {
        return sendClaim;
    }

    public void setSendClaim(boolean sendClaim) {
        this.sendClaim = sendClaim;
    }
}