/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package open.dolphin.scheam;

import java.awt.Graphics2D;
import java.awt.Point;

/**
 *
 * @author kazm
 */
public interface DrawingHolder {
    
    public boolean contains(Point p);
    
    public void draw(Graphics2D g2);
    
    public void translate(double x, double y);

}
