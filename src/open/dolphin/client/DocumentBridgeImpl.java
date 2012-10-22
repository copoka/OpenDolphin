package open.dolphin.client;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JScrollPane;
import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.infomodel.IInfoModel;

/**
 * �Q�ƃ^�u��ʂ�񋟂��� Bridge �N���X�B���̃N���X�� scroller ��
 * �J���e�A�Љ�󓙂̂ǂ���߂�Ƃ��\�������B
 * 
 * @author kazushi Minagawa, Digital Globe, Inc.
 */
public class DocumentBridgeImpl extends AbstractChartDocument 
    implements PropertyChangeListener, DocumentBridger {
    
    private static final String TITLE = "�Q ��";
        
    // �����\���N���X�̃C���^�[�t�F�C�X
    private DocumentViewer curViwer;
    
    // Scroller  
    private JScrollPane scroller;
    
    public DocumentBridgeImpl() {
        setTitle(TITLE);
    }
    

    @Override
    public void start() {
        
        scroller = new JScrollPane();
        getUI().setLayout(new BorderLayout());
        getUI().add(scroller, BorderLayout.CENTER);
        
        // ���������̃v���p�e�B�ʒm�����b�X������
        DocumentHistory h = getContext().getDocumentHistory();
        h.addPropertyChangeListener(DocumentHistory.DOCUMENT_TYPE, this);
        h.addPropertyChangeListener(DocumentHistory.HITORY_UPDATED, this);
        h.addPropertyChangeListener(DocumentHistory.SELECTED_HISTORIES, this);
        
        curViwer = new KarteDocumentViewer();
        curViwer.setContext(getContext());
        curViwer.start();
            
        enter();
    }

    @Override
    public void stop() {  
        if (curViwer != null) {
            curViwer.stop();
        }
    }
    
    @Override
    public void enter() {
        if (curViwer != null) {
            // ����ɂ�胁�j���[�� viwer �Ő��䂳���
            curViwer.enter();
        } else {
            super.enter();
        }
    }
    
    /**
     * Bridge �@�\��񋟂���B�I�����ꂽ�����̃^�C�v�ɉ����ăr���[�փu���b�W����B
     * @param docs �\�����镶���� DocInfo �z��
     */
    public void showDocuments(DocInfoModel[] docs) {
        
        if (docs == null || docs.length == 0) {
            return;
        }
        
        if (curViwer != null) {
            //getContext().showDocument(0);
            curViwer.showDocuments(docs, scroller);
            //getContext().showDocument(0);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        
        ChartMediator med = this.getContext().getChartMediator();
        med.setCurKarteComposit(null);
        
        String prop = evt.getPropertyName();
        
        if (prop.equals(DocumentHistory.DOCUMENT_TYPE)) {
            
            String docType = (String) evt.getNewValue();
            
            if (docType.equals(IInfoModel.DOCTYPE_LETTER)) {
                curViwer = new LetterViewer();
            } else {
                curViwer = new KarteDocumentViewer();
            }
            
            curViwer.setContext(getContext());
            curViwer.start();
            
        } else if (prop.equals(DocumentHistory.HITORY_UPDATED)) {
            // ���������̒��o���Ԃ��ύX���ꂽ�ꍇ
            if (curViwer != null) {
                curViwer.historyPeriodChanged();
            }
            this.scroller.setViewportView(null);
            
        } else if (prop.equals(DocumentHistory.SELECTED_HISTORIES)) {
            
            // ���������̑I�����ύX���ꂽ�ꍇ
            DocInfoModel[] selectedHistoroes = (DocInfoModel[]) evt.getNewValue();
            this.showDocuments(selectedHistoroes);
        }
    }
    
    public KarteViewer getBaseKarte() {
        if (curViwer != null && curViwer instanceof KarteDocumentViewer) {
            return ((KarteDocumentViewer) curViwer).getBaseKarte();
        }
        return null;
    }
}
