package open.dolphin.order;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import open.dolphin.client.ClientContext;
import open.dolphin.client.GUIConst;
import open.dolphin.client.IStampEditorDialog;
import open.dolphin.client.StampEditorDialog;
import open.dolphin.client.StampModelEditor;
import open.dolphin.client.StampTree;
import open.dolphin.client.StampTreeNode;
import open.dolphin.client.TaskTimerMonitor;
import open.dolphin.delegater.StampDelegater;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.StampModel;
import open.dolphin.util.BeanUtils;
import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;

/**
 * EditorSetPanel
 *
 * @author Minagawa,Kazushi
 *
 */
public class EditorSetPanel extends JPanel implements IStampEditorDialog, PropertyChangeListener, TreeSelectionListener {
    
    private LBacteriaStampEditor bacteria;
    private LBaseChargeStampEditor baseCharge;
    private LDiagnosisEditor diagnosis;
    private LGeneralStampEditor general;
    private LInjectionStampEditor injection;
    private LInstractionChargeStampEditor instraction;
    private LMedStampEditor2 med;
    private LOtherStampEditor other;
    private LPhysiologyStampEditor physiology;
    private LRadiologyStampEditor radiology;
    private LSurgeryStampEditor surgery;
    private LTestStampEditor test;
    private LTreatmentStampEditor treatment;
    private MasterSetPanel masterSet;
    
    private StampModelEditor curEditor;
    
    private JPanel editorSet;
    private CardLayout cardLayout;
    
    private Hashtable<String, StampModelEditor> table;
    
    private JButton rightArrow;
    private JButton leftArrow;
    
    private PropertyChangeSupport boundSupport = new PropertyChangeSupport(this);
    private Object editorValue;
    private StampTreeNode selectedNode;
    
    private ApplicationContext appCtx;
    private Application app;
    private Logger logger;
    
    /** EditorSetPanel �𐶐�����B */
    public EditorSetPanel() {
        appCtx = ClientContext.getApplicationContext();
        app = appCtx.getApplication();
        logger = ClientContext.getBootLogger();
        editorSet = new JPanel();
        cardLayout = new CardLayout();
        editorSet.setLayout(cardLayout);
        initComponent();
    }
    
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
        boundSupport.firePropertyChange(IStampEditorDialog.EDITOR_VALUE_PROP, null, editorValue);
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
    
    public void close() {
        if (curEditor != null) {
            curEditor.dispose();
        }
        masterSet.dispose();
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
        
        // ���݃G�f�B�^������Ό�n������
        if (curEditor != null) {
            curEditor.dispose();
            curEditor.removePropertyChangeListener(StampEditorDialog.VALIDA_DATA_PROP, this);
            rightArrow.setEnabled(false);
            leftArrow.setEnabled(false);
        }
        
        // �v�����ꂽ�G�f�B�^���J�n����
        curEditor = table.get(entity);
        // ���̃N���X�� VALID_DATA_PROP �̃��X�i�ɂȂ��Ă���
        curEditor.addPropertyChangeListener(StampEditorDialog.VALIDA_DATA_PROP, this);
        curEditor.start();
        
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
        
        if (curEditor instanceof LRadiologyStampEditor) {
            masterSet.setRadLocationEnabled(true);
        } else {
            masterSet.setRadLocationEnabled(false);
        }
        
        cardLayout.show(editorSet, entity);
    }
    
    /**
     * �ҏW���̃X�^���v�̗L��/�����̑����ʒm���󂯁A�X�^���v�{�b�N�X�֓o�^����
     * �E�����{�^���𐧌䂷��B
     */
    public void propertyChange(PropertyChangeEvent e) {
        
        String prop = e.getPropertyName();
        
        if (prop.equals(StampEditorDialog.VALIDA_DATA_PROP)) {
            
            Boolean i = (Boolean) e.getNewValue();
            boolean state = i.booleanValue();
            
            if (state) {
                rightArrow.setEnabled(true);
            } else {
                rightArrow.setEnabled(false);
            }   
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
        
        // �m�[�h���t�ŏ��a���łȂ����̂� enabled �ɂ���
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
    
    public JButton getOkButton() {
        return null;
    }
    
    /**
     * �ҏW�����X�^���v���{�b�N�X�֒ʒm���邽�߂̃A�N�V�������X�i�B
     * �E�����{�^���̃��X�i�ŃG�f�B�^�̕ҏW�l��get�������v���p�e�B�ɐݒ肷��B
     * �Ō�ɉE�����{�^����disabled�ɂ���B
     */
    class EditorValueListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            Object obj = curEditor.getValue();
            setEditorValue(obj);
            rightArrow.setEnabled(false);
        }
    }
    
