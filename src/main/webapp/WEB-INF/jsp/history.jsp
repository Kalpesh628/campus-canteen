<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<h2 class="mb-3">My Orders</h2>
<c:choose>
  <c:when test="${empty orders}">
    <div class="alert alert-info">No orders yet. <a href="${pageContext.request.contextPath}/menu">Order something!</a></div>
  </c:when>
  <c:otherwise>
    <table class="table table-striped align-middle">
      <thead><tr><th>#</th><th>Pickup</th><th>Total</th><th>Status</th><th></th></tr></thead>
      <tbody>
        <c:forEach var="o" items="${orders}">
          <tr>
            <td>${o.id}</td>
            <td>${o.slot.slotDate} ${o.slot.label}</td>
            <td>&#8377;<fmt:formatNumber value="${o.total}" minFractionDigits="2"/></td>
            <td>
              <c:choose>
                <c:when test="${o.status=='PLACED'}"><span class="badge bg-secondary">PLACED</span></c:when>
                <c:when test="${o.status=='ACCEPTED'}"><span class="badge bg-info text-dark">ACCEPTED</span></c:when>
                <c:when test="${o.status=='PREPARING'}"><span class="badge bg-warning text-dark">PREPARING</span></c:when>
                <c:when test="${o.status=='READY'}"><span class="badge bg-primary">READY</span></c:when>
                <c:when test="${o.status=='PICKED_UP'}"><span class="badge bg-success">PICKED UP</span></c:when>
                <c:otherwise><span class="badge bg-danger">REJECTED</span></c:otherwise>
              </c:choose>
            </td>
            <td class="text-end">
              <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/track?id=${o.id}">Track</a>
              <c:if test="${o.status=='PICKED_UP' && !feedbackDone[o.id]}">
                <a class="btn btn-sm btn-outline-success" href="${pageContext.request.contextPath}/feedback?orderId=${o.id}">Rate</a>
              </c:if>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/jsp/inc/footer.jsp" %>
