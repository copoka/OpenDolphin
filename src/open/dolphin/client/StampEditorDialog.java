/*
 * StampEditorDialog.java        1.0 2001/3/1
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2003,2004 Digital Globe, Inc. All rights reserved.
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

import open.dolphin.plugin.IPluginContext;
import open.dolphin.plugin.helper.ComponentMemory;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;

/**
 * Stamp �ҏW�p�̊O�g��񋟂��� Dialog.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class StampEditorDialog implements IStampEditorDialog, PropertyChangeListener {
    
    public static final String VALIDA_DATA_PROP = "validData";
    public static final String VALUE_PROP = "value";
    
    /** stampEditor �̃v���O�|�C���g */
    private static final String EDITOR_PLUG_POINT = "karteEditor/stampEditor";
    
    /** command buttons */
    private static final String OK_ICON_KARTE = "/open/dolphin/resources/images/lgicn_16.gif";
    private static final String OK_ICON_STAMPBOX = "/open/dolphin/resources/images/tools_16.gif";
    private JButton okButton;
    
    /** target editor */
    private IStampModelEditor editor;
    private PropertyChangeSupport boundSupport;
    
    private JDialog dialog;
    private String entity;
    private Object value;
    private boolean toKarte;
    private BlockGlass glass;
    
    private static final int DEFAULT_X = 159;
    private static final int DEFAULT_Y = 67;
    private static final int DEFAULT_WIDTH = 924;
    private static final int DEFAULT_HEIGHT = 616;
    
    /**
     * Constructor. Use layered inititialization pattern.
     */
    public StampEditorDialog(String entity, Object value, boolean toKarte)  {
        this.entity = entity;
        this.value = value;
        this.toKarte = toKarte;
        boundSupport = new PropertyChangeSupport(this);
    }
    
    /**
     * Constructor. Use layered inititialization pattern.
     */
    public StampEditorDialog(String entity, Object value)  {
        this(entity, value, true);
    }
    
    /**
     * �G�f�B�^���J�n����B
     */
    public void start() {
        
        Runnable initilizer = new Runnable() {
            public void run() {
                initialize();
            }
        };
        Thread t = new Thread(initilizer);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }
    
    /**
     * GUI�R���|�[�l���g������������B
     */
    private void initialize() {
        
        // �J���e�ɓW�J���邩�X�^���v�{�b�N�X�ɕۑ����邩��
        // ���[�_�������y�у{�^���̃A�C�R���ƃc�[���`�b�v��ς���
        if (toKarte) {
            dialog = new JDialog((Frame)null, true);
            okButton = new JButton(createImageIcon(OK_ICON_KARTE));
            okButton.setToolTipText("�J���e�ɓW�J���܂�");
        } else {
            dialog = new JDialog((Frame)null, false);
            okButton = new JButton(createImageIcon(OK_ICON_STAMPBOX));
            okButton.setToolTipText("�X�^���v�{�b�N�X�ɕۑ����܂�");
        }
            
        //
        // BlockGlass �𐶐��� dialog �ɐݒ肷��
        //
        glass = new BlockGlass();
        dialog.setGlassPane(glass);
        
        // OK �{�^���Ƃ��̃A�N�V�����𐶐�����
        ActionListener action = (ActionListener) (GenericListener.create(
                ActionListener.class,
                "actionPerformed",
                this,
                "okButtonClicked"));
        okButton.addActionListener(action);
        okButton.setMnemonic('O');
        okButton.setEnabled(false);
        
        // Cancel button�@�Ƃ��̃A�N�V�����𐶐�����
        //action = (ActionListener) (GenericListener.create (
        // ActionListener.class,
        // "actionPerformed",
        // this,
        // "cancelButtonClicked"));
        //cancelButton.addActionListener(action);
        //cancelButton.setMnemonic('C');
        
        // ���ۂ́i�����ƂȂ�j�G�f�B�^�𐶐����� Dialog �� add ����
        try {
            IPluginContext pluginCtx = ClientContext.getPluginContext();
            String jndiName = EDITOR_PLUG_POINT + "/" + entity;
            editor = (IStampModelEditor) pluginCtx.lookup(jndiName);
            editor.setContext(this);
            editor.start();
            editor.addPropertyChangeListener(VALIDA_DATA_PROP, this);
            System.err.println("edito.setValue");
            editor.setValue(value);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        // ���A�C�E�g����
        JPanel panel = new JPanel(new BorderLayout(0,11));
        panel.add((Component)editor, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        
        // CloseBox ������o�^����
        dialog.addWindowListener(new WindowAdapter() {
            
            public void windowClosing(WindowEvent e) {
                // CloseBox ���N���b�N���ꂽ�ꍇ�̓L�����Z���Ƃ���
                value = null;
                close();
            }
        });
        
        dialog.setTitle(editor.getTitle());
        ComponentMemory cm = new ComponentMemory(dialog, new Point(DEFAULT_X,DEFAULT_Y), new Dimension(DEFAULT_WIDTH,DEFAULT_HEIGHT), this);
        cm.setToPreferenceBounds();
        
        dialog.setVisible(true);
    }
    
    /**
     * �ҏW���� Stamp ��Ԃ��B
     */
    public Object getValue() {
        return editor.getValue();
    }
    
    /**
     * �v���p�e�B�`�F���W���X�i��o�^����B
     * @param prop �v���p�e�B��
     * @param listener �v���p�e�B�`�F���W���X�i
     */
    public void addPropertyChangeListener(String prop, PropertyChangeListener listener) {
        boundSupport.addPropertyChangeListener(prop, listener);
    }
    
    /**
     * �v���p�e�B�`�F���W���X�i���폜����B
     * @param prop �v���p�e�B��
     * @param listener �v���p�e�B�`�F���W���X�i
     */
    public void remopvePropertyChangeListener(String prop, PropertyChangeListener listener) {
        boundSupport.removePropertyChangeListener(prop, listener);
    }
    
    /**
     * OK�{�^����Ԃ��B����̓G�f�B�^�Ƀ��C�A�E�g�����B
     * @return OK�{�^��
     */
    public JButton getOkButton() {
        return okButton;
    }
    
    /**
     * OK�{�^�����N���b�N����B
     * �G�f�B�^�ŕҏW�����X�^���v��v�����ƂɕԂ��B
     * @param e ActionEvent
     */
    public void okButtonClicked(ActionEvent e) {
        value = getValue();
        if (toKarte) {
            close();
        } else {
            boundSupport.firePropertyChange(VALUE_PROP, null, value);
        }
    }
    
    public void addStampButtonClicked(ActionEvent e) {
        value = null;
        close();
    }
    
    /**
     * �ҏW���̃��f���l���L���Ȓl���ǂ����̒ʒm���󂯁A
     * �J���e�ɓW�J�{�^���� enable/disable �ɂ���
     */
    public void propertyChange(PropertyChangeEvent evt) {
        
        Boolean i = (Boolean)evt.getNewValue();
        boolean state = i.booleanValue();
        
        if (state) {
            okButton.setEnabled(true);
        } else {
            okButton.setEnabled(false);
        }
    }
    
    /**
     * �_�C�A���O�����
     */
    public void close() {
        editor.dispose();
        dialog.setVisible(false);
        dialog.dispose();
        boundSupport.firePropertyChange(VALUE_PROP, null, value);
    }
    
    private ImageIcon createImageIcon(String name) {
        return new ImageIcon(this.getClass().getResource(name));
    }
}