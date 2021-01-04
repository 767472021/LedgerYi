package cn.ledgeryi.contract.utils;

import cn.ledgeryi.chainbase.core.capsule.AccountCapsule;
import cn.ledgeryi.common.core.exception.ContractValidateException;
import cn.ledgeryi.common.utils.ByteArray;
import cn.ledgeryi.common.utils.ByteUtil;
import cn.ledgeryi.common.utils.DecodeUtil;
import cn.ledgeryi.contract.vm.config.VmConfig;
import cn.ledgeryi.contract.vm.repository.Repository;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import static java.lang.String.format;

@Slf4j(topic = "VM")
public class VMUtils {
    private static final int BUF_SIZE = 4096;

    private VMUtils() {
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    private static File createProgramTraceFile(String txHash) {
        File result = null;
        if (VmConfig.vmTrace()) {
            File file = new File(new File("./", "vm_trace"), txHash + ".json");
            if (file.exists()) {
                if (file.isFile() && file.canWrite()) {
                    result = file;
                }
            } else {
                try {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                    result = file;
                } catch (IOException e) {
                    // ignored
                }
            }
        }
        return result;
    }

    private static void writeStringToFile(File file, String data) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            if (data != null) {
                out.write(data.getBytes("UTF-8"));
            }
        } catch (Exception e) {
            log.error(format("Cannot write to file '%s': ", file.getAbsolutePath()), e);
        } finally {
            closeQuietly(out);
        }
    }

    public static void saveProgramTraceFile(String txHash, String content) {
        File file = createProgramTraceFile(txHash);
        if (file != null) {
            writeStringToFile(file, content);
        }
    }

    private static void write(InputStream in, OutputStream out, int bufSize) throws IOException {
        try {
            byte[] buf = new byte[bufSize];
            for (int count = in.read(buf); count != -1; count = in.read(buf)) {
                out.write(buf, 0, count);
            }
        } finally {
            closeQuietly(in);
            closeQuietly(out);
        }
    }

    public static byte[] compress(byte[] bytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        DeflaterOutputStream out = new DeflaterOutputStream(baos, new Deflater(), BUF_SIZE);
        write(in, out, BUF_SIZE);
        return baos.toByteArray();
    }

    public static byte[] compress(String content) throws IOException {
        return compress(content.getBytes("UTF-8"));
    }

    public static byte[] decompress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        InflaterOutputStream out = new InflaterOutputStream(baos, new Inflater(), BUF_SIZE);
        write(in, out, BUF_SIZE);
        return baos.toByteArray();
    }

    public static boolean validateForSmartContract(Repository deposit, byte[] ownerAddress,
                                                   byte[] toAddress, long amount) throws ContractValidateException {
        if (deposit == null) {
            throw new ContractValidateException("No deposit!");
        }
        if (!DecodeUtil.addressValid(ownerAddress)) {
            throw new ContractValidateException("Invalid ownerAddress");
        }
        if (!DecodeUtil.addressValid(toAddress)) {
            throw new ContractValidateException("Invalid toAddress");
        }
        if (amount <= 0) {
            throw new ContractValidateException("Amount must greater than 0.");
        }
        if (Arrays.equals(ownerAddress, toAddress)) {
            throw new ContractValidateException("Cannot transfer asset to yourself.");
        }
        AccountCapsule ownerAccount = deposit.getAccount(ownerAddress);
        if (ownerAccount == null) {
            throw new ContractValidateException("No owner account!");
        }
        /*Map<String, Long> asset;
        if (deposit.getDynamicPropertiesStore().getAllowSameTokenName() == 0) {
            asset = ownerAccount.getAssetMap();
        } else {
            asset = ownerAccount.getAssetMapV2();
        }
        if (asset.isEmpty()) {
            throw new ContractValidateException("Owner no asset!");
        }*/

        /*Long assetBalance = asset.get(ByteArray.toStr(tokenIdWithoutLeadingZero));
        if (null == assetBalance || assetBalance <= 0) {
            throw new ContractValidateException("assetBalance must greater than 0.");
        }
        if (amount > assetBalance) {
            throw new ContractValidateException("assetBalance is not sufficient.");
        }*/

        /*AccountCapsule toAccount = deposit.getAccount(toAddress);
        if (toAccount != null) {
            if (deposit.getDynamicPropertiesStore().getAllowSameTokenName() == 0) {
                assetBalance = toAccount.getAssetMap().get(ByteArray.toStr(tokenIdWithoutLeadingZero));
            } else {
                assetBalance = toAccount.getAssetMapV2().get(ByteArray.toStr(tokenIdWithoutLeadingZero));
            }
            if (assetBalance != null) {
                try {
                    assetBalance = Math.addExact(assetBalance, amount); //check if overflow
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    throw new ContractValidateException(e.getMessage());
                }
            }
        } else {
            throw new ContractValidateException("Validate InternalTransfer error, no ToAccount. And not allowed to create account in smart contract.");
        }*/
        return true;
    }

    public static String align(String s, char fillChar, int targetLen, boolean alignRight) {
        if (targetLen <= s.length()) {
            return s;
        }
        String alignString = repeat("" + fillChar, targetLen - s.length());
        return alignRight ? alignString + s : s + alignString;
    }

    private static String repeat(String s, int n) {
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