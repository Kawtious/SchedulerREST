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
import jakarta.ws.rs.POST;
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
import net.kaw.dev.scheduler.data.Comment;
import net.kaw.dev.scheduler.data.HalfHour;
import net.kaw.dev.scheduler.data.ScheduleMap;
import net.kaw.dev.scheduler.data.Teacher;
import net.kaw.dev.scheduler.rest.Response;
import net.kaw.dev.scheduler.sql.SQLControl;

@Path("teachers")
public class TeachersResource {

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Context
    private UriInfo context;

    public TeachersResource() {
    }

    @POST
    @Path(value = "/post")
    @Produces(MediaType.APPLICATION_JSON)
    public void postDummyCareer(@Suspended final AsyncResponse asyncResponse, final Teacher teacher) {
        executorService.submit(() -> {
            asyncResponse.resume(doPostTeacher(teacher));
        });
    }

    @DELETE
    @Path(value = "/delete/{id}")
    public void deleteTeacher(@Suspended final AsyncResponse asyncResponse, @PathParam(value = "id") final String id) {
        executorService.submit(() -> {
            asyncResponse.resume(doDeleteTeacher(id));
        });
    }

    @GET
    @Path(value = "/postDummy")
    @Produces(MediaType.APPLICATION_JSON)
    public void postDummyCareer(@Suspended final AsyncResponse asyncResponse) {
        executorService.submit(() -> {
            asyncResponse.resume(doPostDummyTeacher());
        });
    }

    @DELETE
    @Path(value = "/deleteDummy")
    public void deleteDummyTeacher(@Suspended final AsyncResponse asyncResponse, @PathParam(value = "id") final String id) {
        executorService.submit(() -> {
            asyncResponse.resume(doDeleteDummyTeacher());
        });
    }

    private Response doPostTeacher(Teacher teacher) {
        Response response = new Response();

        try {
            ScheduleMap scheduleMap = teacher.getScheduleMap();

            SQLControl.Cycles.insert(scheduleMap.getCycle());

            SQLControl.ScheduleMaps.insert(teacher.getScheduleMap(), teacher);

            for (int day = 0; day < ScheduleMap.DAYS; day++) {
                for (int hour = 0; hour < ScheduleMap.HALFHOURS; hour++) {
                    HalfHour halfHour = scheduleMap.getMapValue(day, hour);

                    SQLControl.HalfHours.insert(halfHour, scheduleMap, day, hour);
                }
            }

            if (!scheduleMap.getComments().isEmpty()) {
                for (Comment comment : scheduleMap.getComments()) {
                    for (HalfHour halfHour : comment.getHalfHours()) {
                        SQLControl.Comments.insert(comment, halfHour);
                    }
                }
            }

            SQLControl.Teachers.insert(teacher);

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

    private Response doPostDummyTeacher() {
        Teacher teacher = new Teacher("123456", "P", 123, "AAA", "BBB");

        return doPostTeacher(teacher);
    }

    private Response doDeleteDummyTeacher() {
        return doDeleteTeacher("123456");
    }

}