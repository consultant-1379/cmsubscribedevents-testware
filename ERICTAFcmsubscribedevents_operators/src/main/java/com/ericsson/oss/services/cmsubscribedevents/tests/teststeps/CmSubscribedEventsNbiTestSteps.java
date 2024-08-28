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

package com.ericsson.oss.services.cmsubscribedevents.tests.teststeps;

import static com.ericsson.oss.services.cmsubscribedevents.tests.utils.CmSubscribedEventsNbiTestConstants.DPS_NETWORK_ELEMENT_DATA_SOURCE;
import static com.ericsson.oss.services.cmsubscribedevents.tests.utils.CmSubscribedEventsNbiTestConstants.ID_RETRIEVED_FROM_TEST_STEP;
import static com.ericsson.oss.services.cmsubscribedevents.tests.utils.CmSubscribedEventsNbiTestConstants.IS_NOTIFY_MOI_CHANGES_TEST_CONTEXT;
import static com.ericsson.oss.services.cmsubscribedevents.tests.utils.CmSubscribedEventsNbiTestConstants.SUBSCRIPTION_ID_DATA_SOURCE;
import static com.ericsson.oss.services.cmsubscribedevents.tests.utils.CmSubscribedEventsNbiTestConstants.SUBSCRIPTION_RECIPIENT_HOST;
import static com.ericsson.oss.services.cmsubscribedevents.tests.utils.CmSubscribedEventsNbiTestConstants.SUBSCRIPTION_RECIPIENT_PATH;
import static com.ericsson.oss.services.cmsubscribedevents.tests.utils.CmSubscribedEventsNbiTestConstants.SUBSCRIPTION_RECIPIENT_PORT;
import static com.ericsson.oss.services.cmsubscribedevents.tests.utils.CmSubscribedEventsNbiTestConstants.SUBSCRIPTIONS_DATA_SOURCE;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.NODES_TO_ADD;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.Truth.assertThat;
import static java.text.MessageFormat.format;
import static org.junit.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.DataRecordImpl;
import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.oss.services.cmsubscribedevents.tests.datasource.DpsNetworkElement;
import com.ericsson.oss.services.cmsubscribedevents.tests.datasource.Subscription;
import com.ericsson.oss.services.cmsubscribedevents.tests.datasource.CreatedSubscriptionId;
import com.ericsson.oss.services.cmsubscribedevents.tests.operators.CmSubscribedEventsNbiRestOperator;
import com.ericsson.oss.services.cmsubscribedevents.tests.operators.providers.CliOperatorProvider;
import com.ericsson.oss.testware.enm.cli.EnmCliResponse;
import com.ericsson.oss.testware.enmbase.data.NetworkNode;
import com.ericsson.oss.testware.hostconfigurator.HostConfigurator;
import com.ericsson.oss.testware.hostconfigurator.dit.DitConfigurationBuilder;
import com.ericsson.oss.testware.security.authentication.tool.TafToolProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.configuration.Configuration;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.openqa.selenium.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
/**
 * Provides test steps for testing against CM Subscribed Events NBI
 */
public class CmSubscribedEventsNbiTestSteps {

    private static final Logger logger = LoggerFactory.getLogger(CmSubscribedEventsNbiTestSteps.class);

    public static final String CREATE_SUBSCRIPTION_TEST_STEP = "CreateSubscriptionTestStep";

    public static final String GET_SUBSCRIPTION_TEST_STEP = "GetSubscriptionTestStep";

    public static final String DELETE_SUBSCRIPTION_TEST_STEP = "DeleteSubscriptionTestStep";

    public static final String CLEANUP_SUBSCRIPTION_TEST_STEP = "CleanupSubscriptionsTestStep";

    public static final String TRIGGER_COM_ECIM_EVENT_HANDLING_TEST_STEP = "TriggerComEcimEventHandling";

    public static final String TRIGGER_DPS_EVENT_HANDLING_TEST_STEP = "TriggerDPSEventHandling";

    public static final String VERIFY_HEARTBEAT_SUCCESSFUL_TEST_STEP = "VerifyHeartbeatSuccessful";

    private static final String LOG_START_TEST_STEP = "START TEST STEP: {0}";

