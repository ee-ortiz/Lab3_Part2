package vn.viettuts.client;

public class ClientManager {
    
    public static void main(String[] args) {
        for(int i = 1; i<3; i++) {
            UDPClient client = new UDPClient(i);
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