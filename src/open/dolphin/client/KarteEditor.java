/*
 * KarteEditor2.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2003-2005 Digital Globe, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.*;
import java.awt.print.PageFormat;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.TooManyListenersException;
import java.util.prefs.Preferences;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.exception.DolphinException;
import open.dolphin.infomodel.AccessRightModel;
import open.dolphin.infomodel.ClaimBundle;
import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.infomodel.ExtRefModel;
import open.dolphin.infomodel.ID;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.KarteBean;
import open.dolphin.infomodel.ModelUtils;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.ProgressCourse;
import open.dolphin.infomodel.SchemaModel;
import open.dolphin.message.*;
import open.dolphin.project.Project;
import open.dolphin.util.BeanUtils;

import com.sun.image.codec.jpeg.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import open.dolphin.infomodel.PVTHealthInsuranceModel;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * 2���J���e�N���X�B
 *
 * @author Kazushi Minagawa
 */
public class KarteEditor extends DefaultChartDocument implements IInfoModel {
    
    private static final long serialVersionUID = 5805336364541168205L;
    
    // TimeStamp �̃J���[
    private static final Color TIMESTAMP_FORE = Color.BLUE;
    
    private static final int TIMESTAMP_FONT_SIZE = 14;
    
    private static final Font TIMESTAMP_FONT = new Font("Dialog", Font.PLAIN,
            TIMESTAMP_FONT_SIZE);
    
    private static final int TIMESTAMP_SPACING = 7;
    
    // ���x����
    private static final String DEFAULT_TITLE = "�o�ߋL�^";
    
    private static final String UPDATE_TAB_TITLE = "�X�V";
    
    private static final String[] TASK_MSG = { "�ۑ����Ă��܂�...", "�ۑ����܂���",
    "�ʐM�������̓A�v���P�[�V�����G���[���N���Ă��܂�", "������Ă��܂�..." };
    
    /** ���̃G�f�B�^�̃��f�� */
    private DocumentModel model;
    
    /** ���̃G�f�B�^���\������R���|�[�l���g */
    private JLabel timeStampLabel;
    
    private String timeStamp;
    
    //private JLabel sendClaimLabel;
    
    // ���N�ی�Box
    private boolean insuranceVisible;
    
    /** SOA Pane*/
    private KartePane soaPane;
    
    /** P Pane */
    private KartePane pPane;
    
    /** 2���J���e JPanel */
    private Panel2 panel2;
    
    /** �^�C���X�^���v�� foreground */
    private Color timeStampFore = TIMESTAMP_FORE;
    
    /** �^�C���X�^���v�t�H���g */
    private Font timeStampFont = TIMESTAMP_FONT;
    
    private int timeStampSpacing = TIMESTAMP_SPACING;
    
    /** �ҏW�\���ǂ����̃t���O */
    private boolean editable;
    
    /** �C������ true */
    private boolean modify;
    
    /** CLAIM ���M���X�i */
    private ClaimMessageListener claimListener;
    
    /** MML���M���X�i */
    private MmlMessageListener mmlListener;
    
    /** MML���M�t���O */
    private boolean sendMml;
    
    /** CLAIM ���M�t���O */
    private boolean sendClaim;
    
    /** State Manager */
    private StateMgr stateMgr;
    
    private javax.swing.Timer taskTimer;
    
    private Logger logger;
    
    /** 
     * Creates new KarteEditor2 
     */
    public KarteEditor() {
        logger = ClientContext.getLogger("boot");
        setTitle(DEFAULT_TITLE);
    }
    
    /**
     * DocumentModel��Ԃ��B
     * @return DocumentModel
     */
    public DocumentModel getModel() {
        return model;
    }
    
    /**
     * DocumentModel��ݒ肷��B
     * @param model DocumentModel
     */
    public void setModel(DocumentModel model) {
        this.model = model;
    }
    
    // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    // Junzo SATO
    public void printPanel2(final PageFormat format) {
        String name = getContext().getPatient().getFullName();
        panel2.printPanel(format, 1, false, name);
    }
    
    public void printPanel2(final PageFormat format, final int copies,
        final boolean useDialog) {
        String name = getContext().getPatient().getFullName();
        panel2.printPanel(format, copies, useDialog, name);
    }
    
