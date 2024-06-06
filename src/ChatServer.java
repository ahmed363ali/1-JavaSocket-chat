package com.example.chatserver;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Set<UserThread> userThreads = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat server is listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New user connected");

                UserThread newUser = new UserThread(socket, userThreads);
                userThreads.add(newUser);
                newUser.start();
            }
        } catch (IOException ex) {
            System.out.println("Error in the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

class UserThread extends Thread {
    private Socket socket;
    private PrintWriter writer;
    private static Set<UserThread> userThreads;
    private String userName;

    public UserThread(Socket socket, Set<UserThread> userThreads) {
        this.socket = socket;
        UserThread.userThreads = userThreads;
    }

    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);

            printUsers();

            userName = reader.readLine();
            String serverMessage = "New user connected: " + userName;
            broadcast(serverMessage, this);

            String clientMessage;

            do {
                clientMessage = reader.readLine();
                if (clientMessage != null) {
                    System.out.println("Received message: " + clientMessage); // Debug message
                    serverMessage = "[" + userName + "]: " + clientMessage;
                    broadcast(serverMessage, this);
                }
            } while (clientMessage != null && !clientMessage.equalsIgnoreCase("bye"));

            removeUser(this);
            socket.close();

            serverMessage = userName + " has quitted.";
            broadcast(serverMessage, this);

        } catch (IOException ex) {
            System.out.println("Error in UserThread: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    void printUsers() {
        if (userThreads.isEmpty()) {
            writer.println("No other users connected");
        } else {
            writer.println("Connected users: ");
            for (UserThread userThread : userThreads) {
                writer.println(userThread.userName);
            }
        }
    }

    void broadcast(String message, UserThread excludeUser) {
        for (UserThread aUser : userThreads) {
            if (aUser != excludeUser) {
                aUser.writer.println(message);
            }
        }
    }

    void removeUser(UserThread aUser) {
        boolean removed = userThreads.remove(aUser);
        if (removed) {
            System.out.println("The user " + userName + " quitted");
        }
    }
}
