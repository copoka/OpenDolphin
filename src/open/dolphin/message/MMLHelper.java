/*
 * MMLBuilder.java
 *
 * Created on 2007/01/04, 11:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package open.dolphin.message;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import open.dolphin.infomodel.AccessRightModel;
import open.dolphin.infomodel.BundleDolphin;
import open.dolphin.infomodel.BundleMed;
import open.dolphin.infomodel.ClaimItem;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModelUtils;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.ProgressCourse;
import open.dolphin.infomodel.SchemaModel;
import open.dolphin.infomodel.UserModel;
import open.dolphin.project.Project;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * MMLBuilder
 *
 * @author Minagawa,Kazushi
 */
public class MMLHelper {
    
    private DocumentModel document;
    private UserModel user;
    private String patientId;
    
    private StringBuilder freeExp;
    private StringBuilder paragraphBuilder;
    private String soaSpec;
    private String pSpec;  
    private List<ModuleModel> pModules;
    
    private List<SchemaModel> schemas;
    
    private List<AccessRightModel> accessRights;
    
    private boolean DEBUG = false;
    
    /** Creates a new instance of MMLBuilder */
    public MMLHelper() {
    }
    
    public DocumentModel getDocument() {
        return document;
    }

    public void setDocument(DocumentModel document) {
        
        this.document = document;
    }
    
