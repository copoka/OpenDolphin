package open.dolphin.infomodel;

/**
 * IInfoModel
 *
 * @athor Minagawa, Kazushi
 *
 */
public interface IInfoModel extends java.io.Serializable, java.lang.Cloneable {
    
    /** ISO 8601 style date format */
    public static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    
    /** �J���e�̊m����\���p�̃t�H�[�}�b�g */
    public static final String KARTE_DATE_FORMAT = "yyyy�NM��d��'�i'EEE'�j'H��m��";
    
    /** ���ԕ����̂Ȃ� Date */
    public static final String DATE_WITHOUT_TIME = "yyyy-MM-dd";
    
    /** Oersistence Query �� LIKE ���Z�q */
    public static final String LIKE_OPERATOR = "%";
    
    /** �����L�[�ɂ��邽�߂̌����q */
    public static final String COMPOSITE_KEY_MAKER = ":";
    
    /** �Ǘ��҃��[�� */
    public static final String ADMIN_ROLE = "admin";
    
    /** ���p�҃��[�� */
    public static final String USER_ROLE = "user";
    
    /** ������ */
    public static final String MARITAL_STATUS = "maritalStatus";
    
    /** ���� */
    public static final String NATIONALITY = "nationality";
    
    /** ���� */
    public static final String MEMO = "memo";    
    
    public static final String MALE 		= "male";
    
    public static final String MALE_DISP 	= "�j";
    
    public static final String FEMALE 		= "female";
    
    public static final String FEMALE_DISP 	= "��";
    
    public static final String UNKNOWN 		= "�s��";
    
    public static final String AGE 		= "��";

    /** �v���C������ */
    public static final String DOCTYPE_S_KARTE = "s_karte";

    /** 2���J���e */
    public static final String DOCTYPE_KARTE = "karte";

    /** �Љ�� */
    public static final String DOCTYPE_LETTER = "letter";

    /** �Љ��ԏ� */
    public static final String DOCTYPE_LETTER_REPLY = "letterReply";

    /** �Љ��ԏ�2 */
    public static final String DOCTYPE_LETTER_REPLY2 = "letterReply2";

    /** �Љ��y�ѕԏ��Ńv���O�C�� */
    public static final String DOCTYPE_LETTER_PLUGIN = "letterPlugin";
    
    public static final String PURPOSE_RECORD = "recode";
    
    public static final String PARENT_OLD_EDITION = "oldEdition";
    
    public static final String RELATION_NEW = "newVersion";
    public static final String RELATION_OLD = "oldVersion";
    
    public static final String DEFAULT_DIAGNOSIS_TITLE = "�a���o�^";
    public static final String DEFAULT_DIAGNOSIS_CATEGORY = "mainDiagnosis";
    public static final String DEFAULT_DIAGNOSIS_CATEGORY_DESC = "��a��";
    public static final String DEFAULT_DIAGNOSIS_CATEGORY_CODESYS = "MML0012";
    public static final String ORCA_OUTCOME_RECOVERED ="����";
    public static final String ORCA_OUTCOME_DIED = "���S";
    public static final String ORCA_OUTCOME_END = "���~";
    public static final String ORCA_OUTCOME_TRANSFERED = "�ڍs";
    
    //
    // Stamp Roles
    //
    /** ProgessCourse */
    public static final String MODULE_PROGRESS_COURSE           = "progressCourse";
    
    /** SOA stamp */
    public static final String ROLE_SOA 			= "soa";
    
    /** P stamp */
    public static final String ROLE_P 				= "p";
    
    /** SOA spec */
    public static final String ROLE_SOA_SPEC 			= "soaSpec";
    
    /** P spec */
    public static final String ROLE_P_SPEC 			= "pSpec";
    
    /** Text stamp */
    public static final String ROLE_TEXT 			= "text";
    
    /** ���a�� */
    public static final String ROLE_DIAGNOSIS 			= "diagnosis";
    
