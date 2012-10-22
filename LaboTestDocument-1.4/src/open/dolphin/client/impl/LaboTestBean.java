package open.dolphin.client.impl;

import java.util.*;
import java.util.prefs.Preferences;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;
import open.dolphin.client.*;

import open.dolphin.delegater.LaboDelegater;

import java.awt.event.*;
import java.util.List;
import open.dolphin.helper.DBTask;
import open.dolphin.infomodel.LabTestRowObject;
import open.dolphin.infomodel.LabTestValueObject;
import open.dolphin.infomodel.NLaboItem;
import open.dolphin.infomodel.NLaboModule;
import open.dolphin.infomodel.SampleDateComparator;
import open.dolphin.table.ListTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * LaboTestBean
 * 
 * @author Kazushi Minagawa, Digital Globe, Inc.
 *
 */
public class LaboTestBean extends AbstractChartDocument {

    private static final String TITLE = "���{�e�X�g";
    private static final int DEFAULT_DIVIDER_LOC = 210;
    private static final int DEFAULT_DIVIDER_WIDTH = 10;
    private static final String COLUMN_HEADER_ITEM = "�� ��";
    private static final String GRAPH_TITLE = "��������";
    private static final String X_AXIS_LABEL = "���̍̎��";
    private static final String GRAPH_TITLE_LINUX = "Lab. Test";
    private static final String X_AXIS_LABEL_LINUX = "Sampled Date";
    private static final int FONT_SIZE_WIN = 12;
    private static final String FONT_MS_GOTHIC = "MSGothic";
    private static final int MAX_RESULT = 5;
    private static final String[] EXTRACTION_MENU = new String[]{"5��", "0", "10~5��", "5"};

    private ListTableModel<LabTestRowObject> tableModel;
    private JTable table;
    private JPanel graphPanel;

    private JComboBox extractionCombo;
    private JTextField countField;
    private LaboDelegater ldl;
    private int dividerWidth;
    private int dividerLoc;

    // �P��̌����œ��钊�o����
    private int maxResult = MAX_RESULT;

    // ���o���j���[
    private String[] extractionMenu = EXTRACTION_MENU;

    private boolean widthAdjusted;

    
    // �W�{�y�ь����l�̕\���J���[
    private Preferences myPrefs = Preferences.userNodeForPackage(this.getClass());
    
    public LaboTestBean() {
        setTitle(TITLE);
    }

    public int getMaxResult() {
        return maxResult;
    }

    public void setMaxResult(int maxResult) {
        this.maxResult = maxResult;
    }

    public String[] getExtractionMenu() {
        return extractionMenu;
    }

    public void setExtractionMenu(String[] extractionMenu) {
        this.extractionMenu = extractionMenu;
    }

    public void createTable(List<NLaboModule> modules) {

        // ���݂̃f�[�^���N���A����
        if (tableModel != null && tableModel.getDataProvider() != null) {
            tableModel.getDataProvider().clear();
        }

        // �O���t���N���A����
        graphPanel.removeAll();
        graphPanel.validate();

        // Table �̃J�����w�b�_�[�𐶐�����
        String[] header = new String[getMaxResult() + 1];
        header[0] = COLUMN_HEADER_ITEM;
        for (int col = 1; col < header.length; col++) {
            header[col] = "";
        }

        // ���ʂ��[���ł���ΕԂ�
        if (modules == null || modules.size() == 0) {
            tableModel = new ListTableModel<LabTestRowObject>(header, 0);
            table.setModel(tableModel);
            setColumnWidth();
            return;
        }

        // ���̍̎���̍~���Ȃ̂ŏ����Ƀ\�[�g����
        Collections.sort(modules, new SampleDateComparator());

        // �e�X�g���ڑS�ĂɑΉ����� rowObject �𐶐�����
        List<LabTestRowObject> dataProvider = new ArrayList<LabTestRowObject>();

        int moduleIndex = 0;

        for (NLaboModule module : modules) {

            // ���̍̎��
            header[moduleIndex+1] = module.getSampleDate();

            // ���W���[���Ɋ܂܂�錟������
            Collection<NLaboItem> c = module.getItems();

            for (NLaboItem item : c) {

                // RowObject �𐶐��� dataProvider �։�����
                // �ŏ��̃��W���[���̃e�X�g���ڂ͖������ɉ�����
                if (moduleIndex == 0) {
                    // row
                    LabTestRowObject row = new LabTestRowObject();
                    row.setLabCode(item.getLaboCode());
                    row.setGroupCode(item.getGroupCode());
                    row.setParentCode(item.getParentCode());
                    row.setItemCode(item.getItemCode());
                    row.setItemName(item.getItemName());
                    row.setUnit(item.getUnit());
                    row.setNormalValue(item.getNormalValue());
                    // value�� moduleIndex�ԖڂɃZ�b�g����
                    LabTestValueObject value = new LabTestValueObject();
                    value.setValue(item.getValue());
                    value.setOut(item.getAbnormalFlg());
                    value.setComment1(item.getComment1());
                    value.setComment2(item.getComment2());
                    row.addLabTestValueObjectAt(moduleIndex, value);
                    //
                    dataProvider.add(row);
                    continue;
                }

                // ��ڂ̃��W���[������͖��������������
                boolean found = false;

                for (LabTestRowObject rowObject : dataProvider) {
                    if (item.getItemCode().equals(rowObject.getItemCode())) {
                        found = true;
                        LabTestValueObject value = new LabTestValueObject();
                        value.setValue(item.getValue());
                        value.setOut(item.getAbnormalFlg());
                        value.setComment1(item.getComment1());
                        value.setComment2(item.getComment2());
                        rowObject.addLabTestValueObjectAt(moduleIndex, value);
                        break;
                    }
                }

                if (!found) {
                    LabTestRowObject row = new LabTestRowObject();
                    row.setLabCode(item.getLaboCode());
                    row.setGroupCode(item.getGroupCode());
                    row.setParentCode(item.getParentCode());
                    row.setItemCode(item.getItemCode());
                    row.setItemName(item.getItemName());
                    row.setUnit(item.getUnit());
                    row.setNormalValue(item.getNormalValue());
                    //
                    LabTestValueObject value = new LabTestValueObject();
                    value.setValue(item.getValue());
                    value.setOut(item.getAbnormalFlg());
                    value.setComment1(item.getComment1());
                    value.setComment2(item.getComment2());
                    row.addLabTestValueObjectAt(moduleIndex, value);
                    //
                    dataProvider.add(row);
                }
            }

            moduleIndex++;
        }

        // dataProvider �̗v�f rowObject ���\�[�g����
        // grpuCode,parentCode,itemcode;
        Collections.sort(dataProvider);

        // Table Model
        tableModel = new ListTableModel<LabTestRowObject>(header, 0);

        // �������ʃe�[�u���𐶐�����
        table.setModel(tableModel);
        setColumnWidth();

        // dataProvider
        tableModel.setDataProvider(dataProvider);
    }

