/*
 * AllergyItem.java
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
package open.dolphin.infomodel;

/**
 * AllergyModel
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public class AllergyModel extends InfoModel implements Comparable {
    
    private static final long serialVersionUID = -6327488237646390391L;
    
    private long observationId;
    
    // �v��
    private String factor;
    
    // �������x
    private String severity;
    
    // �R�[�h�̌n
    private String severityTableId;
    
    // �����
    private String identifiedDate;
    
    // ����
    private String memo;
    
    public String getFactor() {
        return factor;
    }
    
    public void setFactor(String factor) {
        this.factor = factor;
    }
    
    public String getIdentifiedDate() {
        return identifiedDate;
    }
    
    public void setIdentifiedDate(String identifiedDate) {
        this.identifiedDate = identifiedDate;
    }
    
    public String getMemo() {
        return memo;
    }
    
    public void setMemo(String memo) {
        this.memo = memo;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    public String getSeverityTableId() {
        return severityTableId;
    }
    
    public void setSeverityTableId(String severityTableId) {
        this.severityTableId = severityTableId;
    }
    
    /**
     * ������Ŕ�r����B
     * @param other ��r�ΏۃI�u�W�F�N�g
     * @return ��r�l
     */
    public int compareTo(Object other) {
        if (other != null && getClass() == other.getClass()) {
            String val1 = getIdentifiedDate();
            String val2 = ((AllergyModel)other).getIdentifiedDate();
            return val1.compareTo(val2);
        }
        return 1;
    }
    
    public long getObservationId() {
        return observationId;
    }
    
    public void setObservationId(long observationId) {
        this.observationId = observationId;
    }
}