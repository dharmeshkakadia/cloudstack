
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
package org.apache.network;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.naming.ConfigurationException;

import org.apache.agent.AgentManager;
import org.apache.agent.api.Answer;
import org.apache.agent.api.StartupCommand;
import org.apache.agent.api.StartupExternalFirewallCommand;
import org.apache.agent.api.routing.IpAssocCommand;
import org.apache.agent.api.routing.NetworkElementCommand;
import org.apache.agent.api.routing.RemoteAccessVpnCfgCommand;
import org.apache.agent.api.routing.SetFirewallRulesCommand;
import org.apache.agent.api.routing.SetPortForwardingRulesCommand;
import org.apache.agent.api.routing.SetStaticNatRulesCommand;
import org.apache.agent.api.routing.VpnUsersCfgCommand;
import org.apache.agent.api.to.FirewallRuleTO;
import org.apache.agent.api.to.IpAddressTO;
import org.apache.agent.api.to.PortForwardingRuleTO;
import org.apache.agent.api.to.StaticNatRuleTO;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.response.ExternalFirewallResponse;
import org.apache.cloudstack.network.ExternalNetworkDeviceManager.NetworkDevice;
import org.apache.configuration.Config;
import org.apache.configuration.dao.ConfigurationDao;
import org.apache.dc.DataCenter;
import org.apache.dc.DataCenterVO;
import org.apache.dc.Vlan;
import org.apache.dc.VlanVO;
import org.apache.dc.dao.DataCenterDao;
import org.apache.dc.dao.VlanDao;
import org.apache.exception.InsufficientCapacityException;
import org.apache.exception.InsufficientNetworkCapacityException;
import org.apache.exception.InvalidParameterValueException;
import org.apache.exception.ResourceUnavailableException;
import org.apache.host.DetailVO;
import org.apache.host.Host;
import org.apache.host.HostVO;
import org.apache.host.dao.HostDao;
import org.apache.host.dao.HostDetailsDao;
import org.apache.log4j.Logger;
import org.apache.network.IpAddress;
import org.apache.network.Network;
import org.apache.network.NetworkModel;
import org.apache.network.PhysicalNetworkServiceProvider;
import org.apache.network.PublicIpAddress;
import org.apache.network.RemoteAccessVpn;
import org.apache.network.VpnUser;
import org.apache.network.VpnUserVO;
import org.apache.network.Networks.TrafficType;
import org.apache.network.dao.*;
import org.apache.network.rules.FirewallRule;
import org.apache.network.rules.FirewallRuleVO;
import org.apache.network.rules.PortForwardingRule;
import org.apache.network.rules.StaticNat;
import org.apache.network.rules.FirewallRule.Purpose;
import org.apache.network.rules.dao.PortForwardingRulesDao;
import org.apache.offering.NetworkOffering;
import org.apache.offerings.NetworkOfferingVO;
import org.apache.offerings.dao.NetworkOfferingDao;
import org.apache.resource.ResourceManager;
import org.apache.resource.ResourceState;
import org.apache.resource.ResourceStateAdapter;
import org.apache.resource.ServerResource;
import org.apache.resource.UnableDeleteHostException;
import org.apache.user.Account;
import org.apache.user.AccountManager;
import org.apache.user.dao.AccountDao;
import org.apache.user.dao.UserStatisticsDao;
import org.apache.utils.NumbersUtil;
import org.apache.utils.Pair;
import org.apache.utils.component.AdapterBase;
import org.apache.utils.db.DB;
import org.apache.utils.db.GlobalLock;
import org.apache.utils.db.Transaction;
import org.apache.utils.exception.CloudRuntimeException;
import org.apache.utils.net.NetUtils;
import org.apache.utils.net.UrlUtil;
import org.apache.vm.NicVO;
import org.apache.vm.Nic.ReservationStrategy;
import org.apache.vm.dao.DomainRouterDao;
import org.apache.vm.dao.NicDao;

public abstract class ExternalFirewallDeviceManagerImpl extends AdapterBase implements ExternalFirewallDeviceManager, ResourceStateAdapter {

