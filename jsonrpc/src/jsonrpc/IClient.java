package jsonrpc;

public interface IClient {
    Response sendRequest(Request request) throws JSONRPCException;
    void sendNotify(Request notify) throws JSONRPCException;
}
