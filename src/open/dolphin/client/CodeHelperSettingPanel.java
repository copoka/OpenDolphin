package open.dolphin.client;

import java.awt.GridBagConstraints;
import java.util.prefs.Preferences;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import open.dolphin.client.AbstractSettingPanel.State;
import open.dolphin.helper.GridBagBuilder;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.project.ProjectStub;

/**
 * �R�[�h�w���p�[�ݒ�p�l���B
 *
 * @author Kazushi Minagawa
 */
public class CodeHelperSettingPanel extends AbstractSettingPanel {
    
    private static final String ID = "codeHelperSetting";
    private static final String TITLE = "�R�[�h";
    private static final String ICON = "help_24.gif";
    
    private JRadioButton ctrlMask;
    
    private JRadioButton metaMask;
    
    private JTextField text;
    
    private JTextField path;
    
    private JTextField general;
    
    private JTextField other;
    
    private JTextField treatment;
    
    private JTextField surgery;
    
    private JTextField radiology;
    
    private JTextField labo;
    
    private JTextField physiology;
    
    private JTextField bacteria;
    
    private JTextField injection;
    
    private JTextField rp;
    
    private JTextField baseCharge;
    
    private JTextField instraction;
    
    private JTextField orca;
    
    private State curState = State.NONE_STATE;
    
    private HelperModel model;
            
    
    /** 
     * Creates a new instance of CodeHelperSettingPanel 
     */
    public CodeHelperSettingPanel() {
        this.setId(ID);
        this.setTitle(TITLE);
        this.setIcon(ICON);
    }

