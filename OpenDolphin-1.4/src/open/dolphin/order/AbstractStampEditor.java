package open.dolphin.order;

import open.dolphin.infomodel.ClaimConst;
import java.awt.Window;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import open.dolphin.client.ClientContext;
import open.dolphin.dao.SqlDaoFactory;
import open.dolphin.dao.SqlMasterDao;
import open.dolphin.infomodel.ClaimItem;
import open.dolphin.infomodel.TensuMaster;
import open.dolphin.project.Project;
import open.dolphin.util.ZenkakuUtils;

/**
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public abstract class AbstractStampEditor {

    public static final String VALUE_PROP = "value";
    public static final String VALIDA_DATA_PROP = "validData";
    public static final String EDIT_END_PROP = "editEnd";

    protected static final String DEFAULT_NUMBER = "1";

    protected static final String DEFAULT_STAMP_NAME     = "�V�K�X�^���v";
    protected static final String FROM_EDITOR_STAMP_NAME = "�G�f�B�^����";

    protected static final String[] MED_COST_FLGAS = {"�p","��","�s","","","","","��","�s"};
    protected static final String[] TOOL_COST_FLGAS = {"�p","��","�s","","","%��","","","","��"};
    protected static final String[] TREAT_COST_FLGAS = {"�p","��","","+�_","�s","%��","%��","��","-�_"};
    protected static final String[] IN_OUT_FLAGS = {"���O","��","�O"};
    protected static final String[] HOSPITAL_CLINIC_FLAGS = {"�a�f","�a","�f"};
    protected static final String[] OLD_FLAGS = {"�ИV","��","�V"};

    protected static final String ADMIN_MARK = "[�p�@] ";
    protected static final String REG_ADMIN_MARK = "\\[�p�@\\] ";

    protected static final int START_NUM_ROWS = 20;

    // ORCA �L�������p��DF
    protected static SimpleDateFormat effectiveFormat = new SimpleDateFormat("yyyyMMdd");

    // �h���t�B���̃I�[�_����p�̖��O
    protected String orderName;

    // ClaimBundle �ɐݒ肷�� �f�Ís�׋敪 400,500,600 .. etc
    protected String classCode;

    // �f�Ís�׋敪��`�̃e�[�u��ID == Claim007
    protected String classCodeId    = "Claim007";

    // ClaimItem (����) �̎�ʂ��`���Ă���e�[�u��ID = Claim003
    protected String subclassCodeId = "Claom003";

    // ���̃G�f�B�^�̃G���e�B�e�B
    protected String entity;
    
    // ���̃G�f�B�^�őg���킹���\�ȓ_���}�X�^���ڂ̐��K�\��
    protected String passRegExp;

    // ���̃G�f�B�^�̐f�搳�K�\��
    protected String shinkuRegExp;

    // ���̃G�f�B�^�̏��
    private String info;

    protected String implied007;

    protected JTextField searchTextField;

    protected JTextField countField;

    // �ʒm�p�̑����T�|�[�g
    protected PropertyChangeSupport boundSupport;

    // �}�X�^�����p�֗̕��I�u�W�F�N�g
    protected SqlMasterDao dao;
    protected String now;

    // �Z�b�g�̗L�����𐧌䂷��֗��t���O
    protected Boolean setIsEmpty;
    protected Boolean setIsValid;

    // StampEditor ����N�����ꂽ�� true
    // StampMaker ����N�����ꂽ���� false
    private Boolean fromStampEditor;

    /**
     * Entity ����}�X�^�����ɕK�v�Ȑ��K�\���𐶐�����B
     * @param entity �G���e�B�e�B
     * @return ���K�\�����i�[���� Hashtable
     */
    public static Hashtable<String, String> getEditorSpec(String entity) {

        Hashtable<String, String> ht = new Hashtable<String, String>(10, 0.75f);
        String orderName = null;
        String passRegExp = null;
        String shinkuRegExp = null;
        String implied007 = null;
        String info = null;

        if (entity.equals("baseChargeOrder")) {

            orderName = "�f�f��";
            passRegExp = "[�肻]";
            shinkuRegExp = "^(11|12)";
            info = "�f�f���i�f��=110-120�j";


        } else if (entity.equals("instractionChargeOrder")) {

            orderName = "�w���E�ݑ�";
            passRegExp = "[�肻]";
            shinkuRegExp = "^(13|14)";
            info = "�w���E�ݑ�i�f��=130-140�j";


        } else if (entity.equals("medOrder")) {

            orderName = "�� ��";
            passRegExp = "[��p�ނ�]";              // ��܁A�p�@�A�ޗ��A���̑�(�ی��K�p�O���i�j
            info = "�� ��";

        } else if (entity.equals("injectionOrder")) {

            orderName = "�� ��";
            passRegExp = "[�肻����]";              // ��Z�A���̑��A���˖�A�ޗ�
            shinkuRegExp = "^3";
            info = "�� �ˁi�f��=300�j";


        } else if (entity.equals("treatmentOrder")) {

            orderName = "�� �u";
            passRegExp = "[�肻���]";              // ��Z�A���̑��A��܁A�ޗ�
            shinkuRegExp = "^4";
            implied007 = "400";
            info = "�� �u�i�f��=400�j";


        } else if (entity.equals("surgeryOrder")) {

            orderName = "�� �p";
            passRegExp = "[�肻���]";              // ��Z�A���̑��A��܁A�ޗ�
            shinkuRegExp = "^5";
            info = "�� �p�i�f��=500�j";


        } else if (entity.equals("bacteriaOrder")) {

            orderName = "�׋ی���";
            passRegExp = "[�肻���]";              // ��Z�A���̑��A��܁A�ޗ�
            shinkuRegExp = "^6";
            implied007 = "600";
            info = "�׋ی����i�f��=600�j";

        } else if (entity.equals("physiologyOrder")) {

            orderName = "�����E����������";
            passRegExp = "[�肻���]";              // ��Z�A���̑��A��܁A�ޗ�
            shinkuRegExp = "^6";
            implied007 = "600";
            info = "�����E�����������i�f��=600�j";


        } else if (entity.equals("testOrder")) {

            orderName = "���̌���";
            passRegExp = "[�肻���]";              // ��Z�A���̑��A��܁A�ޗ�
            shinkuRegExp = "^6";
            implied007 = "600";
            info = "���̌����i�f��=600�j";


        } else if (entity.equals("radiologyOrder")) {

            orderName = "���ː�";
            passRegExp = "[�肻��ޕ�]";            // ��Z�A���̑��A��܁A�ޗ��A����
            shinkuRegExp = "^7";
            implied007 = "700";
            info = "���ː��i�f��=700�j";


        }   else if (entity.equals("otherOrder")) {

            orderName = "���̑�";
            passRegExp = "[�肻���]";              // ��Z�A���̑��A��܁A�ޗ�
            shinkuRegExp = "^8";
            implied007 = "800";
            info = "���̑��i�f��=800�j";


        } else if (entity.equals("generalOrder")) {

            orderName = "�� �p";
            passRegExp = "[�肻��ޗp��]";        // ��Z�A���̑��A��܁A�ޗ��A�p�@�A����
            shinkuRegExp = "\\d";
            info = "�� �p�i�f��=100-999�j";

        } else if (entity.equals("diagnosis")) {

            orderName = "���a��";
            passRegExp = "[�肻��ޗp��]";
        }

        ht.put("orderName", orderName);

        if (passRegExp!=null) {
            ht.put("passRegExp", passRegExp);
        }
        
        if (shinkuRegExp!=null) {
            ht.put("shinkuRegExp", shinkuRegExp);
        }

        if (info!=null) {
            ht.put("info", info);
        }

        if (implied007!=null) {
            ht.put("implied007", implied007);
        }

        return ht;
    }

    protected static boolean isCode(String text) {
        boolean maybe = true;
        if (text != null) {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                int type = Character.getType(c);
                if (type == Character.DECIMAL_DIGIT_NUMBER) {
                    continue;
                } else {
                    maybe = false;
                    break;
                }
            }
            return maybe;
        }
        return false;
    }

    public abstract JPanel getView();

    public abstract Object getValue();

    public abstract void setValue(Object theStamp);

    public void dispose() {

        if (searchTextField != null) {
            searchTextField.setText("");
        }

        if (countField != null) {
            countField.setText("");
        }
    }

    protected void checkValidation() {
        
        if (boundSupport != null) {
            boundSupport.firePropertyChange(VALIDA_DATA_PROP, new Boolean(!setIsValid), new Boolean(setIsValid));
        }
    }
    
    /**
     * �Z�b�g�e�[�u����MasterItem����ClaimItem�𐶐�����B
     * @param masterItem �Z�b�g�e�[�u���̍s�I�u�W�F�N�g
     * @return ClaimItem
     */
    protected ClaimItem masterToClaimItem(MasterItem masterItem) {

        ClaimItem ret = new ClaimItem();

        // �R�[�h
        ret.setCode(masterItem.getCode());

        // ����
        ret.setName(masterItem.getName());

        // subclassCode(��Z|���|�ޗ�|����|�p�@|���̑�)
        ret.setClassCode(String.valueOf(masterItem.getClassCode()));

        // Claim003
        ret.setClassCodeSystem(ClaimConst.SUBCLASS_CODE_ID);

        // ����
        String number = trimToNullIfEmpty(masterItem.getNumber());
        if (number != null) {
            number = ZenkakuUtils.toHalfNumber(number);
            ret.setNumber(number);
            ret.setNumberCode(getNumberCode(masterItem.getClassCode()));
            ret.setNumberCodeSystem(ClaimConst.NUMBER_CODE_ID);
        }

        // �P��
        String unit = trimToNullIfEmpty(masterItem.getUnit());
        if (unit != null) {
            ret.setUnit(unit);
        }

        return ret;
    }

    /**
     * ClaimItem���Z�b�g�e�[�u���̍sMasterItem�֕ϊ�����B
     * @param claimItem ClaimItem
     * @return  MasterItem
     */
    protected MasterItem claimToMasterItem(ClaimItem claimItem) {

        MasterItem ret = new MasterItem();

        // Code
        ret.setCode(claimItem.getCode());

        // Name
        ret.setName(claimItem.getName());

        // ��Z�E�ޗ��E��i�̃t���O
        String test = trimToNullIfEmpty(claimItem.getClassCode());
        if (test != null ) {
            ret.setClassCode(Integer.parseInt(test));
        }

        // ����
        test = trimToNullIfEmpty(claimItem.getNumber());
        if (test != null) {
            test = ZenkakuUtils.toHalfNumber(test.trim());
            ret.setNumber(test);
        }

        // �P��
        test = trimToNullIfEmpty(claimItem.getUnit());
        if (test != null) {
            ret.setUnit(test.trim());
        }
        
        return ret;
    }

    /**
     * �_���}�X�^����MasterItem�𐶐�����B
     * @param tm �_���}�X�^
     * @return MasterItem
     */
    protected MasterItem tensuToMasterItem(TensuMaster tm) {

        MasterItem ret = new MasterItem();

        // code
        ret.setCode(tm.getSrycd());

        // name
        ret.setName(tm.getName());

        // unit
        ret.setUnit(trimToNullIfEmpty(tm.getTaniname()));

        // ClaimInterface �́@��Z�A��܁A��ނ̕�
        // �y�ѐf�Ís�׋敪�i�f��j��ݒ肷��
        // 0: ��Z  1: �ޗ�  2: ��� 3: �p�@ 4:���� 5:���̑�
        String test = tm.getSlot();

        if (test.equals(ClaimConst.SLOT_SYUGI)) {

            // ��Z
            ret.setClassCode(ClaimConst.SYUGI);

            // �f�Ís�׋敪 ��Z�Őݒ肵�Ă���
            ret.setClaimClassCode(tm.getSrysyukbn());

            // ���������Đ��ʂ����邩��...
            if (ret.getUnit()!=null) {
                ret.setNumber(DEFAULT_NUMBER);
            }

        } else if (Pattern.compile(ClaimConst.SLOT_MEDICINE).matcher(test).find()) {

            // ���
            ret.setClassCode(ClaimConst.YAKUZAI);

            String inputNum = DEFAULT_NUMBER;

            if (ret.getUnit().equals(ClaimConst.UNIT_T)) {
                inputNum = Project.getPreferences().get("defaultZyozaiNum", "3");

            } else if (ret.getUnit().equals(ClaimConst.UNIT_G)) {
                inputNum = Project.getPreferences().get("defaultSanyakuNum", "1.0");

            } else if (ret.getUnit().equals(ClaimConst.UNIT_ML)) {
                inputNum = Project.getPreferences().get("defaultMizuyakuNum", "1");

            } else if (ret.getUnit().equals(ClaimConst.UNIT_CAPSULE)) {
                inputNum = Project.getPreferences().get("defaultKapuselNum", "1");
            }

            ret.setNumber(inputNum);


        } else if (test.equals(ClaimConst.SLOT_ZAIRYO)) {
            // �ޗ�
            ret.setClassCode(ClaimConst.ZAIRYO);
            ret.setNumber(DEFAULT_NUMBER);

        } else if (test.equals(ClaimConst.SLOT_YOHO)) {
            // �p�@
            ret.setClassCode(ClaimConst.ADMIN);
            ret.setName(ADMIN_MARK + tm.getName());
            ret.setDummy("X");
            ret.setBundleNumber(Project.getPreferences().get("defaultRpNum", "1"));

        } else if (test.equals(ClaimConst.SLOT_BUI)) {
            // ����
            ret.setClassCode(ClaimConst.BUI);

        } else if (test.equals(ClaimConst.SLOT_OTHER)) {
            // ���̑�
            ret.setClassCode(ClaimConst.OTHER);
            if (ret.getUnit()!=null) {
                ret.setNumber(DEFAULT_NUMBER);
            }
        }

        return ret;
    }

    protected String trimToNullIfEmpty(String test) {

        if (test == null) {
            return null;
        }

        test = test.trim();

        return test.equals("") ? null : test;
    }

    protected String getClaim007Code(String code) {

        if (code == null) {
            return null;
        }

        if (code.equals(ClaimConst.INJECTION_311)) {
            return ClaimConst.INJECTION_310;

        } else if (code.equals(ClaimConst.INJECTION_321)) {
            return ClaimConst.INJECTION_320;

        } else if (code.equals(ClaimConst.INJECTION_331)) {
            return ClaimConst.INJECTION_330;

        } else {
            // ���ˈȊO�̃P�[�X
            return code;
        }
    }

    /**
     * �}�X�^�[�����őI�����ꂽ�_���I�u�W�F�N�g���Z�b�g�e�[�u���֒ǉ�����B
     * @param tm �_���}�X�^
     */
    protected abstract void addSelectedTensu(TensuMaster tm);

    /**
     * Returns Claim004 Number Code 21 �ޗ��� when subclassCode = 1 11
     * ��ܓ��^�ʁi�P��jwhen subclassCode = 2
     */
    protected String getNumberCode(int subclassCode) {
        return (subclassCode == 1) ? ClaimConst.ZAIRYO_KOSU : ClaimConst.YAKUZAI_TOYORYO; // �ޗ��� : ��ܓ��^�ʂP��
        // 2010 ORAC �̎���
        //return ClaimConst.YAKUZAI_TOYORYO;
    }

    protected void alertIpAddress() {

        String msg0 = "���Z�R����IP�A�h���X���ݒ肳��Ă��Ȃ����߁A�}�X�^�[�������ł��܂���B";
        String msg1 = "���ݒ胁�j���[���烌�Z�R����IP�A�h���X��ݒ肵�Ă��������B";
        Object message = new String[]{msg0, msg1};
        Window parent = SwingUtilities.getWindowAncestor(getView());
        String title = ClientContext.getFrameTitle("�}�X�^����");
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    protected void alertSearchError(String err) {

        String msg0 = "�}�X�^�[�������ł��܂���B�A�N�Z�X��������Ă��邩���m�F���������B";
        String msg1 = err;

        Object message = new String[]{msg0, msg1};
        Window parent = SwingUtilities.getWindowAncestor(getView());
        String title = ClientContext.getFrameTitle("�}�X�^����");
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    protected SqlMasterDao getDao() {

        if (dao == null) {
            dao = (SqlMasterDao) SqlDaoFactory.create("dao.master");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            now = sdf.format(new Date());
        }

        return dao;
    }

    protected Boolean ipOk() {

        // CLAIM(Master) Address ���ݒ肳��Ă��Ȃ��ꍇ�Ɍx������
        String address = Project.getClaimAddress();
        if (address == null || address.equals("")) {
            alertIpAddress();
            return false;
        }

        return true;
    }

    protected abstract void search(final String text);

    protected abstract void clear();

    protected abstract void initComponents();

    public void addPropertyChangeListener(String prop, PropertyChangeListener listener) {
        if (boundSupport == null) {
            boundSupport = new PropertyChangeSupport(this);
        }
        boundSupport.addPropertyChangeListener(prop, listener);
    }


    public void remopvePropertyChangeListener(String prop, PropertyChangeListener listener) {
        boundSupport.removePropertyChangeListener(prop, listener);
    }

    /**
     * @return the passRegExp
     */
    public String getPassRegExp() {
        return passRegExp;
    }

    /**
     * @param passRegExp the passRegExp to set
     */
    public void setPassRegExp(String passRegExp) {
        this.passRegExp = passRegExp;
    }

    /**
     * @return the shinkuRegExp
     */
    public String getShinkuRegExp() {
        return shinkuRegExp;
    }

    /**
     * @param shinkuRegExp the shinkuRegExp to set
     */
    public void setShinkuRegExp(String shinkuRegExp) {
        this.shinkuRegExp = shinkuRegExp;
    }

    /**
     * @return the orderName
     */
    public String getOrderName() {
        return orderName;
    }

    /**
     * @param orderName the orderName to set
     */
    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    /**
     * @return the classCode
     */
    public String getClassCode() {
        return classCode;
    }

    /**
     * @param classCode the classCode to set
     */
    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    /**
     * @return the classCodeId
     */
    public String getClassCodeId() {
        return classCodeId;
    }

    /**
     * @param classCodeId the classCodeId to set
     */
    public void setClassCodeId(String classCodeId) {
        this.classCodeId = classCodeId;
    }

    /**
     * @return the subclassCodeId
     */
    public String getSubclassCodeId() {
        return subclassCodeId;
    }

    /**
     * @param subclassCodeId the subclassCodeId to set
     */
    public void setSubclassCodeId(String subclassCodeId) {
        this.subclassCodeId = subclassCodeId;
    }

    /**
     * @return the entity
     */
    public String getEntity() {
        return entity;
    }

    /**
     * @param entity the entity to set
     */
    public void setEntity(String entity) {
        this.entity = entity;
    }

    /**
     * @return the info
     */
    public String getInfo() {
        return info;
    }

    /**
     * @param info the info to set
     */
    public void setInfo(String info) {
        this.info = info;
    }

    public String getImplied007() {
        return implied007;
    }

    public void setImplied007(String default007) {
        this.implied007 = default007;
    }

    /**
     * @return the fromStampEditor
     */
    public Boolean getFromStampEditor() {
        return fromStampEditor;
    }

    /**
     * @param fromStampEditor the fromStampEditor to set
     */
    public void setFromStampEditor(Boolean fromStampEditor) {
        this.fromStampEditor = fromStampEditor;
    }

    public AbstractStampEditor() {
        initComponents();
    }

    public AbstractStampEditor(String entity) {
        this(entity,true);
    }

    public AbstractStampEditor(String entity, boolean mode) {

        Hashtable<String, String> ht = AbstractStampEditor.getEditorSpec(entity);

        this.setEntity(entity);
        this.setOrderName(ht.get("orderName"));

        if (ht.get("passRegExp")!=null) {
            this.setPassRegExp(ht.get("passRegExp"));
        }
        
        if (ht.get("shinkuRegExp")!=null) {
            this.setShinkuRegExp(ht.get("shinkuRegExp"));
        }

        if (ht.get("info")!=null) {
            this.setInfo(ht.get("info"));
        }

        if (ht.get("implied007")!=null) {
            this.setImplied007(ht.get("implied007"));
        }

        setFromStampEditor(mode);

        initComponents();
    }
}
