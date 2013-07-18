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
package org.apache.network.element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Local;
import javax.inject.Inject;
import javax.naming.ConfigurationException;

import org.apache.agent.api.to.LoadBalancerTO;
import org.apache.configuration.Config;
import org.apache.configuration.dao.ConfigurationDao;
import org.apache.deploy.DeployDestination;
import org.apache.exception.ConcurrentOperationException;
import org.apache.exception.InsufficientCapacityException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.exception.UnsupportedServiceException;
import org.apache.log4j.Logger;
import org.apache.network.Network;
import org.apache.network.NetworkModel;
import org.apache.network.PhysicalNetworkServiceProvider;
import org.apache.network.PublicIpAddress;
import org.apache.network.Network.Capability;
import org.apache.network.Network.Provider;
import org.apache.network.Network.Service;
import org.apache.network.Networks.TrafficType;
import org.apache.network.dao.NetworkDao;
import org.apache.network.element.IpDeployer;
import org.apache.network.element.LoadBalancingServiceProvider;
import org.apache.network.element.NetworkElement;
import org.apache.network.lb.ElasticLoadBalancerManager;
import org.apache.network.lb.LoadBalancingRule;
import org.apache.network.rules.LoadBalancerContainer;
import org.apache.offering.NetworkOffering;
import org.apache.offerings.dao.NetworkOfferingDao;
import org.apache.utils.component.AdapterBase;
import org.apache.vm.NicProfile;
import org.apache.vm.ReservationContext;
import org.apache.vm.VirtualMachine;
import org.apache.vm.VirtualMachineProfile;
import org.springframework.stereotype.Component;

@Component
@Local(value=NetworkElement.class)
public class ElasticLoadBalancerElement extends AdapterBase implements LoadBalancingServiceProvider, IpDeployer {
    private static final Logger s_logger = Logger.getLogger(ElasticLoadBalancerElement.class);
    private static final Map<Service, Map<Capability, String>> capabilities = setCapabilities();
    @Inject NetworkModel _networkManager;
    @Inject ElasticLoadBalancerManager _lbMgr;
    @Inject ConfigurationDao _configDao;
    @Inject NetworkOfferingDao _networkOfferingDao;
    @Inject NetworkDao _networksDao;
    
    boolean _enabled;
    TrafficType _frontEndTrafficType = TrafficType.Guest;
    
    private boolean canHandle(Network network, List<LoadBalancingRule> rules) {
        if (network.getGuestType() != Network.GuestType.Shared|| network.getTrafficType() != TrafficType.Guest) {
            s_logger.debug("Not handling network with type  " + network.getGuestType() + " and traffic type " + network.getTrafficType());
            return false;
        }
        
        Map<Capability, String> lbCaps = this.getCapabilities().get(Service.Lb);
        if (!lbCaps.isEmpty()) {
            String schemeCaps = lbCaps.get(Capability.LbSchemes);
            if (schemeCaps != null) {
                for (LoadBalancingRule rule : rules) {
                    if (!schemeCaps.contains(rule.getScheme().toString())) {
                        s_logger.debug("Scheme " + rules.get(0).getScheme() + " is not supported by the provider " + this.getName());
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    @Override
    public Provider getProvider() {
        return Provider.ElasticLoadBalancerVm;
    }
    
    @Override
    public Map<Service, Map<Capability, String>> getCapabilities() {
        return capabilities;
    }
    
    private static Map<Service, Map<Capability, String>> setCapabilities() {
        Map<Service, Map<Capability, String>> capabilities = new HashMap<Service, Map<Capability, String>>();
        
        Map<Capability, String> lbCapabilities = new HashMap<Capability, String>();
        lbCapabilities.put(Capability.SupportedLBAlgorithms, "roundrobin,leastconn,source");
        lbCapabilities.put(Capability.SupportedLBIsolation, "shared");
        lbCapabilities.put(Capability.SupportedProtocols, "tcp, udp");
        lbCapabilities.put(Capability.LbSchemes, LoadBalancerContainer.Scheme.Public.toString());
        
        capabilities.put(Service.Lb, lbCapabilities);   
        return capabilities;
    }

    @Override
    public boolean implement(Network network, NetworkOffering offering, DeployDestination dest, ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException,
            InsufficientCapacityException {
        
        return true;
    }

    @Override
    public boolean prepare(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, DeployDestination dest, ReservationContext context)
            throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException {
       
        return true;
    }

    @Override
    public boolean release(Network network, NicProfile nic, VirtualMachineProfile<? extends VirtualMachine> vm, ReservationContext context) throws ConcurrentOperationException,
            ResourceUnavailableException {
        
        return true;
    }

    @Override
    public boolean shutdown(Network network, ReservationContext context, boolean cleanup) throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO kill all loadbalancer vms by calling the ElasticLoadBalancerManager
        return false;
    }

    @Override
    public boolean destroy(Network network, ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO  kill all loadbalancer vms by calling the ElasticLoadBalancerManager
        return false;
    }
    
    @Override
    public boolean validateLBRule(Network network, LoadBalancingRule rule) {
        return true;
    }
    
    @Override
    public boolean applyLBRules(Network network, List<LoadBalancingRule> rules) throws ResourceUnavailableException {
        if (!canHandle(network, rules)) {
            return false;
        }
                
        return _lbMgr.applyLoadBalancerRules(network, rules);
    }


    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        
        super.configure(name, params);
        String enabled = _configDao.getValue(Config.ElasticLoadBalancerEnabled.key());
        _enabled = (enabled == null) ? false: Boolean.parseBoolean(enabled);
        if (_enabled) {
            String traffType = _configDao.getValue(Config.ElasticLoadBalancerNetwork.key());
            if ("guest".equalsIgnoreCase(traffType)) {
                _frontEndTrafficType = TrafficType.Guest;
            } else if ("public".equalsIgnoreCase(traffType)){
                _frontEndTrafficType = TrafficType.Public;
            } else
                throw new ConfigurationException("Traffic type for front end of load balancer has to be guest or public; found : " + traffType);
        }
        return true;
    }

    @Override
    public boolean isReady(PhysicalNetworkServiceProvider provider) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean shutdownProviderInstances(PhysicalNetworkServiceProvider provider, ReservationContext context) throws ConcurrentOperationException,
            ResourceUnavailableException {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean canEnableIndividualServices() {
        return false;
    }
    
    @Override
    public boolean verifyServicesCombination(Set<Service> services) {
        return true;
    }

    @Override
    public boolean applyIps(Network network, List<? extends PublicIpAddress> ipAddress, Set<Service> services) throws ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public IpDeployer getIpDeployer(Network network) {
        return this;
    }

    @Override
    public List<LoadBalancerTO> updateHealthChecks(Network network, List<LoadBalancingRule> lbrules) {
        return null;
    }

}
