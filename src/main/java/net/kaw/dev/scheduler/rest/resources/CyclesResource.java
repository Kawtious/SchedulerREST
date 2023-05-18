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
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.kaw.dev.scheduler.data.Cycle;
import net.kaw.dev.scheduler.data.factories.MappableFactory;
import net.kaw.dev.scheduler.exceptions.InvalidDataException;
import net.kaw.dev.scheduler.persistence.sql.SQLControl;
import net.kaw.dev.scheduler.persistence.sql.auth.AuthManager;
import net.kaw.dev.scheduler.rest.resources.utils.RequestUtils;
import net.kaw.dev.scheduler.rest.resources.utils.ResponseUtils;
import net.kaw.dev.scheduler.utils.JSONUtils;

@Path("cycles")
public class CyclesResource {

    public CyclesResource() {
    }

    @POST
    @Path(value = "/get")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    public Response getCycles(final String jsonString) {
        return doGetCycle(jsonString);
    }

    @POST
    @Path(value = "/current")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    public Response getCurrentCycle(final String jsonString) {
        return doGetCurrentCycle(jsonString);
    }

    @POST
    @Path(value = "/post")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response postCycle(final String jsonString) {
        return doPostCycle(jsonString);
    }

    @POST
    @Path(value = "/delete")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response deleteCycle(final String jsonString) {
        return doDeleteCycle(jsonString);
    }

    private Response doGetCycle(String jsonString) {
        try {
            if (!AuthManager.authenticate(jsonString, 0)) {
                return ResponseUtils.createResponse(ResponseUtils.FORBIDDEN);
            }

            Map<String, Object> map = RequestUtils.getMap(jsonString);

            if (map == null) {
                return doGetCycles();
            }

            if (!map.containsKey(Cycle.ID_KEY)) {
                return doGetCycles();
            }

            String id = (String) map.get(Cycle.ID_KEY);

            Cycle cycle = SQLControl.Cycles.select(id);

            if (cycle == null) {
                return ResponseUtils.createResponse(ResponseUtils.INTERNAL_SERVER_ERROR);
            }

            return ResponseUtils.createResponse(ResponseUtils.OK, JSONUtils.mapToJSON(cycle.toMap()));
        } catch (SQLException | InvalidDataException ex) {
            Logger.getLogger(CyclesResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseUtils.createResponse(ResponseUtils.INTERNAL_SERVER_ERROR);
    }

    private Response doGetCycles() {
        try {
            List<Cycle> cycles = SQLControl.Cycles.select();

            Map<String, Object> cyclesMap = new HashMap<>();

            // Todo: Order by period
            for (Cycle cycle : cycles) {
                cyclesMap.put(cycle.getId(), cycle.toMap());
            }

            return ResponseUtils.createResponse(ResponseUtils.OK, JSONUtils.mapToJSON(cyclesMap));
        } catch (SQLException | InvalidDataException ex) {
            Logger.getLogger(CyclesResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseUtils.createResponse(ResponseUtils.INTERNAL_SERVER_ERROR);
    }

    private Response doGetCurrentCycle(String jsonString) {
        try {
            if (!AuthManager.authenticate(jsonString, 0)) {
                return ResponseUtils.createResponse(ResponseUtils.FORBIDDEN);
            }

            List<Cycle> cycles = SQLControl.Cycles.select();

            Date date = new Date();

            long timeMilli = date.getTime();

            Cycle currentCycle = cycles.get(0);

            for (Cycle cycle : cycles) {
                if (timeMilli >= cycle.getStart() && timeMilli <= cycle.getEnd()) {
                    currentCycle = cycle;
                }
            }

            return ResponseUtils.createResponse(ResponseUtils.OK, JSONUtils.mapToJSON(currentCycle.toMap()));
        } catch (SQLException | InvalidDataException ex) {
            Logger.getLogger(CyclesResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseUtils.createResponse(ResponseUtils.INTERNAL_SERVER_ERROR);
    }

    private Response doPostCycle(String jsonString) {
        try {
            if (!AuthManager.authenticate(jsonString, 1)) {
                return ResponseUtils.createResponse(ResponseUtils.FORBIDDEN);
            }

            Cycle cycle = (Cycle) MappableFactory.build(MappableFactory.MappableType.CYCLE, JSONUtils.jsonToMap(jsonString));

            SQLControl.Cycles.insert(cycle);

            return ResponseUtils.createResponse(ResponseUtils.OK);
        } catch (SQLException | InvalidDataException ex) {
            Logger.getLogger(CyclesResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseUtils.createResponse(ResponseUtils.INTERNAL_SERVER_ERROR);
    }

    private Response doDeleteCycle(String jsonString) {
        try {
            if (!AuthManager.authenticate(jsonString, 1)) {
                return ResponseUtils.createResponse(ResponseUtils.FORBIDDEN);
            }

            Map<String, Object> map = RequestUtils.getMap(jsonString);

            if (map == null) {
                return ResponseUtils.createResponse(ResponseUtils.BAD_REQUEST);
            }

            if (!map.containsKey(Cycle.ID_KEY)) {
                return ResponseUtils.createResponse(ResponseUtils.BAD_REQUEST);
            }

            String id = (String) map.get(Cycle.ID_KEY);

            SQLControl.Cycles.delete(id);

            return ResponseUtils.createResponse(ResponseUtils.OK, true);
        } catch (SQLException ex) {
            Logger.getLogger(CyclesResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseUtils.createResponse(ResponseUtils.INTERNAL_SERVER_ERROR);
    }

}
