package io.devcsrj.report.jclouds;

import static com.google.common.base.Preconditions.checkState;
import static org.jclouds.compute.options.RunScriptOptions.Builder.nameTask;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.profitbricks.ProfitBricksApi;
import org.jclouds.profitbricks.domain.DataCenter;
import org.jclouds.profitbricks.domain.Location;
import org.jclouds.profitbricks.features.DataCenterApi;
import org.jclouds.scriptbuilder.statements.login.AdminAccess;
import org.jclouds.sshj.config.SshjSshClientModule;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Minimal run for failing test {@code BaseComputeServiceLiveTest#testAScriptExecutionAfterBootWithBasicTemplate}.
 * <br/>
 * When {@link ListenableFuture} is cancelled, the provider throws:
 * <br/>
 * <pre>
 *  <code>
 * org.jclouds.rest.AuthorizationException: (foo:rsa[fingerprint(0d:81:67:75:04:fb:be:47:c3:ff:a9:b1:54:c3:69:c0),sha1(35:9c:dd:63:e3:f0:db:b9:79:b5:d7:90:1e:11:78:62:a1:9f:ca:8a)]@192.96.159.199:22) (foo:rsa[fingerprint(0d:81:67:75:04:fb:be:47:c3:ff:a9:b1:54:c3:69:c0),sha1(35:9c:dd:63:e3:f0:db:b9:79:b5:d7:90:1e:11:78:62:a1:9f:ca:8a)]@192.96.159.199:22) error acquiring {hostAndPort=192.96.159.199:22, loginUser=foo, ssh=1962400579, connectTimeout=60000, sessionTimeout=60000} (not retryable): Exhausted available authentication methods
 *  at net.schmizz.concurrent.Promise.retrieve(Promise.java:139)
 *  at net.schmizz.sshj.userauth.UserAuthImpl.authenticate(UserAuthImpl.java:69)
 *  at net.schmizz.sshj.SSHClient.auth(SSHClient.java:211)
 *  at net.schmizz.sshj.SSHClient.authPublickey(SSHClient.java:316)
 *  at net.schmizz.sshj.SSHClient.authPublickey(SSHClient.java:335)
 *  at org.jclouds.sshj.SSHClientConnection.create(SSHClientConnection.java:163)
 *  at org.jclouds.sshj.SSHClientConnection.create(SSHClientConnection.java:49)
 *  at org.jclouds.sshj.SshjSshClient.acquire(SshjSshClient.java:195)
 *  at org.jclouds.sshj.SshjSshClient.connect(SshjSshClient.java:225)
 *  at org.jclouds.compute.callables.SudoAwareInitManager.refreshAndRunAction(SudoAwareInitManager.java:74)
 *  at org.jclouds.compute.callables.BlockUntilInitScriptStatusIsZeroThenReturnOutput.interruptTask(BlockUntilInitScriptStatusIsZeroThenReturnOutput.java:159)
 *  at com.google.common.util.concurrent.AbstractFuture.cancel(AbstractFuture.java:136)
 *  at org.jclouds.compute.internal.BaseComputeServiceLiveTest.weCanCancelTasks(BaseComputeServiceLiveTest.java:305)
 *  at org.jclouds.compute.internal.BaseComputeServiceLiveTest.testAScriptExecutionAfterBootWithBasicTemplate(BaseComputeServiceLiveTest.java:262)
 *  at org.jclouds.profitbricks.compute.ProfitBricksComputeServiceLiveTest.testAScriptExecutionAfterBootWithBasicTemplate(ProfitBricksComputeServiceLiveTest.java:93)
 *  </code>
 * </pre>
 *
 * @see target/log/jclouds-ssh.log
 * @see https://github.com/jclouds/jclouds-labs/pull/224
 * @author Reijhanniel Jearl Campos
 */
public class RunScriptUsingPrivateKey {

    public static void main(String[] args) throws Exception {
        final String identity = "profitbricks email", credential = "password";
        final String imageUser = "root", imagePassword = "image password";
        final String snapshotId = "minimal ubuntu, with no password prompt on login";

        ComputeService client = ContextBuilder.newBuilder("profitbricks")
                .credentials(identity, credential)
                .modules(ImmutableSet.of(new SLF4JLoggingModule(), new SshjSshClientModule()))
                .buildView(ComputeServiceContext.class)
                .getComputeService();

        DataCenterApi dataCenterApi = client.getContext().unwrapApi(ProfitBricksApi.class).dataCenterApi();

        DataCenter testDc = null;
        try {
            System.out.println("Creating test datacenter");
            testDc = dataCenterApi.createDataCenter(
                    DataCenter.Request.creatingPayload("jclouds-1047", Location.US_LAS));

            System.out.println("Building template");
            Template template = client.templateBuilder()
                    .imageId(snapshotId)
                    .smallest()
                    .options(client.templateOptions()
                            .overrideLoginUser(imageUser)
                            .overrideLoginPassword(imagePassword))
                    .build();

            String group = "rrr";
            System.out.println("Bootstrapping node");
            NodeMetadata node = bootstrapAndReturnNode(client, group, template);

            System.out.println("Testing future.cancel()");
            testWeCanCancelTask(client, node);
        } catch (Exception ex) {
            throw ex;
        } finally {
            System.out.println("Deleting test datacenter");
            dataCenterApi.deleteDataCenter(testDc.id());
            client.getContext().close();
        }
    }

    private static void testWeCanCancelTask(ComputeService client, NodeMetadata node) throws InterruptedException, ExecutionException {
        // BaseComputeServiceLiveTest#weCanCancelTask
        ListenableFuture<ExecResponse> future = client.submitScriptOnNode(node.getId(), "sleep 300",
                nameTask("sleeper").runAsRoot(false));
        checkState(!future.isDone(), "future.isDone()");
        future.cancel(true);
        checkState(future.isCancelled(), "future.isCancelled()");
    }

    private static NodeMetadata bootstrapAndReturnNode(ComputeService client, String group, Template template) throws Exception {
        try {
            Set<? extends NodeMetadata> nodes = client.createNodesInGroup(group, 1, template);
            NodeMetadata node = Iterables.getOnlyElement(nodes);

            // test add foo
            ListenableFuture<ExecResponse> future = client.submitScriptOnNode(node.getId(), AdminAccess.builder()
                    .adminUsername("foo").adminHome("/over/ridden/foo").build(), nameTask("adminUpdate"));

            ExecResponse response = future.get(3, TimeUnit.MINUTES);
            checkState(response.getExitStatus() == 0, "Add foo failed");

            node = client.getNodeMetadata(node.getId());
            checkState(node.getCredentials().identity.equals("foo"), "Identity should've been changed to foo");

            return node;
        } catch (Exception ex) {
            throw ex;
        }
    }

}
