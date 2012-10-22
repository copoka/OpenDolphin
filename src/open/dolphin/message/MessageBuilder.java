/*
 * ClaimMessageBuilder.java
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
package open.dolphin.message;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import open.dolphin.client.ClientContext;
import open.dolphin.client.IMessageBuilder;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

/**
 * DML �� �C�ӂ�Message �ɖ|�󂷂�N���X�B
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 *
 */
public class MessageBuilder implements IMessageBuilder {
    
    private static final String ENCODING = "SHIFT_JIS";
    
    /** �e���v���[�g�t�@�C�� */
    private String templateFile;
    
    /** �e���v���[�g�t�@�C���̃G���R�[�f�B���O */
    private String encoding = ENCODING;
    
    public String getTemplateFile() {
        return templateFile;
    }
    
    public void setTemplateFile(String templateFile) {
        this.templateFile = templateFile;
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    
    public String build(String dml) {
        
        String ret = null;
        
        try {
            // Document root ��Velocity �ϐ��ɃZ�b�g����
            SAXBuilder sbuilder = new SAXBuilder();
            Document root = sbuilder.build(new BufferedReader(new StringReader(dml)));
            VelocityContext context = ClientContext.getVelocityContext();
            context.put("root", root);
            
            // Merge ����
            StringWriter sw = new StringWriter();
            BufferedWriter bw = new BufferedWriter(sw);
            InputStream instream = ClientContext.getTemplateAsStream(templateFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(instream, encoding));
            Velocity.evaluate(context, bw, "MessageBuilder", reader);
            bw.flush();
            bw.close();
            
            ret = sw.toString();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return ret;
    }
    
    public String build(Object helper) {
        
        String ret = null;
        String name = helper.getClass().getName();
        int index = name.lastIndexOf('.');
        name = name.substring(index+1);
        StringBuffer sb = new StringBuffer();
        sb.append(name.substring(0,1).toLowerCase());
        sb.append(name.substring(1));
        name = sb.toString();
        
        try {
            VelocityContext context = ClientContext.getVelocityContext();
            context.put(name, helper);
            
            // ���̃X�^���v�̃e���v���[�g�t�@�C���𓾂�
            String templateFile = name + ".vm";
            
            // Merge ����
            StringWriter sw = new StringWriter();
            BufferedWriter bw = new BufferedWriter(sw);
            InputStream instream = ClientContext.getTemplateAsStream(templateFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(instream, "SHIFT_JIS"));
            Velocity.evaluate(context, bw, name, reader);
            bw.flush();
            bw.close();
            reader.close();
            
            ret = sw.toString();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return ret;
    }
}
