//use "allen_sauer" since "allen-sauer" would be illegal
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class Memory extends JFrame implements ActionListener, Runnable {
  private JPanel gamePanel, gameBoardPanel, gameStatsPanel, introPanel, youWinPanel;
  private Vector<MemoryButton> allButtons=new Vector<MemoryButton>();
  private JButton startButton;
  private Image noCardImage, cardImage, tinyCardImage;
  private JLabel noCardLabel;
  private Properties myProperties;
  private String imageBaseLocation;
  private String soundBaseLocation;
  private MemoryButton currentlyFlippedButton=null, recentlyPushedButton=null;
  private Vector<MemoryAudioClip> alrightVector=new Vector<MemoryAudioClip>(), uhuhuhVector=new Vector<MemoryAudioClip>();
  private Vector<MemoryAudioClip> welcomeVector=new Vector<MemoryAudioClip>(), youWinVector=new Vector<MemoryAudioClip>();
  private long endOfClipTime=0;
  private Object myActionSyncObject=new Object();
  private int totalImageCount, gameImageCount;
  private Font bigFont;
  private int totalPairCount, remainingPairCount;
  private static Random random=new Random();

  public Memory() throws IOException {
    super("Memory");
    bigFont=new Font("SansSerif",Font.PLAIN,20);
    myProperties=new Properties();
    System.out.println("URL: " + Memory.class.getResource("Memory.properties"));
    myProperties.load(getClass().getResourceAsStream("Memory.properties"));

    imageBaseLocation=myProperties.getProperty("images.location");
    cardImage=getToolkit().getImage(getClass().getResource(imageBaseLocation+myProperties.getProperty("images.card_image")));
    noCardImage=getToolkit().getImage(getClass().getResource(imageBaseLocation+myProperties.getProperty("images.no_card_image")));
    noCardLabel=new JLabel(new ImageIcon(noCardImage));

    ImageFilter filter=new ReplicateScaleFilter(20,20);
    ImageProducer producer=new FilteredImageSource(cardImage.getSource(),filter);
    tinyCardImage=createImage(producer);

    MemoryAudioClip.initialize(myProperties,"sounds.location");

    totalPairCount=0;
    for(Enumeration enumeration=myProperties.propertyNames(); enumeration.hasMoreElements(); ) {
      String name=(String)enumeration.nextElement();
      if (name.startsWith("sounds.uhuhuh.")) {
        uhuhuhVector.addElement(new MemoryAudioClip(name));
      } else if (name.startsWith("sounds.alright.")) {
        alrightVector.addElement(new MemoryAudioClip(name));
      } else if (name.startsWith("sounds.you_win.")) {
        youWinVector.addElement(new MemoryAudioClip(name));
      } else if (name.startsWith("sounds.welcome.")) {
        welcomeVector.addElement(new MemoryAudioClip(name));
      } else if (name.startsWith("images.pair.")) {
        totalPairCount++;

        Image image1, image2, imagePaired;
        MemoryAudioClip clip1=null,clip2=null;

        String imageValue=myProperties.getProperty(name);
        int pipePos1=imageValue.indexOf('|');
        int pipePos2=imageValue.indexOf('|',pipePos1+1);
        image1=getToolkit().getImage(getClass().getResource(imageBaseLocation+imageValue.substring(0,pipePos1)));
        image2=getToolkit().getImage(getClass().getResource(imageBaseLocation+imageValue.substring(pipePos1+1,pipePos2)));
        imagePaired=getToolkit().getImage(getClass().getResource(imageBaseLocation+imageValue.substring(pipePos2+1)));

        String soundValue=myProperties.getProperty("sounds.pair."+name.substring(12));
        if (soundValue!=null) {
          clip1=new MemoryAudioClip(soundValue,MemoryAudioClip.LEFT_OF_PIPE);
          clip2=new MemoryAudioClip(soundValue,MemoryAudioClip.RIGHT_OF_PIPE);
        }

        MemoryButton button1,button2;
        button1=new MemoryButton(noCardImage, cardImage,image1,clip1);
        button2=new MemoryButton(noCardImage, cardImage,image2,clip2);
        button1.setMatchingButton(button2);
        button2.setMatchingButton(button1);
        button1.setPairedImage(imagePaired);
        button2.setPairedImage(imagePaired);

        allButtons.addElement(button1);
        allButtons.addElement(button2);
      }
    }
    totalImageCount=totalPairCount*2;

    initIntroPanel();
    initYouWinPanel();
  }

  public void startGame(int imageCount) {
    gameImageCount=imageCount;
    Vector buttons=(Vector)allButtons.clone();
    for(int i=totalImageCount; i>imageCount; i-=2) {
      //System.out.println("totalImageCount: "+totalImageCount+" imageCount: "+imageCount);
      int r=random.nextInt(buttons.size());
      MemoryButton button1=(MemoryButton)buttons.elementAt(r);
      buttons.removeElement(button1);
      buttons.removeElement(button1.getMatchingButton());
    }
    initGamePanel(buttons);
    for(Enumeration enumeration=buttons.elements(); enumeration.hasMoreElements(); ) {
      MemoryButton button=(MemoryButton)enumeration.nextElement();
      button.flipTo(MemoryButton.COMMON_ICON);
    }
    setContentPane(gamePanel);
    pack();
    validate();
  }

  public static void main(String[] args)
  throws Exception {
//    try {
//      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
//    } catch (Exception ex) {
//      System.err.println("Couldn't use the cross-platform look and feel: "+ex);
//    }
    new Memory().doit();
  }

  public void doit() throws Exception {
    WindowListener l=new WindowAdapter() {
      public void windowClosing(WindowEvent ev) {
        System.exit(0);
      }
    };
    addWindowListener(l);

    setContentPane(introPanel);
    pack();
    new Thread(this).start();

    setVisible(true);

    MemoryAudioClip.playAndWait(welcomeVector);
  }

  private GridLayout getGridLayout(int imageCount) {
    switch(imageCount) {
//      case 2: return new GridLayout(1,2);
      case 4: return new GridLayout(2,2);
      case 6: return new GridLayout(3,2);
      case 8: return new GridLayout(4,2);
//      case 10: return new GridLayout(5,2);
      case 12: return new GridLayout(4,3);
      case 16: return new GridLayout(4,4);
      default: return null;
    }
  }

  private void initGamePanel(Vector buttons) {
    gamePanel=new JPanel();
    gameBoardPanel=new JPanel();
    gameStatsPanel=new JPanel();
//    gameStatsPanel.setLayout(new FlowLayout());
    gameStatsPanel.setLayout(new GridLayout(0,2,5,5));

//    gamePanel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
    gameBoardPanel.setLayout(getGridLayout(buttons.size()));
    remainingPairCount=buttons.size()/2;

    //add some empty images to space it out
    for(int i=0; i<remainingPairCount; i++) {
      gameStatsPanel.add(noCardLabel);
    }

    Vector buttonsClone=(Vector)buttons.clone();
    while (!buttonsClone.isEmpty()) {
      MemoryButton button=(MemoryButton)buttonsClone.get(random.nextInt(buttonsClone.size()));
      buttonsClone.remove(button);
      button.addActionListener(this);
      gameBoardPanel.add(button);
    }

    GridBagLayout gridbag=new GridBagLayout();
    gamePanel.setLayout(gridbag);
    GridBagConstraints c=new GridBagConstraints();
    c.anchor=GridBagConstraints.NORTH;
    c.gridwidth=1;
    c.gridheight=GridBagConstraints.REMAINDER;
    c.weightx=1.0;
    c.weighty=1.0;
    gridbag.setConstraints(gameBoardPanel,c);
//    c.gridwidth=GridBagConstraints.REMAINDER;
    gridbag.setConstraints(gameStatsPanel,c);

    gamePanel.add(gameBoardPanel);
    gamePanel.add(gameStatsPanel);
  }

  private void initIntroPanel() {
    introPanel=new JPanel();
    JPanel scenarioPanel=new JPanel();
    JPanel welcomeImagePanel=new JPanel();

    welcomeImagePanel.setLayout(new BoxLayout(welcomeImagePanel,BoxLayout.Y_AXIS));

    introPanel.setLayout(new BoxLayout(introPanel,BoxLayout.X_AXIS));
    introPanel.add(scenarioPanel);
    introPanel.add(welcomeImagePanel);

//    introPanel.setBorder(BorderFactory.createEmptyBorder(3,1,3,1));

    GridBagLayout gridbag=new GridBagLayout();
    GridBagConstraints c=new GridBagConstraints();
    scenarioPanel.setLayout(gridbag);
///    scenarioPanel.setLayout(new GridLayout(5,4));

//    Font myFont=new Font("SansSerif",Font.BOLD,12);
//    System.out.println("getToolkit().getScreenSize(): "+getToolkit().getScreenSize());
//    System.out.println("getToolkit().getScreenResolution(): "+getToolkit().getScreenResolution());
//    System.out.println("getToolkit().getFontList()...");
//    String[] t=getToolkit().getFontList();
//    for(int i=0; i<t.length; i++) System.out.println("  ["+i+"]: "+t[i]);
//
//    System.out.println("GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()...");
//    Font[] f=GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
//    for(int i=0; i<t.length; i++) System.out.println("  ["+i+"]: "+t[i]);

    JLabel welcomeImageLabel=new JLabel(new ImageIcon(getToolkit().getImage(getClass().getResource(imageBaseLocation+myProperties.getProperty("images.welcome")))));
    JLabel welcomeTextLabel=new JLabel(myProperties.getProperty("message.welcome"),SwingConstants.CENTER);
    welcomeTextLabel.setFont(bigFont);

    c.gridwidth=1;
    c.gridheight=1;

    c.weightx=1.0;
    c.weighty=1.0;

    welcomeImagePanel.add(welcomeImageLabel);
    welcomeImagePanel.add(welcomeTextLabel);

    int scenarios=0;
    for(int i=2; i<=totalImageCount; i+=2) {
      if (getGridLayout(i)!=null) scenarios++;
    }

    int j=1, horizontalScenarios=(int)Math.ceil(Math.sqrt(scenarios));
    for(int i=2; i<=totalImageCount; i+=2) {
      if (j==horizontalScenarios) {
        c.gridwidth=GridBagConstraints.REMAINDER;
        j=1;
      } else {
        c.gridwidth=1;
      }
      LayoutManager lm=getGridLayout(i);
      if (lm!=null) {
        j++;
        MemorySamplePanel samplePanel=new MemorySamplePanel(this,i,lm,tinyCardImage);
        samplePanel.setBorder(BorderFactory.createEtchedBorder());
        gridbag.setConstraints(samplePanel,c);
        scenarioPanel.add(samplePanel);
      }
    }

  }

  private void initYouWinPanel() {
    youWinPanel=new JPanel();

    youWinPanel.setBorder(BorderFactory.createEmptyBorder(3,1,3,1));
    youWinPanel.setLayout(new GridLayout(2,2));

    JLabel youWinImageLabel=new JLabel(new ImageIcon(getToolkit().getImage(getClass().getResource(imageBaseLocation+myProperties.getProperty("images.you_win")))));
    JLabel youWinTextLabel=new JLabel(myProperties.getProperty("message.you_win"),SwingConstants.CENTER);
    youWinTextLabel.setFont(bigFont);

    startButton=new JButton("Play Again");
    startButton.addActionListener(this);

    JButton quitButton=new JButton("QUIT");
    quitButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        System.exit(0);
      }
    });

    youWinPanel.add(youWinImageLabel);
    youWinPanel.add(youWinTextLabel);
    youWinPanel.add(quitButton);
    youWinPanel.add(startButton);
  }

  public void actionPerformed(ActionEvent ev) {

//    System.out.println("----------------");
//    System.out.println("getSource(): "+ev.getSource());
//    System.out.println("getActionCommand(): "+ev.getActionCommand());
//    System.out.println("getModifiers(): "+ev.getModifiers());
//    System.out.println("paramString(): "+ev.paramString());
//    System.out.println("getID(): "+ev.getID());

    Object source=ev.getSource();
    if (source==startButton) {
      setContentPane(introPanel);
      pack();
    }
    if (source instanceof MemoryButton) {
      recentlyPushedButton=(MemoryButton)source;
      synchronized(myActionSyncObject) {
        myActionSyncObject.notifyAll();
      }
    } else if (source instanceof MemorySamplePanel) {
      MemorySamplePanel samplePanel=(MemorySamplePanel)source;
      startGame(samplePanel.getImageCount());
    }
  }

  public void run() {
    while(true) {
      recentlyPushedButton=null;

      //wait for any button push activity
      synchronized(myActionSyncObject) {
        try { myActionSyncObject.wait(); } catch(Exception ex) {}
      }
      if (System.currentTimeMillis()<endOfClipTime) {
        //we were waiting for the end of a clip; can't accept button action
        recentlyPushedButton=null;
        continue;
      }

      MemoryButton button=recentlyPushedButton;
      recentlyPushedButton=null;

      if (currentlyFlippedButton==null) {
        currentlyFlippedButton=button;
        button.flipTo(MemoryButton.FACE_ICON);
//        endOfClipTime=button.playClip();
        button.playClip();
      } else if (currentlyFlippedButton==button) {
        continue; //ignore
      } else {
        button.flipTo(MemoryButton.FACE_ICON);
        if (button.getMatchingButton()==currentlyFlippedButton) {
          MemoryAudioClip.playAndWait(alrightVector);
          button.flipTo(MemoryButton.NULL_ICON);
          currentlyFlippedButton.flipTo(MemoryButton.NULL_ICON);
          button.removeActionListener(this);
          currentlyFlippedButton.removeActionListener(this);
          JLabel label=new JLabel(new ImageIcon(button.getPairedImage()));
          gameStatsPanel.remove(noCardLabel);
          gameStatsPanel.add(label);
          label.invalidate();
          gameStatsPanel.invalidate();
//          gamePanel.invalidate();
          gamePanel.validate();
          if (--remainingPairCount==0) {
            MemoryAudioClip.playAndWait(youWinVector);
            setContentPane(youWinPanel);
            pack();
            validate();
          }
        } else {
          MemoryAudioClip.playAndWait(uhuhuhVector);
          button.flipTo(MemoryButton.COMMON_ICON);
          currentlyFlippedButton.flipTo(MemoryButton.COMMON_ICON);
        }
        currentlyFlippedButton=null;
      }
    }
  }

}
