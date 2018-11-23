import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;

    private ChatClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    /*
     * This starts the Chat Client
     */
    private boolean start() {
        // Create a socket
        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            System.out.println("The specificed server doesn't exist, or the port is already in use!");
            System.exit(0);
        }

        // Create your input and output streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This thread will listen from the server for incoming messages
        Runnable r = new ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        // After starting, send the clients username to the server.
        try {
            sOutput.writeObject(username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) {
        // Get proper arguments and override defaults
        String server = "localhost";
        int port = 1500;
        String userName = "Anonymous";

        if (args.length >= 1) {
            userName = args[0];
        }
        if (args.length >= 2) {
            try {
                int p = Integer.parseInt(args[1]);
                port = p;
            } catch (Exception e) {
                port = 1500;
            }
        }
        if (args.length == 3) {
            server = args[2];
        }

        // Create your client and start it
        ChatClient client = new ChatClient(server, port, userName);
        client.start();

        // Send an empty message to the server
        //client.sendMessage(new ChatMessage("Ping", 0));
        String message = "";
        Scanner input = new Scanner(System.in);
        while (!(message.toLowerCase().equals("/logout"))) {
            message = input.nextLine();
            int type = 0;
            if (message.toLowerCase().equals("/logout")) {
                type = 1;
            }
            client.sendMessage(new ChatMessage(message, type));
        }
        try {
            client.socket.close();
            client.sOutput.close();
            client.sInput.close();
        } catch (IOException e) {

        }
    }


    /*
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     */
    private final class ListenFromServer implements Runnable {
        public void run() {
            while (true) {
                try {
                    String msg = (String) sInput.readObject();
                    System.out.print(msg + "\n");
                } catch (IOException | ClassNotFoundException e) {
                    //e.printStackTrace();
                    break;
                }
            }
        }
    }
}
