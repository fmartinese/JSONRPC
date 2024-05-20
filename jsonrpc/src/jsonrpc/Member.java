package jsonrpc;

import org.json.JSONArray;
import org.json.JSONObject;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;

public class Member {
    public enum Types {NULL, STRING, NUMBER, BOOL, OBJ, ARRAY}
    private Object value;
    private Types type;

    public Member() {
        value = JSONObject.NULL;
        type = Types.NULL;
    }

    public Member(String string) {
        if (string == null) {throw new NullPointerException("Member value is null");}
        if (string.isEmpty()) {throw new InvalidParameterException("Member value is empty");}
        value = string;
        type = Types.STRING;
    }

    public Member(Number num) {
        if (num == null) {throw new NullPointerException("Member value is null");}
        value = num;
        type = Types.NUMBER;
    }

    public Member(boolean bool) {
        value = bool;
        type = Types.BOOL;
    }

    public Member(JSONObject obj) {
        if (obj == null) {throw new NullPointerException("Member value is null");}
        value = new StructuredMember(obj);
        type = Types.OBJ;
    }

    public Member(JSONArray array) {
        if (array == null) {throw new NullPointerException("Member value is null");}
        value = new StructuredMember(array);
        type = Types.ARRAY;
    }

    public Member(StructuredMember m) {
        if (m == null) {throw new NullPointerException("Member value is null");}
        value = m;
        type = m.isArray() ? Types.ARRAY : Types.OBJ;
    }

    public Types getType() {
        return type;
    }

    /*public boolean isStructured() {
        return (type == Types.OBJ || type == Types.ARRAY);
    }*/

    /*public boolean isNull() {
        return type == Types.NULL;
    }*/

    public boolean getBool() {
        if (type != Types.BOOL) {throw new ClassCastException("Not a boolean");}
        return (boolean)value;
    }

    public Number getNumber() {
        if (type != Types.NUMBER) {throw new ClassCastException("Not a number");}
        return (Number)value;
    }

    public int getInt() {
        if (!(value instanceof Integer)) {throw new ClassCastException("Not an integer");}
        return (int)value;
    }

    public String getString() {
        if (type != Types.STRING) {throw new ClassCastException("Not a string");}
        return (String)value;
    }

    public StructuredMember getStructuredMember() {
        if (type != Types.OBJ && type != Types.ARRAY) {throw new ClassCastException("Not a structured member");}
        return (StructuredMember)value;
    }

    public ArrayList<Member> getList() {
        if (type != Types.ARRAY) {throw new ClassCastException("Not a json array");}
        return ((StructuredMember)value).getList();
    }

    public HashMap<String, Member> getMap() {
        if (type != Types.OBJ) {throw new ClassCastException("Not a json object");}
        return ((StructuredMember)value).getMap();
    }

    JSONObject getJSONObj() {
        if (type != Types.OBJ) {throw new ClassCastException("Not a json object");}
        return ((StructuredMember)value).getJSONObject();
    }

    JSONArray getJSONArray() {
        if (type != Types.ARRAY) {throw new ClassCastException("Not a json array");}
        return ((StructuredMember)value).getJSONArray();
    }

    private static Member parse(Object value) {
        if (value == null) {
            return new Member();
        } else if(value instanceof JSONArray) {
            return new Member((JSONArray)value);
        } else if(value instanceof JSONObject) {
            return new Member((JSONObject)value);
        } else if(value instanceof Number) {
            return new Member((Number) value);
        } else if(value instanceof String) {
            return new Member((String)value);
        } else if(value instanceof Boolean) {
            return new Member((boolean)value);
        } else {
            throw new InvalidParameterException("Not a valid parameter type");
        }
    }

    public static Member toMember(Object obj) {
        return parse(obj);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Member)) return false;
        Member o = (Member) other;
        if (this.type != o.type) return false;

        switch (this.type) {
            case NULL: return true;
            case STRING: return getString().equals(o.getString());
            case NUMBER: return getNumber().equals(o.getNumber());
            case BOOL: return getBool()==o.getBool();
            default: return getStructuredMember().equals(o.getStructuredMember());
        }
    }
}