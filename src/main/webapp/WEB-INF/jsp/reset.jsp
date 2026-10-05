<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<div class="row justify-content-center mt-4">
  <div class="col-md-5">
    <div class="card shadow-sm">
      <div class="card-body p-4">
        <h3 class="card-title mb-2">Reset your password</h3>
        <p class="text-muted">Enter the 6-digit code sent to <strong>${email}</strong>, then choose a new password.</p>
        <c:if test="${not empty error}">
          <div class="alert alert-danger">${error}</div>
        </c:if>
        <c:if test="${not empty demoCode}">
          <div class="alert alert-warning">
            <strong>Demo mode</strong> — no email server configured, so your code is shown here:
            <span class="fs-4 fw-bold d-block mt-1" style="letter-spacing:6px">${demoCode}</span>
          </div>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/reset">
          <input type="hidden" name="email" value="${email}">
          <div class="mb-3">
            <label class="form-label">6-digit code</label>
            <input class="form-control form-control-lg text-center" name="code" inputmode="numeric"
                   pattern="[0-9]{6}" maxlength="6" placeholder="••••••" required
                   style="letter-spacing:8px" autocomplete="one-time-code">
          </div>
          <div class="mb-3">
            <label class="form-label">New password</label>
            <input class="form-control" type="password" name="newPassword" required minlength="6"
                   placeholder="At least 6 characters" autocomplete="new-password">
          </div>
          <div class="mb-3">
            <label class="form-label">Confirm new password</label>
            <input class="form-control" type="password" name="confirmPassword" required minlength="6"
                   placeholder="Repeat the new password" autocomplete="new-password">
          </div>
          <button class="btn btn-success w-100">Set new password</button>
        </form>
        <form method="post" action="${pageContext.request.contextPath}/forgot" class="mt-2">
          <input type="hidden" name="email" value="${email}">
          <button class="btn btn-link w-100">Didn't get the code? Send a fresh one</button>
        </form>
      </div>
    </div>
  </div>
</div>
<%@ include file="/WEB-INF/jsp/inc/footer.jsp" %>
