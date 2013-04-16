package open.dolphin.impl.labrcv;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import open.dolphin.client.*;
import open.dolphin.delegater.LaboDelegater;
import open.dolphin.impl.pvt.WatingListImpl;
import open.dolphin.infomodel.ChartEventModel;
import open.dolphin.infomodel.PatientLiteModel;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.project.Project;
import open.dolphin.table.ListTableModel;
import open.dolphin.table.StripeTableCellRenderer;

/**
 * LabTestImporter
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class NLaboTestImporter extends AbstractMainComponent implements PropertyChangeListener {
    
    private static final String NAME = "ラボレシーバ";
    private static final String SUCCESS = "成功";
    private static final String ERROR = "エラー";
    private static final Color UNCONSTRAINED_COLOR = new Color(255,102,102);
    private static final String[] LAB_FILES = {"DAT","dat","DAT2","dat2","HL7","hl7", "TXT", "txt", "CSV", "csv"};
    
    // 選択されている患者情報
    private NLaboImportSummary selectedLabo;
    private int number = 100000;
   
    // View
    private ListTableModel<NLaboImportSummary> tableModel;
    private NLabTestImportView view;
    
    private int stateColumn=9;
    
//masuda^
    private String clientUUID;
    private ChartEventHandler cel;
//masuda$    
    
    /** Creates new NLaboTestImporter */
    public NLaboTestImporter() {
        setName(NAME);
        cel = ChartEventHandler.getInstance();
        clientUUID = cel.getClientUUID();
        cel.addPropertyChangeListener(NLaboTestImporter.this);
    }
    
    @Override
    public void start() {
        initComponents();
        connect();
        enter();
    }
    
    @Override
    public void enter() {
        controlMenu();
    }
    
    @Override
    public void stop() {
        cel.removePropertyChangeListener(NLaboTestImporter.this);
    }

    public JProgressBar getProgressBar() {
        return getContext().getProgressBar();
    }

    public ListTableModel<NLaboImportSummary> getTableModel() {
        return (ListTableModel<NLaboImportSummary>) view.getTable().getModel();
    }
    
    public NLaboImportSummary getSelectedLabo() {
        return selectedLabo;
    }

    public void setSelectedLabo(NLaboImportSummary selectedLabo) {
        this.selectedLabo = selectedLabo;
        controlMenu();
    }
    
    public void openKarte() {
        
        boolean showReceiptMessage = Project.getBoolean("showReceiptMessage", true);
        if (showReceiptMessage) {
            JLabel msg1 = new JLabel("受付リストからオープンしないと診療データをレセコンに");
            JLabel msg2 = new JLabel("送信することができません。続けますか?");
            final JCheckBox cb = new JCheckBox("今後このメッセージを表示しない");
            cb.setFont(new Font("Dialog", Font.PLAIN, 10));
            cb.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Project.setBoolean("showReceiptMessage", !cb.isSelected());
                }
            });
            JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
            p1.add(msg1);
            JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
            p2.add(msg2);
            JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
            p3.add(cb);
            JPanel box = new JPanel();
            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
            box.add(p1);
            box.add(p2);
            box.add(p3);
            box.setBorder(BorderFactory.createEmptyBorder(0, 0, 11, 11));
            
            int option = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(getUI()),
                    new Object[]{box},
                    ClientContext.getFrameTitle(getName()),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
//minagawa^ Icon server                    
                    //ClientContext.getImageIcon("about_32.gif"));
                    ClientContext.getImageIconArias("icon_info"));
