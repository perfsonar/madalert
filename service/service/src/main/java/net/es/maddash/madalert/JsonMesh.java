/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.es.maddash.madalert;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * A mesh.
 * <p>
 * The actual data of the mash is represented internally by one of the JSON
 * specifications. This class provides JSON to Java bindings and other
 * view-like functionality.
 *
 * @author carcassi
 */
public class JsonMesh extends BaseMesh{

    private final JsonObject jObj;
    private final String meshLocation;

    private JsonMesh(JsonObject jObj, String meshLocation) {
        this.jObj = jObj;
        this.meshLocation = meshLocation;
    }
    
    public List<String> getColumnNames() {
        return JsonUtil.toListString(jObj.getJsonArray("columnNames"));
    }
    
    public List<String> getRowNames() {
        JsonArray rows = jObj.getJsonArray("rows");
        ArrayList<String> rowNames = new ArrayList<String>();
        for(int i = 0; i < rows.size(); i++){
            rowNames.add(rows.getJsonObject(i).getJsonObject("props").getString("label"));
        }
        return rowNames;
    }
    
    public int getCheckCount() {
        return jObj.getJsonArray("checkNames").size();
    }
    
    public List<String> getStatusLabels() {
        return JsonUtil.toListString(jObj.getJsonArray("statusLabels"));
    }

    public String getLocation() {
        return meshLocation;
    }
    
    public String getName() {
        return jObj.getString("name");
    }
    
    public JsonObject toJson() {
        return jObj;
    }
    
    public int statusFor(int row, int column, int check) {
        return jObj.getJsonArray("grid").getJsonArray(row).getJsonArray(column).getJsonObject(check).getInt("status");
    }
    
    public boolean hasColumn(int row, int column){
        return jObj.getJsonArray("grid").getJsonArray(row).get(column) != JsonValue.NULL;
    }
    
    public static JsonMesh from(JsonObject jObj) {
        return new JsonMesh(jObj, null);
    }
    
    public static JsonMesh from(JsonObject jObj, String meshLocation) {
        return new JsonMesh(jObj, meshLocation);
    }
}
