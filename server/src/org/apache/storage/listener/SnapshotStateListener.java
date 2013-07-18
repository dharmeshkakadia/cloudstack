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

package org.apache.storage.listener;

import javax.inject.Inject;

import org.apache.cloudstack.framework.events.EventBus;
import org.apache.cloudstack.framework.events.EventBusException;
import org.apache.event.EventCategory;
import org.apache.log4j.Logger;
import org.apache.server.ManagementServer;
import org.apache.storage.Snapshot;
import org.apache.storage.SnapshotVO;
import org.apache.storage.Snapshot.Event;
import org.apache.storage.Snapshot.State;
import org.apache.utils.component.ComponentContext;
import org.apache.utils.fsm.StateListener;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.util.HashMap;
import java.util.Map;

public class SnapshotStateListener implements StateListener<State, Event, SnapshotVO> {

    protected static EventBus _eventBus = null;

    private static final Logger s_logger = Logger.getLogger(VolumeStateListener.class);

    public SnapshotStateListener() {

    }

    @Override
    public boolean preStateTransitionEvent(State oldState, Event event, State newState, SnapshotVO vo, boolean status, Object opaque) {
        pubishOnEventBus(event.name(), "preStateTransitionEvent", vo, oldState, newState);
        return true;
    }

    @Override
    public boolean postStateTransitionEvent(State oldState, Event event, State newState, SnapshotVO vo, boolean status, Object opaque) {
        pubishOnEventBus(event.name(), "postStateTransitionEvent", vo, oldState, newState);
        return true;
    }

    private void pubishOnEventBus(String event, String status, Snapshot vo, State oldState, State newState) {

        try {
            _eventBus = ComponentContext.getComponent(EventBus.class);
        } catch(NoSuchBeanDefinitionException nbe) {
            return; // no provider is configured to provide events bus, so just return
        }

        String resourceName = getEntityFromClassName(Snapshot.class.getName());
        org.apache.cloudstack.framework.events.Event eventMsg =  new org.apache.cloudstack.framework.events.Event(
                ManagementServer.Name,
                EventCategory.RESOURCE_STATE_CHANGE_EVENT.getName(),
                event,
                resourceName,
                vo.getUuid());
        Map<String, String> eventDescription = new HashMap<String, String>();
        eventDescription.put("resource", resourceName);
        eventDescription.put("id", vo.getUuid());
        eventDescription.put("old-state", oldState.name());
        eventDescription.put("new-state", newState.name());
        eventMsg.setDescription(eventDescription);
        try {
            _eventBus.publish(eventMsg);
        } catch (EventBusException e) {
            s_logger.warn("Failed to publish state change event on the the event bus.");
        }
    }

    private String getEntityFromClassName(String entityClassName) {
        int index = entityClassName.lastIndexOf(".");
        String entityName = entityClassName;
        if (index != -1) {
            entityName = entityClassName.substring(index+1);
        }
        return entityName;
    }
}