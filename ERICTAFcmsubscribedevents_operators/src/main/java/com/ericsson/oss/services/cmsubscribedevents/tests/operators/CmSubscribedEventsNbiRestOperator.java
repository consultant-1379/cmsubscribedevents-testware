/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------
 */

package com.ericsson.oss.services.cmsubscribedevents.tests.operators;

import static com.ericsson.cifwk.taf.tools.http.constants.ContentType.APPLICATION_JSON;
import static com.ericsson.oss.services.cmsubscribedevents.tests.utils.CmSubscribedEventsNbiTestConstants.ACCEPT;
import static com.ericsson.oss.services.cmsubscribedevents.tests.utils.CmSubscribedEventsNbiTestConstants.APPLICATION_HAL_JSON;
import static com.ericsson.oss.services.cmsubscribedevents.tests.utils.CmSubscribedEventsNbiTestConstants.SUBSCRIPTIONS_URI;
import static com.ericsson.oss.services.cmsubscribedevents.tests.utils.CmSubscribedEventsNbiTestConstants.UTF_8;

import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * REST Operator class for building and performing HTTP request to CM Subscribed Events NBI endpoint
 */
public class CmSubscribedEventsNbiRestOperator {

    private static final Logger logger = LoggerFactory.getLogger(CmSubscribedEventsNbiRestOperator.class);
    private HttpTool httpTool;  //Required to establish HTTP session

    /**
     * Sets the HttpTool to create user session
     *
     * @param httpTool
     *     - HTTP session
     */
    public void setHttpTool(final HttpTool httpTool) {
        this.httpTool = httpTool;
    }

    /**
     * Sends a POST request to the subscription endpoint of the CM Subscribed Events NBI and returns response
     *
     * @param ntfSubscriptionControl
     *     - JSON of Subscription to be persisted.
     * @return HttpResponse - response to be returned
     */
    public HttpResponse postSubscription(final String ntfSubscriptionControl) throws IOException {
        final HttpPost httpPost = new HttpPost(SUBSCRIPTIONS_URI);
        logger.debug("Sending RESTful POST to CM Subscribed Events NBI: {}", httpPost.getRequestLine().getUri());

        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(ntfSubscriptionControl, UTF_8));

        return httpTool.request().header(ACCEPT, APPLICATION_HAL_JSON).contentType(APPLICATION_JSON).body(httpPost.getEntity().getContent())
            .post(httpPost.getURI().toString());
    }

    /**
     * Sends a GET request for a Subscription and is returned as part of the response.
     *
     * @param subscriptionId
     *     - ID of Subscription to be retrieved.
     * @return HttpResponse - response with Subscription contained in body
     * @throws URISyntaxException
     *     - exception thrown where URI reference can't be parsed
     */
    public HttpResponse getSubscription(final String subscriptionId) throws URISyntaxException {
        final HttpGet httpGet = new HttpGet(SUBSCRIPTIONS_URI + "/" + subscriptionId);

        final URI uri = (new URIBuilder(httpGet.getURI())).build();
        httpGet.setURI(uri);

        return httpTool.request().header(ACCEPT, APPLICATION_HAL_JSON).contentType(APPLICATION_JSON).get(httpGet.getURI().toString());
    }

    /**
     * Sends a DELETE request to delete a Subscription and return the response
     *
     * @param subscriptionId
     *     - ID of Subscription to be deleted.
     * @return HttpResponse - response returned.
     * @throws URISyntaxException
     *     - exception thrown where URI reference can't be parsed
     */
    public HttpResponse deleteSubscription(final String subscriptionId) throws URISyntaxException {
        final HttpDelete httpDelete = new HttpDelete(SUBSCRIPTIONS_URI + "/" + subscriptionId);

        final URI uri = (new URIBuilder(httpDelete.getURI())).build();
        httpDelete.setURI(uri);

        return httpTool.request().header(ACCEPT, APPLICATION_HAL_JSON).contentType(APPLICATION_JSON).delete(httpDelete.getURI().toString());
    }
}
