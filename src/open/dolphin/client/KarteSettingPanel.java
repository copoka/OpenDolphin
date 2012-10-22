package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.text.NumberFormat;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import open.dolphin.project.Project;
import open.dolphin.project.ProjectStub;

/**
 * KarteSettingPanel
 *
 * @author Minagawa,Kazushi
 */
public class KarteSettingPanel extends AbstractSettingPanel {
    
    private Preferences prefs;
    
    // �f�t�H���g�l
    private int defaultMemoLocation;
    private boolean defaultLocator;
    private boolean defaultAsc;
    private boolean defaultShowModified;
    private int defaultFetchCount;
    private int minFetchCount;
    private int maxFetchCount;
    private int stepFetchCount;
    private boolean defaultScDirection;
    private int defaultPeriod;
    private boolean defaultDiagnosisAsc;
    private int defaultDiagnosisPeriod;
    private int defaultOffsetOutcomeDate;
    private int defaultLaboTestPeriod;
    
    // �C���X�y�N�^���
    //private JRadioButton memoTop;
    //private JRadioButton memoBottom;
    private JComboBox memoLocCombo;
    private JRadioButton pltform;
    private JRadioButton prefLoc;
    
    // �J���e�����֌W
    private JRadioButton asc;
    private JRadioButton desc;
    private JCheckBox showModifiedCB;
    private JSpinner spinner;
    private JComboBox periodCombo;
    private JRadioButton vSc;
    private JRadioButton hSc;
    private NameValuePair[] periodObjects;
    
    // �a���֌W
    private JRadioButton diagnosisAsc;
    private JRadioButton diagnosisDesc;
    private JComboBox diagnosisPeriodCombo;
    private JSpinner outcomeSpinner;
    private NameValuePair[] diagnosisPeriodObjects;
    
    // ���̌���
    private NameValuePair[] laboTestPeriodObjects;
    private JComboBox laboTestPeriodCombo;
    
    // �R�}���h�{�^��
    private JButton restoreDefaultBtn;
    
    //
    // CLAIM ���M�֌W
    //
    private JRadioButton sendAtTmp;
    private JRadioButton noSendAtTmp;
    private JRadioButton sendAtSave;
    private JRadioButton noSendAtSave;
    private JRadioButton sendAtModify;
    private JRadioButton noSendAtModify;
    private JRadioButton sendDiagnosis;
    private JRadioButton noSendDiagnosis;
    
    //
    // �m�F�_�C�A���O�֌W
    //
    private JCheckBox noConfirmAtNew;
    private JRadioButton copyNew;
    private JRadioButton applyRp;
    private JRadioButton emptyNew;
    private JRadioButton placeWindow;
    private JRadioButton palceTabbedPane;
    
    private JCheckBox noConfirmAtSave;
    private JRadioButton save;
    private JRadioButton saveTmp;
    private JFormattedTextField printCount;
    
    private KarteModel model;
    
