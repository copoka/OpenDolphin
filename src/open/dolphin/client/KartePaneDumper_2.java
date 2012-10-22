/*
 * KartePaneDumper.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2003-2006 Digital Globe, Inc. All rights reserved.
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

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.text.*;

import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.SchemaModel;
import org.apache.log4j.Logger;

/**
 * KartePane �� dumper
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class KartePaneDumper_2 {
    
    private static final String[] MATCHES = new String[] { "<", ">", "&", "'","\""};
    
    private static final String[] REPLACES = new String[] { "&lt;", "&gt;", "&amp;" ,"&apos;", "&quot;"};
    
    private ArrayList<ModuleModel> moduleList;
    
    private ArrayList<SchemaModel> schemaList;
    
    private String spec;
    
    private Logger logger;
    
    /** Creates a new instance of TextPaneDumpBuilder */
    public KartePaneDumper_2() {
        logger = ClientContext.getLogger("boot");
    }
    
    /**
     * �_���v���� Document �� XML ��`��Ԃ��B
     *
     * @return Document�̓��e�� XML �ŕ\��������
     */
    public String getSpec() {
        logger.debug(spec);
        return spec;
    }
    
    /**
     * �_���v���� Document�Ɋ܂܂�Ă��� ModuleModel��Ԃ��B
     *
     * @return
     */
    public ModuleModel[] getModule() {
        
        ModuleModel[] ret = null;
        
        if ((moduleList != null) && (moduleList.size() > 0)) {
            ret = moduleList.toArray(new ModuleModel[moduleList.size()]);
        }
        return ret;
    }
    
    /**
     * �_���v���� Document�Ɋ܂܂�Ă��� SchemaModel ��Ԃ��B
     *
     * @return
     */
    public SchemaModel[] getSchema() {
        
        SchemaModel[] schemas = null;
        
        if ((schemaList != null) && (schemaList.size() > 0)) {
            
            schemas = schemaList.toArray(new SchemaModel[schemaList.size()]);
        }
        return schemas;
    }
    
    /**
     * ������ Document ���_���v����B
     *
     * @param doc �_���v����h�L�������g
     */
    public void dump(DefaultStyledDocument doc) {
        
        StringWriter sw = new StringWriter();
        BufferedWriter writer = new BufferedWriter(sw);
        
        try {
            // ���[�g�v�f����ċA�I�Ƀ_���v����
            javax.swing.text.Element root = (javax.swing.text.Element) doc.getDefaultRootElement();
            writeElemnt(root, writer);
            
            // �o�̓o�b�t�@�[���t���b�V�����y�C����XML��`�𐶐�����
            writer.flush();
            writer.close();
            spec = sw.toString();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * �v�f���ċA�I�Ƀ_���v����B
     * @param element �v�f
     * @param writer	�o�̓��C�^�[
     * @throws IOException
     * @throws BadLocationException
     */
    private void writeElemnt(javax.swing.text.Element element, Writer writer)
    throws IOException, BadLocationException {
        
        // �v�f�̊J�n�y�яI���̃I�t�Z�b�g�l��ۑ�����
        int start = element.getStartOffset();
        int end = element.getEndOffset();
        logger.debug("start = " + start);
        logger.debug("end = " + end);
        
        // ���̃G�������g�̑����Z�b�g�𓾂�
        AttributeSet atts = element.getAttributes().copyAttributes();
        
        // �����l�̕�����\��
        String asString = "";
        
        // �����𒲂ׂ�
        if (atts != null) {
            
            StringBuilder retBuffer = new StringBuilder();
            
            // �S�Ă̑�����񋓂���
            Enumeration names = atts.getAttributeNames();
            
            while (names.hasMoreElements()) {
                
                // �����̖��O�𓾂�
                Object nextName = names.nextElement();
                
                if (nextName != StyleConstants.ResolveAttribute) {
                    
                    logger.debug("attribute name = " + nextName.toString());
                    
                    // $ename�͏��O����
                    if (nextName.toString().startsWith("$")) {
                        continue;
                    }
                    
                    // ����= �̌`����������
                    retBuffer.append(" ");
                    retBuffer.append(nextName);
                    retBuffer.append("=");
                    
                    // foreground �����̏ꍇ�͍č\�z�̍ۂɗ��p���₷���`�ɕ�������
                    if (nextName.toString().equals("foreground")) {
                        Color c = (Color) atts.getAttribute(StyleConstants.Foreground);
                        logger.debug("color = " + c.toString());
                        StringBuilder buf = new StringBuilder();
                        buf.append(String.valueOf(c.getRed()));
                        buf.append(",");
                        buf.append(String.valueOf(c.getGreen()));
                        buf.append(",");
                        buf.append(String.valueOf(c.getBlue()));
                        retBuffer.append(addQuote(buf.toString()));
                        
                    } else {
                        // �����Z�b�g���疼�O���L�[�ɂ��đ����I�u�W�F�N�g���擾����
                        Object attObject = atts.getAttribute(nextName);
                        logger.debug("attribute object = " + attObject.toString());
                        
                        if (attObject instanceof StampHolder) {
                            // �X�^���v�̏ꍇ
                            if (moduleList == null) {
                                moduleList = new ArrayList<ModuleModel>();
                            }
                            StampHolder sh = (StampHolder) attObject;
                            moduleList.add((ModuleModel) sh.getStamp());
                            String value = String.valueOf(moduleList.size() - 1); // �y�C���ɏo�����鏇�Ԃ����̑����̒l�Ƃ���
                            retBuffer.append(addQuote(value));
                            
                        } else if (attObject instanceof SchemaHolder) {
                            // �V���F�[�}�̏ꍇ
                            if (schemaList == null) {
                                schemaList = new ArrayList<SchemaModel>();
                            }
                            SchemaHolder ch = (SchemaHolder) attObject;
                            schemaList.add(ch.getSchema());
                            String value = String.valueOf(schemaList.size() - 1); // �y�C���ɏo�����鏇�Ԃ����̑����̒l�Ƃ���
                            retBuffer.append(addQuote(value));
                            
                        } else {
                            // ����ȊO�̑����ɂ��Ă͂��̂܂܋L�^����
                            retBuffer.append(addQuote(attObject.toString()));
                        }
                    }
                }
            }
            asString = retBuffer.toString();
        }
        
        // <�v�f�� start="xx" end="xx" + asString>
        writer.write("<");
        writer.write(element.getName());
        writer.write(" start=");
        writer.write(addQuote(start));
        writer.write(" end=");
        writer.write(addQuote(end));
        writer.write(asString);
        writer.write(">");
        
        // content�v�f�̏ꍇ�̓e�L�X�g�𒊏o����
        if (element.getName().equals("content")) {
            writer.write("<text>");
            int len = end - start;
            String text = element.getDocument().getText(start, len);
            logger.debug("text = " + text);
            
            // ����̕������u������
            for (int i = 0; i < REPLACES.length; i++) {
                text = text.replaceAll(MATCHES[i], REPLACES[i]);
            }
            writer.write(text);
            writer.write("</text>");

        }
        
        // �q�v�f�ɂ��čċA����
        int children = element.getElementCount();
        for (int i = 0; i < children; i++) {
            writeElemnt(element.getElement(i), writer);
        }
        
        // ���̑������I������
        // </������>
        writer.write("</");
        writer.write(element.getName());
        writer.write(">");
    }
    
    private String addQuote(String str) {
        StringBuilder buf = new StringBuilder();
        buf.append("\"");
        buf.append(str);
        buf.append("\"");
        return buf.toString();
    }
    
    private String addQuote(int str) {
        StringBuilder buf = new StringBuilder();
        buf.append("\"");
        buf.append(str);
        buf.append("\"");
        return buf.toString();
    }
}