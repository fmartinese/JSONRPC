package test;

import jsonrpc.Error;
import jsonrpc.JsonRpcObj;
import jsonrpc.Member;
import jsonrpc.StructuredMember;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.InvalidParameterException;
import java.util.HashMap;

import static org.junit.Assert.*;

public class ErrorTest {
    private static String msg = "message";
    private static int code = -32000;
    private static Member errData;

    @BeforeClass
    public static void setUpBeforeClass() {
        HashMap<String, Member> data = new HashMap<>();
        data.put("data", new Member(32));
        data.put("data2", new Member(false));
        errData = new Member(new StructuredMember(data));
    }

    @Test
    public void hasErrorData() {
        Error e = new Error(Error.Errors.INTERNAL_ERROR);
        assertFalse(e.hasErrorData());
        e = new Error(Error.Errors.INTERNAL_ERROR, null);
        assertFalse(e.hasErrorData());
        e = new Error("message",0);
        assertFalse(e.hasErrorData());
        e = new Error("message", 0, null);
        assertFalse(e.hasErrorData());

        Member errData = new Member();
        e = new Error(Error.Errors.PARSE, errData);
        assertTrue(e.hasErrorData());
        e = new Error("message",0,errData);
        assertTrue(e.hasErrorData());
    }

    @Test (expected = NullPointerException.class)
    public void getNullData() throws NullPointerException {
        Error e = new Error("message", 0);
        e.getErrorData();
        fail("Expected null ptr exception");
    }

    @Test (expected = InvalidParameterException.class)
    public void nullMsg() {
        new Error(null, 0);
        fail("Expected invalid param exception");
    }

    @Test (expected = InvalidParameterException.class)
    public void emptyMsg() {
        new Error("", 0);
        fail("Expected invalid param exception");
    }

    @Test
    public void testConstruction() {
        Error e = new Error(msg, code, errData);
        assertTrue(e.hasErrorData());
        assertEquals(msg, e.getErrorMessage());
        assertEquals(code, e.getErrorCode());
        assertEquals(errData, e.getErrorData());
    }

    @Test
    public void testConstructorFromObj() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(Error.ErrMembers.CODE.toString(), code);
        obj.put(Error.ErrMembers.MESSAGE.toString(), msg);
        JsonRpcObj.putMember(obj, Error.ErrMembers.DATA.toString(), errData);
        Error e = new Error(obj);
        assertEquals(code, e.getErrorCode());
        assertEquals(msg, e.getErrorMessage());
        assertEquals(errData, e.getErrorData());
    }

    @Test (expected = InvalidParameterException.class)
    public void testConstructorFromObjNoCode() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(Error.ErrMembers.MESSAGE.toString(), msg);
        JsonRpcObj.putMember(obj, Error.ErrMembers.DATA.toString(), errData);
        new Error(obj);
        fail("Expected invalid parameter exception");
    }

    @Test (expected = InvalidParameterException.class)
    public void testConstructorFromObjNoMsg() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(Error.ErrMembers.CODE.toString(), code);
        JsonRpcObj.putMember(obj, Error.ErrMembers.DATA.toString(), errData);
        new Error(obj);
        fail("Expected invalid parameter exception");
    }

    @Test (expected = InvalidParameterException.class)
    public void testConstructorFromObjNullCode() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(Error.ErrMembers.CODE.toString(), JSONObject.NULL);
        obj.put(Error.ErrMembers.MESSAGE.toString(), msg);
        JsonRpcObj.putMember(obj, Error.ErrMembers.DATA.toString(), errData);
        new Error(obj);
        fail("Expected invalid parameter exception");
    }

    @Test (expected = InvalidParameterException.class)
    public void testConstructorFromObjNullMsg() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(Error.ErrMembers.CODE.toString(), code);
        obj.put(Error.ErrMembers.MESSAGE.toString(), JSONObject.NULL);
        JsonRpcObj.putMember(obj, Error.ErrMembers.DATA.toString(), errData);
        new Error(obj);
        fail("Expected invalid parameter exception");
    }

    @Test (expected = InvalidParameterException.class)
    public void testConstructorFromObjEmptyMsg() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(Error.ErrMembers.CODE.toString(), code);
        obj.put(Error.ErrMembers.MESSAGE.toString(), "");
        JsonRpcObj.putMember(obj, Error.ErrMembers.DATA.toString(), errData);
        new Error(obj);
        fail("Expected invalid parameter exception");
    }

    @Test (expected = InvalidParameterException.class)
    public void testConstructorFromObjExtraMember() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put(Error.ErrMembers.CODE.toString(), code);
        obj.put(Error.ErrMembers.MESSAGE.toString(), msg);
        JsonRpcObj.putMember(obj, Error.ErrMembers.DATA.toString(), errData);
        obj.put("unexpected", "member");
        new Error(obj);
        fail("Expected invalid parameter exception");
    }

    @Test
    public void testEquals() {
        Member data = new Member("error data");
        Error parse = new Error(Error.Errors.PARSE);
        Error parseData = new Error(Error.Errors.PARSE, data);

        assertNotEquals(parse,null);
        assertNotEquals(parse, "test");
        assertNotEquals(parse, new Error(Error.Errors.METHOD_NOT_FOUND));
        assertNotEquals(parse, parseData);
        assertNotEquals(parseData, new Error(Error.Errors.METHOD_NOT_FOUND, data));
        assertEquals(parse, new Error(Error.Errors.PARSE));
        assertEquals(parseData, new Error(Error.Errors.PARSE, data));
        assertNotEquals(parseData, new Error(Error.Errors.PARSE, new Member(false)));
        assertEquals(parse, new Error(Error.Errors.PARSE.getMessage(), Error.Errors.PARSE.getCode()));
    }
}