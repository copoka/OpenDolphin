package open.dolphin.infomodel;

public final class ClaimConst {
    
    public static final String DISEASE_MASTER_SYSTEM = "mml.codeSystem.diseaseMaster=ICD10_2001-10-03MEDIS";
    public static final String CLASS_CODE_ID    = "Claim007";	// �f�Ís�׋敪�e�[�u��ID
    public static final String SUBCLASS_CODE_ID = "Claim003";	// ��Z�A�ޗ��A��܋敪�e�[�u��ID
    public static final String NUMBER_CODE_ID   = "Claim004";	// ���ʃR�[�h�e�[�u��ID

    /** ��Z */
    public static final int SYUGI = 0;

    /** �ޗ� */
    public static final int ZAIRYO = 1;

    /** ��� */
    public static final int YAKUZAI = 2;

    /** �p�@ */
    public static final int ADMIN = 3;

    /** ���� */
    public static final int BUI = 4;
    
    /** ���̑� */
    public static final int OTHER = 5;

    /** ��܋敪 ���p */
    public static final String YKZ_KBN_NAIYO = "1";

    /** ��܋敪 ���� */
    public static final String YKZ_KBN_INJECTION = "4";

    /** ��܋敪 �O�p */
    public static final String YKZ_KBN_GAIYO = "6";

    /** ���Z�d�Z�R�[�h ���p */
    public static final String RECEIPT_CODE_NAIYO = "210";

    /** ���Z�d�Z�R�[�h ���p�@�� */
    public static final String RECEIPT_CODE_NAIYO_IN = "211";

    /** ���Z�d�Z�R�[�h ���p�@�O */
    public static final String RECEIPT_CODE_NAIYO_EXT = "212";
    
    /** ���Z�d�Z�R�[�h ���p� */
    public static final String RECEIPT_CODE_NAIYO_HOKATSU = "213";

    /** ���Z�d�Z�R�[�h �ڗp */
    public static final String RECEIPT_CODE_TONYO = "220";

    /** ���Z�d�Z�R�[�h �ڗp�@�� */
    public static final String RECEIPT_CODE_TONYO_IN = "221";

    /** ���Z�d�Z�R�[�h �ڗp�@�O */
    public static final String RECEIPT_CODE_TONYO_EXT = "222";

    /** ���Z�d�Z�R�[�h �ڗp� */
    public static final String RECEIPT_CODE_TONYO_HOKATSU = "222";

    /** ���Z�d�Z�R�[�h �O�p*/
    public static final String RECEIPT_CODE_GAIYO = "230";

    /** ���Z�d�Z�R�[�h �O�p�@��*/
    public static final String RECEIPT_CODE_GAIYO_IN = "231";

    /** ���Z�d�Z�R�[�h �O�p�@�O*/
    public static final String RECEIPT_CODE_GAIYO_EXT = "232";

     /** ���Z�d�Z�R�[�h �O�p�*/
    public static final String RECEIPT_CODE_GAIYO_HOKATSU = "233";

    public static final String YAKUZAI_TOYORYO = "10";          // ��ܓ��^��
    public static final String YAKUZAI_TOYORYO_1KAI = "11";	// ��ܓ��^�ʂP��
    public static final String YAKUZAI_TOYORYO_1NICHI = "12";	// ��ܓ��^�ʂP��
    public static final String ZAIRYO_KOSU = "21";		// �ޗ���
    public static final String INJECTION_310 = "310";
    public static final String INJECTION_320 = "320";
    public static final String INJECTION_330 = "330";
    public static final String INJECTION_311 = "311";
    public static final String INJECTION_321 = "321";
    public static final String INJECTION_331 = "331";

    /** ��Z�i�f�Ís�ׁj�R�[�h�̓��ԍ� */
    public static final String SYUGI_CODE_START = "1";

    /** ��܃R�[�h�̓��ԍ� */
    public static final String YAKUZAI_CODE_START = "6";

    /** �ޗ��R�[�h�̓��ԍ� */
    public static final String ZAIRYO_CODE_START = "7";

    /** �p�@�R�[�h�̓��ԍ� */
    public static final String ADMIN_CODE_START = "001";

    /** ���ː����ʃR�[�h�̓��ԍ� */
    public static final String RBUI_CODE_START = "002";

    /** �@������ */
    public static final String IN_MEDICINE = "�@������";
    
    /** �@�O���� */
    public static final String EXT_MEDICINE = "�@�O����";

    public static final String SLOT_SYUGI = "��Z";
    public static final String SLOT_NAIYO_YAKU = "���p��";
    public static final String SLOT_TYUSHYA_YAKU = "���˖�";
    public static final String SLOT_GAIYO_YAKU = "�O�p��";
    public static final String SLOT_YAKUZAI = "���";
    public static final String SLOT_MEDICINE = "��";
    public static final String SLOT_ZAIRYO = "�ޗ�";
    public static final String SLOT_YOHO = "�p�@";
    public static final String SLOT_BUI = "����";
    public static final String SLOT_OTHER = "���̑�";
    
    public static final String UNIT_T = "��";
    public static final String UNIT_G = "��";
    public static final String UNIT_ML = "���k";
    public static final String UNIT_CAPSULE = "�J�v�Z��";
}
