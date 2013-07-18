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

package org.apache.vpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Local;
import javax.inject.Inject;
import javax.naming.ConfigurationException;

import org.apache.dc.Vlan;
import org.apache.exception.InsufficientAddressCapacityException;
import org.apache.exception.InvalidParameterValueException;
import org.apache.hypervisor.Hypervisor.HypervisorType;
import org.apache.network.IpAddress;
import org.apache.network.Network;
import org.apache.network.NetworkModel;
import org.apache.network.PhysicalNetwork;
import org.apache.network.PhysicalNetworkSetupInfo;
import org.apache.network.PublicIpAddress;
import org.apache.network.Network.Capability;
import org.apache.network.Network.GuestType;
import org.apache.network.Network.Provider;
import org.apache.network.Network.Service;
import org.apache.network.Networks.IsolationType;
import org.apache.network.Networks.TrafficType;
import org.apache.network.dao.IPAddressVO;
import org.apache.network.dao.NetworkVO;
import org.apache.network.element.NetworkElement;
import org.apache.network.element.UserDataServiceProvider;
import org.apache.offering.NetworkOffering;
import org.apache.offering.NetworkOffering.Detail;
import org.apache.offerings.NetworkOfferingVO;
import org.apache.offerings.dao.NetworkOfferingServiceMapDao;
import org.apache.user.Account;
import org.apache.utils.component.ManagerBase;
import org.apache.vm.Nic;
import org.apache.vm.NicProfile;
import org.apache.vm.VirtualMachine;

@Local(value = {NetworkModel.class})
public class MockNetworkModelImpl extends ManagerBase implements NetworkModel {

