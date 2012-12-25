import java.io.IOException;
import java.net.ServerSocket;

public class TrannyServer {
   
   public static void main(String[] args) {
      System.out.println("SERVER: starting");
      ServerSocket socket;
      
      try {
         socket = new ServerSocket(App.PORT);
         System.out.println("SERVER: listening to port " + App.PORT);
         
         while (true) {
            System.out.println("SERVER: waiting for connection");
            new TrannyServerThread(socket.accept()).start();
         }
         
      } catch (IOException e) {
         e.printStackTrace();
      }
      
      System.out.println("SERVER: stopping");
   }
}
