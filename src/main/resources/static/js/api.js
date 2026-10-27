const TOKEN_KEY = 'fizjobook.token';

export const auth = {
    get token() {
        return localStorage.getItem(TOKEN_KEY);
    },
    save(token) {
        localStorage.setItem(TOKEN_KEY, token);
    },
    clear() {
        localStorage.removeItem(TOKEN_KEY);
    },
};

export class ApiError extends Error {
    constructor(status, message) {
        super(message);
        this.status = status;
    }
}

export async function api(method, path, body, { anonymous = false } = {}) {
    const headers = {};
    if (body !== undefined) {
        headers['Content-Type'] = 'application/json';
    }
    if (!anonymous && auth.token) {
        headers['Authorization'] = `Bearer ${auth.token}`;
    }

    const response = await fetch(path, {
        method,
        headers,
        body: body === undefined ? undefined : JSON.stringify(body),
    });

    const text = await response.text();
    const data = text ? JSON.parse(text) : null;

    if (!response.ok) {
        throw new ApiError(response.status, errorMessage(response.status, data));
    }
    return data;
}

function errorMessage(status, problem) {
    if (!problem) {
        return `Błąd ${status}`;
    }
    let message = problem.detail ?? problem.title ?? `Błąd ${status}`;
    if (problem.errors) {
        const fields = Object.entries(problem.errors).map(([field, error]) => `${field}: ${error}`);
        message += '\n' + fields.join('\n');
    }
    return message;
}