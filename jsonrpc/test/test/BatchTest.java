package test;

import jsonrpc.*;
import jsonrpc.Error;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import static org.junit.Assert.*;

public class BatchTest {
    private static ArrayList<Request> validReqs = new ArrayList<>();
    private static String reqArr;
    private static ArrayList<Response> validResps = new ArrayList<>();
    private static String respArr;
    private static Error invalidReqError = new Error(Error.Errors.INVALID_REQUEST);

    @BeforeClass
    public static void setUpBeforeClass() {
        // generate some valid requests to use in tests

        validReqs.add(new Request("method", null, new Id("a")));
        validReqs.add(new Request("method", null, new Id(1)));

        JSONArray requests = new JSONArray();
        for (Request r : validReqs)
            requests.put(r.getObj());
        reqArr = requests.toString();

        validResps.add(new Response(new Id("a"), new Member("b")));
        validResps.add(new Response(new Id(1), new Member(2)));

        JSONArray responses = new JSONArray();
        for (Response r : validResps)
            responses.put(r.getObj());
        respArr = responses.toString();
    }

    @Test (expected = NullPointerException.class)
    public void testNullArraylist() {
        new Batch((ArrayList<Request>)null);
        fail("Expected null ptr exception");
    }

    @Test (expected = NullPointerException.class)
    public void testNullJsonArray() {
        new Batch((JSONArray) null);
        fail("Expected null ptr exception");
    }

    @Test (expected = InvalidParameterException.class)
    public void testEmptyArrayList() {
        new Batch(new ArrayList<Request>());
        fail("Expected invalid parameter exception");
    }

    @Test (expected = InvalidParameterException.class)
    public void testEmptyJsonArray() {
        new Batch(new JSONArray());;
        fail("Expected invalid parameter exception");
    }

    private void testInvalidRequest(JSONArray reqs) {
        Batch b = new Batch(reqs);
        int i = reqs.length()-1;
        testInvalidRequest(b, i);
    }

    private void testInvalidRequest(ArrayList<Request> reqs) {
        Batch b = new Batch(reqs);
        int i = reqs.size() - 1;
        testInvalidRequest(b, i);
    }

    private void testInvalidRequest(Batch b, int i) {
        assertTrue(b.getAllRequests().get(i)==null); // last request is null because it's invalid
        assertTrue(b.getValidRequests().size()==i); // request is not included among the valid ones
        b.put(validResps);
        assertEquals(b.getAllResponses(), b.getValidResponses());
        assertTrue(b.getAllResponses().get(i).hasError());
        assertEquals(b.getAllResponses().get(i).getError(),invalidReqError);
        assertEquals(b.getAllResponses().get(i).getId(), new Id());
    }

    @Test
    public void testNullRequestArrayList() {
        ArrayList<Request> reqs = validReqs;
        reqs.add(null);
        testInvalidRequest(reqs);
    }

    @Test
    public void testNullRequestJsonArray() throws JSONException {
        JSONArray reqs = new JSONArray(reqArr);
        reqs.put((JSONObject)null);
        testInvalidRequest(reqs);
    }

    @Test
    public void testEmpty() throws JSONException {
        JSONArray reqs = new JSONArray(reqArr);
        reqs.put("");
        testInvalidRequest(reqs);
    }

    @Test
    public void testInvalidJson() throws JSONException {
        JSONArray reqs = new JSONArray(reqArr);
        reqs.put("test");
        testInvalidRequest(reqs);
    }

    @Test
    public void testNotRequestJson() throws JSONException {
        JSONArray reqs = new JSONArray(reqArr);
        reqs.put("[]");
        testInvalidRequest(reqs);
    }

    @Test
    public void testInvalidRequestJson() throws JSONException {
        JSONArray reqs = new JSONArray(reqArr);
        reqs.put("{\"not\": \"a request\"}");
        testInvalidRequest(reqs);
    }

    @Test
    public void testBatch() {
        ArrayList<Request> testReq = new ArrayList<>();
        testReq.add(new Request("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": [42, 23], \"id\": 1}"));
        testReq.add(new Request("{\"jsonrpc\": \"2.0\", \"method\": \"update\", \"params\": [1,2,3,4,5]}"));
        testReq.add(new Request("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": {\"subtrahend\": 23, \"minuend\": 42}, \"id\": 3}"));
        testReq.add(new Request("{\"jsonrpc\": \"2.0\", \"method\": \"test\", \"params\": {\"subtrahend\": 23, \"minuend\": 42, \"subobj\": {\"par1\": 34, \"par2\": \"value\", \"array\": [1,2,3]}}, \"id\": 3}"));
        testReq.add(new Request("{\"jsonrpc\": \"2.0\", \"method\": \"update\", \"params\": [1,2,5]}"));

        ArrayList<Response> testResp = new ArrayList<>();
        testResp.add(new Response("{\"jsonrpc\": \"2.0\", \"result\": 19, \"id\": 1}"));
        testResp.add(new Response("{\"jsonrpc\": \"2.0\", \"error\": {\"code\": -32601, \"message\": \"Method not found\"}, \"id\": \"1\"}"));
        testResp.add(new Response("{\"jsonrpc\": \"2.0\", \"error\": {\"code\": -32700, \"message\": \"Parse error\"}, \"id\": null}"));

        Batch b = new Batch(testReq);
        b.put(testResp);

        assertEquals(testResp, b.getValidResponses());
        assertTrue(b.getAllResponses().size()==5);
        assertEquals(testReq, b.getValidRequests());
        assertEquals(testReq, b.getAllRequests());

        JSONArray reqJ = new JSONArray(), respJ = new JSONArray();
        for (Response r : testResp)
            respJ.put(r.getObj());
        for (Request r : testReq)
            reqJ.put(r.getObj());

        assertEquals(respJ.toString(), b.getResponseJSON());
        assertEquals(reqJ.toString(), b.getRequestJSON());

    }

    @Test
    public void testIsOnlyNotifies() throws JSONException {
        Request r1 = new Request("foo",null, new Id(1));
        Request r2 = new Request("foo",null, new Id(2));
        Request n1 = new Request("foo",null);
        Request n2 = new Request("foo",null);

        ArrayList<Request> reqs = new ArrayList<>();
        reqs.add(r1);
        assertFalse(new Batch(reqs).isOnlyNotifies());

        reqs.add(r2);
        assertFalse(new Batch(reqs).isOnlyNotifies());

        reqs.add(n1);
        assertFalse(new Batch(reqs).isOnlyNotifies());

        reqs.clear();

        reqs.add(n1);
        assertTrue(new Batch(reqs).isOnlyNotifies());

        reqs.add(n2);
        assertTrue(new Batch(reqs).isOnlyNotifies());

        reqs.add(r1);
        assertFalse(new Batch(reqs).isOnlyNotifies());

        reqs.clear();

        reqs.add(n1);
        reqs.add(null);
        assertFalse(new Batch(reqs).isOnlyNotifies());

        JSONArray arr = new JSONArray();

        arr.put(r1.getObj());
        assertFalse(new Batch(arr).isOnlyNotifies());

        arr.put(n1.getObj());
        assertFalse(new Batch(arr).isOnlyNotifies());

        arr = new JSONArray();

        arr.put(n2.getObj());
        assertTrue(new Batch(arr).isOnlyNotifies());

        arr.put(r2.getObj().put("not", "a valid request"));
        assertFalse(new Batch(arr).isOnlyNotifies());
    }
}
