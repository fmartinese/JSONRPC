package jsonrpc;

import java.security.InvalidParameterException;

public abstract class AbstractRequest extends JsonRpcMessage {
    enum Members {
        JSONRPC("jsonrpc"), METHOD("method"), ID("id"), PARAMS("params");

        private final String text;
        Members(final String text) {
            this.text = text;
        }
        @Override
        public String toString() {return text;}
    }

    boolean notify;
    String method;
    StructuredMember params; // structured object that can be either an array or a key-value map

    AbstractRequest(String method, StructuredMember params, Id id) {
        this.notify = id == null;
        this.id = id;
        this.method = method;
        this.params = params;
        try {
            this.obj = toJsonObj();
        } catch (JSONRPCException e) {
            throw new InvalidParameterException(e.getMessage());
        }
        this.jsonRpcString = obj.toString();
    }

    AbstractRequest() {
        super();
    }

    public String getMethod() {
        return method;
    }

    public StructuredMember getParams() {
        return params;
    }

    public boolean isNotify() {
        return notify;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof AbstractRequest)) return false;
        AbstractRequest o = (AbstractRequest) other;

        if (this.id != null) {
            if (!this.id.equals(o.id)) {return false;}
        } else {
            if (o.id != null) {return false;}
        }

        if (this.params == null)
            return this.notify == o.notify && this.method.equals(o.method) && o.params == null;
        else
            return this.notify == o.notify && this.method.equals(o.method) && this.params.equals(o.params);
    }
}