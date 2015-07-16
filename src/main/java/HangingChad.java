/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.Closeable;
import java.io.IOException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class HangingChad
    extends LeaderSelectorListenerAdapter implements Closeable {

    final CuratorFramework client;

    HangingChad(String zk) {
        client = CuratorFrameworkFactory.builder()
            .connectString(zk)
            .connectionTimeoutMs(30000)
            .retryPolicy(new ExponentialBackoffRetry(1000, 3))
            .sessionTimeoutMs(30000).build();
        client.start();
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    @Override
    public void takeLeadership(CuratorFramework client) throws Exception {
        try {
            while (true) {
                System.out.println("I am leader");
                Thread.sleep(10);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            System.out.println("Ooops, I'm no longer the leader");
        }
    }

    public void run() throws Exception {
        LeaderSelector leaderSelector
            = new LeaderSelector(client, "/hangingChad", this);
        try {
            leaderSelector.autoRequeue();
            leaderSelector.start();
            System.out.println("Leader selector started");
            while (true) {
                Thread.sleep(1000);
            }
        } finally {
            leaderSelector.close();
        }
    }

    public static void main(String[] args) throws Exception {
        CmdLine cmd = new CmdLine();
        JCommander cmdr = new JCommander(cmd, args);

        if (cmd.help) {
            cmdr.usage();
            System.exit(1);
        }
        new HangingChad(cmd.zk).run();
    }

    static class CmdLine {
        @Parameter(names = "-zk", description = "ZooKeeper server list")
        String zk = "localhost:2181";

        @Parameter(names = "-help",
                   description = "This help message", help = true)
        boolean help;
    }
}
