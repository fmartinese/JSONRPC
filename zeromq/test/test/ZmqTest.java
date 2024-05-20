package test;

import org.junit.*;
import zeromq.IZmqClient;
import zeromq.IZmqServer;
import zeromq.ZmqClient;
import zeromq.ZmqServer;

import static org.junit.Assert.*;

public class ZmqTest {
    private static int port = 5001;
    private static IZmqServer s;
    private static IZmqClient c;
    private static final String MSG = "testmessage";
    private static final String RESP = "response";
    private static final String REQ = "request";

    @BeforeClass
    public static void setUpBeforeClass() {
        //
    }

    @Before
    public void setUp() {
        // each test has a server listening on a different port
        s = new ZmqServer(port);
        c = new ZmqClient(port);
        port++;
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        //
    }

    @After
    public void tearDown() throws Exception {
        //
    }

    @Test (expected = NullPointerException.class)
    public void testSendNull() {
        c.send(null);
        fail("Expected null ptr exception");
    }

    @Test
    public void testSendEmpty() {
        c.send("");
        // no exception thrown
    }

    @Test
    public void testReceive() throws Exception {
        c.send(MSG);
        String receivedString = s.receive();
        assertEquals(MSG,receivedString);
    }

    @Test (expected = UnsupportedOperationException.class)
    public void testReplyToNobody() {
        s.reply(MSG);
        fail("Expected UnsupportedOperationException");
    }

    @Test
    public void testRequest() {
        (new Thread(new Server())).start();
        String r = c.request(REQ);
        assertEquals(RESP, r);
    }

    @Test
    public void testMultipleRequestAndNotify() {
        (new Thread(new Server())).start();
        String r = c.request(REQ);
        assertEquals(RESP, r);
        c.send(MSG);
        c.send(MSG);
        r = c.request(REQ);
        assertEquals(RESP, r);
        r = c.request(REQ);
        assertEquals(RESP, r);
        r = c.request(REQ);
        assertEquals(RESP, r);
        c.send(MSG);
        r = c.request(REQ);
        assertEquals(RESP, r);
    }

    @Test
    public void testSendToNoServer() {
        c = new ZmqClient(5123);
        c.send(MSG);
        // no exception thrown
    }

    @SuppressWarnings("InfiniteLoopStatement")
    class Server implements Runnable {
        public void run() {
            while(true) {
                String r = s.receive();
                System.out.println(r);
                if (r.equals(REQ))
                    s.reply(RESP);
            }
        }
    }
}