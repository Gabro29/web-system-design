

let MACHINE_CODE = null;
let MACHINE_PLACE = null;
$(document).ready(function() {
    updateMachineUI("screensaver", "offline", "Avvio sistema...");
    initClock();
    initSugarButtons();
    bindProductAction();
    bindConfirmPopupAction();

    MACHINE_CODE = $("#screensaver-machine-id").text().trim();
    const $body = $('body');
    const INITIAL_ERROR = $body.data('error-message');
    MACHINE_PLACE = $body.attr('macchinetta-place');

    if (INITIAL_ERROR) {
        updateMachineUI("screensaver", "offline", INITIAL_ERROR);
    } else {
        updateMachineUI("screensaver", "offline", "Avvio sistema...");
    }

    performMachineLogin(MACHINE_PLACE);
});


let pollingTimer = null;
function performMachineLogin(place) {
    if (pollingTimer) clearTimeout(pollingTimer);

    if (!MACHINE_CODE || MACHINE_CODE === "--") {
        updateMachineUI("screensaver", "offline", "Codice mancante nella URL");
        return;
    }


    $.post("/api/macchinetta/auth", {
        code: MACHINE_CODE,
        place: place
    })
        .done(function() {
            checkConnectedUser();
        })
        .fail(function(xhr) {
            if (xhr.status === 404) {
                updateMachineUI("screensaver", "offline", "Fuori servizio");
                return;
            }
            const errorMsg = getErrorMessage(xhr, "Errore comunicazione server");
            updateMachineUI("screensaver", "offline", errorMsg);
            pollingTimer = setTimeout(() => performMachineLogin(place), 10000);
        });
}


function initClock() {

    const now = new Date();
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const formattedTime = hours + ':' + minutes;

    $('#time').text(formattedTime);
    $('#screensaver-time').text(formattedTime);

    const seconds = now.getSeconds();
    setTimeout(initClock, (60 - seconds) * 1000 + 100);
}


let selectedSugarLevel = 1;
function initSugarButtons() {

    $('.sugar-btn').on('click', function() {

        $(this).addClass('active').siblings().removeClass('active');

        selectedSugarLevel = $(this).data('level');
    });
}


let selectedProductForConfirmation = null;
function bindProductAction() {

    $('.product-item').on('click', function() {

        const productName = $(this).find('.product-name').text();
        const productPrice = parseFloat($(this).find('.product-price').text().replace('€', ''));
        const productId = $(this).find('.prod-id').val();

        selectedProductForConfirmation = {
            id: productId,
            name: productName,
            price: productPrice
        };

        $('#confirm-drink-name').text(productName);
        $('#confirm-sugar-level').text(selectedSugarLevel);
        $('#confirm-price').text(productPrice.toFixed(2));
        $('#confirm-popup-overlay').addClass('active');
    });
}


let isMachineErogating = false;
function bindConfirmPopupAction() {

    $('.popup-btn-cancel').on('click', function() {
        $('#confirm-popup-overlay').removeClass('active');
        selectedProductForConfirmation = null;
    });

    $('.popup-btn-confirm').on('click', function() {
        if (isMachineErogating) return;
        if (!selectedProductForConfirmation) return;

        const requestData = {
            machineCode: MACHINE_CODE,
            productId: selectedProductForConfirmation.id,
            sugarLevel: selectedSugarLevel
        };

        const price = selectedProductForConfirmation.price;

        $.post('/api/macchinetta/purchase', requestData)
            .done(function() {
                $('#confirm-popup-overlay').removeClass('active');
                startErogationProcess(price);
                selectedProductForConfirmation = null;
            })
            .fail(function(xhr) {
                const errorMsg = getErrorMessage(xhr, "Errore durante l'acquisto");
                showAlert("ERRORE", errorMsg);
                $('#confirm-popup-overlay').removeClass('active');
            });
    });
}


function startErogationProcess(price) {

    if (isMachineErogating) return;
    isMachineErogating = true;

    const $progressSection = $('#progress-section');
    const $progressFill = $('#progress-fill');

    $('.sugar-section, .products-list').hide();

    $progressFill.css({ 'transition': 'none', 'width': '0%' });
    $progressSection.removeClass('hidden');
    $progressFill[0].offsetHeight;
    $progressFill.css({ 'transition': 'width 3s ease-in-out', 'width': '100%' });

    setTimeout(function() {
        showAlert("INFO", "Erogazione completata!\nCredito scalato di: €" + price.toFixed(2));
        $progressSection.addClass('hidden');

        $('.sugar-section, .products-list').show();
        isMachineErogating = false;
        checkConnectedUser();
    }, 3000);
}


