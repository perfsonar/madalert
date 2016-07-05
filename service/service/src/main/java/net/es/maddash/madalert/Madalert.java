/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.es.maddash.madalert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Madalert library, provides fluent API for matching problematic patterns on
 * MadDash.
 *
 * @author carcassi
 */
public class Madalert {
    public static final String INFRASTRUCTURE = "INFRASTRUCTURE";
    public static final String ACTUAL = "ACTUAL";
    public static final String RULE_DEFAULT_KEY = "default";
    private static Map<String, Rule> ruleMap = null;
    
    private Madalert() {
        // Prevent creation
    }
    
    public static void setRules(Map<String, Rule> newRuleMap){
        ruleMap = newRuleMap;
    }
    
    public static Rule lookupRule(String name) {
        if(ruleMap == null){
            return defaultRule();
        }else if(ruleMap.containsKey(name) && ruleMap.get(name) != null){
            return ruleMap.get(name);
        }else if(ruleMap.containsKey(RULE_DEFAULT_KEY) && ruleMap.get(RULE_DEFAULT_KEY) != null){
            return ruleMap.get(RULE_DEFAULT_KEY);
        }
        return defaultRule();
    }
    
    public static Rule defaultRule() {
        return matchFirst(
                          rule(forAllSites(), matchStatus(3), new Problem("Grid is down", 3, INFRASTRUCTURE)),
                          rule(forAllSites(), matchStatus(0), new Problem("No issues found", 0, INFRASTRUCTURE)),
                          forEachSite(
                                      matchFirst(
                                                 rule(forSite(), matchStatus(3), new Problem("Site is down", 3, INFRASTRUCTURE)),
                                                 matchAll(
                                                          matchFirst(
                                                                     rule(forCheck(0,1), matchStatus(3), new Problem("Site can't test", 3, INFRASTRUCTURE)),
                                                                     rule(forCheck(0,1), matchStatus(3, 0.7), new Problem("Site mostly can't test", 3, INFRASTRUCTURE))),
                                                          matchFirst(
                                                                     rule(forCheck(1,0), matchStatus(3), new Problem("Site can't be tested", 3, INFRASTRUCTURE)),
                                                                     rule(forCheck(1,0), matchStatus(3, 0.7), new Problem("Site mostly can't be tested", 3, INFRASTRUCTURE))),
                                                          rule(forRow(), matchStatus(new double[]{0.0, 0.5, 1.0, -1.0}, 0.7), new Problem("Outgoing test failures", 2, ACTUAL)),
                                                          rule(forColumn(), matchStatus(new double[]{0.0, 0.5, 1.0, -1.0}, 0.7), new Problem("Incoming test failures", 2, ACTUAL))))));
    }
    
    public static Rule rule(final TestSet testSet, final StatusMatcher matcher, final Problem problem) {
        return new Rule() {

            @Override
            boolean addToReport(Report report, Mesh mesh) {
                if (testSet.match(mesh, matcher)) {
                    report.addGlobalProblem(problem);
                    return true;
                } else {
                    return false;
                }
            }
            
        };
    }
    
    public static SiteRule rule(final SiteTestSet siteTestSet, final StatusMatcher matcher, final Problem problem) {
        return new SiteRule() {

            @Override
            public Rule site(final String site) {
                return new Rule() {

                    @Override
                    boolean addToReport(Report report, Mesh mesh) {
                        TestSet testSet = siteTestSet.site(site);
                        if (testSet.match(mesh, matcher)) {
                            report.addProblem(site, problem);
                            return true;
                        } else {
                            return false;
                        }
                    }
                };
            }
            
        };
    }
    
    public static Rule forEachSite(final SiteRule siteRule) {
        return new Rule() {

            @Override
            boolean addToReport(Report report, Mesh mesh) {
                boolean matched = false;
                for (String site : mesh.getAllNames()) {
                    Rule rule = siteRule.site(site);
                    matched = rule.addToReport(report, mesh) || matched;
                }
                return matched;
            }
        };
    }
    
    public static Rule matchFirst(Rule... rules) {
        return matchFirst(Arrays.asList(rules));
    }
    
