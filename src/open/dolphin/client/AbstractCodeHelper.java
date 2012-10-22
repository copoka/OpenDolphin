package open.dolphin.client;

import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import open.dolphin.infomodel.ModuleInfoBean;

/**
 * KartePane �̒��ۃR�[�h�w���p�[�N���X�B
 *
 * @author Kazyshi Minagawa
 */
public abstract class AbstractCodeHelper {
    
    /** �L�[���[�h�̋��E�ƂȂ镶�� */
    static final String[] WORD_SEPARATOR = {" ", " ", "�A", "�B", "\n", "\t"};
    
    static final String LISTENER_METHOD = "importStamp";
    
    static final Icon icon = ClientContext.getImageIcon("foldr_16.gif");
    
    /** �Ώۂ� KartePane */
    KartePane kartePane;
    
    /** KartePane �� JTextPane */
    JTextPane textPane;
    
    /** �⊮���X�g���j���[ */
    JPopupMenu popup;
    
    /** �L�[���[�h�p�^�[�� */
    Pattern pattern;
    
    /** �L�[���[�h�̊J�n�ʒu */
    int start;
    
    /** �L�[���[�h�̏I���ʒu */
    int end;
    
    /** ChartMediator */
    ChartMediator mediator;
    
    /** �C���L�[ */
    int MODIFIER;
    
    
    /** 
     * Creates a new instance of CodeHelper 
     */
    public AbstractCodeHelper(KartePane kartePane, ChartMediator mediator) {
        
        this.kartePane = kartePane;
        this.mediator = mediator;
        this.textPane = kartePane.getTextPane();
        
        Preferences prefs = Preferences.userNodeForPackage(AbstractCodeHelper.class);
        String modifier = prefs.get("modifier", "ctrl");
        
        if (modifier.equals("ctrl")) {
            MODIFIER =  KeyEvent.CTRL_DOWN_MASK;
        } else if (modifier.equals("meta")) {
            MODIFIER =  KeyEvent.META_DOWN_MASK;
        }

        this.textPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.getModifiersEx() == MODIFIER) && e.getKeyCode() == KeyEvent.VK_SPACE) {
                    buildAndShowPopup();
                }
            }
        });
    }
    
    protected abstract void buildPopup(String text);
    
    protected void buildEntityPopup(String entity) {
        
        //
        // ������ entity�ɑΉ����� StampTree ���擾����
        //
        StampBoxPlugin stampBox = mediator.getStampBox();
        StampTree tree = stampBox.getStampTree(entity);
        if (tree == null) {
            return;
        }
        
        popup = new JPopupMenu();
        
        Hashtable<Object, Object> ht = new Hashtable<Object, Object>(5, 0.75f);
        
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
        ht.put(rootNode, popup);
        
        Enumeration e = rootNode.preorderEnumeration();
        
        if (e != null) {
            
            e.nextElement(); // consume root
            
            while (e.hasMoreElements()) {
                
                StampTreeNode node = (StampTreeNode) e.nextElement();
                
                if (!node.isLeaf()) {
                    
                    JMenu subMenu = new JMenu(node.getUserObject().toString());
                    if (node.getParent() == rootNode) {
                        JPopupMenu parent = (JPopupMenu) ht.get(node.getParent());
                        parent.add(subMenu);
                        ht.put(node, subMenu);
                    } else {
                        JMenu parent = (JMenu) ht.get(node.getParent());
                        parent.add(subMenu);
                        ht.put(node, subMenu);   
                    }
                    
            
                    // �z���̎q��S�ė񋓂�JmenuItem�ɂ܂Ƃ߂�
                    JMenuItem item = new JMenuItem(node.getUserObject().toString());
                    item.setIcon(icon);
                    subMenu.add(item);
                    
                    addActionListner(item, node);
                
                } else if (node.isLeaf()) {
                    
                    ModuleInfoBean info = (ModuleInfoBean) node.getUserObject();
                    String stampName = info.getStampName();
                     
                    JMenuItem item = new JMenuItem(stampName);
                    addActionListner(item, node);
                    
                    if (node.getParent() == rootNode) {
                        JPopupMenu parent = (JPopupMenu) ht.get(node.getParent());
                        parent.add(item);
                    } else {
                        JMenu parent = (JMenu) ht.get(node.getParent());
                        parent.add(item);
                    }
                }
            }
        }
    }
    
    protected void addActionListner(JMenuItem item, StampTreeNode node) {
        
        ReflectActionListener ral = new ReflectActionListener(this, LISTENER_METHOD, 
                            new Class[]{JComponent.class, TransferHandler.class, LocalStampTreeNodeTransferable.class},
                            new Object[]{textPane, textPane.getTransferHandler(), new LocalStampTreeNodeTransferable(node)});
        
        item.addActionListener(ral);
    }

    protected void showPopup() {
        
        if (popup == null || popup.getComponentCount() < 1) {
            return;
        }
        
        try {
            int pos = textPane.getCaretPosition();
            Rectangle r = textPane.modelToView(pos);
            popup.show (textPane, r.x, r.y);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void importStamp(JComponent comp, TransferHandler handler, LocalStampTreeNodeTransferable tr) {
        textPane.setSelectionStart(start);
        textPane.setSelectionEnd(end);
        textPane.replaceSelection("");
        handler.importData(comp, tr);
        closePopup();
    }
    
    protected void closePopup() {
        if (popup != null) {
            popup.removeAll();
            popup = null;
        }
    }

    /**
     * �P��̋��E����L�����b�g�̈ʒu�܂ł̃e�L�X�g���擾���A
     * �������[���ȏ�ł�Ε⊮���j���[���|�b�v�A�b�v����B
     */
    protected void buildAndShowPopup() {

        end = textPane.getCaretPosition();
        start = end;
        boolean found = false;

        while (start > 0) {
            
            start--;
  
            try {
                String text = textPane.getText(start, 1);
                for (String test : WORD_SEPARATOR) {
                    if (test.equals(text)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    start++;
                    break;
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            
            String str = textPane.getText(start, end - start);
            
            if (str.length() > 0) {
                buildPopup(str);
                showPopup();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
