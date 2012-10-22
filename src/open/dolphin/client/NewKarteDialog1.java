package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.EventHandler;
import java.util.prefs.Preferences;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionListener;

import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.PVTHealthInsuranceModel;

import org.jdesktop.application.ResourceMap;

/**
 * Dialog to select Health Insurance for new Karte.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class NewKarteDialog1 {
    
    private static final String LAST_CREATE_MODE = "newKarteDialog.lastCreateMode";
    private static final String FRAME_MEMORY 	 = "newKarteDialog.openFrame";
    
    private NewKarteParams params;
    
    // GUI components
    private JList docList;                  // �쐬�\�������X�g
    private JButton okButton;
    private JButton cancelButton;
    private JRadioButton emptyNew;          // �󔒐V�K�J���e
    private JRadioButton applyRp;           // �O�񏈕���K�p
    private JRadioButton allCopy;           // �S�ăR�s�[
    private JList insuranceList;            // �ی����X�g
    private JLabel departmentLabel;
    private JRadioButton addToTab;          // �^�u�p�l���֒ǉ�
    private JRadioButton openAnother;       // �� Window �֕\��
    
    private Preferences prefs;
    private Frame parentFrame;
    private String title;
    private JPanel content;
    private JDialog dialog;
    private Object value;
    
    private String savedDept;
    
    /** 
     * Creates new OpenKarteDialog 
     */
    public NewKarteDialog1(Frame parentFrame, String title) {
        this.parentFrame = parentFrame;
        this.title = title;
        prefs = Preferences.userNodeForPackage(this.getClass());
        content = createComponent();
    }
    
    public void setValue(Object o) {
        
        this.params = (NewKarteParams) o;
        setDocType(params.getDocType());
        savedDept = params.getDepartment();
        String[] depts = savedDept.split("\\s*,\\s*");
        if (depts[0] != null) {
            setDepartment(depts[0]);
        } else {
            setDepartment(params.getDepartment());
        }
        setInsurance(params.getInsurances());
        
        int lastCreateMode = prefs.getInt(LAST_CREATE_MODE, 0);
        boolean frameMemory = prefs.getBoolean(FRAME_MEMORY, true);
        
        switch (params.getOption()) {
            
            case BROWSER_NEW:
                applyRp.setEnabled(false);
                allCopy.setEnabled(false);
                emptyNew.setSelected(true);
                openAnother.setSelected(frameMemory);
                addToTab.setSelected(!frameMemory);
                ButtonGroup bg = new ButtonGroup();
                bg.add(openAnother);
                bg.add(addToTab);
                break;
                
            case BROWSER_COPY_NEW:
                selectCreateMode(lastCreateMode);
                bg = new ButtonGroup();
                bg.add(emptyNew);
                bg.add(applyRp);
                bg.add(allCopy);
                
                openAnother.setSelected(frameMemory);
                addToTab.setSelected(!frameMemory);
                bg = new ButtonGroup();
                bg.add(openAnother);
                bg.add(addToTab);
                break;
                
            case BROWSER_MODIFY:
                insuranceList.setEnabled(false);
                applyRp.setEnabled(false);
                allCopy.setEnabled(false);
                emptyNew.setEnabled(false);
                openAnother.setSelected(frameMemory);
                addToTab.setSelected(!frameMemory);
                bg = new ButtonGroup();
                bg.add(openAnother);
                bg.add(addToTab);
                // OK Button
                okButton.setEnabled(true);
                break;
                
            case EDITOR_NEW:
                applyRp.setEnabled(false);
                allCopy.setEnabled(false);
                emptyNew.setSelected(true);
                openAnother.setSelected(true);
                openAnother.setEnabled(false);
                addToTab.setEnabled(false);
                break;
                
            case EDITOR_COPY_NEW:
                selectCreateMode(lastCreateMode);
                bg = new ButtonGroup();
                bg.add(applyRp);
                bg.add(allCopy);
                bg.add(emptyNew);
                openAnother.setSelected(true);
                openAnother.setEnabled(false);
                addToTab.setEnabled(false);
                break;
                
            case EDITOR_MODIFY:
                insuranceList.setEnabled(false);
                applyRp.setEnabled(false);
                allCopy.setEnabled(false);
                emptyNew.setEnabled(false);
                openAnother.setSelected(true);
                openAnother.setEnabled(false);
                addToTab.setEnabled(false);
                break;
        }
    }
    
    public void start() {
        
        Object[] options = new Object[]{okButton, cancelButton};
        
        JOptionPane jop = new JOptionPane(
                content,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                options,
                okButton);
        
        dialog = jop.createDialog(parentFrame, title);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                insuranceList.requestFocusInWindow();
            }
        });
        dialog.setVisible(true);
    }
    
    public Object getValue() {
        return value;
    }
    
    private void setDepartment(String dept) {
        if (dept != null) {
            departmentLabel.setText(dept);
        }
    }
    
    private void setDocType(String docType) {
        int index = 0;
        if (docType.equals(IInfoModel.DOCTYPE_KARTE)) {
            index = 0;
        } else if (docType.equals(IInfoModel.DOCTYPE_S_KARTE)) {
            index = 1;
        } else if (docType.equals(IInfoModel.DOCTYPE_LETTER)) {
            index = 2;
        }
        docList.getSelectionModel().setSelectionInterval(index, index);
    }
    
    private void setInsurance(Object[] o) {

        insuranceList.setListData(o);
        if (o != null && o.length > 0) {
            int index = params.getInitialSelectedInsurance();
            if (index >=0 && index < o.length) {
                insuranceList.getSelectionModel().setSelectionInterval(index,index);
            }
        }
    }
    
    private Chart.NewKarteMode getCreateMode() {
        if (emptyNew.isSelected()) {
            return Chart.NewKarteMode.EMPTY_NEW;
        } else if (applyRp.isSelected()) {
            return Chart.NewKarteMode.APPLY_RP;
        } else if (allCopy.isSelected()) {
            return Chart.NewKarteMode.ALL_COPY;
        }
        return Chart.NewKarteMode.EMPTY_NEW;
    }
    
    private void selectCreateMode(int mode) {
        emptyNew.setSelected(false);
        applyRp.setSelected(false);
        allCopy.setSelected(false);
        if (mode == 0) {
            emptyNew.setSelected(true);
        } else if (mode == 1) {
            applyRp.setSelected(true);
        } else if (mode == 2) {
            allCopy.setSelected(true);
        }
    }
    
    /**
     * GUI ���\�z����B
     * �Q���J���e�A�V���O���A�Љ��̑I�����X�g��\������B
     */
    protected JPanel createComponent() {
        
        //
        // ���\�[�X�ƃA�N�V�����𓾂�
        //
        ResourceMap resMap = ClientContext.getResourceMap(this.getClass());
        ActionMap actions = ClientContext.getActionMap(this); 
        
        // �f�Éȏ�񃉃x��
        JLabel deptPrifix = injectLabel("deptPrifix");
        departmentLabel = new JLabel();
        JPanel dp = new JPanel(new FlowLayout(FlowLayout.CENTER, 11, 0));
        dp.add(deptPrifix);
        dp.add(departmentLabel);
        
        //
        // ������ʂ� JList
        //
        String[] docTypes = resMap.getString("docTypes").split(",");
        String newDocBorder = resMap.getString("newDocBorder");
        JLabel newDocImage = injectLabel("newDocImage"); // new_32.gif
        
        docList = new JList(docTypes);
        docList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        docList.setLayoutOrientation(JList.VERTICAL);
        docList.setVisibleRowCount(-1);
        JPanel kp = new JPanel(new BorderLayout(9, 0));
        kp.setBorder(BorderFactory.createTitledBorder(newDocBorder));
        kp.add(newDocImage, BorderLayout.WEST);
        kp.add(docList, BorderLayout.CENTER);
        docList.addListSelectionListener((ListSelectionListener) 
            EventHandler.create(ListSelectionListener.class, this, "docSelectionChanged", "valueIsAdjusting"));
        
        // �ی��I�����X�g
        JLabel insImage = injectLabel("insImage"); // addbk_32.gif
        insuranceList = new JList();
        insuranceList.setLayoutOrientation(JList.VERTICAL);
        insuranceList.setVisibleRowCount(-1);
        insuranceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        insuranceList.addListSelectionListener((ListSelectionListener) 
            EventHandler.create(ListSelectionListener.class, this, "insuranceSelectionChanged", "valueIsAdjusting"));
        
        JPanel ip = new JPanel(new BorderLayout(9, 0));
        ip.add(insImage, BorderLayout.WEST);
        ip.add(insuranceList, BorderLayout.CENTER);
        
        // �O�񏈕��K�p �S�R�s�[ ��
        emptyNew = injectRadio("emptyNew");
        applyRp = injectRadio("applyRp");
        allCopy = injectRadio("allCopy");
        emptyNew.setAction(actions.get("memoryMode"));
        applyRp.setAction(actions.get("memoryMode"));
        allCopy.setAction(actions.get("memoryMode"));
        JPanel rpPanel = createRadioPanel(new JRadioButton[]{emptyNew, applyRp, allCopy});
        
        // �J���e�I�v�V�����p�l��
        JPanel karteOption = new JPanel();
        karteOption.setLayout(new BoxLayout(karteOption, BoxLayout.Y_AXIS));
        karteOption.add(ip);
        karteOption.add(Box.createRigidArea(new Dimension(0, 7)));
        karteOption.add(rpPanel);
        
        // �^�u�p�l���֒ǉ�/�ʃE�B���h�E
        openAnother = injectRadio("openAnother");
        addToTab = injectRadio("addToTab");
        openAnother.setAction(actions.get("memoryFrame"));
        addToTab.setAction(actions.get("memoryFrame"));
        JPanel openPanel = createRadioPanel(new JRadioButton[]{openAnother, addToTab});
        
        // ok
        String buttonText =  (String) UIManager.get("OptionPane.okButtonText");
        okButton = new JButton();
        okButton.setAction(actions.get("doOk"));
        okButton.setText(buttonText);
        okButton.setEnabled(false);
        
        // Cancel Button
        buttonText =  (String) UIManager.get("OptionPane.cancelButtonText");
        cancelButton = new JButton();
        cancelButton.setAction(actions.get("doCancel"));
        cancelButton.setText(buttonText);
                
        // �S�̂�z�u����
        String karteOptionBorder = resMap.getString("karteOptionBorder");
        String placeOptionBorder = resMap.getString("placeOptionBorder");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(dp);
        panel.add(Box.createVerticalStrut(11));
        panel.add(kp);
        panel.add(Box.createVerticalStrut(11));
        panel.add(GUIFactory.createTitledPanel(karteOption, karteOptionBorder));
        panel.add(Box.createVerticalStrut(11));
        panel.add(GUIFactory.createTitledPanel(openPanel, placeOptionBorder));
        panel.add(Box.createVerticalStrut(11));
        
        // Injection
        resMap.injectComponents(panel);
        
        return panel;
    }
    
    /**
     * �ی��I�����X�g�Ƀt�H�[�J�X����B
     */
    public void controlFocus(WindowEvent e) {
        docList.requestFocusInWindow();
    }
    
    /**
     * �ی��I���̕ύX����������B
     */
    public void docSelectionChanged(boolean adjusting) {
        
        if (adjusting == false) {
            int  index = docList.getSelectedIndex();
            
            switch (index) {
                case 0:
                    insuranceList.setEnabled(true);
                    
                    switch (params.getOption()) {
            
                            case BROWSER_NEW:
                                emptyNew.setEnabled(true);
                                break;

                            case BROWSER_COPY_NEW:
                                applyRp.setEnabled(true);
                                allCopy.setEnabled(true);
                                emptyNew.setEnabled(true);
                                break;

                            case EDITOR_NEW:
                                emptyNew.setEnabled(true);
                                break;

                            case EDITOR_COPY_NEW:
                                applyRp.setEnabled(true);
                                allCopy.setEnabled(true);
                                emptyNew.setEnabled(true);
                                break;
                        }
                    
                    Object o = insuranceList.getSelectedValue();
                    boolean ok = o != null ? true : false;
                    okButton.setEnabled(ok);
                    break;
                    
                case 1:
                case 2:
                    insuranceList.setEnabled(false);
                    emptyNew.setEnabled(true);
                    applyRp.setEnabled(false);
                    allCopy.setEnabled(true);
                    okButton.setEnabled(true);
                    break;
            }
        }
    }
    
    /**
     * �ی��I���̕ύX����������B
     */
    public void insuranceSelectionChanged(boolean adjusting) {
        if (adjusting == false) {
            Object o = insuranceList.getSelectedValue();
            boolean ok = o != null ? true : false;
            okButton.setEnabled(ok);
        }
    }
    
    /**
     * �J���e�̍쐬���@���v���t�@�����X�ɋL�^����B
     */
    @org.jdesktop.application.Action
    public void memoryMode() {
        
        if (emptyNew.isSelected()) {
            prefs.putInt(LAST_CREATE_MODE, 0);
        } else if (applyRp.isSelected()) {
            prefs.putInt(LAST_CREATE_MODE, 1);
        } else if (allCopy.isSelected()) {
            prefs.putInt(LAST_CREATE_MODE, 2);
        }
    }
    
    /**
     * �J���e�t���[��(�E�C���h�E)�̍쐬���@���v���t�@�����X�ɋL�^����B
     */
    @org.jdesktop.application.Action
    public void memoryFrame() {
        boolean openFrame = openAnother.isSelected();
        prefs.putBoolean(FRAME_MEMORY,openFrame);
    }
    
    /**
     * �p���[���[�^���擾���_�C�A���O�̒l�ɐݒ肷��B
     */
    @org.jdesktop.application.Action
    public void doOk() {
        int index = docList.getSelectedIndex();
        switch (index) {
            case 0:
                params.setDocType(IInfoModel.DOCTYPE_KARTE);
                break;
                
            case 1:
                params.setDocType(IInfoModel.DOCTYPE_S_KARTE);
                break;
                
            case 2:
                params.setDocType(IInfoModel.DOCTYPE_LETTER);
                break;
        }
        params.setDepartment(savedDept);
        params.setPVTHealthInsurance((PVTHealthInsuranceModel)insuranceList.getSelectedValue());
        params.setCreateMode(getCreateMode());
        //System.err.println("create mode = " + params.getCreateMode().toString());
        params.setOpenFrame(openAnother.isSelected());
        value = (Object) params;
        dialog.setVisible(false);
        dialog.dispose();
    }
    
    /**
     * �L�����Z������B�_�C�A���O�����B
     */
    @org.jdesktop.application.Action
    public void doCancel() {
        value = null;
        dialog.setVisible(false);
        dialog.dispose();
    }
    
    private JLabel injectLabel(String name) {
        JLabel ret = new JLabel();
        ret.setName(name);
        return ret;
    }
    
    private JRadioButton injectRadio(String name) {
        JRadioButton ret = new JRadioButton();
        ret.setName(name);
        return ret;
    }
    
    private JPanel createRadioPanel(JRadioButton[] btns) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        for (JRadioButton btn : btns) {
            panel.add(btn);
        }
        return panel;
    }
    
//    private JPanel createCmdPanel(JButton[] btns) {
//        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
//        for (JButton btn : btns) {
//            panel.add(btn);
//        }
//        return panel;
//    }
}