import { api, auth } from '../api.js';
import { formToObject, hideError, showError } from '../ui.js';

export function renderAuthView(container, onLoggedIn) {
    container.innerHTML = `
        <div class="card narrow">
            <h1>Logowanie</h1>
            <form id="login-form">
                <label>E-mail <input name="email" type="email" required autocomplete="username"></label>
                <label>Hasło <input name="password" type="password" required autocomplete="current-password"></label>
                <button type="submit">Zaloguj</button>
                <p class="error" hidden></p>
            </form>
        </div>

        <div class="card narrow">
            <h2>Nie masz konta?</h2>
            <button class="link-button" id="show-register">Zarejestruj się jako pacjent</button>
            <form id="register-form" hidden>
                <label>Imię <input name="firstName" required></label>
                <label>Nazwisko <input name="lastName" required></label>
                <label>E-mail <input name="email" type="email" required autocomplete="username"></label>
                <label>Hasło (min. 8 znaków) <input name="password" type="password" required minlength="8" autocomplete="new-password"></label>
                <label>Data urodzenia <input name="birthDate" type="date"></label>
                <label>Telefon <input name="phone"></label>
                <button type="submit">Załóż konto</button>
                <p class="error" hidden></p>
            </form>
        </div>
    `;

    const loginForm = container.querySelector('#login-form');
    const registerForm = container.querySelector('#register-form');

    container.querySelector('#show-register').addEventListener('click', () => {
        registerForm.hidden = false;
    });

    loginForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        const error = loginForm.querySelector('.error');
        hideError(error);
        try {
            await logIn(formToObject(loginForm));
            onLoggedIn();
        } catch (e) {
            showError(error, e);
        }
    });

    registerForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        const error = registerForm.querySelector('.error');
        hideError(error);
        const data = formToObject(registerForm);
        try {
            await api('POST', '/api/auth/register', data, { anonymous: true });
            await logIn({ email: data.email, password: data.password });
            onLoggedIn();
        } catch (e) {
            showError(error, e);
        }
    });
}

async function logIn(credentials) {
    const response = await api('POST', '/api/auth/login', credentials, { anonymous: true });
    auth.save(response.accessToken);
}

function renderPanel(me) {
    if (me.role === 'PATIENT') {
        renderPatientView(app, me);
        return;
    }
    app.innerHTML = `
        <div class="card">
            <h1>Witaj!</h1>
            <p>Jesteś zalogowany jako <strong>${ROLE_NAMES[me.role]}</strong>.</p>
            <p class="muted">Id profilu: ${me.profileId ?? 'brak'}</p>
        </div>
    `;
}