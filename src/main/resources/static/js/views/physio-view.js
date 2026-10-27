import { api } from '../api.js';
import {
    APPOINTMENT_STATUS_NAMES,
    NOTE_TYPE_NAMES,
    escapeHtml,
    formatDateTime,
    formatTime,
    hideError,
    showError,
    toIsoDate,
} from '../ui.js';

export function renderPhysioView(container, me) {
    container.innerHTML = `
        <section class="card" id="schedule">
            <div class="card-header">
                <h2>Moje wizyty</h2>
                <div class="actions">
                    <button class="mode" data-mode="day">Wybrany dzień</button>
                    <button class="mode secondary" data-mode="all">Wszystkie</button>
                </div>
            </div>
            <div class="row filters">
                <label class="filter-day">Dzień <input name="date" type="date"></label>
                <label class="checkbox filter-all" hidden><input type="checkbox" name="upcomingOnly" checked> Tylko nadchodzące</label>
            </div>
            <ul class="list"></ul>
            <p class="error" hidden></p>
        </section>

        <section class="card" id="patient" hidden></section>
    `;

    const scheduleSection = container.querySelector('#schedule');
    const patientSection = container.querySelector('#patient');
    const dateInput = scheduleSection.querySelector('[name=date]');
    const upcomingOnlyInput = scheduleSection.querySelector('[name=upcomingOnly]');
    const modeButtons = scheduleSection.querySelectorAll('button.mode');
    dateInput.value = toIsoDate(new Date());

    const filter = { mode: 'day', date: dateInput.value, upcomingOnly: true };

    const loadSchedule = () => renderSchedule(scheduleSection, me, filter, (patientId) => {
        renderPatientCard(patientSection, patientId);
    });

    modeButtons.forEach((button) => {
        button.addEventListener('click', () => {
            filter.mode = button.dataset.mode;
            modeButtons.forEach((b) => b.classList.toggle('secondary', b !== button));
            scheduleSection.querySelector('.filter-day').hidden = filter.mode !== 'day';
            scheduleSection.querySelector('.filter-all').hidden = filter.mode !== 'all';
            loadSchedule();
        });
    });
    dateInput.addEventListener('change', () => {
        filter.date = dateInput.value;
        loadSchedule();
    });
    upcomingOnlyInput.addEventListener('change', () => {
        filter.upcomingOnly = upcomingOnlyInput.checked;
        loadSchedule();
    });

    loadSchedule();
}

async function renderSchedule(section, me, filter, onOpenPatient) {
    const list = section.querySelector('.list');
    const error = section.querySelector('.error');
    hideError(error);

    const query = filter.mode === 'day' ? `?date=${filter.date}` : '';
    let appointments;
    try {
        appointments = await api('GET', `/api/physiotherapists/${me.profileId}/appointments${query}`);
    } catch (e) {
        list.innerHTML = '';
        showError(error, e);
        return;
    }

    if (filter.mode === 'all' && filter.upcomingOnly) {
        const now = new Date();
        appointments = appointments.filter((a) => new Date(a.endTime) > now);
    }

    if (appointments.length === 0) {
        list.innerHTML = `<li class="muted">${filter.mode === 'day' ? 'Brak wizyt w tym dniu.' : 'Brak wizyt.'}</li>`;
        return;
    }

    const when = (a) => filter.mode === 'day'
        ? `${formatTime(a.startTime)}–${formatTime(a.endTime)}`
        : formatDateTime(a.startTime);

    list.innerHTML = appointments.map((a) => `
        <li>
            <div class="list-header">
                <div>
                    <strong>${when(a)}</strong>
                    <span class="badge ${a.status}">${APPOINTMENT_STATUS_NAMES[a.status] ?? a.status}</span>
                    <div>${escapeHtml(a.patientName ?? '')}${a.reason ? ' · ' + escapeHtml(a.reason) : ''}</div>
                    ${a.cancellationReason ? `<div class="muted">Powód odwołania: ${escapeHtml(a.cancellationReason)}</div>` : ''}
                </div>
                <div class="actions">
                    <button class="secondary" data-patient="${a.patientId}">Karta pacjenta</button>
                    ${statusButtons(a)}
                </div>
            </div>
        </li>
    `).join('');

    const reload = () => renderSchedule(section, me, filter, onOpenPatient);

    list.querySelectorAll('button[data-patient]').forEach((button) => {
        button.addEventListener('click', () => onOpenPatient(button.dataset.patient));
    });

    list.querySelectorAll('button[data-action]').forEach((button) => {
        button.addEventListener('click', async () => {
            const { id, action } = button.dataset;
            let body;
            if (action === 'cancel') {
                const reason = prompt('Podaj powód odwołania wizyty:');
                if (!reason || !reason.trim()) {
                    return;
                }
                body = { reason: reason.trim() };
            }
            try {
                await api('PATCH', `/api/appointments/${id}/${action}`, body);
                reload();
            } catch (e) {
                showError(error, e);
            }
        });
    });
}

