/*
 * AllergyInspector.java
 *
 * Created on 2007/01/18, 18:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package open.dolphin.client;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.im.InputSubset;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentListener;
import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.infomodel.AllergyModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModelUtils;
import open.dolphin.infomodel.ObservationModel;
import open.dolphin.infomodel.SimpleDate;
import open.dolphin.project.Project;

/**
 *
 * @author kazm
 */
public class AllergyInspector {
    
    // �A�����M�[�e�[�u��
    private ObjectListTable allergyTable;

    // �폜�{�^��
    private JButton deleteBtn;

    // �ǉ��{�^��
    private JButton addBtn;

    // �v�����͗p�e�L�X�g�t�B�[���h
    private JTextField factorField;

    // �������x��I�����邽�߂̃R���{�{�b�N�X
    private JComboBox severityCombo;

    // ���������͂��邽�߂̃e�L�X�g�t�B�[���h
    private JTextField confirmDateField;

    // ��������͂��邽�߂̃e�L�X�g�t�B�[���h
    private JTextField memoField;

    // �������x��
    private JLabel memoLabel;

    // �R���e�i�p�l��
    private JPanel allergyPanel;

    // �{�^���R���g���[���t���O
    private boolean ok;
    
    private ChartPlugin context;
    

    /**
     * AllergyInspector�I�u�W�F�N�g�𐶐�����B
     */
    public AllergyInspector(ChartPlugin context) {
        this.context = context;
        initComponents();
        update();
    }

    public void clear() {
        if (allergyTable != null) {
            allergyTable.clear();
        }
    }

    /**
     * ���C�A�E�g�p�l����Ԃ��B
     * @return
     */
    public JPanel getPanel() {
        return allergyPanel;
    }

