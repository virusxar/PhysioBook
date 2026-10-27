import { api } from '../api.js';
import { DAY_NAMES, asList, escapeHtml, formToObject, hideError, personName, showError } from '../ui.js';

export function renderAdminView(container) {
    container.innerHTML = `
        <section class="card" id="physiotherapists">
            <h2>Fizjoterapeuci</h2>
            <table>
                <thead><tr><th>Imię i nazwisko</th><th>Specjalizacja</th><th></th></tr></thead>
                <tbody></tbody>
            </table>
            <p class="error" hidden></p>
        </section>

        <section class="card" id="schedule-editor" hidden></section>

        <section class="card" id="new-physiotherapist">
            <h2>Nowy fizjoterapeuta</h2>
            <form>
                <div class="row">
                    <label>Imię <input name="firstName" required maxlength="100"></label>
                    <label>Nazwisko <input name="lastName" required maxlength="100"></label>
                </div>
                <div class="row">
                    <label>E-mail (login) <input name="email" type="email" required></label>
                    <label>Hasło startowe (min. 8 znaków) <input name="password" type="password" required minlength="8" autocomplete="new-password"></label>
                </div>
                <div class="row">
                    <label>Specjalizacja <input name="specialization" maxlength="150"></label>
                    <label>Numer prawa wykonywania zawodu <input name="licenseNumber" maxlength="30"></label>
                </div>
                <label>Telefon <input name="phone" maxlength="30"></label>
                <button type="submit">Dodaj fizjoterapeutę</button>
                <p class="error" hidden></p>
                <p class="success" hidden></p>
            </form>
        </section>

        <section class="card" id="patients">
            <h2>Pacjenci</h2>
            <table>
                <thead><tr><th>Imię i nazwisko</th><th>E-mail</th><th>Telefon</th></tr></thead>
                <tbody></tbody>
            </table>
            <p class="error" hidden></p>
        </section>
    `;

    const physioSection = container.querySelector('#physiotherapists');
    const scheduleSection = container.querySelector('#schedule-editor');

    const loadPhysiotherapists = () => renderPhysiotherapists(physioSection, (physio) => {
        renderScheduleEditor(scheduleSection, physio);
    });

    setUpNewPhysiotherapistForm(container.querySelector('#new-physiotherapist form'), loadPhysiotherapists);
    loadPhysiotherapists();
    renderPatients(container.querySelector('#patients'));
}

async function renderPhysiotherapists(section, onEditSchedule) {
    const body = section.querySelector('tbody');
    const error = section.querySelector('.error');
    hideError(error);

    let physiotherapists;
    try {
        physiotherapists = asList(await api('GET', '/api/physiotherapists'));
    } catch (e) {
        showError(error, e);
        return;
    }

    if (physiotherapists.length === 0) {
        body.innerHTML = '<tr><td colspan="3" class="muted">Brak fizjoterapeutów.</td></tr>';
        return;
    }

    body.innerHTML = physiotherapists.map((p, index) => `
        <tr>
            <td>${escapeHtml(personName(p))}</td>
            <td>${escapeHtml(p.specialization ?? '')}</td>
            <td><button class="secondary" data-index="${index}">Grafik</button></td>
        </tr>
    `).join('');

    body.querySelectorAll('button[data-index]').forEach((button) => {
        button.addEventListener('click', () => onEditSchedule(physiotherapists[button.dataset.index]));
    });
}

function setUpNewPhysiotherapistForm(form, onCreated) {
    const error = form.querySelector('.error');
    const success = form.querySelector('.success');

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        hideError(error);
        success.hidden = true;
        try {
            const created = await api('POST', '/api/physiotherapists', formToObject(form));
            form.reset();
            success.textContent = `Dodano: ${personName(created)}. Może się już zalogować.`;
            success.hidden = false;
            onCreated();
        } catch (e) {
            showError(error, e);
        }
    });
}

