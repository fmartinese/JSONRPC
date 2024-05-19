package zeromq;

public interface IZmqServer {
    String receive();
    void reply(String string) throws UnsupportedOperationException;
}
