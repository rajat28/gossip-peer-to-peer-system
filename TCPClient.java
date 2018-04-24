import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class TCPClient {
    private static Gossip gossip = new Gossip();
    private static Peer peer = new Peer();
    private static PeersQuery peersQuery = new PeersQuery();

    public static byte[] encodeGossip(final String message, String timestamp) throws NoSuchAlgorithmException {
        String clearText = message + timestamp;

        gossip.timestamp = timestamp;
        gossip.message = message;
        gossip.sha256hash = new String(Base64.getEncoder()
                .encode(MessageDigest.getInstance("SHA-256").digest(clearText.getBytes(StandardCharsets.UTF_8))));

        return gossip.encode();
    }

    public static byte[] encodePeer(String peerString) {
        String data[] = peerString.split(":");
        String port[] = data[2].split("=");
        String IP[] = data[3].split("=");

        peer.name = data[1];
        peer.port = Integer.parseInt(port[1]);
        peer.ip = IP[1];

        return peer.encode();
    }


    private static byte[] encodePeers(String peers) {
        peersQuery.query = peers;

        return peersQuery.encode();
    }

    public static void TCP(String params[]) {
        try {

            byte[] encode = gossip.encode();

            String timestamp = "";

            int port = Integer.parseInt(params[0]);
            String ip = params[1];
            String message = params[2];
            timestamp = params[3];

            String modifiedMessage;
            byte encodeMessage[];

            Socket clientSocket = null;
            clientSocket = new Socket(ip, port);

            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            PrintStream printStream;

            System.out.println("Enter 'Q' to quit the connection");

            if (message != null) {
                printStream = new PrintStream("active.txt");

                encodeMessage = encodeGossip(message, timestamp);
                printStream.print("GOSSIP");
                printStream.flush();

                outToServer.write(encodeMessage);
                modifiedMessage = inFromServer.readLine();
                System.out.println("FROM SERVER: " + modifiedMessage + "\n");
            }

            while (true) {
                printStream = new PrintStream("active.txt");

                System.out.print("Enter message: ");
                message = inFromUser.readLine();

                if (message.equalsIgnoreCase("Q")) {
                    clientSocket.close();
                    break;
                } else {
                    if (message.contains("PEERS?")) {
                        encodeMessage = encodePeers(message);
                        printStream.print("PEERS?");
                    } else if (message.contains("PEER")) {
                        encodeMessage = encodePeer(message);
                        printStream.print("PEER");
                    } else {
                        encodeMessage = encodeGossip(message, timestamp);
                        printStream.print("GOSSIP");
                    }
                    printStream.flush();

                    outToServer.write(encodeMessage);
                    modifiedMessage = inFromServer.readLine();
                    System.out.println("FROM SERVER: " + modifiedMessage + "\n");

                    printStream.close();
                }
            }
        } catch (SocketException e) {
            System.err.println("Connection reset!");
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}