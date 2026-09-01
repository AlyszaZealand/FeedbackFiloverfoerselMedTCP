package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {
    private final int port;
    private final String baseDirectory;
    private ServerSocket serverSocket;
    
    public FileServer(int port, String baseDirectory) {
        this.port = port;
        this.baseDirectory = baseDirectory;
    }
    
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("FileServer started on port " + port);
        System.out.println("Base directory: " + baseDirectory);
        
        Socket clientSocket = serverSocket.accept();
        System.out.println("New client connected: " + clientSocket.getInetAddress());
        
        handleClient(clientSocket);
        
        clientSocket.close();
        serverSocket.close();
    }
    
    private void handleClient(Socket clientSocket) throws IOException {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(clientSocket.getInputStream())
        );
        
        OutputStream out = clientSocket.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out), true);
        
        String commandString = reader.readLine();
        
        if (commandString != null) {
            try {
                Protocol.ParsedCommand parsedCommand = Protocol.parseCommand(commandString);
                System.out.println("Received command: " + commandString);
                
                if ("GET".equals(parsedCommand.command)) {
                    handleGetRequest(parsedCommand.parameter, writer, out);
                }
            } catch (Protocol.ProtocolException e) {
                System.err.println("Protocol error: " + e.getMessage());
            }
        }
    }
    
    private void handleGetRequest(String filename, PrintWriter writer, OutputStream out) throws IOException {
        if (isBlockedFile(filename)) {
            String errorResponse = Protocol.buildErrorResponse("Access denied: file is blocked");
            writer.println(errorResponse);
            System.out.println("Sent: " + errorResponse);
            return;
        }
        
        File file = new File(baseDirectory, filename);
        
        if (file.exists() && file.isFile()) {
            writer.println(Protocol.buildOkResponse());
            writer.flush();
            System.out.println("Sent: " + Protocol.buildOkResponse());
            
            sendFileBytes(file, out);
        } else {
            String errorResponse = Protocol.buildErrorResponse("File not found: " + filename);
            writer.println(errorResponse);
            System.out.println("Sent: " + errorResponse);
        }
    }
    
    private boolean isBlockedFile(String filename) {
        return "hemmelig.txt".equals(filename);
    }
    
    private void sendFileBytes(File file, OutputStream out) throws IOException {
        FileInputStream fileInput = new FileInputStream(file);
        byte[] buffer = new byte[8192];
        int bytesRead;
        long totalBytes = 0;
        
        while ((bytesRead = fileInput.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
            totalBytes += bytesRead;
        }
        
        out.flush();
        fileInput.close();
        
        System.out.println("File sent: " + file.getName() + " (" + totalBytes + " bytes)");
    }
    
    public static void main(String[] args) {
        int port = 5000;
        String baseDirectory = "./test-files";
        
        java.io.File baseDir = new java.io.File(baseDirectory);
        baseDir.mkdirs();
        
//        createTestFile(baseDirectory, "test.txt", "Hello, this is a test file!");
        
        FileServer server = new FileServer(port, baseDirectory);
        
        try {
            server.start();
        } catch (java.io.IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    // vi har en test.txt i test-files lavet.
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
