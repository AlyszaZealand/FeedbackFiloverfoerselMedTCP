package org.example;

public class Main {
    public static void main(String[] args) {
        int serverPort = 5000;
        String serverAddress = "localhost";
        String baseDirectory = "./test-files";
        
        new java.io.File(baseDirectory).mkdirs();
        
        FileServer server = new FileServer(serverPort, baseDirectory);
        
        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (java.io.IOException e) {
                System.err.println("Server error: " + e.getMessage());
            }
        });
        serverThread.start();
        
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            System.err.println("Main interrupted");
        }
        
        FileClient client = new FileClient(serverAddress, serverPort);
        
        try {
            client.connect("test.txt");
        } catch (java.io.IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}
