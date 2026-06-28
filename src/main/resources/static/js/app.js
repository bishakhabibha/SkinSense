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
