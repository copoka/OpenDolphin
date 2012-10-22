/*
 * BloodType.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
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
package open.dolphin.infomodel;


/**
 * BloodType �v�f�N���X
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class BloodType extends InfoModel {
   
    // ABO�����t�^
    String abo;           

    // RH�����t�^
    String rhod;     

    // ����
    String memo;          

    /**
     * �f�t�H���g�R���X�g���N�^
     */
    public BloodType() {
    }

    // Memo
    public String getMemo() {
        return memo;
    }
    
    public void setMemo(String value) {
        memo = value;
    }

    public boolean isValidModel() {
        
        return ( (getAbo() == null) || (getAbo().equals("")) )
                                                   ? false
                                                   : true;
    }

	public void setAbo(String abo) {
		this.abo = abo;
	}

	public String getAbo() {
		return abo;
	}

	public void setRhod(String rhod) {
		this.rhod = rhod;
	}

	public String getRhod() {
		return rhod;
	}
}