    @Inject
    NetworkOfferingServiceMapDao _ntwkOfferingSrvcDao;
    /* (non-Javadoc)
     * @see org.apache.utils.component.Manager#configure(java.lang.String, java.util.Map)
     */
    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.utils.component.Manager#start()
     */
    @Override
    public boolean start() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.utils.component.Manager#stop()
     */
    @Override
    public boolean stop() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.utils.component.Manager#getName()
     */
    @Override
    public String getName() {
        return "MockNetworkModelImpl";
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#listPublicIpsAssignedToGuestNtwk(long, long, java.lang.Boolean)
     */
    @Override
    public List<IPAddressVO> listPublicIpsAssignedToGuestNtwk(long accountId, long associatedNetworkId,
            Boolean sourceNat) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getSystemAccountNetworkOfferings(java.lang.String[])
     */
    @Override
    public List<NetworkOfferingVO> getSystemAccountNetworkOfferings(String... offeringNames) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getNics(long)
     */
    @Override
    public List<? extends Nic> getNics(long vmId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getNextAvailableMacAddressInNetwork(long)
     */
    @Override
    public String getNextAvailableMacAddressInNetwork(long networkConfigurationId)
            throws InsufficientAddressCapacityException {
        // TODO Auto-generated method stub
        return null;
    }

    

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getPublicIpAddress(long)
     */
    @Override
    public PublicIpAddress getPublicIpAddress(long ipAddressId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#listPodVlans(long)
     */
    @Override
    public List<? extends Vlan> listPodVlans(long podId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#listNetworksUsedByVm(long, boolean)
     */
    @Override
    public List<NetworkVO> listNetworksUsedByVm(long vmId, boolean isSystem) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getNicInNetwork(long, long)
     */
    @Override
    public Nic getNicInNetwork(long vmId, long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getNicsForTraffic(long, org.apache.network.Networks.TrafficType)
     */
    @Override
    public List<? extends Nic> getNicsForTraffic(long vmId, TrafficType type) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getDefaultNetworkForVm(long)
     */
    @Override
    public Network getDefaultNetworkForVm(long vmId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getDefaultNic(long)
     */
    @Override
    public Nic getDefaultNic(long vmId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getUserDataUpdateProvider(org.apache.network.Network)
     */
    @Override
    public UserDataServiceProvider getUserDataUpdateProvider(Network network) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#networkIsConfiguredForExternalNetworking(long, long)
     */
    @Override
    public boolean networkIsConfiguredForExternalNetworking(long zoneId, long networkId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getNetworkServiceCapabilities(long, org.apache.network.Network.Service)
     */
    @Override
    public Map<Capability, String> getNetworkServiceCapabilities(long networkId, Service service) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#areServicesSupportedByNetworkOffering(long, org.apache.network.Network.Service[])
     */
    @Override
    public boolean areServicesSupportedByNetworkOffering(long networkOfferingId, Service... services) {
        return (_ntwkOfferingSrvcDao.areServicesSupportedByNetworkOffering(networkOfferingId, services));
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getNetworkWithSecurityGroupEnabled(java.lang.Long)
     */
    @Override
    public NetworkVO getNetworkWithSecurityGroupEnabled(Long zoneId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getIpOfNetworkElementInVirtualNetwork(long, long)
     */
    @Override
    public String getIpOfNetworkElementInVirtualNetwork(long accountId, long dataCenterId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#listNetworksForAccount(long, long, org.apache.network.Network.GuestType)
     */
    @Override
    public List<NetworkVO> listNetworksForAccount(long accountId, long zoneId, GuestType type) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#listAllNetworksInAllZonesByType(org.apache.network.Network.GuestType)
     */
    @Override
    public List<NetworkVO> listAllNetworksInAllZonesByType(GuestType type) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getGlobalGuestDomainSuffix()
     */
    @Override
    public String getGlobalGuestDomainSuffix() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getStartIpAddress(long)
     */
    @Override
    public String getStartIpAddress(long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getIpInNetwork(long, long)
     */
    @Override
    public String getIpInNetwork(long vmId, long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getIpInNetworkIncludingRemoved(long, long)
     */
    @Override
    public String getIpInNetworkIncludingRemoved(long vmId, long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getPodIdForVlan(long)
     */
    @Override
    public Long getPodIdForVlan(long vlanDbId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#listNetworkOfferingsForUpgrade(long)
     */
    @Override
    public List<Long> listNetworkOfferingsForUpgrade(long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#isSecurityGroupSupportedInNetwork(org.apache.network.Network)
     */
    @Override
    public boolean isSecurityGroupSupportedInNetwork(Network network) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#isProviderSupportServiceInNetwork(long, org.apache.network.Network.Service, org.apache.network.Network.Provider)
     */
    @Override
    public boolean isProviderSupportServiceInNetwork(long networkId, Service service, Provider provider) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#isProviderEnabledInPhysicalNetwork(long, java.lang.String)
     */
    @Override
    public boolean isProviderEnabledInPhysicalNetwork(long physicalNetowrkId, String providerName) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getNetworkTag(org.apache.hypervisor.Hypervisor.HypervisorType, org.apache.network.Network)
     */
    @Override
    public String getNetworkTag(HypervisorType hType, Network network) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getElementServices(org.apache.network.Network.Provider)
     */
    @Override
    public List<Service> getElementServices(Provider provider) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#canElementEnableIndividualServices(org.apache.network.Network.Provider)
     */
    @Override
    public boolean canElementEnableIndividualServices(Provider provider) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#areServicesSupportedInNetwork(long, org.apache.network.Network.Service[])
     */
    @Override
    public boolean areServicesSupportedInNetwork(long networkId, Service... services) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#isNetworkSystem(org.apache.network.Network)
     */
    @Override
    public boolean isNetworkSystem(Network network) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getNetworkOfferingServiceCapabilities(org.apache.offering.NetworkOffering, org.apache.network.Network.Service)
     */
    @Override
    public Map<Capability, String> getNetworkOfferingServiceCapabilities(NetworkOffering offering, Service service) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getPhysicalNetworkId(org.apache.network.Network)
     */
    @Override
    public Long getPhysicalNetworkId(Network network) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getAllowSubdomainAccessGlobal()
     */
    @Override
    public boolean getAllowSubdomainAccessGlobal() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#isProviderForNetwork(org.apache.network.Network.Provider, long)
     */
    @Override
    public boolean isProviderForNetwork(Provider provider, long networkId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#isProviderForNetworkOffering(org.apache.network.Network.Provider, long)
     */
    @Override
    public boolean isProviderForNetworkOffering(Provider provider, long networkOfferingId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#canProviderSupportServices(java.util.Map)
     */
    @Override
    public void canProviderSupportServices(Map<Provider, Set<Service>> providersMap) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getPhysicalNetworkInfo(long, org.apache.hypervisor.Hypervisor.HypervisorType)
     */
    @Override
    public List<PhysicalNetworkSetupInfo> getPhysicalNetworkInfo(long dcId, HypervisorType hypervisorType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#canAddDefaultSecurityGroup()
     */
    @Override
    public boolean canAddDefaultSecurityGroup() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#listNetworkOfferingServices(long)
     */
    @Override
    public List<Service> listNetworkOfferingServices(long networkOfferingId) {
        if (networkOfferingId == 2) {
            return new ArrayList<Service>();
        }

        List<Service> services = new ArrayList<Service>();
        services.add(Service.SourceNat);
        return services;

    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#areServicesEnabledInZone(long, org.apache.offering.NetworkOffering, java.util.List)
     */
    @Override
    public boolean areServicesEnabledInZone(long zoneId, NetworkOffering offering, List<Service> services) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#checkIpForService(org.apache.network.IPAddressVO, org.apache.network.Network.Service, java.lang.Long)
     */
    @Override
    public boolean checkIpForService(IpAddress ip, Service service, Long networkId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#checkCapabilityForProvider(java.util.Set, org.apache.network.Network.Service, org.apache.network.Network.Capability, java.lang.String)
     */
    @Override
    public void checkCapabilityForProvider(Set<Provider> providers, Service service, Capability cap, String capValue) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getDefaultUniqueProviderForService(java.lang.String)
     */
    @Override
    public Provider getDefaultUniqueProviderForService(String serviceName) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#checkNetworkPermissions(org.apache.user.Account, org.apache.network.Network)
     */
    @Override
    public void checkNetworkPermissions(Account owner, Network network) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getDefaultManagementTrafficLabel(long, org.apache.hypervisor.Hypervisor.HypervisorType)
     */
    @Override
    public String getDefaultManagementTrafficLabel(long zoneId, HypervisorType hypervisorType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getDefaultStorageTrafficLabel(long, org.apache.hypervisor.Hypervisor.HypervisorType)
     */
    @Override
    public String getDefaultStorageTrafficLabel(long zoneId, HypervisorType hypervisorType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getDefaultPublicTrafficLabel(long, org.apache.hypervisor.Hypervisor.HypervisorType)
     */
    @Override
    public String getDefaultPublicTrafficLabel(long dcId, HypervisorType vmware) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getDefaultGuestTrafficLabel(long, org.apache.hypervisor.Hypervisor.HypervisorType)
     */
    @Override
    public String getDefaultGuestTrafficLabel(long dcId, HypervisorType vmware) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getElementImplementingProvider(java.lang.String)
     */
    @Override
    public NetworkElement getElementImplementingProvider(String providerName) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getAccountNetworkDomain(long, long)
     */
    @Override
    public String getAccountNetworkDomain(long accountId, long zoneId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getDefaultNetworkDomain()
     */
    @Override
    public String getDefaultNetworkDomain(long zoneId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getNtwkOffDistinctProviders(long)
     */
    @Override
    public List<Provider> getNtwkOffDistinctProviders(long ntwkOffId) {
        return new ArrayList<Provider>();
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#listPublicIpsAssignedToAccount(long, long, java.lang.Boolean)
     */
    @Override
    public List<IPAddressVO> listPublicIpsAssignedToAccount(long accountId, long dcId, Boolean sourceNat) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getPhysicalNtwksSupportingTrafficType(long, org.apache.network.Networks.TrafficType)
     */
    @Override
    public List<? extends PhysicalNetwork> getPhysicalNtwksSupportingTrafficType(long zoneId, TrafficType trafficType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#isPrivateGateway(org.apache.vm.Nic)
     */
    @Override
    public boolean isPrivateGateway(Nic guestNic) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getNetworkCapabilities(long)
     */
    @Override
    public Map<Service, Map<Capability, String>> getNetworkCapabilities(long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getSystemNetworkByZoneAndTrafficType(long, org.apache.network.Networks.TrafficType)
     */
    @Override
    public Network getSystemNetworkByZoneAndTrafficType(long zoneId, TrafficType trafficType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getDedicatedNetworkDomain(long)
     */
    @Override
    public Long getDedicatedNetworkDomain(long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getNetworkOfferingServiceProvidersMap(long)
     */
    @Override
    public Map<Service, Set<Provider>> getNetworkOfferingServiceProvidersMap(long networkOfferingId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#listSupportedNetworkServiceProviders(java.lang.String)
     */
    @Override
    public List<? extends Provider> listSupportedNetworkServiceProviders(String serviceName) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#listNetworksByVpc(long)
     */
    @Override
    public List<? extends Network> listNetworksByVpc(long vpcId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#canUseForDeploy(org.apache.network.Network)
     */
    @Override
    public boolean canUseForDeploy(Network network) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getExclusiveGuestNetwork(long)
     */
    @Override
    public Network getExclusiveGuestNetwork(long zoneId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#findPhysicalNetworkId(long, java.lang.String, org.apache.network.Networks.TrafficType)
     */
    @Override
    public long findPhysicalNetworkId(long zoneId, String tag, TrafficType trafficType) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getNetworkRate(long, java.lang.Long)
     */
    @Override
    public Integer getNetworkRate(long networkId, Long vmId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#isVmPartOfNetwork(long, long)
     */
    @Override
    public boolean isVmPartOfNetwork(long vmId, long ntwkId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getDefaultPhysicalNetworkByZoneAndTrafficType(long, org.apache.network.Networks.TrafficType)
     */
    @Override
    public PhysicalNetwork getDefaultPhysicalNetworkByZoneAndTrafficType(long zoneId, TrafficType trafficType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getNetwork(long)
     */
    @Override
    public Network getNetwork(long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getIp(long)
     */
    @Override
    public IpAddress getIp(long sourceIpAddressId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#isNetworkAvailableInDomain(long, long)
     */
    @Override
    public boolean isNetworkAvailableInDomain(long networkId, long domainId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getNicProfile(org.apache.vm.VirtualMachine, long, java.lang.String)
     */
    @Override
    public NicProfile getNicProfile(VirtualMachine vm, long networkId, String broadcastUri) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getAvailableIps(org.apache.network.Network, java.lang.String)
     */
    @Override
    public Set<Long> getAvailableIps(Network network, String requestedIp) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getDomainNetworkDomain(long, long)
     */
    @Override
    public String getDomainNetworkDomain(long domainId, long zoneId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getIpToServices(java.util.List, boolean, boolean)
     */
    @Override
    public Map<PublicIpAddress, Set<Service>> getIpToServices(List<? extends PublicIpAddress> publicIps, boolean rulesRevoked,
            boolean includingFirewall) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getProviderToIpList(org.apache.network.Network, java.util.Map)
     */
    @Override
    public Map<Provider, ArrayList<PublicIpAddress>> getProviderToIpList(Network network,
            Map<PublicIpAddress, Set<Service>> ipToServices) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#getSourceNatIpAddressForGuestNetwork(org.apache.user.Account, org.apache.network.Network)
     */
    @Override
    public PublicIpAddress getSourceNatIpAddressForGuestNetwork(Account owner, Network guestNetwork) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.network.NetworkModel#isNetworkInlineMode(org.apache.network.Network)
     */
    @Override
    public boolean isNetworkInlineMode(Network network) {
        // TODO Auto-generated method stub
        return false;
    }

	@Override
	public boolean isIP6AddressAvailableInNetwork(long networkId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isIP6AddressAvailableInVlan(long vlanId) {
		// TODO Auto-generated method stub
		return false;
	}

        @Override
        public void checkIp6Parameters(String startIPv6, String endIPv6, String ip6Gateway, String ip6Cidr)
                  throws InvalidParameterValueException {
            // TODO Auto-generated method stub
        }

	@Override
	public void checkRequestedIpAddresses(long networkId, String ip4, String ip6)
			throws InvalidParameterValueException {
		// TODO Auto-generated method stub
	}

	@Override
	public String getStartIpv6Address(long id) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public boolean isProviderEnabledInZone(long zoneId, String provider) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Nic getPlaceholderNicForRouter(Network network, Long podId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IpAddress getPublicIpAddress(String ipAddress, long zoneId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getUsedIpsInNetwork(Network network) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Detail, String> getNtwkOffDetails(long offId) {
        return null;
    }
    
    public IsolationType[] listNetworkIsolationMethods() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Nic getNicInNetworkIncludingRemoved(long vmId, long networkId) {
        return null;
    }

    @Override
    public boolean getExecuteInSeqNtwkElmtCmd() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isNetworkReadyForGc(long networkId) {
        return true;
    }
}
