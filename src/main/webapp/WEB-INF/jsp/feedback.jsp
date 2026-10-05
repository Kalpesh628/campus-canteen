<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<div class="row justify-content-center mt-4">
  <div class="col-md-6">
    <div class="card shadow-sm">
      <div class="card-body p-4">
        <h3>Rate Order #${order.id}</h3>
        <p class="text-muted">Picked up on ${order.slot.slotDate}. How was your food?</p>
        <form method="post" action="${pageContext.request.contextPath}/feedback" onsubmit="return vFb()">
          <input type="hidden" name="orderId" value="${order.id}">
          <div class="mb-3">
            <label class="form-label">Rating</label>
            <select class="form-select" name="rating" id="rating">
              <option value="5">5 - Excellent</option>
              <option value="4">4 - Good</option>
              <option value="3">3 - Average</option>
              <option value="2">2 - Poor</option>
              <option value="1">1 - Terrible</option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-label">Comment <small class="text-muted">(optional)</small></label>
            <textarea class="form-control" name="comment" rows="3" maxlength="1000"></textarea>
          </div>
          <button class="btn btn-success w-100">Submit Feedback</button>
        </form>
      </div>
    </div>
  </div>
</div>
<script>
function vFb(){
  var r = parseInt(document.getElementById('rating').value, 10);
  if(!(r >= 1 && r <= 5)){ alert('Pick a rating between 1 and 5.'); return false; }
  return true;
}
</script>
<%@ include file="/WEB-INF/jsp/inc/footer.jsp" %>
