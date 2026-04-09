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
    if("true" === sortColumn.dataset.numeric) {
      rows.sort((a, b) => sortOrder * (Number(a.cells[idx].dataset.raw) - Number(b.cells[idx].dataset.raw)));
    } else {
      rows.sort((a, b) => sortOrder * a.cells[idx].innerText.localeCompare(b.cells[idx].innerText));
    }
    rows.forEach(r => tbody.appendChild(r));
});
