
package org.fcrepo.dissemination;

import java.net.URI;

import org.apache.http.client.methods.HttpRequestBase;

public class DisseminationRequest<Method extends HttpRequestBase> {

    HttpRequestBase request;

    public DisseminationRequest(HttpRequestMethod method, URI uri) {
        this.request = method.build();
        request.setURI(uri);
    }
}
