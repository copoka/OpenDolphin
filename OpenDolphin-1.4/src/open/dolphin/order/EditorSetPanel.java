package open.dolphin.order;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import open.dolphin.client.ClientContext;
import open.dolphin.client.StampTree;
import open.dolphin.client.StampTreeNode;
import open.dolphin.delegater.StampDelegater;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.StampModel;
import open.dolphin.util.BeanUtils;

/**
 * EditorSetPanel
 *
 * @author Minagawa,Kazushi
 *
 */
public class EditorSetPanel extends JPanel implements PropertyChangeListener, TreeSelectionListener {

    public static final String EDITOR_VALUE_PROP = "editorValue";

    // �G�f�B�^ �̑g
    private AbstractStampEditor bacteria;
    private AbstractStampEditor baseCharge;
    private AbstractStampEditor diagnosis;
    private AbstractStampEditor general;
    private AbstractStampEditor injection;
    private AbstractStampEditor instraction;
    private AbstractStampEditor med;
    private AbstractStampEditor other;
    private AbstractStampEditor physiology;
    private AbstractStampEditor radiology;
    private AbstractStampEditor surgery;
    private AbstractStampEditor test;
    private AbstractStampEditor treatment;
    
    // ��L�G�f�B�^���i�[����J�[�h�p�l��
    private JPanel cardPanel;
    private CardLayout cardLayout;

    // ���ݎg�p���̃G�f�B�^
    private AbstractStampEditor curEditor;
    
    // ����
    private Hashtable<String, AbstractStampEditor> table;

    // StampBox �Ƃ��Ƃ肷��{�^��
    private JButton rightArrow;
    private JButton leftArrow;

    // StampBox �ƘA�g���邽�߂̃I�u�W�F�N�g
    private PropertyChangeSupport boundSupport = new PropertyChangeSupport(this);
    private Object editorValue;
    private StampTreeNode selectedNode;

    
    /**
     * �ҏW�����X�^���v�I�u�W�F�N�g��Ԃ��B
     * @return �ҏW�����X�^���v�I�u�W�F�N�g
     */
    public Object getEditorValue() {
        return editorValue;
    }
    
    /**
     * �ҏW�l���Z�b�g����B���̑����͑����v���p�e�B�ł���A���X�i�֒ʒm�����B
     * @param value �ҏW���ꂽ�X�^���v
     */
    public void setEditorValue(Object value) {
        editorValue = value;
        boundSupport.firePropertyChange(EditorSetPanel.EDITOR_VALUE_PROP, null, editorValue);
        curEditor.setValue(null);
    }
    
    public StampTreeNode getSelectedNode() {
        return selectedNode;
    }
    
    /**
     * �X�^���v�{�b�N�X�őI������Ă���m�[�h�i�X�^���v�j���Z�b�g����B
     * @param node �X�^���v�{�b�N�X�őI������Ă���X�^���v�m�[�h
     */
    public void setSelectedNode(StampTreeNode node) {
        selectedNode = node;
    }

    /**
     * EditorSet ���I������B
     */
    public void close() {

        if (curEditor!=null) {
            curEditor.dispose();
            curEditor.remopvePropertyChangeListener(AbstractStampEditor.VALIDA_DATA_PROP, this);
        }
    }
    
    /**
     * �v���p�e�B�`�F���W���X�i��o�^����B
     * @param prop �v���p�e�B��
     * @param listener �v���p�e�B�`�F���W���X�i
     */
    @Override
    public void addPropertyChangeListener(String prop, PropertyChangeListener listener) {
        boundSupport.addPropertyChangeListener(prop, listener);
    }
    
    /**
     * �v���p�e�B�`�F���W���X�i���폜����B
     * @param prop �v���p�e�B��
     * @param listener �v���p�e�B�`�F���W���X�i
     */
    public void remopvePropertyChangeListener(String prop, PropertyChangeListener listener) {
        boundSupport.removePropertyChangeListener(prop, listener);
    }
    
