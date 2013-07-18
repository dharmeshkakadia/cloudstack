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
package org.apache.cloudstack.engine.subsystem.api.storage;

import java.util.List;

import org.apache.deploy.DeploymentPlan;
import org.apache.deploy.DeploymentPlanner.ExcludeList;
import org.apache.storage.StoragePool;
import org.apache.utils.component.Adapter;
import org.apache.vm.DiskProfile;
import org.apache.vm.VirtualMachine;
import org.apache.vm.VirtualMachineProfile;

/**
 */
public interface StoragePoolAllocator extends Adapter {
    /**
     * Determines which storage pools are suitable for the guest virtual machine
     * 
     * @param DiskProfile
     *            dskCh
     * @param VirtualMachineProfile
     *            vmProfile
     * @param DeploymentPlan
     *            plan
     * @param ExcludeList
     *            avoid
     * @param int returnUpTo (use -1 to return all possible pools)
     * @return List<StoragePool> List of storage pools that are suitable for the
     *         VM
     **/
    List<StoragePool> allocateToPool(DiskProfile dskCh, VirtualMachineProfile<? extends VirtualMachine> vmProfile,
            DeploymentPlan plan, ExcludeList avoid, int returnUpTo);

    static int RETURN_UPTO_ALL = -1;
}
