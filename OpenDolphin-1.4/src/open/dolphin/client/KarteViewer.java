package open.dolphin.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.awt.print.PageFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import javax.swing.text.BadLocationException;
import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModelUtils;

/**
 * �V���O���h�L�������g�̃r�����[�N���X�B
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class KarteViewer extends AbstractChartDocument implements Comparable {
    
    // �I������Ă��鎞�̃{�[�_�F
    //private static final Color SELECTED_COLOR = new Color(255, 0, 153);
    private static final Color SELECTED_COLOR = new Color(60, 115, 216);
    
    // �I�����ꂽ��Ԃ̃{�[�_ 
    private static final Border SELECTED_BORDER = BorderFactory.createLineBorder(SELECTED_COLOR);
    
    // �I������Ă��Ȃ����̃{�[�_�F 
    private static final Color NOT_SELECTED_COLOR = new Color(227, 250, 207);
    
    // �I������Ă��Ȃ���Ԃ̃{�[�_ 
    private static final Border NOT_SELECTED_BORDER = BorderFactory.createLineBorder(NOT_SELECTED_COLOR);
    
    // �^�C���X�^���v�� foreground �J���[ 
    private static final Color TIMESTAMP_FORE = Color.BLUE;
    
    // �^�C���X�^���v�̃t�H���g�T�C�Y 
    private static final int TIMESTAMP_FONT_SIZE = 14;
    
    // �^�C���X�^���v�t�H���g
    private static final Font TIMESTAMP_FONT = new Font("Dialog", Font.PLAIN, TIMESTAMP_FONT_SIZE);
    
    // �^�C���X�^���v�p�l�� FlowLayout �̃}�[�W�� 
    private static final int TIMESTAMP_SPACING = 7;
    
    // ���ۑ����̃h�L�������g��\������ 
    protected static final String UNDER_TMP_SAVE = " - ���ۑ���";
    
    //
    // �C���X�^���X�ϐ�
    //
    // ���� view �̃��f�� 
    protected DocumentModel model;
    
    // �^�C���X�^���v���x��
    protected JLabel timeStampLabel;
    
    // SOA Pane 
    protected KartePane soaPane;
    
    // 2���J���e�p�l��
    protected Panel2 panel2;
    
    // �^�C���X�^���v�� foreground �J���[
    protected Color timeStampFore = TIMESTAMP_FORE;
    
    // �^�C���X�^���v�̃t�H���g 
    protected Font timeStampFont = TIMESTAMP_FONT;
    
    protected int timeStampSpacing = TIMESTAMP_SPACING;
    
    protected boolean avoidEnter;
    
    // �I������Ă��邩�ǂ����̃t���O
    protected boolean selected;
    
    private int mode;
    
    /**
     * Creates new KarteViewer
     */
    public KarteViewer() {
    }
    
    public int getActualHeight() {
        try {
            JTextPane pane = soaPane.getTextPane();
            int pos = pane.getDocument().getLength();
            Rectangle r = pane.modelToView(pos);
            int hsoa = r.y;
            return hsoa;
            
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
        return 0;
    }
    
    public void adjustSize() {
        int h = getActualHeight();
        int soaWidth = soaPane.getTextPane().getPreferredSize().width; 
        soaPane.getTextPane().setPreferredSize(new Dimension(soaWidth, h));
    }
    
    public String getDocType() {
        if (model != null) {
            String docType = model.getDocInfo().getDocType();
            return docType;
        }
        return null;
    }
    
    public void setAvoidEnter(boolean b) {
        avoidEnter = b;
    }
    
    // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    // Junzo SATO
    public void printPanel2(final PageFormat format) {
        String name = getContext().getPatient().getFullName();
        panel2.printPanel(format, 1, false, name, getActualHeight()+60);
    }
    
    public void printPanel2(final PageFormat format, final int copies,
            final boolean useDialog) {
        String name = getContext().getPatient().getFullName();
        panel2.printPanel(format, copies, useDialog, name, getActualHeight()+60);
    }
    
    @Override
    public void print() {
        PageFormat pageFormat = getContext().getContext().getPageFormat();
        this.printPanel2(pageFormat);
    }
    
    /**
     * SOA Pane ��Ԃ��B
     * @return soaPane
     */
    public KartePane getSOAPane() {
        return soaPane;
    }
    
    /**
     * �R���e�i����R�[������� enter() ���\�b�h��
     * ���j���[�𐧌䂷��B
     */
    @Override
    public void enter() {
        
        if (avoidEnter) {
            return;
        }
        super.enter();
        
        // ReadOnly ����
        boolean canEdit = getContext().isReadOnly() ? false : true;
        
        // ���ۑ����ǂ���
        boolean tmp = model.getDocInfo().getStatus().equals(IInfoModel.STATUS_TMP) ? true : false;
        
        // �V�K�J���e�쐬���\�ȏ���
        boolean newOk = canEdit && (!tmp) ? true : false;
        
        ChartMediator mediator = getContext().getChartMediator();
        mediator.getAction(GUIConst.ACTION_NEW_KARTE).setEnabled(newOk);        // �V�K�J���e
        mediator.getAction(GUIConst.ACTION_PRINT).setEnabled(true);             // ���
        mediator.getAction(GUIConst.ACTION_MODIFY_KARTE).setEnabled(canEdit);   // �C��
    }
        
    /**
     * �V���O���J���e�ŏ���������B
     */
    private void initialize() {
        
        KartePanel1 kp1 = new KartePanel1();
        panel2 = kp1;
        
        // TimeStampLabel �𐶐�����
        timeStampLabel = kp1.getTimeStampLabel();
        timeStampLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeStampLabel.setForeground(timeStampFore);
        timeStampLabel.setFont(timeStampFont);
        
        // SOA Pane �𐶐�����
        soaPane = new KartePane();
        soaPane.setTextPane(kp1.getSoaTextPane());
        soaPane.setRole(IInfoModel.ROLE_SOA);
        if (model != null) {
            // Schema �摜�Ƀt�@�C������t����̂��߂ɕK�v
            String docId = model.getDocInfo().getDocId();
            soaPane.setDocId(docId);
        }
        
        setUI(kp1);
    }
    
    /**
     * �v���O�������J�n����B
     */
    @Override
    public void start() {
        //
        // Creates GUI
        //
        this.initialize();
        
        //
        // Model ��\������
        //
        if (this.getModel() != null) {
            //
            // �m����𕪂���₷���\���ɕς���
            //
            String timeStamp = ModelUtils.getDateAsFormatString(
                    model.getDocInfo().getFirstConfirmDate(), 
                    IInfoModel.KARTE_DATE_FORMAT);
            
            if (model.getDocInfo().getStatus().equals(IInfoModel.STATUS_TMP)) {
                StringBuilder sb = new StringBuilder();
                sb.append(timeStamp);
                sb.append(UNDER_TMP_SAVE);
                timeStamp = sb.toString();
            }
            timeStampLabel.setText(timeStamp);
            KarteRenderer_2 renderer = new KarteRenderer_2(soaPane, null);
            renderer.render(model);
        }
        
        //
        // ���f���\����Ƀ��X�i����ݒ肷��
        //
        ChartMediator mediator = getContext().getChartMediator();
        soaPane.init(false, mediator);
        enter();
    }
    
    @Override
    public void stop() {
        soaPane.clear();
    }
    
    /**
     * �\�����郂�f����ݒ肷��B
     * @param model �\������DocumentModel
     */
    public void setModel(DocumentModel model) {
        this.model = model;
    }
    
    /**
     * �\�����郂�f����Ԃ��B
     * @return �\������DocumentModel
     */
    public DocumentModel getModel() {
        return model;
    }
    
    /**
     * �I����Ԃ�ݒ肷��B
     * �I����Ԃɂ��View�̃{�[�_�̐F��ς���B
     * @param selected �I�����ꂽ�� true
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
//        if (selected) {
//            getUI().setBorder(SELECTED_BORDER);
//        } else {
//            getUI().setBorder(NOT_SELECTED_BORDER);
//        }
    }
    
    /**
     * �I������Ă��邩�ǂ�����Ԃ��B
     * @return �I������Ă��鎞 true
     */
    public boolean isSelected() {
        return selected;
    }
    
    public void addMouseListener(MouseListener ml) {
        soaPane.getTextPane().addMouseListener(ml);
    }
    
    @Override
    public int hashCode() {
        return getModel().getDocInfo().getDocId().hashCode() + 72;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other != null && other.getClass() == this.getClass()) {
            DocInfoModel otheInfo = ((KarteViewer) other).getModel()
            .getDocInfo();
            return getModel().getDocInfo().equals(otheInfo);
        }
        return false;
    }
    
    @Override
    public int compareTo(Object other) {
        if (other != null && other.getClass() == this.getClass()) {
            DocInfoModel otheInfo = ((KarteViewer) other).getModel()
            .getDocInfo();
            return getModel().getDocInfo().compareTo(otheInfo);
        }
        return -1;
    }
}