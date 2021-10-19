package cliente;

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

import logger.Log;
import vn.viettuts.common.FileInfo;

public class UDPClient extends Thread{
	private static final int PIECES_OF_FILE_SIZE = 1024 * 32;
	private DatagramSocket clientSocket;
	private int serverPort = 6677;
	private String serverHost = "localhost";
	private int threadNumber;
	private int totalConexions;
	private int tipoArchivo;
	private Log logger;

	public UDPClient(int num, int conex, int tipoArch, Log pLogger) {
		threadNumber = num;
		totalConexions = conex;
		tipoArchivo = tipoArch;
		logger = pLogger;
	}

	/**
	 * run program
	 * 
	 * @param args
	 */
	public void run() {
		
		String sourcePath = "";
		if(tipoArchivo == 1) {
			sourcePath = "D:\\Desktop\\UDP-Files\\1MB.txt";
		}
		if(tipoArchivo == 2) {
			sourcePath = "D:\\Desktop\\UDP-Files\\250MB.txt";
		}
		String destinationDir = "D:\\Desktop\\ArchivosRecibidos\\";
		connectServer();
		sendFile(sourcePath, destinationDir);
	}

	/**
	 * connect server
	 * 
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
			synchronized (logger) {
	            Long file_size = fileInfo.getFileSize();
	            int pieces_of_file = fileInfo.getPiecesOfFile();
	            int last_byte_length = fileInfo.getLastByteLength();
	            	
	            String info_log = "";
	            	
            	info_log += "Cliente " + fileName.split("-")[0] + " conectado:";
            	info_log += "{'File name':" + fileName + ",";
            	info_log += "'File size':" + file_size + ",";
            	info_log += "'Pieces of file':" + pieces_of_file + ",";
            	info_log += "'Last bytes length':" + last_byte_length;
            	info_log += "}";
	            	
	            logger.logger.info(info_log);
			}
			
            final long startTime = System.currentTimeMillis();
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
			
            final long endTime = System.currentTimeMillis();

            synchronized (logger) {
                logger.logger.info("El tiempo total que tardó en ser recibido el archivo "+fileInfo.getFilename()+ " es de "+ (endTime - startTime) + "ms");
                logger.logger.info("Archivo recibido!");
			}

			// close stream
			bis.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
            synchronized (logger) {
            	logger.logger.info("Ocurrió un error inesperado al recibir el archivo");
			}
		}
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