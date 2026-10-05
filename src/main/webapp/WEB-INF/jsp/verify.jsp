<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<div class="row justify-content-center mt-4">
  <div class="col-md-5">
    <div class="card shadow-sm">
      <div class="card-body p-4">
        <h3 class="card-title mb-2">Verify your phone</h3>
        <p class="text-muted">We sent a 6-digit code to <strong>${maskedPhone}</strong>. Enter it below (expires in 15 minutes).</p>
        <c:if test="${not empty error}">
          <div class="alert alert-danger">${error}</div>
        </c:if>
        <c:if test="${not empty info}">
          <div class="alert alert-success">${info}</div>
        </c:if>
        <c:if test="${not empty demoCode}">
          <div class="alert alert-warning">
            <strong>Demo mode</strong> — no SMS gateway configured, so your code is shown here:
            <span class="fs-4 fw-bold d-block mt-1" style="letter-spacing:6px">${demoCode}</span>
          </div>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/verify">
          <input type="hidden" name="action" value="verify">
          <input type="hidden" name="email" value="${email}">
          <input type="hidden" name="next" value="${next}">
          <div class="mb-3">
            <label class="form-label">6-digit code</label>
            <input class="form-control form-control-lg text-center" name="code" inputmode="numeric"
                   pattern="[0-9]{6}" maxlength="6" placeholder="••••••" required
                   style="letter-spacing:8px" autocomplete="one-time-code">
          </div>
          <button class="btn btn-success w-100">Verify &amp; continue</button>
        </form>
        <form method="post" action="${pageContext.request.contextPath}/verify" class="mt-2">
          <input type="hidden" name="action" value="resend">
          <input type="hidden" name="email" value="${email}">
          <input type="hidden" name="next" value="${next}">
          <button class="btn btn-link w-100">Didn't get the code? Resend</button>
        </form>
      </div>
    </div>
  </div>
</div>
<%@ include file="/WEB-INF/jsp/inc/footer.jsp" %>
