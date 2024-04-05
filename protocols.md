### Protocol Overview:

1. **Client Registration:**

   - **Request:** `REGISTER <player_name>`
   - **Response (Success):** `REGISTRATION_SUCCESS <player_name> <order>`
   - **Response (Failure):** `REGISTRATION_FAILURE <reason>`. Reasons: `Name already taken`, `Game already started`, `Game already finished`, `Invalid name`.

2. **Game Start:**

   - **Notification:** `GAME_START <player_order> <player1_name> <player2_order> <player2_name> ...`

3. **Game Loading:**

   - **Notification:** `GAME_LOADING <keyword_length> <hint> NEXT_TURN <player_name>`
   - **Request:** `GUESS <guess_character> <guess_word>`

4. **Gameplay:**

   - **Response (Correct Guess):** `CORRECT_GUESS <occurrences> NEXT_TURN <next_player>`
   - **Response (Wrong Guess):** `WRONG_GUESS <reason> NEXT_TURN <next_player>`
   - **Response (Correct Keyword):** `CORRECT_KEYWORD <keyword> Congratulations! You guessed the whole word correctly.`
   - **Response (Wrong Keyword):** `WRONG_KEYWORD DISABLE_PLAYER <reason> NEXT_TURN <next_player>`

5. **Score Update:**

   - **Notification:** `SCORE_UPDATE <player_name> <points>`

6. **Game End:**
   - **Notification:** `GAME_END <reason>`. Possible reasons: `Keyword guessed correctly`, `All players have guessed the keyword incorrectly`, `Some players have left the game`.
   - **Notification:** `FINAL_SCORE <player1_name> <points1> <player2_name> <points2> ...`

### Example:

1. **Client Registration:**

   - **Request:** `REGISTER Alice`
     - **Response (Success):** `REGISTRATION_SUCCESS Alice 1`
     - **Response (Failure):** `REGISTRATION_FAILURE Name already taken`

2. **Game Start:**

   - **Notification:** `GAME_START 1 Alice 2 Bob 3 Charlie`

3. **Game Loading:**

   - **Notification:** `GAME_LOADING 6 One of the most popular interpreter programming languages. NEXT_TURN Alice`
   - **Request:** `GUESS p python`

4. **Gameplay:**

   - **Response (Correct Guess):** `CORRECT_GUESS 1 NEXT_TURN Alice`
   - **Response (Wrong Guess):** `WRONG_GUESS Character 'x' is not in the keyword. NEXT_TURN Bob`
   - **Response (Correct Keyword):** `CORRECT_KEYWORD python Congratulations! You guessed the whole word correctly.`
   - **Response (Wrong Keyword):** `WRONG_KEYWORD DISABLE_PLAYER You guessed the whole word incorrectly. NEXT_TURN Bob`

5. **Score Update:**

   - **Notification:** `SCORE_UPDATE Alice 5`

6. **Game End:**
   - **Notification:** `GAME_END Keyword guessed correctly.`
   - **Notification:** `FINAL_SCORE Alice 10 Bob 8 Charlie 6`
