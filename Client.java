import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    private volatile boolean running = true;

    public static void main(String[] args){
        new Client().run();
    }

    private void run(){
        Socket socket = null;
        //String serverHostName1 = "RAZER-BLADE-15";
        String serverHostName2 = "MSI-TRIDENT-X-PLUS";

        OutputStream os = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;

        try {
            socket = new Socket(InetAddress.getByName(serverHostName2), 8888);

            // a sub thread to receive messages from server
            Thread thread = new Thread(new ReceiveMessageFromServer(socket));
            thread.start();

            // main thread to post messages to server
            os = socket.getOutputStream();
            osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            bw = new BufferedWriter(osw);

            Scanner scanner = new Scanner(System.in);
            while(true){
                String message = scanner.nextLine();
                bw.write(message);
                bw.newLine();
                bw.flush();
                if("bye".equalsIgnoreCase(message)){
                    this.running = false;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            MyIO.close(bw);
            MyIO.close(osw);
            MyIO.close(os);
            MyIO.close(socket);
        }
    }

    private class ReceiveMessageFromServer implements Runnable{
        private final Socket socket;

        public ReceiveMessageFromServer(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("a thread ReceiveMessageFromServer is running...");
            InputStream is = null;
            InputStreamReader isr = null;
            BufferedReader br = null;

            try {
                is = this.socket.getInputStream();
                isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                br = new BufferedReader(isr);

                while(running){
                    String message = br.readLine();
                    System.out.println(message);
                }
            } catch (IOException e) {
                 e.printStackTrace();
            } finally {
                MyIO.close(br);
                MyIO.close(isr);
                MyIO.close(is);
                System.out.println("The thread ReceiveMessageFromServer is closed.");
            }
        }
    }
}
