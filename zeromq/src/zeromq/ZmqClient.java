package zeromq;

import org.zeromq.ZMQ;

public class ZmqClient implements IZmqClient {
    private ZMQ.Context ctx;
    private ZMQ.Socket socket;
    private int port;

    public ZmqClient(int port) {
        ctx = ZMQ.context(1);
        this.port = port;
    }

    // use a connection pooling pattern for connections?
    @Override
    public String request(String req) {
        socket = ctx.socket(ZMQ.REQ);
        socket.connect("tcp://localhost:" + String.valueOf(port));
        socket.send(req.getBytes());
        String s = socket.recvStr();
        socket.close();
        return s;
    }

    @Override
    public void send(String msg) {
        socket = ctx.socket(ZMQ.DEALER);
        socket.connect("tcp://localhost:"+String.valueOf(port));
        socket.send(msg.getBytes());
        socket.close();
    }
}