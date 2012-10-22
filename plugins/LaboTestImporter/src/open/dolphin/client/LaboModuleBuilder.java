
package open.dolphin.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import open.dolphin.delegater.LaboDelegater;
import open.dolphin.infomodel.LaboImportSummary;
import open.dolphin.infomodel.LaboItemValue;
import open.dolphin.infomodel.LaboModuleValue;
import open.dolphin.infomodel.LaboSpecimenValue;
import open.dolphin.infomodel.ModelUtils;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.project.Project;

import open.dolphin.util.GUIDGenerator;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.Namespace;

/**
 * LaboModuleBuilder
 *
 * @author Kazushi Minagawa
 */
public class LaboModuleBuilder {
    
    //private static final Namespace xhtml = Namespace.getNamespace("xhtml","http://www.w3.org/1999/xhtml");
    private static final Namespace mmlCm = Namespace.getNamespace("mmlCm","http://www.medxml.net/MML/SharedComponent/Common/1.0");
    private static final Namespace mmlNm = Namespace.getNamespace("mmlNm","http://www.medxml.net/MML/SharedComponent/Name/1.0");
    private static final Namespace mmlFc = Namespace.getNamespace("mmlFc","http://www.medxml.net/MML/SharedComponent/Facility/1.0");
    private static final Namespace mmlDp = Namespace.getNamespace("mmlDp","http://www.medxml.net/MML/SharedComponent/Department/1.0");
    private static final Namespace mmlAd = Namespace.getNamespace("mmlAd","http://www.medxml.net/MML/SharedComponent/Address/1.0");
    private static final Namespace mmlPh = Namespace.getNamespace("mmlPh","http://www.medxml.net/MML/SharedComponent/Phone/1.0");
    private static final Namespace mmlPsi = Namespace.getNamespace("mmlPsi","http://www.medxml.net/MML/SharedComponent/PersonalizedInfo/1.0");
    private static final Namespace mmlCi = Namespace.getNamespace("mmlCi","http://www.medxml.net/MML/SharedComponent/CreatorInfo/1.0");
    private static final Namespace mmlPi = Namespace.getNamespace("mmlPi","http://www.medxml.net/MML/ContentModule/PatientInfo/1.0");
    //private static final Namespace mmlBc = Namespace.getNamespace("mmlBc","http://www.medxml.net/MML/ContentModule/BaseClinic/1.0");
    //private static final Namespace mmlFcl = Namespace.getNamespace("mmlFcl","http://www.medxml.net/MML/ContentModule/FirstClinic/1.0");
    private static final Namespace mmlHi = Namespace.getNamespace("mmlHi","http://www.medxml.net/MML/ContentModule/HealthInsurance/1.1");
    //private static final Namespace mmlLs = Namespace.getNamespace("mmlLs","http://www.medxml.net/MML/ContentModule/Lifestyle/1.0");
    //private static final Namespace mmlPc = Namespace.getNamespace("mmlPc","http://www.medxml.net/MML/ContentModule/ProgressCourse/1.0");
    //private static final Namespace mmlRd = Namespace.getNamespace("mmlRd","http://www.medxml.net/MML/ContentModule/RegisteredDiagnosis/1.0");
    //private static final Namespace mmlSg = Namespace.getNamespace("mmlSg","http://www.medxml.net/MML/ContentModule/Surgery/1.0");
    //private static final Namespace mmlSm = Namespace.getNamespace("mmlSm","http://www.medxml.net/MML/ContentModule/Summary/1.0");
    private static final Namespace mmlLb = Namespace.getNamespace("mmlLb","http://www.medxml.net/MML/ContentModule/test/1.0");
    //private static final Namespace mmlRp = Namespace.getNamespace("mmlRp","http://www.medxml.net/MML/ContentModule/report/1.0");
    //private static final Namespace mmlRe = Namespace.getNamespace("mmlRe","http://www.medxml.net/MML/ContentModule/Referral/1.0");
    private static final Namespace mmlSc = Namespace.getNamespace("mmlSc","http://www.medxml.net/MML/SharedComponent/Security/1.0");
    private static final Namespace claim = Namespace.getNamespace("claim","http://www.medxml.net/claim/claimModule/2.1");
    //private static final Namespace claimA = Namespace.getNamespace("claimA","http://www.medxml.net/claim/claimAmountModule/2.1");
    
