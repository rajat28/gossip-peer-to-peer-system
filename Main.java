import java.sql.Timestamp;
import java.util.Date;

public class Main implements Runnable {
    private static CmdLineParser cmdLine;
    private static String[] params;
    private static String database;

    public static String getTimestamp(String timestamp) {
        String currentTimestamp = "";
        currentTimestamp = timestamp;
        if (currentTimestamp != null) {
            return currentTimestamp;
        } else {
            Timestamp currentTime = new Timestamp(new Date().getTime());
            currentTimestamp = currentTime.toString();
            currentTimestamp = currentTimestamp.replaceAll("[\\s+:\\.]", "-") + "Z";
            return currentTimestamp;
        }
    }

    @Override
    public void run() {
        try {
            Thread currentThread = Thread.currentThread();

            if (currentThread.getName().contains("SERVER")) {
                int port = Integer.parseInt(params[0]);
                Server server = new Server(port, database);
                server.TCPUDPServer();
            } else if (currentThread.getName().contains("TCP")) {
                TCPClient TCPObj = new TCPClient();
                TCPObj.TCP(params);
            } else if (currentThread.getName().contains("UDP")) {
                UDPClient UDPObj = new UDPClient();
                UDPObj.UDP(params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        try {
            cmdLine = new CmdLineParser();
            params = cmdLine.parse_options(args);

            params[3] = getTimestamp(params[3]);
            String tcpFlag = params[4];
            String udpFlag = params[5];
            database = params[6];

            if (database != null) {
                Thread serverThread = new Thread(new Main());
                serverThread.setName("SERVER");
                serverThread.start();
            } else if (tcpFlag == "TCP") {
                Thread tcpThread = new Thread(new Main());
                tcpThread.setName("TCP");
                tcpThread.start();
            } else if (udpFlag == "UDP") {
                Thread udpThread = new Thread(new Main());
                udpThread.setName("UDP");
                udpThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
