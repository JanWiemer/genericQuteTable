
//====================================================================
// Fill detail area depending on selected row
//====================================================================
document.addEventListener('click', e => {
    if (!e.target.classList.contains('main-td')) {
        return;
    }
    const row = e.target.closest('.row-clickable');
    if (!row) return;
    document.querySelectorAll('.row-clickable').forEach(r => r.classList.remove('selected'));
    row.classList.add('selected');
    const details = row.dataset.details;
    const detailMap = JSON.parse(details);
    const kvList = createKvList(detailMap);
    document.getElementById('kvContent').innerHTML = '<div class="kv-grid">' + kvList + '</div>';
    //--- JSON Baum
    let output = document.getElementById('jsonContent');
    const jsonText = row.dataset.json
    try {
        output.jsonDetails = jsonText
        const parsed = JSON.parse(jsonText);
        output.innerHTML = "";
        output.appendChild(createJsonTree(parsed));
    } catch (e) {
        output.innerHTML = `<span class="error">Invalid JSON: ${e}</span>\n` + jsonText;
    }
});

function createKvList(map) {
    let html = '<div id="detailTablePane" class="detail-table">';
    html += '<table>';
    html += '<thead>';
    html += '<tr> <th>Key<div class="resizer"></div></th> <th>Value</th> </tr>';
    html += '</thead>';
    html += '<tbody>';
    for (const [key, value] of Object.entries(map)) {
        html += '<tr>'
        html += `<td class="kv-key" title="${key}">${key}</td> <td class="kv-value" title="${value}">${value}</td>`;
        html += '</tr>'
    }
    html += '</tbody>';
    html += '</table>';
    html += '</div>';
    return html;
}

function createJsonTree(data, isLast = true) {
    if (typeof data !== 'object' || data === null) {
        const valSpan = createJsonValueSpan(data);
        if (!isLast) valSpan.append(',');
        return valSpan;
    }
    const isArray = Array.isArray(data);
    const container = document.createElement('div');
    container.className = 'json-node';
    const toggle = document.createElement('span');
    toggle.className = 'toggle';
    toggle.textContent = '▼';
    container.appendChild(toggle);
    container.appendChild(document.createTextNode(isArray ? '[' : '{'));
    const list = document.createElement('ul');
    const keys = Object.keys(data);
    keys.forEach((key, index) => {
        const li = document.createElement('li');
        const lastInLevel = index === keys.length - 1;
        if (!isArray) {
            const keySpan = document.createElement('span');
            keySpan.className = 'key';
            keySpan.textContent = `"${key}": `;
            li.appendChild(keySpan);
        }
        li.appendChild(createJsonTree(data[key], lastInLevel));
        list.appendChild(li);
    });
    container.appendChild(list);
    const closingText = (isArray ? ']' : '}') + (isLast ? '\n' : ', ');
    container.appendChild(document.createTextNode(closingText));
    toggle.onclick = (e) => {
        e.stopPropagation();
        const isHidden = list.classList.toggle('hidden');
        toggle.textContent = isHidden ? '▶' : '▼';
    };
    return container;
}

function createJsonValueSpan(val) {
    const span = document.createElement('span');
    if (typeof val === 'string') {
        span.className = 'string';
        span.textContent = `"${val}"`;
    } else if (typeof val === 'number') {
        span.className = 'number';
        span.textContent = val;
    } else if (typeof val === 'boolean') {
        span.className = 'boolean';
        span.textContent = val;
    } else {
        span.className = 'null';
        span.textContent = 'null';
    }
    return span;
}


//====================================================================
// Copy JSON in Detail Area to Clipboard
//====================================================================

document.body.addEventListener('click', function (evt) {
    if (!evt.target.classList.contains('copy-json-to-clipboard')) {
        return;
    }
    const copyWrapper = evt.target.closest(".detailJsonPane");
    const copySource = copyWrapper.querySelector(".jsonContent")
    navigator.clipboard.writeText(copySource.dataset.jsonDetails)
    .catch(err => { console.error("Copy failed: ", err); });
});


