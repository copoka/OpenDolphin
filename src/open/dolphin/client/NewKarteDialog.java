/*
 * NewKarteDialog.java
 * Copyright (C) 2002 Dolphin Project. All rights reserved.
 * Copyright (C) 2003,2004 Digital Globe, Inc. All rights reserved.
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

import java.beans.EventHandler;
import javax.swing.*;
import javax.swing.event.*;

import open.dolphin.infomodel.PVTHealthInsuranceModel;

import java.awt.*;
import java.awt.event.*;
import java.util.prefs.Preferences;


/**
 * Dialog to select Health Insurance for new Karte.
 *
 * @author  Kazushi Minagawa, Digital Globe, Inc.
 */
public final class NewKarteDialog {
    
    private static final long serialVersionUID = -3463826098185685559L;
    
    private static final String OPEN_ANOTHER 		= "別ウィンドウで編集";
    private static final String ADD_TO_TAB 		= "タブパネルへ追加";
    private static final String EMPTY_NEW 		= "空白の新規カルテ";
    private static final String APPLY_RP 		= "前回処方を適用";
    private static final String ALL_COPY 		= "全てコピー";
    private static final String DEPARTMENT 		=  "診療科:";
    private static final String SELECT_INS 		=  "保険選択";
    private static final String LAST_CREATE_MODE 	= "newKarteDialog.lastCreateMode";
    private static final String FRAME_MEMORY 		= "newKarteDialog.openFrame";
    
    private NewKarteParams params;
    
    // GUI components
    private JButton okButton;
    private JButton cancelButton;
    private JRadioButton emptyNew;
    private JRadioButton applyRp;			// 前回処方を適用
    private JRadioButton allCopy;			// 全てコピー
    private JList insuranceList;
    private JLabel departmentLabel;
    private JRadioButton addToTab;			// タブパネルへ追加
    private JRadioButton openAnother;		// 別 Window へ表示
    
    private Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    
    private Frame parentFrame;
    private String title;
    private JPanel content;
    private JDialog dialog;
    private Object value;
    
    
    /** 
     * Creates new OpenKarteDialog 
     */
    public NewKarteDialog(Frame parentFrame, String title) {
        this.parentFrame = parentFrame;
        this.title = title;
        content = createComponent();
    }
    
    public void setValue(Object o) {
        
        this.params = (NewKarteParams) o;
        setDepartment(params.getDepartment());
        setInsurance(params.getInsurances());
        
        int lastCreateMode = prefs.getInt(LAST_CREATE_MODE, 0);
        boolean frameMemory = prefs.getBoolean(FRAME_MEMORY, true);
        
        switch (params.getOption()) {
            
            case BROWSER_NEW:
                applyRp.setEnabled(false);
                allCopy.setEnabled(false);
                emptyNew.setSelected(true);
                openAnother.setSelected(frameMemory);
                addToTab.setSelected(!frameMemory);
                ButtonGroup bg = new ButtonGroup();
                bg.add(openAnother);
                bg.add(addToTab);
                break;
                
            case BROWSER_COPY_NEW:
                selectCreateMode(lastCreateMode);
                bg = new ButtonGroup();
                bg.add(emptyNew);
                bg.add(applyRp);
                bg.add(allCopy);
                
                openAnother.setSelected(frameMemory);
                addToTab.setSelected(!frameMemory);
                bg = new ButtonGroup();
                bg.add(openAnother);
                bg.add(addToTab);
                break;
                
            case BROWSER_MODIFY:
                insuranceList.setEnabled(false);
                applyRp.setEnabled(false);
                allCopy.setEnabled(false);
                emptyNew.setEnabled(false);
                openAnother.setSelected(frameMemory);
                addToTab.setSelected(!frameMemory);
                bg = new ButtonGroup();
                bg.add(openAnother);
                bg.add(addToTab);
                // OK Button
                okButton.setEnabled(true);
                break;
                
            case EDITOR_NEW:
                applyRp.setEnabled(false);
                allCopy.setEnabled(false);
                emptyNew.setSelected(true);
                openAnother.setSelected(true);
                openAnother.setEnabled(false);
                addToTab.setEnabled(false);
                break;
                
            case EDITOR_COPY_NEW:
                selectCreateMode(lastCreateMode);
                bg = new ButtonGroup();
                bg.add(applyRp);
                bg.add(allCopy);
                bg.add(emptyNew);
                openAnother.setSelected(true);
                openAnother.setEnabled(false);
                addToTab.setEnabled(false);
                break;
                
            case EDITOR_MODIFY:
                insuranceList.setEnabled(false);
                applyRp.setEnabled(false);
                allCopy.setEnabled(false);
                emptyNew.setEnabled(false);
                openAnother.setSelected(true);
                openAnother.setEnabled(false);
                addToTab.setEnabled(false);
                break;
        }
    }
    
