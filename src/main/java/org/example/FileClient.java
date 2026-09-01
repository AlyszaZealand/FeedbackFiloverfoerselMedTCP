package org.example;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class FileClient {
    private final String serverAddress;
    private final int serverPort;
    private Scanner scanner;
    
    public FileClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.scanner = new Scanner(System.in);
    }
    
    public void connect(String remoteFilename) throws IOException {
        Socket socket = new Socket(serverAddress, serverPort);
        System.out.println("Connected to server: " + serverAddress + ":" + serverPort);
        
        sendRequest(socket, remoteFilename);
        byte[] fileBytes = receiveFile(socket);
        
        socket.close();
        
        if (fileBytes != null) {
            saveFileWithUserInput(fileBytes);
        }
    }
    
    private void sendRequest(Socket socket, String filename) throws IOException {
        OutputStream out = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out), true);
        
        String request = Protocol.buildGetRequest(filename);
        System.out.println("Sending request: " + request);
        writer.println(request);
        writer.flush();
    }
    
    private byte[] receiveFile(Socket socket) throws IOException {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(socket.getInputStream())
        );
        
        String responseLine = reader.readLine();
        
        if (responseLine != null) {
            System.out.println("Received response: " + responseLine);
            
            if (Protocol.RESPONSE_OK.equals(responseLine)) {
                System.out.println("Server: File found (OK)");
                return receiveFileBytes(socket);
            } else if (responseLine.startsWith(Protocol.RESPONSE_ERROR)) {
                System.out.println("Server: " + responseLine);
            }
        }
        
        return null;
    }
    
    private byte[] receiveFileBytes(Socket socket) throws IOException {
        InputStream in = socket.getInputStream();
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        
        System.out.println("Receiving file bytes...");

        // Read until the end of the stream (fx 1 byte = sender 27 gange)
        while ((bytesRead = in.read(buffer)) != -1) {
            byteOut.write(buffer, 0, bytesRead);
        }
        
        byte[] fileBytes = byteOut.toByteArray();
        System.out.println("File received: " + fileBytes.length + " bytes");
        
        return fileBytes;
    }
    
    private void saveFileWithUserInput(byte[] fileBytes) {
        System.out.println("Enter filename to save - EXAMPLE: file.txt");
        String filename = scanner.nextLine().trim();
        
        if (filename.isEmpty()) {
            System.err.println("Filename cannot be empty");
            return;
        }
        
        try {
            FileOutputStream fileOut = new FileOutputStream(filename);
            fileOut.write(fileBytes);
            fileOut.flush();
            fileOut.close();
            
            System.out.println("File saved: " + filename);
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 5000;
        
        Scanner scanner = new Scanner(System.in);
        FileClient client = new FileClient(serverAddress, serverPort);
        
        try {
            System.out.println("File Transfer Client");
            System.out.println("Enter command (e.g., GET|test.txt):");
            String command = scanner.nextLine().trim();
            
            if (!command.startsWith("GET|")) {
                System.err.println("Invalid command. Must start with 'GET|'");
                return;
            }
            
            String remoteFilename = command.substring(4);
            
            client.connect(remoteFilename);
        } catch (java.io.IOException e) {
            System.err.println("Client error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
