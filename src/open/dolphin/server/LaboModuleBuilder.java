/*
 * LaboModuleBuilder.java
 * Copyright (C) 2004 Digital Globe, Inc. All rights reserved.
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
package open.dolphin.server;

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

import open.dolphin.client.ClientContext;
import open.dolphin.delegater.LaboDelegater;
import open.dolphin.infomodel.LaboImportSummary;
import open.dolphin.infomodel.LaboItemValue;
import open.dolphin.infomodel.LaboModuleValue;
import open.dolphin.infomodel.LaboSpecimenValue;
import open.dolphin.infomodel.ModelUtils;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.project.Project;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 * LaboModuleBuilder
 *
 * @author Kazushi Minagawa
 */
public class LaboModuleBuilder {
    
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
        
        // �q�v�f��񋓂���
        List children = header.getChildren();
        Iterator iterator = children.iterator();
        
        while (iterator.hasNext()) {
            
            Element child = (Element) iterator.next();
            String qname = child.getQualifiedName();
            String ename = child.getName();
            Namespace ns = child.getNamespace();
            debug(child.toString());
            
            // MasterId���擾������ID�𓾂�
            if (ename.equals("masterId")) {
                logger.debug("masterId�@���p�[�X��");
                masterId = true;
                
            } else if (masterId && qname.equals("mmlCm:Id")) {
                
                // patientId
                patientId = child.getTextTrim();
                logger.debug("patientId = " + patientId);
                
                // type
                patientIdType = child.getAttributeValue("type", ns);
                logger.debug("type = " + patientIdType);
                
                // tableId
                patientIdTypeTableId = child.getAttributeValue("tableId", ns);
                logger.debug("tableId = " + patientIdTypeTableId);
            }
            // �ċA����
            parseHeader(child);
        }
    }
    
    /**
     * MML Body���p�[�X����B
     * ModuleItem��DocInfo�� uuid, confirmdate���擾����B
     * @param body Body�v�f
     */
    private void parseBody(Element body) {
        
        // �q����񋓂���
        List children = body.getChildren();
        Iterator iterator = children.iterator();
        
        while (iterator.hasNext()) {
            
            Element child = (Element) iterator.next();
            //String qname = child.getQualifiedName();
            String ename = child.getName();
            //Namespace ns = child.getNamespace();
            debug(child.toString());
            
            if (ename.equals("MmlModuleItem")) {
                logger.debug("MmlModuleItem�@���p�[�X��");
                
            } else if (ename.equals("docInfo")) {
                String val = child.getAttributeValue("contentModuleType");
                logger.debug("contentModuleType = " + val);
                
            } else if (ename.equals("docId")) {
                
            } else if (ename.equals("uid")) {
                // ������UUID���擾����
                moduleUUID = child.getTextTrim();
                logger.debug("uid = " + moduleUUID);
                
            } else if (ename.equals("confirmDate")) {
                // �m������擾���� ModuleIte-DocInfo
                confirmDate = child.getTextTrim();
                logger.debug("confirmDate = " + confirmDate);
                
            } else if (ename.equals("content")) {
                // content�v�f���p�[�X����
                parseContent(child);
            }
            
            parseBody(child);
        }
    }
    
    /**
     * Content�v�f���p�[�X����B
     * �N���C�A���g���A���{�Z���^�[���A���̏��A�������ڏ����擾����B
     * @param content �������ʂ�����R���e���g�v�f
     */
    private void parseContent(Element content) {
        
        // �R���e���g�v�f�̎q��񋓂���
        List children = content.getChildren();
        Iterator iterator = children.iterator();
        
        while (iterator.hasNext()) {
            
            Element child = (Element) iterator.next();
            //String ename = child.getName();
            String qname = child.getQualifiedName();
            Namespace ns = child.getNamespace();
            debug(child.toString());
            String val = null;
            
            if (qname.equals("mmlLb:TestModule")) {
                // ��͂��郂�W���[����mmlLb:TestModule
                logger.debug("TestModule�@���p�[�X��");
                
                // LaboModuleValue�I�u�W�F�N�g�𐶐�����
                // ���̃I�u�W�F�N�g�̑�����MML�̓��e���ݒ肳���
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
                
            } else if (qname.equals("mmlLb:information")) {
                // mmlLb:information�v�f���p�[�X����
                logger.debug("infomation�@���p�[�X��");
                
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
                logger.info("reportStatus�@���p�[�X��");
                
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
                logger.debug("facility�@���p�[�X��");
                
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
                logger.debug("laboratoryCenter�@���p�[�X��");
                
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
                
            } else if (qname.equals("mmlLb:labotest")) {
                // labotest�v�f���p�[�X����
                logger.debug("labotest�@���p�[�X��");
                
            } else if (qname.equals("mmlLb:specimen")) {
                // ���̏����p�[�X����
                logger.debug("specimen�@���p�[�X��");
                laboSpecimen = new LaboSpecimenValue();
                //laboSpecimen.setId(GUIDGenerator.generate(laboSpecimen)); // EJB3.0�ŕύX
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
                logger.debug("item�@���p�[�X��");
                laboItem = new LaboItemValue();
                //laboItem.setId(GUIDGenerator.generate(laboItem)); // EJB3.0�ŕύX
                laboSpecimen.addLaboItem(laboItem);
                laboItem.setLaboSpecimen(laboSpecimen);	// �֌W��ݒ肷��
                
            } else if (qname.equals("mmlLb:itemName")) {
                // �������ږ����p�[�X����
                logger.debug("itemName�@���p�[�X��");
                
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
                logger.debug("value�@���p�[�X��");
                
                // �l���擾����
                val = child.getTextTrim();
                logger.debug("value = " + val);
                laboItem.setItemValue(val);
                
            } else if (qname.equals("mmlLb:numValue")) {
                // ���l�v�f���p�[�X����
                logger.debug("value�@���p�[�X��");
                
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
                logger.debug("unit�@���p�[�X��");
                
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
            
            parseContent(child);
        }
    }
    
    private void debug(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }
}
