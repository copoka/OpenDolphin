/*
 * SqlMasterDao.java
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

import open.dolphin.infomodel.AdministrationEntry;
import open.dolphin.infomodel.RadiologyMethodEntry;


/**
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public final class SqlDolphinMasterDao extends SqlDaoBean {

    /** Creates a new instance of SqlMasterDao */
    public SqlDolphinMasterDao() {
    }
    
    /**
     * select ... from administration where hiearchyCode1 is not null order by hierarchyCode1
     * valueObject: AdministrationEntry
     */
    public Object[] getAdminClass() {			

        Connection con = null;
        ArrayList collection = null;
        Statement st = null;

        // Constracts sql
        StringBuffer buf = new StringBuffer();
        buf.append("select hierarchyCode1,adminName ");
        buf.append("from tbl_administration where hierarchyCode1 != '' order by hierarchyCode1");

        String sql = buf.toString();
        printTrace(sql);

        try {
            con = getConnection();
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            collection = new ArrayList();

            while (rs.next()) {
                AdministrationEntry ae = new AdministrationEntry();
                ae.setHierarchyCode1(rs.getString(1));									// HierarchyCode1
                ae.setAdminName( rs.getString(2));
                collection.add(ae);
            }
            rs.close();

        } catch (SQLException e) {
            processError(con, collection, "SQLException while getting administration: " + e.toString());
        }

        closeStatement(st);
        closeConnection(con);

        return collection != null ?  collection.toArray() : null;
    }

    /**
     * select ... from administration where hierarchyCode2 like %h1 order by hierarchyCode2
     * valuObject: AdministrationEntry
     */
    public Vector getAdministration(String h1) {

        Connection con = null;
        Vector collection = null;
        Statement st = null;

        // Constracts sql
        StringBuffer buf = new StringBuffer();
        buf.append("select hierarchyCode2,adminName,code,claimClassCode,numberCode,displayName ");
        buf.append("from tbl_administration where  hierarchyCode2 like");
        buf.append(addSingleQuote(h1 + "%"));
        buf.append (" order by hierarchyCode2");

        String sql = buf.toString();
        printTrace(sql);
        
        try {
            con = getConnection();
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            // ValueObject
            AdministrationEntry ae;
            collection = new Vector();

            while (rs.next()) {
                ae = new AdministrationEntry();									// Hierarchy Code1
                ae.setHierarchyCode2(rs.getString(1));     									// Hierarchy Code2
                ae.setAdminName(rs.getString(2));       									// Admin Name
                ae.setCode(rs.getString(3));        												// Code
                ae.setClaimClassCode(rs.getString(4));   									// Claim Class Code
                ae.setNumberCode(rs.getString(5));        									// Number Code
                ae.setDisplayName(rs.getString(6));        								// Display Name
                collection.add(ae);
            }
            rs.close();
            
        } catch (SQLException e) {
            processError(con, collection, "SQLException while getting tbl_administration: " + e.toString());
        }

        closeStatement(st);
        closeConnection(con);

        return collection;
    }

    /**
     * select ... from admin_comment
     * valuObject: String
     */
    public Object[] getAdminComment() {

        Connection con = null;
        Object collection[] = null;
        Statement st = null;

        // Constracts sql
        StringBuffer buf = new StringBuffer();
        buf.append("select admincomment from tbl_admin_comment");

        String sql = buf.toString();
        printTrace(sql);

        try {
            con = getConnection();
            st = con.createStatement();

            // To get number of records to initialize array
            ResultSet rs = st.executeQuery("select count(*) from tbl_admin_comment");

            if(rs.next())
                collection = new String[rs.getInt(1)];

            rs = st.executeQuery(sql);

            int i=0;

            while (rs.next()) {
                collection[i] = new String(rs.getString(1));										// Admin Comment
                i++;
            }
            rs.close();
            
        } catch (SQLException e) {
            processError(con, collection, "SQLException while getting admin comments: " + e.toString());
        }

        closeStatement(st);
        closeConnection(con);

        return collection;
    }


    /**
     * select ... from radiology_method where hierarchyCode1 is not null order by hierarchyCode1
     * valuObject: RadiologyMethodEntry
     */
    public Object[] getRadiologyMethod() {     						//This was orginal

        Connection con = null;
        ArrayList collection = null;
        Statement st = null;

        // Constracts sql
        StringBuffer buf = new StringBuffer();
        buf.append("select hierarchyCode1,name from tbl_radiology_method where hierarchyCode1 ");
        buf.append("!= '' order by hierarchyCode1");

        String sql = buf.toString();
        printTrace(sql);

        try {
            con = getConnection();
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            collection = new ArrayList();

            while (rs.next()) {
                RadiologyMethodEntry re = new RadiologyMethodEntry();
                re.setHierarchyCode1(rs.getString(1));  // Hierarchy Code1
                re.setName(rs.getString(2));										// Name
                collection.add(re);
            }
            rs.close();

        } catch (SQLException e) {
            processError(con, collection, "SQLException while getting radiology methods: " + e.toString());
        }

        closeStatement(st);
        closeConnection(con);

        return collection != null ? collection.toArray() : null;
    }

    /**
     * select ... from radiology_method where hierarchyCode2 like h1% order by hierarchyCode2
     * valuObject: RadiologyMethodEntry
     */
    public Vector getRadiologyComments(String h1) {

	Connection con = null;
        Vector collection = null;
        Statement st = null;

        // Constracts sql
        StringBuffer buf = new StringBuffer();
        buf.append("select hierarchyCode2, name from tbl_radiology_method where hierarchyCode2 like ");
        buf.append(addSingleQuote(h1 + "%"));
        buf.append (" order by hierarchyCode2");

        String sql = buf.toString();
        printTrace(sql);

        try {
            con = getConnection();
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            // ValueObject
            RadiologyMethodEntry re;
            collection = new Vector();

            while (rs.next()) {
                re = new RadiologyMethodEntry();
                re.setHierarchyCode2(rs.getString(1));     							// hierarchyCode2
                re.setName(rs.getString(2));       									// Name
                collection.add(re);
            }
            rs.close();

        } catch (SQLException e) {
            processError(con, collection, "SQLException while getting radiology methods: " + e.toString());
        }

        closeStatement(st);
        closeConnection(con);

        return collection;
    }

}