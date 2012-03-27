package playn.android;

import playn.core.PlayN;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author shmyga.z@gmail.com <shmyga>
 *         Date: 15.03.12
 */
final public class Util {

  public static String md5(final String s) {
    try {
      // Create MD5 Hash
      MessageDigest digest = MessageDigest.getInstance("MD5");
      digest.update(s.getBytes());
      byte messageDigest[] = digest.digest();

      // Create Hex String
      StringBuffer hexString = new StringBuffer();
      for (int i = 0; i < messageDigest.length; i++) {
        String h = Integer.toHexString(0xFF & messageDigest[i]);
        while (h.length() < 2)
          h = "0" + h;
        hexString.append(h);
      }
      return hexString.toString();

    } catch (NoSuchAlgorithmException e) {
      PlayN.log().error("", e);
    }
    return "";
  }

  public static void copyStream(InputStream is, OutputStream os) throws IOException {
    try {
      try {
        byte[] buffer = new byte[16 * 1024];
        while (true) {
          int r = is.read(buffer);
          if (r < 0)
            break;
          os.write(buffer, 0, r);
        }
      } finally {
        os.close();
        is.close();
      }
    } finally {
      os.close();
      is.close();
    }
  }
}
