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
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.kaw.dev.scheduler.persistence.sql.SQLControl;
import net.kaw.dev.scheduler.rest.resources.utils.ResponseUtils;

@Path("careers")
public class CareersResource {

    public CareersResource() {
    }

    @DELETE
    @Path(value = "/delete/{id}")
    public Response deleteCareer(@PathParam(value = "id") final String id) {
        return doDeleteCareer(id);
    }

    private Response doDeleteCareer(String id) {
        try {
            SQLControl.Careers.delete(id);

            return ResponseUtils.createResponse(200, true);
        } catch (SQLException ex) {
            Logger.getLogger(CareersResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ResponseUtils.createResponse(200, false);
    }

}
