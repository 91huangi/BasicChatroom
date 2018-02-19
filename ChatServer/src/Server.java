import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private static ServerSocket serverSocket = null;
	private static Socket clientSocket;
	private static final ClientThread[] clients = new ClientThread[4];
	
	public static void main(String[] args) {
		
		// searching for open port
		int portNumber = 1024;
		
		for(portNumber = 1024; portNumber < 2000; portNumber ++) {
			try {
				serverSocket = new ServerSocket(portNumber);
				System.out.println("Server started at "+portNumber);
				break;
			} catch(IOException e) {
				System.out.println(portNumber + " is in use, scanning for next port");
				continue;
			}
		}

		
		try {
			
			// continuously wait for client to connect
			while(true) {
				clientSocket = serverSocket.accept();
				
				int i=0;
				
				for(i=0; i<4; i++) {
					if(clients[i]==null) {
						clients[i] = new ClientThread(portNumber, clientSocket, clients);
						clients[i].start();
						break;
					}
				}
				
				if(i==4) {
					PrintStream os = new PrintStream(clientSocket.getOutputStream());
					os.println("Chat room is full, please try again later");
					os.close();
					System.out.println("5th person trying to sign in");
				}
				
				
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}
	
}

class ClientThread extends Thread {
	
	int portNumber;
	Socket socket;
	PrintStream os;
	ClientThread[] clients;
	
	public ClientThread(int portNumber, Socket socket, ClientThread[] clients) {
		this.portNumber = portNumber;
		this.socket=socket;
		this.clients = clients;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		try {
			DataInputStream is = new DataInputStream(socket.getInputStream());
			os = new PrintStream(socket.getOutputStream());
		
			String line;
			while(true) {
				if((line = is.readLine())!= null) {
					for(int i=0;i<clients.length;i++) {
						if(clients[i] != null) clients[i].os.println(line);
					}
				}
			}

		} catch (IOException e) {
			
		}
		
	}
}
