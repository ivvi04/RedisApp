document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('chatForm');
    const nameInput = document.getElementById('chatName');
    const typeInput = document.getElementById('chatType');
    const createdBy = document.getElementById('createdBy');
    const membersList = document.getElementById('membersList');
    const submitBtn = form.querySelector('button[type="submit"]');

    document.getElementById('openChatForm').addEventListener('submit', event => {
        event.preventDefault();
        const chatId = Number(new FormData(event.target).get('chatId'));
        if (chatId > 0) location.href = '/chats/' + chatId;
    });

    function renderRecent() {
        const container = document.getElementById('recentChats');
        const chats = getRecentChats();
        if (chats.length === 0) {
            container.innerHTML = '<p class="muted small">Пока нет открытых чатов.</p>';
            return;
        }
        container.innerHTML = chats.map(chat => `
            <div class="chat-item">
                <div>
                    <div class="chat-name">${escapeHtml(chat.name || ('Чат #' + chat.id))}</div>
                    <div class="chat-meta">#${chat.id} · ${escapeHtml(chat.type || '—')}</div>
                </div>
                <a class="btn ghost small" href="/chats/${chat.id}">Открыть</a>
            </div>`).join('');
    }

    document.getElementById('clearRecentBtn').addEventListener('click', () => {
        localStorage.removeItem('redisapp.recentChats');
        renderRecent();
    });

    async function loadUsers() {
        try {
            const page = await api('/api/users?page=0&size=100&sort=username,asc');
            const users = page.content || [];

            if (users.length === 0) {
                createdBy.innerHTML = '<option value="">Нет пользователей</option>';
                membersList.innerHTML = '<p class="muted small">Сначала создайте пользователей.</p>';
                return;
            }

            createdBy.innerHTML = '<option value="">Выберите создателя</option>' +
                users.map(user => `<option value="${user.id}">#${user.id} · ${escapeHtml(user.username)}</option>`).join('');

            membersList.innerHTML = users.map(user => `
                <label>
                    <input type="checkbox" name="memberIds" value="${user.id}">
                    <span>#${user.id} · ${escapeHtml(user.username)} <span class="muted small">${escapeHtml(user.email)}</span></span>
                </label>`).join('');
        } catch (error) {
            createdBy.innerHTML = '<option value="">Ошибка загрузки</option>';
            membersList.innerHTML = `<p class="muted small">${escapeHtml(error.message)}</p>`;
            toast(error.message, 'error');
        }
    }

    form.addEventListener('submit', async event => {
        event.preventDefault();
        const memberIds = Array.from(form.querySelectorAll('input[name="memberIds"]:checked'))
            .map(input => Number(input.value));

        if (!createdBy.value) {
            toast('Выберите создателя чата', 'error');
            return;
        }

        submitBtn.disabled = true;
        try {
            const chat = await api('/api/chats', {
                method: 'POST',
                body: {
                    name: nameInput.value.trim() || null,
                    type: typeInput.value,
                    memberIds,
                    createdById: Number(createdBy.value)
                }
            });
            rememberChat(chat);
            toast('Чат создан', 'success');
            location.href = '/chats/' + chat.id;
        } catch (error) {
            toast(error.message, 'error');
            submitBtn.disabled = false;
        }
    });

    renderRecent();
    loadUsers();
});
