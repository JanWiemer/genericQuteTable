import 'htmx.org';
//import lucene from 'lucene-query-parser';

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
// Application Menu (left side)
//====================================================================
document.addEventListener('click', function (evt) {
    if (!evt.target.classList.contains('menu-node')) {
        return;
    }
    const clickedElement = evt.target.closest('.menu-item');
    const isOpening = !clickedElement.classList.contains('open');
    const level = clickedElement.dataset.level;
    const path = clickedElement.dataset.path;
    const siblings = document.querySelectorAll(`.menu-item[data-level="${level}"][data-parent="${clickedElement.dataset.parent}"]`);
    siblings.forEach(sib => { // --- close all siblings
        sib.classList.remove('open');
        closeMenuChildren(sib.dataset.path);
    });
    if (isOpening) {  // --- Open selected element and show children
        clickedElement.classList.add('open');
        const children = document.querySelectorAll(`.menu-item[data-parent="${path}"]`);
        children.forEach(child => child.style.display = 'block');
    }
});

function closeMenuChildren(parentPath) {
    const children = document.querySelectorAll(`.menu-item[data-parent="${parentPath}"]`);
    children.forEach(child => {
        child.style.display = 'none';
        child.classList.remove('open');
        closeMenuChildren(child.dataset.path);
    });
}

//====================================================================
// Sorting the main table
//====================================================================
let sortOrder = 1;
let lastIdx = -1;
document.addEventListener('click', function (evt) {
    if (!evt.target.classList.contains('main-th')) {
        return;
    }
    const idx = evt.target.dataset.colidx;
    const tbody = document.getElementById("genericTableBody");
    const rows = Array.from(tbody.rows);
    sortOrder = (lastIdx === idx) ? sortOrder * -1 : 1;
    lastIdx = idx;
    let sortColumn = document.querySelectorAll("th")[idx];
    document.querySelectorAll("th").forEach(th => th.classList.remove("asc", "desc"));
    sortColumn.classList.add(sortOrder === 1 ? "asc" : "desc");
    document.querySelector("input[id='tableSortColumnIdx']").value = idx;
    document.querySelector("input[id='tableSortColumnName']").value = sortColumn.innerText;
    document.querySelector("input[id='tableSortDirection']").value = (sortOrder === 1 ? "asc" : "desc");
    document.querySelector("input[id='tableSortSpec']").value = (sortOrder === 1 ? "ASC" : "DESC") + " by " + sortColumn.innerText;
    rows.sort((a, b) => sortOrder * a.cells[idx].innerText.localeCompare(b.cells[idx].innerText));
    rows.forEach(r => tbody.appendChild(r));
});


//====================================================================
// Filtering the main table
//====================================================================
// Autocompletion column filter names
document.addEventListener("keydown", function (e) {
    if (e.target.id !== "tableSearchField") {
        return;
    }
    if (e.ctrlKey && e.code === "Space") {
        e.preventDefault();
        const val = e.target.value;
        const lastWordMatch = val.match(/([\w:]+)$/);
        const lastWord = lastWordMatch ? lastWordMatch[0].toLowerCase() : "";
        if (lastWord.startsWith(":") && !lastWord.includes("=") && !lastWord.includes("~")) {
            const columnPrefix = lastWord.substring(1).trim();
            const table = document.getElementById('genericTable');
            const matchingCols = Array.from(table.querySelectorAll('th'))
                .map(th => th.innerText.trim())
                .filter(s => s.startsWith(columnPrefix));
            if (matchingCols !== undefined && matchingCols.length === 1) {
                e.target.value = val.substring(0, val.length - columnPrefix.length) + matchingCols[0];
                localFilterTable();
            }
        }
    }
});
document.addEventListener("keyup", function (e) {
    if (e.target.id !== "tableSearchField") {
        return;
    }
    localFilterTable();
});
// Filtering locally on client
function localFilterTable() {
    const table = document.getElementById('genericTable');
    const headers = Array.from(table.querySelectorAll('th'));
    const colMap = headers.reduce((map, th, index) => {
        const name = th.innerText.trim();
        map[name] = index;
        return map;
    }, {});
    const filterQuery = document.getElementById("tableSearchField").value;
    const rows = document.querySelectorAll("#genericTableBody tr");
    const conditions = filterQuery.split(" & ").map(c => c.trim()).filter(c => c !== "");
    rows.forEach(row => {
        let matchesFilter = true;
        for (const condition of conditions) {
            if (!condition.startsWith(":")) { // Normal full-text search in all columns
                if (!row.innerHTML.includes(condition)) {
                    matchesFilter = false;
                    break;
                }
            } else if (condition.includes("=")) { // column filter with normal includes
                const [columnKey, filterPattern] = condition.substring(1).split("=").map(s => s.trim());
                const columnIdx = colMap[columnKey.toLowerCase()];
                if (columnIdx !== undefined && filterPattern !== "") {
                    const columnCellText = row.cells[columnIdx].innerText;
                    if (!columnCellText.includes(filterPattern)) {
                        matchesFilter = false;
                        break;
                    }
                }
            } else if (condition.includes("~")) { // column filter with regexp search
                const [columnKey, filterPattern] = condition.substring(1).split("~").map(s => s.trim());
                const columnIdx = colMap.get(columnKey.toLowerCase());
                if (columnIdx !== undefined && filterPattern !== "") {
                    const columnCellText = row.cells[columnIdx].innerText;
                    let matchesCondition = true;
                    try {
                        matchesCondition = new RegExp(filterPattern).test(columnCellText);
                    } catch (e) {
                        matchesCondition = columnCellText.includes(filterPattern);
                    }
                    if (!matchesCondition) {
                        matchesFilter = false;
                        break;
                    }
                }
            }
        }
        row.style.display = matchesFilter ? "" : "none";
    });
}

