package jsonrpc;

import java.util.ArrayList;

public interface IServer {
    ArrayList<Request> receive();
    void reply(ArrayList<Response> response) throws JSONRPCException;
    void reply(Response response) throws JSONRPCException;
}
