/*
 * LaboTestProcessor.java
 *
 * Created on 2001/11/04, 17:36
 *
 * Modified by Mirror-I on 2003/01/30 for storing parsed data in Postgres DB, logger function added
 *
 * Last updated on 2003/02/28
 *
 * Revised on 2003/03/08 to add 'mmlID' value into labo_module table.
 *
 */

package mirrorI.dolphin.server;

import java.io.*;
import java.util.*;
import javax.xml.parsers.* ;
import org.xml.sax.*;
import java.util.logging.*;
import java.sql.*;

import jp.ac.kumamoto_u.kuh.fc.jsato.math_mml.*;

/**
 *
 * @author  Junzo SATO
 * @copyright   Copyright (c) 2001, Junzo SATO. All rights reserved.
 *
 */
public class LaboTestProcessor {
	//default mml files receiving directory
	private static final String MML_REC_DEFAULT_DIR="/dolphin/mmlLb/incoming/";
	//default extRef receiving directory
	private static final String EXTREF_REC_DEFAULT_DIR="/dolphin/mmlLb/extRefs";

    private Logger logger;
	Connection conPostgres = null;

	//Database UID
	String uidLaboModule=null;
	String uidLaboSpecimen=null;
	String mmlId=null;

	mirrorI.dolphin.server.MMLDirector mmlDirector;
    private Properties laboTestParameter;
    private LaboTestImageHandler laboTestImageHandler;

	mirrorI.dolphin.dao.PostgresConnection postgresConnection;

    /** Creates new LaboTestProcessor */
    public LaboTestProcessor(Properties laboTestParameter, Logger logger) {
		this.laboTestParameter=laboTestParameter;
		this.logger = logger;

		postgresConnection = new mirrorI.dolphin.dao.PostgresConnection(laboTestParameter);
    }

	public boolean processFile(File file, LaboTestImageHandler laboTestImageHandler) throws SQLException{
		boolean processFileReturn=false;
		//Reset mmlID
		mmlId=null;
       	this.laboTestImageHandler = laboTestImageHandler;

       	processFileReturn=readMML(file);

       	if(!processFileReturn){
	   		logger.warning("Error in readMML()");
	   		processFileReturn=false;
		}
		else {
        	processFileReturn=handleMmlTree();

        	if(!processFileReturn){
				logger.warning("Error in handleMmlTree()");
				processFileReturn=false;
			}
		}

        return processFileReturn;
    }

    // MML Parser
    public boolean readMML(File file) {

        String path = file.getPath();

        if (path == null) {
            logger.warning("Couldn't get path for file: " + file);
            return false;
        }
        logger.finer("Processing file: " + file.getPath());

        // it is assumed that this xml file is MML
        try {
            // create parser
            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            // we need to detect namespaces by SAX2.
            // this setting should be called explicitely for jdk1.4 or later
            saxFactory.setNamespaceAware(true);
            SAXParser parser = saxFactory.newSAXParser();
            XMLReader reader = parser.getXMLReader();

            if ( mmlDirector != null ) {
                mmlDirector.releaseDirector();
                mmlDirector = null;
            }
            mmlDirector = new mirrorI.dolphin.server.MMLDirector(logger);

            reader.setContentHandler(mmlDirector);
            // parse xml
            reader.parse(path);

            logger.finer("Parsing done...\n");
        }
        catch (Exception e) {
            logger.warning("Exception while parsing file: " + file.getPath() );
			logger.warning( "Exception details:"  + e );
            return false;
        }
        return true;
    }

    String patientId = null;

    public boolean handleMmlTree() throws SQLException {
		boolean handleMmlTreeReturn=false;

        if (getMml() == null) {
            logger.warning("Mml object is null.");
            return false;
        }
        // check Mml version
        if (getMml().getVersion() != null &&
            getMml().getVersion().equals("2.3") == false ) {
            logger.warning("Unsupported MML version. The Dolphin Project is based on MML ver 2.3");
            return false;
        }
        //------------
        // <MmlHeader>
        //------------
        if (getMml().getMmlHeader() == null) {
            logger.warning("MmlHeader object is null.");
            return false;
        }
        // mmlCiCreatorInfo -------------------------------------------
        // we don't detect creator info in the header

        // masterId -------------------------------------------------
        if (getMml().getMmlHeader().getMasterId() == null) {
            logger.warning("masterId object is null.");
            return false;
        }
        mmlCmId id = getMml().getMmlHeader().getMasterId().getId();
        if (id == null) {
            logger.warning("id object is null.");
            return false;
        }

        String type = null;
        if (id.getMmlCmtype() != null) {
            type = id.getMmlCmtype();
        }
        logger.finer("type: " + type);

        // checkDigitSchema
        String cdschema = null;
        if (id.getMmlCmcheckDigitSchema() != null) {
            cdschema = id.getMmlCmcheckDigitSchema();
        }
        logger.finer("cdschema: " + cdschema);

        // checkDigit
        String cdigit = null;
        if (id.getMmlCmcheckDigit() != null) {
            cdigit = id.getMmlCmcheckDigit();
        }
        logger.finer("cdigit: " + cdigit);

        // tableId
        String tableId = null;
        if (id.getMmlCmtableId() != null) {
            tableId = id.getMmlCmtableId();
        }
        logger.finer("tableId: " + tableId);

        // get the body of mmlCm:Id
        patientId = id.getText();
        if (patientId == null) {
            logger.warning("Missing patient ID.");
            return false;
        }
        patientId = patientId.trim();
        logger.finer("patientId: " + patientId);
        //-----------
        // <MmlBody>
        //-----------
        // get the list of MmlModuleItem
        Vector mmlModuleItem = getModules(getMml());
        if (mmlModuleItem == null) {
            logger.warning("Couldn't get any modules.");
            return false;
        }
        logger.finer("Number of Modules in MML instance: " + String.valueOf( mmlModuleItem.size() ));

        int numTests = countSpecifiedModules(mmlModuleItem, "test");
        logger.finer("Number of LaboTest module: " + String.valueOf(numTests));
        if (numTests == 0) {
            logger.warning("Processing ended because of no TestModule existence.");
            return false;
        }

        // handle each modules
        handleMmlTreeReturn=handleTestModules(mmlModuleItem, patientId, type,cdschema, cdigit, tableId);

        if(!handleMmlTreeReturn){
			//Disconnect from DB and roll back
			if (conPostgres != null) {
				conPostgres.rollback();
				postgresConnection.releasePostgresConnection(conPostgres);
				conPostgres=null;
			}
			return false;
		}
		//Disconnect from DB and commit
        if (conPostgres != null) {
			conPostgres.commit();
			postgresConnection.releasePostgresConnection(conPostgres);
			conPostgres=null;
		}
		return true;
    }

    //--------------------------------------------------------------------------
    // tools for deriving attributes and bodies
    public Mml getMml() {
        // get root object

        if(mmlDirector.getMMLBuilder().getMmlTree().size()>0){
        	MMLObject obj = (MMLObject)mmlDirector.getMMLBuilder().getMmlTree().firstElement();
			if (obj == null) {
				logger.warning("COULDN'T GET MML OBJECT");
				return null;
			}
			else {
				if (obj.getQName().equals("Mml")) {
					return (Mml)obj;
				} else {
					logger.warning("ROOT OF MMLTREE IS NOT Mml");
					return null;
				}
        	}
		}
		else{
            logger.warning("COULDN'T GET MML OBJECT");
            return null;
		}
    }

    public Vector getModules(Mml obj) {
        if (obj == null) return null;

        // get the list of MmlModuleItem
        MmlBody body = obj.getMmlBody();
        if (body == null) {
            logger.warning("MmlBody object is null.");
            return null;
        } else {
            return body.getMmlModuleItem();
        }
    }

    //--------------------------------------------------------------------------
    public int countSpecifiedModules(Vector v, String modulename) {
        int numModules = 0;
        Enumeration e = v.elements();
        while (e.hasMoreElements()) {
            // get MmlModuleItem
            MmlModuleItem item = (MmlModuleItem)e.nextElement();
            // get contentModuleType
            if (item.getDocInfo().getContentModuleType().equals(modulename) ) {
                ++numModules;
            }
        }
        return numModules;
    }

