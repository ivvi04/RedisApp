(function () {
    'use strict';

    const form = document.getElementById('registerForm');
    if (!form) return;

    const errorBox = document.getElementById('registerError');
    const submitButton = document.getElementById('registerSubmit');

    function showError(message) {
        errorBox.textContent = message;
        errorBox.hidden = false;
    }

    function hideError() {
        errorBox.hidden = true;
        errorBox.textContent = '';
    }

    form.addEventListener('submit', async function (event) {
        event.preventDefault();
        hideError();

        const payload = {
            email: form.email.value.trim(),
            username: form.username.value.trim(),
            password: form.password.value,
            confirmPassword: form.confirmPassword.value
        };

        if (!payload.email || !payload.username || !payload.password) {
            showError('Заполните все поля');
            return;
        }
        if (payload.password.length < 6) {
            showError('Пароль должен содержать минимум 6 символов');
            return;
        }
        if (payload.password !== payload.confirmPassword) {
            showError('Пароли не совпадают');
            return;
        }

        submitButton.disabled = true;
        submitButton.textContent = 'Создаём аккаунт…';

        try {
            await api('/api/auth/register', {
                method: 'POST',
                body: payload
            });
            location.href = '/login?registered';
        } catch (error) {
            showError(error.message || 'Не удалось зарегистрироваться');
            submitButton.disabled = false;
            submitButton.textContent = 'Создать аккаунт';
        }
    });
})();
