package jsonrpc;

import org.json.JSONException;
import org.json.JSONObject;
import java.security.InvalidParameterException;
import java.util.ArrayList;

public abstract class JsonRpcObj { // public for test
    JSONObject obj;
    // private boolean valid;
    String jsonRpcString;

    public String getJsonString() { // public for test
        return jsonRpcString;
    }

    /*public boolean isValid() {
        return valid;
    }*/

    abstract JSONObject toJsonObj() throws JSONRPCException; // build a JSON-RPC object using attributes. Implemented differently in request, response and error

    static boolean checkMembersSubset(Enum<?> members[], JSONObject obj) {
        // verify that the object has only the parameters contained in the member array
        ArrayList<String> memNames = new ArrayList<>();
        for (Enum<?> mem : members) {
            memNames.add(mem.toString());
        }
        for (String m : JSONObject.getNames(obj)) {
            if (!memNames.contains(m)) {
                return false;
            }
        }
        return true;
    }

    public static void putMember(JSONObject obj, String key, Member value) { // public for test
        try {
            switch (value.getType()) {
                case ARRAY:
                    obj.put(key, value.getJSONArray()); break;
                case OBJ:
                    obj.put(key, value.getJSONObj()); break;
                case BOOL:
                    obj.put(key, value.getBool()); break;
                case NUMBER:
                    obj.put(key, value.getNumber()); break;
                case STRING:
                    obj.put(key, value.getString()); break;
                case NULL:
                    obj.put(key, JSONObject.NULL); break;
                default: throw new InvalidParameterException("Invalid member type");
            }
        } catch (JSONException e) {
            System.out.println(e.getMessage());
        }
    }

    static void putStructuredMember(JSONObject obj, String key, StructuredMember member) {
        try {
            if (member.isArray()) {
                obj.put(key, member.getJSONArray());
            } else {
                obj.put(key, member.getJSONObject());
            }
        } catch (JSONException e) {
            System.out.println(e.getMessage());
        }
    }

    public JSONObject getObj() { // public for test
        return this.obj;
    }
}
