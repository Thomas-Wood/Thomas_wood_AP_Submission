package uk.ac.bournemouth.ap.dotsandboxes

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_game.*
import org.example.student.dotsboxgame.CustomComputerPlayer
import org.example.student.dotsboxgame.CustomHumanPlayer
import org.example.student.dotsboxgame.StudentDotsBoxGame
import uk.ac.bournemouth.ap.dotsandboxeslib.DotsAndBoxesGame
import uk.ac.bournemouth.ap.dotsandboxeslib.Player
import kotlin.random.Random

/** This activity contains the game grid and the current player view.
 * It also controls when to show the win screen **/

class GameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val data = intent.extras

        if (data != null) {
            //Pull the data out of the intent
            val width = data.getInt("DotsAndBoxesMainActivityWidth")
            val height = data.getInt("DotsAndBoxesMainActivityHeight")
            val humanPlayers = data.getInt("DotsAndBoxesMainActivityHumanPlayers")
            val computerPlayers = data.getInt("DotsAndBoxesMainActivityComputerPlayers")
            val difficulty = data.getString("DotsAndBoxesMainActivityDifficulty")

            //All human players go first, followed by all computer players
            val players = mutableListOf<Player>()
            for (humans in 0 until humanPlayers) {
                val humanNumber = humans+1
                val name = "Human $humanNumber"
                val playerColor = Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
                players.add(CustomHumanPlayer(name, playerColor))
            }
            for (computers in 0 until computerPlayers) {
                val computerNumber = computers+1
                val name = "Computer $computerNumber"
                val playerColor = Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
                players.add(CustomComputerPlayer(name, playerColor, difficulty))
            }

            //Create the game logic and update the gameView with this new game
            val game = StudentDotsBoxGame(width, height, players)
            this.gameView.redefineGame(game)

            //Add the current player listener
            this.gameView.game.addOnGameChangeListener(this.currentPlayerView.currentPlayerListener)
            this.gameView.game.fireGameChange() //Gives the currentPlayer view the starting player

            //Add the game over listener
            this.gameView.game.addOnGameOverListener(this.GameOverListener())
        }

    }

    inner class GameOverListener: DotsAndBoxesGame.GameOverListener {
        override fun onGameOver(game: DotsAndBoxesGame, scores: List<Pair<Player, Int>>) {
            //Find winning player
            var winningPlayerName = "Unknown"
            var winningPlayerScore = 0
            var currentPlayer: Player?

            //Look at every score
            for (pair in scores) {
                currentPlayer = pair.first

                //If a new leading player is found
                if (pair.second > winningPlayerScore) {

                    //Update the name
                    if (currentPlayer is CustomHumanPlayer) {
                        winningPlayerName = currentPlayer.name
                    } else if (currentPlayer is CustomComputerPlayer) {
                        winningPlayerName = currentPlayer.name
                    }

                    //Update the score
                    winningPlayerScore = pair.second
                } else if (pair.second == winningPlayerScore) { //If there is another score the same
                    //Add the new name to the current name
                    if (currentPlayer is CustomHumanPlayer) {
                        winningPlayerName += "\n" + currentPlayer.name
                    } else if (currentPlayer is CustomComputerPlayer) {
                        winningPlayerName += "\n" + currentPlayer.name
                    }
                }
            }

            //Start the win screen activity
            val contentIdentifier = "GameOver"
            val intent = Intent(this@GameActivity, WinningScreenActivity::class.java).apply {
                putExtra(contentIdentifier + "winnerName", winningPlayerName)
                putExtra(contentIdentifier + "winnerScore", winningPlayerScore)
            }
            startActivity(intent)
        }
    }

}