    public boolean handleTestModules(Vector mmlModuleItem, String patientId, String patientType, String patientChkDgtSchema,
                                                     String patientChkDgt, String patientTableId)  throws SQLException {

		boolean handleTestModulesReturn=false;
		String checkString = null;
        Enumeration enum = mmlModuleItem.elements();

        while (enum.hasMoreElements()) {
            //--------------
            // MmlModuleItem
            //--------------
            MmlModuleItem item = (MmlModuleItem)enum.nextElement();
            if (item == null) continue;

            // type ---------------------------------------------

            //----------
            // docInfo
            //----------
            if (item.getDocInfo() == null) {
                logger.warning("docInfo object is null.");
                continue;
            }

            // contentModuleType --------------------------------
            if (item.getDocInfo().getContentModuleType() == null) {
                logger.warning("contentModuleType object is null.");
                continue;
            }
            if ( item.getDocInfo().getContentModuleType().equals("test") == false ) {
                // this module is not a lobo test
                logger.warning("This module is not a labo test.");
                continue;
            }

            // title --------------------------------------------
            String title = null;
            if (item.getDocInfo().getTitle() != null) {
                if (item.getDocInfo().getTitle().getText() != null ) {
                    title = item.getDocInfo().getTitle().getText().trim();
                }
                logger.finer("title: " + title);

                // generationPurpose ----------------------
                /* generationPurpose should be set to "reportTest" in MML0007 */
                if ( item.getDocInfo().getTitle().getGenerationPurpose() != null ) {
                    logger.finer("generationPurpose: " + item.getDocInfo().getTitle().getGenerationPurpose());
                    if ( item.getDocInfo().getTitle().getGenerationPurpose().equals("reportTest") == false ) {
                        logger.warning("generationPurpose doesn't equal to \"reportTest\".");
                    }
                } else {
                    logger.warning(" generationPurpose is not set.");
                }
            }
            // docId
            if (item.getDocInfo().getDocId() == null) {
                logger.warning("docId object is null");
                continue;
            }
            // uid
            String uniqueId = null;
            if (item.getDocInfo().getDocId().getUid() != null &&
                item.getDocInfo().getDocId().getUid().getText() != null) {
                uniqueId = item.getDocInfo().getDocId().getUid().getText().trim();
            }
            logger.finer("uid: " + uniqueId);
            // groupId
            String gID = null;
            Vector gid = item.getDocInfo().getDocId().getGroupId();
            if ( gid != null && gid.size() > 0) {
                logger.finer("this module is a part of the group.");
                // currently only one groupId is derived...
                groupId g = (groupId)gid.firstElement();
                gID = g.getText().trim();
                logger.finer("groupId: " + gID);
                if (g.getGroupClass() != null) {
                    logger.finer("groupClass: " + g.getGroupClass());
                } else {
                    logger.warning("groupClass is unknown");
                }
            } else {
                logger.finer("this module doesn't have any groupId.");
            }
            // confirm date
            String confirmDate = null;
            if (item.getDocInfo().getConfirmDate() != null &&
                item.getDocInfo().getConfirmDate().getText() != null) {
                confirmDate = item.getDocInfo().getConfirmDate().getText().trim();
            }
            logger.finer("confirm date: " + confirmDate);

            /* confirmDate should be the same as reportTime in the module */
            /* laboTest never uses attributes start and end. */
            // start ----------------------------------
            // end ------------------------------------
            /* Keep confirmDate for comparing this to reportTime */
            if (checkString != null) checkString = null;
            checkString = new String(confirmDate);

            // mmlCiCreatorInfo
            mmlCiCreatorInfo ci = item.getDocInfo().getCreatorInfo();
            if (ci == null) {
                logger.warning("creator info object is null.");
                continue;
            }
            // mmlPsiPersonalizedInfo
            mmlPsiPersonalizedInfo pi = ci.getPersonalizedInfo();
            String reporterName = null;
            String facilityName = null;
            String facilityCode = null;
            String departmentName = null;
            String departmentCode = null;
            StringBuffer bufAddresses = new StringBuffer();
            StringBuffer bufEmails = new StringBuffer();
            StringBuffer bufPhones = new StringBuffer();
            String emails[] = null;
            String phones[] = null;
            if (pi != null) {

                // mmlPsipersonName----------------------
                // person name at the labo test center
                if (pi.getPersonName() != null &&
                    pi.getPersonName().getName() != null) {
                    reporterName = toPersonName(
                        (mmlNmName)pi.getPersonName().getName().firstElement()
                    );
                }
                logger.finer("person's name" + reporterName);
                // mmlFcFacility ------------------------
                if (pi.getFacility() != null) {
                    // mmlFcname
                    // name of the labo test center
                    if (pi.getFacility().getName() != null &&
                        pi.getFacility().getName().firstElement() != null) {
                        facilityName = ((mmlFcname)pi.getFacility().getName().firstElement()).getText().trim();
                    }
                    logger.finer("facility name: " + facilityName);
                    // repCode (I|A|P)
                    // tableId
                    // mmlCmId-------------------------------
                    if (pi.getFacility().getId() != null &&
                        pi.getFacility().getId().getText() != null) {
                        facilityCode = pi.getFacility().getId().getText().trim();
                    }
                    logger.finer("facility code: " + facilityCode);
                }
                // mmlDpDepartment-----------------------
                if (pi.getDepartment() != null) {
                    // mmlDpname
                    // name of the department
                    if (pi.getDepartment().getName() != null &&
                        pi.getDepartment().getName().firstElement() != null &&
                        ((mmlDpname)pi.getDepartment().getName().firstElement()).getText() != null) {
                        departmentName = ((mmlDpname)pi.getDepartment().getName().firstElement()).getText().trim();
                    }
                    logger.finer("department name: " + departmentName);
                    // repCode (I|A|P)
                    // tableId
                    // mmlCmId
                    if (pi.getDepartment().getId() != null &&
                        pi.getDepartment().getId().getText() != null) {
                        departmentCode = pi.getDepartment().getId().getText().trim();
                    }
                    logger.finer("department code: " + departmentCode);
                }

                // mmlPsiaddresses-----------------------
                mmlPsiaddresses addressesObj = pi.getAddresses();
                if (addressesObj != null) {
                    Vector addressesVector = addressesObj.getAddress();
                    if (addressesVector != null && addressesVector.size() > 0) {
                        for (int k = 0; k < addressesVector.size(); k++) {
                            String s = toAddress((mmlAdAddress)addressesVector.elementAt(k));
                            logger.finer("address: " + s);
                            if ((s.equals("")) || (s==null)) {
                            	continue;
							}
							if((s !=null) && (s.length() >0)){
								if(k==0){
									bufAddresses.append(s);
								}
								//To add ',' in the begining
								else{
									bufAddresses.append(", " + s);
								}
								logger.finer("Address: " + bufAddresses.toString());
							}
                        }
                    }
                }
                // mmlPsiemailAddresses------------------
                mmlPsiemailAddresses addrsObj = pi.getEmailAddresses();
                if (addrsObj != null) {
                    Vector addrsVector = addrsObj.getEmail();
                    if (addrsVector != null && addrsVector.size() > 0) {
                        for (int k = 0; k < addrsVector.size(); k++) {
                            if ( ((mmlCmemail)addrsVector.elementAt(k)).getText() != null) {
                                if(k==0){
									bufEmails.append(((mmlCmemail)addrsVector.elementAt(k)).getText().trim());
								}
								//To add ',' in the begining
								else{
									bufEmails.append(", " + ((mmlCmemail)addrsVector.elementAt(k)).getText().trim());
								}
								logger.finer("Emails: " + bufEmails.toString());
                            }
                        }
                    }
                }

                // mmlPsiphones--------------------------
                mmlPsiphones phonesObj = pi.getPhones();
                if (phonesObj != null) {
                    Vector phonesVector = phonesObj.getPhone();
                    if (phonesVector != null && phonesVector.size() > 0) {
                   		for (int k = 0; k < phonesVector.size(); k++) {
                            String s = toPhoneNumber((mmlPhPhone)phonesVector.elementAt(k));
                            if ((s.equals("")) || (s==null)) {
								continue;
							}
							if((s !=null) && (s.length() >0)){
								if(k==0){
									bufPhones.append(s);
								}
								//To add ',' in the begining
								else{
									bufPhones.append(", " + s);
								}
								logger.finer("Phones: " + bufPhones.toString());
							}
                        }
                    }
                }
			}
            // mmlCicreatorLicense
           StringBuffer bufCreatorLicenses = new StringBuffer();
            Vector licenseV = ci.getCreatorLicense();
            if (licenseV != null && licenseV.size() > 0) {
                for (int k = 0; k < licenseV.size(); k++) {
                    mmlCicreatorLicense cl = (mmlCicreatorLicense)licenseV.elementAt(k);
                    if (cl != null) {
                        String tid = "";
                        if (cl.getMmlCitableId() != null) {
                            tid = cl.getMmlCitableId();
                        }
                        String ls = "";
                        if (cl.getText() != null) {
                            ls = cl.getText().trim();
                        }
                        if(k==0){
							bufCreatorLicenses.append(tid + "__" + ls);
						}
						//To add ',' in the begining
						else{
							bufCreatorLicenses.append(", "+ tid  + "__" + ls);
						}
                        logger.finer("Creator license: " + bufCreatorLicenses.toString());
                    }
                }
            }
            // <extRefs>
            extRefs refs = item.getDocInfo().getExtRefs();
            Vector exts = null;
            if ( refs != null ) {
                exts = refs.getExtRef();
            }
            // content
            if (item.getContent() == null) {
                logger.warning("content object is null");
                continue;
            }
            mmlLbTestModule test = item.getContent().getTestModule();
            if (test == null) {
                logger.warning("testModule was not found in content.");
                continue;
            }
            // mmlLbTestModule
            // information
            mmlLbinformation info = test.getInformation();
            if (info == null) {
                logger.warning("information object is null.");
                continue;
            }
            String registId = null;
            if (info.getMmlLbregistId() != null) {
                registId = info.getMmlLbregistId();
                logger.finer("registId: " + registId);
            }
            String registTime = null;
            if (info.getMmlLbregistTime() != null) {
                registTime = info.getMmlLbregistTime();
                logger.finer("registTime: " + registTime);
            }
            String reportTime = null;
            if (info.getMmlLbreportTime() != null) {
                reportTime = info.getMmlLbreportTime();
                logger.finer("reportTime: " + reportTime);
                /* check reportTime with confirmDate in docInfo */
                // cut off time from dateTime format
                String reportDate = new String(reportTime);
                if ( reportDate.indexOf("T") > 0 ) {
                    reportDate = reportDate.substring(0, reportDate.indexOf("T"));
                }
                if ( checkString.equals(reportDate) == false ) {
                    logger.warning("reportTime doesn't equal to confirmDate: " + checkString);
                }
            }
            String sampleTime = null;
            if (info.getMmlLbsampleTime() != null) {
                sampleTime = info.getMmlLbsampleTime();
                logger.finer("sampleTime: " + sampleTime);
            }
            // reportStatus
            mmlLbreportStatus repStObj = info.getReportStatus();
            String reportStatusCode = null;
            String reportStatusCodeId = null;
            String reportStatus = null;
            if (repStObj != null) {
                if (repStObj.getMmlLbstatusCode() != null) {
                     reportStatusCode = repStObj.getMmlLbstatusCode();
                     logger.finer("reportStatusCode: " + reportStatusCode);
                }
                if (repStObj.getMmlLbstatusCodeId() != null) {
                     reportStatusCodeId = repStObj.getMmlLbstatusCodeId();
                     logger.finer("reportStatusCodeId: " + reportStatusCodeId);
                }
                if (repStObj != null && repStObj.getText() != null) {
                     reportStatus = repStObj.getText().trim();
                     logger.finer("reportStatus: " + reportStatus);
                }
            }
            // set
            mmlLbset setObj = info.getSet();
            String setCode = null;
            String setCodeId = null;
            String set = null;
            if (setObj != null) {
                if (setObj.getMmlLbsetCode() != null) {
                     setCode = setObj.getMmlLbsetCode();
                     logger.finer("setCode: " + setCode);
                }
                if (setObj.getMmlLbsetCodeId() != null) {
                     setCodeId = setObj.getMmlLbsetCodeId();
                     logger.finer("setCodeId: " + setCodeId);
                }
                if (setObj != null && setObj.getText() != null) {
                     set = setObj.getText().trim();
                     logger.finer("set: " + set);
                }
            }
            // CLIENT facility
            mmlLbfacility clientFacilityObj = info.getFacility();
            String clientFacilityCode = null;
            String clientFacilityCodeId = null;
            String clientFacility = null;
            if (clientFacilityObj != null) {
                if (clientFacilityObj.getMmlLbfacilityCode() != null) {
                     clientFacilityCode = clientFacilityObj.getMmlLbfacilityCode();
                     logger.finer("client facilityCode: " + clientFacilityCode);
                }
                if (clientFacilityObj.getMmlLbfacilityCodeId() != null) {
                     clientFacilityCodeId = clientFacilityObj.getMmlLbfacilityCodeId();
                     logger.finer("client facilityCodeId: " + clientFacilityCodeId);
                }
                if (clientFacilityObj != null && clientFacilityObj.getText() != null) {
                     clientFacility = clientFacilityObj.getText().trim();
                     logger.finer("client facility: " + clientFacility);
                }
            }
            // CLIENT department
            mmlLbdepartment clientDepartmentObj = info.getDepartment();
            String clientDepartmentCode = null;
            String clientDepartmentCodeId = null;
            String clientDepartment = null;
            if (clientDepartmentObj != null) {
                if (clientDepartmentObj.getMmlLbdepCode() != null) {
                     clientDepartmentCode = clientDepartmentObj.getMmlLbdepCode();
                     logger.finer("client departmentCode: " + clientDepartmentCode);
                }
                if (clientDepartmentObj.getMmlLbdepCodeId() != null) {
                     clientDepartmentCodeId = clientDepartmentObj.getMmlLbdepCodeId();
                     logger.finer("client departmentCodeId: " + clientDepartmentCodeId);
                }
                if (clientDepartmentObj != null && clientDepartmentObj.getText() != null) {
                     clientDepartment = clientDepartmentObj.getText().trim();
                     logger.finer("client department: " + clientDepartment);
                }
            }
            // CLIENT ward
            mmlLbward clientWardObj = info.getWard();
            String clientWardCode = null;
            String clientWardCodeId = null;
            String clientWard = null;
            if (clientWardObj != null) {
                if (clientWardObj.getMmlLbwardCode() != null) {
                     clientWardCode = clientWardObj.getMmlLbwardCode();
                     logger.finer("client wardCode: " + clientWardCode);
                }
                if (clientWardObj.getMmlLbwardCodeId() != null) {
                     clientWardCodeId = clientWardObj.getMmlLbwardCodeId();
                     logger.finer("client wardCodeId: " + clientWardCodeId);
                }
                if (clientWardObj != null && clientWardObj.getText() != null) {
                     clientWard = clientWardObj.getText().trim();
                     logger.finer("client ward: " + clientWard);
                }
            }
            // client
            mmlLbclient clientObj = info.getClient();
            String clientCode = null;
            String clientCodeId = null;
            String client = null;
            if (clientObj != null) {
                if (clientObj.getMmlLbclientCode() != null) {
                     clientCode = clientObj.getMmlLbclientCode();
                     logger.finer("client Code: " + clientCode);
                }
                if (clientObj.getMmlLbclientCodeId() != null) {
                     clientCodeId = clientObj.getMmlLbclientCodeId();
                     logger.finer("client CodeId: " + clientCodeId);
                }
                if (clientObj != null && clientObj.getText() != null) {
                     client = clientObj.getText().trim();
                     logger.finer("client: " + client);
                }
            }
            // laboratoryCenter
            mmlLblaboratoryCenter laboratoryCenterObj = info.getLaboratoryCenter();
            String laboratoryCenterCode = null;
            String laboratoryCenterCodeId = null;
            String laboratoryCenter = null;
            if (laboratoryCenterObj != null) {
                if (laboratoryCenterObj.getMmlLbcenterCode() != null) {
                     laboratoryCenterCode = laboratoryCenterObj.getMmlLbcenterCode();
                     logger.finer("laboratoryCenter Code: " + laboratoryCenterCode);
                }
                if (laboratoryCenterObj.getMmlLbcenterCodeId() != null) {
                     laboratoryCenterCodeId = laboratoryCenterObj.getMmlLbcenterCodeId();
                     logger.finer("laboratoryCenter CodeId: " + laboratoryCenterCodeId);
                }
                if (laboratoryCenterObj != null && laboratoryCenterObj.getText() != null) {
                     laboratoryCenter = laboratoryCenterObj.getText().trim();
                     logger.finer("laboratoryCenter: " + laboratoryCenter);
                }
            }
            // technician
            mmlLbtechnician technicianObj = info.getTechnician();
            String technicianCode = null;
            String technicianCodeId = null;
            String technician = null;
            if (technicianObj != null) {
                if (technicianObj.getMmlLbtechCode() != null) {
                     technicianCode = technicianObj.getMmlLbtechCode();
                     logger.finer("technician Code: " + technicianCode);
                }
                if (technicianObj.getMmlLbtechCodeId() != null) {
                     technicianCodeId = technicianObj.getMmlLbtechCodeId();
                     logger.finer("technician CodeId: " + technicianCodeId);
                }
                if (technicianObj != null && technicianObj.getText() != null) {
                     technician = technicianObj.getText().trim();
                     logger.finer("technician: " + technician);
                }
            }
            // repMemo*
            Vector repV = info.getRepMemo();
            StringBuffer bufRepMemoCodeName = new StringBuffer();
			StringBuffer bufRepMemoCode = new StringBuffer();
			StringBuffer bufRepMemoCodeId = new StringBuffer();
			StringBuffer bufRepMemo = new StringBuffer();
            if (repV != null && repV.size() > 0) {
                for (int k = 0; k < repV.size(); k++) {
                    mmlLbrepMemo repM = (mmlLbrepMemo)repV.elementAt(k);
                    if (repM == null) {
                        continue;
                    }
					if (repM.getMmlLbrepCodeName() != null) {
						if(k==0) {
							bufRepMemoCodeName.append((k+1) + "__" + repM.getMmlLbrepCodeName().trim());
						}
						else{
							bufRepMemoCodeName.append(", "+(k+1) + "__" + repM.getMmlLbrepCodeName().trim());
						}
						logger.finer("Report memo code name: " +  bufRepMemoCodeName.toString());
					}
					if (repM.getMmlLbrepCode() != null) {
						if(k==0) {
							bufRepMemoCode.append((k+1) + "__" + repM.getMmlLbrepCode().trim());
						}
						else{
							bufRepMemoCode.append(", "+(k+1) + "__" + repM.getMmlLbrepCode().trim());
						}
						logger.finer("Report memo code: " +  bufRepMemoCode.toString());
					}
					if (repM.getMmlLbrepCodeId() != null) {
						if(k==0) {
							bufRepMemoCodeId.append((k+1) + "__" + repM.getMmlLbrepCodeId().trim());
						}
						else{
							bufRepMemoCodeId.append(", "+(k+1) + "__" + repM.getMmlLbrepCodeId().trim());
						}
						logger.finer("Report memo code ID: " +  bufRepMemoCodeId.toString());
					}
					if (repM.getText() != null) {
						if(k==0) {
							bufRepMemo.append((k+1) + "__" + repM.getText().trim());
						}
						else{
							bufRepMemo.append(", "+(k+1) + "__" + repM.getText().trim());
						}
						logger.finer("Report memo : " +  bufRepMemo.toString());
					}
                }
            }
            // repMemoF
            String repMemoF = null;
            mmlLbrepMemoF repmemoFObj = info.getRepMemoF();
            if (repmemoFObj != null && repmemoFObj.getText() != null) {
                repMemoF = repmemoFObj.getText().trim();
                logger.finer("report free memo: " + repMemoF);
            }
			//Get connection only first time
			if(conPostgres == null) {
				conPostgres = postgresConnection.acquirePostgresConnection();
				if (conPostgres == null) {
					logger.warning("Could not connect to Postgres database");
					return false;
				}
			}
			if(conPostgres!=null){
				//To put in one transaction
				conPostgres.setAutoCommit(false);
				try {
					handleTestModulesReturn =createLaboModule(conPostgres, uniqueId, gID, patientId, patientType, patientChkDgtSchema,
																					  patientChkDgt, patientTableId, registId, sampleTime, registTime, reportTime,
																					  reportStatus, reportStatusCode, reportStatusCodeId, set, setCode,
																					  setCodeId, clientFacility, clientFacilityCode, clientFacilityCodeId,
																					  clientDepartment, clientDepartmentCode, clientDepartmentCodeId,
																					  clientWard, clientWardCode, clientWardCodeId, client, clientCode,
																					  clientCodeId, laboratoryCenter, laboratoryCenterCode, laboratoryCenterCodeId,
																					  technician, technicianCode, technicianCodeId, bufRepMemo,
																					  bufRepMemoCodeName, bufRepMemoCode, bufRepMemoCodeId, repMemoF,
																					  bufAddresses, reporterName, bufEmails, bufPhones,confirmDate,
																					  bufCreatorLicenses,facilityName, facilityCode, departmentName,
																					  departmentCode, title);
					if(!handleTestModulesReturn){
						logger.warning("Error in createLaboModule()");
						return false;
					}
				}
				catch (Exception e) {
					logger.warning("Exception while calling createLaboModule()");
					logger.warning("Exception details:"  + e );
					return false;
				}
			}
			// append entries for extRefs
			if (exts != null && exts.size() > 0) {
				handleTestModulesReturn=appendExtRefs(exts,conPostgres);

				if(!handleTestModulesReturn){
					logger.warning("Error in appendExtRefs()" );
					return false;
				}
			}
            //Labo Test (Specimen and Items)
            Vector laboTests = test.getLaboTest();
            logger.finer("number of laboTests: " + laboTests.size());

            Enumeration etest = laboTests.elements();
            //LaboTest Specimen
            while (etest.hasMoreElements()) {
                mmlLblaboTest lab = (mmlLblaboTest)etest.nextElement();
                if (lab == null) continue;
                // laboTest specimen
                if (lab.getSpecimen() == null) {
                    logger.warning("specimen object is null.");
                    continue;
                }
                // spacimen
                String specimen = null;
                String spCode = null;
                String spCodeId = null;
                if (lab.getSpecimen().getSpecimenName() != null) {
                    if (lab.getSpecimen().getSpecimenName().getText() != null) {
                        specimen = lab.getSpecimen().getSpecimenName().getText().trim();
                    }
                    if ( lab.getSpecimen().getSpecimenName().getMmlLbspCode() != null ) {
                        spCode = lab.getSpecimen().getSpecimenName().getMmlLbspCode();
                    }
                    if ( lab.getSpecimen().getSpecimenName().getMmlLbspCodeId() != null ) {
                        spCodeId = lab.getSpecimen().getSpecimenName().getMmlLbspCodeId();
                    }
                }
                logger.finer("Specimen: " + specimen);
                logger.finer("Specimen code: " + spCode);
                logger.finer("Specimen code id: " + spCodeId);

                // spcMemo
                Vector spV = lab.getSpecimen().getSpcMemo();
				StringBuffer bufSpcMemoCodeName = new StringBuffer();
				StringBuffer bufSpcMemoCode = new StringBuffer();
				StringBuffer bufSpcMemoCodeId = new StringBuffer();
				StringBuffer bufSpcMemo = new StringBuffer();
                if (spV != null && spV.size() > 0) {
                    for (int k = 0; k < spV.size(); k++) {
                        mmlLbspcMemo spcm = (mmlLbspcMemo)spV.elementAt(k);
                        if (spcm == null) {
                            continue;
                        }
                        if (spcm.getMmlLbsmCodeName() != null) {
                            if(k==0) {
								bufSpcMemoCodeName.append((k+1) + "__" + spcm.getMmlLbsmCodeName().trim());
							}
							else{
								bufSpcMemoCodeName.append(", "+(k+1) + "__" + spcm.getMmlLbsmCodeName().trim());
							}
                            logger.finer("specimen memo code name: " +  bufSpcMemoCodeName.toString());
                        }
                        if (spcm.getMmlLbsmCode() != null) {
							if(k==0) {
								bufSpcMemoCode.append((k+1) + "__" + spcm.getMmlLbsmCode().trim());
							}
							else{
								bufSpcMemoCode.append(", "+(k+1) + "__" + spcm.getMmlLbsmCode().trim());
							}
							logger.finer("specimen memo code: " +  bufSpcMemoCode.toString());
                        }
                        if (spcm.getMmlLbsmCodeId() != null) {
							if(k==0) {
								bufSpcMemoCodeId.append((k+1) + "__" + spcm.getMmlLbsmCodeId().trim());
							}
							else{
								bufSpcMemoCodeId.append(", "+(k+1) + "__" + spcm.getMmlLbsmCodeId().trim());
							}
							logger.finer("specimen memo code ID: " +  bufSpcMemoCodeId.toString());
                        }
                        if (spcm.getText() != null) {
							if(k==0) {
								bufSpcMemo.append((k+1) + "__" + spcm.getText().trim());
							}
							else{
								bufSpcMemo.append(", "+(k+1) + "__" + spcm.getText().trim());
							}
							logger.finer("specimen memo : " +  bufSpcMemo.toString());
                        }
                    }
                }

                // spcMemoF -------------------------
                String spcMemoF = null;
                mmlLbspcMemoF memoF = lab.getSpecimen().getSpcMemoF();
                if (memoF != null && memoF.getText() != null) {
                    spcMemoF = memoF.getText().trim();
                    logger.finer("specimen free memo: " + spcMemoF);
                }

                if (conPostgres == null) {
					logger.warning("Could not connect to Postgres database");
					return false;
				}
				else {
					try {
						handleTestModulesReturn=createLaboSpecimen(conPostgres, specimen, spCode,
						                                                                     spCodeId, bufSpcMemo,bufSpcMemoCodeName,
						                                                                     bufSpcMemoCode,bufSpcMemoCodeId,spcMemoF);

						if(!handleTestModulesReturn){
							logger.warning("Error in createLaboSpecimen()" );
							return false;
						}
					}
					catch (Exception e) {
									logger.warning("Exception while calling createLaboSpecimen()");
									logger.warning("Exception details:"  + e);
									return false;
					}
				}

                // LaboTest Items
                handleTestModulesReturn = handleLaboItems(lab.getItem());
                if(!handleTestModulesReturn){
					logger.warning("Error in handleLaboItems()" );
					return false;
				}
            }
        }
        return true;
    }

