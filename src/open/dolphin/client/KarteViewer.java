/*
 * KarteEditor2.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2003-2005 Digital Globe, Inc. All rights reserved.
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.print.PageFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;

import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModelUtils;

/**
 * 2���J���e�N���X�B
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class KarteViewer extends DefaultChartDocument implements Comparable {
    
    
    /** �I������Ă��鎞�̃{�[�_�F */
    private static final Color SELECTED_COLOR = new Color(255, 0, 153);
    
    /** �I�����ꂽ��Ԃ̃{�[�_ */
    private static final Border SELECTED_BORDER = BorderFactory.createLineBorder(SELECTED_COLOR);
    
    /** �I������Ă��Ȃ����̃{�[�_�F */
    private static final Color NOT_SELECTED_COLOR = new Color(227, 250, 207);
    
    /** �I������Ă��Ȃ���Ԃ̃{�[�_ */
    private static final Border NOT_SELECTED_BORDER = BorderFactory.createLineBorder(NOT_SELECTED_COLOR);
    
    /** �^�C���X�^���v�� foreground �J���[ */
    private static final Color TIMESTAMP_FORE = Color.BLUE;
    
    /** �^�C���X�^���v�̃t�H���g�T�C�Y */
    private static final int TIMESTAMP_FONT_SIZE = 14;
    
    /** �^�C���X�^���v�t�H���g */
    private static final Font TIMESTAMP_FONT = new Font("Dialog", Font.PLAIN, TIMESTAMP_FONT_SIZE);
    
    /** �^�C���X�^���v�p�l�� FlowLayout �̃}�[�W�� */
    private static final int TIMESTAMP_SPACING = 7;
    
    /** ���ۑ����̃h�L�������g��\������ */
    private static final String UNDER_TMP_SAVE = " - ���ۑ���";
    
    //
    // �C���X�^���X�ϐ�
    //
    
    /** ���� view �̃��f�� */
    private DocumentModel model;
    
    /** �^�C���X�^���v���x�� */
    private JLabel timeStampLabel;
    
    /** SOA Pane */
    private KartePane soaPane;
    
    /** P Pane */
    private KartePane pPane;
    
    /** 2���J���e�p�l�� */
    private Panel2 panel2;
    
    /** �^�C���X�^���v�� foreground �J���[ */
    private Color timeStampFore = TIMESTAMP_FORE;
    
    /** �^�C���X�^���v�̃t�H���g */
    private Font timeStampFont = TIMESTAMP_FONT;
    
    private int timeStampSpacing = TIMESTAMP_SPACING;
    
    private boolean avoidEnter;
    
    /** �I������Ă��邩�ǂ����̃t���O */
    private boolean selected;
    
    /**
     * Creates new KarteViewer
     */
    public KarteViewer() {
    }
    
    
    public void setAvoidEnter(boolean b) {
        avoidEnter = b;
    }
    
    // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    // Junzo SATO
    public void printPanel2(final PageFormat format) {
        String name = getContext().getPatient().getFullName();
        panel2.printPanel(format, 1, false, name);
    }
    
    public void printPanel2(final PageFormat format, final int copies,
            final boolean useDialog) {
        String name = getContext().getPatient().getFullName();
        panel2.printPanel(format, copies, useDialog, name);
    }
    
    public void print() {
        PageFormat pageFormat = getContext().getContext().getPageFormat();
        this.printPanel2(pageFormat);
    }
    
    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        
    /**
     * �h�L�������g�̒����� view �̃s�N�Z�����ɂ��ĕԂ��B
     * @return modelToView(�h�L�������g�̒���)
     */
    public int getActualHeight() {
        try {
            JTextPane pane = soaPane.getTextPane();
            int pos = pane.getDocument().getLength();
            Rectangle r = pane.modelToView(pos);
            int hsoa = r.y;
            
            pane = pPane.getTextPane();
            pos = pane.getDocument().getLength();
            r = pane.modelToView(pos);
            int hp = r.y;
            
            return Math.max(hsoa, hp);
            
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
        return 0;
    }
    
    public void adjustSize() {
        int h = getActualHeight();
        soaPane.getTextPane().setPreferredSize(new Dimension(KartePane.PANE_WIDTH, h));
        pPane.getTextPane().setPreferredSize(new Dimension(KartePane.PANE_WIDTH, h));
    }
    
    /**
     * SOA Pane ��Ԃ��B
     * @return soaPane
     */
    public KartePane getSOAPane() {
        return soaPane;
    }
    
    /**
     * P Pane ��Ԃ��B
     * @return pPane
     */
    public KartePane getPPane() {
        return pPane;
    }
    
    /**
     * �R���e�i����R�[������� enter() ���\�b�h��
     * ���j���[�𐧌䂷��B
     */
    public void enter() {
        
        if (avoidEnter) {
            return;
        }
        super.enter();
        
        // ReadOnly ����
        boolean canEdit = getContext().isReadOnly() ? false : true;
        
        // ���ۑ����ǂ���
        boolean tmp = model.getDocInfo().getStatus().equals(IInfoModel.STATUS_TMP) ? true : false;
        
        // �V�K�J���e�쐬���\�ȏ���
        boolean newOk = canEdit && (!tmp) ? true : false;
        
        ChartMediator mediator = getContext().getChartMediator();
        mediator.getAction(GUIConst.ACTION_NEW_KARTE).setEnabled(newOk); // �V�K�J���e
        mediator.getAction(GUIConst.ACTION_PRINT).setEnabled(true); // ���
        mediator.getAction(GUIConst.ACTION_MODIFY_KARTE).setEnabled(canEdit); // �C��
    }
    
    /**
     * ����������BGUI ���\�z����B
     */
    public void initialize() {
        
        // TimeStampLabel �𐶐�����
        timeStampLabel = new JLabel("TimeStamp");
        timeStampLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeStampLabel.setForeground(timeStampFore);
        timeStampLabel.setFont(timeStampFont);
        
        // SOA Pane �𐶐�����
        soaPane = new KartePane();
        if (model != null) {
            // Schema �摜�Ƀt�@�C������t����̂��߂ɕK�v
            String docId = model.getDocInfo().getDocId();
            soaPane.setDocId(docId);
        }
        
        // P Pane �𐶐�����
        pPane = new KartePane();
        
        // �݂��Ɋ֘A������
        soaPane.setRole(IInfoModel.ROLE_SOA, pPane);
        pPane.setRole(IInfoModel.ROLE_P, soaPane);
        
        //
        // 2���J���e�𐶐�����
        //
        panel2 = new Panel2();
        panel2.setLayout(new BorderLayout());
        
        JPanel flowPanel = new JPanel();
        flowPanel.setLayout(new BoxLayout(flowPanel, BoxLayout.X_AXIS));
        flowPanel.add(soaPane.getTextPane());
        flowPanel.add(Box.createHorizontalStrut(KartePane.PANE_DIVIDER_WIDTH));
        flowPanel.add(pPane.getTextPane());        
        
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, timeStampSpacing));
        timePanel.add(timeStampLabel);
        //timePanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        panel2.add(timePanel, BorderLayout.NORTH);
        panel2.add(flowPanel, BorderLayout.CENTER);
        
        setUI(panel2);
    }
    
    /**
     * �v���O�������J�n����B
     */
    public void start() {
        //
        // Creates GUI
        //
        this.initialize();
        
        //
        // Display Model
        //
        if (this.getModel() != null) {
            //
            // �m����𕪂���₷���\���ɕς���
            //
            String timeStamp = ModelUtils.getDateAsFormatString(
                    model.getDocInfo().getFirstConfirmDate(), 
                    IInfoModel.KARTE_DATE_FORMAT);
            
            if (model.getDocInfo().getStatus().equals(IInfoModel.STATUS_TMP)) {
                StringBuilder sb = new StringBuilder();
                sb.append(timeStamp);
                sb.append(UNDER_TMP_SAVE);
                timeStamp = sb.toString();
            }
            timeStampLabel.setText(timeStamp);
            KarteRenderer_2 renderer = new KarteRenderer_2(soaPane, pPane);
            renderer.render(model);
        }
        
        //
        // ���f���\����Ƀ��X�i����ݒ肷��
        //
        ChartMediator mediator = getContext().getChartMediator();
        soaPane.init(false, mediator);
        pPane.init(false, mediator);
        enter();
    }
    
    public void stop() {
        soaPane.clear();
        pPane.clear();
    }
    
    /**
     * �\�����郂�f����ݒ肷��B
     * @param model �\������DocumentModel
     */
    public void setModel(DocumentModel model) {
        this.model = model;
    }
    
    /**
     * �\�����郂�f����Ԃ��B
     * @return �\������DocumentModel
     */
    public DocumentModel getModel() {
        return model;
    }
    
    /**
     * �I����Ԃ�ݒ肷��B
     * �I����Ԃɂ��View�̃{�[�_�̐F��ς���B
     * @param selected �I�����ꂽ�� true
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            getUI().setBorder(SELECTED_BORDER);
        } else {
            getUI().setBorder(NOT_SELECTED_BORDER);
        }
    }
    
    /**
     * �I������Ă��邩�ǂ�����Ԃ��B
     * @return �I������Ă��鎞 true
     */
    public boolean isSelected() {
        return selected;
    }
    
    
    /*public boolean copyStamp() {
            return pPane.copyStamp();
    }*/
    
    public int hashCode() {
        return getModel().getDocInfo().getDocId().hashCode() + 72;
    }
    
    public boolean equals(Object other) {
        if (other != null && other.getClass() == this.getClass()) {
            DocInfoModel otheInfo = ((KarteViewer) other).getModel()
            .getDocInfo();
            return getModel().getDocInfo().equals(otheInfo);
        }
        return false;
    }
    
    public int compareTo(Object other) {
        if (other != null && other.getClass() == this.getClass()) {
            DocInfoModel otheInfo = ((KarteViewer) other).getModel()
            .getDocInfo();
            return getModel().getDocInfo().compareTo(otheInfo);
        }
        return -1;
    }
}