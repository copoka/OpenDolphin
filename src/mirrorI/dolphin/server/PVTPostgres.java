/*
 * PVTPostgres.java
 *
 * Created on 2002/12/17
 *
 * Last updated on 2002/12/31.
 * Revised on 2003/01/06 for Null Pointer Exception at pvtHealthInsurance.toString() and when pvtClaim is NULL.
 * Revised on 2003/01/08 logic for checking previous information of the patient in 'checkPatientEntry' is changed.
 * Revised on 2003/01/09 for adding 'marital status' checking in  'checkPatientEntry'.
 *
 */
package mirrorI.dolphin.server;

import java.util.*;
import java.sql.*;
//import open.dolphin.server.*;
import java.io.*;
import java.util.logging.*;

import open.dolphin.dao.*;
import open.dolphin.util.*;

/**
 *
 *
 * This class stores the data received from MML (object) into Postgres database<br>
 *
 * @author Prashanth Kumar, Mirror-i Corp.
 *
 */
public final class PVTPostgres extends SqlDaoBean {

    private static final int NEW_PATIENT             	= 0;
    private static final int PATENT_EXIST_NO_CHANGE		= 1;
    private static final int PATENT_EXIST_CHANGED		= 2;
    private static final int DATABASE_ERROR				= -1;

    private String masterId;

    private PVTPatient pvtPatient;

    private Vector pvtHealth;

    private mirrorI.dolphin.server.PVTClaim pvtClaim;

    private static Logger logger = Logger.getLogger(PVTServer.loggerLocation);

