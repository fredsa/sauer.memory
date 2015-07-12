//use "allen_sauer" since "allen-sauer" would be illegal
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

class MemoryButton extends JButton {
  private ImageIcon myCommonIcon, myFaceIcon, myNullIcon;
  private Image myPairedImage;
  private MemoryAudioClip myClip;
  private MemoryButton myMatchingButton, myPairedButton;
  public static final int NULL_ICON=0, FACE_ICON=1, COMMON_ICON=2;
  
  MemoryButton(Image nullImage, Image commonImage, Image faceImage, MemoryAudioClip clip) {
    myCommonIcon=new ImageIcon(commonImage);
    myFaceIcon=new ImageIcon(faceImage);
    myNullIcon=new ImageIcon(nullImage);
    myClip=clip;
    
    setIcon(myCommonIcon);
  }
  
  public void setMatchingButton(MemoryButton button) {
    myMatchingButton=button;
  }
  
  public void setPairedImage(Image image) {
    myPairedImage=image;
  }
  
  public MemoryButton getMatchingButton() {
    return myMatchingButton;
  }
  
  public Image getPairedImage() {
    return myPairedImage;
  }
  
  public Image getFaceImage() {
    return myFaceIcon.getImage();
  }
  
  public long playClip() {
    if (myClip==null) return System.currentTimeMillis();
    return myClip.play();
//    myClip.playAndWait();
//    return System.currentTimeMillis();
  }
  
  public void flipTo(int view) {
    if (view==FACE_ICON) setIcon(myFaceIcon);
    else if (view==COMMON_ICON) setIcon(myCommonIcon);
    else if (view==NULL_ICON) setIcon(myNullIcon);
    else throw new IllegalArgumentException();
    try { paint(getGraphics()); } catch(Exception ex) {}
  }
  
}