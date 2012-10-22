package open.dolphin.client;

import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import open.dolphin.infomodel.LetterModel;
import open.dolphin.infomodel.ModelUtils;
import open.dolphin.infomodel.TouTouLetter;

/**
 * �Љ��� PDF ���[�J�[�B
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class PDFLetterMaker {
    
    private static final String DOC_TITLE = "�f�Ï��񋟏�";
    private static final String GREETINGS = "���L�̊��҂�����Љ�v���܂��B�����f�̒��X�������肢�\���グ�܂��B";
    private static final String HEISEI_MIN_W3 = "HeiseiMin-W3";
    private static final String UNIJIS_UCS2_HW_H = "UniJIS-UCS2-HW-H";
    
    private static final int TOP_MARGIN = 75;
    private static final int LEFT_MARGIN = 75;
    private static final int BOTTOM_MARGIN = 75;
    private static final int RIGHT_MARGIN = 75;
    
    private static final int TITLE_FONT_SIZE = 14;
    private static final int BODY_FONT_SIZE = 12;
    
    private String documentDir;
    private String fileName;
    private TouTouLetter model;
    private int marginLeft = LEFT_MARGIN;
    private int marginRight = RIGHT_MARGIN;
    private int marginTop = TOP_MARGIN;
    private int marginBottom = BOTTOM_MARGIN;
    
    private BaseFont baseFont;
    private Font titleFont;
    private Font bodyFont;
    private int titleFontSize = TITLE_FONT_SIZE;
    private int bodyFontSize = BODY_FONT_SIZE;
    
    
    public boolean create() {
        
        boolean result = true;
        
        try {

            Document document = new Document(
                    PageSize.A4,
                    getMarginLeft(),
                    getMarginRight(),
                    getMarginTop(),
                    getMarginBottom());
            
            if (documentDir == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(System.getProperty("user.dir"));
                sb.append(File.separator);
                sb.append("pdf");
                setDocumentDir(sb.toString());
            }
            File dir = new File(getDocumentDir());
            dir.mkdir();
            
            String name = model.getPatientName();
            name = name.replaceAll(" ", "");
            name = name.replaceAll("�@", "");
            StringBuilder sb = new StringBuilder();
            sb.append("�Љ��-");
            sb.append(name);
            sb.append("�l-");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sb.append(sdf.format(new Date()));
            sb.append(".pdf");
            setFileName(sb.toString());
            
            sb = new StringBuilder();
            if (getDocumentDir() != null) {
                sb.append(getDocumentDir());
                sb.append(File.separator);
            }
            sb.append(getFileName());
            
            PdfWriter.getInstance(document, new FileOutputStream(sb.toString()));
            document.open();
            
            // Font
            baseFont = BaseFont.createFont(HEISEI_MIN_W3, UNIJIS_UCS2_HW_H, false);
            titleFont = new Font(baseFont, getTitleFontSize());
            bodyFont = new Font(baseFont, getBodyFontSize());
            
            // �^�C�g��
            Paragraph para = new Paragraph(DOC_TITLE, titleFont);
            para.setAlignment(Element.ALIGN_CENTER);
            document.add(para);
            
            // ���t
            String dateStr = getDateString(model.getConfirmed());
            para = new Paragraph(dateStr, bodyFont);
            para.setAlignment(Element.ALIGN_RIGHT);
            document.add(para);
            
            document.add(new Paragraph("�@"));
            
            // �Љ��a�@
            Paragraph para2 = new Paragraph(model.getConsultantHospital(), bodyFont);
            para2.setAlignment(Element.ALIGN_LEFT);
            document.add(para2);
            
            // �Љ��f�É�
            para2 = new Paragraph(model.getConsultantDept(), bodyFont);
            para2.setAlignment(Element.ALIGN_LEFT);
            document.add(para2);
            
            // �Љ���t
            sb = new StringBuilder();
            sb.append(model.getConsultantDoctor());
            sb.append(" �搶");
            para2 = new Paragraph(sb.toString(), bodyFont);
            para2.setAlignment(Element.ALIGN_LEFT);
            document.add(para2);
            
            // �Љ�a�@
            para2 = new Paragraph(model.getClientHospital(), bodyFont);
            para2.setAlignment(Element.ALIGN_RIGHT);
            document.add(para2);
            
//            // �Љ�f�É�
//            para2 = new Paragraph(model.getCl, bodyFont);
//            para2.setAlignment(Element.ALIGN_RIGHT);
//            document.add(para2);
            
            // �Љ��t
            sb = new StringBuilder();
            sb.append(model.getClientName());
            sb.append(" ��");
            para2 = new Paragraph(sb.toString(), bodyFont);
            para2.setAlignment(Element.ALIGN_RIGHT);
            document.add(para2);
            
            // �Љ�Z��
            para2 = new Paragraph(model.getClientAddress(), bodyFont);
            para2.setAlignment(Element.ALIGN_RIGHT);
            document.add(para2);
            
            // �Љ�d�b�ԍ�
            sb = new StringBuilder();
            sb.append("�d�b ");
            sb.append(model.getClientTelephone());
            para2 = new Paragraph(sb.toString(), bodyFont);
            para2.setAlignment(Element.ALIGN_RIGHT);
            document.add(para2);
            
            document.add(new Paragraph("�@"));
            
            // �Љ�A
            para2 = new Paragraph(GREETINGS, bodyFont);
            para2.setAlignment(Element.ALIGN_CENTER);
            document.add(para2);
            
            // ����
            Table pTable = new Table(4);
            pTable.setPadding(2);
            int width[] = new int[]{20, 60, 10, 10};
            pTable.setWidths(width);
            pTable.setWidth(100);     
            
            String birthday = getDateString(model.getPatientBirthday());
            String sexStr = getSexString(model.getPatientGender());
            pTable.addCell(new Phrase("���Ҏ���", bodyFont));
            pTable.addCell(new Phrase(model.getPatientName(), bodyFont));
            pTable.addCell(new Phrase("����", bodyFont));
            pTable.addCell(new Phrase(sexStr, bodyFont));
            pTable.addCell(new Phrase("���N����", bodyFont));
            sb = new StringBuilder();
            sb.append(birthday);
            sb.append(" (");
            sb.append(model.getPatientAge());
            sb.append(" ��)");
            Cell cell = new Cell(new Phrase(sb.toString(), bodyFont));
            cell.setColspan(3);
            pTable.addCell(cell);
            document.add(pTable);
            
            // �Љ����e
            Table lTable = new Table(2); //�e�[�u���E�I�u�W�F�N�g�̐���
            lTable.setPadding(2);
            width = new int[]{20, 80};
            lTable.setWidths(width); //�e�J�����̑傫����ݒ�i�p�[�Z���g�j
            lTable.setWidth(100);
            
            lTable.addCell(new Phrase("���a��", bodyFont));
            lTable.addCell(new Phrase(model.getDisease(), bodyFont));
            
            lTable.addCell(new Phrase("�Љ�ړI", bodyFont));
            lTable.addCell(new Phrase(model.getPurpose(), bodyFont));
            
            lTable.addCell(new Phrase("������\n�Ƒ���", bodyFont));
            cell = new Cell(new Phrase(model.getPastFamily(), bodyFont));
            lTable.addCell(cell);
            
            lTable.addCell(new Phrase("�Ǐ�o��\n��������\n���Ìo��", bodyFont));
            lTable.addCell(new Phrase(model.getClinicalCourse(), bodyFont));
            
            lTable.addCell(new Phrase("���݂̏���", bodyFont));
            lTable.addCell(new Phrase(model.getMedication(), bodyFont));
            
            lTable.addCell(new Phrase("�� �l", bodyFont));
            lTable.addCell(new Phrase(model.getRemarks(), bodyFont));
            
            document.add(lTable);
            
            document.close();
            
        } catch (IOException ex) {
            ClientContext.getBootLogger().warn(ex);
            result = false;
        } catch (DocumentException ex) {
            ClientContext.getBootLogger().warn(ex);
            result = false;
        }
        
        return result;
    }
    
    private String getDateString(Date d) {
        return ModelUtils.getDateAsFormatString(d, "yyyy�NM��d��");      
    }
    
    private String getDateString(String date) {
        Date d = ModelUtils.getDateAsObject(date);
        return ModelUtils.getDateAsFormatString(d, "yyyy�NM��d��");
    }
    
    private String getSexString(String sex) {
        //return ModelUtils.getGenderDesc(sex);
        return sex;
    }

    public LetterModel getModel() {
        return model;
    }

    public void setModel(TouTouLetter model) {
        this.model = model;
    }

    public int getMarginLeft() {
        return marginLeft;
    }

    public void setMarginLeft(int marginleft) {
        this.marginLeft = marginleft;
    }

    public int getMarginRight() {
        return marginRight;
    }

    public void setMarginRight(int marginRight) {
        this.marginRight = marginRight;
    }

    public int getMarginTop() {
        return marginTop;
    }

    public void setMarginTop(int marginTop) {
        this.marginTop = marginTop;
    }

    public int getMarginBottom() {
        return marginBottom;
    }

    public void setMarginBottom(int marginBottom) {
        this.marginBottom = marginBottom;
    }

    public int getTitleFontSize() {
        return titleFontSize;
    }

    public void setTitleFontSize(int titleFontSize) {
        this.titleFontSize = titleFontSize;
    }

    public int getBodyFontSize() {
        return bodyFontSize;
    }

    public void setBodyFontSize(int bodyFontSize) {
        this.bodyFontSize = bodyFontSize;
    }

    public String getDocumentDir() {
        return documentDir;
    }

    public void setDocumentDir(String documentDir) {
        this.documentDir = documentDir;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}

























