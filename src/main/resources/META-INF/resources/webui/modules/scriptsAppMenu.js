//====================================================================
// Application Menu (left side)
//====================================================================
document.addEventListener('click', function (evt) {
    if (!evt.target.classList.contains('menu-node') && !evt.target.classList.contains('menu-icon')) {
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
