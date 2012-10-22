package open.dolphin.client;

import java.awt.GridBagConstraints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.im.InputSubset;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import open.dolphin.helper.GridBagBuilder;

import open.dolphin.infomodel.DepartmentModel;
import open.dolphin.infomodel.FacilityModel;
import open.dolphin.infomodel.LicenseModel;
import open.dolphin.infomodel.UserModel;

/**
 * AccountInfoPanel
 *
 * @author Minagawa,Kazushi
 *
 */
public class AccountInfoPanel extends JPanel {
    
    private static final long serialVersionUID = -7695417342865642933L;
    
    public static final String VALID_INFO_PROP = "validInfoProp";
    
    private static final String MEMBER_TYPE = "ASP_TESTER";
    
    //private JTextField facilityId;
    private JTextField facilityName;
    private JTextField zipField1;
    private JTextField zipField2;
    private JTextField addressField;
    private JTextField areaField;
    private JTextField cityField;
    private JTextField numberField;
    private JTextField urlField;
    private JTextField adminId;
    private JPasswordField adminPassword1;
    private JPasswordField adminPassword2;
    private JTextField adminSir;
    private JTextField adminGiven;
    private JComboBox licenseCombo;
    private JComboBox deptCombo;
    private LicenseModel[] licenses;
    private DepartmentModel[] depts;
    private JTextField emailField;
    private int[] userIdLength;
    private int[] passwordLength;
    
    private UserModel adminModel;
    private boolean validInfo;
    private PropertyChangeSupport boundSupport = new PropertyChangeSupport(this);
    
    
    public AccountInfoPanel() {
        initialize();
        connect();
        setModel(adminModel);
    }
    
    public UserModel getModel() {
        adminModel = new UserModel();
        return getAdminUser(adminModel);
    }
    
    public void setModel(UserModel adminModel) {
        this.adminModel = adminModel;
    }
    
    public boolean isValidInfo() {
        return validInfo;
    }
    
    public void setValidInfo(boolean b) {
        boolean old = this.validInfo;
        this.validInfo = b;
        boundSupport.firePropertyChange(VALID_INFO_PROP, old, this.validInfo);
    }
    
    public void addValidInfoPropertyListener(PropertyChangeListener l) {
        boundSupport.addPropertyChangeListener(VALID_INFO_PROP, l);
    }
    
    public void removeValidInfoPropertyListener(PropertyChangeListener l) {
        boundSupport.removePropertyChangeListener(VALID_INFO_PROP, l);
    }
    
    private UserModel getAdminUser(UserModel admin) {
        
        FacilityModel facility = new FacilityModel();
        admin.setFacilityModel(facility);
        
        // ��Ë@��ID
        //String fid = facilityId.getText().trim();
        //facility.setFacilityId(fid);
        
        // ��Ë@�֖�
        facility.setFacilityName(facilityName.getText().trim());
        
        // �X�֔ԍ�
        StringBuilder buf = new StringBuilder();
        buf.append(zipField1.getText().trim());
        buf.append("-");
        buf.append(zipField2.getText().trim());
        facility.setZipCode(buf.toString());
        
        // �Z��
        facility.setAddress(addressField.getText().trim());
        
        // �d�b
        buf = new StringBuilder();
        buf.append(areaField.getText().trim());
        buf.append("-");
        buf.append(cityField.getText().trim());
        buf.append("-");
        buf.append(numberField.getText().trim());
        facility.setTelephone(buf.toString());
        
        // URL
        facility.setUrl(urlField.getText().trim());
        
        // �o�^�� MemberTpe
        Date date = new Date();
        facility.setRegisteredDate(date);
        facility.setMemberType(MEMBER_TYPE);
        
        // �Ǘ��Ҋ�{���
        admin.setUserId(adminId.getText().trim());
        admin.setPassword(new String(adminPassword1.getPassword()));
        admin.setSirName(adminSir.getText().trim());
        admin.setGivenName(adminGiven.getText().trim());
        admin.setCommonName(admin.getSirName() + " " + admin.getGivenName());
        
        // ��Î��i
        int index = licenseCombo.getSelectedIndex();
        admin.setLicenseModel(licenses[index]);
        
        // �f�É�
        index = deptCombo.getSelectedIndex();
        admin.setDepartmentModel(depts[index]);
        
        // Email
        admin.setEmail(emailField.getText().trim());
        
        // MemberTpe
        admin.setMemberType(MEMBER_TYPE);
        
        // �o�^��
        admin.setRegisteredDate(date);
        
        return admin;
    }
    
