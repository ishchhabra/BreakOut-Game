/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package breakout;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Iterator;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

/**
 *
 * @author Ish Chhabra
 */
public class ContentPane extends Pane {
    private static final int SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
    private static final int SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
    
    private static final int BRICK_HEIGHT = 20;
    private static final int BRICK_WIDTH = 100;
    
    private static final int BRICK_SEP = 3;
    
    private static final int BRICK_Y_OFFSET = 10;
    
    private static final int PADDLE_HEIGHT = 25;
    private static final int PADDLE_WIDTH = 125;
    
    private static final int PADDLE_Y_OFFSET = 50;
    
    private static final int BALL_RADIUS = 10;
    private static final double BALL_INITIAL_VELOCITY_X = 1.00;
    private static final double BALL_INITIAL_VELOCITY_Y = 2.00;
    
    private static final Color[] colors = { 
        Color.RED,
        Color.RED,
        Color.ORANGE,
        Color.ORANGE,
        Color.YELLOW,
        Color.YELLOW,
        Color.GREEN,
        Color.GREEN,
        Color.CYAN,
        Color.CYAN,
    };
    
    private ArrayList<Rectangle> bricks = new ArrayList<>();
    
    private Rectangle paddle;
    
    private class ballProps {
        private Circle circle;
        private double velx;
        private double vely;
        
        private ballProps(Circle circle, double velx, double vely) {
            this.circle = circle;
            this.velx = velx;
            this.vely = vely;
        }
    }
    
    private ballProps ball;
    
    private boolean gameStarted = false;
    
