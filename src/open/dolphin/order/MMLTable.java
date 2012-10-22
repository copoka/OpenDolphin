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

    private static Hashtable diagnosisCategoryDesc;
    static {
        diagnosisCategoryDesc = new Hashtable(20, 0.75f);
        diagnosisCategoryDesc.put("mainDiagnosis", "��a��");
        diagnosisCategoryDesc.put("complication", "����(����)��");
        diagnosisCategoryDesc.put("drg", "�f�f�Q��(DRG)");
        diagnosisCategoryDesc.put("academicDiagnosis", "�w�p�f�f��");
        diagnosisCategoryDesc.put("claimingDiagnosis", "�㎖�a��");
        diagnosisCategoryDesc.put("clinicalDiagnosis", "�Տ��f�f��");
        diagnosisCategoryDesc.put("pathologicalDiagnosis", "�a���f�f��");
        diagnosisCategoryDesc.put("laboratoryDiagnosis", "�����f�f��");
        diagnosisCategoryDesc.put("operativeDiagnosis", "��p�f�f��");
        diagnosisCategoryDesc.put("confirmedDiagnosis", "�m��f�f");
        diagnosisCategoryDesc.put("suspectedDiagnosis", "�^���a��");        
    }
    public static String getDiagnosisCategoryDesc(String key) {
        return (String)diagnosisCategoryDesc.get(key);
    }
    
    private static Hashtable diagnosisCategoryValue;
    static {
        diagnosisCategoryValue = new Hashtable(20, 0.75f);
        diagnosisCategoryValue.put("��a��", "mainDiagnosis");
        diagnosisCategoryValue.put("����(����)��", "complication");
        diagnosisCategoryValue.put("�f�f�Q��(DRG)", "drg");
        diagnosisCategoryValue.put("�w�p�f�f��", "academicDiagnosis");
        diagnosisCategoryValue.put("�㎖�a��", "claimingDiagnosis");
        diagnosisCategoryValue.put("�Տ��f�f��", "clinicalDiagnosis");
        diagnosisCategoryValue.put("�a���f�f��", "pathologicalDiagnosis");
        diagnosisCategoryValue.put("�����f�f��", "laboratoryDiagnosis");
        diagnosisCategoryValue.put("��p�f�f��", "operativeDiagnosis");
        diagnosisCategoryValue.put("�m��f�f", "confirmedDiagnosis");
        diagnosisCategoryValue.put("�^���a��", "suspectedDiagnosis");        
    }
    public static String getDiagnosisCategoryValue(String key) {
        return (String)diagnosisCategoryValue.get(key);
    }    
    
        
    private static Hashtable diagnosisCategoryTable;
    static {
        diagnosisCategoryTable = new Hashtable(20, 0.75f);
        diagnosisCategoryTable.put("mainDiagnosis", "MML0012");
        diagnosisCategoryTable.put("complication", "MML0012");
        diagnosisCategoryTable.put("drg", "MML0012");
        diagnosisCategoryTable.put("academicDiagnosis", "MML0013");
        diagnosisCategoryTable.put("claimingDiagnosis", "MML0013");
        diagnosisCategoryTable.put("clinicalDiagnosis", "MML0014");
        diagnosisCategoryTable.put("pathologicalDiagnosis", "MML0014");
        diagnosisCategoryTable.put("laboratoryDiagnosis", "MML0014");
        diagnosisCategoryTable.put("operativeDiagnosis", "MML0014");
        diagnosisCategoryTable.put("confirmedDiagnosis", "MML0015");
        diagnosisCategoryTable.put("suspectedDiagnosis", "MML0015");        
    }
    public static String getDiagnosisCategoryTable(String key) {
        return (String)diagnosisCategoryTable.get(key);
    }    

    
    private static Hashtable diagnosisOutcomeDesc;
    static {
        diagnosisOutcomeDesc = new Hashtable(20, 0.75f);
        diagnosisOutcomeDesc.put("died", "���S");
        diagnosisOutcomeDesc.put("worsening", "����");
        diagnosisOutcomeDesc.put("unchanged", "�s��");
        diagnosisOutcomeDesc.put("recovering", "��");
        diagnosisOutcomeDesc.put("fullyRecovered", "�S��");
        diagnosisOutcomeDesc.put("sequelae", "������(�̔���)");
        diagnosisOutcomeDesc.put("end", "�I��");
        diagnosisOutcomeDesc.put("pause", "���~");
        diagnosisOutcomeDesc.put("continued", "�p��");
        diagnosisOutcomeDesc.put("transfer", "�]��");
        diagnosisOutcomeDesc.put("transferAcute", "�]��(�}���a�@��)");
        diagnosisOutcomeDesc.put("transferChronic", "�]��(�����a�@��)"); 
        diagnosisOutcomeDesc.put("home", "����֑މ@"); 
        diagnosisOutcomeDesc.put("unknown", "�s��"); 
    }
    public static String getDiagnosisOutcomeDesc(String key) {
        return (String)diagnosisOutcomeDesc.get(key);
    }    
    
    private static Hashtable diagnosisOutcomeValue;
    static {
        diagnosisOutcomeValue = new Hashtable(20, 0.75f);
        diagnosisOutcomeValue.put("���S", "died");
        diagnosisOutcomeValue.put("����", "worsening");
        diagnosisOutcomeValue.put("�s��", "unchanged");
        diagnosisOutcomeValue.put("��", "recovering");
        diagnosisOutcomeValue.put("�S��", "fullyRecovered");
        diagnosisOutcomeValue.put("������(�̔���)", "sequelae");
        diagnosisOutcomeValue.put("�I��", "end");
        diagnosisOutcomeValue.put("���~", "pause");
        diagnosisOutcomeValue.put("�p��", "continued");
        diagnosisOutcomeValue.put("�]��", "transfer");
        diagnosisOutcomeValue.put("�]��(�}���a�@��)", "transferAcute");
        diagnosisOutcomeValue.put("�]��(�����a�@��)", "transferChronic"); 
        diagnosisOutcomeValue.put("����֑މ@", "home"); 
        diagnosisOutcomeValue.put("�s��", "unknown"); 
    }
    public static String getDiagnosisOutcomeValue(String key) {
        return (String)diagnosisOutcomeValue.get(key);
    }    
    
    
    private static Hashtable claimClassCode;
    static {
     
        claimClassCode = new Hashtable(45, 0.75f);
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
    
    private static Hashtable departmentCode;
    static {
     
        departmentCode = new Hashtable(40, 1.0f);
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
    
    private static Hashtable departmentName;
    static {
     
        departmentName = new Hashtable(40, 1.0f);
        departmentName.put("01", "����");
        departmentName.put("02", "���_��");
        departmentName.put("03", "�_�o��");
        departmentName.put("04", "�_�o����");
        departmentName.put("05", "�ċz���");
        departmentName.put("06", "�������");
        departmentName.put("07", "�ݒ���");
        departmentName.put("08", "�z���");
        departmentName.put("09", "������");
        departmentName.put("10", "�O��");
        departmentName.put("11", "���`�O��");
        departmentName.put("12", "�`���O��");
        departmentName.put("13", "���e�O��");
        departmentName.put("14", "�]�_�o�O��");
        departmentName.put("15", "�ċz��O��");
        departmentName.put("16", "�S�����ǊO��");
        departmentName.put("17", "�����O��");
        departmentName.put("18", "�畆�ДA���");
        departmentName.put("19", "�畆��");
        departmentName.put("20", "�ДA���");
        departmentName.put("21", "���a��");
        departmentName.put("22", "�������");
        departmentName.put("23", "�Y�w�l��");
        departmentName.put("24", "�Y��");
        departmentName.put("25", "�w�l��");
        departmentName.put("26", "���");
        departmentName.put("27", "���@���񂱂���");
        departmentName.put("28", "�C�ǐH����");
        departmentName.put("29", "���w�f�É�");
        departmentName.put("30", "���ː���");
        departmentName.put("31", "������");
        departmentName.put("32", "�l�H���͉�");
        departmentName.put("33", "�S�Ó���");
        departmentName.put("34", "�A�����M�[");
        departmentName.put("35", "���E�}�` ");
        departmentName.put("36", "���n�r��");
        departmentName.put("A1", "�I��");
    }    
    public static String getDepartmentName(String key) {
        return (String)departmentName.get(key);
    }    
}