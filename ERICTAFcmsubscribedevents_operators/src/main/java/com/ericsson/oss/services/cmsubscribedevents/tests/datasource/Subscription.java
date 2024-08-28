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
 *  DataRecord representation of the Subscription parameters,
 *  bound to subscriptions.csv for data source
 */
public interface Subscription extends DataRecord {

    /**
     * Scope Type provided to specify the objects selected.
     *
     * @return String - scopeType
     */
    String getScopeType();

    /**
     * Scope Level provided to specify the level object of objects below the specified Scope Type
     *
     * @return int - scopeLevel
     */
    int getScopeLevel();

    /**
     * Notification Types provided to specify notifications to be forwarded to recipient address.
     *
     * @return String [] - notificationTypes
     */
    String[] getNotificationTypes();

    /**
     * ID generated as the primary key for any given Subscription.
     *
     * @return String - id
     */
    String getId();

    /**
     * Object Class representing MO class names
     *
     * @return String - objectClass
     */
    String getObjectClass();

    /**
     * Object Instance representing FDN of an MO
     *
     * @return String - objectInstance
     */
    String getObjectInstance();
}