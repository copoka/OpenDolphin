package open.dolphin.client.impl;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import open.dolphin.client.ClientContext;
import open.dolphin.infomodel.LabTestRowObject;
import open.dolphin.infomodel.LabTestValueObject;

/**
 *
 * @author Kazushi Minagawa, Digital Globe, Inc
 */
public class LabTestRenderer extends JLabel implements TableCellRenderer {

    // JTable�����_���p�̊�J���[
    private static final Color ODD_COLOR = ClientContext.getColor("color.odd");

    // JTable�����_���p�̋����J���[
    private static final Color EVEN_COLOR = ClientContext.getColor("color.even");

    private Color penCol;

    public LabTestRenderer() {
        setOpaque(true);
        setBackground(Color.white);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus, int row, int column) {

         if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
            
         } else {
             if (row % 2 == 0) {
                this.setBackground(EVEN_COLOR);
            } else {
                this.setBackground(ODD_COLOR);
            }
         }

        //-------------------------------------------------------
        if (value != null) {

            LabTestRowObject rowObj = (LabTestRowObject) value;

            if (column == 0) {

                // �e�X�g���ږ�(�P�ʁj��\������
                penCol = Color.black;
                setForeground(penCol);
                setText(rowObj.nameWithUnit());
                String toolTip = rowObj.getNormalValue() != null ? rowObj.getNormalValue() : "";
                setToolTipText(toolTip);

            } else {

                // column-1�Ԗڂ̒l�I�u�W�F�N�gwp���o��
                LabTestValueObject valueObj = rowObj.getLabTestValueObjectAt(column -1);

                String text = valueObj != null ? valueObj.getValue() : "";
                String flag = valueObj != null ? valueObj.getOut() : null;
                String toolTip = valueObj != null ? valueObj.concatComment() : "";

                if (flag != null && flag.startsWith("H")) {
                    penCol = Color.RED;
                } else if (flag != null && flag.startsWith("L")) {
                    penCol = Color.BLUE;
                } else if (!toolTip.equals("")) {
                    penCol = Color.MAGENTA;
                } else {
                    penCol = Color.black;
                }

                setForeground(penCol);
                setText(text);
                setToolTipText(toolTip);
            }

        } else {
            penCol = Color.black;
            setForeground(penCol);
            setText("");
            setToolTipText("");
        }
        //-------------------------------------------------------

        return this;
    }
    
}
