/*
 * SqlMasterDao.java
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
package open.dolphin.dao;

import java.sql.*;
import java.util.*;
import open.dolphin.infomodel.AdminEntry;

import open.dolphin.infomodel.DiseaseEntry;
import open.dolphin.infomodel.MedicineEntry;
import open.dolphin.infomodel.ToolMaterialEntry;
import open.dolphin.infomodel.TreatmentEntry;
import open.dolphin.util.*;

/**
 * SqlMasterDao
 *
 * @author Kazushi Minagawa
 */
public final class SqlMasterDao extends SqlDaoBean {
    
    private static final String DISEASE_MASTER = "disease";
    private static final String MEDICAL_SUPLLIES = "medicine";
    private static final String ADMIN_MASTER = "admin";
    private static final String MEDICINE_CODE = "20";
    private static final String TREATMENT_MASTER = "treatment";
    private static final String TOOL_MATERIAL_MASTER = "tool_material";
    private static final String YKZKBN = "4";	// ��܋敪
    
    private int totalCount;
    
    /** Creates a new instance of SqlMasterDao */
    public SqlMasterDao() {
    }
    
    public int getTotalCount() {
        return totalCount;
    }
    
    /**
     * �}�X�^�����ڂ̖��O�Ō�������B
     * @param master            �����Ώۂ̃}�X�^
     * @param name		���ڂ̖���
     * @param startsWith	�O����v�̎� TRUE
     * @param serchClassCode	�f�Ís�׃}�X�^�����̏ꍇ�̓_���W�v��R�[�h
     * @param sortBy            �\�[�g����J����
     * @param order             �����܂��͍~��
     * @return                  �}�X�^���ڂ̃��X�g
     */
    public ArrayList getByName(String master, String name, boolean startsWith, String serchClassCode,
            String sortBy, String order) {
        
        // �߂�l�̃��X�g��p�ӂ���
        ArrayList results = null;
        
        // ���p�̃��[�}��(���[�U����)��S�p�̃��[�}��(�}�X�^�Ŏg�p)�ɕϊ�����
        String zenkakuRoman = StringTool.toZenkakuUpperLower(name);
        
        if (master.equals(DISEASE_MASTER)) {
            // ���a���}�X�^����������
            results = getDiseaseByName(name, startsWith, sortBy, order);
            
        } else if (master.equals(MEDICAL_SUPLLIES)) {
            // ���i�}�X�^����������
            if (serchClassCode.equals(MEDICINE_CODE)) {
                // ��܂̌������s��
                results = getMedicineByName(zenkakuRoman, startsWith, sortBy, order);
                
            } else {
                // ���˖�̌������s��
                results = getInjectionByName(zenkakuRoman, startsWith, sortBy, order);
            }
            
        } else if (master.equals(TREATMENT_MASTER)) {
            // �f�Ís�׃}�X�^����������
            results = getTreatmentByName(zenkakuRoman, startsWith, serchClassCode, sortBy, order);
            
        } else if (master.equals(TOOL_MATERIAL_MASTER)) {
            // ����@�ރ}�X�^����������
            results = getToolMaterialByName(zenkakuRoman, startsWith, sortBy, order);
            
        } else if (master.equals(ADMIN_MASTER)) {
            // �p�@�}�X�^����������
            results = getAdminByName(zenkakuRoman, startsWith, sortBy, order);
            
        } else {
            throw new RuntimeException("Unsupported master: " + master);
        }
        
        return results;
    }
    
    
    /**
     * �a���������s���B
     * @param text	�����L�[���[�h
     * @param startsWith �O����v�̎� true
     * @param sortBy �\�[�g����J����
     * @param order �����~���̕�
     * @return �a�����X�g
     */
    private ArrayList<DiseaseEntry> getDiseaseByName(String text, boolean startsWith, String sortBy, String order) {
        
        // �O����v�������s��
        String sql = getDiseaseql(text, sortBy, order, true);
        ArrayList<DiseaseEntry> ret = getDiseaseCollection(sql);
        
        // NoError �Ō��ʂ��Ȃ��Ƃ�������v�������s��
        if (isNoError() && (ret == null || ret.size() == 0) ) {
            sql = getDiseaseql(text, sortBy, order, false);
            ret = getDiseaseCollection(sql);
        }
        
        return ret;
    }
    private String getDiseaseql(String text, String sortBy, String order, boolean forward)  {
        
        String word = null;
        StringBuilder buf = new StringBuilder();
        
        buf.append("select byomeicd, byomei, byomeikana, icd10, haisiymd from tbl_byomei where ");
        
        // �S�Đ����̏ꍇ�̓R�[�h����������
        if (StringTool.isAllDigit(text)) {
            word = text;
            buf.append("byomeicd ~ ");
            
        } else {
            // ����ȊO�͖��̂���������
            word = text;
            buf.append("byomei ~ ");
        }
        
        if (forward) {
            buf.append(addSingleQuote("^" + word));
        } else {
            buf.append(addSingleQuote(word));
        }
        
        String orderBy = getOrderBy(sortBy, order);
        if (orderBy == null) {
            orderBy = " order by byomeicd";
        }
        buf.append(orderBy);
        
        String sql = buf.toString();
        printTrace(sql);
        
        return sql;
    }
    private ArrayList<DiseaseEntry> getDiseaseCollection(String sql) {
        
        Connection con = null;
        ArrayList<DiseaseEntry> collection = null;
        ArrayList<DiseaseEntry> outUse = null;
        Statement st = null;
        
        try {
            con = getConnection();
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            // ValueObject
            DiseaseEntry de = null;
            collection = new ArrayList<DiseaseEntry>();
            outUse = new ArrayList<DiseaseEntry>();
            
            while (rs.next()) {
                de = new DiseaseEntry();
                de.setCode(rs.getString(1));        // Code
                de.setName(rs.getString(2));        // Name
                de.setKana(rs.getString(3));         // Kana
                de.setIcdTen(rs.getString(4));      // IcdTen
                de.setDisUseDate(rs.getString(5));  // DisUseDate
                
                if (de.isInUse()) {
                    collection.add(de);
                } else {
                    outUse.add(de);
                }
            }
            rs.close();
            collection.addAll(outUse);
            
            closeStatement(st);
            closeConnection(con);
            return collection;
            
        } catch (Exception e) {
            processError(e);
            closeConnection(con);
            closeStatement(st);
        }
        return null;
    }
    
