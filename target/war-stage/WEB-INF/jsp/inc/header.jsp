<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- NOTE: these URIs must match the <uri> declared in the JSTL jar's
     META-INF/*.tld files; this JSTL 2.0 impl jar uses the classic URIs. --%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Campus Canteen</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-expand-lg navbar-dark bg-success mb-4">
  <div class="container">
    <a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/menu">&#127860; Campus Canteen</a>
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
