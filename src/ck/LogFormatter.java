package ck;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import org.apache.jute.BinaryInputArchive;
import org.apache.zookeeper.server.persistence.FileHeader;
import org.apache.zookeeper.server.persistence.FileTxnLog;
import org.apache.zookeeper.server.util.SerializeUtils;
import org.apache.zookeeper.txn.TxnHeader;

/**
 * @author xuer
 * @date 2014-10-29 - 上午10:58:19
 * @Description 查看zookeeper日志，直接输入命令：java -jar LogFormatter.jar log.100000001
 */
public class LogFormatter {

  public LogFormatter() {}

  public static void main(String args[]) throws Exception {
    if (args.length != 1) {
      System.err.println("USAGE: LogFormatter log_file");
      System.exit(2);
    }
    FileInputStream fis = new FileInputStream(args[0]);
    BinaryInputArchive logStream = BinaryInputArchive.getArchive(fis);
    FileHeader fhdr = new FileHeader();
    fhdr.deserialize(logStream, "fileheader");
    if (fhdr.getMagic() != FileTxnLog.TXNLOG_MAGIC) {
      System.err.println((new StringBuilder()).append("Invalid magic number for ").append(args[0])
          .toString());
      System.exit(2);
    }
    System.out.println((new StringBuilder()).append("ZooKeeper Transactional Log File with dbid ")
        .append(fhdr.getDbid()).append(" txnlog format version ").append(fhdr.getVersion())
        .toString());
    int count = 0;
    do {
      long crcValue;
      byte bytes[];
      try {
        crcValue = logStream.readLong("crcvalue");
        bytes = logStream.readBuffer("txnEntry");
      } catch (EOFException e) {
        System.out.println((new StringBuilder()).append("EOF reached after ").append(count)
            .append(" txns.").toString());
        return;
      }
      if (bytes.length == 0) {
        System.out.println((new StringBuilder()).append("EOF reached after ").append(count)
            .append(" txns.").toString());
        return;
      }
      Checksum crc = new Adler32();
      crc.update(bytes, 0, bytes.length);
      if (crcValue != crc.getValue())
        throw new IOException((new StringBuilder()).append("CRC doesn't match ").append(crcValue)
            .append(" vs ").append(crc.getValue()).toString());
      org.apache.jute.InputArchive iab =
          BinaryInputArchive.getArchive(new ByteArrayInputStream(bytes));
      TxnHeader hdr = new TxnHeader();
      SerializeUtils.deserializeTxn(iab, hdr);
      System.out.println((new StringBuilder())
          .append(DateFormat.getDateTimeInstance(3, 1).format(new Date(hdr.getTime())))
          .append(" session 0x").append(Long.toHexString(hdr.getClientId())).append(" cxid 0x")
          .append(Long.toHexString(hdr.getCxid())).append(" zxid 0x")
          .append(Long.toHexString(hdr.getZxid())).append(" ")
          .append(ck.TraceFormatter.op2String(hdr.getType())));
      if (logStream.readByte("EOR") != 66) {
        // LOG.error("Last transaction was partial.");
        throw new EOFException("Last transaction was partial.");
      }
      count++;
    } while (true);
  }
  // private static final Logger LOG = Logger.getLogger(ck / LogFormatter);

}


/*
 * DECOMPILATION REPORT
 * 
 * Decompiled from: D:\Workspaces\storm_project_new\lib\zookeeper-3.3.3.jar Total time: 408 ms Jad
 * reported messages/errors: Exit status: 0 Caught exceptions:
 */
