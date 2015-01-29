package ck;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.util.Date;

/**
 * @author xuer
 * @date 2014-10-29 - 下午2:48:52
 * @Description 由于要调用op2String方法，而op2String在包里面是default修饰的，所以提取出来
 */
public class TraceFormatter {

  public TraceFormatter() {}

  public static String op2String(int op) {
    switch (op) {
      case 0: // '\0'
        return "notification";

      case 1: // '\001'
        return "create";

      case 2: // '\002'
        return "delete";

      case 3: // '\003'
        return "exists";

      case 4: // '\004'
        return "getDate";

      case 5: // '\005'
        return "setData";

      case 6: // '\006'
        return "getACL";

      case 7: // '\007'
        return "setACL";

      case 8: // '\b'
        return "getChildren";

      case 12: // '\f'
        return "getChildren2";

      case 11: // '\013'
        return "ping";

      case -10:
        return "createSession";

      case -11:
        return "closeSession";

      case -1:
        return "error";

      case -9:
      case -8:
      case -7:
      case -6:
      case -5:
      case -4:
      case -3:
      case -2:
      case 9: // '\t'
      case 10: // '\n'
      default:
        return (new StringBuilder()).append("unknown ").append(op).toString();
    }
  }

  public static void main(String args[]) throws IOException {
    if (args.length != 1) {
      System.err.println("USAGE: TraceFormatter trace_file");
      System.exit(2);
    }
    FileChannel fc = (new FileInputStream(args[0])).getChannel();
    do {
      ByteBuffer bb = ByteBuffer.allocate(41);
      fc.read(bb);
      bb.flip();
      byte app = bb.get();
      long time = bb.getLong();
      long id = bb.getLong();
      int cxid = bb.getInt();
      long zxid = bb.getLong();
      int txnType = bb.getInt();
      int type = bb.getInt();
      int len = bb.getInt();
      bb = ByteBuffer.allocate(len);
      fc.read(bb);
      bb.flip();
      String path = "n/a";
      if (bb.remaining() > 0 && type != -10) {
        int pathLen = bb.getInt();
        byte b[] = new byte[pathLen];
        bb.get(b);
        path = new String(b);
      }
      System.out.println((new StringBuilder())
          .append(DateFormat.getDateTimeInstance(3, 1).format(new Date(time))).append(": ")
          .append((char) app).append(" id=0x").append(Long.toHexString(id)).append(" cxid=")
          .append(cxid).append(" op=").append(op2String(type)).append(" zxid=0x")
          .append(Long.toHexString(zxid)).append(" txnType=").append(txnType).append(" len=")
          .append(len).append(" path=").append(path).toString());
    } while (true);
  }
}


/*
 * DECOMPILATION REPORT
 * 
 * Decompiled from: D:\Workspaces\LogFormatter\lib\zookeeper-3.3.3.jar Total time: 100 ms Jad
 * reported messages/errors: Exit status: 0 Caught exceptions:
 */
