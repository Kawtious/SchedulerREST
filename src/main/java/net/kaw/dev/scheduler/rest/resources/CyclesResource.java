/*
 * MIT License
 * 
 * Copyright (c) 2023 Kawtious
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, career to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.kaw.dev.scheduler.rest.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.kaw.dev.scheduler.data.Cycle;
import net.kaw.dev.scheduler.data.factories.MappableFactory;
import net.kaw.dev.scheduler.exceptions.InvalidDataException;
import net.kaw.dev.scheduler.persistence.sql.SQLControl;
import net.kaw.dev.scheduler.utils.JSONUtils;

@Path("cycles")
public class CyclesResource {

    public CyclesResource() {
    }

    @GET
    @Path(value = "/get/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getCycle(@PathParam(value = "id") final String id) {
        return doGetCycle(id);
    }

    @GET
    @Path(value = "/current")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getCurrentCycle() {
        return doGetCurrentCycle();
    }

    @GET
    @Path(value = "/dummy")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getDummyCycle() {
        return doGetDummyCycle();
    }

    @POST
    @Path(value = "/post")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response postCycle(final String jsonString) {
        return doPostCycle(jsonString);
    }

    @DELETE
    @Path(value = "/delete/{id}")
    public Response deleteCycle(@PathParam(value = "id") final String id) {
        return doDeleteCycle(id);
    }

    private Response doGetCycle(String id) {
        try {
            return ResponseManager.createResponse(200, JSONUtils.mapToJSON(SQLControl.Cycles.select(id).toMap()));
        } catch (SQLException | InvalidDataException ex) {
            Logger.getLogger(CyclesResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseManager.createResponse(200, "");
    }

    private Response doGetDummyCycle() {
        String dummyId = "dummy_cycle_id";
        return doGetCycle(dummyId);
    }

    private Response doGetCurrentCycle() {
        try {
            List<Cycle> cycles = SQLControl.Cycles.select();

            Date date = new Date();

            long timeMilli = date.getTime();

            Cycle currentCycle = cycles.get(0);

            for (Cycle cycle : cycles) {
                if (timeMilli >= cycle.getStart() && timeMilli <= cycle.getEnd()) {
                    currentCycle = cycle;
                }
            }

            return ResponseManager.createResponse(200, JSONUtils.mapToJSON(currentCycle.toMap()));
        } catch (SQLException | InvalidDataException ex) {
            Logger.getLogger(CyclesResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseManager.createResponse(200, "");
    }

    private Response doPostCycle(String jsonString) {
        try {
            Cycle cycle = (Cycle) MappableFactory.build(MappableFactory.MappableType.CYCLE, JSONUtils.jsonToMap(jsonString));

            SQLControl.Cycles.insert(cycle);

            return ResponseManager.createResponse(200, true);
        } catch (SQLException | InvalidDataException ex) {
            Logger.getLogger(CyclesResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseManager.createResponse(200, false);
    }

    private Response doDeleteCycle(String id) {
        try {
            SQLControl.Cycles.delete(id);

            return ResponseManager.createResponse(200, true);
        } catch (SQLException ex) {
            Logger.getLogger(CyclesResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseManager.createResponse(200, false);
    }

}
