<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<h2 class="mb-3">Your Cart</h2>

<c:choose>
  <c:when test="${empty sessionScope.cart}">
    <div class="alert alert-info">Your cart is empty. <a href="${pageContext.request.contextPath}/menu">Browse the menu</a> to add something tasty.</div>
  </c:when>
  <c:otherwise>
    <table class="table table-striped align-middle">
      <thead>
        <tr><th>Dish</th><th>Type</th><th>Price</th><th>Qty</th><th>Total</th><th></th></tr>
      </thead>
      <tbody>
        <c:forEach var="e" items="${sessionScope.cart}">
          <c:set var="it" value="${e.value}"/>
          <tr>
            <td>${it.name}</td>
            <td><span class="${it.veg ? 'veg-dot' : 'nonveg-dot'}"></span> ${it.veg ? 'Veg' : 'Non-veg'}</td>
            <td>&#8377;<fmt:formatNumber value="${it.price}" minFractionDigits="2"/></td>
            <td>
              <form method="post" action="${pageContext.request.contextPath}/cart" class="d-flex gap-1">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="id" value="${it.menuItemId}">
                <input type="hidden" name="back" value="/cart">
                <input type="number" name="qty" value="${it.qty}" min="0" max="20" class="form-control form-control-sm" style="width:70px">
                <button class="btn btn-sm btn-outline-primary">Update</button>
              </form>
            </td>
            <td>&#8377;<fmt:formatNumber value="${it.lineTotal}" minFractionDigits="2"/></td>
            <td>
              <form method="post" action="${pageContext.request.contextPath}/cart">
                <input type="hidden" name="action" value="remove">
                <input type="hidden" name="id" value="${it.menuItemId}">
                <input type="hidden" name="back" value="/cart">
                <button class="btn btn-sm btn-outline-danger">Remove</button>
              </form>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
    <div class="d-flex justify-content-end align-items-center gap-3">
      <h4 class="mb-0">Total: <span class="text-success">&#8377;<fmt:formatNumber value="${cartTotal}" minFractionDigits="2"/></span></h4>
      <a class="btn btn-success btn-lg" href="${pageContext.request.contextPath}/checkout">Checkout &#8594;</a>
    </div>
  </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/jsp/inc/footer.jsp" %>