    /**
     * �X�^���v�{�b�N�X�̃^�u���؂�ւ���ꂽ���A�Ή�����G�f�B�^�� show ����B
     * @param show ����G�f�B�^�̃G���e�B�e�B��
     */
    public void show(String entity) {

        // ORCA�y�уp�X�^�u�̏ꍇ
        if (table.get(entity)==null) {
            return;
        }
        
        // ���݃G�f�B�^������Ό�n������
        if (curEditor != null) {
            curEditor.dispose();
            curEditor.remopvePropertyChangeListener(AbstractStampEditor.VALIDA_DATA_PROP, this);
            rightArrow.setEnabled(false);
            leftArrow.setEnabled(false);
        }
        
        // �v�����ꂽ�G�f�B�^�ɐ؂�ւ���
        curEditor = table.get(entity);
        curEditor.addPropertyChangeListener(AbstractStampEditor.VALIDA_DATA_PROP, this);
        
        if (entity.equals("diagnosis")) {
            leftArrow.setEnabled(false);
            curEditor.setValue(null);
            
        } else {
            
            ModuleModel stamp = new ModuleModel();
            ModuleInfoBean stampInfo = new ModuleInfoBean();
            stampInfo.setStampName("�G�f�B�^���甭�s...");
            stampInfo.setStampRole("p");
            stampInfo.setEntity(entity);
            stamp.setModuleInfo(stampInfo);
            curEditor.setValue(stamp);
        }
        
        cardLayout.show(cardPanel, entity);
    }
    
    /**
     * �ҏW���̃X�^���v�̗L��/�����̑����ʒm���󂯁A�E�����{�^���𐧌䂷��B
     */
    public void propertyChange(PropertyChangeEvent e) {
        
        String prop = e.getPropertyName();
        
        if (prop.equals(AbstractStampEditor.VALIDA_DATA_PROP)) {

            // �L�����������ŉE���{�^���𐧌䂷��
            Boolean i = (Boolean) e.getNewValue();
            boolean state = i.booleanValue();
            rightArrow.setEnabled(state);
        }
    }
    
    /**
     * �X�^���v�c���[�őI�����ꂽ�X�^���v�ɉ����Ď�荞�݃{�^���𐧌䂷��B
     */
    public void valueChanged(TreeSelectionEvent e) {
        
        StampTree tree = (StampTree) e.getSource();
        StampTreeNode node =(StampTreeNode) tree.getLastSelectedPathComponent();
        boolean enabled = false;
        StampTreeNode selected = null;
        
        // �m�[�h���t�ŏ��a���łȂ����̂� ��荞�݃{�^���i�����j��enabled �ɂ���
        // �܂����̎��ȊO�͑I���m�[�h������null�ɂ���
        if (node != null && node.isLeaf()) {
            
            ModuleInfoBean info = (ModuleInfoBean) node.getUserObject();
               
            if (info.isSerialized() && (!info.getEntity().equals(IInfoModel.ENTITY_DIAGNOSIS)) ) {
                enabled = true;
                selected = node;
            }
        }
        
        leftArrow.setEnabled(enabled);
        setSelectedNode(selected);
    }
    
    /**
     * �ҏW�����X�^���v���{�b�N�X�֒ʒm���邽�߂̃A�N�V�������X�i�B
     * �E�����{�^���̃��X�i�ŃG�f�B�^�̕ҏW�l��get�������v���p�e�B�ɐݒ肷��B
     * �Ō�ɉE�����{�^����disabled�ɂ���B
     */
    class EditorValueListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {

