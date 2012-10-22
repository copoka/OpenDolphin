package open.dolphin.order;

public class ClaimConst {
    
    public enum ClaimSpec {
        
        BASE_CHARGE("�f�f��","110-125",null),
        
        INSTRACTION_CHARGE("�w���E�ݑ�", "130-140", null),
        
        INJECTION("�� ��", "300-331", null),
        
        TREATMENT("�� �u", "400-499", "400"),
        
        SURGERY("��p", "500-599", "500"),
        
        LABO_TEST("���̌���" ,"600-699" ,"600"),
        
        PHYSIOLOGY("�����E����������", "600-699", "600"),
        
        RADIOLOGY("���ː�", "700-799", "700"),
        
        OTHER("���̑�", "800-899", "800"),
        
        GENERAL("�� �p", null, null);
        
        private String name;
        private String searchCode;
        private String classCode;
        
        ClaimSpec(String name, String searchCode, String classCode) {
            setName(name);
            setSearchCode(searchCode);
            setClassCode(classCode);
        }
        
        public String getClassCode() {
            return classCode;
        }
        
        public void setClassCode(String classCode) {
            this.classCode = classCode;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getSearchCode() {
            return searchCode;
        }
        
        public void setSearchCode(String searchCode) {
            this.searchCode = searchCode;
        }
    }
    
    public enum MasterSet {
        
        DIAGNOSIS("disease", "���a��"),
        
        TREATMENT("treatment", "�f�Ís��"),
        
        MEDICAL_SUPPLY("medicine", "���p�E�O�p��"),
        
        ADMINISTRATION("admin", "�p�@"),
        
        INJECTION_MEDICINE("medicine", "���˖�"),
        
        TOOL_MATERIAL("tool_material", "������");
        
        private String name;
        private String dispName;
        
        MasterSet(String name, String dispName) {
            setName(name);
            setDispName(dispName);
        }
        
        public String getDispName() {
            return dispName;
        }
        
        public void setDispName(String dispName) {
            this.dispName = dispName;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    public static final String DISEASE_MASTER_SYSTEM = "mml.codeSystem.diseaseMaster=ICD10_2001-10-03MEDIS";
    
    public static final String MASTER_FLAG_MEDICICE     = "20";
    public static final String MASTER_FLAG_INJECTION    = "40";
    
    public static final String CLASS_CODE_ID 	= "Claim007";	// �f�Ís�׋敪�e�[�u��ID
    public static final String SUBCLASS_CODE_ID = "Claim003";	// ��Z�A�ޗ��A��܋敪�e�[�u��ID
    public static final String NUMBER_CODE_ID	= "Claim004";	// ���ʃR�[�h�e�[�u��ID
    
    /** ��Z */
    public static final int SYUGI  	= 0;
    
    /** �ޗ� */
    public static final int ZAIRYO 	= 1;
    
    /** ��� */
    public static final int YAKUZAI     = 2;
    
    /** ��܋敪 ���p */
    public static final String YKZ_KBN_NAIYO = "1";
    
    /** ��܋敪 ���� */
    public static final String YKZ_KBN_INJECTION = "4";
    
    /** ��܋敪 �O�p */
    public static final String YKZ_KBN_GAIYO = "6";
    
    /** ���Z�d�Z�R�[�h ���p */
    public static final String RECEIPT_CODE_NAIYO   = "210";
    
    /** ���Z�d�Z�R�[�h �O�p*/
    public static final String RECEIPT_CODE_GAIYO   = "230";
    
    public static final String YAKUZAI_TOYORYO      = "10";     // ��ܓ��^��
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
    public static final String ZAIRYO_CODE_START  = "7";
    
    /** �p�@�R�[�h�̓��ԍ� */
    public static final String ADMIN_CODE_START = "001";
    
    /** ���ː����ʃR�[�h�̓��ԍ� */
    public static final String RBUI_CODE_START = "002";
    
    /** �@������ */
    public static final String IN_MEDICINE     = "�@������";
    
    /** �@�O���� */
    public static final String EXT_MEDICINE    = "�@�O����";
    
    
}