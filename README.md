# Thomas_wood_AP_Submission
 The final submission for the dots and boxes android application for BU. Coded by Thomas Wood.

This file contains a list of extension features to the dots and boxes game

Logic (StudentDotsBoxGame.kt)
- Supports multiple human and computer players, including no computer players
- Created a 'CustomHumanPlayer' which extends 'HumanPlayer' which has the attribute 'name' and 'color'
- Created a 'CustomComputerPlayer' which extends 'ComputerPlayer'. It also has the attributes 'name' and
	'color' and also 'difficultyLevel'. It has the overriden function 'makeMove' which can call 
	three different difficulties, depending on the difficulty selected:
	- makeEasyMove
		This will pick a random line that is not already drawn
	- makeMediumMove
		This will attempt to pick a line that will cause a box to be completed, if none are
		available, it reverts to makeEasyMove
	- makeHardMove
		This will attempt to pick a line that will cause a box to be completed, if none are
		available, it will pick a line that will not cause a box to have three bounding lines,
		if this is not possible, it will pick a random line not already drawn.

User Inteface
- MainActivity
	Validation for all inputs
- WinningScreenActivity
	Shows a winning screen displaying the name of the player that won and their score.
	Also works for draws (equal points between leading players).
- CurrentPlayerView
	This adds a bar at the top of the game which is the colour of the current player with their
	name written on top so you know who's turn it is.
- GameView
	The game uses pointed lines that slot together for a nicer look than dots and lines.
	The latest line to be drawn is shown in green which helps see where the last player ended
	their turn. The boxes will vary in size to fit the screen while always being square