    private static final String LOG_END_TEST_STEP = "END TEST STEP: {0} ";

    public static final String HTTP = "http";

    private static final String CONTENT_TYPE = "Content-type";

    private static final String APPLICATION_JSON = "application/json";

    private static final String UTF_8 = "UTF-8";

    private static final String WIREMOCK_COUNT_REQUESTS_API = "/__admin/requests/count";

    private static final String comEcimNodeNotificationAvcBaseCommand = "cmedit set ManagedElement=%s userLabel=%s";

    private static final String comEcimNodeNotificationCreateBaseCommand = "cmedit create ManagedElement=%s,Transport=1,Router=OAM,InterfaceIPv4=1,AddressIPv4=2 addressIPv4Id=2";

    private static final String comEcimNodeNotificationDeleteBaseCommand = "cmedit delete ManagedElement=%s,Transport=1,Router=OAM,InterfaceIPv4=1,AddressIPv4=2";

    private static final String notifyMOIChangesBaseJsonExpected = "{\"method\":\"POST\",\"url\":\"/eventListener/v1/sub1\",\"bodyPatterns\":[{\"matchesJsonPath\":\"$.event.stndDefinedFields.data[?(@.notificationType== 'notifyMOIChanges')]\"},{\"matchesJsonPath\":\"$.event.stndDefinedFields.data[?(@.moiChanges[0].operation == '%s')]\"},{\"matchesJsonPath\" : \"$.event.commonEventHeader[?(@.reportingEntityName == '%s')]\"}]}";

    private static final String notifyMOIEventTypeBaseJsonExpected = "{\"method\":\"POST\",\"url\":\"/eventListener/v1/sub1\",\"bodyPatterns\":[{\"matchesJsonPath\":\"$.event.stndDefinedFields.data[?(@.notificationType=='%s')]\"},{\"matchesJsonPath\" : \"$.event.commonEventHeader[?(@.reportingEntityName == '%s')]\"}]}";

    @Inject
    private Provider<CmSubscribedEventsNbiRestOperator> restOperatorProvider;

    @Inject
    private TafToolProvider tafToolProvider;

    @Inject
    private CliOperatorProvider cliOperatorProvider;

    private CmSubscribedEventsNbiRestOperator restOperator;

    @Inject
    private TestContext context;

    /**
     * This test step builds the NtfSubscriptionControl JSON from the subscription data record and calls the CmSubscribedEventsNbiRestOperator to POST the subscription.
     *
     * @param subscription - The DataRecord representation of the subscriptions.csv
     * @return Subscription ID map as a DataRecord, returns to and collected in the subscriptionsIdDataSource
     *
     */
    @TestStep(id = CREATE_SUBSCRIPTION_TEST_STEP)
    public DataRecord postSubscriptionTestStep(@Input(SUBSCRIPTIONS_DATA_SOURCE) final Subscription subscription) throws IOException, URISyntaxException {
        logger.info(format(LOG_START_TEST_STEP, CREATE_SUBSCRIPTION_TEST_STEP));
        initialiseOperators();
        final String ntfSubscriptionControl = buildNtfSubscriptionControlJson(subscription);
        logger.debug("NtfSubscriptionControl JSON to persist: {}", ntfSubscriptionControl);

        JSONObject jsonObject = new JSONObject(ntfSubscriptionControl);
        final String notificationRecipientAddress = jsonObject.getJSONObject("ntfSubscriptionControl").getString("notificationRecipientAddress");

        final URI recipientUri = new URI(notificationRecipientAddress);
        final String recipientHost = recipientUri.getHost();
        final int recipientPort = recipientUri.getPort();
        final String recipientPath = recipientUri.getPath();

        context.setAttribute(SUBSCRIPTION_RECIPIENT_HOST, recipientHost);
        context.setAttribute(SUBSCRIPTION_RECIPIENT_PORT, recipientPort);
        context.setAttribute(SUBSCRIPTION_RECIPIENT_PATH, recipientPath);
        context.setAttribute(IS_NOTIFY_MOI_CHANGES_TEST_CONTEXT, Arrays.asList(subscription.getNotificationTypes()).contains("notifyMOIChanges"));

        deleteExistingWiremockServerRequests(recipientHost, recipientPort);

        HttpResponse postHttpResponse = null;
        try {
            postHttpResponse = restOperator.postSubscription(ntfSubscriptionControl);
        } catch (IOException e) {
            logger.error("Failed to POST Subscription to NBI, failed to retrieve HTTP entity content ", e.getMessage());
            throw new RuntimeException("Failed to POST Subscription to NBI", e);
        } catch (Exception e) {
            logger.error("Failed to POST Subscription to NBI", e.getMessage());
        }

        final String responseBody = postHttpResponse.getBody();
        logger.info("Response Body: {}", postHttpResponse.getBody());

        JSONObject jsonResponseObject = new JSONObject(responseBody);
        final String subscriptionIdRetrieved = jsonResponseObject.getJSONObject("ntfSubscriptionControl").getString("id");
        logger.debug("Subscription ID returned: " + subscriptionIdRetrieved);

        assertThat(postHttpResponse.getResponseCode().getCode()).isEqualTo(201);
        assertThat(subscriptionIdRetrieved).isNotEqualTo(subscription.getId());

        final Map<String, String> subscriptionIdMap = new HashMap<>();
        subscriptionIdMap.put("subscriptionId", subscriptionIdRetrieved);
        logger.info(format(LOG_END_TEST_STEP, CREATE_SUBSCRIPTION_TEST_STEP));
        return new DataRecordImpl(subscriptionIdMap);
    }