    @Inject HostDao _hostDao;
    @Inject NetworkServiceMapDao _ntwkSrvcProviderDao;
    @Inject DataCenterDao _dcDao;
    @Inject NetworkModel _networkModel;
    @Inject NetworkManager _networkMgr;
    @Inject InlineLoadBalancerNicMapDao _inlineLoadBalancerNicMapDao;
    @Inject NicDao _nicDao;
    @Inject AgentManager _agentMgr;
    @Inject ResourceManager _resourceMgr;
    @Inject IPAddressDao _ipAddressDao;
    @Inject VlanDao _vlanDao;
    @Inject NetworkOfferingDao _networkOfferingDao;
    @Inject AccountDao _accountDao;
    @Inject PhysicalNetworkDao _physicalNetworkDao;
    @Inject PhysicalNetworkServiceProviderDao _physicalNetworkServiceProviderDao;
    @Inject AccountManager _accountMgr;
    @Inject UserStatisticsDao _userStatsDao;
    @Inject NetworkDao _networkDao;
    @Inject DomainRouterDao _routerDao;
    @Inject LoadBalancerDao _loadBalancerDao;
    @Inject PortForwardingRulesDao _portForwardingRulesDao;
    @Inject ConfigurationDao _configDao;
    @Inject ExternalFirewallDeviceDao _externalFirewallDeviceDao;
    @Inject NetworkExternalFirewallDao _networkExternalFirewallDao;
    @Inject VpnUserDao _vpnUsersDao;
    @Inject HostDetailsDao _hostDetailDao;
    @Inject FirewallRulesDao _fwRulesDao;

