package com.example;

import java.io.*;
import java.net.*;

// Клиентская часть чата
class ChatClient {
    private static final String HOST = "localhost";
    private static final int PORT = 55555;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT)) {
            BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

            // Поток для приема сообщений
            Thread receiveThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = serverInput.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException ex) {
                    System.out.println("Connection closed");
                }
            });
            receiveThread.start();

            // Отправка сообщений с консоли
            String userInput;
            while ((userInput = consoleInput.readLine()) != null) {
                output.println(userInput);
                if ("exit".equalsIgnoreCase(userInput)) break;
            }

        } catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
        }
    }
}