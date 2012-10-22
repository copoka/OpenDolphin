package open.dolphin.helper;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * Window Menu ���T�|�[�g���邽�߂̃N���X�B
 * Factory method �� WindowMenu ������ JFrame �𐶐�����B
 *
 * @author Minagawa,Kazushi
 */
public class WindowSupport implements MenuListener {
    
    private static ArrayList<WindowSupport> allWindows = new ArrayList<WindowSupport>(5);
    
    private static final String WINDOW_MWNU_NAME = "�E�C���h�E";
    
    // Window support ���񋟂���X�^�b�t
    // �t���[��
    private JFrame frame;
    
    // ���j���[�o�[
    private JMenuBar menuBar;
    
    // �E�C���h�E���j���[
    private JMenu windowMenu;
    
    // Window Action
    private Action windowAction;
    
    /**
     * WindowSupport�𐶐�����B
     * @param title �t���[���^�C�g��
     * @return WindowSupport
     */
    public static WindowSupport create(String title) {
        
        // �t���[���𐶐�����
        final JFrame frame = new JFrame(title);
        
        // ���j���[�o�[�𐶐�����
        JMenuBar menuBar = new JMenuBar();
        
        // Window ���j���[�𐶐�����
        JMenu windowMenu = new JMenu(WINDOW_MWNU_NAME);
        
        // ���j���[�o�[��Window ���j���[��ǉ�����
        menuBar.add(windowMenu);
        
        // �t���[���Ƀ��j���[�o�[��ݒ肷��
        frame.setJMenuBar(menuBar);
        
        // Window���j���[�̃A�N�V����
        // �I�����ꂽ��t���[����S�ʂɂ���
        Action windowAction = new AbstractAction(title) {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.toFront();
            }
        };
        
        // �C���X�^���X�𐶐�����
        final WindowSupport ret
                = new WindowSupport(frame, menuBar, windowMenu, windowAction);
        
        // WindowEvent �����̃N���X�ɒʒm�����X�g�̊Ǘ����s��
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                WindowSupport.windowOpened(ret);
            }
            
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                WindowSupport.windowClosed(ret);
            }
        });
        
        // windowMenu �Ƀ��j���[���X�i��ݒ肵���̃N���X�ŏ���������
        windowMenu.addMenuListener(ret);
        return ret;
    }
    
    public static ArrayList getAllWindows() {
        return allWindows;
    }
    
    public static void windowOpened(WindowSupport opened) {
        // ���X�g�ɒǉ�����
        allWindows.add(opened);
    }
    
    public static void windowClosed(WindowSupport closed) {
        // ���X�g����폜����
        allWindows.remove(closed);
        closed = null;
    }
    
    public static boolean contains(WindowSupport toCheck) {
        return allWindows.contains(toCheck);
    }
    
    // �v���C�x�[�g�R���X�g���N�^
    private WindowSupport(JFrame frame, JMenuBar menuBar, JMenu windowMenu,
            Action windowAction) {
        this.frame = frame;
        this.menuBar = menuBar;
        this.windowMenu = windowMenu;
        this.windowAction = windowAction;
    }
    
    public JFrame getFrame() {
        return frame;
    }
    
    public JMenuBar getMenuBar() {
        return menuBar;
    }
    
    public JMenu getWindowMenu() {
        return windowMenu;
    }
    
    public Action getWindowAction() {
        return windowAction;
    }
    
    /**
     * �E�C���h�E���j���[���I�����ꂽ�ꍇ�A���݃I�[�v�����Ă���E�C���h�E�̃��X�g���g�p���A
     * ������I�����邽�߂� MenuItem ��ǉ�����B
     */
    @Override
    public void menuSelected(MenuEvent e) {
        
        // �S�ă����[�u����
        JMenu wm = (JMenu) e.getSource();
        wm.removeAll();
        
        // ���X�g����V�K�ɐ�������
        for (WindowSupport ws : allWindows) {
            Action action = ws.getWindowAction();
            wm.add(action);
        }
    }
    
    @Override
    public void menuDeselected(MenuEvent e) {
    }
    
    @Override
    public void menuCanceled(MenuEvent e) {
    }
}
