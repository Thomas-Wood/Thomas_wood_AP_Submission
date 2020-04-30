package uk.ac.bournemouth.ap.dotsandboxes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import java.lang.NumberFormatException

/** The main activity contains the main menu where the user can select the grid size, number of
 * human players, number of computer players and their difficulty.
 * It also handles the data validation from these inputs. **/


const val intentIdentifier = "DotsAndBoxesMainActivity"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    //This is run when the start game button is pressed
    fun startGame(view: View) {
        try {
            //Get all the values from the input boxes
            val width = findViewById<EditText>(R.id.width).text.toString().toInt()
            val height = findViewById<EditText>(R.id.height).text.toString().toInt()
            val humanPlayers = findViewById<EditText>(R.id.human_player_total).text.toString().toInt()
            val computerPlayers = findViewById<EditText>(R.id.computer_player_total).text.toString().toInt()
            val difficultyID = findViewById<RadioGroup>(R.id.difficulty).checkedRadioButtonId
            val difficultyLevel = findViewById<RadioButton>(difficultyID).text.toString()

            //Data validation
            if (width > 20 || height > 20) {
                Toast.makeText(this,
                               "Game sizes are limited to 20 for user experience",
                               Toast.LENGTH_LONG).show()
            } else if (humanPlayers + computerPlayers > 10) {
                Toast.makeText(
                    this,
                    "Total players are limited to 10 for user experience",
                    Toast.LENGTH_LONG).show()
            } else if (humanPlayers == 0) {
                Toast.makeText(this,
                               "There must be at least 1 human player",
                               Toast.LENGTH_LONG).show()
            } else { //Start the game activity with these values
                val intent = Intent(this, GameActivity::class.java).apply {
                    putExtra(intentIdentifier + "Width", width)
                    putExtra(intentIdentifier + "Height", height)
                    putExtra(intentIdentifier + "HumanPlayers", humanPlayers)
                    putExtra(intentIdentifier + "ComputerPlayers", computerPlayers)
                    putExtra(intentIdentifier + "Difficulty", difficultyLevel)
                }
                startActivity(intent)
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Please enter a value for each field", Toast.LENGTH_LONG).show()
        }


    }
}
