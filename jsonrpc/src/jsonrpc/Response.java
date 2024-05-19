package jsonrpc;

import org.json.JSONException;
import org.json.JSONObject;
import java.security.InvalidParameterException;

public class Response extends AbstractResponse {
    public Response(Id id, Member result) {
        super(id, result);
    }

    public Response(Id id, Error error) {
        super(id, error);
    }

    public Response(String jsonRpcString) { // public only for junit test
        try {
            obj = new JSONObject(jsonRpcString);

            if (!obj.has(Members.JSONRPC.toString()) || !obj.getString(Members.JSONRPC.toString()).equals(VER)) {
                throw new InvalidParameterException("Not jsonrpc 2.0");
            }

            if (obj.has(Members.RESULT.toString())) {
                result = Member.toMember(obj.get(Members.RESULT.toString()));
                error = null;
            } else if (obj.has(Members.ERROR.toString())) {
                error = new Error((JSONObject) obj.get(Members.ERROR.toString()));
                result = null;
            } else {
                throw new InvalidParameterException("Method member not defined");
            }

            // mandatory in responses
            if (obj.has(Members.ID.toString())) {
                id = Id.toId(obj.get(Members.ID.toString()));
            } else {
                throw new InvalidParameterException("ID member not defined");
            }
        } catch (JSONException e) {
            throw new InvalidParameterException(e.getMessage());
        }

        // check that there are no other parameters
        if (!checkMembersSubset(Members.values(), obj)) {
            throw new InvalidParameterException("Unexpected member");
        }

        this.jsonRpcString = obj.toString();
    }

    @Override
    JSONObject toJsonObj() {
        JSONObject object = new JSONObject();

        try {
            object.put(Members.JSONRPC.toString(), VER);
            if (error == null) {
                putMember(object, Members.RESULT.toString(), result);
            } else {
                object.put(Members.ERROR.toString(), error.getJsonObj());
            }
        } catch (JSONException e) {
            System.out.println(e.getMessage());
        }
        putId(object, Members.ID.toString(), id);

        return object;
    }
}