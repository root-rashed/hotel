// Dynamic booking calculations for the booking-form pages
// (reception/booking-form.html and customer/booking-form.html).

document.addEventListener('DOMContentLoaded', function () {
    var form = document.getElementById('bookingForm');
    if (!form) return;

    var checkInInput = document.getElementById('checkInDate');
    var checkOutInput = document.getElementById('checkOutDate');
    var roomSelect = document.getElementById('roomId');
    var estimateEl = document.getElementById('priceEstimate');

    function todayIso() {
        return new Date().toISOString().split('T')[0];
    }

    if (checkInInput) {
        checkInInput.min = todayIso();
        checkInInput.addEventListener('change', function () {
            if (checkOutInput) {
                checkOutInput.min = checkInInput.value;
            }
            recalculate();
        });
    }
    if (checkOutInput) {
        checkOutInput.addEventListener('change', recalculate);
    }
    if (roomSelect) {
        roomSelect.addEventListener('change', recalculate);
    }

    function nightsBetween(checkInStr, checkOutStr) {
        var checkIn = new Date(checkInStr);
        var checkOut = new Date(checkOutStr);
        var diffMs = checkOut - checkIn;
        return Math.round(diffMs / (1000 * 60 * 60 * 24));
    }

    function recalculate() {
        if (!estimateEl) return;

        var checkIn = checkInInput ? checkInInput.value : null;
        var checkOut = checkOutInput ? checkOutInput.value : null;

        if (!checkIn || !checkOut) {
            estimateEl.textContent = '';
            return;
        }

        var nights = nightsBetween(checkIn, checkOut);
        if (nights <= 0) {
            estimateEl.textContent = 'Check-out date must be after check-in date.';
            return;
        }

        var pricePerNight = 0;
        if (roomSelect && roomSelect.selectedOptions.length > 0) {
            pricePerNight = parseFloat(roomSelect.selectedOptions[0].getAttribute('data-price')) || 0;
        }

        if (pricePerNight > 0) {
            var total = (nights * pricePerNight).toFixed(2);
            estimateEl.textContent = nights + ' night(s) x $' + pricePerNight.toFixed(2) + ' = $' + total + ' estimated total';
        } else {
            estimateEl.textContent = nights + ' night(s) selected';
        }
    }

    // Run once on load in case of pre-filled values (e.g. browser autofill).
    recalculate();
});