    /**
     * �o�ߋL�^���W���[���̎��R�L�ڕ\���𐶐�����B
     */
    public void buildText() {
        
        // Module�𒊏o����
        Collection<ModuleModel> moduleBeans = getDocument().getModules();
        pModules = new ArrayList<ModuleModel>();
        
        for (ModuleModel module : moduleBeans) {
            
            String role = module.getModuleInfo().getStampRole();
            
            if (role.equals(IInfoModel.ROLE_SOA_SPEC)) {
                soaSpec = ((ProgressCourse) module.getModel()).getFreeText();
                
            } else if (role.equals(IInfoModel.ROLE_P)) {
                pModules.add(module);
                
            } else if (role.equals(IInfoModel.ROLE_P_SPEC)) {
                pSpec = ((ProgressCourse) module.getModel()).getFreeText();
            }
        }
        
        // Schema�𒊏o����
        Collection<SchemaModel> schemaC = getDocument().getSchema();
        if (schemaC != null && schemaC.size() > 0) {
            schemas = new ArrayList<SchemaModel>(schemaC.size());
            schemas.addAll(schemaC);
        }
        
        // �A�N�Z�X���𒊏o����
        Collection<AccessRightModel> arc = getDocument().getDocInfo().getAccessRights();
        if (arc != null && arc.size() > 0) {
            accessRights = new ArrayList<AccessRightModel>(arc.size());
            accessRights.addAll(arc);
        }
        
        // Builder�𐶐��� soa�y��p�h�L�������g���p�[�X����
        freeExp = new StringBuilder();
        
        if (soaSpec != null) {
            parse(soaSpec);
        }
        
        if (pSpec != null) {
            parse(pSpec);
        }
    }
    
    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }
    
    /**
     * ����ID��Ԃ��B
     * @return ����ID
     */
    public String getPatientId() {
        return patientId;
    }
    
    public void setPatientId(String patientId) {
        this.patientId = patientId; 
    }
    
    /**
     * �n��A�g�p�̊���ID��Ԃ��B
     * �������[��  �{�ݓ���ID�ł��邱�Ƃ������B
     * <mmlCm:Id mmlCm:type="facility" mmlCm:tableId="JPN452015100001">12345</mmlCm:Id> 
     */
    public String getCNPatientId() {
        return patientId;
    }
    
    /**
     * �n��A�g�p�̊���IDType��Ԃ��B
     * �������[�� facility
     */
    public String getCNPatientIdType() {
        return "facility";
    }
    
    /**
     * �n��A�g�p�̊���ID TableId��Ԃ��B
     * �������[�� ���̎{�݂�JMARI�R�[�h
     */
    public String getCNPatientIdTableId() {
        return getCNFacilityId();
    }
    
    /**
     * �n��A�g�Ŏg�p����{�ݖ���Ԃ��B
     * @return �{�ݖ�
     */
    public String getCNFacilityName() {
        return getUser().getFacilityModel().getFacilityName();
    }
    
    /**
     * �n��A�g�p�̎{��ID��Ԃ��B
     * �������[�� JMARI�R�[�h��K�p����
     * <mmlCm:Id mmlCm:type="JMARI" mmlCm:tableId="MML0027">JPN452015100001</mmlCm:Id> 
     */
    public String getCNFacilityId() {
        // TODO 
        if (Project.getJoinAreaNetwork()) {
            return Project.getAreaNetworkFacilityId();
        }
        return getUser().getFacilityModel().getFacilityId();
    }
    
    /**
     * �n��A�g�p�̎{��ID Type��Ԃ��B
     * �������[�� JMARI
     */
    public String getCNFacilityIdType() {
        return "JMARI";
    }
    
    /**
     * �n��A�g�p�̎{��ID tableId��Ԃ��B
     * �������[�� MML0027
     */
    public String getCNFacilityIdTableId() {
        return "MML0027";
    }
    
    /**
     * �n��A�g�p��CreatorId��Ԃ��B
     * �������[�� 
     * <mmlCm:Id mmlCm:type="local" mmlCm:tableId="MML0024">12345</mmlCm:Id>
     */
    public String getCNCreatorId() {
        if (Project.getJoinAreaNetwork()) {
            return Project.getAreaNetworkCreatorId();
        }
        return getUser().getUserId();
    }
    
    /**
     * �n��A�g�p��CreatorId Type��Ԃ��B
     * �������[�� local
     */
    public String getCNCreatorIdType() {
        return "local";
    }   
    
    /**
     * �n��A�g�p��CreatorId TableId��Ԃ��B
     * �������[�� MML0024
     */
    public String getCNCreatorIdTableId() {
        return "MML0024";
    }  
    
    
    public String getCreatorName() {
        return getUser().getCommonName();
    }
    
    public String getCreatorLicense() {
        return getUser().getLicenseModel().getLicense();
    }
    
    public String getPurpose() {
        return getDocument().getDocInfo().getPurpose();
    }
    
    public String getTitle() {
        return getDocument().getDocInfo().getTitle();
    }
    
    public String getDocId() {
        return getDocument().getDocInfo().getDocId();
    }
    
    public String getParentId() {
        return getDocument().getDocInfo().getParentId();
    }
    
    public String getParentIdRelation() {
        return getDocument().getDocInfo().getParentIdRelation();
    }
    
    public String getGroupId() {
        return getDocument().getDocInfo().getDocId();
    }
    
    public String getConfirmDate() {
        return ModelUtils.getDateTimeAsString(getDocument().getDocInfo().getConfirmDate());
    }
    
    public String getFirstConfirmDate() {
        return ModelUtils.getDateTimeAsString(getDocument().getDocInfo().getFirstConfirmDate());
    }
    
    public List<SchemaModel> getSchema() {
        return schemas;
    }
    
    public List<AccessRightModel> getAccessRights() {
        return accessRights;
    }
    
    /**
     * �o�ߋL�^���W���[���̎��R�L�ڕ\����Ԃ��B
     */
    public String getFreeExpression() {
        String ret = freeExp.toString();
        debug(ret);
        
        return ret;
    }
    
    /**
     * soaSpec �y�� pSpec���p�[�X�� xhtml �̎��R�L�ڕ\���ɕϊ�����B
     */
    private void parse(String spec) {
        
        try {
            BufferedReader reader = new BufferedReader(new StringReader(spec));
            SAXBuilder docBuilder = new SAXBuilder();
            Document doc = docBuilder.build(reader);
            Element root = doc.getRootElement();
            debug(root.toString());
            parseChildren(root);
            reader.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * �q�v�f���ċA�I�Ƀp�[�X����B
     */
    private void parseChildren(Element current) {
        
        List children = current.getChildren();
        
        // Leaf �Ȃ烊�^�[������
        if (children == null || children.size() == 0) {
            return;
        }
        
        for (Iterator iterator = children.iterator(); iterator.hasNext();) {
            
            Element child = (Element) iterator.next();
            //String qname = child.getQualifiedName();
            String ename = child.getName();
            //Namespace ns = child.getNamespace();
            //debug(ename);
            
            if (ename.equals("paragraph")) {
                // �i���P�ʂ�<xhtml:br/>������
                // ���̒i���p�Ƀr���_��V���ɐ�������
                if (paragraphBuilder != null) {
                    freeExp.append(paragraphBuilder.toString());
                    freeExp.append("<xhtml:br/>\n");
                }
                paragraphBuilder = new StringBuilder();
                
            
            } else if (ename.equals("content")) {
                // �擾������̂Ȃ�
            
            } else if (ename.equals("component")) {
                
                String name = child.getAttributeValue("name");
                int number = Integer.parseInt(child.getAttributeValue("component"));
                
                if (name.equals("schemaHolder")) {
                    // Schema �̏ꍇ��extRef�ɕϊ�����
                    paragraphBuilder.append(getSchemaInfo(schemas.get(number)));
                    
                } else if (name.equals("stampHolder")) {
                    // �I�[�_�̏ꍇ��<br>��toString()
                    paragraphBuilder.append(getStampInfo(pModules.get(number)));
                }
                
                
            } else if (ename.equals("text")) {
                // �Ӗ������邩���m��Ȃ��̂�trim()���Ȃ�
                //paragraphBuilder.append(child.getTextTrim());
                paragraphBuilder.append(child.getText());
            }
            
            // �ċA����
            parseChildren(child);
        }
    }
    
    /**
     * Schema �� extRef Info ��Ԃ��B
     */
    private String getSchemaInfo(SchemaModel schema) {
        String contentType = schema.getExtRef().getContentType();
        String medicalRole = schema.getExtRef().getMedicalRole();
        String title = schema.getExtRef().getTitle();
        String href = schema.getExtRef().getHref();
        StringBuilder sb = new StringBuilder();
        sb.append("<mmlCm:extRef");
        sb.append(" mmlCm:contentType=");
        sb.append(addQuote(contentType));
        sb.append(" mmlCm:medicalRole=");
        sb.append(addQuote(medicalRole));
        sb.append(" mmlCm:title=");
        sb.append(addQuote(title));
        sb.append(" mmlCm:href=");
        sb.append(addQuote(href));
        sb.append(" />");
        return sb.toString();
    }
    
    /**
     * �X�^���v�̕�����\����Ԃ��B
     */
    private String getStampInfo(ModuleModel module) {
        
        IInfoModel obj = module.getModel();
        StringBuilder buf = new StringBuilder();
        
        if (obj instanceof BundleMed) {
            
            BundleMed med = (BundleMed) obj;
            
            buf.append("RP<xhtml:br/>\n");
        
            ClaimItem[] items = med.getClaimItem();
           
            for (ClaimItem item : items) {
                
                buf.append("�E");
                buf.append(item.getName());
                buf.append("�@");

                if (item.getNumber() != null) {
                    buf.append(item.getNumber());
                    if (item.getUnit() != null) {
                        buf.append(item.getUnit());
                    }
                }
                buf.append("<xhtml:br/>\n");
            }

            if (med.getAdmin().startsWith("����")) {
                buf.append(med.getAdmin().substring(0,2));
                buf.append(" ");
                buf.append(med.getAdmin().substring(4));
            } else {
                buf.append(med.getAdmin());
            }
            buf.append(" x ");
            buf.append(med.getBundleNumber());
            // FIXME
            if (med.getAdmin().startsWith("����")) {
                if (med.getAdmin().charAt(3) == '��') {
                    buf.append(" ����");
                }
            }
            buf.append("<xhtml:br/>\n");

            // Print admMemo
            if (med.getAdminMemo() != null) {
                buf.append(med.getAdminMemo());
                buf.append("<xhtml:br/>\n");
            }

            // Print admMemo
            if (med.getMemo() != null) {
                buf.append(med.getMemo());
                buf.append("<xhtml:br/>\n");
            }

        }
        
        else if (obj instanceof BundleDolphin) {
           
            BundleDolphin bundle = (BundleDolphin) obj;

            // Print order name
            buf.append(bundle.getOrderName());
            buf.append("<xhtml:br/>\n");
            ClaimItem[] items = bundle.getClaimItem();

            for (ClaimItem item : items) {

                // Print item name
                buf.append("�E");
                buf.append(item.getName());

                // Print item number
                String number = item.getNumber();
                if (number != null) {
                    buf.append("�@");
                    buf.append(number);
                    if (item.getUnit() != null) {
                        buf.append(item.getUnit());
                    }
                }
                buf.append("<xhtml:br/>\n");
            }

            // Print bundleNumber
            if (! bundle.getBundleNumber().equals("1")) {
                buf.append("X�@");
                buf.append(bundle.getBundleNumber());
                buf.append("<xhtml:br/>\n");
            }

            // Print admMemo
            if (bundle.getAdminMemo() != null) {
                buf.append(bundle.getAdminMemo());
                buf.append("<xhtml:br/>\n");
            }

            // Print bundleMemo
            if (bundle.getMemo() != null) {
                buf.append(bundle.getMemo());
                buf.append("<xhtml:br/>\n");
            }
        } 
        
        return buf.toString();
        
    }
    
    String addQuote(String str) {
        StringBuilder buf = new StringBuilder();
        buf.append("\"");
        buf.append(str);
        buf.append("\"");
        return buf.toString();
    }
    
    private void debug(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }
}
