<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<h2 class="mb-3">Admin Dashboard</h2>

<div class="row mb-4">
  <div class="col-md-3"><div class="card text-center shadow-sm"><div class="card-body">
    <h6 class="text-muted">Today's orders</h6><h3>${todayCount}</h3></div></div></div>
  <div class="col-md-3"><div class="card text-center shadow-sm"><div class="card-body">
    <h6 class="text-muted">Today's revenue</h6><h3 class="text-success">&#8377;<fmt:formatNumber value="${todayRevenue}" minFractionDigits="2"/></h3></div></div></div>
  <div class="col-md-3"><div class="card text-center shadow-sm"><div class="card-body">
    <h6 class="text-muted">Pending orders</h6><h3 class="text-warning">${pendingCount}</h3></div></div></div>
  <div class="col-md-3"><div class="card text-center shadow-sm"><div class="card-body">
    <h6 class="text-muted">Items unavailable</h6><h3 class="text-danger">${unavailableCount}</h3></div></div></div>
</div>

<div class="row">
  <div class="col-md-6">
    <div class="card shadow-sm mb-4"><div class="card-body">
      <h5>Orders per day (last 7 days)</h5>
      <table class="table table-sm"><tbody>
        <c:forEach var="r" items="${ordersPerDay}">
          <tr><td>${r[0]}</td><td><div class="progress"><div class="progress-bar bg-success" style="width:${r[1] * 10}%">${r[1]}</div></div></td></tr>
        </c:forEach>
      </tbody></table>
    </div></div>
  <div class="col-md-6">
    <div class="card shadow-sm mb-4"><div class="card-body">
      <h5>Top items (all time)</h5>
      <table class="table table-sm"><tbody>
        <c:forEach var="t" items="${topItems}">
          <tr><td>${t[0]}</td><td class="text-end"><span class="badge bg-success">${t[1]} sold</span></td></tr>
        </c:forEach>
        <c:if test="${empty topItems}"><tr><td class="text-muted">No sales yet.</td></tr></c:if>
      </tbody></table>
    </div></div>
</div>

<div class="card shadow-sm">
  <div class="card-body">
    <div class="d-flex justify-content-between align-items-center mb-2">
      <h5 class="mb-0">Recent orders</h5>
      <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/orders">All orders</a>
    </div>
    <table class="table table-sm">
      <thead><tr><th>#</th><th>Student</th><th>Pickup</th><th>Total</th><th>Status</th></tr></thead>
      <tbody>
        <c:forEach var="o" items="${recentOrders}">
          <tr>
            <td><a href="${pageContext.request.contextPath}/admin/orders?id=${o.id}">${o.id}</a></td>
            <td>${o.userName}</td>
            <td>${o.slot.slotDate} ${o.slot.label}</td>
            <td>&#8377;<fmt:formatNumber value="${o.total}" minFractionDigits="2"/></td>
            <td><span class="badge bg-secondary">${o.status}</span></td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
    <div class="mt-3">
      <a class="btn btn-outline-success me-2" href="${pageContext.request.contextPath}/admin/menu">Manage Menu</a>
      <a class="btn btn-outline-success me-2" href="${pageContext.request.contextPath}/admin/slots">Pickup Slots</a>
      <a class="btn btn-outline-success" href="${pageContext.request.contextPath}/admin/feedback">Feedback</a>
    </div>
  </div>
</div>
<%@ include file="/WEB-INF/jsp/inc/footer.jsp" %>
