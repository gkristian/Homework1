/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package homework1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Handler extends Thread
{
    private Socket clientSocket;

    public Handler(Socket clientSocket)
    {
        this.clientSocket = clientSocket;
    }

    public void run()
    {
        System.out.println("CLIENT CONNECTED");
        BufferedInputStream in;
        BufferedOutputStream out;

        try   {
            in = new BufferedInputStream(clientSocket.getInputStream());
            out = new BufferedOutputStream(clientSocket.getOutputStream());
        } catch (IOException e)  {
            System.out.println(e.toString());
            return;
        }

        try       {
            //try { 
            //    Thread.sleep(3000);
            //} catch (InterruptedException ex) {
            //    Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
            //} 
            
        	//while(true){
	            String rawMsg = "_AH_A 10 1";
	            byte[] toServer = rawMsg.getBytes();
	            out.write(toServer, 0, toServer.length);
	            out.flush();
	            System.out.println("Message sent "+toServer);
	            //byte[] fromServer = new byte[4096];
	            //int bytesRead = 0;
	            //int n;
	            //String fromServerString = null;
	            //while ((n = in.read(fromServer, bytesRead, 256)) != -1)    {
	            //    fromServerString = new String(fromServer, "UTF-8");     //Convert bytestream to string
	            //}
        	//}
        } catch (IOException e)   {
            System.out.println(e.toString());
        }

        //try        {
        //    out.close();
        //    in.close();
        //    clientSocket.close();
        //} catch (IOException e)        {
        //    e.printStackTrace();
        //}
    }

}
