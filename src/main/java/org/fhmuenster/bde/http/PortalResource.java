package org.fhmuenster.bde.http;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.fhmuenster.bde.api.PortalApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Singleton
@Produces(MediaType.APPLICATION_JSON)
@ThreadSafe
@Path("/rest")
public class PortalResource {

  protected final static Logger LOG = LoggerFactory.getLogger(PortalResource.class);

  private final PortalApi portalApi;

  @Inject
  PortalResource(PortalApi portalApi) {
    this.portalApi = portalApi;
  }

  @GET
  @Path("/search")
  @Produces({"application/json"})
  public String search(@QueryParam("q") String searchText) throws IOException {
    return portalApi.search(searchText);
  }

  @GET
  @Path("/version")
  public String version() throws Exception {
    return "0.1";
  }

  /* TESTING ONLY BELOW */

  /**
   * This method does nothing but serves as an entry point to send test events to the service.
   *
   * @throws Exception When the request fails.
   */
  @POST
  @Path("/noop")
  public void noOp() throws Exception {
    LOG.info("NOOP Called!");
    portalApi.noop();
  }

}