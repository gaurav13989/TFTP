package Server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * This class implements the Runnable interface and creates a file
 * using an object of FileChunks
 * @author Gaurav
 *
 */
public class CreateFile implements Runnable {

	private Thread createFileThread;
	private FileChunks fileChunks;
	private int lastChunkLength;

	/**
	 * Parametrized Constructor
	 * @param fileChunks - consists of chunks of the file in many byte[]
	 */
	public CreateFile(FileChunks fileChunks) {
		this.createFileThread = new Thread(this);
		this.fileChunks = fileChunks;
		createFileThread.start();
	}

	/**
	 * This method is executed when start() is called in the constructor
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			// System.out.println("File creation started... Filename: "+fileChunks.getFileName());

			String fileName = fileChunks.getFileName();
			int fileSize = fileChunks.getFileSize();
			byte[][] chunks = fileChunks.getFileChunks();

			byte[] fileBytesTemp = new byte[chunks.length * chunks[0].length];
			int fileBytesIndex = 0;
			for (int index1 = 0; index1 < chunks.length; index1++) {
				for (int index2 = 0; index2 < chunks[index1].length; index2++) {
					fileBytesTemp[fileBytesIndex++] = chunks[index1][index2];
				}
			}
			byte[] fileBytes = Arrays.copyOfRange(fileBytesTemp, 0,
					fileBytesIndex);
			FileOutputStream fileOuputStream = new FileOutputStream(fileName);
			fileOuputStream.write(fileBytes);
			fileOuputStream.close();
			System.out.println("File creation successful... Filename: "
					+ fileChunks.getFileName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("File creation aborted due to: ");
			e.printStackTrace();
		}
	}
}