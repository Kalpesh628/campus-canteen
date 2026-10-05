<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<h2>Order #${order.id} <span class="badge bg-secondary">${order.status}</span></h2>
<p class="text-muted">${order.userName} &middot; ${order.slot.slotDate} ${order.slot.label} &middot; placed ${order.createdAt}</p>
<c:if test="${order.status == 'REJECTED'}">
  <div class="alert alert-danger">Rejected. Reason: ${order.rejectReason}</div>
</c:if>
<c:if test="${not empty order.note}"><div class="alert alert-info">Kitchen note: <c:out value="${order.note}"/></div></c:if>

<table class="table">
  <thead><tr><th>Dish</th><th>Qty</th><th>Price</th><th>Total</th></tr></thead>
  <tbody>
    <c:forEach var="it" items="${order.items}">
      <tr><td>${it.itemName}</td><td>${it.qty}</td>
        <td>&#8377;<fmt:formatNumber value="${it.priceAtOrder}" minFractionDigits="2"/></td>
        <td>&#8377;<fmt:formatNumber value="${it.lineTotal}" minFractionDigits="2"/></td></tr>
    </c:forEach>
  </tbody>
</table>
<h5>Total: <span class="text-success">&#8377;<fmt:formatNumber value="${order.total}" minFractionDigits="2"/></span> (Pay at Canteen)</h5>

<c:if test="${not empty nextStatuses}">
  <div class="card shadow-sm mt-3"><div class="card-body">
    <h5>Advance status</h5>
    <form method="post" action="${pageContext.request.contextPath}/admin/orders" class="row g-2">
      <input type="hidden" name="id" value="${order.id}">
      <div class="col-md-3">
        <select class="form-select" name="newStatus" id="ns" onchange="document.getElementById('rej').style.display = this.value=='REJECTED' ? 'block':'none'">
          <c:forEach var="s" items="${nextStatuses}"><option value="${s}">${s}</option></c:forEach>
        </select>
      </div>
      <div class="col-md-6" id="rej" style="display:none">
        <input class="form-control" name="rejectReason" placeholder="Rejection reason (required for REJECTED)">
      </div>
      <div class="col-md-3"><button class="btn btn-primary w-100">Update</button></div>
    </form>
  </div></div>
</c:if>
<a class="btn btn-outline-secondary mt-3" href="${pageContext.request.contextPath}/admin/orders">Back to orders</a>
<%@ include file="/WEB-INF/jsp/inc/footer.jsp" %>
