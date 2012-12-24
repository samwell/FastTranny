/**
 * Used Real's How To's MD5 checksum example to create this. 
 * It was modified from being a static based class to a more object oriented class. 
 * 
 * Link: http://www.rgagnon.com/javadetails/java-0416.html
 */

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class MD5Checksum {
   private String fileName;
   
   public MD5Checksum(String fileName) {
      this.fileName = fileName;
   }
   
   private byte[] createChecksum() throws Exception {
      InputStream fis = new FileInputStream(fileName);
      
      byte[] buffer = new byte[1024];
      MessageDigest complete = MessageDigest.getInstance("MD5");
      int numRead;
      do {
         numRead = fis.read(buffer);
         if (numRead > 0) {
            complete.update(buffer, 0, numRead);
         }
      } while (numRead != -1);
      fis.close();
      return complete.digest();
   }
   
   public String getMD5Checksum() throws Exception {
      byte[] b = createChecksum();
      String result = "";
      for (int i = 0; i < b.length; i++) {
         result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
      }
      return result;
   }
   
   @Override
   public String toString() {
      try {
         return getMD5Checksum();
      } catch (Exception e) {
         e.printStackTrace();
      }
      
      return null;
   }
}