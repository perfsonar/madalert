/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.es.maddash.madalert;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

/**
 * A mesh diff.
 *
 * @author carcassi
 */
public class MeshDiff {
    
    static JsonMesh calculateDiff(JsonMesh mesh1, JsonMesh mesh2) {
        if (!mesh1.getRowNames().equals(mesh2.getRowNames())) {
            throw new RuntimeException("Cannot diff two meshes with a different set of rows");
        }
        if (!mesh1.getColumnNames().equals(mesh2.getColumnNames())) {
            throw new RuntimeException("Cannot diff two meshes with a different set of columns");
        }
        if (!mesh1.getStatusLabels().equals(mesh2.getStatusLabels())) {
            throw new RuntimeException("Cannot diff two meshes with a different status labels");
        }
        if (mesh1.isSplitCell() != mesh2.isSplitCell()) {
            throw new RuntimeException("Cannot diff two meshes if they aren't both split-cells");
        }
        JsonObjectBuilder diffMesh = Json.createObjectBuilder();
        diffMesh.add("name", "Diff between (" + mesh1.getName() + ") and (" + mesh2.getName() + ")");
        diffMesh.add("statusLabels", mesh1.toJson().getJsonArray("statusLabels"));
        diffMesh.add("lastUpdateTime", mesh1.toJson().getJsonNumber("lastUpdateTime"));
        diffMesh.add("columnNames", mesh1.toJson().getJsonArray("columnNames"));
        diffMesh.add("columnProps", mesh1.toJson().getJsonArray("columnProps"));
        diffMesh.add("checkNames", mesh1.toJson().getJsonArray("checkNames"));
        JsonArrayBuilder resultGrid = Json.createArrayBuilder();
        for (int row = 0; row < mesh1.getRowNames().size(); row++) {
            JsonArrayBuilder resultRow = Json.createArrayBuilder();
            for (int column = 0; column < mesh1.getColumnNames().size(); column++) {
                if (mesh1.hasColumn(row, column) != mesh2.hasColumn(row, column)) {
                    resultRow.add(Json.createArrayBuilder().add(Json.createObjectBuilder().add("message", "Difference found. Missing column.")
                            .add("status", -1)
                            .add("prevCheckTime", 0)
                            .add("uri", ""))
                            .add(Json.createObjectBuilder().add("message", "Difference found. Missing column.")
                                    .add("status", -1)
                                    .add("prevCheckTime", 0)
                                    .add("uri", "")));
                    continue;
                }
                for (int check = 0; check < mesh1.getCheckCount(); check++) {
                    if (mesh1.statusFor(row, column, check)
                            == mesh2.statusFor(row, column, check)) {
                        resultRow.add(Json.createArrayBuilder().add(Json.createObjectBuilder().add("message", "No difference")
                                .add("status", mesh1.getStatusLabels().size() - 1)
                                .add("prevCheckTime", 0)
                                .add("uri", ""))
                                .add(Json.createObjectBuilder().add("message", "No difference")
                                        .add("status", mesh1.getStatusLabels().size() - 1)
                                        .add("prevCheckTime", 0)
                                        .add("uri", "")));
                    } else {
                        resultRow.add(Json.createArrayBuilder().add(Json.createObjectBuilder().add("message", "Difference found")
                                .add("status", mesh1.statusFor(row, column,check))
                                .add("prevCheckTime", 0)
                                .add("uri", ""))
                                .add(Json.createObjectBuilder().add("message", "Difference found")
                                        .add("status", mesh2.statusFor(row, column, check))
                                        .add("prevCheckTime", 0)
                                        .add("uri", "")));
                    }
                }
                resultGrid.add(resultRow);
            }
        }
        diffMesh.add("grid", resultGrid);
        diffMesh.add("rows", mesh1.toJson().getJsonArray("rows"));
        
        return JsonMesh.from(diffMesh.build());
    }
    
    public static JsonMesh diff(JsonMesh mesh1, JsonMesh mesh2) {
        return calculateDiff(mesh1, mesh2);
    }
}
