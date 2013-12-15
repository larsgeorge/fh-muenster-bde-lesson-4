package org.fhmuenster.bde.http;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;

import org.fhmuenster.bde.guice.module.PortalModule;
import org.fhmuenster.bde.guice.module.PortalServletModule;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * Creates the injector within the listener so that  JSP pages can retrieve it from the
 * servlet context.
 */
public class PortalServletContextListener extends GuiceServletContextListener {

  private static Injector injector = null;

  @Override
  protected Injector getInjector() {
    return injector;
  }

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    // no call to super as it also calls getInjector()
    ServletContext sc = servletContextEvent.getServletContext();
    sc.setAttribute(Injector.class.getName(), getInjector());
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    ServletContext sc = servletContextEvent.getServletContext();
    sc.removeAttribute(Injector.class.getName());
    super.contextDestroyed(servletContextEvent);
  }

  static {
    injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        binder().requireExplicitBindings();
        install(new PortalModule());
        install(new PortalServletModule());
        bind(GuiceFilter.class);
      }
    });
  }
}
