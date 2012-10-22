package open.dolphin.client;

import java.awt.event.MouseAdapter;

import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.TransferHandler;

/**
 * ComponentHolder
 *
 * @author  Kazushi Minagawa
 */
public abstract class AbstractComponentHolder extends JLabel implements MouseListener, MouseMotionListener {
    
    protected MouseEvent firstMouseEvent;
    
    /** Creates new ComponentHolder */
    public AbstractComponentHolder() {
        this.setFocusable(true);
        //this.addFocusListener(this);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseListener(new PopupListner());
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        ActionMap map = this.getActionMap();
        map.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());
    }
    
    public void mouseClicked(MouseEvent e) {
        requestFocusInWindow();
        if (e.getClickCount() == 2) {
            edit();
        }
    }
    
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mousePressed(MouseEvent e) {
        firstMouseEvent = e;
        e.consume();
    }
    public void mouseReleased(MouseEvent e) {
    }
    
    public void mouseDragged(MouseEvent e) {
        
        if (firstMouseEvent != null) {
            
            e.consume();
            
            //If they are holding down the control key, COPY rather than MOVE
            int ctrlMask = InputEvent.CTRL_DOWN_MASK;
            int action = ((e.getModifiersEx() & ctrlMask) == ctrlMask)
            ? TransferHandler.COPY
                    : TransferHandler.MOVE;
            
            int dx = Math.abs(e.getX() - firstMouseEvent.getX());
            int dy = Math.abs(e.getY() - firstMouseEvent.getY());
            
            if (dx > 5 || dy > 5) {
                JComponent c = (JComponent) e.getSource();
                TransferHandler handler = c.getTransferHandler();
                handler.exportAsDrag(c, firstMouseEvent, action);
                firstMouseEvent = null;
            }
        }
    }
    
    public void mouseMoved(MouseEvent e) { }
    
    //public abstract void focusGained(FocusEvent e);
    
    //public abstract void focusLost(FocusEvent e);
    
    public abstract void edit();
    
    class PopupListner extends MouseAdapter {
        
        @Override
        public void mousePressed(MouseEvent e) {
            mabeShowPopup(e);
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            mabeShowPopup(e);
        }
    }
    
    public abstract void mabeShowPopup(MouseEvent e);
}