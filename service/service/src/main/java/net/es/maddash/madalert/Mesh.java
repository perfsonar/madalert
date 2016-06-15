/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.es.maddash.madalert;

import java.util.ArrayList;
import java.util.HashMap;
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
public class Mesh {

    private final JsonObject jObj;
    private final String meshLocation;

    private Mesh(JsonObject jObj, String meshLocation) {
        this.jObj = jObj;
        this.meshLocation = meshLocation;
    }
    
    public boolean isSplitCell() {
        return this.getCheckCount() != 1;
    }
    
    public List<String> getAllNames() {
        HashMap<String, Boolean> nameMap = new HashMap<String, Boolean>();
        List<String> nameList = new ArrayList<String>();
        for(String row : this.getRowNames()){
            nameMap.put(row, true);
        }
        for(String col : this.getColumnNames()){
            nameMap.put(col, true);
        }
        nameList.addAll(nameMap.keySet());
        return nameList;
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
    
    public int nSeverityLevels() {
        // TODO: get it from the mesh
        return 4;
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
    
    public static Mesh from(JsonObject jObj) {
        return new Mesh(jObj, null);
    }
    
    public static Mesh from(JsonObject jObj, String meshLocation) {
        return new Mesh(jObj, meshLocation);
    }
}
