/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.fuse.example.support;

import org.junit.Before;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

/**
 * Created by ceposta
 * <a href="http://christianposta.com/blog>http://christianposta.com/blog</a>.
 */
public class ServerTestSupport {

    // 30s timeout
    protected long timeout = 30 * 1000;

    @Before
    public void setUp() throws InterruptedException {
        JMXConnector connector = null;
        long startTime = System.currentTimeMillis();

        while (connector == null) {

            if (elapsedTimeout(startTime)) {
                break;
            }
            try {
                connector = getJMXConnector();
            } catch (Exception e) {
                //don't worry too much, just sleep and try again
                TimeUnit.MICROSECONDS.sleep(1000);
            }

        }
    }

    private boolean elapsedTimeout(long startTime) {
        return System.currentTimeMillis() - startTime > timeout;
    }

    public JMXConnector getJMXConnector() throws Exception {
        return getJMXConnector("admin", "admin");
    }

    public JMXConnector getJMXConnector(String userName, String passWord) throws Exception {
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + FuseTestSupport.RMI_REG_PORT+ "/karaf-root");
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        String[] credentials = new String[]{ userName, passWord };
        env.put("jmx.remote.credentials", credentials);
        JMXConnector connector = JMXConnectorFactory.connect(url, env);
        return connector;
    }
}