    /**
     * This test step retrieves a subscription from postgres using the ID generated from POST test step
     *
     * @param subscriptionId - DataRecord representation of the Subscription ID from the POSTed subscription
     *
     */
    @TestStep(id = GET_SUBSCRIPTION_TEST_STEP)
    public void getSubscriptionTestStep(@Input(ID_RETRIEVED_FROM_TEST_STEP) final CreatedSubscriptionId subscriptionId) {
        logger.info(format(LOG_START_TEST_STEP, GET_SUBSCRIPTION_TEST_STEP));
        initialiseOperators();
        logger.debug("GET Subscription with ID: {}", subscriptionId.getSubscriptionId());
        checkNotNull(subscriptionId.getSubscriptionId(), "Subscription ID not retrieved from CREATE_SUBSCRIPTION_TEST_STEP.");

        HttpResponse getHttpResponse = null;
        try {
            getHttpResponse = restOperator.getSubscription(subscriptionId.getSubscriptionId());
        } catch (URISyntaxException e) {
            logger.error("Failed to GET Subscription, failed to parse endpoint provided", e);
            throw new RuntimeException("Failed to GET Subscription", e);
        } catch (Exception e) {
            logger.error("Failed to GET Subscription from NBI", e.getMessage());
        }

        logger.info("Response is: {} : {}", getHttpResponse.getResponseCode().getCode(), getHttpResponse.getBody());
        assertThat(getHttpResponse.getResponseCode().getCode()).isEqualTo(200);
        logger.info(format(LOG_END_TEST_STEP, GET_SUBSCRIPTION_TEST_STEP));
    }

    /**
     * This test step initiates a DELETE request using the ID generated from POST test step
     *
     * @param subscriptionId - DataRecord representation of the Subscription ID from the POSTed subscription
     *
     */
    @TestStep(id = DELETE_SUBSCRIPTION_TEST_STEP)
    public void deleteSubscription(@Input(ID_RETRIEVED_FROM_TEST_STEP) final CreatedSubscriptionId subscriptionId) {
        logger.info(format(LOG_START_TEST_STEP, DELETE_SUBSCRIPTION_TEST_STEP));
        initialiseOperators();
        logger.debug("Extracting Subscription ID to execute DELETE request");
        checkNotNull(subscriptionId.getSubscriptionId(), "Subscription ID not retrieved from CREATE_SUBSCRIPTION_TEST_STEP.");
        logger.info("DELETE Subscription with ID: {}", subscriptionId.getSubscriptionId());

        HttpResponse deleteHttpResponse = null;
        try {
            deleteHttpResponse = restOperator.deleteSubscription(subscriptionId.getSubscriptionId());
        } catch (URISyntaxException e) {
            logger.error("Failed to DELETE Subscription, failed to parse endpoint provided", e);
            throw new RuntimeException("Failed to DELETE Subscription", e);
        } catch (Exception e) {
            logger.error("Failed to DELETE Subscription from NBI", e.getMessage());
        }

        logger.info("Response is: {} : {}", deleteHttpResponse.getResponseCode().getCode(), deleteHttpResponse.getBody());
        assertThat(deleteHttpResponse.getResponseCode().getCode()).isEqualTo(204);

        logger.info(format(LOG_END_TEST_STEP, DELETE_SUBSCRIPTION_TEST_STEP));
    }

