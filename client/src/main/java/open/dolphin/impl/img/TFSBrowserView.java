/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DefaultBrowserView.java
 *
 * Created on 2011/06/07, 21:06:46
 */

package open.dolphin.impl.img;

/**
 *
 * @author kazushi
 */
public class TFSBrowserView extends javax.swing.JPanel {

    /** Creates new form DefaultBrowserView */
    public TFSBrowserView() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dirLbl = new javax.swing.JLabel();
        refreshBtn = new javax.swing.JButton();
        settingBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        tfsCalendarBtn = new javax.swing.JButton();
        tfsListBtn = new javax.swing.JButton();
        tfsCloseBtn = new javax.swing.JButton();

        refreshBtn.setText("更新");

        settingBtn.setText("設定");

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(table);

        tfsCalendarBtn.setText("TFS Cal.");

        tfsListBtn.setText("TFS List");

        tfsCloseBtn.setText("TFS Close");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(20, 20, 20)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(dirLbl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 348, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(tfsListBtn)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(tfsCalendarBtn)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(tfsCloseBtn)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 25, Short.MAX_VALUE)
                        .add(refreshBtn)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(settingBtn))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 845, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(refreshBtn)
                    .add(settingBtn)
                    .add(dirLbl)
                    .add(tfsListBtn)
                    .add(tfsCalendarBtn)
                    .add(tfsCloseBtn))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel dirLbl;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton refreshBtn;
    private javax.swing.JButton settingBtn;
    private javax.swing.JTable table;
    private javax.swing.JButton tfsCalendarBtn;
    private javax.swing.JButton tfsCloseBtn;
    private javax.swing.JButton tfsListBtn;
    // End of variables declaration//GEN-END:variables

    public javax.swing.JLabel getDirLbl() {
        return dirLbl;
    }

    public javax.swing.JButton getRefreshBtn() {
        return refreshBtn;
    }

    public javax.swing.JButton getSettingBtn() {
        return settingBtn;
    }

    public javax.swing.JTable getTable() {
        return table;
    }

    public javax.swing.JButton getTfsCalendarBtn() {
        return tfsCalendarBtn;
    }

    public javax.swing.JButton getTfsListBtn() {
        return tfsListBtn;
    }

    public javax.swing.JButton getTfsCloseBtn() {
        return tfsCloseBtn;
    }
}
