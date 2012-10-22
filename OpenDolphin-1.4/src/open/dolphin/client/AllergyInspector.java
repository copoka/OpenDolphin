package open.dolphin.client;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.helper.DBTask;
import open.dolphin.infomodel.AllergyModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModelUtils;
import open.dolphin.infomodel.ObservationModel;
import open.dolphin.project.Project;
import open.dolphin.table.ListTableModel;
import open.dolphin.table.OddEvenRowRenderer;

/**
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class AllergyInspector {

    // TableModel
    private ListTableModel<AllergyModel> tableModel;

    // �R���e�i�p�l��
    private AllergyView view;

    // Chart
    private ChartImpl context;

    /**
     * AllergyInspector�I�u�W�F�N�g�𐶐�����B
     */
    public AllergyInspector(ChartImpl context) {
        this.context = context;
        initComponents();
        update();
    }
    
    public Chart getContext() {
        return context;
    }

    /**
     * ���C�A�E�g�p�l����Ԃ��B
     * @return
     */
    public JPanel getPanel() {
        return (JPanel) view;
    }
    
    public void clear() {
        tableModel.clear();
    }

    /**
     * GUI�R���|�[�l���g������������B
     */
    private void initComponents() {

        view = new AllergyView();

        // �A�����M�[�e�[�u����ݒ肷��
        String[] columnNames = ClientContext.getStringArray("patientInspector.allergyInspector.columnNames");
        int startNumRows = ClientContext.getInt("patientInspector.allergyInspector.startNumRows");
        String[] methodNames = ClientContext.getStringArray("patientInspector.allergyInspector.methodNames");
        tableModel = new ListTableModel<AllergyModel>(columnNames, startNumRows, methodNames, null);
        view.getTable().setModel(tableModel);
        view.getTable().setRowHeight(ClientContext.getHigherRowHeight());
        view.getTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
       
        // �����_����ݒ肷��
        view.getTable().setDefaultRenderer(Object.class, new OddEvenRowRenderer());

        // �E�N���b�N�ɂ��ǉ��폜�̃��j���[��o�^����
        view.getTable().addMouseListener(new MouseAdapter() {

            private void mabeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    JPopupMenu pop = new JPopupMenu();
                    JMenuItem item = new JMenuItem("�ǉ�");
                    pop.add(item);
                    item.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            new AllergyEditor(AllergyInspector.this);
                        }
                    });
                    final int row = view.getTable().rowAtPoint(e.getPoint());
                    if (tableModel.getObject(row) != null) {
                        pop.add(new JSeparator());
                        JMenuItem item2 = new JMenuItem("�폜");
                        pop.add(item2);
                        item2.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                delete(row);
                            }
                        });
                    }
                    pop.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mabeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mabeShowPopup(e);
            }
        });
    }
    
    private void scroll(boolean ascending) {
        
        int cnt = tableModel.getObjectCount();
        if (cnt > 0) {
            int row = 0;
            if (ascending) {
                row = cnt - 1;
            }
            Rectangle r = view.getTable().getCellRect(row, row, true);
            view.getTable().scrollRectToVisible(r);
        }
    }

    /**
     * �A�����M�[����\������B
     */
    public void update() {
        List<AllergyModel> list = (List<AllergyModel>) context.getKarte().getEntryCollection("allergy");
        if (list != null && list.size() >0) {
            boolean asc = Project.getPreferences().getBoolean(Project.DOC_HISTORY_ASCENDING, false);
            if (asc) {
                Collections.sort(list);
            } else {
                Collections.sort(list, Collections.reverseOrder());
            }
            tableModel.setDataProvider(list);
            scroll(asc);
        }
    }

    /**
     * �A�����M�[�f�[�^��ǉ�����B
     */
    public void add(final AllergyModel model) {

        // GUI �̓������TimeStamp�ɕύX����
        Date date = ModelUtils.getDateTimeAsObject(model.getIdentifiedDate()+"T00:00:00");

        final List<ObservationModel> addList = new ArrayList<ObservationModel>(1);

        ObservationModel observation = new ObservationModel();
        observation.setKarte(context.getKarte());
        observation.setCreator(Project.getUserModel());
        observation.setObservation(IInfoModel.OBSERVATION_ALLERGY);
        observation.setPhenomenon(model.getFactor());
        observation.setCategoryValue(model.getSeverity());
        observation.setConfirmed(date);
        observation.setRecorded(new Date());
        observation.setStarted(date);
        observation.setStatus(IInfoModel.STATUS_FINAL);
        observation.setMemo(model.getMemo());
        addList.add(observation);
        
        DBTask task = new DBTask<List<Long>, Void>(context) {

            @Override
            protected List<Long> doInBackground() throws Exception {
                DocumentDelegater ddl = new DocumentDelegater();
                List<Long> ids = ddl.addObservations(addList);
                return ids;
            }
            
            @Override
            protected void succeeded(List<Long> result) {
                model.setObservationId(result.get(0));
                boolean asc = Project.getPreferences().getBoolean(Project.DOC_HISTORY_ASCENDING, false);
                if (asc) {
                    tableModel.addObject(model);
                } else {
                    tableModel.addObject(0, model);
                }
                scroll(asc);
            }
        };
        
        task.execute();
    }

    /**
     * �e�[�u���őI�������A�����M�[���폜����B
     */
    public void delete(final int row) {

        AllergyModel model = (AllergyModel) tableModel.getObject(row);

        if (model == null) {
            return;
        }

        final List<Long> list = new ArrayList<Long>(1);
        list.add(new Long(model.getObservationId()));

        DBTask task = new DBTask<Void, Void>(this.context) {

            @Override
            protected Void doInBackground() throws Exception {
                DocumentDelegater ddl = new DocumentDelegater();
                ddl.removeObservations(list);
                return null;
            }

            @Override
            protected void succeeded(Void result) {
                tableModel.deleteAt(row);
            }
        };

        task.execute();
    }
}