    /**
     * Table�̃J�������𒲐�����B
     */
    private void setColumnWidth() {
        // �J�������𒲐�����
        if (!widthAdjusted) {
            table.getTableHeader().getColumnModel().getColumn(0).setPreferredWidth(180);
            table.getTableHeader().getColumnModel().getColumn(1).setPreferredWidth(100);
            table.getTableHeader().getColumnModel().getColumn(2).setPreferredWidth(100);
            table.getTableHeader().getColumnModel().getColumn(3).setPreferredWidth(100);
            table.getTableHeader().getColumnModel().getColumn(4).setPreferredWidth(100);
            table.getTableHeader().getColumnModel().getColumn(5).setPreferredWidth(100);
            widthAdjusted = true;
        }
    }

    /**
     * GUI�R���|�[�l���g������������B
     */
    private void initialize() {

        // Divider
        dividerWidth = DEFAULT_DIVIDER_WIDTH;
        dividerLoc = DEFAULT_DIVIDER_LOC;

        JPanel controlPanel = createControlPanel();

        graphPanel = new JPanel(new BorderLayout());
        graphPanel.setPreferredSize(new Dimension(500, dividerLoc));

        // �������ʃe�[�u���𐶐�����
        table = new JTable();

        // �s��
        table.setRowHeight(ClientContext.getHigherRowHeight());

        // Renderer��ݒ肷��
        table.setDefaultRenderer(Object.class, new LabTestRenderer());

        // �s�I�����\�ɂ���
        table.setRowSelectionAllowed(true);

        // �O���t�\���̃��X�i��o�^����
        ListSelectionModel m = table.getSelectionModel();
        m.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    createAndShowGraph(table.getSelectedRows());
                }
            }
        });

        JScrollPane jScrollPane1 = new JScrollPane();
        jScrollPane1.setViewportView(table);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(3, 600));

        JPanel tablePanel = new JPanel(new BorderLayout(0, 7));
        tablePanel.add(controlPanel, BorderLayout.SOUTH);
        tablePanel.add(jScrollPane1, BorderLayout.CENTER);

        // Lyouts
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, graphPanel, tablePanel);
        splitPane.setDividerSize(dividerWidth);
        splitPane.setContinuousLayout(false);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(dividerLoc);

        getUI().setLayout(new BorderLayout());
        getUI().add(splitPane, BorderLayout.CENTER);

        getUI().setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
    }

    @Override
    public void start() {
        initialize();
        NameValuePair pair = (NameValuePair) extractionCombo.getSelectedItem();
        String value = pair.getValue();
        int firstResult = Integer.parseInt(value);
        searchLaboTest(firstResult);
    }

    @Override
    public void stop() {
        if (tableModel != null && tableModel.getDataProvider() != null) {
            tableModel.getDataProvider().clear();
        }
    }

    /**
     * LaboTest �̌����^�X�N���R�[������B
     */
    private void searchLaboTest(final int firstResult) {

        final String pid = getContext().getPatient().getPatientId();
        ldl = new LaboDelegater();

        DBTask task = new DBTask<List<NLaboModule>, Void>(getContext()) {

            @Override
            protected List<NLaboModule> doInBackground() throws Exception {

                List<NLaboModule> modules = ldl.getLaboTest(pid, firstResult, getMaxResult());
                return modules;
            }

            @Override
            protected void succeeded(List<NLaboModule> modules) {
                int moduleCount = modules != null ? modules.size() : 0;
                countField.setText(String.valueOf(moduleCount));
                createTable(modules);
            }
        };

        task.execute();

    }

    /**
     * �������ʃe�[�u���őI�����ꂽ�s�i�������ځj�̐܂���O���t�𐶐�����B
     * �����I��Ή�
     * JFreeChart ���g�p����B
     */
    private void createAndShowGraph(int[] selectedRows) {

        if (selectedRows == null || selectedRows.length == 0) {
            return;
        }
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // �I������Ă���s�i�������ځj���C�e���[�g���Adataset �֒l��ݒ肷��
        for (int cnt = 0; cnt < selectedRows.length; cnt++) {

            int row = selectedRows[cnt];
            List<LabTestRowObject> dataProvider = tableModel.getDataProvider();
            LabTestRowObject rowObj = dataProvider.get(row);
            List<LabTestValueObject> values = rowObj.getValues();

            boolean valueIsNumber = true;
            
            // ���̍̎�����Ƃ̒l��ݒ肷��
            // �J�����̂P�Ԗڂ���̎�����Z�b�g����Ă���
            for (int col = 1; col < getMaxResult(); col++) {

                String sampleTime = tableModel.getColumnName(col);

                // ���̍̎��="" -> �����Ȃ�
                if (sampleTime.equals("")) {
                    break;
                }

                LabTestValueObject value = values.get(col -1);

                try {
                    if (value != null) {
                        double val = Double.parseDouble(value.getValue());
                        dataset.setValue(val, rowObj.nameWithUnit(), sampleTime);
                    } else {
                        dataset.setValue(null, rowObj.nameWithUnit(), sampleTime);
                    }

                } catch (Exception e) {
                    valueIsNumber = false;
                    break;
                }
            }

            if (!valueIsNumber) {
                return;
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(
                    getGraphTitle(),                // Title
                    getXLabel(),                    // x-axis Label
                    "",                             // y-axis Label
                    dataset,                        // Dataset
                    PlotOrientation.VERTICAL,       // Plot Orientation
                    true,                           // Show Legend
                    true,                           // Use tooltips
                    false                           // Configure chart to generate URLs?
                    );

        // Win �̕�������
        if (ClientContext.isWin()) {
            chart.getTitle().setFont(getWinFont());
            chart.getLegend().setItemFont(getWinFont());
            chart.getCategoryPlot().getDomainAxis().setLabelFont(getWinFont());
            chart.getCategoryPlot().getDomainAxis().setTickLabelFont(getWinFont());
        }

        ChartPanel chartPanel = new ChartPanel(chart);

        graphPanel.removeAll();
        graphPanel.add(chartPanel, BorderLayout.CENTER);
        graphPanel.validate();
    }

    //====================================================================
    private String getGraphTitle() {
        return ClientContext.isLinux() ? GRAPH_TITLE_LINUX : GRAPH_TITLE;
    }

    private String getXLabel() {
        return ClientContext.isLinux() ? X_AXIS_LABEL_LINUX : X_AXIS_LABEL;
    }

    private Font getWinFont() {
        return new Font(FONT_MS_GOTHIC, Font.PLAIN, FONT_SIZE_WIN);
    }
    //====================================================================

    /**
     * ���o���ԃp�l����Ԃ�
     */
    private JPanel createControlPanel() {

        String[] menu = getExtractionMenu();
        int cnt = menu.length / 2;
        NameValuePair[] periodObject = new NameValuePair[cnt];
        int valIndex = 0;
        for (int i = 0; i < cnt; i++) {
            periodObject[i] = new NameValuePair(menu[valIndex], menu[valIndex+1]);
            valIndex += 2;
        }

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(Box.createHorizontalStrut(7));

        // ���o���ԃR���{�{�b�N�X
        p.add(new JLabel("�ߋ�"));
        p.add(Box.createRigidArea(new Dimension(5, 0)));
        extractionCombo = new JComboBox(periodObject);

        extractionCombo.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    NameValuePair pair = (NameValuePair) extractionCombo.getSelectedItem();
                    int firstResult = Integer.parseInt(pair.getValue());
                    searchLaboTest(firstResult);
                }
            }
        });
        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        comboPanel.add(extractionCombo);

        p.add(comboPanel);

        // �O��
        p.add(Box.createHorizontalGlue());

        // �����t�B�[���h
        p.add(new JLabel("����"));
        p.add(Box.createRigidArea(new Dimension(5, 0)));
        countField = new JTextField(2);
        countField.setEditable(false);
        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        countPanel.add(countField);
        p.add(countPanel);

        // �X�y�[�X
        p.add(Box.createHorizontalStrut(7));

        return p;
    }
}
