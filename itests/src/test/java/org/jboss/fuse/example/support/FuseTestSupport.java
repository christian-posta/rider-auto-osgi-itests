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

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.ProbeBuilder;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.*;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;


/**
 * Created by ceposta
 * <a href="http://christianposta.com/blog>http://christianposta.com/blog</a>.
 */
public class FuseTestSupport {

    // note, for this to work, you must download and put fuse in the location
    // specified by the maven coordinates here
    public static final String GROUP_ID = "org.jboss.fuse";
    public static final String ARTIFACT_ID = "jboss-fuse-minimal";
    public static final String VERSION = "6.1.0.redhat-379";

    static final Long COMMAND_TIMEOUT = 10000L;
    static final Long SERVICE_TIMEOUT = 30000L;

    // can further enhance this to auto discover the ports at run time
    // so they don't conflict with other tests/running versions of Fuse
    public static final String RMI_SERVER_PORT = "44445";
    public static final String HTTP_PORT = "9081";
    public static final String RMI_REG_PORT = "1100";

    ExecutorService executor = Executors.newCachedThreadPool();

    @Inject
    protected BundleContext bundleContext;

    @ProbeBuilder
    public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
        probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE, "*,org.apache.felix.service.*;status=provisional");
        return probe;
    }

    @Configuration
    public Option[] config() {
        // we could maybe configure some external dependencies here, like
        // activemq for example
        return container();
    }

    public static Option[] container() {
        return new Option[]{
                karafDistributionConfiguration()
                        .frameworkUrl(maven().groupId(GROUP_ID).artifactId(ARTIFACT_ID).version(VERSION).type("zip"))
                        .karafVersion("2.3.0")
                        .useDeployFolder(false)
                        .name("JBoss Fuse")
                        .unpackDirectory(new File("target/paxexam/unpack")),
                configureConsole().ignoreLocalConsole(),
                editConfigurationFilePut("etc/config.properties", "karaf.startup.message", "Loading Fabric from: ${karaf.home}"),
                editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.port", HTTP_PORT),
                editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiRegistryPort", RMI_REG_PORT),
                editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiServerPort", RMI_SERVER_PORT),
                editConfigurationFilePut("etc/users.properties", "admin", "admin,admin"),

                // this is the key... we can install features, bundles, etc. using these pax-exam options
                features(maven().groupId("org.jboss.fuse.examples").artifactId("rider-auto-common").versionAsInProject().classifier("features").type("xml"),
                        "rider-auto-osgi"),
                logLevel(LogLevelOption.LogLevel.INFO),

                // enable this if you want to keep the exploded directories of fuse after the tests are run
//                keepRuntimeFolder(),

        };
    };

    /**
     * Executes a shell command and returns output as a String.
     * Commands have a default timeout of 10 seconds.
     *
     * @param command
     * @return
     */
    protected String executeCommand(final String command) {
        return executeCommand(command, COMMAND_TIMEOUT, false);
    }

    /**
     * Executes a shell command and returns output as a String.
     * Commands have a default timeout of 10 seconds.
     *
     * @param command The command to execute.
     * @param timeout The amount of time in millis to wait for the command to execute.
     * @param silent  Specifies if the command should be displayed in the screen.
     * @return
     */
    protected String executeCommand(final String command, final Long timeout, final Boolean silent) {
        String response;
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(byteArrayOutputStream);
        final CommandProcessor commandProcessor = getOsgiService(CommandProcessor.class);
        final CommandSession commandSession = commandProcessor.createSession(System.in, printStream, System.err);
        FutureTask<String> commandFuture = new FutureTask<String>(
                new Callable<String>() {
                    public String call() {
                        try {
                            if (!silent) {
                                System.err.println(command);
                            }
                            commandSession.execute(command);
                        } catch (Exception e) {
                            e.printStackTrace(System.err);
                        }
                        printStream.flush();
                        return byteArrayOutputStream.toString();
                    }
                });

        try {
            executor.submit(commandFuture);
            response = commandFuture.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            response = "SHELL COMMAND TIMED OUT: ";
        }

        return response;
    }

    protected <T> T getOsgiService(Class<T> type, long timeout) {
        return getOsgiService(type, null, timeout);
    }

    protected <T> T getOsgiService(Class<T> type) {
        return getOsgiService(type, null, SERVICE_TIMEOUT);
    }

    protected <T> T getOsgiService(Class<T> type, String filter, long timeout) {
        ServiceTracker tracker = null;
        try {
            String flt;
            if (filter != null) {
                if (filter.startsWith("(")) {
                    flt = "(&(" + Constants.OBJECTCLASS + "=" + type.getName() + ")" + filter + ")";
                } else {
                    flt = "(&(" + Constants.OBJECTCLASS + "=" + type.getName() + ")(" + filter + "))";
                }
            } else {
                flt = "(" + Constants.OBJECTCLASS + "=" + type.getName() + ")";
            }
            Filter osgiFilter = FrameworkUtil.createFilter(flt);
            tracker = new ServiceTracker(bundleContext, osgiFilter, null);
            tracker.open(true);
            // Note that the tracker is not closed to keep the reference
            // This is buggy, as the service reference may change i think
            Object svc = type.cast(tracker.waitForService(timeout));
            if (svc == null) {
                Dictionary dic = bundleContext.getBundle().getHeaders();
                System.err.println("Test bundle headers: " + explode(dic));

                for (ServiceReference ref : asCollection(bundleContext.getAllServiceReferences(null, null))) {
                    System.err.println("ServiceReference: " + ref);
                }

                for (ServiceReference ref : asCollection(bundleContext.getAllServiceReferences(null, flt))) {
                    System.err.println("Filtered ServiceReference: " + ref);
                }

                throw new RuntimeException("Gave up waiting for service " + flt);
            }
            return type.cast(svc);
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException("Invalid filter", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /*
* Explode the dictionary into a ,-delimited list of key=value pairs
*/
    private static String explode(Dictionary dictionary) {
        Enumeration keys = dictionary.keys();
        StringBuffer result = new StringBuffer();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            result.append(String.format("%s=%s", key, dictionary.get(key)));
            if (keys.hasMoreElements()) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    /**
     * Provides an iterable collection of references, even if the original array is null
     */
    private static Collection<ServiceReference> asCollection(ServiceReference[] references) {
        return references != null ? Arrays.asList(references) : Collections.<ServiceReference>emptyList();
    }
}