    private static final org.apache.log4j.Logger s_logger = Logger.getLogger(ExternalFirewallDeviceManagerImpl.class);
    private long _defaultFwCapacity;

    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);
        _resourceMgr.registerResourceStateAdapter(this.getClass().getSimpleName(), this);
        _defaultFwCapacity = NumbersUtil.parseLong(_configDao.getValue(Config.DefaultExternalFirewallCapacity.key()), 50);
        return true;
    }

    @Override
    @DB
    public ExternalFirewallDeviceVO addExternalFirewall(long physicalNetworkId, String url, String username, String password, String deviceName, ServerResource resource) {
        String guid;
        PhysicalNetworkVO pNetwork=null;
        NetworkDevice ntwkDevice = NetworkDevice.getNetworkDevice(deviceName);
        long zoneId;

        if ((ntwkDevice == null) || (url == null) || (username == null) || (resource == null) || (password == null) ) {
            throw new InvalidParameterValueException("Atleast one of the required parameters (url, username, password," +
                    " server resource, zone id/physical network id) is not specified or a valid parameter.");
        }

        pNetwork = _physicalNetworkDao.findById(physicalNetworkId);
        if (pNetwork == null) {
            throw new InvalidParameterValueException("Could not find phyical network with ID: " + physicalNetworkId);
        }
        zoneId = pNetwork.getDataCenterId();

        PhysicalNetworkServiceProviderVO ntwkSvcProvider = _physicalNetworkServiceProviderDao.findByServiceProvider(pNetwork.getId(), ntwkDevice.getNetworkServiceProvder());
        if (ntwkSvcProvider == null ) {
            throw new CloudRuntimeException("Network Service Provider: " + ntwkDevice.getNetworkServiceProvder() + 
                    " is not enabled in the physical network: " + physicalNetworkId + "to add this device" );
        } else if (ntwkSvcProvider.getState() == PhysicalNetworkServiceProvider.State.Shutdown) {
            throw new CloudRuntimeException("Network Service Provider: " + ntwkSvcProvider.getProviderName() + 
                    " is not added or in shutdown state in the physical network: " + physicalNetworkId + "to add this device" );
        }

        URI uri;
        try {
            uri = new URI(url);
        } catch (Exception e) {
            s_logger.debug(e);
            throw new InvalidParameterValueException(e.getMessage());
        }

        String ipAddress = uri.getHost();
        Map hostDetails = new HashMap<String, String>();
        guid = getExternalNetworkResourceGuid(pNetwork.getId(), deviceName, ipAddress);
        hostDetails.put("name", guid);
        hostDetails.put("guid", guid);
        hostDetails.put("zoneId", String.valueOf(pNetwork.getDataCenterId()));
        hostDetails.put("ip", ipAddress);
        hostDetails.put("physicalNetworkId", String.valueOf(pNetwork.getId()));
        hostDetails.put("username", username);
        hostDetails.put("password", password);
        hostDetails.put("deviceName", deviceName);
        Map<String, String> configParams = new HashMap<String, String>();
        UrlUtil.parseQueryParameters(uri.getQuery(), false, configParams);
        hostDetails.putAll(configParams);

        // let the server resource to do parameters validation
        try {
            resource.configure(guid, hostDetails);
        } catch (ConfigurationException e) {
            throw new CloudRuntimeException(e.getMessage());
        }

        Host externalFirewall = _resourceMgr.addHost(zoneId, resource, Host.Type.ExternalFirewall, hostDetails);
        if (externalFirewall != null) {
            Transaction txn = Transaction.currentTxn();
            txn.start();

            boolean dedicatedUse = (configParams.get(ApiConstants.FIREWALL_DEVICE_DEDICATED) != null) ? Boolean.parseBoolean(configParams.get(ApiConstants.FIREWALL_DEVICE_DEDICATED)) : false;
            long capacity =  NumbersUtil.parseLong((String)configParams.get(ApiConstants.FIREWALL_DEVICE_CAPACITY), 0);
            if (capacity == 0) {
                capacity = _defaultFwCapacity;
            }

            ExternalFirewallDeviceVO fwDevice = new ExternalFirewallDeviceVO(externalFirewall.getId(), pNetwork.getId(), ntwkSvcProvider.getProviderName(), 
                    deviceName, capacity, dedicatedUse);

            _externalFirewallDeviceDao.persist(fwDevice);

            DetailVO hostDetail = new DetailVO(externalFirewall.getId(), ApiConstants.FIREWALL_DEVICE_ID, String.valueOf(fwDevice.getId()));
            _hostDetailDao.persist(hostDetail);

            txn.commit();
            return fwDevice;
        } else {
            return null;
        }
    }

    @Override
    public boolean deleteExternalFirewall(Long hostId) {
        HostVO externalFirewall = _hostDao.findById(hostId);
        if (externalFirewall == null) {
            throw new InvalidParameterValueException("Could not find an external firewall with ID: " + hostId);
        }

        DetailVO fwHostDetails = _hostDetailDao.findDetail(hostId, ApiConstants.FIREWALL_DEVICE_ID);
        long fwDeviceId = Long.parseLong(fwHostDetails.getValue());

        // check if any networks are using this balancer device
        List<NetworkExternalFirewallVO> networks = _networkExternalFirewallDao.listByFirewallDeviceId(fwDeviceId);
        if ((networks != null) && !networks.isEmpty()) {
            throw new CloudRuntimeException("Delete can not be done as there are networks using the firewall device ");
        }

        try {
            // put the host in maintenance state in order for it to be deleted
            externalFirewall.setResourceState(ResourceState.Maintenance);
            _hostDao.update(hostId, externalFirewall);
            _resourceMgr.deleteHost(hostId, false, false);

            // delete the external load balancer entry
            _externalFirewallDeviceDao.remove(fwDeviceId);
                return true;
        } catch (Exception e) {
            s_logger.debug("Failed to delete external firewall device due to " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Host> listExternalFirewalls(long physicalNetworkId, String deviceName) {
        List<Host> firewallHosts = new ArrayList<Host>();
        NetworkDevice fwNetworkDevice = NetworkDevice.getNetworkDevice(deviceName);
        PhysicalNetworkVO pNetwork=null;

        pNetwork = _physicalNetworkDao.findById(physicalNetworkId);
        if (pNetwork == null) {
            throw new InvalidParameterValueException("Could not find phyical network with ID: " + physicalNetworkId);
        }

        if ((pNetwork == null) || (fwNetworkDevice == null)) {
            throw new InvalidParameterValueException("Atleast one of ther required parameter physical networkId, device name is missing or invalid.");
        }

        PhysicalNetworkServiceProviderVO ntwkSvcProvider = _physicalNetworkServiceProviderDao.findByServiceProvider(pNetwork.getId(), fwNetworkDevice.getNetworkServiceProvder());
        if (ntwkSvcProvider == null) {
            return null; 
        }

        List<ExternalFirewallDeviceVO> fwDevices = _externalFirewallDeviceDao.listByPhysicalNetworkAndProvider(physicalNetworkId, ntwkSvcProvider.getProviderName());
        for (ExternalFirewallDeviceVO fwDevice : fwDevices) {
            firewallHosts.add(_hostDao.findById(fwDevice.getHostId()));
        }
        return firewallHosts;
    }
 
    public ExternalFirewallDeviceVO getExternalFirewallForNetwork(Network network) {
        NetworkExternalFirewallVO fwDeviceForNetwork = _networkExternalFirewallDao.findByNetworkId(network.getId());
        if (fwDeviceForNetwork != null) {
            long fwDeviceId = fwDeviceForNetwork.getExternalFirewallDeviceId();
            ExternalFirewallDeviceVO fwDevice = _externalFirewallDeviceDao.findById(fwDeviceId);
            assert(fwDevice != null);
            return fwDevice;
        }
        return null;
    }

    public void setExternalFirewallForNetwork(Network network, long externalFWDeviceID) {
        NetworkExternalFirewallVO fwDeviceForNetwork = new NetworkExternalFirewallVO(network.getId(), externalFWDeviceID);
        _networkExternalFirewallDao.persist(fwDeviceForNetwork);
    }

    @Override
    public ExternalFirewallDeviceVO findSuitableFirewallForNetwork(Network network) throws InsufficientCapacityException {
        long physicalNetworkId = network.getPhysicalNetworkId();
        List<ExternalFirewallDeviceVO> fwDevices = _externalFirewallDeviceDao.listByPhysicalNetwork(physicalNetworkId);

        // loop through the firewall device in the physical network and pick the first-fit 
        for (ExternalFirewallDeviceVO fwDevice: fwDevices) {
            // max number of guest networks that can be mapped to this device
            long fullCapacity = fwDevice.getCapacity();
            if (fullCapacity == 0) {
                fullCapacity = _defaultFwCapacity; // if capacity not configured then use the default
            }

            // get the list of guest networks that are mapped to this load balancer
            List<NetworkExternalFirewallVO> mappedNetworks = _networkExternalFirewallDao.listByFirewallDeviceId(fwDevice.getId()); 

            long usedCapacity = (mappedNetworks == null) ? 0 : mappedNetworks.size();
            if ((fullCapacity - usedCapacity) > 0) {
                return fwDevice;
            }
        }
        throw new InsufficientNetworkCapacityException("Unable to find a firewall provider with sufficient capcity " +
                " to implement the network", DataCenter.class, network.getDataCenterId());
    }

    @DB
    protected boolean freeFirewallForNetwork(Network network) {
        Transaction txn = Transaction.currentTxn();
        GlobalLock deviceMapLock =  GlobalLock.getInternLock("NetworkFirewallDeviceMap");
        try {
            if (deviceMapLock.lock(120)) {
                try {
                    NetworkExternalFirewallVO fwDeviceForNetwork = _networkExternalFirewallDao.findByNetworkId(network.getId());
                    if (fwDeviceForNetwork != null) {
                        _networkExternalFirewallDao.remove(fwDeviceForNetwork.getId());
                    }
                } catch (Exception exception) {
                    txn.rollback();
                    s_logger.error("Failed to release firewall device for the network" + network.getId() + " due to " + exception.getMessage());
                    return false;
                } finally {
                    deviceMapLock.unlock();
                }
            }
        } finally {
            deviceMapLock.releaseRef();
        }
        txn.commit();
        return true;
    }

    public String getExternalNetworkResourceGuid(long physicalNetworkId, String deviceName, String ip) {
        return physicalNetworkId + "-" + deviceName + "-" + ip;
    }

    public ExternalFirewallResponse createExternalFirewallResponse(Host externalFirewall) {
        Map<String, String> fwDetails = _hostDetailDao.findDetails(externalFirewall.getId());
        ExternalFirewallResponse response = new ExternalFirewallResponse();
        response.setId(externalFirewall.getUuid());
        response.setIpAddress(externalFirewall.getPrivateIpAddress());
        response.setUsername(fwDetails.get("username"));
        response.setPublicInterface(fwDetails.get("publicInterface"));
        response.setUsageInterface(fwDetails.get("usageInterface"));
        response.setPrivateInterface(fwDetails.get("privateInterface"));
        response.setPublicZone(fwDetails.get("publicZone"));
        response.setPrivateZone(fwDetails.get("privateZone"));
        response.setNumRetries(fwDetails.get("numRetries"));
        response.setTimeout(fwDetails.get("timeout"));
        return response;
    }

    @Override
    public boolean manageGuestNetworkWithExternalFirewall(boolean add, Network network) throws ResourceUnavailableException, InsufficientCapacityException {
        if (network.getTrafficType() != TrafficType.Guest) {
            s_logger.trace("External firewall can only be used for add/remove guest networks.");
            return false;
        }

        long zoneId = network.getDataCenterId();
        DataCenterVO zone = _dcDao.findById(zoneId);
        HostVO externalFirewall = null;

        if (add) {
            GlobalLock deviceMapLock =  GlobalLock.getInternLock("NetworkFirewallDeviceMap");
            try {
                if (deviceMapLock.lock(120)) {
                    try {
                        ExternalFirewallDeviceVO device  = findSuitableFirewallForNetwork(network); 
                        long externalFirewallId = device.getId();

                        NetworkExternalFirewallVO networkFW = new NetworkExternalFirewallVO(network.getId(), externalFirewallId);
                        _networkExternalFirewallDao.persist(networkFW);

                        externalFirewall = _hostDao.findById(device.getHostId());
                    } finally {
                        deviceMapLock.unlock();
                    }
                }
            } finally {
                deviceMapLock.releaseRef();
            }
        } else {
            ExternalFirewallDeviceVO fwDeviceVO = getExternalFirewallForNetwork(network);
            if (fwDeviceVO == null) {
                s_logger.warn("Network shutdown requested on external firewall element, which did not implement the network." +
                              " Either network implement failed half way through or already network shutdown is completed.");
                return true;
            }
            externalFirewall = _hostDao.findById(fwDeviceVO.getHostId());
        }

        Account account = _accountDao.findByIdIncludingRemoved(network.getAccountId());
        
        NetworkOffering offering = _networkOfferingDao.findById(network.getNetworkOfferingId());      
        boolean sharedSourceNat = offering.getSharedSourceNat();
        
        IPAddressVO sourceNatIp = null;
        if (!sharedSourceNat) {
            // Get the source NAT IP address for this account          
            List<? extends IpAddress> sourceNatIps = _networkModel.listPublicIpsAssignedToAccount(network.getAccountId(), 
                    zoneId, true);

            if (sourceNatIps.size() != 1) {
                String errorMsg = "External firewall was unable to find the source NAT IP address for account " 
            + account.getAccountName();
                s_logger.error(errorMsg);
                return true;
            } else {
                sourceNatIp = _ipAddressDao.findById(sourceNatIps.get(0).getId());
            }
        }

        // Send a command to the external firewall to implement or shutdown the guest network
        long guestVlanTag = Long.parseLong(network.getBroadcastUri().getHost());
        String guestVlanGateway = network.getGateway();
        String guestVlanCidr = network.getCidr();
        String sourceNatIpAddress = null;
        String publicVlanTag = null;

        if (sourceNatIp != null) {
            sourceNatIpAddress = sourceNatIp.getAddress().addr();
            VlanVO publicVlan = _vlanDao.findById(sourceNatIp.getVlanId());
            publicVlanTag = publicVlan.getVlanTag();
        }

        // Get network rate
        Integer networkRate = _networkModel.getNetworkRate(network.getId(), null);

        IpAddressTO ip = new IpAddressTO(account.getAccountId(), sourceNatIpAddress, add, false, !sharedSourceNat, publicVlanTag, null, null, null, networkRate, false);
        IpAddressTO[] ips = new IpAddressTO[1];
        ips[0] = ip;
        IpAssocCommand cmd = new IpAssocCommand(ips);
        cmd.setAccessDetail(NetworkElementCommand.GUEST_NETWORK_GATEWAY, guestVlanGateway);
        cmd.setAccessDetail(NetworkElementCommand.GUEST_NETWORK_CIDR, guestVlanCidr);
        cmd.setAccessDetail(NetworkElementCommand.GUEST_VLAN_TAG, String.valueOf(guestVlanTag));
        Answer answer = _agentMgr.easySend(externalFirewall.getId(), cmd);

        List<String> reservedIpAddressesForGuestNetwork = _nicDao.listIpAddressInNetwork(network.getId());
        
        if (answer == null || !answer.getResult()) {
            String action = add ? "implement" : "shutdown";
            String answerDetails = (answer != null) ? answer.getDetails() : "answer was null";
            String msg = "External firewall was unable to " + action + " the guest network on the external firewall in zone " + zone.getName() + " due to " + answerDetails;
            s_logger.error(msg);
            if (!add && (!reservedIpAddressesForGuestNetwork.contains(network.getGateway()))) {
                // If we failed the implementation as well, then just return, no complain
                s_logger.error("Skip the shutdown of guest network on SRX because it seems we didn't implement it as well");
                return true;
            }
            throw new ResourceUnavailableException(msg, DataCenter.class, zoneId);
        }

        if (add && (!reservedIpAddressesForGuestNetwork.contains(network.getGateway()))) {
            // Insert a new NIC for this guest network to reserve the gateway address
            _networkMgr.savePlaceholderNic(network,  network.getGateway(), null, null);
        }
        
        // Delete any mappings used for inline external load balancers in this network
        List<NicVO> nicsInNetwork = _nicDao.listByNetworkId(network.getId());
        for (NicVO nic : nicsInNetwork) {
            InlineLoadBalancerNicMapVO mapping = _inlineLoadBalancerNicMapDao.findByNicId(nic.getId());
            
            if (mapping != null) {
                _nicDao.expunge(mapping.getNicId());
                _inlineLoadBalancerNicMapDao.expunge(mapping.getId());
            }
        }
        
        // on network shutdown, delete placeHolder nics used for the firewall device
        if (!add) {
            List<NicVO> nics = _nicDao.listByNetworkId(network.getId());
            for (NicVO nic : nics) {
                if (nic.getVmType() == null && nic.getReservationStrategy().equals(ReservationStrategy.PlaceHolder) && nic.getIp4Address().equals(network.getGateway())) {
                    s_logger.debug("Removing placeholder nic " + nic + " for the network " + network);
                    _nicDao.remove(nic.getId());
                }
            }
            freeFirewallForNetwork(network);
        }

        String action = add ? "implemented" : "shut down";
        s_logger.debug("External firewall has " + action + " the guest network for account " + account.getAccountName() + "(id = " + account.getAccountId() + ") with VLAN tag " + guestVlanTag);

        return true;
    }


    @Override
    public boolean applyFirewallRules(Network network, List<? extends FirewallRule> rules) throws ResourceUnavailableException {
        // Find the external firewall in this zone
        long zoneId = network.getDataCenterId();
        DataCenterVO zone = _dcDao.findById(zoneId);
        ExternalFirewallDeviceVO fwDeviceVO = getExternalFirewallForNetwork(network);
        // During destroy, device reference may already been clean up, then we just return true
        if (fwDeviceVO == null) {
            return true;
        }
        HostVO externalFirewall = _hostDao.findById(fwDeviceVO.getHostId());

        assert(externalFirewall != null);

        if (network.getState() == Network.State.Allocated) {
            s_logger.debug("External firewall was asked to apply firewall rules for network with ID " + network.getId() + "; this network is not implemented. Skipping backend commands.");
            return true;
        }

        List<FirewallRuleTO> rulesTO = new ArrayList<FirewallRuleTO>();
        NetworkVO networkVO = _networkDao.findById(network.getId());
        NetworkOfferingVO offering = _networkOfferingDao.findById(networkVO.getNetworkOfferingId());
        Boolean defaultEgressPolicy = offering.getEgressDefaultPolicy();

        for (FirewallRule rule : rules) {
            if (rule.getSourceCidrList() == null && (rule.getPurpose() == Purpose.Firewall || rule.getPurpose() == Purpose.NetworkACL)) {
                _fwRulesDao.loadSourceCidrs((FirewallRuleVO)rule);
            }
            FirewallRuleTO ruleTO;
            if (rule.getPurpose() == Purpose.Firewall && rule.getTrafficType() == FirewallRule.TrafficType.Egress) {
                String guestVlanTag = network.getBroadcastUri().getHost();
                String guestCidr = network.getCidr();
                ruleTO = new FirewallRuleTO(rule, guestVlanTag, rule.getTrafficType(), guestCidr, defaultEgressPolicy, rule.getType());
            } else {
                IpAddress sourceIp = _networkModel.getIp(rule.getSourceIpAddressId());
                Vlan vlan = _vlanDao.findById(sourceIp.getVlanId());

                ruleTO = new FirewallRuleTO(rule, vlan.getVlanTag(), sourceIp.getAddress().addr());
            }
            rulesTO.add(ruleTO);
        }

        //Firewall rules configured for staticNAT/PF
        sendFirewallRules(rulesTO, zone, externalFirewall.getId());

        return true;
            }
    
    public boolean applyStaticNatRules(Network network, List<? extends StaticNat> rules) throws ResourceUnavailableException {
        long zoneId = network.getDataCenterId();
        DataCenterVO zone = _dcDao.findById(zoneId);
        ExternalFirewallDeviceVO fwDeviceVO = getExternalFirewallForNetwork(network);
        HostVO externalFirewall = _hostDao.findById(fwDeviceVO.getHostId());

        assert(externalFirewall != null);

        if (network.getState() == Network.State.Allocated) {
            s_logger.debug("External firewall was asked to apply firewall rules for network with ID " + network.getId() + "; this network is not implemented. Skipping backend commands.");
            return true;
        }

        List<StaticNatRuleTO> staticNatRules = new ArrayList<StaticNatRuleTO>();
        
        for (StaticNat rule : rules) {
            IpAddress sourceIp = _networkModel.getIp(rule.getSourceIpAddressId());
            Vlan vlan = _vlanDao.findById(sourceIp.getVlanId());

            StaticNatRuleTO ruleTO = new StaticNatRuleTO(0,vlan.getVlanTag(), sourceIp.getAddress().addr(), -1, -1, rule.getDestIpAddress(), -1, -1, "any", rule.isForRevoke(), false);
            staticNatRules.add(ruleTO);
        }

        sendStaticNatRules(staticNatRules, zone, externalFirewall.getId());

        return true;
    }

    protected void sendFirewallRules(List<FirewallRuleTO> firewallRules, DataCenter zone, long externalFirewallId) throws ResourceUnavailableException {
        if (!firewallRules.isEmpty()) {
        	SetFirewallRulesCommand cmd = new SetFirewallRulesCommand(firewallRules);
            Answer answer = _agentMgr.easySend(externalFirewallId, cmd);
            if (answer == null || !answer.getResult()) {
                String details = (answer != null) ? answer.getDetails() : "details unavailable";
                String msg = "External firewall was unable to apply static nat rules to the SRX appliance in zone " + zone.getName() + " due to: " + details + ".";
                s_logger.error(msg);
                throw new ResourceUnavailableException(msg, DataCenter.class, zone.getId());
            }
        }
    }

    protected void sendStaticNatRules(List<StaticNatRuleTO> staticNatRules, DataCenter zone, long externalFirewallId) throws ResourceUnavailableException {
        if (!staticNatRules.isEmpty()) {
            SetStaticNatRulesCommand cmd = new SetStaticNatRulesCommand(staticNatRules, null);
            Answer answer = _agentMgr.easySend(externalFirewallId, cmd);
            if (answer == null || !answer.getResult()) {
                String details = (answer != null) ? answer.getDetails() : "details unavailable";
                String msg = "External firewall was unable to apply static nat rules to the SRX appliance in zone " + zone.getName() + " due to: " + details + ".";
                s_logger.error(msg);
                throw new ResourceUnavailableException(msg, DataCenter.class, zone.getId());
            }
        }
    }

    protected void sendPortForwardingRules(List<PortForwardingRuleTO> portForwardingRules, DataCenter zone, long externalFirewallId) throws ResourceUnavailableException {
        if (!portForwardingRules.isEmpty()) {
            SetPortForwardingRulesCommand cmd = new SetPortForwardingRulesCommand(portForwardingRules);
            Answer answer = _agentMgr.easySend(externalFirewallId, cmd);
            if (answer == null || !answer.getResult()) {
                String details = (answer != null) ? answer.getDetails() : "details unavailable";
                String msg = "External firewall was unable to apply port forwarding rules to the SRX appliance in zone " + zone.getName() + " due to: " + details + ".";
                s_logger.error(msg);
                throw new ResourceUnavailableException(msg, DataCenter.class, zone.getId());
            }
        }
    }

    public boolean applyIps(Network network, List<? extends PublicIpAddress> ipAddresses) throws ResourceUnavailableException {
        return true;
    }

    public boolean manageRemoteAccessVpn(boolean create, Network network, RemoteAccessVpn vpn) throws ResourceUnavailableException {
        ExternalFirewallDeviceVO fwDeviceVO = getExternalFirewallForNetwork(network);
        HostVO externalFirewall = _hostDao.findById(fwDeviceVO.getHostId());

        if (externalFirewall == null) {
            return false;
        }
        
        // Create/delete VPN
        IpAddress ip = _networkModel.getIp(vpn.getServerAddressId());
        
        // Mask the IP range with the network's VLAN tag
        String[] ipRange = vpn.getIpRange().split("-");
        DataCenterVO zone = _dcDao.findById(network.getDataCenterId());
        int vlanTag = Integer.parseInt(network.getBroadcastUri().getHost());
        int offset = getVlanOffset(network.getPhysicalNetworkId(), vlanTag);
        int cidrSize = getGloballyConfiguredCidrSize();
        
        for (int i = 0; i < 2; i++) {
            ipRange[i] = NetUtils.long2Ip((NetUtils.ip2Long(ipRange[i]) & 0xff000000) | (offset << (32 - cidrSize)));
        }
        
        String maskedIpRange = ipRange[0] + "-" + ipRange[1];
        
        RemoteAccessVpnCfgCommand createVpnCmd = new RemoteAccessVpnCfgCommand(create, ip.getAddress().addr(), vpn.getLocalIp(), maskedIpRange, vpn.getIpsecPresharedKey());
        createVpnCmd.setAccessDetail(NetworkElementCommand.ACCOUNT_ID, String.valueOf(network.getAccountId()));
        createVpnCmd.setAccessDetail(NetworkElementCommand.GUEST_NETWORK_CIDR, network.getCidr()); 
        Answer answer = _agentMgr.easySend(externalFirewall.getId(), createVpnCmd);
        if (answer == null || !answer.getResult()) {
             String details = (answer != null) ? answer.getDetails() : "details unavailable";
             String msg = "External firewall was unable to create a remote access VPN in zone " + zone.getName() + " due to: " + details + ".";
             s_logger.error(msg);
             throw new ResourceUnavailableException(msg, DataCenter.class, zone.getId());
        }
        
        // Add/delete users
        List<VpnUserVO> vpnUsers = _vpnUsersDao.listByAccount(vpn.getAccountId());
        return manageRemoteAccessVpnUsers(network, vpn, vpnUsers);
    }  
    
    public boolean manageRemoteAccessVpnUsers(Network network, RemoteAccessVpn vpn, List<? extends VpnUser> vpnUsers) throws ResourceUnavailableException {
        ExternalFirewallDeviceVO fwDeviceVO = getExternalFirewallForNetwork(network);
        HostVO externalFirewall = _hostDao.findById(fwDeviceVO.getHostId());

        if (externalFirewall == null) {
            return false;
        }
        
        List<VpnUser> addUsers = new ArrayList<VpnUser>();
        List<VpnUser> removeUsers = new ArrayList<VpnUser>();
        for (VpnUser user : vpnUsers) {
            if (user.getState() == VpnUser.State.Add ||
                user.getState() == VpnUser.State.Active) {
                addUsers.add(user);
            } else if (user.getState() == VpnUser.State.Revoke) {
                removeUsers.add(user);
            }
        }
        
        VpnUsersCfgCommand addUsersCmd = new VpnUsersCfgCommand(addUsers, removeUsers);
        addUsersCmd.setAccessDetail(NetworkElementCommand.ACCOUNT_ID, String.valueOf(network.getAccountId()));
        addUsersCmd.setAccessDetail(NetworkElementCommand.GUEST_NETWORK_CIDR, network.getCidr());        
        
        Answer answer = _agentMgr.easySend(externalFirewall.getId(), addUsersCmd);
        if (answer == null || !answer.getResult()) {
             String details = (answer != null) ? answer.getDetails() : "details unavailable";
             DataCenterVO zone = _dcDao.findById(network.getDataCenterId());
             String msg = "External firewall was unable to add remote access users in zone " + zone.getName() + " due to: " + details + ".";
             s_logger.error(msg);
             throw new ResourceUnavailableException(msg, DataCenter.class, zone.getId());
        }
        
        return true;
    }

    public int getVlanOffset(long physicalNetworkId, int vlanTag) {
        PhysicalNetworkVO pNetwork = _physicalNetworkDao.findById(physicalNetworkId);
        if (pNetwork == null) {
            throw new CloudRuntimeException("Could not find the physical Network " + physicalNetworkId + ".");
        }

        if (pNetwork.getVnet() == null) {
            throw new CloudRuntimeException("Could not find vlan range for physical Network " + physicalNetworkId + ".");
        }
        Integer lowestVlanTag = null;
        List<Pair<Integer, Integer>> vnetList = pNetwork.getVnet();
        //finding the vlanrange in which the vlanTag lies.
        for (Pair <Integer,Integer> vnet : vnetList){
            if (vlanTag >= vnet.first() && vlanTag <= vnet.second()){
                lowestVlanTag = vnet.first();
            }
        }
        if (lowestVlanTag == null) {
            throw new InvalidParameterValueException ("The vlan tag does not belong to any of the existing vlan ranges");
        }
        return vlanTag - lowestVlanTag;
    }
    
    public int getGloballyConfiguredCidrSize() {
        try {
            String globalVlanBits = _configDao.getValue(Config.GuestVlanBits.key());
            return 8 + Integer.parseInt(globalVlanBits);
        } catch (Exception e) {
            throw new CloudRuntimeException("Failed to read the globally configured VLAN bits size.");
        }
    }

    @Override
    public HostVO createHostVOForConnectedAgent(HostVO host, StartupCommand[] cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HostVO createHostVOForDirectConnectAgent(HostVO host, StartupCommand[] startup, ServerResource resource,
            Map<String, String> details, List<String> hostTags) {
        if (!(startup[0] instanceof StartupExternalFirewallCommand)) {
            return null;
        }
        host.setType(Host.Type.ExternalFirewall);
        return host;
    }

    @Override
    public DeleteHostAnswer deleteHost(HostVO host, boolean isForced, boolean isForceDeleteStorage) throws UnableDeleteHostException {
        if (host.getType() != org.apache.host.Host.Type.ExternalFirewall) {
        return null;
    }
        return new DeleteHostAnswer(true);
    }
    
    @Override
    public boolean applyPortForwardingRules(Network network, List<? extends PortForwardingRule> rules) throws ResourceUnavailableException {
        // Find the external firewall in this zone
        long zoneId = network.getDataCenterId();
        DataCenterVO zone = _dcDao.findById(zoneId);
        ExternalFirewallDeviceVO fwDeviceVO = getExternalFirewallForNetwork(network);
        HostVO externalFirewall = _hostDao.findById(fwDeviceVO.getHostId());

        assert(externalFirewall != null);

        if (network.getState() == Network.State.Allocated) {
            s_logger.debug("External firewall was asked to apply firewall rules for network with ID " + network.getId() + "; this network is not implemented. Skipping backend commands.");
            return true;
        }

        List<PortForwardingRuleTO> pfRules = new ArrayList<PortForwardingRuleTO>();

        for (PortForwardingRule rule : rules) {
            IpAddress sourceIp = _networkModel.getIp(rule.getSourceIpAddressId());
            Vlan vlan = _vlanDao.findById(sourceIp.getVlanId());

            PortForwardingRuleTO ruleTO = new PortForwardingRuleTO(rule, vlan.getVlanTag(), sourceIp.getAddress().addr());
            pfRules.add(ruleTO);
        }
        
        sendPortForwardingRules(pfRules, zone, externalFirewall.getId());
        return true;
    }
}
