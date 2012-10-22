package open.dolphin.infomodel;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * DocumentBean
 *
 * @author Minagawa,Kazushi
 *
 */
@Entity
@Table(name = "d_document")
public class DocumentModel extends KarteEntryBean {
    
    private static final long serialVersionUID = 8273677751373923433L;
    
    @Embedded
    private DocInfoModel docInfo;
    
    @OneToMany(mappedBy="document", cascade={CascadeType.PERSIST, CascadeType.REMOVE})
    private Collection<ModuleModel> modules;
    
    @OneToMany(mappedBy="document", cascade={CascadeType.PERSIST, CascadeType.REMOVE})
    //@Column(name="c_schema")
    private Collection<SchemaModel> schema;

    
    /**
     * DocumentModel�𐶐�����B
     */
    public DocumentModel() {
        setDocInfo(new DocInfoModel());
        getDocInfo().setDocType(DOCTYPE_KARTE);
    }
    
    public void toDetuch() {
        docInfo.setDocPk(getId());
        docInfo.setParentPk(getLinkId());
        docInfo.setConfirmDate(getConfirmed());
        docInfo.setFirstConfirmDate(getStarted());
        docInfo.setStatus(getStatus());
    }
    
    public void toPersist() {
        setLinkId(docInfo.getParentPk());
        setLinkRelation(docInfo.getParentIdRelation());
        setConfirmed(docInfo.getConfirmDate());
        setFirstConfirmed(docInfo.getFirstConfirmDate());
        setStatus(docInfo.getStatus());
    }
    
    /**
     * ��������Ԃ��B
     * @return �������
     */
    public DocInfoModel getDocInfo() {
        return docInfo;
    }
    
    /**
     * ��������ݒ肷��B
     * @param docInfo �������
     */
    public void setDocInfo(DocInfoModel docInfo) {
        this.docInfo = docInfo;
    }
    
    /**
     * �V�F�[�}��Ԃ��B
     * @return �V�F�[�}
     */
    public Collection<SchemaModel> getSchema() {
        return schema;
    }
    
    /**
     * �V�F�[�}��ݒ肷��B
     * @param images �V�F�[�}
     */
    public void setSchema(Collection<SchemaModel> images) {
        this.schema = images;
    }
    
    /**
     * �V�F�[�}��ǉ�����B
     * @param model �V�F�[�}
     */
    public void addSchema(SchemaModel model) {
        if (this.schema == null) {
            this.schema = new ArrayList<SchemaModel>();
        }
        this.schema.add(model);
    }
    
    /**
     * �V�F�[�}�R���N�V�������N���A����B
     */
    public void clearSchema() {
        if (schema != null && schema.size() > 0) {
            schema.clear();
        }
    }
    
    
    public SchemaModel getSchema(int index) {
        if (schema != null && schema.size() > 0) {
            int cnt = 0;
            for (SchemaModel bean : schema) {
                if (index == cnt) {
                    return bean;
                }
                cnt++;
            }
        }
        return null;
    }
    
    /**
     * ���W���[����Ԃ��B
     * @return ���W���[��
     */
    public Collection<ModuleModel> getModules() {
        return modules;
    }
    
    /**
     * ���W���[����ݒ肷��B
     * @param modules ���W���[��
     */
    public void setModules(Collection<ModuleModel> modules) {
        this.modules = modules;
    }
    
    /**
     * ���W���[�����f���̔z���ǉ�����B
     * @param moules ���W���[�����f���̔z��
     */
    public void addModule(ModuleModel[] addArray) {
        if (modules == null) {
            modules = new ArrayList<ModuleModel>(addArray.length);
        }
        for (ModuleModel bean : addArray) {
            modules.add(bean);
        }
    }
    
    /**
     * ���W���[�����f����ǉ�����B
     * @param value ���W���[�����f��
     */
    public void addModule(ModuleModel addModule) {
        if (modules == null) {
            modules = new ArrayList<ModuleModel>();
        }
        modules.add(addModule);
    }
    
    /**
     * ���W���[�����N���A����B
     */
    public void clearModules() {
        if (modules != null && modules.size() > 0) {
            modules.clear();
        }
    }
    
    /**
     * �����̃G���e�B�e�B�������W���[�����f����Ԃ��B
     * @param entityName �G���e�B�e�B�̖��O
     * @return �Y�����郂�W���[�����f��
     */
    public ModuleModel getModule(String entityName) {
        
        if (modules != null) {
            
            ModuleModel ret = null;
            
            for (ModuleModel model : modules) {
                if (model.getModuleInfo().getEntity().equals(entityName)) {
                    ret = model;
                    break;
                }
            }
            return ret;
        }
        
        return null;
    }
    
    /**
     * �����̃G���e�B�e�B���������W���[������Ԃ��B
     * @param entityName �G���e�B�e�B�̖��O
     * @return ���W���[�����
     */
    public ModuleInfoBean[] getModuleInfo(String entityName) {
        
        if (modules != null) {
            
            ArrayList<ModuleInfoBean> list = new ArrayList<ModuleInfoBean>(2);
            
            for (ModuleModel model : modules) {
                
                if (model.getModuleInfo().getEntity().equals(entityName)) {
                    list.add(model.getModuleInfo());
                }
            }
            
            if (list.size() > 0) {
                return  (ModuleInfoBean[])list.toArray(new ModuleInfoBean[list.size()]);
            }
        }
        
        return null;
    }    
}
