package net.es.maddash.madalert;

import java.util.List;

/**
 * An interface for defining meshes independent of data source
 * 
 * @author alake
 *
 */
public interface Mesh {
    
    /**
     * Returns if there is more than one check in cells
     * @return true if more than one check in cells, false otherwise
     */
    public boolean isSplitCell();
    
    /**
     * Returns list of all unique names found in rows and columns
     * @return list of all unique names found in rows and columns
     */
    public List<String> getAllNames();
    
    /**
     * Returns list of all the column names
     * @return List of all the column names
     */
    public List<String> getColumnNames();
    
    /**
     * Returns list of all the row names
     * @return List of all the row names
     */
    public List<String> getRowNames();
    
    /**
     * Returns a count of the number of checks
     * @return List of all the row names
     */
    public int getCheckCount();
    
    /**
     * Returns a list of human-readable labels for each possible status
     * @return a list of human-readable labels for each possible status
     */
    public List<String> getStatusLabels();
    
    /**
     * Returns the URL of the mesh. This can be null.
     * @return the URL of the mesh or null if not specified
     */
    public String getLocation();
    
    /**
     * Returns the name of the mesh
     * @return the name of the mesh
     */
    public String getName();
    
    /**
     * Returns the number of severity levels
     * @return the number of severity levels
     */
    public int nSeverityLevels();
    
    /**
     * The status of the check at the given point in the grid.
     * @param row the row index
     * @param column the column index
     * @param check the check index
     * @return the status of the given check
     */
    public int statusFor(int row, int column, int check);
    
    /**
     * Returns true if there is a non-null value at the given row and column
     * @param row the row index
     * @param column the column index 
     * @return true if there is a non-null value at the given row and column
     */
    public boolean hasColumn(int row, int column);
}
