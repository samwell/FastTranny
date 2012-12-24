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

public class TrannyFile {
   private String fileName;
   private byte[] iv;
   private SecretKey secret;
   
   public TrannyFile() throws NoSuchAlgorithmException {
      KeyGenerator keyGen = KeyGenerator.getInstance("AES", new BouncyCastleProvider());
      keyGen.init(128);
      
      secret = keyGen.generateKey();
   }
   
   public TrannyFile(String fileName) throws NoSuchAlgorithmException {
      this();
      this.fileName = fileName;
   }
   
   public String getSecret() {
      return new String(Base64.encode(secret.getEncoded()));
   }
   
   public void setSecret(String secret) {
      this.secret = new SecretKeySpec(Base64.decode(secret), "AES");
   }
   
   public String getIV() {
      return new String(Base64.encode(iv));
   }
   
   public void setIV(String iv) {
      this.iv = Base64.decode(iv);
   }
   
   public void encrypt() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
         InvalidParameterSpecException, IOException, IllegalBlockSizeException, BadPaddingException {
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, secret);
      AlgorithmParameters params = cipher.getParameters();
      iv = params.getParameterSpec(IvParameterSpec.class).getIV();
      
      FileInputStream inFile = new FileInputStream(fileName);
      FileOutputStream outFile = new FileOutputStream(fileName + ".aes");
      
      byte[] input = new byte[128];
      int read;
      
      while ((read = inFile.read(input)) != -1) {
         byte[] output = cipher.update(input, 0, read);
         
         if (output != null)
            outFile.write(output);
      }
      
      byte[] output = cipher.doFinal();
      if (output != null)
         outFile.write(output);
      
      inFile.close();
      outFile.flush();
      outFile.close();
   }
   
   public void decrypt() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
         InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException {
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
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
   }
   
   public static void main(String[] args) {
      try {
         TrannyFile crypte = new TrannyFile("message.txt");
         TrannyFile cryptd = new TrannyFile("message.txt");
         
         crypte.encrypt();
         System.out.println(new MD5Checksum("message.txt"));
         
         String secret = crypte.getSecret();
         String iv = crypte.getIV();
         
         cryptd.setSecret(secret);
         cryptd.setIV(iv);
         
         cryptd.decrypt();
         System.out.println(new MD5Checksum("message.txt.aes"));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
