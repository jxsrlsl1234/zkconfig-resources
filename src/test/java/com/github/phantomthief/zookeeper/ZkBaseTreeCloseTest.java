package com.github.phantomthief.zookeeper;

import static com.github.phantomthief.zookeeper.util.ZkUtils.setToZk;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phantomthief.util.ThrowableFunction;

/**
 * @author w.vela
 * Created on 2017-09-04.
 */
public class ZkBaseTreeCloseTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(ZkBaseTreeCloseTest.class);

    @Test
    public void test() throws InterruptedException {
        boolean[] shutdown = { false };
        ThrowableFunction<Map<String, ChildData>, String, Exception> func = i -> {
            String result = i.keySet().stream().collect(joining(","));
            logger.info("found change:{}", result);
            if (shutdown[0]) {
                fail("shouldn't occurred.");
            }
            return result;
        };
        ZkBasedTreeNodeResource<String> testNode = ZkBasedTreeNodeResource.<String> newBuilder()
                .path("/test") //
                .curator(curatorFramework) //
                .factoryEx(func) //
                .build();
        System.out.println(testNode.get());
        setToZk(curatorFramework, "/test/A", "test2".getBytes());
        SECONDS.sleep(5);
        try {
            testNode.close();
            shutdown[0] = true;
        } catch (IOException e) {
            fail(e.toString());
        }
        setToZk(curatorFramework, "/test/B", "test3".getBytes());
        SECONDS.sleep(10);
        try {
            testNode.get();
            fail();
        } catch (IllegalStateException e) {
            // ignore
        }
    }
}
