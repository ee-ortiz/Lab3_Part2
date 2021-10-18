package vn.viettuts.client;

import java.util.Scanner;

import java.util.logging.Logger;

import java.util.logging.FileHandler;

public class ClientManager {
	public static FileHandler handler;
	public static Logger logger;
    
    public static void main(String[] args) {
        try {
        	handler = new FileHandler("C:\\ArchivosRecibidos\\default.log", true);
    		logger = Logger.getLogger(ClientManager.class.getName());
    		logger.addHandler(handler);
        	
        	Scanner myObj = new Scanner(System.in);  // Create a Scanner object
        	
        	System.out.println("Entre que tipo de archivo desea descargar (1 para 100MB, 2 para 250MB): ");

            int opcion1 = Integer.parseInt(myObj.nextLine());  // Read user input
            
            System.out.println("Ingrese por favor el número de conexiones concurrentes que desea: ");
            
            int opcion2 = Integer.parseInt(myObj.nextLine());  // Read user input
        	
            for(int i = 1; i<opcion2+1; i++) {
            	UDPClient client = new UDPClient(i, opcion2, opcion1);
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