//minagawa$            
            
            if (option != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        PatientModel patient = selectedLabo.getPatient();
        PatientVisitModel pvt = new PatientVisitModel();
        pvt.setNumber(number++);
        pvt.setPatientModel(patient);
        
        // 受け付けを通していないので診療科はユーザ登録してあるものを使用する
        // 診療科名、診療科コード、医師名、医師コード、JMARI
        // 2.0
        pvt.setDeptName(Project.getUserModel().getDepartmentModel().getDepartmentDesc());
        pvt.setDeptCode(Project.getUserModel().getDepartmentModel().getDepartment());
        pvt.setDoctorName(Project.getUserModel().getCommonName());
        if (Project.getUserModel().getOrcaId()!=null) {
            pvt.setDoctorId(Project.getUserModel().getOrcaId());
        } else {
            pvt.setDoctorId(Project.getUserModel().getUserId());
        }
        pvt.setJmariNumber(Project.getString(Project.JMARI_CODE));
        
        // カルテコンテナを生成する
        getContext().openKarte(pvt);
    }
    
    
    /**
     * 検索結果件数を設定しステータスパネルへ表示する。
     * @param cnt 件数
     */
    public void updateCount() {
        int count = getTableModel().getObjectCount();
        String text = String.valueOf(count);
        text += "件";
        view.getCountLbl().setText(text);
    }
    
    /**
     * メニューを制御する
     */
    private void controlMenu() {
        
        PatientModel pvt = getSelectedLabo() != null 
                         ? getSelectedLabo().getPatient() 
                         : null;
        
        boolean enabled = canOpen(pvt);
        getContext().enabledAction(GUIConst.ACTION_OPEN_KARTE, enabled);
    }
    
    /**
     * カルテを開くことが可能かどうかを返す。
     * @return 開くことが可能な時 true
     */
    private boolean canOpen(PatientModel patient) {
        if (patient == null) {
            return false;
        }
        
        if (isKarteOpened(patient)) {
            return false;
        }
     
        return true;
    }
    
    /**
     * カルテがオープンされているかどうかを返す。
     * @return オープンされている時 true
     */
    private boolean isKarteOpened(PatientModel patient) {
        if (patient != null) {
            boolean opened = false;
            java.util.List<ChartImpl> allCharts = ChartImpl.getAllChart();
            for (ChartImpl chart : allCharts) {
                if (chart.getPatient().getId() == patient.getId()) {
                    opened = true;
                    break;
                }
            }
            return opened;
        }
        return false;
    }
    
    /**
     * 検査結果ファイルを選択し、パースする。
     */
    private void selectAndParseLabFile() {
//minagawa^ mac-jdk7 対応
//        // 検査結果ファイルを選択する
//        JFileChooser chooser = new JFileChooser();
//        FileNameExtensionFilter filter = new FileNameExtensionFilter("Lab Result File", "DAT","dat","DAT2","dat2","HL7","hl7", "TXT", "txt", "CSV", "csv");
//        chooser.setFileFilter(filter);
//        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//        int returnVal = chooser.showOpenDialog(getUI());
//
//        if (returnVal != JFileChooser.APPROVE_OPTION) {
//            return;
//        }
//        
//        // パースしてテーブルへ表示する
//        // 登録ボタンをアクティブにする
//        //final File labFile = new File(chooser.getSelectedFile().getPath());
//        final Path path = Paths.get(chooser.getSelectedFile().getPath());
        Window parent = SwingUtilities.getWindowAncestor(getUI());
        FileDialog fd = new FileDialog((Frame)parent, "検査結果ファイルを選択" , FileDialog.LOAD);
        fd.setFilenameFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean match = false;
                for (String ext : LAB_FILES) {
                    if (name.endsWith(ext)) {
                        match = true;
                        break;
                    }
                }
                return match;
            }
        });
        fd.setMultipleMode(false);
        fd.setVisible(true);
        String dir=fd.getDirectory();//ディレクトリーの取得
        String fileName=fd.getFile();//File名の取得
        if (fileName==null) {
            return;
        }
        final Path path = Paths.get(dir, fileName);

        final javax.swing.SwingWorker worker = new javax.swing.SwingWorker<List<NLaboImportSummary>, Void>() {

            @Override
            protected List<NLaboImportSummary> doInBackground() throws Exception {
//minagawa^
                //LabResultParser parse = LabParserFactory.getParser(labFile.getName());
                //List<NLaboImportSummary> dataList = parse.parse(labFile);
                LabResultParser parse = LabParserFactory.getParser(path.getFileName().toString());
                List<NLaboImportSummary> dataList = parse.parse(path);
//minagawa$
                if (dataList!=null && dataList.size()>0) {

                    List<String> idList = new ArrayList<String>(dataList.size());
                    for (NLaboImportSummary sm : dataList) {
                        idList.add(sm.getPatientId());
                    }

                    LaboDelegater laboDelegater = new LaboDelegater();
                    List<PatientLiteModel> pList = laboDelegater.getConstrainedPatients(idList);

                    //for (int i = 0; i < pList.size(); i++) {
                    for (int i = 0; i < dataList.size(); i++) {
                        NLaboImportSummary sm = dataList.get(i);
                         PatientLiteModel pl = pList.get(i);
                        if (pl!=null) {
                            sm.setKarteId(pl.getPatientId());
                            sm.setKarteBirthday(pl.getBirthday());
                            sm.setKarteKanaName(pl.getKanaName());
                            sm.setKarteName(pl.getFullName());
                            sm.setKarteSex(pl.getGenderDesc());
                        }
                    }
                }

                return dataList;
            }

            @Override
            protected void done() {

                try {
                    List<NLaboImportSummary> allModules = get();
                    getTableModel().setDataProvider(allModules);

                } catch (Throwable e) {
                    e.printStackTrace(System.err);
                    String why = e.getMessage();
                    Window parent = SwingUtilities.getWindowAncestor(getUI());
                    StringBuilder sb = new StringBuilder();
                    sb.append("登録できないファイルがあります。").append("\n");
                    sb.append("検査結果ファイルに誤りがある可能性があります。").append("\n");
                    sb.append(why);
                    String message = sb.toString();
                    String title = "ラボレシーバ";
                    JOptionPane.showMessageDialog(parent, message, ClientContext.getFrameTitle(title), JOptionPane.WARNING_MESSAGE);
                }
            }
        };

        worker.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getNewValue().equals(javax.swing.SwingWorker.StateValue.STARTED)) {
                    getProgressBar().setIndeterminate(true);
                } else if (evt.getNewValue().equals(javax.swing.SwingWorker.StateValue.DONE)) {
                    getProgressBar().setIndeterminate(false);
                    getProgressBar().setValue(0);
                    worker.removePropertyChangeListener(this);
                }
            }
        });

        worker.execute();
    }

    /**
     * パースした検査結果を登録する。
     */
    private void addLabtest() {

        final List<NLaboImportSummary> modules = getTableModel().getDataProvider();

        final javax.swing.SwingWorker worker = new javax.swing.SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {

                LaboDelegater laboDelegater = new LaboDelegater();

                for (NLaboImportSummary summary : modules) {

                    PatientModel pm = laboDelegater.putNLaboModule(summary.getModule());

                    if (pm != null) {
                        summary.setPatient(pm);
                        summary.setResult(SUCCESS);

                    } else {
                        summary.setResult(ERROR);
                    }

                    // Table 更新
                    Runnable awt = new Runnable() {
                        @Override
                        public void run() {
                            getTableModel().fireTableDataChanged();
                        }
                    };
                    EventQueue.invokeLater(awt);
                }

                return null;
            }
        };

        worker.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getNewValue().equals(javax.swing.SwingWorker.StateValue.STARTED)) {
                    getProgressBar().setIndeterminate(true);
                } else if (evt.getNewValue().equals(javax.swing.SwingWorker.StateValue.DONE)) {
                    getProgressBar().setIndeterminate(false);
                    getProgressBar().setValue(0);
                    worker.removePropertyChangeListener(this);
                }
            }
        });

        worker.execute();
    }

    
    /**
     * コンポーンントにリスナを登録し接続する。
     */
    private void connect() {

        // ファイル選択ボタン
        view.getFileBtn().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // ファイル選択
                selectAndParseLabFile();
            }
        });

        // 登録ボタン
        view.getAddBtn().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // 検査結果登録
                view.getAddBtn().setEnabled(false);
                addLabtest();
            }
        });
        view.getAddBtn().setEnabled(false);

        // クリアボタン
        view.getClearBtn().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // 検査結果登録
                getTableModel().setDataProvider(null);
            }
        });
        view.getClearBtn().setEnabled(false);
        
        // 行選択
        view.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    NLaboImportSummary lab = getTableModel().getObject(view.getTable().getSelectedRow());
                    if (lab != null) {
                        setSelectedLabo(lab);
                    }
                }
            }
        });
        
        // ダブルクリック
        view.getTable().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    NLaboImportSummary lab = getTableModel().getObject(view.getTable().getSelectedRow());
                    if (lab != null && lab.getPatient()!=null) {
                        openKarte();
                    }
                }
            }
        });

        // コンテキストメニューリスナを設定する
        view.getTable().addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                mabeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mabeShowPopup(e);
            }

            private void mabeShowPopup(MouseEvent e) {

                if (e.isPopupTrigger()) {

                    final JPopupMenu contextMenu = new JPopupMenu();

                    JTable table = view.getTable();
                    int row = table.rowAtPoint(e.getPoint());
                    Object obj = (Object) getTableModel().getObject(row);
                    int selected = table.getSelectedRow();

                    if (row == selected && obj != null) {
                        String pop1 = ClientContext.getString("watingList.popup.openKarte");
                        contextMenu.add(new JMenuItem(new ReflectAction(pop1, NLaboTestImporter.this, "openKarte")));
                    }
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        // data 件数リスナ
        getTableModel().addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                List<NLaboImportSummary> list = (List<NLaboImportSummary>) evt.getNewValue();
                boolean enabled = (list != null && list.size() > 0) ? true : false;
                boolean clearOk = enabled;
                if (enabled) {
                    for (NLaboImportSummary sm : list) {
                        if (sm.getKarteId()==null) {
                            enabled = false;
                            break;
                        }
                    }
                }
                view.getAddBtn().setEnabled(enabled);
                view.getClearBtn().setEnabled(clearOk);
                updateCount();
            }
        });
    }

    /**
     * GUI コンポーネントを初期化する。
     */
    private void initComponents() {

        view = new NLabTestImportView();
        setUI(view);

        String[] columnNames = new String[]{"ラボ", "患者ID", "カナ", "カルテ・カナ", "性別", "カルテ・性別", "検体採取日", "項目数", "登録", "状態"};
        String[] propNames = new String[]{"laboCode", "patientId", "patientName", "karteKanaName", "patientSex", "karteSex", "sampleDate", "numOfTestItems", "result", null};
        int[] columnWidth = new int[]{50, 120, 120, 120, 50, 70, 110, 50, 40,20};

        tableModel = new ListTableModel<NLaboImportSummary>(
                columnNames, 0, propNames, null);
        view.getTable().setModel(tableModel);
        view.getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        view.getTable().setTransferHandler(new NLaboTestFileTransferHandler(this));
        
//minagawa^ lsctest カラム移動禁止
        view.getTable().getTableHeader().setReorderingAllowed(false);
//minagawa$        

        // カラム幅を変更する
        for (int i = 0; i < columnWidth.length; i++) {
            view.getTable().getColumnModel().getColumn(i).setPreferredWidth(columnWidth[i]);
        }

        // レンダラを設定する
        LabTestRenderer renderer = new LabTestRenderer();
        renderer.setTable(view.getTable());
        renderer.setDefaultRenderer();
        
        // 行高
        view.getTable().setRowHeight(ClientContext.getHigherRowHeight());

        // カウント値０を設定する
        updateCount();
    }
    
    //masuda$
    // ChartEventListener
    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        
        if (tableModel==null) {
            return;
        }
        
        List<NLaboImportSummary> list = tableModel.getDataProvider();
        if (list==null || list.isEmpty()) {
            return;
        }
        
        ChartEventModel evt = (ChartEventModel)pce.getNewValue();
        
        int sRow = -1;
        long ptPk = evt.getPtPk();
        int eventType = evt.getEventType();

        switch (eventType) {
            case ChartEventModel.PVT_STATE:
                for (int row = 0; row < list.size(); ++row) {
                    NLaboImportSummary nlab = list.get(row);
                    PatientModel pm = nlab.getPatient();
                    if (pm!=null && ptPk == pm.getId()) {
                        sRow = row;
                        pm.setOwnerUUID(evt.getOwnerUUID());
                        break;
                    }
                }
                break;
            case ChartEventModel.PM_MERGE:
                for (int row = 0; row < list.size(); ++row) {
                    NLaboImportSummary nlab = list.get(row);
                    PatientModel pm = nlab.getPatient();
                    if (pm!=null && ptPk == pm.getId()) {
                        sRow = row;
                        nlab.setPatient(evt.getPatientModel());
                        break;
                    }
                }
                break;                
            case ChartEventModel.PVT_MERGE:
                for (int row = 0; row < list.size(); ++row) {
                    NLaboImportSummary nlab = list.get(row);
                    PatientModel pm = nlab.getPatient();
                    if (pm!=null && ptPk == pm.getId()) {
                        sRow = row;
                        nlab.setPatient(evt.getPatientVisitModel().getPatientModel());
                        break;
                    }
                }
                break;
            default:
                break;
        }
        
        if (sRow != -1) {
            tableModel.fireTableRowsUpdated(sRow, sRow);
        }
    }

