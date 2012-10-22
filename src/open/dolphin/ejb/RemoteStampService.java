package open.dolphin.ejb;

import java.util.List;

import open.dolphin.infomodel.IStampTreeModel;
import open.dolphin.infomodel.PublishedTreeModel;
import open.dolphin.infomodel.StampModel;
import open.dolphin.infomodel.StampTreeModel;
import open.dolphin.infomodel.SubscribedTreeModel;

/**
 * RemoteStampService
 *
 * @author Minagawa,Kazushi
 */
public interface RemoteStampService {
    
    /**
     * �l�p��StampTree��ۑ�����B
     * @param model �l�p��StampTreeModel
     * @return db pk
     */
    public long putTree(StampTreeModel model);
    
    /**
     * User �̃X�^���v�c���[���f�[�^�x�[�X���猟�����ĕԂ��B
     * @param userPk ���O�C�����[�U�� PK
     * @return StampTree ��`�̃��X�g�i�l�p�A���L�p�A���J����Ă�����́j
     */
    public List<IStampTreeModel> getTrees(long userPk);
    
    public long saveAndPublishTree(StampTreeModel model, byte[] publishBytes);
    
    public int publishTree(StampTreeModel model, byte[] publishBytes);
    
    public int updatePublishedTree(StampTreeModel model, byte[] publishBytes);
    
    public int cancelPublishedTree(StampTreeModel model);
    
    public List<PublishedTreeModel> getPublishedTrees();
    
    public List<Long> subscribeTrees(List<SubscribedTreeModel> addList);
    
    public int unsubscribeTrees(List<SubscribedTreeModel> removeList);
    
    public StampTreeModel getAspTree(String managerId);
    
    public List<String> putStamp(List<StampModel> list);
    
    public String putStamp(StampModel model);
    
    public StampModel getStamp(String stampId);
    
    public List<StampModel> getStamp(List<String> ids);
    
    public int removeStamp(String stampId);
    
    public int removeStamp(List<String> ids);
    
}
