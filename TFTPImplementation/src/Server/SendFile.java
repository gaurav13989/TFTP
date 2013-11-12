package Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * This class sends a file from server to client in chunks
 * 
 * @author Gaurav Komera
 * 
 */
public class SendFile implements Runnable {

	private Thread thread = new Thread(this);
	private File file;
	private InetAddress host;
	private int port;
	private DatagramSocket socketForSending;

	/**
	 * 
	 * @param file
	 * @param host
	 * @param port
	 * @param socketForSending
	 */
	public SendFile(File file, InetAddress host, int port,
			DatagramSocket socketForSending) {
		// TODO Auto-generated constructor stub

		this.file = file;
		this.host = host;
		this.port = port;
		this.socketForSending = socketForSending;
		System.out.println("File name> " + file);
		System.out.println("Host name> " + host);
		System.out.println("Port number> " + port);
		thread.start();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			// DatagramSocket socketForSending = new DatagramSocket();
			System.out.println("File transfer started... Filename: "
					+ file.getName() + " File Size: " + file.length());

			// start sending file
			byte[] buf = new byte[512];
			DatagramPacket packetToBeSent = new DatagramPacket(buf, buf.length);
			DatagramPacket packetReceived = new DatagramPacket(buf, buf.length);
			socketForSending.receive(packetReceived);
			System.out.println("Handshake begun... received>"
					+ new String(packetReceived.getData()));

			socketForSending.send(new DatagramPacket("Hi".getBytes(), "Hi"
					.getBytes().length, packetReceived.getAddress(),
					packetReceived.getPort()));

			FileInputStream fis = new FileInputStream(file);
			byte[] fileAsByteArray = new byte[(int) file.length()];
			fis.read(fileAsByteArray);
			fis.close();
			int timeOut = 1500;
			int packetNo = 1;
			int retryAttemptNo = 1;
			boolean lastPacket = false;
			/*
			 * Data Packet 2 bytes 4 bytes 506 bytes
			 * --------------------------------- DATA | 04 | Block # | Data |
			 * ---------------------------------
			 */
			// max file size not limited to 65535
			while (true && !lastPacket) {
				// disconnecting after three failed
				// attempts
				if (retryAttemptNo > 3) {
					System.out
							.println("File transfer aborted after 3 retry attempts.");
					return;
				}
				// code
				buf[0] = 0;
				buf[1] = 4;
				// block #
				buf[2] = (byte) (packetNo);
				buf[3] = (byte) (packetNo >>> 8);
				buf[4] = (byte) (packetNo >>> 16);
				buf[5] = (byte) (packetNo >>> 24);

				// maximum 506 bytes of data
				int packetSize = 6;
				int index2 = 0;
				for (index2 = (packetNo - 1) * 506; packetSize < 512
						&& index2 < (int) file.length(); packetSize++, index2++) {
					buf[packetSize] = fileAsByteArray[index2];
				}
				if (index2 == (int) file.length())
					lastPacket = true;

				packetToBeSent = new DatagramPacket(buf, packetSize, host, port);
				System.out.println("Sending packet to: " + host + "@" + port);
				try {
					socketForSending.send(packetToBeSent);
					socketForSending.setSoTimeout(timeOut);
					// checking for response as ack
					socketForSending.receive(packetReceived);
					packetNo = packetNo + 1;
					retryAttemptNo = 0;
				} catch (Exception e) {
					if (retryAttemptNo != 3)
						System.out.println("Request timed out. "
								+ "Trying again...");
					else
						System.out.println("Request timed out.");
					timeOut = retryAttemptNo * timeOut;
					retryAttemptNo++;
					continue;
				}
			}
			System.out.println("File transfer successful.");
			socketForSending.close();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			System.out
					.println("File transfer unsuccessful due to a connection error.");
			e.printStackTrace();
		} catch (FileNotFoundException f) {
			System.out
					.println("File transfer unsuccessful due to a file error.");
			f.printStackTrace();
		} catch (IOException i) {
			System.out.println("File transfer unsuccessful due to IO error.");
			i.printStackTrace();
		} catch (Exception me) {
			System.out.println("File transfer unsuccessful due to "
					+ me.getMessage());
		}
	}
}