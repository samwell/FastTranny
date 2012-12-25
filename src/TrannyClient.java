import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class TrannyClient {
   private Socket socket;
   private BufferedReader in;
   private PrintWriter out;
   
   public TrannyClient() throws Exception {
      System.out.println("CLIENT: starting");
   }
   
   private void connect() throws Exception {
      System.out.println("CLIENT: connecting");
      socket = new Socket("localhost", App.PORT);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream(), true);
   }
   
   private void close() throws Exception {
      System.out.println("CLIENT: disconnection");
      out.close();
      in.close();
      socket.close();
   }
   
   public void sendMessage(String msg, boolean connect) throws Exception {
      System.out.println("CLIENT: sending " + msg);
      
      if(connect)
         connect();
      
      out.println(msg);
      
      System.out.println("CLIENT: message sent");
   }
   
   public void receive() throws Exception {
      System.out.println("CLIENT: waiting for response");
      
      String line = in.readLine();
      while(line != null)
      {
         System.out.println("CLIENT rsp: " + line);
         line = in.readLine();
      }
      
      System.out.println("CLIENT: done receiving");
      
      close();
   }
   
   public static void main(String args[]) throws Exception {
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      TrannyClient client = new TrannyClient();
      
      String input = reader.readLine();
      
      while(input != null) {
         client.sendMessage(input, true);
         
         if(input.equals(App.OPENDIR))
            client.sendMessage(reader.readLine(), false);
         else if (input.equals(App.QUIT))
            System.exit(0);
         else if(input.equals(App.GET))
            client.sendMessage(reader.readLine(), false);
         
         client.receive();
         
         input = reader.readLine();
      }
   }
}