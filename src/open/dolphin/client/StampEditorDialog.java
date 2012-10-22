package open.dolphin.client;

import javax.swing.*;
import open.dolphin.helper.ComponentMemory;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import org.apache.log4j.Logger;

/**
 * Stamp �ҏW�p�̊O�g��񋟂��� Dialog.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class StampEditorDialog implements IStampEditorDialog, PropertyChangeListener {
    
    public static final String VALIDA_DATA_PROP = "validData";
    public static final String VALUE_PROP = "value";
    
    /** stampEditor �̃v���O�|�C���g */
    private static final String EDITOR_PLUG_POINT = "karteEditor/stampEditor";
    
    /** command buttons */
    private static final String OK_ICON_KARTE = "/open/dolphin/resources/images/lgicn_16.gif";
    private static final String OK_ICON_STAMPBOX = "/open/dolphin/resources/images/tools_16.gif";
    private JButton okButton;
    
    /** target editor */
    private IStampModelEditor editor;
    private PropertyChangeSupport boundSupport;
    
    private JDialog dialog;
    private String entity;
    private Object value;
    private boolean toKarte;
    private BlockGlass glass;
    
    private static final int DEFAULT_X = 159;
    private static final int DEFAULT_Y = 67;
    private static final int DEFAULT_WIDTH = 924;
    private static final int DEFAULT_HEIGHT = 616;
    
    private Logger logger;
    
    /**
     * Constructor. Use layered inititialization pattern.
     */
    public StampEditorDialog(String entity, Object value, boolean toKarte)  {
        this.entity = entity;
        this.value = value;
        this.toKarte = toKarte;
        boundSupport = new PropertyChangeSupport(this);
        logger = ClientContext.getBootLogger();
    }
    
    /**
     * Constructor. Use layered inititialization pattern.
     */
    public StampEditorDialog(String entity, Object value)  {
        this(entity, value, true);
    }
    
    /**
     * �G�f�B�^���J�n����B
     */
    public void start() {
        
        Runnable initilizer = new Runnable() {
            public void run() {
                initialize();
            }
        };
        Thread t = new Thread(initilizer);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
        //initialize();
    }
    
    /**
     * GUI�R���|�[�l���g������������B
     */
    private void initialize() {
        
        // �J���e�ɓW�J���邩�X�^���v�{�b�N�X�ɕۑ����邩��
        // ���[�_�������y�у{�^���̃A�C�R���ƃc�[���`�b�v��ς���
        if (toKarte) {
            dialog = new JDialog((Frame) null, true);
            okButton = new JButton(createImageIcon(OK_ICON_KARTE));
            okButton.setToolTipText("�J���e�ɓW�J���܂�");
        } else {
            dialog = new JDialog((Frame) null, false);
            okButton = new JButton(createImageIcon(OK_ICON_STAMPBOX));
            okButton.setToolTipText("�X�^���v�{�b�N�X�ɕۑ����܂�");
        }
            
        //
        // BlockGlass �𐶐��� dialog �ɐݒ肷��
        //
        glass = new BlockGlass();
        dialog.setGlassPane(glass);
        
        // OK �{�^���Ƃ��̃A�N�V�����𐶐�����
        ActionListener action = (ActionListener) (GenericListener.create(
                ActionListener.class,
                "actionPerformed",
                this,
                "okButtonClicked"));
        okButton.addActionListener(action);
        okButton.setMnemonic('O');
        okButton.setEnabled(false);
        
        // Cancel button�@�Ƃ��̃A�N�V�����𐶐�����
        //action = (ActionListener) (GenericListener.create (
        // ActionListener.class,
        // "actionPerformed",
        // this,
        // "cancelButtonClicked"));
        //cancelButton.addActionListener(action);
        //cancelButton.setMnemonic('C');
        
        // ���ۂ́i�����ƂȂ�j�G�f�B�^�𐶐����� Dialog �� add ����
        try {
            editor = createEditor(this.entity);
            editor.setContext(this);
            editor.start();
            editor.addPropertyChangeListener(VALIDA_DATA_PROP, this);
            editor.setValue(value);
            
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn(e.getMessage());
            return;
        }
        
        // ���A�C�E�g����
        JPanel panel = new JPanel(new BorderLayout(0, 11));
        panel.add((Component) editor, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        
        // CloseBox ������o�^����
        dialog.addWindowListener(new WindowAdapter() {
            
            @Override
            public void windowClosing(WindowEvent e) {
                // CloseBox ���N���b�N���ꂽ�ꍇ�̓L�����Z���Ƃ���
                value = null;
                close();
            }
        });
        
        dialog.setTitle(editor.getTitle());
        ComponentMemory cm = new ComponentMemory(dialog, new Point(DEFAULT_X,DEFAULT_Y), new Dimension(DEFAULT_WIDTH,DEFAULT_HEIGHT), this);
        cm.setToPreferenceBounds();
        
        dialog.setVisible(true);
    }
    
    /**
     * �ҏW���� Stamp ��Ԃ��B
     */
    public Object getValue() {
        return editor.getValue();
    }
    
    /**
     * �v���p�e�B�`�F���W���X�i��o�^����B
     * @param prop �v���p�e�B��
     * @param listener �v���p�e�B�`�F���W���X�i
     */
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
     * OK�{�^����Ԃ��B����̓G�f�B�^�Ƀ��C�A�E�g�����B
     * @return OK�{�^��
     */
    public JButton getOkButton() {
        return okButton;
    }
    
    /**
     * OK�{�^�����N���b�N����B
     * �G�f�B�^�ŕҏW�����X�^���v��v�����ƂɕԂ��B
     * @param e ActionEvent
     */
    public void okButtonClicked(ActionEvent e) {
        value = getValue();
        if (toKarte) {
            close();
        } else {
            boundSupport.firePropertyChange(VALUE_PROP, null, value);
        }
    }
    
    public void addStampButtonClicked(ActionEvent e) {
        value = null;
        close();
    }
    
    /**
     * �ҏW���̃��f���l���L���Ȓl���ǂ����̒ʒm���󂯁A
     * �J���e�ɓW�J�{�^���� enable/disable �ɂ���
     */
    public void propertyChange(PropertyChangeEvent evt) {
        
        Boolean i = (Boolean)evt.getNewValue();
        boolean state = i.booleanValue();
        
        if (state) {
            okButton.setEnabled(true);
        } else {
            okButton.setEnabled(false);
        }
    }
    
    /**
     * �_�C�A���O�����
     */
    public void close() {
        editor.dispose();
        dialog.setVisible(false);
        dialog.dispose();
        boundSupport.firePropertyChange(VALUE_PROP, null, value);
    }
    
    private ImageIcon createImageIcon(String name) {
        return new ImageIcon(this.getClass().getResource(name));
    }
    
    
    private IStampModelEditor createEditor(String entity) {
        
        IStampModelEditor ret = null;
        
        if (entity.equals("diagnosis")) {
            ret = new open.dolphin.order.DiagnosisEditor();
        
        } else if (entity.equals("medOrder")) {
            ret = new open.dolphin.order.MedStampEditor2();
        
        } else if (entity.equals("injectionOrder")) {
            ret = new open.dolphin.order.InjectionStampEditor();
            
        } else if (entity.equals("testOrder")) {
            ret = new open.dolphin.order.TestStampEditor();
            
        } else if (entity.equals("bacteriaOrder")) {
            ret = new open.dolphin.order.BacteriaStampEditor();
            
        } else if (entity.equals("physiologyOrder")) {
            ret = new open.dolphin.order.PhysiologyStampEditor();
            
        } else if (entity.equals("treatmentOrder")) {
            ret = new open.dolphin.order.TreatmentStampEditor();
            
        } else if (entity.equals("radiologyOrder")) {
            ret = new open.dolphin.order.RadiologyStampEditor();
            
        } else if (entity.equals("baseChargeOrder")) {
            ret = new open.dolphin.order.BaseChargeStampEditor();
            
        } else if (entity.equals("instractionChargeOrder")) {
            ret = new open.dolphin.order.InstractionChargeStampEditor();
            
        } else if (entity.equals("otherOrder")) {
            ret = new open.dolphin.order.OtherStampEditor();
            
        } else if (entity.equals("generalOrder")) {
            ret = new open.dolphin.order.GeneralStampEditor();
            
        } else if (entity.equals("surgeryOrder")) {
            ret = new open.dolphin.order.SurgeryStampEditor();
            
        }
        
        logger.debug("StampEditor class = " + ret.getClass().getName());
        
        return ret;
    }
}