    /**
     * ���i�}�X�^����������B
     * @param text	�����L�[���[�h
     * @param startsWith	noUse
     * @param sortBy	�\�[�g������
     * @param order	�����~��
     * @return	���i���X�g
     */
    private ArrayList<MedicineEntry> getMedicineByName(String text, boolean startsWith, String sortBy, String order) {
        // �O����v�������s��
        String sql = getMedicineSql(text, sortBy, order, true);
        ArrayList<MedicineEntry> ret = getMedicineCollection(sql);
        
        // NoError �Ō��ʂ��Ȃ��Ƃ�������v�������s��
        if (isNoError() && (ret == null || ret.size() == 0) ) {
            sql = getMedicineSql(text, sortBy, order, false);
            ret = getMedicineCollection(sql);
        }
        
        return ret;
    }
    private String getMedicineSql(String text, String sortBy, String order, boolean forward) {
        
        //
        // �_���}�X�^��6�Ŏn�܂�A��܋敪�����˖� 4 �łȂ����̂���������
        // 
        String word = null;
        StringBuilder buf = new StringBuilder();
        
        buf.append("select srycd, name, kananame, taniname, tensikibetu, ten, ykzkbn, yakkakjncd, yukostymd, yukoedymd from tbl_tensu where srycd ~ '^6' and ");
        
        // �S�Đ����ł���΃R�[�h���������A����ȊO�͖��̂���������
        if (StringTool.isAllDigit(text)) {
            word = text;
            buf.append("srycd ~ ");
            
        } else {
            word = text;
            buf.append("name ~ ");
        }
        
        if (forward) {
            buf.append(addSingleQuote("^" + word));
        } else {
            buf.append(addSingleQuote(word));
        }
        
        //
        // ���˖�łȂ�
        // 
        buf.append(" and ykzkbn != ");
        buf.append(addSingleQuote(YKZKBN));
        
        String orderBy = getOrderBy(sortBy, order);
        if (orderBy == null) {
            orderBy = " order by srycd";
        }
        buf.append(orderBy);
        
        String sql = buf.toString();
        printTrace(sql);
        
        return sql;
    }
    private ArrayList<MedicineEntry> getMedicineCollection(String sql) {
        
        Connection con = null;
        ArrayList<MedicineEntry> collection = null;
        ArrayList<MedicineEntry> outUse = null;
        Statement st = null;
        
        // �O����v�����݂�
        try {
            con = getConnection();
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            // ValueObject
            MedicineEntry me = null;
            collection = new ArrayList<MedicineEntry>();
            outUse = new ArrayList<MedicineEntry>();
            
            while (rs.next()) {
                me = new MedicineEntry();
                me.setCode(rs.getString(1));        // Code
                me.setName(rs.getString(2));        // Name
                me.setKana(rs.getString(3));        // Name
                me.setUnit(rs.getString(4));        // Unit
                me.setCostFlag(rs.getString(5));    // Cost flag
                me.setCost(rs.getString(6));        // Cost
                me.setYkzKbn(rs.getString(7));      // ��܋敪
                me.setJNCD(rs.getString(8));        // JNCD
                me.setStartDate(rs.getString(9));  // startDate
                me.setEndDate(rs.getString(10));    // endDate
                
                if (me.isInUse()) {
                    collection.add(me);
                } else {
                    outUse.add(me);
                }
            }
            rs.close();
            collection.addAll(outUse);
            
            closeStatement(st);
            closeConnection(con);
            return collection;
            
        } catch (Exception e) {
            processError(e);
            closeStatement(st);
            closeConnection(con);
        }
        return null;
    }
    
