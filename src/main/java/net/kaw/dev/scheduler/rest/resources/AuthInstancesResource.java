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
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.kaw.dev.scheduler.data.Teacher;
import net.kaw.dev.scheduler.data.factories.MappableFactory;
import net.kaw.dev.scheduler.exceptions.InvalidDataException;
import net.kaw.dev.scheduler.persistence.sql.SQLControl;
import net.kaw.dev.scheduler.persistence.sql.auth.AuthManager;
import net.kaw.dev.scheduler.persistence.sql.auth.data.AuthInstance;
import net.kaw.dev.scheduler.rest.resources.utils.RequestUtils;
import net.kaw.dev.scheduler.rest.resources.utils.ResponseUtils;
import net.kaw.dev.scheduler.utils.JSONUtils;

@Path("auth")
public class AuthInstancesResource {

    public AuthInstancesResource() {
    }

    @POST
    @Path(value = "/get")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    public Response getAuthInstance(final String jsonString) {
        return doGetAuthInstance(jsonString);
    }

    @POST
    @Path(value = "/post")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response postAuthInstance(final String jsonString) {
        return doPostAuthInstance(jsonString);
    }

    @POST
    @Path(value = "/delete")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response deleteAuthInstance(final String jsonString) {
        return doDeleteAuthInstance(jsonString);
    }

    @POST
    @Path(value = "/authenticate")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    public Response authenticate(final String jsonString) {
        return doAuthenticate(jsonString);
    }

