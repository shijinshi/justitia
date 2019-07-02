package cn.shijinshi.fabricmanager.service.fabric.tools;

import org.hyperledger.fabric.protos.common.Configtx;

import java.util.HashMap;
import java.util.Map;

public class ComputeUpdate {

    public Configtx.ConfigUpdate Compute(Configtx.Config original, Configtx.Config updated,String channelId) throws FabricToolsException {
        if (original.getChannelGroup() == null) {
            throw new FabricToolsException("no channel group included for original config");
        }

        if (updated.getChannelGroup() == null) {
            throw new FabricToolsException("no channel group included for updated config");
        }

        ComputeGroupResult computeGroupResult = computeGroupUpdate(original.getChannelGroup(), updated.getChannelGroup());

        if (!computeGroupResult.isUpdatedGroup()) {
            throw new FabricToolsException("no differences detected between original and updated config");
        }
        return Configtx.ConfigUpdate.newBuilder()
                .setChannelId(channelId)
                .setReadSet(computeGroupResult.getReadSet())
                .setWriteSet(computeGroupResult.getWriteSet())
                .build();
    }


    private class ComputeGroupResult {
        private Configtx.ConfigGroup readSet;
        private Configtx.ConfigGroup writeSet;
        private boolean updatedGroup;

        ComputeGroupResult(Configtx.ConfigGroup readSet, Configtx.ConfigGroup writeSet, boolean updatedGroup) {
            this.readSet = readSet;
            this.writeSet = writeSet;
            this.updatedGroup = updatedGroup;
        }

        Configtx.ConfigGroup getReadSet() {
            return readSet;
        }

        Configtx.ConfigGroup getWriteSet() {
            return writeSet;
        }

