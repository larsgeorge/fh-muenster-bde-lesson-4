package org.fhmuenster.bde.guice.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import org.fhmuenster.bde.api.PortalApi;
import org.fhmuenster.bde.config.PortalConfiguration;

public class PortalModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(PortalConfiguration.class).in(Scopes.SINGLETON);
    bind(PortalApi.class).in(Scopes.SINGLETON);
  }
}