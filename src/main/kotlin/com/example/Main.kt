package com.example

import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.beans.value.ChangeListener
import javafx.geometry.Pos
import javafx.stage.Stage
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import kotlin.math.min
import kotlin.random.Random

class GameOfLifeApp : Application() {
    private var width = 10
    private var height = 10
    private var cellSize = 20.0
    private lateinit var grid: Array<BooleanArray>
    private lateinit var rectangles: Array<Array<Rectangle>>

    override fun start(primaryStage: Stage) {
        showStartMenu(primaryStage)
    }

    private fun showStartMenu(primaryStage: Stage) {
        val vbox = VBox(10.0)
        vbox.alignment = Pos.CENTER
        val scene = Scene(vbox, 300.0, 200.0)

        val welcomeLabel = Label("Welcome to the Game of Life")
        welcomeLabel.font = Font.font("Arial", FontWeight.BOLD, 16.0)

        val widthField = TextField("10")
        val heightField = TextField("10")

        val startButton = Button("Start")
        startButton.setOnAction {

            try {
                val newWidth = widthField.text.toInt()
                val newHeight = heightField.text.toInt()
                if (newWidth in 5..30 && newHeight in 5..30) {
                    this.width = newWidth
                    this.height = newHeight
                    grid = Array(height) { BooleanArray(width) }
                    rectangles = Array(height) { Array(width) { Rectangle(cellSize, cellSize) } }
                    showGame(primaryStage)
                } else {
                    val alert = Alert(Alert.AlertType.ERROR, "Grid dimensions must be between 5 and 30.", ButtonType.OK)
                    alert.showAndWait()
                }
            } catch (e: NumberFormatException) {
                val alert = Alert(Alert.AlertType.ERROR, "Grid dimensions must be integers.", ButtonType.OK)
                alert.showAndWait()
            }
        }

        vbox.children.addAll(
            welcomeLabel,
            HBox(10.0, Label("Width:"), widthField).apply { alignment = Pos.CENTER },
            HBox(10.0, Label("Height:"), heightField).apply { alignment = Pos.CENTER },
            startButton
        )

        primaryStage.title = "Conway's Game of Life"
        primaryStage.scene = scene
        primaryStage.show()

    }

    private fun showGame(primaryStage: Stage) {
        val pane = Pane()
        val pauseButton = Button("Pause")
        val stopButton = Button("Stop")
        val toolBar = ToolBar(pauseButton, stopButton)
        val root = VBox(toolBar, pane)

        val timer = object : AnimationTimer() {
            private var lastUpdate: Long = 0

            override fun handle(now: Long) {
                if (now - lastUpdate >= 500_000_000) {
                    nextGeneration()
                    lastUpdate = now
                }
            }
        }

        var paused = false
        pauseButton.setOnAction {
            paused = !paused
            if (paused) {
                timer.stop()
                pauseButton.text = "Resume"
            } else {
                timer.start()
                pauseButton.text = "Pause"
            }
        }

        stopButton.setOnAction {
            timer.stop()
            showStartMenu(primaryStage)
        }

        initGridAndRectangles(pane)
        val scene = Scene(root, width * cellSize, height * cellSize + 25)
        val sizeChangeListener = ChangeListener<Number> { _, _, _ ->
            val newCellSize = min(scene.width / width, scene.height / height)
            updateCellSize(newCellSize)
        }

        scene.widthProperty().addListener(sizeChangeListener)
        scene.heightProperty().addListener(sizeChangeListener)

        primaryStage.scene = scene

        timer.start()
    }

    private fun initGridAndRectangles(pane: Pane) {
        for (i in 0 until height) {
            for (j in 0 until width) {
                grid[i][j] = Random.nextBoolean()
                rectangles[i][j].apply {
                    x = j * cellSize
                    y = i * cellSize
                    fill = if (grid[i][j]) Color.BLACK else Color.WHITE
                    strokeWidth = 1.0
                    stroke = Color.GRAY

                    // Add a click event to the rectangle
                    setOnMouseClicked {
                        grid[i][j] = !grid[i][j]  // Flip the state in the grid
                        fill = if (grid[i][j]) Color.BLACK else Color.WHITE  // Update the color
                    }
                }
                pane.children.add(rectangles[i][j]) // Make sure to add rectangles to the pane
            }
        }
    }


    private fun updateCellSize(newCellSize: Double) {
        cellSize = newCellSize

        for (i in 0 until height) {
            for (j in 0 until width) {
                rectangles[i][j].apply {
                    x = j * cellSize
                    y = i * cellSize
                    width = cellSize
                    height = cellSize
                }
            }
        }
    }

    private fun nextGeneration() {
        val newGrid = Array(height) { BooleanArray(width) }

        for (i in 0 until height) {
            for (j in 0 until width) {
                val liveNeighbors = countLiveNeighbors(i, j)

                newGrid[i][j] = when {
                    grid[i][j] && liveNeighbors in 2..3 -> true
                    !grid[i][j] && liveNeighbors == 3 -> true
                    else -> false
                }

                rectangles[i][j].fill = if (newGrid[i][j]) Color.BLACK else Color.WHITE
            }
        }

        for (i in grid.indices) {
            grid[i] = newGrid[i].copyOf()
        }
    }
    private fun countLiveNeighbors(row: Int, col: Int): Int {
        var count = 0
        val rowRange = (row - 1)..(row + 1)
        val colRange = (col - 1)..(col + 1)

        for (i in rowRange) {
            for (j in colRange) {
                if ((i != row || j != col) && i in 0 until height && j in 0 until width && grid[i][j]) {
                    count++
                }
            }
        }

        return count
    }
}

fun main() {
    Application.launch(GameOfLifeApp::class.java)
}
