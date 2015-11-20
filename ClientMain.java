package homework1;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.*;


/**
 * 
 * @author Gregorius Kristian Purwidi
 */
public class ClientMain extends JFrame implements ActionListener {
	Scanner userInput = new Scanner(System.in);

    private String serverAddressInput;
    private String serverPortInput; 
    private String msg;
    private int serverPortInt;
    String[] AfterSplit;
    
    public BlockingQueue queue = new ArrayBlockingQueue(256);
    
	private final int width = Toolkit.getDefaultToolkit().getScreenSize().width;
	private final int height = Toolkit.getDefaultToolkit().getScreenSize().height;
	private JPanel mainPanel;
	private JLabel upperLabel,wordLabel,guessHistoryLabel1,guessHistoryLabel2,belowWordLabel,topLabel;
	private JTextField guessField;
	private JButton submit;
	
    String score, newscore, word, availableAttempt, gameStatus, playerInput, currentGuess;
    int scoreInt, newscoreInt, attemptInt;
    String guessHistory = "";
    
    Boolean isStillPlaying = true, true1 = true, false1 = false;
    
    public static void main(String[] args) throws IOException, InterruptedException {      
    	new ClientMain();
    }
    
    //CONSTRUCTOR
    public ClientMain() throws InterruptedException    {
    	
    	//Ask player
    	AskAddressAndPort();
    	
    	//Initialize socket thread    	
    	InitializeSocket(); //Just start thread and open connection to server 	
    	
    	//First time. Socket receive message, put to queue
    	//then Main take it from queue
    	String msga = null;
    	msga = ReceiveFromSocket();    	
    	
    	//Split message and assign to specified variable
    	CustomSplitter(msga);
    	word = AfterSplit[0];
    	availableAttempt = AfterSplit[1];
    	score = AfterSplit[2];    	 
    	
    	word = SpacingAdder(word);
    	//Setup first time GUI
    	//while (isStillPlaying){
    	SetupGui();
    	
    }
    
    //THE FUNCTIONS    
    private void AskAddressAndPort(){
    	//TO GET SERVER ADDRESS AND PORT
    	//Open dialog box and ask for user input.
    	JTextField input1 = new JTextField();
    	JTextField input2 = new JTextField();
    	String[] options = {"Start New Game!"};
    	Object[] message = {
    		    "Server address :", input1,
    		    "Server port    :", input2,
    		    "Or just press start to use default value"
    		};
    	int option = JOptionPane.showOptionDialog(null, message, "Valkommen till HANGMAN", JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options , options[0]);
    	
    	//Get the input into variable
    	serverAddressInput = input1.getText();
    	serverPortInput = input2.getText();    
    	
    	//If one of the field empty, use default value 
    	if(serverAddressInput.isEmpty() == true || serverPortInput.isEmpty() == true){
        	serverAddressInput = "localhost";
        	serverPortInput = "5555";   
    	}
    		
    	serverPortInt = Integer.parseInt(serverPortInput);       
    }    
        
    private void InitializeSocket(){
        //Connect to server by start a thread    	
        (new Thread(new ClientSocket(serverAddressInput,serverPortInt,queue))).start();
       
    }
    
    private void SendToSocket(String msg){
    	//Put data into queue
        try {
            queue.put(msg);
        } catch (InterruptedException e) {
            System.err.println(e.toString());
        }
    }
    
    private String ReceiveFromSocket(){
    	//TAKE DATA FROM SOCKET (QUEUE)
        try {
            msg = (String) queue.take();
            queue.clear();
        } catch (InterruptedException e) {
            System.err.println(e.toString());
        }      
        return(msg);
    }
    
    private void CustomSplitter(String rawString){
    	//SPLIT INTO SEVERAL STRINGS LIMITED BY SPACE
        AfterSplit = rawString.split("#");  
    }
    
    private void Sleep(int time){
        try {
			Thread.sleep(time);
		} catch (InterruptedException e) {	
			e.printStackTrace();
		}
    }
        
    private String SpacingAdder(String input){
    	String result = input.replace("", " ").trim();
    	return result;
    }
    
    private void SendGuess(){
		//Apply space and append to history to be shown
		guessHistory = guessHistory+" "+currentGuess;

		//Send to Server		
		SendToSocket(currentGuess);
		Sleep(100);//Sleep to give socket time to take the data
    }
    
    private void ReceiveUpdateGui(){
		//Receive back update from server
		String msgb = ReceiveFromSocket();
		
		//Do the rest
		CustomSplitter(msgb);
    	word = AfterSplit[0];
    	availableAttempt = AfterSplit[1];
    	newscore = AfterSplit[2];
    	newscoreInt = Integer.parseInt(newscore.trim());
    	word = SpacingAdder(word);
    	
    	//Update
    	upperLabel.setText("Score : "+newscoreInt+"        Attempt(s) left : "+availableAttempt);
		wordLabel.setText(word);
		guessHistoryLabel2.setText(guessHistory);
		guessField.setText("");    	
    }
    
    private Boolean isEnd(){
		//int scoreInt = Integer.parseInt(score.trim());
    	//int newscoreInt = Integer.parseInt(newscore.trim());
		Boolean result = null;
    	//CHECK IF THE GAME REACH END OR NOT 
    	//Win
		if (newscoreInt != scoreInt) result= true;    		
    	else if (newscoreInt == scoreInt) result= false;    	
    	return result;
    }
    