    private void initialize() {
        
        FocusAdapter imeOn = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                JTextField tf = (JTextField)event.getSource();
                tf.getInputContext().setCharacterSubsets(new Character.Subset[] {InputSubset.KANJI});
            }
        };
        
        FocusAdapter imeOff = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                JTextField tf = (JTextField)event.getSource();
                tf.getInputContext().setCharacterSubsets(null);
            }
        };
        
        facilityName = GUIFactory.createTextField(30, null, imeOn, null);
        zipField1 = GUIFactory.createTextField(3, null, imeOff, null);
        zipField2 = GUIFactory.createTextField(3, null, imeOff, null);
        addressField = GUIFactory.createTextField(30, null, imeOn, null);
        areaField = GUIFactory.createTextField(3, null, imeOff, null);
        cityField = GUIFactory.createTextField(3, null, imeOff, null);
        numberField = GUIFactory.createTextField(3, null, imeOff, null);
        urlField = GUIFactory.createTextField(30, null, imeOff, null);
        
        adminId = GUIFactory.createTextField(10, null, imeOff, null);
        adminPassword1 = GUIFactory.createPassField(10, null, imeOff, null);
        adminPassword2 = GUIFactory.createPassField(10, null, imeOff, null);
        adminSir = GUIFactory.createTextField(10, null, imeOn, null);
        adminGiven = GUIFactory.createTextField(10, null, imeOn, null);
        emailField =  GUIFactory.createTextField(15, null, imeOff, null);
        
        String digitPattern = ClientContext.getString("addUser.pattern.digit");
        RegexConstrainedDocument zip1Doc = new RegexConstrainedDocument(digitPattern);
        zipField1.setDocument(zip1Doc);
        RegexConstrainedDocument zip2Doc = new RegexConstrainedDocument(digitPattern);
        zipField2.setDocument(zip2Doc);
        RegexConstrainedDocument areaDoc = new RegexConstrainedDocument(digitPattern);
        areaField.setDocument(areaDoc);
        RegexConstrainedDocument cityDoc = new RegexConstrainedDocument(digitPattern);
        cityField.setDocument(cityDoc);
        RegexConstrainedDocument numberDoc = new RegexConstrainedDocument(digitPattern);
        numberField.setDocument(numberDoc);
        
        String pattern = ClientContext.getString("addUser.pattern.idPass");
        RegexConstrainedDocument userIdDoc = new RegexConstrainedDocument(pattern);
        adminId.setDocument(userIdDoc);
        adminId.setToolTipText(pattern);
        
        RegexConstrainedDocument passwordDoc1 = new RegexConstrainedDocument(pattern);
        adminPassword1.setDocument(passwordDoc1);
        adminPassword1.setToolTipText(pattern);
        RegexConstrainedDocument passwordDoc2 = new RegexConstrainedDocument(pattern);
        adminPassword2.setDocument(passwordDoc2);
        adminPassword1.setToolTipText(pattern);
        
        pattern = ClientContext.getString("addUser.pattern.email");
        RegexConstrainedDocument emailDoc = new RegexConstrainedDocument(pattern);
        emailField.setDocument(emailDoc);
        emailField.setToolTipText("�܂�Ԃ��{��ID�𑗐M���܂�");
        
        licenses = ClientContext.getLicenseModel();
        licenseCombo = new JComboBox(licenses);
        
        depts = ClientContext.getDepartmentModel();
        deptCombo = new JComboBox(depts);
        
        GridBagBuilder gbl = new GridBagBuilder("�{�ݏ�� - URL�ȊO�̑S�Ă̍��ڂ��K�v�ł�");
        JLabel label = null;
        StringBuilder sb = null;
        
        int x = 0;
        int y = 0;
        label = new JLabel("��Ë@�֖�:", SwingConstants.RIGHT);
        gbl.add(label,        x,    y, GridBagConstraints.EAST);
        gbl.add(facilityName, x+1,  y, GridBagConstraints.WEST);
        
        x = 0;
        y += 1;
        label = new JLabel("�X�֔ԍ�:", SwingConstants.RIGHT);
        gbl.add(label, 													x, 	y, GridBagConstraints.EAST);
        gbl.add(GUIFactory.createZipCodePanel(zipField1, zipField2), 	x+1, y, GridBagConstraints.WEST);
        
        x = 0;
        y += 1;
        label = new JLabel("�Z�@��:", SwingConstants.RIGHT);
        gbl.add(label, x, y, GridBagConstraints.EAST);
        gbl.add(addressField, x+1, y, 2, 1, GridBagConstraints.WEST);
        
        x = 0;
        y += 1;
        label = new JLabel("�d�b�ԍ�:", SwingConstants.RIGHT);
        gbl.add(label, x, y, GridBagConstraints.EAST);
        gbl.add(GUIFactory.createPhonePanel(areaField, cityField, numberField), x+1, y, GridBagConstraints.WEST);
        
        x = 0;
        y += 1;
        label = new JLabel("URL:", SwingConstants.RIGHT);
        gbl.add(label, x, y, GridBagConstraints.EAST);
        gbl.add(urlField, x+1, y, GridBagConstraints.WEST);
        
        JPanel facilityPanel = gbl.getProduct();
        this.add(facilityPanel);
        
        // �Ǘ��҃p�l��
        x = 0;
        y = 0;
        userIdLength = ClientContext.getIntArray("addUser.userId.length");
        sb = new StringBuilder();
        sb.append("�Ǘ��҃��O�C��ID(���p�p���L)");
        sb.append(userIdLength[0]);
        sb.append("~");
        sb.append(userIdLength[1]);
        sb.append("����):");
        label = new JLabel(sb.toString(), SwingConstants.RIGHT);
        gbl = new GridBagBuilder("���̈�Ë@�ւ�OpenDolphin�Ǘ��ҏ�� - �S�Ă̍��ڂ��K�v�ł�");
        gbl.add(label, x, y, GridBagConstraints.EAST);
        gbl.add(adminId, x+1, y, GridBagConstraints.WEST);
        
        x = 0;
        y += 1;
        passwordLength = ClientContext.getIntArray("addUser.password.length");
        sb = new StringBuilder();
        sb.append("�p�X���[�h(���p�p���L)");
        sb.append(passwordLength[0]);
        sb.append("~");
        sb.append(passwordLength[1]);
        sb.append("����):");
        label = new JLabel(sb.toString(), SwingConstants.RIGHT);
        gbl.add(label,           x,     y, GridBagConstraints.EAST);
        gbl.add(adminPassword1,  x + 1, y, GridBagConstraints.WEST);
        label = new JLabel("�m�F:", SwingConstants.RIGHT);
        gbl.add(label,           x + 2, y, GridBagConstraints.EAST);
        gbl.add(adminPassword2,  x + 3, y, GridBagConstraints.WEST);
        
        x = 0;
        y += 1;
        label = new JLabel("���i�����j:", SwingConstants.RIGHT);
        gbl.add(label, x, y, GridBagConstraints.EAST);
        gbl.add(adminSir, x+ 1, y, GridBagConstraints.WEST);
        label = new JLabel("���i�����j:", SwingConstants.RIGHT);
        gbl.add(label, x+2, y, GridBagConstraints.EAST);
        gbl.add(adminGiven, x+ 3, y, GridBagConstraints.WEST);
        
        x = 0;
        y +=1;
        label = new JLabel("��Î��i:", SwingConstants.RIGHT);
        gbl.add(label, x, y, GridBagConstraints.EAST);
        gbl.add(licenseCombo,x+ 1, y, GridBagConstraints.WEST);
        label = new JLabel("�f�É�:", SwingConstants.RIGHT);
        gbl.add(label, x+2, y, GridBagConstraints.EAST);
        gbl.add(deptCombo,x+ 3, y, GridBagConstraints.WEST);
        
        x = 0;
        y +=1;
        label = new JLabel("�d�q���[��:", SwingConstants.RIGHT);
        gbl.add(label, x, y, GridBagConstraints.EAST);
        gbl.add(emailField, x+1, y, 1, 1, GridBagConstraints.WEST);
        
        JPanel adminPanel = gbl.getProduct();
        
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(adminPanel);
    }
    
    private void connect() {
        
        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                checkValidInfo();
            }
            public void removeUpdate(DocumentEvent e) {
                checkValidInfo();
            }
            public void changedUpdate(DocumentEvent e) {
            }
        };
        
        facilityName.getDocument().addDocumentListener(dl);
        zipField1.getDocument().addDocumentListener(dl);
        zipField2.getDocument().addDocumentListener(dl);
        addressField.getDocument().addDocumentListener(dl);
        areaField.getDocument().addDocumentListener(dl);
        cityField.getDocument().addDocumentListener(dl);
        numberField.getDocument().addDocumentListener(dl);
        adminId.getDocument().addDocumentListener(dl);
        adminPassword1.getDocument().addDocumentListener(dl);
        adminSir.getDocument().addDocumentListener(dl);
        adminGiven.getDocument().addDocumentListener(dl);
        emailField.getDocument().addDocumentListener(dl);
    }
    
    private void checkValidInfo() {
        boolean infoOk = (
                facilityName.getText().trim().equals("") ||
                zipField1.getText().trim().equals("") ||
                zipField2.getText().trim().equals("") ||
                addressField.getText().trim().equals("") ||
                areaField.getText().trim().equals("") ||
                cityField.getText().trim().equals("") ||
                numberField.getText().trim().equals("") ||
                (! validUserId()) ||
                (! validPassword()) ||
                adminSir.getText().trim().equals("") ||
                adminGiven.getText().trim().equals("") ||
                (licenseCombo.getSelectedItem() == null) ||
                (deptCombo.getSelectedItem() == null ||
                emailField.getText().trim().equals("")) ) ? false : true;
        setValidInfo(infoOk);
    }
    
    private boolean validUserId() {
        
        String val = adminId.getText().trim();
        if (val.equals("")) {
            return false;
        }
        if (val.length() < userIdLength[0] || val.length() > userIdLength[1]) {
            return false;
        }
        
        return true;
    }
    
    private boolean validPassword() {
        
        String passwd1 = new String(adminPassword1.getPassword());
        String passwd2 = new String(adminPassword2.getPassword());
        
        if (passwd1.equals("") || passwd2.equals("")) {
            return false;
        }
        
        if ( (passwd1.length() < passwordLength[0]) || (passwd1.length() > passwordLength[1]) ) {
            return false;
        }
        
        if ( (passwd2.length() < passwordLength[0]) || (passwd2.length() > passwordLength[1]) ) {
            return false;
        }
        
        return passwd1.equals(passwd2)? true : false;
    }
}
