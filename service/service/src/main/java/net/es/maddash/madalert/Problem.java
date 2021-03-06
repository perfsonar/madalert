/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.es.maddash.madalert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A potential problem that may be included in the report.
 *
 * @author carcassi
 */
public class Problem {
    private final String name;
    private final int severity;
    private final String category;
    private final List<String> solutions;
    
    public Problem(String name, int severity, String category) {
        this(name, severity, category, new ArrayList<String>());
    }
    
    public Problem(String name, int severity, String category, List<String> solutions) {
        this.name = name;
        this.severity = severity;
        this.category = category;
        this.solutions = solutions;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getSeverity() {
        return severity;
    }
    
    public List<String> getSolutions() {
        return solutions;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.name);
        hash = 53 * hash + this.severity;
        hash = 53 * hash + Objects.hashCode(this.category);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Problem other = (Problem) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (this.severity != other.severity) {
            return false;
        }
        if (!Objects.equals(this.category, other.category)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[" + category + "](" + severity + ") " + name;
    }
    
}
