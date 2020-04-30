package org.example.student.dotsboxgame

import uk.ac.bournemouth.ap.dotsandboxeslib.*
import uk.ac.bournemouth.ap.dotsandboxeslib.matrix.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule
import kotlin.random.Random

class StudentDotsBoxGame(columns: Int, rows: Int, players: List<Player> = listOf(HumanPlayer(), HumanPlayer())) : AbstractDotsAndBoxesGame() {

    override val players: List<Player> = players.toList()

    override val currentPlayer: Player
        get()
            {return players[currentPlayerIndex]}

    var currentPlayerIndex: Int = 0

    //Create an empty array of type StudentBox
    override val boxes: MutableMatrix<StudentBox> = MutableMatrix(columns, rows, ::StudentBox)

    //True if the line coordinates given are for a usable line
    private val lineValidate: (Int, Int) -> Boolean = { x: Int, y: Int -> !(y % 2 == 0 && x == columns)}

    //Create an empty array of type StudentLine
    override val lines: MutableSparseMatrix<StudentLine> = MutableSparseMatrix<StudentLine>(columns+1,
                                                          (rows*2)+1,
                                                          ::StudentLine,
                                                          lineValidate)

    override val isFinished: Boolean
        get() {
            for (box in boxes) {
                if (box.owningPlayer == null) {
                    return false
                }
            }
            return true
        }

    override fun playComputerTurns() {
        var current = currentPlayer
        while (current is ComputerPlayer && ! isFinished) {
            current.makeMove(this)
            current = currentPlayer
        }
    }

    //Represents a line within the game that sits between two boxes
    inner class StudentLine(lineX: Int, lineY: Int) : AbstractLine(lineX, lineY) {
        override var isDrawn: Boolean = false

        //Find the two boxes above and below or left and right of the line
        override val adjacentBoxes: Pair<StudentBox?, StudentBox?>
            get() {
                //find if the line splits two boxes vertically or horizontally
                if (this.lineY % 2 == 0) { //splits vertically (on top of each other)
                    //The coordinates the boxes should be in relation to the lines coordinates
                    val boxAboveX = this.lineX
                    val boxAboveY = (this.lineY/2) - 1
                    val boxBelowX = this.lineX
                    val boxBelowY = this.lineY/2

                    if (boxAboveY < 0) { //Top box out of bounds (line must be on top row)
                        return Pair(null, boxes[boxBelowX, boxBelowY])
                    } else if (boxBelowY >= (lines.maxHeight-1)/2) { //Bottom box out of bounds
                        return Pair(boxes[boxAboveX, boxAboveY], null)
                    } else {
                        return Pair(boxes[boxAboveX, boxAboveY], boxes[boxBelowX, boxBelowY])
                    }
                } else { //splits horizontally (side by side boxes)
                    //The coordinates the boxes should be in relation to the lines coordinates
                    val boxLeftX = this.lineX - 1
                    val boxLeftY = (this.lineY-1) / 2
                    val boxRightX = this.lineX
                    val boxRightY = (this.lineY-1) / 2

                    if (boxLeftX < 0) { //Left box out of bounds
                        return Pair(null, boxes[boxRightX, boxRightY])
                    } else if (boxRightX > lines.maxWidth-1 - 1) { //Right box out of bounds
                        return Pair(boxes[boxLeftX, boxLeftY], null)
                    } else {
                        return Pair(boxes[boxLeftX, boxLeftY], boxes[boxRightX, boxRightY])
                    }
                }
            }

        //Creates a copy of the line
        fun copy(): StudentLine {
            var newLine = StudentLine(lineX, lineY)
            newLine.isDrawn = isDrawn
            return newLine
        }

        //Draw the line, call the listeners and trigger the computer turns
        override fun drawLine() {

            var lineHasBeenDrawn = false
            if (!isDrawn) {
                isDrawn = true
                lineHasBeenDrawn = true
            } else {
                throw IllegalArgumentException("You can't draw a line that is already drawn")
            }

            var newBoxOwned = false
            val boxesToCheck = adjacentBoxes.toList()

            //Checking if the line completes a box
            for (boxIndex in 0..1) {
                val box = boxesToCheck[boxIndex]

                //If the box exists
                if (box != null) {
                    val linesToCheck = box.boundingLines
                    var allLinesDrawn = true

                    //Check if this box is surrounded by drawn lines
                    for (line in linesToCheck) {
                        if (!line.isDrawn) { //Found line not drawn
                            allLinesDrawn = false
                        }
                    }
                    //Must be surrounded by drawn lines, so claim the box
                    if (allLinesDrawn) {
                        box.owningPlayer = currentPlayer
                        newBoxOwned = true
                    }
                }
            }

            //If the game is over, call the listeners
            if (isFinished) {
                val scores = getScores()
                val results: MutableList<Pair<Player, Int>> = mutableListOf()
                for (playerIndex in players.indices) {
                    results.add(Pair(players[playerIndex], scores[playerIndex]))
                }
                fireGameOver(results)
            }

            if (newBoxOwned) {
                fireGameChange()
                //Don't change player index as they get another go
            } else {
                //Update the current player
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size //Circular list
                fireGameChange()
            }

            //Trigger any computer turns up to the next human player
            playComputerTurns()
        }
    }

