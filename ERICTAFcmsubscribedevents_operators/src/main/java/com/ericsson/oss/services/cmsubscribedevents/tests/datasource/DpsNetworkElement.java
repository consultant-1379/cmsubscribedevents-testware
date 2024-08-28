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

package com.ericsson.oss.services.cmsubscribedevents.tests.datasource;

import com.ericsson.cifwk.taf.datasource.DataRecord;

/**
 *  DataRecord representation of the DpsNetworkElement parameters,
 *  bound to dpsNetworkElement.csv for data source
 */
public interface DpsNetworkElement extends DataRecord {

    /**
     * The NetworkElement used to generate DPS events
     *
     * @return String - networkElementId
     */
    String getNetworkElementId();
}