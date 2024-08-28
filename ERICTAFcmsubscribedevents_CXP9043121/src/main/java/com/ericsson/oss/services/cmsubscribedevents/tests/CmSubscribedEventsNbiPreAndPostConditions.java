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
package com.ericsson.oss.services.cmsubscribedevents.tests;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.oss.services.cmsubscribedevents.tests.utils.CmSubscribedEventsNbiTestConstants.DPS_NETWORK_ELEMENT_DATA_SOURCE;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.ADDED_NODES;
import static com.ericsson.oss.testware.enmbase.data.CommonDataSources.NODES_TO_ADD;
import static com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows.EnmObjectType.USER;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandHandler;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.scenario.impl.LoggingScenarioListener;
import com.ericsson.oss.services.cmsubscribedevents.tests.datasource.DpsNetworkElement;
import com.ericsson.oss.services.cmsubscribedevents.tests.flows.CmSubscribedEventsNbiTestFlows;
import com.ericsson.oss.services.cmsubscribedevents.tests.operators.providers.CliOperatorProvider;
import com.ericsson.oss.testware.enm.cli.EnmCliResponse;
import com.ericsson.oss.testware.enm.cli.matchers.EnmCliResponseMatcher;
import com.ericsson.oss.testware.enmbase.data.CommonDataSources;
import com.ericsson.oss.testware.enmbase.data.ENMUser;
import com.ericsson.oss.testware.enmbase.data.NetworkNode;
import com.ericsson.oss.testware.hostconfigurator.HostConfigurator;
import com.ericsson.oss.testware.network.operators.netsim.NetsimOperator;
import com.ericsson.oss.testware.security.authentication.flows.LoginLogoutRestFlows;
import com.ericsson.oss.testware.security.gim.flows.GimCleanupFlows;
import com.ericsson.oss.testware.security.gim.flows.UserManagementTestFlows;
import com.ericsson.oss.testware.nodeintegration.flows.NodeIntegrationFlows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Sets up User data sources and creates users in ENM. Users created are cleaned down as are any Subscriptions remaining.
 */
public class CmSubscribedEventsNbiPreAndPostConditions extends TafTestBase {

    private static final Logger logger = LoggerFactory.getLogger(CmSubscribedEventsNbiPreAndPostConditions.class);

    public static final String START_NETSIM_NETWORK_ELEMENT = "Start Netsim Network Element";
    public static final String CLEAN_UP_DPS_MANAGED_OBJECTS = "Clean Up DPS MOs";
    public static final String RESTORE_NETSIM_NE_STATE = "Restore Netsim NE State";

    @Inject
    private LoginLogoutRestFlows loginLogoutFlows;
    @Inject
    private NodeIntegrationFlows nodeIntegrationFlows;
    @Inject
    private UserManagementTestFlows userManagementTestFlows;
    @Inject
    private GimCleanupFlows gimCleanupFlows;
    @Inject
    private TestContext context;
    @Inject
    private CmSubscribedEventsNbiTestFlows cmSubscribedEventsNbiTestFlows;
    @Inject
    private CliOperatorProvider cliOperatorProvider;

    @Inject
    private Provider<NetsimOperator> netsimOperatorProvider;

    private static void initDataSourceFromCsv(final String datasourceName) {
        final TestContext testContext = TafTestContext.getContext();
        testContext.dataSource(datasourceName, (Class) ENMUser.class);
        final TestDataSource<DataRecord> datasource = TafDataSources.shared(TafDataSources.fromCsv("usersToCreate.csv"));
        testContext.addDataSource(datasourceName, datasource);
        logger.info("Datasource '{}' with dataType '{}.class' has been populated using the csvFile '{}' .",
            datasourceName, ENMUser.class.getSimpleName(), "usersToCreate.csv");
    }

    @BeforeSuite(alwaysRun = true)
    public void setupSystemUnderTest() {
        logger.info("Setting up system under test");
        initialiseDataSources();
        deleteEnmUsers();
        createEnmUser();
        createAndSyncNodes();
    }

    @AfterSuite(alwaysRun = true)
    public void cleanUp() {
        logger.info("Executing TAF clean up");
        deleteSubscriptions();
        deleteEnmUsers();
        cleanUpAddedNodes();
    }

    private void createAndSyncNodes() {
        final TestScenario createAndSyncNodesScenario = scenario("Create and Sync Nodes")
            .addFlow(addSyncNodesSubFlow())
            .build();

        getTestScenarioRunner().start(createAndSyncNodesScenario);
    }

    private void cleanUpAddedNodes() {
        logger.info("Clean up nodes");
        final TestScenario cleanUpAddedNodesScenario = scenario("Clean Up added nodes")
            .addFlow(cleanUpDpsMosFlow())
            .addFlow(cleanUpAddedNodesFlow())
            .addFlow(restoreNetsimNeState())
            .build();

        getTestScenarioRunner().start(cleanUpAddedNodesScenario);
    }

    private TestStepFlow cleanUpDpsMosFlow() {
        return flow("Clean up DPS MOs - FLOW")
            .addSubFlow(loginLogoutFlows.loginDefaultUser())
            .addTestStep(annotatedMethod(this, CmSubscribedEventsNbiPreAndPostConditions.CLEAN_UP_DPS_MANAGED_OBJECTS))
            .withDataSources(dataSource(DPS_NETWORK_ELEMENT_DATA_SOURCE))
            .build();
    }

    private TestStepFlow restoreNetsimNeState() {
        return flow("Restore Netsim state and clean up ComEcim MOs - FLOW")
            .addTestStep(annotatedMethod(this, CmSubscribedEventsNbiPreAndPostConditions.RESTORE_NETSIM_NE_STATE))
            .withDataSources(dataSource(ADDED_NODES))
            .build();
    }

