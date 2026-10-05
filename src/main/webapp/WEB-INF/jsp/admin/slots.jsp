<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<h2 class="mb-3">Pickup Slots</h2>
<div class="card shadow-sm mb-4"><div class="card-body">
  <h5>Add slot</h5>
  <form method="post" action="${pageContext.request.contextPath}/admin/slots" class="row g-2">
    <input type="hidden" name="action" value="add">
    <div class="col-md-3"><input type="date" class="form-control" name="date" required></div>
    <div class="col-md-2"><input type="time" class="form-control" name="start" required></div>
    <div class="col-md-2"><input type="time" class="form-control" name="end" required></div>
    <div class="col-md-2"><input type="number" class="form-control" name="maxOrders" min="1" max="500" value="30" required></div>
    <div class="col-md-3"><button class="btn btn-success w-100">Add Slot</button></div>
  </form>
</div></div>
<table class="table table-striped align-middle">
  <thead><tr><th>Date</th><th>Window</th><th>Max</th><th>Booked</th><th>Active</th><th></th></tr></thead>
  <tbody>
    <c:forEach var="s" items="${slots}">
      <tr class="${s.active ? '' : 'table-secondary'}">
        <td>${s.slotDate}</td><td>${s.label}</td><td>${s.maxOrders}</td>
        <td>${s.maxOrders - s.remaining}</td>
        <td>${s.active ? 'Yes' : 'No'}</td>
        <td class="text-end">
          <form method="post" action="${pageContext.request.contextPath}/admin/slots" class="d-inline">
            <input type="hidden" name="action" value="toggle"><input type="hidden" name="id" value="${s.id}">
            <button class="btn btn-sm btn-outline-secondary">${s.active ? 'Disable' : 'Enable'}</button>
          </form>
          <form method="post" action="${pageContext.request.contextPath}/admin/slots" class="d-inline"
                onsubmit="return confirm('Delete this slot? Only safe when nothing is booked.')">
            <input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${s.id}">
            <button class="btn btn-sm btn-outline-danger">Delete</button>
          </form>
        </td>
      </tr>
    </c:forEach>
    <c:if test="${empty slots}"><tr><td colspan="6" class="text-muted">No slots yet. Add the first one above.</td></tr></c:if>
  </tbody>
</table>
<%@ include file="/WEB-INF/jsp/inc/footer.jsp" %>
