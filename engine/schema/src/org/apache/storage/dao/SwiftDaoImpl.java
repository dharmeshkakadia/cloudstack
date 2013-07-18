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
package org.apache.storage.dao;

import java.util.Collections;
import java.util.List;

import javax.ejb.Local;

import org.apache.agent.api.to.SwiftTO;
import org.apache.log4j.Logger;
import org.apache.storage.SwiftVO;
import org.apache.utils.db.GenericDaoBase;
import org.springframework.stereotype.Component;

/**
 * 
 * 
 */
@Component
@Local(value = { SwiftDao.class })
public class SwiftDaoImpl extends GenericDaoBase<SwiftVO, Long> implements SwiftDao {
    public static final Logger s_logger = Logger.getLogger(SwiftDaoImpl.class.getName());

    @Override
    public SwiftTO getSwiftTO(Long swiftId) {
        if (swiftId != null) {
            SwiftVO swift = findById(swiftId);
            if (swift != null) {
                return swift.toSwiftTO();
            }
            return null;
        }

        List<SwiftVO> swiftVOs = listAll();
        if (swiftVOs == null || swiftVOs.size() < 1) {
            return null;
        } else {
            Collections.shuffle(swiftVOs);
            return swiftVOs.get(0).toSwiftTO();
        }
    }
}