    /**
     * �ݒ��ʂ��J�n����B
     */
    public void start() {
       
        prefs = Project.getPreferences();
        
        //
        // ���f���𐶐�������������
        //
        model = new KarteModel();
        model.populate(getProjectStub());
        
        //
        // GUI ���\�z����
        //
        initComponents();
        
        //
        // bindModel
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
     * GUI ���\�z����B
     */
    private void initComponents() {
        
        //
        // �f�t�H���g�l���擾����
        //
        defaultMemoLocation = 0; // top=0, bottom=1
        defaultLocator = true;
        defaultAsc = ClientContext.getBoolean("docHistory.default.ascending");
        defaultShowModified = ClientContext.getBoolean("docHistory.default.showModified");
        defaultFetchCount = ClientContext.getInt("docHistory.default.fetchCount");
        minFetchCount = ClientContext.getInt("docHistory.min.fetchCount");
        maxFetchCount = ClientContext.getInt("docHistory.max.fetchCount");
        stepFetchCount = ClientContext.getInt("docHistory.step.fetchCount");
        defaultScDirection = ClientContext.getBoolean("karte.default.scDirection");
        defaultPeriod = ClientContext.getInt("docHistory.default.period");
        defaultDiagnosisAsc = ClientContext.getBoolean("diagnosis.default.ascending");
        defaultDiagnosisPeriod = ClientContext.getInt("diagnosis.default.period");
        defaultOffsetOutcomeDate = ClientContext.getInt("diagnosis.default.offsetOutcomeDate");
        defaultLaboTestPeriod = ClientContext.getInt("laboTest.default.period");
        
        //
        // GUI �R���|�[�l���g�𐶐�����
        //
        
        // Memo �̈ʒu
        //memoTop = new JRadioButton("�g�b�v");
        //memoBottom = new JRadioButton("�{�g��");
        String[] memoLoc = new String[]{"�J�����_�E���������E����", "�����E�J�����_�E��������", "�����E���������E�J�����_"};
        memoLocCombo = new JComboBox(memoLoc);
        
        // ���҃C���X�y�N�^��ʂ̃��P�[�^
        pltform = new JRadioButton("�v���b�g�t�H�[��");
        prefLoc = new JRadioButton("�ʒu�Ƒ傫�����L������");
        
        // �J���e�����֌W
        asc = new JRadioButton("����");
        desc = new JRadioButton("�~��");
        showModifiedCB = new JCheckBox("�C������\��");
        periodObjects = ClientContext.getNameValuePair("docHistory.combo.period");
        periodCombo = new JComboBox(periodObjects);
        vSc = new JRadioButton("����");
        hSc = new JRadioButton("����");
        
        // �a���֌W
        diagnosisAsc = new JRadioButton("����");
        diagnosisDesc = new JRadioButton("�~��");
        diagnosisPeriodObjects = ClientContext.getNameValuePair("diagnosis.combo.period");
        diagnosisPeriodCombo = new JComboBox(diagnosisPeriodObjects);
        
        // ���̌���
        laboTestPeriodObjects = ClientContext.getNameValuePair("docHistory.combo.period");
        laboTestPeriodCombo = new JComboBox(laboTestPeriodObjects);
        
        // �R�}���h�{�^��
        restoreDefaultBtn = new JButton("�f�t�H���g�ݒ�ɖ߂�");
        
        //
        // CLAIM ���M�֌W
        //
        sendAtTmp = new JRadioButton("���M����");
        noSendAtTmp = new JRadioButton("���M���Ȃ�");
        sendAtSave = new JRadioButton("���M����");
        noSendAtSave = new JRadioButton("���M���Ȃ�");
        sendAtModify = new JRadioButton("���M����");
        noSendAtModify = new JRadioButton("���M���Ȃ�");
        sendDiagnosis = new JRadioButton("���M����");
        noSendDiagnosis = new JRadioButton("���M���Ȃ�");
        
        //
        // �m�F�_�C�A���O�֌W
        //
        noConfirmAtNew = new JCheckBox("�m�F�_�C�A���O��\�����Ȃ�");
        copyNew = new JRadioButton("�S�ăR�s�[");
        applyRp = new JRadioButton("�O�񏈕���K�p");
        emptyNew = new JRadioButton("�󔒂̐V�K�J���e");
        placeWindow = new JRadioButton("�ʃE�B���h�E�ŕҏW");
        palceTabbedPane = new JRadioButton("�^�u�p�l���֒ǉ�");
        
        noConfirmAtSave = new JCheckBox("�m�F�_�C�A���O��\�����Ȃ�");
        save = new JRadioButton("�� ��");
        saveTmp = new JRadioButton("���ۑ�");
        
        //
        // ���������擾���� Spinner
        //
        int currentFetchCount = prefs.getInt(Project.DOC_HISTORY_FETCHCOUNT, defaultFetchCount);
        SpinnerModel fetchModel = new SpinnerNumberModel(currentFetchCount,minFetchCount,maxFetchCount,stepFetchCount);
        spinner = new JSpinner(fetchModel);
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "#"));
        
