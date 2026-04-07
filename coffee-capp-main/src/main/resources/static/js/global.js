

function showPage(pageId) {
    // Pulisci schermata
    const currentPage = document.querySelector('.page.active');
    if (currentPage) {
        clearInputs(currentPage.id);
    }

    // Nascondi tutte le pagine
    document.querySelectorAll('.page').forEach(function(p) {
        p.classList.remove('active');
    });

    // Mostra la pagina richiesta
    document.getElementById(pageId).classList.add('active');
}


function clearInputs(pageId) {
    // Input text
    document.querySelectorAll(`#${pageId} input`).forEach(i => i.value = '');

    // Select
    document.querySelectorAll(`#${pageId} select`).forEach(s => s.selectedIndex = 0);

    // Checkbox
    document.querySelectorAll(`#${pageId} input[type="checkbox"]`).forEach(c => c.checked = false);
}


function showAlert(type, message) {
    alert(type + "\n" + message);
}


function getErrorMessage(xhr, defaultMessage) {
    if (xhr.responseXML) {
        const msg = $(xhr.responseXML).find("message").text();
        if (msg) return msg;
    }
    return defaultMessage;
}


// Token CSRF
$.ajaxSetup({
    beforeSend: function(xhr, settings) {
        if (!/^(GET|HEAD|OPTIONS|TRACE)$/i.test(settings.type) && !this.crossDomain) {
            const token = $("meta[name='_csrf']").attr("content");
            const header = $("meta[name='_csrf_header']").attr("content");
            if (token && header) {
                xhr.setRequestHeader(header, token);
            }
        }
    }
});


// Quando il server si riavvia o scade una sessione, si viene sbattuti fuori (macchinetta esclusa)
$(document).ajaxError(function(event, xhr, settings) {
    if (xhr.status === 401 || xhr.status === 403) {
        if (settings.url.includes("/api/login") || settings.url.includes("/auth") ||
            settings.url.endsWith("/me") || settings.url.endsWith("/status")) {
            return;
        }

        if (window.location.pathname.includes("/macchinetta")) {
            return;
        }

        window.location.href = "/access-denied";
    }
});