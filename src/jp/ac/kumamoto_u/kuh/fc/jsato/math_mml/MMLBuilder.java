/*
 * MMLBuilder.java
 *
 * Created on 2001/09/18, 23:29
 */

package jp.ac.kumamoto_u.kuh.fc.jsato.math_mml;

import java.awt.*;
import java.util.*;
import org.xml.sax.*;

/**
 *
 * @author	Junzo SATO
 * @version
 */

public class MMLBuilder {
    // mmlTree holds all MML objects
    public Vector mmlTree = new Vector();
    // mmlInstruction has processing instructions in a xml document
    public Vector mmlInstruction = new Vector();
    public Vector mmlInstructionTable = new Vector();
    
    // mmlCmextRef objects in the content of modules
    // (these should be match with objects in extRefs)
    public Vector extRefInContent = new Vector();
    
    //--------------------------------------------------------------------------
    
    public Vector getMmlTree() {
        return mmlTree;
    }
    
    public Vector getMmlInstruction() {
        return mmlInstruction;
    }
    
    public Vector getMmlInstructionTable() {
        return mmlInstructionTable;
    }
    
    public void clearMmlTree() {
        if ( mmlTree != null ) {
            mmlTree.removeAllElements();
            mmlTree = null;
            mmlTree = new Vector();
            mmlTreeIndex = -1;
        } else {
            mmlTree = new Vector();
            mmlTreeIndex = -1;
        }
        
        //========================================
        // clear instruction too
        if (mmlInstruction != null) {
            mmlInstruction.removeAllElements();
            mmlInstruction = null;
            mmlInstruction = new Vector();
        } else {
            mmlInstruction = new Vector();
        }
        
        if (mmlInstructionTable != null) {
            mmlInstructionTable.removeAllElements();
            mmlInstructionTable = null;
            mmlInstructionTable = new Vector();
        } else {
            mmlInstructionTable = new Vector();
        }
        
        if (extRefInContent != null) {
            extRefInContent.removeAllElements();
            extRefInContent = null;
            extRefInContent = new Vector();
        } else {
            extRefInContent = new Vector();
        }
        //========================================
    }
    
    // mmlTreeIndex is used by the parser
    public int mmlTreeIndex = -1;

    public void adjustIndex() {
        // this method is called when the parser finds the element
        mmlTreeIndex = mmlTree.size() - 1;
    }
    
    public void restoreIndex() {
        // this method is called when the parser leaves the elemnt
        mmlTreeIndex = getElement().getParentIndex();
    }

    // currentElement is the variable to keep the current element 
    // while traversing xml document
    protected MMLObject currentElement = null;
    public MMLObject getCurrentElement() {
        return currentElement;
    }
    
    public void setCurrentElement(MMLObject obj) {
        currentElement = obj;
    }
    
    public MMLObject getElement() {
        // mmlTreeIndex points the location of the element in the mmlTree
        if (mmlTreeIndex >= 0) {
            return (MMLObject)mmlTree.elementAt(mmlTreeIndex);
        }
        return null;
    }
    
    public MMLObject getParent() {
        // returns the parent element in the mmlTree
        return (MMLObject)mmlTree.elementAt( getElement().getParentIndex() );
    }
    
    // builder objects
    //Vector v = null;
    private Vector v = new Vector();
    
    /** Creates new MMLBuilder */
    public MMLBuilder(/*Vector builders*/) {
        //v = builders;
        InitVector();
        clearMmlTree();
    }
    
    //======================================================================
    
    public void buildStart(String namespaceURI, String localName, String qName, Attributes atts) {
        int i = 0;
        while (i < v.size()) {
            if (true == ((MMLObject)v.elementAt(i)).buildStart(namespaceURI, localName, qName, atts, this)) {
                break;
            }
            ++i;
        }
    }
    
    public void buildEnd(String namespaceURI, String localName, String qName) {
        int i = 0;
        while (i < v.size()) {
            if (true == ((MMLObject)v.elementAt(i)).buildEnd(namespaceURI, localName, qName, this)) {
                break;
            }
            ++i;
        }
    }
    
    public void characters(char[] ch, int start, int length) {
        int i = 0;
        while (i < v.size()) {
            if (true == ((MMLObject)v.elementAt(i)).characters(ch, start, length, this)) {
                break;
            }
            ++i;
        }
    }
    
