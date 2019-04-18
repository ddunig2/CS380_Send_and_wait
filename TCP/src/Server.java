import java.io.*;
import java.net.*;

class Server {
	private DatagramSocket serverSocket;
	private InetAddress IPAddress;
	private byte[] sendData;
	private byte[] receiveData;
	private int nextInLine;
	private int timeOut;
	private int size;
	private int port;
	// data = 0
	// ack = 1
	// syn = 2
	// fyn = 3

	public static void main(String args[]) throws Exception {
		Server ser = new Server();
		ser.parseArgs(args);
		ser.connect();
		//ser.recieveData();
		//ser.close();

//		while (true) {
//			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//			serverSocket.receive(receivePacket);
//			// timer up here^^
//			String sentence = new String(receivePacket.getData());
//			System.out.println("RECEIVED: " + sentence);
//			String capitalizedSentence = sentence.toUpperCase();
//			sendData = capitalizedSentence.getBytes();
//			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
//			serverSocket.send(sendPacket);
//		}
	}

	private void close() {
		// TODO Auto-generated method stub
		
	}

	private void recieveData() throws IOException {
		// serverSocket.setSoTimeout(timeOut);
		while (true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
		}

	}

	private void connect() throws IOException {
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

	}

	public void replyToSyn() throws IOException {
		//send an ack
		sendData[0] |= (1<<0);
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
		serverSocket.send(sendPacket);
	}

	public void replyToFyn() {
		//send an ack
	}

	public void replyToData() {
		//send ack and sequence number
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