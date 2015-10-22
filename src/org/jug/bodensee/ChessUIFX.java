/*
 * Copyright (c) 2015, JUG Bodensee
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *    following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and 
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jug.bodensee;

import java.io.IOException;
import java.util.Scanner;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.scenicview.ScenicView;

/**
 *
 * @author MICHELB
 */
public class ChessUIFX extends Application {

    private boolean humanIsWhite = false;
    private String humanColor = "b";
    private String compColor = "w";
    private int style = 0;
    private Stage stage;

    private ChessModel myChessModell;
    private String lastClickedFigure = "11";
    private String lastClickedField = "11";
    private int computerSkill = 1;

    @Override
    public void start(Stage primaryStage) throws IOException {

        myChessModell = new ChessModel();
        final UCIController uciController = new UCIController("stockfish");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                uciController.stop();
            }
        });
        stage = primaryStage;
        stage.initStyle(StageStyle.DECORATED);
        double width = 80f;
        double heigth = 80f;
        BorderPane root = new BorderPane();
        Pane chessboardContainer = new Pane();
        chessboardContainer.getStyleClass().add("board");
        root.setCenter(chessboardContainer);
        TextArea textArea = new TextArea();
        textArea.setMinHeight(60);
        textArea.setMinWidth(140);
        CheckBox c = new CheckBox("Mensch spielt weiß");
        c.setOnAction(a -> {
            CheckBox chk = (CheckBox) a.getSource();
            System.out.println("Mensch spielt weiß ist: " + chk.isSelected());
            humanIsWhite = chk.isSelected();
            if (humanIsWhite) {
                humanColor = "w";
                compColor = "b";
            } else {
                humanColor = "b";
                compColor = "w";
            }
        });
        Slider slider = new Slider(1, 20, 1);
        slider.setValue(1);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setSnapToTicks(true);
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Slider Value Changed (newValue: " + newValue.intValue() + ")");
            computerSkill = newValue.intValue();
        });
        Button b = new Button("Neues Spiel");
        b.setOnAction(a -> {

            myChessModell.newGame();
            uciController.startNewGame((int) computerSkill);

            uciController.go();
            textArea.appendText("Neues Spiel, Computer spielt: " + compColor + "\n");
        });

        uciController.lastLineFromEngineProperty().addListener((ov, old, newValue) -> {
            Platform.runLater(() -> {
                //textArea.appendText(newValue + "\n");

                Scanner myscanner = new Scanner(newValue);
                if (myscanner.hasNext()) {
                    String word = myscanner.next();
                    if (word.equals("bestmove")) {
                        String sugMove = myscanner.next();
                        System.out.println("suggested move " + sugMove);
                        if (sugMove.equals("(none)")) {
                            textArea.appendText("Computer kann nicht ziehen oder ist Schach matt" + "\n");
                        } else {
                            String startPos, endPos;
                            startPos = myChessModell.getFieldID(sugMove.charAt(0), sugMove.charAt(1));
                            endPos = myChessModell.getFieldID(sugMove.charAt(2), sugMove.charAt(3));
                            String fen = myChessModell.getFENString(startPos, endPos);
                            boolean moveFigure = myChessModell.moveFigure(startPos, endPos, !humanIsWhite);
                            if (moveFigure) { // move was accepted 
                                textArea.appendText("Computer zieht: " + sugMove + "\n");
                            } //nothing todo
                            //send command to chess engine
                         /*   uciController.positionMoves(fen,
                             "w", myChessModell.getCastlingPossibilities(),
                             myChessModell.getEnpassant(), 
                             myChessModell.getHalfeMoveNr(),
                             myChessModell.getMoveNr("w"),sugMove);
                             }*/
                            System.out.println("Move of chessComputer from " + sugMove
                                    + " = " + startPos + " to " + endPos + " was " + moveFigure);
                        }
                    } else if (word.equals("uciok")) {
                        textArea.appendText("Computer ist bereit" + "\n");
                    }

                }
            });
        });
        VBox box = new VBox(textArea, c, slider, b);
        root.setBottom(box);
        Scene scene = new Scene(root, width * 8.5, heigth * 8.5);
        chessboardContainer.getStylesheets().add(this.getClass().getResource("chess.css").toExternalForm());
        for (int col = 0; col < 8; col++) {
            Rectangle brectangle = new Rectangle(heigth / 2, width / 2);
            brectangle.setLayoutX(width / 2);
            brectangle.setLayoutY(brectangle.getHeight() * col + heigth / 2);
            brectangle.widthProperty().bind(Bindings.divide(chessboardContainer.widthProperty(), 8.5));
            brectangle.heightProperty().bind(Bindings.divide(chessboardContainer.heightProperty(), 16.5));
            brectangle.getStyleClass().add("board");
            StackPane bstackPane = new StackPane(brectangle);
            bstackPane.getStyleClass().add("borderWrapper");
            bstackPane.layoutXProperty().bind(Bindings.add(Bindings.multiply(col, Bindings.divide(chessboardContainer.widthProperty(), 8.5)), width / 2));
            bstackPane.layoutYProperty().bind(Bindings.add(Bindings.multiply(8, Bindings.divide(chessboardContainer.heightProperty(), 8.5)), 0));
            final Label nr = new Label();
            nr.textProperty().setValue(Character.toString((char) (col + 'A')));

            nr.getStyleClass().add("boardtext");
            bstackPane.getChildren().add(nr);
            chessboardContainer.getChildren().add(bstackPane);
            bstackPane.rotateProperty().bind(chessboardContainer.rotateProperty().negate());
        }
        for (int row = 0; row < 8; row++) {
            Rectangle brectangle = new Rectangle(heigth / 2, width / 2);
            brectangle.setLayoutX(brectangle.getWidth() * row + width / 2);
            brectangle.setLayoutY(heigth / 2);
            brectangle.widthProperty().bind(Bindings.divide(chessboardContainer.widthProperty(), 16.5));
            brectangle.heightProperty().bind(Bindings.divide(chessboardContainer.heightProperty(), 8.5));
            brectangle.getStyleClass().add("board");
            StackPane bstackPane = new StackPane(brectangle);
            bstackPane.getStyleClass().add("borderWrapper");
            bstackPane.layoutXProperty().bind(Bindings.add(Bindings.multiply(0, Bindings.divide(chessboardContainer.widthProperty(), 8.5)), 0));
            bstackPane.layoutYProperty().bind(Bindings.add(Bindings.multiply(row, Bindings.divide(chessboardContainer.heightProperty(), 8.5)), 0));
            final Label nr = new Label();
            nr.textProperty().setValue(Integer.toString(8 - row));

            nr.getStyleClass().add("boardtext");
            bstackPane.getChildren().add(nr);
            chessboardContainer.getChildren().add(bstackPane);
            bstackPane.rotateProperty().bind(chessboardContainer.rotateProperty().negate());
            for (int col = 0; col < 8; col++) {
                Rectangle rectangle = new Rectangle(heigth, width);
                rectangle.setLayoutX(rectangle.getWidth() * row + width / 2);
                rectangle.setLayoutY(rectangle.getHeight() * col + heigth / 2);
                if ((row + 1 * col + 1) % 2 == 0) {
                    rectangle.getStyleClass().add("black");
                    rectangle.getStyleClass().add("field");
                } else {
                    rectangle.getStyleClass().add("white");
                    rectangle.getStyleClass().add("field");
                }

                rectangle.widthProperty().bind(Bindings.divide(chessboardContainer.widthProperty(), 8.5));
                rectangle.heightProperty().bind(Bindings.divide(chessboardContainer.heightProperty(), 8.5));

                StackPane stackPane = new StackPane(rectangle);
                stackPane.getStyleClass().add("fieldWrapper");
                Integer myrow = 7 - row;
                Integer mycol = col;

                stackPane.setId(myrow.toString() + mycol.toString());
                stackPane.layoutXProperty().bind(Bindings.add(Bindings.multiply(col, Bindings.divide(chessboardContainer.widthProperty(), 8.5)), width / 2));
                stackPane.layoutYProperty().bind(Bindings.add(Bindings.multiply(row, Bindings.divide(chessboardContainer.heightProperty(), 8.5)), 0));
                stackPane.setOnMouseClicked(e -> {
                    StackPane myfield = (StackPane) e.getSource();
                    System.out.println("field " + myfield.getId() + " clicked");
                    // store figure
                    lastClickedField = myfield.getId();
                    if (!lastClickedField.equals(lastClickedFigure)) {
                        if (myChessModell.moveFigure(lastClickedFigure, lastClickedField, humanIsWhite)) { //move is valid
                            String fen = myChessModell.getFENString(lastClickedFigure,
                                    lastClickedField);
                            uciController.positionMoves(fen,
                                    compColor, myChessModell.getCastlingPossibilities(),
                                    myChessModell.getEnpassant(),
                                    myChessModell.getHalfeMoveNr(),
                                    myChessModell.getMoveNr("b"),
                                    myChessModell.getBoardField(lastClickedFigure)
                                    + myChessModell.getBoardField(lastClickedField));
                            lastClickedField = "11";
                            lastClickedFigure = "11";

                        }
                    }
                });
                //find Figure by setting id to String <rowcol>
                //String figure = myChessModell.get(row,col);    
                //if (null != figure) {
                final Label piece = new Label();
                piece.textProperty().bind(myChessModell.getProp(7 - row, col));

                piece.setId(myrow.toString() + mycol.toString());
                piece.getStyleClass().add("piece");

                piece.setOnMouseClicked(e -> {
                    RotateTransition rotateTransition = new RotateTransition(Duration.seconds(3), piece);
                    rotateTransition.setFromAngle(0d);
                    rotateTransition.setByAngle(360d);
                    rotateTransition.play();

                    Label mypiece = (Label) e.getSource();
                    System.out.println("figure " + mypiece.getId() + " clicked");
                    // store figure
                    lastClickedFigure = mypiece.getId();
                });

                stackPane.getChildren().add(piece);
                stackPane.rotateProperty().bind(chessboardContainer.rotateProperty().negate());
                //}

                chessboardContainer.getChildren().add(stackPane);
            }
        }

        MenuBar bar = new MenuBar();
        Menu menu = new Menu("Actions");
        MenuItem rotateBoard = new MenuItem("Rotate board");
        rotateBoard.setOnAction(e -> chessboardContainer.rotateProperty().set(chessboardContainer.rotateProperty().get() + 180));

        MenuItem scenicView = new MenuItem("ScenicView");
        scenicView.setOnAction(e -> ScenicView.show(scene));

        menu.getItems().addAll(rotateBoard, scenicView);

        bar.getMenus().add(menu);

        bar.setUseSystemMenuBar(false);
        root.setTop(bar);
        primaryStage.setTitle("JUG Bodensee ChessUIFX");
        primaryStage.setScene(scene);
        primaryStage.show();
        uciController.init();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
