package open.dolphin.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.text.Position;

import open.dolphin.infomodel.BundleDolphin;
import open.dolphin.infomodel.ClaimItem;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.ModuleModel;

import open.dolphin.project.Project;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;

/**
 * KartePane �� Component�@�Ƃ��đ}�������X�^���v��ێ��X���N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class StampHolder extends AbstractComponentHolder implements ComponentHolder{
    
    private static final char[] MATCHIES = {'�O','�P','�Q','�R','�S','�T','�U','�V','�W','�X','�@','��','��'};
    private static final char[] REPLACES = {'0','1','2','3','4','5','6','7','8','9',' ','m','g'};
    private static final Color FOREGROUND = new Color(20, 20, 140);
    private static final Color BACKGROUND = Color.white;
    private static final Color SELECTED_BORDER = new Color(255, 0, 153);
    
    private ModuleModel stamp;
    private StampRenderingHints hints;
    private KartePane kartePane;
    private Position start;
    private Position end;
    private boolean selected;
    
    private Color foreGround = FOREGROUND;
    private Color background = BACKGROUND;
    private Color selectedBorder = SELECTED_BORDER;
    
    /** Creates new StampHolder2 */
    public StampHolder(KartePane kartePane, ModuleModel stamp) {
        super();
        this.kartePane = kartePane;
        setHints(new StampRenderingHints());
        setForeground(foreGround);
        setBackground(background);
        setBorder(BorderFactory.createLineBorder(kartePane.getTextPane().getBackground()));
        setStamp(stamp);
    }
    
    /**
     * Focus���ꂽ�ꍇ�̃��j���[����ƃ{�[�_�[��\������B
     */
    public void enter(ActionMap map) {
        
        map.get(GUIConst.ACTION_COPY).setEnabled(true);
        
        if (kartePane.getTextPane().isEditable()) {
            map.get(GUIConst.ACTION_CUT).setEnabled(true);
        } else {
            map.get(GUIConst.ACTION_CUT).setEnabled(false);
        }
        
        map.get(GUIConst.ACTION_PASTE).setEnabled(false);
        
        setSelected(true);
    }
    
    /**
     * Focus���͂��ꂽ�ꍇ�̃��j���[����ƃ{�[�_�[�̔�\�����s���B
     */
    public void exit(ActionMap map) {
        setSelected(false);
    }
    
    /**
     * Popup���j���[��\������B
     */
    public void mabeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            JPopupMenu popup = new JPopupMenu();
            ChartMediator mediator = kartePane.getMediator();
            popup.add(mediator.getAction(GUIConst.ACTION_CUT));
            popup.add(mediator.getAction(GUIConst.ACTION_COPY));
            popup.add(mediator.getAction(GUIConst.ACTION_PASTE));
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
    
    public Component getComponent() {
        return this;
    }
    
    /**
     * ���̃X�^���v�z���_��KartePane��Ԃ��B
     */
    public KartePane getKartePane() {
        return kartePane;
    }
    
    /**
     * �X�^���v�z���_�̃R���e���g�^�C�v��Ԃ��B
     */
    public int getContentType() {
        return ComponentHolder.TT_STAMP;
    }
    
    /**
     * ���̃z���_�̃��f����Ԃ��B
     * @return
     */
    public ModuleModel getStamp() {
        return stamp;
    }
    
    /**
     * ���̃z���_�̃��f����ݒ肷��B
     * @param stamp
     */
    public void setStamp(ModuleModel stamp) {
        this.stamp = stamp;
        setMyText();
    }
    
    public StampRenderingHints getHints() {
        return hints;
    }
    
    public void setHints(StampRenderingHints hints) {
        this.hints = hints;
    }
    
    /**
     * �I������Ă��邩�ǂ�����Ԃ��B
     * @return �I������Ă��鎞 true
     */
    public boolean isSelected() {
        return selected;
    }
    
    /**
     * �I�𑮐���ݒ肷��B
     * @param selected �I���̎� true
     */
    public void setSelected(boolean selected) {
        boolean old = this.selected;
        this.selected = selected;
        if (old != this.selected) {
            if (this.selected) {
                this.setBorder(BorderFactory.createLineBorder(selectedBorder));
            } else {
                this.setBorder(BorderFactory.createLineBorder(kartePane.getTextPane().getBackground()));
            }
        }
    }
    
    /**
     * KartePane �ł��̃X�^���v���_�u���N���b�N���ꂽ���R�[�������B
     * StampEditor ���J���Ă��̃X�^���v��ҏW����B
     */
    public void edit() {
        
        if (kartePane.getTextPane().isEditable()) {
            String category = stamp.getModuleInfo().getEntity();
            StampEditorDialog stampEditor = new StampEditorDialog(category,stamp);
            stampEditor.addPropertyChangeListener(StampEditorDialog.VALUE_PROP, this);
            stampEditor.start();
            
        } else {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
    }
    
    /**
     * �G�f�B�^�ŕҏW�����l���󂯎����e��\������B
     */
    public void propertyChange(PropertyChangeEvent e) {
        
        ModuleModel newStamp = (ModuleModel) e.getNewValue();
        
        if (newStamp != null) {
            // �X�^���v��u��������
            importStamp(newStamp);
        }
    }
    
    /**
     * �X�^���v�̓��e��u��������B
     * @param newStamp
     */
    public void importStamp(ModuleModel newStamp) {
        setStamp(newStamp);
        kartePane.setDirty(true);
        kartePane.getTextPane().validate();
        kartePane.getTextPane().repaint();
    }
    
    /**
     * TextPane���ł̊J�n�ƏI���|�W�V������ۑ�����B
     */
    public void setEntry(Position start, Position end) {
        this.start = start;
        this.end = end;
    }
    
    /**
     * �J�n�|�W�V������Ԃ��B
     */
    public int getStartPos() {
        return start.getOffset();
    }
    
    /**
     * �I���|�W�V������Ԃ��B
     */
    public int getEndPos() {
        return end.getOffset();
    }
    
    /**
     * Velocity �𗘗p���ăX�^���v�̓��e��\������B
     */
    private void setMyText() {
        
        try {
            IInfoModel model = getStamp().getModel();
            VelocityContext context = ClientContext.getVelocityContext();
            context.put("model", model);
            context.put("hints", getHints());
            context.put("stampName", getStamp().getModuleInfo().getStampName());
            
            String templateFile = getStamp().getModel().getClass().getName() + ".vm";
            
            // ���̃X�^���v�̃e���v���[�g�t�@�C���𓾂�
            if (getStamp().getModuleInfo().getEntity().equals(IInfoModel.ENTITY_LABO_TEST)) {
                if (Project.getPreferences().getBoolean("laboFold", true)) {
                    templateFile = "labo.vm";
                }  
            } 
            
            // Merge ����
            StringWriter sw = new StringWriter();
            BufferedWriter bw = new BufferedWriter(sw);
            InputStream instream = ClientContext.getTemplateAsStream(templateFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(instream, "SHIFT_JIS"));
            Velocity.evaluate(context, bw, "stmpHolder", reader);
            bw.flush();
            bw.close();
            reader.close();
            
            // �S�p�����ƃX�y�[�X�𒼂�
            String text = sw.toString();
            for (int i = 0; i < MATCHIES.length; i++) {
                text = text.replace(MATCHIES[i], REPLACES[i]);
            }
            this.setText(text);
            
            // �J���e�y�C���֓W�J���ꂽ���L����̂�h��
            this.setMaximumSize(this.getPreferredSize());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}












