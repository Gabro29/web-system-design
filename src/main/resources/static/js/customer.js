
$(document).ready(function() {
    bindConnectDisconnectButtons();

    MapController.init();
    checkActiveSession();
});


function checkActiveSession() {
    $.get("/api/cliente/me")
        .done(function(xml) {
            const username = $(xml).find("username").text();
            currentUserEmail = $(xml).find("email").text();
            const credit = parseFloat($(xml).find("credito").text()) || 0.00;
            const currentMachineId = $(xml).find("currentMachineId").text();

            $('#username').text(username);
            $('#credit').text(credit.toFixed(2));

            if (currentMachineId && currentMachineId.trim() !== "") {
                restoreConnectedState(currentMachineId);
            } else {
                resetConnectionUI();
            }
            showPage('home');
            startCreditPolling();
        })
        .fail(function() {
            // console.log("Nessuna sessione attiva rilevata.");
            showPage('login');
        });
}


function handleRegister() {
    const email = $('#register-email').val();
    const password = $('#register-password').val();
    const repeat_password = $('#register-password-repeat').val();
    if(password !== repeat_password) {
        showAlert('ATTENZIONE', 'Le password non corrispondono');
        return;
    }

    $.post("/api/cliente/register", { email: email, password: password })
        .done(function(xml) {
            const username = $(xml).find("username").text();
            showAlert("SUCCESS", "Benvenuto " + username + "! Ora effettua il login.");
            showPage('login');
        })
        .fail(function(xhr) {
            const errorMsg = getErrorMessage(xhr, "Registrazione Fallita");
            showAlert("ERRORE", errorMsg);
        });
}


let currentUserEmail = "";
function handleLogin() {
    const email = $('#login-email').val();
    const password = $('#login-password').val();

    $.post("/api/login", { email: email, password: password, requiredRole: 'CLIENTE'})
        .done(function(xml) {
            const username = $(xml).find("username").text();
            currentUserEmail = $(xml).find("email").text();
            const credit = parseFloat($(xml).find("credito").text()) || 0.00;
            const currentMachineId = $(xml).find("currentMachineId").text();

            $('#username').text(username);
            $('#credit').text(credit.toFixed(2));
            if (currentMachineId && currentMachineId.trim() !== "") {
                restoreConnectedState(currentMachineId);
            } else {
                resetConnectionUI();
            }
            showPage('home');
            startCreditPolling();
        })
        .fail(function(xhr) {
            const errorMsg = getErrorMessage(xhr, "Login Fallito");
            showAlert("ERRORE", errorMsg);
        });
}


let isPollingActive = false;
function startCreditPolling() {
    if (isPollingActive) return;
    isPollingActive = true;
    pollCreditLoop();
}


function pollCreditLoop() {
    if (!isPollingActive || !currentUserEmail) return;

    $.get("/api/cliente/status")
        .done(function(xml) {
            const newCredit = parseFloat($(xml).find("credito").text()) || 0.00;
            $('#credit').text(newCredit.toFixed(2));

            const serverMachineId = $(xml).find("currentMachineId").text();
            const isUiConnected = $('#status').text() === 'Connesso';

            if ((!serverMachineId || serverMachineId.trim() === "") && isUiConnected) {
                resetConnectionUI();
                showAlert("INFO", "Cliente Disconnesso forzatamente.");
            }

            else if (serverMachineId && serverMachineId.trim() !== "" && !isUiConnected) {
                restoreConnectedState(serverMachineId);
            }
        })
        .fail(function(xhr) {
            let errorMsg = "Errore comunicazione server";
            // console.warn("[Polling]" + errorMsg);
        })
        .always(function() {
            if (isPollingActive) {
                setTimeout(pollCreditLoop, 3000);
            }
        });
}


