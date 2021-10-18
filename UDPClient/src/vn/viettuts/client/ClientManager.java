package vn.viettuts.client;

import java.util.Scanner;

public class ClientManager {
    
    public static void main(String[] args) {
        
    	Scanner myObj = new Scanner(System.in);  // Create a Scanner object
    	
    	System.out.println("Entre que tipo de archivo desea descargar (1 para 100MB, 2 para 250MB): ");

        int opcion1 = Integer.parseInt(myObj.nextLine());  // Read user input
        
        System.out.println("Ingrese por favor el número de conexiones concurrentes que desea: ");
        
        int opcion2 = Integer.parseInt(myObj.nextLine());  // Read user input
    	
        for(int i = 1; i<opcion2+1; i++) {
        	System.out.println("Client "+ i + " connection to server stablished, ready to start file transfer");
        }
        
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
    }

}