    /**
     * ���˖����������B
     * @param text	�����L�[���[�h
     * @param startsWith no use
     * @param sortBy	�\�[�g����J����
     * @param order	�����~��
     * @return		���˖�̃��X�g
     */
    private ArrayList<MedicineEntry> getInjectionByName(String text, boolean startsWith, String sortBy, String order) {
        // �O����v�������s��
        String sql = getInjectionSql(text, sortBy, order, true);
        ArrayList<MedicineEntry> ret = getInjectionCollection(sql);
        
        // NoError �Ō��ʂ��Ȃ��Ƃ�������v�������s��
        if (isNoError() && (ret == null || ret.size() == 0) ) {
            sql = getInjectionSql(text, sortBy, order, false);
            ret = getInjectionCollection(sql);
        }
        
        return ret;
    }
    private String getInjectionSql(String text, String sortBy, String order, boolean forward) {
        
        String word = null;
        StringBuilder buf = new StringBuilder();
        
        buf.append("select srycd, name, kananame, taniname, tensikibetu, ten, ykzkbn, yakkakjncd, yukostymd, yukoedymd from tbl_tensu where srycd ~ '^6' and ");
        
        if (StringTool.isAllDigit(text)) {
            word = text;
            buf.append("srycd ~ ");
            
        } else {
            word = text;
            buf.append("name ~ ");
        }
        if (forward) {
            buf.append(addSingleQuote("^" + word));
        } else {
            buf.append(addSingleQuote(word));
        }
        buf.append(" and ykzkbn = ");
        buf.append(addSingleQuote(YKZKBN));
        
        String orderBy = getOrderBy(sortBy, order);
        if (orderBy == null) {
            orderBy =" order by srycd";
        }
        buf.append(orderBy);
        
        String sql = buf.toString();
        printTrace(sql);
        
        return sql;
    }
    private ArrayList<MedicineEntry> getInjectionCollection(String sql) {
        
        Connection con = null;
        ArrayList<MedicineEntry> collection = null;
        ArrayList<MedicineEntry> outUse = null;
        Statement st = null;
        
        try {
            con = getConnection();
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            // ValueObject
            MedicineEntry me = null;
            collection = new ArrayList<MedicineEntry>();
            outUse = new ArrayList<MedicineEntry>();
            
            while (rs.next()) {
                me = new MedicineEntry();
                me.setCode(rs.getString(1));        // Code
                me.setName(rs.getString(2));        // Name
                me.setKana(rs.getString(3));        // Name
                me.setUnit(rs.getString(4));        // Unit
                me.setCostFlag(rs.getString(5));    // Cost flag
                me.setCost(rs.getString(6));
                me.setYkzKbn(rs.getString(7));      // ��܋敪
                me.setJNCD(rs.getString(8));
                me.setStartDate(rs.getString(9));  // start Date
                me.setEndDate(rs.getString(10));    // end Date
                
                if (me.isInUse()) {
                    collection.add(me);
                } else {
                    outUse.add(me);
                }
            }
            rs.close();
            collection.addAll(outUse);
            
            closeStatement(st);
            closeConnection(con);
            return collection;
            
        } catch (Exception e) {
            processError(e);
            closeStatement(st);
            closeConnection(con);
        }
        return null;
    }
    
