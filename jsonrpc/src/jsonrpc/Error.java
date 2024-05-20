package jsonrpc;

import org.json.JSONException;
import org.json.JSONObject;
import java.security.InvalidParameterException;

public class Error extends JsonRpcObj {
    private String message;
    private int code; // as per the specification, it must be an integer
    private Member data; // primitive or structured

    public enum ErrMembers {
        CODE("code"), MESSAGE("message"), DATA("data");

        private final String text;
        ErrMembers(final String text) {
            this.text = text;
        }
        @Override
        public String toString() {return text;}
    }
    
    public enum Errors {
        PARSE(-32700, "Parse error"),
        INVALID_REQUEST(-32600, "Invalid Request"),
        METHOD_NOT_FOUND(-32601, "Method not found"),
        INVALID_PARAMS(-32602, "Invalid params"),
        INTERNAL_ERROR(-32603, "Internal error");

        private final int code;
        private final String message;
        Errors(final int code, final String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {return code;}
        public String getMessage() {return message;}
    }
    
    public Error(String errorMessage, int errorCode, Member errorData) {
        if (errorMessage == null || errorMessage.isEmpty()) {throw new InvalidParameterException("Error message not defined");}
        this.message = errorMessage;
        this.code = errorCode;
        this.data = errorData;
        try {
            this.obj = toJsonObj();
        } catch (JSONRPCException e) {
            throw new InvalidParameterException(e.getMessage());
        }
        this.jsonRpcString = obj.toString();
    }

    public Error(String errorMessage, int errorCode) {
        this(errorMessage, errorCode, null);
    }

    public Error(Errors error) {
        this.message = error.getMessage();
        this.code = error.getCode();
        this.data = null;
        try {
            this.obj = toJsonObj();
        } catch (JSONRPCException e) {
            throw new InvalidParameterException(e.getMessage());
        }
        this.jsonRpcString = obj.toString();
    }

    public Error(Errors error, Member errorData) {
        this(error.getMessage(), error.getCode(), errorData);
    }

    public String getErrorMessage() {
        return message;
    }

    public int getErrorCode() {
        return code;
    }

    public Member getErrorData() throws NullPointerException {
        if (data == null) {throw new NullPointerException("No error data defined");}
        return data;
    }

    public boolean hasErrorData() {
        return data!=null;
    }

    JSONObject toJsonObj() throws JSONRPCException {
        // mandatory
        // if (code == null) {throw new JSONRPCException("Error code not defined");} code is no longer an Integer
        if (message == null) { throw new JSONRPCException("Error message not defined");}

        JSONObject object = new JSONObject();
        try {
            object.put(ErrMembers.CODE.toString(), code);
            object.put(ErrMembers.MESSAGE.toString(), message);
        } catch (JSONException e) {
            System.out.println(e.getMessage());
            return null;
        }
        if (data != null) { // optional
            putMember(object, ErrMembers.DATA.toString(), data);
        }

        return object;
    }

    public Error(JSONObject error) {
        this.obj = error;

        try {
            if (obj.has(ErrMembers.CODE.toString())) {
                code = error.getInt(ErrMembers.CODE.toString());
            } else {
                throw new InvalidParameterException("Error code not found");
            }
            if (obj.has(ErrMembers.MESSAGE.toString())) {
                message = error.getString(ErrMembers.MESSAGE.toString());
            } else {
                throw new InvalidParameterException("Error message not found");
            }

            if (obj.has(ErrMembers.DATA.toString())) {
                data = Member.toMember(obj.get(ErrMembers.DATA.toString()));
            } else {
                data = null;
            }
        } catch (JSONException e) {
            System.out.println(e.getMessage());
            throw new InvalidParameterException(e.getMessage());
        }

        // check that there are no other parameters
        if (!checkMembersSubset(ErrMembers.values(), obj)) {throw new InvalidParameterException("Unexpected paramater");}

        this.jsonRpcString = obj.toString();
    }

    JSONObject getJsonObj() {
        return this.obj;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Error)) return false;
        Error o = (Error) other;

        if (this.data == null)
            return this.code == o.code && this.message.equals(o.message) && o.data == null;
        else
            return this.code == o.code && this.message.equals(o.message) && this.data.equals(o.data);
    }
}