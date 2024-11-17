let stompClient = null;
let roomId = null;
let userId = null;
let username = null;
let timerInterval = null;
let roundId = null;
let roundTimerInterval = null;
let gameStarted = false;
let timerStarted = false;
let roomCapacity = null;
let hintsUsed = 0;
let soundEffectsVolume = 1.0;

//guessify..
const submitAnswerButton = document.getElementById('submit-answer');
const playersPanel = document.getElementById('user-panel');
const playersList = document.getElementById('players-list');
const roomSection = document.getElementById('room-section');
const gameSection = document.getElementById('game-section');
const roundContent = document.getElementById('round-content');
const countdownTimer = document.getElementById('countdown-timer');
const timerElement = document.getElementById('timer');
const winnerMessage = document.getElementById('winner-message');
const rejoinRoomButton = document.getElementById('rejoin-room-button');
const roomNumberInput = document.getElementById('roomNumber');
const navBar = document.getElementById('nav-bar');
const logoutButton = document.getElementById('logout-button');
const backToMainMenuButton = document.getElementById('back-to-main-menu-button');
const roundNumberElement = document.getElementById('round-number');
const roundTimerElement = document.getElementById('round-timer');
const hintButton = document.getElementById('give-hint-button');

const backgroundMusic = new Audio('../static/sounds/palestine.mp3');
backgroundMusic.loop = true;


const clickSoundPath = '../static/sounds/click-21156.mp3';
const buttons = document.querySelectorAll('button');
buttons.forEach(button => {
    button.addEventListener('click', function() {
        if (button.id === 'join-room-button') return;
        const clickSound = new Audio(clickSoundPath);
        clickSound.volume = soundEffectsVolume;
        clickSound.play();
        showFlagAnimation();
    });
});





function openSettingsModal() {
    document.getElementById('settings-modal').style.display = 'block';
}

function closeSettingsModal() {
    document.getElementById('settings-modal').style.display = 'none';
}

function enableDarkMode() {
    document.body.classList.add('dark-mode');
    document.body.classList.remove('light-mode');
}

function enableLightMode() {
    document.body.classList.add('light-mode');
    document.body.classList.remove('dark-mode');
}
function openAboutModal() {
    document.getElementById('about-modal').style.display = 'block';
}

function closeAboutModal() {
    document.getElementById('about-modal').style.display = 'none';
}




window.addEventListener('load', function() {
    username = localStorage.getItem('username');
    userId = localStorage.getItem('userId');

    if (!username || !userId) {
        window.location.href = 'sign-in.html';
    } else {
        document.getElementById('user-name').innerText = `${username}`;
        navBar.style.display = 'flex';
        roomSection.style.display = 'block';
        fetchAvailableRooms();

        const settingsButton = document.getElementById('settings-button');
        settingsButton.addEventListener('click', function() {
            openSettingsModal();
        });

        backgroundMusic.volume = soundEffectsVolume;
        backgroundMusic.play();
        const bgMusicToggle = document.getElementById('bg-music-toggle');
        bgMusicToggle.checked = true;
        bgMusicToggle.addEventListener('change', function() {
            if (bgMusicToggle.checked) {
                backgroundMusic.play();
            } else {
                backgroundMusic.pause();
            }
        });

        const aboutButton = document.getElementById('about-button');
        aboutButton.addEventListener('click', function() {
            openAboutModal();
        });


        const darkModeToggle = document.getElementById('dark-mode-toggle');
        darkModeToggle.checked = true;
        darkModeToggle.addEventListener('change', function() {
            if (darkModeToggle.checked) {
                enableDarkMode();
            } else {
                enableLightMode();
            }
        });

        const volumeControl = document.getElementById('volume-control');
        volumeControl.addEventListener('input', function() {
            const volumeValue = parseFloat(volumeControl.value) / 100;
            soundEffectsVolume = volumeValue;
            backgroundMusic.volume = volumeValue;
        });

    }
});

logoutButton.addEventListener('click', logout);

