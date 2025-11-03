import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.Socket;

import org.apache.jute.BinaryOutputArchive;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooDefs.OpCode;
import org.apache.zookeeper.proto.ConnectRequest;
import org.apache.zookeeper.proto.CreateRequest;
import org.apache.zookeeper.proto.RequestHeader;

public class RawSessionClient {
    public static void main(String[] args) throws Exception {
        String hostPort = (args.length>0)? args[0] : "127.0.0.1:2181";
        int repeat = (args.length>1)? Integer.parseInt(args[1]) : 10;

        for (int i = 0; i < repeat; i++) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BinaryOutputArchive boa = BinaryOutputArchive.getArchive(baos);

            // connect (create a session)
            boa.writeInt(44, "len");
            ConnectRequest conReq = new ConnectRequest(0, 0, 30000, 0, new byte[16]);
            conReq.serialize(boa, "connect");

            // close session
            boa.writeInt(8, "len");
            RequestHeader h = new RequestHeader(1, ZooDefs.OpCode.closeSession);
            h.serialize(boa, "header");

            // create ephemeral znode
            boa.writeInt(52, "len"); // filler length
            RequestHeader header = new RequestHeader(2, OpCode.create);
            header.serialize(boa, "header");
            CreateRequest createReq = new CreateRequest("/foo" + i, new byte[0],
                    Ids.OPEN_ACL_UNSAFE, 1);
            createReq.serialize(boa, "request");
            baos.close();

            byte[] data = baos.toByteArray();
            String hp[] = hostPort.split(":");
            Socket sock = new Socket(hp[0], Integer.parseInt(hp[1]));
            try {
                OutputStream out = sock.getOutputStream();
                out.write(data);
                out.flush();

                InputStream in = sock.getInputStream();
                // read server responses (non-blocking read loop with small timeout)
                sock.setSoTimeout(500);
                byte[] buf = new byte[1024];
                try {
                    while (in.read(buf) >= 0) { /* consume */ }
                } catch (java.net.SocketTimeoutException ste) {
                    // normal, no more data
                }
            } finally {
                sock.close();
            }
            System.out.println("iteration " + i + " done");
            // tiny pause to increase race likelihood
            Thread.sleep(50);
        }
        System.out.println("done sending raw sequences");
    }
}