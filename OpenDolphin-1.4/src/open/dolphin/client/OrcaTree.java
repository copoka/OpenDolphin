package open.dolphin.client;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import open.dolphin.dao.SqlOrcaSetDao;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.OrcaInputCd;
import open.dolphin.project.Project;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;

/**
 * ORCA StampTree �N���X�B
 *
 * @author Kazushi Minagawa
 */
public class OrcaTree extends StampTree {
    
    private static final String MONITOR_TITLE = "ORCA�Z�b�g����";
    
    /** ORCA ���̓Z�b�g���t�F�b�`�������ǂ����̃t���O */
    private boolean fetched;
    
    /** 
     * Creates a new instance of OrcaTree 
     */
    public OrcaTree(TreeModel model) {
        super(model);
    }
    
    /**
     * ORCA ���̓Z�b�g���t�F�b�`�������ǂ�����Ԃ��B
     * @return �擾�ς݂̂Ƃ� true
     */
    public boolean isFetched() {
        return fetched;
    }
    
    /**
     * ORCA ���̓Z�b�g���t�F�b�`�������ǂ�����ݒ肷��B
     * @param fetched �擾�ς݂̂Ƃ� true
     */
    public void setFetched(boolean fetched) {
        this.fetched = fetched;
    }
    
    /**
     * StampBox �̃^�u�ł���Tree���I�����ꂽ���R�[�������B
     */
    @Override
    public void enter() {
        
        if (!fetched) {

            // CLAIM(Master) Address ���ݒ肳��Ă��Ȃ��ꍇ�Ɍx������
            String address = Project.getClaimAddress();
            if (address == null || address.equals("")) {
//                if (SwingUtilities.isEventDispatchThread()) {
//                    String msg0 = "���Z�R����IP�A�h���X���ݒ肳��Ă��Ȃ����߁A�}�X�^�[�������ł��܂���B";
//                    String msg1 = "���ݒ胁�j���[���烌�Z�R����IP�A�h���X��ݒ肵�Ă��������B";
//                    Object message = new String[]{msg0, msg1};
//                    Window parent = SwingUtilities.getWindowAncestor(OrcaTree.this);
//                    String title = ClientContext.getFrameTitle(MONITOR_TITLE);
//                    JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
//                }
                return;
            }

            if (SwingUtilities.isEventDispatchThread()) {
                fetchOrcaSet();
            } else {
                fetchOrcaSet2();
            }
        }
    }
    
    /**
     * ORCA �̓��̓Z�b�g���擾��Tree�ɉ�����B
     */
    private void fetchOrcaSet2() {
        
        try {
            SqlOrcaSetDao dao = new SqlOrcaSetDao();
            
            ArrayList<OrcaInputCd> inputSet = dao.getOrcaInputSet();
            StampTreeNode root = (StampTreeNode) this.getModel().getRoot();
            
            for (OrcaInputCd set : inputSet) {
                ModuleInfoBean stampInfo = set.getStampInfo();
                StampTreeNode node = new StampTreeNode(stampInfo);
                root.add(node);
            }
            
            DefaultTreeModel model = (DefaultTreeModel) this.getModel();
            model.reload(root);
            
            setFetched(true);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
      
    /**
     * ORCA �̓��̓Z�b�g���擾��Tree�ɉ�����B
     */
    private void fetchOrcaSet() {

        ApplicationContext appCtx = ClientContext.getApplicationContext();
        String message = MONITOR_TITLE;
        String note = "���̓Z�b�g���������Ă��܂�...  ";
        final Component c = SwingUtilities.getWindowAncestor(this);
        int maxEstimation = 60 * 1000;
        int delay = 300;

        Task task = new Task<List<OrcaInputCd>, Void>(appCtx.getApplication()) {

            @Override
            protected List<OrcaInputCd> doInBackground() throws Exception {
                SqlOrcaSetDao dao = new SqlOrcaSetDao();
                List<OrcaInputCd> result = dao.getOrcaInputSet();
                if (dao.isNoError()) {
                    return result;
                } else {
                    throw new Exception(dao.getErrorMessage());
                }
            }

            @Override
            protected void succeeded(List<OrcaInputCd> result) {
                processResult(result);
            }

            @Override
            protected void failed(Throwable e) {
                String title = ClientContext.getFrameTitle(MONITOR_TITLE);
                JOptionPane.showMessageDialog(c, e.getMessage(), title, JOptionPane.WARNING_MESSAGE);
            }
        };

        new TaskTimerMonitor(task, appCtx.getTaskMonitor(), c, message, note, delay, maxEstimation);
        appCtx.getTaskService().execute(task);

    }
    
    /**
     * ORCA�Z�b�g��StampTree���\�z����B
     */
    private void processResult(List<OrcaInputCd> inputSet) {
        
        StampTreeNode root = (StampTreeNode) this.getModel().getRoot();

        for (OrcaInputCd set : inputSet) {
            ModuleInfoBean stampInfo = set.getStampInfo();
            StampTreeNode node = new StampTreeNode(stampInfo);
            root.add(node);
        }

        DefaultTreeModel model = (DefaultTreeModel) this.getModel();
        model.reload(root);

        setFetched(true);
    }
}
