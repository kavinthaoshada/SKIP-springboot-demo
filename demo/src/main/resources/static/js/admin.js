// ── Sidebar toggle ─────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', function () {

    const toggle  = document.getElementById('sidebarToggle');
    const sidebar = document.getElementById('adminSidebar');
    const main    = document.querySelector('.admin-main');

    if (toggle && sidebar && main) {
        toggle.addEventListener('click', function () {
            sidebar.classList.toggle('collapsed');
            main.classList.toggle('expanded');
        });
    }

    // ── Auto-dismiss alerts ────────────────────────────────────
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            alert.style.transition = 'opacity 0.5s';
            alert.style.opacity    = '0';
            setTimeout(function () {
                if (alert.parentNode) alert.remove();
            }, 500);
        }, 4000);
    });

    // ── Confirm dangerous actions ──────────────────────────────
    document.querySelectorAll('[data-confirm]').forEach(function (el) {
        el.addEventListener('click', function (e) {
            const msg = el.getAttribute('data-confirm');
            if (!confirm(msg)) e.preventDefault();
        });
    });
});

// ── Client-side table search ───────────────────────────────────
document.addEventListener('DOMContentLoaded', function () {

    // Users search
    const userSearch = document.getElementById('userSearch');
    if (userSearch) {
        userSearch.addEventListener('input', function () {
            filterTable('usersTable', this.value);
        });
    }

    // Role filter
    const roleFilter = document.getElementById('roleFilter');
    if (roleFilter) {
        roleFilter.addEventListener('change', function () {
            filterTable('usersTable', this.value);
        });
    }

    // Product search
    const productSearch = document.getElementById('productSearch');
    if (productSearch) {
        productSearch.addEventListener('input', function () {
            filterTable('productsTable', this.value);
        });
    }

    function filterTable(tableId, query) {
        const table = document.getElementById(tableId);
        if (!table) return;
        const rows  = table.querySelectorAll('tbody tr');
        const q     = query.toLowerCase();

        rows.forEach(function (row) {
            const text = row.textContent.toLowerCase();
            row.style.display = text.includes(q) ? '' : 'none';
        });
    }
});