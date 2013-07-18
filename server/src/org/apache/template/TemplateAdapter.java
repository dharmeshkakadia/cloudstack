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
package org.apache.template;

import java.util.Map;

import org.apache.cloudstack.api.command.user.iso.DeleteIsoCmd;
import org.apache.cloudstack.api.command.user.iso.RegisterIsoCmd;
import org.apache.cloudstack.api.command.user.template.DeleteTemplateCmd;
import org.apache.cloudstack.api.command.user.template.RegisterTemplateCmd;
import org.apache.cloudstack.api.command.user.template.ExtractTemplateCmd;
import org.apache.exception.ResourceAllocationException;
import org.apache.hypervisor.Hypervisor.HypervisorType;
import org.apache.storage.TemplateProfile;
import org.apache.storage.VMTemplateVO;
import org.apache.storage.Storage.TemplateType;
import org.apache.user.Account;
import org.apache.utils.component.Adapter;

public interface TemplateAdapter extends Adapter {
	public static class TemplateAdapterType {
		String _name;

		public static final TemplateAdapterType Hypervisor = new TemplateAdapterType("HypervisorAdapter");
		public static final TemplateAdapterType BareMetal = new TemplateAdapterType("BareMetalAdapter");

		public TemplateAdapterType(String name) {
			_name = name;
		}

		public String getName() {
			return _name;
		}
	}

	public TemplateProfile prepare(RegisterTemplateCmd cmd) throws ResourceAllocationException;

	public TemplateProfile prepare(RegisterIsoCmd cmd) throws ResourceAllocationException;

	public VMTemplateVO create(TemplateProfile profile);

	public TemplateProfile prepareDelete(DeleteTemplateCmd cmd);

	public TemplateProfile prepareDelete(DeleteIsoCmd cmd);

	public TemplateProfile prepareExtractTemplate(ExtractTemplateCmd cmd);

	public boolean delete(TemplateProfile profile);

	public TemplateProfile prepare(boolean isIso, Long userId, String name, String displayText, Integer bits,
            Boolean passwordEnabled, Boolean requiresHVM, String url, Boolean isPublic, Boolean featured,
            Boolean isExtractable, String format, Long guestOSId, Long zoneId, HypervisorType hypervisorType,
            String accountName, Long domainId, String chksum, Boolean bootable, Map details) throws ResourceAllocationException;

    public TemplateProfile prepare(boolean isIso, long userId, String name, String displayText, Integer bits,
            Boolean passwordEnabled, Boolean requiresHVM, String url, Boolean isPublic, Boolean featured,
            Boolean isExtractable, String format, Long guestOSId, Long zoneId, HypervisorType hypervisorType,
            String chksum, Boolean bootable, String templateTag, Account templateOwner, Map details, Boolean sshKeyEnabled,
            String imageStoreUuid, Boolean isDynamicallyScalable, TemplateType templateType) throws ResourceAllocationException;

}
