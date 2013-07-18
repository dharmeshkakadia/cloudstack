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
package org.apache.resource;

import java.util.List;
import java.util.Map;

import org.apache.agent.api.StartupAnswer;
import org.apache.agent.api.StartupCommand;
import org.apache.exception.ConnectionException;
import org.apache.host.HostVO;
import org.apache.resource.ServerResource;
import org.apache.resource.UnableDeleteHostException;
import org.apache.utils.component.Adapter;

public interface ResourceStateAdapter extends Adapter {
    static public enum Event {
        CREATE_HOST_VO_FOR_CONNECTED,
        CREATE_HOST_VO_FOR_DIRECT_CONNECT,
        DELETE_HOST,
    }
    
    static public class DeleteHostAnswer {
        private boolean isContinue;
        private boolean isException;
        
        public DeleteHostAnswer(boolean isContinue) {
            this.isContinue = isContinue;
            this.isException = false;
        }
        
        public DeleteHostAnswer(boolean isContinue, boolean isException) {
            this.isContinue = isContinue;
            this.isException = isException;
        }
        
        public boolean getIsContinue() {
            return this.isContinue;
        }
        
        public boolean getIsException() {
            return this.isException;
        }
    }
    
    public HostVO createHostVOForConnectedAgent(HostVO host, StartupCommand[] cmd);
    
    public HostVO createHostVOForDirectConnectAgent(HostVO host, final StartupCommand[] startup, ServerResource resource, Map<String, String> details, List<String> hostTags);
    
    public DeleteHostAnswer deleteHost(HostVO host, boolean isForced, boolean isForceDeleteStorage) throws UnableDeleteHostException;
}