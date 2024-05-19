package test;

import jsonrpc.Id;
import jsonrpc.Member;
import jsonrpc.Request;
import jsonrpc.StructuredMember;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import static org.junit.Assert.*;

public class RequestTest {
    private static StructuredMember paramsList;
    private static StructuredMember paramsMap;
    private static final String METHOD = "method";

    @BeforeClass
    public static void setUpBeforeClass() {
        ArrayList<Member> params = new ArrayList<>();
        params.add(new Member(3));
        params.add(new Member("value"));
        paramsList = new StructuredMember(params);

        HashMap<String, Member> par = new HashMap<>();
        par.put("member1", new Member(3));
        par.put("member", new Member("value"));
        paramsMap = new StructuredMember(par);
    }

    @Test (expected = InvalidParameterException.class)
    public void testNullMethodConstructor() {
        new Request(null, paramsList);
        fail("Expected Invalid params exception");
    }

    @Test (expected = InvalidParameterException.class)
    public void testEmptyMethodConstructor() {
        new Request("", paramsList);
        fail("Expected Invalid params exception");
    }

    @Test
    public void testNotifyConstructor() {
        Request req = new Request(METHOD, paramsList);
        assertTrue(req.isNotify());
    }

    @Test
    public void testNotifyFromNullId() {
        Request req = new Request(METHOD, paramsList, null);
        assertTrue(req.isNotify());
    }

    @Test
    public void testParams() {
        Request reqList = new Request(METHOD, paramsList);
        Request reqMap = new Request(METHOD, paramsMap);
        assertEquals(paramsList, reqList.getParams());
        assertEquals(paramsMap, reqMap.getParams());
    }

    @Test
    public void testId() {
        Request nullid = new Request(METHOD, null, new Id());
        String sId = "stringid";
        Request stringid = new Request(METHOD, null, new Id(sId));
        int nId = 5;
        Request intid = new Request(METHOD, null, new Id(nId));

        assertFalse(nullid.isNotify());
        assertFalse(stringid.isNotify());
        assertFalse(intid.isNotify());
        assertTrue(nullid.getId().isNull());
        assertEquals(sId, stringid.getId().getString());
        assertEquals(nId, intid.getId().getInt());
    }

    @Test
    public void testMethod() {
        Request r = new Request(METHOD, null);
        assertEquals(METHOD, r.getMethod());
    }

    @Test (expected = InvalidParameterException.class)
    public void testFromEmptyJson() {
        new Request("");
        fail("Expected invalid parameter exception");
    }

    @Test (expected = NullPointerException.class)
    public void testFromNullJson() {
        new Request(null);
        fail("Expected null ptr exception");
    }

    @Test (expected = InvalidParameterException.class)
    public void testFromInvalidJson() {
        new Request("test");
        fail("Expected invalid parameter exception");
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test // (expected = InvalidParameterException.class)
    public void testNoVersion() {
        expectedEx.expect(InvalidParameterException.class);
        expectedEx.expectMessage("Not jsonrpc 2.0");
        // no jsonrpc 2.0
        new Request("{\"method\": \"subtract\", \"params\": [42, 23], \"id\": 1}");
        fail("Expected invalid parameter exception");
    }

    @Test // (expected = InvalidParameterException.class)
    public void testWrongVergion() {
        expectedEx.expect(InvalidParameterException.class);
        expectedEx.expectMessage("Not jsonrpc 2.0");
        // jsonrpc != 2.0
        new Request("{\"jsonrpc\": \"3.0\", \"method\": \"subtract\", \"params\": [42, 23], \"id\": 1}");
        fail("Expected invalid parameter exception");
    }

    @Test // (expected = InvalidParameterException.class)
    public void testNotStructuredParams() {
        expectedEx.expect(InvalidParameterException.class);
        expectedEx.expectMessage("Not a structured member");
        new Request("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": 42, \"id\": 1}");
        fail("Expected invalid parameter exception");
    }

    @Test
    public void testNullIdFromJson() {
        Request r = new Request("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": [42, 23], \"id\": null}");
        assertFalse(r.isNotify());
    }

    @Test
    public void testFromJson() {
        Request r1 = new Request("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": [42, 23], \"id\": 1}");
        assertEquals("subtract", r1.getMethod());
        assertEquals(1, r1.getId().getInt());
        ArrayList<Member> params1 = new ArrayList<>();
        params1.add(new Member(42));
        params1.add(new Member(23));
        StructuredMember sm1 = new StructuredMember(params1);
        assertEquals(sm1, r1.getParams());

        Request r2 = new Request("{\"jsonrpc\": \"2.0\", \"method\": \"update\", \"params\": [1,2,3,4,5]}");
        assertEquals("update", r2.getMethod());
        assertTrue(r2.isNotify());
        ArrayList<Member> params2 = new ArrayList<>();
        for (int i=1; i<=5; i++) {
            params2.add(new Member(i));
        }
        assertEquals(new StructuredMember(params2), r2.getParams());

        Request r3 = new Request("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": {\"subtrahend\": 23, \"minuend\": 42}, \"id\": 3}");
        assertEquals("subtract", r3.getMethod());
        assertEquals(3, r3.getId().getInt());
        HashMap<String, Member> params3 = new HashMap<>();
        params3.put("subtrahend", new Member(23));
        params3.put("minuend", new Member(42));
        assertEquals(new StructuredMember(params3), r3.getParams());
    }

    @Test
    public void testNestedParams() {
        Request r = new Request("{\"jsonrpc\": \"2.0\", \"method\": \"test\", \"params\": {\"subtrahend\": 23, \"minuend\": 42, \"subobj\": {\"par1\": 34, \"par2\": \"value\", \"array\": [1,2,3]}}, \"id\": 3}");

        HashMap<String, Member> par = new HashMap<>();
        par.put("subtrahend", new Member(23));
        par.put("minuend", new Member(42));

        HashMap<String, Member> subpar = new HashMap<>();
        subpar.put("par1", new Member(34));
        subpar.put("par2", new Member("value"));

        ArrayList<Member> subarray = new ArrayList<>();
        subarray.add(new Member(1));
        subarray.add(new Member(2));
        subarray.add(new Member(3));

        subpar.put("array", new Member(new StructuredMember(subarray)));

        par.put("subobj", new Member(new StructuredMember(subpar)));

        assertEquals(new StructuredMember(par), r.getParams());
    }
}