<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<h2 class="mb-3">Today's Menu</h2>

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