    /**
     * This test step initiates a cleanup on any Subscriptions leftover after SubscriptionAndEventHandling suite completes, if they didn't complete previously.
     *
     * @param subscriptionId - DataRecord representation of the Subscription ID from the POSTed subscription
     *
     */
    @TestStep(id = CLEANUP_SUBSCRIPTION_TEST_STEP)
    public void cleanupSubscriptions(@Input(SUBSCRIPTION_ID_DATA_SOURCE) final CreatedSubscriptionId subscriptionId) {
        logger.info(format(LOG_START_TEST_STEP, CLEANUP_SUBSCRIPTION_TEST_STEP));
        initialiseOperators();
        logger.info("Extracting Subscription ID to perform DELETE request with");
        logger.debug("Cleaning up Subscription with ID if present: {}", subscriptionId.getSubscriptionId());

        HttpResponse deleteResponse = null;
        try {
            deleteResponse = restOperator.deleteSubscription(subscriptionId.getSubscriptionId());
        } catch (URISyntaxException e) {
            logger.error("Failed to DELETE Subscription, failed to parse endpoint provided", e);
            throw new RuntimeException("Failed to DELETE Subscription", e);
        } catch (Exception e) {
            logger.error("Failed to DELETE Subscription from NBI", e.getMessage());
        }
        logger.info("Response is: {} : {}", deleteResponse.getResponseCode().getCode(), deleteResponse.getBody());
        logger.info(format(LOG_END_TEST_STEP, CLEANUP_SUBSCRIPTION_TEST_STEP));
    }

    /**
     * This test step builds the necessary cmedit commands and calls the CLI Operator to execute these commands
     * to trigger ComEcimNodeNotification  events and verifies the success of pushing the subsequent VES events to the event listener.
     *
     * @param node - DataRecord representation of the Node added to ENM
     */
    @TestStep(id = TRIGGER_COM_ECIM_EVENT_HANDLING_TEST_STEP)
    public void triggerComEcimEventsTestStep(@Input(NODES_TO_ADD) final NetworkNode node) throws URISyntaxException, IOException {
        logger.info(format(LOG_START_TEST_STEP, TRIGGER_COM_ECIM_EVENT_HANDLING_TEST_STEP));

        final String recipientHost = context.getAttribute(SUBSCRIPTION_RECIPIENT_HOST);
        final int recipientPort = context.getAttribute(SUBSCRIPTION_RECIPIENT_PORT);

        deleteExistingWiremockServerRequests(recipientHost, recipientPort);

        StringEntity moiUpdateJsonExpected;
        StringEntity moiCreateJsonExpected;
        StringEntity moiDeleteJsonExpected;

        final String networkElementId = node.getNetworkElementId();

        if(context.getAttribute(IS_NOTIFY_MOI_CHANGES_TEST_CONTEXT)) {
            moiUpdateJsonExpected = new StringEntity(String.format(notifyMOIChangesBaseJsonExpected, "REPLACE", networkElementId));
            moiCreateJsonExpected = new StringEntity(String.format(notifyMOIChangesBaseJsonExpected, "CREATE", networkElementId));
            moiDeleteJsonExpected = new StringEntity(String.format(notifyMOIChangesBaseJsonExpected, "DELETE", networkElementId));
        } else {
            moiUpdateJsonExpected = new StringEntity(String.format(notifyMOIEventTypeBaseJsonExpected, "notifyMOIAttributeValueChanges", networkElementId));
            moiCreateJsonExpected = new StringEntity(String.format(notifyMOIEventTypeBaseJsonExpected, "notifyMOICreation", networkElementId));
            moiDeleteJsonExpected = new StringEntity(String.format(notifyMOIEventTypeBaseJsonExpected, "notifyMOIDeletion", networkElementId));
        }

        final EnmCliResponse createResponse = cliOperatorProvider.executeCliCommand(String.format(comEcimNodeNotificationCreateBaseCommand, networkElementId));
        logger.debug("CREATE ComEcimNodeNotification Response: " + createResponse.toString());
        sleep();
        final String countCreateResponse = executeWiremockCountRequest(moiCreateJsonExpected, recipientHost, recipientPort);
        assertThat(new ObjectMapper().readTree(countCreateResponse).get("count").asInt()).isEqualTo(1);

        final EnmCliResponse deleteResponse = cliOperatorProvider.executeCliCommand(String.format(comEcimNodeNotificationDeleteBaseCommand, networkElementId));
        logger.debug("DELETE ComEcimNodeNotification Response: " + deleteResponse.toString());
        sleep();
        final String countDeleteResponse = executeWiremockCountRequest(moiDeleteJsonExpected, recipientHost, recipientPort);
        assertThat(new ObjectMapper().readTree(countDeleteResponse).get("count").asInt()).isEqualTo(1);

        final EnmCliResponse avcResponse = cliOperatorProvider.executeCliCommand(String.format(comEcimNodeNotificationAvcBaseCommand, networkElementId, new Random().nextInt(999_999)));
        logger.debug("REPLACE ComEcimNodeNotification Response: " + avcResponse.toString());
        sleep();
        final String countAvcResponse = executeWiremockCountRequest(moiUpdateJsonExpected, recipientHost, recipientPort);
        assertThat(new ObjectMapper().readTree(countAvcResponse).get("count").asInt()).isEqualTo(1);

        logger.info(format(LOG_END_TEST_STEP, TRIGGER_COM_ECIM_EVENT_HANDLING_TEST_STEP));
    }

