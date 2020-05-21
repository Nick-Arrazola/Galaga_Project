import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.util.Scanner;

public class GalagaClient {

	private InetAddress serverAddress;
	private final int PORT_NUMBER;
	private DatagramSocket mainSocket;
	private final int MAX_SIZE = 1024; //Might be too much for out current needs
	private byte[] recieveData;
	private byte[] sendData;
	private Scanner scan;
	
	/*
	 * TODO: possibly create some type of optimization for byte array so we aren't wasting too much space in memory
	 * 	     this can be done by keeping track of the next available spot in the byte[] and checking if the length 
	 *       of the data being filled plus the available spot is out of bounds. If so, call method that will create
	 *       a new byte[] and re fill the data in.
	 */
	
	/*
	 * TODO: Should maybe add datagramPacket headers to signify what is being received (like one that specifies a 
	 *       connectivity packet, a ship position packet, or a bullet position packet).
	 */
	
	/**
	 * Constructor that accepts port number
	 * @param port - port number to be used
	 */
	public GalagaClient(int port) {
		
		PORT_NUMBER = port;
		scan = new Scanner(System.in);
	}
	
	/**
	 * This method activates the Galaga client
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
				
				//TODO: possibly send the 'connected' datagram packet here
				
				//The 'receive(...)' method fills up the buffer of the packet we passed into it with the data received
				mainSocket.receive(packet);
				
				if
				sendUserName(new String(packet.getData(), 0, packet.getLength()), packet);						
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
	 * This method deactivates the Galaga client
	 */
	public void close() {
		mainSocket.close();
	}	
	
	private void sendUserName(String msg, DatagramPacket packet) throws IOException, PortUnreachableException {
		
		System.out.println(msg);
		byte[] name = scan.nextLine().getBytes();
		DatagramPacket userName = new DatagramPacket(name, name.length, packet.getAddress(), packet.getPort());
		mainSocket.send(userName);
		
	}
}
