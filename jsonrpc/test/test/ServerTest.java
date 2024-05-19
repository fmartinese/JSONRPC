package test;

import jsonrpc.*;
import jsonrpc.Error;
import org.junit.Before;
import org.junit.Test;
import zeromq.IZmqClient;
import zeromq.ZmqClient;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class ServerTest {
    private static int port = 5454;
    private Server server;
    private IZmqClient client;
    private Request req = new Request("method", null, new Id(1));
    private Request not = new Request("method", null);
    private static final Error PARSE_ERROR = new Error(Error.Errors.PARSE);

    @Before
    public void setUp() {
        server = new Server(port);
        client = new ZmqClient(port);
        port++;
    }

    @Test
    public void receive() {
        // receive single request (JSONObject)
        client.send(req.getJsonString());
        ArrayList<Request> r = server.receive();
        assertEquals(1, r.size());
        assertEquals(req, r.get(0));
        // receive batch of one request
        ArrayList<Request> reqs = new ArrayList<>();
        reqs.add(not);
        Batch b = new Batch(reqs);
        client.send(b.getRequestJSON());
        r = server.receive();
        assertEquals(1, r.size());
        assertEquals(not, r.get(0));
        // receive multiple request (batch) (JSONArray)
        reqs = new ArrayList<>();
        reqs.add(req);
        reqs.add(not);
        b = new Batch(reqs);
        client.send(b.getRequestJSON());
        r = server.receive();
        assertEquals(2, r.size());
        assertEquals(req, r.get(0));
        assertEquals(not, r.get(1));

    }

    private String thread_received;
    private ArrayList<Request> thread_req;
    @Test
    public void receiveInvalid() throws InterruptedException {
        class Receiver implements Runnable {
            public void run() {
                thread_req = server.receive();
            }
        }
        class Requester implements Runnable {
            public void run() {
                thread_received = client.request("not a json object or json array");
            }
        }

        Thread s = new Thread(new Receiver());
        Thread c = new Thread(new Requester());
        s.start();
        c.start();
        s.join();
        c.join();
        Response resp = new Response(thread_received);
        assertEquals(0, thread_req.size());
        assertTrue(resp.hasError());
        assertEquals(PARSE_ERROR, resp.getError());
    }

    @Test (expected = UnsupportedOperationException.class)
    public void replySingleEx() throws JSONRPCException {
        server.reply(new Response(new Id(), new Member()));
        fail("Expected Unsupported Operation exception");
    }

    @Test
    public void replySingleResponse() {
    }

    @Test
    public void replyMultiResponses() {
    }
}