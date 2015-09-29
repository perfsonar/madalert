/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.es.maddash.madalert;

import java.util.Arrays;
import java.util.List;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author carcassi
 */
public class MadalertTest {

    @Test
    public void defaultRule1() {
        try (JsonReader reader = Json.createReader(getClass().getResourceAsStream("allWell.json"))) {
            Mesh mesh = Mesh.from(reader.readObject());
            Report report = Madalert.defaultRule().createReport(mesh);
            assertThat(report.getGlobalProblems().size(), equalTo(1));
            assertThat(report.getGlobalProblems().get(0), equalTo(new Problem("No issues found", 0, Madalert.INFRASTRUCTURE)));
            for (int i = 0; i < report.getSites().size(); i++) {
                assertThat(report.getSiteProblems(i).isEmpty(), equalTo(true));
            }
        }
    }

}