        boolean isUpdatedGroup() {
            return updatedGroup;
        }
    }
    private ComputeGroupResult computeGroupUpdate(Configtx.ConfigGroup original, Configtx.ConfigGroup updated) {
//        readSet, writeSet *cb.ConfigGroup, updatedGroup bool
        ComputePoliciesMapResult computePoliciesMapResult = computePoliciesMapUpdate(original.getPoliciesMap(), updated.getPoliciesMap());
        Map<String, Configtx.ConfigPolicy> readSetPolicies = computePoliciesMapResult.getReadSet();
        Map<String, Configtx.ConfigPolicy> writeSetPolicies = computePoliciesMapResult.getWriteSet();
        Map<String, Configtx.ConfigPolicy> sameSetPolicies = computePoliciesMapResult.getSameSet();
        boolean policiesMembersUpdated = computePoliciesMapResult.isUpdatedMembers();

        ComputeValuesMapResult computeValuesMapResult = computeValuesMapUpdate(original.getValuesMap(), updated.getValuesMap());
        Map<String, Configtx.ConfigValue> readSetValues = computeValuesMapResult.getReadSet();
        Map<String, Configtx.ConfigValue> writeSetValues = computeValuesMapResult.getWriteSet();
        Map<String, Configtx.ConfigValue> sameSetValues = computeValuesMapResult.getSameSet();
        boolean valuesMembersUpdated = computeValuesMapResult.isUpdatedMembers();

        ComputeGroupsMapResult computeGroupsMapResult = computeGroupsMapUpdate(original.getGroupsMap(), updated.getGroupsMap());
        Map<String, Configtx.ConfigGroup> readSetGroups = computeGroupsMapResult.getReadSet();
        Map<String, Configtx.ConfigGroup> writeSetGroups = computeGroupsMapResult.getWriteSet();
        Map<String, Configtx.ConfigGroup> sameSetGroups = computeGroupsMapResult.getSameSet();
        boolean groupsMembersUpdated = computeGroupsMapResult.isUpdatedMembers();

        // If the updated group is 'Equal' to the updated group (none of the members nor the mod policy changed)
        if (!(policiesMembersUpdated || valuesMembersUpdated || groupsMembersUpdated || !original.getModPolicy().equals(updated.getModPolicy()))) {

            // If there were no modified entries in any of the policies/values/groups maps
            if (readSetPolicies.size() == 0 && writeSetPolicies.size() == 0 && readSetValues.size() == 0 &&
                    writeSetValues.size() == 0 && readSetGroups.size() == 0 && writeSetGroups.size() == 0 ){
                Configtx.ConfigGroup readSet = Configtx.ConfigGroup.newBuilder().setVersion(original.getVersion()).build();
                Configtx.ConfigGroup writeSet = Configtx.ConfigGroup.newBuilder().setVersion(original.getVersion()).build();
                return new ComputeGroupResult(readSet,writeSet,false);
            }

            Configtx.ConfigGroup readSet = Configtx.ConfigGroup.newBuilder()
                    .setVersion(original.getVersion())
                    .putAllPolicies(readSetPolicies)
                    .putAllValues(readSetValues)
                    .putAllGroups(readSetGroups)
                    .build();
            Configtx.ConfigGroup writeSet = Configtx.ConfigGroup.newBuilder()
                    .setVersion(original.getVersion())
                    .putAllPolicies(writeSetPolicies)
                    .putAllValues(writeSetValues)
                    .putAllGroups(writeSetGroups)
                    .build();
            return new ComputeGroupResult(readSet, writeSet ,true);
        }

        for (Map.Entry<String, Configtx.ConfigPolicy> policyEntry : sameSetPolicies.entrySet()) {
            String k = policyEntry.getKey();
            Configtx.ConfigPolicy samePolicy = policyEntry.getValue();
            readSetPolicies.put(k, samePolicy);
            writeSetPolicies.put(k, samePolicy);
        }

        for (Map.Entry<String, Configtx.ConfigValue> valueEntry : sameSetValues.entrySet()) {
            String k = valueEntry.getKey();
            Configtx.ConfigValue sameValue = valueEntry.getValue();
            readSetValues.put(k, sameValue);
            writeSetValues.put(k, sameValue);
        }

        for (Map.Entry<String, Configtx.ConfigGroup> groupEntry :sameSetGroups.entrySet()) {
            String k = groupEntry.getKey();
            Configtx.ConfigGroup sameGroup = groupEntry.getValue();
            readSetGroups.put(k, sameGroup);
            writeSetGroups.put(k, sameGroup);
        }

        Configtx.ConfigGroup readSet = Configtx.ConfigGroup.newBuilder()
                .setVersion(original.getVersion())
                .putAllPolicies(readSetPolicies)
                .putAllValues(readSetValues)
                .putAllGroups(readSetGroups)
                .build();
        Configtx.ConfigGroup writeSet = Configtx.ConfigGroup.newBuilder()
                .setVersion(original.getVersion() + 1)
                .putAllPolicies(writeSetPolicies)
                .putAllValues(writeSetValues)
                .putAllGroups(writeSetGroups)
                .setModPolicy(updated.getModPolicy())
                .build();
        return new ComputeGroupResult(readSet, writeSet ,true);
    }

    private class ComputePoliciesMapResult {
        private Map<String, Configtx.ConfigPolicy> readSet;
        private Map<String, Configtx.ConfigPolicy> writeSet;
        private Map<String, Configtx.ConfigPolicy> sameSet;
        private boolean updatedMembers;

        ComputePoliciesMapResult(Map<String, Configtx.ConfigPolicy> readSet, Map<String, Configtx.ConfigPolicy> writeSet,
                                        Map<String, Configtx.ConfigPolicy> sameSet, boolean updatedMembers) {
            this.readSet = readSet;
            this.writeSet = writeSet;
            this.sameSet = sameSet;
            this.updatedMembers = updatedMembers;
        }

        Map<String, Configtx.ConfigPolicy> getReadSet() {
            return readSet;
        }

        Map<String, Configtx.ConfigPolicy> getWriteSet() {
            return writeSet;
        }

        Map<String, Configtx.ConfigPolicy> getSameSet() {
            return sameSet;
        }