    /**
     * GUI �𐶐����v���O�������J�n����B
     */
    public void start() {
        
        //
        // ���f���𐶐�����
        //
        model = new HelperModel();
        
        //
        // GUI ���\�z����
        //
        initComponents();
        
        //
        // ModelToView
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
     * GUI ���\�z����
     */
    private void initComponents() {
        
        ctrlMask = new JRadioButton("�R���g���[��");
        String str = ClientContext.isMac() ? "�A�b�v��" : "���^";
        metaMask = new JRadioButton(str);
        text = new JTextField(5);
        path = new JTextField(5);
        general = new JTextField(5);
        other = new JTextField(5);
        treatment = new JTextField(5);
        surgery = new JTextField(5);
        radiology = new JTextField(5);
        labo = new JTextField(5);
        physiology = new JTextField(5);
        bacteria = new JTextField(5);
        injection = new JTextField(5);
        rp = new JTextField(5);
        baseCharge = new JTextField(5);
        instraction = new JTextField(5);
        orca = new JTextField(5);
        
        //
        // �C���L�[
        //
        GridBagBuilder gbl = new GridBagBuilder("�C���L�[ + �X�y�[�X = �⊮�|�b�v�A�b�v");
        
        gbl.add(new JLabel("�C���L�[:"),  0, 0, GridBagConstraints.EAST);
        gbl.add(GUIFactory.createRadioPanel(new JRadioButton[]{ctrlMask,metaMask}), 1, 0, GridBagConstraints.CENTER);
        JPanel keyBind = gbl.getProduct();
        
        //
        // Stamptree
        //
        gbl = new GridBagBuilder("�X�^���v���̃L�[���[�h");
        
        gbl.add(new JLabel("�e�L�X�g:"),         0, 0, GridBagConstraints.EAST);
        gbl.add(text,                           1, 0, GridBagConstraints.WEST);
                
        gbl.add(new JLabel("�p�X:"),            2, 0, GridBagConstraints.EAST);
        gbl.add(path,                           3, 0, GridBagConstraints.WEST);
        
        gbl.add(new JLabel("�� �p:"),           0, 1, GridBagConstraints.EAST);
        gbl.add(general,                        1, 1, GridBagConstraints.WEST);
        
        gbl.add(new JLabel("���̑�:"),           2, 1, GridBagConstraints.EAST);
        gbl.add(other,                          3, 1, GridBagConstraints.WEST);
        
        gbl.add(new JLabel("�� �u:"),            0, 2, GridBagConstraints.EAST);
        gbl.add(treatment,                       1, 2, GridBagConstraints.WEST);
        
        gbl.add(new JLabel("�� �p:"),            2, 2, GridBagConstraints.EAST);
        gbl.add(surgery,                         3, 2, GridBagConstraints.WEST);
        
        gbl.add(new JLabel("���ː�:"),           0, 3, GridBagConstraints.EAST);
        gbl.add(radiology,                       1, 3, GridBagConstraints.WEST);
        
        gbl.add(new JLabel("���̌���:"),          2, 3, GridBagConstraints.EAST);
        gbl.add(labo,                            3, 3, GridBagConstraints.WEST);
        
        gbl.add(new JLabel("���̌���:"),          0, 4, GridBagConstraints.EAST);
        gbl.add(physiology,                      1, 4, GridBagConstraints.WEST);
        
        gbl.add(new JLabel("�׋ی���:"),          2, 4, GridBagConstraints.EAST);
        gbl.add(bacteria,                        3, 4, GridBagConstraints.WEST);
        
        gbl.add(new JLabel("�� ��:"),            0, 5, GridBagConstraints.EAST);
        gbl.add(injection,                       1, 5, GridBagConstraints.WEST);
        
        gbl.add(new JLabel("�� ��:"),            2, 5, GridBagConstraints.EAST);
        gbl.add(rp,                              3, 5, GridBagConstraints.WEST);
        
        gbl.add(new JLabel("�f�f��:"),           0, 6, GridBagConstraints.EAST);
        gbl.add(baseCharge,                      1, 6, GridBagConstraints.WEST);
        
        gbl.add(new JLabel("�w���E�ݑ�:"),         2, 6, GridBagConstraints.EAST);
        gbl.add(instraction,                     3, 6, GridBagConstraints.WEST);
        
        gbl.add(new JLabel("ORCA:"),            0, 7, GridBagConstraints.EAST);
        gbl.add(orca,                           1, 7, GridBagConstraints.WEST);
        
        JPanel stamp = gbl.getProduct();
        
        // �S�̂����C�A�E�g����
        gbl = new GridBagBuilder();
        gbl.add(keyBind,        0, 0, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gbl.add(stamp,          0, 1, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gbl.add(new JLabel(""), 0, 2, GridBagConstraints.BOTH,       1.0, 1.0);
        
        setUI(gbl.getProduct());
        
    }
    
    private void connect() {
        
        ButtonGroup bg = new ButtonGroup();
        bg.add(ctrlMask);
        bg.add(metaMask);
        
        //
        // DocumentListener
        //
        DocumentListener dl = ProxyDocumentListener.create(this, "checkState");
        
        text.getDocument().addDocumentListener(dl);
        path.getDocument().addDocumentListener(dl);
        general.getDocument().addDocumentListener(dl);
        other.getDocument().addDocumentListener(dl);
        treatment.getDocument().addDocumentListener(dl);
        surgery.getDocument().addDocumentListener(dl);
        radiology.getDocument().addDocumentListener(dl);
        labo.getDocument().addDocumentListener(dl);
        physiology.getDocument().addDocumentListener(dl);
        bacteria.getDocument().addDocumentListener(dl);
        injection.getDocument().addDocumentListener(dl);
        rp.getDocument().addDocumentListener(dl);
        baseCharge.getDocument().addDocumentListener(dl);
        instraction.getDocument().addDocumentListener(dl);
        orca.getDocument().addDocumentListener(dl);
        
    }
    
    public void checkState() {
        
        State newState = State.NONE_STATE;
        
        if (text.getText().trim().equals("") || 
                path.getText().trim().equals("") || 
                general.getText().trim().equals("") || 
                other.getText().trim().equals("") || 
                treatment.getText().trim().equals("") || 
                surgery.getText().trim().equals("") || 
                radiology.getText().trim().equals("") || 
                labo.getText().trim().equals("") || 
                physiology.getText().trim().equals("") || 
                bacteria.getText().trim().equals("") ||     
                injection.getText().trim().equals("") ||     
                injection.getText().trim().equals("") ||  
                rp.getText().trim().equals("") ||  
                baseCharge.getText().trim().equals("") ||  
                instraction.getText().trim().equals("") ||  
                orca.getText().trim().equals("")) {
            
            newState = State.INVALID_STATE;
            
        } else {
            newState = State.VALID_STATE;
        }
        
        if (curState != newState) {
            curState = newState;
            setState(curState);
        }
    }
    
    class HelperModel {

        /**
         * �ݒ肵���l���v���t�@�����X�ɕۑ�����B
         */
        public void restore(ProjectStub stub) {

            Preferences prefs = Preferences.userNodeForPackage(AbstractCodeHelper.class);

            String mask = ctrlMask.isSelected() ? "ctrl" : "meta";

            prefs.put("modifier", mask);

            prefs.put(IInfoModel.ENTITY_TEXT, text.getText().trim());

            prefs.put(IInfoModel.ENTITY_PATH, path.getText().trim());

            prefs.put(IInfoModel.ENTITY_GENERAL_ORDER, general.getText().trim());

            prefs.put(IInfoModel.ENTITY_OTHER_ORDER, other.getText().trim());

            prefs.put(IInfoModel.ENTITY_TREATMENT, treatment.getText().trim());

            prefs.put(IInfoModel.ENTITY_SURGERY_ORDER, surgery.getText().trim());

            prefs.put(IInfoModel.ENTITY_RADIOLOGY_ORDER, radiology.getText().trim());

            prefs.put(IInfoModel.ENTITY_LABO_TEST, labo.getText().trim());

            prefs.put(IInfoModel.ENTITY_PHYSIOLOGY_ORDER, physiology.getText().trim());

            prefs.put(IInfoModel.ENTITY_BACTERIA_ORDER, bacteria.getText().trim());

            prefs.put(IInfoModel.ENTITY_INJECTION_ORDER, injection.getText().trim());

            prefs.put(IInfoModel.ENTITY_MED_ORDER, rp.getText().trim());

            prefs.put(IInfoModel.ENTITY_BASE_CHARGE_ORDER, baseCharge.getText().trim());

            prefs.put(IInfoModel.ENTITY_INSTRACTION_CHARGE_ORDER, instraction.getText().trim());

            prefs.put(IInfoModel.ENTITY_ORCA, orca.getText().trim());
        }

        /**
         * �v���t�@�����X����l��GUI�ɃZ�b�g����B
         */
        public void populate(ProjectStub stub) {

            Preferences prefs = Preferences.userNodeForPackage(AbstractCodeHelper.class);

            String mask = ClientContext.isMac() ? "meta" : "ctrl";
            String modifier = prefs.get("modifier", mask);

            if (modifier.equals("ctrl")) {
                ctrlMask.setSelected(true);
                metaMask.setSelected(false);
            } else {
                ctrlMask.setSelected(false);
                metaMask.setSelected(true);
            }

            text.setText(prefs.get(IInfoModel.ENTITY_TEXT, "tx").trim());

            path.setText(prefs.get(IInfoModel.ENTITY_PATH, "pat").trim());

            general.setText(prefs.get(IInfoModel.ENTITY_GENERAL_ORDER, "gen").trim());

            other.setText(prefs.get(IInfoModel.ENTITY_OTHER_ORDER, "oth").trim());

            treatment.setText(prefs.get(IInfoModel.ENTITY_TREATMENT, "tr").trim());

            surgery.setText(prefs.get(IInfoModel.ENTITY_SURGERY_ORDER, "sur").trim());

            radiology.setText(prefs.get(IInfoModel.ENTITY_RADIOLOGY_ORDER, "rad").trim());

            labo.setText(prefs.get(IInfoModel.ENTITY_LABO_TEST, "lab").trim());

            physiology.setText(prefs.get(IInfoModel.ENTITY_PHYSIOLOGY_ORDER, "phy").trim());

            bacteria.setText(prefs.get(IInfoModel.ENTITY_BACTERIA_ORDER, "bac").trim());

            injection.setText(prefs.get(IInfoModel.ENTITY_INJECTION_ORDER, "inj").trim());

            rp.setText(prefs.get(IInfoModel.ENTITY_MED_ORDER, "rp").trim());

            baseCharge.setText(prefs.get(IInfoModel.ENTITY_BASE_CHARGE_ORDER, "base").trim());

            instraction.setText(prefs.get(IInfoModel.ENTITY_INSTRACTION_CHARGE_ORDER, "ins").trim());

            orca.setText(prefs.get(IInfoModel.ENTITY_ORCA, "orca").trim());

            connect();
            checkState();

        }
    }
}












