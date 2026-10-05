<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<h2 class="mb-3">Checkout</h2>
<div class="alert alert-info">Payment mode: <strong>Pay at Canteen</strong> - pay cash/UPI when you pick up. No online payment needed.</div>

<div class="row">
  <div class="col-md-7">
    <div class="card shadow-sm">
      <div class="card-body">
        <h5>1. Choose pickup date</h5>
        <div class="btn-group mb-3" role="group">
          <c:forEach var="d" items="${dates}">
            <a class="btn ${d == chosenDate ? 'btn-success' : 'btn-outline-success'}"
               href="${pageContext.request.contextPath}/checkout?date=${d}">${d}</a>
          </c:forEach>
        </div>

        <h5>2. Choose a time slot</h5>
        <form method="post" action="${pageContext.request.contextPath}/checkout" onsubmit="return vSlot()">
          <div id="slotList" class="list-group mb-3">
            <c:forEach var="s" items="${slots}">
              <label class="list-group-item ${s.full ? 'disabled text-muted' : ''}">
                <input class="form-check-input me-2" type="radio" name="slotId" value="${s.id}" ${s.full ? 'disabled' : ''}>
                <strong>${s.label}</strong>
                <span class="badge ${s.full ? 'bg-danger' : 'bg-success'} ms-2">${s.full ? 'FULL' : s.remaining} ${s.full ? '' : 'left'}</span>
              </label>
            </c:forEach>
            <c:if test="${empty slots}">
              <div class="alert alert-warning mb-0">No slots defined for this date yet. Please try another date.</div>
            </c:if>
          </div>

          <h5>3. Note for the kitchen <small class="text-muted">(optional)</small></h5>
          <textarea class="form-control mb-3" name="note" rows="2" maxlength="255" placeholder="e.g. less spicy, no onion..."></textarea>

          <button class="btn btn-success btn-lg w-100">Place Order - Pay at Canteen</button>
        </form>
      </div>
    </div>
  </div>
  <div class="col-md-5">
    <div class="card shadow-sm">
      <div class="card-body">
        <h5>Order summary</h5>
        <ul class="list-group list-group-flush">
          <c:forEach var="e" items="${sessionScope.cart}">
            <li class="list-group-item d-flex justify-content-between">
              <span>${e.value.name} &times; ${e.value.qty}</span>
              <span>&#8377;<fmt:formatNumber value="${e.value.lineTotal}" minFractionDigits="2"/></span>
            </li>
          </c:forEach>
        </ul>
      </div>
    </div>
  </div>
</div>

<script>
// Rebuild the slot list when the date changes, without reloading the page.
document.querySelectorAll('a.btn').forEach(function(a){
  if(!a.href.includes('/checkout?date=')) return;
  a.addEventListener('click', function(ev){
    ev.preventDefault();
    var url = new URL(a.href);
    var date = url.searchParams.get('date');
    document.querySelectorAll('a.btn').forEach(function(x){ x.className = x.className.replace('btn-success','btn-outline-success'); });
    a.className = a.className.replace('btn-outline-success','btn-success');
    fetch('${pageContext.request.contextPath}/slots?date=' + date)
      .then(function(r){ return r.json(); })
      .then(function(slots){
        var box = document.getElementById('slotList');
        box.innerHTML = '';
        if(slots.length === 0){
          box.innerHTML = '<div class="alert alert-warning mb-0">No slots for this date yet.</div>';
          return;
        }
        slots.forEach(function(s){
          var full = s.remaining <= 0;
          var lbl = document.createElement('label');
          lbl.className = 'list-group-item' + (full ? ' disabled text-muted' : '');
          lbl.innerHTML = '<input class="form-check-input me-2" type="radio" name="slotId" value="' + s.id + '"' + (full ? ' disabled' : '') + '>'
            + '<strong>' + s.label + '</strong> '
            + '<span class="badge ' + (full ? 'bg-danger' : 'bg-success') + ' ms-2">' + (full ? 'FULL' : s.remaining + ' left') + '</span>';
          box.appendChild(lbl);
        });
      });
  });
});
function vSlot(){
  if(!document.querySelector('input[name=slotId]:checked')){ alert('Please choose a pickup slot.'); return false; }
  return true;
}
</script>
<%@ include file="/WEB-INF/jsp/inc/footer.jsp" %>
