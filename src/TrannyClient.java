import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
      
      if (connect)
         connect();
      
      out.println(msg);
      
      System.out.println("CLIENT: message sent");
   }
   
   public String receive(boolean close) throws Exception {
      String rcv = "";
      System.out.println("CLIENT: waiting for response");
      
      String line = in.readLine();
      while (line != null && !line.equals(App.ENDTRANSMISSION)) {
         rcv += line + " ";
         System.out.println("CLIENT rsp: " + line);
         line = in.readLine();
      }
      
      System.out.println("CLIENT: done receiving");
      
      if (close)
         close();
      
      return rcv.trim();
   }
   
   public void get(String fileName) throws Exception {
      sendMessage(fileName, false);
      
      System.out.println("CLIENT: about to receive");
      String rsp = receive(false);
      System.out.println("CLIENT rsp to get: " + rsp);
      
      if (!rsp.equals(App.OK)) {
         System.out.println("CLIENT: did not get ok");
         close();
         return;
      }
      
      sendMessage(App.OK, false);
      
      System.out.println("CLIENT: receiving file " + fileName);
      
      if (!in.readLine().equals(App.SENDING)) {
         System.out.println("CLIENT: error in sending header and file");
         close();
         return;
      }
      
      System.out.println("CLIENT: getting header");
      
      TrannyFile file = new TrannyFile();
      
      String newFileName = in.readLine();
      System.out.println("FileName: " + newFileName);
      
      if (!in.readLine().equals(App.ENDTRANSMISSION)) {
         System.out.println("CLIENT: error getting file name");
         close();
         return;
      }

      String newSecret = in.readLine();
      System.out.println("secret: " + newSecret);
      
      if (!in.readLine().equals(App.ENDTRANSMISSION)) {
         System.out.println("CLIENT: error getting file name");
         close();
         return;
      }

      String newMD5checksum = in.readLine();
      System.out.println("MD5checksum: " + newMD5checksum);
      
      if (!in.readLine().equals(App.ENDTRANSMISSION)) {
         System.out.println("CLIENT: error getting MD5checksum");
         close();
         return;
      }
      
       file.setFileName(newFileName);
       file.setSecret(newSecret);
       
       System.out.println("fileName: " + file.getFileName());
       System.out.println("secret: " + file.getSecret());
   }
   
   public static void main(String args[]) throws Exception {
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      TrannyClient client = new TrannyClient();
      
      String input = reader.readLine();
      
      while (input != null) {
         client.sendMessage(input, true);
         
         if (input.equals(App.OPENDIR))
            client.sendMessage(reader.readLine(), false);
         else if (input.equals(App.QUIT))
            System.exit(0);
         else if (input.equals(App.GET)) {
            client.get(reader.readLine());
            input = reader.readLine();
            continue;
         }
         
         client.receive(true);
         
         input = reader.readLine();
      }
   }
}