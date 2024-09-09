package learn.tcp.http1_1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImprovedHttpServerWithConnectionHandling {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("HTTP/1.1 Server is listening on port " + port);

        ExecutorService executor = Executors.newFixedThreadPool(10);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            executor.execute(() -> handleClient(clientSocket));
        }
    }

    private static void handleClient(Socket clientSocket) {
        String clientAddress = clientSocket.getInetAddress().getHostAddress();
        int clientPort = clientSocket.getPort();
        LocalDateTime connectionTime = LocalDateTime.now();

        System.out.printf("New connection from %s:%d at %s%n", clientAddress, clientPort, connectionTime);

        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream out = clientSocket.getOutputStream()
        ) {
            clientSocket.setSoTimeout(30000); // 30 seconds timeout

            String requestLine = in.readLine();
            if (requestLine != null) {
                System.out.printf("Received from %s:%d: %s%n", clientAddress, clientPort, requestLine);

                Map<String, String> headers = readHeaders(in);
                String requestBody = readBody(in, headers);

                // 응답 처리
                sendResponse(out, requestLine, headers, requestBody);

                // Keep-Alive 처리
                if ("keep-alive".equalsIgnoreCase(headers.get("Connection"))) {
                    // Keep-Alive 로직 구현
                    System.out.printf("Keeping connection alive for %s:%d%n", clientAddress, clientPort);
                    // 여기서 연결을 계속 유지하고 추가 요청을 처리할 수 있습니다.
                }
            } else {
                System.out.printf("No data received from %s:%d%n", clientAddress, clientPort);
            }
        } catch (SocketTimeoutException e) {
            System.out.printf("Connection timed out for %s:%d%n", clientAddress, clientPort);
        } catch (IOException e) {
            System.out.printf("Error handling client %s:%d: %s%n", clientAddress, clientPort, e.getMessage());
        } finally {
            try {
                clientSocket.close();
                LocalDateTime disconnectionTime = LocalDateTime.now();
                System.out.printf("Connection closed for %s:%d at %s%n", clientAddress, clientPort, disconnectionTime);
            } catch (IOException e) {
                System.out.printf("Error closing connection for %s:%d: %s%n", clientAddress, clientPort, e.getMessage());
            }
        }
    }

    private static Map<String, String> readHeaders(BufferedReader in) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String headerLine;
        System.out.println("Headers: ");
        while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            int colonIndex = headerLine.indexOf(':');
            if (colonIndex > 0) {
                String key = headerLine.substring(0, colonIndex).trim();
                String value = headerLine.substring(colonIndex + 1).trim();
                headers.put(key, value);
                System.out.print(headerLine + " || ");
            }
        }
        return headers;
    }

    private static String readBody(BufferedReader in, Map<String, String> headers) throws IOException {
        StringBuilder body = new StringBuilder();
        if (headers.containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(headers.get("Content-Length"));
            char[] buffer = new char[1024];
            int bytesRead;
            int totalBytesRead = 0;
            while (totalBytesRead < contentLength && (bytesRead = in.read(buffer, 0, Math.min(buffer.length, contentLength - totalBytesRead))) != -1) {
                body.append(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }
        }
        return body.toString();
    }

    private static void sendResponse(OutputStream out, String requestLine, Map<String, String> headers, String requestBody) throws IOException {
        String responseBody = "<html><body>" +
                              "<h1>Hello, HTTP/1.1!</h1>" +
                              "<p>Request: " + requestLine + "</p>" +
                              "<p>Headers: " + headers + "</p>" +
                              "<p>Body: " + requestBody + "</p>" +
                              "</body></html>";

        out.write("HTTP/1.1 200 OK\r\n".getBytes());
        out.write("Content-Type: text/html\r\n".getBytes());
        out.write(("Content-Length: " + responseBody.length() + "\r\n").getBytes());
        out.write("Connection: close\r\n".getBytes());
        out.write("\r\n".getBytes());
        out.write(responseBody.getBytes());
        out.flush();
    }
}
