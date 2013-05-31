
package org.fcrepo.dissemination;

import static com.google.common.base.Throwables.propagate;
import static org.slf4j.LoggerFactory.getLogger;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;

public enum HttpRequestMethod {

    GET(HttpGet.class), POST(HttpPost.class);

    private Class<? extends HttpRequestBase> implClass;

    private static final Logger LOGGER = getLogger(HttpRequestMethod.class);

    private HttpRequestMethod(Class<? extends HttpRequestBase> implClass) {
        this.implClass = implClass;
    }

    public Class<? extends HttpRequestBase> getRequestClass() {
        return implClass;
    }

    public HttpRequestBase build() {
        try {
            return implClass.newInstance();
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Couldn't build request object for dissemination!", e);
            propagate(e);
        }
        // should never be reached
        return null;
    }
}
