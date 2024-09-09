
package learn.tcp.http1_1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class BasicTcpServerWithHttp {

    public static void main(String[] args) {
        int port = 8080;
        ServerSocket serverSocket = createServerSocketWith(port);
        System.out.println("HTTP/1.1. Server is listening on port " + port);

        while (true) {
            Socket clientSocket = acceptSocket(serverSocket);
            System.out.println("New Client connection from " + clientSocket.getRemoteSocketAddress());

            try (InputStream inputStream = clientSocket.getInputStream()) {
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String requestLine  = in.readLine();
                System.out.println("Received: " + requestLine);

                Map<String, String> headers = new HashMap<>();
                String inputLine;
                while ((inputLine = in.readLine()) != null && !inputLine.isEmpty()) {
                    int colonIndex = inputLine.indexOf(':');
                    if (colonIndex > 0) {
                        String key = inputLine.substring(0, colonIndex).trim();
                        String value = inputLine.substring(colonIndex + 1).trim();
                        headers.put(key, value);
                    }
                    System.out.println(inputLine);
                }


                String contentType = headers.get("Content-Type");
                String contentLength = headers.get("Content-Length");
                System.out.println("contentType: " + contentType);
                System.out.println("contentLength: " + contentLength);

                // Send response
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/html");
                out.println();
                out.println("<html><body><h1>Hello, HTTP/1.1!</h1></body></html>");

                clientSocket.close();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private static ServerSocket createServerSocketWith(int port) {
        try {
           return new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Socket acceptSocket(ServerSocket serverSocket) {
        try {
            return serverSocket.accept();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
