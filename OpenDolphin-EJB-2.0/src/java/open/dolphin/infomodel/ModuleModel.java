package open.dolphin.infomodel;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;


/**
 * ModuleModel
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
@Entity
@Table(name = "d_module")
public class ModuleModel extends KarteEntryBean 
        implements Stamp, java.io.Serializable, java.lang.Cloneable {
    
    @Embedded
    private ModuleInfoBean moduleInfo;
    
    @Transient
    private IInfoModel model;
    
    @Lob
    @Column(nullable=false)
    private byte[] beanBytes;
    
    @ManyToOne
    @JoinColumn(name="doc_id", nullable=false)
    private DocumentModel document;
    
    /**
     * ModuleModel�I�u�W�F�N�g�𐶐�����B
     */
    public ModuleModel() {
        moduleInfo = new ModuleInfoBean();
    }
    
    public DocumentModel getDocumentModel() {
        return document;
    }
    
    public void setDocumentModel(DocumentModel document) {
        this.document = document;
    }
    
    /**
     * ���W���[������ݒ肷��B
     * @param moduleInfo ���W���[�����
     */
    public void setModuleInfoBean(ModuleInfoBean moduleInfo) {
        this.moduleInfo = moduleInfo;
    }
    
    /**
     * ���W���[������Ԃ��B
     * @return ���W���[�����
     */
    public ModuleInfoBean getModuleInfoBean() {
        return moduleInfo;
    }
    
    /**
     * ���W���[���̏�񃂃f���i���̂�POJO)��ݒ肷��B
     * @param model ���f��
     */
    public void setModel(IInfoModel model) {
        this.model = model;
    }
    
    /**
     * ���W���[���̏�񃂃f���i���̂�POJO)��Ԃ��B
     * @return ���f��
     */
    public IInfoModel getModel() {
        return model;
    }
    
    /**
     * ���W���[���̉i�����o�C�g�z���Ԃ��B
     * @return ���W���[���̉i�����o�C�g�z��
     */
    public byte[] getBeanBytes() {
        return beanBytes;
    }
    
    /**
     * ���W���[���̉i�����o�C�g�z���ݒ肷��B
     * @param beanBytes ���W���[���̉i�����o�C�g�z��
     */
    public void setBeanBytes(byte[] beanBytes) {
        this.beanBytes = beanBytes;
    }
    
    /**
     * �h�L�������g�Ɍ���鏇�ԂŔ�r����B
     * @return ��r�l
     */
    @Override
    public int compareTo(Object other) {
        if (other != null && getClass() == other.getClass()) {
            ModuleInfoBean moduleInfo1 = getModuleInfoBean();
            ModuleInfoBean moduleInfo2 = ((ModuleModel)other).getModuleInfoBean();
            return moduleInfo1.compareTo(moduleInfo2);
        }
        return -1;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        ModuleModel ret = new ModuleModel();
        ret.setConfirmed(this.getConfirmed());
        ret.setEnded(this.getEnded());
        ret.setFirstConfirmed(this.getConfirmed());
        ret.setLinkId(this.getLinkId());
        ret.setLinkRelation(this.getLinkRelation());
        ret.setModuleInfoBean((ModuleInfoBean)this.getModuleInfoBean().clone());
        ret.setRecorded(this.getRecorded());
        ret.setStarted(this.getStarted());
        ret.setStatus(this.getStatus());

        byte[] bytes = this.getBeanBytes();
        if (bytes!=null) {
            byte[] dest = new byte[bytes.length];
            System.arraycopy(bytes, 0, dest, 0, bytes.length);
            ret.setBeanBytes(dest);
        }

        if (model!=null) {
            if (model instanceof BundleDolphin) {
                BundleDolphin m = (BundleDolphin)model;
                ret.setModel((BundleDolphin)m.clone());
            } else if (model instanceof BundleMed) {
                BundleMed m = (BundleMed)model;
                ret.setModel((BundleMed)m.clone());
            } else if (model instanceof ProgressCourse) {
                ProgressCourse m = (ProgressCourse)model;
                ret.setModel((ProgressCourse)m.clone());
            } else {
                throw new CloneNotSupportedException();
            }
        }

        // ���L�͗��p���ōĐݒ肷��
        //ret.setKarteBean(this.getKarteBean());
        //ret.setUserModel(this.getUserModel());
        //ret.setDocumentModel(this.getDocumentModel());

        return ret;
    }
}