async function logout() {
    try {
        if (stompClient != null && roomId != null) {
            await fetch(`/guessify/room/remove/player?roomId=${roomId}&userId=${userId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            stompClient.disconnect();
            stompClient = null;
        }

        await fetch('/auth/logout', {
            method: 'POST'
        });

        localStorage.removeItem('username');
        localStorage.removeItem('userId');
        redirectToSignIn();
    } catch (error) {
        console.error('Error during logout:', error);
    }
}

async function redirectToSignIn() {
    try {
        const response = await fetch('/', {
            method: 'GET',
            headers: {
                'Content-Type': 'text/html'
            }
        });

        if (response.ok) {
            window.location.href = '/';
        } else {
            throw new Error(`Error: ${response.status}`);
        }
    } catch (error) {
        console.error("Error redirecting to sign-in:", error);
        alert("Unable to redirect to the sign-in page. Please try again later.");
    }
}


async function fetchAvailableRooms() {
    try {
        const response = await fetch('/guessify/rooms/available');
        if (!response.ok) throw new Error('Error fetching available rooms');

        const rooms = await response.json();
        updateRoomsList(rooms);
    } catch (error) {
        console.error('Error fetching available rooms:', error);
    }
}

function updateRoomsList(rooms) {
    const roomsList = document.getElementById('rooms-list');
    roomsList.innerHTML = '';
    rooms.forEach(room => {
        const li = document.createElement('li');
        li.className = 'room-item';
        li.innerHTML = `
            Room ${room.roomId} - Players: ${room.userCount}/${room.capacity}
            <button onclick="joinSpecificRoom(${room.roomId})">Join</button>
        `;
        roomsList.appendChild(li);
    });
}

function joinSpecificRoom(roomIdToJoin) {
    roomId = roomIdToJoin;
    joinRoom();
}

function startGame() {
    if (!roomId || !stompClient) {
        console.error('Cannot start game, roomId or stompClient is not set.');
        return;
    }
    gameStarted = true;
    stompClient.send(`/app/start-game/${roomId}`, {}, JSON.stringify({ roomId, totalRounds: 5 }));

}

function showWinnerAnimation() {
    winnerMessage.classList.add('winner-animate');

    fireConfetti();
}

function fireConfetti() {
    const duration = 5 * 1000;
    const animationEnd = Date.now() + duration;
    const defaults = { startVelocity: 30, spread: 360, ticks: 60, zIndex: 1000 };

    const interval = setInterval(function() {
        const timeLeft = animationEnd - Date.now();

        if (timeLeft <= 0) {
            return clearInterval(interval);
        }

        const particleCount = 50 * (timeLeft / duration);
        confetti(Object.assign({}, defaults, { particleCount, origin: { x: Math.random(), y: Math.random() - 0.2 } }));
    }, 250);
}


function endGame(winnerMessageText) {
    winnerMessage.innerText = winnerMessageText || 'No winner available';
    gameSection.style.display = 'none';
    submitAnswerButton.style.display = 'none';
    roundContent.innerText = '';
    submitAnswerButton.disabled = false;
    submitAnswerButton.innerText = 'Submit Answer';
    submitAnswerButton.style.color = '#E0E0E0';
    submitAnswerButton.style.backgroundColor = '#333';
    winnerMessage.style.display = 'block';
    playersList.style.display = 'block';
    roundTimerElement.style.display = 'none';
    rejoinRoomButton.style.display = 'block';
    backToMainMenuButton.style.display = 'block';
    gameStarted = false;
    const hintButton = document.getElementById('give-hint-button');
    hintButton.style.display = 'none';
    resetHintButton();
    showWinnerAnimation();
}

async function createRoom() {
    try {
        const response = await fetch(`/guessify/room/create-room?userId=${userId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok)
        {
            const data = await response.json();
            roomId = data.roomId;
            roomCapacity = data.capacity || 4;
            await joinRoom();
        }
        else {
            throw new Error('Error creating room');
        }
    } catch (error) {
        console.error('Error creating room:', error);
    }
}

function reJoinRoom() {
    winnerMessage.style.display = 'none';
    backToMainMenuButton.style.display = 'none';
    winnerMessage.innerText = '';
    rejoinRoomButton.style.display = 'none';
    joinRoom();
}

function backToMainMenu() {
    rejoinRoomButton.style.display = 'none';
    backToMainMenuButton.style.display = 'none';
    playersPanel.style.display = 'none';
    winnerMessage.style.display = 'none';
    roomSection.style.display = 'block';
    fetchAvailableRooms();
}

async function joinRoom() {
    if (!roomId) roomId = roomNumberInput.value;
    if (!roomId) return alert('Please enter a valid room number.');

    try {
        const joinResponse = await fetch(`/guessify/room/${roomId}/join`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId })
        });

        if (!joinResponse.ok) {
            const errorText = await joinResponse.text();
            throw new Error(errorText);
        }

        await connectToWebSocket();

        const playersResponse = await fetch(`/guessify/room/${roomId}/score`);
        const players = await playersResponse.json();
        updatePlayersList(players);

        const roomResponse = await fetch(`/guessify/room/${roomId}`);
        const roomData = await roomResponse.json();
        roomCapacity = roomData.capacity || 4;

        roomSection.style.display = 'none';
        gameSection.style.display = 'block';
        playersPanel.style.display = 'block';


    } catch (error) {
        console.error('Error joining room:', error);
        alert("Room is locked or doesn't exist!");
    }
}

