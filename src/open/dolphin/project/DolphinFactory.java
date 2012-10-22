/*
 * DolphinFactory.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2003,2004 Digital Globe, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package open.dolphin.project;

import java.awt.*;

import open.dolphin.client.*;
import open.dolphin.infomodel.DocInfoModel;
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
    
    public Object createSaveDialog(Frame parent,SaveParams params) {
        SaveDialog sd = new SaveDialog(parent);
        params.setAllowPatientRef(false);    // ���҂̎Q��
        params.setAllowClinicRef(false);     // �f�×����̂����Ë@��
        sd.setValue(params);
        return sd;
    }
}