async function renderScheduleEditor(section, physio) {
    section.hidden = false;
    section.innerHTML = '<p class="muted">Wczytywanie…</p>';

    let entries;
    try {
        entries = await api('GET', `/api/physiotherapists/${physio.id}/availability`);
    } catch (e) {
        section.innerHTML = '<p class="error"></p>';
        showError(section.querySelector('.error'), e);
        return;
    }

    section.innerHTML = `
        <div class="card-header">
            <h2>Grafik: ${escapeHtml(personName(physio))}</h2>
            <button class="secondary" id="close-schedule">Zamknij</button>
        </div>
        <p class="muted">Każdy wiersz to jeden przedział godzin pracy. W jednym dniu może być kilka przedziałów, np. przed i po przerwie.</p>
        <form>
            <div class="rows"></div>
            <div class="form-actions">
                <button type="button" class="secondary" id="add-row">+ Dodaj przedział</button>
                <button type="submit">Zapisz grafik</button>
            </div>
            <p class="error" hidden></p>
            <p class="success" hidden></p>
        </form>
    `;

    const form = section.querySelector('form');
    const rows = form.querySelector('.rows');
    const error = form.querySelector('.error');
    const success = form.querySelector('.success');

    entries.forEach((entry) => rows.append(scheduleRow(entry)));
    if (entries.length === 0) {
        rows.append(scheduleRow());
    }

    section.querySelector('#close-schedule').addEventListener('click', () => {
        section.hidden = true;
    });
    form.querySelector('#add-row').addEventListener('click', () => rows.append(scheduleRow()));

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        hideError(error);
        success.hidden = true;

        const newEntries = [...rows.querySelectorAll('.schedule-row')].map((row) => ({
            dayOfWeek: row.querySelector('[name=dayOfWeek]').value,
            startTime: row.querySelector('[name=startTime]').value,
            endTime: row.querySelector('[name=endTime]').value,
            slotMinutes: Number(row.querySelector('[name=slotMinutes]').value),
        }));

        try {
            await api('PUT', `/api/physiotherapists/${physio.id}/availability`, { entries: newEntries });
            success.textContent = 'Grafik zapisany.';
            success.hidden = false;
        } catch (e) {
            showError(error, e);
        }
    });

    section.scrollIntoView({ behavior: 'smooth' });
}

function scheduleRow(entry = { dayOfWeek: 'MONDAY', startTime: '08:00', endTime: '16:00', slotMinutes: 45 }) {
    const row = document.createElement('div');
    row.className = 'schedule-row';

    const dayOptions = Object.entries(DAY_NAMES)
        .map(([value, label]) => `<option value="${value}" ${value === entry.dayOfWeek ? 'selected' : ''}>${label}</option>`)
        .join('');

    row.innerHTML = `
        <label>Dzień <select name="dayOfWeek">${dayOptions}</select></label>
        <label>Od <input name="startTime" type="time" required value="${entry.startTime.slice(0, 5)}"></label>
        <label>Do <input name="endTime" type="time" required value="${entry.endTime.slice(0, 5)}"></label>
        <label>Wizyta (min) <input name="slotMinutes" type="number" required min="15" max="240" step="5" value="${entry.slotMinutes}"></label>
        <button type="button" class="danger remove">Usuń</button>
    `;
    row.querySelector('.remove').addEventListener('click', () => row.remove());
    return row;
}

async function renderPatients(section) {
    const body = section.querySelector('tbody');
    const error = section.querySelector('.error');

    try {
        const patients = asList(await api('GET', '/api/patients'));
        if (patients.length === 0) {
            body.innerHTML = '<tr><td colspan="3" class="muted">Brak pacjentów.</td></tr>';
            return;
        }
        body.innerHTML = patients.map((p) => `
            <tr>
                <td>${escapeHtml(personName(p))}</td>
                <td>${escapeHtml(p.email ?? '')}</td>
                <td>${escapeHtml(p.phone ?? '')}</td>
            </tr>
        `).join('');
    } catch (e) {
        showError(error, e);
    }
}