    private String patientId;
    private String patientIdType;
    private String patientIdTypeTableId;
    private String moduleUUID;
    private String confirmDate;
    private ArrayList<LaboModuleValue> allModules;
    private LaboModuleValue laboModule;
    private LaboSpecimenValue laboSpecimen;
    private LaboItemValue laboItem;
    private boolean masterId;
    private List<File> parseFiles;
    private String encoding;
    private LaboDelegater laboDelegater;
    // != null
    private Logger logger;
    private boolean DEBUG = false;
    
    /** LaboModuleBuilder �𐶐�����B */
    public LaboModuleBuilder() {
    }
    
    public void setLogger(Logger l) {
        this.logger = l;
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    public void setEncoding(String enc) {
        encoding = enc;
    }
    
    public LaboDelegater getLaboDelegater() {
        return laboDelegater;
    }
    
    public void setLaboDelegater(LaboDelegater laboDelegater) {
        this.laboDelegater = laboDelegater;
    }
    
    public List<LaboModuleValue> getProduct() {
        return allModules != null ? allModules : null;
    }
    
    /**
     * ������MML�������ʃt�@�C�����p�[�X�����̒��Ɋ܂܂��
     * �������ʃ��W���[���̃��X�g��Ԃ��B
     * @param file MML�������ʃt�@�C��
     * @return �p�[�X�������W���[�� LaboModuleValue �̃��X�g
     */
    public List<LaboModuleValue> build(File file) {
        
        if (logger == null) {
            setLogger(ClientContext.getLogger("laboTest"));
        }
        
        if (file == null ) {
            return null;
        }
        
        try {
            String name = file.getName();
            logger.info(name + " �̃p�[�X���J�n���܂�");
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),encoding));
            parse(reader);
            reader.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("Exception while building LaboModules" + e.toString());
        }
        
        return getProduct();
    }
    
    /**
     * MML�������ʃt�@�C�����p�[�X����B
     * @param files MML�������ʃt�@�C���̔z��
     */
    public List<LaboImportSummary> build(List<File> files) {
        
        if (logger == null) {
            setLogger(ClientContext.getLogger("laboTest"));
        }
        
        parseFiles = files;
        if (parseFiles == null || parseFiles.size() == 0) {
            logger.warn("�p�[�X����t�@�C��������܂���");
            return null;
        }
        if (laboDelegater == null) {
            logger.warn("���{�e�X�g�p�̃f���Q�[�^���ݒ肳��Ă��܂���");
            return null;
        }
        if (encoding == null) {
            encoding = "UTF-8";
            logger.debug("�f�t�H���g�̃G���R�[�f�B���O" + encoding + "���g�p���܂�");
        } else {
            logger.debug("�G���R�[�f�B���O��" + encoding + "���w�肳��Ă��܂�");
        }
        
        // �p�[�X�y�ѓo�^�ɐ��������f�[�^�̏�񃊃X�g�𐶐�����
        // ���̃��\�b�h�̃��^�[���l
        List<LaboImportSummary> ret = new ArrayList<LaboImportSummary>(files.size());
        
        // �t�@�C�����C�e���[�g����
        for (File file : parseFiles) {
            
            try {
                // �t�@�C�������o�͂���
                String name = file.getName();
                logger.info(name + " �̃p�[�X���J�n���܂�");
                
                // ��̃t�@�C���Ɋ܂܂��SLaboModule�̃��X�g�𐶐�����
                // �p�[�X���ʂ�LaboModuleValue���i�[���郊�X�g�ł���
                if (allModules == null) {
                    allModules = new ArrayList<LaboModuleValue>(1);
                } else {
                    allModules.clear();
                }
                
                // ���̓X�g���[���𐶐����p�[�X����
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
                parse(reader);
                reader.close();
                
                // �p�[�X�̗�O�������őS�ăL���b�`����
            } catch (Exception pe) {
                pe.printStackTrace();
                logger.warn("�p�[�X���ɗ�O�������܂����B");
                if (pe.getCause() != null) {
                    logger.warn("����: " + pe.getCause());
                }
                if (pe.getMessage() != null) {
                    logger.warn("���e: " + pe.getMessage());
                }
                continue;
            }
            
            // �p�[�X��f�[�^�x�[�X�֓o�^����
            for (LaboModuleValue module : allModules) {
                
                LaboImportSummary summary = new LaboImportSummary();
                summary.setPatientId(module.getPatientId());
                if (module.getSetName() != null) {
                    summary.setSetName(module.getSetName());
                } else {
                    Collection<LaboSpecimenValue> c = module.getLaboSpecimens();
                    for (LaboSpecimenValue specimen : c) {
                        summary.setSetName(specimen.getSpecimenName());
                    }
                }
                summary.setSampleTime(module.getSampleTime());
                summary.setReportTime(module.getReportTime());
                summary.setLaboratoryCenter(module.getLaboratoryCenter());
                summary.setReportStatus(module.getReportStatus());
                
                PatientModel reply = laboDelegater.putLaboModule(module);
                
                if (laboDelegater.isNoError()) {
                    summary.setPatient(reply);
                    summary.setResult("����");
                    logger.info("LaboModule��o�^���܂����B����ID :" + module.getPatientId());
                    
                    ret.add(summary);
                    
                } else {
                    logger.warn("LaboModule ��o�^�ł��܂���ł����B����ID :" + module.getPatientId());
                    logger.warn(laboDelegater.getErrorMessage());
                    summary.setResult("�G���[");
                }
            }
        }
        
        return ret;
    }
    
    /**
     * ���̓X�g���[���̌������ʂ��p�[�X����B
     */
    public void parse(BufferedReader reader) throws IOException, Exception {
        
        SAXBuilder docBuilder = new SAXBuilder();
        Document doc = docBuilder.build(reader);
        Element root = doc.getRootElement();
        
        // Header���p�[�X����
        parseHeader(root.getChild("MmlHeader"));
        
        // Body���p�[�X����
        parseBody(root.getChild("MmlBody"));
    }
    
    /**
     * MML�w�b�_�[���p�[�X����B
     * �擾����̂� MasterId�� mmlCm:Id ����ID�̂݁B
     * @param header �w�b�_�[�v�f
     */
    private void parseHeader(Element header) {
        
        Element masterIdElement = header.getChild("masterId");
        Element id = masterIdElement.getChild("Id", mmlCm);
        if (id == null) {
            logger.info("id is null");
        }
        
        patientId = id.getText();
        patientIdType = id.getAttributeValue("type", mmlCm);
        patientIdTypeTableId = id.getAttributeValue("tableId", mmlCm);
        
        logger.debug("patientId = " + patientId);
        logger.debug("patientIdType = " + patientIdType);
        logger.debug("patientId TableId = " + patientIdTypeTableId);
    }
    
    /**
     * MML Body���p�[�X����B
     * ModuleItem��DocInfo�� uuid, confirmdate���擾����B
     * @param body Body�v�f
     */
    private void parseBody(Element body) {
        
        // MmlModuleItem �̃��X�g�𓾂�
        List children = body.getChildren("MmlModuleItem");
        
        //
        // ������C�e���[�g����
        //
        for (Iterator iterator = children.iterator(); iterator.hasNext();) {
            
            Element moduleItem = (Element) iterator.next();
            
            //
            // ModuleItem = docInfo + content �Ȃ̂ŕv�X�̗v�f�𓾂�
            //
            Element docInfo = moduleItem.getChild("docInfo");
            Element content = moduleItem.getChild("content");
            
            // docInfo �� contentModuleType �𒲂ׂ�
            String attr = docInfo.getAttributeValue("contentModuleType");
            
            // LaboTest Module �݂̂��p�[�X����
            if (attr.equals("test")) {
                
                // uuid ���擾����
                moduleUUID = docInfo.getChild("docId").getChildTextTrim("uid");
                logger.debug("module UUID = " + moduleUUID);
                if (moduleUUID == null || moduleUUID.length() != 32) {
                    moduleUUID = GUIDGenerator.generate(this);
                    logger.debug("Changed module UUID to " + moduleUUID);
                }
                
                // �m������擾����
                confirmDate = docInfo.getChildTextTrim("confirmDate");
                logger.debug("confirmDate = " + confirmDate);
                int tIndex = confirmDate.indexOf("T");
                if (tIndex < 0) {
                    confirmDate += "T00:00:00";
                    logger.debug("Changed confirmDate to " + confirmDate);
                }
                
                // ��͂��郂�W���[����mmlLb:TestModule
                Element testModule = content.getChild("TestModule", mmlLb);
                
                // ���̗v�f�ɑΉ�����I�u�W�F�N�g�𐶐����A���X�g�։�����
                laboModule = new LaboModuleValue();
                allModules.add(laboModule);
                
                // ����܂łɎ擾������{����ݒ肷��
                // ����ID�AModuleUUID�A�m�����ݒ肷��
                laboModule.setCreator(Project.getUserModel());
                laboModule.setPatientId(patientId);
                laboModule.setPatientIdType(patientIdType);
                laboModule.setPatientIdTypeCodeSys(patientIdTypeTableId);
                laboModule.setDocId(moduleUUID);
                
                // �m����A�K���J�n���A�L�^����ݒ肷��
                Date confirmed = ModelUtils.getDateTimeAsObject(confirmDate);
                laboModule.setConfirmed(confirmed);
                laboModule.setStarted(confirmed);
                laboModule.setRecorded(new Date());
                laboModule.setStatus("F");
                
                // �v�f���p�[�X����
                parseTestModule(testModule);
                
            }
        } 
    }
    
    /**
     * Content�v�f���p�[�X����B
     * �N���C�A���g���A���{�Z���^�[���A���̏��A�������ڏ����擾����B
     * @param content �������ʂ�����R���e���g�v�f
     */
    private void parseTestModule(Element testModule) {
        
        // �R���e���g�v�f�̎q��񋓂���
        List children = testModule.getChildren();
        Iterator iterator = children.iterator();
        
        while (iterator.hasNext()) {
            
            Element child = (Element) iterator.next();
            //String ename = child.getName();
            String qname = child.getQualifiedName();
            Namespace ns = child.getNamespace();
            debug(child.toString());
            String val = null;
            
            if (qname.equals("mmlLb:information")) {
                // mmlLb:information�v�f���p�[�X����
                //logger.debug("infomation�@���p�[�X��");
                
                // �o�^ID�������擾����
                val = child.getAttributeValue("registId", ns);
                logger.debug("registId = " + val);
                laboModule.setRegistId(val);
                
                // �T���v���^�C���������擾����
                val = child.getAttributeValue("sampleTime", ns);
                logger.debug("sampleTime = " + val);
                laboModule.setSampleTime(val);
                
                // �o�^�����������擾����
                val = child.getAttributeValue("registTime", ns);
                logger.debug("registTime = " + val);
                laboModule.setRegistTime(val);
                
                // �񍐎��ԑ������擾����
                val = child.getAttributeValue("reportTime", ns);
                logger.debug("reportTime = " + val);
                laboModule.setReportTime(val);
                
                
            } else if (qname.equals("mmlLb:reportStatus")) {
                // mmlLb:reportStatus�v�f���p�[�X����
                //logger.info("reportStatus�@���p�[�X��");
                
                // ���|�[�g�X�e�[�^�X���擾����
                val = child.getTextTrim();
                logger.debug("reportStatus = " + val);
                laboModule.setReportStatus(val);
                
                // statusCode���擾����
                val = child.getAttributeValue("statusCode", ns);
                logger.debug("statusCode = " + val);
                laboModule.setReportStatusCode(val);
                
                // statusCodeId���擾����
                val = child.getAttributeValue("statusCodeId", ns);
                logger.debug("statusCodeId = " + val);
                laboModule.setReportStatusCodeId(val);
                
            } else if (qname.equals("mmlLb:facility")) {
                // �N���C�A���g�{�ݏ����p�[�X����
                //logger.debug("facility�@���p�[�X��");
                
                // �{�݂��擾����
                val = child.getTextTrim();
                logger.debug("facility = " + val);
                laboModule.setClientFacility(val);
                
                // �{�݃R�[�h�������擾����
                val = child.getAttributeValue("facilityCode", ns);
                logger.debug("facilityCode = " + val);
                laboModule.setClientFacilityCode(val);
                
                // �{�݃R�[�h�̌n��o�^����
                val = child.getAttributeValue("facilityCodeId", ns);
                logger.debug("facilityCodeId = " + val);
                laboModule.setClientFacilityCodeId(val);
                
            } else if (qname.equals("mmlLb:laboratoryCenter")) {
                // ���{�Z���^�[�����p�[�X����
                //logger.debug("laboratoryCenter�@���p�[�X��");
                
                // ���{�Z���^�[���擾����
                val = child.getTextTrim();
                logger.debug("laboratoryCenter = " + val);
                laboModule.setLaboratoryCenter(val);
                
                // ���{�R�[�h���擾����
                val = child.getAttributeValue("centerCode", ns);
                logger.debug("centerCode = " + val);
                laboModule.setLaboratoryCenterCode(val);
                
                // ���{�R�[�h�̌n���擾����
                val = child.getAttributeValue("centerCodeId", ns);
                logger.debug("centerCodeId = " + val);
                laboModule.setLaboratoryCenterCodeId(val);
                
            } else if (qname.equals("mmlLb:laboTest")) {
                // labotest�v�f���p�[�X����
                //logger.debug("labotest�@���p�[�X��");
                
            } else if (qname.equals("mmlLb:specimen")) {
                // ���̏����p�[�X����
                //logger.debug("specimen�@���p�[�X��");
                laboSpecimen = new LaboSpecimenValue();
                laboModule.addLaboSpecimen(laboSpecimen);
                laboSpecimen.setLaboModule(laboModule);	// �֌W��ݒ肷��
                
            } else if (qname.equals("mmlLb:specimenName")) {
                // ���̖����擾����
                val = child.getTextTrim();
                logger.debug("specimenName = " + val);
                laboSpecimen.setSpecimenName(val);
                
                // spCode���擾����
                val = child.getAttributeValue("spCode", ns);
                logger.debug("spCode = " + val);
                laboSpecimen.setSpecimenCode(val);
                
                // spCodeId���擾����
                val = child.getAttributeValue("spCodeId", ns);
                logger.debug("spCodeId = " + val);
                laboSpecimen.setSpecimenCodeId(val);
                
            } else if (qname.equals("mmlLb:item")) {
                // �������ڂ��p�[�X����
                //logger.debug("item�@���p�[�X��");
                laboItem = new LaboItemValue();
                //laboItem.setId(GUIDGenerator.generate(laboItem)); // EJB3.0�ŕύX
                laboSpecimen.addLaboItem(laboItem);
                laboItem.setLaboSpecimen(laboSpecimen);	// �֌W��ݒ肷��
                
            } else if (qname.equals("mmlLb:itemName")) {
                // �������ږ����p�[�X����
                //logger.debug("itemName�@���p�[�X��");
                
                // �������ږ����擾����
                val = child.getTextTrim();
                logger.debug("itemName = " + val);
                laboItem.setItemName(val);
                
                // ���ڃR�[�h���擾����
                val = child.getAttributeValue("itCode", ns);
                logger.debug("itCode = " + val);
                laboItem.setItemCode(val);
                
                // ���ڃR�[�h�̌n���擾����
                val = child.getAttributeValue("itCodeId", ns);
                logger.debug("itCodeId = " + val);
                laboItem.setItemCodeId(val);
                
            } else if (qname.equals("mmlLb:value")) {
                // �����l���p�[�X����
                //logger.debug("value�@���p�[�X��");
                
                // �l���擾����
                val = child.getTextTrim();
                logger.debug("value = " + val);
                laboItem.setItemValue(val);
                
            } else if (qname.equals("mmlLb:numValue")) {
                // ���l�v�f���p�[�X����
                //logger.debug("value�@���p�[�X��");
                
                // �l���擾����
                val = child.getTextTrim();
                logger.debug("numValue = " + val);
                // TODO laboItem.setValue()***************************
                
                // up
                val = child.getAttributeValue("up", ns);
                logger.debug("up = " + val);
                laboItem.setUp(val);
                
                // low
                val = child.getAttributeValue("low", ns);
                logger.debug("low = " + val);
                laboItem.setLow(val);
                
                // normal
                val = child.getAttributeValue("normal", ns);
                logger.debug("low = " + val);
                laboItem.setNormal(val);
                
                // out
                val = child.getAttributeValue("out", ns);
                logger.debug("out = " + val);
                laboItem.setNout(val);
                
            } else if (qname.equals("mmlLb:unit")) {
                // �P�ʏ����擾����
                //logger.debug("unit�@���p�[�X��");
                
                // value
                val = child.getTextTrim();
                logger.debug("unit = " + val);
                laboItem.setUnit(val);
                
                // uCode
                val = child.getAttributeValue("uCode", ns);
                logger.debug("uCode = " + val);
                laboItem.setUnitCode(val);
                
                // uCodeId
                val = child.getAttributeValue("uCodeId", ns);
                logger.debug("uCodeId = " + val);
                laboItem.setUnitCodeId(val);
            }
            
            parseTestModule(child);
        }
    }
    
    private void debug(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }
}
