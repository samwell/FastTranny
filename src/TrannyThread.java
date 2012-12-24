import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;


public class TrannyThread extends Thread
{
   private Socket socket;
   private DataOutputStream outToClient;
   private BufferedReader inFromClient;
   private PrintWriter logger;
   
   public TrannyThread(Socket socket)
   {
      try
      {
         System.out.println("Thread started");
         this.socket = socket;
         logger = new PrintWriter(new FileOutputStream("server.log", true), true);
         outToClient = new DataOutputStream(socket.getOutputStream());
         inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      }
      catch (FileNotFoundException e)
      {
         e.printStackTrace();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   public void run()
   {
      System.out.println("Thread running");
      log("CONNECTION: Opened");
      try
      {
         processRequest();
      }
      catch (Exception e)
      {
         log("Unknown error: " + e.getMessage());
      }
      log("CONNECTION: Closed");
   }
   
   private void processRequest() throws IOException
   {
      System.out.println("TrannyThread: " + inFromClient.readLine());
   }

   private void log(String message)
   {
      String date = new Date(new Date().getTime()).toString();
      String ipAddress = "IP: " + socket.getInetAddress();
      String port = "Port: " + socket.getPort() + "";

      logger.println(date + "   " + ipAddress + "   " + port + "   "
            + message);
   }
}
