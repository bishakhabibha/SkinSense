document.querySelectorAll("[data-scroll-target]").forEach((button) => {
    button.addEventListener("click", () => {
        const target = document.querySelector(button.dataset.scrollTarget);

        if (target) {
            target.scrollIntoView({
                behavior: "smooth",
                block: "start"
            });
        }
    });
});

const assessmentForm = document.querySelector(".assessment-form");
const loadingPage = document.querySelector("#loadingPage");
const loadingMessage = document.querySelector("#loadingMessage");
const loadingMessages = [
    "Analyzing your skin profile...",
    "Evaluating ingredients...",
    "Generating personalized routine...",
    "Selecting recommended products...",
    "Preparing AI report..."
];

if (assessmentForm && loadingPage && loadingMessage) {
    assessmentForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        document.body.classList.add("loading-active");
        loadingPage.hidden = false;

        let messageIndex = 0;
        const messageTimer = window.setInterval(() => {
            messageIndex = (messageIndex + 1) % loadingMessages.length;
            loadingMessage.textContent = loadingMessages[messageIndex];
        }, 1800);

        try {
            const formData = new FormData(assessmentForm);
            const urlEncodedData = new URLSearchParams();

            formData.forEach((value, key) => {
                urlEncodedData.append(key, value);
            });

            const response = await fetch(assessmentForm.action, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
                },
                body: urlEncodedData
            });

            const html = await response.text();
            document.open();
            document.write(html);
            document.close();
        } catch (error) {
            window.clearInterval(messageTimer);
            loadingMessage.textContent = "Could not reach the server. Please check that the app is running and try again.";
        }
    });
}

document.querySelectorAll(".timeline-scroll-shell").forEach((shell) => {
    const scroller = shell.querySelector(".timeline-scroll");
    const track = shell.querySelector(".timeline-scrollbar");
    const thumb = shell.querySelector(".timeline-scrollbar-thumb");

    if (!scroller || !track || !thumb) {
        return;
    }

    const minimumThumbHeight = 36;
    let thumbHeight = minimumThumbHeight;
    let maximumThumbTop = 0;
    let dragStartY = 0;
    let dragStartScrollTop = 0;

    const updateScrollbar = () => {
        const visibleHeight = scroller.clientHeight;
        const contentHeight = scroller.scrollHeight;
        const scrollRange = Math.max(contentHeight - visibleHeight, 0);
        const trackHeight = track.clientHeight;
        const isScrollable = scrollRange > 1 && trackHeight > 0;

        shell.classList.toggle("is-scrollable", isScrollable);
        if (!isScrollable) {
            thumb.style.height = "0px";
            thumb.style.transform = "translateY(0)";
            return;
        }

        thumbHeight = Math.max(minimumThumbHeight, trackHeight * (visibleHeight / contentHeight));
        thumbHeight = Math.min(thumbHeight, trackHeight);
        maximumThumbTop = Math.max(trackHeight - thumbHeight, 0);

        const scrollProgress = scroller.scrollTop / scrollRange;
        thumb.style.height = `${thumbHeight}px`;
        thumb.style.transform = `translateY(${scrollProgress * maximumThumbTop}px)`;
    };

    const scrollToTrackPosition = (pointerY) => {
        const trackBounds = track.getBoundingClientRect();
        const targetThumbTop = Math.min(
            Math.max(pointerY - trackBounds.top - (thumbHeight / 2), 0),
            maximumThumbTop
        );
        const scrollRange = scroller.scrollHeight - scroller.clientHeight;
        scroller.scrollTop = maximumThumbTop > 0
            ? (targetThumbTop / maximumThumbTop) * scrollRange
            : 0;
    };

    scroller.addEventListener("scroll", updateScrollbar, { passive: true });

    scroller.addEventListener("keydown", (event) => {
        const lineDistance = 48;
        const pageDistance = scroller.clientHeight * 0.85;
        const keyboardScroll = {
            ArrowDown: lineDistance,
            ArrowUp: -lineDistance,
            PageDown: pageDistance,
            PageUp: -pageDistance
        };

        if (event.key === "Home") {
            event.preventDefault();
            scroller.scrollTop = 0;
        } else if (event.key === "End") {
            event.preventDefault();
            scroller.scrollTop = scroller.scrollHeight;
        } else if (Object.hasOwn(keyboardScroll, event.key)) {
            event.preventDefault();
            scroller.scrollTop += keyboardScroll[event.key];
        }
    });

    track.addEventListener("pointerdown", (event) => {
        if (event.target === thumb || !shell.classList.contains("is-scrollable")) {
            return;
        }
        event.preventDefault();
        scrollToTrackPosition(event.clientY);
    });

    thumb.addEventListener("pointerdown", (event) => {
        event.preventDefault();
        event.stopPropagation();
        dragStartY = event.clientY;
        dragStartScrollTop = scroller.scrollTop;
        shell.classList.add("is-dragging");
        document.body.classList.add("timeline-scrollbar-dragging");
        thumb.setPointerCapture(event.pointerId);
    });

    thumb.addEventListener("pointermove", (event) => {
        if (!shell.classList.contains("is-dragging")) {
            return;
        }

        const scrollRange = scroller.scrollHeight - scroller.clientHeight;
        const pointerDelta = event.clientY - dragStartY;
        scroller.scrollTop = dragStartScrollTop
            + (maximumThumbTop > 0 ? pointerDelta * (scrollRange / maximumThumbTop) : 0);
    });

    const finishDragging = (event) => {
        if (!shell.classList.contains("is-dragging")) {
            return;
        }
        shell.classList.remove("is-dragging");
        document.body.classList.remove("timeline-scrollbar-dragging");
        if (thumb.hasPointerCapture(event.pointerId)) {
            thumb.releasePointerCapture(event.pointerId);
        }
    };

    thumb.addEventListener("pointerup", finishDragging);
    thumb.addEventListener("pointercancel", finishDragging);

    const resizeObserver = new ResizeObserver(updateScrollbar);
    resizeObserver.observe(shell);
    resizeObserver.observe(scroller);

    const mutationObserver = new MutationObserver(updateScrollbar);
    mutationObserver.observe(scroller, {
        childList: true,
        subtree: true,
        characterData: true
    });

    window.addEventListener("resize", updateScrollbar, { passive: true });
    window.requestAnimationFrame(updateScrollbar);
});
