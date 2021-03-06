package homework1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import java.util.concurrent.ArrayBlockingQueue;

public class ClientSocket implements Runnable {

	private Socket clientSocket;
	private String serverAddress;
	int serverPortInt;
	private BufferedInputStream in = null;
	private BufferedOutputStream out = null;

	private static String msg;
	private static String msg2;
	private final BlockingQueue queue;

	private String ReceivedDataFromServer;
	private String ReceivedDataFromMain;

	// CONSTRUCTOR
	public ClientSocket(String serverAddress, int serverPortInput, BlockingQueue queue) {
		this.serverAddress = serverAddress;
		this.serverPortInt = serverPortInput;
		this.queue = queue;
	}

	// This will run automatically when you start the thread
	public void run() {
		// Initialize socket connection
		InitializeConnection();		
		Sleep(100);

		// First time receive message from server
		String msga = null;
		msga = ReceiveFromServer();

		// Send word and attempt to main, unsplitted		
		SendToMain(msga);
		Sleep(500); // Sleep to give MainClient time to take the data
		// While playing game
		String update;
		while (true) {
			update = ReceiveFromMain();			
			SendToServer(update);
			if (update == "ENDGAME")
				break; // Break if receive EKZIT from Main
			Sleep(100);

			update = ReceiveFromServer();			
			SendToMain(update);
			Sleep(100);
		}

		// Program will reach here if it reach end
		CloseAll();
	}

	// FUNCTIONS
	private void InitializeConnection() {
		clientSocket = null;

		// Initialize socket to server
		try {
			clientSocket = new Socket(serverAddress, serverPortInt);
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: " + serverAddress + ".");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: " + serverAddress + "");
			System.exit(1);
		}

		// Open IO stream
		try {
			in = new BufferedInputStream(clientSocket.getInputStream());
			out = new BufferedOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			System.err.println(e.toString());
			System.exit(1);
		}
	}

	// Convert string message to bytestream and send it
	private void SendToServer(String rawMsg) {
		try {
			byte[] toServer = rawMsg.getBytes();
			out.write(toServer, 0, toServer.length);
			out.flush();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}

	// Receive bytestream from server, convert to string, return it
	private String ReceiveFromServer() {
		String fromServerString = "";

		try {
			byte[] fromServer = new byte[40];
			int n = in.read(fromServer, 0, fromServer.length);
			fromServerString = new String(fromServer);
		} catch (IOException e) {
			System.err.println(e.toString());
		}
		return fromServerString;
	}

	// Send data to main thread using LinkedBlockingQueue
	private void SendToMain(String msg) {
		try {

			queue.put(msg);
		} catch (InterruptedException e) {
			System.err.println(e.toString());
		}
	}

	// Receive data from main thread using LinkedBlockingQueue
	private String ReceiveFromMain() {
		try {
			msg = (String) queue.take();
		} catch (InterruptedException e) {
			System.err.println(e.toString());
		}
		return msg;
	}

	// To close all kind of connection with server
	private void CloseAll() {
		try {
			out.close();
			in.close();
			clientSocket.close();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}

	private void Sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
