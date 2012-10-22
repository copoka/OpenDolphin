package open.dolphin.project;

import java.awt.*;
import java.io.OutputStream;
import java.util.prefs.Preferences;

import open.dolphin.client.*;
import open.dolphin.infomodel.ID;
import open.dolphin.infomodel.UserModel;

/**
 * �v���W�F�N�g���Ǘ��N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class Project  {
    
    // Prpject Name
    public static final String PROJECT_NAME		= "name";
    
    // USER
    public static final String USER_TYPE		= "userType";
    public static final String USER_ID 			= "userId";
    public static final String FACILITY_ID 		= "facilityId";
    
    // SERVER
    public static final String HOST_ADDRESS 		= "hostAddress";
    public static final String HOST_PORT 		= "hostPort";
    
    // CLAIM
    public static final String SEND_CLAIM 		= "sendClaim";
    public static final String SEND_CLAIM_SAVE          = "sendClaimSave";
    public static final String SEND_CLAIM_TMP 		= "sendClaimTmp";
    public static final String SEND_CLAIM_MODIFY 	= "sendClaimModify";
    public static final String SEND_DIAGNOSIS 		= "sendDiagnosis";
    public static final String CLAIM_HOST_NAME          = "claimHostName";
    public static final String CLAIM_VERSION 		= "claimVersion";
    public static final String CLAIM_ENCODING 		= "claimEncoding";
    public static final String CLAIM_ADDRESS 		= "claimAddress";
    public static final String CLAIM_PORT 		= "claimPort";
    public static final String USE_AS_PVT_SERVER 	= "useAsPVTServer";
    
    // Area Network
    public static final String JOIN_AREA_NETWORK 	= "joinAreaNetwork";
    public static final String AREA_NETWORK_NAME 	= "jareaNetworkName";
    public static final String AREA_NETWORK_FACILITY_ID = "jareaNetworkFacilityId";
    public static final String AREA_NETWORK_CREATOR_ID  = "jareaNetworkCreatorId";
    
    // MML
    public static final String SEND_MML			= "mml.send";
    public static final String MML_VERSION    		= "mml.version";
    public static final String MML_ENCODING		= "mml.encoding";
    public static final String SEND_MML_ADDRESS 	= "mml.address";
    public static final String SEND_MML_DIRECTORY 	= "mml.directory";
    public static final String SEND_MML_PROTOCOL 	= "mml.protocol";
    
    // �\�t�g�E�F�A�X�V
    public static final String USE_PROXY		= "useProxy";
    public static final String PROXY_HOST		= "proxyHost";
    public static final String PROXY_PORT		= "proxyPort";
    public static final String LAST_MODIFIED  		= "lastModify";
    
    // �C���X�y�N�^�̃����ʒu
    public static final String INSPECTOR_MEMO_LOCATION  = "inspectorMemoLocation";
    
    // �C���X�y�N�^�� locationByPlatform ����
    public static final String LOCATION_BY_PLATFORM     = "locationByPlatform";
    
    // ��������
    public static final String DOC_HISTORY_ASCENDING 	= "docHistory.ascending";
    public static final String DOC_HISTORY_SHOWMODIFIED = "docHistory.showModified";
    public static final String DOC_HISTORY_FETCHCOUNT 	= "docHistory.fetchCount";
    public static final String DOC_HISTORY_PERIOD 	= "docHistory.period";
    public static final String KARTE_SCROLL_DIRECTION   = "karte.scroll.direction";
    public static final String DOUBLE_KARTE             = "karte.double";
    
    // �a��
    public static final String DIAGNOSIS_ASCENDING 	= "diagnosis.ascending";
    public static final String DIAGNOSIS_PERIOD 	= "diagnosis.period";
    public static final String OFFSET_OUTCOME_DATE 	= "diagnosis.offsetOutcomeDate";
    
    // ���̌���
    public static final String LABOTEST_PERIOD 		= "laboTest.period";
    
    // ����
    public static final String RP_OUT			= "rp.out";
        
    // �m�F�_�C�A���O
    public static final String KARTE_SHOW_CONFIRM_AT_NEW = "karte.showConfirmAtNew";
    public static final String KARTE_CREATE_MODE = "karte.createMode";
    public static final String KARTE_PLACE_MODE = "karte.placeMode";
    public static final String KARTE_SHOW_CONFIRM_AT_SAVE = "karte.showConfirmAtSave";
    public static final String KARTE_PRINT_COUNT = "karte.printCount";
    public static final String KARTE_SAVE_ACTION = "karte.saveAction";
    
    // ���[�U�̗��p�`��
    public enum UserType {ASP_MEMBER, ASP_TESTER, ASP_DEV, FACILITY_USER, UNKNOWN, EXPIRED};
    
    private static ProjectStub stub;
    
    /** Creates new Project */
    public Project() {
    }
    
    public static void setProjectStub(ProjectStub p) {
        stub = p;
    }
    
    public static ProjectStub getProjectStub() {
        return stub;
    }
    
    public static UserType getUserType() {
        return stub.getUserType();
    }
    
    public static boolean isValid() {
        return stub.isValid();
    }
    
    public static Preferences getPreferences() {
        return stub.getPreferences();
    }
    
    public static DolphinPrincipal getDolphinPrincipal() {
        return stub.getDolphinPrincipal();
    }
    
    public static String getProviderURL() {
        return stub.getProviderURL();
    }
    
    public static UserModel getUserModel() {
        return stub.getUserModel();
    }
    
    public static void setUserModel(UserModel value) {
        stub.setUserModel(value);
    }
    
    public static boolean isReadOnly() {
        String licenseCode = stub.getUserModel().getLicenseModel().getLicense();
        String userId = stub.getUserModel().getUserId();
        return ( licenseCode.equals("doctor") || userId.equals("lasmanager") ) ? false : true;
    }
    
    public static String getUserId() {
        return stub.getUserId();
    }
    
    public static String getFacilityId() {
        return stub.getFacilityId();
    }
    
    public static String getOrcaVersion() {
        return stub.getOrcaVersion();
    }
    
    public static String getJMARICode() {
        return stub.getJMARICode();
    }
    
    public static String getHostAddress() {
        return stub.getHostAddress();
    }
    
    public static int getHostPort() {
        return stub.getHostPort();
    }
    
    //
    // CLAIM
    //
    
    /**
     * �f�Ís�ׂ̑��M���s�����ǂ�����Ԃ��B
     * @return �s���Ƃ� true
     */
    public static boolean getSendClaim() {
        return stub.getSendClaim();
    }
    
    /**
     * �ۑ����ɑ��M���s�����ǂ�����Ԃ��B
     * @return �s���Ƃ� true
     */
    public static boolean getSendClaimSave() {
        return stub.getSendClaimSave();
    }
    
    /**
     * ���ۑ����ɐf�Ís�ׂ̑��M���s�����ǂ�����Ԃ��B
     * @return �s���Ƃ� true
     */
    public static boolean getSendClaimTmp() {
        return stub.getSendClaimTmp();
    }
    
    /**
     * �C�����ɐf�Ís�ׂ̑��M���s�����ǂ�����Ԃ��B
     * @return �s���Ƃ� true
     */
    public static boolean getSendClaimModify() {
        return stub.getSendClaimModify();
    }
    
    /**
     * �a���̑��M���s�����ǂ�����Ԃ��B
     * @return �s���Ƃ� true
     */
    public static boolean getSendDiagnosis() {
        return stub.getSendDiagnosis();
    }
    
    /**
     * CLAIM �̃z�X�g����Ԃ��B
     * @return return CLAIM �̃z�X�g��
     */
    public static String getClaimHostName() {
        return stub.getClaimHostName();
    }
    
    /**
     * ��t������M���邩�ǂ�����Ԃ��B
     * @return �s���Ƃ� true
     */
    public static boolean getUseAsPVTServer() {
        return stub.getUseAsPVTServer();
    }
    
    public static boolean isClaim01() {
        return stub.isClaim01();
    }
    
    /**
     * CLAIM �z�X�g�� IP �A�h���X��Ԃ��B
     * @return CLAIM �z�X�g�� IP �A�h���X
     */
    public static String getClaimAddress() {
        return stub.getClaimAddress();
    }
    
    /**
     * CLAIM �z�X�g�̐f�Ís�ב��M��|�[�g�ԍ���Ԃ��B
     * @return CLAIM �z�X�g���̐f�Ís�ב��M��|�[�g�ԍ�
     */
    public static int getClaimPort() {
        return stub.getClaimPort();
    }
    
    /**
     * CLAIM ���M����XML�G���R�[�f�B���O��Ԃ��B
     * @return CLAIM �G���R�[�f�B���O
     */
    public static String getClaimEncoding() {
        return stub.getClaimEncoding();
    }
    
    public static String getProxyHost() {
        return stub.getProxyHost();
    }
    
    public static int getProxyPort() {
        return stub.getProxyPort();
    }
    
    public static long getLastModify() {
        return stub.getLastModify();
    }
    
    public static void setLastModify(long val) {
        stub.setLastModify(val);
    }
    
    /**
     * ProjectFactory��Ԃ��B
     * @return Project���ɈقȂ镔���̏��𐶐����邽�߂�Factory
     */
    public static AbstractProjectFactory getProjectFactory() {
        return AbstractProjectFactory.getProjectFactory(stub.getName());
    }
    
    /**
     * �n��A�g�ɎQ�����邩�ǂ�����Ԃ��B
     * @return �Q�����鎞 true
     */
    public static boolean getJoinAreaNetwork() {
        return stub.getJoinAreaNetwork();
    }
    
    /**
     * �n��A�g�p�̎{��ID��Ԃ��B
     * @return �n��A�g�Ŏg�p����{��ID
     */
    public static String getAreaNetworkFacilityId() {
        return stub.getAreaNetworkFacilityId();
    }
    
    /**
     * �n��A�g�p��CreatorID��Ԃ��B
     * @return �n��A�g�Ŏg�p����CreatorId
     */
    public static String getAreaNetworkCreatorId() {
        return stub.getAreaNetworkCreatorId();
    }
        
    /**
     * �n��A�g�p�̊���MasterId��Ԃ��B
     * @return �n��A�g�Ŏg�p���銳��MasterId
     */
    public static ID getMasterId(String pid) {
        String fid = stub.getAreaNetworkFacilityId();
        return getProjectFactory().createMasterId(pid, fid);
    }
    
    /**
     * CLAIM���M�Ɏg�p���銳��MasterId��Ԃ��B
     * �n��A�g���[���ƈقȂ邽�߁B
     */
    public static ID getClaimMasterId(String pid) {
        return new ID(pid, "facility", "MML0024");
    }
    
    public static Object createSaveDialog(Window parent, SaveParams params) {
        return getProjectFactory().createSaveDialog(parent, params);
    }
    
    /**
     * CSGW(Client Side Gate Way)�ւ̃p�X��Ԃ��B
     */
    public static String getCSGWPath() {
        String uploader = getUploaderIPAddress();
        String share = getUploadShareDirectory();
        String id = stub.getAreaNetworkFacilityId()!= null 
                    ? stub.getAreaNetworkFacilityId() 
                    : stub.getUserModel().getFacilityModel().getFacilityId();
        return getProjectFactory().createCSGWPath(uploader, share, id);
    }
    
    // HOT
    public static boolean getSendMML() {
        return stub.getSendMML();
    }
    
    public static String getMMLVersion() {
        return stub.getMMLVersion();
    }
    
    public static String getMMLEncoding() {
        return stub.getMMLEncoding();
    }
    
    public static String getUploaderIPAddress() {
        return stub.getUploaderIPAddress();
    }
    
    public static String getUploadShareDirectory() {
        return stub.getUploadShareDirectory();
    }
    
    public static void exportSubtree(OutputStream os) {
        stub.exportSubtree(os);
    }
    
    public static void clear() {
        stub.clear();
    }
}