package vn.viettuts.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import vn.viettuts.common.FileInfo;

public class UDPClient extends Thread{
	private static final int PIECES_OF_FILE_SIZE = 1024 * 32;
	private DatagramSocket clientSocket;
	private int serverPort = 6677;
	private String serverHost = "localhost";
	private int threadNumber;
	private int totalConexions;
	private int tipoArchivo;

	public UDPClient(int num, int conex, int tipoArch) {
		threadNumber = num;
		totalConexions = conex;
		tipoArchivo = tipoArch;
	}

	/**
	 * run program
	 * 
	 * @author viettuts.vn
	 * @param args
	 */
	public void run() {
		
		String sourcePath = "";
		if(tipoArchivo == 1) {
			sourcePath = "Z:\\udpfile\\100MB.txt";
		}
		if(tipoArchivo == 2) {
			sourcePath = "Z:\\udpfile\\250MB.txt";
		}
		String destinationDir = "C:\\ArchivosRecibidos\\";
		connectServer();
		sendFile(sourcePath, destinationDir);
	}

	/**
	 * connect server
	 * 
	 * @author viettuts.vn
	 */
	private void connectServer() {
		try {
			clientSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	/**
	 * send file to server
	 *
	 * @author viettuts.vn
	 * @param sourcePath
	 * @param destinationDir 
	 */
	private void sendFile(String sourcePath, String destinationDir) {
		InetAddress inetAddress;
		DatagramPacket sendPacket;

		try {
			File fileSend = new File(sourcePath);
			InputStream inputStream = new FileInputStream(fileSend);
			BufferedInputStream bis = new BufferedInputStream(inputStream);
			inetAddress = InetAddress.getByName(serverHost);
			byte[] bytePart = new byte[PIECES_OF_FILE_SIZE];

			// get file size
			long fileLength = fileSend.length();
			int piecesOfFile = (int) (fileLength / PIECES_OF_FILE_SIZE);
			int lastByteLength = (int) (fileLength % PIECES_OF_FILE_SIZE);

			// check last bytes of file
			if (lastByteLength > 0) {
				piecesOfFile++;
			}

			// split file into pieces and assign to fileBytess
			byte[][] fileBytess = new byte[piecesOfFile][PIECES_OF_FILE_SIZE];
			int count = 0;
			while (bis.read(bytePart, 0, PIECES_OF_FILE_SIZE) > 0) {
				fileBytess[count++] = bytePart;
				bytePart = new byte[PIECES_OF_FILE_SIZE];
			}

			// read file info
			FileInfo fileInfo = new FileInfo();
			String[] filenameArray = fileSend.getName().split("\\.");
			String fileName =  threadNumber + "-Prueba-" + totalConexions+ "." + filenameArray[1];
			
			fileInfo.setFilename(fileName);
			fileInfo.setFileSize(fileSend.length());
			fileInfo.setPiecesOfFile(piecesOfFile);
			fileInfo.setLastByteLength(lastByteLength);
			fileInfo.setDestinationDirectory(destinationDir);

			// send file info
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(fileInfo);
			sendPacket = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length,
					inetAddress, serverPort);
			clientSocket.send(sendPacket);

			// send file content
			System.out.println("Sending file...");
			// send pieces of file
			for (int i = 0; i < (count - 1); i++) {
				sendPacket = new DatagramPacket(fileBytess[i], PIECES_OF_FILE_SIZE,
						inetAddress, serverPort);
				clientSocket.send(sendPacket);
				waitMillisecond(40);
			}
			// send last bytes of file
			sendPacket = new DatagramPacket(fileBytess[count - 1], PIECES_OF_FILE_SIZE,
					inetAddress, serverPort);
			clientSocket.send(sendPacket);
			waitMillisecond(40);

			// close stream
			bis.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Sent.");
		System.out.println("Client " + threadNumber + " connection to server closed");
	}

	/**
	 * sleep program in millisecond
	 * 
	 * @param millisecond
	 */
	public void waitMillisecond(long millisecond) {
		try {
			Thread.sleep(millisecond);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}