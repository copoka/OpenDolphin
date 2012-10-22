/*
 * Kumamoto.java
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
import open.dolphin.dao.*;
import open.dolphin.infomodel.ID;

/**
 * �Ђ����h�ɌŗL�̃I�u�W�F�N�g�𐶐�����t�@�N�g���N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc. 
 */
public final class KumamotoFactory extends DolphinFactory {

    private KumamotoDao dao;
    
    /** Creates new Project */
    public KumamotoFactory() {
        super();
    }
  
    // Since 2003/08/01 HOT 02b
    public ID createMasterId(String pid, String facilityId) {
       
        ID id = null;
        
        if (dao == null) {
            dao = (KumamotoDao)SqlDaoFactory.create(this, "dao.kumamoto");
        }
        String localId = dao.fetchLocalId(pid);
        
        if (localId == null) {

            /*JOptionPane.showMessageDialog(null,
                             (String)"���҂̒n��ID���o�^����Ă��܂���B\n�o�^��ɂ�����x�ۑ������s���Ă��������B",
                             "Dolphin: �n��T�[�o���M",
                             JOptionPane.INFORMATION_MESSAGE);*/
        }
        else {
            id = new ID(localId, "local", "MML0024");
        }
        return id;        
    }
    
    public Object createSaveDialog(Frame parent, SaveParams params) {
        
        SaveDialog sd = new SaveDialog(parent);
        //params.setPrintCount(1);              // �������
        params.setAllowPatientRef(true);        // ���҂̎Q�Ƃ�����
        params.setAllowClinicRef(true);         // �f�×����̂����Ë@�ւ֎Q�Ƃ�����
        sd.setValue(params);
        return sd;
    }
    
	public Object createAboutDialog() {
		String title = "�A�o�E�g-" + ClientContext.getString("application.title");
		return new AboutDialog(null, title, "splash.jpg");
	}    
}