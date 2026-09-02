// Lightweight client-side form validation to complement server-side
// Jakarta Bean Validation. This never replaces server validation — it only
// gives the user faster feedback before submitting.

document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('form.form').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            var valid = true;
            var firstInvalid = null;

            form.querySelectorAll('[required]').forEach(function (field) {
                if (!field.value || !field.value.trim()) {
                    valid = false;
                    field.classList.add('field-invalid');
                    if (!firstInvalid) firstInvalid = field;
                } else {
                    field.classList.remove('field-invalid');
                }
            });

            // Cross-field date validation for booking forms.
            var checkIn = form.querySelector('#checkInDate');
            var checkOut = form.querySelector('#checkOutDate');
            if (checkIn && checkOut && checkIn.value && checkOut.value) {
                if (new Date(checkOut.value) <= new Date(checkIn.value)) {
                    valid = false;
                    checkOut.classList.add('field-invalid');
                    if (typeof window.showToast === 'function') {
                        window.showToast('Check-out date must be after check-in date.', 'error');
                    }
                    if (!firstInvalid) firstInvalid = checkOut;
                }
            }

            if (!valid) {
                event.preventDefault();
                if (firstInvalid) firstInvalid.focus();
            }
        });
    });
});