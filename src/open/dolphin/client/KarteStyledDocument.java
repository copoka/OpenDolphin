/*
 * KarteStyledDocument.java
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
import javax.swing.text.*;
import java.awt.*;

/**
 * KartePane �� StyledDocument class�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class KarteStyledDocument extends DefaultStyledDocument {
    
    /** Style */
    static final String TIMESTAMP_STYLE  = "StampTimestamp";
    static final String SEPARATOR_LINE   = "StampSeparatorLine";
    static final String STAMP_STYLE      = "stampHolder";
    static final String SCHEMA_STYLE     = "schemaHolder"; 
    static final String LINE_IMAGE       = "line.jpg";

	/** KartePane �ɕ\������^�C���X�^���v�J���[ */
    private Color timeSTampColor = new Color(255, 64, 183);
	private KartePane kartePane;
	private Position insertStampPos;
	private Position topFreePos;
    
    /** Creates new TestDocument */
    public KarteStyledDocument() {
                        
        // �^�C���X�^���v�X�^�C����o�^����
        Style style = addStyle(TIMESTAMP_STYLE, null);
        StyleConstants.setForeground(style, timeSTampColor);
        StyleConstants.setAlignment(style, StyleConstants.ALIGN_CENTER);
        
        // �Z�p��-�^���C���X�^�C����o�^����
        style = addStyle(SEPARATOR_LINE, null);
        ImageIcon icon = ClientContext.getImageIcon(LINE_IMAGE);
        StyleConstants.setIcon(style, icon);
    }
    
    public void setParent(KartePane kartePane) {
        this.kartePane = kartePane;
    }
    
    public int getFreeTop() {
        int offset = insertStampPos.getOffset() + 2;
        return offset;
    }
    
    public int getTopFreePos() {
        return topFreePos.getOffset();
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
			super.insertString(getLength(), "\n", null);
		}
		catch (BadLocationException e) {
			System.out.println("BadLocationException at the makeParagraph: " + e.toString());
			e.printStackTrace();
		}
	}    
    
    public void setTimestamp(String timeStamp) {
                
        try {            
            // �^�C���X�^���v��}������
            setLogicalStyle(TIMESTAMP_STYLE);
            super.insertString(getLength(), timeStamp, null);
            makeParagraph();
                             
            // �^�C���X�^���v�{���s�@�̌オ�@�X�^���v�}���|�W�V����
            int start = getLength();
            
            // �Z�p��-�^�A�C�R����}������ �_���X�^�C���͌p������Ă���
            AttributeSet a = (AttributeSet)getStyle(SEPARATOR_LINE);
            super.insertString(getLength()," ", a);
			makeParagraph();
            
            // Free text �� top �ʒu
            topFreePos = createPosition(getLength()-1);
            
            // �_���X�^�C�����N���A����
			clearLogicalStyle();
            
            // �X�^���v�̑}���ʒu�𐶐�����
            insertStampPos = createPosition(start);
        }
        catch (BadLocationException e) {
            System.out.println("Exception at the setTimestamp: " + e.toString());
            e.printStackTrace();
        }
    }  
    
    public void stamp(StampHolder sh) {
                
        try {
            // ���̃X�^���v�p�̃X�^�C���𓮓I�ɐ�������
            Style runStyle = addStyle(STAMP_STYLE, null);
            StyleConstants.setComponent(runStyle, sh);
            
            // Stamp ��}������
            int start = insertStampPos.getOffset();
            setLogicalStyle(start, null);
            super.insertString(start, " ", runStyle);

            // Stamp ���s
            int end = insertStampPos.getOffset();
            super.insertString(end, "\n", SimpleAttributeSet.EMPTY);

            // �ǉ��̉��s start ���琔����3��
            int offset = insertStampPos.getOffset();
            super.insertString(offset, "\n", SimpleAttributeSet.EMPTY);                 
            
            // �X�^���v�̊J�n�ƏI���ʒu�𐶐����ĕۑ�����
            sh.setEntry(createPosition(start), createPosition(end));
        }
        catch(BadLocationException be) {
            System.out.println ("BadLocationException at the stamp: " + be.toString());
            be.printStackTrace();
        }
        catch(NullPointerException ne) {
            System.out.println("NullPointerException at the stamp");
            ne.printStackTrace();
        }
    } 
    
    public void removeStamp(int start, int len) {
        
        try {
            //len = 3;
            super.remove(start, len);
        }
        catch(BadLocationException be) {
            System.out.println ("BadLocationException at the removeStamp: " + be.toString());
            be.printStackTrace();
        }
    }
    
    public void insertStamp(Position inPos, StampHolder sh) {
            
        try {
            Style runStyle = this.addStyle(STAMP_STYLE, null);
            StyleConstants.setComponent(runStyle, sh);
            
            // Stamp �}��
            int start = inPos.getOffset();
            setLogicalStyle(start, null);
            super.insertString(start, " ", runStyle);
            
            // Stamp ���s
            int end = inPos.getOffset();
            super.insertString(end, "\n", SimpleAttributeSet.EMPTY);
            
            // �ǉ��̉��s
            int offset = inPos.getOffset();
            super.insertString(offset, "\n", SimpleAttributeSet.EMPTY);            
            
            // Position ��ۑ�����
            sh.setEntry(createPosition(start), createPosition(end));
        }
        catch(BadLocationException be) {
            System.out.println ("BadLocationException at the insertStamp: " + be.toString());
            be.printStackTrace();
        }
    }   
    
    public void stampSchema(SchemaHolder sc) {
            
        try {
            // Icon Label �}��
            int start = kartePane.getCaretPosition();
            
            // �摜�̂����E���ɂ͓��͕s�� !!
            // 2003-10-02
            if (isJustRightOfSchema(start)) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            
            // ���̃X�^���v�p�̃X�^�C���𓮓I�ɐ�������
            Style runStyle = addStyle(SCHEMA_STYLE, null);
            StyleConstants.setComponent(runStyle, sc);
            
            //System.out.println("pos = " + start);
            start = start >= insertStampPos.getOffset() ? start : getLength();

            super.insertString(start, " ", runStyle);
                        
            // Icon Label ���s
            int end = start + 1;
            super.insertString(end, "\n", SimpleAttributeSet.EMPTY);
                        
            // Position �ۑ�
            sc.setEntry(createPosition(start), createPosition(end));
        }
        catch(BadLocationException be) {
            System.out.println ("BadLocationException at the stampSchema: " + be.toString());
            be.printStackTrace();
        }
    }
    
    public void dndSchema(SchemaHolder sc, int pos) {
            
        try {
            // ���̃X�^���v�p�̃X�^�C���𓮓I�ɐ�������
            Style runStyle = addStyle(SCHEMA_STYLE, null);
            StyleConstants.setComponent(runStyle, sc);
            
            // Icon Label �}��
            int len = getLength();
            if (pos > len) {
                //int line = kartePane.getLineOfOffset(pos);
                //int col = pos - kartePane.getLineStartOffset(line);
                int width = kartePane.getWidth();
                //System.out.println(width);
                Point left = new Point(10, 10);
                Point right = new Point(width+ 10, 10);
                int cols = kartePane.viewToModel(right) - kartePane.viewToModel(left);
                //System.out.println(cols);
                //cols = 20;
                int line = pos / cols;
                int col = pos % cols;
                for (int i = 0; i < line; i++) {
                    super.insertString(getLength(), "\n", SimpleAttributeSet.EMPTY);
                }
                for (int i = 0; i < col; i++) {
                    super.insertString(getLength(), " ", SimpleAttributeSet.EMPTY);
                }
            
            } else {
                int top = topFreePos.getOffset();
                pos = pos > top ? pos : top;
            }
            
            super.insertString(pos, " ", runStyle);
                        
            // Icon Label ���s
            int end = pos + 1;
            super.insertString(end, "\n", SimpleAttributeSet.EMPTY);
                        
            // Position �ۑ�
            sc.setEntry(createPosition(pos), createPosition(end));
        }
        catch(BadLocationException be) {
            System.out.println ("BadLocationException at stamp: " + be.toString());
        }
    }
    
    public void insertTextStamp(String text) {
        int pos = kartePane.getCaretPosition();
        if ( pos <= (insertStampPos.getOffset() + 1) ) {
            pos = getLength();
            //super.insertString(offset,str,a);
        }
        
        if (isJustRightOfSchema(pos)) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        
        try {
            super.insertString(pos, text, null);
        }
        catch (BadLocationException e) {
            System.out.println("BadLocationException at the insertFreeString: " + e.toString());
            e.printStackTrace();
        }
    }
    
    public void insertFreeString(String text, AttributeSet a) {
        try {
            super.insertString(getLength(), text, a);
        }
        catch (BadLocationException e) {
            System.out.println("BadLocationException at the insertFreeString: " + e.toString());
            e.printStackTrace();
        }
    }
        
    public void insertString(int offset,String str,AttributeSet a)
    throws BadLocationException {
                
        if ( offset > (insertStampPos.getOffset() + 1) ) {
            
            // �摜�̂����E���͓��͕s�� !  2003-10-02
            if (isJustRightOfSchema(offset)) {
                //System.out.println("Schema!!");
                Toolkit.getDefaultToolkit().beep();
                return;
            
            } else {
            
                super.insertString(offset,str,a);
            }
        }
        else {
            Toolkit.getDefaultToolkit().beep();
        }
    }
    
    public void remove(int offset,int len) throws BadLocationException {
        
        if ( offset > (insertStampPos.getOffset() + 1) ) {
            Element e = getCharacterElement(offset);
            if (e.getName().equals("component")) {
                Toolkit.getDefaultToolkit().beep();
            }
            else {
                super.remove(offset,len);
            }
        }
        else {
            Toolkit.getDefaultToolkit().beep();
        }
    }
    
    private boolean isJustRightOfSchema(int offset) {
        Element e = getCharacterElement(offset -1);
        return e.getName().equals("component") ? true : false;
    }
}