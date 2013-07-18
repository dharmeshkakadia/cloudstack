// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.snapshot;

import java.io.IOException;

import org.apache.cloudstack.test.utils.SpringUtils;
import org.apache.cluster.agentlb.dao.HostTransferMapDaoImpl;
import org.apache.dc.dao.ClusterDaoImpl;
import org.apache.dc.dao.HostPodDaoImpl;
import org.apache.host.dao.HostDaoImpl;
import org.apache.host.dao.HostDetailsDaoImpl;
import org.apache.host.dao.HostTagsDaoImpl;
import org.apache.storage.dao.SnapshotDaoImpl;
import org.apache.storage.dao.VolumeDaoImpl;
import org.apache.tags.dao.ResourceTagsDaoImpl;
import org.apache.vm.dao.NicDaoImpl;
import org.apache.vm.dao.VMInstanceDaoImpl;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

@Configuration
@ComponentScan(basePackageClasses={
        SnapshotDaoImpl.class,
        ResourceTagsDaoImpl.class,
        VMInstanceDaoImpl.class,
        VolumeDaoImpl.class,
        NicDaoImpl.class,
        HostDaoImpl.class,
        HostDetailsDaoImpl.class,
        HostTagsDaoImpl.class,
        HostTransferMapDaoImpl.class,
        ClusterDaoImpl.class,
        HostPodDaoImpl.class},
        includeFilters={@Filter(value=SnapshotDaoTestConfiguration.Library.class, type=FilterType.CUSTOM)},
        useDefaultFilters=false
        )
public class SnapshotDaoTestConfiguration {


    public static class Library implements TypeFilter {

        @Override
        public boolean match(MetadataReader mdr, MetadataReaderFactory arg1) throws IOException {
            mdr.getClassMetadata().getClassName();
            ComponentScan cs = SnapshotDaoTestConfiguration.class.getAnnotation(ComponentScan.class);
            return SpringUtils.includedInBasePackageClasses(mdr.getClassMetadata().getClassName(), cs);
        }

    }
}