    public static Rule matchFirst(final List<Rule> rules) {
        return new Rule() {

            @Override
            boolean addToReport(Report report, Mesh mesh) {
                for (Rule rule : rules) {
                    if (rule.addToReport(report, mesh)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
    
    public static SiteRule matchFirst(final SiteRule... siteRules) {
        return new SiteRule() {

            @Override
            public Rule site(String site) {
                List<Rule> rules = new ArrayList<Rule>();
                for(SiteRule sr : Arrays.asList(siteRules)){
                    rules.add(sr.site(site));
                }
                return matchFirst(rules);
            }
            
        };
    }
    
    public static Rule matchAll(Rule... rules) {
        return matchAll(Arrays.asList(rules));
    }
    
    public static Rule matchAll(final List<Rule> rules) {
        return new Rule() {

            @Override
            boolean addToReport(Report report, Mesh mesh) {
                boolean matched = false;
                for (Rule rule : rules) {
                    matched = rule.addToReport(report, mesh) || matched;
                }
                return matched;
            }
        };
    }
    
    public static SiteRule matchAll(final SiteRule... siteRules) {
        return new SiteRule() {

            @Override
            public Rule site(String site) {
                List<Rule> rules = new ArrayList<Rule>();
                for(SiteRule sr : Arrays.asList(siteRules)){
                    rules.add(sr.site(site));
                }
                return matchAll(rules);
            }
            
        };
    }
    
    public static TestSet forAllSites() {
        return new TestSet() {

            @Override
            boolean match(Mesh mesh, StatusMatcher matcher) {
                StatusMatcher.Instance instance = matcher.prepareInstance(mesh);
                for (int row = 0; row < mesh.getRowNames().size(); row++) {
                    for (int col = 0; col < mesh.getColumnNames().size(); col++) {
                        if(mesh.hasColumn(row, col)){
                            for(int check = 0; check < mesh.getCheckCount(); check++){
                                instance.match(row, col, check,
                                    mesh.statusFor(row, col, check));
                            }
                        }
                    }
                }
                return instance.isMatched();
            }
        };
    }
    
    public static StatusMatcher matchStatus(final int status) {
        return new StatusMatcher() {

            @Override
            public StatusMatcher.Instance prepareInstance(Mesh mesh) {
                return new StatusMatcher.Instance() {
                    
                    //this is an all or nothing match so default to true
                    private boolean result = true;
                    //...but make sure we don't mark stuff matched if we never looked at it
                    private boolean checked = false;
                    
                    @Override
                    public void match(int row, int column, int check, int matchStatus) {
                        checked = true;
                        if (status != matchStatus) {
                            result = false;
                        }
                    }

                    @Override
                    public boolean isMatched() {
                        return (checked && result);
                    }
                };
            }
        };
    }
    
    public static StatusMatcher matchStatus(final int status, final double threshold) {
        return new StatusMatcher() {

            @Override
            public StatusMatcher.Instance prepareInstance(Mesh mesh) {
                return new StatusMatcher.Instance() {
            
                    private double matches = 0.0;
                    private double total = 0.0;

                    @Override
                    public void match(int row, int column, int check, int matchStatus) {
                        total += 1.0;
                        if (status == matchStatus) {
                            matches += 1.0;
                        }
                    }

                    @Override
                    public boolean isMatched() {
                        return (total != 0.0 && (matches / total) >= threshold);
                    }
                };
            }
        };
    }
    
    public static StatusMatcher matchStatus(final double[] weights, final double threshold) {
        return new StatusMatcher() {

            @Override
            public StatusMatcher.Instance prepareInstance(Mesh mesh) {
                return new StatusMatcher.Instance() {
            
                    private double matches = 0.0;
                    private double total = 0.0;

                    @Override
                    public void match(int row, int column, int check, int matchStatus) {
                        if(matchStatus >= weights.length){
                            return;
                        }
                        double weight = weights[matchStatus];
                        if (weight >= 0.0 && weight <= 1.0) {
                            total += 1.0;
                            matches += weight;
                        }
                    }

                    @Override
                    public boolean isMatched() {
                        return (total != 0.0 && (matches / total) >= threshold);
                    }
                };
            }
        };
    }
    
    public static SiteTestSet forSite() {
        return new SiteTestSet() {

            @Override
            TestSet site(final String site) {
                return new TestSet() {

                    @Override
                    boolean match(Mesh mesh, StatusMatcher matcher) {
                        StatusMatcher.Instance instance = matcher.prepareInstance(mesh);
                        for (int row = 0; row < mesh.getRowNames().size(); row++) {
                            String rowName = mesh.getRowNames().get(row);
                            for (int col = 0; col < mesh.getColumnNames().size(); col++) {
                                String colName = mesh.getColumnNames().get(col);
                                if(mesh.hasColumn(row, col) && (rowName.equals(site) || colName.equals(site))){
                                    for(int check = 0; check < mesh.getCheckCount(); check++){
                                        instance.match(row, col, check,
                                            mesh.statusFor(row, col, check));
                                    }
                                }
                            }
                        }
                        return instance.isMatched();
                    }
                };
            }
        };
    }
    
    public static SiteTestSet forRow() {
        return new SiteTestSet() {

            @Override
            TestSet site(final String site) {
                return new TestSet() {

                    @Override
                    boolean match(Mesh mesh, StatusMatcher matcher) {
                        StatusMatcher.Instance instance = matcher.prepareInstance(mesh);
                        for (int row = 0; row < mesh.getRowNames().size(); row++) {
                            String rowName = mesh.getRowNames().get(row);
                            if(site.equals(rowName)){
                                for (int col = 0; col < mesh.getColumnNames().size(); col++) {
                                    if(mesh.hasColumn(row, col)){
                                        for (int check = 0; check < mesh.getCheckCount(); check++) {
                                            instance.match(row, col, check,
                                                    mesh.statusFor(row, col, check));
                                        }
                                    }
                                }
                            }
                        }
                        return instance.isMatched();
                    }
                };
            }
        };
    }
    
    public static SiteTestSet forColumn() {
        return new SiteTestSet() {

            @Override
            TestSet site(final String site) {
                return new TestSet() {

                    @Override
                    boolean match(Mesh mesh, StatusMatcher matcher) {
                        StatusMatcher.Instance instance = matcher.prepareInstance(mesh);
                        for (int row = 0; row < mesh.getRowNames().size(); row++) {
                            for (int col = 0; col < mesh.getColumnNames().size(); col++) {
                                String colName = mesh.getColumnNames().get(col);
                                if(site.equals(colName) && mesh.hasColumn(row, col)){
                                    for (int check = 0; check < mesh.getCheckCount(); check++) {
                                        instance.match(row, col, check,
                                                mesh.statusFor(row, col, check));
                                    }
                                }
                            }
                        }
                        return instance.isMatched();
                    }
                };
            }
        };
    }
    
    public static SiteTestSet forCell(final String rowSite, final String colSite, final int rowCheck, final int colCheck) {
        return new SiteTestSet() {

            @Override
            TestSet site(final String site) {
                return new TestSet() {

                    @Override
                    boolean match(Mesh mesh, StatusMatcher matcher) {
                        StatusMatcher.Instance instance = matcher.prepareInstance(mesh);
                        for (int row = 0; row < mesh.getRowNames().size(); row++) {
                            String rowName = mesh.getRowNames().get(row);
                            if(!rowName.equals(site) && !rowName.equals(rowSite)){
                                continue;
                            }
                            for (int col = 0; col < mesh.getColumnNames().size(); col++) {
                                String colName = mesh.getColumnNames().get(col);
                                if(rowName.equals(site) && !colName.equals(colSite)){
                                    continue;
                                }else if(rowName.equals(rowSite) && !colName.equals(site)){
                                    continue;
                                }else if( mesh.hasColumn(row, col)){
                                    if(rowName.equals(rowSite) && rowCheck >= 0){
                                        instance.match(row, col, rowCheck, mesh.statusFor(row, col, rowCheck));
                                    }else if(colName.equals(colSite) && colCheck >= 0){
                                        instance.match(row, col, colCheck, mesh.statusFor(row, col, colCheck));
                                    }else{
                                        for (int check = 0; check < mesh.getCheckCount(); check++) {
                                            instance.match(row, col, check, mesh.statusFor(row, col, check));
                                        }
                                    }
                                }
                            }
                        }
                        return instance.isMatched();
                    }
                };
            }
        };
    }
    
    public static SiteTestSet forCheck(final int rowCheck, final int colCheck) {
        return new SiteTestSet() {

            @Override
            TestSet site(final String site) {
                return new TestSet() {

                    @Override
                    boolean match(Mesh mesh, StatusMatcher matcher) {
                        StatusMatcher.Instance instance = matcher.prepareInstance(mesh);
                        for (int row = 0; row < mesh.getRowNames().size(); row++) {
                            String rowName = mesh.getRowNames().get(row);
                            for (int col = 0; col < mesh.getColumnNames().size(); col++) {
                                String colName = mesh.getColumnNames().get(col);
                                if( mesh.hasColumn(row, col)){
                                    if(rowCheck >= 0 && site.equals(rowName)){
                                        instance.match(row, col, rowCheck,
                                                mesh.statusFor(row, col, rowCheck));
                                    }
                                    //only add column check, if different row name or same row but different check
                                    if(colCheck >= 0 && site.equals(colName) && !(rowName.equals(colName) && rowCheck == colCheck)){
                                        instance.match(row, col, colCheck,
                                                mesh.statusFor(row, col, colCheck));
                                    }
                                }
                            }
                        }
                        return instance.isMatched();
                    }
                };
            }
        };
    }
}