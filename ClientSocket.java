package homework1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientSocket implements Runnable {

    
    private Socket clientSocket;
    private String serverAddress;
    int serverPortInt;
    private BufferedInputStream in = null;
    private BufferedOutputStream out = null;

    private static String msg;
    private static String msg2;   
    private final BlockingQueue queue;
               
    //CONSTRUCTOR
    public ClientSocket(String serverAddress,int serverPortInput,BlockingQueue queue) {
        this.serverAddress = serverAddress;
        this.serverPortInt = serverPortInput;
        this.queue = queue;
    }
    
//This will run automatically when you start the thread
    public void run()
    {
        //ADD CODE HERE
        System.out.println("\n\n++++++++++++++++++++++++++++");
        System.out.println("Thread clientSocket started!");
        System.out.println("++++++++++++++++++++++++++++");
       
        System.out.println(serverAddress);
        System.out.println(serverPortInt);
        
        InitializeConnection();
        
        //First, retreive data and pass to main thread to be splitted
        String ReceivedDataFromServer;
        String ReceivedDataFromMain;
        ReceivedDataFromServer = ReceiveFromServer();
        System.out.println("From server : "+ReceivedDataFromServer);
        SendToMain(ReceivedDataFromServer);
        
        while(true){
            //Wait for the player to guess a letter/word, then send it to server
            ReceivedDataFromMain = ReceiveFromMain();
            if ("end".equals(ReceivedDataFromMain)) break; //Break all if reach end of game
            SendToServer(ReceivedDataFromMain);
            
            //Wait for server reply, then pass any message from server to ClientMain
            ReceivedDataFromServer = ReceiveFromServer();
            SendToMain(ReceivedDataFromServer);
        }
        
        //Program will reach here if it reach end
        CloseConnection();
    }    

    //FUNCTIONS  
    private void InitializeConnection() {           
        clientSocket = null;
        
        //Initialize socket to server
        try  {
            clientSocket = new Socket(serverAddress, serverPortInt);
        } catch (UnknownHostException e)    {
            System.err.println("Don't know about host: " + serverAddress + ".");
            System.exit(1);
        } catch (IOException e)     {
            System.err.println("Couldn't get I/O for the connection to: " + serverAddress + "");
            System.exit(1);
        }
        
        //Open IO stream
        try     {
            in = new BufferedInputStream(clientSocket.getInputStream());
            out = new BufferedOutputStream(clientSocket.getOutputStream());
        } catch (IOException e)   {
            System.err.println(e.toString());
            System.exit(1);
        }    
        System.out.println("Connection initialized");
    }   
    
    //Convert string message to bytestream and send it
    private void SendToServer(String rawMsg) {
        try {
            byte[] toServer = rawMsg.getBytes();
            out.write(toServer, 0, toServer.length);
            out.flush();
        } catch (IOException e) {
            System.err.println(e.toString());
        }        
    }
    
    //Receive bytestream from server, convert to string, return it 
    private String ReceiveFromServer() {
        byte[] fromServer = new byte[4096];
        int bytesRead = 0;
        int n;
        String fromServerString = null;
        
        try {
            while ((n = in.read(fromServer, bytesRead, 256)) != -1)
            {
                fromServerString = new String(fromServer, "UTF-8");     //Convert bytestream to string
            }
        } catch (IOException e) {
            System.err.println(e.toString());
        }      
        return fromServerString;
    }
    
    //Send data to main thread using LinkedBlockingQueue
    private void SendToMain(String msg){
        try {
            queue.put(msg);
        } catch (InterruptedException e) {
            System.err.println(e.toString());
        }
    }

    //Receive data from main thread using LinkedBlockingQueue
    private String ReceiveFromMain(){
        try {
            msg = (String) queue.take();
        } catch (InterruptedException e) {
            System.err.println(e.toString());
        }      
        return msg;
    }        
    
    //To close all kind of connection with server
    private void CloseConnection() {
        try {
            out.close();
            in.close();
            clientSocket.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
        
    }

}