    /** ORCA ���̓Z�b�g */
    public static final String ROLE_ORCA_SET                    = "orcaSet";
    
    
    public static final String STATUS_FINAL 			= "F";
    public static final String STATUS_MODIFIED 			= "M";
    public static final String STATUS_TMP                       = "T";
    public static final String STATUS_NONE                      = "N";
    public static final String STATUS_DELETE                    = "D";
    
    public static final String PERMISSION_ALL = "all";
    public static final String PERMISSION_READ = "read";
    public static final String ACCES_RIGHT_PATIENT = "patient";
    public static final String ACCES_RIGHT_CREATOR = "creator";
    public static final String ACCES_RIGHT_EXPERIENCE = "experience";
    public static final String ACCES_RIGHT_PATIENT_DISP = "��L�ڎ�(����)";
    public static final String ACCES_RIGHT_CREATOR_DISP = "�L�ڎҎ{��";
    public static final String ACCES_RIGHT_EXPERIENCE_DISP = "�f�×��̂���{��";
    public static final String ACCES_RIGHT_PERSON_CODE = "personCode";
    public static final String ACCES_RIGHT_FACILITY_CODE = "facilityCode";
    public static final String ACCES_RIGHT_EXPERIENCE_CODE = "facilityCode";
    
    /** �����R�[�h */
    public static final String CLAIM_210 = "210";
    
    public static final String INSURANCE_SELF = "����";
    public static final String INSURANCE_SELF_CODE = "Z1";
    public static final String INSURANCE_SELF_PREFIX = "Z";
    public static final String INSURANCE_SYS = "MML031";
    
    //
    // StampTree�̃G���e�B�e�B�i���̎��́j��
    //
    /** ���a�� */
    public static final String ENTITY_DIAGNOSIS = "diagnosis";
    
    /** �e�L�X�g */
    public static final String ENTITY_TEXT = "text";
    
    /** �p �X */
    public static final String ENTITY_PATH = "path";
    
    /** �ėp */
    public static final String ENTITY_GENERAL_ORDER = "generalOrder";
    
    /** ���̑� */
    public static final String ENTITY_OTHER_ORDER = "otherOrder";
    
    /** �� �u */
    public static final String ENTITY_TREATMENT = "treatmentOrder";
    
    /** �� �p */
    public static final String ENTITY_SURGERY_ORDER = "surgeryOrder";
    
    /** ���ː� */
    public static final String ENTITY_RADIOLOGY_ORDER = "radiologyOrder";
    
    /** ���{�e�X�g */
    public static final String ENTITY_LABO_TEST = "testOrder";
    
    /** ���̌��� */
    public static final String ENTITY_PHYSIOLOGY_ORDER = "physiologyOrder";
    
    /** �׋ی��� */
    public static final String ENTITY_BACTERIA_ORDER = "bacteriaOrder";
    
    /** �� �� */
    public static final String ENTITY_INJECTION_ORDER = "injectionOrder";
    
    /** �� �� */
    public static final String ENTITY_MED_ORDER = "medOrder";
    
    /** �f �f */
    public static final String ENTITY_BASE_CHARGE_ORDER = "baseChargeOrder";
    
    /** �w �� */
    public static final String ENTITY_INSTRACTION_CHARGE_ORDER = "instractionChargeOrder";
    
    /** ORCA �Z�b�g */
    public static final String ENTITY_ORCA = "orcaSet";
    
    /** Entity �̔z�� */
    public static final String[] STAMP_ENTITIES = new String[] {
        ENTITY_DIAGNOSIS, ENTITY_TEXT, ENTITY_PATH, ENTITY_ORCA, ENTITY_GENERAL_ORDER, ENTITY_OTHER_ORDER, ENTITY_TREATMENT,
        ENTITY_SURGERY_ORDER, ENTITY_RADIOLOGY_ORDER, ENTITY_LABO_TEST, ENTITY_PHYSIOLOGY_ORDER,
        ENTITY_BACTERIA_ORDER, ENTITY_INJECTION_ORDER, ENTITY_MED_ORDER, ENTITY_BASE_CHARGE_ORDER, ENTITY_INSTRACTION_CHARGE_ORDER
    };
    