    /**
     * �f�Ís�׃}�X�^����������B
     * @param text	�����L�[���[�h
     * @param startsWith	no use
     * @param orderClassCode	�_���W�v��
     * @param sortBy	�\�[�g�J����
     * @param order	�����~��
     * @return	�f�Ís�׃��X�g
     */
    private ArrayList<TreatmentEntry> getTreatmentByName(String text, boolean startsWith, String orderClassCode, String sortBy, String order) {
        // �O����v�������s��
        String sql = getTreatemenrSql(text, orderClassCode, sortBy, order, true);
        ArrayList<TreatmentEntry> ret = getTreatmentCollection(sql);
        
        // NoError �Ō��ʂ��Ȃ��Ƃ�������v�������s��
        if (isNoError() && (ret == null || ret.size() == 0) ) {
            sql = getTreatemenrSql(text, orderClassCode, sortBy, order, false);
            ret = getTreatmentCollection(sql);
        }
        return ret;
    }
    private String getTreatemenrSql(String text,  String orderClassCode, String sortBy, String order, boolean forward) {
        
        String word = null;
        StringBuilder buf = new StringBuilder();
        
        buf.append("select srycd, name, kananame, tensikibetu, ten, nyugaitekkbn, routekkbn, srysyukbn, hospsrykbn, yukostymd, yukoedymd from tbl_tensu where (srycd ~ '^1' or srycd ~ '^00') and ");
        
        if (StringTool.isAllDigit(text)) {
            word = text;
            buf.append("srycd ~ ");
            
        } else {
            word = text;
            buf.append("name ~ ");
        }
        
        if (forward) {
            buf.append(addSingleQuote("^" + word));
        } else {
            buf.append(addSingleQuote(word));
        }
        
        StringBuilder sbd = new StringBuilder();
        if (orderClassCode != null) {
            String[] cClass = new String[]{"",""};
            int index = 0;
            StringTokenizer tokenizer = new StringTokenizer(orderClassCode,"-");
            while (tokenizer.hasMoreTokens()) {
                cClass[index++] = tokenizer.nextToken();
            }
            String min = cClass[0];
            String max = cClass[1];
            
            if ( (! min.equals("")) && max.equals("") ) {
                sbd.append(" and srysyukbn = ");
                sbd.append(addSingleQuote(min));
                
            } else if ((! min.equals("")) && (! max.equals("")) ) {
                sbd.append(" and srysyukbn >= ");
                sbd.append(addSingleQuote(min));
                sbd.append(" and srysyukbn <= ");
                sbd.append(addSingleQuote(max));
            }
        }
        String sql2 = sbd.toString();
        buf.append(sql2);
        
        String orderBy = getOrderBy(sortBy, order);
        if (orderBy == null) {
            orderBy = " order by srycd";
        }
        buf.append(orderBy);
        
        String sql = buf.toString();
        printTrace(sql);
        
        return sql;
    }
    private ArrayList<TreatmentEntry> getTreatmentCollection(String sql) {
        
        Connection con = null;
        ArrayList<TreatmentEntry> collection = null;
        ArrayList<TreatmentEntry> outUse = null;
        Statement st = null;
        
        try {
            con = getConnection();
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            // ValueObject
            TreatmentEntry te = null;
            collection = new ArrayList<TreatmentEntry>();
            outUse = new ArrayList<TreatmentEntry>();
            
            while (rs.next()) {
                te = new TreatmentEntry();
                te.setCode(rs.getString(1));            // srycd
                te.setName(rs.getString(2));            // name
                te.setKana(rs.getString(3));            // kana
                //te.setUnit(rs.getString(3));          // Unit
                te.setCostFlag(rs.getString(4));        // tensikibetu
                te.setCost(rs.getString(5));            // ten
                te.setInOutFlag(rs.getString(6));       // nyugaitekkbn
                te.setOldFlag(rs.getString(7));  	// routekkbn									// OldFlag
                te.setClaimClassCode(rs.getString(8));  // srysuykbn
                te.setHospitalClinicFlag(rs.getString(9)); // hospsrykbn
                te.setStartDate(rs.getString(10));     // start
                te.setEndDate(rs.getString(11));     // end
                
                if (te.isInUse()) {
                    collection.add(te);
                } else {
                    outUse.add(te);
                }
            }
            rs.close();
            collection.addAll(outUse);
            
            closeStatement(st);
            closeConnection(con);
            return collection;
            
        } catch (Exception e) {
            processError(e);
            closeStatement(st);
            closeConnection(con);
        }
        return null;
    }
    
