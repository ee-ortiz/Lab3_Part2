package cliente;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import logger.Log;

import java.util.logging.FileHandler;

public class ClientManager {
	public static FileHandler handler;
    
    public static void main(String[] args) {
        try {
        	
        	Log logger;
        	// Formato de nombre de log.
    		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");  
    		LocalDateTime now = LocalDateTime.now();  
    		String formato = dtf.format(now)+"-log.txt";  
    		
    	    logger = new Log("D:\\ArchivosRecibidos\\"+formato);

        	Scanner myObj = new Scanner(System.in);  // Create a Scanner object
        	
        	System.out.println("Entre que tipo de archivo desea descargar (1 para 100MB, 2 para 250MB): ");

            int opcion1 = Integer.parseInt(myObj.nextLine());  // Read user input
            
            System.out.println("Ingrese por favor el número de conexiones concurrentes que desea: ");
            
            int opcion2 = Integer.parseInt(myObj.nextLine());  // Read user input
        	
            for(int i = 1; i<opcion2+1; i++) {
            	UDPClient client = new UDPClient(i, opcion2, opcion1, logger);
                client.start();
                try {
    				client.join();
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
            }
            
            myObj.close();
        } catch(Exception e) {
        	e.getMessage();       	
        }
    }
}