            // cureditor ����l���擾���A�������g�̃v���p�e�B�ɐݒ肷��B
            // �����v���p�e�B�ɂ�胊�X�i�ւ��̒l���ʒm�����B
            Object obj = curEditor.getValue();
            setEditorValue(obj);
            rightArrow.setEnabled(false);
        }
    }
    
    /**
     * �X�^���v�{�b�N�X�őI������Ă���X�^���v���G�f�B�^�֎�荞��ŕҏW���邽�߂�
     * �������{�^���̃��X�i�N���X�B
     */
    class EditStampListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            
            // StampInfo����X�^���v�����[�h���G�f�B�^�ɃZ�b�g����
            StampTreeNode node = getSelectedNode();
            if (node == null || !(node.getUserObject() instanceof ModuleInfoBean)) {
                return;
            }
            final ModuleInfoBean stampInfo = (ModuleInfoBean) node.getUserObject();

            Runnable r = new Runnable() {

                public void run() {

                    final StampDelegater sdl = new StampDelegater();
                    final StampModel stampModel = sdl.getStamp(stampInfo.getStampId());

                    Runnable awt = new Runnable() {

                        public void run() {
                            
                            if (sdl.isNoError() && stampModel != null) {
                                if (stampModel != null) {
                                    IInfoModel model = (IInfoModel) BeanUtils.xmlDecode(stampModel.getStampBytes());
                                    if (model != null) {
                                        ModuleModel stamp = new ModuleModel();
                                        stamp.setModel(model);
                                        stamp.setModuleInfo(stampInfo);
                                        if (curEditor != null) {
                                            curEditor.setValue(stamp);
                                        }
                                    }
                                }
                            } else {
                                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(cardPanel),
                                        sdl.getErrorMessage(),
                                        ClientContext.getFrameTitle("Stamp�擾"),
                                        JOptionPane.WARNING_MESSAGE);
                            }
                        }
                    };

                    SwingUtilities.invokeLater(awt);
                }
            };

            Thread t = new Thread(r);
            t.setPriority(Thread.NORM_PRIORITY);
            t.start();
        }
    }
    
    /**
     * GUI �R���|�[�l���g�𐶐�����B
     */
    private void initComponent() {
        
        // �ҏW�����X�^���v���{�b�N�X�֓o�^����E�����{�^���𐶐�����
        rightArrow = new JButton(ClientContext.getImageIcon("forwd_16.gif"));
        rightArrow.addActionListener(new EditorValueListener());
        rightArrow.setEnabled(false);
        
        // �X�^���v�{�b�N�X�̃X�^���v���Z�b�g�e�[�u���֎�荞�ލ������̃{�^���𐶐�����
        leftArrow = new JButton(ClientContext.getImageIcon("back_16.gif"));
        leftArrow.addActionListener(new EditStampListener());
        leftArrow.setEnabled(false);

        //
        // �ʂ̃G�f�B�^�𐶐�����
        //
        bacteria = new BaseEditor("bacteriaOrder", false);
        baseCharge = new BaseEditor("baseChargeOrder", false);
        diagnosis = new DiseaseEditor(false);
        general = new BaseEditor("generalOrder", false);
        injection = new BaseEditor("injectionOrder", false);
        instraction = new BaseEditor("instractionChargeOrder", false);
        med = new RpEditor("medOrder", false);
        other = new BaseEditor("otherOrder", false);
        physiology = new BaseEditor("physiologyOrder", false);
        radiology = new RadEditor("radiologyOrder", false);
        surgery = new BaseEditor("surgeryOrder", false);
        test = new BaseEditor("testOrder", false);
        treatment = new BaseEditor("treatmentOrder", false);

        // Hash�e�[�u���ɓo�^�� show(entity) �Ŏg�p����
        table = new Hashtable<String, AbstractStampEditor>();
        table.put("bacteriaOrder", bacteria);
        table.put("baseChargeOrder", baseCharge);
        table.put("diagnosis", diagnosis);
        table.put("generalOrder", general);
        table.put("injectionOrder", injection);
        table.put("instractionChargeOrder", instraction);
        table.put("medOrder", med);
        table.put("otherOrder", other);
        table.put("physiologyOrder", physiology);
        table.put("radiologyOrder", radiology);
        table.put("surgeryOrder", surgery);
        table.put("testOrder", test);
        table.put("treatmentOrder", treatment);

        //
        // �J�[�h�p�l���ɃG�f�B�^��ǉ�����
        //
        cardPanel.add(bacteria.getView(), "bacteriaOrder");
        cardPanel.add(baseCharge.getView(), "baseChargeOrder");
        cardPanel.add(diagnosis.getView(), "diagnosis");
        cardPanel.add(general.getView(), "generalOrder");
        cardPanel.add(injection.getView(), "injectionOrder");
        cardPanel.add(instraction.getView(), "instractionChargeOrder");
        cardPanel.add(med.getView(), "medOrder");
        cardPanel.add(other.getView(), "otherOrder");
        cardPanel.add(physiology.getView(), "physiologyOrder");
        cardPanel.add(radiology.getView(), "radiologyOrder");
        cardPanel.add(surgery.getView(), "surgeryOrder");
        cardPanel.add(test.getView(), "testOrder");
        cardPanel.add(treatment.getView(), "treatmentOrder");
        
        // StampBox �Ƃ̊Ԃɂ�����{�^���p�l��
        JPanel btnPanel = new JPanel();
        BoxLayout box = new BoxLayout(btnPanel, BoxLayout.Y_AXIS);
        btnPanel.setLayout(box);
        btnPanel.add(Box.createVerticalStrut(100));
        btnPanel.add(rightArrow);
        btnPanel.add(leftArrow);
        btnPanel.add(Box.createVerticalGlue());

        // �z�u����
        this.setLayout(new BorderLayout(0, 0));
        this.add(cardPanel, BorderLayout.CENTER);
        this.add(btnPanel, BorderLayout.EAST);
    }

    /** EditorSetPanel �𐶐�����B */
    public EditorSetPanel() {
        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);
        initComponent();
    }
}
