package open.dolphin.client;

import java.util.*;

import org.apache.log4j.Logger;

import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleInfoBean;

/**
 * StampTree Builder �N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class DefaultStampTreeBuilder extends AbstractStampTreeBuilder {
    
    /** XML�����Œu�����K�v�ȕ��� */
    private static final String[] REPLACES = new String[] { "<", ">", "&", "'" ,"\""};
    
    /** �u������ */
    private static final String[] MATCHES = new String[] { "&lt;", "&gt;", "&amp;", "&apos;", "&quot;" };
    
    /** �G�f�B�^���甭�s�̃X�^���v�� */
    private static final String FROM_EDITOR = "�G�f�B�^���甭�s...";
    
    /** root�m�[�h�̖��O */
    private String rootName;
    
    /** �G�f�B�^���甭�s�����������ǂ����̃t���O */
    private boolean hasEditor;
    
    /** StampTree �̃��[�g�m�[�h*/
    private StampTreeNode rootNode;
    
    /** StampTree �̃m�[�h*/
    private StampTreeNode node;
    
    /** �m�[�h�� UserObject �ɂȂ� StampInfo */
    private ModuleInfoBean info;
    
    /** ����p�̃��X�g */
    private LinkedList<StampTreeNode> linkedList;
    
    /** ������ */
    private List<StampTree> products;
    
    /** Logger */
    private Logger logger;  // = ClientContext.getLogger("boot");
    
    /** 
     * Creates new DefaultStampTreeBuilder 
     */
    public DefaultStampTreeBuilder() {
    }
    
    /**
     * Returns the product of this builder
     * @return vector that contains StampTree instances
     */
    public List<StampTree> getProduct() {
        return products;
    }
    
    /**
     * build ���J�n����B
     */
    public void buildStart() {
        products = new ArrayList<StampTree>();
        if (logger != null) {
            logger.debug("Build StampTree start");
        }
    }
    
    /**
     * Root �𐶐�����B
     * @param name root��
     * @param Stamptree �� Entity
     */
    public void buildRoot(String name, String entity) {
        
        if (logger != null) {
            logger.debug("Root=" + name);
        }
        linkedList = new LinkedList<StampTreeNode>();
        
        //
        // TreeInfo �� ������ rootNode �ɕۑ�����
        //
        TreeInfo treeInfo = new TreeInfo();
        treeInfo.setName(name);
        treeInfo.setEntity(entity);
        rootNode = new StampTreeNode(treeInfo);
        
        hasEditor = false;
        rootName = name;
        linkedList.addFirst(rootNode);
    }
    
    /**
     * �m�[�h�𐶐�����B
     * @param name �m�[�h��
     */
    public void buildNode(String name) {
        
        if (logger != null) {
            logger.debug("Node=" + name);
        }
        
        //
        // Node �𐶐������݂̃m�[�h�ɉ�����
        //
        node = new StampTreeNode(toXmlText(name));
        getCurrentNode().add(node);
        
        //
        // ���̃m�[�h�� first �ɉ�����
        //
        linkedList.addFirst(node);
    }
    
    /**
     * StampInfo �� UserObject �ɂ���m�[�h�𐶐�����B
     * @param name �m�[�h��
     * @param entity �G���e�B�e�B
     * @param editable �ҏW�\���ǂ����̃t���O
     * @param memo ����
     * @param id DB key
     */
    public void buildStampInfo(String name,
            String role,
            String entity,
            String editable,
            String memo,
            String id) {
        
        if (logger != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            sb.append(",");
            sb.append(role);
            sb.append(",");
            sb.append(entity);
            sb.append(",");
            sb.append(editable);
            sb.append(",");
            sb.append(memo);
            sb.append(",");
            sb.append(id);
            logger.debug(sb.toString());
        }
        
        //
        // StampInfo �𐶐�����
        //
        info = new ModuleInfoBean();
        info.setStampName(toXmlText(name));
        info.setStampRole(role);
        info.setEntity(entity);
        if (editable != null) {
            info.setEditable(Boolean.valueOf(editable).booleanValue());
        }
        if (memo != null) {
            info.setStampMemo(toXmlText(memo));
        }
        if ( id != null ) {
            info.setStampId(id);
        }
        
        //
        // StampInfo ���� TreeNode �𐶐������݂̃m�[�h�֒ǉ�����
        //
        node = new StampTreeNode(info);
        getCurrentNode().add(node);
        
        //
        // �G�f�B�^���甭�s�������Ă��邩
        //
        if (info.getStampName().equals(FROM_EDITOR) && (! info.isSerialized()) ) {
            hasEditor = true;
            info.setEditable(false);
        }
    }
    
    /**
     * Node �̐������I������B
     */
    public void buildNodeEnd() {
        if (logger != null) {
            logger.debug("End node");
        }
        linkedList.removeFirst();
    }
    
    /**
     * Root Node �̐������I������B 
     */
    public void buildRootEnd() {
        
        //
        // �G�f�B�^���甭�s...���폜���ꂽ�ꍇ�ɒǉ����鏈�u
        //
        if ( (!hasEditor) && (getEntity(rootName) != null) ) {
            
            if	( getEntity(rootName).equals(IInfoModel.ENTITY_TEXT) || getEntity(rootName).equals(IInfoModel.ENTITY_PATH)) {
                //
                // �e�L�X�g�X�^���v�ƃp�X�X�^���v�ɂ̓G�f�B�^���甭�s...�͂Ȃ�
                //
            } else {
                ModuleInfoBean si = new ModuleInfoBean();
                si.setStampName(FROM_EDITOR);
                si.setStampRole(IInfoModel.ROLE_P);
                si.setEntity(getEntity(rootName));
                si.setEditable(false);
                StampTreeNode sn = new StampTreeNode(si);
                rootNode.add(sn);
            }
        }
        
        //
        // StampTree �𐶐����v���_�N�g���X�g�։�����
        //
        StampTree tree = new StampTree(new StampTreeModel(rootNode));
        products.add(tree);
        
        if (logger != null) {
            int pCount = products.size();
            logger.debug("End root " + "count=" + pCount);
        }
    }
    
    /**
     * build ���I������B
     */
    public void buildEnd() {
        
        if (logger != null) {
            logger.debug("Build end");
        }
        
        //
        // ORCA�Z�b�g��������
        //
        boolean hasOrca = false;
        for (StampTree st : products) {
            String entity = st.getTreeInfo().getEntity();
            if (entity.equals(IInfoModel.ENTITY_ORCA)) {
                hasOrca = true;
            }
        }
        
        if (!hasOrca) {
            TreeInfo treeInfo = new TreeInfo();
            treeInfo.setName(IInfoModel.TABNAME_ORCA);
            treeInfo.setEntity(IInfoModel.ENTITY_ORCA);
            rootNode = new StampTreeNode(treeInfo);
            OrcaTree tree = new OrcaTree(new StampTreeModel(rootNode));
            products.add(IInfoModel.TAB_INDEX_ORCA, tree);
            if (logger != null) {
                logger.debug("ORCA�Z�b�g�������܂���");
            }
        }
    }
    
    /**
     * ���X�g����擪�� StampTreeNode �����o���B
     */
    private StampTreeNode getCurrentNode() {
        return (StampTreeNode) linkedList.getFirst();
    }
    
        
    /**
     * ���ꕶ����ϊ�����B
     */
    private String toXmlText(String text) {
        for (int i = 0; i < REPLACES.length; i++) {
            text = text.replaceAll(MATCHES[i], REPLACES[i]);
        }
        return text;
    }
}