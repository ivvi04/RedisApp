document.addEventListener('DOMContentLoaded', () => {
    const main = document.querySelector('main[data-chat-id]');
    const chatId = Number(main.dataset.chatId);

    const usersById = new Map();
    let chat = null;
    let activeQuery = '';

    const chatTitle = document.getElementById('chatTitle');
    const chatSubtitle = document.getElementById('chatSubtitle');
    const currentUser = document.getElementById('currentUser');
    const membersCount = document.getElementById('membersCount');
    const membersList = document.getElementById('membersList');
    const memberForm = document.getElementById('memberForm');
    const memberToAdd = document.getElementById('memberToAdd');
    const messagesBox = document.getElementById('messages');
    const messageForm = document.getElementById('messageForm');
    const messageContent = document.getElementById('messageContent');
    const searchForm = document.getElementById('searchForm');
    const searchQuery = document.getElementById('searchQuery');
    const clearSearchBtn = document.getElementById('clearSearchBtn');

    function currentUserId() {
        return Number(currentUser.value) || null;
    }

    function userName(id) {
        return usersById.get(String(id))?.username || ('Пользователь #' + id);
    }

    function statusBadge(status) {
        const value = status || 'sent';
        return `<span class="badge status-${escapeHtml(value)}">${escapeHtml(value)}</span>`;
    }

    async function loadUsers() {
        const page = await api('/api/users?page=0&size=100&sort=username,asc');
        const users = page.content || [];
        usersById.clear();
        users.forEach(user => usersById.set(String(user.id), user));

        const saved = localStorage.getItem('redisapp.currentUser');
        currentUser.innerHTML = '<option value="">Выберите пользователя</option>' +
            users.map(user => `<option value="${user.id}">#${user.id} · ${escapeHtml(user.username)}</option>`).join('');
        if (saved && usersById.has(saved)) {
            currentUser.value = saved;
        } else if (users.length > 0) {
            currentUser.value = String(users[0].id);
            localStorage.setItem('redisapp.currentUser', currentUser.value);
        }

        memberToAdd.innerHTML = users.map(user =>
            `<option value="${user.id}">#${user.id} · ${escapeHtml(user.username)}</option>`).join('');
    }

    async function loadChat() {
        chat = await api('/api/chats/' + chatId);
        rememberChat(chat);

        chatTitle.textContent = chat.name || ('Чат #' + chat.id);
        chatSubtitle.textContent = `#${chat.id} · ${chat.type} · создан ${formatDate(chat.createdAt)}`;
        document.getElementById('deleteChatBtn').disabled = chat.type === 'deleted';

        const memberIds = Array.from(chat.memberIds || []);
        membersCount.textContent = memberIds.length;

        if (memberIds.length === 0) {
            membersList.innerHTML = '<li class="muted small">Нет участников</li>';
            return;
        }

        membersList.innerHTML = memberIds.map(id => `
            <li>
                <span class="member-name">
                    <span class="avatar">${escapeHtml(initials(userName(id)))}</span>
                    <span>${escapeHtml(userName(id))} <span class="muted small">#${id}</span></span>
                </span>
                <button class="btn danger small" type="button" data-remove-member="${id}">Убрать</button>
            </li>`).join('');
    }

    async function loadMessages() {
        messagesBox.innerHTML = '<p class="empty">Загрузка…</p>';
        try {
            const url = activeQuery
                ? `/api/messages/chat/${chatId}/search?query=${encodeURIComponent(activeQuery)}`
                : `/api/messages/chat/${chatId}`;
            const messages = await api(url);
            renderMessages(messages || []);
        } catch (error) {
            messagesBox.innerHTML = `<p class="empty">Ошибка: ${escapeHtml(error.message)}</p>`;
            toast(error.message, 'error');
        }
    }

    function renderMessages(messages) {
        if (messages.length === 0) {
            messagesBox.innerHTML = `<p class="empty">${activeQuery ? 'Ничего не найдено' : 'Сообщений пока нет'}</p>`;
            return;
        }
        const me = currentUserId();
        messagesBox.innerHTML = messages.map(message => {
            const own = me !== null && Number(message.senderId) === me;
            const canRead = message.status !== 'read' && message.status !== 'deleted';
            const canDelete = message.status !== 'deleted';
            return `
                <article class="message ${own ? 'own' : ''}">
                    <div class="message-head">
                        <strong>${escapeHtml(userName(message.senderId))}</strong>
                        <span>${formatDate(message.timestamp)}</span>
                        ${statusBadge(message.status)}
                    </div>
                    <div class="message-body">${escapeHtml(message.content)}</div>
                    <div class="message-actions">
                        ${canRead ? `<button class="btn ghost small" data-read="${message.id}">Прочитано</button>` : ''}
                        ${canDelete ? `<button class="btn danger small" data-delete-message="${message.id}">Удалить</button>` : ''}
                    </div>
                </article>`;
        }).join('');
        messagesBox.scrollTop = messagesBox.scrollHeight;
    }

    memberForm.addEventListener('submit', async event => {
        event.preventDefault();
        const userId = Number(memberToAdd.value);
        if (!userId) return;
        try {
            await api('/api/chats/members', {
                method: 'PUT',
                body: {chatId, userId, action: 'ADD'}
            });
            toast('Участник добавлен', 'success');
            await loadChat();
        } catch (error) {
            toast(error.message, 'error');
        }
    });

    membersList.addEventListener('click', async event => {
        const button = event.target.closest('button[data-remove-member]');
        if (!button) return;
        const userId = Number(button.dataset.removeMember);
        try {
            await api('/api/chats/members', {
                method: 'PUT',
                body: {chatId, userId, action: 'REMOVE'}
            });
            toast('Участник удалён', 'success');
            await loadChat();
        } catch (error) {
            toast(error.message, 'error');
        }
    });

    document.getElementById('deleteChatBtn').addEventListener('click', async () => {
        if (!confirm('Удалить чат? Он будет помечен как deleted.')) return;
        try {
            await api('/api/chats/' + chatId, {method: 'DELETE'});
            toast('Чат удалён', 'success');
            location.href = '/chats';
        } catch (error) {
            toast(error.message, 'error');
        }
    });

    currentUser.addEventListener('change', () => {
        if (currentUser.value) {
            localStorage.setItem('redisapp.currentUser', currentUser.value);
        }
        loadMessages();
    });

    searchForm.addEventListener('submit', event => {
        event.preventDefault();
        activeQuery = searchQuery.value.trim();
        clearSearchBtn.hidden = !activeQuery;
        loadMessages();
    });

    clearSearchBtn.addEventListener('click', () => {
        searchQuery.value = '';
        activeQuery = '';
        clearSearchBtn.hidden = true;
        loadMessages();
    });

    messageForm.addEventListener('submit', async event => {
        event.preventDefault();
        const senderId = currentUserId();
        if (!senderId) {
            toast('Выберите пользователя', 'error');
            return;
        }
        const content = messageContent.value.trim();
        if (!content) return;

        const button = messageForm.querySelector('button[type="submit"]');
        button.disabled = true;
        try {
            await api('/api/messages', {
                method: 'POST',
                body: {chatId, senderId, content}
            });
            messageContent.value = '';
            searchQuery.value = '';
            activeQuery = '';
            clearSearchBtn.hidden = true;
            await loadMessages();
        } catch (error) {
            toast(error.message, 'error');
        } finally {
            button.disabled = false;
        }
    });

    messagesBox.addEventListener('click', async event => {
        const readButton = event.target.closest('button[data-read]');
        if (readButton) {
            const userId = currentUserId();
            if (!userId) {
                toast('Выберите пользователя', 'error');
                return;
            }
            try {
                await api(`/api/messages/${readButton.dataset.read}/read?userId=${userId}`, {method: 'PUT'});
                await loadMessages();
            } catch (error) {
                toast(error.message, 'error');
            }
            return;
        }

        const deleteButton = event.target.closest('button[data-delete-message]');
        if (deleteButton) {
            if (!confirm('Удалить сообщение?')) return;
            try {
                await api('/api/messages/' + deleteButton.dataset.deleteMessage, {method: 'DELETE'});
                await loadMessages();
            } catch (error) {
                toast(error.message, 'error');
            }
        }
    });

    (async () => {
        try {
            await loadUsers();
            await loadChat();
            await loadMessages();
        } catch (error) {
            toast(error.message, 'error');
            messagesBox.innerHTML = `<p class="empty">Ошибка: ${escapeHtml(error.message)}</p>`;
        }
    })();
});
