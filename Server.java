import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Server {
    private static final List<Socket> sockets = Collections.synchronizedList(new ArrayList<>());

    private static synchronized boolean containSocket(Socket s){
        for(Socket socket : sockets) {
            if(socket.getRemoteSocketAddress().toString().equals(s.getRemoteSocketAddress().toString())){
                return true;
            }
        }
        return false;
    }
    private static synchronized void connectSocket(Socket socket){
        Server.sockets.add(socket);
        String serverMessage = "[server]: " + socket.getRemoteSocketAddress() + " is connected.";
        Server.postMessage(serverMessage);
        System.out.println(serverMessage);
    }
    private static synchronized void disconnectSocket(Socket socket){
        Server.sockets.remove(socket);
        String serverMessage = "[server]: " + socket.getRemoteSocketAddress() + " is disconnected.";
        Server.postMessage(serverMessage);
        System.out.println(serverMessage);
    }
    private static synchronized void postMessage(String message){
        if(message.isEmpty()){
            System.out.println("[server]: warning - empty message.");
            return;
        }
        for(Socket socket : Server.sockets){
            try {
                OutputStream os = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                BufferedWriter bw = new BufferedWriter(osw);
                bw.write(message);
                bw.newLine();
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static synchronized void handleMessageFromSocket(Socket sender, String message){
        Server.postMessage("[" + sender.getRemoteSocketAddress() + "]: " + message);
        if("bye".equalsIgnoreCase(message)){
            Server.disconnectSocket(sender);
        }
    }

    public static void main(String[] args) {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(8888);
            System.out.println("[server]: the server is running...");

            while(true) {
                Socket socket = ss.accept();
                Server.connectSocket(socket);
                new Thread(new SocketThread(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("[server]: the server is closed.");
            MyIO.close(ss);
        }
    }

    private static class SocketThread implements Runnable{
        private final Socket socket;

        public SocketThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            InputStream is = null;
            InputStreamReader isr = null;
            BufferedReader br = null;

            try {
                // 1 create flow object
                is = this.socket.getInputStream();
                isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                br = new BufferedReader(isr);
                // 2 read
                while(Server.containSocket(this.socket)){
                    System.out.println("br = " + br);
                    String message = br.readLine();
                    if(message == null){
                        break;
                    }
                    Server.handleMessageFromSocket(this.socket, message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // 3 close resources
                MyIO.close(br);
                MyIO.close(isr);
                MyIO.close(is);
                MyIO.close(this.socket);
            }
        }
    }
}
