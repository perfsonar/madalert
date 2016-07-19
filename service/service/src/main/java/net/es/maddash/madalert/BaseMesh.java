package net.es.maddash.madalert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Abstract class with some basic implementation for a few of the common Mesh methods
 * 
 * @author alake
 *
 */
public abstract class BaseMesh implements Mesh{
    
    @Override
    public boolean isSplitCell() {
        return this.getCheckCount() != 1;
    }
    
    @Override
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
    
    @Override
    public int nSeverityLevels() {
        //make sure we include a slot for each default state even if they don't all have labels
        return this.getStatusLabels().size() > 6 ? this.getStatusLabels().size() : 6;
    }
}