function bindConnectDisconnectButtons() {

    const $machineId = $('#machine-id');
    const $btnConnect = $('#btn-connect');
    const $btnDisconnect = $('#btn-disconnect');


    $btnConnect.on('click', function() {
        const machineId = $machineId.val().trim().toUpperCase();

        const pattern = /^[A-Z]{2}[0-9]{3}[A-Z]{2}$/;
        if (!machineId) {
            showAlert("ATTENZIONE", "Inserisci un ID macchina valido");
            return;
        }
        if (!pattern.test(machineId)) {
            showAlert("ATTENZIONE", "Formato ID non valido. Usa il formato: FE716XW");
            return;
        }

        $.post("/api/cliente/connect", { machineId: machineId })
            .done(function(xml) {
                const connectedMachine = $(xml).find("currentMachineId").text();
                showAlert("INFO", "Connesso con successo al distributore " + connectedMachine);
                restoreConnectedState(connectedMachine);
            })
            .fail(function(xhr) {
                const errorMsg = getErrorMessage(xhr, "Errore comunicazione server");
                showAlert("ERRORE", errorMsg);
            });
    });


    $btnDisconnect.on('click', function() {

        $.post("/api/cliente/disconnect")
            .done(function() {
                showAlert("INFO", "Disconnessione effettuata.");
            })
            .fail(function(xhr) {
                let errorMsg = "Errore comunicazione server";

                if (xhr.responseXML) {
                    const msgServer = $(xhr.responseXML).find("message").text();
                    if (msgServer) errorMsg = msgServer;
                }
                else if (xhr.responseText) {
                    errorMsg = xhr.responseText;
                }
                showAlert("ERRORE", errorMsg);
            })
            .always(function() {
                resetConnectionUI();
            });
    });
}


function handleRecharge() {

    const $form = $('#recharge-form');
    const amount = parseFloat($('#amount').val());
    const $progressSection = $('#progress-section');
    const $progressText = $('#progress-text');
    const $progressFill = $('#progress-fill');

    $form.hide();
    $progressSection.removeClass('hidden');

    $progressFill.css({ transition: 'none', width: '0' });
    $progressFill[0].offsetWidth;
    $progressFill.css({ transition: 'width 3s ease-in-out', width: '100%' });

    setTimeout(function() {

        $.post("/api/cliente/recharge", { amount: amount })
            .done(function(xml) {
                const newCredit = parseFloat($(xml).find("credito").text()) || 0.00;
                $('#credit').text(newCredit.toFixed(2));

                $form[0].reset();
                showAlert("INFO", 'Ricarica confermata!\nImporto: €' + amount.toFixed(2) + '\nNuovo credito: €' + newCredit.toFixed(2));
                showPage('home');
            })
            .fail(function(xhr) {
                const errorMsg = getErrorMessage(xhr, "Errore comunicazione server");
                showAlert("ERRORE", errorMsg);
            })
            .always(function() {
                $progressSection.addClass('hidden');
                $form.show();
                $progressText.text('Elaborazione pagamento...');
            });
    }, 3000);
}


function handleLogout() {
    $.post("/api/logout")
        .always(function() {
            isPollingActive = false;
            currentUserEmail = "";
            resetHomePageFields();
            resetRechargeForm();
            window.location.href = "/customer";
            // showPage('login');
        });
}


function showHomePage() {
    // window.location.href = "/customer";
    showPage('home');
}


function restoreConnectedState(machineId) {
    $('#status').text('Connesso');
    $('#machine-display').text(machineId);
    $('#machine-id').val(machineId).prop('disabled', true);
    $('#btn-connect').addClass('hidden');
    $('#btn-disconnect').removeClass('hidden');
}


function resetConnectionUI() {
    $('#status').text('Disconnesso');
    $('#machine-display').text('-');
    $('#machine-id').val('').prop('disabled', false);
    $('#btn-connect').removeClass('hidden');
    $('#btn-disconnect').addClass('hidden');
}


function resetHomePageFields() {
    $('#username').text('');
    $('#credit').text('0.00');
    $('#status').text('');
    $('#machine-display').text('-');

    $('#machine-id').val('').prop('disabled', false);

    $('#btn-connect').removeClass('hidden');
    $('#btn-disconnect').addClass('hidden');
}


