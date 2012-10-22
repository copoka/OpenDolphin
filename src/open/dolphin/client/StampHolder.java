/*
 * StampHolder2.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2004-2005 Digital Globe, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package open.dolphin.client;

import javax.swing.*;
import javax.swing.text.*;
import java.beans.*;
import java.io.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;

import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleModel;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;

/**
 * KartePane �� Component�@�Ƃ��đ}�������X�^���v��ێ��X���N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class StampHolder extends ComponentHolder implements IComponentHolder{
    
    private static final long serialVersionUID = -115789645956065719L;
    
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
    public void focusGained(FocusEvent e) {
        //System.out.println("stamp gained");
        ChartMediator mediator = kartePane.getMediator();
        mediator.setCurrentComponent(this);
        mediator.getAction(GUIConst.ACTION_CUT).setEnabled(false);
        mediator.getAction(GUIConst.ACTION_COPY).setEnabled(false);
        mediator.getAction(GUIConst.ACTION_PASTE).setEnabled(false);
        mediator.getAction(GUIConst.ACTION_UNDO).setEnabled(false);
        mediator.getAction(GUIConst.ACTION_REDO).setEnabled(false);
        mediator.getAction(GUIConst.ACTION_INSERT_TEXT).setEnabled(false);
        mediator.getAction(GUIConst.ACTION_INSERT_SCHEMA).setEnabled(false);
        mediator.getAction(GUIConst.ACTION_INSERT_STAMP).setEnabled(false);
        mediator.enableMenus(new String[]{GUIConst.ACTION_COPY});
        if (kartePane.getTextPane().isEditable()) {
            mediator.enableMenus(new String[]{GUIConst.ACTION_CUT});
        } else {
            mediator.disableMenus(new String[]{GUIConst.ACTION_CUT});
        }
        mediator.disableMenus(new String[]{GUIConst.ACTION_PASTE});
        setSelected(true);
    }
    
    /**
     * Focus���͂��ꂽ�ꍇ�̃��j���[����ƃ{�[�_�[�̔�\�����s���B
     */
    public void focusLost(FocusEvent e) {
        //System.out.println("stamp lost");
        //ChartMediator mediator = kartePane.getMediator();
        //String[] menus = new String[]{"cut", "copy", "paste"};
        //mediator.disableMenus(menus);
        setSelected(false);
    }
    
    /**
     * Popup���j���[��\������B
     */
    public void mabeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            JPopupMenu popup = new JPopupMenu();
            // popup����StampHolder��FocusLost�ɂȂ邽��
            popup.setFocusable(false);
            ChartMediator mediator = kartePane.getMediator();
            popup.add(mediator.getAction(GUIConst.ACTION_CUT));
            popup.add(mediator.getAction(GUIConst.ACTION_COPY));
            popup.add(mediator.getAction(GUIConst.ACTION_PASTE));
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
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
        return IComponentHolder.TT_STAMP;
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
            
            // ���̃X�^���v�̃e���v���[�g�t�@�C���𓾂�
            String templateFile = getStamp().getModel().getClass().getName() + ".vm";
            
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