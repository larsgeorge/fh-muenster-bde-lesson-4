<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.google.inject.Injector" %>
<%@ page import="org.fhmuenster.bde.api.PortalApi" %>
<%@ page import="org.fhmuenster.bde.config.PortalConfiguration" %>
<%@ page import="org.fhmuenster.bde.http.PortalJspHelper" %>
<%@ page import="org.fhmuenster.bde.http.SearchParameters" %>
<%@ page import="org.fhmuenster.bde.model.ResultDocument" %>
<%@ page import="org.fhmuenster.bde.model.DocumentInfo" %>
<%
  int errorCode = 0;
  // get or create object instance references
  ServletContext ctx = pageContext.getServletContext();
  Injector injector = (Injector) ctx.getAttribute(Injector.class.getName());
  PortalConfiguration conf = injector.getInstance(PortalConfiguration.class);
  PortalJspHelper helper = new PortalJspHelper(request, response, ctx, conf);
  SearchParameters params = helper.getSearchParameters();
  PortalApi portalApi = null;
  // get request details and set up response variables
  boolean dumpError = request.getParameter("SHOWERROR") != null;
  String searchText = params.getSearchText();
  String searchResult = null;
  ArrayList<ResultDocument> docs = null;
  int numFound = 0;
  boolean hasSearchResults = false;
  long msecsUsed = 0;
  // check if we can actually execute the search
  if (searchText != null && searchText.length() > 0) {
    portalApi = injector.getInstance(PortalApi.class);
    long time = System.currentTimeMillis();
    try {
      searchResult = portalApi.search(searchText);
      docs = helper.convertResultDocuments(searchResult);
      numFound = docs.size();
    } catch (Exception e) {
      errorCode = 99;
      if (dumpError) {
        throw e;
      }
    }
    hasSearchResults = errorCode == 0;
    msecsUsed = System.currentTimeMillis() - time;
  }
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Search Portal</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="description" content="Search Portal">
  <meta name="author" content="bde">
  <!-- styles -->
  <link href="assets/css/bootstrap.css" rel="stylesheet">
  <link href="assets/css/portal.css" rel="stylesheet">
</head>

<body>
<div class="navbar navbar-inverse navbar-fixed-top portal-nav">
  <div class="container">
    <div class="navbar-header portal-nav">
      <button type="button" class="navbar-toggle" data-toggle="collapse"
              data-target=".navbar-collapse">
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
      <a class="navbar-brand" href="/">Search Portal</a>
    </div>
    <div class="navbar-collapse collapse">
      <ul class="nav navbar-nav">
        <li class="active"><a href="/">Home</a></li>
        <li><a href="#about">About</a></li>
        <li><a href="#contact">Contact</a></li>
      </ul>
    </div>
  </div>
</div>
<%
  if (!hasSearchResults) {  // show default page
%>
<div class="container">
  <div class="portal-main-div">
    <%
      if (errorCode > 0) {
    %>
    <div class="alert alert-danger">
      An error occurred.
      <% if (errorCode == 1) { %>
      The search server is not responsive!
      <% } %>
      (Code: <%=errorCode%>)
    </div>
    <%
      }
    %>
    <h1>Search Portal!</h1>

    <p class="lead">Welcome to the Search Portal, where you can enter words and find documents.</p>

    <form role="form">
      <div class="search-text input-group input-group-lg">
        <input type="search" class="form-control search-text"
          <% if (searchText == null) {%>
               placeholder="Search..."
          <% } else { %>
               value="<%=searchText%>"
          <% } %>
               name="q">
        <span class="input-group-btn">
          <button class="btn btn-default" type="submit">
            <span class="glyphicon glyphicon-search"></span>
          </button>
        </span>
      </div>
    </form>
  </div>
</div>
<div class="container">
  <hr>
  <footer>
    <p>&copy; 2013 bde</p>
  </footer>
</div>

<%
  } else { // display search results
%>
<p></p>

<div class="portal-fluid-container">
<%  /* ========================== LEFT SIDE COLUMN ========================== */  %>
<%--<div class="col-md-3">--%>
<%--</div>--%>
<%  /* ========================= MAIN COLUMN HEADER ========================= */  %>
<%--<div class="col-md-9">--%>
<div class="col-md-12">
  <form role="form">
    <div class="search-text input-group input-group">
      <input type="search" class="form-control search-text" name="q"
             value="<%= searchText %>">
          <span class="input-group-btn">
            <button class="btn btn-default" type="submit">
              <span class="glyphicon glyphicon-search"></span>
            </button>
          </span>
    </div>
  </form>
  <div class="text-muted">
    <small>
      Found <%=numFound%> documents in <%=(double) msecsUsed / 1000.0 %> seconds.
    </small>
  </div>
  <%  /* ========================= RESULT DOCUMENTS ========================= */  %>
  <p></p>
  <table class="portal-search-docs table table-hover">
    <tbody>
    <%
      for (ResultDocument doc : docs) {
        DocumentInfo info = portalApi.getDocumentInfo(doc.getName());
        int docId = helper.getDocumentId(doc.getName());
    %>
    <tr>
      <td>
        <h4>
          <a href="http://www.gutenberg.org/ebooks/<%=docId%>"><%= info != null ? info.getTitle() : doc.getName() %></a>
        </h4>
        <div>
          Author: <%= info != null && info.getAuthor() != null ? info.getAuthor() : "n/a" %>
        </div>
        <div>
          Release: <%= info != null && info.getReleaseDate() != null ? info.getReleaseDate() : "n/a" %>
        </div>
        <div class="text-muted">
          <small>Score <%= String.format("%f", doc.getScore()) %> | ID: <%=docId%></small>
        </div>
      </td>
    </tr>
    <%
      }
    %>
    </tbody>
  </table>
</div>
</div>
<%
  } // end of search results
%>
<%  /* ========================= JavaScript Section ========================= */  %>
<!-- Javascript, placed at the end of the document so the pages load faster -->
<script src="assets/js/jquery-1.10.2.min.js"></script>
<script src="assets/js/bootstrap.min.js"></script>
<script>
  $(document).ready(function () {
    $('.search-text').focus();
  });
</script>
</body>
</html>

