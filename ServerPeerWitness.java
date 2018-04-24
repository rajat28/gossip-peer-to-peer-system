import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerPeerWitness {
    private static int port = 9999;

    public int getPort() {
        return port;
    }

    public static void main(String args[]) throws IOException {
        System.out.println("ServerPeerWitness is running on port 9999.");

        ServerSocket serverSocket = new ServerSocket(port);
        Socket socket = serverSocket.accept();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String message = bufferedReader.readLine();
        System.out.println(message);
        socket.close();
    }
}