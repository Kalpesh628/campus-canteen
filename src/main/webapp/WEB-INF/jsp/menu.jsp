<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<div class="hero-panel">
  <h1>Today's Menu</h1>
  <p class="page-subtitle">Fresh from the kitchen &#8212; pre-order, skip the queue, pick up at your slot.</p>
  <div class="hero-chips"><span>Pre-order online</span><span>Pay at the canteen</span><span>Pickup slots</span></div>
</div>

<form class="row g-2 mb-4 filter-bar" method="get" action="${pageContext.request.contextPath}/menu">
  <div class="col-md-5">
    <input class="form-control" name="q" placeholder="Search dishes..." value="<c:out value='${q}'/>">
  </div>
  <div class="col-6 col-md-3">
    <select class="form-select" name="category">
      <option value="">All categories</option>
      <c:forEach var="c" items="${categories}">
        <option value="<c:out value='${c}'/>" ${c == category ? 'selected' : ''}><c:out value="${c}"/></option>
      </c:forEach>
    </select>
  </div>
  <div class="col-6 col-md-2">
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
    <div class="col-sm-6 col-lg-4 mb-4">
      <div class="card dish-card h-100 shadow-sm">
        <c:if test="${not empty m.imageUrl}">
          <img src="<c:out value='${m.imageUrl}'/>" class="card-img-top" alt="<c:out value='${m.name}'/>" loading="lazy" decoding="async">
        </c:if>
        <c:if test="${empty m.imageUrl}">
          <c:choose>
            <c:when test="${m.category == 'Beverages'}"><c:set var="bandCls" value="band-beverages"/></c:when>
            <c:when test="${m.category == 'Breakfast'}"><c:set var="bandCls" value="band-breakfast"/></c:when>
            <c:when test="${m.category == 'Lunch'}"><c:set var="bandCls" value="band-lunch"/></c:when>
            <c:when test="${m.category == 'Snacks'}"><c:set var="bandCls" value="band-snacks"/></c:when>
            <c:otherwise><c:set var="bandCls" value=""/></c:otherwise>
          </c:choose>
          <div class="dish-band ${bandCls}" aria-hidden="true"><c:choose><c:when test="${m.name.contains('Coffee')}">&#9749;</c:when><c:when test="${m.name.contains('Chai')}">&#127861;</c:when><c:when test="${m.name.contains('Dosa')}">&#129374;</c:when><c:when test="${m.name.contains('Poha')}">&#127834;</c:when><c:when test="${m.name.contains('Biryani')}">&#127831;</c:when><c:when test="${m.name.contains('Paneer')}">&#127835;</c:when><c:when test="${m.name.contains('Thali')}">&#127869;</c:when><c:when test="${m.name.contains('Samosa')}">&#129375;</c:when><c:when test="${m.name.contains('Sandwich')}">&#129386;</c:when><c:otherwise>&#127860;</c:otherwise></c:choose></div>
        </c:if>
        <div class="card-body d-flex flex-column">
          <h5 class="card-title">
            <span class="${m.veg ? 'veg-dot' : 'nonveg-dot'}" title="${m.veg ? 'Veg' : 'Non-veg'}"></span>
            <c:out value="${m.name}"/>
          </h5>
          <p class="card-text desc small flex-grow-1"><c:out value="${m.description}"/></p>
          <div class="d-flex justify-content-between align-items-center mb-2">
            <span class="badge cat-badge"><c:out value="${m.category}"/></span>
            <span class="price-pill">&#8377;<fmt:formatNumber value="${m.price}" minFractionDigits="2"/></span>
          </div>
          <c:choose>
            <c:when test="${empty sessionScope.user}">
              <a class="btn btn-outline-success btn-sm" href="${pageContext.request.contextPath}/login?next=/menu">Login to order</a>
            </c:when>
            <c:otherwise>
              <form method="post" action="${pageContext.request.contextPath}/cart" class="d-flex gap-2">
                <input type="hidden" name="action" value="add">
                <input type="hidden" name="id" value="${m.id}">
                <input type="number" name="qty" value="1" min="1" max="20" class="form-control form-control-sm qty-input">
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
