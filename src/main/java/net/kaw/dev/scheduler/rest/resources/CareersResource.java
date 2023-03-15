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

import jakarta.websocket.server.PathParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.kaw.dev.scheduler.data.Career;
import net.kaw.dev.scheduler.rest.Response;
import net.kaw.dev.scheduler.sql.SQLControl;

@Path("careers")
public class CareersResource {

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Context
    private UriInfo context;

    public CareersResource() {
    }

    @DELETE
    @Path(value = "/delete/{id}")
    public void deleteCareer(@Suspended final AsyncResponse asyncResponse, @PathParam(value = "id") final String id) {
        executorService.submit(() -> {
            asyncResponse.resume(doDeleteCareer(id));
        });
    }

    @GET
    @Path(value = "/postDummy")
    @Produces(MediaType.APPLICATION_JSON)
    public void postDummyCareer(@Suspended final AsyncResponse asyncResponse, final Career career) {
        executorService.submit(() -> {
            asyncResponse.resume(doPostDummyCareer());
        });
    }

    private Response doDeleteCareer(String id) {
        Response response = new Response();

        try {
            SQLControl.Careers.delete(id);

            response.setStatus(true);
            response.setMessage("Success");
        } catch (SQLException ex) {
            response.setStatus(false);
            response.setMessage("Something went wrong.");
        }

        return response;
    }

    private Response doPostDummyCareer() {
        Response response = new Response();

        try {
            SQLControl.Careers.insert(new Career("123456", "IEC1", "EAC2", 0, 1));

            response.setStatus(true);
            response.setMessage("Success");
        } catch (SQLException ex) {
            response.setStatus(false);
            response.setMessage("Something went wrong.");
        }

        return response;
    }

}
