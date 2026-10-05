<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<h2 class="mb-3">Student Feedback</h2>
<c:choose>
  <c:when test="${empty feedbacks}">
    <div class="alert alert-info">No feedback yet.</div>
  </c:when>
  <c:otherwise>
    <table class="table table-striped">
      <thead><tr><th>Order</th><th>Student</th><th>Rating</th><th>Comment</th><th>When</th></tr></thead>
      <tbody>
        <c:forEach var="f" items="${feedbacks}">
          <tr>
            <td><a href="${pageContext.request.contextPath}/admin/orders?id=${f.orderId}">#${f.orderId}</a></td>
            <td>${f.userName}</td>
            <td>
              <c:choose>
                <c:when test="${f.rating >= 4}"><span class="badge bg-success">${f.rating} / 5</span></c:when>
                <c:when test="${f.rating == 3}"><span class="badge bg-warning text-dark">${f.rating} / 5</span></c:when>
                <c:otherwise><span class="badge bg-danger">${f.rating} / 5</span></c:otherwise>
              </c:choose>
            </td>
            <td>${f.comment}</td>
            <td class="text-muted small">${f.createdAt}</td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/jsp/inc/footer.jsp" %>