    private Response doGetAuthInstance(String jsonString) {
        try {
            if (!AuthManager.authenticate(jsonString, 1)) {
                return ResponseUtils.createResponse(ResponseUtils.FORBIDDEN);
            }

            Map<String, Object> map = RequestUtils.getMap(jsonString);

            if (map == null) {
                return ResponseUtils.createResponse(ResponseUtils.BAD_REQUEST);
            }

            if (!map.containsKey(AuthInstance.AUTH_TOKEN_KEY)) {
                return doGetAuthInstances(jsonString);
            }

            String authToken = (String) map.get(AuthInstance.AUTH_TOKEN_KEY);

            AuthInstance authInstance = SQLControl.AuthInstances.select(authToken);

            if (authInstance == null) {
                return ResponseUtils.createResponse(ResponseUtils.NOT_FOUND);
            }

            return ResponseUtils.createResponse(ResponseUtils.OK, JSONUtils.mapToJSON(authInstance.toMap()));
        } catch (SQLException | InvalidDataException ex) {
            Logger.getLogger(AuthInstancesResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseUtils.createResponse(ResponseUtils.INTERNAL_SERVER_ERROR);
    }

    private Response doGetAuthInstances(String jsonString) {
        try {
            Map<String, Object> map = RequestUtils.getMap(jsonString);

            if (map.containsKey(AuthInstance.AUTH_LEVEL_KEY)) {
                return doGetAuthInstancesLevel(jsonString);
            }

            if (!AuthManager.authenticate(jsonString, 1)) {
                return ResponseUtils.createResponse(ResponseUtils.FORBIDDEN);
            }

            List<AuthInstance> authInstances = SQLControl.AuthInstances.select();

            Map<String, Object> authInstancesMap = new HashMap<>();

            for (AuthInstance authInstance : authInstances) {
                authInstancesMap.put(authInstance.getAuthToken(), authInstance.toMap());
            }

            return ResponseUtils.createResponse(ResponseUtils.OK, JSONUtils.mapToJSON(authInstancesMap));
        } catch (SQLException | InvalidDataException ex) {
            Logger.getLogger(AuthInstancesResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseUtils.createResponse(ResponseUtils.INTERNAL_SERVER_ERROR);
    }

    private Response doGetAuthInstancesLevel(String jsonString) {
        try {
            Map<String, Object> map = RequestUtils.getMap(jsonString);

            if (!map.containsKey(AuthInstance.AUTH_LEVEL_KEY)) {
                return ResponseUtils.createResponse(ResponseUtils.BAD_REQUEST);
            }

            Integer authLevel;

            if (map.get(AuthInstance.AUTH_LEVEL_KEY) instanceof Number) {
                Number _authLevel = (Number) map.get(AuthInstance.AUTH_LEVEL_KEY);
                authLevel = _authLevel.intValue();
            } else {
                return ResponseUtils.createResponse(ResponseUtils.BAD_REQUEST);
            }

            if (!AuthManager.authenticate(jsonString, authLevel + 1)) {
                return ResponseUtils.createResponse(ResponseUtils.FORBIDDEN);
            }

            List<AuthInstance> authInstances = SQLControl.AuthInstances.select();

            Map<String, Object> authInstancesMap = new HashMap<>();

            for (AuthInstance authInstance : authInstances) {
                if (Objects.equals(authInstance.getAuthLevel(), authLevel)) {
                    authInstancesMap.put(authInstance.getAuthToken(), authInstance.toMap());
                }
            }

            return ResponseUtils.createResponse(ResponseUtils.OK, JSONUtils.mapToJSON(authInstancesMap));
        } catch (SQLException | InvalidDataException ex) {
            Logger.getLogger(AuthInstancesResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseUtils.createResponse(ResponseUtils.INTERNAL_SERVER_ERROR);
    }

    @SuppressWarnings("unchecked")
    private Response doPostAuthInstance(String jsonString) {
        try {
            if (!AuthManager.authenticate(jsonString, 1)) {
                return ResponseUtils.createResponse(ResponseUtils.FORBIDDEN);
            }

            System.out.println(jsonString);

            AuthInstance authInstance = (AuthInstance) MappableFactory.build(MappableFactory.MappableType.AUTH_INSTANCE, JSONUtils.jsonToMap(jsonString));

            SQLControl.AuthInstances.insert(authInstance);

            return ResponseUtils.createResponse(ResponseUtils.OK);
        } catch (SQLException | InvalidDataException ex) {
            Logger.getLogger(AuthInstancesResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseUtils.createResponse(ResponseUtils.INTERNAL_SERVER_ERROR);
    }

    private Response doDeleteAuthInstance(String jsonString) {
        try {
            if (!AuthManager.authenticate(jsonString, 1)) {
                return ResponseUtils.createResponse(ResponseUtils.FORBIDDEN);
            }

            Map<String, Object> map = RequestUtils.getMap(jsonString);

            if (map == null) {
                return ResponseUtils.createResponse(ResponseUtils.BAD_REQUEST);
            }

            if (!map.containsKey(AuthInstance.AUTH_TOKEN_KEY)) {
                return ResponseUtils.createResponse(ResponseUtils.BAD_REQUEST);
            }

            String authToken = (String) map.get(AuthInstance.AUTH_TOKEN_KEY);

            SQLControl.AuthInstances.delete(authToken);

            return ResponseUtils.createResponse(ResponseUtils.OK);
        } catch (SQLException ex) {
            Logger.getLogger(AuthInstancesResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseUtils.createResponse(ResponseUtils.INTERNAL_SERVER_ERROR);
    }

    private Response doAuthenticate(String jsonString) {
        try {
            Map<String, Object> map = RequestUtils.getMap(jsonString);

            if (map == null) {
                return ResponseUtils.createResponse(ResponseUtils.BAD_REQUEST);
            }

            if (!map.containsKey(AuthInstance.AUTH_USERNAME_KEY) || !map.containsKey(AuthInstance.AUTH_PASSWORD_KEY)) {
                return ResponseUtils.createResponse(ResponseUtils.BAD_REQUEST);
            }

            String username = (String) map.get(AuthInstance.AUTH_USERNAME_KEY);

            String password = (String) map.get(AuthInstance.AUTH_PASSWORD_KEY);

            AuthInstance authInstance = SQLControl.AuthInstances.authenticate(username, password);

            if (authInstance == null) {
                return ResponseUtils.createResponse(ResponseUtils.NOT_FOUND);
            }

            return ResponseUtils.createResponse(ResponseUtils.OK, JSONUtils.mapToJSON(authInstance.toMap()));
        } catch (SQLException | InvalidDataException ex) {
            Logger.getLogger(AuthInstancesResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseUtils.createResponse(ResponseUtils.INTERNAL_SERVER_ERROR);
    }

}