function resetRechargeForm() {
    $('#amount').val('');
    $('#card-number').val('');
    $('#card-expiry').val('');
    $('#card-cvv').val('');
}


const MapController = {

    config: {
        containerId: 'map-container',
        modalId: '#map-modal',
        openBtnId: '#btn-show-map',
        closeBtnSelector: '.close-modal',
        jakartaEndpoint: window.APP_CONFIG.JAKARTA_SERVER + '/monitoring/list',
        defaultView: [38.104, 13.348],
        zoomLevel: 15,
        statusStyles: {
            'ACTIVE':      { color: '#28a745', label: 'Attivo' },
            'MAINTENANCE': { color: '#ffc107', label: 'In Manutenzione' },
            'ERROR':       { color: '#dc3545', label: 'Guasto' },
            'OFFLINE':     { color: '#6c757d', label: 'Offline' },
            'DEFAULT':     { color: '#6c757d', label: 'Sconosciuto' }
        }
    },

    state: {
        mapInstance: null,
        markersLayer: null,
        pollingInterval: null
    },

    init: function() {
        this._bindEvents();
        // console.log("MapController inizializzato.");
    },

    _bindEvents: function() {
        const self = this;

        $(this.config.openBtnId).on('click', function(e) {
            e.preventDefault();
            self.open();
        });

        $(window).on('click', function(event) {
            if ($(event.target).is(self.config.modalId)) {
                self.close();
            }
        });

        $(this.config.modalId).on('click', this.config.closeBtnSelector, function() {
            self.close();
        });

        $(document).on('keyup', function(e) {
            if (e.key === "Escape" && $(self.config.modalId).is(':visible')) {
                self.close();
            }
        });
    },

    open: function() {
        $(this.config.modalId).css('display', 'flex');

        if (!this.state.mapInstance) {
            this._initLeaflet();
        } else {
            setTimeout(() => {
                this.state.mapInstance.invalidateSize();
                this.refreshData();
            }, 200);
        }
    },


    close: function() {
        $(this.config.modalId).hide();
    },


    _initLeaflet: function() {
        this.state.mapInstance = L.map(this.config.containerId)
            .setView(this.config.defaultView, this.config.zoomLevel);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; OpenStreetMap contributors'
        }).addTo(this.state.mapInstance);
        this.state.markersLayer = L.layerGroup().addTo(this.state.mapInstance);
        this.refreshData();
    },


    refreshData: function() {
        // console.log("Mappa: Aggiornamento dati da", this.config.jakartaEndpoint);
        const self = this;
        $.get(this.config.jakartaEndpoint)
            .done((xml) => {
                this.state.markersLayer.clearLayers();
                $(xml).find("machine").each(function() {
                    self._addMarkerFromXml(this);
                });
            })
            .fail(() => {
                console.error("Mappa: Impossibile contattare il server.");
            });
    },


    _addMarkerFromXml: function(xmlElement) {
        const $el = $(xmlElement);
        const code = $el.find("code").text();
        const status = $el.find("status").text();
        const lat = parseFloat($el.find("lat").text().replace(',', '.'));
        const lng = parseFloat($el.find("lng").text().replace(',', '.'));

        if (isNaN(lat) || isNaN(lng)) return;

        const style = this.config.statusStyles[status] || this.config.statusStyles['DEFAULT'];
        const marker = L.circleMarker([lat, lng], {
            color: style.color,
            fillColor: style.color,
            fillOpacity: 0.8,
            radius: 12,
            weight: 2
        });
        const popupContent = `
            <div style="text-align: center; min-width: 100px;">
                <strong>ID: ${code}</strong><br>
                <hr style="margin: 5px 0;">
                <span style="color:${style.color}; font-weight:bold;">${style.label}</span>
            </div>
        `;

        marker.bindPopup(popupContent);
        this.state.markersLayer.addLayer(marker);
    }
};
