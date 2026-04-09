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