import 'htmx.org';
import './modules/scriptsAppMenu.js';
import './modules/scriptsSorting.js';
import './modules/scriptsFiltering.js';
import './modules/scriptsDetailArea.js';

//====================================================================
// HTMX: show request state in status bar
//====================================================================
document.body.addEventListener('htmx:afterOnLoad', function (evt) {
    document.getElementById('requestStatus').innerText = evt.detail.xhr.status + ' ' + evt.detail.xhr.statusText;
});
document.body.addEventListener('htmx:sendError', function (_) {
    document.getElementById('requestStatus').innerText = 'Disconnected';
});
document.body.addEventListener('htmx:responseError', function (evt) {
    document.getElementById('requestStatus').innerText = evt.detail.xhr.status + ' ' + evt.detail.xhr.statusText;
});
document.body.addEventListener('htmx:beforeRequest', function (evt) {
    let params = evt.detail.requestConfig.parameters;
    document.getElementById('statusText').innerText = 'Filter: ' + params.get("filter") + ' | Sort: ' + params.get("sortCol") + ' ' + params.get("sortDir");
});


//====================================================================
// HTMX: disable elements during Load
//====================================================================
document.body.addEventListener('htmx:afterOnLoad', function () {
    if (!document.getElementById('mainTablePane')) return;
    document.getElementById('mainTablePane').classList.remove("disabled");
});
document.body.addEventListener('htmx:responseError', function () {
    if (!document.getElementById('mainTablePane')) return;
    document.getElementById('mainTablePane').classList.add("disabled");
});
document.body.addEventListener('htmx:sendError', function () {
    if (!document.getElementById('mainTablePane')) return;
    document.getElementById('mainTablePane').classList.add("disabled");
});

//====================================================================
// Resize Table Columns
//====================================================================
document.addEventListener('mousedown', function (e) {
    if (e.target.classList.contains('resizer')) {
        const resizer = e.target.closest('.resizer');
        const th = resizer.parentElement;
        const nextTh = th.nextElementSibling;
        if (!nextTh) return;
        const startX = e.pageX;
        const thStartWidth = th.offsetWidth;
        const nextThStartWidth = nextTh.offsetWidth;
        const onMouseMove = e => {
            const diff = e.pageX - startX;
            if (thStartWidth + diff > 50 && nextThStartWidth - diff > 50) {
                th.style.width = `${thStartWidth + diff}px`;
                nextTh.style.width = `${nextThStartWidth - diff}px`;
            }
        };
        const onMouseUp = () => {
            document.removeEventListener('mousemove', onMouseMove);
            document.removeEventListener('mouseup', onMouseUp);
        };
        document.addEventListener('mousemove', onMouseMove);
        document.addEventListener('mouseup', onMouseUp);
    }
});