    /**
     * GUI�R���|�[�l���g������������B
     */
    private void initComponents() {
        
        // �A�����M�e�[�u���̎d�l
        String[] columnNames = ClientContext.getStringArray("patientInspector.allergyInspector.columnNames");
        int startNumRows = ClientContext.getInt("patientInspector.allergyInspector.startNumRows");
        String[] methodNames = ClientContext.getStringArray("patientInspector.allergyInspector.methodNames");
        allergyTable = new ObjectListTable(columnNames, startNumRows, methodNames, null);
        String[] severityValue = ClientContext.getStringArray("patientInspector.allergyInspector.severity.values"); // {"severe","moderate","mild","noReaction"};
        int[] fieldLength = ClientContext.getIntArray("patientInspector.allergyInspector.fieldLength"); // 10,15

        // �ǉ��A�폜�{�^���̃A�C�R��
        ImageIcon addIcon = ClientContext.getImageIcon("add_16.gif");
        ImageIcon deleteIcon = ClientContext.getImageIcon("del_16.gif");

        // �ǉ��{�^�� �폜�{�^��
        deleteBtn = new JButton(deleteIcon);
        deleteBtn.setEnabled(false);
        deleteBtn.setMargin(new Insets(2, 2, 2, 2));
        
        addBtn = new JButton(addIcon);
        addBtn.setEnabled(false);
        addBtn.setMargin(new Insets(2, 2, 2, 2));
        
        confirmDateField = new JTextField(fieldLength[0]);
        String datePattern = ClientContext.getString("common.pattern.mmlDate");
        confirmDateField.setDocument(new RegexConstrainedDocument(datePattern));

        // �I�����ꂽ�A�����M�[�f�[�^���폜����
        allergyTable.addPropertyChangeListener(ObjectListTable.SELECTED_OBJECT, 
                (PropertyChangeListener) EventHandler.create(PropertyChangeListener.class, this, "rowSelectionChanged", ""));

        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                addCheck();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                addCheck();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                addCheck();
            }
        };
        
        // �v�����͗p�e�L�X�g�t�B�[���h
        factorField = new JTextField(fieldLength[0]);
        factorField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent event) {
                factorField.getInputContext().setCharacterSubsets(new Character.Subset[] {InputSubset.KANJI});
            }
        });

        // �������x��I�����邽�߂̃R���{�{�b�N�X
        severityCombo = new JComboBox(severityValue);

        // ��������͂��邽�߂̃e�L�X�g�t�B�[���h
        memoField = new JTextField(fieldLength[1]);
        memoField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent event) {
                memoField.getInputContext().setCharacterSubsets(new Character.Subset[] {InputSubset.KANJI});
            }
        });

        factorField.getDocument().addDocumentListener(dl);
        confirmDateField.getDocument().addDocumentListener(dl);
        severityCombo.addItemListener((ItemListener) EventHandler.create(ItemListener.class, this, "severityChanged", ""));
        
        addBtn.addActionListener((ActionListener) EventHandler.create(ActionListener.class, this, "add"));
        addBtn.setToolTipText("�A�����M�[��ǉ����܂�");

        deleteBtn.addActionListener((ActionListener) EventHandler.create(ActionListener.class, this, "delete"));
        deleteBtn.setToolTipText("�I�������A�����M�[���폜���܂�");

        // ������Ƀ|�b�v�A�b�v�J�����_��ݒ肷��
        new PopupListener(confirmDateField);
        confirmDateField.setToolTipText("�E�N���b�N�ŃJ�����_�[���|�b�v�A�b�v���܂�");
        confirmDateField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent event) {
                confirmDateField.getInputContext().setCharacterSubsets(null);
            }
        });

        JPanel cmdPanel = new JPanel();
        cmdPanel.add(deleteBtn);
        cmdPanel.add(addBtn);
        
        // ���\�[�X���烉�x����������擾����
        String[] labelTexts = ClientContext.getStringArray("patientInspector.allergyInspector.labelTexts");// �v��,�������x,�����,����

        // �v�����x��
        JLabel factorLabel = new JLabel(labelTexts[0], SwingConstants.RIGHT);

        // �������x���x��
        JLabel severityLabel = new JLabel(labelTexts[1], SwingConstants.RIGHT);

        // ��������x��
        JLabel confirmDateLabel = new JLabel(labelTexts[2], SwingConstants.RIGHT);
        
        memoLabel = new JLabel(labelTexts[3], SwingConstants.RIGHT);

        GridBagBuilder gb = new GridBagBuilder();
        gb.add(factorLabel, 0, 0, GridBagConstraints.EAST);
        gb.add(factorField, 1, 0, GridBagConstraints.WEST);
        gb.add(severityLabel, 0, 1, GridBagConstraints.EAST);
        gb.add(severityCombo, 1, 1, GridBagConstraints.WEST);
        gb.add(confirmDateLabel, 0, 2, GridBagConstraints.EAST);
        gb.add(confirmDateField, 1, 2, GridBagConstraints.WEST);
        gb.add(memoLabel, 0, 3, GridBagConstraints.EAST);
        gb.add(memoField, 1, 3, GridBagConstraints.WEST);
        JPanel sip = gb.getProduct();

        allergyPanel = new JPanel();
        allergyPanel.setLayout(new BoxLayout(allergyPanel, BoxLayout.Y_AXIS));
        allergyPanel.add(allergyTable.getPanel());
        allergyPanel.add(cmdPanel);
        allergyPanel.add(sip);

        //�{���I�u�W�F�N�g
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(IInfoModel.DATE_WITHOUT_TIME);
        String todayString = sdf.format(date);
        confirmDateField.setText(todayString);
    }

    /**
     * �A�����M�[����\������B
     */
    @SuppressWarnings("unchecked")
    public void update() {
        List list = context.getKarte().getEntryCollection("allergy");
        allergyTable.setObjectList(list);
    }

    /**
     * �ǉ��{�^���̃R���g���[�����s���^
     */
    private void addCheck() {
        boolean newOk
                = (factorField.getText().trim().equals("") == false && confirmDateField.getText().trim().equals("") == false)
                ? true
                : false;

        if (newOk != ok) {
            addBtn.setEnabled(newOk);
            ok = newOk;
        }
    }
    
    public void rowSelectionChanged(PropertyChangeEvent e) {
        if (e.getPropertyName().equals(
                ObjectListTable.SELECTED_OBJECT)) {
            Object[] selected = (Object[]) e.getNewValue();
            boolean canDelete
                    = (selected != null && selected.length > 0)
                    ? true
                    : false;
            if (canDelete != deleteBtn.isEnabled()) {
                deleteBtn.setEnabled(canDelete);
            }
        }
    }
    
    public void severityChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            addCheck();
        }
    }

    /**
     * �A�����M�[�f�[�^��ǉ�����B
     */
    public void add() {

        final AllergyModel model = new AllergyModel();
        model.setFactor(factorField.getText().trim());
        model.setSeverity((String)severityCombo.getSelectedItem());
        model.setIdentifiedDate(confirmDateField.getText().trim());
        model.setMemo(memoField.getText().trim());

        // GUI �̓������TimeStamp�ɕύX����
        Date date = ModelUtils.getDateTimeAsObject(model.getIdentifiedDate()+"T00:00:00");

        final List<ObservationModel> addList = new ArrayList<ObservationModel>(1);

        ObservationModel observation = new ObservationModel();
        observation.setKarte(context.getKarte());
        observation.setCreator(Project.getUserModel());
        observation.setObservation(IInfoModel.OBSERVATION_ALLERGY);
        observation.setPhenomenon(model.getFactor());
        observation.setCategoryValue(model.getSeverity());
        observation.setConfirmed(date);
        observation.setRecorded(new Date());
        observation.setStarted(date);
        observation.setStatus(IInfoModel.STATUS_FINAL);
        observation.setMemo(model.getMemo());
        addList.add(observation);

        //worker thread
        Runnable r = new Runnable() {
            public void run() {
                fireStart();
                DocumentDelegater ddl = new DocumentDelegater();
                // �o�^���Ƀ��R�[�hID��Ԃ�
                List<Long> ids = ddl.addObservations(addList);
                model.setObservationId(ids.get(0));
                allergyTable.addRow(model);
                fireStop();
            }
        };
        addBtn.setEnabled(false);
        Thread t = new Thread(r);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

    /**
     * �ǉ��{�^����񊈐����� progressbar ���J�n����B
     */
    private void fireStart() {
        Runnable awt = new Runnable() {
            public void run() {
                context.getStatusPanel().start();
            }
        };
        SwingUtilities.invokeLater(awt);
    }

    /**
     * ProgressBar ���X�g�b�v���ǉ��{�^���� enable �����𐧌䂷��B
     */
    private void fireStop() {
        Runnable awt = new Runnable() {
            public void run() {
                context.getStatusPanel().stop();
            }
        };
        SwingUtilities.invokeLater(awt);
    }

    /**
     * �e�[�u���őI�������A�����M�[���폜����B
     */
    public void delete() {
        Object[] selected = allergyTable.getSelectedObject();
        AllergyModel model = (AllergyModel) selected[0];

        final List<Long> list = new ArrayList<Long>(1);
        list.add(new Long(model.getObservationId()));

        Runnable r = new Runnable() {
            public void run() {
                fireStart();
                DocumentDelegater ddl = new DocumentDelegater();
                ddl.removeObservations(list);
                allergyTable.deleteSelectedRows();
                fireStop();
            }
        };
        deleteBtn.setEnabled(false);
        Thread t = new Thread(r);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }
    
    
    class PopupListener extends MouseAdapter implements PropertyChangeListener {
        
        private JPopupMenu popup;
        
        private JTextField tf;
        
        // private LiteCalendarPanel calendar;
        
        public PopupListener(JTextField tf) {
            this.tf = tf;
            tf.addMouseListener(this);
        }
        
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        private void maybeShowPopup(MouseEvent e) {
            
            if (e.isPopupTrigger()) {
                popup = new JPopupMenu();
                CalendarCardPanel cc = new CalendarCardPanel(context.getContext().getEventColorTable());
                cc.addPropertyChangeListener(CalendarCardPanel.PICKED_DATE, this);
                cc.setCalendarRange(new int[] { -12, 0 });
                popup.insert(cc, 0);
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
        
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals(CalendarCardPanel.PICKED_DATE)) {
                SimpleDate sd = (SimpleDate) e.getNewValue();
                tf.setText(SimpleDate.simpleDateToMmldate(sd));
                popup.setVisible(false);
                popup = null;
            }
        }
    }
}
