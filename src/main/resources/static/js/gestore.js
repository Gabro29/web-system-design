

$(document).ready(function() {

    // MANAGE MACHINES SECTION //
    bindManageMachineCard();

    // MANAGE ADDETTI SECTION //
    bindManagedAddetti();

    // OVERVIEW SECTION //
    bindFilters();
    bindOverviewPopupAction();

    MapController.init();
    checkActiveSession();
});



function checkActiveSession() {
    $.get("/api/gestore/me")
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


function handleLogin() {
    const email = $('#login-email').val();
    const password = $('#login-password').val();

    $.post("/api/login", { email: email, password: password, requiredRole: 'GESTORE' })
        .done(function(xml) {
            const username = $(xml).find("username").text();
            $('#username').text(username);
            showPage('home');
        })
        .fail(function(xhr) {
            const errorMsg = getErrorMessage(xhr, "Login fallito");
            showAlert("ERRORE", errorMsg);
        });
}


function showHomePage() {
    resetGrids();
    // window.location.href = "/gestore";
    showPage('home');
}


function handleLogout() {
    $.post("/api/logout")
        .always(function() {
            $('#username').text('');
            resetGrids();
            // Per il refresh Token CSRF
            window.location.href = "/gestore";
            // showPage('login');
        });
}


function resetGrids() {
    $('.machines-grid, #addetti-grid').empty();
    $('#results-count').text('0 distributori trovati');
}



// GESTIONE DISTRIBUTORI SECTION //

function showManageMachines() {
    loadAndDisplayMachines(null, 'manage');
    showPage('manage-machines');
}


function bindManageMachineCard() {

    // Se una card nella lista viene cliccata
    // Importante in quanto nascono e muoiono dinamicamente,
    // altrimenti il binding vale solo per quelle che esistono ora
    $(document).on('click', '.machine-card.manage', function() {
        const machineId = $(this).data('machine-id');
        const selectedAction = $('#machine-action').val();
        askConfirmAction(machineId, selectedAction);
    });


    $("#add-new-machine-btn").on("click", function() {
        askConfirmAction(null, 'add');
    });

    $('#macchinetta-cancel-action-btn').on('click', function () {
        $('#action-confirm-popup-macchinetta').addClass('hidden');
        $('#add-machine-form')[0].reset();
    });
}


function addMachine() {
    const $actionConfirm = $('#action-confirm-popup-macchinetta');
    const machineId = $actionConfirm.data('machine-id');
    const selectedAction = $actionConfirm.data('selected-action');
    performActionMacchinetta(machineId, selectedAction);
}


// POPUP GESTIONE DISTRIBUTORI
function askConfirmAction(machineId, selectedAction) {

    const actionMapping = {
        'add': 'aggiunta',
        'remove': 'rimozione',
        'enable': 'attivazione',
        'disable': 'disattivazione'
    };


    $('#action-machine-id-text').text(machineId);
    $('#action-type-text-macchinetta').text(actionMapping[selectedAction]);

    const $machineFields = $('#add-machine-fields')
    const $machineIdInput = $('#new-machine-id');
    const $machinePlaceInput = $('#new-machine-place');

    // Mostro eventuali campi aggiuntivi
    if (selectedAction === 'add') {
        $machineFields.removeClass('hidden');
        $machineIdInput.prop('required', true);
        $machinePlaceInput.prop('required', true);
    } else {
        $machineFields.addClass('hidden');
        $machineIdInput.prop('required', false);
        $machinePlaceInput.prop('required', false);
    }

    const $actionConfirm = $('#action-confirm-popup-macchinetta');

    $actionConfirm
        .data('machine-id', machineId)
        .data('selected-action', selectedAction);

    // Mostro il popup
    $actionConfirm.removeClass('hidden');
}


function performActionMacchinetta(machineId, selectedAction) {
    const $actionConfirmPopup = $('#action-confirm-popup-macchinetta');
    const $machineCard = $(`.machine-card[data-machine-id="${machineId}"]`);
    const $badge = $machineCard.find('.status-badge');
    const currentStatus = $badge.text().trim().toLowerCase();

    if (selectedAction === 'add') {
        const newMachineId = $('#new-machine-id').val().trim().toUpperCase();
        const newMachinePlace = $('#new-machine-place').val().trim();

        const pattern = /^[A-Z]{2}[0-9]{3}[A-Z]{2}$/;
        if (!pattern.test(newMachineId)) {
            showAlert("ATTENZIONE", 'Formato ID non valido. Esempio richiesto: AB123CD');
            return;
        }

        const $existingCard = $(`.machine-card[data-machine-id="${newMachineId}"]`);
        if ($existingCard.length > 0) {
            showAlert("ERRORE", `Il distributore ${newMachineId} esiste già nella lista.`);
            return;
        }

        $.post("/api/gestore/machines/add", { machineId: newMachineId, machinePlace: newMachinePlace })
            .done(function() {
                const newCardHTML = `
                    <div class="machine-card manage" data-machine-id="${newMachineId}">
                        <h4>${newMachineId}</h4>
                        <span class="status-badge maintenance">maintenance</span>
                    </div>
                `;

                $('#machines-grid-manage').append(newCardHTML);
                // showAlert("INFO", `Distributore ${newMachineId} aggiunto in ${newMachinePlace}`);

                $actionConfirmPopup.addClass('hidden');
                $('#add-machine-form')[0].reset();
            })
            .fail(function(xhr) {
                const errorMsg = getErrorMessage(xhr, "Errore aggiunta macchina");
                showAlert("ERRORE", errorMsg);
            });

        return;
    }

    if (selectedAction === 'remove') {
        if (currentStatus !== 'maintenance') {
            showAlert("ATTENZIONE", `Per sicurezza, disattivare prima il distributore ${machineId}`);
            $actionConfirmPopup.addClass('hidden');
            return;
        }
    }

    else if (selectedAction === 'enable') {
        if (currentStatus !== 'maintenance') {
            showAlert("ATTENZIONE", `Stato attuale: ${currentStatus}. Solo macchine MAINTENANCE possono essere attivate`);
            $actionConfirmPopup.addClass('hidden');
            return;
        }
    }

    else if (selectedAction === 'disable') {
        if (currentStatus !== 'active') {
            showAlert("ATTENZIONE", `Stato attuale: ${currentStatus}. Solo macchine ACTIVE possono essere disattivate`);
            $actionConfirmPopup.addClass('hidden');
            return;
        }
    }

    let url = "";
    let payload = { machineId: machineId };

    if (selectedAction === 'remove') {
        url = "/api/gestore/machines/delete";
    } else if (selectedAction === 'enable') {
        url = "/api/gestore/machines/status";
        payload.status = "ACTIVE";
    } else if (selectedAction === 'disable') {
        url = "/api/gestore/machines/status";
        payload.status = "MAINTENANCE";
    }

    $.post(url, payload)
        .done(function() {
            if (selectedAction === 'remove') {
                $machineCard.remove();
                // showAlert("INFO", `Distributore ${machineId} rimosso definitivamente.`);
            }
            else if (selectedAction === 'enable') {
                $badge
                    .removeClass('active offline maintenance error')
                    .addClass('active')
                    .text('active');
                // showAlert("INFO", `Distributore ${machineId} attivato.`);
            }
            else if (selectedAction === 'disable') {
                $badge
                    .removeClass('active offline maintenance error')
                    .addClass('maintenance')
                    .text('maintenance');
                // showAlert("INFO", `Distributore ${machineId} disattivato.`);
            }

            if ($('#manage-machines').hasClass('active')) {
                loadAndDisplayMachines(null, 'manage');
            }
        })
        .fail(function(xhr) {
            const errorMsg = getErrorMessage(xhr, "Errore durante l'operazione");
            showAlert("ERRORE", errorMsg);
        })
        .always(function() {
            $actionConfirmPopup.addClass('hidden');
        });
}



// GESTIONE ADDETTI //

function bindManagedAddetti() {

    $('#search-addetto-id').on('input', function () {
        const query = $(this).val().trim().toLowerCase();
        loadAndDisplayAddetti(query);
    });

    $('#add-new-addetto-btn').on('click', function () {
        askConfirmActionAddetto(null, 'add');
    });


    $(document).on("click", ".remove-addetto-btn", function (e) {
        const email = $(this).data("remove-id");
        askConfirmActionAddetto(email, 'remove');
    });


    $('#addetto-cancel-action-btn').on('click', function () {
        $('#action-confirm-popup-addetto').addClass('hidden');
        $('#add-addetto-form')[0].reset();
    });
}


function showManageAddetti() {
    loadAndDisplayAddetti(null);
    showPage('manage-addetti');
}


function loadAndDisplayAddetti(filterQuery = null) {

    $.get('/api/gestore/addettixml')
        .done(function (xml) {
            let $addetti = $(xml).find('addetto');

            if (filterQuery) {
                $addetti = $addetti.filter(function () {
                    const email = $(this).find('email').text().trim().toLowerCase();
                    return email.includes(filterQuery);
                });
            }

            fillAddettiGrid($addetti);
        })
        .fail(function (xhr) {
            const errorMsg = getErrorMessage(xhr, "Errore comunicazione server");
            showAlert("ERRORE", errorMsg);
        });
}


function fillAddettiGrid($addettiData) {
    const $addettiGrid = $('#addetti-grid');
    $addettiGrid.empty();

    if ($addettiData.length > 0) {
        $addettiData.each(function () {
            const addettoEmail = $(this).find('email').text().trim();

            const cardHTML = `
                <div class="addetto-card" data-addetto-id="${addettoEmail}">
                    <h4>${addettoEmail}</h4>
                    <button class="remove-addetto-btn" data-remove-id="${addettoEmail}">×</button>
                </div>
            `;

            $addettiGrid.append(cardHTML);
        });
    } else {
        $addettiGrid.html(`
            <div class="addetto-card">
                <h4>Nessun addetto trovato</h4>
            </div>
        `);
    }
}


function askConfirmActionAddetto(addettoEmail, selectedAction) {

    const actionMapping = {
        add: 'aggiunta',
        remove: 'rimozione'
    };

    $('#action-addetto-id-text').text(addettoEmail || "");
    $('#action-type-text-addetto').text(actionMapping[selectedAction]);

    const $fields = $('#add-addetto-fields');
    const $idInput = $('#new-addetto-id');
    const $passwordInput = $('#new-addetto-password');

    if (selectedAction === 'add') {
        $fields.removeClass('hidden');
        $idInput.prop('required', true).val("");
        $passwordInput.prop('required', true).val("");
    } else {
        $fields.addClass('hidden');
        $idInput.prop('required', false);
        $passwordInput.prop('required', false);
    }

    const $popup = $('#action-confirm-popup-addetto');

    $popup
        .data('addetto-email', addettoEmail)
        .data('selected-action', selectedAction)
        .removeClass('hidden');
}


function addAddetto() {
    const $popup = $('#action-confirm-popup-addetto');
    const email = $popup.data('addetto-email');
    const action = $popup.data('selected-action');
    performActionAddetto(email, action);
}


function performActionAddetto(addettoEmail, selectedAction) {
    const $popup = $('#action-confirm-popup-addetto');

    if (selectedAction === "add") {
        const newId = $('#new-addetto-id').val().trim().toLowerCase();
        const newPassword = $('#new-addetto-password').val().trim();

        const $existingCard = $(`.addetto-card[data-addetto-id="${newId}"]`);
        if ($existingCard.length > 0) {
            showAlert("ERRORE", `L'addetto ${newId} è già presente nella lista.`);
            return;
        }


        $.post("/api/gestore/addetto/add", { email: newId, password: newPassword })
            .done(function() {
                const newCardHTML = `
                    <div class="addetto-card" data-addetto-id="${newId}">
                        <h4>${newId}</h4>
                        <button class="remove-addetto-btn" data-remove-id="${newId}">×</button>
                    </div>
                `;
                $('#addetti-grid').append(newCardHTML);

                // showAlert("INFO", `Addetto ${newId} aggiunto con successo!`);
                $('#add-addetto-form')[0].reset();
                $popup.addClass('hidden');
            })
            .fail(function(xhr) {
                const errorMsg = getErrorMessage(xhr, "Errore aggiunta addetto");
                showAlert("ERRORE", errorMsg);
            });
        return;
    }


    if (selectedAction === "remove") {

        const url = "/api/gestore/addetto/delete";
        const payload = { email: addettoEmail };

        $.post(url, payload)
            .done(function() {
                $(`.addetto-card[data-addetto-id="${addettoEmail}"]`).remove();

                // showAlert("INFO", `Addetto ${addettoEmail} rimosso con successo`);

                const $addettiGrid = $('#addetti-grid');
                if ($addettiGrid.children().length === 0) {
                    $addettiGrid.html('<div class="addetto-card"><h4>Nessun addetto trovato</h4></div>');
                }
            })
            .fail(function(xhr) {
                let errorMsg = "Errore rimozione addetto";

                if (xhr.responseXML) {
                    errorMsg = $(xhr.responseXML).find("message").text();
                } else if (xhr.responseText) {
                    try {
                        const json = JSON.parse(xhr.responseText);
                        if(json.message) errorMsg = json.message;
                    } catch(e) {
                        errorMsg = xhr.responseText;
                    }
                }
                showAlert("ERRORE", errorMsg);
            })
            .always(function() {
                $popup.addClass('hidden');
            });
    }
}



// OVERVIEW SECTION //

function bindFilters() {
    $(document).on('keyup', '#search-machine-id', function () {
        const searchQuery = $(this).val().trim().toUpperCase();
        loadAndDisplayMachines(searchQuery, 'overview');
    });

    $(document).on('change', '#filter-status, #filter-problems-only, #filter-low-supplies', function () {
        const searchQuery = $('#search-machine-id').val().trim().toUpperCase();
        loadAndDisplayMachines(searchQuery, 'overview');
    });
}


function bindOverviewPopupAction(){
    $(document).on('click', '.machine-card.overview', function() {
        const machineId = $(this).data('machine-id');
        showSelectedMachineDetails(machineId);
    });

    $('#popup-overlay button').on('click', function() {
        $('#popup-overlay').addClass('hidden');
    });
}


function showOverviewPage(typedMachineId = null) {
    loadAndDisplayMachines(typedMachineId, 'overview');
    showPage('overview');
}


// POPUP OVERVIEW
function showSelectedMachineDetails(machineId) {

    $.get('/api/gestore/machinesxml', function (xml) {
        const $machines = $(xml).find('glc\\:machine');
        let found = false;

        $machines.each(function() {
            const $currentMachine = $(this);
            const id = $currentMachine.find('glc\\:machine-id').text().trim().toUpperCase();

            if (id === machineId) {
                found = true;
                fillPopupHTML($currentMachine);
                return false;
            }
        });

    }).fail(function (xhr) {
        const errorMsg = getErrorMessage(xhr, "Errore comunicazione server");
        showAlert("ERRORE", errorMsg);
    });

}


function fillPopupHTML($machineData) {

    const machineId = $machineData.find('glc\\:machine-id').text().trim();
    $('#detail-machine-id').text(machineId);

    const machineStatus = $machineData.find('glc\\:machine-status').text().trim().toLowerCase();
    const $state = $('#detail-machine-state');
    $state.removeClass('active inactive maintenance offline error');
    $state.text(machineStatus);
    $state.addClass(machineStatus);

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

        const supplyHTML = `
            <div class="supply-item">
                <span class="supply-name">${name}</span>
                <span class="supply-level ${levelClass}">${levelPercent}%</span>
                <div class="progress-bar">
                    <div class="fill ${levelClass}" style="width: ${levelPercent}%"></div>
                </div>
            </div>
        `;

        $suppliesContainer.append(supplyHTML);
    });

    // Aggiungo eventuali messaggi
    const $alertsContainer = $('#alerts-container');
    $alertsContainer.empty();

    const $messages = $machineData.find('glc\\:message');

    if ($messages.length === 0) {
        $alertsContainer.html(`
            <div class="alert info">Nessun alert</div>
        `);
    } else {
        $messages.each(function() {
            const $message = $(this);

            const messageType = $message.find('glc\\:type').text().trim();
            const text = $message.find('glc\\:text').text().trim();

            const alertHTML = `
                <div class="alert ${messageType.toLowerCase()}">${text}</div>
            `;

            $alertsContainer.append(alertHTML);
        });
    }

    // Mostro il popup
    $('#popup-overlay').removeClass('hidden');
}




