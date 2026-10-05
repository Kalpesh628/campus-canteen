<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<div class="row justify-content-center mt-4">
  <div class="col-md-5">
    <div class="card shadow-sm">
      <div class="card-body p-4">
        <h3 class="card-title mb-3">Change Admin Password</h3>
        <c:if test="${not empty error}">
          <div class="alert alert-danger">${error}</div>
        </c:if>
        <c:if test="${not empty info}">
          <div class="alert alert-success">${info}</div>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/admin/password"
              onsubmit="return vPw()">
          <div class="mb-3">
            <label class="form-label">Current password</label>
            <input type="password" class="form-control" name="current" id="cur" required>
          </div>
          <div class="mb-3">
            <label class="form-label">New password (min 6 chars)</label>
            <input type="password" class="form-control" name="next" id="pw" required>
          </div>
          <div class="mb-3">
            <label class="form-label">Confirm new password</label>
            <input type="password" class="form-control" name="confirm" id="pw2" required>
          </div>
          <button class="btn btn-success w-100">Update password</button>
        </form>
        <p class="mt-3 mb-0 text-center"><a href="${pageContext.request.contextPath}/admin">Back to dashboard</a></p>
      </div>
    </div>
  </div>
</div>
<script>
function vPw(){
  var p = document.getElementById('pw').value;
  var p2 = document.getElementById('pw2').value;
  if(p.length < 6){ alert('New password must be at least 6 characters.'); return false; }
  if(p !== p2){ alert('New passwords do not match.'); return false; }
  return true;
}
</script>
<%@ include file="/WEB-INF/jsp/inc/footer.jsp" %>