function statusButtons(appointment) {
    const buttons = [];
    if (appointment.status === 'BOOKED') {
        buttons.push(['confirm', 'Potwierdź', '']);
    }
    if (appointment.status === 'BOOKED' || appointment.status === 'CONFIRMED') {
        buttons.push(['complete', 'Zakończ', '']);
        buttons.push(['no-show', 'Nieobecność', 'secondary']);
        buttons.push(['cancel', 'Odwołaj', 'danger']);
    }
    return buttons
        .map(([action, label, cssClass]) =>
            `<button class="${cssClass}" data-id="${appointment.id}" data-action="${action}">${label}</button>`)
        .join('');
}

async function renderPatientCard(section, patientId) {
    section.hidden = false;
    section.innerHTML = '<p class="muted">Wczytywanie…</p>';

    let history;
    try {
        history = await api('GET', `/api/patients/${patientId}/history`);
    } catch (e) {
        section.innerHTML = '<p class="error"></p>';
        showError(section.querySelector('.error'), e);
        return;
    }

    const typeOptions = Object.entries(NOTE_TYPE_NAMES)
        .map(([value, label]) => `<option value="${value}">${label}</option>`)
        .join('');

    section.innerHTML = `
        <div class="card-header">
            <h2>${escapeHtml(history.patientName)}</h2>
            <button class="secondary" id="close-patient">Zamknij</button>
        </div>

        <h3>Nowa notatka</h3>
        <form id="note-form">
            <label>Typ <select name="type">${typeOptions}</select></label>
            <label>Treść <textarea name="content" rows="4" required maxlength="10000"></textarea></label>
            <label class="checkbox"><input type="checkbox" name="visibleToPatient"> Widoczna dla pacjenta</label>
            <button type="submit">Dodaj notatkę</button>
            <p class="error" hidden></p>
        </form>

        <h3>Historia leczenia</h3>
        <ul class="list">${timelineItems(history.timeline)}</ul>
    `;

    section.querySelector('#close-patient').addEventListener('click', () => {
        section.hidden = true;
    });

    const form = section.querySelector('#note-form');
    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const error = form.querySelector('.error');
        hideError(error);
        try {
            await api('POST', `/api/patients/${patientId}/notes`, {
                type: form.elements.type.value,
                content: form.elements.content.value.trim(),
                visibleToPatient: form.elements.visibleToPatient.checked,
            });
            renderPatientCard(section, patientId);
        } catch (e) {
            showError(error, e);
        }
    });

    section.scrollIntoView({ behavior: 'smooth' });
}

function timelineItems(timeline) {
    if (timeline.length === 0) {
        return '<li class="muted">Brak wpisów w historii leczenia.</li>';
    }
    return timeline.map((entry) => `
        <li>
            <div class="muted">${formatDateTime(entry.at)}</div>
            <strong>${escapeHtml(entry.title)}</strong>
            ${entry.content ? `<div>${escapeHtml(entry.content)}</div>` : ''}
        </li>
    `).join('');
}