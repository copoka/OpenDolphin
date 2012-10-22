package open.dolphin.client;

import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import open.dolphin.helper.GridBagBuilder;

import open.dolphin.project.ProjectStub;

/**
 * AreaNetWorkSettingPanel
 *
 * @author Kazushi Minagawa Digital Globe, Inc.
 *
 */
public class AreaNetWorkSettingPanel extends AbstractSettingPanel {
    
    private static final String ID = "areaNetwork";
    private static final String TITLE = "�n��A�g";
    private static final String ICON = "web_24.gif";
    
    // �n��A�g�p�R���|�[�l���g
    private JRadioButton joinAreaNetwork;
    private JRadioButton noJoinAreaNetwork;
    private JComboBox areaNetworkCombo;
    private JTextField facilityIdField;
    private JTextField creatorIdField;
    private NameValuePair[] networkProjects;
    
    private NetworkModel model;
    
    private StateMgr stateMgr;
    
    public AreaNetWorkSettingPanel() {
        setId(ID);
        this.setTitle(TITLE);
        this.setIcon(ICON);
    }
    
    /**
     * �n��A�g�ݒ���J�n����B
     */
    public void start() {
        
        //
        // ���f���𐶐�����
        //
        model = new NetworkModel();
        
        //
        // GUI�𐶐�����
        //
        initComponents();
        
        //
        // populate
        //
        model.populate(getProjectStub());
                
    }
    
    /**
     * �ۑ�����B
     */
    public void save() {
        model.restore(getProjectStub());
    }
    
    /**
     * GUI�𐶐�����
     */
    private void initComponents() {
        
        ButtonGroup bg = new ButtonGroup();
        joinAreaNetwork = GUIFactory.createRadioButton("�Q������", null, bg);
        noJoinAreaNetwork = GUIFactory.createRadioButton("�Q�����Ȃ�", null, bg);
        networkProjects = ClientContext.getNameValuePair("areaNetwork.list");
        areaNetworkCombo = new JComboBox(networkProjects);
        facilityIdField = GUIFactory.createTextField(20, null, null, null);
        creatorIdField = GUIFactory.createTextField(20, null, null, null);
               
        // �n��A�g���
        GridBagBuilder gbl = new GridBagBuilder("�n��A�g");
        gbl.add(GUIFactory.createRadioPanel(new JRadioButton[]{noJoinAreaNetwork,joinAreaNetwork}),0, 0, 2, 1, GridBagConstraints.CENTER);
        gbl.add(new JLabel("�v���W�F�N�g:"), 0, 1, GridBagConstraints.EAST);
        gbl.add(areaNetworkCombo,	   1, 1, GridBagConstraints.WEST);
        gbl.add(new JLabel("�A�g�p��Ë@��ID:"), 0, 2, GridBagConstraints.EAST);
        gbl.add(facilityIdField, 	      1, 2, GridBagConstraints.WEST);
        gbl.add(new JLabel("�A�g�p���[�UID:"),  0, 3, GridBagConstraints.EAST);
        gbl.add(creatorIdField, 	      1, 3, GridBagConstraints.WEST);
        JPanel content = gbl.getProduct();
        
        // �S�̂����C�A�E�g����
        gbl = new GridBagBuilder();
        gbl.add(content,        0, 0, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gbl.add(new JLabel(""), 0, 1, GridBagConstraints.BOTH,       1.0, 1.0);
        
        setUI(gbl.getProduct());
       
    }
    
    public void connect() {
        
        stateMgr = new StateMgr();
        
        // �n��A�g�Q���{�^����ActionListener�𐶐�����
        ActionListener alArea = ProxyActionListener.create(stateMgr, "controlJoinArea");
        joinAreaNetwork.addActionListener(alArea);
        noJoinAreaNetwork.addActionListener(alArea);
        
        // �n��A�g���̃��X�i�𐶐�����
        areaNetworkCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    stateMgr.checkState();
                }
            }
        });
        
        // DocumentListener
        DocumentListener dl = ProxyDocumentListener.create(stateMgr, "checkState");
        facilityIdField.getDocument().addDocumentListener(dl);
        creatorIdField.getDocument().addDocumentListener(dl);
        
        //
        // IME OFF FocusAdapter
        //
        facilityIdField.addFocusListener(AutoRomanListener.getInstance());
        creatorIdField.addFocusListener(AutoRomanListener.getInstance());
        
        stateMgr.controlJoinArea();
    }
    
    class NetworkModel {
        
        public void populate(ProjectStub stub) {
            
            boolean join = stub.getJoinAreaNetwork();
            joinAreaNetwork.setSelected(join);
            noJoinAreaNetwork.setSelected(! join);
            
            String val = stub.getAreaNetworkName();
            if (val != null) {
                for (int i = 0; i < networkProjects.length; i++) {
                    if (val.equals(networkProjects[i].getValue())) {
                        areaNetworkCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
            
            val = stub.getAreaNetworkFacilityId();
            val = val != null ? val : "";
            facilityIdField.setText(val);
            
            val = stub.getAreaNetworkCreatorId();
            val = val != null ? val : "";
            creatorIdField.setText(val);
            
            connect();
        }
        
        public void restore(ProjectStub stub) {
            
            boolean join = joinAreaNetwork.isSelected();
            stub.setJoinAreaNetwork(join);
            
            NameValuePair pair = (NameValuePair) areaNetworkCombo.getSelectedItem();
            stub.setAreaNetworkName(pair.getValue());
            
            String val = facilityIdField.getText().trim();
            if (!val.equals("")) {
                stub.setAreaNetworkFacilityId(val);
            }
            
            val = creatorIdField.getText().trim();
            if (!val.equals("")) {
                stub.setAreaNetworkCreatorId(val);
            }
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
        
        public void controlJoinArea() {
            boolean join = joinAreaNetwork.isSelected();
            areaNetworkCombo.setEnabled(join);
            facilityIdField.setEnabled(join);
            creatorIdField.setEnabled(join);
            this.checkState();
        }
        
        private boolean isValid() {
            if (joinAreaNetwork.isSelected()) {
                boolean projOk = areaNetworkCombo.getSelectedIndex() != 0 ? true : false;
                boolean facilityOk = facilityIdField.getText().trim().equals("") ? false : true;
                boolean creatorOk = creatorIdField.getText().trim().equals("") ? false : true;
                return (projOk && facilityOk && creatorOk) ? true : false;
            }
            return true;
        }
    }
}
