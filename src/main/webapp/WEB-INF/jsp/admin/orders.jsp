<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<h2 class="mb-3">Orders</h2>
<form class="row g-2 mb-3" method="get" action="${pageContext.request.contextPath}/admin/orders">
  <div class="col-md-3">
    <select class="form-select" name="status" onchange="this.form.submit()">
      <option value="">All statuses</option>
      <c:forEach var="s" items="${allStatuses}">
        <option value="${s}" ${s == statusFilter ? 'selected' : ''}>${s}</option>
      </c:forEach>
    </select>
  </div>
</form>
<table class="table table-striped align-middle">
  <thead><tr><th>#</th><th>Student</th><th>Pickup</th><th>Total</th><th>Status</th><th></th></tr></thead>
  <tbody>
    <c:forEach var="o" items="${orders}">
      <tr>
        <td>${o.id}</td><td>${o.userName}</td>
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
        <td class="text-end"><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/orders?id=${o.id}">Open</a></td>
      </tr>
    </c:forEach>
    <c:if test="${empty orders}"><tr><td colspan="6" class="text-muted">No orders found.</td></tr></c:if>
  </tbody>
</table>
<%@ include file="/WEB-INF/jsp/inc/footer.jsp" %>
