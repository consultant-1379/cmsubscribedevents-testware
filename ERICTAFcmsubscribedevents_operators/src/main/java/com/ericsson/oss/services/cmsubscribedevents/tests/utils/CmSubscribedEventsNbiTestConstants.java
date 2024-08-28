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

package com.ericsson.oss.services.cmsubscribedevents.tests.utils;

/**
 * Constants used in testing CM Subscribed Events NBI TAF
 */
public class CmSubscribedEventsNbiTestConstants {

    public static final String SUBSCRIPTIONS_URI = "/cm/subscribed-events/v1/subscriptions";
    public static final String ID_RETRIEVED_FROM_TEST_STEP = "idRetrievedFromCreateTestStep";
    public static final String SUBSCRIPTIONS_DATA_SOURCE = "subscriptions";
    public static final String SUBSCRIPTION_ID_DATA_SOURCE = "subscriptionsIdDataSource";
    public static final String SUBSCRIPTION_RECIPIENT_HOST = "subscriptionRecipientHost";
    public static final String DPS_NETWORK_ELEMENT_DATA_SOURCE = "dpsNetworkElement";
    public static final String SUBSCRIPTION_RECIPIENT_PORT = "subscriptionRecipientPort";
    public static final String SUBSCRIPTION_RECIPIENT_PATH = "subscriptionRecipientPath";
    public static final String IS_NOTIFY_MOI_CHANGES_TEST_CONTEXT = "isNotifyMOIChanges";
    public static final String APPLICATION_HAL_JSON = "application/hal+json";
    public static final String ACCEPT = "Accept";
    public static final String UTF_8 = "UTF-8";

    private CmSubscribedEventsNbiTestConstants() {
    }

}