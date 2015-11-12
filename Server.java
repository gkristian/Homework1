/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package homework1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{

    public static void main(String[] args) throws IOException
    {
        boolean listening = true;
        ServerSocket serverSocket = null;
        System.out.println("SERVER STARTED");
        try
        {
            serverSocket = new ServerSocket(1234);
        } catch (IOException e)
        {
            System.err.println("Could not listen on port: 1234.");
            System.exit(1);
        }

        while (listening)
        {
            Socket clientSocket = serverSocket.accept();
            (new Handler(clientSocket)).start();
            
        }

        serverSocket.close();
    }
}
