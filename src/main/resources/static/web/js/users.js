document.addEventListener('DOMContentLoaded', () => {
    const state = {page: 0, size: 10, sort: 'createdAt,desc', totalPages: 0};
    const usersById = new Map();

    const body = document.getElementById('usersBody');
    const pageInfo = document.getElementById('pageInfo');
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    const form = document.getElementById('userForm');
    const formTitle = document.getElementById('formTitle');
    const userId = document.getElementById('userId');
    const email = document.getElementById('email');
    const username = document.getElementById('username');
    const password = document.getElementById('password');
    const passwordRow = document.getElementById('passwordRow');
    const submitBtn = document.getElementById('submitBtn');
    const cancelEditBtn = document.getElementById('cancelEditBtn');

    async function load() {
        body.innerHTML = '<tr><td colspan="5" class="empty">Загрузка…</td></tr>';
        try {
            const page = await api(`/api/users?page=${state.page}&size=${state.size}&sort=${encodeURIComponent(state.sort)}`);
            state.totalPages = page.totalPages || 0;
            usersById.clear();
            (page.content || []).forEach(user => usersById.set(String(user.id), user));

            if (!page.content || page.content.length === 0) {
                body.innerHTML = '<tr><td colspan="5" class="empty">Пользователей пока нет</td></tr>';
            } else {
                body.innerHTML = page.content.map(user => `
                    <tr>
                        <td>${user.id}</td>
                        <td>${escapeHtml(user.username)}</td>
                        <td>${escapeHtml(user.email)}</td>
                        <td>${formatDate(user.createdAt)}</td>
                        <td>
                            <div class="row-actions">
                                <button class="btn ghost small" data-action="edit" data-id="${user.id}">Изменить</button>
                                <button class="btn danger small" data-action="delete" data-id="${user.id}">Удалить</button>
                            </div>
                        </td>
                    </tr>`).join('');
            }

            const total = page.totalElements ?? 0;
            pageInfo.textContent = total === 0
                ? 'Нет записей'
                : `Страница ${(page.number ?? 0) + 1} из ${Math.max(page.totalPages ?? 1, 1)} · всего ${total}`;
            prevBtn.disabled = state.page <= 0;
            nextBtn.disabled = state.page >= state.totalPages - 1;
        } catch (error) {
            body.innerHTML = `<tr><td colspan="5" class="empty">Ошибка: ${escapeHtml(error.message)}</td></tr>`;
            toast(error.message, 'error');
        }
    }

    function resetForm() {
        form.reset();
        userId.value = '';
        formTitle.textContent = 'Новый пользователь';
        submitBtn.textContent = 'Создать';
        cancelEditBtn.hidden = true;
        passwordRow.hidden = false;
        password.required = true;
    }

    function startEdit(user) {
        userId.value = user.id;
        email.value = user.email;
        username.value = user.username;
        password.value = '';
        formTitle.textContent = 'Редактирование #' + user.id;
        submitBtn.textContent = 'Сохранить';
        cancelEditBtn.hidden = false;
        passwordRow.hidden = true;
        password.required = false;
        window.scrollTo({top: 0, behavior: 'smooth'});
    }

    form.addEventListener('submit', async event => {
        event.preventDefault();
        submitBtn.disabled = true;
        try {
            if (userId.value) {
                await api('/api/users/' + userId.value, {
                    method: 'PUT',
                    body: {email: email.value.trim(), username: username.value.trim()}
                });
                toast('Пользователь обновлён', 'success');
            } else {
                if (!password.value) {
                    throw new Error('Укажите пароль');
                }
                await api('/api/users', {
                    method: 'POST',
                    body: {
                        email: email.value.trim(),
                        username: username.value.trim(),
                        password: password.value
                    }
                });
                toast('Пользователь создан', 'success');
                state.page = 0;
            }
            resetForm();
            await load();
        } catch (error) {
            toast(error.message, 'error');
        } finally {
            submitBtn.disabled = false;
        }
    });

    body.addEventListener('click', async event => {
        const button = event.target.closest('button[data-action]');
        if (!button) return;
        const id = button.dataset.id;

        if (button.dataset.action === 'edit') {
            const user = usersById.get(id);
            if (user) startEdit(user);
            return;
        }

        if (button.dataset.action === 'delete') {
            const user = usersById.get(id);
            if (!confirm(`Удалить пользователя «${user?.username ?? id}»?`)) return;
            try {
                await api('/api/users/' + id, {method: 'DELETE'});
                toast('Пользователь удалён', 'success');
                if (state.page > 0 && usersById.size === 1) state.page--;
                await load();
            } catch (error) {
                toast(error.message, 'error');
            }
        }
    });

    document.getElementById('resetFormBtn').addEventListener('click', resetForm);
    cancelEditBtn.addEventListener('click', resetForm);

    document.getElementById('sortSelect').addEventListener('change', event => {
        state.sort = event.target.value;
        state.page = 0;
        load();
    });
    document.getElementById('sizeSelect').addEventListener('change', event => {
        state.size = Number(event.target.value);
        state.page = 0;
        load();
    });
    prevBtn.addEventListener('click', () => {
        if (state.page > 0) {
            state.page--;
            load();
        }
    });
    nextBtn.addEventListener('click', () => {
        if (state.page < state.totalPages - 1) {
            state.page++;
            load();
        }
    });

    resetForm();
    load();
});