    private Boolean isWin(){
		//int scoreInt = Integer.parseInt(score.trim());
    	//int newscoreInt = Integer.parseInt(newscore.trim());
		Boolean result = null;
    	//CHECK IF THE GAME REACH END OR NOT 
    	//Win
		if (newscoreInt > scoreInt) result= true;    		
    	else if (newscoreInt < scoreInt) result= false;    	
    	return result;
    }
    
    private void WinAskReplay(){
    	String[] options = new String[2];
    	options[0] = new String("Yes");
    	options[1] = new String("No");
    	int res = JOptionPane.showOptionDialog(null,"You Win! Replay?","Congratulations", 0,JOptionPane.INFORMATION_MESSAGE,null,options,null);
        
    	switch (res) {
        	case JOptionPane.YES_OPTION:
        		SendToSocket("NEWGAME");
        		Sleep(100);
        		scoreInt++;
        		guessHistory ="";
        		ReceiveUpdateGui();
        		break;
        	case JOptionPane.NO_OPTION:
        		SendToSocket("ENDGAME"); //Socket will detect this word and close connection
        		System.exit(0);
        		break;
    	}
    }
    
    private void LoseAskReplay(){
    	String[] options = new String[2];
    	options[0] = new String("Yes");
    	options[1] = new String("No");
    	int res = JOptionPane.showOptionDialog(null,"You Lose! Replay?","Game Over", 0,JOptionPane.INFORMATION_MESSAGE,null,options,null);
    	switch (res) {
	    	case JOptionPane.YES_OPTION:
	    		SendToSocket("NEWGAME");
	    		Sleep(100);
	    		scoreInt--;
	    		guessHistory ="";
	    		ReceiveUpdateGui();
	    		break;
	    	case JOptionPane.NO_OPTION:
	    		SendToSocket("ENDGAME"); //Socket will detect this word and close connection
	    		System.exit(0);
	    		break;
    	}
    }
    
    private void SetupGui(){
    	JFrame f = new JFrame();
    	f.setBounds(0,0,500,400); 
    	
    	//Specify panel. Only use 1 big panel 
    	mainPanel = new JPanel();
    	mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    		mainPanel.setBackground(Color.yellow);
    		setPreferredSize(new Dimension(500, 400));
    	
    	//Specify components. ADD COMPONENT ABOVE!!!!
        topLabel = new JLabel("The HANGMAN game");
    		topLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    		topLabel.setBorder(new EmptyBorder(20,0,0,0));
    		topLabel.setFont(new Font(null, Font.PLAIN, 32));
    		
    	upperLabel = new JLabel("Score : "+scoreInt+"        Attempt(s) left : "+availableAttempt);
    		upperLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    		upperLabel.setBorder(new EmptyBorder(20,0,0,0));
    		upperLabel.setFont(new Font("Serif", Font.PLAIN, 18));
    		
    	wordLabel = new JLabel(word);
    		wordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    		wordLabel.setBorder(new EmptyBorder(10,0,0,0));
    		wordLabel.setFont(new Font("Serif", Font.BOLD, 36));
    	
        belowWordLabel = new JLabel("Guess a letter or the whole word!");
    		belowWordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    		belowWordLabel.setBorder(new EmptyBorder(10,0,10,0));
    		
    	guessField = new JTextField(20);
    		guessField.setAlignmentX(Component.CENTER_ALIGNMENT);
    		guessField.setMaximumSize(new Dimension(300,45));
    		guessField.setHorizontalAlignment(JTextField.CENTER);
    		guessField.setFont(new Font(null, Font.BOLD, 20));
    		guessField.requestFocusInWindow();
    		
    	guessHistoryLabel1 = new JLabel("You have guessed : ");
    		guessHistoryLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
    		guessHistoryLabel1.setBorder(new EmptyBorder(10,0,0,0));

    	guessHistoryLabel2 = new JLabel(guessHistory);
    		guessHistoryLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
    		guessHistoryLabel2.setBorder(new EmptyBorder(10,0,20,0));
    		guessHistoryLabel2.setText("-");
    		
    	submit = new JButton("Guess!"); 
    		submit.addActionListener(this);
    		submit.setAlignmentX(Component.CENTER_ALIGNMENT);
    	
    	//Put components to panels
    	mainPanel.add(topLabel);
    	mainPanel.add(upperLabel);
    	mainPanel.add(wordLabel);
    	mainPanel.add(belowWordLabel);
    	mainPanel.add(guessField);
    	mainPanel.add(guessHistoryLabel1);
    	mainPanel.add(guessHistoryLabel2);
    	mainPanel.add(submit);
    	
    	//Put panels to frame and set visible
    	f.add(mainPanel);   
    	f.getRootPane().setDefaultButton(submit); //Player can press enter
    	f.setVisible(true);
    	f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    	f.addWindowListener(new WindowAdapter() {
    		public void windowClosing(WindowEvent evt) {
    			SendToSocket("ENDGAME");
    			System.exit(0);
    		}
    	});
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		//First, get text field and convert all to uppercase
		playerInput = guessField.getText();	
		currentGuess = playerInput.toUpperCase();
		
		//Check first if the string is empty
    	if (!currentGuess.isEmpty()) {
    		SendGuess();
    		ReceiveUpdateGui();
    		attemptInt = Integer.parseInt(availableAttempt.trim());
    		if (isEnd()){
    			if (isWin()) WinAskReplay();
    			else if(!isWin()) LoseAskReplay();
    		}
    	} else if (currentGuess.isEmpty()){
    		JOptionPane.showMessageDialog(null, "Please guess a letter or word.");
    	}					
	}	
}