    /**
     * This test step builds the necessary cmedit commands and calls the CLI Operator to execute these commands
     * to trigger DPS events and verifies the success of pushing the subsequent VES events to the event listener.
     *
     * @param dpsNetworkElement - DataRecord representation of the NetworkElement to be created in ENM
     */
    @TestStep(id = TRIGGER_DPS_EVENT_HANDLING_TEST_STEP)
    public void triggerDpsEventsTestStep(@Input(DPS_NETWORK_ELEMENT_DATA_SOURCE) final DpsNetworkElement dpsNetworkElement) throws IOException, URISyntaxException {
        logger.info(format(LOG_START_TEST_STEP, TRIGGER_DPS_EVENT_HANDLING_TEST_STEP));
        logger.info(format("DPS Network Element: " + dpsNetworkElement.getNetworkElementId()));

        final String recipientHost = context.getAttribute(SUBSCRIPTION_RECIPIENT_HOST);
        final int recipientPort = context.getAttribute(SUBSCRIPTION_RECIPIENT_PORT);

        deleteExistingWiremockServerRequests(recipientHost, recipientPort);

        StringEntity moiUpdateJsonExpected;
        StringEntity moiCreateJsonExpected;
        StringEntity moiDeleteJsonExpected;

        final String networkElement = dpsNetworkElement.getNetworkElementId();

        if(context.getAttribute(IS_NOTIFY_MOI_CHANGES_TEST_CONTEXT)) {
            moiUpdateJsonExpected = new StringEntity(String.format(notifyMOIChangesBaseJsonExpected, "REPLACE", networkElement));
            moiCreateJsonExpected = new StringEntity(String.format(notifyMOIChangesBaseJsonExpected, "CREATE", networkElement));
            moiDeleteJsonExpected = new StringEntity(String.format(notifyMOIChangesBaseJsonExpected, "DELETE", networkElement));
        } else {
            moiUpdateJsonExpected = new StringEntity(String.format(notifyMOIEventTypeBaseJsonExpected, "notifyMOIAttributeValueChanges", networkElement));
            moiCreateJsonExpected = new StringEntity(String.format(notifyMOIEventTypeBaseJsonExpected, "notifyMOICreation", networkElement));
            moiDeleteJsonExpected = new StringEntity(String.format(notifyMOIEventTypeBaseJsonExpected, "notifyMOIDeletion", networkElement));
        }

        final String dpsCreateCommand = String.format("cmedit create NetworkElement=%s networkElementId=%<s,neType=RadioNode",networkElement);
        final String dpsAvcCommand = String.format("cmedit set NetworkElement=%s,ComConnectivityInformation=1 ipAddress=\"99.99.99.99\"",networkElement);
        final String dpsDeleteCommand = String.format("cmedit delete NetworkElement=%s --ALL",networkElement);

        final EnmCliResponse createResponse = cliOperatorProvider.executeCliCommand(dpsCreateCommand);
        logger.debug("CREATE DPS Response: " + createResponse.toString());
        sleep();
        final String countCreateResponse = executeWiremockCountRequest(moiCreateJsonExpected, recipientHost, recipientPort);
        assertThat(new ObjectMapper().readTree(countCreateResponse).get("count").asInt()).isEqualTo(1);

        createComConnectivityMoForDpsAVC(networkElement);
        sleep();
        final EnmCliResponse avcResponse = cliOperatorProvider.executeCliCommand(dpsAvcCommand);
        logger.debug("REPLACE DPS Response: " + avcResponse.toString());
        sleep();
        final String countAvcResponse = executeWiremockCountRequest(moiUpdateJsonExpected, recipientHost, recipientPort);
        assertThat(new ObjectMapper().readTree(countAvcResponse).get("count").asInt()).isEqualTo(1);

        final EnmCliResponse deleteResponse = cliOperatorProvider.executeCliCommand(dpsDeleteCommand);
        logger.debug("DELETE DPS Response: " + deleteResponse.toString());
        sleep();
        String countDeleteResponse = executeWiremockCountRequest(moiDeleteJsonExpected, recipientHost, recipientPort);
        assertThat(new ObjectMapper().readTree(countDeleteResponse).get("count").asInt()).isEqualTo(4); // For deletion of NetworkElement, ComConnectivityInformation, CmNodeHeartbeatSupervision and CmFunction

        logger.info(format(LOG_END_TEST_STEP, TRIGGER_DPS_EVENT_HANDLING_TEST_STEP));
    }

