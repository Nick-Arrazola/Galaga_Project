import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class holds the functionality of the central server for the Galaga mulitplayer
 * @author Nick Arrazola
 *
 */
public class GalagaServer{

	private Map<String, DatagramPacket> playerData; //To store the data of the players who are in the game
	private final int PORT_NUMBER;
	private DatagramSocket mainSocket;
	private final int MAX_SIZE = 1024; //Might be too much for out current needs
	private byte[] recieveData;
	private byte[] sendData;
	
	//TODO: possibly create some type of optimization for byte array so we aren't wasting too much space in memory
	
	/**
	 * Constructor that accepts port number
	 * @param port - port number to be used
	 */
	public GalagaServer(int port) {
		
		PORT_NUMBER = port;
		playerData = new HashMap<String, DatagramPacket>();
	}
	
	/**
	 * This method activates the Galaga server
	 */
	public void start() {
		
		try {
			mainSocket = new DatagramSocket(PORT_NUMBER);
		}
		catch(SocketException ex) {
			ex.printStackTrace();
		}
		
		//Lambda expression to shorten the run method
		Thread serverThread = new Thread( () -> {
			listen();
		});
		
		serverThread.start();
	}
	
	/**
	 * This method is used to 'listen' for any client trying to connect to it
	 */
	public void listen() {
		
		while(true) {
			
			DatagramPacket packet = new DatagramPacket(recieveData, MAX_SIZE);
			int available = 0;
			
			try {
				//The 'receive(...)' method fills up the buffer of the packet we passed into it with the data received
				mainSocket.receive(packet);
				//available = packet.getLength();
				String msg = new String(packet.getData(), 0, packet.getLength());
				
				if(msg.equals("Connected") && askUser(packet, available))
					makeClientThread();
					
			}
			catch(PortUnreachableException ex) {
				
				System.out.println("PortUnreachableException caught in server side.\n Unable to recieve packet\n");
				ex.printStackTrace();
			}
			catch (IOException ex) {
				
				System.out.println("1. IOException Caught in server side.\n Unable to vertify user\n");
				ex.printStackTrace();
			}
		}
		
	}
	
	/**
	 * This method is used to send to data to whoever is associated IP address and port number
	 * @param data - the byte[] to be sent
	 * @param address - the address the data will be sent to
	 * @param port - the specific application / process that will receive the data
	 */
	public void send(byte[] data, InetAddress address, int port) {
		
		DatagramPacket sendingPacket = new DatagramPacket(data, data.length, address, port);
		
		try {
			mainSocket.send(sendingPacket);
		}
		catch(PortUnreachableException ex) {
			
			System.out.println("PortUnreachableException caught in server side.\n Unable to send packet\n");
			ex.printStackTrace();
		}
		catch (IOException ex) {
			
			System.out.println("2. IOException Caught in server side.\n Unable to send packet\n");
			ex.printStackTrace();
		}
		
	}
	
	/**
	 * This method deactivates the Galaga server
	 */
	public void close() {
		mainSocket.close();
	}
	
	private boolean askUser(DatagramPacket packet, int available) throws IOException{
	    
		String userName = "";
		boolean response = true;
		
		do {
			String prompt = "Please enter a username: ";
			sendData = prompt.getBytes();
			DatagramPacket userPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
			
			mainSocket.send(userPacket);
			
			recieveData = new byte[MAX_SIZE];
			DatagramPacket vertifyPacket = new DatagramPacket(recieveData, MAX_SIZE);
				
			//"This method blocks until a datagram is received."
			mainSocket.receive(vertifyPacket);
			userName = new String(vertifyPacket.getData(), 0, vertifyPacket.getLength());
			//available += packet.getLength();
					
			if(!playerData.containsKey(userName)) {
				playerData.put(userName, vertifyPacket);
				response = false;
			}
			
		}while(response);		
		
		return !response;
	}
	
	private void makeClientThread() {
		
		Thread clientThread = new Thread( () -> { 
			
			while(true) {
				try {
					recieveData = new byte[MAX_SIZE]; //Might cause trouble with multiple threads
					DatagramPacket recievingPacket = new DatagramPacket(recieveData, MAX_SIZE);
					mainSocket.receive(recievingPacket);
					
					for(String user : playerData.keySet()) {
						
						DatagramPacket current = playerData.get(user);
						
						//Comparing if the address that was recently received from is the same as the current address in the hashMap
						if(current.getAddress().equals(recievingPacket.getAddress())) 
							continue;
						
						byte[] sendingData = recievingPacket.getData();
						DatagramPacket sendingPacket = new DatagramPacket(sendingData, sendingData.length, current.getAddress(), current.getPort());
						mainSocket.send(sendingPacket);
					}
				} 
				catch (IOException e) {
					System.out.println("3. IOException Caught in client thread.");
					e.printStackTrace();
				}
			}
			
		});
		
		clientThread.start();
	}
	
}
	
	
