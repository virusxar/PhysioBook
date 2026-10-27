const CLINIC_TIME_ZONE = 'Europe/Warsaw';

export function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

export function showError(element, error) {
    element.textContent = error.message;
    element.hidden = false;
}

export function hideError(element) {
    element.textContent = '';
    element.hidden = true;
}

export function formToObject(form) {
    const result = {};
    for (const [key, value] of new FormData(form)) {
        if (value !== '') {
            result[key] = value;
        }
    }
    return result;
}

export function formatDateTime(isoString) {
    return new Date(isoString).toLocaleString('pl-PL', {
        timeZone: CLINIC_TIME_ZONE,
        weekday: 'short',
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    });
}

export function formatTime(isoString) {
    return new Date(isoString).toLocaleTimeString('pl-PL', {
        timeZone: CLINIC_TIME_ZONE,
        hour: '2-digit',
        minute: '2-digit',
    });
}

export function toIsoDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

export const APPOINTMENT_STATUS_NAMES = {
    BOOKED: 'Zarezerwowana',
    CONFIRMED: 'Potwierdzona',
    COMPLETED: 'Zakończona',
    CANCELLED: 'Odwołana',
    NO_SHOW: 'Nieobecność',
};

export const NOTE_TYPE_NAMES = {
    INTERVIEW: 'Wywiad',
    EXAMINATION: 'Badanie',
    TREATMENT: 'Zabieg',
    RECOMMENDATION: 'Zalecenia',
    OTHER: 'Inne',
};