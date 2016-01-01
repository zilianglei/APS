package se.natusoft.osgi.aps.tcpipsvc

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.osgi.aps.api.net.tcpip.NetworkConfig
import se.natusoft.osgi.aps.api.net.tcpip.TCPRequest
import se.natusoft.osgi.aps.api.net.tcpip.TCPListener
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigList
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValue
import se.natusoft.osgi.aps.tcpipsvc.config.ExpertConfig
import se.natusoft.osgi.aps.tcpipsvc.config.GroupConfig
import se.natusoft.osgi.aps.tcpipsvc.config.NamedConfig
import se.natusoft.osgi.aps.tcpipsvc.config.TCPIPConfig
import se.natusoft.osgi.aps.tcpipsvc.security.TCPSecurityHandler
import se.natusoft.osgi.aps.tools.APSLogger

import static org.junit.Assert.assertTrue

/**
 * Test UDPReceiver and UDPSender.
 */
@CompileStatic
@TypeChecked
class TCPConProviderTest {

    private static boolean isTestActive() {
        return !(System.getProperty("aps.test.disabled") == "true")
    }

    private static void configSetup1() {

        NamedConfig namedConfig1 = new NamedConfig()
        namedConfig1.configName = new TestConfigValue(value: "testsvc")
        namedConfig1.type = new TestConfigValue(value: NetworkConfig.Type.TCP.name())
        namedConfig1.address = new TestConfigValue(value: "localhost")
        namedConfig1.port = new TestConfigValue(value: "12345")
        namedConfig1.secure = new TestConfigValue(value: "false")

        NamedConfig namedConfig2 = new NamedConfig()
        namedConfig2.configName = new TestConfigValue(value: "testclient")
        namedConfig2.type = new TestConfigValue(value: NetworkConfig.Type.TCP.name())
        namedConfig2.address = new TestConfigValue(value: "localhost")
        namedConfig2.port = new TestConfigValue(value: "12345")
        namedConfig2.secure = new TestConfigValue(value: "false")

        TestConfigList<NamedConfig> configs = new TestConfigList<>()
        configs.configs.add(namedConfig1)
        configs.configs.add(namedConfig2)

        GroupConfig groupConfig1 = new GroupConfig()
        groupConfig1.groupName = new TestConfigValue(value: "test")
        groupConfig1.namedConfigs = configs

        TestConfigList<GroupConfig> groupConfigs = new TestConfigList<>()
        groupConfigs.configs.add(groupConfig1)

        TCPIPConfig testTCPIPConfig = new TCPIPConfig()
        testTCPIPConfig.groupConfigs = groupConfigs

        ExpertConfig expertConfig = new ExpertConfig()
        expertConfig.exceptionGuardMaxExceptions = new TestConfigValue(value: "30")
        expertConfig.exceptionGuardReactLimit = new TestConfigValue(value: "300")
        expertConfig.tcpCallbackThreadPoolSize = new TestConfigValue(value: "30")

        testTCPIPConfig.expert = expertConfig

        TCPIPConfig.managed.serviceProviderAPI.configInstance = testTCPIPConfig
        TCPIPConfig.managed.serviceProviderAPI.setManaged() // VERY IMPORTANT!
    }

    @Test
    public void testSendAndReceive() throws Exception {
        if (testActive) {
            configSetup1()

            assertTrue("Config failure!", TCPIPConfig.managed.get().groupConfigs.get(0).namedConfigs.get(0).configName.string == "testsvc")
            assertTrue("Config failure!", TCPIPConfig.managed.get().groupConfigs.get(0).namedConfigs.get(1).configName.string == "testclient")

            TCPSecurityHandler tcpSecurityHandler = new TCPSecurityHandler()

            TCPReceiver receiver = new TCPReceiver(
                    config: new ConfigWrapper(name: "test/testsvc"),
                    logger: new APSLogger(),
                    securityHandler: tcpSecurityHandler
            )

            receiver.setListener(new TCPListener() {
                @Override
                void tcpRequestReceived(String name, InetAddress address, InputStream tcpReqStream, OutputStream tcpRespStream)
                        throws IOException {
                    System.out.println("name:${name}")

                    DataInputStream dis = new DataInputStream(tcpReqStream)
                    String reqMsg = dis.readUTF()
                    DataOutputStream dos = new DataOutputStream(tcpRespStream)
                    if (reqMsg == "This is a test string!") {
                        dos.writeUTF("OK")
                    }
                    else {
                        dos.writeUTF("BAD")
                    }
                    dos.flush()

//                    dos.close() // Uncomment to test the close() catch.
                }
            })

            receiver.start()

                TCPSender sender = new TCPSender(
                        config: new ConfigWrapper(name: "test/testclient"),
                        securityHandler: tcpSecurityHandler
                )
                sender.start()
                    String resp = ""

                    sender.send(new TCPRequest() {
                        @Override
                        void tcpRequest(OutputStream requestStream, InputStream responseStream) throws IOException {
                            DataOutputStream dos = new DataOutputStream(requestStream)
                            dos.writeUTF("This is a test string!")
                            dos.flush()

                            println("Sent req, waiting answer ...")

                            DataInputStream dis = new DataInputStream(responseStream)
                            resp = dis.readUTF()
                        }
                    })

                    assertTrue("Expected 'OK', got '${resp}'!", resp == "OK")

                    println("Got OK Response!")
                sender.stop()

            receiver.stop()

        }
        else {
            println("This test is currently disabled!")
            println("Run with -Daps.test.disabled=false to run it.")
        }

        System.out.println("Test done!")
    }

}
