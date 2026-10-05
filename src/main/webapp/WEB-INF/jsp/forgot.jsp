<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<div class="row justify-content-center mt-4">
  <div class="col-md-5">
    <div class="card shadow-sm">
      <div class="card-body p-4">
        <h3 class="card-title mb-2">Forgot password?</h3>
        <p class="text-muted">Enter your registered mobile number and we'll send you a 6-digit code to reset your password (expires in 15 minutes).</p>
        <c:if test="${not empty error}">
          <div class="alert alert-danger">${error}</div>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/forgot">
          <div class="mb-3">
            <label class="form-label">Mobile number</label>
            <input class="form-control" name="phone" required inputmode="numeric"
                   placeholder="9876543210" autocomplete="tel">
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