// FUNZIONI COMUNI PER GESTIONI DISTRIBUTORI E OVERVIEW

function loadAndDisplayMachines(filterQuery = null, idContainer = 'overview') {
    const isOverview = idContainer === 'overview';

    const statusFilter = isOverview ? $('#filter-status').val() : null;
    const problemsOnly = isOverview ? $('#filter-problems-only').is(':checked') : false;
    const lowSuppliesOnly = isOverview ? $('#filter-low-supplies').is(':checked') : false;

    $.get('/api/gestore/machinesxml')
        .done(function (xml) {
            let $machines = $(xml).find('glc\\:machine');

            if (isOverview) {
                $machines = applyFilters($machines, filterQuery, statusFilter, problemsOnly, lowSuppliesOnly);
            }

            fillMachineGrid($machines, idContainer);

            const count = $machines.length;
            $('#results-count').text(`${count} distributori trovati`);
        })
        .fail(function (xhr) {
            const errorMsg = getErrorMessage(xhr, "Errore comunicazione server");
            showAlert("ERRORE", errorMsg);
        });
}


function applyFilters($machines, filterQuery, statusFilter, problemsOnly, lowSuppliesOnly) {

    if (filterQuery) {
        $machines = $machines.filter(function () {
            const id = $(this).find('glc\\:machine-id').text().trim().toUpperCase();
            return id.includes(filterQuery);
        });
    }

    if (statusFilter) {
        $machines = $machines.filter(function () {
            const status = $(this).find('glc\\:machine-status').text().trim();
            return status === statusFilter;
        });
    }

    if (problemsOnly) {
        $machines = $machines.filter(function () {
            const status = $(this).find('glc\\:machine-status').text().trim();
            return status === 'ERROR';
        });
    }

    if (lowSuppliesOnly) {
        $machines = $machines.filter(function () {
            const $supplies = $(this).find('glc\\:supply');
            let hasCritical = false;

            $supplies.each(function() {
                const level = parseInt($(this).find('glc\\:level-percent').text().trim(), 10);
                const threshold = parseInt($(this).find('glc\\:warning-threshold').text().trim(), 10);

                if (level < threshold) {
                    hasCritical = true;
                    return false;
                }
            });
            return hasCritical;
        });
    }

    return $machines;
}


function fillMachineGrid($machinesData, idContainer) {
    const $machineGrid = $(`#machines-grid-${idContainer}`);
    $machineGrid.empty();

    if ($machinesData.length > 0) {
        $machinesData.each(function () {
            const machineId = $(this).find('glc\\:machine-id').text().trim();
            const machineStatus = $(this).find('glc\\:machine-status').text().trim().toLowerCase();

            const cardHTML = `
                <div class="machine-card ${idContainer}" data-machine-id="${machineId}">
                    <h4>${machineId}</h4>
                    <span class="status-badge ${machineStatus}">${machineStatus}</span>
                </div>
            `;

            $machineGrid.append(cardHTML);
        });
    } else {
        const errorMessage = idContainer === 'overview' ? 'Cambiare il filtro di ricerca' : 'Errore nel reperire i dati';
        $machineGrid.html(`
            <div class="machine-card">
                <h4>Nessuna macchina trovata</h4>
                <span class="status-badge error">${errorMessage}</span>
            </div>
        `);
    }
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
