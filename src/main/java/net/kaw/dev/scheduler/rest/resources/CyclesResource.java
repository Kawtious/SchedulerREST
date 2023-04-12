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
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.kaw.dev.scheduler.data.Cycle;
import net.kaw.dev.scheduler.data.factories.MappableFactory;
import net.kaw.dev.scheduler.persistence.sql.SQLControl;
import net.kaw.dev.scheduler.rest.Response;
import net.kaw.dev.scheduler.utils.JSONUtils;

@Path("cycles")
public class CyclesResource {

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public CyclesResource() {
    }

    @GET
    @Path(value = "/get/{id}")
    public void getCycle(@Suspended final AsyncResponse asyncResponse, @PathParam(value = "id") final String id) {
        executorService.submit(() -> {
            asyncResponse.resume(doGetCycle(id));
        });
    }

    @POST
    @Path(value = "/post")
    @Consumes(MediaType.TEXT_PLAIN)
    public void postCycle(@Suspended final AsyncResponse asyncResponse, final String jsonString) {
        executorService.submit(() -> {
            asyncResponse.resume(doPostCycle(jsonString));
        });
    }

    @DELETE
    @Path(value = "/delete/{id}")
    public void deleteCycle(@Suspended final AsyncResponse asyncResponse, @PathParam(value = "id") final String id) {
        executorService.submit(() -> {
            asyncResponse.resume(doDeleteCycle(id));
        });
    }

    private Cycle doGetCycle(String id) {
        try {
            return SQLControl.Cycles.select(id);
        } catch (SQLException ex) {
        }

        return null;
    }

    private Response doPostCycle(String jsonString) {
        Response response = new Response();

        try {
            Cycle cycle = (Cycle) MappableFactory.build(MappableFactory.MappableType.CYCLE, JSONUtils.jsonToMap(jsonString));

            SQLControl.Cycles.insert(cycle);

            response.setStatus(true);
            response.setMessage("Success");
        } catch (SQLException ex) {
            response.setStatus(false);
            response.setMessage("Something went wrong.");
        }

        return response;
    }

    private Response doDeleteCycle(String id) {
        Response response = new Response();

        try {
            SQLControl.Cycles.delete(id);

            response.setStatus(true);
            response.setMessage("Success");
        } catch (SQLException ex) {
            response.setStatus(false);
            response.setMessage("Something went wrong.");
        }

        return response;
    }

}
