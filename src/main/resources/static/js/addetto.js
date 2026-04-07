
$(document).ready(function() {
    checkActiveSession();
});


function checkActiveSession() {
    $.get("/api/addetto/me")
        .done(function(xml) {
            const username = $(xml).find("username").text();
            $('#username').text(username);
            showPage('home');
        })
        .fail(function() {
            // console.log("Nessuna sessione attiva rilevata.");
            showPage('login');
        });
}


let currentMachineStatus = "";
let pendingActionType = null;
function handleLogin() {
    const email = $('#login-email').val();
    const password = $('#login-password').val();

    $.post("/api/login", { email: email, password: password, requiredRole: 'ADDETTO'})
        .done(function(xml) {
            const username = $(xml).find("username").text();
            $('#username').text(username);
            showPage('home');
        })
        .fail(function(xhr) {
            const errorMsg = getErrorMessage(xhr, "Login Fallito");
            showAlert("ERRORE", errorMsg);
        });
}


function showStatusSelectedMachine() {

    const selectedId = $('#machine-id').val().trim().toUpperCase();

    if (!selectedId) {
        showAlert("ATTENZIONE", 'Inserisci un ID macchina');
        return;
    }

    const pattern = /^[A-Z]{2}[0-9]{3}[A-Z]{2}$/;
    if (!pattern.test(selectedId)) {
        showAlert("ATTENZIONE", 'Formato ID non valido. Usa il formato: 2 lettere maiuscole, 3 numeri, 2 lettere maiuscole (es. FE716XW)');
        return;
    }

    $.get('/api/addetto/machinesxml', function (xml) {
        const $machines = $(xml).find('glc\\:machine');

        let found = false;

        $machines.each(function() {
            const $currentMachine = $(this);
            const id = $currentMachine.find('glc\\:machine-id').text().trim().toUpperCase();

            if (id === selectedId) {
                found = true;
                fillStatoMacchinaHTML($currentMachine);
                return false;
            }
        });

        if (!found) {
            showAlert("INFO", 'ID macchina non presente nel database');
        }

    }).fail(function (xhr) {
        const errorMsg = getErrorMessage(xhr, "Errore Server");
        showAlert("ERRORE", errorMsg);
    });
}


function fillStatoMacchinaHTML($machineData) {

    $('#actions-section').removeClass('hidden');
    $('#machine-status').removeClass('hidden');

    // Imposto le intestazioni
    const machineId = $machineData.find('glc\\:machine-id').text().trim();
    $('#current-machine-id').text(machineId);
    const machineStatus = $machineData.find('glc\\:machine-status').text().trim();
    currentMachineStatus = machineStatus;

    const $state = $('#machine-state');
    $state.removeClass('active maintenance offline error');
    $state.text(machineStatus);
    $state.addClass(machineStatus.toLowerCase());

    const $btnChange = $('#btn-change-status');
    const isEditable = ['ACTIVE', 'MAINTENANCE'].includes(machineStatus);
    $btnChange.prop('disabled', !isEditable);


    // Aggiungo le forniture, rimuovendo eventuale roba precedentemente caricata
    const $suppliesContainer = $('#supplies-container');
    $suppliesContainer.empty();

    const $supplies = $machineData.find('glc\\:supply');

    $supplies.each(function() {
        const $supply = $(this);

        const name = $supply.find('glc\\:name').text().trim();
        const levelPercent = parseInt($supply.find('glc\\:level-percent').text().trim(), 10);
        const warningThreshold = parseInt($supply.find('glc\\:warning-threshold').text().trim(), 10);

        const levelClass = levelPercent >= 70 ? 'high'
            : levelPercent >= warningThreshold ? 'medium'
                : 'low';

        const $item = $('<div>').addClass('supply-item');

        const $nameSpan = $('<span>')
            .addClass('supply-name')
            .text(name);

        const $levelSpan = $('<span>')
            .addClass('supply-level ' + levelClass)
            .text(levelPercent + '%');

        const $progressBar = $('<div>').addClass('progress-bar');
        const $fill = $('<div>')
            .addClass('fill ' + levelClass)
            .css('width', levelPercent + '%');

        // Impacchetto e incapsulo
        $progressBar.append($fill);
        $item.append($nameSpan, $levelSpan, $progressBar);
        $suppliesContainer.append($item);
    });

    // Aggiungo eventuali messaggi
    const $alertsContainer = $('#alerts-container');
    $alertsContainer.empty();

    const $messages = $machineData.find('glc\\:message');

    if ($messages.length === 0) {
        $alertsContainer.append(
            $('<div>')
                .addClass('alert info')
                .text('Nessun alert')
        );

    } else {

        $messages.each(function() {
            const $message = $(this);

            const messageType = $message.find('glc\\:type').text().trim();
            const text = $message.find('glc\\:text').text().trim();

            const $alertDiv = $('<div>')
                .addClass('alert ' + messageType.toLowerCase())
                .text(text);

            $alertsContainer.append($alertDiv);
        });
    }
}


function handleLogout() {
    $.post("/api/logout")
        .always(function() {
            clearStatoMacchina();
            currentMachineStatus = "";
            pendingActionType = null;
            window.location.href = "/addetto";
            // showPage('login');
        });
}


function clearStatoMacchina() {
    $('#username').text('');
    $('#machine-status').addClass('hidden');
    $('#actions-section').addClass('hidden');
    $('.supplies-container').empty();
    $('#alerts-container').empty();
}


function prepareStatusChange() {
    const machineId = $('#current-machine-id').text().trim();

    if (!machineId) {
        showAlert("ERRORE", "Nessuna macchina selezionata");
        return;
    }

    let nextStatus = "";

    if (currentMachineStatus === 'MAINTENANCE') {
        nextStatus = "ACTIVE";
    } else if (currentMachineStatus === 'ACTIVE') {
        nextStatus = "MAINTENANCE";
    }

    pendingActionType = {
        type: 'STATUS',
        payload: { machineId: machineId, status: nextStatus },
        desc: `Stato attuale: <b>${currentMachineStatus}</b>.<br>Vuoi cambiare in <strong>${nextStatus}</strong>?`,
        url: '/api/addetto/status'
    };

    showPopup("Cambio Stato", pendingActionType.desc);
}


function prepareRefill() {
    const machineId = $('#current-machine-id').text().trim();

    pendingActionType = {
        type: 'REFILL',
        payload: { machineId: machineId },
        desc: `Confermi il ripristino completo delle scorte per <strong>${machineId}</strong>?`,
        url: '/api/addetto/restore-supplies'
    };

    showPopup("Refill Forniture", pendingActionType.desc);
}


function showPopup(title, htmlBody) {
    $('#popup-title').text(title);
    $('#popup-message').html(htmlBody);
    $('#action-popup').removeClass('hidden');
}


function closePopup() {
    $('#action-popup').addClass('hidden');
    pendingActionType = null;
}


function confirmAction() {
    if (!pendingActionType) return;

    $.post(pendingActionType.url, pendingActionType.payload)
    .done(function() {
        closePopup();
        showStatusSelectedMachine();
        showAlert("INFO", "Stato aggiornato!");
    })
    .fail(function(xhr) {
        const errorMsg = getErrorMessage(xhr, "Errore Server");
        closePopup();
        showAlert("ERRORE", errorMsg);
    });
}
