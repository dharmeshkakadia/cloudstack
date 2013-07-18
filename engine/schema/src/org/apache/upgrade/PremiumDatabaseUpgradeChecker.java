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
package org.apache.upgrade;

import javax.ejb.Local;

import org.apache.upgrade.dao.DbUpgrade;
import org.apache.upgrade.dao.Upgrade217to218;
import org.apache.upgrade.dao.Upgrade218to224DomainVlans;
import org.apache.upgrade.dao.Upgrade218to22Premium;
import org.apache.upgrade.dao.Upgrade2210to2211;
import org.apache.upgrade.dao.Upgrade2211to2212Premium;
import org.apache.upgrade.dao.Upgrade2212to2213;
import org.apache.upgrade.dao.Upgrade2213to2214;
import org.apache.upgrade.dao.Upgrade2214to30;
import org.apache.upgrade.dao.Upgrade221to222Premium;
import org.apache.upgrade.dao.Upgrade222to224Premium;
import org.apache.upgrade.dao.Upgrade224to225;
import org.apache.upgrade.dao.Upgrade225to226;
import org.apache.upgrade.dao.Upgrade227to228Premium;
import org.apache.upgrade.dao.Upgrade228to229;
import org.apache.upgrade.dao.Upgrade229to2210;
import org.apache.upgrade.dao.Upgrade301to302;
import org.apache.upgrade.dao.Upgrade302to40;
import org.apache.upgrade.dao.Upgrade30to301;
import org.apache.upgrade.dao.Upgrade40to41;
import org.apache.upgrade.dao.UpgradeSnapshot217to224;
import org.apache.upgrade.dao.UpgradeSnapshot223to224;
import org.apache.utils.component.SystemIntegrityChecker;