    //Represents a box that is surrounded by lines and can have a owner
    inner class StudentBox(boxX: Int, boxY: Int) : AbstractBox(boxX, boxY) {

        override var owningPlayer: Player? = null

        //Get the lines that surround this box
        override val boundingLines: List<DotsAndBoxesGame.Line>
            get() {
                val boundingLinesList: MutableList<StudentLine> = ArrayList()
                //Lines returned in a clockwise direction starting from top

                //Line above
                boundingLinesList.add(lines[this.boxX, this.boxY*2])
                //Line right
                boundingLinesList.add(lines[this.boxX+1, (this.boxY*2)+1])
                //Line below
                boundingLinesList.add(lines[this.boxX, (this.boxY*2)+2])
                //Line left
                boundingLinesList.add(lines[this.boxX, (this.boxY*2)+1])

                return boundingLinesList
            }

    }
}

//A human player used by the UI which has a name and color to display
class CustomHumanPlayer(val name: String, val color: Int): HumanPlayer() {
}

//A computer player used by the UI with three levels of AI, a name and color
class CustomComputerPlayer(val name: String,
                           val color: Int,
                           private val difficultyLevel: String?): ComputerPlayer() {

    //Called by the game logic to make a move in the game
    override fun makeMove(game: DotsAndBoxesGame) {
        if (difficultyLevel == "Easy") {
            makeEasyMove(game as StudentDotsBoxGame)
        } else if (difficultyLevel == "Medium") {
            makeMediumMove(game as StudentDotsBoxGame)
        } else if (difficultyLevel == "Hard") {
            makeHardMove(game as StudentDotsBoxGame)
        } else {
            throw IllegalStateException("Difficulty level is not assigned a function")
        }
    }

    //Pick a random line that is not drawn to draw
    private fun makeEasyMove(game: StudentDotsBoxGame) {

        //Get the lines that are not drawn
        var linesToPickFrom: MutableList<DotsAndBoxesGame.Line> = mutableListOf()
        for (line in game.lines) {
            if (!line.isDrawn) {
                linesToPickFrom.add(line)
            }
        }

        //Pick one at random
        linesToPickFrom[Random.nextInt(linesToPickFrom.size)].drawLine()
    }

    //Make a complete box if it can, or pick a random line
    private fun makeMediumMove(game: StudentDotsBoxGame) {

        var linesToPickFrom: MutableList<DotsAndBoxesGame.Line> =
            getLinesFromBoxMissingOneLine(game)

        if (linesToPickFrom.size != 0) {
            linesToPickFrom[Random.nextInt(linesToPickFrom.size)].drawLine()
        } else {
            makeEasyMove(game)
        }
    }

    //Make a complete box if it can, or avoid boxes with two lines already, or pick a random line
    private fun makeHardMove(game: StudentDotsBoxGame) {

        var linesToPickFrom = getLinesFromBoxMissingOneLine(game)

        if (linesToPickFrom.size != 0) { //Claim a box is available
            linesToPickFrom[Random.nextInt(linesToPickFrom.size)].drawLine()
        } else { //Draw a line that doesn't give the next player a box
            linesToPickFrom = mutableListOf()
            for (line in game.lines) {
                if (lineSuitableForHardMove(line)) {
                    linesToPickFrom.add(line)
                }
            }
            if (linesToPickFrom.size != 0) {
                linesToPickFrom[Random.nextInt(linesToPickFrom.size)].drawLine()
            } else {
                makeEasyMove(game)
            }
        }
    }

    //Returns a list of lines that, if drawn, would make a complete box
    private fun getLinesFromBoxMissingOneLine(game: StudentDotsBoxGame): MutableList<DotsAndBoxesGame.Line> {

        var currentLines: List<DotsAndBoxesGame.Line>
        var undrawnLinesTotal: Int
        var possibleSingleLine: DotsAndBoxesGame.Line? = null
        var linesToPickFrom: MutableList<DotsAndBoxesGame.Line> = mutableListOf()

        for (box in game.boxes) {
            currentLines = box.boundingLines
            undrawnLinesTotal = 0

            //Count up the drawn lines around a box
            for (line in currentLines) {
                if (!line.isDrawn) {
                    undrawnLinesTotal += 1
                    possibleSingleLine = line
                }
            }
            if (undrawnLinesTotal == 1 && possibleSingleLine != null) {
                linesToPickFrom.add(possibleSingleLine)
            }
        }

        return linesToPickFrom

    }

    //Returns true if a line will not make a three sided box when drawn
    private fun lineSuitableForHardMove(line: StudentDotsBoxGame.StudentLine): Boolean {

        val adjacentBoxes = line.adjacentBoxes.toList()
        var drawnLinesTotal: Int

        if (line.isDrawn) {
            return false
        }

        //Looking at the boxes either side of the line
        for (box in adjacentBoxes) {
            if (box != null) {
                drawnLinesTotal = drawnLinesAroundBox(box)
                if (drawnLinesTotal == 2 || drawnLinesTotal == 3 || drawnLinesTotal == 4) {
                    return false
                }
            }
        }
        return true
    }

    //Returns the number of drawn lines around a box
    private fun drawnLinesAroundBox(box: StudentDotsBoxGame.StudentBox): Int {
        var drawnLines = 0
        for (line in box.boundingLines) {
            if (line.isDrawn) {
                drawnLines += 1
            }
        }
        return drawnLines
    }

}