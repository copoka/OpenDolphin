package open.dolphin.order;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import open.dolphin.infomodel.RegisteredDiagnosisModel;

/**
 * ���� Transferable �N���X�B
 * @author kazm
 */
public class RegisteredDiagnosisTransferable implements Transferable {

    public static DataFlavor registeredDiagnosisFlavor = new DataFlavor(open.dolphin.infomodel.RegisteredDiagnosisModel.class, "RegisteredDiagnosis");
    public static final DataFlavor[] flavors = {registeredDiagnosisFlavor};
    private RegisteredDiagnosisModel diagnosis;

    public RegisteredDiagnosisTransferable(RegisteredDiagnosisModel diagnosis) {
        this.diagnosis = diagnosis;
    }

    @Override
    public synchronized DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(registeredDiagnosisFlavor) ? true : false;
    }

    @Override
    public synchronized Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {

        if (flavor.equals(registeredDiagnosisFlavor)) {
            return diagnosis;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
