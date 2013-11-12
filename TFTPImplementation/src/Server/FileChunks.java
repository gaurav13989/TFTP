package Server;

/**
 * This class stores the file in chunks
 * @author Gaurav
 *
 */
public class FileChunks {
	private String fileName;
	private int fileSize;
	private byte[][] fileChunks;

	/**
	 * Parameterized constructor initializes the object with given
	 * file name and file size
	 * @param fileName
	 * @param fileSize
	 */
	public FileChunks(String fileName, int fileSize) {
		int chunkSize = 506;
		int numberOfChunks = fileSize / chunkSize;
		if (fileSize % chunkSize > 0)
			numberOfChunks++;
		fileChunks = new byte[numberOfChunks][chunkSize];
		this.fileName = fileName;
	}
	
	/**
	 * getter method for fileChunks
	 * @return
	 */
	public byte[][] getFileChunks() {
		return fileChunks;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the fileSize
	 */
	public int getFileSize() {
		return fileSize;
	}

	/**
	 * @param fileSize
	 *            the fileSize to set
	 */
	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}
}