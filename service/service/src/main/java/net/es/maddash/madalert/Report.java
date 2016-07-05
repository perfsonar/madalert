/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.es.maddash.madalert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * The result of a Madalert analysis.
 *
 * @author carcassi
 */
public class Report {
    private final Map<String, List<Problem>> siteProblems = new HashMap<>();
    private final List<Problem> globalProblems = new ArrayList<>();
    private final int[] globalStats;
    private final Map<String, int[]> siteStats = new HashMap<>();
    private final List<String> sites;
    private final String meshName;
    private final String meshLocation;
    
    Report(Mesh mesh) {
        meshName = mesh.getName();
        meshLocation = mesh.getLocation();
        sites = mesh.getAllNames();
        globalStats = new int[mesh.nSeverityLevels()];
        //init stats
        for(String siteName : sites){
            siteStats.put(siteName, new int[mesh.nSeverityLevels()]);
        }
        //calc stats
        for (int row = 0; row < mesh.getRowNames().size(); row++) {
            String rowName = mesh.getRowNames().get(row);
            for (int column = 0; column < mesh.getColumnNames().size(); column++) {
                if(!mesh.hasColumn(row, column)){
                    //skip excluded columns
                    continue;
                }
                String colName = mesh.getColumnNames().get(column);
                for (int check = 0; check < mesh.getCheckCount(); check++){
                    int status = mesh.statusFor(row, column, check);
                    globalStats[status]++;
                    siteStats.get(rowName)[status]++;
                    if(!rowName.equals(colName)){
                        //don't double count
                        siteStats.get(colName)[status]++;
                    }
                    
                }
            }
        }
    }
    
    void addProblem(String site, Problem problem) {
        List<Problem> problems = siteProblems.get(site);
        if (problems == null) {
            problems = new ArrayList<>();
            siteProblems.put(site, problems);
        }
        problems.add(problem);
    }
    
    void addGlobalProblem(Problem problem) {
        globalProblems.add(problem);
    }

    public List<Problem> getGlobalProblems() {
        return globalProblems;
    }
    
    public List<Problem> getSiteProblems(String site) {
        return (siteProblems.get(site) == null) ? new ArrayList<Problem>() : siteProblems.get(site);
    }
    
    public List<String> getSites() {
        return sites;
    }
    
    public int getGlobalMaxSeverity() {
        int max = 0;
        for(Problem gp: globalProblems){
            if(gp.getSeverity() > max){
                max = gp.getSeverity();
            }
        }
        return max;
    }
    
    public int getSiteMaxSeverity(String site) {
        int max = 0;
        for(Problem p: getSiteProblems(site)){
            if(p.getSeverity() > max){
                max = p.getSeverity();
            }
        }
        
        return max;
    }
    
    private static void addStats(JsonObjectBuilder jsonSite, int[] stats) {
        JsonArrayBuilder jsonGlobalStats = Json.createArrayBuilder();
        for (int i = 0; i < stats.length; i++) {
            jsonGlobalStats.add(stats[i]);
        }
        jsonSite.add("stats", jsonGlobalStats);
    }
    
    private static void addProblems(JsonObjectBuilder jsonSite, List<Problem> problems) {
        if (problems != null && !problems.isEmpty()) {
            JsonArrayBuilder globalProblems = Json.createArrayBuilder();
            for (Problem problem : problems) {
                JsonArrayBuilder solutions = Json.createArrayBuilder();
                for(String solution : problem.getSolutions()){
                    solutions.add(solution);
                }
                globalProblems.add(Json.createObjectBuilder()
                    .add("name", problem.getName())
                    .add("severity", problem.getSeverity())
                    .add("category", problem.getCategory())
                    .add("solutions", solutions));
            }
            jsonSite.add("problems", globalProblems);
        }
    }
    
    public JsonObject toJson() {
        JsonObjectBuilder root = Json.createObjectBuilder();
        JsonObjectBuilder mesh = Json.createObjectBuilder();
        mesh.add("name", meshName);
        if (meshLocation != null) {
            mesh.add("location", meshLocation);
        }
        root.add("mesh", mesh);
        JsonObjectBuilder globalSite = Json.createObjectBuilder();
        addStats(globalSite, globalStats);
        globalSite.add("severity", getGlobalMaxSeverity());
        addProblems(globalSite, getGlobalProblems());
        root.add("global", globalSite);

        JsonObjectBuilder jsonSites = Json.createObjectBuilder();
        for (String site : sites) {
            JsonObjectBuilder jsonSite = Json.createObjectBuilder();
            addStats(jsonSite, siteStats.get(site));
            jsonSite.add("severity", getSiteMaxSeverity(site));
            addProblems(jsonSite, siteProblems.get(site));
            jsonSites.add(site, jsonSite);
        }
        root.add("sites", jsonSites);
        return root.build();
    }
}
