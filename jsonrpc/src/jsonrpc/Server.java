package jsonrpc;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import zeromq.IZmqServer;
import zeromq.ZmqServer;
import java.util.ArrayList;

public class Server implements IServer {
    private IZmqServer server;
    private Batch currBatch;

    public Server(int port) {
        this.server = new ZmqServer(port);
        this.currBatch = null;
    }

    @Override
    public ArrayList<Request> receive() {
        String receivedString = server.receive();
        try {
            Object json = new JSONTokener(receivedString).nextValue();
            if (json instanceof JSONArray) {
                JSONArray arr = new JSONArray(receivedString);
                if (arr.length() == 0) {throw new JSONRPCException("Request batch array is empty");}
                currBatch = new Batch(arr);
                return currBatch.getValidRequests();
            } else if (json instanceof JSONObject) {
                currBatch = null;
                // a non-batch request: ArrayList<Request> of size 1
                Request req = new Request(receivedString);
                ArrayList<Request> requests = new ArrayList<>();
                requests.add(req);
                return requests;
            } else {
                throw new JSONRPCException("Invalid json received");
            }
        } catch (JSONException | JSONRPCException e) {
            // if the JSON is invalid, an automatic single response with parsing error is returned (see documentation)
            // regardless of whether it was a single request or an array of requests
            Id id = Id.getIdFromRequest(receivedString); // if it's a single request, it attempts to retrieve its ID; otherwise, the ID is null
            Error err = new Error(Error.Errors.PARSE);
            Response errorResp = new Response(id, err);
            server.reply(errorResp.getJsonString());
            currBatch = null;
        }
        return new ArrayList<>(); // if there are errors, the list of requests to execute is empty
    }

    public void reply(Response response) throws JSONRPCException {
        if (currBatch != null) { throw new JSONRPCException("Batch responses needed");}
        server.reply(response.getJsonString());
    }

    @Override
    public void reply(ArrayList<Response> responses) throws JSONRPCException {
        if (currBatch == null && responses.size() > 1) {
            throw new JSONRPCException("Single response needed");
        }
        if (responses.size() == 0) {
            // no response to a batch of only notifications
            currBatch = null;
        } else if (responses.size() == 1 && currBatch == null) {
            // single response to a single request
            // when currBatch == null, it prevents responding with a single response to a batch of requests of size 1. In this case, respond with a batch of responses of size 1
            this.reply(responses.get(0));
        } else {
            currBatch.put(responses);
            server.reply(currBatch.getResponseJSON());
            currBatch = null;
        }
    }
}
