package net.nashlegend.request.parsers;

import net.nashlegend.request.JsonHandler;
import net.nashlegend.request.ResponseObject;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by NashLegend on 2015/9/23 0023.
 */
public class JsonArrayParser implements Parser<JSONArray> {
    @Override
    public JSONArray parse(String str, ResponseObject<JSONArray> responseObject) throws
            JSONException {
        return JsonHandler.getUniversalJsonArray(str, responseObject);
    }
}