    /**
     * �X�^���v�{�b�N�X�őI������Ă���X�^���v���G�f�B�^�֎�荞��ŕҏW���邽�߂�
     * �������{�^���̃��X�i�B
     */
    class EditStampListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            
            // StampInfo����X�^���v�����[�h���G�f�B�^�ɃZ�b�g����
            StampTreeNode node = getSelectedNode();
            if (node == null || !(node.getUserObject() instanceof ModuleInfoBean)) {
                return;
            }
            final ModuleInfoBean stampInfo = (ModuleInfoBean) node.getUserObject();
            
            final StampDelegater sdl = new StampDelegater();
            
            int maxEstimation = 60*1000;
            int delay = 200;
            String note = "�������Ă��܂�...";
            
            Task task = new Task<StampModel, Void>(app) {

                @Override
                protected StampModel doInBackground() throws Exception {
                    StampModel result = sdl.getStamp(stampInfo.getStampId());
                    return result;
                }

                @Override
                protected void succeeded(StampModel stampModel) {
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
                        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(editorSet),
                                sdl.getErrorMessage(),
                                ClientContext.getFrameTitle("Stamp�擾"),
                                JOptionPane.WARNING_MESSAGE);
                    }
                }

                @Override
                protected void cancelled() {
                    logger.debug("Task cancelled");
                }

                @Override
                protected void failed(java.lang.Throwable cause) {
                    logger.warn(cause.getMessage());
                }

                @Override
                protected void interrupted(java.lang.InterruptedException e) {
                    logger.warn(e.getMessage());
                }
            };
            
            TaskMonitor taskMonitor = appCtx.getTaskMonitor();
            String message = "�X�^���v��";
            Component c = SwingUtilities.getWindowAncestor(editorSet);
            TaskTimerMonitor w = new TaskTimerMonitor(task, taskMonitor, c, message, note, delay, maxEstimation);
            taskMonitor.addPropertyChangeListener(w);

            appCtx.getTaskService().execute(task);
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
        
        // �}�X�^�[�Z�b�g�p�l���𐶐�����
        masterSet = new MasterSetPanel();
        
        // �G�f�B�^(�Z�b�g�e�[�u��)�𐶐�����
        bacteria = new LBacteriaStampEditor(this, masterSet);
        baseCharge = new LBaseChargeStampEditor(this, masterSet);
        diagnosis = new LDiagnosisEditor(this, masterSet);
        general = new LGeneralStampEditor(this, masterSet);
        injection = new LInjectionStampEditor(this, masterSet);
        instraction = new LInstractionChargeStampEditor(this, masterSet);
        med = new LMedStampEditor2(this, masterSet);
        other = new LOtherStampEditor(this, masterSet);
        physiology = new LPhysiologyStampEditor(this, masterSet);
        radiology = new LRadiologyStampEditor(this, masterSet);
        surgery = new LSurgeryStampEditor(this, masterSet);
        test = new LTestStampEditor(this, masterSet);
        treatment = new LTreatmentStampEditor(this, masterSet);
        
        // �J�[�h�p�l���ɃG�f�B�^��ǉ�����
        editorSet.add(bacteria, "bacteriaOrder");
        editorSet.add(baseCharge, "baseChargeOrder");
        editorSet.add(diagnosis, "diagnosis");
        editorSet.add(general, "generalOrder");
        editorSet.add(injection, "injectionOrder");
        editorSet.add(instraction, "instractionChargeOrder");
        editorSet.add(med, "medOrder");
        editorSet.add(other, "otherOrder");
        editorSet.add(physiology, "physiologyOrder");
        editorSet.add(radiology, "radiologyOrder");
        editorSet.add(surgery, "surgeryOrder");
        editorSet.add(test, "testOrder");
        editorSet.add(treatment, "treatmentOrder");
        
        // �J�[�h�p�l���� PreferedSize ��ݒ肷��
        editorSet.setPreferredSize(new Dimension(GUIConst.DEFAULT_EDITOR_WIDTH, GUIConst.DEFAULT_EDITOR_HEIGHT));
        
        // �z�u����
        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.add(editorSet, BorderLayout.NORTH);
        center.add(masterSet, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel();
        BoxLayout box = new BoxLayout(btnPanel, BoxLayout.Y_AXIS);
        btnPanel.setLayout(box);
        btnPanel.add(Box.createVerticalStrut(100));
        btnPanel.add(rightArrow);
        btnPanel.add(leftArrow);
        btnPanel.add(Box.createVerticalGlue());
        
        this.setLayout(new BorderLayout(0, 0));
        this.add(center, BorderLayout.CENTER);
        this.add(btnPanel, BorderLayout.EAST);
        
        // �S�̂� PreferedSize ��ݒ肷��
        this.setPreferredSize(GUIConst.DEFAULT_STAMP_EDITOR_SIZE);
        this.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 0));
        
        // Hash�e�[�u���ɓo�^�� show(entity) �Ŏg�p����
        table = new Hashtable<String, StampModelEditor>();
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
    }
}
