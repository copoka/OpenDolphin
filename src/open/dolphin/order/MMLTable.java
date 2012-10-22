/*
 * MMLTable.java
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

import java.util.*;

/**
 * MML Table Dictionary class.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class MMLTable {

    /** Creates new MMLTable */
    public MMLTable() {
    }
    
    private static Hashtable<String, String> claimClassCode;
    static {
        claimClassCode = new Hashtable<String, String>(45, 0.75f);
        claimClassCode.put("110", "���f");
        claimClassCode.put("120", "�Đf(�Đf)");
        claimClassCode.put("122", "�Đf(�O���Ǘ����Z)");
        claimClassCode.put("123", "�Đf(���ԊO)");
        claimClassCode.put("124", "�Đf(�x��)");
        claimClassCode.put("125", "�Đf(�[��)");
        claimClassCode.put("130", "�w��");
        claimClassCode.put("140", "�ݑ�");
        claimClassCode.put("210", "����(�����E�ڕ��E����)(���@�O)");
        claimClassCode.put("230", "����(�O�p�E����)(���@�O)");
        claimClassCode.put("240", "����(����)(���@)");
        claimClassCode.put("250", "����(����)");
        claimClassCode.put("260", "����(����)");
        claimClassCode.put("270", "����(����)");
        claimClassCode.put("300", "����(�����w�I���܁E���������_�H�E����)");
        claimClassCode.put("311", "����(�牺�ؓ���)");
        claimClassCode.put("321", "����(�Ö���)");
        claimClassCode.put("331", "����(���̑�)");
        claimClassCode.put("400", "���u");
        claimClassCode.put("500", "��p(��p)");
        claimClassCode.put("502", "��p(�A��)");
        claimClassCode.put("503", "��p(�M�v�X)");
        claimClassCode.put("540", "����");
        claimClassCode.put("600", "����");
        claimClassCode.put("700", "�摜�f�f");
        claimClassCode.put("800", "���̑�");
        claimClassCode.put("903", "���@(���@��)");
        claimClassCode.put("906", "���@(�O��)");
        claimClassCode.put("910", "���@(���@����w�Ǘ���)");
        claimClassCode.put("920", "���@(������@���E���̑�)");
        claimClassCode.put("970", "���@(�H���×{)");
        claimClassCode.put("971", "���@(�W�����S�z)");
    }    
    public static String getClaimClassCodeName(String key) {
        return (String)claimClassCode.get(key);
    }
    
    private static Hashtable<String, String> departmentCode;
    static {
     
        departmentCode = new Hashtable<String, String>(40, 1.0f);
        departmentCode.put("����", "01");
        departmentCode.put("���_��", "02");
        departmentCode.put("�_�o��", "03");
        departmentCode.put("�_�o����", "04");
        departmentCode.put("�ċz���", "05");
        departmentCode.put("�������", "06");
        departmentCode.put("�ݒ���", "07");
        departmentCode.put("�z���", "08");
        departmentCode.put("������", "09");
        departmentCode.put("�O��", "10");
        departmentCode.put("���`�O��", "11");
        departmentCode.put("�`���O��", "12");
        departmentCode.put("���e�O��", "13");
        departmentCode.put("�]�_�o�O��", "14");
        departmentCode.put("�ċz��O��", "15");
        departmentCode.put("�S�����ǊO��", "16");
        departmentCode.put("�����O��", "17");
        departmentCode.put("�畆�ДA���", "18");
        departmentCode.put("�畆��", "19");
        departmentCode.put("�ДA���", "20");
        departmentCode.put("��A��", "20");
        departmentCode.put("���a��", "21");
        departmentCode.put("�������", "22");
        departmentCode.put("�Y�w�l��", "23");
        departmentCode.put("�Y��", "24");
        departmentCode.put("�w�l��", "25");
        departmentCode.put("���", "26");
        departmentCode.put("���@���񂱂���", "27");
        departmentCode.put("�C�ǐH����", "28");
        departmentCode.put("���w�f�É�", "29");
        departmentCode.put("���ː���", "30");
        departmentCode.put("������", "31");
        departmentCode.put("�l�H���͉�", "32");
        departmentCode.put("�S�Ó���", "33");
        departmentCode.put("�A�����M�[", "34");
        departmentCode.put("���E�}�` ", "35");
        departmentCode.put("���n�r��", "36");
        departmentCode.put("�I��", "A1");
    }    
    public static String getDepartmentCode(String key) {
       return (String)departmentCode.get(key);
    }   
}