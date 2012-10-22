/*
 * KarteEditor2.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2003-2004 Digital Globe, Inc. All rights reserved.
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.*;
import java.awt.print.PageFormat;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.TooManyListenersException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultStyledDocument;

import open.dolphin.dao.SqlDaoFactory;
import open.dolphin.dao.SqlKarteSaverDao;
import open.dolphin.exception.DolphinException;
import open.dolphin.infomodel.AccessRight;
import open.dolphin.infomodel.DocInfo;
import open.dolphin.infomodel.ExtRef;
import open.dolphin.infomodel.ID;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.Karte;
import open.dolphin.infomodel.Module;
import open.dolphin.infomodel.ModuleInfo;
import open.dolphin.infomodel.ProgressCourse;
import open.dolphin.infomodel.Schema;
import open.dolphin.message.*;
import open.dolphin.message.MessageBuilder;
import open.dolphin.plugin.event.ClaimMessageEvent;
import open.dolphin.plugin.event.ClaimMessageListener;
import open.dolphin.plugin.event.MmlMessageEvent;
import open.dolphin.plugin.event.MmlMessageListener;
import open.dolphin.project.Project;
import open.dolphin.util.DesignFactory;
import open.dolphin.util.MMLDate;

import com.sun.image.codec.jpeg.*;

/**
 * 2���J���e�N���X�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class KarteEditor extends DefaultChartDocument {
    
    private static final int PANE_WIDTH       = 345;
    private static final int PANE_HEIGHT      = 32000;
    //private static final int PANE_HEIGHT      = 700;
    private static final int MARGIN_LEFT      = 10;
    private static final int MARGIN_TOP       = 10;
    private static final int MARGIN_RIGHT     = 10;
    private static final int MARGIN_BOTTOM    = 10;
    
    private static final String DOLPHIN_CODE_SYSTEM = "dolphin_2001-10-03";
    private static final String FUJITSU_RECICON     = "�x�m��";
    private static final String FUJITSU_CODE_SYSTEM = "Fujitsu SX-P V1";
    
    /** ���̃G�f�B�^�̃��f�� */
    private Karte model;
    
    /** ���̃G�f�B�^���\������R���|�[�l���g */
    private KartePane soaPane;
    private KartePane pPane;
    private Panel2 panel2;
	private StatusPanel statusPanel;
    
    /** �ҏW�\���ǂ����̃t���O */
    private boolean editable;
        
    /** �C������ true  */
    private boolean modify;
        
    /** Listeners to handle XML */
    private ClaimMessageListener claimListener;
    private MmlMessageListener mmlListener;
    private boolean sendMml;
    private boolean sendClaim;
    
    /** State Manager */
    private StateMgr stateMgr;
    
    
    /** Creates new KarteEditor2 */
    public KarteEditor() {
    }
    
    public Karte getModel() {
    	return model;
    }
    
    public void setModel(Karte model) {
    	this.model = model;
    }
    
	public void setNewModel(Karte m) {
		
		this.model = m;
		
		Runnable runner = new Runnable() {
			
			public void run() {
				
				String timeStamp = model.getDocInfo().getFirstConfirmDate();
				setTimestamp(timeStamp);
				
				SwingUtilities.invokeLater(new Runnable() {
					
					public void run() {
						KarteRenderer renderer = new KarteRenderer(soaPane, pPane);
						renderer.render(model);
					} 
				});
			}
		};
		
		Thread t = new Thread(runner);
		t.start();
	}   
    
    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    // Junzo SATO
    public void printPanel2(final PageFormat format) {
        String name = ((ChartPlugin)context).getPatient().getName();
        panel2.printPanel(format, 1, false, name);
    }
    
    public void printPanel2(final PageFormat format, final int copies, final boolean useDialog) {
        String name = ((ChartPlugin)context).getPatient().getName();
        panel2.printPanel(format, copies, useDialog, name);
    }
    //XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    
    public KartePane getSOAPane() {
        return soaPane;
    }
    
    public KartePane getPPane() {
        return pPane;
    }
    
    public void setEditable(boolean b) {
        editable = b;
    }
    
    public void addMMLListner(MmlMessageListener l) throws TooManyListenersException {
        if (mmlListener != null) {
            throw new TooManyListenersException();
        }
        mmlListener = l;
    }
 
    public void removeMMLListener(MmlMessageListener l) {
        if (mmlListener !=null && mmlListener == l) {
            mmlListener = null;
        }
    }
    
    public void addCLAIMListner(ClaimMessageListener l) throws TooManyListenersException {
        if (claimListener != null) {
            throw new TooManyListenersException();
        }
        claimListener = l;
    }
 
    public void removeCLAIMListener(ClaimMessageListener l) {
        if (claimListener !=null && claimListener == l) {
            claimListener = null;
        }
    }    
        
    public void setModify(boolean b) {
        modify = b;
    }
    
    public void enter() {
        super.enter();
        super.controlMenu();
        stateMgr.controlMenu();
    }
        
    public void setDirty(boolean b) {
        boolean b2 = (soaPane.isDirty() || pPane.isDirty()) ? true : false;
        stateMgr.setDirty(b);
    }
    
    public boolean isDirty() {
        return stateMgr.isDirty();
    }
            
    public void start() {
        
        setLayout(new BorderLayout());

        Dimension paneDimension = new Dimension(PANE_WIDTH, PANE_HEIGHT);
        Insets insets = new Insets(MARGIN_LEFT,MARGIN_TOP,MARGIN_RIGHT,MARGIN_BOTTOM);
        ChartMediator mediator = ((ChartPlugin)context).getChartMediator();
        
        soaPane = new KartePane(editable, mediator);
        soaPane.setMargin(insets);
        soaPane.setMaximumSize(paneDimension);
        //soaPane.setPreferredSize(paneDimension);
        soaPane.setParent(this);
        if (model != null) {
			// Schema �摜�Ƀt�@�C������t����̂��߂ɕK�v
			String docId = model.getDocInfo().getDocId();
			soaPane.setDocId(docId);
        }
        
        pPane = new KartePane(editable, mediator);
        pPane.setMargin(insets);
        pPane.setMaximumSize(paneDimension);
        //pPane.setPreferredSize(paneDimension);
        pPane.setParent(this);
        
        soaPane.setRole("soa", pPane);
        pPane.setRole("p",soaPane);
        
        panel2 = new Panel2();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
        panel2.add(soaPane);
        Component separator = Box.createRigidArea(new Dimension(1, 0));
        separator.setForeground(Color.lightGray);
        panel2.add(separator);
        panel2.add(pPane);

        JScrollPane scroller = new JScrollPane(panel2);
        scroller.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);        
        add(scroller,BorderLayout.CENTER);
        
        stateMgr = new StateMgr();
        statusPanel = ((ChartPlugin)context).getStatusPanel();
        
        // Model ��\������
        if (model != null) {
			displayModel();
        }
    }
    
    private void displayModel() {

		// Timestamp ��\������
		String timeStamp = MMLDate.getDateTime(new GregorianCalendar());
		
		// �C���̏ꍇ
		if (modify) {
			//�X�V: YYYY-MM-DDTHH:MM:SS (firstConfirmDate)
			StringBuffer buf = new StringBuffer();
			buf.append("�X�V: ");
			buf.append(MMLDate.getDateTime(new GregorianCalendar()));
			buf.append(" (");
			buf.append(model.getDocInfo().getFirstConfirmDate());
			buf.append(" )");
			timeStamp = buf.toString();			
		}
		
		// ���ꂪ�L�[
		setTimestamp(timeStamp);
    	
    	// ���e��\��
		if (model.getModule() != null) {
			KarteRenderer renderer = new KarteRenderer(soaPane, pPane);
			renderer.render(model);
		}
    }
    
    public void setDropTargetBorder(boolean b) {
        Color c = b ? DesignFactory.getDropOkColor() : this.getBackground();
        this.setBorder(BorderFactory.createLineBorder(c, 2));
    }
        
    public void setTimestamp(String timeStamp) {
        soaPane.init();
        pPane.init();
        soaPane.setTimestamp(timeStamp);
        pPane.setTimestamp(timeStamp);
    }
    
    public boolean copyStamp() {
        return pPane.copyStamp();
    }
    
    public void pasteStamp() {
        pPane.pasteStamp();
    }
                        
    private SaveParams getSaveParams(boolean sendMML) {
       
        // SOAPane ����ŏ��̂P�T�����𕶏��^�C�g���Ƃ��Ď擾����
        String text = soaPane.getTitle();
        if ( (text == null) || text.equals("") ) {
            text = "NOP";
        }

        // �_�C�A���O��\�����A�A�N�Z�X�����̕ۑ����̃p�����[�^���擾����
        SaveParams params = new SaveParams(sendMML);
        params.setTitle(text);
		params.setDepartment(model.getDocInfo().getClaimInfo().getDepartment());
        params.setPrintCount(0);
        
        SaveDialog sd = (SaveDialog)Project.createSaveDialog(getParentFrame(), params);
        sd.show();
        params = sd.getValue();
        sd.dispose();
        
        return params;
    } 
        
    public void save() {
        
        try {
            // ����������Ă��Ȃ����̓��^�[������
            if (! stateMgr.isDirty()) {
                debug("Empty karte, return");
                return;
            }

            // MML���M�p�̃}�X�^ID���擾����
            // �P�[�X�P HANIWA �����@facilityID + patientID
            // �P�[�X�Q HIGO ����    �n��ID ���g�p
            ID masterID = Project.getMasterId(context.getPatient().getId());
            if (masterID == null) {
                //  �n��ID���t�Ԃ���Ă��Ȃ��P�[�X
                //  2003-08-14 ���̏ꍇ�̓J���e�͕ۑ����AMML ���M�͂��Ȃ�
                //  return;
                debug("Master ID is null");
            }

            sendMml = (Project.getSendMML() && masterID != null && mmlListener != null) ? true : false;
            sendClaim = (!modify && Project.getSendClaim() && claimListener != null) ? true : false;

            // �ۑ��_�C�A���O��\�����A�p�����[�^�𓾂�
            SaveParams params = getSaveParams(sendMml);

            // �L�����Z���̏ꍇ�̓��^�[��
            if (params == null) {
                debug("Cancels to save the karte");
                return;
            }
            
            save2(params);
            
        
        } catch (DolphinException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
    
	private void save2(final SaveParams params) throws DolphinException {
		
		// DocInfo
		DocInfo docInfo = model.getDocInfo();
				
		// ConfirmDate
		String confirmDate = MMLDate.getDateTime(new GregorianCalendar());
		debug("Confirm Date: " + confirmDate);
		docInfo.setConfirmDate(confirmDate);
		if (! modify) {
			docInfo.setFirstConfirmDate(confirmDate);
			
			// Claim �p�� UUID �𐶐�����
			docInfo.getClaimInfo().setUid(Project.createUUID());
		}
		
		// Status ���ۑ����m��ۑ���
		// ����͊m��ۑ��̂݃T�|�[�g
		docInfo.setStatus(DocInfo.TT_FINAL);
		
		// title
		docInfo.setTitle(params.getTitle());
		
		// �f�t�H���g�̃A�N�Z�X����ݒ������
		AccessRight ar = new AccessRight();
		ar.setPermission("all");
		ar.setLicenseeCode("creator");
		ar.setLicenseeName("�L�ڎҎ{��");
		ar.setLicenseeCodeType("facilityCode");
		docInfo.addAccessRight(ar);
		
		// ���҂̃A�N�Z�X����ݒ������
		if (params.isAllowPatientRef()) {
			ar = new AccessRight();
			ar.setPermission("read");
			ar.setLicenseeCode("patient");
			ar.setLicenseeName("��L�ڎ�(����)");
			ar.setLicenseeCodeType("personCode");
			docInfo.addAccessRight(ar);
		}
		
		// �f�×����̂���{�݂̃A�N�Z�X����ݒ������
		if (params.isAllowClinicRef()) {
			ar = new AccessRight();
			ar.setPermission("read");
			ar.setLicenseeCode("experience");
			ar.setLicenseeName("�f�×��̂���{��");
			ar.setLicenseeCodeType("facilityCode");
			docInfo.addAccessRight(ar);
		}		
		
		// DEBUG
		debug("patientId: " + model.getPatient().getId());
		debug("docId: " + model.getDocInfo().getDocId());
		debug("firstConfirmDate: " + model.getDocInfo().getFirstConfirmDate());
		debug("confirmDate: " + model.getDocInfo().getConfirmDate());
		debug("docType: " + model.getDocInfo().getDocType());
		debug("title: " + model.getDocInfo().getTitle());
		debug("purpose: " + model.getDocInfo().getPurpose());
		debug("department: " + model.getDocInfo().getClaimInfo().getDepartment());
		debug("insuranceClass: " + model.getDocInfo().getClaimInfo().getInsuranceClass());
		debug("version: " + model.getDocInfo().getVersion().getVersionNumber());
		if (model.getDocInfo().getParentId() != null) {
			debug("parentId: " + model.getDocInfo().getParentId().getId());
			debug("parentIdRelation: " + model.getDocInfo().getParentId().getRelation());
		}
		debug("creatorId: " + model.getDocInfo().getCreator().getId());
		debug("creatorName: " + model.getDocInfo().getCreator().getName());
		debug("creatorLicense: " + model.getDocInfo().getCreator().getLicense());
		debug("status: " + model.getDocInfo().getStatus());
		
		// ProgressCourseModule �� ModuleInfo ��ۑ����Ă���
		ModuleInfo[] progressInfo = model.getModuleInfo("progressCourse");
		if (progressInfo == null) {
			// ���݂��Ȃ��ꍇ�͐V�K�ɍ쐬����
			progressInfo = new ModuleInfo[2];
			ModuleInfo mi = new ModuleInfo();
			mi.setName("progressCourse");
			mi.setEntity("progressCourse");
			mi.setRole("soaSpec");
			progressInfo[0] = mi;
			mi = new ModuleInfo();
			mi.setName("progressCourse");
			mi.setEntity("progressCourse");
			mi.setRole("pSpec");
			progressInfo[1] = mi;
		}
		
		// Clear
		model.setModule(null);
		model.setSchema(null);
    	
    	// SOAPane ���_���v�� model �ɒǉ�����
		KartePaneDumper dumper = new KartePaneDumper();
		KarteStyledDocument doc = (KarteStyledDocument)soaPane.getDocument();
		dumper.setTopFreePos(doc.getTopFreePos());
		dumper.dump(doc);
		Module[] soa = dumper.getModule();
		if (soa != null) {
			model.addModule(soa);	
		}
				
		// ProgressCourse SOA �𐶐�����
		ProgressCourse pc = new ProgressCourse();
		pc.setFreeText(dumper.getSpec());
		Module progressSoa = new Module();
		progressSoa.setModuleInfo(progressInfo[0]);
		progressSoa.setModel(pc);
		model.addModule(progressSoa);
		
		// Schema ��ǉ�����
		Schema[] schemas = dumper.getSchema();
		if (schemas != null) {
			//�ۑ��̂��� Icon �� JPEG �ɕϊ�����
			 for (int i = 0; i < schemas.length; i++) {
				byte[] jpegByte = getJPEGByte(schemas[i].getIcon().getImage());
				schemas[i].setJPEGByte(jpegByte);
				schemas[i].setIcon(null);
				String fileName = model.getDocInfo().getDocId() + "-" + i + ".jpg";
				schemas[i].setFileName(fileName);
				ExtRef ref = (ExtRef)schemas[i].getModel();
				ref.setHref(fileName);
			 }
		 	model.setSchema(schemas);
		}
		debug(dumper.getSpec());
    	
		// PPane ���_���v�� model �ɒǉ�����
		dumper = new KartePaneDumper();
		doc = (KarteStyledDocument)pPane.getDocument();
		dumper.setTopFreePos(doc.getTopFreePos());
		dumper.dump((DefaultStyledDocument)pPane.getDocument());
		Module[] plan = dumper.getModule();
		
		if (plan != null) {
			model.addModule(plan);
		} else {
			sendClaim = false;
		}
		
		// ProgressCourse P �𐶐�����
		pc = new ProgressCourse();
		pc.setFreeText(dumper.getSpec());
		Module progressP = new Module();
		progressP.setModuleInfo(progressInfo[1]);
		progressP.setModel(pc);
		model.addModule(progressP);
		
		// Setup the moduleInfo
		setupModuleInfo(model, modify);
		
		// �ۑ�
		final SqlKarteSaverDao dao = (SqlKarteSaverDao)SqlDaoFactory.create(this, "dao.karteSaver");
		
		Runnable r = new Runnable() {
			
			public void run() {
				
				SwingUtilities.invokeLater(new Runnable() {
					
					public void run() {
						statusPanel.start("�ۑ����Ă��܂�...");
					}
					
				});
				
				dao.setKarte(model);
				dao.setPvtOid( ((ChartPlugin)context).getPatientVisit().getNumber());
				dao.setModify(modify);
				dao.doWork();
				
				// Print the DML
				writeDml(model); 
				
				SwingUtilities.invokeLater(new Runnable() {
					
					public void run() {
						
						// ���
						int copies = params.getPrintCount();
						if (copies > 0) {
							statusPanel.setMessage("������Ă��܂�...");
							printPanel2(ChartPlugin.pageFormat, copies, false);
						}
						
						statusPanel.stop("");
						
						// �ҏW�s�ɐݒ肷��
						soaPane.setEditableProp(false);
						pPane.setEditableProp(false);

						// ��ԑJ�ڂ���
						setDirty(false);
						
						//��v�I�����Z�b�g����
						((ChartPlugin)context).setClaimSent(true);
					}
				});
			}
		};
		
		Thread t = new Thread(r);
		t.start();
	}
	
	private void setupModuleInfo(Karte model, boolean bModify) {
		
		String confirmDate = model.getDocInfo().getConfirmDate();
		
		Module[] module = model.getModule();
		ModuleInfo mInfo = null;
		
		for (int i = 0; i < module.length; i++) {
			
			mInfo = module[i].getModuleInfo();
			
			if (bModify) {
				// �ύX�̏ꍇ�AparentId=oldModuleId, firstConfirmDate=oldFirst
				mInfo.setFirstConfirmDate(mInfo.getFirstConfirmDate());
				mInfo.setParentId(mInfo.getModuleId());
				mInfo.setParentIdRelation("oldEdition");
				
			} else {
				mInfo.setFirstConfirmDate(confirmDate);
				mInfo.setParentId(null);
				mInfo.setParentIdRelation(null);
			}
			
			// moduleId, confirmdate �͂��̓x���ƂɐV�K
			mInfo.setModuleId(Project.createUUID());
			mInfo.setConfirmDate(confirmDate);
		}
	}
	
	private void writeDml(Karte model) {
		
		DmlMessageBuilder builder = new DmlMessageBuilder();
		String dml = builder.build((IInfoModel)model);
		debug(dml);
		
		if (sendClaim) {
			MessageBuilder cb = new MessageBuilder();
			cb.setTemplateFile("claim.vm");
			String claimMessage = cb.build(dml);
			debug(claimMessage);
			ClaimMessageEvent cvt = new ClaimMessageEvent(this);
			cvt.setClaimInstance(claimMessage);
			cvt.setPatientId(model.getPatient().getId());
			cvt.setPatientName(model.getPatient().getName());
			cvt.setPatientSex(model.getPatient().getGender());
			cvt.setTitle(model.getDocInfo().getTitle());
			cvt.setConfirmDate(model.getDocInfo().getConfirmDate());
			claimListener.claimMessageEvent(cvt);
		}
		
		if (sendMml) {
			MessageBuilder mb = new MessageBuilder();
			mb.setTemplateFile("mml2.3.vm");
			String mmlMessage = mb.build(dml);
			debug(mmlMessage);
			MmlMessageEvent cvt = new MmlMessageEvent(this);
			cvt.setMmlInstance(mmlMessage);
			cvt.setPatientId(model.getPatient().getId());
			cvt.setPatientName(model.getPatient().getName());
			cvt.setPatientSex(model.getPatient().getGender());
			cvt.setTitle(model.getDocInfo().getTitle());
			cvt.setConfirmDate(model.getDocInfo().getConfirmDate());
			cvt.setGroupId(model.getDocInfo().getDocId());
			cvt.setSchema(model.getSchema());
			cvt.setContentInfo(model.getDocInfo().getDocType());
			mmlListener.mmlMessageEvent(cvt);	
		}
	}
                
    /**
     * Courtesy of Junzo SATO
     */
    private byte[] getJPEGByte(Image image) {
    	
        byte[] ret = null;
        BufferedOutputStream writer = null;
        
        try {
            Dimension d = new Dimension(image.getWidth(this), image.getHeight(this));
            BufferedImage bf = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
            Graphics g = bf.getGraphics();
            g.setColor(Color.white);
            g.drawImage(image, 0, 0, d.width, d.height, this);

            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            writer = new BufferedOutputStream(bo);
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(writer);
            encoder.encode(bf);
            writer.flush();
            writer.close();
            ret = bo.toByteArray();
            
        } catch (IOException e) {
            System.out.println("IOException while creating the JPEG image: " + e.toString());
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e2) {
                }
            }
        }
        return ret;
    } 
            
    /**
     * ���̃G�f�B�^�̒��ۏ�ԃN���X
     */
    protected abstract class EditorState {
        
        public EditorState() {
        }
        
        public abstract void controlMenu();
    }
    
    /**
     * No dirty ��ԃN���X
     */ 
    protected final class NoDirtyState extends EditorState {
        
        public NoDirtyState() {
        }
        
        public void controlMenu() {
            ChartMediator mediator = ((ChartPlugin)context).getChartMediator();
            mediator.saveKarteAction.setEnabled(false);     // �ۑ�
            //mediator.orderAction.setEnabled(false);         // �I�[�_�̂ݔ��s
            mediator.printAction.setEnabled(false);         // ���
        }
    }
    
    /**
     * Dirty ��ԃN���X
     */
    protected final class DirtyState extends EditorState {
        
        public DirtyState() {
        }
        
        public void controlMenu() {
            ChartMediator mediator = ((ChartPlugin)context).getChartMediator();
            mediator.saveKarteAction.setEnabled(true);      // �ۑ�
            //mediator.orderAction.setEnabled(true);          // �I�[�_�̂ݔ��s Bug!
            mediator.printAction.setEnabled(true);          // ���
        }
    }    
        
    /**
     * ��ԃ}�l�W��
     */
    protected final class StateMgr {
        
        private EditorState noDirtyState = new NoDirtyState();
        private EditorState dirtyState = new DirtyState();
        private EditorState currentState;
        
        public StateMgr() {
            currentState = noDirtyState;
        }
        
        public boolean isDirty() {
            return currentState == dirtyState ? true : false;
        }
        
        public void setDirty(boolean b) {
            currentState = b ? dirtyState : noDirtyState;
            currentState.controlMenu();
        }
        
        public void controlMenu() {
            currentState.controlMenu();
        }
    }
}