package org.example;

import java.io.*;
import java.net.Socket;

public class FileClient {
    private final String serverAddress;
    private final int serverPort;
    
    public FileClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }
    
    public void connect(String remoteFilename, String localFilePath) throws IOException {
        Socket socket = new Socket(serverAddress, serverPort);
        System.out.println("Connected to server: " + serverAddress + ":" + serverPort);
        
        sendRequest(socket, remoteFilename);
        receiveResponseAndFile(socket, localFilePath);
        
        socket.close();
    }
    
    private void sendRequest(Socket socket, String filename) throws IOException {
        OutputStream out = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out), true);
        
        String request = Protocol.buildGetRequest(filename);
        System.out.println("Sending request: " + request);
        writer.println(request);
        writer.flush();
    }
    
    private void receiveResponseAndFile(Socket socket, String localFilePath) throws IOException {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(socket.getInputStream())
        );
        
        String responseLine = reader.readLine();
        
        if (responseLine != null) {
            System.out.println("Received response: " + responseLine);
            
            if (Protocol.RESPONSE_OK.equals(responseLine)) {
                System.out.println("Server: File found (OK)");
                receiveFileBytes(socket, localFilePath);
            } else if (responseLine.startsWith(Protocol.RESPONSE_ERROR)) {
                System.out.println("Server: " + responseLine);
            }
        }
    }
    
    private void receiveFileBytes(Socket socket, String localFilePath) throws IOException {
        InputStream in = socket.getInputStream();
        FileOutputStream fileOut = new FileOutputStream(localFilePath);
        byte[] buffer = new byte[8192];
        int bytesRead;
        long totalBytes = 0;
        
        System.out.println("Receiving file bytes...");
        
        while ((bytesRead = in.read(buffer)) != -1) {
            fileOut.write(buffer, 0, bytesRead);
            totalBytes += bytesRead;
        }
        
        fileOut.flush();
        fileOut.close();
        
        System.out.println("File saved: " + localFilePath + " (" + totalBytes + " bytes)");
    }
}