    private int getActualHeight() {
        try {
            JTextPane pane = soaPane.getTextPane();
            int pos = pane.getDocument().getLength();
            Rectangle r = pane.modelToView(pos);
            int hsoa = r.y;
            
            pane = pPane.getTextPane();
            pos = pane.getDocument().getLength();
            r = pane.modelToView(pos);
            int hp = r.y;
            
            return Math.max(hsoa, hp);
            
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
        return 0;
    }
    
    public void print() {
        PageFormat pageFormat = getContext().getContext().getPageFormat();
        this.printPanel2(pageFormat);
    }
    
    public void insertImage() {
        JFileChooser chooser = new JFileChooser();
        int selected = chooser.showOpenDialog(getContext().getFrame());
        if (selected == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getPath();
            PlanarImage ri = JAI.create("fileload", path);
            if (ri == null) {
                return;
            }
            BufferedImage bf = ri.getAsBufferedImage();
            
            // insert image to the SOA Pane
            this.getSOAPane().myInsertImage(bf);
            
        } else if (selected == JFileChooser.CANCEL_OPTION) {
            return;
        }
    }
    
    // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    
    /**
     * SOAPane��Ԃ��B
     * @return SOAPane
     */
    protected KartePane getSOAPane() {
        return soaPane;
    }
    
    /**
     * PPane��Ԃ��B
     * @return PPane
     */
    protected KartePane getPPane() {
        return pPane;
    }
    
    /**
     * �ҏW�\������ݒ肷��B
     * @param b �ҏW�\�Ȏ�true
     */
    protected void setEditable(boolean b) {
        editable = b;
    }
    
    /**
     * MML���X�i��ǉ�����B
     * @param listener MML���X�i���X�i
     */
    public void addMMLListner(MmlMessageListener listener) throws TooManyListenersException {
        if (mmlListener != null) {
            throw new TooManyListenersException();
        }
        mmlListener = listener;
    }
    
    /**
     * MML���X�i���폜����B
     * @param listener MML���X�i���X�i
     */ 
    public void removeMMLListener(MmlMessageListener listener) {
        if (mmlListener != null && mmlListener == listener) {
            mmlListener = null;
        }
    }
    
    /**
     * CLAIM���X�i��ǉ�����B
     * @param listener CLAIM���X�i
     * @throws TooManyListenersException
     */
    public void addCLAIMListner(ClaimMessageListener listener)
    throws TooManyListenersException {
        if (claimListener != null) {
            throw new TooManyListenersException();
        }
        claimListener = listener;
    }
    
    /**
     * CLAIM���X�i���폜����B
     * @param listener �폜����CLAIM���X�i
     */
    public void removeCLAIMListener(ClaimMessageListener listener) {
        if (claimListener != null && claimListener == listener) {
            claimListener = null;
        }
    }
    
    /**
     * �C��������ݒ肷��B
     * @param b �C�����鎞true
     */
    protected void setModify(boolean b) {
        modify = b;
    }
    
    /**
     * ���̃G�f�B�^�ɐ؂�ւ���������j���[�𐧌䂷��B
     */
    public void enter() {
        super.enter();
        stateMgr.controlMenu();
    }
    
    /**
     * dirty������ݒ肷��B
     * @param dirty
     */
    public void setDirty(boolean dirty) {
        boolean bdirty = (soaPane.isDirty() || pPane.isDirty()) ? true : false;
        stateMgr.setDirty(bdirty);
    }
    
    /**
     * Dirty���ǂ�����Ԃ��B
     * @return dirty�̎�true
     */
    public boolean isDirty() {
        return stateMgr.isDirty();
    }
    
    /**
     * ����������B
     */
    public void initialize() {
        
        stateMgr = new StateMgr();
        
        // TimeStampLabel �𐶐�����
        timeStampLabel = new JLabel("TimeStamp");
        timeStampLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeStampLabel.setForeground(timeStampFore);
        timeStampLabel.setFont(timeStampFont);
        
        // SOA Pane �𐶐�����
        soaPane = new KartePane();
        soaPane.setParent(this);
        if (model != null) {
            // Schema �摜�Ƀt�@�C������t����̂��߂ɕK�v
            String docId = model.getDocInfo().getDocId();
            soaPane.setDocId(docId);
        }
        
        // P Pane �𐶐�����
        pPane = new KartePane();
        pPane.setParent(this);
        
        // �݂��Ɋ֘A������
        soaPane.setRole(ROLE_SOA, pPane);
        pPane.setRole(ROLE_P, soaPane);
        
        // TransferHandler��ݒ肷��
        soaPane.getTextPane().setTransferHandler(new SOATransferHandler(soaPane));
        pPane.getTextPane().setTransferHandler(new PTransferHandler(pPane));
        
        // 2���J���e�𐶐�����
        panel2 = new Panel2();
        panel2.setLayout(new BorderLayout());
        
        //JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
        JPanel flowPanel = new JPanel();
        flowPanel.setLayout(new BoxLayout(flowPanel, BoxLayout.X_AXIS));
        flowPanel.add(soaPane.getTextPane());
        flowPanel.add(javax.swing.Box.createHorizontalStrut(KartePane.PANE_DIVIDER_WIDTH));
        flowPanel.add(pPane.getTextPane());
                 
        //
        // TimeStamp + Health Insurances ��\������
        //       
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, timeStampSpacing));
        timePanel.add(timeStampLabel);
        //timePanel.add(Box.createHorizontalStrut(20));
        //timePanel.add(insBox);
        
        panel2.add(timePanel, BorderLayout.NORTH);
        panel2.add(flowPanel, BorderLayout.CENTER);
        
        JScrollPane scroller = new JScrollPane(panel2, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        getUI().setLayout(new BorderLayout());
        getUI().add(scroller, BorderLayout.CENTER);
        
//        JPanel sendInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 1));
//        sendClaimLabel = new JLabel(ClientContext.getImageIcon("calc_16.gif"));
//        sendClaimLabel.setToolTipText("�f�Ís�ׂ̑��M���s���ݒ�ɂȂ��Ă��܂��B");
//        sendInfoPanel.add(sendClaimLabel);
//        getUI().add(sendInfoPanel, BorderLayout.SOUTH);
        
        // �������̑O�Ƀ��f�����Z�b�g���Ă���B
        // Model ��\������
        displayModel();
    }
    
    /**
     * �v���O�������J�n����B�������̌�R�[�������B
     */
    public void start() {
        // ���f���\����Ƀ��X�i����ݒ肷��
        ChartMediator mediator = getContext().getChartMediator();
        soaPane.init(editable, mediator);
        pPane.init(editable, mediator);
        enter();
    }
    
