/*
 * DataConverison.java
 *
 * Delivered on 2003/01/31
 *
 * Revised for changed table column name and additional tables on 2003/03/24
 *
 */
package mirrorI.dolphin.data;


/**
 * @author  Aniruddha, Mirror - I
 */
public  class DataMapping {

    public final static String ldap1        = "cn=Patient,o=Dolphin";
    public final static String table1       = "patient";

    public final static String ldap2        = "cn=Document,cn=Karte,o=Dolphin";
    public final static String table2       = "DocInfo";

    public final static String ldap3        = "cn=SOA,cn=MedicalRecord,o=Dolphin";
    public final static String table3       = "soa";

    public final static String ldap4        = "cn=Order,cn=MedicalRecord,o=Dolphin";
    public final static String table4       = "morder";

    public final static String ldap5        = "cn=Image,cn=MedicalRecord,o=Dolphin";
    public final static String table5       = "image";

    public final static String ldap6        = "cn=Diagnosis,cn=MedicalRecord,o=Dolphin";
    public final static String table6       = "diagnosis";

    public final static String ldap7        = "cn=Allergy,cn=MedicalRecord,o=Dolphin";
    public final static String table7       = "allergy";

    public final static String ldap8        = "cn=Bloodtype,cn=MedicalRecord,o=Dolphin";
    public final static String table8       = "blood_type";

    public final static String ldap9        = "cn=Infection,cn=MedicalRecord,o=Dolphin";
    public final static String table9       = "infection";
    
    public final static String ldap10        = "cn=StampTree,cn=Seed,o=Dolphin";
    public final static String table10       = "stamp_tree";
    
    public final static String ldap11        = "cn=Stamp,cn=Tool,o=Dolphin";
    public final static String table11       = "stamp";
    
    public final static String ldap12        = "cn=Appointment,o=Dolphin";
    public final static String table12       = "appointment";
    
    public final static String masterl1      = "cn=Master,cn=Disease,cn=KMaster,o=Dolphin";
    public final static String mastert1      = "disease";
    
    public final static String masterl2      = "cn=Master,cn=MedicalSupply,cn=KMaster,o=Dolphin";
    public final static String mastert2      = "medicine";
    
    public final static String masterl3      = "cn=Master,cn=MedicalTreatment,cn=KMaster,o=Dolphin";
    public final static String mastert3      = "treatment";
    
    public final static String masterl4      = "cn=Master,cn=ToolMaterial,cn=KMaster,o=Dolphin";
    public final static String mastert4      = "tool_material";
    
    public final static String[] masterBase = {masterl1, masterl2, masterl3, masterl4};
    public final static String[] masterObjClass = {"mmlDisease", "mmlMedicalSupply", "mmlMedicalTreatment", "mmlToolMaterial"};
    public final static String[] masterTable = {mastert1, mastert2, mastert3, mastert4};

    public final static String[] ldapCN     = {ldap1,ldap2, ldap3, ldap4, ldap5, ldap6, ldap7, ldap8, ldap9, ldap10, ldap11, ldap12};

    public final static String[] databaseTable = {table1, table2, table3, table4, table5, table6, table7, table8, table9, table10, table11, table12};

