package uk.ac.bournemouth.ap.dotsandboxes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView

class WinningScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_winning_screen)

        val data = intent.extras

        if (data != null) {
            val name = data.getString("GameOverwinnerName")
            val score = data.getInt("GameOverwinnerScore")

            findViewById<TextView>(R.id.playerName).text = name
            findViewById<TextView>(R.id.winnerPoints).text = score.toString()
        }
    }

    fun playAgain(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}
