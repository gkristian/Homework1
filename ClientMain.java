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
	private Label scoreLabel,wordLabel,attemptLabel,guessHistoryLabel;
	private JTextField guessField;
	private JButton submit;
	
    String score, newscore, word, availableAttempt, gameStatus, playerInput, currentGuess;
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
    	System.out.println("1 Main : Send initialize socket");
    	InitializeSocket(); //Just start thread and open connection to server 	
    	
    	//First time. Socket receive message, put to queue
    	//then Main take it from queue
    	String msga = null;
    	msga = ReceiveFromSocket();
    	System.out.println("4 Main : Receive : "+msga);
    	
    	//Split message and assign to specified variable
    	CustomSplitter(msga);
    	word = AfterSplit[0];
    	availableAttempt = AfterSplit[1];
    	score = AfterSplit[2];
    	 
    	System.out.println(word);
    	System.out.println(availableAttempt);
    	
    	
    	//Setup first time GUI
    	//while (isStillPlaying){
    	SetupGui();
    	//}
    	//while (still playing) {
    	//	receive
    	//if word = WIN/LOSE then break
    	//	askinput
    	//	sendtoserver
    	//}
    	//endGame;
    	
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
        	serverPortInput = "4444";   
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
    
    private void SendGuess(){
		//When user press button
		//First, get text field and convert all to uppercase -> need this??
		playerInput = guessField.getText();	
		currentGuess = playerInput.toUpperCase();
		
		//Apply space and append to history to be shown
		guessHistory = guessHistory+" "+currentGuess;

		//Send to Server
		System.out.println("1 Main : Send to socket : "+currentGuess);
		SendToSocket(currentGuess);
		Sleep(100);//Sleep to give socket time to take the data
    }
    
    private void ReceiveUpdateGui(){
		//Receive back update from server
		String msgb = ReceiveFromSocket();
		System.out.println("4 Main : Update fr socket : "+msgb);
		System.out.println("===================================");
		
		//Do the rest
		CustomSplitter(msgb);
    	word = AfterSplit[0];
    	availableAttempt = AfterSplit[1];
    	newscore = AfterSplit[2];
    	    	
    	//Update
    	scoreLabel.setText("Your score now : " + newscore);
		wordLabel.setText("Come on, guess it		: " + word);
		attemptLabel.setText("Your available attempts	: " + availableAttempt);
		guessHistoryLabel.setText("You have guessed			: "+guessHistory);
		guessField.setText("");    	
    }
    
    private Boolean isEnd(){
		int scoreInt = Integer.parseInt(score.trim());
    	int newscoreInt = Integer.parseInt(newscore.trim());
		Boolean result = null;
    	//CHECK IF THE GAME REACH END OR NOT 
    	//Win
		if (newscoreInt != scoreInt) result= true;    		
    	else if (newscoreInt == scoreInt) result= false;    	
    	return result;
    }
    
    private Boolean isWin(){
		int scoreInt = Integer.parseInt(score.trim());
    	int newscoreInt = Integer.parseInt(newscore.trim());
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
    	int res = JOptionPane.showOptionDialog(null,"You Lose! Replay?","Congratulations", 0,JOptionPane.INFORMATION_MESSAGE,null,options,null);
    	switch (res) {
	    	case JOptionPane.YES_OPTION:
	    		SendToSocket("NEWGAME");
	    		Sleep(100);
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
    	f.setBounds(0,0,500,300); 
    	
    	//Specify panel. Only use 1 big panel 
    	mainPanel = new JPanel();
    	mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    	
    	//Specify components. ADD COMPONENT ABOVE!!!!
    	scoreLabel = new Label("Your score now : " + score);
    	wordLabel = new Label("Come on, guess it		: " + word);
    	attemptLabel = new Label("Your available attempts	: " + availableAttempt);
    	guessHistoryLabel = new Label("You have guessed			: " + guessHistory);
    	guessField = new JTextField(20);
    	submit = new JButton("Guess!"); 
    		submit.addActionListener(this); 
    	
    	//Put components to panels
    	mainPanel.add(scoreLabel);
    	mainPanel.add(wordLabel);
    	mainPanel.add(attemptLabel);
    	mainPanel.add(guessHistoryLabel);
    	mainPanel.add(guessField);
    	mainPanel.add(submit);
    	
    	//Put panels to frame and set visible
    	f.add(mainPanel);     	
    	f.setVisible(true);
    	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		SendGuess();
		ReceiveUpdateGui();
		if (isEnd()){
			if (isWin()) WinAskReplay();
			else if(!isWin()) LoseAskReplay();			
		}
	}
}
