import java.io.*;
import java.net.*;
import java.util.Stack;

class Server {
	private DatagramSocket serverSocket;
	private InetAddress IPAddress;
	private byte[] sendData;
	private byte[] receiveData;
	private int nextInLine;
	private int timeOut;
	private int size;
	private int port;
	private Stack<Integer> sequenceNumbers;

	// data = 0
	// ack = 1
	// syn = 2
	// fyn = 3
	public static void main(String args[]) throws Exception {
		Server ser = new Server();
		ser.parseArgs(args);
		ser.connect();
		ser.recieveData();
		
	}

	private void close() {
		// TODO Auto-generated method stub

	}

	private void recieveData() throws IOException {
		serverSocket.setSoTimeout(200);
		// recive packet and out it on the screen
		while (true) {
			while (true) {
				try {
					
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					serverSocket.receive(receivePacket);
					if (getPacketType() == 0) {
						if(getSqAckNum() == sequenceNumbers.peek()) {
							//toggle sequence numbers
							
							sequenceNumbers.push((sequenceNumbers.peek() == 0)? 1:0);
							//dump to screen
							System.out.println(new String(receiveData, 1, sendData.length -1));
							//ask for next packet
							sendData = new byte[1024];
							sendData[0] |= (1 << 0);
							sendData[0] |= (sequenceNumbers.peek()<<2);
							DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
							serverSocket.send(sendPacket);
							break;
						}
					} else if (getPacketType() == 3) {
						// end connection
						replyToFyn();
						return;
					}
				} catch (SocketTimeoutException e) {
					System.out.println("timer ran out");
				}
			}

		}
	}

	private void connect() throws IOException {
		System.out.println("Waiting for someone to connect..");
		while (true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			System.out.println("someone is trying to connect......");
			if (getPacketType() == 2) {
				IPAddress = receivePacket.getAddress();
				port = receivePacket.getPort();
				replyToSyn();
				System.out.println("connected.");
				break;
			}
		}

	}

	public Server() throws SocketException {
		serverSocket = new DatagramSocket(7443);
		sendData = new byte[1024];
		receiveData = new byte[1024];
		sequenceNumbers = new Stack<>();
	}

	public int getSqAckNum() {
		// shift our bits to the bit corresponding with the sequence number(3rd and 4th
		// bit)
		int num = receiveData[0] >> 2;
		return num & 3;
	}

	public void replyToSyn() throws IOException {
		// send an ack
		sendData[0] |= (1 << 0);
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
		serverSocket.send(sendPacket);
		sequenceNumbers.push(0);
	}

	public void replyToFyn() throws IOException {
		// send an ack
		sendData[0] |= (1 << 0);
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
		serverSocket.send(sendPacket);
		serverSocket.close();
	}

	public void replyToData() {

	}

	public int getPacketType() {
		return receiveData[0] & 3;
	}

	public void parseArgs(String args[]) {
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