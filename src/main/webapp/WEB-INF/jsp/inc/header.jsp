<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- NOTE: these URIs must match the <uri> declared in the JSTL jar's
     META-INF/*.tld files; this JSTL 2.0 impl jar uses the classic URIs. --%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <c:set var="sp" value="${pageContext.request.servletPath}"/>
  <c:choose>
    <c:when test="${sp.contains('admin')}"><c:set var="pageTitle" value="Admin"/></c:when>
    <c:when test="${sp.contains('menu')}"><c:set var="pageTitle" value="Today's Menu"/></c:when>
    <c:when test="${sp.contains('login')}"><c:set var="pageTitle" value="Student Login"/></c:when>
    <c:when test="${sp.contains('register')}"><c:set var="pageTitle" value="Create Student Account"/></c:when>
    <c:when test="${sp.contains('forgot')}"><c:set var="pageTitle" value="Forgot Password"/></c:when>
    <c:when test="${sp.contains('cart')}"><c:set var="pageTitle" value="Your Cart"/></c:when>
    <c:when test="${sp.contains('track')}"><c:set var="pageTitle" value="Track Order"/></c:when>
    <c:when test="${sp.contains('orders')}"><c:set var="pageTitle" value="My Orders"/></c:when>
    <c:when test="${sp.contains('checkout')}"><c:set var="pageTitle" value="Checkout"/></c:when>
    <c:otherwise><c:set var="pageTitle" value=""/></c:otherwise>
  </c:choose>
  <title><c:if test="${not empty pageTitle}">${pageTitle} &#183; </c:if>Campus Canteen</title>
  <meta name="description" content="Campus Canteen - pre-order from the college canteen, skip the queue, pick up at your slot.">
  <meta property="og:title" content="Campus Canteen">
  <meta property="og:description" content="Pre-order from the college canteen, skip the queue, pick up at your slot.">
  <meta property="og:type" content="website">
  <meta property="og:url" content="https://canteen.kptimes.in/">
  <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/favicon.svg">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-expand-lg navbar-dark bg-success mb-4 sticky-top">
  <div class="container">
    <a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/menu"><span class="brand-badge">&#127860;</span> Campus Canteen</a>
    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#nav">
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="nav">
      <ul class="navbar-nav me-auto">
        <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/menu">Menu</a></li>
        <c:if test="${not empty sessionScope.user}">
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/orders">My Orders</a></li>
        </c:if>
        <c:if test="${sessionScope.user.admin}">
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/admin">Admin</a></li>
        </c:if>
      </ul>
      <ul class="navbar-nav">
        <c:choose>
          <c:when test="${empty sessionScope.user}">
            <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/login">Login</a></li>
            <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/register">Register</a></li>
          </c:when>
          <c:otherwise>
            <li class="nav-item">
              <a class="nav-link position-relative" href="${pageContext.request.contextPath}/cart">Cart
                <c:if test="${not empty sessionScope.cart}">
                  <span class="badge bg-warning text-dark">${sessionScope.cart.size()}</span>
                </c:if>
              </a>
            </li>
            <li class="nav-item"><span class="nav-link text-light">Hi, ${sessionScope.user.name}!</span></li>
            <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/logout">Logout</a></li>
          </c:otherwise>
        </c:choose>
      </ul>
    </div>
  </div>
</nav>
<div class="container">
  <c:if test="${not empty flash}">
    <div class="alert alert-info alert-dismissible fade show">${flash}
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
    <c:remove var="flash" scope="session"/>
  </c:if>
