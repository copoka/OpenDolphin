/*
 * Created on 2005/09/13
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package open.dolphin.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * ImagePalette
 *
 * @author Minagawa,Kazushi
 */
public class ImagePalette extends JPanel implements DragSourceListener, DragGestureListener {
    
    private static final long serialVersionUID = -3156381268784747382L;
    
    private static final int DEFAULT_COLUMN_COUNT 	=   3;
    private static final int DEFAULT_IMAGE_WIDTH 	= 120;
    private static final int DEFAULT_IMAGE_HEIGHT 	= 120;
    private static final String[] DEFAULT_IMAGE_SUFFIX = {".jpg"};
    
    private ImageTableModel imageTableModel;
    private int imageWidth;
    private int imageHeight;
    private JTable imageTable;
    private DragSource dragSource;
    private File imageDirectory;
    private String[] suffix = DEFAULT_IMAGE_SUFFIX;
    private boolean showHeader;
    
    public ImagePalette(String[] columnNames, int columnCount, int imageWidth, int imageHeight) {
        imageTableModel = new ImageTableModel(columnNames, columnCount);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        initComponent(columnCount);
        connect();
    }
    
    public ImagePalette() {
        this(null, DEFAULT_COLUMN_COUNT, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);
    }
    
    public List getImageList() {
        return imageTableModel.getImageList();
    }
    
    public void setImageList(ArrayList list) {
        imageTableModel.setImageList(list);
    }
    
    public JTable getable() {
        return imageTable;
    }
    
    public String[] getimageSuffix() {
        return suffix;
    }
    
    public void setImageSuffix(String[] suffix) {
        this.suffix = suffix;
    }
    
    public File getImageDirectory() {
        return imageDirectory;
    }
    
    public void setImageDirectory(File imageDirectory) {
        this.imageDirectory = imageDirectory;
        refresh();
    }
    
    public void dispose() {
        if (imageTableModel != null) {
            imageTableModel.clear();
        }
    }
    
    @SuppressWarnings("unchecked")
    public void refresh() {
        
        if ( (! imageDirectory.exists()) || (! imageDirectory.isDirectory()) ) {
            return;
        }
        
        Dimension imageSize = new Dimension(imageWidth, imageHeight);
        File[] imageFiles = listImageFiles(imageDirectory, suffix);
        if (imageFiles != null && imageFiles.length > 0) {
            ArrayList imageList = new ArrayList();
            for (int j= 0; j < imageFiles.length; j++) {
                try {
                    URL url = imageFiles[j].toURL();
                    ImageIcon icon = new ImageIcon(url);
                    ImageEntry entry = new ImageEntry();
                    entry.setImageIcon(adjustImageSize(icon, imageSize));
                    entry.setUrl(url.toString());
                    imageList.add(entry);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            imageTableModel.setImageList(imageList);
        }
    }
    
    private void initComponent(int columnCount) {
        
        // Image table �𐶐�����
        imageTable = new JTable(imageTableModel);
        imageTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        imageTable.setCellSelectionEnabled(true);
        imageTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        TableColumn column = null;
        for (int i = 0; i < columnCount; i++) {
            column = imageTable.getColumnModel().getColumn(i);
            column.setPreferredWidth(imageWidth);
        }
        imageTable.setRowHeight(imageHeight);
        
        ImageRenderer imageRenderer = new ImageRenderer();
        imageRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        imageTable.setDefaultRenderer(java.lang.Object.class, imageRenderer);
        
        this.setLayout(new BorderLayout());
        if (showHeader) {
            this.add(new JScrollPane(imageTable));
        } else {
            JPanel ui = new JPanel(new BorderLayout());
            ui.add(imageTable);
            this.add(new JScrollPane(ui));
        }
    }
    
    private void connect() {
        dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(imageTable, DnDConstants.ACTION_COPY_OR_MOVE, this);
    }
    
    private ImageIcon adjustImageSize(ImageIcon icon, Dimension dim) {
        
        if ( (icon.getIconHeight() > dim.height) || (icon.getIconWidth() > dim.width) ) {
            Image img = icon.getImage();
            float hRatio = (float)icon.getIconHeight() / dim.height;
            float wRatio = (float)icon.getIconWidth() / dim.width;
            int h, w;
            if (hRatio > wRatio) {
                h = dim.height;
                w = (int)(icon.getIconWidth() / hRatio);
            } else {
                w = dim.width;
                h = (int)(icon.getIconHeight() / wRatio);
            }
            img = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } else {
            return icon;
        }
    }
    
    private File[] listImageFiles(File dir, String[] suffix) {
        ImageFileFilter filter = new ImageFileFilter(suffix);
        return dir.listFiles(filter);
    }
    
    class ImageFileFilter implements FilenameFilter {
        
        private String[] suffix;
        
        public ImageFileFilter(String[] suffix) {
            this.suffix = suffix;
        }
        
        public boolean accept(File dir, String name) {
            
            boolean accept = false;
            for (int i = 0; i < suffix.length; i++) {
                if (name.toLowerCase().endsWith(suffix[i])) {
                    accept = true;
                    break;
                }
            }
            return accept;
        }
    }
    
    public void dragDropEnd(DragSourceDropEvent event) {
    }
    
    public void dragEnter(DragSourceDragEvent event) {
    }
    
    public void dragOver(DragSourceDragEvent event) {
    }
    
    public void dragExit(DragSourceEvent event) {
    }
    
    public void dropActionChanged( DragSourceDragEvent event) {
    }
    
    public void dragGestureRecognized(DragGestureEvent event) {
        
        try {
            int row = imageTable.getSelectedRow();
            int col = imageTable.getSelectedColumn();
            if (row != -1 && col != -1) {
                ImageEntry entry = (ImageEntry)imageTable.getValueAt(row, col);
                Transferable t = new ImageEntryTransferable(entry);
                dragSource.startDrag(event, DragSource.DefaultCopyDrop, t, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    class ImageRenderer extends DefaultTableCellRenderer {
        
        private static final long serialVersionUID = -7952145522385412194L;
        
        public ImageRenderer() {
            setVerticalTextPosition(JLabel.BOTTOM);
            setHorizontalTextPosition(JLabel.CENTER);
        }
        
        public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean isFocused,
                int row, int col) {
            Component compo = super.getTableCellRendererComponent(table,
                    value,
                    isSelected,
                    isFocused,
                    row, col);
            JLabel l = (JLabel)compo;
            
            if (value != null) {
                
                ImageEntry entry = (ImageEntry)value;
                l.setIcon(entry.getImageIcon());
                l.setText(null);
                
            } else {
                l.setIcon(null);
                l.setText(null);
            }
            return compo;
        }
    }
}