//    protected class LabTestRenderer extends DefaultTableCellRenderer {
//
//        /** Creates new IconRenderer */
//        public LabTestRenderer() {
//            super();
//        }
//
//        @Override
//        public Component getTableCellRendererComponent(JTable table,
//                Object value,
//                boolean isSelected,
//                boolean isFocused,
//                int row, int col) {
//
//            NLaboImportSummary summary = (NLaboImportSummary) tableModel.getObject(row);
//
//            if (isSelected) {
//                this.setBackground(table.getSelectionBackground());
//                this.setForeground(table.getSelectionForeground());
//
//            } else {
//                if (summary!=null && summary.getKarteId()==null) {
//
//                    this.setBackground(UNCONSTRAINED_COLOR);
//                    
//                } else {
//
//                    if ((row & (1)) == 0) {
//                        this.setBackground(EVEN_COLOR);
//                    } else {
//                        this.setBackground(ODD_COLOR);
//                    }
//                }
//
//                this.setForeground(table.getForeground());
//            }
//            
//            PatientModel pm = summary!=null ? summary.getPatient() : null;
//            if (pm != null && col == stateColumn) {
//                setHorizontalAlignment(JLabel.CENTER);
//                if (pm.isOpened()) {
//                    if (clientUUID.equals(pm.getOwnerUUID())) {
//                        setIcon(WatingListImpl.OPEN_ICON);
//                    } else {
//                        setIcon(WatingListImpl.NETWORK_ICON);
//                    }
//                } else {
//                    setIcon(null);
//                }
//                setText("");
//            } else {
//                setIcon(null);
//                setText(value == null ? "" : value.toString());
//            }
//
//            return this;
//        }
//    }
    
       // ストライプテーブル
    private class LabTestRenderer extends StripeTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean isFocused,
                int row, int col) {

            super.getTableCellRendererComponent(table, value, isSelected, isFocused, row, col);

            this.setHorizontalAlignment(JLabel.LEFT);
            NLaboImportSummary summary = tableModel.getObject(row);
            if (summary != null && summary.getKarteId() == null) {
                this.setBackground(UNCONSTRAINED_COLOR);
            }
            
            PatientModel pm = summary.getPatient();
            if (pm != null && col == stateColumn) {
                setHorizontalAlignment(JLabel.CENTER);
                if (pm.isOpened()) {
                    if (clientUUID.equals(pm.getOwnerUUID())) {
                        setIcon(WatingListImpl.OPEN_ICON);
                    } else {
                        setIcon(WatingListImpl.NETWORK_ICON);
                    }
                } else {
                    setIcon(null);
                }
                setText("");
            } else {
                setIcon(null);
                setText(value == null ? "" : value.toString());
            }

            return this;
        }
    }

}