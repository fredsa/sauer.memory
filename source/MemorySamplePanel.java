//use "allen_sauer" since "allen-sauer" would be illegal
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MemorySamplePanel extends JPanel {
  private int myImageCount;
  private ActionListener myActionListener;
  private MemorySamplePanel myself;
  
  public MemorySamplePanel(ActionListener actionListener, int imageCount, LayoutManager lm, Image image) {
    myActionListener=actionListener;
    myImageCount=imageCount;
    setLayout(lm);
    for(int i=0; i<imageCount; i++) {
      JLabel label=new JLabel(new ImageIcon(image));
      add(label);
    }
    myself=this;
    addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent ev) {
//          System.out.println("will send message to "+myActionListener);
        myActionListener.actionPerformed(new ActionEvent(myself,ActionEvent.ACTION_PERFORMED,"SELECTED GAME SIZE"));
      }
    }
    );
  }
  
  public int getImageCount() {
    return myImageCount;
  }
  
}
