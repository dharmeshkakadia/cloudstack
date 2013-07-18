// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.


package org.apache.utils.exception;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * CSExceptionErrorCode lists the CloudStack error codes that correspond
 * to a each exception thrown by the CloudStack API.
 */

public class CSExceptionErrorCode {

    public static final Logger s_logger = Logger.getLogger(CSExceptionErrorCode.class.getName());

    // Declare a hashmap of CloudStack Error Codes for Exceptions.
    protected static final HashMap<String, Integer> ExceptionErrorCodeMap;

    static {
        try {
            ExceptionErrorCodeMap = new HashMap<String, Integer>();
            ExceptionErrorCodeMap.put("org.apache.utils.exception.CloudRuntimeException", 4250);
            ExceptionErrorCodeMap.put("org.apache.utils.exception.ExecutionException", 4260);
            ExceptionErrorCodeMap.put("org.apache.utils.exception.HypervisorVersionChangedException", 4265);
            ExceptionErrorCodeMap.put("org.apache.exception.CloudException", 4275);
            ExceptionErrorCodeMap.put("org.apache.exception.AccountLimitException", 4280);
            ExceptionErrorCodeMap.put("org.apache.exception.AgentUnavailableException", 4285);
            ExceptionErrorCodeMap.put("org.apache.exception.CloudAuthenticationException", 4290);
            ExceptionErrorCodeMap.put("org.apache.exception.ConcurrentOperationException", 4300);
            ExceptionErrorCodeMap.put("org.apache.exception.ConflictingNetworkSettingsException", 4305);
            ExceptionErrorCodeMap.put("org.apache.exception.DiscoveredWithErrorException", 4310);
            ExceptionErrorCodeMap.put("org.apache.exception.HAStateException", 4315);
            ExceptionErrorCodeMap.put("org.apache.exception.InsufficientAddressCapacityException", 4320);
            ExceptionErrorCodeMap.put("org.apache.exception.InsufficientCapacityException", 4325);
            ExceptionErrorCodeMap.put("org.apache.exception.InsufficientNetworkCapacityException", 4330);
            ExceptionErrorCodeMap.put("org.apache.exception.InsufficientServerCapacityException", 4335);
            ExceptionErrorCodeMap.put("org.apache.exception.InsufficientStorageCapacityException", 4340);
            ExceptionErrorCodeMap.put("org.apache.exception.InternalErrorException", 4345);
            ExceptionErrorCodeMap.put("org.apache.exception.InvalidParameterValueException", 4350);
            ExceptionErrorCodeMap.put("org.apache.exception.ManagementServerException", 4355);
            ExceptionErrorCodeMap.put("org.apache.exception.NetworkRuleConflictException", 4360);
            ExceptionErrorCodeMap.put("org.apache.exception.PermissionDeniedException", 4365);
            ExceptionErrorCodeMap.put("org.apache.exception.ResourceAllocationException", 4370);
            ExceptionErrorCodeMap.put("org.apache.exception.ResourceInUseException", 4375);
            ExceptionErrorCodeMap.put("org.apache.exception.ResourceUnavailableException", 4380);
            ExceptionErrorCodeMap.put("org.apache.exception.StorageUnavailableException", 4385);
            ExceptionErrorCodeMap.put("org.apache.exception.UnsupportedServiceException", 4390);
            ExceptionErrorCodeMap.put("org.apache.exception.VirtualMachineMigrationException", 4395);
            ExceptionErrorCodeMap.put("org.apache.async.AsyncCommandQueued", 4540);
            ExceptionErrorCodeMap.put("org.apache.exception.RequestLimitException", 4545);

            // Have a special error code for ServerApiException when it is
            // thrown in a standalone manner when failing to detect any of the above
            // standard exceptions.
            ExceptionErrorCodeMap.put("org.apache.cloudstack.api.ServerApiException", 9999);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }

    public static HashMap<String, Integer> getErrCodeList() {
        return ExceptionErrorCodeMap;
    }

    public static int getCSErrCode(String exceptionName) {
        if (ExceptionErrorCodeMap.containsKey(exceptionName)) {
            return ExceptionErrorCodeMap.get(exceptionName);
        } else {
            s_logger.info("Could not find exception: " + exceptionName + " in error code list for exceptions");
            return -1;
        }
    }

    public static String getCurMethodName() {
        StackTraceElement stackTraceCalls[] = (new Throwable()).getStackTrace();
        return stackTraceCalls[1].toString();
    }
}
