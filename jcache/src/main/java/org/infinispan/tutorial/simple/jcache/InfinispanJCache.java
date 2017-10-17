package org.infinispan.tutorial.simple.jcache;

import java.lang.reflect.Method;
import java.net.URI;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import org.infinispan.AdvancedCache;
import org.infinispan.commons.util.ReflectionUtil;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.jcache.AbstractJCache;
import org.infinispan.jcache.embedded.ConfigurationAdapter;
import org.infinispan.jcache.embedded.JCache;
import org.infinispan.jcache.embedded.JCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

public class InfinispanJCache {

   public static void main(String[] args) {
      // Construct a simple local cache manager with default configuration
      CachingProvider jcacheProvider = Caching.getCachingProvider();
      CacheManager cacheManager = jcacheProvider.getCacheManager(URI.create("infinispan-jcache.xml"), InfinispanJCache.class.getClassLoader());
      createJCacheWithTemplate(cacheManager, "myCache", "inv-template");
      Cache<String, String> cache = cacheManager.getCache("myCache");
      // Store a value
      cache.put("key", "value");
      // Retrieve the value and print it out
      System.out.printf("key = %s\n", cache.get("key"));
      // Stop the cache manager and release all resources
      cacheManager.close();
   }

   // HACK ALERT !!!!
   private static void createJCacheWithTemplate(CacheManager cacheManager, String cacheName, String templateName) {
      // create a cache using the supplied configuration
      EmbeddedCacheManager embeddedCacheManager = cacheManager.unwrap(EmbeddedCacheManager.class);
      embeddedCacheManager.defineConfiguration("myCache", new ConfigurationBuilder().read(embeddedCacheManager.getCacheConfiguration(templateName)).template(false).build());
      try {
         Method registerPredefinedCache = ReflectionUtil.findMethod(JCacheManager.class, "registerPredefinedCache", new Class[]{String.class, AbstractJCache.class});

         AdvancedCache<Object, Object> cache = embeddedCacheManager.getCache(cacheName).getAdvancedCache();
         JCache jCache = new JCache(cache, cacheManager, ConfigurationAdapter.create());
         ReflectionUtil.invokeAccessibly(cacheManager, registerPredefinedCache, new Object[]{cacheName, jCache});
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

}
