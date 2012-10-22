package open.dolphin.order;

public class ClaimConst {

    public enum ClaimSpec {

        BASE_CHARGE("ff¿", "110-125", null),
        INSTRACTION_CHARGE("w±EÝî", "130-140", null),
        INJECTION(" Ë", "300-331", null),
        TREATMENT(" u", "400-499", "400"),
        SURGERY("èp", "500-599", "500"),
        LABO_TEST("Ì¸", "600-699", "600"),
        PHYSIOLOGY("¶Eà¾¸", "600-699", "600"),
        RADIOLOGY("úËü", "700-799", "700"),
        OTHER("»Ì¼", "800-899", "800"),
        GENERAL("Ä p", null, null);
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

        DIAGNOSIS("disease", "a¼"),
        TREATMENT("treatment", "fÃs×"),
        MEDICAL_SUPPLY("medicine", "àpEOpò"),
        ADMINISTRATION("admin", "p@"),
        INJECTION_MEDICINE("medicine", "Ëò"),
        TOOL_MATERIAL("tool_material", "ÁèíÞ");
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
    public static final String MASTER_FLAG_MEDICICE = "20";
    public static final String MASTER_FLAG_INJECTION = "40";
    public static final String CLASS_CODE_ID = "Claim007";	// fÃs×æªe[uID
    public static final String SUBCLASS_CODE_ID = "Claim003";	// èZAÞ¿AòÜæªe[uID
    public static final String NUMBER_CODE_ID = "Claim004";	// ÊR[he[uID
    /** èZ */
    public static final int SYUGI = 0;
    /** Þ¿ */
    public static final int ZAIRYO = 1;
    /** òÜ */
    public static final int YAKUZAI = 2;
    /** p@ */
    public static final int ADMIN = 3;
    /** òÜæª àp */
    public static final String YKZ_KBN_NAIYO = "1";
    /** òÜæª Ë */
    public static final String YKZ_KBN_INJECTION = "4";
    /** òÜæª Op */
    public static final String YKZ_KBN_GAIYO = "6";
    /** ZdZR[h àp */
    public static final String RECEIPT_CODE_NAIYO = "210";
    /** ZdZR[h Op*/
    public static final String RECEIPT_CODE_GAIYO = "230";
    public static final String YAKUZAI_TOYORYO = "10";     // òÜ^Ê
    public static final String YAKUZAI_TOYORYO_1KAI = "11";	// òÜ^ÊPñ
    public static final String YAKUZAI_TOYORYO_1NICHI = "12";	// òÜ^ÊPú
    public static final String ZAIRYO_KOSU = "21";		// Þ¿Â
    public static final String INJECTION_310 = "310";
    public static final String INJECTION_320 = "320";
    public static final String INJECTION_330 = "330";
    public static final String INJECTION_311 = "311";
    public static final String INJECTION_321 = "321";
    public static final String INJECTION_331 = "331";
    /** èZifÃs×jR[hÌªÔ */
    public static final String SYUGI_CODE_START = "1";
    /** òÜR[hÌªÔ */
    public static final String YAKUZAI_CODE_START = "6";
    /** Þ¿R[hÌªÔ */
    public static final String ZAIRYO_CODE_START = "7";
    /** p@R[hÌªÔ */
    public static final String ADMIN_CODE_START = "001";
    /** úËüÊR[hÌªÔ */
    public static final String RBUI_CODE_START = "002";
    /** @àû */
    public static final String IN_MEDICINE = "@àû";
    /** @Oû */
    public static final String EXT_MEDICINE = "@Oû";
}
