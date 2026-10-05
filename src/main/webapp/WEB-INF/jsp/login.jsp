<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<div class="row justify-content-center mt-4">
  <div class="col-md-5">
    <div class="card shadow-sm">
      <div class="card-body p-4">
        <h3 class="card-title mb-3">Student Login</h3>
        <c:if test="${param.reset == 'ok'}">
          <div class="alert alert-success">Password reset successful. Please log in with your new password.</div>
        </c:if>
        <c:if test="${not empty error}">
          <div class="alert alert-danger">${error}</div>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/login" onsubmit="return vLogin()">
          <input type="hidden" name="csrfToken" value="${csrfToken}">
          <input type="hidden" name="next" value="<c:out value='${next}'/>">
          <div class="mb-3">
            <label class="form-label">Email</label>
            <input type="email" class="form-control" name="email" id="email" required
                   placeholder="you@acpce.ac.in">
            <div class="form-text">Students: your college email (@acpce.ac.in).</div>
          </div>
          <div class="mb-3">
            <label class="form-label">Password</label>
            <input type="password" class="form-control" name="password" id="password" required>
          </div>
          <button class="btn btn-success w-100">Login</button>
        </form>
        <p class="mt-3 mb-1 text-center"><a href="${pageContext.request.contextPath}/forgot">Forgot password?</a></p>
        <p class="mt-0 mb-0 text-center">New here? <a href="${pageContext.request.contextPath}/register">Create an account</a></p>
      </div>
    </div>
  </div>
</div>
<script>
function vLogin(){
  var e = document.getElementById('email').value.trim();
  var p = document.getElementById('password').value;
  if(!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(e)){ alert('Enter a valid email.'); return false; }
  if(p.length < 1){ alert('Enter your password.'); return false; }
  return true;
}
</script>
<%@ include file="/WEB-INF/jsp/inc/footer.jsp" %>
