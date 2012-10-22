package open.dolphin.plugin.helper;

import java.net.URL;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.MenuListener;

import open.dolphin.client.ClientContext;

/**
 * MenuBar and ToolBar builder.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class MenuBarBuilder {
    
    static final String[] ACCELERATOR_STRING = {
        "VK_E", "VK_M", "VK_N", "VK_O", "VK_S", "VK_P", "VK_W", "VK_Q", "VK_Z", "VK_Y", "VK_X", "VK_C", "VK_V","VK_B","VK_I","VK_U","VK_+","VK_-",
    };
    
    static final int[] ACCELERATOR_INT = {
        KeyEvent.VK_E, KeyEvent.VK_M, KeyEvent.VK_N, KeyEvent.VK_O, KeyEvent.VK_S, KeyEvent.VK_P, KeyEvent.VK_W, KeyEvent.VK_Q,
        KeyEvent.VK_Z, KeyEvent.VK_Y, KeyEvent.VK_X, KeyEvent.VK_C, KeyEvent.VK_V,
        KeyEvent.VK_B, KeyEvent.VK_I, KeyEvent.VK_U,
        KeyEvent.VK_PLUS,KeyEvent.VK_MINUS,
    };
    
    static final String WINDOW_MEMU_NAME = "�E�C���h�E";
    
    MenuBarDirector director;
    Hashtable actions;
    Hashtable btnGroups;
    Hashtable menuTargets;
    ActionListener actionListener;
    
    /** Products of this builder */
    JMenuBar menuBar;
    boolean hasWindowMenu;
    JPanel toolPanel;
    int addPos = 0;
    LinkedList menuList;
    LinkedList toolList;
    boolean hasToolBar;
    boolean subMenuToolBar;
    
    boolean DEBUG = false;
    
    /**
     * Creates new MenuBarBuilder
     */
    public MenuBarBuilder() {
        director = new MenuBarDirector(this);
    }
    
    public void setMenuBar(JMenuBar menuBar) {
        this.menuBar = menuBar;
        hasWindowMenu = true;
    }
    
    public void setActionListener(ActionListener l) {
        actionListener = l;
    }
    
    public void setTargets(Hashtable t) {
        menuTargets = t;
    }
    
    public void setActions(Hashtable table) {
        actions = table;
    }
    
    public void build(URL url) {
        director.build(url);
    }
    
    /**
     * Returns JMenuBar product
     */
    public JMenuBar getJMenuBar() {
        return menuBar;
    }
    
    /**
     * Returns ToolBar panel product
     */
    public JPanel getToolPanel() {
        return toolPanel;
    }
    
    /**
     * ��n�����s���B
     */
    public void close() {
        if (menuList != null) {
            menuList.clear();
        }
        if (toolList != null) {
            toolList.clear();
        }
    }
    
    /**
     * MenuBar �𐶐�����
     */
    public void buildMenuBar() {
        // �������郁�j���[�o�[���w�肳��Ă��Ȃ��ꍇ
        if (menuBar == null) {
            menuBar = new JMenuBar();
            hasWindowMenu = false;
        }
        menuList = new LinkedList();
        btnGroups = new Hashtable(2, 1.0f);
        toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolList = new LinkedList();
    }
    
    /**
     * Menu �𐶐��� MenuBar/���݂�Menu �֒ǉ�����
     */
    @SuppressWarnings("unchecked")
    public void buildMenu(String name, String text, String mnemonic) {
        
        // ���O���E�C���h�E���j���[�� hasWindowMenu �� true �̎�
        // �}���|�W�V�������C���N�������g���ă��^�[������
        if (text.equals(WINDOW_MEMU_NAME) && hasWindowMenu) {
            addPos++;
            return;
        }
        
        // Menu�𐶐�����
        JMenu menu = new JMenu(text);
        hasToolBar = false;
        subMenuToolBar = false;
        
        // ���̃��j���[�� MenuListener ��ݒ肷��
        if (name != null) {
            // MenuTarget table �ɑ��݂��邩�H
            Object target = (Object) menuTargets.get(name + "_listener");
            if (target != null) {
                menu.addMenuListener((MenuListener) target);
            }
        }
        
        // mnemonic ������ΐݒ肷��
        if (mnemonic != null) {
            menu.setMnemonic(mnemonic.charAt(0));
        }
        
        // MenuBar �֒ǉ�����
        if (addPos <  menuBar.getMenuCount()) {
            menuBar.add(menu, addPos++);
        } else {
            menuBar.add(menu);
        }
        
        // ���̃��j���[�����݂̃��j���[�ɂ���
        menuList.addFirst(menu);
    }
    
    /**
     * �T�u���j���[�𐶐�����B
     */
    @SuppressWarnings("unchecked")
    public void buildSubMenu(String name, String text, String enabled) {
        
        JMenu subMenu = null;
        boolean enable = enabled != null ? Boolean.valueOf(enabled).booleanValue() : false;
        
        // ActionTable�ɑ��݂��邩
        Action subMenuAction = (Action) actions.get(name);
        if (subMenuAction != null) {
            subMenu = new JMenu(subMenuAction);
            subMenuAction.setEnabled(enable);
        } else {
            subMenu = new JMenu(text);
            subMenu.setEnabled(enable);
        }
        // ���̃��j���[�� MenuListener ��ݒ肷��
        if (name != null) {
            // MenuTarget table �ɑ��݂��邩�H
            Object target = (Object) menuTargets.get(name + "_listener");
            if (target != null) {
                subMenu.addMenuListener((MenuListener) target);
            }
        }
        
        // �T�u���j���[��ǉ�����
        ((JMenu)menuList.getFirst()).add(subMenu);
        
        // ���̃��j���[�����݂̃��j���[�ɂ���
        menuList.addFirst(subMenu);
    }
    
    /**
     * Action �𐶐��� Menu, ToolBar �֒ǉ�����
     */
    @SuppressWarnings("unchecked")
    public void buildActionItem(String name,	// �����K���ŏ������邽�߂̖��O
            String type,			// Item �̃^�C�v
            String group,			// RadioBytton type �̏ꍇ�̃O���[�v��
            String text,			// MenuItem �̃e�L�X�g
            String iconSpec,                    // ToolBar �A�C�R��
            String accelerator,                 // Accelerator
            String shiftMask,                   // Shif_Mask
            String toolTip,			// ToolTip �e�L�X�g
            String enabled) {
        
        // name ���L�[�ɂ��� Action ���e�[�u��������o��
        Action theAction = (Action) actions.get(name);
        
        // global �łȂ��ꍇ�͐�������
        if (theAction == null) {
            // ChainAction �𐶐�����
            // MenuSupport�� cahin �̔��Ό�
            // method �����L�[�ɂ��ăA�N�V�����e�[�u���֐��������A�N�V������ۑ�����
            // �A�N�V�����e�[�u���̓N���C���g�Ŏg�p�����
            theAction = new ChainAction(text, (MenuSupport) menuTargets.get("chain"), name);
            actions.put(name, theAction);
        }
        
        // RadioButton MenuItem �̎�
        if (type != null && type.equals("radio")) {
            
            ButtonGroup bg = (ButtonGroup)btnGroups.get(group);
            if (bg == null) {
                bg = new ButtonGroup();
                btnGroups.put(group, bg);
            }
            JRadioButtonMenuItem rdItem = new JRadioButtonMenuItem(theAction);
            theAction.putValue("menuItem",rdItem);
            getCurrentMenu().add(rdItem);
            bg.add(rdItem);
            setAcceleEnabled(rdItem, theAction, accelerator, shiftMask, enabled);
            
        } else if (type != null && type.equals("checkBox")) {
            
            JCheckBoxMenuItem cbItem = new JCheckBoxMenuItem(theAction);
            theAction.putValue("menuItem",cbItem);
            getCurrentMenu().add(cbItem);
            setAcceleEnabled(cbItem, theAction, accelerator, shiftMask, enabled);
        }
        
        // Tool Icon ���Ȃ��ꍇ
        else if (iconSpec == null) {
            
            // current �̃��j���[�����o���AMenuItem �Ƃ��Ēǉ�����
            JMenu menu = getCurrentMenu();
            JMenuItem menuItem = menu.add(theAction);
            menuItem.setText(text);
            menuItem.setIcon(null);
            theAction.putValue("menuItem", menuItem);
            setAcceleEnabled(menuItem, theAction, accelerator, shiftMask, enabled);
            
        } else {
            // Tool Icon �����郁�j���[����
            ImageIcon icon = ClientContext.getImageIcon(iconSpec);
            
            if (!hasToolBar) {
                JToolBar toolBar = new JToolBar();
                toolBar.setMargin(new Insets(5,5,5,5));
                toolPanel.add(toolBar);
                toolList.addFirst(toolBar);
                hasToolBar = true;
            }
            
            // current �̃��j���[�ƃc�[���o�[�����o���ǉ�����
            JMenu menu = getCurrentMenu();
            JToolBar toolBar = getCurrentToolBar();
            
            JMenuItem menuItem = menu.add(theAction);
            menuItem.setText(text);
            menuItem.setIcon(null);
            theAction.putValue("menuItem", menuItem);
            
            JButton btn = toolBar.add(theAction);
            btn.setText("");
            btn.setIcon(icon);
            btn.setToolTipText(toolTip);
            theAction.putValue("toolButton", btn);
            
            setAcceleEnabled(menuItem, theAction, accelerator, shiftMask, enabled);
        }
    }
    
    /**
     * MenuItem �𐶐��� Menu �֒ǉ�����
     */
    public void buildMenuItem(String name, String text, String accelerator, String shiftMask, String enabled) {
        
        JMenu menu = getCurrentMenu();
        
        if (text.equals("-")) {
            menu.addSeparator();
            
        } else {
            
            JMenuItem menuItem = new JMenuItem(text);
            menu.add(menuItem);
            menuItem.addActionListener(actionListener); // ActionListener
            setAcceleEnabled(menuItem, accelerator, shiftMask, enabled);
        }
    }
    
    /**
     * StyledEditorAction �𐶐���Menu�֒ǉ�����
     */
    @SuppressWarnings("unchecked")
    public void buildStyleItem(String name,	// �����K���ŏ������邽�߂̖��O
            String type,			// Item �̃^�C�v
            String group,			// RadioBytton type �̏ꍇ�̃O���[�v��
            String text,			// MenuItem �̃e�L�X�g
            String iconSpec,			// ToolBar �A�C�R��
            String accelerator,                 // Accelerator
            String shiftMask,                   // Shif_Mask
            String toolTip,			// ToolTip �e�L�X�g
            String enabled) {
        
        // name ���L�[�ɂ��� Action �����o��
        Action theAction = (Action)actions.get(name);
        
        if (theAction == null) {
            return;
        }
        
        // RadioButton MenuItem �̎�
        if (type != null && type.equals("radio")) {
            
            ButtonGroup bg = (ButtonGroup)btnGroups.get(group);
            if (bg == null) {
                bg = new ButtonGroup();
                btnGroups.put(group, bg);
            }
            JRadioButtonMenuItem rdItem = new JRadioButtonMenuItem(theAction);
            rdItem.setText(text);
            theAction.putValue("menuItem",rdItem);
            getCurrentMenu().add(rdItem);
            bg.add(rdItem);
            setAcceleEnabled(rdItem, theAction, accelerator, shiftMask, enabled);
            
        } else if (type != null && type.equals("checkBox")) {
            
            JCheckBoxMenuItem cbItem = new JCheckBoxMenuItem(theAction);
            cbItem.setText(text);
            theAction.putValue("menuItem",cbItem);
            getCurrentMenu().add(cbItem);
            setAcceleEnabled(cbItem, theAction, accelerator, shiftMask, enabled);
        }
        
        // Tool Icon ���Ȃ��ꍇ
        else if (iconSpec == null) {
            
            // current �̃��j���[�����o���AMenuItem �Ƃ��Ēǉ�����
            JMenu menu = getCurrentMenu();
            JMenuItem menuItem = menu.add(theAction);
            menuItem.setText(text);
            menuItem.setIcon(null);
            theAction.putValue("menuItem", menuItem);
            setAcceleEnabled(menuItem, theAction, accelerator, shiftMask, enabled);
            
        } else {
            
            ImageIcon icon = ClientContext.getImageIcon(iconSpec);
            
            // current �̃��j���[�ƃc�[���o�[�����o���ǉ�����
            JMenu menu = getCurrentMenu();
            JToolBar toolBar = getCurrentToolBar();
            
            JMenuItem menuItem = menu.add(theAction);
            menuItem.setText(text);
            menuItem.setIcon(null);
            theAction.putValue("menuItem", menuItem);
            
            JButton btn = toolBar.add(theAction);
            btn.setText("");
            btn.setIcon(icon);
            btn.setToolTipText(toolTip);
            theAction.putValue("toolButton",btn);
            
            setAcceleEnabled(menuItem, theAction, accelerator, shiftMask, enabled);
        }
    }
    
    JMenu getCurrentMenu() {
        return (JMenu)menuList.getFirst();
    }
    
    JToolBar getCurrentToolBar() {
        return (JToolBar)toolList.getFirst();
    }
    
    void setAcceleEnabled(JMenuItem menuItem, Action action, String accelerator, String shiftMask, String enabled) {
        
        if (accelerator != null) {
            int i = acceleStringToInt(accelerator);
            if (shiftMask != null) {
                menuItem.setAccelerator(
                        KeyStroke.getKeyStroke(i, (java.awt.event.InputEvent.SHIFT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
                
            } else {
                menuItem.setAccelerator(KeyStroke.getKeyStroke(i,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            }
        }
        
        boolean enable = enabled != null ? Boolean.valueOf(enabled).booleanValue() : false;
        action.setEnabled(enable);
    }
    
    void setAcceleEnabled(JMenuItem menuItem, String accelerator, String shiftMask, String enabled) {
        
        if (accelerator != null) {
            int i = acceleStringToInt(accelerator);
            if (shiftMask != null) {
                menuItem.setAccelerator(
                        KeyStroke.getKeyStroke(i, (java.awt.event.InputEvent.SHIFT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
                
            } else {
                menuItem.setAccelerator(KeyStroke.getKeyStroke(i,Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            }
        }
        boolean enable = enabled != null ? Boolean.valueOf(enabled).booleanValue() : false;
        menuItem.setEnabled(enable);
    }
    
    public void buildEnd(int cmpType) {
        
        if (cmpType == MenuBarDirector.CMP_MENU || cmpType == MenuBarDirector.CMP_SUB_MENU) {
            if (menuList.size() > 0) {
                menuList.removeFirst();
            }
            if (hasToolBar && toolList.size() > 0) {
                toolList.removeFirst();
            }
        }
    }
    
    void debugString(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        }
    }
    
    int acceleStringToInt(String str) {
        
        int ret = 0;
        
        for (int i = 0; i < ACCELERATOR_STRING.length; i++) {
            
            if (ACCELERATOR_STRING[i].equals(str)) {
                ret = ACCELERATOR_INT[i];
                break;
            }
        }
        
        return ret;
    }
}