    //
    // StampTree�̃^�u��
    //
    /** ���a�� */
    public static final String TABNAME_DIAGNOSIS = "���a��";
    
    /** �e�L�X�g */
    public static final String TABNAME_TEXT = "�e�L�X�g";
    
    /** �p �X */
    public static final String TABNAME_PATH = "�p �X";
    
    /** ORCA �Z�b�g */
    public static final String TABNAME_ORCA = "ORCA";
    
    /** �� �p */
    public static final String TABNAME_GENERAL = "�� �p";
    
    /** ���̑� */
    public static final String TABNAME_OTHER = "���̑�";
    
    /** �� �u */
    public static final String TABNAME_TREATMENT = "�� �u";
    
    /** �� �p */
    public static final String TABNAME_SURGERY = "�� �p";
    
    /** ���ː� */
    public static final String TABNAME_RADIOLOGY = "���ː�";
    
    /** ���̌��� */
    public static final String TABNAME_LABO = "���̌���";
    
    /** ���̌��� */
    public static final String TABNAME_PHYSIOLOGY = "���̌���";
    
    /** �׋ی��� */
    public static final String TABNAME_BACTERIA = "�׋ی���";
    
    /** �� �� */
    public static final String TABNAME_INJECTION = "�� ��";
    
    /** �� �� */
    public static final String TABNAME_MED = "�� ��";
    
    /** ���f�E�Đf */
    public static final String TABNAME_BASE_CHARGE = "���f�E�Đf";
    
    /** �w���E�ݑ� */
    public static final String TABNAME_INSTRACTION = "�w���E�ݑ�";
    
    /** ORCA �̃^�u�ԍ� */
    public static final int TAB_INDEX_ORCA = 3;
    
    
    /** �X�^���v�̃^�u���z�� */
    public static String[] STAMP_NAMES = {
        TABNAME_DIAGNOSIS, TABNAME_TEXT, TABNAME_PATH, TABNAME_ORCA, 
        TABNAME_GENERAL, TABNAME_OTHER, TABNAME_TREATMENT, TABNAME_SURGERY, 
        TABNAME_RADIOLOGY, TABNAME_LABO, TABNAME_PHYSIOLOGY, TABNAME_BACTERIA,
        TABNAME_INJECTION, TABNAME_MED, TABNAME_BASE_CHARGE, TABNAME_INSTRACTION
    };
    
    /** �X�^���v��CLAIM�œ_���W�v�� */
    public static String[] CLAIM_CLASS_CODE = {
        "", "", "", "", "", "800-899", "400-499", "500-599", "700-799", "600-699", "600-699",
        "600", "300-331", "210-230", "110-125", "130-140"
    };
    
    public static final String OBSERVATION_ALLERGY = "Allergy";
    public static final String OBSERVATION_BLOODTYPE = "Bloodtype";
    public static final String OBSERVATION_INFECTION = "Infection";
    public static final String OBSERVATION_LIFESTYLE = "Lifestyle";
    public static final String OBSERVATION_PHYSICAL_EXAM = "PhysicalExam";
    
    public static final String PHENOMENON_BODY_HEIGHT = "bodyHeight";
    public static final String PHENOMENON_BODY_WEIGHT = "bodyWeight";
    
    public static final String PHENOMENON_TOBACCO = "tobacco";
    public static final String PHENOMENON_ALCOHOL = "alcohol";
    public static final String PHENOMENON_OCCUPATION = "occupation";
    
    public static final String UNIT_BODY_WEIGHT = "Kg";
    public static final String UNIT_BODY_HEIGHT = "cm";
    
    public static final String PUBLISH_TREE_LOCAL = "�@��";
    public static final String PUBLISH_TREE_PUBLIC = "�O���[�o��";
    public static final String PUBLISHED_TYPE_GLOBAL = "global";

    public static final String CONSULTANT = "consultant";
    public static final String CLIENT = "client";

    public static final String MEDICAL_CERTIFICATE = "medicalCertificate";
    
}
