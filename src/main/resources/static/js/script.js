// Auto-hide alerts after 5 seconds
document.addEventListener('DOMContentLoaded', function() {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 300);
        }, 5000);
    });

    // Form validation
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const requiredFields = form.querySelectorAll('[required]');
            let isValid = true;

            requiredFields.forEach(field => {
                if (!field.value.trim()) {
                    isValid = false;
                    field.style.borderColor = 'var(--error-color)';
                } else {
                    field.style.borderColor = 'var(--border-color)';
                }
            });

            if (!isValid) {
                e.preventDefault();
                alert('Please fill in all required fields');
            }
        });
    });

    // Confirm delete actions
    const deleteForms = document.querySelectorAll('form[action*="delete"]');
    deleteForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            if (!confirm('Are you sure you want to delete this item?')) {
                e.preventDefault();
            }
        });
    });
});

// Date validation - prevent past dates
const dateInputs = document.querySelectorAll('input[type="date"]');
dateInputs.forEach(input => {
    const today = new Date().toISOString().split('T')[0];
    if (!input.hasAttribute('max')) {
        input.setAttribute('min', today);
    }
});

// Password strength indicator
const passwordInputs = document.querySelectorAll('input[type="password"]');
passwordInputs.forEach(input => {
    input.addEventListener('input', function() {
        const strength = checkPasswordStrength(this.value);
        // You can add visual feedback here
    });
});

function checkPasswordStrength(password) {
    let strength = 0;
    if (password.length >= 8) strength++;
    if (/[a-z]/.test(password)) strength++;
    if (/[A-Z]/.test(password)) strength++;
    if (/[0-9]/.test(password)) strength++;
    if (/[^a-zA-Z0-9]/.test(password)) strength++;
    return strength;
}

// Table search functionality
function searchTable(inputId, tableId) {
    const input = document.getElementById(inputId);
    const table = document.getElementById(tableId);

    if (input && table) {
        input.addEventListener('keyup', function() {
            const filter = this.value.toLowerCase();
            const rows = table.getElementsByTagName('tr');

            for (let i = 1; i < rows.length; i++) {
                const row = rows[i];
                const cells = row.getElementsByTagName('td');
                let found = false;

                for (let j = 0; j < cells.length; j++) {
                    if (cells[j].textContent.toLowerCase().includes(filter)) {
                        found = true;
                        break;
                    }
                }

                row.style.display = found ? '' : 'none';
            }
        });
    }
}

// Format currency
function formatCurrency(amount) {
    return 'LKR ' + parseFloat(amount).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,');
}

// Appointment time slot validation
function validateTimeSlot(timeInput, dateInput) {
    timeInput.addEventListener('change', function() {
        const selectedDate = new Date(dateInput.value);
        const today = new Date();

        if (selectedDate.toDateString() === today.toDateString()) {
            const selectedTime = this.value;
            const currentTime = today.getHours() + ':' + String(today.getMinutes()).padStart(2, '0');

            if (selectedTime < currentTime) {
                alert('Please select a future time');
                this.value = '';
            }
        }
    });
}

// Mobile menu toggle
const menuToggle = document.querySelector('.menu-toggle');
const navMenu = document.querySelector('.nav-menu');

if (menuToggle && navMenu) {
    menuToggle.addEventListener('click', () => {
        navMenu.classList.toggle('active');
    });
}

// Print functionality for reports
function printReport() {
    window.print();
}

// Export table to CSV
function exportTableToCSV(tableId, filename) {
    const table = document.getElementById(tableId);
    let csv = [];
    const rows = table.querySelectorAll('tr');

    for (let i = 0; i < rows.length; i++) {
        const row = [], cols = rows[i].querySelectorAll('td, th');

        for (let j = 0; j < cols.length; j++) {
            row.push(cols[j].innerText);
        }

        csv.push(row.join(','));
    }

    downloadCSV(csv.join('\n'), filename);
}

function downloadCSV(csv, filename) {
    const csvFile = new Blob([csv], { type: 'text/csv' });
    const downloadLink = document.createElement('a');

    downloadLink.download = filename;
    downloadLink.href = window.URL.createObjectURL(csvFile);
    downloadLink.style.display = 'none';

    document.body.appendChild(downloadLink);
    downloadLink.click();
    document.body.removeChild(downloadLink);
}


    const menu = document.getElementById('mobile-menu');
    const navLinks = document.querySelector('.nav-links');
    const navbar = document.querySelector('.navbar');

    menu.addEventListener('click', () => {
    menu.classList.toggle('active');
    navLinks.classList.toggle('active');
});

    window.addEventListener('scroll', () => {
    if (window.scrollY > 30) {
    navbar.classList.add('scrolled');
} else {
    navbar.classList.remove('scrolled');
}
});