    /**
     * �f�Ís�׃}�X�^����������B
     * @param master
     * @param claimClass �f�Ís�׃R�[�h(�_���W�v��)
     * @param sortBy	�\�[�g�J����
     * @param order	�����~��
     * @return�f�Ís�׃��X�g
     */
    public ArrayList<TreatmentEntry> getByClaimClass(String master, String claimClass, String sortBy, String order) {
        
        Connection con = null;
        ArrayList<TreatmentEntry> collection = null;
        ArrayList<TreatmentEntry> outUse = null;
        Statement st = null;
        
        // �f�Ís�׃R�[�h�͈̔͂𕪉�����
        // ex. 700-799 ��
        String[] cClass = new String[]{"",""};
        int index = 0;
        StringTokenizer tokenizer = new StringTokenizer(claimClass,"-");
        while (tokenizer.hasMoreTokens()) {
            cClass[index++] = tokenizer.nextToken();
        }
        String min = cClass[0];
        String max = cClass[1];
        
        StringBuffer buf = new StringBuffer();
        buf.append("select srycd,name,kananame,tensikibetu,ten,nyugaitekkbn,routekkbn,srysyukbn,hospsrykbn, yukostymd, yukoedymd from tbl_tensu where srycd ~ '^1' and ");
        
        if ( (! min.equals("")) && max.equals("") ) {
            buf.append("srysyukbn = ");
            buf.append(addSingleQuote(min));
            
        } else if ((! min.equals("")) && (! max.equals("")) ) {
            buf.append("srysyukbn >= ");
            buf.append(addSingleQuote(min));
            buf.append(" and srysyukbn <= ");
            buf.append(addSingleQuote(max));
        }
        
        String orderBy = getOrderBy(sortBy, order);
        if (orderBy != null) {
            buf.append(orderBy);
        } else {
            buf.append(" order by srycd");
        }
        
        String sql = buf.toString();
        printTrace(sql);
        
        try {
            con = getConnection();
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            // ValueObject
            TreatmentEntry te = null;
            collection = new ArrayList<TreatmentEntry>();
            outUse = new ArrayList<TreatmentEntry>();
            
            while (rs.next()) {
                te = new TreatmentEntry();
                te.setCode(rs.getString(1));            // srycd
                te.setName(rs.getString(2));            // name
                te.setKana(rs.getString(3));            // kana
                //te.setUnit(rs.getString(3));          // Unit
                te.setCostFlag(rs.getString(4));        // tensikibetu
                te.setCost(rs.getString(5));            // ten
                te.setInOutFlag(rs.getString(6));       // nyugaitekkbn
                te.setOldFlag(rs.getString(7));  	// routekkbn									// OldFlag
                te.setClaimClassCode(rs.getString(8));  // srysuykbn
                te.setHospitalClinicFlag(rs.getString(9)); // hospsrykbn
                te.setStartDate(rs.getString(10));     // start
                te.setEndDate(rs.getString(11));     // end
                
                if (te.isInUse()) {
                    collection.add(te);
                } else {
                    outUse.add(te);
                }
            }
            rs.close();
            collection.addAll(outUse);
            
            closeStatement(st);
            closeConnection(con);
            return collection;
            
        } catch (Exception e) {
            processError(e);
            closeStatement(st);
            closeConnection(con);
        }
        return null;
    }
    
