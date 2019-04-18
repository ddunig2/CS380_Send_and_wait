import java.io.*;
import java.net.*;
import java.util.*;


public class Client {
	// data = 0
	// ack = 1
	// syn = 2
	// fyn = 3
	private InputStream inputStream;
	private DatagramSocket clientSocket;
	private InetAddress IPAddress;
	private byte[] sendData;
	private byte[] receiveData;
	private int nextInLine;
	private static int timeOut;
	private static int size;
	private int port;
	public static void main(String args[]) throws Exception {
		//grab arguments from and parse them
		//to=(timeout time)
		//size=(size of packets)
		parseArgs(args);
		Client cl = new Client();
		//try to connect
		if (cl.connect()) {
			//if we connect succesfuky send packects
			cl.sendPackets();
			//try to close
			if(cl.close()) {
				//if we can close then
				//close sockets
			}
		}
		
	}

	public Client() throws Exception {
		//initialize our variables
		inputStream = new FileInputStream("data.txt");
		clientSocket = new DatagramSocket();
		IPAddress = InetAddress.getByName("localhost");
		sendData = new byte[1024];
		receiveData = new byte[1024];
		port = 7443;
	}

	public boolean connect() throws Exception {
		//set a timeout on the block when trying to connect
		clientSocket.setSoTimeout(100);
		int i = 0;
		//set the syn bit on
		sendData[0] |= (2 << 0);
		//System.out.println(Integer.toBinaryString((int) sendData[0]));
		System.out.println("Trying to connect to port " + port);
		//try to connect 4 times
		while (i < 4) {
			// sendData[0] = (byte) (sendData[0] | 2);
			System.out.print("Attempt# " + (i+1));
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			//send out a syn
			clientSocket.send(sendPacket);
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			// System.out.println(sendData[0] |0);
			try {
				//check to see if we recieved anything yet
				clientSocket.receive(receivePacket);
				//check to see if we got an (ack) for our syn
				if (getPacketType() == 1) {
					nextInLine = getSqAckNum();
					System.out.println(" connected");
					System.out.println("Sending data to port " + port);
					System.out.println("this is the next sequence num " + nextInLine);
					return true;
				}
			} catch (SocketTimeoutException e) {
				//if our timer runs out it throws an exception (error)
				//in case it does catch it
				System.out.println(" failed");
			}
			i++;
		}
		//if we try 4 times and we cant connect then stop trying
		System.out.println("cant connect right now");
		return false;
	}
	
	//very similar to connect
	public boolean close() throws IOException {
		int i = 0;
		//set the fyn bit on
		sendData[0] |= (3 << 0);

		// System.out.println(Integer.toBinaryString((int) sendData[0]));
		while (i < 4) {
			// sendData[0] = (byte) (sendData[0] | 2);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 7443);
			clientSocket.send(sendPacket);
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			// System.out.println(sendData[0] |0);
			try {
				System.out.println(getSqAckNum());
				clientSocket.receive(receivePacket);
				if (getPacketType() == 1) {
					return true;
				}
			} catch (SocketTimeoutException e) {
				// System.out.println("nohting to connect ot at the moment");
			}
			i++;
		}
		return false;
	}
	
	//parse the type of packet we recieved
	public int getPacketType() {
		return receiveData[0] & 3;
	}

	//parse the sequence number from the header
	public int getSqAckNum() {
		//shift our bits to the bit corresponding with the sequence number(3rd and 4th bit)
		int num = sendData[0] >> 2;
		return num & 3;
	}
	
	
	public void sendPackets() throws Exception {
		while (true) {
			int len = inputStream.read(sendData, 1, 180);
			//System.out.println(len);
			sendData[0] = (byte) 0xde;

			// for(byte b: sendData) {
			// System.out.print(UnicodeFormatter.byteToHex(b));
			// }

			if (len < 32) {
				System.out.println("This is the end");
				break;
			}
			//System.out.println(len++);
			// String sentence = inFromUser.readLine();
			// sendData = sentence.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 7443);
			clientSocket.send(sendPacket);
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			// clientSocket.setSoTimeout(100);
			// try {

			// while (i < 10) {

			clientSocket.receive(receivePacket);
			// }

			// } catch (SocketTimeoutException e) {
			// System.out.println("timed out");
			// }
			String modifiedSentence = new String(receivePacket.getData());
			System.out.println("FROM SERVER:" + modifiedSentence);
		}
		clientSocket.close();
	}
	public static void parseArgs(String args[]) {
		for (String s : args) {
			if (s.startsWith("to=")) {
				try {
					timeOut = Integer.parseInt(s.substring(3));
				} catch (Exception e) {
					System.out.println("timeout args error, defualts to 100 millis");
					timeOut = 100;
				}
			} else if (s.startsWith("size=")) {
				try {
					size = Integer.parseInt(s.substring(5));
				} catch (Exception e) {
					System.out.println("size args error, defaults to 1500");
					size = 1500;
				}
			}

		}
	}
}