let currentUserCredit = 0.0;
function updateMachineUI(screen, indicator, text) {

    if (screen === "active") {
        $('#active').addClass('active');
        $('#screensaver').removeClass('active');
    } else {
        $('#screensaver').addClass('active');
        $('#active').removeClass('active');
    }

    $('.status-indicator').removeClass('busy connected offline').addClass(indicator);
    $('.status-text').text(text);
}


let currentMachineStatus = null;
let pollingTimerConnectedUser = null;
function checkConnectedUser() {
    if (pollingTimerConnectedUser) clearTimeout(pollingTimerConnectedUser);

    if (isMachineErogating) {
        pollingTimerConnectedUser = setTimeout(checkConnectedUser, 3000);
        return;
    }

    const pollUrl = '/api/macchinetta/poll';

    $.get(pollUrl)
        .done(function (xml){
            let connected = $(xml).find("userConnected").text() === "true";
            let username = $(xml).find("username").text() || "Utente";
            let credit = parseFloat($(xml).find("credit").text().replace(',', '.')) || 0;
            let status = $(xml).find("status").text();

            currentMachineStatus = status;
            startHeartbeat();

            if (status !== 'ACTIVE') {
                let msg = (status === 'MAINTENANCE') ? "Macchinetta in manutenzione" : "Macchinetta fuori servizio";
                updateMachineUI("screensaver", "offline", msg);
                pollingTimerConnectedUser = setTimeout(checkConnectedUser, 5000);
                return;
            }


            if (connected) {
                currentUserCredit = credit;
                $('#user-name').text(username);
                $('#user-credit').text(credit.toFixed(2));
                updateMachineUI("active", "busy", "Macchina Occupata");
            } else {
                currentUserCredit = 0.0;
                updateMachineUI("screensaver", "connected", "Macchina Libera");
            }

            pollingTimerConnectedUser = setTimeout(checkConnectedUser, 3000);
        })
        .fail(function(xhr) {
            if (xhr.status === 401 || xhr.status === 403) {
                performMachineLogin(MACHINE_PLACE);
                return;
            }
            if (xhr.status === 404) {
                if (pollingTimerConnectedUser) clearTimeout(pollingTimerConnectedUser);
                if (heartbeatTimer) clearTimeout(heartbeatTimer);
                updateMachineUI("screensaver", "offline", "Fuori servizio");
                return;
            }
            updateMachineUI("screensaver", "offline", "Riconnessione...");
            pollingTimerConnectedUser = setTimeout(checkConnectedUser, 5000);
        });
}


let heartbeatTimer = null;
let isSendingHeartbeat = false;
function startHeartbeat() {
    if (heartbeatTimer) return;
    sendHeartbeat();
    heartbeatTimer = setInterval(sendHeartbeat, 60000);
}


function sendHeartbeat() {
    if (!MACHINE_CODE) return;
    if (!currentMachineStatus) return;
    if (isSendingHeartbeat) return;
    isSendingHeartbeat = true;

    const JAKARTA_SERVER = window.APP_CONFIG.JAKARTA_SERVER;
    $.post(JAKARTA_SERVER + "/heartbeat", { macchinetta_code: MACHINE_CODE, status: currentMachineStatus }, null, "xml")
        .done(function (xml) {
            const status = $(xml).find("status").text();
            const errorCode = $(xml).find("errorCode").text();
            const message = $(xml).find("message").text();

            if (status !== "OK") {
                console.warn(`[Heartbeat] ERROR ${errorCode}: ${message}`);
            }
        })
        .fail(function (xhr) {
            const xml = xhr.responseXML;
            if (xml) {
                const status = $(xml).find("status").text();
                const errorCode = $(xml).find("errorCode").text();
                const message = $(xml).find("message").text();

                console.warn(`[Heartbeat] HTTP ${xhr.status} ${status} ${errorCode}: ${message}`);
            } else {
                console.error("[Heartbeat] FAIL HTTP", xhr.status, xhr.responseText);
            }
        })
        .always(function () {
            isSendingHeartbeat = false;
        });

}