    /**
     * ���ː��B�e���ʂ̌������s���B
     * @param master
     * @param sortBy �\�[�g�J����
     * @param order �����~��
     * @return �f�Ís�׃��X�g
     */
    public ArrayList<TreatmentEntry> getRadLocation(String master, String sortBy, String order) {
        
        Connection con = null;
        ArrayList<TreatmentEntry> collection = null;
        ArrayList<TreatmentEntry> outUse = null;
        Statement st = null;
        
        StringBuffer buf = new StringBuffer();
        buf.append("select srycd,name,kananame,srysyukbn,yukostymd, yukoedymd from tbl_tensu where srycd ~ '^002'");
        String orderBy = getOrderBy(sortBy, order);
        if (orderBy != null) {
            buf.append(orderBy);
        } else {
            buf.append(" order by srycd");
        }
        String sql = buf.toString();
        printTrace(sql);
        
        try {
            con = getConnection();
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            // ValueObject
            TreatmentEntry te = null;
            collection = new ArrayList<TreatmentEntry>();
            outUse = new ArrayList<TreatmentEntry>();
            
            while (rs.next()) {
                te = new TreatmentEntry();
                te.setCode(rs.getString(1));            // srycd
                te.setName(rs.getString(2));            // name
                te.setKana(rs.getString(3));            // kana								// OldFlag
                te.setClaimClassCode(rs.getString(4));  // srysuykbn
                te.setStartDate(rs.getString(5));     // start
                te.setEndDate(rs.getString(6));     // end
                
                if (te.isInUse()) {
                    collection.add(te);
                } else {
                    outUse.add(te);
                }
            }
            rs.close();
            collection.addAll(outUse);
            
            closeStatement(st);
            closeConnection(con);
            return collection;
            
        } catch (Exception e) {
            processError(e);
            closeStatement(st);
            closeConnection(con);
        }
        return null;
    }
    
    /**
     * ����@�ރ}�X�^�̌������s���B
     * @param text �����L�[���[�h
     * @param startsWith no use
     * @param sortBy �\�[�g�J����
     * @param order �����~��
     * @return ����@�ރ��X�g
     */
    private ArrayList<ToolMaterialEntry> getToolMaterialByName(String text, boolean startsWith, String sortBy, String order) {
        // �O����v�������s��
        String sql = getToolMaterialSql(text, sortBy, order, true);
        ArrayList<ToolMaterialEntry> ret = getToolMaterialCollection(sql);
        
        // NoError �Ō��ʂ��Ȃ��Ƃ�������v�������s��
        if (isNoError() && (ret == null || ret.size() == 0) ) {
            sql = getToolMaterialSql(text, sortBy, order, false);
            ret = getToolMaterialCollection(sql);
        }
        return ret;
    }
    private String getToolMaterialSql(String text, String sortBy, String order, boolean forward) {
        
        String word = null;
        StringBuilder buf = new StringBuilder();
        
        buf.append("select srycd, name, kananame, taniname, tensikibetu, ten ,yukostymd, yukoedymd from tbl_tensu where srycd ~ '^7' and ");
        
        if (StringTool.isAllDigit(text)) {
            word = text;
            buf.append("srycd ~ ");
            
        } else {
            word = text;
            buf.append("name ~ ");
        }
        
        if (forward) {
            buf.append(addSingleQuote("^" + word));
        } else {
            buf.append(addSingleQuote(word));
        }
        
        String orderBy = getOrderBy(sortBy, order);
        if (orderBy == null) {
            orderBy = " order by srycd";
        }
        buf.append(orderBy);
        String sql = buf.toString();
        printTrace(sql);
        
        return sql;
    }
    private ArrayList<ToolMaterialEntry> getToolMaterialCollection(String sql) {
        
        Connection con = null;
        ArrayList<ToolMaterialEntry> collection = null;
        ArrayList<ToolMaterialEntry> outUse = null;
        Statement st = null;
        
        try {
            con = getConnection();
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            // ValueObject
            ToolMaterialEntry te = null;
            collection = new ArrayList<ToolMaterialEntry>();
            outUse = new ArrayList<ToolMaterialEntry>();
            
            while (rs.next()) {
                te = new ToolMaterialEntry();
                te.setCode(rs.getString(1));        // Code
                te.setName(rs.getString(2));        // name
                te.setKana(rs.getString(3));        // kana
                te.setUnit(rs.getString(4));        // Unit
                te.setCostFlag(rs.getString(5));    // Cost flag
                te.setCost(rs.getString(6));        // Cost
                te.setStartDate(rs.getString(7));        // start
                te.setEndDate(rs.getString(8));        // end
                
                if (te.isInUse()) {
                    collection.add(te);
                } else {
                    outUse.add(te);
                }
            }
            rs.close();
            collection.addAll(outUse);
            
            closeStatement(st);
            closeConnection(con);
            return collection;
            
        } catch (Exception e) {
            processError(e);
            closeStatement(st);
            closeConnection(con);
        }
        return null;
    }
    
    
    /**
     * �p�@�}�X�^�̌������s���B
     * @param text �����L�[���[�h
     * @param startsWith no use
     * @param sortBy �\�[�g�J����
     * @param order �����~��
     * @return �p�@���X�g
     */
    private ArrayList<AdminEntry> getAdminByName(String text, boolean startsWith, String sortBy, String order) {
        // �O����v�������s��
        String sql = getAdminByNameSql(text, sortBy, order, true);
        ArrayList<AdminEntry> ret = getAdminCollection(sql);
        
        // NoError �Ō��ʂ��Ȃ��Ƃ�������v�������s��
        if (isNoError() && (ret == null || ret.size() == 0) ) {
            sql = getAdminByNameSql(text, sortBy, order, false);
            ret = getAdminCollection(sql);
        }
        return ret;
    }
    private String getAdminByNameSql(String text, String sortBy, String order, boolean forward) {
        
        String word = null;
        StringBuilder buf = new StringBuilder();
        
        buf.append("select srycd, name from tbl_tensu where srycd ~ '^001' and ");
        
        if (StringTool.isAllDigit(text)) {
            word = text;
            buf.append("srycd ~ ");
            
        } else {
            word = text;
            buf.append("name ~ ");
        }
        
        if (forward) {
            buf.append(addSingleQuote("^" + word));
        } else {
            buf.append(addSingleQuote(word));
        }
        
        String orderBy = getOrderBy(sortBy, order);
        if (orderBy == null) {
            orderBy = " order by srycd";
        }
        buf.append(orderBy);
        String sql = buf.toString();
        printTrace(sql);
        
        return sql;
    }
    private ArrayList<AdminEntry> getAdminCollection(String sql) {
        
        Connection con = null;
        ArrayList<AdminEntry> collection = null;
        Statement st = null;
        
        try {
            con = getConnection();
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            // ValueObject
            AdminEntry te = null;
            collection = new ArrayList<AdminEntry>();
            
            while (rs.next()) {
                te = new AdminEntry();
                te.setCode(rs.getString(1));        // Code
                te.setName(rs.getString(2));        // name                
                collection.add(te);
            }
            rs.close();
            
            closeStatement(st);
            closeConnection(con);
            return collection;
            
        } catch (Exception e) {
            processError(e);
            closeStatement(st);
            closeConnection(con);
        }
        return null;
    }
    
