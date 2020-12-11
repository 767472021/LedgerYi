package cn.ledgeryi.framework.common.utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Arrays;

public interface Utils {

  SecureRandom random = new SecureRandom();

  static SecureRandom getRandom() {
    return random;
  }

  static byte[] getBytes(char[] chars) {
    Charset cs = Charset.forName("UTF-8");
    CharBuffer cb = CharBuffer.allocate(chars.length);
    cb.put(chars);
    cb.flip();
    ByteBuffer bb = cs.encode(cb);

    return bb.array();
  }

  static String getIdShort(String Id) {
    return Id == null ? "<null>" : Id.substring(0, 8);
  }

  static char[] getChars(byte[] bytes) {
    Charset cs = Charset.forName("UTF-8");
    ByteBuffer bb = ByteBuffer.allocate(bytes.length);
    bb.put(bytes);
    bb.flip();
    CharBuffer cb = cs.decode(bb);

    return cb.array();
  }

  static byte[] clone(byte[] value) {
    byte[] clone = new byte[value.length];
    System.arraycopy(value, 0, clone, 0, value.length);
    return clone;
  }

  static String sizeToStr(long size) {
    if (size < 2 * (1L << 10)) {
      return size + "b";
    }
    if (size < 2 * (1L << 20)) {
      return String.format("%dKb", size / (1L << 10));
    }
    if (size < 2 * (1L << 30)) {
      return String.format("%dMb", size / (1L << 20));
    }
    return String.format("%dGb", size / (1L << 30));
  }

  static String align(String s, char fillChar, int targetLen, boolean alignRight) {
    if (targetLen <= s.length()) {
      return s;
    }
    String alignString = repeat("" + fillChar, targetLen - s.length());
    return alignRight ? alignString + s : s + alignString;

  }

  static String repeat(String s, int n) {
    if (s.length() == 1) {
      byte[] bb = new byte[n];
      Arrays.fill(bb, s.getBytes()[0]);
      return new String(bb);
    } else {
      StringBuilder ret = new StringBuilder();
      for (int i = 0; i < n; i++) {
        ret.append(s);
      }
      return ret.toString();
    }
  }
}