    /**
     * Creates new PVT object.
     */
    public PVTPostgres() {
        super();
    }

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String val) {
        masterId = val;
    }

    public PVTPatient getPVTPatient() {
        return pvtPatient;
    }

    public void setPVTPatient(PVTPatient val) {
        pvtPatient = val;
    }

    public mirrorI.dolphin.server.PVTClaim getPVTClaim() {
        return pvtClaim;
    }

    public void setPVTClaim(mirrorI.dolphin.server.PVTClaim val) {
        pvtClaim = val;
    }

    public Vector getPVTHealthInsurance() {
        return pvtHealth;
    }

    public void addHealthInsurance(mirrorI.dolphin.server.PVTHealthInsurance val) {

        if (pvtHealth == null) {
            pvtHealth = new Vector(3);
        }
        pvtHealth.add(val);
    }

	//To make sql statement ('xxxx',)
    public String addSingleQuoteComa(String s) {
        StringBuffer buf = new StringBuffer();
        buf.append("'");
        buf.append(s);
        buf.append("',");
        return buf.toString();
    }

 	/**
	 *
	 * save(), checks whether the patient is a<br>
	 * 1) new patient or<br>
	 * 2) exist or<br>
	 * 3) exist and whether some of the key values are same<br>
	 * Based on the above result it calls addPatientEntry() / modifyPatientEntry()<br>
	 * to store patient information<br>
	 * <br>
	 * Calls updateHealthInsuraceEntries() to update health insurance information<br>
	 * <br>
	 * Calls savePatientVisitInfo() to add patient visit information<br>
	 * <br>
	 * This method is called by PVTPostgresConnection.addWork()<br>
	 * <br>
	 * Returns true/false based on the database operation result<br>
	 * <br>
	 * If return is false, then it rolls back the transaction<br>
	 *
     */
    public boolean save(Connection con) throws SQLException {

		logger.finer("Method Entry");

		boolean saveReturn = false;

		//Setting connection Auto Commit to false
		con.setAutoCommit(false);

	 	int state = checkPatientEntry(con);
		logger.finer("checkPatientEntry result(0=new patient, 1= modification not required, 2=modification required)= " + state);

        //Add (if new patient) or update patient information
        if (state == NEW_PATIENT) {
			logger.finer("New patient, patient information need to be storeed in  'patient' table");
			saveReturn=addPatientEntry(con);
			if (!saveReturn){
				con.rollback();
				logger.warning("Database error while fetching patient info from 'patient' table");
				return saveReturn;
			}
		}

        else if (state == PATENT_EXIST_CHANGED) {
			logger.finer("Patient Information modified, patient information need to be updated in  'patient' table");
			saveReturn=modifyPatientEntry(con);
			if (!saveReturn){
				con.rollback();
				logger.warning("Database error while modifying patient info in 'patient' table");
				return saveReturn;
			}
		}

        else if (state == PATENT_EXIST_NO_CHANGE) {
			logger.finer("Patient Information exist and no need to modify");
		}

		// Database error
		else if (state == DATABASE_ERROR) {
			logger.warning("Data could not be fetched from 'patient' table");
			return saveReturn;
		}

       //Add (if new patient) or update health insurance information
		saveReturn=updateHealthInsuraceEntries(con);
		if (!saveReturn){
			con.rollback();
			logger.warning("Database error while updating patient health insurance info in 'health_insurance' table");
			return saveReturn;
		}

        // Save patient visit info
		saveReturn=savePatientVisitInfo(con);
		if (saveReturn){
			con.commit();
		}

		//Rollback the transaction
		else{
			con.rollback();
			logger.warning("Database error while inserting into 'patient_visit' table");
			return saveReturn;
		}

		logger.finer("Method Exit");
		return saveReturn;
    }

 	/**
	 *
	 * checkPatientEntry(), checks whether the patient is a<br>
	 * 1) new patient or (Return == NEW_PATIENT(0) or<br>
	 * 2) exist and whether some of the key values are same ( Return == PATENT_EXISIT_NO_CHANGE(1) or<br>
	 * 3) exist ( Return == PATENT_EXISIT_CHANGED(2)<br>
	 * <br>
	 * This method returns NEW_PATIENT or PATENT_EXISIT_NO_CHANGE or
	 * PATENT_EXISIT_CHANGED or DATABASE_ERROR<br>
	 *
     */
    private int checkPatientEntry(Connection con) throws SQLException {

        logger.finer("Method Entry");
        int ret=DATABASE_ERROR;
		Statement st = null;

		// Constructing sql
		StringBuffer buf = new StringBuffer();
		buf.append("select pid, name, kana, birthDay, homePostalCode, homePostalAddress, homePhone, maritalStatus from tbl_patient where pid=");
		buf.append(addSingleQuote(pvtPatient.getPatientId()));
		logger.finer("Patient ID for which previous information is compared: " + pvtPatient.getPatientId());

		String sql = buf.toString();
		logger.finer("SQL Statement: " + sql);

		try {

			st = con.createStatement();

			ResultSet rs = st.executeQuery(sql);

			//If PID exisit
			if (rs.next()) {

				ret=PATENT_EXIST_NO_CHANGE;

				// Check wheather following parameters are changed
				exitOnChange:
				while(true) {
					//Comparing Patient Full name
					if(rs.getString("name") != null && pvtPatient.getFullName("I") != null){
						logger.finer("Comparing Patient Full name, Old name: "  + rs.getString("name")
					                                                                                   + " new name :  " + pvtPatient.getFullName("I"));
						if (!(rs.getString("name").equals(pvtPatient.getFullName("I")))) {
							logger.finer("Comparing Patient Full name: not same");
							ret=PATENT_EXIST_CHANGED;
							break exitOnChange;
						}
					}
					else if(rs.getString("name") == null && pvtPatient.getFullName("I") != null){
							logger.finer("Comparing Patient Full name:  database null");
							ret=PATENT_EXIST_CHANGED;
							break exitOnChange;
					}
					else if(rs.getString("name") != null && pvtPatient.getFullName("I") == null){
							logger.finer("Comparing Patient Full name: object null");
							ret=PATENT_EXIST_CHANGED;
							break exitOnChange;
					}

					//Comparing Patient kana
					//Convert to Katakana
					String newVal = null;
					newVal = pvtPatient.getFullName("P");
					if (newVal != null) {
						newVal = StringTool.hiraganaToKatakana(newVal);
					}
					if(rs.getString("kana") != null && newVal != null){
						logger.finer("Comparing Patient kana, old kana: "  + rs.getString("kana")
					                                                                          + " new kana :  " + newVal);
						if (!(rs.getString("kana").equals(newVal))) {
							logger.finer("Comparing Patient kana: not same");
							ret=PATENT_EXIST_CHANGED;
							break exitOnChange;
						}
					}
					else if(rs.getString("kana") == null && newVal != null){
							logger.finer("Comparing Patient kana:  database null");
							ret=PATENT_EXIST_CHANGED;
							break exitOnChange;
					}
					else if(rs.getString("kana") != null &&newVal == null){
							logger.finer("Comparing Patient kana: object null");
							ret=PATENT_EXIST_CHANGED;
							break exitOnChange;
					}

					//Comparing Patient Birth day
					if(rs.getString("birthDay") != null && pvtPatient.getBirthday() != null){
						logger.finer("Comparing  Birth day, Old Birth day: "  + rs.getString("birthDay")
					                                                                             + " new Birth day :  " + pvtPatient.getBirthday());
						if (!(rs.getString("birthDay").equals(pvtPatient.getBirthday()))) {
							logger.finer("Comparing Patient Birth day: not same");
							ret=PATENT_EXIST_CHANGED;
							break exitOnChange;
						}
					}
					else if(rs.getString("birthDay") == null && pvtPatient.getBirthday() != null){
							logger.finer("Comparing Patient Birth day:  database null");
							ret=PATENT_EXIST_CHANGED;
							break exitOnChange;
					}
					else if(rs.getString("birthDay") != null && pvtPatient.getBirthday() == null){
							logger.finer("Comparing Patient Birth day: object null");
							ret=PATENT_EXIST_CHANGED;
							break exitOnChange;
					}

					//Comparing Patient Zip Code
					if(rs.getString("homePostalCode") != null && pvtPatient.getZipCode() != null){
						logger.finer("Comparing  postal Code, Old postal Code: "  + rs.getString("homePostalCode")
					                                                                                     + " new postal Code :  " + pvtPatient.getZipCode());
						if (!(rs.getString("homePostalCode").equals(pvtPatient.getZipCode()))) {
							logger.finer("Comparing Patient postal Code: not same");
							ret=PATENT_EXIST_CHANGED;
							break exitOnChange;
						}
					}
					else if(rs.getString("homePostalCode") == null && pvtPatient.getZipCode() != null){
							logger.finer("Comparing Patient postal Code:  database null");
							ret=PATENT_EXIST_CHANGED;
							break exitOnChange;
					}
					else if(rs.getString("homePostalCode") != null && pvtPatient.getZipCode() == null){
							logger.finer("Comparing Patient postal Code: object null");
							ret=PATENT_EXIST_CHANGED;
							break exitOnChange;
					}

					//Comapring Patient home Postal Address
					if(rs.getString("homePostalAddress") != null && pvtPatient.getFullAddress() != null){
						logger.finer("Comparing home Postal Address, Old home Postal Address: "  + rs.getString("homePostalAddress")
											                                                                                     + " new home Postal Address :  "
					                                                                                                             + pvtPatient.getFullAddress());
						if (!(rs.getString("homePostalAddress").equals(pvtPatient.getFullAddress()))) {
							logger.finer("Comparing Patient Postal Address: not same");
							ret=PATENT_EXIST_CHANGED;
							break exitOnChange;
						}
					}
					else if(rs.getString("homePostalAddress") == null && pvtPatient.getFullAddress() != null){
							logger.finer("Comparing Patient Postal Address:  database null");
							ret=PATENT_EXIST_CHANGED;
							break exitOnChange;
					}
					else if(rs.getString("homePostalAddress") != null && pvtPatient.getFullAddress() == null){
							logger.finer("Comparing Patient Postal Address: object null");
							ret=PATENT_EXIST_CHANGED;
							break exitOnChange;
					}

					//Comapring Patient Phone number
					String[] phone = pvtPatient.getPhone();
					newVal=null;

					if (phone != null) {
						newVal = phone[0];
					}
					if(rs.getString("homePhone") != null && newVal != null){
						logger.finer("Comparing Patient homePhone, Old homePhone: "  + rs.getString("name")
					                                                                                   + " new homePhone :  " + pvtPatient.getFullName("I"));
						if (!(rs.getString("homePhone").equals(newVal))) {
							logger.finer("Comparing Patient homePhone: not same");
							ret=PATENT_EXIST_CHANGED;
							break exitOnChange;
						}
					}
					else if(rs.getString("homePhone") == null && newVal != null){
							logger.finer("Comparing Patient homePhone:  database null");
							ret=PATENT_EXIST_CHANGED;
							break exitOnChange;
					}
					else if(rs.getString("homePhone") != null && newVal == null){
							logger.finer("Comparing Patient homePhone: object null");
							ret=PATENT_EXIST_CHANGED;
							break exitOnChange;
					}

					//Comapring Patient marital status
					if(rs.getString("maritalStatus") != null && pvtPatient.getMarital() != null){
						logger.finer("Comparing marital status, Old marital status: "  + rs.getString("maritalStatus")
											                                                                 + " new marital status :  "
					                                                                                         + pvtPatient.getMarital());
						if (!(rs.getString("maritalStatus").equals(pvtPatient.getMarital()))) {
							logger.finer("Comparing Patient marital status: not same");
							ret=PATENT_EXIST_CHANGED;
							break exitOnChange;
						}
					}
					else if(rs.getString("maritalStatus") == null && pvtPatient.getMarital() != null){
							logger.finer("Comparing Patient marital status:  database null");
							ret=PATENT_EXIST_CHANGED;
							break exitOnChange;
					}
					else if(rs.getString("maritalStatus") != null && pvtPatient.getMarital() == null){
							logger.finer("Comparing Patient marital status: object null");
							ret=PATENT_EXIST_CHANGED;
							break exitOnChange;
					}

					//Data Not changed, no need to modify
					break;
				}
			}
			//If pid doesn't exist
			else {
				ret=NEW_PATIENT;
			}
			rs.close();
		}
		catch (SQLException e) {
			logger.warning("Exception while getting the patient information from 'patient' table");
			logger.warning( "Exception details:"  + e );
			ret=DATABASE_ERROR;
		}

		closeStatement(st);
       	logger.finer("Method Exit");
       	return ret;
    }

 	/**
	 *
	 * addPatientEntry(), adds new patient information in 'patient' table<br>
	 * <br>
	 * This method returns true on successful insertion, returns false on any database error in inserting<br>
	 *
     */
   	private boolean addPatientEntry(Connection con) throws SQLException {

		boolean addPatientEntryReturn=false;

		logger.finer("Method Entry");

		Statement st = null;
		String val=null;

		// Constructing sql
		StringBuffer buf = new StringBuffer();
		buf.append("insert into tbl_patient (pid, name, kana, roman, gender, birthDay, nationality, maritalStatus, homePostalCode,");
		buf.append(" homePostalAddress, homePhone) values( ");

		// Getting Patient ID and adding for sql statement
		buf.append(addSingleQuoteComa(pvtPatient.getPatientId()));
		logger.finer("Patient ID: "+ pvtPatient.getPatientId());

		// Getting Patient full name and adding for sql statement
		if (pvtPatient.getFullName("I") != null) {
			buf.append(addSingleQuoteComa(pvtPatient.getFullName("I")));
			logger.finer("Patient full name: "+  pvtPatient.getFullName("I"));
		}
		else {
			buf.append("NULL,");
			logger.finer("Patient full name: Not Available");
		}

		// Getting Patient name in kana and adding for sql statement
		val = pvtPatient.getFullName("P");
		if (val != null) {
	 		val = StringTool.hiraganaToKatakana(val);
		}
		if (val != null) {
			buf.append(addSingleQuoteComa(val));
			logger.finer("Patient name in kana: "+  val);
		}
		else{
			buf.append("NULL,");
			logger.finer("Patient name in kana: Not Available");
		}

		// Getting Patient full name in roman and adding for sql statement
		if (pvtPatient.getFullName("A") != null){
			buf.append(addSingleQuoteComa(pvtPatient.getFullName("A")));
			logger.finer("Patient full name in roman: "+  pvtPatient.getFullName("A"));
		}
		else{
			buf.append("NULL,");
			logger.finer("Patient full name in roman: Not Available");
		}

		// Getting Patient sex and adding for sql statement
		if (pvtPatient.getSex() != null) {
			buf.append(addSingleQuoteComa(pvtPatient.getSex()));
			logger.finer("Patient gender: "+  pvtPatient.getSex());
		}
		else {
			buf.append("NULL,");
			logger.finer("Patient gender: Not Available");
		}

		// Getting Patient Birth day and adding for sql statement
		if (pvtPatient.getBirthday() != null) {
			buf.append(addSingleQuoteComa(pvtPatient.getBirthday()));
			logger.finer("Patient Birth day: "+ pvtPatient.getBirthday());
		}
		else {
			buf.append("NULL,");
			logger.finer("Patient Birth day: Not Available");
		}

		// Getting Patient nationality and adding for sql statement
		if (pvtPatient.getNationality() != null) {
			buf.append(addSingleQuoteComa(pvtPatient.getNationality()));
			logger.finer("Patient nationality: "+ pvtPatient.getNationality());
		}
		else {
			buf.append("NULL,");
			logger.finer("Patient nationality: Not Available");
		}

		// Getting Patient Marital status and adding for sql statement
		if (pvtPatient.getMarital() != null) {
			buf.append(addSingleQuoteComa(pvtPatient.getMarital()));
			logger.finer("Patient Marital status: "+ pvtPatient.getMarital());
		}
		else {
			buf.append("NULL,");
			logger.finer("Patient Marital status: Not Available");
		}

		// Getting Patient Zip Code and adding for sql statement
		if (pvtPatient.getZipCode() != null) {
			buf.append(addSingleQuoteComa(pvtPatient.getZipCode()));
			logger.finer("Patient Zip Code: "+ pvtPatient.getZipCode());
		}
		else {
			buf.append("NULL,");
			logger.finer("Patient Zip Code: Not Available");
		}

		// Getting Patient Full Address and adding for sql statement
		if (pvtPatient.getFullAddress() != null) {
			buf.append(addSingleQuoteComa(pvtPatient.getFullAddress()));
			logger.finer("Patient Full Address: "+  pvtPatient.getFullAddress());
		}
		else {
			buf.append("NULL,");
			logger.finer("Patient Full Address: Not Available");
		}

		// Getting Patient phone number and adding for sql statement
		String patientPhone=null;
		String[] phone = pvtPatient.getPhone();
		if (phone != null) {
			patientPhone = phone[0];
		}
		if (patientPhone != null) {
			buf.append(addSingleQuote(patientPhone));
			logger.finer("Patient phone number: "+ patientPhone);
		}
		else {
			buf.append("NULL");
			logger.finer("Patient phone number: Not Available");
		}

		buf.append(")");
		String sql = buf.toString();

		logger.finer("SQL Statement: " + sql);

		try {
			st = con.createStatement();
			st.executeUpdate(sql);
			addPatientEntryReturn = true;
		}

		catch (SQLException e) {
			logger.warning("Exception while inserting patient information into 'patient' table");
			logger.warning( "Exception details:"  + e );
		}
		closeStatement(st);
		logger.finer("Method Exit");
		return addPatientEntryReturn;
	}

 	/**
	 *
	 * modifyPatientEntry(), Update patient informaton in 'patient' table<br>
	 * <br>
	 * This method returns true on successful modification, returns false on any database error in modifying<br>
	 *
     */
    private boolean modifyPatientEntry(Connection con) throws SQLException {

		logger.finer("Method Entry");

		boolean modifyPatientEntryReturn=false;

		Statement st = null;
		String val=null;

		// Constructing sql
		StringBuffer buf = new StringBuffer();

		buf.append("update tbl_patient set name=");
		// Getting Patient full name and adding for sql statement
		if (pvtPatient.getFullName("I") != null) {
			buf.append(addSingleQuoteComa(pvtPatient.getFullName("I")));
			logger.finer("Patient full name: "+  pvtPatient.getFullName("I"));
		}
		else {
			buf.append("NULL,");
			logger.finer("Patient full name: Not Available");
		}

		buf.append(" kana=");
		// Getting Patient name in kana and adding for sql statement
		val = pvtPatient.getFullName("P");
		if (val != null) {
	 		val = StringTool.hiraganaToKatakana(val);
		}
		if (val != null) {
			buf.append(addSingleQuoteComa(val));
			logger.finer("Patient name in kana: "+  val);
		}
		else{
			buf.append("NULL,");
			logger.finer("Patient name in kana: Not Available");
		}

		buf.append(" roman=");
		// Getting Patient full name in roman and adding for sql statement
		if (pvtPatient.getFullName("A") != null){
			buf.append(addSingleQuoteComa(pvtPatient.getFullName("A")));
			logger.finer("Patient full name in roman: "+  pvtPatient.getFullName("A"));
		}
		else{
			buf.append("NULL, ");
			logger.finer("Patient full name in roman: Not Available");
		}

		buf.append(" gender=");
		// Getting Patient sex and adding for sql statement
		if (pvtPatient.getSex() != null) {
			buf.append(addSingleQuoteComa(pvtPatient.getSex()));
			logger.finer("Patient gender: "+  pvtPatient.getSex());
		}
		else {
			buf.append("NULL,");
			logger.finer("Patient gender: Not Available");
		}

		buf.append(" birthDay=");
		// Getting Patient Birth day and adding for sql statement
		if (pvtPatient.getBirthday() != null) {
			buf.append(addSingleQuoteComa(pvtPatient.getBirthday()));
			logger.finer("Patient Birth day: "+ pvtPatient.getBirthday());
		}
		else {
			buf.append("NULL,");
			logger.finer("Patient Birth day: Not Available");
		}

		buf.append(" nationality=");
		// Getting Patient nationality and adding for sql statement
		if (pvtPatient.getNationality() != null) {
			buf.append(addSingleQuoteComa(pvtPatient.getNationality()));
			logger.finer("Patient nationality: "+ pvtPatient.getNationality());
		}
		else {
			buf.append("NULL,");
			logger.finer("Patient nationality: Not Available");
		}

		buf.append(" maritalStatus=");
		// Getting Patient Marital status and adding for sql statement
		if (pvtPatient.getMarital() != null) {
			buf.append(addSingleQuoteComa(pvtPatient.getMarital()));
			logger.finer("Patient Marital status: "+ pvtPatient.getMarital());
		}
		else {
			buf.append("NULL,");
			logger.finer("Patient Marital status: Not Available");
		}

		buf.append(" homePostalCode=");
		// Getting Patient Zip Code and adding for sql statement
		if (pvtPatient.getZipCode() != null) {
			buf.append(addSingleQuoteComa(pvtPatient.getZipCode()));
			logger.finer("Patient Zip Code: "+ pvtPatient.getZipCode());
		}
		else {
			buf.append("NULL,");
			logger.finer("Patient Zip Code: Not Available");
		}

		buf.append(" homePostalAddress=");
		// Getting Patient Full Address and adding for sql statement
		if (pvtPatient.getFullAddress() != null) {
			buf.append(addSingleQuoteComa(pvtPatient.getFullAddress()));
			logger.finer("Patient Full Address: "+  pvtPatient.getFullAddress());
		}
		else {
			buf.append("NULL,");
			logger.finer("Patient Full Address: Not Available");
		}

		buf.append(" homePhone=");
		// Getting Patient phone number and adding for sql statement
		String patientPhone=null;
		String[] phone = pvtPatient.getPhone();
		if (phone != null) {
			patientPhone = phone[0];
		}
		if (patientPhone != null) {
			buf.append(addSingleQuote(patientPhone));
			logger.finer("Patient phone number: "+ patientPhone);
		}
		else {
			buf.append("NULL ");
			logger.finer("Patient phone number: Not Available");
		}

		buf.append("where pid=");
		buf.append(addSingleQuote(pvtPatient.getPatientId()));
		logger.finer("Patient pid: "+ pvtPatient.getPatientId());

		String sql = buf.toString();
		logger.finer("SQL Statement: "+ sql);

		try {
			st = con.createStatement();
			st.executeUpdate(sql);
			modifyPatientEntryReturn=true;
		}

		catch (SQLException e) {
			logger.warning("Exception while updating patient information in 'patient' table");
			logger.warning( "Exception details:"  + e );
		}
		closeStatement(st);
		logger.finer("Method Exit");
		return modifyPatientEntryReturn;
	}

 	/**
	 *
	 * updateHealthInsuraceEntries(), adds patient health insurance informaton in 'health_insurance' table<br>
	 * <br>
	 * This method returns true on successful insertion, returns false on any database error in insertion<br>
	 *
     */
    private boolean updateHealthInsuraceEntries(Connection con) throws SQLException {

		logger.finer("Method Entry");

		boolean updateHealthInsuraceEntriesReturn = false;

		Statement st = null;
		PreparedStatement pst=null;
		byte[] pvtHealthInsuranceBytes=null;
		byte[] pvtHealthInsuranceBytesAfterDecode=null;

		// Constructing sql
		StringBuffer deleteString = new StringBuffer();
		deleteString.append("delete from tbl_healthinsurance where pid=");

		// Getting Patient ID and adding for sql statement
		deleteString.append(addSingleQuote(pvtPatient.getPatientId()));
		logger.finer("Health insurance being updated for Patient ID: " +  pvtPatient.getPatientId());

		String sql = deleteString.toString();

		logger.finer("SQL Statement: " +  sql);

		try {
			//Execute SQL statement for deleting previous record.
			st = con.createStatement();
			st.executeUpdate(sql);

			//Getting PVTHealthInsurance object from pvtHealth and storing in health_insurance table
			if (pvtHealth != null && pvtHealth.size() > 0) {

				int len = pvtHealth.size();
				PVTHealthInsurance pvtHealthInsurance;

				//Constructing SQL statement for inserting health insurance information
				StringBuffer updateString = new StringBuffer();
				updateString.append("insert into tbl_healthinsurance (pid,insuranceBytes) values(");

				// Getting Patient ID and adding for sql statement
				updateString.append(addSingleQuoteComa(pvtPatient.getPatientId()));
				logger.finer("Health insurance being updated for Patient ID: " +  pvtPatient.getPatientId());
				updateString.append(" ?)");

				sql = updateString.toString();
				logger.finer("SQL Statement: " +  sql);

				//Adding sql to prepareStatement
				pst = con.prepareStatement(sql);

            	// Getting XML Byte array of individual PVTHealthInsurance object
            	for (int i = 0; i < len; i++) {
					pvtHealthInsurance = (PVTHealthInsurance)pvtHealth.get(i);

					pvtHealthInsuranceBytes=null;
					pvtHealthInsuranceBytes = getXMLBytes(pvtHealthInsurance);

					//setting  Byte array of PVTHealthInsurance object into prepareStatement
					pst.setBytes(1,pvtHealthInsuranceBytes);
					// prepareStatement execute
					pst.executeUpdate();
					logger.info("Patient Health Insurance details, PID: " + pvtPatient.getPatientId());
					if(pvtHealthInsurance != null) {
						logger.info( "Health Insurance: "+ pvtHealthInsurance.toString());
					}
					else{
						logger.info( "Health Insurance: Not Available");
					}
					updateHealthInsuraceEntriesReturn = true;
				}
			}
		}
		catch (SQLException e) {
			logger.warning("Exception while deleting / updating patient health insurance information in 'health_insurance' table");
			logger.warning( "Exception details:"  + e );
		}
		closeStatement(st);
		logger.finer("Method Exit");
		return updateHealthInsuraceEntriesReturn;
	}

	/**
	 *
	 * getXMLBytes(), converts passed object into XML bytes and returns byte array<br>
	 *
     */
	private byte[] getXMLBytes(Object pvtHealthInsuranceObj) {

		logger.finer("Method Entry");

		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		java.beans.XMLEncoder e = new java.beans.XMLEncoder (new BufferedOutputStream (bo));
		e.writeObject(pvtHealthInsuranceObj);
		e.close();

		logger.finer("Method Exit");
		return bo.toByteArray();
	}

	/**
	 *
	 * savePatientVisitInfo(), inserts new record in 'patient_visit' table as and when patient visit<br>
	 * <br>
	 * This method returns true on successful insertion, returns false on any database error in insertion<br>
	 *
     */
    private boolean savePatientVisitInfo(Connection con) throws SQLException {

		logger.finer("Method Entry");

		boolean savePatientVisitInfoReturn = false;

		Statement st = null;
		String val=null;

		// Constructing sql
		StringBuffer buf = new StringBuffer();
		buf.append("insert into tbl_patientVisit (pid, name, gender, birthDay, registTime,department,status) values( ");

		// Getting Patient ID and adding for sql statement
		buf.append(addSingleQuoteComa(pvtPatient.getPatientId()));
		logger.finer("Patient ID : " + pvtPatient.getPatientId());

		// Getting Patient full name and adding for sql statement
		if (pvtPatient.getFullName("I") != null) {
			buf.append(addSingleQuoteComa(pvtPatient.getFullName("I")));
			logger.finer("Patient full name: "+  pvtPatient.getFullName("I"));
		}
		else {
			buf.append("NULL,");
			logger.finer("Patient full name: Not Available");
		}

		// Getting Patient sex and adding for sql statement
		if (pvtPatient.getSex() != null) {
			buf.append(addSingleQuoteComa(pvtPatient.getSex()));
			logger.finer("Patient gender: "+  pvtPatient.getSex());
		}
		else {
			buf.append("NULL,");
			logger.finer("Patient gender: Not Available");
		}

		// Getting Patient Birth day and adding for sql statement
		if (pvtPatient.getBirthday() != null) {
			buf.append(addSingleQuoteComa(pvtPatient.getBirthday()));
			logger.finer("Patient Birth day: "+ pvtPatient.getBirthday());
		}
		else {
			buf.append("NULL,");
			logger.finer("Patient Birth day: Not Available");
		}

		// Getting Patient register time and adding for sql statement
		if (pvtClaim != null && pvtClaim.getClaimRegistTime() != null) {
			buf.append(addSingleQuoteComa(pvtClaim.getClaimRegistTime()));
			logger.finer("Patient register time: "+ pvtClaim.getClaimRegistTime());

		}
		else {
			buf.append("NULL,");
			logger.finer("Patient register time: Not Available");
		}

		// Getting department time and adding for sql statement
		if (pvtClaim != null && pvtClaim.getClaimDeptName() != null) {

			buf.append(addSingleQuoteComa(pvtClaim.getClaimDeptName()));
			logger.finer("Patient department name: "+ pvtClaim.getClaimDeptName());
		}
		else {
			buf.append("NULL,");
			logger.finer("department name: Not Available");
		}

		// adding default '0' for status field
		buf.append(addSingleQuote("0"));
		logger.finer("Status (default): 0 ");

		buf.append(")");
		String sql = buf.toString();

		logger.finer("SQL Statement: " + sql);

		try {
			st = con.createStatement();
			st.executeUpdate(sql);
			logger.info("Patient visited, PID: " + pvtPatient.getPatientId()
															+ ", Full name: "+  pvtPatient.getFullName("I")
															+ ", gender: " + pvtPatient.getSex()
															+ ", Birth day: "+ pvtPatient.getBirthday());
			if(pvtClaim != null) {
				logger.info(", Register time: "+ pvtClaim.getClaimRegistTime()
											             + ", Department name: " + pvtClaim.getClaimDeptName());
			}
			else {
				logger.info(", Register time: Not Available, Department name: Not Available");
			}

			savePatientVisitInfoReturn = true;
		}
		catch (SQLException e) {
			logger.warning("Exception while inserting patient visit information in 'patient_visit' table");
			logger.warning( "Exception details:"  + e );
		}
		closeStatement(st);
		logger.finer("Method Exit");
		return savePatientVisitInfoReturn;
	}
}