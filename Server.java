import net.ddp2p.ASN1.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Server implements Runnable {
    private static Database database;
    private static ServerPeerWitness serverPeerWitness;
    private static Gossip gossip;
    private static Peer peer;
    private static PeersQuery peersQuery;
    private static PeerAnswer peerAnswer;
    private static CharsetEncoder encoder;
    private SocketChannel tcpClient;
    private static int port;
    private static String databasePath;

    public Server(int port, String database) {
        this.port = port;
        databasePath = database;
    }

    Server(SocketChannel client) {
        tcpClient = client;
    }

    public void TCPUDPServer() {

        try {
            encoder = Charset.forName("US-ASCII").newEncoder();

            database = new Database(databasePath);
            gossip = new Gossip();
            peer = new Peer();
            peersQuery = new PeersQuery();
            peerAnswer = new PeerAnswer();

            SocketAddress localport = new InetSocketAddress(port);

            ServerSocketChannel tcpserver = ServerSocketChannel.open();
            tcpserver.socket().bind(localport);

            DatagramChannel udpserver = DatagramChannel.open();
            udpserver.socket().bind(localport);

            tcpserver.configureBlocking(false);
            udpserver.configureBlocking(false);

            Selector selector = Selector.open();

            tcpserver.register(selector, SelectionKey.OP_ACCEPT);
            udpserver.register(selector, SelectionKey.OP_READ);

            database.createTableGossip();
            database.createTablePeers();

            for (; ; ) {
                try {
                    selector.select();

                    Set keys = selector.selectedKeys();


                    for (Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
                        SelectionKey key = (SelectionKey) iterator.next();
                        iterator.remove();

                        Channel c = (Channel) key.channel();

                        if (key.isAcceptable() && c == tcpserver) {
                            SocketChannel client = tcpserver.accept();

                            if (client != null) {
                                Thread thread = new Thread(new Server(client));
                                thread.start();

                            }
                        } else if (key.isReadable() && c == udpserver) {
                            ByteBuffer buf = ByteBuffer.allocate(2048);
                            SocketAddress clientAddress = udpserver.receive(buf);

                            if (clientAddress != null) {
                                String formattedMessage;
                                buf.flip();
                                int limits = buf.limit();
                                byte bytes[] = new byte[limits];
                                buf.get(bytes, 0, limits);
                                Decoder decoder = new Decoder(bytes);

                                String line, currentActive = "";

                                FileReader fileReader = new FileReader("active.txt");
                                BufferedReader bufferedReader = new BufferedReader(fileReader);
                                if ((line = bufferedReader.readLine()) != null) {
                                    currentActive = line;
                                }

                                String msg = "";
                                if (currentActive.equals("GOSSIP")) {
                                    String encodedMessage = gossip.decode(decoder).sha256hash;
                                    String timestamp = gossip.decode(decoder).timestamp;
                                    String message = gossip.decode(decoder).message;

                                    msg = "GOSSIP:" + encodedMessage + ":" + timestamp + ":" + message + "%";
                                } else if (currentActive.equals("PEER")) {
                                    String name = peer.decode(decoder).name;
                                    int port = peer.decode(decoder).port;
                                    String ip = peer.decode(decoder).ip;

                                    msg = "PEER:" + name + ":PORT=" + port + ":IP=" + ip;
                                } else if (currentActive.equals("PEERS?")) {
                                    msg = peersQuery.decode(decoder).query;

                                    String names[] = database.returnPeerName();
                                    int ports[] = database.returnPeerPort();
                                    String ip[] = database.returnPeerIP();

                                    for (int i = 0; i < database.printTablePeers().length() && names[i] != null; i++ ) {
                                        peerAnswer.peers.add(new Peer(names[i], ports[i], ip[i]));
                                    }

                                    byte peerAnswerBytes[] = peerAnswer.getEncoder().getBytes();

                                    decoder = new Decoder(peerAnswerBytes);
                                    peerAnswer.decode(decoder);

                                    StringBuilder peersResult = new StringBuilder();
                                    peersResult.append("PEERS|" + peerAnswer.peers.size() + "|");
                                    for (int i = 0; i < peerAnswer.peers.size(); i++) {
                                        peersResult.append(peerAnswer.peers.get(i).name + ":PORT=" + peerAnswer.peers.get(i).port +
                                                ":IP=" + peerAnswer.peers.get(i).ip + "|");
                                    }
                                    peersResult.append("%\n");

                                    ByteBuffer response = encoder.encode(CharBuffer.wrap(peersResult));
                                    buf.rewind();
                                    udpserver.send(response, clientAddress);
                                    peersResult.setLength(0);
                                    peerAnswer.peers.clear();
                                    buf.clear();
                                    continue;
                                }


                                formattedMessage = parse(msg.replaceAll("(\\r|\\n)", "").trim());
                                ByteBuffer response = encoder.encode(CharBuffer.wrap(formattedMessage));
                                buf.rewind();
                                udpserver.send(response, clientAddress);
                                buf.clear();
                            }
                        }
                    }
                } catch (IOException e) {
                    Logger l = Logger.getLogger(Server.class.getName());
                    l.log(Level.WARNING, "IOException in Server", e);
                } catch (Throwable t) {
                    Logger l = Logger.getLogger(Server.class.getName());
                    l.log(Level.SEVERE, "FATAL error in Server", t);
                    System.exit(1);
                }
            }
        } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
        }
    }


    public static String parse(String message) {
        if ((message.contains("GOSSIP") || message.contains("gossip")) && message.endsWith("%")) {
            String data[] = message.split(":");

            if (data.length == 4) {
                String dateAndTime[] = data[2].split("-");
                String text = data[3].replace("%", "");
                String finalMessage = "\"" + text + "\"" + " generated at " + dateAndTime[1] + "/" + dateAndTime[2]
                        + "/" + dateAndTime[0] + " at " + dateAndTime[3] + " H, Minute " + dateAndTime[4] + ", Second "
                        + dateAndTime[5] + ", and Millisecond " + dateAndTime[6].replace("Z", "") + " UTC\n";

                int port[] = database.returnPeerPort();

                if (database.insertTableGossip(message)) {

                    try {
                        serverPeerWitness = new ServerPeerWitness();
                        Socket socket1 = new Socket("localhost", serverPeerWitness.getPort());
                        PrintWriter out = new PrintWriter(socket1.getOutputStream(), true);
                        out.println(finalMessage);
                        out.close();
                        socket1.close();
                    } catch (IOException e) {
                    }

                    try {
                        for (int peerPorts : port) {
                            byte buffer[] = finalMessage.getBytes();
                            DatagramSocket socket = new DatagramSocket();
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("127.0.0.1"), peerPorts);
                            socket.send(packet);
                            socket.close();
                        }
                    } catch (IOException e) {
                        System.err.println("Broadcast: " + finalMessage);
                    }

                    return finalMessage;
                }
            }
        } else if (message.contains("PEERS?") || message.contains("peers?")) {
            return (database.printTablePeers().trim() + "\n");
        } else if (message.contains("PEER") || message.contains("peer")) {
            String data[] = message.split(":");
            String port[] = data[2].split("=");
            String IP[] = data[3].split("=");

            if (data.length == 4) {
                String finalMessage = "\"" + data[1] + "\"" + " with " + data[3] + " at " + data[2] + "\n";
                database.insertTablePeers(data[1], Integer.parseInt(port[1]), IP[1].substring(0, IP[1].length() - 1));
                return finalMessage;
            }
        }

        return "Invalid Message\n";
    }

    @Override
    public void run() {
        try {
            String formattedResult = "";
            String formattedMessage;

            long currentTime = System.currentTimeMillis();
            long endTime = currentTime + 20000;

            while (!tcpClient.socket().isClosed()) {
                byte buffer[] = new byte[10000];

                InputStream inputStream = tcpClient.socket().getInputStream();
                int readBytes = inputStream.read(buffer);
                if (readBytes == -1) {
                    break;
                }

                Decoder decoder = new Decoder(buffer);
                if (!decoder.fetchAll(inputStream)) {
                    System.out.println("Buffer too small!");
                    continue;
                }

                String line, currentActive = "";

                FileReader fileReader = new FileReader("active.txt");
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                if ((line = bufferedReader.readLine()) != null) {
                    currentActive = line;
                }

                String result = "";
                if (currentActive.equals("GOSSIP")) {
                    String encodedMessage = gossip.decode(decoder).sha256hash;
                    String timestamp = gossip.decode(decoder).timestamp;
                    String message = gossip.decode(decoder).message;

                    result = "GOSSIP:" + encodedMessage + ":" + timestamp + ":" + message + "%";
                } else if (currentActive.equals("PEER")) {
                    String name = peer.decode(decoder).name;
                    int port = peer.decode(decoder).port;
                    String ip = peer.decode(decoder).ip;

                    result = "PEER:" + name + ":PORT=" + port + ":IP=" + ip;
                } else if (currentActive.equals("PEERS?")) {
                    result = peersQuery.decode(decoder).query;

                    String names[] = database.returnPeerName();
                    int ports[] = database.returnPeerPort();
                    String ip[] = database.returnPeerIP();

                    for (int i = 0; i < database.printTablePeers().length() && names[i] != null; i++ ) {
                        peerAnswer.peers.add(new Peer(names[i], ports[i], ip[i]));
                    }

                    byte peerAnswerBytes[] = peerAnswer.getEncoder().getBytes();

                    decoder = new Decoder(peerAnswerBytes);
                    peerAnswer.decode(decoder);

                    StringBuilder peersResult = new StringBuilder();
                    peersResult.append("PEERS|" + peerAnswer.peers.size() + "|");
                    for (int i = 0; i < peerAnswer.peers.size(); i++) {
                        peersResult.append(peerAnswer.peers.get(i).name + ":PORT=" + peerAnswer.peers.get(i).port +
                                ":IP=" + peerAnswer.peers.get(i).ip + "|");
                    }
                    peersResult.append("%\n");

                    ByteBuffer response = encoder.encode(CharBuffer.wrap(peersResult));
                    tcpClient.write(response);
                    peersResult.setLength(0);
                    peerAnswer.peers.clear();
                    continue;
                }

                if (!result.contains("%") && !result.contains("\\n")) {
                    formattedResult += result;
                    System.out.println(formattedResult);
                } else {
                    formattedResult += result;
                    formattedResult = formattedResult.replaceAll("(\\r|\\n)", "").trim();

                    if (!formattedResult.isEmpty()) {
                        formattedMessage = parse(formattedResult);
                        ByteBuffer response = encoder.encode(CharBuffer.wrap(formattedMessage));
                        tcpClient.write(response);
                        formattedResult = "";
                    }
                }
                bufferedReader.close();

                if (System.currentTimeMillis() > endTime) {
                    ByteBuffer response = encoder.encode(CharBuffer.wrap("20 seconds timeout!"));
                    tcpClient.write(response);
                    System.out.println("20 seconds timeout!");
                    break;
                }
            }
            tcpClient.close();
            Thread.currentThread().interrupt();
        } catch (CharacterCodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ASN1DecoderFail asn1DecoderFail) {
            asn1DecoderFail.printStackTrace();
        }
    }
}