    @TestStep(id = VERIFY_HEARTBEAT_SUCCESSFUL_TEST_STEP)
    public void verifyHeartbeatSuccessfulTestStep() throws IOException, URISyntaxException {
        logger.info(format(LOG_START_TEST_STEP,VERIFY_HEARTBEAT_SUCCESSFUL_TEST_STEP ));
        final String recipientHost = context.getAttribute(SUBSCRIPTION_RECIPIENT_HOST);
        final int recipientPort = context.getAttribute(SUBSCRIPTION_RECIPIENT_PORT);
        final String recipientPath = context.getAttribute(SUBSCRIPTION_RECIPIENT_PATH);

        logger.debug("Verifying Heartbeat request to eventListener: http://{}:{}{}",recipientHost ,recipientPort, recipientPath);

        String responseMessage;
        final StringEntity heartbeatJsonExpected = new StringEntity("{\"method\":\"POST\",\"url\":\"" + recipientPath + "\",\"bodyPatterns\":[{\"matchesJsonPath\":\"$.event.commonEventHeader[?(@.domain == 'heartbeat')]\"}]}");
        responseMessage = executeWiremockCountRequest(heartbeatJsonExpected, recipientHost, recipientPort);

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode wiremockCountNode = mapper.readTree(responseMessage);
        int numberOfHeartbeatVesReceived = wiremockCountNode.get("count").asInt();
        assertThat(numberOfHeartbeatVesReceived).isEqualTo(1);
        logger.info(format(LOG_END_TEST_STEP, VERIFY_HEARTBEAT_SUCCESSFUL_TEST_STEP));
    }

    private void createComConnectivityMoForDpsAVC(final String dpsNetworkElement){
        final String createComConnectivity = String.format("cmedit create NetworkElement=%s,ComConnectivityInformation=1 ComConnectivityInformationId=1, ipAddress=\"1.2.3.4\", transportProtocol=\"TLS\", port=6513 -ns=COM_MED -version=1.1.0", dpsNetworkElement);
        final EnmCliResponse resetResponse = cliOperatorProvider.executeCliCommand(createComConnectivity);
        logger.debug("ComConnectivity create response: " + resetResponse.toString());sleep();
    }

