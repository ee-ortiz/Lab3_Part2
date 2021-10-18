package vn.viettuts.server;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import java.util.logging.Logger;
import java.util.logging.FileHandler;

import vn.viettuts.common.FileInfo;

public class UDPServer extends Thread{
	
	private final int PIECES_OF_FILE_SIZE = 1024 * 32;
    private DatagramSocket serverSocket;
    private int port;
    
    private FileHandler handler;
    private Logger logger;
    boolean append = true;
	
	public UDPServer() throws Exception{
		port = 6677;
		
		append = true;
		
		handler = new FileHandler("Z:\\udpfile\\default.log", append);
		logger = Logger.getLogger(UDPServer.class.getName());
		logger.addHandler(handler);
	}
	
    /**
     * open server
     * 
     * @author viettuts.vn
     */
    private void openServer() {
        try {
            serverSocket = new DatagramSocket(port);
            logger.info("El servidor esta abierto en el puerto: " + port);
            listening();
        } catch (SocketException e) {
            e.printStackTrace();
            logger.warning("No se pudo conectar al puerto: " + port);
        }
    }

    /**
     * listening to clients handle events
     * 
     * @author viettuts.vn
     */
    private void listening() {
        while (true) {
            receiveFile();
        }
    }

    /**
     * receive file from clients
     * 
     * @author viettuts.vn
     */
    public void receiveFile() {
        byte[] receiveData = new byte[PIECES_OF_FILE_SIZE];
        DatagramPacket receivePacket;
        
        try {
            // get file info
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            InetAddress inetAddress = receivePacket.getAddress();
            ByteArrayInputStream bais = new ByteArrayInputStream(receivePacket.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);
            FileInfo fileInfo = (FileInfo) ois.readObject();
            // show file info
            if (fileInfo != null) {
            	String file_name = fileInfo.getFilename();
            	Long file_size = fileInfo.getFileSize();
            	int pieces_of_file = fileInfo.getPiecesOfFile();
            	int last_byte_length = fileInfo.getLastByteLength();
            	
            	String info_log = "";
            	
            	info_log += "Cliente " + file_name.split("-")[0] + " conectado:";
            	info_log += "{'File size':" + file_size + ",";
            	info_log += "'Pieces of file':" + pieces_of_file + ",";
            	info_log += "'Last bytes length':" + last_byte_length;
            	info_log += "}";
            	
            	logger.info(info_log);
            }
            // get file content
            logger.info("Recibiendo Archivo...");
            File fileReceive = new File(fileInfo.getDestinationDirectory() 
                    + fileInfo.getFilename());
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(fileReceive));
            // write pieces of file
            for (int i = 0; i < (fileInfo.getPiecesOfFile()-1); i++) {
                receivePacket = new DatagramPacket(receiveData, receiveData.length, 
                        inetAddress, port);
                serverSocket.receive(receivePacket);
                bos.write(receiveData, 0, PIECES_OF_FILE_SIZE);
            }
            // write last bytes of file
            receivePacket = new DatagramPacket(receiveData, receiveData.length, 
                    inetAddress, port);
            serverSocket.receive(receivePacket);
            bos.write(receiveData, 0, PIECES_OF_FILE_SIZE);
            bos.flush();
            logger.info("Archivo Enviado!");

            // close stream
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * run program
     * 
     * @author viettuts.vn
     * @param args
     */
    public static void main(String[] args) throws Exception{
    	try {
    		UDPServer udpServer = new UDPServer();
            udpServer.openServer();
    	} catch (Exception e) {
            e.printStackTrace();
    	}        
    }
}