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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.kaw.dev.scheduler.data.ScheduleMap;
import net.kaw.dev.scheduler.data.Teacher;
import net.kaw.dev.scheduler.data.factories.MappableFactory;
import net.kaw.dev.scheduler.exceptions.InvalidDataException;
import net.kaw.dev.scheduler.persistence.sql.SQLControl;
import net.kaw.dev.scheduler.persistence.sql.auth.AuthManager;
import net.kaw.dev.scheduler.persistence.sql.auth.data.AuthInstance;
import net.kaw.dev.scheduler.rest.resources.utils.RequestUtils;
import net.kaw.dev.scheduler.rest.resources.utils.ResponseUtils;
import net.kaw.dev.scheduler.utils.JSONUtils;

@Path("teachers")
public class TeachersResource {

    public TeachersResource() {
    }

    @POST
    @Path(value = "/get")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    public Response getTeacher(final String jsonString) {
        return doGetTeacher(jsonString);
    }

    @POST
    @Path(value = "/post")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response postTeacher(final String jsonString) {
        return doPostTeacher(jsonString);
    }

    @POST
    @Path(value = "/delete")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response deleteTeacher(final String jsonString) {
        return doDeleteTeacher(jsonString);
    }

    private Response doGetTeacher(String jsonString) {
        try {
            Map<String, Object> map = RequestUtils.getMap(jsonString);

            if (map != null && map.containsKey(AuthInstance.AUTH_TOKEN_KEY)) {
                String authToken = (String) map.get(AuthInstance.AUTH_TOKEN_KEY);

                AuthInstance authInstance = SQLControl.AuthInstances.select(authToken);

                if (authInstance != null) {
                    Teacher teacher = authInstance.getTeacher();

                    if (teacher != null) {
                        return ResponseUtils.createResponse(ResponseUtils.OK, JSONUtils.mapToJSON(teacher.toMap()));
                    }
                }
            }

            if (!AuthManager.authenticate(jsonString, 0)) {
                return ResponseUtils.createResponse(ResponseUtils.FORBIDDEN);
            }

            if (map == null) {
                return doGetTeachers();
            }

            if (!map.containsKey(Teacher.ID_KEY)) {
                return doGetTeachers();
            }

            String id = (String) map.get(Teacher.ID_KEY);

            Teacher teacher = SQLControl.Teachers.select(id);

            if (teacher == null) {
                return ResponseUtils.createResponse(ResponseUtils.NOT_FOUND);
            }

            return ResponseUtils.createResponse(ResponseUtils.OK, JSONUtils.mapToJSON(teacher.toMap()));
        } catch (SQLException | InvalidDataException ex) {
            Logger.getLogger(TeachersResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseUtils.createResponse(ResponseUtils.INTERNAL_SERVER_ERROR);
    }

    private Response doGetTeachers() {
        try {
            List<Teacher> teachers = SQLControl.Teachers.select();

            Map<String, Object> teachersMap = new HashMap<>();

            for (Teacher teacher : teachers) {
                teachersMap.put(teacher.getId(), teacher.toMap());
            }

            return ResponseUtils.createResponse(ResponseUtils.OK, JSONUtils.mapToJSON(teachersMap));
        } catch (SQLException | InvalidDataException ex) {
            Logger.getLogger(TeachersResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseUtils.createResponse(ResponseUtils.INTERNAL_SERVER_ERROR);
    }

    private Response doPostTeacher(String jsonString) {
        try {
            if (!AuthManager.authenticate(jsonString, 1)) {
                return ResponseUtils.createResponse(ResponseUtils.FORBIDDEN);
            }

            Teacher teacher = (Teacher) MappableFactory.build(MappableFactory.MappableType.TEACHER, JSONUtils.jsonToMap(jsonString));

            SQLControl.Teachers.insert(teacher);

            List<ScheduleMap> scheduleMaps = teacher.getScheduleMaps();

            for (ScheduleMap scheduleMap : scheduleMaps) {
                SQLControl.ScheduleMaps.insert(scheduleMap, teacher);
                SQLControl.HalfHours.insertFromScheduleMap(scheduleMap);
            }

            return ResponseUtils.createResponse(ResponseUtils.OK);
        } catch (SQLException | InvalidDataException ex) {
            Logger.getLogger(TeachersResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseUtils.createResponse(ResponseUtils.INTERNAL_SERVER_ERROR);
    }

    private Response doDeleteTeacher(String jsonString) {
        try {
            if (!AuthManager.authenticate(jsonString, 1)) {
                return ResponseUtils.createResponse(ResponseUtils.FORBIDDEN);
            }

            Map<String, Object> map = RequestUtils.getMap(jsonString);

            if (map == null) {
                return ResponseUtils.createResponse(ResponseUtils.BAD_REQUEST);
            }

            if (!map.containsKey(Teacher.ID_KEY)) {
                return ResponseUtils.createResponse(ResponseUtils.BAD_REQUEST);
            }

            String id = (String) map.get(Teacher.ID_KEY);

            SQLControl.Teachers.delete(id);

            return ResponseUtils.createResponse(ResponseUtils.OK);
        } catch (SQLException ex) {
            Logger.getLogger(TeachersResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseUtils.createResponse(ResponseUtils.INTERNAL_SERVER_ERROR);
    }

}
