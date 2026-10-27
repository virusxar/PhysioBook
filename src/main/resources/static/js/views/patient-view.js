import { api } from '../api.js';
import {
    APPOINTMENT_STATUS_NAMES,
    escapeHtml,
    formatDateTime,
    formatTime,
    hideError,
    showError,
    toIsoDate,
} from '../ui.js';

const CANCELLABLE_STATUSES = ['BOOKED', 'CONFIRMED'];

export function renderPatientView(container, me) {
    container.innerHTML = `
        <section class="card" id="booking">
            <h2>Umów wizytę</h2>
            <div class="row">
                <label>Fizjoterapeuta <select name="physiotherapist"></select></label>
                <label>Dzień <input name="date" type="date"></label>
            </div>
            <div class="slots"></div>
            <form id="booking-form" hidden>
                <p>Wybrany termin: <strong class="selected-slot"></strong></p>
                <label>Powód wizyty (opcjonalnie) <input name="reason" maxlength="500"></label>
                <button type="submit">Zarezerwuj</button>
            </form>
            <p class="error" hidden></p>
            <p class="success" hidden></p>
        </section>

        <section class="card" id="appointments">
            <h2>Moje wizyty</h2>
            <ul class="list"></ul>
            <p class="error" hidden></p>
        </section>

        <section class="card" id="history">
            <h2>Historia leczenia</h2>
            <ul class="list"></ul>
            <p class="error" hidden></p>
        </section>
    `;

    const booking = new BookingSection(container.querySelector('#booking'), me, () => loadAppointments());
    const appointmentsSection = container.querySelector('#appointments');
    const historySection = container.querySelector('#history');

    async function loadAppointments() {
        await renderAppointments(appointmentsSection, me, () => {
            loadAppointments();
            booking.loadSlots();
        });
    }

    booking.init();
    loadAppointments();
    renderHistory(historySection, me);
}

class BookingSection {
    constructor(section, me, onBooked) {
        this.section = section;
        this.me = me;
        this.onBooked = onBooked;
        this.physioSelect = section.querySelector('[name=physiotherapist]');
        this.dateInput = section.querySelector('[name=date]');
        this.slotsBox = section.querySelector('.slots');
        this.form = section.querySelector('#booking-form');
        this.error = section.querySelector('.error');
        this.success = section.querySelector('.success');
        this.selectedSlot = null;
    }

    async init() {
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        this.dateInput.value = toIsoDate(tomorrow);
        this.dateInput.min = toIsoDate(new Date());

        this.physioSelect.addEventListener('change', () => this.loadSlots());
        this.dateInput.addEventListener('change', () => this.loadSlots());
        this.form.addEventListener('submit', (event) => {
            event.preventDefault();
            this.book();
        });

        try {
            const physiotherapists = await api('GET', '/api/physiotherapists');
            this.physioSelect.innerHTML = physiotherapists
                .map((p) => `<option value="${p.id}">${escapeHtml(physioLabel(p))}</option>`)
                .join('');
            await this.loadSlots();
        } catch (e) {
            showError(this.error, e);
        }
    }

    async loadSlots() {
        this.selectSlot(null);
        hideError(this.error);
        const physioId = this.physioSelect.value;
        const date = this.dateInput.value;
        if (!physioId || !date) {
            this.slotsBox.innerHTML = '';
            return;
        }

        try {
            const slots = await api('GET', `/api/physiotherapists/${physioId}/slots?date=${date}`);
            if (slots.length === 0) {
                this.slotsBox.innerHTML = '<p class="muted">Brak wolnych terminów w tym dniu.</p>';
                return;
            }
            this.slotsBox.innerHTML = slots
                .map((slot, index) => `<button type="button" data-index="${index}">${formatTime(slot.start)}</button>`)
                .join('');
            this.slotsBox.querySelectorAll('button').forEach((button) => {
                button.addEventListener('click', () => this.selectSlot(slots[button.dataset.index], button));
            });
        } catch (e) {
            this.slotsBox.innerHTML = '';
            showError(this.error, e);
        }
    }