    public final static String[][][] dataSpec    = {   //1st Item - LDAP field
                                                       //2nd Item - PostgreSQL table field
                                                       //3rd  Item - data type
                                                       //4th  Item - whether LDAPdata  needs change, if so change type

        {   //for 1st ladp / table : table patient
            {"mmlPid","pid","text","none"
            },
	    {"cn","cn","text","none"
            },
	    {"mmlKanaCn","kana","text","none"
            },
	    {"mmlRomanCn","roman","text","none"
            },
	    {"mmlSex","sex","text","none"
            },
	    {"mmlBirthday","birthday","text","none"
            },
	    {"mmlNationality","nationality","text","none"
            },
	    {"mmlMaritalStatus","maritalStatus","text","none"
            },
	    {"postalCode","postalCode","text","none"
            },
	    {"homePostalAddress","homePostalAddress","text","none"
            },
	    {"homePhone","homePhone","text","none"
            },
	    {"mail","mail","text","none"
            },
	    {"mmlLocalPid","localId","text","none"
            }
	},

	{   //for 2nd ladp / table : table docInfo
            {"mmlUid","uid","text","none"
            },
	    {"mmlPid","pid","text","none"
            },
	    {"mmlConfirmDate","confirmdate","text","none"
            },
	    {"mmlConfirmDate","firstConfirmdate","text","none"
            },
	    {"mmlDocType","docType","text","none"
            },
	    {"mmlTitle","title","text","none"
            },
	    {"mmlGenerationPurpose","purpose","text","none"
            },
	    {"mmlInsuranceClass","insuranceClass","text","none"
            },
	    {"mmlGroupId","groupId","text","none"
            },
	    {"mmlParentId","parentId","text","none"
            },
	    {"mmlCid","creatorId","text","keepCid"
            },
	    {"cn","creatorName","text","none"
            },
	    {"mmlCreatorLicense","creatorLicense","text","none"
            },
	    {"mmlVisibleFlag","status","char","visibleFlag"               //changed on 2003/03/22
	    },
            {"mmlUid","department","text","department"   //Field from LDAP is dummy, postgres value set by condition
	    }
	},

	{   //for 3rd ladp / table : table soa
            {"mmlGroupId","groupId","text","none"
	    },
	    {"mmlBinary","soabytes","bytea","none"
	    },
	    {"mmlVisibleFlag","status","char","visibleFlag"              //changed on 2003/03/22
	    }
	},

	{   //for 4th ladp / table : table morder
            {"mmlOrderId","orderNo","text","none"
	    },
	    {"mmlGroupId","groupId","text","none"
	    },
	    {"mmlPid","pid","text","none"
	    },
	    {"mmlConfirmDate","confirmDate","text","none"
	    },
	    {"mmlConfirmDate","firstConfirmDate","text","none"
	    },
            {"mmlOrderName","orderName","text","none"
            },
            {"mmlBinary","orderbytes","bytea","none"
            },
            {"mmlOrderId","creatorId","text","creatorID"          //Field from LDAP is dummy, postgres value set by condition
            },
            {"mmlVisibleFlag","status","char","visibleFlag"              //changed on 2003/03/22
            }
	},

	{   //for 5th ladp / table : table image
            {"mmlImageId","imageNo","text","none"
            },
            {"mmlGroupId","groupId","text","none"
            },
            {"mmlPid","pid","text","none"
            },
            {"mmlConfirmDate","confirmDate","text","none"
            },
            {"mmlConfirmDate","firstConfirmDate","text","none"
            },
            {"mmlHRef","href","text","none"
            },
            {"mmlContentType","contentType","text","none"
            },
            {"mmlTitle","title","text","none"
            },
            {"mmlMedicalRole","medicalRole","text","none"
            },
            {"jpegPhoto","jpegPhoto","bytea","none"
            },
            {"mmlImageId","creatorId","text","creatorID"          //Field from LDAP is dummy, postgres value set by condition
            },
            {"mmlVisibleFlag","status","char","visibleFlag"             //changed on 2003/03/22
            }
	},

	{   //for 6th ladp / table : table diagnosis
            {"mmlUid","uid","text","none"
            },
            {"mmlPid","pid","text","none"
            },
            {"mmlConfirmDate","confirmDate","text","none"
            },
            {"mmlConfirmDate","firstConfirmDate","text","none"
            },
            {"mmlDiagnosis","diagnosis","text","none"
            },
            {"mmlDiagnosisCode","diagnosisCode","text","none"
            },
            {"mmlDiagnosisCodeSystem","codeSystem","text","none"
            },
            {"mmlCategory","category","text","none"
            },
            {"mmlCategoryTableId","categoryTable","text","none"
            },
            {"mmlOutcome","outcome","text","none"
            },
            {"mmlFirstEncounterDate","firstEncounterDate","text","none"
            },
            {"mmlStartDate","startDate","text","none"
            },
            {"mmlEndDate","endDate","text","none"
            },
            {"mmlParentId","parentId","text","none"
            },
            {"mmlUid","creatorId","text","creatorID"
            },
            {"mmlVisibleFlag","status","char","visibleFlag"             //changed on 2003/03/22
            }
        },

	{   //for 7th ladp / table : table allergy   //added on 2003/03/22
            {"mmlUid","groupId","text","none"
            },
            {"mmlPid","pid","text","none"
            },
            {"mmlConfirmDate","confirmDate","text","none"
            },
            {"mmlConfirmDate","firstConfirmDate","text","none"
            },
            {"mmlFactor","factor","text","none"
            },
            {"mmlSeverity","severity","text","none"
            },
            {"mmlIdentifiedDate","identifiedDate","text","none"
            },
            {"mmlMemo","memo","text","none"
            },
            {"mmlMemo","creatorId","text","creatorID"          //Field from LDAP is dummy, postgres value set by condition
            },
            {"mmlVisibleFlag","status","char","visibleFlag"
            }
	},

	{   //for 8th ladp / table : blood_type   //added on 2003/03/22
            {"mmlUid","groupId","text","none"
            },
            {"mmlPid","pid","text","none"
            },
            {"mmlConfirmDate","confirmDate","text","none"
            },
            {"mmlConfirmDate","firstConfirmDate","text","none"
            },
            {"mmlABO","abo","text","none"
            },
            {"mmlRho","rho","text","none"
            },
            {"mmlMemo","memo","text","none"
            },
            {"mmlMemo","creatorId","text","creatorID"          //Field from LDAP is dummy, postgres value set by condition
            },
            {"mmlVisibleFlag","status","char","visibleFlag"
            }
	},

	{   //for 9th ladp / table : table infection   //added on 2003/03/22
            {"mmlUid","groupId","text","none"
            },
            {"mmlPid","pid","text","none"
            },
            {"mmlConfirmDate","confirmDate","text","none"
            },
            {"mmlConfirmDate","firstConfirmDate","text","none"
            },
            {"mmlFactor","factor","text","none"
            },
            {"mmlExamValue","examvalue","text","none"
            },
            {"mmlIdentifiedDate","identifiedDate","text","none"
            },
            {"mmlMemo","memo","text","none"
            },
            {"mmlMemo","creatorId","text","creatorID"          //Field from LDAP is dummy, postgres value set by condition
            },
            {"mmlVisibleFlag","status","char","visibleFlag"
            }
	},
        
        {   //for 10th ladp / table : table stamp_tree   //added on 2003/04/01
            {"uid","userId","text","none"
            },
            {"mmlBinary","tree","bytea","none"
            }
	},
        
        {   //for 11th ladp / table : table stamp   //added on 2003/04/01
            {"mmlStampId","stampId","text","none"
            },
            {"mmlBinary","stamp","bytea","none"
            },
            {"mmlStampId","userId","text","creatorID" //Field from LDAP is dummy, postgres value set by condition
            }
	},
        
        {   //for 12th ladp / table : table appointment   //added on 2003/04/01
            {"mmlAppointmentId","appointmemo","text","null"    // 2003-04-01 for appointment
            },
            {"mmlPid","pid","text","none"
            },
            {"mmlPvtDate","appointdate","text","none"
            },
            {"mmlAppointName","appointname","text","none"
            }
	}
        
    };