    public ContentPane() {
        createBricks();
        createPaddle();
        createBall();
        
        this.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if(!gameStarted) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(true) {
                                if(Thread.currentThread().isInterrupted()) {
                                    break;
                                }
                                else {
                                    moveBall();
                                }
                                
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException ex) {
                                    break;
                                }
                            }
                        }
                    }).start();
                    
                    
                    gameStarted = true;
                }
            }
        });
        
        this.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                paddle.setX(e.getX());
            }
        });
    }
    
    private void createBricks() {
        int brickMaxCount = (SCREEN_WIDTH + BRICK_SEP)/(BRICK_WIDTH + BRICK_SEP);
        int startingWidth = (SCREEN_WIDTH - (brickMaxCount * BRICK_WIDTH) - ((brickMaxCount - 1) * BRICK_SEP))/2;
        
        for(int height = BRICK_Y_OFFSET, rowCount = 0; height < SCREEN_HEIGHT && rowCount < colors.length; height+= BRICK_HEIGHT, rowCount++) {
            for(int width = startingWidth, brickCount = 0; width < SCREEN_WIDTH && brickCount < brickMaxCount; width+= BRICK_WIDTH, brickCount++) {
                Rectangle brick = new Rectangle(width, height, BRICK_WIDTH, BRICK_HEIGHT);
                brick.setStroke(colors[rowCount]);
                brick.setFill(colors[rowCount]);
                bricks.add(brick);
                this.getChildren().add(brick);
                
                width += BRICK_SEP;
            }
            height += BRICK_SEP;
        }
    }
    
    private void destroyBrick(Rectangle brick, Iterator<Rectangle> it) {
        it.remove();
        if(bricks.isEmpty()) {
            Alert alert = new Alert(AlertType.INFORMATION, "Congratulations, you won the game !", ButtonType.OK);
            alert.setTitle("Game Won !");
            alert.setOnCloseRequest(new EventHandler<DialogEvent>() {
                @Override
                public void handle(DialogEvent event) {
                    System.exit(0);
                }
            });
            alert.showAndWait();
        }
        
        Platform.runLater(new Runnable() { // Since we can't run commands from another thread in JavaFX, we use JavaFX's way to add a runnable to queue to run in the specified future. Apart from this, we can't do what we want to.
            @Override
            public void run() {
                ContentPane.this.getChildren().remove(brick);
                ball.circle.setStroke(brick.getFill());
                ball.circle.setFill(brick.getFill());
            }
        });
        
    }
    
    private void createPaddle() {
        paddle = new Rectangle(SCREEN_WIDTH/2 - PADDLE_WIDTH/2, SCREEN_HEIGHT - PADDLE_HEIGHT - PADDLE_Y_OFFSET, PADDLE_WIDTH, PADDLE_HEIGHT);
        this.getChildren().add(paddle);
    }
    
    private void createBall() {
        ball = new ballProps(new Circle(SCREEN_WIDTH/2 - 300, SCREEN_HEIGHT/2, BALL_RADIUS, Color.CYAN), BALL_INITIAL_VELOCITY_X, BALL_INITIAL_VELOCITY_Y);
        this.getChildren().add(ball.circle);
    }
    
    private void moveBall() {
        ball.circle.setCenterX(ball.circle.getCenterX() + ball.velx);
        ball.circle.setCenterY(ball.circle.getCenterY() + ball.vely);
        
        Bounds ballBounds = ball.circle.getBoundsInLocal();
        
        // Check ball intersection with the paddle
        if(ballBounds.intersects(paddle.getBoundsInLocal())) {
            ball.vely *= -1;
        }
        
        // Check ball intersection with walls
        double screenWidth = SCREEN_WIDTH;
        double screenHeight = SCREEN_HEIGHT;
        
        // Bottom wall
        if(ballBounds.intersects((new Line(0, screenHeight, screenWidth, screenHeight)).getBoundsInLocal())) {
            Thread.currentThread().interrupt();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Alert alert = new Alert(AlertType.WARNING, "GAME OVER !", ButtonType.OK);
                    alert.setTitle("GAME OVER");
                    alert.setOnCloseRequest(new EventHandler<DialogEvent>() {
                        @Override
                        public void handle(DialogEvent event) {
                            System.exit(0);
                        }
                    });
                    alert.showAndWait();
                }
            });
        }
        // Top wall
        else if(ballBounds.intersects((new Line(0, 0, screenWidth, 0)).getBoundsInLocal())) {
            ball.vely *= -1;
        }
        // Right wall
        else if(ballBounds.intersects((new Line(screenWidth, 0, screenWidth, screenHeight)).getBoundsInLocal())) {
            ball.velx *= -1;
        }
        // Left wall
        else if(ballBounds.intersects((new Line(0, 0, 0, screenHeight)).getBoundsInLocal())) {
            ball.velx *= -1;
        }
        
        // Check ball intersection with bricks
        Rectangle brick;
        for (Iterator<Rectangle> it = bricks.iterator(); it.hasNext();) {
            brick = it.next();
            
            double brickX = brick.getX();
            double brickY = brick.getY();
            double brickWidth = brick.getWidth();
            double brickHeight = brick.getHeight();
            
            // Bottom
            if(ballBounds.intersects((new Line(brickX, brickY + brickHeight, brickX + brickWidth, brickY + brickHeight)).getBoundsInLocal())) {
                ball.vely *= -1;
                this.destroyBrick(brick, it);
            }
            // Top
            else if(ballBounds.intersects((new Line(brickX, brickY, brickX + brickWidth, brickY)).getBoundsInLocal())) {
                ball.vely *= -1;
                this.destroyBrick(brick, it);
            }
            // Right
            else if(ballBounds.intersects((new Line(brickX + brickWidth, brickY, brickX + brickWidth, brickY + brickHeight)).getBoundsInLocal())) {
                ball.velx *= -1;
                this.destroyBrick(brick, it);
            }
            // Left
            else if(ballBounds.intersects((new Line(brickX, brickY, brickX, brickY + brickHeight)).getBoundsInLocal())) {
                ball.velx *= -1;
                this.destroyBrick(brick, it);
            }
        }
    }
}
