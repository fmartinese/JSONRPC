package test;

import jsonrpc.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class ClientTest {
    private static int port = 5001;
    private Client client;
    private Member validResult = new Member(true);

    @Before
    public void setUp() {
        client = new Client(port);
        (new Thread(new RunServer(new Server(port)))).start();
        port++;
    }

    @After
    public void tearDown() {
    }

    @SuppressWarnings("InfiniteLoopStatement")
    class RunServer implements Runnable {
        private IServer s;

        RunServer(IServer s) {
            this.s = s;
        }

        public void run() {
            // while(true) {
            ArrayList<Request> reqs = s.receive();
            ArrayList<Response> resps = new ArrayList<>();
            for (Request r : reqs) {
                if (!r.isNotify()) {
                    resps.add(new Response(r.getId(), validResult));
                }
            }
            try {
                s.reply(resps);
            } catch (JSONRPCException e) {
                fail(e.getMessage());
            }
            // }
        }
    }

    @Test
    public void sendRequest() throws JSONRPCException {
        Request req = new Request("method", null, new Id(1));
        Response resp = client.sendRequest(req);
        assertFalse(resp.hasError());
        assertEquals(validResult, resp.getResult());
        assertEquals(req.getId(), resp.getId());
    }

    @Test (expected = NullPointerException.class)
    public void sendNull() throws JSONRPCException {
        client.sendNotify(null);
        fail("Expected Null ptr exception");
    }

    @Test
    public void sendNotify() throws JSONRPCException {
        Request notify = new Request("method", null);
        client.sendNotify(notify);
        // no exception thrown
    }

    @Test(expected = JSONRPCException.class)
    public void sendRequestAsNotify() throws JSONRPCException {
        Request request = new Request("method", null, new Id(3));
        client.sendNotify(request);
        fail("Expected JSONRPC exception");
    }

    @Test(expected = JSONRPCException.class)
    public void sendNotifyAsRequest() throws JSONRPCException {
        Request notify = new Request("method", null);
        client.sendRequest(notify);
        fail("Expected JSONRPC exception");
    }

    @Test
    public void sendAllRequestBatch() {
        ArrayList<Request> reqs = new ArrayList<>();
        reqs.add(new Request("method", null, new Id(1)));
        reqs.add(new Request("method", null, new Id(2)));
        reqs.add(new Request("method", null, new Id(3)));
        reqs.add(new Request("method", null, new Id(4)));
        ArrayList<Response> resps = client.sendBatch(reqs);

        assertEquals(reqs.size(), resps.size());
        for (Response r : resps) {
            assertFalse(r == null);
            assertFalse(r.hasError());
        }
    }

    @Test
    public void sendAllNotifiesBatch() {
        ArrayList<Request> reqs = new ArrayList<>();
        reqs.add(new Request("method", null));
        reqs.add(new Request("method", null));
        reqs.add(new Request("method", null));
        reqs.add(new Request("method", null));
        ArrayList<Response> resps = client.sendBatch(reqs);

        assertTrue(resps == null);
    }

    @Test
    public void sendMixedBatch() {
        ArrayList<Request> reqs = new ArrayList<>();
        Id id1 = new Id(1);
        Id id2 = new Id(2);
        reqs.add(new Request("method", null));
        reqs.add(new Request("method", null, id1));
        reqs.add(new Request("method", null));
        reqs.add(new Request("method", null, id2));
        reqs.add(new Request("method", null));
        ArrayList<Response> resps = client.sendBatch(reqs);

        assertFalse(resps == null);
        assertEquals(2, resps.size());
        assertEquals(id1, resps.get(0).getId());
        assertEquals(id2, resps.get(1).getId());
    }
}