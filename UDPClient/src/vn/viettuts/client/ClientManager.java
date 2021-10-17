package vn.viettuts.client;

public class ClientManager {
    
    public static void main(String[] args) {
        for(int i = 1; i<2; i++) {
            UDPClient client = new UDPClient(i);
            client.start();
        }
    }

}