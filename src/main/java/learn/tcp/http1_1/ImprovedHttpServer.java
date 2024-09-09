package learn.tcp.http1_1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImprovedHttpServer {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("HTTP/1.1 Server is listening on port " + port);

        ExecutorService executor = Executors.newFixedThreadPool(10); // 10개의 스레드를 가진 풀 생성

        while (true) {
            Socket clientSocket = serverSocket.accept();
            executor.execute(() -> handleClient(clientSocket));
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream out = clientSocket.getOutputStream()
        ) {
            // 요청 라인 파싱
            String requestLine = in.readLine();
            if (requestLine == null) {
                return;
            }

            String[] requestParts = requestLine.split(" ");
            String method = requestParts[0];
            String path = requestParts[1];

            System.out.println("Received: " + requestLine);

            // 헤더 파싱
            Map<String, String> headers = new HashMap<>();
            String headerLine;
            while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
                int colonIndex = headerLine.indexOf(':');
                if (colonIndex > 0) {
                    String key = headerLine.substring(0, colonIndex).trim();
                    String value = headerLine.substring(colonIndex + 1).trim();
                    headers.put(key, value);
                }
            }

            // 요청 본문 읽기 (POST, PUT 등의 경우)
            StringBuilder requestBody = new StringBuilder();
            if ("POST".equals(method) || "PUT".equals(method)) {
                int contentLength = Integer.parseInt(headers.getOrDefault("Content-Length", "0"));
                char[] buffer = new char[1024];
                int bytesRead;
                int totalBytesRead = 0;
                while (totalBytesRead < contentLength && (bytesRead = in.read(buffer, 0, Math.min(buffer.length, contentLength - totalBytesRead))) != -1) {
                    requestBody.append(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }
            }

            System.out.println("====================");
            // 응답 생성 및 전송
            for (String key : headers.keySet()) {
                System.out.print("key = " + key + ", value = " + headers.get(key) + " ");
            }
            System.out.println("====================");
            System.out.println("requestBody = " + requestBody);
            String responseBody = """
                    <html><body><h1>Hello, HTTP/1.1!</h1>
                    <p>Method: %s </p>
                    <p>Path: %s </p>
                    <p>Request Body: %s </p></body></html>
                    """.formatted(method, path, requestBody.toString());

            out.write("HTTP/1.1 200 OK\r\n".getBytes());
            out.write("Content-Type: text/html\r\n".getBytes());
            out.write(("Content-Length: " + responseBody.length() + "\r\n").getBytes());
            out.write("\r\n".getBytes());
            out.write(responseBody.getBytes());
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
