package cn.ledgeryi.common.core.db;

import org.spongycastle.util.encoders.Hex;
import cn.ledgeryi.common.utils.FastByteComparisons;

import java.io.Serializable;
import java.util.Arrays;


public class ByteArrayWrapper implements Comparable<ByteArrayWrapper>, Serializable {

  private static final long serialVersionUID = -8645797230368480951L;

  private final byte[] data;
  private int hashCode = 0;

  /**
   * constructor.
   */
  public ByteArrayWrapper(byte[] data) {
    if (data == null) {
      throw new IllegalArgumentException("Data must not be null");
    }
    this.data = data;
    this.hashCode = Arrays.hashCode(data);
  }


  /**
   * equals Objects.
   */
  public boolean equals(Object other) {
    if (other == null || this.getClass() != other.getClass()) {
      return false;
    }
    byte[] otherData = ((ByteArrayWrapper) other).getData();
    return FastByteComparisons.compareTo(
        data, 0, data.length,
        otherData, 0, otherData.length) == 0;
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  @Override
  public int compareTo(ByteArrayWrapper o) {
    return FastByteComparisons.compareTo(
        data, 0, data.length,
        o.getData(), 0, o.getData().length);
  }

  public byte[] getData() {
    return data;
  }

  @Override
  public String toString() {
    return Hex.toHexString(data);
  }
}
