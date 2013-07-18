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
package org.apache.hypervisor;

import javax.ejb.Local;
import javax.inject.Inject;

import org.apache.agent.api.to.VirtualMachineTO;
import org.apache.hypervisor.HypervisorGuru;
import org.apache.hypervisor.Hypervisor.HypervisorType;
import org.apache.storage.GuestOSVO;
import org.apache.storage.dao.GuestOSDao;
import org.apache.vm.VirtualMachine;
import org.apache.vm.VirtualMachineProfile;

@Local(value=HypervisorGuru.class)
public class KVMGuru extends HypervisorGuruBase implements HypervisorGuru {
    @Inject GuestOSDao _guestOsDao;
    
	@Override
	public HypervisorType getHypervisorType() {
		return HypervisorType.KVM;
	}
	
	protected KVMGuru() {
		super();
	}

	@Override
	public <T extends VirtualMachine> VirtualMachineTO implement(
			VirtualMachineProfile<T> vm) {
		VirtualMachineTO to = toVirtualMachineTO(vm);

		// Determine the VM's OS description
		GuestOSVO guestOS = _guestOsDao.findById(vm.getVirtualMachine().getGuestOSId());
		to.setOs(guestOS.getDisplayName());

		return to;
	}
	
	@Override
    public boolean trackVmHostChange() {
    	return false;
    }
}
