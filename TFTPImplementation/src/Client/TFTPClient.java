package Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * This class acts as the Client
 * 
 * @author Gaurav Komera
 * 
 */
public class TFTPClient {
	static String root = "tftp> ";
	static InetAddress host;
	static int port = 0;
	static boolean connected;

	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			FileChunks receivedChunks;
			String userInput = "";
			String serverResponse = "";
			DatagramSocket datagramClientSocket = new DatagramSocket();
			byte[] buf = new byte[512];
			// Add host name to get InetAddress
			DatagramPacket packetToBeSent = new DatagramPacket(buf, buf.length);
			DatagramPacket packetReceived = new DatagramPacket(buf, buf.length);
			System.out
					.println("***********************  Client Program  *****************************\n");
			System.out
					.println("\tTFTP client implementation by Gaurav Komera (gxk9544@rit.edu)");
			System.out.println("\tincludes timeouts and retransmissions");
			System.out.println("\tClient Program\n");
			System.out.println("\tSupports following functionalities");
			System.out.println("\t* connect");
			System.out
					.println("\t\tconnects to a specified server at a spcified port");
			System.out.println("\t\tusage: connect <host name> <port number>");
			System.out.println("\t* wrq");
			System.out
					.println("\t\tallows to write a file to the specified server");
			System.out.println("\t\tusage: wrq <file path on local system>");
			System.out.println("\t* rrq");
			System.out
					.println("\t\tallows to read a file from the specified server");
			System.out.println("\t\tusage: rrq <name of file with extension>");
			System.out.println("\t* quit");
			System.out
					.println("\t\tquits the current session with the specified ");
			System.out.println("\t\tserver or quits client if not connected");
			System.out.println("\t\tusage: quit");
			System.out.println("\t* ?");
			System.out.println("\t\tlists the available commands");
			System.out.println("\t\tusage: ?\n");
			System.out
					.println("**********************************************************************");
			while (true) {
				System.out.print(root);
				buf = new byte[512];
				packetReceived = new DatagramPacket(buf, buf.length);
				packetToBeSent = new DatagramPacket(buf, buf.length);
				userInput = br.readLine();
				userInput = userInput.toLowerCase();
				serverResponse = "";
				boolean inputIsFine = checkUserInput(userInput);
				if (inputIsFine) {
					// connect - to a server by specifying host name and port
					// format - connect <host-name> <port>
					if (userInput.startsWith("connect")) {
						if (connected) {
							System.out.println("Disconnected from " + host);

							connected = false;
							root = "tftp> ";
							host = null;
							port = 0;
						}
						String[] connectDetails = userInput.split(" ");
						buf = new byte[] { 0, 1 };
						host = InetAddress.getByName(connectDetails[1]);
						port = Integer.parseInt(connectDetails[2]);
						System.out.println("Connecting to " + host + "@" + port
								+ "...");

						packetToBeSent = new DatagramPacket(buf, buf.length,
								host, port);
						try {
							datagramClientSocket.send(packetToBeSent);
							datagramClientSocket.setSoTimeout(3000);
							datagramClientSocket.receive(packetReceived);
						} catch (Exception e) {
							System.out.println("Request timed out. Kindly "
									+ "check host name and port.");
							continue;
						}

						serverResponse = new String(packetReceived.getData());

						if (serverResponse.trim().equals("OK")) {
							root = "tftp@" + host + "> ";
							connected = true;
						} else {
							System.out
									.println("Unexpected response from server"
											+ ". Response: "
											+ serverResponse.trim());
						}
					}
					// wrq - request to write a file
					// format - wrq <path of file to be sent>
					else if (userInput.startsWith("wrq")) {
						String[] writeReqDetails = userInput.split(" ");
						String fileName = writeReqDetails[1];
						boolean lastPacket = false;
						File file = null;
						String fileSize = "";
						try {
							file = new File(fileName);
							fileSize = file.length() + "";
							if (!file.exists()) {
								System.out
										.println("File not found. Please "
												+ "check file name.");
								continue;
							}
						} catch (Exception e) {
							System.out
									.println("File not found. Please "
											+ "check file name.");
							continue;
						}
						if (fileSize.equals("0L")) {
							System.out
									.println("File not found. Please "
											+ "check file name.");
							continue;
						}
						byte[] code = new byte[] { 0, 3 };
						byte[] fileNameByteArray = file.getName().getBytes();
						byte[] fileSizeByteArray = fileSize.getBytes();

						// preparing buf array for sending
						// 1. code
						// 2. file name
						// 3. file size
						buf = new byte[512];
						int index1 = 0;
						for (; index1 < code.length; index1++) {
							buf[index1] = code[index1];
						}
						buf[index1] = 0;
						index1++;
						for (int index2 = 0; index2 < fileNameByteArray.length;
								index2++, index1++) {
							buf[index1] = fileNameByteArray[index2];
						}
						buf[index1] = 0;
						index1++;
						for (int index2 = 0; index2 < fileSizeByteArray.length;
								index2++, index1++) {
							buf[index1] = fileSizeByteArray[index2];
						}
						buf[index1] = 0;

						try {
							// send write request with file name and file size
							packetToBeSent = new DatagramPacket(buf,
									index1 + 1, host, port);
							buf = new byte[512];
							packetReceived = new DatagramPacket(buf, buf.length);
							datagramClientSocket.send(packetToBeSent);
							datagramClientSocket.setSoTimeout(1500);
							// receive response
							datagramClientSocket.receive(packetReceived);
						} catch (Exception e) {
							System.out
									.println("Could not contact server. Please try again.");
							continue;
						}

						serverResponse = new String(packetReceived.getData());
						if (serverResponse.trim().equals("OK")) {
							// start sending file
							FileInputStream fis = new FileInputStream(file);
							byte[] fileAsByteArray = new byte[(int) file
									.length()];
							fis.read(fileAsByteArray);
							fis.close();
							int timeOut = 1500;
							int packetNo = 1;
							int retryAttemptNo = 1;
							/**
							 * Data Packet 2 bytes 4 bytes 506 bytes
							 * --------------------------------- DATA | 04 |
							 * Block # | Data |
							 * ---------------------------------
							 */
							System.out.print("Sending...");
							while (true && !lastPacket) {
								System.out.print(".");
								// disconnecting after three failed
								// attempts
								if (retryAttemptNo > 3) {
									System.out.println("Retry attempts failed."
											+ " Seems like the server is down."
											+ " Disconnecting...");
									disconnect();
									break;
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
								for (index2 = (packetNo - 1) * 506; 
										packetSize < 512 && index2 < (int) file.length(); 
										packetSize++, index2++) {
									buf[packetSize] = fileAsByteArray[index2];
								}
								if (index2 == (int) file.length()) {
									lastPacket = true;
									System.out.println();
								}
								packetToBeSent = new DatagramPacket(buf,
										packetSize, host, port);
								try {
									datagramClientSocket.send(packetToBeSent);
									datagramClientSocket.setSoTimeout(timeOut);
									// checking for response
									datagramClientSocket
											.receive(packetReceived);
									if (lastPacket)
										System.out
												.println("File sent "
														+ "successfully.");
									packetNo = packetNo + 1;
									retryAttemptNo = 0;
								} catch (Exception e) {
									System.out.println(" Request timed out. "
											+ "Trying again...");
									System.out.print("Sending...");
									timeOut = retryAttemptNo * timeOut;
									retryAttemptNo++;
									continue;
								}
							}
						} else {
							System.out
									.println("The server could not accept "
											+ "the file at this time. "
											+ "Please try again "
											+ "later.");
						}
					}
					// rrq - request to read a file from the remote server
					else if (userInput.startsWith("rrq")) {
						String[] readReqDetails = userInput.split(" ");
						String fileName = readReqDetails[1];

						byte[] code = new byte[] { 0, 5 };
						byte[] fileNameByteArray = fileName.getBytes();

						// preparing buf array for sending
						// 1. code
						int index1 = 0;
						for (; index1 < code.length; index1++) {
							buf[index1] = code[index1];
						}
						buf[index1] = 0;
						// 2. file name
						index1++;
						for (int index2 = 0; index2 < fileNameByteArray.length; 
								index2++, index1++) {
							buf[index1] = fileNameByteArray[index2];
						}
						buf[index1] = 0;

						try {
							// send read request with file name
							packetToBeSent = new DatagramPacket(buf,
									index1 + 1, host, port);
							datagramClientSocket.send(packetToBeSent);
							datagramClientSocket.setSoTimeout(1500);
							// receive response
							buf = new byte[512];
							packetReceived = new DatagramPacket(buf, buf.length);
							datagramClientSocket.receive(packetReceived);
						} catch (Exception e) {
							e.printStackTrace();
							System.out
									.println("Could not contact server. Please"
											+ " try again.");
							continue;
						}
						serverResponse = new String(packetReceived.getData());
						String[] serverResponseArr = serverResponse.split(" ");
						serverResponse = serverResponseArr[0].trim();
						if (serverResponse.equals("FDNE")) {
							System.out
									.println("File with name \""
											+ fileName
											+ "\" does not exist on the server"
											+ ". Please "
											+ "check file name and retry.");
							continue;
						}
						if (serverResponseArr.length != 4) {
							System.out
									.println("Unknown error. Please try again.");
							continue;
						}

						fileName = serverResponseArr[1].trim();
						int fileSize = Integer.parseInt(serverResponseArr[2]
								.trim());
						receivedChunks = new FileChunks(fileName, fileSize);

						datagramClientSocket.send(new DatagramPacket("Hi"
								.getBytes(), "Hi".getBytes().length, host,
								Integer.parseInt(serverResponseArr[3].trim())));
						datagramClientSocket.receive(packetReceived);
//						System.out.println("Handshake complete...");
						System.out.print("Receiving...");
						int retryAttemptNo = 1;
						while (true) {
							System.out.print(".");
							if (retryAttemptNo > 3) {
								System.out
										.println("\nFile transfer aborted "
												+ "after 3 retry attempts.");
								return;
							}
							buf = new byte[512];

							packetReceived = new DatagramPacket(buf, 
									buf.length);
							int timeout = 1500;
							boolean timeoutBool = false;
							do {
								try {
									datagramClientSocket.setSoTimeout(timeout);
									datagramClientSocket
											.receive(packetReceived);// receive(packetReceived);
									timeoutBool = false;
									retryAttemptNo = 1;
								} catch (Exception e) {
									timeoutBool = true;
									timeout = timeout * retryAttemptNo++;
								}
							} while (timeout < 6001 && timeoutBool);
							if (timeoutBool) {
								System.out
										.println("\nRequest timed out. File could"
												+ " not be received. "
												+ "Try again.");
								break;
							}
							byte[] receivedBytes = Arrays.copyOfRange(
									packetReceived.getData(), 0,
									packetReceived.getLength());
							int blockNo = ((receivedBytes[5] << 24) & 0xff000000)
									| ((receivedBytes[4] << 16) & 0x00ff0000)
									| ((receivedBytes[3] << 8) & 0x0000ff00)
									| (receivedBytes[2] & 0x000000ff);
							byte[] data = Arrays.copyOfRange(
									packetReceived.getData(), 6,
									packetReceived.getLength());

							// retrieve chunks from receive buffer and add to it
							receivedChunks.getFileChunks()[blockNo - 1] = data;

							buf = "OK".getBytes();
							packetToBeSent = new DatagramPacket(buf,
									buf.length, packetReceived.getAddress(),
									packetReceived.getPort());
							datagramClientSocket.send(packetToBeSent);

							// last packet
							if (packetReceived.getLength() < 512) {
								// spawn thread to create file from bytes in
								// received buffer
								CreateFile createFile = new CreateFile(
										receivedChunks);
								System.out.println();
								break;
							}
						}
					}
					// quit - quit without connect quits the client problem
					// - quit after a connect quits the connection
					else if (userInput.equals("quit")) {
						if (connected) {
							System.out.println("Disconnected from " + host);
							disconnect();
							continue;
						} else {
							System.out.println("Thank you.");
							break;
						}
					}
					// ? - help - displays commands to the client
					else if (userInput.equals("?")) {
						if (connected) {
							buf = new byte[] { 0, 7 };
							packetToBeSent = new DatagramPacket(buf,
									buf.length, host, port);
							datagramClientSocket.send(packetToBeSent);
							datagramClientSocket.setSoTimeout(1000);
							datagramClientSocket.receive(packetReceived);

							serverResponse = new String(
									packetReceived.getData());

							System.out.println(serverResponse);
						} else {
							String helpResponse = "These are the available com"
									+ "mands:\n\n"
									+ "\tconnect\t\t connect to a specified se"
									+ "rver at a specified port\n"
									+ "\t\t\t usage: connect <host name> <port"
									+ " number>\n\n"
									+ "\twrq\t\tsend a write request for a fil"
									+ "e\n"
									+ "\t\t\tusage: wrq <file path on local sy"
									+ "stem>\n\n"
									+ "\trrq\t\tsend a read request for a file"
									+ "\n"
									+ "\t\t\tusage: rrq <name of file with ext"
									+ "ension>\n\n"
									+ "\tquit\t\tquit server session\n"
									+ "\t\t\tusage: quit\n\n"
									+ "\t?\t\tlists available commands\n"
									+ "\t\t\tusage: ?\n";
							System.out.println(helpResponse);
						}
					}
				} else
					continue;
			}

			datagramClientSocket.close();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * This method disconnects from the specified server
	 */
	public static void disconnect() {
		connected = false;
		root = "tftp> ";
		host = null;
		port = 0;
	}

	/**
	 * This method checks for validity of the user input
	 * 
	 * @param userInput
	 * @return boolean - true if user input is valid false if user input is
	 *         invalid
	 */
	public static boolean checkUserInput(String userInput) {
		boolean retVal = true;
		if (userInput.equals(""))
			retVal = true;
		else if (!(userInput.matches("^connect\\s([\\w\\-\\.])+\\s\\d{1,5}$")
				|| userInput.matches("^rrq\\s([\\w\\-\\.\\:\\\\])+$")
				|| userInput.matches("^wrq\\s([\\w\\-\\.\\:\\\\])+$")
				|| userInput.matches("^\\?$") || userInput.matches("^quit$"))) {
			System.out.println("Invalid command \"" + userInput + "\"");
			System.out.println("For a list of commands use \"?\"");
			retVal = false;
		} else if (!connected
				&& (userInput.matches("^rrq\\s([\\w\\-\\.])+$") || userInput
						.matches("^wrq\\s([\\w\\-\\.])+$"))) {
			System.out
					.println("You need to specify a server before reading or"
							+ " writing. For a list of commands use \"?\"");
			retVal = false;
		}
		return retVal;
	}
}