        boolean isUpdatedMembers() {
            return updatedMembers;
        }
    }
    private ComputePoliciesMapResult computePoliciesMapUpdate(Map<String, Configtx.ConfigPolicy> original, Map<String, Configtx.ConfigPolicy> updated){
        boolean updatedMembers = false;
        Map<String, Configtx.ConfigPolicy> readSet = new HashMap<>();
        Map<String, Configtx.ConfigPolicy> writeSet = new HashMap<>();

        // All modified config goes into the read/write sets, but in case the map membership changes, we retain the
        // config which was the same to add to the read/write sets
        Map<String, Configtx.ConfigPolicy> sameSet = new HashMap<>();


        for (Map.Entry<String, Configtx.ConfigPolicy> policyEntry : original.entrySet()) {
            String policyName = policyEntry.getKey();
            Configtx.ConfigPolicy originalPolicy = policyEntry.getValue();
            if (!updated.containsKey(policyName)) {
                updatedMembers = true;
                continue;
            }
            Configtx.ConfigPolicy updatedPolicy = updated.get(policyName);

            if (originalPolicy.getModPolicy().equals(updatedPolicy.getModPolicy())
                    && originalPolicy.getPolicy().equals(updatedPolicy.getPolicy())) {
                sameSet.put(policyName, Configtx.ConfigPolicy.newBuilder().setVersion(originalPolicy.getVersion()).build());
                continue;
            }

            writeSet.put(policyName, Configtx.ConfigPolicy.newBuilder()
                    .setVersion(originalPolicy.getVersion() + 1)
                    .setModPolicy(updatedPolicy.getModPolicy())
                    .setPolicy(updatedPolicy.getPolicy())
                    .build());
        }

        for (Map.Entry<String, Configtx.ConfigPolicy> policyEntry : updated.entrySet()) {
            String policyName = policyEntry.getKey();
            Configtx.ConfigPolicy updatedPolicy = policyEntry.getValue();
            if (original.containsKey(policyName)) {
                continue;
            }
            updatedMembers = true;

            writeSet.put(policyName, Configtx.ConfigPolicy.newBuilder()
                    .setVersion(0)
                    .setModPolicy(updatedPolicy.getModPolicy())
                    .setPolicy(updatedPolicy.getPolicy())
                    .build());
        }
        return new ComputePoliciesMapResult(readSet, writeSet, sameSet, updatedMembers);
    }

    private class ComputeValuesMapResult {
        private Map<String, Configtx.ConfigValue> readSet;
        private Map<String, Configtx.ConfigValue> writeSet;
        private Map<String, Configtx.ConfigValue> sameSet;
        private boolean updatedMembers;

        ComputeValuesMapResult(Map<String, Configtx.ConfigValue> readSet, Map<String, Configtx.ConfigValue> writeSet,
                                      Map<String, Configtx.ConfigValue> sameSet, boolean updatedMembers) {
            this.readSet = readSet;
            this.writeSet = writeSet;
            this.sameSet = sameSet;
            this.updatedMembers = updatedMembers;
        }

        Map<String, Configtx.ConfigValue> getReadSet() {
            return readSet;
        }

        Map<String, Configtx.ConfigValue> getWriteSet() {
            return writeSet;
        }

        Map<String, Configtx.ConfigValue> getSameSet() {
            return sameSet;
        }

        boolean isUpdatedMembers() {
            return updatedMembers;
        }
    }
    private ComputeValuesMapResult computeValuesMapUpdate(Map<String, Configtx.ConfigValue> original, Map<String, Configtx.ConfigValue>  updated) {
        boolean updatedMembers = false;
        Map<String, Configtx.ConfigValue> readSet = new HashMap<>();
        Map<String, Configtx.ConfigValue> writeSet = new HashMap<>();

        // All modified config goes into the read/write sets, but in case the map membership changes, we retain the
        // config which was the same to add to the read/write sets
        Map<String, Configtx.ConfigValue> sameSet = new HashMap<>();

        for (Map.Entry<String, Configtx.ConfigValue> valueEntry : original.entrySet()) {
            String valueName = valueEntry.getKey();
            Configtx.ConfigValue originalValue = valueEntry.getValue();
            if (!updated.containsKey(valueName)) {
                updatedMembers = true;
                continue;
            }
            Configtx.ConfigValue updatedValue = updated.get(valueName);

            if (originalValue.getModPolicy().equals(updatedValue.getModPolicy()) && originalValue.getValue().equals(updatedValue.getValue())) {
                sameSet.put(valueName, Configtx.ConfigValue.newBuilder().setVersion(originalValue.getVersion()).build());
                continue;
            }

            writeSet.put(valueName, Configtx.ConfigValue.newBuilder()
                    .setVersion(originalValue.getVersion() + 1)
                    .setModPolicy(updatedValue.getModPolicy())
                    .setValue(updatedValue.getValue())
                    .build());

        }

        for (Map.Entry<String, Configtx.ConfigValue> valueEntry : updated.entrySet()) {
            String valueName = valueEntry.getKey();
            Configtx.ConfigValue updatedValue = valueEntry.getValue();
            if (original.containsKey(valueName)) {
                continue;
            }
            updatedMembers = true;
            writeSet.put(valueName, Configtx.ConfigValue.newBuilder()
                    .setVersion(0)
                    .setModPolicy(updatedValue.getModPolicy())
                    .setValue(updatedValue.getValue())
                    .build());
        }
        return new ComputeValuesMapResult(readSet, writeSet, sameSet, updatedMembers);
    }