async function leaveRoom() {
    try {
        if (roomId) {
            await fetch(`/guessify/room/remove/player?roomId=${roomId}&userId=${userId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (stompClient != null) {
                stompClient.disconnect();
                stompClient = null;
            }
        }

        roomId = null;
        roundContent.innerText = '';
        roundContent.style.display = 'none';
        submitAnswerButton.disabled = false;
        submitAnswerButton.innerText = 'Submit Answer';
        submitAnswerButton.style.color = '#E0E0E0';
        submitAnswerButton.style.backgroundColor = '#333';
        gameSection.style.display = 'none';
        playersPanel.style.display = 'none';
        winnerMessage.style.display = 'none';
        resetHintButton();
        const hintButton = document.getElementById('give-hint-button');
        hintButton.style.display = 'none';
        const hintStatement = document.getElementById('hint-cost-note');
        hintStatement.style.display = 'none';
        roomSection.style.display = 'block';
        gameStarted = false;
        timerStarted = false;
        clearInterval(timerInterval);
        clearInterval(roundTimerInterval);
        countdownTimer.style.display = 'none';
        roundTimerElement.style.display = 'none';
        fetchAvailableRooms();
    } catch (error) {
        console.error('Error during logout:', error);
    }
}

async function connectToWebSocket() {
    const socket = new SockJS('/guessify-websocket');
    stompClient = Stomp.over(socket);

    return new Promise((resolve, reject) => {
        stompClient.connect({}, () => {
            stompClient.subscribe(`/room/${roomId}/players`, (message) => {
                const players = JSON.parse(message.body);
                updatePlayersList(players);
            });

            stompClient.subscribe(`/user/queue/answerFeedback`, (message) => {
                const feedback = message.body;
                alert(feedback);
            });

            stompClient.subscribe(`/user/queue/errors`, (message) => {
                const errorMessage = message.body;
                alert('Error: ' + errorMessage);
            });

            stompClient.subscribe(`/room/${roomId}/question`, (message) => {
                const round = JSON.parse(message.body);
                updateRound(round);
            });

            stompClient.subscribe(`/room/${roomId}/end-game`, (message) => {
                const messageData = JSON.parse(message.body);
                endGame(messageData.winner || 'No winner available');
                roundNumberElement.style.display = 'none';
                rejoinRoomButton.style.display = 'block';
                if (stompClient != null) {
                    stompClient.disconnect();
                }
            });

            stompClient.subscribe(`/room/${roomId}/timer`, (message) => {
                const timerData = JSON.parse(message.body);
                startCountdown(timerData.totalSeconds);
            });

            stompClient.subscribe(`/room/${roomId}/timer/cancel`, (message) => {
                cancelCountdown();
            });

            stompClient.subscribe(`/room/${roomId}/round-end`, (message) => {
                submitAnswerButton.disabled = true;
                submitAnswerButton.style.backgroundColor = 'red';
                submitAnswerButton.style.color = 'white';
                submitAnswerButton.innerText = 'Round ended';
            });

            resolve();
        }, (error) => {
            console.error('WebSocket connection error:', error);
            reject(error);
        });
    });
}

function updatePlayersList(players) {
    players.sort((a, b) => b.score - a.score);
    playersList.innerHTML = '';
    players.forEach(player => {
        const li = document.createElement('li');
        li.className = 'player-item';
        li.innerHTML = `👤 ${player.name} - Score: <span class="player-score">${player.score}</span>`;
        playersList.appendChild(li);
    });
    playersList.style.display = 'flex';
    playersList.style.flexDirection = 'column';
}

function startCountdown(seconds) {
    timerStarted = true;
    let timeLeft = seconds;
    countdownTimer.style.display = 'block';
    timerElement.innerText = timeLeft;
    clearInterval(timerInterval);
    timerInterval = setInterval(() => {
        timeLeft--;
        timerElement.innerText = timeLeft;
        if (timeLeft <= 0) {
            clearInterval(timerInterval);
            countdownTimer.style.display = 'none';
            timerStarted = false;
        }
    }, 1000);
}

function cancelCountdown() {
    clearInterval(timerInterval);
    countdownTimer.style.display = 'none';
    timerStarted = false;
}

function updateRound(round) {
    roundId = round.roundId;
    gameSection.style.display = 'block';
    roundContent.innerText = round.content;
    submitAnswerButton.style.display = 'block';
    submitAnswerButton.disabled = false;
    submitAnswerButton.style.backgroundColor = '';
    resetHintButton();
    const hintButton = document.getElementById('give-hint-button');
    const hintStatement = document.getElementById('hint-cost-note');
    hintStatement.style.display = 'block';
    hintButton.style.display = 'block';
    submitAnswerButton.style.color = '';
    submitAnswerButton.innerText = 'Submit Answer';
    roundNumberElement.innerText = `Round ${round.roundNumber}`;
    roundNumberElement.style.display = 'block';
    startRoundCountdown(59);
}

function startRoundCountdown(seconds) {
    let timeLeft = seconds;
    roundTimerElement.style.display = 'block';
    roundTimerElement.innerText = `Time left: ${timeLeft} seconds`;
    if (roundTimerInterval) {
        clearInterval(roundTimerInterval);
    }
    roundTimerInterval = setInterval(() => {
        timeLeft--;
        roundTimerElement.innerText = `Time left: ${timeLeft} seconds`;
        if (timeLeft <= 0) {
            clearInterval(roundTimerInterval);
            roundTimerElement.style.display = 'none';
            submitAnswerButton.disabled = true;
            submitAnswerButton.style.backgroundColor = 'red';
            submitAnswerButton.style.color = 'white';
            submitAnswerButton.innerText = 'Time\'s up';
            const hintButton = document.getElementById('give-hint-button');
            hintButton.style.display = 'none';
        }
    }, 1000);
}

function openModal() {
    document.getElementById('answerModal').style.display = 'block';
}

function closeModal() {
    document.getElementById('answerModal').style.display = 'none';
    document.getElementById('answerInput').innerText = '';
}

async function submitAnswer() {
    const answerInput = document.getElementById('answerInput');
    const answerNumber = answerInput.value;

    if (!answerNumber) return alert("Answer cannot be empty");

    const parsedAnswerNumber = parseInt(answerNumber);
    if (isNaN(parsedAnswerNumber) || parsedAnswerNumber < 1 || parsedAnswerNumber > 6) return alert("Please enter a valid answer number.");

    try {
        stompClient.send('/app/submitAnswer', {}, JSON.stringify({
            userId, roundId, roomId, answerNumber: parsedAnswerNumber
        }));
        hintButton.style.display = 'none';
        submitAnswerButton.disabled = true;
        submitAnswerButton.style.backgroundColor = 'red';
        submitAnswerButton.style.color = 'white';
        submitAnswerButton.innerText = 'Answer submitted';
        closeModal();
    } catch (error) {
        console.error('Error submitting answer:', error);
        submitAnswerButton.disabled = false;
    }
}

function giveHint() {
    fetch(`/guessify/get-hint?roundId=${roundId}&userId=${userId}`)
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => { throw new Error(text) });
            }
            return response.text();
        })
        .then(hint => {
            hintsUsed++;
            const hintDisplay = document.getElementById('hint-display');
            hintDisplay.innerText += `\n${hint}`;

            const hintButton = document.getElementById('give-hint-button');
            if (hintsUsed >= 2) {
                hintButton.disabled = true;
                hintButton.innerText = 'No more hints';
            } else {
                hintButton.innerText = 'Give Another Hint';
            }
        })
        .catch(error => {
            console.error('Error getting hint:', error);
            alert(error.message);
        });
}

function resetHintButton() {
    hintsUsed = 0;
    const hintButton = document.getElementById('give-hint-button');
    hintButton.style.display = 'none';
    hintButton.disabled = false;
    hintButton.innerText = 'Give Hint';
    document.getElementById('hint-display').innerText = '';
}


function showFlagAnimation() {
    const flagAnimation = document.getElementById('flag-animation');
    flagAnimation.style.display = 'block';

    flagAnimation.classList.remove('animate');
    void flagAnimation.offsetWidth;
    flagAnimation.classList.add('animate');

    setTimeout(() => {
        flagAnimation.style.display = 'none';
    }, 3000);
}


const joinClickSoundPath = '../static/sounds/click-21156.mp3';

const joinRoomButton = document.getElementById('join-room-button');
joinRoomButton.addEventListener('click', function(event) {
    event.stopPropagation();

    const joinClickSound = new Audio(joinClickSoundPath);
    joinClickSound.volume = soundEffectsVolume;
    joinClickSound.play();

    joinRoom();
});
