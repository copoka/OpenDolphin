/*
 * MmlMessageEvent.java
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
package open.dolphin.client;

import java.util.List;
import open.dolphin.infomodel.SchemaModel;


/**
 * MML �C���X�^���X��ʒm����C�x���g�N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class MmlMessageEvent extends java.util.EventObject {
    
    private static final long serialVersionUID = -5163032502414937817L;
    
    private String patientId;
    private String patientName;
    private String patientSex;
    private String title;
    private String groupId;
    private String mmlInstance;
    private List<SchemaModel> schemas;
    private int number;
    private String content;
    private String confirmDate;
    
    /** Creates new MmlPackage */
    public MmlMessageEvent(Object source) {
        super(source);
    }
    
    public String getPatientId() {
        return patientId;
    }
    
    public void setPatientId(String val) {
        patientId = val;
    }
    
    public String getPatientName() {
        return patientName;
    }
    
    public void setPatientName(String val) {
        patientName = val;
    }
    
    public String getPatientSex() {
        return patientSex;
    }
    
    public void setPatientSex(String val) {
        patientSex = val;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String val) {
        title = val;
    }
    
    public String getGroupId() {
        return groupId;
    }
    
    public void setGroupId(String val) {
        groupId = val;
    }
    
    public String getMmlInstance() {
        return mmlInstance;
    }
    
    public void setMmlInstance(String val) {
        mmlInstance = val;
    }
    
    public List<SchemaModel> getSchema() {
        return schemas;
    }
    
    public void setSchema(List<SchemaModel> val) {
        schemas = val;
    }
    
    public int getNumber() {
        return number;
    }
    
    public void setNumber(int val) {
        number = val;
    }
    
    public String getContentInfo() {
        return content;
    }
    
    public void setContentInfo(String val) {
        content = val;
    }
    
    public String getConfirmDate() {
        return confirmDate;
    }
    
    public void setConfirmDate(String val) {
        confirmDate = val;
    }
}