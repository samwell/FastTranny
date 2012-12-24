import java.io.IOException;
import java.net.ServerSocket;


public class TrannyServer
{
   public static final int PORT = 5589;
   
   public static void main(String[] args)
   {
      ServerSocket socket;

      try
      {
         socket = new ServerSocket(5589);
         
         while(true)
            new TrannyThread(socket.accept()).start();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
}
