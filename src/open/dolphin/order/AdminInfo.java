/*
 * AdminInfo.java
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
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class AdminInfo extends Object {
    
    protected static final int TT_CLASSCODE = 0;
    protected static final int TT_ADMIN     = 1;
    protected static final int TT_MEMO      = 2;
    protected int eventType;
    
    private String class1Code;     // �啪�ރR�[�h
    private String admin;          // 1 + 2;
    private String admin1;         // �啪�ޖ�(����3��A�ڗp��)
    private String admin2;         // �����ޖ�(���H��@��)
    private String adminCode;      // ���Z�p�R�[�h
    private String classCode;      // �f�Ís�׋敪�i�_���W�v��j
    private String numberCode;     // ���ʃR�[�h
    private String adminMemo;      // ����
    private int showState = 2;

    /** 
     * Creates new SimpleMedItem 
     */
    public AdminInfo() {
    }
    
    public String toString() {
        return getAdmin();
//        if (showState == 2) {
//            return admin2;
//        }
//        else {
//            return admin;
//        }
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getAdminCode() {
        return adminCode;
    }

    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getNumberCode() {
        return numberCode;
    }

    public void setNumberCode(String numberCode) {
        this.numberCode = numberCode;
    }

    public String getAdminMemo() {
        return adminMemo;
    }

    public void setAdminMemo(String adminMemo) {
        this.adminMemo = adminMemo;
    }
}