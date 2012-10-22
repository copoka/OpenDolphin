package open.dolphin.client;

import java.util.concurrent.Callable;

/**
 * MainWindow �� Tool �v���O�C���N���X�B
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public interface MainTool extends MainService {
    
    public void enter();
    
    public Callable<Boolean> getStartingTask();
    
    public Callable<Boolean> getStoppingTask();
    
}
