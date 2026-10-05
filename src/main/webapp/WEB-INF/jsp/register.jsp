<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<div class="row justify-content-center mt-4">
  <div class="col-md-6">
    <div class="card shadow-sm">
      <div class="card-body p-4">
        <h3 class="card-title mb-3">Create Student Account</h3>
        <c:if test="${not empty error}">
          <div class="alert alert-danger">${error}</div>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/register" onsubmit="return vReg()">
          <div class="mb-3">
            <label class="form-label">Full name</label>
            <input class="form-control" name="name" id="name" value="${name}" required>
          </div>
          <div class="row">
            <div class="col-md-6 mb-3">
              <label class="form-label">Email</label>
              <input type="email" class="form-control" name="email" id="email" value="${email}" required>
            </div>
            <div class="col-md-6 mb-3">
              <label class="form-label">Phone</label>
              <input class="form-control" name="phone" id="phone" value="${phone}" placeholder="9876543210">
            </div>
          </div>
          <div class="row">
            <div class="col-md-6 mb-3">
              <label class="form-label">Password (min 6 chars)</label>
              <input type="password" class="form-control" name="password" id="pw" required>
            </div>
            <div class="col-md-6 mb-3">
              <label class="form-label">Confirm password</label>
              <input type="password" class="form-control" name="confirm" id="pw2" required>
            </div>
          </div>
          <button class="btn btn-success w-100">Register</button>
        </form>
        <p class="mt-3 mb-0 text-center">Already registered? <a href="${pageContext.request.contextPath}/login">Login</a></p>
      </div>
    </div>
  </div>
</div>
<script>
function vReg(){
  var n = document.getElementById('name').value.trim();
  var e = document.getElementById('email').value.trim();
  var p = document.getElementById('pw').value;
  var p2 = document.getElementById('pw2').value;
  if(n.length < 2){ alert('Enter your full name.'); return false; }
  if(!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(e)){ alert('Enter a valid email.'); return false; }
  if(p.length < 6){ alert('Password must be at least 6 characters.'); return false; }
  if(p !== p2){ alert('Passwords do not match.'); return false; }
  return true;
}
</script>
<%@ include file="/WEB-INF/jsp/inc/footer.jsp" %>