    //======================================================================
    
    public void releaseVector() {
        if (v != null ) {
            v.removeAllElements();
            v = null;
        }
    }
    
    protected void InitVector() {
        //----------------------------------------------------------------------
	 // FOR DOLPHIN MML //
	 //-----------------//
		v.add( new xhtmlbr() );
		v.add( new xhtmli() );
		v.add( new xhtmlb() );
		v.add( new xhtmlu() );
		v.add( new xhtmlfont() );
		v.add( new mmlAdAddress() );
		v.add( new mmlAdcountryCode() );
		v.add( new mmlAdzip() );
		v.add( new mmlAdhomeNumber() );
		v.add( new mmlAdtown() );
		v.add( new mmlAdcity() );
		v.add( new mmlAdprefecture() );
		v.add( new mmlAdfull() );
		v.add( new mmlPhPhone() );
		v.add( new mmlPhmemo() );
		v.add( new mmlPhcountry() );
		v.add( new mmlPhextension() );
		v.add( new mmlPhnumber() );
		v.add( new mmlPhcity() );
		v.add( new mmlPharea() );
		v.add( new mmlCmId() );
		v.add( new mmlCmextRef() );
		v.add( new mmlCmemail() );
		v.add( new mmlNmName() );
		v.add( new mmlNmdegree() );
		v.add( new mmlNmprefix() );
		v.add( new mmlNmfullname() );
		v.add( new mmlNmmiddle() );
		v.add( new mmlNmgiven() );
		v.add( new mmlNmfamily() );
		v.add( new mmlFcFacility() );
		v.add( new mmlFcname() );
		v.add( new mmlDpDepartment() );
		v.add( new mmlDpname() );
		v.add( new mmlPsiPersonalizedInfo() );
		v.add( new mmlPsiphones() );
		v.add( new mmlPsiemailAddresses() );
		v.add( new mmlPsiaddresses() );
		v.add( new mmlPsipersonName() );
		v.add( new mmlCiCreatorInfo() );
		v.add( new mmlCicreatorLicense() );
		v.add( new Mml() );
		v.add( new MmlHeader() );
		v.add( new MmlBody() );
		v.add( new docInfo() );
		v.add( new encryptInfo() );
		v.add( new title() );
		v.add( new docId() );
		v.add( new masterId() );
		v.add( new content() );
		v.add( new toc() );
		v.add( new tocItem() );
		v.add( new extRefs() );
		v.add( new scopePeriod() );
		v.add( new MmlModuleItem() );
		v.add( new uid() );
		v.add( new parentId() );
		v.add( new groupId() );
		v.add( new confirmDate() );
		v.add( new securityLevel() );
		v.add( new accessRight() );
		v.add( new mmlScfacility() );
		v.add( new mmlScfacilityName() );
		v.add( new mmlScperson() );
		v.add( new mmlScpersonName() );
		v.add( new mmlSclicense() );
		v.add( new mmlSclicenseName() );
		v.add( new mmlScdepartment() );
		v.add( new mmlScdepartmentName() );
		v.add( new mmlPiPatientModule() );
		v.add( new mmlPiotherId() );
		v.add( new mmlPimasterId() );
		v.add( new mmlPiuniqueInfo() );
		v.add( new mmlPideath() );
		v.add( new mmlPisocialIdentification() );
		v.add( new mmlPiaccountNumber() );
		v.add( new mmlPiphones() );
		v.add( new mmlPiemailAddresses() );
		v.add( new mmlPiaddresses() );
		v.add( new mmlPimarital() );
		v.add( new mmlPinationality() );
		v.add( new mmlPisex() );
		v.add( new mmlPibirthday() );
		v.add( new mmlPipersonName() );
		v.add( new mmlHiHealthInsuranceModule() );
		v.add( new mmlHiinsuranceClass() );
		v.add( new mmlHiinsuranceNumber() );
		v.add( new mmlHiclientId() );
		v.add( new mmlHigroup() );
		v.add( new mmlHinumber() );
		v.add( new mmlHifamilyClass() );
		v.add( new mmlHiclientInfo() );
		v.add( new mmlHipersonName() );
		v.add( new mmlHiaddresses() );
		v.add( new mmlHiphones() );
		v.add( new mmlHicontinuedDiseases() );
		v.add( new mmlHidiseases() );
		v.add( new mmlHistartDate() );
		v.add( new mmlHiexpiredDate() );
		v.add( new mmlHipaymentInRatio() );
		v.add( new mmlHipaymentOutRatio() );
		v.add( new mmlHiinsuredInfo() );
		v.add( new mmlHifacility() );
		v.add( new mmlHiworkInfo() );
		v.add( new mmlHipublicInsurance() );
		v.add( new mmlHipublicInsuranceItem() );
		v.add( new mmlHiproviderName() );
		v.add( new mmlHiprovider() );
		v.add( new mmlHirecipient() );
		v.add( new mmlHipaymentRatio() );
		v.add( new mmlRdRegisteredDiagnosisModule() );
		v.add( new mmlRddiagnosis() );
		v.add( new mmlRddiagnosisContents() );
		v.add( new mmlRddxItem() );
		v.add( new mmlRdname() );
		v.add( new mmlRdcategory() );
		v.add( new mmlRdstartDate() );
		v.add( new mmlRdendDate() );
		v.add( new mmlRdoutcome() );
		v.add( new mmlRdfirstEncounterDate() );
		v.add( new mmlRdrelatedHealthInsurance() );
		v.add( new mmlRdcategories() );
		v.add( new mmlLsLifestyleModule() );
		v.add( new mmlLsoccupation() );
		v.add( new mmlLstobacco() );
		v.add( new mmlLsalcohol() );
		v.add( new mmlLsother() );
		v.add( new mmlBcBaseClinicModule() );
		v.add( new mmlBcallergy() );
		v.add( new mmlBcallergyItem() );
		v.add( new mmlBcfactor() );
		v.add( new mmlBcseverity() );
		v.add( new mmlBcidentifiedDate() );
		v.add( new mmlBcmemo() );
		v.add( new mmlBcbloodtype() );
		v.add( new mmlBcrh() );
		v.add( new mmlBcabo() );
		v.add( new mmlBcother() );
		v.add( new mmlBctypeName() );
		v.add( new mmlBctypeJudgement() );
		v.add( new mmlBcdescription() );
		v.add( new mmlBcinfection() );
		v.add( new mmlBcinfectionItem() );
		v.add( new mmlBcexamValue() );
		v.add( new mmlBcothers() );
		v.add( new mmlFclFirstClinicModule() );
		v.add( new mmlFclfamilyHistory() );
		v.add( new mmlFclfamilyHistoryItem() );
		v.add( new mmlFclrelation() );
		v.add( new mmlFclage() );
		v.add( new mmlFclmemo() );
		v.add( new mmlFclchildhood() );
		v.add( new mmlFclbirthInfo() );
		v.add( new mmlFcldeliveryWeeks() );
		v.add( new mmlFcldeliveryMethod() );
		v.add( new mmlFclbodyWeight() );
		v.add( new mmlFclbodyHeight() );
		v.add( new mmlFclchestCircumference() );
		v.add( new mmlFclheadCircumference() );
		v.add( new mmlFclvaccination() );
		v.add( new mmlFclvaccinationItem() );
		v.add( new mmlFclvaccine() );
		v.add( new mmlFclinjected() );
		v.add( new mmlFclpastHistory() );
		v.add( new mmlFclpastHistoryItem() );
		v.add( new mmlFcltimeExpression() );
		v.add( new mmlFcleventExpression() );
		v.add( new mmlFclchiefComplaints() );
		v.add( new mmlFclpresentIllnessNotes() );
		v.add( new mmlFclfreeNotes() );
		v.add( new mmlPcProgressCourseModule() );
		v.add( new mmlPcproblem() );
		v.add( new mmlPcsubjective() );
		v.add( new mmlPcsubjectiveItem() );
		v.add( new mmlPctimeExpression() );
		v.add( new mmlPceventExpression() );
		v.add( new mmlPcobjective() );
		v.add( new mmlPcphysicalExam() );
		v.add( new mmlPcphysicalExamItem() );
		v.add( new mmlPctitle() );
		v.add( new mmlPcresult() );
		v.add( new mmlPcinterpretation() );
		v.add( new mmlPcreferenceInfo() );
		v.add( new mmlPctestResult() );
		v.add( new mmlPcrxRecord() );
		v.add( new mmlPctxRecord() );
		v.add( new mmlPcplanNotes() );
		v.add( new mmlPcassessment() );
		v.add( new mmlPcassessmentItem() );
		v.add( new mmlPcplan() );
		v.add( new mmlPctestOrder() );
		v.add( new mmlPcrxOrder() );
		v.add( new mmlPctxOrder() );
		v.add( new mmlPcobjectiveNotes() );
		v.add( new mmlPcFreeExpression() );
		v.add( new mmlPcstructuredExpression() );
		v.add( new mmlPcproblemItem() );
		v.add( new mmlPcfreeNotes() );
		v.add( new mmlSgSurgeryModule() );
		v.add( new mmlSgsurgeryItem() );
		v.add( new mmlSgsurgicalInfo() );
		v.add( new mmlSgsurgicalDepartment() );
		v.add( new mmlSgpatientDepartment() );
		v.add( new mmlSgdate() );
		v.add( new mmlSgstartTime() );
		v.add( new mmlSgduration() );
		v.add( new mmlSgsurgicalDiagnosis() );
		v.add( new mmlSgtitle() );
		v.add( new mmlSgmemo() );
		v.add( new mmlSgsurgicalProcedure() );
		v.add( new mmlSgprocedureItem() );
		v.add( new mmlSgoperationElement() );
		v.add( new mmlSgoperationElementItem() );
		v.add( new mmlSgsurgicalStaffs() );
		v.add( new mmlSgstaff() );
		v.add( new mmlSgstaffInfo() );
		v.add( new mmlSganesthesiaProcedure() );
		v.add( new mmlSganesthesiologists() );
		v.add( new mmlSgoperativeNotes() );
		v.add( new mmlSgreferenceInfo() );
		v.add( new mmlSgoperation() );
		v.add( new mmlSgprocedureMemo() );
		v.add( new mmlSganesthesiaDuration() );
		v.add( new mmlSmSummaryModule() );
		v.add( new mmlSmserviceHistory() );
		v.add( new mmlSmoutPatient() );
		v.add( new mmlSminPatient() );
		v.add( new mmlSmoutPatientItem() );
		v.add( new mmlSminPatientItem() );
		v.add( new mmlSmdate() );
		v.add( new mmlSmoutPatientCondition() );
		v.add( new mmlSmstaffs() );
		v.add( new mmlSmadmission() );
		v.add( new mmlSmdischarge() );
		v.add( new mmlSmadmissionCondition() );
		v.add( new mmlSmreferFrom() );
		v.add( new mmlSmdischargeCondition() );
		v.add( new mmlSmreferTo() );
		v.add( new mmlSmstaffInfo() );
		v.add( new mmlSmdeathInfo() );
		v.add( new mmlSmchiefComplaints() );
		v.add( new mmlSmpatientProfile() );
		v.add( new mmlSmhistory() );
		v.add( new mmlSmphysicalExam() );
		v.add( new mmlSmclinicalCourse() );
		v.add( new mmlSmdischargeFindings() );
		v.add( new mmlSmmedication() );
		v.add( new mmlSmtestResults() );
		v.add( new mmlSmplan() );
		v.add( new mmlSmremarks() );
		v.add( new mmlSmclinicalRecord() );
		v.add( new mmlSmtestResult() );
		v.add( new mmlSmrelatedDoc() );
		v.add( new mmlLbTestModule() );
		v.add( new mmlLbinformation() );
		v.add( new mmlLblaboTest() );
		v.add( new mmlLbreportStatus() );
		v.add( new mmlLbset() );
		v.add( new mmlLbfacility() );
		v.add( new mmlLbdepartment() );
		v.add( new mmlLbward() );
		v.add( new mmlLbclient() );
		v.add( new mmlLblaboratoryCenter() );
		v.add( new mmlLbtechnician() );
		v.add( new mmlLbrepMemo() );
		v.add( new mmlLbrepMemoF() );
		v.add( new mmlLbspecimen() );
		v.add( new mmlLbspecimenName() );
		v.add( new mmlLbspcMemo() );
		v.add( new mmlLbspcMemoF() );
		v.add( new mmlLbitem() );
		v.add( new mmlLbitemName() );
		v.add( new mmlLbvalue() );
		v.add( new mmlLbnumValue() );
		v.add( new mmlLbunit() );
		v.add( new mmlLbreferenceInfo() );
		v.add( new mmlLbitemMemo() );
		v.add( new mmlLbitemMemoF() );
		v.add( new mmlRpReportModule() );
		v.add( new mmlRpinformation() );
		v.add( new mmlRpreportStatus() );
		v.add( new mmlRptestClass() );
		v.add( new mmlRptestSubclass() );
		v.add( new mmlRporgan() );
		v.add( new mmlRpconsultFrom() );
		v.add( new mmlRpconFacility() );
		v.add( new mmlRpconDepartment() );
		v.add( new mmlRpconWard() );
		v.add( new mmlRpclient() );
		v.add( new mmlRpperform() );
		v.add( new mmlRppFacility() );
		v.add( new mmlRppDepartment() );
		v.add( new mmlRppWard() );
		v.add( new mmlRpperformer() );
		v.add( new mmlRpsupervisor() );
		v.add( new mmlRpreportBody() );
		v.add( new mmlRpchiefComplaints() );
		v.add( new mmlRptestPurpose() );
		v.add( new mmlRptestDx() );
		v.add( new mmlRptestNotes() );
		v.add( new mmlRptestMemo() );
		v.add( new mmlRptestMemoF() );
		v.add( new mmlReReferralModule() );
		v.add( new mmlReoccupation() );
		v.add( new mmlRereferFrom() );
		v.add( new mmlRetitle() );
		v.add( new mmlRegreeting() );
		v.add( new mmlRechiefComplaints() );
		v.add( new mmlReclinicalDiagnosis() );
		v.add( new mmlRepastHistory() );
		v.add( new mmlRefamilyHistory() );
		v.add( new mmlRepresentIllness() );
		v.add( new mmlRetestResults() );
		v.add( new mmlRemedication() );
		v.add( new mmlRereferPurpose() );
		v.add( new mmlReremarks() );
		v.add( new mmlRereferToFacility() );
		v.add( new mmlRereferToPerson() );
		v.add( new mmlRereferToUnknownName() );
		v.add( new claimClaimModule() );
		v.add( new claiminformation() );
		v.add( new claimbundle() );
		v.add( new claimappoint() );
		v.add( new claimappName() );
		v.add( new claimpatientDepartment() );
		v.add( new claimpatientWard() );
		v.add( new claimclassName() );
		v.add( new claimadministration() );
		v.add( new claimadmMemo() );
		v.add( new claimbundleNumber() );
		v.add( new claimitem() );
		v.add( new claimname() );
		v.add( new claimnumber() );
		v.add( new claimduration() );
		v.add( new claimlocation() );
		v.add( new claimfilm() );
		v.add( new claimevent() );
		v.add( new claimmemo() );
		v.add( new claimfilmSize() );
		v.add( new claimfilmNumber() );
		v.add( new claimAClaimAmountModule() );
		v.add( new claimAamountInformation() );
		v.add( new claimAbundle() );
		v.add( new claimApatientDepartment() );
		v.add( new claimApatientWard() );
		v.add( new claimAclassName() );
		v.add( new claimAclaimBundlePoint() );
		v.add( new claimAclaimBundleRate() );
		v.add( new claimAadministration() );
		v.add( new claimAadmMemo() );
		v.add( new claimAbundleNumber() );
		v.add( new claimAmethodPoint() );
		v.add( new claimAmaterialPoint() );
		v.add( new claimAdrugPoint() );
		v.add( new claimAppsClass() );
		v.add( new claimAitem() );
		v.add( new claimAname() );
		v.add( new claimAnumber() );
		v.add( new claimAclaimPoint() );
		v.add( new claimAclaimRate() );
		v.add( new claimAduration() );
		v.add( new claimAlocation() );
		v.add( new claimAfilm() );
		v.add( new claimAevent() );
		v.add( new claimAmemo() );
		v.add( new claimAfilmSize() );
		v.add( new claimAfilmNumber() );
        //----------------------------------------------------------------------
	 // FOR MML QUERY //
	 //---------------//
                v.add( new userId() );
                v.add( new userPassword() );
                v.add( new loginRequest() );
                v.add( new resultCode() );
                v.add( new resultString() );
                v.add( new loginResult() );
                v.add( new MmlAddendum() );
        //----------------------------------------------------------------------      
        //printlnStatus("MMLBuilder Vector: " + v.size());
    }
}
