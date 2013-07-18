/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cloudstack.storage.datastore;

import edu.emory.mathcs.backport.java.util.Collections;

import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.Scope;
import org.apache.cloudstack.engine.subsystem.api.storage.ZoneScope;
import org.apache.cloudstack.storage.image.datastore.ImageStoreProviderManager;
import org.apache.storage.DataStoreRole;
import org.apache.utils.exception.CloudRuntimeException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import java.util.List;

@Component
public class DataStoreManagerImpl implements DataStoreManager {
    @Inject
    PrimaryDataStoreProviderManager primaryStoreMgr;
    @Inject
    ImageStoreProviderManager imageDataStoreMgr;

    @Override
    public DataStore getDataStore(long storeId, DataStoreRole role) {
        try {
            if (role == DataStoreRole.Primary) {
                return primaryStoreMgr.getPrimaryDataStore(storeId);
            } else if (role == DataStoreRole.Image) {
                return imageDataStoreMgr.getImageStore(storeId);
            } else if (role == DataStoreRole.ImageCache) {
                return imageDataStoreMgr.getImageStore(storeId);
            }
        } catch (CloudRuntimeException e) {
            throw e;
        }
        throw new CloudRuntimeException("un recognized type" + role);
    }

    @Override
    public DataStore getDataStore(String uuid, DataStoreRole role) {
        if (role == DataStoreRole.Primary) {
            return primaryStoreMgr.getPrimaryDataStore(uuid);
        } else if (role == DataStoreRole.Image) {
            return imageDataStoreMgr.getImageStore(uuid);
        }
        throw new CloudRuntimeException("un recognized type" + role);
    }

    @Override
    public List<DataStore> getImageStoresByScope(ZoneScope scope) {
        return imageDataStoreMgr.listImageStoresByScope(scope);
    }

    @Override
    public DataStore getImageStore(long zoneId) {
        List<DataStore> stores = getImageStoresByScope(new ZoneScope(zoneId));
        if (stores == null || stores.size() == 0) {
            return null;
        }
        Collections.shuffle(stores);
        return stores.get(0);
    }

    @Override
    public DataStore getPrimaryDataStore(long storeId) {
        return primaryStoreMgr.getPrimaryDataStore(storeId);
    }

    @Override
    public List<DataStore> getImageCacheStores(Scope scope) {
        return imageDataStoreMgr.listImageCacheStores(scope);
    }

    @Override
    public List<DataStore> listImageStores() {
        return imageDataStoreMgr.listImageStores();
    }

    public void setPrimaryStoreMgr(PrimaryDataStoreProviderManager primaryStoreMgr) {
        this.primaryStoreMgr = primaryStoreMgr;
    }

    public void setImageDataStoreMgr(ImageStoreProviderManager imageDataStoreMgr) {
        this.imageDataStoreMgr = imageDataStoreMgr;
    }
}
