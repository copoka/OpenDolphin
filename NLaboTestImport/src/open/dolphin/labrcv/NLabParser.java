package open.dolphin.labrcv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import open.dolphin.infomodel.NLaboItem;
import open.dolphin.infomodel.NLaboModule;

/**
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class NLabParser {

    private static final String MIHOKOKU = "����";

    private String encoding = "SHIFT-JIS";
    

    /**
     * ���̓X�g���[���̌������ʂ��p�[�X����B
     */
    public List<NLaboImportSummary> parse(File labFile) throws IOException, Exception {

        String line = null;
        String curKey = null;
        NLaboModule curModule = null;
        List<NLaboModule> allModules = new ArrayList<NLaboModule>();
        List<NLaboImportSummary> retList = new ArrayList<NLaboImportSummary>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(labFile),getEncoding()));

        while ((line = reader.readLine()) != null) {

            String[] data = line.split(",");    // CSV

            String lboCode = data[0];
            String patientId = data[1];
            String sampleDate = data[2];
            String patientName = data[3];
            String patientSex = data[4];

            sampleDate = sampleDate.replaceAll("/", "-");

            StringBuffer buf = new StringBuffer();
            buf.append(patientId);
            buf.append(".");
            buf.append(sampleDate);
            buf.append(".");
            buf.append(lboCode);
            String testKey = buf.toString();

            if (!testKey.equals(curKey)) {

                curModule = new NLaboModule();
                curModule.setLaboCenterCode(lboCode);
                curModule.setPatientId(patientId);
                curModule.setPatientName(patientName);
                curModule.setPatientSex(patientSex);
                curModule.setSampleDate(sampleDate);
                allModules.add(curModule);

                curKey = testKey;
            }

            NLaboItem item = new NLaboItem();

            item.setPatientId(patientId);   // �J���e�ԍ�
            item.setSampleDate(sampleDate); // ���̍̎��

            int index = 5;
            while (index < data.length) {

                switch (index) {

                    case 5:
                        item.setLipemia(data[index]);       // ���r
                        break;

                    case 6:
                        item.setHemolysis(data[index]);     // �n��
                        break;

                    case 7:
                        item.setDialysis(data[index]);      // ����
                        break;

                    case 8:
                        item.setReportStatus(data[index]);  // �񍐏�
                        break;

                    case 9:
                        item.setGroupCode(data[index]);     // �O���[�v�R�[�h
                        break;

                    case 10:
                        item.setGroupName(data[index]);     // �O���[�v����
                        break;

                    case 11:
                        item.setParentCode(data[index]);    // �������ڃR�[�h�E�e
                        break;

                    case 12:
                        item.setItemCode(data[index]);      // �������ڃR�[�h
                        break;

                    case 13:
                        item.setMedisCode(data[index]);     // MEDIS �R�[�h
                        break;

                    case 14:
                        item.setItemName(data[index]);      // �������ږ�
                        break;

                    case 15:
                        item.setAbnormalFlg(data[index]);   // �ُ�敪
                        break;

                    case 16:
                        item.setNormalValue(data[index]);   // ��l
                        break;

                    case 17:
                        item.setValue(data[index]);         // ��������
                        break;

                    case 18:
                        item.setUnit(data[index]);          // �P��
                        break;

                    case 19:
                        item.setSpecimenCode(data[index]);   // ���̍ޗ��R�[�h
                        break;

                    case 20:
                        item.setSpecimenName(data[index]);   // ���̍ޗ�����
                        break;

                    case 21:
                        item.setCommentCode1(data[index]);  // �R�����g�R�[�h1
                        break;

                    case 22:
                        item.setComment1(data[index]);      // �R�����g1
                        break;

                    case 23:
                        item.setCommentCode2(data[index]);  // �R�����g�R�[�h2
                        break;

                    case 24:
                        item.setComment2(data[index]);     // �R�����g2
                        break;
                }

                index+=1;
            }

            // �������ʒl���Ȃ��ꍇ
            if (item.getValue() == null || item.getValue().equals("")) {
                item.setValue(MIHOKOKU);
            }

            // �֌W���\�z����
            curModule.addItem(item);
            item.setLaboModule(curModule);
        }

        reader.close();

        // �T�}���𐶐�����
        for (NLaboModule module : allModules) {

            NLaboImportSummary summary = new NLaboImportSummary();
            summary.setLaboCode(module.getLaboCenterCode());
            summary.setPatientId(module.getPatientId());
            summary.setPatientName(module.getPatientName());
            summary.setPatientSex(module.getPatientSex());
            summary.setSampleDate(module.getSampleDate());
            summary.setNumOfTestItems(String.valueOf(module.getItems().size()));
            summary.setModule(module);
            retList.add(summary);
        }
        
        return retList;
    }

    /**
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
