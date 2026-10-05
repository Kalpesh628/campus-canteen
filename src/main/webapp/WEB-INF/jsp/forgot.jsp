<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<div class="row justify-content-center mt-4">
  <div class="col-md-5">
    <div class="card shadow-sm">
      <div class="card-body p-4">
        <h3 class="card-title mb-2">Forgot password?</h3>
        <p class="text-muted">Enter your college email and we'll send you a 6-digit code to reset your password (expires in 15 minutes).</p>
        <c:if test="${not empty error}">
          <div class="alert alert-danger">${error}</div>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/forgot">
          <div class="mb-3">
            <label class="form-label">College email</label>
            <input class="form-control" type="email" name="email" required
                   placeholder="you@acpce.ac.in" autocomplete="email">
          </div>
          <button class="btn btn-success w-100">Send reset code</button>
        </form>
        <div class="text-center mt-3">
          <a href="${pageContext.request.contextPath}/login">Back to login</a>
        </div>
      </div>
    </div>
  </div>
</div>
<%@ include file="/WEB-INF/jsp/inc/footer.jsp" %>
