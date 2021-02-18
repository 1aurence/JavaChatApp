package com.muc;

import java.io.*;
import java.net.Socket;
import java.util.List;

//TODO add class comment
public class ServerWorker extends Thread {
    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;

    public ServerWorker(Server server, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        while ((line = reader.readLine()) != null) {
            // Split user input into list
            String[] tokens = line.split(" ");
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                if ("quit".equalsIgnoreCase(cmd)) {
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);

                } else if ("logout".equalsIgnoreCase(cmd)) {
                    handleLogout(outputStream);
                } else {
                    String msg = "Unknown command: " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }

        clientSocket.close();
    }

    public String getLogin() {
        return login;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {

        if (tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];
            if (this.login != null) {
                String msg = "Already logged in\n";
                outputStream.write(msg.getBytes());
            } else {
                if (login.equals("guest") && password.equals("guest") || login.equals("joe") && password.equals("joe")) {
                    String msg = "Ok login\n";
                    outputStream.write(msg.getBytes());
                    this.login = login;
                    System.out.println("User logged in successfully: " + login);

                    String onlineMsg = login + " has logged in" + "\n";
                    List<ServerWorker> workerList = server.getWorkerList();
                    for (ServerWorker worker : workerList) {
                        worker.send(onlineMsg);
                    }

                } else {
                    String msg = "Error login\n";
                    outputStream.write(msg.getBytes());
                }
            }


        }

    }

    private void handleLogout(OutputStream outputStream) throws IOException {
        if (this.login == null) {
            String msg = "Not logged in\n";
            outputStream.write(msg.getBytes());
        } else {
            String logoutMsg = this.login + " has logged out\n";
            List<ServerWorker> workerList = server.getWorkerList();
            for (ServerWorker worker : workerList) {
                worker.send(logoutMsg);
            }
            this.login = null;
        }

    }

    private void send(String onlineMsg) throws IOException {
        this.outputStream.write(onlineMsg.getBytes());
    }
}