    public String toPersonName(mmlNmName nm) {
        if (nm == null) {
            return "";
        }

        if (nm.getFullname() != null &&
            nm.getFullname().getText() != null) {
            return (nm.getFullname().getText().trim());
        } else if (nm.getFamily() != null &&
                    nm.getFamily().getText() != null &&
                    nm.getGiven() != null &&
                    nm.getGiven().getText() != null){
            return (nm.getFamily().getText().trim() + " " + nm.getGiven().getText().trim());
        } else {
            return "";
        }
    }

 public String toAddress( mmlAdAddress ad ) {
        if (ad == null) {
            return "";
        }

        // one string is enough to express the address. hey, hey!
        String address = "";

        String repCode = ad.getMmlAdrepCode();
        String addressClass = ad.getMmlAdaddressClass();
        String tableId = ad.getMmlAdtableId();

        mmlAdfull fullObj = ad.getFull();
        if (fullObj != null && fullObj.getText() != null) {
            String full = "";
            full = fullObj.getText().trim();
            address = full;
        } else {
            String prefecture = "";
            String city = "";
            String town = "";
            String homeNumber = "";

            mmlAdprefecture prefectureObj = ad.getPrefecture();
            if (prefectureObj != null && prefectureObj.getText() != null) {
                prefecture = prefectureObj.getText().trim();
            }

            mmlAdcity cityObj = ad.getCity();
            if (cityObj != null && cityObj.getText() != null) {
                city = cityObj.getText().trim();
            }

            mmlAdtown townObj = ad.getTown();
            if (townObj != null && townObj.getText() != null) {
                town = townObj.getText().trim();
            }

            mmlAdhomeNumber homeNumberObj = ad.getHomeNumber();
            if (homeNumberObj != null && homeNumberObj.getText() != null) {
                homeNumber = homeNumberObj.getText().trim();
            }

            address = prefecture + city + town + homeNumber;
        }

        String zip = "";
        mmlAdzip zipObj = ad.getZip();
        if (zipObj != null && zipObj.getText() != null) {
            zip = zipObj.getText().trim();
        }
        if (zip.equals("") == false) {
            address = zip + " " + address;
        }
        //mmlAdcountryCode countryCodeObj = ad.getCountryCode();// ignored:-)

        return address;
    }

