package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.SimpleAddressModel;

/**
 *
 * @author kazm
 */
public class BasicInfoInspector {
    
    private JPanel basePanel; // ���̃N���X�̃p�l��
    private  JLabel nameLabel;
    private JLabel addressLabel;
    private Color maleColor;
    private Color femaleColor;
    private Color unknownColor;
    
    // Context ���̃C���X�y�N�^�̐e�R���e�L�X�g
    private ChartImpl context;


    /**
     * BasicInfoInspector�I�u�W�F�N�g�𐶐�����B
     */
    public BasicInfoInspector(ChartImpl context) {
        this.context = context;
        initComponent();
        update();
    }

    /**
     * ���C�E�A�g�̂��߂ɂ��̃C���X�y�N�^�̃R���e�i�p�l����Ԃ��B
     * @return �R���e�i�p�l��
     */
    public JPanel getPanel() {
        return basePanel;
    }

    /**
     * ���҂̊�{����\������B
     */
    private void update() {

        StringBuilder sb = new StringBuilder();
        sb.append(context.getPatient().getFullName());
        sb.append("  ");
        sb.append(context.getPatient().getAgeBirthday());
        nameLabel.setText(sb.toString());

        SimpleAddressModel address = context.getPatient().getAddress();
        if (address != null) {
            addressLabel.setText(address.getAddress());
        } else {
            addressLabel.setText("�@");
        }

        String gender = context.getPatient().getGenderDesc();

        Color color = null;
        if (gender.equals(IInfoModel.MALE_DISP)) {
            color = maleColor;
        } else if (gender.equals(IInfoModel.FEMALE_DISP)) {
            color = femaleColor;
        } else {
            color = unknownColor;
        }
        nameLabel.setBackground(color);
        addressLabel.setBackground(color);
        basePanel.setBackground(color);
    }

    /**
     * GUI �R���|�[�l���g������������B
     */
    private void initComponent() {
        
        // ���ʂɂ���ĕς���p�l���̃o�b�N�O�����h�J���[
        Color foreground = ClientContext.getColor("patientInspector.basicInspector.foreground"); // new
        maleColor = ClientContext.getColor("color.male"); // Color.CYAN;
        femaleColor = ClientContext.getColor("color.female"); // Color.PINK;
        unknownColor = ClientContext.getColor("color.unknown"); // Color.LIGHT_GRAY;
        int[] size = ClientContext.getIntArray("patientInspector.basicInspector.size");
        
        nameLabel = new JLabel("�@");
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nameLabel.setForeground(foreground);
        nameLabel.setOpaque(true);
        
        addressLabel = new JLabel("�@");
        addressLabel.setHorizontalAlignment(SwingConstants.CENTER);
        addressLabel.setForeground(foreground);
        addressLabel.setOpaque(true);

        basePanel = new JPanel(new BorderLayout(0, 2));
        basePanel.add(nameLabel, BorderLayout.CENTER);
        basePanel.add(addressLabel, BorderLayout.SOUTH);
        basePanel.setBorder(BorderFactory.createEtchedBorder());
        Dimension dim = new Dimension(size[0], size[1]);
        basePanel.setMinimumSize(dim);
        basePanel.setMaximumSize(dim);
        basePanel.setPreferredSize(dim);
    }
    
}
