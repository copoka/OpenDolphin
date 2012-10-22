/*
 * ClaimSettingPanel.java
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

import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;

import open.dolphin.project.ProjectStub;

/**
 * ClaimSettingPanel
 *
 * @author Kazushi Minagawa Digital Globe, Inc.
 *
 */
public class ClaimSettingPanel extends AbstractSettingPanel {
    
    // GUI staff
    private JRadioButton sendClaimYes;
    private JRadioButton sendClaimNo;
    private JComboBox claimHostCombo;
    private JTextField claimAddressField;
    private JTextField claimPortField;
    private JCheckBox useAsPVTServer;
    
    /** ��ʃ��f�� */
    private ClaimModel model;
    
    private StateMgr stateMgr;
    
    
    public ClaimSettingPanel() {
    }
    
    /**
     * GUI �y�� State �𐶐�����B
     */
    public void start() {
        
        //
        // ���f���𐶐�������������
        //
        model = new ClaimModel();
        model.populate(getProjectStub());
        
        //
        // GUI���\�z����
        //
        initComponents();
        
        //
        // bind ����
        //
        bindModelToView();
    }
    
    /**
     * �ݒ�l��ۑ�����B
     */
    public void save() {
        bindViewToModel();
        model.restore(getProjectStub());
    }
    
