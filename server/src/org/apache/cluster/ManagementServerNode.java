// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package org.apache.cluster;

import javax.ejb.Local;

import org.apache.log4j.Logger;
import org.apache.utils.component.AdapterBase;
import org.apache.utils.component.ComponentLifecycle;
import org.apache.utils.component.SystemIntegrityChecker;
import org.apache.utils.exception.CloudRuntimeException;
import org.apache.utils.net.MacAddress;
import org.springframework.stereotype.Component;

@Component
@Local(value = {SystemIntegrityChecker.class})
public class ManagementServerNode extends AdapterBase implements SystemIntegrityChecker {
	private final Logger s_logger = Logger.getLogger(ManagementServerNode.class);
    
	private static final long s_nodeId = MacAddress.getMacAddress().toLong();
    
    public static enum State { Up, Down };

    public ManagementServerNode() {
    	setRunLevel(ComponentLifecycle.RUN_LEVEL_FRAMEWORK_BOOTSTRAP);
    }
    
    @Override
    public void check() {
        if (s_nodeId <= 0) {
            throw new CloudRuntimeException("Unable to get the management server node id");
        }
    }
    
    public static long getManagementServerId() {
        return s_nodeId;
    }
    
    @Override
    public boolean start() {
    	try {
    		check();
    	} catch (Exception e) {
			s_logger.error("System integrity check exception", e);
			System.exit(1);
    	}
    	return true;
    }
}