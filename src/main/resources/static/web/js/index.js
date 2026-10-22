document.addEventListener('DOMContentLoaded', async () => {
    document.getElementById('openChatForm').addEventListener('submit', event => {
        event.preventDefault();
        const chatId = Number(new FormData(event.target).get('chatId'));
        if (chatId > 0) location.href = '/chats/' + chatId;
    });

    try {
        const page = await api('/api/users?page=0&size=5&sort=createdAt,desc');
        document.getElementById('usersCount').textContent = page.totalElements ?? 0;

        const body = document.getElementById('recentUsers');
        if (!page.content || page.content.length === 0) {
            body.innerHTML = '<tr><td colspan="4" class="empty">Пользователей пока нет</td></tr>';
            return;
        }
        body.innerHTML = page.content.map(user => `
            <tr>
                <td>${user.id}</td>
                <td>${escapeHtml(user.username)}</td>
                <td>${escapeHtml(user.email)}</td>
                <td>${formatDate(user.createdAt)}</td>
            </tr>`).join('');
    } catch (error) {
        toast('Не удалось загрузить данные: ' + error.message, 'error');
    }
});
