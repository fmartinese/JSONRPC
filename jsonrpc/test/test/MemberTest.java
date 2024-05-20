package test;

import jsonrpc.Error;
import jsonrpc.Member;
import jsonrpc.StructuredMember;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;

import static jsonrpc.JsonRpcObj.putMember;
import static org.junit.Assert.*;

public class MemberTest extends Member {

    /* TODO: fix
    @Test 
    public void toMember() throws JSONException {
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();

        int i = -52;
        Member m = Member.toMember(i);
        assertEquals(Member.Types.NUMBER, m.getType());
        assertEquals(i, m.getInt());
        assertEquals(i, m.getNumber());
        putMember(array, m);
        putMember(object, "integer", m);

        Number num = 3.63;
        m = Member.toMember(num);
        assertEquals(Member.Types.NUMBER, m.getType());
        assertEquals(num, m.getNumber());
        putMember(array, m);
        putMember(object, "number", m);

        Boolean b = false;
        m = Member.toMember(b);
        assertEquals(Member.Types.BOOL, m.getType());
        assertEquals(b, m.getBool());
        putMember(array, m);
        putMember(object, "bool", m);

        String s = "test";
        m = Member.toMember(s);
        assertEquals(Member.Types.STRING, m.getType());
        assertEquals(s, m.getString());
        putMember(array, m);
        putMember(object, "string", m);

        m = Member.toMember(null);
        assertTrue(m.isNull());
        putMember(array, m);
        putMember(object, "nil", m);

        m = Member.toMember(array);
        assertEquals(Member.Types.ARRAY, m.getType());
        assertEquals(array, m.getStructuredMember().getJSONArray());

        putMember(object, "sub array", m);
        m = Member.toMember(object);
        assertEquals(Member.Types.OBJ, m.getType());
        assertEquals(object, m.getStructuredMember().getJSONObject());
    }*/

    @Test (expected = InvalidParameterException.class)
    public void invalidObjectToMember() {
        Member.toMember(new Error("not a member", 0));
        fail("Expected invalid param exception");
    }

    @Test (expected = ClassCastException.class)
    public void nonIntNumber() {
        Number x = -7.12;
        Member m = new Member(x);
        m.getInt();
        fail("Expected class type exceptioin");
    }

    @Test (expected = ClassCastException.class)
    public void wrongTypeGetter() {
        new Member(false).getStructuredMember();
        fail("Expected class cast exceptioin");
    }

    @Test
    public void equals() {
        Member nil = new Member();
        assertEquals(nil, Member.toMember(null));

        Member num = new Member(3.0);
        assertNotEquals(num, new Member(3));
        assertNotEquals(num, Member.toMember(3));
        assertEquals(num, Member.toMember((double)3));

        ArrayList<Member> mems = new ArrayList<>();
        HashMap<String, Member> memsMap = new HashMap<>();
        mems.add(nil);
        mems.add(num);
        memsMap.put("nil",nil);
        memsMap.put("num",num);
        StructuredMember list = new StructuredMember(mems);
        StructuredMember map = new StructuredMember(memsMap);

        Member lm = new Member(list);
        assertNotEquals(lm, Member.toMember(map));
        assertEquals(lm, Member.toMember(list));
    }
}