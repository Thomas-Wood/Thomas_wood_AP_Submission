package uk.ac.bournemouth.ap.dotsandboxes

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import org.example.student.dotsboxgame.CustomComputerPlayer
import org.example.student.dotsboxgame.CustomHumanPlayer
import org.example.student.dotsboxgame.StudentDotsBoxGame
import uk.ac.bournemouth.ap.dotsandboxeslib.DotsAndBoxesGame

/** This view shows the current player's name and color so that the user knows who's turn it is **/

class CurrentPlayerView: View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr)

    val currentPlayerListener: GameChangeListener = GameChangeListener()

    var currentPlayerPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = Color.YELLOW
    }
    var currentPlayerName = "Player name not found"

    private val textPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }

    inner class GameChangeListener: DotsAndBoxesGame.GameChangeListener {
        override fun onGameChange(game: DotsAndBoxesGame) {
            //Update the current player details
            if (game is StudentDotsBoxGame) {
                val currentPlayer = game.currentPlayer

                if (currentPlayer is CustomHumanPlayer) {
                    currentPlayerPaint.color = currentPlayer.color
                    currentPlayerName = currentPlayer.name

                } else if (currentPlayer is CustomComputerPlayer) {
                    currentPlayerPaint.color = currentPlayer.color
                    currentPlayerName = currentPlayer.name
                }
            }
            //A tell the screen to update
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val canvasWidth = width.toFloat()
        val canvasHeight = height.toFloat()

        val offset = canvasWidth/10

        textPaint.textSize = canvasWidth/10

        //Draw the background color of the player
        canvas.drawRect(offset, offset, canvasWidth-offset,
                        (canvasHeight-(offset*1.5)).toFloat(), currentPlayerPaint)

        //Write the player's name
        canvas.drawText(currentPlayerName, canvasWidth/2, canvasHeight/2, textPaint)
    }

}