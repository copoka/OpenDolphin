package open.dolphin.client;

import java.util.prefs.Preferences;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Position;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import open.dolphin.project.Project;

/**
 * KartePane �� StyledDocument class�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class KarteStyledDocument extends DefaultStyledDocument {
    
    // �X�^���v�̐擪�����s����
    private boolean topSpace;
    
    // stampHolder Style
    private final String STAMP_STYLE      = "stampHolder";
    
    // schemaHolder
    private final String SCHEMA_STYLE     = "schemaHolder";
    
    // KartePane
    private KartePane kartePane;
    
    
    /** Creates new TestDocument */
    public KarteStyledDocument() {
        topSpace = Project.getPreferences().getBoolean("stampSpace", true);
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
            Style runStyle = this.getStyle(STAMP_STYLE);
            if (runStyle == null) {
                runStyle = addStyle(STAMP_STYLE, null);
            }
            StyleConstants.setComponent(runStyle, sh);
            
            // �L�����b�g�ʒu���擾����
            int start = kartePane.getTextPane().getCaretPosition();
            
            // Stamp ��}������
            if (topSpace) {
                insertString(start, "\n", null);
                insertString(start+1, " ", runStyle);
                insertString(start+2, "\n", null);                           // ���s�����Ȃ��ƃe�L�X�g���͐��䂪���ɂ����Ȃ�
                sh.setEntry(createPosition(start+1), createPosition(start+2)); // �X�^���v�̊J�n�ƏI���ʒu�𐶐����ĕۑ�����
            } else {
                insertString(start, " ", runStyle);
                insertString(start+1, "\n", null);                           // ���s�����Ȃ��ƃe�L�X�g���͐��䂪���ɂ����Ȃ�
                sh.setEntry(createPosition(start), createPosition(start+1)); // �X�^���v�̊J�n�ƏI���ʒu�𐶐����ĕۑ�����
            }
            
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
            Style runStyle = this.getStyle(STAMP_STYLE);
            if (runStyle == null) {
                runStyle = addStyle(STAMP_STYLE, null);
            }
            // ���̃X�^���v�p�̃X�^�C���𓮓I�ɐ�������
            //Style runStyle = addStyle(STAMP_STYLE, null);
            StyleConstants.setComponent(runStyle, sh);
            
            // �L�����b�g�ʒu���擾����
            int start = kartePane.getTextPane().getCaretPosition();
            
            // Stamp ��}������
            insertString(start, " ", runStyle);
            
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
            Style runStyle = this.getStyle(STAMP_STYLE);
            if (runStyle == null) {
                runStyle = addStyle(STAMP_STYLE, null);
            }
            //Style runStyle = this.addStyle(STAMP_STYLE, null);
            StyleConstants.setComponent(runStyle, sh);
            
            // �}���ʒu
            int start = inPos.getOffset();
            insertString(start, " ", runStyle);
            sh.setEntry(createPosition(start), createPosition(start+1));
        } catch(BadLocationException be) {
            be.printStackTrace();
        }
    }
    
    public void stampSchema(SchemaHolder sc) {
        
        try {
            Style runStyle = this.getStyle(SCHEMA_STYLE);
            if (runStyle == null) {
                runStyle = addStyle(SCHEMA_STYLE, null);
            }
            // ���̃X�^���v�p�̃X�^�C���𓮓I�ɐ�������
            //Style runStyle = addStyle(SCHEMA_STYLE, null);
            StyleConstants.setComponent(runStyle, sc);
            
            // Stamp���l
            int start = kartePane.getTextPane().getCaretPosition();
            insertString(start, " ", runStyle);
            insertString(start+1, "\n", null);
            sc.setEntry(createPosition(start), createPosition(start+1));
        } catch(BadLocationException be) {
            be.printStackTrace();
        }
    }
    
    public void flowSchema(final SchemaHolder sh) {
        
        try {
            Style runStyle = this.getStyle(SCHEMA_STYLE);
            if (runStyle == null) {
                runStyle = addStyle(SCHEMA_STYLE, null);
            }
            // ���̃X�^���v�p�̃X�^�C���𓮓I�ɐ�������
            //Style runStyle = addStyle(SCHEMA_STYLE, null);
            StyleConstants.setComponent(runStyle, sh);
            
            // �L�����b�g�ʒu���擾����
            int start = kartePane.getTextPane().getCaretPosition();
            
            // Stamp ��}������
            insertString(start, " ", runStyle);
            
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