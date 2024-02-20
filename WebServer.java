import java.util.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;

public class WebServer {
    public static void main(String[] args) {

        Map<String, String> contentTypes = new HashMap<>();
        contentTypes.put("html", "text/html");
        contentTypes.put("txt", "text/plain");
        contentTypes.put("jpg", "image/jpeg");
        contentTypes.put("jpeg", "image/jpeg");
        contentTypes.put("png", "image/png");

        int port = 8080;
        try{
            ServerSocket server = new ServerSocket(port) ;
            while(true) {
                Socket client = server.accept();
                Scanner clientIn = new Scanner(client.getInputStream());
                DataOutputStream clientOut = new DataOutputStream(client.getOutputStream());

                String reqMessage = clientIn.nextLine();
                System.out.println("Client request: " + reqMessage);

                String fileName = reqMessage.split("/")[1].split(" ")[0];
                System.out.println("File requested: " + fileName);

                String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                System.out.println("File type: " + fileExtension);

                if(!contentTypes.containsKey(fileExtension)){
                    // Tell client that type is not acceptable and close connections then loop again
                    System.out.println("Content type is not accepted");
                    String response = 
                        "HTTP/1.1 415 Unsupported Media Type\r\n" +
                        "Content-Type: " + contentTypes.get("txt") + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" +
                        "415 Unsupported Media Type: The requested content type is not supported.";
                    clientOut.writeBytes(response);
                    clientOut.flush();
                    
                    client.close();
                    clientIn.close();
                    clientOut.close();
                    continue;
                } // End of if

                String contentType = contentTypes.get(fileExtension);

                // Get files path and check if it exists
                Path filePath = Paths.get(fileName);

                // If it doesn't exist send 404 response message
                if(!Files.exists(filePath)) {
                    System.out.println("File Not Found");

                    String response = 
                        "HTTP/1.1 404 File Not Found\r\n" +
                        "Content-Type: " + contentType + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" +
                        "404 Not Found: The requested file was not found.";
                    clientOut.writeBytes(response);
                    clientOut.flush();
                    continue;
                }

                // All is good, send 200 OK response message and file contents
                byte[] fileBytes = Files.readAllBytes(filePath);
                String response = 
                    "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";
                clientOut.writeBytes(response);
                clientOut.flush();
                clientOut.write(fileBytes);
                clientOut.flush();

                // No memory leaks here
                client.close();
                clientIn.close();
                clientOut.close();
            }
        } catch(IOException ex){
            System.out.println("ERR: " + ex.getMessage());
        } // End of try catch

    } // End of method Main

} // End oof class WebServer