
package org.fcrepo.dissemination.api;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.fcrepo.AbstractResource;
import org.fcrepo.Datastream;
import org.fcrepo.FedoraObject;
import org.fcrepo.dissemination.HttpRequestMethod;
import org.fcrepo.services.DatastreamService;
import org.fcrepo.services.ObjectService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/rest/{path: .*}/fcr:dissemination/{dissPath: .*}")
public class FedoraDisseminations extends AbstractResource {

    private final Logger logger = getLogger(FedoraDisseminations.class);

    @Autowired
    private DatastreamService datastreamService;

    @Autowired
    private ObjectService objectService;

    DefaultHttpClient httpclient = new DefaultHttpClient();

    @GET
    @POST
    public Response performDissemination(@PathParam("path")
    List<PathSegment> pathList, @PathParam("dissPath")
    List<PathSegment> dissPathList, final InputStream requestBody, @Context
    final Request request) throws RepositoryException, IOException {

        final Session session = getAuthenticatedSession();
        try {
            final FedoraObject object =
                    objectService.getObject(session, toPath(pathList));
            final FedoraObject dissObject =
                    objectService.getObject(session, toPath(dissPathList));
            final String requestMethod = request.getMethod();

            final URI dissUri =
                    URI.create(IOUtils.toString(new Datastream(
                            datastreamService.getNode(dissObject.getNode(),
                                    requestMethod, true)).getContent()));
            final HttpRequestMethod dissRequestMethod =
                    HttpRequestMethod.valueOf(requestMethod);
            final HttpRequestBase dissRequest = dissRequestMethod.build();
            dissRequest.setURI(dissUri);
            // if this is a request with a body of content, add it to the dissemintation request
            if (HttpEntityEnclosingRequestBase.class
                    .isAssignableFrom(dissRequestMethod.getRequestClass())) {
                ((HttpEntityEnclosingRequestBase) dissRequest)
                        .setEntity(new InputStreamEntity(requestBody, -1));
            }
            HttpResponse dissResponse = httpclient.execute(dissRequest);
            return Response
                    .status(dissResponse.getStatusLine().getStatusCode())
                    .entity(dissResponse.getEntity().getContent()).build();

        } finally {
            session.logout();
        }
    }

}
