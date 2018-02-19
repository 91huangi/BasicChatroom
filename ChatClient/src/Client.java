import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	
	public static boolean closed = false;
	
	public static void main(String[] args) {
		Socket clientSocket;
		int portNumber = Integer.parseInt(args[0]);
		
		try {
			InetAddress address = InetAddress.getByName("localhost");
			clientSocket= new Socket(address, portNumber);
			PrintStream os = new PrintStream(clientSocket.getOutputStream());

			RecieveThread r = new RecieveThread(clientSocket);
			r.start();
			
			String input;
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			while(!closed) {
				input = br.readLine().trim();
				os.println(input);
			}
			
			os.close();
			clientSocket.close();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}

class RecieveThread extends Thread {
	
	Socket socket;
	
	public RecieveThread(Socket socket) {
		this.socket=socket;
	}
	
	
	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		
		try {
			while(true) {
				DataInputStream is = new DataInputStream(socket.getInputStream());
				String line;
				if((line = is.readLine()) != null) System.out.println(line);
				if(line.equals("***EXIT***")) break;
			}
			Client.closed = true;
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
