<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<section class="hero">
  <div class="kicker">// today's drop — pre-order now, pick up hot</div>
  <h1>Skip the line.<br><span class="hl">Grab</span> <span class="stroke">your grub.</span></h1>
  <p class="sub">Order from your phone between lectures. We fire it up, you walk in, <b>pickup in minutes</b> — and you pay at the counter, like always.</p>
  <div class="chip-row">
    <span class="chip"><b>&#9889;</b> zero queue</span>
    <span class="chip">pay at canteen — <b>no prepaid drama</b></span>
    <span class="chip tilt"><b>new</b> neon build v2.0</span>
    <span class="chip">pickup slots <b>live</b></span>
  </div>
</section>
<div class="ticker" aria-hidden="true"><div class="track">
  <span>fresh &amp; hot</span><b>&#183;</b><span>order ahead</span><b>&#183;</b><span>skip the queue</span><b>&#183;</b><span>pay at counter</span><b>&#183;</b><span>veg + non-veg</span><b>&#183;</b><span>pickup slots live</span><b>&#183;</b><span>fresh &amp; hot</span><b>&#183;</b><span>order ahead</span><b>&#183;</b><span>skip the queue</span><b>&#183;</b><span>pay at counter</span><b>&#183;</b>
</div></div>
<h2 class="mb-3 mt-4">Today's Menu</h2>

<form class="row g-2 mb-4" method="get" action="${pageContext.request.contextPath}/menu">
  <div class="col-md-5">
    <input class="form-control" name="q" placeholder="Search dishes..." value="<c:out value='${q}'/>">
  </div>
  <div class="col-md-3">
    <select class="form-select" name="category">
      <option value="">All categories</option>
      <c:forEach var="c" items="${categories}">
        <option value="${c}" ${c == category ? 'selected' : ''}>${c}</option>
      </c:forEach>
    </select>
  </div>
  <div class="col-md-2">
    <select class="form-select" name="veg">
      <option value="all" ${veg == 'all' ? 'selected' : ''}>Veg + Non-veg</option>
      <option value="veg" ${veg == 'veg' ? 'selected' : ''}>Veg only</option>
      <option value="nonveg" ${veg == 'nonveg' ? 'selected' : ''}>Non-veg only</option>
    </select>
  </div>
  <div class="col-md-2">
    <button class="btn btn-success w-100">Search</button>
  </div>
</form>

<c:if test="${empty items}">
  <div class="alert alert-warning">No dishes match your search. Try clearing the filters.</div>
</c:if>

<div class="row">
  <c:forEach var="m" items="${items}">
    <div class="col-md-4 mb-4">
      <div class="card h-100 shadow-sm">
        <c:if test="${not empty m.imageUrl}">
          <img src="${m.imageUrl}" class="card-img-top" alt="${m.name}" style="height:180px;object-fit:cover;">
        </c:if>
        <div class="card-body d-flex flex-column">
          <h5 class="card-title">
            <span class="${m.veg ? 'veg-dot' : 'nonveg-dot'}" title="${m.veg ? 'Veg' : 'Non-veg'}"></span>
            ${m.name}
          </h5>
          <p class="card-text text-muted small flex-grow-1">${m.description}</p>
          <div class="d-flex justify-content-between align-items-center mb-2">
            <span class="badge bg-secondary">${m.category}</span>
            <strong class="text-success fs-5">&#8377;<fmt:formatNumber value="${m.price}" minFractionDigits="2"/></strong>
          </div>
          <c:choose>
            <c:when test="${empty sessionScope.user}">
              <a class="btn btn-outline-success btn-sm" href="${pageContext.request.contextPath}/login?next=/menu">Login to order</a>
            </c:when>
            <c:otherwise>
              <form method="post" action="${pageContext.request.contextPath}/cart" class="d-flex gap-2">
                <input type="hidden" name="action" value="add">
                <input type="hidden" name="id" value="${m.id}">
                <input type="number" name="qty" value="1" min="1" max="20" class="form-control form-control-sm" style="width:70px">
                <button class="btn btn-success btn-sm flex-grow-1">Add to Cart</button>
              </form>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </c:forEach>
</div>
<%@ include file="/WEB-INF/jsp/inc/footer.jsp" %>
