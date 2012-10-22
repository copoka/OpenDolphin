/*
 * ChangePasswordService.java
 * Copyright (C) 2004 Digital Globe, Inc. All rights reserved.
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
import javax.swing.event.*;

import open.dolphin.dao.*;
import open.dolphin.infomodel.*;
import open.dolphin.plugin.*;
import open.dolphin.project.*;

import java.awt.*;
import java.awt.event.*;

/**
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class ChangePasswordService extends AbstractFramePlugin {
    
    private int DEFAULT_WIDTH   = 460;
    private int DEFAULT_HEIGHT  = 240;
    
    protected JButton okButton;
    
    /** Creates a new instance of AddUserService */
    public ChangePasswordService() {
    }
    
    public void initComponent() {
        
        ChangePasswordPanel cp = new ChangePasswordPanel();
        centerFrame(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT), cp);
        
        this.getRootPane().setDefaultButton(okButton);
    }
    
    protected class ChangePasswordPanel extends JPanel {
        
        private JTextField uid;                 // ���p��ID
        private JPasswordField userPassword;    // �p�X���[�h
        private JPasswordField userPassword2;   // �p�X���[�h
        private JTextField sn;                  // ��
        private JTextField givenName;           // ��
        private String cn;                      // ����(sn & ' ' & givenName),
        private JTextField licenceCode;         // �E��(MML0026)
        private JTextField facilityId;          // ��Ë@�փR�[�h(ORCA��Ë@�փR�[�h)
        private JComboBox licenseCombo;
        private JComboBox departmentCombo;      // �f�É�(MML0028)
        private String authority;               // LAS�ɑ΂��錠��(admin:�Ǘ���,user:��ʗ��p��)
        //JTextField mail;                      // ���[���A�h���X
        //JTextField description;
        //private JButton okButton;
        private JButton cancelButton;
        private boolean ok;
        
        public ChangePasswordPanel() {
        
            //JPanel panel = new JPanel();
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            // DocumentListener
            DocumentListener dl = new DocumentListener() {

                public void changedUpdate(DocumentEvent e) {
                }

                public void insertUpdate(DocumentEvent e) {
                    checkButton();
                }

                public void removeUpdate(DocumentEvent e) {
                    checkButton();
                }
            };

            Dimension dim = new Dimension(180, 21);
            
            UserProfileEntry oldProfile = Project.getUserProfileEntry();

            uid = new JTextField();
            uid.setEditable(false);
            uid.setText(oldProfile.getUserId());
            this.add(createItemPanel("�@���[�U�@ID (���p�p��):", uid, dl, dim));
            this.add(Box.createVerticalStrut(5));

            userPassword = new JPasswordField();
            //userPassword.setText(profile.getPasswd());
            userPassword.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (isValidPassword1()) {
                        userPassword2.requestFocus();
                    }
                }
            });
            this.add(createItemPanel("�V�p�X���[�h (���p�p��):", userPassword, dl, dim));
            this.add(Box.createVerticalStrut(5));

            userPassword2 = new JPasswordField();
            //userPassword2.setText(profile.getPasswd());
            userPassword2.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (isValidPassword2()) {
                        okButton.doClick();
                    }
                    /*if (passwordOk()) {
                        okButton.doClick();

                    } else {
                        showWarning("�p�X���[�h����v���Ă��܂���B");
                        userPassword.requestFocus();
                    }*/
                }
            });
            this.add(createItemPanel("�@�@�@�V�p�X���[�h�m�F:", userPassword2, dl, dim));
            this.add(Box.createVerticalStrut(5));

            sn = new JTextField();
            sn.setText(oldProfile.getSirName());
            sn.setEditable(false);

            givenName = new JTextField();
            givenName.setText(oldProfile.getGivenName());
            givenName.setEditable(false);
            
            this.add(createNamePanel(sn, givenName, null, dim));
            this.add(Box.createVerticalStrut(5));

            String[] code = ClientContext.getStringArray("settingDialog.license.code");
            int index = getIndex(code, oldProfile.getLicenseCode());
            String[] str = ClientContext.getStringArray("settingDialog.license.list");
            licenseCombo = new JComboBox(str);
            licenseCombo.setSelectedIndex(index);
            licenseCombo.setEnabled(false);

            code = ClientContext.getStringArray("settingDialog.department.code");
            index = getIndex(code, oldProfile.getDepartmentId());
            str = ClientContext.getStringArray("settingDialog.department.list");
            departmentCombo = new JComboBox(str);
            departmentCombo.setSelectedIndex(index);
            departmentCombo.setEnabled(false);
            
            this.add(createCodePanel(licenseCombo, departmentCombo, dim));
            this.add(Box.createVerticalStrut(17));

            ActionListener al = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    modifyUserEntry();
                }
            };

            okButton = new JButton("�� �X");
            okButton.addActionListener(al);
            //okButton.setMnemonic('M');
            okButton.setEnabled(false);
            cancelButton = new JButton("����(C)");
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    stop();
                }
            });
            cancelButton.setMnemonic('C');
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(Box.createHorizontalGlue());
            p.add(okButton);
            p.add(Box.createHorizontalStrut(5));
            p.add(cancelButton);
            this.add(p);

            this.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        }
        
        private boolean isValidPassword1() {
            
            String passwd = new String(userPassword.getPassword());
            int len = passwd.length();
            boolean ok = (len >= 6 && len <= 8) ? true : false;
            
            if (! ok) {
                showWarning("�p�X���[�h�͔��p�̉p������6�����ȏ�8�����ȓ��ɂ��Ă��������B");
            }
            
            return ok;
        } 
        
        private boolean isValidPassword2() {
            
            String passwd2 = new String(userPassword2.getPassword());
            String passwd = new String(userPassword.getPassword());
            int len = passwd2.length();
            boolean ok = (len >= 6 && len <= 8) ? true : false;
            
            if (! ok) {
                showWarning("�p�X���[�h�͔��p�̉p������6�����ȏ�8�����ȓ��ɂ��Ă��������B");
            } else if (! passwd2.equals(passwd)) {
                showWarning("�p�X���[�h����v���Ă��܂���B");
                ok = false;
            } else {
                ok = true;
            }
            
            return ok;
        }         
        
        private boolean passwordOk() {
            String passwd = new String(userPassword.getPassword());
            String passwd2 = new String(userPassword2.getPassword());
            return ( (! passwd.equals("")) && (! passwd2.equals(""))) ? true : false;
            //return ( (! passwd.equals("")) && (! passwd2.equals("")) && passwd.equals(passwd2)) ? true : false;
        }

        private void modifyUserEntry() {
            
            if (! isValidPassword2()) {
                return;
            }
            
            UserProfileEntry profile = Project.getUserProfileEntry();
            
            String password = new String(userPassword.getPassword());
            if (! password.equals("")) {
                profile.setPasswd(password);
            
            }else {
                profile.setPasswd(profile.getPasswd());
            }
            
            //ClientContext.getLogger().info(profile.toString());

            UserProfileDao dao = (UserProfileDao)DaoFactory.create(this, "dao.userProfile");
            
            if (dao != null) {

                boolean ret = dao.changePassword(profile);

                if (! ret) {
                    showWarning(dao.getErrorMessage());
                    //ClientContext.getLogger().warning(dao.getErrorMessage());
                
                } else {
                    showWarning("�p�X���[�h��ύX���܂����B");
                    okButton.setEnabled(false);
                }
            }
        }

        private void checkButton() {

            boolean newOk = passwordOk() ? true : false;

            if (ok != newOk) {
                ok = newOk;
                okButton.setEnabled(ok);
            }   
        }
    }    
        
    private JPanel createItemPanel(String itemName, JTextField tf, DocumentListener dl, Dimension dim) {
        
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(itemName));
        p.add(Box.createHorizontalStrut(7));
        p.add(tf);
        tf.getDocument().addDocumentListener(dl);
        tf.setPreferredSize(dim);
        return p;
    }
    
    private JPanel createItemPanel(String itemName, JComboBox cb, Dimension dim) {
        
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(itemName));
        p.add(Box.createHorizontalStrut(7));
        p.add(cb);
        cb.setPreferredSize(dim);
        return p;
    }
    
    private JPanel createNamePanel(JTextField cn, JTextField gn, DocumentListener dl, Dimension dim) {
        
        cn.setPreferredSize(dim);
        gn.setPreferredSize(dim);
        cn.getDocument().addDocumentListener(dl);
        gn.getDocument().addDocumentListener(dl);
        
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel("��:"));
        p.add(Box.createHorizontalStrut(7));
        p.add(cn);
        p.add(Box.createHorizontalStrut(20));
        p.add(new JLabel("��:"));
        p.add(Box.createHorizontalStrut(7));
        p.add(gn);
        
        return p;
    }
    
    private JPanel createCodePanel(JComboBox license, JComboBox dept, Dimension dim) {
        license.setPreferredSize(dim);
        dept.setPreferredSize(dim);    
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel("��Î��i:"));
        p.add(Box.createHorizontalStrut(7));
        p.add(license);
        p.add(Box.createHorizontalStrut(20));
        p.add(new JLabel("�f�É�:"));
        p.add(Box.createHorizontalStrut(7));
        p.add(dept);
        
        return p;
    }
    
    private void showWarning(String msg) {
        
        JOptionPane.showMessageDialog(null,
                                     msg,
                                     "���[�U�o�^ - " + ClientContext.getString("application.title"),
                                     JOptionPane.WARNING_MESSAGE);
    }
    
    private int getIndex(String[] code, String val) {
        
        int index = 0;
        
        for (int i = 0; i < code.length; i++) {
            if (code[i].equals(val)) {
                index = i;
                break;
            }
        }
        return index;
    }
}
