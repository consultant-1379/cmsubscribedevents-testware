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

package com.ericsson.oss.services.cmsubscribedevents.tests.flows;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.fromTestStepResult;
import static com.ericsson.oss.services.cmsubscribedevents.tests.teststeps.CmSubscribedEventsNbiTestSteps.CLEANUP_SUBSCRIPTION_TEST_STEP;
import static com.ericsson.oss.services.cmsubscribedevents.tests.teststeps.CmSubscribedEventsNbiTestSteps.CREATE_SUBSCRIPTION_TEST_STEP;
import static com.ericsson.oss.services.cmsubscribedevents.tests.teststeps.CmSubscribedEventsNbiTestSteps.DELETE_SUBSCRIPTION_TEST_STEP;
import static com.ericsson.oss.services.cmsubscribedevents.tests.teststeps.CmSubscribedEventsNbiTestSteps.GET_SUBSCRIPTION_TEST_STEP;
import static com.ericsson.oss.services.cmsubscribedevents.tests.teststeps.CmSubscribedEventsNbiTestSteps.TRIGGER_COM_ECIM_EVENT_HANDLING_TEST_STEP;
import static com.ericsson.oss.services.cmsubscribedevents.tests.teststeps.CmSubscribedEventsNbiTestSteps.TRIGGER_DPS_EVENT_HANDLING_TEST_STEP;
import static com.ericsson.oss.services.cmsubscribedevents.tests.teststeps.CmSubscribedEventsNbiTestSteps.VERIFY_HEARTBEAT_SUCCESSFUL_TEST_STEP;
import static com.ericsson.oss.services.cmsubscribedevents.tests.utils.CmSubscribedEventsNbiTestConstants.DPS_NETWORK_ELEMENT_DATA_SOURCE;
import static com.ericsson.oss.services.cmsubscribedevents.tests.utils.CmSubscribedEventsNbiTestConstants.ID_RETRIEVED_FROM_TEST_STEP;
import static com.ericsson.oss.services.cmsubscribedevents.tests.utils.CmSubscribedEventsNbiTestConstants.SUBSCRIPTION_ID_DATA_SOURCE;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.AVAILABLE_USERS;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.NODES_TO_ADD;
import static com.ericsson.oss.testware.security.authentication.steps.LoginLogoutRestTestSteps.TEST_STEP_LOGIN;
import static com.ericsson.oss.testware.security.authentication.steps.LoginLogoutRestTestSteps.TEST_STEP_LOGOUT;
import static com.ericsson.oss.testware.security.authentication.steps.LoginLogoutRestTestSteps.TEST_STEP_IS_USER_LOGGED_IN;
import static java.text.MessageFormat.format;

import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.oss.services.cmsubscribedevents.tests.teststeps.CmSubscribedEventsNbiTestSteps;
import com.ericsson.oss.testware.security.authentication.steps.LoginLogoutRestTestSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Provides Test flows for CM Subscribed Events NBI
 */
public class CmSubscribedEventsNbiTestFlows {

    private static final Logger logger = LoggerFactory.getLogger(CmSubscribedEventsNbiTestFlows.class);
    private static final String LOG_START_FLOW = "TEST FLOW STARTED: {0}";

    @Inject
    private LoginLogoutRestTestSteps loginLogoutRestTestSteps;

    @Inject
    private CmSubscribedEventsNbiTestSteps cmSubscribedEventsNbiTestSteps;

    /**
     * Test flow to create, read back and delete a Subscription that gets persisted to the cm-subscribed-events-nbi.
     *
     * @return TestStepFlow for logging in, calling subscription handling and logging out.
     */
    public TestStepFlow subscriptionAndEventHandlingFlow() {
        logger.info(format(LOG_START_FLOW, "Subscription Handling Flow"));
        return flow("Login and initiate Subscription Handling - FLOW")
                .addTestStep(annotatedMethod(loginLogoutRestTestSteps, TEST_STEP_LOGIN))
                .addTestStep(annotatedMethod(loginLogoutRestTestSteps, TEST_STEP_IS_USER_LOGGED_IN))
                .addSubFlow(subscriptionHandlingSubFlow())
                .addTestStep(annotatedMethod(loginLogoutRestTestSteps, TEST_STEP_LOGOUT))
                .withDataSources(dataSource(AVAILABLE_USERS)).build();
    }

