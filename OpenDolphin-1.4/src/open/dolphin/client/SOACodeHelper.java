package open.dolphin.client;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleInfoBean;

/**
 * SOA�y�C���̃R�[�h�w���p�[�N���X�B
 *
 * @author Kazyshi Minagawa
 */
public class SOACodeHelper extends AbstractCodeHelper {
    
    /**
     * Creates a new instance of CodeHelper
     */
    public SOACodeHelper(KartePane pPane, ChartMediator mediator) {
        super(pPane, mediator);
    }
    
    protected void buildPopup(String text) {
        
        Preferences prefs = Preferences.userNodeForPackage(AbstractCodeHelper.class);
        
        if (prefs.get(IInfoModel.ENTITY_TEXT, "tx").startsWith(text.toLowerCase())) {
            buildEntityPopup(IInfoModel.ENTITY_TEXT);
            
        } else {
            buildMatchPopup(text);
        }
    }
    
    protected void buildMatchPopup(String text) {
        
        StampBoxPlugin stampBox = mediator.getStampBox();
        StampTree tree = stampBox.getStampTree(IInfoModel.ENTITY_TEXT);
        if (tree == null) {
            return;
        }
        
        popup = new JPopupMenu();
        
        //
        // ���j���[�̃X�^�b�N�𐶐�����
        //
        LinkedList menus = new LinkedList();
        menus.addFirst(popup);
        
        //
        // �e�m�[�h�̃X�^�b�N�𐶐�����
        //
        LinkedList parents = new LinkedList();
        
        //
        // Stamp �̖��O���L�[���[�h�Ŏn�܂�A���ꂪ�P�ȏ゠����̂�⊮���j���[�ɉ�����
        //
        pattern = Pattern.compile("^" + text + ".*");
        
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
        
        Enumeration e = rootNode.preorderEnumeration();
        
        if (e != null) {
            
            e.nextElement(); // consume root
            
            while (e.hasMoreElements()) {
                
                //
                // �����Ώۂ̃m�[�h�𓾂�
                //
                StampTreeNode node = (StampTreeNode) e.nextElement();
                
                //
                // ���̐e�𓾂�
                //
                StampTreeNode parent = (StampTreeNode) node.getParent();
                
                //
                // �e�����X�g�Ɋ܂܂�Ă��邩�ǂ���
                //
                int index = parents.indexOf(parent);
                if (index > -1) {
                    //
                    // �����̐e���C���f�b�N�X=0�ɂȂ�܂Ń|�b�v����
                    //
                    for (int i = 0; i < index; i++) {
                        parents.removeFirst();
                        menus.removeFirst();
                    }
                    
                    if (!node.isLeaf()) {
                        //
                        // �t�H���_�̏ꍇ
                        //
                        String folderName = node.getUserObject().toString();
                        JMenu subMenu = new JMenu(folderName);
                        if (menus.getFirst() instanceof JPopupMenu) {
                            ((JPopupMenu) menus.getFirst()).add(subMenu);
                        } else {
                            ((JMenu) menus.getFirst()).add(subMenu);
                        }
                        menus.addFirst(subMenu);
                        parents.addFirst(node);
                        JMenuItem item = new JMenuItem(folderName);
                        item.setIcon(icon);
                        subMenu.add(item);
                        addActionListner(item, node);
                        
                    } else {
                        ModuleInfoBean info = (ModuleInfoBean) node.getUserObject();
                        String completion = info.getStampName();
                        JMenuItem item = new JMenuItem(completion);
                        addActionListner(item, node);
                        if (menus.getFirst() instanceof JPopupMenu) {
                            ((JPopupMenu) menus.getFirst()).add(item);
                        } else {
                            ((JMenu) menus.getFirst()).add(item);
                        }
                    }
                    
                } else {
                    //
                    // �܂܂�Ă��Ȃ��̂Ń}�b�`�������K�v
                    //
                    if (!node.isLeaf()) {
                        //
                        // �t�H���_�̏ꍇ
                        //
                        String completion = node.getUserObject().toString();
                        Matcher matcher = pattern.matcher(completion);
                        if (matcher.matches()) {
                            //
                            // �}�b�`�����ꍇ�̓J�����g���j���[�։�����
                            // �������J�����g���j���[�ɂȂ�
                            // �e���X�g�Ɏ�����������
                            String folderName = node.getUserObject().toString();
                            JMenu subMenu = new JMenu(folderName);
                            if (menus.getFirst() instanceof JPopupMenu) {
                                ((JPopupMenu) menus.getFirst()).add(subMenu);
                            } else {
                                ((JMenu) menus.getFirst()).add(subMenu);
                            }
                            menus.addFirst(subMenu);
                            parents.addFirst(node);
                            
                            //
                            // �t�H���_�I���̃A�C�e���𐶐����T�u���j���[�̗v�f�ɂ���
                            //
                            JMenuItem item = new JMenuItem(folderName);
                            item.setIcon(icon);
                            subMenu.add(item);
                            addActionListner(item, node);
                        }
                        
                    } else {
                        //
                        // �t�̏ꍇ
                        //
                        ModuleInfoBean info = (ModuleInfoBean) node.getUserObject();
                        String completion = info.getStampName();
                        Matcher matcher = pattern.matcher(completion);
                        
                        if (matcher.matches()) {
                            //
                            // ��v�����ꍇ
                            //
                            JMenuItem item = new JMenuItem(completion);
                            addActionListner(item, node);
                            if (menus.getFirst() instanceof JPopupMenu) {
                                ((JPopupMenu) menus.getFirst()).add(item);
                            } else {
                                ((JMenu) menus.getFirst()).add(item);
                            }
                        }
                    }
                }
            }
        }
    }
}