        //
        // �]�A���͎��ɓ��t����͂���ꍇ�̂��ӂ����ƒl
        //
        int currentOffsetOutcomeDate = prefs.getInt(Project.OFFSET_OUTCOME_DATE, defaultOffsetOutcomeDate);
        SpinnerModel outcomeModel = new SpinnerNumberModel(currentOffsetOutcomeDate, -31, 0, 1);
        outcomeSpinner = new JSpinner(outcomeModel);
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "#"));
        
        //
        // �C���X�y�N�^��� Memo & ���P�[�^
        //
        JPanel memoLocatin = new JPanel();
        //memoLocatin.add(memoTop);
        //memoLocatin.add(memoBottom);
        memoLocatin.add(memoLocCombo);
        JPanel frameLocator = new JPanel();
        frameLocator.add(pltform);
        frameLocator.add(prefLoc);
        
        //
        // ���������̏����~��
        //
        JPanel ascDesc = new JPanel();
        ascDesc.add(asc);
        ascDesc.add(desc);
        ascDesc.add(showModifiedCB);
        
        //
        // �X�N���[������
        //
        JPanel scrP = new JPanel();
        scrP.add(vSc);
        scrP.add(hSc);
        
        // �C���X�y�N�^�^�u
        GridBagBuilder gbb = new GridBagBuilder("�C���X�y�N�^���");
        int row = 0;
        JLabel label = new JLabel("�����ʒu:", SwingConstants.RIGHT);
        gbb.add(label,       0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(memoLocatin, 1, row, 1, 1, GridBagConstraints.WEST);
        
        row++;
        label = new JLabel("��ʃ��P�[�^:", SwingConstants.RIGHT);
        gbb.add(label, 	      0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(frameLocator, 1, row, 1, 1, GridBagConstraints.WEST);
        JPanel insP = gbb.getProduct();
        
        gbb = new GridBagBuilder();
        gbb.add(insP,           0, 0, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gbb.add(new JLabel(""), 0, 1, GridBagConstraints.BOTH,       1.0, 1.0);
        JPanel inspectorPanel = gbb.getProduct();
                
        // �����֘A�^�u
        // Karte
        gbb = new GridBagBuilder("�J���e");
        row = 0;
        label = new JLabel("��������:", SwingConstants.RIGHT);
        gbb.add(label,   0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(ascDesc, 1, row, 1, 1, GridBagConstraints.WEST);
        
        row++;
        label = new JLabel("���������擾��:", SwingConstants.RIGHT);
        gbb.add(label,   0, row,  1, 1, GridBagConstraints.EAST);
        gbb.add(spinner, 1, row,  1, 1, GridBagConstraints.WEST);
        
        row++;
        label = new JLabel("�X�N���[������:", SwingConstants.RIGHT);
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(scrP,  1, row, 1, 1, GridBagConstraints.WEST);
        
        row++;
        label = new JLabel("�������o����:", SwingConstants.RIGHT);
        gbb.add(label,       0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(periodCombo, 1, row, 1, 1, GridBagConstraints.WEST);
        JPanel kartePanel = gbb.getProduct();
        
        
        // Diagnosis
        JPanel diagAscDesc = new JPanel();
        diagAscDesc.add(diagnosisAsc);
        diagAscDesc.add(diagnosisDesc);
        gbb = new GridBagBuilder("���a��");
        row = 0;
        label = new JLabel("�\����:", SwingConstants.RIGHT);
        gbb.add(label,       0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(diagAscDesc, 1, row, 1, 1, GridBagConstraints.WEST);
        
        row++;
        label = new JLabel("���o����:", SwingConstants.RIGHT);
        gbb.add(label,                0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(diagnosisPeriodCombo, 1, row, 1, 1, GridBagConstraints.WEST);
        
        row++;
        label = new JLabel("�]�A���͎��̏I�����I�t�Z�b�g:", SwingConstants.RIGHT);
        gbb.add(label,          0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(outcomeSpinner, 1, row, 1, 1, GridBagConstraints.WEST);
        JPanel diagnosisPanel = gbb.getProduct();
        
        // LaboTest
        gbb = new GridBagBuilder("���{�e�X�g");
        row = 0;
        label = new JLabel("���o����:", SwingConstants.RIGHT);
        gbb.add(label, 		     0,	row, 1, 1, GridBagConstraints.EAST);
        gbb.add(laboTestPeriodCombo, 1, row, 1, 1, GridBagConstraints.WEST);
        JPanel laboPanel = gbb.getProduct();
        
        // Set default button
        JPanel cmd = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        cmd.add(restoreDefaultBtn);
        
        gbb = new GridBagBuilder();
        gbb.add(kartePanel,        0, 0, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gbb.add(diagnosisPanel,    0, 1, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gbb.add(cmd,               0, 2, GridBagConstraints.HORIZONTAL, 1.0, 0.0);
        gbb.add(new JLabel(""),    0, 3, GridBagConstraints.BOTH,       1.0, 1.0);
        
        JPanel docPanel = gbb.getProduct();
        
        ButtonGroup bg = new ButtonGroup();
        bg.add(asc);
        bg.add(desc);
        
        bg = new ButtonGroup();
        bg.add(diagnosisAsc);
        bg.add(diagnosisDesc);
        
        //bg = new ButtonGroup();
        //bg.add(memoTop);
        //bg.add(memoBottom);
        
        bg = new ButtonGroup();
        bg.add(pltform);
        bg.add(prefLoc);
        
        bg = new ButtonGroup();
        bg.add(vSc);
        bg.add(hSc);
        
        restoreDefaultBtn.addActionListener((ActionListener) EventHandler.create(ActionListener.class, this, "restoreDefault"));
        
        //
        // CLAIM ���M�̃f�t�H���g�ݒ�
        //
        JPanel sendPanel = new JPanel();
        sendPanel.setLayout(new BoxLayout(sendPanel, BoxLayout.Y_AXIS));
        
        gbb = new GridBagBuilder("�f�Ís�ב��M�̃f�t�H���g�ݒ�");
        row = 0;
        label = new JLabel("���ۑ���:", SwingConstants.RIGHT);
        JPanel p9 = GUIFactory.createRadioPanel(new JRadioButton[]{sendAtTmp, noSendAtTmp});
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(p9,    1, row, 1, 1, GridBagConstraints.WEST);
        
        row++;
        label = new JLabel("�ۑ���:", SwingConstants.RIGHT);
        p9 = GUIFactory.createRadioPanel(new JRadioButton[]{sendAtSave, noSendAtSave});
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(p9,    1, row, 1, 1, GridBagConstraints.WEST);
        
        row++;
        label = new JLabel("�C����:", SwingConstants.RIGHT);
        p9 = GUIFactory.createRadioPanel(new JRadioButton[]{sendAtModify, noSendAtModify});
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(p9,    1, row, 1, 1, GridBagConstraints.WEST);
        
        row++;
        label = new JLabel("���a��:", SwingConstants.RIGHT);
        p9 = GUIFactory.createRadioPanel(new JRadioButton[]{sendDiagnosis, noSendDiagnosis});
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(p9,    1, row, 1, 1, GridBagConstraints.WEST);
        
        sendPanel.add(gbb.getProduct());
        sendPanel.add(Box.createVerticalStrut(500));
        sendPanel.add(Box.createVerticalGlue());
        
        
        //
        // �V�K�J���e�쐬���ƕۑ����̊m�F�_�C�A���O�I�v�V����
        //
        JPanel confirmPanel = new JPanel();
        confirmPanel.setLayout(new BoxLayout(confirmPanel, BoxLayout.Y_AXIS));
        NumberFormat numFormat = NumberFormat.getNumberInstance();
        printCount = new JFormattedTextField(numFormat);
        printCount.setValue(new Integer(0));
        
        row = 0;
        gbb = new GridBagBuilder("�V�K�J���e�쐬��");
        gbb.add(noConfirmAtNew, 0, row, 2, 1, GridBagConstraints.WEST);
        
        row+=1;
        label = new JLabel("�쐬���@:", SwingConstants.RIGHT);
        JPanel p = GUIFactory.createRadioPanel(new JRadioButton[]{copyNew, applyRp, emptyNew});
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(p,     1, row, 1, 1, GridBagConstraints.WEST);
        
        row+=1;
        label = new JLabel("�z�u���@:", SwingConstants.RIGHT);
        JPanel p2 = GUIFactory.createRadioPanel(new JRadioButton[]{placeWindow, palceTabbedPane});
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(p2,    1, row, 1, 1, GridBagConstraints.WEST);
        confirmPanel.add(gbb.getProduct());
        
        gbb = new GridBagBuilder("�J���e�ۑ���");
        row = 0;
        gbb.add(noConfirmAtSave, 0, row, 2, 1, GridBagConstraints.WEST);
        
        row++;
        label = new JLabel("�������:", SwingConstants.RIGHT);
        gbb.add(label,      0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(printCount, 1, row, 1, 1, GridBagConstraints.WEST);
        
        row++;
        label = new JLabel("�� ��:", SwingConstants.RIGHT);
        JPanel p4 = GUIFactory.createRadioPanel(new JRadioButton[]{save, saveTmp});
        gbb.add(label, 0, row, 1, 1, GridBagConstraints.EAST);
        gbb.add(p4,    1, row, 1, 1, GridBagConstraints.WEST);
        confirmPanel.add(gbb.getProduct());
        
        confirmPanel.add(Box.createVerticalStrut(200));
        confirmPanel.add(Box.createVerticalGlue());
        
        bg = new ButtonGroup();
        bg.add(copyNew);
        bg.add(applyRp);
        bg.add(emptyNew);
        
        bg = new ButtonGroup();
        bg.add(placeWindow);
        bg.add(palceTabbedPane);
        
        bg = new ButtonGroup();
        bg.add(sendAtTmp);
        bg.add(noSendAtTmp);
        
        bg = new ButtonGroup();
        bg.add(sendAtSave);
        bg.add(noSendAtSave);
        
        bg = new ButtonGroup();
        bg.add(sendAtModify);
        bg.add(noSendAtModify);
        
        bg = new ButtonGroup();
        bg.add(sendDiagnosis);
        bg.add(noSendDiagnosis);
        
        bg = new ButtonGroup();
        bg.add(save);
        bg.add(saveTmp);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("�C���X�y�N�^", inspectorPanel);
        tabbedPane.addTab("�����֘A", docPanel);
        tabbedPane.addTab("�f�Ís�ב��M", sendPanel);
        tabbedPane.addTab("�m�F�_�C�A���O", confirmPanel);
        tabbedPane.setPreferredSize(docPanel.getPreferredSize());
        
        getUI().setLayout(new BorderLayout());
        getUI().add(tabbedPane);
    }
    
    /**
     * ModelToView
     */
    private void bindModelToView() {
        
        // �����ʒu
        int curMemoLoc = model.getMemoLocation();
//        if (curMemoLoc == 0) {
//            memoTop.setSelected(true);
//        } else if (curMemoLoc == 1) {
//            memoBottom.setSelected(true);
//        } else {
//            memoBottom.setSelected(true);
//        }
        memoLocCombo.setSelectedIndex(curMemoLoc);
        
        // �C���X�y�N�^��ʂ̃��P�[�^
        boolean curLocator = model.isLocateByPlatform();
        pltform.setSelected(curLocator);
        prefLoc.setSelected(!curLocator);
        
        // �J���e�̏����\��
        boolean currentAsc = model.isAscendingKarte();
        asc.setSelected(currentAsc);
        desc.setSelected(!currentAsc);
        
        // �C������\��
        showModifiedCB.setSelected(model.isShowModifiedKarte());
        
        // ���o����
        int currentPeriod = model.getKarteExtractionPeriod();
        periodCombo.setSelectedIndex(NameValuePair.getIndex(String.valueOf(currentPeriod), periodObjects));
        
        // �J���e�̎擾����
        spinner.setValue(new Integer(model.getFetchKarteCount()));
        
        // �����J���e�̃X�N���[������
        boolean vscroll = model.isScrollKarteV();
        vSc.setSelected(vscroll);
        hSc.setSelected(!vscroll);
        
        // �a���̏����\��
        boolean currentDiagnosisAsc = model.isAscendingDiagnosis();
        diagnosisAsc.setSelected(currentDiagnosisAsc);
        diagnosisDesc.setSelected(!currentDiagnosisAsc);
        
        // �a���̒��o����
        int currentDiagnosisPeriod = model.getDiagnosisExtractionPeriod();
        diagnosisPeriodCombo.setSelectedIndex(NameValuePair.getIndex(String.valueOf(currentDiagnosisPeriod), diagnosisPeriodObjects));
        
        // �]�A�̃I�t�Z�b�g
        
        // ���{�e�X�g�̒��o����
        int currentLaboTestPeriod = model.getLabotestExtractionPeriod();
        laboTestPeriodCombo.setSelectedIndex(NameValuePair.getIndex(String.valueOf(currentLaboTestPeriod), laboTestPeriodObjects));
        
        //
        // CLAIM ���M�֌W
        // ���ۑ��̎��͑��M�ł��Ȃ��B���R�� CRC ���̓��͂���P�[�X�B
        //
        noSendAtTmp.doClick();
        sendAtTmp.setEnabled(false);
        noSendAtTmp.setEnabled(false);
        
        // �ۑ����̑��M
        if (model.isSendClaimSave()) {
            sendAtSave.doClick();
        } else {
            noSendAtSave.doClick();
        }
        
        // �C�����̑��M
        if (model.isSendClaimModify()) {
            sendAtModify.doClick();
        } else {
            noSendAtModify.doClick();
        }
        
        // �a�����M
        if (model.isSendDiagnosis()) {
            sendDiagnosis.doClick();
        } else {
            noSendDiagnosis.doClick();
        }
        
        //
        // �m�F�_�C�A���O�֌W
        //
        ActionListener al = new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                boolean enabled = noConfirmAtNew.isSelected();
                emptyNew.setEnabled(enabled);
                applyRp.setEnabled(enabled);
                copyNew.setEnabled(enabled);
                placeWindow.setEnabled(enabled);
                palceTabbedPane.setEnabled(enabled);
            }
        };
        
        ActionListener al2 = new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                boolean enabled = noConfirmAtSave.isSelected();
                printCount.setEnabled(enabled);
                save.setEnabled(enabled);
                saveTmp.setEnabled(enabled);
            }
        };
        
        // �J���e�̍쐬���[�h
        switch (model.getCreateKarteMode()) {
            case 0:
                emptyNew.setSelected(true);
                break;
                
            case 1:
                applyRp.setSelected(true);
                break;
                
            case 2:
                copyNew.setSelected(true);
                break;
        }
        
        // �z�u���@
        if (model.isPlaceKarteMode()) {
            placeWindow.setSelected(true);
        } else {
            palceTabbedPane.setSelected(true);
        }
        
        // �V�K�J���e���̊m�F�_�C���O
        boolean curConfirmAtNew = model.isConfirmAtNew();
        noConfirmAtNew.setSelected(!curConfirmAtNew);
        emptyNew.setEnabled(!curConfirmAtNew);
        applyRp.setEnabled(!curConfirmAtNew);
        copyNew.setEnabled(!curConfirmAtNew);
        placeWindow.setEnabled(!curConfirmAtNew);
        palceTabbedPane.setEnabled(!curConfirmAtNew);
        noConfirmAtNew.addActionListener(al);
        
        // �ۑ����̃f�t�H���g����
        if (model.getSaveKarteMode() == 0) {
            save.setSelected(true);
        } else {
            saveTmp.setSelected(true);
        }
        
        // �ۑ����̊m�F�_�C���O
        boolean curConfirmAtSave = model.isConfirmAtSave();
        noConfirmAtSave.setSelected(!curConfirmAtSave);
        printCount.setValue(new Integer(model.getPrintKarteCount()));
        printCount.setEnabled(!curConfirmAtSave);
        save.setEnabled(!curConfirmAtSave);
        saveTmp.setEnabled(!curConfirmAtSave);
        noConfirmAtSave.addActionListener(al2);
        
        //
        // ���̐ݒ��ʂ͏�ɗL����Ԃł���
        //
        setState(AbstractSettingPanel.State.VALID_STATE);      
        
    }
    
    /**
     * ViewToModel
     */
    private void bindViewToModel() {
        
        // �C���X�y�N�^�̃����ʒu
//        if (memoTop.isSelected()) {
//            model.setMemoLocation(0);
//        } else if (memoBottom.isSelected()) {
//            model.setMemoLocation(1);
//        }
        int loc = memoLocCombo.getSelectedIndex();
        model.setMemoLocation(loc);
               
        // �C���X�y�N�^��ʂ̃��P�[�^
        model.setLocateByPlatform(pltform.isSelected());
        
        // �J���e�̏����\��
        model.setAscendingKarte(asc.isSelected());
        
        // �J���e�̏C������\��
        model.setShowModifiedKarte(showModifiedCB.isSelected());
        
        // �J���e�̎擾����
        String value = spinner.getValue().toString();
        model.setFetchKarteCount(Integer.parseInt(value));
        
        // �����J���e�̃X�N���[������
        model.setScrollKarteV(vSc.isSelected());
        
        // �J���e�̒��o����
        String code = ((NameValuePair) periodCombo.getSelectedItem()).getValue();
        model.setKarteExtractionPeriod(Integer.parseInt(code));
        
        // �a���̏����\��
        model.setAscendingDiagnosis(diagnosisAsc.isSelected());
        
        // �a���̒��o����
        code = ((NameValuePair) diagnosisPeriodCombo.getSelectedItem()).getValue();
        model.setDiagnosisExtractionPeriod(Integer.parseInt(code));
        
        // �]�A���͎��̏I�����I�t�Z�b�g
        String val = outcomeSpinner.getValue().toString();
        prefs.putInt(Project.OFFSET_OUTCOME_DATE, Integer.parseInt(val));
        
        // ���{�e�X�g�̒��o����
        code = ((NameValuePair) laboTestPeriodCombo.getSelectedItem()).getValue();
        model.setLabotestExtractionPeriod(Integer.parseInt(code));
        
        // ���ۑ����� CLAIM ���M
        model.setSendClaimTmp(sendAtTmp.isSelected());
        
        // �ۑ����� CLAIM ���M
        model.setSendClaimSave(sendAtSave.isSelected());
        
        // �C������ CLAIM ���M
        model.setSendClaimModify(sendAtModify.isSelected());
        
        // �a���� CLAIM ���M
        model.setSendDiagnosis(sendDiagnosis.isSelected());
        
        // �V�K�J���e���̊m�F�_�C�A���O
        model.setConfirmAtNew(!noConfirmAtNew.isSelected());
        
        // �ۑ����̊m�F�_�C�A���O
        model.setConfirmAtSave(!noConfirmAtSave.isSelected());
        
        // �V�K�J���e�̍쐬���[�h
        int cMode = 0;
        if (emptyNew.isSelected()) {
            cMode = 0;
        } else if (applyRp.isSelected()) {
            cMode = 1;
        } else if (copyNew.isSelected()) {
            cMode = 2;
        }
        model.setCreateKarteMode(cMode); // 0=emptyNew, 1=applyRp, 2=copyNew
        
        // �V�K�J���e�̔z�u���@
        model.setPlaceKarteMode(placeWindow.isSelected());
        
        // �������
        Integer ival = (Integer) printCount.getValue();
        model.setPrintKarteCount(ival.intValue());
        
        // �ۑ����̃f�t�H���g����
        int sMode = save.isSelected() ? 0 : 1;
        model.setSaveKarteMode(sMode); // 0=save, 1=saveTmp
        
    }
    
    /**
     * ��ʃ��f���N���X�B
     */
    class KarteModel {
        
        // �����ʒu
        private int memoLocation;
        
        // �C���X�y�N�^��ʂ̃��P�[�^
        private boolean locateByPlatform;
        
        // �J���e�����֌W
        private int fetchKarteCount;
        private boolean ascendingKarte;
        private boolean showModifiedKarte;
        private boolean scrollKarteV;
        private int karteExtractionPeriod;
        
        // �a���֌W
        private boolean ascendingDiagnosis;
        private int diagnosisExtractionPeriod;
        
        // ���̌���
        private int labotestExtractionPeriod;
        
        //
        // CLAIM ���M�֌W
        //
        private boolean sendClaimTmp;
        private boolean sendClaimSave;
        private boolean sendClaimModify;
        private boolean sendDiagnosis;
        
        //
        // �m�F�_�C�A���O�֌W
        //
        private boolean confirmAtNew;
        private int createKarteMode;
        private boolean placeKarteMode;
        private boolean confirmAtSave;
        private int saveKarteMode;
        private int printKarteCount;
        
        /**
         * ProjectStub ���� populate ����B
         */
        public void populate(ProjectStub stub) {
            
            setMemoLocation(stub.getInspectorMemoLocation());
            
            setLocateByPlatform(stub.getLocateByPlatform());
            
            setFetchKarteCount(stub.getFetchKarteCount());
            
            setScrollKarteV(stub.getScrollKarteV());
            
            setAscendingKarte(stub.getAscendingKarte());
            
            setKarteExtractionPeriod(stub.getKarteExtractionPeriod());
            
            setShowModifiedKarte(stub.getShowModifiedKarte());
            
            setAscendingDiagnosis(stub.getAscendingDiagnosis());
            
            setDiagnosisExtractionPeriod(stub.getDiagnosisExtractionPeriod());
            
            setLabotestExtractionPeriod(stub.getLabotestExtractionPeriod());
            
            setSendClaimTmp(stub.getSendClaimTmp());
            
            setSendClaimSave(stub.getSendClaimSave());
            
            setSendClaimModify(stub.getSendClaimModify());
            
            setSendDiagnosis(stub.getSendDiagnosis());
            
            setConfirmAtNew(stub.getConfirmAtNew());
            
            setCreateKarteMode(stub.getCreateKarteMode());
            
            setPlaceKarteMode(stub.getPlaceKarteMode());
            
            setConfirmAtSave(stub.getConfirmAtSave());
            
            setPrintKarteCount(stub.getPrintKarteCount());
            
            setSaveKarteMode(stub.getSaveKarteMode());
            
        }
        
        public void restore(ProjectStub stub) {
            
            stub.setInspectorMemoLocation(getMemoLocation());
            
            stub.setLocateByPlatform(isLocateByPlatform());
            
            stub.setFetchKarteCount(getFetchKarteCount());
            
            stub.setScrollKarteV(isScrollKarteV());
            
            stub.setAscendingKarte(isAscendingKarte());
            
            stub.setKarteExtractionPeriod(getKarteExtractionPeriod());
            
            stub.setShowModifiedKarte(isShowModifiedKarte());
            
            stub.setAscendingDiagnosis(isAscendingDiagnosis());
            
            stub.setDiagnosisExtractionPeriod(getDiagnosisExtractionPeriod());
            
            stub.setLabotestExtractionPeriod(getLabotestExtractionPeriod());
            
            stub.setSendClaimTmp(isSendClaimTmp());
            
            stub.setSendClaimSave(isSendClaimSave());
            
            stub.setSendClaimModify(isSendClaimModify());
            
            stub.setSendDiagnosis(isSendDiagnosis());
            
            stub.setConfirmAtNew(isConfirmAtNew());
            
            stub.setCreateKarteMode(getCreateKarteMode());
            
            stub.setPlaceKarteMode(isPlaceKarteMode());
            
            stub.setConfirmAtSave(isConfirmAtSave());
            
            stub.setPrintKarteCount(getPrintKarteCount());
            
            stub.setSaveKarteMode(getSaveKarteMode());
            
        }
        
        public int getMemoLocation() {
            return memoLocation;
        }
        
        public void setMemoLocation(int memoLocation) {
            this.memoLocation = memoLocation;
        }
        
        public boolean isLocateByPlatform() {
            return locateByPlatform;
        }
        
        public void setLocateByPlatform(boolean locateByPlatform) {
            this.locateByPlatform = locateByPlatform;
        }
        
        public int getFetchKarteCount() {
            return fetchKarteCount;
        }
        
        public void setFetchKarteCount(int fetchKarteCount) {
            this.fetchKarteCount = fetchKarteCount;
        }
        
        public boolean isAscendingKarte() {
            return ascendingKarte;
        }
        
        public void setAscendingKarte(boolean ascendingKarte) {
            this.ascendingKarte = ascendingKarte;
        }
        
        public boolean isShowModifiedKarte() {
            return showModifiedKarte;
        }
        
        public void setShowModifiedKarte(boolean showModifiedKarte) {
            this.showModifiedKarte = showModifiedKarte;
        }
        
        public boolean isScrollKarteV() {
            return scrollKarteV;
        }
        
        public void setScrollKarteV(boolean scrollKarteV) {
            this.scrollKarteV = scrollKarteV;
        }
        
        public int getKarteExtractionPeriod() {
            return karteExtractionPeriod;
        }
        
        public void setKarteExtractionPeriod(int karteExtractionPeriod) {
            this.karteExtractionPeriod = karteExtractionPeriod;
        }
        
        public boolean isAscendingDiagnosis() {
            return ascendingDiagnosis;
        }
        
        public void setAscendingDiagnosis(boolean ascendingDiagnosis) {
            this.ascendingDiagnosis = ascendingDiagnosis;
        }
        
        public int getDiagnosisExtractionPeriod() {
            return diagnosisExtractionPeriod;
        }
        
        public void setDiagnosisExtractionPeriod(int diagnosisExtractionPeriod) {
            this.diagnosisExtractionPeriod = diagnosisExtractionPeriod;
        }
        
        public int getLabotestExtractionPeriod() {
            return labotestExtractionPeriod;
        }
        
        public void setLabotestExtractionPeriod(int laboTestExtractionPeriod) {
            this.labotestExtractionPeriod = laboTestExtractionPeriod;
        }
        
        public boolean isSendClaimTmp() {
            return sendClaimTmp;
        }
        
        public void setSendClaimTmp(boolean sendClaimTmp) {
            this.sendClaimTmp = sendClaimTmp;
        }
        
        public boolean isSendClaimSave() {
            return sendClaimSave;
        }
        
        public void setSendClaimSave(boolean sendClaimSave) {
            this.sendClaimSave = sendClaimSave;
        }
        
        public boolean isSendClaimModify() {
            return sendClaimModify;
        }
        
        public void setSendClaimModify(boolean sendClaimModify) {
            this.sendClaimModify = sendClaimModify;
        }
        
        public boolean isSendDiagnosis() {
            return sendDiagnosis;
        }
        
        public void setSendDiagnosis(boolean sendDiagnosis) {
            this.sendDiagnosis = sendDiagnosis;
        }
        
        public boolean isConfirmAtNew() {
            return confirmAtNew;
        }
        
        public void setConfirmAtNew(boolean confirmAtNew) {
            this.confirmAtNew = confirmAtNew;
        }
        
        public int getCreateKarteMode() {
            return createKarteMode;
        }
        
        public void setCreateKarteMode(int createKarteMode) {
            this.createKarteMode = createKarteMode;
        }
        
        public boolean isPlaceKarteMode() {
            return placeKarteMode;
        }
        
        public void setPlaceKarteMode(boolean placeKarteMode) {
            this.placeKarteMode = placeKarteMode;
        }
        
        public boolean isConfirmAtSave() {
            return confirmAtSave;
        }
        
        public void setConfirmAtSave(boolean confirmAtSave) {
            this.confirmAtSave = confirmAtSave;
        }
        
        public int getSaveKarteMode() {
            return saveKarteMode;
        }
        
        public void setSaveKarteMode(int saveKarteMode) {
            this.saveKarteMode = saveKarteMode;
        }
        
        public int getPrintKarteCount() {
            return printKarteCount;
        }
        
        public void setPrintKarteCount(int printKarteCount) {
            this.printKarteCount = printKarteCount;
        }
    }
        
    private void restoreDefault() {
        
        pltform.setSelected(defaultLocator);
        prefLoc.setSelected(!defaultLocator);
        asc.setSelected(defaultAsc);
        desc.setSelected(!defaultAsc);
        showModifiedCB.setSelected(defaultShowModified);
        spinner.setValue(new Integer(defaultFetchCount));
        periodCombo.setSelectedIndex(NameValuePair.getIndex(String.valueOf(defaultPeriod), periodObjects));
        vSc.setSelected(defaultScDirection);
        
        diagnosisAsc.setSelected(defaultDiagnosisAsc);
        diagnosisDesc.setSelected(!defaultDiagnosisAsc);
        diagnosisPeriodCombo.setSelectedIndex(NameValuePair.getIndex(String.valueOf(defaultDiagnosisPeriod), diagnosisPeriodObjects));
        outcomeSpinner.setValue(new Integer(defaultOffsetOutcomeDate));
        
        laboTestPeriodCombo.setSelectedIndex(NameValuePair.getIndex(String.valueOf(defaultLaboTestPeriod), laboTestPeriodObjects));
    }
}
