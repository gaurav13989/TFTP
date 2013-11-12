package Server;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class acts as the Server
 * @author gxk9544
 * 
 */
public class TFTPServer {
	
	/**
	 * Main method
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			DatagramSocket mainServerSocket = new DatagramSocket(49152);
			// HashMap<sender, chunks>
			HashMap<String, FileChunks> receiveBuffer = new HashMap<String, FileChunks>(); //

			System.out
					.println("**********************   Server Program   ****************************\n");
			System.out
					.println("\tTFTP server implementation by Gaurav Komera (gxk9544@rit.edu)");
			System.out
					.println("\t     can handle more than one client simultaneously");
			System.out.println("\t          includes timeouts and retransmissions\n");
			System.out
					.println("**********************************************************************");

			System.out.println("Server started on port "
					+ mainServerSocket.getLocalPort() + "...");
			byte[] buf = new byte[512];
			DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
			DatagramPacket responsePacket = new DatagramPacket(buf, buf.length);

			while (true) {
				// making the socket receive a packet
				mainServerSocket.receive(receivedPacket);
				buf = new byte[512];
				responsePacket = new DatagramPacket(buf, buf.length);
				// handle packets appropriately according to type code
				byte[] receivedBytes = Arrays
						.copyOfRange(receivedPacket.getData(), 0,
								receivedPacket.getLength());
				// retrieving type code
				String codeInString = receivedBytes[0] + receivedBytes[1] + "";
				int code = Integer.parseInt(codeInString);

				switch (code) {
				// connect
				case 1:
					String response = "OK";
					buf = response.getBytes();
					responsePacket = new DatagramPacket(buf, buf.length,
							receivedPacket.getAddress(),
							receivedPacket.getPort());
					mainServerSocket.send(responsePacket);
					System.out.println("Request from: "
							+ receivedPacket.getAddress() + "@"
							+ receivedPacket.getPort());
					break;
				// ? - help
				case 7:
					String helpResponse = "These are the available commands:\n\n"
							+ "\tconnect\t\t connect to a specified server at a specified port\n"
							+ "\t\t\t usage: connect <host name> <port number>\n\n"
							+ "\twrq\t\tsend a write request for a file\n"
							+ "\t\t\tusage: wrq <file path on local system>\n\n"
							+ "\trrq\t\tsend a read request for a file\n"
							+ "\t\t\tusage: rrq <name of file with extension>\n\n"
							+ "\tquit\t\tquit server session\n"
							+ "\t\t\tusage: quit\n\n"
							+ "\t?\t\tlists available commands\n"
							+ "\t\t\tusage: ?\n";
					buf = helpResponse.getBytes();
					responsePacket = new DatagramPacket(buf, buf.length,
							receivedPacket.getAddress(),
							receivedPacket.getPort());
					mainServerSocket.send(responsePacket);
					break;
				// wrq - write request
				case 3:
					String from = receivedPacket.getAddress() + "";
					if (receiveBuffer.containsKey(from))
						receiveBuffer.remove(from);

					byte[] fileName;
					byte[] fileSize;

					int index1 = 3,
					index2 = 0;
					while (receivedBytes[index1] != 0) {
						index1++;
					}
					fileName = Arrays.copyOfRange(receivedBytes, 3, index1);

					int tempStartIndex = index1++;
					index2 = 0;
					while (receivedBytes[index1] != 0) {
						index1++;
					}

					fileSize = Arrays.copyOfRange(receivedBytes,
							tempStartIndex, index1);
					receiveBuffer.put(
							from,
							new FileChunks(new String(fileName), Integer
									.parseInt(new String(fileSize).trim())));

					String wrqResponse = "OK";
					buf = new byte[512];
					buf = wrqResponse.getBytes();
					responsePacket = new DatagramPacket(buf, buf.length,
							receivedPacket.getAddress(),
							receivedPacket.getPort());

					mainServerSocket.send(responsePacket);
					// System.out.println("Receiving file from "+receivedPacket.getAddress()+"@"+receivedPacket.getPort());
					break;
				// data - receiving data
				case 4:
					int blockNo = ((receivedBytes[5] << 24) & 0xff000000)
							| ((receivedBytes[4] << 16) & 0x00ff0000)
							| ((receivedBytes[3] << 8) & 0x0000ff00)
							| (receivedBytes[2] & 0x000000ff);
					// System.out.println("from: "+receivedPacket.getAddress()+"block no: "+blockNo);
					byte[] data = Arrays.copyOfRange(receivedPacket.getData(),
							6, receivedPacket.getLength());

					// retrieve chunks from receive buffer and add to it
					receiveBuffer.get(receivedPacket.getAddress() + "")
							.getFileChunks()[blockNo - 1] = data;
					buf = new byte[512];
					buf = "OK".getBytes();
					responsePacket = new DatagramPacket(buf, buf.length,
							receivedPacket.getAddress(),
							receivedPacket.getPort());
					mainServerSocket.send(responsePacket);

					// last packet
					if (receivedPacket.getLength() < 512) {
						// spawn thread to create file from bytes in received
						// buffer
						CreateFile createFile = new CreateFile(
								receiveBuffer.get(receivedPacket.getAddress()
										+ ""));
						// System.out.println("end of file for "+receivedPacket.getAddress());
					}
					break;
				// rrq
				case 5:
					// System.out.println(1);
					String nameOfFileToBeSent;

					// System.out.println(2);
					index1 = 3;
					index2 = 0;
					while (receivedBytes[index1] != 0) {
						index1++;
					}
					nameOfFileToBeSent = new String(Arrays.copyOfRange(
							receivedBytes, 3, index1));

					File file = new File(nameOfFileToBeSent);
					if (!file.exists()) {
						String fileDoesNotExist = "FDNE";
						responsePacket = new DatagramPacket(
								fileDoesNotExist.getBytes(),
								fileDoesNotExist.getBytes().length,
								receivedPacket.getAddress(),
								receivedPacket.getPort());
						mainServerSocket.send(responsePacket);
						System.out.println("File \"" + nameOfFileToBeSent
								+ "\" does not exist.");
						break;
					}
					// System.out.println("size of file>"+file.length());
					// send OK followed by file name and file size
					DatagramSocket socketForSending = new DatagramSocket();
					int newPort = socketForSending.getLocalPort();
					String rrqResponse = "OK " + file.getName() + " "
							+ file.length() + " " + newPort;
					responsePacket = new DatagramPacket(rrqResponse.getBytes(),
							rrqResponse.getBytes().length,
							receivedPacket.getAddress(),
							receivedPacket.getPort());
					mainServerSocket.send(responsePacket);
					// spawn new thread and start sending packets to client
					sendFile(file, receivedPacket.getAddress(),
							receivedPacket.getPort(), socketForSending);

					break;
				default:
					break;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * This method creates a new Thread for a sending file to a client
	 * @param file - file to be sent
	 * @param host - host name of client
	 * @param port - port number of client
	 * @param socket - DatagramSocket used for sending file to client
	 */
	private static void sendFile(File file, InetAddress host, int port,
			DatagramSocket socket) {
		// TODO Auto-generated method stub
		SendFile sendFile = new SendFile(file, host, port, socket);
	}
}