# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
from marvin.base import CloudStackEntity
from marvin.cloudstackAPI import createVPCOffering
from marvin.cloudstackAPI import listVPCOfferings
from marvin.cloudstackAPI import updateVPCOffering
from marvin.cloudstackAPI import deleteVPCOffering

class VPCOffering(CloudStackEntity.CloudStackEntity):


    def __init__(self, items):
        self.__dict__.update(items)


    @classmethod
    def create(cls, apiclient, factory, **kwargs):
        cmd = createVPCOffering.createVPCOfferingCmd()
        [setattr(cmd, factoryKey, factoryValue) for factoryKey, factoryValue in factory.__dict__.iteritems()]
        [setattr(cmd, key, value) for key,value in kwargs.iteritems()]
        vpcoffering = apiclient.createVPCOffering(cmd)
        return VPCOffering(vpcoffering.__dict__)


    @classmethod
    def list(self, apiclient, **kwargs):
        cmd = listVPCOfferings.listVPCOfferingsCmd()
        [setattr(cmd, key, value) for key,value in kwargs.iteritems()]
        vpcoffering = apiclient.listVPCOfferings(cmd)
        return map(lambda e: VPCOffering(e.__dict__), vpcoffering)


    def update(self, apiclient, **kwargs):
        cmd = updateVPCOffering.updateVPCOfferingCmd()
        cmd.id = self.id
        [setattr(cmd, key, value) for key,value in kwargs.iteritems()]
        vpcoffering = apiclient.updateVPCOffering(cmd)
        return vpcoffering


    def delete(self, apiclient, **kwargs):
        cmd = deleteVPCOffering.deleteVPCOfferingCmd()
        cmd.id = self.id
        [setattr(cmd, key, value) for key,value in kwargs.iteritems()]
        vpcoffering = apiclient.deleteVPCOffering(cmd)
        return vpcoffering