    selectSlot(slot, button) {
        this.selectedSlot = slot;
        this.slotsBox.querySelectorAll('button').forEach((b) => b.classList.toggle('selected', b === button));
        this.form.hidden = !slot;
        if (slot) {
            this.form.querySelector('.selected-slot').textContent = formatDateTime(slot.start);
            this.success.hidden = true;
        }
    }

    async book() {
        hideError(this.error);
        const slot = this.selectedSlot;
        const reason = this.form.querySelector('[name=reason]').value.trim();
        const durationMinutes = (new Date(slot.end) - new Date(slot.start)) / 60000;

        try {
            await api('POST', '/api/appointments', {
                patientId: this.me.profileId,
                physiotherapistId: Number(this.physioSelect.value),
                startTime: slot.start,
                durationMinutes,
                reason: reason || undefined,
            });
            this.form.reset();
            this.success.textContent = `Zarezerwowano wizytę: ${formatDateTime(slot.start)}`;
            this.success.hidden = false;
            await this.loadSlots();
            this.onBooked();
        } catch (e) {
            showError(this.error, e);
        }
    }
}

async function renderAppointments(section, me, onChanged) {
    const list = section.querySelector('.list');
    const error = section.querySelector('.error');
    hideError(error);

    let appointments;
    try {
        appointments = await api('GET', `/api/patients/${me.profileId}/appointments`);
    } catch (e) {
        showError(error, e);
        return;
    }

    if (appointments.length === 0) {
        list.innerHTML = '<li class="muted">Nie masz jeszcze żadnych wizyt.</li>';
        return;
    }

    const now = new Date();
    list.innerHTML = appointments.map((a) => {
        const canCancel = CANCELLABLE_STATUSES.includes(a.status) && new Date(a.startTime) > now;
        return `
            <li>
                <div class="list-header">
                    <div>
                        <strong>${formatDateTime(a.startTime)}</strong>
                        <span class="badge ${a.status}">${APPOINTMENT_STATUS_NAMES[a.status] ?? a.status}</span>
                        <div class="muted">${escapeHtml(a.physiotherapistName ?? '')}${a.reason ? ' · ' + escapeHtml(a.reason) : ''}</div>
                        ${a.cancellationReason ? `<div class="muted">Powód odwołania: ${escapeHtml(a.cancellationReason)}</div>` : ''}
                    </div>
                    ${canCancel ? `<button class="danger" data-id="${a.id}">Odwołaj</button>` : ''}
                </div>
            </li>
        `;
    }).join('');

    list.querySelectorAll('button[data-id]').forEach((button) => {
        button.addEventListener('click', async () => {
            const reason = prompt('Podaj powód odwołania wizyty:');
            if (!reason || !reason.trim()) {
                return;
            }
            try {
                await api('PATCH', `/api/appointments/${button.dataset.id}/cancel`, { reason: reason.trim() });
                onChanged();
            } catch (e) {
                showError(error, e);
            }
        });
    });
}

async function renderHistory(section, me) {
    const list = section.querySelector('.list');
    const error = section.querySelector('.error');

    try {
        const history = await api('GET', `/api/patients/${me.profileId}/history`);
        if (history.timeline.length === 0) {
            list.innerHTML = '<li class="muted">Brak wpisów w historii leczenia.</li>';
            return;
        }
        list.innerHTML = history.timeline.map((entry) => `
            <li>
                <div class="muted">${formatDateTime(entry.at)}</div>
                <strong>${escapeHtml(entry.title)}</strong>
                ${entry.content ? `<div>${escapeHtml(entry.content)}</div>` : ''}
            </li>
        `).join('');
    } catch (e) {
        showError(error, e);
    }
}

function physioLabel(p) {
    const name = p.fullName ?? `${p.firstName} ${p.lastName}`;
    return p.specialization ? `${name} (${p.specialization})` : name;
}