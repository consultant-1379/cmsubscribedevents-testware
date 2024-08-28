/* ------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2023
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ----------------------------------------------------------------------------
 */

package com.ericsson.oss.services.cmsubscribedevents.tests.operators.providers;

import static com.google.common.base.Preconditions.checkState;

import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.oss.testware.enm.cli.EnmCliOperatorImpl;
import com.ericsson.oss.testware.enm.cli.EnmCliResponse;
import com.ericsson.oss.testware.security.authentication.tool.TafToolProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Provides an instance of {@link EnmCliOperatorImpl}.
 */
public class CliOperatorProvider {

    private static final int EXECUTE_CLI_COMMAND_ATTEMPTS = 5;

    @Inject
    private Provider<EnmCliOperatorImpl> provider;

    @Inject
    private TafToolProvider tafToolProvider;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Forwards call to {@link EnmCliOperatorImpl} to execute the given CM CLI command.
     *
     * @param command
     *            the command to execute
     * @return the {@link EnmCliResponse}
     */
    public EnmCliResponse executeCliCommand(final String command) {
        final EnmCliOperatorImpl cliOperator = provider.get();
        EnmCliResponse response = null;
        int attempts = 0;
        while (response == null) {
            attempts++;
            try {
                response = cliOperator.executeCliCommand(command, getHttpTool());
                logger.info("Command executed: {}", response.getCommandDto().toString());
            } catch (final Exception e) {
                logger.warn(e.getMessage());
                if (attempts >= EXECUTE_CLI_COMMAND_ATTEMPTS) {
                    throw e;
                }
            }
        }
        return response;
    }

    private HttpTool getHttpTool() {
        final HttpTool httpTool = tafToolProvider.getHttpTool();
        checkState(httpTool != null, "No HttpTool available");
        return httpTool;
    }
}