(function () {
    'use strict';

    function getCookie(name) {
        const prefix = name + '=';
        const parts = document.cookie ? document.cookie.split(';') : [];
        for (const part of parts) {
            const value = part.trim();
            if (value.startsWith(prefix)) {
                return decodeURIComponent(value.substring(prefix.length));
            }
        }
        return null;
    }

    function redirectToLogin() {
        if (!location.pathname.startsWith('/login')) {
            location.href = '/login';
        }
    }

    async function api(url, options = {}) {
        const opts = {credentials: 'same-origin', ...options};
        const headers = new Headers(opts.headers || {});
        const method = (opts.method || 'GET').toUpperCase();

        if (opts.body && typeof opts.body !== 'string') {
            opts.body = JSON.stringify(opts.body);
            headers.set('Content-Type', 'application/json');
        }
        headers.set('Accept', 'application/json');

        if (!['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method)) {
            const csrfToken = getCookie('XSRF-TOKEN');
            if (csrfToken) {
                headers.set('X-XSRF-TOKEN', csrfToken);
            }
        }
        opts.headers = headers;

        const response = await fetch(url, opts);

        if (response.status === 401 || response.redirected && response.url.includes('/login')) {
            redirectToLogin();
            throw new Error('Требуется вход в систему');
        }

        const text = await response.text();
        let data = null;
        if (text) {
            try {
                data = JSON.parse(text);
            } catch (e) {
                data = text;
            }
        }
        if (!response.ok) {
            const message = typeof data === 'string' && data
                ? data
                : (data && data.message) || response.statusText || ('HTTP ' + response.status);
            throw new Error(message);
        }
        return data;
    }

    function toast(message, type = 'info') {
        const el = document.getElementById('toast');
        if (!el) return;
        el.textContent = message;
        el.className = 'toast show ' + type;
        clearTimeout(toast._timer);
        toast._timer = setTimeout(() => {
            el.className = 'toast ' + type;
        }, 3200);
    }

    function escapeHtml(value) {
        return String(value ?? '')
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');
    }

    function formatDate(value) {
        if (!value) return '—';
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) return String(value);
        return date.toLocaleString('ru-RU', {
            day: '2-digit', month: '2-digit', year: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });
    }

    function initials(name) {
        const parts = String(name || '?').trim().split(/\s+/);
        return (parts[0][0] + (parts[1]?.[0] || '')).toUpperCase();
    }

    function userIds(value) {
        return String(value || '')
            .split(/[\s,;]+/)
            .map(v => v.trim())
            .filter(Boolean)
            .map(Number)
            .filter(n => Number.isInteger(n) && n > 0);
    }

    let currentUserRequest = null;

    function getCurrentUser(force = false) {
        if (force) {
            currentUserRequest = null;
        }
        if (!currentUserRequest) {
            currentUserRequest = api('/api/auth/login').catch(error => {
                currentUserRequest = null;
                throw error;
            });
        }
        return currentUserRequest;
    }

    window.api = api;
    window.toast = toast;
    window.escapeHtml = escapeHtml;
    window.formatDate = formatDate;
    window.initials = initials;
    window.userIds = userIds;
    window.getCurrentUser = getCurrentUser;
})();
