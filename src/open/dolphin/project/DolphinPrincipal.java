/*
 * Created on 2004/11/13
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package open.dolphin.project;

import java.io.Serializable;

/**
 * DolphinPrincipal
 * 
 * @author Minagawa, Kazushi
 */
public class DolphinPrincipal implements Serializable {
	
	private static final long serialVersionUID = -2401769499519969282L;
	
	private String userId;
	private String facilityId;
	
	/**
	 * ���[�UID��Ԃ��B
	 * @return ���[�UID
	 */
	public String getUserId() {
		return userId;
	}
	
	/**
	 * ���[�UID��ݒ肷��B
	 * @param uid ���[�UID
	 */
	public void setUserId(String uid) {
		this.userId = uid;
	}
	
	/**
	 * �{��ID��ݒ肷��B
	 * @param facilityId �{��ID
	 */
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	
	/**
	 * �{��ID��Ԃ��B
	 * @return �{��ID
	 */
	public String getFacilityId() {
		return facilityId;
	}

}