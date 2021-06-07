package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class MainServer {
    private Vector<ClientHandler> clients;

    public MainServer() {
        clients = new Vector<>();
        ServerSocket server = null;
        Socket socket = null;

        try {
            AuthService.connect();
            //AuthService.addUser("login1","pass1","nick1");
            server = new ServerSocket(8189);
            System.out.println("Server was started");

            while (true) {
                socket = server.accept();
                System.out.println("New client was connected");
                new ClientHandler(this,socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }

    public void broadcastMsq(ClientHandler from, String msg) {
        for (ClientHandler o : clients) {
            if (!o.checkBlackList(from.getNick())) {
                o.sendMsg(msg);
            }// else System.out.println("You're blocked!");

        }
    }

    public void broadcastClientsList() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("/clientList");
        for (ClientHandler o: clients) {
            stringBuilder.append(o.getNick() + " ");
        }
        String out = stringBuilder.toString();
        for (ClientHandler o: clients) {
            o.sendMsg(out);
        }
    }

    public void sendPersonalMsg(ClientHandler from, String nickTo, String msg) {
        for (ClientHandler o: clients) {
            if (o.getNick().equals(nickTo)) {
                o.sendMsg("from " + from.getNick() + ": " + msg);
                from.sendMsg("to " + nickTo + ": " + msg);
                return;
            }
        }
        from.sendMsg("User with nickname " + nickTo + " cannot be found");
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
        broadcastClientsList();
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
        broadcastClientsList();
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nick)){
                return true;
            }
        }
        return false;
    }
}
