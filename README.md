# GuessifyGame

### By Omar, Bashar, Momen (2024)

**Guessify**  
An educational game about Palestinian heritage, culture, and history. The game is a multiplayer experience where you can create a room or join an existing room. Once at least two players join, the game will start automatically after 30 seconds.

This is where the AI integration plays a crucial role: we used OpenAI's ChatGPT API to generate questions for each round. These questions focus on Palestinian history, heritage, or culture, varying in pattern every time.

Each round lasts **60 seconds** and offers **6 multiple-choice options**. Players earn points based on the time taken to answer and the correctness of their answers. A **hint system** allows players to use one of their points to receive a clue from ChatGPT about the question.

After 5 rounds, the game concludes by declaring the winner.

**We used AWS EC2 and other tools to host the game online, making it accessible to everyone. This game was developed as part of a paid backend internship at Co.Te.De Technology.**

---

## Key Features

- **Multiplayer rooms**: Create or join rooms to play with friends.
- **AI-generated questions**: Interactive and educational questions about Palestinian heritage and history.
- **Time-based scoring**: Earn more points by answering quickly and accurately.
- **Hint system**: Use points to get hints for tricky questions.
- **Dynamic gameplay**: Every round is unique due to AI-generated content.

---

## **Purpose of the Project**

The primary focus of this project was to integrate **WebSocket** for real-time communication and the **OpenAI API** for generating questions dynamically.

⚠️ **Note**:  
This internship focused solely on backend development, not frontend. We used basic **HTML**, **CSS**, and **JavaScript** to visualize the game, but we are not specialized in frontend development. Expect a functional, but not highly polished, interface.

---

## **How to Clone and Use the Project**

1. Clone the repository:

   ```bash
   git clone https://github.com/Omarjabari007/Guessify.git
   cd Guessify

   ```

2. Set up your environment:

Ensure you have Java, Maven, and Docker installed.
Create a .env file to add your OpenAI API key and other configurations.
Run the backend server:

```bash
mvn spring-boot:run
Access the game:
Open the browser and navigate to the provided local or deployed URL.
```

**Game Workflow**

Here’s an overview of the game from start to finish:

1. Signup & Signin Page

2. Home Page

3. Waiting Room

4. Inside the Game

5. Declare the Winner
