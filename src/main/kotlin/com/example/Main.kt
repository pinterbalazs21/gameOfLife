package com.example

import javafx.application.Application

import javafx.animation.AnimationTimer
import javafx.beans.value.ChangeListener
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.stage.Stage
import kotlin.math.min
import kotlin.random.Random

class GameOfLifeApp : Application() {
    private val width = 10
    private val height = 10
    private var cellSize = 20.0
    private val grid = Array(height) { BooleanArray(width) }
    private val rectangles = Array(height) { Array(width) { Rectangle(cellSize, cellSize) } }

    override fun start(primaryStage: Stage) {
        val pane = Pane()

        initGridAndRectangles(pane)
        val scene = Scene(pane, width * cellSize, height * cellSize)
        val sizeChangeListener = ChangeListener<Number> { _, _, _ ->
            val newCellSize = min(scene.width / width, scene.height / height)
            updateCellSize(newCellSize)
        }

        scene.widthProperty().addListener(sizeChangeListener)
        scene.heightProperty().addListener(sizeChangeListener)

        primaryStage.apply {
            title = "Conway's Game of Life"
            this.scene = scene
            show()
        }

        val timer = object : AnimationTimer() {
            private var lastUpdate: Long = 0

            override fun handle(now: Long) {
                if (now - lastUpdate >= 500_000_000) {
                    nextGeneration()
                    lastUpdate = now
                }
            }
        }

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

                if (grid[i][j]) {
                    newGrid[i][j] = liveNeighbors == 2 || liveNeighbors == 3
                } else {
                    newGrid[i][j] = liveNeighbors == 3
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