    private String buildNtfSubscriptionControlJson(final Subscription subscriptionDataRecord) throws NotFoundException{
        logger.debug("Building Subscription JSON from data record fields");
        String notificationRecipientAddress = getNotificationRecipientAddress();

        String notificationTypesString = String.join(",", Arrays.toString(subscriptionDataRecord.getNotificationTypes())
                .replaceAll("\\[", "")
                .replaceAll("]","")
                .replaceAll(" ","")
                .replaceAll(",","\",\""));

        String scope = String.format("\"scope\":{\"scopeType\":\"%s\",\"scopeLevel\":%d}", subscriptionDataRecord.getScopeType(), subscriptionDataRecord.getScopeLevel());
        String subscription = String.format("\"notificationRecipientAddress\":\"%s\",\"id\":\"%s\",\"notificationTypes\":[\"%s\"],\"objectClass\":\"%s\",\"objectInstance\":\"%s\",%s}",
                notificationRecipientAddress, subscriptionDataRecord.getId(), notificationTypesString, subscriptionDataRecord.getObjectClass(), subscriptionDataRecord.getObjectInstance(), scope);

        return String.format("{\"ntfSubscriptionControl\":{%s}", subscription);
    }


    private String executeWiremockCountRequest(final StringEntity responseCriteriaExpected, final String recipientHost, final int recipientPort) throws IOException, URISyntaxException {
        URI wiremockUri = new URIBuilder().setScheme(HTTP).setHost(recipientHost).setPort(recipientPort).setPath(WIREMOCK_COUNT_REQUESTS_API).build();

        final HttpPost wireMockVerificationString = new HttpPost(wiremockUri);
        wireMockVerificationString.setHeader(CONTENT_TYPE, APPLICATION_JSON);
        wireMockVerificationString.setEntity(responseCriteriaExpected);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        try (CloseableHttpResponse response = httpclient.execute(wireMockVerificationString)) {
            int actualStatusCode = response.getStatusLine().getStatusCode();
            logger.info("Wiremock ResponseMessage Status Code: {}", actualStatusCode);
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, UTF_8);
        }
    }

    private void deleteExistingWiremockServerRequests(final String recipientHost, final int recipientPort) throws IOException, URISyntaxException {
        // Deletion of requests sent to wiremock to simplify verification for later tests
        final URI deleteWiremockReqUri = new URIBuilder().setScheme(HTTP).setHost(recipientHost).setPort(recipientPort).setPath("/__admin/requests").build();
        final HttpDelete deleteRequests = new HttpDelete(deleteWiremockReqUri);
        deleteRequests.setHeader(CONTENT_TYPE, APPLICATION_JSON);

        try (final CloseableHttpClient httpclient = HttpClients.createDefault()){
            try (CloseableHttpResponse response = httpclient.execute(deleteRequests)) {
                final int actualStatusCode = response.getStatusLine().getStatusCode();
                assertEquals(actualStatusCode, 200);
            }
        }
    }

    @SuppressFBWarnings("THREAD_SLEEP")
    private static void sleep() {
        try {
            Thread.sleep(8000);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void initialiseOperators() {
        restOperator = restOperatorProvider.get();
        restOperator.setHttpTool(getHttpTool());
    }

    private HttpTool getHttpTool() {
        return tafToolProvider.getHttpTool();
    }

    private String getNotificationRecipientAddress() {
        String notificationRecipientAddress = "";

        if (HostConfigurator.isPhysicalEnvironment()){
            Host host = HostConfigurator.getEventListener();
            assertNotNull(host.getIp());
            notificationRecipientAddress = String.format("http://%s" + ":9100/eventListener/v1/sub1", host.getIp());
        }
        else if (HostConfigurator.isVirtualEnvironment()){

            DitConfigurationBuilder builder = new DitConfigurationBuilder();
            final TafConfiguration configuration = TafConfigurationProvider.provide();
            builder.setup(configuration);
            Configuration config = builder.build();
            assertNotNull(config.getProperty("host.emp.node.EVENTLISTENER_0.ip"));
            notificationRecipientAddress = String.format("http://%s" + ":9100/eventListener/v1/sub1", config.getProperty("host.emp.node.EVENTLISTENER_0.ip"));
        }

        return notificationRecipientAddress;
    }
}