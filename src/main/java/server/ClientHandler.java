package server;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler {

    private MainServer server;
    private Socket socket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    String nick;
    ArrayList<String> blacklist; // = new ArrayList<>();

    public ClientHandler (MainServer server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    while (true) {
                        String str = inputStream.readUTF();
                        if (str.startsWith("/auth")) {
                            String[] tokens = str.split(" ");
                            String newNick = AuthService.getNickByLoginAndPass(tokens[1], tokens[2]);
                            if (newNick != null) {
                                if (!server.isNickBusy(newNick)) {
                                    sendMsg("/authok");
                                    nick = newNick;
                                    server.subscribe(this);
                                    break;
                                } else  {
                                    sendMsg("Login have been already used");
                                }
                            } else {
                                sendMsg("Login or password's wrong");
                            }
                        }
                    }
                    while (true) {
                        String str = inputStream.readUTF();
                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                outputStream.writeUTF("/serverclosed");
                                break;
                            }
                            if (str.startsWith("/w")) {
                                String[] tokens = str.split(" ", 3);
                                server.sendPersonalMsg(this, tokens[1], tokens[2]);
                            }
                            if (str.startsWith("/blacklist")) {
                                String[] tokens = str.split(" ");
                                blacklist.add(tokens[1]);
                                sendMsg("You're blocked user: " + tokens[1]);
                            }
                        } else {
                            server.broadcastMsq(this, nick + ": " + str);
                        }


                        System.out.println("Client: " + str);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    server.unsubscribe(this);
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendMsg(String msg) {
        try {
            outputStream.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getNick() {
        return nick;
    }

    public boolean checkBlackList(String nick) {
        return blacklist.contains(nick);
    }
}
