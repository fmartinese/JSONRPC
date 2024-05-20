package jsonrpc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.security.InvalidParameterException;
import java.util.ArrayList;

public class Batch { // public only for test
    private ArrayList<Request> reqs;
    private ArrayList<Response> resps;
    private boolean onlyNotifies;

    public Batch(JSONArray requestArray) { // public only for test
        setup(requestArray);
    }

    public Batch(ArrayList<Request> requests) { // public only for test
        JSONArray array = new JSONArray();
        for (Request r : requests) {
            array.put( r == null ? null : r.getObj() );
        }
        setup(array);
    }

    private void setup(JSONArray requestArray) {
        if (requestArray.length() == 0) {throw new InvalidParameterException("Empty array of requests");}
        reqs = new ArrayList<>();
        resps = new ArrayList<>();
        onlyNotifies = true;
        for (int i=0; i<requestArray.length(); i++) {
            Request req = null;
            Response resp = null;
            String stringReq = null;
            try {
                JSONObject o = requestArray.getJSONObject(i);
                stringReq = o.toString();
                req = new Request(stringReq);
                // resp = null;
                if (!req.isNotify()) {onlyNotifies = false;}
            } catch (InvalidParameterException | JSONException e) {
                Id id = stringReq != null ? Id.getIdFromRequest(stringReq) : new Id(); // attempt to retrieve the ID; otherwise, ID is null
                Error err = new Error(Error.Errors.INVALID_REQUEST);
                // req = null;
                resp = new Response(id, err);
                onlyNotifies = false;
            } finally {
                reqs.add(req);
                resps.add(resp);
            }
        }
    }

    private void put(Request req, Response resp) {
        int i = reqs.indexOf(req);
        resps.set(i, resp);
    }
    
    public void put(ArrayList<Response> responses) { // public only for test
        // responses must be passed in exact number (equal to the number of valid non-notification requests)
        int c; // count requests where corresponding responses should not be inserted because they are either invalid or notifications
        int i;
        for (i = 0, c = 0; i < responses.size() + c; i++) {
            Request req = reqs.get(i); // IndexOutOfBoundsException if there are too many responses
            if (req == null || req.isNotify()) {
                // there should not be a response to an invalid request or notification
                c++;
            } else {
                this.put(req, responses.get(i-c));
            }
        }
        for (; i < reqs.size(); i++) {
            // if there are still non-notification requests without a response assigned
            if (reqs.get(i)!=null && !reqs.get(i).isNotify()) {throw new IndexOutOfBoundsException("Not enough responses");}
            // too few responses
        }
    }

    void put(JSONArray responses) {
        ArrayList<Response> resps = new ArrayList<>();
        for (int i = 0; i<responses.length(); i++) {
            try {
                resps.add(new Response(responses.get(i).toString()));
            } catch (JSONException e) {
                System.out.println(e.getMessage());
            }
        }
        this.put(resps);
    }

    public ArrayList<Request> getAllRequests() { // public only for test
        return reqs;
    }

    public ArrayList<Request> getValidRequests() { // public only for test
        ArrayList<Request> rq = new ArrayList<>();
        for (Request r : reqs) {
            if (r!=null) {
                rq.add(r);
            }
        }
        return rq;
    }

    public ArrayList<Response> getAllResponses() { // public only for test
        return resps;
    }

    public ArrayList<Response> getValidResponses() { // public only for test
        ArrayList<Response> rp = new ArrayList<>();
        for (Response r : resps) {
            if (r!=null) {
                rp.add(r);
            }
        }
        return rp;
    }

    public String getResponseJSON() { // public only for test
        JSONArray arr = new JSONArray();
        for (Response r : resps) {
            if (r != null) { // responses to notifications are not sent
                arr.put(r.getObj());
            }
        }
        return arr.toString();
    }

    public String getRequestJSON() { // public only for test
        JSONArray arr = new JSONArray();
        for (Request r : reqs) {
            arr.put(r.getObj());
        }
        return arr.toString();
    }

    public boolean isOnlyNotifies() {
        return onlyNotifies;
    }
}