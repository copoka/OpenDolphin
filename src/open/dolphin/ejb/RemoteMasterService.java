/*
 * Created on 2004/10/13
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package open.dolphin.ejb;

import java.util.Collection;

import open.dolphin.dto.MasterSearchSpec;

/**
 * RemoteMasterService
 *
 * @author Minagawa,Kazushi
 *
 */
public interface RemoteMasterService {
    
    /**
     * �}�X�^����������B
     *
     * @param spec �����d�l
     * @return �Y���}�X�^�̃R���N�V����
     */
    public Collection getMaster(MasterSearchSpec spec);
}
