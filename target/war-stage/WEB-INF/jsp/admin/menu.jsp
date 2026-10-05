<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<h2 class="mb-3">Manage Menu</h2>

<div class="card shadow-sm mb-4">
  <div class="card-body">
    <h5>${empty editItem ? 'Add new item' : 'Edit item'}</h5>
    <form method="post" action="${pageContext.request.contextPath}/admin/menu" class="row g-2">
      <input type="hidden" name="action" value="${empty editItem ? 'add' : 'update'}">
      <c:if test="${not empty editItem}"><input type="hidden" name="id" value="${editItem.id}"></c:if>
      <div class="col-md-4"><input class="form-control" name="name" placeholder="Dish name" value="${editItem.name}" required></div>
      <div class="col-md-2"><input class="form-control" name="price" placeholder="Price" type="number" step="0.01" min="1" value="${editItem.price}" required></div>
      <div class="col-md-3">
        <input class="form-control" name="category" list="cats" placeholder="Category" value="${editItem.category}" required>
        <datalist id="cats"><c:forEach var="c" items="${categories}"><option value="${c}"></c:forEach></datalist>
      </div>
      <div class="col-md-3"><input class="form-control" name="imageUrl" placeholder="Image URL (optional)" value="${editItem.imageUrl}"></div>
      <div class="col-md-8"><input class="form-control" name="description" placeholder="Short description" value="${editItem.description}"></div>
      <div class="col-md-2 form-check mt-2">
        <input class="form-check-input" type="checkbox" name="veg" id="veg" ${empty editItem || editItem.veg ? 'checked' : ''}>
        <label class="form-check-label" for="veg">Veg</label>
      </div>
      <div class="col-md-2">
        <button class="btn btn-success w-100">${empty editItem ? 'Add' : 'Update'}</button>
      </div>
      <c:if test="${not empty editItem}">
        <div class="col-12"><a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/admin/menu">Cancel edit</a></div>
      </c:if>
    </form>
  </div>
</div>

<table class="table table-striped align-middle">
  <thead><tr><th>#</th><th>Name</th><th>Category</th><th>Price</th><th>Veg</th><th>Available</th><th></th></tr></thead>
  <tbody>
    <c:forEach var="m" items="${items}">
      <tr class="${m.available ? '' : 'table-warning'}">
        <td>${m.id}</td><td>${m.name}</td><td>${m.category}</td>
        <td>&#8377;<fmt:formatNumber value="${m.price}" minFractionDigits="2"/></td>
        <td>${m.veg ? 'Veg' : 'Non-veg'}</td>
        <td>${m.available ? 'Yes' : 'No'}</td>
        <td class="text-end">
          <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/menu?edit=${m.id}">Edit</a>
          <form method="post" action="${pageContext.request.contextPath}/admin/menu" class="d-inline">
            <input type="hidden" name="action" value="toggle"><input type="hidden" name="id" value="${m.id}">
            <button class="btn btn-sm btn-outline-secondary">${m.available ? 'Hide' : 'Show'}</button>
          </form>
          <form method="post" action="${pageContext.request.contextPath}/admin/menu" class="d-inline"
                onsubmit="return confirm('Delete ${m.name}? Past orders keep their own copy, but prefer Hide for items with history.')">
            <input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${m.id}">
            <button class="btn btn-sm btn-outline-danger">Delete</button>
          </form>
        </td>
      </tr>
    </c:forEach>
  </tbody>
</table>
<%@ include file="/WEB-INF/jsp/inc/footer.jsp" %>
