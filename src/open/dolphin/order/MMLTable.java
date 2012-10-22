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
        diagnosisCategoryDesc.put("mainDiagnosis", "主病名");
        diagnosisCategoryDesc.put("complication", "合併(併存)症");
        diagnosisCategoryDesc.put("drg", "診断群名(DRG)");
        diagnosisCategoryDesc.put("academicDiagnosis", "学術診断名");
        diagnosisCategoryDesc.put("claimingDiagnosis", "医事病名");
        diagnosisCategoryDesc.put("clinicalDiagnosis", "臨床診断名");
        diagnosisCategoryDesc.put("pathologicalDiagnosis", "病理診断名");
        diagnosisCategoryDesc.put("laboratoryDiagnosis", "検査診断名");
        diagnosisCategoryDesc.put("operativeDiagnosis", "手術診断名");
        diagnosisCategoryDesc.put("confirmedDiagnosis", "確定診断");
        diagnosisCategoryDesc.put("suspectedDiagnosis", "疑い病名");        
    }
    public static String getDiagnosisCategoryDesc(String key) {
        return (String)diagnosisCategoryDesc.get(key);
    }
    
    private static Hashtable diagnosisCategoryValue;
    static {
        diagnosisCategoryValue = new Hashtable(20, 0.75f);
        diagnosisCategoryValue.put("主病名", "mainDiagnosis");
        diagnosisCategoryValue.put("合併(併存)症", "complication");
        diagnosisCategoryValue.put("診断群名(DRG)", "drg");
        diagnosisCategoryValue.put("学術診断名", "academicDiagnosis");
        diagnosisCategoryValue.put("医事病名", "claimingDiagnosis");
        diagnosisCategoryValue.put("臨床診断名", "clinicalDiagnosis");
        diagnosisCategoryValue.put("病理診断名", "pathologicalDiagnosis");
        diagnosisCategoryValue.put("検査診断名", "laboratoryDiagnosis");
        diagnosisCategoryValue.put("手術診断名", "operativeDiagnosis");
        diagnosisCategoryValue.put("確定診断", "confirmedDiagnosis");
        diagnosisCategoryValue.put("疑い病名", "suspectedDiagnosis");        
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
        diagnosisOutcomeDesc.put("died", "死亡");
        diagnosisOutcomeDesc.put("worsening", "悪化");
        diagnosisOutcomeDesc.put("unchanged", "不変");
        diagnosisOutcomeDesc.put("recovering", "回復");
        diagnosisOutcomeDesc.put("fullyRecovered", "全治");
        diagnosisOutcomeDesc.put("sequelae", "続発症(の発生)");
        diagnosisOutcomeDesc.put("end", "終了");
        diagnosisOutcomeDesc.put("pause", "中止");
        diagnosisOutcomeDesc.put("continued", "継続");
        diagnosisOutcomeDesc.put("transfer", "転医");
        diagnosisOutcomeDesc.put("transferAcute", "転医(急性病院へ)");
        diagnosisOutcomeDesc.put("transferChronic", "転医(慢性病院へ)"); 
        diagnosisOutcomeDesc.put("home", "自宅へ退院"); 
        diagnosisOutcomeDesc.put("unknown", "不明"); 
    }
    public static String getDiagnosisOutcomeDesc(String key) {
        return (String)diagnosisOutcomeDesc.get(key);
    }    
    
    private static Hashtable diagnosisOutcomeValue;
    static {
        diagnosisOutcomeValue = new Hashtable(20, 0.75f);
        diagnosisOutcomeValue.put("死亡", "died");
        diagnosisOutcomeValue.put("悪化", "worsening");
        diagnosisOutcomeValue.put("不変", "unchanged");
        diagnosisOutcomeValue.put("回復", "recovering");
        diagnosisOutcomeValue.put("全治", "fullyRecovered");
        diagnosisOutcomeValue.put("続発症(の発生)", "sequelae");
        diagnosisOutcomeValue.put("終了", "end");
        diagnosisOutcomeValue.put("中止", "pause");
        diagnosisOutcomeValue.put("継続", "continued");
        diagnosisOutcomeValue.put("転医", "transfer");
        diagnosisOutcomeValue.put("転医(急性病院へ)", "transferAcute");
        diagnosisOutcomeValue.put("転医(慢性病院へ)", "transferChronic"); 
        diagnosisOutcomeValue.put("自宅へ退院", "home"); 
        diagnosisOutcomeValue.put("不明", "unknown"); 
    }
    public static String getDiagnosisOutcomeValue(String key) {
        return (String)diagnosisOutcomeValue.get(key);
    }    
    
    
    private static Hashtable claimClassCode;
    static {
     
        claimClassCode = new Hashtable(45, 0.75f);
        claimClassCode.put("110", "初診");
        claimClassCode.put("120", "再診(再診)");
        claimClassCode.put("122", "再診(外来管理加算)");
        claimClassCode.put("123", "再診(時間外)");
        claimClassCode.put("124", "再診(休日)");
        claimClassCode.put("125", "再診(深夜)");
        claimClassCode.put("130", "指導");
        claimClassCode.put("140", "在宅");
        claimClassCode.put("210", "投薬(内服・頓服・調剤)(入院外)");
        claimClassCode.put("230", "投薬(外用・調剤)(入院外)");
        claimClassCode.put("240", "投薬(調剤)(入院)");
        claimClassCode.put("250", "投薬(処方)");
        claimClassCode.put("260", "投薬(麻毒)");
        claimClassCode.put("270", "投薬(調基)");
        claimClassCode.put("300", "注射(生物学的製剤・精密持続点滴・麻薬)");
        claimClassCode.put("311", "注射(皮下筋肉内)");
        claimClassCode.put("321", "注射(静脈内)");
        claimClassCode.put("331", "注射(その他)");
        claimClassCode.put("400", "処置");
        claimClassCode.put("500", "手術(手術)");
        claimClassCode.put("502", "手術(輸血)");
        claimClassCode.put("503", "手術(ギプス)");
        claimClassCode.put("540", "麻酔");
        claimClassCode.put("600", "検査");
        claimClassCode.put("700", "画像診断");
        claimClassCode.put("800", "その他");
        claimClassCode.put("903", "入院(入院料)");
        claimClassCode.put("906", "入院(外泊)");
        claimClassCode.put("910", "入院(入院時医学管理料)");
        claimClassCode.put("920", "入院(特定入院料・その他)");
        claimClassCode.put("970", "入院(食事療養)");
        claimClassCode.put("971", "入院(標準負担額)");
    }    
    public static String getClaimClassCodeName(String key) {
        return (String)claimClassCode.get(key);
    }
    
    private static Hashtable departmentCode;
    static {
     
        departmentCode = new Hashtable(40, 1.0f);
        departmentCode.put("内科", "01");
        departmentCode.put("精神科", "02");
        departmentCode.put("神経科", "03");
        departmentCode.put("神経内科", "04");
        departmentCode.put("呼吸器科", "05");
        departmentCode.put("消化器科", "06");
        departmentCode.put("胃腸科", "07");
        departmentCode.put("循環器科", "08");
        departmentCode.put("小児科", "09");
        departmentCode.put("外科", "10");
        departmentCode.put("整形外科", "11");
        departmentCode.put("形成外科", "12");
        departmentCode.put("美容外科", "13");
        departmentCode.put("脳神経外科", "14");
        departmentCode.put("呼吸器外科", "15");
        departmentCode.put("心臓血管外科", "16");
        departmentCode.put("小児外科", "17");
        departmentCode.put("皮膚ひ尿器科", "18");
        departmentCode.put("皮膚科", "19");
        departmentCode.put("ひ尿器科", "20");
        departmentCode.put("性病科", "21");
        departmentCode.put("こう門科", "22");
        departmentCode.put("産婦人科", "23");
        departmentCode.put("産科", "24");
        departmentCode.put("婦人科", "25");
        departmentCode.put("眼科", "26");
        departmentCode.put("耳鼻いんこう科", "27");
        departmentCode.put("気管食道科", "28");
        departmentCode.put("理学診療科", "29");
        departmentCode.put("放射線科", "30");
        departmentCode.put("麻酔科", "31");
        departmentCode.put("人工透析科", "32");
        departmentCode.put("心療内科", "33");
        departmentCode.put("アレルギー", "34");
        departmentCode.put("リウマチ ", "35");
        departmentCode.put("リハビリ", "36");
        departmentCode.put("鍼灸", "A1");
    }    
    public static String getDepartmentCode(String key) {
        return (String)departmentCode.get(key);
    }   
    
    private static Hashtable departmentName;
    static {
     
        departmentName = new Hashtable(40, 1.0f);
        departmentName.put("01", "内科");
        departmentName.put("02", "精神科");
        departmentName.put("03", "神経科");
        departmentName.put("04", "神経内科");
        departmentName.put("05", "呼吸器科");
        departmentName.put("06", "消化器科");
        departmentName.put("07", "胃腸科");
        departmentName.put("08", "循環器科");
        departmentName.put("09", "小児科");
        departmentName.put("10", "外科");
        departmentName.put("11", "整形外科");
        departmentName.put("12", "形成外科");
        departmentName.put("13", "美容外科");
        departmentName.put("14", "脳神経外科");
        departmentName.put("15", "呼吸器外科");
        departmentName.put("16", "心臓血管外科");
        departmentName.put("17", "小児外科");
        departmentName.put("18", "皮膚ひ尿器科");
        departmentName.put("19", "皮膚科");
        departmentName.put("20", "ひ尿器科");
        departmentName.put("21", "性病科");
        departmentName.put("22", "こう門科");
        departmentName.put("23", "産婦人科");
        departmentName.put("24", "産科");
        departmentName.put("25", "婦人科");
        departmentName.put("26", "眼科");
        departmentName.put("27", "耳鼻いんこう科");
        departmentName.put("28", "気管食道科");
        departmentName.put("29", "理学診療科");
        departmentName.put("30", "放射線科");
        departmentName.put("31", "麻酔科");
        departmentName.put("32", "人工透析科");
        departmentName.put("33", "心療内科");
        departmentName.put("34", "アレルギー");
        departmentName.put("35", "リウマチ ");
        departmentName.put("36", "リハビリ");
        departmentName.put("A1", "鍼灸");
    }    
    public static String getDepartmentName(String key) {
        return (String)departmentName.get(key);
    }    
}