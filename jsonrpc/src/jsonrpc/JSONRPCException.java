package jsonrpc;

public class JSONRPCException extends Exception {
    public JSONRPCException(String message) {
        super(message);
    }

    public JSONRPCException() {super();}
}