    /**
     * This test step checks if the NetworkElement name is still present on ENM, and if so delete it
     *
     * @param dpsNetworkElement - DataRecord representation of the DPS Network Element to cleanup
     */
    @TestStep(id = CLEAN_UP_DPS_MANAGED_OBJECTS)
    public void cleanUpDpsMosTestStep(@Input(DPS_NETWORK_ELEMENT_DATA_SOURCE) final DpsNetworkElement dpsNetworkElement) {
        final String networkElementForCleanup = dpsNetworkElement.getNetworkElementId();
        if (doesMoExist("NetworkElement=" + networkElementForCleanup)) {
            logger.info("Cleaning up Network Element: {}", networkElementForCleanup);
            cliOperatorProvider.executeCliCommand(String.format("cmedit delete NetworkElement=%s --ALL", networkElementForCleanup));
        }
    }

    /**
     * This test step restores the state of the netsim node used in the SubscriptionAndEventHandling suite
     *
     * @param nodeToCleanup - name of the netsim node to stop and restore
     */
    @TestStep(id = RESTORE_NETSIM_NE_STATE)
    public void restoreNetsimStateTestStep(@Input(ADDED_NODES) NetworkNode nodeToCleanup) {
        final NetSimCommandHandler netSimCommandHandler = NetSimCommandHandler.getInstance(HostConfigurator.getAllNetsimHosts());

        for (final NetworkElement netsimNetworkElement : netSimCommandHandler.getAllNEs()) {
            if (netsimNetworkElement.getName().equals(nodeToCleanup.getNetworkElementId())) {
                final String restoreImagePath = String.format("/netsim/netsimdir/%s/allsaved/dbs/%s_%s", netsimNetworkElement.getSimulationName(), "curr", netsimNetworkElement.getName());
                logger.info("Restoring state of netsim node {} from path {}", netsimNetworkElement.getName(), restoreImagePath);
                netsimNetworkElement.exec(NetSimCommands.stop());
                netsimNetworkElement.exec(NetSimCommands.restorenedatabase(restoreImagePath));
                netsimNetworkElement.exec(NetSimCommands.start());
            }
        }
    }

    public TestStepFlow addSyncNodesSubFlow() {
        return flow("Add and sync node - SUB FLOW")
            .addSubFlow(loginLogoutFlows.loginDefaultUser())
            .addSubFlow(startNodeInNetsim())
            .addSubFlow(nodeIntegrationFlows.addNode())
            .addSubFlow(nodeIntegrationFlows.syncNode())
            .addSubFlow(loginLogoutFlows.logout())
            .withDataSources(dataSource(NODES_TO_ADD))
            .build();
    }

    public TestStepFlow startNodeInNetsim() {
        return flow("Start node in netsim - FLOW")
            .addTestStep(annotatedMethod(this, CmSubscribedEventsNbiPreAndPostConditions.START_NETSIM_NETWORK_ELEMENT))
            .withDataSources(dataSource(NODES_TO_ADD))
            .build();
    }

    @TestStep(id = START_NETSIM_NETWORK_ELEMENT)
    public void startNetsimNetworkElement(@Input(NODES_TO_ADD) final NetworkNode node) {
        try {
            logger.info("Starting node {} in netsim", node.getNetworkElementId());
            netsimOperatorProvider.get().startNode(node.getNetworkElementId());
        } catch (final Exception e) {
            throw new AssertionError(String.format("Netsim node preparation failed for node %s: %s", node.getFdn(), e.getMessage()));
        }
    }

    private void createEnmUser() {
        final TestScenario scenario = scenario("Creating CM Subscribed Events Administrator User").addFlow(userManagementTestFlows.createUser())
            .build();
        getTestScenarioRunner().start(scenario);
    }

    private void deleteEnmUsers() {
        final TestScenario scenario = scenario("Deleting Setup Users - SCENARIO").addFlow(gimCleanupFlows.cleanUp(USER)).build();
        getTestScenarioRunner().start(scenario);
    }

    private void deleteSubscriptions() {
        final TestScenario scenario = scenario("Deleting leftover Subscriptions - SCENARIO").addFlow(
            cmSubscribedEventsNbiTestFlows.subscriptionCleanupFlow()).build();
        getTestScenarioRunner().start(scenario);
    }

    private void initialiseDataSources() {
        initDataSourceFromCsv(CommonDataSources.USERS_TO_CREATE);
        initDataSourceFromCsv(CommonDataSources.USER_TO_CLEAN_UP);
    }

    private boolean doesMoExist(String networkElement) {
        logger.info("Checking if FDN {} exists", networkElement);
        final EnmCliResponse response = cliOperatorProvider.executeCliCommand(String.format("%s %s", "cmedit get", networkElement));
        logger.debug("FDN cmedit get response: {}", response.toString());
        return !EnmCliResponseMatcher.hasLineContaining("0 instance(s)").matches(response);
    }

    private TestScenarioRunner getTestScenarioRunner() {
        return runner()
            .withListener(new LoggingScenarioListener())
            .withDefaultExceptionHandler(ScenarioExceptionHandler.PROPAGATE)
            .build();
    }

    private TestStepFlow cleanUpAddedNodesFlow() {
        return flow("Remove nodes added as part of TAF")
            .addSubFlow(loginLogoutFlows.loginDefaultUser())
            .addSubFlow(nodeIntegrationFlows.deleteNode()).withDataSources(dataSource(ADDED_NODES).allowEmpty())
            .addSubFlow(loginLogoutFlows.logout())
            .build();
    }
}