
package homework1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.lang.String;
import java.util.Scanner;


public class Client {

    public static void main(String[] args) throws IOException
    {
        Socket clientSocket = null;
        String serverAddress = "130.229.148.209";  
        
        try
        {
            clientSocket = new Socket(serverAddress, 4444);
        } catch (UnknownHostException e)
        {
            System.err.println("Don't know about host: " + serverAddress + ".");
            System.exit(1);
        } catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection to: " + serverAddress + "");
            System.exit(1);
        }

        BufferedInputStream in = null;
        BufferedOutputStream out = null;

        try
        {
            in = new BufferedInputStream(clientSocket.getInputStream());
            out = new BufferedOutputStream(clientSocket.getOutputStream());
        } catch (IOException e)
        {
            System.out.println(e.toString());
            System.exit(1);
        }
        //Ask user for input message
        String rawMsg;
        Scanner userInput = new Scanner(System.in);
        rawMsg = userInput.nextLine();
        System.out.println("You write : \n"+rawMsg);
                
        //Sending message to server
        byte[] toServer = rawMsg.getBytes();
        out.write(toServer, 0, toServer.length);
        out.flush();

        //Receive message from server     
        byte[] fromServer = new byte[4096];
        int bytesRead = 0;
        int n;
        String fromServerString = null;
        
        while ((n = in.read(fromServer, bytesRead, 256)) != -1)
        {
            fromServerString = new String(fromServer, "UTF-8");     //Convert bytestream to string           
            System.out.println("Received message : \n"+fromServerString);
        }
        
        //Split received message into 3 strings and print it
        String word;
        String availableAttempt;
        String gameStatus;
        String[] splited;
        
        splited = fromServerString.split("\\s+");
        System.out.println(splited[0]); //word
        System.out.println(splited[1]); //attempt
        System.out.println(splited[2]); //status

        out.close();
        in.close();
        clientSocket.close();
    }
}
