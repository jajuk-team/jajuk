package ext;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

/**
 * @author Rizwan A. Qureshi
 * @version 1.0
 */
 
public class SmartPopupMenu extends JPopupMenu {
      private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      private int screenWidth = 0;
      private int screenHeight = 0;
      private final int correction = 50;//approx pixels for the taskbar
      private boolean firstTime = true;
      private int width = 100;
      private int height = 150;
 
 
      public SmartPopupMenu() {
            this.screenWidth = this.screenSize.width;
            this.screenHeight = this.screenSize.height-this.correction;
      }
 
      public SmartPopupMenu(String title){
            super(title);
            this.screenWidth = this.screenSize.width;
            this.screenHeight = this.screenSize.height-this.correction;
      }//const
 
      protected void show(MouseEvent e){
            Component comp = e.getComponent();
            Point p = e.getPoint();
            Point original = e.getPoint();
            Point newPoint = this.getNewLocation(p,original,comp);
            this.show(e.getComponent(),newPoint.x, newPoint.y);
      }//method
 
      private Point getNewLocation(Point p,Point original,Component comp){
             SwingUtilities.convertPointToScreen(p,comp);
             if (this.firstTime) this.computeMaximumStringWidth();
             int absX = firstTime?this.screenWidth-this.width:this.screenWidth-this.getSize().width;
             int absY = firstTime?this.screenHeight-this.height:this.screenHeight-this.getSize().height;
             boolean xChanged = p.x>absX;
             boolean yChanged = p.y>absY;
             if (this.firstTime) this.firstTime=false;
             if (!xChanged && !yChanged) return original;
             p.setLocation(xChanged?absX:0,yChanged?absY:0);
             SwingUtilities.convertPointFromScreen(p, comp);
             if (xChanged && !yChanged) p.setLocation(p.x,original.y);
             if (!xChanged && yChanged) p.setLocation(original.x,p.y);
             return p;
      }//method
 
//compute widht/height of Popupmenu only for the first time
      private void computeMaximumStringWidth(){
             Component[] menuItems = this.getComponents();
             int maxWidth = 0;
             for (int i=0;i<menuItems.length;i++){
                    if (menuItems[i] instanceof JMenuItem){
                           JMenuItem item = (JMenuItem)menuItems[i];
                           String text = item.getText();
                           if (text==null) continue;
                           int len = SwingUtilities.computeStringWidth(item.getFontMetrics(item.getFont()),text)+60;
                           if (len > maxWidth)maxWidth = len;
                    }//if
             }//for
             this.width = maxWidth;
             this.height = menuItems.length*18;//height of one menuItem is around 20 pixels (approx)
      }//method
 
}//class
