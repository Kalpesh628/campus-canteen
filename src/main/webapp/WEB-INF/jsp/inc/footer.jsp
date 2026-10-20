</div><!-- /.container -->
<footer class="cc-foot">
  <div class="container text-center">
    <div class="mb-2"><b style="color:var(--cc-ink)">Campus Canteen</b> &middot; pre-order &middot; pickup &middot; pay at the counter</div>
    <div class="mono stack">running on <b>Java 17</b> &middot; <b>Tomcat 10</b> &middot; <b>MySQL 8</b> &middot; <b>JSP + Servlets</b> &middot; status: <b style="color:var(--cc-lime)">200 OK</b> &middot; <span id="cc-clock">--:--:-- IST</span></div>
    <div class="mono mt-2" style="color:#5f7263">FSJP mini project &middot; neon build v2.0 &middot; no queues were harmed</div>
  </div>
</footer>
<script>
  (function(){var el=document.getElementById('cc-clock');if(!el)return;
    function tick(){try{el.textContent=new Date().toLocaleTimeString('en-IN',{timeZone:'Asia/Kolkata',hour12:false})+' IST';}catch(e){}}
    tick();setInterval(tick,1000);})();
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
