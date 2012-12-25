import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class TrannyFile {
   private DataOutputStream out;
   private String fileName;
   private SecretKey secret;
   private MD5Checksum MD5checksum;
   private byte[] iv;
   private Cipher cipher;
   
   public TrannyFile() throws NoSuchAlgorithmException, NoSuchPaddingException {
      KeyGenerator keyGen = KeyGenerator.getInstance("AES", new BouncyCastleProvider());
      keyGen.init(128);
      
      cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      
      secret = keyGen.generateKey();
   }
   
   public TrannyFile(String fileName) throws NoSuchAlgorithmException, NoSuchPaddingException {
      this();
      this.fileName = fileName;
      this.MD5checksum = new MD5Checksum(fileName);
   }
   
   public TrannyFile(String fileName, DataOutputStream out) throws NoSuchAlgorithmException, NoSuchPaddingException {
      this(fileName);
      this.out = out;
   }
   
   public String getMD5checksum() {
      try {
         return MD5checksum.getMD5Checksum();
      } catch (Exception e) {
         e.printStackTrace();
      }
      
      return null;
   }
   
   @SuppressWarnings("unchecked")
   public String getFileName()
   {
      JSONObject json = new JSONObject();
      json.put("fileName", new File(fileName).getName());
      
      return json.toJSONString();
   }
   
   public void setFileName(String fileName) {
      JSONObject json = (JSONObject) JSONValue.parse(fileName);
      this.fileName = (String)json.get("fileName");
   }
   
   @SuppressWarnings("unchecked")
   public String getSecret() {
      JSONObject json = new JSONObject();
      json.put("secret", new String(Base64.encode(secret.getEncoded())));
      
      return json.toJSONString();
   }
   
   public void setSecret(String secret) {
      JSONObject json = (JSONObject) JSONValue.parse(secret);
      this.secret = new SecretKeySpec(Base64.decode((String)json.get("secret")), "AES");
   }
   
   @SuppressWarnings("unchecked")
   public String getIV() {
      JSONObject json = new JSONObject();
      json.put("iv", new String(Base64.encode(iv)));
      
      return json.toJSONString();
   }
   
   public void setIV(String iv) {
      JSONObject json = (JSONObject) JSONValue.parse(iv);
      this.iv = Base64.decode((String)json.get("iv"));
   }
   
   public void encrypt() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
         InvalidParameterSpecException, IOException, IllegalBlockSizeException, BadPaddingException {
      
      System.out.println("CRYPT: encrypting file");
      
      cipher.init(Cipher.ENCRYPT_MODE, secret);
      AlgorithmParameters params = cipher.getParameters();
      
      System.out.println("CRYPT: created iv");
      iv = params.getParameterSpec(IvParameterSpec.class).getIV();
      
      System.out.println("CRYPT: sending iv");
      System.out.println("CRYPT: sending " + getIV());
      out.writeBytes(getIV() + App.CRLF);
      out.writeBytes(App.ENDTRANSMISSION + App.CRLF);
      System.out.println("CRYPT: sent iv");
      
      
      FileInputStream inFile = new FileInputStream(fileName);
      
      byte[] input = new byte[App.MAX_BUFFER];
      int read;
      String encoded;
      
      System.out.println("CRYPT: encrypting file");
      while ((read = inFile.read(input)) != -1) {
         byte[] output = cipher.update(input, 0, read);
         
         if (output != null) {
            encoded = new String(Base64.encode(output));
            System.out.println("CRYPT: sending file - " + encoded);
            out.writeBytes(encoded + App.CRLF);
         }
      }
      
      byte[] output = cipher.doFinal();
      if (output != null) {
         encoded = new String(Base64.encode(output));
         System.out.println("CRYPT: sending file - " + encoded);
         out.writeBytes(encoded + App.CRLF);
      }
      
      System.out.println("CRYPT: finished sending file");
      
      inFile.close();
      out.flush();
   }
   
   public String decrypt() throws Exception {

      System.out.println("CRYPT: decrypting file");
      
      cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
      
      FileInputStream inFile = new FileInputStream(fileName + ".aes");
      FileOutputStream outFile = new FileOutputStream(fileName + ".dcr");
      
      byte[] input = new byte[128];
      int bytesRead;
      
      while ((bytesRead = inFile.read(input)) != -1) {
         byte[] output = cipher.update(input, 0, bytesRead);
         
         if (output != null)
            outFile.write(output);
      }
      
      byte[] output = cipher.doFinal();
      
      if (output != null)
         outFile.write(output);
      
      inFile.close();
      outFile.flush();
      outFile.close();
      
      System.out.println("CRYPT: finished decrypting");
      
      if(new File(fileName + ".aes").delete())
         System.out.println("CRYPT: encrypted file was deleted - " + fileName + ".aes");
      else
         System.out.println("CRYPT: couldn't delete encrypted file - " + fileName + ".aes");
      
      return new MD5Checksum(fileName + ".dcr").getMD5Checksum();
   }
   
   public static void main(String[] args) {
      try {
         TrannyFile crypte = new TrannyFile("message.txt");
         TrannyFile cryptd = new TrannyFile("message.txt");
         
         crypte.encrypt();
         System.out.println(new MD5Checksum("message.txt"));
         
         String secret = crypte.getSecret();
         String iv = crypte.getIV();
         
         System.out.println("SECRET: " + secret);
         
         cryptd.setSecret(secret);
         cryptd.setIV(iv);
         
         cryptd.decrypt();
         System.out.println(new MD5Checksum("message.txt.aes"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
