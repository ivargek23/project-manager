document.addEventListener('DOMContentLoaded', () => {
    const dialog = document.getElementById('confirm-dialog');
    if (!dialog) return;

    const text = dialog.querySelector('[data-confirm-text]');
    const confirmButton = dialog.querySelector('[data-confirm-ok]');
    let pendingForm = null;

    document.addEventListener('submit', (event) => {
        const form = event.target;
        if (!form.matches('form[data-confirm]')) return;

        event.preventDefault();
        pendingForm = form;
        text.textContent = form.dataset.confirm;
        dialog.showModal();
    })

    confirmButton.addEventListener('click', () => {
        dialog.close();
        if (pendingForm) pendingForm.submit();
    })

    dialog.addEventListener('close', () => {
        if (dialog.returnValue === 'cancel') pendingForm = null;
    });
})