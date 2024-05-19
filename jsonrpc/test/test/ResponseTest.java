package test;

import jsonrpc.Id;
import jsonrpc.Member;
import jsonrpc.Response;
import jsonrpc.Error;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.security.InvalidParameterException;

import static org.junit.Assert.*;

public class ResponseTest {
    @Test
    public void testNoId() {
        Response r = new Response(null, new Member(1));
        assertTrue(r.getId().isNull());
    }

    @Test
    public void testNullId() {
        Response r = new Response(new Id(), new Member(1));
        assertTrue(r.getId().isNull());
    }

    @Test
    public void testNullResult() {
        Response r = new Response(new Id(1), new Member());
        assertTrue(r.getResult().getType()==Member.Types.NULL);
    }

    @Test
    public void testResponses() {
        Response r1 = new Response(new Id(3),new Member(2.5));
        Response r2 = new Response(new Id(), new Member("result"));
        Response r3 = new Response(new Id("stringid"), new Member(false));

        assertFalse(r1.hasError());
        assertFalse(r2.hasError());
        assertFalse(r3.hasError());

        assertEquals(3,r1.getId().getInt());
        assertTrue(r2.getId().isNull());
        assertEquals("stringid", r3.getId().getString());

        assertEquals(2.5, r1.getResult().getNumber());
        assertEquals("result", r2.getResult().getString());
        assertEquals(false, r3.getResult().getBool());
    }

    @Test
    public void testError() {
        Error e1 = new Error(Error.Errors.INTERNAL_ERROR);
        Error e2 = new Error("err message", 53, new Member("error data"));
        Response r1 = new Response(new Id(), e1);
        Response r2 = new Response(new Id(), e2);

        assertTrue(r1.hasError());
        assertTrue(r2.hasError());
        assertEquals(e1, r1.getError());
        assertEquals(e2, r2.getError());
    }

    @Test (expected = NullPointerException.class)
    public void testNullJson() {
        new Response(null);
        fail("Expected null ptr exception");
    }

    @Test (expected = InvalidParameterException.class)
    public void testEmptyJson() {
        new Response("");
        fail("Expected invalid parameter exception");
    }

    @Test (expected = InvalidParameterException.class)
    public void testInvlidJson() {
        new Response("test");
        fail("Expected invalid parameter exception");
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test // (expected = InvalidParameterException.class)
    public void testNoVersion() {
        expectedEx.expect(InvalidParameterException.class);
        expectedEx.expectMessage("Not jsonrpc 2.0");
        // no jsonrpc 2.0
        new Response("{\"result\": 19, \"id\": 1}");
        fail("Expected invalid parameter exception");
    }

    @Test // (expected = InvalidParameterException.class)
    public void testWrongVersion() {
        expectedEx.expect(InvalidParameterException.class);
        expectedEx.expectMessage("Not jsonrpc 2.0");
        // no jsonrpc 2.0
        new Response("{\"jsonrpc\": \"2\", \"result\": 19, \"id\": 1}");
        fail("Expected invalid parameter exception");
    }

    @Test
    public void testExtraMembers() {
        try {
            new Response("{\"jsonrpc\": \"2.0\", \"result\": 19, \"id\": 1}, \"unexpected\": \"member\"");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    @Test
    public void testJson() {
        Response r = new Response("{\"jsonrpc\": \"2.0\", \"result\": 19, \"id\": \"stringid\"}");
        assertEquals("stringid",r.getId().getString());

        r = new Response("{\"jsonrpc\": \"2.0\", \"result\": 19, \"id\": 1}");
        assertEquals(19, r.getResult().getNumber());
        assertEquals(1, r.getId().getInt());

        r = new Response("{\"jsonrpc\": \"2.0\", \"error\": {\"code\": -32601, \"message\": \"Method not found\"}, \"id\": \"1\"}");
        assertTrue(r.hasError());
        Error e = new Error(Error.Errors.METHOD_NOT_FOUND);
        assertEquals(e, r.getError());

        r = new Response("{\"jsonrpc\": \"2.0\", \"error\": {\"code\": -32700, \"message\": \"Parse error\"}, \"id\": null}");
        assertTrue(r.hasError());
        e = new Error(Error.Errors.PARSE);
        assertEquals(e, r.getError());
    }
}