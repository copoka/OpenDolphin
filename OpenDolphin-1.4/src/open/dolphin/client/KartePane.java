package open.dolphin.client;

import open.dolphin.order.StampEditor;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyledEditorKit;

import open.dolphin.dao.SqlOrcaSetDao;
import open.dolphin.delegater.StampDelegater;
import open.dolphin.infomodel.ExtRefModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.SchemaModel;
import open.dolphin.infomodel.StampModel;
import open.dolphin.util.BeanUtils;

import open.dolphin.client.ChartMediator.CompState;
import open.dolphin.helper.DBTask;
import open.dolphin.helper.ImageHelper;
import open.dolphin.order.AbstractStampEditor;
import open.dolphin.plugin.PluginLoader;
import org.apache.log4j.Logger;

/**
 * Karte Pane
 *
 * @author Kazushi Minagawa, Digital Globe, inc.
 */
public class KartePane implements DocumentListener, MouseListener,
        CaretListener, PropertyChangeListener, KarteComposite {

    // �����ɕt����^�C�g���������Ŏ擾���鎞�̒���

    private static final int TITLE_LENGTH = 15;
    // �ҏW�s���̔w�i�F

    private static final Color UNEDITABLE_COLOR = new Color(227, 250, 207);
    // JTextPane

    private JTextPane textPane;
    // SOA �܂��� P �̃��[��

    private String myRole;
    // ���� KartePane�̃I�[�i

    private ChartDocument parent;
    // StampHolder��TransferHandler

    private StampHolderTransferHandler stampHolderTransferHandler;
    // SchemaHolder��TransferHandler

    private SchemaHolderTransferHandler schemaHolderTransferHandler;
    private int stampId;
    // Dirty Flag

    private boolean dirty;
    // Selection Flag

    private boolean hasSelection;
    private CompState curState;
    // ���������ꂽ����Document�̒���

    private int initialLength;
    // ChartMediator(MenuSupport)

    private ChartMediator mediator;
    // ���̃I�u�W�F�N�g�Ő������镶��DocumentModel�̕���ID

    private String docId;
    // �ۑ���y�уu���E�Y���̕ҏW�s��\���J���[

    private Color uneditableColor = UNEDITABLE_COLOR;
    // ���̃y�C������Dragg�y��Dropp���ꂽ�X�^���v�̏��

    private ComponentHolder[] drragedStamp;
    private int draggedCount;
    private int droppedCount;
    
    Logger logger;

    /** 
     * Creates new KartePane2 
     */
    public KartePane() {
        logger = ClientContext.getBootLogger();
    }

    public void setMargin(Insets margin) {
        textPane.setMargin(margin);
    }

    public void setPreferredSize(Dimension size) {
        textPane.setPreferredSize(size);
    }

    public void setSize(Dimension size) {
        textPane.setMinimumSize(size);
        textPane.setMaximumSize(size);
    }

    /**
     * ����Pane�̃I�[�i��ݒ肷��B
     * @param parent KarteEditor�I�[�i
     */
    public void setParent(ChartDocument parent) {
        this.parent = parent;
    }

    /**
     * ����Pane�̃I�[�i��Ԃ��B
     * @return KarteEditor�I�[�i
     */
    public ChartDocument getParent() {
        return parent;
    }

    /**
     * �ҏW�s��\���J���[��ݒ肷��B
     * @param uneditableColor �ҏW�s��\���J���[
     */
    public void setUneditableColor(Color uneditableColor) {
        this.uneditableColor = uneditableColor;
    }

    /**
     * �ҏW�s��\���J���[��Ԃ��B
     * @return �ҏW�s��\���J���[
     */
    public Color getUneditableColor() {
        return uneditableColor;
    }

    /**
     * ����Pane�Ő�������DocumentModel�̕���ID��ݒ肷��B
     * @param docId ����ID
     */
    protected void setDocId(String docId) {
        this.docId = docId;
    }

    /**
     * ����Pane�Ő�������DocumentModel�̕���ID��Ԃ��B
     * @return ����ID
     */
    protected String getDocId() {
        return docId;
    }

    /**
     * ChartMediator��ݒ肷��B
     * @param mediator ChartMediator
     */
    protected void setMediator(ChartMediator mediator) {
        this.mediator = mediator;
    }

    /**
     * ChartMediator��Ԃ��B
     * @return ChartMediator
     */
    protected ChartMediator getMediator() {
        return mediator;
    }

    /**
     * ����Pane�̃��[����ݒ肷��B
     * @param myRole SOA�܂���P�̃��[��
     */
    public void setMyRole(String myRole) {
        this.myRole = myRole;
    }

    /**
     *  ����Pane�̃��[����Ԃ��B
     * @return SOA�܂���P�̃��[��
     */
    public String getMyRole() {
        return myRole;
    }

    /**
     * JTextPane��ݒ肷��B
     * @param textPane JTextPane
     */
    public void setTextPane(JTextPane textPane) {
        this.textPane = textPane;
        if (this.textPane != null) {
            KarteStyledDocument doc = new KarteStyledDocument();
            this.textPane.setDocument(doc);
            this.textPane.putClientProperty("kartePane", this);

            doc.setParent(this);
            stampHolderTransferHandler = new StampHolderTransferHandler();
            schemaHolderTransferHandler = new SchemaHolderTransferHandler();
        }
    }

    /**
     * JTextPane��Ԃ��B
     * @return JTextPane
     */
    public JTextPane getTextPane() {
        return textPane;
    }

    /**
     * JTextPane��StyledDocument��Ԃ��B
     * @return JTextPane��StyledDocument
     */
    protected KarteStyledDocument getDocument() {
        return (KarteStyledDocument) getTextPane().getDocument();
    }

    /**
     * ��������ݒ肷��B
     * @param Document�̏�����
     */
    public void setInitialLength(int initialLength) {
        this.initialLength = initialLength;
    }

    /**
     * ��������Ԃ��B
     * @return Document�̏�����
     */
    public int getInitialLength() {
        return initialLength;
    }

    /**
     * ����Pane����Drag���ꂽ�X�^���v����Ԃ��B
     * @return ����Pane����Drag���ꂽ�X�^���v��
     */
    protected int getDraggedCount() {
        return draggedCount;
    }

    /**
     * ����Pane����Drag���ꂽ�X�^���v����ݒ肷��B
     * @param draggedCount ����Pane����Drag���ꂽ�X�^���v��
     */
    protected void setDraggedCount(int draggedCount) {
        this.draggedCount = draggedCount;
    }

    /**
     * ����Pane��Drop���ꂽ�X�^���v����Ԃ��B
     * @return ����Pane��Drop���ꂽ�X�^���v��
     */
    protected int getDroppedCount() {
        return droppedCount;
    }

    /**
     * ����Pane��Drop���ꂽ�X�^���v����ݒ肷��B
     * @param droppedCount ����Pane��Drop���ꂽ�X�^���v��
     */
    protected void setDroppedCount(int droppedCount) {
        this.droppedCount = droppedCount;
    }

    /**
     * ����Pane����Drag���ꂽ�X�^���v��Ԃ��B
     * @return ����Pane����Drag���ꂽ�X�^���v�z��
     */
    protected ComponentHolder[] getDrragedStamp() {
        return drragedStamp;
    }

    /**
     * ����Pane����Drag���ꂽ�X�^���v��ݒ�i�L�^�j����B
     * @param drragedStamp ����Pane����Drag���ꂽ�X�^���v�z��
     */
    protected void setDrragedStamp(ComponentHolder[] drragedStamp) {
        this.drragedStamp = drragedStamp;
    }

    /**
     * ����������B
     * @param editable �ҏW�\���ǂ����̃t���O
     * @param mediator �`���[�g���f�B�G�[�^�i���ۂɂ̓��j���[�T�|�[�g�j
     */
    public void init(boolean editable, ChartMediator mediator) {

        // Mediator��ۑ�����
        setMediator(mediator);

        // JTextPane�փA�N�V������o�^����
        // Undo & Redo
        ActionMap map = getTextPane().getActionMap();
        KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        map.put(keystroke, mediator.getAction(GUIConst.ACTION_UNDO));
        keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        map.put(keystroke, mediator.getAction(GUIConst.ACTION_REDO));
        keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        map.put(keystroke, mediator.getAction(GUIConst.ACTION_CUT));
        keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        map.put(keystroke, mediator.getAction(GUIConst.ACTION_COPY));
        keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        map.put(keystroke, mediator.getAction(GUIConst.ACTION_PASTE));

        // Drag �� editable �Ɋ֌W�Ȃ��\
        getTextPane().setDragEnabled(true);

        // ���X�i��o�^����
        getTextPane().addMouseListener(this);
        getTextPane().addCaretListener(this);

        // Editable Property ��ݒ肷��
        setEditableProp(editable);
    }

    public void setEditableProp(boolean editable) {
        getTextPane().setEditable(editable);
        if (editable) {
            getTextPane().getDocument().addDocumentListener(this);
            getTextPane().addFocusListener(AutoKanjiListener.getInstance());
            getTextPane().getDocument().addUndoableEditListener(mediator);
            if (myRole.equals(IInfoModel.ROLE_SOA)) {
                SOACodeHelper helper = new SOACodeHelper(this, getMediator());
            } else {
                PCodeHelper helper = new PCodeHelper(this, getMediator());
            }
            getTextPane().setBackground(Color.WHITE);
            getTextPane().setOpaque(true);
        } else {
            getTextPane().getDocument().removeDocumentListener(this);
            getTextPane().removeFocusListener(AutoKanjiListener.getInstance());
            getTextPane().getDocument().removeUndoableEditListener(mediator);
            setBackgroundUneditable();
        }
    }

    // JTextPane�ւ̑}����dirty���ǂ����𔻒肷��
    @Override
    public void insertUpdate(DocumentEvent e) {
        boolean newDirty = getDocument().getLength() > getInitialLength() ? true : false;
        setDirty(newDirty);
    }

    // �폜���N��������dirty���ǂ����𔻒肷��
    @Override
    public void removeUpdate(DocumentEvent e) {
        boolean newDirty = getDocument().getLength() > getInitialLength() ? true : false;
        setDirty(newDirty);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

//    public void focusGained(FocusEvent e) {
//        getTextPane().getInputContext().setCharacterSubsets(new Character.Subset[]{InputSubset.KANJI});
//    }
//
//    public void focusLost(FocusEvent e) {
//    }

    @Override
    public void caretUpdate(CaretEvent e) {
        boolean newSelection = (e.getDot() != e.getMark()) ? true : false;
        if (newSelection != hasSelection) {
            hasSelection = newSelection;

            // �e�L�X�g�I���̏�Ԃ֑J�ڂ���
            if (hasSelection) {
                curState = getMyRole().equals(IInfoModel.ROLE_SOA) ? CompState.SOA_TEXT : CompState.P_TEXT;
            } else {
                curState = getMyRole().equals(IInfoModel.ROLE_SOA) ? CompState.SOA : CompState.P;
            }
            controlMenus(mediator.getActions());
        }
    }

    /**
     * ���\�[�X��clear����B
     */
    public void clear() {

        getTextPane().getDocument().removeDocumentListener(this);
        getTextPane().removeMouseListener(this);
        getTextPane().removeFocusListener(AutoKanjiListener.getInstance());
        getTextPane().removeCaretListener(this);

        try {
            KarteStyledDocument doc = getDocument();
            doc.remove(0, doc.getLength());
            doc = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTextPane(null);
    }

    /**
     * ���j���[�𐧌䂷��B
     *
     */
    private void controlMenus(ActionMap map) {

        // �eState��enable�ɂȂ�����������Ǘ�����
        switch (curState) {

            case NONE:
                break;

            case SOA:
                // SOAPane��Focus������e�L�X�g�I�����Ȃ����
                if (getTextPane().isEditable()) {
                    map.get(GUIConst.ACTION_PASTE).setEnabled(canPaste());
                    map.get(GUIConst.ACTION_INSERT_TEXT).setEnabled(true);
                    map.get(GUIConst.ACTION_INSERT_SCHEMA).setEnabled(true);
                }
                break;

            case SOA_TEXT:
                // SOAPane��Focus������e�L�X�g�I����������
                map.get(GUIConst.ACTION_CUT).setEnabled(getTextPane().isEditable());
                map.get(GUIConst.ACTION_COPY).setEnabled(true);
                boolean pasteOk = (getTextPane().isEditable() && canPaste()) ? true : false;
                map.get(GUIConst.ACTION_PASTE).setEnabled(pasteOk);
                break;

            case P:
                // PPane��Focus������e�L�X�g�I�����Ȃ����
                if (getTextPane().isEditable()) {
                    map.get(GUIConst.ACTION_PASTE).setEnabled(canPaste());
                    map.get(GUIConst.ACTION_INSERT_TEXT).setEnabled(true);
                    map.get(GUIConst.ACTION_INSERT_STAMP).setEnabled(true);
                }
                break;

            case P_TEXT:
                // PPane��Focus������e�L�X�g�I����������
                map.get(GUIConst.ACTION_CUT).setEnabled(getTextPane().isEditable());
                map.get(GUIConst.ACTION_COPY).setEnabled(true);
                pasteOk = (getTextPane().isEditable() && canPaste()) ? true : false;
                map.get(GUIConst.ACTION_PASTE).setEnabled(pasteOk);
                break;
        }
    }

    @Override
    public void enter(ActionMap map) {
        curState = getMyRole().equals(IInfoModel.ROLE_SOA) ? CompState.SOA : CompState.P;
        controlMenus(map);
    }

    @Override
    public void exit(ActionMap map) {
    }

    @Override
    public Component getComponent() {
        return getTextPane();
    }

    protected JPopupMenu createMenus() {

        final JPopupMenu contextMenu = new JPopupMenu();

        // cut, copy, paste ���j���[��ǉ�����
        contextMenu.add(mediator.getAction(GUIConst.ACTION_CUT));
        contextMenu.add(mediator.getAction(GUIConst.ACTION_COPY));
        contextMenu.add(mediator.getAction(GUIConst.ACTION_PASTE));

        // �e�L�X�g�J���[���j���[��ǉ�����
        if (getTextPane().isEditable()) {
            ColorChooserComp ccl = new ColorChooserComp();
            ccl.addPropertyChangeListener(ColorChooserComp.SELECTED_COLOR, new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent e) {
                    Color selected = (Color) e.getNewValue();
                    Action action = new StyledEditorKit.ForegroundAction("selected", selected);
                    action.actionPerformed(new ActionEvent(getTextPane(), ActionEvent.ACTION_PERFORMED, "foreground"));
                    contextMenu.setVisible(false);
                }
            });
            JLabel l = new JLabel("  �J���[:");
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            p.add(l);
            p.add(ccl);
            contextMenu.add(p);
        } else {
            contextMenu.addSeparator();
        }

        // PPane �̏ꍇ��StampMenu��ǉ�����
        if (getMyRole().equals(IInfoModel.ROLE_P)) {
            //contextMenu.addSeparator();
            mediator.addStampMenu(contextMenu, this);
        } else {
            // TextMenu��ǉ�����
            mediator.addTextMenu(contextMenu);
        }

        return contextMenu;
    }

    private void mabeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            JPopupMenu contextMenu = createMenus();
            contextMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mabeShowPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mabeShowPopup(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    /**
     * �w�i��ҏW�s�J���[�ɐݒ肷��B
     */
    protected void setBackgroundUneditable() {
        getTextPane().setBackground(getUneditableColor());
        getTextPane().setOpaque(true);
    }

    /**
     * ���[���ƃp�[�g�i��ݒ肷��B
     * @param role ���̃y�C���̃��[��
     * @param partner �p�[�g�i
     */
    public void setRole(String role) {
        setMyRole(role);
    //setMyPartner(partner);
    }

    /**
     * Dirty���ǂ�����Ԃ��B
     * @return dirty �̎� true
     */
    protected boolean isDirty() {
        return getTextPane().isEditable() ? dirty : false;
    }
    
    protected void setDirty(boolean newDirty) {
        if (newDirty != dirty) {
            dirty = newDirty;
            getParent().setDirty(dirty);
        }
    }

    /**
     * �ۑ����ɂ���h�L�������g�̃^�C�g����Document Object���璊�o����B
     * @return �擪����w�肳�ꂽ������؂�o����������
     */
    protected String getTitle() {
        try {
            KarteStyledDocument doc = getDocument();
            int len = doc.getLength();
            int freeTop = 0; // doc.getFreeTop();
            int freeLen = len - freeTop;
            freeLen = freeLen < TITLE_LENGTH ? freeLen : TITLE_LENGTH;
            return getTextPane().getText(freeTop, freeLen).trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Document�̒i���X�^�C����ݒ肷��B
     * @param str �X�^�C��
     */
    public void setLogicalStyle(String str) {
        getDocument().setLogicalStyle(str);
    }

    /**
     * Document�̒i���_���X�^�C�����N���A����B
     */
    public void clearLogicalStyle() {
        getDocument().clearLogicalStyle();
    }

    /**
     * �i�����\������B
     */
    public void makeParagraph() {
        getDocument().makeParagraph();
    }

    /**
     * Document�ɕ������}������B
     * @param str �}�����镶����
     * @param attr ����
     */
    public void insertFreeString(String s, AttributeSet a) {
        getDocument().insertFreeString(s, a);
    }

    /**
     * ���̃y�C���� Stamp ��}������B
     */
    public void stamp(final ModuleModel stamp) {
        if (stamp != null) {
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    StampHolder h = new StampHolder(KartePane.this, stamp);
                    h.setTransferHandler(stampHolderTransferHandler);
                    KarteStyledDocument doc = getDocument();
                    doc.stamp(h);
                }
            });
        }
    }

    /**
     * ���̃y�C���� Stamp ��}������B
     */
    public void flowStamp(ModuleModel stamp) {
        if (stamp != null) {
            StampHolder h = new StampHolder(this, stamp);
            h.setTransferHandler(stampHolderTransferHandler);
            KarteStyledDocument doc = getDocument();
            doc.flowStamp(h);
        }
    }

    /**
     * ���̃y�C���ɃV�F�[�}��}������B
     * @param schema �V�F�[�}
     */
    public void stampSchema(final SchemaModel schema) {
        if (schema != null) {
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {

                    SchemaHolder h = new SchemaHolder(KartePane.this, schema);
                    h.setTransferHandler(schemaHolderTransferHandler);
                    KarteStyledDocument doc = getDocument();
                    doc.stampSchema(h);
                }
            });
        }
    }

    /**
     * ���̃y�C���ɃV�F�[�}��}������B
     * @param schema  �V�F�[�}
     */
    public void flowSchema(SchemaModel schema) {
        if (schema != null) {
            SchemaHolder h = new SchemaHolder(this, schema);
            h.setTransferHandler(schemaHolderTransferHandler);
            KarteStyledDocument doc = (KarteStyledDocument) getTextPane().getDocument();
            doc.flowSchema(h);
        }
    }

    /**
     * ���̃y�C���� TextStamp ��}������B
     */
    public void insertTextStamp(final String s) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                KarteStyledDocument doc = getDocument();
                doc.insertTextStamp(s);
            }
        });
    }

    /**
     * StampInfo��Drop���ꂽ���A���̃f�[�^���y�C���ɑ}������B
     * @param stampInfo �h���b�v���ꂽ�X�^���v���
     */
    public void stampInfoDropped(ModuleInfoBean stampInfo) {

        //
        // Drop ���ꂽ StampInfo �̑����ɉ����ď�����U������
        //
        String entity = stampInfo.getEntity();
        String role = stampInfo.getStampRole();

        //
        // �a���̏ꍇ�͂Q���J���e�y�C���ɂ͓W�J���Ȃ�
        //
        if (entity.equals(IInfoModel.ENTITY_DIAGNOSIS)) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        //
        // Text �X�^���v��}������
        //
        if (entity.equals(IInfoModel.ENTITY_TEXT)) {
            applyTextStamp(stampInfo);
            return;
        }

        //
        // ORCA ���̓Z�b�g�̏ꍇ
        //
        if (role.equals(IInfoModel.ROLE_ORCA_SET)) {
            applyOrcaSet(stampInfo);
            return;
        }

        //
        // �f�[�^�x�[�X�ɕۑ�����Ă���X�^���v��}������
        //
        if (stampInfo.isSerialized()) {
            applySerializedStamp(stampInfo);
            return;
        }

        //
        // Stamp �G�f�B�^���N������
        //
        ModuleModel stamp = new ModuleModel();
        stamp.setModuleInfo(stampInfo);
        new StampEditor(stamp, this);
    }

    /**
     * StampInfo��Drop���ꂽ���A���̃f�[�^���y�C���ɑ}������B
     * @param addList �X�^���v���̃��X�g
     */
    public void stampInfoDropped(final ArrayList<ModuleInfoBean> addList) {

        final StampDelegater sdl = new StampDelegater();
        
        DBTask task = new DBTask<List<StampModel>, Void>(parent.getContext()) {

            @Override
            protected List<StampModel> doInBackground() throws Exception {
                List<StampModel> list = sdl.getStamp(addList);
                return list;
            }
            
            @Override
            public void succeeded(List<StampModel> list) {
                logger.debug("stampInfoDropped succeeded");
                if (list != null) {
                    for (int i = 0; i < list.size(); i++) {
                        ModuleInfoBean stampInfo = addList.get(i);
                        StampModel theModel = list.get(i);
                        IInfoModel model = (IInfoModel) BeanUtils.xmlDecode(theModel.getStampBytes());
                        if (model != null) {
                            ModuleModel stamp = new ModuleModel();
                            stamp.setModel(model);
                            stamp.setModuleInfo(stampInfo);
                            stamp(stamp);
                        }
                    }
                }
            }
        };
        
        task.execute();
    }

    /**
     * TextStampInfo �� Drop ���ꂽ���̏������s�Ȃ��B
     */
    public void textStampInfoDropped(final ArrayList<ModuleInfoBean> addList) {
        
        final StampDelegater sdl = new StampDelegater();
        
        DBTask task = new DBTask<List<StampModel>, Void>(parent.getContext()) {

            @Override
            protected List<StampModel> doInBackground() throws Exception {
                List<StampModel> list = sdl.getStamp(addList);
                return list;
            }
            
            @Override
            public void succeeded(List<StampModel> list) {
                logger.debug("textStampInfoDropped succeeded");
                if (list != null) {
                    for (int i = 0; i < list.size(); i++) {
                        StampModel theModel = list.get(i);
                        IInfoModel model = (IInfoModel) BeanUtils.xmlDecode(theModel.getStampBytes());
                        if (model != null) {
                            insertTextStamp(model.toString() + "\n");
                        }
                    }
                }
            }
        };
        
        task.execute();
    }

    /**
     * TextStamp �����̃y�C���ɑ}������B
     */
    private void applyTextStamp(final ModuleInfoBean stampInfo) {
        
        final StampDelegater sdl = new StampDelegater();
        
        DBTask task = new DBTask<StampModel, Void>(parent.getContext()) {

            @Override
            protected StampModel doInBackground() throws Exception {
                StampModel getStamp = sdl.getStamp(stampInfo.getStampId());
                return getStamp;
            }
            
            @Override
            public void succeeded(StampModel result) {
                logger.debug("applyTextStamp succeeded");
                if (result != null) {
                    try {
                        byte[] bytes = result.getStampBytes();
                        XMLDecoder d = new XMLDecoder(new BufferedInputStream(new ByteArrayInputStream(bytes)));

                        IInfoModel model = (IInfoModel) d.readObject();
                        if (model != null) {
                            insertTextStamp(model.toString());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        
        task.execute();
    }

    /**
     * �i��������Ă���X�^���v���擾���Ă��̃y�C���ɓW�J����B
     */
    private void applySerializedStamp(final ModuleInfoBean stampInfo) {
        
        final StampDelegater sdl = new StampDelegater();
        
        DBTask task = new DBTask<StampModel, Void>(parent.getContext()) {

            @Override
            protected StampModel doInBackground() throws Exception {
                StampModel getStamp = sdl.getStamp(stampInfo.getStampId());
                return getStamp;
            }
            
            @Override
            public void succeeded(StampModel result) {
                logger.debug("applySerializedStamp succeeded");
                if (result != null) {
                    IInfoModel model = (IInfoModel) BeanUtils.xmlDecode(result.getStampBytes());
                    ModuleModel stamp = new ModuleModel();
                    stamp.setModel(model);
                    stamp.setModuleInfo(stampInfo);
                    stamp(stamp);
                }
            }
        };

        task.execute();
    }

    /**
     * ORCA �̓��̓Z�b�g���擾���Ă��̃y�C���ɓW�J����B
     */
    private void applyOrcaSet(final ModuleInfoBean stampInfo) {
        
        final SqlOrcaSetDao sdl = new SqlOrcaSetDao();
        
        DBTask task = new DBTask<List<ModuleModel>, Void>(parent.getContext()) {

            @Override
            protected List<ModuleModel> doInBackground() throws Exception {
                List<ModuleModel> models = sdl.getStamp(stampInfo);
                return models;
            }
            
            @Override
            public void succeeded(List<ModuleModel> models) {
                logger.debug("applyOrcaSet succeeded");
                if (models != null) {
                    for (ModuleModel stamp : models) {
                        stamp(stamp);
                    }
                }
            }
        };

        task.execute();
    }
    
    private void showMetaDataMessage() {
        
        Window w = SwingUtilities.getWindowAncestor(getTextPane());  
        JOptionPane.showMessageDialog(w,
                                      "�摜�̃��^�f�[�^���擾�ł����A�ǂݍ��ނ��Ƃ��ł��܂���B",
                                      ClientContext.getFrameTitle("�摜�C���|�[�g"),
                                      JOptionPane.WARNING_MESSAGE);
    }
    
    private boolean showMaxSizeMessage() {
        
        int maxImageWidth = ClientContext.getInt("image.max.width");
        int maxImageHeight = ClientContext.getInt("image.max.height");
        final Preferences pref = Preferences.userNodeForPackage(this.getClass());
        
        String title = ClientContext.getFrameTitle("�摜�T�C�Y�ɂ���");
        JLabel msg1 = new JLabel("�J���e�ɑ}������摜�́A�ő�� " + maxImageWidth + " x " + maxImageHeight + " pixcel �ɐ������Ă��܂��B");
        JLabel msg2 = new JLabel("�摜���k�����J���e�ɓW�J���܂���?");
        final JCheckBox cb = new JCheckBox("���ケ�̃��b�Z�[�W��\�����Ȃ�");
        cb.setFont(new Font("Dialog", Font.PLAIN, 10));
        cb.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                pref.putBoolean("showImageSizeMessage", !cb.isSelected());
            }
        });
        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
        p1.add(msg1);
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
        p2.add(msg2);
        JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
        p3.add(cb);
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.add(p1);
        box.add(p2);
        box.add(p3);
        box.setBorder(BorderFactory.createEmptyBorder(0, 0, 11, 11));
        Window w = SwingUtilities.getWindowAncestor(getTextPane());        

        int option = JOptionPane.showOptionDialog(w,
                            new Object[]{box},
                            ClientContext.getFrameTitle(title),
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            ClientContext.getImageIcon("about_32.gif"),
                            new String[]{"�k������", "�����"}, "�k������");
        return option == 0 ? true : false;
    }
    
    private void showNoReaderMessage() {
        Window w = SwingUtilities.getWindowAncestor(getTextPane());  
        JOptionPane.showMessageDialog(w,
                                      "�I�������摜��ǂނ��Ƃ��ł��郊�[�_�����݂��܂���B",
                                      ClientContext.getFrameTitle("�摜�C���|�[�g"),
                                      JOptionPane.WARNING_MESSAGE);
    }
    
    public void imageEntryDropped(final ImageEntry entry) {
        
        int width = entry.getWidth();
        int height = entry.getHeight();
        int maxImageWidth = ClientContext.getInt("image.max.width");
        int maxImageHeight = ClientContext.getInt("image.max.height");
        Preferences pref = Preferences.userNodeForPackage(this.getClass());
        boolean ok = true;
        
        if (width == 0 || height == 0) {
            Icon icon = entry.getImageIcon();
            width = icon.getIconWidth();
            height = icon.getIconHeight();
            if (width > maxImageWidth || height > maxImageHeight) {
                if (pref.getBoolean("showImageSizeMessage", true)) {
                    ok = showMaxSizeMessage();
                }
                //showMetaDataMessage();
                //ok = false;
            }
        } else if (width > maxImageWidth || height > maxImageHeight) {
            if (pref.getBoolean("showImageSizeMessage", true)) {
                ok = showMaxSizeMessage();
            }
        }

        if (!ok) {
            return;
        }        
        
        DBTask task = new DBTask<ImageIcon, Void>(parent.getContext()) {

            @Override
            protected ImageIcon doInBackground() throws Exception {
                
                URL url = new URL(entry.getUrl());
                BufferedImage importImage = ImageIO.read(url);
                
                int width = importImage.getWidth();
                int height = importImage.getHeight();
                int maxImageWidth = ClientContext.getInt("image.max.width");
                int maxImageHeight = ClientContext.getInt("image.max.height");
                
                if (width > maxImageWidth || height > maxImageHeight) {
                    importImage = ImageHelper.getFirstScaledInstance(importImage, maxImageWidth);
                }
                
                return new ImageIcon(importImage);
            }
            
            @Override
            public void succeeded(ImageIcon icon) {
               
                logger.debug("imageEntryDropped succeeded");
                
                if (icon != null) {
                    
                    SchemaModel schema = new SchemaModel();
                    schema.setIcon(icon);

                    // IInfoModel �Ƃ��� ExtRef ��ێ����Ă���
                    ExtRefModel ref = new ExtRefModel();
                    ref.setContentType("image/jpeg");
                    ref.setTitle("Schema Image");
                    schema.setExtRef(ref);

                    stampId++;
                    String fileName = getDocId() + "-" + stampId + ".jpg";
                    schema.setFileName(fileName);
                    ref.setHref(fileName);
                    
                    PluginLoader<SchemaEditor> loader 
                        = PluginLoader.load(SchemaEditor.class, ClientContext.getPluginClassLoader());
                    Iterator<SchemaEditor> iter = loader.iterator();
                    if (iter.hasNext()) {
                        final SchemaEditor editor = iter.next();
                        editor.setSchema(schema);
                        editor.setEditable(true);
                        editor.addPropertyChangeListener(KartePane.this);
                        Runnable awt = new Runnable() {

                            @Override
                            public void run() {
                                editor.start();
                            }
                        };
                        EventQueue.invokeLater(awt);
                    }
                }
            }
        };
        
        task.execute();
    }

    /**
     * Schema �� DnD ���ꂽ�ꍇ�A�V�F�[�}�G�f�B�^���J���ĕҏW����B
     */
    public void insertImage(String path) {
        
        if (path == null) {
            return;
        }
        
        String suffix = path.toLowerCase();
        int index = suffix.lastIndexOf('.');
        if (index == 0) {
            showNoReaderMessage();
            return;
        }
        suffix = suffix.substring(index+1);
            
        Iterator readers = ImageIO.getImageReadersBySuffix(suffix);

        if (!readers.hasNext()) {
            showNoReaderMessage();
            return;
        }

        ImageReader reader = (ImageReader) readers.next();
        logger.debug("reader = " + reader.getClass().getName());
        int width = 0;
        int height = 0;
        String name = null;
        try {
            File file = new File(path);
            name = file.getName();
            reader.setInput(new FileImageInputStream(file), true);
            width = reader.getWidth(0);
            height = reader.getHeight(0);
            
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return;
        }
        reader = null;
        ImageEntry entry = new ImageEntry();
        entry.setPath(path);
        entry.setFileName(name);
        entry.setNumImages(1);
        entry.setWidth(width);
        entry.setHeight(height);
        imageEntryDropped(entry);
    }

    /**
     * StampEditor �̕ҏW���I������Ƃ����֒ʒm�����B
     * �ʒm���ꂽ�X�^���v���y�C���ɑ}������B
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {

        String prop = e.getPropertyName();

        if (prop.equals("imageProp")) {

            SchemaModel schema = (SchemaModel) e.getNewValue();

            if (schema != null) {
                // �ҏW���ꂽ�V�F�[�}�����̃y�C���ɑ}������
                stampSchema(schema);
            }

        } else if (prop.equals(AbstractStampEditor.VALUE_PROP)) {

            Object o = e.getNewValue();

            if (o != null) {
                // �ҏW���ꂽ Stamp �����̃y�C���ɑ}������
                ModuleModel stamp = (ModuleModel) o;
                stamp(stamp);
            }
        }
    }

    /**
     * ���j���[����̂��߁A�y�[�X�g�\���ǂ�����Ԃ��B
     * @return �y�[�X�g�\�Ȏ� true
     */
    protected boolean canPaste() {

        boolean ret = false;
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (t == null) {
            return false;
        }

        if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return true;
        }

        if (getMyRole().equals(IInfoModel.ROLE_P)) {
            if (t.isDataFlavorSupported(OrderListTransferable.orderListFlavor)) {
                ret = true;
            }
        } else {
            if (t.isDataFlavorSupported(StampListTransferable.stampListFlavor) || t.isDataFlavorSupported(SchemaListTransferable.schemaListFlavor)) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * ���̃y�C������X�^���v���폜����B
     * @param sh �폜����X�^���v�̃z���_
     */
    public void removeStamp(StampHolder sh) {
        getDocument().removeStamp(sh.getStartPos(), 2);
    }

    /**
     * ���̃y�C������X�^���v���폜����B
     * @param sh �폜����X�^���v�̃z���_���X�g
     */
    public void removeStamp(StampHolder[] sh) {
        if (sh != null && sh.length > 0) {
            for (int i = 0; i < sh.length; i++) {
                removeStamp(sh[i]);
            }
        }
    }

    /**
     * ���̃y�C������V�F�[�}���폜����B
     * @param sh �폜����V�F�[�}�̃z���_
     */
    public void removeSchema(SchemaHolder sh) {
        getDocument().removeStamp(sh.getStartPos(), 2);
    }

    /**
     * ���̃y�C������V�F�[�}���폜����B
     * @param sh �폜����V�F�[�}�̃z���_���X�g
     */
    public void removeSchema(SchemaHolder[] sh) {
        if (sh != null && sh.length > 0) {
            for (int i = 0; i < sh.length; i++) {
                removeSchema(sh[i]);
            }
        }
    }
}