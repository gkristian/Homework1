package homework1;

import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerHandler extends Thread {
	//public static final String FilePath = "C:\\Users\\MyPro\\Documents\\NetBeansProjects\\Homework1\\src\\homework1\\Words.txt";
	public static final String FilePath = "C:\\Users\\Words.txt";
	private Socket client_Socket;
	private BufferedInputStream in;
	private BufferedOutputStream out;
	private String fileName = null;
	private int trialNum;
	private int gameScore = 0;
	private String selectedWord, CurrentWord, inputFrClient, updateWord = null;

	boolean gameStatus = false;
	boolean isStillPlaying = true, isInGame = true;

	char[] charWord, CurrentArr, guessedChar;

	// Default Constructor which got clientSpckets and Handle them
	public ServerHandler(Socket clientSocket) // clientSocket comes from
												// ClientSocket
	{
		this.client_Socket = clientSocket;
	}

	public void run() {
		try {
			in = new BufferedInputStream(client_Socket.getInputStream());
			out = new BufferedOutputStream(client_Socket.getOutputStream());
		} catch (IOException ex) {
			Logger.getLogger(ServerHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
		while (isStillPlaying) {
			playGame();
		}
		// Player doesn't want to play again.
		CloseAll();
		System.out.println("One client released.");
		return;
	}

	public void playGame() {
		try {
			// First time client calls server
			startNewGame();
			System.out.println("Start new game. Choosen word : " + selectedWord);
		} catch (IOException ex) {
			Logger.getLogger(ServerHandler.class.getName()).log(Level.SEVERE, null, ex);
		}

		//
		while (isInGame) {
			inputFrClient = receiveFromClient();
			System.out.println("Input from client : " + inputFrClient);
			// we want to check the input from client
			String NewGame = "NEWGAME";
			String EndGame = "ENDGAME";

			if (inputFrClient.equals(NewGame)) {
				// isStillPlaying=true;
				// isInGame=false;
				break;
			} else if (inputFrClient.equals(EndGame)) {
				Sleep(1000);
				isStillPlaying = false;
				isInGame = false;
				CloseAll();
				break;
			} else {
				wordChecking();
				sendToClient(updateWord);
			}
		}
		Sleep(500);		
		// ONE game end here
	}

	public String ChooseWord() throws IOException {
		File file;
		FileInputStream fileStream;
		FileReader fileReader = null;
		BufferedReader bufferedReader;

		Random rand = new Random();
		int firstLine = 0;
		int lastLine = 25143;
		int R = rand.nextInt(lastLine - firstLine) + firstLine;

		String line = null;
		// reading from words.txt
		file = new File(FilePath);
		try {
			fileStream = new FileInputStream(file);
			fileReader = new FileReader(file);
		} catch (FileNotFoundException ex) {
			Logger.getLogger(ServerHandler.class.getName()).log(Level.SEVERE, null, ex);
		}

		bufferedReader = new BufferedReader(fileReader);
		for (int i = 0; i < R; i++) {
			bufferedReader.readLine();
		}
		line = bufferedReader.readLine();
		return line;
	}

	// Input word from choose word and send to client
	public void startNewGame() throws IOException {
		CurrentWord = "";
		selectedWord = ChooseWord();
		// selectedWord = "wordword";
		trialNum = (selectedWord.length()) + 3;
		for (int i = 0; i < selectedWord.length(); i++) {
			CurrentWord += "_";
		}
		sendToClient(CurrentWord);
		updateWord = CurrentWord;
	}

	public void wordChecking() {
		// Check guess (word) and update gameScore
		if (inputFrClient.length() > 1) {
			if ((trialNum >= 2) && !(selectedWord.equalsIgnoreCase(inputFrClient))) {
				// Just continue
				updateWord = CurrentWord;
				trialNum--;
			} else if ((trialNum >= 1) && (selectedWord.equalsIgnoreCase(inputFrClient))) {
				// Wins!
				gameStatus = true;
				updateWord = inputFrClient;
				gameScore++;
				// trialNum--;
			} else {
				// Lose
				gameScore--;
				trialNum--;
			}
		}

		// Check guess (letter)
		else if (inputFrClient.length() == 1) {
			charWord = selectedWord.toCharArray();
			CurrentArr = CurrentWord.toCharArray();
			guessedChar = inputFrClient.toCharArray();

			// Updating the currentWord
			for (int i = 0; i < selectedWord.length(); i++) {
				if (Character.toUpperCase(charWord[i]) == guessedChar[0]) {
					CurrentArr[i] = guessedChar[0];
				}
				// updateWord = new String(CurrentArr);
			}
			CurrentWord = new String(CurrentArr);

			// Updating the gameScore
			if ((trialNum >= 2) && !(selectedWord.equalsIgnoreCase(CurrentWord))) {
				// Just continue
				if (updateWord.equalsIgnoreCase(CurrentWord)) { // Means that no
																// changes
					trialNum--;
				} else {
					updateWord = CurrentWord;
				}

			} else if ((trialNum >= 1) && (selectedWord.equalsIgnoreCase(CurrentWord))) {
				// Wins!
				gameStatus = true;
				updateWord = new String(CurrentArr);
				gameScore++;
				// trialNum--;
			} else {
				// Lose
				gameScore--;
				trialNum--;
			}
		}
	}

	public void sendToClient(String msgToClient) {
		// writing or send
		try {
			String delimiter = "#";
			String sendToClient = msgToClient + delimiter + trialNum + delimiter + gameScore;			
			byte[] outputToClient = (sendToClient).getBytes();
			out.write(outputToClient);
			out.flush();
		} catch (IOException ex) {
			System.err.println("IOException while sending object to client");
		}
	}

	public String receiveFromClient() {
		String msgCln = null;
		byte[] bt = new byte[256];
		int bytesRead = 0;
		int n;
		try {
			while ((n = in.read(bt, bytesRead, 64)) != -1) {
				bytesRead += n;
				if (bytesRead == 256) {
					break;
				} else if (in.available() == 0) {
					break;
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(ServerHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
		msgCln = new String(bt);
		msgCln = msgCln.substring(0, bytesRead);
		return msgCln; // word which is received from Client
	}

	private void Sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void CloseAll() {
		try {
			out.close();
			in.close();
			client_Socket.close();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}

}
