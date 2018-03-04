/*
 * Ivan Huang
 * 1001289440
 * code inspired from: http://makemobiapps.blogspot.com/p/multiple-client-server-chat-programming.html
 */

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Client extends Application implements EventHandler<ActionEvent>{
	
	static int portNumber;		// client port number
	Socket clientSocket;		// client socket
	PrintStream os;				// client output stream
	
	
	
	TextArea txtaDisplay;		// textarea for displaying chats
	TextField txtInput;			// textinput for getting client input
	Button btnSend;				// send button
	
	
	/*
	 * @params: primaryStage (Stage): window for display
	 * @returns: void
	 * Start function, loads widgets onto stage
	 * (non-Javadoc)
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
				
		
		
		primaryStage.setTitle("Chat Window");
		
		// loading children
		txtaDisplay = new TextArea();
		txtaDisplay.setEditable(false);
		
		txtInput = new TextField();
		
		btnSend = new Button();
		btnSend.setText("Send");
		btnSend.setOnAction(this);
		
		
		// setting layout
		GridPane layout = new GridPane();
		layout.setConstraints(txtaDisplay, 1, 1, 10, 1);
		layout.setConstraints(txtInput, 1, 2, 8, 1);
		layout.setConstraints(btnSend, 10, 2, 1, 1);
		layout.getChildren().addAll(txtaDisplay, txtInput, btnSend);
		Scene scene = new Scene(layout, 500, 500);
		
		
		// will connect to client and create listening thread
		runInBackground(this);

		// setting scene
		primaryStage.setScene(scene);
		primaryStage.show();
		
		

	}
	
	/*
	 * @params: event (ActionEvent): the event occurring
	 * @returns: void
	 * Handles click evenst
	 * (non-Javadoc)
	 * @see javafx.event.EventHandler#handle(javafx.event.Event)
	 */
	@Override
	public void handle(ActionEvent event) {
		// if the event came from send button, call post method
		if(event.getSource()==btnSend) {
			post(txtInput.getText());
			txtInput.setText("");
		}
	}
	
	/*
	 * @params: none
	 * @returns: void
	 * On closing frame, send exit string to chat server
	 * (non-Javadoc)
	 * @see javafx.application.Application#stop()
	 */
	@Override
	public void stop() {
		
		// will notify server that client has left chatroom with unique exit string
		try {
			post("3x!t$tr!ng");
			os.close();
			clientSocket.close();			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	/* 
	 * @params: none
	 * @returns: (String): current date in HTTP format
	 * Taken from stackoverflow...
	 * https://stackoverflow.com/questions/7707555/getting-date-in-http-format-in-java
	 */
	public String getHttpDate() {
		
		// get calendar instance, and use date format to convert to HTTP standard
	    Calendar calendar = Calendar.getInstance();
	    SimpleDateFormat dateFormat = new SimpleDateFormat(
	        "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
	    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	    return dateFormat.format(calendar.getTime());
	}
	
	/*
	 * @params: content (String): content to be posted to server
	 * @returns: void
	 * Converts string into HTTP post format
	 */
	public void post(String content) {
		
		// add message headers
		String postMsg = "POST http://localhost:"+String.valueOf(portNumber)+" HTTP/1.1\n";
		postMsg += "Host: http://localhost\n";
		postMsg += "User-Agent: Mozilla/5.0\n";
		postMsg += "Content-Type: text/html; charset=utf-8\n";
		postMsg += "Content-Length: "+String.valueOf(content.length())+"\n";
		postMsg += "Date: "+getHttpDate() + "\n";
		postMsg += "\n";
		postMsg += content;
		os.println(postMsg);			// writes to server
	}
	
	/*
	 * @params: client (Client): client obj that contains socket and output stream
	 * @returns: void
	 * function connects to host at port and creates an output stream that interacts
	 * with the UI
	 */
	public void runInBackground(Client client) {
		
		
		try {
			// connecting to server at portNumber
			InetAddress address = InetAddress.getByName("localhost");
			clientSocket= new Socket(address, portNumber);
			// creating new output stream
			os = new PrintStream(clientSocket.getOutputStream());

			// starting thread to listen for server messages
			RecieveThread r = new RecieveThread(clientSocket, client);
			r.start();

			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
		
	/*
	 * @params: args (String[]): port number
	 * @returns: void
	 * Main function
	 */
	public static void main(String[] args) {
		
		portNumber = Integer.parseInt(args[0]);

		launch(args);
		
		
	}
}


class RecieveThread extends Thread {
	
	Socket socket;		// listening socket
	Client client;		// client object (needed to update text area)
	
	/*
	 * Constructor
	 */
	public RecieveThread(Socket socket, Client client) {
		this.socket=socket;
		this.client = client;
	}
	
	
	@SuppressWarnings("deprecation")
	@Override
	/*
	 * @params: none
	 * @returns: void
	 * Runs a seperate thread that listens to server and echos response
	 * back to the UI
	 */
	public void run() {
		
		try {
			
			// continuously listen to server
			while(true) {
				// get input from server
				DataInputStream is = new DataInputStream(socket.getInputStream());
				String line;
				// update text area with server response
				if((line = is.readLine()) != null) client.txtaDisplay.appendText(line+"\n");
			}
		} catch (IOException e) {
		}

	}
}
