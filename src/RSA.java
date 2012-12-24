import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class RSA {
   private Cipher cipher;
   private KeyPairGenerator kpg;
   private KeyPair keyPair;
   private PublicKey pubKey;
   private PrivateKey privKey;
   private String fileName, encryptedFileName, decryptedFileName;
   
   public RSA(String fileName, String encodedFileName, String decodedFileName) throws NoSuchAlgorithmException,
         NoSuchProviderException, NoSuchPaddingException {
      BouncyCastleProvider provider = new BouncyCastleProvider();
      Security.addProvider(provider);
      
      cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", provider);
      kpg = KeyPairGenerator.getInstance("RSA", provider);
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
      kpg.initialize(2048, random);
      
      keyPair = kpg.generateKeyPair();
      
      this.fileName = fileName;
      this.encryptedFileName = encodedFileName;
      this.decryptedFileName = decodedFileName;
   }
   
   /**
    * Creates a public key and creates a file called pubKey which contains the
    * pubKey.
    * 
    * @throws IOException
    */
   public void createPubKey() throws IOException {
      pubKey = keyPair.getPublic();
      byte[] key = pubKey.getEncoded();
      FileOutputStream keyfos = new FileOutputStream("pubKey");
      keyfos.write(key);
      keyfos.close();
   }
   
   /**
    * Creates a private key and creates a file called privKey which contains the
    * privKey.
    * 
    * @throws IOException
    */
   public void createprivKey() throws IOException {
      privKey = keyPair.getPrivate();
      byte[] key = privKey.getEncoded();
      FileOutputStream keyfos = new FileOutputStream("privKey");
      keyfos.write(key);
      keyfos.close();
   }
   
   /**
    * Creates a public key from the file pubKey which contains the public key.
    * 
    * @throws NoSuchAlgorithmException
    * @throws IOException
    * @throws InvalidKeySpecException
    */
   public void getPubKey() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      
      FileInputStream keyfis = new FileInputStream("pubKey");
      byte[] encKey = new byte[keyfis.available()];
      keyfis.read(encKey);
      keyfis.close();
      
      X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
      pubKey = keyFactory.generatePublic(pubKeySpec);
   }
   
   /**
    * Creates a private key from the file privKey which contains the private
    * key.
    * 
    * @throws NoSuchAlgorithmException
    * @throws IOException
    * @throws InvalidKeySpecException
    */
   public void getPrivKey() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      
      FileInputStream keyfis = new FileInputStream("privKey");
      byte[] encKey = new byte[keyfis.available()];
      keyfis.read(encKey);
      keyfis.close();
      
      PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encKey);
      privKey = keyFactory.generatePrivate(privKeySpec);
   }
   
   /**
    * Encrypts the file, fileName, and produces an encrypted file,
    * encryptedFileName. fileName and encryptedFileName only contain names of
    * the file.
    * 
    * @throws InvalidKeyException
    * @throws IOException
    */
   public void encrypt() throws InvalidKeyException, IOException {
      cipher.init(Cipher.ENCRYPT_MODE, pubKey);
      
      FileInputStream fis = new FileInputStream(fileName);
      InputStream reader = new BufferedInputStream(fis);
      FileOutputStream fos = new FileOutputStream(encryptedFileName);
      CipherOutputStream cos = new CipherOutputStream(fos, cipher);
      
      byte[] block = new byte[fis.available()];
      int i;
      while ((i = reader.read(block)) != -1) {
         cos.write(block, 0, i);
      }
      
      cos.close();
      fos.close();
      reader.close();
      fis.close();
   }
   
   /**
    * Decrypts the file, encryptedFileName, and produces a decrypted file,
    * decryptedFileName. encryptedFileName and decryptedFileName only contain
    * the file name of the file.
    * 
    * @throws IOException
    * @throws InvalidKeyException
    */
   public void decrypt() throws IOException, InvalidKeyException {
      cipher.init(Cipher.DECRYPT_MODE, privKey);
      
      FileInputStream fis = new FileInputStream(encryptedFileName);
      FileOutputStream fos = new FileOutputStream(decryptedFileName);
      CipherInputStream cis = new CipherInputStream(fis, cipher);
      
      byte[] block = new byte[fis.available()];
      int i;
      while ((i = cis.read(block)) != -1) {
         fos.write(block, 0, i);
      }
      
      cis.close();
      fos.close();
      fis.close();
   }
   
   public static void main(String[] args) {
      RSA file;
      
      try {
         file = new RSA("message.txt", "encoded", "decoded.txt");
         file.createprivKey();
         file.createPubKey();
         file.encrypt();
         file.decrypt();
      } catch (NoSuchAlgorithmException e) {
         e.printStackTrace();
      } catch (NoSuchProviderException e) {
         e.printStackTrace();
      } catch (NoSuchPaddingException e) {
         e.printStackTrace();
      } catch (InvalidKeyException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}