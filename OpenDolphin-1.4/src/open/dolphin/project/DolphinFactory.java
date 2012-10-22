package open.dolphin.project;

import java.awt.*;

import open.dolphin.client.*;
import open.dolphin.infomodel.ID;

/**
 * �v���W�F�N�g�Ɉˑ�����I�u�W�F�N�g�𐶐�����t�@�N�g���N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class DolphinFactory extends AbstractProjectFactory {
    
    protected String csgwPath;
    
    /** Creates new Project */
    public DolphinFactory() {
    }
    
    /**
     * �n��A�g�p�̊��҂�MasterId��Ԃ��B
     */
    public ID createMasterId(String pid, String facilityId) {
        return new ID(pid, "facility", facilityId);
    }
    
    /**
     * CSGW(Client Side Gate Way) �̃p�X��Ԃ��B
     * 
     * @param  uploaderAddress MML�A�b�v���[�_��IP Address
     * @param  share Samba ���L�f�B���N�g��
     * @param  facilityId �A�g�p�̎{��ID
     */
    public String createCSGWPath(String uploaderAddress, String share, String facilityId) {
        if (csgwPath == null) {
            if (ClientContext.isWin()) {
                StringBuilder sb = new StringBuilder();
                sb.append("\\\\");
                sb.append(uploaderAddress);
                sb.append("\\");
                sb.append(share);
                sb.append("\\");
                sb.append(facilityId);
                csgwPath = sb.toString();
            } else if (ClientContext.isMac()) {
                StringBuilder sb = new StringBuilder();
                sb.append("smb://");
                sb.append(uploaderAddress);
                sb.append("/");
                sb.append(share);
                sb.append("/");
                sb.append(facilityId);
                csgwPath = sb.toString();
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("/");
                sb.append(uploaderAddress);
                sb.append("/");
                sb.append(share);
                sb.append("/");
                sb.append(facilityId);
                csgwPath = sb.toString();
            }
        }
        return csgwPath;
    }
    
    public Object createAboutDialog() {
        String title = ClientContext.getFrameTitle("�A�o�E�g");
        return new AboutDialog(null, title, "splash.jpg");
    }
    
    public Object createSaveDialog(Window parent,SaveParams params) {
        SaveDialog sd = new SaveDialog(parent);
        params.setAllowPatientRef(false);    // ���҂̎Q��
        params.setAllowClinicRef(false);     // �f�×����̂����Ë@��
        sd.setValue(params);
        return sd;
    }
}