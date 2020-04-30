package uk.ac.bournemouth.ap.dotsandboxes

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import org.example.student.dotsboxgame.CustomComputerPlayer
import org.example.student.dotsboxgame.CustomHumanPlayer

import org.example.student.dotsboxgame.StudentDotsBoxGame
import uk.ac.bournemouth.ap.dotsandboxeslib.DotsAndBoxesGame
import java.lang.IllegalStateException
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** This view contains the grid that shows the state of the game. It has boxes and lines. **/

class GameView : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr)

    //set the colours of the game
    private val undrawnLineColor: Int = Color.GRAY
    private val drawnLineColor: Int = Color.BLUE
    private val lastDrawnLineColor: Int = Color.GREEN
    private val unClaimedBoxColor: Int = Color.LTGRAY

    private var undrawnLinePaint: Paint
    private var drawnLinePaint: Paint
    private var lastDrawnLinePaint: Paint
    private var currentBoxesPaint: Paint
    private var currentLinesPaint: Paint

    private val myGestureDetector = GestureDetector(context, MyGestureListener())

    //Create a basic game so it is never null
    var game: StudentDotsBoxGame = StudentDotsBoxGame(5,5)

    private var gridWidth = game.boxes.maxWidth
    private var gridHeight = game.boxes.maxHeight

    private var lineLength: Float = 0f
    private var pointWidth: Float = 0f

    private var lastTurnsLines: List<StudentDotsBoxGame.StudentLine> = game.lines.toList()
    private var lastLineX: Int? = null
    private var lastLineY: Int? = null

    private val myGameChangeListener = GameChangeListener()

    init {
        undrawnLinePaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            color = undrawnLineColor
        }
        drawnLinePaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            color = drawnLineColor
        }
        lastDrawnLinePaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            color = lastDrawnLineColor
        }
        currentBoxesPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            color = unClaimedBoxColor
        }
        currentLinesPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            color = undrawnLineColor
        }
    }

    //Assign a new game and re-add the listeners
    fun redefineGame(newGame: StudentDotsBoxGame) {
        game = newGame
        game.addOnGameChangeListener(myGameChangeListener)
        lastTurnsLines = game.lines.toList()
        gridWidth = game.boxes.maxWidth
        gridHeight = game.boxes.maxHeight
    }

    //Capture the touch event
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return myGestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    //Decode the gesture into what to do
    inner class MyGestureListener: GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        //Draw the line the user touched
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val line = getLineTouched(e.x, e.y)
            try {
                line.drawLine()
            } catch(e: IllegalArgumentException) {
                Toast.makeText(context,
                               "Line is already drawn",
                               Toast.LENGTH_LONG).show()
            }
            return true
        }
    }

    //Update the screen on game change
    inner class GameChangeListener: DotsAndBoxesGame.GameChangeListener {
        override fun onGameChange(game: DotsAndBoxesGame) {
            val gameCopy = game as StudentDotsBoxGame
            updateLastLineDrawn(gameCopy.lines.toList())
            invalidate()
        }
    }

    //Get the line object that was touched from the screen coordinates
    fun getLineTouched(xCoord: Float, yCoord: Float): DotsAndBoxesGame.Line {
        val canvasWidth = width.toFloat()
        val canvasHeight = height.toFloat()

        val startingX = (canvasWidth-(lineLength*gridWidth)-(pointWidth*2))/2
        val startingY = (canvasHeight-(lineLength*gridHeight)-(pointWidth*2))/2

        val distanceBetweenLineCentres = lineLength/2

        var lineX: Int
        var lineY: Int

        //The line centre the Y coordinate is closest to
        //This is already the correct coordinate
        lineY = ((yCoord-startingY)/distanceBetweenLineCentres).roundToInt()
        if (lineY > (gridHeight*2)) {
            lineY = gridHeight*2
        } else if (lineY < 0) {
            lineY = 0
        }

        //The line centre the X coordinate is closest to
        val lineColumn = ((xCoord-startingX)/distanceBetweenLineCentres).roundToInt()
        //Turn this into a lines array coordinate
        if (lineColumn % 2 == 0) {
            lineX = lineColumn/2
        } else {
            lineX = (lineColumn-1)/2
        }

        if (lineX < 0) { //If user clicked off left of grid
            lineX = 0
        } else if (lineX > gridWidth ||
            (lineX == gridWidth && lineY % 2 == 0)) { //If user clicked right of grid
            if (lineY % 2 == 0) {
                lineX = gridWidth-1
            } else {
                lineX = gridWidth
            }
        }

        return game.lines[lineX, lineY]
    }

    //Update the coordinates of the last line to be drawn
    fun updateLastLineDrawn(newLines: List<StudentDotsBoxGame.StudentLine>) {

        //Finding the line that has been changed
        val oldLines = lastTurnsLines.iterator()
        for (line in newLines) {
            if (line.isDrawn != oldLines.next().isDrawn) { //Once the difference has been found
                lastLineX = line.lineX
                lastLineY = line.lineY
            }
        }

        //Taking a deep copy of the lines list
        val tempNewLines = mutableListOf<StudentDotsBoxGame.StudentLine>()
        for (line in newLines) {
            tempNewLines.add(line.copy())
        }

        //Update the list ready for the next turn
        lastTurnsLines = tempNewLines.toList()
    }

    //Provides the paint to be used to draw a line
    private fun getLinePaint(currentLine: StudentDotsBoxGame.StudentLine): Paint {
        if (currentLine.isDrawn) {
            //If this is the latest line to be drawn
            if (currentLine.lineX == lastLineX && currentLine.lineY == lastLineY) {
                return lastDrawnLinePaint
            } else { //Otherwise draw a normal drawn line
                return drawnLinePaint
            }
        } else { //set the undrawn colour
            return undrawnLinePaint
        }
    }

    //Provides the paint to be used to draw a box
    private fun getBoxPaint(box: StudentDotsBoxGame.StudentBox): Paint {
        val owningPlayer = box.owningPlayer

        //The box is owned
        if (owningPlayer != null) {

            if (owningPlayer is CustomHumanPlayer) {
                currentBoxesPaint.color = owningPlayer.color
            } else if (owningPlayer is CustomComputerPlayer) {
                currentBoxesPaint.color = owningPlayer.color
            } else {
                throw IllegalStateException("Player has no color attribute")
            }

        } else {
            currentBoxesPaint.color = unClaimedBoxColor
        }
        return currentBoxesPaint
    }

    //Draw the grid with the correct colored lines and boxes
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val canvasWidth = width.toFloat()
        val canvasHeight = height.toFloat()

        //Work out the length of a line based on the grid size and width available
        val lineLengthBasedOnWidth = (canvasWidth / (gridWidth + 0.125)).toFloat()
        val lineLengthBasedOnHeight = (canvasHeight / (gridHeight + 0.125)).toFloat()

        //Pick the smaller line length so it fits on the screen
        //The line length runs from the point of one end to the point of the other
        if (lineLengthBasedOnWidth <= lineLengthBasedOnHeight) {
            lineLength = lineLengthBasedOnWidth
        } else {
            lineLength = lineLengthBasedOnHeight
        }

        //The point on the ends of each line
        pointWidth = lineLength/16

        //Centres the grid horizontally
        val xStartPoint = (canvasWidth-(lineLength*gridWidth)-(pointWidth*2))/2
        var xOffset: Float
        var yOffset = (canvasHeight-(lineLength*gridHeight)-(pointWidth*2))/2 //Centres the grid vertically

        //Create iterators for the lines and boxes
        val lines = game.lines.iterator()
        val boxes = game.boxes.iterator()

        for (row in 0 until (gridHeight*2)+1) { //For every row in the lines array

            //if row index is even (1st, 3rd, 5th...) then it's a horizontal line
            if (row % 2 == 0) {
                xOffset = xStartPoint + pointWidth

                //for every line to draw on this row
                for (line in 0 until gridWidth) {

                    //get the paint color to use for the line
                    currentLinesPaint = getLinePaint(lines.next())
                    //Draw the horizontal line
                    drawHorizontalLine(canvas, xOffset, yOffset, lineLength, currentLinesPaint)

                    //Draw a box below if this isn't the last row
                    if (row != gridHeight*2) {
                        //Get the paint color to use for the box
                        currentBoxesPaint = getBoxPaint(boxes.next())
                        //Draw the box
                        drawBox(canvas, xOffset+(pointWidth*2), yOffset+(pointWidth*3), lineLength-(pointWidth*4), currentBoxesPaint)

                    }
                    //The next line will start one line length further along
                    xOffset += lineLength
                }
                //The next set of lines (vertical) start further down as the lines slot together
                yOffset += pointWidth

            } else { //looking at vertical lines
                xOffset = xStartPoint

                //for every line to draw on this row
                for (line in 0 until (gridWidth+1)) {

                    //get the paint color to use for the line
                    currentLinesPaint = getLinePaint(lines.next())
                    //Draw the vertical line
                    drawVerticalLine(canvas, xOffset, yOffset, lineLength, currentLinesPaint)

                    //The next line will start one line length further along
                    xOffset += lineLength
                }
                //The next set of lines (horizontal) start just before the end of the vertical line
                yOffset += (lineLength-pointWidth)
            }
        }
    }

    //Draws a single horizontal line
    private fun drawHorizontalLine(canvas: Canvas, left: Float, top: Float, totalLength: Float, paintToUse: Paint) {
        val thickness = totalLength/8
        val pointWidth = totalLength/16
        val middleRectLength = totalLength*7/8

        //Draw the left point (a triangle)
        val leftSide = Path()
        leftSide.moveTo(left+0, top+(thickness/2)) //start on point
        leftSide.lineTo(left+pointWidth, top+0) //draw line to top
        leftSide.lineTo(left+pointWidth, top+thickness) //draw line to bottom
        leftSide.lineTo(left+0, top+(thickness/2)) //draw line back to start
        leftSide.close()
        canvas.drawPath(leftSide, paintToUse)

        //Draw the centre part (a rectangle)
        //The +1 and -1 cover the 1 pixel gap between the rectangle and the points
        canvas.drawRect(left+pointWidth-1, top+0, left+pointWidth+middleRectLength+1, top+thickness, paintToUse)

        //Draw the right point (a triangle)
        val rightSide = Path()
        rightSide.moveTo(left+totalLength, top+(thickness/2)) //start on point
        rightSide.lineTo(left+pointWidth+middleRectLength, top+0) //draw line to top
        rightSide.lineTo(left+pointWidth+middleRectLength, top+thickness) //draw line to bottom
        rightSide.lineTo(left+totalLength, top+(thickness/2)) //draw line back to start
        rightSide.close()
        canvas.drawPath(rightSide, paintToUse)
    }

    //Draws a single vertical line
    private fun drawVerticalLine(canvas: Canvas, left: Float, top: Float, totalLength: Float, paintToUse: Paint) {
        val thickness = totalLength/8
        val pointWidth = totalLength/16
        val middleRectLength = totalLength*7/8

        //Draw the top point (a triangle)
        val topPoint = Path()
        topPoint.moveTo(left+(thickness/2), top+0) //start on point
        topPoint.lineTo(left+0 ,top+pointWidth) //draw line to lower left
        topPoint.lineTo(left+thickness, top+pointWidth) //draw line to lower right
        topPoint.lineTo(left+(thickness/2), top+0) //draw line back to start
        topPoint.close()
        canvas.drawPath(topPoint, paintToUse)

        //Draw the centre part (a rectangle)
        //The +1 and -1 cover the 1 pixel gap between the rectangle and the points
        canvas.drawRect(left+0, top+pointWidth-1, left+thickness, top+pointWidth+middleRectLength+1, paintToUse)

        //Draw to bottom point (a triangle)
        val bottomPoint = Path()
        bottomPoint.moveTo(left+thickness/2, top+totalLength) //start on point
        bottomPoint.lineTo(left+0, top+pointWidth+middleRectLength) //draw line to upper left
        bottomPoint.lineTo(left+thickness, top+pointWidth+middleRectLength) // draw line to upper right
        bottomPoint.lineTo(left+thickness/2, top+totalLength) //draw line back to start
        bottomPoint.close()
        canvas.drawPath(bottomPoint, paintToUse)
    }

    //Draws a single box
    private fun drawBox(canvas: Canvas, left: Float, top: Float, totalLength: Float, paintToUse: Paint) {
        canvas.drawRect(left, top, left+totalLength, top+totalLength, paintToUse)
    }
}