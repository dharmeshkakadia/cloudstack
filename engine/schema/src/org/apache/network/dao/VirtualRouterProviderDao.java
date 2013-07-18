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
package org.apache.network.dao;

import java.util.List;

import org.apache.network.VirtualRouterProvider;
import org.apache.network.VirtualRouterProvider.VirtualRouterProviderType;
import org.apache.network.element.VirtualRouterProviderVO;
import org.apache.utils.db.GenericDao;

public interface VirtualRouterProviderDao extends GenericDao<VirtualRouterProviderVO, Long> {
    public VirtualRouterProviderVO findByNspIdAndType(long nspId, VirtualRouterProviderType type);
    public List<VirtualRouterProviderVO> listByEnabledAndType(boolean enabled, VirtualRouterProviderType type);
    public VirtualRouterProviderVO findByIdAndEnabledAndType(long id, boolean enabled, VirtualRouterProviderType type);
    public List<VirtualRouterProviderVO> listByType(VirtualRouterProviderType type);
}