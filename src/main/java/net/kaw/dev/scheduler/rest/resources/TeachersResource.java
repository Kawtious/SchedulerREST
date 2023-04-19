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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.kaw.dev.scheduler.data.ScheduleMap;
import net.kaw.dev.scheduler.data.Teacher;
import net.kaw.dev.scheduler.data.factories.MappableFactory;
import net.kaw.dev.scheduler.exceptions.InvalidDataException;
import net.kaw.dev.scheduler.persistence.sql.SQLControl;
import net.kaw.dev.scheduler.utils.JSONUtils;

@Path("teachers")
public class TeachersResource {

    public TeachersResource() {
    }

    @GET
    @Path(value = "/get/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getTeacher(@PathParam(value = "id") final String id) {
        return doGetTeacher(id);
    }

    @GET
    @Path(value = "/dummy")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getDummyTeacher() {
        return doGetDummyTeacher();
    }

    @POST
    @Path(value = "/post")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response postTeacher(final String jsonString) {
        return doPostTeacher(jsonString);
    }

    @DELETE
    @Path(value = "/delete/{id}")
    public Response deleteTeacher(@PathParam(value = "id") final String id) {
        return doDeleteTeacher(id);
    }

    private Response doGetTeacher(String id) {
        try {
            Teacher teacher = SQLControl.Teachers.select(id);

            if (teacher == null) {
                return ResponseManager.createResponse(200, "");
            }

            return ResponseManager.createResponse(200, JSONUtils.mapToJSON(teacher.toMap()));
        } catch (SQLException | InvalidDataException ex) {
            Logger.getLogger(TeachersResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseManager.createResponse(200, "");
    }

    private Response doGetDummyTeacher() {
        String dummyId = "dummy_teacher_id";
        return doGetTeacher(dummyId);
    }

    private Response doPostTeacher(String jsonString) {
        try {
            Teacher teacher = (Teacher) MappableFactory.build(MappableFactory.MappableType.TEACHER, JSONUtils.jsonToMap(jsonString));

            SQLControl.Teachers.insert(teacher);

            List<ScheduleMap> scheduleMaps = teacher.getScheduleMaps();

            for (ScheduleMap scheduleMap : scheduleMaps) {
                SQLControl.ScheduleMaps.insert(scheduleMap, teacher);
                SQLControl.HalfHours.insertFromScheduleMap(scheduleMap);
            }

            return ResponseManager.createResponse(200, true);
        } catch (SQLException | InvalidDataException ex) {
            Logger.getLogger(TeachersResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseManager.createResponse(200, false);
    }

    private Response doDeleteTeacher(String id) {
        try {
            SQLControl.Teachers.delete(id);

            return ResponseManager.createResponse(200, true);
        } catch (SQLException ex) {
            Logger.getLogger(TeachersResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseManager.createResponse(200, false);
    }

}
