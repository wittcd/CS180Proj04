import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    private String badwords;

    //TODO
    // - Error handling - Fixed error when client starts without server running, can't test Username handling right now so I'll get back to it later
    // - chat filtering - Done, but can't figure out relative path for some reason
    // - personal messages
    // - list



    private ChatServer(int port, String badwords) {
        this.port = port;
        this.badwords = badwords;
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        String badwords = "C:\\Users\\justi\\IdeaProjects\\proj4\\src\\badwords.txt";
        int port = 1500;
        if (args.length == 1) {
            try {
                int p = Integer.parseInt(args[0]);
                port = p;
            } catch (Exception e) {
                port = 1500;
            }
        } else if (args.length == 2) {
            try {
                int p = Integer.parseInt(args[0]);
                port = p;
            } catch (Exception e) {
                port = 1500;
            }
            badwords = args[1];

        }
        ChatServer server = new ChatServer(port, badwords);
        server.start();
    }


    /*
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        private synchronized void broadcast(String message) {
            ChatFilter cf = new ChatFilter(badwords);
            for (int x = 0; x < clients.size(); x++) {
                try {
                    clients.get(x).writeMessage(cf.filter(message));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private synchronized void remove(int id) {
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).id == id) {
                    clients.remove(i);
                    break;
                }
            }
        }

        private void close() {
            try {
                sOutput.close();
                sInput.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private boolean writeMessage(String msg) {
            if (!socket.isConnected()) {
                return false;
            } else {
                try {
                    sOutput.writeObject(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        }

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            // Read the username sent to you by client
            while (true) {
                try {
                    cm = (ChatMessage) sInput.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if (cm.getMessage().toLowerCase().equals("/logout")) {
                    remove(this.id);
                    close();
                    break;
                } else {
                    SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
                    Date d = new Date();
                    String time = f.format(d);
                    broadcast(time + " " + username + ": " + cm.getMessage());
                    System.out.println(time + " " + username + ": " + cm.getMessage());
                }

                // Send message back to the client
                /*try {
                    sOutput.writeObject("Pong");
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
        }
    }
}
