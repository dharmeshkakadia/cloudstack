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
package org.apache.cloudstack.storage.datastore.driver;

import java.util.UUID;

import javax.inject.Inject;

import org.apache.agent.api.Answer;
import org.apache.agent.api.storage.CreateEntityDownloadURLCommand;
import org.apache.agent.api.to.DataStoreTO;
import org.apache.agent.api.to.NfsTO;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPoint;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPointSelector;
import org.apache.cloudstack.storage.image.datastore.ImageStoreEntity;
import org.apache.cloudstack.storage.image.store.ImageStoreImpl;
import org.apache.log4j.Logger;
import org.apache.storage.Storage.ImageFormat;
import org.apache.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.storage.image.BaseImageStoreDriverImpl;
import org.apache.configuration.Config;
import org.apache.configuration.dao.ConfigurationDao;

public class CloudStackImageStoreDriverImpl extends BaseImageStoreDriverImpl {
    private static final Logger s_logger = Logger.getLogger(CloudStackImageStoreDriverImpl.class);

    @Inject
    ConfigurationDao _configDao;
    @Inject
    EndPointSelector _epSelector;


    @Override
    public DataStoreTO getStoreTO(DataStore store) {
        ImageStoreImpl nfsStore = (ImageStoreImpl) store;
        NfsTO nfsTO = new NfsTO();
        nfsTO.setRole(store.getRole());
        nfsTO.setUrl(nfsStore.getUri());
        return nfsTO;
    }

    @Override
    public String createEntityExtractUrl(DataStore store, String installPath, ImageFormat format) {
        // find an endpoint to send command
        EndPoint ep = _epSelector.select(store);
        // Create Symlink at ssvm
        String path = installPath;
        String uuid = UUID.randomUUID().toString() + "." + format.getFileExtension();
        CreateEntityDownloadURLCommand cmd = new CreateEntityDownloadURLCommand(((ImageStoreEntity) store).getMountPoint(), path, uuid);
        Answer ans = ep.sendMessage(cmd);
        if (ans == null || !ans.getResult()) {
            String errorString = "Unable to create a link for entity at " + installPath + " on ssvm," + ans.getDetails();
            s_logger.error(errorString);
            throw new CloudRuntimeException(errorString);
        }
        // Construct actual URL locally now that the symlink exists at SSVM
        return generateCopyUrl(ep.getPublicAddr(), uuid);
    }

    private String generateCopyUrl(String ipAddress, String uuid){

        String hostname = ipAddress;
        String scheme = "http";
        boolean _sslCopy = false;
        String sslCfg = _configDao.getValue(Config.SecStorageEncryptCopy.toString());
        if ( sslCfg != null ){
            _sslCopy = Boolean.parseBoolean(sslCfg);
        }
        if (_sslCopy) {
            hostname = ipAddress.replace(".", "-");
            hostname = hostname + ".realhostip.com";
            scheme = "https";
        }
        return scheme + "://" + hostname + "/userdata/" + uuid;
    }

}
