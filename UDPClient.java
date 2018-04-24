import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class UDPClient {
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

    public static void UDP(String[] params) throws Exception {

        String timestamp = "";

        int port = Integer.parseInt(params[0]);
        String ip = params[1];
        String message = params[2];
        timestamp = params[3];

        String modifiedMessage;
        byte encodeMessage[];

        byte[] sendData, receiveData;
        DatagramPacket sendPacket, receivePacket;
        DatagramSocket clientSocket = new DatagramSocket();
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        InetAddress IPAddress = InetAddress.getByName(ip);

        PrintStream printStream;

        System.out.println("Enter 'Q' to quit the connection");

        if (message != null) {
            printStream = new PrintStream("active.txt");

            sendData = encodeGossip(message, timestamp);
            sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);

            printStream.print("GOSSIP");
            printStream.flush();

            clientSocket.send(sendPacket);

            receiveData = new byte[1024];
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            modifiedMessage = new String(receivePacket.getData());
            System.out.println("FROM SERVER: " + modifiedMessage);
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
                    sendData = encodePeers(message);
                    printStream.print("PEERS?");
                } else if (message.contains("PEER")) {
                    sendData = encodePeer(message);
                    printStream.print("PEER");
                } else {
                    sendData = encodeGossip(message, timestamp);
                    printStream.print("GOSSIP");
                }
                printStream.flush();

                sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                clientSocket.send(sendPacket);

                receiveData = new byte[1024];
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivePacket);
                modifiedMessage = new String(receivePacket.getData());
                System.out.println("FROM SERVER: " + modifiedMessage);
            }
        }
    }
}