package jsonrpc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Test {
    public static void main(String args[]) {
        //testBatch();
        /*
        testRequestFromString();
        testResponseFromString();
        testRequestFromParams();
        testResponseFromParams();
        */
    }

    private static void testRequestFromParams() {
        ArrayList<StructuredMember> params = new ArrayList<>();
        params.add(null); // correct way to omit optional parameters
        ArrayList<Member> pl = new ArrayList<>();
        HashMap<String, Member> hm = new HashMap<>();

        pl.add(new Member());
        pl.add(new Member("aaaa"));
        pl.add(new Member(3));
        pl.add(new Member(2.75));
        pl.add(new Member(false));
        params.add(new StructuredMember(pl));

        int key = 0;
        for (Member t : pl) {
            hm.put("key"+key, t);
            key++;
        }
        params.add(new StructuredMember(hm));

        ArrayList<Member> memberlist = new ArrayList<>();
        HashMap<String, Member> membermap = new HashMap<>();
        Member m1 = new Member(1);
        Member m2 = new Member("prova");
        memberlist.add(m1);
        memberlist.add(m2);
        membermap.put("numero", m1);
        membermap.put("stringa", m2);

        ArrayList<Member> n_arr = new ArrayList<>();
        n_arr.add(new Member(10));
        n_arr.add(new Member(11));
        n_arr.add(new Member(12));
        Member numarray = new Member(new StructuredMember(n_arr));
        memberlist.add(numarray);
        membermap.put("array", numarray);

        HashMap<String, Member> b_map = new HashMap<>();
        b_map.put("b1",new Member(false));
        b_map.put("b2",new Member(true));
        Member boolmap = new Member(new StructuredMember(b_map));
        memberlist.add(boolmap);
        membermap.put("map", boolmap);

        params.add(new StructuredMember(memberlist));
        params.add(new StructuredMember(membermap));

        String method = "testmetodo";
        ArrayList<Id> ids = new ArrayList<>();
        ids.add(new Id());
        ids.add(new Id(1));
        ids.add(new Id("testid"));

        for (StructuredMember p : params) {
            for (Id i : ids) {
                AbstractRequest req = new Request(method, p, i);
                AbstractRequest notify = new Request(method, p);
                readReq(req);
                System.out.println(System.lineSeparator());
                readReq(notify);

                System.out.println(System.lineSeparator());
                System.out.println(System.lineSeparator());
                System.out.println(System.lineSeparator());
            }
        }
    }

    private static void testResponseFromParams() {
        ArrayList<Id> ids= new ArrayList<>();
        ids.add(new Id(1));
        ids.add(new Id("testid"));
        ids.add(new Id());

        ArrayList<Member> results = new ArrayList<>();
        results.add(new Member());
        results.add(new Member(false));

        for (Id i : ids) {
            for (Member res : results) {
                AbstractResponse resp = new Response(i, res);
                readResp(resp);
            }
        }

        ArrayList<Error> errors = new ArrayList<>();
        errors.add(new Error(Error.Errors.METHOD_NOT_FOUND));
        errors.add(new Error(Error.Errors.METHOD_NOT_FOUND, null));
        errors.add(new Error(Error.Errors.METHOD_NOT_FOUND, new Member(1)));
        errors.add(new Error("message", 0));
        errors.add(new Error("msg", 0, null));
        errors.add(new Error("msg",0, new Member(55)));
        errors.add(new Error("msg", 0, new Member()));

        for (Error e : errors) {
            AbstractResponse resp = new Response(new Id(1), e);
            readResp(resp);
        }

    }

    private static void testResponseFromString() {
        ArrayList<String> testStrings = new ArrayList<>();
        testStrings.add("{\"jsonrpc\": \"2.0\", \"result\": 19, \"id\": 1}");
        testStrings.add("{\"jsonrpc\": \"2.0\", \"error\": {\"code\": -32601, \"message\": \"Method not found\"}, \"id\": \"1\"}");
        testStrings.add("{\"jsonrpc\": \"2.0\", \"error\": {\"code\": -32700, \"message\": \"Parse error\"}, \"id\": null}");
        testStrings.add("");
        testStrings.add("test");
        testStrings.add("{\"result\": 19, \"id\": 1}");
        testStrings.add("{\"jsonrpc\": \"2.0\", \"result\": 19, \"id\": 1}, \"unexpected\": \"member\"");
        testStrings.add("{\"jsonrpc\": \"2\", \"result\": 19, \"id\": 1}");
        testStrings.add("{\"jsonrpc\": \"2.0\", \"result\": 19, \"id\": \"stringid\"}");

        for (String ts : testStrings) {
            System.out.println(ts);
            readResp(new Response(ts));
            System.out.println(System.lineSeparator());
            System.out.println(System.lineSeparator());
            System.out.println(System.lineSeparator());
        }
    }

    private static void readResp(AbstractResponse resp) {
        try {
            switch (resp.getId().getType()) {
                case NULL:
                    System.out.println("ID nullo");
                    break;
                case INT:
                    System.out.println("ID: " + String.valueOf(resp.getId().getInt()));
                    break;
                case STRING:
                    System.out.println("ID: " + resp.getId().getString());
            }

            if (resp.hasError()) {
                Error e = resp.getError();
                System.out.println("Error code: " + e.getErrorCode());
                System.out.println("Error message: " + e.getErrorMessage());
                if (e.hasErrorData()) {
                    System.out.println("Error data: " + readMember(e.getErrorData()));
                }
            } else {
                System.out.println("Risultato: " + readMember(resp.getResult()));
            }
        } catch (Exception e) {
            System.out.println("NON COSTRUTTORE: " + e.getClass().toString() + " - " + e.getMessage());
        }
    }

    private static void readReq(AbstractRequest req) {
        try {
            System.out.println("Notifica: " + req.isNotify());
            if (!req.isNotify()) {
                switch (req.getId().getType()) {
                    case NULL:
                        System.out.println("ID nullo");
                        break;
                    case INT:
                        System.out.println("ID: " + String.valueOf(req.getId().getInt()));
                        break;
                    case STRING:
                        System.out.println("ID: " + req.getId().getString());
                }
            }
            System.out.println("Method: " + req.getMethod());
            System.out.println("Params: " + readStructured(req.getParams()));
        } catch (Exception e) {
            System.out.println("NON COSTRUTTORE: " + e.getClass().toString() + " - " + e.getMessage());
        }
    }

    private static void testRequestFromString() {
        ArrayList<String> testStrings = new ArrayList<>();
        testStrings.add("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": [42, 23], \"id\": 1}");
        testStrings.add("{\"jsonrpc\": \"2.0\", \"method\": \"update\", \"params\": [1,2,3,4,5]}");
        testStrings.add("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": {\"subtrahend\": 23, \"minuend\": 42}, \"id\": 3}");
        testStrings.add("{\"jsonrpc\": \"2.0\", \"method\": \"test\", \"params\": {\"subtrahend\": 23, \"minuend\": 42, \"subobj\": {\"par1\": 34, \"par2\": \"value\", \"array\": [1,2,3]}}, \"id\": 3}");
        testStrings.add("");
        testStrings.add("test");
        testStrings.add("{\"method\": \"subtract\", \"params\": [42, 23], \"id\": 1}"); // no jsonrpc 2.0
        testStrings.add("{\"jsonrpc\": \"3.0\", \"method\": \"subtract\", \"params\": [42, 23], \"id\": 1}"); // jsonrpc <> 2.0
        testStrings.add("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": 42, \"id\": 1}"); // unstructured parameters
        testStrings.add("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": [42, 23], \"id\": null}"); // null ID

        for (String ts : testStrings) {
            System.out.println(ts);
            readReq(new Request(ts));
            System.out.println(System.lineSeparator());
            System.out.println(System.lineSeparator());
            System.out.println(System.lineSeparator());

        }
    }

    private static String readStructured(StructuredMember params) throws JSONException, JSONRPCException {
        if (params == null) {return "";}
        if (params.isArray()) {
            return readArray(params.getJSONArray());
        } else {
            return readObj(params.getJSONObject());
        }

    }

    private static String readArray(JSONArray params) throws JSONRPCException {
        StringBuilder val= new StringBuilder();
        for (int i = 0; i < params.length(); i++) {
            try {
                val.append(readMember(Member.toMember(params.get(i))));
            } catch (JSONException e) {
                val.append(e.getMessage());
            }
            val.append(System.lineSeparator());
        }
        return val.toString();
    }

    private static String readObj(JSONObject params) throws JSONRPCException {
        StringBuilder val= new StringBuilder();
        Iterator<?> keys = params.keys();

        while( keys.hasNext() ) {
            String key = (String)keys.next();
            val.append(key);
            val.append(": ");
            try {
                val.append(readMember(Member.toMember(params.get(key))));
            } catch (JSONException e) {
                val.append(e.getMessage());
            }
            val.append(System.lineSeparator());
        }

        return val.toString();
    }

    private static String readMember(Member m) throws JSONRPCException {
        switch (m.getType()) {
            case NULL: return "NULL";
            case STRING: return m.getString();
            case NUMBER: return String.valueOf(m.getNumber());
            case BOOL: return String.valueOf(m.getBool());
            case OBJ: return readObj(m.getJSONObj());
            case ARRAY: return readArray(m.getJSONArray());
            default: throw new JSONRPCException("Unexpected member type");
        }
    }
}
