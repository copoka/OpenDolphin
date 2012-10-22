package open.dolphin.client;

import java.awt.Dimension;

public class GUIConst {
    
    //
    // client package �Ŏg�p����萔
    //
    // ���j���[�֘A
    public static final String ACTION_WINDOW_CLOSING = "processWindowClosing";
    public static final String PRINTER_SETUP = "printerSetup";
    public static final String SHOW_ABOUT = "showAbout";
    public static final String EXIT = "exit";
    public static final String CHANGE_PASSWORD = "changePassword";
    public static final String ADD_USER = "addUser";
    public static final String UPDATE_SOFTWARE = "update1";
    public static final String BROWSE_DOLPHIN_SUPPORT = "browseDolphinSupport";
    public static final String BROWSE_DOLPHIN_PROJECT = "browseDolphinProject";
    public static final String BROWSE_MEDXML = "browseMedXml";
    public static final String SET_KARTE_ENV = "setKarteEnviroment";
    public static final String SHOW_STAMP_BOX = "showStampBox";
    public static final String SHOW_SCHEMA_BOX = "showSchemaBox";
    public static final String MENU_TEXT = "�e�L�X�g";
    public static final String MENU_SCHEMA = "�V�F�[�}";
    public static final String MENU_STAMP = "�X�^���v";
    public static final String MENU_INSERT = "�} ��";
    public static final String MENU_INSURANCE = "�ی��I��";
    public static final String ACTION_SIZE = "size";
    public static final String ACTION_STYLE = "style";
    public static final String ACTION_ALIGNMENT = "alignment";
    public static final String ACTION_COLOR = "color";
    public static final String ACTION_RESET_STYLE = "resetStyle";
    public static final String ACTION_RED = "redAction";
    public static final String ACTION_ORANGE = "orangeAction";
    public static final String ACTION_YELLOW = "yellowAction";
    public static final String ACTION_GREEN = "greenAction";
    public static final String ACTION_BLUE = "blueAction";
    public static final String ACTION_PURPLE = "purpleAction";
    public static final String ACTION_GRAY = "grayAction";
    public static final String ACTION_S9 = "s9Action";
    public static final String ACTION_S10 = "s10Action";
    public static final String ACTION_S12 = "s12Action";
    public static final String ACTION_S14 = "s14Action";
    public static final String ACTION_S18 = "s18Action";
    public static final String ACTION_S24 = "s24Action";
    public static final String ACTION_S36 = "s36Action";
    public static final String ACTION_BOLD = "boldAction";
    public static final String ACTION_ITALIC = "italicAction";
    public static final String ACTION_UNDERLINE = "underlineAction";
    public static final String ACTION_LEFT_ALIGN = "leftAlignmentAction";
    public static final String ACTION_CENTER_ALIGN = "centerAlignmentAction";
    public static final String ACTION_RIGHT_ALIGN = "rightAlignmentAction";
    public static final String ACTION_NEW_KARTE = "newKarte";
    public static final String ACTION_SAVE = "save";
    public static final String ACTION_DELETE_KARTE = "delete";
    public static final String ACTION_PRINT = "print";
    public static final String ACTION_CUT = "cut";
    public static final String ACTION_COPY = "copy";
    public static final String ACTION_PASTE = "paste";
    public static final String ACTION_UNDO = "undo";
    public static final String ACTION_REDO = "redo";
    public static final String ACTION_MODIFY_KARTE = "modifyKarte";
    public static final String ACTION_ASCENDING = "ascending";
    public static final String ACTION_DESCENDING = "descending";
    public static final String ACTION_SHOW_MODIFIED = "showModified";
    public static final String ACTION_INSERT_TEXT = "insertText";
    public static final String ACTION_INSERT_SCHEMA = "insertSchema";
    public static final String ACTION_INSERT_STAMP = "insertStamp";
    public static final String ACTION_SELECT_INSURANCE = "selectInsurance";
    public static final String TOOLTIPS_INSERT_TEXT = "�e�L�X�g�X�^���v��}�����܂��B";
    public static final String TOOLTIPS_INSERT_SCHEMA = "�V�F�[�}��}�����܂��B";
    public static final String TOOLTIPS_INSERT_STAMP = "�I�[�_�X�^���v��}�����܂��B";
    public static final String TOOLTIPS_SELECT_INSURANCE = "�ی���I�����܂��B";
//    public static final String MENU_PRINTER_SETUP = "�y�[�W�ݒ�...";
//    public static final String MENU_ABOUT = "�A�o�E�g...";
//    public static final String MENU_EXIT = "�I��";
//    public static final String MENU_CHANGE_PASSWORD = "�p�X���[�h�ύX...";
//    public static final String MENU_ADD_USER = "���[�U�o�^...";
//    public static final String MENU_UPDATE_SOFTWARE = "�A�b�v�f�[�g�m�F...";
//    public static final String MENU_DOLPHIN_SUPPORT = "�h���t�B���T�|�[�g";
//    public static final String MENU_DOLPHIN_PROJECT = "�h���t�B���v���W�F�N�g";
//    public static final String MENU_MEDXML = "MedXML�R���\�[�V�A��";
//    public static final String MENU_SET_KARTE_ENV = "���ݒ�";
//    public static final String MENU_STAMP_BOX = "�X�^���v�{�b�N�X";
//    public static final String MENU_SCHEMA_BOX = "�V�F�[�}�{�b�N�X";
//    public static final String MENU_SIZE = "�T�C�Y";
//    public static final String MENU_STYLE = "�X�^�C��";
//    public static final String MENU_TEXT_ALIGN = "�s����";
//    public static final String MENU_COLOR = "�J���[";
//    public static final String MENU_RED = "���b�h";
//    public static final String MENU_ORANGE = "�I�����W";
//    public static final String MENU_YELLOW = "�C�F���[";
//    public static final String MENU_GREEN = "�O���[��";
//    public static final String MENU_BLUE = "�u���[";
//    public static final String MENU_PURPLE = "�p�[�v��";
//    public static final String MENU_GRAY = "�O���[";
//    public static final String MENU_LEFT_ALIGN = "������";
//    public static final String MENU_CENTER_ALIGN = "��������";
//    public static final String MENU_RIGHT_ALIGN = "�E����";

    
    // JNDI
    public static final String JNDI_STAMP_BOX = "mainWindow/stampBox";
    public static final String JNDI_SCHEMA_BOX = "mainWindow/schemaBox";
    public static final String JNDI_SEND_CLAIM = "karteEditor/sendClaim";
    public static final String JNDI_SEND_MML = "karteEditor/sendMml";
    public static final String JNDI_WATING_LIST = "mainWindow/comp/watingList";
    public static final String JNDI_CHART = "mainWindow/chart";
    public static final String JNDI_MENUBAR_BUILDER = "helper/menuBarBuilder";
    public static final String JNDI_CHANGE_PASSWORD = "mainWindow/menu/system/changePassword";
    public static final String JNDI_ADD_USER = "mainWindow/menu/system/addUser";
    public static final String JNDI_STAMP_PUBLISH = "mainWindow/menu/system/stampPublish";
    
    // Role
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_USER = "user";
    
    // ���ݒ��̃T�[�r�X�J�n��~�֘A
    public static final String KEY_PVT_SERVER = "pvtServer";
    public static final String KEY_SEND_CLAIM = "sendClaim";
    public static final String KEY_SEND_MML = "sendMml";
    public static final String SERVICE_RUNNING = "running";
    public static final String SERVICE_NOT_RUNNING = "notRunning";
    public static final String ADDRESS_CLAIM = "claimAddress";
    public static final String CSGW_PATH = "csgwPath";

    //
    // order package �Ŏg�p����萔
    //
    public static final int DEFAULT_CMP_V_SPACE = 11;
    
    public static final int DEFAULT_STAMP_EDITOR_WIDTH  = 700;
    public static final int DEFAULT_STAMP_EDITOR_HEIGHT = 690;
    public static final Dimension DEFAULT_STAMP_EDITOR_SIZE = new Dimension(DEFAULT_STAMP_EDITOR_WIDTH, DEFAULT_STAMP_EDITOR_HEIGHT);
    
    public static final int DEFAULT_EDITOR_WIDTH 	= 680;  //724
    public static final int DEFAULT_EDITOR_HEIGHT 	= 230;    
    
}
