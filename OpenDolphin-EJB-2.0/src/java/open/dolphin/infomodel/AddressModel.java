package open.dolphin.infomodel;

/**
 * AddressModel
 * 
 * 
 * @author Minagawa,kazushi
 */
public class AddressModel extends InfoModel {

    private String addressType;
    private String addressTypeDesc;
    private String addressTypeCodeSys;
    private String countryCode;
    private String zipCode;
    private String address;

    /**
     * ���R�[�h��ݒ肷��B
     * 
     * @param countryCode
     *            ���R�[�h
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * ���R�[�h��Ԃ��B
     * 
     * @return ���R�[�h
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * �X�֔ԍ���ݒ肷��B
     * 
     * @param zipCode
     *            �X�֔ԍ�
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    /**
     * �X�֔ԍ���Ԃ��B
     * 
     * @return �X�֔ԍ�
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * �Z����ݒ肷��B
     * 
     * @param address
     *            �Z��
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * �Z����Ԃ��B
     * 
     * @return �Z��
     */
    public String getAddress() {
        return address;
    }

    /**
     * �Z���敪��ݒ肷��B
     * 
     * @param addressType
     *            �Z���敪
     */
    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    /**
     * �Z���敪��Ԃ��B
     * 
     * @return �Z���敪
     */
    public String getAddressType() {
        return addressType;
    }

    /**
     * �Z���敪������ݒ肷��B
     * 
     * @param addressTypeDesc
     *            �Z���敪����
     */
    public void setAddressTypeDesc(String addressTypeDesc) {
        this.addressTypeDesc = addressTypeDesc;
    }

    /**
     * �Z���敪������Ԃ��B
     * 
     * @return �Z���敪����
     */
    public String getAddressTypeDesc() {
        return addressTypeDesc;
    }

    /**
     * �Z���敪�̌n��ݒ肷��B
     * 
     * @param addressTypeCodeSys
     *            �Z���敪�̌n
     */
    public void setAddressTypeCodeSys(String addressTypeCodeSys) {
        this.addressTypeCodeSys = addressTypeCodeSys;
    }

    /**
     * �Z���敪�̌n��Ԃ��B
     * 
     * @return �Z���敪�̌n
     */
    public String getAddressTypeCodeSys() {
        return addressTypeCodeSys;
    }
}
