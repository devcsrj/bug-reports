import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.vysper.mina.TCPEndpoint;
import org.apache.vysper.storage.inmemory.MemoryStorageProviderRegistry;
import org.apache.vysper.xmpp.addressing.EntityImpl;
import org.apache.vysper.xmpp.authorization.AccountManagement;
import org.apache.vysper.xmpp.modules.extension.xep0119_xmppping.XmppPingModule;
import org.apache.vysper.xmpp.modules.extension.xep0202_entity_time.EntityTimeModule;
import org.apache.vysper.xmpp.server.XMPPServer;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SampleTest {

    private static final String DOMAIN = "localhost";

    private final String username1 = "user1";
    private final String password1 = "password1";
    private XMPPServer xmppServer;

    private Path cert;
    private int port;
    private final String certPw = "boguspw";

    @BeforeClass
    public void beforeClass() throws Exception {
        MemoryStorageProviderRegistry providerRegistry = new MemoryStorageProviderRegistry();
        AccountManagement accountManagement = (AccountManagement) providerRegistry.retrieve( AccountManagement.class );
        accountManagement.addUser( EntityImpl.parse( username1 ), password1 );

        cert = Paths.get( getClass().getResource( "/bogus_mina_tls.cert" ).toURI() );

        port = findFreePort();
        TCPEndpoint tcpEndpoint = new TCPEndpoint();
        tcpEndpoint.setPort( port );

        xmppServer = new XMPPServer( DOMAIN );
        xmppServer.addEndpoint( tcpEndpoint );
        xmppServer.setStorageProviderRegistry( providerRegistry );
        xmppServer.setTLSCertificateInfo( cert.toFile(), certPw );
        xmppServer.start();

        xmppServer.addModule( new EntityTimeModule() );
        xmppServer.addModule( new XmppPingModule() );
    }

    @Test
    public void testConnect() throws IOException, XMPPException, SmackException {

        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
            .setHost( DOMAIN )
            .setPort( port )
            .setUsernameAndPassword( username1, password1 )
            .setServiceName( DOMAIN )
            .setKeystorePath( cert.toString() )
            .setSocketFactory( TrustAllSslSocketFactory.createSSLSocketFactory() )
            .build();
        XMPPTCPConnection connection = new XMPPTCPConnection( config );
        connection.connect();
        connection.disconnect();
    }


    @AfterClass
    public void afterClass() throws Exception {
        xmppServer.stop();
    }

    private int findFreePort() {
        try ( ServerSocket socket = new ServerSocket( 0 ) ) {
            socket.setReuseAddress( true );
            return socket.getLocalPort();
        } catch ( IOException e ) {
            throw new IllegalStateException( "Unable to find free port", e );
        }
    }
}