    /**
     * GUI���\�z����
     */
    private void initComponents() {
        
        //
        // �f�Ís�ב��M�{�^��
        //
        ButtonGroup bg1 = new ButtonGroup();
        sendClaimYes = GUIFactory.createRadioButton("���M����", null, bg1);
        sendClaimNo = GUIFactory.createRadioButton("���M���Ȃ�", null, bg1);
        
        //
        // �z�X�g���A�A�h���X�A�|�[�g�ԍ�
        //
        String[] hostNames = ClientContext.getStringArray("settingDialog.claim.hostNames");
        claimHostCombo = new JComboBox(hostNames);
        claimAddressField = GUIFactory.createTextField(10, null, null, null);
        claimPortField = GUIFactory.createTextField(5, null, null, null);
        
        //
        // ��t��M�{�^��
        //
        useAsPVTServer = GUIFactory.createCheckBox("���̃}�V����ORCA����̎�t������M����", null);
        useAsPVTServer.setToolTipText("���̃}�V����ORCA����̎�t������M����ꍇ�̓`�F�b�N���Ă�������");
        
        //
        // CLAIM�i�����j���M���
        //
        GridBagBuilder gbl = new GridBagBuilder("CLAIM�i�����f�[�^�j���M");
        int row = 0;
        JLabel label = new JLabel("�f�Ís�ב��M:");
        JPanel panel = GUIFactory.createRadioPanel(new JRadioButton[]{sendClaimYes,sendClaimNo});
        gbl.add(label, 0, row, GridBagConstraints.EAST);
        gbl.add(panel, 1, row, GridBagConstraints.CENTER);
        JPanel sendClaim = gbl.getProduct();
        
        // ���Z�R�����
        gbl = new GridBagBuilder("���Z�R�����");
        row = 0;
        label = new JLabel("�@��:");
        gbl.add(label,          0, row, GridBagConstraints.EAST);
        gbl.add(claimHostCombo, 1, row, GridBagConstraints.WEST);
        
        row++;
        label = new JLabel("IP�A�h���X:");
        gbl.add(label,             0, row, GridBagConstraints.EAST);
        gbl.add(claimAddressField, 1, row, GridBagConstraints.WEST);
        
        row++;
        label = new JLabel("�|�[�g�ԍ�:");
        gbl.add(label,          0, row, GridBagConstraints.EAST);
        gbl.add(claimPortField, 1, row, GridBagConstraints.WEST);
        JPanel port = gbl.getProduct();
        
        // ���Z�R������̎�t��M
        gbl = new GridBagBuilder("��t���̎�M");
        gbl.add(useAsPVTServer, 0, 0, GridBagConstraints.CENTER);
        JPanel pvt = gbl.getProduct();
        
        // �S�̃��C�A�E�g
        gbl = new GridBagBuilder();
        gbl.add(sendClaim, 0, 0, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gbl.add(port,      0, 1, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gbl.add(pvt,       0, 2, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gbl.add(new JLabel(""), 0, 3, GridBagConstraints.BOTH,  1.0, 1.0);
        setUI(gbl.getProduct());

        connect();       
    }
    
    /**
     * ���X�i��ڑ�����B
     */
    private void connect() {
        
        stateMgr = new StateMgr();
        
        // DocumentListener
        DocumentListener dl = ProxyDocumentListener.create(stateMgr, "checkState");
        claimAddressField.getDocument().addDocumentListener(dl);
        claimPortField.getDocument().addDocumentListener(dl);
        
        //
        // IME OFF FocusAdapter
        //
        claimAddressField.addFocusListener(AutoRomanListener.getInstance());
        claimPortField.addFocusListener(AutoRomanListener.getInstance());
        
        // �A�N�V�������X�i
        ActionListener al = ProxyActionListener.create(stateMgr, "controlClaim");
        sendClaimYes.addActionListener(al);
        sendClaimNo.addActionListener(al);
    }
    
    /**
     * ModelToView
     */
    private void bindModelToView() {
        //
        // �f�Ís�ב��M��I������
        //
        boolean sending = model.isSendClaim();
        sendClaimYes.setSelected(sending);
        sendClaimNo.setSelected(!sending);
        claimPortField.setEnabled(sending);
        
        //
        // CLAIM �z�X�g��IP�A�h���X��ݒ肷��
        //
        String val = model.getClaimAddress();
        val = val != null ? val : "";
        claimAddressField.setText(val);
        
        //
        // CLAIM �z�X�g�̃|�[�g�ԍ���ݒ肷��
        //
        val = String.valueOf(model.getClaimPort());
        val = val != null ? val : "";
        claimPortField.setText(val);
        
        //
        // �z�X�g��
        //
        val = model.getClaimHostName();
        val = val != null ? val : "";
        claimHostCombo.setSelectedItem(val);
        
        //
        // ��t��M
        //
        useAsPVTServer.setSelected(model.isUseAsPVTServer());
    }
    
    /**
     * ViewToModel
     */
    private void bindViewToModel() {
        //
        // �f�Ís�ב��M�A���ۑ����A�C�����A�a�����M
        // �̐ݒ��ۑ�����
        //
        model.setSendClaim(sendClaimYes.isSelected());
        
        //
        // �z�X�g����ۑ�����
        //
        String val = (String)claimHostCombo.getSelectedItem();
        model.setClaimHostName(val);
        
        //
        // IP�A�h���X��ۑ�����
        //
        val = claimAddressField.getText().trim();
        model.setClaimAddress(val);
        
        //
        // �|�[�g�ԍ���ۑ�����
        //
        val = claimPortField.getText().trim();
        try {
            int port = Integer.parseInt(val);
            model.setClaimPort(port);
            
        } catch (NumberFormatException e) {
            model.setClaimPort(5001);
        }
        
        //
        // ��t��M��ۑ�����
        //
        model.setUseAsPVTServer(useAsPVTServer.isSelected());
    }
    
    /**
     * ��ʂ��o��N���X�B
     */
    class ClaimModel {
        
        private boolean sendClaim;
        private String claimHostName;
        private String claimAddress;
        private int claimPort;
        private boolean useAsPvtServer;
        
        public void populate(ProjectStub stub) {
            
            // �f�Ís�ב��M
            setSendClaim(stub.getSendClaim());
            
            // CLAIM �z�X�g��IP�A�h���X
            setClaimAddress(stub.getClaimAddress());
            
            // CLAIM �z�X�g�̃|�[�g�ԍ�
            setClaimPort(stub.getClaimPort());
            
            // �z�X�g��
            setClaimHostName(stub.getClaimHostName());
            
            // ��t��M
            setUseAsPVTServer(stub.getUseAsPVTServer());
        }
        
        public void restore(ProjectStub stub) {
            
            // �f�Ís�ב��M
            stub.setSendClaim(isSendClaim());
            
            // CLAIM �z�X�g��IP�A�h���X
            stub.setClaimAddress(getClaimAddress());
            
            // CLAIM �z�X�g�̃|�[�g�ԍ�
            stub.setClaimPort(getClaimPort());
            
            // �z�X�g��
            stub.setClaimHostName(getClaimHostName());
            
            // ��t��M
            stub.setUseAsPVTServer(isUseAsPVTServer());
        }
        
        public boolean isSendClaim() {
            return sendClaim;
        }
        
        public void setSendClaim(boolean sendClaim) {
            this.sendClaim = sendClaim;
        }
        
        public boolean isUseAsPVTServer() {
            return useAsPvtServer;
        }
        
        public void setUseAsPVTServer(boolean useAsPvtServer) {
            this.useAsPvtServer = useAsPvtServer;
        }
        
        public String getClaimHostName() {
            return claimHostName;
        }
        
        public void setClaimHostName(String claimHostName) {
            this.claimHostName = claimHostName;
        }
        
        public String getClaimAddress() {
            return claimAddress;
        }
        
        public void setClaimAddress(String claimAddress) {
            this.claimAddress = claimAddress;
        }
        
        public int getClaimPort() {
            return claimPort;
        }
        
        public void setClaimPort(int claimPort) {
            this.claimPort = claimPort;
        }
    }
    
    class StateMgr {
        
        public void checkState() {
            
            AbstractSettingPanel.State newState = isValid()
            ? AbstractSettingPanel.State.VALID_STATE
                    : AbstractSettingPanel.State.INVALID_STATE;
            if (newState != state) {
                setState(newState);
            }
        }
        
        public void controlClaim() {
            
            //
            // �f�Ís�ׂ̑��M���s���ꍇ�̂�
            // ���ۑ��A�C���A�a�����M�A�z�X�g�I���A�|�[�g���A�N�e�B�u�ɂȂ�
            //
            boolean b = sendClaimYes.isSelected();
            
            claimHostCombo.setEnabled(b);
            claimPortField.setEnabled(b);
            
            this.checkState();
        }
        
        private boolean isValid() {
            
            //
            // �f�Ís�ׂ̑��M���s���ꍇ�̓A�h���X�ƃ|�[�g�̒l���K�v�ł���
            //
            if (sendClaimYes.isSelected()) {
                boolean claimAddrOk = (claimAddressField.getText().trim().equals("") == false) ? true : false;
                boolean claimPortOk = (claimPortField.getText().trim().equals("") == false) ? true : false;
                return (claimAddrOk && claimPortOk) ? true : false;
            }
            
            return true;
        }
    }
}
