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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.kaw.dev.scheduler.data.HalfHour;
import net.kaw.dev.scheduler.data.ScheduleMap;
import net.kaw.dev.scheduler.data.Teacher;
import net.kaw.dev.scheduler.data.factories.MappableFactory;
import net.kaw.dev.scheduler.persistence.sql.SQLControl;
import net.kaw.dev.scheduler.rest.Response;
import net.kaw.dev.scheduler.utils.JSONUtils;

@Path("teachers")
public class TeachersResource {

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public TeachersResource() {
    }

    @GET
    @Path(value = "/get/{id}")
    public void getTeacher(@Suspended final AsyncResponse asyncResponse, @PathParam(value = "id") final String id) {
        executorService.submit(() -> {
            asyncResponse.resume(doGetTeacher(id));
        });
    }

    @POST
    @Path(value = "/post")
    @Consumes(MediaType.TEXT_PLAIN)
    public void postTeacher(@Suspended final AsyncResponse asyncResponse, final String jsonString) {
        executorService.submit(() -> {
            asyncResponse.resume(doPostTeacher(jsonString));
        });
    }

    @DELETE
    @Path(value = "/delete/{id}")
    public void deleteTeacher(@Suspended final AsyncResponse asyncResponse, @PathParam(value = "id") final String id) {
        executorService.submit(() -> {
            asyncResponse.resume(doDeleteTeacher(id));
        });
    }

    private Teacher doGetTeacher(String id) {
        try {
            return SQLControl.Teachers.select(id);
        } catch (SQLException ex) {
        }

        return null;
    }

    private Response doPostTeacher(String jsonString) {
        Response response = new Response();

        try {
            Teacher teacher = (Teacher) MappableFactory.build(MappableFactory.MappableType.TEACHER, JSONUtils.jsonToMap(jsonString));

            SQLControl.Teachers.insert(teacher);

            List<ScheduleMap> scheduleMaps = teacher.getScheduleMaps();

            for (ScheduleMap scheduleMap : scheduleMaps) {
                SQLControl.ScheduleMaps.insert(scheduleMap, teacher);
                SQLControl.HalfHours.insertFromScheduleMap(scheduleMap);
            }

            response.setStatus(true);
            response.setMessage("Success");
        } catch (SQLException ex) {
            response.setStatus(false);
            response.setMessage("Something went wrong.");
        }

        return response;
    }

    private Response doDeleteTeacher(String id) {
        Response response = new Response();

        try {
            SQLControl.Teachers.delete(id);

            response.setStatus(true);
            response.setMessage("Success");
        } catch (SQLException ex) {
            response.setStatus(false);
            response.setMessage("Something went wrong.");
        }

        return response;
    }

}
