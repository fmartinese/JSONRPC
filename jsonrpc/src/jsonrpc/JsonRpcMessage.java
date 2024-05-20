package jsonrpc;

import org.json.JSONException;
import org.json.JSONObject;

abstract class JsonRpcMessage extends JsonRpcObj {
    Id id; // it can be a String or an Integer (or null in some cases (non-notification))
    static final String VER = "2.0";

    public Id getId() {
        // a null ID is different from a notification
        if (id == null) {
            throw new NullPointerException("Notify: id undefined"); // it is a notification
        }
        return id;
    }

    static void putId(JSONObject obj, String key, Id id) {
        try {
            switch (id.getType()) {
                case INT: obj.put(key, id.getInt()); break;
                case STRING: obj.put(key, id.getString()); break;
                case NULL: obj.put(key, JSONObject.NULL); break;
            }
        } catch (JSONException e) {
            System.out.println(e.getMessage());
        }
    }
}