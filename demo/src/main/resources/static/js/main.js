// ── Auto-dismiss alerts after 4 seconds ───────────────────────
document.addEventListener('DOMContentLoaded', function () {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            alert.style.transition = 'opacity 0.5s';
            alert.style.opacity = '0';
            setTimeout(function () { alert.remove(); }, 500);
        }, 4000);
    });
});

// ── Quantity input: prevent values below 1 ────────────────────
document.addEventListener('DOMContentLoaded', function () {
    const qtyInput = document.getElementById('quantity');
    if (qtyInput) {
        qtyInput.addEventListener('change', function () {
            if (parseInt(this.value) < 1) this.value = 1;
        });
    }
});

// ── Navbar dropdown toggle ─────────────────────────────────────
document.addEventListener('DOMContentLoaded', function () {
    const toggle = document.getElementById('dropdownToggle');
    const menu   = document.getElementById('dropdownMenu');

    if (!toggle || !menu) return;

    // Open/close on button click
    toggle.addEventListener('click', function (e) {
        e.stopPropagation();
        menu.classList.toggle('open');
    });

    // Close when clicking anywhere else on the page
    document.addEventListener('click', function (e) {
        if (!menu.contains(e.target) && e.target !== toggle) {
            menu.classList.remove('open');
        }
    });

    // Close when pressing Escape key
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            menu.classList.remove('open');
        }
    });
});