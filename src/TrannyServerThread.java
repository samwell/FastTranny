import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

public class TrannyServerThread extends Thread {
   private Socket socket;
   private DataOutputStream outToClient;
   private BufferedReader inFromClient;
   private PrintWriter logger;
   private String workingDir;
   
   public TrannyServerThread(Socket socket) {
      try {
         System.out.println("THREAD: started");
         this.socket = socket;
         logger = new PrintWriter(new FileOutputStream("server.log", true), true);
         outToClient = new DataOutputStream(socket.getOutputStream());
         inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         workingDir = System.getProperty("user.dir");
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   
   public void run() {
      System.out.println("THREAD: running");
      log("CONNECTION: Opened");
      try {
         System.out.println("THREAD: Waiting");
         processCMD();
         System.out.println("THREAD: Finished");
      } catch (Exception e) {
         log("Unknown error: " + e.getMessage());
      }
      log("CONNECTION: Closed");
      
      try {
         outToClient.close();
         inFromClient.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
      
      System.out.println("THREAD: stopped");
   }
   
   private void processCMD() throws IOException, InterruptedException {
      String cmd = inFromClient.readLine().trim();
      
      System.out.println("THREAD: received " + cmd);
      
      if(cmd.equals(App.LIST))
         list();
      else if(cmd.equals(App.OPENDIR))
         opendir();
      else if(cmd.equals(App.GET))
         get();
      else if(cmd.equals(App.QUIT))
         sendMessage(App.QUIT);
      else
         sendMessage(App.UNKNOWN);
   }
   
   private void list() throws IOException, InterruptedException {
      System.out.println("THREAD: listing");
      File[] list = new File(workingDir).listFiles();
      
      for(int i = 0; i < list.length; i++)
         outToClient.writeBytes((list[i].isDirectory() ? "D " : "F ") + list[i].getName() + App.CRLF);
      
      outToClient.writeBytes(App.ENDTRANSMISSION);
      
      outToClient.flush();
   }

   private void opendir() throws IOException, InterruptedException {
      System.out.println("THREAD: opening directory");
      
      System.out.println("THREAD: waiting for directory");
      workingDir += "\\" + inFromClient.readLine().trim();
      System.out.println("THREAD: selected " + workingDir);
      
      if(new File(workingDir).isDirectory())
         list();
      else
         sendMessage(App.NODIR);
   }

   private void get() throws IOException, InterruptedException {
      System.out.println("THREAD: sending file");
      
      System.out.println("THREAD: waiting for file");
      workingDir += "\\" + inFromClient.readLine().trim();
      System.out.println("THREAD: selected " + workingDir);
      
      if(new File(workingDir).isFile())
         list();
      else
         sendMessage(App.NOFILE);
   }
   
   private void sendMessage(String msg) throws IOException, InterruptedException {
      System.out.println("THREAD: sending " + msg);
      
      outToClient.writeBytes(msg + App.CRLF);
      outToClient.writeBytes(App.ENDTRANSMISSION + App.CRLF);
      
      outToClient.flush();
   }

   private void log(String message) {
      String date = new Date(new Date().getTime()).toString();
      String ipAddress = "IP: " + socket.getInetAddress();
      String port = "Port: " + socket.getPort() + "";
      
      logger.println(date + "   " + ipAddress + "   " + port + "   " + message);
   }
}
