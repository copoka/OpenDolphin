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

import open.dolphin.exception.*;
import open.dolphin.util.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;

/**
 * Stamp �ҏW�p�̊O�g��񋟂��� Dialog.
 * 
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public final class StampEditorDialog extends JDialog 
implements PropertyChangeListener, Runnable {
          
    /** button text */
    private static final String OK_TEXT          = "�J���e�ɓW�J(O)";
    private String okButtonText = OK_TEXT;
    private static final String CANCEL_TEXT      = 
        (String)UIManager.get("OptionPane.cancelButtonText") + "(C)";
    
    /** command buttons */
    private final JButton okButton = new JButton(okButtonText);
    private final JButton cancelButton = new JButton(CANCEL_TEXT);
    
    /** target editor */
    private IStampModelEditor editor;
    private PropertyChangeSupport boundSupport;
    private Object value;
    
    /**
     * Constructor. Use layered inititialization pattern.
     */
    public StampEditorDialog(String category) throws DolphinException {
        
        super((Frame)null, true);     // create a modal dialog
        
        try {
        	// ���ۂ́i�����ƂȂ�j�G�f�B�^�𐶐����� Dialog �� add ����
            editor = (IStampModelEditor)StampEditorFactory.create(category);
            
        } catch (Exception e) {
        	String error = "Problems creating stamp editor: " + e.toString();
            System.out.println(error);
            e.printStackTrace();
            throw new DolphinException(error);
        }
            
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

        panel.add((Component)editor);
        panel.add(Box.createVerticalStrut(17));  // Adds 17 pixels spacing
        panel.add(createButtonPane(this));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));

        getContentPane().add(panel, BorderLayout.CENTER);

        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
            	// CloseBox ���N���b�N���ꂽ�ꍇ�̓L�����Z���Ƃ���
                value = null;
                notifyValue();
                close();
            }
        });

        editor.addPropertyChangeListener("validData", this);
        boundSupport = new PropertyChangeSupport(this);
        setTitle(editor.getTitle());
    }
    
    /**
     * ���̃G�f�B�^�� Thread �Ƃ��Ď��s�����B
     */
    public void run() {
    	
    	// TODO pack ��ɃR���|�[�l���g�փA�N�Z�X���Ă���
        pack();
        Point loc = DesignFactory.getCenterLoc(getWidth(), getHeight());
        setLocation(loc.x, loc.y);
        
        // Modal state �ɂ���
        show();
        
        // Block ���������ꂽ��l��ʒm����
        notifyValue();
    }    
    
    /**
     * �ҏW���� Stamp ��Ԃ��B
     */    
    public Object getValue() {
        return editor.getValue();
    }
    
    /**
     * �ҏW����X�^���v���Z�b�g����
     */
    public void setValue(Object val) {
        editor.setValue(val);
    }
    
    public void addPropertyChangeListener(String prop, PropertyChangeListener l) {
        boundSupport.addPropertyChangeListener(prop, l);
    }
    
    public void remopvePropertyChangeListener(String prop, PropertyChangeListener l) {
        boundSupport.removePropertyChangeListener(prop, l);
    }    
    
    public String getOkButtonText() {
        return okButtonText;
    }
    
    public void setOkButtonText(String text) {
        okButtonText = text + "(O)";
        okButton.setText(okButtonText);
        okButton.setMnemonic('O');
    }
        
    public void okButtonClicked(ActionEvent e) {
        value = getValue();
        close();
    }
    
    public void cancelButtonClicked(ActionEvent e) {
        value = null;
        close();
    }
    
    public void addStampButtonClicked(ActionEvent e) {
        value = null;
        close();
    }    
    
    private void notifyValue() {
        boundSupport.firePropertyChange("value", null, value);
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
        }
        else {
            okButton.setEnabled(false);
        }
    }
      
     /**
      * �_�C�A���O�����
      */
     public void close() {
         editor.dispose();
         setVisible(false);
         dispose();
     }
   
     /**
      * �L�����Z���E�J���e�ɓW�J�{�^���@�̃y�C���𐶐�����B
      */
     private JPanel createButtonPane(Object target) {
         
         JPanel buttonPane = new JPanel();
         buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
         buttonPane.add(Box.createHorizontalGlue());
         
         // OK �{�^���Ƃ��̃A�N�V�����𐶐�
         ActionListener action = (ActionListener) (GenericListener.create (
                                    ActionListener.class,
                                    "actionPerformed",
                                    target,
                                    "okButtonClicked"));
         okButton.addActionListener(action);
         okButton.setMnemonic('O');
         okButton.setEnabled(false);
         buttonPane.add(okButton);
      
         // Adds 5 spacing (see the design guide)
         buttonPane.add(DesignFactory.createtButtonHSpace());
      
         // Cancel button�@�Ƃ��̃A�N�V�����𐶐�
         action = (ActionListener) (GenericListener.create (
                                    ActionListener.class,
                                    "actionPerformed",
                                    target,
                                    "cancelButtonClicked"));
         cancelButton.addActionListener(action);
         cancelButton.setMnemonic('C');
         buttonPane.add(cancelButton);
      
         return buttonPane;     
    }
}