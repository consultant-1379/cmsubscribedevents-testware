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

package com.ericsson.oss.services.cmsubscribedevents.tests.subscriptionhandling;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataDrivenScenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.oss.services.cmsubscribedevents.tests.utils.CmSubscribedEventsNbiTestConstants.SUBSCRIPTIONS_DATA_SOURCE;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.annotations.TestSuite;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.scenario.impl.LoggingScenarioListener;
import com.ericsson.oss.services.cmsubscribedevents.tests.flows.CmSubscribedEventsNbiTestFlows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.inject.Inject;

/**
 * Sets up scenario for full end to end Subscription Handling and Event Handling
 */
public class CmSubscribedEventsSubscriptionAndEventHandlingScenario extends TafTestBase {
    private static final Logger logger = LoggerFactory.getLogger(CmSubscribedEventsSubscriptionAndEventHandlingScenario.class);

    @Inject
    private CmSubscribedEventsNbiTestFlows cmSubscribedEventsNbiTestFlows;

    /**
     *  Test Scenario to verify Subscription and Event Handling end to end.
     */
    @Test
    @TestSuite
    public void subscriptionAndEventHandlingScenario() {
        logger.info("TEST SCENARIO STARTED: {}", "Subscription and Event Handling");
        final TestScenario scenario = dataDrivenScenario("Subscription and Event Handling")
                .addFlow(cmSubscribedEventsNbiTestFlows.subscriptionAndEventHandlingFlow())
                .withScenarioDataSources(dataSource(SUBSCRIPTIONS_DATA_SOURCE))
                .build();
        getTestScenarioRunner().start(scenario);
        logger.info("TEST SCENARIO COMPLETED: {}", "Subscription and Event Handling");
    }

    private TestScenarioRunner getTestScenarioRunner() {
        return runner()
                .withListener(new LoggingScenarioListener())
                .withDefaultExceptionHandler(ScenarioExceptionHandler.PROPAGATE)
                .build();
    }
}