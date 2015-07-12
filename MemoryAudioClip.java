//use "allen_sauer" since "allen-sauer" would be illegal
import java.net.*;
import java.applet.*;
import java.util.*;

public class MemoryAudioClip {
  private static String baseLocation="";
  private static Properties myProperties;
  private static boolean soundOn;
  private static AudioClip lastPlayedClip=null;
  private AudioClip myClip=null;
  private long myDuration;
  public static final int LEFT_OF_PIPE=0, RIGHT_OF_PIPE=1;
  private static Random random=new Random();
  
  public static void initialize(Properties properties, String baseLocationPropertyName) {
    myProperties=properties;
    baseLocation=properties.getProperty(baseLocationPropertyName);
    soundOn=properties.getProperty("sounds.activate","true").equalsIgnoreCase("true");
  }
  
  public MemoryAudioClip(String propertyName) {
    try {
      String value=myProperties.getProperty(propertyName,"");
      int commaPos=value.indexOf(',');
      setDurationAndClipLocation(value.substring(0,commaPos),value.substring(commaPos+1));
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  
  public MemoryAudioClip(String value, int whichHalf) {
    if (whichHalf!=LEFT_OF_PIPE && whichHalf!=RIGHT_OF_PIPE) throw new IllegalArgumentException();
    try {
      int pipePos=value.indexOf('|');
      value=(whichHalf==LEFT_OF_PIPE) ? value.substring(0,pipePos) : value.substring(pipePos+1);
      int commaPos=value.indexOf(',');
      setDurationAndClipLocation(value.substring(0,commaPos),value.substring(commaPos+1));
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  
  private void setDurationAndClipLocation(String duration, String clipLocation)
  throws MalformedURLException {
    myDuration=(long)(new Float(duration).floatValue()*1000);
    //System.out.println(baseLocation+clipLocation);
    myClip=Applet.newAudioClip(getClass().getResource(baseLocation+clipLocation));
  }
  
  public long play() {
    long now=System.currentTimeMillis();
    if (!soundOn) return now;
    playItSam(myClip);
    return now+myDuration;
  }
  
  public void playAndWait() {
    long endOfClipTime=play();
    try { Thread.currentThread().sleep(endOfClipTime-System.currentTimeMillis()); } catch(Exception ex) {}
  }
  
  public static void playAndWait(Vector clipVector) {
    int size=clipVector.size();
    if (size>1) {
      MemoryAudioClip clip=(MemoryAudioClip)clipVector.get(random.nextInt(clipVector.size()));
      clip.playAndWait();
    }
  }
  
  private static synchronized void playItSam(AudioClip clip) {
    try {
      lastPlayedClip.stop();
      try { Thread.currentThread().sleep(100); } catch(Exception ex) {}
    } catch(Exception ex) {
      //ex.printStackTrace();
    }
    lastPlayedClip=clip;
    try {
      clip.play();
      try { Thread.currentThread().sleep(100); } catch(Exception ex) {}
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }
}