	public final static String[][] departmentData    = {

		{"01","����"},
		{"02","���_��"},
		{"03","�_�o��"},
		{"04","�_�o����"},
		{"05","�ċz���"},
		{"06","�������"},
		{"07","�ݒ���"},
		{"08","�z���"},
		{"09","������"},
		{"10","�O��"},
		{"11","���`�O��"},
		{"12","�`���O��"},
		{"13","���e�O��"},
		{"14","�]�_�o�O��"},
		{"15","�ċz��O��"},
		{"16","�S�����ǊO��"},
		{"17","�����O��"},
		{"18","�畆�ДA���"},
		{"19","�畆��"},
		{"20","�ДA���"},
		{"21","���a��"},
		{"22","�������"},
		{"23","�Y�w�l��"},
		{"24","�Y��"},
		{"25","�w�l��"},
		{"26","���"},
		{"27","���@���񂱂���"},
		{"28","�C�ǐH����"},
		{"29","���w�f�É�"},
		{"30","���ː���"},
		{"31","������"},
		{"32","�l�H���͉�"},
		{"33","�S�Ó���"},
		{"34","�A�����M�["},
		{"35","���E�}�`"},
		{"36","���n�r��"},
		{"A1","�I��"}
	};


	static String[] getLdapCN(){

		return ldapCN;
	}


	static String[] getDatabaseTable(){

		return databaseTable;
	}


	static String[] getLDAPAttribute(int indexOfCN){

       String [] attributeList = new String[dataSpec[indexOfCN].length];

       for(int i=0;i<dataSpec[indexOfCN].length;i++){

       attributeList[i] = new String(dataSpec[indexOfCN][i][0]);

	   }

		return attributeList;
	}


	static String[] getTableField(int indexOfCN){

	   String [] tableField = new String[dataSpec[indexOfCN].length];

	   for(int i=0;i<dataSpec[indexOfCN].length;i++){

	   tableField [i] = new String(dataSpec[indexOfCN][i][1]);

	   }

		return tableField;
	}


	static String[] getDataType(int indexOfCN){

	   String [] dataType = new String[dataSpec[indexOfCN].length];

	   for(int i=0;i<dataSpec[indexOfCN].length;i++){

	   dataType [i] = new String(dataSpec[indexOfCN][i][2]);

	   }

		return dataType;
	}


	static String[] getCondition(int indexOfCN){

	   String [] condition = new String[dataSpec[indexOfCN].length];

	   for(int i=0;i<dataSpec[indexOfCN].length;i++){

	   condition [i] = new String(dataSpec[indexOfCN][i][3]);

	   }

		return condition;
	}


	static String getDepartmentName(String id){

	   String name=null;
	   for(int i=0;i<departmentData.length;i++){
		  if(id.equals(departmentData[i][0])){
			  name = departmentData[i][1];
			  return name;
		  }
	   }
		return name;
	}
}
