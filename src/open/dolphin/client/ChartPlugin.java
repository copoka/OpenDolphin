/*
 * ChartPlugin.java
 * Copyright 2001,2002 Dolphin project. All Rights Reserved.
 * Copyright 2004 Digital Globe, Inc. All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *	
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *	
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package open.dolphin.client;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.beans.*;
import java.util.*;
import open.dolphin.dao.*;
import open.dolphin.infomodel.*;
import open.dolphin.order.MMLTable;
import open.dolphin.plugin.*;
import open.dolphin.plugin.event.*;
import open.dolphin.project.*;
import open.dolphin.util.*;
import java.awt.print.*;
import javax.media.jai.*;
import java.awt.image.*;

/**
 * 2���J���e�A���a���A�������ʗ��𓙁A���҂̑����I�f�[�^��񋟂���N���X�B
 * 
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public final class ChartPlugin extends AbstractFramePlugin implements IChartContext {
    
    // �t���[���T�C�Y
    private static int FRAME_WIDTH          = 724;
    private static int FRAME_HEIGHT         = 740;
    
    // Placement �萔
    private static final int OFFSET_X       = 7;
    private static final int OFFSET_Y       = 0;
    private static final int CICLE_COUNT    = 1;
   
    // Chart �C���X�^���X���Ǘ�����x�N�g��
    private static int chartCount;
	private static ArrayList chartList = new ArrayList(10);
                
    // �h�L�������g���i�[����^�u�p�l��
	private JTabbedPane tabbedPane;
	private ArrayList documents;
	
	// ��b�I�f�Ï���\������p�l��
    private BasicInfoPanel basicInfo;
    
    // �����󋵓���\�����鋤�ʂ̃p�l��
    private StatusPanel statusPanel;
    
    // ���җ��@���
    private PatientVisit pvt;
    
    // Read Only �̎� true
    private boolean readOnly;
    
    // �����T�|�[�g
    private PropertyChangeSupport boundSupport;
    
    // ���ʂ� MEDIATOR
    private ChartMediator mediator;
    
    // State Mgr
    private StateMgr stateMgr;
    
    // ���̃`���[�g�ŋ��ʗ��p���� Data Access Object
    private SqlKarteDao karteDao;
    
    // MMLEvent listener
    private MmlMessageListener mmlListener;
    
    // CLAIM event listener
    private ClaimMessageListener claimListener;
        
    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    // Junzo SATO
    // because this field is referred from the KarteEditor when saving the document,
    // this is set to public and static.
    public static PageFormat pageFormat = null;
    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    
    /** Creates new ChartService */
    public ChartPlugin() {
    	
        //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        // Junzo SATO
        PrinterJob printJob = PrinterJob.getPrinterJob();
        if (printJob != null && pageFormat == null) {
            // set default format
            pageFormat = printJob.defaultPage();
        }
        //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    }
            
    public void addPropertyChangeListener(String prop, PropertyChangeListener l) {
    	if (boundSupport == null) {
			boundSupport = new PropertyChangeSupport(this);
    	}
        boundSupport.addPropertyChangeListener(prop, l);
    }
    
    public void removePropertyChangeListener(String prop, PropertyChangeListener l) {
		if (boundSupport == null) {
			boundSupport = new PropertyChangeSupport(this);
		}
        boundSupport.removePropertyChangeListener(prop, l);
    }   
            
    public StatusPanel getStatusPanel() {
        return statusPanel;
    }
    
    public void initComponent() {
    	
        statusPanel = new StatusPanel();
        basicInfo = new BasicInfoPanel(pvt.getPatient());
        mediator = new ChartMediator(this);
        
        // Tool panel
        JPanel toolPanel = createMenu(this, basicInfo);
        
        // Document tab
		documents = new ArrayList(12);
        tabbedPane = loadDocuments();
        tabbedPane.setSelectedIndex(0);
        
        // Layouts
        Container c = this.getContentPane();
        c.add(toolPanel, BorderLayout.NORTH);
        c.add(tabbedPane, BorderLayout.CENTER);
        c.add(statusPanel, BorderLayout.SOUTH);
    }
    
    public void start() {
    	
    	Runnable r = new Runnable() {
    		
    		public void run() {
    			
    			rStart();
    			
    			SwingUtilities.invokeLater(new Runnable() {
    				
    				public void run() {
						// Gets document-history
					 	KarteBrowser browser = (KarteBrowser)tabbedPane.getComponentAt(0);
					 	browser.getHistory();
        
					 	//pvt.setState(WatingListService.TT_OPENED);
					 	//boundSupport.firePropertyChange("pvtNumber", -1, pvt.getNumber());
					 	chartList.add(this);  
						stateMgr = new StateMgr();
    				}
    			});
    			if (pvt.getState() != WatingListService.TT_CLAIM_SENT) {
					pvt.setState(WatingListService.TT_OPENED);
					boundSupport.firePropertyChange("pvtNumber", -1, pvt.getNumber());
    			}
    		}
    	};
    	
		Thread t = new Thread(r);
    	t.start();
    }
    
    public void rStart() {
    	                     
        // MML ���M Queue
        if (Project.getSendMML()) {
            mmlListener = (MmlMessageListener)ClientContext.getPlugin("mainWindow.sendMml");
        }
        
        // CLAIM ���M Queue
        if (Project.getSendClaim()) {
            claimListener = (ClaimMessageListener)ClientContext.getPlugin("mainWindow.sendClaim");
        }
        
        // Place this
        int n = chartCount % CICLE_COUNT;
        int x = n * OFFSET_X;
        int y = n * OFFSET_Y;
        this.setLocation(x, y);
        this.setSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        
        // Gets BaseClinicInfo
        String pid = pvt.getPatient().getId();
        BaseClinicModule base = getKarteDao().getBaseClinicModule(pid);
        basicInfo.setBaseClinicModule(base);
        LifestyleModule life = getKarteDao().getLifestyleModule(pid);
        basicInfo.setLifestyleModule(life);
        basicInfo.display();
                
        // Gets document-history
		//KarteBrowser browser = (KarteBrowser)tabbedPane.getComponentAt(0);
        //browser.getHistory();
        
        //pvt.setState(WatingListService.TT_OPENED);
        //boundSupport.firePropertyChange("pvtNumber", -1, pvt.getNumber());
        
        setVisible(true);
        
		//browser.getHistory();
		
		// ChartList �֒ǉ�
		//chartList.add(this);  
		//stateMgr = new StateMgr();
		//pvt.setState(WatingListService.TT_OPENED);
		//boundSupport.firePropertyChange("pvtNumber", -1, pvt.getNumber());
    }
        
    public MmlMessageListener getMMLListener() {
        return mmlListener;
    }
    
    public ClaimMessageListener getCLAIMListener() {
        return claimListener;
    }    
    
    public void setPatientVisit(PatientVisit pvt) {
        this.pvt = pvt;
    }
    
    public PatientVisit getPatientVisit() {
        return pvt;
    }
    
    public boolean isReadOnly() {
        return readOnly;
    }
    
    public void setReadOnly(boolean b) {
        readOnly = b;
    }
            
    public Patient getPatient() {
    	return pvt.getPatient();
    }
    
    public void setClaimSent(boolean b) {
        if (b) {
            pvt.setState(WatingListService.TT_CLAIM_SENT);
            boundSupport.firePropertyChange("pvtNumber", -1, pvt.getNumber());
        }
    }
    
    public ChartMediator getChartMediator() {
        return mediator;
    }
    
    public void controlMenu() {
        stateMgr.controlMenu();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private JTabbedPane loadDocuments() {
        
        JTabbedPane tab = new JTabbedPane();
        
        String[] docs = ClientContext.getStringArray("chart.documents");
        int len = docs.length;
        DocumentProxy proxy;
        String[] params;
        for (int i = 0; i < len; i++) {
            params = ClientContext.getStringArray(docs[i]);
            proxy = new DocumentProxy(params[0],
                                      params[1]);
            documents.add(proxy);
            tab.addTab(proxy.getTitle(), null);
        }
        
        tab.setSelectedIndex(tab.getTabCount()-1);
        tab.addChangeListener(new ChangeListener() {
            
            public void stateChanged(ChangeEvent e) {
                JTabbedPane tabbedPane = (JTabbedPane)e.getSource();
                int index = tabbedPane.getSelectedIndex();
                Component c = tabbedPane.getComponentAt(index);
                if (c == null) {
                    DocumentProxy proxy = (DocumentProxy)documents.get(index);
                    proxy.setChartContext(ChartPlugin.this);
                    proxy.start();
                    tabbedPane.setComponentAt(index, proxy.getUI());
                }
                else {
                    IChartDocument doc = (IChartDocument)c;
                    doc.enter();
                }
            }
        });
        
        return tab;
    }
    
    private void selectLastTab() {
        int count = tabbedPane.getTabCount();
        count--;
        tabbedPane.setSelectedIndex(count);
    }
    
    private void selecFirstTab() {
        tabbedPane.setSelectedIndex(0);
    } 
    
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * �V�K�i�󔒂́j�J���e���쐬����
     */
    public void newKarte() {
                 
        // �_�C�A���O���I�[�v�����A�ی���I��������
        NewKarteParams params = getNewKarteParams(pvt.getDepartment());
        if (params == null) {
        	// �V�K�J���e�쐬���L�����Z�����ꂽ�ꍇ
            return;
        }
        
        // CaretEvent �ŃX�^���v�̑I�����N���A����邽�ߐ�ɃR�s�[���Ă���
        KarteBrowser browser = (KarteBrowser)tabbedPane.getComponentAt(0);     
        boolean copy = browser.copyStamp();
        
        // Karte (Model) ���쐬����
        Karte model = new Karte();
        model.setPatient(getPatientVisit().getPatient()); 	// patient
        DocInfo docInfo = model.getDocInfo();
        docInfo.setDocId(Project.createUUID());				// docId
        //docInfo.setDocType(model.getDocType());			// docType
        docInfo.setPurpose("record");						// purpose
        
        // ClaimInfo
        ClaimInfo claimInfo = new ClaimInfo();
        docInfo.setClaimInfo(claimInfo);
        
		// ClaimInfo-department
		String dept = pvt.getDepartment();
		claimInfo.setDepartment(dept);
		claimInfo.setDepartmentId(MMLTable.getDepartmentCode(dept));
		
		// ClaimInfo-insurance
		InsuranceClass insurance = params.getDInsuranceInfo().getInsuranceClass();
		claimInfo.setInsuranceClass(insurance.getInsuranceClass());
		claimInfo.setInsuranceClassCode(insurance.getClassCode());
		claimInfo.setInsuranceUid(params.getDInsuranceInfo().getUid());
		                
        // Creator
        docInfo.setCreator(Project.getCreatorInfo());
        
        // Version
        Version version = new Version();
        version.initialize();
        docInfo.setVersion(version);
        
		// KarteEditor�𐶐�����
        KarteEditor editor = new KarteEditor();
        editor.setChartContext(this);               // Context
        editor.setEditable(true);                   // Editable prop
               
        try {
            editor.addMMLListner(mmlListener);      // Listeners to send XML
            editor.addCLAIMListner(claimListener);
        
        } catch (TooManyListenersException e) {
        	System.out.println(e);
        	e.printStackTrace();
        }
        
        // Starts editor
		editor.setModel(model);
        editor.start();
        
        // �h�L�������g�^�u�֒ǉ�����
        documents.add(editor);
        String title =  getTabTitle(params.getDepartment(), params.getDInsuranceInfo().toString());
        tabbedPane.addTab(title, editor);
                
        // �I������Ă���X�^���v������΃R�s�[�y�[�X�g����       
        if (copy) {
            editor.pasteStamp();
        }
        
        selectLastTab();
    }
    
    /**
     * �\�����Ă���J���e���R�s�[���ĐV�K�J���e���쐬����
     */
    public void copyNewKarte() {
    	
		// �\�����Ă���J���e�� Model ���擾����
		KarteBrowser browser = (KarteBrowser)tabbedPane.getComponentAt(0);
		Karte oldModel = browser.getKarteModel();
		DocInfo oldDocInfo = oldModel.getDocInfo();    	
    	
		// �f�ÉȂƕی���I�� �f�ÉȂ͑O��̂�I��������ԂŃ_�C�A���O���J��
		NewKarteParams params = getNewKarteParams(oldDocInfo.getClaimInfo().getDepartment());
		if (params == null) {
			return;
		}
        
        // �V�K���f�����쐬���A�\������Ă��郂�f���̓��e���R�s�[����
		Karte newModel = new Karte();
		newModel.setPatient(getPatientVisit().getPatient());	// patient
		copyModel(oldModel, newModel);

		// �V DocInfo ��ݒ肷��
		DocInfo docInfo = newModel.getDocInfo();
		docInfo.setDocId(Project.createUUID());			// docId
		//docInfo.setDocType(newModel.getDocType());	// docType
		docInfo.setPurpose("record");					// purpose
		
		// ClaimInfo
		ClaimInfo claimInfo = new ClaimInfo();
		docInfo.setClaimInfo(claimInfo);
        
		// ClaimInfo-department
		String dept = pvt.getDepartment();
		claimInfo.setDepartment(dept);
		claimInfo.setDepartmentId(MMLTable.getDepartmentCode(dept));
		
		// ClaimInfo-insurance
		InsuranceClass insurance = params.getDInsuranceInfo().getInsuranceClass();
		claimInfo.setInsuranceClass(insurance.getInsuranceClass());
		claimInfo.setInsuranceClassCode(insurance.getClassCode());
		claimInfo.setInsuranceUid(params.getDInsuranceInfo().getUid());
		                
		// Creator
		docInfo.setCreator(Project.getCreatorInfo());
		
		// Version
		Version version = new Version();
		version.initialize();
		docInfo.setVersion(version);

        // Creates KarteEditor
        KarteEditor editor = new KarteEditor();
        editor.setChartContext(this);                   // Context
        editor.setEditable(true);                       // Editable prop
        
        try {
            editor.addMMLListner(mmlListener);          // Listeners to send XML
            editor.addCLAIMListner(claimListener);
        }
        catch (TooManyListenersException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        
        // Starts editor
        editor.setModel(newModel);
        editor.start();
        
        // �h�L�������g�^�u�փG�f�B�^��ǉ�����
        documents.add(editor);
        String title =  getTabTitle(params.getDepartment(), params.getDInsuranceInfo().toString());
        tabbedPane.addTab(title, editor);
        selectLastTab();
        
        oldDocInfo = null;
        oldModel = null;
    }
    
    /**
     * �\�����Ă���J���e���C������
     */
    public void modifyKarte() {
        
        // �\������Ă���J���e�� Model ���擾����
        KarteBrowser browser = (KarteBrowser)tabbedPane.getComponentAt(0);
        Karte oldModel = browser.getKarteModel();
        DocInfo oldDocInfo = oldModel.getDocInfo();
                
        // �V�������f���ɓ��e���R�s�[����
        Karte newModel = new Karte();
		newModel.setPatient(getPatientVisit().getPatient());			// patient
		copyModel(oldModel, newModel);

		// �V���� DocInfo ��ݒ肷��
        DocInfo newInfo = newModel.getDocInfo();
        newInfo.setDocId(Project.createUUID());							// �V docId
        newInfo.setFirstConfirmDate(oldDocInfo.getFirstConfirmDate());	// firstConfirmDate = old one
		newInfo.setDocType(oldDocInfo.getDocType());					// docType = old one
		newInfo.setPurpose(oldDocInfo.getPurpose());					// purpose = old one
        
        // ClaimInfo
        ClaimInfo newci = new ClaimInfo();
        newci.setDepartment(oldDocInfo.getClaimInfo().getDepartment());
        newci.setDepartmentId(MMLTable.getDepartmentCode(newci.getDepartment()));
		newci.setInsuranceClass(oldDocInfo.getClaimInfo().getInsuranceClass());
        newInfo.setClaimInfo(newci);
        
        // ParentId ��ݒ肷��
        ParentId parentId = new ParentId();
        parentId.setId(oldDocInfo.getDocId());							// id = = old docId
        parentId.setRelation("oldEdition");								// relation
        newInfo.setParentId(parentId);
		
		// CreatorId,name,license
		newInfo.setCreator(Project.getCreatorInfo());
		
		// Version
		Version newVersion = new Version();
		newVersion.setVersionNumber(oldDocInfo.getVersion().getVersionNumber());
		newVersion.incrementNumber();                                         // version number ++
		newInfo.setVersion(newVersion);
		
        // Editor �𐶐�����
        KarteEditor editor = new KarteEditor();
        editor.setChartContext(this);                   // Context
        editor.setEditable(true);                       // Editable prop
		editor.setModify(true);                         // Modify prop
                     
        try {
        	// MML ���M�͂��邪 CLAIM ���M�͂��Ȃ�  Dolphin Project
            editor.addMMLListner(mmlListener);
        }
        catch (TooManyListenersException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        
        // Starts editor
        editor.setModel(newModel);
        editor.start();
    
        // �h�L�������g�^�u�֒ǉ�����
        String title = "�X�V";
        tabbedPane.addTab(title, editor);
        selectLastTab();
        
        oldDocInfo = null;
        oldModel = null;
    }
    
    private void copyModel(Karte oldModel, Karte newModel) {
		newModel.setModule(oldModel.getModule());
		newModel.setSchema(oldModel.getSchema());
    }
    
    /**
     * �J���e�쐬���Ƀ_�A�C���O���I�[�v�����A�ی���I��������B
     * @return NewKarteParams
     */
    private NewKarteParams getNewKarteParams(String dept) {
     
        NewKarteParams ret = null;
        
		DInsuranceInfo[] insurances = pvt.getInsurance();
        int len = insurances.length;  // TODO insurances == null
        ArrayList list = new ArrayList(len);
        DInsuranceInfo info;
        DInsuranceInfo self = null;
        String text = ClientContext.getString("insurance.name.self");
        for (int i = 0; i < len; i++) {
            info = insurances[i];
            if (info.getInsuranceClass() == null) {
                list.add(info);
                continue;
            }
            if (text.equals(info.getInsuranceClass().getInsuranceClass())) {
                self = info;
            }
            else {
                list.add(info);
            }
        }
        
        if ( (list.size() > 0) || (self != null) ) {
            if (self != null) {
                list.add(self);
            }
            text = ClientContext.getString("insurance.dialog.title");
            
            NewKarteDialog od = new NewKarteDialog(this, text, true);
            
            od.setDepartment(dept);
            od.setInsurance(list.toArray());
            od.start();
            
            ret = (NewKarteParams)od.getValue();
        }
        
        return  ret;
    }
    
    /**
     * �V�K�J���e�p�̃^�u�^�C�g�����쐬����
     * @param insurance �ی���
     * @return �^�u�^�C�g��
     */
    private String getTabTitle(String dept, String insurance) {
        StringBuffer buf = new StringBuffer();
        buf.append("�L��(");
        buf.append(dept);
        buf.append("�E");
        buf.append(insurance);
        buf.append(")");
        return buf.toString();
    }
    
    /**
     * TabbedPane �őI������Ă���h�L�������g��ۑ�����
     */
    public void save() {
        
        try {
            int index = tabbedPane.getSelectedIndex();
            IChartDocument doc = (IChartDocument)tabbedPane.getComponentAt(index);
            if (doc != null) {
                doc.save();
            }
        }
        catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    /**
     * �S�Ẵh�L�������g��ۑ�����
     */
    private void saveAll() {
        
        try {
            int count = tabbedPane.getTabCount();
            for (int i = 0; i < count; i++) {
                IChartDocument doc = (IChartDocument)tabbedPane.getComponentAt(i);
                if (doc != null) {
                    doc.save();
                }
            }
        }
        catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }        
    }
    
    private boolean isDirty() {
        boolean dirty = false;
        int count = tabbedPane.getTabCount();
        for (int i = 0; i < count; i++) {
            IChartDocument doc = (IChartDocument)tabbedPane.getComponentAt(i);
            if (doc != null && doc.isDirty()) {
                dirty = true;
                break;
            }
        }
        return dirty;
    }
    
    public void processWindowClosing() {
        
        if (isDirty()) {
            String saveAll = "�ۑ�";
            String discard = "�j��";
            String cancelText =  (String)UIManager.get("OptionPane.cancelButtonText");
            int option = JOptionPane.showOptionDialog(
							  this,
                              "���ۑ��̃h�L�������g������܂��B�ۑ����܂��� ?",
                              "Dolphin: ���ۑ�����", 
                              JOptionPane.DEFAULT_OPTION, 
                              JOptionPane.QUESTION_MESSAGE,
                              null, 
                              new String[]{saveAll, discard, cancelText},
                              saveAll
                              );
            
            switch(option) {
                case 0:
                    saveAll();
                    //close();
                    break;
                
                case 1:
                    stop();
                    break;
               
                case 2:
                    break;
            }
        }
        else {
            stop();
        }
    }
    
    public void stop() {
    	int state = pvt.getState();
        if (state != WatingListService.TT_CLAIM_SENT) {
            state = WatingListService.TT_CLOSED;
			pvt.setState(state); 
            boundSupport.firePropertyChange("pvtNumber", -1 , pvt.getNumber());
        }
        chartList.remove(this);
        super.stop();
    }
    
    public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
        
        if (! e.getPropertyName().equals("exitProp")) {
            return;
        }
        
        if (isDirty()) {
            JOptionPane.showMessageDialog(this,
                                     (String)"���ۑ��̃h�L�������g������܂��B�I���ł��܂���B",
                                     "Dolphin: �I��",
                                     JOptionPane.WARNING_MESSAGE);
            throw new PropertyVetoException("���ۑ��h�L�������g", e);
        }
        
    }
    
    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    // Courtesy of Junzo SATO
    public void print() {
              
        // print out the kerte editor which is an printable object
        //
        DefaultChartDocument doc = (DefaultChartDocument)tabbedPane.getSelectedComponent();
        if ( doc instanceof open.dolphin.client.KarteEditor ) {
            KarteEditor editor = (KarteEditor)doc;
            if (editor == null) {
                System.out.println("KarteEditor is null.");
                return;
            }
            editor.printPanel2(pageFormat);
        }
        // add printing the KarteBrowser 
        else if  (doc instanceof open.dolphin.client.KarteBrowser ) {
            KarteEditor editor = ((KarteBrowser)doc).getEditor();
            if (editor == null) {
                System.out.println("KarteEditor is null.");
                return;
            }
            editor.printPanel2(pageFormat);
        }
    }
      
    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    // Junzo SATO
    public void printerSetup() {
            
        new Thread() {
            public void run() {
                PrinterJob printJob = PrinterJob.getPrinterJob();
                if (pageFormat != null) {
                    // set current format
                    pageFormat = printJob.pageDialog(pageFormat);
                } else {
                    // set default format
                    pageFormat = printJob.defaultPage();
                    // the default page size is 'letter'.
                    // it would be better to set it to 'A4'...
                    pageFormat = printJob.pageDialog(pageFormat);
                }
            }
        }.start();        
    }
    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    public void insertImage() {
        //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        // Junzo SATO
        // show file shooser
        JFileChooser chooser = new JFileChooser();
        int selected = chooser.showOpenDialog(this);
        if (selected == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getPath();
            PlanarImage ri = JAI.create("fileload", path);
            if (ri == null) {
                System.out.println("Couldn't load " + path);
                return;
            }
            BufferedImage bf = ri.getAsBufferedImage();
            
            // insert image to the SOA Pane
            IChartDocument doc = (IChartDocument)tabbedPane.getSelectedComponent();
            if ( doc instanceof KarteEditor ) {
                KarteEditor editor = (KarteEditor)doc;
                if ((editor != null) && (editor.isEnabled() == true)) {
                    editor.getSOAPane().myInsertImage(bf);
                }
            }
        } else if (selected == JFileChooser.CANCEL_OPTION) {
            return;
        }       
        //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    }

    protected abstract class ChartState {
        
        public ChartState() {
        }
        
        public abstract void controlMenu();
    }
    
    protected final class ReadOnlyState extends ChartState {
        
        public ReadOnlyState() {
        }
        
        public void controlMenu() {
            mediator.newKarteAction.setEnabled(false);
            mediator.copyNewKarteAction.setEnabled(false);
            mediator.modifyKarteAction.setEnabled(false);
        }
    }
    
    protected final class NoInsuranceState extends ChartState {
        
        public NoInsuranceState() {
        }
        
        public void controlMenu() {
            mediator.newKarteAction.setEnabled(false);
            mediator.copyNewKarteAction.setEnabled(false);
        }
    }  
    
    protected final class OrdinalyState extends ChartState {
        
        public OrdinalyState() {
        }
        
        public void controlMenu() {
            mediator.newKarteAction.setEnabled(true);
        }
    }
    
    protected final class StateMgr {
        
        private ChartState readOnlyState = new ReadOnlyState();
        private ChartState noInsuranceState = new NoInsuranceState();
        private ChartState ordinalyState = new OrdinalyState();
        private ChartState currentState;
        
        public StateMgr() {
            if (isReadOnly()) {
                enterReadOnlyState();
                
            //} else if (! isInsuranceAvailable()) {
                //enterNoInsuranceState();
                
            } else {
                enterOrdinalyState();
            }
        }
        
        public void enterReadOnlyState() {
            currentState = readOnlyState;
            currentState.controlMenu();
        }
        
        public void enterNoInsuranceState() {
            currentState = noInsuranceState;
            currentState.controlMenu();
        }
        
        public void enterOrdinalyState() {
            currentState = ordinalyState;
            currentState.controlMenu();
        }     
        
        public void controlMenu() {
            currentState.controlMenu();
        }
    }        
    ////////////////////////////////////////////////////////////////////////////
    private JPanel createMenu(JFrame f, BasicInfoPanel base) {
     
        JPanel toolPanel = null;
        
        HashMap actionTable = new HashMap();
        
            // Red
        Action action = new StyledEditorKit.ForegroundAction("", Color.red);
        actionTable.put("redAction", action);
        
            // Blue
        action = new StyledEditorKit.ForegroundAction("", Color.blue);
        actionTable.put("blueAction", action);
        
            // Green
        action = new StyledEditorKit.ForegroundAction("", Color.green);
        actionTable.put("greenAction", action);

            // 10
        action = new StyledEditorKit.FontSizeAction("",10);;
        actionTable.put("s10Action", action);
        
            // 12
        action = new StyledEditorKit.FontSizeAction("",12);
        actionTable.put("s12Action", action);
        
            // 14
        action = new StyledEditorKit.FontSizeAction("",14);
        actionTable.put("s14Action", action);
        
            // 16
        action = new StyledEditorKit.FontSizeAction("",16);
        actionTable.put("s16Action", action);

            // 18
        action = new StyledEditorKit.FontSizeAction("",18);
        actionTable.put("s18Action", action);
        
            // 20
        action = new StyledEditorKit.FontSizeAction("",20);
        actionTable.put("s20Action", action);
        
            // 24
        action = new StyledEditorKit.FontSizeAction("",24);
        actionTable.put("s24Action", action);
        
            // Bold
        action = new StyledEditorKit.BoldAction();
        actionTable.put("boldAction", action);
        
            // Italic
        action = new StyledEditorKit.ItalicAction();
        actionTable.put("italicAction", action);
        
            // Underline
        action = new StyledEditorKit.UnderlineAction();
        actionTable.put("underlineAction", action);
        
        
        /**
         * Build
         */
        DefaultMenuBarBuilder builder = new DefaultMenuBarBuilder();
        builder.setActionListener(mediator);
        builder.setRoutingTarget(mediator);
        builder.setActionTable(actionTable);
        MenuBarDirector director = new MenuBarDirector(builder);
        
        try {
            // Build
            director.build(ClientContext.getResource("chartWindowMenuBar.xml"));
            mediator.registerActions(actionTable);
            
            JMenuBar mb = builder.getJMenuBar();
            if (mb == null) {
                System.out.println("menu bar is null");
            }
            
            f.setJMenuBar(mb);
            toolPanel = builder.getToolPanel();
            
            // Adds basicInfo panel
            toolPanel.add(base);            
            
        } catch (Exception e) {
            System.out.println("Exception while creating the menu bar of the chart window: " + e.toString());
            e.printStackTrace();
        }
        
        return toolPanel;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public static boolean isOpened(int number) {
        boolean opened = false;
        int size = chartList.size();
        ChartPlugin chart;
        for (int i = 0; i < size; i++) {
            chart = (ChartPlugin)chartList.get(i);
            if (chart.pvt.getNumber() == number) {
                opened = true;
                break;
            }
        }
        return opened;
    }
    
    public static int getChartCount() {
        return chartList.size();
    }
    
    public static ChartPlugin getChart(PatientVisit pvt) {
        
        ChartPlugin chart = null;
        
        int size = chartList.size();
        for (int i = 0; i < size; i++) {
            chart = (ChartPlugin)chartList.get(i);
            if (chart.pvt.getNumber() == pvt.getNumber()) {
                break;
            }
        }        
        return chart;
    }
    
    public static void removeChart(int number) {
        ChartPlugin chart = null;
        int size = chartList.size();
        for (int i = 0; i < size; i++) {
            chart = (ChartPlugin)chartList.get(i);
            if (chart.pvt.getNumber() == number) {
                chartList.remove(i);
                break;
            }
        }   
    }
    
    ///////////////////////////////////////////////////////////////
    
	private SqlKarteDao getKarteDao() {
		if (karteDao == null) {
			karteDao = (SqlKarteDao)SqlDaoFactory.create(this,"dao.karte");
		}
		return karteDao;
	}
    
	public ArrayList getDocumentHistory(String pid, String docType, String fromDate, boolean historyDisplay){
		System.out.println("getDocumentHistory");
		return getKarteDao().getDocumentHistory(pid, docType, fromDate, historyDisplay);
	}
	
	public ArrayList getDiagnosisHistory(String pid, String fromDate){
		return getKarteDao().getDiagnosisHistory(pid, fromDate);
	}
	
	public ArrayList getOrderHistory(String pid, String orderName, String fromDate, String toDate) {
		return getKarteDao().getOrderHistory(pid, orderName, fromDate, toDate);
	}
	
	public ArrayList getImageHistory(String pid, String fromDate, String toDate, Dimension iconSize) {
		return getKarteDao().getImageHistory(pid, fromDate, toDate, iconSize);
	}
	
	public Schema getSchema(String oid) {
		return getKarteDao().getSchema(oid);
	}
	
	public ArrayList getPvtHistory(String pid, String fromDate, String toDate) {
		return getKarteDao().getPvtHistory(pid, fromDate, toDate);
	}
	
	public ArrayList getOrderDateHistory(String pid, String orderName, String fromDate, String toDate) {
		return getKarteDao().getOrderDateHistory(pid, orderName, fromDate, toDate);
	}
	
	public ArrayList getImageDateHistory(String pid, String fromDate, String toDate) {
		return getKarteDao().getImageDateHistory(pid, fromDate, toDate);
	}
	
	public Karte getKarte(String docId) {
		return getKarteDao().getKarte(docId);
	}
	
	public ArrayList getAppointments(String pid, String fromDate, String toDate) {
		return getKarteDao().getAppointments(pid, fromDate, toDate);
	}
	
	public BaseClinicModule getBaseClinicModule(String pid) {
		return getKarteDao().getBaseClinicModule(pid);
	}
	
	public LifestyleModule getLifestyleModule(String pid) {
		return getKarteDao().getLifestyleModule(pid);
	}
}