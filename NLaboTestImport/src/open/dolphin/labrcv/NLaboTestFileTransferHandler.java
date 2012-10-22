package open.dolphin.labrcv;

import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.datatransfer.*;

import java.beans.PropertyChangeListener;
import javax.swing.*;
import open.dolphin.client.ClientContext;


/**
 * LaboTestFileTransferHandler
 *
 * @author kazushi Minagawa, Digital Globe, Inc.
 *
 */
public class NLaboTestFileTransferHandler extends TransferHandler {

    private static final String DAT_EXT = ".dat";
    
    private DataFlavor fileFlavor;
    private NLaboTestImporter context;
    
    public NLaboTestFileTransferHandler(NLaboTestImporter context) {
        fileFlavor = DataFlavor.javaFileListFlavor;
        this.context = context;
    }
    
    @Override
    public boolean importData(JComponent c, Transferable t) {
        
        if (!canImport(c, t.getTransferDataFlavors())) {
            return false;
        }
        
        try {
            if (hasFileFlavor(t.getTransferDataFlavors())) {

                // Drag & Drop ���ꂽ�t�@�C���̃��X�g�𓾂�
                java.util.List<File> files = (java.util.List<File>) t.getTransferData(fileFlavor);
                List<File> labFiles = new ArrayList<File>(files.size());

                // �g���q�� .DAT �̃t�@�C���̂� queue �֒ǉ�����
                for (File file : files) {
                    if (!file.isDirectory() && file.getName().toLowerCase().endsWith(DAT_EXT)) {
                        labFiles.add(file);
                    }
                }
                
                if (labFiles != null && labFiles.size() > 0) {
                    parseFiles(labFiles);
                }
                
                return true;
            }
            
        } catch (UnsupportedFlavorException ufe) {
            ufe.printStackTrace();
            
        } catch (IOException ieo) {
            ieo.printStackTrace();
            
        }
        return false;
    }
    
    @Override
    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        if (hasFileFlavor(flavors)) {
            return true;
        }
        return false;
    }
    
    private boolean hasFileFlavor(DataFlavor[] flavors) {
        for (int i = 0; i < flavors.length; i++) {
            if (fileFlavor.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }

    private void parseFiles(final List<File> labFiles) {

        final javax.swing.SwingWorker worker = new javax.swing.SwingWorker<List<NLaboImportSummary>, Void>() {

            @Override
            protected List<NLaboImportSummary> doInBackground() throws Exception {
                NLabParser parse = new NLabParser();
                List<NLaboImportSummary> allModules = new ArrayList<NLaboImportSummary>();
                for (File lab : labFiles) {
                    List<NLaboImportSummary> dataList = parse.parse(lab);
                    allModules.addAll(dataList);
                }
                return allModules;
            }

            @Override
            protected void done() {

                try {
                    List<NLaboImportSummary> allModules = get();
                    context.getTableModel().setDataProvider(allModules);

                } catch (Exception e) {
                    String why = null;
                    Throwable cause = e.getCause();
                    if (cause != null) {
                        why = cause.getMessage();
                    } else {
                        why = e.getMessage();
                    }
                    Window parent = SwingUtilities.getWindowAncestor(context.getUI());
                    String message = "�p�[�X�ł��Ȃ��t�@�C��������܂��B\n�����񍐏��t�H�[�}�b�g���m�F���Ă��������B\n" + why;
                    String title = "���{���V�[�o";
                    JOptionPane.showMessageDialog(parent, message, ClientContext.getFrameTitle(title), JOptionPane.WARNING_MESSAGE);
                }
            }
        };

        worker.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getNewValue().equals(SwingWorker.StateValue.STARTED)) {
                    context.getProgressBar().setIndeterminate(true);
                } else if (evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
                    context.getProgressBar().setIndeterminate(false);
                    context.getProgressBar().setValue(0);
                    worker.removePropertyChangeListener(this);
                }
            }
        });

        worker.execute();
    }
}