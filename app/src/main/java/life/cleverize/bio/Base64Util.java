package life.cleverize.bio;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

public class Base64Util {
    /**
     * Encode txt
     * @param txt
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String encode(String txt) throws UnsupportedEncodingException {
        byte[] data = txt.getBytes("UTF-8");
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    /**
     * Decode txt
     * @param txt
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String decode(String txt) throws UnsupportedEncodingException {
        return new String(Base64.decode(txt, Base64.DEFAULT), "UTF-8");
    }
}