    /**
     * DocumentModel��\������B
     */
    private void displayModel() {
        
        // Timestamp ��\������
        Date now = new Date();
        timeStamp = ModelUtils.getDateAsFormatString(now, IInfoModel.KARTE_DATE_FORMAT);
        
        // �C���̏ꍇ
        if (modify) {
            // �X�V: YYYY-MM-DDTHH:MM:SS (firstConfirmDate)
            StringBuilder buf = new StringBuilder();
            buf.append(UPDATE_TAB_TITLE);
            buf.append(": ");
            buf.append(timeStamp);
            buf.append(" [");
            buf.append(ModelUtils.getDateAsFormatString(model.getDocInfo().getFirstConfirmDate(), IInfoModel.KARTE_DATE_FORMAT));
            buf.append(" ]");
            timeStamp = buf.toString();
        }
        
        // ���e��\������
        if (model.getModules() != null) {
            KarteRenderer_2 renderer = new KarteRenderer_2(soaPane, pPane);
            renderer.render(model);
        }
        
        //
        // ���N�ی���\������
        //
        PVTHealthInsuranceModel[] ins = null;
        
        //
        // �R���e�L�X�g�� EditotFrame �̏ꍇ�� Chart �̏ꍇ������
        //
        if (getContext() instanceof ChartPlugin) {
            ins = ((ChartPlugin) getContext()).getHealthInsurances();
        } else if (getContext() instanceof EditorFrame) {
            EditorFrame ef = (EditorFrame) getContext();
            ChartPlugin chart = (ChartPlugin) ef.getChart();
            ins = chart.getHealthInsurances();
        }
        
        //
        // Model �ɐݒ肵�Ă��錒�N�ی���I������
        //
        String selecteIns = null;
        String insGUID = getModel().getDocInfo().getHealthInsuranceGUID();
        if (insGUID != null) {
            for (int i = 0; i < ins.length; i++) {
                String GUID = ins[i].getGUID();
                if (GUID != null && GUID.equals(insGUID)) {
                    selecteIns = ins[i].toString();
                    break;
                }
            }
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(timeStamp);
        if (selecteIns != null) {
            sb.append(" (");
            sb.append(selecteIns);
            sb.append(")");
        }
        
        timeStampLabel.setText(sb.toString());
        timeStampLabel.addMouseListener(new PopupListener());
        
        insuranceVisible = true;
    }
    
    public void applyInsurance(PVTHealthInsuranceModel hm) {
        
        getModel().getDocInfo().setHealthInsurance(hm.getInsuranceClassCode());
        //getModel().getDocInfo().setHealthInsuranceDesc(hm.getInsuranceClass());
        getModel().getDocInfo().setHealthInsuranceDesc(hm.toString());
        getModel().getDocInfo().setHealthInsuranceGUID(hm.getGUID());
        
        if (isInsuranceVisible()) {
            StringBuilder sb = new StringBuilder();
            sb.append(timeStamp);
            sb.append(" (");
            sb.append(hm.toString());
            sb.append(")");

            timeStampLabel.setText(sb.toString());
            timeStampLabel.revalidate();
        }
    }
    
    public void setInsuranceVisible(Boolean b) {
        
        boolean old = insuranceVisible;
        
        if (old != b) {
            
            insuranceVisible = b;
            
            StringBuilder sb = new StringBuilder();
            sb.append(timeStamp);
            
            if (b) {
                sb.append(" (");
                sb.append(getModel().getDocInfo().getHealthInsuranceDesc());
                sb.append(")");
            } 
            
            timeStampLabel.setText(sb.toString());
            timeStampLabel.revalidate();
        }
    }
    
    public boolean isInsuranceVisible() {
        return insuranceVisible;
    }
    
    class PopupListener extends MouseAdapter {

        public PopupListener() {
        }
        
        public void mouseClicked(MouseEvent e) {
            
            if (e.getClickCount() == 1) {
                boolean b = isInsuranceVisible();
                setInsuranceVisible(new Boolean(!b));
            }
            e.consume();
        }

//        public void mousePressed(MouseEvent e) {
//            maybeShowPopup(e);
//        }
//
//        public void mouseReleased(MouseEvent e) {
//            maybeShowPopup(e);
//        }
//
//        private void maybeShowPopup(MouseEvent e) {
//
//            if (e.isPopupTrigger()) {
//                
//                JPopupMenu popup = new JPopupMenu();
//                
//                ReflectActionListener ra =  null;
//                
//                if (isInsuranceVisible()) {
//                    ra = new ReflectActionListener(KarteEditor.this,
//                                                   "setInsuranceVisible", 
//                                                   new Class[]{Boolean.class}, 
//                                                   new Object[]{Boolean.FALSE});
//                } else {
//                    ra = new ReflectActionListener(KarteEditor.this,
//                                                   "setInsuranceVisible", 
//                                                   new Class[]{Boolean.class}, 
//                                                   new Object[]{Boolean.TRUE});
//                }
//                
//                JCheckBoxMenuItem mi = new JCheckBoxMenuItem("�ی�����\��");
//                mi.setSelected(!isInsuranceVisible());
//                mi.addActionListener(ra);
//                popup.add(mi);
//                
//                popup.show(e.getComponent(), e.getX(), e.getY());
//            }
//        }
    }
    
//    public boolean copyStamp() {
//            return pPane.copyStamp();
//    }
//
//    public void pasteStamp() {
//            pPane.pasteStamp();
//    }
    
    /**
     * �ۑ��_�C�A���O��\�����ۑ����̃p�����[�^���擾����B
     * @params sendMML MML���M�t���O ���M����Ƃ� true
     */
    private SaveParams getSaveParams(boolean joinAreaNetwork) {
        
        //
        // Title ���ݒ肳��Ă��邩
        //
        String text = model.getDocInfo().getTitle();
        if(text == null || text.equals("")) {

            // SOAPane ����ŏ��̂P�T�����𕶏��^�C�g���Ƃ��Ď擾����
            text = soaPane.getTitle();
            if ((text == null) || text.equals("")) {
                text = DEFAULT_TITLE;
            }
        }
        
        SaveParams params = null;
        
        //
        // �V�K�J���e�ŕۑ��̏ꍇ
        // ���ۑ�����C�����������Ă���ꍇ
        // �C���̏ꍇ
        //
        DocInfoModel docInfo = getModel().getDocInfo();
        
        if (!modify && docInfo.getStatus().equals(IInfoModel.STATUS_NONE)) {
            logger.debug("saveFromNew");
            if (sendClaim) {
                sendClaim = Project.getSendClaimSave();
            }
            
        } else if (modify && docInfo.getStatus().equals(IInfoModel.STATUS_TMP)) {
            logger.debug("saveFromTmp");
            if (sendClaim) {
                sendClaim = Project.getSendClaimSave();
            }
            
        } else if (modify) {
            logger.debug("saveFromModify");
            if (sendClaim) {
                sendClaim = Project.getSendClaimModify();
            }
        }
        
        //
        // �m�F�_�C�A���O��\�����邩�ǂ���
        //
        if (Project.getPreferences().getBoolean(Project.KARTE_SHOW_CONFIRM_AT_SAVE, true)) {
            //
            // �_�C�A���O��\�����A�A�N�Z�X�����̕ۑ����̃p�����[�^���擾����
            //
            params = new SaveParams(joinAreaNetwork);
            params.setTitle(text);
            params.setDepartment(model.getDocInfo().getDepartmentDesc());
            
            // ���������Preference����擾����
            Preferences prefs = Preferences.userNodeForPackage(this.getClass());
            int numPrint = prefs.getInt("karte.print.count", 0);
            params.setPrintCount(numPrint);
            
            //
            // CLAIM ���M
            //
            params.setSendClaim(sendClaim);

            SaveDialog sd = (SaveDialog) Project.createSaveDialog(getParentFrame(), params);
            sd.start();
            params = sd.getValue();

            // ���������ۑ�����
            if (params != null) {
                prefs.putInt("karte.print.count", params.getPrintCount());
            }
            
        } else {
            
            //
            // �m�F�_�C�A���O��\�����Ȃ�
            //
            params = new SaveParams(false);
            params.setTitle(text);
            params.setDepartment(model.getDocInfo().getDepartmentDesc());
            params.setPrintCount(Project.getPreferences().getInt(Project.KARTE_PRINT_COUNT, 0));
            
            //
            // ���ۑ����w�肳��Ă���[���̏ꍇ
            //
            int sMode = Project.getPreferences().getInt(Project.KARTE_SAVE_ACTION, 0);
            boolean tmpSave = sMode == 1 ? true : false;
            params.setTmpSave(tmpSave);
            if (tmpSave) {
                params.setSendClaim(false);
            } else {
                //
                // �ۑ������s�����[���̏ꍇ
                //
                params.setSendClaim(sendClaim);
            }
            
            //
            // ���ҎQ�ƁA�{�ݎQ�ƕs��
            //
            params.setAllowClinicRef(false);
            params.setAllowPatientRef(false);
        }
        
        return params;
    }
    
    /**
     * �ҏW����DocumentModel��ۑ�����B
     */
    public void save() {
        
        try {
            // ����������Ă��Ȃ����̓��^�[������
            if (!stateMgr.isDirty()) {
                return;
            }
            
            // MML���M�p�̃}�X�^ID���擾����
            // �P�[�X�P HANIWA ���� facilityID + patientID
            // �P�[�X�Q HIGO ���� �n��ID ���g�p
            ID masterID = Project.getMasterId(getContext().getPatient().getPatientId());
            if (masterID == null) {
            }
            
            sendMml = (Project.getSendMML() && masterID != null && mmlListener != null)
            ? true
            : false;
            
            //
            // ���̒i�K�ł� CLAIM ���M = �f�Ís�ב��M����claimListener!=null
            //
            sendClaim = (Project.getSendClaim() && claimListener != null) ? true : false;
            
            // �ۑ��_�C�A���O��\�����A�p�����[�^�𓾂�
            // �n��A�g�ɎQ����������MML���M���s���ꍇ�͊��ҋy�ѐf�×��̂���{�݂ւ̎Q�Ƌ���
            // �p�����[�^���ݒ�ł���悤�ɂ���
            // boolean karteKey = (Project.getJoinAreaNetwork() || sendMml) ? true : false;
            // �n��A�g�ɎQ������ꍇ�݂̂ɕύX����
            SaveParams params = getSaveParams(Project.getJoinAreaNetwork());
            
            //
            // �L�����Z���̏ꍇ�̓��^�[������
            //
            if (params != null) {
                //
                // ���̃X�e�[�W�����s����
                //
                save2(params);
            }
            
        } catch (DolphinException e) {
           e.printStackTrace();
       }
    }
    
    /**
     * �ۑ������̎�ȕ��������s����B
     **/
    private void save2(final SaveParams params) throws DolphinException {
        
        //
        // DocInfo�ɒl��ݒ肷��
        //
        final DocInfoModel docInfo = model.getDocInfo();
        
        // ���ݎ����� ConfirmDate �ɂ���
        Date confirmed = new Date();
        docInfo.setConfirmDate(confirmed);
        
        //
        // �C���łȂ��ꍇ�� FirstConfirmDate = ConfirmDate �ɂ���
        // �C���̏ꍇ�� FirstConfirmDate �͊��ɐݒ肳��Ă���
        // �C���łȂ��V�K�J���e�� parentId = null �ł���
        //
        if (docInfo.getParentId() == null) {
            docInfo.setFirstConfirmDate(confirmed);
        }
        
        //
        // Status ���ۑ����m��ۑ�����ݒ肷��
        // final �̎��� CLAIM ���M���邪�O�̏�ԂɈˑ�����
        //
        if (!params.isTmpSave()) {
            // 
            // �ҏW���J�n���ꂽ���� state ���擾����
            //
            String oldStatus = docInfo.getStatus();
            
            if (oldStatus.equals(STATUS_NONE)) {
                //
                // NONE����m��ւ̑J�� newSave
                //
                sendClaim = params.isSendClaim();
                logger.debug("NONE����m�� : " + sendClaim);
                
            } else if (oldStatus.equals(STATUS_TMP)) {
                //
                // ���ۑ�����m��֑J�ڂ���ꍇ   saveFromTmp
                // �����̏ꍇ���� CLIAM ���M����
                //
                //String first = ModelUtils.getDateAsString(docInfo.getFirstConfirmDate());
                //String cd = ModelUtils.getDateAsString(docInfo.getConfirmDate());
                //if (first.equals(cd)) {
                    //sendClaim = params.isSendClaim();
                //} else {
                    //sendClaim = false;
                //}
                sendClaim = params.isSendClaim();
                logger.debug("���ۑ�����m�� : " + sendClaim);
                
            } else {
                //
                // �m�肩��m��i�C���̏ꍇ�ɑ�������j�ȑO�� sendClaim = false;
                //
                sendClaim = params.isSendClaim();   //sendClaim && Project.getSendClaimModify();
                
                logger.debug("�C�� : " + sendClaim);
            }
            
            //
            // �ۑ����� state �� final �ɃZ�b�g����
            //
            docInfo.setStatus(STATUS_FINAL);
            
        } else {
            //
            // ���ۑ��̏ꍇ CLAIM ���M���Ȃ�
            //
            sendClaim = false;
            logger.debug("���ۑ� : " + sendClaim);
            
            sendMml = false;
            docInfo.setStatus(STATUS_TMP);
        }
        
        // title��ݒ肷��
        docInfo.setTitle(params.getTitle());
        
        // �f�t�H���g�̃A�N�Z�X����ݒ������ TODO
        AccessRightModel ar = new AccessRightModel();
        ar.setPermission(PERMISSION_ALL);
        ar.setLicenseeCode(ACCES_RIGHT_CREATOR);
        ar.setLicenseeName(ACCES_RIGHT_CREATOR_DISP);
        ar.setLicenseeCodeType(ACCES_RIGHT_FACILITY_CODE);
        docInfo.addAccessRight(ar);
        
        // ���҂̃A�N�Z�X����ݒ������
        if (params.isAllowPatientRef()) {
            ar = new AccessRightModel();
            ar.setPermission(PERMISSION_READ);
            ar.setLicenseeCode(ACCES_RIGHT_PATIENT);
            ar.setLicenseeName(ACCES_RIGHT_PATIENT_DISP);
            ar.setLicenseeCodeType(ACCES_RIGHT_PERSON_CODE);
            docInfo.addAccessRight(ar);
        }
        
        // �f�×����̂���{�݂̃A�N�Z�X����ݒ������
        if (params.isAllowClinicRef()) {
            ar = new AccessRightModel();
            ar.setPermission(PERMISSION_READ);
            ar.setLicenseeCode(ACCES_RIGHT_EXPERIENCE);
            ar.setLicenseeName(ACCES_RIGHT_EXPERIENCE_DISP);
            ar.setLicenseeCodeType(ACCES_RIGHT_EXPERIENCE_CODE);
            docInfo.addAccessRight(ar);
        }
        
        // ProgressCourseModule �� ModuleInfo ��ۑ����Ă���
        ModuleInfoBean[] progressInfo = model.getModuleInfo(MODULE_PROGRESS_COURSE);
        if (progressInfo == null) {
            // ���݂��Ȃ��ꍇ�͐V�K�ɍ쐬����
            progressInfo = new ModuleInfoBean[2];
            ModuleInfoBean mi = new ModuleInfoBean();
            mi.setStampName(MODULE_PROGRESS_COURSE);
            mi.setEntity(MODULE_PROGRESS_COURSE);
            mi.setStampRole(ROLE_SOA_SPEC);
            progressInfo[0] = mi;
            mi = new ModuleInfoBean();
            mi.setStampName(MODULE_PROGRESS_COURSE);
            mi.setEntity(MODULE_PROGRESS_COURSE);
            mi.setStampRole(ROLE_P_SPEC);
            progressInfo[1] = mi;
        }
        
        //
        // ���f���̃��W���[�����k���ɐݒ肷��
        // �G�f�B�^�̉�ʂ��_���v���Đ����������W���[����ݒ肷��
        //
        model.clearModules();
        model.clearSchema();
        
        //
        // SOAPane ���_���v�� model �ɒǉ�����
        // 
        KartePaneDumper_2 dumper = new KartePaneDumper_2();
        KarteStyledDocument doc = (KarteStyledDocument) soaPane.getTextPane().getDocument();
        dumper.dump(doc);
        ModuleModel[] soa = dumper.getModule();
        if (soa != null && soa.length > 0) {
            model.addModule(soa);
        }
        
        // ProgressCourse SOA �𐶐�����
        ProgressCourse pc = new ProgressCourse();
        pc.setFreeText(dumper.getSpec());
        ModuleModel progressSoa = new ModuleModel();
        progressSoa.setModuleInfo(progressInfo[0]);
        progressSoa.setModel(pc);
        model.addModule(progressSoa);
        
        // 
        // Schema ��ǉ�����
        //      
        int maxImageWidth = ClientContext.getInt("image.max.width");
        int maxImageHeight = ClientContext.getInt("image.max.height");
        Dimension maxSImageSize = new Dimension(maxImageWidth, maxImageHeight);
        SchemaModel[] schemas = dumper.getSchema();
        if (schemas != null && schemas.length > 0) {
            // �ۑ��̂��� Icon �� JPEG �ɕϊ�����
            for (SchemaModel schema : schemas) {
                ImageIcon icon = schema.getIcon();
                icon = adjustImageSize(icon, maxSImageSize);
                byte[] jpegByte = getJPEGByte(icon.getImage());
                schema.setJpegByte(jpegByte);
                schema.setIcon(null);
                model.addSchema(schema);
            }
        }
        
        //
        // PPane ���_���v�� model �ɒǉ�����
        // 
        dumper = new KartePaneDumper_2();
        doc = (KarteStyledDocument) pPane.getTextPane().getDocument();
        dumper.dump((DefaultStyledDocument) pPane.getTextPane().getDocument());
        ModuleModel[] plan = dumper.getModule();
        
        if (plan != null && plan.length > 0) {
            model.addModule(plan);
        } else {
            sendClaim = false;
        }
        
        // ProgressCourse P �𐶐�����
        pc = new ProgressCourse();
        pc.setFreeText(dumper.getSpec());
        ModuleModel progressP = new ModuleModel();
        progressP.setModuleInfo(progressInfo[1]);
        progressP.setModel(pc);
        model.addModule(progressP);
        
        // FLAG��ݒ肷��
        // image �����邩�ǂ���
        boolean flag = model.getSchema() != null ? true : false;
        docInfo.setHasImage(flag);
        
        // RP �����邩�ǂ���
        flag = model.getModule(ENTITY_MED_ORDER) != null ? true : false;
        docInfo.setHasRp(flag);
        
        // ���u�����邩�ǂ���
        flag = model.getModule(ENTITY_TREATMENT) != null ? true : false;
        docInfo.setHasTreatment(flag);
        
        // LaboTest �����邩�ǂ���
        flag = model.getModule(ENTITY_LABO_TEST) != null ? true : false;
        docInfo.setHasLaboTest(flag);
        
        //
        // EJB3.0 Model �̊֌W���\�z����
        //
        // confirmed, firstConfirmed �͐ݒ�ς�
        KarteBean karte = getContext().getKarte();
        model.setKarte(karte);                          // karte
        model.setCreator(Project.getUserModel());       // �L�^��
        model.setRecorded(docInfo.getConfirmDate());    // �L�^��
        
        // Module�Ƃ̊֌W��ݒ肷��
        Collection<ModuleModel> moduleBeans = model.getModules();
        int number = 0;
        int totalSize = 0;
        for (ModuleModel bean : moduleBeans) {
            bean.setId(0L);                             // unsaved-value
            bean.setKarte(karte);                       // Karte
            bean.setCreator(Project.getUserModel());    // �L�^��
            bean.setDocument(model);                    // Document
            bean.setConfirmed(docInfo.getConfirmDate());            // �m���
            bean.setFirstConfirmed(docInfo.getFirstConfirmDate());  // �K���J�n��
            bean.setRecorded(docInfo.getConfirmDate());             // �L�^��
            bean.setStatus(STATUS_FINAL);                           // status
            bean.setBeanBytes(BeanUtils.getXMLBytes(bean.getModel()));
             
            // ModuleInfo ��ݒ肷��
            // Name, Role, Entity �͐ݒ肳��Ă���
            ModuleInfoBean mInfo = bean.getModuleInfo();
            mInfo.setStampNumber(number++);
            
            int size = bean.getBeanBytes().length / 1024;
            logger.debug("stamp size(KB) = " + size);
            totalSize += size;
        }
        logger.debug("stamp total size(KB) = " + totalSize);
        totalSize = 0;
        
        // �摜�Ƃ̊֌W��ݒ肷��
        number = 0;
        Collection<SchemaModel> imagesimages = model.getSchema();
        if (imagesimages != null && imagesimages.size() > 0) {
            for (SchemaModel bean : imagesimages) {
                bean.setId(0L);                                         // unsaved
                bean.setKarte(karte);                                   // Karte
                bean.setCreator(Project.getUserModel());                // Creator
                bean.setDocument(model);                                // Document
                bean.setConfirmed(docInfo.getConfirmDate());            // �m���
                bean.setFirstConfirmed(docInfo.getFirstConfirmDate());  // �K���J�n��
                bean.setRecorded(docInfo.getConfirmDate());             // �L�^��
                bean.setStatus(STATUS_FINAL);                           // Status
                bean.setImageNumber(number++);
                
                ExtRefModel ref = bean.getExtRef();
                StringBuilder sb = new StringBuilder();
                sb.append(model.getDocInfo().getDocId());
                sb.append("-");
                sb.append(number++);
                sb.append(".jpg");
                ref.setHref(sb.toString());
                
                int size = bean.getJpegByte().length / 1024;
                logger.debug("schema size(KB) = " + size);
                totalSize += size;
            }
            logger.debug("total schema size(KB) = " + totalSize);
        }
        
        // �ۑ��^�X�N���J�n����
        final IStatusPanel statusPanel = getContext().getStatusPanel();
        
        final DocumentDelegater ddl = new DocumentDelegater();
        
        // Worker �𐶐�����
        int maxEstimation = ClientContext.getInt("task.default.maxEstimation");
        int delay = ClientContext.getInt("task.default.delay");
        final SaveTask worker = new SaveTask(model, ddl, maxEstimation/delay);
        
        // �^�C�}�[���N������
        taskTimer = new javax.swing.Timer(delay, new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                
                worker.getCurrent();
                statusPanel.setMessage(worker.getMessage());
                
                if (worker.isDone()) {
                    // �ۑ���̏������s��
                    statusPanel.stop();
                    taskTimer.stop();
                    
                    //long putCode = worker.getResult();
                    
                    if (ddl.isNoError()) {
                        // ���
                        int copies = params.getPrintCount();
                        if (copies > 0) {
                            statusPanel.setMessage(TASK_MSG[3]);
                            printPanel2(getContext().getContext().getPageFormat(), copies, false);
                        }
                        
                        statusPanel.stop(TASK_MSG[1]);
                        
                        // �ҏW�s�ɐݒ肷��
                        soaPane.setEditableProp(false);
                        pPane.setEditableProp(false);
                        
                        // ��ԑJ�ڂ���
                        stateMgr.setSaved(true);

                        //
                        // Chart �̏�Ԃ�ݒ肷��
                        //
                        if (docInfo.getStatus().equals(STATUS_TMP)) {
                            getContext().setChartState(ChartPlugin.OPEN_NONE);
                            
                        } else if (docInfo.getStatus().equals(STATUS_FINAL)) {
                            getContext().setChartState(ChartPlugin.OPEN_SAVE);
                        }
                        
                        //
                        // ���������̍X�V��ʒm����
                        //
                        getContext().getDocumentHistory().getDocumentHistory();
                        
                        
                        
                    } else {
                        // �G���[��\������
                        JFrame parent = getContext().getFrame();
                        String title = ClientContext.getString("karte.task.saveTitle");
                        JOptionPane.showMessageDialog(
                                parent,
                                ddl.getErrorMessage(),
                                ClientContext.getFrameTitle(title),
                                JOptionPane.WARNING_MESSAGE);
                    }
                    
                } else if (worker.isTimeOver()) {
                    statusPanel.stop();
                    taskTimer.stop();
                    JFrame parent = getContext().getFrame();
                    String title = ClientContext.getString("karte.task.saveTitle");
                    new TimeoutWarning(parent, title, null).start();
                }
            }
        });
        statusPanel.start("");
        worker.start();
        taskTimer.start();
    }
    
    /**
     * Courtesy of Junzo SATO
     */
    private byte[] getJPEGByte(Image image) {
        
        byte[] ret = null;
        BufferedOutputStream writer = null;
        
        try {

            JPanel myPanel = getUI();
            Dimension d = new Dimension(image.getWidth(myPanel), image.getHeight(myPanel));
            BufferedImage bf = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
            Graphics g = bf.getGraphics();
            g.setColor(Color.white);
            g.drawImage(image, 0, 0, d.width, d.height, myPanel);
            
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            writer = new BufferedOutputStream(bo);
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(writer);
            encoder.encode(bf);
            writer.flush();
            writer.close();
            ret = bo.toByteArray();
            
        } catch (IOException e) {
            e.printStackTrace();
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e2) {
                }
            }
        }
        return ret;
    }
    
        
    private ImageIcon adjustImageSize(ImageIcon icon, Dimension dim) {
        
        if ( (icon.getIconHeight() > dim.height) ||
                (icon.getIconWidth() > dim.width) ) {
            Image img = icon.getImage();
            float hRatio = (float)icon.getIconHeight() / dim.height;
            float wRatio = (float)icon.getIconWidth() / dim.width;
            int h, w;
            if (hRatio > wRatio) {
                h = dim.height;
                w = (int)(icon.getIconWidth() / hRatio);
            } else {
                w = dim.width;
                h = (int)(icon.getIconHeight() / wRatio);
            }
            img = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } else {
            return icon;
        }
    }
    
    ////////////////////////////////////////////////////////////////////
    
    /**
     * �J���e�̕ۑ������s����^�X�N�N���X�B
     */
    protected class SaveTask extends AbstractInfiniteTask {
        
        private DocumentModel model;
        private DocumentDelegater ddl;
        private long putCode;
        
        public SaveTask(DocumentModel model, DocumentDelegater ddl, int taskLength) {
            this.model = model;
            this.ddl = ddl;
            setTaskLength(taskLength);
        }
        
        protected long getResult() {
            return putCode;
        }
        
        protected void doTask() {
            
            message = TASK_MSG[0];
            
            // �ŏ��Ƀf�[�^�x�[�X�֕ۑ�����
            putCode = ddl.putKarte(model);
            
            if (ddl.isNoError()) {
                // ���������ꍇ
                // CLAIM���M���s��
                if (sendClaim && claimListener != null) {
                    sendClaim();
                }
                
                // MML���M���s��
                if (Project.getJoinAreaNetwork() || sendMml) {
                    sendMml();
                }
                
                message = TASK_MSG[1];
                
            } else {
                message = TASK_MSG[2];
            }
            
            setDone(true);
        }
        
        /**
         * CLAIM ���M���s���B
         *
         */
        private void sendClaim() {
            
            // �w���p�[�N���X�𐶐���Velocity���g�p���邽�߂̃p�����[�^��ݒ肷��
            ClaimHelper helper = new ClaimHelper();
            DocInfoModel docInfo = model.getDocInfo();
            Collection<ModuleModel> modules = model.getModules();
            
            String confirmedStr = ModelUtils.getDateTimeAsString(docInfo.getConfirmDate());
            helper.setConfirmDate(confirmedStr);
            helper.setCreatorId(model.getCreator().getUserId());
            helper.setCreatorName(model.getCreator().getCommonName());
            helper.setCreatorDept(docInfo.getDepartment());
            helper.setCreatorDeptDesc(docInfo.getDepartmentDesc());
            helper.setCreatorLicense(model.getCreator().getLicenseModel().getLicense());
            helper.setPatientId(model.getKarte().getPatient().getPatientId());
            helper.setGenerationPurpose(docInfo.getPurpose());
            helper.setDocId(docInfo.getDocId());
            helper.setHealthInsuranceGUID(docInfo.getHealthInsuranceGUID());
            helper.setHealthInsuranceClassCode(docInfo.getHealthInsurance());
            helper.setHealthInsuranceDesc(docInfo.getHealthInsuranceDesc());
            
            // �ۑ����� KarteModel �̑S���W���[�����`�F�b�N��
            // ���ꂪ ClaimBundle �Ȃ�w���p�[�֒ǉ�����
            for (ModuleModel module : modules) {
                IInfoModel model = module.getModel();
                if (model instanceof ClaimBundle) {
                    helper.addClaimBundle((ClaimBundle) model);
                }
            }
            
            MessageBuilder mb = new MessageBuilder();
            String claimMessage = mb.build(helper);
            ClaimMessageEvent cvt = new ClaimMessageEvent(this);
            cvt.setClaimInstance(claimMessage);
            
            cvt.setPatientId(model.getKarte().getPatient().getPatientId());
            cvt.setPatientName(model.getKarte().getPatient().getFullName());
            cvt.setPatientSex(model.getKarte().getPatient().getGender());
            
            cvt.setTitle(model.getDocInfo().getTitle());
            cvt.setConfirmDate(confirmedStr);
            
            // debug �o�͂��s��
            if (ClientContext.getLogger("claim") != null) {
                ClientContext.getLogger("claim").debug(claimMessage);
            }
            
            if (claimListener != null) {
                claimListener.claimMessageEvent(cvt);
            }
        }
        
        /**
         * MML���M���s��
         */
        private void sendMml() {
            
            // MML Message �𐶐�����
            MMLHelper mb = new MMLHelper();
            mb.setDocument(model);
            mb.setUser(Project.getUserModel());
            mb.setPatientId(getContext().getPatient().getPatientId());
            mb.buildText();
            
            try {
                VelocityContext context = ClientContext.getVelocityContext();
                context.put("mmlHelper", mb);

                // ���̃X�^���v�̃e���v���[�g�t�@�C���𓾂�
                String templateFile = "mml2.3Helper.vm";

                // Merge ����
                StringWriter sw = new StringWriter();
                BufferedWriter bw = new BufferedWriter(sw);
                InputStream instream = ClientContext.getTemplateAsStream(templateFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(instream, "SHIFT_JIS"));
                Velocity.evaluate(context, bw, "mml", reader);
                bw.flush();
                bw.close();
                reader.close();
                String mml = sw.toString();
                //System.out.println(mml);
                
                // debug�o�͂��s��
                if (ClientContext.getLogger("mml") != null) {
                    ClientContext.getLogger("mml").debug(mml);
                }
                
                if (sendMml && mmlListener != null) {
                    MmlMessageEvent mevt = new MmlMessageEvent(this);
                    mevt.setGroupId(mb.getDocId());
                    mevt.setMmlInstance(mml);
                    if (mb.getSchema() != null) {
                        mevt.setSchema(mb.getSchema());
                    }
                    mmlListener.mmlMessageEvent(mevt);
                }
                
                if (Project.getJoinAreaNetwork()) {
                    // TODO
                }
                

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    // //////////////////////////////////////////////////////////////////
    
    /**
     * ���̃G�f�B�^�̒��ۏ�ԃN���X
     */
    protected abstract class EditorState {
        
        public EditorState() {
        }
        
        public abstract boolean isDirty();
        
        public abstract void controlMenu();
    }
    
    /**
     * No dirty ��ԃN���X
     */
    protected final class NoDirtyState extends EditorState {
        
        public NoDirtyState() {
        }
        
        public void controlMenu() {
            ChartMediator mediator = getContext().getChartMediator();
            mediator.getAction(GUIConst.ACTION_SAVE).setEnabled(false); // �ۑ�
            mediator.getAction(GUIConst.ACTION_PRINT).setEnabled(false); // ���
            mediator.getAction(GUIConst.ACTION_CUT).setEnabled(false);
            mediator.getAction(GUIConst.ACTION_COPY).setEnabled(false);
            mediator.getAction(GUIConst.ACTION_PASTE).setEnabled(false);
            mediator.getAction(GUIConst.ACTION_UNDO).setEnabled(false);
            mediator.getAction(GUIConst.ACTION_REDO).setEnabled(false);
            mediator.getAction(GUIConst.ACTION_INSERT_TEXT).setEnabled(false);
            mediator.getAction(GUIConst.ACTION_INSERT_SCHEMA).setEnabled(false);
            mediator.getAction(GUIConst.ACTION_INSERT_STAMP).setEnabled(false);
            mediator.getAction(GUIConst.ACTION_SELECT_INSURANCE).setEnabled(!modify);
        }
        
        public boolean isDirty() {
            return false;
        }
    }
    
    /**
     * Dirty ��ԃN���X
     */
    protected final class DirtyState extends EditorState {
        
        public DirtyState() {
        }
        
        public void controlMenu() {
            ChartMediator mediator = getContext().getChartMediator();
            mediator.getAction(GUIConst.ACTION_SAVE).setEnabled(true);
            mediator.getAction(GUIConst.ACTION_PRINT).setEnabled(true);
            mediator.getAction(GUIConst.ACTION_SELECT_INSURANCE).setEnabled(!modify);
        }
        
        public boolean isDirty() {
            return true;
        }
    }
    
    /**
     * EmptyNew ��ԃN���X
     */
    protected final class SavedState extends EditorState {
        
        public SavedState() {
        }
        
        public void controlMenu() {
            ChartMediator mediator = getContext().getChartMediator();
            mediator.getAction(GUIConst.ACTION_SAVE).setEnabled(false);
            mediator.getAction(GUIConst.ACTION_PRINT).setEnabled(true);
            mediator.getAction(GUIConst.ACTION_CUT).setEnabled(false);
            mediator.getAction(GUIConst.ACTION_COPY).setEnabled(false);
            mediator.getAction(GUIConst.ACTION_PASTE).setEnabled(false);
            mediator.getAction(GUIConst.ACTION_UNDO).setEnabled(false);
            mediator.getAction(GUIConst.ACTION_REDO).setEnabled(false);
            mediator.getAction(GUIConst.ACTION_INSERT_TEXT).setEnabled(false);
            mediator.getAction(GUIConst.ACTION_INSERT_SCHEMA).setEnabled(false);
            mediator.getAction(GUIConst.ACTION_INSERT_STAMP).setEnabled(false);
            mediator.getAction(GUIConst.ACTION_SELECT_INSURANCE).setEnabled(false);
        }
        
        public boolean isDirty() {
            return false;
        }
    }
    
    /**
     * ��ԃ}�l�[�W��
     */
    protected final class StateMgr {
        
        private EditorState noDirtyState = new NoDirtyState();
        
        private EditorState dirtyState = new DirtyState();
        
        private EditorState savedState = new SavedState();
        
        private EditorState currentState;
        
        public StateMgr() {
            currentState = noDirtyState;
        }
        
        public boolean isDirty() {
            return currentState.isDirty();
        }
        
        public void setDirty(boolean dirty) {
            currentState = dirty ? dirtyState : noDirtyState;
            currentState.controlMenu();
        }
        
        public void setSaved(boolean saved) {
            if (saved) {
                currentState = savedState;
                currentState.controlMenu();
            }
        }
        
        public void controlMenu() {
            currentState.controlMenu();
        }
    }
}