    public ArrayList<AdminEntry> getAdminByCategory(String category) {
                
        Connection con = null;
        ArrayList<AdminEntry> collection = null;
        Statement st = null;
        
        StringBuffer buf = new StringBuffer();
        buf.append("select srycd,name from tbl_tensu where srycd ~ '^");
        int index = category.indexOf(' ');
        if (index > 0) {
            String s1 = category.substring(0, index);
            String s2 = category.substring(index+1);
            buf.append(s1);
            buf.append("' or srycd ~ '^");
            buf.append(s2);
            buf.append("'");
            
        } else {
            buf.append(category);
            buf.append("'");
        }
        buf.append(" order by srycd");
        String sql = buf.toString();
        printTrace(sql);
        
        try {
            con = getConnection();
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            // ValueObject
            AdminEntry te = null;
            collection = new ArrayList<AdminEntry>();
            
            while (rs.next()) {
                te = new AdminEntry();
                te.setCode(rs.getString(1));            // srycd
                te.setName(rs.getString(2));            // name
                collection.add(te);
                
            }
            rs.close();
            
            closeStatement(st);
            closeConnection(con);
            return collection;
            
        } catch (Exception e) {
            processError(e);
            closeStatement(st);
            closeConnection(con);
        }
        return null;
        
    }
    
    
    private String getOrderBy(String sortBy, String order) {
        
        StringBuilder buf = null;
        
        if (sortBy != null) {
            buf = new StringBuilder();
            buf.append(" order by ");
            buf.append(sortBy);
        }
        
        if (order != null) {
            buf.append(" ");
            buf.append(order);
        }
        
        return (buf != null) ? buf.toString() : null;
    }
}