@Local(value = { SystemIntegrityChecker.class })
public class PremiumDatabaseUpgradeChecker extends DatabaseUpgradeChecker {
    public PremiumDatabaseUpgradeChecker() {
        _upgradeMap.put("2.1.7", new DbUpgrade[] { new Upgrade217to218(), new Upgrade218to22Premium(),
                new Upgrade221to222Premium(), new UpgradeSnapshot217to224(), new Upgrade222to224Premium(),
                new Upgrade224to225(), new Upgrade225to226(), new Upgrade227to228Premium(), new Upgrade228to229(),
                new Upgrade229to2210(), new Upgrade2210to2211(), new Upgrade2211to2212Premium(),
                new Upgrade2212to2213(), new Upgrade2213to2214(), new Upgrade2214to30(), new Upgrade30to301(),
                new Upgrade301to302(), new Upgrade302to40(), new Upgrade40to41() });

        _upgradeMap.put("2.1.8", new DbUpgrade[] { new Upgrade218to22Premium(), new Upgrade221to222Premium(),
                new UpgradeSnapshot217to224(), new Upgrade222to224Premium(), new Upgrade218to224DomainVlans(),
                new Upgrade224to225(), new Upgrade225to226(), new Upgrade227to228Premium(), new Upgrade228to229(),
                new Upgrade229to2210(), new Upgrade2210to2211(), new Upgrade2211to2212Premium(), new Upgrade2212to2213(),
                new Upgrade2213to2214(), new Upgrade2214to30(), new Upgrade30to301(), new Upgrade301to302(),
                new Upgrade302to40(), new Upgrade40to41() });

        _upgradeMap.put("2.1.9", new DbUpgrade[] { new Upgrade218to22Premium(), new Upgrade221to222Premium(),
                new UpgradeSnapshot217to224(), new Upgrade222to224Premium(), new Upgrade218to224DomainVlans(),
                new Upgrade224to225(), new Upgrade225to226(), new Upgrade227to228Premium(), new Upgrade228to229(),
                new Upgrade229to2210(), new Upgrade2210to2211(), new Upgrade2211to2212Premium(), new Upgrade2212to2213(),
                new Upgrade2213to2214(), new Upgrade2214to30(), new Upgrade30to301(), new Upgrade301to302(),
                new Upgrade302to40(), new Upgrade40to41() });

        _upgradeMap.put("2.2.1", new DbUpgrade[] { new Upgrade221to222Premium(), new Upgrade222to224Premium(),
                new UpgradeSnapshot223to224(), new Upgrade224to225(), new Upgrade225to226(), new Upgrade227to228Premium(),
                new Upgrade228to229(), new Upgrade229to2210(), new Upgrade2210to2211(), new Upgrade2211to2212Premium(),
                new Upgrade2212to2213(), new Upgrade2213to2214(), new Upgrade2214to30(), new Upgrade30to301(),
                new Upgrade301to302(), new Upgrade302to40(), new Upgrade40to41() });

        _upgradeMap.put("2.2.2", new DbUpgrade[] { new Upgrade222to224Premium(), new UpgradeSnapshot223to224(),
                new Upgrade224to225(), new Upgrade225to226(), new Upgrade227to228Premium(), new Upgrade228to229(),
                new Upgrade229to2210(), new Upgrade2210to2211(), new Upgrade2211to2212Premium(), new Upgrade2212to2213(),
                new Upgrade2213to2214(), new Upgrade2214to30(), new Upgrade30to301(), new Upgrade301to302(),
                new Upgrade302to40(), new Upgrade40to41() });

        _upgradeMap.put("2.2.3", new DbUpgrade[] { new Upgrade222to224Premium(), new UpgradeSnapshot223to224(),
                new Upgrade224to225(), new Upgrade225to226(), new Upgrade227to228Premium(), new Upgrade228to229(),
                new Upgrade229to2210(), new Upgrade2210to2211(), new Upgrade2211to2212Premium(), new Upgrade2212to2213(),
                new Upgrade2213to2214(), new Upgrade2214to30(), new Upgrade30to301(), new Upgrade301to302(),
                new Upgrade302to40(), new Upgrade40to41() });

        _upgradeMap.put("2.2.4", new DbUpgrade[] { new Upgrade224to225(), new Upgrade225to226(), new Upgrade227to228Premium(),
                new Upgrade228to229(), new Upgrade229to2210(), new Upgrade2210to2211(), new Upgrade2211to2212Premium(),
                new Upgrade2212to2213(), new Upgrade2213to2214(), new Upgrade2214to30(), new Upgrade30to301(),
                new Upgrade301to302(), new Upgrade302to40(), new Upgrade40to41() });

        _upgradeMap.put("2.2.5", new DbUpgrade[] { new Upgrade225to226(), new Upgrade227to228Premium(), new Upgrade228to229(),
                new Upgrade229to2210(), new Upgrade2210to2211(), new Upgrade2211to2212Premium(),
                new Upgrade2212to2213(), new Upgrade2213to2214(), new Upgrade2214to30(), new Upgrade30to301(),
                new Upgrade301to302(), new Upgrade302to40(), new Upgrade40to41() });

        _upgradeMap.put("2.2.6", new DbUpgrade[] { new Upgrade227to228Premium(), new Upgrade228to229(), new Upgrade229to2210(),
                new Upgrade2210to2211(), new Upgrade2211to2212Premium(), new Upgrade2212to2213(),
                new Upgrade2213to2214(), new Upgrade2214to30(), new Upgrade30to301(), new Upgrade301to302(),
                new Upgrade302to40(), new Upgrade40to41() });

        _upgradeMap.put("2.2.7", new DbUpgrade[] { new Upgrade227to228Premium(), new Upgrade228to229(), new Upgrade229to2210(),
                new Upgrade2210to2211(), new Upgrade2211to2212Premium(), new Upgrade2212to2213(),
                new Upgrade2213to2214(), new Upgrade2214to30(), new Upgrade30to301(), new Upgrade301to302(),
                new Upgrade302to40(), new Upgrade40to41() });

        _upgradeMap.put("2.2.8", new DbUpgrade[] { new Upgrade228to229(), new Upgrade229to2210(), new Upgrade2210to2211(),
                new Upgrade2211to2212Premium(), new Upgrade2212to2213(), new Upgrade2213to2214(),
                new Upgrade2214to30(), new Upgrade30to301(), new Upgrade301to302(), new Upgrade302to40(), new Upgrade40to41() });

        _upgradeMap.put("2.2.9", new DbUpgrade[] { new Upgrade229to2210(), new Upgrade2210to2211(),
                new Upgrade2211to2212Premium(), new Upgrade2212to2213(), new Upgrade2213to2214(), new Upgrade2214to30(),
                new Upgrade30to301(), new Upgrade301to302(), new Upgrade302to40(), new Upgrade40to41() });

        _upgradeMap.put("2.2.10", new DbUpgrade[] { new Upgrade2210to2211(), new Upgrade2211to2212Premium(),
                new Upgrade2212to2213(), new Upgrade2213to2214(), new Upgrade2214to30(), new Upgrade30to301(),
                new Upgrade301to302(), new Upgrade302to40(), new Upgrade40to41() });

        _upgradeMap.put("2.2.11", new DbUpgrade[] { new Upgrade2211to2212Premium(), new Upgrade2212to2213(),
                new Upgrade2213to2214(), new Upgrade2214to30(), new Upgrade30to301(), new Upgrade301to302(),
                new Upgrade302to40(), new Upgrade40to41() });

        _upgradeMap.put("2.2.12", new DbUpgrade[] { new Upgrade2212to2213(), new Upgrade2213to2214(), new Upgrade2214to30(),
                new Upgrade30to301(), new Upgrade301to302(), new Upgrade302to40(), new Upgrade40to41() });

        _upgradeMap.put("2.2.13", new DbUpgrade[] { new Upgrade2213to2214(), new Upgrade2214to30(), new Upgrade30to301(),
                new Upgrade301to302(), new Upgrade302to40(), new Upgrade40to41() });

        _upgradeMap.put("2.2.14", new DbUpgrade[] { new Upgrade2214to30(), new Upgrade30to301(), new Upgrade301to302(),
                new Upgrade302to40(), new Upgrade40to41() });

        _upgradeMap.put("3.0.0", new DbUpgrade[] { new Upgrade30to301(), new Upgrade301to302(), new Upgrade302to40(), new Upgrade40to41() });

        _upgradeMap.put("3.0.1", new DbUpgrade[] { new Upgrade301to302(), new Upgrade302to40(), new Upgrade40to41()  });

        _upgradeMap.put("3.0.2", new DbUpgrade[] { new Upgrade302to40(), new Upgrade40to41() });

        _upgradeMap.put("4.0.0", new DbUpgrade[] { new Upgrade40to41() });

        _upgradeMap.put("4.0.1", new DbUpgrade[] { new Upgrade40to41() });

        _upgradeMap.put("4.0.2", new DbUpgrade[] { new Upgrade40to41() });    }
}