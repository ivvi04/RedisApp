(function () {
    'use strict';

    async function api(url, options = {}) {
        const opts = {...options};
        const headers = new Headers(opts.headers || {});
        if (opts.body && typeof opts.body !== 'string') {
            opts.body = JSON.stringify(opts.body);
            headers.set('Content-Type', 'application/json');
        }
        headers.set('Accept', 'application/json');
        opts.headers = headers;

        const response = await fetch(url, opts);
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

    window.api = api;
    window.toast = toast;
    window.escapeHtml = escapeHtml;
    window.formatDate = formatDate;
    window.initials = initials;
    window.userIds = userIds;

    window.rememberChat = function (chat) {
        if (!chat || !chat.id) return;
        const key = 'redisapp.recentChats';
        let list = [];
        try {
            list = JSON.parse(localStorage.getItem(key) || '[]');
        } catch (e) {
            list = [];
        }
        list = list.filter(item => item.id !== chat.id);
        list.unshift({id: chat.id, name: chat.name, type: chat.type});
        localStorage.setItem(key, JSON.stringify(list.slice(0, 10)));
    };

    window.getRecentChats = function () {
        try {
            return JSON.parse(localStorage.getItem('redisapp.recentChats') || '[]');
        } catch (e) {
            return [];
        }
    };
})();
