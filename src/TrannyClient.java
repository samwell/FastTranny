import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class TrannyClient
{   
   public static void main(String args[])
   {
      Socket connectionToServer;
      BufferedReader inFromServer;
      PrintWriter outToServer;
      
      try
      {
         connectionToServer = new Socket("localhost", App.PORT);
         InputStream socketInputStream = connectionToServer.getInputStream();
         inFromServer = new BufferedReader(new InputStreamReader(socketInputStream));
         outToServer = new PrintWriter(connectionToServer.getOutputStream(), true);
         
         outToServer.println("GET HTTP/1.1");
         outToServer.println("Host: ");
         outToServer.println();
         
         connectionToServer.close();
      }
      catch (ConnectException ex)
      {
         ex.printStackTrace();
      }
      catch (UnknownHostException ex)
      {
         ex.printStackTrace();
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }
}