    public void start() {
        
        Object[] options = new Object[]{okButton, cancelButton};
        
        JOptionPane jop = new JOptionPane(
                content,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                options,
                okButton);
        
        dialog = jop.createDialog(parentFrame, title);
        dialog.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                insuranceList.requestFocusInWindow();
            }
        });
        dialog.setVisible(true);
    }
    
    public Object getValue() {
        return value;
    }
    
    private void setDepartment(String dept) {
        if (dept != null) {
            departmentLabel.setText(dept);
        }
    }
    
    private void setInsurance(Object[] o) {
        
        insuranceList.setListData(o);
        
        //
        // 保険が一つしかない場合はそれを選択する
        //
        if (o != null && o.length > 0) {
            int index = params.getInitialSelectedInsurance();
            if (index >=0 && index < o.length) {
                insuranceList.getSelectionModel().setSelectionInterval(index,index);
            }
        }
    }
    
    private IChart.NewKarteMode getCreateMode() {
        if (emptyNew.isSelected()) {
            return IChart.NewKarteMode.EMPTY_NEW;
        } else if (applyRp.isSelected()) {
            return IChart.NewKarteMode.APPLY_RP;
        } else if (allCopy.isSelected()) {
            return IChart.NewKarteMode.ALL_COPY;
        }
        return IChart.NewKarteMode.EMPTY_NEW;
    }
    
    private void selectCreateMode(int mode) {
        emptyNew.setSelected(false);
        applyRp.setSelected(false);
        allCopy.setSelected(false);
        if (mode == 0) {
            emptyNew.setSelected(true);
        } else if (mode == 1) {
            applyRp.setSelected(true);
        } else if (mode == 2) {
            allCopy.setSelected(true);
        }
    }
    
    protected JPanel createComponent() {
        
        // 診療科情報ラベル
        departmentLabel = new JLabel();
        JPanel dp = new JPanel();
        dp.setLayout(new BoxLayout(dp, BoxLayout.X_AXIS));
        dp.add(new JLabel(DEPARTMENT));
        dp.add(Box.createHorizontalStrut(11));
        dp.add(departmentLabel);
        
        // 保険選択リスト
        insuranceList = new JList();
        insuranceList.setFixedCellWidth(200);
        insuranceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        insuranceList.addListSelectionListener((ListSelectionListener) 
            EventHandler.create(ListSelectionListener.class, this, "insuranceSelectionChanged", "valueIsAdjusting"));
        
        JPanel ip = new JPanel(new BorderLayout(9, 0));
        ip.setBorder(BorderFactory.createTitledBorder(SELECT_INS));
        ip.add(insuranceList, BorderLayout.CENTER);
        ip.add(new JLabel(ClientContext.getImageIcon("addbk_32.gif")), BorderLayout.WEST);
        
        // 前回処方適用 / 全コピー / 空白
        emptyNew = new JRadioButton(EMPTY_NEW);
        applyRp = new JRadioButton(APPLY_RP);
        allCopy = new JRadioButton(ALL_COPY);
        ActionListener memory = (ActionListener) EventHandler.create(ActionListener.class, this, "memoryMode");
        emptyNew.addActionListener(memory);
        applyRp.addActionListener(memory);
        allCopy.addActionListener(memory);
        JPanel rpPanel = new JPanel();
        rpPanel.setLayout(new BoxLayout(rpPanel, BoxLayout.X_AXIS));
        rpPanel.add(applyRp);
        rpPanel.add(Box.createRigidArea(new Dimension(5,0)));
        rpPanel.add(allCopy);
        rpPanel.add(Box.createRigidArea(new Dimension(5,0)));
        rpPanel.add(emptyNew);
        rpPanel.add(Box.createHorizontalGlue());
        
        // タブパネルへ追加/別ウィンドウ
        openAnother = new JRadioButton(OPEN_ANOTHER);
        addToTab = new JRadioButton(ADD_TO_TAB);
        openAnother.addActionListener((ActionListener) EventHandler.create(ActionListener.class, this, "memoryFrame"));
        addToTab.addActionListener((ActionListener) EventHandler.create(ActionListener.class, this, "memoryFrame"));
        JPanel openPanel = new JPanel();
        openPanel.setLayout(new BoxLayout(openPanel, BoxLayout.X_AXIS));
        openPanel.add(openAnother);
        openPanel.add(Box.createRigidArea(new Dimension(5,0)));
        openPanel.add(addToTab);
        openPanel.add(Box.createHorizontalGlue());
        
        // ok
        String buttonText =  (String)UIManager.get("OptionPane.okButtonText");
        okButton = new JButton(buttonText);
        okButton.addActionListener((ActionListener) EventHandler.create(ActionListener.class, this, "doOk"));
        okButton.setEnabled(false);
        
        // Cancel Button
        buttonText =  (String)UIManager.get("OptionPane.cancelButtonText");
        cancelButton = new JButton(buttonText);
        //cancelButton.setMnemonic(KeyEvent.VK_C);
        cancelButton.addActionListener((ActionListener) EventHandler.create(ActionListener.class, this, "doCancel"));
                
        // 全体を配置
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(dp);
        panel.add(Box.createVerticalStrut(11));
        panel.add(ip);
        panel.add(Box.createVerticalStrut(11));
        panel.add(GUIFactory.createTitledPanel(rpPanel, "カルテ作成方法"));
        panel.add(Box.createVerticalStrut(11));
        panel.add(GUIFactory.createTitledPanel(openPanel, "カルテ編集ウインドウ"));
        panel.add(Box.createVerticalStrut(11));
        
        return panel;
    }
    
    /**
     * 保険選択リストにフォーカスする。
     */
    public void controlFocus(WindowEvent e) {
        insuranceList.requestFocusInWindow();
    }
    
    /**
     * 保険選択の変更を処理する。
     */
    public void insuranceSelectionChanged(boolean adjusting) {
        if (adjusting == false) {
            Object o = insuranceList.getSelectedValue();
            boolean ok = o != null ? true : false;
            okButton.setEnabled(ok);
        }
    }
    
    /**
     * カルテの作成方法をプレファレンスに記録する。
     */
    public void memoryMode() {
        
        if (emptyNew.isSelected()) {
            prefs.putInt(LAST_CREATE_MODE, 0);
        } else if (applyRp.isSelected()) {
            prefs.putInt(LAST_CREATE_MODE, 1);
        } else if (allCopy.isSelected()) {
            prefs.putInt(LAST_CREATE_MODE, 2);
        }
    }
    
    /**
     * カルテフレーム(ウインドウ)の作成方法をプレファレンスに記録する。
     */
    public void memoryFrame() {
        boolean openFrame = openAnother.isSelected();
        prefs.putBoolean(FRAME_MEMORY,openFrame);
    }
    
    /**
     * パラーメータを取得しダイアログの値に設定する。
     */
    public void doOk() {
        params.setDepartment(departmentLabel.getText());
        params.setPVTHealthInsurance((PVTHealthInsuranceModel)insuranceList.getSelectedValue());
        params.setCreateMode(getCreateMode());
        params.setOpenFrame(openAnother.isSelected());
        value = (Object) params;
        dialog.setVisible(false);
        dialog.dispose();
    }
    
    /**
     * キャンセルする。ダイアログを閉じる。
     */
    public void doCancel() {
        value = null;
        dialog.setVisible(false);
        dialog.dispose();
    }
    
    private JPanel createCmdPanel(JButton[] btns) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT,5, 0));
        for (JButton btn : btns) {
            panel.add(btn);
        }
        return panel;
    }
}