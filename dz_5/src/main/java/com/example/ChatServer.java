package com.example;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

// Серверная часть чата
class ChatServer {
    private static final int PORT = 55555;
    private static HashSet<ClientHandler> clients = new HashSet<>();
    private static ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                
                ClientHandler clientThread = new ClientHandler(socket);
                clients.add(clientThread);
                threadPool.execute(clientThread);
            }
        } catch (IOException ex) {
            System.out.println("Server error: " + ex.getMessage());
        }
    }

    // Метод для рассылки сообщений всем клиентам
    static void broadcastMessage(String message, ClientHandler excludeClient) {
        for (ClientHandler client : clients) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }

    // Внутренний класс для обработки клиентских подключений
    static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader input;
        private PrintWriter output;
        private String username;

        public ClientHandler(Socket socket) {
            try {
                this.socket = socket;
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException ex) {
                System.out.println("Client handler error: " + ex.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                // Запрос и установка username
                output.println("Enter your username:");
                username = input.readLine();
                System.out.println(username + " has joined the chat");
                broadcastMessage(username + " has joined the chat", this);

                // Прием и пересылка сообщений
                String message;
                while ((message = input.readLine()) != null) {
                    if ("exit".equalsIgnoreCase(message)) break;
                    broadcastMessage(username + ": " + message, this);
                }
            } catch (IOException ex) {
                System.out.println(username + " left the chat");
            } finally {
                // Очистка ресурсов при отключении
                clients.remove(this);
                broadcastMessage(username + " has left the chat", this);
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void sendMessage(String message) {
            output.println(message);
        }
    }
}