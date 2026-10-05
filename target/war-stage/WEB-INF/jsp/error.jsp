<%@ include file="/WEB-INF/jsp/inc/header.jsp" %>
<div class="row justify-content-center mt-5">
  <div class="col-md-6 text-center">
    <div class="display-1 mb-3">&#9888;&#65039;</div>
    <h2>${empty errorTitle ? "Oops! Something went wrong." : errorTitle}</h2>
    <p class="text-muted">${empty errorMessage ? "Please try again, or go back to the menu." : errorMessage}</p>
    <a class="btn btn-success" href="${pageContext.request.contextPath}/menu">Back to Menu</a>
  </div>
</div>
<%@ include file="/WEB-INF/jsp/inc/footer.jsp" %>
