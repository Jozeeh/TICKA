/*
 * Confirmaciones con SweetAlert2 en lugar de confirm() del navegador.
 *
 * Uso en un formulario:
 *   <form ... data-confirmar="¿Eliminar esta zona?"
 *         data-detalle="Texto opcional" data-boton="Sí, eliminar" data-peligro="si">
 * Si el usuario acepta, el formulario se envía; si cancela, no pasa nada.
 */
(function () {
    'use strict';

    function confirmar(form) {
        var peligro = form.getAttribute('data-peligro') === 'si';
        return Swal.fire({
            title: form.getAttribute('data-confirmar'),
            text: form.getAttribute('data-detalle') || '',
            icon: peligro ? 'warning' : 'question',
            showCancelButton: true,
            confirmButtonText: form.getAttribute('data-boton') || 'Sí, continuar',
            cancelButtonText: 'Cancelar',
            reverseButtons: true,
            focusCancel: peligro,
            confirmButtonColor: peligro ? '#c0392b' : '#1f3a5f',
            cancelButtonColor: '#7b8794'
        });
    }

    document.addEventListener('submit', function (e) {
        var form = e.target;
        if (!form.hasAttribute || !form.hasAttribute('data-confirmar')) {
            return;
        }
        // Sin SweetAlert (por ejemplo, si el archivo no cargó) se usa el confirm() nativo.
        if (typeof Swal === 'undefined') {
            if (!window.confirm(form.getAttribute('data-confirmar'))) {
                e.preventDefault();
            }
            return;
        }
        e.preventDefault();
        confirmar(form).then(function (r) {
            if (r.isConfirmed) {
                // submit() no vuelve a disparar este evento, así no se pregunta dos veces.
                HTMLFormElement.prototype.submit.call(form);
            }
        });
    });

    /** Aviso simple (reemplaza alert()). */
    window.avisar = function (titulo, texto) {
        if (typeof Swal === 'undefined') {
            window.alert(titulo + (texto ? '\n' + texto : ''));
            return;
        }
        Swal.fire({ title: titulo, text: texto || '', icon: 'error', confirmButtonColor: '#1f3a5f' });
    };
})();
