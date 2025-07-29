package com.zdd.config;


import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;


import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

public class MeetingIdShardingAlgorithm implements StandardShardingAlgorithm<String> {
    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<String> preciseShardingValue) {
        String meetingId = preciseShardingValue.getValue();
        int hash = Math.abs(meetingId.hashCode());
        int suffix = hash % collection.size() + 1;

        for (String tableName : collection) {
            if (tableName.endsWith("_" + suffix)) {
                return tableName;
            }
        }

        throw new UnsupportedOperationException(
                String.format("No actual table matches suffix %s in %s", suffix, collection));

    }

    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<String> rangeShardingValue) {
        return collection;
    }

    @Override
    public Properties getProps() {
        return new Properties();
    }

    @Override
    public void init(Properties properties) {

    }


//    @Override
//    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
//        String meetingId = shardingValue.getValue();
//        int tableIndex;
//
//        try {
//            // 尝试将meeting_id转换为长整型进行取模运算
//            long numericValue = Long.parseLong(meetingId);
//            tableIndex = (int) (numericValue % 2);
//        } catch (NumberFormatException e) {
//            // 如果meeting_id不是数字格式，使用hashCode取模
//            tableIndex = Math.abs(meetingId.hashCode() % 2);
//        }
//
//        // 将计算结果0或1映射到表名后缀1或2
//        return "meeting_chat_message_" + (tableIndex + 1);
//    }
}
