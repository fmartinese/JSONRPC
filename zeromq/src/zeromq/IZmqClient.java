package zeromq;

public interface IZmqClient {
    String request(String req);
    void send(String msg);
}