    public String toPhoneNumber(mmlPhPhone ph) {
        if (ph == null) {
            return "";
        }

        String telephoneNumber = "";
        // hey, hey, only one string for the phone number is enough:-)

        //String type = ph.getMmlPhtelEquipType(); // ignored (^^;;

        mmlPharea areaObj = ph.getArea();
        mmlPhcity cityObj = ph.getCity();
        //mmlPhcountry countryObj = ph.getCountry(); // ignored (^^;;
        mmlPhextension extensionObj = ph.getExtension();
        mmlPhmemo memoObj = ph.getMemo();
        mmlPhnumber numberObj = ph.getNumber();

        String area = "";
        if (areaObj != null && areaObj.getText() != null) {
            area = areaObj.getText().trim();
        }

        String city = "";
        if (cityObj != null && cityObj.getText() != null) {
            city = cityObj.getText().trim();
        }

        String extension = "";
        if (extensionObj != null && extensionObj.getText() != null) {
            extension = extensionObj.getText().trim();
        }

        String memo = "";
        if (memoObj != null && memoObj.getText() != null) {
            memo = memoObj.getText().trim();
        }

        String number = "";
        if (numberObj != null && numberObj.getText() != null) {
            number = numberObj.getText().trim();
        }

        if (extension.equals("")) {
            telephoneNumber = area + " - " + city + " - " + number;
        } else {
            telephoneNumber = area + " - " + city + " - " + number + " Ext. " + extension;
        }

        if (memo.equals("") == false) {
            telephoneNumber = telephoneNumber + " " + memo;
        }

        return telephoneNumber;
    }


	public boolean appendExtRefs(Vector exts, Connection conPostgres ) throws SQLException {
        boolean appendExtRefsReturn=false;
        boolean moveFileReturn=false;

        for ( int i = 0; i < exts.size(); ++i ) {
            mmlCmextRef ref = (mmlCmextRef)exts.elementAt(i);
            if ( ref == null ) continue;

            String type = null;
            if ( ref.getMmlCmcontentType() != null ) {
                type = ref.getMmlCmcontentType();
                logger.finer("contentType: " + type);
            }

            String role = null;
            if ( ref.getMmlCmmedicalRole() != null ) {
                role = ref.getMmlCmmedicalRole();
                logger.finer("medicalRole: " + role);
            }

            String title = null;
            if ( ref.getMmlCmtitle() != null ) {
                title = ref.getMmlCmtitle();
                logger.finer("title: " + title);
            }

            String href = null;
            if ( ref.getMmlCmhref() != null ) {
                href = ref.getMmlCmhref();
                logger.finer("href: " + href);
            }

            //====================================================
            // move this extRef file to local directory
            String imageStatus = null;
            if(href != null){
				moveFileReturn = moveFile(href);
				if(!moveFileReturn){
					imageStatus="a";
					//Add href into ImageHandler
					laboTestImageHandler.addExtRef(href);
					logger.warning("Error in moving External reference file, added into Imagehandler to move in next interval");
				}
				else{
					imageStatus="y";
				}
			}
            //====================================================
            appendExtRefsReturn = createLaboExtRef(conPostgres, type, role, title, href,imageStatus);
            if(!appendExtRefsReturn){
				return appendExtRefsReturn;
			}

        }
        return appendExtRefsReturn;
    }

