package jsonrpc;

import org.json.JSONException;
import org.json.JSONObject;
import java.security.InvalidParameterException;

public class Request extends AbstractRequest {
    public Request(String method, StructuredMember params) {
        super(method, params, null);
    }

    public Request(String method, StructuredMember params, Id id) {
        super(method, params, id);
    }

    public Request(String jsonRpcString) { // public only for junit test
        //https://stleary.github.io/JSON-java/
        //https://stackoverflow.com/questions/21720759/convert-a-json-string-to-a-hashmap
        try {
            obj = new JSONObject(jsonRpcString);

            /* define more specific exceptions */
            if (!obj.has(Members.JSONRPC.toString()) || !obj.getString(Members.JSONRPC.toString()).equals(VER)) {
                throw new InvalidParameterException("Not jsonrpc 2.0");
            }
            if (!obj.has(Members.METHOD.toString())) {
                throw new InvalidParameterException("Method member not defined");
            }
            method = obj.getString(Members.METHOD.toString());

            // parameters can be omitted, but if present, they must be either a JSON array or a JSON object (they cannot be a primitive, e.g., "params": "foo")
            if (obj.has(Members.PARAMS.toString())) {
                params = StructuredMember.toStructuredMember(obj.get(Members.PARAMS.toString()));
            } else {
                params = null;
            }

            notify = !obj.has(Members.ID.toString());
            id = notify ? null : Id.toId(obj.get(Members.ID.toString()));
        } catch (JSONException e) {
            throw new InvalidParameterException(e.getMessage());
        }

        // verify that there are no other parameters
        if (!checkMembersSubset(Members.values(), obj)) {throw new InvalidParameterException("Unexpected member");}

        this.jsonRpcString = obj.toString();
    }

    @Override
    JSONObject toJsonObj() throws JSONRPCException {
        JSONObject object = new JSONObject();

        try {
            object.put(Members.JSONRPC.toString(), VER);
            if (method == null || method.isEmpty()) {
                throw new JSONRPCException("Method member not defined");
            }
            object.put(Members.METHOD.toString(), method);
        } catch (JSONException e) {
            System.out.println(e.getMessage());
        }
        if (params != null) { putStructuredMember(object, Members.PARAMS.toString(), params);} // optional
        if (!notify) {
            putId(object, Members.ID.toString(), id);
        }
        return object;
    }
}