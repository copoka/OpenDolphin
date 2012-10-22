/*
 * StampBoxService.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2001, 2004 Digital Globe, Inc. All rights reserved.
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

import open.dolphin.dao.*;
import open.dolphin.exception.DolphinException;
import open.dolphin.plugin.*;
import open.dolphin.project.*;

import java.awt.*;
import java.util.*;
import java.io.*;

/**
 * StampBox�B
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class StampBoxService extends AbstractFramePlugin {
        
	private int DEFAULT_WIDTH   = 300;
	private int DEFAULT_HEIGHT  = 741;
	private String alertTitle = "�X�^���v��-" + ClientContext.getString("application.title");
    
	private JTabbedPane stampBox;
	private int treeCount;
	private int aspTreeCount;
	private int gcpTreeCount;
	//private boolean newTree;
    
	/** Creates new StampBoxService */
	public StampBoxService() {
	}
       
	public void initComponent() {
                                
		stampBox = new JTabbedPane();
		this.getContentPane().add(stampBox);
		loadLocalStampTree(stampBox);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int defaultX = (int)screen.getWidth() - DEFAULT_WIDTH;
		setToPreferenceBounds(defaultX, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}
    
	public JTabbedPane getStampBox() {
		return stampBox;
	}
    
	public void stop() {
		saveStampTree();
		super.stop();
	}
    
	private void loadLocalStampTree(JTabbedPane tabbedPane) {
    	
		try {
			// DB ���� StampTree �� �f�[�^��ǂݍ���
			String userId = Project.getUserId();
			SqlStampDao dao = (SqlStampDao)SqlDaoFactory.create(this, "dao.stamp");
            
			String treeXml = dao.getTree(userId);
			
			// ResultCode ���`�F�b�N����
			if (dao.getResultCode() != DaoBean.TT_NO_ERROR) {
				// Error �������Ă���ꍇ�͗�O���X���[���ďI���
				throw new DolphinException("StampTree �̓ǂݍ��݂��ł��܂���");
			}
						
			if (treeXml == null) {
				// �V�K���[�U�Ńf�[�^�x�[�X�� StampTree ���܂��Ȃ��ꍇ
				// �����c���[�����\�[�X����ǂݍ���
				debug("���� StampTree �����\�[�X����ǂݍ��݂܂�");
				loadInitialStampTree(tabbedPane);
				return;
			}
			
			//Build stampTree
			BufferedReader reader = new BufferedReader(new StringReader(treeXml));
			DefaultStampTreeBuilder builder = new DefaultStampTreeBuilder();
			StampTreeDirector director = new StampTreeDirector(builder);
			ArrayList localTrees = director.build(reader);
			reader.close();
						
			// StampBox(TabbedPane) �փ��X�g���Ɋi�[����
			treeCount = localTrees.size();
			StampTreePopupAdapter popAdapter = new StampTreePopupAdapter();
			StampTreePanel treePanel = null;
			StampTree stampTree = null;
            
			for (int i = 0; i < treeCount; i++) {
				stampTree = (StampTree)localTrees.get(i); 
				stampTree.setEdiatble(true);               
				stampTree.addMouseListener(popAdapter);
				stampTree.setStampBox(this);
				treePanel = new StampTreePanel(stampTree);
				tabbedPane.addTab(stampTree.getRootName(), treePanel);
			} 
			
		} catch (Exception e) {
			debug(e.toString());
			errorAlert("loadLocalStampTree", e.toString(), alertTitle);
			System.exit(1);
		}
	}
    
	private void loadInitialStampTree(JTabbedPane tabbedPane) {
		
		//���\�[�X�t�@�C������ǂݍ��ށi�����C���X�g�[�������j
		BufferedReader reader = null;
		
		try {
			InputStream in = ClientContext.getResourceAsStream("stamptree-seed.xml");
			reader = new BufferedReader(new InputStreamReader(in, "SHIFT_JIS"));

			DefaultStampTreeBuilder builder = new DefaultStampTreeBuilder();
			StampTreeDirector director = new StampTreeDirector(builder);
			ArrayList initialTrees = director.build(reader);
			reader.close();
			
			// StampBox(TabbedPane) �փ��X�g���Ɋi�[����
			treeCount = initialTrees.size();
			System.out.println("treeCount:" + treeCount);
			StampTreePopupAdapter popAdapter = new StampTreePopupAdapter();
			StampTreePanel treePanel = null;
			StampTree stampTree = null;
			
			 for (int i = 0; i < treeCount; i++) {
				 stampTree = (StampTree)initialTrees.get(i);
				 stampTree.setEdiatble(true);
				 stampTree.addMouseListener(popAdapter);
				 stampTree.setStampBox(this);
				 treePanel = new StampTreePanel(stampTree);
				 tabbedPane.addTab(stampTree.getRootName(), treePanel);
				 debug(i + " = " + stampTree.getRootName());
			 }			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
		}
	}
		
	private void saveStampTree() {
        
		// Local Tree �݂̂�ۑ�����
		if (treeCount == 0) {
			return;
		}
        
		DefaultStampTreeXmlBuilder builder = new DefaultStampTreeXmlBuilder();
		StampTreeXmlDirector director = new StampTreeXmlDirector(builder);
        
		ArrayList list = new ArrayList(treeCount);
		for (int i = 0; i < treeCount; i++) {
			StampTreePanel panel = (StampTreePanel)stampBox.getComponentAt(i);
			list.add(panel.getTree());
		}
		
		String treeXml = director.build(list);
        
		SqlStampDao dao = (SqlStampDao)SqlDaoFactory.create(this, "dao.stamp");
		
		boolean result = dao.putTree(Project.getUserId(), treeXml);
        
		if (! result) {
			String msg = "�X�^���v�c���[�̕ۑ����ł��܂���B�T�|�[�g�����ւ��A�����������B";
			String title = "�X�^���v�ۑ�";
			JOptionPane.showMessageDialog(null,
									 msg,
									 title,
									 JOptionPane.ERROR_MESSAGE);
		}else {
			System.out.println("stampBox: save tree ok");
		}
	}	
    
	public StampTree getStampTree(String category) {
		String rootName = ClientContext.getString("stampTree.category." + category);
		int count = treeCount;
		StampTree tree = null;
		StampTreePanel panel = null;
		boolean found = false;
		for (int i = 0; i < count; i++) {
			panel = (StampTreePanel)stampBox.getComponentAt(i);
			tree = panel.getTree();
			if (rootName.equals(tree.getRootName())) {
				found = true;
				break;
			}
		}
        
		return found ? tree : null;
	}   
    
	private void errorAlert(String method, String excetion, String title) {
		StringBuffer buf = new StringBuffer();
		buf.append(ClientContext.getString("stampTree.error.unknown"));
		buf.append(method);
		buf.append(": ");
		buf.append(excetion);
		JOptionPane.showMessageDialog(null,
									 buf.toString(),
									 title,
									 JOptionPane.ERROR_MESSAGE);
	}
}