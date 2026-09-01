package org.example;

public class Main {
    public static void main(String[] args) {
        int serverPort = 5000;
        String serverAddress = "localhost";
        String baseDirectory = "./test-files";
        String remoteFilename = "test.txt";
        String localFilePath = "downloaded-test.txt";
        
        java.io.File baseDir = new java.io.File(baseDirectory);
        baseDir.mkdirs();
        
        createTestFile(baseDirectory, remoteFilename, "Hello, this is a test file!");
        
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
            client.connect(remoteFilename, localFilePath);
        } catch (java.io.IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
    
    private static void createTestFile(String baseDirectory, String filename, String content) {
        try {
            java.io.File file = new java.io.File(baseDirectory, filename);
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(content);
            writer.close();
            System.out.println("Test file created: " + file.getAbsolutePath());
        } catch (java.io.IOException e) {
            System.err.println("Error creating test file: " + e.getMessage());
        }
    }
}
