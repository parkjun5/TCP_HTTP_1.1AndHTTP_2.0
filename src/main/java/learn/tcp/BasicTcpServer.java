package learn.tcp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class BasicTcpServer {

    public static void main(String[] args) {
        int port = 8080;
        ServerSocket serverSocket = createServerSocketWith(port);
        System.out.println("Listening on port " + port);

        while (true) {
            Socket clientSocket = acceptSocket(serverSocket);
            System.out.println("New Client connection from " + clientSocket.getRemoteSocketAddress());

            try (InputStream inputStream = clientSocket.getInputStream()) {
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Received: " + inputLine);
                    out.println("Server: " + inputLine);
                }

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