    public boolean moveFile(String href) {
		boolean moveFileReturn=false;

		String extRefSrcDir =MML_REC_DEFAULT_DIR;
		String extRefDestDir =EXTREF_REC_DEFAULT_DIR;

		//check for above directories in INI file
		if (laboTestParameter != null && laboTestParameter.size() > 0 &&
													  laboTestParameter.containsKey("SourceDir") &&
													  laboTestParameter.getProperty("SourceDir") !=null ) {
			//Get extRef incoming dir and store it in extRefSrcDir
			extRefSrcDir=(String)laboTestParameter.getProperty("SourceDir");
		}
		else{
			logger.warning("Error in getting 'mml incoming dir' value from INI, taking default as: "+extRefSrcDir);
		}
		if (laboTestParameter != null && laboTestParameter.size() > 0 &&
													  laboTestParameter.containsKey("ExtRefDir") &&
													  laboTestParameter.getProperty("ExtRefDir") !=null ) {
			//Get extRef moving directory and store it in extRefDestDir
			extRefDestDir=(String)laboTestParameter.getProperty("ExtRefDir");
		}
		else{
			logger.warning("Error in getting 'extRef dir' value from INI, taking default as: "+extRefDestDir);
		}

		//Checking file separator at the end of the below paths
		if(!extRefSrcDir.endsWith(File.separator)) {
			extRefSrcDir = extRefSrcDir + File.separator;
		}

		if(!extRefDestDir.endsWith(File.separator)) {
			extRefDestDir = extRefDestDir + File.separator;
		}

		//Checking for file separator in the below paths
		extRefSrcDir = extRefSrcDir.replace('/',File.separatorChar);
		extRefSrcDir = extRefSrcDir.replace('\\', File.separatorChar);
		extRefDestDir = extRefDestDir.replace('/',File.separatorChar);
		extRefDestDir = extRefDestDir.replace('\\', File.separatorChar);

        // it is assummed that href is just a filename
        File src = new File( extRefSrcDir  + href );
        File dst = new File( extRefDestDir + href );

        // move file from srcDir to extRefsDir
        try {
			   if(!src.isFile()){
					logger.warning("extRef file not yet received:  " + href );
					moveFileReturn=false;
				}
           		else {
				   moveFileReturn = src.renameTo(dst);
					if (!moveFileReturn) {
						logger.warning("Couldn't move extRef file: " + href);
						moveFileReturn=false;
					}
					else{
						logger.finer("ExtRef moved successfuly: " + href);
						moveFileReturn=true;
					}
				}
        }
        catch (Exception e) {
            logger.warning("Exception while calling moveFile()");
			logger.warning("Exception details:"  + e);
            moveFileReturn=false;
        }
        return moveFileReturn;
    }

    public boolean  handleLaboItems(Vector items) {
		boolean handleLaboItemsReturn=false;
        Enumeration enum = items.elements();
        logger.finer("number of items in this specimen: " + String.valueOf(items.size()));

        while (enum.hasMoreElements()) {
            mmlLbitem item = (mmlLbitem)enum.nextElement();
            if (item == null) {
                logger.warning("item object is null.");
                continue;
            }

            if (item.getItemName() == null) {
                logger.warning("item name object is null.");
                continue;
            }
            // get item name (ex. GOT) and value
            String itemName = null;
            if (item.getItemName().getText() != null ) {
                itemName = item.getItemName().getText().trim();
            }
            logger.finer("itemName: " + itemName);

            String value = null;
            if (item.getValue() != null &&
                item.getValue().getText() != null) {
                value = item.getValue().getText().trim();
            }
            logger.finer(itemName + ": " + value);

            // itemCode, itemCodeId
            String itemCode = null;
            if (item.getItemName().getMmlLbitCode() != null) {
                itemCode = item.getItemName().getMmlLbitCode();
            }

            String itemCodeId = null;
            if (item.getItemName().getMmlLbitCodeId() != null) {
                itemCodeId = item.getItemName().getMmlLbitCodeId();
            }

            // Acode,Icode,Scode,Mcode,Rcode
            String Acode = null;
            String Icode = null;
            String Scode = null;
            String Mcode = null;
            String Rcode = null;
            if (item.getItemName().getMmlLbAcode() != null) {
                Acode = item.getItemName().getMmlLbAcode();
            }
            if (item.getItemName().getMmlLbIcode() != null) {
                Icode = item.getItemName().getMmlLbIcode();
            }
            if (item.getItemName().getMmlLbScode() != null) {
                Scode = item.getItemName().getMmlLbScode();
            }
            if (item.getItemName().getMmlLbMcode() != null) {
                Mcode = item.getItemName().getMmlLbMcode();
            }
            if (item.getItemName().getMmlLbRcode() != null) {
                Rcode = item.getItemName().getMmlLbRcode();
            }

            // unit
            mmlLbunit unit = item.getUnit();
            String u = null;
            if (unit != null && unit.getText() != null) {
                u = unit.getText().trim();
            }
            logger.finer("Unit:" + u);

            // uCode
            String uCode = null;
            if (unit != null && unit.getMmlLbuCode() != null) {
                uCode = unit.getMmlLbuCode();
                logger.finer("unit code:" + uCode);
            }
            // uCodeId
            String uCodeId = null;
            if (unit != null && unit.getMmlLbuCodeId() != null) {
                uCodeId = unit.getMmlLbuCodeId();
            }
            logger.finer("unit code Id:" + uCodeId);

            // numerical value
            mmlLbnumValue nval = item.getNumValue();

            String low = null;
            String up = null;
            String normal = null;
            String out = null;
            if (nval != null) {
                //logger.finer("---numValue");

                if (nval.getMmlLblow() != null) {
                    low = nval.getMmlLblow();
                    logger.finer("[low:" + low + "] ");
                }

                if (nval.getMmlLbup() != null) {
                    up = nval.getMmlLbup();
                    logger.finer("[up:" + up + "] ");
                }

                if (nval.getMmlLbnormal() != null) {
                    normal = nval.getMmlLbnormal();
                    logger.finer("[normal:" + normal + "] ");
                }

                if (nval.getMmlLbout() != null) {
                    out = nval.getMmlLbout();
                    logger.finer("[out:" + out + "] ");
                }

            }
            else {
                //logger.finer("---no numValue");
            }

            // extRef in  referenceInfo
            // ***** extRef has been inported while the parsing docInfo
            // ***** we should also keep extRef appeared here to know which file is for this item
            StringBuffer bufhRef = new StringBuffer();
            mmlLbreferenceInfo referenceInfoObj = item.getReferenceInfo();
            if (referenceInfoObj != null) {
                Vector exts = referenceInfoObj.getExtRef();
                if (exts != null && exts.size() > 0) {
                    for ( int i = 0; i < exts.size(); i++ ) {
                        mmlCmextRef ref = (mmlCmextRef)exts.elementAt(i);
                        if ( ref == null ) {
							continue;
						}
                        if ( ref.getMmlCmhref() != null ) {
                            if (i == 0){
								bufhRef.append(ref.getMmlCmhref());
							}
							//To add ',' in the begining
							else{
								bufhRef.append(", " + ref.getMmlCmhref());
							}
                            logger.finer("href: " + bufhRef.toString());
                        }
                    }
                }
            }

            // itemMemo -------------------------
            Vector itV = item.getItemMemo();
			StringBuffer bufItemMemoCodeName = new StringBuffer();
			StringBuffer bufItemMemoCode = new StringBuffer();
			StringBuffer bufItemMemoCodeId = new StringBuffer();
			StringBuffer bufItemMemo = new StringBuffer();
            if (itV != null && itV.size() > 0) {
                for (int k = 0; k < itV.size(); k++) {
                    mmlLbitemMemo itM = (mmlLbitemMemo)itV.elementAt(k);
                    if (itM == null) {
						continue;
                  	}
                    if (itM.getMmlLbimCodeName() != null) {
                        if(k==0) {
							bufItemMemoCodeName.append((k+1) + "__" + itM.getMmlLbimCodeName().trim());
						}
						else{
							bufItemMemoCodeName.append(", "+(k+1) + "__" + itM.getMmlLbimCodeName().trim());
						}
                        logger.finer("Item memo code name: " +  itM.getMmlLbimCodeName().trim());
                    }
                    if (itM.getMmlLbimCode() != null) {
                         if(k==0) {
							bufItemMemoCode.append((k+1) + "__" + itM.getMmlLbimCode().trim());
						}
						else{
							bufItemMemoCode.append(", "+(k+1) + "__" + itM.getMmlLbimCode().trim());
						}
                        logger.finer("Item memo code : " + itM.getMmlLbimCode().trim());
                    }
                    if (itM.getMmlLbimCodeId() != null) {
                         if(k==0) {
							bufItemMemoCodeId.append((k+1) + "__" + itM.getMmlLbimCodeId().trim());
						}
						else{
							bufItemMemoCodeId.append(", "+(k+1) + "__" + itM.getMmlLbimCodeId().trim());
						}
                        logger.finer("Item memo code ID : " + itM.getMmlLbimCodeId().trim());
                    }
                    if (itM.getText() != null) {
                         if(k==0) {
							bufItemMemo.append((k+1) + "__" + itM.getText().trim());
						}
						else{
							bufItemMemo.append(", "+(k+1) + "__" + itM.getText().trim());
						}
                        logger.finer("Item memo : " + itM.getText().trim());
                    }
                }
            }

            // itemMemoF -------------------------
            String itemMemoF = null;
            mmlLbitemMemoF memoF = item.getItemMemoF();
            if (memoF != null && memoF.getText() != null) {
                itemMemoF = memoF.getText().trim();
                logger.finer("item free memo: " + itemMemoF);
            }

			if (conPostgres == null) {
            	logger.warning("Could not connect to Postgres database");
            	return false;
			}
			else {
				try {
					handleLaboItemsReturn=createLaboItem(conPostgres, itemName, itemCode, itemCodeId,
                															 Acode, Icode, Scode, Mcode, Rcode, value, up, low, normal, out,
                															 u, uCode, uCodeId, bufItemMemo, bufItemMemoCodeName, bufItemMemoCode,
                															 bufItemMemoCodeId, itemMemoF, bufhRef);
					if(!handleLaboItemsReturn){
						logger.warning("Error in createLaboItem()" );
						return false;
					}
				}
 				catch (Exception e) {
					logger.warning("Exception while calling createLaboItem method");
					logger.warning("Exception details:"  + e );
					return false;
				}
			}

		}
		return true;
    }


