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

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.reactors.EagerSingleStagedReactor;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import java.net.URI;
import java.util.List;

/**
 * Created by ceposta
 * <a href="http://christianposta.com/blog>http://christianposta.com/blog</a>.
 */
public class ActiveMQPerClass extends PerClass {

    protected BrokerService brokerService = null;

    @Override
    public StagedExamReactor create(List<TestContainer> containers, List<TestProbeBuilder> mProbes) {
        return new EagerSingleStagedReactor(containers, mProbes){
            @Override
            public void beforeClass() {
                bootStrapActiveMQ();
                super.beforeClass();
            }

            @Override
            public void afterClass() {
                teardownActiveMQ();
                super.afterClass();
            }
        };
    }

    protected void bootStrapActiveMQ() {
        try {
            brokerService = BrokerFactory.createBroker(new URI("broker://()/localhost?useJmx=true&deleteAllMessagesOnStartup=true"));

            // you can also bootstrap the broker from an activemq config file like this:
//            BrokerFactoryBean bean = new BrokerFactoryBean();
//            bean.setConfig(new ClassPathResource("/activemq.xml"));
//            bean.afterPropertiesSet();
//            brokerService = bean.getBroker();


            brokerService.setDataDirectory("target/activemq-data");
            brokerService.start();
            brokerService.waitUntilStarted();

        } catch (Exception e) {
            throw new IllegalStateException("Cannot bootstrap broker, failing test", e);
        }
    }


    protected void teardownActiveMQ() {
        if (brokerService != null) {
            try {
                brokerService.stop();
                brokerService.waitUntilStopped();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
