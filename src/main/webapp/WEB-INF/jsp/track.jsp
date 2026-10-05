<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<h2 class="mb-1">Order #${order.id}</h2>
<p class="text-muted">Placed on ${order.createdAt} &middot; Pickup: ${order.slot.slotDate} ${order.slot.label}</p>

<c:choose>
  <c:when test="${order.status == 'REJECTED'}">
    <div class="alert alert-danger"><strong>Rejected by canteen.</strong> Reason: ${order.rejectReason}</div>
  </c:when>
  <c:otherwise>
    <%-- visual pipeline: PLACED -> ACCEPTED -> PREPARING -> READY -> PICKED_UP --%>
    <c:set var="steps" value="${['PLACED','ACCEPTED','PREPARING','READY','PICKED_UP']}"/>
    <c:set var="orderIdx" value="0"/>
    <c:forEach var="st" items="${steps}" varStatus="vs">
      <c:if test="${st == order.status}"><c:set var="orderIdx" value="${vs.index}"/></c:if>
    </c:forEach>
    <div class="timeline">
      <c:forEach var="st" items="${steps}" varStatus="vs">
        <c:choose>
          <c:when test="${vs.index < orderIdx}"><c:set var="cls" value="done"/></c:when>
          <c:when test="${vs.index == orderIdx}"><c:set var="cls" value="now"/></c:when>
          <c:otherwise><c:set var="cls" value=""/></c:otherwise>
        </c:choose>
        <div class="tstep ${cls}">
          <div class="dot">${vs.index + 1}</div>${st}
        </div>
      </c:forEach>
    </div>
  </c:otherwise>
</c:choose>

<div class="card shadow-sm mb-3">
  <div class="card-body">
    <h5>Items</h5>
    <table class="table">
      <thead><tr><th>Dish</th><th>Qty</th><th>Price</th><th>Total</th></tr></thead>
      <tbody>
        <c:forEach var="it" items="${order.items}">
          <tr>
            <td>${it.itemName}</td><td>${it.qty}</td>
            <td>&#8377;<fmt:formatNumber value="${it.priceAtOrder}" minFractionDigits="2"/></td>
            <td>&#8377;<fmt:formatNumber value="${it.lineTotal}" minFractionDigits="2"/></td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
    <div class="d-flex justify-content-between">
      <span>Payment: <strong>Pay at Canteen</strong></span>
      <h5>Total: <span class="text-success">&#8377;<fmt:formatNumber value="${order.total}" minFractionDigits="2"/></span></h5>
    </div>
    <c:if test="${not empty order.note}"><p class="text-muted">Note: <c:out value="${order.note}"/></p></c:if>
  </div>
</div>
<a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/orders">Back to My Orders</a>
<%@ include file="/WEB-INF/jsp/inc/footer.jsp" %>
