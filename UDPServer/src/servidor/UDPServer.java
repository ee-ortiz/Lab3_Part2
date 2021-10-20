package servidor;
import logger.Log;

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
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime; 

import vn.viettuts.common.FileInfo;

public class UDPServer extends Thread{
	
	private final int PIECES_OF_FILE_SIZE = 1024 * 32;
    private DatagramSocket serverSocket;
    private int port;
    Log logger;
    
	public UDPServer() throws Exception{
		port = 6677;
		
        // Formato de nombre de log.
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");  
		LocalDateTime now = LocalDateTime.now();  
		String formato = dtf.format(now)+"-log.txt";  
		
	    logger = new Log("/home/infracom/archivos/UDP-Files/"+formato);
	}
	
    /**
     * open server
     * 
     */
    private void openServer() {
        try {
            serverSocket = new DatagramSocket(port);
            System.out.println("El servidor esta abierto en el puerto: " + port);
            listening();
        } catch (SocketException e) {
            e.printStackTrace();
            System.out.println("No se pudo conectar al puerto: " + port);
        }
    }

    /**
     * listening to clients handle events
     * 
     */
    private void listening() {
        while (true) {
            receiveFile();
        }
    }

    /**
     * receive file from clients
     * 
     */
    public void receiveFile() {
        byte[] receiveData = new byte[PIECES_OF_FILE_SIZE];
        DatagramPacket receivePacket;
        
        try {
            // Informacion del archivo.
        	receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            InetAddress inetAddress = receivePacket.getAddress();
            ByteArrayInputStream bais = new ByteArrayInputStream(receivePacket.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);
            FileInfo fileInfo = (FileInfo) ois.readObject();
            
            if (fileInfo != null) {
            	String file_name = fileInfo.getFilename();
            	Long file_size = fileInfo.getFileSize();
            	int pieces_of_file = fileInfo.getPiecesOfFile();
            	int last_byte_length = fileInfo.getLastByteLength();
            	
            	String info_log = "";
            	
            	info_log += "Cliente " + file_name.split("-")[0] + " conectado:";
            	info_log += "{'File name':" + file_name + ",";
            	info_log += "'File size':" + file_size + ",";
            	info_log += "'Pieces of file':" + pieces_of_file + ",";
            	info_log += "'Last bytes length':" + last_byte_length;
            	info_log += "}";
            	
            	logger.logger.info(info_log);
            }
            // Recibiendo contenido del archivo
            logger.logger.info("Recibiendo Archivo...");
            
        	
            File fileReceive = new File(fileInfo.getDestinationDirectory() 
                    + fileInfo.getFilename());
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(fileReceive));
            
            final long startTime = System.currentTimeMillis();
            // Primer paquete de envio
            for (int i = 0; i < (fileInfo.getPiecesOfFile()-1); i++) {
                receivePacket = new DatagramPacket(receiveData, receiveData.length, 
                        inetAddress, port);
                serverSocket.receive(receivePacket);
                bos.write(receiveData, 0, PIECES_OF_FILE_SIZE);
            }
            // Último paquete de envio
            receivePacket = new DatagramPacket(receiveData, receiveData.length, 
                    inetAddress, port);
            serverSocket.receive(receivePacket);
            bos.write(receiveData, 0, PIECES_OF_FILE_SIZE);
            bos.flush();
            final long endTime = System.currentTimeMillis();

            logger.logger.info("El tiempo total que tardo en ser enviado el archivo: "+fileInfo.getFilename()+ " es de "+ (endTime - startTime) + "ms");
            logger.logger.info("Archivo Enviado!");

            // Cerrando stream
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.logger.info("Ocurrio un error inesperado enviando un archivo");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * run program
     * 
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
