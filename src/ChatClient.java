package com.example.chatclient;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChatClient {
    private String hostname;
    private int port;
    private String userName;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private JTextArea textArea;
    private JTextField textField;
    private JButton sendButton;
    private JFrame frame;

    public ChatClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void execute() {
        try {
            socket = new Socket(hostname, port);

            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true); // Auto-flush enabled

            createGUI();

            new ReadThread(socket, this).start();

        } catch (IOException ex) {
            System.out.println("Error connecting to the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void createGUI() {
        frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        textArea = new JTextArea();
        textArea.setEditable(false);
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        textField = new JTextField();
        textField.setEditable(true);
        textField.setColumns(25);  // Set a preferred size
        textField.setForeground(Color.BLACK);
        textField.setBackground(Color.LIGHT_GRAY);
        textField.setText("Write a message...");
        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals("Write a message...")) {
                    textField.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText("Write a message...");
                }
            }
        });

        // Add ActionListener to send message when Enter is pressed
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        panel.add(textField, BorderLayout.SOUTH);

        sendButton = new JButton("Send");
        panel.add(sendButton, BorderLayout.EAST);

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        frame.add(panel);
        frame.setVisible(true);
        textField.requestFocusInWindow();  // Request focus

        userName = JOptionPane.showInputDialog(frame, "Enter your username:");
        writer.println(userName);
        writer.flush(); // Ensure the username is sent immediately
    }

    private void sendMessage() {
        String message = textField.getText();
        System.out.println("Attempting to send message: " + message); // Debug message
        if (message != null && !message.trim().isEmpty() && !message.equals("Write a message...")) {
            writer.println(message);
            writer.flush(); // Ensure the message is sent immediately
            System.out.println("Message sent: " + message); // Debug message
            textArea.append("You: " + message + "\n"); // Display the sent message
            textField.setText(""); // Clear the input field after sending
        }
    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    String getUserName() {
        return this.userName;
    }

    void displayMessage(String message) {
        textArea.append(message + "\n");
    }

    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 12345;

        ChatClient client = new ChatClient(hostname, port);
        client.execute();
    }
}

class ReadThread extends Thread {
    private BufferedReader reader;
    private Socket socket;
    private ChatClient client;

    public ReadThread(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;

        try {
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } catch (IOException ex) {
            System.out.println("Error getting input stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                String response = reader.readLine();
                if (response != null) {
                    client.displayMessage(response);
                    System.out.println("Received message: " + response); // Debug message
                }
            } catch (IOException ex) {
                System.out.println("Error reading from server: " + ex.getMessage());
                ex.printStackTrace();
                break;
            }
        }
    }
}
