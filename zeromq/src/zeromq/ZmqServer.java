package zeromq;

import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

public class ZmqServer implements IZmqServer {
    private ZMQ.Socket socket;
    private ZFrame identity;
    private ZFrame empty;
    private String addr;
    private ZMQ.Context ctx;

    public ZmqServer(int port) {
        ctx = ZMQ.context(1);
        socket = ctx.socket(ZMQ.ROUTER);
        addr = "tcp://*:" + String.valueOf(port);
        socket.bind(addr);
        identity = null;
        empty = null;
    }

    @Override
    public String receive() {
        ZMsg msg = ZMsg.recvMsg(socket);
        identity = msg.pop(); // incoming messages from the dealer have an identity, making it possible to reply
        empty = msg.size() == 2 ? msg.pop() : null; // messages sent by the dealer do not have an empty frame
        return msg.pop().toString();
    }

    @Override
    public void reply(String string) throws UnsupportedOperationException{
        if (identity == null) { throw new UnsupportedOperationException("Receiver undefined"); } // change exception type
        ZMsg msg = new ZMsg();
        msg.push(new ZFrame(string.getBytes()));
        if (empty!=null) {msg.push(empty);} // messages to be sent to the dealer must not have empty frames
        msg.push(identity);
        msg.send(socket);

        // in theory, a router can reply multiple times to a single message, not knowing the client's type (it might not necessarily be a req)
        // in case it is desired to make it possible, do not set identity and empty to null, but the server might reply multiple times only to the last client
        identity = null;
        empty = null;
    }
}