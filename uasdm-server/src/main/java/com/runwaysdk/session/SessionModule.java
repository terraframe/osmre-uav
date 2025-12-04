package com.runwaysdk.session;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.runwaysdk.constants.LocalProperties;

public class SessionModule implements Module
{

  @Request
  public void configure(Binder binder)
  {
    SessionCache cache = build();

    binder.bind(SessionCache.class).toInstance(cache);
  }

  @Request
  private SessionCache build()
  {
    // System.out.println("***********************************************************");
    // System.out.println("BUILDING SESSION CACHE IN REQUEST");
    // System.out.println("***********************************************************");

    SessionCache cache = new BufferedSessionCache( //
        new OverflowSessionCache( //
            new MemorySessionCache(100, 100), //
            new FileSessionCache(LocalProperties.getSessionCacheDirectory())), //
        new MemorySessionCache()); //

    return cache;
  }

}
