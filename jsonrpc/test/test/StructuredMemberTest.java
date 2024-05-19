package test;

import jsonrpc.Member;
import jsonrpc.StructuredMember;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.*;

public class StructuredMemberTest {
    // constructors from JSONArray and JSONObject are not tested because they simply assign the value
    // same for getArray and getObj, which are simple getters

    @Test
    public void getMap() {
        HashMap<String, Member> map = new HashMap<>();
        map.put("null", new Member());
        map.put("num", new Member(3));
        map.put("bool", new Member(false));
        StructuredMember subobj = new StructuredMember(map);
        map.put("subobj", new Member(subobj));

        // construction from map internally creates a JSONObject
        StructuredMember tested = new StructuredMember(map);
        assertFalse(tested.isArray());
        // the map is extracted from the JSONObject
        assertEquals(map, tested.getMap());
    }

    @Test
    public void getList() {
        ArrayList<Member> list = new ArrayList<>();
        list.add(new Member());
        list.add(new Member(3));
        list.add(new Member(false));
        StructuredMember subarray = new StructuredMember(list);
        list.add(new Member(subarray));

        // construction from list internally creates a JSONArray
        StructuredMember tested = new StructuredMember(list);
        assertTrue(tested.isArray());
        // the list is extracted from the JSONArray
        assertEquals(list, tested.getList());
    }

    @Test
    public void toStructuredMember() throws JSONException {
        JSONArray a = new JSONArray();
        JSONObject o = new JSONObject();
        a.put("value");
        o.put("key", "value");
        StructuredMember list = StructuredMember.toStructuredMember(a);
        assertTrue(list.isArray());
        StructuredMember map = StructuredMember.toStructuredMember(o);
        assertFalse(map.isArray());
    }

    @Test
    public void equals() throws JSONException {
        ArrayList<Member> list = new ArrayList<>();
        list.add(new Member());
        list.add(new Member(2.5));
        StructuredMember sm1 = new StructuredMember(list);
        list.add(new Member(false));
        StructuredMember sm2 = new StructuredMember(list);
        assertNotEquals(sm1, sm2);

        JSONArray array = new JSONArray();
        array.put(JSONObject.NULL);
        array.put(2.5);
        assertEquals(sm1, StructuredMember.toStructuredMember(array));

        Collections.reverse(list);
        assertNotEquals(sm2, new StructuredMember(list));
    }
}