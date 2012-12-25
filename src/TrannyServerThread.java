import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class TrannyServerThread extends Thread {
   private Socket socket;
   private DataOutputStream out;
   private BufferedReader in;
   private PrintWriter logger;
   private String workingDir;
   
   public TrannyServerThread(Socket socket) {
      try {
         System.out.println("THREAD: started");
         this.socket = socket;
         logger = new PrintWriter(new FileOutputStream("server.log", true), true);
         out = new DataOutputStream(socket.getOutputStream());
         in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
         out.close();
         in.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
      
      System.out.println("THREAD: stopped");
   }
   
   private void processCMD() throws IOException, InterruptedException, NoSuchAlgorithmException, NoSuchPaddingException {
      String cmd = in.readLine().trim();
      
      System.out.println("THREAD: received " + cmd);
      
      if (cmd.equals(App.LIST))
         list();
      else if (cmd.equals(App.OPENDIR))
         opendir();
      else if (cmd.equals(App.GET))
         get();
      else if (cmd.equals(App.QUIT))
         sendMessage(App.QUIT);
      else
         sendMessage(App.UNKNOWN);
   }
   
   private void list() throws IOException, InterruptedException {
      System.out.println("THREAD: listing");
      File[] list = new File(workingDir).listFiles();
      
      for (int i = 0; i < list.length; i++)
         out.writeBytes((list[i].isDirectory() ? "D " : "F ") + list[i].getName() + App.CRLF);
      
      out.writeBytes(App.ENDTRANSMISSION);
      
      out.flush();
   }
   
   private void opendir() throws IOException, InterruptedException {
      System.out.println("THREAD: opening directory");
      
      System.out.println("THREAD: waiting for directory");
      workingDir += "\\" + in.readLine().trim();
      System.out.println("THREAD: selected " + workingDir);
      
      if (new File(workingDir).isDirectory())
         list();
      else
         sendMessage(App.NODIR);
   }
   
   private void get() throws IOException, InterruptedException, NoSuchAlgorithmException, NoSuchPaddingException {
      System.out.println("THREAD: sending file");
      
      System.out.println("THREAD: waiting for file");
      workingDir += "\\" + in.readLine().trim();
      System.out.println("THREAD: selected " + workingDir);
      
      if (!new File(workingDir).isFile()) {
         sendMessage(App.NOFILE);
         return;
      }
      
      sendMessage(App.OK);
      
      System.out.println("THREAD: waiting for ok");
      if (!in.readLine().trim().equals(App.OK)) {
         sendMessage(App.UNKNOWN);
         return;
      }
      
      System.out.println("THREAD: got ok");
      
      System.out.println("THREAD: start sending file");
      out.writeBytes(App.SENDING + App.CRLF);
      
      System.out.println("THREAD: created transfer file");
      TrannyFile file = new TrannyFile(workingDir, out);
      
      sendMessage(file.getFileName());
      sendMessage(file.getSecret());
      sendMessage(file.getMD5checksum());
      
      System.out.println("THREAD: waiting for ok");
      if (!in.readLine().trim().equals(App.OK)) {
         sendMessage(App.UNKNOWN);
         return;
      }
      
      try {
         file.encrypt();
      } catch (Exception e) {
         System.out.println("THREAD: error encrypting file");
         out.flush();
         e.printStackTrace();
         return;
      }
      
      System.out.println("THREAD: finished sending file");
      
      sendMessage(App.ENDTRANSMISSION);
      
      out.flush();
   }
   
   private void sendMessage(String msg) throws IOException, InterruptedException {
      System.out.println("THREAD: sending " + msg);
      
      out.writeBytes(msg + App.CRLF);
      
      if (!msg.equals(App.ENDTRANSMISSION))
         out.writeBytes(App.ENDTRANSMISSION + App.CRLF);
      
      out.flush();
   }
   
   private void log(String message) {
      String date = new Date(new Date().getTime()).toString();
      String ipAddress = "IP: " + socket.getInetAddress();
      String port = "Port: " + socket.getPort() + "";
      
      logger.println(date + "   " + ipAddress + "   " + port + "   " + message);
   }
}
