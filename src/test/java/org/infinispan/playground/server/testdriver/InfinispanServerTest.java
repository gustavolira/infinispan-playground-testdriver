package org.infinispan.playground.server.testdriver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.file.Paths;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.infinispan.client.hotrod.transaction.lookup.RemoteTransactionManagerLookup;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.server.test.core.ServerRunMode;
import org.infinispan.server.test.core.TestSystemPropertyNames;
import org.infinispan.server.test.junit4.InfinispanServerRule;
import org.infinispan.server.test.junit4.InfinispanServerRuleBuilder;
import org.infinispan.server.test.junit4.InfinispanServerTestMethodRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Tristan Tarrant &lt;tristan@infinispan.org&gt;
 **/
public class InfinispanServerTest {
   @ClassRule
   public static final InfinispanServerRule SERVERS =
         InfinispanServerRuleBuilder.config(Paths.get("src/test/resources/infinispan-cluster.xml").toAbsolutePath().toString())
               .numServers(2)
               .runMode(ServerRunMode.FORKED)
               .build();

   @Rule
   public InfinispanServerTestMethodRule SERVER_TEST = new InfinispanServerTestMethodRule(SERVERS);

   @Test
   public void testHotRodWithDefaultConfiguration() {
      RemoteCache<String, String> cache = SERVER_TEST.hotrod().withCacheMode(CacheMode.DIST_SYNC).create();
      cache.put("k1", "v1");
      assertEquals(1, cache.size());
      assertEquals("v1", cache.get("k1"));
   }

   @Test
   public void testHotRodWithCustomClientConfiguration() {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.statistics().enable();
      RemoteCache<String, String> cache = SERVER_TEST.hotrod().withClientConfiguration(builder).withCacheMode(CacheMode.DIST_SYNC).create();
      cache.put("k1", "v1");
      assertEquals(1, cache.size());
      assertEquals("v1", cache.get("k1"));
      assertEquals(1, cache.clientStatistics().getRemoteStores());
   }

   @Test
   public void testHotRodWithCustomServerConfiguration() throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
      String SERVER_CACHE_XML_CONFIG = String.format(
            "<infinispan><cache-container>" +
                  "  <distributed-cache-configuration name=\"%s\">" +
                  "    <locking isolation=\"REPEATABLE_READ\"/>" +
                  "    <transaction locking=\"PESSIMISTIC\" mode=\"NON_XA\" />" +
                  "  </distributed-cache-configuration>" +
                  "</cache-container></infinispan>",
            SERVER_TEST.getMethodName());
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.transaction().transactionMode(TransactionMode.NON_XA);
      builder.transaction().transactionManagerLookup(RemoteTransactionManagerLookup.getInstance());
      RemoteCache<String, String> cache = SERVER_TEST.hotrod().withClientConfiguration(builder).withServerConfiguration(new XMLStringConfiguration(SERVER_CACHE_XML_CONFIG)).create();
      TransactionManager tm = cache.getTransactionManager();
      tm.begin();
      cache.put("k", "v1");
      assertEquals("v1", cache.get("k"));
      tm.commit();

      assertEquals("v1", cache.get("k"));

      tm.begin();
      cache.put("k", "v2");
      cache.put("k2", "v1");
      assertEquals("v2", cache.get("k"));
      assertEquals("v1", cache.get("k2"));
      tm.rollback();

      assertEquals("v1", cache.get("k"));
      assertNull(cache.get("k2"));
   }
}