    public String generateUUID() {
        String osName = System.getProperty("os.name");
        if ( osName.equals("Windows 2000") ) {
            // it is assummed that the platform is WIndows2000
            return generateUUIDWindows();
        } else if ( osName.equals("Linux") ) {
            return generateUUIDLinux();
        } else if ( osName.equals("Mac OS X") ) {
            return generateUUIDMacOSX();
        } else {
            return "";
        }
    }

    //--------------------------------------------------------------------------
    public String generateUUIDWindows() {
        String cmd = "uuidgen";

        try {
            // invoke command and get result...
            // output strings are stored in the vector
            Runtime r = Runtime.getRuntime();
            Process p = r.exec(cmd);
            BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream())
            );
            String line = null;
            Vector v = new Vector();
            while( (line = br.readLine()) != null ) {
                v.add(line);
                //logger.finer( line );
            }
            p.waitFor();

             // get result
            if (v.size() > 0) {
                String s = (String)v.firstElement();
                s=s.trim();
                return s;
            } else {
                return "";
            }
        }
        catch( IOException ex ) {
            logger.warning("IO Exception while calling generateUUIDWindows(), command executed:" + cmd );
			logger.warning("Exception details:"  + ex );
            return "";
        }
        catch ( Exception e ) {
            logger.warning("IO Exception while calling generateUUIDWindows()");
			logger.warning("Exception details:"  + e );
            return "";
        }
    }

    public String generateUUIDLinux() {
        String cmd = "uuidgen -t";

        try {
            // invoke command and get result...
            // output strings are stored in the vector
            Runtime r = Runtime.getRuntime();
            Process p = r.exec(cmd);
            BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream())
            );
            String line = null;
            Vector v = new Vector();
            while( (line = br.readLine()) != null ) {
                v.add(line);
                //logger.finer( line );
            }
            p.waitFor();

             // get result
            if (v.size() >= 0) {
                String s = (String)v.firstElement();
                s=s.trim();
                return s;
            } else {
                return "";
            }
        }
        catch( IOException ex ) {
            logger.warning("IO Exception while calling generateUUIDLinux(), command executed:" + cmd );
			logger.warning("Exception details:"  + ex );
            return "";
        }
        catch ( Exception e ) {
            logger.warning("IO Exception while calling generateUUIDLinux()");
			logger.warning("Exception details:"  + e );
            return "";
        }
    }

    public String generateUUIDMacOSX() {
        String cmd = "uuidgen";

        try {
            // invoke command and get result...
            // output strings are stored in the vector
            Runtime r = Runtime.getRuntime();
            Process p = r.exec(cmd);
            BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream())
            );
            String line = null;
            Vector v = new Vector();
            while( (line = br.readLine()) != null ) {
                v.add(line);
                //logger.finer( line );
            }
            p.waitFor();

             // get result
            if (v.size() >= 0) {
                String s = (String)v.firstElement();
                s=s.trim();
                return s;
            } else {
                return "";
            }
        }
        catch( IOException ex ) {
            logger.warning("IO Exception while calling generateUUIDMacOSX(), command executed:" + cmd );
			logger.warning("Exception details:"  + ex );
            return "";
        }
        catch ( Exception e ) {
            logger.warning("IO Exception while calling generateUUIDMacOSX()");
			logger.warning("Exception details:"  + e );
            return "";
        }
    }

	/**
	 *
	 * createLaboModule(), Creates Labo Module<br>
	 *
	 * <br>
	 * This method is called from handleTestModules()<br>
	 *
 	 */
	public boolean createLaboModule(Connection conPostgres, String mmlUID, String mmlGroupId, String patientId, String patientType,
	                                                String patientChkDgtSchema, String patientChkDgt, String patientTableId, String registId,
	                                                String sampleTime, String registTime, String reportTime, String reportStatus,
	                                                String reportStatusCode, String reportStatusCodeId, String set, String setCode, String setCodeId,
	                                                String clientFacility, String clientFacilityCode, String clientFacilityCodeId, String clientDepartment,
	                                                String clientDepartmentCode, String clientDepartmentCodeId, String clientWard, String clientWardCode,
	                                                String clientWardCodeId, String client, String clientCode,String clientCodeId, String laboratoryCenter,
	                                                String laboratoryCenterCode, String laboratoryCenterCodeId, String technician, String technicianCode,
	                                                String technicianCodeId,  StringBuffer repMemo, StringBuffer repMemoCodeName,
	                                                StringBuffer repMemoCode, StringBuffer repMemoCodeId, String repFreeMemo, StringBuffer addresses,
	                                                String reporterName, StringBuffer eMails, StringBuffer phones, String confirmDate,
	                                                StringBuffer creatorLicenses, String testCenterName, String testCenterId, String departmentName,
	                                                String departmentCode, String title )throws SQLException{
		logger.finer("Method Entry");
		boolean createLaboModuleReturn = false;
		Statement st = null;
		StringBuffer buf = null;
		String sql=null;

		String uidLaboModuleLocal=generateUUID();
		uidLaboModuleLocal = uidLaboModuleLocal.replaceAll("-","");
		uidLaboModuleLocal = uidLaboModuleLocal.replaceAll(":","");
        uidLaboModuleLocal = uidLaboModuleLocal.replaceAll("'","''");

        //Generate mmlId (Which is unique for this mml file)
        if (mmlId == null){
        	mmlId=generateUUID();
        	mmlId = mmlId.replaceAll("-","");
			mmlId = mmlId.replaceAll(":","");
        	mmlId = mmlId.replaceAll("'","''");
        	//Concatenate PID with this mmlID
        	mmlId = mmlId + "__"+patientId;
		}

		//Check any of the following has any null value (DB Constraints)
		//Removing any items from below check needs null checking while making below sql statement.
		if( (conPostgres == null) || (mmlUID==null) || (patientId == null) || (patientType ==null) || (uidLaboModuleLocal==null)  ||
			(patientTableId==null) || (registId==null) || (registTime==null) || (reportTime==null) || (reportStatus==null) || (reportStatusCode==null) ||
			(reportStatusCodeId==null) || (clientFacility==null) || (clientFacilityCode==null) || (clientFacilityCodeId==null) || (laboratoryCenter==null) ||
			(laboratoryCenterCode==null) || (laboratoryCenterCodeId==null) || (reporterName==null) || (confirmDate==null) || (creatorLicenses==null) ||
			(title==null) || (mmlId==null) ){
			createLaboModuleReturn = false;
			logger.warning("Some of the key fields value to be inserted into Tbl_Labo_Module is null");
		}
		else{
			buf = new StringBuffer();
			buf.append("insert into Tbl_Labo_Module (UID,MmlUID,MmlGroupId,PatientId,PatientType,PatientCheckDigitSchema,PatientCheckDigit,");
			buf.append("PatientTableID, RegistId, SampleTime, RegistTime, ReportTime, Reportstatus, ReportStatusCode, ReportStatusCodeId,");
			buf.append("Set,SetCode,SetCodeId,ClientFacility,ClientFacilityCode,ClientFacilityCodeId,ClientDepartment,ClientDepCode,ClientDepCodeId,");
			buf.append("Ward,WardCode,WardCodeId,Client,ClientCode,ClientCodeId,LaboratoryCenter,LaboratoryCenterCode,LaboratoryCenterCodeId,");
			buf.append("Technician,TechnicianCode,TechnicianCodeId,RepMemo,RepMemoCodeName,RepMemoCode,RepMemoCodeId,RepFreeMemo,");
			buf.append("Address,TestReporterName,Email,Phone,MmlConfirmDate,CreatorLicense,TestCenterName,TestCenterId,TestDeptName,");
			buf.append("TestDeptId,ModuleTitle,mmlId) values (");

			uidLaboModule = sampleTime +"__"+ testCenterId + "__"+ uidLaboModuleLocal;
			buf.append(postgresConnection.addSingleQuoteComa( uidLaboModule));
			//MML UID
			buf.append(postgresConnection.addSingleQuoteComa(mmlUID));
			//MML Group ID
			if(mmlGroupId != null)
				buf.append(postgresConnection.addSingleQuoteComa(mmlGroupId));
			else
				buf.append("NULL,");
			//patient Id
			buf.append(postgresConnection.addSingleQuoteComa(patientType + "__" + patientId));
			//patient Type
			buf.append(postgresConnection.addSingleQuoteComa(patientType));
			//patient Chk Dgt Schema
			if(patientChkDgtSchema != null)
				buf.append(postgresConnection.addSingleQuoteComa(patientChkDgtSchema));
			else
				buf.append("NULL,");
 			//patient Chk Dgt
			if(patientChkDgt != null)
				buf.append(postgresConnection.addSingleQuoteComa(patientChkDgt));
			else
				buf.append("NULL,");
 			//patient Table Id
			buf.append(postgresConnection.addSingleQuoteComa(patientTableId));
			//regist Id
			buf.append(postgresConnection.addSingleQuoteComa(registId));
			//sample Time
			if(sampleTime != null)
				buf.append(postgresConnection.addSingleQuoteComa(sampleTime));
			else
				buf.append("NULL,");
			//regist Time
			buf.append(postgresConnection.addSingleQuoteComa(registTime));
			//report Time
			buf.append(postgresConnection.addSingleQuoteComa(reportTime));
			//report Status
			buf.append(postgresConnection.addSingleQuoteComa(reportStatus));
			//report Status Code
			buf.append(postgresConnection.addSingleQuoteComa(reportStatusCode));
			//report Status code ID
			buf.append(postgresConnection.addSingleQuoteComa(reportStatusCodeId));
			//set
			if(set != null)
				buf.append(postgresConnection.addSingleQuoteComa(set));
			else
				buf.append("NULL,");
			//set Code
			if(setCode != null)
				buf.append(postgresConnection.addSingleQuoteComa(setCode));
			else
				buf.append("NULL,");
			//setCode Id
			if(setCodeId != null)
				buf.append(postgresConnection.addSingleQuoteComa(setCodeId));
			else
				buf.append("NULL,");
			//client Facility
			buf.append(postgresConnection.addSingleQuoteComa(clientFacility));
			//client Facility Code
			buf.append(postgresConnection.addSingleQuoteComa(clientFacilityCode));
			//client Facility Code ID
			buf.append(postgresConnection.addSingleQuoteComa(clientFacilityCodeId));
			//client Department
			if(clientDepartment != null)
				buf.append(postgresConnection.addSingleQuoteComa(clientDepartment));
			else
				buf.append("NULL,");
			//client Department Code
			if(clientDepartmentCode != null)
				buf.append(postgresConnection.addSingleQuoteComa(clientDepartmentCode));
			else
				buf.append("NULL,");
			//client Department Code Id
			if(clientDepartmentCodeId != null)
				buf.append(postgresConnection.addSingleQuoteComa(clientDepartmentCodeId));
			else
				buf.append("NULL,");
			//client Ward
			if(clientWard != null)
				buf.append(postgresConnection.addSingleQuoteComa(clientWard));
			else
				buf.append("NULL,");
			//client Ward Code
			if(clientWardCode != null)
				buf.append(postgresConnection.addSingleQuoteComa(clientWardCode));
			else
				buf.append("NULL,");
			//client Ward CodeId
			if(clientWardCodeId != null)
				buf.append(postgresConnection.addSingleQuoteComa(clientWardCodeId));
			else
				buf.append("NULL,");
			//client
			if(client != null)
				buf.append(postgresConnection.addSingleQuoteComa(client));
			else
				buf.append("NULL,");
			//client Code
			if(clientCode != null)
				buf.append(postgresConnection.addSingleQuoteComa(clientCode));
			else
				buf.append("NULL,");
			//client Code
			if(clientCodeId != null)
				buf.append(postgresConnection.addSingleQuoteComa(clientCodeId));
			else
				buf.append("NULL,");
			//laboratory Center
			buf.append(postgresConnection.addSingleQuoteComa(laboratoryCenter));
			//laboratory Center Code
			buf.append(postgresConnection.addSingleQuoteComa(laboratoryCenterCode));
			//laboratory Center Code
			buf.append(postgresConnection.addSingleQuoteComa(laboratoryCenterCodeId));
			//technician
			if(technician != null)
				buf.append(postgresConnection.addSingleQuoteComa(technician));
			else
				buf.append("NULL,");
			//technician Code
			if(technicianCode != null)
				buf.append(postgresConnection.addSingleQuoteComa(technicianCode));
			else
				buf.append("NULL,");
			//technician Code Id
			if(technicianCodeId != null)
				buf.append(postgresConnection.addSingleQuoteComa(technicianCodeId));
			else
				buf.append("NULL,");
			//Report Memo
			if((repMemo != null) && (repMemo.length() > 0))
				buf.append(postgresConnection.addSingleQuoteComa(repMemo.toString()));
			else
				buf.append("NULL,");
			//Report Memo Code name
			if((repMemoCodeName != null) && (repMemoCodeName.length() > 0))
					buf.append(postgresConnection.addSingleQuoteComa(repMemoCodeName.toString()));
			else
				buf.append("NULL,");
			//Report Memo Code
			if((repMemoCode != null) && (repMemoCode.length() > 0))
				buf.append(postgresConnection.addSingleQuoteComa(repMemoCode.toString()));
			else
				buf.append("NULL,");
			//Report Memo Code ID
			if((repMemoCodeId != null) && (repMemoCodeId.length() > 0))
				buf.append(postgresConnection.addSingleQuoteComa(repMemoCodeId.toString()));
			else
				buf.append("NULL,");
			//technician Code Id
			if(repFreeMemo != null)
				buf.append(postgresConnection.addSingleQuoteComa(repFreeMemo));
			else
				buf.append("NULL,");
			//addresses
			if((addresses != null) && (addresses.length() > 0))
				buf.append(postgresConnection.addSingleQuoteComa(addresses.toString()));
			else
				buf.append("NULL,");
			//reporter Name
			buf.append(postgresConnection.addSingleQuoteComa(reporterName));
			//e-Mails
			if((eMails != null) && (eMails.length() > 0))
				buf.append(postgresConnection.addSingleQuoteComa(eMails.toString()));
			else
				buf.append("NULL,");
			//Phones
			if((phones != null) && (phones.length() > 0))
				buf.append(postgresConnection.addSingleQuoteComa(phones.toString()));
			else
				buf.append("NULL,");
			//confirm Date
			buf.append(postgresConnection.addSingleQuoteComa(confirmDate));
			//Creator Licenses
			buf.append(postgresConnection.addSingleQuoteComa(creatorLicenses.toString()));
			//test Center Name
			if(testCenterName != null)
				buf.append(postgresConnection.addSingleQuoteComa(testCenterName));
			else
				buf.append("NULL,");
			//test Center Id
			if(testCenterId != null)
				buf.append(postgresConnection.addSingleQuoteComa(testCenterId));
			else
				buf.append("NULL,");
			//department Name
			if(departmentName != null)
				buf.append(postgresConnection.addSingleQuoteComa(departmentName));
			else
				buf.append("NULL,");
			//Department Code
			if(departmentCode != null)
				buf.append(postgresConnection.addSingleQuoteComa(departmentCode));
			else
				buf.append("NULL,");
			//Title
			buf.append(postgresConnection.addSingleQuoteComa(title));
			//MML ID
			buf.append(postgresConnection.addSingleQuote(mmlId));
			buf.append(")");

			//Converting buf to string
			sql = buf.toString();
			//check for Japanese Minus char
			sql = mirrorI.dolphin.util.CharConversion.convert(sql);
			logger.finer("SQL Statement: " + sql);
			//Logging message
			logger.info("Received Labo test result of patient: " + patientId);
			try {
				st = conPostgres.createStatement();
				st.executeUpdate(sql);
				createLaboModuleReturn = true;
			}
			catch (SQLException sqle) {
				logger.warning("SQL Exception while inserting labo module information into 'Tbl_Labo_Module' table");
				logger.warning( "Exception details:"  + sqle );
				createLaboModuleReturn = false;
			}
			catch (Exception e) {
				logger.warning("Exception while inserting labo module information into 'Tbl_Labo_Module' table");
				logger.warning( "Exception details:"  + e );
				createLaboModuleReturn = false;
			}
			postgresConnection.closeStatement(st);
		}
		logger.finer("Method Exit");
		return createLaboModuleReturn;
	}

	/**
	 *
	 * createLaboExtRef(), Creates Labo test external reference database<br>
	 *
	 * <br>
	 * This method is called from appendExtRefs()<br>
	 *
 	 */
	public boolean createLaboExtRef(Connection conPostgres, String contentType,String medicalRole, String title, String href,
												   String imageStatus) throws SQLException{
		logger.finer("Method Entry");
		boolean createLaboExtRefReturn = false;
		Statement st = null;
		StringBuffer buf = null;
		String sql=null;
		String uidLaboExtRef=null;

		//Get Unique ID for this LaboExtRef
		uidLaboExtRef=generateUUID();
		uidLaboExtRef = uidLaboExtRef.replaceAll("-","");
		uidLaboExtRef = uidLaboExtRef.replaceAll(":","");
        uidLaboExtRef = uidLaboExtRef.replaceAll("'","''");

		//Check any of the following has any null value (DB Constraints)
		//Removing any items from below check needs null checking while making below sql statement.
		if( (href==null) || (uidLaboExtRef==null) || (uidLaboModule==null) || (conPostgres == null) ){
			createLaboExtRefReturn = false;
			logger.warning("Some of the key fields value to be inserted into Tbl_Labo_Ext_Ref is null");
		}
		else{
			buf = new StringBuffer();
			buf.append("insert into Tbl_Labo_Ext_Ref (UID, LaboModuleUID, ContentType, MedicalRole, Title, Href, ImageStatus) values (");
			buf.append(postgresConnection.addSingleQuoteComa("extRef__"+ uidLaboExtRef));
			buf.append(postgresConnection.addSingleQuoteComa(uidLaboModule));
			//Content Type
			buf.append(postgresConnection.addSingleQuoteComa(contentType));
			//Medical Role
			if(medicalRole != null)
				buf.append(postgresConnection.addSingleQuoteComa(medicalRole));
			else
				buf.append("NULL,");
			//Title
			if(title != null)
				buf.append(postgresConnection.addSingleQuoteComa(title));
			else
				buf.append("NULL,");
			//Href
			buf.append(postgresConnection.addSingleQuoteComa(href));
			//Image Status
			if(imageStatus != null)
				buf.append(postgresConnection.addSingleQuote(imageStatus));
			else
				buf.append("NULL");

			buf.append(")");

			//Converting buf to string
			sql = buf.toString();
			//check for Japanese Minus char
			sql = mirrorI.dolphin.util.CharConversion.convert(sql);
			logger.finer("SQL Statement: " + sql);
			try {
				st = conPostgres.createStatement();
				st.executeUpdate(sql);
				createLaboExtRefReturn = true;
			}
			catch (SQLException sqle) {
				logger.warning("SQL Exception while inserting extRef information into 'Tbl_Labo_Ext_Ref' table");
				logger.warning( "Exception details:"  + sqle );
				createLaboExtRefReturn = false;
			}
			catch (Exception e) {
				logger.warning("Exception while inserting extRef information into 'Tbl_Labo_Ext_Ref' table");
				logger.warning( "Exception details:"  + e );
				createLaboExtRefReturn = false;
			}
			postgresConnection.closeStatement(st);
		}
		logger.finer("Method Exit");
		return createLaboExtRefReturn;
	}

	/**
	 *
	 * createLaboSpecimen(), Creates Labo test specimen database<br>
	 *
	 * <br>
	 * This method is called from handleTestModules()<br>
	 *
 	 */
	public boolean createLaboSpecimen(Connection conPostgres, String specimen, String spCode, String spCodeId,
													   StringBuffer spcMemo,StringBuffer spcMemoCodeName,StringBuffer spcMemoCode,
													   StringBuffer spcMemoCodeId, String spcFreeMemo ) throws SQLException{
		logger.finer("Method Entry");
		boolean createLaboSpecReturn = false;
		Statement st = null;
		StringBuffer buf = null;
		String sql=null;
		//Get Unique ID for this specimen
		String uidLaboSpecimenLocal=generateUUID();
		uidLaboSpecimenLocal = uidLaboSpecimenLocal.replaceAll("-","");
        uidLaboSpecimenLocal = uidLaboSpecimenLocal.replaceAll(":","");
        uidLaboSpecimenLocal = uidLaboSpecimenLocal.replaceAll("'","''");

		//Check any of the following has any null value (DB Constraints)
		//Removing any items from below check needs null checking while making below sql statement.
		if( (specimen==null) || (spCode==null) || (spCodeId==null) || (uidLaboSpecimenLocal==null) || (uidLaboModule==null) || (conPostgres == null) ){
			createLaboSpecReturn = false;
			logger.warning("Some of the key fields value to be inserted into Tbl_Labo_Specimen is null");
		}
		else{
			buf = new StringBuffer();
			buf.append("insert into Tbl_Labo_Specimen (UID, LaboModuleUID, SpecimenName, SpecimenCode, SpecimenCodeId, SpcMemo,");
			buf.append("SpcMemoCodeName, SpcMemoCode, SpcMemoCodeId, SpecimenFreeMemo) values ( ");

			uidLaboSpecimen = specimen + "__"+ uidLaboSpecimenLocal;
			buf.append(postgresConnection.addSingleQuoteComa(uidLaboSpecimen));
			buf.append(postgresConnection.addSingleQuoteComa(uidLaboModule));
			buf.append(postgresConnection.addSingleQuoteComa(specimen));
			buf.append(postgresConnection.addSingleQuoteComa(spCode));
			buf.append(postgresConnection.addSingleQuoteComa(spCodeId));
			//Specimen Memo
			if((spcMemo != null) && (spcMemo.length() > 0))
				buf.append(postgresConnection.addSingleQuoteComa(spcMemo.toString()));
			else
				buf.append("NULL,");
			//Specimen Memo Code Name
			if((spcMemoCodeName != null) && (spcMemoCodeName.length() > 0))
				buf.append(postgresConnection.addSingleQuoteComa(spcMemoCodeName.toString()));
			else
				buf.append("NULL,");
			//Specimen Memo Code
			if((spcMemoCode != null) && (spcMemoCode.length() > 0))
				buf.append(postgresConnection.addSingleQuoteComa(spcMemoCode.toString()));
			else
				buf.append("NULL,");
			//Specimen Memo Code ID
			if((spcMemoCodeId != null) && (spcMemoCodeId.length() > 0))
				buf.append(postgresConnection.addSingleQuoteComa(spcMemoCodeId.toString()));
			else
				buf.append("NULL,");
			//Specimen Free memo
			if(spcFreeMemo != null)
				buf.append(postgresConnection.addSingleQuote(spcFreeMemo));
			else
				buf.append("NULL");

			buf.append(")");

			//Converting buf to string
			sql = buf.toString();
			//check for Japanese Minus char
			sql = mirrorI.dolphin.util.CharConversion.convert(sql);
			logger.finer("SQL Statement: " + sql);
			try {
				st = conPostgres.createStatement();
				st.executeUpdate(sql);
				createLaboSpecReturn = true;
			}
			catch (SQLException sqle) {
				logger.warning("SQL Exception while inserting specimen information into 'Tbl_Labo_Specimen' table");
				logger.warning( "Exception details:"  + sqle );
				createLaboSpecReturn = false;
			}
			catch (Exception e) {
				logger.warning("Exception while inserting specimen information into 'Tbl_Labo_Specimen' table");
				logger.warning( "Exception details:"  + e );
				createLaboSpecReturn = false;
			}
			postgresConnection.closeStatement(st);
		}
		logger.finer("Method Exit");
		return createLaboSpecReturn;
	}

	/**
	 *
	 * createLaboItem(), Creates Labo test items database<br>
	 *
	 * <br>
	 * This method is called from handleLaboItems()<br>
	 *
 	 */
    public boolean createLaboItem( Connection conPostgres, String itemName, String itemCode,
    											 String itemCodeId, String Acode, String Icode, String Scode, String Mcode, String Rcode,
    											 String value, String up, String low, String normol, String out,
    											 String unit, String uCode, String uCodeId, StringBuffer itemMemo, StringBuffer itemMemoCodeName,
    											 StringBuffer itemMemoCode, StringBuffer itemMemoCodeId, String itemFreeMemo,
    											 StringBuffer extRef) throws SQLException {

		logger.finer("Method Entry");
		boolean createLaboItemReturn = false;
		Statement st = null;
		StringBuffer buf = null;
		String sql=null;
		String uidLaboItem=null;

		//Get Unique ID for this Labo Item
		uidLaboItem=generateUUID();
		uidLaboItem = uidLaboItem.replaceAll("-","");
        uidLaboItem = uidLaboItem.replaceAll(":","");
        uidLaboItem = uidLaboItem.replaceAll("'","''");

		//Check any of the following has any null value (DB Constraints)
		//Removing any items from below check needs null checking while making below sql statement.
		if( (itemName==null) || (itemCode == null) || (itemCodeId == null) || (value == null) || (uidLaboItem==null) || (uidLaboSpecimen==null) ||
		    (conPostgres == null) ){
			createLaboItemReturn = false;
			logger.warning("Some of the key fields value to be inserted into Tbl_Labo_Item is null");
		}
		else{
			buf = new StringBuffer();
			buf.append("insert into Tbl_Labo_Item (UID, LaboSpecimenUID, ItemName, ItemCode, ItemCodeId, Acode, Icode, Scode, Mcode, ");
			buf.append("Rcode, Value, Up, Low, Normal, NOut, Unit, UnitCode, UnitCodeId, ItemMemo, ItemMemoCodeName, ");
			buf.append("ItemMemoCode, ItemMemoCodeID, ItemFreeMemo, ExtRef ) values (");
			//UID
			buf.append(postgresConnection.addSingleQuoteComa(itemName + "__"+ uidLaboItem));
			//UID of Labo Specimen
			buf.append(postgresConnection.addSingleQuoteComa(uidLaboSpecimen));
			//Item name
			buf.append(postgresConnection.addSingleQuoteComa(itemName));
			//Item code
			buf.append(postgresConnection.addSingleQuoteComa(itemCode));
			//Item code ID
			buf.append(postgresConnection.addSingleQuoteComa(itemCodeId));
			//Acode
			if(Acode != null)
				buf.append(postgresConnection.addSingleQuoteComa(Acode));
			else
				buf.append("NULL,");
			//Icode
			if(Icode != null)
				buf.append(postgresConnection.addSingleQuoteComa(Icode));
			else
				buf.append("NULL,");
			//Scode
			if(Scode != null)
				buf.append(postgresConnection.addSingleQuoteComa(Scode));
			else
				buf.append("NULL,");
			//Mcode
			if(Mcode != null)
				buf.append(postgresConnection.addSingleQuoteComa(Mcode));
			else
				buf.append("NULL,");
			//Rcode
			if(Rcode != null)
				buf.append(postgresConnection.addSingleQuoteComa(Rcode));
			else
				buf.append("NULL,");
			//Value
			buf.append(postgresConnection.addSingleQuoteComa(value));
			//up
			if(up != null)
				buf.append(postgresConnection.addSingleQuoteComa(up));
			else
				buf.append("NULL,");
			//low
			if(low != null)
				buf.append(postgresConnection.addSingleQuoteComa(low));
			else
				buf.append("NULL,");
			//normol
			if(normol != null)
				buf.append(postgresConnection.addSingleQuoteComa(normol));
			else
				buf.append("NULL,");
			//out
			if(out != null)
				buf.append(postgresConnection.addSingleQuoteComa(out));
			else
				buf.append("NULL,");
			//unit
			if(unit != null)
				buf.append(postgresConnection.addSingleQuoteComa(unit));
			else
				buf.append("NULL,");
			//unit code
			if(uCode != null)
				buf.append(postgresConnection.addSingleQuoteComa(uCode));
			else
				buf.append("NULL,");
			//Unit Code ID
			if(uCodeId != null)
				buf.append(postgresConnection.addSingleQuoteComa(uCodeId));
			else
				buf.append("NULL,");
			//Item Memo
			if((itemMemo != null) && (itemMemo.length() > 0))
				buf.append(postgresConnection.addSingleQuoteComa(itemMemo.toString()));
			else
				buf.append("NULL,");
			//Item Memo Code Name
			if((itemMemoCodeName != null) && (itemMemoCodeName.length() > 0))
				buf.append(postgresConnection.addSingleQuoteComa(itemMemoCodeName.toString()));
			else
				buf.append("NULL,");
			//Item Memo Code
			if((itemMemoCode != null) && (itemMemoCode.length() > 0))
				buf.append(postgresConnection.addSingleQuoteComa(itemMemoCode.toString()));
			else
				buf.append("NULL,");
			//Item Memo Code ID
			if((itemMemoCodeId != null) && (itemMemoCodeId.length() > 0))
				buf.append(postgresConnection.addSingleQuoteComa(itemMemoCodeId.toString()));
			else
				buf.append("NULL,");
			//Item Free Memo
			if(itemFreeMemo != null)
				buf.append(postgresConnection.addSingleQuoteComa(itemFreeMemo));
			else
				buf.append("NULL,");
			//External Ref
			if((extRef != null) && (extRef.length() > 0))
				buf.append(postgresConnection.addSingleQuote(extRef.toString()));
			else
				buf.append("NULL");

			buf.append(")");

			//Converting buf to string
			sql = buf.toString();
			//check for Japanese Minus char
			sql = mirrorI.dolphin.util.CharConversion.convert(sql);
			logger.finer("SQL Statement: " + sql);
			try {
				st = conPostgres.createStatement();
				st.executeUpdate(sql);
				createLaboItemReturn = true;
			}
			catch (SQLException sqle) {
				logger.warning("SQL Exception while inserting Labo Item information into 'Tbl_Labo_Item' table");
				logger.warning( "Exception details:"  + sqle );
				createLaboItemReturn = false;
			}
			catch (Exception e) {
				logger.warning("Exception while inserting Labo Item information into 'Tbl_Labo_Item' table");
				logger.warning( "Exception details:"  + e );
				createLaboItemReturn = false;
			}
		}
		logger.finer("Method Exit");
		return createLaboItemReturn;
	}
}
