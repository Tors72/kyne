import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.awt.Desktop;
import java.util.Objects;


public class mainWindow extends JFrame {

    final String SERVER_PATH = System.getenv("APPDATA")+"\\StardewValley\\Saves\\";
    //This project was created so that I could play Stardew Valley with my friends, hence the default server Path and credential path are relative to stardew valley
    //I will make it so that save location + credential file name can be changed, in future commits
    final String CRED_PATH = System.getenv("APPDATA")+"\\Stardew_Geit_Creeedentials.txt";
    File CRED = new File(CRED_PATH);
    GitManager GM;
    String username, token;
    JLabel usernameDisplay, lastcommitDisplay, tors;
    Font pixel;

    mainWindow(){
        //Kyne looked the best with the font
        this.setTitle("Kyne Shared Hosting");
        this.setBounds(new Rectangle(800,400));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setUndecorated(true);

        FrameDragListener frameDragListener = new FrameDragListener(this);
        this.addMouseListener(frameDragListener);
        this.addMouseMotionListener(frameDragListener);


        this.setIconImage( new ImageIcon(Objects.requireNonNull(getClass().getResource("/icon.png"))).getImage());
        setContentPane(new JPanel() {

            final Image bg = new ImageIcon(Objects.requireNonNull(getClass().getResource("/bg.png"))).getImage()
                    .getScaledInstance(800, 400, Image.SCALE_SMOOTH);

            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bg, 0, 0, this);
            }
        });
        getContentPane().setLayout(null);

        try {
            this.pixel = Font.createFont(Font.TRUETYPE_FONT,
                    Objects.requireNonNull(getClass().getResourceAsStream(
                            "/pixel_font.ttf")));
            System.out.println(this.pixel.getFontName());
            this.pixel = this.pixel.deriveFont(Font.PLAIN, 150f);
        }catch(IOException | FontFormatException e){
            e.printStackTrace();
        }

        initContent();
        loadCredentials();

        this.setLocationRelativeTo(null);
        this.requestFocus();
        this.setVisible(true);
    }

    void initContent(){

        //Close UI Button
        initButton(this,"",770,10,20,20,100,
                new Color(255, 110, 110), new Color(255, 110, 110),
                20,e-> System.exit(67)
        );


        initButton(this,"Download",250,25,150,50
                ,20,new Color(245, 246, 251),new Color(104, 165, 255),
                20,e->{
                    if(notLoggedIn())
                        JOptionPane.showMessageDialog(null,"Login first");
                    else
                        GM.cloneRepo();
        });


        initButton(this,"Upload",420,25,150,50
                ,20,new Color(197, 241, 255, 236),new Color(65, 128, 242),
                20,e->{
                    if(notLoggedIn())
                        JOptionPane.showMessageDialog(null,"Login first");
                    else {
                        GM.pushAll("Multiplayer Server Save");
                        lastcommitDisplay.setText("Latest Commit: " + GM.lastCommitTime());
                    }
        });


        initButton(this, "Open Hamachi",10,205,200,50,20,
                new Color(245,246,251), new Color(104,165,255),
                20, e->
                {
                    try {
                        new ProcessBuilder(
                                "C:\\Program Files (x86)\\LogMeIn Hamachi\\hamachi-2-ui.exe")
                                .start();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                );


        initButton(this,"Server Folder",10,260,200,50,20,
                new Color(245, 246, 251),new Color(104, 165, 255),
                20,e->
                {
                    try {
                        Desktop.getDesktop().open(new File(SERVER_PATH));
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
        );


        initButton(this,"Background",10,315,200,35
                ,10,new Color(245, 246, 251),new Color(104, 165, 255),
                18,e-> {
                    try {
                        Desktop.getDesktop().browse(new URL(
                                "https://in.pinterest.com/pin/786933734892781690/"
                        ).toURI());
                    } catch (IOException | URISyntaxException ex) {
                        throw new RuntimeException(ex);
                    }
                }
        );


        initButton(this,"Install Git",585,340,200,50,
                20,new Color(197, 241, 255, 236),new Color(65, 128, 242),
                20,e-> {
                    JOptionPane.showMessageDialog(null,
                            "Installing Git, click OK to begin!" +
                                    "\n Will display downloaded version once installed.");
                    GM.runCommand(List.of("winget", "install", "Git.Git"));
                    try {
                        Runtime.getRuntime().exec("cmd /c start cmd /k git version");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
        );

        //Token Help
        initButton(this,"i",
                585,280,50,50,20,new Color(197, 241, 255, 236),new Color(65, 128, 242),
                20,e->
                    JOptionPane.showMessageDialog(null,
                            "Github tokens are generated codes used to access select repositories, specified by the user.\n This access extends to read/write privileges which are required to use this program properly.\n Please visit github.com/settings/personal-access-tokens and Generate a new token.\n 1. Select the repository you wish to read/write to.\n2. Make sure repository access is set as per your repo accessibility(private or public)\n3.Permissions: Add ,Content, with read/write \n4.Generate your key, be sure to save it.\n Thats all.")
        );

        //Login to Git via token
        initButton(this,"Login",645,280,140,50,
                20,new Color(197, 241, 255, 236),new Color(65, 128, 242),
                20,e->
                        doUserLogin()
        );

        //JLabels, to be redone
        usernameDisplay = initLabel("Not logged in","Arial",
                15,11,345,200,40);
        tors = initLabel("KYNE","IT Gridbit Demo Italic",150,
                240,150,490,121);
        tors.setFont(pixel);
        lastcommitDisplay = initLabel("Cannot fetch latest commit","Arial",
                15,11,365,500,40);
    }


    JLabel initLabel(String text, String font, int fontsize, int x, int y, int w, int h){
        JLabel label = new JLabel(text){
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                        0.3f)); // 0.0 = invisible, 1.0 = fully opaque
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };
        label.setFont(new Font(font,Font.BOLD,fontsize));
        label.setBounds(x,y,w,h);
        label.setForeground(new Color(40, 84, 181));
        this.add(label);

        return label;
    }


    boolean notLoggedIn(){ return token == null || username == null; }


    //initialize git manager IFF credentials are already stored
    void initGM(){
        try{
            GM= new GitManager(SERVER_PATH
                    ,username,token,
                    "https://github.com/Tors72/server_world_files.git");
        }catch(IOException | InterruptedException e){
            e.printStackTrace();
        }
    }


    void doUserLogin(){
        username = JOptionPane.showInputDialog("Enter Github Username:").trim();
        token = JOptionPane.showInputDialog("Enter Token:").trim();

        try {
            GM= new GitManager(SERVER_PATH
                    ,username,token,
                    "https://github.com/Tors72/server_world_files.git");
            GM.cloneRepo();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        //Saves Credentials to folder in drive root, to retrieve at a later time
        saveCredentials();

        System.out.println("Successfully logged in as:"+username);
        usernameDisplay.setText(username);
        lastcommitDisplay.setText("Latest Commit: "+GM.lastCommitTime());
    }


    void saveCredentials(){
        try{
            Files.writeString(CRED.toPath(), username +
                    "\n" + token);
        }catch (IOException ex) {
            throw new RuntimeException("Failed to save credentials", ex);
        }
    }


    void loadCredentials(){
        try{
            //retrieves credentials in string array, 0 - username, 1 = token
            String[] cred = (Files.readString(CRED.toPath())).split("\n");
            username = cred[0];
            token = cred[1];

            initGM();

            usernameDisplay.setText(username);
            lastcommitDisplay.setText("Latest Commit: "+GM.lastCommitTime());

        }catch(IOException e){
            e.printStackTrace();
            System.out.println("Unable to retrieve credentials, credentials not saved?");
        }
    }


    void initButton(JFrame frame, String text, int x, int y, int width,
                    int height, int roundness,Color bgcolor, Color txtcolor,
                    int textSize,
                    ActionListener e) {
        JButton button = new JButton();
        button.setText(text);
        button.setBounds(x, y, width, height);
        button.setFont(new Font("Arial",Font.BOLD,20));
        button.setForeground(txtcolor);
        button.setFocusable(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setBackground(bgcolor);
        button.addActionListener(e);
        //To add roundness and make it dark when pressed, TBR
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                JButton b = (JButton) c;
                g2.setColor(b.getModel().isPressed()
                        ? b.getBackground().brighter()
                        : b.getBackground());

                g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(),
                        roundness, roundness);
                g2.dispose();

                super.paint(g, c);
            }
        });
        // Source: https://stackoverflow.com/questions/22638926/how-to-put-hover-effect-on-jbutton
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgcolor.brighter());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgcolor);
            }
        });
        frame.add(button);
    }
}