    /**
     * Test Flow to clean up any remaining subscriptions that may have been leftover from the Subscription Handling suite
     *
     * @return TestStepFlow to clean up subscriptions.
     */
    public TestStepFlow subscriptionCleanupFlow() {
        logger.info(format(LOG_START_FLOW, "Subscription Cleanup Flow"));
        return flow("Deletes any leftover Subscriptions - FLOW")
                .addTestStep(annotatedMethod(loginLogoutRestTestSteps, TEST_STEP_LOGIN))
                .addTestStep(annotatedMethod(loginLogoutRestTestSteps, TEST_STEP_IS_USER_LOGGED_IN))
                .addSubFlow(deleteAllSubscriptionsSubFlow())
                .addTestStep(annotatedMethod(loginLogoutRestTestSteps, TEST_STEP_LOGOUT))
                .withDataSources(dataSource(AVAILABLE_USERS)).build();
    }

    private TestStepFlow subscriptionHandlingSubFlow() {
        logger.info(format(LOG_START_FLOW, "Subscription Handling Subflow"));
        return flow("Subscriptions and Events - SUBFLOW")
                .addTestStep(annotatedMethod(cmSubscribedEventsNbiTestSteps, CREATE_SUBSCRIPTION_TEST_STEP)
                        .collectResultToDatasource(SUBSCRIPTION_ID_DATA_SOURCE))
                .addTestStep(annotatedMethod(cmSubscribedEventsNbiTestSteps, VERIFY_HEARTBEAT_SUCCESSFUL_TEST_STEP))
                .addTestStep(annotatedMethod(cmSubscribedEventsNbiTestSteps, GET_SUBSCRIPTION_TEST_STEP)
                        .withParameter(ID_RETRIEVED_FROM_TEST_STEP, fromTestStepResult(CREATE_SUBSCRIPTION_TEST_STEP)))
                .addSubFlow(eventHandlingSubFlow())
                        .withDataSources(dataSource(DPS_NETWORK_ELEMENT_DATA_SOURCE))
                .addTestStep(annotatedMethod(cmSubscribedEventsNbiTestSteps, DELETE_SUBSCRIPTION_TEST_STEP)
                        .withParameter(ID_RETRIEVED_FROM_TEST_STEP, fromTestStepResult(CREATE_SUBSCRIPTION_TEST_STEP)))
            .build();
    }

    private TestStepFlow deleteAllSubscriptionsSubFlow() {
        logger.info(format(LOG_START_FLOW, "Delete All Subscriptions Subflow"));
        return flow("Cleanup any remaining Subscriptions - SUBFLOW")
                .addTestStep(annotatedMethod(cmSubscribedEventsNbiTestSteps, CLEANUP_SUBSCRIPTION_TEST_STEP))
                .withDataSources(dataSource(SUBSCRIPTION_ID_DATA_SOURCE).allowEmpty())
                .build();
    }

    private TestStepFlow eventHandlingSubFlow() {
        return flow("Event Handling - SUBFLOW")
            .addTestStep(annotatedMethod(cmSubscribedEventsNbiTestSteps, TRIGGER_COM_ECIM_EVENT_HANDLING_TEST_STEP))
            .addTestStep(annotatedMethod(cmSubscribedEventsNbiTestSteps, TRIGGER_DPS_EVENT_HANDLING_TEST_STEP))
            .withDataSources(dataSource(NODES_TO_ADD))
            .build();
    }
}