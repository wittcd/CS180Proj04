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

/**
 * Chat Server class, hosts chat clients
 * @author Justin Chen, Colin Witt
 * @version 1.0.0
 */

final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    private String badwords;


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
                boolean uniqueUser = false;
                int c = 1;
                String name = username;
                while (!uniqueUser) {
                    uniqueUser = true;
                    for (int x = 0; x < clients.size(); x++) {
                        if (clients.get(x).username.equals(name)) {
                            uniqueUser = false;
                            name = username + "(" + c + ")";
                            c++;
                        }
                    }
                }
                username = name;
                SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
                Date d = new Date();
                String time = f.format(d);
                System.out.println(time + " " + username + " just connected");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
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

            }
            try {
                sOutput.writeObject(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        private synchronized void broadcast(String message) {
            for (int x = 0; x < clients.size(); x++) {
                boolean write = clients.get(x).writeMessage(message);
                if (!write) {
                    this.remove(x);
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

        private synchronized void list(ClientThread caller) {
            for (int x = 0; x < clients.size(); x++) {
                if (clients.get(x).id != caller.id) {
                    caller.writeMessage(clients.get(x).username);
                }
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
                    SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
                    Date d = new Date();
                    String time = f.format(d);
                    System.out.println(time + " " + username + " disconnected with a LOGOUT message.");
                    remove(this.id);
                    close();
                    break;
                } else if (cm.getMessage().toLowerCase().equals("/list")) {
                    list(this);
                } else if (cm.getMessage().length() > 5 && cm.getMessage().toLowerCase().substring(0, 5).equals("/msg ")) {
                    String[] s = cm.getMessage().split(" ");
                    if (s.length >= 3) {
                        SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
                        Date d = new Date();
                        String time = f.format(d);
                        String recipient = s[1];
                        String msg = "";
                        for (int x = 2; x < s.length; x++) {
                            msg += s[x];
                            if (x < s.length - 1) {
                                msg += " ";
                            }
                        }
                        for (int x = 0; x < clients.size(); x++) {
                            if (clients.get(x).username.equals(recipient)) {
                                clients.get(x).writeMessage(time + " " + username + " -> " + recipient + ": " + msg);
                                System.out.println(time + " " + username + " -> " + recipient + ": " + msg);
                                this.writeMessage(time + " " + username + " -> " + recipient + ": " + msg);
                            }
                        }
                    }
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
