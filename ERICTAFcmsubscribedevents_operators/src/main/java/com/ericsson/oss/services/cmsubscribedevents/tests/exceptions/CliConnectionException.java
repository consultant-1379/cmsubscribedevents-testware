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

package com.ericsson.oss.services.cmsubscribedevents.tests.exceptions;

/**
 * Exception Class for the TafCliToolShell.
 */
public class CliConnectionException extends  Exception{

    private static final long serialVersionUID = -392025124478290758L;

    /**
     * Exception to be thrown when there is an error with the TafCliToolShell.
     *
     */
    public CliConnectionException(final String errorMessage, final Throwable err){
        super(errorMessage,err);
    }
}
