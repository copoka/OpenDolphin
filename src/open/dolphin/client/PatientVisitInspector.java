package open.dolphin.client;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import open.dolphin.infomodel.SimpleDate;

/**
 *
 * @author kazm
 */
public class PatientVisitInspector {
    
    private CalendarCardPanel calendarCardPanel;

    private String pvtEvent; // PVT
    
    private ChartImpl context;

    /**
     * PatientVisitInspector �𐶐�����B
     */
    public PatientVisitInspector(ChartImpl context) {
        this.context = context;
        initComponent();
        update();
    }

    /**
     * ���C�A�E�g�p�l����Ԃ��B
     * @return ���C�A�E�g�p�l��
     */
    public JPanel getPanel() {
        return calendarCardPanel;
    }

    /**
     * GUI�R���|�[�l���g������������B
     */
    private void initComponent() {
        pvtEvent = ClientContext.getString("eventCode.pvt"); // PVT
        calendarCardPanel = new CalendarCardPanel(ClientContext.getEventColorTable());
    }

    @SuppressWarnings("unchecked")
    private void update() {

        // ���@�������o��
        List<String> latestVisit = context.getKarte().getEntryCollection("visit");

        // ���@��
        if (latestVisit != null && latestVisit.size() > 0) {
            ArrayList<SimpleDate> simpleDates = new ArrayList<SimpleDate>(latestVisit.size());
            for (String pvtDate : latestVisit) {
                SimpleDate sd = SimpleDate.mmlDateToSimpleDate(pvtDate);
                sd.setEventCode(pvtEvent);
                simpleDates.add(sd);
            }
            // CardCalendar�ɒʒm����
            calendarCardPanel.setMarkList(simpleDates);
        }
    }
    
}
