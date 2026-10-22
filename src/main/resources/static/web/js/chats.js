document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('chatForm');
    const nameInput = document.getElementById('chatName');
    const membersList = document.getElementById('membersList');
    const creatorName = document.getElementById('creatorName');
    const creatorAvatar = document.getElementById('creatorAvatar');
    const submitBtn = form.querySelector('button[type="submit"]');
    const searchForm = document.getElementById('searchChatsForm');
    const searchInput = document.getElementById('chatSearchQuery');
    const resetSearchBtn = document.getElementById('resetSearchBtn');
    const chatsTitle = document.getElementById('chatsTitle');
    const chatsSubtitle = document.getElementById('chatsSubtitle');

    let me = null;

    const openChatForm = document.getElementById('openChatForm');

    if (openChatForm) {
        openChatForm.addEventListener('submit', event => {
            event.preventDefault();
            const chatId = Number(new FormData(event.target).get('chatId'));
            if (chatId > 0) location.href = '/chats/' + chatId;
        });
    }

    function renderChats(chats, emptyText) {
        const container = document.getElementById('myChats');
        if (!chats || chats.length === 0) {
            container.innerHTML = `<p class="muted small">${emptyText}</p>`;
            return;
        }
        container.innerHTML = chats.map(chat => `
            <div class="chat-item">
                <div>
                    <div class="chat-name">${escapeHtml(chat.name || ('Чат #' + chat.id))}</div>
                    <div class="chat-meta">
                        #${chat.id} · ${escapeHtml(chat.type || '—')}
                        · участников: ${(chat.memberIds || []).length}
                        · создан ${formatDate(chat.createdAt)}
                    </div>
                </div>
                <a class="btn ghost small" href="/chats/${chat.id}">Открыть</a>
            </div>`).join('');
    }

    async function loadChats() {
        const container = document.getElementById('myChats');
        chatsTitle.textContent = 'Мои чаты';
        chatsSubtitle.textContent = 'Список загружается с сервера';
        searchInput.value = '';
        resetSearchBtn.hidden = true;
        container.innerHTML = '<p class="muted small">Загрузка…</p>';
        try {
            const chats = await api('/api/chats');
            renderChats(chats, 'У вас пока нет чатов. Создайте первый чат слева.');
        } catch (error) {
            container.innerHTML = `<p class="muted small">${escapeHtml(error.message)}</p>`;
            toast(error.message, 'error');
        }
    }

    async function searchChats(event) {
        event.preventDefault();
        const query = searchInput.value.trim();
        if (!query) return;

        const container = document.getElementById('myChats');
        chatsTitle.textContent = 'Результаты поиска';
        chatsSubtitle.textContent = 'Поиск по названию чата';
        resetSearchBtn.hidden = false;
        container.innerHTML = '<p class="muted small">Загрузка…</p>';
        try {
            const chats = await api('/api/chats/search?query=' + encodeURIComponent(query));
            renderChats(chats, 'Ничего не найдено.');
        } catch (error) {
            container.innerHTML = `<p class="muted small">${escapeHtml(error.message)}</p>`;
            toast(error.message, 'error');
        }
    }

    searchForm.addEventListener('submit', searchChats);
    resetSearchBtn.addEventListener('click', loadChats);

    document.getElementById('refreshChatsBtn').addEventListener('click', loadChats);

    async function loadCurrentUser() {
        me = await getCurrentUser();
        creatorName.textContent = me.username;
        creatorAvatar.textContent = initials(me.username);
    }

    async function loadUsers() {
        try {
            const page = await api('/api/users?page=0&size=100&sort=username,asc');
            const users = page.content || [];
            const available = users.filter(user => !me || user.id !== me.id);

            if (available.length === 0) {
                membersList.innerHTML = '<p class="muted small">Других пользователей пока нет — чат будет создан без дополнительных участников.</p>';
                return;
            }

            membersList.innerHTML = available.map(user => `
                <label>
                    <input type="checkbox" name="memberIds" value="${user.id}">
                    <span>#${user.id} · ${escapeHtml(user.username)} <span class="muted small">${escapeHtml(user.email)}</span></span>
                </label>`).join('');
        } catch (error) {
            membersList.innerHTML = `<p class="muted small">${escapeHtml(error.message)}</p>`;
            toast(error.message, 'error');
        }
    }

    form.addEventListener('submit', async event => {
        event.preventDefault();
        if (!me) {
            toast('Не удалось определить пользователя', 'error');
            return;
        }

        const memberIds = Array.from(form.querySelectorAll('input[name="memberIds"]:checked'))
            .map(input => Number(input.value));

        submitBtn.disabled = true;
        try {
            const chat = await api('/api/chats', {
                method: 'POST',
                body: {
                    name: nameInput.value.trim() || null,
                    memberIds,
                    createdById: me.id
                }
            });
            toast('Чат создан', 'success');
            location.href = '/chats/' + chat.id;
        } catch (error) {
            toast(error.message, 'error');
            submitBtn.disabled = false;
        }
    });

    (async () => {
        try {
            await loadCurrentUser();
            await loadUsers();
            await loadChats();
        } catch (error) {
            toast(error.message, 'error');
            creatorName.textContent = 'Ошибка';
        }
    })();
});
