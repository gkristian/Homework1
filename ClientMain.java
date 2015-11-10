package homework1;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author Gregorius Kristian Purwidi
 */
public class ClientMain {
    
    Scanner userInput = new Scanner(System.in);
    private static String serverAddressInput;
    private static String serverPortInput;
    private static String msg;
    private static String msg2;
    private int serverPortInt;
    String[] AfterSplit;
    public BlockingQueue queue = new LinkedBlockingQueue(256);
    
    public static void main(String[] args) throws IOException, InterruptedException {      
        new ClientMain();
    }
    
    //CONSTRUCTOR
    public ClientMain() throws InterruptedException {
        System.out.println("-----------------------");
        System.out.println("Hello from main thread!");
        System.out.println("-----------------------");        

        Boolean NewGame = true;        
        System.out.print("New Game? (Y/N) :  ");
        String yn = userInput.next();
        
        AskAddressAndPort();
        InitializeSocket(); 
        
        if ("N".equals(yn))  NewGame = false;
        
        //Loop when user choose not to exit
        while(NewGame){
            //Game start
            InGame(); 
            
            //Ask if user want to replay
            System.out.print("Replay Game? (Y/N) :  ");
            yn = userInput.next();
            if ("N".equals(yn))  NewGame = false;            
        }
        
        SendToSocket("end");
        System.out.println("Closing connection to server....");            
        TimeUnit.SECONDS.sleep(2);
        System.out.println("Goodbye!");
        TimeUnit.SECONDS.sleep(1);   
        System.exit(0);
    }
    
    //THE FUNCTIONS    
    //Ask user input about address and port of the server
    private void AskAddressAndPort(){
        
        System.out.print("Server Address : ");
        serverAddressInput = userInput.next();
        System.out.print("Server Port : ");
        serverPortInput = userInput.next();
        serverPortInt = Integer.parseInt(serverPortInput);
    }
    
    //Connect to server by start a thread
    private void InitializeSocket(){
        (new Thread(new ClientSocket(serverAddressInput,serverPortInt,queue))).start();
    }
    
    private void SendToSocket(String msg){
        try {
            queue.put(msg);
        } catch (InterruptedException e) {
            System.err.println(e.toString());
        }
    }
    
    private String ReceiveFromSocket(){
        try {
            msg = (String) queue.take();
        } catch (InterruptedException e) {
            System.err.println(e.toString());
        }      
        return msg;
    }
    
    private void CustomSplitter(String rawString){
        AfterSplit = rawString.split("\\s+");  
    }
    
    private void InGame(){
    	//Loop once for 1 input of letter or word
        while(true){
            //First, receive the data
            String ReceivedData = ReceiveFromSocket();
            
            //Split received message into 3 strings and print it
            CustomSplitter(ReceivedData);
            
            String word = AfterSplit[0];
            String availableAttempt = AfterSplit[1];
            String gameStatus = AfterSplit[2];
            
            System.out.println("Word            : " + AfterSplit[0]);
            System.out.println("Avail Attempt   : " + AfterSplit[1]);
            System.out.println("Status          : " + AfterSplit[2]);       
            System.out.println("--------------------------");
            
            //Player guess a letter or word, then send it to server
            System.out.print("Your guess : (or type 'end' to exit)");
            String guess = userInput.next();
            SendToSocket(guess);
            if ("end".equals(guess)) break;
        }    
    }    
}
