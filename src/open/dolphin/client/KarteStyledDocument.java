/*
 * KarteStyledDocument.java
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

import javax.swing.text.*;

/**
 * KartePane �� StyledDocument class�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class KarteStyledDocument extends DefaultStyledDocument {
    
    private static final long serialVersionUID = 3078315320512749196L;
    
    /** Style */
    private final String STAMP_STYLE      = "stampHolder";
    private final String SCHEMA_STYLE     = "schemaHolder";
    
    // �I�[�i�� KartePane
    private KartePane kartePane;
    
    /** Creates new TestDocument */
    public KarteStyledDocument() {
    }
    
    public void setParent(KartePane kartePane) {
        this.kartePane = kartePane;
    }
    
    public void setLogicalStyle(String str) {
        Style style = this.getStyle(str);
        this.setLogicalStyle(this.getLength(), style);
    }
    
    public void clearLogicalStyle() {
        this.setLogicalStyle(this.getLength(), null);
    }
    
    public void makeParagraph() {
        try {
            insertString(getLength(), "\n", null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Stamp ��}������B
     * @param sh �}������X�^���v�z���_
     */
    public void stamp(final StampHolder sh) {
        
        
        try {
            // ���̃X�^���v�p�̃X�^�C���𓮓I�ɐ�������
            Style runStyle = addStyle(STAMP_STYLE, null);
            StyleConstants.setComponent(runStyle, sh);
            //StyleConstants.setLeftIndent(runStyle, 10);
            
            // �L�����b�g�ʒu���擾����
            int start = kartePane.getTextPane().getCaretPosition();
            
            // Stamp ��}������
            //insertString(start, " ", null);
            insertString(start, "S", runStyle);
            insertString(start+1, "\n", null);  // ���s�����Ȃ��ƃe�L�X�g���͐��䂪���ɂ����Ȃ�
            
            // �X�^���v�̊J�n�ƏI���ʒu�𐶐����ĕۑ�����
            sh.setEntry(createPosition(start), createPosition(start+1));
            
        } catch(BadLocationException be) {
            be.printStackTrace();
        } catch(NullPointerException ne) {
            ne.printStackTrace();
        }
    }
    
    /**
     * Stamp ��}������B
     * @param sh �}������X�^���v�z���_
     */
    public void flowStamp(final StampHolder sh) {
        
        
        try {
            // ���̃X�^���v�p�̃X�^�C���𓮓I�ɐ�������
            Style runStyle = addStyle(STAMP_STYLE, null);
            StyleConstants.setComponent(runStyle, sh);
            
            // �L�����b�g�ʒu���擾����
            int start = kartePane.getTextPane().getCaretPosition();
            
            // Stamp ��}������
            insertString(start, "S", runStyle);
            
            // �X�^���v�̊J�n�ƏI���ʒu�𐶐����ĕۑ�����
            sh.setEntry(createPosition(start), createPosition(start+1));
            
        } catch(BadLocationException be) {
            be.printStackTrace();
        } catch(NullPointerException ne) {
            ne.printStackTrace();
        }
    }
    
    /**
     * Stamp���폜����B
     * @param start �폜�J�n�̃I�t�Z�b�g�ʒu
     * @param len
     */
    public void removeStamp(int start, int len) {
        
        try {
            // Stamp �͈ꕶ���ŕ\����Ă���
            remove(start, 1);
        } catch(BadLocationException be) {
            be.printStackTrace();
        }
    }
    
    /**
     * Stamp���w�肳�ꂽ�|�W�V�����ɑ}������B
     * @param inPos�@�}���|�W�V����
     * @param sh�@�}������ StampHolder
     */
    public void insertStamp(Position inPos, StampHolder sh) {
        
        try {
            Style runStyle = this.addStyle(STAMP_STYLE, null);
            StyleConstants.setComponent(runStyle, sh);
            
            // �}���ʒu
            int start = inPos.getOffset();
            insertString(start, "S", runStyle);
            sh.setEntry(createPosition(start), createPosition(start+1));
        } catch(BadLocationException be) {
            be.printStackTrace();
        }
    }
    
    public void stampSchema(SchemaHolder sc) {
        
        try {
            // ���̃X�^���v�p�̃X�^�C���𓮓I�ɐ�������
            Style runStyle = addStyle(SCHEMA_STYLE, null);
            StyleConstants.setComponent(runStyle, sc);
            
            // Stamp���l
            int start = kartePane.getTextPane().getCaretPosition();
            insertString(start, "I", runStyle);
            insertString(start+1, "\n", null);  // ���s�����Ȃ��ƃe�L�X�g���͐��䂪���ɂ����Ȃ�
            sc.setEntry(createPosition(start), createPosition(start+1));
        } catch(BadLocationException be) {
            be.printStackTrace();
        }
    }
    
    public void flowSchema(final SchemaHolder sh) {
        
        try {
            // ���̃X�^���v�p�̃X�^�C���𓮓I�ɐ�������
            Style runStyle = addStyle(SCHEMA_STYLE, null);
            StyleConstants.setComponent(runStyle, sh);
            
            // �L�����b�g�ʒu���擾����
            int start = kartePane.getTextPane().getCaretPosition();
            
            // Stamp ��}������
            insertString(start, "I", runStyle);
            
            // �X�^���v�̊J�n�ƏI���ʒu�𐶐����ĕۑ�����
            sh.setEntry(createPosition(start), createPosition(start+1));
            
        } catch(BadLocationException be) {
            be.printStackTrace();
        } catch(NullPointerException ne) {
            ne.printStackTrace();
        }
    }
    
    public void insertTextStamp(String text) {
        
        try {
            //System.out.println("insertTextStamp");
            clearLogicalStyle();
            setLogicalStyle("default"); // mac 2207-03-31
            int pos = kartePane.getTextPane().getCaretPosition();
            //System.out.println("pos = " + pos);
            insertString(pos, text, null);
            //System.out.println("inserted TextStamp");
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    public void insertFreeString(String text, AttributeSet a) {
        try {
            insertString(getLength(), text, a);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}