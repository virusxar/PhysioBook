import { api, auth } from './api.js';
import { escapeHtml } from './ui.js';
import { renderAuthView } from './views/auth-view.js';
import { renderPatientView } from './views/patient-view.js';
import { renderPhysioView } from './views/physio-view.js';

const app = document.querySelector('#app');
const userBox = document.querySelector('#user-box');

const ROLE_NAMES = {
    PATIENT: 'Pacjent',
    PHYSIOTHERAPIST: 'Fizjoterapeuta',
    ADMIN: 'Administrator',
};

async function start() {
    if (!auth.token) {
        showLoggedOut();
        return;
    }

    let me;
    try {
        me = await api('GET', '/api/auth/me');
    } catch {
        auth.clear();
        showLoggedOut();
        return;
    }

    renderUserBox(me);
    renderPanel(me);
}

function showLoggedOut() {
    userBox.innerHTML = '';
    renderAuthView(app, start);
}

function renderUserBox(me) {
    userBox.innerHTML = `
        <span>${escapeHtml(me.email)} (${ROLE_NAMES[me.role]})</span>
        <button class="secondary" id="logout">Wyloguj</button>
    `;
    userBox.querySelector('#logout').addEventListener('click', () => {
        auth.clear();
        showLoggedOut();
    });
}

function renderPanel(me) {
    if (me.role === 'PATIENT') {
        renderPatientView(app, me);
        return;
    }
    if (me.role === 'PHYSIOTHERAPIST') {
        renderPhysioView(app, me);
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

start();