    private class ComputeGroupsMapResult {
        private Map<String, Configtx.ConfigGroup> readSet;
        private Map<String, Configtx.ConfigGroup> writeSet;
        private Map<String, Configtx.ConfigGroup> sameSet;
        private boolean updatedMembers;

        ComputeGroupsMapResult(Map<String, Configtx.ConfigGroup> readSet, Map<String, Configtx.ConfigGroup> writeSet,
                               Map<String, Configtx.ConfigGroup> sameSet, boolean updatedMembers) {
            this.readSet = readSet;
            this.writeSet = writeSet;
            this.sameSet = sameSet;
            this.updatedMembers = updatedMembers;
        }

        Map<String, Configtx.ConfigGroup> getReadSet() {
            return readSet;
        }

        Map<String, Configtx.ConfigGroup> getWriteSet() {
            return writeSet;
        }

        Map<String, Configtx.ConfigGroup> getSameSet() {
            return sameSet;
        }

        boolean isUpdatedMembers() {
            return updatedMembers;
        }
    }
    private ComputeGroupsMapResult computeGroupsMapUpdate(Map<String, Configtx.ConfigGroup> original, Map<String, Configtx.ConfigGroup> updated) {
//    (readSet, writeSet, sameSet map[string]*cb.ConfigGroup, updatedMembers bool) {
        boolean updatedMembers = false;
        Map<String, Configtx.ConfigGroup> readSet = new HashMap<>();
        Map<String, Configtx.ConfigGroup> writeSet = new HashMap<>();

        // All modified config goes into the read/write sets, but in case the map membership changes, we retain the
        // config which was the same to add to the read/write sets
        Map<String, Configtx.ConfigGroup> sameSet = new HashMap<>();

        for (Map.Entry<String, Configtx.ConfigGroup> groupEntry : original.entrySet()) {
            String groupName = groupEntry.getKey();
            Configtx.ConfigGroup originalGroup = groupEntry.getValue();
            Configtx.ConfigGroup updatedGroup = updated.get(groupName);
            if (!updated.containsKey(groupName)) {
                updatedMembers = true;
                continue;
            }

            ComputeGroupResult computeGroupResult = computeGroupUpdate(originalGroup, updatedGroup);
            Configtx.ConfigGroup groupReadSet = computeGroupResult.getReadSet();
            Configtx.ConfigGroup groupWriteSet = computeGroupResult.getWriteSet();
            boolean groupUpdated = computeGroupResult.isUpdatedGroup();
            if (!groupUpdated) {
                sameSet.put(groupName, groupReadSet);
                continue;
            }
            readSet.put(groupName, groupReadSet);
            writeSet.put(groupName,groupWriteSet );
        }

        for (Map.Entry<String, Configtx.ConfigGroup> groupEntry : updated.entrySet()) {
            String groupName = groupEntry.getKey();
            Configtx.ConfigGroup updatedGroup = groupEntry.getValue();
            if (original.containsKey(groupName)) {
                // If the updatedGroup is in the original set of groups, it was already handled
                continue;
            }

            updatedMembers = true;
            ComputeGroupResult computeGroupResult = computeGroupUpdate(Configtx.ConfigGroup.newBuilder().build(), updatedGroup);
            Configtx.ConfigGroup groupWriteSet = computeGroupResult.getWriteSet();
            writeSet.put(groupName, Configtx.ConfigGroup.newBuilder()
                    .setVersion(0)
                    .setModPolicy(updatedGroup.getModPolicy())
                    .putAllPolicies(groupWriteSet.getPoliciesMap())
                    .putAllValues(groupWriteSet.getValuesMap())
                    .putAllGroups(groupWriteSet.getGroupsMap())
                    .build());
        }

        return new ComputeGroupsMapResult(readSet, writeSet, sameSet, updatedMembers);
    }
}
