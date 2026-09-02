/* Shared UI interactions across every page. */
(function () {
    'use strict';

    /* ------------------------------------------------------------------
       Alerts: close button + auto-dismiss
       ------------------------------------------------------------------ */
    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('[data-alert-close]').forEach(function (btn) {
            btn.addEventListener('click', function () {
                var alertEl = btn.closest('.alert');
                if (alertEl) alertEl.remove();
            });
        });

        // Auto-dismiss info/success alerts after a short delay.
        document.querySelectorAll('.alert--info, .alert--success').forEach(function (alertEl) {
            setTimeout(function () { if (alertEl.parentNode) alertEl.remove(); }, 4500);
        });
    });

    /* ------------------------------------------------------------------
       Toasts
       ------------------------------------------------------------------ */
    function toastRegion() {
        var region = document.querySelector('.toast-region');
        if (!region) {
            region = document.createElement('div');
            region.className = 'toast-region';
            region.setAttribute('aria-live', 'polite');
            document.body.appendChild(region);
        }
        return region;
    }

    window.showToast = function (message, type) {
        var toast = document.createElement('div');
        toast.className = 'toast' + (type ? ' toast--' + type : '');
        toast.textContent = message;
        toastRegion().appendChild(toast);
        setTimeout(function () {
            toast.style.transition = 'opacity 0.3s ease';
            toast.style.opacity = '0';
            setTimeout(function () { toast.remove(); }, 320);
        }, 3500);
    };

    /* ------------------------------------------------------------------
       Confirmation modal (replaces window.confirm for data-confirm forms)
       ------------------------------------------------------------------ */
    var confirmModal = null;
    var confirmOk = null;
    var lastFocused = null;

    function buildConfirmModal() {
        var wrap = document.createElement('div');
        wrap.innerHTML =
            '<div class="overlay" data-confirm-overlay></div>' +
            '<div class="modal" role="dialog" aria-modal="true" aria-labelledby="confirmModalTitle" tabindex="-1">' +
            '  <div class="modal__body">' +
            '    <h2 class="modal__title" id="confirmModalTitle">Are you sure?</h2>' +
            '    <p class="modal__text" id="confirmModalText"></p>' +
            '    <div class="modal__actions">' +
            '      <button type="button" class="btn btn-secondary" data-confirm-cancel>Cancel</button>' +
            '      <button type="button" class="btn btn-danger" data-confirm-ok>Continue</button>' +
            '    </div>' +
            '  </div>' +
            '</div>';
        document.body.appendChild(wrap);

        confirmModal = wrap.querySelector('.modal');
        var overlay = wrap.querySelector('[data-confirm-overlay]');

        function close() {
            closeConfirm();
        }
        wrap.querySelector('[data-confirm-cancel]').addEventListener('click', close);
        overlay.addEventListener('click', close);
        wrap.querySelector('[data-confirm-ok]').addEventListener('click', function () {
            var fn = confirmOk;
            closeConfirm();
            if (fn) fn();
        });

        document.addEventListener('keydown', function (e) {
            if (!confirmModal.classList.contains('is-open')) return;
            if (e.key === 'Escape') { e.preventDefault(); closeConfirm(); }
            if (e.key === 'Tab') {
                var focusables = confirmModal.querySelectorAll('button');
                var first = focusables[0], last = focusables[focusables.length - 1];
                if (e.shiftKey && document.activeElement === first) { last.focus(); e.preventDefault(); }
                else if (!e.shiftKey && document.activeElement === last) { first.focus(); e.preventDefault(); }
            }
        });
    }

    function closeConfirm() {
        if (!confirmModal) return;
        confirmModal.classList.remove('is-open');
        var overlay = document.querySelector('[data-confirm-overlay]');
        if (overlay) overlay.style.display = 'none';
        if (lastFocused) lastFocused.focus();
        lastFocused = null;
    }

    function openConfirm(message, onOk) {
        if (!confirmModal) buildConfirmModal();
        confirmOk = onOk;
        lastFocused = document.activeElement;
        document.querySelector('#confirmModalText').textContent = message;
        var overlay = document.querySelector('[data-confirm-overlay]');
        if (overlay) overlay.style.display = 'block';
        confirmModal.classList.add('is-open');
        confirmModal.querySelector('[data-confirm-cancel]').focus();
    }

    document.addEventListener('click', function (e) {
        var submit = e.target.closest ? e.target.closest('button[type="submit"], input[type="submit"]') : null;
        if (!submit || !submit.form) return;
        if (submit.form.hasAttribute('data-confirm')) {
            e.preventDefault();
            var message = submit.form.getAttribute('data-confirm');
            openConfirm(message, function () {
                submit.form.removeAttribute('data-confirm');
                submit.form.submit();
            });
        }
    });

    /* Backwards-compatible helper used by legacy inline handlers. */
    window.confirmDelete = function (label) {
        var result = false;
        openConfirm('Are you sure you want to delete ' + label + '? This cannot be undone.', function () {
            result = true;
        });
        return result;
    };

    /* ------------------------------------------------------------------
       Mobile sidebar
       ------------------------------------------------------------------ */
    var shell = document.querySelector('.app-shell');

    function setSidebar(open) {
        if (!shell) return;
        shell.classList.toggle('shell-sidebar-open', open);
        var btn = document.querySelector('[data-sidebar-toggle]');
        if (btn) {
            btn.setAttribute('aria-expanded', open ? 'true' : 'false');
        }
        document.body.style.overflow = open ? 'hidden' : '';
    }

    document.addEventListener('DOMContentLoaded', function () {
        var btn = document.querySelector('[data-sidebar-toggle]');
        if (btn) btn.addEventListener('click', function () {
            setSidebar(!shell.classList.contains('shell-sidebar-open'));
        });

        // Close the drawer when an inner link is chosen on mobile.
        document.querySelectorAll('.sidebar a').forEach(function (link) {
            link.addEventListener('click', function () { setSidebar(false); });
        });

        // Close drawer on escape or resize up to desktop.
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && shell.classList.contains('shell-sidebar-open')) setSidebar(false);
        });
        var media = window.matchMedia('(min-width: 768px)');
        media.addEventListener('change', function (m) { if (m.matches) setSidebar(false); });
    });

    /* ------------------------------------------------------------------
       Dropdown (user menu)
       ------------------------------------------------------------------ */
    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('[data-dropdown]').forEach(function (dropdown) {
            var toggle = dropdown.querySelector('[data-dropdown-toggle]');
            if (!toggle) return;
            toggle.addEventListener('click', function (e) {
                e.stopPropagation();
                var open = dropdown.classList.toggle('is-open');
                toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
            });
        });

        document.addEventListener('click', function (e) {
            document.querySelectorAll('.dropdown.is-open').forEach(function (dropdown) {
                if (!dropdown.contains(e.target)) {
                    dropdown.classList.remove('is-open');
                    var t = dropdown.querySelector('[data-dropdown-toggle]');
                    if (t) t.setAttribute('aria-expanded', 'false');
                }
            });
        });

        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape') {
                document.querySelectorAll('.dropdown.is-open').forEach(function (dropdown) {
                    dropdown.classList.remove('is-open');
                    var t = dropdown.querySelector('[data-dropdown-toggle]');
                    if (t) t.setAttribute('aria-expanded', 'false');
                });
            }
        });
    });
})();